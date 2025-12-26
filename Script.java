import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner; // Import the Scanner class to read text files

public class Script {

    protected final GameManager manager;
    protected final IOHandler parser;
    protected final File source;

    protected final ArrayList<String> lines;
    protected final HashMap<String, Integer> labels;
    
    private int cursor = 0; // The current line index
    private boolean boolCondition = false;
    private int intCondition = 100;
    private String strCondition = "";

    private static final DialogueLine CLAIMFOLD = new DialogueLine("Something reaches out and folds her into its myriad arms.");

    // --- CONSTRUCTORS ---

    /**
     * Constructor
     * @param manager the GameManager to link this Script to
     * @param parser the IOHandler to link this Script to
     * @param source the file containing the text of this Script
     */
    public Script(GameManager manager, IOHandler parser, File source) {
        this.manager = manager;
        this.parser = parser;
        this.source = source;

        this.lines = new ArrayList<>();
        this.labels = new HashMap<>();
        try {
            Scanner fileReader = new Scanner(source);
            String lineContent;
            String[] args;
            String label;

            while (fileReader.hasNextLine()) {
                lineContent = fileReader.nextLine().trim();
                this.lines.add(lineContent);

                if (lineContent.startsWith("label ")) {
                    args = lineContent.split(" ", 2);
                    label = args[1];

                    if (this.labels.containsKey(label)) {
                        System.out.println("[DEBUG: Duplicate label " + label + " in " + source.getName() + " at line " + (this.lines.size()) + "]");
                    } else {
                        this.labels.put(label, this.lines.size() - 1);
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
     * @param manager the GameManager to link this Script to
     * @param parser the IOHandler to link this Script to
     * @param fileDirectory the file path of the file containing the text of this Script
     */
    public Script(GameManager manager, IOHandler parser, String fileDirectory) {
        this(manager, parser, getFromDirectory(fileDirectory));
    }

    // --- ACCESSORS & CHECKS ---

    /**
     * Returns the number of lines in this script
     * @return the number of lines in this script
     */
    protected int nLines() {
        return this.lines.size();
    }

    /**
     * Returns the line at a given index of this script
     * @param lineIndex the index of the line being retrieved
     * @return the line at index lineIndex of this script
     */
    protected String getLine(int lineIndex) {
        return this.lines.get(lineIndex);
    }

    /**
     * Checks whether a given String is a valid character identifier
     * @param characterID the String to check
     * @return true if characterID corresponds to a valid character; false otherwise
     */
    protected static boolean isValidCharacter(String characterID) {
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
            case "dragon":
            case "uext":
            case "unknownext":
            case "hext":
            case "heroext":
            case "cext":
            case "coldext":
            case "oext":
            case "oppoext":
            case "opportunistext":
            case "nstub":
            case "stubcont":
            case "paraskep":
                return true;

            default: return false;
        }
    }

    /**
     * Checks if this script has a label with the given name
     * @param label the name to check
     * @return true if this script has a label with label as its name; false otherwise
     */
    protected boolean hasLabel(String label) {
        return this.labels.containsKey(label);
    }

    /**
     * Retrieves the line index of a given label in this script
     * @param label the name of the label
     * @return the line index of the label with label as its name or null if it does not exist in this script
     * @throws IllegalArgumentException if the given label does not exist within this Script
     */
    private Integer getLabelIndex(String label) {
        if (this.hasLabel(label)) {
            return this.labels.get(label);
        } else {
            throw new IllegalArgumentException("Label " + label + " does not exist");
        }
    }

    /**
     * Resets all conditional switches to their default values
     */
    private void resetConditions() {
        this.boolCondition = false;
        this.intCondition = 100;
        this.strCondition = "";
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

        this.resetConditions();
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
     * Executes this script starting from a given label and ending at the next break
     * @param labelName the name of the label to start executing at
     * @param returnToCurrentIndex whether to reset the cursor after finishing this section
     */
    public void runSection(String labelName, boolean returnToCurrentIndex) {
        try {
            this.runSection(this.getLabelIndex(labelName), returnToCurrentIndex);
        } catch (IllegalArgumentException e) {
            System.out.println("[DEBUG: Label " + labelName + " does not exist in " + source.getName() + "]");
        }        
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
     * Executes this script starting from a given label and ending at the next break
     * @param labelName the name of the label to start executing at
     */
    public void runSection(String labelName) {
        try {
            this.cursor = this.getLabelIndex(labelName);
            this.runSection();
        } catch (IllegalArgumentException e) {
            System.out.println("[DEBUG: Label " + labelName + " does not exist in " + source.getName() + "]");
        }
    }

    /**
     * Executes this script from the cursor until the next break, using a given boolean, int, and String as conditional switches
     * @param boolCondition the boolean condition to use as a switch
     * @param intCondition the int to use as a switch
     * @param strCondition the String to use as a switch
     */
    public void runConditionalSection(boolean boolCondition, int intCondition, String strCondition) {
        this.boolCondition = boolCondition;
        this.intCondition = intCondition;
        this.strCondition = strCondition;
        this.runSection();
    }

    /**
     * Executes this script starting from a given label and ending at the next break, using a given boolean, int, and String as conditional switches
     * @param labelName the name of the label to start executing at
     * @param boolCondition the boolean condition to use as a switch
     * @param intCondition the int to use as a switch
     * @param strCondition the String to use as a switch
     */
    public void runConditionalSection(String labelName, boolean boolCondition, int intCondition, String strCondition) {
        try {
            this.cursor = this.getLabelIndex(labelName);
            this.boolCondition = boolCondition;
            this.intCondition = intCondition;
            this.strCondition = strCondition;
            this.runSection();
        } catch (IllegalArgumentException e) {
            System.out.println("[DEBUG: Label " + labelName + " does not exist in " + source.getName() + "]");
        }
    }

    /**
     * Executes this script from the cursor until the next break, using a given boolean and int as conditional switches
     * @param boolCondition the boolean condition to use as a switch
     * @param intCondition the int to use as a switch
     */
    public void runConditionalSection(boolean boolCondition, int intCondition) {
        this.boolCondition = boolCondition;
        this.intCondition = intCondition;
        this.runSection();
    }

    /**
     * Executes this script starting from a given label and ending at the next break, using a given boolean and int as conditional switches
     * @param labelName the name of the label to start executing at
     * @param boolCondition the boolean condition to use as a switch
     * @param intCondition the int to use as a switch
     */
    public void runConditionalSection(String labelName, boolean boolCondition, int intCondition) {
        try {
            this.cursor = this.getLabelIndex(labelName);
            this.boolCondition = boolCondition;
            this.intCondition = intCondition;
            this.runSection();
        } catch (IllegalArgumentException e) {
            System.out.println("[DEBUG: Label " + labelName + " does not exist in " + source.getName() + "]");
        }
    }

    /**
     * Executes this script from the cursor until the next break, using a given boolean and String as conditional switches
     * @param boolCondition the boolean condition to use as a switch
     * @param strCondition the String to use as a switch
     */
    public void runConditionalSection(boolean boolCondition, String strCondition) {
        this.boolCondition = boolCondition;
        this.strCondition = strCondition;
        this.runSection();
    }

    /**
     * Executes this script starting from a given label and ending at the next break, using a given boolean and String as conditional switches
     * @param labelName the name of the label to start executing at
     * @param boolCondition the boolean condition to use as a switch
     * @param strCondition the String to use as a switch
     */
    public void runConditionalSection(String labelName, boolean boolCondition, String strCondition) {
        try {
            this.cursor = this.getLabelIndex(labelName);
            this.boolCondition = boolCondition;
            this.strCondition = strCondition;
            this.runSection();
        } catch (IllegalArgumentException e) {
            System.out.println("[DEBUG: Label " + labelName + " does not exist in " + source.getName() + "]");
        }
    }

    /**
     * Executes this script from the cursor until the next break, using a given int and String as conditional switches
     * @param intCondition the int to use as a switch
     * @param strCondition the String to use as a switch
     */
    public void runConditionalSection(int intCondition, String strCondition) {
        this.intCondition = intCondition;
        this.strCondition = strCondition;
        this.runSection();
    }

    /**
     * Executes this script starting from a given label and ending at the next break, using a given int and String as conditional switches
     * @param labelName the name of the label to start executing at
     * @param intCondition the int to use as a switch
     * @param strCondition the String to use as a switch
     */
    public void runConditionalSection(String labelName, int intCondition, String strCondition) {
        try {
            this.cursor = this.getLabelIndex(labelName);
            this.intCondition = intCondition;
            this.strCondition = strCondition;
            this.runSection();
        } catch (IllegalArgumentException e) {
            System.out.println("[DEBUG: Label " + labelName + " does not exist in " + source.getName() + "]");
        }
    }

    /**
     * Executes this script from the cursor until the next break, using a given boolean as a conditional switch
     * @param condition the boolean condition to use as a switch
     */
    public void runConditionalSection(boolean condition) {
        this.boolCondition = condition;
        this.runSection();
    }

    /**
     * Executes this script starting from a given label and ending at the next break, using a given boolean as a conditional switch
     * @param labelName the name of the label to start executing at
     * @param condition the boolean condition to use as a switch
     */
    public void runConditionalSection(String labelName, boolean condition) {
        try {
            this.cursor = this.getLabelIndex(labelName);
            this.boolCondition = condition;
            this.runSection();
        } catch (IllegalArgumentException e) {
            System.out.println("[DEBUG: Label " + labelName + " does not exist in " + source.getName() + "]");
        }
    }

    /**
     * Executes this script from the cursor until the next break, using a given AbstractCondition as a conditional switch
     * @param condition the condition to use as a switch
     */
    public void runConditionalSection(AbstractCondition condition) {
        this.boolCondition = condition.check();
        this.runSection();
    }

    /**
     * Executes this script starting from a given label and ending at the next break, using a given AbstractCondition as a conditional switch
     * @param labelName the name of the label to start executing at
     * @param condition the condition to use as a switch
     */
    public void runConditionalSection(String labelName, AbstractCondition condition) {
        try {
            this.cursor = this.getLabelIndex(labelName);
            this.boolCondition = condition.check();
            this.runSection();
        } catch (IllegalArgumentException e) {
            System.out.println("[DEBUG: Label " + labelName + " does not exist in " + source.getName() + "]");
        }
    }

    /**
     * Executes this script from the cursor until the next break, using a given int as a conditional switch
     * @param condition the int to use as a switch
     */
    public void runConditionalSection(int condition) {
        this.intCondition = condition;
        this.runSection();
    }

    /**
     * Executes this script starting from a given label and ending at the next break, using a given int as a conditional switch
     * @param labelName the name of the label to start executing at
     * @param condition the int to use as a switch
     */
    public void runConditionalSection(String labelName, int condition) {
        try {
            this.cursor = this.getLabelIndex(labelName);
            this.intCondition = condition;
            this.runSection();
        } catch (IllegalArgumentException e) {
            System.out.println("[DEBUG: Label " + labelName + " does not exist in " + source.getName() + "]");
        }
    }

    /**
     * Executes this script from the cursor until the next break, using a given GlobalInt as a conditional switch
     * @param condition the GlobalInt to use as a switch
     */
    public void runConditionalSection(GlobalInt condition) {
        this.intCondition = condition.check();
        this.runSection();
    }

    /**
     * Executes this script starting from a given label and ending at the next break, using a given GlobalInt as a conditional switch
     * @param labelName the name of the label to start executing at
     * @param condition the GlobalInt to use as a switch
     */
    public void runConditionalSection(String labelName, GlobalInt condition) {
        try {
            this.cursor = this.getLabelIndex(labelName);
            this.intCondition = condition.check();
            this.runSection();
        } catch (IllegalArgumentException e) {
            System.out.println("[DEBUG: Label " + labelName + " does not exist in " + source.getName() + "]");
        }
    }

    /**
     * Executes this script from the cursor until the next break, using a given String as a conditional switch
     * @param condition the String to use as a switch
     */
    public void runConditionalSection(String condition) {
        this.strCondition = condition;
        this.runSection();
    }

    /**
     * Executes this script starting from a given label and ending at the next break, using a given String as a conditional switch
     * @param labelName the name of the label to start executing at
     * @param condition the String to use as a switch
     */
    public void runConditionalSection(String labelName, String condition) {
        try {
            this.cursor = this.getLabelIndex(labelName);
            this.strCondition = condition;
            this.runSection();
        } catch (IllegalArgumentException e) {
            System.out.println("[DEBUG: Label " + labelName + " does not exist in " + source.getName() + "]");
        }
    }

    /**
     * Executes this script while claiming a vessel, redirecting to one of two sections depending on whether or not the player has already claimed at least one vessel
     * @param labelPrefix the prefix of the label to start executing at
     * @param skipFirstLineBreak whether to skip the first line break of the pre-claim sequence
     */
    public void runClaimSection(String labelPrefix, boolean skipFirstLineBreak) {
        this.claimFoldLine(skipFirstLineBreak);
        this.firstSwitchJump(labelPrefix);
        this.runSection();
    }

    /**
     * Executes this script while claiming a vessel, redirecting to one of two sections depending on whether or not the player has already claimed at least one vessel
     * @param labelPrefix the prefix of the label to start executing at
     */
    public void runClaimSection(String labelPrefix) {
        this.runClaimSection(labelPrefix, false);
    }

    /**
     * Executes one of two sections of this script, depending on whether the player has the blade or not
     * @param labelPrefix the prefix of the label to start executing at
     */
    public void runBladeSection(String labelPrefix) {
        this.bladeSwitchJump(labelPrefix);
        this.runSection();
    }

    /**
     * Executes one of two sections of this script, depending on whether the player has the blade or not
     * @param labelPrefix the prefix of the label to start executing at
     * @param labelSuffix the suffix of the label to start executing at
     */
    public void runBladeSection(String labelPrefix, String labelSuffix) {
        this.bladeSwitchJump(labelPrefix, labelSuffix);
        this.runSection();
    }

    /**
     * Executes one of two sections of this script, depending on whether the player has the blade or not
     * @param labelSuffix the suffix of the label to start executing at
     */
    public void runSourceSection(String labelSuffix) {
        this.sourceSwitchJump(labelSuffix);
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
     * Executes this script from the cursor until the given label
     * @param labelName the label to stop executing at
     */
    public void runThrough(String labelName) {
        try {
            this.runNextLines(this.getLabelIndex(labelName) - this.cursor + 1);
        } catch (IllegalArgumentException e) {
            System.out.println("[DEBUG: Label " + labelName + " does not exist in " + source.getName() + "]");
        }
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
     * Executes this script from a given line index until a given label
     * @param startIndex the index of the first line to execute
     * @param labelName the name of the label to stop executing at
     * @param returnToCurrentIndex whether to reset the cursor after finishing this section
     */
    public void runThrough(int startIndex, String labelName, boolean returnToCurrentIndex) {
        int returnIndex = this.cursor;
        this.cursor = startIndex;

        try {
            this.runNextLines(this.getLabelIndex(labelName) - this.cursor + 1);
            if (returnToCurrentIndex) this.cursor = returnIndex;
        } catch (IllegalArgumentException e) {
            System.out.println("[DEBUG: Label " + labelName + " does not exist in " + source.getName() + "]");
        }
    }

    /**
     * Executes this script from a given label until a given line index
     * @param labelName the name of the label to start executing at
     * @param endIndex the index to stop executing at
     * @param returnToCurrentIndex whether to reset the cursor after finishing this section
     */
    public void runThrough(String labelName, int endIndex, boolean returnToCurrentIndex) {
        try {
            this.runThrough(this.getLabelIndex(labelName), endIndex, returnToCurrentIndex);
        } catch (IllegalArgumentException e) {
            System.out.println("[DEBUG: Label " + labelName + " does not exist in " + source.getName() + "]");
        }
    }

    /**
     * Executes this script from a given label until a given line index
     * @param startLabelName the name of the label to start executing at
     * @param endLabelName the name of the label to stop executing at
     * @param returnToCurrentIndex whether to reset the cursor after finishing this section
     */
    public void runThrough(String startLabelName, String endLabelName, boolean returnToCurrentIndex) {
        try {
            this.runThrough(this.getLabelIndex(startLabelName), this.getLabelIndex(endLabelName), returnToCurrentIndex);
        } catch (IllegalArgumentException e) {
            if (e.getLocalizedMessage().contains(startLabelName)) {
                System.out.println("[DEBUG: Label " + startLabelName + " does not exist in " + source.getName() + "]");
            } else {
                System.out.println("[DEBUG: Label " + endLabelName + " does not exist in " + source.getName() + "]");
            }
        }
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
     * Executes this script from a given line index until a given label
     * @param startIndex the index of the first line to execute
     * @param labelName the name of the label to stop executing at
     */
    public void runThrough(int startIndex, String labelName) {
        this.cursor = startIndex;
        try {
            this.runNextLines(this.getLabelIndex(labelName) - this.cursor + 1);
        } catch (IllegalArgumentException e) {
            System.out.println("[DEBUG: Label " + labelName + " does not exist in " + source.getName() + "]");
        }
    }

    /**
     * Executes this script from a given label until a given line index
     * @param labelName the name of the label to start executing at
     * @param endIndex the index to stop executing at
     */
    public void runThrough(String labelName, int endIndex) {
        try {
            this.cursor = this.getLabelIndex(labelName);
            this.runNextLines(endIndex - this.cursor + 1);
        } catch (IllegalArgumentException e) {
            System.out.println("[DEBUG: Label " + labelName + " does not exist in " + source.getName() + "]");
        }
    }

    /**
     * Executes this script from a given label until another given label
     * @param startLabelName the name of the label to start executing at
     * @param endLabelName the name of the label to stop executing at
     */
    public void runThrough(String startLabelName, String endLabelName) {
        try {
            this.cursor = this.getLabelIndex(startLabelName);
            this.runNextLines(this.getLabelIndex(endLabelName) - this.cursor + 1);
        } catch (IllegalArgumentException e) {
            System.out.println("[DEBUG: Label " + startLabelName + " does not exist in " + source.getName() + "]");
        }
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

        this.resetConditions();
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
     * Executes the next n lines in this script, starting from a given label
     * @param labelName the name of the label to start executing at
     * @param nLines the number of lines to run
     * @param returnToCurrentIndex whether to reset the cursor after finishing this section
     */
    public void runNextLines(String labelName, int nLines, boolean returnToCurrentIndex) {
        try {
            this.runNextLines(this.getLabelIndex(labelName), nLines, returnToCurrentIndex);
        } catch (IllegalArgumentException e) {
            System.out.println("[DEBUG: Label " + labelName + " does not exist in " + source.getName() + "]");
        }
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
     * Executes the next n lines in this script, starting from a given label
     * @param labelName the name of the label to start executing at
     * @param nLines the number of lines to run
     */
    public void runNextLines(String labelName, int nLines) {
        try {
            this.cursor = this.getLabelIndex(labelName);
            this.runNextLines(nLines);
        } catch (IllegalArgumentException e) {
            System.out.println("[DEBUG: Label " + labelName + " does not exist in " + source.getName() + "]");
        }
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
        String argument = "";
        String[] args;
        String modifiers = "";
        String[] mods;
        try {
            argument = split[1];
            split = argument.split(" /// ");
            argument = split[0];
            args = argument.split(" ", 2);

            if (split.length == 1) {
                mods = new String[0];
            } else {
                modifiers = split[1];
                mods = modifiers.split(" ");
            }
        } catch (IndexOutOfBoundsException e) {
            args = new String[0];
            mods = new String[0];
        }

        boolean cont = true;
        switch (prefix) {
            case "//":
                // Comment; skip
                break;

            case "label":
                // Skip to next line
                break;

            case "":
                cont = false;
                break;

            case "linebreak":
                if (this.runModifierChecks(mods)) this.lineBreak(argument);
                break;

            case "jumpto":
                // add "jumpto [label] return"?
                if (this.runModifierChecks(mods)) {
                   try {
                        int jumpTarget = Integer.parseInt(argument);
                        this.jumpTo(jumpTarget);
                    } catch (NumberFormatException e) {
                        this.jumpTo(argument);
                    } 
                }
                
                break;

            case "firstswitch":
            case "claim":
                if (args.length == 0) {
                    // Invalid line; print error message and skip to next line
                    System.out.println("[DEBUG: Invalid claim command in file " + source.getName() + " at line " + (this.cursor + 1) + "]");
                    break;
                }

                if (this.runModifierChecks(mods)) this.firstSwitchJump(argument);
                break;

            case "bladeswitch":
                switch (args.length) {
                    case 0:
                        // Invalid line; print error message and skip to next line
                        System.out.println("[DEBUG: Invalid bladeswitch in file " + source.getName() + " at line " + (this.cursor + 1) + "]");
                        break;

                    case 1:
                        if (this.runModifierChecks(mods)) this.bladeSwitchJump(argument);
                        break;
                    
                    default: if (this.runModifierChecks(mods)) this.bladeSwitchJump(args[0], args[1]);
                }

                break;

            case "sourceswitch":
                if (args.length == 0) {
                    // Invalid line; print error message and skip to next line
                    System.out.println("[DEBUG: Invalid claim command in file " + source.getName() + " at line " + (this.cursor + 1) + "]");
                    break;
                }

                if (this.runModifierChecks(mods)) this.sourceSwitchJump(argument);
                break;
            
            case "switchjump":
                if (this.runModifierChecks(mods)) this.boolSwitchJumpTo(argument);
                break;
            
            case "numswitchjump":
                if (this.runModifierChecks(mods)) this.numSwitchJumpTo(argument, false);
                break;
            case "numautojump":
                if (this.runModifierChecks(mods)) this.numSwitchJumpTo(argument, true);
                break;
            
            case "stringswitchjump":
                if (this.runModifierChecks(mods)) this.strSwitchJumpTo(argument, false);
                break;
            case "stringautojump":
                if (this.runModifierChecks(mods)) this.strSwitchJumpTo(argument, true);
                break;

            case "nowplaying":
                if (this.runModifierChecks(mods)) manager.setNowPlaying(argument);
                break;

            case "quietcreep":
                if (this.runModifierChecks(mods)) this.quietCreep();
                break;
            case "claimfold":
                if (this.runModifierChecks(mods)) this.claimFoldLine();
                break;

            default:
                if (isValidCharacter(prefix)) {
                    this.printDialogueLine(lineIndex);
                } else {
                    // Invalid line; print error message and skip to next line
                    System.out.println("[DEBUG: Invalid line in file " + source.getName() + " at line " + (this.cursor + 1) + "]");
                }
        }

        return cont;
    }

    /**
     * Runs the appropriate checks for any modifiers on a given Voice dialogue line
     * @param modifiers the modifiers to run checks for
     * @param speaker the Voice this line is spoken by
     * @return true if all modifier checks pass and the line should be printed; false otherwise
     */
    private boolean runModifierChecks(String[] modifiers, Voice speaker) {
        if (modifiers.length == 0) return true;

        Cycle currentCycle = manager.getCurrentCycle();
        boolean firstVessel = (currentCycle == null) ? false : currentCycle.isFirstVessel();
        boolean hasBlade = (currentCycle == null) ? false : currentCycle.hasBlade();
        String source = (currentCycle == null) ? "" : currentCycle.getSource();

        HashMap<Voice, Boolean> voiceChecks = new HashMap<>();
        String[] args;
        int targetInt;

        for (String m : modifiers) {
            args = m.split("-");

            if (m.startsWith("checkvoice")) {
                if (args.length == 1) {
                    if (speaker != null) voiceChecks.put(speaker, true);
                } else {
                    for (String id : args) {
                        if (Voice.getVoice(id) != null) {
                            voiceChecks.put(Voice.getVoice(id), true);
                        }
                    }
                }
            } else if (m.startsWith("checknovoice-")) {
                for (String id : args) {
                    if (Voice.getVoice(id) != null) {
                        voiceChecks.put(Voice.getVoice(id), false);
                    }
                }
            } else if (m.equals("firstvessel")) {
                if (!firstVessel) return false;
            } else if (m.equals("notfirstvessel")) {
                if (firstVessel) return false;
            } else if (m.equals("hasblade")) {
                if (!hasBlade) return false;
            } else if (m.equals("noblade")) {
                if (hasBlade) return false;
            } else if (m.startsWith("ifsource-")) {
                if (!source.equals(args[1])) return false;
            } else if (m.startsWith("ifsourcenot-")) {
                if (source.equals(args[1])) return false;
            } else if (m.equals("check")) {
                if (!this.boolCondition) return false;
            } else if (m.equals("checkfalse")) {
                if (this.boolCondition) return false;
            } else if (m.startsWith("ifnum")) {
                if (args.length == 2) {
                    try {
                        targetInt = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        targetInt = 0;
                    }
                } else {
                    targetInt = 0;
                }

                if (this.intCondition != targetInt) return false;
            } else if (m.startsWith("ifnumnot")) {
                if (args.length == 2) {
                    try {
                        targetInt = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        targetInt = 0;
                    }
                } else {
                    targetInt = 0;
                }

                if (this.intCondition == targetInt) return false;
            } else if (m.startsWith("ifstring-")) {
                if (!strCondition.equals(args[1])) return false;
            } else if (m.startsWith("ifstringnot-")) {
                if (strCondition.equals(args[1])) return false;
            }
        }

        if (currentCycle != null && !voiceChecks.isEmpty()) {
            for (Voice checkVoice : voiceChecks.keySet()) {
                if (parser.getCurrentCycle().hasVoice(checkVoice) != voiceChecks.get(checkVoice)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Runs the appropriate checks for any modifiers on a given line
     * @param modifiers the modifiers to run checks for
     * @return true if all modifier checks pass and the line should be run; false otherwise
     */
    private boolean runModifierChecks(String[] modifiers) {
        return this.runModifierChecks(modifiers, null);
    }

    /**
     * Prints out a given number of line breaks
     * @param argument the number of line breaks to print (or an empty string, resulting in 1 line break)
     */
    private void lineBreak(String argument) {
        try {
            int nBreaks = Integer.parseInt(argument);
            this.lineBreak(nBreaks);
        } catch (NumberFormatException e) {
            if (argument.equals("")) {
                System.out.println();
            } else {
                // Invalid line; print error message and skip to next line
                System.out.println("[DEBUG: Invalid linebreak in file " + source.getName() + " at line " + (this.cursor + 1) + "]");
            }
        }
    }

    /**
     * Prints out a given number of line breaks
     * @param n the number of line breaks to print
     */
    private void lineBreak(int n) {
        for (int i = 0; i < n; i++) {
            System.out.println();
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
     * Moves the cursor of this script to a given label
     * @param label the label to move to
     */
    public void jumpTo(String label) {
        try {
            // If label is "NOJUMP", this was probably triggered from a switchjump -- just continue, don't jump
            if (!label.equals("NOJUMP")) this.jumpTo(this.getLabelIndex(label));
        } catch (IllegalArgumentException e) {
            System.out.println("[DEBUG: Label " + label + " does not exist in " + source.getName() + "]");
        }
        
    }

    /**
     * Jumps to one of two labels, depending on whether or not the player has already claimed at least one vessel
     * @param labelPrefix the prefix of the label to jump to
     */
    private void firstSwitchJump(String labelPrefix) {
        Cycle currentCycle = manager.getCurrentCycle();
        boolean firstVessel = (currentCycle == null) ? false : currentCycle.isFirstVessel();
        String labelSuffix = (firstVessel) ? "FirstVessel" : "NotFirstVessel";

        this.jumpTo(labelPrefix + labelSuffix);
    }

    /**
     * Jumps to one of two labels, depending on whether the player has the blade or not
     * @param labelPrefix the prefix of the label to jump to
     */
    private void bladeSwitchJump(String labelPrefix) {
        Cycle currentCycle = manager.getCurrentCycle();
        boolean hasBlade = (currentCycle == null) ? false : currentCycle.hasBlade();
        String labelSuffix = (hasBlade) ? "Blade" : "NoBlade";

        this.jumpTo(labelPrefix + labelSuffix);
    }

    /**
     * Jumps to one of two labels, depending on whether the player has the blade or not
     * @param labelPrefix the prefix of the label to jump to
     * @param labelSuffix the suffix of the label to jump to
     */
    private void bladeSwitchJump(String labelPrefix, String labelSuffix) {
        Cycle currentCycle = manager.getCurrentCycle();
        boolean hasBlade = (currentCycle == null) ? false : currentCycle.hasBlade();
        String labelBlade = (hasBlade) ? "Blade" : "NoBlade";

        this.jumpTo(labelPrefix + labelBlade + labelSuffix);
    }

    /**
     * Jumps to one of several labels, depending on the "source" of the active chapter
     * @param labelSuffix the suffix of the label to jump to
     */
    private void sourceSwitchJump(String labelSuffix) {
        Cycle currentCycle = manager.getCurrentCycle();
        String source = (currentCycle == null) ? "" : currentCycle.getSource();
        this.jumpTo(source + labelSuffix);
    }

    /**
     * Moves the cursor to one of two labels depending on a given boolean
     * @param arguments the possible labels to move to
     */
    public void boolSwitchJumpTo(String arguments) {
        String[] jumpLabels = arguments.split(" ");

        switch (jumpLabels.length) {
            case 0:
                // Invalid line; print error message and skip to next line
                System.out.println("[DEBUG: Invalid switchjump in file " + source.getName() + " at line " + (this.cursor + 1) + "]");
                break;

            case 1:
                if (this.boolCondition) this.jumpTo(jumpLabels[0]);
                break;

            default: // Any labels past the second will be ignored!
                if (this.boolCondition) this.jumpTo(jumpLabels[0]);
                else this.jumpTo(jumpLabels[1]);
        }
    }

    /**
     * Moves the cursor to one of several labels depending on a given int
     * @param arguments the possible labels to move to
     * @param auto whether to automatically determine the label to jump to using a given prefix
     */
    public void numSwitchJumpTo(String arguments, boolean auto) {
        if (auto) {
            if (this.hasLabel(arguments + this.intCondition)) this.jumpTo(arguments + this.intCondition);
            // else: default - continue without jumping
        } else {
            String[] jumpLabels = arguments.split(" ");

            if (jumpLabels.length > 0) {
                try {
                    this.jumpTo(jumpLabels[this.intCondition]);
                } catch (IndexOutOfBoundsException e) {
                    // Default: continue without jumping
                }
            } else {
                // Invalid line; print error message and skip to next line
                System.out.println("[DEBUG: Invalid numswitchjump in file " + source.getName() + " at line " + (this.cursor + 1) + "]");
            }
        }
    }

    /**
     * Moves the cursor to one of several labels depending on a given String
     * @param arguments the possible labels to move to
     * @param auto whether to automatically determine the label to jump to using a given suffix
     */
    public void strSwitchJumpTo(String arguments, boolean auto) {
        if (auto) {
            if (this.hasLabel(this.strCondition + arguments)) this.jumpTo(this.strCondition + arguments);
            // else: default - continue without jumping
        } else {
            String[] args = arguments.split(" ");
            int nArgs = args.length;

            if (nArgs >= 2 && nArgs % 2 == 0) { // Must have an even number of arguments
                for (int i = 0; i < nArgs; i += 2) {
                    if (strCondition.equals(args[i])) this.jumpTo(args[i+1]);
                }
            } else {
                // Invalid line; print error message and skip to next line
                System.out.println("[DEBUG: Invalid strswitchjump in file " + source.getName() + " at line " + (this.cursor + 1) + "]");
            }
        }
    }

    /**
     * Prints a line about the Long Quiet beginning to creep closer, used in most endings right before a vessel is claimed
     */
    private void quietCreep() {
        Cycle currentCycle = parser.getCurrentCycle();
        if (currentCycle != null) currentCycle.quietCreep();
    }

    /**
     * Prints out the short sequence of the Shifting Mound taking the current vessel away
     * @param skipFirstLineBreak whether to skip the first line break of the sequence
     */
    public void claimFoldLine(boolean skipFirstLineBreak) {
        if (!skipFirstLineBreak) System.out.println();
        parser.printDialogueLine(CLAIMFOLD);
        System.out.println();
    }

    /**
     * Prints out the short sequence of the Shifting Mound taking the current vessel away
     */
    public void claimFoldLine() {
        this.claimFoldLine(false);
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
        String modifiers = "";

        if (!argument.isEmpty()) {
            split = argument.split(" /// ");
            if (split.length != 1) {
                argument = split[0];
                modifiers = split[1];
            }
        }

        this.printDialogueLine(prefix, argument, modifiers);
    }

    /**
     * Prints out the dialogue line specified by a given character identifier and line
     * @param characterID the ID of the character speaking the line
     * @param arguments the dialogue line itself, as well as any optional modifiers
     * @param modifiers any modifiers to apply to the line
     */
    private void printDialogueLine(String characterID, String line, String modifiers) {
        Voice v = Voice.getVoice(characterID);
        boolean isInterrupted = false;

        if (!modifiers.isEmpty()) {
            String[] mods = modifiers.split(" ");
            if (!this.runModifierChecks(mods, v)) return;

            for (String m : mods) {
                if (m.equals("interrupt")) {
                    isInterrupted = true;
                }
            }
        }

        if (v == null) {
            if (characterID.equals("t") || characterID.equals("truth")) {
                parser.printDialogueLine(line, isInterrupted);
            } else if (characterID.equals("p") || characterID.equals("princess")) {
                parser.printDialogueLine(new PrincessDialogueLine(line, isInterrupted));
            } else {
                // Invalid character; print error message and skip to next line
                System.out.println("[DEBUG: Invalid character ID in file " + source.getName() + " at line " + (this.cursor + 1) + "]");
            }
        } else {
            parser.printDialogueLine(new VoiceDialogueLine(v, line, isInterrupted));
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

    
    public static void main(String[] args) {
        GameManager manager = new GameManager();
        IOHandler parser = new IOHandler(manager);
        
        File testFile = new File("Scripts", "TestScript.txt");
        System.out.println(testFile.getPath());
        
        Script script = new Script(manager, parser, "TestScript");
        for (String a : script.labels.keySet()) {
            System.out.println(a + ", " + script.labels.get(a));
        }

        manager.toggleAutoAdvance();
        script.runConditionalSection("strConditionTest", "revival");
        script.runConditionalSection("strConditionTest", "mutual");
        script.runConditionalSection("strConditionTest", "normal");
        script.runConditionalSection("strConditionTest", "other");
    }
    

}

/*
--- SCRIPT SYNTAX GUIDE ---

// This is a comment, and will be ignored during execution. //
Trailing spaces and indentation will also be ignored during execution.

Indentation is usually used to indicate conditional lines, e.g. dialogue that only triggers if you have certain Voices.

Including " /// " at the end of the line allows you to toggle additional modifiers for all lines except for comments and labels:
    regular line /// modA modB ...
Multiple modifiers  can be used together, separated by spaces.
All functions share the same modifiers, except for dialogue lines, which also have several unique modifiers.

Different functions a script can perform:
  - linebreak
  - linebreak [n]
        Prints out a line break. Can print out multiple line breaks at once if you specify a number, such as "linebreak 2".

  - label [id]
        Essentially acts as an anchor the script can move its cursor to at any time.

  - jumpto [n]
  - jumpto [label]
        Moves the cursor to a given line index or label.

  - firstswitch [prefix]
  - claim [prefix]
        Most often used while claiming a vessel at the end of a StandardCycle. Runs the section of the script at the label starting with the given prefix and ending with either "FirstVessel" or "NotFirstVessel", depending on whether the player has already claimed at least one vessel.

  - bladeswitch [prefix]
        Runs the section of the script at the label starting with the given prefix and ending with either "Blade" or "NoBlade", depending on whether the player currently has the blade.

  - sourceswitch [suffix]
        Runs the section of the script at the label starting with the "source" of the current chapter and ending with the given suffix.

  - switchjump [true label] [false label]
        Moves the cursor to the first given label if the boolean condition given in runConditionalSection() is true, or to the second given label if the condition is false (or no condition was given).

  - numswitchjump [label 0] [label 1] [...]
        Moves the cursor to the nth given label, where n is the int condition given in runConditionalSection(); if there are fewer than n labels or no int condition was given, continues without jumping.
  - numautojump [prefix]
        Moves the cursor to the label "prefixN", where N is the int condition given in runConditionalSection(); if there is no such label or no int condition was given, continues without jumping.

  - stringswitchjump [String 1] [label 1] [String 2] [label 2] [...]
        If the String condition given in runConditionalSection() matches one of the given Strings, jumps to the corresponding label; if the value of the String condition is not present or no String condition was given, continues without jumping.
  - stringautojump [suffix]
        Moves the cursor to the label "conditionSuffix", where condition is the String condition given in runConditionalSection(); if there is no such label or no String condition was given, continues without jumping.

  - nowplaying [song]
        Sets the song currently playing.

  - quietcreep
        Triggers StandardCycle.quietCreep(). Used in most endings right before a vessel is claimed.

  - [character] Dialogue line goes here
  - [character] Dialogue line goes here /// [modifiers]
        Modifiers are optional. Multiple modifiers can be used together.
        The first word specifies the ID of the speaking character, then anything after that is considered the actual dialogue line.
        Including " /// " at the end of the line allows you to toggle additional modifiers for this dialogue line.

        Unique modifiers:
          - checkvoice
                Checks whether the player has the speaker's voice before printing.
          - interrupt
                The line is interrupted.

Generic modifiers available for all lines (except comments and labels):
  - checkvoice-[id]
        Checks whether the player has the voice specified by the ID before running the line.
        (Multiple voices can be specified, as long as they are separated with hyphens.)
  - checknovoice-[id]
        Checks whether the player does NOT have the voice specified by the ID before running the line.
        (Multiple voices can be specified, as long as they are separated with hyphens.)
  
  - firstvessel
        Checks whether the player has not yet claimed any vessels before running the line.
  - notfirstvessel
        Checks whether the player has already claimed at least one vessel before running the line.

  - hasblade
        Checks whether the player currently has the blade before running the line.
  - noblade
        Checks whether the player currently does not have the blade before running the line.
        
  - ifsource-[value]
        Checks whether the "source" of the active chapter is equal to the given value before running the line.
  - ifsourcenot-[value]
        Checks whether the "source" of the active chapter is not equal to the given value before running the line.
  
  - check
        Checks the boolean condition given in runConditionalSection() before running the line.
  - checkfalse
        Checks whether the boolean condition given in runConditionalSection() is false before running the line.

  - ifnum
  - ifnum-[value]
        Checks whether the int condition given in runConditionalSection() is equal to the given value before running the line.
        (If no argument is given, the target value defaults to 0.)
  - ifnumnot
  - ifnumnot-[value]
        Checks whether the int condition given in runConditionalSection() is not equal to the given value before running the line.
        (If no argument is given, the target value defaults to 0.)

  - ifstring-[value]
        Checks whether the String condition given in runConditionalSection() is equal to the given value before running the line.
  - ifstringnot-[value]
        Checks whether the String condition given in runConditionalSection() is not equal to the given value before running the line.
*/