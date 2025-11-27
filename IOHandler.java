import java.util.Scanner;

public class IOHandler {
    
    private GameManager manager;
    private Scanner input;

    public static final int WRAPCOLUMNS = 80; // default = 80
    private static final String DIVIDER = "-----------------------------------";

    // --- CONSTRUCTORS ---

    public IOHandler(GameManager manager) {
        this.input = new Scanner(System.in);
        this.manager = manager;
    }

    // only for testing purposes!
    private IOHandler() {
        this(new GameManager());
    }

    // --- ACCESSORS & MANIPULATORS ---

    private Cycle currentCycle() {
        return this.manager.getCurrentCycle();
    }

    // --- BASIC INPUT ---

    public void waitForInput() {
        this.input.nextLine();
    }

    public String getInput() {
        System.out.print("> ");
        String in = this.input.nextLine();
        return in.toLowerCase();
    }

    // --- PRINT DIALOGUE ---

    public void printDivider(boolean wait) {
        this.printDialogueLine(new DialogueLine(DIVIDER, wait), 1.75);
    }

    public void printDivider() {
        this.printDivider(true);
    }

    public void printDialogueLine(DialogueLine line, double speedMultiplier) {
        line.print(speedMultiplier);

        if (line.isInterrupted()) {
            System.out.print("\n");
        } else {
            this.input.nextLine();
        }
    }

    public void printDialogueLine(DialogueLine line) {
        line.print();

        if (line.isInterrupted()) {
            System.out.print("\n");
        } else {
            this.input.nextLine();
        }
    }

    public void printDialogueLine(String line, boolean isInterrupted) {
        DialogueLine lineDialogue = new DialogueLine(line, isInterrupted);
        this.printDialogueLine(lineDialogue);
    }

    public void printDialogueLine(String line) {
        this.printDialogueLine(line, false);
    }

    // --- COMMAND HANDLING ---

    public String promptCommand(DialogueLine prompt) {
        prompt.print();
        String command = this.getInput();
        String parsed = null;

        while (parsed == null) {
            try {
                parsed = this.parseCommand(command);
            } catch (Exception e) {
                // Invalid command; re-input

            }
        }

        return parsed;
    }

    public String promptCommand(String prompt) {
        return this.promptCommand(new DialogueLine(prompt));
    }

