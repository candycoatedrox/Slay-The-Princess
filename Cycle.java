import java.util.HashMap;

public abstract class Cycle {
    protected final GameManager manager;
    protected final IOHandler parser;

    protected Chapter activeChapter;
    protected HashMap<Voice, Boolean> currentVoices;
    protected GameLocation currentLocation = GameLocation.PATH;

    // Utility variables for option menus
    protected OptionsMenu activeMenu;
    protected boolean trueExclusiveMenu = false; // Only used during show() menu
    protected boolean repeatActiveMenu = false;
    protected String activeOutcome;
    protected boolean reverseDirection = false;
    protected int timer = -1; // -1 indicates timer is inactive

    protected boolean hasBlade = false;

    // Utility variables for checking command availability & default responses
    protected boolean withPrincess = false;
    protected boolean knowsBlade = false; // The Narrator knows you know about the blade
    protected boolean withBlade = false; // Determines whether TAKE BLADE works
    protected boolean mirrorPresent = false;
    protected boolean canSlayPrincess = false;
    protected boolean canSlaySelf = false;
    protected boolean canDropBlade = false;
    protected boolean canThrowBlade = false;
    protected boolean goodEndingAttempted = false;

    // Variables that are used in a lot of chapters
    protected String jointSource;
    protected boolean bladeReverse = false; // Used in any chapter the Contrarian is in
    protected boolean threwBlade = false;

    // Variables that persist between chapters
    protected boolean isHarsh = false; // Used in Chapter 1, Spectre, Princess and the Dragon, Nightmare

    // --- CONSTRUCTOR ---

    /**
     * Constructor
     * @param manager the GameManager to link this Cycle to
     * @param parser the IOHandler to link this Cycle to
     */
    public Cycle(GameManager manager, IOHandler parser) {
        this.manager = manager;
        this.parser = parser;
    }

    // --- ACCESSORS & MANIPULATORS ---

    /**
     * Checks whether a given Voice is currently present
     * @param v the Voice to check for
     * @return true if v is currently present; false otherwise
     */
    public boolean hasVoice(Voice v) {
        return this.currentVoices.get(v);
    }

    /**
     * Adds a given Voice to the list of active Voices
     * @param v the Voice to add
     */
    protected void addVoice(Voice v) {
        this.currentVoices.put(v, true);
    }

    /**
     * Removes all Voices from the list of active Voices
     */
    protected void clearVoices() {
        for (Voice v : Voice.values()) {
            this.currentVoices.put(v, false);
        }
    }

    /**
     * Removes all Voices except the Narrator from the list of active Voices
     */
    protected void clearTrueVoices() {
        for (Voice v : Voice.values()) {
            if (v != Voice.NARRATOR) {
                this.currentVoices.put(v, false);
            }
        }
    }

    /**
     * Accessor for trueExclusiveMenu
     * @return whether the active menu is truly exclusive (i.e. not even meta commands are available until an option is selected)
     */
    public boolean trueExclusiveMenu() {
        return this.trueExclusiveMenu;
    }

    // --- TIMER ---

    /**
     * Starts a timer that lasts n turns
     * @param n the number of turns the timer will last for
     */
    protected void startTimer(int n) {
        this.timer = n;
    }

    /**
     * Resets the timer back to an inactive state
     */
    protected void cancelTimer() {
        this.timer = -1;
    }

    /**
     * Checks whether a timer is active
     * @return
     */
    protected boolean timerActive() {
        return this.timer != -1;
    }

    /**
     * Decreases the number of turns left on the timer by 1
     */
    protected void decrementTimer() {
        this.timer -= 1;
    }

    // --- COMMANDS ---

