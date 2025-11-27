import java.util.ArrayList;
import java.util.HashMap;

public class StandardCycle extends Cycle {

    // One CYCLE = from beginning of Ch1 to the end of Shifting Mound interlude

    private boolean isFirstVessel;
    private ArrayList<Voice> voicesMet;
    private ArrayList<Chapter> route;
    private ChapterEnding prevEnding;

    // Utility variables for checking command availability & default responses
    private boolean goodEndingAttempted;
    private boolean localGoodEndingAttempt = false;

    // Variables that are used in a lot of chapters
    private boolean mentionedLooping = false; // Used in all Chapter 2s and 3s: does the Narrator know?

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

        this.voicesMet = new ArrayList<>();

        this.currentVoices = new HashMap<>();
        for (Voice v : Voice.values()) {
            if (v == Voice.NARRATOR || v == Voice.HERO) {
                this.currentVoices.put(v, true);
            } else {
                this.currentVoices.put(v, false);
            }
        }

        this.goodEndingAttempted = manager.goodEndingAttempted();
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
            case "": this.go("backTRUE");

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

    // --- CYCLE MANAGEMENT ---

    /**
     * Initiates and coordinates a full cycle, from the beginning of Chapter I through the player's conversation with the Shifting Mound
     */
    @Override
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
        boolean canSoft = !(manager.hasVisited(Chapter.BEAST) && manager.hasVisited(Chapter.WITCH) && manager.hasVisited(Chapter.DAMSEL) && manager.hasVisited(Chapter.NIGHTMARE));
        boolean canHarsh = !(manager.hasVisited(Chapter.ADVERSARY) && manager.hasVisited(Chapter.TOWER) && manager.hasVisited(Chapter.SPECTRE) && manager.hasVisited(Chapter.NIGHTMARE) && manager.hasVisited(Chapter.RAZOR) && manager.hasVisited(Chapter.PRISONER));
        boolean canCabin = canSoft || canHarsh;

        boolean canStranger = !manager.hasVisited(Chapter.STRANGER);

