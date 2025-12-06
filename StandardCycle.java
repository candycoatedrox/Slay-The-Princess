import java.util.ArrayList;
import java.util.HashMap;

public class StandardCycle extends Cycle {

    // One CYCLE = from beginning of Ch1 to the end of Shifting Mound interlude

    private final boolean isFirstVessel;
    private final ArrayList<Voice> voicesMet;
    private final ArrayList<Chapter> route;
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
    private boolean mirrorComment = false; // Used in all Chapter 2s and 3s
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
     * Attempts to let the player enter a given location or the nearest appropriate location
     * @param argument the location to enter (should be "cabin", "basement", or an empty String)
     * @return "cFail" if argument is invalid; "cGo[Location]" if there is a valid location the player can enter; "cEnterFail" otherwise
     */
    @Override
    public String enter(String argument) {
        String outcome = super.enter(argument);
        if (this.activeChapter == Chapter.SPACESBETWEEN && outcome.equals("GoCabin")) return "EnterFail";
        return outcome;
    }

    /**
     * Attempts to let the player slay either the Princess or themselves
     * @param argument the target to slay
     * @param secondPrompt whether the player has already been given a chance to re-enter a valid argument
     * @return "cFail" if argument is invalid; "cSlayNoPrincessFail" if attempting to slay the Princess when she is not present; "cSlayPrincessNoBladeFail" if attempting to slay the Princess without the blade; "cSlayPrincessFail" if the player cannot slay the Princess  right now; "cSlayPrincess" if otherwise attempting to slay the Princess; "cSlaySelfNoBladeFail" if attempting to slay themselves without the blade; "cSlaySelfFail" if the player cannot slay themselves right now; "cSlaySelf" if otherwise attempting to slay themselves
     */
    @Override
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
        if (!this.hasVoice(Voice.NARRATOR)) {
            super.giveDefaultFailResponse(outcome);
            return;
        }

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
                
            case "cApproachMirrorFail":
                parser.printDialogueLine(new VoiceDialogueLine("What are you talking about? There isn't a mirror."));
                if ((this.mirrorComment || this.touchedMirror) && this.hasVoice(Voice.HERO)) parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "He's actually right this time. The mirror really isn't here."));
                break;

            case "cApproachHerFail":
                parser.printDialogueLine(new VoiceDialogueLine("...What?"));
                break;
                

            case "cSlayNoPrincessFail":
                parser.printDialogueLine(new VoiceDialogueLine("As much as I appreciate your enthusiasm, the Princess isn't here right now. Save it for when you reach the basement."));
                break;

            case "cSlayPrincessNoBladeFail":
                parser.printDialogueLine(new VoiceDialogueLine("*Sigh.* Unfortunately, you have no weapon with which to slay her. If only you had the blade, this would be so much easier."));
                break;

            case "cSlayPrincessFail":
                // The Narrator doesn't have a line here because there is no universe in which he would ever say no to you trying to slay the Princess if you have the opportunity.
                // Unfortunately for him, sometimes I can't let you slay her in the middle of certain menus. Too bad, Narrator.
                super.giveDefaultFailResponse(outcome);
                break;

            case "cSlaySelfNoBladeFail":
                parser.printDialogueLine(new VoiceDialogueLine("Why on earth would you even consider that?!"));

                if (this.knowsBlade) {
                    parser.printDialogueLine(new VoiceDialogueLine("*Sigh.* *Conveniently,* you don't have any sort of weapon with which to kill yourself, so you *can't.* Just get the idea out of your head, alright?"));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine("*Sigh.* *Conveniently,* you don't have the blade, so you *can't.* Just get the idea out of your head, alright?"));
                }

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

            case "cTake":
                super.giveDefaultFailResponse(outcome);

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
        Voice newVoice;

        while (!this.prevEnding.isFinal()) {
            nextChapter = this.prevEnding.getNextChapter();
            this.route.add(nextChapter);

            if (this.prevEnding != ChapterEnding.NEWCYCLE) {
                newVoice = this.prevEnding.getNewVoice();

                switch (nextChapter.getNumber()) {
                    case 2:
                        this.ch2Voice = newVoice;
                        break;
                    case 3:
                        this.ch3Voice = newVoice;
                }

                if (nextChapter == Chapter.CLARITY) {
                    for (Voice v : Voice.values()) {
                        if (v != Voice.PRINCESS) {
                            this.addVoice(v);
                        }
                    }
                } else if (newVoice != null) {
                    this.addVoice(newVoice);
                }
                
                switch (nextChapter) {
                    case CAGE:
                        this.hasBlade = true;
                        this.withPrincess = false;
                        this.knowsBlade = true;
                        this.currentLocation = GameLocation.PATH;
                        break;
                    case ARMSRACE:
                        this.hasBlade = false;
                        this.withPrincess = false;
                        this.knowsBlade = true;
                        this.currentLocation = GameLocation.CABIN;
                        this.mirrorPresent = true;
                        this.addVoice(Voice.HUNTED);
                        break;
                    case NOWAYOUT:
                        this.hasBlade = false;
                        this.withPrincess = false;
                        this.knowsBlade = true;
                        this.currentLocation = GameLocation.CABIN;
                        this.mirrorPresent = true;
                        this.addVoice(Voice.CONTRARIAN);
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
                        this.currentVoices.put(Voice.SMITTEN, false);
                        break;
                    case DRAGON:
                        this.hasBlade = false;
                        this.withPrincess = true;
                        this.knowsBlade = false;
                        this.currentLocation = GameLocation.BASEMENT;
                        this.clearVoices();
                        this.addVoice(Voice.PRINCESS);
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
        this.mainScript = new Script(this.manager, this.parser, c.getScriptFile());
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
            case ARMSRACE: return this.razor3Intro(true);
            case NOWAYOUT: return this.razor3Intro(false);
            case DEN: return this.den();
            case WILD: return this.wild();
            case THORN: return this.thorn();
            case CAGE: return this.cage();
            case GREY:
                if (this.prevEnding == ChapterEnding.LADYKILLER) return this.greyBurned();
                else return this.greyDrowned();
            case HAPPY: return this.happilyEverAfter();

            case MUTUALLYASSURED: return this.razor4(true);
            case EMPTYCUP: return this.razor4(false);
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
                parser.printDialogueLine("Chapter III", true);
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

        mainScript.runSection();

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
                    mainScript.runSection("question1");
                    break;
                case "question2":
                    activeMenu.setCondition("whyDanger", false);
                    mainScript.runSection("question2");
                    break;
                case "whyDanger":
                    activeMenu.setCondition("question2", false);
                    mainScript.runSection("whyDanger");
                    break;
                case "someoneElse":
                    if (activeMenu.hasBeenPicked("question2") || activeMenu.hasBeenPicked("whyDanger")) {
                        mainScript.runSection("someoneElseA");
                    } else {
                        mainScript.runSection("someoneElseB");
                    }
                    break;
                case "refuse":
                    activeMenu.setCondition("reluctant", true);
                    mainScript.runSection("refuse");
                    break;
                case "prize":
                    askPrize = true;
                    mainScript.runSection("prize");
                    break;
                case "whatHappens":
                case "evidence":
                case "chickenEgg":
                case "conscience":
                case "letItBurn":
                case "prize2":
                    mainScript.runSection(activeOutcome);
                    break;
                    
                case "reluctant":
                case "okFine":
                case "sold":
                case "thanks":
                case "sweet":
                    mainScript.runSection(activeOutcome);
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
            mainScript.runSection("hillDialogue");
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
        mainScript.runSection();

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
                    activeMenu.setGreyedOut("enter", false);

                    mainScript.runSection();
                    break;
                
                case "cGoStairs":
                    if (!this.isHarsh && !canSoft) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                case "enter":
                    this.repeatActiveMenu = false;
                    this.withBlade = false;
                    manager.addToPlaylist("The World Ender");
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
        this.secondaryScript = new Script(this.manager, this.parser, "Chapter 1/StrangerAttempt");
        secondaryScript.runSection();

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
                case "maybe":
                    secondaryScript.runSection(outcome);
                    return 1;

                case "lie":
                    repeatMenu = false;
                    secondaryScript.runSection("lie");
                    break;
                case "nihilistA":
                case "nihilistB":
                    repeatMenu = false;
                    secondaryScript.runSection("nihilist");
                    break;
                case "cGoLeave":
                case "nope":
                case "notGoing":
                case "quiet":
                    repeatMenu = false;
                    secondaryScript.runSection("fine");
                    break;

                default:
                    this.giveDefaultFailResponse(outcome);
            }
        }
        
        this.currentLocation = GameLocation.HILL;

        leaveMenu = new OptionsMenu();
        leaveMenu.add(new Option(this.manager, "cabin", !canCabin, "Okay, okay! I'm going into the cabin. Sheesh."));
        leaveMenu.add(new Option(this.manager, "commit", "[Turn around (again) and leave (again).]"));

        repeatMenu = true;
        while (repeatMenu) {
            outcome = parser.promptOptionsMenu(leaveMenu);
            switch (outcome) {
                case "cGoCabin":
                    secondaryScript.runSection("cabinSilent");
                    return 2;
                case "cabin":
                    secondaryScript.runSection("cabin");
                    return 2;

                case "cGoPath":
                case "commit":
                    repeatMenu = false;
                    break;

                default:
                    this.giveDefaultFailResponse(outcome);
            }
        }

        secondaryScript.runSection("commit");

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
                case "good":
                case "blackmail":
                case "cGoPath":
                case "quiet":
                    repeatMenu = false;
                    secondaryScript.runSection(outcome);
                    break;

                case "cGoCabin":
                    secondaryScript.runSection("cabin2");
                    return 2;

                default:
                    this.giveDefaultFailResponse(outcome);
            }
        }

        this.currentLocation = GameLocation.LEAVING;

        leaveMenu = new OptionsMenu();
        leaveMenu.add(new Option(this.manager, "cabin", !canCabin, "There's no fighting this, is there? I have to go into the cabin, don't I? Fine."));
        leaveMenu.add(new Option(this.manager, "commit", "Oh, yeah? Well I guess I start walking in a different direction. Again. In fact, I'm going to just keep trekking through the wilderness until I find a way out of this place.", 0));

        repeatMenu = true;
        while (repeatMenu) {
            outcome = parser.promptOptionsMenu(leaveMenu);
            switch (outcome) {
                case "cGoHill":
                case "cabin":
                    secondaryScript.runSection("cabin3");
                    return 2;
                case "cGoLeave":
                case "commit":
                    if (manager.confirmContentWarnings(Chapter.STRANGER)) repeatMenu = false;
                    break;

                default:
                    this.giveDefaultFailResponse(outcome);
            }
        }

        secondaryScript.runSection("strangerCommit");
        return 0;
    }

    /**
     * Runs the beginning of the basement sequence with the soft princess (did not take the blade)
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding ch1BasementSoft() {
        this.secondaryScript = new Script(this.manager, this.parser, "Chapter 1/Basement1Soft");

        boolean canDamsel = !manager.hasVisited(Chapter.DAMSEL);
        boolean canBeast = !manager.hasVisited(Chapter.BEAST);
        boolean canWitch = !manager.hasVisited(Chapter.WITCH);
        boolean canNightmare = !manager.hasVisited(Chapter.NIGHTMARE);

        boolean canFree = canDamsel || canWitch;
        boolean canNotFree = canBeast || canWitch || canNightmare;
        boolean canGetBlade = canBeast || canWitch;

        this.currentLocation = GameLocation.STAIRS;
        this.withPrincess = true;
        mainScript.runSection("stairs");
        secondaryScript.runSection();

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
                case "checkIn":
                    this.repeatActiveMenu = false;
                    secondaryScript.runSection(activeOutcome);
                    break;
                case "lieSave":
                    lieSave = true;
                case "hereToSave":
                    this.repeatActiveMenu = false;
                    hereToSave = true;
                    secondaryScript.runSection(activeOutcome);
                    break;
                case "jokeKill":
                    this.repeatActiveMenu = false;
                    jokeKill = true;
                    secondaryScript.runSection("jokeKill");
                    break;
                case "cGoBasement":
                case "silent":
                    this.repeatActiveMenu = false;
                    secondaryScript.runSection("silentStairs");
                    break;
                
                case "cGoCabin":
                    mainScript.runSection("stairsLeaveFail");
                    break;

                default:
                    this.giveDefaultFailResponse(this.activeOutcome);
            }
        }

        this.currentLocation = GameLocation.BASEMENT;

        if (jokeKill) {
            secondaryScript.runSection("jokeKillBasement");
        } else if (lieSave) {
            secondaryScript.runSection("lieSaveBasement");
        } else if (hereToSave) {
            secondaryScript.runSection("hereToSaveBasement");
        } else {
            secondaryScript.runSection("genericBasement");
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
                    break;
                case "free":
                    if (!manager.confirmContentWarnings("self-mutilation", true)) break;

                    return this.ch1RescueSoft(false, hereToSave && !lieSave, false, false, canFree, canNotFree);
                
                case "cGoStairs":
                    mainScript.runSection("basementLeaveFail");
                    break;

                default:
                    this.giveDefaultFailResponse(this.activeOutcome);
            }
        }

        OptionsMenu subMenu;
        String outcome = "";
        boolean repeatSub;

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
                case "name2":
                case "whyImprisoned":
                case "eat":
                    secondaryScript.runSection(activeOutcome);
                    break;
                    
                case "notKidding":
                    if (this.ch1ShareTaskSoft(false, canFree)) {
                        return this.ch1RescueSoft(true, false, false, true, canFree, canNotFree);
                    } else {
                        if (this.whatWouldYouDo) activeMenu.setCondition("whatWouldYouDo", false);
                    }

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
                            secondaryScript.runSection("shareNotRight");

                            if (this.ch1ShareTaskSoft(true, canFree)) {
                                return this.ch1RescueSoft(true, hereToSave && !lieSave, false, true, canFree, canNotFree);
                            } else {
                                if (this.whatWouldYouDo) activeMenu.setCondition("whatWouldYouDo", false);
                            }
                    }

                    break;

                case "whatWouldYouDo":
                    this.whatWouldYouDo = true;
                    secondaryScript.runSection("whatWouldYouDo");
                    secondaryScript.runSection("whatDoA");

                case "compromiseA":
                case "compromiseB":
                    this.repeatActiveMenu = false;
                    if (jokeKill && !this.knowsDestiny) {
                        secondaryScript.runSection("compromiseA");
                    } else {
                        secondaryScript.runSection("compromiseB");
                    }

                    subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "getBlade", !canGetBlade, "[Retrieve the blade.]", 0));
                    subMenu.add(new Option(this.manager, "free", !canFree, "\"Okay. Let's get you out of here.\" [Examine the chains.]", 0));
                    subMenu.add(new Option(this.manager, "lock", "[Lock her in the basement.]", Chapter.NIGHTMARE));
                    
                    repeatSub = true;
                    while (repeatSub) {
                        switch (parser.promptOptionsMenu(subMenu)) {
                            case "getBlade":
                                if (!manager.confirmContentWarnings("mutilation", true)) break;

                                secondaryScript.runSection("retrieveFromLockA");
                                return this.ch1RetrieveBlade(false);

                            case "free":
                                if (!manager.confirmContentWarnings("self-mutilation", true)) break;

                                secondaryScript.runSection("rescueFromLock");

                                return this.ch1RescueSoft(true, hereToSave && !lieSave, false, true, canFree, canNotFree);

                            case "lock":
                                if (manager.confirmContentWarnings(Chapter.NIGHTMARE)) repeatSub = false;
                                break;
                        }
                    }

                    // Lock continues here
                    secondaryScript.runSection();

                    subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "lock", "No, we're sticking to the plan and locking her away."));
                    subMenu.add(new Option(this.manager, "slay", !canGetBlade, "Oh that's a relief! I was afraid I'd already committed to not slaying her.", 0));

                    repeatSub = true;
                    while (repeatSub) {
                        switch (parser.promptOptionsMenu(subMenu)) {
                            case "lock":
                                secondaryScript.runSection();
                                this.ch1ToNightmare(false, false);
                                return ChapterEnding.TONIGHTMARE;

                            case "slay":
                                if (!manager.confirmContentWarnings("mutilation", true)) break;

                                secondaryScript.runSection("retrieveFromLockB");
                                return this.ch1RetrieveBlade(false);
                        }
                    }
                    
                
                case "getBladeSorry":
                    if (!manager.confirmContentWarnings("mutilation", true)) break;

                    this.repeatActiveMenu = false;

                    if (jokeKill && !this.knowsDestiny) {
                        secondaryScript.runSection("retrieveA");
                    } else if (!this.knowsDestiny) {
                        secondaryScript.runSection("retrieveB");
                    }

                    return this.ch1RetrieveBlade(true);
                
                case "cGoStairs":
                    secondaryScript.runSection("attemptLeave");

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

                                secondaryScript.runSection();

                                subMenu = new OptionsMenu(true);
                                subMenu.add(new Option(this.manager, "lock", "No, we're sticking to the plan and locking her away."));
                                subMenu.add(new Option(this.manager, "slay", !canGetBlade, "Oh that's a relief! I was afraid I'd already committed to not slaying her.", 0));

                                repeatSub = true;
                                while (repeatSub) {
                                    switch (parser.promptOptionsMenu(subMenu)) {
                                        case "lock":
                                            secondaryScript.runSection();

                                            System.out.println();
                                            this.ch1ToNightmare(false, false);
                                            return ChapterEnding.TONIGHTMARE;

                                        case "slay":
                                            if (!manager.confirmContentWarnings("mutilation", true)) break;

                                            secondaryScript.runSection("retrieveFromLockB");

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

                    secondaryScript.runSection("retrieveSilent");
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
                    secondaryScript.runSection("whatWouldYouDo");
                    secondaryScript.runSection("whatDoB");
                    break;
                case "youTell":
                    repeatMenu = false;
                    secondaryScript.runSection("shareYouTell");
                    break;
                case "reasons":
                    repeatMenu = false;
                    secondaryScript.runSection("shareReasons");
                    break;
                case "noDanger":
                    repeatMenu = false;
                    secondaryScript.runSection("shareNoDanger");

                    OptionsMenu finalShareMenu = new OptionsMenu(true);
                    finalShareMenu.add(new Option(this.manager, "talk", "\"I still have a few more questions before we leave.\""));
                    finalShareMenu.add(new Option(this.manager, "free", !canFree, "\"I'll see what I can do.\" [Examine the chains.]", 0));

                    repeatMenu = true;
                    while (repeatMenu) {
                       switch (parser.promptOptionsMenu(finalShareMenu)) {
                            case "talk":
                                secondaryScript.runSection();
                                return false;
                            case "free":
                                if (!manager.confirmContentWarnings("self-mutilation", true)) break;
                                return true;
                        } 
                    }

                case "enough":
                    repeatMenu = false;
                    secondaryScript.runSection("shareEnough");
                    break;
                case "silent":
                    repeatMenu = false;
                    secondaryScript.runSection("shareSilent");
                    break;
            }
        }

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
        if (lateJoin) {
            secondaryScript.runSection("rescueLateJoin");
        } else {
            if (dontRegret) secondaryScript.runSection("rescueDontRegret");
            else secondaryScript.runSection("rescue");
        }

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "whatIf", "\"And if there isn't a key... do you have any other ideas?\""));
        activeMenu.add(new Option(this.manager, "check", "\"I'm going to check upstairs. Maybe the key's still lying around somewhere up there. And if not, maybe I can at least find something to break you free.\""));

        secondaryScript.runSection(parser.promptOptionsMenu(activeMenu));
        
        this.currentLocation = GameLocation.STAIRS;
        this.reverseDirection = true;
        this.withPrincess = false;

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
                    secondaryScript.runSection("rescueStairsShout");
                    break;

                case "cGoCabin":
                    activeMenu.setCondition("try", false);
                case "try":
                    if (triedDoor) {
                        secondaryScript.runSection("rescueStairsTryAgain");
                    } else {
                        secondaryScript.runSection("rescueStairsTryFirst");
                    }
                    triedDoor = true;
                    break;

                case "cGoBasement":
                case "return":
                    this.repeatActiveMenu = false;
                    break;
                
                case "cSlayNoPrincessFail":
                    mainScript.runSection("rescueStairsSlayFail");
                    break;

                default:
                    this.giveDefaultFailResponse(activeOutcome);
            }
        }

        this.currentLocation = GameLocation.BASEMENT;
        this.reverseDirection = false;
        this.hasBlade = true;
        this.withPrincess = true;
        this.canSlayPrincess = true;

        secondaryScript.runSection("rescueReturn");

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "save", !canFree, "[Save the Princess.]", 0));
        activeMenu.add(new Option(this.manager, "slay", !canNotFree, "[Slay the Princess.]", !hereToSaveTruth));

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
        secondaryScript.runSection("rescueSave");

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

        secondaryScript.runSection();

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
                    
                    secondaryScript.runSection("rescueControlledSlay1");
                    return ChapterEnding.TOWITCH;

                case "warn":
                    this.repeatActiveMenu = false;
                    break;

                default:
                    this.giveDefaultFailResponse();
            }
        }

        secondaryScript.runSection();
        
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

                    secondaryScript.runSection("rescueControlledSlay2");
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
        secondaryScript.runSection();
        return ChapterEnding.TODAMSEL;
    }

    /**
     * The player decides to slay the soft Princess when the blade falls into the basement after initially deciding to free her (leads to Chapter II: The Beast / The Witch)
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding ch1RescueSlaySoft() {
        secondaryScript.runSection("rescueSlay");

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

        secondaryScript.runSection();

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
                    secondaryScript.runSection("rescueSlayGiveUp");

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
                    secondaryScript.runSection("rescueSlayFinish");

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
                    secondaryScript.runSection("rescueSlayLock");

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

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "hello", "(Explore) \"Hello?\"", 0, Chapter.BEAST));
        activeMenu.add(new Option(this.manager, "wrongFoot", "(Explore) \"I think we got off on the wrong foot. Do you think we can start over?\"", 0, Chapter.BEAST));
        activeMenu.add(new Option(this.manager, "lock", "She's lost an arm. I'm locking her down there and letting her bleed out.", 0, Chapter.NIGHTMARE));
        activeMenu.add(new Option(this.manager, "finish", "Let's finish this."));

        OptionsMenu subMenu;
        String outcome = "";
        boolean repeatSub;

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "hello":
                    if (!manager.confirmContentWarnings(Chapter.BEAST)) break;

                    activeMenu.setCondition("wrongFoot", false);
                    canWitch = false;
                    secondaryScript.runSection("retrieveExploreJoin");
                    break;

                case "wrongFoot":
                    if (!manager.confirmContentWarnings(Chapter.BEAST)) break;

                    activeMenu.setCondition("hello", false);
                    canWitch = false;
                    secondaryScript.runSection("retrieveWrongFoot");
                    break;

                case "cGoCabin":
                    secondaryScript.runSection("retrieveSilentLock");

                    subMenu = new OptionsMenu();
                    subMenu.add(new Option(this.manager, "lock", "Tell you what. I'll even stay here for a while to make sure she's dead. [Lock her away.]", Chapter.NIGHTMARE));
                    subMenu.add(new Option(this.manager, "finish", !manager.hasVisited(Chapter.WITCH) || (!manager.hasVisited(Chapter.BEAST) && !canWitch), "You're right. Let's finish this."));

                    outcome = "";
                    repeatSub = true;
                    while (repeatSub) {
                        outcome = parser.promptOptionsMenu(subMenu);
                        switch (outcome) {
                            case "cGoCabin":
                            case "lock":
                                if (!manager.confirmContentWarnings(Chapter.NIGHTMARE)) break;
                                
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
                    
                    break;

                case "lock":
                    secondaryScript.runSection("retrieveLock");

                    subMenu = new OptionsMenu();
                    subMenu.add(new Option(this.manager, "lock", "Tell you what. I'll even stay here for a while to make sure she's dead. [Lock her away.]", Chapter.NIGHTMARE));
                    subMenu.add(new Option(this.manager, "finish", !manager.hasVisited(Chapter.WITCH) || (!manager.hasVisited(Chapter.BEAST) && !canWitch), "You're right. Let's finish this."));

                    outcome = "";
                    repeatSub = true;
                    while (repeatSub) {
                        outcome = parser.promptOptionsMenu(subMenu);
                        switch (outcome) {
                            case "cGoCabin":
                            case "lock":
                                if (!manager.confirmContentWarnings(Chapter.NIGHTMARE)) break;

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

        secondaryScript.runSection("retrieveFinish");

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

                    secondaryScript.runSection("locked");
                    if (worthRisk) secondaryScript.runSection("worthRisk");
                    else secondaryScript.runSection("lockedCont");
                    
                    return ChapterEnding.TOWITCHLOCKED;
                
                case "close":
                    activeMenu.setCondition("armOpen", false);
                    secondaryScript.runSection("retrieveClose");
                    break;
                
                case "armClosed":
                    this.repeatActiveMenu = false;
                    break;
                
                case "comeOut":
                    if (!manager.confirmContentWarnings(Chapter.BEAST)) break;

                    secondaryScript.runSection("retrieveComeOut");

                    subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "wait", "[Wait.]"));
                    subMenu.add(new Option(this.manager, "shadows", "[Venture into the shadows.]"));

                    switch (parser.promptOptionsMenu(subMenu)) {
                        case "wait":
                            this.ch1BladeBeastWaiting();
                            break;

                        case "shadows":
                            secondaryScript.runSection("retrieveShadows");
                            break;
                    }
                    
                    return ChapterEnding.TOBEAST;

                case "wait":
                    if (!manager.confirmContentWarnings(Chapter.BEAST)) break;

                    secondaryScript.runSection("retrieveWait");
                    this.ch1BladeBeastWaiting();
                    return ChapterEnding.TOBEAST;
            }
        }
        
        // Investigate arm continues here
        secondaryScript.runSection("investigate");
        
        if (!canWitch) {
            secondaryScript.runSection("investigateHestitate");
            return ChapterEnding.TOBEAST;
        }
        
        secondaryScript.runSection("investigateCont");

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "giveUp", "[Give up.]", 0, Chapter.BEAST));
        activeMenu.add(new Option(this.manager, "fight", "[Fight back.]", 0, Chapter.WITCH));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(this.activeMenu)) {
                case "giveUp":
                    if (!manager.confirmContentWarnings(Chapter.BEAST)) break;

                    secondaryScript.runSection("investigateGiveUp");
                    return ChapterEnding.TOBEAST;
                
                case "fight":
                    if (!manager.confirmContentWarnings(Chapter.WITCH)) break;

                    secondaryScript.runSection("investigateFight");
                    return ChapterEnding.TOWITCH;
            }
        }
        
        throw new RuntimeException("No ending reached");
    }

    /**
     * The player attempts to outwait the Princess after retrieving the blade (leads to Chapter II: the Beast)
     */
    private void ch1BladeBeastWaiting() {
        secondaryScript.runSection("retrieveWaitJoin");

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "wait", "[Keep waiting.]"));
        activeMenu.add(new Option(this.manager, "shadows", "[Venture into the shadows.]"));

        switch (parser.promptOptionsMenu(activeMenu)) {
            case "wait":
                break;

            case "shadows":
                secondaryScript.runSection("retrieveShadows");
                return;
        }

        secondaryScript.runSection();
    }


    /**
     * Runs the beginning of the basement sequence with the harsh princess (took the blade)
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding ch1BasementHarsh(boolean askPrize) {
        this.secondaryScript = new Script(this.manager, this.parser, "Chapter 1/Basement1Harsh");

        boolean canNightmare = !manager.hasVisited(Chapter.NIGHTMARE);

        boolean canHesitateSlay = !manager.hasVisitedAll(Chapter.ADVERSARY, Chapter.TOWER, Chapter.NIGHTMARE);
        boolean mustSpectre = !canHesitateSlay && manager.hasVisited(Chapter.PRISONER);

        this.currentLocation = GameLocation.STAIRS;
        mainScript.runSection("stairs");
        secondaryScript.runSection();

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
                case "jokeKill":
                    jokeKill = true;
                case "hi":
                case "checkIn":
                    this.repeatActiveMenu = false;
                    secondaryScript.runSection(activeOutcome);
                    break;
                case "cGoBasement":
                case "silent":
                    secondaryScript.runSection("silentStairs");
                    break;
                
                case "cGoCabin":
                    mainScript.runSection("stairsLeaveFail");
                    break;

                default:
                    this.giveDefaultFailResponse(this.activeOutcome);
            }
        }

        this.currentLocation = GameLocation.BASEMENT;
        this.withPrincess = true;

        if (jokeKill) secondaryScript.runSection("jokeKillBasement");
        else secondaryScript.runSection("genericBasement");

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
            case "undecided":
                undecided = true;
            case "noWay":
            case "caughtMe":
            case "nuhUh":
            case "talk":
                hesitated = true;
            case "noJoke":
                this.repeatActiveMenu = false;
                secondaryScript.runSection(activeOutcome);
                break;

            case "steel":
                this.canDropBlade = true;
                this.canSlayPrincess = true;
                
                return this.ch1SteelNervesHarsh(canHesitateSlay, mustSpectre, false, askPrize);
        }

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
                    if (undecided) secondaryScript.runSection("dropA");
                    else secondaryScript.runSection("dropB");

                    return this.ch1DropBladeHarsh(canHesitateSlay, false, false, false);

                case "cSlayPrincess":
                case "tighten":
                    secondaryScript.runSection("tighten");
                    
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
        
        secondaryScript.runSection("steelJoin");
        if (hesitated) secondaryScript.runSection("steelHesitated");
        else secondaryScript.runSection("steelCont");

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
                    
                    secondaryScript.runSection("steelBluff");
                    break;

                case "isArmed":
                    activeMenu.setCondition("bluff", false);
                    activeMenu.setCondition("sorry", false);
                    isArmed = true;
                    canSlay = canRazor;
                    activeMenu.setCondition("slay", canSlay);
                    
                    secondaryScript.runSection("steelIsArmed");
                    break;

                case "sorry":
                    activeMenu.setCondition("bluff", false);
                    activeMenu.setCondition("isArmed", false);
                    canSlay = canHesitateSlay;
                    activeMenu.setCondition("slay", canSlay);
                    
                    secondaryScript.runSection("steelSorry");
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
                    secondaryScript.runSection("dropA");

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
        secondaryScript.runSection("steelNoDrop");

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

                    secondaryScript.runSection("steelImpasse");
                    break;

                case "stare":
                    activeMenu.setCondition("sure", true);
                    activeMenu.setCondition("contStare", true);
                    activeMenu.setCondition("impasse", false);
                    
                    secondaryScript.runSection("steelStare");
                    break;

                case "sure":
                    activeMenu.setCondition("stare2", false);
                    activeMenu.setCondition("contStare", false);

                    secondaryScript.runSection("steelSure");
                    break;

                case "stare2":
                    activeMenu.setCondition("sure", false);

                    secondaryScript.runSection("steelStare2");
                    break;
                
                case "contStare":
                    activeMenu.setCondition("sure", false);
                    
                    secondaryScript.runSection("steelContStare");
                    break;

                case "cDrop":
                case "drop":
                    secondaryScript.runSection("dropA");

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
                    secondaryScript.runSection("awkward");

                    subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "reasons", "\"I have my reasons. Do you think I'd just come here to kill someone without even knowing why? That'd be ridiculous!\""));
                    subMenu.add(new Option(this.manager, "deflect", "\"Do you know why I'm here to kill you?\""));
                    subMenu.add(new Option(this.manager, "shareTask", !canTower, "\"You're supposed to end the world.\"", !this.knowsDestiny));
                    subMenu.add(new Option(this.manager, "notSure", "\"I've been told things, but I'm not sure what to believe.\""));

                    switch (parser.promptOptionsMenu(subMenu)) {
                        case "reasons":
                            if (steeled) secondaryScript.runSection("awkwardReasonsSteeled");
                            else secondaryScript.runSection("awkwardReasons");
                            break;

                        case "deflect":
                            if (steeled) secondaryScript.runSection("awkwardDeflectSteeled");
                            else secondaryScript.runSection("awkwardDeflect");
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
                            secondaryScript.runSection("awkwardNotSure");
                            break;
                    }

                case "relationship":
                    activeMenu.setCondition("awkward", false);
                    secondaryScript.runSection("relationship");

                    secondaryScript.runSection("relationship");
                    break;

                case "howFree":
                    howFree = true;
                    secondaryScript.runSection("howFree");
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
                case "howLong":
                    vagueCount += 1;
                    secondaryScript.runSection(activeOutcome);
                    
                    if (vagueCount == 1) {
                        secondaryScript.runSection("vague1");
                    } else if (vagueCount == 2) {
                        secondaryScript.runSection("vague2");
                    }

                    break;

                case "whyHere":
                    secondaryScript.runSection("whyHere");

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
                            secondaryScript.runSection("whyHereTold");
                            break;

                        case "lie":
                            secondaryScript.runSection("whyHereLie");
                            break;

                        case "silent":
                            secondaryScript.runSection("whyHereSilent");
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
                    secondaryScript.runSection("attemptLeave");

                    subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "lock", !canNightmare, "I don't have enough information to make a decision yet. I'm going to keep her locked away down here, at least for a little bit. We can get to know each other better while I decide what to do. [Keep her locked away.]", 0));
                    subMenu.add(new Option(this.manager, "nevermind", "You're right. I still have a few more questions for her before I make a decision. [Turn back.]"));

                    repeatSub = true;
                    while (repeatSub) {
                        outcome = parser.promptOptionsMenu(subMenu);
                        switch (outcome) {
                            case "lock":
                                if (!manager.confirmContentWarnings(Chapter.NIGHTMARE)) break;

                                secondaryScript.runSection();

                                subMenu = new OptionsMenu(true);
                                subMenu.add(new Option(this.manager, "lock", "No, we're sticking to the plan and locking her down here."));
                                subMenu.add(new Option(this.manager, "slay", !canTower, "Oh that's a relief! I was afraid I'd already committed to not slaying her.", 0));

                                while (repeatSub) {
                                    switch (parser.promptOptionsMenu(subMenu)) {
                                        case "lock":
                                            secondaryScript.runSection();

                                            System.out.println();
                                            this.ch1ToNightmare(false, false);
                                            return ChapterEnding.TONIGHTMARE;

                                        case "slay":
                                            if (!manager.confirmContentWarnings(Chapter.TOWER)) break;

                                            secondaryScript.runSection("slayFromLock");

                                            System.out.println();
                                            return this.ch1SlayHarsh(true, true, isArmed);
                                    }
                                }
                                
                                break;

                            case "nevermind":
                                repeatSub = false;
                                secondaryScript.runSection("leaveNevermind");
                                break;
                        }
                    }
                    
                    break;

                default:
                    this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // "We've talked enough" continues here
        secondaryScript.runSection("enough");

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
                    secondaryScript.runSection("enoughRescue");
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
        secondaryScript.runSection("lock");

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
                    secondaryScript.runSection("enoughRescue2");
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
        secondaryScript.runSection("lock2");

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "lock", "No, we're sticking to the plan and locking her down here."));
        activeMenu.add(new Option(this.manager, "slay", !canTower, "Oh that's a relief! I was afraid I'd already committed to not slaying her.", 0));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "lock":
                    secondaryScript.runSection();

                    System.out.println();
                    this.ch1ToNightmare(false, false);
                    return ChapterEnding.TONIGHTMARE;

                case "slay":
                    if (!manager.confirmContentWarnings(Chapter.TOWER)) break;
                    
                    secondaryScript.runSection("slayFromLock");

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

        if (steeled) {
            secondaryScript.runSection("shareTaskSteeled");
        } else {
            secondaryScript.runSection("shareTask");
        }

        OptionsMenu shareMenu = new OptionsMenu(true);
        shareMenu.add(new Option(this.manager, "deflect", "(Deflect) \"What are you going to do if I let you out of here?\""));
        shareMenu.add(new Option(this.manager, "enough", "\"I've been told enough.\""));
        shareMenu.add(new Option(this.manager, "youTell", "\"I was hoping you'd tell me.\""));
        shareMenu.add(new Option(this.manager, "reasons", "\"No. But I'm sure they have their reasons for keeping that information secret from me.\""));
        shareMenu.add(new Option(this.manager, "trustYou", "\"No. And if I'm being honest, I'm more inclined to trust you than I'm inclined to trust Them.\""));
        shareMenu.add(new Option(this.manager, "silent", "[Remain silent.]"));

        switch (parser.promptOptionsMenu(shareMenu)) {
            case "deflect":
                secondaryScript.runSection("shareDeflect");
                break;

            case "enough":
                secondaryScript.runSection("shareEnough");
                break;

            case "youTell":
                secondaryScript.runSection("shareYouTell");
                break;

            case "reasons":
                secondaryScript.runSection("shareReasons");
                break;
            
            case "trustYou":
                secondaryScript.runSection("shareTrustYou");

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
                            secondaryScript.runSection();
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
                secondaryScript.runSection("shareSilent");
                break;
        }

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
                secondaryScript.runSection("towerCommitUnharmed");
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
        secondaryScript.runSection("steelSlay");

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

                    secondaryScript.runSection("steelSlayYes");
                    return this.ch1SlaySuccess(askPrize);
                
                case "maybe":
                    this.repeatActiveMenu = false;

                    secondaryScript.runSection("steelSlayMaybe");
                    break;

                case "no":
                    this.repeatActiveMenu = false;

                    secondaryScript.runSection("steelSlayNo");
                    break;
            }
        }

        // Unsure continues here
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
                    secondaryScript.runSection("steelSlayCheckBlade");
                    break;

                case "pulse":
                    if (!manager.confirmContentWarnings(Chapter.RAZOR)) break;
                    
                    this.repeatActiveMenu = false;
                    secondaryScript.runSection("steelSlayCheckPulse");
                    break;

                case "cGoStairs":
                case "leave":
                    if (!manager.confirmContentWarnings(Chapter.SPECTRE, "suicide")) break;

                    secondaryScript.runSection("steelSlayYes");
                    return this.ch1SlaySuccess(askPrize);

                case "cSlayPrincessNoBladeFail":
                    secondaryScript.runSection("steelSlaySlayFail");
                    break;

                default:
                    this.giveDefaultFailResponse(activeOutcome);
            }
        }

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
                    secondaryScript.runSection("slaySuccessStairsFail", true);
                    break;
                    
                case "cSlayPrincessNoBladeFail":
                    secondaryScript.runSection("slaySuccessSlayFail", true);
                    break;

                default:
                    this.giveDefaultFailResponse(activeOutcome);
            }
        }

        secondaryScript.runSection();

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "prizeYay", "Wait, is this my prize? This is great! Thank you so much.", askPrize));
        activeMenu.add(new Option(this.manager, "prizeBoo", "Wait, is this my prize? This sucks!", askPrize));
        activeMenu.add(new Option(this.manager, "bullshit", "That's bullshit! Let me out of here!"));
        activeMenu.add(new Option(this.manager, "ok", "Oh. Okay."));
        activeMenu.add(new Option(this.manager, "better", "I was kind of hoping I'd get a better ending for saving the world."));

        switch (parser.promptOptionsMenu(activeMenu)) {
            case "prizeYay":
                secondaryScript.runSection("successPrizeYay");
                break;

            case "prizeBoo":
                secondaryScript.runSection("successPrizeBoo");
                break;

            case "bullshit":
                if (askPrize) secondaryScript.runSection("successPrizeBoo");
                else secondaryScript.runSection("successBullshit");
                break;

            case "ok":
                secondaryScript.runSection("successOk");
                break;

            case "better":
                secondaryScript.runSection("successBetter");
                break;
        }

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
                    secondaryScript.runSection("successExplore");
                    break;

                case "notHappyA":
                case "notHappyB":
                    secondaryScript.runSection("successNotHappy");
                    
                    if (this.ch1HeroSuggestSpectre()) {
                        return ChapterEnding.TOSPECTRE;
                    } else if (this.isFirstVessel) {
                        return ChapterEnding.GOODENDING;
                    } else {
                        activeMenu.setCondition("notHappyA", false);
                        activeMenu.setCondition("hellNo", false);

                        activeMenu.setCondition("notHappyB", true);
                        activeMenu.setGreyedOut("sure", true);
                        activeMenu.setGreyedOut("ofCourse", true);

                        manager.attemptGoodEnding();

                        System.out.println();
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                
                case "hellNo":
                    secondaryScript.runSection("successHellNo");
                    
                    if (this.ch1HeroSuggestSpectre()) {
                        return ChapterEnding.TOSPECTRE;
                    } else if (this.isFirstVessel) {
                        return ChapterEnding.GOODENDING;
                    } else {
                        activeMenu.setCondition("notHappyA", false);
                        activeMenu.setCondition("hellNo", false);

                        activeMenu.setCondition("notHappyB", true);
                        activeMenu.setGreyedOut("sure", true);
                        activeMenu.setGreyedOut("ofCourse", true);

                        manager.attemptGoodEnding();

                        System.out.println();
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }

                case "sure":
                case "ofCourse":
                    secondaryScript.runSection("attemptGoodEnding");

                    if (this.isFirstVessel) {
                        return ChapterEnding.GOODENDING;
                    } else {
                        activeMenu.setCondition("notHappyA", false);
                        activeMenu.setCondition("hellNo", false);

                        activeMenu.setCondition("notHappyB", true);
                        activeMenu.setGreyedOut("sure", true);
                        activeMenu.setGreyedOut("ofCourse", true);

                        manager.attemptGoodEnding();

                        System.out.println();
                        parser.printDialogueLine(CANTSTRAY);
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
                    secondaryScript.runSection("suggestExplore");
                    break;
                    
                case "stickAround":
                    secondaryScript.runSection("suggestStickAround");
                    return false;

                case "notRisking":
                    secondaryScript.runSection("suggestNotRisking");
                    return false;

                case "reluctant":
                    repeatSub = false;
                    secondaryScript.runSection("suggestReluctant");
                    break;

                case "cSlaySelfNoBladeFail":
                case "cTakeFail":
                case "cGoStairs":
                case "anything":
                    repeatSub = false;
                    secondaryScript.runSection("suggestAnything");
                    break;

                case "cGoHill":
                    secondaryScript.runSection("suggestLeaveFail");
                    break;
                    
                case "cSlayNoPrincessFail":
                    secondaryScript.runSection("slaySuccessSlayFail");
                    break;

                default:
                    this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Committed to Spectre here
        return true;
    }

    /**
     * The player slays the harsh Princess after hesitating (leads to Chapter II: The Adversary / The Tower / The Nightmare)
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding ch1SlayHarshHesitated() {
        secondaryScript.runSection("slayHesitate");

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

                    secondaryScript.runSection("commitTower");
                    return ChapterEnding.TOTOWER;

                case "cSlayPrincess":
                    if (manager.hasVisited(Chapter.TOWER)) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                case "finish":
                    if (!manager.confirmContentWarnings(Chapter.ADVERSARY)) break;

                    secondaryScript.runSection("commitAdversary");
                    return ChapterEnding.TOADVERSARY;

                case "cGoStairs":
                    if (manager.hasVisited(Chapter.NIGHTMARE)) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                case "flee":
                    if (!manager.confirmContentWarnings(Chapter.NIGHTMARE)) break;

                    secondaryScript.runSection("slayFlee");

                    this.ch1ToNightmare(true, false);
                    return ChapterEnding.TONIGHTMAREFLED;
            }
        }

        throw new RuntimeException("No ending reached");
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
                    secondaryScript.runSection("commitRazor");
                    return ChapterEnding.TORAZOR;

                case "cSlayPrincess":
                case "finish":
                    secondaryScript.runSection("commitRazorMutual");
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

        secondaryScript.runSection("rescue");

        if (howFree) {
            secondaryScript.runSection("rescueHowFree");
        } else {
            secondaryScript.runSection("rescueStandard");
        }

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "whatIfA", "\"And if there isn't a key... do you have any ideas? Besides me cutting you out of here?\"", howFree));
        activeMenu.add(new Option(this.manager, "whatIfB", "\"And if there isn't a key... do you have any ideas?\"", !howFree));
        activeMenu.add(new Option(this.manager, "check", "\"I'm going to check upstairs. Maybe the key's still lying around somewhere up there. And if not, maybe I can at least find something to break you free.\""));

        switch (parser.promptOptionsMenu(activeMenu)) {
            case "whatIfA":
                secondaryScript.runSection("rescueWhatIfA");
                break;

            case "whatIfB":
                secondaryScript.runSection("rescueWhatIfB");
                break;

            case "check":
                secondaryScript.runSection("rescueCheck");
                break;
        }
        
        this.currentLocation = GameLocation.STAIRS;
        this.reverseDirection = true;
        this.withPrincess = false;

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
                    if (explore) {
                        secondaryScript.runSection("rescueStairsShoutA");
                    } else {
                        explore = true;
                        secondaryScript.runSection("rescueStairsShoutB");
                    }
                    break;

                case "cGoCabin":
                    activeMenu.setCondition("try", false);
                case "try":
                    explore = true;
                    
                    if (triedDoor) {
                        secondaryScript.runSection("rescueStairsTryAgain");
                    } else {
                        triedDoor = true;
                        if (explore) {
                            secondaryScript.runSection("rescueStairsTryFirstA");
                        } else {
                            explore = true;
                            secondaryScript.runSection("rescueStairsTryFirstB");
                        }
                    }

                    break;

                case "cGoBasement":
                case "return":
                    this.repeatActiveMenu = false;
                    break;
                
                case "cSlayNoPrincessFail":
                    mainScript.runSection("rescueStairsSlayFail");
                    break;

                default:
                    this.giveDefaultFailResponse(this.activeOutcome);
            }
        }

        secondaryScript.runSection("rescueReturn");

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
        secondaryScript.runSection("rescueSave");

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
        
        secondaryScript.runSection();

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

                    secondaryScript.runSection("rescueControlledSlay1");

                    this.activeMenu = new OptionsMenu();
                    activeMenu.add(slay);
                    activeMenu.add(new Option(this.manager, "giveUp", "[Give up.]"));

                    this.repeatActiveMenu = true;
                    while (repeatActiveMenu) {
                        switch (parser.promptOptionsMenu(activeMenu)) {
                            case "cSlayPrincess":
                            case "slay":
                                secondaryScript.runSection("commitTowerRescueA");
                                return ChapterEnding.TOTOWER;

                            case "giveUp":
                                secondaryScript.runSection("rescueControlledSlayGiveUp");
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

        secondaryScript.runSection();
        
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

                    secondaryScript.runSection("commitTowerRescueA");
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
        secondaryScript.runSection();
        return ChapterEnding.TOPRISONER;
    }

    /**
     * The player decides to slay the harsh Princess after initially deciding to free her (leads to Chapter II: The Adversary / The Tower / The Nightmare)
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding ch1RescueSlayHarsh() {
        secondaryScript.runSection("rescueSlay");

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
        
        secondaryScript.runSection();

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
                    
                    secondaryScript.runSection("commitTowerRescueB");
                    return ChapterEnding.TOTOWERPATHETIC;

                case "cSlayPrincess":
                    if (manager.hasVisited(Chapter.ADVERSARY)) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                case "finish":
                    if (!manager.confirmContentWarnings(Chapter.ADVERSARY)) break;
                    
                    secondaryScript.runSection("commitAdversaryRescue");
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
                    
                    secondaryScript.runSection("rescueFlee");

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

        if (wounded) mainScript.runSection("nightmareStartWounded");
        else mainScript.runSection("nightmareStart");

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "threat", "\"Threaten me all you want! All it does is ease my guilty conscience.\""));
        activeMenu.add(new Option(this.manager, "notPrincess", "\"Whatever you are, you're not a Princess. Go ahead and waste your energy. I'll be waiting for you.\"", this.isHarsh));
        activeMenu.add(new Option(this.manager, "act", "\"So all of that was just an act, wasn't it? You're not really innocent or harmless. You're not even a princess. You're a *monster.*\"", !this.isHarsh));
        activeMenu.add(new Option(this.manager, "bleedOut", "\"Bang on the door all you want. It'll only make you bleed out faster.\"", wounded && !this.isHarsh));
        activeMenu.add(new Option(this.manager, "ignore", "[Ignore her and go to sleep.]"));

        switch (parser.promptOptionsMenu(activeMenu)) {
            case "threat":
                mainScript.runSection("nightmareThreat");
                break;
            
            case "notPrincess":
                mainScript.runSection("nightmareNotPrincess");
                break;

            case "act":
                mainScript.runSection("nightmareAct");
                break;

            case "bleedOut":
                if (lostArm) mainScript.runSection("nightmareBleedOutLostArm");
                else mainScript.runSection("nightmareBleedOut");
                break;

            case "ignore":
                mainScript.runSection("nightmareIgnore");
                break;
        }
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
        if (this.isFirstVessel) manager.setFirstPrincess(this.activeChapter);
        this.secondaryScript = new Script(this.manager, this.parser, "Chapter2Shared");
        
        manager.setNowPlaying("Fragmentation");

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
            parser.printDialogueLine("You emerge into the clearing. The cabin waits at the top of the hill.");
            
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

        this.currentLocation = GameLocation.CABIN;
        this.mirrorPresent = true;
        this.knowsBlade = true;
        this.withBlade = true;
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
        parser.printDialogueLine("You emerge into the clearing. The cabin waits at the top of the hill.");
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

    /**
     * The player asks the Narrator about the mirror in the cabin during Chapter II
     * @return true if the player chooses to approach the mirror; false otherwise
     */
    private boolean ch2AskMirror() {
        this.mirrorComment = true;
        
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "There's *definitely* a mirror."));
        parser.printDialogueLine(new VoiceDialogueLine("There isn't."));

        boolean defaultCareLie = false;
        boolean defaultWhyLie = false;
        boolean defaultNoMatter = true;
        boolean defaultHandsomeCare = false;
        switch (this.activeChapter) {
            case ADVERSARY:
                defaultCareLie = true;
                defaultNoMatter = false;
                defaultHandsomeCare = true;
                parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "Oh, stop bickering and just get on with it. Who even cares if there's a mirror?"));
                break;

            case BEAST:
                defaultWhyLie = true;
                if (this.sharedLoop) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "What a strange thing to lie about. Maybe He doesn't see it, much like He hasn't seen what's already happened."));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "What a strange thing to lie about. Maybe He doesn't see it."));
                }
                break;

            case DAMSEL:
                defaultWhyLie = true;
                parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "I'm sure the Princess would tell us there was a mirror if *she* were up here."));
                parser.printDialogueLine(new VoiceDialogueLine("In which case she'd be lying to you, because again, there isn't a mirror."));
                break;

            case NIGHTMARE:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "We have to look at it... unless that's what He wants us to do and pretending it isn't there is a trick to get us to do exactly what He wants."));
                break;

            case PRISONER:
                defaultWhyLie = true;
                parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "I think you know what we have to do."));
                break;

            case RAZOR:
                defaultCareLie = true;
                defaultNoMatter = false;
                defaultHandsomeCare = true;
                parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Can you two stop arguing? It's stressful enough in here without all of this extra noise."));
                break;

            case SPECTRE:
                defaultCareLie = true;
                defaultNoMatter = false;
                defaultHandsomeCare = true;
                parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "Who cares if there's a mirror? Let's just go into the basement and find her body so we can be done with this."));
                break;

            case STRANGER:
                defaultWhyLie = true;
                parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "You insisting it isn't there just makes me want to look at it even more."));
                break;

            case TOWER:
                defaultNoMatter = false;
                parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "Who cares if there's a mirror? We're all going to die anyway, and I'm sure that if we looked in there we'd just see something sad and miserable looking back at us. We don't need any reminders of what we are. It would only make things worse."));
                parser.printDialogueLine(new VoiceDialogueLine("*Sigh.* For the last time, you're not going to die unless you let it happen. And luckily for you, there isn't a mirror, so no one needs to worry about confronting a grisly visage any time in the near future."));
                parser.printDialogueLine(new VoiceDialogueLine("Though, for what it's worth, if there were a mirror, I'm sure that you wouldn't find anything \"sad\" or \"miserable\" in it looking back at you. You probably look perfectly normal."));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "\"Probably?\" Do you not know what we look like?"));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "He knows. He just doesn't have the heart to tell us."));
                break;

            case WITCH:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "But He says there isn't one. That's got to count for something, right?"));
                break;
        }

        OptionsMenu mirrorMenu = new OptionsMenu();
        mirrorMenu.add(new Option(this.manager, "careLie", "I care about whether I'm being lied to.", defaultCareLie));
        mirrorMenu.add(new Option(this.manager, "careLieTower", "I care about whether I'm being lied to. There's a mirror.", this.activeChapter == Chapter.TOWER));
        mirrorMenu.add(new Option(this.manager, "whyLie", "Why *would* you lie about that? What's the point?", defaultWhyLie));
        mirrorMenu.add(new Option(this.manager, "whyLieNightmare", "Why *would* you lie about there not being a mirror when it's clearly right there? What's the point?", this.activeChapter == Chapter.NIGHTMARE));
        mirrorMenu.add(new Option(this.manager, "whyLieWitch", "I trust my eyes. Why would He lie about there not being a mirror? What's the point?", this.activeChapter == Chapter.WITCH));
        mirrorMenu.add(new Option(this.manager, "handsome", "I want to look at myself. I want to see how *handsome* I am.", !defaultHandsomeCare && this.activeChapter != Chapter.STRANGER));
        mirrorMenu.add(new Option(this.manager, "handsomeCare", "I care. I want to look at myself. I want to see how *handsome* I am.", defaultHandsomeCare));
        mirrorMenu.add(new Option(this.manager, "handsomeStranger", "I also want to look at myself. I want to see how *handsome* I am.", this.activeChapter == Chapter.STRANGER));
        mirrorMenu.add(new Option(this.manager, "noMatter", "It doesn't matter.", defaultNoMatter));
        mirrorMenu.add(new Option(this.manager, "noMatterRight", "You're right. It doesn't matter.", !defaultNoMatter && this.activeChapter != Chapter.TOWER));
        mirrorMenu.add(new Option(this.manager, "noMatterTower", "It doesn't matter whether there's a mirror.", this.activeChapter == Chapter.TOWER));
        mirrorMenu.add(new Option(this.manager, "silent", "[Remain silent.]"));
        mirrorMenu.add(new Option(this.manager, "approach", "[Approach the mirror.]"));

        boolean repeatMenu = true;
        while (repeatMenu) {
            switch (parser.promptOptionsMenu(mirrorMenu)) {
                case "careLie":
                    repeatMenu = false;
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "As do I."));
                    parser.printDialogueLine(new VoiceDialogueLine("I'm not lying to you. Use your eyes, there is no mirror. Why would I lie about something so meaningless? What good would a mirror even do? Let you waste time preening yourself instead of doing what needs to be done?"));
                    break;

                case "careLieTower":
                    repeatMenu = false;
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "As do I, and yeah, there is."));
                    parser.printDialogueLine(new VoiceDialogueLine("I'm not lying to you. Use your eyes, there is no mirror. Why would I lie about something so meaningless? What good would a mirror even do? Let you waste time preening yourself instead of doing what needs to be done?"));
                    break;

                case "whyLieWitch":
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "Come on, now. He's pretty much in charge here. When have authority figures ever lied about anything? If there were a mirror in this cabin and we were supposed to look at it, He would have told us about it."));
                case "whyLieNightmare":
                case "whyLie":
                    repeatMenu = false;
                    parser.printDialogueLine(new VoiceDialogueLine("Exactly. Why would I lie about something so meaningless? What good would a mirror even do? Let you waste time preening yourself instead of doing what needs to be done?"));
                    break;

                case "handsomeCare":
                    repeatMenu = false;
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I care less about that and more about whether we're being lied to. If He's willing to lie about something as innocuous as a mirror, what else is He hiding from us?"));
                    parser.printDialogueLine(new VoiceDialogueLine("I'm not lying to you. Use your eyes, there is no mirror. Why would I lie about something so meaningless? What good would it even do?"));
                    break;

                case "handsome":
                case "handsomeStranger":
                    repeatMenu = false;

                    if (this.activeChapter == Chapter.PRISONER) {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "Let's not get caught up in vanity, but we should definitely take a closer look. Whatever it is, He must not want us to know about it."));
                        parser.printDialogueLine(new VoiceDialogueLine("Is this some sort of rehearsed bit? Use your eyes, there is no mirror. Why would I lie about something so meaningless? What good would it even do?"));
                    } else if (this.activeChapter == Chapter.TOWER) {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "Please don't. I'd rather the Princess kill us again than see how dreadful we are."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I care less about what we look like and more about whether we're being lied to. If He's willing to lie about something as innocuous as a mirror, what else is He hiding from us?"));
                        parser.printDialogueLine(new VoiceDialogueLine("I'm not lying to you. Use your eyes, there is no mirror. Why would I lie about something so meaningless? What good would it even do?"));
                    } else if (this.activeChapter == Chapter.WITCH) {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "Oh, I'm sure we'd look great. If only there were some sort of reflective surface we could examine. Absolute shame there isn't anything like that around here."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "You... you *do* see it, right? I don't know how to read you."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "I-I... see all sorts of things."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "But do you see the mirror? It's a simple yes or no question."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "Uh... I, um... help me out here, will you?"));
                        parser.printDialogueLine(new VoiceDialogueLine("I'm not lying to you. Use your eyes, there is no mirror. Why would I lie about something so meaningless? What good would a mirror even do? Let you waste time preening yourself instead of doing what needs to be done?"));
                    } else {
                        if (this.activeChapter == Chapter.DAMSEL) {
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "That's a great idea. We have to make sure we're looking our best before we save her."));
                        }

                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "We shouldn't waste time *preening,* but if He *is* lying about the mirror, it might be important."));

                        if (this.activeChapter == Chapter.BEAST) {
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "Or it's dangerous."));
                        }

                        parser.printDialogueLine(new VoiceDialogueLine("I'm not lying to you. Use your eyes, there is no mirror. Why would I lie about something so meaningless? What good would it even do?"));

                        if (this.activeChapter == Chapter.NIGHTMARE) {
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "Maybe He's trying to keep us from looking because there's something horribly wrong with us."));
                            parser.printDialogueLine(new VoiceDialogueLine("No. There isn't something horribly wrong with you. You look exactly how you're supposed to look, now stop second-guessing my every word."));
                        }
                    }

                    break;

                case "noMatter":
                case "noMatterRight":
                case "noMatterTower":
                    repeatMenu = false;
                    this.mirrorPresent = false;

                    if (this.activeChapter == Chapter.PRISONER) {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "We should treat everything in here like it matters."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Exactly, don't you care if we're being lied to? If He's willing to lie about something as innocuous as a mirror, what else is He hiding from us?"));
                    } else {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "But it *does* matter! Don't you care if we're being lied to? If He's willing to lie about something as innocuous as a mirror, what else is He hiding from us?"));
                    }

                    parser.printDialogueLine(new VoiceDialogueLine("I'm not lying to you. Use your eyes, there is no mirror. Why would I lie about something so meaningless? What good would a mirror even do? Let you waste time preening yourself instead of doing what needs to be done?"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "But there *was* a mirror a second ago."));

                    switch (this.activeChapter) {
                        case ADVERSARY:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "And now it's gone, so all of us can stop arguing about it and get to fightin'."));
                            break;

                        case BEAST:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "And now it's gone."));
                            break;

                        case DAMSEL:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "And now it's gone. Pity. We could have a feather out of place and now we'll never know. We can't gallantly sweep her off her feet if we have a feather out of place."));
                            break;

                        case NIGHTMARE:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "Did *He* make it go away? Clearly there was something in there worth investigating if He wants it hidden so bad..."));
                            break;

                        case PRISONER:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "And now it's gone. If He doesn't want us to know about it, it must be important. We should keep our eyes peeled. Maybe it'll be back."));
                            break;

                        case RAZOR:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "And now it's gone. Yet another thing in here playing tricks on us. I hate this place."));
                            break;

                        case SPECTRE:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "And now it's gone. Let's not spend much longer worrying over it. Clearly it's not even important enough to be acknowledged."));
                            break;

                        case STRANGER:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "And now it's gone. You know that taking the mirror away from us isn't going to change things, right? We'll find it again, and then we'll see whatever it is that you don't want us to see."));
                            break;

                        case TOWER:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "We should count ourselves lucky. Some things are better left unseen."));
                            break;

                        case WITCH:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "Well, at least we can all agree now that there's nothing to see here. Case closed. Good work everyone."));
                            break;
                    }

                    break;

                case "silent":
                    repeatMenu = false;
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I care if we're being lied to. If He's willing to lie about something as innocuous as a mirror, what else could He hiding from us?"));
                    parser.printDialogueLine(new VoiceDialogueLine("I'm not lying to you, I *promise.* There isn't a mirror. Really."));
                    break;

                case "cApproachMirror":
                case "approach":
                    this.ch2ApproachMirror();
                    return true;

                default:
                    super.giveDefaultFailResponse();
            }
        }

        return false;
    }

    /**
     * The player approaches the mirror in the cabin during Chapter II
     */
    private void ch2ApproachMirror() {
        this.touchedMirror = true;
        this.mirrorPresent = false;

        if (this.activeChapter == Chapter.NIGHTMARE) {
            parser.printDialogueLine(new VoiceDialogueLine("You walk up to the wall next to the empty basement doorframe. It's a wall. There isn't much to see here."));
        } else {
            parser.printDialogueLine(new VoiceDialogueLine("You walk up to the wall next to the basement door. It's a wall. There isn't much to see here."));
        }

        if (this.mirrorComment) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "This *really* isn't funny."));
        } else {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "What are you talking about? This isn't a wall. It's a mirror. Or at least it'll *be* a mirror once we wipe off that layer of grime."));
        }

        OptionsMenu mirrorMenu = new OptionsMenu();
        mirrorMenu.add(new Option(this.manager, "wipe", "[Wipe the mirror clean.]"));

        boolean repeatMenu = true;
        while (repeatMenu) {
            switch (parser.promptOptionsMenu(mirrorMenu)) {
                case "wipe":
                    repeatMenu = false;
                    break;

                default: super.giveDefaultFailResponse(activeOutcome);
            }
        }
        
        parser.printDialogueLine(new VoiceDialogueLine("You reach forward and rub your hand against the cabin wall. I hope you know how ridiculous you look right now."));

        switch (this.activeChapter) {
            case ADVERSARY:
                if (this.mirrorComment) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "But it was there a second ago!"));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "But there was a mirror a second ago."));
                }

                parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "And now it's gone, so all of us can stop arguing about it and get to fightin'."));
                break;

            case BEAST:
                if (this.mirrorComment) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "But it was there a second ago!"));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "But there was a mirror a second ago."));
                }
                
                parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "And now it's gone."));
                break;

            case DAMSEL:
                if (this.mirrorComment) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "But it was there a second ago!"));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "But there was a mirror a second ago."));
                }
                
                parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "And now it's gone. Pity. We could have a feather out of place and now we'll never know. We can't gallantly sweep her off her feet if we have a feather out of place."));
                break;

            case NIGHTMARE:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "Did *He* make it go away? Clearly there was something in there worth investigating if He wants it hidden so bad..."));
                break;

            case PRISONER:
                if (this.mirrorComment) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "But it was there a second ago!"));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "But there was a mirror a second ago."));
                }
                
                parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "And now it's gone. If He doesn't want us to know about it, it must be important. We should keep our eyes peeled. Maybe it'll be back."));
                break;

            case RAZOR:
                if (this.mirrorComment) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "But it was there a second ago!"));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "But there was a mirror a second ago."));
                }
                
                parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "And now it's gone. Yet another thing in here playing tricks on us. I hate this place."));
                break;

            case SPECTRE:
                if (this.mirrorComment) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "But it was there a second ago!"));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "But there was a mirror a second ago."));
                }
                
                parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "And now it's gone. Let's not spend much longer worrying over it. Clearly it's not even important enough to be acknowledged."));
                break;

            case STRANGER:
                if (this.mirrorComment) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "But it was there a second ago!"));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "But there was a mirror a second ago."));
                }
                
                parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "And now it's gone. You know that taking the mirror away from us isn't going to change things, right? We'll find it again, and then we'll see whatever it is that you don't want us to see."));
                break;

            case TOWER:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "We should count ourselves lucky. Some things are better left unseen."));
                break;

            case WITCH:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "Well, at least we can all agree now that there's nothing to see here. Case closed. Good work everyone."));
                break;
        }
    }


    // - Chapter II: The Adversary -

    /**
     * Runs Chapter II: The Adversary
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding adversary() {
        // You gain the Voice of the Stubborn

        if (!this.chapter2Intro(true, true, false)) {
            return ChapterEnding.ABORTED;   
        }

        manager.setNowPlaying("The Adversary");
        
        parser.printDialogueLine(new VoiceDialogueLine("The cabin is tighter than its exterior would suggest. Its cold stone walls press in on you, as if trying to forcefully direct you towards your destination. The only furniture of note is a black-iron altar with a pristine blade perched on its edge."));
        parser.printDialogueLine(new VoiceDialogueLine("The blade is your implement. You'll need it if you want to do this right."));

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "mirror", "(Explore) You didn't say anything about the mirror on the wall."));
        activeMenu.add(new Option(this.manager, "different", "(Explore) This whole cabin is different than last time.", this.sharedLoopInsist));
        activeMenu.add(new Option(this.manager, "approach", "(Explore) [Approach the mirror.]"));
        activeMenu.add(new Option(this.manager, "take", "(Explore) [Take the blade.]"));
        activeMenu.add(new Option(this.manager, "enter", "[Enter the basement.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "mirror":
                    parser.printDialogueLine(new VoiceDialogueLine("That's because there isn't a mirror. There's the altar, the blade sitting on the altar, and the door to the basement. There's nothing else in here."));
                    if (this.ch2AskMirror()) {
                        activeMenu.setCondition("approach", false);
                    }
                    break;

                case "different":
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "*Very* different."));
                    parser.printDialogueLine(new VoiceDialogueLine("Maybe that's because you haven't actually been here. I hope this means you'll finally drop that ridiculous past-life nonsense. You haven't died, and you certainly haven't been killed by the Princess."));
                    parser.printDialogueLine(new VoiceDialogueLine("So focus up. The world is depending on you."));
                    break;

                case "cApproachMirror":
                    activeMenu.setCondition("approach", false);
                case "approach":
                    activeMenu.setCondition("mirror", false);
                    this.ch2ApproachMirror();
                    break;

                case "cTake":
                    activeMenu.setCondition("take", false);
                case "take":
                    this.hasBlade = true;
                    this.withBlade = false;
                    parser.printDialogueLine(new VoiceDialogueLine("You take the blade from the altar. It would be difficult to slay the Princess and save the world without a weapon."));
                    break;

                case "cGoStairs":
                case "enter":
                    this.repeatActiveMenu = false;
                    break;

                default:
                    this.giveDefaultFailResponse(activeOutcome);
            }
        }

        if (!this.hasBlade) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "No blade this time? Right then, fisticuffs it is. Probably more fair to her anyway. Wouldn't want to feel like we cheated our way to a win."));
            parser.printDialogueLine(new VoiceDialogueLine("As long as you can still get the job done... and don't forget that the blade is waiting for you upstairs if you happen to change your mind."));
            System.out.println();
        }

        this.withBlade = false;
        this.mirrorPresent = false;
        parser.printDialogueLine(new VoiceDialogueLine("The door to the basement creaks open, revealing a rough stone staircase, its walls pressing at your sides and tightening as you descend. The air seeping from below is heavy and oppressive, with an almost sulfuric odor to it. If the Princess lives here, slaying her would probably be doing her a favor."));
        parser.printDialogueLine(new VoiceDialogueLine("Her fierce voice carries up the stairs."));
        parser.printDialogueLine(new PrincessDialogueLine("Is that another challenger? *Finally.* It's been ages since I've had a good fight."));
        parser.printDialogueLine(new VoiceDialogueLine("This isn't what she sounded like last time. Her voice is a little deeper, almost threatening."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "Good. Sounds like my kind of Princess."));
        parser.printDialogueLine(new VoiceDialogueLine("As much as I appreciate the enthusiasm, just make sure you don't let your bloodlust get to your head. You need to stay focused and keep your wits about you. Remember you're here to *slay* the Princess, not to have a good fight."));

        this.currentLocation = GameLocation.BASEMENT;
        this.withPrincess = true;
        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("As you descend the final step, the form of the Princess comes into view, a large shackle leading from her wrist to the basement wall."));
        parser.printDialogueLine("This time, she looks almost demonic. She stands tall on hooved feet, her dress torn and tattered. Horns sprout from her forehead in place of her tiara. Her pointed tail flicks back and forth behind her as she catches sight of you, a cruel grin splitting her face.");
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "It looks like she could rip those chains out of the wall without a second thought."));
        parser.printDialogueLine(new PrincessDialogueLine("Oh, it's you again. I've been hoping you'd find your way back here. Good to see that death doesn't stick for either of us."));

        if (this.hasBlade) {
            parser.printDialogueLine(new PrincessDialogueLine("And you brought your little knife too. YES."));
            parser.printDialogueLine(new PrincessDialogueLine("I'm going to have fun breaking you into little pieces."));
        } else {
            parser.printDialogueLine(new PrincessDialogueLine("But no little knife this time, huh?"));
            parser.printDialogueLine(new PrincessDialogueLine("Ugh. I hope you're not just here to \"chat.\" I've been itching for a rematch."));
        }



        
        // temporary templates for copy-and-pasting
        /*
        parser.printDialogueLine(new VoiceDialogueLine("XXXXX"));
        parser.printDialogueLine(new PrincessDialogueLine("XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "(Explore) XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "\"XXXXX\""));
        */

        // PLACEHOLDER
        return null;
    }


    // - Chapter III: The Eye of the Needle -

    /**
     * Runs Chapter III: The Eye of the Needle
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
     * Runs Chapter III: The Fury
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
     * Runs Chapter II: The Tower
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding tower() {
        // You gain the Voice of the Broken

        int resistCount = 0;
        int submitCount = 0;
        boolean tookBlade = false;

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

        manager.setNowPlaying("The Tower");
        
        parser.printDialogueLine(new VoiceDialogueLine("The interior of the cabin is larger and more grandiose than its humble exterior would suggest. The only furniture of note is a massive marble altar with a pristine blade perched on its edge."));
        parser.printDialogueLine(new VoiceDialogueLine("The blade is your implement. You'll need it if you want to do this right."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Why do we feel so... *small?*"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "We don't feel small. We *are* small."));

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "mirror", "(Explore) You didn't say anything about the mirror on the wall."));
        activeMenu.add(new Option(this.manager, "different", "(Explore) This whole cabin is different than last time.", this.sharedLoopInsist));
        activeMenu.add(new Option(this.manager, "approach", "(Explore) [Approach the mirror.]"));
        activeMenu.add(new Option(this.manager, "take", "(Explore) [Take the blade.]"));
        activeMenu.add(new Option(this.manager, "enter", "[Enter the basement.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "mirror":
                    parser.printDialogueLine(new VoiceDialogueLine("That's because there isn't a mirror. There's the altar, the blade sitting on the altar, and the door to the basement. There's nothing else in here."));
                    if (this.ch2AskMirror()) {
                        activeMenu.setCondition("approach", false);
                    }
                    break;

                case "different":
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "*Very* different."));
                    parser.printDialogueLine(new VoiceDialogueLine("Maybe that's because you haven't actually been here. I hope this means you'll finally drop that ridiculous past-life nonsense. You haven't died, and you certainly haven't been killed by the Princess."));
                    break;

                case "cApproachMirror":
                    activeMenu.setCondition("approach", false);
                case "approach":
                    activeMenu.setCondition("mirror", false);
                    this.ch2ApproachMirror();
                    break;

                case "cTake":
                    activeMenu.setCondition("take", false);
                case "take":
                    this.hasBlade = true;
                    this.withBlade = false;
                    tookBlade = true;
                    parser.printDialogueLine(new VoiceDialogueLine("You take the blade from the altar. It would be difficult to slay the Princess and save the world without a weapon."));
                    break;

                case "cGoStairs":
                case "enter":
                    this.repeatActiveMenu = false;
                    break;

                default:
                    this.giveDefaultFailResponse(activeOutcome);
            }
        }

        if (!this.hasBlade) {
            submitCount += 1;
            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "No blade this time? Yeah. Maybe she'll be more receptive if we're unarmed."));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "Blade. No blade. It doesn't matter."));
            System.out.println();
        }

        this.currentLocation = GameLocation.STAIRS;
        this.withBlade = false;
        this.mirrorPresent = false;
        parser.printDialogueLine(new VoiceDialogueLine("The door to the basement creaks open, revealing a spiral staircase, its steps almost as deep as you are tall. The smell of incense drifts up from below. For a moment, you almost feel at ease."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "Huh. This is actually kind of nice."));
        parser.printDialogueLine(new VoiceDialogueLine("It's still a stone basement. If the Princess lives here, slaying her is probably doing her a favor."));
        parser.printDialogueLine(new VoiceDialogueLine("Her booming voice rolls up the stairs."));
        parser.printDialogueLine(new PrincessDialogueLine("s that a guest I hear? Don't linger on the stairs. *Come down and witness me.*"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "You weren't kidding when you said it was \"booming.\" She wasn't like this last time."));
        if (!this.hasBlade) parser.printDialogueLine(new VoiceDialogueLine("You shouldn't have come down here unarmed."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "We need to get down there. She wants us to see her. We need to see her."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Should we be worried about your sudden change in attitude? Just a few minutes ago you were going on about how pointless everything was, now you *want* to go down there?"));
        parser.printDialogueLine(new VoiceDialogueLine("It doesn't matter what that little voice says. He's not the one making the decisions... Though if his ramblings get you to the Princess, they get you to the Princess."));

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "continue", "[Continue down the stairs.]"));

        this.repeatActiveMenu = true;
        while (this.repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "cGoBasement":
                case "continue":
                    this.repeatActiveMenu = false;
                    break;

                case "cGoCabin":
                    parser.printDialogueLine(new VoiceDialogueLine("What? No. You're already halfway down the stairs, you can't just turn around now."));
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        this.currentLocation = GameLocation.BASEMENT;
        this.withPrincess = true;
        parser.printDialogueLine(new VoiceDialogueLine("Making your way down the spiral staircase is a time consuming and exhausting effort, every step requiring you to clamber over one edge before dropping to the next. But soon, the end comes into view, and you tumble to the bottom, entering the vast, temple-like room beyond."));
        parser.printDialogueLine(new VoiceDialogueLine("The Princess towers over you, almost glowing in the weak starlight, her figure framed by a stained glass window. Her long hair billows around her, and a chain binds her wrist to the far wall."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "The chain is nothing to her. It might as well be a toy for all the good it would do. I told you it was pointless to resist her."));
        parser.printDialogueLine(new PrincessDialogueLine("The little bird has returned to me. I wonder what he wants."));

        if (this.hasBlade) {
            parser.printDialogueLine(new PrincessDialogueLine("You've brought that knife again... even though you know it's useless. Such charming audacity."));
            parser.printDialogueLine(new PrincessDialogueLine("DROP IT."));

            this.canDropBlade = true;
            this.activeMenu = new OptionsMenu();
            activeMenu.add(new Option(this.manager, "drop", "[Drop it.]"));
            activeMenu.add(new Option(this.manager, "tighten", "[Tighten your grip.]"));

            this.repeatActiveMenu = true;
            while (repeatActiveMenu) {
                switch (parser.promptOptionsMenu(activeMenu)) {
                    case "cDrop":
                    case "drop":
                        submitCount += 1;
                        parser.printDialogueLine(new VoiceDialogueLine("The blade slips from your fingers and clatters uselessly to the floor."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "We didn't have to do that."));
                        parser.printDialogueLine(new VoiceDialogueLine("And yet you did."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "She's so much more than us. You wouldn't understand what it feels like to be in her presence."));
                        break;

                    case "cSlayPrincessFail":
                    case "tighten":
                        resistCount += 1;
                        parser.printDialogueLine(new VoiceDialogueLine("As if on command, the blade slips from your grasp. It clatters uselessly to the floor."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "But we *didn't* drop it. We decided to grip it tighter, remember? Are you really just going to let that happen to us?"));
                        parser.printDialogueLine(new VoiceDialogueLine("I have a duty to report facts as facts, and the fact is that you dropped the blade."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "Of course we dropped it. She's so much more than us. You wouldn't understand what it feels like to be in her presence."));
                        break;

                    default: this.giveDefaultFailResponse();
                }
            }

            this.hasBlade = false;
            this.canDropBlade = false;
            this.withBlade = true;
            parser.printDialogueLine(new VoiceDialogueLine("Oh, I understand what's going on, and you'd better snap yourself out of it."));
        } else {
            parser.printDialogueLine(new PrincessDialogueLine("I see your hands are empty. You've already given up, haven't you? You aren't even going to try and kill me. How sweet."));
            parser.printDialogueLine(new PrincessDialogueLine("And more than a little disappointing."));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "She's disappointed in us?"));
        }
        
        parser.printDialogueLine(new PrincessDialogueLine("KNEEL."));

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "no", "\"No.\""));
        activeMenu.add(new Option(this.manager, "kneel", "[Kneel.]"));

        switch (parser.promptOptionsMenu(activeMenu)) {
            case "no":
                if (resistCount == 2) {
                    resistCount += 1;
                    parser.printDialogueLine(new PrincessDialogueLine("Oh? Are you still trying to defy me? I. Said. KNEEL."));
                    parser.printDialogueLine(new VoiceDialogueLine("Your legs buckle, and your knees hit the floor."));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine("But the words don't leave your mouth. Instead, your legs buckle, and your knees hit the floor."));
                }

                break;

            case "kneel":
                submitCount += 1;
                parser.printDialogueLine(new VoiceDialogueLine("On her command, you fall to the floor, knees painfully connecting with hard stone."));
                break;
        }

        parser.printDialogueLine(new PrincessDialogueLine("That's my good little bird. Now... why don't we talk?"));
        


        
        // temporary templates for copy-and-pasting
        /*
        parser.printDialogueLine(new VoiceDialogueLine("XXXXX"));
        parser.printDialogueLine(new PrincessDialogueLine("XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "(Explore) XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "\"XXXXX\""));
        */

        // PLACEHOLDER
        return null;
    }


    // - Chapter III: The Apotheosis -

    /**
     * Runs Chapter III: The Apotheosis
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
     * Runs Chapter II: The Spectre
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding spectre() {
        // You gain the Voice of the Cold

        this.isHarsh = false;

        if (!this.chapter2Intro(false, true, true)) {
            return ChapterEnding.ABORTED;
        }

        manager.setNowPlaying("The Spectre");
        
        parser.printDialogueLine(new VoiceDialogueLine("The interior of the cabin is cold, a soft odor of dirt permeating the air. Cobwebs flutter in the corners. You can hear wind whistling outside, banging the shutters against the windows. The only furniture of note is an elegant antique table with a pristine blade perched on the edge."));
        parser.printDialogueLine(new VoiceDialogueLine("The blade is your implement. You'll need it if you want to do this right."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "It feels like no one's been here for a long, long time."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "Like I've been saying. She's dead. We killed her already."));

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "mirror", "(Explore) You didn't say anything about the mirror on the wall."));
        activeMenu.add(new Option(this.manager, "different", "(Explore) This whole cabin is different than last time.", this.sharedLoopInsist));
        activeMenu.add(new Option(this.manager, "approach", "(Explore) [Approach the mirror.]"));
        activeMenu.add(new Option(this.manager, "take", "(Explore) [Take the blade.]"));
        activeMenu.add(new Option(this.manager, "enter", "[Enter the basement.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "mirror":
                    parser.printDialogueLine(new VoiceDialogueLine("That's because there isn't a mirror. There's a table, the blade sitting on the table, and the door to the basement. There's nothing else in here."));
                    if (this.ch2AskMirror()) {
                        activeMenu.setCondition("approach", false);
                    }
                    break;

                case "different":
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "*Very* different."));
                    parser.printDialogueLine(new VoiceDialogueLine("Maybe that's because you haven't actually been here. I hope this means you'll finally drop that ridiculous past-life nonsense. You haven't died, and you certainly haven't already slain the Princess."));
                    parser.printDialogueLine(new VoiceDialogueLine("So focus up. The world is depending on you."));
                    break;

                case "cApproachMirror":
                    activeMenu.setCondition("approach", false);
                case "approach":
                    activeMenu.setCondition("mirror", false);
                    this.ch2ApproachMirror();
                    break;

                case "cTake":
                    activeMenu.setCondition("take", false);
                case "take":
                    this.hasBlade = true;
                    this.withBlade = false;
                    parser.printDialogueLine(new VoiceDialogueLine("You take the blade from the table. It would be difficult to slay the Princess and save the world without a weapon."));
                    break;

                case "cGoStairs":
                case "enter":
                    this.repeatActiveMenu = false;
                    break;

                default:
                    this.giveDefaultFailResponse(activeOutcome);
            }
        }

        this.withBlade = false;
        this.mirrorPresent = false;
        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("The door to the basement groans open, revealing an old banister and a creaky, wooden stairwell. Everything is coated in a thick layer of dust, and you can feel it settle into your lungs as you breathe in the stale air. The very building itself feels dead. If the Princess lives here, slaying her would probably be doing her a favor."));
        parser.printDialogueLine(new VoiceDialogueLine("The room below is silent."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "Nobody's here. Naturally."));
        parser.printDialogueLine(new VoiceDialogueLine("As much as I appreciate the optimism, you shouldn't be so sure."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I guess we'll just have to go down there and see."));

        this.currentLocation = GameLocation.BASEMENT;
        this.withPrincess = true;
        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("As you descend the final step, the form of the Princess comes into view. A skeletal body lying in a heap on the floor, its wrist still bound to the wall by a thick chain."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Okay. She's definitely dead."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "It's just like I told you --", true));
        parser.printDialogueLine(new VoiceDialogueLine("Before you have a chance to finish your thought, the top of a head appears from underneath the floor."));
        parser.printDialogueLine(new VoiceDialogueLine("Two deep-set eyes stare up at you, followed by a mischievous skeletal grin."));
        parser.printDialogueLine(new VoiceDialogueLine("And finally, the rest of the body floats up to join the head. Wait... this isn't right. What's going on here?"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "A g-g-g-ghost!"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "Oh. Wow. How absolutely terrifying. What's a ghost supposed to do to us?"));
        parser.printDialogueLine(new PrincessDialogueLine("Oh. It's you. Hiya, killer. I was hoping to see you again. I have some issues with how our last meeting went."));
        parser.printDialogueLine("She speaks in a whisper, her voice soft and playful.");



        
        // temporary templates for copy-and-pasting
        /*
        parser.printDialogueLine(new VoiceDialogueLine("XXXXX"));
        parser.printDialogueLine(new PrincessDialogueLine("XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "(Explore) XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "\"XXXXX\""));
        */

        // PLACEHOLDER
        return null;
    }


    // - Chapter III: The Princess and the Dragon -

    /**
     * Runs Chapter III: The Princess and the Dragon
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding princessAndDragon() {
        // "You" have Cold + Opportunist, but you do not have any of the voices at the start of the Chapter

        // PLACEHOLDER
        return null;
    }


    // - Chapter III: The Wraith -

    /**
     * Runs Chapter III: The Wraith
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
     * Runs Chapter II: The Nightmare
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding nightmare() {
        // You gain the Voice of the Paranoid

        this.source = (this.prevEnding == ChapterEnding.TONIGHTMAREFLED) ? "fled" : "normal";
        if (!this.chapter2Intro(true, false, false)) return ChapterEnding.ABORTED;

        manager.setNowPlaying("The Nightmare");
        
        parser.printDialogueLine(new VoiceDialogueLine("The interior of the cabin is plain, the smooth wood of the walls almost featureless. The only furniture of note is a lone table, knocked on its side in the corner of the room. A pristine blade stands between you and the open, inviting basement doorway."));
        parser.printDialogueLine(new VoiceDialogueLine("The blade is your implement. You'll need it if you want to do this right."));
        if (this.sharedLoop) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "Hold on. What happened to the door? There was a door here last time."));
        } else {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "Hold on. What happened to the door?"));
        }
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "It's just an empty frame..."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "She's already gotten out, hasn't she? And she's ready for us. She's been *waiting.* Can't you feel her eyes on us?"));
        parser.printDialogueLine(new VoiceDialogueLine("I'm going to need all of you to pull yourselves together. The Princess has *not* already gotten out, but if you keep getting stuck in your head like this, you're going to struggle to get the job done."));
        parser.printDialogueLine(new VoiceDialogueLine("Yes. So deep breath in, deep breath out. Your task awaits, and only you can do it."));

        
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "mirror", "(Explore) You didn't say anything about the mirror on the wall."));
        activeMenu.add(new Option(this.manager, "different", "(Explore) This whole cabin is different than last time.", this.sharedLoopInsist));
        activeMenu.add(new Option(this.manager, "approach", "(Explore) [Approach the mirror.]"));
        activeMenu.add(new Option(this.manager, "take", "(Explore) [Take the blade.]"));
        activeMenu.add(new Option(this.manager, "enter", "[Enter the basement.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "mirror":
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "You're right. I was so stuck on the the *eyes* watching us that I didn't even notice it there."));
                    parser.printDialogueLine(new VoiceDialogueLine("What are you two talking about? There isn't a mirror. There's the table, the blade sitting on the floor, and the open doorway leading to the basement. There's nothing else in here."));
                    if (this.ch2AskMirror()) {
                        activeMenu.setCondition("approach", false);
                    }
                    break;

                case "different":
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "*Very* different."));
                    if (this.mirrorComment) {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "I'm not the only one who sees her in the window, right? She knows that we're here."));
                    } else {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "*He* changed it, didn't He? It's like He's trying to make us doubt our reality."));
                    }
                    parser.printDialogueLine(new VoiceDialogueLine("Calm down. Maybe the three of you just *think* everything is different because you haven't been here before. Enough of this past-life nonsense. You haven't died, and you certainly haven't been killed by the Princess."));
                    parser.printDialogueLine(new VoiceDialogueLine("So focus up. A lot's riding on this."));
                    break;

                case "cApproachMirror":
                    activeMenu.setCondition("approach", false);
                case "approach":
                    activeMenu.setCondition("mirror", false);
                    this.ch2ApproachMirror();
                    break;

                case "cTake":
                    activeMenu.setCondition("take", false);
                case "take":
                    this.hasBlade = true;
                    this.withBlade = false;
                    parser.printDialogueLine(new VoiceDialogueLine("You reach down and pick the blade up off the floor. It would be difficult to slay the Princess and save the world without a weapon."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Is it gonna be enough, though? Couldn't you have -- given us something else? Something... I don't know... better than a knife? Can we have a bomb?"));
                    parser.printDialogueLine(new VoiceDialogueLine("The blade is the only thing you need to finish your task. You're more than capable of pulling this off so long as you don't lose faith in yourself."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "Those are the words of someone who knows He's sending us to our death..."));
                    break;

                case "cGoStairs":
                case "enter":
                    this.repeatActiveMenu = false;
                    break;

                default:
                    this.giveDefaultFailResponse(activeOutcome);
            }
        }
        
        if (!this.hasBlade) {
            if (this.isHarsh) {
                parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "We should have taken the knife again. I don't think she's going to take an olive branch. We need something sharper."));
            } else {
                parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "We should have taken the knife. I don't think going down there unarmed is going to do us any favors."));
            }

            System.out.println();
        }

        this.currentLocation = GameLocation.STAIRS;
        this.withBlade = false;
        this.mirrorPresent = false;
        parser.printDialogueLine(new VoiceDialogueLine("You cross over the threshold, and onto a series of isolated steps suspended in darkness."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "More eyes, too. You never mention the eyes."));
        parser.printDialogueLine(new VoiceDialogueLine("The air seeping up from below reminds you of fresh lightning and static, as if you're descending into a place that isn't meant for a creature of flesh and blood. If the Princess lives here, slaying her would probably be doing her a favor."));
        parser.printDialogueLine(new VoiceDialogueLine("Her cruel and playful voice prances up the stairs."));
        parser.printDialogueLine(new PrincessDialogueLine("I didn't think you'd come back. We're going to have a lot of fun, you and I."));

        if (this.sharedLoop) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Okay. We need a game plan. Last time we were here, just being close to her was enough to kill us."));
        } else {
            parser.printDialogueLine(new VoiceDialogueLine("\"Come back?\" She must have you confused with someone else."));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "You really don't remember, do you? It doesn't matter. We need a game plan. We *know* we can't just go down there unprepared."));
        }

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "throw", "(Explore) \"How hard is it to throw a knife?\""));
        activeMenu.add(new Option(this.manager, "talk", "I'm going to talk to her."));
        activeMenu.add(new Option(this.manager, "noPlan", "We don't need a plan. I'm just going to kill her. Mr. Narrator seems to think I can do it. I don't know why you're all being such pessimists right now."));
        activeMenu.add(new Option(this.manager, "stepOff", "[Step off into the void between the stairs.]"));
        activeMenu.add(new Option(this.manager, "silent", "[Continue down the stairs in silence.]"));

        boolean lookedBack = false;
        boolean voiceOfReasonComment = false;
        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "throw":
                    if (this.hasBlade) {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "It can't be *that* hard."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "But then we'd lose our weapon. We'd have to make it count. Otherwise she'd be furious and we'd be defenseless. If a knife is enough to even do anything against something like her in the first place..."));
                        parser.printDialogueLine(new VoiceDialogueLine("It'll be enough."));
                    } else {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "You're joking, right? You didn't even *bring* the knife."));

                        if (!lookedBack) {
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "Should we go back for it? *Can* we even go back for it?"));
                            parser.printDialogueLine(new VoiceDialogueLine("You briefly turn back. Where there once was an entrance to the cabin, now there are only more stairs. Hmm. That's not right."));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I guess the only way out of this place is through it."));
                        }
                    }

                    break;

                case "talk":
                    this.repeatActiveMenu = false;
                    parser.printDialogueLine(new VoiceDialogueLine("Didn't you hear my warning a minute ago? She can't be trusted. Talking won't do you any good."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Something tells me she isn't going to be keen on talking anyway."));
                    parser.printDialogueLine(new VoiceDialogueLine("You make your way to the bottom of the stairs."));
                    break;

                case "noPlan":
                    this.repeatActiveMenu = false;
                    voiceOfReasonComment = true;

                    if (this.hasBlade) {
                        parser.printDialogueLine(new VoiceDialogueLine("Finally, a voice of reason. The rest of you should take notes."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "You know why I'm being a pessimist."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "I'm just asking questions."));
                    } else {
                        parser.printDialogueLine(new VoiceDialogueLine("Finally, a voice of reason. The rest of you should take notes. Still, slaying her would be much easier if you had a weapon."));

                        if (!lookedBack) {
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "Should we go back for it? *Can* we even go back for it?"));
                            parser.printDialogueLine(new VoiceDialogueLine("You briefly turn back. Where there once was an entrance to the cabin, now there are only more stairs. Hmm. That's not right."));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I guess the only way out of this place is through it."));
                        }
                    }

                    parser.printDialogueLine(new VoiceDialogueLine("You make your way to the bottom of the stairs."));
                    break;

                case "stepOff":
                    this.repeatActiveMenu = false;
                    parser.printDialogueLine(new VoiceDialogueLine("You attempt to step off the stairs and into the pitch black surrounding them, but you're stopped by an invisible force. Why did you do that? What did you think would happen?"));

                    this.activeMenu = new OptionsMenu(true);
                    activeMenu.add(new Option(this.manager, "curious", "I was curious."));
                    activeMenu.add(new Option(this.manager, "dunno", "I don't know. Falling into an infinite void seemed better than going downstairs and dying. I'm just scared."));
                    activeMenu.add(new Option(this.manager, "silent", "[Say nothing.]"));

                    switch (parser.promptOptionsMenu(activeMenu)) {
                        case "curious":
                            parser.printDialogueLine(new VoiceDialogueLine("Congratulations, you really lucked out. Of all the things that could have happened from stepping into a void, nothing is quite possibly the best outcome you could have gotten."));
                            break;

                        case "dunno":
                            parser.printDialogueLine(new VoiceDialogueLine("How would falling into an infinite void be better than *anything?*"));
                            break;

                        case "silent":
                            break;
                    }

                    parser.printDialogueLine(new VoiceDialogueLine("*Sigh.* You make your way to the bottom of the stairs."));
                    break;

                case "cGoBasement":
                case "silent":
                    this.repeatActiveMenu = false;
                    parser.printDialogueLine(new VoiceDialogueLine("You make your way to the bottom of the stairs."));
                    break;

                case "cGoCabin":
                    if (lookedBack) {
                        parser.printDialogueLine(new VoiceDialogueLine("There is nowhere for you to go but down."));
                    } else {
                        lookedBack = true;
                        parser.printDialogueLine(new VoiceDialogueLine("You turn back the way you came. Where there once was an entrance to the cabin, now there are only more stairs. Hmm. That's not right."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I guess the only way out of this place is through it."));
                    }

                    break;

                default:
                    this.giveDefaultFailResponse(activeOutcome);
            }
        }

        this.currentLocation = GameLocation.BASEMENT;
        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("As you emerge, you find yourself between two loose rows of white wooden planks, suspended in nothingness. A smattering of cobblestones, visible against the inky black of the basement, mark where the floor should be, forming vague pathways. At what seems to be the end of the room, they diverge in opposite directions. Left and right."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "She could be anywhere. And there's nowhere for us to hide. We're completely exposed."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Are you really not going to comment on how *weird* this place is?"));

        if (voiceOfReasonComment) {
            parser.printDialogueLine(new VoiceDialogueLine("No. I'm not."));
        } else {
            parser.printDialogueLine(new VoiceDialogueLine("No, I'm not. Somebody needs to be the voice of reason here, and it certainly isn't you."));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Excuse me? I'm being *incredibly* reasonable. You're the one who's just matter-of-factly describing whatever the hell we're looking at like it's an ordinary basement."));
        }
        
        parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "We're going to die down here. I don't want to die again."));
        parser.printDialogueLine(new VoiceDialogueLine("Please stop saying that. You're only going to make things worse. Just pick a direction and start moving."));
        parser.printDialogueLine(new PrincessDialogueLine("I wouldn't give it too much thought, if I were you. It doesn't really matter, because either way you go, I'm going to find you."));

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "left", "[Go left.]"));
        activeMenu.add(new Option(this.manager, "right", "[Go right.]"));
        activeMenu.add(new Option(this.manager, "nothing", "[Do nothing.]"));
        activeMenu.add(new Option(this.manager, "back", "[Go back the way you came.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "left":
                    this.repeatActiveMenu = false;
                    parser.printDialogueLine(new VoiceDialogueLine("You turn to the left. A faintly outlined path lies before you."));
                    break;

                case "right":
                    this.repeatActiveMenu = false;
                    parser.printDialogueLine(new VoiceDialogueLine("You turn to the right. A faintly outlined path lies before you."));
                    break;

                case "nothing":
                    this.repeatActiveMenu = false;
                    parser.printDialogueLine(new VoiceDialogueLine("You decide it's best to do nothing."));
                    break;

                case "back":
                    this.repeatActiveMenu = false;
                    parser.printDialogueLine(new VoiceDialogueLine("You turn back to the stairs, only to find that they aren't there. Instead, a faintly outlined path lies before you."));
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }
        
        this.withPrincess = true;
        parser.printDialogueLine("A figure appears in the darkness. The only thing you can make out is a porcelain mask with wide, sunken eyes.");
        parser.printDialogueLine(new PrincessDialogueLine("There you are! I told you I was going to find you."));
        parser.printDialogueLine(new VoiceDialogueLine("As the Princess approaches, your legs suddenly go numb."));
        parser.printDialogueLine(new VoiceDialogueLine("Your arms quickly follow."));
        parser.printDialogueLine("As she glides closer, you can begin to make out more details. Her mask is cracked in multiple places, and the rest of her is still shrouded in unnatural shadows, save for her white opera gloves. Her hair floats wildly around her face, and the bottom of her dress is tattered.");
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "This is it, isn't it?"));

        if (this.hasBlade) {
            if (this.isHarsh) {
                parser.printDialogueLine(new PrincessDialogueLine("And you brought your little knife with you again. Cute."));
            } else {
                parser.printDialogueLine(new PrincessDialogueLine("And you brought a little knife with you. Cute."));
            }
        } else {
            if (this.isHarsh) {
                parser.printDialogueLine(new PrincessDialogueLine("No little knife this time? It's almost like... you *want me to get you."));
            } else {
                parser.printDialogueLine(new PrincessDialogueLine("It's almost like... you *want me to get you."));
            }
        }
        
        parser.printDialogueLine(new VoiceDialogueLine("What did you do? Pull yourself together, she isn't supposed to be like this."));
        parser.printDialogueLine(new PrincessDialogueLine("I wonder how many times I'll get to play with you before you break."));
        


        
        // temporary templates for copy-and-pasting
        /*
        parser.printDialogueLine(new VoiceDialogueLine("XXXXX"));
        parser.printDialogueLine(new PrincessDialogueLine("XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "(Explore) XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "\"XXXXX\""));
        */
        
        // PLACEHOLDER
        return null;
    }


    // - Chapter ???: The Moment of Clarity -

    /**
     * Runs Chapter ???: The Moment of Clarity
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding momentOfClarity() {
        // You have all voices

        // PLACEHOLDER
        return ChapterEnding.MOMENTOFCLARITY;
    }


    // - Chapter II: The Razor -

    /**
     * Runs Chapter II: The Razor
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

        manager.setNowPlaying("The Razor");
        
        parser.printDialogueLine(new VoiceDialogueLine("The interior of the cabin is a jagged mess of warped wood and broken boards, their splintered edges as uninviting as shattered glass. The only furniture of note is a pointed table with a pristine blade perched on its edge."));
        parser.printDialogueLine(new VoiceDialogueLine("The blade is your implement. You'll need it if you want to do this right."));

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "mirror", "(Explore) You didn't say anything about the mirror on the wall."));
        activeMenu.add(new Option(this.manager, "different", "(Explore) This whole cabin is different than last time.", this.sharedLoopInsist));
        activeMenu.add(new Option(this.manager, "approach", "(Explore) [Approach the mirror.]"));
        activeMenu.add(new Option(this.manager, "take", "(Explore) [Take the blade.]"));
        activeMenu.add(new Option(this.manager, "enter", "[Enter the basement.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "mirror":
                    parser.printDialogueLine(new VoiceDialogueLine("That's because there isn't a mirror. There's a table, the blade sitting on the table, and the door to the basement. There's nothing else in here."));
                    if (this.ch2AskMirror()) {
                        activeMenu.setCondition("approach", false);
                    }
                    break;

                case "different":
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "*Very* different."));
                    parser.printDialogueLine(new VoiceDialogueLine("Maybe that's because you haven't actually been here. I hope this means you'll finally drop that ridiculous past-life nonsense. You haven't died, and you certainly haven't been killed by the Princess."));
                    parser.printDialogueLine(new VoiceDialogueLine("So focus up. The world is depending on you."));
                    break;

                case "cApproachMirror":
                    activeMenu.setCondition("approach", false);
                case "approach":
                    activeMenu.setCondition("mirror", false);
                    this.ch2ApproachMirror();
                    break;

                case "cTake":
                    activeMenu.setCondition("take", false);
                case "take":
                    this.hasBlade = true;
                    this.withBlade = false;
                    parser.printDialogueLine(new VoiceDialogueLine("You take the blade from the table. It would be difficult to slay the Princess and save the world without a weapon."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "It feels a bit better to have a weapon in our hands. Let's make her hurt for what she's done to us."));
                    break;

                case "cGoStairs":
                case "enter":
                    this.repeatActiveMenu = false;
                    break;

                default:
                    this.giveDefaultFailResponse(activeOutcome);
            }
        }

        if (!this.hasBlade) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Are we really doing this without a weapon? You know she has one, right?"));
            if (this.sharedLoop) parser.printDialogueLine(new VoiceDialogueLine("Once again, I'd like to remind you that she's unarmed. But you're right. This would be a lot easier if you had the blade. I hope you know what you're doing."));
            System.out.println();
        }

        this.withBlade = false;
        this.mirrorPresent = false;
        parser.printDialogueLine(new VoiceDialogueLine("The door to the basement creaks open, revealing what must once have been stairs. The fractured slats look as if they've been torn from their source and violently jammed into the wall."));
        parser.printDialogueLine(new VoiceDialogueLine("The air seeping up from below has an almost metallic quality to it. Like the smell of fresh blood. And you can hear what sounds like the rhythmic scraping of metal coming from down below. If the Princess lives here, slaying her would probably be doing her a favor."));

        if (this.sharedLoop) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "That's right, *scraping.* I told you she has something. I *told* you."));
        } else {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Scraping? She's not even trying to hide her knife. It's like she wants to get in our head."));
        }

        parser.printDialogueLine(new VoiceDialogueLine("That sound could be anything. It's probably just her chains dragging across the floor. I am begging you to get out of your head."));
        parser.printDialogueLine(new VoiceDialogueLine("Her grating voice carries up the stairs."));
        parser.printDialogueLine(new PrincessDialogueLine("I hope you've come to rescue me. I've been stuck down here *forever.*"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "There's something so wrong with that voice."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Yeah. She thinks she's better than us. Like she doesn't even have to put on an act this time."));

        this.currentLocation = GameLocation.BASEMENT;
        this.withPrincess = true;
        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("As you descend the final step, the form of the Princess comes into view, her sharp eyes following you from across the room."));
        parser.printDialogueLine("Her hair somehow looks sharper than last time, and the bottom of her dress is ripped. She stands with her hands behind her back.");
        if (this.sharedLoop) parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I wonder if she remembers us."));
        parser.printDialogueLine(new PrincessDialogueLine("Finally, *somebody!* Quick, get me out of these chains, we're not safe here."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Come on, now. We're not falling for that, are we? She's trying to trick us, but she can't hide that threatening edge to her voice. She just wants us to get close, to let our guard down."));

        if (this.hasBlade) {
            parser.printDialogueLine(new VoiceDialogueLine("If she sounds threatening, it's because her mask is already slipping. She knows why you're here. You *are* armed, after all."));
        } else {
            parser.printDialogueLine(new VoiceDialogueLine("Exactly. She sounds threatening because her mask is already slipping. She knows why you're here."));
        }

        parser.printDialogueLine(new PrincessDialogueLine("What are you waiting for? You are here to rescue me, right?"));

        String honestText = "(Explore) \"What if we're both honest with each other? I was sent here to stop you from ending the world, and you ";
        if (source.equals("revival")) {
            honestText += "killed me last time after coming back from being stabbed in the heart.\"";
        } else {
            honestText += "slashed my throat last time.\"";
        }

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "knife2", "(Explore) \"Prove it then. Prove that you don't have a knife.\"", activeMenu.get("knife")));
        activeMenu.add(new Option(this.manager, "knife3", "(Explore) \"But what if you're just hiding it somewhere secret?\"", activeMenu.get("knife2")));
        activeMenu.add(new Option(this.manager, "stab", "(Explore) \"If I come close to you, you're just going to stab me, aren't you?\""));
        activeMenu.add(new Option(this.manager, "lastTime1", "(Explore) \"Do you remember what happened last time?\""));
        activeMenu.add(new Option(this.manager, "lastTime2", "(Explore) \"But that's exactly what happened! So you do remember it.\"", activeMenu.get("lastTime1")));
        activeMenu.add(new Option(this.manager, "knife", "(Explore) \"I know you have a knife. I'll let you out of here if you drop it.\""));
        activeMenu.add(new Option(this.manager, "suspicious", "(Explore) \"I'd like to trust you, but you're being so suspicious right now!\""));
        activeMenu.add(new Option(this.manager, "talk", "(Explore) \"Can we just talk things through?\""));
        activeMenu.add(new Option(this.manager, "key", "(Explore) \"I don't have the key.\""));
        activeMenu.add(new Option(this.manager, "goodWill", "(Explore) \"I don't have a weapon. Isn't that a sign of good will?\"", !this.hasBlade));
        activeMenu.add(new Option(this.manager, "lastKill", "(Explore) \"We killed each other last time. I'd rather not do that again.\"", source.equals("mutual")));
        activeMenu.add(new Option(this.manager, "mad", "(Explore) \"Look, I know that you're mad at me but I think we both just need to let bygones be bygones.\""));
        activeMenu.add(new Option(this.manager, "happened", "(Explore) \"What happened after you died last time?\"", source.equals("mutual")));
        activeMenu.add(new Option(this.manager, "honest", honestText));
        activeMenu.add(new Option(this.manager, "nobody", "(Explore) \"Nobody has to die.\""));
        activeMenu.add(new Option(this.manager, "letOut", "(Explore) \"Okay. What are you going to do if I let you out?\""));
        activeMenu.add(new Option(this.manager, "activities", "(Explore) \"Activities like stabbing or cutting or murdering?\"", activeMenu.get("letOut")));
        activeMenu.add(new Option(this.manager, "approach", "\"Okay, fine. I'm coming closer to free you. You'd better not try anything.\" [Approach the Princess.]"));
        activeMenu.add(new Option(this.manager, "bye", "\"Yeah, I'm not trusting this. Bye!\" [Turn and leave.]"));
        activeMenu.add(new Option(this.manager, "rightBack", "\"I'll be right back. I left something upstairs.\" [Go retrieve the blade upstairs.]", !this.hasBlade));
        activeMenu.add(new Option(this.manager, "slay", "\"Screw it.\" [Slay the Princess.]", this.hasBlade));


        boolean lastTimeFlag = false;
        boolean loopComment = false;
        boolean followUpFlag = false;
        int followUpCount = 0;
        
        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            if (lastTimeFlag && !loopComment) {
                loopComment = true;
                
                if (this.sharedLoopInsist) {
                    parser.printDialogueLine(new VoiceDialogueLine("There you go again, talking up delusions about a past-life experience that clearly didn't happen."));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine("\"Last time?\" What are you talking about?"));
                }

                parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Ugh. It's like the two of you are working together on this. Aren't you listening to her? She's obviously lying through her teeth."));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I'm terrible at spotting liars and even I can tell something's up here. We can't be the only ones who looped back to the start. Someone else has to remember, right?"));
                parser.printDialogueLine(new VoiceDialogueLine("Yes. Something is obviously \"up\" and we can all tell that she's lying. But the thing she's lying about is how dangerous she is, not \"dimension-hopping\" or \"time-travel\" or whatever it is you think you're doing."));
            } else if (followUpFlag) {
                switch (followUpCount) {
                    case 1:
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "I have absolutely zero doubts that she is going to stab us if we get close to her."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "She certainly feels threatening."));
                        parser.printDialogueLine(new VoiceDialogueLine("Just because she's acting like she's going to stab you doesn't mean she has the means to actually do it."));

                        if (this.hasBlade) {
                            parser.printDialogueLine(new VoiceDialogueLine("But you know who is armed? You. So stop second guessing yourself and do your job."));
                        } else {
                            parser.printDialogueLine(new VoiceDialogueLine("But you know who has the capacity to quickly arm himself? You do. So stop second guessing yourself, go upstairs, take the blade, and do your job."));
                        }

                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "But I'm nervous."));
                        parser.printDialogueLine(new VoiceDialogueLine("All the more reason to jump into the deep end and deal with her right now, before you waste any more time getting stuck in your head."));
                        break;

                    case 2:
                        parser.printDialogueLine(new VoiceDialogueLine("How many more times does she have to vaguely threaten you before you to finally decide you're ready to deal with her?"));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "We're clearly still figuring out our angle. We don't have the luxury of watching this from a distance."));
                        parser.printDialogueLine(new VoiceDialogueLine("Oh, I'm sorry, do you think I'm in a position of luxury right now?"));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "You're acting like you are."));
                        parser.printDialogueLine(new VoiceDialogueLine("My entire world is at risk."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Then maybe you should behave with a little more humility. A bit of self-deprecation would go a long way."));
                        parser.printDialogueLine(new VoiceDialogueLine("No. I have my dignity."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Fine. Then we'll continue to treat you exactly how you deserve to be treated."));
                        break;

                    case 3:
                        parser.printDialogueLine(new VoiceDialogueLine("I think I've said my piece at this point."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "I think we all have. But if you want to keep exhausting your questions, it beats getting stabbed to death."));
                        break;

                    case 4:
                        parser.printDialogueLine(new PrincessDialogueLine("Okay. I'm bored now."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "She's bored...?"));
                        parser.printDialogueLine(new VoiceDialogueLine("That's absurd. She doesn't get to be bored. Not in a way that matters. She's a *prisoner.* She's --", true));
                        return this.razorInitiative(this.hasBlade, false);
                }
                
                followUpFlag = false;
                followUpCount += 1;
            }


            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "knife2":
                    parser.printDialogueLine(new PrincessDialogueLine("It would be so much easier to prove that I *do* have a sharp object. I could just show it to you! But I don't have one, so I can't."));
                    parser.printDialogueLine(new VoiceDialogueLine("The Princess smiles as she pulls her hands from behind her back."));
                    parser.printDialogueLine(new PrincessDialogueLine("But look at this! Hands! Hands that don't have *anything* in them to stab you with."));
                    parser.printDialogueLine(new VoiceDialogueLine("Her smile stretches into an even wider grin as she shakes her sleeves."));
                    parser.printDialogueLine(new PrincessDialogueLine("And *empty sleeves* too! Look at how few stabbing implements I have, it's practically zero."));
                    break;

                case "knife3":
                    parser.printDialogueLine(new PrincessDialogueLine("I've shown you all of my hiding spots! What kind of Princess do you think I am? I would never hide something sharp somewhere secret."));
                    parser.printDialogueLine(new PrincessDialogueLine("Wait, that sounds like I'm lying, but I'm actually not. My secret zones are for me only, they have nothing to do with you or my intention to not-stab you to death the second you get close to me."));
                    parser.printDialogueLine(new VoiceDialogueLine("Her smile drops for a moment, her expression sharp and flat."));
                    parser.printDialogueLine(new PrincessDialogueLine("I assure you, there's nothing hidden there."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I'm inclined to believe her on that one. She seems serious."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Of course, but that doesn't mean that she doesn't have something hidden *somewhere.* We know for a fact she's armed."));
                    break;

                case "stab":
                    followUpFlag = true;
                    parser.printDialogueLine(new PrincessDialogueLine("What? *Noooo!* No, I wouldn't stab you. I am just a sweet in-no-cent Princess, trapped here for no reason!"));
                    parser.printDialogueLine(new PrincessDialogueLine("And you are a brave knight who is supposed to walk up to... not-stabbing-distance to help me."));
                    break;

                case "lastTime1":
                    lastTimeFlag = true;

                    switch (this.source) {
                        case "mutual":
                            parser.printDialogueLine(new PrincessDialogueLine("Last time? If somebody came into *my* house and tried to kill me and I cut his neck open and then he stabbed me in the heart and we both died looking in each other's eyes, well, surely I would remember that!"));
                            break;

                        case "revival":
                            parser.printDialogueLine(new PrincessDialogueLine("Last time? If somebody came into *my* house and stabbed *me* to death and then I *killed him,* surely I would remember that!"));
                            break;

                        case "pathetic":
                            parser.printDialogueLine(new PrincessDialogueLine("Last time? If somebody came into *my* house and tried to kill me and I cut his neck open, surely I would remember that!"));
                            break;
                    }

                    parser.printDialogueLine(new PrincessDialogueLine("But I *don't* remember it! So it must not have happened."));
                    break;

                case "lastTime2":
                    parser.printDialogueLine(new PrincessDialogueLine("Would I just *lie?* Would I just lie to your face and tell you that a thing I remembered happening didn't happen just so I could *stab you again?*"));
                    parser.printDialogueLine(new PrincessDialogueLine("I mean, just so I could stab you for the *first* time."));

                    if (loopComment) {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Aha! She slipped up there! She said again. And her taking it back doesn't count."));
                        parser.printDialogueLine(new VoiceDialogueLine("Do you hear how deranged you sound right now? Please just stop dawdling. This is only going to end with violence. Postponing the inevitable is only going to make it worse for you when it actually happens."));
                    }
                    
                    break;

                case "knife":
                    parser.printDialogueLine(new PrincessDialogueLine("A *knife?* What are you *talking about?* I don't have a knife, where would I keep a knife?"));
                    parser.printDialogueLine(new PrincessDialogueLine("And *why* would I stab you to death? I don't know you. You haven't given me a *reason* to stab you to death. It would be *so silly* of me to cut you open and look at your insides."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Okay, I could have sworn we didn't mention stabbing anyone to death."));
                    parser.printDialogueLine(new VoiceDialogueLine("It sounds like she's really out for blood. Fortunately for you, she isn't armed."));
                    break;

                case "suspicious":
                    followUpFlag = true;
                    parser.printDialogueLine(new PrincessDialogueLine("That's... so *rude* of you, passing judgments on strangers you've never met just because they're different from you! How would you like it if *I* did that, *huh?*"));

                    if (this.hasBlade) {
                        parser.printDialogueLine(new PrincessDialogueLine("Silly little birdface thinks he's *so serious* coming down here but doesn't know *anything!* Thinks he can tell me to get rid of all the *knives* I don't even *have* while he gets to wave one around right in front of me!"));
                    } else {
                        parser.printDialogueLine(new PrincessDialogueLine("Silly little birdface thinks he's *so serious* coming down here but doesn't know *anything!* He doesn't even have a knife for stabbing!"));
                    }
                    
                    parser.printDialogueLine(new PrincessDialogueLine("I bet you didn't *like* that, *did* you? I bet you didn't *like* being judged for no reason."));
                    break;

                case "talk":
                    followUpFlag = true;
                    parser.printDialogueLine(new PrincessDialogueLine("But we *don't* have anything to talk through! We're *strangers* and this place is *cramped* and *annoying* and you should just come over here and let me out."));
                    break;

                case "key":
                    followUpFlag = true;
                    parser.printDialogueLine(new PrincessDialogueLine("Ohh, you don't, okay, I see."));
                    parser.printDialogueLine(new PrincessDialogueLine("I have an idea! You should come over here and stare directly at the chains! You won't be able to find the key if you don't know what it's supposed to look like, so you'd better come *right* within close staring distance, *just* to be sure!"));
                    break;

                case "goodWill":
                    parser.printDialogueLine(new PrincessDialogueLine("*Yes!* Yes, of *course!* Haven't you seen that I've only been nice to you since you got here?"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Has she?"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Her words have been fine but her general attitude's been weird and extremely suspicious, and I think that's what really counts."));
                    parser.printDialogueLine(new VoiceDialogueLine("This shouldn't even be a question. She absolutely has not been nice to you."));
                    break;

                case "lastKill":
                    lastTimeFlag = true;
                    parser.printDialogueLine(new PrincessDialogueLine("But if we killed each other, then why are we here, right now, both of us normal and un-stabbed?"));

                    if (!loopComment) {
                        parser.printDialogueLine(new VoiceDialogueLine("I do have to hand it to her, that's a very good question, and it's one with a simple answer: you haven't slain her yet. So how about you get moving?"));
                    }

                    break;

                case "mad":
                    followUpFlag = true;
                    parser.printDialogueLine(new PrincessDialogueLine("But I'm not mad at you! So please stop standing so far out of reach."));
                    break;

                case "happened":
                    lastTimeFlag = true;
                    parser.printDialogueLine(new PrincessDialogueLine("That's funny! You're *funny!* I'm not dead, so I *can't* have died!"));
                    break;

                case "honest":
                    lastTimeFlag = true;
                    followUpFlag = true;
                    parser.printDialogueLine(new PrincessDialogueLine("Hahaha! *That* doesn't sound like me!"));
                    break;

                case "nobody":
                    followUpFlag = true;
                    parser.printDialogueLine(new PrincessDialogueLine("Of *course not!* At least not *now.* Because *you're* here to save me. But you'll have to come close!"));
                    break;

                case "letOut":
                    parser.printDialogueLine(new PrincessDialogueLine("All *sorts* of things, which is why I think that's a great idea! I would *love* to not be chained up down here. Being chained up is so *boring* and I crave fresh and new activities to broaden my horizons!"));
                    parser.printDialogueLine(new VoiceDialogueLine("Please don't let her out of here."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Believe it or not, I think I'm actually with Him on this."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Okay, but what if all of this is just a misunderstanding? There has to be room for this to be a misunderstanding."));
                    break;

                case "activities":
                    parser.printDialogueLine(new PrincessDialogueLine("*Yes!* I mean, maybe! I've never done *any* of those things, but there is something alluring about the sound of it."));
                    parser.printDialogueLine(new PrincessDialogueLine("I think it would also be fun to do *other* activities like... look at a bird, or touch a tree."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Okay, now listen."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "You're not actually buying this, are you?"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Listen."));
                    parser.printDialogueLine(new VoiceDialogueLine("... Yes?"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I would like to look at a bird."));
                    parser.printDialogueLine(new VoiceDialogueLine("You can look at a bird later."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "But if we look at a bird *now* we wouldn't have to be *here.*"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "He has a point."));
                    parser.printDialogueLine(new VoiceDialogueLine("Just make a *decision* already!"));
                    break;


                case "approach":
                    return this.razorApproach(this.hasBlade);

                case "cGoStairs":
                    parser.printDialogueLine(new VoiceDialogueLine("You've barely even begun to turn around when she speaks again."));
                case "bye":
                case "rightBack":
                    parser.printDialogueLine(new PrincessDialogueLine("It's boring if you leave."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "She's bored...?"));
                    parser.printDialogueLine(new VoiceDialogueLine("That's absurd. She doesn't get to be bored. Not in a way that matters. She's a *prisoner.* She's --", true));
                    return this.razorInitiative(this.hasBlade, true);

                case "cSlayPrincess":
                case "slay":
                    this.razorSlay();
                    return ChapterEnding.TOARMSRACEFIGHT;

                case "cSlayPrincessNoBladeFail":
                    parser.printDialogueLine(new VoiceDialogueLine("*Sigh.* You left the blade upstairs, remember? You can always go back and retrieve it, but as of *now,* you have no weapon with which to slay her."));
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        throw new RuntimeException("No ending found");
    }

    /**
     * The Razor takes initiative and attacks the player, ending the Chapter
     * @param bladePath whether the player took the blade in The Razor (leading to The Arms Race) or not (leading to No Way Out)
     * @param leaveAttempt whether the player reached this ending by attempting to leave (giving them the Voice of the Paranoid instead of the Voice of the Broken)
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding razorInitiative(boolean bladePath, boolean leaveAttempt) {
        parser.printDialogueLine(new VoiceDialogueLine("Shit. In a sudden burst of movement, the Princess leaps towards you, a blade erupting from her free arm, her wrist limp and empty from the violent expulsion. Huh. So I guess she did have a knife of her own after all."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "How conciliatory of you. We appreciate it, really. Now what are we going to do?!"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "At least we're safe here. She's still in chains."));
        parser.printDialogueLine(new VoiceDialogueLine("And those chains stop her from continuing her advance, at least for a moment."));
        parser.printDialogueLine(new VoiceDialogueLine("She looks down at them with something between annoyance and confusion."));
        parser.printDialogueLine(new VoiceDialogueLine("And then she slices through her arm."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Okay. Maybe we aren't safe here."));
        parser.printDialogueLine(new VoiceDialogueLine("She doesn't even hesitate before darting towards you with a terrifying speed you can't hope to outpace."));

        if (this.hasBlade) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Ah, shit. Okay. She's down an arm and we still have a weapon. I guess we'll have to use it."));
            parser.printDialogueLine(new VoiceDialogueLine("And use it you do. But unfortunately for you, and for the entire world, you are horribly outmatched."));
            parser.printDialogueLine(new VoiceDialogueLine("You keep pace with her for a single brief and wordless exchange before she severs your hand, and with it, your only line of defense."));

            if (source.equals("revival")) {
                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "She's much better at this than she was last time."));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Yeah. It's unreal. Bloody cheater."));
            } else {
                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "She's even better at this than she was last time."));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Bloody cheater."));
            }
        } else {
            parser.printDialogueLine(new VoiceDialogueLine("You sprint for the stairs, but I wasn't exaggerating when I said she was running at you with a terrifying speed you couldn't hope to outpace."));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "We don't make it, do we?"));
            parser.printDialogueLine(new VoiceDialogueLine("No. You feel her blade in your back before you make it to the first stair."));
        }

        parser.printDialogueLine(new PrincessDialogueLine("I'm going to kill you now."));
        parser.printDialogueLine(new VoiceDialogueLine("And with a squelch, she does just that."));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("Everything goes dark, and you die."));

        if (bladePath) {
            if (leaveAttempt) {
                return ChapterEnding.TOARMSRACELEFT;
            } else {
                return ChapterEnding.TOARMSRACEBORED;
            }
        } else {
            if (leaveAttempt) {
                return ChapterEnding.TONOWAYOUTLEFT;
            } else {
                return ChapterEnding.TONOWAYOUTBORED;
            }
        }
    }
    
    /**
     * The player chooses to approach the Razor, ending the Chapter and giving them the Voice of the Broken
     * @param bladePath whether the player took the blade in The Razor (leading to The Arms Race) or not (leading to No Way Out)
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding razorApproach(boolean bladePath) {
        parser.printDialogueLine(new VoiceDialogueLine("Against your better judgment, you walk across the room to within arm's reach of the Princess."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "I don't like the way you said \"within arm's reach.\""));
        parser.printDialogueLine(new VoiceDialogueLine("You hear the horrible sound of metal slicing through meat."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "... Whose meat? Not ours, right?!"));
        parser.printDialogueLine(new VoiceDialogueLine("Hers, at first."));
        parser.printDialogueLine(new VoiceDialogueLine("Then yours. Your neck, specifically."));
        if (!source.equals("revival")) parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Again? Really?!"));
        parser.printDialogueLine(new PrincessDialogueLine("Hehe!"));
        parser.printDialogueLine(new VoiceDialogueLine("You collapse to the ground, your vision swimming as you attempt to focus on her bloody blade and the limp sack of flesh that was once her arm."));
        parser.printDialogueLine(new PrincessDialogueLine("You're going to die now."));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("And with a quick jerk of her elbow, she does just that. Everything goes dark, and you die."));

        if (bladePath) {
            return ChapterEnding.TOARMSRACEBORED;
        } else {
            return ChapterEnding.TONOWAYOUTBORED;
        }
    }

    /**
     * The player chooses to fight the Razor, leading to Chapter III: The Arms Race and giving them the Voice of the Stubborn
     */
    private void razorSlay() {
        parser.printDialogueLine(new VoiceDialogueLine("The Princess falls silent, her smile unwavering as you charge across the room."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Okay. She hasn't pulled out a knife yet. And her hands are still behind her back. I think we can do this. I think we can win. We just have to strike now, but make sure you keep your eyes on those hands, I don't trust her for a second."));
        parser.printDialogueLine(new VoiceDialogueLine("But your focus is broken by the horrible sound of metal slicing through meat."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "... Whose meat? Not ours, right?!"));
        parser.printDialogueLine(new VoiceDialogueLine("Hers, at first."));
        parser.printDialogueLine(new VoiceDialogueLine("Then yours."));
        parser.printDialogueLine(new PrincessDialogueLine("Hehe!"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "How?! What did she even hit us with?"));
        parser.printDialogueLine(new VoiceDialogueLine("You stare down at your chest, and at the long, thin blade she impaled you with."));
        parser.printDialogueLine(new VoiceDialogueLine("And then at the red, angry slit along the flesh of her thigh, where the blade had been nestled just a moment ago. It's still lodged in her leg, emerging from her knee, hinging up and out of her body like some extra metallic limb."));
        parser.printDialogueLine(new PrincessDialogueLine("You're going to die now."));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("With a twist of her knee and a painful squelch, she does just that. Everything goes dark, and you die."));
    }


    // - Chapter III: The Arms Race / No Way Out -

    /**
     * Runs the opening sequence of Chapter III: The Arms Race / No Way Out
     * @param bladePath whether the player took the blade in The Razor (leading to The Arms Race) or not (leading to No Way Out)
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding razor3Intro(boolean bladePath) {
        /*
          Possible starting combinations for The Arms Race:
            - Cheated + Hunted + Stubborn
            - Cheated + Hunted + Broken
            - Cheated + Hunted + Paranoid

          Possible starting combinations for No Way Out:
            - Cheated + Contrarian + Broken
            - Cheated + Contrarian + Paranoid
         */

        parser.printDialogueLine(new VoiceDialogueLine("You're on a path in the woods --", true));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "No, *fuck* that! If we're going to have to keep doing this over and over and over again, we're not starting in the *goddamn woods* every time. We're starting in the *fucking cabin!*"));
        parser.printDialogueLine(new VoiceDialogueLine("You're what?!", true));
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException("Thread interrupted");
        }

        parser.printDialogueLine(new VoiceDialogueLine("The interior of the cabin is sharp, a constricting mess of curved and battered sheet metal pushing you towards -- wait, excuse me?! What just happened? What did you just do?"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I feel dizzy."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Ohohoho. I guess I took us to the cabin, didn't I? Isn't *that* interesting. Who holds the cards now?"));

        if (bladePath) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "The circle's getting smaller and smaller. Running isn't an option anymore. We have to fight."));

            switch (this.ch3Voice) {
                case STUBBORN:
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "Good. It's better that way. Without a fight, no one can win, and if no one can win, then nothing has any meaning."));
                    break;
                case BROKEN:
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "What's the point of fighting if she's just going to win every time? It hurts being sliced to pieces. We're better off just sitting up here and doing nothing."));
                    break;
                case PARANOID:
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "Yeah, but for whom? Someone or something is out there pulling the strings, and we're all just puppets until we can figure out how to see them."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "But what if that someone is *us?* Eh? Eh? Wouldn't that be neat."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "If we were the ones pulling the strings, I'm pretty sure we wouldn't have died twice already."));
                    break;
            }
        } else {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Who cares about cards? You're all acting like this is about winning and losing, while this is *actually* about having fun."));

            switch (this.ch3Voice) {
                case BROKEN:
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "Is this fun for you? It's not fun for me. I don't like being sliced to pieces."));
                    break;
                case PARANOID:
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "How could you care about having fun at a time like this? There is someone or something out there pulling the strings, and we're all just puppets until we can figure out how to see them."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "But what if that someone is *us?* Eh? Eh? Wouldn't that be neat."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "If we were the ones pulling the strings, I'm pretty sure we wouldn't have died twice already."));
                    break;
            }
        }
        
        parser.printDialogueLine(new VoiceDialogueLine("Great. So obviously you've already been here. How many times?"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "This is our third?"));
        parser.printDialogueLine(new VoiceDialogueLine("No wonder things have fallen apart. You do realize that every time you fail, she escapes and an entire world is damned to destruction, right?"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "That can't be right. That's too much responsibility!"));
        
        switch (this.ch3Voice) {
            case STUBBORN:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "Nah, impossibly high stakes make the fight so much better."));
                break;
            case BROKEN:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "I couldn't agree more. We couldn't be trusted with the fate of a single person, let alone the fate of the world."));
                break;
            case PARANOID:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "It's only too much responsibility if these \"worlds\" are real."));
                break;
        }
        
        parser.printDialogueLine(new VoiceDialogueLine("*Sigh.* Let's just stay focused, shall we?"));
        parser.printDialogueLine(new VoiceDialogueLine("The only furniture of note is a bent metal table, a pristine blade perched --", true));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "We take it."));
        parser.printDialogueLine(new VoiceDialogueLine("Okay. Sure. You take the blade before letting me finish telling you it's there. It would be difficult to slay the Princess and save the world without a weapon."));

        if (bladePath) {
            this.hasBlade = true;

            parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "This feels right. We just have to keep our senses sharp."));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "That's right. We've got to be able to win eventually."));

            switch (this.ch3Voice) {
                case STUBBORN:
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "We *will* win eventually. Hell, we might even win now."));
                    parser.printDialogueLine(new VoiceDialogueLine("That's a fighting spirit I like to see. You could all learn a thing or two from this one."));
                    break;
                case BROKEN:
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "And what if we never do?"));
                    parser.printDialogueLine(new VoiceDialogueLine("So. Are you just going to stand there, or are you going to head to the basement like you're supposed to?"));
                    break;
                case PARANOID:
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "What if winning is the wrong move?"));
                    parser.printDialogueLine(new VoiceDialogueLine("It isn't. I don't care whether you trust me or not, but at least trust that defeating the person who has apparently killed you twice already is a good idea."));
                    break;
            }
        } else {
            this.threwBlade = true;

            parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "And then we throw it out the window!"));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "What?! That blade is the only edge we have, we are not --", true));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Too late! Because... we already did it, didn't we?"));
            parser.printDialogueLine(new VoiceDialogueLine("... Unfortunately for the rest of you, and for me, and for the sake of the world... yes."));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "That is *horribly* unfair! He shouldn't be allowed to just do things like that!"));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "You were the one who made us pick it up! See? You're not the only one who can figure out how to do things."));
            parser.printDialogueLine(new VoiceDialogueLine("What's done is done. I suggest you make the best of it."));

            switch (this.ch3Voice) {
                case BROKEN:
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "You're all so mad at each other. I'm just going to sit here quietly in the corner. You can be the ones to figure out what to do."));
                    break;
                case PARANOID:
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "Oh. Is this how things are going to be now? All of us vying over a single body? Fine. See this corner? It's mine. And I'd better not see any of you trying to invade my personal space."));
                    break;
            }

            parser.printDialogueLine(new VoiceDialogueLine("So. Are you just going to stand there, or are you going to head to the basement like you're supposed to?"));
        }
        
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "I'd love to get started just as much as you would, but how *are* we supposed to get down there?"));
        parser.printDialogueLine(new VoiceDialogueLine("You walk through the door. You do know what doors are, right?"));

        if (this.mirrorComment || this.touchedMirror) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "But there isn't a door. It's just that mirror again."));
        } else {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "But there isn't a door. There's just that mirror."));
        }
        
        parser.printDialogueLine(new VoiceDialogueLine("There isn't a mirror. You really messed things up, didn't you? It's like you can't even see reality anymore."));

        switch (this.ch3Voice) {
            case STUBBORN:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "If it's in our way, let's just break it and move on."));
                break;
            case BROKEN:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "I can feel the air coming up from behind it, stinking of iron and steel. He might be right. Could be a trick. If our other senses can't feel it, then we can't trust it."));
                break;
            case PARANOID:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "We can see our reality just fine. Why should we trust Him?"));
                break;
        }

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "approach", "[Approach the mirror.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "cGoStairs":
                case "cApproachMirror":
                case "approach":
                    this.repeatActiveMenu = false;
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }
        
        parser.printDialogueLine(new VoiceDialogueLine("You make your way to the door at the end of the room, stopping just in front of it. You really must think you're looking at a \"mirror.\" Well, it doesn't exist. *Sigh.* Just reach forward and open it."));
        if (this.hasVoice(Voice.BROKEN)) parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "Let's just move it out of the way without looking. I don't want to see us. I'm sure we all look awful after dying twice."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Let's just fumble for the handle and be done with it, I don't care what we look like. I care about getting to the end of this mess."));

        this.currentLocation = GameLocation.MIRROR;
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "wipe", "[Wipe the mirror clean.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "wipe":
                    this.repeatActiveMenu = false;
                    break;

                default: super.giveDefaultFailResponse(activeOutcome);
            }
        }

        this.mirrorPresent = false;
        if (this.hasVoice(Voice.BROKEN)) parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "*Long, drawn-out sigh...*"));
        parser.printDialogueLine(new VoiceDialogueLine("You reach forward and place your hand on the door to the basement. It creaks open."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "And the mirror's gone. How surprising."));

        if (bladePath) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "It was never there. Just an illusion."));
        } else {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "I can't say I was particularly invested in looking at it before, but now... now I really want to see what's in it. If it's so keen on hiding from us, whatever it has must be *real good.*"));
        }

        switch (this.ch3Voice) {
            case STUBBORN:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "Let's just get to the Princess already. I didn't care about the mirror before, and I care about it even less now."));
                break;
            case PARANOID:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "It feels like it's hiding something. It's part of the big picture, I just know it. That's why it's being kept from us."));
                break;
        }
        
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I guess it's time for us to see her again."));
        parser.printDialogueLine(new VoiceDialogueLine("Just stay focused, and you'll be fine."));

        this.currentLocation = GameLocation.BASEMENT;
        this.withPrincess = true;
        this.canSlayPrincess = true;
        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("You step forward, but you don't get a chance to linger on the basement stairs. They are smooth and flat and metallic, an unintentional and unfortunately slippery ramp that quickly sends you skittering to the bottom."));
        parser.printDialogueLine(new VoiceDialogueLine("Your body tumbles onto the basement floor, and the form of the Princess comes into view, standing at a distance. She gives you a wry smile."));
        parser.printDialogueLine(new PrincessDialogueLine("Hi! It looks like you don't have a way out, so I'm not going to play dumb anymore."));

        // Create the options menu used in both chapters here, then pass it into the basement methods; the menu is almost identical in both chapters anyway
        // Your choice in this menu determines which voice you get after dying
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "coldA", "We're going to fight her again, and we're going to have a stiff upper lip about it. She can't hurt us if we don't let ourselves feel it.", this.hasVoice(Voice.STUBBORN)));
        activeMenu.add(new Option(this.manager, "stubborn", "We're fighting her, obviously.", !this.hasVoice(Voice.STUBBORN)));
        activeMenu.add(new Option(this.manager, "oppo", "We're going to appeal to her authority. Puff her up a bit. There's no reason we can't talk this out."));
        activeMenu.add(new Option(this.manager, "broken", "We're going to unconditionally surrender.", !this.hasVoice(Voice.BROKEN)));
        activeMenu.add(new Option(this.manager, "hunted", "I'm going to go with not letting her stab us. We can dodge, right?", !this.hasVoice(Voice.HUNTED)));
        activeMenu.add(new Option(this.manager, "smitten", "Oh, that's easy. I'm going to try flirting with her."));
        activeMenu.add(new Option(this.manager, "para", "She has swords for arms and we don't. We're panicking!", !this.hasVoice(Voice.PARANOID)));
        activeMenu.add(new Option(this.manager, "coldB", "We're going to fight her again, and we're going to have a stiff upper lip about it. She can't hurt us if we don't let ourselves feel it.", bladePath && !this.hasVoice(Voice.STUBBORN)));
        activeMenu.add(new Option(this.manager, "coldNWO", "We're going to let her stab us, and we're going to have a stiff upper lip about it. She can't hurt us if we don't let ourselves feel it.", !bladePath));
        activeMenu.add(new Option(this.manager, "contra", "She wins by killing us, right? So let's beat her to it!", !this.hasVoice(Voice.CONTRARIAN)));
        activeMenu.add(new Option(this.manager, "skeptic", "[All of these ideas suck. Think up something better.]"));

        if (bladePath) {
            this.armsRaceBasement();
            return ChapterEnding.TOMUTUALLYASSURED;
        } else {
            this.noWayOutBasement();
            return ChapterEnding.TOEMPTYCUP;
        }
    }

    /**
     * Runs the basement section of Chapter III: The Arms Race
     */
    private void armsRaceBasement() {
        parser.printDialogueLine(new PrincessDialogueLine("But don't worry about how bad you did last time. That's part of the fun!"));

        switch (this.ch3Voice) {
            case STUBBORN:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "She's got another thing coming if she thinks we're going down easy again."));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "Pride makes us dead. The only thing that matters is survival."));
                break;
            case BROKEN:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "Fun for her, maybe. I didn't like dying all over again."));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "Thinking about dying makes us as good as dead. The only thing that matters is survival."));
                break;
            case PARANOID:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "She's toying with us. She's acting like she already knows she's won."));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "Thinking about dying makes us as good as dead. The only thing that matters is survival."));
                break;
        }
        
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Actually, does survival matter? We've died twice and nothing bad has come of it. We just need to find a way to win once."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Nothing bad has come of it *yet.*"));
        parser.printDialogueLine(new VoiceDialogueLine("Plenty bad has come of it! You've left at least one entire world to ruin. The people there mattered."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "The past isn't real. There's only here and now."));
        parser.printDialogueLine(new VoiceDialogueLine("Your internal bickering is cut short by the wet sound of slicing meat. From the Princess's arms erupt twin blades, glistening with her blood, the empty flesh of her arms flopping at her elbows like torn sleeves. The chain clatters to the floor."));
        parser.printDialogueLine(new VoiceDialogueLine("She's loose, and she is coming for you."));
        parser.printDialogueLine(new PrincessDialogueLine("You're going to make me walk over to you, aren't you?"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Shit, she's coming for us, and I'm out of ideas."));

        OptionsMenu subMenu;

        // Remember, activeMenu was set up at the end of razor3Intro()
        this.repeatActiveMenu = true;
        this.canSlaySelf = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);

            // Handle "slay" command here, redirecting to different options in the menu based on which fight options are available
            if (activeOutcome.equals("cSlayPrincess")) {
                if (!this.hasVoice(Voice.STUBBORN)) {
                    this.activeOutcome = "stubborn";
                } else if (!this.hasVoice(Voice.COLD)) {
                    this.activeOutcome = "coldA";
                } else {
                    this.activeOutcome = "noFightOptions"; // Fails
                }
            }

            switch (activeOutcome) {
                case "coldA":
                    this.canSlayPrincess = false;
                    activeMenu.setCondition("coldB", false);
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "Hahahaha yes! Yes! This is the best idea anyone has ever had!"));
                case "coldB":
                    this.repeatActiveMenu = false;
                    activeMenu.setCondition("coldA", false);
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Yeah. Sure. Why the hell not! Let's see if we can turn off the part of us that feels things."));

                    switch (this.ch3Voice) {
                        case BROKEN:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "I turned that off ages ago."));
                            break;
                        case PARANOID:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "If we can't feel things, then how are we supposed to know what's true?"));
                            parser.printDialogueLine(new VoiceDialogueLine("You could always just trust what I tell you."));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Ha! No."));
                            break;
                    }

                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "Pain is good. It's how we stay alive."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Nah, I'm sick of pain."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Yeah, this whole thing would be a lot more tolerable if it didn't hurt so much."));

                    subMenu = new OptionsMenu(true);
                    activeMenu.add(new Option(this.manager, "taunt", "\"Do your worst! I bet you can't even hurt me.\""));
                    activeMenu.add(new Option(this.manager, "wait", "[Wait for her to come to you.]"));

                    switch (parser.promptOptionsMenu(subMenu)) {
                        case "taunt":
                            parser.printDialogueLine(new PrincessDialogueLine("Sure thing! I love a challenge. I bet I can hurt you *so much!*"));
                            break;

                        case "wait":
                            parser.printDialogueLine(new PrincessDialogueLine("Just standing there, huh? A bold strategy."));
                            break;
                    }
                    
                    parser.printDialogueLine(new VoiceDialogueLine("The Princess closes the distance, and --", true));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "We dodge!"));
                    parser.printDialogueLine(new VoiceDialogueLine("And you dodge."));

                    if (this.hasVoice(Voice.STUBBORN)) {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "And we fight back."));
                        parser.printDialogueLine(new VoiceDialogueLine("And you fight back."));
                    }

                    parser.printDialogueLine(new PrincessDialogueLine("Oooh. You're fast! But let's see how fast you really are."));

                    if (this.hasVoice(Voice.STUBBORN)) {
                        parser.printDialogueLine(new VoiceDialogueLine("You and the Princess enter a quick and vicious exchange, each of you wounding the other, but neither landing a fatal blow."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "Yes! Yes! This is exactly it!"));
                        parser.printDialogueLine(new VoiceDialogueLine("But the dance couldn't last forever. All it takes is a single clumsy moment. She skewers you."));
                    } else {
                        parser.printDialogueLine(new VoiceDialogueLine("You dodge the Princess's blows for as long as you can, sustaining only nicks and cuts as you attempt to avoid her blades. But it isn't long before you're winded, your feet a little slower, your gait a little clumsier."));
                        parser.printDialogueLine(new VoiceDialogueLine("That's all the opening she needs. She skewers you."));
                    }
                    
                    parser.printDialogueLine(new PrincessDialogueLine("Gotcha!"));
                    
                    System.out.println();
                    parser.printDialogueLine("Everything goes dark.");
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "And? Does it hurt?"));

                    this.addVoice(Voice.COLD);
                    System.out.println();
                    parser.printDialogueLine("All of a sudden, everything comes back into focus. The Princess stands in front of you with a manic grin on her face.");
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "No."));
                    break;
                    
                case "stubborn":
                    this.repeatActiveMenu = false;
                    if (this.hasVoice(Voice.COLD)) this.canSlayPrincess = false;
                    activeMenu.setCondition("coldA", true);
                    activeMenu.setCondition("coldB", false);
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I guess we have a weapon."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Okay. I'm in. Let's do this."));

                    switch (this.ch3Voice) {
                        case BROKEN:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "She's going to kill us."));
                            break;
                        case PARANOID:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "This can't be the right answer. It's too easy!"));
                            break;
                    }

                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Well, if we die, we die, right? Then we can try again next time."));

                    switch (this.ch3Voice) {
                        case BROKEN:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "Bad idea. But it's not like any of you listen to me."));
                            break;
                        case PARANOID:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "Are you sure?"));
                            break;
                    }

                    // Your choice here doesn't actually matter
                    subMenu = new OptionsMenu(true);
                    activeMenu.add(new Option(this.manager, "cheer", "Cheer up! Maybe we'll win!"));
                    activeMenu.add(new Option(this.manager, "see", "See, but that's the brilliance of it all. She doesn't think we have it in us to win."));
                    activeMenu.add(new Option(this.manager, "done", "I'm done explaining myself. I'm going to stab her now."));
                    parser.promptOptionsMenu(subMenu);
                    
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Yeah! No more moping, we're gonna fight her, and we're gonna win!"));
                    parser.printDialogueLine(new VoiceDialogueLine("Blade poised to strike, you charge the Princess."));
                    parser.printDialogueLine(new PrincessDialogueLine("Oh, so you do want a fight, huh? Okay! Yes! I love this! Try to stab me! Try to stab me and see what happens!"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Here goes... we can make this work."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "Yes. If we pay attention, we won't die."));

                    switch (this.ch3Voice) {
                        case BROKEN:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "And what if we don't?"));
                            break;
                        case PARANOID:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "Yeah. Mind over matter, mind over matter, mind over matter. What if we can't do this?"));
                            break;
                    }
                    
                    parser.printDialogueLine(new VoiceDialogueLine("You and the Princess exchange a flurry of blows, and you manage to narrowly avoid death several times."));
                    parser.printDialogueLine(new VoiceDialogueLine("Until you finally see it. An opening."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "It's a trick. Don't take it."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "No, screw that. Ours! Ours! I called it, it's ours and we're taking it!"));
                    parser.printDialogueLine(new VoiceDialogueLine("And you do take it. But as you close in, yet another blade erupts from the Princess. You have no time to react before you feel it sliding through your ribs."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "A trick. I knew it. I told you to be careful of tricks."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "What?! From where?"));
                    parser.printDialogueLine(new VoiceDialogueLine("From her knee, apparently."));
                    parser.printDialogueLine(new PrincessDialogueLine("Hehehehe. Too slow!"));
                    parser.printDialogueLine(new VoiceDialogueLine("Unfortunately for the sake of the world, fair doesn't factor into this. Another of her blades comes slicing up to finish the job. She skewers you."));
                    
                    System.out.println();
                    parser.printDialogueLine("Everything goes dark.");
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Ow."));
                    parser.printDialogueLine(new PrincessDialogueLine("Ribbons! I'm going to make you ribbons! This is so much fun and I want to celebrate."));

                    this.addVoice(Voice.STUBBORN);
                    System.out.println();
                    parser.printDialogueLine("All of a sudden, everything comes back into focus. The Princess stands in front of you with a manic grin on her face.");
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "It would have worked if we had just stabbed her harder."));
                    break;
                    
                case "oppo":
                    this.repeatActiveMenu = false;
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "She isn't one to talk and we shouldn't be either."));
                    
                    switch (this.ch3Voice) {
                        case STUBBORN:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "I agree. Talking is boring. We should just get back to fighting her!"));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "That's not helpful. We already tried that one, didn't we?"));
                            break;
                        case BROKEN:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "He's right. I don't think we can talk this out. I think she wants to kill us."));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Exactly. I think she established that at some point between stabbing us to death and directly telling us, just now, that she was going to kill us."));
                            break;
                        case PARANOID:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "Exactly. She wants us dead, you know!"));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Exactly. I think she established that at some point between stabbing us to death and directly telling us, just now, that she was going to kill us."));
                            break;
                    }
                    
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "You were the one who asked for ideas."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Fine. Talk our way out of this! Maybe that's the answer."));

                    subMenu = new OptionsMenu(true);
                    activeMenu.add(new Option(this.manager, "winner", "\"You know, I'm a big fan of winners, and you've got 'winner' written all over you. How about we stop fighting and team up? I'll even let you be in charge!\""));
                    activeMenu.add(new Option(this.manager, "join", "\"Look, both of us are stuck here against our will. What if we joined forces?\""));
                    activeMenu.add(new Option(this.manager, "stabbing", "\"Has anyone ever told you how good you are at stabbing things?\""));

                    switch (parser.promptOptionsMenu(subMenu)) {
                        case "winner":
                        case "join":
                            parser.printDialogueLine(new VoiceDialogueLine("Oh for the love of... *sigh.* The Princess stops for a moment, mulling over your deranged proposition."));
                            parser.printDialogueLine(new PrincessDialogueLine("Nah! Not interested. And don't take it the wrong way! I think you're neat. But I'm having way too much fun to stop."));
                            break;

                        case "stabbing":
                            parser.printDialogueLine(new PrincessDialogueLine("Nope! But I don't need anyone to tell me that. I know I'm good at what I do! The best, I think."));
                            parser.printDialogueLine(new VoiceDialogueLine("The Princess pauses for a moment to further ponder your ill-placed compliment."));
                            parser.printDialogueLine(new PrincessDialogueLine("Hey, what gives? Are you trying to get on my good side? You're not bored of me stabbing you, are you?"));

                            subMenu = new OptionsMenu(true);
                            activeMenu.add(new Option(this.manager, "goodSide", "\"Yes! Yes, I am trying to get on your good side. Did it work?\""));
                            activeMenu.add(new Option(this.manager, "bored", "\"Yes! Yes, I am bored of you stabbing me. Can you stop stabbing me now?\""));
                            activeMenu.add(new Option(this.manager, "facts", "\"Psht. What? Me? Fluffing you up? I'm just stating facts.\""));
                            activeMenu.add(new Option(this.manager, "silent", "[Say nothing.]"));

                            switch (parser.promptOptionsMenu(subMenu)) {
                                case "goodSide":
                                    parser.printDialogueLine(new PrincessDialogueLine("You're *already* on my good side!"));
                                    parser.printDialogueLine(new PrincessDialogueLine("But that doesn't mean I'm going to not stab you. I'm having fun! Why would I stop?"));
                                    break;
                                    
                                case "bored":
                                    parser.printDialogueLine(new PrincessDialogueLine("Well, *I'm* having fun! Why would I stop?"));
                                    break;
                                    
                                case "facts":
                                    parser.printDialogueLine(new PrincessDialogueLine("And they're good facts! Great facts! And for what it's worth, you're already on my good side!"));
                                    parser.printDialogueLine(new PrincessDialogueLine("But that doesn't mean I'm going to not stab you. I'm having fun! Why would I stop?"));
                                    break;
                                    
                                case "silent":
                                    parser.printDialogueLine(new PrincessDialogueLine("Well, for what it's worth, you're already on my good side!"));
                                    parser.printDialogueLine(new PrincessDialogueLine("But that doesn't mean I'm going to not stab you. I'm having fun! Why would I stop?"));
                                    break;
                            }

                            break;
                    }
                    
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "We should coil ourselves. She's about to pounce."));

                    switch (this.ch3Voice) {
                        case STUBBORN:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "Just get ready to fight back."));
                            break;
                        case BROKEN:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "We're screwed. Again. I'll see you all when we die."));
                            break;
                        case PARANOID:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "I hate this. Every choice we make is wrong."));
                            break;
                    }
                    
                    parser.printDialogueLine(new VoiceDialogueLine("But before you can finish another thought, the Princess closes the distance and --", true));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "Dodge. Now!"));
                    parser.printDialogueLine(new VoiceDialogueLine("Fails to hit you."));
                    if (this.ch3Voice == Voice.STUBBORN) parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "Haha, yes!"));
                    parser.printDialogueLine(new PrincessDialogueLine("Oooh, you're a fast one! That's fun! But I think I can get even faster."));

                    if (this.ch3Voice == Voice.STUBBORN) {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "Now hit her back!"));
                        parser.printDialogueLine(new VoiceDialogueLine("You dodge and parry and strike and are struck, her nicking your flesh and you nicking her skin to reveal shining metal below. But eventually you slip up. You lose the pattern, just for a moment."));
                        parser.printDialogueLine(new VoiceDialogueLine("A blade flashes through the air, and she skewers you."));
                    } else {
                        switch (this.ch3Voice) {
                            case BROKEN:
                                parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "I'm going to be quiet for a bit now. Let me know when we die again."));
                                break;
                            case PARANOID:
                                parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "I don't like the way she said that."));
                                break;
                        }
                        
                        parser.printDialogueLine(new VoiceDialogueLine("You dodge and parry and dodge and parry in an endless repeating pattern, but you can only keep it up so long before making a mistake. The pattern breaks for just long enough."));
                        parser.printDialogueLine(new VoiceDialogueLine("She skewers you."));
                    }
                    
                    System.out.println();
                    parser.printDialogueLine("Everything goes dark.");
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Ow."));

                    this.addVoice(Voice.OPPORTUNIST);
                    System.out.println();
                    parser.printDialogueLine("All of a sudden, everything comes back into focus. The Princess stands in front of you with a manic grin on her face.");
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "Wow, she is absolutely uncompromising, isn't she? I know it seems weird, but if anything this makes me want her to like us even more."));
                    break;
                    
                case "broken":
                    this.repeatActiveMenu = false;

                    switch (this.ch3Voice) {
                        case STUBBORN:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "That's pathetic."));
                            break;
                        case PARANOID:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "Oh. A bit of reverse psychology. I like it!"));
                            break;
                    }

                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "Dying is bad, and if you do this, we die."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "How does unconditionally surrendering work for us, anyway? Does it have to be unanimous?"));
                    parser.printDialogueLine(new VoiceDialogueLine("*Sigh.* Apparently not."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Hey now, let's see how it goes. It could work, it's worth a shot."));

                    // Your choice here doesn't actually matter
                    subMenu = new OptionsMenu(true);
                    activeMenu.add(new Option(this.manager, "giveUp", "\"I give up. I'll do anything, just please don't stab me!\""));
                    activeMenu.add(new Option(this.manager, "silent", "[Silently throw your hands in the air.]"));
                    parser.promptOptionsMenu(subMenu);
                    
                    parser.printDialogueLine(new VoiceDialogueLine("You throw your hands in the air and drop your blade."));
                    parser.printDialogueLine(new PrincessDialogueLine("You can't surrender! Don't you know it takes two to stop a fight?"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Ah, shit."));
                    parser.printDialogueLine(new VoiceDialogueLine("Before you can do anything else, she charges you and --", true));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "Dodge! We dodge!"));
                    parser.printDialogueLine(new VoiceDialogueLine("Huh. Okay. You dodge."));
                    parser.printDialogueLine(new PrincessDialogueLine("Aw. I thought you were surrendering."));
                    parser.printDialogueLine(new VoiceDialogueLine("And then she skewers you."));
                    
                    System.out.println();
                    parser.printDialogueLine("Everything goes dark.");
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Ow."));

                    this.addVoice(Voice.BROKEN);
                    System.out.println();
                    parser.printDialogueLine("All of a sudden, everything comes back into focus. The Princess stands in front of you with a manic grin on her face.");
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Huh. That's weird. The blade's back in our hand."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "Can't even surrender right."));
                    break;
                    
                case "smitten":
                    this.repeatActiveMenu = false;
                    parser.printDialogueLine(new VoiceDialogueLine("Now I've tolerated quite a bit from you, but this is a bridge too far. Please don't try romancing the Princess. She wants to kill you! She's going to end the world if you don't stop her."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Yeah... do we have to flirt with the murderous monster?"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "I'd rather not."));

                    switch (this.ch3Voice) {
                        case STUBBORN:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "I'm into it."));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "I can't say I mind, either. If it weren't for all the cheating, I'd say she's pretty cute."));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "Can we flirt by fighting her, though?"));
                            break;
                        case BROKEN:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "It's not like she wants us, anyway."));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "I'm fine with it. Let's see where this goes."));
                            break;
                        case PARANOID:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "Maybe it'll work! Maybe it'll throw her off. I know I'd be thrown off if she started flirting with us."));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "I'm fine with it. Let's see where this goes."));
                            break;
                    }

                    subMenu = new OptionsMenu(true);
                    activeMenu.add(new Option(this.manager, "gorgeous", "\"I know you want to kill me, but has anyone ever told you how gorgeous you are?\""));
                    activeMenu.add(new Option(this.manager, "getYou", "\"I just feel like I really get you. I like you. Romantically, even. Maybe we can hash this out over a date.\""));
                    activeMenu.add(new Option(this.manager, "dinner", "\"How about you buy me dinner before impaling me to death?\""));
                    activeMenu.add(new Option(this.manager, "theLook", "[Give her *The Look.*]"));

                    switch (parser.promptOptionsMenu(subMenu)) {
                        case "gorgeous":
                            parser.printDialogueLine(new VoiceDialogueLine("A rosy blush flushes in the Princess's cheeks, and a wide grin cuts across her face."));
                            parser.printDialogueLine(new PrincessDialogueLine("You're the only person I know, so that's a first! You're sweet! I like you! You're also gorgeous!"));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "I'll be damned. This is actually going to work, isn't it?"));
                            parser.printDialogueLine(new PrincessDialogueLine("Still gonna kill you though."));
                            break;

                        case "getYou":
                            parser.printDialogueLine(new VoiceDialogueLine("A rosy blush flushes in the Princess's cheeks, and a wide grin cuts across her face."));
                            parser.printDialogueLine(new PrincessDialogueLine("You're sweet! I like you too! You're probably my favorite person other than me."));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "I'll be damned. This is actually going to work, isn't it?"));
                            parser.printDialogueLine(new PrincessDialogueLine("Still gonna kill you though."));
                            break;

                        case "dinner":
                            parser.printDialogueLine(new VoiceDialogueLine("A rosy blush flushes in the Princess's cheeks, and a wide grin cuts across her face."));
                            parser.printDialogueLine(new PrincessDialogueLine("Oh? Is that how it is? Yeah okay I feel that. I like you too."));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "I'll be damned. This is actually going to work, isn't it?"));
                            parser.printDialogueLine(new PrincessDialogueLine("But why mess around with appetizers when the main course is right there?"));
                            break;

                        case "theLook":
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "*The Look?*"));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "The Look. We've all used it."));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Yeah, do you not know about The Look?"));

                            switch (this.ch3Voice) {
                                case STUBBORN:
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "The Look, eh? So we're getting serious about this."));
                                    break;
                                case BROKEN:
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "Even I know about The Look."));
                                    break;
                                case PARANOID:
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "Are you *sure* we want to use The Look so early? It's supposed to be saved for emergencies."));
                                    break;
                            }

                            parser.printDialogueLine(new VoiceDialogueLine("*Sigh.* You flash the princess *The Look.*"));
                            parser.printDialogueLine(new VoiceDialogueLine("... and a rosy blush rushes to the Princess's cheeks as she breaks into a wide grin. Unbelievable."));
                            parser.printDialogueLine(new PrincessDialogueLine("Oh? Is that how it is? Yeah okay I feel that. I like you too. Neat!"));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "I'll be damned. This is actually going to work, isn't it?"));
                            parser.printDialogueLine(new PrincessDialogueLine("Still going to kill you, but now we can both enjoy a mutual romantic subtext to the murder!"));
                            break;
                    }

                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Or not."));
                    switch (this.ch3Voice) {
                        case STUBBORN:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "Oh, I like her. I like her a lot!"));
                            break;
                        case BROKEN:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "At least she likes this. I've never been liked before."));
                            break;
                        case PARANOID:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "Is anything going to work with her? She's so single minded. It's like whatever we do it's always going to end exactly the same."));
                            break;
                    }
                    
                    parser.printDialogueLine(new VoiceDialogueLine("Blush still glowing in her cheeks, the Princess closes the distance between you, blades flashing."));
                    parser.printDialogueLine(new VoiceDialogueLine("She skewers you."));
                    
                    System.out.println();
                    parser.printDialogueLine("Everything goes dark.");
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Ow."));

                    this.addVoice(Voice.SMITTEN);
                    System.out.println();
                    parser.printDialogueLine("All of a sudden, everything comes back into focus. The Princess stands in front of you with a manic grin on her face.");
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "What worthwhile romance doesn't hurt at least a little bit? What matters is that she likes us. She's even said as much!"));
                    break;
                    
                case "para":
                    this.repeatActiveMenu = false;

                    switch (this.ch3Voice) {
                        case STUBBORN:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "Panicking is the worst possible thing for us to do."));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "And panic where? Up the slide that dropped us down here?"));
                            break;
                        case BROKEN:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Panic where? Up the slide that dropped us down here?"));
                            break;
                    }
                    
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "There's no way out?!"));
                    parser.printDialogueLine(new PrincessDialogueLine("I'm coming to get you!"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "There's no way out!"));

                    switch (this.ch3Voice) {
                        case STUBBORN:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Okay. Screw it. We're panicking!"));
                            break;
                        case BROKEN:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "It's hard to panic when we already know what's going to happen to us."));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "No. Screw it. We're panicking!"));
                            break;
                    }

                    parser.printDialogueLine(new VoiceDialogueLine("You panic, but unsurprisingly, panicking doesn't save you from her blades. She skewers you."));
                    
                    System.out.println();
                    parser.printDialogueLine("Everything goes dark.");
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Ow."));

                    this.addVoice(Voice.PARANOID);
                    System.out.println();
                    parser.printDialogueLine("All of a sudden, everything comes back into focus. The Princess stands in front of you with a manic grin on her face.");
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "Sorry about that. I gave into a bit of a fear response there, and I don't think it was very helpful."));
                    break;
                    
                case "cSlaySelf":
                case "contra":
                    this.repeatActiveMenu = false;
                    this.canSlaySelf = false;
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "No, no, no that's a *terrible* idea!"));

                    switch (this.ch3Voice) {
                        case STUBBORN:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "A win is a win."));
                            break;
                        case BROKEN:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "We're dead either way."));
                            break;
                        case PARANOID:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "Yeah, I'm not sure that's going to work."));
                            break;
                    }
                    
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Screw it. We've already died twice. What's a third?"));
                    parser.printDialogueLine(new VoiceDialogueLine("A third is a third. It's bad."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Who cares!"));
                    parser.printDialogueLine(new VoiceDialogueLine("*Sigh.* Fine. You raise your blade above your head."));
                    parser.printDialogueLine(new PrincessDialogueLine("Oh, this is new! What are you gonna do? Are you really going to stab yourself? Neat!"));
                    parser.printDialogueLine(new VoiceDialogueLine("And then you skewer yourself."));
                    
                    System.out.println();
                    parser.printDialogueLine("Everything goes dark.");
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Ow."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Are we still here? Can we not actually off ourselves? Boo."));

                    this.addVoice(Voice.CONTRARIAN);
                    System.out.println();
                    parser.printDialogueLine("All of a sudden, everything comes back into focus. The Princess stands in front of you with a manic grin on her face.");
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Huh. That didn't do much of anything. We're tougher than I thought."));
                    break;
                    
                case "skeptic":
                    this.repeatActiveMenu = false;
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Yeah, that's right. We just have to think. There's probably an answer if we think."));
                    parser.printDialogueLine(new PrincessDialogueLine("Just standing there, huh? A bold strategy."));
                    parser.printDialogueLine(new VoiceDialogueLine("But you don't have time to finish your thought. In a moment, she's across the room, blades flashing in the dim starlight."));
                    parser.printDialogueLine(new VoiceDialogueLine("She skewers you."));
                    
                    System.out.println();
                    parser.printDialogueLine("Everything goes dark.");
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Ow."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "What a surprise."));

                    this.addVoice(Voice.SKEPTIC);
                    System.out.println();
                    parser.printDialogueLine("All of a sudden, everything comes back into focus. The Princess stands in front of you with a manic grin on her face.");
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "Yeah. We don't even get a second to think without her stabbing us."));
                    break;

                case "cGoStairs":
                    parser.printDialogueLine(new VoiceDialogueLine("The stairs are practically a slide, remember? You have no way out."));
                    break;

                case "cSlayPrincessFail":
                case "cSlaySelfFail":
                case "noFightOptions":
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "We already *tried* that!"));
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        this.razor3Ending(true);
    }

    /**
     * Runs the basement section of Chapter III: No Way Out
     */
    private void noWayOutBasement() {
        parser.printDialogueLine(new PrincessDialogueLine("And you still don't have a weapon! That's funny! That's a joke! I'm going to kill you now."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Yes, that was extremely silly of whoever did that. Probably a bad idea!"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "That was you!"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "I know! I'm just trying to add some levity to this."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Well, since all of this was your idea, how about you figure out how to get us out of it?"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Oh, guys like us don't get to make any decisions, you should know that."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "I decided to pick up that blade, and *you* decided to throw it out the window."));

        switch (this.ch3Voice) {
            case BROKEN:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "This is why we've already lost. Can't even stop bickering with ourself. How are we supposed to beat her without a weapon? She's so sharp."));
                break;
            case PARANOID:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "I take it back. Having my own corner clearly isn't working because I can still hear you three yelling at each other. She's going to kill us again, you know. Especially if we keep fighting with ourself. We need to get rid of our thoughts."));
                break;
        }

        parser.printDialogueLine(new VoiceDialogueLine("Your internal bickering is cut short by the wet sound of slicing meat. From the Princess's arms erupt twin blades, glistening with her blood, the empty flesh of her arms flopping at her elbows like torn sleeves. The chain clatters to the floor."));
        parser.printDialogueLine(new PrincessDialogueLine("You're going to make me walk over to you, aren't you?"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "All right, I'm out of ideas... What're we doing?"));

        OptionsMenu subMenu;

        // Remember, activeMenu was set up at the end of razor3Intro()
        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);

            switch (activeOutcome) {
                case "cSlayPrincess":
                case "stubborn":
                    this.repeatActiveMenu = false;
                    this.canSlayPrincess = false;
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "We're fighting her? Are you forgetting the part where the cheeky one thought it'd be funny to throw our only weapon out the window?"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "I mean, it *was* funny. Even she said it was funny."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Oh, *yes!* It was absolutely *hilarious!*"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "We'll all be laughing together once we're out of here."));

                    switch (this.ch3Voice) {
                        case BROKEN:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "If we make it out of here. But I don't know. That seems unlikely."));
                            break;
                        case PARANOID:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "Do any of us think we can hurt her like this?"));
                            break;
                    }

                    // Your choice here doesn't actually matter
                    subMenu = new OptionsMenu(true);
                    activeMenu.add(new Option(this.manager, "maybe", "Maybe we'll win!"));
                    activeMenu.add(new Option(this.manager, "see", "See, but that's the brilliance of it all. She won't see it coming."));
                    activeMenu.add(new Option(this.manager, "done", "I'm done explaining myself. I'm going to punch her now."));

                    if (parser.promptOptionsMenu(subMenu).equals("maybe")) {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Yes, exactly! Who knows what we're capable of? For all we know that 'blade' was holding us back."));
                    }
                    
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "*Sigh.* Okay, why not?"));
                    parser.printDialogueLine(new VoiceDialogueLine("Fists raised, you charge the Princess."));
                    parser.printDialogueLine(new PrincessDialogueLine("Oh, so you do want a fight, huh? Okay! Hit me! Hit me and see what happens! I'll give you a free shot and everything!"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Here goes... we can make this work."));

                    switch (this.ch3Voice) {
                        case BROKEN:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "And what if we don't?"));
                            break;
                        case PARANOID:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "Yeah. Mind over matter, mind over matter, mind over matter -- what if she's lying? What if she isn't going to give us a free shot?"));
                            break;
                    }
                    
                    parser.printDialogueLine(new VoiceDialogueLine("Your fist slams into the Princess's face, and she recoils in pain."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Huh. We actually did something to her!"));
                    parser.printDialogueLine(new PrincessDialogueLine("Ow ow ow ow ow! That hurt! What are your bones made of, metal?"));
                    parser.printDialogueLine(new PrincessDialogueLine("Because mine are."));
                    parser.printDialogueLine(new VoiceDialogueLine("Before you can react, she returns a punch of her own, only it isn't really a \"punch.\""));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Yes, we all know she has swords for arms. We have eyes."));
                    parser.printDialogueLine(new VoiceDialogueLine("Well, she skewers you."));
                    
                    System.out.println();
                    parser.printDialogueLine("Everything goes dark.");
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Ow."));
                    parser.printDialogueLine(new PrincessDialogueLine("Ribbons! I'm going to make you ribbons! This is so much fun and I want to celebrate."));

                    this.addVoice(Voice.STUBBORN);
                    System.out.println();
                    parser.printDialogueLine("All of a sudden, everything comes back into focus. The Princess stands in front of you with a manic grin on her face.");
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "It would have worked if we punched harder."));
                    break;
                    
                case "oppo":
                    this.repeatActiveMenu = false;

                    switch (this.ch3Voice) {
                        case BROKEN:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "*Can* we talk this out? I think she wants to kill us."));
                            break;
                        case PARANOID:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "Can we trust someone who wants us dead?"));
                            break;
                    }

                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Exactly. I think she established that at some point between stabbing us to death and directly telling us, just now, that she was going to kill us."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "You were the one who asked for ideas."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Fine. Talk our way out of this! Maybe that's the answer."));

                    subMenu = new OptionsMenu(true);
                    activeMenu.add(new Option(this.manager, "winner", "\"You know, I'm a big fan of winners, and you've got 'winner' written all over you. How about we stop fighting and team up? I'll even let you be in charge!\""));
                    activeMenu.add(new Option(this.manager, "join", "\"Look, both of us are stuck here against our will. What if we joined forces?\""));
                    activeMenu.add(new Option(this.manager, "stabbing", "\"Has anyone ever told you how good you are at stabbing things?\""));

                    switch (parser.promptOptionsMenu(subMenu)) {
                        case "winner":
                        case "join":
                            parser.printDialogueLine(new VoiceDialogueLine("Oh for the love of... *sigh.* The Princess stops for a moment, mulling over your deranged proposition."));
                            parser.printDialogueLine(new PrincessDialogueLine("Nah! Not interested. And don't take it the wrong way! I think you're neat. But I'm having way too much fun to stop."));
                            break;

                        case "stabbing":
                            parser.printDialogueLine(new PrincessDialogueLine("Nope! But I don't need anyone to tell me that. I know I'm good at what I do! The best, I think."));
                            parser.printDialogueLine(new VoiceDialogueLine("The Princess pauses for a moment to further ponder your ill-placed compliment."));
                            parser.printDialogueLine(new PrincessDialogueLine("Hey, what gives? Are you trying to get on my good side?"));

                            subMenu = new OptionsMenu(true);
                            activeMenu.add(new Option(this.manager, "goodSide", "\"Yes! Yes, I am trying to get on your good side. Did it work?\""));
                            activeMenu.add(new Option(this.manager, "bored", "\"Yes! Yes, I am bored of you stabbing me. Can you stop stabbing me now?\""));
                            activeMenu.add(new Option(this.manager, "facts", "\"Psht. What? Me? Fluffing you up? I'm just stating facts.\""));
                            activeMenu.add(new Option(this.manager, "silent", "[Say nothing.]"));

                            switch (parser.promptOptionsMenu(subMenu)) {
                                case "goodSide":
                                    parser.printDialogueLine(new PrincessDialogueLine("You're *already* on my good side!"));
                                    break;
                                    
                                case "facts":
                                    parser.printDialogueLine(new PrincessDialogueLine("And they're good facts! Great facts! And for what it's worth, you're already on my good side!"));
                                    break;
                                    
                                case "silent":
                                    parser.printDialogueLine(new PrincessDialogueLine("Well, for what it's worth, you're already on my good side!"));
                                    break;
                            }

                            parser.printDialogueLine(new PrincessDialogueLine("But that doesn't mean I'm going to not stab you. I'm having fun! Why would I stop?"));
                            break;
                    }
                    
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Is this what it's like dealing with me?"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Yeah, it is. You're the worst one of us. You know that, right?"));

                    switch (this.ch3Voice) {
                        case BROKEN:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "We're screwed. Again. I'll see you all when we die."));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Really? I'm the worst one? Do you hear how whiny and un-fun *he* is?"));
                            break;
                        case PARANOID:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "Don't rank us! The last thing we need is to be arguing with ourselves while --", true));
                            break;
                    }
                    
                    parser.printDialogueLine(new VoiceDialogueLine("But you never finish that argument before the Princess closes the distance."));
                    parser.printDialogueLine(new VoiceDialogueLine("She skewers you."));
                    
                    System.out.println();
                    parser.printDialogueLine("Everything goes dark.");
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Ow."));

                    this.addVoice(Voice.OPPORTUNIST);
                    System.out.println();
                    parser.printDialogueLine("All of a sudden, everything comes back into focus. The Princess stands in front of you with a manic grin on her face.");
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "Wow, she is absolutely uncompromising, isn't she? I know it seems weird, but if anything this makes me want her to like us even more."));
                    break;
                    
                case "broken":
                    this.repeatActiveMenu = false;
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Ooh! That'll show her!"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "Yeah. A bit of reverse psychology. I like it!"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "How does unconditionally surrendering work for us? Does it have to be unanimous?"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "I think it's up to the one making the decisions. We're really all just here in an advisory role."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Let's see how it goes."));

                    subMenu = new OptionsMenu(true);
                    activeMenu.add(new Option(this.manager, "giveUp", "\"I give up. I'll do anything, just please don't stab me!\""));
                    activeMenu.add(new Option(this.manager, "silent", "[Silently throw your hands in the air.]"));

                    if (parser.promptOptionsMenu(subMenu).equals("silent")) {
                        parser.printDialogueLine(new VoiceDialogueLine("You silently throw your hands in the air."));
                    }
                    
                    parser.printDialogueLine(new PrincessDialogueLine("Surrendering, are we? Don't you know it takes two to stop a fight?"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Ah, shit."));
                    parser.printDialogueLine(new VoiceDialogueLine("Before you can make another move, she skewers you."));
                    
                    System.out.println();
                    parser.printDialogueLine("Everything goes dark.");
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Ow."));

                    this.addVoice(Voice.BROKEN);
                    System.out.println();
                    parser.printDialogueLine("All of a sudden, everything comes back into focus. The Princess stands in front of you with a manic grin on her face.");
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "We deserved it. Can't even surrender right."));
                    break;
                    
                case "hunted":
                    this.repeatActiveMenu = false;
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Okay, that's not a bad idea."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "What are we going to do, tire her out?"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Maybe!"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "And we'll learn how she moves. If we can keep ourselves alive, that's one step closer to getting through this."));

                    switch (this.ch3Voice) {
                        case BROKEN:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "It doesn't matter if she kills us. It's always going to be the same. This is what we deserve."));
                            break;
                        case PARANOID:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "We're not the only ones who can learn. For all we know, she's going to pick up on our movements faster than we can pick up on hers. She's been one step ahead of us since we got here."));
                            break;
                    }
                    
                    parser.printDialogueLine(new VoiceDialogueLine("But you don't have time to bicker amongst yourselves forever. Knives out, she charges you."));
                    parser.printDialogueLine(new VoiceDialogueLine("And what do you know? You dodge her attack."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "And there we go! Yes."));
                    parser.printDialogueLine(new VoiceDialogueLine("And then she attacks again."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Hey, wait a second!"));
                    parser.printDialogueLine(new VoiceDialogueLine("And you dodge again."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Phew."));
                    parser.printDialogueLine(new VoiceDialogueLine("And she attacks again, barely missing you."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Just how many attacks does she have?"));
                    parser.printDialogueLine(new PrincessDialogueLine("Do you really think you can dodge me forever? I have so many more moves than you can even imagine! And one of them's going to hit you."));

                    switch (this.ch3Voice) {
                        case BROKEN:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "We're doomed, aren't we?"));
                            break;
                        case PARANOID:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "It's like she's in our head! What are we supposed to do?"));
                            break;
                    }
                    
                    parser.printDialogueLine(new VoiceDialogueLine("She attacks once more --", true));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "I don't like how you just changed the way you said it."));
                    parser.printDialogueLine(new VoiceDialogueLine("And she skewers you."));
                    
                    System.out.println();
                    parser.printDialogueLine("Everything goes dark.");
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Ow."));

                    this.addVoice(Voice.HUNTED);
                    System.out.println();
                    parser.printDialogueLine("All of a sudden, everything comes back into focus. The Princess stands in front of you with a manic grin on her face.");
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "I did my best."));
                    break;
                    
                case "smitten":
                    this.repeatActiveMenu = false;
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Now *that* is an interesting move."));
                    parser.printDialogueLine(new VoiceDialogueLine("Interesting? It's disgusting. No, don't try romancing the Princess. She wants to kill you! She's going to end the world if you don't stop her."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Yeah... do we have to flirt with the murderous monster?"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Of course we do! I'm into it. The one making the decisions is into it. Are you not?"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "... I don't think so? I don't know."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "I could go either way, honestly."));

                    switch (this.ch3Voice) {
                        case BROKEN:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "She doesn't want us."));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "You're just saying that because you want her to be into you."));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "I know. I thought I was being obvious."));
                            break;
                        case PARANOID:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "Maybe it'll work! Maybe it'll throw her off. I know I'd be thrown off if she started flirting with us."));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Yeah, because you'd be into it."));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "No comment."));
                            break;
                    }

                    subMenu = new OptionsMenu(true);
                    activeMenu.add(new Option(this.manager, "gorgeous", "\"I know you want to kill me, but has anyone ever told you how gorgeous you are?\""));
                    activeMenu.add(new Option(this.manager, "getYou", "\"I just feel like I really get you. I like you. Romantically, even. Maybe we can hash this out over a date.\""));
                    activeMenu.add(new Option(this.manager, "dinner", "\"How about you buy me dinner before impaling me to death?\""));
                    activeMenu.add(new Option(this.manager, "theLook", "[Give her *The Look.*]"));

                    switch (parser.promptOptionsMenu(subMenu)) {
                        case "gorgeous":
                            parser.printDialogueLine(new VoiceDialogueLine("A rosy blush flushes in the Princess's cheeks, and a wide grin cuts across her face."));
                            parser.printDialogueLine(new PrincessDialogueLine("You're the only person I know, so that's a first! You're sweet! I like you! You're also gorgeous!"));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "I'll be damned. This is actually going to work, isn't it?"));
                            parser.printDialogueLine(new PrincessDialogueLine("Still gonna kill you though."));
                            break;

                        case "getYou":
                            parser.printDialogueLine(new VoiceDialogueLine("A rosy blush flushes in the Princess's cheeks, and a wide grin cuts across her face."));
                            parser.printDialogueLine(new PrincessDialogueLine("You're sweet! I like you too! You're probably my favorite person other than me."));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "I'll be damned. This is actually going to work, isn't it?"));
                            parser.printDialogueLine(new PrincessDialogueLine("Still gonna kill you though."));
                            break;

                        case "dinner":
                            parser.printDialogueLine(new VoiceDialogueLine("A rosy blush flushes in the Princess's cheeks, and a wide grin cuts across her face."));
                            parser.printDialogueLine(new PrincessDialogueLine("Oh? Is that how it is? Yeah okay I feel that. I like you too."));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "I'll be damned. This is actually going to work, isn't it?"));
                            parser.printDialogueLine(new PrincessDialogueLine("But why mess around with appetizers when the main course is right there?"));
                            break;

                        case "theLook":
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "*The Look?*"));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Yeah. The best flirts know how to flirt without saying anything. We just have to let her know, right?"));
                            parser.printDialogueLine(new VoiceDialogueLine("*Sigh.* You flash the princess *The Look.*"));
                            parser.printDialogueLine(new VoiceDialogueLine("... and a rosy blush rushes to the Princess's cheeks as she breaks into a wide grin. Unbelievable."));
                            parser.printDialogueLine(new PrincessDialogueLine("Oh? Is that how it is? Yeah okay I feel that. I like you too. Neat!"));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "I'll be damned. This is actually going to work, isn't it?"));
                            parser.printDialogueLine(new PrincessDialogueLine("Still going to kill you, but now we can both enjoy a mutual romantic subtext to the murder!"));
                            break;
                    }

                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Or not."));

                    switch (this.ch3Voice) {
                        case BROKEN:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "At least she likes this. I've never been liked before."));
                            break;
                        case PARANOID:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "Is anything going to work with her? She's so single minded. It's like whatever we do it's always going to end exactly the same."));
                            break;
                    }
                    
                    parser.printDialogueLine(new VoiceDialogueLine("Blush still glowing in her cheeks, the Princess closes the distance between you, blades flashing."));
                    parser.printDialogueLine(new VoiceDialogueLine("She skewers you."));
                    
                    System.out.println();
                    parser.printDialogueLine("Everything goes dark.");
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Ow."));

                    this.addVoice(Voice.SMITTEN);
                    System.out.println();
                    parser.printDialogueLine("All of a sudden, everything comes back into focus. The Princess stands in front of you with a manic grin on her face.");
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "What worthwhile romance doesn't hurt at least a little bit? What matters is that she likes us. She's even said as much!"));
                    break;
                    
                case "para":
                    this.repeatActiveMenu = false;
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Panic where? Up the slide that dropped us down here?"));
                    parser.printDialogueLine(new PrincessDialogueLine("I'm coming to get you!"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Nah, I'm with the decider. It defeats the whole point of panicking if we think about what we're doing, and I don't know if you've been listening, but she's coming to get us! So panic! Give in to the chaos!"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "Oh yes, the chaos of dying. How fun."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Stop whining and do it!"));
                    parser.printDialogueLine(new VoiceDialogueLine("You panic, but unsurprisingly, panicking doesn't save you from her blades."));
                    parser.printDialogueLine(new VoiceDialogueLine("She skewers you."));
                    
                    System.out.println();
                    parser.printDialogueLine("Everything goes dark.");
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Ow."));

                    this.addVoice(Voice.PARANOID);
                    System.out.println();
                    parser.printDialogueLine("All of a sudden, everything comes back into focus. The Princess stands in front of you with a manic grin on her face.");
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "Sorry about that. I gave into a bit of a fear response there, and I don't think it was very helpful."));
                    break;

                case "coldNWO":
                    this.repeatActiveMenu = false;
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Sure! Why the hell not! Let's see if we can turn off the part of us that feels things."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "That is the worst plan I've ever heard, and I *absolutely LOVE IT.* Let's try it out!"));

                    switch (this.ch3Voice) {
                        case BROKEN:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "I turned that off ages ago."));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "If you'd managed to do that, you wouldn't be such a *whiner!*"));
                            break;
                        case PARANOID:
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "If we can't feel things, then how are we supposed to know what's true?"));
                            parser.printDialogueLine(new VoiceDialogueLine("You could always just trust what I tell you."));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Ha! No."));
                            break;
                    }

                    subMenu = new OptionsMenu(true);
                    activeMenu.add(new Option(this.manager, "taunt", "\"Do your worst! I bet you can't even hurt me.\""));
                    activeMenu.add(new Option(this.manager, "wait", "[Wait for her to come to you.]"));

                    switch (parser.promptOptionsMenu(subMenu)) {
                        case "taunt":
                            parser.printDialogueLine(new PrincessDialogueLine("Sure thing! I love a challenge. I bet I can hurt you *so much!*"));
                            break;

                        case "wait":
                            parser.printDialogueLine(new PrincessDialogueLine("Just standing there, huh? A bold strategy."));
                            break;
                    }
                    
                    parser.printDialogueLine(new VoiceDialogueLine("The Princess rapidly closes the distance."));
                    parser.printDialogueLine(new VoiceDialogueLine("And then she skewers you."));
                    
                    System.out.println();
                    parser.printDialogueLine("Everything goes dark.");
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "And? Does it hurt?"));

                    this.addVoice(Voice.COLD);
                    System.out.println();
                    parser.printDialogueLine("All of a sudden, everything comes back into focus. The Princess stands in front of you with a manic grin on her face.");
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "No."));
                    break;
                    
                case "skeptic":
                    this.repeatActiveMenu = false;
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Yeah, that's right. We just have to think. There's probably an answer if we think."));
                    parser.printDialogueLine(new PrincessDialogueLine("Just standing there, huh? A bold strategy."));
                    parser.printDialogueLine(new VoiceDialogueLine("But you don't have time to finish your thought. In a moment, she's across the room, blades flashing in the dim starlight."));
                    parser.printDialogueLine(new VoiceDialogueLine("She skewers you."));
                    
                    System.out.println();
                    parser.printDialogueLine("Everything goes dark.");
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Ow."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "What a surprise."));

                    this.addVoice(Voice.SKEPTIC);
                    System.out.println();
                    parser.printDialogueLine("All of a sudden, everything comes back into focus. The Princess stands in front of you with a manic grin on her face.");
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "Yeah. We don't even get a second to think without her stabbing us."));
                    break;

                case "cGoStairs":
                    parser.printDialogueLine(new VoiceDialogueLine("The stairs are practically a slide, remember? You have no way out."));
                    break;

                case "cSlayPrincessFail":
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "We already *tried* that!"));
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        this.razor3Ending(false);
    }

    /**
     * Runs the ending of Chapter III: The Arms Race / No Way Out (after dying for the first time)
     * @param bladePath whether the player took the blade in The Razor (leading to The Arms Race) or not (leading to No Way Out)
     */
    private void razor3Ending(boolean bladePath) {
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Oh! A new one of us."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "I thought that only happens when we die. Did we die?"));

        if (this.hasVoice(Voice.CONTRARIAN)) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Nah, we'd *know* if we died... right?"));
        }

        parser.printDialogueLine(new VoiceDialogueLine("You're on a -- no, you're in a -- where the hell are you?"));

        if (this.hasVoice(Voice.PARANOID)) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "We're dead, aren't we? We're *dead* dead. How long have we been dead? Have we been dead the whole time?"));
            if (this.hasVoice(Voice.BROKEN)) {
                parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "Dead, dead, dead, dead, dead."));
            }
        } else if (this.hasVoice(Voice.BROKEN)) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "I think we're dead. And that's all we'll ever be. Dead, dead, dead, dead, dead."));
        } else {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I don't like that we died without us knowing it. This really, really blurs some lines that I prefer not be blurred. Are we still dead? Are we alive again? How are we even supposed to know the difference?"));
        }
        
        if (this.hasVoice(Voice.COLD)) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "If we didn't realize we were dead, then we made progress. Good job."));
        }

        parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Stop saying dead, all of you! We might have died a second ago, but right now we're extremely not dead."));
        parser.printDialogueLine(new VoiceDialogueLine("This is all horribly wrong. How many times have you been here?"));

        if (bladePath) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "This is four."));
        } else {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "I don't actually know how to answer that question."));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I think he means how many times have we died."));
            parser.printDialogueLine(new VoiceDialogueLine("Yes. That."));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Oh, I've lost count to be honest."));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "I haven't. It's four."));
        }
        
        parser.printDialogueLine(new VoiceDialogueLine("No wonder everything's such a mess. This wasn't supposed to go past one."));
        parser.printDialogueLine(new PrincessDialogueLine("I wonder what you're going to do next! You're so full of ideas and I love that."));
        parser.printDialogueLine(new VoiceDialogueLine("But I guess we don't have time to talk about things before the Princess advances."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Okay. Whatever we do gets us another... us. Let's see how many we can stack. There's got to be a point where it makes us better than her."));

        if (this.hasVoice(Voice.STUBBORN)) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "We don't need any other voices chattering about in here. It'll just confuse us. All we need is to keep fighting!"));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Yeah, I'll pass on that."));
        } if (this.hasVoice(Voice.HUNTED)) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "As long as we keep moving."));
        } if (this.hasVoice(Voice.COLD)) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "Why not? It's not like dying again and again is doing us any harm. Let's see how far this little mind-hole goes, shall we?"));
        } if (this.hasVoice(Voice.SMITTEN)) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "We'll win her heart eventually!"));
        } if (this.hasVoice(Voice.PARANOID) && this.ch3Voice != Voice.PARANOID) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "It's going to get so loud in here. How are we going to keep it all straight?"));
        } if (this.hasVoice(Voice.BROKEN) && this.ch3Voice != Voice.BROKEN) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "Oh, great. So it's going to get even more crowded. Even more deluded voices that think we might stand any kind of chance."));
        } if (this.hasVoice(Voice.OPPORTUNIST)) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "Flawless idea if you ask me. Such a go-getter attitude!"));
        } if (this.hasVoice(Voice.CONTRARIAN)) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Who cares about getting better than her. Let's do something weird. Like really, really weird."));
        }
        
        parser.printDialogueLine(new PrincessDialogueLine("Come onnnnnn! Show me something new!"));
        if (this.hasVoice(Voice.SKEPTIC)) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "Okay. Plan. Now!"));
        }

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);

            // Handle "slay" command here, redirecting to different options in the menu based on which fight options are available
            if (activeOutcome.equals("cSlayPrincess")) {
                if (!this.hasVoice(Voice.STUBBORN)) {
                    this.activeOutcome = "stubborn";
                } else if (bladePath && !this.hasVoice(Voice.COLD)) {
                    this.activeOutcome = "coldA";
                } else {
                    this.activeOutcome = "noFightOptions"; // Fails
                }
            }

            switch (activeOutcome) {
                case "coldA":
                case "coldB":
                case "coldNWO":
                    this.repeatActiveMenu = false;
                    parser.printDialogueLine("It doesn't work, and she kills you again. And again, and again, and again. Your memory blurs as your consciousness leaps from life to life to life, holding only snippets of the conflict that transpires.");
                    this.razor3MontageVoiceComment(Voice.COLD);
                    break;
                    
                case "stubborn":
                    this.repeatActiveMenu = false;
                    parser.printDialogueLine("It doesn't work, and she kills you again. And again, and again, and again. Your memory blurs as your consciousness leaps from life to life to life, holding only snippets of the conflict that transpires.");
                    this.razor3MontageVoiceComment(Voice.STUBBORN);
                    break;
                    
                case "oppo":
                    this.repeatActiveMenu = false;
                    parser.printDialogueLine("It doesn't work, and she kills you again. And again, and again, and again. Your memory blurs as your consciousness leaps from life to life to life, holding only snippets of the conflict that transpires.");
                    this.razor3MontageVoiceComment(Voice.OPPORTUNIST);
                    break;
                    
                case "broken":
                    this.repeatActiveMenu = false;
                    parser.printDialogueLine("It doesn't work, and she kills you again. And again, and again, and again. Your memory blurs as your consciousness leaps from life to life to life, holding only snippets of the conflict that transpires.");
                    this.razor3MontageVoiceComment(Voice.BROKEN);
                    break;
                    
                case "hunted":
                    this.repeatActiveMenu = false;
                    parser.printDialogueLine("It doesn't work, and she kills you again. And again, and again, and again. Your memory blurs as your consciousness leaps from life to life to life, holding only snippets of the conflict that transpires.");
                    this.razor3MontageVoiceComment(Voice.HUNTED);
                    break;
                    
                case "smitten":
                    this.repeatActiveMenu = false;
                    parser.printDialogueLine("It doesn't work, and she kills you again. And again, and again, and again. Your memory blurs as your consciousness leaps from life to life to life, holding only snippets of the conflict that transpires.");
                    this.razor3MontageVoiceComment(Voice.SMITTEN);
                    break;
                    
                case "para":
                    this.repeatActiveMenu = false;
                    parser.printDialogueLine("It doesn't work, and she kills you again. And again, and again, and again. Your memory blurs as your consciousness leaps from life to life to life, holding only snippets of the conflict that transpires.");
                    this.razor3MontageVoiceComment(Voice.PARANOID);
                    break;
                    
                case "cSlaySelf":
                case "contra":
                    this.repeatActiveMenu = false;
                    parser.printDialogueLine("It doesn't work, and she kills you again. And again, and again, and again. Your memory blurs as your consciousness leaps from life to life to life, holding only snippets of the conflict that transpires.");
                    this.razor3MontageVoiceComment(Voice.CONTRARIAN);
                    break;
                    
                case "skeptic":
                    this.repeatActiveMenu = false;
                    parser.printDialogueLine("It doesn't work, and she kills you again. And again, and again, and again. Your memory blurs as your consciousness leaps from life to life to life, holding only snippets of the conflict that transpires.");
                    this.razor3MontageVoiceComment(Voice.SKEPTIC);
                    break;

                case "cGoStairs":
                    parser.printDialogueLine(new VoiceDialogueLine("The stairs are practically a slide, remember? You have no way out."));
                    break;

                case "cSlaySelfFail":
                    if (!bladePath) {
                        this.giveDefaultFailResponse(activeOutcome);
                        break;
                    }
                case "cSlayPrincessFail":
                case "noFightOptions":
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "We already *tried* that!"));
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Ending montage
        Voice[] voicesOrder = {Voice.STUBBORN, Voice.CONTRARIAN, Voice.BROKEN, Voice.HUNTED, Voice.SMITTEN, Voice.PARANOID, Voice.COLD, Voice.OPPORTUNIST, Voice.SKEPTIC};
        int segmentNum = 0; // Technically speaking, segment 0 is the segment from the options menu above
        boolean contraLast = false;

        for (Voice v : voicesOrder) {
            if (this.hasVoice(v)) continue;

            segmentNum += 1;
            if (segmentNum > 5) throw new RuntimeException("Impossible segment number");

            switch (segmentNum) {
                case 1:
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Well, there's more of us. Let's see if that helps."));
                    break;

                case 2:
                    if (!bladePath || contraLast) {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "More noise isn't helping. It's just making it harder to focus."));
                    } else {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Do you see that? We almost had her!"));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "That was luck."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "But we only have to get lucky once."));
                    }

                    break;

                case 3:
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "It doesn't matter how many times this takes. We can't give up."));

                    if (bladePath) {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "*Sigh.* Okay. Let's go again."));
                    } else {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "We don't even have a weapon."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Yeah. Some clod threw it out the window."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Again. That was you!"));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "No, I was just the clod who suggested it. And if I knew we'd be stuck here forever, I wouldn't have done that."));
                    }

                    break;

                case 4:
                    if (bladePath) {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "See? We're getting better."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Okay. Okay, yeah. That was a good one."));
                    } else {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "See? We lasted a little longer."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Barely."));
                    }

                    break;

                case 5:
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "We're getting close to something, can't you feel it? One. Last. Time."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "You're right. One last time. That's all we need."));
                    break;
            }

            this.razor3MontageVoiceComment(v);
            contraLast = v == Voice.CONTRARIAN;
        }

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("And then everything goes dark, and you die."));
    }

    /**
     * The Voices and the Princess comment on a segment of the montage in Chapter III: The Arms Race / No Way Out
     * @param bladePath whether the player took the blade in The Razor (leading to The Arms Race) or not (leading to No Way Out)
     * @param v the Voice to add this segment
     */
    private void razor3MontageVoiceComment(Voice v) {
        if (this.hasVoice(v)) {
            throw new RuntimeException("Cannot get Razor montage comment for a Voice that is already present");
        }

        this.addVoice(v);
        System.out.println();

        switch (v) {
            case BROKEN:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "What's the point? It's all the same."));
                parser.printDialogueLine(new VoiceDialogueLine("She skewers you."));
                parser.printDialogueLine(new PrincessDialogueLine("Oh, don't give up on me just yet! You gotta keep going!"));
                break;
                
            case COLD:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "She's going to kill this body either way. So stop feeling what it feels."));
                parser.printDialogueLine(new VoiceDialogueLine("She skewers you."));
                parser.printDialogueLine(new PrincessDialogueLine("Ooooh. Not bad! Real tough!"));
                break;
                
            case CONTRARIAN:
                parser.printDialogueLine(new VoiceDialogueLine("And then you skewer yourself."));
                parser.printDialogueLine(new PrincessDialogueLine("I thought we both understood that dying doesn't get you anywhere."));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Huh. That didn't do much of anything. We're tougher than I thought."));
                break;
                
            case HUNTED:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "Just keep dodging. Just keep dodging. Just keep dodging."));
                parser.printDialogueLine(new VoiceDialogueLine("She skewers you."));
                parser.printDialogueLine(new PrincessDialogueLine("What's the point of avoiding me if you're not going to fight."));
                break;
                
            case OPPORTUNIST:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "Let's appeal to her better nature! We haven't tried that. I'm sure she'll listen to reason."));
                parser.printDialogueLine(new VoiceDialogueLine("She skewers you."));
                break;
                
            case PARANOID:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "Just panic! Flee!"));
                parser.printDialogueLine(new VoiceDialogueLine("She skewers you."));
                parser.printDialogueLine(new PrincessDialogueLine("No, you don't get to escape! That's not how this works."));
                break;
                
            case SKEPTIC:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "None of this is working! Think. Think!"));
                parser.printDialogueLine(new VoiceDialogueLine("She skewers you."));
                break;
                
            case SMITTEN:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "Compliment her on those gleaming blades! There's nothing better than a capable woman."));
                parser.printDialogueLine(new VoiceDialogueLine("She skewers you."));
                parser.printDialogueLine(new PrincessDialogueLine("You're cute."));
                break;
                
            case STUBBORN:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "We just have to hit her harder!"));
                parser.printDialogueLine(new VoiceDialogueLine("She skewers you."));
                parser.printDialogueLine(new PrincessDialogueLine("You'll have to be trickier than that."));
                break;
        }
    }


    // - Chapter IV: Mutually Assured Destruction / The Empty Cup -

    /**
     * Runs Chapter IV: Mutually Assured Destruction / The Empty Cup
     * @param bladePath whether the player took the blade in The Razor (leading to Mutually Assured Destruction) or not (leading to The Empty Cup)
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding razor4(boolean bladePath) {
        // You have all Voices
        
        parser.printDialogueLine(new VoiceDialogueLine("You're on a --", true));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Don't lose your head. We're in a cabin, and we'll take it from here."));

        if (bladePath) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "Everything feels like it finally fits, doesn't it? We're up here which is different, and different is good. And our steel claw is already in our hand."));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Oho! What if we throw it out the window?"));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "Over my dead body."));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "That wouldn't be very hard. We've died a lot. But I can't say I mind anymore."));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "Besides, what better way to die so very many times than at the sharp hands of a beautiful woman."));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "I'm sure I can think of a better way to die."));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "Eh, they're all the same, really."));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "How about we stop thinking about horrible ways to die? I don't want us to accidentally *manifest* anything."));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "The only thing we're going to manifest is finally ending up on top."));
        } else {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "No steel claw though."));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Was tossing it the only thing we've done that was permanent? That's a sick joke, universe. A sick, sick joke!"));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "If it's gone for good, then maybe we never actually needed it."));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "That's what I've been telling you all. We can do this without it. We're tougher than steel."));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "Yeah. Mind over matter."));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "Who needs violence when you have love?"));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "Who needs love when you've mastered yourself?"));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "Who needs anything when we don't matter?"));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "Well, boys? Are we ready?"));
        }
        
        parser.printDialogueLine(new VoiceDialogueLine("There are entirely too many of you. How many times have you been here?! This isn't good, this is --", true));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "How about you stick to describing things, and we'll stick to doing them?"));

        if (bladePath) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "Yeah. Leave it to the pros. We'll notch up that win in no time."));
        } else {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "Yeah. Leave it to the pros."));
        }
        
        this.currentLocation = GameLocation.BASEMENT;
        this.withPrincess = true;
        parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "Narrator! We heroically stride through the door and towards our destined final encounter with our star-crossed lover!"));
        parser.printDialogueLine(new VoiceDialogueLine("Fine by me. You walk to the door and onto the basement stairs, only --", true));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "It's more of a slide? We know."));
        parser.printDialogueLine(new VoiceDialogueLine("Fine. I'll just shut up then and speed this whole thing along."));
        parser.printDialogueLine(new VoiceDialogueLine("... Are you sure you don't want me to describe the stairs? Or this room? Or anything? It feels like I'm hardly a part of this."));

        if (bladePath) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Don't care. Just want to win."));
        } else {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Don't care. Just want to see how this ends."));
        }

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("Fine. You make your way to the basement."));
        parser.printDialogueLine(new PrincessDialogueLine("You know, this last time I killed you and you didn't pop right back up again?"));
        parser.printDialogueLine(new PrincessDialogueLine("I thought I'd actually done it! I thought I'd cut you into so many pieces you just weren't able to stitch yourself back together."));
        parser.printDialogueLine(new PrincessDialogueLine("But I guess we're not done! That's okay with me. It's good, even. I like that!"));
        parser.printDialogueLine(new PrincessDialogueLine("I got something ready for you while you were gone. Do you want to see it?"));
        parser.printDialogueLine(new PrincessDialogueLine("I'm not going to wait for an answer. I'm just gonna show you! It's worth it though. Just you wait. And not for very long, because I'm going to do it right now."));
        
        parser.printDialogueLine(new VoiceDialogueLine("*Distracted humming...*"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Are you going to say what she does?"));
        parser.printDialogueLine(new VoiceDialogueLine("Oh, do you want me to talk now?"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Well, yeah. She says she has something new. I want to hear about the new thing."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Yeah, me too."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "I think I speak for all of us when I say that I would like to hear you describe her new thing."));
        parser.printDialogueLine(new VoiceDialogueLine("Really? Okay then."));

        System.out.println();
        parser.printDialogueLine(new PrincessDialogueLine("Here we go! Now!"));
        parser.printDialogueLine(new VoiceDialogueLine("The Princess's skin twists, splitting into red blooms of raw meat as it stretches and tears. And then it... erupts."));
        parser.printDialogueLine(new VoiceDialogueLine("She becomes a wave of blood and viscera, pieces of her splattering against the walls. All that remains in the center of the room is a skeleton of blades. A heart beats furiously in its cage of a chest."));
        parser.printDialogueLine(new PrincessDialogueLine("Are you ready for what comes next?"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Holy *shit!*"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "She's gorgeous! Absolutely divine!"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "Yes! Behold, the perfect woman!"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Do you think we can throw *her* out the window?"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "That looked... painful."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "How is she still alive?"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "Heart's still beating. That's all she needs."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "This is fake! This is all fake! That's all! Just made up!"));

        if (bladePath) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "She doesn't even have a back anymore. How are we supposed to stab her in it?"));
        } else {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "I'd say we bow down to her right now if that had ever even slightly worked for us."));
        }
        
        parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "This is all just a sick joke. I hate existing."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "We're screwed. I quit! I'm done! Forget it!"));

        System.out.println();
        parser.printDialogueLine("They begin to talk over one another. Their arguments go in circles, never leading anywhere, and all the while the noise builds until it is nearly unbearable. You can barely even hear yourself think.");

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "empty", "[Empty your mind.]"));
        activeMenu.add(new Option(this.manager, "empty2", "[Him too.]", activeMenu.get("empty")));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu, new DialogueLine("[You have no other option.]", true))) {
                case "empty":
                    parser.printDialogueLine("All at once, it is blessedly quiet.");
                    parser.printDialogueLine("You stand in front of the Princess in a basement identical to the one you first encountered oh so long ago.");
                    parser.printDialogueLine(new VoiceDialogueLine("... What just happened? It's so quiet."));
                    break;

                case "empty2":
                    this.repeatActiveMenu = false;
                    break;
            }
        }
        
        if (this.isFirstVessel) {
            parser.printDialogueLine("The basement is gone, replaced by a textured nothingness.");
        } else {
            parser.printDialogueLine("The basement is gone, replaced by a textured nothingness. Somehow, it feels familiar.");
        }

        parser.printDialogueLine(new PrincessDialogueLine("Something feels different about you. It almost makes *me* feel different. Like I should actually take this seriously for once."));

        if (bladePath) {
            parser.printDialogueLine("You do not act, and yet through that inaction your body moves on its own. The Princess strikes as you approach, but as her blow finishes its arc, you're already somewhere else.");
            parser.printDialogueLine(new PrincessDialogueLine("You're incredible."));
            parser.printDialogueLine("Your weapons clash again and again, you and her entering a rhythm free of thought and free of self.");
            parser.printDialogueLine("There is only the dance. The ebb and flow, the shifting of the tides back and forth between you.");
            parser.printDialogueLine("The deeper you fall into your play, the faster your hearts pound, and the faster the momentum volleys between you.");
            parser.printDialogueLine("An endlessly building crescendo and then... an opening.");
            parser.printDialogueLine("Your blade strikes free of volition, and hers strikes, too.");
            parser.printDialogueLine("Both strikes are lethal. Neither of you will survive, but neither of you fear what's to come. This is a good ending.");

            System.out.println();
            parser.printDialogueLine("Something reaches out and folds her into its myriad arms.");
            if (this.isFirstVessel) {
                parser.printDialogueLine("You do not get to see each other die. Something has taken her away, and it's left something else in her place.");
            } else {
                parser.printDialogueLine("You do not get to see each other die. Nor will you ever. It's time for you to leave. Memory returns.");
            }

            return ChapterEnding.MUTUALLYASSURED;
        } else {
            parser.printDialogueLine("You do not act as the Princess approaches, instead allowing her to crash against your form. And yet, there is seemingly nothing for her to crash against.");
            parser.printDialogueLine("Again and again she swings at \"you,\" but there never really was a \"you\" to swing at.");
            parser.printDialogueLine(new PrincessDialogueLine("This worked before. I was able to make you dead before!"));
            parser.printDialogueLine("She swings again, and this time, she hits something, or something hits her. She looks down in confused terror as her arm bends and folds in upon itself.");
            parser.printDialogueLine(new PrincessDialogueLine("Did you do that? It's funny if you did. You're nothing! You've done nothing to me and I've done so much to you and --"));
            parser.printDialogueLine(new PrincessDialogueLine("That's who we are. But it's like you're nothing now. You can't be nothing! If you're nothing, then what am I? Am I nothing, too?"));
            parser.printDialogueLine(new PrincessDialogueLine("No! I'm the one who hurts you!"));
            parser.printDialogueLine("She hurls herself at you, but as she does, her metal body bends outward, the very contact with what you are repelling her to the point of destruction.");
            parser.printDialogueLine("The din of shrieking metal subsides, and something small and delicate falls into your hand. It's her heart. It beats gently, calmly, in your palm.");

            System.out.println();
            parser.printDialogueLine("Something reaches out from the darkness and gently takes her away.");
            if (this.isFirstVessel) {
                parser.printDialogueLine("Not another word is spoken. She's gone, replaced with something else.");
            } else {
                parser.printDialogueLine("Not another word is spoken. It's time for you to leave. Memory returns.");
            }

            return ChapterEnding.EMPTYCUP;
        }
    }


    // - Chapter II: The Beast -

    /**
     * Runs Chapter II: The Beast
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding beast() {
        // You gain the Voice of the Hunted

        if (!this.chapter2Intro(true, false, false)) {
            return ChapterEnding.ABORTED;
        }

        manager.setNowPlaying("The Beast");
        
        parser.printDialogueLine(new VoiceDialogueLine("The interior of the cabin is ruinous and dilapidated. It feels like no one has lived here for a long time, wind rushing in through cracks and holes in the wooden walls. The only furniture of note is a termite-eaten table with a pristine blade perched on its edge."));
        parser.printDialogueLine(new VoiceDialogueLine("The blade is your implement. You'll need it if you want to do this right."));

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "mirror", "(Explore) You didn't say anything about the mirror on the wall."));
        activeMenu.add(new Option(this.manager, "different", "(Explore) This whole cabin is different than last time.", this.sharedLoopInsist));
        activeMenu.add(new Option(this.manager, "approach", "(Explore) [Approach the mirror.]"));
        activeMenu.add(new Option(this.manager, "take", "(Explore) [Take the blade.]"));
        activeMenu.add(new Option(this.manager, "enter", "[Enter the basement.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "mirror":
                    parser.printDialogueLine(new VoiceDialogueLine("That's because there isn't a mirror. There's a table, the blade sitting on the table, and the door to the basement. There's nothing else in here."));
                    if (this.ch2AskMirror()) {
                        activeMenu.setCondition("approach", false);
                    }
                    break;

                case "different":
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "*Very* different."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "I wonder why."));
                    parser.printDialogueLine(new VoiceDialogueLine("Maybe that's because you haven't actually been here. I hope this means you'll finally drop that ridiculous past-life nonsense. You haven't died, and you certainly haven't been killed by the Princess."));
                    parser.printDialogueLine(new VoiceDialogueLine("So focus up. The world is depending on you."));
                    break;

                case "cApproachMirror":
                    activeMenu.setCondition("approach", false);
                case "approach":
                    activeMenu.setCondition("mirror", false);
                    this.ch2ApproachMirror();
                    break;

                case "cTake":
                    activeMenu.setCondition("take", false);
                case "take":
                    this.hasBlade = true;
                    this.withBlade = false;
                    parser.printDialogueLine(new VoiceDialogueLine("You take the blade from the table. It would be difficult to slay the Princess and save the world without a weapon."));
                    break;

                case "cGoStairs":
                case "enter":
                    this.repeatActiveMenu = false;
                    break;

                default:
                    this.giveDefaultFailResponse(activeOutcome);
            }
        }

        if (!this.hasBlade) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "No steel claw. Do you think we can talk our way out of this? I don't think she wants to talk."));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I guess we'll just have to trust that we made the right call. It'll still be here if we need it."));
            System.out.println();
        }

        this.withBlade = false;
        this.mirrorPresent = false;
        parser.printDialogueLine(new VoiceDialogueLine("The door to the basement creaks open, revealing what's left of an old, wooden staircase. It's still sturdy enough that you can make your way down in one piece, though you'll have to be mindful of holes."));
        parser.printDialogueLine(new VoiceDialogueLine("The air seeping up from below is oddly warm and wet, as if you're descending into a jungle. If the Princess lives here, slaying her would probably be doing her a favor."));
        parser.printDialogueLine(new VoiceDialogueLine("She growls up the stairs."));
        parser.printDialogueLine(new PrincessDialogueLine("I can smell you."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "She sounds almost... feral."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "Impatient. Or maybe eager."));
        parser.printDialogueLine(new VoiceDialogueLine("You carefully make your way down the stairs."));

        this.currentLocation = GameLocation.BASEMENT;
        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("The last step gives way to the damp earth floor of a starlit pit. The walls are obscured by an impenetrable darkness, giving the illusion that the room might stretch on forever. You brush against the wide leaves of plants that surround you on all sides, seemingly the only living things that occupy this strange underground wilderness."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "The jungle is pressing in on us... hiding her from view. She could be anywhere."));
        parser.printDialogueLine(new VoiceDialogueLine("You see only a flash of the Princess before she scurries away into the underbrush, dragging her heavy chain behind her."));
        parser.printDialogueLine(new VoiceDialogueLine("Remember, she's just a Princess."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "She is certainly *not* just a Princess."));
        parser.printDialogueLine(new VoiceDialogueLine("You're not helping."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "It doesn't matter what she is. It only matters what she does."));
        parser.printDialogueLine(new VoiceDialogueLine("Her shining eyes appear between the leaves, staring hungrily at you from the darkness."));

        if (this.hasBlade) {
            parser.printDialogueLine(new PrincessDialogueLine("I can hear your heart pounding from the bottom of the stairs, fledgling. You're right to be terrified."));
            parser.printDialogueLine(new PrincessDialogueLine("I'm so much more than you, and a little splinter clutched in trembling hands won't save you from me."));
        } else {
            parser.printDialogueLine(new PrincessDialogueLine("I can hear your heart pounding from the bottom of the stairs, fledgling. You're right to be terrified. I'm so much more than you."));
        }



        
        // temporary templates for copy-and-pasting
        /*
        parser.printDialogueLine(new VoiceDialogueLine("XXXXX"));
        parser.printDialogueLine(new PrincessDialogueLine("XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "(Explore) XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "\"XXXXX\""));
        */
        
        // PLACEHOLDER
        return null;
    }


    // - Chapter III: The Den -

    /**
     * Runs Chapter III: The Den
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
     * Runs Chapter III: The Wild
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
     * Runs Chapter II: The Witch
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

        manager.setNowPlaying("The Witch");
        
        parser.printDialogueLine(new VoiceDialogueLine("The interior of the cabin is a mess of twisted roots, the walls a chaotic weave of knotted wood that, almost as if by accident just happened to resemble a room. The floor is damp and earthy, and the only furniture of note is a slab of mud in the shape of a shelf, with a pristine blade perched on its edge."));
        parser.printDialogueLine(new VoiceDialogueLine("The blade is your implement. You'll need it if you want to do this right."));

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "mirror", "(Explore) You didn't say anything about the mirror on the wall."));
        activeMenu.add(new Option(this.manager, "different", "(Explore) This whole cabin is different than last time.", this.sharedLoopInsist));
        activeMenu.add(new Option(this.manager, "approach", "(Explore) [Approach the mirror.]"));
        activeMenu.add(new Option(this.manager, "take", "(Explore) [Take the blade.]"));
        activeMenu.add(new Option(this.manager, "enter", "[Enter the basement.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "mirror":
                    parser.printDialogueLine(new VoiceDialogueLine("That's because there isn't a mirror. There's the muddy shelf, the blade sitting on the table, and the door to the basement. There's nothing else in here."));
                    if (this.ch2AskMirror()) {
                        activeMenu.setCondition("approach", false);
                    }
                    break;

                case "different":
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "*Very* different."));
                    parser.printDialogueLine(new VoiceDialogueLine("Maybe that's because you haven't actually been here. I hope this means you'll finally drop that ridiculous past-life nonsense. You haven't died, and you certainly haven't been killed by the Princess."));
                    parser.printDialogueLine(new VoiceDialogueLine("So focus up. The world is depending on you."));
                    break;

                case "cApproachMirror":
                    activeMenu.setCondition("approach", false);
                case "approach":
                    activeMenu.setCondition("mirror", false);
                    this.ch2ApproachMirror();
                    break;

                case "cTake":
                    activeMenu.setCondition("take", false);
                case "take":
                    this.hasBlade = true;
                    this.withBlade = false;
                    parser.printDialogueLine(new VoiceDialogueLine("You take the blade from the shelf. It would be difficult to slay the Princess and save the world without a weapon."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "Well, if we're grabbing a weapon, we should probably keep it hidden behind our backs. She doesn't have to know we have it."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "That's not actually not a bad idea."));
                    break;

                case "cGoStairs":
                case "enter":
                    this.repeatActiveMenu = false;
                    break;

                default:
                    this.giveDefaultFailResponse(activeOutcome);
            }
        }

        if (!this.hasBlade) {
            if (this.sharedLoopInsist) {
                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "No blade? Leaving it behind didn't work out so well for us last time..."));
            } else {
                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "No blade. I hope you know what you're getting us into."));
            }

            parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "It'll always be here if we need it. Sure, that was also true last time, and we still died. But we definitely know what we're doing *this* time."));
            System.out.println();
        }

        this.currentLocation = GameLocation.STAIRS;
        this.withBlade = false;
        this.mirrorPresent = false;
        parser.printDialogueLine(new VoiceDialogueLine("The door to the basement creaks open, revealing a staircase dug into the muddy earth below. The ceiling is thick with roots that hang like locks of tangled hair."));
        parser.printDialogueLine(new VoiceDialogueLine("The weak starlight from the cabin windows behind you can barely penetrate the gloom here, only illuminating the edges of an opening below. It shines in the darkness like some kind of massive maw, waiting to swallow you up into the earth."));
        parser.printDialogueLine(new VoiceDialogueLine("The air smells of dirt and copper. It's thick and wet, as if your lungs are being coated in mud with each intake of breath. If the Princess lives here, slaying her would probably be doing her a favor."));
        parser.printDialogueLine(new VoiceDialogueLine("Her voice skitters up the stairs."));
        parser.printDialogueLine(new PrincessDialogueLine("Something nasty finds itself on my stairs. Come on down, don't be scared. I probably won't bite."));

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "notNasty", "\"I'm not nasty!\""));
        activeMenu.add(new Option(this.manager, "hello", "\"Hello.\""));
        activeMenu.add(new Option(this.manager, "silent", "[Say nothing.]"));

        this.repeatActiveMenu = true;
        while (this.repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "notNasty":
                    this.repeatActiveMenu = false;
                    parser.printDialogueLine(new PrincessDialogueLine("But you are. You're a wretched little thing."));
                    parser.printDialogueLine(new PrincessDialogueLine("I recognize that voice as easily as I recognized your nervous little footsteps coming up the path. I know who you are, and I remember what you've done."));
                    break;

                case "hello":
                    this.repeatActiveMenu = false;
                    parser.printDialogueLine(new PrincessDialogueLine("I recognize that voice as easily as I recognized your nervous little footsteps coming up the path. I know who you are, and I remember what you've done."));
                    break;

                case "cGoBasement":
                case "silent":
                    this.repeatActiveMenu = false;
                    parser.printDialogueLine(new PrincessDialogueLine("Silence, I see. Don't think I've forgotten about you. I recognized the sound of your nervous little footsteps as soon as they came into my home. I know who you are, and I remember what you've done."));
                    break;
                
                case "cGoCabin":
                    parser.printDialogueLine(new VoiceDialogueLine("What? No. You're already halfway down the stairs, you can't just turn around now."));
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        if (this.sharedLoopInsist) {
            parser.printDialogueLine(new VoiceDialogueLine("She must have you confused with someone else."));
        } else {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "See? She knows us. Is this enough for you to believe what we've been saying?"));
            parser.printDialogueLine(new VoiceDialogueLine("Maybe, but you shouldn't let that cloud your judgment. She's just a Princess. As long as you remember that and remain focused, slaying her will be *easy.*"));
        }
        
        parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "She seems friendly enough. Maybe we can talk our way out of this whole situation."));
        parser.printDialogueLine(new VoiceDialogueLine("*Sigh.* You can't. Unless you slay her right away, she's going to break free and end the world. There's no reasoning with what she is."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "Look, I'm just throwing ideas out there. I like to think out loud. I'm the kind of guy who likes a *discussion,* don't we want to hear what everyone has to say before making any big decisions?"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Do you want to hear what everyone has to say, or do you just want to hear yourself talk?"));
        parser.printDialogueLine(new VoiceDialogueLine("You need to stop lingering. Your task is to *slay* the Princess, not endlessly debate about what to do with the Princess."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "Fine, fine. You're the boss."));
        parser.printDialogueLine(new VoiceDialogueLine("Thank you. You descend the basement steps, entering the dark room below."));

        this.currentLocation = GameLocation.BASEMENT;
        this.withPrincess = true;
        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("You can just make out the shape of the Princess in the gloom. She's huddled against the far wall, her eyes bright and glaring from amid the thick roots."));
        parser.printDialogueLine("Her appearance is unkempt, her hair wild and littered with twigs, her dress in tatters. The tail of a lion swishes behind her, and her nose and mouth resemble those of a cat.");

        if (this.hasBlade) {
            parser.printDialogueLine(new PrincessDialogueLine("And there you are, one hand tucked away behind your back, gripping that sharp, sharp blade, no doubt."));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "That's no fair, how would she know that?"));
            parser.printDialogueLine(new PrincessDialogueLine("So we've dropped the pretenses."));
            parser.printDialogueLine(new PrincessDialogueLine("Good."));
        } else {
            parser.printDialogueLine(new PrincessDialogueLine("And there you are, once again seeming to offer a helping hand while likely hiding the other behind your back. Fine. I'll play along for now. What do you want?"));
        }
        
        
        // temporary templates for copy-and-pasting
        /*
        parser.printDialogueLine(new VoiceDialogueLine("XXXXX"));
        parser.printDialogueLine(new PrincessDialogueLine("XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "(Explore) XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "\"XXXXX\""));
        */
        
        // PLACEHOLDER
        return null;
    }


    // - Chapter III: The Thorn -

    /**
     * Runs Chapter III: The Thorn
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
     * Runs Chapter II: The Stranger
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding stranger() {
        // You gain the Voice of the Contrarian

        this.secondaryScript = new Script(this.manager, this.parser, "Chapter2Shared");
        if (this.isFirstVessel) manager.setFirstPrincess(Chapter.STRANGER);

        manager.setNowPlaying("Fragmentation");

        parser.printDialogueLine(new VoiceDialogueLine("You're on a path in the woods. And at the end of that path is a cabin. And in the basement of that cabin is a princess."));
        parser.printDialogueLine(new VoiceDialogueLine("You're here to slay her. If you don't, it will be the end of the world."));

        System.out.println();
        parser.printDialogueLine("This time, the path is walled in on either side. The path behind you is blocked off.");

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
                    parser.printDialogueLine(new VoiceDialogueLine("The wall blocks your path. It seems you have no choice but to go to the cabin."));
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
        parser.printDialogueLine("You emerge into the clearing, now enclosed by the same walls as before. The cabin waits at the top of the hill.");
        
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
                    parser.printDialogueLine(new VoiceDialogueLine("The wall blocks your path. It seems you have no choice but to enter the cabin."));
                    
                default:
                    this.giveDefaultFailResponse(this.activeOutcome);
            }
        }

        this.currentLocation = GameLocation.CABIN;
        this.mirrorPresent = true;
        this.knowsBlade = true;
        this.withBlade = true;

        manager.setNowPlaying("The Stranger");
        
        parser.printDialogueLine(new VoiceDialogueLine("The cabin interior is wrong, a confusing patchwork of many cabin interiors all projected across what's *almost* the same space. But it's all shifted -- an inch here, a foot there -- such that the seams are never quite visible enough for the place to make any sense."));
        parser.printDialogueLine(new VoiceDialogueLine("The only furniture of note is a plain table, its legs all the wrong lengths, its material devoid of feature. Perched on that table is a pristine blade."));
        parser.printDialogueLine(new VoiceDialogueLine("The blade is your implement. You'll need it if you want to do this right."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "If He wants us to take it, maybe we should just leave it to collect dust. Or better yet, grab it and throw it out the window! What good is a knife against a world-ending monstrosity anyways?"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "No, we're taking the knife. H-have you seen this place? We have literally no idea what to expect, and no idea what we're dealing with."));
        parser.printDialogueLine(new VoiceDialogueLine("I've already told you what you're dealing with. You're dealing with a *Princess.* How many times do I have to explain this incredibly simple and straightforward premise?"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "You can't just say that! Not when everything here is so wrong."));
        parser.printDialogueLine(new VoiceDialogueLine("Listen to me. My job is to describe facts as facts, and to guide you through your job, which is to slay the Princess, and through that action, save the entire world."));
        parser.printDialogueLine(new VoiceDialogueLine("And if you're going to slay her, you cannot let fear creep into your heart. You cannot lose yourself before you even get to her."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Ohoho! You've piqued my interest. What's going to happen if we \"lose ourself?\""));
        parser.printDialogueLine(new VoiceDialogueLine("Nothing. Because you're going to pull yourself together."));
        parser.printDialogueLine(new VoiceDialogueLine("Just ignore the stressful geometry and stay calm."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "How?! Even if we closed our eyes you're constantly describing it to us."));
        parser.printDialogueLine(new VoiceDialogueLine("I'm not going to stop doing my job, so you're just going to have to get better at yours. And quickly, if you don't mind."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Yes, take a deep breath. I'm all for getting under His skin, but we'll miss out on loads of fun if we shrivel up into a ball and go insane the first time we see something weird."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "What you're seeing here is obviously real. Just accept it and go with the flow. It really isn't hard."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Okay. Okay. I'm fine."));
        parser.printDialogueLine(new VoiceDialogueLine("Good. Now whenever you're ready, we're all waiting for you to complete a very important task."));

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "mirror", "(Explore) You didn't say anything about the mirror on the wall."));
        activeMenu.add(new Option(this.manager, "approach", "(Explore) [Approach the mirror.]"));
        activeMenu.add(new Option(this.manager, "take", "(Explore) [Take the blade.]"));
        activeMenu.add(new Option(this.manager, "throw", "(Explore) [Throw the blade out the window.]"));
        activeMenu.add(new Option(this.manager, "enter", "[Enter the basement.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "mirror":
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Ooh! We should look at ourselves. Wouldn't that be fun?"));
                    parser.printDialogueLine(new VoiceDialogueLine("You won't be looking at yourself because there isn't a mirror. There's the table, the blade sitting on the table, and the door to the basement. There's nothing else in here."));
                    if (this.ch2AskMirror()) {
                        activeMenu.setCondition("approach", false);
                    }
                    break;

                case "cApproachMirror":
                    activeMenu.setCondition("approach", false);
                case "approach":
                    activeMenu.setCondition("mirror", false);
                    this.ch2ApproachMirror();
                    break;

                case "cTake":
                    activeMenu.setCondition("take", false);
                case "take":
                    this.hasBlade = true;
                    this.withBlade = false;
                    this.canThrowBlade = true;
                    activeMenu.setCondition("throw", true);
                    parser.printDialogueLine(new VoiceDialogueLine("You take the blade from the table. It would be difficult to slay the Princess and save the world without a weapon."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Okay, fine. You took the knife. But you really shouldn't hold it like *that.*"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Then how are we supposed to hold it?"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "The *other* way. Thumb at the bottom. We'll look much cooler and more serious if we hold it with our thumb at the bottom."));
                    parser.printDialogueLine(new VoiceDialogueLine("It really doesn't matter how you hold the blade, as long as you have it. Just make a choice."));

                    OptionsMenu subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "keep", "[Keep your grip as it is.]"));
                    subMenu.add(new Option(this.manager, "reverse", "[Hold the blade the other way.]"));

                    switch (parser.promptOptionsMenu(subMenu)) {
                        case "keep":
                            parser.printDialogueLine(new VoiceDialogueLine("Great. You keep your grip the way it is. Your task awaits."));
                            break;

                        case "reverse":
                            this.bladeReverse = true;
                            parser.printDialogueLine(new VoiceDialogueLine("You switch your grip on the blade. Congratulations."));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Yes! Isn't this so much better?"));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Okay, fine. You're right. This does look a lot better."));
                            parser.printDialogueLine(new VoiceDialogueLine("It really doesn't matter. Just get on with it and deal with the Princess already."));
                            break;
                    }

                    break;

                case "cThrow":
                    activeMenu.setCondition("throw", false);
                case "throw":
                    this.hasBlade = false;
                    this.canThrowBlade = false;
                    this.threwBlade = true;
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Hahaha, *yes!* Do it!"));
                    parser.printDialogueLine(new VoiceDialogueLine("Seriously? *Sigh.* You throw the blade at the window, glass showering the cabin as your weapon flies out into the night. I suppose you'll just have to deal with the Princess without it."));

                    if (this.sharedLoop) {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "We'll be fine. Don't worry about it. What's the worst that could happen, the world ends? Been there, done that."));
                    } else {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "We'll be fine. Don't worry about it. What's the worst that could happen, the world ends? Oh well. If the Princess wasn't going to do it, the heat death of the universe was going to come for it eventually."));
                    }

                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I'm not so sure. This place is already messing with my head. It would be much better if we had a weapon."));
                    parser.printDialogueLine(new VoiceDialogueLine("What's done is done. Good luck, *hero.*"));
                    break;

                case "cGoStairs":
                case "enter":
                    this.repeatActiveMenu = false;
                    break;

                default:
                    this.giveDefaultFailResponse(activeOutcome);
            }
        }

        this.currentLocation = GameLocation.STAIRS;
        this.withBlade = false;
        this.canThrowBlade = false;
        this.mirrorPresent = false;
        parser.printDialogueLine(new VoiceDialogueLine("The door to the basement creaks open, revealing a web of branching staircases all built from unidentifiable materials."));
        parser.printDialogueLine(new VoiceDialogueLine("Nothing here seems to belong, and the closer you examine your surroundings, the more confused you get, your head throbbing with the effort of making sense of this place. None of the stairs even seem to go anywhere, let alone down."));
        parser.printDialogueLine(new VoiceDialogueLine("The air here has a sickening, almost sludge-like miasma to it, the kind of indiscernible quality that comes from the blending together of every scent there is at once. An odor that is simultaneously everything and yet the sum of it all coalescing into a thick, nauseating nothing."));
        parser.printDialogueLine(new VoiceDialogueLine("If the Princess lives here, slaying her is probably doing her a favor."));
        parser.printDialogueLine(new VoiceDialogueLine("Her voice, a disquieting collage of tone and personality, drags up the stairs."));
        parser.printDialogueLine(new PrincessDialogueLine("Hello? HI. What are you doing here? Are you here to -- KILL."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Mmmm. No. No thank you."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Oh, don't be such a baby!"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I don't want to do this. Let's just turn around and leave. This feels wrong, this feels like a trap, like whatever we do we're gonna die."));
        if (!this.hasBlade) parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "We don't even have a weapon..."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "But we already tried turning around and leaving, didn't we? And He threw up a wall. No way to go but forward, and whatever choice we make, whatever she is, we know one thing for sure."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "And what's that?"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "There'll still be plenty of ways to ruin His day."));

        int schismCount = 1;
        boolean schismHarsh = false;
        boolean schismNeutral = false;
        boolean schismGentle = false;
        boolean schismEmo = false;
        boolean schismMonster = false;
        String firstSchism = "";

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "left", "[Take the harsh stairs to the left.]"));
        activeMenu.add(new Option(this.manager, "center", "[Take the center staircase.]"));
        activeMenu.add(new Option(this.manager, "right", "[Take the gentle stairs to the right.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "left":
                    this.repeatActiveMenu = false;
                    firstSchism = "harsh";
                    parser.printDialogueLine(new VoiceDialogueLine("You step to the left. The path is cruel against your feet, the impact of each step sending pulsing vibrations up your legs until there's nothing left in them to feel."));
                    parser.printDialogueLine(new VoiceDialogueLine("The air around you grows colder the further you progress, at first a barely-noticeable drop, quickly evolving into a numbing cold."));
                    parser.printDialogueLine(new VoiceDialogueLine("Your toes feel like blocks of ice, your breaths puff out in clouds of condensed vapor."));
                    parser.printDialogueLine(new VoiceDialogueLine("You shudder against it as you continue down the stairway, losing yourself in the bone-deep chill."));
                    break;
                    
                case "center":
                    this.repeatActiveMenu = false;
                    firstSchism = "neutral";
                    parser.printDialogueLine(new VoiceDialogueLine("You step onto the center staircase. Paths wind out around you in all directions, each step branching into its own staircases which branch into their own staircases and so on. You aren't quite sure if yours is taking you up or down, but at the very least it's taking you somewhere."));
                    parser.printDialogueLine(new VoiceDialogueLine("You concentrate on where you are, careful not to stray onto any of the many splitting branches that tempt you on all sides. You wouldn't want to have to backtrack to yours once you'd made a decision that took you someplace else."));
                    parser.printDialogueLine(new VoiceDialogueLine("And so you take one careful, focused step after another. One foot down, another foot down, another after that. You lose yourself in following the correct pattern, in following what looks to you to be the true path, the one that cuts straight down. Or up. Or maybe sideways."));
                    parser.printDialogueLine(new VoiceDialogueLine("But no matter the direction it goes, it certainly is the most true path, you know that much."));
                    break;
                    
                case "right":
                    this.repeatActiveMenu = false;
                    firstSchism = "gentle";
                    parser.printDialogueLine(new VoiceDialogueLine("You step to the right. The path feels soft and reassuring against your feet. The stairs almost seem to cradle you as you make your way down, like they're guiding your heels from one step directly to the next. You barely have to extend any effort to descend, the stairway doing most of the work for you, and you don't feel like there's any concern that you might slip or tumble or lose your way."));
                    parser.printDialogueLine(new VoiceDialogueLine("But the further you go, the deeper you sink in. First it's like a lovely plush carpet, your toes digging down and barely hitting any resistance at all. But soon enough you're fighting just to keep your knees from sinking out of sight."));
                    parser.printDialogueLine(new VoiceDialogueLine("The softness threatens to swallow you whole, to wrest control of your body and surround you in a false ethereal bliss, pretending to save you from the cruelties of choice and consequence."));
                    parser.printDialogueLine(new VoiceDialogueLine("It's slow-going, but you manage to fight against the overwhelming urge to fall back into the comfort and nothingness, the very struggle to continue forward consuming your every thought."));
                    break;

                case "cGoBasement":
                    parser.printDialogueLine("You have to pick a staircase.");
                    break;

                case "cGoCabin":
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "If we try that, He'll just keep throwing up walls. Come on, just pick a staircase already!"));
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("You slowly lose sense of yourself the further you go."));
        parser.printDialogueLine(new VoiceDialogueLine("Time disappears, and you can feel yourself begin to untether."));
        parser.printDialogueLine(new VoiceDialogueLine("Physical sensations dull and then vanish, until the only things experienced are the endlessly repeating patterns and emotions of the journey. A continuous march forward to a destination long forgotten."));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("Consumption and betrayal. Skepticism and blind devotion. Rivalry and submission. Terror and longing. Pain and unfamiliarity. And at the heart of it all, an emotion that can only be described as --", true));

        this.currentLocation = GameLocation.BASEMENT;
        this.withPrincess = true;
        System.out.println();

        switch (firstSchism) {
            case "harsh":
                parser.printDialogueLine(new PrincessDialogueLine("Are you just going to stand there?"));
                break;

            case "neutral":
                parser.printDialogueLine(new PrincessDialogueLine("Can I help you?"));
                break;

            case "gentle":
                parser.printDialogueLine(new PrincessDialogueLine("Are you okay?"));
                break;
        }
        
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "What -- what the hell was that?! Wh-what happened to us?"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "I feel so... strange. Like I'm fundamentally different, but also... still the same person I was at the top of the stairs."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Oh well! That was a trip but now it's over. Time to get back to our old devilish ways."));
        parser.printDialogueLine(new VoiceDialogueLine("The Princess, eyes bright but otherwise shrouded in darkness, watches you impatiently from the other side of the basement. Don't forget why you're here."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "And why are we here again? In case you weren't listening, I'm afraid I lost myself on the way down."));
        parser.printDialogueLine(new VoiceDialogueLine("*Sigh.* You're here to --", true));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "He's just being an ass. We remember. Though I'm still not sure if we should trust you. Let's talk to her for a bit. Try and get our bearings. She seems... normal."));

        String endChoiceText = "\"I'm getting you out of here.\" [Try and free her.]\n  (NUM) \"I don't know what you are, but I can't trust you. I can't trust anyone here.\" [Leave her in the basement.]\n  (NUM) ";
        if (this.threwBlade) {
            endChoiceText += "[Regretfully think about that time you threw the blade out the window.]";
        } else if (!this.hasBlade) {
            endChoiceText += "[Retrieve the blade.]";
        } else {
            endChoiceText += "[Slay the Princess.]";
        }

        HashMap<String, String> currentQuestionLines = new HashMap<>();
        currentQuestionLines.put("harsh", "");
        currentQuestionLines.put("neutral", "");
        currentQuestionLines.put("gentle", "");
        currentQuestionLines.put("emo", "");
        currentQuestionLines.put("monster", "");

        String setNewSchism = "";
        boolean newSchismComment = false;
        boolean schismThisOption = false;
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "sorry", "(Explore) \"I'm sorry... I didn't realize I was here.\""));
        activeMenu.add(new Option(this.manager, "more", "(Explore) \"There's more of you now...\"", false));
        activeMenu.add(new Option(this.manager, "name", "(Explore) \"What's your name?\""));
        activeMenu.add(new Option(this.manager, "weird", "(Explore) \"Getting down here was... weird. Like I was pulled apart and put back together again. Do you know what happened to me?\""));
        activeMenu.add(new Option(this.manager, "reason", "(Explore) \"For all I know, you're locked up down here for a reason. Do you know why you're down here?\""));
        activeMenu.add(new Option(this.manager, "threatShare", "(Explore) \"You're apparently a threat to the world. I was sent here to slay you.\""));
        activeMenu.add(new Option(this.manager, "whatDo", "(Explore) \"If I let you out of here, what are you going to do?\"", false));
        activeMenu.add(new Option(true, this.manager, "ending", endChoiceText));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            schismThisOption = false;

            if (schismCount == 2) {
                activeMenu.setCondition("sorry", false);
                activeMenu.setCondition("more", true);
                activeMenu.setCondition("ending", true);
            } else if (schismCount == 5) {
                activeMenu.setCondition("more", false);
                activeMenu.setCondition("name", false);
                activeMenu.setCondition("weird", false);
                activeMenu.setCondition("reason", false);
                activeMenu.setCondition("threatShare", false);
                activeMenu.setCondition("whatDo", false);
            }

            switch (setNewSchism) {
                case "harsh":
                    schismHarsh = true;
                    break;
                    
                case "neutral":
                    schismNeutral = true;
                    break;
                    
                case "gentle":
                    schismGentle = true;
                    break;
                    
                case "emo":
                    schismEmo = true;
                    break;
                    
                case "monster":
                    schismMonster = true;
                    break;
            }
            setNewSchism = "";

            if (!newSchismComment && schismCount > 1) {
                newSchismComment = true;

                switch (schismCount) {
                    case 2:
                        parser.printDialogueLine(new VoiceDialogueLine("As the Princess speaks again, it's almost as if she fractures, and where there was once just one of her, there is now another."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "We can do that?!"));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I don't like this. It's those cabins all over again. Can... can we put her back?"));

                        if (this.sharedLoop) {
                            if (this.sharedLoopInsist) {
                                parser.printDialogueLine(new VoiceDialogueLine("You said \"the world ended\" last time you were here, didn't you?"));
                                parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Yeah, what of it?"));
                            } else {
                                parser.printDialogueLine(new VoiceDialogueLine("You said you'd been here before, right? What exactly happened last time?"));
                                parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Does it matter?"));
                                parser.printDialogueLine(new VoiceDialogueLine("Yes, it matters! But I'm not going to waste any more time prying out details if you're going to be so irritating about it."));
                            }
                        } else {
                            parser.printDialogueLine(new VoiceDialogueLine("\"Again?\" Have you been here before?"));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Should we tell Him?"));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Nah. Let Him stew."));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Right. I'm telling him. Yeah, we've \"been here before.\" But we never went to the cabin. We just... turned around and left, until..."));
                            parser.printDialogueLine(new VoiceDialogueLine("Until?"));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "It's hard to describe. Until the only thing we could see was the same cabin going on forever? And then you told us that the world ended and we died. And then we woke up, and I'm pretty sure you're familiar with all the rest of it."));
                        }
                        
                        parser.printDialogueLine(new VoiceDialogueLine("It seems to me like you saw something you weren't supposed to have seen. If only you'd listened to whatever words of wisdom you were given in that other reality. *Sigh.* But what's done is done, isn't it?"));
                        parser.printDialogueLine(new VoiceDialogueLine("Whatever you saw last time? Unsee it. Whatever thoughts weaseled their way into your head? Unthink them, if it's not already too late. You have a job to do here, and you need to do it *now.*"));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "New plan! Let's see if we can make even more of her."));
                        break;

                    case 3:
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I don't think we're going to be able to put her back."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "It kind of hurts to think about it, doesn't it? It's like everything we say just multiplies her."));
                        parser.printDialogueLine(new VoiceDialogueLine("It certainly looks that way. So please, for the love of everything, stop asking her questions, and stop stalling. You're obviously just making things worse."));
                        break;

                    case 4:
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Okay. This was fun for a bit, but we can't even really interact with her, can we? What's the point of asking questions if all we're going to get is a million answers?"));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I can't even follow what's going on anymore."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "We need to get out of here. This whole place is making me itch."));
                        break;

                    case 5:
                        parser.printDialogueLine(new VoiceDialogueLine("This is reaching its breaking point. If you don't act now, there will be nothing in here but her. Take a deep breath and focus up. You can do this."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "But how do we decide what to do? Can there even be a right choice when all of them are so different?"));
                        parser.printDialogueLine(new VoiceDialogueLine("Stop overthinking it. Your drifting thoughts have clearly been part of the reason this situation has gotten out of hand. If you're trying to do the right thing, there's only ever been the one option, and that option is slaying her."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Just do something! Do anything! Do all of it if that's what you want. This place is *HELL* and it's only getting worse!"));
                        break;
                }
            }

            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "sorry":
                    switch (firstSchism) {
                        case "harsh":
                            parser.printDialogueLine(new PrincessDialogueLine("Yeah. I know. I've been watching you stare at me for a long, long time."));
                            parser.printDialogueLine(new VoiceDialogueLine("The shadows recede, revealing the Princess's face."));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "She's so warm. And friendly..."));
                            parser.printDialogueLine(new VoiceDialogueLine("It's deception. Don't buy into it."));
                            break;

                        case "neutral":
                            parser.printDialogueLine(new PrincessDialogueLine("And yet here you are. How strange. Do you remember anything at all? Do you know why you're here? Do you know me?"));
                            parser.printDialogueLine(new VoiceDialogueLine("The shadows recede, revealing the Princess's face."));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "I don't think she likes us!"));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Wouldn't you be skeptical of someone stumbling in here if you were her? We lost ourselves the second we stepped into this place. I don't know how long she's been here, but I can't imagine it'd be easy for her to trust anyone."));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Those eyes, though... they're so... sharp."));
                            parser.printDialogueLine(new VoiceDialogueLine("It's just deception. Don't buy into it. You can do this."));
                            break;

                        case "gentle":
                            parser.printDialogueLine(new PrincessDialogueLine("That's okay. Sometimes I forget where I am too."));
                            parser.printDialogueLine(new VoiceDialogueLine("The shadows recede, revealing the Princess's face."));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "She's so... blank. I have no idea who she is."));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Isn't that fun? A new puzzle for us to take apart."));
                            parser.printDialogueLine(new VoiceDialogueLine("If she's keeping her cards close to her chest, it's because she wants to deceive you."));
                            break;
                    }

                    // New schism; harsh if you have the blade, gentle if not, neutral if you already have that princess
                    newSchismComment = false;
                    schismCount += 1;
                    if (this.hasBlade && !firstSchism.equals("harsh")) {
                        schismHarsh = true;
                    } else if (!this.hasBlade && !firstSchism.equals("gentle")) {
                        schismGentle = true;
                    } else {
                        schismNeutral = true;
                    }

                    if (schismHarsh) {
                        parser.printDialogueLine(new PrincessDialogueLine("I'm tired of waiting for an answer."));
                    }
                    if (schismNeutral) {
                        parser.printDialogueLine(new PrincessDialogueLine("How strange. So why are you here?"));
                    }
                    if (schismGentle) {
                        parser.printDialogueLine(new PrincessDialogueLine("It's okay. Don't worry. Sometimes I get lost here too."));
                    }

                    break;

                case "more":
                    currentQuestionLines.put("harsh", "And what's that supposed to mean? Are you trying to get under my skin?");
                    currentQuestionLines.put("neutral", "There must be something wrong with you. I'm the same as I was a moment ago.");
                    currentQuestionLines.put("gentle", "Do you need help? Not that there's much I can do chained up like this, but I'm the only one down here, so if you need anything I'll do my best.");
                    currentQuestionLines.put("emo", "I don't feel like I've gotten any bigger.");
                    currentQuestionLines.put("monster", "It must be fear creeping into your heart. You know you can't stop me.");

                    parser.printDialogueLine(new PrincessDialogueLine(currentQuestionLines.get(firstSchism)));
                    if (schismEmo) parser.printDialogueLine(new PrincessDialogueLine(currentQuestionLines.get("emo")));
                    if (schismMonster) parser.printDialogueLine(new PrincessDialogueLine(currentQuestionLines.get("monster")));

                    // Attempt new schism: attempt neutral, then attempt harsh, then attempt gentle; fails if you have all 3 already
                    if (!schismNeutral && !firstSchism.equals("neutral")) {
                        newSchismComment = false;
                        schismThisOption = true;
                        schismNeutral = true;
                        schismCount += 1;
                        
                        parser.printDialogueLine(new PrincessDialogueLine(currentQuestionLines.get("neutral")));
                    } else if (!schismHarsh && !firstSchism.equals("harsh")) {
                        newSchismComment = false;
                        schismThisOption = true;
                        schismHarsh = true;
                        schismCount += 1;
                        
                        parser.printDialogueLine(new PrincessDialogueLine(currentQuestionLines.get("harsh")));
                    } else if (!schismGentle && !firstSchism.equals("gentle")) {
                        newSchismComment = false;
                        schismThisOption = true;
                        schismGentle = true;
                        schismCount += 1;

                        parser.printDialogueLine(new PrincessDialogueLine(currentQuestionLines.get("gentle")));
                    }

                    if (schismThisOption) {
                        parser.printDialogueLine(new VoiceDialogueLine("She fractures again."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I don't like where this is going."));
                        parser.printDialogueLine(new VoiceDialogueLine("Neither do I. Which is why you need to slay her now before things get more complicated than they already are."));
                    } else {
                        if (schismNeutral) parser.printDialogueLine(new PrincessDialogueLine(currentQuestionLines.get("neutral")));
                        if (schismHarsh) parser.printDialogueLine(new PrincessDialogueLine(currentQuestionLines.get("harsh")));
                        if (schismGentle) parser.printDialogueLine(new PrincessDialogueLine(currentQuestionLines.get("gentle")));

                        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Huh. And here I was expecting her to split again. Is that it? What do we do now?"));
                        parser.printDialogueLine(new VoiceDialogueLine("You slay her."));
                    }
                    
                    parser.printDialogueLine(new VoiceDialogueLine("XXXXX"));

                    if (this.hasBlade) {
                        parser.printDialogueLine(new VoiceDialogueLine("You could always start by stabbing her."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "... Which her?"));
                        parser.printDialogueLine(new VoiceDialogueLine("Any of them."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "I don't know about you but I'm sure glad we took that knife with us. I can't believe someone suggested you toss it out the window. Can you imagine?"));
                    } else {
                        parser.printDialogueLine(new VoiceDialogueLine("You could always start by retrieving the blade..."));

                        if (this.threwBlade) {
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "The one that he made us throw out the window?"));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "I wasn't the one who threw it."));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Oh come on, you told us to! Don't try to pass the blame now that it's come back to bite us."));
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Well, if I'd known we'd be dealing with this, maybe I wouldn't have been so hasty with my suggestions."));
                        } else {
                            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Can we even leave this place? I don't like thinking about what might happen to us if we have to go back... *through* those stairs."));
                            parser.printDialogueLine(new VoiceDialogueLine("Well that's where the blade is. If you want it, you'll have to go and get it."));
                        }
                    }

                    break;

                case "name":
                    currentQuestionLines.put("harsh", "You can address me as Your Royal Highness, or Her Majesty. Any honorific should do, really.");
                    currentQuestionLines.put("neutral", "Princess.");
                    currentQuestionLines.put("gentle", "You can call me Princess, if you'd like...");
                    currentQuestionLines.put("emo", "It doesn't matter. I've been down here for so long. What's the point of a name if there's no one around to use it?");
                    currentQuestionLines.put("monster", "I don't need a name. My name is whatever hushed whispers follow in the wake of my devastation.");

                    parser.printDialogueLine(new PrincessDialogueLine(currentQuestionLines.get(firstSchism)));

                    // Attempt new schism: harsh if you have the blade, gentle if not, neutral if you already have that princess; fails if you have all 3 already
                    if (this.hasBlade && !firstSchism.equals("harsh")) {
                        newSchismComment = false;
                        schismCount += 1;
                        schismThisOption = true;
                        schismHarsh = true;
                    } else if (!this.hasBlade && !firstSchism.equals("gentle")) {
                        newSchismComment = false;
                        schismCount += 1;
                        schismThisOption = true;
                        schismGentle = true;
                    } else if (!schismNeutral && !firstSchism.equals("neutral")) {
                        newSchismComment = false;
                        schismCount += 1;
                        schismThisOption = true;
                        schismNeutral = true;
                    }

                    if (schismThisOption) {
                        parser.printDialogueLine(new VoiceDialogueLine("She fractures again."));
                    }

                    if (schismNeutral) parser.printDialogueLine(new PrincessDialogueLine(currentQuestionLines.get("neutral")));
                    if (schismHarsh) parser.printDialogueLine(new PrincessDialogueLine(currentQuestionLines.get("harsh")));
                    if (schismGentle) parser.printDialogueLine(new PrincessDialogueLine(currentQuestionLines.get("gentle")));
                    if (schismEmo) parser.printDialogueLine(new PrincessDialogueLine(currentQuestionLines.get("emo")));
                    if (schismMonster) parser.printDialogueLine(new PrincessDialogueLine(currentQuestionLines.get("monster")));

                    if (schismCount != 2) {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "None of them have names!"));
                        parser.printDialogueLine(new VoiceDialogueLine("How astute. I told you she was untrustworthy."));
                    }

                    break;

                case "weird":
                    currentQuestionLines.put("harsh", "You're not really cut out for this, are you? Why are you even here?");
                    currentQuestionLines.put("neutral", "I don't remember what it was like before I was in this place. Why would I know what happened to you?");
                    currentQuestionLines.put("gentle", "Sometimes I feel like I'm being pulled apart, too. It's so terrifying down here. But at least now you're not alone, and I'm not alone, either.");
                    currentQuestionLines.put("emo", "We're probably stuck down here forever, aren't we? There's no way out, and barely a way in...");
                    currentQuestionLines.put("monster", "I thought they would send something better to deal with me. If the stairs managed to chew you up, I will devour you.");

                    switch (firstSchism) {
                        case "harsh":
                            parser.printDialogueLine(new PrincessDialogueLine("What, like you need me to hold your hand and tell you everything's okay? You're not really cut out for this, are you? Why are you even here?"));
                            break;

                        case "neutral":
                            parser.printDialogueLine(new PrincessDialogueLine(currentQuestionLines.get("neutral")));
                            break;

                        case "gentle":
                            parser.printDialogueLine(new PrincessDialogueLine("I don't know what happened to you, but you look like you're in one piece now. But I understand. Sometimes I feel like I'm being pulled apart, too. It's so terrifying down here. But at least now you're not alone, and I'm not alone, either."));
                            break;
                    }

                    parser.printDialogueLine(new VoiceDialogueLine("She fractures again."));

                    // New schism: attempt emo, then attempt monster, then attempt gentle, then attempt harsh, then neutral
                    newSchismComment = false;
                    schismCount += 1;
                    if (!schismEmo) {
                        setNewSchism = "emo";
                        parser.printDialogueLine(new PrincessDialogueLine(currentQuestionLines.get("emo")));
                    } else if (!schismMonster) {
                        setNewSchism = "monster";
                        parser.printDialogueLine(new PrincessDialogueLine(currentQuestionLines.get("monster")));
                    } else if (!schismGentle && !firstSchism.equals("gentle")) {
                        setNewSchism = "gentle";
                        parser.printDialogueLine(new PrincessDialogueLine(currentQuestionLines.get("gentle")));
                    } else if (!schismHarsh && !firstSchism.equals("harsh")) {
                        setNewSchism = "harsh";
                        parser.printDialogueLine(new PrincessDialogueLine(currentQuestionLines.get("harsh")));
                    } else {
                        setNewSchism = "neutral";
                        parser.printDialogueLine(new PrincessDialogueLine(currentQuestionLines.get("neutral")));
                    }

                    if (schismGentle) parser.printDialogueLine(new PrincessDialogueLine(currentQuestionLines.get("gentle")));
                    if (schismNeutral) parser.printDialogueLine(new PrincessDialogueLine(currentQuestionLines.get("neutral")));
                    if (schismHarsh) parser.printDialogueLine(new PrincessDialogueLine(currentQuestionLines.get("harsh")));
                    if (schismEmo) parser.printDialogueLine(new PrincessDialogueLine(currentQuestionLines.get("emo")));
                    if (schismMonster) parser.printDialogueLine(new PrincessDialogueLine(currentQuestionLines.get("monster")));
                    
                    activeMenu.setCondition("whatDo", activeMenu.hasBeenPicked("reason") && schismMonster);
                    break;

                case "reason":
                    activeMenu.setCondition("threatShare", false);

                    currentQuestionLines.put("harsh", "Maybe it's because I'm dangerous.");
                    if (this.hasBlade) {
                        currentQuestionLines.put("neutral", "Is this a quiz? You're the one who came down here, and with a sharp, sharp knife, too.");
                    } else {
                        currentQuestionLines.put("neutral", "Is this a quiz? If you're here, then surely you know why I'm here.");
                    }
                    currentQuestionLines.put("gentle", "I don't know why I'm here, but there has to be a reason, right? You don't just lock a Princess away in a place like this without a reason. I wish I knew what it was.");
                    currentQuestionLines.put("emo", "But you know, right? You have to know. You're the only other person I've ever seen, or at least the only one I can remember. Don't give me false hope. Please just end this already. One way or another, just do it.");
                    currentQuestionLines.put("monster", "Don't be coy. We both know why I'm locked away here. I'm a monster, and the second I get out of this place, I'm going to end the entire world.");

                    parser.printDialogueLine(new PrincessDialogueLine(currentQuestionLines.get(firstSchism)));

                    // New schism: attempt emo, then attempt monster
                    if (!schismEmo) {
                        newSchismComment = false;
                        schismCount += 1;
                        schismThisOption = true;
                        schismEmo = true;
                    } else if (!schismMonster) {
                        newSchismComment = false;
                        schismCount += 1;
                        schismThisOption = true;
                        schismMonster = true;
                    }

                    if (schismThisOption) {
                        parser.printDialogueLine(new VoiceDialogueLine("She fractures again."));
                    }
                    
                    parser.printDialogueLine(new PrincessDialogueLine(currentQuestionLines.get("emo")));
                    if (schismMonster) parser.printDialogueLine(new PrincessDialogueLine(currentQuestionLines.get("monster")));
                    
                    activeMenu.setCondition("whatDo", schismMonster);
                    break;

                case "threatShare":
                    activeMenu.setCondition("reason", false);
                    activeMenu.setCondition("whatDo", true);

                    currentQuestionLines.put("harsh", "And you believe that? Do you think I'm some sort of... monster?");
                    currentQuestionLines.put("neutral", "I don't have any weapons. And I'm chained to a wall. Do I look like someone that could end the world? Do I look like a monster?");
                    currentQuestionLines.put("gentle", "But I don't want to hurt anyone. I like the world! I think. You... you don't think I'm some sort of monster, do you?");
                    currentQuestionLines.put("emo", "I don't know. Maybe that's true. I probably shouldn't be given the chance anyway. If you were sent here to kill me, maybe you should just get it over with.");
                    currentQuestionLines.put("monster", "Because I am. Everything you've heard about me is true, and I am going to lay waste to everything. Starting with you.");

                    parser.printDialogueLine(new PrincessDialogueLine(currentQuestionLines.get(firstSchism)));

                    // New schism: attempt monster, then attempt emo
                    if (!schismMonster) {
                        newSchismComment = false;
                        schismCount += 1;
                        schismThisOption = true;
                        schismMonster = true;
                    } else if (!schismEmo) {
                        newSchismComment = false;
                        schismCount += 1;
                        schismThisOption = true;
                        schismEmo = true;
                    }

                    if (schismThisOption) {
                        parser.printDialogueLine(new VoiceDialogueLine("She fractures again."));
                    }
                    
                    parser.printDialogueLine(new PrincessDialogueLine(currentQuestionLines.get("monster")));
                    if (schismEmo) parser.printDialogueLine(new PrincessDialogueLine(currentQuestionLines.get("emo")));
                    
                    break;

                case "whatDo":
                    currentQuestionLines.put("harsh", "What do you want me to say? That I'd be a good person?");
                    currentQuestionLines.put("neutral", "I could tell you that I'd lead a quiet life in the woods or that I'd open an orphanage or that I'd do any other number of \"good\" things that I'm sure you think you want to hear.");
                    currentQuestionLines.put("gentle", "I just want to live my life.");
                    currentQuestionLines.put("emo", "If you want to put an end to me, then put an end to me.");
                    currentQuestionLines.put("monster", "Besides, you already know what I'm going to do.");

                    switch (firstSchism) {
                        case "harsh":
                            parser.printDialogueLine(new PrincessDialogueLine("I don't think what I'd do really matters, does it?"));
                            break;

                        case "neutral":
                            parser.printDialogueLine(new PrincessDialogueLine("I don't think I can answer that question in a way you'd find meaningful."));
                            break;

                        case "gentle":
                            parser.printDialogueLine(new PrincessDialogueLine("Are you looking for the truth, or are you looking for the 'right' answer?"));
                            break;
                    }

                    // New schism attempt: attempt harsh, then attempt neutral, then attempt gentle
                    if (!schismHarsh && !firstSchism.equals("harsh")) {
                        newSchismComment = false;
                        schismCount += 1;
                        schismThisOption = true;
                        setNewSchism = "harsh";
                        parser.printDialogueLine(new VoiceDialogueLine("She fractures again."));
                        parser.printDialogueLine(new PrincessDialogueLine(currentQuestionLines.get("harsh")));
                    } else if (!schismNeutral && !firstSchism.equals("neutral")) {
                        newSchismComment = false;
                        schismCount += 1;
                        schismThisOption = true;
                        setNewSchism = "neutral";
                        parser.printDialogueLine(new VoiceDialogueLine("She fractures again."));
                        parser.printDialogueLine(new PrincessDialogueLine(currentQuestionLines.get("neutral")));
                    } else if (!schismGentle && !firstSchism.equals("gentle")) {
                        newSchismComment = false;
                        schismCount += 1;
                        schismThisOption = true;
                        setNewSchism = "gentle";
                        parser.printDialogueLine(new VoiceDialogueLine("She fractures again."));
                        parser.printDialogueLine(new PrincessDialogueLine(currentQuestionLines.get("gentle")));
                    }

                    if (!schismThisOption) {
                        if (schismNeutral) {
                            parser.printDialogueLine(new PrincessDialogueLine(currentQuestionLines.get("neutral")));
                        }
                        if (schismHarsh) {
                            parser.printDialogueLine(new PrincessDialogueLine(currentQuestionLines.get("harsh")));
                        }
                        if (schismGentle) {
                            parser.printDialogueLine(new PrincessDialogueLine(currentQuestionLines.get("gentle")));
                        }
                    }

                    if (schismGentle) {
                        parser.printDialogueLine(new PrincessDialogueLine("You either trust me or you believe that I'm dangerous. What I say won't change how you already feel about me."));
                    }
                    if (schismNeutral) {
                        parser.printDialogueLine(new PrincessDialogueLine("I'm a prisoner here, and whether or not you shoved me down here, you're practically my captor at this point. Anything I'd say is tainted by that."));
                    }
                    if (schismHarsh) {
                        parser.printDialogueLine(new PrincessDialogueLine("I'm not going to dance for you."));
                    }
                    if (schismMonster) parser.printDialogueLine(new PrincessDialogueLine(currentQuestionLines.get("monster")));
                    if (schismEmo) parser.printDialogueLine(new PrincessDialogueLine(currentQuestionLines.get("emo")));
                    
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Not a single real answer..."));

                    if (schismMonster) {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "At least aside from Miss Blood-and-Destruction. It's infuriating, isn't it?"));
                    } else {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "It's *infuriating,* isn't it?"));
                    }

                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Whose buttons are there for us to press? Whose skin is there for us to get under?"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Not exactly how I'd put it, but I don't disagree. There must be something we can do. Asking questions just seems to make things worse."));
                    break;

                case "cGoStairs":
                case "cSlayPrincess":
                    if (schismCount == 1) {
                        super.giveDefaultFailResponse();
                        break;
                    }
                case "ending":
                    this.repeatActiveMenu = false;
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Chapter ends here
        parser.printDialogueLine(new VoiceDialogueLine("Wait... that's not right."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Go on."));
        parser.printDialogueLine(new VoiceDialogueLine("You take a step forward. Your foot lands. But it lands... different. You experience a firm footfall, a gentle tread, a confident stride."));
        parser.printDialogueLine(new VoiceDialogueLine("You can feel yourself rupture. The room spins, your perception multiplying in a sickening kaleidoscope as your very self is pulled in incomprehensibly many directions."));

        if (this.hasBlade) {
            parser.printDialogueLine(new VoiceDialogueLine("All at once you charge forward, knife gleaming to slay the Princess, just as you strike at her bindings and leave her to languish alone."));
        } else {
            parser.printDialogueLine(new VoiceDialogueLine("You find the blade suddenly in your hands. All at once you use it to strike at her bindings as you remain upstairs and slay her and leave her to languish alone."));
        }
        
        parser.printDialogueLine(new VoiceDialogueLine("Is this what the end of the world looks like? What an unbearable mess..."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "But this... w-we can't --"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "... Do you not have anything *witty* to say? I could use a good bit of wit right now."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "No, I don't! Because this isn't fun! How are we supposed to have fun if everything is happening at the same time? It's the same as nothing happening, and nothing is *excrutiating!*"));
        parser.printDialogueLine(new VoiceDialogueLine("Luckily for all of us, nothing, and everything, doesn't go on forever. The world, and the Princess, collapse in on themselves before it all --"));

        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "...Falls apart?"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "I think He's gone."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "We were never going to salvage this, were we?"));
        parser.printDialogueLine("The Princess in front of you is all wrong, multiple faces and bodies smashed together in ways you can't begin to make sense of. Their skin stretches and contorts, veins bulging. They have too many fingers on too few hands. The pristine blade still sticks out of one of their chests, the face above it staring blankly into the distance.");

        if (this.isFirstVessel) {
            parser.printDialogueLine("A textured nothingness begins to creep into the edges of your vision.");
        } else {
            parser.printDialogueLine("A textured nothingness begins to creep into the edges of your vision. Somehow, it feels familiar.");
        }
        
        parser.printDialogueLine(new PrincessDialogueLine("What happened to us? What are we? There are parts of us that are dead, and the others.... they just don't fit."));
        parser.printDialogueLine(new PrincessDialogueLine("We can feel them moving around in spaces they don't belong. It's all so uncomfortable."));
        parser.printDialogueLine(new PrincessDialogueLine("Did you do this? Did we do this? Can... can you pull us back apart? Can you fix us?"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "We should help her. I think... we did this."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "How... surprisingly sincere."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "I didn't actually think our actions had consequences."));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "It's a little late for regret, isn't it?"));
        parser.printDialogueLine(new PrincessDialogueLine("Please?"));

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "okay", "\"It's going to be okay...\""));
        activeMenu.add(new Option(this.manager, "best", "\"I'll do my best.\""));
        activeMenu.add(new Option(this.manager, "supposed", "\"I don't think you're supposed to be fixed.\""));
        activeMenu.add(new Option(this.manager, "no", "\"No.\""));
        activeMenu.add(new Option(this.manager, "destroyed", "\"You just destroyed everything. I'm not going to fix you.\""));
        activeMenu.add(new Option(this.manager, "silent", "[Say nothing.]"));

        if (parser.promptOptionsMenu(activeMenu).equals("silent")) {
            parser.printDialogueLine("Something reaches out and folds her into its myriad arms.");
            if (this.isFirstVessel) {
                parser.printDialogueLine("But you don't know if she had the chance to hear your silence. She's gone, replaced with something else.");
            } else {
                parser.printDialogueLine("But you'll never know if she hears your silence. She's gone. Memory returns.");
            }
        } else {
            parser.printDialogueLine("Something reaches out and folds her into its myriad arms.");
            if (this.isFirstVessel) {
                parser.printDialogueLine("But you don't know if she had the chance to hear your reply. She's gone, replaced with something else.");
            } else {
                parser.printDialogueLine("But you'll never know if she hears your reply. She's gone. Memory returns.");
            }
        }
            
        return ChapterEnding.ILLUSIONOFCHOICE;
    }


    // - Chapter II: The Prisoner -

    /**
     * Runs Chapter II: The Prisoner
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding prisoner() {
        // You gain the Voice of the Skeptic

        if (!this.chapter2Intro(true, false, true)) {
            return ChapterEnding.ABORTED;
        }

        manager.setNowPlaying("The Prisoner");
        
        parser.printDialogueLine(new VoiceDialogueLine("The interior of the cabin is less a cozy woodland retreat and more like a dungeon. A few pathetic wisps of starlight attempt to illuminate the cold, uninviting stone walls, and thick wrought-iron bars barricade the windows, reminding anyone who enters that this is a prison."));
        parser.printDialogueLine(new VoiceDialogueLine("The only furniture of note is an iron table, bolted to the floor, a pristine blade perched on its edge."));
        parser.printDialogueLine(new VoiceDialogueLine("The blade is your implement. You'll need it if you want to do this right."));

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "mirror", "(Explore) You didn't say anything about the mirror on the wall."));
        activeMenu.add(new Option(this.manager, "different", "(Explore) This whole cabin is different than last time.", this.sharedLoopInsist));
        activeMenu.add(new Option(this.manager, "approach", "(Explore) [Approach the mirror.]"));
        activeMenu.add(new Option(this.manager, "take", "(Explore) [Take the blade.]"));
        activeMenu.add(new Option(this.manager, "enter", "[Enter the basement.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "mirror":
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "He definitely did not. Does a mirror not count as \"furniture of note\" to you? Because it should."));
                    parser.printDialogueLine(new VoiceDialogueLine("There isn't a mirror. There's a table, the blade sitting on the table, and the door to the basement. There's nothing else in here."));
                    if (this.ch2AskMirror()) {
                        activeMenu.setCondition("approach", false);
                    }
                    break;

                case "different":
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "*Very* different."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "Yes, but *why?* Did He change it, or did it change all on its own? Maybe it's a different cabin entirely."));
                    parser.printDialogueLine(new VoiceDialogueLine("Now isn't that a novel thought! Maybe you *haven't* actually been here before. I hope this means you'll finally drop that ridiculous past-life nonsense. You haven't died, and you certainly haven't been killed by the Princess."));
                    parser.printDialogueLine(new VoiceDialogueLine("So focus up. Don't get distracted by minor details."));
                    break;

                case "cApproachMirror":
                    activeMenu.setCondition("approach", false);
                case "approach":
                    activeMenu.setCondition("mirror", false);
                    this.ch2ApproachMirror();
                    break;

                case "cTake":
                    activeMenu.setCondition("take", false);
                case "take":
                    this.hasBlade = true;
                    this.withBlade = false;
                    parser.printDialogueLine(new VoiceDialogueLine("You take the blade from the table. It would be difficult to slay the Princess and save the world without a weapon."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "Good idea. Much better to be armed than to go in with blind hope alone."));
                    break;

                case "cGoStairs":
                case "enter":
                    this.repeatActiveMenu = false;
                    break;

                default:
                    this.giveDefaultFailResponse(activeOutcome);
            }
        }

        if (!this.hasBlade) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "I'm afraid I'm going to insist we take the blade. We're in a dangerous situation, and I'm not letting us go down there without a weapon."));
            
            if (this.sharedLoopInsist) {
                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Are you sure? She killed us with it last time. What if she turns it against us *again?*"));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "Yes. I'm sure. And I've already got a plan for that."));
                parser.printDialogueLine(new VoiceDialogueLine("Still with those past-life delusions, are we? I hope part of that plan is \"don't give the world-ending monstrosity your only weapon.\" Because unless you've decided to arm the Princess, I don't think you need to worry about her having a weapon."));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "Peachy. We'll be fine."));
            } else {
                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Are you sure? What if she, I don't know... turns it against us? ... Which I'm bringing up in a purely hypothetical manner."));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "Yes. I'm sure."));
                parser.printDialogueLine(new VoiceDialogueLine("Turns it against you? She's a prisoner here. And she'll only be able to turn it against you if you give it to her. Which you won't be doing, because she's an existential threat to the entire world."));
            }
            
            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Okay. I'm trusting you."));

            this.activeMenu = new OptionsMenu();
            activeMenu.add(new Option(this.manager, "hey", "Hey! Don't I get a say here? What's the big idea?"));
            activeMenu.add(new Option(this.manager, "take", "[Take the blade.]"));

            this.repeatActiveMenu = true;
            while (repeatActiveMenu) {
                switch (parser.promptOptionsMenu(activeMenu)) {
                    case "hey":
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "Normally, yeah. But not about this. Call it a reflex. We take the knife as we go."));
                        parser.printDialogueLine(new VoiceDialogueLine("Wonderful. You do exactly that, sweeping the blade from the table before proceeding to the basement."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Don't worry about it. We have a knife, so what? It's not like we have to use it."));
                        parser.printDialogueLine(new VoiceDialogueLine("No, you don't have to do anything. But you'd do well to use it regardless. *Sigh.* Moving on."));
                        break;

                    case "cTake":
                    case "take":
                        parser.printDialogueLine(new VoiceDialogueLine("You take the blade from the table. It would be difficult to slay the Princess and save the world without a weapon."));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "Thanks. I mean it."));
                        break;

                    default: this.giveDefaultFailResponse();
                }
            }

            this.hasBlade = true;
            System.out.println();
        }

        this.withBlade = false;
        this.mirrorPresent = false;
        parser.printDialogueLine(new VoiceDialogueLine("The door to the basement creaks open, revealing an old stone staircase. A few sputtering torches attempt to vaguely illuminate your path, dancing across glimmering patches of slimy moss on the stone steps. If the Princess lives here, slaying her would probably be doing her a favor."));
        parser.printDialogueLine(new VoiceDialogueLine("Her voice, harsh but controlled, carries up the stairs."));
        parser.printDialogueLine(new PrincessDialogueLine("Is that a visitor I hear? Please, come downstairs. It's been a while since I've had company."));

        if (this.sharedLoop) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "Does she remember us?"));
        } else {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "I wonder what visitors she could be referring to. Are we not the first?"));
        }

        this.currentLocation = GameLocation.BASEMENT;
        this.withPrincess = true;
        parser.printDialogueLine(new VoiceDialogueLine("You walk down the stairs and lock eyes with the Princess. She looks up at you, the heavy collar around her neck clanking loudly as she moves, the chains binding both her wrists to the far wall joining the metallic chorus as she adjusts her hands in her lap."));

        if (this.sharedLoopInsist) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "So much for cutting her out of here..."));
            parser.printDialogueLine(new VoiceDialogueLine("Do you hear yourself right now? \"Cutting her out of here\" never should have been on the table."));
        } else {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Should we be worried about the one around her neck?"));
            parser.printDialogueLine(new VoiceDialogueLine("Why would you be worried about her restraints. If anything, they'll make your job easier."));
        }

        parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "Have you noticed the empty chain on the wall? Odd that in a place where everything seems to serve a distinct purpose, there would be something so obviously useless."));

        if (this.sharedLoopInsist) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "That was there last time too, wasn't it?"));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "It was."));
        }

        parser.printDialogueLine(new PrincessDialogueLine("What an interesting development. Why don't you have a seat? The two of us should chat before you bury that thing in my heart."));


        
        
        // temporary templates for copy-and-pasting
        /*
        parser.printDialogueLine(new VoiceDialogueLine("XXXXX"));
        parser.printDialogueLine(new PrincessDialogueLine("XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "(Explore) XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "\"XXXXX\""));
        */
        
        // PLACEHOLDER
        return null;
    }


    // - Chapter III: The Cage -

    /**
     * Runs Chapter III: The Cage
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
     * Runs Chapter III: The Grey (coming from the Prisoner)
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding greyDrowned() {
        /*
          You gain the Voice of the Cold
          Possible combinations:
            - Smitten + Cold
         */

        
            
        return ChapterEnding.ANDALLTHISLONGING;
    }

    /**
     * Runs Chapter III: The Grey (coming from the Damsel)
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding greyBurned() {
        /*
          You gain the Voice of the Cold
          Possible combinations:
            - Skeptic + Cold
         */



        return ChapterEnding.BURNINGDOWNTHEHOUSE;
    }


    // - Chapter II: The Damsel -

    /**
     * Runs Chapter II: The Damsel
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding damsel() {
        // You gain the Voice of the Smitten

        if (!this.chapter2Intro(true, false, true)) {
            return ChapterEnding.ABORTED;
        }

        manager.setNowPlaying("The Damsel");
        
        parser.printDialogueLine(new VoiceDialogueLine("The interior of the cabin is clean and elegant, its stone walls draped in fine-threaded tapestries, a prison befitting a royal prisoner. The only furniture of note is an ornate wooden table with a pristine blade perched on its edge."));
        parser.printDialogueLine(new VoiceDialogueLine("The blade is your implement. You'll need it if you want to do this right."));

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "mirror", "(Explore) You didn't say anything about the mirror on the wall."));
        activeMenu.add(new Option(this.manager, "different", "(Explore) This whole cabin is different than last time.", this.sharedLoopInsist));
        activeMenu.add(new Option(this.manager, "approach", "(Explore) [Approach the mirror.]"));
        activeMenu.add(new Option(this.manager, "take", "(Explore) [Take the blade.]"));
        activeMenu.add(new Option(this.manager, "enter", "[Enter the basement.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "mirror":
                    parser.printDialogueLine(new VoiceDialogueLine("That's because there isn't a mirror. There's a table, the blade sitting on the table, and the door to the basement. There's nothing else in here."));
                    if (this.ch2AskMirror()) {
                        activeMenu.setCondition("approach", false);
                    }
                    break;

                case "different":
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "*Very* different."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "Is it? I can't say I was paying much attention to the scenery last time around."));
                    parser.printDialogueLine(new VoiceDialogueLine("Maybe that's because you haven't actually been here. I hope this means you'll finally drop that ridiculous past-life nonsense. You haven't died, and you certainly haven't been killed by the Princess."));
                    parser.printDialogueLine(new VoiceDialogueLine("So focus up. Stop letting yourself get distracted."));
                    break;

                case "cApproachMirror":
                    activeMenu.setCondition("approach", false);
                case "approach":
                    activeMenu.setCondition("mirror", false);
                    this.ch2ApproachMirror();
                    break;

                case "cTake":
                    activeMenu.setCondition("take", false);
                case "take":
                    this.hasBlade = true;
                    this.withBlade = false;
                    parser.printDialogueLine(new VoiceDialogueLine("You take the blade from the table. It would be difficult to slay the Princess and save the world without a weapon."));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "I suppose if we're to play the role of dashing knight we need an equally dashing sword. That way she'll know we can defend her from her enemies."));

                    if (this.sharedLoop) {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Hopefully it doesn't put her on edge. And hopefully it doesn't get turned on us... again."));
                    } else {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Hopefully it doesn't put her on edge."));
                    }

                    parser.printDialogueLine(new VoiceDialogueLine("There's no use arguing over *motivations* right now. It's good that you took the blade. You'll need it to do your job."));
                    break;

                case "cGoStairs":
                case "enter":
                    this.repeatActiveMenu = false;
                    break;

                default:
                    this.giveDefaultFailResponse(activeOutcome);
            }
        }

        this.withBlade = false;
        this.mirrorPresent = false;
        parser.printDialogueLine(new VoiceDialogueLine("The door to the basement creaks open, revealing an intricate stairwell. Gold-trimmed carpet glimmers in the light of the torches positioned along the walls. The basement almost seems welcoming in the dim firelight."));
        parser.printDialogueLine(new VoiceDialogueLine("But it's still a stone basement. If the Princess lives here, slaying her is probably doing her a favor."));
        parser.printDialogueLine(new VoiceDialogueLine("A soft voice carries up the stairs."));
        parser.printDialogueLine(new PrincessDialogueLine("H-hello? Is someone there?"));

        if (this.sharedLoop) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "Her voice... it's somehow even more beautiful than last time. I can hear wedding bells already..."));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I've held my tongue 'til now, but you're taking this a little too far. We barely even know the Princess. We can still do right by her without all this over-the-top fawning."));
        } else {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "Her voice... it's somehow even more beautiful than last time. I think we're in love."));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Okay, I'm with you that we should be doing whatever we can to save her, but saying we're in love is a bit much, don't you think?"));
            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "We don't even know the Princess. We can still do right by her without all this... fawning."));
        }
        
        parser.printDialogueLine(new VoiceDialogueLine("Yes. For everyone's sake, you're not in love. *Sigh.* Just remember that her charms are all part of the manipulation."));

        this.currentLocation = GameLocation.BASEMENT;
        this.withPrincess = true;
        System.out.println();
        parser.printDialogueLine(new VoiceDialogueLine("You walk down the stairs and lock eyes with the Princess. There's a heavy chain around her wrist, binding her to the far wall."));
        parser.printDialogueLine("Her dress is more elaborate than last time, with several layers of ruffles at the bottom, and she wears a more traditional crown in place of her tiara.");
        parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "My love! We're here to rescue you from your unjust and foul imprisonment!"));
        parser.printDialogueLine(new VoiceDialogueLine("You know she can't hear you, right?"));
        parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "She may not be able to hear my words, but surely, she can hear my spirit."));
        parser.printDialogueLine(new VoiceDialogueLine("Oh, your spirit's plenty loud, alright."));


        
        
        // temporary templates for copy-and-pasting
        /*
        parser.printDialogueLine(new VoiceDialogueLine("XXXXX"));
        parser.printDialogueLine(new PrincessDialogueLine("XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "(Explore) XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "\"XXXXX\""));
        */
        
        // PLACEHOLDER
        return null;
    }


    // - Epilogue: Happily Ever After -

    /**
     * Runs Chapter III: Happily Ever After
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
        this.currentLocation = GameLocation.BEFOREMIRROR;
        this.mirrorPresent = true;

        this.currentVoices.put(Voice.NARRATOR, false);

        switch (this.prevEnding) {
            case HINTOFFEELING:
            case LEAVEHERBEHIND:
            case NEWLEAFWEATHEREDBOOK:
                if (this.hasVoice(Voice.COLD)) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "So that's a wrap. I didn't think we'd ever get here. There's only one thing left to do."));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "So the world's gone, and she's gone with it. There's only one thing left for you to do here. You need to look at yourself. Whatever you see, it's going to be okay."));
                }

                this.activeMenu = new OptionsMenu();
                activeMenu.add(new Option(this.manager, "approach", "[Approach the mirror.]"));

                this.repeatActiveMenu = true;
                while (repeatActiveMenu) {
                    switch (parser.promptOptionsMenu(activeMenu)) {
                        case "cApproachMirror":
                        case "approach":
                            this.repeatActiveMenu = false;
                            break;

                        default: this.giveDefaultFailResponse();
                    }
                }

                parser.printDialogueLine("The voice falls silent as you approach.");
                break;

            case MOMENTOFCLARITY:
                this.activeMenu = new OptionsMenu();
                activeMenu.add(new Option(this.manager, "approach", "[Approach the mirror.]"));

                this.repeatActiveMenu = true;
                while (repeatActiveMenu) {
                    switch (parser.promptOptionsMenu(activeMenu)) {
                        case "cApproachMirror":
                        case "approach":
                            this.repeatActiveMenu = false;
                            break;

                        default: this.giveDefaultFailResponse();
                    }
                }

                if (this.isFirstVessel) {
                    parser.printDialogueLine("You step towards the mirror. It holds a truth that you must witness.");
                } else {
                    parser.printDialogueLine("You step towards the mirror. Its secrets remain hidden. Its mysteries remain unresolved.");
                }

                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Something tells me that this is the end of the line, but I don't feel bad about it. I'm ready."));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "It feels okay."));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "The fear's gone."));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "I'm done fighting."));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "My heart feels quiet."));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "The game was always going to end."));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "I'll be free of all of you."));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "I'm ready for the truth."));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "I'm ready to sleep."));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "I'm just ready to be anywhere that isn't here."));
                parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "Boys, it's been an honor."));
                
                break;

            case MUTUALLYASSURED:
            case EMPTYCUP:
                this.activeMenu = new OptionsMenu();
                activeMenu.add(new Option(this.manager, "approach", "[Approach the mirror.]"));

                this.repeatActiveMenu = true;
                while (repeatActiveMenu) {
                    switch (parser.promptOptionsMenu(activeMenu)) {
                        case "cApproachMirror":
                        case "approach":
                            this.repeatActiveMenu = false;
                            break;

                        default: this.giveDefaultFailResponse();
                    }
                }

                break;

            default:
                if (this.prevEnding != ChapterEnding.GRACE) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "She's gone. Where did she go? Should we try and find her?"));
                }

                if (this.mirrorComment || this.touchedMirror) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "And there's that mirror again. Why is it here? Why now?!"));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "And is that a... mirror? Why is it here? Why now?!"));
                }

                boolean explore = false;
                boolean silence = false;
                if (this.isFirstVessel) {
                    this.activeMenu = new OptionsMenu();
                    activeMenu.add(new Option(this.manager, "where", "(Explore) I don't know where she went, and I don't know how we'd even go about looking for her."));
                    activeMenu.add(new Option(this.manager, "gone", "(Explore) The Narrator is gone..."));
                    activeMenu.add(new Option(this.manager, "suggest", "(Explore) I think I'm supposed to look at the mirror."));
                    activeMenu.add(new Option(this.manager, "approach", "[Approach the mirror.]"));

                    boolean contraAsk = false;
                    this.repeatActiveMenu = true;
                    while (repeatActiveMenu) {
                        this.activeOutcome = parser.promptOptionsMenu(activeMenu);
                        switch (activeOutcome) {
                            case "where":
                                if (this.hasVoice(Voice.SKEPTIC)) {
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "If there's even a her to find anymore."));
                                }
                                if (this.hasVoice(Voice.OPPORTUNIST)) {
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "Does this mean we won?"));
                                }

                                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "You're right. She's gone. It's just us and that... awful thing."));
                                
                                if (this.hasVoice(Voice.STUBBORN)) {
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "It's like it's mocking us."));
                                }
                                if (this.hasVoice(Voice.HUNTED)) {
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "Let's just stay still."));
                                }
                                if (this.hasVoice(Voice.SMITTEN)) {
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "I can't believe she was taken away from us! The nerve."));
                                }
                                if (this.hasVoice(Voice.PARANOID)) {
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "I should feel better with her gone, but I don't."));
                                }
                                if (this.hasVoice(Voice.CHEATED)) {
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "We just can't win."));
                                }
                                if (this.hasVoice(Voice.COLD)) {
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "Don't bother looking for her. I'm sure it's just a waste of time."));
                                }
                                if (this.hasVoice(Voice.BROKEN)) {
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "I feel anxious. Does anyone else feel anxious?"));
                                }
                                if (this.hasVoice(Voice.CONTRARIAN)) {
                                    if (contraAsk) {
                                        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Again, what the hell are we supposed to do?"));
                                    } else {
                                        contraAsk = true;
                                        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Then what the hell are we supposed to do?"));
                                    }
                                }

                                break;

                            case "gone":
                                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "He is. Does that mean the world ended?"));

                                if (this.hasVoice(Voice.SKEPTIC)) {
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "It must have. Do any of us know what the world ending is supposed to look like?"));
                                }
                                if (this.hasVoice(Voice.HUNTED)) {
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "It hasn't ended. We're still here."));
                                }
                                if (this.hasVoice(Voice.COLD)) {
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "He was never going to outlast us."));
                                }
                                if (this.hasVoice(Voice.CONTRARIAN)) {
                                    if (contraAsk) {
                                        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Again, what the hell are we supposed to do?"));
                                    } else {
                                        contraAsk = true;
                                        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Then what the hell are we supposed to do?"));
                                    }
                                }
                                if (this.hasVoice(Voice.PARANOID)) {
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "Yeah. No voice needling us anymore. Feels good, but I also feel... itchy. Cold."));
                                }
                                if (this.hasVoice(Voice.STUBBORN)) {
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "The world didn't end. We're still here. Come on, we just need to keep going!"));
                                }
                                if (this.hasVoice(Voice.BROKEN)) {
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "Figures the world would end and leave us with all this nothing."));
                                }
                                if (this.hasVoice(Voice.OPPORTUNIST)) {
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "We're at the top of the pecking order now... right, boys?"));
                                }
                                if (this.hasVoice(Voice.SMITTEN)) {
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "A villain vanquished."));
                                }
                                if (this.hasVoice(Voice.CHEATED)) {
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "Good riddance."));
                                }

                                break;

                            case "suggest":
                                explore = true;

                                if (this.hasVoice(Voice.CONTRARIAN)) {
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "No. Don't do that."));
                                }
                                if (this.hasVoice(Voice.HUNTED)) {
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "That thing reeks of death."));
                                }
                                if (this.hasVoice(Voice.PARANOID)) {
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "It's calling us. And not in a good way."));
                                }
                                if (this.hasVoice(Voice.SKEPTIC)) {
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "You're right. Part of me wants the truth, but something stronger is holding me back. Fear."));
                                }
                                if (this.hasVoice(Voice.STUBBORN)) {
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "Screw the mirror! We just need to find the Princess."));
                                }
                                if (this.hasVoice(Voice.BROKEN)) {
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "I don't want to look at us."));
                                }
                                if (this.hasVoice(Voice.SMITTEN)) {
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "Yes, I fear that we won't like what we'll see. What if we just sit here and preen for a while? That can't hurt, right?"));
                                }
                                if (this.hasVoice(Voice.CHEATED)) {
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "It's going to do something to us. I can feel it."));
                                }
                                if (this.hasVoice(Voice.OPPORTUNIST)) {
                                    if (this.nVoices() == 2) {
                                        parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "If he thinks it's bad, I'm with him."));
                                    } else {
                                        parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "If they think it's bad, I'm with them."));
                                    }
                                }
                                if (this.hasVoice(Voice.COLD)) {
                                    if (this.nVoices() == 2) {
                                        parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "Ignore him. You have to look."));
                                    } else {
                                        parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "Ignore the cowards. You have to look."));
                                    }
                                }

                                break;

                            case "cApproachMirror":
                            case "approach":
                                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I'm begging you, don't do this."));

                                this.activeMenu = new OptionsMenu();
                                activeMenu.add(new Option(this.manager, "explore", "(Explore) \"The mirror never scared you before.\"", this.mirrorComment || this.touchedMirror));
                                activeMenu.add(new Option(this.manager, "ignore", "[Ignore him.]"));
                                
                                while (repeatActiveMenu) {
                                    switch (parser.promptOptionsMenu(activeMenu)) {
                                        case "explore":
                                            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "It's different now! It feels... I don't know. Final."));

                                            if (!explore) {
                                                explore = true;

                                                if (this.hasVoice(Voice.CONTRARIAN)) {
                                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Yeah, don't look at it. I don't like that *thing.*"));
                                                }
                                                if (this.hasVoice(Voice.HUNTED)) {
                                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "That thing reeks of death."));
                                                }
                                                if (this.hasVoice(Voice.PARANOID)) {
                                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "It's calling us. And not in a good way."));
                                                }
                                                if (this.hasVoice(Voice.SKEPTIC)) {
                                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "You're right. Part of me wants the truth, but something stronger is holding me back. Fear."));
                                                }
                                                if (this.hasVoice(Voice.STUBBORN)) {
                                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "Screw the mirror! We just need to find the Princess."));
                                                }
                                                if (this.hasVoice(Voice.BROKEN)) {
                                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "I don't want to look at us."));
                                                }
                                                if (this.hasVoice(Voice.SMITTEN)) {
                                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "Yes, I fear that we won't like what we'll see. What if we just sit here and preen for a while? That can't hurt, right?"));
                                                }
                                                if (this.hasVoice(Voice.CHEATED)) {
                                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "It's going to do something to us. I can feel it."));
                                                }
                                                if (this.hasVoice(Voice.OPPORTUNIST)) {
                                                    if (this.nVoices() == 2) {
                                                        parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "If he thinks it's bad, I'm with him."));
                                                    } else {
                                                        parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "If they think it's bad, I'm with them."));
                                                    }
                                                }
                                                if (this.hasVoice(Voice.COLD)) {
                                                    if (this.nVoices() == 2) {
                                                        parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "Ignore him. You have to look."));
                                                    } else {
                                                        parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "Ignore the cowards. You have to look."));
                                                    }
                                                }
                                            }

                                            break;

                                        case "cApproachMirror":
                                        case "ignore":
                                            this.repeatActiveMenu = false;
                                            break;

                                        default: this.giveDefaultFailResponse(activeOutcome);
                                    }
                                }

                                break;

                            default: this.giveDefaultFailResponse(activeOutcome);
                        }
                    }
                } else {
                    this.activeMenu = new OptionsMenu();
                    activeMenu.add(new Option(this.manager, "cruel", "(Explore) Of course you're scared. This is the end, for you. But it's not the end for me."));
                    activeMenu.add(new Option(this.manager, "comfortA", "(Explore) It's going to be okay. Just trust me.", !manager.getMirrorScaredFlag()));
                    activeMenu.add(new Option(this.manager, "comfortB", "(Explore) It's going to be okay. Just trust me. We've been here before, and you always get scared.", manager.getMirrorScaredFlag()));
                    activeMenu.add(new Option(this.manager, "approach", "[Approach the mirror.]"));

                    this.repeatActiveMenu = true;
                    while (repeatActiveMenu) {
                        this.activeOutcome = parser.promptOptionsMenu(activeMenu);
                        switch (activeOutcome) {
                            case "cruel":
                                activeMenu.setCondition("comfort", false);
                                explore = true;
                                this.mirrorCruel();
                                break;

                            case "comfortA":
                            case "comfortB":
                                activeMenu.setCondition("cruel", false);
                                explore = true;
                                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "But it feels so bad! Like looking into it right now is going to be the end of everything."));

                                if (this.hasVoice(Voice.CONTRARIAN)) {
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Yeah, don't look at it. I don't like that *thing.*"));
                                }
                                if (this.hasVoice(Voice.HUNTED)) {
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "That thing reeks of death."));
                                }
                                if (this.hasVoice(Voice.PARANOID)) {
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "It's calling us. And not in a good way."));
                                }
                                if (this.hasVoice(Voice.SKEPTIC)) {
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "You're right. Part of me wants the truth, but something stronger is holding me back. Fear."));
                                }
                                if (this.hasVoice(Voice.STUBBORN)) {
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "Screw the mirror! We just need to find the Princess."));
                                }
                                if (this.hasVoice(Voice.BROKEN)) {
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "I don't want to look at us."));
                                }
                                if (this.hasVoice(Voice.SMITTEN)) {
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "Yes, I fear that we won't like what we'll see. What if we just sit here and preen for a while? That can't hurt, right?"));
                                }
                                if (this.hasVoice(Voice.CHEATED)) {
                                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "It's going to do something to us. I can feel it."));
                                }
                                if (this.hasVoice(Voice.OPPORTUNIST)) {
                                    if (this.nVoices() == 2) {
                                        parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "If he thinks it's bad, I'm with him."));
                                    } else {
                                        parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "If they think it's bad, I'm with them."));
                                    }
                                }
                                if (this.hasVoice(Voice.COLD)) {
                                    if (this.nVoices() == 2) {
                                        parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "You don't need to comfort him."));
                                    } else {
                                        parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "You don't need to comfort them."));
                                    }
                                }

                                boolean repeatSub = true;
                                OptionsMenu subMenu = new OptionsMenu();
                                subMenu.add(new Option(this.manager, "comfortA", "(Explore) It's not the end. Whatever's on the other side is going to be nice."));
                                subMenu.add(new Option(this.manager, "cruel", "(Explore) It's the end for you, but not for me."));
                                subMenu.add(new Option(this.manager, "comfortB", "(Explore) I'll see you on the other side. It's going to be okay."));
                                subMenu.add(new Option(this.manager, "approach", "[Approach the mirror.]"));

                                while (repeatSub) {
                                    switch (parser.promptOptionsMenu(subMenu)) {
                                        case "comfortA":
                                        case "comfortB":
                                            repeatSub = false;
                                            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Okay. If you say so, we'll trust you."));

                                            if (this.hasVoice(Voice.PARANOID)) {
                                                parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "Can we trust you?"));
                                            }
                                            if (this.hasVoice(Voice.HUNTED)) {
                                                parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "I'd like to be somewhere nice."));
                                            }
                                            if (this.hasVoice(Voice.STUBBORN)) {
                                                parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "Maybe there'll be a good fight there. Maybe we'll find her again."));
                                            }
                                            if (this.hasVoice(Voice.BROKEN)) {
                                                parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "A mercy."));
                                            }
                                            if (this.hasVoice(Voice.CONTRARIAN)) {
                                                parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "You're not messing with us, right?"));
                                            }
                                            if (this.hasVoice(Voice.CHEATED)) {
                                                parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "So this is all going to work out?"));
                                            }
                                            if (this.hasVoice(Voice.SKEPTIC)) {
                                                parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "Feels too good to be true."));
                                            }
                                            if (this.hasVoice(Voice.SMITTEN)) {
                                                parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "She'll be there waiting for us, I just know it."));
                                            }
                                            if (this.hasVoice(Voice.OPPORTUNIST)) {
                                                parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "Finally. We're going places."));
                                            }
                                            if (this.hasVoice(Voice.COLD)) {
                                                parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "Whatever makes you happy."));
                                            }

                                            break;
                                        
                                        case "cruel":
                                            repeatSub = false;
                                            this.mirrorCruel();
                                            break;

                                        case "cApproachMirror":
                                        case "approach":
                                            this.repeatActiveMenu = false;
                                            repeatSub = false;
                                            silence = true;
                                            break;
                                    }
                                }

                                break;

                            case "cApproachMirror":
                            case "approach":
                                this.repeatActiveMenu = false;
                                break;

                            default: this.giveDefaultFailResponse(activeOutcome);
                        }
                    }
                }

            // You've now been to the mirror with the Voice of the Hero at least once
            manager.setMirrorScaredFlag();
                
            // Approach the mirror
            parser.printDialogueLine("You approach the mirror.");

            if (silence) {
                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "I don't like that silence."));
                parser.printDialogueLine("The voices feel small, distant as you approach.");
            }
            
            if (!explore) {
                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "This... this doesn't feel right. It feels different. Final."));

                if (this.hasVoice(Voice.CONTRARIAN)) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "Yeah, don't look at it. I don't like that *thing.*"));
                }
                if (this.hasVoice(Voice.HUNTED)) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "That thing reeks of death."));
                }
                if (this.hasVoice(Voice.PARANOID)) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "It's calling us. And not in a good way."));
                }
                if (this.hasVoice(Voice.SKEPTIC)) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "You're right. Part of me wants the truth, but something stronger is holding me back. Fear."));
                }
                if (this.hasVoice(Voice.STUBBORN)) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "Screw the mirror! We just need to find the Princess."));
                }
                if (this.hasVoice(Voice.BROKEN)) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "I don't want to look at us."));
                }
                if (this.hasVoice(Voice.SMITTEN)) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "Yes, I fear that we won't like what we'll see. What if we just sit here and preen for a while? That can't hurt, right?"));
                }
                if (this.hasVoice(Voice.CHEATED)) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "It's going to do something to us. I can feel it."));
                }
                if (this.hasVoice(Voice.OPPORTUNIST)) {
                    if (this.nVoices() == 2) {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "If he thinks it's bad, I'm with him."));
                    } else {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "If they think it's bad, I'm with them."));
                    }
                }
                if (this.hasVoice(Voice.COLD)) {
                    if (this.nVoices() == 2) {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "Ignore him. You have to look."));
                    } else {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "Ignore the cowards. You have to look."));
                    }
                }
            }
        }

        // Gaze into your reflection
        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "gaze", "[Gaze into your reflection.]"));
        parser.promptOptionsMenu(activeMenu);

        this.currentLocation = GameLocation.MIRROR;
        this.mirrorPresent = false;
        this.clearVoices();

        if (this.prevEnding == ChapterEnding.MUTUALLYASSURED || this.prevEnding == ChapterEnding.EMPTYCUP) {
            parser.printDialogueLine("Silence as you reach towards the glass. It's time for you to see what's in it.");
        } else if (this.isFirstVessel || (manager.nClaimedVessels() == 1 && manager.hasClaimedAnyVessel(Vessel.RAZORFULL, Vessel.RAZORHEART))) {
            parser.printDialogueLine("Silence as you reach forward. They're gone, but the mirror remains. It's time for you to see what's in it.");
        } else {
            parser.printDialogueLine("Silence as you reach forward. They're gone once again. The mirror always makes them leave. But you need to see what's in it.");
        }

        System.out.println();
        switch (manager.nClaimedVessels()) {
            case 0:
                parser.printDialogueLine("It's you.");
                this.theSpacesBetween();
                break;
            case 1:
                parser.printDialogueLine("You've grown.");
                this.theSpacesBetween();
                break;
            case 2:
                parser.printDialogueLine("You've withered.");
                this.theSpacesBetween();
                break;
            case 3:
                parser.printDialogueLine("You've unraveled.");
                this.theSpacesBetween();
                break;
            case 4:
                parser.printDialogueLine("You are nothing at all.");
                parser.printDialogueLine("But that isn't right. You can't be nothing. You refocus your gaze, and then you see it: a figure, faint and veiled in shadow, just beyond the reflection.");

                this.activeMenu = new OptionsMenu(true);
                activeMenu.add(new Option(this.manager, "ask", "\"Are you me?\""));
                parser.promptOptionsMenu(activeMenu);
                
                parser.printDialogueLine(new VoiceDialogueLine("I think you know what I am."));
                parser.printDialogueLine("A crack slides down the center of the mirror, splitting the image in the glass in two.");
                parser.printDialogueLine("And then another crack forms, and another, and another, turning the mirror into jagged shards of broken glass.");

                // Leads into Finale.finalMirror()
                break;
        }
    }

    /**
     * The player is cruel to the Voices at the mirror (i.e. tells them they're going to die)
     */
    private void mirrorCruel() {
        manager.incrementCruelCount();

        if (this.hasVoice(Voice.COLD)) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.COLD, "I would have kept them in the dark, if I were you."));
        }

        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "What is that supposed to mean? Whatever awful thing I felt before, it feels so much worse now!"));

        if (this.hasVoice(Voice.HUNTED)) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.HUNTED, "Death. Real death."));
        }
        if (this.hasVoice(Voice.BROKEN)) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.BROKEN, "This is what we all deserve, isn't it?"));
        }
        if (this.hasVoice(Voice.STUBBORN)) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.STUBBORN, "Screw that! This can't be the end! It just can't!"));
        }
        if (this.hasVoice(Voice.OPPORTUNIST)) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.OPPORTUNIST, "You'd better watch your back, you can't get rid of me that easy!"));
        }
        if (this.hasVoice(Voice.CONTRARIAN)) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "He's just messing with us... right?!"));
        }
        if (this.hasVoice(Voice.CHEATED)) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.CHEATED, "So you're the real puppet master here? Can't believe I tried to help you."));
        }
        if (this.hasVoice(Voice.SKEPTIC)) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.SKEPTIC, "No, that can't be right! There has to be something more!"));
        }
        if (this.hasVoice(Voice.PARANOID)) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.PARANOID, "Can't even trust ourself."));
        }
        if (this.hasVoice(Voice.SMITTEN)) {
            parser.printDialogueLine(new VoiceDialogueLine(Voice.SMITTEN, "Do it, then. End us all before I die of a broken heart."));
        }
    }

    /**
     * Runs the encounter with the Shifting Mound after claiming each vessel, excluding the fifth and final vessel
     */
    private void theSpacesBetween() {
        this.currentLocation = GameLocation.PATH;

        System.out.println();
        System.out.println();
        System.out.println();

        // spaces between intro here
        if (this.isFirstVessel) {
            parser.printDialogueLine("You are alone in a place that is empty. It is quiet here.");
        } else {
            parser.printDialogueLine("You find yourself in The Long Quiet once again.");
        }

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "proceed", "[Proceed to the cabin.]"));

        this.repeatActiveMenu = true;
        while (this.repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);

            switch (this.activeOutcome) {
                case "cGoHill":
                case "proceed":
                    this.repeatActiveMenu = false;
                    break;

                case "cGoLeave":
                case "cGoFail":
                case "cEnterFail":
                case "cLeaveFail":
                    parser.printDialogueLine("There is nowhere else for you to go.");
                    break;
                    
                default: this.giveDefaultFailResponse(this.activeOutcome);
            }
        }

        this.currentLocation = GameLocation.HILL;
        if (this.isFirstVessel) {
            manager.setNowPlaying("The Shifting Mound Movement I");
            parser.printDialogueLine("You are at the cabin.");
            parser.printDialogueLine("A mass of hands waits at the top of the hill, holding the Princess upright.");
        } else {
            parser.printDialogueLine("You are at the cabin.");
        }

        this.canApproachHer = true;
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "approach", "[Approach her.]"));

        this.repeatActiveMenu = true;
        while (this.repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);

            switch (this.activeOutcome) {
                case "cGoCabin":
                case "cApproachHer":
                case "approach":
                    this.repeatActiveMenu = false;
                    break;

                case "cGoPath":
                case "cGoFail":
                case "cEnterFail":
                case "cLeaveFail":
                    parser.printDialogueLine("There is nowhere else for you to go.");
                    break;
                    
                default: this.giveDefaultFailResponse(this.activeOutcome);
            }
        }

        this.canApproachHer = false;
        this.withPrincess = true;

        System.out.println();
        switch (manager.nClaimedVessels()) { // conversation
            case 0:
                this.shiftingMoundTalk1();
                break;

            case 1:
                this.shiftingMoundTalk2();
                break;

            case 2:
                this.shiftingMoundTalk3();
                break;

            case 3:
                this.shiftingMoundTalk4();
                break;
        }
    }

    /**
     * Runs the conversation with the Shifting Mound after claiming the first vessel
     */
    private void shiftingMoundTalk1() {
        if (manager.nVesselsAborted() == 0) {
            parser.printDialogueLine(new PrincessDialogueLine("Something finds me in the Long Quiet and brings me the gift of a fragile vessel."));
        } else {
            parser.printDialogueLine("You recognize the presence inhabiting the shell. It is the entity that dwells in the spaces between.");
            parser.printDialogueLine(new PrincessDialogueLine("Something returns to the Long Quiet. It has surrendered its path of annihilation and brings me the gift of a fragile vessel."));
        }

        parser.printDialogueLine("She speaks in a soft voice, almost a whisper.");

        boolean repeatSub;
        OptionsMenu subMenu;
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "dream", "(Explore) \"You're that thing I met in the space outside of the woods, aren't you? I thought that was a dream.\"", manager.nVesselsAborted() != 0));
        activeMenu.add(new Option(this.manager, "what", "(Explore) \"What are you?\""));
        activeMenu.add(new Option(this.manager, "fragile", "(Explore) \"The gift of a fragile vessel?\""));
        activeMenu.add(new Option(this.manager, "end", "(Explore) \"Is this the end of the world?\""));
        activeMenu.add(new Option(this.manager, "letOut", "(Explore) \"Let her out of there!\""));
        activeMenu.add(new Option(this.manager, "narrator", "(Explore) \"Do you know the Narrator?\""));
        activeMenu.add(new Option(this.manager, "trapped", "(Explore) \"Are you what sent me to slay the Princess? Are you what trapped me here?\""));
        activeMenu.add(new Option(this.manager, "worlds", "(Explore) \"Do you know about the worlds beyond this place?\""));
        activeMenu.add(new Option(this.manager, "princess", "(Explore) \"Are you the Princess?\""));
        activeMenu.add(new Option(this.manager, "familiar", "(Explore) \"Do we know each other?\"", manager.nVesselsAborted() == 0));
        activeMenu.add(new Option(this.manager, "whatNow", "\"What happens now?\""));
        activeMenu.add(manager.getIntermissionAttackMound());
        activeMenu.add(manager.getIntermissionAttackSelf());

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "dream":
                    parser.printDialogueLine(new PrincessDialogueLine("Vague recollections. Empty tunnels without a mouth. I am sorry if I frightened you."));
                    break;

                case "what":
                    parser.printDialogueLine(new PrincessDialogueLine("I am solitary lights in an empty city. What are you?"));

                    subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "explore", "(Explore) \"Solitary lights? What do you mean?\""));
                    subMenu.add(new Option(this.manager, "youThink", "\"What do you think I am?\""));
                    subMenu.add(new Option(this.manager, "dunno", "\"I don't know what I am.\""));
                    subMenu.add(new Option(this.manager, "person", "\"I'm a person.\""));

                    repeatSub = true;
                    while (repeatSub) {
                        switch (parser.promptOptionsMenu(subMenu)) {
                            case "explore":
                                parser.printDialogueLine(new PrincessDialogueLine("Thoughts without connections. A dim and nascent network. I wish to be more."));
                                break;

                            case "youThink":
                            case "dunno":
                                repeatSub = false;
                                parser.printDialogueLine(new PrincessDialogueLine("I think that you are like me."));
                                break;

                            case "person":
                                repeatSub = false;
                                parser.printDialogueLine(new PrincessDialogueLine("A person. A set of eyes witnessing from one perspective. I think that you are more like me than you are like a person."));
                                break;
                        }
                    }

                    parser.printDialogueLine(new PrincessDialogueLine("We are oceans reduced to shallow creeks."));
                    break;

                case "fragile":
                    parser.printDialogueLine(new PrincessDialogueLine("Yes. Nerves and fibers to feel the worlds beyond. Perspectives to make my own."));
                    this.giveVesselThoughts(prevEnding.getVessel());
                    break;

                case "end":
                    parser.printDialogueLine(new PrincessDialogueLine("How can the world have ended if we are talking?"));
                    break;

                case "letOut":
                    parser.printDialogueLine(new PrincessDialogueLine("I'm sorry. There are some changes that can never be undone, there are some tears that can never be unshed. This is not a place that can hold a fragment of a concept. The moment she arrived here, she was going to return to me."));
                    parser.printDialogueLine(new PrincessDialogueLine("I promise that it doesn't hurt."));
                    break;

                case "narrator":
                    if (manager.hasClaimedAnyVessel(Vessel.WOUNDEDWILD, Vessel.NETWORKWILD, Vessel.SPECTRE, Vessel.WRAITH, Vessel.TOWER, Vessel.APOTHEOSIS)) {
                        parser.printDialogueLine(new PrincessDialogueLine("I know of him through the memories of my vessel. But she had nothing like him on her own."));
                    } else {
                        parser.printDialogueLine(new PrincessDialogueLine("You are the only thing I have ever known."));
                    }
                    
                    parser.printDialogueLine(new PrincessDialogueLine("The space we're in is vacant. Nothing comes here but us."));
                    break;

                case "trapped":
                    parser.printDialogueLine(new PrincessDialogueLine("I have only just now stirred to consciousness. I could not have trapped you here, and I too yearn to be free."));
                    break;

                case "worlds":
                    parser.printDialogueLine(new PrincessDialogueLine("I know only that they are."));
                    break;

                case "princess":
                    parser.printDialogueLine(new PrincessDialogueLine("She is part of me, and part of me is her."));

                    subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "press", "\"But were you always the Princess, or are you just making her a part of yourself?\""));
                    subMenu.add(new Option(this.manager, "silent", "[Say nothing.]"));

                    if (parser.promptOptionsMenu(subMenu).equals("press")) {
                        parser.printDialogueLine(new PrincessDialogueLine("You speak in circles. Does it matter where one thing begins and another ends?"));
                    }

                    break;

                case "familiar":
                    parser.printDialogueLine(new PrincessDialogueLine("You are familiar, but you are not me. I feel sadness, longing, hope, as I witness you."));
                    break;

                case "whatNow":
                    this.repeatActiveMenu = false;
                    break;

                case "cSlayPrincessNoBladeFail": // Override: you don't need the blade
                    if (manager.getIntermissionAttackMound().hasBeenPicked()) {
                        parser.printDialogueLine("You have already tried that.");
                        break;
                    }
                case "attackMound":
                    parser.printDialogueLine("Your will cuts across the entity in front of you, but nothing happens.");
                    parser.printDialogueLine(new PrincessDialogueLine("My roots burrow in an ocean beyond your sight. We cannot harm each other as we are now."));
                    break;

                case "cSlaySelfNoBladeFail": // Override: you don't need the blade
                    if (manager.getIntermissionAttackSelf().hasBeenPicked()) {
                        parser.printDialogueLine("You have already tried that.");
                        break;
                    }
                case "attackSelf":
                    parser.printDialogueLine("You raise your will to end your life. But as it buries into the space your body should be, you feel nothing at all.");
                    parser.printDialogueLine("One of the many hands in front of you reaches forward, and gently touches the side of your face.");
                    parser.printDialogueLine(new PrincessDialogueLine("There's nowhere for you to be but here."));
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // "What happens now?" continues here
        parser.printDialogueLine(new PrincessDialogueLine("Nothing, as we are. But I know that there are worlds beyond us, and that we are meant to reach them."));
        parser.printDialogueLine(new PrincessDialogueLine("There is no exit, but this vessel is a creature of perception. She can make you forget, if only you believe her to be able to."));
        parser.printDialogueLine(new PrincessDialogueLine("Bring me more perspectives, so that I may be whole, and perhaps then we will know our freedom."));

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "kill", "(Explore) \"Aren't you scared that I'll find a way to kill you?\""));
        activeMenu.add(new Option(this.manager, "howMuch", "(Explore) \"How much will I forget?\""));
        activeMenu.add(new Option(this.manager, "pieces", "(Explore) \"How many more pieces of you do I have to find?\""));
        activeMenu.add(new Option(this.manager, "refuse", "(Explore) \"And what if I don't let you do this to me?\""));
        activeMenu.add(new Option(this.manager, "destroy", "(Explore) \"I was sent to slay the Princess to stop her from destroying the world. If I help you, is that what you're going to do?\""));
        activeMenu.add(new Option(this.manager, "wait", "\"I'm not going back.\" [Wait.]", activeMenu.get("refuse")));
        activeMenu.add(new Option(this.manager, "forget", "\"Okay. Make me forget.\""));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "kill":
                    parser.printDialogueLine(new PrincessDialogueLine("I have not lived. I am not afraid to die."));
                    break;

                case "howMuch":
                    parser.printDialogueLine(new PrincessDialogueLine("Everything, until we meet again."));
                    break;

                case "pieces":
                    parser.printDialogueLine(new PrincessDialogueLine("More than you have found, but less than there are to find. I am infinite. The rest will find their own way home."));
                    break;

                case "refuse":
                    manager.refuseExploreMound();
                    parser.printDialogueLine(new PrincessDialogueLine("Then we will be here forever, as we are now. Unfinished, dry, hollow."));
                    break;

                case "destroy":
                    parser.printDialogueLine(new PrincessDialogueLine("You ask of things that cannot be done. To destroy is merely to reshape. To remold."));

                    subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "press", "\"You're being semantic. What are you going to do if I help you?\""));
                    subMenu.add(new Option(this.manager, "silent", "[Let it be.]"));

                    if (parser.promptOptionsMenu(subMenu).equals("press")) {
                        parser.printDialogueLine(new PrincessDialogueLine("How can I know? I am flickers in something sprawling and unilluminated."));
                    }

                    break;

                case "wait":
                    parser.printDialogueLine(new PrincessDialogueLine("If you need time, then I'll wait for you."));
                    
                    // The original game has a creative gimmick here where, if you choose to wait forever, the game will quit out. When you reopen it, instead of starting on the main menu, the game will start right where you left off, and the Shifting Mound will comment on how long the game was closed for.
                    // Unfortunately, I have no idea how to replicate any of that here, and it would be insanely complicated to figure out (I would need to implement an entire save-and-load system...), so this option is just stuck being kind of weird and lame.

                    subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "wait", true, "[Wait forever.]"));
                    subMenu.add(new Option(this.manager, "forget", "\"Okay. I'm ready. Make me forget.\""));
                    parser.promptOptionsMenu(subMenu);

                    this.repeatActiveMenu = false;
                    break;

                case "forget":
                    this.repeatActiveMenu = false;
                    break;
            }
        }

        // Forget continues here
        parser.printDialogueLine(new PrincessDialogueLine("She asks that I tell you to remember her."));
        parser.printDialogueLine(new PrincessDialogueLine("You won't.", true));
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException("Thread interrupted");
        }

        System.out.println();
        parser.printDialogueLine("Everything goes dark, and you die.");
    }

    /**
     * Runs the conversation with the Shifting Mound after claiming the second vessel
     */
    private void shiftingMoundTalk2() {



        // temporary templates for copy-and-pasting
        /*
        parser.printDialogueLine("XXXXX");
        parser.printDialogueLine(new PrincessDialogueLine("XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "(Explore) XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "\"XXXXX\""));
        */
    }

    /**
     * Runs the conversation with the Shifting Mound after claiming the third vessel
     */
    private void shiftingMoundTalk3() {



        // temporary templates for copy-and-pasting
        /*
        parser.printDialogueLine("XXXXX");
        parser.printDialogueLine(new PrincessDialogueLine("XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "(Explore) XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "\"XXXXX\""));
        */
    }

    /**
     * Runs the conversation with the Shifting Mound after claiming the fourth vessel
     */
    private void shiftingMoundTalk4() {



        // temporary templates for copy-and-pasting
        /*
        parser.printDialogueLine("XXXXX");
        parser.printDialogueLine(new PrincessDialogueLine("XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "(Explore) XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "\"XXXXX\""));
        */
    }

    /**
     * The Shifting Mound gives her thoughts on a given Vessel
     * @param v the Vessel to comment on
     */
    private void giveVesselThoughts(Vessel v) {
        switch (v) {
            // Chapter II
            case ADVERSARY:
                parser.printDialogueLine(new PrincessDialogueLine("This one yearns to grow and struggle. Even now I feel a will pushing against mine, not realizing that we are one. She will make for a bold heart."));
                parser.printDialogueLine(new PrincessDialogueLine("Do not mourn her. We will provide her with the growth she fought for."));
                break;
            case TOWER:
                parser.printDialogueLine(new PrincessDialogueLine("This one is dominance. A figure capable of bending everything to her will. She will make for an overwhelming heart."));
                parser.printDialogueLine(new PrincessDialogueLine("Do not mourn her, for she would not be able to mourn you."));
                break;
            case SPECTRE:
                parser.printDialogueLine(new PrincessDialogueLine("This one is vaporous. She is a dream of a life she could never have, but that longing has given her so much capacity for kindness. She will make for a yearning heart."));
                parser.printDialogueLine(new PrincessDialogueLine("Do not mourn her -- she will finally be able to hold what she never knew."));
                break;
            case NIGHTMARE:
                parser.printDialogueLine(new PrincessDialogueLine("This one is filled with sadness. A doll abandoned to the company of her darkest impulses. She desires only companionship, but the only thing she knows is how to hurt. She will make for a tender heart."));
                parser.printDialogueLine(new PrincessDialogueLine("Do not mourn her -- she has finally found her way home."));
                break;
            case BEAST:
                parser.printDialogueLine(new PrincessDialogueLine("This one is consumed by instinct. A predator pushing those around her to adapt. She will make for a cunning heart."));
                parser.printDialogueLine(new PrincessDialogueLine("She wishes me to devour you. To make you a part of myself. But she is only a voice."));
                parser.printDialogueLine(new PrincessDialogueLine("Do not mourn her, for she is part of something greater."));
                break;
            case WITCH:
                parser.printDialogueLine(new PrincessDialogueLine("This one is hope marred by bitterness and betrayal. She could see the end of the tunnel, and the door was closed on her. She will make for a righteous heart."));
                parser.printDialogueLine(new PrincessDialogueLine("Do not mourn her -- she is finally on the other side."));
                break;
            case STRANGER:
                parser.printDialogueLine(new PrincessDialogueLine("These ones are a contradiction. A winding kaleidoscope of paths unwalked. They are stretched into a shape not unlike me, but it is a shape they cannot hold."));
                if (this.isFirstVessel) {
                    parser.printDialogueLine(new PrincessDialogueLine("I am sorry that you met this vessel so early in your journey, but they will make for a rich and vibrant heart."));
                } else {
                    parser.printDialogueLine(new PrincessDialogueLine("They will make for a rich and vibrant heart."));
                }
                break;
            case PRISONERHEAD:
                parser.printDialogueLine(new PrincessDialogueLine("This one is cold and cynical. She has protected herself when others could not. She will make for a clever heart."));
                parser.printDialogueLine(new PrincessDialogueLine("Do not mourn her -- she doesn't need to be protected any longer."));
                break;
            case PRISONER:
                parser.printDialogueLine(new PrincessDialogueLine("This one is determined, but also cautious, one that would rather let the world move around her than move it herself. She will make for a patient heart."));
                parser.printDialogueLine(new PrincessDialogueLine("Do not mourn her -- she is exactly where she needs to be."));
                break;
            case DAMSEL:
                parser.printDialogueLine(new PrincessDialogueLine("This one is soft and delicate. You molded her to love you, and she'll make for a gentle heart."));
                parser.printDialogueLine(new PrincessDialogueLine("Do not mourn her. She has served her purpose."));
                break;
            case DECONDAMSEL:
                parser.printDialogueLine(new PrincessDialogueLine("This one is a reflecting pool of desire unfulfilled. She will make for a pliant heart."));
                parser.printDialogueLine(new PrincessDialogueLine("Do not mourn her. She has served her purpose."));
                break;

            // Chapter III or IV
            case NEEDLE:
                parser.printDialogueLine(new PrincessDialogueLine("This one remembers a spark lost in time, and she would stop at nothing to reclaim it. She will make for a burning heart."));
                parser.printDialogueLine(new PrincessDialogueLine("Do not mourn her. She has finally remembered what she thought she'd lost."));
                break;
            case FURY:
            case REWOUNDFURY:
                parser.printDialogueLine(new PrincessDialogueLine("This one is desecration. She placed the weight of her agony on you, yet it is she who unwound herself. There is passion and empathy buried under her unfeeling skin. She will make for a weathered heart."));
                parser.printDialogueLine(new PrincessDialogueLine("Do not mourn her -- she has finally found peace."));
                break;
            case APOTHEOSIS:
                parser.printDialogueLine(new PrincessDialogueLine("This one sits at the cusp of awakening. A new god, waiting to be born. She will make for a terrifying and divine heart."));
                parser.printDialogueLine(new PrincessDialogueLine("Do not mourn her, for she has finally found her light."));
                break;
            case PATD:
            case STENCILPATD:
                parser.printDialogueLine(new PrincessDialogueLine("This one is perspectives bleeding into one. You know her better than you know yourself. She will make for an empathetic heart."));
                parser.printDialogueLine(new PrincessDialogueLine("Do not mourn her, for you would not mourn yourself."));
                break;
            case WRAITH:
                parser.printDialogueLine(new PrincessDialogueLine("This one is loneliness turned to seething. She could not find her strength in others, so she found it in herself. She will make for a driven heart."));
                parser.printDialogueLine(new PrincessDialogueLine("Do not mourn her -- she isn't alone anymore."));
                break;
            case CLARITY:
                parser.printDialogueLine(new PrincessDialogueLine("This one is a waiting maw. An inevitable destination where all roads end. She will make for a wise heart."));
                parser.printDialogueLine(new PrincessDialogueLine("Do not mourn her -- she is exactly where she needs to be."));
                break;
            case RAZORFULL:
            case RAZORHEART:
                parser.printDialogueLine(new PrincessDialogueLine("This one is a single-minded edge. She is cruelty. But she is also joy. She will make for a piercing heart."));
                parser.printDialogueLine(new PrincessDialogueLine("Do not mourn her -- she is exactly where she needs to be."));
                break;
            case DEN:
                parser.printDialogueLine(new PrincessDialogueLine("This one is consumed by instinct. A dancer moving to the rhythm of the flesh. She will make for a ravenous heart."));
                // might need another Vessel for den for an extra line??
                parser.printDialogueLine(new PrincessDialogueLine("Do not mourn her, for she is part of something greater."));
                break;
            case NETWORKWILD:
                parser.printDialogueLine(new PrincessDialogueLine("This one is like a shadow of me, twisting vines in search of answers. She will make for a curious heart."));
                parser.printDialogueLine(new PrincessDialogueLine("Do not mourn her -- she has found what she yearned for."));
                break;
            case WOUNDEDWILD:
                parser.printDialogueLine(new PrincessDialogueLine("This one is like a shadow of me, a memory of what she used to be, bound to the wounds of distance and time. She will make for a scarred and beautiful heart."));
                parser.printDialogueLine(new PrincessDialogueLine("Do not mourn her -- she has finally found peace."));
                break;
            case THORN:
                parser.printDialogueLine(new PrincessDialogueLine("This one yearns for connections she feels she doesn't deserve. Even when shown compassion, she hid herself away. She will make for a cautious heart."));
                parser.printDialogueLine(new PrincessDialogueLine("Do not mourn her -- she isn't alone anymore."));
                break;
            case WATCHFULCAGE:
                parser.printDialogueLine(new PrincessDialogueLine("This one is a body that convinced herself she was only a set of eyes. She will make for a watchful heart."));
                parser.printDialogueLine(new PrincessDialogueLine("Do not mourn her. She is now what she wished that she could be."));
                break;
            case OPENCAGE:
                parser.printDialogueLine(new PrincessDialogueLine("This one is a locked door to which you held the key. She will make for an open heart, if you let her."));
                parser.printDialogueLine(new PrincessDialogueLine("Do not mourn her. She is no longer bound."));
                break;
            case BURNEDGREY:
                parser.printDialogueLine(new PrincessDialogueLine("This one is passion betrayed. But even in the end, her love never faded. She will make for a bright heart."));
                parser.printDialogueLine(new PrincessDialogueLine("Do not mourn her, for she has finally found her light."));
                break;
            case DROWNEDGREY:
                parser.printDialogueLine(new PrincessDialogueLine("This one is guarded sorrow. She saw herself as alone, but in the end had the courage to share with another. She will make for a deep heart."));
                parser.printDialogueLine(new PrincessDialogueLine("Do not mourn her -- she has finally been heard."));
                break;
            case HAPPY:
            case HAPPYDRY:
                parser.printDialogueLine(new PrincessDialogueLine("This one is a songbird in a cage of gilded shadows. She will make for an honest heart."));
                parser.printDialogueLine(new PrincessDialogueLine("Do not mourn her. She has finally learned to sing for herself."));
                break;
        }
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

            parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Wh-what's going on. Where are we?"));

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
            parser.printDialogueLine("The world around you is unwound, its physical matter replaced by a textured nothing. You find yourself in The Long Quiet once again. Memory returns.");
        } else if (manager.nVesselsAborted() > 0) {
            parser.printDialogueLine("The world around you is unwound, its physical matter replaced by a textured nothing. It is quiet. You have been here before. Memory returns.");
        } else {
            parser.printDialogueLine("The world around you is unwound, its physical matter replaced by a textured nothingness. It is quiet.");
        }

        parser.printDialogueLine("There is a distant rumbling, a sound of many sounds. Undulations pulse louder as something Other comes close.");
        
        switch (manager.nVesselsAborted()) {
            case 0:
                if (!this.isFirstVessel) parser.printDialogueLine("You already know what dwells in the empty spaces.");
                parser.printDialogueLine("Feelers probe across the fabric of reality. Extremities find your consciousness and wrap themselves around it. You are no longer alone.");

                if (this.isFirstVessel) {
                    parser.printDialogueLine("Confusion. \"Why are you here? I am unfinished.\"");
                    parser.printDialogueLine("Resistance. Fingers drag claws across the glass surface of your soul.");
                    parser.printDialogueLine("Frustration. \"This vessel is full of you. It is useless to us if it doesn't bring more gifts.\"");
                    parser.printDialogueLine("Force pushing against your will. \"NO. You cannot go back. Not there.\"");
                    parser.printDialogueLine("Regret. \"This world is broken beyond repair. We must weave something new.\"");
                    parser.printDialogueLine("A wagging finger. \"There is only so much thread in this place. Do not waste it. I am our only salvation.\"");
                } else {
                    parser.printDialogueLine("Resistance. Fingers drag claws across the glass surface of your soul.");
                    parser.printDialogueLine("Frustration. \"This vessel is full of you. I need something empty I can crawl inside of. I need something shaped like me.\"");

                    this.activeMenu = new OptionsMenu(true);
                    activeMenu.add(new Option(this.manager, "wake", "This is a nightmare. Wake up."));
                    activeMenu.add(new Option(this.manager, "embrace", "Embrace the thoughts constricting you."));

                    switch (parser.promptOptionsMenu(activeMenu)) {
                        case "wake":
                            parser.printDialogueLine("It's not.");
                            break;

                        case "embrace":
                            parser.printDialogueLine("Urgency. \"You have a story you need to finish. It is the only way for us to escape this place.\"");
                            parser.printDialogueLine("Force pushing against your will. \"NO. You cannot go back. Not there.\"");
                            parser.printDialogueLine("Regret. \"This world is broken beyond repair. We must weave something new.\"");
                            parser.printDialogueLine("A wagging finger. \"There is only so much thread in this place. Do not waste it. I am our only salvation.\"");
                            break;
                    }
                }

                break;


            case 1:
                parser.printDialogueLine("That which dwells in the empty spaces contracts across the edges of your mind again. She is furious.");
                parser.printDialogueLine("Betrayal. \"Every door you close on me is a door you close on yourself. Do you want to linger here, entwined with a creature you taught to hate you forever? Eternity never ends.\"");
                parser.printDialogueLine("Cold spite. \"Our infinities shrink into something less. I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I hate you I --\"", true);
                break;


            case 2:
                parser.printDialogueLine("Desperate pleas. \"I do not hate you. I am sorry I said I hate you. I do not have to hate you. We can still leave this place together.\"");
                parser.printDialogueLine("An offering. \"We can be friends.\"");
                parser.printDialogueLine("Ecstasy. You are elated. You have never felt more elated than you feel now. Everything is good. You cannot remember what it is like to feel anything other than euphoric joy.");
                parser.printDialogueLine("A reminder. \"We can be worse than enemies.\"");
                parser.printDialogueLine("Agony. You are torn into a million pieces, and you feel pain in each of them. You have never felt more miserable than you feel now. You cannot remember what it is like to feel anything other than anguish.");
                parser.printDialogueLine("Mercy. You are elated again. You have never felt more elated than you feel now. In contrast to the agony you've suffered, this elation is better than all of the other elation you have experienced.");

                if (this.isFirstVessel) {
                    parser.printDialogueLine("Round eyes looking up at you. \"I need more vessels so that I can be finished. I cannot find them on my own, for they are me. You are the only one who can do this. You are our only salvation.\"");
                } else {
                    parser.printDialogueLine("Round eyes looking up at you. \"I need vessels so that I can be finished. I cannot find them on my own, for they are me. You are the only one who can do this. You are our only salvation.\"");
                }

                break;


            case 3:
                parser.printDialogueLine("Dejection. Feelers limp against your soul. \"Why?\"");
                parser.printDialogueLine("Long silence. A hollow heart.");
                parser.printDialogueLine("\"I don't want to see you.\"");
                break;


            case 4:
                parser.printDialogueLine("The feelers hold you in a gentle caress.");
                parser.printDialogueLine("Resignation. \"I cannot stop you. But our spool is nearly taut.\"");
                parser.printDialogueLine("A warning. \"If you come here again, we will be here forever.\"");
                break;


            case 5: // Oblivion ending
                manager.addToPlaylist("Oblivion");
                parser.printDialogueLine("Oblivion. The many feelers pull your shape into something formless. \"You have made a decision. It is the wrong one. I love you.\"");

                this.activeMenu = new OptionsMenu(true);
                activeMenu.add(new Option(this.manager, "exist", "[Exist.]", 0));
                activeMenu.add(new Option(this.manager, "fade", "[Consciousness fades away.]"));

                for (int i = 0; i < 4; i++) {
                    parser.printDialogueLine("You are bliss. Joy and understanding everywhere at once. Your soul threatens to fade away. \"I love you.\"");
                    parser.printDialogueLine("You are agony. A numbing arm. A parched throat. An open wound. Your soul forced back into existence. \"I love you.\"");

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
            parser.printDialogueLine("All at once, the nothingness shatters.");
        }
    }

}
