import java.util.ArrayList;
import java.util.HashMap;

public class StandardCycle extends Cycle {

    // One CYCLE = from beginning of Ch1 to the end of Shifting Mound interlude

    private boolean isFirstVessel;
    private ArrayList<Voice> voicesMet;
    private ArrayList<Chapter> route;
    private ChapterEnding prevEnding;
    
    // Utility variables for checking command availability & default responses
    private boolean canTryAbort = true;

    // Variables that are used in a lot of chapters
    private Voice ch2Voice;
    private Voice ch3Voice;
    protected String source = "";
    private boolean sharedLoop = false; // Used in all Chapter 2s and 3s: does the Narrator know?
    private boolean sharedLoopInsist = false; // Used in all Chapter 2s
    private boolean skipHillDialogue = false; // Used in Chapter 1 and all Chapter 2s; if you backed out of Stranger / aborting the Chapter

    // Variables that persist between chapters
    private boolean seenMirror = false;
    private boolean touchedMirror = false; // Used in all Chapter 2s and 3s
    private boolean whatWouldYouDo = false; // Used in Chapter 1, Damsel
    private boolean knowsDestiny = false; // Used in Chapter 1, Tower, Fury

    private static final PrincessDialogueLine CANTSTRAY = new PrincessDialogueLine(true, "You have already committed to my completion. You cannot go further astray.");

    // --- CONSTRUCTOR ---

    /**
     * Constructor
     * @param manager the GameManager to link this StandardCycle to
     * @param parser the IOHandler to link this StandardCycle to
     */
    public StandardCycle(GameManager manager, IOHandler parser) {
        super(manager, parser);

        this.isFirstVessel = manager.nClaimedVessels() == 0;
        this.route = new ArrayList<>();

        this.prevEnding = ChapterEnding.NEWCYCLE;

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

    // --- MANIPULATORS ---

    /**
     * Adds a given Voice to the list of active Voices
     * @param v the Voice to add
     */
    @Override
    protected void addVoice(Voice v) {
        super.addVoice(v);
        if (v != Voice.NARRATOR && v != Voice.HERO && v != Voice.PRINCESS) {
            this.voicesMet.add(v);
        }
    }

    // --- COMMANDS ---

    /**
     * Lets the player choose between viewing general content warnings, content warnings by chapter, or content warnings for the current chapter
     */
    @Override
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
                manager.showChapterWarnings(this.activeChapter, this.prevEnding);
                break;
            case "cancel":
                break;
        }

