import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.ArrayList; // Import the Scanner class to read text files
import java.util.HashMap;
import java.util.Scanner;

public class Script {

    private final IOHandler parser;
    private final File scriptFile;

    private final ArrayList<String> lines;
    private final HashMap<String, Integer> jumpAnchors;
    
    private int currentLineIndex = 1;

    // --- CONSTRUCTORS ---

    public Script(IOHandler parser, File scriptFile) {
        this.parser = parser;
        this.scriptFile = scriptFile;

        this.lines = new ArrayList<>();
        this.jumpAnchors = new HashMap<>();
        try {
            Scanner fileReader = new Scanner(scriptFile);
            String lineContent;
            String anchor;

            while (fileReader.hasNextLine()) {
                lineContent = fileReader.nextLine().trim();
                this.lines.add(lineContent);

                if (lineContent.startsWith("jumpAnchor ")) {
                    anchor = lineContent.split(" ", 2)[1];
                    this.jumpAnchors.put(anchor, this.lines.size());
                }
            }

            fileReader.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Script not found");
        }
    }

    public Script(IOHandler parser, String fileDirectory) {
        this(parser, getFromDirectory(fileDirectory));
    }

    // --- ACCESSORS & CHECKS ---

    private String getLine(int lineIndex) {
        return this.lines.get(lineIndex);
    }

    private static boolean isValidCharacter(String characterID) {
        switch (characterID) {
            case "truth":
            case "p":
            case "n":
            case "pint":
            case "hero":
            case "broken":
            case "cheated":
            case "cold":
            case "contra":
            case "hunted":
            case "oppo":
            case "para":
            case "skeptic":
            case "smitten":
            case "stubborn":
                return true;

            default: return false;
        }
    }

    private boolean hasJumpAnchor(String jumpAnchor) {
        return this.jumpAnchors.containsKey(jumpAnchor);
    }

    private Integer getJumpAnchorIndex(String jumpAnchor) {
        if (this.hasJumpAnchor(jumpAnchor)) {
            return this.jumpAnchors.get(jumpAnchor);
        } else {
            return null;
        }
    }

    // --- RUN SCRIPT ---

    public void runSection() {
        boolean cont = true;
        while (cont) {
            cont = this.parseLine(this.currentLineIndex);
            this.currentLineIndex += 1; // Proceed to next line
        }
    }

    public void runSection(int startIndex, boolean returnToCurrentIndex) {
        int returnIndex = this.currentLineIndex;
        this.currentLineIndex = startIndex;
        this.runSection();
        if (returnToCurrentIndex) this.currentLineIndex = returnIndex;
    }

    public void runSection(String anchorName, boolean returnToCurrentIndex) {
        this.runSection(this.getJumpAnchorIndex(anchorName), returnToCurrentIndex);
    }

    public void runSection(int startIndex) {
        this.currentLineIndex = startIndex;
        this.runSection();
    }

    public void runSection(String anchorName) {
        this.currentLineIndex = this.getJumpAnchorIndex(anchorName);
        this.runSection();
    }

    public void runThrough(int endIndex) {
        this.runNextLines(endIndex - this.currentLineIndex + 1);
    }

    public void runThrough(String anchorName) {
        this.runNextLines(this.getJumpAnchorIndex(anchorName) - this.currentLineIndex + 1);
    }

    public void runThrough(int startIndex, int endIndex, boolean returnToCurrentIndex) {
        int returnIndex = this.currentLineIndex;
        this.currentLineIndex = startIndex;
        this.runNextLines(endIndex - this.currentLineIndex + 1);
        if (returnToCurrentIndex) this.currentLineIndex = returnIndex;
    }

    public void runThrough(int startIndex, String anchorName, boolean returnToCurrentIndex) {
        int returnIndex = this.currentLineIndex;
        this.currentLineIndex = startIndex;
        this.runNextLines(this.getJumpAnchorIndex(anchorName) - this.currentLineIndex + 1);
        if (returnToCurrentIndex) this.currentLineIndex = returnIndex;
    }

    public void runThrough(String anchorName, int endIndex, boolean returnToCurrentIndex) {
        this.runThrough(this.getJumpAnchorIndex(anchorName), endIndex, returnToCurrentIndex);
    }

    public void runThrough(int startIndex, int endIndex) {
        this.currentLineIndex = startIndex;
        this.runNextLines(endIndex - this.currentLineIndex + 1);
    }

    public void runThrough(int startIndex, String anchorName) {
        this.currentLineIndex = startIndex;
        this.runNextLines(this.getJumpAnchorIndex(anchorName) - this.currentLineIndex + 1);
    }

    public void runThrough(String anchorName, int endIndex) {
        this.currentLineIndex = this.getJumpAnchorIndex(anchorName);
        this.runNextLines(endIndex - this.currentLineIndex + 1);
    }

    public void runThrough(String startAnchorName, String endAnchorName) {
        this.currentLineIndex = this.getJumpAnchorIndex(startAnchorName);
        this.runNextLines(this.getJumpAnchorIndex(endAnchorName) - this.currentLineIndex + 1);
    }

    public void runNextLines(int nLines) {
        for (int i = 0; i < nLines; i++) {
            this.parseLine(this.currentLineIndex);
            this.currentLineIndex += 1; // Proceed to next line
        }
    }