    /**
     * Shows content warnings 
     * @param arguments
     */
    public void show(String arguments) {
        /*
        This one is highly complex because it technically has 3 arguments instead of 1, and ALL of them are optional.

        Syntax essentially boils down to:
            show [warnings/...] [general/by chapter/current/...] [warnings/...]"
            (where "..." indicates other valid arguments that are parsed the same way)
        
        If the type argument [general/by chapter/current/...] is present, the program immediately shows the player the content warnings they chose to view. If not, the player can choose which content warnings to view from a menu.

        In either case, the program passes the type argument (either given or chosen) to showGivenContentWarnings().

        Both instances of [warnings/...] are more or less there to make using the command more intuitive for the player, and if a valid argument is found there, it is ignored and does not affect the functionality of the command.

        Here are some example variations of the command that a player might intuitively try, all of which function as expected thanks to this "argument wrapping" method:
          - show --> Lets player choose content warning types from a menu
          - show warnings --> Lets player choose content warning types from a menu
          - show all --> Shows general content warnings
          - show warnings by chapter --> Shows content warnings by chapter
          - show current warnings --> Shows content warnings for the current chapter

        A side effect of this method is that an input such as "show warnings all warnings" or "show warnings warnings" would be parsed as perfectly valid. I could technically code in an exception so they would be parsed as invalid, but it's not really a problem in any way. I just think it's a silly little side effect :)
        */

        String args = arguments;

        // Trim off leading "warnings" argument
        if (arguments.startsWith("warnings") || arguments.startsWith("content warnings") || arguments.startsWith("trigger warnings")) {
            int firstArgIndex = arguments.indexOf("warnings");

            if (firstArgIndex == -1) {
                args = "";
            } else {
                try {
                    args = arguments.substring(firstArgIndex + 8);
                } catch (IndexOutOfBoundsException e) {
                    args = "";
                }
            }
        } else if (arguments.startsWith("cws") || arguments.startsWith("tws")) {
            int firstArgIndex = arguments.indexOf("ws");
            
            if (firstArgIndex == -1) {
                args = "";
            } else {
                try {
                    args = arguments.substring(firstArgIndex + 2);
                } catch (IndexOutOfBoundsException e) {
                    args = "";
                }
            }
        }

        // Trim off trailing "warnings" argument
        if (args.endsWith("content warnings") || args.endsWith("trigger warnings")) {
            args = args.substring(0, args.length() - 16);
        } else if (args.endsWith("warnings")) {
            args = args.substring(0, args.length() - 8);
        } else if (args.endsWith("tws") || args.endsWith("cws")) {
            args = args.substring(0, args.length() - 3);
        }

        // Trim off any trailing spaces
        args = args.trim();

        // Now parse remaining argument
        if (args.equals("")) {
            this.showWarningsMenu();
        } else if (Command.SHOW.argumentIsValid(args)) {
            this.showContentWarnings(args);
        } else {
            manager.showCommandHelp(Command.SHOW);
        }
    }

    protected abstract void showWarningsMenu();

    /**
     * Shows general content warnings, content warnings by chapter, or content warnings for the current chapter
     * @param argument the set of content warnings to view
     */
    protected void showContentWarnings(String argument) {
        switch (argument) {
            case "general":
            case "generic":
            case "all":
            case "full":
            case "game":
            case "full game":
            case "full-game":
                manager.showGeneralWarnings();
                break;

            case "by chapter":
            case "by-chapter":
            case "chapter by chapter":
            case "chapter-by-chapter":
            case "chapters":
            case "all chapters":
                manager.showByChapterWarnings();
                break;

            case "current":
            case "active":
            case "chapter":
            case "current chapter":
            case "active chapter":
            case "route":
            case "current route":
            case "active route":
                parser.printDialogueLine("[The current chapter has no content warnings to display.]");
                break;

            default: this.showWarningsMenu();
        }
    }

    /**
     * Attempts to move the player in a given direction
     * @param argument the direction to move the player in
     * @return "cFail" if argument is invalid; "cGo[Location]" if there is a valid location in the given direction; "cGoFail" otherwise
     */
    public String go(String argument) {
        return this.go(argument, false);
    }
    protected abstract String go(String argument, boolean secondPrompt);

    public abstract String enter(String argument);
    public abstract String leave(String argument);

    /**
     * Attempts to move the player forward
     * @param argument the argument given by the player (should be an empty String)
     * @return "cFail" if argument is invalid; "cGo[Location]" if there is a valid location forward from the player; "cGoFail" otherwise
     */
    public String proceed(String argument) {
        switch (argument) {
            case "": return this.go("forward");
            default:
                manager.showCommandHelp(Command.PROCEED);
                return "Fail";
        }
    }

