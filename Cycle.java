import java.util.ArrayList;
import java.util.HashMap;

public class Cycle {

    // one CYCLE = from beginning of Ch1 to the end of Shifting Mound interlude

    protected final GameManager manager;
    protected final IOHandler parser;

    private boolean isFirstVessel;
    private ArrayList<Chapter> route;
    protected Chapter activeChapter;
    private ChapterEnding prevEnding;
    protected ArrayList<Voice> voicesMet;
    protected HashMap<Voice, Boolean> currentVoices;
    protected GameLocation currentLocation = GameLocation.PATH;

    // Utility variables for option menus
    protected OptionsMenu activeMenu;
    protected boolean trueExclusiveMenu = false; // Only used during show() menu
    protected boolean repeatActiveMenu = false;
    protected String activeOutcome;
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

    // Variables that are used in nearly all chapters
    private boolean mentionedLooping = false; // Used in all Chapter 2s and 3s: does the Narrator know?
    protected boolean bladeReverse = false; // Used in any chapter the Contrarian is in
    protected boolean threwBlade = false;

    // Variables that persist between chapters
    private boolean seenMirror = false;
    private boolean touchedMirror = false; // Used in all Chapter 2s and 3s
    protected boolean isHarsh = false; // Used in Chapter 1, Spectre, and Princess and the Dragon
    private boolean knowsDestiny = false; // Used in Chapter 1, Tower, Fury

    private static final PrincessDialogueLine CANTSTRAY = new PrincessDialogueLine(true, "You have already committed to my completion. You cannot go further astray.");

    // --- CONSTRUCTOR ---

    public Cycle(GameManager manager, IOHandler parser) {
        this.manager = manager;
        this.parser = parser;
        this.isFirstVessel = manager.nClaimedVessels() == 0;
        this.route = new ArrayList<>();

        this.voicesMet = new ArrayList<>();

        this.currentVoices = new HashMap<>();
        for (Voice v : Voice.values()) {
            if (v == Voice.NARRATOR || v == Voice.HERO) {
                this.currentVoices.put(v, true);
            } else {
                this.currentVoices.put(v, false);
            }
        }
    }

    // --- ACCESSORS & MANIPULATORS ---

    public boolean hasVoice(Voice v) {
        return this.currentVoices.get(v);
    }

    protected void addVoice(Voice v) {
        this.currentVoices.put(v, true);
        if (v != Voice.NARRATOR && v != Voice.HERO && v != Voice.PRINCESS) {
            this.voicesMet.add(v);
        }
    }

    protected void clearVoices() {
        for (Voice v : Voice.values()) {
            this.currentVoices.put(v, false);
        }
    }

    protected void clearTrueVoices() {
        for (Voice v : Voice.values()) {
            if (v != Voice.NARRATOR) {
                this.currentVoices.put(v, false);
            }
        }
    }

    public boolean trueExclusiveMenu() {
        return this.trueExclusiveMenu;
    }

    // --- TIMER ---

    protected void startTimer(int n) {
        this.timer = n;
    }

    protected void cancelTimer() {
        this.timer = -1;
    }

    protected boolean timerActive() {
        return this.timer != -1;
    }

    protected void decrementTimer() {
        this.timer -= 1;
    }

    // --- COMMANDS ---

    public String show(String arguments) {
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
            manager.showCommandHelp("show");
        }

