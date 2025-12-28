import java.io.Closeable;
import java.util.Scanner;

public class IOHandler implements Closeable {
    
    private final GameManager manager;
    private final Scanner input;

    public static final int WRAPCOLUMNS = 80; // default = 80
    private static final DialogueLine DIVIDER = new DialogueLine("-----------------------------------");
    private static final VoiceDialogueLine NINVALIDOPTIONLINE = new VoiceDialogueLine(Voice.NARRATOR, "What are you even trying to do? You're not accomplishing anything.", true);
    private static final VoiceDialogueLine NEXCLUSIVELINE = new VoiceDialogueLine(Voice.NARRATOR, "You have to make a decision.", true);

    // --- CONSTRUCTORS ---

    /**
     * Constructor
     * @param manager the GameManager to link this IOHandler to
     */
    public IOHandler(GameManager manager) {
        this.manager = manager;
        this.input = new Scanner(System.in);
    }

    // --- ACCESSORS ---

    /**
     * Returns the current active Cycle of this IOHandler's manager
     * @return the current active Cycle of this IOHandler's manager
     */
    public Cycle getCurrentCycle() {
        return manager.getCurrentCycle();
    }

    // --- BASIC INPUT ---

    /**
     * Waits until the player presses enter to progress
     */
    public void waitForInput() {
        if (manager.autoAdvance()) {
            System.out.println();
        } else {
            this.input.nextLine();
        }
    }

    /**
     * Recieves input from the player
     * @return the player's input in all lowercase
     */
    public String getInput() {
        System.out.print("> ");
        String in = this.input.nextLine();
        return in.toLowerCase();
    }

    // --- PRINT DIALOGUE ---

    /**
     * Prints a divider
     * @param wait whether to wait for player input after printing the divider
     */
    public void printDivider(boolean wait) {
        if (manager.globalSlowPrint()) {
            DIVIDER.print(false, 1.75);
        } else {
            wrapPrint(DIVIDER);
        }

        if (wait) {
            System.out.print("\n");
        } else {
            this.waitForInput();
        }
    }

    /**
     * Prints a divider, then waits for player input to continue
     */
    public void printDivider() {
        this.printDivider(true);
    }

    /**
     * Prints a DialogueLine
     * @param line the DialogueLine to print
     * @param speedMultiplier the multiplier to apply to the standard speed of printing a line
     */
    public void printDialogueLine(DialogueLine line, double speedMultiplier) {
        if (manager.globalSlowPrint()) {
            line.print(speedMultiplier);
        } else {
            wrapPrint(line);
        }

        if (line.isInterrupted()) {
            System.out.print("\n");
        } else {
            this.waitForInput();
        }
    }

    /**
     * Prints a DialogueLine
     * @param line the DialogueLine to print
     */
    public void printDialogueLine(DialogueLine line) {
        if (manager.globalSlowPrint()) {
            line.print();
        } else {
            wrapPrint(line);
        }

        if (line.isInterrupted()) {
            System.out.print("\n");
        } else {
            this.waitForInput();
        }
    }

    /**
     * Prints a given String as a DialogueLine
     * @param line the dialogue line to print as a String
     * @param isInterrupted whether to go straight into printing the next line or wait for player input after printing this line
     */
    public void printDialogueLine(String line, boolean isInterrupted) {
        DialogueLine lineDialogue = new DialogueLine(line, isInterrupted);
        this.printDialogueLine(lineDialogue);
    }

    /**
     * Prints a given String as a DialogueLine
     * @param line the dialogue line to print as a String
     */
    public void printDialogueLine(String line) {
        this.printDialogueLine(line, false);
    }

    // --- OPTIONS HANDLING ---
    
    /**
     * Prints an OptionsMenu, then allows the player to choose an Option from the menu or enter a command (if the OptionsMenu is not exclusive)
     * @param options the OptionsMenu to offer to the player
     * @param proceedOverride whether to override default behavior and treat "proceed" as its own unique command
     * @return the ID of the chosen Option or the outcome of the entered command
     */
    public String promptOptionsMenu(OptionsMenu options, boolean proceedOverride) {
        Cycle cycle = manager.getCurrentCycle();
        
        System.out.println();
        wrapPrintln(options);
        return this.parseOptionChoice(cycle, options, new DialogueLine(), true);
    }
    