    private String parseCommand(Cycle cycle, String command) throws Exception {
        /* Returns "cMeta" if the command is a meta command (HELP, SHOW, or TOGGLE);
           Or returns the ID-String (always starting with "c") of the command's "outcome" (accounting for whether the command is currently accessible);
           Or throws an Exception (caught by either promptCommand() or promptOptionsMenu()) if the command or its arguments is invalid */

        String[] split = command.split(" ", 2);
        String prefix = split[0];

        String argument;
        if (split.length == 2) {
            argument = split[1];
            argument = argument.trim(); // Trim any trailing spaces
        } else {
            argument = "";
        }

        Command c = Command.getCommand(prefix);
        if (c == null) {
            throw new Exception("Invalid command");
        }

        String commandOutcome = "c";
        switch (c) {
            case HELP:
                this.manager.help(argument);
                commandOutcome += "Meta";
                break;
            case SHOW:
                cycle.show(argument);
                commandOutcome += "Meta";
                break;
            case TOGGLE:
                this.manager.toggle(argument);
                commandOutcome += "Meta";
                break;
            case GO:
                commandOutcome += cycle.go(argument);
                break;
            case DIRECTGO:
                commandOutcome += cycle.go(prefix);
                break;
            case WALK:
                commandOutcome += cycle.go(argument);
                break;
            case ENTER:
                commandOutcome += cycle.enter(argument);
                break;
            case LEAVE:
                commandOutcome += cycle.leave(argument);
                break;
            case PROCEED:
                commandOutcome += cycle.proceed(argument);
                break;
            case TURN:
                commandOutcome += cycle.turn(argument);
                break;
            case APPROACH:
                commandOutcome += cycle.approach(argument);
                break;
            case SLAY:
                commandOutcome += cycle.slay(argument);
                break;
            case TAKE:
                commandOutcome += cycle.take(argument);
                break;
            case DROP:
                commandOutcome += cycle.drop(argument);
                break;
            case THROW:
                commandOutcome += cycle.throwBlade(argument);
                break;
        }

        /* POSSIBLE OUTCOMES:
        (all outcomes start with c, to indicate at a glance that they are from a command)
           - cMeta (HELP, SHOW, TOGGLE)
           - cFail (invalid argument, even after second prompt)

           - cGoLeave
           - cGoPath
           - cGoHill
           - cGoCabin
           - cGoStairs
           - cGoBasement
           - cGoFail
           - cEnterFail
           - cLeaveFail

           - cApproach
           - cApproachAtMirrorFail (you're already at the mirror)
           - cApproachFail

           - cSlayPrincess
           - cSlayNoPrincessFail (Princess isn't even present)
           - cSlayPrincessNoBladeFail (don't have the blade)
           - cSlayPrincessFail (can't slay right now)
           - cSlaySelf
           - cSlaySelfNoBladeFail (don't have the blade)
           - cSlaySelfFail (can't slay self right now)

           - cTake
           - cTakeHasBladeFail (you already have the blade)
           - cTakeFail (blade isn't here / can't take right now)
           - cDrop
           - cDropNoBladeFail (you don't even have the blade)
           - cDropFail (can't drop right now)
           - cThrow
           - cThrowNoBladeFail (you don't even have the blade)
           - cThrowFail (can't throw right now)
        */

        /* TEMPLATE CASES FOR COMMANDS:
            case "cMeta":

            case "cGoLeave":
            case "cGoPath":
            case "cGoHill":
            case "cGoCabin":
            case "cGoStairs":
            case "cGoBasement":

            case "cApproach":

            case "cSlayPrincess":
            case "cSlaySelf":

            case "cTake":
            case "cDrop":
            case "cThrow":

            case "cGoFail":
            case "cEnterFail":
            case "cLeaveFail":

            case "cApproachAtMirrorFail":
            case "cApproachFail":
                
            case "cSlayNoPrincessFail":
            case "cSlayPrincessNoBladeFail":
            case "cSlayPrincessFail":
            case "cSlaySelfNoBladeFail":
            case "cSlaySelfFail":
                
            case "cTakeHasBladeFail":
            case "cTakeFail":
            case "cDropNoBladeFail":
            case "cDropFail":
            case "cThrowNoBladeFail":
            case "cThrowFail":
                
            case "cFail":
        */

        if (commandOutcome.equals("cFail")) {
            throw new Exception("Invalid argument");
        }

        return commandOutcome;
    }

    private String parseCommand(String command) throws Exception {
        return this.parseCommand(this.currentCycle(), command);
    }

    // --- OPTIONS HANDLING ---
    
    public String promptOptionsMenu(OptionsMenu options, DialogueLine exclusiveOverride) {
        // returns choice id or command outcome
        Cycle cycle = this.currentCycle();
        
        System.out.println();
        wrapPrintln(options.toString());
        return this.parseOptionChoice(cycle, options, exclusiveOverride);
    }

    public String promptOptionsMenu(OptionsMenu options, String exclusiveOverride) {
        return this.promptOptionsMenu(options, new DialogueLine(exclusiveOverride, true));
    }

    public String promptOptionsMenu(OptionsMenu options) {
        return this.promptOptionsMenu(options, new DialogueLine());
    }

