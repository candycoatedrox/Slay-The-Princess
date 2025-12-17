import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.ArrayList; // Import the Scanner class to read text files
import java.util.HashMap;
import java.util.Scanner;

public class Script {

    private final GameManager manager;
    private final IOHandler parser;
    private final File scriptFile;

    private final ArrayList<String> lines;
    private final HashMap<String, Integer> jumpAnchors;
    
    private int cursor = 0; // The current line index

    // --- CONSTRUCTORS ---

    /**
     * Constructor
     * @param parser the IOHandler to link this Script to
     * @param scriptFile the file containing the text of this Script
     */
    public Script(GameManager manager, IOHandler parser, File scriptFile) {
        this.manager = manager;
        this.parser = parser;
        this.scriptFile = scriptFile;

        this.lines = new ArrayList<>();
        this.jumpAnchors = new HashMap<>();
        try {
            Scanner fileReader = new Scanner(scriptFile);
            String lineContent;
            String[] args;
            String anchor;

            while (fileReader.hasNextLine()) {
                lineContent = fileReader.nextLine().trim();
                this.lines.add(lineContent);

                if (lineContent.startsWith("jumpanchor ")) {
                    args = lineContent.split(" ", 2);
                    anchor = args[1];

                    if (this.jumpAnchors.containsKey(anchor)) {
                        System.out.println("[DEBUG: Duplicate jump anchor " + anchor + " in " + scriptFile.getName() + " at line " + (this.cursor + 1) + "]");
                    } else {
                        this.jumpAnchors.put(anchor, this.lines.size() - 1);
                    }
                }
            }

            fileReader.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Script not found (FileNotFound)");
        } catch (NullPointerException e) {
            throw new RuntimeException("Script not found (NullPointer)");
        }
    }

    /**
     * Constructor
     * @param parser the IOHandler to link this Script to
     * @param fileDirectory the file path of the file containing the text of this Script
     */
    public Script(GameManager manager, IOHandler parser, String fileDirectory) {
        this(manager, parser, getFromDirectory(fileDirectory));
    }

    // --- ACCESSORS & CHECKS ---

    /**
     * Returns the line at a given index of this script
     * @param lineIndex the index of the line being retrieved
     * @return the line at index lineIndex of this script
     */
    private String getLine(int lineIndex) {
        return this.lines.get(lineIndex);
    }

    /**
     * Checks whether a given String is a valid character identifier
     * @param characterID the String to check
     * @return true if characterID corresponds to a valid character; false otherwise
     */
    private static boolean isValidCharacter(String characterID) {
        switch (characterID) {
            case "t":
            case "truth":
            case "np":
            case "narratorprincess":
            case "p":
            case "princess":
            case "n":
            case "narrator":
            case "pint":
            case "princessint":
            case "h":
            case "hero":
            case "b":
            case "broken":
            case "ch":
            case "cheated":
            case "cl":
            case "cold":
            case "cn":
            case "contra":
            case "contrarian":
            case "hu":
            case "hunted":
            case "o":
            case "oppo":
            case "opportunist":
            case "pr":
            case "para":
            case "paranoid":
            case "sk":
            case "skeptic":
            case "sm":
            case "smitten":
            case "st":
            case "stubborn":
            case "nstub":
                return true;

            default: return false;
        }
    }

    /**
     * Checks if this script has a jump anchor with the given name
     * @param jumpAnchor the name to check
     * @return true if this script has a jump anchor with jumpAnchor as its name; false otherwise
     */
    private boolean hasJumpAnchor(String jumpAnchor) {
        return this.jumpAnchors.containsKey(jumpAnchor);
    }

    /**
     * Retrieves the line index of a given jump anchor in this script
     * @param jumpAnchor the name of the jump anchor
     * @return the line index of the jump anchor with jumpAnchor as its name or null if it does not exist in this script
     */
    private Integer getJumpAnchorIndex(String jumpAnchor) {
        if (this.hasJumpAnchor(jumpAnchor)) {
            return this.jumpAnchors.get(jumpAnchor);
        } else {
            return null;
        }
    }

    // --- RUN SCRIPT ---

    /**
     * Executes this script from the cursor until the next break
     */
    public void runSection() {
        boolean cont = true;
        while (cont && this.cursor < this.lines.size()) {
            cont = this.executeLine(this.cursor);
            this.cursor += 1; // Proceed to next line
        }
    }

