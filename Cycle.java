import java.util.HashMap;

public abstract class Cycle {
    protected final GameManager manager;
    protected final IOHandler parser;

    protected Chapter activeChapter;
    protected Script mainScript;
    protected Script secondaryScript;
    protected HashMap<Voice, Boolean> currentVoices;
    protected GameLocation currentLocation = GameLocation.PATH;

    // Utility variables for option menus
    protected OptionsMenu activeMenu;
    protected boolean repeatActiveMenu = false;
    protected String activeOutcome;
    protected boolean reverseDirection = false;

    protected boolean hasBlade = false;

    // Utility variables for checking command availability & default responses
    protected boolean withPrincess = false;
    protected boolean knowsBlade = false; // The Narrator knows you know about the blade
    protected boolean withBlade = false; // Determines whether TAKE BLADE works
    protected boolean mirrorPresent = false;
    protected boolean canApproachHer = false;
    protected boolean canSlayPrincess = false;
    protected boolean canSlaySelf = false;
    protected boolean canDropBlade = false;
    protected boolean canThrowBlade = false;

    // Variables that are used in a lot of chapters
    protected String source = "";
    protected boolean threwBlade = false; // Used in any chapter the Contrarian is in
    protected boolean sharedLoop = false; // Used in all Chapter 2s
    protected boolean sharedLoopInsist = false; // Used in all Chapter 2s
    protected boolean mirrorComment = false; // Used in all Chapter 2s and 3s
    protected boolean touchedMirror = false; // Used in all Chapter 2s and 3s

    // Variables that persist between chapters
    protected boolean isHarsh = false; // Used in Chapter 1, Spectre, Princess and the Dragon, Nightmare, and the Finale
    protected boolean knowsDestiny = false; // Used in Chapter 1, Adversary, Tower, Fury

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
     * Returns whether the player has not yet claimed their first vessel
     * @return whether the player has not yet claimed their first vessel
     */
    public boolean isFirstVessel() {
        return false;
    }

    /**
     * Checks whether a given Voice is currently present
     * @param v the Voice to check for
     * @return true if v is currently present; false otherwise
     */
    public boolean hasVoice(Voice v) {
        return this.currentVoices.get(v);
    }

    /**
     * Returns the number of Voices the player currently has, excluding the Narrator and the Princess
     * @return the number of Voices the player currently has, excluding the Narrator and the Princess
     */
    protected int nVoices() {
        int n = 0;

        for (Voice v : Voice.TRUEVOICES) {
            if (this.hasVoice(v)) n += 1;
        }

        return n;
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
        for (Voice v : Voice.TRUEVOICES) {
            this.currentVoices.put(v, false);
        }
    }

    /**
     * Accessor for hasBlade
     * @return whether the player currently has the blade
     */
    public boolean hasBlade() {
        return this.hasBlade;
    }

    /**
     * Accessor for source
     * @return the current "source" of the active chapter
     */
    public String getSource() {
        return this.source;
    }

    /**
     * Accessor for threwBlade
     * @return whether or not the player threw the blade out the window
     */
    public boolean threwBlade() {
        return this.threwBlade;
    }

    /**
     * Accessor for sharedLoop
     * @return whether or not the Narrator knows that the player has been here before
     */
    public boolean sharedLoop() {
        return this.sharedLoop;
    }

    /**
     * Accessor for sharedLoopInsist
     * @return whether or not the player insisted that they've been here before in the woods
     */
    public boolean sharedLoopInsist() {
        return this.sharedLoopInsist;
    }

    /**
     * Accessor for mirrorComment
     * @return whether or not the player asked about the mirror in Chapter 2
     */
    public boolean mirrorComment() {
        return this.mirrorComment;
    }

    /**
     * Accessor for touchedMirror
     * @return whether or not the player approached the mirror in Chapter 2
     */
    public boolean touchedMirror() {
        return this.touchedMirror;
    }

    /**
     * Accessor for isHarsh
     * @return whether the Princess is currently hostile in chapters where it varies
     */
    public boolean isHarsh() {
        return this.isHarsh;
    }

