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

    // The current cycle and related checks
    private Cycle currentCycle;
    private boolean noCycle;
    private boolean isChapter2;
    private boolean isChapter3;

    // Player-dependent flags used during all chapters
    private boolean firstVessel;
    private boolean hasBlade;
    private boolean isHarsh;
    private boolean knowsDestiny;

    // Gameplay-dependent flags used during Chapter 2 or 3
    private String ch2Voice = "";
    private String chapterSource = "";
    private boolean sharedLoop;
    private boolean sharedLoopInsist;
    private boolean mirrorComment;
    private boolean touchedMirror;
    private boolean mirrorKnown;
    private boolean threwBlade;
    private boolean freeFromChains2;
    private boolean adversaryTookBlade;

    // Gameplay-dependent flags used during Chapter 2 only
    private boolean narratorProof;
    private boolean droppedBlade1;
    private boolean whatWouldYouDo;
    private boolean rescuePath;

    // Gameplay-dependent flags used during Chapter 3 only
    private String ch3Voice = "";
    private boolean abandoned2;
    private boolean adversaryFaceExplore;
    private boolean spectreShareDied;
    private boolean spectrePossessAsk;
    private boolean spectreCantWontAsk;
    private boolean spectreEndSlay;
    private boolean prisonerForcedBlade;
    private boolean prisonerWatchedHead;
    private boolean prisonerGoodEndingSeen;
    private boolean prisonerHeartStopped;
    private Condition cageCutRoute = EMPTYCONDITION;
    private Condition happySmittenKnown = EMPTYCONDITION;
    private Condition happyGetUpAttempt = EMPTYCONDITION;

    // Given conditions for checks
    private boolean boolCondition = false;
    private int intCondition = 100;
    private String strCondition = "";

    private static final Condition EMPTYCONDITION = new Condition();
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
        try (Scanner fileReader = new Scanner(source);) {
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
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Script not found (FileNotFound)");
        } catch (NullPointerException e) {
            throw new RuntimeException("Script not found (NullPointer)");
        }

        this.initializeChapterFlags();
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Script to
     * @param parser the IOHandler to link this Script to
     * @param fileDirectory the file path of the file containing the text of this Script
     */
    public Script(GameManager manager, IOHandler parser, String fileDirectory) {
        this(manager, parser, getScriptFromDirectory(fileDirectory));
    }

    // --- ACCESSORS & CHECKS ---

    /**
     * Returns the name of the file linked to this Script
     * @return the name of the file linked to this Script
     */
    public String getFileName() {
        return source.getName();
    }

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

    // --- RUN SCRIPT ---

    /**
     * Executes this script from the cursor until the next break
     */
    public void runSection() {
        this.updateChapterFlags();
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
        this.updateChapterFlags();
        this.claimFoldLine(skipFirstLineBreak);
        this.firstSwitchJump(labelPrefix);
        this.runSection();
    }

    /**
     * Executes this script while claiming a vessel, redirecting to one of two sections depending on whether or not the player has already claimed at least one vessel
     * @param labelPrefix the prefix of the label to start executing at
     */
    public void runClaimSection(String labelPrefix) {
        this.updateChapterFlags();
        this.runClaimSection(labelPrefix, false);
    }

    /**
     * Executes one of two sections of this script, depending on whether the player has the blade or not
     * @param labelPrefix the prefix of the label to start executing at
     */
    public void runBladeSection(String labelPrefix) {
        this.updateChapterFlags();
        this.bladeSwitchJump(labelPrefix);
        this.runSection();
    }

    /**
     * Executes one of two sections of this script, depending on whether the player has the blade or not
     * @param labelPrefix the prefix of the label to start executing at
     * @param labelSuffix the suffix of the label to start executing at
     */
    public void runBladeSection(String labelPrefix, String labelSuffix) {
        this.updateChapterFlags();
        this.bladeSwitchJump(labelPrefix, labelSuffix);
        this.runSection();
    }

    /**
     * Executes one of two sections of this script, depending on whether the Princess is currently hostile or not
     * @param labelPrefix the prefix of the label to start executing at
     */
    public void runMoodSection(String labelPrefix) {
        this.updateChapterFlags();
        this.moodSwitchJump(labelPrefix);
        this.runSection();
    }

    /**
     * Executes one of two sections of this script, depending on the Voice gained at the start of Chapter 2
     * @param labelSuffix the suffix of the label to start executing at
     */
    public void runVoice2Section(String labelSuffix) {
        this.updateChapterFlags();
        this.voice2SwitchJump(labelSuffix);
        this.runSection();
    }

    /**
     * Executes one of two sections of this script, depending on the Voice gained at the start of Chapter 3
     * @param labelSuffix the suffix of the label to start executing at
     */
    public void runVoice3Section(String labelSuffix) {
        this.updateChapterFlags();
        this.voice3SwitchJump(labelSuffix);
        this.runSection();
    }

    /**
     * Executes one of two sections of this script, depending on whether the player has the blade or not
     * @param labelSuffix the suffix of the label to start executing at
     */
    public void runSourceSection(String labelSuffix) {
        this.updateChapterFlags();
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
        this.updateChapterFlags();
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

    /**
     * Resets all gameplay-dependent flags to their default state at the beginning of a cycle
     */
    private void resetChapterFlags() {
        this.firstVessel = false;
        this.hasBlade = false;
        this.mirrorComment = false;
        this.touchedMirror = false;
        this.mirrorKnown = false;
        this.isHarsh = false;
        this.knowsDestiny = false;

        this.ch2Voice = "";
        this.chapterSource = "";
        this.sharedLoop = false;
        this.sharedLoopInsist = false;
        this.threwBlade = false;
        this.adversaryTookBlade = false;
        this.freeFromChains2 = false;

        this.narratorProof = false;
        this.droppedBlade1 = false;
        this.whatWouldYouDo = false;
        this.rescuePath = false;
        
        this.ch3Voice = "";
        this.abandoned2 = false;
        this.adversaryFaceExplore = false;
        this.spectreShareDied = false;
        this.spectrePossessAsk = false;
        this.spectreCantWontAsk = false;
        this.spectreEndSlay = false;
        this.prisonerForcedBlade = false;
        this.prisonerWatchedHead = false;
        this.prisonerGoodEndingSeen = false;
        this.prisonerHeartStopped = false;
        this.cageCutRoute = EMPTYCONDITION;
        this.happySmittenKnown = EMPTYCONDITION;
        this.happyGetUpAttempt = EMPTYCONDITION;
    }

    /**
     * Sets all gameplay-dependent flags based on the state of the current Cycle at the beginning of a chapter
     */
    private void initializeChapterFlags() {
        this.currentCycle = manager.getCurrentCycle();
        this.noCycle = this.currentCycle == null;
        this.isChapter2 = this.currentCycle instanceof ChapterII;
        this.isChapter3 = this.currentCycle instanceof ChapterIII;

        if (this.noCycle) {
            this.cageCutRoute = EMPTYCONDITION;
            this.happySmittenKnown = EMPTYCONDITION;
            this.happyGetUpAttempt = EMPTYCONDITION;
        } else {
            this.firstVessel = currentCycle.isFirstVessel();
            this.hasBlade = currentCycle.hasBlade();
            this.mirrorComment = currentCycle.mirrorComment();
            this.touchedMirror = currentCycle.touchedMirror();
            this.threwBlade = currentCycle.threwBlade();
            this.isHarsh = currentCycle.isHarsh();
            this.knowsDestiny = currentCycle.knowsDestiny();

            if (this.isChapter2) {
                ChapterII chapter2 = (ChapterII)this.currentCycle;

                try {
                    this.ch2Voice = chapter2.ch2Voice().toString();
                } catch (NullPointerException e) {
                    this.ch2Voice = "";
                }
                
                this.chapterSource = chapter2.getSource();
                this.sharedLoop = chapter2.sharedLoop();
                this.sharedLoopInsist = chapter2.sharedLoopInsist();
                this.freeFromChains2 = chapter2.freeFromChains2();
                this.adversaryTookBlade = chapter2.adversaryTookBlade();

                this.droppedBlade1 = chapter2.droppedBlade1();
                this.whatWouldYouDo = chapter2.whatWouldYouDo();
                this.rescuePath = chapter2.rescuePath();
                
                this.cageCutRoute = EMPTYCONDITION;
                this.happySmittenKnown = EMPTYCONDITION;
                this.happyGetUpAttempt = EMPTYCONDITION;
            } else if (this.isChapter3) {
                ChapterIII chapter3 = (ChapterIII)this.currentCycle;

                try {
                    this.ch2Voice = chapter3.ch2Voice().toString();
                } catch (NullPointerException e) {
                    this.ch2Voice = "";
                }

                this.chapterSource = chapter3.getSource();
                this.sharedLoop = chapter3.sharedLoop();
                this.sharedLoopInsist = chapter3.sharedLoopInsist();
                this.freeFromChains2 = chapter3.freeFromChains2();
                this.adversaryTookBlade = chapter3.adversaryTookBlade();

                try {
                    this.ch3Voice = chapter3.ch3Voice().toString();
                } catch (NullPointerException e) {
                    this.ch3Voice = "";
                }

                this.abandoned2 = chapter3.abandoned2();
                this.adversaryFaceExplore = chapter3.adversaryFaceExplore();
                this.spectreShareDied = chapter3.spectreShareDied();
                this.spectrePossessAsk = chapter3.spectrePossessAsk();
                this.spectreCantWontAsk = chapter3.spectreCantWontAsk();
                this.spectreEndSlay = chapter3.spectreEndSlay();
                this.prisonerForcedBlade = chapter3.prisonerForcedBlade();
                this.prisonerWatchedHead = chapter3.prisonerWatchedHead();
                this.prisonerGoodEndingSeen = chapter3.prisonerGoodEndingSeen();
                this.prisonerHeartStopped = chapter3.prisonerHeartStopped();
                this.cageCutRoute = chapter3.cageCutRoute();
                this.happySmittenKnown = chapter3.happySmittenKnown();
                this.happyGetUpAttempt = chapter3.happyGetUpAttempt();
            } else {
                this.cageCutRoute = EMPTYCONDITION;
                this.happySmittenKnown = EMPTYCONDITION;
                this.happyGetUpAttempt = EMPTYCONDITION;
            }
        }

        this.mirrorKnown = this.mirrorComment || this.touchedMirror;
    }

    /**
     * Updates the source of the current Chapter
     */
    public void updateChapterSource() {
        this.currentCycle = manager.getCurrentCycle();
        this.noCycle = this.currentCycle == null;
        this.isChapter2 = this.currentCycle instanceof ChapterII;
        this.isChapter3 = this.currentCycle instanceof ChapterIII;

        if (this.isChapter2) {
            this.chapterSource = ((ChapterII)this.currentCycle).getSource();
        } else if (this.isChapter3) {
            this.chapterSource = ((ChapterIII)this.currentCycle).getSource();
        } else {
            this.chapterSource = "";
        }
    }

    /**
     * Updates all gameplay-dependent flags based on the state of the current Cycle
     */
    private void updateChapterFlags() {
        this.currentCycle = manager.getCurrentCycle();
        this.noCycle = this.currentCycle == null;
        this.isChapter2 = this.currentCycle instanceof ChapterII;

        if (this.currentCycle != null) {
            this.hasBlade = currentCycle.hasBlade();
            this.mirrorComment = currentCycle.mirrorComment();
            this.touchedMirror = currentCycle.touchedMirror();
            this.threwBlade = currentCycle.threwBlade();
            this.isHarsh = currentCycle.isHarsh();
            this.knowsDestiny = currentCycle.knowsDestiny();

            if (this.isChapter2) {
                ChapterII chapter2 = (ChapterII)this.currentCycle;
                
                this.sharedLoop = chapter2.sharedLoop();
                this.sharedLoopInsist = chapter2.sharedLoopInsist();
                this.narratorProof = chapter2.narratorProof();
                this.adversaryTookBlade = chapter2.adversaryTookBlade();
                this.freeFromChains2 = chapter2.freeFromChains2();
            }
        }

        this.mirrorKnown = this.mirrorComment || this.touchedMirror;
    }

    /**
     * Updates only the gameplay-dependent flags used for the mirror and the intermission, based on the state of the current Cycle
     */
    public void updateReusedScriptFlags() {
        this.resetChapterFlags();

        this.currentCycle = manager.getCurrentCycle();
        this.noCycle = this.currentCycle == null;

        if (this.currentCycle != null) {
            this.firstVessel = currentCycle.isFirstVessel();
            this.mirrorComment = currentCycle.mirrorComment();
            this.touchedMirror = currentCycle.touchedMirror();
        }

        this.mirrorKnown = this.mirrorComment || this.touchedMirror;
    }

    /**
     * Resets all conditional switches to their default values
     */
    private void resetConditions() {
        this.boolCondition = false;
        this.intCondition = 100;
        this.strCondition = "";
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
        String[] mods;
        try {
            argument = split[1];
            split = argument.split(" /// ");
            argument = split[0];
            args = argument.split(" ", 2);

            if (split.length == 1) {
                mods = new String[0];
            } else {
                String modifiers = split[1];
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

            case "break":
                if (!this.runModifierChecks(mods)) break;
            case "":
                cont = false;
                break;

            case "linebreak":
                if (this.runModifierChecks(mods)) this.lineBreak(argument);
                break;

            case "pause":
                if (this.runModifierChecks(mods)) this.pause(argument);
                break;

            case "unlock":
                if (this.runModifierChecks(mods)) this.unlockAchievement(argument);
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

            case "claim":
                if (args.length == 0) {
                    // Invalid line; print error message and skip to next line
                    System.out.println("[DEBUG: Invalid firstswitch in file " + source.getName() + " at line " + (this.cursor + 1) + "]");
                    break;
                }

                if (this.runModifierChecks(mods)) {
                    this.claimFoldLine();
                    this.firstSwitchJump(argument);
                }
                break;

            case "firstswitch":
                if (args.length == 0) {
                    // Invalid line; print error message and skip to next line
                    System.out.println("[DEBUG: Invalid firstswitch in file " + source.getName() + " at line " + (this.cursor + 1) + "]");
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

            case "moodswitch":
            case "harshswitch":
                if (args.length == 0) {
                    // Invalid line; print error message and skip to next line
                    System.out.println("[DEBUG: Invalid harshswitch in file " + source.getName() + " at line " + (this.cursor + 1) + "]");
                    break;
                }

                if (this.runModifierChecks(mods)) this.moodSwitchJump(argument);
                break;

            case "voice2switch":
                if (args.length == 0) {
                    // Invalid line; print error message and skip to next line
                    System.out.println("[DEBUG: Invalid voice2switch in file " + source.getName() + " at line " + (this.cursor + 1) + "]");
                    break;
                }

                if (this.runModifierChecks(mods)) this.voice2SwitchJump(argument);
                break;

            case "voice3switch":
                if (args.length == 0) {
                    // Invalid line; print error message and skip to next line
                    System.out.println("[DEBUG: Invalid voice3switch in file " + source.getName() + " at line " + (this.cursor + 1) + "]");
                    break;
                }

                if (this.runModifierChecks(mods)) this.voice3SwitchJump(argument);
                break;

            case "sourceswitch":
                if (args.length == 0) {
                    // Invalid line; print error message and skip to next line
                    System.out.println("[DEBUG: Invalid sourceswitch in file " + source.getName() + " at line " + (this.cursor + 1) + "]");
                    break;
                }

                if (this.runModifierChecks(mods)) this.sourceSwitchJump(argument);
                break;

            case "setbool":
                if (args.length == 0) {
                    // Invalid line; print error message and skip to next line
                    System.out.println("[DEBUG: Invalid setbool in file " + source.getName() + " at line " + (this.cursor + 1) + "]");
                    break;
                }

                if (this.runModifierChecks(mods)) this.setBoolCondition(argument);
                break;

            case "setnum":
                if (args.length == 0) {
                    // Invalid line; print error message and skip to next line
                    System.out.println("[DEBUG: Invalid setnum in file " + source.getName() + " at line " + (this.cursor + 1) + "]");
                    break;
                }

                if (this.runModifierChecks(mods)) this.setNumCondition(argument);
                break;

            case "setstring":
                if (args.length == 0) {
                    // Invalid line; print error message and skip to next line
                    System.out.println("[DEBUG: Invalid setstring in file " + source.getName() + " at line " + (this.cursor + 1) + "]");
                    break;
                }

                if (this.runModifierChecks(mods)) this.setStringCondition(argument);
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

            default:
                if (isValidCharacter(prefix)) {
                    this.printDialogueLine(prefix, argument, mods);
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

        HashMap<Voice, Boolean> voiceChecks = new HashMap<>();
        String[] args;
        int targetInt;

        for (String m : modifiers) {
            args = m.split("-");

            // Voice checks

            if (m.startsWith("checkvoice")) {
                if (args.length == 1) {
                    if (speaker != null) voiceChecks.put(speaker.checkVoice(), true);
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

            // Checks for any chapter

            } else if (m.equals("firstvessel")) {
                if (!this.firstVessel) return false;
            } else if (m.equals("notfirstvessel")) {
                if (this.firstVessel) return false;

            } else if (m.equals("hasblade")) {
                if (!this.hasBlade) return false;
            } else if (m.equals("noblade")) {
                if (this.hasBlade) return false;

            } else if (m.equals("harsh")) {
                if (!this.isHarsh) return false;
            } else if (m.equals("soft")) {
                if (this.isHarsh) return false;

            } else if (m.equals("knowledge")) {
                if (!this.knowsDestiny) return false;
            } else if (m.equals("noknowledge")) {
                if (this.knowsDestiny) return false;

            // Chapter 2 or 3 specific checks

            } else if (m.startsWith("voice2-")) {
                if ((this.isChapter2 || this.isChapter3) && args.length == 2) {
                    if (!ch2Voice.equals(args[1])) return false;
                } else {
                    return false;
                }
            } else if (m.startsWith("voice2not-")) {
                if ((this.isChapter2 || this.isChapter3) && args.length == 2) {
                    if (ch2Voice.equals(args[1])) return false;
                } else {
                    return false;
                }

            } else if (m.startsWith("ifsource-")) {
                if (!this.chapterSource.equals(args[1])) return false;
            } else if (m.startsWith("ifsourcenot-")) {
                if (this.chapterSource.equals(args[1])) return false;

            } else if (m.equals("sharedloop")) {
                if (!this.sharedLoop) return false;
            } else if (m.equals("noshare")) {
                if (this.sharedLoop) return false;

            } else if (m.equals("sharedinsist")) {
                if (!this.sharedLoopInsist) return false;
            } else if (m.equals("noinsist")) {
                if (this.sharedLoopInsist) return false;

            } else if (m.equals("mirrorask")) {
                if (!this.mirrorComment) return false;
            } else if (m.equals("nomirrorask")) {
                if (this.mirrorComment) return false;

            } else if (m.equals("mirrortouch")) {
                if (!this.touchedMirror) return false;
            } else if (m.equals("nomirrortouch")) {
                if (this.touchedMirror) return false;

            } else if (m.equals("mirror2")) {
                if (!this.mirrorKnown) return false;
            } else if (m.equals("nomirror2")) {
                if (this.mirrorKnown) return false;

            } else if (m.equals("threwblade")) {
                if (!this.threwBlade) return false;
            } else if (m.equals("nothrow")) {
                if (this.threwBlade) return false;

            } else if (m.equals("tookblade")) {
                if (!this.adversaryTookBlade) return false;
            } else if (m.equals("leftblade")) {
                if (this.adversaryTookBlade) return false;

            } else if (m.equals("chainsfree")) {
                if (!this.freeFromChains2) return false;
            } else if (m.equals("notfree")) {
                if (this.freeFromChains2) return false;

            // Chapter 2 variable checks

            } else if (m.equals("narrproof")) {
                if (!this.narratorProof) return false;
            } else if (m.equals("noproof")) {
                if (this.narratorProof) return false;

            } else if (m.equals("drop1")) {
                if (!this.droppedBlade1) return false;
            } else if (m.equals("nodrop1")) {
                if (this.droppedBlade1) return false;

            } else if (m.equals("whatdo1")) {
                if (!this.whatWouldYouDo) return false;
            } else if (m.equals("nowhatdo1")) {
                if (this.whatWouldYouDo) return false;

            } else if (m.equals("rescue1")) {
                if (!this.rescuePath) return false;
            } else if (m.equals("norescue1")) {
                if (this.rescuePath) return false;

            // Chapter 3 variable checks

            } else if (m.startsWith("voice3-")) {
                if (this.isChapter3 && args.length == 2) {
                    if (!ch3Voice.equals(args[1])) return false;
                } else {
                    return false;
                }
            } else if (m.startsWith("voice3not-")) {
                if (this.isChapter3 && args.length == 2) {
                    if (ch3Voice.equals(args[1])) return false;
                } else {
                    return false;
                }

            } else if (m.equals("abandoned")) {
                if (!this.abandoned2) return false;
            } else if (m.equals("noabandon")) {
                if (this.abandoned2) return false;

            } else if (m.equals("faceask")) {
                if (!this.adversaryFaceExplore) return false;
            } else if (m.equals("nofaceask")) {
                if (this.adversaryFaceExplore) return false;

            } else if (m.equals("deathshared")) {
                if (!this.spectreShareDied) return false;
            } else if (m.equals("nodeathshare")) {
                if (this.spectreShareDied) return false;

            } else if (m.equals("possessask")) {
                if (!this.spectrePossessAsk) return false;
            } else if (m.equals("nopossessask")) {
                if (this.spectrePossessAsk) return false;

            } else if (m.equals("cantwontask")) {
                if (!this.spectreCantWontAsk) return false;
            } else if (m.equals("nocantwontask")) {
                if (this.spectreCantWontAsk) return false;

            } else if (m.equals("endslay")) {
                if (!this.spectreEndSlay) return false;
            } else if (m.equals("noendslay")) {
                if (this.spectreEndSlay) return false;

            } else if (m.equals("forcedblade")) {
                if (!this.prisonerForcedBlade) return false;
            } else if (m.equals("noforce")) {
                if (this.prisonerForcedBlade) return false;

            } else if (m.equals("headwatch")) {
                if (!this.prisonerWatchedHead) return false;
            } else if (m.equals("nowatch")) {
                if (this.prisonerWatchedHead) return false;

            } else if (m.equals("goodseen")) {
                if (!this.prisonerGoodEndingSeen) return false;
            } else if (m.equals("goodnotseen")) {
                if (this.prisonerGoodEndingSeen) return false;

            } else if (m.equals("heartstop")) {
                if (!this.prisonerHeartStopped) return false;
            } else if (m.equals("noheartstop")) {
                if (this.prisonerHeartStopped) return false;

            } else if (m.equals("cutroute")) {
                if (!cageCutRoute.check()) return false;
            } else if (m.equals("nocut")) {
                if (cageCutRoute.check()) return false;

            } else if (m.equals("smittenknown")) {
                if (!happySmittenKnown.check()) return false;
            } else if (m.equals("nosmitten")) {
                if (happySmittenKnown.check()) return false;

            } else if (m.equals("getupattempt")) {
                if (!happyGetUpAttempt.check()) return false;
            } else if (m.equals("nogetup")) {
                if (happyGetUpAttempt.check()) return false;

            // Checks on given conditions
                
            } else if (m.equals("check")) {
                if (!this.boolCondition) return false;
            } else if (m.equals("checkfalse")) {
                if (this.boolCondition) return false;

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

                //IOHandler.wrapPrintln("[DEBUG: num = " + this.intCondition + "; target = " + targetInt + "; check should return " + (this.intCondition != targetInt) + "]");
                if (this.intCondition == targetInt) return false;
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

                //IOHandler.wrapPrintln("[DEBUG: num = " + this.intCondition + "; target = " + targetInt + "; check should return " + (this.intCondition == targetInt) + "]");
                if (this.intCondition != targetInt) return false;

            } else if (m.startsWith("ifstring-")) {
                if (!strCondition.equals(args[1])) return false;
            } else if (m.startsWith("ifstringnot-")) {
                if (strCondition.equals(args[1])) return false;
            }
        }

        if (!this.noCycle && !voiceChecks.isEmpty()) {
            for (Voice checkVoice : voiceChecks.keySet()) {
                if (currentCycle.hasVoice(checkVoice) != voiceChecks.get(checkVoice)) {
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
     * Waits for a given number of milliseconds before continuing, depending on whether global slow print is enabled or not
     * @param arguments the time to wait
     */
    private void pause(String arguments)  {
        if (arguments.isEmpty()) {
            // Invalid line; print error message and skip to next line
            System.out.println("[DEBUG: Invalid pause (no argument) in file " + source.getName() + " at line " + (this.cursor + 1) + "]");
        } else {
            String[] times = arguments.split(" ");
            int slowTime;
            int fastTime;

            try {
                slowTime = Integer.parseInt(times[0]);
                if (times.length == 1) {
                    GameManager.pause(slowTime);
                } else {
                    fastTime = Integer.parseInt(times[1]);
                    manager.pause(slowTime, fastTime);
                }
            } catch (NumberFormatException e) {
                // Invalid line; print error message and skip to next line
                System.out.println("[DEBUG: Invalid pause (non-int argument) in file " + source.getName() + " at line " + (this.cursor + 1) + "]");
            }
        }
    }

    /**
     * Unlocks the achievement with the given ID
     * @param argument the ID of the achievement to unlock
     */
    private void unlockAchievement(String argument) {
        if (argument.isEmpty() || argument.contains(" ")) {
            // Invalid line; print error message and skip to next line
            System.out.println("[DEBUG: Invalid unlock in file " + source.getName() + " at line " + (this.cursor + 1) + "]");
        } else {
            manager.unlock(argument);
        }
    }

    /**
     * Prints a line about the Long Quiet beginning to creep closer, used in most endings right before a vessel is claimed
     */
    private void quietCreep() {
        if (!this.noCycle) currentCycle.quietCreep();
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
        String labelSuffix = (firstVessel) ? "FirstVessel" : "NotFirstVessel";
        this.jumpTo(labelPrefix + labelSuffix);
    }

    /**
     * Jumps to one of two labels, depending on whether the player has the blade or not
     * @param labelPrefix the prefix of the label to jump to
     */
    private void bladeSwitchJump(String labelPrefix) {
        String labelSuffix = (hasBlade) ? "Blade" : "NoBlade";
        this.jumpTo(labelPrefix + labelSuffix);
    }

    /**
     * Jumps to one of two labels, depending on whether the player has the blade or not
     * @param labelPrefix the prefix of the label to jump to
     * @param labelSuffix the suffix of the label to jump to
     */
    private void bladeSwitchJump(String labelPrefix, String labelSuffix) {
        String labelBlade = (hasBlade) ? "Blade" : "NoBlade";
        this.jumpTo(labelPrefix + labelBlade + labelSuffix);
    }

    /**
     * Jumps to one of two labels, depending on whether the Princess is currently hostile or not
     * @param labelPrefix the prefix of the label to jump to
     */
    private void moodSwitchJump(String labelPrefix) {
        String labelSuffix = (isHarsh) ? "Harsh" : "Soft";
        this.jumpTo(labelPrefix + labelSuffix);
    }

    /**
     * Jumps to one of several labels, depending on the Voice gained at the start of Chapter 2
     * @param labelSuffix the suffix of the label to jump to
     */
    private void voice2SwitchJump(String labelSuffix) {
        if (this.ch2Voice != null) this.jumpTo(this.ch2Voice + labelSuffix);
    }

    /**
     * Jumps to one of several labels, depending on the Voice gained at the start of Chapter 3
     * @param labelSuffix the suffix of the label to jump to
     */
    private void voice3SwitchJump(String labelSuffix) {
        if (this.ch3Voice != null) this.jumpTo(this.ch3Voice + labelSuffix);
    }

    /**
     * Jumps to one of several labels, depending on the "source" of the active chapter
     * @param labelSuffix the suffix of the label to jump to
     */
    private void sourceSwitchJump(String labelSuffix) {
        this.jumpTo(this.chapterSource + labelSuffix);
    }

    /**
     * Sets the boolean condition of this Script to the given value
     * @param argument the argument given by the Script
     */
    private void setBoolCondition(String argument) {
        switch (argument) {
            case "true":
                this.boolCondition = true;
                break;
            case "false":
                this.boolCondition = false;
                break;
            default:
                System.out.println("[DEBUG: Invalid setbool argument " + argument + " at line " + this.cursor + " in " + source.getName() + "]");
        }
    }

    /**
     * Sets the int condition of this Script to the given value
     * @param argument the argument given by the Script
     */
    private void setNumCondition(String argument) {
        try {
            int newValue = Integer.parseInt(argument);
            this.intCondition = newValue;
        } catch (NumberFormatException e) {
            System.out.println("[DEBUG: Invalid setnum argument " + argument + " at line " + this.cursor + " in " + source.getName() + "]");
        }
    }

    /**
     * Sets the String condition of this Script to the given value
     * @param argument the argument given by the Script
     */
    private void setStringCondition(String argument) {
        this.strCondition = argument;
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
     * Prints out the dialogue line specified by a given line
     * @param lineIndex the index of the dialogue line to print
     */
    public void printDialogueLine(int lineIndex) {
        String lineContent = this.getLine(lineIndex).trim();
        String[] split = lineContent.split(" ", 2);

        String prefix = split[0];
        String argument = (split.length == 2) ? split[1] : "";
        String[] mods = new String[0];

        if (!argument.isEmpty()) {
            split = argument.split(" /// ");
            if (split.length != 1) {
                argument = split[0];
                String modifiers = split[1];
                mods = modifiers.split(" ");
            }
        }

        this.printDialogueLine(prefix, argument, mods);
    }

    /**
     * Prints out the dialogue line specified by a given character identifier and line
     * @param characterID the ID of the character speaking the line
     * @param arguments the dialogue line itself, as well as any optional modifiers
     * @param modifiers any modifiers to apply to the line
     */
    private void printDialogueLine(String characterID, String line, String[] modifiers) {
        Voice v = Voice.getVoice(characterID);
        boolean isInterrupted = false;
        double speedMultiplier = 1;

        boolean checkResult = this.runModifierChecks(modifiers, v);
        //System.out.println("[DEBUG: modifier checks returned " + checkResult + "]");
        if (!checkResult) return;
        //System.out.println("[DEBUG: checks passed, printing line]");

        for (String m : modifiers) {
            if (m.equals("interrupt")) {
                isInterrupted = true;
            } else if (m.equals("slow")) {
                speedMultiplier = 0.5;
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
            parser.printDialogueLine(v, line, isInterrupted, speedMultiplier);
        }        
    }

    // --- MISC ---

    /**
     * Retrieve a File from a given file path
     * @param directory the file path of the file containing the text of this Script
     * @return the File found at the given directory
     */
    public static File getScriptFromDirectory(String directory) {
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

        //manager.toggleAutoAdvance();
        //manager.toggleSlowPrint();
        script.runSection("heaTest");
    }

}

/*
--- SCRIPT SYNTAX GUIDE ---

// This is a comment, and will be ignored during execution. //
Trailing spaces and indentation will also be ignored during execution.

Indentation is usually used to indicate conditional lines, e.g. dialogue that only triggers if you have certain Voices.

Including " /// " at the end of the line allows you to toggle additional modifiers for all lines except for comments and labels:
    regular line /// modA modB ...
Multiple modifiers can be used together, separated by spaces.
All functions share the same modifiers: a variety of conditional checks that must pass before running the line. Dialogue lines also have several exclusive modifiers.

Different functions a script can perform:
  - linebreak
  - linebreak [n]
        Prints out a line break. Can print out multiple line breaks at once if you specify a number, such as "linebreak 2".

  - pause [time]
  - pause [slow time] [fast time]
        Waits for a given number of milliseconds before continuing. If two arguments are given, the first argument will be used if global slow print is enabled, and the second will be used if it is disabled.

  - break
        Tells the script to stop executing here for now. Should NEVER be used without modifiers.

  - unlock [id]
        Unlocks the achievement with the given ID.

  - nowplaying [song]
        Sets the song currently playing.

  - quietcreep
        Triggers StandardCycle.quietCreep(). Used in most endings right before a vessel is claimed.

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

  - voice2switch
        Runs the section of the script at the label starting with the ID of the Voice gained at the start of Chapter 2 and ending with the given suffix.

  - voice3switch
        Runs the section of the script at the label starting with the ID of the Voice gained at the start of Chapter 2 and ending with the given suffix.

  - sourceswitch [suffix]
        Runs the section of the script at the label starting with the "source" of the current chapter and ending with the given suffix.

  - moodswitch [prefix]
  - harshswitch [prefix]
        Runs the section of the script at the label starting with the given prefix and ending with either "Harsh" or "Soft", depending on whether the Princess is currently hostile.

  - setbool [true / false]
        Sets the boolean condition currently being used by the Script to the given value.
  - setnum [value]
        Sets the int condition currently being used by the Script to the given value.
  - setstring [value]
        Sets the String condition currently being used by the Script to the given value.

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

  - [character] Dialogue line goes here
  - [character] Dialogue line goes here /// [modifiers]
        The first word specifies the ID of the speaking character, then anything after that is considered the actual dialogue line.

        Exclusive modifiers:
          - checkvoice
                Checks whether the player has the speaker's voice before printing.
          - interrupt
                The line is interrupted.
          - slow
                Prints the line at half speed.

Generic modifiers available for all lines (except comments and labels):

    - Voice checks -
      - checkvoice-[id]
            Checks if the player has the voice specified by the ID before running the line.
            (Multiple voices can be specified, as long as they are separated with hyphens.)
      - checknovoice-[id]
            Checks if the player does NOT have the voice specified by the ID before running the line.
            (Multiple voices can be specified, as long as they are separated with hyphens.)
    
    - Checks that apply during any chapter -
      - firstvessel
            Checks if the player has not yet claimed any vessels before running the line.
      - notfirstvessel
            Checks if the player has already claimed at least one vessel before running the line.

      - hasblade
            Checks if the player currently has the blade before running the line.
      - noblade
            Checks if the player currently does not have the blade before running the line.

      - harsh
            Checks if the Princess is currently hostile before running the line.
      - soft
            Checks if the Princess is currently friendly before running the line.

      - knowledge
            Checks if the Princess knows she's (allegedly) going to end the world before running the line.
      - noknowledge
            Checks if the Princess does not know she's (allegedly) going to end the world before running the line.
            
    - Checks that apply during Chapter 2 or 3 -
      - voice2-[id]
            Checks if the Voice the player gained at the start of Chapter 2 is the Voice specified by the ID before running the line.
      - voice2not-[id]
            Checks if the Voice the player gained at the start of Chapter 2 is not the Voice specified by the ID before running the line.

      - ifsource-[value]
            Checks if the "source" of the active chapter is equal to the given value before running the line.
      - ifsourcenot-[value]
            Checks if the "source" of the active chapter is not equal to the given value before running the line.

      - sharedloop
            Checks if the Narrator knows that the player has been here already before running the line.
      - noshare
            Checks if the Narrator does not know that the player has been here already before running the line.

      - sharedinsist
            Checks if the player insisted that they've been here before in the woods before running the line.
      - noinsist
            Checks if the player did not insist that they've been here before in the woods before running the line.
            
      - mirrorask
            Checks if the player asked about the mirror in Chapter 2 before running the line.
      - nomirrorask
            Checks if the player asked about the mirror in Chapter 2 before running the line.
            
      - mirrortouch
            Checks if the player approached the mirror in Chapter 2 before running the line.
      - nomirrortouch
            Checks if the player approached the mirror in Chapter 2 before running the line.
            
      - mirror2
            Checks if the player interacted with the mirror in Chapter 2 before running the line.
      - nomirror2
            Checks if the player interacted with the mirror in Chapter 2 before running the line.

      - threwblade
            Checks if the player threw the blade out the window before running the line.
      - nothrow
            Checks if the player did not throw the blade out the window before running the line.

      - chainsfree
            Checks if the Princess freed herself from her chains in Chapter 2 before running the line.
      - notfree
            Checks if the Princess did not free herself from her chains in Chapter 2 before running the line.

      - tookblade
            Checks if the player took the blade before entering the basement in Chapter 2: The Adversary before running the line.
      - leftblade
            Checks if the player did not take the blade before entering the basement in Chapter 2: The Adversary before running the line.

    - Checks that apply during Chapter 2 only -
      - narrproof
            Checks if the Narrator has proof that you have been here before before running the line.
      - noproof
            Checks if the Narrator does not have proof that you have been here before before running the line.

      - drop1
            Checks if the player dropped the blade in Chapter 1 before running the line.
      - nodrop1
            Checks if the player did not drop the blade in Chapter 1 before running the line.
            
      - whatdo1
            Checks if the player asked the Princess what she would do if she left the cabin in Chapter 1 before running the line.
      - nowhatdo1
            Checks if the player did not ask the Princess what she would do if she left the cabin in Chapter 1 before running the line.
            
      - rescue1
            Checks if the player started to free the Princess in Chapter 1 before running the line.
      - norescue1
            Checks if the player did not start to free the Princess in Chapter 1 before running the line.

    - Checks that apply during Chapter 3 only -
      - voice3-[id]
            Checks if the Voice the player gained at the start of Chapter 3 is the Voice specified by the ID before running the line.
      - voice3not-[id]
            Checks if the Voice the player gained at the start of Chapter 3 is not the Voice specified by the ID before running the line.
            
      - abandoned
            Checks if the player tried to abandon the Spectre or the Nightmare before running the line.
      - noabandon
            Checks if the player did not try to abandon the Spectre or the Nightmare before running the line.

      - faceask
            Checks if the player asked about their missing face while fighting the Adversary unarmed before running the line.
      - nofaceask
            Checks if the player did not ask about their missing face while fighting the Adversary unarmed before running the line.
            
      - deathshared
            Checks if the the player told the Spectre that they died before running the line.
      - nodeathshare
            Checks if the player did not the Spectre that they died before running the line.
            
      - possessask
            Checks if the Spectre asked to possess the player before running the line.
      - nopossessask
            Checks if the Spectre did not ask to possess the player before running the line.
            
      - cantwontask
            Checks if the player asked the Spectre whether she "couldn't" or "wouldn't" possess them if they refused before running the line.
      - nocantwontask
            Checks if the player did not ask the Spectre whether she "couldn't" or "wouldn't" possess them if they refused the player before running the line.
            
      - endslay
            Checks if the player tried to take the Spectre down as she killed them before running the line.
      - noendslay
            Checks if the player did not try to take the Spectre down as she killed them before running the line.
            
      - forcedblade
            Checks if the Voice of the Skeptic forced the player to take the blade in Chapter 2 before running the line.
      - noforce
            Checks if the Voice of the Skeptic did not force the player to take the blade in Chapter 2 before running the line.

      - headwatch
            Checks if the player chose to watch the Prisoner decapitate herself before running the line.
      - nowatch
            Checks if the player did not choose to watch the Prisoner decapitate herself before running the line.

      - goodseen
            Checks if the player saw the Good Ending in Chapter 2 before running the line.
      - goodnotseen
            Checks if the player did not see the Good Ending in Chapter 2 before running the line.
            
      - heartstop
            Checks if the Voice of the Skeptic stopped the player's heart in Chapter 2 before running the line.
      - noheartstop
            Checks if the Voice of the Skeptic did not stop the player's heart in Chapter 2 before running the line.

      - cutroute
            Checks if the player is attempting to cut themselves out of their chains in The Cage before running the line.
      - nocut
            Checks if the player is not attempting to cut themselves out of their chains in The Cage before running the line.

      - smittenknown
            Checks if the Voice of the Skeptic has figured out the shadow's identity in Happily Ever After before running the line.
      - nosmitten
            Checks if the player has not figured out the shadow's identity in Happily Ever After before running the line.

      - getupattempt
            Checks if the player has attempted to get out of their seat in Happily Ever After before running the line.
      - nogetup
            Checks if the player has not attempted to get out of their seat in Happily Ever After before running the line.
    
    - Checks on a given condition -
      - check
            Checks the boolean condition given in runConditionalSection() before running the line.
      - checkfalse
            Checks if the boolean condition given in runConditionalSection() is false before running the line.

      - ifnum
      - ifnum-[value]
            Checks if the int condition given in runConditionalSection() is equal to the given value before running the line.
            (If no argument is given, the target value defaults to 0.)
      - ifnumnot
      - ifnumnot-[value]
            Checks if the int condition given in runConditionalSection() is not equal to the given value before running the line.
            (If no argument is given, the target value defaults to 0.)

      - ifstring-[value]
            Checks if the String condition given in runConditionalSection() is equal to the given value before running the line.
      - ifstringnot-[value]
            Checks if the String condition given in runConditionalSection() is not equal to the given value before running the line.
*/