    /**
     * Executes this script starting from a given line index and ending at the next break
     * @param startIndex the index of the first line to execute
     * @param returnToCurrentIndex whether to reset the cursor after finishing this section
     */
    public void runSection(int startIndex, boolean returnToCurrentIndex) {
        int returnIndex = this.cursor;
        this.cursor = startIndex;
        this.runSection();
        if (returnToCurrentIndex) this.cursor = returnIndex;
    }

    /**
     * Executes this script starting from a given jump anchor and ending at the next break
     * @param anchorName the name of the jump anchor to start executing at
     * @param returnToCurrentIndex whether to reset the cursor after finishing this section
     */
    public void runSection(String anchorName, boolean returnToCurrentIndex) {
        this.runSection(this.getJumpAnchorIndex(anchorName), returnToCurrentIndex);
    }

    /**
     * Executes this script starting from a given line index and ending at the next break
     * @param startIndex the index of the first line to execute
     */
    public void runSection(int startIndex) {
        this.cursor = startIndex;
        this.runSection();
    }

    /**
     * Executes this script starting from a given jump anchor and ending at the next break
     * @param anchorName the name of the jump anchor to start executing at
     */
    public void runSection(String anchorName) {
        this.cursor = this.getJumpAnchorIndex(anchorName);
        this.runSection();
    }

    /**
     * Executes this script from the cursor until the given line index
     * @param endIndex the index to stop executing at
     */
    public void runThrough(int endIndex) {
        this.runNextLines(endIndex - this.cursor + 1);
    }

    /**
     * Executes this script from the cursor until the given jump anchor
     * @param anchorName the jump anchor to stop executing at
     */
    public void runThrough(String anchorName) {
        this.runNextLines(this.getJumpAnchorIndex(anchorName) - this.cursor + 1);
    }

    /**
     * Executes this script from a given line index until another given line index
     * @param startIndex the index of the first line to execute
     * @param endIndex the index to stop executing at
     * @param returnToCurrentIndex whether to reset the cursor after finishing this section
     */
    public void runThrough(int startIndex, int endIndex, boolean returnToCurrentIndex) {
        int returnIndex = this.cursor;
        this.cursor = startIndex;
        this.runNextLines(endIndex - this.cursor + 1);
        if (returnToCurrentIndex) this.cursor = returnIndex;
    }

    /**
     * Executes this script from a given line index until a given jump anchor
     * @param startIndex the index of the first line to execute
     * @param anchorName the name of the jump anchor to stop executing at
     * @param returnToCurrentIndex whether to reset the cursor after finishing this section
     */
    public void runThrough(int startIndex, String anchorName, boolean returnToCurrentIndex) {
        int returnIndex = this.cursor;
        this.cursor = startIndex;
        this.runNextLines(this.getJumpAnchorIndex(anchorName) - this.cursor + 1);
        if (returnToCurrentIndex) this.cursor = returnIndex;
    }

    /**
     * Executes this script from a given jump anchor until a given line index
     * @param anchorName the name of the jump anchor to start executing at
     * @param endIndex the index to stop executing at
     * @param returnToCurrentIndex whether to reset the cursor after finishing this section
     */
    public void runThrough(String anchorName, int endIndex, boolean returnToCurrentIndex) {
        this.runThrough(this.getJumpAnchorIndex(anchorName), endIndex, returnToCurrentIndex);
    }

    /**
     * Executes this script from a given jump anchor until a given line index
     * @param startAnchorName the name of the jump anchor to start executing at
     * @param endAnchorName the name of the jump anchor to stop executing at
     * @param returnToCurrentIndex whether to reset the cursor after finishing this section
     */
    public void runThrough(String startAnchorName, String endAnchorName, boolean returnToCurrentIndex) {
        this.runThrough(this.getJumpAnchorIndex(startAnchorName), this.getJumpAnchorIndex(endAnchorName), returnToCurrentIndex);
    }

    /**
     * Executes this script from a given line index until another given line index
     * @param startIndex the index of the first line to execute
     * @param endIndex the index to stop executing at
     */
    public void runThrough(int startIndex, int endIndex) {
        this.cursor = startIndex;
        this.runNextLines(endIndex - this.cursor + 1);
    }

    /**
     * Executes this script from a given line index until a given jump anchor
     * @param startIndex the index of the first line to execute
     * @param anchorName the name of the jump anchor to stop executing at
     */
    public void runThrough(int startIndex, String anchorName) {
        this.cursor = startIndex;
        this.runNextLines(this.getJumpAnchorIndex(anchorName) - this.cursor + 1);
    }

