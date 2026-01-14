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
        String modifiers;
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

                if (split.length > 2) errorsFound.add(new ScriptError(lineIndex, 8, 0));
                if (mods.length == 0) errorsFound.add(new ScriptError(lineIndex, 8, 1));
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
                    if (intArg < 0) errorsFound.add(new ScriptError(lineIndex, 1, 1, prefix));
                } catch (NumberFormatException e) {
                    if (!argument.equals("")) errorsFound.add(new ScriptError(lineIndex, 1, 0, prefix));
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
                    errorsFound.add(new ScriptError(lineIndex, 1, 0, prefix));
                }

                if (negativeArgs) errorsFound.add(new ScriptError(lineIndex, 1, 1, prefix));
                break;

            case "unlock":
                switch (args.length) {
                    case 0:
                        errorsFound.add(new ScriptError(lineIndex, 3, 0));
                        break;

                    case 1:
                        if (!manager.getTracker().achievementExists(argument)) errorsFound.add(new ScriptError(lineIndex, 3, 2, argument));
                        break;
                    
                    case 2:
                        errorsFound.add(new ScriptError(lineIndex, 3, 1));
                        break;
                }

                break;

            case "nowplaying":
                if (argument.isEmpty()) errorsFound.add(new ScriptError(lineIndex, 4));
                break;

            case "quietcreep":
                if (!argument.isEmpty()) issuesFound.add(new ScriptIssue(lineIndex, 1, 0));
                break;
                
            case "claimfold":
                if (!argument.isEmpty()) issuesFound.add(new ScriptIssue(lineIndex, 1, 1));
                break;

            case "jumpto":
                try {
                    int jumpTarget = Integer.parseInt(argument);
                    if (jumpTarget >= this.nLines()) errorsFound.add(new ScriptError(lineIndex, 5, 0));
                } catch (NumberFormatException e) {
                    if (!argument.equals("NOJUMP")) {
                        if (!this.hasLabel(argument)) errorsFound.add(new ScriptError(lineIndex, 5, 1, argument));
                    } 
                }

                break;

            case "firstswitch":
            case "claim":
                if (args.length == 0) {
                    errorsFound.add(new ScriptError(lineIndex, 6, 0, "firstswitch"));
                } else {
                    if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 6, 1, "firstswitch"));
                    if (!this.hasLabel(argument + "FirstVessel")) extraInfo.add(argument + "FirstVessel");
                    if (!this.hasLabel(argument + "NotFirstVessel")) extraInfo.add(argument + "NotFirstVessel");

                    if (!extraInfo.isEmpty()) {
                        extraInfo.add(0, "firstswitch");
                        errorsFound.add(new ScriptError(lineIndex, 6, 3, extraInfo));
                    }
                }

                break;

            case "bladeswitch":
                switch (args.length) {
                    case 0:
                        errorsFound.add(new ScriptError(lineIndex, 6, 0, prefix));
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
                        errorsFound.add(new ScriptError(lineIndex, 6, 2));
                }

                if (!extraInfo.isEmpty()) {
                    extraInfo.add(0, prefix);
                    errorsFound.add(new ScriptError(lineIndex, 6, 3, extraInfo));
                }
                break;

            case "moodswitch":
            case "harshswitch":
                if (args.length == 0) {
                    errorsFound.add(new ScriptError(lineIndex, 6, 0, prefix));
                } else {
                    if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 6, 1, "moodswitch"));
                    if (!this.hasLabel(argument + "Harsh")) extraInfo.add(argument + "Harsh");
                    if (!this.hasLabel(argument + "Soft")) extraInfo.add(argument + "Soft");

                    if (!extraInfo.isEmpty()) {
                        extraInfo.add(0, prefix);
                        errorsFound.add(new ScriptError(lineIndex, 6, 3, extraInfo));
                    }
                }

                break;

            case "voice2switch":
            case "voice3switch":
            case "sourceswitch":
                if (args.length == 0) {
                    errorsFound.add(new ScriptError(lineIndex, 6, 4, prefix));
                } else {
                    if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 6, 1, prefix));

                    boolean labelFound = false;
                    for (String label : labels.keySet()) {
                        if (label.endsWith(args[0])) {
                            labelFound = true;
                            break;
                        }
                    }

                    if (!labelFound) {
                        extraInfo.add(prefix);
                        extraInfo.add(args[0]);
                        errorsFound.add(new ScriptError(lineIndex, 6, 5, extraInfo));
                    } 
                }

                break;

            case "setbool":
                switch (args.length) {
                    case 0:
                        errorsFound.add(new ScriptError(lineIndex, 10, 0, prefix));
                        break;
                    case 1:
                        switch (argument) {
                            case "true":
                            case "false": break;
                            default: errorsFound.add(new ScriptError(lineIndex, 10, 2, argument));
                        }
                    default: errorsFound.add(new ScriptError(lineIndex, 10, 1, prefix));
                }

                break;

            case "setnum":
                switch (args.length) {
                    case 0:
                        errorsFound.add(new ScriptError(lineIndex, 10, 0, prefix));
                        break;
                    case 1:
                        try {
                            Integer.parseInt(argument);
                        } catch (NumberFormatException e) {
                            errorsFound.add(new ScriptError(lineIndex, 10, 3, argument));
                        }
                    default: errorsFound.add(new ScriptError(lineIndex, 10, 1, prefix));
                }

                break;

            case "setstring":
                switch (args.length) {
                    case 0:
                        errorsFound.add(new ScriptError(lineIndex, 10, 0, prefix));
                        break;
                    case 1: break;
                    default: errorsFound.add(new ScriptError(lineIndex, 10, 1, prefix));
                }

                break;
            
            case "switchjump":
                if (nArgs == 0) {
                    errorsFound.add(new ScriptError(lineIndex, 7, 0));
                    break;
                } else if (nArgs == 1) {
                    issuesFound.add(new ScriptIssue(lineIndex, 0, 0));
                    if (!this.hasLabel(argument)) extraInfo.add(argument);
                } else {
                    if (nArgs > 2) issuesFound.add(new ScriptIssue(lineIndex, 0, 1));
                    if (!this.hasLabel(args[0])) extraInfo.add(args[0]);
                    if (!this.hasLabel(args[1])) extraInfo.add(args[1]);
                }

                if (!extraInfo.isEmpty()) errorsFound.add(new ScriptError(lineIndex, 7, 1, extraInfo));
                break;
            
            case "numswitchjump":
                if (argument.isEmpty()) {
                    errorsFound.add(new ScriptError(lineIndex, 7, 2));
                } else {
                    for (String label : args) {
                        if (!this.hasLabel(label)) extraInfo.add(label);
                    }
                }

                if (!extraInfo.isEmpty()) errorsFound.add(new ScriptError(lineIndex, 7, 3, extraInfo));
                break;
            
            case "stringswitchjump":
                if (nArgs == 0) {
                    errorsFound.add(new ScriptError(lineIndex, 7, 4));
                    break;
                } else if (nArgs == 1) {
                    errorsFound.add(new ScriptError(lineIndex, 7, 5));
                    break;
                } else if (nArgs % 2 != 0) {
                    errorsFound.add(new ScriptError(lineIndex, 7, 6));
                }

                for (int i = 1; i < nArgs; i += 2) {
                    if (!this.hasLabel(args[i])) extraInfo.add(args[i]);
                }

                if (!extraInfo.isEmpty()) errorsFound.add(new ScriptError(lineIndex, 7, 7, extraInfo));
                break;

            case "numautojump":
                if (argument.isEmpty()) {
                    errorsFound.add(new ScriptError(lineIndex, 7, 8));
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
                if (argument.isEmpty()) errorsFound.add(new ScriptError(lineIndex, 7, 9));
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

        boolean redundantVoice2 = false;
        boolean redundantVoice3 = false;
        ArrayList<Voice> voice2Checks = new ArrayList<>();
        ArrayList<Voice> voice3Checks = new ArrayList<>();
        ArrayList<String> posTargetSources = new ArrayList<>();
        ArrayList<String> negTargetSources = new ArrayList<>();
        ArrayList<Integer> posTargetInts = new ArrayList<>();
        ArrayList<Integer> negTargetInts = new ArrayList<>();
        ArrayList<String> posTargetStrings = new ArrayList<>();
        ArrayList<String> negTargetStrings = new ArrayList<>();

        for (String m : modifiers) {
            if (m.isEmpty()) continue;
            args = m.split("-");

            // Dialogue line exclusive

            if (m.startsWith("interrupt")) {
                if (!m.equals("interrupt")) errorsFound.add(new ScriptError(lineIndex, 8, 11));

                if (presentMods.contains("interrupt")) {
                    if (!duplicateMods.contains("interrupt")) duplicateMods.add("interrupt");
                } else {
                    presentMods.add("interrupt");
                    if (!isDialogue) errorsFound.add(new ScriptError(lineIndex, 8, 10));
                }

            // Voice checks

            } else if (m.startsWith("checkvoice")) {
                if (presentMods.contains("checkvoice")) {
                    if (!duplicateMods.contains("checkvoice")) duplicateMods.add("checkvoice");
                } else {
                    presentMods.add("checkvoice");
                }
                
                if (args.length == 1) {
                    if (m.equals("checkvoice")) {
                        if (!isDialogue) {
                            errorsFound.add(new ScriptError(lineIndex, 8, 3, "checkvoice"));
                        } else if (speaker == null) {
                            errorsFound.add(new ScriptError(lineIndex, 8, 4));
                        } else {
                            posVoiceChecks.add(speaker);
                        }
                    } else {
                        errorsFound.add(new ScriptError(lineIndex, 8, 3, "checkvoice"));
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
                    errorsFound.add(new ScriptError(lineIndex, 8, 3, "checknovoice"));
                } else {
                   for (int i = 1; i < args.length; i++) {
                        currentVoice = Voice.getVoice(args[i]);
                        if (currentVoice == null) {
                            invalidVoiceArgs.add(args[i]);
                        } else {
                            if (currentVoice == speaker) errorsFound.add(new ScriptError(lineIndex, 9, 8, speaker.toString()));
                            negVoiceChecks.add(currentVoice);
                        }
                    }
                }

            // Any chapter checks

            } else if (m.equals("firstvessel")) {
                if (presentMods.contains(prefix)) {
                    if (!duplicateMods.contains(prefix)) duplicateMods.add(prefix);
                } else {
                    presentMods.add(prefix);
                    if (presentMods.contains(getNegativeCounterpart(prefix))) errorsFound.add(new ScriptError(lineIndex, 9, 0, prefix + " & " + getNegativeCounterpart(prefix)));

                    if (prefix.equals("firstswitch") || prefix.equals("claim")) {
                        errorsFound.add(new ScriptError(lineIndex, 9, 1, prefix, getAutoswitch(prefix)));
                    }
                }

                if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 8, 6, prefix));

            } else if (m.equals("notfirstvessel")) {
                if (presentMods.contains(prefix)) {
                    if (!duplicateMods.contains(prefix)) duplicateMods.add(prefix);
                } else {
                    presentMods.add(prefix);
                    if (presentMods.contains(getNegativeCounterpart(prefix))) errorsFound.add(new ScriptError(lineIndex, 9, 0, getNegativeCounterpart(prefix) + " & " + prefix));

                    if (prefix.equals("firstswitch") || prefix.equals("claim")) {
                        errorsFound.add(new ScriptError(lineIndex, 9, 1, prefix, getAutoswitch(prefix)));
                    }
                }

                if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 8, 6, prefix));

            } else if (m.equals("hasblade")) {
                if (presentMods.contains(prefix)) {
                    if (!duplicateMods.contains(prefix)) duplicateMods.add(prefix);
                } else {
                    presentMods.add(prefix);
                    if (presentMods.contains(getNegativeCounterpart(prefix))) errorsFound.add(new ScriptError(lineIndex, 9, 0, prefix + " & " + getNegativeCounterpart(prefix)));

                    if (prefix.equals(getAutoswitch(prefix))) {
                        errorsFound.add(new ScriptError(lineIndex, 9, 1, prefix, getAutoswitch(prefix)));
                    }
                }
                
                if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 8, 6, prefix));

            } else if (m.equals("noblade")) {
                if (presentMods.contains(prefix)) {
                    if (!duplicateMods.contains(prefix)) duplicateMods.add(prefix);
                } else {
                    presentMods.add(prefix);
                    if (presentMods.contains(getNegativeCounterpart(prefix))) errorsFound.add(new ScriptError(lineIndex, 9, 0, getNegativeCounterpart(prefix) + " & " + prefix));

                    if (prefix.equals(getAutoswitch(prefix))) {
                        errorsFound.add(new ScriptError(lineIndex, 9, 1, prefix, getAutoswitch(prefix)));
                    }
                }
                
                if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 8, 6, prefix));

            } else if (m.equals("harsh")) {
                if (presentMods.contains(prefix)) {
                    if (!duplicateMods.contains(prefix)) duplicateMods.add(prefix);
                } else {
                    presentMods.add(prefix);
                    if (presentMods.contains(getNegativeCounterpart(prefix))) errorsFound.add(new ScriptError(lineIndex, 9, 0, prefix + " & " + getNegativeCounterpart(prefix)));

                    if (prefix.equals("moodswitch") || prefix.equals("harshswitch")) {
                        errorsFound.add(new ScriptError(lineIndex, 9, 1, prefix, getAutoswitch(prefix)));
                    }
                }

                if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 8, 6, prefix));

            } else if (m.equals("soft")) {
                if (presentMods.contains(prefix)) {
                    if (!duplicateMods.contains(prefix)) duplicateMods.add(prefix);
                } else {
                    presentMods.add(prefix);
                    if (presentMods.contains(getNegativeCounterpart(prefix))) errorsFound.add(new ScriptError(lineIndex, 9, 0, getNegativeCounterpart(prefix) + " & " + prefix));

                    if (prefix.equals("moodswitch") || prefix.equals("harshswitch")) {
                        errorsFound.add(new ScriptError(lineIndex, 9, 1, prefix, getAutoswitch(prefix)));
                    }
                }

                if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 8, 6, prefix));

            // Chapter 2 or 3 checks

            } else if (m.startsWith("voice2")) {
                if (presentMods.contains(prefix)) {
                    if (!duplicateMods.contains(prefix)) duplicateMods.add(prefix);
                } else {
                    presentMods.add(prefix);
                }
                
                switch (args.length) {
                    case 1:
                        if (m.equals(prefix)) {
                            errorsFound.add(new ScriptError(lineIndex, 8, 9, prefix));
                        } else {
                            errorsFound.add(new ScriptError(lineIndex, 8, 3, prefix));
                        }
                        break;

                    case 2:
                        currentVoice = Voice.getVoice(args[1]);
                        if (currentVoice != null) {
                            if (voice2Checks.contains(currentVoice)) {
                                redundantVoice2 = true;
                            } else {
                                voice2Checks.add(currentVoice);
                            }
                        } 
                        break;

                    default:
                        errorsFound.add(new ScriptError(lineIndex, 8, 7, prefix));
                }

            } else if (m.startsWith("voice3")) {
                if (presentMods.contains(prefix)) {
                    if (!duplicateMods.contains(prefix)) duplicateMods.add(prefix);
                } else {
                    presentMods.add(prefix);
                }
                
                switch (args.length) {
                    case 1:
                        if (m.equals(prefix)) {
                            errorsFound.add(new ScriptError(lineIndex, 8, 9, prefix));
                        } else {
                            errorsFound.add(new ScriptError(lineIndex, 8, 3, prefix));
                        }
                        break;

                    case 2:
                        currentVoice = Voice.getVoice(args[1]);
                        if (currentVoice != null) {
                            if (voice3Checks.contains(currentVoice)) {
                                redundantVoice3 = true;
                            } else {
                                voice3Checks.add(currentVoice);
                            }
                        } 
                        break;

                    default:
                        errorsFound.add(new ScriptError(lineIndex, 8, 7, prefix));
                }

            } else if (m.startsWith("ifsource")) {
                if (presentMods.contains(prefix)) {
                    if (!duplicateMods.contains(prefix)) duplicateMods.add(prefix);
                } else {
                    presentMods.add(prefix);

                    if (prefix.equals(getAutoswitch(prefix))) {
                        errorsFound.add(new ScriptError(lineIndex, 9, 1, prefix, getAutoswitch(prefix)));
                    }
                }
                
                switch (args.length) {
                    case 1:
                        if (m.equals("ifsource")) {
                            errorsFound.add(new ScriptError(lineIndex, 8, 9, prefix));
                        } else {
                            errorsFound.add(new ScriptError(lineIndex, 8, 3, prefix));
                        }
                        break;

                    case 2:
                        if (!posTargetSources.contains(args[1])) posTargetSources.add(args[1]);
                        break;

                    default:
                        errorsFound.add(new ScriptError(lineIndex, 8, 7, prefix));
                }

            } else if (m.startsWith("ifsourcenot")) {
                if (presentMods.contains(prefix)) {
                    if (!duplicateMods.contains(prefix)) duplicateMods.add(prefix);
                } else {
                    presentMods.add(prefix);

                    if (prefix.equals(getAutoswitch(prefix))) {
                        errorsFound.add(new ScriptError(lineIndex, 9, 1, prefix, getAutoswitch(prefix)));
                    }
                }
                
                switch (args.length) {
                    case 1:
                        if (m.equals("ifsource")) {
                            errorsFound.add(new ScriptError(lineIndex, 8, 9, prefix));
                        } else {
                            errorsFound.add(new ScriptError(lineIndex, 8, 3, prefix));
                        }
                        break;

                    case 2:
                        if (!negTargetSources.contains(args[1])) negTargetSources.add(args[1]);
                        break;

                    default:
                        errorsFound.add(new ScriptError(lineIndex, 8, 7, prefix));
                }

            } else if (m.equals("knowledge") || m.equals("sharedloop") || m.equals("sharedinsist") || m.equals("mirrorask") || m.equals("mirrortouch") || m.equals("mirror2") || m.equals("threwblade") || m.equals("chainsfree") || m.equals("tookblade") || m.equals("narrproof") || m.equals("drop1") || m.equals("whatdo1") || m.equals("rescue1") || m.equals("abandoned") || m.equals("faceask") || m.equals("deathshared") || m.equals("possessask") || m.equals("cantwontask") || m.equals("endslay") || m.equals("forcedblade") || m.equals("headwatch") || m.equals("goodseen") || m.equals("heartstop") || m.equals("cutroute")) {
                if (presentMods.contains(prefix)) {
                    if (!duplicateMods.contains(prefix)) duplicateMods.add(prefix);
                } else {
                    presentMods.add(prefix);
                    if (presentMods.contains(getNegativeCounterpart(prefix))) errorsFound.add(new ScriptError(lineIndex, 9, 0, prefix + " & " + getNegativeCounterpart(prefix)));
                }

                if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 8, 6, prefix));

            } else if (m.equals("noknowledge") ||m.equals("noshare") || m.equals("noinsist") || m.equals("nomirrorask") || m.equals("nomirrortouch") || m.equals("nomirror2") || m.equals("nothrow") || m.equals("notfree") || m.equals("leftblade") || m.equals("noproof") || m.equals("nodrop1") || m.equals("nowhatdo1") || m.equals("norescue1") || m.equals("noabandon") || m.equals("nofaceask") || m.equals("nodeathshare") || m.equals("nopossessask") || m.equals("nocantwontask") || m.equals("noendslay") || m.equals("noforce") || m.equals("nowatch") || m.equals("goodnotseen") || m.equals("noheartstop") || m.equals("nocut")) {
                if (presentMods.contains(prefix)) {
                    if (!duplicateMods.contains(prefix)) duplicateMods.add(prefix);
                } else {
                    presentMods.add(prefix);
                    if (presentMods.contains(getNegativeCounterpart(prefix))) errorsFound.add(new ScriptError(lineIndex, 9, 0, getNegativeCounterpart(prefix) + " & " + prefix));
                }

                if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 8, 6, prefix));

            // Given condition checks

            } else if (m.equals("check")) {
                if (presentMods.contains(prefix)) {
                    if (!duplicateMods.contains(prefix)) duplicateMods.add(prefix);
                } else {
                    presentMods.add(prefix);
                    if (presentMods.contains(getNegativeCounterpart(prefix))) errorsFound.add(new ScriptError(lineIndex, 9, 0, prefix + " & " + getNegativeCounterpart(prefix)));

                    if (prefix.equals(getAutoswitch(prefix))) {
                        errorsFound.add(new ScriptError(lineIndex, 9, 2, prefix, getAutoswitch(prefix)));
                    }
                }
                
                if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 8, 6, prefix));

            } else if (m.equals("checkfalse")) {
                if (presentMods.contains(prefix)) {
                    if (!duplicateMods.contains(prefix)) duplicateMods.add(prefix);
                } else {
                    presentMods.add(prefix);
                    if (presentMods.contains(getNegativeCounterpart(prefix))) errorsFound.add(new ScriptError(lineIndex, 9, 0, getNegativeCounterpart(prefix) + " & " + prefix));

                    if (prefix.equals(getAutoswitch(prefix))) {
                        errorsFound.add(new ScriptError(lineIndex, 9, 2, prefix, getAutoswitch(prefix)));
                    }
                }
                
                if (args.length != 1) errorsFound.add(new ScriptError(lineIndex, 8, 6, prefix));

            } else if (m.startsWith("ifnum")) {
                if (presentMods.contains(prefix)) {
                    if (!duplicateMods.contains(prefix)) duplicateMods.add(prefix);
                } else {
                    presentMods.add(prefix);

                    if (prefix.equals(getAutoswitch(prefix))) {
                        errorsFound.add(new ScriptError(lineIndex, 9, 2, prefix, getAutoswitch(prefix)));
                    }
                }
                
                if (args.length == 2) {
                    try {
                        targetInt = Integer.parseInt(args[1]);
                        if (!posTargetInts.contains(targetInt)) posTargetInts.add(targetInt);
                    } catch (NumberFormatException e) {
                        errorsFound.add(new ScriptError(lineIndex, 8, 8, prefix));
                    }
                } else if (args.length > 2) {
                    errorsFound.add(new ScriptError(lineIndex, 8, 7, prefix));
                } else {
                    if (!m.equals("ifnum")) errorsFound.add(new ScriptError(lineIndex, 8, 3, prefix));
                    if (!posTargetInts.contains(0)) posTargetInts.add(0);
                }

            } else if (m.startsWith("ifnumnot")) {
                if (presentMods.contains(prefix)) {
                    if (!duplicateMods.contains(prefix)) duplicateMods.add(prefix);
                } else {
                    presentMods.add(prefix);

                    if (prefix.equals(getAutoswitch(prefix))) {
                        errorsFound.add(new ScriptError(lineIndex, 9, 2, prefix, getAutoswitch(prefix)));
                    }
                }
                
                if (args.length == 2) {
                    try {
                        targetInt = Integer.parseInt(args[1]);
                        if (!negTargetInts.contains(targetInt)) negTargetInts.add(targetInt);
                    } catch (NumberFormatException e) {
                        errorsFound.add(new ScriptError(lineIndex, 8, 8, prefix));
                    }
                } else if (args.length > 2) {
                    errorsFound.add(new ScriptError(lineIndex, 8, 7, prefix));
                } else {
                    if (!m.equals("ifnumnot")) errorsFound.add(new ScriptError(lineIndex, 8, 3, prefix));
                    if (!negTargetInts.contains(0)) negTargetInts.add(0);
                }

            } else if (m.startsWith("ifstring")) {
                if (presentMods.contains(prefix)) {
                    if (!duplicateMods.contains(prefix)) duplicateMods.add(prefix);
                } else {
                    presentMods.add(prefix);

                    if (prefix.equals(getAutoswitch(prefix))) {
                        errorsFound.add(new ScriptError(lineIndex, 9, 2, prefix, getAutoswitch(prefix)));
                    }
                }
            
                switch (args.length) {
                    case 1:
                        if (m.equals("ifstring")) {
                            errorsFound.add(new ScriptError(lineIndex, 8, 9, prefix));
                        } else {
                            errorsFound.add(new ScriptError(lineIndex, 8, 3, prefix));
                        }
                        break;

                    case 2:
                        if (!posTargetStrings.contains(args[1])) posTargetStrings.add(args[1]);
                        break;

                    default:
                        errorsFound.add(new ScriptError(lineIndex, 8, 7, prefix));
                }

            } else if (m.startsWith("ifstringnot")) {
                if (presentMods.contains(prefix)) {
                    if (!duplicateMods.contains(prefix)) duplicateMods.add(prefix);
                } else {
                    presentMods.add(prefix);

                    if (prefix.equals(getAutoswitch(prefix))) {
                        errorsFound.add(new ScriptError(lineIndex, 9, 2, prefix, getAutoswitch(prefix)));
                    }
                }
                
                switch (args.length) {
                    case 1:
                        if (m.equals("ifstringnot")) {
                            errorsFound.add(new ScriptError(lineIndex, 8, 9, prefix));
                        } else {
                            errorsFound.add(new ScriptError(lineIndex, 8, 3, prefix));
                        }
                        break;

                    case 2:
                        if (!negTargetStrings.contains(args[1])) negTargetStrings.add(args[1]);
                        break;

                    default:
                        errorsFound.add(new ScriptError(lineIndex, 8, 7, prefix));
                }

            } else {
                invalidMods.add(m);
            }
        }

        if (!duplicateMods.isEmpty()) issuesFound.add(new ScriptIssue(lineIndex, 3, 0, duplicateMods));
        if (!invalidMods.isEmpty()) errorsFound.add(new ScriptError(lineIndex, 8, 2, invalidMods));
        if (!invalidVoiceArgs.isEmpty()) errorsFound.add(new ScriptError(lineIndex, 8, 5, invalidVoiceArgs));
        if (voice2Checks.size() > 1) errorsFound.add(new ScriptError(lineIndex, 9, 6, "voice2"));
        if (voice3Checks.size() > 1) errorsFound.add(new ScriptError(lineIndex, 9, 6, "voice3"));
        if (posTargetSources.size() > 1) errorsFound.add(new ScriptError(lineIndex, 9, 6, "ifsource"));
        if (posTargetInts.size() > 1) errorsFound.add(new ScriptError(lineIndex, 9, 6, "ifnum"));
        if (posTargetStrings.size() > 1) errorsFound.add(new ScriptError(lineIndex, 9, 6, "ifstring"));

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
            for (int i = 0; i < posTargetSources.size(); i++) {
                if (negTargetSources.contains(posTargetSources.get(i))) {
                    impossibleSourceChecks.add(posTargetSources.get(i));
                } else if (i != 0) {
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
            for (int i = 0; i < posTargetSources.size(); i++) {
                if (negTargetInts.contains(posTargetInts.get(i))) {
                    impossibleNumChecks.add(posTargetInts.get(i).toString());
                } else if (i != 0) {
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
            for (int i = 0; i < posTargetSources.size(); i++) {
                if (negTargetStrings.contains(posTargetStrings.get(i))) {
                    impossibleStringChecks.add(posTargetStrings.get(i));
                } else if (i != 0) {
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
        if (!impossibleVoiceChecks.isEmpty()) errorsFound.add(new ScriptError(lineIndex, 9, 7, impossibleVoiceChecks));
        if (!impossibleSourceChecks.isEmpty()) errorsFound.add(new ScriptError(lineIndex, 9, 3, impossibleSourceChecks));
        if (!impossibleNumChecks.isEmpty()) errorsFound.add(new ScriptError(lineIndex, 9, 4, impossibleNumChecks));
        if (!impossibleStringChecks.isEmpty()) errorsFound.add(new ScriptError(lineIndex, 9, 5, impossibleStringChecks));
        if (redundantVoice2) issuesFound.add(new ScriptIssue(lineIndex, 4, 0, "voice2"));
        if (redundantVoice3) issuesFound.add(new ScriptIssue(lineIndex, 4, 0, "voice3"));
        if (redundantSource) issuesFound.add(new ScriptIssue(lineIndex, 4, 1, "ifsource & ifsourcenot"));
        if (redundantNum) issuesFound.add(new ScriptIssue(lineIndex, 4, 1, "ifnum & ifnumnot"));
        if (redundantString) issuesFound.add(new ScriptIssue(lineIndex, 4, 1, "ifstring & ifstringnot"));
    }

    /**
     * Returns the negative counterpart of a given modifier
     * @param modifier the modifier to get the negative counterpart of
     * @return the negative counterpart of the given modifier
     */
    private static String getNegativeCounterpart(String modifier) {
        switch (modifier) {
            // Checks that can apply at any time
            case "checkvoice": return "checknovoice";
            case "checknovoice": return "checkvoice";

            case "firstvessel": return "notfirstvessel";
            case "notfirstvessel": return "firstvessel";

            case "hasblade": return "noblade";
            case "noblade": return "hasblade";

            case "harsh": return "soft";
            case "soft": return "harsh";

            case "knowledge": return "noknowledge";
            case "noknowledge": return "knowledge";

            // Checks that apply during Chapter 2 or 3
            case "voice2": return "voice2not";
            case "voice2not": return "voice2";

            case "ifsource": return "ifsourcenot";
            case "ifsourcenot": return "ifsource";

            case "sharedloop": return "noshare";
            case "noshare": return "sharedloop";
            
            case "sharedinsist": return "noinsist";
            case "noinsist": return "sharedinsist";

            case "mirrorask": return "nomirrorask";
            case "nomirrorask": return "mirrorask";

            case "mirrortouch": return "nomirrortouch";
            case "nomirrortouch": return "mirrortouch";

            case "mirror2": return "nomirror2";
            case "nomirror2": return "mirror2";

            case "threwblade": return "nothrow";
            case "nothrow": return "threwblade";

            case "tookblade": return "leftblade";
            case "leftblade": return "tookblade";

            case "chainsfree": return "notfree";
            case "notfree": return "chainsfree";

            // Checks that apply during Chapter 2 only
            case "narrproof": return "noproof";
            case "noproof": return "narrproof";

            case "drop1": return "nodrop1";
            case "nodrop1": return "drop1";

            case "whatdo1": return "nowhatdo1";
            case "nowhatdo1": return "whatdo1";

            case "rescue1": return "norescue1";
            case "norescue1": return "rescue1";

            // Checks that apply during Chapter 3 only
            case "voice3": return "voice3not";
            case "voice3not": return "voice3";

            case "abandoned": return "noabandon";
            case "noabandon": return "abandoned";

            case "faceask": return "nofaceask";
            case "nofaceask": return "faceask";

            case "deathshared": return "nodeathshare";
            case "nodeathshare": return "deathshared";

            case "possessask": return "nopossessask";
            case "nopossessask": return "possessask";

            case "cantwontask": return "nocantwontask";
            case "nocantwontask": return "cantwontask";

            case "endslay": return "noendslay";
            case "noendslay": return "endslay";

            case "forcedblade": return "noforce";
            case "noforce": return "forcedblade";

            case "headwatch": return "nowatch";
            case "nowatch": return "headwatch";

            case "goodseen": return "goodnotseen";
            case "goodnotseen": return "goodseen";

            case "heartstop": return "noheartstop";
            case "noheartstop": return "heartstop";

            case "cutroute": return "nocut";
            case "nocut": return "cutroute";

            // Condition checks
            case "check": return "checkfalse";
            case "checkfalse": return "check";

            case "ifnum": return "ifnumnot";
            case "ifnumnot": return "ifnum";

            case "ifstring": return "ifstringnot";
            case "ifstringnot": return "ifstring";

            default: return "";
        }
    }

    /**
     * Returns the corresponding autoswitch for a given modifier
     * @param modifier the modifier to return the corresponding autoswitch of
     * @return the corresponding autoswitch for the given modifier
     */
    private static String getAutoswitch(String modifier) {
        switch (modifier) {
            case "firstvessel":
            case "notfirstvessel": return "firstswitch";

            case "hasblade":
            case "noblade": return "bladeswitch";

            case "harsh":
            case "soft": return "moodswitch";

            case "voice2":
            case "voice2not": return "voice2switch";

            case "voice3": return "voice3not";
            case "voice3not": return "voice3switch";

            case "ifsource":
            case "ifsourcenot": return "sourceswitch";

            case "check":
            case "checkfalse": return "switchjump";

            case "ifnum":
            case "ifnumnot": return "numswitchjump";

            case "ifstring":
            case "ifstringnot": return "stringswitchjump";

            default: return "";
        }
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