    /**
     * Accessor for knowsDestiny
     * @return whether or not the Princess knows she's (allegedly) going to end the world
     */
    public boolean knowsDestiny() {
        return this.sharedLoopInsist;
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

    /**
     * Attempts to move the player in a given direction
     * @param argument the direction to move the player in
     * @param secondPrompt whether the player has already been given a chance to re-enter a valid argument
     * @return "cFail" if argument is invalid; "cGo[Location]" if there is a valid location in the given direction; "cGoFail" otherwise
     */
    protected String go(String argument, boolean secondPrompt) {
        switch (argument) {
            case "forward":
            case "forwards":
            case "f":
                switch (this.currentLocation.getForward(this.reverseDirection)) {
                    case LEAVING: return "GoLeave";
                    case PATH: return "GoPath";
                    case HILL: return "GoHill";
                    case CABIN: return "GoCabin";
                    case STAIRS: return "GoStairs";
                    case BASEMENT: return "GoBasement";
                    case MIRROR: return this.approach("mirror");
                    default: return "GoFail";
                }
            
            case "forwardTRUE":
                switch (this.currentLocation.getForward()) {
                    case LEAVING: return "GoLeave";
                    case HILL: return "GoHill";
                    case CABIN: return "GoCabin";
                    case STAIRS: return "GoStairs";
                    case BASEMENT: return "GoBasement";
                    case MIRROR: return this.approach("mirror");
                    default: return "GoFail";
                }
            
            case "back":
            case "backward":
            case "backwards":
            case "b":
                switch (this.currentLocation.getBackward(this.reverseDirection)) {
                    case LEAVING: return "GoLeave";
                    case PATH: return "GoPath";
                    case HILL: return "GoHill";
                    case CABIN: return "GoCabin";
                    case STAIRS: return "GoStairs";
                    case BASEMENT: return "GoBasement";
                    case MIRROR: return this.approach("mirror");
                    default: return "GoFail";
                }
                
            case "backTRUE":
                switch (this.currentLocation.getBackward()) {
                    case LEAVING: return "GoLeave";
                    case PATH: return "GoPath";
                    case HILL: return "GoHill";
                    case CABIN: return "GoCabin";
                    case STAIRS: return "GoStairs";
                    default: return "GoFail";
                }

            case "inside":
            case "in":
            case "i":
                return (this.currentLocation.canGoInside()) ? this.go("forwardTRUE") : "GoFail";

            case "outside":
            case "out":
            case "o":
                return (this.currentLocation.canGoOutside()) ? this.go("backTRUE") : "GoFail";

            case "down":
            case "d":
                return (this.currentLocation.canGoDown()) ? this.go("forwardTRUE") : "GoFail";

            case "up":
            case "u":
                return (this.currentLocation.canGoUp()) ? this.go("backTRUE") : "GoFail";

            case "":
                if (secondPrompt) {
                    manager.showCommandHelp("go");
                    return "Fail";
                } else {
                    parser.printDialogueLine("Where do you want to go?", true);
                    return this.go(parser.getInput(), true);
                }

            default:
                manager.showCommandHelp("go");
                return "Fail";
        }
    }

    /**
     * Attempts to let the player enter a given location or the nearest appropriate location
     * @param argument the location to enter (should be "cabin", "basement", or an empty String)
     * @return "cFail" if argument is invalid; "cGo[Location]" if there is a valid location the player can enter; "cEnterFail" otherwise
     */
    public String enter(String argument) {
        switch (argument) {
            case "": return this.go("inside");

            case "cabin":
                return (this.currentLocation == GameLocation.HILL) ? "GoCabin" : "EnterFail";

            case "basement":
                switch (this.currentLocation) {
                    case CABIN:
                    case STAIRS: return this.go("forwardTRUE");
                    default: return "EnterFail";
                }

            default:
                manager.showCommandHelp("enter");
                return "Fail";
        }
    }

    /**
     * Attempts to let the player leave the current location
     * @param argument the location to leave (should be "woods", "path", "cabin", "basement", or an empty String)
     * @return "cFail" if argument is invalid; "cGo[Location]" if there is a valid location the player can leave; "cLeaveFail" otherwise
     */
    public String leave(String argument) {
        switch (argument) {
            case "": return this.go("backTRUE");

            case "woods":
            case "path":
                switch (this.currentLocation) {
                    case PATH:
                    case HILL: return "GoLeave";
                    default: return "LeaveFail";
                }

            case "cabin":
                switch (this.currentLocation) {
                    case CABIN:
                    case STAIRS:
                    case BASEMENT: return this.go("backTRUE");
                    default: return "LeaveFail";
                }

            case "basement":
                switch (this.currentLocation) {
                    case STAIRS:
                    case BASEMENT: return this.go("backTRUE");
                    default: return "LeaveFail";
                }

            default:
                manager.showCommandHelp("leave");
                return "Fail";
        }
    }

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

    /**
     * Attempts to let the player approach the mirror or her
     * @param argument the argument given by the player -- the target to approach
     * @param secondPrompt whether the player has already been given a chance to re-enter a valid argument
     * @return "cFail" if argument is invalid; "cApproachAtMirrorFail" if attempting to approach the mirror when the player is already at the mirror; "cApproachMirrorFail" if attempting to approach the mirror when it is not present; "cApproachMirror" if otherwise attempting to approach the mirror; "cApproachHerFail" if attempting to approach her when not in the Spaces Between; "cApproachHer" if otherwise attempting to approach her
     */
    protected String approach(String argument, boolean secondPrompt) {
        switch (argument) {
            case "the mirror":
            case "mirror":
                if (this.currentLocation == GameLocation.MIRROR) {
                    return "ApproachAtMirrorFail";
                } else if (!this.mirrorPresent) {
                    return "ApproachMirrorFail";
                } else {
                    return "ApproachMirror";
                }

            case "her":
            case "the princess":
            case "princess":
            case "hands":
                if (this.canApproachHer) {
                    return "ApproachHer";
                } else {
                    return "ApproachHerFail";
                }
            
            case "":
                if (secondPrompt) {
                    manager.showCommandHelp("approach");
                    return "Fail";
                } else {
                    parser.printDialogueLine("What do you want to approach?", true);
                    return this.approach(parser.getInput(), true);
                }

            default:
                manager.showCommandHelp("approach");
                return "Fail";
        }
    }

    /**
     * Attempts to let the player approach the mirror or her
     * @param argument the argument given by the player -- the target to approach
     * @return "cFail" "cFail" if argument is invalid; "cApproachAtMirrorFail" if attempting to approach the mirror when the player is already at the mirror; "cApproachMirrorFail" if attempting to approach the mirror when it is not present; "cApproachMirror" if otherwise attempting to approach the mirror; "cApproachHerFail" if attempting to approach her when not in the Spaces Between; "cApproachHer" if otherwise attempting to approach her
     */
    public String approach(String argument) {
        return this.approach(argument, false);
    }

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
            case "cMeta":
                break;

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

            case "cApproachMirrorFail":
                parser.printDialogueLine(new DialogueLine("There is no mirror."));
                break;

            case "cApproachHerFail":
                if (this.withPrincess) {
                    parser.printDialogueLine(new DialogueLine("You are already with her."));
                } else {
                    parser.printDialogueLine(new DialogueLine("She is not here."));
                }

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
            
            case "cTake":
                parser.printDialogueLine(new DialogueLine("You cannot take the blade now."));
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
                parser.printDialogueLine("You have no other choice.");
        }
    }

    /**
     * Prints a generic response to a command failing or being unavailable
     */
    protected void giveDefaultFailResponse() {
        parser.printDialogueLine("You have no other choice.");
    }

    /**
     * Prints a line about the Long Quiet beginning to creep closer, used in most endings right before a vessel is claimed
     */
    public void quietCreep() {
        System.out.println();
        parser.printDialogueLine("A textured nothingness begins to creep into the edges of your vision.");
        System.out.println();
    }

    // --- CYCLE MANAGEMENT ---

    public abstract ChapterEnding runCycle();
    
    /**
     * (DEBUG ONLY) Initiates and coordinates a full cycle, starting from a given Chapter ending through the player's conversation with the Shifting Mound
     * @param startFromEnding the ChapterEnding to start from
     * @param harsh the value to set isHarsh to
     * @return the Chapter ending reached by the player
     */
    public ChapterEnding debugRunCycle(ChapterEnding startFromEnding, boolean harsh) {
        return this.runCycle();
    }
}