    /**
     * Prints an OptionsMenu, then allows the player to choose an Option from the menu or enter a command (if the OptionsMenu is not exclusive)
     * @param options the OptionsMenu to offer to the player
     * @param exclusiveOverride a DialogueLine to print instead of the default line if the OptionsMenu is exclusive
     * @return the ID of the chosen Option or the outcome of the entered command
     */
    public String promptOptionsMenu(OptionsMenu options, DialogueLine exclusiveOverride) {
        Cycle cycle = manager.getCurrentCycle();
        
        System.out.println();
        wrapPrintln(options);
        return this.parseOptionChoice(cycle, options, exclusiveOverride, false);
    }

    /**
     * Prints an OptionsMenu, then allows the player to choose an Option from the menu or enter a command (if the OptionsMenu is not exclusive)
     * @param options the OptionsMenu to offer to the player
     * @param exclusiveOverride a String to print as a DialogueLine instead of the default line if the OptionsMenu is exclusive
     * @return the ID of the chosen Option or the outcome of the entered command
     */
    public String promptOptionsMenu(OptionsMenu options, String exclusiveOverride) {
        return this.promptOptionsMenu(options, new DialogueLine(exclusiveOverride, true));
    }

    /**
     * Prints an OptionsMenu, then allows the player to choose an Option from the menu or enter a command (if the OptionsMenu is not exclusive)
     * @param options the OptionsMenu to offer to the player
     * @return the ID of the chosen Option or the outcome of the entered command
     */
    public String promptOptionsMenu(OptionsMenu options) {
        return this.promptOptionsMenu(options, "");
    }

    /**
     * Allows the player to choose an Option from a given OptionsMenu or enter a command (if the OptionsMenu is not exclusive)
     * @param cycle the current active Cycle, if there is one
     * @param options the OptionsMenu to offer to the player
     * @param exclusiveOverride a DialogueLine to print instead of the default line if the OptionsMenu is exclusive
     * @param proceedOverride whether to override default behavior and treat "proceed" as its own unique command
     * @return the ID of the chosen Option or the outcome of the entered command
     */
    private String parseOptionChoice(Cycle cycle, OptionsMenu options, DialogueLine exclusiveOverride, boolean proceedOverride) {
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
                    this.printDialogueLine(NINVALIDOPTIONLINE);
                }