        return "Meta";
    }

    protected void showWarningsMenu() {
        OptionsMenu warningsMenu = manager.warningsMenu();
        warningsMenu.setCondition("current", this.activeChapter.hasContentWarnings());
        this.trueExclusiveMenu = true;

        switch (parser.promptOptionsMenu(warningsMenu)) {
            case "general":
                manager.showGeneralWarnings();
                break;
            case "by chapter":
                manager.showByChapterWarnings();
                break;
            case "current":
                this.showThisChapterWarnings();
                break;
            case "cancel":
                break;
        }

        this.trueExclusiveMenu = false;
    }

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
                this.showThisChapterWarnings();
                break;

            default: manager.showCommandHelp("show");
        }
    }

    protected void showThisChapterWarnings() {
        if (this.activeChapter.hasContentWarnings()) {
            manager.showChapterWarnings(this.activeChapter, this.prevEnding);
        } else {
            parser.printDialogueLine("[The current chapter has no content warnings to display.]");
        }
    }

    protected String go(String argument, boolean secondPrompt) {
        switch (argument) {
            case "forward":
            case "forwards":
            case "f":
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
                return (this.currentLocation.canGoInside()) ? this.go("forward") : "GoFail";

            case "outside":
            case "out":
            case "o":
                return (this.currentLocation.canGoOutside()) ? this.go("back") : "GoFail";

            case "down":
            case "d":
                return (this.currentLocation.canGoDown()) ? this.go("forward") : "GoFail";

            case "up":
            case "u":
                return (this.currentLocation.canGoUp()) ? this.go("back") : "GoFail";

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

    public String go(String argument) {
        return this.go(argument, false);
    }

    public String enter(String argument) {
        switch (argument) {
            case "": return this.go("inside");

            case "cabin":
                return (this.currentLocation == GameLocation.HILL) ? this.go("forward") : "EnterFail";

            case "basement":
                switch (this.currentLocation) {
                    case CABIN:
                    case STAIRS: return this.go("forward");
                    default: return "EnterFail";
                }

            default:
                manager.showCommandHelp("enter");
                return "Fail";
        }
    }

    public String leave(String argument) {
        switch (argument) {
            case "": this.go("back");

            case "woods":
            case "path":
                switch (this.currentLocation) {
                    case PATH:
                    case HILL: return this.go("back");
                    default: return "LeaveFail";
                }

            case "cabin":
                return (this.currentLocation == GameLocation.CABIN) ? this.go("back") : "LeaveFail";

            case "basement":
                switch (this.currentLocation) {
                    case STAIRS:
                    case BASEMENT: return this.go("back");
                    default: return "LeaveFail";
                }

            default:
                manager.showCommandHelp("leave");
                return "Fail";
        }
    }

    public String turn(String argument) {
        switch (argument) {
            case "":
            case "around":
            case "back": return this.go("back");

            default:
                manager.showCommandHelp("turn");
                return "Fail";
        }
    }

    protected String approach(String argument, boolean secondPrompt) {
        switch (argument) {
            case "the mirror":
            case "mirror":
                return (this.mirrorPresent) ? "Approach" : "ApproachFail";
            
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

    public String approach(String argument) {
        return this.approach(argument, false);
    }

    protected String slay(String argument, boolean secondPrompt) {
        switch (argument) {
            case "the princess":
            case "princess":
                if (!this.withPrincess) {
                    return "SlayNoPrincessFail";
                } else if (!this.hasBlade) {
                    return "SlayPrincessNoBladeFail";
                } else if (!this.canSlayPrincess) {
                    return "SlayPrincessFail";
                } else {
                    return "SlayPrincess";
                }

            case "yourself":
            case "self":
            case "you":
            case "myself":
            case "me":
            case "ourself":
            case "ourselves":
            case "us":
                if (!this.hasBlade) {
                    return "SlaySelfNoBladeFail";
                } else if (!this.canSlaySelf) {
                    return "SlaySelfFail";
                } else {
                    return "SlaySelf";
                }
            
            case "":
                if (secondPrompt) {
                    manager.showCommandHelp("slay");
                    return "Fail";
                } else {
                    parser.printDialogueLine("Who do you want to slay?", true);
                    return this.slay(parser.getInput(), true);
                }

            default:
                manager.showCommandHelp("slay");
                return "Fail";
        }
    }

    public String slay(String argument) {
        return this.slay(argument, false);
    }

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
                    manager.showCommandHelp("take");
                    return "Fail";
                } else {
                    parser.printDialogueLine("What do you want to take?", true);
                    return this.take(parser.getInput(), true);
                }

            default:
                manager.showCommandHelp("take");
                return "Fail";
        }
    }

    public String take(String argument) {
        return this.take(argument, false);
    }

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
                    manager.showCommandHelp("drop");
                    return "Fail";
                } else {
                    parser.printDialogueLine("What do you want to drop?", true);
                    return this.drop(parser.getInput(), true);
                }

            default:
                manager.showCommandHelp("drop");
                return "Fail";
        }
    }

    public String drop(String argument) {
        return this.drop(argument, false);
    }

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
                    manager.showCommandHelp("throw");
                    return "Fail";
                } else {
                    parser.printDialogueLine("What do you want to throw?", true);
                    return this.throwBlade(parser.getInput(), true);
                }

            default:
                manager.showCommandHelp("throw");
                return "Fail";
        }
    }

    public String throwBlade(String argument) {
        return this.throwBlade(argument, false);
    }

    protected void giveDefaultResponse(String outcome) {
        boolean narratorPresent = this.hasVoice(Voice.NARRATOR);

        switch (outcome) {
            case "cGoFail":
                if (narratorPresent) {
                    parser.printDialogueLine(new VoiceDialogueLine("There's nowhere to go that way."));
                } else {
                    parser.printDialogueLine(new DialogueLine("You cannot go that way now."));
                }
                
                break;

            case "cEnterFail":
                if (narratorPresent) {
                    parser.printDialogueLine(new VoiceDialogueLine("You can't get there from where you are now. Just keep moving forward."));
                } else {
                    parser.printDialogueLine(new DialogueLine("You cannot enter there from where you are now."));
                }
                
                break;
                
            case "cLeaveFail":
                if (narratorPresent) {
                    parser.printDialogueLine(new VoiceDialogueLine("You can't leave a place if you aren't there in the first place."));
                } else {
                    parser.printDialogueLine(new DialogueLine("You cannot leave a place you are not in."));
                }
                
                break;

            case "cApproachFail":
                if (narratorPresent) {
                    parser.printDialogueLine(new VoiceDialogueLine("What are you talking about? There isn't a mirror."));
                    if (this.seenMirror) parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "He's actually right this time. The mirror really isn't here."));
                } else {
                    parser.printDialogueLine(new DialogueLine("There is no mirror."));
                }
                
                break;
                

            case "cSlayNoPrincessFail":
                if (narratorPresent) {
                    parser.printDialogueLine(new VoiceDialogueLine("As much as I appreciate your enthusiasm, the Princess isn't here right now. Save it for when you reach the basement."));
                } else {
                    parser.printDialogueLine(new DialogueLine("The Princess is not here."));
                }
                
                break;

            case "cSlayPrincessNoBladeFail":
                if (narratorPresent) {
                    parser.printDialogueLine(new VoiceDialogueLine("*sigh* Unfortunately, you have no weapon with which to slay her. If only you had the blade, this would be so much easier."));
                } else {
                    parser.printDialogueLine(new DialogueLine("You do not have a weapon."));
                }
                
                break;

            case "cSlayPrincessFail":
                if (narratorPresent) {
                    parser.printDialogueLine(new VoiceDialogueLine("XXXXXX"));
                } else {
                    parser.printDialogueLine(new DialogueLine("You cannot attempt to slay her now."));
                }
                
                break;

            case "cSlaySelfNoBladeFail":
                if (narratorPresent) {
                    parser.printDialogueLine(new VoiceDialogueLine("I don't know why on earth you would even consider doing that, but conveniently, you don't have a weapon. You can't."));
                } else {
                    parser.printDialogueLine(new DialogueLine("You do not have the blade."));
                }
                
                break;

            case "cSlaySelfFail":
                if (narratorPresent) {
                    parser.printDialogueLine(new VoiceDialogueLine("Are you insane?! Absolutely not."));
                    if (this.hasVoice(Voice.HERO)) parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "He's right. There's no reason to make such a rash decision."));
                } else {
                    parser.printDialogueLine(new DialogueLine("You cannot slay yourself now."));
                }
                
                break;
                

            case "cTakeHasBladeFail":
                if (narratorPresent) {
                    parser.printDialogueLine(new VoiceDialogueLine("You already have the blade, remember?"));
                } else {
                    parser.printDialogueLine(new DialogueLine("The pristine blade is already in your possession."));
                }

                break;
            
            case "cTakeFail":
                if (narratorPresent) {
                    if (this.knowsBlade) {
                        parser.printDialogueLine(new VoiceDialogueLine("As much as I appreciate your enthusiasm, the blade isn't here right now."));
                    } else {
                        parser.printDialogueLine(new VoiceDialogueLine("As much as I appreciate your enthusiasm, there isn't a blade here."));
                    }
                } else {
                    parser.printDialogueLine(new DialogueLine("The pristine blade is not here."));
                }
                
                break;

            case "cDropNoBladeFail":
                if (narratorPresent) {
                    if (this.knowsBlade) {
                        parser.printDialogueLine(new VoiceDialogueLine("I can't fathom why you would want to drop your implement, but you don't even have it right now."));
                    } else {
                        parser.printDialogueLine(new VoiceDialogueLine("I can't fathom why you would want to drop your weapon, but you don't even have one right now."));
                    }
                } else {
                    parser.printDialogueLine(new DialogueLine("You do not have the blade."));
                }
                
                break;

            case "cDropFail":
                if (narratorPresent) {
                    parser.printDialogueLine(new VoiceDialogueLine("You can't drop the blade now. You're here for a reason. Finish the job."));
                } else {
                    parser.printDialogueLine(new DialogueLine("You cannot drop the blade now."));
                }
                
                break;

            case "cThrowNoBladeFail":
                if (narratorPresent) {
                    if (this.knowsBlade) {
                        parser.printDialogueLine(new VoiceDialogueLine("I can't fathom why you would want to throw your implement away, but conveniently, you don't even have it right now."));
                    } else {
                        parser.printDialogueLine(new VoiceDialogueLine("I can't fathom why you would want to throw your weapon away, but conveniently, you don't even have one right now."));
                    }
                } else {
                    parser.printDialogueLine(new DialogueLine("You do not have the blade."));
                }
                
                break;
                
            case "cThrowFail":
                if (narratorPresent) {
                    parser.printDialogueLine(new VoiceDialogueLine("Are you insane?! Absolutely not."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Why would we even do that? That seems... silly."));
                } else {
                    parser.printDialogueLine(new DialogueLine("You cannot throw the blade now."));
                }

                break;
        }
    }

    // --- CYCLE MANAGEMENT ---

    public ChapterEnding runCycle() {
        Chapter nextChapter;
        this.prevEnding = ChapterEnding.NEWCYCLE;

        while (!this.prevEnding.isFinal()) {
            nextChapter = this.prevEnding.getNextChapter();

            if (this.prevEnding != ChapterEnding.NEWCYCLE) {
                if (nextChapter == Chapter.CLARITY) {
                    for (Voice v : Voice.values()) {
                        if (v != Voice.PRINCESS) {
                            this.addVoice(v);
                        }
                    }
                } else if (nextChapter == Chapter.HAPPY) {
                    this.currentVoices.put(Voice.SMITTEN, false);
                }

                if (this.prevEnding.getNewVoice() != null) {
                    this.addVoice(this.prevEnding.getNewVoice());
                    if (this.prevEnding.getNewVoice2() != null) {
                        this.addVoice(this.prevEnding.getNewVoice2());
                    }
                }
                
                switch (nextChapter) {
                    case CAGE:
                        this.hasBlade = true;
                        this.withPrincess = false;
                        this.knowsBlade = true;
                        this.currentLocation = GameLocation.PATH;
                        break;
                    case MUTUALLYASSURED:
                        this.hasBlade = true;
                        this.withPrincess = false;
                        this.knowsBlade = true;
                        this.currentLocation = GameLocation.CABIN;
                        break;
                    case EMPTYCUP:
                        this.hasBlade = false;
                        this.withPrincess = false;
                        this.knowsBlade = false;
                        this.currentLocation = GameLocation.CABIN;
                        break;
                    case HAPPY:
                        this.hasBlade = false;
                        this.withPrincess = false;
                        this.knowsBlade = false;
                        this.currentLocation = GameLocation.CABIN;
                        break;
                    case DRAGON:
                        this.hasBlade = false;
                        this.withPrincess = true;
                        this.knowsBlade = false;
                        this.currentLocation = GameLocation.BASEMENT;
                        break;
                    default:
                        this.hasBlade = false;
                        this.withPrincess = false;
                        this.knowsBlade = false;
                        this.currentLocation = GameLocation.PATH;
                        break;
                }

                this.cancelTimer();

                this.threwBlade = false;
                this.mentionedLooping = false;
                this.bladeReverse = false;

                this.repeatActiveMenu = false;
                this.withBlade = false;
                this.mirrorPresent = false;
                this.canSlayPrincess = false;
                this.canSlaySelf = false;
                this.canDropBlade = false;
            }

            this.prevEnding = this.runChapter(nextChapter);
        }

        this.mirrorSequence();

        manager.updateVisitedChapters(this.route);
        manager.updateVoicesMet(this.voicesMet);
        return this.prevEnding;
    }

    private ChapterEnding runChapter(Chapter c) {
        // return Chapter.ABORTED if aborted
        this.activeChapter = c;
        if (c != Chapter.SPACESBETWEEN) {
            this.route.add(c);
        }

        if (!c.hasSpecialTitle()) {
            this.displayTitleCard();
        }

        switch (c) {
            case CH1: return this.heroAndPrincess();

            case ADVERSARY: return this.adversary();
            case TOWER: return this.tower();
            case SPECTRE: return this.spectre();
            case NIGHTMARE: return this.nightmare();
            case RAZOR: return this.razor();
            case BEAST: return this.beast();
            case WITCH: return this.witch();
            case STRANGER: return this.stranger();
            case PRISONER: return this.prisoner();
            case DAMSEL: return this.damsel();

            case NEEDLE: return this.eyeOfNeedle();
            case FURY: return this.fury();
            case APOTHEOSIS: return this.apotheosis();
            case DRAGON: return this.princessAndDragon();
            case WRAITH: return this.wraith();
            case CLARITY: return this.momentOfClarity();
            case ARMSRACE: return this.armsRace();
            case NOWAYOUT: return this.noWayOut();
            case DEN: return this.den();
            case WILD: return this.wild();
            case THORN: return this.thorn();
            case CAGE: return this.cage();
            case GREY:
                if (this.prevEnding == ChapterEnding.LADYKILLER) {
                    return this.greyBurned();
                } else {
                    return this.greyDrowned();
                }
            case HAPPY: return this.happilyEverAfter();

            case MUTUALLYASSURED: return this.mutuallyAssuredDestruction();
            case EMPTYCUP: return this.emptyCup();
        }

        throw new RuntimeException("Cannot run an invalid chapter");
    }

    private void displayTitleCard() {
        if (this.activeChapter == Chapter.CLARITY) {
            try {
                parser.printDivider();
                parser.printDialogueLine(new DialogueLine("Chapter III", true));
                parser.printDivider();
                Thread.sleep(750);

                parser.printDialogueLine(new DialogueLine("   Chapter IV      Chapter V", true), 1.5);
                parser.printDivider();
                Thread.sleep(700);
                
                parser.printDialogueLine(new DialogueLine(" Chapter VII   Chapter VI          Chapter VIII", true), 2.5);
                parser.printDivider();
                Thread.sleep(700);
                
                IOHandler.wrapPrintln("Chapter XIIChapter IX  ChapterChXVIerX Chapter XVChapterXIV   ChapterhaXIer XIIIChapter XVII");
                parser.printDivider();
                Thread.sleep(550);
                
                System.out.println("-----------------------------------");
                System.out.println("CChXpICXaVIIaXtVapVerXhVIItXXIhapXrIVpChXXerV");
                System.out.println("-----------------------------------");
                System.out.println("-----------------------------------");
                Thread.sleep(400);

                
                System.out.println("-----------------------------------");
                System.out.println("-----------------------------------");
                System.out.println("-----------------------------------");
                Thread.sleep(350);

                
                System.out.println("-----------------------------------");
                System.out.println("-----------------------------------");
                System.out.println("-----------------------------------");
                System.out.println("-----------------------------------");
                Thread.sleep(200);

                
                System.out.println("-----------------------------------");
                System.out.println("-----------------------------------");
                System.out.println("-----------------------------------");
                System.out.println("-----------------------------------");
                System.out.println("-----------------------------------");
                System.out.println("-----------------------------------");
                Thread.sleep(2000);

                
                IOHandler.wrapPrintln("THE MOMENT OF CLARITY");
                System.out.print("-----------------------------------");
                parser.waitForInput();

                System.out.println();
            } catch (InterruptedException e) {
                throw new RuntimeException("Thread interrupted");
            }
        } else {
            parser.printDivider();
            parser.printDialogueLine(this.activeChapter.getPrefix(), true);
            parser.printDialogueLine(this.activeChapter.getTitle(), true);
            parser.printDivider(false);
            
            System.out.println();
        }
    }

    // --- CHAPTERS & SCENES ---

    private ChapterEnding heroAndPrincess() {
        boolean canSoft = manager.hasVisited(Chapter.BEAST) && manager.hasVisited(Chapter.WITCH) && manager.hasVisited(Chapter.DAMSEL) && manager.hasVisited(Chapter.NIGHTMARE);
        boolean canHarsh = manager.hasVisited(Chapter.ADVERSARY) && manager.hasVisited(Chapter.TOWER) && manager.hasVisited(Chapter.SPECTRE) && manager.hasVisited(Chapter.NIGHTMARE) && manager.hasVisited(Chapter.RAZOR) && manager.hasVisited(Chapter.PRISONER);
        boolean canCabin = canSoft && canHarsh;

        boolean canStranger = !manager.hasVisited(Chapter.STRANGER);
        boolean canSpectre = true;
        boolean canRazor = false;

        boolean skipHillDialogue = false;

        manager.setNowPlaying("The Princess");

        parser.printDialogueLine(new VoiceDialogueLine("You're on a path in the woods. And at the end of that path is a cabin. And in the basement of that cabin is a princess."));
        parser.printDialogueLine(new VoiceDialogueLine("You're here to slay her. If you don't, it will be the end of the world."));

        this.activeMenu = new OptionsMenu();
        this.activeMenu.add(new Option(this.manager, "question1", "(Explore) The end of the world? What are you talking about?"));
        this.activeMenu.add(new Option(this.manager, "question2", "(Explore) But how can a princess locked away in a basement end the world?", activeMenu.get("question1")));
        this.activeMenu.add(new Option(this.manager, "whyDanger", "(Explore) If you don't tell me why she's dangerous, I'm not going to kill her.", activeMenu.get("question1")));
        this.activeMenu.add(new Option(this.manager, "whatHappens", "(Explore) Okay. What happens if she gets out then? I want specifics.", activeMenu.get("whyDanger")));
        this.activeMenu.add(new Option(this.manager, "evidence", "(Explore) Do you have any evidence to back this up?", activeMenu.get("question1")));
        this.activeMenu.add(new Option(this.manager, "chickenEgg", "(Explore) Have you considered that maybe the only reason she's going to end the world is *because* she's locked up?"));
        this.activeMenu.add(new Option(this.manager, "conscience", "(Explore) Killing a princess seems kind of bad, though, doesn't it?"));
        this.activeMenu.add(new Option(this.manager, "someoneElse", "(Explore) Can't someone else do this?"));
        this.activeMenu.add(new Option(this.manager, "refuse", "(Explore) Forget it. I'm not doing this."));
        this.activeMenu.add(new Option(this.manager, "letItBurn", "(Explore) Have you considered that maybe I'm okay with the world ending?"));
        this.activeMenu.add(new Option(this.manager, "prize", "(Explore) Do I get some kind of reward for doing this?"));
        this.activeMenu.add(new Option(this.manager, "prize2", "(Explore) Can you tell me what my prize is going to be for doing a good job?", activeMenu.get("prize")));
        this.activeMenu.add(new Option(this.manager, "reluctant", canCabin, "Look, I'll go to the cabin and I'll talk to her, and if she's as bad as you say she is then *maybe* I'll slay her. But I'm not committing to anything until I've had the chance to meet her face to face.", false));
        this.activeMenu.add(new Option(this.manager, "okFine", canCabin, "Okay. Fine. I'll go to the cabin.", activeMenu.get("refuse")));
        this.activeMenu.add(new Option(this.manager, "sold", canCabin, "Okay, I'm sold. Let's get this over with.", activeMenu.get("question1")));
        this.activeMenu.add(new Option(this.manager, "thanks", canCabin, "Oh, okay. Thanks for telling me what to do."));
        this.activeMenu.add(new Option(this.manager, "sweet", canCabin, "Sweet! I've always wanted to off a monarch. Viva la revolución!"));
        this.activeMenu.add(new Option(this.manager, "silent", canCabin, "[Silently continue to the cabin.]"));
        this.activeMenu.add(new Option(this.manager, "leave", "[Turn around and leave.]", Chapter.STRANGER));

        this.repeatActiveMenu = true;
        while (this.repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(this.activeMenu);

            switch (this.activeOutcome) {
                case "question1":
                    activeMenu.setCondition("reluctant", canCabin);
                    parser.printDialogueLine(new VoiceDialogueLine("I'm talking about the end of everything as we know it. No more birds, no more trees, and, perhaps most problematically of all, no more people. You have to put an end to her."));
                    break;
                case "question2":
                    activeMenu.setCondition("whyDanger", false);
                    parser.printDialogueLine(new VoiceDialogueLine("Don't linger on the specifics. You have a job to do here. Just get in there and do what needs to be done. We're all counting on you."));
                    break;
                case "whyDanger":
                    activeMenu.setCondition("question2", false);
                    parser.printDialogueLine(new VoiceDialogueLine("*She's* not dangerous. She's just a princess. The danger comes if she gets out. Which she will. Unless *you* do something about it."));
                    break;
                case "whatHappens":
                    parser.printDialogueLine(new VoiceDialogueLine("The more specifics you have, the harder it will be for you to do this very important job. She's a princess. People will listen to her, because listening to her is in their nature. And when they do, everything will come crashing down."));
                    break;
                case "evidence":
                    parser.printDialogueLine(new VoiceDialogueLine("Look, you're already on the path that leads to the cabin. Why would you be here if it weren't to complete a very important task? You've made it this far, you might as well reach the end of your journey."));
                    break;
                case "chickenEgg":
                    parser.printDialogueLine(new VoiceDialogueLine("While I appreciate the mental exercise, we are running up against a bit of a ticking clock."));
                    parser.printDialogueLine(new VoiceDialogueLine("Nevertheless, let me assure you: the Princess is locked up because she's dangerous, she is not dangerous because she's locked up."));
                    parser.printDialogueLine(new VoiceDialogueLine("And before you decide to waste even more of our time by asking how I know that, let me suggest a more pragmatic lens through which to view this situation."));
                    parser.printDialogueLine(new VoiceDialogueLine("Causality doesn't matter here, because the end result is the same no matter what led us up to this point. If the Princess leaves the cabin, the world will end, and there is no changing that."));
                    parser.printDialogueLine(new VoiceDialogueLine("It's no use arguing semantics over a metaphorical chicken-or-egg, because the egg is hatched and it's about to ruin everything."));
                    parser.printDialogueLine(new VoiceDialogueLine("Unless, of course, you do your job and *slay her*."));
                    break;
                case "conscience":
                    parser.printDialogueLine(new VoiceDialogueLine("Does it? Are you a monarchist? Is slaying a princess that much worse than slaying a fisherman or a miller or a seamstress? If anything, slaying a princess is much *better* than slaying a seamstress. Seamstresses contribute something of value to society."));
                    break;
                case "someoneElse":
                    if (activeMenu.hasBeenPicked("question2") || activeMenu.hasBeenPicked("whyDangerous")) {
                        parser.printDialogueLine(new VoiceDialogueLine("Oh, if only that were the case, but I don't make the rules."));
                        parser.printDialogueLine(new VoiceDialogueLine("I have to say I'm surprised at your reluctance thus far. But unfortunately for the both of us, you're the only one who can pull this off."));
                        parser.printDialogueLine(new VoiceDialogueLine("Like I said, I don't make the rules. No matter how much I wish I did."));
                    } else {
                        parser.printDialogueLine(new VoiceDialogueLine("Unfortunately, you're the only one who can pull this off. I don't make the rules. I wish I did, but I don't."));
                    }
                    break;
                case "refuse":
                    activeMenu.setCondition("reluctant", canCabin);
                    parser.printDialogueLine(new VoiceDialogueLine("Are you serious? No, you *have* to do it."));
                    break;
                case "letItBurn":
                    parser.printDialogueLine(new VoiceDialogueLine("Of course I haven't. Why would I even consider that? *Nobody* wants the world to end."));
                    parser.printDialogueLine(new VoiceDialogueLine("I mean, maybe *some* people do, like nihilists or very very evil people, but surely you're not one of those... right?"));
                    break;
                case "prize":
                    parser.printDialogueLine(new VoiceDialogueLine("Yes, but you'll have to slay her before you get it."));
                    break;
                case "prize2":
                    parser.printDialogueLine(new VoiceDialogueLine("It's a secret, but I think you'll like it. It's a special reward, just for you. And whatever you think it might be, I can promise you it's going to be even better than your wildest imagination."));
                    break;
                    
                case "reluctant":
                    parser.printDialogueLine(new VoiceDialogueLine("Then I guess we'll just have to see what happens. But a word of warning— if you go in prepared to hear her out, she could easily trap you in her web of lies. And the more you listen to her honeyed words, the harder it'll be to pull yourself out."));
                    parser.printDialogueLine(new VoiceDialogueLine("Then each and every one of us is doomed."));
                    parser.printDialogueLine(new VoiceDialogueLine("So, sure, go talk to her. See how that turns out for all of us."));

                    this.repeatActiveMenu = false;
                    break;
                case "okFine":
                case "sold":
                    parser.printDialogueLine(new VoiceDialogueLine("Good. As long as you remain focused on your goal, it should all be smooth sailing."));

                    this.repeatActiveMenu = false;
                    break;
                case "thanks":
                    parser.printDialogueLine(new VoiceDialogueLine("Don't mention it. It's all part of the job."));

                    this.repeatActiveMenu = false;
                    break;
                case "sweet":
                    parser.printDialogueLine(new VoiceDialogueLine("That's the spirit!"));

                    this.repeatActiveMenu = false;
                    break;
                case "cGoHill":
                    if (!canCabin) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                case "silent":
                    this.repeatActiveMenu = false;
                    break;
                case "cGoLeave":
                    if (manager.hasVisited(Chapter.STRANGER)) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    } else if (!canStranger) {
                        parser.printDialogueLine("You have already tried that.");
                        break;
                    }
                case "leave":
                    switch (this.attemptStranger(canCabin)) {
                        case 0:
                            return ChapterEnding.TOSTRANGER;
                        case 2:
                            skipHillDialogue = true;
                        case 1:
                            this.repeatActiveMenu = false;
                            canStranger = false;
                            break;
                    }

                case "cMeta":
                    System.out.println();
                    break;

                case "cGoFail":
                case "cEnterFail":
                case "cLeaveFail":
                case "cApproachFail":
                case "cSlayNoPrincessFail":
                case "cSlaySelfNoBladeFail":
                case "cTakeFail":
                case "cDropNoBladeFail":
                case "cThrowNoBladeFail":
                case "cFail":
                    this.giveDefaultResponse(this.activeOutcome);
            }
        }

        this.currentLocation = GameLocation.HILL;
        if (!skipHillDialogue) {
            System.out.println();
            parser.printDialogueLine(new DialogueLine("You emerge into a clearing. The path ahead of you winds up a hill, stopping just before a quaint wooden cabin."));
            
            parser.printDialogueLine(new VoiceDialogueLine("A warning, before you go any further..."));
            parser.printDialogueLine(new VoiceDialogueLine("She will lie, she will cheat, and she will do everything in her power to stop you from slaying her. Don't believe a word she says."));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "We're not going to go through with this, right? She's a princess. We're supposed to save princesses, not slay them."));
            parser.printDialogueLine(new VoiceDialogueLine("Ignore him. He doesn't know what he's talking about."));
        }

        this.activeMenu = new OptionsMenu();
        this.activeMenu.add(new Option(this.manager, "proceed", "[Proceed into the cabin.]"));

        this.repeatActiveMenu = true;
        while (this.repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(this.activeMenu);

            switch (this.activeOutcome) {
                case "proceed":
                    break;
            }
        }

        this.currentLocation = GameLocation.CABIN;
        parser.printDialogueLine(new VoiceDialogueLine("The interior of the cabin is almost entirely bare. The air is stale and musty and the floor and walls are painted in a fine layer of dust. The only furniture of note is a plain wooden table. Perched on that table is a pristine blade."));
        parser.printDialogueLine(new VoiceDialogueLine("The blade is your implement. You'll need it if you want to do this right."));


        //parser.printDialogueLine(new VoiceDialogueLine("XXXXX"));
        //this.activeMenu.add(new Option(this.manager, "q1", "(Explore) XXXXX"));


        // include this snippet wherever a ch2 is triggered
        if (manager.nClaimedVessels() > 0) {
            manager.setFirstPrincess(null);
        }
        
        // PLACEHOLDER
        return null;
    }

    private int attemptStranger(boolean canCabin) {
        // 0 = committed to Stranger
        // 1 = returned to cabin at first menu
        // 2 = returned to cabin at second menu or beyond

        parser.printDialogueLine(new VoiceDialogueLine("Seriously? You're just going to turn around and leave? Do you even know where you're going?"));

        OptionsMenu leaveMenu = new OptionsMenu();
        leaveMenu.add(new Option(this.manager, "ugh", canCabin, "Okay, fine. You're persistent. I'll go to the cabin and I'll slay the Princess. Ugh!"));
        leaveMenu.add(new Option(this.manager, "maybe", canCabin, "Okay, fine. I'll go to the cabin and I'll talk to the Princess. Maybe I'll slay her. Maybe I won't. I guess we'll see."));
        leaveMenu.add(new Option(this.manager, "lie", "(Lie) Yes, I definitely know where I'm going."));
        leaveMenu.add(new Option(this.manager, "nope", "Nope!"));
        leaveMenu.add(new Option(this.manager, "notGoing", "The only thing that matters is where I'm not going. (The cabin. I am not going to the cabin.)"));
        leaveMenu.add(new Option(this.manager, "nihilistA", "It's like I said, I'm pretty okay with the world ending. I relish the coming of a new dawn beyond our own.", activeMenu.hasBeenPicked("letItBurn")));
        leaveMenu.add(new Option(this.manager, "nihilistB", "I'm actually pretty okay with the world ending. I relish the coming of a new dawn beyond our own. Gonna go walk in the opposite direction now!", !activeMenu.hasBeenPicked("letItBurn")));
        leaveMenu.add(new Option(this.manager, "quiet", "[Quietly continue down the path away from the cabin.]"));

        switch (parser.promptOptionsMenu(leaveMenu)) {
            case "ugh":
                parser.printDialogueLine(new VoiceDialogueLine("*Thank you*! The whole world owes you a debt of gratitude. Really."));
                return 1;
            case "maybe":
                parser.printDialogueLine(new VoiceDialogueLine("I guess we will."));
                return 1;
            case "lie":
                parser.printDialogueLine(new VoiceDialogueLine("Somehow I doubt that, but fine."));
                parser.printDialogueLine(new VoiceDialogueLine("I suppose you just quietly continue down the path away from the cabin."));
                break;
            case "nope":
                parser.printDialogueLine(new VoiceDialogueLine("Fine, I suppose you just quietly continue down the path away from the cabin."));
                break;
            case "notGoing":
                parser.printDialogueLine(new VoiceDialogueLine("Fine, I suppose you just quietly continue down the path away from the cabin."));
                break;
            case "nihilistA":
            case "nihilistB":
                parser.printDialogueLine(new VoiceDialogueLine("There won't *be* a \"new dawn\" if the world ends. There'll just be *nothing*. Forever!"));
                parser.printDialogueLine(new VoiceDialogueLine("Fine, I suppose you just quietly continue down the path away from the cabin."));
                break;
            case "quiet":
                parser.printDialogueLine(new VoiceDialogueLine("Fine, I suppose you just quietly continue down the path away from the cabin."));
                break;
        }
        
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Good. What we're being asked to do here is *wrong*. Better to wash our hands of this whole situation than to take part in it."));
        parser.printDialogueLine(new VoiceDialogueLine("Ignore that annoying little voice. He doesn't know what he's talking about."));

        System.out.println();
        this.currentLocation = GameLocation.HILL;
        parser.printDialogueLine(new DialogueLine("You emerge into a clearing. The path ahead of you winds up a hill, stopping just before a quaint wooden cabin."));
        parser.printDialogueLine(new VoiceDialogueLine("That's strange. It looks like this path also leads to the cabin. How convenient! Everything's back on track again. Maybe the world can still be saved after all."));

        leaveMenu = new OptionsMenu();
        leaveMenu.add(new Option(this.manager, "cabin", canCabin, "Okay, okay! I'm going into the cabin. Sheesh."));
        leaveMenu.add(new Option(this.manager, "commit", "[Turn around (again) and leave (again).]"));

        switch (parser.promptOptionsMenu(leaveMenu)) {
            case "cabin":
                parser.printDialogueLine(new VoiceDialogueLine("That's great to hear! And as long as you bring that fiery attitude to Princess slaying, I think this will all resolve splendidly."));

                parser.printDialogueLine(new VoiceDialogueLine("A warning, before you go any further..."));
                parser.printDialogueLine(new VoiceDialogueLine("She will lie, she will cheat, and she will do everything in her power to stop you from slaying her. Don't believe a word she says."));
                parser.printDialogueLine(new VoiceDialogueLine("Fortunately, she's only a Princess, whereas you are a valiant and talented warrior. It'll be *easy* so long as you stay focused."));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "We can't just go through with this and listen to Him. She's a princess. We're supposed to save princesses, not slay them."));

                return 2;
            case "commit":
                break;
        }

        parser.printDialogueLine(new VoiceDialogueLine("You're really keen on wasting everyone's time, aren't you? It's remarkably selfish, if you ask me. I've already outlined the stakes of the situation. If you don't do your job, everyone dies. Like, *dies* dies. Forever."));

        leaveMenu = new OptionsMenu();
        leaveMenu.add(new Option(this.manager, "dontCare", "I don't care! I'm not killing a princess!"));
        leaveMenu.add(new Option(this.manager, "good", "Good! Maybe everyone *should* die! It's what they get for dumping me in the woods and asking me to *kill* someone for them."));
        leaveMenu.add(new Option(this.manager, "blackmail", "You're not emotionally blackmailing me into doing this!"));
        leaveMenu.add(new Option(this.manager, "quiet", "[Quietly continue down the path.]"));

        switch (parser.promptOptionsMenu(leaveMenu)) {
            case "dontCare":
                parser.printDialogueLine(new VoiceDialogueLine("\"Killing\" is such gauche phrasing, and completely ignores the bigger picture. Your task is to *slay* the Princess. Because she's terrible and she's really got it coming to her."));
                break;
            case "good":
                parser.printDialogueLine(new VoiceDialogueLine("When I said everyone, I meant *everyone*. That's a pretty large group to just condemn to death over a single Princess."));
                parser.printDialogueLine(new VoiceDialogueLine("And last I checked you're a part of everyone, too, so if you think about it, walking up to that cabin and slaying her is really in *your* best interests as well."));
                break;
            case "blackmail":
                parser.printDialogueLine(new VoiceDialogueLine("Stakes and consequences aren't emotional blackmail. They're facts of life, and if you had an ounce of maturity you'd understand that."));
                break;
            case "quiet":
                parser.printDialogueLine(new VoiceDialogueLine("Your silence is deafening."));
                break;
        }

        parser.printDialogueLine(new VoiceDialogueLine("But fine. You turn around and trek back down the path you came."));

        System.out.println();
        parser.printDialogueLine(new DialogueLine("Eventually, the cabin comes into view once again."));
        parser.printDialogueLine(new VoiceDialogueLine("Oh, would you look at that! You're at the cabin again! Now, I'm not normally one for superstition or astrology, but I have to say, it seems like the Universe itself is doing its best to bring you to your fated confrontation with the Princess."));

        leaveMenu = new OptionsMenu();
        leaveMenu.add(new Option(this.manager, "cabin", canCabin, "There's no fighting this, is there? I have to go into the cabin, don't I? Fine."));
        leaveMenu.add(new Option(this.manager, "commit", "Oh, yeah? Well I guess I start walking in a different direction. Again. In fact, I'm going to just keep trekking through the wilderness until I find a way out of this place.", 0));

        boolean repeatMenu = true;
        while (repeatMenu) {
            switch (parser.promptOptionsMenu(leaveMenu)) {
                case "cabin":
                    parser.printDialogueLine(new VoiceDialogueLine("There's always a choice, but let me tell you right now that you're making the correct decision for pretty much everyone."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "We can't just go through with this and listen to Him. She's a princess. We're supposed to save princesses, not slay them."));
                    return 2;
                case "commit":
                    if (manager.confirmContentWarnings(Chapter.STRANGER)) {
                        repeatMenu = false;
                        break;
                    } else {
                        leaveMenu.setGreyedOut("commit", true);
                    }
            }
        }

        parser.printDialogueLine(new VoiceDialogueLine("There's always a choice, but let me tell you right now that you're making the *wrong* one for pretty much everyone who's ever lived, as well as for everyone who ever will."));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("And here we go. As you trudge into the woods, something strange starts to happen."));
        parser.printDialogueLine(new VoiceDialogueLine("At first, it's little flickers out of the corner of your eyes, glimpses of familiar wooden structures through the leaves."));
        parser.printDialogueLine(new VoiceDialogueLine("But as you focus on your surroundings, you start to realize that those flickers weren't just a trick of light."));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("In every direction there is a path and a cabin. And not just *a* cabin. *The* cabin. An infinite fractal of paths and cabins desperately trying to draw you back to where you need to be."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Wait... what's going on?"));
        parser.printDialogueLine(new VoiceDialogueLine("But you're too stubborn for that, aren't you? It doesn't matter how many paths or cabins appear around you. You're just going to do whatever you can to shirk your responsibility, because you care more about irritating me than you do about the fate of the world."));
        parser.printDialogueLine(new VoiceDialogueLine("You've doomed us all. You know that, right? But of course you do, otherwise you wouldn't just wander off into the forest in search of certain death."));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("You lose track of just how long you spend aimlessly tromping through the wilderness, but it's not like any of that time spent lost in the woods really matters, because it isn't long before the world ends and everyone dies."));

        return 0;
    }

    private void chapter2Intro() {
        // shared conversation up through entering the cabin
    }

    private ChapterEnding adversary() {
        this.chapter2Intro();

        // PLACEHOLDER
        return null;
    }

    private ChapterEnding eyeOfNeedle() {
        // PLACEHOLDER
        return null;
    }

    private ChapterEnding fury() {
        String source;
        switch (this.prevEnding) {
            case STRIKEMEDOWN: source = "pacifism";
            case HEARNOBELL: source = "unarmed";
            case DEADISDEAD: source = "pathetic";
            default: source = "tower";
        }

        // PLACEHOLDER
        return null;
    }

    private ChapterEnding tower() {
        this.chapter2Intro();
        
        // PLACEHOLDER
        return null;
    }

    private ChapterEnding apotheosis() {
        // PLACEHOLDER
        return null;
    }

    private ChapterEnding spectre() {
        this.chapter2Intro();
        
        this.isHarsh = false;

        // PLACEHOLDER
        return null;
    }

    private ChapterEnding princessAndDragon() {
        // PLACEHOLDER
        return null;
    }

    private ChapterEnding wraith() {
        String source;
        switch (this.prevEnding) {
            case HEARTRIPPER: source = "spectre";
            case HEARTRIPPERLEAVE: source = "spectre";
            default: source = "nightmare";
        }

        // PLACEHOLDER
        return null;
    }

    private ChapterEnding nightmare() {
        this.chapter2Intro();
        
        // PLACEHOLDER
        return null;
    }

    private ChapterEnding momentOfClarity() {
        // PLACEHOLDER
        return null;
    }

    private ChapterEnding razor() {
        this.chapter2Intro();
        
        // PLACEHOLDER
        return null;
    }

    private ChapterEnding armsRace() {
        // PLACEHOLDER
        return null;
    }

    private ChapterEnding mutuallyAssuredDestruction() {
        // PLACEHOLDER
        return null;
    }

    private ChapterEnding noWayOut() {
        // PLACEHOLDER
        return null;
    }

    private ChapterEnding emptyCup() {
        // PLACEHOLDER
        return null;
    }

    private ChapterEnding beast() {
        this.chapter2Intro();
        
        // PLACEHOLDER
        return null;
    }

    private ChapterEnding den() {
        // PLACEHOLDER
        return null;
    }

    private ChapterEnding wild() {
        String source;
        if (this.hasVoice(Voice.HUNTED)) {
            source = "beast";
        } else {
            source = "witch";
        }

        // PLACEHOLDER
        return null;
    }

    private ChapterEnding witch() {
        this.chapter2Intro();
        
        // PLACEHOLDER
        return null;
    }

    private ChapterEnding thorn() {
        // PLACEHOLDER
        return null;
    }

    private ChapterEnding stranger() {
        // NO CH2INTRO -- will need to include that conversation manually here!
        
        // PLACEHOLDER
        return null;
    }

    private ChapterEnding prisoner() {
        this.chapter2Intro();
        
        // PLACEHOLDER
        return null;
    }

    private ChapterEnding cage() {
        // PLACEHOLDER
        return null;
    }

    private ChapterEnding greyDrowned() {
        // PLACEHOLDER
        return null;
    }

    private ChapterEnding greyBurned() {
        // PLACEHOLDER
        return null;
    }

    private ChapterEnding damsel() {
        this.chapter2Intro();
        
        // PLACEHOLDER
        return null;
    }

    private ChapterEnding happilyEverAfter() {
        // PLACEHOLDER
        return null;
    }

    private void mirrorSequence() {
        this.activeChapter = Chapter.SPACESBETWEEN;
        this.mirrorPresent = true;

        // regular lead-up here

        switch (manager.nClaimedVessels()) { // look at mirror
            case 0:
                // it's you
                this.theSpacesBetween();
                break;
            case 1:
                // you've grown
                this.theSpacesBetween();
                break;
            case 2:
                // you've withered
                this.theSpacesBetween();
                break;
            case 3:
                // you've unraveled
                this.theSpacesBetween();
                break;
            case 4:
                // you are nothing at all
                // but that isn't right etc.
                // include "are you me?" here
                this.finalMirror();
                break;
        }
    }

    private void theSpacesBetween() {
        // spaces between intro here

        switch (manager.nClaimedVessels()) { // conversation
            case 0:
                break;
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
        }
    }

    private void finalMirror() {
        // starts right after mirror shatters
    }

    private boolean attemptAbortVessel() {
        // true if attempt goes through, false if player returns
        
        // PLACEHOLDER
        return false;
    }

    private void abortVessel() {

    }

}
