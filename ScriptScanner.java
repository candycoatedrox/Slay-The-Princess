import java.io.File;
import java.util.ArrayList;

public class ScriptScanner extends Script {

    private ArrayList<ScriptError> errorsFound;
    private ArrayList<ScriptIssue> issuesFound;
    
    /**
     * Constructor
     * @param manager the GameManager to link this Script to
     * @param parser the IOHandler to link this Script to
     * @param source the file containing the text of this Script
     */
    public ScriptScanner(GameManager manager, IOHandler parser, File source) {
        super(manager, parser, source);

        this.errorsFound = new ArrayList<>();
        this.issuesFound = new ArrayList<>();

        for (int i = 0; i < this.nLines(); i++) {
            this.scanLine(i);
        }
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Script to
     * @param parser the IOHandler to link this Script to
     * @param fileDirectory the file path of the file containing the text of this Script
     */
    public ScriptScanner(GameManager manager, IOHandler parser, String fileDirectory) {
        this(manager, parser, getFromDirectory(fileDirectory));
    }

    /**
     * Scans a single line of this script for errors and potential issues
     * @param lineIndex the index of the scanned line
     */
    private void scanLine(int lineIndex) {
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

                if (split.length > 2) errorsFound.add(new ScriptError(lineIndex, 6, 0));
                if (mods.length == 0) errorsFound.add(new ScriptError(lineIndex, 6, 1));
            }
        } catch (IndexOutOfBoundsException e) {
            args = new String[0];
            mods = new String[0];
        }

        boolean hasModifiers = mods.length != 0;
        boolean isDialogue = false;
        int nArgs = args.length;
        ArrayList<String> extraInfo = new ArrayList<>();