    public void runNextLines(int startIndex, int nLines, boolean returnToCurrentIndex) {
        int returnIndex = this.currentLineIndex;
        this.currentLineIndex = startIndex;
        this.runNextLines(nLines);
        if (returnToCurrentIndex) this.currentLineIndex = returnIndex;
    }

    public void runNextLines(String anchorName, int nLines, boolean returnToCurrentIndex) {
        this.runNextLines(this.getJumpAnchorIndex(anchorName), nLines, returnToCurrentIndex);
    }

    public void runNextLines(int startIndex, int nLines) {
        this.currentLineIndex = startIndex;
        this.runNextLines(nLines);
    }

    public void runNextLines(String anchorName, int nLines) {
        this.currentLineIndex = this.getJumpAnchorIndex(anchorName);
        this.runNextLines(nLines);
    }

    // --- PARSE LINES ---

    private boolean parseLine(int lineIndex) {
        // false if end of section (ie line break), true otherwise
        String lineContent = this.getLine(lineIndex).trim();
        String[] split = lineContent.split(" ", 2);

        String prefix = split[0];
        String argument = (split.length == 2) ? split[1] : "";

        boolean cont = true;
        switch (prefix) {
            case "linebreak":
                lineBreak(argument);
                break;

            case "jumpto":
                try {
                    int jumpTarget = Integer.parseInt(argument);
                    this.jumpTo(jumpTarget);
                } catch (NumberFormatException e) {
                    this.jumpTo(argument);
                }

                break;

            case "jumpanchor":
                // Skip to next line
                break;

            case "":
                cont = false;
                break;

            case "//":
                // Comment; skip
                break;

            default:
                if (isValidCharacter(argument)) {
                    this.printDialogueLine(lineIndex);
                } else {
                    // Invalid line; print error message and skip to next line
                    System.out.println("[DEBUG: Invalid line in file " + scriptFile.getName() + " at line " + this.currentLineIndex + "]");
                }
        }

        return cont;
    }

    private void lineBreak(String argument) {
        try {
            int nBreaks = Integer.parseInt(argument);
            for (int i = 0; i < nBreaks; i++) {
                System.out.println();
            }
        } catch (NumberFormatException e) {
            if (argument.equals("")) {
                System.out.println();
            } else {
                // Invalid line; print error message and skip to next line
                System.out.println("[DEBUG: Invalid linebreak in file " + scriptFile.getName() + " at line " + this.currentLineIndex + "]");
            }
        }
    }

    public void jumpTo(int lineIndex) {
        this.currentLineIndex = lineIndex;
    }

    public void jumpTo(String lineAnchor) {
        this.jumpTo(this.getJumpAnchorIndex(lineAnchor));
    }

    public void printDialogueLine(int lineIndex) {
        String lineContent = this.getLine(lineIndex).trim();
        String[] split = lineContent.split(" ", 2);

        String prefix = split[0];
        String argument = (split.length == 2) ? split[1] : "";

        this.printDialogueLine(prefix, argument);
    }

    private void printDialogueLine(String characterID, String arguments) {
        // check for modifiers, then print
        boolean isInterrupted = false;
        boolean checkVoice = false;

        String[] args = arguments.split(" /// ");
        String line = args[0];

        if (args.length == 2) {
            if (args[1].contains("interrupt")) {
                isInterrupted = true;
            }
            if (args[1].contains("checkvoice")) {
                checkVoice = true;
            }
        }

        Voice v = Voice.getVoice(characterID);
        if (v == null) {
            if (characterID.equals("truth")) {
                parser.printDialogueLine(line, isInterrupted);
            } else if (characterID.equals("p")) {
                parser.printDialogueLine(new PrincessDialogueLine(line, isInterrupted));
            } else {
                // Invalid character; print error message and skip to next line
                System.out.println("[DEBUG: Invalid character ID in file " + scriptFile.getName() + " at line " + this.currentLineIndex + "]");
                return;
            }
        } else {
            if (checkVoice && parser.getCurrentCycle() != null) {
                if (parser.getCurrentCycle().hasVoice(v)) {
                    parser.printDialogueLine(new VoiceDialogueLine(v, line, isInterrupted));
                }
            } else {
                parser.printDialogueLine(new VoiceDialogueLine(v, line, isInterrupted));
            }
        }        
    }

    // --- MISC ---

    public static File getFromDirectory(String directory) {
        String[] path = directory.split("/");
        File currentDirectory = new File("Scripts");

        if (path.length == 0) {
            throw new RuntimeException("No file name given");
        }

        for (int i = 0; i < path.length; i++) {
            if (i == path.length - 1) {
                currentDirectory = new File(currentDirectory, path[i] + ".txt");
            } else {
                currentDirectory = new File(currentDirectory, path[i]);
            }
        }

        return currentDirectory;
    }

}

/*
--- SCRIPT SYNTAX GUIDE ---

// Comment //

linebreak
linebreak num

jumpto num
jumpto anchor

jumpanchor id

character Dialogue line goes here
character Dialogue line goes here /// modifiers modifiers

modifiers:
  - character Dialogue line goes here /// checkvoice
        check whether you have the given voice before printing
  - character Dialogue line goes here /// interrupt
        the line is interrupted
*/