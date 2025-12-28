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
        this(manager, parser, getScriptFromDirectory(fileDirectory));
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

                if (split.length > 2) errorsFound.add(new ScriptError(lineIndex, 7, 0));
                if (mods.length == 0) errorsFound.add(new ScriptError(lineIndex, 7, 1));
            }
        } catch (IndexOutOfBoundsException e) {
            args = new String[0];
            mods = new String[0];
        }

        int nArgs = args.length;
        boolean hasModifiers = mods.length != 0;
        boolean isDialogue = false;
        int intArg;
        ArrayList<String> extraInfo = new ArrayList<>();

        switch (prefix) {
            case "//":
            case "":
                if (hasModifiers) issuesFound.add(new ScriptIssue(lineIndex, 2, 1));
                break;

            case "break":
                if (!argument.isEmpty()) errorsFound.add(new ScriptError(lineIndex, 2, 0));
                if (!hasModifiers) errorsFound.add(new ScriptError(lineIndex, 2, 1));
                break;

            case "label":
                if (hasModifiers) issuesFound.add(new ScriptIssue(lineIndex, 2, 0));
                break;

            case "linebreak":
                try {
                    intArg = Integer.parseInt(argument);
                    if (intArg < 0) errorsFound.add(new ScriptError(lineIndex, 1, 1, "linebreak"));
                } catch (NumberFormatException e) {
                    if (!argument.equals("")) errorsFound.add(new ScriptError(lineIndex, 1, 0, "linebreak"));
                }

                break;

            case "pause":
                if (nArgs == 0) {
                    errorsFound.add(new ScriptError(lineIndex, 1, 2));
                    break;
                } else if (nArgs > 2) {
                    errorsFound.add(new ScriptError(lineIndex, 1, 3));
                }
                
                boolean negativeArgs = false;
                try {
                    for (String arg : args) {
                        intArg = Integer.parseInt(arg);
                        if (intArg < 0) {
                            negativeArgs = true;
                        } else if (intArg > 5000) {
                            issuesFound.add(new ScriptIssue(lineIndex, 6, arg));
                        }
                    }
                } catch (NumberFormatException e) {
                    errorsFound.add(new ScriptError(lineIndex, 1, 0, "pause"));
                }

                if (negativeArgs) errorsFound.add(new ScriptError(lineIndex, 1, 1, "pause"));
                break;

            case "jumpto":
                try {
                    int jumpTarget = Integer.parseInt(argument);
                    if (jumpTarget >= this.nLines()) errorsFound.add(new ScriptError(lineIndex, 3, 0));
                } catch (NumberFormatException e) {
                    if (!argument.equals("NOJUMP") && !this.hasLabel(argument)) errorsFound.add(new ScriptError(lineIndex, 3, 1, argument));
                }

                break;

            case "firstswitch":
            case "claim":
                if (args.length == 0) {
                    errorsFound.add(new ScriptError(lineIndex, 4, 0, "firstswitch"));
                } else {
                    if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 4, 1, "firstswitch"));
                    if (!this.hasLabel(argument + "FirstVessel")) extraInfo.add(argument + "FirstVessel");
                    if (!this.hasLabel(argument + "NotFirstVessel")) extraInfo.add(argument + "NotFirstVessel");

                    if (!extraInfo.isEmpty()) {
                        extraInfo.add(0, "firstswitch");
                        errorsFound.add(new ScriptError(lineIndex, 4, 3, extraInfo));
                    }
                }

                break;

            case "bladeswitch":
                switch (args.length) {
                    case 0:
                        errorsFound.add(new ScriptError(lineIndex, 4, 0, "bladeswitch"));
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
                        errorsFound.add(new ScriptError(lineIndex, 4, 2));
                }

                if (!extraInfo.isEmpty()) {
                    extraInfo.add(0, "bladeswitch");
                    errorsFound.add(new ScriptError(lineIndex, 4, 3, extraInfo));
                }
                break;

            case "moodswitch":
            case "harshswitch":
                if (args.length == 0) {
                    errorsFound.add(new ScriptError(lineIndex, 4, 0, "moodswitch"));
                } else {
                    if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 4, 1, "moodswitch"));
                    if (!this.hasLabel(argument + "Harsh")) extraInfo.add(argument + "Harsh");
                    if (!this.hasLabel(argument + "Soft")) extraInfo.add(argument + "Soft");

                    if (!extraInfo.isEmpty()) {
                        extraInfo.add(0, "moodswitch");
                        errorsFound.add(new ScriptError(lineIndex, 4, 3, extraInfo));
                    }
                }

                break;

            case "sourceswitch":
                if (args.length == 0) {
                    errorsFound.add(new ScriptError(lineIndex, 4, 4));
                } else {
                    if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 4, 1, "sourceswitch"));

                    boolean labelFound = false;
                    for (String label : labels.keySet()) {
                        if (label.endsWith(args[0])) {
                            labelFound = true;
                            break;
                        }
                    }

                    if (!labelFound) errorsFound.add(new ScriptError(lineIndex, 4, 5, args[0]));
                }

                break;
            
            case "switchjump":
                if (nArgs == 0) {
                    errorsFound.add(new ScriptError(lineIndex, 5, 0));
                    break;
                } else if (nArgs == 1) {
                    issuesFound.add(new ScriptIssue(lineIndex, 0, 0));
                    if (!this.hasLabel(argument)) extraInfo.add(argument);
                } else {
                    if (nArgs > 2) issuesFound.add(new ScriptIssue(lineIndex, 0, 1));
                    if (!this.hasLabel(args[0])) extraInfo.add(args[0]);
                    if (!this.hasLabel(args[1])) extraInfo.add(args[1]);
                }

                if (!extraInfo.isEmpty()) errorsFound.add(new ScriptError(lineIndex, 5, 1, extraInfo));
                break;
            
            case "numswitchjump":
                if (argument.isEmpty()) {
                    errorsFound.add(new ScriptError(lineIndex, 5, 2));
                } else {
                    for (String label : args) {
                        if (!this.hasLabel(label)) extraInfo.add(label);
                    }
                }

                if (!extraInfo.isEmpty()) errorsFound.add(new ScriptError(lineIndex, 5, 3, extraInfo));
                break;
            
            case "stringswitchjump":
                if (nArgs == 0) {
                    errorsFound.add(new ScriptError(lineIndex, 5, 4));
                    break;
                } else if (nArgs == 1) {
                    errorsFound.add(new ScriptError(lineIndex, 5, 5));
                    break;
                } else if (nArgs % 2 != 0) {
                    errorsFound.add(new ScriptError(lineIndex, 5, 6));
                }

                for (int i = 1; i < nArgs; i += 2) {
                    if (!this.hasLabel(args[i])) extraInfo.add(args[i]);
                }

                if (!extraInfo.isEmpty()) errorsFound.add(new ScriptError(lineIndex, 5, 7, extraInfo));
                break;

            case "numautojump":
                if (argument.isEmpty()) {
                    errorsFound.add(new ScriptError(lineIndex, 5, 8));
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
                if (argument.isEmpty()) errorsFound.add(new ScriptError(lineIndex, 5, 9));
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

        ArrayList<String> extraTemp = new ArrayList<>();

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
            extraTemp.clear();
            args = m.split("-");

            if (m.startsWith("interrupt")) {
                if (!m.equals("interrupt")) errorsFound.add(new ScriptError(lineIndex, 7, 11));

                if (presentMods.contains("interrupt")) {
                    if (!duplicateMods.contains("interrupt")) duplicateMods.add("interrupt");
                } else {
                    presentMods.add("interrupt");
                    if (!isDialogue) errorsFound.add(new ScriptError(lineIndex, 7, 10));
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
                            errorsFound.add(new ScriptError(lineIndex, 7, 3, "checkvoice"));
                        } else if (speaker == null) {
                            errorsFound.add(new ScriptError(lineIndex, 7, 4));
                        } else {
                            posVoiceChecks.add(speaker);
                        }
                    } else {
                        errorsFound.add(new ScriptError(lineIndex, 7, 3, "checkvoice"));
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
                    errorsFound.add(new ScriptError(lineIndex, 7, 3, "checknovoice"));
                } else {
                   for (int i = 1; i < args.length; i++) {
                        currentVoice = Voice.getVoice(args[i]);
                        if (currentVoice == null) {
                            invalidVoiceArgs.add(args[i]);
                        } else {
                            if (currentVoice == speaker) errorsFound.add(new ScriptError(lineIndex, 8, 8, speaker.toString()));
                            negVoiceChecks.add(currentVoice);
                        }
                    }
                }

            } else if (m.equals("firstvessel")) {
                if (presentMods.contains("firstvessel")) {
                    if (!duplicateMods.contains("firstvessel")) duplicateMods.add("firstvessel");
                } else {
                    presentMods.add("firstvessel");
                    if (presentMods.contains("firstvessel")) errorsFound.add(new ScriptError(lineIndex, 8, 0, "firstvessel & notfirstvessel"));

                    if (prefix.equals("firstswitch") || prefix.equals("claim")) {
                        extraTemp.add("firstvessel");
                        extraTemp.add("firstswitch");
                        errorsFound.add(new ScriptError(lineIndex, 8, 1, extraTemp));
                    }
                }

                if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 7, 6, "firstvessel"));

            } else if (m.equals("notfirstvessel")) {
                if (presentMods.contains("notfirstvessel")) {
                    if (!duplicateMods.contains("notfirstvessel")) duplicateMods.add("notfirstvessel");
                } else {
                    presentMods.add("notfirstvessel");
                    if (presentMods.contains("firstvessel")) errorsFound.add(new ScriptError(lineIndex, 8, 0, "firstvessel & notfirstvessel"));

                    if (prefix.equals("firstswitch") || prefix.equals("claim")) {
                        extraTemp.add("notfirstvessel");
                        extraTemp.add("firstswitch");
                        errorsFound.add(new ScriptError(lineIndex, 8, 1, extraTemp));
                    }
                }

                if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 7, 6, "notfirstvessel"));

            } else if (m.equals("hasblade")) {
                if (presentMods.contains("hasblade")) {
                    if (!duplicateMods.contains("hasblade")) duplicateMods.add("hasblade");
                } else {
                    presentMods.add("hasblade");
                    if (presentMods.contains("noblade")) errorsFound.add(new ScriptError(lineIndex, 8, 0, "hasblade & noblade"));

                    if (prefix.equals("bladeswitch")) {
                        extraTemp.add("hasblade");
                        extraTemp.add("bladeswitch");
                        errorsFound.add(new ScriptError(lineIndex, 8, 1, extraTemp));
                    }
                }
                
                if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 7, 6, "hasblade"));

            } else if (m.equals("noblade")) {
                if (presentMods.contains("noblade")) {
                    if (!duplicateMods.contains("noblade")) duplicateMods.add("noblade");
                } else {
                    presentMods.add("noblade");
                    if (presentMods.contains("hasblade")) errorsFound.add(new ScriptError(lineIndex, 8, 0, "hasblade & noblade"));

                    if (prefix.equals("bladeswitch")) {
                        extraTemp.add("noblade");
                        extraTemp.add("bladeswitch");
                        errorsFound.add(new ScriptError(lineIndex, 8, 1, extraTemp));
                    }
                }
                
                if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 7, 6, "noblade"));

            } else if (m.equals("sharedloop")) {
                if (presentMods.contains("sharedloop")) {
                    if (!duplicateMods.contains("sharedloop")) duplicateMods.add("sharedloop");
                } else {
                    presentMods.add("sharedloop");
                    if (presentMods.contains("noshare")) errorsFound.add(new ScriptError(lineIndex, 8, 0, "sharedloop & noshare"));
                }

                if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 7, 6, "sharedloop"));

            } else if (m.equals("noshare")) {
                if (presentMods.contains("noshare")) {
                    if (!duplicateMods.contains("noshare")) duplicateMods.add("noshare");
                } else {
                    presentMods.add("noshare");
                    if (presentMods.contains("sharedloop")) errorsFound.add(new ScriptError(lineIndex, 8, 0, "sharedloop & noshare"));
                }

                if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 7, 6, "noshare"));

            } else if (m.equals("sharedinsist")) {
                if (presentMods.contains("sharedinsist")) {
                    if (!duplicateMods.contains("sharedinsist")) duplicateMods.add("sharedinsist");
                } else {
                    presentMods.add("sharedinsist");
                    if (presentMods.contains("noinsist")) errorsFound.add(new ScriptError(lineIndex, 8, 0, "sharedinsist & noinsist"));
                }

                if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 7, 6, "sharedinsist"));

            } else if (m.equals("noinsist")) {
                if (presentMods.contains("noinsist")) {
                    if (!duplicateMods.contains("noinsist")) duplicateMods.add("noinsist");
                } else {
                    presentMods.add("noinsist");
                    if (presentMods.contains("sharedinsist")) errorsFound.add(new ScriptError(lineIndex, 8, 0, "sharedinsist & noinsist"));
                }

                if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 7, 6, "noinsist"));

            } else if (m.equals("mirrorask")) {
                if (presentMods.contains("mirrorask")) {
                    if (!duplicateMods.contains("mirrorask")) duplicateMods.add("mirrorask");
                } else {
                    presentMods.add("mirrorask");
                    if (presentMods.contains("nomirrorask")) errorsFound.add(new ScriptError(lineIndex, 8, 0, "mirrorask & nomirrorask"));
                }

                if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 7, 6, "mirrorask"));

            } else if (m.equals("nomirrorask")) {
                if (presentMods.contains("nomirrorask")) {
                    if (!duplicateMods.contains("nomirrorask")) duplicateMods.add("nomirrorask");
                } else {
                    presentMods.add("nomirrorask");
                    if (presentMods.contains("mirrorask")) errorsFound.add(new ScriptError(lineIndex, 8, 0, "mirrorask & nomirrorask"));
                }

                if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 7, 6, "nomirrorask"));

            } else if (m.equals("mirrortouch")) {
                if (presentMods.contains("mirrortouch")) {
                    if (!duplicateMods.contains("mirrortouch")) duplicateMods.add("mirrortouch");
                } else {
                    presentMods.add("mirrortouch");
                    if (presentMods.contains("nomirrortouch")) errorsFound.add(new ScriptError(lineIndex, 8, 0, "mirrortouch & nomirrortouch"));
                }

                if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 7, 6, "mirrortouch"));

            } else if (m.equals("nomirrortouch")) {
                if (presentMods.contains("nomirrortouch")) {
                    if (!duplicateMods.contains("nomirrortouch")) duplicateMods.add("nomirrortouch");
                } else {
                    presentMods.add("nomirrortouch");
                    if (presentMods.contains("mirrortouch")) errorsFound.add(new ScriptError(lineIndex, 8, 0, "mirrortouch & nomirrortouch"));
                }

                if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 7, 6, "nomirrortouch"));

            } else if (m.equals("mirror2")) {
                if (presentMods.contains("mirror2")) {
                    if (!duplicateMods.contains("mirror2")) duplicateMods.add("mirror2");
                } else {
                    presentMods.add("mirror2");
                    if (presentMods.contains("nomirror2")) errorsFound.add(new ScriptError(lineIndex, 8, 0, "mirror2 & nomirror2"));
                }

                if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 7, 6, "mirror2"));

            } else if (m.equals("nomirror2")) {
                if (presentMods.contains("nomirror2")) {
                    if (!duplicateMods.contains("nomirror2")) duplicateMods.add("nomirror2");
                } else {
                    presentMods.add("nomirror2");
                    if (presentMods.contains("mirror2")) errorsFound.add(new ScriptError(lineIndex, 8, 0, "mirror2 & nomirror2"));
                }

                if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 7, 6, "nomirror2"));

            } else if (m.equals("harsh")) {
                if (presentMods.contains("harsh")) {
                    if (!duplicateMods.contains("harsh")) duplicateMods.add("harsh");
                } else {
                    presentMods.add("harsh");
                    if (presentMods.contains("soft")) errorsFound.add(new ScriptError(lineIndex, 8, 0, "harsh & soft"));

                    if (prefix.equals("moodswitch") || prefix.equals("harshswitch")) {
                        extraTemp.add("harsh");
                        extraTemp.add("moodswitch");
                        errorsFound.add(new ScriptError(lineIndex, 8, 1, extraTemp));
                    }
                }

                if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 7, 6, "harsh"));

            } else if (m.equals("soft")) {
                if (presentMods.contains("soft")) {
                    if (!duplicateMods.contains("soft")) duplicateMods.add("soft");
                } else {
                    presentMods.add("soft");
                    if (presentMods.contains("harsh")) errorsFound.add(new ScriptError(lineIndex, 8, 0, "harsh & soft"));

                    if (prefix.equals("moodswitch") || prefix.equals("harshswitch")) {
                        extraTemp.add("soft");
                        extraTemp.add("moodswitch");
                        errorsFound.add(new ScriptError(lineIndex, 8, 1, extraTemp));
                    }
                }

                if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 7, 6, "soft"));

            } else if (m.equals("knowledge")) {
                if (presentMods.contains("knowledge")) {
                    if (!duplicateMods.contains("knowledge")) duplicateMods.add("knowledge");
                } else {
                    presentMods.add("knowledge");
                    if (presentMods.contains("noknowledge")) errorsFound.add(new ScriptError(lineIndex, 8, 0, "knowledge & noknowledge"));
                }

                if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 7, 6, "knowledge"));

            } else if (m.equals("noknowledge")) {
                if (presentMods.contains("noknowledge")) {
                    if (!duplicateMods.contains("noknowledge")) duplicateMods.add("noknowledge");
                } else {
                    presentMods.add("noknowledge");
                    if (presentMods.contains("knowledge")) errorsFound.add(new ScriptError(lineIndex, 8, 0, "knowledge & noknowledge"));
                }

                if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 7, 6, "noknowledge"));

            } else if (m.startsWith("ifsource")) {
                if (presentMods.contains("ifsource")) {
                    if (!duplicateMods.contains("ifsource")) duplicateMods.add("ifsource");
                } else {
                    presentMods.add("ifsource");

                    if (prefix.equals("sourceswitch")) {
                        extraTemp.add("ifsource");
                        extraTemp.add("sourceswitch");
                        errorsFound.add(new ScriptError(lineIndex, 8, 1, extraTemp));
                    }
                }
                
                switch (args.length) {
                    case 1:
                        errorsFound.add(new ScriptError(lineIndex, 7, 9, "ifsource"));
                        if (!m.equals("ifsource")) errorsFound.add(new ScriptError(lineIndex, 7, 3, "ifsource"));
                        break;

                    case 2:
                        if (!posTargetSources.contains(m)) posTargetSources.add(args[1]);
                        break;

                    default:
                        errorsFound.add(new ScriptError(lineIndex, 7, 7, "ifsourcenot"));
                }

            } else if (m.startsWith("ifsourcenot")) {
                if (presentMods.contains("ifsourcenot")) {
                    if (!duplicateMods.contains("ifsourcenot")) duplicateMods.add("ifsourcenot");
                } else {
                    presentMods.add("ifsourcenot");

                    if (prefix.equals("sourceswitch")) {
                        extraTemp.add("ifsourcenot");
                        extraTemp.add("sourceswitch");
                        errorsFound.add(new ScriptError(lineIndex, 8, 1, extraTemp));
                    }
                }
                
                switch (args.length) {
                    case 1:
                        errorsFound.add(new ScriptError(lineIndex, 7, 9, "ifsourcenot"));
                        if (!m.equals("ifsourcenot")) errorsFound.add(new ScriptError(lineIndex, 7, 3, "ifsourcenot"));
                        break;

                    case 2:
                        if (!negTargetSources.contains(m)) negTargetSources.add(args[1]);
                        break;

                    default:
                        errorsFound.add(new ScriptError(lineIndex, 7, 7, "ifsourcenot"));
                }

            } else if (m.equals("check")) {
                if (presentMods.contains("check")) {
                    if (!duplicateMods.contains("check")) duplicateMods.add("check");
                } else {
                    presentMods.add("check");
                    if (presentMods.contains("checkfalse")) errorsFound.add(new ScriptError(lineIndex, 8, 0, "check & checkfalse"));

                    if (prefix.equals("switchjump")) {
                        extraTemp.add("check");
                        extraTemp.add("switchjump");
                        errorsFound.add(new ScriptError(lineIndex, 8, 2, extraTemp));
                    }
                }
                
                if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 7, 6, "check"));

            } else if (m.equals("checkfalse")) {
                if (presentMods.contains("checkfalse")) {
                    if (!duplicateMods.contains("checkfalse")) duplicateMods.add("checkfalse");
                } else {
                    presentMods.add("checkfalse");
                    if (presentMods.contains("check")) errorsFound.add(new ScriptError(lineIndex, 8, 0, "check & checkfalse"));

                    if (prefix.equals("switchjump")) {
                        extraTemp.add("checkfalse");
                        extraTemp.add("switchjump");
                        errorsFound.add(new ScriptError(lineIndex, 8, 2, extraTemp));
                    }
                }
                
                if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 7, 6, "checkfalse"));

            } else if (m.startsWith("ifnum")) {
                if (presentMods.contains("ifnum")) {
                    if (!duplicateMods.contains("ifnum")) duplicateMods.add("ifnum");
                } else {
                    presentMods.add("ifnum");

                    if (prefix.equals("numswitchjump")) {
                        extraTemp.add("ifnum");
                        extraTemp.add("numswitchjump");
                        errorsFound.add(new ScriptError(lineIndex, 8, 2, extraTemp));
                    }
                }
                
                if (args.length == 2) {
                    try {
                        targetInt = Integer.parseInt(args[1]);
                        if (!posTargetInts.contains(targetInt)) posTargetInts.add(targetInt);
                    } catch (NumberFormatException e) {
                        errorsFound.add(new ScriptError(lineIndex, 7, 8, "ifnum"));
                    }
                } else if (args.length > 2) {
                    errorsFound.add(new ScriptError(lineIndex, 7, 7, "ifnum"));
                } else {
                    if (!m.equals("ifnum")) errorsFound.add(new ScriptError(lineIndex, 7, 3, "ifnum"));
                    if (!posTargetInts.contains(0)) posTargetInts.add(0);
                }

            } else if (m.startsWith("ifnumnot")) {
                if (presentMods.contains("ifnumnot")) {
                    if (!duplicateMods.contains("ifnumnot")) duplicateMods.add("ifnumnot");
                } else {
                    presentMods.add("ifnumnot");
                    if (prefix.equals("numswitchjump")) 

                    if (prefix.equals("numswitchjump")) {
                        extraTemp.add("ifnumnot");
                        extraTemp.add("numswitchjump");
                        errorsFound.add(new ScriptError(lineIndex, 8, 2, extraTemp));
                    }
                }
                
                if (args.length == 2) {
                    try {
                        targetInt = Integer.parseInt(args[1]);
                        if (!negTargetInts.contains(targetInt)) negTargetInts.add(targetInt);
                    } catch (NumberFormatException e) {
                        errorsFound.add(new ScriptError(lineIndex, 7, 8, "ifnumnot"));
                    }
                } else if (args.length > 2) {
                    errorsFound.add(new ScriptError(lineIndex, 7, 7, "ifnumnot"));
                } else {
                    if (!m.equals("ifnumnot")) errorsFound.add(new ScriptError(lineIndex, 7, 3, "ifnumnot"));
                    if (!negTargetInts.contains(0)) negTargetInts.add(0);
                }

            } else if (m.startsWith("ifstring")) {
                if (presentMods.contains("ifstring")) {
                    if (!duplicateMods.contains("ifstring")) duplicateMods.add("ifstring");
                } else {
                    presentMods.add("ifstring");

                    if (prefix.equals("stringswitchjump")) {
                        extraTemp.add("ifstring");
                        extraTemp.add("stringswitchjump");
                        errorsFound.add(new ScriptError(lineIndex, 8, 2, extraTemp));
                    }
                }
            
                switch (args.length) {
                    case 1:
                        errorsFound.add(new ScriptError(lineIndex, 7, 9, "ifstring"));
                        if (!m.equals("ifstring")) errorsFound.add(new ScriptError(lineIndex, 7, 3, "ifstring"));
                        break;

                    case 2:
                        if (!posTargetStrings.contains(m)) posTargetStrings.add(args[1]);
                        break;

                    default:
                        errorsFound.add(new ScriptError(lineIndex, 7, 7, "ifstring"));
                }

            } else if (m.startsWith("ifstringnot")) {
                if (presentMods.contains("ifstringnot")) {
                    if (!duplicateMods.contains("ifstringnot")) duplicateMods.add("ifstringnot");
                } else {
                    presentMods.add("ifstringnot");

                    if (prefix.equals("stringswitchjump")) {
                        extraTemp.add("ifstringnot");
                        extraTemp.add("stringswitchjump");
                        errorsFound.add(new ScriptError(lineIndex, 8, 2, extraTemp));
                    }
                }
                
                switch (args.length) {
                    case 1:
                        errorsFound.add(new ScriptError(lineIndex, 7, 9, "ifstringnot"));
                        if (!m.equals("ifstringnot")) errorsFound.add(new ScriptError(lineIndex, 7, 3, "ifstringnot"));
                        break;

                    case 2:
                        if (!negTargetStrings.contains(m)) negTargetStrings.add(args[1]);
                        break;

                    default:
                        errorsFound.add(new ScriptError(lineIndex, 7, 7, "ifstringnot"));
                }

            } else {
                invalidMods.add(m);
            }
        }

        if (!duplicateMods.isEmpty()) issuesFound.add(new ScriptIssue(lineIndex, 3, 0, duplicateMods));
        if (!invalidMods.isEmpty()) errorsFound.add(new ScriptError(lineIndex, 7, 2, invalidMods));
        if (!invalidVoiceArgs.isEmpty()) errorsFound.add(new ScriptError(lineIndex, 7, 5, invalidVoiceArgs));
        if (posTargetSources.size() > 1) errorsFound.add(new ScriptError(lineIndex, 8, 6, "ifsource"));
        if (posTargetInts.size() > 1) errorsFound.add(new ScriptError(lineIndex, 8, 6, "ifnum"));
        if (posTargetStrings.size() > 1) errorsFound.add(new ScriptError(lineIndex, 8, 6, "ifstring"));

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
                duplicatePosVoices.add(v.toString());
            } else {
                checkedVoices.add(v);
            }
        }
        
        checkedVoices.clear();
        for (Voice v : negVoiceChecks) {
            if (checkedVoices.contains(v)) {
                duplicateNegVoices.add(v.toString());
            } else {
                if (posVoiceChecks.contains(v)) impossibleVoiceChecks.add(v.toString());
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

        if (prefix.equals("break") && (presentMods.isEmpty() || (presentMods.size() == 1 && presentMods.contains("interrupt")))) {
            // If the given command is "break" and there are no valid modifiers
            errorsFound.add(new ScriptError(lineIndex, 2, 1));
        }

        if (!duplicatePosVoices.isEmpty()) issuesFound.add(new ScriptIssue(lineIndex, 3, 1, duplicatePosVoices));
        if (!duplicateNegVoices.isEmpty()) issuesFound.add(new ScriptIssue(lineIndex, 3, 1, duplicateNegVoices));
        if (!impossibleVoiceChecks.isEmpty()) errorsFound.add(new ScriptError(lineIndex, 8, 7, impossibleVoiceChecks));
        if (!impossibleSourceChecks.isEmpty()) errorsFound.add(new ScriptError(lineIndex, 8, 3, impossibleSourceChecks));
        if (!impossibleNumChecks.isEmpty()) errorsFound.add(new ScriptError(lineIndex, 8, 4, impossibleNumChecks));
        if (!impossibleStringChecks.isEmpty()) errorsFound.add(new ScriptError(lineIndex, 8, 5, impossibleStringChecks));
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