    private String parseOptionChoice(Cycle cycle, OptionsMenu options, DialogueLine exclusiveOverride) {
        // Returns choice id or command outcome
        boolean isOption = true;
        int choiceN = -1;
        String outcome;

        System.out.print("\n");
        String in = this.getInput();

        try {
            choiceN = Integer.parseInt(in);
        } catch (NumberFormatException e) {
            isOption = false;
        }

        if (isOption) {
            try {
                return options.playerChoose(choiceN);
            } catch (IllegalArgumentException e) {
                if (cycle == null) {
                    this.printDialogueLine("[That is not a choice available to you.]", true);
                } else if (!cycle.hasVoice(Voice.NARRATOR)) {
                    this.printDialogueLine("[That is not a choice available to you.]", true);
                } else {
                    this.printDialogueLine(new VoiceDialogueLine(Voice.NARRATOR, "What are you even trying to do? You're not accomplishing anything.", true));
                }

                return this.parseOptionChoice(cycle, options, exclusiveOverride);
            }
        } else {
            boolean isTrueExclusive = false;
            if (cycle == null) {
                isTrueExclusive = true;
            } else if (cycle.trueExclusiveMenu()) {
                isTrueExclusive = true;
            }

            if (options.isExclusive() || isTrueExclusive) {
                if (isTrueExclusive) {
                    try {
                        outcome = this.parseCommand(cycle, in);
                    } catch (Exception e) {
                        outcome = "cFail";
                    }
                } else {
                    outcome = "";
                }

                if (outcome.equals("cFail")) {
                    this.printDialogueLine("[That is not a valid command.]", true);
                } else if (!outcome.equals("cMeta")) {
                    if (exclusiveOverride.isEmpty()) {
                        if (cycle == null) {
                            this.printDialogueLine("[You have no other options.]", true);
                        } else if (!cycle.hasVoice(Voice.NARRATOR)) {
                            this.printDialogueLine("[You have no other options.]", true);
                        } else {
                            this.printDialogueLine(new VoiceDialogueLine(Voice.NARRATOR, "You have to make a decision.", true));
                        }
                    } else {
                        this.printDialogueLine(exclusiveOverride);
                    }
                }

                return this.parseOptionChoice(cycle, options, exclusiveOverride);
            } else {
                try {
                    outcome = this.parseCommand(cycle, in);
                    return (outcome.equals("cMeta")) ? this.parseOptionChoice(cycle, options, exclusiveOverride) : outcome;
                } catch (Exception e) {
                    // Invalid command; re-input, do not show options again
                    
                    if (cycle == null) {
                        this.printDialogueLine("[That is not a valid command.]", true);
                    } else if (!cycle.hasVoice(Voice.NARRATOR)) {
                        this.printDialogueLine("[That is not a valid command.]", true);
                    } else {
                        this.printDialogueLine(new VoiceDialogueLine(Voice.NARRATOR, "What are you even trying to do? You're not accomplishing anything.", true));
                    }

                    return this.parseOptionChoice(cycle, options, exclusiveOverride);
                }
            }
        }
    }

    // --- YES/NO HANDLING ---

    public boolean promptYesNo(DialogueLine prompt) {
        prompt.print();
        return this.parseYesNo();
    }

    public boolean promptYesNo(String prompt) {
        return this.promptYesNo(prompt, true);
    }

    public boolean promptYesNo(String prompt, boolean slowPrint) {
        if (slowPrint) {
            return this.promptYesNo(new DialogueLine(prompt));
        } else {
            wrapPrint(prompt);
            return this.parseYesNo(slowPrint);
        }
    }

    private boolean parseYesNo(boolean slowPrint) {
        System.out.print("\n");
        String in = this.getInput();

        if (in.equals("y") || in.equals("yes")) {
            return true;
        } else if (in.equals("n") || in.equals("no")) {
            return false;
        } else {
            return this.promptYesNo("Please answer \"yes\" or \"no\".", slowPrint);
        }
    }

    private boolean parseYesNo() {
        return this.parseYesNo(true);
    }

    public void closeInput() {
        this.input.close();
    }

    // --- WRAPAROUND MANAGEMENT ---

    public static String wordWrap(String s) {
        // HANDLE STRINGS WITH LINE BREAKS ALREADY IN THEM

        String wrappedLine = "";
        String[] lines = s.split("\n");
        String[] wordsInLine;
        int columnInLine;

        for (int i = 0; i < lines.length; i++) {
            if (i != 0) {
                wrappedLine += "\n";
            }

            columnInLine = 0;
            wordsInLine = lines[i].split(" ");

            for (int j = 0; j < wordsInLine.length; j++) {
                if (j != 0) {
                    columnInLine += 1;
                }

                columnInLine += wordsInLine[j].length();

                if (columnInLine > WRAPCOLUMNS) {
                    wrappedLine += "\n" + wordsInLine[j];
                    columnInLine = wordsInLine[j].length();
                } else {
                    if (j != 0) {
                        wrappedLine += " ";
                    }

                    wrappedLine += wordsInLine[j];
                }
            }
        }

        return wrappedLine;
    }

    public static String wordWrap(DialogueLine line) {
        return wordWrap(line.toString());
    }

    public static void wrapPrint(String s) {
        System.out.print(wordWrap(s));
    }

    public static void wrapPrintln(String s) {
        System.out.println(wordWrap(s));
    }

}