                return this.parseOptionChoice(cycle, options, exclusiveOverride, proceedOverride);
            }
        } else {
            boolean isTrueExclusive = manager.trueExclusiveMenu();

            if (options.isExclusive() || isTrueExclusive) {
                if (isTrueExclusive) {
                    try {
                        outcome = this.parseCommand(cycle, in, false);
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
                            this.printDialogueLine("[You have no other choice.]", true);
                        } else if (!cycle.hasVoice(Voice.NARRATOR)) {
                            this.printDialogueLine("[You have no other choice.]", true);
                        } else {
                            this.printDialogueLine(NEXCLUSIVELINE);
                        }
                    } else {
                        this.printDialogueLine(exclusiveOverride);
                    }
                }

                return this.parseOptionChoice(cycle, options, exclusiveOverride, proceedOverride);
            } else {
                try {
                    outcome = this.parseCommand(cycle, in, false);
                    return (outcome.equals("cMeta")) ? this.parseOptionChoice(cycle, options, exclusiveOverride, proceedOverride) : outcome;
                } catch (Exception e) {
                    if (cycle == null) {
                        this.printDialogueLine("[That is not a valid command.]", true);
                    } else if (!cycle.hasVoice(Voice.NARRATOR)) {
                        this.printDialogueLine("[That is not a valid command.]", true);
                    } else {
                        this.printDialogueLine(new VoiceDialogueLine(Voice.NARRATOR, "What are you even trying to do? You're not accomplishing anything.", true));
                    }

                    // Invalid command; re-input, do not show options again
                    return this.parseOptionChoice(cycle, options, exclusiveOverride, proceedOverride);
                }
            }
        }
    }

    // --- COMMAND HANDLING ---

    /**
     * Prints a given DialogueLine, then has the player input a command in response
     * @param prompt the DialogueLine to prompt the player with
     * @return the outcome of the entered command
     */
    public String promptCommand(DialogueLine prompt) {
        Cycle cycle = manager.getCurrentCycle();

        prompt.print();
        String outcome = null;

        while (outcome == null) {
            try {
                outcome = this.parseCommand(this.getInput());
            } catch (Exception e) {
                if (e.getLocalizedMessage().equals("Invalid command")) {
                    if (cycle == null) {
                        this.printDialogueLine("[That is not a valid command.]", true);
                    } else if (!cycle.hasVoice(Voice.NARRATOR)) {
                        this.printDialogueLine("[That is not a valid command.]", true);
                    } else {
                        this.printDialogueLine(new VoiceDialogueLine(Voice.NARRATOR, "What are you even trying to do? You're not accomplishing anything.", true));
                    }
                }

                // Invalid command or argument; re-input
            }
        }

        return outcome;
    }

    /**
     * Prints a given String as a DialogueLine, then has the player input a command in response
     * @param prompt the String to prompt the player with
     * @return the outcome of the entered command
     */
    public String promptCommand(String prompt) {
        return this.promptCommand(new DialogueLine(prompt));
    }

    /**
     * Parses a given String as a command and returns the outcome
     * @param cycle the current active Cycle, if there is one
     * @param playerInput the player's input
     * @param proceedOverride whether to override default behavior and treat "proceed" as its own unique command
     * @return a String representing the outcome of the player's command: "cMeta" if the command is a meta command (HELP, SHOW, or TOGGLE) or the ID-String of the command's "outcome" (accounting for whether the command is currently accessible)
     * @throws Exception if the prefix or arguments of playerInput are invalid
     */
    private String parseCommand(Cycle cycle, String playerInput, boolean proceedOverride) throws Exception {
        String[] split = playerInput.split(" ", 2);
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
                manager.help(argument);
                commandOutcome += "Meta";
                break;
            case SHOW:
                cycle.show(argument);
                commandOutcome += "Meta";
                break;
            case SETTINGS:
                manager.settings();
                commandOutcome += "Meta";
            case TOGGLE:
                manager.toggle(argument);
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
                if (proceedOverride) {
                    commandOutcome += "Proceed";
                } else {
                    commandOutcome += cycle.proceed(argument);
                }
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

           - cProceed (only if proceedOverride)

           - cApproachMirror
           - cApproachAtMirrorFail (you're already at the mirror)
           - cApproachMirrorFail
           - cApproachHer
           - cApproachHerFail

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

            case "cProceed":

            case "cApproachMirror":
            case "cApproachHer":

            case "cSlayPrincess":
            case "cSlaySelf":

            case "cTake":
            case "cDrop":
            case "cThrow":

            case "cGoFail":
            case "cEnterFail":
            case "cLeaveFail":

            case "cApproachAtMirrorFail":
            case "cApproachMirrorFail":
            case "cApproachHerFail":
                
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

    /**
     * Parses a given String as a command and returns the outcome
     * @param playerInput the player's input
     * @return a String representing the outcome of the player's command: "cMeta" if the command is a meta command (HELP, SHOW, or TOGGLE) or the ID-String of the command's "outcome" (accounting for whether the command is currently accessible)
     * @throws Exception if the prefix or arguments of playerInput are invalid
     */
    private String parseCommand(String playerInput) throws Exception {
        return this.parseCommand(manager.getCurrentCycle(), playerInput, false);
    }

    // --- YES/NO HANDLING ---

    /**
     * Prints a given DialogueLine, then has the player input YES/Y or NO/N in response
     * @param prompt the DialogueLine to prompt the player with
     * @return true if the player responds with YES/Y, false if the player responds with NO/N
     */
    public boolean promptYesNo(DialogueLine prompt) {
        prompt.print();
        return this.parseYesNo();
    }

    /**
     * Prints a given String as a DialogueLine, then has the player input YES/Y or NO/N in response
     * @param prompt the DialogueLine to prompt the player with
     * @param slowPrint whether to print the prompt slowly or instantly print it
     * @return true if the player responds with YES/Y, false if the player responds with NO/N
     */
    public boolean promptYesNo(String prompt, boolean slowPrint) {
        if (slowPrint) {
            return this.promptYesNo(new DialogueLine(prompt));
        } else {
            wrapPrint(prompt);
            return this.parseYesNo(slowPrint);
        }
    }

    /**
     * Prints a given String as a DialogueLine, then has the player input YES/Y or NO/N in response
     * @param prompt the DialogueLine to prompt the player with
     * @return true if the player responds with YES/Y, false if the player responds with NO/N
     */
    public boolean promptYesNo(String prompt) {
        return this.promptYesNo(prompt, true);
    }

    /**
     * Retrieves and parses the player's input as an answer to a yes/no question
     * @param slowPrint whether to print the prompt slowly or instantly print it
     * @return true if the player responds with YES/Y, false if the player responds with NO/N
     */
    private boolean parseYesNo(boolean slowPrint) {
        System.out.print("\n");
        String in = this.getInput();

        switch (in) {
            case "y":
            case "yes": return true;

            case "n":
            case "no": return false;

            default: return this.promptYesNo("Please answer \"yes\" or \"no\".", slowPrint);
        }
    }

    /**
     * Retrieves and parses the player's input as an answer to a yes/no question
     * @return true if the player responds with YES/Y, false if the player responds with NO/N
     */
    private boolean parseYesNo() {
        return this.parseYesNo(true);
    }

    /**
     * Closes the Scanner being used for input
     */
    @Override
    public void close() {
        this.input.close();
    }

    // --- WRAPAROUND MANAGEMENT ---

    /**
     * Returns a given String with line breaks inserted such that it will only wrap around to a new line at word boundaries, not in the middle of words
     * @param s the String to modify
     * @return the given String with line breaks inserted such that it will only wrap around to a new line at word boundaries, not in the middle of words
     */
    public static String wordWrap(String s) {
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

    /**
     * Returns the String representation of a given DialogueLine with line breaks inserted such that it will only wrap around to a new line at word boundaries, not in the middle of words
     * @param line the DialogueLine to modify
     * @return the String representation of a given DialogueLine with line breaks inserted such that it will only wrap around to a new line at word boundaries, not in the middle of words
     */
    public static String wordWrap(DialogueLine line) {
        return wordWrap(line.toString());
    }

    /**
     * Returns the String representation of a given OptionsMenu with line breaks inserted such that it will only wrap around to a new line at word boundaries, not in the middle of words
     * @param menu the OptionsMenu to modify
     * @return the String representation of a given OptionsMenu with line breaks inserted such that it will only wrap around to a new line at word boundaries, not in the middle of words
     */
    public static String wordWrap(OptionsMenu menu) {
        return wordWrap(menu.toString());
    }

    /**
     * Instantly prints a given String, inserting line breaks such that it only wraps around to a new line at word boundaries, not in the middle of words
     * @param s the String to print
     */
    public static void wrapPrint(String s) {
        System.out.print(wordWrap(s));
    }

    /**
     * Instantly prints a given DialogueLine, inserting line breaks such that it only wraps around to a new line at word boundaries, not in the middle of words
     * @param line the DialogueLine to print
     */
    public static void wrapPrint(DialogueLine line) {
        System.out.print(wordWrap(line));
    }

    /**
     * Instantly prints a given OptionsMenu, inserting line breaks such that it only wraps around to a new line at word boundaries, not in the middle of words
     * @param menu the OptionsMenu to print
     */
    public static void wrapPrint(OptionsMenu menu) {
        System.out.print(wordWrap(menu));
    }

    /**
     * Instantly prints a given String, inserting line breaks such that it only wraps around to a new line at word boundaries, not in the middle of words, then terminates the line
     * @param s the String to print
     */
    public static void wrapPrintln(String s) {
        System.out.println(wordWrap(s));
    }

    /**
     * Instantly prints a given DialogueLine, inserting line breaks such that it only wraps around to a new line at word boundaries, not in the middle of words, then terminates the line
     * @param line the DialogueLine to print
     */
    public static void wrapPrintln(DialogueLine line) {
        System.out.println(wordWrap(line));
    }

    /**
     * Instantly prints a given OptionsMenu, inserting line breaks such that it only wraps around to a new line at word boundaries, not in the middle of words, then terminates the line
     * @param menu the OptionsMenu to print
     */
    public static void wrapPrintln(OptionsMenu menu) {
        System.out.println(wordWrap(menu));
    }

}
