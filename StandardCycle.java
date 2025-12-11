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

        manager.setTrueExclusiveMenu(true);
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
            default: super.giveDefaultFailResponse();
        }

        manager.setTrueExclusiveMenu(false);
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
            case "cMeta":
                break;

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
                System.out.println("DEBUG default fail response");
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
                        this.withPrincess = true;
                        this.knowsBlade = true;
                        this.currentLocation = GameLocation.BASEMENT;
                        break;
                    case EMPTYCUP:
                        this.hasBlade = false;
                        this.withPrincess = true;
                        this.knowsBlade = false;
                        this.currentLocation = GameLocation.BASEMENT;
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

            if (manager.demoMode()) {
                if (this.prevEnding == null) {
                    this.prevEnding = ChapterEnding.DEMOENDING;
                } else if (!this.prevEnding.isFinal() && this.prevEnding.getNextChapter().getNumber() > 2) {
                    this.prevEnding = ChapterEnding.DEMOENDING;
                }
            }
        }

        if (this.prevEnding != ChapterEnding.DEMOENDING) this.mirrorSequence();

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
            case ARMSRACE:
            case NOWAYOUT: return this.razor3Intro();
            case DEN: return this.den();
            case WILD: return this.wild();
            case THORN: return this.thorn();
            case CAGE: return this.cage();
            case GREY:
                if (this.prevEnding == ChapterEnding.LADYKILLER) return this.greyBurned();
                else return this.greyDrowned();
            case HAPPY: return this.happilyEverAfter();

            case MUTUALLYASSURED:
            case EMPTYCUP: return this.razor4();
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
                        mainScript.runSection("alreadyTried");
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
                        mainScript.runSection("alreadyTried");
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
                        mainScript.runSection("alreadyTried");
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

        if (!joinLate) secondaryScript.runSection("shareTask");

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

                            break;

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

                    break;

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

                    break;

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

                            break;

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
        secondaryScript.runSection("slayForceRazor");

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
        this.secondaryScript = new Script(this.manager, this.parser, "Chapter2Shared");
        if (this.isFirstVessel) manager.setFirstPrincess(this.activeChapter);

        secondaryScript.runSection();

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

                    secondaryScript.runSection("dejaVu");
                    mainScript.runSection();
                    
                    if (this.activeChapter == Chapter.RAZOR) {
                        switch (this.source) {
                            case "revival":
                                mainScript.runSection("shareNormal");
                                break;

                            default:
                                mainScript.runSection("shareRevival");
                                break;
                        }
                    }
                    
                    break;

                case "dejaVu2":
                    activeMenu.setCondition("assume", true);
                    
                    secondaryScript.runSection("dejaVu2");
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
                    
                    secondaryScript.runSection("happened");
                    mainScript.runSection();
                    
                    if (this.activeChapter == Chapter.RAZOR) {
                        switch (this.source) {
                            case "revival":
                                mainScript.runSection("shareNormal");
                                break;

                            default:
                                mainScript.runSection("shareRevival");
                                break;
                        }
                    }
                    
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
                    
                    secondaryScript.runSection("no");
                    mainScript.runSection();
                    
                    if (this.activeChapter == Chapter.RAZOR) {
                        switch (this.source) {
                            case "revival":
                                mainScript.runSection("shareNormal");
                                break;

                            default:
                                mainScript.runSection("shareRevival");
                                break;
                        }
                    }
                    
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
                    
                    secondaryScript.runSection("died");
                    mainScript.runSection();
                    
                    if (this.activeChapter == Chapter.PRISONER) {
                        mainScript.runSection();
                    } else if (this.activeChapter == Chapter.RAZOR) {
                        switch (this.source) {
                            case "revival":
                                mainScript.runSection("shareNormal");
                                break;

                            default:
                                mainScript.runSection("shareRevival");
                                break;
                        }
                    }
                    
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
                    
                    secondaryScript.runSection("killedSelf");
                    mainScript.runSection();
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
                    
                    secondaryScript.runSection("alreadyKilled");
                    mainScript.runSection();
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
                    
                    secondaryScript.runSection("trapped");
                    mainScript.runSection();
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
                    
                    secondaryScript.runSection("killMe");
                    mainScript.runSection();
                    
                    if (this.activeChapter == Chapter.RAZOR) {
                        switch (this.source) {
                            case "revival":
                                mainScript.runSection("shareNormal");
                                break;

                            default:
                                mainScript.runSection("shareRevival");
                                break;
                        }
                    }
                    
                    break;

                case "slewHer":
                    activeMenu.setCondition("assume", true);
                    activeMenu.setCondition("dejaVu", false);
                    activeMenu.setCondition("happened", false);
                    activeMenu.setCondition("no", false);
                    activeMenu.setCondition("died", false);
                    activeMenu.setCondition("killMe", false);
                    activeMenu.setCondition("wise", false);
                    
                    secondaryScript.runSection("slewHer");
                    mainScript.runSection();
                    
                    if (this.activeChapter == Chapter.RAZOR) {
                        switch (this.source) {
                            case "revival":
                                mainScript.runSection("shareNormal");
                                break;

                            default:
                                mainScript.runSection("shareRevival");
                                break;
                        }
                    }
                    
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
                    
                    secondaryScript.runSection("wise");
                    mainScript.runSection();
                    break;

                case "assume":
                    this.ch2IntroAssumeTruth(youDied, princessDied, shareDied);
                    break;

                case "defy":
                    mainScript.runSection("defy");
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
            secondaryScript.runSection("hillDialogue");
            
            switch (this.activeChapter) {
                case ADVERSARY:
                case BEAST:
                case NIGHTMARE:
                case PRISONER:
                case RAZOR:
                case SPECTRE:
                    mainScript.runSection("hillDialogue");
                    break;

                case DAMSEL:
                    if (this.sharedLoop) {
                        mainScript.runSection("hillDialogueSharedLoop");
                    } else {
                        mainScript.runSection("hillDialogue");
                    }
                    
                    break;

                case TOWER:
                    if (pessimismComment) {
                        mainScript.runSection("hillDialogue");
                    } else {
                        mainScript.runSection("princessHowDangerPessimist");
                        mainScript.runSection("hillDialoguePessimistJoin");
                    }
                    
                    break;

                case WITCH:
                    if (this.sharedLoopInsist) {
                        mainScript.runSection("hillDialogueSharedLoop");
                    } else {
                        mainScript.runSection("hillDialogue");
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
     * The Narrator indulges the player in a "thought experiment" where they have, in fact, been here before
     * @param youDied whether the player died in Chapter I
     * @param princessDied whether the Princess died in Chapter I
     * @param shareDied whether the player (or the Voices) mentioned that they died in Chapter I
     */
    private void ch2IntroAssumeTruth(boolean youDied, boolean princessDied, boolean shareDied) {
        this.sharedLoopInsist = true;

        if (shareDied) secondaryScript.runSection("assumeShareDied");
        else secondaryScript.runSection("assume");

        switch (this.activeChapter) {
            case NIGHTMARE:
                if (source.equals("fled")) {
                    if (this.isHarsh) {
                        mainScript.runSection("assumeFledHarsh");
                    } else {
                        mainScript.runSection("assumeFledSoft");
                    }
                } else {
                    mainScript.runSection("assume");
                }

                break;

            case RAZOR:
                if (source.equals("revival")) {
                    mainScript.runSection("assumeRevival");
                } else {
                    mainScript.runSection("assume");
                }

                break;

            case TOWER:
                if (source.equals("unharmed")) {
                    mainScript.runSection("assumeUnharmed");
                } else {
                    mainScript.runSection("assume");
                }

                break;
                
            case WITCH:
                if (princessDied) {
                    mainScript.runSection("assumeSlain");
                } else {
                    mainScript.runSection("assume");
                }

                break;

            default: mainScript.runSection("assume");
        }

        if (this.activeChapter == Chapter.NIGHTMARE || this.activeChapter == Chapter.PRISONER || this.activeChapter == Chapter.DAMSEL) {
            secondaryScript.runSection("assumePointA");
        } else {
            secondaryScript.runSection("assumePointB");
        }
        
        if (this.activeChapter == Chapter.NIGHTMARE) {
            secondaryScript.runSection("consequenceFreeNightmare");
        } else if (youDied && princessDied) {
            if (this.activeChapter == Chapter.RAZOR) {
                secondaryScript.runSection("consequenceFreeMutualRazor");
            } else {
                secondaryScript.runSection("consequenceFreeMutual");
            }
        } else if (youDied) {
            secondaryScript.runSection("consequenceFreeDied");
        } else if (princessDied) { // Only possible in Spectre
            secondaryScript.runSection("consequenceFreeSlain");
        } else { // Only possible in Witch if you were locked in the basement
            secondaryScript.runSection("consequenceFreeLocked");
        }

        if (this.activeChapter == Chapter.WITCH) {
            if (source.equals("revival")) {
                mainScript.runSection("assume2Revival");
            } else {
                mainScript.runSection("assume2");
            }
        } else {
            mainScript.runSection("assume2");
        }
    }

    /**
     * The player asks the Narrator questions about the Princess
     * @param youDied whether the player died in Chapter I
     * @param princessDied whether the Princess died in Chapter I
     * @return 0 if the player returns to the dialogue menu normally while pessimismComment is false; 1 if the player returns to the dialogue menu normally while pessimismComment is true; 2 if the player proceeds to the cabin via a command; 3 if the player attempts to leave via a command
     */
    private int ch2IntroAskPrincess(boolean youDied, boolean princessDied) {
        secondaryScript.runSection("askPrincess");

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
                    mainScript.runSection("princessTeleport");
                    break;

                case "tips":
                    askCount += 1;
                    secondaryScript.runSection("princessTips");

                    if (this.activeChapter == Chapter.ADVERSARY && askCount == 2) {
                        mainScript.runSection("princessImpatient");
                    }

                    break;

                case "howDanger":
                    askCount += 1;
                    askMenu.setCondition("quote", false);

                    if (this.activeChapter == Chapter.RAZOR) {
                        mainScript.runSection("princessHowDanger");
                    } else if (source.equals("locked")) {
                        mainScript.runSection("princessHowDangerLocked");
                    } else {
                        secondaryScript.runSection("princessHowDanger");
                    }

                    if (this.activeChapter == Chapter.ADVERSARY && askCount == 2) {
                        mainScript.runSection("princessImpatient");
                    } else if (this.activeChapter == Chapter.DAMSEL) {
                        mainScript.runSection("princessHowDanger");
                    } else if (this.activeChapter == Chapter.TOWER && !pessimismComment) {
                        pessimismComment = true;
                        mainScript.runSection("princessHowDangerPessimist");
                    } else if (this.activeChapter == Chapter.PRISONER) {
                        mainScript.runSection("princessHowDanger");

                        OptionsMenu pocketsMenu = new OptionsMenu(true);
                        pocketsMenu.add(new Option(this.manager, "check", "[Check your pockets.]"));
                        pocketsMenu.add(new Option(this.manager, "leave", "[Leave your pockets unchecked.]"));

                        switch (parser.promptOptionsMenu(pocketsMenu, new VoiceDialogueLine("Well? After all that, are you going to check your pockets or not?", true))) {
                            case "check":
                                mainScript.runSection("princessCheckPockets");
                                break;

                            case "leave":
                                mainScript.runSection("princessIgnorePockets");
                                break;
                        }
                    }

                    break;

                case "quote":
                    askCount += 1;
                    askMenu.setCondition("howDanger", false);
                    
                    secondaryScript.runSection("princessQuote");

                    if (this.activeChapter == Chapter.ADVERSARY && askCount == 2) {
                        mainScript.runSection("princessImpatient");
                    } else if (this.activeChapter == Chapter.TOWER && !pessimismComment) {
                        pessimismComment = true;
                        mainScript.runSection("princessQuotePessimist");
                    }

                    break;

                case "basement":
                    askCount += 1;
                    secondaryScript.runSection("princessBasement");

                    if (this.activeChapter == Chapter.ADVERSARY && askCount == 2) {
                        mainScript.runSection("princessImpatient");
                    }

                    break;

                case "whyMe":
                    askCount += 1;

                    if (this.activeChapter == Chapter.ADVERSARY && askCount == 2) {
                        mainScript.runSection("princessImpatient");
                    }
                    
                    switch (this.activeChapter) {
                        case ADVERSARY:
                        case SPECTRE:
                        case NIGHTMARE:
                            mainScript.runSection("princessSpecial");
                            break;

                        case RAZOR:
                            secondaryScript.runSection("princessWhyMe");

                            if (source.equals("revival")) {
                                mainScript.runSection("princessSpecialRevival");
                            } else {
                                mainScript.runSection("princessSpecial");
                            }

                        default:
                            secondaryScript.runSection("princessWhyMe");
                            mainScript.runSection("princessSpecial");
                    }
                    
                    if (this.activeChapter == Chapter.NIGHTMARE && !pleadLeave) {
                        pleadLeave = true;
                        mainScript.runSection("whyMePlead");
                    }

                    break;

                case "cagey":
                    secondaryScript.runSection("princessCagey");

                    switch (this.activeChapter) {
                        case ADVERSARY:
                            mainScript.runSection("princessCagey");
                            break;

                        case BEAST:
                            secondaryScript.runSection("princessCageyArmedInfo");
                            mainScript.runSection("princessCagey");
                            break;

                        case DAMSEL:
                        case PRISONER:
                            secondaryScript.runSection("princessCagey2");
                            break;

                        case NIGHTMARE:
                            secondaryScript.runSection("princessCageyArmedInfo");
                            mainScript.runSection("princessCagey");

                            if (pleadLeave) {
                                mainScript.runSection("cageyNoPlead");
                            } else {
                                pleadLeave = true;
                                mainScript.runSection("cageyPlead");
                            }

                            break;

                        case RAZOR:
                        case SPECTRE:
                            mainScript.runSection("princessCagey");
                            break;

                        case TOWER:
                            if (pessimismComment) {
                                mainScript.runSection("princessCagey");
                            } else {
                                mainScript.runSection("princessCageyPessimist");
                            }

                            break;

                        case WITCH:
                            if (!source.equals("locked")) {
                                secondaryScript.runSection("princessCageyArmedInfo");
                            } else {
                                secondaryScript.runSection("princessCagey2");
                            }
                            
                            mainScript.runSection("princessCagey");
                            break;
                    }

                    break;

                case "return":
                    secondaryScript.runSection("princessReturn");
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
            } else if (askCount == 2) {
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

        mainScript.runSection("abortStart");
        secondaryScript.runSection("abortStart");

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
                    secondaryScript.runSection("abortReturnUgh");
                    mainScript.runSection("abortReturn");

                    return 2;

                case "maybe":
                    secondaryScript.runSection("abortReturnMaybe");

                    if (this.activeChapter == Chapter.DAMSEL) {
                        mainScript.runSection("abortReturnB");
                    } else {
                        mainScript.runSection("abortReturn");
                    }

                    return 2;

                case "lie":
                    repeatMenu = false;
                    secondaryScript.runSection("abortLie");
                    break;

                case "nihilist":
                    secondaryScript.runSection("abortNihilist");
                case "cGoLeave":
                case "nope":
                case "notGoing":
                case "quiet":
                    repeatMenu = false;
                    secondaryScript.runSection("abortCont");
                    break;

                default:
                    this.giveDefaultFailResponse(outcome);
            }
        }

        this.currentLocation = GameLocation.HILL;
        secondaryScript.runSection("abortClearing");

        leaveMenu = new OptionsMenu();
        leaveMenu.add(new Option(this.manager, "cabin", "Okay, okay! I'm going into the cabin. Sheesh."));
        leaveMenu.add(new Option(this.manager, "commit", "[Turn around (again) and leave (again).]"));

        repeatMenu = true;
        while (repeatMenu) {
            outcome = parser.promptOptionsMenu(leaveMenu);
            switch (outcome) {
                case "cabin":
                    secondaryScript.runSection("abortReturnMenu2");
                case "cGoCabin":
                    if (this.activeChapter == Chapter.DAMSEL) {
                        mainScript.runSection("abortReturnC");
                    } else if (this.activeChapter == Chapter.NIGHTMARE) {
                        mainScript.runSection("abortReturnB");
                    } else {
                        mainScript.runSection("abortReturn");
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

        secondaryScript.runSection("abortCommit");
        
        this.abortVessel(false);
        return 0;
    }

    /**
     * The player asks the Narrator about the mirror in the cabin during Chapter II
     * @return true if the player chooses to approach the mirror; false otherwise
     */
    private boolean ch2AskMirror() {
        this.mirrorComment = true;
        
        mainScript.runSection("askMirror");
        secondaryScript.runSection("askMirrorStart");

        boolean defaultCareLie = false;
        boolean defaultWhyLie = false;
        boolean defaultNoMatter = true;
        boolean defaultHandsomeCare = false;
        switch (this.activeChapter) {
            case ADVERSARY:
                defaultCareLie = true;
                defaultNoMatter = false;
                defaultHandsomeCare = true;
                mainScript.runSection("askMirrorStart");
                break;

            case BEAST:
                defaultWhyLie = true;

                if (this.sharedLoop) {
                    mainScript.runSection("askMirrorStartSharedLoop");
                } else {
                    mainScript.runSection("askMirrorStart");
                }

                break;

            case DAMSEL:
            case PRISONER:
            case STRANGER:
                defaultWhyLie = true;
            case NIGHTMARE:
            case WITCH:
                mainScript.runSection("askMirrorStart");
                break;

            case RAZOR:
                defaultCareLie = true;
                defaultNoMatter = false;
                defaultHandsomeCare = true;
                mainScript.runSection("askMirrorStart");
                break;

            case SPECTRE:
                defaultCareLie = true;
                defaultNoMatter = false;
                defaultHandsomeCare = true;
                mainScript.runSection("askMirrorStart");
                break;

            case TOWER:
                defaultNoMatter = false;
                mainScript.runSection("askMirrorStart");
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
                    secondaryScript.runSection("mirrorCareLie");
                    break;

                case "careLieTower":
                    repeatMenu = false;
                    secondaryScript.runSection("mirrorCareLieTower");
                    break;

                case "whyLieWitch":
                    mainScript.runSection("mirrorWhyLie");
                case "whyLieNightmare":
                case "whyLie":
                    repeatMenu = false;
                    secondaryScript.runSection("mirrorWhyLie");
                    break;

                case "handsomeCare":
                    repeatMenu = false;
                    secondaryScript.runSection("mirrorHandsomeCare");
                    break;

                case "handsome":
                case "handsomeStranger":
                    repeatMenu = false;

                    switch (this.activeChapter) {
                        case PRISONER:
                        case TOWER:
                        case WITCH:
                            mainScript.runSection("mirrorHandsome");
                            break;

                        default:
                            secondaryScript.runSection("mirrorHandsomeGeneric");
                    }

                    break;

                case "noMatter":
                case "noMatterRight":
                case "noMatterTower":
                    repeatMenu = false;
                    this.mirrorPresent = false;

                    if (this.activeChapter == Chapter.PRISONER) {
                        mainScript.runSection("mirrorNoMatterIntro");
                        secondaryScript.runSection("mirrorNoMatterJoin");
                    } else {
                        secondaryScript.runSection("mirrorNoMatter");
                    }

                    mainScript.runSection("mirrorGone");

                    break;

                case "silent":
                    repeatMenu = false;
                    secondaryScript.runSection("mirrorSilent");
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
            mainScript.runSection("approachMirror");
        } else {
            secondaryScript.runSection("approachMirror");
        }

        if (this.mirrorComment) {
            secondaryScript.runSection("approachAsked");
        } else {
            secondaryScript.runSection("approachNoAsk");
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
        
        secondaryScript.runSection("approachWipe");

        switch (this.activeChapter) {
            case ADVERSARY:
            case BEAST:
            case DAMSEL:
            case PRISONER:
            case RAZOR:
            case SPECTRE:
            case STRANGER:
                if (this.mirrorComment) {
                    secondaryScript.runSection("approachWipeAsked");
                } else {
                    secondaryScript.runSection("approachWipeNoAsk");
                }

            default:
                mainScript.runSection("mirrorGone");
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

        mainScript.runSection("cabinIntro");

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
                    if (this.ch2AskMirror()) {
                        activeMenu.setCondition("approach", false);
                    }
                    break;

                case "different":
                    secondaryScript.runSection("cabinDifferent");
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
                    mainScript.runSection("takeBlade");
                    break;

                case "cGoStairs":
                case "enter":
                    this.repeatActiveMenu = false;
                    break;

                default:
                    this.giveDefaultFailResponse(activeOutcome);
            }
        }

        if (!this.hasBlade) mainScript.runSection("stairsNoBlade");

        this.currentLocation = GameLocation.BASEMENT;
        this.withPrincess = true;
        this.withBlade = false;
        this.mirrorPresent = false;
        mainScript.runSection("stairsStart");

        if (this.hasBlade) {
            mainScript.runSection("basementStartBlade");
        } else {
            mainScript.runSection("basementStartNoBlade");
        }

        if (manager.trueDemoMode()) return ChapterEnding.DEMOENDING;


        
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

        mainScript.runSection("cabinIntro");

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
                    if (this.ch2AskMirror()) {
                        activeMenu.setCondition("approach", false);
                    }
                    break;

                case "different":
                    mainScript.runSection("cabinDifferent");
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
                    mainScript.runSection("takeBlade");
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
            mainScript.runSection("stairsNoBlade");
        }

        this.currentLocation = GameLocation.STAIRS;
        this.withBlade = false;
        this.mirrorPresent = false;
        mainScript.runSection("stairsStart");

        if (this.hasBlade) {
            mainScript.runSection("stairsJoin");
        } else {
            mainScript.runSection("stairsStartNoBlade");
        }

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
                    secondaryScript.runSection("stairsLeaveFail");
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        this.currentLocation = GameLocation.BASEMENT;
        this.withPrincess = true;
        mainScript.runSection("basementStart");

        if (this.hasBlade) {
            mainScript.runSection("basementStartBlade");

            this.canDropBlade = true;
            this.activeMenu = new OptionsMenu();
            activeMenu.add(new Option(this.manager, "drop", "[Drop it.]"));
            activeMenu.add(new Option(this.manager, "tighten", "[Tighten your grip.]"));

            this.repeatActiveMenu = true;
            while (repeatActiveMenu) {
                switch (parser.promptOptionsMenu(activeMenu)) {
                    case "cDrop":
                    case "drop":
                        this.repeatActiveMenu = false;
                        submitCount += 1;
                        mainScript.runSection("dropWilling");
                        break;

                    case "cSlayPrincessFail":
                    case "tighten":
                        this.repeatActiveMenu = false;
                        resistCount += 1;
                        mainScript.runSection("dropForced");
                        break;

                    default: this.giveDefaultFailResponse();
                }
            }

            this.hasBlade = false;
            this.canDropBlade = false;
            this.withBlade = true;
        } else {
            mainScript.runSection("basementStartNoBlade");
        }

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "no", "\"No.\""));
        activeMenu.add(new Option(this.manager, "kneel", "[Kneel.]"));

        switch (parser.promptOptionsMenu(activeMenu)) {
            case "no":
                if (resistCount == 2) {
                    resistCount += 1;
                    mainScript.runSection("kneelResistAgain");
                } else {
                    mainScript.runSection("kneelResistFirst");
                }

                break;

            case "kneel":
                submitCount += 1;
                mainScript.runSection("kneelWilling");
                break;
        }

        if (manager.trueDemoMode()) return ChapterEnding.DEMOENDING;
        


        
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

        mainScript.runSection("cabinIntro");

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
                    if (this.ch2AskMirror()) {
                        activeMenu.setCondition("approach", false);
                    }
                    break;

                case "different":
                    mainScript.runSection("cabinDifferent");
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
                    mainScript.runSection("takeBlade");
                    break;

                case "cGoStairs":
                case "enter":
                    this.repeatActiveMenu = false;
                    break;

                default:
                    this.giveDefaultFailResponse(activeOutcome);
            }
        }

        this.currentLocation = GameLocation.BASEMENT;
        this.withPrincess = true;
        this.withBlade = false;
        this.mirrorPresent = false;
        mainScript.runSection("stairsStart");

        if (manager.trueDemoMode()) return ChapterEnding.DEMOENDING;



        
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

        mainScript.runSection("cabinIntro");

        if (this.sharedLoop) {
            mainScript.runSection("cabinIntro2SharedLoop");
        } else {
            mainScript.runSection("cabinIntro2");
        }

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
                    if (this.ch2AskMirror()) {
                        activeMenu.setCondition("approach", false);
                    }
                    break;

                case "different":
                    if (this.mirrorComment) {
                        mainScript.runSection("cabinDifferentAskedMirror");
                    } else {
                        mainScript.runSection("cabinDifferent");
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
                    mainScript.runSection("takeBlade");
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
                mainScript.runSection("stairsNoBladeSoft");
            } else {
                mainScript.runSection("stairsNoBladeHarsh");
            }
        }

        this.currentLocation = GameLocation.STAIRS;
        this.withBlade = false;
        this.mirrorPresent = false;
        mainScript.runSection("stairsStart");

        if (this.sharedLoop) {
            mainScript.runSection("stairsStartSharedLoop");
        } else {
            mainScript.runSection("stairsStartNoShare");
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
                        mainScript.runSection("stairsThrowBlade");
                    } else {
                        mainScript.runSection("stairsThrowNoBlade");

                        if (!lookedBack) {
                            lookedBack = true;
                            mainScript.runSection("stairsLookBack");
                        }
                    }

                    break;

                case "talk":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("stairsTalk");
                    break;

                case "noPlan":
                    this.repeatActiveMenu = false;
                    voiceOfReasonComment = true;
                    
                    if (this.hasBlade) {
                        mainScript.runSection("stairsNoPlanBlade");
                    } else {
                        mainScript.runSection("stairsNoPlanNoBlade");

                        if (!lookedBack) {
                            lookedBack = true;
                            mainScript.runSection("stairsLookBack");
                        }
                    }

                    break;

                case "stepOff":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("stairsStepOff");

                    this.activeMenu = new OptionsMenu(true);
                    activeMenu.add(new Option(this.manager, "curious", "I was curious."));
                    activeMenu.add(new Option(this.manager, "dunno", "I don't know. Falling into an infinite void seemed better than going downstairs and dying. I'm just scared."));
                    activeMenu.add(new Option(this.manager, "silent", "[Say nothing.]"));

                    switch (parser.promptOptionsMenu(activeMenu)) {
                        case "curious":
                            mainScript.runSection("stepOffCurious");
                            break;

                        case "dunno":
                            mainScript.runSection("stepOffDunno");
                            break;

                        case "silent":
                            mainScript.runSection("stepOffJoin");
                            break;
                    }

                    break;

                case "cGoBasement":
                case "silent":
                    this.repeatActiveMenu = false;
                    break;

                case "cGoCabin":
                    if (lookedBack) {
                        mainScript.runSection("stairsLookBackAgain");
                    } else {
                        lookedBack = true;
                        mainScript.runSection("stairsLookBackJoin");
                    }

                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        this.currentLocation = GameLocation.BASEMENT;
        mainScript.runSection("basementStart");

        if (voiceOfReasonComment) {
            mainScript.runSection("basementReason");
        } else {
            mainScript.runSection("basementNoReason");
        }

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
                    mainScript.runSection("startLeft");
                    break;

                case "right":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("startRight");
                    break;

                case "nothing":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("startNothing");
                    break;

                case "cGoStairs":
                case "back":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("startBack");
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }
        
        this.withPrincess = true;

        if (this.hasBlade) {
            if (this.isHarsh) {
                mainScript.runSection("startBladeHarsh");
            } else {
                mainScript.runSection("startBladeSoft");
            }
        } else {
            if (this.isHarsh) {
                mainScript.runSection("startNoBladeHarsh");
            } else {
                mainScript.runSection("startNoBladeSoft");
            }
        }

        if (manager.trueDemoMode()) return ChapterEnding.DEMOENDING;
        


        
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

        mainScript.runSection("cabinIntro");

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
                    if (this.ch2AskMirror()) {
                        activeMenu.setCondition("approach", false);
                    }
                    break;

                case "different":
                    secondaryScript.runSection("cabinDifferent");
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
                    mainScript.runSection("takeBlade");
                    break;

                case "cGoStairs":
                case "enter":
                    this.repeatActiveMenu = false;
                    break;

                default:
                    this.giveDefaultFailResponse(activeOutcome);
            }
        }

        if (!this.hasBlade) mainScript.runSection("stairsNoBlade");

        this.currentLocation = GameLocation.BASEMENT;
        this.withPrincess = true;
        this.withBlade = false;
        this.mirrorPresent = false;
        mainScript.runSection("stairsStart");

        if (this.sharedLoop) {
            mainScript.runSection("stairsStartSharedLoop");
            mainScript.runSection("basementStartSharedLoop");
        } else {
            mainScript.runSection("stairsStartNoShare");
            mainScript.runSection("basementStartJoin");
        }

        if (this.hasBlade) {
            mainScript.runSection("basementStartBlade");
        } else {
            mainScript.runSection("basementStartNoBlade");
        }

        if (manager.trueDemoMode()) return ChapterEnding.DEMOENDING;

        String honestText = "(Explore) \"What if we're both honest with each other? I was sent here to stop you from ending the world, and you ";
        if (source.equals("revival")) {
            honestText += "killed me last time after coming back from being stabbed in the heart.\"";
        } else {
            honestText += "slashed my throat last time.\"";
        }

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "knife2", "(Explore) \"Prove it then. Prove that you don't have a knife.\""));
        activeMenu.add(new Option(this.manager, "knife3", "(Explore) \"But what if you're just hiding it somewhere secret?\"", activeMenu.get("knife2")));
        activeMenu.add(new Option(this.manager, "stab", "(Explore) \"If I come close to you, you're just going to stab me, aren't you?\""));
        activeMenu.add(new Option(this.manager, "lastTime1", "(Explore) \"Do you remember what happened last time?\""));
        activeMenu.add(new Option(this.manager, "lastTime2", "(Explore) \"But that's exactly what happened! So you do remember it.\"", activeMenu.get("lastTime1")));
        activeMenu.add(new Option(this.manager, "knife1", "(Explore) \"I know you have a knife. I'll let you out of here if you drop it.\""));
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

        // I need to set the prerequisite manually here, since this option comes before its prerequisite option in the menu
        activeMenu.get("knife2").setPrerequisite(activeMenu.get("knife1"));


        boolean lastTimeFlag = false;
        boolean loopComment = false;
        boolean followUpFlag = false;
        int followUpCount = 0;
        
        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "knife1":
                case "knife2":
                case "knife3":
                case "goodWill":
                case "letOut":
                case "activities":
                    mainScript.runSection(activeOutcome);
                    break;

                case "stab":
                case "talk":
                case "key":
                case "mad":
                case "nobody":
                    followUpFlag = true;
                    mainScript.runSection(activeOutcome);
                    break;

                case "lastTime1":
                    lastTimeFlag = true;

                    switch (this.source) {
                        case "mutual":
                            mainScript.runSection("lastTime1Mutual");
                            break;

                        case "revival":
                            mainScript.runSection("lastTime1Revival");
                            break;

                        case "pathetic":
                            mainScript.runSection("lastTime1Pathetic");
                            break;
                    }

                    break;

                case "lastTime2":
                    mainScript.runSection("lastTime2");
                    if (loopComment) mainScript.runSection("lastTime2LoopComment");
                    break;

                case "suspicious":
                    followUpFlag = true;
                    if (this.hasBlade) {
                        mainScript.runSection("suspiciousBlade");
                    } else {
                        mainScript.runSection("suspiciousNoBlade");
                    }
                    
                    break;

                case "lastKill":
                    lastTimeFlag = true;
                    mainScript.runSection("lastKill");
                    if (!loopComment) mainScript.runSection("lastKillLoopComment");
                    break;

                case "happened":
                    lastTimeFlag = true;
                    mainScript.runSection("happened");
                    break;

                case "honest":
                    lastTimeFlag = true;
                    followUpFlag = true;
                    mainScript.runSection("honest");
                    break;


                case "approach":
                    mainScript.runSection("approachEnd");
                    if (source.equals("revival")) {
                        mainScript.runSection("approachJoin");
                    } else {
                        mainScript.runSection("approachNonRevival");
                    }

                    if (this.hasBlade) {
                        return ChapterEnding.TOARMSRACEBORED;
                    } else {
                        return ChapterEnding.TONOWAYOUTBORED;
                    }

                case "cGoStairs":
                    mainScript.runSection("leaveAttemptSilent");
                case "bye":
                case "rightBack":
                    mainScript.runSection("leaveAttempt");
                    return this.razorInitiative(true);

                case "cSlayPrincess":
                case "slay":
                    mainScript.runSection("slayEnd");
                    return ChapterEnding.TOARMSRACEFIGHT;

                case "cSlayPrincessNoBladeFail":
                    mainScript.runSection("slayAttemptNoBlade");
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }


            if (lastTimeFlag && !loopComment) {
                loopComment = true;
                
                if (this.sharedLoopInsist) {
                    mainScript.runSection("loopCommentShare");
                } else {
                    mainScript.runSection("loopCommentNoShare");
                }
            } else if (followUpFlag) {
                switch (followUpCount) {
                    case 1:
                        if (this.hasBlade) {
                            mainScript.runSection("followUp1Blade");
                        } else {
                            mainScript.runSection("followUp1NoBlade");
                        }

                        break;

                    case 2:
                        mainScript.runSection("followUp2");
                        break;

                    case 3:
                        mainScript.runSection("followUp3");
                        break;

                    case 4:
                        mainScript.runSection("followUp4");
                        return this.razorInitiative(false);
                }
                
                followUpFlag = false;
                followUpCount += 1;
            }
        }

        throw new RuntimeException("No ending found");
    }

    /**
     * The Razor takes initiative and attacks the player, ending the Chapter
     * @param leaveAttempt whether the player reached this ending by attempting to leave (giving them the Voice of the Paranoid instead of the Voice of the Broken)
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding razorInitiative(boolean leaveAttempt) {
        if (this.hasBlade) {
            mainScript.runSection("intiativeEndBlade");

            if (source.equals("revival")) {
                mainScript.runSection("intiativeEndBladeRevival");
            } else {
                mainScript.runSection("intiativeEndBladeOther");
            }

            if (leaveAttempt) {
                return ChapterEnding.TOARMSRACELEFT;
            } else {
                return ChapterEnding.TOARMSRACEBORED;
            }
        } else {
            mainScript.runSection("intiativeEndNoBlade");

            if (leaveAttempt) {
                return ChapterEnding.TONOWAYOUTLEFT;
            } else {
                return ChapterEnding.TONOWAYOUTBORED;
            }
        }
    }


    // - Chapter III: The Arms Race / No Way Out -

    /**
     * Runs the opening sequence of Chapter III: The Arms Race / No Way Out
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding razor3Intro() {
        /*
          Possible starting combinations for The Arms Race:
            - Cheated + Hunted + Stubborn
            - Cheated + Hunted + Broken
            - Cheated + Hunted + Paranoid

          Possible starting combinations for No Way Out:
            - Cheated + Contrarian + Broken
            - Cheated + Contrarian + Paranoid
         */

        mainScript.runSection();

        int waitTime = (manager.globalSlowPrint()) ? 500 : 750;
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            throw new RuntimeException("Thread interrupted");
        }

        mainScript.runSection();

        if (this.hasBlade) {
            this.hasBlade = true;
            mainScript.runSection("startArmsRace");mainScript.runSection("takeBladeArmsRace");
        } else {
            this.threwBlade = true;
            mainScript.runSection("startNoWayOut");
            mainScript.runSection("takeBladeNoWayOut");
        }

        if (this.mirrorComment || this.touchedMirror) {
            mainScript.runSection("mirrorCommented");
        } else {
            mainScript.runSection("mirrorNotCommented");
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
        
        mainScript.runSection("approachMirror");

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

        this.currentLocation = GameLocation.BASEMENT;
        this.withPrincess = true;
        this.canSlayPrincess = true;
        this.mirrorPresent = false;
        mainScript.runSection("wipeMirror");

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
        activeMenu.add(new Option(this.manager, "coldB", "We're going to fight her, and we're going to have a stiff upper lip about it. She can't hurt us if we don't let ourselves feel it.", this.hasBlade && !this.hasVoice(Voice.STUBBORN)));
        activeMenu.add(new Option(this.manager, "coldNWO", "We're going to let her stab us, and we're going to have a stiff upper lip about it. She can't hurt us if we don't let ourselves feel it.", !this.hasBlade));
        activeMenu.add(new Option(this.manager, "contra", "She wins by killing us, right? So let's beat her to it!", !this.hasVoice(Voice.CONTRARIAN)));
        activeMenu.add(new Option(this.manager, "skeptic", "[All of these ideas suck. Think up something better.]"));

        if (this.hasBlade) {
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
        this.secondaryScript = new Script(this.manager, this.parser, "Routes/Razor/BasementArmsRace");

        secondaryScript.runSection();

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
                    secondaryScript.runSection("coldMenuStubborn");
                case "coldB":
                    this.repeatActiveMenu = false;
                    activeMenu.setCondition("coldA", false);
                    mainScript.runSection("coldMenuJoin");

                    subMenu = new OptionsMenu(true);
                    activeMenu.add(new Option(this.manager, "taunt", "\"Do your worst! I bet you can't even hurt me.\""));
                    activeMenu.add(new Option(this.manager, "wait", "[Wait for her to come to you.]"));

                    switch (parser.promptOptionsMenu(subMenu)) {
                        case "taunt":
                            mainScript.runSection("coldTaunt");
                            break;

                        case "wait":
                            mainScript.runSection("coldWait");
                            break;
                    }
                    
                    secondaryScript.runSection("coldMenu");

                    if (this.hasVoice(Voice.STUBBORN)) {
                        secondaryScript.runSection("coldStubborn");
                    } else {
                        secondaryScript.runSection("coldNoStubborn");
                    }

                    mainScript.runSection("coldJoin");
                    this.addVoice(Voice.COLD);
                    break;
                    
                case "stubborn":
                    this.repeatActiveMenu = false;
                    if (this.hasVoice(Voice.COLD)) this.canSlayPrincess = false;
                    activeMenu.setCondition("coldA", true);
                    activeMenu.setCondition("coldB", false);

                    secondaryScript.runSection("stubbornMenu");

                    // Your choice here doesn't actually matter
                    subMenu = new OptionsMenu(true);
                    activeMenu.add(new Option(this.manager, "cheer", "Cheer up! Maybe we'll win!"));
                    activeMenu.add(new Option(this.manager, "see", "See, but that's the brilliance of it all. She doesn't think we have it in us to win."));
                    activeMenu.add(new Option(this.manager, "done", "I'm done explaining myself. I'm going to stab her now."));
                    parser.promptOptionsMenu(subMenu);
                    
                    secondaryScript.runSection();
                    this.addVoice(Voice.STUBBORN);
                    break;
                    
                case "oppo":
                    this.repeatActiveMenu = false;
                    secondaryScript.runSection("oppoMenu");
                    mainScript.runSection("oppoMenu");

                    subMenu = new OptionsMenu(true);
                    activeMenu.add(new Option(this.manager, "winner", "\"You know, I'm a big fan of winners, and you've got 'winner' written all over you. How about we stop fighting and team up? I'll even let you be in charge!\""));
                    activeMenu.add(new Option(this.manager, "join", "\"Look, both of us are stuck here against our will. What if we joined forces?\""));
                    activeMenu.add(new Option(this.manager, "stabbing", "\"Has anyone ever told you how good you are at stabbing things?\""));

                    switch (parser.promptOptionsMenu(subMenu)) {
                        case "winner":
                        case "join":
                            mainScript.runSection("oppoJoin");
                            break;

                        case "stabbing":
                            secondaryScript.runSection("oppoStabbing");

                            subMenu = new OptionsMenu(true);
                            activeMenu.add(new Option(this.manager, "goodSide", "\"Yes! Yes, I am trying to get on your good side. Did it work?\""));
                            activeMenu.add(new Option(this.manager, "bored", "\"Yes! Yes, I am bored of you stabbing me. Can you stop stabbing me now?\""));
                            activeMenu.add(new Option(this.manager, "facts", "\"Psht. What? Me? Fluffing you up? I'm just stating facts.\""));
                            activeMenu.add(new Option(this.manager, "silent", "[Say nothing.]"));

                            switch (parser.promptOptionsMenu(subMenu)) {
                                case "goodSide":
                                    mainScript.runSection("oppoGoodSide");
                                    break;
                                    
                                case "bored":
                                    mainScript.runSection("oppoBored");
                                    break;
                                    
                                case "facts":
                                    mainScript.runSection("oppoFacts");
                                    break;
                                    
                                case "silent":
                                    mainScript.runSection("oppoSilent");
                                    break;
                            }

                            break;
                    }
                    
                    secondaryScript.runSection("oppoJoin");

                    if (this.ch3Voice == Voice.STUBBORN) {
                        secondaryScript.runSection("oppoStubborn");
                    } else {
                        secondaryScript.runSection("oppoNoStubborn");
                    }
                    
                    mainScript.runSection("oppoEnd");
                    this.addVoice(Voice.OPPORTUNIST);
                    break;
                    
                case "broken":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("brokenMenu");

                    // Your choice here doesn't actually matter
                    subMenu = new OptionsMenu(true);
                    activeMenu.add(new Option(this.manager, "giveUp", "\"I give up. I'll do anything, just please don't stab me!\""));
                    activeMenu.add(new Option(this.manager, "silent", "[Silently throw your hands in the air.]"));
                    parser.promptOptionsMenu(subMenu);

                    secondaryScript.runSection("brokenMenu");
                    this.addVoice(Voice.BROKEN);
                    break;
                    
                case "smitten":
                    this.repeatActiveMenu = false;
                    secondaryScript.runSection("smittenMenu");

                    subMenu = new OptionsMenu(true);
                    activeMenu.add(new Option(this.manager, "gorgeous", "\"I know you want to kill me, but has anyone ever told you how gorgeous you are?\""));
                    activeMenu.add(new Option(this.manager, "getYou", "\"I just feel like I really get you. I like you. Romantically, even. Maybe we can hash this out over a date.\""));
                    activeMenu.add(new Option(this.manager, "dinner", "\"How about you buy me dinner before impaling me to death?\""));
                    activeMenu.add(new Option(this.manager, "theLook", "[Give her *The Look.*]"));

                    switch (parser.promptOptionsMenu(subMenu)) {
                        case "gorgeous":
                            mainScript.runSection("smittenGorgeous");
                            break;

                        case "getYou":
                            mainScript.runSection("smittenGetYou");
                            break;

                        case "dinner":
                            mainScript.runSection("smittenDinner");
                            break;

                        case "theLook":
                            secondaryScript.runSection("smittenTheLook");
                            mainScript.runSection("smittenTheLook");
                            break;
                    }

                    this.addVoice(Voice.SMITTEN);
                    break;
                    
                case "para":
                    this.repeatActiveMenu = false;
                    secondaryScript.runSection("paraMenu");
                    mainScript.runSection("paraMenu");
                    this.addVoice(Voice.PARANOID);
                    break;
                    
                case "cSlaySelf":
                case "contra":
                    this.repeatActiveMenu = false;
                    this.canSlaySelf = false;
                    mainScript.runSection("contraMenu");
                    this.addVoice(Voice.CONTRARIAN);
                    break;
                    
                case "skeptic":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("skepticMenu");
                    this.addVoice(Voice.SKEPTIC);
                    break;

                case "cGoStairs":
                    mainScript.runSection("leaveAttempt");
                    break;

                case "cSlayPrincessFail":
                case "cSlaySelfFail":
                case "noFightOptions":
                    mainScript.runSection("failedSlayAttempt");
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        this.razor3Ending();
    }

    /**
     * Runs the basement section of Chapter III: No Way Out
     */
    private void noWayOutBasement() {
        this.secondaryScript = new Script(this.manager, this.parser, "Routes/Razor/BasementNoWayOut");

        secondaryScript.runSection();

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
                    secondaryScript.runSection("stubbornMenu");

                    // Your choice here doesn't actually matter
                    subMenu = new OptionsMenu(true);
                    activeMenu.add(new Option(this.manager, "maybe", "Maybe we'll win!"));
                    activeMenu.add(new Option(this.manager, "see", "See, but that's the brilliance of it all. She won't see it coming."));
                    activeMenu.add(new Option(this.manager, "done", "I'm done explaining myself. I'm going to punch her now."));

                    if (parser.promptOptionsMenu(subMenu).equals("maybe")) {
                        secondaryScript.runSection("stubbornMaybe");
                    }

                    secondaryScript.runSection("stubbornCont");
                    break;
                    
                case "oppo":
                    this.repeatActiveMenu = false;
                    secondaryScript.runSection("oppoMenu");
                    mainScript.runSection("oppoMenu");

                    subMenu = new OptionsMenu(true);
                    activeMenu.add(new Option(this.manager, "winner", "\"You know, I'm a big fan of winners, and you've got 'winner' written all over you. How about we stop fighting and team up? I'll even let you be in charge!\""));
                    activeMenu.add(new Option(this.manager, "join", "\"Look, both of us are stuck here against our will. What if we joined forces?\""));
                    activeMenu.add(new Option(this.manager, "stabbing", "\"Has anyone ever told you how good you are at stabbing things?\""));

                    switch (parser.promptOptionsMenu(subMenu)) {
                        case "winner":
                        case "join":
                            mainScript.runSection("oppoJoin");
                            break;

                        case "stabbing":
                            secondaryScript.runSection("oppoStabbing");

                            subMenu = new OptionsMenu(true);
                            activeMenu.add(new Option(this.manager, "goodSide", "\"Yes! Yes, I am trying to get on your good side. Did it work?\""));
                            activeMenu.add(new Option(this.manager, "facts", "\"Psht. What? Me? Fluffing you up? I'm just stating facts.\""));
                            activeMenu.add(new Option(this.manager, "silent", "[Say nothing.]"));

                            switch (parser.promptOptionsMenu(subMenu)) {
                                case "goodSide":
                                    mainScript.runSection("oppoGoodSide");
                                    break;
                                    
                                case "facts":
                                    mainScript.runSection("oppoFacts");
                                    break;
                                    
                                case "silent":
                                    mainScript.runSection("oppoSilent");
                                    break;
                            }

                            break;
                    }

                    secondaryScript.runSection("oppoJoin");
                    mainScript.runSection("oppoEnd");
                    break;
                    
                case "broken":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("brokenMenu");

                    subMenu = new OptionsMenu(true);
                    activeMenu.add(new Option(this.manager, "giveUp", "\"I give up. I'll do anything, just please don't stab me!\""));
                    activeMenu.add(new Option(this.manager, "silent", "[Silently throw your hands in the air.]"));

                    if (parser.promptOptionsMenu(subMenu).equals("silent")) {
                        secondaryScript.runSection("brokenSilent");
                    }
                    
                    secondaryScript.runSection("brokenMenu");
                    this.addVoice(Voice.BROKEN);
                    break;
                    
                case "hunted":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("huntedMenu");
                    this.addVoice(Voice.HUNTED);
                    break;
                    
                case "smitten":
                    this.repeatActiveMenu = false;
                    secondaryScript.runSection("smittenMenu");

                    subMenu = new OptionsMenu(true);
                    activeMenu.add(new Option(this.manager, "gorgeous", "\"I know you want to kill me, but has anyone ever told you how gorgeous you are?\""));
                    activeMenu.add(new Option(this.manager, "getYou", "\"I just feel like I really get you. I like you. Romantically, even. Maybe we can hash this out over a date.\""));
                    activeMenu.add(new Option(this.manager, "dinner", "\"How about you buy me dinner before impaling me to death?\""));
                    activeMenu.add(new Option(this.manager, "theLook", "[Give her *The Look.*]"));

                    switch (parser.promptOptionsMenu(subMenu)) {
                        case "gorgeous":
                            mainScript.runSection("smittenGorgeous");
                            break;

                        case "getYou":
                            mainScript.runSection("smittenGetYou");
                            break;

                        case "dinner":
                            mainScript.runSection("smittenDinner");
                            break;

                        case "theLook":
                            secondaryScript.runSection("smittenTheLook");
                            mainScript.runSection("smittenTheLook");
                            break;
                    }

                    break;
                    
                case "para":
                    this.repeatActiveMenu = false;
                    secondaryScript.runSection("paraMenu");
                    mainScript.runSection("paraMenu");
                    break;

                case "coldNWO":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("coldMenuNoWayOut");

                    subMenu = new OptionsMenu(true);
                    activeMenu.add(new Option(this.manager, "taunt", "\"Do your worst! I bet you can't even hurt me.\""));
                    activeMenu.add(new Option(this.manager, "wait", "[Wait for her to come to you.]"));

                    switch (parser.promptOptionsMenu(subMenu)) {
                        case "taunt":
                            mainScript.runSection("coldTaunt");
                            break;

                        case "wait":
                            mainScript.runSection("coldWait");
                            break;
                    }
                    
                    secondaryScript.runSection("coldMenu");
                    mainScript.runSection("coldMenu");
                    break;
                    
                case "skeptic":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("skepticMenu");
                    this.addVoice(Voice.SKEPTIC);
                    break;

                case "cGoStairs":
                    mainScript.runSection("leaveAttempt");
                    break;

                case "cSlayPrincessFail":
                    mainScript.runSection("failedSlayAttempt");
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        this.razor3Ending();
    }

    /**
     * Runs the ending of Chapter III: The Arms Race / No Way Out (after dying for the first time)
     */
    private void razor3Ending() {
        mainScript.runSection("endStart");

        if (this.hasVoice(Voice.PARANOID)) {
            mainScript.runSection("endStartPara");
        } else if (this.hasVoice(Voice.BROKEN)) {
            mainScript.runSection("endStartBroken");
        } else {
            mainScript.runSection("endStartOther");
        }

        if (this.hasBlade) {
            mainScript.runSection("endHowManyArmsRace");
        } else {
            mainScript.runSection("endHowManyNoWayOut");
        }

        if (this.ch3Voice != Voice.PARANOID) {
            mainScript.runSection("endPara");
        }
        if (this.ch3Voice != Voice.BROKEN) {
            mainScript.runSection("endBroken");
        } 

        mainScript.runSection("endVoicesJoin");

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);

            // Handle "slay" command here, redirecting to different options in the menu based on which fight options are available
            if (activeOutcome.equals("cSlayPrincess")) {
                if (!this.hasVoice(Voice.STUBBORN)) {
                    this.activeOutcome = "stubborn";
                } else if (this.hasBlade && !this.hasVoice(Voice.COLD)) {
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
                    mainScript.runSection("endMenu");
                    this.razor3MontageVoiceComment(Voice.COLD);
                    break;
                    
                case "stubborn":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("endMenu");
                    this.razor3MontageVoiceComment(Voice.STUBBORN);
                    break;
                    
                case "oppo":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("endMenu");
                    this.razor3MontageVoiceComment(Voice.OPPORTUNIST);
                    break;
                    
                case "broken":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("endMenu");
                    this.razor3MontageVoiceComment(Voice.BROKEN);
                    break;
                    
                case "hunted":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("endMenu");
                    this.razor3MontageVoiceComment(Voice.HUNTED);
                    break;
                    
                case "smitten":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("endMenu");
                    this.razor3MontageVoiceComment(Voice.SMITTEN);
                    break;
                    
                case "para":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("endMenu");
                    this.razor3MontageVoiceComment(Voice.PARANOID);
                    break;
                    
                case "cSlaySelf":
                case "contra":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("endMenu");
                    this.razor3MontageVoiceComment(Voice.CONTRARIAN);
                    break;
                    
                case "skeptic":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("endMenu");
                    this.razor3MontageVoiceComment(Voice.SKEPTIC);
                    break;

                case "cGoStairs":
                    mainScript.runSection("leaveAttempt");
                    break;

                case "cSlaySelfFail":
                    if (!this.hasBlade) {
                        this.giveDefaultFailResponse(activeOutcome);
                        break;
                    }
                case "cSlayPrincessFail":
                case "noFightOptions":
                    mainScript.runSection("failedSlayAttempt");
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
                    mainScript.runSection("montage1");
                    break;

                case 2:
                    if (!this.hasBlade || contraLast) {
                        mainScript.runSection("montage2NoBlade");
                    } else {
                        mainScript.runSection("montage2Blade");
                    }

                    break;

                case 3:
                    if (this.hasBlade) {
                        mainScript.runSection("montage3Blade");
                    } else {
                        mainScript.runSection("montage3NoBlade");
                    }

                    break;

                case 4:
                    if (this.hasBlade) {
                        mainScript.runSection("montage4Blade");
                    } else {
                        mainScript.runSection("montage4NoBlade");
                    }

                    break;

                case 5:
                    mainScript.runSection("montage5");
                    break;
            }

            this.razor3MontageVoiceComment(v);
            contraLast = v == Voice.CONTRARIAN;
        }

        mainScript.runSection("montageEnd");
    }

    /**
     * The Voices and the Princess comment on a segment of the montage in Chapter III: The Arms Race / No Way Out
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
                mainScript.runSection("montageBroken");
                break;
                
            case COLD:
                mainScript.runSection("montageCold");
                break;
                
            case CONTRARIAN:
                mainScript.runSection("montageContra");
                break;
                
            case HUNTED:
                mainScript.runSection("montageHunted");
                break;
                
            case OPPORTUNIST:
                mainScript.runSection("montageOppo");
                break;
                
            case PARANOID:
                mainScript.runSection("montagePara");
                break;
                
            case SKEPTIC:
                mainScript.runSection("montageSkeptic");
                break;
                
            case SMITTEN:
                mainScript.runSection("montageSmitten");
                break;
                
            case STUBBORN:
                mainScript.runSection("montageStubborn");
                break;
        }
    }


    // - Chapter IV: Mutually Assured Destruction / The Empty Cup -

    /**
     * Runs Chapter IV: Mutually Assured Destruction / The Empty Cup
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding razor4() {
        // You have all Voices
        
        mainScript.runSection();

        if (this.hasBlade) {
            mainScript.runSection("startBlade");
            mainScript.runSection("stubbornStartBlade");
            mainScript.runSection("stairsBlade");
            mainScript.runSection("voicesContBlade");
        } else {
            mainScript.runSection("startNoBlade");
            mainScript.runSection("stubbornStartNoBlade");
            mainScript.runSection("stairsNoBlade");
            mainScript.runSection("voicesContNoBlade");
        }

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "empty", "[Empty your mind.]"));
        activeMenu.add(new Option(this.manager, "empty2", "[Him too.]", activeMenu.get("empty")));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu, new DialogueLine("[You have no other option.]", true))) {
                case "empty":
                    mainScript.runSection("empty");
                    break;

                case "empty2":
                    this.repeatActiveMenu = false;
                    break;
            }
        }
        
        if (this.isFirstVessel) {
            mainScript.runSection("empty2FirstVessel");
        } else {
            mainScript.runSection("empty2NotFirstVessel");
        }

        if (this.hasBlade) {
            mainScript.runSection("endBlade");

            if (this.isFirstVessel) {
                mainScript.runSection("endBladeFirstVessel");
            } else {
                mainScript.runSection("endBladeNotFirstVessel");
            }

            return ChapterEnding.MUTUALLYASSURED;
        } else {
            mainScript.runSection("endBlade");
            
            if (this.isFirstVessel) {
                mainScript.runSection("endNoBladeFirstVessel");
            } else {
                mainScript.runSection("endNoBladeNotFirstVessel");
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

        mainScript.runSection("cabinIntro");

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
                    if (this.ch2AskMirror()) {
                        activeMenu.setCondition("approach", false);
                    }
                    break;

                case "different":
                    mainScript.runSection("cabinDifferent");
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
                    mainScript.runSection("takeBlade");
                    break;

                case "cGoStairs":
                case "enter":
                    this.repeatActiveMenu = false;
                    break;

                default:
                    this.giveDefaultFailResponse(activeOutcome);
            }
        }

        if (!this.hasBlade) mainScript.runSection("stairsNoBlade");

        this.currentLocation = GameLocation.BASEMENT;
        this.withBlade = false;
        this.mirrorPresent = false;
        mainScript.runSection("stairsStart");

        if (this.hasBlade) {
            mainScript.runSection("basementStartBlade");
        } else {
            mainScript.runSection("basementStartNoBlade");
        }

        if (manager.trueDemoMode()) return ChapterEnding.DEMOENDING;



        
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

        mainScript.runSection("cabinIntro");

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
                    if (this.ch2AskMirror()) {
                        activeMenu.setCondition("approach", false);
                    }
                    break;

                case "different":
                    secondaryScript.runSection("cabinDifferent");
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
                    mainScript.runSection("takeBlade");
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
                mainScript.runSection("stairsNoBladeSharedLoop");
            } else {
                mainScript.runSection("stairsNoBlade");
            }
        }

        this.currentLocation = GameLocation.STAIRS;
        this.withBlade = false;
        this.mirrorPresent = false;
        mainScript.runSection("stairsStart");

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
                    mainScript.runSection("stairsNotNasty");
                    break;

                case "hello":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("stairsHello");
                    break;

                case "cGoBasement":
                case "silent":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("stairsSilent");
                    break;
                
                case "cGoCabin":
                    secondaryScript.runSection("stairsLeaveFail");
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }
        
        this.currentLocation = GameLocation.BASEMENT;
        this.withPrincess = true;

        if (this.sharedLoopInsist) {
            mainScript.runSection("stairsEndSharedLoop");
        } else {
            mainScript.runSection("stairsEndNoShare");
        }

        if (this.hasBlade) {
            mainScript.runSection("basementStartBlade");
        } else {
            mainScript.runSection("basementStartNoBlade");
        }

        if (manager.trueDemoMode()) return ChapterEnding.DEMOENDING;
        
        
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

        secondaryScript.runSection();
        mainScript.runSection();

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
                case "happened":
                case "no":
                case "wise":
                case "died":
                    activeMenu.setCondition("assume", true);
                    activeMenu.setCondition("dejaVu", false);
                    activeMenu.setCondition("happened", false);
                    activeMenu.setCondition("no", false);
                    activeMenu.setCondition("notKidding", false);
                    activeMenu.setCondition("wise", false);
                    activeMenu.setCondition("died", false);
                    activeMenu.setCondition("proceed", false);
                    
                    secondaryScript.runSection(activeOutcome);
                    mainScript.runSection("shareLoop");
                    break;

                case "notKidding":
                    activeMenu.setCondition("assume", true);
                    activeMenu.setCondition("dejaVu", false);
                    activeMenu.setCondition("happened", false);
                    activeMenu.setCondition("no", false);
                    activeMenu.setCondition("wise", false);
                    activeMenu.setCondition("died", false);
                    activeMenu.setCondition("proceed", false);

                    mainScript.runSection("notKidding");
                    break;

                case "walls":
                    mainScript.runSection("walls");
                    
                    if (!this.sharedLoop) {
                        activeMenu.setCondition("assume", true);
                        activeMenu.setCondition("dejaVu", false);
                        activeMenu.setCondition("happened", false);
                        activeMenu.setCondition("no", false);
                        activeMenu.setCondition("notKidding", false);
                        activeMenu.setCondition("wise", false);
                        activeMenu.setCondition("died", false);
                        activeMenu.setCondition("proceed", false);

                        mainScript.runSection();
                    }

                    break;

                case "assume":
                    this.sharedLoopInsist = true;
                    mainScript.runSection("assume");
                    break;

                case "lie":
                    mainScript.runSection("lie");
                case "cGoHill":
                case "cabin":
                case "proceed":
                    this.repeatActiveMenu = false;
                    break;

                case "cGoLeave":
                    mainScript.runSection("leaveWoodsAttempt");
                    break;

                case "abort":
                    if (manager.nClaimedVessels() >= 2) {
                        this.canTryAbort = false;
                        activeMenu.setGreyedOut("abort", true);
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }

                    mainScript.runSection("abort");

                    this.abortVessel(true);
                    return ChapterEnding.ABORTED;

                default:
                    this.giveDefaultFailResponse(activeOutcome);
            }
        }
        
        this.currentLocation = GameLocation.HILL;
        mainScript.runSection("hillDialogue");

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
                    mainScript.runSection("leaveWoodsAttempt");
                    
                default:
                    this.giveDefaultFailResponse(this.activeOutcome);
            }
        }

        this.currentLocation = GameLocation.CABIN;
        this.mirrorPresent = true;
        this.knowsBlade = true;
        this.withBlade = true;

        mainScript.runSection("cabinIntro");

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "mirror", "(Explore) You didn't say anything about the mirror on the wall."));
        activeMenu.add(new Option(this.manager, "approach", "(Explore) [Approach the mirror.]"));
        activeMenu.add(new Option(this.manager, "take", "(Explore) [Take the blade.]"));
        activeMenu.add(new Option(this.manager, "throw", "(Explore) [Throw the blade out the window.]", false));
        activeMenu.add(new Option(this.manager, "enter", "[Enter the basement.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "mirror":
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
                    mainScript.runSection("takeBlade");

                    OptionsMenu subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "keep", "[Keep your grip as it is.]"));
                    subMenu.add(new Option(this.manager, "reverse", "[Hold the blade the other way.]"));

                    switch (parser.promptOptionsMenu(subMenu)) {
                        case "keep":
                            mainScript.runSection("keepGrip");
                            break;

                        case "reverse":
                            this.bladeReverse = true;
                            mainScript.runSection("reverseGrip");
                            break;
                    }

                    break;

                case "cThrow":
                    activeMenu.setCondition("throw", false);
                case "throw":
                    this.hasBlade = false;
                    this.canThrowBlade = false;
                    this.threwBlade = true;
                    mainScript.runSection("throwBlade");

                    if (this.sharedLoop) {
                        mainScript.runSection("throwSharedLoop");
                    } else {
                        mainScript.runSection("throwNoShare");
                    }

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
        mainScript.runSection("stairsStart");
        
        if (this.hasBlade) {
            mainScript.runSection("stairsStartJoin");
        } else {
            mainScript.runSection("stairsStartNoBlade");
        }

        if (manager.trueDemoMode()) return ChapterEnding.DEMOENDING;

        int schismCount = 1;
        String firstSchism = "";

        HashMap<String, Boolean> schismsPresent = new HashMap<>();
        schismsPresent.put("harsh", false);
        schismsPresent.put("neutral", false);
        schismsPresent.put("gentle", false);
        schismsPresent.put("emo", false);
        schismsPresent.put("monster", false);

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
                    break;
                    
                case "center":
                    this.repeatActiveMenu = false;
                    firstSchism = "neutral";
                    break;
                    
                case "right":
                    this.repeatActiveMenu = false;
                    firstSchism = "gentle";
                    break;

                case "cGoBasement":
                    mainScript.runSection("stairsNonSpecific");
                    break;

                case "cGoCabin":
                    mainScript.runSection("stairsLeaveAttempt");
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        mainScript.runSection(firstSchism + "Stairs");

        this.mainScript = new Script(this.manager, this.parser, "Routes/Stranger/StrangerBasement");
        this.currentLocation = GameLocation.BASEMENT;
        this.withPrincess = true;

        switch (firstSchism) {
            case "harsh":
                mainScript.runSection("basementStartHarsh");
                break;

            case "neutral":
                mainScript.runSection("basementStartNeutral");
                break;

            case "gentle":
                mainScript.runSection("basementStartGentle");
                break;
        }

        String endChoiceText = "\"I'm getting you out of here.\" [Try and free her.]\n  (NUM) \"I don't know what you are, but I can't trust you. I can't trust anyone here.\" [Leave her in the basement.]\n  (NUM) ";
        if (this.threwBlade) {
            endChoiceText += "[Regretfully think about that time you threw the blade out the window.]";
        } else if (!this.hasBlade) {
            endChoiceText += "[Retrieve the blade.]";
        } else {
            endChoiceText += "[Slay the Princess.]";
        }

        String setNewSchism = "";
        boolean newSchismComment = false;
        boolean schismThisOption;
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

            if (!setNewSchism.equals("")) {
                schismsPresent.put(setNewSchism, true);
                setNewSchism = "";
            }

            if (!newSchismComment && schismCount > 1) {
                newSchismComment = true;

                switch (schismCount) {
                    case 2:
                        mainScript.runSection("schism2Comment");

                        if (this.sharedLoop) {
                            if (this.sharedLoopInsist) {
                                mainScript.runSection("schism2CommentSharedLoopInsist");
                            } else {
                                mainScript.runSection("schism2CommentSharedLoop");
                            }
                        } else {
                            mainScript.runSection("schism2CommentNoShare");
                        }
                        
                        break;

                    case 3:
                    case 4:
                    case 5:
                        mainScript.runSection("schism" + schismCount + "Comment");
                        break;
                }
            }

            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "sorry":
                    mainScript.runSection(firstSchism + "SorryOpening");

                    // New schism; harsh if you have the blade, gentle if not, neutral if you already have that princess
                    newSchismComment = false;
                    schismCount += 1;
                    if (this.hasBlade && !firstSchism.equals("harsh")) {
                        schismsPresent.put("harsh", true);
                    } else if (!this.hasBlade && !firstSchism.equals("gentle")) {
                        schismsPresent.put("gentle", true);
                    } else {
                        schismsPresent.put("neutral", true);
                    }

                    this.strangerRunSchismSection(schismsPresent, "Sorry", "gentle", "neutral", "harsh");
                    break;

                case "more":
                    mainScript.runSection(firstSchism + "More");
                    this.strangerRunSchismSection(schismsPresent, "More", "emo", "monster");

                    // Attempt new schism: attempt neutral, then attempt harsh, then attempt gentle; fails if you have all 3 already
                    if (!schismsPresent.get("neutral") && !firstSchism.equals("neutral")) {
                        newSchismComment = false;
                        schismThisOption = true;
                        schismsPresent.put("neutral", true);
                        schismCount += 1;

                        this.strangerRunSchismSection("More", "neutral");
                    } else if (!schismsPresent.get("harsh") && !firstSchism.equals("harsh")) {
                        newSchismComment = false;
                        schismThisOption = true;
                        schismsPresent.put("harsh", true);
                        schismCount += 1;
                        
                        this.strangerRunSchismSection("More", "harsh");
                    } else if (!schismsPresent.get("gentle") && !firstSchism.equals("gentle")) {
                        newSchismComment = false;
                        schismThisOption = true;
                        schismsPresent.put("gentle", true);
                        schismCount += 1;

                        this.strangerRunSchismSection("More", "gentle");
                    }

                    if (schismThisOption) {
                        mainScript.runSection("moreSchism");
                    } else {
                        this.strangerRunSchismSection("More", "neutral", "harsh", "gentle");
                        mainScript.runSection("moreNoSchism");
                    }

                    if (this.hasBlade) {
                        mainScript.runSection("moreBlade");
                    } else if (this.threwBlade) {
                        mainScript.runSection("moreThrewBlade");
                    } else {
                        mainScript.runSection("moreNoBlade");
                    }

                    break;

                case "name":
                    mainScript.runSection(firstSchism + "Name");

                    // Attempt new schism: harsh if you have the blade, gentle if not, neutral if you already have that princess; fails if you have all 3 already
                    if (this.hasBlade && !firstSchism.equals("harsh")) {
                        newSchismComment = false;
                        schismCount += 1;
                        schismThisOption = true;
                        schismsPresent.put("harsh", true);
                    } else if (!this.hasBlade && !firstSchism.equals("gentle")) {
                        newSchismComment = false;
                        schismCount += 1;
                        schismThisOption = true;
                        schismsPresent.put("gentle", true);
                    } else if (!schismsPresent.get("neutral") && !firstSchism.equals("neutral")) {
                        newSchismComment = false;
                        schismCount += 1;
                        schismThisOption = true;
                        schismsPresent.put("neutral", true);
                    }

                    if (schismThisOption) mainScript.runSection("genericFracture");
                    this.strangerRunSchismSection(schismsPresent, "Name", "neutral", "harsh", "gentle", "emo", "monster");
                    if (schismCount != 2) mainScript.runSection("nameFollowUp");
                    break;

                case "weird":
                    mainScript.runSection(firstSchism + "FirstWeird");
                    mainScript.runSection("genericFracture");

                    // New schism: attempt emo, then attempt monster, then attempt gentle, then attempt harsh, then neutral
                    newSchismComment = false;
                    schismCount += 1;
                    if (!schismsPresent.get("emo")) {
                        setNewSchism = "emo";
                    } else if (!schismsPresent.get("monster")) {
                        setNewSchism = "monster";
                    } else if (!schismsPresent.get("gentle") && !firstSchism.equals("gentle")) {
                        setNewSchism = "gentle";
                    } else if (!schismsPresent.get("harsh") && !firstSchism.equals("harsh")) {
                        setNewSchism = "harsh";
                    } else {
                        setNewSchism = "neutral";
                    }
                    
                    this.strangerRunSchismSection("Weird", setNewSchism);
                    this.strangerRunSchismSection(schismsPresent, "Weird", "gentle", "neutral", "harsh", "emo", "monster");
                    
                    activeMenu.setCondition("whatDo", activeMenu.hasBeenPicked("reason") && schismsPresent.get("monster"));
                    break;

                case "reason":
                    activeMenu.setCondition("threatShare", false);

                    if (firstSchism.equals("neutral")) {
                        if (this.hasBlade) {
                            mainScript.runSection("neutralReasonBlade");
                        } else {
                            mainScript.runSection("neutralReasonNoBlade");
                        }
                    } else {
                        mainScript.runSection(firstSchism + "Reason");
                    }

                    // New schism: attempt emo, then attempt monster
                    if (!schismsPresent.get("emo")) {
                        newSchismComment = false;
                        schismCount += 1;
                        schismThisOption = true;
                        schismsPresent.put("emo", true);
                    } else if (!schismsPresent.get("monster")) {
                        newSchismComment = false;
                        schismCount += 1;
                        schismThisOption = true;
                        schismsPresent.put("monster", true);
                    }

                    if (schismThisOption) mainScript.runSection("genericFracture");
                    this.strangerRunSchismSection(schismsPresent, "Reason", "emo", "monster");
                    
                    activeMenu.setCondition("whatDo", schismsPresent.get("monster"));
                    break;

                case "threatShare":
                    activeMenu.setCondition("reason", false);
                    activeMenu.setCondition("whatDo", true);

                    mainScript.runSection(firstSchism + "ThreatShare");

                    // New schism: attempt monster, then attempt emo
                    if (!schismsPresent.get("monster")) {
                        newSchismComment = false;
                        schismCount += 1;
                        schismThisOption = true;
                        schismsPresent.put("monster", true);
                    } else if (!schismsPresent.get("emo")) {
                        newSchismComment = false;
                        schismCount += 1;
                        schismThisOption = true;
                        schismsPresent.put("emo", true);
                    }

                    if (schismThisOption) mainScript.runSection("genericFracture");
                    this.strangerRunSchismSection(schismsPresent, "ThreatShare", "emo", "monster");
                    break;

                case "whatDo":
                    mainScript.runSection(firstSchism + "FirstWhatDo");

                    // New schism attempt: attempt harsh, then attempt neutral, then attempt gentle
                    if (!schismsPresent.get("harsh") && !firstSchism.equals("harsh")) {
                        newSchismComment = false;
                        schismCount += 1;
                        schismThisOption = true;
                        setNewSchism = "harsh";
                    } else if (!schismsPresent.get("neutral") && !firstSchism.equals("neutral")) {
                        newSchismComment = false;
                        schismCount += 1;
                        schismThisOption = true;
                        setNewSchism = "neutral";
                    } else if (!schismsPresent.get("gentle") && !firstSchism.equals("gentle")) {
                        newSchismComment = false;
                        schismCount += 1;
                        schismThisOption = true;
                        setNewSchism = "gentle";
                    }

                    if (schismThisOption) {
                        mainScript.runSection("genericFracture");
                        this.strangerRunSchismSection("WhatDoA", setNewSchism);
                    } else {
                        this.strangerRunSchismSection(schismsPresent, "WhatDoA", "neutral", "harsh", "gentle");
                    }
                    
                    this.strangerRunSchismSection(schismsPresent, "Name", "gentle", "neutral", "harsh", "monster", "emo");

                    if (schismsPresent.get("monster")) {
                        mainScript.runSection("whatDoFollowUpMonster");
                    } else {
                        mainScript.runSection("whatDoFollowUpNoMonster");
                    }

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
        mainScript.runSection("endingStart");

        if (this.hasBlade) {
            mainScript.runSection("endingBlade");
        } else {
            mainScript.runSection("endingNoBlade");
        }

        if (this.isFirstVessel) {
            mainScript.runSection("endingFirstVessel");
        } else {
            mainScript.runSection("endingNotFirstVessel");
        }

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "okay", "\"It's going to be okay...\""));
        activeMenu.add(new Option(this.manager, "best", "\"I'll do my best.\""));
        activeMenu.add(new Option(this.manager, "supposed", "\"I don't think you're supposed to be fixed.\""));
        activeMenu.add(new Option(this.manager, "no", "\"No.\""));
        activeMenu.add(new Option(this.manager, "destroyed", "\"You just destroyed everything. I'm not going to fix you.\""));
        activeMenu.add(new Option(this.manager, "silent", "[Say nothing.]"));

        if (parser.promptOptionsMenu(activeMenu).equals("silent")) {
            if (this.isFirstVessel) {
                mainScript.runSection("endSilentFirstVessel");
            } else {
                mainScript.runSection("endSilentNotFirstVessel");
            }
        } else {
            if (this.isFirstVessel) {
                mainScript.runSection("endReplyFirstVessel");
            } else {
                mainScript.runSection("endReplyNotFirstVessel");
            }
        }
            
        return ChapterEnding.ILLUSIONOFCHOICE;
    }

    /**
     * Used during Chapter II: The Stranger; for a given ordering of schisms, checks whether a given schism of the Princess is present, then runs the corresponding section in the script
     * @param schismsPresent Which versions of the Princess are currently present
     * @param sectionID The last part of each jump anchor in the script which corresponds to this response
     * @param schisms The names of the schisms that are currently speaking (harsh, neutral, gentle, emo, or monster), in order
     */
    private void strangerRunSchismSection(HashMap<String, Boolean> schismsPresent, String sectionID, String... schisms) {
        for (String schism : schisms) {
            // Convert shorthands
            switch (schism) {
                case "h":
                    schism = "harsh";
                    break;

                case "n":
                    schism = "neutral";
                    break;

                case "g":
                    schism = "gentle";
                    break;

                case "e":
                    schism = "emo";
                    break;

                case "m":
                    schism = "monster";
                    break;
            }

            if (schismsPresent.get(schism)) {
                mainScript.runSection(schism + sectionID);
            }
        }
    }

    /**
     * Used during Chapter II: The Stranger; for a given ordering of schisms of the Princess, runs the corresponding section in the script
     * @param sectionID The last part of each jump anchor in the script which corresponds to this response
     * @param schisms The names of the schisms that are currently speaking (harsh, neutral, gentle, emo, or monster), in order
     */
    private void strangerRunSchismSection(String sectionID, String... schisms) {
        for (String schism : schisms) {
            // Convert shorthands
            switch (schism) {
                case "h":
                    schism = "harsh";
                    break;

                case "n":
                    schism = "neutral";
                    break;

                case "g":
                    schism = "gentle";
                    break;

                case "e":
                    schism = "emo";
                    break;

                case "m":
                    schism = "monster";
                    break;
            }

            mainScript.runSection(schism + sectionID);
        }
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

        mainScript.runSection("cabinIntro");

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
                    if (this.ch2AskMirror()) {
                        activeMenu.setCondition("approach", false);
                    }
                    break;

                case "different":
                    mainScript.runSection("cabinDifferent");
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
                    mainScript.runSection("takeBlade");
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
                mainScript.runSection("stairsNoBladeSharedLoop");
            } else {
                mainScript.runSection("stairsNoBlade");
            }

            this.activeMenu = new OptionsMenu();
            activeMenu.add(new Option(this.manager, "hey", "Hey! Don't I get a say here? What's the big idea?"));
            activeMenu.add(new Option(this.manager, "take", "[Take the blade.]"));

            this.repeatActiveMenu = true;
            while (repeatActiveMenu) {
                switch (parser.promptOptionsMenu(activeMenu)) {
                    case "hey":
                        this.repeatActiveMenu = false;
                        mainScript.runSection("stairsTakeBladeProtest");
                        break;

                    case "cTake":
                    case "take":
                        this.repeatActiveMenu = false;
                        mainScript.runSection("stairsTakeBlade");
                        break;

                    default: this.giveDefaultFailResponse();
                }
            }

            this.hasBlade = true;
        }

        this.currentLocation = GameLocation.BASEMENT;
        this.withPrincess = true;
        this.withBlade = false;
        this.mirrorPresent = false;
        mainScript.runSection("stairsStart");

        if (this.sharedLoopInsist) {
            mainScript.runSection("stairsSharedLoop");
            mainScript.runSection("basementStartSharedLoop");
        } else if (this.sharedLoop) {
            mainScript.runSection("stairsSharedLoop");
            mainScript.runSection("basementStartNoShare");
        } else {
            mainScript.runSection("stairsNoShare");
            mainScript.runSection("basementStartNoShare");
        }

        if (manager.trueDemoMode()) return ChapterEnding.DEMOENDING;


        
        
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

        mainScript.runSection("cabinIntro");

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
                    if (this.ch2AskMirror()) {
                        activeMenu.setCondition("approach", false);
                    }
                    break;

                case "different":
                    mainScript.runSection("cabinDifferent");
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

                    if (this.sharedLoop) {
                        mainScript.runSection("takeBladeSharedLoop");
                    } else {
                        mainScript.runSection("takeBlade");
                    }

                    break;

                case "cGoStairs":
                case "enter":
                    this.repeatActiveMenu = false;
                    break;

                default:
                    this.giveDefaultFailResponse(activeOutcome);
            }
        }

        this.currentLocation = GameLocation.BASEMENT;
        this.withPrincess = true;
        this.withBlade = false;
        this.mirrorPresent = false;
        mainScript.runSection("stairsStart");

        if (this.sharedLoop) {
            mainScript.runSection("stairsSharedLoop");
        } else {
            mainScript.runSection("stairsNoShare");
        }

        if (manager.trueDemoMode()) return ChapterEnding.DEMOENDING;


        
        
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
        this.secondaryScript = new Script(this.manager, this.parser, "Mirror/MirrorGeneric");

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
                    mainScript.runSection("surviveMirrorCold");
                } else {
                    mainScript.runSection("surviveMirrorBroken");
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

                mainScript.runSection("surviveMirrorApproach");
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

                        default: super.giveDefaultFailResponse();
                    }
                }

                if (this.isFirstVessel) {
                    mainScript.runSection("mirrorFirstVessel");
                } else {
                    mainScript.runSection("mirrorNotFirstVessel");
                }

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

                        default: super.giveDefaultFailResponse();
                    }
                }

                break;

            default:
                if (this.prevEnding != ChapterEnding.GRACE) secondaryScript.runSection();

                if (this.mirrorComment || this.touchedMirror) {
                    secondaryScript.runSection("mirrorCommented");
                } else {
                    secondaryScript.runSection("mirrorNotCommented");
                }

                boolean explore = false;
                boolean silence = false;
                this.activeMenu = new OptionsMenu();
                if (this.isFirstVessel) {
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
                                secondaryScript.runSection("where");

                                if (contraAsk) {
                                    secondaryScript.runSection("contraAskB");
                                } else {
                                    contraAsk = true;
                                    secondaryScript.runSection("contraAskA");
                                }

                                break;

                            case "gone":
                                secondaryScript.runSection("gone");
                                
                                if (contraAsk) {
                                    secondaryScript.runSection("contraAskB");
                                } else {
                                    contraAsk = true;
                                    secondaryScript.runSection("contraAskA");
                                }

                                secondaryScript.runSection("goneJoin");
                                break;

                            case "suggest":
                                explore = true;
                                secondaryScript.runSection("suggest");

                                if (this.nVoices() == 2) {
                                    secondaryScript.runSection("explore2Voices");
                                } else {
                                    secondaryScript.runSection("exploreMoreVoices");
                                }

                                break;

                            case "cApproachMirror":
                            case "approach":
                                secondaryScript.runSection("approachFirstVessel");

                                this.activeMenu = new OptionsMenu();
                                activeMenu.add(new Option(this.manager, "explore", "(Explore) \"The mirror never scared you before.\"", this.mirrorComment || this.touchedMirror));
                                activeMenu.add(new Option(this.manager, "ignore", "[Ignore him.]"));
                                
                                while (repeatActiveMenu) {
                                    switch (parser.promptOptionsMenu(activeMenu)) {
                                        case "explore":
                                            secondaryScript.runSection();

                                            if (!explore) {
                                                explore = true;
                                                secondaryScript.runSection("exploreJoin");

                                                if (this.nVoices() == 2) {
                                                    secondaryScript.runSection("explore2Voices");
                                                } else {
                                                    secondaryScript.runSection("exploreMoreVoices");
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
                                manager.incrementCruelCount();
                                secondaryScript.runSection("cruel");
                                break;

                            case "comfortA":
                            case "comfortB":
                                activeMenu.setCondition("cruel", false);
                                explore = true;
                                secondaryScript.runSection("explore");

                                if (this.nVoices() == 2) {
                                    secondaryScript.runSection("explore2Voices");
                                } else {
                                    secondaryScript.runSection("exploreMoreVoices");
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
                                            secondaryScript.runSection("comfort");
                                            break;
                                        
                                        case "cruel":
                                            repeatSub = false;
                                            manager.incrementCruelCount();
                                            secondaryScript.runSection("cruel");
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
            secondaryScript.runSection("approach");
            if (silence) secondaryScript.runSection("approachSilence");
            
            if (!explore) {
                secondaryScript.runSection("approachExplore");

                if (this.nVoices() == 2) {
                    secondaryScript.runSection("explore2Voices");
                } else {
                    secondaryScript.runSection("exploreMoreVoices");
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
            mainScript.runSection("mirrorGaze");
        } else if (this.isFirstVessel || (manager.nClaimedVessels() == 1 && manager.hasClaimedAnyVessel(Vessel.RAZORFULL, Vessel.RAZORHEART))) {
            secondaryScript.runSection("gazeFirst");
        } else {
            secondaryScript.runSection("gazeAgain");
        }

        System.out.println();
        switch (manager.nClaimedVessels()) {
            case 0:
            case 1:
            case 2:
            case 3:
                secondaryScript.runSection("gaze" + manager.nClaimedVessels());
                this.theSpacesBetween();
                break;

            case 4:
                secondaryScript.runSection("gazeFinal");

                this.activeMenu = new OptionsMenu(true);
                activeMenu.add(new Option(this.manager, "ask", "\"Are you me?\""));
                parser.promptOptionsMenu(activeMenu);
                
                secondaryScript.runSection("gazeFinalCont");

                // Leads into Finale.finalMirror()
                break;
        }
    }

    /**
     * Runs the encounter with the Shifting Mound after claiming each vessel, excluding the fifth and final vessel
     */
    private void theSpacesBetween() {
        this.mainScript = new Script(this.manager, this.parser, Chapter.SPACESBETWEEN.getScriptFile());

        this.currentLocation = GameLocation.PATH;

        if (this.isFirstVessel) {
            mainScript.runSection("firstVesselIntro");
        } else {
            mainScript.runSection();
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
                    mainScript.runSection("nowhereToGo");
                    break;
                    
                default: this.giveDefaultFailResponse(this.activeOutcome);
            }
        }

        this.currentLocation = GameLocation.HILL;
        if (this.isFirstVessel) {
            secondaryScript.runSection("cabinFirst");
        } else {
            secondaryScript.runSection("cabin");
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
                    mainScript.runSection("nowhereToGo");
                    break;
                    
                default: this.giveDefaultFailResponse(this.activeOutcome);
            }
        }

        this.canApproachHer = false;
        this.withPrincess = true;

        switch (manager.nClaimedVessels()) { // Conversation
            case 0:
                this.canApproachHer = false;
                this.shiftingMoundTalk1();
                break;

            case 1:
                this.canApproachHer = false;
                this.shiftingMoundTalk2();
                break;

            case 2:
                this.canApproachHer = false;
                this.shiftingMoundTalk3();
                break;

            case 3:
                this.canApproachHer = false;
                this.shiftingMoundTalk4();
                break;
        }
    }

    /**
     * Runs the conversation with the Shifting Mound after claiming the first vessel
     */
    private void shiftingMoundTalk1() {
        this.secondaryScript = new Script(this.manager, this.parser, "Intermission/IntermissionTalk1");

        if (manager.nVesselsAborted() == 0) {
            secondaryScript.runSection();
        } else {
            secondaryScript.runSection("introNoAbort");
        }

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
                case "end":
                case "letOut":
                case "trapped":
                case "worlds":
                case "familiar":
                    secondaryScript.runSection(activeOutcome);
                    break;

                case "what":
                    secondaryScript.runSection("what");

                    subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "explore", "(Explore) \"Solitary lights? What do you mean?\""));
                    subMenu.add(new Option(this.manager, "youThink", "\"What do you think I am?\""));
                    subMenu.add(new Option(this.manager, "dunno", "\"I don't know what I am.\""));
                    subMenu.add(new Option(this.manager, "person", "\"I'm a person.\""));

                    repeatSub = true;
                    while (repeatSub) {
                        switch (parser.promptOptionsMenu(subMenu)) {
                            case "explore":
                                secondaryScript.runSection("whatExplore");
                                break;

                            case "youThink":
                            case "dunno":
                                repeatSub = false;
                                secondaryScript.runSection("whatYouThink");
                                break;

                            case "person":
                                repeatSub = false;
                                secondaryScript.runSection("whatPerson");
                                break;
                        }
                    }

                    break;

                case "fragile":
                    secondaryScript.runSection("fragile");
                    this.giveVesselThoughts(prevEnding.getVessel());
                    break;

                case "narrator":
                    if (manager.hasClaimedAnyVessel(Vessel.WOUNDEDWILD, Vessel.NETWORKWILD, Vessel.SPECTRE, Vessel.WRAITH, Vessel.TOWER, Vessel.APOTHEOSIS)) {
                        secondaryScript.runSection("narratorMet");
                    } else {
                        secondaryScript.runSection("narrator");
                    }
                    
                    break;

                case "princess":
                    secondaryScript.runSection("princess");

                    subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "press", "\"But were you always the Princess, or are you just making her a part of yourself?\""));
                    subMenu.add(new Option(this.manager, "silent", "[Say nothing.]"));

                    if (parser.promptOptionsMenu(subMenu).equals("press")) secondaryScript.runSection();
                    break;

                case "whatNow":
                    this.repeatActiveMenu = false;
                    break;

                case "cSlayPrincessNoBladeFail": // Override: you don't need the blade
                    if (manager.getIntermissionAttackMound().hasBeenPicked()) {
                        mainScript.runSection("alreadyTried");
                        break;
                    }
                case "attackMound":
                    mainScript.runSection("attackMound");
                    break;

                case "cSlaySelfNoBladeFail": // Override: you don't need the blade
                    if (manager.getIntermissionAttackSelf().hasBeenPicked()) {
                        mainScript.runSection("alreadyTried");
                        break;
                    }
                case "attackSelf":
                    mainScript.runSection("attackSelf");
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // "What happens now?" continues here
        secondaryScript.runSection("whatNow");

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
                case "refuse":
                    manager.refuseExploreMound();
                case "kill":
                case "howMuch":
                case "pieces":
                    secondaryScript.runSection(activeOutcome);
                    break;

                case "destroy":
                    secondaryScript.runSection("destroy");

                    subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "press", "\"You're being semantic. What are you going to do if I help you?\""));
                    subMenu.add(new Option(this.manager, "silent", "[Let it be.]"));

                    if (parser.promptOptionsMenu(subMenu).equals("press")) secondaryScript.runSection();
                    break;

                case "wait":
                    secondaryScript.runSection("wait");
                    
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
        secondaryScript.runSection("forget");

        int waitTime = (manager.globalSlowPrint()) ? 500 : 1000;
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            throw new RuntimeException("Thread interrupted");
        }

        mainScript.runSection("forget");
    }

    /**
     * Runs the conversation with the Shifting Mound after claiming the second vessel
     */
    private void shiftingMoundTalk2() {
        this.secondaryScript = new Script(this.manager, this.parser, "Intermission/IntermissionTalk2");



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
        this.secondaryScript = new Script(this.manager, this.parser, "Intermission/IntermissionTalk3");



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
        this.secondaryScript = new Script(this.manager, this.parser, "Intermission/IntermissionTalk4");



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
                mainScript.runSection("adversary");
                break;
            case TOWER:
                mainScript.runSection("tower");
                break;
            case SPECTRE:
                mainScript.runSection("spectre");
                break;
            case NIGHTMARE:
                mainScript.runSection("nightmare");
                break;
            case BEAST:
                mainScript.runSection("beast");
                break;
            case WITCH:
                mainScript.runSection("witch");
                break;
            case STRANGER:
                if (this.isFirstVessel) {
                    mainScript.runSection("stranger");
                } else {
                    mainScript.runSection("strangerFirst");
                }
                break;
            case PRISONERHEAD:
                mainScript.runSection("prisonerHead");
                break;
            case PRISONER:
                mainScript.runSection("prisoner");
                break;
            case DAMSEL:
                mainScript.runSection("damsel");
                break;
            case DECONDAMSEL:
                mainScript.runSection("deconDamsel");
                break;

            // Chapter III or IV
            case NEEDLE:
                mainScript.runSection("needle");
                break;
            case FURY:
            case REWOUNDFURY:
                mainScript.runSection("fury");
                break;
            case APOTHEOSIS:
                mainScript.runSection("apotheosis");
                break;
            case PATD:
            case STENCILPATD:
                mainScript.runSection("dragon");
                break;
            case WRAITH:
                mainScript.runSection("wraith");
                break;
            case CLARITY:
                mainScript.runSection("clarity");
                break;
            case RAZORFULL:
            case RAZORHEART:
                mainScript.runSection("razor");
                break;
            case DEN:
                mainScript.runSection("den");
                // might need another Vessel for den for an extra line??
                break;
            case NETWORKWILD:
                mainScript.runSection("nWild");
                break;
            case WOUNDEDWILD:
                mainScript.runSection("wWild");
                break;
            case THORN:
                mainScript.runSection("thorn");
                break;
            case WATCHFULCAGE:
                mainScript.runSection("cageWatchful");
                break;
            case OPENCAGE:
                mainScript.runSection("cageOpen");
                break;
            case DROWNEDGREY:
                mainScript.runSection("dGrey");
                break;
            case BURNEDGREY:
                mainScript.runSection("bGrey");
                break;
            case HAPPY:
            case HAPPYDRY:
                mainScript.runSection("happy");
                break;
        }
    }


    
    /**
     * The player aborts the current Chapter (and therefore the current Cycle as well), contributing to the Oblivion ending
     */
    private void abortVessel(boolean lateJoin) {
        this.secondaryScript = new Script(this.manager, this.parser, "Intermission/AbortVessel");

        if (!lateJoin) {
            if (this.activeChapter.getNumber() > 2) {
                secondaryScript.runSection("narratorCh3");
            } else {
                secondaryScript.runSection();
            }

            this.currentVoices.put(Voice.NARRATOR, false);
            secondaryScript.runSection("voices");
        }

        this.clearVoices();

        if (!this.isFirstVessel) {
            secondaryScript.runSection("unwoundVesselsClaimed");
        } else if (manager.nVesselsAborted() > 0) {
            secondaryScript.runSection("unwoundAgain");
        } else {
            secondaryScript.runSection("unwoundFirst");
        }
        
        switch (manager.nVesselsAborted()) {
            case 0:
                if (this.isFirstVessel) {
                    secondaryScript.runSection("firstAbort");

                    this.activeMenu = new OptionsMenu(true);
                    activeMenu.add(new Option(this.manager, "wake", "This is a nightmare. Wake up."));
                    activeMenu.add(new Option(this.manager, "embrace", "Embrace the thoughts constricting you."));

                    switch (parser.promptOptionsMenu(activeMenu)) {
                        case "wake":
                            secondaryScript.runSection("firstAbortDream");
                            break;

                        case "embrace":
                            secondaryScript.runSection();
                            break;
                    }
                } else {
                    secondaryScript.runSection("firstAbortVesselsClaimed");
                }

                break;


            case 1:
                secondaryScript.runSection("secondAbort");
                break;


            case 2:
                secondaryScript.runSection("thirdAbort");

                if (this.isFirstVessel) {
                    secondaryScript.runSection("thirdAbortCont");
                } else {
                    secondaryScript.runSection("thirdAbortVesselsClaimed");
                }

                break;


            case 3:
                secondaryScript.runSection("fourthAbort");
                break;


            case 4:
                secondaryScript.runSection("fifthAbort");
                break;


            case 5: // Oblivion ending
                manager.addToPlaylist("Oblivion");
                secondaryScript.runSection("oblivion");

                this.activeMenu = new OptionsMenu(true);
                activeMenu.add(new Option(this.manager, "exist", "[Exist.]", 0));
                activeMenu.add(new Option(this.manager, "fade", "[Consciousness fades away.]"));

                for (int i = 0; i < 4; i++) {
                    secondaryScript.runSection("oblivionLoop");

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
            secondaryScript.runSection("endLoop");
        }
    }

}