        boolean skipHillDialogue = false;

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
        activeMenu.add(new Option(this.manager, "reluctant", canCabin, "Look, I'll go to the cabin and I'll talk to her, and if she's as bad as you say she is then *maybe* I'll slay her. But I'm not committing to anything until I've had the chance to meet her face to face.", false));
        activeMenu.add(new Option(this.manager, "okFine", canCabin, "Okay. Fine. I'll go to the cabin.", activeMenu.get("refuse")));
        activeMenu.add(new Option(this.manager, "sold", canCabin, "Okay, I'm sold. Let's get this over with.", activeMenu.get("question1")));
        activeMenu.add(new Option(this.manager, "thanks", canCabin, "Oh, okay. Thanks for telling me what to do."));
        activeMenu.add(new Option(this.manager, "sweet", canCabin, "Sweet! I've always wanted to off a monarch. Viva la revolución!"));
        activeMenu.add(new Option(this.manager, "silent", canCabin, "[Silently continue to the cabin.]"));
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
                    askPrize = true;
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
                            this.currentLocation = GameLocation.HILL;
                            this.repeatActiveMenu = false;
                            canStranger = false;
                            break;
                    }

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
                    this.giveDefaultFailResponse(this.activeOutcome);
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

                    switch (this.attemptStranger(true)) {
                        case 0:
                            return ChapterEnding.TOSTRANGER;
                        case 2:
                        case 1:
                            this.currentLocation = GameLocation.HILL;
                            this.repeatActiveMenu = false;
                            canStranger = false;
                            break;
                    }
                    
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
                    this.giveDefaultFailResponse(this.activeOutcome);
            }
        }

        this.currentLocation = GameLocation.CABIN;
        this.knowsBlade = true;
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
                    parser.printDialogueLine("The door to the basement creaks open.");
                    return (this.isHarsh) ? this.basementHarsh(askPrize) : this.basementSoft();

                case "cGoHill":
                    if (manager.hasVisited(Chapter.STRANGER)) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    } else if (!canStranger) {
                        parser.printDialogueLine("You have already tried that.");
                        break;
                    }

                    switch (this.attemptStranger(true)) {
                        case 0:
                            return ChapterEnding.TOSTRANGER;
                        case 2:
                        case 1:
                            this.currentLocation = GameLocation.CABIN;
                            this.repeatActiveMenu = false;
                            canStranger = false;
                            break;
                    }
                    
                case "cGoFail":
                case "cEnterFail":
                case "cLeaveFail":
                case "cApproachFail":
                case "cSlayNoPrincessFail":
                case "cSlaySelfNoBladeFail":
                case "cSlaySelfFail":
                case "cTakeHasBladeFail":
                case "cTakeFail":
                case "cDropNoBladeFail":
                case "cDropFail":
                case "cThrowNoBladeFail":
                case "cThrowFail":
                case "cFail":
                    this.giveDefaultFailResponse(this.activeOutcome);
            }
        }

        throw new RuntimeException("No ending reached");
    }

    /**
     * The player attempts to leave and not go to the cabin (leads to Chapter II: The Stranger)
     * @param canCabin whether the player can go to the cabin or the routes are blocked
     * @return 0 if the player commits to going to the Stranger; 1 if the player returns to the cabin at the first menu; 2 otherwise
     */
    private int attemptStranger(boolean canCabin) {
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
        leaveMenu.add(new Option(this.manager, "cabin", canCabin, "Okay, okay! I'm going into the cabin. Sheesh."));
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
                    this.giveDefaultFailResponse(outcome);
            }
        }

        this.currentLocation = GameLocation.LEAVING;
        parser.printDialogueLine(new VoiceDialogueLine("But fine. You turn around and trek back down the path you came."));

        System.out.println();
        parser.printDialogueLine(new DialogueLine("Eventually, the cabin comes into view once again."));
        parser.printDialogueLine(new VoiceDialogueLine("Oh, would you look at that! You're at the cabin again! Now, I'm not normally one for superstition or astrology, but I have to say, it seems like the Universe itself is doing its best to bring you to your fated confrontation with the Princess."));

        leaveMenu = new OptionsMenu();
        leaveMenu.add(new Option(this.manager, "cabin", canCabin, "There's no fighting this, is there? I have to go into the cabin, don't I? Fine."));
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
    private ChapterEnding basementSoft() {
        boolean canDamsel = !manager.hasVisited(Chapter.DAMSEL);
        boolean canBeast = !manager.hasVisited(Chapter.BEAST);
        boolean canWitch = !manager.hasVisited(Chapter.WITCH);
        boolean canNightmare = !manager.hasVisited(Chapter.NIGHTMARE);

        boolean canFree = canDamsel || canWitch;
        boolean canNotFree = canBeast || canWitch || canNightmare;
        boolean canGetBlade = canBeast || canWitch;

        this.currentLocation = GameLocation.STAIRS;
        System.out.println();
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

                    return this.rescueSoft(false, hereToSave && !lieSave, false, false, canFree, canNotFree);
                
                case "cGoStairs":
                    parser.printDialogueLine(new VoiceDialogueLine("What? No. You've hardly even laid eyes on the Princess, you can't just abandon your duty now."));
                    break;

                case "cGoFail":
                case "cEnterFail":
                case "cLeaveFail":
                case "cApproachFail":
                case "cSlayPrincessNoBladeFail":
                case "cSlaySelfNoBladeFail":
                case "cTakeFail":
                case "cDropNoBladeFail":
                case "cThrowNoBladeFail":
                case "cFail":
                    this.giveDefaultFailResponse(this.activeOutcome);
            }
        }

        System.out.println();
        parser.printDialogueLine("You walk a bit closer to the princess and take a seat on the hard stone floor, putting the two of you at eye level. She smiles up at you, a hopeful glimmer in her eyes. She truly is beautiful.");

        OptionsMenu subMenu;
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
                    if (this.shareTaskSoft(false, canFree)) {
                        return this.rescueSoft(true, false, false, true, canFree, canNotFree);
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
                            if (this.shareTaskSoft(false, canFree)) {
                                return this.rescueSoft(true, hereToSave && !lieSave, false, true, canFree, canNotFree);
                            } else {
                                if (this.whatWouldYouDo) activeMenu.setCondition("whatWouldYouDo", false);
                            }
                            
                        case "notRight":
                            parser.printDialogueLine(new PrincessDialogueLine("Would everything feel right about you if you were locked away in a hole by yourself for as long as you can remember?"));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Just how long *has* she been down here?"));
                            parser.printDialogueLine(new PrincessDialogueLine("So... did they tell you why I'm supposed to be so dangerous?"));

                            if (this.shareTaskSoft(true, canFree)) {
                                return this.rescueSoft(true, hereToSave && !lieSave, false, true, canFree, canNotFree);
                            } else {
                                if (this.whatWouldYouDo) activeMenu.setCondition("whatWouldYouDo", false);
                            }
                    }

                case "whatWouldYouDo":
                    this.whatWouldYouDo = true;
                    parser.printDialogueLine(new VoiceDialogueLine("The Princess hesitates before responding."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "She doesn't know. She's been down here too long to have any idea of what she'd do in another life."));
                    parser.printDialogueLine(new VoiceDialogueLine("She knows what she'd do. She's just searching for whatever answer she thinks you want to hear."));
                    parser.printDialogueLine(new PrincessDialogueLine("Are you looking for the truth, or are you looking for the 'right' answer? Because with the dynamic we have going on here I don't think the specifics of what I'd 'do' really matter."));
                    parser.printDialogueLine(new PrincessDialogueLine("It's not like you'd believe me."));

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
                                return this.retrieveBlade(false);

                            case "free":
                                if (!manager.confirmContentWarnings("self-mutilation", true)) break;

                                parser.printDialogueLine(new VoiceDialogueLine("You can't be *serious*-"));
                                parser.printDialogueLine(new PrincessDialogueLine("Thank you, thank you! You won't regret this, I promise."));
                                parser.printDialogueLine(new VoiceDialogueLine("You're making a huge mistake."));
                                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "No. I think you're doing the right thing."));

                                return this.rescueSoft(true, hereToSave && !lieSave, false, true, canFree, canNotFree);

                            case "lock":
                                if (manager.confirmContentWarnings(Chapter.NIGHTMARE)) repeatSub = false;
                                break;
                        }
                    }

                    // lock continues here
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
                                this.toNightmare(false, false);
                                return ChapterEnding.TONIGHTMARE;

                            case "slay":
                                if (!manager.confirmContentWarnings("mutilation", true)) break;

                                parser.printDialogueLine(new VoiceDialogueLine("It's never too late to do the right thing. Now hurry."));

                                System.out.println();
                                return this.retrieveBlade(false);
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
                    return this.retrieveBlade(true);
                
                case "cGoStairs":
                    parser.printDialogueLine(new VoiceDialogueLine("Oh? Have you finally decided to slay her? Maybe the world isn't doomed after all."));

                    subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "slay", !canGetBlade, "Yes. Something here just doesn't add up, and it isn't worth the risk to take her word over the potential fate of the world. [Retrieve the blade.]"));
                    subMenu.add(new Option(this.manager, "lock", !canNightmare, "No, but I can't just set her free. I don't have enough information to make a decision yet. I'm going to keep her locked away down here, at least for a little bit. We can get to know each other better while I decide what to do. [Keep her locked away.]", 0));
                    subMenu.add(new Option(this.manager, "nevermind", "No. Not yet. Actually, I still have a few more questions for her before I make a decision. [Turn back.]"));

                    String choice = "";
                    repeatSub = true;
                    while (repeatSub) {
                        choice = parser.promptOptionsMenu(subMenu);
                        switch (choice) {
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
                                            this.toNightmare(false, false);
                                            return ChapterEnding.TONIGHTMARE;

                                        case "slay":
                                            if (!manager.confirmContentWarnings("mutilation", true)) break;

                                            parser.printDialogueLine(new VoiceDialogueLine("It's never too late to do the right thing. Now hurry."));

                                            System.out.println();
                                            return this.retrieveBlade(false);
                                    }
                                }
                                
                                break; // Should be unreachable

                            case "nevermind":
                                repeatSub = false;
                                break;
                        }
                    }

                    if (!choice.equals("slay")) break;

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
                    return this.retrieveBlade(false);
                
                case "freeDontRegret":
                    if (!manager.confirmContentWarnings("self-mutilation", true)) break;

                    return this.rescueSoft(false, hereToSave && !lieSave, true, true, canFree, canNotFree);
                case "free":
                    if (!manager.confirmContentWarnings("self-mutilation", true)) break;

                    return this.rescueSoft(false, hereToSave && !lieSave, false, true, canFree, canNotFree);

                case "cGoFail":
                case "cEnterFail":
                case "cLeaveFail":
                case "cApproachFail":
                case "cSlayPrincessNoBladeFail":
                case "cSlaySelfNoBladeFail":
                case "cTakeFail":
                case "cDropNoBladeFail":
                case "cThrowNoBladeFail":
                case "cFail":
                    this.giveDefaultFailResponse(this.activeOutcome);
            }
        }

        throw new RuntimeException("No ending reached");
    }

    /**
     * The player tells the soft Princess that she's allegedly going to end the world
     * @param joinLate whether to skip the first block of dialogue or not
     * @param canFree whether the player can free the Princess or the route is blocked
     * @return true if the player chooses to free the Princess; false otherwise
     */
    private boolean shareTaskSoft(boolean joinLate, boolean canFree) {
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
    private ChapterEnding rescueSoft(boolean lateJoin, boolean hereToSaveTruth, boolean dontRegret, boolean talked, boolean canFree, boolean canNotFree) {
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

                case "cGoFail":
                case "cEnterFail":
                case "cLeaveFail":
                case "cApproachFail":
                case "cSlaySelfNoBladeFail":
                case "cTakeFail":
                case "cDropNoBladeFail":
                case "cThrowNoBladeFail":
                case "cFail":
                    this.giveDefaultFailResponse(this.activeOutcome);
            }
        }

        this.currentLocation = GameLocation.BASEMENT;
        this.reverseDirection = false;
        this.withPrincess = true;

        System.out.println();
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
        activeMenu.add(new Option(this.manager, "save", !canFree, "[Save the Princess.]"));
        activeMenu.add(new Option(this.manager, "slay", !canNotFree || hereToSaveTruth, "[Slay the Princess.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "save":
                    if (manager.confirmContentWarnings("mutilation; loss of bodily autonomy", true)) {
                        return this.rescueCommitSoft();
                    } else {
                        break;
                    }

                case "cSlayPrincess":
                    if (!canNotFree || hereToSaveTruth) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                case "slay":
                    return this.rescueSlaySoft();

                case "cGoStairs":
                case "cGoFail":
                case "cEnterFail":
                case "cLeaveFail":
                case "cApproachFail":
                case "cSlaySelfFail":
                case "cTakeHasBladeFail":
                case "cDropFail":
                case "cThrowFail":
                case "cFail":
                    this.giveDefaultFailResponse(this.activeOutcome);
            }
        }

        throw new RuntimeException("No ending reached");
    }

    /**
     * The player commits to freeing the soft Princess (leads to Chapter II: The Damsel / The Witch)
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding rescueCommitSoft() {
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
                    
                case "cGoFail":
                case "cEnterFail":
                case "cLeaveFail":
                case "cApproachFail":
                case "cSlayPrincessFail":
                case "cSlaySelfFail":
                case "cTakeHasBladeFail":
                case "cDropFail":
                case "cThrowFail":
                case "cFail":
                    this.giveDefaultFailResponse(this.activeOutcome);
            }
        }
        
        parser.printDialogueLine(new VoiceDialogueLine("No. We won't have any of that. The stakes are too high. You can't just let her escape into the world."));
        parser.printDialogueLine(new VoiceDialogueLine("... no. *I* can't just let her escape into the world."));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("As the Princess approaches the bottom stair, your body steps forward and raises the blade."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Wait... this isn't fair. You can't just *do* that!"));
        parser.printDialogueLine(new VoiceDialogueLine("Watch me."));

        System.out.println();
        parser.printDialogueLine(new PrincessDialogueLine("Wh-what are you doing?"));

        this.canSlayPrincess = true;
        Option slay = new Option(this.manager, "slay", manager.hasVisited(Chapter.WITCH), "[Slay the Princess.]", 0);

        this.activeMenu = new OptionsMenu();
        for (int i = 0; i < 13; i++) activeMenu.add(slay);
        activeMenu.add(new Option(this.manager, "warn", manager.hasVisited(Chapter.DAMSEL), "[Warn her.]"));
        for (int i = 0; i < 4; i++) activeMenu.add(slay);

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "cSlayPrincess":
                    if (manager.hasVisited(Chapter.WITCH)) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                case "slay":
                    if (!manager.confirmContentWarnings(Chapter.WITCH)) {
                        break;
                    }
                    
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Okay. There's no going back now. I'm with you to the end."));

                    System.out.println();
                    parser.printDialogueLine(new VoiceDialogueLine("You bring the blade down to strike at the Princess's heart."));
                    parser.printDialogueLine(new VoiceDialogueLine("But she's fast. She ducks to the floor, your blade narrowly grazing her backside. Slaying her won't be easy now that she's free."));
                    parser.printDialogueLine(new PrincessDialogueLine("We could have gotten out of here together! Were you just lying to me this whole time?"));
                    parser.printDialogueLine(new PrincessDialogueLine("I don't know what's come over you, but if I have to kill you, then I'll kill you. Do you think I need both of my arms to do that?"));
                    
                    this.rescueControlledSlay();
                    return ChapterEnding.TOWITCH;

                case "save":
                    this.repeatActiveMenu = false;
                    break;

                case "cGoStairs":
                case "cGoFail":
                case "cEnterFail":
                case "cLeaveFail":
                case "cApproachFail":
                case "cSlaySelfFail":
                case "cTakeHasBladeFail":
                case "cDropFail":
                case "cThrowFail":
                case "cFail":
                    this.giveDefaultFailResponse(this.activeOutcome);
            }
        }

        parser.printDialogueLine(new VoiceDialogueLine("Stop that."));
        parser.printDialogueLine(new PrincessDialogueLine("Something's come over you, hasn't it? Y-you know you don't have to do this, right?"));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("Your body lunges forward, the blade held low, ready to sink into her heart."));
        parser.printDialogueLine(new VoiceDialogueLine("But the Princess dodges, stumbling back against the wall before the blade has a chance to connect."));
        parser.printDialogueLine(new VoiceDialogueLine("Stop it! Stop trying to resist me! I'm trying to get you out of here alive."));
        
        this.activeMenu = new OptionsMenu();
        for (int i = 0; i < 13; i++) activeMenu.add(slay);
        activeMenu.add(new Option(this.manager, "resist", manager.hasVisited(Chapter.DAMSEL), "[Resist.]", 0));
        for (int i = 0; i < 6; i++) activeMenu.add(slay);

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "cSlayPrincess":
                    if (manager.hasVisited(Chapter.WITCH)) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                case "slay":
                    if (!manager.confirmContentWarnings(Chapter.WITCH)) {
                        break;
                    }
                    
                    parser.printDialogueLine(new VoiceDialogueLine("*Thank* you."));
                    parser.printDialogueLine(new PrincessDialogueLine("There's no getting through to you right now, is there?"));
                    parser.printDialogueLine(new PrincessDialogueLine("A betrayal of will is still a betrayal. You'll regret thinking of me as a helpless damsel."));

                    this.rescueControlledSlay();
                    return ChapterEnding.TOWITCH;

                case "resist":
                    if (!manager.confirmContentWarnings(Chapter.DAMSEL)) {
                        break;
                    }

                    this.repeatActiveMenu = false;
                    break;

                case "cGoStairs":
                case "cGoFail":
                case "cEnterFail":
                case "cLeaveFail":
                case "cApproachFail":
                case "cSlaySelfFail":
                case "cTakeHasBladeFail":
                case "cDropFail":
                case "cThrowFail":
                case "cFail":
                    this.giveDefaultFailResponse(this.activeOutcome);
            }
        }

        // Committed to Damsel
        parser.printDialogueLine(new VoiceDialogueLine("The blade! Move. The. Blade!"));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("As your body remains frozen in stubborn resistance, the Princess takes a cautious step forward."));
        parser.printDialogueLine(new PrincessDialogueLine("We both know this isn't you..."));
        this.hasBlade = false;
        parser.printDialogueLine(new VoiceDialogueLine("She nervously reaches towards you and takes the blade from your infuriatingly rigid hands... What are you *doing?*"));
        parser.printDialogueLine(new PrincessDialogueLine("I'm sorry... I'll try to be quick."));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("She plunges it into your chest, tearing through flesh and sinew. It is *agony.* But you aren't dead yet."));
        parser.printDialogueLine(new PrincessDialogueLine("Oh no, I'm so sorry!"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Stay strong. We can tough it out until it's done. For her sake."));
        parser.printDialogueLine(new VoiceDialogueLine("For *her* sake? Don't you start pretending that dying a painful death is some sort of heroic gesture. The two of you have literally doomed *everyone.*"));

        System.out.println();
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
    private void rescueControlledSlay() {
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
    private ChapterEnding rescueSlaySoft() {
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

                case "cGoStairs":
                case "cGoFail":
                case "cEnterFail":
                case "cLeaveFail":
                case "cApproachFail":
                case "cSlaySelfFail":
                case "cTakeHasBladeFail":
                case "cDropFail":
                case "cThrowFail":
                case "cFail":
                    this.giveDefaultFailResponse(this.activeOutcome);
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
                    this.toNightmare(true, true);
                    return ChapterEnding.TONIGHTMAREFLED;

                case "cGoFail":
                case "cEnterFail":
                case "cLeaveFail":
                case "cApproachFail":
                case "cSlaySelfFail":
                case "cTakeHasBladeFail":
                case "cDropFail":
                case "cThrowFail":
                case "cFail":
                    this.giveDefaultFailResponse(this.activeOutcome);
            }
        }



        parser.printDialogueLine(new VoiceDialogueLine("XXXXX"));
        


        parser.printDialogueLine(new VoiceDialogueLine("XXXXX"));
        parser.printDialogueLine(new PrincessDialogueLine("XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "(Explore) XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "\"XXXXX\""));

        // PLACEHOLDER
        return null;
    }

    /**
     * The player decides to retrieve the blade and slay the soft Princess (leads to Chapter II: the Witch / The Beast)
     * @param worthRisk whether the player said setting the Princess free wasn't "worth the risk"
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding retrieveBlade(boolean worthRisk) {
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
        boolean repeatSub = false;

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
                                this.toNightmare(true, true);
                                return ChapterEnding.TONIGHTMARE;
                            
                            case "cGoBasement":
                            case "finish":
                                repeatSub = false;
                                break;

                            case "cGoFail":
                            case "cEnterFail":
                            case "cLeaveFail":
                            case "cApproachFail":
                            case "cSlayNoPrincessFail":
                            case "cSlaySelfFail":
                            case "cTakeHasBladeFail":
                            case "cDropFail":
                            case "cThrowFail":
                            case "cFail":
                                this.giveDefaultFailResponse(outcome);
                        }
                    }

                case "cGoBasement":
                case "finish":
                    this.repeatActiveMenu = false;
                    break;

                case "cGoFail":
                case "cEnterFail":
                case "cLeaveFail":
                case "cApproachFail":
                case "cSlayNoPrincessFail":
                case "cSlaySelfFail":
                case "cTakeHasBladeFail":
                case "cDropFail":
                case "cThrowFail":
                case "cFail":
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
                            this.bladeBeastWaiting();
                            break;

                        case "shadows":
                            this.bladeBeastShadows();
                            break;
                    }
                    
                    return ChapterEnding.TOBEAST;

                case "wait":
                    if (!manager.confirmContentWarnings(Chapter.BEAST)) break;

                    parser.printDialogueLine(new PrincessDialogueLine("Oh? Do you want to play a waiting game? I've been down here for a long, long time. I'm very good at waiting."));
                    this.bladeBeastWaiting();
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
    private void bladeBeastWaiting() {
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
                this.bladeBeastShadows();
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
    private void bladeBeastShadows() {
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
    private ChapterEnding basementHarsh(boolean askPrize) {
        boolean canNightmare = !manager.hasVisited(Chapter.NIGHTMARE);

        boolean canHesitateSlay = !manager.hasVisited(Chapter.ADVERSARY) || !manager.hasVisited(Chapter.TOWER) || !canNightmare;
        boolean mustSpectre = !canHesitateSlay && manager.hasVisited(Chapter.PRISONER);

        this.currentLocation = GameLocation.STAIRS;
        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("The door to the basement creaks open, revealing a staircase faintly illuminated by an unseen light in the room below. This is an oppressive place. The air feels heavy and damp, a hint of rot filtering from the ancient wood. If the Princess really lives here, slaying her is probably doing her a favor."));
        parser.printDialogueLine(new VoiceDialogueLine("Her voice carries up the stairs."));
        parser.printDialogueLine(new PrincessDialogueLine("Who's there?"));
        parser.printDialogueLine(new VoiceDialogueLine("She sounds... dangerous... It's almost as if she's the one in charge down here."));
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

                case "cGoFail":
                case "cEnterFail":
                case "cLeaveFail":
                case "cApproachFail":
                case "cSlayNoPrincessFail":
                case "cSlaySelfFail":
                case "cTakeHasBladeFail":
                case "cDropFail":
                case "cThrowFail":
                case "cFail":
                    this.giveDefaultFailResponse(this.activeOutcome);
            }
        }

        this.currentLocation = GameLocation.BASEMENT;
        System.out.println();
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
                
                return this.steelNervesHarsh(canHesitateSlay, mustSpectre, false, askPrize);
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

                    return this.dropBladeHarsh(canHesitateSlay, false, false);

                case "cSlayPrincess":
                case "tighten":
                    parser.printDialogueLine(new VoiceDialogueLine("You ignore the trembling in your hands and tighten your grip on the blade."));
                    parser.printDialogueLine(new PrincessDialogueLine("You poor thing, your hands are shaking. Are you... scared of me? Because you should be."));
                    
                    return this.steelNervesHarsh(canHesitateSlay, mustSpectre, hesitated, askPrize);

                case "cGoStairs":
                case "cGoFail":
                case "cEnterFail":
                case "cLeaveFail":
                case "cApproachFail":
                case "cSlaySelfFail":
                case "cTakeHasBladeFail":
                case "cThrowFail":
                case "cFail":
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
    private ChapterEnding steelNervesHarsh(boolean canHesitateSlay, boolean mustSpectre, boolean hesitated, boolean askPrize) {
        boolean canTower = !manager.hasVisited(Chapter.TOWER);
        boolean canSpectre = !manager.hasVisited(Chapter.SPECTRE);
        boolean canRazor = !manager.hasVisited(Chapter.RAZOR);

        boolean canSlay = (!hesitated && (canSpectre || canRazor)) || (hesitated && canHesitateSlay);
        boolean afraid = false;
        boolean isArmed = false;

        // steeled: Spectre / Razor (no hesitation)
        // hesitated: Adversary / Tower / Nightmare
        // tower
        // razor
        String slayTarget;
        slayTarget = (hesitated) ? "hesitated" : "steeled";
        
        System.out.println();
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
                    slayTarget = "tower";
                    canSlay = canTower;
                    activeMenu.setCondition("slay", canSlay);
                    
                    parser.printDialogueLine(new VoiceDialogueLine("If you go into this expecting to die, you're going to die."));
                    parser.printDialogueLine(new PrincessDialogueLine("Hesitating? Why don't you drop the knife and the two of us can be civilized with each other."));
                    break;

                case "isArmed":
                    activeMenu.setCondition("bluff", false);
                    activeMenu.setCondition("sorry", false);
                    isArmed = true;
                    slayTarget = "razor";
                    canSlay = canRazor;
                    activeMenu.setCondition("slay", canSlay);
                    
                    parser.printDialogueLine(new VoiceDialogueLine("I'm positive."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I'm not. But we'll keep our eyes peeled. If she has a weapon, she'll have to draw it before she can use it."));
                    parser.printDialogueLine(new PrincessDialogueLine("Hesitating? Why don't you drop the knife and the two of us can be civilized with each other."));
                    break;

                case "sorry":
                    activeMenu.setCondition("bluff", false);
                    activeMenu.setCondition("isArmed", false);
                    slayTarget = "hesitated"; // Adversary / Tower / Nightmare
                    canSlay = canHesitateSlay;
                    activeMenu.setCondition("slay", canSlay);
                    
                    parser.printDialogueLine(new VoiceDialogueLine("You're so close! Don't give up, you've come this far."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "No, this is a good idea. Maybe we can de-escalate things."));
                    parser.printDialogueLine(new PrincessDialogueLine("Oh, threatened, are we? You poor thing. Drop the knife and of course we can talk."));
                    break;

                case "noDrop":
                    this.repeatActiveMenu = false;
                    slayTarget = "hesitated"; // Adversary / Tower / Nightmare
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

                    return this.dropBladeHarsh(canHesitateSlay, afraid, isArmed);

                case "cSlayPrincess":
                    if (!canSlay) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                case "slay":
                    if (slayTarget.equals("steeled") && !canRazor) {
                        if (!manager.confirmContentWarnings(Chapter.SPECTRE)) break;
                    } else if (slayTarget.equals("razor") || (slayTarget.equals("steeled") && !canSpectre)) {
                        if (!manager.confirmContentWarnings(Chapter.RAZOR)) break;
                    } else if (slayTarget.equals("tower")) {
                        if (!manager.confirmContentWarnings(Chapter.TOWER)) break;
                    }

                    return this.slayHarsh(slayTarget, askPrize);

                case "cGoStairs":
                case "cGoFail":
                case "cEnterFail":
                case "cLeaveFail":
                case "cApproachFail":
                case "cSlaySelfFail":
                case "cTakeHasBladeFail":
                case "cThrowFail":
                case "cFail":
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

                    return this.dropBladeHarsh(canHesitateSlay, afraid, isArmed);

                case "cSlayPrincess":
                    if (!canSlay) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                case "slay":
                    if (slayTarget.equals("razor")) {
                        if (!manager.confirmContentWarnings(Chapter.RAZOR)) break;
                    } else if (slayTarget.equals("tower")) {
                        if (!manager.confirmContentWarnings(Chapter.TOWER)) break;
                    }

                    return this.slayHarsh(slayTarget);

                case "cGoStairs":
                case "cGoFail":
                case "cEnterFail":
                case "cLeaveFail":
                case "cApproachFail":
                case "cSlaySelfFail":
                case "cTakeHasBladeFail":
                case "cThrowFail":
                case "cFail":
                    this.giveDefaultFailResponse(this.activeOutcome);
            }
        }

        throw new RuntimeException("No ending reached");
    }

    /**
     * The player drops the blade to talk to the harsh Princess
     * @param canSlay whether the player can slay the Princess after hesitating or the routes are blocked
     * @param afraid whether the player wondered if the Princess was bluffing
     * @param isArmed whether the player wondered if the Princess is armed
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding dropBladeHarsh(boolean canSlay, boolean afraid, boolean isArmed) {
        this.canSlayPrincess = false;
        this.canDropBlade = false;
        this.hasBlade = false;
        


        parser.printDialogueLine(new VoiceDialogueLine("XXXXX"));
        parser.printDialogueLine(new PrincessDialogueLine("XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "(Explore) XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "\"XXXXX\""));


        // PLACEHOLDER
        return null;
    }

    /**
     * The player attempts to slay the harsh Princess
     * @param origin the path the player attempted to slay the Princess from; determines the outcome
     * @param askPrize whether the player asked the Narrator about their prize
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding slayHarsh(String origin, boolean askPrize) {
        // Origin:
        // steeled = Spectre / Razor
        // hesitated = Adversary / Tower / Nightmare
        // tower
        // razor

        this.canDropBlade = false;
        switch (origin) {
            case "steeled": return this.slayHarshSteeled(askPrize);
            case "hesitated": return this.slayHarshHesitated();
            case "tower":
                this.slayHarshForceTower();
                return ChapterEnding.TOTOWERUNHARMED;
            case "razor": return this.slayHarshForceRazor();

            default: throw new RuntimeException("Invalid slay origin");
        }
    }

    /**
     * The player attempts to slay the harsh Princess
     * @param origin the path the player attempted to slay the Princess from; determines the outcome
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding slayHarsh(String origin) {
        return this.slayHarsh(origin, false);
    }

    /**
     * The player slays the harsh Princess without hesitation (leads to Chapter II: The Spectre / The Razor)
     * @param askPrize whether the player asked the Narrator about their prize
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding slayHarshSteeled(boolean askPrize) {
        parser.printDialogueLine(new VoiceDialogueLine("You lunge forward without a moment's hesitation."));
        parser.printDialogueLine(new VoiceDialogueLine("You feel flesh easily give way and look down to see your blade already sinking deep into her heart."));
        parser.printDialogueLine(new PrincessDialogueLine("O... oh."));
        parser.printDialogueLine(new PrincessDialogueLine("This is it, isn't it?"));
        parser.printDialogueLine(new PrincessDialogueLine("I'm almost embarrassed. I should've seen that coming. But... I have to wonder..."));
        parser.printDialogueLine(new PrincessDialogueLine("Do you *actually* believe this was enough to kill me?"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "It's like she's convinced she can't die."));
        parser.printDialogueLine(new VoiceDialogueLine("Yes. Even as she lays there dying, she entirely {i}believes{/i} herself to be alive and well."));
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
                    parser.printDialogueLine(new VoiceDialogueLine("Yes, exactly. It's over."));
                    return this.slaySuccess(askPrize);
                
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
                    this.repeatActiveMenu = false;

                    parser.printDialogueLine(new VoiceDialogueLine("You lean down and wrap your hand around the blade's hilt."));
                    parser.printDialogueLine(new VoiceDialogueLine("But as you begin to slide it out of its resting place, you feel a sharp and sudden jab in your side."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "What was *that?*"));
                    parser.printDialogueLine(new PrincessDialogueLine("I guess I won't be dying alone after all..."));
                    break;

                case "pulse":
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
                    //dialogue
                    return this.slaySuccess(askPrize);

                case "cSlayPrincessNoBladeFail":
                    parser.printDialogueLine(new VoiceDialogueLine("Are you even listening to me? She's already dead."));
                    break;

                case "cGoFail":
                case "cEnterFail":
                case "cLeaveFail":
                case "cApproachFail":
                case "cSlaySelfNoBladeFail":
                case "cDropNoBladeFail":
                case "cThrowNoBladeFail":
                case "cFail":
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
    private ChapterEnding slaySuccess(boolean askPrize) {
        // good ending or Spectre
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

                case "cGoFail":
                case "cEnterFail":
                case "cLeaveFail":
                case "cApproachFail":
                case "cSlaySelfNoBladeFail":
                case "cTakeFail":
                case "cDropNoBladeFail":
                case "cThrowNoBladeFail":
                case "cFail":
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
        activeMenu.add(new Option(this.manager, "notHappyB", "Hmm, okay, maybe I'm not happy. And I'm not just saying that because you're the last person I talked to.", this.goodEndingAttempted));
        activeMenu.add(new Option(this.manager, "sure", this.goodEndingAttempted, "No, we're happy. I'm sure of it.", activeMenu.get("explore")));
        activeMenu.add(new Option(this.manager, "hellNo", "Hell no, do you have any idea how to get us the heck out of here?", !this.goodEndingAttempted));
        activeMenu.add(new Option(this.manager, "ofCourse", this.goodEndingAttempted, "Of course we are. I like it here."));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "explore":
                    activeMenu.setCondition("notHappyB", false);
                    parser.printDialogueLine(new VoiceDialogueLine("Are we really happy, or is He just telling us we are?"));
                    break;

                case "notHappyA":
                case "notHappyB":
                    parser.printDialogueLine(new VoiceDialogueLine("Good, because I have an idea to get us out of here. Though you're probably not going to like it."));
                    break;
                
                case "hellNo":
                    parser.printDialogueLine(new VoiceDialogueLine("I do, but you're probably not going to like it."));
                    break;

                case "sure":
                case "ofCourse":
                    if (this.goodEnding()) return ChapterEnding.GOODENDING;
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

        // Not happy continues here
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "The blade. We can use the blade to get out of this."));
        parser.printDialogueLine(new VoiceDialogueLine("I can hear everything you say, little voice. There's only one thing it would want you to use that blade on, and I'm afraid that thing is *you,* dear hero."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "He's right. It's the only way out."));
        parser.printDialogueLine(new VoiceDialogueLine("Do you hear that? It wants to take this happiness away from you. It wants this wonderful place to *end.*"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Do you not? There's more for us to do, and the only way for us to do it is to take that blade and use it."));
        parser.printDialogueLine(new VoiceDialogueLine("Don't you *dare.*"));





        
        parser.printDialogueLine(new VoiceDialogueLine("XXXXX"));
        parser.printDialogueLine(new PrincessDialogueLine("XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "(Explore) XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "\"XXXXX\""));


        // PLACEHOLDER
        return null;
    }

    /**
     * The player attempts to accept their reward for slaying the Princess (leads to the Good Ending)
     * @return false if the player has already claimed at least one vessel and is forced into Chapter II: The Spectre instead; true otherwise
     */
    private boolean goodEnding() {
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
            this.goodEndingAttempted = true;
            this.localGoodEndingAttempt = true;
            System.out.println();
            parser.printDialogueLine(CANTSTRAY);
            return false;
        }
    }

    /**
     * The player slays the harsh Princess after hesitating (leads to Chapter II: The Adversary / The Tower / The Nightmare)
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding slayHarshHesitated() {
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

                    this.toNightmare(true, false);
                    return ChapterEnding.TONIGHTMAREFLED;
            }
        }

        throw new RuntimeException("No ending reached");
    }

    /**
     * The player slays the harsh Princess while afraid (leads to Chapter II: The Tower)
     */
    private void slayHarshForceTower() {
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
    private ChapterEnding slayHarshForceRazor() {
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

                case "cGoStairs":
                case "cGoFail":
                case "cEnterFail":
                case "cLeaveFail":
                case "cApproachFail":
                case "cSlaySelfFail":
                case "cTakeHasBladeFail":
                case "cThrowFail":
                case "cFail":
                    this.giveDefaultFailResponse(this.activeOutcome);
            }
        }

        throw new RuntimeException("No ending reached");
    }

    /**
     * The player attempts to free the harsh Princess
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding rescueHarsh() {



        parser.printDialogueLine(new VoiceDialogueLine("XXXXX"));
        parser.printDialogueLine(new PrincessDialogueLine("XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "(Explore) XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "\"XXXXX\""));


        // PLACEHOLDER
        return null;
    }

    /**
     * The player decides to lock the Princess in the basement (leads to Chapter II: The Nightmare)
     * @param wounded whether the Princess is wounded
     * @param lostArm whether the Princess lost her arm
     */
    private void toNightmare(boolean wounded, boolean lostArm) {
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
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXX"));
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
     */
    private void chapter2Intro(boolean youDied, boolean princessDied, boolean liedTo, String chapterSpecific) {
        // shared conversation up through entering the cabin

        // chapterSpecific:
        // Tower - towerUnharmed, towerNormal
        // Razor - razorPathetic, razorMutual, razorRevival
        // Witch - witchLocked, witchNormal

        if (this.isFirstVessel) manager.setFirstPrincess(this.activeChapter);
    }

    /**
     * Runs the opening sequence of Chapter II, from the opening conversation up until the player enters the cabin
     * @param youDied whether the player died in Chapter I
     * @param princessDied whether the Princess died in Chapter I
     * @param liedTo whether the player feels like the Narrator lied to them in Chapter I
     */
    private void chapter2Intro(boolean youDied, boolean princessDied, boolean liedTo) {
        this.chapter2Intro(youDied, princessDied, liedTo, "");
    }


    // - Chapter II: The Adversary -

    /**
     * Runs the opening sequence of The Adversary
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding adversary() {
        this.chapter2Intro(true, true, false);

        // PLACEHOLDER
        return null;
    }


    // - Chapter III: The Eye of the Needle -

    /**
     * Runs the opening sequence of The Eye of the Needle
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding eyeOfNeedle() {
        // PLACEHOLDER
        return null;
    }


    // - Chapter III: The Fury -

    /**
     * Runs the opening sequence of The Fury
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding fury() {
        switch (this.prevEnding) {
            case STRIKEMEDOWN: this.jointSource = "pacifism";
            case HEARNOBELL: this.jointSource = "unarmed";
            case DEADISDEAD: this.jointSource = "pathetic";
            default: this.jointSource = "tower";
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
        boolean unharmed = this.prevEnding == ChapterEnding.TOTOWERUNHARMED;

        if (unharmed) this.chapter2Intro(true, false, false, "towerUnharmed");
        else this.chapter2Intro(true, false, false, "towerNormal");
        
        // PLACEHOLDER
        return null;
    }


    // - Chapter III: The Apotheosis -

    /**
     * Runs the opening sequence of The Apotheosis
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding apotheosis() {
        // PLACEHOLDER
        return null;
    }


    // - Chapter II: The Spectre -

    /**
     * Runs the opening sequence of The Spectre
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding spectre() {
        this.isHarsh = false;

        this.chapter2Intro(false, true, true);

        // PLACEHOLDER
        return null;
    }


    // - Chapter III: The Princess and the Dragon -

    /**
     * Runs the opening sequence of The Princess and the Dragon
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding princessAndDragon() {
        // PLACEHOLDER
        return null;
    }


    // - Chapter III: The Wraith -

    /**
     * Runs the opening sequence of The Wraith
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding wraith() {
        switch (this.prevEnding) {
            case HEARTRIPPER: this.jointSource = "spectre";
            case HEARTRIPPERLEAVE: this.jointSource = "spectre";
            default: this.jointSource = "nightmare";
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
        boolean ch1fled = this.prevEnding == ChapterEnding.TONIGHTMAREFLED || this.prevEnding == ChapterEnding.TONIGHTMAREFLED;

        this.chapter2Intro(true, false, false);
        
        // PLACEHOLDER
        return null;
    }


    // - Chapter ???: The Moment of Clarity -

    /**
     * Runs the opening sequence of The Moment of Clarity
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding momentOfClarity() {
        // PLACEHOLDER
        return null;
    }


    // - Chapter II: The Razor -

    /**
     * Runs the opening sequence of The Razor
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding razor() {
        String source = null;
        switch (this.prevEnding) {
            case TORAZOR:
                source = "pathetic";
                this.chapter2Intro(true, false, false, "razorPathetic");
                break;

            case TORAZORMUTUAL:
                source = "mutual";
                this.chapter2Intro(true, true, false, "razorMutual");
                break;

            case TORAZORREVIVAL:
                source = "revival";
                this.chapter2Intro(true, true, false, "razorRevival");
                break;
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
        // PLACEHOLDER
        return null;
    }


    // - Chapter IV: Mutually Assured Destruction -

    /**
     * Runs the opening sequence of Mutually Assured Destruction
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding mutuallyAssuredDestruction() {
        // PLACEHOLDER
        return null;
    }


    // - Chapter III: No Way Out -

    /**
     * Runs the opening sequence of No Way Out
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding noWayOut() {
        // PLACEHOLDER
        return null;
    }


    // - Chapter IV: The Empty Cup -

    /**
     * Runs the opening sequence of The Empty Cup
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding emptyCup() {
        // PLACEHOLDER
        return null;
    }


    // - Chapter II: The Beast -

    /**
     * Runs the opening sequence of The Beast
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding beast() {
        this.chapter2Intro(true, false, false);
        
        // PLACEHOLDER
        return null;
    }


    // - Chapter III: The Den -

    /**
     * Runs the opening sequence of The Den
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding den() {
        // PLACEHOLDER
        return null;
    }


    // - Chapter III: The Wild -

    /**
     * Runs the opening sequence of The Wild
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding wild() {
        if (this.hasVoice(Voice.HUNTED)) this.jointSource = "beast";
        else this.jointSource = "witch";

        // PLACEHOLDER
        return null;
    }


    // - Chapter II: The Witch -

    /**
     * Runs the opening sequence of The Witch
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding witch() {
        boolean locked = this.prevEnding == ChapterEnding.TOWITCHLOCKED;

        if (locked) this.chapter2Intro(false, false, false, "witchNormal");
        else this.chapter2Intro(true, false, false, "witchLocked");
        
        // PLACEHOLDER
        return null;
    }


    // - Chapter III: The Thorn -

    /**
     * Runs the opening sequence of The Thorn
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding thorn() {
        // PLACEHOLDER
        return null;
    }


    // - Chapter II: The Stranger -

    /**
     * Runs the opening sequence of The Stranger
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding stranger() {
        this.chapter2Intro(false, false, true);
        
        // PLACEHOLDER
        return null;
    }


    // - Chapter II: The Prisoner -

    /**
     * Runs the opening sequence of The Prisoner
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding prisoner() {
        this.chapter2Intro(true, false, true);
        
        // PLACEHOLDER
        return null;
    }


    // - Chapter III: The Cage -

    /**
     * Runs the opening sequence of The Cage
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding cage() {
        // PLACEHOLDER
        return null;
    }


    // - Chapter III: The Grey -

    /**
     * Runs the opening sequence of The Grey (coming from the Prisoner)
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding greyDrowned() {
        // PLACEHOLDER
        return null;
    }

    /**
     * Runs the opening sequence of The Grey (coming from the Damsel)
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding greyBurned() {
        // PLACEHOLDER
        return null;
    }


    // - Chapter II: The Damsel -

    /**
     * Runs the opening sequence of The Damsel
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding damsel() {
        this.chapter2Intro(true, false, true);
        
        // PLACEHOLDER
        return null;
    }


    // - Epilogue: Happily Ever After -

    /**
     * Runs the opening sequence of Happily Ever After
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding happilyEverAfter() {
        // PLACEHOLDER
        return null;
    }



    /**
     * The player attempts to abort the current vessel
     * @return true if the player commits to aborting the current vessel; false otherwise
     */
    private boolean attemptAbortVessel() {
        // true if attempt goes through, false if player returns
        
        // PLACEHOLDER
        return false;
    }



    // - The Mirror -

    /**
     * Runs the mirror sequence after claiming a vessel
     */
    private void mirrorSequence() {
        this.cancelTimer();
        this.hasBlade = false;

        this.threwBlade = false;
        this.mentionedLooping = false;
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