    /**
     * Attempts to move the player backward
     * @param argument the argument given by the player (should be "around", "back", or an empty String)
     * @return "cFail" if argument is invalid; "cGo[Location]" if there is a valid location backward from the player; "cGoFail" otherwise
     */
    public String turn(String argument) {
        switch (argument) {
            case "":
            case "around":
            case "back": return this.go("back");

            default:
                manager.showCommandHelp(Command.TURN);
                return "Fail";
        }
    }

    public abstract String approach(String argument);

    /**
     * Attempts to let the player slay either the Princess or themselves
     * @param argument the target to slay
     * @return "cFail" if argument is invalid; "cSlayNoPrincessFail" if attempting to slay the Princess when she is not present; "cSlayPrincessNoBladeFail" if attempting to slay the Princess without the blade; "cSlayPrincessFail" if the player cannot slay the Princess  right now; "cSlayPrincess" if otherwise attempting to slay the Princess; "cSlaySelfNoBladeFail" if attempting to slay themselves without the blade; "cSlaySelfFail" if the player cannot slay themselves right now; "cSlaySelf" if otherwise attempting to slay themselves
     */
    public String slay(String argument) {
        return this.slay(argument, false);
    }
    protected abstract String slay(String argument, boolean secondPrompt);

    /**
     * Attempts to let the player take the blade
     * @param argument the argument given by the player (should be "the blade", "blade", or "pristine blade")
     * @return "cFail" if argument is invalid; "cTakeHasBladeFail" if the player already has the blade; "cTakeFail" if the player cannot take the blade right now; "cTake" otherwise
     */
    public String take(String argument) {
        return this.take(argument, false);
    }

    /**
     * Attempts to let the player take the blade
     * @param argument the argument given by the player (should be "the blade", "blade", or "pristine blade")
     * @param secondPrompt whether the player has already been given a chance to re-enter a valid argument
     * @return "cFail" if argument is invalid; "cTakeHasBladeFail" if the player already has the blade; "cTakeFail" if the player cannot take the blade right now; "cTake" otherwise
     */
    protected String take(String argument, boolean secondPrompt) {
        switch (argument) {
            case "the blade":
            case "blade":
            case "pristine blade":
                if (this.hasBlade) {
                    return "TakeHasBladeFail";
                } else if (!this.withBlade) {
                    return "TakeFail";
                } else {
                    return "Take";
                }
            
            case "":
                if (secondPrompt) {
                    manager.showCommandHelp(Command.TAKE);
                    return "Fail";
                } else {
                    parser.printDialogueLine("What do you want to take?", true);
                    return this.take(parser.getInput(), true);
                }

            default:
                manager.showCommandHelp(Command.TAKE);
                return "Fail";
        }
    }

    /**
     * Attempts to let the player drop the blade
     * @param argument the argument given by the player (should be "the blade", "blade", or "pristine blade")
     * @return "cFail" if argument is invalid; "cDropHasBladeFail" if the player already has the blade; "cDropFail" if the player cannot take the blade right now; "cDrop" otherwise
     */
    public String drop(String argument) {
        return this.drop(argument, false);
    }

    /**
     * Attempts to let the player drop the blade
     * @param argument the argument given by the player (should be "the blade", "blade", or "pristine blade")
     * @param secondPrompt whether the player has already been given a chance to re-enter a valid argument
     * @return "cFail" if argument is invalid; "cDropNoBladeFail" if the player does not have the blade; "cDropFail" if the player cannot drop the blade right now; "cDrop" otherwise
     */
    protected String drop(String argument, boolean secondPrompt) {
        switch (argument) {
            case "the blade":
            case "blade":
            case "pristine blade":
                if (!this.hasBlade) {
                    return "DropNoBladeFail";
                } else if (!this.canDropBlade) {
                    return "DropFail";
                } else {
                    return "Drop";
                }
            
            case "":
                if (secondPrompt) {
                    manager.showCommandHelp(Command.DROP);
                    return "Fail";
                } else {
                    parser.printDialogueLine("What do you want to drop?", true);
                    return this.drop(parser.getInput(), true);
                }

            default:
                manager.showCommandHelp(Command.DROP);
                return "Fail";
        }
    }