    /**
     * Executes this script from a given jump anchor until a given line index
     * @param anchorName the name of the jump anchor to start executing at
     * @param endIndex the index to stop executing at
     */
    public void runThrough(String anchorName, int endIndex) {
        this.cursor = this.getJumpAnchorIndex(anchorName);
        this.runNextLines(endIndex - this.cursor + 1);
    }

    /**
     * Executes this script from a given jump anchor until another given jump anchor
     * @param startAnchorName the name of the jump anchor to start executing at
     * @param endAnchorName the name of the jump anchor to stop executing at
     */
    public void runThrough(String startAnchorName, String endAnchorName) {
        this.cursor = this.getJumpAnchorIndex(startAnchorName);
        this.runNextLines(this.getJumpAnchorIndex(endAnchorName) - this.cursor + 1);
    }

    /**
     * Executes the next n lines in this script, starting from the cursor
     * @param nLines the number of lines to run
     */
    public void runNextLines(int nLines) {
        for (int i = 0; i < nLines; i++) {
            if (this.cursor >= this.lines.size()) {
                break;
            }

            this.executeLine(this.cursor);
            this.cursor += 1; // Proceed to next line
        }
    }

    /**
     * Executes the next n lines in this script, starting from a given line index
     * @param startIndex the index of the first line to execute
     * @param nLines the number of lines to run
     * @param returnToCurrentIndex whether to reset the cursor after finishing this section
     */
    public void runNextLines(int startIndex, int nLines, boolean returnToCurrentIndex) {
        int returnIndex = this.cursor;
        this.cursor = startIndex;
        this.runNextLines(nLines);
        if (returnToCurrentIndex) this.cursor = returnIndex;
    }

    /**
     * Executes the next n lines in this script, starting from a given jump anchor
     * @param anchorName the name of the jump anchor to start executing at
     * @param nLines the number of lines to run
     * @param returnToCurrentIndex whether to reset the cursor after finishing this section
     */
    public void runNextLines(String anchorName, int nLines, boolean returnToCurrentIndex) {
        this.runNextLines(this.getJumpAnchorIndex(anchorName), nLines, returnToCurrentIndex);
    }

    /**
     * Executes the next n lines in this script, starting from a given line index
     * @param startIndex the index of the first line to execute
     * @param nLines the number of lines to run
     */
    public void runNextLines(int startIndex, int nLines) {
        this.cursor = startIndex;
        this.runNextLines(nLines);
    }

    /**
     * Executes the next n lines in this script, starting from a given jump anchor
     * @param anchorName the name of the jump anchor to start executing at
     * @param nLines the number of lines to run
     */
    public void runNextLines(String anchorName, int nLines) {
        this.cursor = this.getJumpAnchorIndex(anchorName);
        this.runNextLines(nLines);
    }

    // --- PARSE & EXECUTE LINES ---

    /**
     * Parse and execute a single line of this script
     * @param lineIndex the index of the executed line
     * @return false if this line is a blank line representing the end of a section; true otherwise
     */
    private boolean executeLine(int lineIndex) {
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
                // add "jumpto [anchor] return"?
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

            case "nowplaying":
                manager.setNowPlaying(argument);
                break;

            case "":
                cont = false;
                break;

            case "//":
                // Comment; skip
                break;

            default:
                if (isValidCharacter(prefix)) {
                    this.printDialogueLine(lineIndex);
                } else {
                    // Invalid line; print error message and skip to next line
                    System.out.println("[DEBUG: Invalid line in file " + scriptFile.getName() + " at line " + (this.cursor + 1) + "]");
                }
        }