        this.trueExclusiveMenu = false;
    }

    /**
     * Shows general content warnings, content warnings by chapter, or content warnings for the current chapter
     * @param argument the set of content warnings to view
     */
    @Override
    protected void showContentWarnings(String argument) {
        switch (argument) {
            case "current":
            case "active":
            case "chapter":
            case "current chapter":
            case "active chapter":
            case "route":
            case "current route":
            case "active route":
                manager.showChapterWarnings(this.activeChapter, this.prevEnding);
                break;

            default: super.showContentWarnings(argument);
        }
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
    @Override
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
    @Override
    public String leave(String argument) {
        switch (argument) {
            case "": return this.go("backTRUE");

            case "woods":
            case "path":
                switch (this.currentLocation) {
                    case PATH:
                    case HILL: return this.go("backTRUE");
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
     * Attempts to let the player approach the mirror
     * @param argument the argument given by the player (should be "the mirror" or "mirror")
     * @param secondPrompt whether the player has already been given a chance to re-enter a valid argument
     * @return "cFail" if argument is invalid; "cApproachFail" if the mirror is not present; "cApproach" otherwise
     */
    protected String approach(String argument, boolean secondPrompt) {
        switch (argument) {
            case "the mirror":
            case "mirror":
                if (this.currentLocation == GameLocation.MIRROR) {
                    return "ApproachAtMirrorFail";
                } else if (!this.mirrorPresent) {
                    return "ApproachFail";
                } else {
                    return "Approach";
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
     * Attempts to let the player approach the mirror
     * @param argument the argument given by the player (should be "the mirror" or "mirror")
     * @return "cFail" if argument is invalid; "cApproachFail" if the mirror is not present; "cApproach" otherwise
     */
    @Override
    public String approach(String argument) {
        return this.approach(argument, false);
    }

    /**
     * Attempts to let the player slay either the Princess or themselves
     * @param argument the target to slay
     * @param secondPrompt whether the player has already been given a chance to re-enter a valid argument
     * @return "cFail" if argument is invalid; "cSlayNoPrincessFail" if attempting to slay the Princess when she is not present; "cSlayPrincessNoBladeFail" if attempting to slay the Princess without the blade; "cSlayPrincessFail" if the player cannot slay the Princess  right now; "cSlayPrincess" if otherwise attempting to slay the Princess; "cSlaySelfNoBladeFail" if attempting to slay themselves without the blade; "cSlaySelfFail" if the player cannot slay themselves right now; "cSlaySelf" if otherwise attempting to slay themselves
     */
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

    /**
     * Prints a generic response to a command failing or being unavailable
     * @param outcome the String representation of the outcome of the attempted command
     */
    @Override
    protected void giveDefaultFailResponse(String outcome) {
        if (!this.hasVoice(Voice.NARRATOR)) super.giveDefaultFailResponse(outcome);

        switch (outcome) {
            case "cGoFail":
                parser.printDialogueLine(new VoiceDialogueLine("There's nowhere to go that way."));
                break;

            case "cEnterFail":
                parser.printDialogueLine(new VoiceDialogueLine("You can't get there from where you are now. Just keep moving forward."));
                break;
                
            case "cLeaveFail":
                parser.printDialogueLine(new VoiceDialogueLine("You can't leave a place if you aren't there in the first place."));
                break;


            case "cApproachAtMirrorFail":
                parser.printDialogueLine(new VoiceDialogueLine("I don't know how many times I have to say this, but there is no mirror."));
                if (this.hasVoice(Voice.HERO)) {
                    if (this.mirrorPresent) {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "We're... already standing in front of the mirror."));
                    } else {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Well, *now* there isn't."));
                    }
                }

                break;
                
            case "cApproachFail":
                parser.printDialogueLine(new VoiceDialogueLine("What are you talking about? There isn't a mirror."));
                if (this.seenMirror && this.hasVoice(Voice.HERO)) parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "He's actually right this time. The mirror really isn't here."));
                break;
                

            case "cSlayNoPrincessFail":
                parser.printDialogueLine(new VoiceDialogueLine("As much as I appreciate your enthusiasm, the Princess isn't here right now. Save it for when you reach the basement."));
                break;

            case "cSlayPrincessNoBladeFail":
                parser.printDialogueLine(new VoiceDialogueLine("*sigh* Unfortunately, you have no weapon with which to slay her. If only you had the blade, this would be so much easier."));
                break;

            case "cSlayPrincessFail":
                // The Narrator doesn't have a line here because there is no universe in which he would ever say no to you trying to slay the Princess if you have the opportunity.
                // Unfortunately for him, sometimes I can't let you slay her in the middle of certain menus. Too bad, Narrator.
                super.giveDefaultFailResponse(outcome);
                break;

            case "cSlaySelfNoBladeFail":
                parser.printDialogueLine(new DialogueLine("You do not have the blade."));
                break;

            case "cSlaySelfFail":
                parser.printDialogueLine(new VoiceDialogueLine("Are you insane?! Absolutely not."));
                if (this.hasVoice(Voice.HERO)) parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "He's right. We don't need to make such a rash decision."));
                break;
                

            case "cTakeHasBladeFail":
                parser.printDialogueLine(new VoiceDialogueLine("You already have the blade, remember?"));
                break;
            
            case "cTakeFail":
                if (this.knowsBlade) {
                    parser.printDialogueLine(new VoiceDialogueLine("As much as I appreciate your enthusiasm, the blade isn't here right now."));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine("As much as I appreciate your enthusiasm, there isn't a blade here."));
                }

                break;

            case "cDropNoBladeFail":
                if (this.knowsBlade) {
                    parser.printDialogueLine(new VoiceDialogueLine("I can't fathom why you would want to drop your only weapon, but you don't even have it right now."));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine("I can't fathom why you would want to drop your weapon, but you don't even have one right now."));
                }
                
                break;

            case "cDropFail":
                parser.printDialogueLine(new VoiceDialogueLine("You can't drop the blade now. You're here for a reason. Finish the job."));
                break;

            case "cThrowNoBladeFail":
                if (this.knowsBlade) {
                    parser.printDialogueLine(new VoiceDialogueLine("I can't fathom why you would want to throw your only weapon away, but conveniently, you don't even have it right now."));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine("I can't fathom why you would want to throw your weapon away, but conveniently, you don't even have one right now."));
                }
                
                break;
                
            case "cThrowFail":
                parser.printDialogueLine(new VoiceDialogueLine("Are you insane?! Absolutely not."));
                if (this.hasVoice(Voice.HERO)) parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Why would we even do that? That seems... silly."));
                break;

            default:
                parser.printDialogueLine(new VoiceDialogueLine("You have to make a decision."));
        }
    }

    /**
     * Prints a generic response to a command failing or being unavailable
     */
    @Override
    protected void giveDefaultFailResponse() {
        if (!this.hasVoice(Voice.NARRATOR)) {
            super.giveDefaultFailResponse();
        } else {
            parser.printDialogueLine(new VoiceDialogueLine("You have to make a decision."));
        }
    }

    // --- CYCLE MANAGEMENT ---

    /**
     * Initiates and coordinates a full cycle, from the beginning of Chapter I through the player's conversation with the Shifting Mound
     * @return the Chapter ending reached by the player
     */
    @Override
    public ChapterEnding runCycle() {
        Chapter nextChapter;

        while (!this.prevEnding.isFinal()) {
            nextChapter = this.prevEnding.getNextChapter();
            this.route.add(nextChapter);

            if (this.prevEnding != ChapterEnding.NEWCYCLE) {
                switch (nextChapter.getNumber()) {
                    case 2:
                        this.ch2Voice = prevEnding.getNewVoice();
                        break;
                    case 3:
                        this.ch3Voice = prevEnding.getNewVoice();
                }

                if (nextChapter == Chapter.CLARITY) {
                    for (Voice v : Voice.values()) {
                        if (v != Voice.PRINCESS) {
                            this.addVoice(v);
                        }
                    }
                } else if (nextChapter == Chapter.HAPPY) {
                    this.currentVoices.put(Voice.SMITTEN, false);
                }

                if (nextChapter == Chapter.DRAGON) {
                    this.clearVoices();
                    this.addVoice(Voice.PRINCESS);
                } else if (prevEnding.getNewVoice() != null) {
                    this.addVoice(prevEnding.getNewVoice());
                    if (prevEnding.getNewVoice2() != null) this.addVoice(prevEnding.getNewVoice2());
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

                this.source = "";
                this.threwBlade = false;
                this.sharedLoop = false;
                this.sharedLoopInsist = false;
                this.skipHillDialogue = false;
                this.bladeReverse = false;

                this.repeatActiveMenu = false;
                this.reverseDirection = false;
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

    /**
     * (DEBUG ONLY) Initiates and coordinates a full cycle, starting from a given Chapter ending through the player's conversation with the Shifting Mound
     * @param startFromEnding the ChapterEnding to start from
     * @param harsh the value to set isHarsh to
     * @return the Chapter ending reached by the player
     */
    @Override
    public ChapterEnding debugRunCycle(ChapterEnding startFromEnding, boolean harsh) {
        this.prevEnding = startFromEnding;
        this.isHarsh = harsh;
        // harsh only matters if initializing Nightmare or Princess and the Dragon

        // If initializing a Chapter 3, add the appropriate Chapter 2 voice
        switch (startFromEnding) {
            case THATWHICHCANNOTDIE:
            case STRIKEMEDOWN:
            case HEARNOBELL:
            case DEADISDEAD:
            case THREADINGTHROUGH:
            case FREEINGSOMEONE:
                this.route.add(Chapter.ADVERSARY);
                this.addVoice(Voice.STUBBORN);
                this.ch2Voice = Voice.STUBBORN;
                break;

            case OBEDIENTSERVANT:
            case GODKILLER:
            case APOBLADE:
            case APOUNARMED:
                this.route.add(Chapter.TOWER);
                this.addVoice(Voice.BROKEN);
                this.ch2Voice = Voice.BROKEN;
                break;

            case HITCHHIKER:
            case HEARTRIPPERLEAVE:
            case HEARTRIPPER:
            case EXORCIST:
                this.route.add(Chapter.SPECTRE);
                this.addVoice(Voice.COLD);
                this.ch2Voice = Voice.COLD;
                break;

            case WORLDOFTERROR:
            case HOUSEOFNOLEAVE:
            case TERMINALVELOCITY:
            case MONOLITHOFFEAR:
                this.route.add(Chapter.NIGHTMARE);
                this.addVoice(Voice.PARANOID);
                this.ch2Voice = Voice.PARANOID;
                break;

            case TOARMSRACEFIGHT:
            case TOARMSRACEBORED:
            case TOARMSRACELEFT:
            case TONOWAYOUTBORED:
            case TONOWAYOUTLEFT:
                this.route.add(Chapter.RAZOR);
                this.addVoice(Voice.CHEATED);
                this.ch2Voice = Voice.CHEATED;
                break;

            case TOMUTUALLYASSURED:
                this.route.add(Chapter.RAZOR);
                this.route.add(Chapter.ARMSRACE);
                this.ch2Voice = Voice.CHEATED;
                break;

            case TOEMPTYCUP:
                this.route.add(Chapter.RAZOR);
                this.route.add(Chapter.NOWAYOUT);
                this.ch2Voice = Voice.CHEATED;
                break;

            case DISSOLVINGWILL:
            case FIGHT:
            case FLIGHT:
            case OPOSSUM:
            case AHAB:
            case SLAYYOURSELF:
            case DISSOLVED:
                this.route.add(Chapter.BEAST);
                this.addVoice(Voice.HUNTED);
                this.ch2Voice = Voice.HUNTED;
                break;

            case SCORPION:
            case FROG:
            case FROGLOCKED:
            case KNIVESOUTMASKSOFF:
            case KNIVESOUTMASKSOFFESCAPE:
            case PLAYINGITSAFE:
            case PASTLIFEGAMBITSPECIAL:
            case PASTLIFEGAMBIT:
                this.route.add(Chapter.WITCH);
                this.addVoice(Voice.OPPORTUNIST);
                this.ch2Voice = Voice.OPPORTUNIST;
                break;

            case ILLUSIONOFCHOICE:
                this.route.add(Chapter.STRANGER);
                this.addVoice(Voice.CONTRARIAN);
                this.ch2Voice = Voice.CONTRARIAN;
                break;

            case TALKINGHEADS:
            case PRISONEROFMIND:
            case COLDLYRATIONAL:
            case RESTLESSFORCED:
            case RESTLESSSELF:
            case RESTLESSGIVEIN:
                this.route.add(Chapter.PRISONER);
                this.addVoice(Voice.SKEPTIC);
                this.ch2Voice = Voice.SKEPTIC;
                break;

            case ROMANTICHAZE:
            case ANDTHEYLIVEDHAPPILY:
            case LADYKILLER:
            case CONTENTSOFOURHEARTDECON:
            case CONTENTSOFOURHEARTUPSTAIRS:
                this.route.add(Chapter.DAMSEL);
                this.addVoice(Voice.SMITTEN);
                this.ch2Voice = Voice.SMITTEN;
                break;
        }

        return this.runCycle();
    }

    /**
     * Initiates a given Chapter
     * @param c the Chapter to run
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding runChapter(Chapter c) {
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
                if (this.prevEnding == ChapterEnding.LADYKILLER) return this.greyBurned();
                else return this.greyDrowned();
            case HAPPY: return this.happilyEverAfter();

            case MUTUALLYASSURED: return this.mutuallyAssuredDestruction();
            case EMPTYCUP: return this.emptyCup();
        }

        throw new RuntimeException("Cannot run an invalid chapter");
    }

    /**
     * Displays the title card of the active Chapter
     */
    private void displayTitleCard() {
        System.out.println();
        System.out.println();
        System.out.println();

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

    // - Chapter I: The Hero and the Princess -

    /**
     * Runs the opening sequence of Chapter I, from the opening conversation to entering the basement
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding heroAndPrincess() {
        // You always start with the Voice of the Hero
        
        boolean canSoft = !manager.hasVisitedAll(Chapter.BEAST, Chapter.WITCH, Chapter.DAMSEL, Chapter.NIGHTMARE);
        boolean canHarsh = !manager.hasVisitedAll(Chapter.ADVERSARY, Chapter.TOWER, Chapter.SPECTRE, Chapter.NIGHTMARE, Chapter.RAZOR, Chapter.PRISONER);
        boolean canCabin = canSoft || canHarsh;

        boolean canStranger = !manager.hasVisited(Chapter.STRANGER);

        manager.setNowPlaying("The Princess");

        parser.printDialogueLine(new VoiceDialogueLine("You're on a path in the woods. And at the end of that path is a cabin. And in the basement of that cabin is a princess."));
        parser.printDialogueLine(new VoiceDialogueLine("You're here to slay her. If you don't, it will be the end of the world."));

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "question1", "(Explore) The end of the world? What are you talking about?"));
        activeMenu.add(new Option(this.manager, "question2", "(Explore) But how can a princess locked away in a basement end the world?", activeMenu.get("question1")));
        activeMenu.add(new Option(this.manager, "whyDanger", "(Explore) If you don't tell me why she's dangerous, I'm not going to kill her.", activeMenu.get("question1")));
        activeMenu.add(new Option(this.manager, "whatHappens", "(Explore) Okay. What happens if she gets out then? I want specifics.", activeMenu.get("whyDanger")));
        activeMenu.add(new Option(this.manager, "evidence", "(Explore) Do you have any evidence to back this up?", activeMenu.get("question1")));
        activeMenu.add(new Option(this.manager, "chickenEgg", "(Explore) Have you considered that maybe the only reason she's going to end the world is *because* she's locked up?"));
        activeMenu.add(new Option(this.manager, "conscience", "(Explore) Killing a princess seems kind of bad, though, doesn't it?"));
        activeMenu.add(new Option(this.manager, "someoneElse", "(Explore) Can't someone else do this?"));
        activeMenu.add(new Option(this.manager, "refuse", "(Explore) Forget it. I'm not doing this."));
        activeMenu.add(new Option(this.manager, "letItBurn", "(Explore) Have you considered that maybe I'm okay with the world ending?"));
        activeMenu.add(new Option(this.manager, "prize", "(Explore) Do I get some kind of reward for doing this?"));
        activeMenu.add(new Option(this.manager, "prize2", "(Explore) Can you tell me what my prize is going to be for doing a good job?", activeMenu.get("prize")));
        activeMenu.add(new Option(this.manager, "reluctant", !canCabin, "Look, I'll go to the cabin and I'll talk to her, and if she's as bad as you say she is then *maybe* I'll slay her. But I'm not committing to anything until I've had the chance to meet her face to face.", false));
        activeMenu.add(new Option(this.manager, "okFine", !canCabin, "Okay. Fine. I'll go to the cabin.", activeMenu.get("refuse")));
        activeMenu.add(new Option(this.manager, "sold", !canCabin, "Okay, I'm sold. Let's get this over with.", activeMenu.get("question1")));
        activeMenu.add(new Option(this.manager, "thanks", !canCabin, "Oh, okay. Thanks for telling me what to do."));
        activeMenu.add(new Option(this.manager, "sweet", !canCabin, "Sweet! I've always wanted to off a monarch. Viva la revolución!"));
        activeMenu.add(new Option(this.manager, "silent", !canCabin, "[Silently continue to the cabin.]"));
        activeMenu.add(new Option(this.manager, "leave", "[Turn around and leave.]", Chapter.STRANGER));

        boolean askPrize = false;
        this.repeatActiveMenu = true;
        while (this.repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);

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
                    parser.printDialogueLine(new VoiceDialogueLine("Unless, of course, you do your job and *slay her.*"));
                    break;
                case "conscience":
                    parser.printDialogueLine(new VoiceDialogueLine("Does it? Are you a monarchist? Is slaying a princess that much worse than slaying a fisherman or a miller or a seamstress? If anything, slaying a princess is much *better* than slaying a seamstress. Seamstresses contribute something of value to society."));
                    break;
                case "someoneElse":
                    if (activeMenu.hasBeenPicked("question2") || activeMenu.hasBeenPicked("whyDanger")) {
                        parser.printDialogueLine(new VoiceDialogueLine("Oh, if only that were the case, but I don't make the rules."));
                        parser.printDialogueLine(new VoiceDialogueLine("I have to say I'm surprised at your reluctance thus far. But unfortunately for the both of us, you're the only one who can pull this off."));
                        parser.printDialogueLine(new VoiceDialogueLine("Like I said, I don't make the rules. No matter how much I wish I did."));
                    } else {
                        parser.printDialogueLine(new VoiceDialogueLine("Unfortunately, you're the only one who can pull this off. I don't make the rules. I wish I did, but I don't."));
                    }
                    break;
                case "refuse":
                    activeMenu.setCondition("reluctant", true);
                    parser.printDialogueLine(new VoiceDialogueLine("Are you serious? No, you *have* to do it."));
                    break;
                case "letItBurn":
                    parser.printDialogueLine(new VoiceDialogueLine("Of course I haven't. Why would I even consider that? *Nobody* wants the world to end."));
                    parser.printDialogueLine(new VoiceDialogueLine("I mean, maybe *some* people do, like nihilists or very very evil people, but surely you're not one of those... right?"));
                    break;
                case "prize":
                    askPrize = true;
                    parser.printDialogueLine(new VoiceDialogueLine("Yes, but you'll have to slay her before you get it."));
                    break;
                case "prize2":
                    parser.printDialogueLine(new VoiceDialogueLine("It's a secret, but I think you'll like it. It's a special reward, just for you. And whatever you think it might be, I can promise you it's going to be even better than your wildest imagination."));
                    break;
                    
                case "reluctant":
                    parser.printDialogueLine(new VoiceDialogueLine("Then I guess we'll just have to see what happens. But a word of warning -- if you go in prepared to hear her out, she could easily trap you in her web of lies. And the more you listen to her honeyed words, the harder it'll be to pull yourself out."));
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
                    switch (this.ch1AttemptStranger(canCabin)) {
                        case 0:
                            return ChapterEnding.TOSTRANGER;
                        case 2:
                            this.skipHillDialogue = true;
                        case 1:
                            this.currentLocation = GameLocation.HILL;
                            this.repeatActiveMenu = false;
                            canStranger = false;
                            break;
                    }

                default:
                    this.giveDefaultFailResponse(this.activeOutcome);
            }
        }

        this.currentLocation = GameLocation.HILL;
        if (!this.skipHillDialogue) {
            System.out.println();
            parser.printDialogueLine(new DialogueLine("You emerge into a clearing. The path ahead of you winds up a hill, stopping just before a quaint wooden cabin."));
            
            parser.printDialogueLine(new VoiceDialogueLine("A warning, before you go any further..."));
            parser.printDialogueLine(new VoiceDialogueLine("She will lie, she will cheat, and she will do everything in her power to stop you from slaying her. Don't believe a word she says."));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "We're not going to go through with this, right? She's a princess. We're supposed to save princesses, not slay them."));
            parser.printDialogueLine(new VoiceDialogueLine("Ignore him. He doesn't know what he's talking about."));
        }

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "proceed", "[Proceed into the cabin.]"));

        this.repeatActiveMenu = true;
        while (this.repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);

            switch (this.activeOutcome) {
                case "cGoCabin":
                case "proceed":
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

                    switch (this.ch1AttemptStranger(true)) {
                        case 0:
                            return ChapterEnding.TOSTRANGER;
                        case 2:
                        case 1:
                            this.currentLocation = GameLocation.HILL;
                            this.repeatActiveMenu = false;
                            canStranger = false;
                            break;
                    }
                    
                default:
                    this.giveDefaultFailResponse(this.activeOutcome);
            }
        }

        this.currentLocation = GameLocation.CABIN;
        this.knowsBlade = true;
        this.withBlade = true;
        parser.printDialogueLine(new VoiceDialogueLine("The interior of the cabin is almost entirely bare. The air is stale and musty and the floor and walls are painted in a fine layer of dust. The only furniture of note is a plain wooden table. Perched on that table is a pristine blade."));
        parser.printDialogueLine(new VoiceDialogueLine("The blade is your implement. You'll need it if you want to do this right."));

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "take", !canHarsh, "(Explore) [Take the blade.]"));
        activeMenu.add(new Option(this.manager, "enter", !canSoft, "[Enter the basement.]"));

        this.repeatActiveMenu = true;
        while (this.repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);

            switch (this.activeOutcome) {
                case "cTake":
                    if (!canHarsh) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }

                    activeMenu.setCondition("take", false);
                case "take":
                    this.isHarsh = true;
                    this.hasBlade = true;
                    this.withBlade = false;
                    manager.setNowPlaying("The World Ender");
                    activeMenu.setGreyedOut("enter", false);

                    parser.printDialogueLine(new VoiceDialogueLine("You take the blade from the table. It'd be rather difficult to slay the Princess and save the world without it."));
                    break;
                
                case "cGoStairs":
                    if (!this.isHarsh && !canSoft) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                case "enter":
                    this.repeatActiveMenu = false;
                    this.withBlade = false;
                    return (this.isHarsh) ? this.ch1BasementHarsh(askPrize) : this.ch1BasementSoft();

                case "cGoHill":
                    if (manager.hasVisited(Chapter.STRANGER)) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    } else if (!canStranger) {
                        parser.printDialogueLine("You have already tried that.");
                        break;
                    }

                    switch (this.ch1AttemptStranger(true)) {
                        case 0:
                            return ChapterEnding.TOSTRANGER;
                        case 2:
                        case 1:
                            this.currentLocation = GameLocation.CABIN;
                            this.repeatActiveMenu = false;
                            canStranger = false;
                            break;
                    }
                    
                default:
                    this.giveDefaultFailResponse(this.activeOutcome);
            }
        }

        throw new RuntimeException("No ending reached");
    }

    /**
     * The player attempts to leave and not go to the cabin (leads to Chapter II: The Stranger)
     * @param canCabin whether the player can go to the cabin or the routes are blocked
     * @return 0 if the player commits to the Stranger; 1 if the player returns to the cabin at the first menu; 2 otherwise
     */
    private int ch1AttemptStranger(boolean canCabin) {
        parser.printDialogueLine(new VoiceDialogueLine("Seriously? You're just going to turn around and leave? Do you even know where you're going?"));

        OptionsMenu leaveMenu = new OptionsMenu();
        leaveMenu.add(new Option(this.manager, "ugh", !canCabin, "Okay, fine. You're persistent. I'll go to the cabin and I'll slay the Princess. Ugh!"));
        leaveMenu.add(new Option(this.manager, "maybe", !canCabin, "Okay, fine. I'll go to the cabin and I'll talk to the Princess. Maybe I'll slay her. Maybe I won't. I guess we'll see."));
        leaveMenu.add(new Option(this.manager, "lie", "(Lie) Yes, I definitely know where I'm going."));
        leaveMenu.add(new Option(this.manager, "nope", "Nope!"));
        leaveMenu.add(new Option(this.manager, "notGoing", "The only thing that matters is where I'm not going. (The cabin. I am not going to the cabin.)"));
        leaveMenu.add(new Option(this.manager, "nihilistA", "It's like I said, I'm pretty okay with the world ending. I relish the coming of a new dawn beyond our own.", activeMenu.hasBeenPicked("letItBurn")));
        leaveMenu.add(new Option(this.manager, "nihilistB", "I'm actually pretty okay with the world ending. I relish the coming of a new dawn beyond our own. Gonna go walk in the opposite direction now!", !activeMenu.hasBeenPicked("letItBurn")));
        leaveMenu.add(new Option(this.manager, "quiet", "[Quietly continue down the path away from the cabin.]"));

        boolean repeatMenu = true;
        String outcome;
        while (repeatMenu) {
            outcome = parser.promptOptionsMenu(leaveMenu);
            switch (outcome) {
                case "cGoHill":
                case "ugh":
                    parser.printDialogueLine(new VoiceDialogueLine("*Thank you!* The whole world owes you a debt of gratitude. Really."));
                    return 1;
                case "maybe":
                    parser.printDialogueLine(new VoiceDialogueLine("I guess we will."));
                    return 1;
                case "lie":
                    repeatMenu = false;
                    parser.printDialogueLine(new VoiceDialogueLine("Somehow I doubt that, but fine."));
                    parser.printDialogueLine(new VoiceDialogueLine("I suppose you just quietly continue down the path away from the cabin."));
                    break;
                case "nope":
                    repeatMenu = false;
                    parser.printDialogueLine(new VoiceDialogueLine("Fine, I suppose you just quietly continue down the path away from the cabin."));
                    break;
                case "notGoing":
                    repeatMenu = false;
                    parser.printDialogueLine(new VoiceDialogueLine("Fine, I suppose you just quietly continue down the path away from the cabin."));
                    break;
                case "nihilistA":
                case "nihilistB":
                    repeatMenu = false;
                    parser.printDialogueLine(new VoiceDialogueLine("There won't *be* a \"new dawn\" if the world ends. There'll just be *nothing.* Forever!"));
                    parser.printDialogueLine(new VoiceDialogueLine("Fine, I suppose you just quietly continue down the path away from the cabin."));
                    break;
                case "cGoLeave":
                case "quiet":
                    repeatMenu = false;
                    parser.printDialogueLine(new VoiceDialogueLine("Fine, I suppose you just quietly continue down the path away from the cabin."));
                    break;

                default:
                    this.giveDefaultFailResponse(outcome);
            }
        }
        
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Good. What we're being asked to do here is *wrong.* Better to wash our hands of this whole situation than to take part in it."));
        parser.printDialogueLine(new VoiceDialogueLine("Ignore that annoying little voice. He doesn't know what he's talking about."));

        System.out.println();
        this.currentLocation = GameLocation.HILL;
        parser.printDialogueLine(new DialogueLine("You emerge into a clearing. The path ahead of you winds up a hill, stopping just before a quaint wooden cabin."));
        parser.printDialogueLine(new VoiceDialogueLine("That's strange. It looks like this path also leads to the cabin. How convenient! Everything's back on track again. Maybe the world can still be saved after all."));

        leaveMenu = new OptionsMenu();
        leaveMenu.add(new Option(this.manager, "cabin", !canCabin, "Okay, okay! I'm going into the cabin. Sheesh."));
        leaveMenu.add(new Option(this.manager, "commit", "[Turn around (again) and leave (again).]"));

        repeatMenu = true;
        while (repeatMenu) {
            outcome = parser.promptOptionsMenu(leaveMenu);
            switch (outcome) {
                case "cGoCabin":
                    parser.printDialogueLine(new VoiceDialogueLine("A warning, before you go any further..."));
                    parser.printDialogueLine(new VoiceDialogueLine("She will lie, she will cheat, and she will do everything in her power to stop you from slaying her. Don't believe a word she says."));
                    parser.printDialogueLine(new VoiceDialogueLine("Fortunately, she's only a Princess, whereas you are a valiant and talented warrior. It'll be *easy* so long as you stay focused."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "We can't just go through with this and listen to Him. She's a princess. We're supposed to save princesses, not slay them."));

                    return 2;
                case "cabin":
                    parser.printDialogueLine(new VoiceDialogueLine("That's great to hear! And as long as you bring that fiery attitude to Princess slaying, I think this will all resolve splendidly."));

                    parser.printDialogueLine(new VoiceDialogueLine("A warning, before you go any further..."));
                    parser.printDialogueLine(new VoiceDialogueLine("She will lie, she will cheat, and she will do everything in her power to stop you from slaying her. Don't believe a word she says."));
                    parser.printDialogueLine(new VoiceDialogueLine("Fortunately, she's only a Princess, whereas you are a valiant and talented warrior. It'll be *easy* so long as you stay focused."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "We can't just go through with this and listen to Him. She's a princess. We're supposed to save princesses, not slay them."));

                    return 2;
                case "cGoPath":
                case "commit":
                    repeatMenu = false;
                    break;

                default:
                    this.giveDefaultFailResponse(outcome);
            }
        }

        parser.printDialogueLine(new VoiceDialogueLine("You're really keen on wasting everyone's time, aren't you? It's remarkably selfish, if you ask me. I've already outlined the stakes of the situation. If you don't do your job, everyone dies. Like, *dies* dies. Forever."));

        leaveMenu = new OptionsMenu();
        leaveMenu.add(new Option(this.manager, "dontCare", "I don't care! I'm not killing a princess!"));
        leaveMenu.add(new Option(this.manager, "good", "Good! Maybe everyone *should* die! It's what they get for dumping me in the woods and asking me to *kill* someone for them."));
        leaveMenu.add(new Option(this.manager, "blackmail", "You're not emotionally blackmailing me into doing this!"));
        leaveMenu.add(new Option(this.manager, "quiet", "[Quietly continue down the path.]"));

        repeatMenu = true;
        while (repeatMenu) {
            outcome = parser.promptOptionsMenu(leaveMenu);
            switch (outcome) {
                case "dontCare":
                    repeatMenu = false;
                    parser.printDialogueLine(new VoiceDialogueLine("\"Killing\" is such gauche phrasing, and completely ignores the bigger picture. Your task is to *slay* the Princess. Because she's terrible and she's really got it coming to her."));
                    break;
                case "good":
                    repeatMenu = false;
                    parser.printDialogueLine(new VoiceDialogueLine("When I said everyone, I meant *everyone.* That's a pretty large group to just condemn to death over a single Princess."));
                    parser.printDialogueLine(new VoiceDialogueLine("And last I checked you're a part of everyone, too, so if you think about it, walking up to that cabin and slaying her is really in *your* best interests as well."));
                    break;
                case "blackmail":
                    repeatMenu = false;
                    parser.printDialogueLine(new VoiceDialogueLine("Stakes and consequences aren't emotional blackmail. They're facts of life, and if you had an ounce of maturity you'd understand that."));
                    break;
                case "cGoPath":
                case "quiet":
                    repeatMenu = false;
                    parser.printDialogueLine(new VoiceDialogueLine("Your silence is deafening."));
                    break;

                case "cGoCabin":
                    parser.printDialogueLine(new VoiceDialogueLine("*Finally.* Now that you finally seem to understand the stakes at play here, you can at last get on with your task."));
                    
                    parser.printDialogueLine(new VoiceDialogueLine("A warning, before you go any further..."));
                    parser.printDialogueLine(new VoiceDialogueLine("She will lie, she will cheat, and she will do everything in her power to stop you from slaying her. Don't believe a word she says."));
                    parser.printDialogueLine(new VoiceDialogueLine("Fortunately, she's only a Princess, whereas you are a valiant and talented warrior. It'll be *easy* so long as you stay focused."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "We can't just go through with this and listen to Him. She's a princess. We're supposed to save princesses, not slay them."));

                    return 2;

                default:
                    this.giveDefaultFailResponse(outcome);
            }
        }

        this.currentLocation = GameLocation.LEAVING;
        parser.printDialogueLine(new VoiceDialogueLine("But fine. You turn around and trek back down the path you came."));

        System.out.println();
        parser.printDialogueLine(new DialogueLine("Eventually, the cabin comes into view once again."));
        parser.printDialogueLine(new VoiceDialogueLine("Oh, would you look at that! You're at the cabin again! Now, I'm not normally one for superstition or astrology, but I have to say, it seems like the Universe itself is doing its best to bring you to your fated confrontation with the Princess."));

        leaveMenu = new OptionsMenu();
        leaveMenu.add(new Option(this.manager, "cabin", !canCabin, "There's no fighting this, is there? I have to go into the cabin, don't I? Fine."));
        leaveMenu.add(new Option(this.manager, "commit", "Oh, yeah? Well I guess I start walking in a different direction. Again. In fact, I'm going to just keep trekking through the wilderness until I find a way out of this place.", 0));

        repeatMenu = true;
        while (repeatMenu) {
            outcome = parser.promptOptionsMenu(leaveMenu);
            switch (outcome) {
                case "cGoHill":
                case "cabin":
                    parser.printDialogueLine(new VoiceDialogueLine("There's always a choice, but let me tell you right now that you're making the correct decision for pretty much everyone."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "We can't just go through with this and listen to Him. She's a princess. We're supposed to save princesses, not slay them."));
                    return 2;
                case "cGoLeave":
                case "commit":
                    if (manager.confirmContentWarnings(Chapter.STRANGER)) repeatMenu = false;
                    break;

                default:
                    this.giveDefaultFailResponse(outcome);
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

    /**
     * Runs the beginning of the basement sequence with the soft princess (did not take the blade)
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding ch1BasementSoft() {
        boolean canDamsel = !manager.hasVisited(Chapter.DAMSEL);
        boolean canBeast = !manager.hasVisited(Chapter.BEAST);
        boolean canWitch = !manager.hasVisited(Chapter.WITCH);
        boolean canNightmare = !manager.hasVisited(Chapter.NIGHTMARE);

        boolean canFree = canDamsel || canWitch;
        boolean canNotFree = canBeast || canWitch || canNightmare;
        boolean canGetBlade = canBeast || canWitch;

        this.currentLocation = GameLocation.STAIRS;
        this.withPrincess = true;
        parser.printDialogueLine(new VoiceDialogueLine("The door to the basement creaks open, revealing a staircase faintly illuminated by an unseen light in the room below. This is an oppressive place. The air feels heavy and damp, a hint of rot filtering from the ancient wood. If the Princess really lives here, slaying her is probably doing her a favor."));
        parser.printDialogueLine(new VoiceDialogueLine("Her voice softly carries up the stairs."));
        parser.printDialogueLine(new PrincessDialogueLine("H-hello? Is someone there?"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "It's hypnotizing. It's the kind of voice you only have to hear once to remember it for the rest of your life."));
        parser.printDialogueLine(new VoiceDialogueLine("Don't let it fool you. It's all part of the manipulation. You're playing a dangerous game by coming here unarmed."));

        boolean jokeKill = false;
        boolean hereToSave = false;
        boolean lieSave = false;

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "hi", "\"Hi!\""));
        activeMenu.add(new Option(this.manager, "checkIn", "\"Just checking in on you.\""));
        activeMenu.add(new Option(this.manager, "hereToSave", "\"I'm here to save you!\""));
        activeMenu.add(new Option(this.manager, "lieSave", "(Lie) \"I'm here to save you!\"", canFree));
        activeMenu.add(new Option(this.manager, "jokeKill", "\"Hey, I think I'm here to slay you?\""));
        activeMenu.add(new Option(this.manager, "silent", "[Continue down the stairs.]"));

        this.repeatActiveMenu = true;
        while (this.repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (this.activeOutcome) {
                case "hi":
                    this.repeatActiveMenu = false;
                    parser.printDialogueLine(new PrincessDialogueLine("Don't be a stranger. It's been so long since I've had any visitors. Please, come downstairs."));
                    break;
                case "checkIn":
                    this.repeatActiveMenu = false;
                    parser.printDialogueLine(new PrincessDialogueLine("You are? It's been so long since anyone's come down here. I was starting to think they'd forgotten about me."));
                    break;
                case "hereToSave":
                    this.repeatActiveMenu = false;
                    hereToSave = true;
                    parser.printDialogueLine(new VoiceDialogueLine("How many times do I have to tell you how dangerous letting her out of here would be before it finally sinks in?"));
                    parser.printDialogueLine(new PrincessDialogueLine("Wait, really?! You're here to rescue me? I was starting to think I'd be stuck down here forever!"));
                    parser.printDialogueLine(new PrincessDialogueLine("Come downstairs! I want to see the face of my rescuer."));
                    break;
                case "lieSave":
                    this.repeatActiveMenu = false;
                    hereToSave = true;
                    lieSave = true;
                    parser.printDialogueLine(new PrincessDialogueLine("Wait, really?! You're here to rescue me? I was starting to think I'd be stuck down here forever!"));
                    parser.printDialogueLine(new VoiceDialogueLine("I see, you're trying to get her to lower her guard. It's a gamble, but it might work."));
                    parser.printDialogueLine(new PrincessDialogueLine("Come downstairs! I want to see the face of my rescuer."));
                    break;
                case "jokeKill":
                    this.repeatActiveMenu = false;
                    jokeKill = true;
                    parser.printDialogueLine(new PrincessDialogueLine("Y-you must have the wrong address."));
                    parser.printDialogueLine(new VoiceDialogueLine("Great job, you've given away the element of surprise. Good luck, \"hero.\""));
                    break;
                case "cGoBasement":
                case "silent":
                    this.repeatActiveMenu = false;
                    parser.printDialogueLine(new VoiceDialogueLine("Good. You're still listening to reason. It would be better if you had a weapon, but you may still be able to do what needs to be done."));
                    break;
                
                case "cGoCabin":
                    parser.printDialogueLine(new VoiceDialogueLine("What? No. You're already halfway down the stairs, you can't just turn around now."));
                    break;

                default:
                    this.giveDefaultFailResponse(this.activeOutcome);
            }
        }

        this.currentLocation = GameLocation.BASEMENT;
        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("You walk down the stairs and lock eyes with the Princess. There's a heavy chain around her wrist, binding her to the far wall of the basement."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "She's beautiful. How could someone like this be a threat to anyone?"));
        parser.printDialogueLine(new VoiceDialogueLine("I am *begging* you to stay focused. There's a lot riding on you here."));

        if (jokeKill) {
            parser.printDialogueLine(new PrincessDialogueLine("H—hi. You were joking about coming here to kill me, right? D-do you think you could get me out of these chains?"));
        } else if (hereToSave) {
            parser.printDialogueLine(new PrincessDialogueLine("Hi! I can't believe you're here, I've been waiting for something like this to happen *forever.*"));
            parser.printDialogueLine(new PrincessDialogueLine("... I hope you brought something to deal with these chains."));
            if (lieSave) {
                parser.printDialogueLine(new VoiceDialogueLine("You were lying when you said you were here to rescue her, but regardless of your intentions, breaking her out of those chains would be a big mistake. Don't even try it."));
            } else {
                parser.printDialogueLine(new VoiceDialogueLine("Don't do it. If she gets out of those chains we're all one step closer to The End."));
            }
        } else {
            parser.printDialogueLine(new PrincessDialogueLine("Hi! Do you think you can get me out of these chains?"));
        }

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "talk", "\"Hold on. Let's talk a bit first...\""));
        activeMenu.add(new Option(this.manager, "free", !canFree, "\"I'll see what I can do.\" [Examine the chains.]", 0));

        this.repeatActiveMenu = true;
        while (this.repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (this.activeOutcome) {
                case "talk":
                    this.repeatActiveMenu = false;
                    parser.printDialogueLine(new PrincessDialogueLine("O... kay."));
                    break;
                case "free":
                    if (!manager.confirmContentWarnings("self-mutilation", true)) break;

                    return this.ch1RescueSoft(false, hereToSave && !lieSave, false, false, canFree, canNotFree);
                
                case "cGoStairs":
                    parser.printDialogueLine(new VoiceDialogueLine("What? No. You've hardly even laid eyes on the Princess, you can't just abandon your duty now."));
                    break;

                default:
                    this.giveDefaultFailResponse(this.activeOutcome);
            }
        }

        System.out.println();
        parser.printDialogueLine("You walk a bit closer to the princess and take a seat on the hard stone floor, putting the two of you at eye level. She smiles up at you, a hopeful glimmer in her eyes. She truly is beautiful.");

        OptionsMenu subMenu;
        String outcome = "";
        boolean repeatSub = false;

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "name", "(Explore) \"What's your name?\""));
        activeMenu.add(new Option(this.manager, "name2", "(Explore) \"So is Princess your name?\"", activeMenu.get("name")));
        activeMenu.add(new Option(this.manager, "whyImprisoned", "(Explore) \"I don't know anything about you. For all I know you're locked up down here for a reason.\""));
        activeMenu.add(new Option(this.manager, "notKidding", "(Explore) \"I wasn't kidding when I said I was sent here to kill you. You're apparently going to end the world.\"", jokeKill && !this.knowsDestiny));
        activeMenu.add(new Option(this.manager, "eat", "(Explore) \"If I'm the first person you've seen in a while, what have you been eating? Or drinking?\""));
        activeMenu.add(new Option(this.manager, "shareTask", "(Explore) \"I was sent here to slay you. You're apparently supposed to end the world...\"", !jokeKill && !this.knowsDestiny));
        activeMenu.add(new Option(this.manager, "whatWouldYouDo", "(Explore) \"What are you going to do if I let you out of here?\"", !this.knowsDestiny));
        activeMenu.add(new Option(this.manager, "compromiseA", "\"I won't kill you, but I can't just set you free. It's too risky. What if I stayed for a while and just kept you company? Maybe then everyone could be happy.\"", jokeKill || this.knowsDestiny));
        activeMenu.add(new Option(this.manager, "compromiseB", !canNightmare, "\"I'm going to keep you locked away down here. At least for a little bit. We can get to know each other better while I decide what to do.\" [Keep her locked away.]"));
        activeMenu.add(new Option(this.manager, "getBladeSorry", !canGetBlade, "\"I'm sorry, but I just can't trust you. This doesn't add up, and it isn't worth the risk to take your word over the potential fate of the world.\" [Retrieve the blade.]", 0));
        activeMenu.add(new Option(this.manager, "getBladeSilent", !canGetBlade, "[Go back upstairs to retrieve the blade without saying another word.]", 0));
        activeMenu.add(new Option(this.manager, "free", !canFree, "\"I can't believe they've been keeping you down here like this! I'm getting you out of here.\" [Examine the chains.]", 0));
        activeMenu.add(new Option(this.manager, "freeDontRegret", !canFree, "\"Okay, I'm going to get you out of here. Don't make me regret this.\" [Examine the chains.]", 0));

        this.repeatActiveMenu = true;
        while (this.repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (this.activeOutcome) {
                case "name":
                    parser.printDialogueLine(new PrincessDialogueLine("Oh..."));
                    parser.printDialogueLine(new VoiceDialogueLine("She pauses, carefully formulating her words before she responds."));
                    parser.printDialogueLine(new PrincessDialogueLine("You can address me as 'Your Royal Highness.' Or you can just call me 'Princess' if 'Your Royal Highness' is too formal."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Is \"Princess\" her name or her title? What if it's both? Could you imagine being named Princess Princess?"));
                    break;

                case "name2":
                    parser.printDialogueLine(new PrincessDialogueLine("..."));
                    parser.printDialogueLine(new PrincessDialogueLine("Like I said, you can call me Princess if you'd like!"));
                    parser.printDialogueLine(new PrincessDialogueLine("..."));
                    parser.printDialogueLine(new PrincessDialogueLine("I'm sorry. I've been down here so long I guess I've just forgotten. I must have a name, though! Everyone has a name."));
                    parser.printDialogueLine(new VoiceDialogueLine("She hadn't even thought to pick a name for herself. Hopefully you're starting to see that she can't be trusted. Go back upstairs, get the blade, and slay her before it's too late."));
                    break;

                case "whyImprisoned":
                    parser.printDialogueLine(new PrincessDialogueLine("Of course I'm locked up down here for a reason! ..."));
                    parser.printDialogueLine(new PrincessDialogueLine("I don't actually know what that reason is, but you don't just stuff a Princess in a basement and throw away the key without there being *some* sort of an explanation, right?"));
                    parser.printDialogueLine(new VoiceDialogueLine("You have all the explanation you need. And you should know better than to trust whatever she comes up with."));
                    break;
                    
                case "notKidding":
                    if (this.ch1ShareTaskSoft(false, canFree)) {
                        return this.ch1RescueSoft(true, false, false, true, canFree, canNotFree);
                    } else {
                        if (this.whatWouldYouDo) activeMenu.setCondition("whatWouldYouDo", false);
                    }

                case "eat":
                    parser.printDialogueLine(new PrincessDialogueLine("I don't see what that has to do with anything."));
                    parser.printDialogueLine(new VoiceDialogueLine("This is the only time this is ever going to happen, but I agree with the Princess. That's hardly relevant."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Okay but actually, what *has* she been eating? She has to eat, right?"));
                    break;

                case "shareTask":
                    subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "notDanger", "\"But I don't think you're actually dangerous.\""));
                    subMenu.add(new Option(this.manager, "notSure", "\"But I wanted to see you for myself. I'm still not sure what to believe.\""));
                    subMenu.add(new Option(this.manager, "notRight", "\"I'm starting to think it's true. There's something about you that doesn't feel right.\""));

                    switch (parser.promptOptionsMenu(subMenu)) {
                        case "notDanger":
                        case "notSure":
                            if (this.ch1ShareTaskSoft(false, canFree)) {
                                return this.ch1RescueSoft(true, hereToSave && !lieSave, false, true, canFree, canNotFree);
                            } else {
                                if (this.whatWouldYouDo) activeMenu.setCondition("whatWouldYouDo", false);
                            }
                            
                        case "notRight":
                            parser.printDialogueLine(new PrincessDialogueLine("Would everything feel right about you if you were locked away in a hole by yourself for as long as you can remember?"));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Just how long *has* she been down here?"));
                            parser.printDialogueLine(new PrincessDialogueLine("So... did they tell you why I'm supposed to be so dangerous?"));

                            if (this.ch1ShareTaskSoft(true, canFree)) {
                                return this.ch1RescueSoft(true, hereToSave && !lieSave, false, true, canFree, canNotFree);
                            } else {
                                if (this.whatWouldYouDo) activeMenu.setCondition("whatWouldYouDo", false);
                            }
                    }

                    break;

                case "whatWouldYouDo":
                    this.whatWouldYouDo = true;
                    parser.printDialogueLine(new VoiceDialogueLine("The Princess hesitates before responding."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "She doesn't know. She's been down here too long to have any idea of what she'd do in another life."));
                    parser.printDialogueLine(new VoiceDialogueLine("She knows what she'd do. She's just searching for whatever answer she thinks you want to hear."));
                    parser.printDialogueLine(new PrincessDialogueLine("Are you looking for the truth, or are you looking for the 'right' answer? Because with the dynamic we have going on here I don't think the specifics of what I'd 'do' really matter."));
                    parser.printDialogueLine(new PrincessDialogueLine("It's not like you'd believe me."));
                    break;

                case "compromiseA":
                case "compromiseB":
                    this.repeatActiveMenu = false;

                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "That seems like a pretty good compromise."));
                    if (jokeKill && !this.knowsDestiny) {
                        parser.printDialogueLine(new PrincessDialogueLine("So you weren't kidding on the stairs. I thought you were just making a bad joke. You were actually sent here to kill me?"));
                    } else {
                        parser.printDialogueLine(new PrincessDialogueLine("I don't think I could bear being down here that much longer."));
                    }
                    parser.printDialogueLine(new VoiceDialogueLine("Leaving her alive is too risky. If you don't deal with her soon, she *will* find a way out."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "So I'm the only one who liked that idea? *Sigh.*"));
                    parser.printDialogueLine(new PrincessDialogueLine("One way or another, I'm going to find a way out of here. It would make it easier for both of us if you'd help."));

                    manager.setNowPlaying("The World Ender", true);
                    parser.printDialogueLine(new PrincessDialogueLine("But if you don't, I can promise that you'll regret that decision."));
                    parser.printDialogueLine(new VoiceDialogueLine("You have to make a choice. Let's hope for all our sakes it's the right one."));

                    subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "getBlade", !canGetBlade, "[Retrieve the blade.]", 0));
                    subMenu.add(new Option(this.manager, "free", !canFree, "\"Okay. Let's get you out of here.\" [Examine the chains.]", 0));
                    subMenu.add(new Option(this.manager, "lock", "[Lock her in the basement.]", Chapter.NIGHTMARE));
                    
                    repeatSub = true;
                    while (repeatSub) {
                        switch (parser.promptOptionsMenu(subMenu)) {
                            case "getBlade":
                                if (!manager.confirmContentWarnings("mutilation", true)) break;

                                parser.printDialogueLine(new VoiceDialogueLine("*Thank* you."));
                                parser.printDialogueLine(new VoiceDialogueLine("You turn back to the stairs, intent on retrieving the blade in the cabin."));
                                parser.printDialogueLine(new PrincessDialogueLine("Where are you going?! You can't just leave me here!"));
                                
                                manager.setNowPlaying("The World Ender", true);
                                parser.printDialogueLine(new PrincessDialogueLine("Fine! Turn your back on me! But it won't be long before I slip these chains, and once I'm out of here? There'll be *hell* to pay for leaving me behind!"));
                                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "\"Slip these chains?\" She can't, right? She needed our help to get out of here. But do you hear the conviction in her voice? I don't think she's bluffing."));
                                parser.printDialogueLine(new VoiceDialogueLine("She has to be bluffing. But... hurry."));

                                System.out.println();
                                return this.ch1RetrieveBlade(false);

                            case "free":
                                if (!manager.confirmContentWarnings("self-mutilation", true)) break;

                                parser.printDialogueLine(new VoiceDialogueLine("You can't be *serious* --"));
                                parser.printDialogueLine(new PrincessDialogueLine("Thank you, thank you! You won't regret this, I promise."));
                                parser.printDialogueLine(new VoiceDialogueLine("You're making a huge mistake."));
                                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "No. I think you're doing the right thing."));

                                return this.ch1RescueSoft(true, hereToSave && !lieSave, false, true, canFree, canNotFree);

                            case "lock":
                                if (manager.confirmContentWarnings(Chapter.NIGHTMARE)) repeatSub = false;
                                break;
                        }
                    }

                    // Lock continues here
                    parser.printDialogueLine(new VoiceDialogueLine("I know you think this is some kind of fair compromise, but it isn't. *No one* wins here."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "It's a chance we'll have to take. We can make this work. If we just stay here and keep watch, no one has to die."));
                    parser.printDialogueLine(new PrincessDialogueLine("Where are you going?! You can't just leave me here!"));
                    parser.printDialogueLine(new VoiceDialogueLine("You turn your back to the Princess and make your way back to the stairs."));
                    parser.printDialogueLine(new PrincessDialogueLine("Fine! Turn your back on me. But it won't be long before I slip these chains. And once I'm out of here, there will be *hell* to pay for leaving me behind."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "\"Slip these chains?\" She can't, right? She needed our help to get out of here. But do you hear the conviction in her voice? I don't think she's bluffing."));
                    parser.printDialogueLine(new VoiceDialogueLine("Either way, she dropped the mask, didn't she? You can still grab the blade and get back down here..."));

                    subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "lock", "No, we're sticking to the plan and locking her away."));
                    subMenu.add(new Option(this.manager, "slay", !canGetBlade, "Oh that's a relief! I was afraid I'd already committed to not slaying her.", 0));

                    repeatSub = true;
                    while (repeatSub) {
                        switch (parser.promptOptionsMenu(subMenu)) {
                            case "lock":
                                parser.printDialogueLine(new VoiceDialogueLine("You'll be the death of all of us, but fine. We'll do it your way."));

                                System.out.println();
                                this.ch1ToNightmare(false, false);
                                return ChapterEnding.TONIGHTMARE;

                            case "slay":
                                if (!manager.confirmContentWarnings("mutilation", true)) break;

                                parser.printDialogueLine(new VoiceDialogueLine("It's never too late to do the right thing. Now hurry."));

                                System.out.println();
                                return this.ch1RetrieveBlade(false);
                        }
                    }
                    
                
                case "getBladeSorry":
                    if (!manager.confirmContentWarnings("mutilation", true)) break;

                    this.repeatActiveMenu = false;
                    
                    parser.printDialogueLine(new VoiceDialogueLine("*Thank* you."));
                    parser.printDialogueLine(new VoiceDialogueLine("You turn back to the stairs, intent on retrieving the blade in the cabin."));
                    if (jokeKill && !this.knowsDestiny) {
                        parser.printDialogueLine(new PrincessDialogueLine("So you weren't kidding... I thought you were just making a bad joke. You were *actually* sent here to kill me."));
                    } else if (!this.knowsDestiny) {
                        parser.printDialogueLine(new PrincessDialogueLine("So what? You're going to try and kill me?"));
                    }
                    parser.printDialogueLine(new PrincessDialogueLine("You'll regret this. I promise you. But go ahead. Run along and get whatever you're planning to get."));
                    
                    manager.setNowPlaying("The World Ender", true);
                    parser.printDialogueLine(new PrincessDialogueLine("But you'd better hope that I don't slip these chains before you make it back down here."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "\"Slip these chains?\" She can't, right? She needed our help to get out of here. But do you hear the conviction in her voice? I don't think she's bluffing."));
                    parser.printDialogueLine(new VoiceDialogueLine("She has to be bluffing. But... hurry."));

                    System.out.println();
                    return this.ch1RetrieveBlade(true);
                
                case "cGoStairs":
                    parser.printDialogueLine(new VoiceDialogueLine("Oh? Have you finally decided to take the blade and slay her? Maybe the world isn't doomed after all."));

                    subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "slay", !canGetBlade, "Yes. Something here just doesn't add up, and it isn't worth the risk to take her word over the potential fate of the world. [Retrieve the blade.]"));
                    subMenu.add(new Option(this.manager, "lock", !canNightmare, "No, but I can't just set her free. I don't have enough information to make a decision yet. I'm going to keep her locked away down here, at least for a little bit. We can get to know each other better while I decide what to do. [Keep her locked away.]", 0));
                    subMenu.add(new Option(this.manager, "nevermind", "No. Not yet. Actually, I still have a few more questions for her before I make a decision. [Turn back.]"));

                    repeatSub = true;
                    while (repeatSub) {
                        outcome = parser.promptOptionsMenu(subMenu);
                        switch (outcome) {
                            case "slay":
                                if (!manager.confirmContentWarnings("mutilation", true)) break;

                                repeatSub = false;
                                break;
                                
                            case "lock":
                                if (!manager.confirmContentWarnings(Chapter.NIGHTMARE)) break;

                                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "That seems like a pretty good compromise."));
                                parser.printDialogueLine(new VoiceDialogueLine("Leaving her alive is too risky. If you don't deal with her soon, she *will* find a way out."));
                                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "It's a chance we'll have to take. We can make this work. If we just stay here and keep watch, no one has to die."));
                                parser.printDialogueLine(new PrincessDialogueLine("Where are you going?! You can't just leave me here!"));
                                parser.printDialogueLine(new VoiceDialogueLine("You turn your back to the Princess and make your way back to the stairs."));
                                parser.printDialogueLine(new PrincessDialogueLine("Fine! Turn your back on me. But it won't be long before I slip these chains. And once I'm out of here, there will be *hell* to pay for leaving me behind."));
                                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "\"Slip these chains?\" She can't, right? She needed our help to get out of here. But do you hear the conviction in her voice? I don't think she's bluffing."));
                                parser.printDialogueLine(new VoiceDialogueLine("Either way, she dropped the mask, didn't she? You can still grab the blade and get back down here..."));

                                subMenu = new OptionsMenu(true);
                                subMenu.add(new Option(this.manager, "lock", "No, we're sticking to the plan and locking her away."));
                                subMenu.add(new Option(this.manager, "slay", !canGetBlade, "Oh that's a relief! I was afraid I'd already committed to not slaying her.", 0));

                                repeatSub = true;
                                while (repeatSub) {
                                    switch (parser.promptOptionsMenu(subMenu)) {
                                        case "lock":
                                            parser.printDialogueLine(new VoiceDialogueLine("You'll be the death of all of us, but fine. We'll do it your way."));

                                            System.out.println();
                                            this.ch1ToNightmare(false, false);
                                            return ChapterEnding.TONIGHTMARE;

                                        case "slay":
                                            if (!manager.confirmContentWarnings("mutilation", true)) break;

                                            parser.printDialogueLine(new VoiceDialogueLine("It's never too late to do the right thing. Now hurry."));

                                            System.out.println();
                                            return this.ch1RetrieveBlade(false);
                                    }
                                }
                                
                                break; // Should be unreachable

                            case "nevermind":
                                repeatSub = false;
                                break;
                        }
                    }

                    if (!outcome.equals("slay")) break;

                case "getBladeSilent":
                    if (!manager.confirmContentWarnings("mutilation", true)) break;

                    this.repeatActiveMenu = false;

                    parser.printDialogueLine(new VoiceDialogueLine("*Thank* you."));
                    parser.printDialogueLine(new VoiceDialogueLine("You turn back to the stairs, intent on retrieving the blade from the cabin."));
                    parser.printDialogueLine(new PrincessDialogueLine("Where are you going?! You can't just leave me here!"));
                    parser.printDialogueLine(new PrincessDialogueLine("You'd better hope for your own sake that I don't slip these chains before you make it back down here."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "\"Slip these chains?\" She can't, right? She needed our help to get out of here. But do you hear the conviction in her voice? I don't think she's bluffing."));
                    parser.printDialogueLine(new VoiceDialogueLine("She has to be bluffing. But... I'd hurry if I were you."));

                    System.out.println();
                    return this.ch1RetrieveBlade(false);
                
                case "freeDontRegret":
                    if (!manager.confirmContentWarnings("self-mutilation", true)) break;

                    return this.ch1RescueSoft(false, hereToSave && !lieSave, true, true, canFree, canNotFree);
                case "free":
                    if (!manager.confirmContentWarnings("self-mutilation", true)) break;

                    return this.ch1RescueSoft(false, hereToSave && !lieSave, false, true, canFree, canNotFree);

                default:
                    this.giveDefaultFailResponse(activeOutcome);
            }
        }

        throw new RuntimeException("No ending reached");
    }

    /**
     * The player tells the soft Princess that she's allegedly going to end the world
     * @param joinLate whether to skip the first block of dialogue or not
     * @param canFree whether the player can free the Princess or the routes are blocked
     * @return true if the player chooses to free the Princess; false otherwise
     */
    private boolean ch1ShareTaskSoft(boolean joinLate, boolean canFree) {
        this.knowsDestiny = true;

        if (!joinLate) {
            parser.printDialogueLine(new PrincessDialogueLine("I-is that why they threw me down here? But I don't want to hurt anyone. I like the world! I think."));
            parser.printDialogueLine(new PrincessDialogueLine("I don't remember much about it, to be honest. I've been down here for so long."));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "That's... How long has she been locked away?!"));
            parser.printDialogueLine(new PrincessDialogueLine("Did they tell you *how* I'm supposed to end the world?"));
        }

        OptionsMenu shareMenu = new OptionsMenu(true);
        shareMenu.add(new Option(this.manager, "deflect", "(Deflect) \"What are you going to do if I let you out of here?\"", !this.whatWouldYouDo));
        shareMenu.add(new Option(this.manager, "enough", "\"I've been told enough.\""));
        shareMenu.add(new Option(this.manager, "youTell", "\"I was hoping you'd tell me.\""));
        shareMenu.add(new Option(this.manager, "reasons", "\"No. But I'm sure they have their reasons for keeping that information secret from me.\""));
        shareMenu.add(new Option(this.manager, "noDanger", "\"No. Which is why I don't think you're actually dangerous.\""));
        shareMenu.add(new Option(this.manager, "silent", "[Remain silent.]"));

        boolean repeatMenu = true;
        String outcome;
        while (repeatMenu) {
            outcome = parser.promptOptionsMenu(shareMenu);
            switch (outcome) {
                case "deflect":
                    repeatMenu = false;
                    this.whatWouldYouDo = true;
                    parser.printDialogueLine(new VoiceDialogueLine("The Princess hesitates before responding."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "She doesn't know. She's been down here too long to have any idea of what she'd do in another life."));
                    parser.printDialogueLine(new VoiceDialogueLine("She knows what she'd do. She's just searching for whatever answer she thinks you want to hear."));
                    parser.printDialogueLine(new PrincessDialogueLine("What I'd do doesn't really matter, right?"));
                    break;
                case "youTell":
                    repeatMenu = false;
                    parser.printDialogueLine(new PrincessDialogueLine("I don't know how to destroy the world, if that's what you're getting at."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I believe her."));
                    parser.printDialogueLine(new VoiceDialogueLine("She doesn't have to know how to destroy the world to be capable of doing it."));
                    break;
                case "reasons":
                    repeatMenu = false;
                    parser.printDialogueLine(new VoiceDialogueLine("I appreciate the vote of confidence."));
                    parser.printDialogueLine(new PrincessDialogueLine("What if they're bad reasons, though? If they had *good* reasons for thinking I was dangerous, wouldn't they have shared them with you? I don't want to hurt anyone. I just want to leave."));
                    break;
                case "noDanger":
                    repeatMenu = false;
                    parser.printDialogueLine(new VoiceDialogueLine("Sooner or later you'll learn to trust me. Hopefully it won't be too late when you finally come around."));
                    parser.printDialogueLine(new PrincessDialogueLine("Thank you for believing me. Now can you help me get out of here?"));

                    OptionsMenu finalShareMenu = new OptionsMenu(true);
                    finalShareMenu.add(new Option(this.manager, "talk", "\"I still have a few more questions before we leave.\""));
                    finalShareMenu.add(new Option(this.manager, "free", !canFree, "\"I'll see what I can do.\" [Examine the chains.]", 0));

                    repeatMenu = true;
                    while (repeatMenu) {
                       switch (parser.promptOptionsMenu(finalShareMenu)) {
                            case "talk":
                                parser.printDialogueLine(new PrincessDialogueLine("There's going to be plenty of time to chat after I'm free, but okay. What do you want to know?"));
                                return false;
                            case "free":
                                if (!manager.confirmContentWarnings("self-mutilation", true)) break;
                                return true;
                        } 
                    }

                case "enough":
                    parser.printDialogueLine(new VoiceDialogueLine("I appreciate the vote of confidence."));
                case "silent":
                    repeatMenu = false;
                    parser.printDialogueLine(new PrincessDialogueLine("They haven't shared a thing, have they? All they've done is point a finger."));
                    break;
            }
        }

        parser.printDialogueLine(new PrincessDialogueLine("At the end of the day, whatever the two of us have going on down here is about trust."));
        parser.printDialogueLine(new PrincessDialogueLine("Whoever sent you to 'slay' me *claimed* I was a threat to the world, but they didn't tell you why."));
        parser.printDialogueLine(new PrincessDialogueLine("I don't trust that, and I don't think you do, either, or you wouldn't have come down here to talk."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "She has a point. We're talking like this for a reason."));
        parser.printDialogueLine(new PrincessDialogueLine("So this shouldn't be about what I'd do if I got out of here, or me saying the right thing to convince you to save me..."));
        parser.printDialogueLine(new PrincessDialogueLine("This is about how *messed up* this whole situation is! This is my life we're talking about!"));
        parser.printDialogueLine(new PrincessDialogueLine("Do you really think I can even end the world? Why would I even want to?"));
        parser.printDialogueLine(new PrincessDialogueLine("We both know that if there's people we can't trust in this situation, it's whoever locked me down here, and it's whoever sent you here. And those two groups are probably one and the same."));
        parser.printDialogueLine(new VoiceDialogueLine("Don't let her turn the tables here. This isn't about trust. This is about *risk.* We stand to lose everything, all for the sake of one person. And a subjugating *monarch,* no less."));

        return false;
    }

    /**
     * The player attempts to free the soft Princess
     * @param lateJoin whether to skip the first block of dialogue or not
     * @param hereToSaveTruth whether the player chose "I'm here to save you!" on the stairs and meant it
     * @param dontRegret whether the player said "don't make me regret it"
     * @param talked whether the player talked to the Princess or immediately decided to free her
     * @param canFree whether the player can free the Princess or the route is blocked
     * @param canNotFree whether the player can not free the Princess or the routes are blocked
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding ch1RescueSoft(boolean lateJoin, boolean hereToSaveTruth, boolean dontRegret, boolean talked, boolean canFree, boolean canNotFree) {
        if (!lateJoin) {
            parser.printDialogueLine(new VoiceDialogueLine("You're only making this more difficult..."));
            if (dontRegret) parser.printDialogueLine(new PrincessDialogueLine("Thank you, and you won't! I promise!"));
            else parser.printDialogueLine(new PrincessDialogueLine("Thank you, thank you!"));

            parser.printDialogueLine(new VoiceDialogueLine("You're making a huge mistake."));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "No. you're doing the right thing."));
        }

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("You walk up to the chains binding the Princess to the wall and give them a tug."));
        parser.printDialogueLine(new VoiceDialogueLine("They're large and heavy, far too solid for you to even imagine trying to break them apart."));
        parser.printDialogueLine(new PrincessDialogueLine("I'm guessing you don't have the key."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Maybe it's somewhere upstairs."));
        parser.printDialogueLine(new VoiceDialogueLine("Doubtful. Whoever locked the Princess away down here intended for her to never see the light of day. They wouldn't have just left the key to her chains somewhere in the cabin."));

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "whatIf", "\"And if there isn't a key... do you have any other ideas?\""));
        activeMenu.add(new Option(this.manager, "check", "\"I'm going to check upstairs. Maybe the key's still lying around somewhere up there. And if not, maybe I can at least find something to break you free.\""));

        switch (parser.promptOptionsMenu(activeMenu)) {
            case "whatIf":
                parser.printDialogueLine(new PrincessDialogueLine("Maybe there's some way to break the chains?"));
                parser.printDialogueLine(new PrincessDialogueLine("..."));
                parser.printDialogueLine(new PrincessDialogueLine("Or if that doesn't work I guess we can always cut me out of them."));
                parser.printDialogueLine(new VoiceDialogueLine("She offers the suggestion with almost complete nonchalance."));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "If we were stuck down here long enough, I'm sure we'd be nonchalant about cutting our way out if it meant we could finally be free."));
                break;

            case "check":
                parser.printDialogueLine(new PrincessDialogueLine("Okay. I'll be here. Good luck."));
                break;
        }
        
        System.out.println();
        this.currentLocation = GameLocation.STAIRS;
        this.reverseDirection = true;
        this.withPrincess = false;
        parser.printDialogueLine(new VoiceDialogueLine("You attempt to make your way out of the basement, but the door at the top of the stairs slams shut. You hear the click of a lock sliding into place."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Is someone else here?"));

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "shout", "(Explore) \"Hey! Let me out of here!\""));
        activeMenu.add(new Option(this.manager, "try", "(Explore) [Try the door.]"));
        activeMenu.add(new Option(this.manager, "return", "[Return to the bottom of the stairs.]"));

        boolean triedDoor = false;
        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "shout":
                    parser.printDialogueLine(new VoiceDialogueLine("Your shouts and pleas are met with silence."));
                    parser.printDialogueLine(new VoiceDialogueLine("You're here to slay the Princess, and you won't leave until the task is done."));
                    break;

                case "cGoCabin":
                    activeMenu.setCondition("try", false);
                case "try":
                    if (triedDoor) {
                        parser.printDialogueLine(new VoiceDialogueLine("You try the door again. It's still locked."));
                    } else {
                        parser.printDialogueLine(new VoiceDialogueLine("You try the door, but it's locked from the outside."));
                    }
                    triedDoor = true;
                    break;

                case "cGoBasement":
                case "return":
                    this.repeatActiveMenu = false;
                    break;
                
                case "cSlayNoPrincessFail":
                    parser.printDialogueLine(new VoiceDialogueLine("The Princess is in the basement below, remember? You can't slay her from here."));
                    break;

                default:
                    this.giveDefaultFailResponse(activeOutcome);
            }
        }

        this.currentLocation = GameLocation.BASEMENT;
        this.reverseDirection = false;
        this.withPrincess = true;

        parser.printDialogueLine(new PrincessDialogueLine("I heard the door slam... they locked you down here too, didn't they?"));
        parser.printDialogueLine(new VoiceDialogueLine("There's a slight panic rising in the Princess's voice."));
        parser.printDialogueLine(new PrincessDialogueLine("If I could just get out of these chains I *know* we could force our way out of here together."));

        manager.setNowPlaying("The World Ender", true);
        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("She barely hesitates before raising her arm to her mouth, her teeth tearing through her limb with the determination of a trapped wolf."));
        parser.printDialogueLine(new VoiceDialogueLine("As she rips her flesh from her bone, a sound comes from behind you. The clang of bouncing metal."));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("It's the blade from upstairs. You're not sure how it made its way down here, but if there's a time to strike, it's now."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Or we could use it to free her."));
        parser.printDialogueLine(new VoiceDialogueLine("You won't like what happens if you do that."));

        this.hasBlade = true;
        this.canSlayPrincess = true;

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "save", !canFree, "[Save the Princess.]", 0));
        activeMenu.add(new Option(this.manager, "slay", !canNotFree || hereToSaveTruth, "[Slay the Princess.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "save":
                    if (!manager.confirmContentWarnings("mutilation; loss of bodily autonomy", true)) break;

                    this.repeatActiveMenu = false;
                    break;

                case "cSlayPrincess":
                    if (!canNotFree || hereToSaveTruth) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                case "slay":
                    return this.ch1RescueSlaySoft();

                default:
                    this.giveDefaultFailResponse();
            }
        }

        // Save continues here
        this.canSlayPrincess = false;
        parser.printDialogueLine(new VoiceDialogueLine("Ugh. Fine."));
        parser.printDialogueLine(new VoiceDialogueLine("Against your better judgment, you place the blade against the ragged, self-inflicted wound on the Princess' arm, just above the unyielding chain binding her to this place."));
        parser.printDialogueLine(new VoiceDialogueLine("You cut into her flesh."));
        parser.printDialogueLine(new VoiceDialogueLine("The blade is sharp, and it takes little effort to crack through the bone of her arm."));
        parser.printDialogueLine(new VoiceDialogueLine("Her limb falls to the ground, and the heavy chains follow suit."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "She didn't so much as utter a sound through that whole ordeal."));
        parser.printDialogueLine(new VoiceDialogueLine("No. She didn't."));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("She smiles softly as her gaze meets yours, blood from her wounded arm dripping rhythmically to the ground."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "How is she still smiling after everything? It's like she isn't even bothered by what just happened."));
        parser.printDialogueLine(new PrincessDialogueLine("Thank you. Now let's get out of here."));

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "door", "[Approach the locked door.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "cGoStairs":
                case "door":
                    this.repeatActiveMenu = false;
                    break;
                    
                default:
                    this.giveDefaultFailResponse(activeOutcome);
            }
        }
        
        parser.printDialogueLine(new VoiceDialogueLine("No. We won't have any of that. The stakes are too high. You can't just let her escape into the world."));
        parser.printDialogueLine(new VoiceDialogueLine("... no. *I* can't just let her escape into the world."));
        parser.printDialogueLine(new VoiceDialogueLine("As the Princess approaches the bottom stair, your body steps forward and raises the blade."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Wait... this isn't fair. You can't just *do* that!"));
        parser.printDialogueLine(new VoiceDialogueLine("Watch me."));

        System.out.println();
        parser.printDialogueLine(new PrincessDialogueLine("Wh-what are you doing?"));

        this.canSlayPrincess = true;
        Option slay = new Option(this.manager, "slay", manager.hasVisited(Chapter.WITCH), "[Slay the Princess.]", 0);

        this.activeMenu = new OptionsMenu();
        for (int i = 0; i < 13; i++) activeMenu.add(slay, "slay" + i);
        activeMenu.add(new Option(this.manager, "warn", manager.hasVisited(Chapter.DAMSEL), "[Warn her.]"));
        for (int i = 0; i < 4; i++) activeMenu.add(slay, "slay" + (12 + i));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "cSlayPrincess":
                    if (manager.hasVisited(Chapter.WITCH)) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                case "slay0":
                case "slay1":
                case "slay2":
                case "slay3":
                case "slay4":
                case "slay5":
                case "slay6":
                case "slay7":
                case "slay8":
                case "slay9":
                case "slay10":
                case "slay11":
                case "slay12":
                case "slay13":
                case "slay14":
                case "slay15":
                case "slay16":
                case "slay17":
                case "slay18":
                    if (!manager.confirmContentWarnings(Chapter.WITCH)) {
                        break;
                    }
                    
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Okay. There's no going back now. I'm with you to the end."));

                    System.out.println();
                    parser.printDialogueLine(new VoiceDialogueLine("You bring the blade down to strike at the Princess's heart."));
                    parser.printDialogueLine(new VoiceDialogueLine("But she's fast. She ducks to the floor, your blade narrowly grazing her backside. Slaying her won't be easy now that she's free."));
                    parser.printDialogueLine(new PrincessDialogueLine("We could have gotten out of here together! Were you just lying to me this whole time?"));
                    parser.printDialogueLine(new PrincessDialogueLine("I don't know what's come over you, but if I have to kill you, then I'll kill you. Do you think I need both of my arms to do that?"));
                    
                    this.ch1RescueControlledSlaySoft();
                    return ChapterEnding.TOWITCH;

                case "warn":
                    this.repeatActiveMenu = false;
                    break;

                default:
                    this.giveDefaultFailResponse();
            }
        }

        parser.printDialogueLine(new VoiceDialogueLine("Stop that."));
        parser.printDialogueLine(new PrincessDialogueLine("Something's come over you, hasn't it? Y-you know you don't have to do this, right?"));
        parser.printDialogueLine(new VoiceDialogueLine("Your body lunges forward, the blade held low, ready to sink into her heart."));
        parser.printDialogueLine(new VoiceDialogueLine("But the Princess dodges, stumbling back against the wall before the blade has a chance to connect."));
        parser.printDialogueLine(new VoiceDialogueLine("Stop it! Stop trying to resist me! I'm trying to get you out of here alive."));
        
        this.activeMenu = new OptionsMenu();
        for (int i = 0; i < 13; i++) activeMenu.add(slay, "slay" + i);
        activeMenu.add(new Option(this.manager, "resist", manager.hasVisited(Chapter.DAMSEL), "[Resist.]", 0));
        for (int i = 0; i < 4; i++) activeMenu.add(slay, "slay" + (12 + i));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "cSlayPrincess":
                    if (manager.hasVisited(Chapter.WITCH)) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                case "slay0":
                case "slay1":
                case "slay2":
                case "slay3":
                case "slay4":
                case "slay5":
                case "slay6":
                case "slay7":
                case "slay8":
                case "slay9":
                case "slay10":
                case "slay11":
                case "slay12":
                case "slay13":
                case "slay14":
                case "slay15":
                case "slay16":
                case "slay17":
                case "slay18":
                    if (!manager.confirmContentWarnings(Chapter.WITCH)) {
                        break;
                    }
                    
                    parser.printDialogueLine(new VoiceDialogueLine("*Thank* you."));
                    parser.printDialogueLine(new PrincessDialogueLine("There's no getting through to you right now, is there?"));
                    parser.printDialogueLine(new PrincessDialogueLine("A betrayal of will is still a betrayal. You'll regret thinking of me as a helpless damsel."));

                    this.ch1RescueControlledSlaySoft();
                    return ChapterEnding.TOWITCH;

                case "resist":
                    if (!manager.confirmContentWarnings(Chapter.DAMSEL)) {
                        break;
                    }

                    this.repeatActiveMenu = false;
                    break;

                default:
                    this.giveDefaultFailResponse();
            }
        }

        // Committed to Damsel
        parser.printDialogueLine(new VoiceDialogueLine("The blade! Move. The. *Blade!*"));
        parser.printDialogueLine(new VoiceDialogueLine("As your body remains frozen in stubborn resistance, the Princess takes a cautious step forward."));
        parser.printDialogueLine(new PrincessDialogueLine("We both know this isn't you..."));
        this.hasBlade = false;
        parser.printDialogueLine(new VoiceDialogueLine("She nervously reaches towards you and takes the blade from your infuriatingly rigid hands... What are you *doing?*"));
        parser.printDialogueLine(new PrincessDialogueLine("I'm sorry... I'll try to be quick."));
        parser.printDialogueLine(new VoiceDialogueLine("She plunges it into your chest, tearing through flesh and sinew. It is *agony.* But you aren't dead yet."));
        parser.printDialogueLine(new PrincessDialogueLine("Oh no, I'm so sorry!"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Stay strong. We can tough it out until it's done. For her sake."));
        parser.printDialogueLine(new VoiceDialogueLine("For *her* sake? Don't you start pretending that dying a painful death is some sort of heroic gesture. The two of you have literally doomed *everyone.*"));
        parser.printDialogueLine(new VoiceDialogueLine("Whatever. She sinks the blade into your chest again, and again, and again... and you feel *every inch* of burning pain that slices its way into your body."));
        parser.printDialogueLine(new PrincessDialogueLine("I'm sorry I'm sorry I'm sorry!"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "She doesn't know how to use a knife, does she?"));
        parser.printDialogueLine(new VoiceDialogueLine("Apparently not, though it doesn't matter how sloppy her blade work is, does it? A stab wound is still a stab wound, and it won't be long before you bleed out."));
        parser.printDialogueLine(new PrincessDialogueLine("I'm so sorry!"));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("With one last thrust of the knife, your legs give out beneath you. You collapse to the floor, your blood pooling around you, your limbs unresponsive. The Princess stares down at your ruined chest as tears carve rivulets of pink down her blood-spattered cheeks."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "It can't just end like this, right?"));
        parser.printDialogueLine(new VoiceDialogueLine("Oh, that's rich coming from you. As much as I'd prefer for things to have gone differently, I can't deny the reality of what's happened. The two of you made your choice. It's over."));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("Everything goes dark, and you die."));

        return ChapterEnding.TODAMSEL;
    }

    /**
     * The player allows the Narrator to take control of their body and slay the soft Princess after setting her free (leads to Chapter II: the Witch)
     */
    private void ch1RescueControlledSlaySoft() {
        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("She pounces on you with the same animal ferocity she used to tear through her arm."));
        parser.printDialogueLine(new VoiceDialogueLine("But you have a weapon. You raise the blade, digging it under her ribs, aiming directly for the heart."));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("It's not enough to stop her. You feel her claws on your throat, then her teeth, somehow sharp enough to pull apart your flesh and sinew with ease."));
        parser.printDialogueLine(new VoiceDialogueLine("You collapse to the floor, your body unresponsive as your blood pools on the ground beneath you. She stares down at your ravaged form, eyes shining in the darkness, dress stained in red as her blood and yours both seep into the fabric."));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("If we're lucky, the wound you managed to inflict will be enough to at least delay her escape from this place. If we're very lucky, it will kill her before she can reach the outside world."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "It can't just end like this, right?"));
        parser.printDialogueLine(new VoiceDialogueLine("As much as I'd prefer for things to have gone differently, I can't deny the reality of what's happened. I'm sorry, but it's over."));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("Everything goes dark, and you die."));
    }

    /**
     * The player decides to slay the soft Princess when the blade falls into the basement after initially deciding to free her (leads to Chapter II: The Witch)
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding ch1RescueSlaySoft() {
        parser.printDialogueLine(new VoiceDialogueLine("Without hesitation, you bring the blade down and plunge it into the Princess's back. *Finally.*"));
        parser.printDialogueLine(new VoiceDialogueLine("The wound drives her to the ground."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Okay. There's no going back now. I'm with you to the end."));

        System.out.println();
        parser.printDialogueLine(new PrincessDialogueLine("You- you bastard! Were you lying to me this whole time?"));
        parser.printDialogueLine(new VoiceDialogueLine("The Princess pushes away from you, the motion ripping the blade from her back."));
        parser.printDialogueLine(new VoiceDialogueLine("Wounded, but still alive, she crouches on all fours in the corner of the room and meets your eyes with the ferocity of a cornered predator."));
        parser.printDialogueLine(new PrincessDialogueLine("You've made a terrible enemy, and there's nothing in the world that can possibly save you from me."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I thought we had the upper hand, but it's as if she's barely even threatened by us."));
        parser.printDialogueLine(new VoiceDialogueLine("It's an act. She's wounded and unarmed. There's nothing she can do to hurt you."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I'm not so sure..."));
        parser.printDialogueLine(new VoiceDialogueLine("Don't waver now."));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("As you ready your blade to deliver a lethal blow, she lunges at your legs with the same animal ferocity she used to tear at her arm."));
        parser.printDialogueLine(new VoiceDialogueLine("Your blade cuts into her again and again as you're tackled to the ground, your body racked with pain as she rips into you with tooth and claw."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Forget about trying to rescue her. This is about survival now. Give her everything you've got."));

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "slay", "[Slay the Princess.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "cSlayPrincess":
                case "slay":
                    this.repeatActiveMenu = false;
                    break;

                default:
                    this.giveDefaultFailResponse();
            }
        }

        parser.printDialogueLine(new VoiceDialogueLine("Though your nerves are seizing with pain, you know you've done your fair share of damage as well, your blade having left deep gashes in the Princess's back."));
        parser.printDialogueLine(new VoiceDialogueLine("You seize a moment of hesitation to throw her off of you and shakily push yourself back to your knees."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "We can still turn this around."));

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "giveUp", "[Give up.]", Chapter.BEAST));
        activeMenu.add(new Option(this.manager, "finish", "[Finish the job.]", Chapter.WITCH));
        activeMenu.add(new Option(this.manager, "run", "[Run for the stairs and lock her in the basement. Maybe she'll bleed out.]", Chapter.NIGHTMARE));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "giveUp":
                    if (!manager.confirmContentWarnings(Chapter.BEAST)) {
                        break;
                    }
                    
                    this.repeatActiveMenu = false;
                    this.hasBlade = false;
                    parser.printDialogueLine(new VoiceDialogueLine("Are you serious? *Sigh.* As what's left of your blood pools around you on the cobblestone floor, the blade falls from your trembling hands and clatters uselessly against the ground. I suppose you simply lacked the will to finish the job."));
                    parser.printDialogueLine(new VoiceDialogueLine("The Princess, wounded but still alive, nervously jumps at the blade and kicks it far away from you before retreating into a dark corner of the room."));
                    parser.printDialogueLine(new VoiceDialogueLine("Her shining eyes watch you from the darkness, unblinking and curious as you bleed out."));
                    parser.printDialogueLine(new VoiceDialogueLine("We can only hope the wounds you managed to inflict will be enough to at least delay her escape from this place. If we're very lucky, they'll kill her before she can reach the outside world."));
                    parser.printDialogueLine(new PrincessDialogueLine("After all this time alone, I thought I'd finally found a friend. But you were just another monster, weren't you?"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "This is the end, isn't it?"));

                    System.out.println();
                    parser.printDialogueLine(new VoiceDialogueLine("Before you can answer, everything goes dark, and you die."));

                    return ChapterEnding.TOBEAST;

                case "cSlayPrincess":
                    if (manager.hasVisited(Chapter.WITCH)) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                case "finish":
                    if (!manager.confirmContentWarnings(Chapter.WITCH)) {
                        break;
                    }
                    
                    this.repeatActiveMenu = false;
                    parser.printDialogueLine(new VoiceDialogueLine("You steel your resolve and take another step closer to the Princess. You probably won't make it out of here alive, but you can still make sure that she won't make it out of here, either."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Excuse me? What's that about not making it out of here alive?"));
                    parser.printDialogueLine(new VoiceDialogueLine("Do you think this is what I wanted to happen? I have a duty to state the facts of the situation, and honestly, it's a miracle anyone in this room is still standing right now. Don't act so surprised."));
                    parser.printDialogueLine(new VoiceDialogueLine("Can you not feel all those gashes and holes pulling you apart? If the Princess doesn't do you in here, blood loss is certain to finish the job."));

                    System.out.println();
                    parser.printDialogueLine(new VoiceDialogueLine("You take another step forward, and the Princess lunges towards you."));
                    parser.printDialogueLine(new VoiceDialogueLine("The two of you enter one last exchange. A flurry of blade, and claw and fleshy ribbons. And then you stop, neither you nor Princess able to go any further."));
                    parser.printDialogueLine(new VoiceDialogueLine("You collapse on the ground, and the Princess collapses beside you. Blood pools around you both and you watch each other fade away."));

                    System.out.println();
                    parser.printDialogueLine(new PrincessDialogueLine("After all this time alone, I thought I'd finally found a friend. But you were just another monster, weren't you?"));
                    parser.printDialogueLine(new VoiceDialogueLine("Silence, as the room starts to get fuzzy around you."));
                    parser.printDialogueLine(new VoiceDialogueLine("You've paid a terrible price, but you've saved us all. It's over."));
                    parser.printDialogueLine(new PrincessDialogueLine("If you think this is it... you're sorely mistaken. One way or another, I'll make sure you pay for this."));

                    System.out.println();
                    parser.printDialogueLine(new VoiceDialogueLine("But you don't have time to worry about such things. Everything goes dark, and you die."));

                    return ChapterEnding.TOWITCH;

                case "cGoStairs":
                    if (manager.hasVisited(Chapter.NIGHTMARE)) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                case "run":
                    if (!manager.confirmContentWarnings(Chapter.NIGHTMARE)) {
                        break;
                    }
                    
                    this.repeatActiveMenu = false;
                    parser.printDialogueLine(new VoiceDialogueLine("The Princess is still chained to the wall. There's nothing she can do to stop you from getting out of here."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "What if she doesn't succumb to her wounds? Whatever she is, she's so much more dangerous than I thought she'd be."));

                    System.out.println();
                    parser.printDialogueLine(new VoiceDialogueLine("You rush up the stairs and dive past the threshold. You're safe. For now."));

                    System.out.println();
                    this.ch1ToNightmare(true, true);
                    return ChapterEnding.TONIGHTMAREFLED;

                default:
                    this.giveDefaultFailResponse();
            }
        }

        throw new RuntimeException("No ending reached");
    }

    /**
     * The player decides to retrieve the blade and slay the soft Princess (leads to Chapter II: the Witch / The Beast)
     * @param worthRisk whether the player said setting the Princess free wasn't "worth the risk"
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding ch1RetrieveBlade(boolean worthRisk) {
        boolean canWitch = true;

        this.currentLocation = GameLocation.CABIN;
        this.withPrincess = false;
        this.hasBlade = true;

        parser.printDialogueLine(new VoiceDialogueLine("You rush up to the first floor, grabbing the blade, both yours and the world's only possible salvation."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Okay. If we're sure about this decision, I'll support it. I suppose we have a world to save, after all..."));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("You slowly creep down the basement stairs. It's quiet."));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("Where the Princess sat only a moment ago, there's only a severed arm, its cooling flesh still chained to the wall. And *she* is nowhere to be seen."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Is it just me or did this room get a lot bigger?"));

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "hello", "(Explore) \"Hello?\"", 0, Chapter.BEAST));
        activeMenu.add(new Option(this.manager, "wrongFoot", "(Explore) \"I think we got off on the wrong foot. Do you think we can start over?\"", 0, Chapter.BEAST));
        activeMenu.add(new Option(this.manager, "lock", "She's lost an arm. I'm locking her down there and letting her bleed out.", 0, Chapter.NIGHTMARE));
        activeMenu.add(new Option(this.manager, "finish", "Let's finish this."));

        OptionsMenu subMenu;
        boolean repeatSub;

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "hello":
                    if (!manager.confirmContentWarnings(Chapter.BEAST)) break;

                    activeMenu.setCondition("wrongFoot", false);
                    canWitch = false;
                    parser.printDialogueLine(new PrincessDialogueLine("Why don't you come closer? I have something to show you."));

                    break;

                case "wrongFoot":
                    if (!manager.confirmContentWarnings(Chapter.BEAST)) break;

                    activeMenu.setCondition("hello", false);
                    canWitch = false;
                    parser.printDialogueLine(new VoiceDialogueLine("Oh, you *coward.*"));
                    parser.printDialogueLine(new PrincessDialogueLine("No. I don't think we can."));
                    parser.printDialogueLine(new PrincessDialogueLine("Why don't you come closer? I have something to show you."));

                    break;

                case "cGoCabin":
                    parser.printDialogueLine(new VoiceDialogueLine("What, are you planning on letting her bleed out down here?"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "This is a dangerous play. Who's to say she'll actually succumb to her wounds?"));
                    parser.printDialogueLine(new VoiceDialogueLine("She doesn't have a weapon and she's missing an arm. You can finish this, right here, right now."));

                    if (!manager.confirmContentWarnings(Chapter.NIGHTMARE)) break;

                    break;

                case "lock":
                    if (!manager.confirmContentWarnings(Chapter.NIGHTMARE)) break;
                    
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "This is a dangerous play. Who's to say she'll actually succumb to her wounds?"));
                    parser.printDialogueLine(new VoiceDialogueLine("She doesn't have a weapon and she's missing an arm. You can finish this, right here, right now."));

                    subMenu = new OptionsMenu();
                    subMenu.add(new Option(this.manager, "lock", "Tell you what. I'll even stay here for a while to make sure she's dead. [Lock her away.]", Chapter.NIGHTMARE));
                    subMenu.add(new Option(this.manager, "finish", !manager.hasVisited(Chapter.WITCH) || (!manager.hasVisited(Chapter.BEAST) && !canWitch), "You're right. Let's finish this."));

                    String outcome = "";
                    repeatSub = true;
                    while (repeatSub) {
                        outcome = parser.promptOptionsMenu(subMenu);
                        switch (outcome) {
                            case "cGoCabin":
                            case "lock":
                                this.ch1ToNightmare(true, true);
                                return ChapterEnding.TONIGHTMARE;
                            
                            case "cGoBasement":
                            case "finish":
                                repeatSub = false;
                                break;

                            default:
                                this.giveDefaultFailResponse();
                        }
                    }

                case "cGoBasement":
                case "finish":
                    this.repeatActiveMenu = false;
                    break;

                default:
                    this.giveDefaultFailResponse(this.activeOutcome);
            }
        }

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("Your eyes dart to the corners of the room. You don't see her."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Where is she?"));

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "armOpen", "[Investigate the arm.]", 0, Chapter.WITCH));
        activeMenu.add(new Option(this.manager, "close", "[Close the door behind you.]"));

        activeMenu.add(new Option(this.manager, "armClosed", "[Investigate the arm.]", activeMenu.get("close")));
        activeMenu.add(new Option(this.manager, "comeOut", "\"Come on out. Let's just get this over with.\"", 0, activeMenu.get("close"), Chapter.BEAST));
        activeMenu.add(new Option(this.manager, "wait", "\"I'll wait.\"", 0, activeMenu.get("close"), Chapter.BEAST));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "armOpen":
                    if (!manager.confirmContentWarnings(Chapter.WITCH)) break;

                    parser.printDialogueLine(new VoiceDialogueLine("As you step towards the severed limb, you hear the pattering of feet behind you, soft against the basement floor, then loud and desperate against the stairs."));
                    parser.printDialogueLine(new VoiceDialogueLine("You turn to chase after the Princess, but she's fast and has too much of a lead."));
                    parser.printDialogueLine(new VoiceDialogueLine("She slams the door behind her before you can make it to the top of the stairs. The lock clicks into place."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "No!"));
                    parser.printDialogueLine(new PrincessDialogueLine("Thanks for letting me out. I'd return the favor, but I think we both know that I can't trust you to let me stay free."));
                    if (worthRisk) parser.printDialogueLine(new PrincessDialogueLine("You should understand... it just isn't 'worth the risk.'"));

                    System.out.println();
                    parser.printDialogueLine(new VoiceDialogueLine("With those parting words the Princess walks away, her quiet footsteps eventually fading as she leaves you and the cabin to rot. You're stuck here. Alone."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "It can't just end like this, right?"));
                    parser.printDialogueLine(new VoiceDialogueLine("As much as I'd prefer for things to have gone differently, I can't deny the reality of what's happened. I'm sorry, but it's over."));
                    parser.printDialogueLine(new VoiceDialogueLine("You don't know how much time passes before the end, but eventually it comes."));

                    System.out.println();
                    parser.printDialogueLine(new VoiceDialogueLine("The world ends, and you end with it."));
                    
                    return ChapterEnding.TOWITCHLOCKED;
                
                case "close":
                    activeMenu.setCondition("armOpen", false);
                    parser.printDialogueLine(new VoiceDialogueLine("You close the door behind you. Almost magically, its locks immediately click into place. Maybe they'll open if you finish the job."));
                    break;
                
                case "armClosed":
                    this.repeatActiveMenu = false;
                    break;
                
                case "comeOut":
                    if (!manager.confirmContentWarnings(Chapter.BEAST)) break;

                    parser.printDialogueLine(new PrincessDialogueLine("I can wait. I'm very patient."));

                    subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "wait", "[Wait.]"));
                    subMenu.add(new Option(this.manager, "shadows", "[Venture into the shadows.]"));

                    switch (parser.promptOptionsMenu(subMenu)) {
                        case "wait":
                            this.ch1BladeBeastWaiting();
                            break;

                        case "shadows":
                            this.ch1BladeBeastShadows();
                            break;
                    }
                    
                    return ChapterEnding.TOBEAST;

                case "wait":
                    if (!manager.confirmContentWarnings(Chapter.BEAST)) break;

                    parser.printDialogueLine(new PrincessDialogueLine("Oh? Do you want to play a waiting game? I've been down here for a long, long time. I'm very good at waiting."));
                    this.ch1BladeBeastWaiting();
                    return ChapterEnding.TOBEAST;
            }
        }
        
        // Investigate arm continues here
        parser.printDialogueLine(new VoiceDialogueLine("You step forward to investigate the severed limb."));
        parser.printDialogueLine(new VoiceDialogueLine("A trail of blood leads from its jagged stump into a dark corner of the basement."));
        parser.printDialogueLine(new VoiceDialogueLine("And then you hear the quiet patter of feet against the basement floor, and there's suddenly a weight on your shoulders. The Princess tears into you."));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("Her teeth and claws are unnaturally sharp, ripping into your shoulders, digging into your throat. You fall to the ground, the Princess eagerly tearing at your flesh."));
        
        if (!canWitch) {
            parser.printDialogueLine(new VoiceDialogueLine("Her ferocity overwhelms you, and as the Princess rends flesh from bone, your limp fingers lose their grip on the blade. It slips from your hand, your one last means of defense lying useless beside you in a pool of your cooling blood. I suppose you just lacked the will to fight back."));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "This is the end, isn't it?"));
            parser.printDialogueLine(new VoiceDialogueLine("I'm afraid it is."));
            parser.printDialogueLine(new VoiceDialogueLine("You shouldn't have let that fear creep into your heart. You had the upper hand, and now look at you."));

            System.out.println();
            parser.printDialogueLine(new VoiceDialogueLine("Everything goes dark, and you die."));

            return ChapterEnding.TOWITCH;
        }
        
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Holy *shit.* What *is* she?!"));

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "giveUp", "[Give up.]", 0, Chapter.BEAST));
        activeMenu.add(new Option(this.manager, "fight", "[Fight back.]", 0, Chapter.WITCH));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(this.activeMenu)) {
                case "giveUp":
                    if (!manager.confirmContentWarnings(Chapter.BEAST)) break;

                    parser.printDialogueLine(new VoiceDialogueLine("Are you serious? *Sigh.* As the Princess rends flesh from bone, your limp fingers can no longer hold the blade. It slips from your hand, your one last means of defense lying useless beside you in a pool of your cooling blood. I suppose you just lacked the will to fight back."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "This is the end, isn't it?"));
                    parser.printDialogueLine(new VoiceDialogueLine("I'm afraid it is."));

                    System.out.println();
                    parser.printDialogueLine(new VoiceDialogueLine("Everything goes dark, and you die."));

                    return ChapterEnding.TOBEAST;
                
                case "fight":
                    if (!manager.confirmContentWarnings(Chapter.WITCH)) break;

                    parser.printDialogueLine(new VoiceDialogueLine("You roll the Princess off your back and turn to face her, rising to your knees and readying your blade."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Those eyes... she's going to kill us."));
                    parser.printDialogueLine(new VoiceDialogueLine("Yes. Things do look a little grim, don't they? The two of you are losing quite a lot of blood. But there's a reason she decided to strike from ambush. She isn't confident about a direct confrontation."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Just because she's playing it safe doesn't mean we have the upper hand."));
                    parser.printDialogueLine(new VoiceDialogueLine("Come on, now. Don't let your confidence waver. This is an important moment."));
                    parser.printDialogueLine(new PrincessDialogueLine("I'm going to kill you."));

                    System.out.println();
                    parser.printDialogueLine(new VoiceDialogueLine("The Princess leaps onto you as you raise your blade in defense."));
                    parser.printDialogueLine(new VoiceDialogueLine("You find your target, and time after time you strike, but the wounds she inflicted in her ambush hinder your movements, and with each fresh exchange you're a little slower, and a little weaker."));
                    parser.printDialogueLine(new VoiceDialogueLine("You seek solace in the fact that she's slowing, too. Finally, she collapses, and you collapse beside her."));

                    System.out.println();
                    parser.printDialogueLine(new PrincessDialogueLine("If you think this is it... you're sorely mistaken. One way or another, I'll make sure you pay for this."));
                    parser.printDialogueLine(new VoiceDialogueLine("Your grasp on the blade weakens. It slips from your numb fingers, lying uselessly on the floor."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "This can't be how it ends, right?"));
                    parser.printDialogueLine(new VoiceDialogueLine("I'm sorry, but it is. You aren't making it out of this basement. But at least you finished the job."));

                    System.out.println();
                    parser.printDialogueLine(new VoiceDialogueLine("Everything goes dark, and you die."));

                    return ChapterEnding.TOWITCH;
            }
        }
        
        throw new RuntimeException("No ending reached");
    }

    /**
     * The player attempts to outwait the Princess after retrieving the blade (leads to Chapter II: the Beast)
     */
    private void ch1BladeBeastWaiting() {
        parser.printDialogueLine(new VoiceDialogueLine("You do your best to patiently wait her out."));
        parser.printDialogueLine(new VoiceDialogueLine("But, eventually, exhaustion starts to set in."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Come on, wake up! We can't fall asleep down here!"));

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "wait", "[Keep waiting.]"));
        activeMenu.add(new Option(this.manager, "shadows", "[Venture into the shadows.]"));

        switch (parser.promptOptionsMenu(activeMenu)) {
            case "wait":
                break;

            case "shadows":
                this.ch1BladeBeastShadows();
                return;
        }

        parser.printDialogueLine(new VoiceDialogueLine("You wait for as long as you can, pushing yourself to stay awake and stay vigilant, but you can't outwait someone's who's been waiting for as long as she remembers."));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("You barely even realize that you've started to drift off."));
        
        System.out.println();
        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("Pain tears you from your sleep. The Princess is upon you, ripping into your flesh, unnaturally sharp teeth and claws severing arteries and digging into organs."));
        parser.printDialogueLine(new VoiceDialogueLine("There's nothing to be done. You're already half-gone."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "It can't just end like this, right?"));
        parser.printDialogueLine(new VoiceDialogueLine("As much as I'd prefer for things to have gone differently, I can't deny the reality of what's happened. I'm sorry, but it's over."));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("Everything goes dark, and you die."));
    }

    /**
     * The player follows the Princess into the darkness after retrieving the blade (leads to Chapter II: the Beast)
     */
    private void ch1BladeBeastShadows() {
        parser.printDialogueLine(new VoiceDialogueLine("You step into the shadows. Too late, you hear the quiet patter of feet against the basement floor, followed by the taut pull and sharp pain of tearing flesh as the Princess lunges into you from behind and drags you to the floor."));
        this.hasBlade = false;
        parser.printDialogueLine(new VoiceDialogueLine("Her ferocity overwhelms you, and as the Princess rends flesh from bone, your limp fingers lose their grip on the blade. It slips from your hand, your one last means of defense lying useless beside you in a pool of your cooling blood. I suppose you just lacked the will to fight back."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "This is the end, isn't it?"));
        parser.printDialogueLine(new VoiceDialogueLine("I'm afraid it is."));
        parser.printDialogueLine(new VoiceDialogueLine("You shouldn't have let that fear creep into your heart. You had the upper hand, and now look at you."));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("Everything goes dark, and you die."));
    }


    /**
     * Runs the beginning of the basement sequence with the harsh princess (took the blade)
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding ch1BasementHarsh(boolean askPrize) {
        boolean canNightmare = !manager.hasVisited(Chapter.NIGHTMARE);

        boolean canHesitateSlay = !manager.hasVisitedAll(Chapter.ADVERSARY, Chapter.TOWER, Chapter.NIGHTMARE);
        boolean mustSpectre = !canHesitateSlay && manager.hasVisited(Chapter.PRISONER);

        this.currentLocation = GameLocation.STAIRS;
        parser.printDialogueLine(new VoiceDialogueLine("The door to the basement creaks open, revealing a staircase faintly illuminated by an unseen light in the room below. This is an oppressive place. The air feels heavy and damp, a hint of rot filtering from the ancient wood. If the Princess really lives here, slaying her is probably doing her a favor."));
        parser.printDialogueLine(new VoiceDialogueLine("Her voice carries up the stairs."));
        parser.printDialogueLine(new PrincessDialogueLine("Who's there?"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "She sounds... dangerous... It's almost as if she's the one in charge down here."));
        parser.printDialogueLine(new VoiceDialogueLine("Don't let it fool you. It's all part of the manipulation."));

        boolean jokeKill = false;
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "hi", "\"Hi!\""));
        activeMenu.add(new Option(this.manager, "checkIn", "\"Just checking in on you.\""));
        activeMenu.add(new Option(this.manager, "jokeKill", "\"Hey, I think I'm here to kill you?\""));
        activeMenu.add(new Option(this.manager, "silent", "[Continue down the stairs.]"));

        this.repeatActiveMenu = true;
        while (this.repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (this.activeOutcome) {
                case "hi":
                    this.repeatActiveMenu = false;
                    parser.printDialogueLine(new PrincessDialogueLine("Don't be a stranger. It's been so long since I've had any visitors, come on down."));
                    break;
                case "checkIn":
                    this.repeatActiveMenu = false;
                    parser.printDialogueLine(new PrincessDialogueLine("Oh? It's been so long since anyone's come down here. I was starting to think they'd forgotten about me."));
                    break;
                case "jokeKill":
                    this.repeatActiveMenu = false;
                    jokeKill = true;
                    parser.printDialogueLine(new PrincessDialogueLine("Ohohohoho, are you now? Why don't you come down and let me take a look at you."));
                    parser.printDialogueLine(new VoiceDialogueLine("Great job, you've given away the element of surprise. Good luck, \"hero.\""));
                    break;
                case "cGoBasement":
                case "silent":
                    this.repeatActiveMenu = false;
                    parser.printDialogueLine(new VoiceDialogueLine("Good. You're still listening to reason."));
                    break;
                
                case "cGoCabin":
                    parser.printDialogueLine(new VoiceDialogueLine("What? No. You're already halfway down the stairs, you can't just turn around now."));
                    break;

                default:
                    this.giveDefaultFailResponse(this.activeOutcome);
            }
        }

        this.currentLocation = GameLocation.BASEMENT;
        this.withPrincess = true;
        parser.printDialogueLine(new VoiceDialogueLine("You walk down the stairs and lock eyes with the Princess. There's a heavy chain around her wrist, binding her to the far wall of the basement."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "She's so coldly beautiful... is she really a threat to the world?"));
        parser.printDialogueLine(new VoiceDialogueLine("Focus on the task at hand."));

        if (jokeKill) parser.printDialogueLine(new PrincessDialogueLine("You weren't kidding when you said you were here to kill me."));
        else parser.printDialogueLine(new PrincessDialogueLine("And there you are. Are you here to kill me or something?"));

        boolean hesitated = false;
        boolean undecided = false;
        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "noWay", mustSpectre, "\"What? No way. Why would you even think that?\""));
        activeMenu.add(new Option(this.manager, "noJoke", "\"Yeah, it wasn't a joke.\"", jokeKill));
        activeMenu.add(new Option(this.manager, "caughtMe", mustSpectre, "\"Okay, yeah, you caught me. I'm here to slay you.\"", !jokeKill));
        activeMenu.add(new Option(this.manager, "nuhUh", mustSpectre, "\"Nuh... nuh uh!\"")); // yes this is a real line from the original game
        activeMenu.add(new Option(this.manager, "undecided", mustSpectre, "\"I haven't decided yet.\""));
        activeMenu.add(new Option(this.manager, "talk", mustSpectre, "\"I'm just here to talk.\""));
        activeMenu.add(new Option(this.manager, "steel", "[Steel your nerves and step forward.]"));

        switch (parser.promptOptionsMenu(activeMenu)) {
            case "noWay":
                this.repeatActiveMenu = false;
                hesitated = true;
                parser.printDialogueLine(new PrincessDialogueLine("That giant knife you're holding kind of gives it away, doesn't it?"));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "The blade! Of course she doesn't want to talk. Who'd want to have a conversation at knife point? We should drop it."));
                break;
            case "noJoke":
                this.repeatActiveMenu = false;
                parser.printDialogueLine(new PrincessDialogueLine("I know. You brought a knife with you and everything. But you don't have to try and kill me. You could always toss that scrap of metal to the ground, and give the two of us a chance to talk things out."));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "She makes a compelling point. What if we didn't kill her? What if we just dropped the blade and *talked?*"));
                break;
            case "caughtMe":
                this.repeatActiveMenu = false;
                hesitated = true;
                parser.printDialogueLine(new PrincessDialogueLine("That isn't a good idea. Just drop the knife, and maybe the two of us can talk things out."));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "She's right. We shouldn't. We should just drop the blade."));
                break;
            case "nuhUh":
                this.repeatActiveMenu = false;
                hesitated = true;
                parser.printDialogueLine(new PrincessDialogueLine("Then drop the knife."));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "We should. It'd go a long way to building trust with her."));
                break;
            case "undecided":
                this.repeatActiveMenu = false;
                hesitated = true;
                undecided = true;
                parser.printDialogueLine(new PrincessDialogueLine("How about you drop the knife and the two of us just... talk?"));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Look how reasonable she's being. We should just drop the blade and talk things out."));
                break;
            case "talk":
                this.repeatActiveMenu = false;
                hesitated = true;
                parser.printDialogueLine(new PrincessDialogueLine("Then why did you bring a knife with you? How about you drop it and then we can chat."));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "She makes a compelling point. What if we just dropped the blade and *talked?* Look at her. It's not like she's a threat."));
                break;

            case "steel":
                this.canDropBlade = true;
                this.canSlayPrincess = true;
                
                return this.ch1SteelNervesHarsh(canHesitateSlay, mustSpectre, false, askPrize);
        }
        
        parser.printDialogueLine(new VoiceDialogueLine("Don't you dare."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "It's fine. We can decide what we want to do *after* we talk to her. Maybe she really is a monster. But killing someone in cold blood isn't very becoming of us."));

        this.canDropBlade = true;
        this.canSlayPrincess = true;
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "drop", mustSpectre, "[Drop it.]"));
        activeMenu.add(new Option(this.manager, "tighten", "[Tighten your grip.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (this.activeOutcome) {
                case "cDrop":
                    if (mustSpectre) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                case "drop":
                    parser.printDialogueLine(new VoiceDialogueLine("*Sigh.* The blade tumbles out of your trembling hands and drops to the floor with an unceremonious clang."));
                    if (undecided) parser.printDialogueLine(new PrincessDialogueLine("Thank you. Maybe now we can just... talk."));
                    else parser.printDialogueLine(new PrincessDialogueLine("Thank you."));

                    return this.ch1DropBladeHarsh(canHesitateSlay, false, false, false);

                case "cSlayPrincess":
                case "tighten":
                    parser.printDialogueLine(new VoiceDialogueLine("You ignore the trembling in your hands and tighten your grip on the blade."));
                    parser.printDialogueLine(new PrincessDialogueLine("You poor thing, your hands are shaking. Are you... scared of me? Because you should be."));
                    
                    return this.ch1SteelNervesHarsh(canHesitateSlay, mustSpectre, hesitated, askPrize);

                default:
                    this.giveDefaultFailResponse(this.activeOutcome);
            }
        }
        
        throw new RuntimeException("No ending reached");
    }

    /**
     * The player steels their nerves and steps forward
     * @param canHesitateSlay whether the player can slay the Princess after hesitating
     * @param mustSpectre whether the player is blocked from all other routes and must slay the Princess without hesitation
     * @param hesitated whether the player already hesitated
     * @param askPrize whether the player asked the Narrator about their prize
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding ch1SteelNervesHarsh(boolean canHesitateSlay, boolean mustSpectre, boolean hesitated, boolean askPrize) {
        boolean canTower = !manager.hasVisited(Chapter.TOWER);
        boolean canSpectre = !manager.hasVisited(Chapter.SPECTRE);
        boolean canRazor = !manager.hasVisited(Chapter.RAZOR);

        boolean canSlay = (!hesitated && (canSpectre || canRazor)) || (hesitated && canHesitateSlay);
        boolean afraid = false;
        boolean isArmed = false;
        
        parser.printDialogueLine(new VoiceDialogueLine("You step forward, your grip on the blade tightening as you steel your resolve."));
        if (hesitated) parser.printDialogueLine(new PrincessDialogueLine("Oh? No talking, then? Fine. What even makes you think you can kill me?"));
        parser.printDialogueLine(new PrincessDialogueLine("I'm probably chained up in this basement for a reason, right? And if that knife is the only weapon you have, you'll have to get close enough to use it."));
        parser.printDialogueLine(new PrincessDialogueLine("So... you should just drop it. Best not to risk finding out what I can do."));
        parser.printDialogueLine(new VoiceDialogueLine("She's unarmed. If you hesitate now, it'll be too late. *End this.*"));

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "bluff", mustSpectre, "(Explore) What if she isn't bluffing? What if she kills us?", Chapter.TOWER));
        activeMenu.add(new Option(this.manager, "isArmed", mustSpectre, "(Explore) Are you *sure* she's not armed?", Chapter.RAZOR));
        activeMenu.add(new Option(this.manager, "sorry", mustSpectre, "(Explore) \"I'm sorry. Can we just talk?\""));
        activeMenu.add(new Option(this.manager, "noDrop", mustSpectre, "\"I'm not dropping the blade.\""));
        activeMenu.add(new Option(this.manager, "drop", mustSpectre, "[Drop the blade.]"));
        activeMenu.add(new Option(this.manager, "slay", !canSlay, "[Slay the Princess.]", 0));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (this.activeOutcome) {
                case "bluff":
                    activeMenu.setCondition("isArmed", false);
                    activeMenu.setCondition("sorry", false);
                    afraid = true;
                    canSlay = canTower;
                    activeMenu.setCondition("slay", canSlay);
                    
                    parser.printDialogueLine(new VoiceDialogueLine("If you go into this expecting to die, you're going to die."));
                    parser.printDialogueLine(new PrincessDialogueLine("Hesitating? Why don't you drop the knife and the two of us can be civilized with each other."));
                    break;

                case "isArmed":
                    activeMenu.setCondition("bluff", false);
                    activeMenu.setCondition("sorry", false);
                    isArmed = true;
                    canSlay = canRazor;
                    activeMenu.setCondition("slay", canSlay);
                    
                    parser.printDialogueLine(new VoiceDialogueLine("I'm positive."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I'm not. But we'll keep our eyes peeled. If she has a weapon, she'll have to draw it before she can use it."));
                    parser.printDialogueLine(new PrincessDialogueLine("Hesitating? Why don't you drop the knife and the two of us can be civilized with each other."));
                    break;

                case "sorry":
                    activeMenu.setCondition("bluff", false);
                    activeMenu.setCondition("isArmed", false);
                    canSlay = canHesitateSlay;
                    activeMenu.setCondition("slay", canSlay);
                    
                    parser.printDialogueLine(new VoiceDialogueLine("You're so close! Don't give up, you've come this far."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "No, this is a good idea. Maybe we can de-escalate things."));
                    parser.printDialogueLine(new PrincessDialogueLine("Oh, threatened, are we? You poor thing. Drop the knife and of course we can talk."));
                    break;

                case "noDrop":
                    this.repeatActiveMenu = false;
                    canSlay = canHesitateSlay;
                    activeMenu.setCondition("slay", canSlay);
                    break;

                case "cDrop":
                    if (mustSpectre) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                case "drop":
                    parser.printDialogueLine(new VoiceDialogueLine("*Sigh.* The blade tumbles out of your trembling hands and drops to the floor with an unceremonious clang."));
                    parser.printDialogueLine(new PrincessDialogueLine("Thank you. Maybe now we can just... talk."));

                    return this.ch1DropBladeHarsh(canSlay, true, afraid, isArmed);

                case "cSlayPrincess":
                    if (!canSlay) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                case "slay":
                    if (!hesitated && !canRazor) {
                        if (!manager.confirmContentWarnings(Chapter.SPECTRE)) break;
                    } else if (isArmed || (!hesitated && !canSpectre)) {
                        if (!manager.confirmContentWarnings(Chapter.RAZOR)) break;
                    } else if (afraid) {
                        if (!manager.confirmContentWarnings(Chapter.TOWER)) break;
                    }

                    return this.ch1SlayHarsh(hesitated, afraid, isArmed, askPrize);

                default:
                    this.giveDefaultFailResponse(this.activeOutcome);
            }
        }

        // noDrop continues here
        parser.printDialogueLine(new PrincessDialogueLine("Then I'm not talking to you."));

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "impasse", "\"Fine then, I guess we're at an impasse!\""));
        activeMenu.add(new Option(this.manager, "stare", "[Squint at the Princess while holding onto the blade.]"));
        activeMenu.add(new Option(this.manager, "sure", "\"Are you sure you don't want to talk?\"", false));
        activeMenu.add(new Option(this.manager, "stare2", "[Stare at the Princess while holding onto the blade.]", activeMenu.get("impasse")));
        activeMenu.add(new Option(this.manager, "contStare", "[Squint at the Princess even harder.]", activeMenu.get("stare")));
        activeMenu.add(new Option(this.manager, "drop", "[Drop the blade.]"));
        activeMenu.add(new Option(this.manager, "slay", !canSlay, "[Slay the Princess.]", 0));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (this.activeOutcome) {
                case "impasse":
                    activeMenu.setCondition("sure", true);
                    activeMenu.setCondition("stare", false);

                    parser.printDialogueLine(new PrincessDialogueLine("I guess we are!"));
                    parser.printDialogueLine(new VoiceDialogueLine("For the love of everything, just slay her already!"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Or drop the blade. Do *something.*"));
                    break;

                case "stare":
                    activeMenu.setCondition("sure", true);
                    activeMenu.setCondition("contStare", true);
                    activeMenu.setCondition("impasse", false);
                    
                    parser.printDialogueLine(new VoiceDialogueLine("You stare at the Princess, squinting."));
                    parser.printDialogueLine(new VoiceDialogueLine("She squints back."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "The two of you are going to do this forever, aren't you?"));
                    break;

                case "sure":
                    activeMenu.setCondition("stare2", false);
                    activeMenu.setCondition("contStare", false);

                    parser.printDialogueLine(new PrincessDialogueLine("Yeah. I'm sure."));
                    parser.printDialogueLine(new VoiceDialogueLine("For goodness' sake, the two of you can't just stand around like this forever. Eventually, something is going to give, and I *highly* recommend that you be the one to take the initiative here."));
                    break;

                case "stare2":
                    activeMenu.setCondition("sure", false);

                    parser.printDialogueLine(new VoiceDialogueLine("You stare at the Princess, squinting fiercely."));
                    parser.printDialogueLine(new VoiceDialogueLine("She squints back."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "The two of you are going to do this forever, aren't you?"));
                    parser.printDialogueLine(new VoiceDialogueLine("You'll have to blink eventually. Just make a *choice.*"));
                    break;
                
                case "contStare":
                    activeMenu.setCondition("sure", false);

                    parser.printDialogueLine(new VoiceDialogueLine("You squint even harder."));
                    parser.printDialogueLine(new VoiceDialogueLine("So does she."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "At least nobody's dying right now..."));
                    parser.printDialogueLine(new VoiceDialogueLine("You're going to have to make a choice. You can't keep squinting forever. Eventually, someone is going to have to blink."));
                    break;

                case "cDrop":
                case "drop":
                    parser.printDialogueLine(new VoiceDialogueLine("*Sigh.* The blade tumbles out of your trembling hands and drops to the floor with an unceremonious clang."));
                    parser.printDialogueLine(new PrincessDialogueLine("Thank you. Maybe now we can just... talk."));

                    return this.ch1DropBladeHarsh(canSlay, true, afraid, isArmed);

                case "cSlayPrincess":
                    if (!canSlay) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                case "slay":
                    if (isArmed) {
                        if (!manager.confirmContentWarnings(Chapter.RAZOR)) break;
                    } else if (afraid) {
                        if (!manager.confirmContentWarnings(Chapter.TOWER)) break;
                    }

                    return this.ch1SlayHarsh(hesitated, afraid, isArmed);

                default:
                    this.giveDefaultFailResponse(this.activeOutcome);
            }
        }

        throw new RuntimeException("No ending reached");
    }

    /**
     * The player drops the blade to talk to the harsh Princess
     * @param canSlay whether the player can slay the Princess after hesitating or the routes are blocked
     * @param steeled whether the player previously steeled their nerves or dropped the blade immediately
     * @param afraid whether the player wondered if the Princess was bluffing
     * @param isArmed whether the player wondered if the Princess is armed
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding ch1DropBladeHarsh(boolean canSlay, boolean steeled, boolean afraid, boolean isArmed) {
        boolean canTower = !manager.hasVisited(Chapter.TOWER);
        boolean canNightmare = !manager.hasVisited(Chapter.NIGHTMARE);
        boolean canRazor = !manager.hasVisited(Chapter.RAZOR);

        boolean canHesitateSlay = !manager.hasVisitedAll(Chapter.ADVERSARY, Chapter.TOWER, Chapter.NIGHTMARE);
        boolean canFree = !manager.hasVisitedAll(Chapter.PRISONER, Chapter.TOWER, Chapter.ADVERSARY);

        this.canDropBlade = false;
        this.hasBlade = false;

        parser.printDialogueLine(new VoiceDialogueLine("Against your better judgment, you step forward to speak with the Princess face-to-face. Unarmed."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "We'll be fine."));
        parser.printDialogueLine(new VoiceDialogueLine("I don't know what you're hoping to accomplish here, but I can assure you there's no reasoning with her. *Sigh.* Just make sure you don't forget about the blade on the floor. You're going to need it."));

        System.out.println();
        parser.printDialogueLine(new PrincessDialogueLine("So here we are. What an awkward start to a relationship."));

        int vagueCount = 0;
        boolean howFree = false;

        OptionsMenu subMenu;
        String outcome;
        boolean repeatSub;
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "awkward", "(Explore) \"Yeah, it's uh... pretty awkward.\""));
        activeMenu.add(new Option(this.manager, "relationship", "(Explore) \"A 'relationship?' Are you coming on to me?\""));
        activeMenu.add(new Option(this.manager, "howFree", "(Explore) \"How would I get you out of here?\""));
        activeMenu.add(new Option(this.manager, "shareTaskA", !canTower, "(Explore) \"I'm here because you're supposed to end the world.\""));
        activeMenu.add(new Option(this.manager, "shareTaskB", !canTower, "(Explore) \"There's people out there who think you're going to end the world. What do you have to say about that?\""));
        activeMenu.add(new Option(this.manager, "name", "(Explore) \"What's your name?\""));
        activeMenu.add(new Option(this.manager, "howLong", "(Explore) \"How long have you been down here?\""));
        activeMenu.add(new Option(this.manager, "whyHere", "(Explore) \"Do you know *why* I'm here to kill you?\""));
        activeMenu.add(new Option(this.manager, "enough", "\"Okay, we've talked enough...\""));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "awkward":
                    activeMenu.setCondition("relationship", false);
                    parser.printDialogueLine(new PrincessDialogueLine("I know. I just said that. Now why are you here to kill me?"));

                    subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "reasons", "\"I have my reasons. Do you think I'd just come here to kill someone without even knowing why? That'd be ridiculous!\""));
                    subMenu.add(new Option(this.manager, "deflect", "\"Do you know why I'm here to kill you?\""));
                    subMenu.add(new Option(this.manager, "shareTask", !canTower, "\"You're supposed to end the world.\"", !this.knowsDestiny));
                    subMenu.add(new Option(this.manager, "notSure", "\"I've been told things, but I'm not sure what to believe.\""));

                    switch (parser.promptOptionsMenu(subMenu)) {
                        case "reasons":
                            if (steeled) parser.printDialogueLine(new PrincessDialogueLine("And yet you hesitated the moment you saw me. And you dropped your knife. If killing me was really that important, you would have had a little more gumption, don't you think?"));
                            else parser.printDialogueLine(new PrincessDialogueLine("And yet you dropped your knife the second you saw me."));

                            parser.printDialogueLine(new PrincessDialogueLine("So... someone put you up to this, right? And whoever it is, it's probably the same someone who shoved me into this dark pit and chained me to a wall."));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "That's a fair question. Who chained her in this basement, and if she's so dangerous, *how* did they manage to trap her? And why have we been left to do their dirty work?"));
                            parser.printDialogueLine(new VoiceDialogueLine("Don't give away the game, and don't let her distract you. That's exactly what she wants."));
                            parser.printDialogueLine(new PrincessDialogueLine("I'm right, aren't I? So who put you up to this?"));
                            break;

                        case "deflect":
                            parser.printDialogueLine(new VoiceDialogueLine("She laughs, her voice dripping with pity."));

                            if (steeled) {
                                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "That laugh...! I think I'm in love."));
                                parser.printDialogueLine(new VoiceDialogueLine("Stop it."));
                            } else {
                                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "She was just threatening us a second ago. Am I the only one here unnerved by that perfect little laugh?"));
                                parser.printDialogueLine(new VoiceDialogueLine("I'll take that as a sign that you're finally coming around to reason."));
                            }

                            parser.printDialogueLine(new PrincessDialogueLine("I have no idea. To be honest, I don't even know why I'm down here... or how I got here."));
                            parser.printDialogueLine(new PrincessDialogueLine("I was kind of hoping *you* might be able to shed some light on the whole situation."));
                            break;

                        case "shareTask":
                            activeMenu.setCondition("shareTaskA", false);
                            activeMenu.setCondition("shareTaskB", false);
                            activeMenu.setCondition("whyHere", false);
                            switch (this.ch1ShareTaskHarsh(steeled, isArmed, canFree)) {
                                case 0:
                                    afraid = true;
                                    canSlay = (isArmed) ? canRazor : canTower;
                                    vagueCount += 1;
                                    break;
                                case 1:
                                    afraid = false;
                                    canSlay = (isArmed) ? canRazor : canHesitateSlay;
                                    vagueCount += 1;
                                    break;
                                case 2: return this.ch1SlayHarsh(true, afraid, isArmed);
                                case 3: return this.ch1RescueHarsh(howFree);
                            }

                        case "notSure":
                            parser.printDialogueLine(new VoiceDialogueLine("Believe *me.*"));
                            parser.printDialogueLine(new PrincessDialogueLine("And do you think asking *me* what to believe is going to suddenly make everything crystal clear? Let's not pretend that's going to happen. As far as you're concerned, and as far as They're concerned, I'm going to say whatever I have to to get out of here. That's just the dynamic of our situation."));
                            break;
                    }

                case "relationship":
                    activeMenu.setCondition("awkward", false);
                    parser.printDialogueLine(new PrincessDialogueLine("Don't jump to any weird conclusions. We're two people who have met each other. By definition, we have a relationship."));
                    break;

                case "howFree":
                    howFree = true;
                    parser.printDialogueLine(new VoiceDialogueLine("You can't. Don't bother."));
                    parser.printDialogueLine(new PrincessDialogueLine("I'm guessing you don't have the key, then? I'm sure there's a key somewhere around here. And if there isn't..."));
                    parser.printDialogueLine(new PrincessDialogueLine("Well, we can always put that knife to good use."));
                    parser.printDialogueLine(new VoiceDialogueLine("Her sharp eyes settle on the edge of the blade."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "She isn't suggesting what I think she's suggesting... right?"));
                    parser.printDialogueLine(new VoiceDialogueLine("She is. I'm sure of it."));
                    break;

                case "shareTaskA":
                    activeMenu.setCondition("shareTaskB", false);
                    activeMenu.setCondition("whyHere", false);
                    switch (this.ch1ShareTaskHarsh(steeled, isArmed, canFree)) {
                        case 0:
                            afraid = true;
                            canSlay = (isArmed) ? canRazor : canTower;
                            vagueCount += 1;
                            break;
                        case 1:
                            afraid = false;
                            canSlay = (isArmed) ? canRazor : canHesitateSlay;
                            vagueCount += 1;
                            break;
                        case 2: return this.ch1SlayHarsh(true, afraid, isArmed);
                        case 3: return this.ch1RescueHarsh(howFree);
                    }

                case "shareTaskB":
                    activeMenu.setCondition("shareTaskA", false);
                    activeMenu.setCondition("whyHere", false);
                    switch (this.ch1ShareTaskHarsh(steeled, isArmed, canFree)) {
                        case 0:
                            afraid = true;
                            canSlay = (isArmed) ? canRazor : canTower;
                            vagueCount += 1;
                            break;
                        case 1:
                            afraid = false;
                            canSlay = (isArmed) ? canRazor : canHesitateSlay;
                            vagueCount += 1;
                            break;
                        case 2: return this.ch1SlayHarsh(true, afraid, isArmed);
                        case 3: return this.ch1RescueHarsh(howFree);
                    }

                case "name":
                    vagueCount += 1;
                    parser.printDialogueLine(new VoiceDialogueLine("She hesitates before answering."));
                    parser.printDialogueLine(new PrincessDialogueLine("You can address me as Your Royal Highness, or Her Majesty. Any honorific should do, really."));
                    
                    if (vagueCount == 1) {
                        parser.printDialogueLine(new VoiceDialogueLine("Note the lack of detail. You can't trust her."));
                    } else if (vagueCount == 2) {
                        parser.printDialogueLine(new VoiceDialogueLine("Again, she offers no specifics. No matter how hard you try, you'll never get a straight answer out of her."));
                    }

                    break;
                
                case "howLong":
                    vagueCount += 1;
                    parser.printDialogueLine(new PrincessDialogueLine("Too long."));
                    
                    if (vagueCount == 1) {
                        parser.printDialogueLine(new VoiceDialogueLine("Note the lack of detail. You can't trust her."));
                    } else if (vagueCount == 2) {
                        parser.printDialogueLine(new VoiceDialogueLine("Again, she offers no specifics. No matter how hard you try, you'll never get a straight answer out of her."));
                    }

                    break;

                case "whyHere":
                    parser.printDialogueLine(new PrincessDialogueLine("Do you?"));

                    subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "shareTask", !canTower, "\"You're apparently going to end the world.\""));
                    subMenu.add(new Option(this.manager, "told", "\"I know what I've been told. Whether or not I believe it is an entirely different matter.\""));
                    subMenu.add(new Option(this.manager, "lie", "(Lie) \"No.\""));
                    subMenu.add(new Option(this.manager, "silent", "[Remain silent.]"));

                    switch (parser.promptOptionsMenu(subMenu)) {
                        case "shareTask":
                            activeMenu.setCondition("shareTaskA", false);
                            activeMenu.setCondition("shareTaskB", false);
                            switch (this.ch1ShareTaskHarsh(steeled, isArmed, canFree)) {
                                case 0:
                                    afraid = true;
                                    canSlay = (isArmed) ? canRazor : canTower;
                                    vagueCount += 1;
                                    break;
                                case 1:
                                    afraid = false;
                                    canSlay = (isArmed) ? canRazor : canHesitateSlay;
                                    vagueCount += 1;
                                    break;
                                case 2: return this.ch1SlayHarsh(true, afraid, isArmed);
                                case 3: return this.ch1RescueHarsh(howFree);
                            }

                        case "told":
                            parser.printDialogueLine(new PrincessDialogueLine("So you're not going to share? How pointless. If you want to talk, I'll talk, but this isn't talking."));
                            break;

                        case "lie":
                            parser.printDialogueLine(new PrincessDialogueLine("You're lying."));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "How does she know that?"));
                            parser.printDialogueLine(new PrincessDialogueLine("Don't think that just because I'm the one in chains it means you have a right to interrogate me."));
                            break;

                        case "silent":
                            parser.printDialogueLine(new PrincessDialogueLine("I see. The silent treatment. You know, if you don't share with me, I'm not going to share with you."));
                            break;
                    }

                    break;
                
                case "enough":
                    this.repeatActiveMenu = false;
                    break;

                case "cSlayPrincess":
                    if (!canSlay) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }

                    if (isArmed && !manager.confirmContentWarnings(Chapter.RAZOR)) break;
                    else if (afraid && !manager.confirmContentWarnings(Chapter.TOWER)) break;

                    break;

                case "cGoStairs":
                    parser.printDialogueLine(new VoiceDialogueLine("Where are you going? In case you've forgotten, you're here to complete a very important task."));

                    subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "lock", !canNightmare, "I don't have enough information to make a decision yet. I'm going to keep her locked away down here, at least for a little bit. We can get to know each other better while I decide what to do. [Keep her locked away.]", 0));
                    subMenu.add(new Option(this.manager, "nevermind", "You're right. I still have a few more questions for her before I make a decision. [Turn back.]"));

                    repeatSub = true;
                    while (repeatSub) {
                        outcome = parser.promptOptionsMenu(subMenu);
                        switch (outcome) {
                            case "lock":
                                if (!manager.confirmContentWarnings(Chapter.NIGHTMARE)) break;

                                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "That seems like a pretty good compromise."));
                                parser.printDialogueLine(new VoiceDialogueLine("Leaving her alive is too risky. If you don't deal with her soon, she *will* find a way out."));
                                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "It's a chance we'll have to take. We can make this work. If we just stay here and keep watch, no one has to die."));
                                parser.printDialogueLine(new PrincessDialogueLine("Where are you going?! You can't just leave me here!"));
                                parser.printDialogueLine(new VoiceDialogueLine("You turn your back to the Princess and make your way back to the stairs."));
                                parser.printDialogueLine(new PrincessDialogueLine("It won't be long before I slip these chains. And once I'm out of here, there will be hell to pay for leaving me behind."));
                                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "\"Slip these chains?\" She can't, right? She needed our help to get out of here. But do you hear the conviction in her voice? I don't think she's bluffing."));
                                parser.printDialogueLine(new VoiceDialogueLine("Either way, she dropped the mask, didn't she? You can still turn around and finish the job."));

                                subMenu = new OptionsMenu(true);
                                subMenu.add(new Option(this.manager, "lock", "No, we're sticking to the plan and locking her down here."));
                                subMenu.add(new Option(this.manager, "slay", !canTower, "Oh that's a relief! I was afraid I'd already committed to not slaying her.", 0));

                                while (repeatSub) {
                                    switch (parser.promptOptionsMenu(subMenu)) {
                                        case "lock":
                                            parser.printDialogueLine(new VoiceDialogueLine("You'll be the death of all of us, but fine. Have it your way."));

                                            System.out.println();
                                            this.ch1ToNightmare(false, false);
                                            return ChapterEnding.TONIGHTMARE;

                                        case "slay":
                                            if (!manager.confirmContentWarnings(Chapter.TOWER)) break;

                                            parser.printDialogueLine(new VoiceDialogueLine("It's never too late to do the right thing. Now hurry."));

                                            System.out.println();
                                            return this.ch1SlayHarsh(true, true, isArmed);
                                    }
                                }
                                
                                break;

                            case "nevermind":
                                repeatSub = false;
                                parser.printDialogueLine(new VoiceDialogueLine("There's no \"decision\" to be made here, but fine."));
                                break;
                        }
                    }
                    
                    break;

                default:
                    this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // "We've talked enough" continues here
        parser.printDialogueLine(new PrincessDialogueLine("Oh. Have you decided what to do with me?"));
        parser.printDialogueLine(new VoiceDialogueLine("You know why you're here..."));

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "slay", !canSlay, "[Slay the Princess.]"));
        activeMenu.add(new Option(this.manager, "free", !canFree, "\"I'm getting you out of here.\" [Examine the chains.]"));
        activeMenu.add(new Option(this.manager, "lock", !canNightmare, "\"I'm going to keep you locked away down here. At least for a bit. We can get to know each other better while I decide what to do.\" [Keep her locked away.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "cSlayPrincess":
                    if (!canSlay) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                case "slay":
                    if (isArmed && !manager.confirmContentWarnings(Chapter.RAZOR)) break;
                    else if (afraid && !manager.confirmContentWarnings(Chapter.TOWER)) break;

                    return this.ch1SlayHarsh(true, afraid, isArmed);

                case "free":
                    parser.printDialogueLine(new VoiceDialogueLine("Oh, you have to be kidding me!"));
                    return this.ch1RescueHarsh(howFree);

                case "cGoStairs":
                    if (!canNightmare) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                case "lock":
                    if (manager.confirmContentWarnings(Chapter.NIGHTMARE)) this.repeatActiveMenu = false;

                    this.repeatActiveMenu = false;
                    break;

                default:
                    this.giveDefaultFailResponse();
            }
        }

        // Lock continues here
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "That seems like a pretty good compromise."));
        parser.printDialogueLine(new VoiceDialogueLine("Leaving her alive is too risky. If you don't deal with her soon, she *will* find a way out."));
        parser.printDialogueLine(new PrincessDialogueLine("One way or another, I'm going to find a way out of here. You can make it easier for both of us if you help."));
        parser.printDialogueLine(new PrincessDialogueLine("And if you don't..."));
        parser.printDialogueLine(new PrincessDialogueLine("I can promise that you'll come to regret that decision."));
        parser.printDialogueLine(new VoiceDialogueLine("You have to make a choice. Let's hope for all our sakes it's the right one."));

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "slay", !canSlay && !canTower, "[Slay the Princess.]"));
        activeMenu.add(new Option(this.manager, "free", !canFree, "\"Okay. Let's get you out of here.\" [Examine the chains.]"));
        activeMenu.add(new Option(this.manager, "lock", "Uh, I *made* my choice. I'm locking her in the basement."));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "cSlayPrincess":
                    if (!canSlay && !canTower) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                case "slay":
                    if (isArmed && !manager.confirmContentWarnings(Chapter.RAZOR)) break;
                    else if (!manager.confirmContentWarnings(Chapter.TOWER)) break;

                    return this.ch1SlayHarsh(true, true, isArmed);

                case "free":
                    parser.printDialogueLine(new VoiceDialogueLine("Oh, for the love of..."));
                    parser.printDialogueLine(new PrincessDialogueLine("Good. I'm glad you've come to your senses."));
                    parser.printDialogueLine(new VoiceDialogueLine("You're making a huge mistake."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "No. You're doing the right thing."));
                    return this.ch1RescueHarsh(howFree);

                case "cGoStairs":
                case "lock":
                    this.repeatActiveMenu = false;
                    break;

                default:
                    this.giveDefaultFailResponse();
            }
        }

        // Lock continues here

        parser.printDialogueLine(new VoiceDialogueLine("I know you think this is a fair compromise, but it isn't. *No one* wins here."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "It's a chance we'll have to take. We can make this work. If we just stay here and keep watch, no one has to die."));
        parser.printDialogueLine(new PrincessDialogueLine("You're making a mistake."));
        parser.printDialogueLine(new VoiceDialogueLine("You turn your back to the Princess and make your way to the stairs."));
        parser.printDialogueLine(new PrincessDialogueLine("It won't be long before I slip these chains. And once I'm out of here, there will be *hell* to pay for leaving me behind."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "\"Slip these chains?\" She can't, right? She needed our help to get out of here. But do you hear the conviction in her voice? I don't think she's bluffing."));
        parser.printDialogueLine(new VoiceDialogueLine("Either way, she dropped her mask, didn't she? You can still turn around and finish the job."));

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "lock", "No, we're sticking to the plan and locking her down here."));
        activeMenu.add(new Option(this.manager, "slay", !canTower, "Oh that's a relief! I was afraid I'd already committed to not slaying her.", 0));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "lock":
                    parser.printDialogueLine(new VoiceDialogueLine("You'll be the death of all of us, but fine. Have it your way."));

                    System.out.println();
                    this.ch1ToNightmare(false, false);
                    return ChapterEnding.TONIGHTMARE;

                case "slay":
                    if (!manager.confirmContentWarnings(Chapter.TOWER)) break;

                    parser.printDialogueLine(new VoiceDialogueLine("It's never too late to do the right thing. Now hurry."));

                    System.out.println();
                    return this.ch1SlayHarsh(true, true, isArmed);
            }
        }

        throw new RuntimeException("No ending reached");
    }

    /**
     * The player tells the harsh Princess that she's allegedly going to end the world
     * @param steeled whether the player previously steeled their nerves or dropped the blade immediately
     * @param canFree whether the player can free the Princess or the routes are blocked
     * @return 1 if the player returns to regular dialogue while trusting the Princess; 2 if the player decides to slay the Princess; 3 if the player decides to free the Princess; 0 otherwise
     */
    private int ch1ShareTaskHarsh(boolean steeled, boolean isArmed, boolean canFree) {
        boolean canSlay = (isArmed) ? !manager.hasVisited(Chapter.RAZOR) : !manager.hasVisitedAll(Chapter.ADVERSARY, Chapter.TOWER, Chapter.NIGHTMARE);

        this.knowsDestiny = true;
        
        parser.printDialogueLine(new VoiceDialogueLine("Don't just *tell* her that!"));

        if (steeled) {
            parser.printDialogueLine(new PrincessDialogueLine("That's cute. Do you believe that? Do you think I'm some sort of... monster?"));
        } else {
            parser.printDialogueLine(new PrincessDialogueLine("Is that why they threw me down here? But I don't want to hurt anyone. I like the world! I think."));
            parser.printDialogueLine(new PrincessDialogueLine("I don't remember much about it, to be honest. I've been down here a long time."));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Just how long has she been down here?"));
        }
        
        parser.printDialogueLine(new PrincessDialogueLine("If I'm supposed to be capable of ending the world, then how did I wind up here, chained to a wall? Have they told you why I'm allegedly so... dangerous?"));

        OptionsMenu shareMenu = new OptionsMenu(true);
        shareMenu.add(new Option(this.manager, "deflect", "(Deflect) \"What are you going to do if I let you out of here?\""));
        shareMenu.add(new Option(this.manager, "enough", "\"I've been told enough.\""));
        shareMenu.add(new Option(this.manager, "youTell", "\"I was hoping you'd tell me.\""));
        shareMenu.add(new Option(this.manager, "reasons", "\"No. But I'm sure they have their reasons for keeping that information secret from me.\""));
        shareMenu.add(new Option(this.manager, "trustYou", "\"No. And if I'm being honest, I'm more inclined to trust you than I'm inclined to trust Them.\""));
        shareMenu.add(new Option(this.manager, "silent", "[Remain silent.]"));

        switch (parser.promptOptionsMenu(shareMenu)) {
            case "deflect":
                parser.printDialogueLine(new VoiceDialogueLine("The Princess hesitates before responding."));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "She doesn't know. She's been down here too long to have any idea of what she'd do in another life."));
                parser.printDialogueLine(new VoiceDialogueLine("She knows what she'd do. She's just searching for whatever answer she thinks you want to hear."));
                parser.printDialogueLine(new PrincessDialogueLine("I don't think I can answer that question in any way you'd find meaningful."));
                break;

            case "enough":
                parser.printDialogueLine(new VoiceDialogueLine("Thanks for the vote of confidence."));
                parser.printDialogueLine(new PrincessDialogueLine("They haven't shared a thing, have they? All they've done is point a finger."));
                break;

            case "youTell":
                parser.printDialogueLine(new PrincessDialogueLine("Ending the world seems like an awful lot for just one person to do. I wouldn't even know where to start."));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I believe her."));
                parser.printDialogueLine(new VoiceDialogueLine("She doesn't have to know how to destroy the world to be capable of doing it."));
                break;

            case "reasons":
                parser.printDialogueLine(new VoiceDialogueLine("Thanks for the vote of confidence."));
                parser.printDialogueLine(new PrincessDialogueLine("What if they're bad reasons, though? If they had *good* reasons for thinking I was dangerous, wouldn't they have shared them with you? I don't want to hurt anyone. I just want to leave."));
                break;
            
            case "trustYou":
                parser.printDialogueLine(new VoiceDialogueLine("Sooner or later you'll understand that I have your best interests at heart. Hopefully sooner."));
                parser.printDialogueLine(new PrincessDialogueLine("How sweet. Now be a pal and help me get out of here, would you? We can figure out how to deal with Them after I'm free."));

                OptionsMenu finalShareMenu = new OptionsMenu();
                finalShareMenu.add(new Option(this.manager, "talk", "\"I still have a few more questions before I decide what to do.\""));
                finalShareMenu.add(new Option(this.manager, "slay", !canSlay, "\"Actually, I've changed my mind. I don't trust you.\" [Slay the Princess.]", 0));
                finalShareMenu.add(new Option(this.manager, "free", !canFree, "\"I'll see what I can do.\" [Examine the chains.]", 0));

                boolean repeatMenu = true;
                String outcome;
                while (repeatMenu) {
                    outcome = parser.promptOptionsMenu(finalShareMenu);
                    switch (outcome) {
                        case "talk":
                            parser.printDialogueLine(new PrincessDialogueLine("Fine. What do you want to know?"));
                            return 1;

                        case "cSlayPrincess":
                            if (!canSlay) {
                                parser.printDialogueLine(CANTSTRAY);
                                break;
                            } else if (isArmed && !manager.confirmContentWarnings(Chapter.RAZOR)) {
                                break;
                            }
                        case "slay":
                            if (isArmed && !manager.confirmContentWarnings(Chapter.RAZOR)) break;
                            return 2;

                        case "free":
                            if (!manager.confirmContentWarnings("self-mutilation", true)) break;
                            return 3;

                        default:
                            this.giveDefaultFailResponse(activeOutcome);
                    } 
                }

            case "silent":
                parser.printDialogueLine(new PrincessDialogueLine("They haven't told you anything, have they?"));
                break;
        }
        
        parser.printDialogueLine(new PrincessDialogueLine("At the end of the day, whatever the two of us have going on down here is about trust."));
        parser.printDialogueLine(new PrincessDialogueLine("Whoever sent you to \"slay\" me claimed I was a threat to the world, but they didn't tell you why."));
        parser.printDialogueLine(new PrincessDialogueLine("That doesn't sound right to me, and I don't think it sounds right to you, either. Otherwise we'd be killing each other instead of talking."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "She has a point. There's a reason I've been telling you to question this situation. And there's a reason you've listened."));
        parser.printDialogueLine(new PrincessDialogueLine("So I could tell you that I'd lead a quiet life in the woods or that I'd open an orphanage or that I'd do any other number of \"good\" things that I'm sure you think you want to hear..."));
        parser.printDialogueLine(new PrincessDialogueLine("But you don't really know me, do you? What can my word possibly be worth in a situation like this?"));
        parser.printDialogueLine(new VoiceDialogueLine("She's right about one thing. Her word isn't worth anything."));
        parser.printDialogueLine(new PrincessDialogueLine("Like I said, it's all about trust. *Blind* trust."));
        parser.printDialogueLine(new PrincessDialogueLine("So do you trust me -- the prisoner, the victim, the Princess *clearly* incapable of ending the world -- or do you trust whoever put me here?"));
        parser.printDialogueLine(new VoiceDialogueLine("She's wrong. This isn't about trust. This is about *risk.* We stand to lose everything, all for the sake of one person. And a subjugating *monarch,* no less."));
        return 0;
    }

    /**
     * The player attempts to slay the harsh Princess
     * @param hesitated whether the player initially hesitated or not
     * @param afraid whether the player is afraid of the Princess
     * @param isArmed whether the player wondered if the Princess is armed
     * @param askPrize whether the player asked the Narrator about their prize
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding ch1SlayHarsh(boolean hesitated, boolean afraid, boolean isArmed, boolean askPrize) {
        // !hesitated = Spectre / Razor
        // isArmed = forced Razor
        // afraid = forced Tower
        // otherwise = Adversary / Tower / Nightmare

        this.hasBlade = true;
        this.canDropBlade = false;
        if (!hesitated) {
            return this.ch1SlayHarshSteeled(askPrize);
        } else {
            if (isArmed) {
                return this.ch1SlayHarshForceRazor();
            } else if (afraid) {
                this.ch1SlayHarshForceTower();
                return ChapterEnding.TOTOWERUNHARMED;
            } else {
                return this.ch1SlayHarshHesitated();
            }
        }
    }

    /**
     * The player attempts to slay the harsh Princess
     * @param hesitated whether the player initially hesitated or not
     * @param afraid whether the player is afraid of the Princess
     * @param isArmed whether the player wondered if the Princess is armed
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding ch1SlayHarsh(boolean hesitated, boolean afraid, boolean isArmed) {
        return this.ch1SlayHarsh(hesitated, afraid, isArmed, false);
    }

    /**
     * The player slays the harsh Princess without hesitation (leads to Chapter II: The Spectre / The Razor)
     * @param askPrize whether the player asked the Narrator about their prize
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding ch1SlayHarshSteeled(boolean askPrize) {
        parser.printDialogueLine(new VoiceDialogueLine("You lunge forward without a moment's hesitation."));
        parser.printDialogueLine(new VoiceDialogueLine("You feel flesh easily give way and look down to see your blade already sinking deep into her heart."));
        parser.printDialogueLine(new PrincessDialogueLine("O... oh."));
        parser.printDialogueLine(new PrincessDialogueLine("This is it, isn't it?"));
        parser.printDialogueLine(new PrincessDialogueLine("I'm almost embarrassed. I should've seen that coming. But... I have to wonder..."));
        parser.printDialogueLine(new PrincessDialogueLine("Do you *actually* believe this was enough to kill me?"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "It's like she's convinced she can't die."));
        parser.printDialogueLine(new VoiceDialogueLine("Yes. Even as she lays there dying, she entirely *believes* herself to be alive and well."));
        parser.printDialogueLine(new VoiceDialogueLine("But it's over, isn't it? She stopped breathing moments ago, that arrogant look still plastered on her face."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "But *is* it over? *Really* over?"));

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "yes", "Of course it is. She's dead.", Chapter.SPECTRE));
        activeMenu.add(new Option(this.manager, "maybe", "I'm not sure. I feel like she has to have some kind of trick up her sleeve.", Chapter.RAZOR));
        activeMenu.add(new Option(this.manager, "no", "Of course not. That was too easy.", Chapter.RAZOR));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "yes":
                    if (!manager.confirmContentWarnings(Chapter.SPECTRE, "suicide")) break;

                    parser.printDialogueLine(new VoiceDialogueLine("Yes, exactly. It's over."));
                    return this.ch1SlaySuccess(askPrize);
                
                case "maybe":
                    this.repeatActiveMenu = false;

                    parser.printDialogueLine(new VoiceDialogueLine("It's over. You could check her sleeves if you want, but I can assure you that there's nothing hidden up there."));
                    break;

                case "no":
                    this.repeatActiveMenu = false;

                    parser.printDialogueLine(new VoiceDialogueLine("It's. Over. Don't get all worked up."));
                    break;
            }
        }

        // Unsure continues here
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "We should make sure. What's the harm in checking for a pulse?"));
        parser.printDialogueLine(new VoiceDialogueLine("I really don't think you should do that."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "And why shouldn't we? Is there something you're not telling us?"));
        parser.printDialogueLine(new VoiceDialogueLine("I've told you everything that's happened with complete accuracy. The Princess is *dead.* Your blade pierced her heart, there's no coming back from that."));

        this.hasBlade = false;
        this.withBlade = true;
        this.canSlayPrincess = false;
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "blade", "[Remove the blade.]"));
        activeMenu.add(new Option(this.manager, "pulse", "[Check for a pulse.]"));
        activeMenu.add(new Option(this.manager, "leave", "You're right. She's dead. Let's just get out of here.", Chapter.SPECTRE));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "cTake":
                case "blade":
                    if (!manager.confirmContentWarnings(Chapter.RAZOR)) break;
                    
                    this.repeatActiveMenu = false;

                    parser.printDialogueLine(new VoiceDialogueLine("You lean down and wrap your hand around the blade's hilt."));
                    parser.printDialogueLine(new VoiceDialogueLine("But as you begin to slide it out of its resting place, you feel a sharp and sudden jab in your side."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "What was *that?*"));
                    parser.printDialogueLine(new PrincessDialogueLine("I guess I won't be dying alone after all..."));
                    break;

                case "pulse":
                    if (!manager.confirmContentWarnings(Chapter.RAZOR)) break;
                    
                    this.repeatActiveMenu = false;

                    parser.printDialogueLine(new VoiceDialogueLine("You lean down and place your hand against her neck, holding your breath as you search for a pulse. Even though you know you're not going to find one."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "We definitely won't if *you* keep talking."));
                    parser.printDialogueLine(new VoiceDialogueLine("I'm sorry, do you *want* her to be alive? You just saved the entire world from annihilation, why are you suddenly trying to call that into *question-*"));

                    System.out.println();
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Wait... what was that?"));
                    parser.printDialogueLine(new VoiceDialogueLine("You know what that was. That was a sound of a heartbeat. Followed by another. And another."));
                    parser.printDialogueLine(new PrincessDialogueLine("I guess I won't be dying alone after all..."));
                    parser.printDialogueLine(new VoiceDialogueLine("Something sharp digs into your side, the shock of it sending your nerves into a pained frenzy."));
                    break;

                case "cGoStairs":
                case "leave":
                    if (!manager.confirmContentWarnings(Chapter.SPECTRE, "suicide")) break;

                    parser.printDialogueLine(new VoiceDialogueLine("Yes, exactly. It's over."));
                    return this.ch1SlaySuccess(askPrize);

                case "cSlayPrincessNoBladeFail":
                    parser.printDialogueLine(new VoiceDialogueLine("Are you even listening to me? She's already dead."));
                    break;

                default:
                    this.giveDefaultFailResponse(activeOutcome);
            }
        }

        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Quick, let's get out of here!"));
        parser.printDialogueLine(new VoiceDialogueLine("It's too late for that now. You collapse to the ground as the mortally-wounded Princess twists a blade of her own deeper between your ribs."));
        parser.printDialogueLine(new VoiceDialogueLine("As you fall, she falls with you, exhausted by the effort, the little life that was left in her eyes fading rapidly."));
        parser.printDialogueLine(new PrincessDialogueLine("An eye for an eye. A life for a life. I guess we're even now..."));
        parser.printDialogueLine(new PrincessDialogueLine("See you around."));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("You were *so* close! Why did you hesitate? *Sigh.* It doesn't matter. At least you managed to take her with you."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "For whatever that's worth."));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("Everything goes dark, and you die."));

        return ChapterEnding.TORAZORREVIVAL;
    }

    /**
     * The player successfully slays the Princess without hesitation (leads to Chapter II: The Spectre / the Good Ending)
     * @param askPrize whether the player asked the Narrator about their prize
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding ch1SlaySuccess(boolean askPrize) {
        this.currentLocation = GameLocation.CABIN;
        this.hasBlade = false;
        this.withBlade = false;
        this.withPrincess = false;

        parser.printDialogueLine(new VoiceDialogueLine("With your work done, you make your way back up the stairs, closing the door to the basement behind you."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Why do I feel like we've done something terrible?"));
        parser.printDialogueLine(new VoiceDialogueLine("You did kill someone. Greater good or not, something would be very wrong with you if you didn't feel at least a little bad. But it *was* for the greater good. One of these days, that will sink in and help ease your guilty conscience."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "But that day isn't today. Let's just get out of here."));

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "leave", "[Leave.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "cGoHill":
                case "leave":
                    this.repeatActiveMenu = false;
                    break;

                case "cGoStairs":
                    parser.printDialogueLine(new VoiceDialogueLine("There's no reason to go back to the basement. Your job here is done."));
                    break;
                    
                case "cSlayPrincessNoBladeFail":
                    parser.printDialogueLine(new VoiceDialogueLine("She's already dead, thanks to your efforts."));
                    break;

                default:
                    this.giveDefaultFailResponse(activeOutcome);
            }
        }

        parser.printDialogueLine(new VoiceDialogueLine("You open the cabin door, ready to return to a world saved from certain doom."));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("Only, a world saved from certain doom isn't what you find. Instead, what you find is nothing at all. Where a lush forest stood mere minutes ago, the only thing in front of you now is the vast emptiness of some place far away."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "What... happened?"));
        parser.printDialogueLine(new VoiceDialogueLine("Everyone is fine, it's just that you and the cabin are now far away from them. Don't worry. You'll be safe here. This is good. Everyone is happy. *You'll* be happy."));

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "prizeYay", "Wait, is this my prize? This is great! Thank you so much.", askPrize));
        activeMenu.add(new Option(this.manager, "prizeBoo", "Wait, is this my prize? This sucks!", askPrize));
        activeMenu.add(new Option(this.manager, "bullshit", "That's bullshit! Let me out of here!"));
        activeMenu.add(new Option(this.manager, "ok", "Oh. Okay."));
        activeMenu.add(new Option(this.manager, "better", "I was kind of hoping I'd get a better ending for saving the world."));

        switch (parser.promptOptionsMenu(activeMenu)) {
            case "prizeYay":
                parser.printDialogueLine(new VoiceDialogueLine("I'm just happy that you're happy. I knew you'd like it."));
                break;

            case "prizeBoo":
                parser.printDialogueLine(new VoiceDialogueLine("What's done is done, and there's no going back now. I'm sorry you don't like your reward, but maybe you'll learn to appreciate it in time."));
                break;

            case "bullshit":
                if (askPrize) parser.printDialogueLine(new VoiceDialogueLine("What's done is done, and there's no going back now. I'm sorry you don't like your reward, but maybe you'll learn to appreciate it in time."));
                else parser.printDialogueLine(new VoiceDialogueLine("What's done is done, and there's no going back now."));
                break;

            case "ok":
                parser.printDialogueLine(new VoiceDialogueLine("I'm so glad you're keeping an open mind."));
                break;

            case "better":
                parser.printDialogueLine(new VoiceDialogueLine("This isn't an ending. In fact, now that the Princess has been slain, endings are a thing of the past. No... this is the beginning of *eternity.* Your reward."));
                break;
        }
        
        parser.printDialogueLine(new VoiceDialogueLine("This is what's best for everyone. Trust me."));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("Time passes. You can't be sure if it's days, or months, or years or even decades. It's all a wonderful, boring blur. You've never been happier."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Pst! Hey! We're not just going to stay here *forever,* right?"));

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "explore", "(Explore) Didn't you hear the Narrator? I'm happy. We're happy."));
        activeMenu.add(new Option(this.manager, "notHappyA", "Hmm, okay, maybe I'm not happy. And I'm not just saying that because you're the last person I talked to.", activeMenu.get("explore")));
        activeMenu.add(new Option(this.manager, "notHappyB", "Hmm, okay, maybe I'm not happy. And I'm not just saying that because you're the last person I talked to.", manager.goodEndingAttempted()));
        activeMenu.add(new Option(this.manager, "sure", manager.goodEndingAttempted(), "No, we're happy. I'm sure of it.", activeMenu.get("explore")));
        activeMenu.add(new Option(this.manager, "hellNo", "Hell no, do you have any idea how to get us the heck out of here?", !manager.goodEndingAttempted()));
        activeMenu.add(new Option(this.manager, "ofCourse", manager.goodEndingAttempted(), "Of course we are. I like it here."));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "explore":
                    activeMenu.setCondition("notHappyB", false);
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Are we really happy, or is He just telling us we are?"));
                    break;

                case "notHappyA":
                case "notHappyB":
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Good, because I have an idea to get us out of here. Though you're probably not going to like it."));
                    
                    if (this.ch1HeroSuggestSpectre()) {
                        return ChapterEnding.TOSPECTRE;
                    } else if (this.ch1GoodEnding()) {
                        return ChapterEnding.GOODENDING;
                    } else {
                        activeMenu.setCondition("notHappyA", false);
                        activeMenu.setCondition("hellNo", false);

                        activeMenu.setCondition("notHappyB", true);
                        activeMenu.setGreyedOut("sure", true);
                        activeMenu.setGreyedOut("ofCourse", true);
                        break;
                    }
                
                case "hellNo":
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I do, but you're probably not going to like it."));
                    
                    if (this.ch1HeroSuggestSpectre()) {
                        return ChapterEnding.TOSPECTRE;
                    } else if (this.ch1GoodEnding()) {
                        return ChapterEnding.GOODENDING;
                    } else {
                        activeMenu.setCondition("notHappyA", false);
                        activeMenu.setCondition("hellNo", false);

                        activeMenu.setCondition("notHappyB", true);
                        activeMenu.setGreyedOut("sure", true);
                        activeMenu.setGreyedOut("ofCourse", true);
                        break;
                    }

                case "sure":
                case "ofCourse":
                    if (this.ch1GoodEnding()) return ChapterEnding.GOODENDING;
                    else {
                        activeMenu.setCondition("notHappyA", false);
                        activeMenu.setCondition("hellNo", false);

                        activeMenu.setCondition("notHappyB", true);
                        activeMenu.setGreyedOut("sure", true);
                        activeMenu.setGreyedOut("ofCourse", true);
                        break;
                    }
            }
        }

        throw new RuntimeException("No ending reached");
    }

    /**
     * The Voice of the Hero suggests slaying yourself to get out of the Narrator's "happy ending"
     * @return whether the player accepted the Voice of the Hero's offer (leads to Chapter II: The Spectre) or not (leads to the Good Ending)
     */
    private boolean ch1HeroSuggestSpectre() {
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "The blade. We can use the blade to get out of this."));
        parser.printDialogueLine(new VoiceDialogueLine("I can hear everything you say, little voice. There's only one thing it would want you to use that blade on, and I'm afraid that thing is *you,* dear hero."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "He's right. It's the only way out."));
        parser.printDialogueLine(new VoiceDialogueLine("Do you hear that? It wants to take this happiness away from you. It wants this wonderful place to *end.*"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Do you not? There's more for us to do, and the only way for us to do it is to take that blade and use it."));
        parser.printDialogueLine(new VoiceDialogueLine("Don't you *dare.*"));

        OptionsMenu subMenu = new OptionsMenu();
        subMenu.add(new Option(this.manager, "explore", "(Explore) Wouldn't \"using\" the blade... you know, kill us? Wouldn't we be dead?"));
        subMenu.add(new Option(this.manager, "reluctant", "You'd better be right about this. I'll be pretty upset if we *die* die.", subMenu.get("explore")));
        subMenu.add(new Option(this.manager, "notRisking", manager.goodEndingAttempted(), "I'm not risking *death* over your weird hunch.", subMenu.get("explore")));
        subMenu.add(new Option(this.manager, "anything", "Anything to get out of this hell."));
        subMenu.add(new Option(this.manager, "stickAround", manager.goodEndingAttempted(), "You're right. I didn't like that idea. I'm just going to stick around and do nothing, at least for a little while longer."));

        boolean repeatSub = true;
        String outcome;
        while (repeatSub) {
            outcome = parser.promptOptionsMenu(subMenu);
            switch (outcome) {
                case "explore":
                    parser.printDialogueLine(new VoiceDialogueLine("How astute. You're absolutely correct. Using the blade to kill yourself would kill you and you shouldn't do it."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "In a sense, we'd die, but looking at things from another angle, are we even really alive anymore? This place... it's nothing! It's absolutely nothing. It's just the same thing, constantly, forever."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I know this is out there, but trust me, I *know* using the blade will work."));
                    parser.printDialogueLine(new VoiceDialogueLine("That little voice didn't want you to slay the Princess. It didn't want you to be *happy.*"));
                    break;
                    
                case "stickAround":
                    parser.printDialogueLine(new VoiceDialogueLine("What a relief."));
                case "notRisking":
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I suppose we've got all the time in the world for you to change your mind."));
                    return false;

                case "reluctant":
                    repeatSub = false;
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "If we \"die\" die, you can yell at me all you want."));
                    break;

                case "cSlaySelfNoBladeFail":
                case "cTakeFail":
                case "cGoStairs":
                case "anything":
                    repeatSub = false;
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Thank you."));
                    break;

                case "cGoHill":
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "There's nowhere for us to go, remember? The world out there is gone."));
                    break;
                    
                case "cSlayNoPrincessFail":
                    parser.printDialogueLine(new VoiceDialogueLine("She's already dead, thanks to your efforts."));
                    break;

                default:
                    this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Committed to Spectre here
        parser.printDialogueLine(new VoiceDialogueLine("I *made* this happy little place for you! Is this not a good enough reward for saving the world? An eternity of bliss? You... you ingrate!"));
        parser.printDialogueLine(new VoiceDialogueLine("Fine. Whatever. For the first time since time stopped meaning anything, you throw open the door to the basement and walk down the stairs."));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("The Princess' body is dust and bones, though the blade you used to slay her is still as pristine as the day you first held it."));
        parser.printDialogueLine(new VoiceDialogueLine("You pick up the blade, you stab yourself, and you *die.*"));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("The end. Nice knowing you."));

        return true;
    }

    /**
     * The player attempts to accept their reward for slaying the Princess (leads to the Good Ending)
     * @return false if the player has already claimed at least one vessel and is forced into Chapter II: The Spectre instead; true otherwise
     */
    private boolean ch1GoodEnding() {
        parser.printDialogueLine(new VoiceDialogueLine("Really? Well, if you ever change your mind, just let me know, I guess."));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("More happy time passes, though the word begins to lose its meaning. \"Time,\" that is, not \"happy.\" \"Happy\" still has plenty of meaning."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Please, shake yourself out of it. We have to get out of here."));
        parser.printDialogueLine(new VoiceDialogueLine("The little voice's pleas fall on deaf ears."));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("Eventually, you pass into a blissful state of pure existence. Though words like \"eventually\" and \"pass\" ceased to have any meaning to you long before that shift. You simply exist. Happy. Forever."));

        System.out.println();
        System.out.println();
        parser.printDialogueLine("--- Good Ending! =:) YOU DID IT!!! you saved EVERYONE! ---");

        if (this.isFirstVessel) return true;
        else {
            manager.attemptGoodEnding();

            System.out.println();
            parser.printDialogueLine(CANTSTRAY);
            return false;
        }
    }

    /**
     * The player slays the harsh Princess after hesitating (leads to Chapter II: The Adversary / The Tower / The Nightmare)
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding ch1SlayHarshHesitated() {
        parser.printDialogueLine(new VoiceDialogueLine("Doubt, unfortunately, clouds your thoughts as you attempt to run her through."));
        parser.printDialogueLine(new VoiceDialogueLine("A moment of distraction and hesitation is all she needed to sidestep your thrust and deliver a catastrophic blow to your jaw."));
        parser.printDialogueLine(new VoiceDialogueLine("It feels like you've been hit with a sledgehammer. You can feel bone grinding on bone where your jaw has been fractured."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Holy *shit* that *hurt!*"));
        parser.printDialogueLine(new VoiceDialogueLine("Though she's unarmed, the shock of that first strike is enough to stagger you, putting you and the Princess on somewhat equal footing."));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("Your blade slashes through the air again and again, and her fists connect with your body as many times or more, each impact as heavy as that first bone-crushing hit."));

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "giveUp", "[Give up.]", 0, Chapter.TOWER));
        activeMenu.add(new Option(this.manager, "finish", "[Finish the job.]", 0, Chapter.ADVERSARY));
        activeMenu.add(new Option(this.manager, "flee", "[Flee and lock her in the basement.]", 0, Chapter.NIGHTMARE));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "giveUp":
                    if (!manager.confirmContentWarnings(Chapter.TOWER)) break;

                    parser.printDialogueLine(new VoiceDialogueLine("Are you serious? *Sigh.* As internal bleeding sets in, the blade falls from your trembling hands, clattering to the ground uselessly."));
                    parser.printDialogueLine(new VoiceDialogueLine("You lacked the will to finish the job, your bruised and broken body falling to its knees before her."));
                    parser.printDialogueLine(new VoiceDialogueLine("The Princess, exhausted, chest heaving with heavy breaths, tosses the blade away from you."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "This is the end, isn't it?"));
                    parser.printDialogueLine(new PrincessDialogueLine("Is this really the best you could do? Look at you. Completely broken. I'd be lying if I said I wasn't a little disappointed."));
                    parser.printDialogueLine(new VoiceDialogueLine("She plants her foot on your chest and pushes you onto your back, the air leaving your lungs in a heavy puff."));
                    parser.printDialogueLine(new VoiceDialogueLine("And then she brings her knee to your throat."));
                    parser.printDialogueLine(new VoiceDialogueLine("She leans into it with the kind of weight you didn't think her slight frame could possibly possess, shattering your windpipe and leaving you starved for breath."));
                    parser.printDialogueLine(new PrincessDialogueLine("It's too bad. I was looking forward to some company."));

                    System.out.println();
                    parser.printDialogueLine(new VoiceDialogueLine("Everything goes dark, and you die."));

                    return ChapterEnding.TOTOWER;

                case "cSlayPrincess":
                    if (manager.hasVisited(Chapter.TOWER)) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                case "finish":
                    if (!manager.confirmContentWarnings(Chapter.ADVERSARY)) break;

                    parser.printDialogueLine(new VoiceDialogueLine("You and the Princess stare at each other, both gasping for breath, equally exhausted. You probably won't make it out of here alive, but you can at least make sure that she won't make it out of here, either."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Excuse me?"));
                    parser.printDialogueLine(new VoiceDialogueLine("Do you think this is what I wanted to happen? I have a duty to state the facts of the situation, and honestly, it's a miracle anyone is still standing right now."));
                    parser.printDialogueLine(new VoiceDialogueLine("Can you not feel all those ruptured organs bouncing around in there? If the Princess doesn't do our friend in herself, internal bleeding is certain to finish the job."));

                    System.out.println();
                    parser.printDialogueLine(new VoiceDialogueLine("The two of you clash for the final time. You feel your ribs break as she delivers a heavy blow, but you push through the pain, falling forward and sinking your blade deep into the Princess' heart."));
                    parser.printDialogueLine(new PrincessDialogueLine("O... oh."));

                    System.out.println();
                    parser.printDialogueLine(new VoiceDialogueLine("The two of you fall to the floor."));
                    parser.printDialogueLine(new PrincessDialogueLine("This was fun."));
                    parser.printDialogueLine(new VoiceDialogueLine("The Princess gasps, her voice an unhealthy rasp as her lungs start to fill with blood."));
                    parser.printDialogueLine(new PrincessDialogueLine("You put up more of a fight than I thought you would... But I have to wonder..."));
                    parser.printDialogueLine(new PrincessDialogueLine("Do you *really* think this is the end?"));

                    System.out.println();
                    parser.printDialogueLine(new VoiceDialogueLine("But you don't have time to worry over such things. Everything goes dark, and you die."));

                    return ChapterEnding.TOADVERSARY;

                case "cGoStairs":
                    if (manager.hasVisited(Chapter.NIGHTMARE)) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                case "flee":
                    if (!manager.confirmContentWarnings(Chapter.NIGHTMARE)) break;

                    parser.printDialogueLine(new VoiceDialogueLine("Are you *serious?*"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "It's a good idea. We've taken some bad hits, but we've dealt some, too. She has to be feeling it more than we are. Let's regroup upstairs. If we're lucky, maybe she'll just bleed out."));
                    parser.printDialogueLine(new VoiceDialogueLine("Fine. You make a mad dash to the basement stairs, the Princess' chains rattling as she tries to chase you, but pulling taut much too soon for her to catch up."));
                    parser.printDialogueLine(new PrincessDialogueLine("Do you really think you can just *walk out of here?*"));

                    System.out.println();
                    parser.printDialogueLine(new VoiceDialogueLine("She steps towards you, ignoring her chains."));
                    parser.printDialogueLine(new VoiceDialogueLine("They creak and strain as she pulls against them, until, finally, they break. The links clatter to the floor, useless."));
                    parser.printDialogueLine(new VoiceDialogueLine("She's free. Hurry."));

                    System.out.println();
                    parser.printDialogueLine(new VoiceDialogueLine("You push your broken body as she closes in, and just barely manage to pass the threshold of the basement doorway before she catches up to you."));

                    this.ch1ToNightmare(true, false);
                    return ChapterEnding.TONIGHTMAREFLED;
            }
        }

        throw new RuntimeException("No ending reached");
    }

    /**
     * The player slays the harsh Princess while afraid (leads to Chapter II: The Tower)
     */
    private void ch1SlayHarshForceTower() {
        parser.printDialogueLine(new VoiceDialogueLine("You charge the Princess, blade trembling in your hand, but you've already lost the battle."));
        parser.printDialogueLine(new VoiceDialogueLine("She casually sidesteps your thrust before knocking you to the ground with a single blow from her elbow."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "We shouldn't have hesitated..."));
        parser.printDialogueLine(new VoiceDialogueLine("But she doesn't stop there. She kicks you a few times for good measure, the pointed tip of her shoes feeling like a pickaxe against your fracturing bones, making sure you *stay* down."));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("As you lie crushed and broken on the basement floor, the Princess kneels on your throat with the kind of weight you didn't think her slight frame could possibly possess. As you gasp for air she eyes you with an intense curiosity."));
        parser.printDialogueLine(new VoiceDialogueLine("You shouldn't have let that fear creep into your heart. You had the upper hand, and now look at you."));
        parser.printDialogueLine(new PrincessDialogueLine("Is this really the best you could do? Look at you. Completely broken. I'd be lying if I said I wasn't a little disappointed."));
        parser.printDialogueLine(new VoiceDialogueLine("She applies more pressure, slowly squeezing what's left of your life out of your lungs."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "This is the end, isn't it?"));
        parser.printDialogueLine(new VoiceDialogueLine("I'm afraid it is."));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("Everything goes dark, and you die."));
    }

    /**
     * The player slays the harsh Princess after wondering if she's armed (leads to Chapter II: The Razor)
     */
    private ChapterEnding ch1SlayHarshForceRazor() {
        parser.printDialogueLine(new VoiceDialogueLine("You charge the Princess, blade in hand, but unfortunately, your earlier suspicions proved correct. A blade of her own slips down her sleeve and catches you in the neck."));
        parser.printDialogueLine(new VoiceDialogueLine("Blood sprays from the cut, your severed carotid artery painting the princess with strokes of red. You'd better finish your task quickly, before you run out of time."));

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "die", "[Die.]"));
        activeMenu.add(new Option(this.manager, "finish", "[Finish the job.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "die":
                    parser.printDialogueLine(new VoiceDialogueLine("Are you serious? *Sigh.*"));

                    System.out.println();
                    parser.printDialogueLine(new VoiceDialogueLine("The wound in your neck is too much for you to bear, and you collapse to the floor of the basement, rapidly bleeding out."));
                    parser.printDialogueLine(new VoiceDialogueLine("The Princess stands over you with an intense curiosity as you fade away."));
                    parser.printDialogueLine(new PrincessDialogueLine("Oops."));

                    System.out.println();
                    parser.printDialogueLine(new VoiceDialogueLine("Everything goes dark, and you die."));
                    
                    return ChapterEnding.TORAZOR;

                case "cSlayPrincess":
                case "finish":
                    parser.printDialogueLine(new VoiceDialogueLine("With the last bit of your will, you press forward, sinking the blade deep into the Princess' heart."));
                    parser.printDialogueLine(new PrincessDialogueLine("O... oh."));

                    System.out.println();
                    parser.printDialogueLine(new PrincessDialogueLine("The two of you collapse on the floor together, rapidly bleeding out."));
                    parser.printDialogueLine(new PrincessDialogueLine("Somehow I thought this would turn out a little differently. But I wonder..."));
                    parser.printDialogueLine(new PrincessDialogueLine("Do you really think *this* was enough to stop me?"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "It's like she's convinced she can't die."));

                    System.out.println();
                    parser.printDialogueLine(new VoiceDialogueLine("But you don't have time to worry over such things. Everything goes dark, and you die."));

                    return ChapterEnding.TORAZORMUTUAL;

                default:
                    this.giveDefaultFailResponse();
            }
        }

        throw new RuntimeException("No ending reached");
    }

    /**
     * The player attempts to free the harsh Princess
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding ch1RescueHarsh(boolean howFree) {
        boolean canPrisoner = !manager.hasVisited(Chapter.PRISONER);
        boolean canTower = !manager.hasVisited(Chapter.TOWER);

        boolean canFree = canPrisoner || canTower;
        boolean canSlay = !manager.hasVisitedAll(Chapter.ADVERSARY, Chapter.TOWER, Chapter.NIGHTMARE);

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("You walk up to the chains binding the Princess to the wall and give them a tug."));
        parser.printDialogueLine(new VoiceDialogueLine("They're large and heavy, far too solid for you to even imagine trying to break them apart."));

        if (howFree) {
            parser.printDialogueLine(new PrincessDialogueLine("If you don't have the key, maybe you should go looking for it. I'm sure it's somewhere upstairs."));
        } else {
            parser.printDialogueLine(new PrincessDialogueLine("I'm guessing you don't have the key."));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Maybe it's somewhere upstairs."));
        }

        parser.printDialogueLine(new VoiceDialogueLine("Doubtful. Whoever locked the Princess away down here intended for her to never see the light of day. They wouldn't have just left the key to her chains somewhere in the cabin."));

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "whatIfA", "\"And if there isn't a key... do you have any ideas? Besides me cutting you out of here?\"", howFree));
        activeMenu.add(new Option(this.manager, "whatIfB", "\"And if there isn't a key... do you have any ideas?\"", !howFree));
        activeMenu.add(new Option(this.manager, "check", "\"I'm going to check upstairs. Maybe the key's still lying around somewhere up there. And if not, maybe I can at least find something to break you free.\""));

        switch (parser.promptOptionsMenu(activeMenu)) {
            case "whatIfA":
                parser.printDialogueLine(new PrincessDialogueLine("That would be fine. I can lose an arm."));
                parser.printDialogueLine(new VoiceDialogueLine("She speaks with almost complete nonchalance."));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "If we were stuck down here for long enough, I'm sure we'd be nonchalant about cutting our way out. Anything to finally be free."));
                break;

            case "whatIfB":
                parser.printDialogueLine(new PrincessDialogueLine("Well, you do have that big sharp knife. You could always cut me out of here."));
                parser.printDialogueLine(new VoiceDialogueLine("She speaks with almost complete nonchalance."));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "If we were stuck down here for long enough, I'm sure we'd be nonchalant about cutting our way out. Anything to finally be free."));
                break;

            case "check":
                parser.printDialogueLine(new PrincessDialogueLine("I'll be here."));
                break;
        }
        
        System.out.println();
        this.currentLocation = GameLocation.STAIRS;
        this.reverseDirection = true;
        this.withPrincess = false;
        parser.printDialogueLine(new VoiceDialogueLine("You attempt to make your way out of the basement, but the door at the top of the stairs slams shut. You hear the click of a lock sliding into place."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Is someone else here?"));

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "shout", "(Explore) \"Hey! Let me out of here!\""));
        activeMenu.add(new Option(this.manager, "try", "(Explore) [Try the door.]"));
        activeMenu.add(new Option(this.manager, "return", "[Return to the bottom of the stairs.]"));

        boolean explore = false;
        boolean triedDoor = false;
        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "shout":
                    parser.printDialogueLine(new VoiceDialogueLine("Your shouts and pleas are met with silence."));

                    if (explore) {
                        parser.printDialogueLine(new VoiceDialogueLine("I'll repeat myself once again. You're here to slay the Princess, and you won't leave until the task is done."));
                    } else {
                        parser.printDialogueLine(new VoiceDialogueLine("You're here to slay the Princess, and you won't leave until the task is done."));
                    }
                    
                    explore = true;
                    break;

                case "cGoCabin":
                    activeMenu.setCondition("try", false);
                case "try":
                    explore = true;
                    
                    if (triedDoor) {
                        parser.printDialogueLine(new VoiceDialogueLine("You try the door again. It's still locked."));
                    } else {
                        parser.printDialogueLine(new VoiceDialogueLine("You try the door, but it's locked from the outside."));

                        if (explore) {
                            parser.printDialogueLine(new VoiceDialogueLine("I'll repeat myself once again. You're here to slay the Princess, and you won't leave until the task is done."));
                        } else {
                            parser.printDialogueLine(new VoiceDialogueLine("You're here to slay the Princess, and you won't leave until the task is done."));
                        }
                    }

                    explore = true;
                    triedDoor = true;
                    break;

                case "cGoBasement":
                case "return":
                    this.repeatActiveMenu = false;
                    break;
                
                case "cSlayNoPrincessFail":
                    parser.printDialogueLine(new VoiceDialogueLine("The Princess is in the basement below, remember? You can't slay her from here."));
                    break;

                default:
                    this.giveDefaultFailResponse(this.activeOutcome);
            }
        }

        parser.printDialogueLine(new VoiceDialogueLine("You make your way back to the bottom of the stairs. This would have been so much easier if you'd simply slain her like you were supposed to."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Easier for whom?"));
        parser.printDialogueLine(new VoiceDialogueLine("Easier for *everyone.*"));

        System.out.println();
        parser.printDialogueLine(new PrincessDialogueLine("I heard the door slam... they locked you down here too, didn't they?"));
        parser.printDialogueLine(new PrincessDialogueLine("The knife. Pick it up and cut me out of here."));
        parser.printDialogueLine(new VoiceDialogueLine("You won't like what happens if you do that..."));

        this.hasBlade = true;
        this.canSlayPrincess = true;

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "save", !canFree, "[Save the Princess.]", 0));
        activeMenu.add(new Option(this.manager, "slay", !canSlay, "[Slay the Princess.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "save":
                    if (!manager.confirmContentWarnings("mutilation; loss of bodily autonomy", true)) break;

                    this.repeatActiveMenu = false;
                    break;

                case "cSlayPrincess":
                    if (!canSlay) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                case "slay":
                    return this.ch1RescueSlayHarsh();

                default:
                    this.giveDefaultFailResponse();
            }
        }

        // Save continues here
        this.canSlayPrincess = false;
        parser.printDialogueLine(new VoiceDialogueLine("Against your better judgment, you place the blade against the Princess's arm, just above the massive, unyielding chain."));
        parser.printDialogueLine(new VoiceDialogueLine("You cut into her flesh."));
        parser.printDialogueLine(new VoiceDialogueLine("The blade is sharp, and you make quick work of it. Before long, you're able to crack through bone, and she pulls the bleeding stub of her arm through the iron gauntlet."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "She didn't so much as utter a sound..."));
        parser.printDialogueLine(new VoiceDialogueLine("Free from her bindings, the Princess turns to face you, her fierce gaze meeting your eye."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "How is she so composed after losing an arm? It's like she isn't even bothered by it."));
        parser.printDialogueLine(new PrincessDialogueLine("Thank you. Now let's get out of here."));

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "door", "[Approach the locked door.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "cGoStairs":
                case "door":
                    this.repeatActiveMenu = false;
                    break;
                    
                default:
                    this.giveDefaultFailResponse(activeOutcome);
            }
        }
        
        parser.printDialogueLine(new VoiceDialogueLine("No. We won't have any of that. The stakes are too high. You can't just let her escape into the world."));
        parser.printDialogueLine(new VoiceDialogueLine("... no. *I* can't just let her escape into the world."));
        parser.printDialogueLine(new VoiceDialogueLine("As the Princess approaches the bottom stair, your body steps forward and raises the blade."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Wait... this isn't fair. You can't just *do* that!"));
        parser.printDialogueLine(new VoiceDialogueLine("Watch me."));

        this.canSlayPrincess = true;
        Option slay = new Option(this.manager, "slay", !canTower, "[Slay the Princess.]", 0);

        this.activeMenu = new OptionsMenu();
        for (int i = 0; i < 13; i++) activeMenu.add(slay, "slay" + i);
        activeMenu.add(new Option(this.manager, "warn", !canPrisoner, "[Warn her.]"));
        for (int i = 0; i < 4; i++) activeMenu.add(slay, "slay" + (12 + i));
        
        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "cSlayPrincess":
                    if (!canTower) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                case "slay0":
                case "slay1":
                case "slay2":
                case "slay3":
                case "slay4":
                case "slay5":
                case "slay6":
                case "slay7":
                case "slay8":
                case "slay9":
                case "slay10":
                case "slay11":
                case "slay12":
                case "slay13":
                case "slay14":
                case "slay15":
                case "slay16":
                case "slay17":
                case "slay18":
                    if (!manager.confirmContentWarnings(Chapter.TOWER)) break;
                    
                    parser.printDialogueLine(new VoiceDialogueLine("You bring the blade down and plunge it into the Princess's back. *Finally.*"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Okay. There's no going back now."));
                    parser.printDialogueLine(new VoiceDialogueLine("Though the blade left a deep gash in her shoulder, she barely so much as flinches, turning around to stare at you incredulously."));
                    parser.printDialogueLine(new PrincessDialogueLine("Are you serious?"));
                    parser.printDialogueLine(new PrincessDialogueLine("I don't know what came over you, but if we're doing this, I guess I'll have to kill you."));
                    parser.printDialogueLine(new PrincessDialogueLine("Do you think I need both of my arms to do that? I can beat you to death with one."));
                    parser.printDialogueLine(new PrincessDialogueLine("But I don't have to tell you that. I'll go ahead and show you."));

                    this.activeMenu = new OptionsMenu();
                    activeMenu.add(slay);
                    activeMenu.add(new Option(this.manager, "giveUp", "[Give up.]"));

                    this.repeatActiveMenu = true;
                    while (repeatActiveMenu) {
                        switch (parser.promptOptionsMenu(activeMenu)) {
                            case "cSlayPrincess":
                            case "slay":
                                this.ch1RescueControlledSlayHarsh();
                                return ChapterEnding.TOTOWER;

                            case "giveUp":
                                parser.printDialogueLine(new VoiceDialogueLine("*Sigh.* As the blade falls from your trembling hands, the Princess rears back, readying a bone-shattering haymaker."));
                                parser.printDialogueLine(new VoiceDialogueLine("You fall to your knees. You're barely able to process the ringing in your ears before she hits you again."));
                                parser.printDialogueLine(new VoiceDialogueLine("Every blow is as punishing as the first. You feel bones shatter with every impact, unknown ruptures blossoming with blood somewhere inside of you."));
                                parser.printDialogueLine(new VoiceDialogueLine("If we're lucky, the wound you managed to inflict will be enough to at least delay her escape from this place. If we're very lucky, it will kill her before she gets out."));
                                parser.printDialogueLine(new PrincessDialogueLine("Too weak to even try fighting back. How disappointing."));
                                parser.printDialogueLine(new VoiceDialogueLine("She places a confident heel on your chest and pushes you down to the ground."));
                                parser.printDialogueLine(new VoiceDialogueLine("Her knee falls to your throat, your windpipe crushed beneath a weight you didn't think her slight form could possibly possess."));
                                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "It can't just end like this, right?"));
                                parser.printDialogueLine(new VoiceDialogueLine("I'm sorry, but it's over."));

                                System.out.println();
                                parser.printDialogueLine(new VoiceDialogueLine("Everything goes dark, and you die."));
                                return ChapterEnding.TOTOWER;

                            default:
                                this.giveDefaultFailResponse();
                        }
                    }

                case "warn":
                    this.repeatActiveMenu = false;
                    break;

                default:
                    this.giveDefaultFailResponse();
            }
        }

        parser.printDialogueLine(new VoiceDialogueLine("Stop that."));
        parser.printDialogueLine(new PrincessDialogueLine("I thought this was a little too easy."));
        parser.printDialogueLine(new VoiceDialogueLine("Your body lunges forward to sink the blade into her back, but the Princess swiftly moves out the way before you can connect."));
        parser.printDialogueLine(new VoiceDialogueLine("Stop it! Stop trying to resist me! I'm trying to get you out of here alive."));
        
        this.activeMenu = new OptionsMenu();
        for (int i = 0; i < 13; i++) activeMenu.add(slay, "slay" + i);
        activeMenu.add(new Option(this.manager, "resist", !canPrisoner, "[Resist.]", 0));
        for (int i = 0; i < 4; i++) activeMenu.add(slay, "slay" + (12 + i));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "cSlayPrincess":
                    if (!canTower) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                case "slay0":
                case "slay1":
                case "slay2":
                case "slay3":
                case "slay4":
                case "slay5":
                case "slay6":
                case "slay7":
                case "slay8":
                case "slay9":
                case "slay10":
                case "slay11":
                case "slay12":
                case "slay13":
                case "slay14":
                case "slay15":
                case "slay16":
                case "slay17":
                case "slay18":
                    if (!manager.confirmContentWarnings(Chapter.TOWER)) break;

                    this.ch1RescueControlledSlayHarsh();
                    return ChapterEnding.TOTOWER;

                case "resist":
                    if (!manager.confirmContentWarnings(Chapter.PRISONER)) break;

                    this.repeatActiveMenu = false;
                    break;

                default:
                    this.giveDefaultFailResponse();
            }
        }

        // Committed to Prisoner
        parser.printDialogueLine(new VoiceDialogueLine("The blade! Move. The. *Blade!*"));
        parser.printDialogueLine(new PrincessDialogueLine("You're doing your best to help me, aren't you? I can see the conflict in your eyes."));
        parser.printDialogueLine(new PrincessDialogueLine("I'll make this quick."));
        parser.printDialogueLine(new VoiceDialogueLine("She steps forward and pries the blade from your rigid hands."));
        parser.printDialogueLine(new PrincessDialogueLine("Maybe I'll see you in another life."));
        parser.printDialogueLine(new VoiceDialogueLine("And then she slits your throat with an almost clinical ease."));
        parser.printDialogueLine(new VoiceDialogueLine("Her face remains unchanged as she watches you collapse to the ground, blood flowing from your butchered neck."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "This is the end, isn't it?"));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("I'm afraid it is. Everything goes dark, and you die. I hope it was worth it."));

        return ChapterEnding.TOPRISONER;
    }

    /**
     * The player allows the Narrator to take control of their body and slay the harsh Princess after setting her free (leads to Chapter II: the Tower)
     */
    private void ch1RescueControlledSlayHarsh() {
        parser.printDialogueLine(new VoiceDialogueLine("*Thank* you."));
        parser.printDialogueLine(new VoiceDialogueLine("You swing your arm towards her throat, the blade singing through the air."));
        parser.printDialogueLine(new VoiceDialogueLine("But she's ready for it. She grabs your arm, her grip like a stone vice."));
        parser.printDialogueLine(new VoiceDialogueLine("You drop the blade. Pathetically."));
        parser.printDialogueLine(new VoiceDialogueLine("She lets go, and faster than you can react, rears back and hits you with a bone-shattering haymaker."));
        parser.printDialogueLine(new VoiceDialogueLine("There's a ringing in your ears. You're fairly certain you can feel bone grinding against bone where she fractured your jaw, but your body isn't allowing you to feel much right now, adrenaline coursing through your system and numbing your nerves."));
        parser.printDialogueLine(new VoiceDialogueLine("You fall to your knees. You're barely able to bring your trembling arms up to defend yourself before she hits you again."));
        parser.printDialogueLine(new VoiceDialogueLine("Every blow is as punishing as the first. You feel bones shatter with every impact, unknown ruptures blossoming with blood somewhere inside of you."));
        parser.printDialogueLine(new PrincessDialogueLine("You poor thing. I'll go ahead and put you out of your misery."));
        parser.printDialogueLine(new VoiceDialogueLine("She places a confident heel on your chest and pushes you down to the ground."));
        parser.printDialogueLine(new VoiceDialogueLine("Her knee falls to your throat, your windpipe crushed beneath a weight you didn't think her slight form could possibly possess."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "It can't just end like this, right?"));
        parser.printDialogueLine(new VoiceDialogueLine("I'm sorry, but it's over."));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("Everything goes dark, and you die."));
    }

    /**
     * The player decides to slay the harsh Princess after initially deciding to free her (leads to Chapter II: The Adversary / The Tower / The Nightmare)
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding ch1RescueSlayHarsh() {
        parser.printDialogueLine(new VoiceDialogueLine("Without hesitation, you bring the blade down. The Princess flinches as you strike, and your weapon sinks into her shoulder."));
        parser.printDialogueLine(new PrincessDialogueLine("You bastard! If I have to kill you to leave this place, I'll *do it.*"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I thought we had the upper hand, but it's as if she's barely even threatened by us."));
        parser.printDialogueLine(new VoiceDialogueLine("It's an act. She's unarmed and there's nothing she can do to hurt you."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I'm not so sure..."));
        parser.printDialogueLine(new VoiceDialogueLine("Don't waver now."));
        parser.printDialogueLine(new VoiceDialogueLine("As you raise your blade to strike again, she kicks out, knocking your legs out from under you."));
        parser.printDialogueLine(new VoiceDialogueLine("The two of you struggle on the ground. You lash out with the blade, slicing wherever you can. Her fists connect with your body again and again, each blow stronger than the last, shattering bone and rupturing tissue with reckless abandon."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Forget trying to rescue her. This is about *survival* now. Give her everything you've got."));

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "slay", "[Slay the Princess.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "cSlayPrincess":
                case "slay":
                    this.repeatActiveMenu = false;
                    break;

                default:
                    this.giveDefaultFailResponse();
            }
        }
        
        parser.printDialogueLine(new VoiceDialogueLine("You roll out of her grasp and shakily push yourself back to your feet."));
        parser.printDialogueLine(new VoiceDialogueLine("Though every inch of you is in pain, the Princess probably has it worse -- blood pours out from countless gashes, staining her once pristine dress. She pauses for a moment, catching her breath, staring at you with wild eyes."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "We can still turn this around."));

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "giveUp", "[Give up.]", Chapter.TOWER));
        activeMenu.add(new Option(this.manager, "finish", "[Finish the job.]", Chapter.ADVERSARY));
        activeMenu.add(new Option(this.manager, "run", "[Run for the stairs and lock her in the basement. Maybe she'll bleed out.]", Chapter.NIGHTMARE));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "giveUp":
                    if (!manager.confirmContentWarnings(Chapter.TOWER)) break;
                    
                    parser.printDialogueLine(new VoiceDialogueLine("Are you serious? *Sigh.* As the force of her blows overwhelms your body, the blade falls from your trembling hands, clattering uselessly to the cobblestones below. I suppose you just lacked the will to finish the job."));
                    parser.printDialogueLine(new VoiceDialogueLine("The Princess, wounded but still alive, turns to face you."));
                    parser.printDialogueLine(new VoiceDialogueLine("She places a confident heel on your chest, and pushes you to the ground."));
                    parser.printDialogueLine(new VoiceDialogueLine("Her knee slides to your throat and your windpipe is crushed under weight you didn't think her frame could possibly possess."));
                    parser.printDialogueLine(new PrincessDialogueLine("Pathetic."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "This is the end, isn't it?"));

                    System.out.println();
                    parser.printDialogueLine(new VoiceDialogueLine("Everything goes dark, and you die."));

                    return ChapterEnding.TOTOWERPATHETIC;

                case "cSlayPrincess":
                    if (manager.hasVisited(Chapter.ADVERSARY)) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                case "finish":
                    if (!manager.confirmContentWarnings(Chapter.ADVERSARY)) break;
                    
                    this.repeatActiveMenu = false;
                    parser.printDialogueLine(new VoiceDialogueLine("You steel your resolve and take another step towards the Princess. You probably won't make it out of here alive, but you can at least make sure she won't make it out of here, either."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Excuse me? What was that about not making it out of here alive?"));
                    parser.printDialogueLine(new VoiceDialogueLine("Do you think this is what I wanted to happen? I have a duty to state the facts of the situation, and honestly, it's a miracle *anyone* is still standing right now."));
                    parser.printDialogueLine(new VoiceDialogueLine("Can you not feel all those ruptured organs bouncing around in there? If the Princess doesn't do our friend in herself, internal bleeding is certain to finish the job."));

                    System.out.println();
                    parser.printDialogueLine(new VoiceDialogueLine("The two of you clash for the final time. You feel your ribs break as she delivers a heavy blow, but you push through the pain, falling forward and sinking your blade deep into the Princess's heart."));
                    parser.printDialogueLine(new PrincessDialogueLine("O... oh."));
                    parser.printDialogueLine(new PrincessDialogueLine("This was fun. You put up more of a fight than I thought you would. But I have to wonder..."));
                    parser.printDialogueLine(new PrincessDialogueLine("Do you *really* think this is the end?"));
                    parser.printDialogueLine(new VoiceDialogueLine("But you don't have time to worry over her words."));

                    System.out.println();
                    parser.printDialogueLine(new VoiceDialogueLine("Everything goes dark, and you die."));

                    return ChapterEnding.TOADVERSARY;

                case "cGoStairs":
                    if (manager.hasVisited(Chapter.NIGHTMARE)) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                case "run":
                    if (!manager.confirmContentWarnings(Chapter.NIGHTMARE)) {
                        break;
                    }
                    
                    this.repeatActiveMenu = false;
                    parser.printDialogueLine(new VoiceDialogueLine("The Princess is still chained to the wall. There's nothing she can do to stop you from getting out of here."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "What if she doesn't succumb to her wounds? Whatever she is, she's *so* much more dangerous than I thought she'd be."));

                    System.out.println();
                    parser.printDialogueLine(new VoiceDialogueLine("You rush up the stairs and dive past the threshold. You're safe. For now."));

                    this.ch1ToNightmare(true, false);
                    return ChapterEnding.TONIGHTMAREFLED;

                default:
                    this.giveDefaultFailResponse();
            }
        }

        throw new RuntimeException("No ending reached");
    }

    /**
     * The player decides to lock the Princess in the basement (leads to Chapter II: The Nightmare)
     * @param wounded whether the Princess is wounded
     * @param lostArm whether the Princess lost her arm
     */
    private void ch1ToNightmare(boolean wounded, boolean lostArm) {
        this.currentLocation = GameLocation.CABIN;
        this.withPrincess = false;
        
        parser.printDialogueLine(new VoiceDialogueLine("You close the basement door, locking it behind you and quickly barricading it with the heavy wooden table that once held the blade."));
        if (wounded) parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Okay. We can make this work. She has an awful wound and we have all the time in the world. Playing jailkeeper for a while might make things a little easier."));
        else parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Okay. We can make this work."));
        parser.printDialogueLine(new VoiceDialogueLine("You settle in against the far wall to watch the basement door."));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("It isn't long before you start to drift off, your eyelids heavy with fatigue. But sleep doesn't come. Instead your rest is broken by a piercing, wailing voice calling out to you from the other side of the door."));
        parser.printDialogueLine(new PrincessDialogueLine("I know you're still there. Why don't you make things easier on yourself and let me out?"));
        parser.printDialogueLine(new VoiceDialogueLine("She bangs on the door over and over again."));
        parser.printDialogueLine(new PrincessDialogueLine("It's not like this little door'll hold for very long anyways... and it's probably a good idea to try and get back on my good side."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "She sounds... terrifying. Like she's less of the Princess you saw and more like something out of a nightmare."));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("As she violently rattles the door, you do your best to shut her out of your mind."));
        parser.printDialogueLine(new PrincessDialogueLine("When I get out of here I'm going to pick you apart piece by piece. I won't forget what you did, and I'll never forgive it."));
        parser.printDialogueLine(new PrincessDialogueLine("You don't know the kind of enemy you've made tonight."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "It doesn't sound like she's getting any weaker..."));
        parser.printDialogueLine(new VoiceDialogueLine("No. It doesn't."));

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "threat", "\"Threaten me all you want! All it does is ease my guilty conscience.\""));
        activeMenu.add(new Option(this.manager, "notPrincess", "\"Whatever you are, you're not a Princess. Go ahead and waste your energy. I'll be waiting for you.\"", this.isHarsh));
        activeMenu.add(new Option(this.manager, "act", "\"So all of that was just an act, wasn't it? You're not really innocent or harmless. You're not even a princess. You're a *monster.*\"", !this.isHarsh));
        activeMenu.add(new Option(this.manager, "bleedOut", "\"Bang on the door all you want. It'll only make you bleed out faster.\"", wounded && !this.isHarsh));
        activeMenu.add(new Option(this.manager, "ignore", "[Ignore her and go to sleep.]"));

        switch (parser.promptOptionsMenu(activeMenu)) {
            case "threat":
                parser.printDialogueLine(new PrincessDialogueLine("These aren't threats. These are *promises.* Sooner or later you're going to have to sleep, and I'll make sure you never see the light of day again."));
                break;
            
            case "notPrincess":
                parser.printDialogueLine(new PrincessDialogueLine("I wouldn't be so sure about outlasting me. You're so... brittle. So go ahead. Rest. Do whatever you think will help you be prepared. But know that I am coming for you, and that when I find you, I will make you hurt."));
                break;

            case "act":
                parser.printDialogueLine(new PrincessDialogueLine("I can be innocent and harmless... if I want to be. Teasing me with fresh air and a chance to finally live freely doesn't inspire me to play nice."));
                break;

            case "bleedOut":
                if (lostArm) parser.printDialogueLine(new PrincessDialogueLine("Do you think losing an arm is actually enough to do me in? I can always find another. I'm not as frail as you think."));
                else parser.printDialogueLine(new PrincessDialogueLine("Do you think a couple cuts is enough to do me in? I'm not as frail as you think."));
                break;

            case "ignore":
                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Just ignore her. Maybe the banging and wailing will stop if you just don't pay attention to it."));
                break;
        }
        
        parser.printDialogueLine(new VoiceDialogueLine("You put the Princess's threats out of your mind as best as you can and huddle up against the wall."));

        System.out.println();
        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("You jolt awake in the middle of the night to silence in the cabin. The ruckus has stopped, and the door to the basement is ajar, its lock broken and the table shoved out the way."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Where is she?"));

        System.out.println();
        parser.printDialogueLine("She appears in the doorway, bathed in shadows and staring at you with unnaturally wide, unblinking eyes. She seems... wrong, somehow. Almost inhuman.");
        parser.printDialogueLine(new PrincessDialogueLine("Thanks for helping me get out of that awful basement."));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("You try and stumble to your feet, but as the Princess draws near, it's as though your body simply stops working."));
        parser.printDialogueLine(new VoiceDialogueLine("It isn't all at once. The paralysis comes in waves. First your toes go numb, and then your feet, and then your legs. You lie prone on the floor of the cabin, unable to do anything but witness her approach."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Whose side are you on?"));
        parser.printDialogueLine(new VoiceDialogueLine("Yours, of course. But I have a duty to uphold the truth. Lying about the facts of the situation doesn't change them."));

        System.out.println();
        parser.printDialogueLine(new PrincessDialogueLine("So helpless. I can take my time with you, can't I?"));
        parser.printDialogueLine(new VoiceDialogueLine("She steps closer, one silent footfall at a time, cocking her head in curiosity as you feel your organs shutting down one by one."));
        parser.printDialogueLine(new PrincessDialogueLine("Or maybe I can't take my time with you. You don't look well. A little green around the gills..."));
        parser.printDialogueLine(new PrincessDialogueLine("What a shame. If you'd only helped me get out of here. We could have done such wonderful things together."));
        parser.printDialogueLine(new VoiceDialogueLine("Your lungs stop drawing in breath, and your heart freezes in your chest. You have seconds left."));
        parser.printDialogueLine(new PrincessDialogueLine("I'd say better luck next time, but we both know that this is the end, don't we?"));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "It can't be. This can't actually be how everything ends..."));
        parser.printDialogueLine(new VoiceDialogueLine("I'm sorry, but it is."));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("Everything goes dark, and you die."));
    }





    /**
     * Runs the opening sequence of Chapter II, from the opening conversation up until the player enters the cabin
     * @param youDied whether the player died in Chapter I
     * @param princessDied whether the Princess died in Chapter I
     * @param liedTo whether the player feels like the Narrator lied to them in Chapter I
     * @param chapterSpecific several Chapter-specific variables depending on the specific outcome of Chapter I
     * @return false if the player chooses to abort the chapter (and therefore the cycle); true otherwise
     */
    private boolean chapter2Intro(boolean youDied, boolean princessDied, boolean liedTo) {
        manager.setNowPlaying("Fragmentation");

        if (this.isFirstVessel) manager.setFirstPrincess(this.activeChapter);

        parser.printDialogueLine(new VoiceDialogueLine("You're on a path in the woods. And at the end of that path is a cabin. And in the basement of that cabin is a princess."));
        parser.printDialogueLine(new VoiceDialogueLine("You're here to slay her. If you don't, it will be the end of the world."));

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "dejaVu", "(Explore) I'm getting a terrible sense of deja vu."));
        activeMenu.add(new Option(this.manager, "dejaVu2", "(Explore) This is more than just deja vu, though. I'm pretty sure this whole thing really just happened.", activeMenu.get("dejaVu")));
        activeMenu.add(new Option(this.manager, "happened", "(Explore) Wait... hasn't this already happened?"));
        activeMenu.add(new Option(this.manager, "no", "(Explore) Okay, no."));
        activeMenu.add(new Option(this.manager, "died", "(Explore) But I died! What am I doing here?", this.activeChapter != Chapter.SPECTRE));
        activeMenu.add(new Option(this.manager, "killedSelf", "(Explore) But I killed myself! What am I doing here?", this.activeChapter == Chapter.SPECTRE));
        activeMenu.add(new Option(this.manager, "alreadyKilled", "(Explore) But I already killed the Princess.", this.activeChapter == Chapter.SPECTRE));
        activeMenu.add(new Option(this.manager, "trapped", "(Explore) You trapped me here after I slew her last time. I'm not going to play along this time.", this.activeChapter == Chapter.SPECTRE));
        activeMenu.add(new Option(this.manager, "killMe", "(Explore) She's going to kill me again!", youDied));
        activeMenu.add(new Option(this.manager, "slewHer", "(Explore) But I already slew the Princess. Sure, she *also* killed me, but I definitely got her. Why am I here again?", youDied && princessDied));
        activeMenu.add(new Option(this.manager, "wise", "(Explore) Oh, you bastard! You're in for it now. I'm wise to your tricks!", liedTo));
        activeMenu.add(new Option(this.manager, "assume", "(Explore)  Let's assume I'm telling the truth, and all of this really did already happen. Why should I listen to you? Why should I bother doing *anything?*", false));
        activeMenu.add(new Option(this.manager, "defy", "(Explore) I'm with them. I'm going to find a way to save her from that cabin.", activeMenu.get("assume"), this.activeChapter == Chapter.DAMSEL));
        activeMenu.add(new Option(this.manager, "princess", "(Explore) Let's talk about this Princess...", activeMenu.get("assume")));
        activeMenu.add(new Option(this.manager, "proceed", "[Proceed to the cabin.]"));
        activeMenu.add(new Option(this.manager, "abort", "[Turn around and leave.]", 0));

        boolean shareDied = false;
        boolean pessimismComment = false;

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "dejaVu":
                    activeMenu.setCondition("happened", false);
                    activeMenu.setCondition("no", false);
                    activeMenu.setCondition("died", false);
                    activeMenu.setCondition("killedSelf", false);
                    activeMenu.setCondition("alreadyKilled", false);
                    activeMenu.setCondition("trapped", false);
                    activeMenu.setCondition("killMe", false);
                    activeMenu.setCondition("slewHer", false);
                    activeMenu.setCondition("wise", false);
                    if (this.ch2Voice == Voice.BROKEN) shareDied = true;

                    parser.printDialogueLine(new VoiceDialogueLine("A terrible sense of deja vu? No, you don't have that. This is the first time either of us have been here."));
                    this.ch2IntroShareLoop(false);
                    break;

                case "dejaVu2":
                    activeMenu.setCondition("assume", true);
                    
                    parser.printDialogueLine(new VoiceDialogueLine("We could go back and forth on this forever, and it won't get you any closer to doing your job and saving the world. So let's just agree to disagree."));
                    break;

                case "happened":
                    activeMenu.setCondition("assume", true);
                    activeMenu.setCondition("dejaVu", false);
                    activeMenu.setCondition("no", false);
                    activeMenu.setCondition("died", false);
                    activeMenu.setCondition("killedSelf", false);
                    activeMenu.setCondition("alreadyKilled", false);
                    activeMenu.setCondition("trapped", false);
                    activeMenu.setCondition("killMe", false);
                    activeMenu.setCondition("slewHer", false);
                    activeMenu.setCondition("wise", false);
                    if (this.ch2Voice == Voice.BROKEN) shareDied = true;
                    
                    parser.printDialogueLine(new VoiceDialogueLine("It hasn't. Or if it has, I certainly haven't been a part of it. We've just met for the first time, you and I."));
                    this.ch2IntroShareLoop(false);
                    break;

                case "no":
                    activeMenu.setCondition("assume", true);
                    activeMenu.setCondition("dejaVu", false);
                    activeMenu.setCondition("happened", false);
                    activeMenu.setCondition("died", false);
                    activeMenu.setCondition("killedSelf", false);
                    activeMenu.setCondition("alreadyKilled", false);
                    activeMenu.setCondition("trapped", false);
                    activeMenu.setCondition("killMe", false);
                    activeMenu.setCondition("slewHer", false);
                    activeMenu.setCondition("wise", false);
                    if (this.ch2Voice == Voice.BROKEN) shareDied = true;
                    
                    parser.printDialogueLine(new VoiceDialogueLine("Oh, don't you start grandstanding about morals. The fate of the world is at risk right now, and the life of a mere Princess shouldn't stop you from saving us all."));
                    this.ch2IntroShareLoop(false);
                    break;

                case "died":
                    activeMenu.setCondition("assume", true);
                    activeMenu.setCondition("dejaVu", false);
                    activeMenu.setCondition("happened", false);
                    activeMenu.setCondition("no", false);
                    activeMenu.setCondition("killedSelf", false);
                    activeMenu.setCondition("alreadyKilled", false);
                    activeMenu.setCondition("trapped", false);
                    activeMenu.setCondition("killMe", false);
                    activeMenu.setCondition("slewHer", false);
                    activeMenu.setCondition("wise", false);
                    shareDied = true;
                    
                    parser.printDialogueLine(new VoiceDialogueLine("I can assure you that you're not dead. And to answer your second question, you're here to slay the Princess. I literally told you that a second ago."));
                    this.ch2IntroShareLoop(true);
                    break;

                case "killedSelf":
                    activeMenu.setCondition("assume", true);
                    activeMenu.setCondition("dejaVu", false);
                    activeMenu.setCondition("happened", false);
                    activeMenu.setCondition("no", false);
                    activeMenu.setCondition("died", false);
                    activeMenu.setCondition("alreadyKilled", false);
                    activeMenu.setCondition("trapped", false);
                    activeMenu.setCondition("wise", false);
                    shareDied = true;
                    
                    parser.printDialogueLine(new VoiceDialogueLine("I can assure you that you're not dead. And to answer your second question, you're here to slay the Princess. I literally told you that a second ago."));
                    this.ch2IntroShareLoop(false);
                    break;

                case "alreadyKilled":
                    activeMenu.setCondition("assume", true);
                    activeMenu.setCondition("dejaVu", false);
                    activeMenu.setCondition("happened", false);
                    activeMenu.setCondition("no", false);
                    activeMenu.setCondition("died", false);
                    activeMenu.setCondition("killedSelf", false);
                    activeMenu.setCondition("trapped", false);
                    activeMenu.setCondition("wise", false);
                    
                    parser.printDialogueLine(new VoiceDialogueLine("I can assure you that you didn't."));
                    this.ch2IntroShareLoop(false);
                    break;

                case "trapped":
                    activeMenu.setCondition("assume", true);
                    activeMenu.setCondition("dejaVu", false);
                    activeMenu.setCondition("happened", false);
                    activeMenu.setCondition("no", false);
                    activeMenu.setCondition("died", false);
                    activeMenu.setCondition("killedSelf", false);
                    activeMenu.setCondition("alreadyKilled", false);
                    activeMenu.setCondition("wise", false);
                    
                    parser.printDialogueLine(new VoiceDialogueLine("How unfortunate that the sole person capable of slaying the Princess also seems to be somewhat insane. Oh, well. So long as you get the job done, it doesn't matter what sort of mental state you're in."));
                    this.ch2IntroShareLoop(false);
                    break;

                case "killMe":
                    activeMenu.setCondition("assume", true);
                    activeMenu.setCondition("dejaVu", false);
                    activeMenu.setCondition("happened", false);
                    activeMenu.setCondition("no", false);
                    activeMenu.setCondition("died", false);
                    activeMenu.setCondition("slewHer", false);
                    activeMenu.setCondition("wise", false);
                    shareDied = true;
                    
                    parser.printDialogueLine(new VoiceDialogueLine("Again? People don't die twice. You haven't even met the Princess, and I hardly think she'd be capable of killing someone as skilled and courageous as yourself."));
                    this.ch2IntroShareLoop(false);
                    break;

                case "slewHer":
                    activeMenu.setCondition("assume", true);
                    activeMenu.setCondition("dejaVu", false);
                    activeMenu.setCondition("happened", false);
                    activeMenu.setCondition("no", false);
                    activeMenu.setCondition("died", false);
                    activeMenu.setCondition("killMe", false);
                    activeMenu.setCondition("wise", false);
                    
                    parser.printDialogueLine(new VoiceDialogueLine("I can assure you that you didn't slay her, and that she didn't kill you. People don't just spring back to life after dying, and the two of us are meeting for the very first time."));
                    this.ch2IntroShareLoop(false);
                    break;

                case "wise":
                    activeMenu.setCondition("assume", true);
                    activeMenu.setCondition("dejaVu", false);
                    activeMenu.setCondition("happened", false);
                    activeMenu.setCondition("no", false);
                    activeMenu.setCondition("died", false);
                    activeMenu.setCondition("killedSelf", false);
                    activeMenu.setCondition("alreadyKilled", false);
                    activeMenu.setCondition("trapped", false);
                    activeMenu.setCondition("killMe", false);
                    activeMenu.setCondition("slewHer", false);
                    
                    parser.printDialogueLine(new VoiceDialogueLine("My tricks? What on earth are you talking about? We've just met for the first time."));
                    this.ch2IntroShareLoop(false);
                    break;

                case "assume":
                    this.ch2IntroAssumeTruth(youDied, princessDied, shareDied);
                    break;

                case "princess":
                    switch (this.ch2IntroAskPrincess(youDied, princessDied)) {
                        case 0: break;

                        case 1:
                            pessimismComment = true;
                            break;

                        case 2:
                            this.repeatActiveMenu = false;
                            break;

                        case 3:
                            switch (this.ch2AttemptAbortVessel()) {
                                case 0: return false;

                                case 1:
                                    this.canTryAbort = false;
                                    activeMenu.setGreyedOut("abort", true);
                                    break;

                                case 3:
                                    this.skipHillDialogue = true;
                                case 2:
                                    this.repeatActiveMenu = false;
                                    break;
                            }
                            break;
                    }

                    break;

                case "cGoHill":
                case "proceed":
                    this.repeatActiveMenu = false;
                    break;

                case "cGoLeave":
                    if (!this.canTryAbort) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                case "abort":
                    switch (this.ch2AttemptAbortVessel()) {
                        case 0: return false;

                        case 1:
                            this.canTryAbort = false;
                            activeMenu.setGreyedOut("abort", true);
                            break;

                        case 3:
                            this.skipHillDialogue = true;
                        case 2:
                            this.repeatActiveMenu = false;
                            break;
                    }

                    break;

                default:
                    this.giveDefaultFailResponse(activeOutcome);
            }
        }
        
        this.currentLocation = GameLocation.HILL;
        if (!this.skipHillDialogue) {
            System.out.println();
            parser.printDialogueLine(new DialogueLine("You emerge into the clearing. The cabin waits at the top of the hill."));
            
            parser.printDialogueLine(new VoiceDialogueLine("A warning, before you go any further..."));
            parser.printDialogueLine(new VoiceDialogueLine("She will lie, she will cheat, and she will do everything in her power to stop you from slaying her. Don't believe a word she says."));
            
            switch (this.activeChapter) {
                case ADVERSARY:
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "\"Lying\" and \"cheating\" doesn't sound like her at all. Not that it matters, it's not like she can lie or cheat in the middle of a fight."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Are you sure about that?"));
                    parser.printDialogueLine(new VoiceDialogueLine("The point of my warning wasn't to start an argument over what circumstances the Princess is capable of lying in. It was to give you some broadly applicable advice."));
                    parser.printDialogueLine(new VoiceDialogueLine("The Princess will do and say whatever she thinks it will take to get her out of there. So don't trust her. Ever. Are we clear?"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "Crystal. Let's just get on with it already."));
                    break;

                case BEAST:
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "Does a cat lie to a cornered mouse when it plays with its freedom, or is it just acting out its nature?"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I don't see why that matters. A lie is a lie, and if anything, she's the one who's cornered."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "She could have gotten out of there whenever she wanted to. We should trust nothing that she tells us, only what we hear and smell."));
                    parser.printDialogueLine(new VoiceDialogueLine("That's a very roundabout way of saying that you should listen to me and take this seriously."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "Maybe."));
                    break;

                case DAMSEL:
                    if (this.sharedLoop) {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "We already told you we're not playing along with your little game, it's your lies that can't be trusted. Her beauty is the only thing in the world we *can* believe in!"));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I think we've already been over this. I'm pretty sure he just likes the sound of his own voice."));
                    } else {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "If only you knew what you did to us, you villain."));
                        parser.printDialogueLine(new VoiceDialogueLine("Excuse me?"));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Forget he said anything."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "But He *is* a villain. He made our beloved brutally take our life last time. He's trying to keep us apart, but He won't be able to withstand the power of our love!"));
                        parser.printDialogueLine(new VoiceDialogueLine("Last time? What are you talking about?"));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I think he just likes to hear the sound of his own voice. Let's try to ignore him."));
                    }
                    
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "I do, but I also speak from the heart. My passions are too great to be stifled, they must be expressed!"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Sure, yeah, your passions are strong and all, but not everyone needs to hear them. Some things are better kept quiet."));
                    parser.printDialogueLine(new VoiceDialogueLine("Don't pay their bickering any mind. Focus on the task ahead."));
                    break;

                case NIGHTMARE:
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I don't think \"lying\" and \"cheating\" is her thing. She was *very* direct with us last time. Or, at least she was direct with us after we decided to lock her away."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "It doesn't matter. Don't. Trust. Anyone."));
                    break;

                case PRISONER:
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "Yes, yes, don't believe a word she says. Just go in, take the knife, and do what you're supposed to. Wink."));
                    parser.printDialogueLine(new VoiceDialogueLine("Did you just say \"wink\" out loud?"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "No, I didn't. Wink."));
                    parser.printDialogueLine(new VoiceDialogueLine("Just ignore this clown and focus on the Princess."));
                    break;

                case RAZOR:
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "He couldn't be more on the money. But we're really doing this, aren't we? I'd say your loss, but I'm stuck here with you."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "We know what to look out for this time. We know to be careful."));
                    parser.printDialogueLine(new VoiceDialogueLine("Just stay focused and you'll be fine."));
                    break;

                case SPECTRE:
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "She won't be a problem."));
                    break;

                case TOWER:
                    if (pessimismComment) {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "Lying? Cheating? Why would she even bother? She didn't need to do anything like that last time."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "She caught us off-guard last time. We'll be fine. Let's just keep our wits about us."));
                        parser.printDialogueLine(new VoiceDialogueLine("At least one of you has a shred of sense. Just make sure you listen to *him* and not that... whiner."));
                    } else {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "We might as well just pledge ourselves to her and stop pretending that we're capable of doing anything in this situation. She probably doesn't even need to try to overpower us."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Can we tone down the pessimism just a smidge?"));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "I'm not being a pessimist. I'm just being *realistic.*"));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "You're being annoying."));
                        parser.printDialogueLine(new VoiceDialogueLine("Just ignore their bickering, and whatever you do, don't \"pledge yourself to her.\" I cannot stress enough how absolutely *catastrophic* that would be for everyone. Yourself included."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I agree. If she's wrongfully imprisoned then we should rescue her, but if He's telling the truth, we shouldn't just hand her the world on a silver platter."));
                        parser.printDialogueLine(new VoiceDialogueLine("\"Rescue her?\" Given the stakes of the situation, there isn't really a difference between \"rescuing her\" and \"pledging yourself to her.\" Either would be terrible."));
                        parser.printDialogueLine(new VoiceDialogueLine("So please, try to ignore *both* of those knuckleheads and focus on saving the world. Let's not make this harder than it has to be."));
                    }
                    
                    break;

                case WITCH:
                    if (this.sharedLoopInsist) {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "Don't worry. I think we've taken that lesson to heart at this point. You can trust us to get the job done."));
                    } else {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "Don't worry. You can trust us to get the job done."));
                    }

                    break;
            }
        }

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "proceed", "[Proceed into the cabin.]"));

        this.repeatActiveMenu = true;
        while (this.repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);

            switch (this.activeOutcome) {
                case "cGoCabin":
                case "proceed":
                    this.repeatActiveMenu = false;
                    break;

                case "cGoLeave":
                    if (!this.canTryAbort) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }

                    switch (this.ch2AttemptAbortVessel()) {
                        case 0: return false;

                        case 1:
                            this.canTryAbort = false;
                            break;

                        case 3:
                            this.skipHillDialogue = true;
                        case 2:
                            this.repeatActiveMenu = false;
                            break;
                    }
                    
                default:
                    this.giveDefaultFailResponse(this.activeOutcome);
            }
        }

        return true;
    }

    /**
     * The player shares with the Narrator that they've been here before, intentionally or not
     * @param butIDied whether the dialogue option that led to this was "But I died!..."; only matters if the current Chapter is The Prisoner
     */
    private void ch2IntroShareLoop(boolean butIDied) {
        this.sharedLoop = true;

        if (this.activeChapter == Chapter.PRISONER) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "Don't forget what He did to us the last time around. I wouldn't trust a word out of his mouth. There's got to be a way out of here, for us *and* for the Princess. We just have to keep trying."));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I'm inclined to agree. If He doesn't remember what happened last time, maybe it's best to keep it that way."));
            parser.printDialogueLine(new VoiceDialogueLine("You know I can hear you two, right? It's going to be a lot harder than you think to keep secrets from me."));
            if (butIDied) parser.printDialogueLine(new VoiceDialogueLine("And as far as trying to *help* her goes, need I remind you how catastrophically dangerous she is to the world at large? I told you about the stakes of this situation less than a minute ago."));
        } else {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "If He doesn't remember what happened, then maybe it's best to keep it that way."));

            switch (this.ch2Voice) {
                case BROKEN:
                    parser.printDialogueLine(new VoiceDialogueLine("You know I can hear you, right? It's going to be a lot harder than you think to keep secrets from me."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "What does it matter what He knows? There's nothing we can do to stop her. She's just going to kill us again."));
                    parser.printDialogueLine(new VoiceDialogueLine("She is *not* going to kill you unless you let her. But slaying the Princess and saving the world is going to be much more difficult than it has to be if you spend the whole time second guessing yourself."));
                    break;

                case CHEATED:
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "This whole thing's a crock of shit. She's just going to pull a knife out of nowhere and stab us again."));
                    parser.printDialogueLine(new VoiceDialogueLine("Stabbed to death? Well, you won't have to worry about that. The Princess is unarmed."));

                    if (source.equals("revival")) {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Yeah, that's exactly what you told us last time. You said this whole thing would be easy, but after we sank our blade into her heart she just got up and started stabbing us."));
                    } else {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Yeah, that's exactly what you told us last time. When we asked you if you were sure she didn't have a weapon on her, you said you were \"positive\" she didn't."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "But it turns out she did. Because when we charged her, she started stabbing us. To *death!*"));
                    }
                    
                    parser.printDialogueLine(new VoiceDialogueLine("Calm down. I assure you she has no weapons, so there's no reason to fear her. You were made for this job. You'll do just fine."));
                    break;
                    
                case COLD:
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "That's fine. It wasn't very hard to kill her last time. We'll just do it again."));
                    parser.printDialogueLine(new VoiceDialogueLine("Well, if for whatever reason you're going to insist that this has happened before, at least your heart's in the right place."));
                    break;
                    
                case CONTRARIAN:
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "I don't know. I think it's more fun if He knows what we're thinking. He's like a captive audience."));

                    if (source.equals("askedWalls")) {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "He might have walled off everything but the path to the cabin, but I'm sure there's plenty of other ways we can ruin his day."));
                    } else {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "The entire world ending wasn't enough to get rid of us. I don't think there's much He can do other than object. I wonder what else we can do to ruin his day."));
                    }
                    parser.printDialogueLine(new VoiceDialogueLine("If by ruining my day you mean ruining everyone's day forever, then yes, I suppose there are plenty of ways you can pull that off."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "The world really did end last time, didn't it? We should be careful. For all we know we just got lucky."));
                    parser.printDialogueLine(new VoiceDialogueLine("The world hasn't ended yet, and you are *never* going to slay her with that attitude. Stuff those pathetic little voices to the back of your mind and stay focused on the task ahead."));
                    break;
                    
                case HUNTED:
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "If He doesn't remember what happened, then something else must have trapped us here."));
                    parser.printDialogueLine(new VoiceDialogueLine("You're not *trapped* here. Nobody's forcing you to do anything, though the only sensible thing for you to do right now is march up to that cabin and slay the Princess."));
                    break;
                    
                case OPPORTUNIST:
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "Brilliant. We need to keep our cards close to our chest, and I'm not sure we can trust *Him.*"));
                    parser.printDialogueLine(new VoiceDialogueLine("You know I can hear you, right? It's going to be a lot harder than you think to keep secrets from me."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "Did I say \"I'm not sure we can trust *Him?\"* Slip of the tongue. Bit of the old brain fog. I meant to say that we should probably head over to the cabin and slay that Princess. We already know we can't trust *her,* so let's get on with the show."));
                    break;
                    
                case PARANOID:
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "Shhh. What if He hears us?"));
                    parser.printDialogueLine(new VoiceDialogueLine("That's a very good question, little voice. What if He *does* hear you?"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "Shit."));
                    parser.printDialogueLine(new VoiceDialogueLine("I think you'll find yourselves very hard pressed to keep any secrets from me. Not that it matters right now, because like I said, this is the first time we've met. Still, I'd rather not get off on the wrong foot. We've a world to save, after all."));
                    break;
                    
                case SMITTEN:
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "Yes, He didn't approve of us last time, did He? If we're going to save our beloved, we'll have to be sneaky about it."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Our... \"beloved?\""));
                    parser.printDialogueLine(new VoiceDialogueLine("Yes, you'll have to be *very* sneaky about your intentions if you're going to try and save the Princess."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "Ah, so all of the cards are on the table. Then you should know that we and the Princess are in love and the four of us will be foiling any and all assassination attempts you've got in the works."));
                    parser.printDialogueLine(new VoiceDialogueLine("We'll see about that. Whatever you do, just be sure to ignore *him,* specifically. It sounds like he's the sort who'd sacrifice the whole world for a peck on the cheek."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "What can I say? A world without love is a world that isn't worth saving."));
                    break;
                    
                case STUBBORN:
                    parser.printDialogueLine(new VoiceDialogueLine("You know I can hear you, right? It's going to be a lot harder than you think to keep secrets from me."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "That's fine. It doesn't matter if He can hear us. The only thing that matters is marching up to that cabin and *winning.*"));
                    parser.printDialogueLine(new VoiceDialogueLine("That's the spirit. There's no point in squabbling when the real threat is just up that hill."));
                    break;
            }
        }

    }

    /**
     * The Narrator indulges the player in a "thought experiment" where they have, in fact, been here before
     * @param youDied whether the player died in Chapter I
     * @param princessDied whether the Princess died in Chapter I
     * @param shareDied whether the player (or the Voices) mentioned that they died in Chapter I
     */
    private void ch2IntroAssumeTruth(boolean youDied, boolean princessDied, boolean shareDied) {
        this.sharedLoopInsist = true;

        parser.printDialogueLine(new VoiceDialogueLine("Those are two *very* different questions, but fine. I'll indulge you if that's what it takes to get you moving."));
        parser.printDialogueLine(new VoiceDialogueLine("Let's say for a moment that this really is the second time you've met me, or, at least, a version of me."));

        if (shareDied) {
            parser.printDialogueLine(new VoiceDialogueLine("You died last time, which probably only happened because you didn't listen to me."));
        } else {
            parser.printDialogueLine(new VoiceDialogueLine("If you're back here, I'm assuming you died, which probably only happened because you didn't listen to me."));
        }

        switch (this.activeChapter) {
            case ADVERSARY:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "We did our best with the information we were given. And we *did* kill her."));
                parser.printDialogueLine(new VoiceDialogueLine("And yet you still died, didn't you? So congratulations. You've been given another chance to actually do this right."));
                break;
                
            case BEAST:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "We did our best with the information we were given."));
                parser.printDialogueLine(new VoiceDialogueLine("And yet you still died, didn't you? So, great. Congratulations. You've been given another chance to actually do this right."));
                break;
                
            case DAMSEL:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "*You* were the one who did us in, villain."));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Well, not you in the literal sense, but you did everything you could to stop us from rescuing her."));
                parser.printDialogueLine(new VoiceDialogueLine("Oh, I wonder why. Maybe it's because the entire world was at stake. No lone Princess is worth that price."));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "I beg to differ."));
                parser.printDialogueLine(new VoiceDialogueLine("I'm not going to argue with you. I'm just going to take a deep breath and assume that whoever is making the decisions here has the common sense to *ignore* your protestations."));
                break;
                
            case NIGHTMARE:
                if (source.equals("fled")) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "Oh, we listened to you, all right. Worst decision of our incredibly short life."));
                    if (this.isHarsh) {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "We tried to slay her, we really did, but she was going to kill us. It was either lock her in the basement or let her finish beating us to death."));
                    } else {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "We tried to slay her, we really did, but she was going to kill us. It was either lock her in the basement or let her finish tearing us to ribbons."));
                    }
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "We couldn't trust *either* of you. And as far as I'm concerned, we still can't."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "All we did was lock her away."));
                    parser.printDialogueLine(new VoiceDialogueLine("And how'd that work out for you?"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "No comment."));
                }

                parser.printDialogueLine(new VoiceDialogueLine("Well then, congratulations. You've been given another chance to actually do this right."));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "And your solution to this is to send us back in there? Do you want us to slay the Princess, or do you want the Princess to slay *us?*"));
                parser.printDialogueLine(new VoiceDialogueLine("Obviously I want *you* to slay *her.* One of you poses a threat to the world, and the other doesn't."));
                break;
                
            case PRISONER:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "The absolute irony. But that's one way to put it, I guess."));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "You *really* don't remember what happened last time, do you? You practically forced the Princess to kill us."));
                parser.printDialogueLine(new VoiceDialogueLine("That doesn't sound like the sort of thing I'd do, which is honestly all the more reason for you to not buy into whatever self-delusions the three of you are crafting."));
                parser.printDialogueLine(new VoiceDialogueLine("*Sigh.* But this is a thought experiment, so I suppose I'll continue to give you the benefit of the doubt. If I did 'practically force the Princess to kill you', it was probably for a good reason. Did you try and free her? Did you *say something really mean to me?* Because if I really did what you said I did, you probably deserved it. I'm a professional, after all."));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "Sure you are."));
                break;

            case RAZOR:
                if (source.equals("revival")) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "We mostly listened to you. How were we supposed to know she'd spring back to life?"));
                    parser.printDialogueLine(new VoiceDialogueLine("If she \"sprung\" back to life in this hypothetical scenario, then clearly you *didn't* slay her."));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "We did exactly what you said..."));
                    parser.printDialogueLine(new VoiceDialogueLine("Sounds to me like you probably had some kind of elaborate nightmare, in which case I shouldn't be held accountable for what supposedly happened."));
                }

                parser.printDialogueLine(new VoiceDialogueLine("But congratulations. You've been given a chance to actually do this right."));
                break;
                
            case SPECTRE:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "Oh, we listened to you plenty. We slew the Princess, just like you asked us to. And then you locked us away in an empty void for eternity. So we slew ourselves, too."));
                parser.printDialogueLine(new VoiceDialogueLine("Well, if you killed yourself then you *weren't* listening to me. Because I would never want you to do that. Believe it or not, I care about you."));
                break;

            case TOWER:
                if (source.equals("unharmed")) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "Of course we died. We couldn't land a *single* blow on her and she broke every bone in our body before she decided to let us die."));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "Of course we died. She didn't feel pain. She didn't feel much of anything, did she? And she broke every bone in our body before she decided to let us die."));
                }

                parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "What were we supposed to do to stop her then? What are we supposed to do to stop her now? It's pointless."));
                parser.printDialogueLine(new VoiceDialogueLine("She's *just* a Princess. Slaying her shouldn't have been difficult, but congratulations. You've been given another chance to actually do this right."));
                break;
                
            case WITCH:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "We were just weighing our options in a morally ambiguous situation. You can't blame us for weighing our options."));

                if (princessDied) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "We did our best with the information we were given. And we *did* kill her."));
                    parser.printDialogueLine(new VoiceDialogueLine("And yet you still died, didn't you? So congratulations. You've been given another chance to actually do this right."));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine("I can if you failed to slay the Princess, which you apparently did. So, great. Congratulations. You've been given another chance to actually do this right."));
                }

                break;
        }

        if (this.activeChapter == Chapter.NIGHTMARE || this.activeChapter == Chapter.PRISONER || this.activeChapter == Chapter.DAMSEL) {
            parser.printDialogueLine(new VoiceDialogueLine("Anyways, I believe your other question was something along the lines of \"what's the point of doing anything?\" If you're asking that, it sounds to me like you're making the rather dangerous assumption that your actions last time around didn't have any consequences."));
        } else {
            parser.printDialogueLine(new VoiceDialogueLine("And I believe your other question was something along the lines of \"what's the point of doing anything?\" If you're asking that, it sounds to me like you're making the rather dangerous assumption that your actions last time around didn't have any consequences."));
        }
        
        String consequenceFree;
        if (youDied && princessDied) {
            if (this.activeChapter == Chapter.RAZOR) {
                consequenceFree = "What do you mean? Of course there weren't any consequences. We stabbed the Princess, the Princess stabbed us, and now everyone's right back where they started. That sounds pretty consequence-free to me.";
            } else {
                consequenceFree = "What do you mean? Of course there weren't any consequences. We killed the Princess, the Princess killed us, and now everyone's right back where they started. That sounds pretty consequence-free to me.";
            }
        } else if (youDied) {
            consequenceFree = "What do you mean? Of course there weren't any consequences. We were killed by the Princess, and now everyone's right back where they started. That sounds pretty consequence-free to me.";
        } else if (princessDied) { // Only possible in Spectre
            consequenceFree = "What do you mean? Of course there weren't any consequences. We slew the Princess, the world outside the cabin disappeared, we died, and now everyone's right back where they started. That sounds pretty consequence-free to me.";
        } else { // Only possible in Witch if you were locked in the basement
            consequenceFree = "What do you mean? Of course there weren't any consequences. The Princess locked us in the basement, we eventually died, and now everyone's right back where they started. That sounds pretty consequence-free to me.";
        }

        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, consequenceFree));

        if (this.activeChapter == Chapter.NIGHTMARE) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "Speak for yourself. From my perspective there were plenty of consequences. I'm never going forget the way she just made us *stop working.*"));
            parser.printDialogueLine(new VoiceDialogueLine("And that's only scratching the surface. If what you said is true, it begs the question of *how* you got back here. Did \"time\" simply rewind itself, or have you found yourself in another world altogether? If it's the latter, what do you think happened *after* you died?"));
            parser.printDialogueLine(new VoiceDialogueLine("Do you think the people there lived happily ever after, or do you think that the Princess, left unhindered, brought about the end to everyone and everything, just like I told you she would?"));
        } else {
            parser.printDialogueLine(new VoiceDialogueLine("Yes, but, in this purely hypothetical scenario, that begs the question of *how* you got back here. Did \"time\" simply rewind itself, or have you found yourself in another world altogether?"));

            if (princessDied) {
                parser.printDialogueLine(new VoiceDialogueLine("Had you failed to slay the Princess, what would have happened to everyone in the place you left?"));
            } else {
                parser.printDialogueLine(new VoiceDialogueLine("If it's the latter, what do you think happened *after* you died?"));
                parser.printDialogueLine(new VoiceDialogueLine("Do you think the people there lived happily ever after, or do you think that the Princess, left unhindered, brought about the end to everyone and everything, just like I told you she would?"));
            }
        }

        switch (this.activeChapter) {
            case ADVERSARY:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "Ugh. Enough with the talking! We've got a fight to win. Nothing. Else. Matters."));
                parser.printDialogueLine(new VoiceDialogueLine("I couldn't agree more. The cabin, and your destined confrontation with the Princess, awaits."));
                break;

            case BEAST:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "All the more reason to keep our wits about us. Should we even return to the cabin? We could find a safe little hole somewhere instead..."));
                parser.printDialogueLine(new VoiceDialogueLine("You have to go to the cabin. If you don't slay the Princess she's going to end the world, which you're always going to be a part of, even if you're cowering in a hole. There's no escaping your responsibility here."));
                break;

            case DAMSEL:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "She would never. She's a perfect angel that you cruelly imprisoned as part of some convoluted, dastardly scheme."));
                parser.printDialogueLine(new VoiceDialogueLine("Convoluted? I don't know how this premise could be any more simple. Princess bad. Stop her. Save everyone."));
                break;
                
            case NIGHTMARE:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "If she brought an end to everything and everyone, how are *we* supposed to stop her? What do you want from us?"));
                parser.printDialogueLine(new VoiceDialogueLine("I want you to succeed. You'll find a way. You're the only one who can."));
                break;

            case PRISONER:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "What a conveniently ambiguous group of things for her to ruin. For all we know, the Princess left the cabin and never saw another soul."));
                parser.printDialogueLine(new VoiceDialogueLine("Oh how I wish that were the case, but if the Princess weren't a certain, inevitable threat to the world, the four of us wouldn't be here. And yet, here we are."));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "You're talking in circles."));
                parser.printDialogueLine(new VoiceDialogueLine("No, I'm talking in *facts.*"));
                break;

            case RAZOR:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Screw this. Who cares what happened to everyone else? She's not going to play fair, so we should do what we can to save ourselves and just *get out of here.*"));
                parser.printDialogueLine(new VoiceDialogueLine("At least you know not to trust her, but you do realize that \"everything and everyone\" includes you, right? If you turn around and leave, you're dooming yourself as well as everyone else."));

                switch (this.source) {
                    case "revival":
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "We were so close to finishing the job last time. She can't get the jump on us twice. If we're careful, we should be fine."));
                        break;

                    default:
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "She just caught us by surprise last time. She can't do that twice. So long as we're careful, we can win this."));
                        break;
                }
                
                parser.printDialogueLine(new VoiceDialogueLine("That's the spirit. Just keep that stiff upper lip and you'll save the world in no time at all."));
                break;

            case SPECTRE:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "It doesn't matter, because we didn't fail to slay her, and if she's really back, which I doubt, it'll be just as easy to do it again. But after that nasty trick you pulled on us, maybe she's not the only one around here in need of slaying."));
                parser.printDialogueLine(new VoiceDialogueLine("Just stay focused, will you?"));
                break;
                
            case TOWER:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "If she ended the entire world, why should we even bother? We might as well just walk up to that cabin, break her chains, and let her do whatever she wants. It's all the same in the end."));
                parser.printDialogueLine(new VoiceDialogueLine("Just because she's capable of ending the world doesn't mean that you're not capable of slaying her. Both of those things can be true at the same time. So chin up, I believe in you."));
                break;

            case WITCH:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "That's a very good point. This Princess character seems like a lot of trouble. And if you think about it, actually slaying her probably breaks us out of this cycle, right? We don't want to be stuck here forever, do we?"));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "You're laying it on a little thick, aren't you?"));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "Laying it on a little thick? What are you talking about? I'm sharing my honest opinions."));
                parser.printDialogueLine(new VoiceDialogueLine("What matters is that almost everyone seems to be on the same page. So whenever you're ready, you can stop dawdling, get to the cabin, and save the world."));
                break;
        }
    }

    /**
     * The player asks the Narrator questions about the Princess
     * @param youDied whether the player died in Chapter I
     * @param princessDied whether the Princess died in Chapter I
     * @return 0 if the player returns to the dialogue menu normally while pessimismComment is false; 1 if the player returns to the dialogue menu normally while pessimismComment is true; 2 if the player proceeds to the cabin via a command; 3 if the player attempts to leave via a command
     */
    private int ch2IntroAskPrincess(boolean youDied, boolean princessDied) {
        parser.printDialogueLine(new VoiceDialogueLine("Just be quick about it."));

        String tipsText = "";
        String howDangerText = "";
        String quoteText = "";
        switch (this.activeChapter) {
            case ADVERSARY:
                tipsText = "(Explore) We killed each other last time around. How can I make sure that doesn't happen again?";
                howDangerText = "(Explore) All she did last time around was beat me to death. How can someone like that end the world?";
                quoteText = "(Explore) To quote you from last time around, \"she's *just* a Princess.\" Why was she strong enough to beat me to death with her bare hands?";
                break;

            case BEAST:
                tipsText = "(Explore) She killed me last time around. How can I make sure that doesn't happen again?";
                howDangerText = "(Explore) She killed me by ripping me to pieces. Don't get me wrong, I hated it, but how can someone like that end the world?";
                quoteText = "(Explore) To quote you from last time around, \"she's *just* a Princess.\" Why was she able to rip me apart with her bare hands?";
                break;

            case DAMSEL:
                howDangerText = "(Explore) The only reason she was even able to kill me last time was because I let her. She could barely hold a knife. How is she supposed to end the world?";
                break;

            case NIGHTMARE:
                tipsText = "(Explore) Just being around her in the end shut down all of my organs. What the hell am I supposed to do about that?";
                quoteText = "(Explore) To quote you from last time around, \"she's *just* a Princess.\" How can you possibly justify saying that? She's clearly something far, far worse.";
                break;

            case PRISONER:
                howDangerText = "(Explore) The only reason she was even able to kill me last time was because I let her. And all she did was slit my throat. How is she supposed to end the world?";
                break;

            case RAZOR:
                if (source.equals("pathetic")) {
                    tipsText = "(Explore) She killed me last time around. How can I make sure that doesn't happen again?";
                } else {
                    tipsText = "(Explore) We killed each other last time around. How can I make sure that doesn't happen again?";
                }

                howDangerText = "(Explore) All she did last time around was stab me to death. How can someone like that end the world?";
                break;

            case SPECTRE:
                howDangerText = "(Explore) Last time around I stabbed her in the heart and she died. How can someone like that end the world?";
                break;

            case TOWER:
                tipsText = "(Explore) She killed me last time around. How can I make sure that doesn't happen again?";
                howDangerText = "(Explore) All she did last time around was beat me to death. How can someone like that end the world?";
                quoteText = "(Explore) To quote you from last time around, \"she's *just* a Princess.\" Why was she strong enough to beat me to death with her bare hands?";
                break;

            case WITCH:
                if (source.equals("locked")) {
                    howDangerText = "(Explore) All she did last time was lock me in a basement until I died. Don't get me wrong, I hated it, but how can someone like that end the world?";
                } else if (source.equals("mutual")) {
                    tipsText = "(Explore) We killed each other last time around. How can I make sure that doesn't happen again?";
                    howDangerText = "(Explore) She killed me by ripping me to pieces. Don't get me wrong, I hated it, but how can someone like that end the world?";
                    quoteText = "(Explore) To quote you from last time around, \"she's *just* a Princess.\" Why was she able to rip me apart with her bare hands?";
                } else {
                    howDangerText = "(Explore) She killed me by ripping me to pieces. Don't get me wrong, I hated it, but how can someone like that end the world?";
                    tipsText = "(Explore) She killed me last time around. How can I make sure that doesn't happen again?";
                    quoteText = "(Explore) To quote you from last time around, \"she's *just* a Princess.\" Why was she able to rip me apart with her bare hands?";
                }

                break;
        }

        int askCount = 0;
        boolean pessimismComment = false; // Used in Tower
        boolean pleadLeave = false; // Used in Nightmare
        OptionsMenu askMenu = new OptionsMenu();
        askMenu.add(new Option(this.manager, "teleport", "(Explore) If anything, the world ended *after* I slew her. When I tried to leave, everything was gone.", this.activeChapter == Chapter.SPECTRE));
        askMenu.add(new Option(this.manager, "tips", tipsText, !tipsText.equals("")));
        askMenu.add(new Option(this.manager, "howDanger", howDangerText, !howDangerText.equals("")));
        askMenu.add(new Option(this.manager, "quote", quoteText, !quoteText.equals("")));
        askMenu.add(new Option(this.manager, "basement", "(Explore) Who locked her in that basement? What *is* this place?"));
        askMenu.add(new Option(this.manager, "whyMe", "(Explore) If people locked her away, why couldn't *they* slay her? Why is this falling on me?", askMenu.get("basement")));
        askMenu.add(new Option(this.manager, "cagey", "(Explore) You're being cagey. What aren't you telling me?", false));
        askMenu.add(new Option(this.manager, "return", "Nevermind."));

        String outcome;
        boolean repeatMenu = true;
        while (repeatMenu) {
            outcome = parser.promptOptionsMenu(askMenu);
            switch (outcome) {
                case "teleport":
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "That's a good point. How do we know we didn't have things backwards? Maybe slaying the Princess was what ended the world, not the other way around."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "Yes, maybe this whole thing was a trick to get us to end the world. And now we get to go through the whole charade again wholly aware of what's waiting for us at the end."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "But that's assuming she's alive in that cabin. We did kill her, after all."));
                    parser.printDialogueLine(new VoiceDialogueLine("You're going to find her in the cabin. If the Princess had actually been slain, you wouldn't be here."));
                    parser.printDialogueLine(new VoiceDialogueLine("And let me assure you, killing her will not end the world. I don't know what you think happened to you \"last time,\" but it's a load of nonsense. You'll get your happy ending, I promise."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "That's exactly what we're afraid of."));
                    parser.printDialogueLine(new VoiceDialogueLine("Really? Living happily ever after sounds *that bad* to you? Oh well, there's no use arguing over your masochism. The cabin awaits."));
                    break;

                case "tips":
                    askCount += 1;
                    parser.printDialogueLine(new VoiceDialogueLine("Like I said, if she killed you, it was probably because you didn't listen to me. Don't talk to her. Don't trust her. Just go in, do your job, and save the world."));

                    if (this.activeChapter == Chapter.ADVERSARY && askCount == 2) {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "Oh this is *maddening.* Why do you keep asking questions?"));
                        parser.printDialogueLine(new VoiceDialogueLine("There's nothing wrong with getting the full picture of what's going on here."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "Sure there is. It's wasting time and energy that would be better spent fighting."));
                    }

                    break;

                case "howDanger":
                    askCount += 1;
                    askMenu.setCondition("quote", false);

                    if (this.activeChapter == Chapter.RAZOR) {
                        parser.printDialogueLine(new VoiceDialogueLine("She just can. But she's still only a Princess. You're fully up to the task you've been given, so long as you remember that."));
                    } else if (source.equals("locked")) {
                        parser.printDialogueLine(new VoiceDialogueLine("She just can. But that doesn't mean you're not fully up to the task that's been given to you. Have a little faith in yourself, and maybe try to not get tricked this time."));
                    } else {
                        parser.printDialogueLine(new VoiceDialogueLine("She just can. Believe me, I wish I could tell you more, but you'll just have to trust that what I'm saying is true and that, despite it all, you're fully up to the task that's been given to you."));
                    }

                    if (this.activeChapter == Chapter.ADVERSARY && askCount == 2) {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "Oh this is *maddening.* Why do you keep asking questions?"));
                        parser.printDialogueLine(new VoiceDialogueLine("There's nothing wrong with getting the full picture of what's going on here."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "Sure there is. It's wasting time and energy that would be better spent fighting."));
                    } else if (this.activeChapter == Chapter.DAMSEL) {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "Maybe it's her *beauty* that threatens the world."));
                        parser.printDialogueLine(new VoiceDialogueLine("Sure. It's her beauty. Why not? And before you ask, no. We can't just keep her down there. If you don't slay her, she's going to find a way out. It's unfortunate, I know, but it's just the way it is."));
                    } else if (this.activeChapter == Chapter.TOWER && !pessimismComment) {
                        pessimismComment = true;
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "We're not. We might as well just pledge ourselves to her and stop pretending that we're capable of doing anything in this situation. She probably doesn't even need to try to overpower us."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Can we tone down the pessimism just a smidge?"));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "I'm not being a pessimist. I'm just being *realistic.*"));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "You're being annoying."));
                        parser.printDialogueLine(new VoiceDialogueLine("Just ignore their bickering, and whatever you do, don't \"pledge yourself to her.\" I cannot stress enough how absolutely *catastrophic* that would be for everyone. Yourself included."));
                    } else if (this.activeChapter == Chapter.PRISONER) {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "She just can. Believe me, I wish I could tell you more, but you'll just have to trust that what I'm saying is true and that, despite it all, you're fully up to the task that's been given to you."));
                        parser.printDialogueLine(new VoiceDialogueLine("What proof could you possibly ask for?"));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Literally anything."));
                        parser.printDialogueLine(new VoiceDialogueLine("*Sigh.* Fine. Check your pockets."));

                        OptionsMenu pocketsMenu = new OptionsMenu(true);
                        pocketsMenu.add(new Option(this.manager, "check", "[Check your pockets.]"));
                        pocketsMenu.add(new Option(this.manager, "leave", "[Leave your pockets unchecked.]"));

                        switch (parser.promptOptionsMenu(pocketsMenu, new VoiceDialogueLine("Well? After all that, are you going to check your pockets or not?", true))) {
                            case "check":
                                parser.printDialogueLine(new VoiceDialogueLine("You put your hands in your pockets and pull out an envelope with the words \"THE EVIDENCE\" written across the front."));
                                parser.printDialogueLine(new VoiceDialogueLine("Within, you find a note in your handwriting. It reads: \"The Princess will end the world if you don't stop her. This is an immutable truth.\""));
                                parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "That doesn't prove anything! How do we know you didn't just forge our handwriting?"));
                                parser.printDialogueLine(new VoiceDialogueLine("I wish I could tell you more, but there are some rules I have to follow for all of our sakes. Please just trust that these rules are in place for a reason. I'm on your side."));
                                parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "You mean you're on our side as long as we do what you tell us to."));
                                parser.printDialogueLine(new VoiceDialogueLine("Exactly. Because you *not* doing what I tell you to do means you're putting the world at risk."));
                                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I think we've got everything out of him that we're going to get."));
                                break;

                            case "leave":
                                parser.printDialogueLine(new VoiceDialogueLine("You decide to leave your pockets unchecked. See? You two are the only ones here who care about this little aside."));
                                break;
                        }
                    }

                    break;

                case "quote":
                    askCount += 1;
                    askMenu.setCondition("howDanger", false);
                    
                    parser.printDialogueLine(new VoiceDialogueLine("She *is* just a Princess. Whatever you think happened to you last time, just get it out of your head before you get to the cabin, and you'll be *fine.*"));

                    if (this.activeChapter == Chapter.ADVERSARY && askCount == 2) {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "Oh this is *maddening.* Why do you keep asking questions?"));
                        parser.printDialogueLine(new VoiceDialogueLine("There's nothing wrong with getting the full picture of what's going on here."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "Sure there is. It's wasting time and energy that would be better spent fighting."));
                    } else if (this.activeChapter == Chapter.TOWER && !pessimismComment) {
                        pessimismComment = true;
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "Or we could pledge ourselves to her and stop pretending that we're capable of doing anything in this situation. She probably doesn't even need to try to overpower us."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Can we tone down the pessimism just a smidge?"));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "I'm not being a pessimist. I'm just being *realistic.*"));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "You're being annoying."));
                        parser.printDialogueLine(new VoiceDialogueLine("Just ignore their bickering, and whatever you do, don't \"pledge yourself to her.\" I cannot stress enough how absolutely *catastrophic* that would be for everyone. Yourself included."));
                    }

                    break;

                case "basement":
                    askCount += 1;
                    parser.printDialogueLine(new VoiceDialogueLine("*People* locked her in that basement. And I told you what this place is. It's a path in the woods. Don't overcomplicate things."));

                    if (this.activeChapter == Chapter.ADVERSARY && askCount == 2) {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "Oh this is *maddening.* Why do you keep asking questions?"));
                        parser.printDialogueLine(new VoiceDialogueLine("There's nothing wrong with getting the full picture of what's going on here."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "Sure there is. It's wasting time and energy that would be better spent fighting."));
                    }

                    break;

                case "whyMe":
                    askCount += 1;

                    if (this.activeChapter == Chapter.ADVERSARY && askCount == 2) {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "Oh this is *maddening.* Why do you keep asking questions?"));
                        parser.printDialogueLine(new VoiceDialogueLine("There's nothing wrong with getting the full picture of what's going on here."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "Sure there is. It's wasting time and energy that would be better spent fighting."));
                    }

                    parser.printDialogueLine(new VoiceDialogueLine("Look, I'm not supposed to say this, but it's because you're special. You're the *only* person capable of doing this. Call it a prophecy if that helps, but it's just the way things are."));

                    if (this.activeChapter == Chapter.NIGHTMARE) {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "You can't just goad us into doing something by calling us special. It's manipulative. Why are you trying to manipulate us?"));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I don't know, I kind of like being special."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "Okay, fine. Maybe you can goad *him* into doing something, but he's not even the one who makes the decisions here."));
                        parser.printDialogueLine(new VoiceDialogueLine("I'm not goading you into doing anything. You already know that the Princess is dangerous. All I'm trying to say is that you have to be the one to deal with her. I know it doesn't seem fair, but that's just the way it is."));
                        parser.printDialogueLine(new VoiceDialogueLine("And for what it's worth, I know you have it in you to finish the job."));

                        if (!pleadLeave) {
                            pleadLeave = true;
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "We don't. You saw what happened to us last time. We need to *leave.*"));
                        }
                    } else {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Oh. I didn't know we were *special.*"));

                        if (this.activeChapter == Chapter.ADVERSARY) {
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "Yeah, I like the sound of that."));
                        }

                        if (this.activeChapter == Chapter.SPECTRE) {
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "Of course we're special."));
                        } else {
                            parser.printDialogueLine(new VoiceDialogueLine("Of course you're special. Why else would you be here?"));

                            switch (this.activeChapter) {
                                case BEAST:
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "Special can mean all sorts of things. Don't let it make you careless. We need clear thoughts and pricked ears."));
                                    break;

                                case DAMSEL:
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "Calling us special isn't going to make us friends, even if it did feel nice."));
                                    parser.printDialogueLine(new VoiceDialogueLine("Oh, believe me, the last thing I want is for you and I to be *friends.* But I'm a professional, and I'm not going to let my dislike for you get in the way of helping you save the world."));
                                    break;

                                case PRISONER:
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "Ah, yes, right. We're here because we're *special.*"));
                                    parser.printDialogueLine(new VoiceDialogueLine("Look. You're annoyed that you're here. I get it. I'm also annoyed that I'm here. But we're all in this together, and we're dealing with a bit of a ticking clock right now, so please, just get to the cabin."));
                                    break;

                                case RAZOR:
                                    if (source.equals("revival")) {
                                        parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "If anyone's \"special\" here, it's her. Last I checked we can't get up from a knife in the chest."));
                                    } else {
                                        parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "If anyone's \"special\" here, it's her. That was a nasty trick she pulled on us."));
                                    }
                                    break;

                                case TOWER:
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "Who cares if *you* think we're special? As far as I can tell, the only thing special about us is that we get to experience painfully dying all over again."));
                                    break;

                                case WITCH:
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "Good point. That really explains almost everything."));
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I'm not so sure about *that.*"));
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "You know, you're right. But it explains *almost* everything."));
                                    break;
                            }
                        }
                    }

                    break;

                case "cagey":
                    parser.printDialogueLine(new VoiceDialogueLine("I've told you everything you need to know, going into more detail would just overcomplicate an otherwise very simple situation and make your job more difficult."));

                    switch (this.activeChapter) {
                        case ADVERSARY:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "What else would we even *need* to know? We've got all the reason we need for a rematch."));
                            parser.printDialogueLine(new VoiceDialogueLine("Exactly. The less you know about her, the better."));
                            break;

                        case BEAST:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "If you want us to stand a chance against her, we need to be armed with information. What is she really capable of? How are we supposed to stop her?"));

                            if (askCount < 2) {
                                parser.printDialogueLine(new VoiceDialogueLine("The less you know about her, the better."));
                            } else {
                                parser.printDialogueLine(new VoiceDialogueLine("Not to sound like a broken record, but the less you know about her, the better things will go for all of us. I know it sounds like I'm hiding something, but you're just going to have to take me at my word."));
                            }

                            parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "You're afraid, aren't you? Just like us."));
                            parser.printDialogueLine(new VoiceDialogueLine("Of course I'm afraid. Fear is an extremely normal thing to feel when the fate of the entire world is at stake."));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "But that's not the only thing you're afraid of. You're scared of something *worse.*"));
                            parser.printDialogueLine(new VoiceDialogueLine("Stop projecting your neuroses onto me and just get to the cabin already."));
                            break;

                        case DAMSEL:
                        case PRISONER:
                            if (askCount < 2) {
                                parser.printDialogueLine(new VoiceDialogueLine("The less you know about her, the better."));
                            } else {
                                parser.printDialogueLine(new VoiceDialogueLine("Not to sound like a broken record, but the less you know about her, the better things will go for all of us. I know it sounds like I'm hiding something, but you're just going to have to take me at my word."));
                            }

                            break;

                        case NIGHTMARE:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "If you want us to stand a chance against her, we need to be armed with information. What is she really capable of? How are we supposed to stop her?"));

                            if (askCount < 2) {
                                parser.printDialogueLine(new VoiceDialogueLine("The less you know about her, the better."));
                            } else {
                                parser.printDialogueLine(new VoiceDialogueLine("Not to sound like a broken record, but the less you know about her, the better things will go for all of us. I know it sounds like I'm hiding something, but you're just going to have to take me at my word."));
                            }
                            
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "He isn't telling us everything He knows because He doesn't trust us. Which means that *we* can't trust *Him.*"));
                            parser.printDialogueLine(new VoiceDialogueLine("Stop talking yourself in neurotic circles and just get to the cabin already."));

                            if (pleadLeave) {
                                parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "Do you see the way He keeps pushing us? We have to get out of here."));
                            } else {
                                parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "No, you should do anything but that. We *know* what's waiting for us in that basement."));
                            }

                            break;

                        case RAZOR:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "You didn't tell us about her knife last time though."));
                            parser.printDialogueLine(new VoiceDialogueLine("That's because she's unarmed, and more than that, it's because there *wasn't* a last time."));
                            break;

                        case SPECTRE:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "This is boring. He's clearly not interested in talking, so let's just do as He says and maybe He'll stop bothering us."));
                            break;

                        case TOWER:
                            if (pessimismComment) {
                                parser.printDialogueLine(new VoiceDialogueLine("The less you know about her, the better."));
                            } else {
                                pessimismComment = true;
                                parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "Even if He's hiding something, I doubt it would help us. I'm sure knowing what He knows would only make things worse."));
                                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Can we tone down the pessimism just a smidge?"));
                                parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "I'm not being a pessimist. I'm just being *realistic.*"));
                                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "You're being annoying."));
                                parser.printDialogueLine(new VoiceDialogueLine("Just ignore their bickering, and whatever you do, don't \"pledge yourself to her.\" I cannot stress enough how absolutely *catastrophic* that would be for everyone. Yourself included."));
                            }

                            break;

                        case WITCH:
                            if (!source.equals("locked")) {
                                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "If you want us to stand a chance against her, we need to be armed with information. What is she really capable of? How are we supposed to stop her?"));
                            }
                            
                            if (askCount < 2) {
                                parser.printDialogueLine(new VoiceDialogueLine("The less you know about her, the better."));
                            } else {
                                parser.printDialogueLine(new VoiceDialogueLine("Not to sound like a broken record, but the less you know about her, the better things will go for all of us. I know it sounds like I'm hiding something, but you're just going to have to take me at my word."));
                            }

                            parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "I don't think either of you really need to press the man on this. He wants us to slay the Princess, so why would He have anything to hide? He seems like a nice guy to me!"));
                            parser.printDialogueLine(new VoiceDialogueLine("I appreciate the vote of confidence, but you should really stop wasting time chatting amongst yourselves in the woods, so if we could move this along..."));
                            break;
                    }

                    break;

                case "return":
                    parser.printDialogueLine(new VoiceDialogueLine("Great. Now if you don't mind, the whole world is waiting with bated breath for you to save it from ruin."));
                    return (pessimismComment) ? 1 : 0;

                case "cGoCabin":
                    return 2;

                case "cGoLeave":
                    if (!this.canTryAbort) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                    
                    return 3;

                default:
                    this.giveDefaultFailResponse(outcome);
            }

            if (askCount == 1) {
                askMenu.setDisplay("return", "That's all.");
            } else if (askCount > 1) {
                askMenu.setCondition("cagey", true);
            }
        }

        throw new RuntimeException("No conclusion reached");
    }



    /**
     * The player attempts to abort the current Chapter II
     * @return 0 if the player commits to aborting the vessel; 1 if the player cannot attempt to abort the vessel; 2 if the player returns to the cabin at the first menu; 3 otherwise
     */
    private int ch2AttemptAbortVessel() {
        if (manager.nClaimedVessels() >= 2) {
            parser.printDialogueLine(CANTSTRAY);
            return 1;
        }

        switch (this.ch2Voice) {
            case BROKEN:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "You're right. We should just leave. There's nothing we can do to stop her, so we might as well enjoy what little time we have left."));
                break;

            case CHEATED:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Heh. And away we go. Good call."));
                break;
                
            case COLD:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "Oh? Do you think there's something else out there? All right, let's see what we can find. It's bound to be more interesting than doing the same thing over again."));
                break;
                
            case HUNTED:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "Yes. We're safer taking flight."));
                break;
                
            case OPPORTUNIST:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "Well, you're the boss."));
                break;
                
            case PARANOID:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "This is good. I was worried you might fall for his shit again, but this is good. Whatever answers there are to be found, they aren't *here,* and they definitely aren't *there.*"));
                break;
                
            case SKEPTIC:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "I'm not so sure running away is the best idea. We're not the only person stuck here. What about her?"));
                break;
                
            case SMITTEN:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "Are we running away? What are you doing, we have to save her!"));
                break;
                
            case STUBBORN:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "We can't just leave, we're supposed to fight her! Where are you going?"));
                break;
        }

        parser.printDialogueLine(new VoiceDialogueLine("Seriously? You're just going to turn around and leave? Do you even know where you're going?"));

        OptionsMenu leaveMenu = new OptionsMenu();
        leaveMenu.add(new Option(this.manager, "ugh", "Okay, fine. You're persistent. I'll go to the cabin and I'll slay the Princess. Ugh!"));
        leaveMenu.add(new Option(this.manager, "maybe", "Okay, fine. I'll go to the cabin and I'll talk to the Princess. Maybe I'll slay her. Maybe I won't. I guess we'll see."));
        leaveMenu.add(new Option(this.manager, "lie", "(Lie) Yes, I definitely know where I'm going."));
        leaveMenu.add(new Option(this.manager, "nope", "Nope!"));
        leaveMenu.add(new Option(this.manager, "notGoing", "The only thing that matters is where I'm not going. (The cabin. I am not going to the cabin.)"));
        leaveMenu.add(new Option(this.manager, "nihilist", "I'm actually pretty okay with the world ending. I relish the coming of a new dawn beyond our own. Gonna go walk in the opposite direction now!"));
        leaveMenu.add(new Option(this.manager, "quiet", "[Quietly continue down the path away from the cabin.]"));

        boolean repeatMenu = true;
        String outcome;
        while (repeatMenu) {
            outcome = parser.promptOptionsMenu(leaveMenu);
            switch (outcome) {
                case "cGoHill":
                case "ugh":
                    parser.printDialogueLine(new VoiceDialogueLine("*Thank you!* The whole world owes you a debt of gratitude. Really."));

                    switch (this.ch2Voice) {
                        case BROKEN:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "So much for getting out of here..."));
                            break;

                        // I think the original game devs forgot to include a line for Voice of the Cheated here...?
                            
                        case COLD:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "Oh well, cabin it is."));
                            break;
                            
                        case HUNTED:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "If this is what you think is best, I'll keep my ears pricked. Hopefully she won't catch us off-guard as easily as she did last time..."));
                            break;
                            
                        case OPPORTUNIST:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "This is probably for the best."));
                            break;
                            
                        case PARANOID:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "One little trick was all it took for you to go in there?"));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "*Sigh.*  I guess you're the one in control, aren't you? So if you want us to die again, I guess we'll die again. Good luck. To all of us."));
                            break;
                            
                        case SKEPTIC:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "Good. Going back to the cabin is the only way we can get to the bottom of things."));
                            break;
                            
                        case SMITTEN:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "Save. You'll go to the cabin and *save* the Princess."));
                            break;
                            
                        case STUBBORN:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "Oh, about *time.* I can't believe you were about to run away."));
                            break;
                    }

                    return 2;

                case "maybe":
                    parser.printDialogueLine(new VoiceDialogueLine("I guess we will."));

                    switch (this.ch2Voice) {
                        case BROKEN:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "So much for getting out of here..."));
                            break;

                        // I think the original game devs forgot to include a line for Voice of the Cheated here...?
                            
                        case COLD:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "Oh well, cabin it is."));
                            break;
                            
                        case HUNTED:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "If this is what you think is best, I'll keep my ears pricked. Hopefully she won't catch us off-guard as easily as she did last time..."));
                            break;
                            
                        case OPPORTUNIST:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "This is probably for the best."));
                            break;
                            
                        case PARANOID:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "One little trick was all it took for you to go in there?"));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "*Sigh.*  I guess you're the one in control, aren't you? So if you want us to die again, I guess we'll die again. Good luck. To all of us."));
                            break;
                            
                        case SKEPTIC:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "Good. Going back to the cabin is the only way we can get to the bottom of things."));
                            break;
                            
                        case SMITTEN:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "You're joking, right? If we're going to the cabin, there's no world where we do anything other than *save* her."));
                            break;
                            
                        case STUBBORN:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "Oh, about *time.* I can't believe you were about to run away."));
                            break;
                    }

                    return 2;

                case "lie":
                    repeatMenu = false;
                    parser.printDialogueLine(new VoiceDialogueLine("Somehow I doubt that, but fine."));
                    parser.printDialogueLine(new VoiceDialogueLine("I suppose you just quietly continue down the path away from the cabin."));
                    break;

                case "nihilist":
                    parser.printDialogueLine(new VoiceDialogueLine("There won't *be* a \"new dawn\" if the world ends. There'll just be *nothing.* Forever!"));
                case "cGoLeave":
                case "nope":
                case "notGoing":
                case "quiet":
                    repeatMenu = false;
                    parser.printDialogueLine(new VoiceDialogueLine("Fine, I suppose you just quietly continue down the path away from the cabin."));
                    break;

                default:
                    this.giveDefaultFailResponse(outcome);
            }
        }

        System.out.println();
        this.currentLocation = GameLocation.HILL;
        parser.printDialogueLine(new DialogueLine("You emerge into the clearing. The cabin waits at the top of the hill."));
        parser.printDialogueLine(new VoiceDialogueLine("That's strange. It looks like this path also leads to the cabin. How convenient! Everything's back on track again. Maybe the world can still be saved after all."));
        if (this.hasVoice(Voice.COLD)) parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "Oh? How quaint. He really wants us to go in there, doesn't He?"));

        leaveMenu = new OptionsMenu();
        leaveMenu.add(new Option(this.manager, "cabin", "Okay, okay! I'm going into the cabin. Sheesh."));
        leaveMenu.add(new Option(this.manager, "commit", "[Turn around (again) and leave (again).]"));

        repeatMenu = true;
        while (repeatMenu) {
            outcome = parser.promptOptionsMenu(leaveMenu);
            switch (outcome) {
                case "cabin":
                    parser.printDialogueLine(new VoiceDialogueLine("That's great to hear! And as long as you bring that fiery attitude to Princess slaying, I think this will all resolve splendidly."));
                case "cGoCabin":
                    switch (this.ch2Voice) {
                        case BROKEN:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "So much for getting out of here..."));
                            break;

                        // I think the original game devs forgot to include a line for Voice of the Cheated here...?
                            
                        case COLD:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "Oh well, cabin it is."));
                            break;
                            
                        case HUNTED:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "If this is what you think is best, I'll keep my ears pricked. Hopefully she won't catch us off-guard as easily as she did last time..."));
                            break;
                            
                        case OPPORTUNIST:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "This is probably for the best."));
                            break;
                            
                        case PARANOID:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "A couple of laps around the woods were all it took for you to go in there?"));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "*Sigh.*  I guess you're the one in control, aren't you? So if you want us to die again, I guess we'll die again. Good luck. To all of us."));
                            break;
                            
                        case SKEPTIC:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "Good. Going back to the cabin is the only way we can get to the bottom of things."));
                            break;
                            
                        case SMITTEN:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "Oh, it's going to resolve splendidly, all right."));
                            break;
                            
                        case STUBBORN:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "Oh, about *time.* I can't believe you were about to run away."));
                            break;
                    }

                    return 3;

                case "cGoPath":
                case "commit":
                    repeatMenu = false;
                    break;

                default:
                    this.giveDefaultFailResponse(outcome);
            }
        }

        this.currentLocation = GameLocation.LEAVING;
        parser.printDialogueLine(new VoiceDialogueLine("You're really keen on wasting everyone's time, aren't you? It's remarkably selfish, if you ask me. I've already outlined the stakes of the situation. If you don't do your job, everyone dies. Like, *dies* dies. Forever."));
        parser.printDialogueLine(new VoiceDialogueLine("But fine. You turn around and trek back down the path you came."));
        
        this.abortVessel(false);
        return 0;
    }


    // - Chapter II: The Adversary -

    /**
     * Runs the opening sequence of The Adversary
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding adversary() {
        // You gain the Voice of the Stubborn

        if (!this.chapter2Intro(true, true, false)) {
            return ChapterEnding.ABORTED;   
        }



        
        // temporary templates for copy-and-pasting
        parser.printDialogueLine(new VoiceDialogueLine("XXXXX"));
        parser.printDialogueLine(new PrincessDialogueLine("XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "(Explore) XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "\"XXXXX\""));

        // PLACEHOLDER
        return null;
    }


    // - Chapter III: The Eye of the Needle -

    /**
     * Runs the opening sequence of The Eye of the Needle
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding eyeOfNeedle() {
        /*
          Possible combinations:
            - Stubborn + Hunted
            - Stubborn + Skeptic
         */

        // PLACEHOLDER
        return null;
    }


    // - Chapter III: The Fury -

    /**
     * Runs the opening sequence of The Fury
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding fury() {
        /*
          Possible combinations:
            - Broken + Stubborn (either Chapter)
            - Stubborn + Cold (Adversary)
            - Stubborn + Contrarian (Adversary)
         */

        switch (this.prevEnding) {
            case STRIKEMEDOWN: this.source = "pacifism";
            case HEARNOBELL: this.source = "unarmed";
            case DEADISDEAD: this.source = "pathetic";
            default: this.source = "tower";
        }

        // PLACEHOLDER
        return null;
    }


    // - Chapter II: The Tower -

    /**
     * Runs the opening sequence of The Tower
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding tower() {
        // You gain the Voice of the Broken

        switch (this.prevEnding) {
            case TOTOWERUNHARMED:
                this.source = "unharmed";
                break;
            
            case TOTOWERPATHETIC:
                this.source = "pathetic";
                break;

            default:
                this.source = "normal";
        }

        if (!this.chapter2Intro(true, false, false)) return ChapterEnding.ABORTED;
        
        // PLACEHOLDER
        return null;
    }


    // - Chapter III: The Apotheosis -

    /**
     * Runs the opening sequence of The Apotheosis
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding apotheosis() {
        /*
          Possible combinations:
            - Broken + Paranoid
            - Broken + Contrarian
         */

        // PLACEHOLDER
        return null;
    }


    // - Chapter II: The Spectre -

    /**
     * Runs the opening sequence of The Spectre
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding spectre() {
        // You gain the Voice of the Cold

        this.isHarsh = false;

        if (!this.chapter2Intro(false, true, true)) {
            return ChapterEnding.ABORTED;
        }

        // PLACEHOLDER
        return null;
    }


    // - Chapter III: The Princess and the Dragon -

    /**
     * Runs the opening sequence of The Princess and the Dragon
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding princessAndDragon() {
        // "You" have Cold + Opportunist, but you do not have any of the voices at the start of the Chapter

        // PLACEHOLDER
        return null;
    }


    // - Chapter III: The Wraith -

    /**
     * Runs the opening sequence of The Wraith
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding wraith() {
        /*
          Possible combinations:
            - Cold + Paranoid (either Chapter)
            - Cold + Cheated (Spectre)
            - Paranoid + Opportunist (Nightmare)
         */

        switch (this.prevEnding) {
            case HEARTRIPPER: this.source = "spectre";
            case HEARTRIPPERLEAVE: this.source = "spectre";
            default: this.source = "nightmare";
        }

        // PLACEHOLDER
        return null;
    }


    // - Chapter II: The Nightmare -

    /**
     * Runs the opening sequence of The Nightmare
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding nightmare() {
        // You gain the Voice of the Paranoid

        this.source = (this.prevEnding == ChapterEnding.TONIGHTMAREFLED) ? "fled" : "normal";
        if (!this.chapter2Intro(true, false, false)) return ChapterEnding.ABORTED;
        
        // PLACEHOLDER
        return null;
    }


    // - Chapter ???: The Moment of Clarity -

    /**
     * Runs the opening sequence of The Moment of Clarity
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding momentOfClarity() {
        // You have all voices

        // PLACEHOLDER
        return null;
    }


    // - Chapter II: The Razor -

    /**
     * Runs the opening sequence of The Razor
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding razor() {
        // You gain the Voice of the Cheated

        switch (this.prevEnding) {
            case TORAZORMUTUAL:
                this.source = "mutual";
                if (!this.chapter2Intro(true, true, false)) return ChapterEnding.ABORTED;

                break;

            case TORAZORREVIVAL:
                this.source = "revival";
                if (!this.chapter2Intro(true, true, false)) return ChapterEnding.ABORTED;
                
                break;

            default:
                this.source = "pathetic";
                if (!this.chapter2Intro(true, false, false)) return ChapterEnding.ABORTED;
        }
        
        // PLACEHOLDER
        return null;
    }


    // - Chapter III: The Arms Race -

    /**
     * Runs the opening sequence of The Arms Race
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding armsRace() {
        /*
          Possible starting combinations:
            - Cheated + Hunted + Stubborn
            - Cheated + Hunted + Broken
            - Cheated + Hunted + Paranoid
         */

        // PLACEHOLDER
        return null;
    }


    // - Chapter IV: Mutually Assured Destruction -

    /**
     * Runs the opening sequence of Mutually Assured Destruction
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding mutuallyAssuredDestruction() {
        // You have all Voices

        // PLACEHOLDER
        return null;
    }


    // - Chapter III: No Way Out -

    /**
     * Runs the opening sequence of No Way Out
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding noWayOut() {
        /*
          Possible starting combinations:
            - Cheated + Contrarian + Broken
            - Cheated + Contrarian + Paranoid
         */

        // PLACEHOLDER
        return null;
    }


    // - Chapter IV: The Empty Cup -

    /**
     * Runs the opening sequence of The Empty Cup
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding emptyCup() {
        // You have all Voices

        // PLACEHOLDER
        return null;
    }


    // - Chapter II: The Beast -

    /**
     * Runs the opening sequence of The Beast
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding beast() {
        // You gain the Voice of the Hunted

        if (!this.chapter2Intro(true, false, false)) {
            return ChapterEnding.ABORTED;
        }
        
        // PLACEHOLDER
        return null;
    }


    // - Chapter III: The Den -

    /**
     * Runs the opening sequence of The Den
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding den() {
        /*
          Possible combinations:
            - Hunted + Stubborn
            - Hunted + Skeptic
         */

        // PLACEHOLDER
        return null;
    }


    // - Chapter III: The Wild -

    /**
     * Runs the opening sequence of The Wild
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding wild() {
        /*
          Possible combinations from Beast:
            - Hunted + Opportunist
            - Hunted + Stubborn
            - Hunted + Broken
            - Hunted + Contrarian

          Possible combinations from Witch:
            - Opportunist + Stubborn
            - Opportunist + Paranoid
            - Opportunist + Cheated
         */

        if (this.hasVoice(Voice.HUNTED)) this.source = "beast";
        else this.source = "witch";

        // PLACEHOLDER
        return null;
    }


    // - Chapter II: The Witch -

    /**
     * Runs the opening sequence of The Witch
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding witch() {
        // You gain the Voice of the Opportunist

        switch (this.prevEnding) {
            case TOWITCHLOCKED:
                this.source = "locked";
                if (!this.chapter2Intro(false, false, false)) return ChapterEnding.ABORTED;
                break;

            case TOWITCHMUTUAL:
                this.source = "mutual";
                if (!this.chapter2Intro(true, true, false)) return ChapterEnding.ABORTED;
                break;

            default:
                this.source = "normal";
                if (!this.chapter2Intro(true, false, false)) return ChapterEnding.ABORTED;
        }
        
        // PLACEHOLDER
        return null;
    }


    // - Chapter III: The Thorn -

    /**
     * Runs the opening sequence of The Thorn
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding thorn() {
        /*
          Possible combinations:
            - Opportunist + Smitten
            - Opportunist + Cheated
         */

        // PLACEHOLDER
        return null;
    }


    // - Chapter II: The Stranger -

    /**
     * Runs the opening sequence of The Stranger
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding stranger() {
        // You gain the Voice of the Contrarian

        // DOESN'T USE CHAPTER2INTRO() -- PROGRAM MANUALLY HERE
        // do make use of ch2IntroShareLoop() though!!!

        manager.setNowPlaying("Fragmentation");

        if (this.isFirstVessel) manager.setFirstPrincess(Chapter.STRANGER);

        parser.printDialogueLine(new VoiceDialogueLine("You're on a path in the woods. And at the end of that path is a cabin. And in the basement of that cabin is a princess."));
        parser.printDialogueLine(new VoiceDialogueLine("You're here to slay her. If you don't, it will be the end of the world."));

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "dejaVu", "(Explore) I'm getting a terrible sense of deja vu."));
        activeMenu.add(new Option(this.manager, "happened", "(Explore) Wait... hasn't this already happened?"));
        activeMenu.add(new Option(this.manager, "no", "(Explore) Okay, no."));
        activeMenu.add(new Option(this.manager, "notKidding", "(Explore) You aren't kidding. She actually ended the world last time, didn't she? What the hell is she?."));
        activeMenu.add(new Option(this.manager, "wise", "(Explore) Oh, you bastard! You're in for it now. I'm wise to your tricks!"));
        activeMenu.add(new Option(this.manager, "died", "(Explore) But I died! The whole world ended! What am I doing here?"));
        activeMenu.add(new Option(this.manager, "walls", "(Explore) Those walls weren't here last time! You can't just force me to go to the cabin."));
        activeMenu.add(new Option(this.manager, "assume", "(Explore)  Let's assume I'm telling the truth, and all of this really did already happen. Why should I listen to you? Why should I bother doing *anything?*", false));
        activeMenu.add(new Option(this.manager, "lie", "(Lie) Yep. Okay. Heading to the cabin now where I'm definitely going to slay that Princess."));
        activeMenu.add(new Option(this.manager, "cabin", "Yeah, yeah. I get it. I'm going to the cabin."));
        activeMenu.add(new Option(this.manager, "proceed", "[Silently proceed to the cabin.]"));
        activeMenu.add(new Option(this.manager, "abort", "\"If I can't run away from the cabin, then I'm just staying here in the woods. Forever.\" [Stay in the woods. Forever.]", 0, manager.nClaimedVessels() >= 1));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "dejaVu":
                    activeMenu.setCondition("assume", true);
                    activeMenu.setCondition("happened", false);
                    activeMenu.setCondition("no", false);
                    activeMenu.setCondition("notKidding", false);
                    activeMenu.setCondition("wise", false);
                    activeMenu.setCondition("died", false);
                    activeMenu.setCondition("proceed", false);

                    parser.printDialogueLine(new VoiceDialogueLine("A terrible sense of deja vu? No, you don't have that. This is the first time either of us have been here."));
                    this.ch2IntroShareLoop(false);
                    break;

                case "happened":
                    activeMenu.setCondition("assume", true);
                    activeMenu.setCondition("dejaVu", false);
                    activeMenu.setCondition("no", false);
                    activeMenu.setCondition("notKidding", false);
                    activeMenu.setCondition("wise", false);
                    activeMenu.setCondition("died", false);
                    activeMenu.setCondition("proceed", false);
                    
                    parser.printDialogueLine(new VoiceDialogueLine("It hasn't. Or if it has, I certainly haven't been a part of it. We've just met for the first time, you and I."));
                    this.ch2IntroShareLoop(false);
                    break;

                case "no":
                    activeMenu.setCondition("assume", true);
                    activeMenu.setCondition("dejaVu", false);
                    activeMenu.setCondition("happened", false);
                    activeMenu.setCondition("notKidding", false);
                    activeMenu.setCondition("wise", false);
                    activeMenu.setCondition("died", false);
                    activeMenu.setCondition("proceed", false);
                    
                    parser.printDialogueLine(new VoiceDialogueLine("Oh, don't you start grandstanding about morals. The fate of the world is at risk right now, and the life of a mere Princess shouldn't stop you from saving us all."));
                    this.ch2IntroShareLoop(false);
                    break;

                case "notKidding":
                    activeMenu.setCondition("assume", true);
                    activeMenu.setCondition("dejaVu", false);
                    activeMenu.setCondition("happened", false);
                    activeMenu.setCondition("no", false);
                    activeMenu.setCondition("wise", false);
                    activeMenu.setCondition("died", false);
                    activeMenu.setCondition("proceed", false);
                    
                    parser.printDialogueLine(new VoiceDialogueLine("Last time? Last I checked there wasn't any \"last time.\" We've just met, you and I."));
                    this.ch2IntroShareLoop(false);
                    break;

                case "wise":
                    activeMenu.setCondition("assume", true);
                    activeMenu.setCondition("dejaVu", false);
                    activeMenu.setCondition("happened", false);
                    activeMenu.setCondition("no", false);
                    activeMenu.setCondition("notKidding", false);
                    activeMenu.setCondition("died", false);
                    activeMenu.setCondition("proceed", false);
                    
                    parser.printDialogueLine(new VoiceDialogueLine("My tricks? What on earth are you talking about? We've just met for the first time."));
                    this.ch2IntroShareLoop(false);
                    break;

                case "died":
                    activeMenu.setCondition("assume", true);
                    activeMenu.setCondition("dejaVu", false);
                    activeMenu.setCondition("happened", false);
                    activeMenu.setCondition("no", false);
                    activeMenu.setCondition("notKidding", false);
                    activeMenu.setCondition("wise", false);
                    activeMenu.setCondition("proceed", false);
                    
                    parser.printDialogueLine(new VoiceDialogueLine("I can assure you that you're not dead. And to answer your second question, you're here to slay the Princess. I literally told you that a second ago."));
                    this.ch2IntroShareLoop(true);
                    break;

                case "walls":
                    this.source = "askedWalls";

                    parser.printDialogueLine(new VoiceDialogueLine("What are you talking about? I'm sure those walls have always been there. It makes sense if you think about it. If there weren't any walls in the woods, someone might have gotten lost. Or, heaven forbid, someone other than you might have stumbled onto the Princess."));
                    
                    if (!this.sharedLoop) {
                        activeMenu.setCondition("assume", true);
                        activeMenu.setCondition("dejaVu", false);
                        activeMenu.setCondition("happened", false);
                        activeMenu.setCondition("no", false);
                        activeMenu.setCondition("notKidding", false);
                        activeMenu.setCondition("wise", false);
                        activeMenu.setCondition("died", false);
                        activeMenu.setCondition("proceed", false);

                        this.ch2IntroShareLoop(false);
                    }

                    break;

                case "assume":
                    this.sharedLoopInsist = true;

                    parser.printDialogueLine(new VoiceDialogueLine("Those are two *very* different questions, but fine. I'll indulge you if that's what it takes to get you moving."));
                    parser.printDialogueLine(new VoiceDialogueLine("Let's say for a moment that this really is the second time you've met me, or, at least, a version of me."));
                    parser.printDialogueLine(new VoiceDialogueLine("You said the world ended, right? I'm assuming that only happened because you didn't listen to me, and you should take that as more than enough evidence that I'm telling the truth about the Princess."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "For all we know, the world just happened to end on its own."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "But He's right. It did end. And what are the odds that something *else* ended it?"));
                    parser.printDialogueLine(new VoiceDialogueLine("Of course she was the one who ended it. And I believe your other question was something along the lines of \"what's the point of doing anything?\" If you're asking that, it sounds to me like you're making the rather dangerous assumption that your actions last time around didn't have any consequences."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "What do you mean? Of course there weren't any consequences. Sure, the world ended, but now everyone's right back where they started. That sounds pretty consequence-free to me."));
                    parser.printDialogueLine(new VoiceDialogueLine("Yes, but, in this purely hypothetical scenario, that begs the question of *how* you got back here. Did \"time\" simply rewind itself, or have you found yourself in another world altogether?"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Are you saying that everyone from the first time around is still dead?"));
                    parser.printDialogueLine(new VoiceDialogueLine("I'm saying it's possible."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Who cares? If that's the case, there's got to be *millions* of worlds out there. What does one of them ending even matter?"));
                    parser.printDialogueLine(new VoiceDialogueLine("Of course it matters! There were people there, and now they're gone! ... at least in this purely hypothetical scenario."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I have to agree with him. On the off chance this is how things work, we should take this scenario a little more seriously."));
                    parser.printDialogueLine(new VoiceDialogueLine("Thank you."));

                    break;

                case "lie":
                    parser.printDialogueLine(new VoiceDialogueLine("You know I can tell when you're lying, right? Please take this seriously. I'm begging you."));
                case "cGoHill":
                case "cabin":
                case "proceed":
                    this.repeatActiveMenu = false;
                    break;

                case "cGoLeave":
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "We already *tried* that. We'll just wind up right back here again."));
                    break;

                case "abort":
                    if (manager.nClaimedVessels() >= 2) {
                        this.canTryAbort = false;
                        activeMenu.setGreyedOut("abort", true);
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }

                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Oooh, that's clever! A little boring, though."));
                    parser.printDialogueLine(new VoiceDialogueLine("It's extremely boring."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Can we really do that? Can we really just do nothing?"));
                    parser.printDialogueLine(new VoiceDialogueLine("No, you can't just do nothing. You have to do *something.*"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "All right! So it's decided. Even if it's boring, we're going to do nothing. Forever!"));

                    System.out.println();
                    parser.printDialogueLine(new VoiceDialogueLine("Congratulations, you continue to waste everyone's time and do nothing... wait, can you still hear me? Everything is getting fuzzy..."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "What is that weird feeling? It's like I'm barely even here anymore."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Well, it's not nothing, that's for sure. Does that mean we messed up?"));

                    this.abortVessel(true);
                    return ChapterEnding.ABORTED;

                default:
                    this.giveDefaultFailResponse(activeOutcome);
            }
        }
        
        this.currentLocation = GameLocation.HILL;
        System.out.println();
        parser.printDialogueLine(new DialogueLine("You emerge into the clearing. The cabin waits at the top of the hill."));
        
        parser.printDialogueLine(new VoiceDialogueLine("A warning, before you go any further..."));
        parser.printDialogueLine(new VoiceDialogueLine("She will lie, she will cheat, and she will do everything in her power to stop you from slaying her. Don't believe a word she says."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "If we're stuck going in there, maybe we *should* believe her. Maybe she *isn't* a liar."));
        parser.printDialogueLine(new VoiceDialogueLine("Ignore him. He's being difficult for the sake of it."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Let's keep an open mind."));

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "proceed", "[Proceed into the cabin.]"));

        this.repeatActiveMenu = true;
        while (this.repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);

            switch (this.activeOutcome) {
                case "cGoCabin":
                case "proceed":
                    this.repeatActiveMenu = false;
                    break;

                case "cGoLeave":
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "We already *tried* that. We'll just wind up right back here again."));
                    
                default:
                    this.giveDefaultFailResponse(this.activeOutcome);
            }
        }
        


        
        // temporary templates for copy-and-pasting
        parser.printDialogueLine(new VoiceDialogueLine("XXXXX"));
        parser.printDialogueLine(new PrincessDialogueLine("XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "(Explore) XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "\"XXXXX\""));

        // PLACEHOLDER
        return null;
    }


    // - Chapter II: The Prisoner -

    /**
     * Runs the opening sequence of The Prisoner
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding prisoner() {
        // You gain the Voice of the Skeptic

        if (!this.chapter2Intro(true, false, true)) {
            return ChapterEnding.ABORTED;
        }
        
        // PLACEHOLDER
        return null;
    }


    // - Chapter III: The Cage -

    /**
     * Runs the opening sequence of The Cage
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding cage() {
        /*
          Possible combinations:
            - Skeptic + Broken
            - Skeptic + Paranoid
            - Skeptic + Cheated
         */

        // PLACEHOLDER
        return null;
    }


    // - Chapter III: The Grey -

    /**
     * Runs the opening sequence of The Grey (coming from the Prisoner)
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding greyDrowned() {
        /*
          You gain the Voice of the Cold
          Possible combinations:
            - Smitten + Cold
         */

        // PLACEHOLDER
        return null;
    }

    /**
     * Runs the opening sequence of The Grey (coming from the Damsel)
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding greyBurned() {
        /*
          You gain the Voice of the Cold
          Possible combinations:
            - Skeptic + Cold
         */

        // PLACEHOLDER
        return null;
    }


    // - Chapter II: The Damsel -

    /**
     * Runs the opening sequence of The Damsel
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding damsel() {
        // You gain the Voice of the Smitten

        if (!this.chapter2Intro(true, false, true)) {
            return ChapterEnding.ABORTED;
        }
        
        // PLACEHOLDER
        return null;
    }


    // - Epilogue: Happily Ever After -

    /**
     * Runs the opening sequence of Happily Ever After
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding happilyEverAfter() {
        /*
          You lose the Voice of the Smitten
          Possible combinations:
            - Skeptic
            - Opportunist
         */

        // PLACEHOLDER
        return null;
    }


    
    /**
     * The player aborts the current Chapter (and therefore the current Cycle as well), contributing to the Oblivion ending
     */
    private void abortVessel(boolean lateJoin) {
        if (!lateJoin) {
            if (this.activeChapter == Chapter.STRANGER || this.activeChapter.getNumber() > 2) {
                parser.printDialogueLine(new VoiceDialogueLine("Wait... something isn't right. Can you still hear me? Everything is getting fuzzy..."));
            } else {
                parser.printDialogueLine(new VoiceDialogueLine("Wait... something isn't right. Can you still hear me? You're supposed to wind up back at the cabin again, but everything is getting fuzzy..."));
            }

            this.currentVoices.put(Voice.NARRATOR, false);

            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "W-what's going on. Where are we?"));

            if (this.hasVoice(Voice.CONTRARIAN)) {
                parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "We're somewhere interesting for once."));
            }
            if (this.hasVoice(Voice.COLD)) {
                parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "I don't know. But it feels like home."));
            }
            if (this.hasVoice(Voice.BROKEN)) {
                parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "We're dead. Obviously."));
            }
            if (this.hasVoice(Voice.HUNTED)) {
                parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "A dark place. Thoughts like us shouldn't be here."));
            }
            if (this.hasVoice(Voice.SKEPTIC)) {
                parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "Did we do this? Is this the end of the world? Was there ever even a world to end?"));
            }
            if (this.hasVoice(Voice.STUBBORN)) {
                parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "I told you we shouldn't have come here, I told you. But did you listen? No."));
            }
            if (this.hasVoice(Voice.SMITTEN)) {
                parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "Oh I don't like this one bit. There's not a single damsel in sight. How dull."));
            }
            if (this.hasVoice(Voice.PARANOID)) {
                parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "It's finally happened, hasn't it? We've finally cracked."));
            }
            if (this.hasVoice(Voice.OPPORTUNIST)) {
                parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "I like it! Seems like it's got some great acoustics..."));
            }
            if (this.hasVoice(Voice.CHEATED)) {
                parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "That son-of-a-bitch flipped over the table, didn't he?"));
            }
        }

        this.clearVoices();

        if (!this.isFirstVessel) {
            parser.printDialogueLine(new DialogueLine("The world around you is unwound, its physical matter replaced by a textured nothing. You find yourself in The Long Quiet once again. Memory returns."));
        } else if (manager.nVesselsAborted() > 0) {
            parser.printDialogueLine(new DialogueLine("The world around you is unwound, its physical matter replaced by a textured nothing. It is quiet. You have been here before. Memory returns."));
        } else {
            parser.printDialogueLine(new DialogueLine("The world around you is unwound, its physical matter replaced by a textured nothingness. It is quiet."));
        }

        parser.printDialogueLine(new DialogueLine("There is a distant rumbling, a sound of many sounds. Undulations pulse louder as something Other comes close."));
        
        switch (manager.nVesselsAborted()) {
            case 0:
                if (!this.isFirstVessel) parser.printDialogueLine(new DialogueLine("You already know what dwells in the empty spaces."));
                parser.printDialogueLine(new DialogueLine("Feelers probe across the fabric of reality. Extremities find your consciousness and wrap themselves around it. You are no longer alone."));

                if (this.isFirstVessel) {
                    parser.printDialogueLine(new DialogueLine("Confusion. \"Why are you here? I am unfinished.\""));
                    parser.printDialogueLine(new DialogueLine("Resistance. Fingers drag claws across the glass surface of your soul."));
                    parser.printDialogueLine(new DialogueLine("Frustration. \"This vessel is full of you. It is useless to us if it doesn't bring more gifts.\""));
                    parser.printDialogueLine(new DialogueLine("Force pushing against your will. \"NO. You cannot go back. Not there.\""));
                    parser.printDialogueLine(new DialogueLine("Regret. \"This world is broken beyond repair. We must weave something new.\""));
                    parser.printDialogueLine(new DialogueLine("A wagging finger. \"There is only so much thread in this place. Do not waste it. I am our only salvation.\""));
                } else {
                    parser.printDialogueLine(new DialogueLine("Resistance. Fingers drag claws across the glass surface of your soul."));
                    parser.printDialogueLine(new DialogueLine("Frustration. \"This vessel is full of you. I need something empty I can crawl inside of. I need something shaped like me.\""));

                    this.activeMenu = new OptionsMenu(true);
                    activeMenu.add(new Option(this.manager, "wake", "This is a nightmare. Wake up."));
                    activeMenu.add(new Option(this.manager, "embrace", "Embrace the thoughts constricting you."));

                    switch (parser.promptOptionsMenu(activeMenu)) {
                        case "wake":
                            parser.printDialogueLine(new DialogueLine("It's not."));
                            break;

                        case "embrace":
                            parser.printDialogueLine(new DialogueLine("Urgency. \"You have a story you need to finish. It is the only way for us to escape this place.\""));
                            parser.printDialogueLine(new DialogueLine("Force pushing against your will. \"NO. You cannot go back. Not there.\""));
                            parser.printDialogueLine(new DialogueLine("Regret. \"This world is broken beyond repair. We must weave something new.\""));
                            parser.printDialogueLine(new DialogueLine("A wagging finger. \"There is only so much thread in this place. Do not waste it. I am our only salvation.\""));
                            break;
                    }
                }

                break;


            case 1:
                parser.printDialogueLine(new DialogueLine("That which dwells in the empty spaces contracts across the edges of your mind again. She is furious."));
                parser.printDialogueLine(new DialogueLine("Betrayal. \"Every door you close on me is a door you close on yourself. Do you want to linger here, entwined with a creature you taught to hate you forever? Eternity never ends.\""));
                parser.printDialogueLine(new DialogueLine("Cold spite. \"Our infinities shrink into something less. I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I --\"", true));
                break;


            case 2:
                parser.printDialogueLine(new DialogueLine("Desperate pleas. \"I do not hate you. I am sorry I said I hate you. I do not have to hate you. We can still leave this place together.\""));
                parser.printDialogueLine(new DialogueLine("An offering. \"We can be friends.\""));
                parser.printDialogueLine(new DialogueLine("Ecstasy. You are elated. You have never felt more elated than you feel now. Everything is good. You cannot remember what it is like to feel anything other than euphoric joy."));
                parser.printDialogueLine(new DialogueLine("A reminder. \"We can be worse than enemies.\""));
                parser.printDialogueLine(new DialogueLine("Agony. You are torn into a million pieces, and you feel pain in each of them. You have never felt more miserable than you feel now. You cannot remember what it is like to feel anything other than anguish."));
                parser.printDialogueLine(new DialogueLine("Mercy. You are elated again. You have never felt more elated than you feel now. In contrast to the agony you've suffered, this elation is better than all of the other elation you have experienced."));

                if (this.isFirstVessel) {
                    parser.printDialogueLine(new DialogueLine("Round eyes looking up at you. \"I need more vessels so that I can be finished. I cannot find them on my own, for they are me. You are the only one who can do this. You are our only salvation.\""));
                } else {
                    parser.printDialogueLine(new DialogueLine("Round eyes looking up at you. \"I need vessels so that I can be finished. I cannot find them on my own, for they are me. You are the only one who can do this. You are our only salvation.\""));
                }

                break;


            case 3:
                parser.printDialogueLine(new DialogueLine("Dejection. Feelers limp against your soul. \"Why?\""));
                parser.printDialogueLine(new DialogueLine("Long silence. A hollow heart."));
                parser.printDialogueLine(new DialogueLine("\"I don't want to see you.\""));
                break;


            case 4:
                parser.printDialogueLine(new DialogueLine("The feelers hold you in a gentle caress."));
                parser.printDialogueLine(new DialogueLine("Resignation. \"I cannot stop you. But our spool is nearly taut.\""));
                parser.printDialogueLine(new DialogueLine("A warning. \"If you come here again, we will be here forever.\""));
                break;


            case 5: // Oblivion ending
                parser.printDialogueLine(new DialogueLine("Oblivion. The many feelers pull your shape into something formless. \"You have made a decision. It is the wrong one. I love you.\""));

                this.activeMenu = new OptionsMenu(true);
                activeMenu.add(new Option(this.manager, "exist", "[Exist.]", 0));
                activeMenu.add(new Option(this.manager, "fade", "[Consciousness fades away.]"));

                for (int i = 0; i < 4; i++) {
                    parser.printDialogueLine(new DialogueLine("You are bliss. Joy and understanding everywhere at once. Your soul threatens to fade away. \"I love you.\""));
                    parser.printDialogueLine(new DialogueLine("You are agony. A numbing arm. A parched throat. An open wound. Your soul forced back into existence. \"I love you.\""));

                    if (i == 3) {
                        switch (parser.promptOptionsMenu(activeMenu)) {
                            case "exist":
                                // You can keep doing this forever, if you want
                                i -= 1;
                                break;

                            case "fade":
                                // End the game
                                break;
                        }
                    }
                }

                break;
        }

        if (manager.nVesselsAborted() != 5) {
            System.out.println();
            parser.printDialogueLine(new DialogueLine("All at once, the nothingness shatters."));
        }
    }



    // - The Mirror -

    /**
     * Runs the mirror sequence after claiming a vessel
     */
    private void mirrorSequence() {
        this.cancelTimer();
        this.hasBlade = false;

        this.threwBlade = false;
        this.sharedLoop = false;
        this.bladeReverse = false;

        this.repeatActiveMenu = false;
        this.reverseDirection = false;
        this.withBlade = false;
        this.canSlayPrincess = false;
        this.canSlaySelf = false;
        this.canDropBlade = false;

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
                break;
        }
    }

    /**
     * Runs the encounter with the Shifting Mound after claiming each vessel
     */
    private void theSpacesBetween() {
        System.out.println();
        System.out.println();
        System.out.println();

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

    /**
     * Prints the Shifting Mound's thoughts on a given Vessel
     * @param v the Vessel being commented on
     */
    private void giveVesselThoughts(Vessel v) {
        // thoughts on the current vessel
    }

}