    /**
     * Attempts to let the player throw the blade out the window
     * @param argument the argument given by the player (should be "the blade", "blade", or "pristine blade")
     * @return "cFail" if argument is invalid; "cThrowNoBladeFail" if the player does not have the blade; "cThrowFail" if the player cannot drop the blade right now; "cThrow" otherwise
     */
    public String throwBlade(String argument) {
        return this.throwBlade(argument, false);
    }

    /**
     * Attempts to let the player throw the blade out the window
     * @param argument the argument given by the player (should be "the blade", "blade", or "pristine blade")
     * @param secondPrompt whether the player has already been given a chance to re-enter a valid argument
     * @return "cFail" if argument is invalid; "cThrowNoBladeFail" if the player does not have the blade; "cThrowFail" if the player cannot drop the blade right now; "cThrow" otherwise
     */
    protected String throwBlade(String argument, boolean secondPrompt) {
        switch (argument) {
            case "the blade":
            case "blade":
            case "pristine blade":
                if (!this.hasBlade) {
                    return "ThrowNoBladeFail";
                } else if (!this.canThrowBlade) {
                    return "ThrowFail";
                } else {
                    return "Throw";
                }
            
            case "":
                if (secondPrompt) {
                    manager.showCommandHelp(Command.THROW);
                    return "Fail";
                } else {
                    parser.printDialogueLine("What do you want to throw?", true);
                    return this.throwBlade(parser.getInput(), true);
                }

            default:
                manager.showCommandHelp(Command.THROW);
                return "Fail";
        }
    }

    /**
     * Prints a generic response to a command failing or being unavailable
     * @param outcome the String representation of the outcome of the attempted command
     */
    protected void giveDefaultFailResponse(String outcome) {
        switch (outcome) {
            case "cGoFail":
                parser.printDialogueLine(new DialogueLine("You cannot go that way now."));                
                break;

            case "cEnterFail":
                parser.printDialogueLine(new DialogueLine("You cannot enter there from where you are now."));
                break;
                
            case "cLeaveFail":
                parser.printDialogueLine(new DialogueLine("You cannot leave a place you are not in."));
                break;

            case "cApproachAtMirrorFail":
                parser.printDialogueLine(new DialogueLine("You are already at the mirror."));
                break;

            case "cApproachFail":
                parser.printDialogueLine(new DialogueLine("There is no mirror."));
                break;
                

            case "cSlayNoPrincessFail":
                parser.printDialogueLine(new DialogueLine("The Princess is not here."));
                break;

            case "cSlayPrincessNoBladeFail":
                parser.printDialogueLine(new DialogueLine("You do not have a weapon."));
                break;

            case "cSlayPrincessFail":
                parser.printDialogueLine(new DialogueLine("You cannot attempt to slay her now."));
                break;

            case "cSlaySelfNoBladeFail":
                parser.printDialogueLine(new DialogueLine("You do not have the blade."));
                break;

            case "cSlaySelfFail":
                parser.printDialogueLine(new DialogueLine("You cannot slay yourself now."));
                break;
                

            case "cTakeHasBladeFail":
                parser.printDialogueLine(new DialogueLine("The pristine blade is already in your possession."));
                break;
            
            case "cTakeFail":
                parser.printDialogueLine(new DialogueLine("The pristine blade is not here."));
                break;

            case "cDropNoBladeFail":
                parser.printDialogueLine(new DialogueLine("You do not have the blade."));
                break;

            case "cDropFail":
                parser.printDialogueLine(new DialogueLine("You cannot drop the blade now."));
                break;

            case "cThrowNoBladeFail":
                parser.printDialogueLine(new DialogueLine("You do not have the blade."));
                break;
                
            case "cThrowFail":
                parser.printDialogueLine(new DialogueLine("You cannot throw the blade now."));
                break;

            default:
                parser.printDialogueLine("You have no other options.");
        }
    }

    // --- CYCLE MANAGEMENT ---

    public abstract ChapterEnding runCycle();
}