        return cont;
    }

    /**
     * Prints out a given number of line breaks
     * @param argument the number of line breaks to print (or an empty string, resulting in 1 line break)
     */
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
                System.out.println("[DEBUG: Invalid linebreak in file " + scriptFile.getName() + " at line " + (this.cursor + 1) + "]");
            }
        }
    }

    /**
     * Moves the cursor of this script to a given index
     * @param lineIndex the index to move to
     */
    public void jumpTo(int lineIndex) {
        this.cursor = lineIndex;
    }

    /**
     * Moves the cursor of this script to a given jump anchor
     * @param jumpAnchor the jump anchor to move to
     */
    public void jumpTo(String jumpAnchor) {
        this.jumpTo(this.getJumpAnchorIndex(jumpAnchor));
    }

    /**
     * Prints out the dialogue line specified by a given line
     * @param lineIndex the index of the dialogue line to print
     */
    public void printDialogueLine(int lineIndex) {
        String lineContent = this.getLine(lineIndex).trim();
        String[] split = lineContent.split(" ", 2);

        String prefix = split[0];
        String argument = (split.length == 2) ? split[1] : "";

        this.printDialogueLine(prefix, argument);
    }

    /**
     * Prints out the dialogue line specified by a given character identifier and line
     * @param characterID the ID of the character speaking the line
     * @param arguments the dialogue line itself, as well as any optional modifiers
     */
    private void printDialogueLine(String characterID, String arguments) {
        boolean isInterrupted = false;
        ArrayList<Voice> checkVoices = new ArrayList<>();

        String[] args = arguments.split(" /// ");
        String line = args[0];

        if (args.length == 2) {
            String modifiers = args[1];
            for (String m : modifiers.split(" ")) {
                if (m.equals("interrupt")) {
                    isInterrupted = true;
                } else if (m.startsWith("checkvoice")){
                    String[] checkIDs = m.split("-");
                    if (checkIDs.length > 1) {
                        for (String id : checkIDs) {
                            if (Voice.getVoice(id) != null) checkVoices.add(Voice.getVoice(id));
                        }
                    } else {
                        if (Voice.getVoice(characterID) != null) checkVoices.add(Voice.getVoice(characterID));
                    }
                }
            }
        }

        boolean checkResult = true;
        Voice v = Voice.getVoice(characterID);
        if (v == null) {
            if (!checkVoices.isEmpty() && parser.getCurrentCycle() != null) {
                for (Voice checkVoice : checkVoices) {
                    if (!parser.getCurrentCycle().hasVoice(checkVoice)) {
                        checkResult = false;
                    }
                }
            }

            if (characterID.equals("t") || characterID.equals("truth")) {
                if (checkResult) parser.printDialogueLine(line, isInterrupted);
            } else if (characterID.equals("p") || characterID.equals("princess")) {
                if (checkResult) parser.printDialogueLine(new PrincessDialogueLine(line, isInterrupted));
            } else {
                // Invalid character; print error message and skip to next line
                System.out.println("[DEBUG: Invalid character ID in file " + scriptFile.getName() + " at line " + (this.cursor + 1) + "]");
            }
        } else {
            if (!checkVoices.isEmpty() && parser.getCurrentCycle() != null) {
                for (Voice checkVoice : checkVoices) {
                    if (!parser.getCurrentCycle().hasVoice(checkVoice)) {
                        checkResult = false;
                    }
                }
            }

            if (checkResult) parser.printDialogueLine(new VoiceDialogueLine(v, line, isInterrupted));
        }        
    }

    // --- MISC ---

    /**
     * Retrieve a File from a given file path
     * @param directory the file path of the file containing the text of this Script
     * @return the File found at the given directory
     */
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

    /*
    public static void main(String[] args) {
        GameManager manager = new GameManager();
        IOHandler parser = new IOHandler(manager);
        
        File testFile = new File("Scripts", "TestScript.txt");
        System.out.println(testFile.getPath());
        
        Script script = new Script(parser, getFromDirectory("TestScript"));
        for (String a : script.jumpAnchors.keySet()) {
            System.out.println(a + ", " + script.jumpAnchors.get(a));
        }

        script.runSection();
        script.runSection();
    }
    */

}

/*
--- SCRIPT SYNTAX GUIDE ---

// This is a comment, and will be ignored during execution //
Trailing spaces and indentation will also be ignored during execution.

Indentation is usually used to indicate conditional dialogue, i.e. dialogue that only triggers if you have certain Voices.

Different functions a script can perform:
  - linebreak
  - linebreak [n]
        Prints out a line break. Can print out multiple line breaks at once if you specify a number, such as "linebreak 2".

  - jumpto [n]
  - jumpto [anchor]
        Moves the cursor to a given line index or jump anchor.

  - jumpanchor [id]
        Essentially acts as a label the script can move its cursor to at any time.

  - nowplaying [song]
        Sets the song currently playing.

  - [character] Dialogue line goes here
  - [character] Dialogue line goes here /// [modifiers]
        Modifiers are optional.
        The first word specifies the ID of the speaking character, then anything after that is considered the actual dialogue line.
        Including " /// " at the end of the line allows you to toggle additional modifiers for this dialogue line.

        Modifiers:
          - checkvoice
                Checks whether the player has the speaker's voice before printing.
          - checkvoice-[id]
                Checks whether the player has the voice specified by the ID before printing.
                (Multiple voices can be specified, as long as they are separated with hyphens.)
          - interrupt
                The line is interrupted.
*/