        switch (prefix) {
            case "//":
            case "":
                if (hasModifiers) issuesFound.add(new ScriptIssue(lineIndex, 2, 1));
                break;

            case "label":
                if (hasModifiers) issuesFound.add(new ScriptIssue(lineIndex, 2, 0));
                break;

            case "linebreak":
                try {
                    Integer.parseInt(argument);
                } catch (NumberFormatException e) {
                    if (!argument.equals("")) errorsFound.add(new ScriptError(lineIndex, 1));
                }

                break;

            case "jumpto":
                try {
                    int jumpTarget = Integer.parseInt(argument);
                    if (jumpTarget >= this.nLines()) errorsFound.add(new ScriptError(lineIndex, 2, 0));
                } catch (NumberFormatException e) {
                    if (!argument.equals("NOJUMP") && !this.hasLabel(argument)) errorsFound.add(new ScriptError(lineIndex, 2, 1, argument));
                }

                break;

            case "firstswitch":
            case "claim":
                if (args.length == 0) {
                    errorsFound.add(new ScriptError(lineIndex, 3, 0));
                } else {
                    if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 3, 1));
                    if (!this.hasLabel(argument + "FirstVessel")) extraInfo.add(argument + "FirstVessel");
                    if (!this.hasLabel(argument + "NotFirstVessel")) extraInfo.add(argument + "NotFirstVessel");
                    if (!extraInfo.isEmpty()) errorsFound.add(new ScriptError(lineIndex, 3, 2, extraInfo));
                }

                break;

            case "bladeswitch":
                switch (args.length) {
                    case 0:
                        errorsFound.add(new ScriptError(lineIndex, 3, 3));
                        break;

                    case 1:
                        if (!this.hasLabel(argument + "Blade")) extraInfo.add(argument + "Blade");
                        if (!this.hasLabel(argument + "NoBlade")) extraInfo.add(argument + "NoBlade");
                        break;

                    case 2:
                        if (!this.hasLabel(args[0] + "Blade" + args[1])) extraInfo.add(args[0] + "Blade" + args[1]);
                        if (!this.hasLabel(args[0] + "NoBlade" + args[1])) extraInfo.add(args[0] + "NoBlade" + args[1]);
                    
                    default:
                        if (!this.hasLabel(args[0] + "Blade" + args[1])) extraInfo.add(args[0] + "Blade" + args[1]);
                        if (!this.hasLabel(args[0] + "NoBlade" + args[1])) extraInfo.add(args[0] + "NoBlade" + args[1]);
                        errorsFound.add(new ScriptError(lineIndex, 3, 4));
                }

                
                if (!extraInfo.isEmpty()) errorsFound.add(new ScriptError(lineIndex, 3, 5, extraInfo));
                break;

            case "sourceswitch":
                if (args.length == 0) {
                    errorsFound.add(new ScriptError(lineIndex, 3, 6));
                } else {
                    if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 3, 7));

                    boolean labelFound = false;
                    for (String label : labels.keySet()) {
                        if (label.endsWith(args[0])) {
                            labelFound = true;
                            break;
                        }
                    }

                    if (!labelFound) errorsFound.add(new ScriptError(lineIndex, 3, 8, args[0]));
                }

                break;
            
            case "switchjump":
                if (nArgs == 0) {
                    errorsFound.add(new ScriptError(lineIndex, 4, 0));
                    break;
                } else if (nArgs == 1) {
                    issuesFound.add(new ScriptIssue(lineIndex, 0, 0));
                    if (!this.hasLabel(argument)) extraInfo.add(argument);
                } else {
                    if (nArgs > 2) issuesFound.add(new ScriptIssue(lineIndex, 0, 1));
                    if (!this.hasLabel(args[0])) extraInfo.add(args[0]);
                    if (!this.hasLabel(args[1])) extraInfo.add(args[1]);
                }

                if (!extraInfo.isEmpty()) errorsFound.add(new ScriptError(lineIndex, 4, 1, extraInfo));
                break;
            
            case "numswitchjump":
                if (argument.isEmpty()) {
                    errorsFound.add(new ScriptError(lineIndex, 4, 2));
                } else {
                    for (String label : args) {
                        if (!this.hasLabel(label)) extraInfo.add(label);
                    }
                }

                if (!extraInfo.isEmpty()) errorsFound.add(new ScriptError(lineIndex, 4, 3, extraInfo));
                break;
            
            case "stringswitchjump":
                if (nArgs == 0) {
                    errorsFound.add(new ScriptError(lineIndex, 4, 4));
                    break;
                } else if (nArgs == 1) {
                    errorsFound.add(new ScriptError(lineIndex, 4, 5));
                    break;
                } else if (nArgs % 2 != 0) {
                    errorsFound.add(new ScriptError(lineIndex, 4, 6));
                }

                for (int i = 1; i < nArgs; i += 2) {
                    if (!this.hasLabel(args[i])) extraInfo.add(args[i]);
                }

                if (!extraInfo.isEmpty()) errorsFound.add(new ScriptError(lineIndex, 4, 7, extraInfo));
                break;

            case "numautojump":
                if (argument.isEmpty()) {
                    errorsFound.add(new ScriptError(lineIndex, 4, 8));
                } else {
                    boolean labelExists = false;
                    for (int i = 0; i < 10; i++) {
                        if (this.hasLabel(argument + i)) {
                            labelExists = true;
                            break;
                        }
                    }
                    if (!labelExists) issuesFound.add(new ScriptIssue(lineIndex, 0, 2));
                }
                
                break;

            case "stringautojump":
                if (argument.isEmpty()) errorsFound.add(new ScriptError(lineIndex, 4, 9));
                break;

            case "nowplaying":
                if (argument.isEmpty()) errorsFound.add(new ScriptError(lineIndex, 5));
                break;

            case "quietcreep":
                if (!argument.isEmpty()) issuesFound.add(new ScriptIssue(lineIndex, 1, 0));
                break;
            case "claimfold":
                if (!argument.isEmpty()) issuesFound.add(new ScriptIssue(lineIndex, 1, 1));
                break;

            default:
                if (isValidCharacter(prefix)) {
                    isDialogue = true;
                } else {
                    errorsFound.add(new ScriptError(lineIndex, 0, 0));
                }
        }
            
        if (hasModifiers) this.scanModifiers(lineIndex, prefix, mods, isDialogue);
    }

    /**
     * Scans the modifiers on a single line of this script for errors and potential issues
     * @param lineIndex the index of the scanned line
     * @param prefix the prefix of this line
     * @param modifiers the modifiers to scan
     * @param isDialogue whether or not this line is a dialogue line
     */
    private void scanModifiers(int lineIndex, String prefix, String[] modifiers, boolean isDialogue) {
        Voice speaker = Voice.getVoice(prefix);
        Voice currentVoice;

        String[] args;
        int targetInt;

        ArrayList<String> presentMods = new ArrayList<>();
        ArrayList<String> duplicateMods = new ArrayList<>();
        
        ArrayList<String> invalidMods = new ArrayList<>();
        ArrayList<String> invalidVoiceArgs = new ArrayList<>();

        ArrayList<Voice> posVoiceChecks = new ArrayList<>();
        ArrayList<Voice> negVoiceChecks = new ArrayList<>();

        ArrayList<String> posTargetSources = new ArrayList<>();
        ArrayList<String> negTargetSources = new ArrayList<>();
        ArrayList<Integer> posTargetInts = new ArrayList<>();
        ArrayList<Integer> negTargetInts = new ArrayList<>();
        ArrayList<String> posTargetStrings = new ArrayList<>();
        ArrayList<String> negTargetStrings = new ArrayList<>();

        for (String m : modifiers) {
            if (m.isEmpty()) continue;
            args = m.split("-");

            if (m.startsWith("interrupt")) {
                if (!m.equals("interrupt")) errorsFound.add(new ScriptError(lineIndex, 6, 11));

                if (presentMods.contains("interrupt")) {
                    if (!duplicateMods.contains("interrupt")) duplicateMods.add("interrupt");
                } else {
                    presentMods.add("interrupt");
                    if (!isDialogue) errorsFound.add(new ScriptError(lineIndex, 6, 10));
                }
            } else if (m.startsWith("checkvoice")) {
                if (presentMods.contains("checkvoice")) {
                    if (!duplicateMods.contains("checkvoice")) duplicateMods.add("checkvoice");
                } else {
                    presentMods.add("checkvoice");
                }
                
                if (args.length == 1) {
                    if (m.equals("checkvoice")) {
                        if (!isDialogue) {
                            errorsFound.add(new ScriptError(lineIndex, 6, 3, "checkvoice"));
                        } else if (speaker == null) {
                            errorsFound.add(new ScriptError(lineIndex, 6, 4));
                        } else {
                            posVoiceChecks.add(speaker);
                        }
                    } else {
                        errorsFound.add(new ScriptError(lineIndex, 6, 3, "checkvoice"));
                    }
                } else {
                    for (int i = 1; i < args.length; i++) {
                        currentVoice = Voice.getVoice(args[i]);
                        if (currentVoice == null) {
                            invalidVoiceArgs.add(args[i]);
                        } else {
                            posVoiceChecks.add(currentVoice);
                        }
                    }
                }
            } else if (m.startsWith("checknovoice")) {
                if (presentMods.contains("checknovoice")) {
                    if (!duplicateMods.contains("checknovoice")) duplicateMods.add("checkvoice");
                } else {
                    presentMods.add("checknovoice");
                }

                if (args.length == 1) {
                    errorsFound.add(new ScriptError(lineIndex, 6, 3, "checknovoice"));
                } else {
                   for (int i = 1; i < args.length; i++) {
                        currentVoice = Voice.getVoice(args[i]);
                        if (currentVoice == null) {
                            invalidVoiceArgs.add(args[i]);
                        } else {
                            if (currentVoice == speaker) errorsFound.add(new ScriptError(lineIndex, 7, 14, speaker.getDialogueTag()));
                            negVoiceChecks.add(currentVoice);
                        }
                    }
                }
            } else if (m.equals("firstvessel")) {
                if (presentMods.contains("firstvessel")) {
                    if (!duplicateMods.contains("firstvessel")) duplicateMods.add("firstvessel");
                } else {
                    presentMods.add("firstvessel");
                    if (presentMods.contains("notfirstvessel")) errorsFound.add(new ScriptError(lineIndex, 7, 0));
                    if (prefix.equals("firstswitch") || prefix.equals("claim")) errorsFound.add(new ScriptError(lineIndex, 7, 1, "firstvessel"));
                }

                if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 6, 6, "firstvessel"));
            } else if (m.equals("notfirstvessel")) {
                if (presentMods.contains("notfirstvessel")) {
                    if (!duplicateMods.contains("notfirstvessel")) duplicateMods.add("notfirstvessel");
                } else {
                    presentMods.add("notfirstvessel");
                    if (presentMods.contains("firstvessel")) errorsFound.add(new ScriptError(lineIndex, 7, 0));
                    if (prefix.equals("firstswitch") || prefix.equals("claim")) errorsFound.add(new ScriptError(lineIndex, 7, 1, "notfirstvessel"));
                }

                if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 6, 6, "notfirstvessel"));
            } else if (m.equals("hasblade")) {
                if (presentMods.contains("hasblade")) {
                    if (!duplicateMods.contains("hasblade")) duplicateMods.add("hasblade");
                } else {
                    presentMods.add("hasblade");
                    if (presentMods.contains("noblade")) errorsFound.add(new ScriptError(lineIndex, 7, 2));
                    if (prefix.equals("bladeswitch")) errorsFound.add(new ScriptError(lineIndex, 7, 3, "hasblade"));
                }
                
                if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 6, 6, "hasblade"));
            } else if (m.equals("noblade")) {
                if (presentMods.contains("noblade")) {
                    if (!duplicateMods.contains("noblade")) duplicateMods.add("noblade");
                } else {
                    presentMods.add("noblade");
                    if (presentMods.contains("hasblade")) errorsFound.add(new ScriptError(lineIndex, 7, 2));
                    if (prefix.equals("bladeswitch")) errorsFound.add(new ScriptError(lineIndex, 7, 3, "noblade"));
                }
                
                if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 6, 6, "noblade"));
            } else if (m.startsWith("ifsource")) {
                if (presentMods.contains("ifsource")) {
                    if (!duplicateMods.contains("ifsource")) duplicateMods.add("ifsource");
                } else {
                    presentMods.add("ifsource");
                    if (prefix.equals("sourceswitch")) errorsFound.add(new ScriptError(lineIndex, 7, 5, "ifsource"));
                }
                
                switch (args.length) {
                    case 1:
                        errorsFound.add(new ScriptError(lineIndex, 6, 9, "ifsource"));
                        if (!m.equals("ifsource")) errorsFound.add(new ScriptError(lineIndex, 6, 3, "ifsource"));
                        break;

                    case 2:
                        if (!posTargetSources.contains(m)) posTargetSources.add(args[1]);
                        break;

                    default:
                        errorsFound.add(new ScriptError(lineIndex, 6, 7, "ifsourcenot"));
                }
            } else if (m.startsWith("ifsourcenot")) {
                if (presentMods.contains("ifsourcenot")) {
                    if (!duplicateMods.contains("ifsourcenot")) duplicateMods.add("ifsourcenot");
                } else {
                    presentMods.add("ifsourcenot");
                    if (prefix.equals("sourceswitch")) errorsFound.add(new ScriptError(lineIndex, 7, 5, "ifsourcenot"));
                }
                
                switch (args.length) {
                    case 1:
                        errorsFound.add(new ScriptError(lineIndex, 6, 9, "ifsourcenot"));
                        if (!m.equals("ifsourcenot")) errorsFound.add(new ScriptError(lineIndex, 6, 3, "ifsourcenot"));
                        break;

                    case 2:
                        if (!negTargetSources.contains(m)) negTargetSources.add(args[1]);
                        break;

                    default:
                        errorsFound.add(new ScriptError(lineIndex, 6, 7, "ifsourcenot"));
                }
            } else if (m.equals("check")) {
                if (presentMods.contains("check")) {
                    if (!duplicateMods.contains("check")) duplicateMods.add("check");
                } else {
                    presentMods.add("check");
                    if (presentMods.contains("checkfalse")) errorsFound.add(new ScriptError(lineIndex, 7, 6));
                    if (prefix.equals("switchjump")) errorsFound.add(new ScriptError(lineIndex, 7, 7, "check"));
                }
                
                if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 6, 6, "check"));
            } else if (m.equals("checkfalse")) {
                if (presentMods.contains("checkfalse")) {
                    if (!duplicateMods.contains("checkfalse")) duplicateMods.add("checkfalse");
                } else {
                    presentMods.add("checkfalse");
                    if (presentMods.contains("check")) errorsFound.add(new ScriptError(lineIndex, 7, 6));
                    if (prefix.equals("switchjump")) errorsFound.add(new ScriptError(lineIndex, 7, 7, "checkfalse"));
                }
                
                if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 6, 6, "checkfalse"));
            } else if (m.startsWith("ifnum")) {
                if (presentMods.contains("ifnum")) {
                    if (!duplicateMods.contains("ifnum")) duplicateMods.add("ifnum");
                } else {
                    presentMods.add("ifnum");
                    if (prefix.equals("numswitchjump")) errorsFound.add(new ScriptError(lineIndex, 7, 9, "ifnum"));
                }
                
                if (args.length == 2) {
                    try {
                        targetInt = Integer.parseInt(args[1]);
                        if (!posTargetInts.contains(targetInt)) posTargetInts.add(targetInt);
                    } catch (NumberFormatException e) {
                        errorsFound.add(new ScriptError(lineIndex, 6, 8, "ifnum"));
                    }
                } else if (args.length > 2) {
                    errorsFound.add(new ScriptError(lineIndex, 6, 7, "ifnum"));
                } else {
                    if (!m.equals("ifnum")) errorsFound.add(new ScriptError(lineIndex, 6, 3, "ifnum"));
                    if (!posTargetInts.contains(0)) posTargetInts.add(0);
                }
            } else if (m.startsWith("ifnumnot")) {
                if (presentMods.contains("ifnumnot")) {
                    if (!duplicateMods.contains("ifnumnot")) duplicateMods.add("ifnumnot");
                } else {
                    presentMods.add("ifnumnot");
                    if (prefix.equals("numswitchjump")) errorsFound.add(new ScriptError(lineIndex, 7, 9, "ifnumnot"));
                }
                
                if (args.length == 2) {
                    try {
                        targetInt = Integer.parseInt(args[1]);
                        if (!negTargetInts.contains(targetInt)) negTargetInts.add(targetInt);
                    } catch (NumberFormatException e) {
                        errorsFound.add(new ScriptError(lineIndex, 6, 8, "ifnumnot"));
                    }
                } else if (args.length > 2) {
                    errorsFound.add(new ScriptError(lineIndex, 6, 7, "ifnumnot"));
                } else {
                    if (!m.equals("ifnumnot")) errorsFound.add(new ScriptError(lineIndex, 6, 3, "ifnumnot"));
                    if (!negTargetInts.contains(0)) negTargetInts.add(0);
                }
            } else if (m.startsWith("ifstring")) {
                if (presentMods.contains("ifstring")) {
                    if (!duplicateMods.contains("ifstring")) duplicateMods.add("ifstring");
                } else {
                    presentMods.add("ifstring");
                    if (prefix.equals("stringswitchjump")) errorsFound.add(new ScriptError(lineIndex, 7, 11, "ifstring"));
                }
            
                switch (args.length) {
                    case 1:
                        errorsFound.add(new ScriptError(lineIndex, 6, 9, "ifstring"));
                        if (!m.equals("ifstring")) errorsFound.add(new ScriptError(lineIndex, 6, 3, "ifstring"));
                        break;

                    case 2:
                        if (!posTargetStrings.contains(m)) posTargetStrings.add(args[1]);
                        break;

                    default:
                        errorsFound.add(new ScriptError(lineIndex, 6, 7, "ifstring"));
                }
            } else if (m.startsWith("ifstringnot")) {
                if (presentMods.contains("ifstringnot")) {
                    if (!duplicateMods.contains("ifstringnot")) duplicateMods.add("ifstringnot");
                } else {
                    presentMods.add("ifstringnot");
                    if (prefix.equals("stringswitchjump")) errorsFound.add(new ScriptError(lineIndex, 7, 11, "ifstringnot"));
                }
                
                switch (args.length) {
                    case 1:
                        errorsFound.add(new ScriptError(lineIndex, 6, 9, "ifstringnot"));
                        if (!m.equals("ifstringnot")) errorsFound.add(new ScriptError(lineIndex, 6, 3, "ifstringnot"));
                        break;

                    case 2:
                        if (!negTargetStrings.contains(m)) negTargetStrings.add(args[1]);
                        break;

                    default:
                        errorsFound.add(new ScriptError(lineIndex, 6, 7, "ifstringnot"));
                }
            } else {
                invalidMods.add(m);
            }
        }

        if (!duplicateMods.isEmpty()) issuesFound.add(new ScriptIssue(lineIndex, 3, 0, duplicateMods));
        if (!invalidMods.isEmpty()) errorsFound.add(new ScriptError(lineIndex, 6, 2, invalidMods));
        if (!invalidVoiceArgs.isEmpty()) errorsFound.add(new ScriptError(lineIndex, 6, 5, invalidVoiceArgs));
        if (posTargetSources.size() > 1) errorsFound.add(new ScriptError(lineIndex, 7, 12, "ifsource"));
        if (posTargetInts.size() > 1) errorsFound.add(new ScriptError(lineIndex, 7, 12, "ifnum"));
        if (posTargetStrings.size() > 1) errorsFound.add(new ScriptError(lineIndex, 7, 12, "ifstring"));

        boolean redundantSource = false;
        boolean redundantNum = false;
        boolean redundantString = false;
        ArrayList<Voice> checkedVoices = new ArrayList<>();
        ArrayList<String> impossibleVoiceChecks = new ArrayList<>();
        ArrayList<String> impossibleSourceChecks = new ArrayList<>();
        ArrayList<String> impossibleNumChecks = new ArrayList<>();
        ArrayList<String> impossibleStringChecks = new ArrayList<>();
        ArrayList<String> duplicatePosVoices = new ArrayList<>();
        ArrayList<String> duplicateNegVoices = new ArrayList<>();

        for (Voice v : posVoiceChecks) {
            if (checkedVoices.contains(v)) {
                duplicatePosVoices.add(v.getDialogueTag());
            } else {
                checkedVoices.add(v);
            }
        }
        
        checkedVoices.clear();
        for (Voice v : negVoiceChecks) {
            if (checkedVoices.contains(v)) {
                duplicateNegVoices.add(v.getDialogueTag());
            } else {
                if (posVoiceChecks.contains(v)) impossibleVoiceChecks.add(v.getDialogueTag());
                checkedVoices.add(v);
            }
        }

        if (!posTargetSources.isEmpty() && !negTargetSources.isEmpty()) {
            for (String s : posTargetSources) {
                if (negTargetSources.contains(s)) {
                    impossibleSourceChecks.add(s);
                } else {
                    redundantSource = true;
                }
            }

            if (!redundantSource) {
                for (String s : negTargetSources) {
                    if (!posTargetSources.contains(s)) {
                        redundantSource = true;
                        break;
                    } 
                }
            }
        }

        if (!posTargetInts.isEmpty() && !negTargetInts.isEmpty()) {
            for (Integer i : posTargetInts) {
                if (negTargetInts.contains(i)) {
                    impossibleNumChecks.add(i.toString());
                } else {
                    redundantNum = true;
                }
            }

            if (!redundantNum) {
                for (Integer i : negTargetInts) {
                    if (!posTargetInts.contains(i)) {
                        redundantNum = true;
                        break;
                    } 
                }
            }
        }

        if (!posTargetStrings.isEmpty() && !negTargetStrings.isEmpty()) {
            for (String s : posTargetStrings) {
                if (negTargetStrings.contains(s)) {
                    impossibleStringChecks.add(s);
                } else {
                    redundantString = true;
                }
            }

            if (!redundantString) {
                for (String s : negTargetStrings) {
                    if (!posTargetStrings.contains(s)) {
                        redundantString = true;
                        break;
                    } 
                }
            }
        }

        if (!duplicatePosVoices.isEmpty()) issuesFound.add(new ScriptIssue(lineIndex, 3, 1, duplicatePosVoices));
        if (!duplicateNegVoices.isEmpty()) issuesFound.add(new ScriptIssue(lineIndex, 3, 1, duplicateNegVoices));
        if (!impossibleVoiceChecks.isEmpty()) errorsFound.add(new ScriptError(lineIndex, 7, 13, impossibleVoiceChecks));
        if (!impossibleSourceChecks.isEmpty()) errorsFound.add(new ScriptError(lineIndex, 7, 4, impossibleSourceChecks));
        if (!impossibleNumChecks.isEmpty()) errorsFound.add(new ScriptError(lineIndex, 7, 8, impossibleNumChecks));
        if (!impossibleStringChecks.isEmpty()) errorsFound.add(new ScriptError(lineIndex, 7, 10, impossibleStringChecks));
        if (redundantSource) issuesFound.add(new ScriptIssue(lineIndex, 4, 0));
        if (redundantNum) issuesFound.add(new ScriptIssue(lineIndex, 4, 1));
        if (redundantString) issuesFound.add(new ScriptIssue(lineIndex, 4, 2));
    }

    /**
     * Prints out a report of all errors and potential issues in the file
     */
    public void printReport() {
        System.out.println();
        IOHandler.wrapPrintln("--- SCAN RESULTS: " + source.getName() + " ---");
        System.out.println();

        if (errorsFound.isEmpty()) {
            if (issuesFound.isEmpty()) {
                IOHandler.wrapPrintln("No errors or potential issues found! " + source.getName() + " is perfecttly functional!");
                System.out.println();
                return;
            } else {
                IOHandler.wrapPrintln("No errors found!");
            }
        } else {
            System.out.println("- " + errorsFound.size() + " ERRORS FOUND -");
            for (ScriptError error : errorsFound) {
                IOHandler.wrapPrintln(error.toString());
            }
        }

        System.out.println();
        if (issuesFound.isEmpty()) {
            IOHandler.wrapPrintln("No potential issues found!");
        } else {
            IOHandler.wrapPrintln("- " + issuesFound.size() + " POTENTIAL ISSUES FOUND -");
            for (ScriptIssue issue : issuesFound) {
                IOHandler.wrapPrintln(issue.toString());
            }
        }

        System.out.println();
    }

    public static void main(String[] args) {
        GameManager manager = new GameManager();
        IOHandler parser = new IOHandler(manager);
        ScriptScanner scanner = new ScriptScanner(manager, parser, "Routes/JOINT/Grey/GreyShared");

        scanner.printReport();
    }

    // note to self: there is EITHER something weird and fucked up going on with the check for numswitchjump's labels being valid OR the way numswitchjump actually stores them, somehow

}
