import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.AbstractAction;

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
    private boolean canUnique3 = true; // Used in all Chapter 2s
    private boolean canJoint3 = true; // Used in all Chapter 2s
    private String source = "";
    private boolean sharedLoop = false; // Used in all Chapter 2s and 3s: does the Narrator know?
    private boolean sharedLoopInsist = false; // Used in all Chapter 2s
    private boolean forestSpecial = false; // Used in all Chapter 2s
    private boolean skipHillDialogue = false; // Used in Chapter 1 and all Chapter 2s; if you backed out of Stranger / aborting the Chapter

    // Variables that persist between chapters
    private boolean mirrorComment = false; // Used in all Chapter 2s and 3s
    private boolean touchedMirror = false; // Used in all Chapter 2s and 3s
    private boolean knowsDestiny = false; // Used in Chapter 1, Adversary, Tower, Fury
    private boolean droppedBlade1 = false; // Used in Chapter 1, Adversary
    private boolean whatWouldYouDo = false; // Used in Chapter 1, Damsel
    private boolean rescuePath = false; // Used in Chapter 1, Witch

    private static final PrincessDialogueLine CANTSTRAY = new PrincessDialogueLine(true, "You have already committed to my completion. You cannot go further astray.");
    private static final PrincessDialogueLine WORNPATH = new PrincessDialogueLine(true, "This path is already worn by travel and has been seen by one of my many eyes. You cannot walk it again. Change your course.");
    private static final VoiceDialogueLine WORNPATHHERO = new VoiceDialogueLine(Voice.HERO, "Wait... what?!");
    private static final PrincessDialogueLine DEMOBLOCK = new PrincessDialogueLine(true, "That path is not available to you.");

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
            switch (v) {
                case NARRATOR:
                case HERO: this.currentVoices.put(v, true);

                default: this.currentVoices.put(v, false);
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

        switch (v) {
            case NARRATOR:
            case NARRATORPRINCESS:
            case PRINCESS:
            case HERO: break;

            default: this.voicesMet.add(v);
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

    /**
     * Prints a line about the Long Quiet beginning to creep closer, used in most endings right before a vessel is claimed
     */
    private void quietCreep() {
        System.out.println();
        if (this.isFirstVessel) {
            parser.printDialogueLine("A textured nothingness begins to creep into the edges of your vision.");
        } else {
            parser.printDialogueLine("A textured nothingness begins to creep into the edges of your vision. Somehow, it feels familiar.");
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

        switch (this.prevEnding) {
            case ABORTED: break;

            case DEMOENDING:
            case GOODENDING:
                manager.updateVoicesMet(this.voicesMet);
                break;

            default:
                this.mirrorSequence();
                manager.updateVoicesMet(this.voicesMet);
        }

        manager.updateVisitedChapters(this.route);
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
            case KNIVESOUTMASKSOFFGIVEUP:
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
                            
                            break;
                            
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

        this.rescuePath = true;
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

                    return ChapterEnding.TOWITCHBETRAYAL;

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
                    hesitated = true;
                    afraid = true;
                    canSlay = canTower;
                    activeMenu.setCondition("slay", canSlay);
                    
                    secondaryScript.runSection("steelBluff");
                    break;

                case "isArmed":
                    activeMenu.setCondition("bluff", false);
                    activeMenu.setCondition("sorry", false);
                    hesitated = true;
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

        this.droppedBlade1 = true;
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
                } else if (source.equals("normal")) {
                    tipsText = "(Explore) She killed me last time around. How can I make sure that doesn't happen again?";
                    howDangerText = "(Explore) She killed me by ripping me to pieces. Don't get me wrong, I hated it, but how can someone like that end the world?";
                    quoteText = "(Explore) To quote you from last time around, \"she's *just* a Princess.\" Why was she able to rip me apart with her bare hands?";
                } else {
                    tipsText = "(Explore) We killed each other last time around. How can I make sure that doesn't happen again?";
                    howDangerText = "(Explore) She killed me by ripping me to pieces. Don't get me wrong, I hated it, but how can someone like that end the world?";
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
                    this.forestSpecial = true;
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

        boolean differentComment = false;
        Condition talked = new Condition(false);
        InverseCondition noTalk = new InverseCondition(talked);
        Condition narratorProof = new Condition(false);
        InverseCondition noProof = new InverseCondition(narratorProof);
        Condition scaredComment = new Condition(false);
        Condition closerComment = new Condition(false);
        InverseCondition noCloserComment = new InverseCondition(closerComment);
        Condition freeOffer = new Condition(false);
        InverseCondition noFreeOffer = new InverseCondition(freeOffer);
        Condition adversaryFree = new Condition(false);
        Condition adversaryNotFree = new Condition(false);

        OptionsMenu subMenu;
        boolean repeatSub;

        this.canSlayPrincess = true;
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "proof", "(Explore) I hope you heard all of that, Mr. Narrator. This is a lot different than last time, but last time definitely *happened.*", this.sharedLoop, noProof));
        activeMenu.add(new Option(this.manager, "different", "(Explore) \"You look... different.\""));
        activeMenu.add(new Option(this.manager, "memory", "(Explore) \"So you *do* remember me!\""));
        activeMenu.add(new Option(this.manager, "chat", "(Explore) \"I actually am just here to chat.\"", !this.hasBlade));
        activeMenu.add(new Option(this.manager, "scared", "(Explore) \"I'm not scared of you.\"", scaredComment, noCloserComment));
        activeMenu.add(new Option(this.manager, "freeOffer", "(Explore) \"I'm actually here to free you.\""));
        activeMenu.add(new Option(this.manager, "unpack", "(Explore) \"I'm not saying I'm *not* here to fight, but I think the two of us have a few things to unpack first. Like how we're both still alive.\""));
        activeMenu.add(new Option(this.manager, "undecided", "(Explore) \"I haven't decided what I'm doing yet.\"", noCloserComment));
        activeMenu.add(new Option(this.manager, "banter", "(Explore) \"Don't worry, I'm always up for a good fight. In fact, the only reason I came down here without a weapon is because having a knife felt *unfair.*\"", !this.hasBlade));
        activeMenu.add(new Option(this.manager, "slay", "[Slay the Princess.]", this.hasBlade, noTalk));
        activeMenu.add(new Option(this.manager, "attack", "[Attack the Princess.]", this.hasBlade, talked));
        activeMenu.add(new Option(this.manager, "unarmedAttackA", manager.demoMode(), "\"Fine. Let's do this.\" [Attack her unarmed.]", 0, activeMenu.get("banter"), !this.hasBlade));
        activeMenu.add(new Option(this.manager, "unarmedAttackB", manager.demoMode(), "[Attack her unarmed.]", 0, !this.hasBlade));
        activeMenu.add(new Option(this.manager, "retrieve", "\"The blade's upstairs. I'll be right back.\" [Go upstairs and retrieve the blade.]", !this.hasBlade));
        activeMenu.add(new Option(this.manager, "closer", "[Step closer.]", closerComment));
        activeMenu.add(new Option(this.manager, "leaveYap", manager.demoMode(), "\"I don't know what happened to you since the last time we met, but I am *not* fighting a giant demon-lady. Bye!\" [Turn around and leave.]", 0));
        activeMenu.add(new Option(this.manager, "free", manager.demoMode(), "[Attempt to free the Princess.]", this.hasBlade, freeOffer));
        activeMenu.add(new Option(this.manager, "leaveSilent", manager.demoMode(), "[Turn around and leave without saying anything.]", 0));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);

            // Redirect "slay" command to the appropriate option
            if (activeOutcome.equals("cSlayPrincess")) {
                if (talked.check()) {
                    this.activeOutcome = "attack";
                } else {
                    this.activeOutcome = "slay";
                }
            }

            switch (activeOutcome) {
                case "proof":
                    narratorProof.set(true);
                    talked.set(true);

                    if (this.sharedLoopInsist) {
                        mainScript.runSection("proofDistantMenuInsist");
                    } else {
                        mainScript.runSection("proofDistantMenuNoInsist");
                    }
                    
                    break;
                    
                case "different":
                    differentComment = true;
                    talked.set(true);

                    if (this.hasBlade) {
                        mainScript.runSection("differentDistantMenuBlade");
                    } else {
                        mainScript.runSection("differentDistantMenuNoBlade");
                    }

                    this.adversaryNarratorProof(narratorProof);
                    break;
                    
                case "memory":
                    talked.set(true);

                    if (this.hasBlade) {
                        mainScript.runSection("memoryDistantMenuBlade");
                    } else {
                        mainScript.runSection("memoryDistantMenuNoBlade");
                        if (narratorProof.check()) mainScript.runSection();
                    }
                        
                    this.adversaryNarratorProof(narratorProof);

                    break;
                    
                case "chat":
                    talked.set(true);
                    scaredComment.set(true);

                    if (this.droppedBlade1) {
                        mainScript.runSection("chatDistantMenuAgain");
                    } else {
                        mainScript.runSection("chatDistantMenuFirst");
                    }

                    this.adversaryNarratorProof(narratorProof);

                    break;
                    
                case "scared":
                    closerComment.set(true);
                    mainScript.runSection("scaredDistantMenu");
                    break;
                    
                case "freeOffer":
                    talked.set(true);
                    mainScript.runSection("freeOfferDistantMenu");

                    subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "serious", "\"You don't want to be free? Are you serious?\""));
                    subMenu.add(new Option(this.manager, "want", "\"If you don't want to be free, then what do you want?\""));
                    subMenu.add(new Option(this.manager, "why", "\"Me? Anyone? Why wouldn't you want to be free?\""));
                    subMenu.add(new Option(this.manager, "silent", "[Remain silent.]"));

                    this.activeOutcome = parser.promptOptionsMenu(subMenu, new DialogueLine("You have no other choice."));
                    switch (activeOutcome) {
                        case "serious":
                            mainScript.runSection("seriousFreeOffer");

                            if (this.hasBlade) {
                                mainScript.runSection("seriousFreeOfferBlade");
                            } else {
                                mainScript.runSection("seriousFreeOfferNoBlade");
                            }

                            break;

                        case "want":
                            mainScript.runSection("wantFreeOffer");
                            this.adversaryNarratorProof(narratorProof);
                            break;

                        default:
                            mainScript.runSection(activeOutcome + "FreeOffer");
                    }

                    break;
                    
                case "unpack":
                    talked.set(true);
                    mainScript.runSection("unpackDistantMenu");
                    this.adversaryNarratorProof(narratorProof);
                    break;
                    
                case "undecided":
                    talked.set(true);
                    closerComment.set(true);

                    if (this.droppedBlade1) {
                        mainScript.runSection("undecidedDistantMenuTalkAgain");
                    } else {
                        mainScript.runSection("undecidedDistantMenuFirstTalk");
                    }

                    break;
                    
                case "banter":
                    talked.set(true);
                    mainScript.runSection("banterDistantMenu");
                    break;
                    
                case "closer":
                    this.repeatActiveMenu = false;
                    break;
                    
                case "slay":
                    mainScript.runSection("directEarlyJoin");
                    return this.adversaryFightDirect(true, adversaryFree, narratorProof, noFreeOffer);
                    
                case "attack":
                    return this.adversaryFight(false, adversaryFree, narratorProof, noFreeOffer);
                    
                case "cSlayPrincessNoBladeFail":
                    if (manager.demoMode() || !this.canJoint3) {
                        parser.printDialogueLine(DEMOBLOCK);
                        break;
                    }
                case "unarmedAttackA":
                case "unarmedAttackB":
                    if (manager.hasVisited(Chapter.FURY)) {
                        this.canJoint3 = false;
                        this.adversaryCantFury("distant");
                        parser.printDialogueLine(WORNPATH);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.FURY)) {
                        this.canJoint3 = false;
                        this.adversaryCantFury("distant");
                        break;
                    }
                    
                    return this.adversaryFightUnarmed(narratorProof);
                    
                case "retrieve":
                    mainScript.runSection("retrieveDistantMenu");
                    return this.adversaryRetrieveBlade(false, adversaryFree, narratorProof, noFreeOffer);
                    
                case "free":
                    return this.adversaryFree(adversaryFree, narratorProof);
                    
                case "cGoStairs":
                    if (manager.demoMode() || !this.canJoint3) {
                        parser.printDialogueLine(DEMOBLOCK);
                        break;
                    }
                case "leaveYap":
                case "leaveSilent":
                    if (manager.hasVisited(Chapter.FURY)) {
                        this.canJoint3 = false;
                        this.adversaryCantFury("distant");
                        parser.printDialogueLine(WORNPATH);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.FURY)) {
                        this.canJoint3 = false;
                        this.adversaryCantFury("distant");
                        break;
                    }
                    
                    return this.adversaryFlee(false, adversaryFree, narratorProof, noFreeOffer);
            }
        }

        // Step closer
        if (this.hasBlade) {
            mainScript.runSection("closeStartBlade");
        } else {
            mainScript.runSection("closeStartNoBlade");
        }

        Condition noEndWorldAsk = new Condition(true);
        Condition noAskFree = new Condition(true);
        Condition purposeAsk = new Condition(false);
        InverseCondition noPurposeAsk = new InverseCondition(purposeAsk);
        Condition whyOrPurpose = new Condition(false);
        InverseCondition noWhyOrPurpose = new InverseCondition(whyOrPurpose);

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "whyKill", "(Explore) \"Why do you want us to kill each other?\""));
        activeMenu.add(new Option(this.manager, "spar", "(Explore) \"Can't we just... I don't know, spar a little? Maybe there's a compromise where we can all get what we want, nobody has to die, and the world doesn't have to end.\""));
        activeMenu.add(new Option(this.manager, "noEnd", "(Explore) \"If all you want to do is fight me, does that mean you won't end the world?\"", noEndWorldAsk));
        activeMenu.add(new Option(this.manager, "freeAskA", "(Explore) \"So you're just... fine locked away in here? You really don't want to be free?\"", noAskFree, adversaryNotFree, freeOffer));
        activeMenu.add(new Option(this.manager, "freeAskB", "(Explore) \"Don't you want to be free?\"", noAskFree, adversaryNotFree, noFreeOffer));
        activeMenu.add(new Option(this.manager, "afterDied", "(Explore) \"What happened after you died?\""));
        activeMenu.add(new Option(this.manager, "why", "(Explore) \"We have to figure out why we're here.\"", noWhyOrPurpose));
        activeMenu.add(new Option(this.manager, "reason", "(Explore) \"You were put down here for a reason. I was sent to kill you for a reason. Don't you care what that reason is?\"", noPurposeAsk));
        activeMenu.add(new Option(this.manager, "cared", "(Explore) \"The last time we talked, you seemed to care about why you were here. What made you stop caring?\"", this.knowsDestiny, noPurposeAsk));
        activeMenu.add(new Option(this.manager, "attackA", "[Attack the Princess.]", this.hasBlade));
        activeMenu.add(new Option(this.manager, "attackB", "\"Fine. If you want a fight, I'll give you a fight.\" [Attack the Princess.]", this.hasBlade));
        activeMenu.add(new Option(this.manager, "retrieve", "\"Fine. If you want a fight, I'll give you a fight. I'll be right back.\" [Retrieve the blade to slay the Princess.]", !this.hasBlade));
        activeMenu.add(new Option(this.manager, "refuse", manager.demoMode() || !this.canJoint3, "\"I'm not going to fight you.\""));
        activeMenu.add(new Option(this.manager, "free", manager.demoMode(), "[Attempt to free the Princess.]", this.hasBlade, adversaryNotFree, new OrCondition(freeOffer, whyOrPurpose)));
        activeMenu.add(new Option(this.manager, "leave", manager.demoMode() || !this.canJoint3, "[Turn around and leave.]"));
        activeMenu.add(new Option(this.manager, "silent", manager.demoMode() || !this.canJoint3, "[Remain silent.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "spar":
                    activeMenu.setCondition("whyKill", false);
                case "whyKill":
                    mainScript.runSection(activeOutcome + "CloseMenu");
                    break;
                    
                case "noEnd":
                    noEndWorldAsk.set(false);

                    if (this.knowsDestiny) {
                        mainScript.runSection("noEndCloseMenuKnowsDestiny");
                    } else {
                        mainScript.runSection("noEndCloseMenuNoKnowledge");
                    }

                    break;
                    
                case "freeAskA":
                case "freeAskB":
                    adversaryFree.set(true);
                    freeOffer.set(true);
                    noAskFree.set(false);
                    mainScript.runSection(activeOutcome + "CloseMenu");

                    if (this.hasBlade) {
                        mainScript.runSection("closeBreakChainsBlade");
                    } else {
                        mainScript.runSection("closeBreakChainsNoBlade");
                    }

                    mainScript.runSection("closeBreakChainsJoin");
                    break;

                case "afterDied":
                    mainScript.runSection("afterDiedCloseMenu");

                    subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "singleFile", "(Explore) \"Or... we could just walk single-file out of here.\""));
                    subMenu.add(new Option(this.manager, "wounds", "(Explore) \"You don't have any wounds...\""));
                    subMenu.add(new Option(this.manager, "you", "(Explore) \"You're different...\""));
                    subMenu.add(new Option(this.manager, "basement", "(Explore) \"The basement is different.\""));
                    subMenu.add(new Option(this.manager, "bother", "(Explore) \"Doesn't any of this bother you? Don't you care about what you are?\""));
                    subMenu.add(new Option(this.manager, "return", "(Return) [Leave it at that.]"));

                    subMenu.get("singleFile").setPrerequisite(subMenu.get("basement"));

                    repeatSub = true;
                    while (repeatSub) {
                        this.activeOutcome = parser.promptOptionsMenu(subMenu);
                        switch (activeOutcome) {
                            case "singleFile":
                            case "wounds":
                            case "basement":
                            case "bother":
                                mainScript.runSection(activeOutcome + "AfterDied");
                                break;
                                
                            case "you":
                                if (differentComment) {
                                    mainScript.runSection("youAfterDiedAlready");
                                } else {
                                    mainScript.runSection("youAfterDiedFirst");
                                }

                                break;
                            
                            case "return":
                                repeatSub = false;
                                this.adversaryNarratorProof(narratorProof);
                                break;
                        }
                    }

                    break;

                case "why":
                    whyOrPurpose.set(true);
                    mainScript.runSection("whyCloseMenu");

                    subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "literal", "\"I don't mean 'why are we here existentially,' I mean, quite literally, why are you in this basement? Who sent me here to kill you? Why me? Why you? There are answers out there and we need to find them.\""));
                    subMenu.add(new Option(this.manager, "reason", "\"You were put down here for a reason. I was sent to kill you for a reason. Don't you care what that reason is?\""));
                    subMenu.add(new Option(this.manager, "want", "\"We're here because some people want me to kill you.\""));
                    subMenu.add(new Option(this.manager, "noEnd", "\"We're here because you're supposed to be a threat to the world. I need to find out if that's true, and if it is true, I have to find out why.\"", noEndWorldAsk));
                    subMenu.add(new Option(this.manager, "return", "(Return) [Leave it at that.]"));

                    this.activeOutcome = parser.promptOptionsMenu(subMenu);
                    switch (activeOutcome) {
                        case "noEnd":
                            noEndWorldAsk.set(false);
                        case "literal":
                            mainScript.runSection(activeOutcome + "WhyHere");
                            break;

                        case "reason":
                        case "want":
                            purposeAsk.set(true);
                            mainScript.runSection(activeOutcome + "WhyHere");

                            if (this.hasBlade) {
                                mainScript.runSection("closeGrinBlade");
                            } else {
                                mainScript.runSection("closeGrinNoBlade");
                            }
                            break;
                    }

                    break;
                    
                case "reason":
                case "cared":
                    purposeAsk.set(true);
                    whyOrPurpose.set(true);
                    mainScript.runSection(activeOutcome + "CloseMenu");

                    if (this.hasBlade) {
                        mainScript.runSection("closeGrinBlade");
                    } else {
                        mainScript.runSection("closeGrinNoBlade");
                    }

                    break;
                    
                case "cSlayPrincess":
                case "attackA":
                case "attackB":
                    return this.adversaryFight(false, adversaryFree, narratorProof, noFreeOffer);
                    
                case "retrieve":
                    mainScript.runSection("retrieveCloseMenu");
                    return this.adversaryRetrieveBlade(false, adversaryFree, narratorProof, noFreeOffer);
                    
                case "refuse":
                case "silent":
                    if (manager.hasVisited(Chapter.FURY)) {
                        this.canJoint3 = false;
                        this.adversaryCantFury("close");
                        parser.printDialogueLine(WORNPATH);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.FURY)) {
                        this.canJoint3 = false;
                        this.adversaryCantFury("close");
                        break;
                    }
                    
                    return this.adversaryPacifism(true, false, adversaryFree, narratorProof, noFreeOffer);
                    
                case "free":
                    return this.adversaryFree(adversaryFree, narratorProof);
                    
                case "cGoStairs":
                    if (manager.demoMode() || !this.canJoint3) {
                        parser.printDialogueLine(DEMOBLOCK);
                        break;
                    }
                case "leave":
                    if (manager.hasVisited(Chapter.FURY)) {
                        this.canJoint3 = false;
                        this.adversaryCantFury("close");
                        parser.printDialogueLine(WORNPATH);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.FURY)) {
                        this.canJoint3 = false;
                        this.adversaryCantFury("close");
                        break;
                    }

                    return this.adversaryFlee(false, adversaryFree, narratorProof, noFreeOffer);
                    
                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }
        
        throw new RuntimeException("No ending reached");
    }

    /**
     * Force the player into fighting the Adversary, either because the game is in demo mode or because they chose not to continue after seeing the Fury's content warnings
     * @param origin indicates the currently active menu
     */
    private void adversaryCantFury(String origin) {
        switch (origin) {
            case "distant":
                activeMenu.setGreyedOut("unarmedAttackA", true);
                activeMenu.setGreyedOut("unarmedAttackB", true);
                activeMenu.setGreyedOut("leaveSilent", true);
                activeMenu.setGreyedOut("leaveYap", true);
                break;

            case "close":
                activeMenu.setGreyedOut("refuse", true);
                activeMenu.setGreyedOut("leave", true);
                activeMenu.setGreyedOut("silent", true);
                break;

            case "retrieve":
                activeMenu.setGreyedOut("stay", true);
                activeMenu.setGreyedOut("leave", true);
                break;
        }
    }

    /**
     * The Narrator finally accepts that the player and the Adversary have met before (if He hasn't already)
     * @param narratorProof whether the Narrator has accepted that you've been here before
     */
    private void adversaryNarratorProof(Condition narratorProof) {
        if (narratorProof.check()) return;

        narratorProof.set(true);
        if (this.sharedLoop) {
            mainScript.runSection("narratorProofSharedLoop");
        } else {
            mainScript.runSection("narratorProofNoShare");
        }

        OptionsMenu proofMenu = new OptionsMenu(true);
        proofMenu.add(new Option(this.manager, "apathy", "Just because it bothers you, I'm going to take this even less seriously. You don't know the depths of my apathy!"));
        proofMenu.add(new Option(this.manager, "gaslight", "(Lie) No. I don't know what you're talking about, I've never died. Do you see how alive I am right now? Would someone as alive as me already have died? I didn't think so."));
        proofMenu.add(new Option(this.manager, "worry", "Don't worry. I'm going to do a good job!"));
        proofMenu.add(new Option(this.manager, "gotMe", "You got me. Pretty much everything you just said is true."));
        proofMenu.add(new Option(this.manager, "silent", "[Remain silent.]"));

        mainScript.runSection(parser.promptOptionsMenu(proofMenu) + "NarratorProof");
    }

    /**
     * The player fights the Adversary head-on
     * @param immediate whether the player immediately attacked the Princess or hesitated
     * @param adversaryFree whether the Princess has already broken out of her chains
     * @param narratorProof whether the Narrator has accepted that you've been here before
     * @param noFreeOffer whether the player has already offered to free the Princess
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding adversaryFightDirect(boolean immediate, Condition adversaryFree, Condition narratorProof, AbstractCondition noFreeOffer) {
        mainScript.runSection("directStart");

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "push", "[Keep pushing.]"));
        activeMenu.add(new Option(this.manager, "unlodge", manager.demoMode(), "[Unlodge the blade and attack her from a different angle.]", 0));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "cSlayPrincess":
                case "push":
                    this.repeatActiveMenu = false;
                    break;

                case "cTakeHasBladeFail":
                    if (manager.demoMode() || !this.canUnique3) {
                        parser.printDialogueLine(DEMOBLOCK);
                        break;
                    }
                case "unlodge":
                    if (!manager.confirmContentWarnings(Chapter.NEEDLE)) {
                        this.canUnique3 = false;
                        activeMenu.setGreyedOut("unlodge", true);
                        break;
                    }

                    mainScript.runSection("unlodgeDirect");
                    mainScript.runSection("dodgeSecondAttack");
                    return ChapterEnding.THREADINGTHROUGH;

                default: this.giveDefaultFailResponse();
            }
        }

        if (immediate) {
            mainScript.runSection("pushDirectImmediate");
        } else {
            mainScript.runSection("pushDirectHesitated");
        }

        InverseCondition noProof = new InverseCondition(narratorProof);
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "agree", "\"I do.\""));
        activeMenu.add(new Option(this.manager, "lie", "(Lie) \"I do.\""));
        activeMenu.add(new Option(this.manager, "question", "\"What are you talking about?\""));
        activeMenu.add(new Option(this.manager, "silent", "[Silently continue pushing.]"));
        activeMenu.add(new Option(this.manager, "unlodge", manager.demoMode() || !this.canUnique3, "[Unlodge the blade.]", 0));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "agree":
                    this.repeatActiveMenu = false;

                    if (this.knowsDestiny) {
                        mainScript.runSection("understandingKnowsDestiny");
                    } else {
                        mainScript.runSection("understandingNoKnowledge");
                    }

                    mainScript.runSection("understandingAgree");
                    break;

                case "question":
                    mainScript.runSection("understandingQuestion");
                case "lie":
                    this.repeatActiveMenu = false;
                    
                    if (this.knowsDestiny) {
                        mainScript.runSection("understandingKnowsDestiny");
                    } else {
                        mainScript.runSection("understandingNoKnowledge");
                    }

                    mainScript.runSection("understandingOther");
                    break;

                case "cSlayPrincess":
                case "silent":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("understandingSilent");
                    break;

                case "cTakeHasBladeFail":
                    if (manager.demoMode() || !this.canUnique3) {
                        parser.printDialogueLine(DEMOBLOCK);
                        break;
                    }
                case "unlodge":
                    if (!manager.confirmContentWarnings(Chapter.NEEDLE)) {
                        this.canUnique3 = false;
                        activeMenu.setGreyedOut("unlodge", true);
                        break;
                    }

                    mainScript.runSection("unlodgeDirect");
                    mainScript.runSection("unlodgeDirectLate");
                    mainScript.runSection("dodgeSecondAttack");
                    return ChapterEnding.THREADINGTHROUGH;
            }
        }

        mainScript.runSection("pushCont");
        if (!adversaryFree.check()) mainScript.runSection("pushContBreakChains");
        mainScript.runSection("pushContJoin");

        Condition canUnderstanding = new Condition(true);
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "proof", "(Explore) I hope you heard all of that, Mr. Narrator. This is a lot different than last time, but last time definitely *happened.*", this.sharedLoop, noProof));
        activeMenu.add(new Option(this.manager, "jumpGun", "(Explore) \"Aren't you jumping the gun a little here? We each died *once.* That doesn't mean we're *immortal.*\""));
        activeMenu.add(new Option(this.manager, "metaphor", "(Explore) \"Were you being metaphorical when you said that nothing exists outside of us? There's more to the world than just this cabin. I saw trees and everything on my way here.\""));
        activeMenu.add(new Option(this.manager, "freeOffer", "(Explore) \"Don't you want to be free, though? Isn't there more to life than waking up chained in a basement and fighting to the death in an endless loop?\"", noFreeOffer));
        activeMenu.add(new Option(this.manager, "dontGet", "(Explore) \"Is that what you meant when you said I 'understood?' Because if that's the case then I absolutely don't get it. I'm not even sure there's an 'it' to get!\"", activeMenu.get("metaphor"), canUnderstanding));
        activeMenu.add(new Option(this.manager, "song", "(Explore) \"Yes! The two of us are a chorus of notes building on top of each other forever. The song we write in our blood will be the most beautiful music ever written!\"", activeMenu.get("metaphor"), canUnderstanding));
        activeMenu.add(new Option(this.manager, "refuse", manager.demoMode() || !this.canJoint3, "\"Actually, I think I'm done fighting you. I don't think this is healthy for either of us.\""));
        activeMenu.add(new Option(this.manager, "attack", "[Pick up the blade and attack her again.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "proof":
                    narratorProof.set(true);

                    if (this.sharedLoopInsist) {
                        mainScript.runSection("proofDistantMenuInsist");
                    } else {
                        mainScript.runSection("proofDistantMenuNoInsist");
                    }
                    
                    break;

                case "jumpGun":
                    canUnderstanding.set(false);
                    mainScript.runSection("jumpGunPush");

                    Condition notDropped = new Condition(true);
                    this.activeMenu = new OptionsMenu();
                    activeMenu.add(new Option(this.manager, "trick", "(Explore) \"How do I know you're not tricking me?\"", notDropped));
                    activeMenu.add(new Option(this.manager, "cut", "(Explore) \"Cut it out, will you? I'm not going to take a cheap win like that.\"", notDropped));
                    activeMenu.add(new Option(this.manager, "slay", "[Slay the Princess.]"));

                    while (repeatActiveMenu) {
                        switch (parser.promptOptionsMenu(activeMenu)) {
                            case "trick":
                                mainScript.runSection("trickOpeningOffer");
                                break;

                            case "cut":
                                notDropped.set(false);
                                mainScript.runSection("cutOpeningOffer");

                            case "cSlayPrincess":
                            case "slay":
                                if (notDropped.check()) {
                                    this.adversaryDirectOpening();
                                    return ChapterEnding.THATWHICHCANNOTDIE;
                                } else {
                                    this.repeatActiveMenu = false;
                                    break;
                                }

                            default: mainScript.runSection("openingFail");
                        }
                    }
                    
                    break;

                case "metaphor":
                    mainScript.runSection("metaphorOfferPush");
                    break;

                case "freeOffer":
                    canUnderstanding.set(false);
                    mainScript.runSection("freeOfferPush");

                    OptionsMenu freeMenu = new OptionsMenu();
                    freeMenu.add(new Option(this.manager, "oh", "\"Oh I get it, so you're like some sort of psychosexual sadomasochist. That makes sense.\""));
                    freeMenu.add(new Option(this.manager, "chance", "\"There will always be another chance to for us to kill each other.\""));
                    freeMenu.add(new Option(this.manager, "fine", "\"There's no getting through to you, is there? Fine.\""));
                    freeMenu.add(new Option(this.manager, "silent", "[Say nothing.]"));
                    freeMenu.add(new Option(this.manager, "attack", "\"Okay, sure, I get it now. Let's do this.\" [Pick up the blade and attack her again.]\""));

                    boolean repeatMenu = true;
                    while (repeatMenu) {
                        this.activeOutcome = parser.promptOptionsMenu(freeMenu);
                        switch (activeOutcome) {
                            case "oh":
                            case "chance":
                            case "fine":
                            case "silent":
                                repeatMenu = false;
                                mainScript.runSection(activeOutcome + "DirectFree");
                                break;

                            case "cTakeHasBladeFail":
                            case "cSlayPrincess":
                            case "attack":
                                this.repeatActiveMenu = false;
                                repeatMenu = false;

                            default: this.giveDefaultFailResponse();
                        }
                    }
                    
                    break;

                case "dontGet":
                case "song":
                    canUnderstanding.set(false);
                    mainScript.runSection(activeOutcome + "Push");
                    break;

                case "refuse":
                    if (manager.hasVisited(Chapter.FURY)) {
                        this.canJoint3 = false;
                        activeMenu.setGreyedOut("refuse", true);
                        parser.printDialogueLine(WORNPATH);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.FURY)) {
                        this.canJoint3 = false;
                        activeMenu.setGreyedOut("refuse", true);
                        break;
                    }

                    mainScript.runSection("refuseCommitFromFight");
                    return this.adversaryPacifism(true, true, adversaryFree, narratorProof, noFreeOffer);

                case "cTakeHasBladeFail":
                case "cSlayPrincess":
                case "attack":
                    this.repeatActiveMenu = false;
                    break;

                default: this.giveDefaultFailResponse();
            }
        }

        // Attack her again
        mainScript.runSection("attackPenultimate");

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "up", "[Get up.]"));
        parser.promptOptionsMenu(activeMenu);

        mainScript.runSection();
        this.adversaryDirectEnd(false);
        return ChapterEnding.THATWHICHCANNOTDIE;
    }

    /**
     * The player takes the opening that the Princess gives them
     */
    private void adversaryDirectOpening() {
        mainScript.runSection("openingStart");

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "watch", "[Watch her body for a little while longer.]"));
        activeMenu.add(new Option(this.manager, "leave", "[Turn and leave.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);

            if (activeOutcome.equals("cGoStairs")) activeOutcome = "leave";
            switch (activeOutcome) {
                case "watch":
                case "leave":
                    this.repeatActiveMenu = false;
                    mainScript.runSection(activeOutcome + "Opening");
                    mainScript.runSection(activeOutcome + "OpeningCont");
                    break;

                default: this.giveDefaultFailResponse();
            }
        }
        
        this.adversaryDirectEnd(true);
    }

    /**
     * Runs the "That Which Cannot Die Cannot Die" ending of Chapter II: The Adversary
     * @param fromOpening whether the player took the opening the Princess gave them
     */
    private void adversaryDirectEnd(boolean fromOpening) {
        mainScript.runSection("noDie");

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "explore", "(Explore) \"Close to untethering?\" What's that supposed to mean?"));
        activeMenu.add(new Option(this.manager, "attack", "[Attack her again.]"));

        boolean cantDieThought = false;
        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "explore":
                    mainScript.runSection("noDieExplore");

                    Option cantDie = new Option(this.manager, "cantDie", "The Princess Can't Die.");
                    this.activeMenu = new OptionsMenu(true);
                    for (int i = 0; i < 12; i++) activeMenu.add(cantDie, "cantDie" + i);
                    activeMenu.add(new Option(this.manager, "late", "You mean like \"The Princess Can't Die\" don't you? Well, it's too late for that. I think we've all thought that one."));
                    for (int i = 0; i < 3; i++) activeMenu.add(cantDie, "cantDie" + (i+12));
                    activeMenu.add(new Option(this.manager, "refuse", "I'm not going to say The Princess Can't Die. I'm not even going to think it."));
                    activeMenu.add(cantDie, "cantDie15");
                    activeMenu.add(new Option(this.manager, "lie", "(Lie) The Princess Can Die."));
                    for (int i = 0; i < 4; i++) activeMenu.add(cantDie, "cantDie" + (i+16));

                    switch (parser.promptOptionsMenu(activeMenu)) {
                        case "cantDie0":
                        case "cantDie1":
                        case "cantDie2":
                        case "cantDie3":
                        case "cantDie4":
                        case "cantDie5":
                        case "cantDie6":
                        case "cantDie7":
                        case "cantDie8":
                        case "cantDie9":
                        case "cantDie10":
                        case "cantDie11":
                        case "cantDie12":
                        case "cantDie13":
                        case "cantDie14":
                        case "cantDie15":
                        case "cantDie16":
                        case "cantDie17":
                        case "cantDie18":
                        case "cantDie19":
                            mainScript.runSection("cantDieThought");
                            break;

                        case "late":
                        case "refuse":
                        case "lie":
                            mainScript.runSection("cantDieAltThought");
                            break;
                    }

                    this.repeatActiveMenu = false;
                    cantDieThought = true;

                    this.activeMenu = new OptionsMenu(true);
                    activeMenu.add(new Option(this.manager, "join", "[Join your Adversary.]"));
                    parser.promptOptionsMenu(activeMenu);
                    break;

                case "cTakeHasBladeFail":
                case "cSlayPrincess":
                case "attack":
                    this.repeatActiveMenu = false;
                    break;

                default: super.giveDefaultFailResponse();
            }
        }

        if (fromOpening) {
            if (cantDieThought) mainScript.runSection("directEndStartThought");
            mainScript.runSection("directEndStartFromOpening");

            this.activeMenu = new OptionsMenu(true);
            activeMenu.add(new Option(this.manager, "wake", "[Wake up.]"));
            activeMenu.add(new Option(this.manager, "join", "[Join your Adversary.]", activeMenu.get("wake")));
            parser.promptOptionsMenu(activeMenu);
            mainScript.runSection();
            parser.promptOptionsMenu(activeMenu);
        } else {
            mainScript.runSection("directEndStartOther");

            this.activeMenu = new OptionsMenu(true);
            activeMenu.add(new Option(this.manager, "up", "[Get up.]"));
            parser.promptOptionsMenu(activeMenu);
            
            this.activeMenu = new OptionsMenu();
            activeMenu.add(new Option(this.manager, "slay", "[Slay the Princess.]"));
            
            this.repeatActiveMenu = true;
            while (repeatActiveMenu) {
                switch (parser.promptOptionsMenu(activeMenu)) {
                    case "cSlayPrincess":
                    case "slay":
                        this.repeatActiveMenu = false;
                        mainScript.runSection();
                        break;

                    default: this.giveDefaultFailResponse();
                }
            }
        }

        mainScript.runSection("directFinalStart");

        this.currentVoices.put(Voice.NARRATOR, false);
        this.quietCreep();
        mainScript.runSection();

        if (this.isFirstVessel) {
            mainScript.runSection("noAnswerFirstVessel");

            this.activeMenu = new OptionsMenu(true);
            activeMenu.add(new Option(this.manager, "chin", "\"Chin up! Isn't this what we wanted? Just you and me forever?\""));
            activeMenu.add(new Option(this.manager, "ok", "\"Are you okay?\""));
            activeMenu.add(new Option(this.manager, "end", "\"We ended the world, didn't we? Everything is gone.\""));
            activeMenu.add(new Option(this.manager, "rest", "\"We've been fighting for a long time. You should rest.\""));
            activeMenu.add(new Option(this.manager, "silent", "[Remain silent.]"));

            switch (parser.promptOptionsMenu(activeMenu)) {
                case "silent":
                    mainScript.runSection();
                    mainScript.runSection("directFinalEndSilent");
                    break;

                default:
                    mainScript.runSection();
                    mainScript.runSection("directFinalEndReply");
                    break;
            }
        } else {
            mainScript.runSection("noAnswerNotFirstVessel");
            mainScript.runSection();
            mainScript.runSection("directFinalEndNotFirstVessel");
        }
    }

    /**
     * After not initially taking the blade, the player chooses to retrieve it from upstairs
     * @param wounded whether the player is already wounded
     * @param adversaryFree whether the Princess has already broken out of her chains
     * @param narratorProof whether the Narrator has accepted that you've been here before
     * @param noFreeOffer whether the player has already offered to free the Princess
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding adversaryRetrieveBlade(boolean wounded, Condition adversaryFree, Condition narratorProof, AbstractCondition noFreeOffer) {
        this.currentLocation = GameLocation.CABIN;
        this.withPrincess = false;
        this.withBlade = true;

        mainScript.runSection("retrieveStart");

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "explore", "(Explore) You know, we could just stay here. Who says we have to fight to the death?"));
        activeMenu.add(new Option(this.manager, "take", "[Take the blade from the altar.]"));
        activeMenu.add(new Option(this.manager, "stay", manager.demoMode() || !this.canJoint3, "We're doing it. We're staying up here.", 0, activeMenu.get("explore")));
        activeMenu.add(new Option(this.manager, "leave", manager.demoMode() || !this.canJoint3, "We're leaving.", 0, activeMenu.get("explore")));
        activeMenu.add(new Option(this.manager, "return", "[Return to the basement.]", false));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "explore":
                    mainScript.runSection("exploreRetrieve");

                    if (adversaryFree.check()) {
                        mainScript.runSection("exploreRetrieveFree");
                    } else {
                        mainScript.runSection("exploreRetrieveNotFree");
                    }

                    break;

                case "cTake":
                case "take":
                    this.withBlade = false;
                    this.hasBlade = true;
                    activeMenu.setCondition("return", true);
                    mainScript.runSection("takeBladeRetrieve");
                    break;

                case "stay":
                    if (manager.hasVisited(Chapter.FURY)) {
                        this.canJoint3 = false;
                        this.adversaryCantFury("retrieve");
                        parser.printDialogueLine(WORNPATH);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.FURY)) {
                        this.canJoint3 = false;
                        this.adversaryCantFury("retrieve");
                        break;
                    }
                    
                    if (this.hasBlade) mainScript.runSection("fleeRetrieveTookBlade");
                    mainScript.runSection("stayRetrieve");
                    if (!adversaryFree.check()) mainScript.runSection("fleeNotFree");
                    mainScript.runSection("stayRetrieveCont");
                    return this.adversaryFleeUpstairs(wounded, true);

                case "cGoHill":
                    if (manager.demoMode() || !this.canJoint3 || !activeMenu.hasBeenPicked("explore")) {
                        parser.printDialogueLine(DEMOBLOCK);
                        break;
                    }
                case "leave":
                    if (manager.hasVisited(Chapter.FURY)) {
                        this.canJoint3 = false;
                        this.adversaryCantFury("retrieve");
                        parser.printDialogueLine(WORNPATH);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.FURY)) {
                        this.canJoint3 = false;
                        this.adversaryCantFury("retrieve");
                        break;
                    }
                    
                    if (this.hasBlade) mainScript.runSection("fleeRetrieveTookBlade");
                    mainScript.runSection("leaveRetrieve");
                    if (!adversaryFree.check()) mainScript.runSection("fleeNotFree");
                    return this.adversaryFleeUpstairs(wounded, true);

                case "cGoStairs":
                    if (!this.hasBlade) {
                        mainScript.runSection("returnFailRetrieve");
                        break;
                    }
                case "return":
                    this.repeatActiveMenu = false;
                    break;
            }
        }

        this.currentLocation = GameLocation.BASEMENT;
        this.withPrincess = true;
        this.canSlayPrincess = true;
        mainScript.runSection("returnRetrieve");

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "attack", "[Attack the Princess.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "cSlayPrincess":
                case "attack":
                    this.repeatActiveMenu = false;
                    break;

                default: this.giveDefaultFailResponse();
            }
        }

        return this.adversaryFight(wounded, adversaryFree, narratorProof, noFreeOffer);
    }

    /**
     * The player chooses to fight the Adversary after hesitating
     * @param wounded whether the player is already wounded
     * @param adversaryFree whether the Princess has already broken out of her chains
     * @param narratorProof whether the Narrator has accepted that you've been here before
     * @param noFreeOffer whether the player has already offered to free the Princess
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding adversaryFight(boolean wounded, Condition adversaryFree, Condition narratorProof, AbstractCondition noFreeOffer) {
        if (wounded) {
            mainScript.runSection("fightWounded");
            return ChapterEnding.DEADISDEAD;
        }

        mainScript.runSection("fightStart");

        if (!adversaryFree.check()) {
            adversaryFree.set(true);
            mainScript.runSection("fightBreakChains");
        }

        mainScript.runSection("fightCont");
        
        if (narratorProof.check()) {
            mainScript.runSection("fightNarratorProof");
        } else {
            mainScript.runSection("fightNoProof");
        }

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "bait", manager.demoMode(), "[Bait an opening and outmaneuver her.]", 0));
        activeMenu.add(new Option(this.manager, "strike", "[Strike at her heart head-on.]"));
        activeMenu.add(new Option(this.manager, "run", manager.demoMode() || !this.canJoint3, "[Run.]", 0));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "bait":
                    if (!manager.confirmContentWarnings(Chapter.NEEDLE)) {
                        activeMenu.setGreyedOut("bait", true);
                        break;
                    }

                    mainScript.runSection("dodgeSecondAttack");
                    return ChapterEnding.THREADINGTHROUGH;
                
                case "cSlayPrincess":
                case "strike":
                    mainScript.runSection("fightStrike");
                    return this.adversaryFightDirect(false, adversaryFree, narratorProof, noFreeOffer);

                case "cGoStairs":
                    if (manager.demoMode() || !this.canJoint3) {
                        parser.printDialogueLine(DEMOBLOCK);
                        break;
                    }
                case "run":
                    if (manager.hasVisited(Chapter.FURY)) {
                        this.canJoint3 = false;
                        activeMenu.setGreyedOut("run", true);
                        parser.printDialogueLine(WORNPATH);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.FURY)) {
                        this.canJoint3 = false;
                        activeMenu.setGreyedOut("run", true);
                        break;
                    }
                    
                    mainScript.runSection("fleeRunFail");

                    if (narratorProof.check()) {
                        mainScript.runSection("fleeEndNarratorProof");
                    } else {
                        mainScript.runSection("fleeEndNoProof");
                    }

                    return ChapterEnding.DEADISDEAD;
            }
        }

        throw new RuntimeException("No ending reached");
    }

    /**
     * The player attempts to fight the Adversary unarmed, leading to Chapter III: The Fury
     * @param narratorProof whether the Narrator has accepted that you've been here before
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding adversaryFightUnarmed(Condition narratorProof) {
        mainScript.runSection("unarmedStart");

        OptionsMenu deathMenu = new OptionsMenu(true);
        deathMenu.add(new Option(this.manager, "getUp", "[Get up.]", 0));
        deathMenu.add(new Option(this.manager, "die", "[Die.]"));

        if (parser.promptOptionsMenu(deathMenu, new VoiceDialogueLine("You have no other choice.")).equals("die")) {
            if (narratorProof.check()) {
                mainScript.runSection("unarmedDieNarratorProof");
            } else {
                mainScript.runSection("unarmedDieNoProof");
            }

            return ChapterEnding.DEADISDEAD;
        }

        mainScript.runSection();

        Condition noFaceExplore = new Condition(true);
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "face", "(Explore) \"Wait no... my face? You're joking right? You're exaggerating? I still have my face right?\" [Touch your face.]"));
        activeMenu.add(new Option(this.manager, "retrieve", "\"Can... can I go get my pristine blade now?\""));
        activeMenu.add(new Option(this.manager, "better", "\"No, you definitely killed me. I just got better.\"", noFaceExplore));
        activeMenu.add(new Option(this.manager, "easy", "\"You're not putting me down that easy.\"", noFaceExplore));
        activeMenu.add(new Option(this.manager, "run", "[Run.]"));
        activeMenu.add(new Option(this.manager, "attack", "[Attack the Princess.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "face":
                    noFaceExplore.set(false);
                    mainScript.runSection("faceUnarmedPhase1");
                    break;

                case "retrieve":
                case "run":
                    this.repeatActiveMenu = false;
                    mainScript.runSection(activeOutcome + "UnarmedPhase1");
                    break;

                case "better":
                case "easy":
                    mainScript.runSection("easyUnarmedPhase1");
                case "attack":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("attackUnarmedPhase1");
                    break;
            }
        }

        if (this.forestSpecial) {
            mainScript.runSection("unarmedPhase1Special");
        } else {
            mainScript.runSection("unarmedPhase1NoSpecial");
        }

        if (parser.promptOptionsMenu(deathMenu, new VoiceDialogueLine("You have no other choice.")).equals("die")) {
            mainScript.runSection("unarmedDiePhase1");

            if (narratorProof.check()) {
                mainScript.runSection("unarmedDieNarratorProof");
            } else {
                mainScript.runSection("unarmedDieNoProof");
            }

            return ChapterEnding.DEADISDEAD;
        }

        mainScript.runSection("unarmedPhase2");
        return ChapterEnding.HEARNOBELL;
    }

    /**
     * The player simply refuses to fight the Adversary, leading to Chapter III: The Fury
     * @param tookBladeStart whether the player took the blade before entering the basement
     * @param fromFight whether the player stopped in the middle of fighting the Princess
     * @param adversaryFree whether the Princess has already freed herself from her chains
     * @param narratorProof whether the Narrator has accepted that you've been here before
     * @param noFreeOffer whether the player has already offered to free the Princess
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding adversaryPacifism(boolean tookBladeStart, boolean fromFight, Condition adversaryFree, Condition narratorProof, AbstractCondition noFreeOffer) {
        if (!adversaryFree.check()) {
            adversaryFree.set(true);
            mainScript.runSection("reufseBreakChains");

            if (this.hasBlade) {
                mainScript.runSection("closeBreakChainsBlade");
            } else {
                mainScript.runSection("closeBreakChainsNoBlade");
            }

            this.canSlayPrincess = this.hasBlade;
            this.activeMenu = new OptionsMenu();
            activeMenu.add(new Option(this.manager, "attack", "[Attack the Princess.]", this.hasBlade));
            activeMenu.add(new Option(this.manager, "retrieve", "\"Okay, fine. If you want to fight then we can fight. You're not giving me many options. Just let me get a weapon.\" [Go upstairs and retrieve the blade.]", !this.hasBlade));
            activeMenu.add(new Option(this.manager, "flee", "[Flee up the stairs and retrieve the blade.]", !this.hasBlade));
            activeMenu.add(new Option(this.manager, "refuse", "\"This doesn't change anything.\" [Refuse to fight.]"));
            activeMenu.add(new Option(this.manager, "silent", "[Silently stand your ground and refuse to fight.]"));

            this.repeatActiveMenu = true;
            while (repeatActiveMenu) {
                this.activeOutcome = parser.promptOptionsMenu(activeMenu);
                switch (activeOutcome) {
                    case "cSlayPrincess":
                    case "attack":
                        return this.adversaryFight(false, adversaryFree, narratorProof, noFreeOffer);

                    case "retrieve":
                        mainScript.runSection("refuseRetrieve");
                        return this.adversaryRetrieveBlade(false, adversaryFree, narratorProof, noFreeOffer);

                    case "cGoStairs":
                        if (this.hasBlade) {
                            this.giveDefaultFailResponse();
                            break;
                        }
                    case "flee":
                        return this.adversaryFlee(false, adversaryFree, narratorProof, noFreeOffer);

                    case "refuse":
                    case "silent":
                        this.repeatActiveMenu = false;
                        mainScript.runSection("refuseBreakNoChange");
                        break;

                    default: this.giveDefaultFailResponse(activeOutcome);
                }
            }
        }

        mainScript.runSection("refuseCommit");

        if (fromFight) {
            mainScript.runSection("refuseCommitBladeFromFight");
        } else if (this.hasBlade) {
            mainScript.runSection("refuseCommitBladeOther");
        } else {
            mainScript.runSection("refuseCommitNoBlade");
        }

        boolean isStanding = false;
        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "stuck", "(Explore) \"I can't get up.\""));
        activeMenu.add(new Option(this.manager, "killMe", "\"You might as well just kill me, because you're not going to change my mind.\"", activeMenu.get("stuck")));
        activeMenu.add(new Option(this.manager, "stand", "[Get up.]", activeMenu.get("stuck")));
        activeMenu.add(new Option(this.manager, "silent", "[Remain silent.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "killMe":
                    this.repeatActiveMenu = false;
                case "stuck":
                    mainScript.runSection(activeOutcome + "Refuse");
                    break;

                case "stand":
                    isStanding = true;
                    mainScript.runSection("standRefuse");

                    this.canSlayPrincess = this.hasBlade;
                    this.activeMenu = new OptionsMenu();
                    activeMenu.add(new Option(this.manager, "explore", "\"If I turn my back on you to get the blade... how do I know you won't just kill me?\"", !this.hasBlade));
                    activeMenu.add(new Option(this.manager, "retrieve", "\"Okay. I'll be right back.\" [Retrieve the blade.]", !this.hasBlade));
                    activeMenu.add(new Option(this.manager, "refuse", "\"I'm still not going to fight you.\""));
                    activeMenu.add(new Option(this.manager, "silent", "[Remain silent.]"));
                    activeMenu.add(new Option(this.manager, "fleeNoBlade", "[Run like hell.]", !this.hasBlade));
                    activeMenu.add(new Option(this.manager, "fleeBlade", "[Grab the blade and run like hell.]", this.hasBlade));
                    activeMenu.add(new Option(this.manager, "attack", "[Attack the Princess.]", tookBladeStart && !fromFight));

                    this.repeatActiveMenu = true;
                    while (repeatActiveMenu) {
                        this.activeOutcome = parser.promptOptionsMenu(activeMenu);
                        switch (activeOutcome) {
                            case "explore":
                                mainScript.runSection("exploreStand");
                                break;

                            case "retrieve":
                                mainScript.runSection("retrieveStand");
                                return this.adversaryRetrieveBlade(true, adversaryFree, narratorProof, noFreeOffer);

                            case "refuse":
                            case "silent":
                                this.repeatActiveMenu = false;
                                mainScript.runSection(activeOutcome + "Stand");
                                break;

                            case "fleeNoBlade":
                                mainScript.runSection("fleeNoBladeStand");
                                return this.adversaryRetrieveBlade(true, adversaryFree, narratorProof, noFreeOffer);

                            case "fleeBlade":
                                mainScript.runSection("fleeBladeStand");
                                return this.adversaryFleeUpstairs(true, false);

                            case "attack":
                                return this.adversaryFight(true, adversaryFree, narratorProof, noFreeOffer);

                            case "cTakeHasBladeFail":
                                this.giveDefaultFailResponse();
                                break;

                            default: this.giveDefaultFailResponse(activeOutcome);
                        }
                    }

                    break;

                case "silent":
                    this.repeatActiveMenu = false;

                    if (activeMenu.hasBeenPicked("stuck")) {
                        mainScript.runSection("silentRefuseStuck");
                    } else {
                        mainScript.runSection("silentRefuseNoStuck");
                    }

                    break;
            }
        }

        // You die
        mainScript.runSection("refuseEndStart");
        
        if (isStanding) {
            mainScript.runSection("refuseEndStanding");
        } else {
            mainScript.runSection("refuseEndNotStanding");
        }

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "noMatter", "\"Because death doesn't matter anymore, does it? Fighting, not fighting -- what does any of it matter if it all ends the same way?\""));
        activeMenu.add(new Option(this.manager, "more", "\"Because there's more to this than just fighting each other. If letting you kill me is how I can show you that, then it's worth it.\""));
        activeMenu.add(new Option(this.manager, "care", "\"I care about you, and I don't want to hurt you anymore.\""));
        activeMenu.add(new Option(this.manager, "funny", "\"I just think it's kind of funny...\""));
        activeMenu.add(new Option(this.manager, "silent", "[Remain silent.]"));

        mainScript.runSection(parser.promptOptionsMenu(activeMenu) + "RefuseEnd");

        if (narratorProof.check()) {
            mainScript.runSection("refuseEndNarratorProof");
        } else {
            mainScript.runSection("refuseEndNoProof");
        }

        return ChapterEnding.STRIKEMEDOWN;
    }

    /**
     * The player attempts to free the Adversary, leading to Chapter III: The Eye of the Needle
     * @param wounded whether the player is already wounded
     * @param adversaryFree whether the Princess has already broken out of her chains
     * @param narratorProof whether the Narrator has accepted that you've been here before
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding adversaryFree(Condition adversaryFree, Condition narratorProof) {
        mainScript.runSection("freeStart");

        Option attack = new Option(this.manager, "attack", "[Attack the Princess.]");
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "free", "We're doing it. [Free the Princess.]"));
        activeMenu.add(attack);

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "free":
                    this.repeatActiveMenu = false;
                    break;
                
                case "cSlayPrincess":
                case "attack":
                    return this.adversaryFight(false, adversaryFree, narratorProof, new Condition(false));

                default: this.giveDefaultFailResponse();
            }
        }

        mainScript.runSection();

        this.activeMenu = new OptionsMenu();
        for (int i = 0; i < 6; i++) activeMenu.add(attack, "attack" + i);
        activeMenu.add(new Option(this.manager, "free", "[Free the Princess.", 0));
        for (int i = 0; i < 4; i++) activeMenu.add(attack, "attack" + (i+6));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "free":
                    this.repeatActiveMenu = false;
                    break;

                case "cSlayPrincess":
                case "attack0":
                case "attack1":
                case "attack2":
                case "attack3":
                case "attack4":
                case "attack5":
                case "attack6":
                case "attack7":
                case "attack8":
                case "attack9":
                    return this.adversaryFight(false, adversaryFree, narratorProof, new Condition(false));

                default: this.giveDefaultFailResponse();
            }
        }

        mainScript.runSection();
        return ChapterEnding.FREEINGSOMEONE;
    }

    /**
     * The player attempts to flee from the Adverary
     * @param wounded whether the player is already wounded
     * @param adversaryFree whether the Princess has already freed herself from her chains
     * @param narratorProof whether the Narrator has accepted that you've been here before
     * @param noFreeOffer whether the player has already offered to free the Princess
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding adversaryFlee(boolean wounded, Condition adversaryFree, Condition narratorProof, AbstractCondition noFreeOffer) {
        boolean brokeFree = !adversaryFree.check();
        adversaryFree.set(true);
        this.activeMenu = new OptionsMenu();

        if (brokeFree) {
            mainScript.runSection("fleeStartNotFree");
            
            activeMenu.add(new Option(this.manager, "explore", "(Explore) Okay, team. What are we thinking?"));
            activeMenu.add(new Option(this.manager, "turn", "[Turn and fight her head-on.]", this.hasBlade));
            activeMenu.add(new Option(this.manager, "dodge", "[Dodge to the side and counter-attack.]", 0, this.hasBlade));
            activeMenu.add(new Option(this.manager, "run", "[Run like hell.]"));
            activeMenu.add(new Option(this.manager, "die", "[Die.]"));

            this.repeatActiveMenu = true;
            while (repeatActiveMenu) {
                this.activeOutcome = parser.promptOptionsMenu(activeMenu);
                switch (activeOutcome) {
                    case "explore":
                        this.repeatActiveMenu = false;
                        mainScript.runSection("fleeExplore");
                        break;

                    case "turn":
                        return this.adversaryFight(wounded, adversaryFree, narratorProof, noFreeOffer);

                    case "dodge":
                        if (!manager.confirmContentWarnings(Chapter.NEEDLE)) {
                            activeMenu.setGreyedOut("dodge", true);
                            break;
                        }

                        mainScript.runSection("dodgeFirstAttack");
                        return ChapterEnding.THREADINGTHROUGH;

                    case "cGoStairs":
                    case "run":
                        if (this.hasBlade) {
                            mainScript.runSection("fleeRunSuccessBlade");
                        } else {
                            mainScript.runSection("fleeRunSuccessNoBlade");
                        }

                        return this.adversaryFleeUpstairs(wounded, false);

                    case "die":
                        this.repeatActiveMenu = false;
                        mainScript.runSection("fleeChooseDie");
                        mainScript.runSection("fleeChooseDieCont");
                        break;

                    default: this.giveDefaultFailResponse(activeOutcome);
                }
            }
        } else {
            if (this.hasBlade) {
                mainScript.runSection("fleeStartFreeBlade");
            } else {
                mainScript.runSection("fleeStartFreeNoBlade");
            }
            
            activeMenu.add(new Option(this.manager, "turn", "[Turn and fight.]", this.hasBlade));
            activeMenu.add(new Option(this.manager, "run", "[Run like hell.]"));
            activeMenu.add(new Option(this.manager, "die", "[Die.]"));

            this.repeatActiveMenu = true;
            while (repeatActiveMenu) {
                this.activeOutcome = parser.promptOptionsMenu(activeMenu);
                switch (activeOutcome) {
                    case "turn":
                        this.repeatActiveMenu = false;
                        mainScript.runSection("fleeTurnFail");
                        break;

                    case "cGoStairs":
                    case "run":
                        this.repeatActiveMenu = false;
                        mainScript.runSection("fleeRunFail");
                        break;

                    case "die":
                        this.repeatActiveMenu = false;
                        mainScript.runSection("fleeChooseDie");
                        mainScript.runSection("fleeChooseDieCont");
                        break;

                    default: this.giveDefaultFailResponse(activeOutcome);
                }
            }
        }

        // You die
        if (narratorProof.check()) {
            mainScript.runSection("fleeEndNarratorProof");
        } else {
            mainScript.runSection("fleeEndNoProof");
        }

        return ChapterEnding.DEADISDEAD;
    }

    /**
     * The player manages to flee from the Adversary and make it upstairs
     * @param wounded whether the player is already wounded
     * @param retrieve whether the player originally went upstairs to retrieve the blade
     * @param lateJoin whether to skip the first portion of dialogue
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding adversaryFleeUpstairs(boolean wounded, boolean retrieve) {
        this.currentLocation = GameLocation.CABIN;
        this.withPrincess = false;
        boolean tookBladeStart = !retrieve && this.hasBlade;

        if (retrieve) {
            mainScript.runSection("upstairsRetrieveStart");
        } else {
            if (!this.hasBlade) {
                this.withBlade = true;
                mainScript.runSection("upstairsNoBlade");

                this.activeMenu = new OptionsMenu();
                activeMenu.add(new Option(this.manager, "take", "[Take the blade.]"));
                activeMenu.add(new Option(this.manager, "leave", "[You're fine just the way you are, thank you.]"));

                this.repeatActiveMenu = true;
                while (repeatActiveMenu) {
                    switch (parser.promptOptionsMenu(activeMenu)) {
                        case "cTake":
                        case "take":
                            this.repeatActiveMenu = false;
                            this.hasBlade = true;
                            mainScript.runSection("upstairsTakeBlade");
                            break;

                        case "leave":
                            this.repeatActiveMenu = false;
                            mainScript.runSection("upstairsTakeBlade");
                            break;

                        default: this.giveDefaultFailResponse();
                    }
                }

                this.withBlade = false;
            }

            mainScript.runSection("upstairsFleeStart");
        }

        if (this.hasBlade) {
            mainScript.runSection("upstairsStartBlade");

            if (tookBladeStart) {
                mainScript.runSection("upstairsCommentBlade");
            } else {
                mainScript.runSection("upstairsCommentTookBlade");
            }

            if (wounded) {
                mainScript.runSection("upstairsDieWounded");
                mainScript.runSection("upstairsDieBlade");
            } else {
                mainScript.runSection("upstairsFightEnd");
                return ChapterEnding.THREADINGTHROUGH;
            }
        } else {
            mainScript.runSection("upstairsStartNoBlade");
            mainScript.runSection("upstairsCommentNoBlade");
            mainScript.runSection("upstairsDieEnd");
        }

        mainScript.runSection("upstairsDieCont");
        return ChapterEnding.DEADISDEAD;
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

        if (this.knowsDestiny) {
            mainScript.runSection("startKnowsDestiny");
            if (this.sharedLoop) {
                mainScript.runSection("startKnowsDestinySharedLoop");
            } else {
                mainScript.runSection("startKnowsDestinyNoShare");
            }
        } else {
            if (this.hasBlade) {
                if (source.equals("pathetic")) {
                    mainScript.runSection("startBladePathetic");
                } else {
                    mainScript.runSection("startBlade");
                }
            } else {
                mainScript.runSection("startNoBlade");
            }

            if (this.sharedLoop) {
                mainScript.runSection("startSharedLoop");
            } else {
                mainScript.runSection("startNoShare");
            }

            this.activeMenu = new OptionsMenu(true);
            activeMenu.add(new Option(this.manager, "cantRefuse", "(Explore) I don't think I can refuse her. Sorry.", resistCount == 0));
            activeMenu.add(new Option(this.manager, "noStutter", "(Explore) \"N-no. I w-won't t-tell you.\"", resistCount > 0 && submitCount > 0));
            activeMenu.add(new Option(this.manager, "shareMotive", "\"You're supposed to end the world.\""));
            activeMenu.add(new Option(this.manager, "noForce", manager.demoMode(), "\"I said NO!\"", 0, activeMenu.get("shareMotive"), tookBlade && resistCount >= 3));
            activeMenu.add(new Option(this.manager, "no", manager.demoMode(), "\"No.\"", 0, submitCount == 0));
            activeMenu.add(new Option(this.manager, "silent", manager.demoMode() && submitCount == 0, "[Remain silent.]", 0));

            this.repeatActiveMenu = true;
            while (repeatActiveMenu) {
                switch (parser.promptOptionsMenu(activeMenu)) {
                    case "cantRefuse":
                        mainScript.runSection("motiveCantRefuse");
                        break;
                    
                    case "noStutter":
                        resistCount += 1;
                        mainScript.runSection("motiveNoStutter");
                        break;
                    
                    case "shareMotive":
                        this.repeatActiveMenu = false;
                        this.knowsDestiny = true;
                        mainScript.runSection("motiveShare");
                        break;
                    
                    case "noForce":
                        if (!manager.confirmContentWarnings("forced self-mutilation, forced suicide", true)) break;

                        this.repeatActiveMenu = false;
                        mainScript.runSection("motiveNoForce");
                        return this.towerResistBlade(resistCount, submitCount, false);
                    
                    case "no":
                        if (!manager.confirmContentWarnings("forced self-mutilation, forced suicide", true)) break;

                        this.repeatActiveMenu = false;
                        return this.towerResistBlade(resistCount, submitCount, false);
                    
                    case "silent":
                        this.repeatActiveMenu = false;
                        this.knowsDestiny = true;

                        if (submitCount == 0) {
                            if (!manager.confirmContentWarnings("forced self-mutilation, forced suicide", true)) break;

                            mainScript.runSection("motiveSilentNoSubmit");
                            return this.towerResistBlade(resistCount, submitCount, false);
                        }

                        mainScript.runSection("motiveSilent");

                        if (tookBlade) {
                            mainScript.runSection("motiveShareBlade");
                        } else {
                            mainScript.runSection("motiveShareNoBlade");
                        }

                        break;
                }
            }
        }

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "role", "(Explore) \"What would you have me do? What do you have planned?\""));
        activeMenu.add(new Option(this.manager, "powerful", "(Explore) \"If you're so powerful, can't you just break the chains yourself?\"", activeMenu.get("role")));
        activeMenu.add(new Option(this.manager, "selfDetermination", "(Explore) \"Just because you're supposed to end the world doesn't mean you actually have to do it. You can be whatever you want to be.\""));
        activeMenu.add(new Option(this.manager, "questions", "(Explore) \"I have questions for you before I decide to do anything.\""));
        activeMenu.add(new Option(this.manager, "happened", "(Explore) \"What happened to you after I died?\""));
        activeMenu.add(new Option(this.manager, "refuseNoBladeA", manager.demoMode(), "\"I'm not going to help you end the world. I don't care if something new comes after. I just can't let you do that.\"", 0, !tookBlade && resistCount >= 2));
        activeMenu.add(new Option(this.manager, "refuseNoBladeB", manager.demoMode(), "\"No. I won't take part in this.\" [Refuse her.]", 0, !tookBlade && resistCount >= 2));
        activeMenu.add(new Option(this.manager, "refuseBlade", manager.demoMode(), "\"I'm not going to help you end the world. I don't care if something new comes after. I just can't let you do that.\"", tookBlade));
        activeMenu.add(new Option(this.manager, "pledge", "\"I'm yours to command.\" [Pledge yourself to her.]"));

        boolean priestOffer = false;
        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            activeOutcome = parser.promptOptionsMenu(activeMenu, new VoiceDialogueLine("You have no other choice."));
            switch (activeOutcome) {
                case "role":
                case "powerful":
                case "selfDetermination":
                    mainScript.runSection(activeOutcome);
                    break;

                case "questions":
                    activeMenu.setCondition("happened", false);
                case "happened":
                    activeMenu.setCondition("questions", false);
                    priestOffer = true;
                    mainScript.runSection("questions");
                    break;

                case "refuseNoBladeA":
                case "refuseNoBladeB":
                    if (!manager.confirmContentWarnings(Chapter.APOTHEOSIS, "forced suicide")) {
                        activeMenu.setGreyedOut("refuseNoBladeA", true);
                        activeMenu.setGreyedOut("refuseNoBladeB", true);
                        break;
                    }

                    mainScript.runSection("refuseNoBlade");
                    mainScript.runSection();
                    return this.towerSlaySelf(false, true);

                case "refuseBlade":
                    mainScript.runSection("refuseBlade");
                    return this.towerResistBlade(resistCount, submitCount, priestOffer);

                case "pledge":
                    mainScript.runSection("menuPledge");
                    this.towerPledge(tookBlade);
                    return ChapterEnding.OBEDIENTSERVANT;
            }
        }

        throw new RuntimeException("No ending reached");
    }

    /**
     * The player pledges themself to the Tower, leading to the "Your Obedient Servant" ending and claiming the Tower as a vessel
     */
    private void towerPledge(boolean tookBlade) {
        mainScript.runSection("pledge");
        mainScript.runSection("pledgeCont");

        if (tookBlade) {
            mainScript.runSection("pledgeBlade");
        } else {
            mainScript.runSection("pledgeNoBlade");
        }

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "believe", "(Explore) \"And what if I don't believe? What happens then?\""));
        activeMenu.add(new Option(this.manager, "sorry", "(Explore) I have to. It's over. I'm sorry."));
        activeMenu.add(new Option(this.manager, "break", "[Break her chains.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu, new VoiceDialogueLine(Voice.NARRATORPRINCESS, "You have no other choice."))) {
                case "believe":
                    mainScript.runSection("pledgeBelieve");
                    break;

                case "sorry":
                    mainScript.runSection("pledgeSorry");
                    break;

                case "break":
                    this.repeatActiveMenu = false;
                    break;
            }
        }

        this.quietCreep();
        mainScript.runSection("pledgeBreak");

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "explore", "(Explore) \"What happens now?\""));
        activeMenu.add(new Option(this.manager, "take", "[Take my hand.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu, new VoiceDialogueLine(Voice.NARRATORPRINCESS, "You have no other choice."))) {
                case "explore":
                    mainScript.runSection("pledgeExplore");
                    break;

                case "take":
                    this.repeatActiveMenu = false;
                    break;
            }
        }

        if (this.isFirstVessel) {
            mainScript.runSection("pledgeEndFirstVessel");
        } else {
            mainScript.runSection("pledgeEndNotFirstVessel");
        }
    }

    /**
     * The player attempts to resist the Tower after bringing down the blade, leading to Chapter III: The Apotheosis / The Fury
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding towerResistBlade(int resistCount, int submitCount, boolean priestOffer) {
        mainScript.runSection("resistBlade");

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "why", true, "(Explore) \"Why? What are you going to try and make me do with it?\""));
        activeMenu.add(new Option(this.manager, "kill", true, "(Explore) \"I'm going to kill you.\""));
        activeMenu.add(new Option(this.manager, "divine", true, "(Explore) You're not kidding about that divine hand. Who's doing this? Is it her? Is it you?"));
        activeMenu.add(new Option(this.manager, "no", true, "\"No.\""));
        activeMenu.add(new Option(this.manager, "take", "[Pick up the blade.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "cTake":
                case "take":
                    this.repeatActiveMenu = false;
                    break;

                default: this.giveDefaultFailResponse();
            }
        }

        this.hasBlade = true;
        mainScript.runSection();

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "hate", true, "\"I hate you so much. All of you.\""));
        activeMenu.add(new Option(this.manager, "stop", true, "\"Stop making me do things!\""));
        activeMenu.add(new Option(this.manager, "kneel", true, "\"I'm going to kneel here forever.\""));
        activeMenu.add(new Option(this.manager, "stand", "[Stand.]"));

        parser.promptOptionsMenu(activeMenu);
        mainScript.runSection();

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "end", "[End this.]"));

        parser.promptOptionsMenu(activeMenu);
        mainScript.runSection();
        
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "self", "[Slay yourself.]", 0));
        activeMenu.add(new Option(this.manager, "resist", "[Resist.]", resistCount >= 3 && submitCount > 0));
        activeMenu.add(new Option(this.manager, "slay", "[Slay the Princess.]", submitCount == 0));

        this.canSlayPrincess = true;
        this.canSlaySelf = true;
        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "cSlaySelf":
                case "self":
                    if (!manager.confirmContentWarnings(Chapter.APOTHEOSIS)) break;
                    
                    mainScript.runSection("bladeSlaySelf");
                    return this.towerSlaySelf(true, false);

                case "cSlayPrincess":
                case "resist":
                case "slay":
                    this.repeatActiveMenu = false;
                    break;

                default: this.giveDefaultFailResponse();
            }
        }

        mainScript.runSection("advance1");
        
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "self", "[Slay yourself.]", 0));
        activeMenu.add(new Option(this.manager, "push", "[Push forward.]", resistCount >= 3 && submitCount > 0));
        activeMenu.add(new Option(this.manager, "slay", "[Slay the Princess.]", submitCount == 0));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "cSlaySelf":
                case "self":
                    if (!manager.confirmContentWarnings(Chapter.APOTHEOSIS)) break;

                    mainScript.runSection("advanceSlaySelf");
                    return this.towerSlaySelf(true, false);

                case "cSlayPrincess":
                case "push":
                case "slay":
                    this.repeatActiveMenu = false;
                    break;

                default: this.giveDefaultFailResponse();
            }
        }

        this.canSlaySelf = false;
        if (submitCount > 0) this.canSlayPrincess = false;
        mainScript.runSection("advance2");

        if (priestOffer) {
            mainScript.runSection("advance2Offer");
        } else {
            mainScript.runSection("advance2NoOffer");
        }
        
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "pledge", "\"I'm yours.\" [Pledge yourself to her.]"));
        activeMenu.add(new Option(this.manager, "resist", "\"I. Said. NO!\" [Resist.]", 0, submitCount > 0));
        activeMenu.add(new Option(this.manager, "slay", "[Slay the Princess.]", submitCount == 0));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "pledge":
                    mainScript.runSection("advancePledge");
                    if (!this.knowsDestiny) mainScript.runSection("advancePledgeShare");
                    this.towerPledge(true);
                    return ChapterEnding.OBEDIENTSERVANT;

                case "resist":
                    if (!manager.confirmContentWarnings(Chapter.APOTHEOSIS)) break;
                    
                    return this.towerSlaySelf(true, false);

                case "cSlayPrincess":
                case "slay":
                    if (manager.hasVisited(Chapter.FURY)) {
                        parser.printDialogueLine(WORNPATH);
                        parser.printDialogueLine(WORNPATHHERO);

                        this.canSlayPrincess = false;
                        activeMenu.setGreyedOut("slay", true);
                        activeMenu.setCondition("resist", true);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.FURY)) {
                        this.canSlayPrincess = false;
                        activeMenu.setGreyedOut("slay", true);
                        activeMenu.setCondition("resist", true);
                        break;
                    }

                    this.repeatActiveMenu = false;
                    break;

                default: this.giveDefaultFailResponse();
            }
        }

        mainScript.runSection("advanceSlay");
        return ChapterEnding.GODKILLER;
    }

    /**
     * The Tower forces the player to kill themself, leading to Chapter III: The Apotheosis
     * @param tookBlade whether the player took the blade in the cabin
     * @return the Chapter ending reached by the player (indicating which Voice to gain in The Apotheosis)
     */
    private ChapterEnding towerSlaySelf(boolean tookBlade, boolean lateJoin) {

        if (!lateJoin) {
            mainScript.runSection("slaySelf");
            mainScript.runSection("slaySelfCont");
        }

        mainScript.runSection("slaySelfJoin");

        if (tookBlade) {
            return ChapterEnding.APOBLADE; // Voice of the Contrarian
        } else {
            return ChapterEnding.APOUNARMED; // Voice of the Paranoid
        }
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

        mainScript.runSection();

        if (this.hasBlade) {
            mainScript.runSection("bodyCommentBlade");
        } else {
            mainScript.runSection("bodyCommentNoBlade");
        }

        Condition isHostile = new Condition(false);
        InverseCondition isSoft = new InverseCondition(isHostile);

        this.canSlayPrincess = true;
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "slay", "[Slay the Princess.]", this.hasBlade));
        activeMenu.add(new Option(this.manager, "grab", "[Grab her.]", !this.hasBlade));
        activeMenu.add(new Option(this.manager, "wait", "[Wait and see how things play out.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);

            switch (activeOutcome) {
                case "cSlayPrincess":
                case "slay":
                    this.repeatActiveMenu = false;
                    this.isHarsh = true;
                    isHostile.set(true);
                    mainScript.runSection("softSlay");
                    break;

                case "cSlayPrincessNoBladeFail":
                case "grab":
                    this.repeatActiveMenu = false;
                    this.isHarsh = true;
                    isHostile.set(true);
                    mainScript.runSection("softGrab");
                    break;

                case "wait":
                    this.repeatActiveMenu = false;
                    
                    if (this.hasBlade) {
                        mainScript.runSection("basementStartWaitBlade");
                    } else {
                        mainScript.runSection("basementStartWaitNoBlade");
                    }

                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Conversation menu

        boolean shareDied = false;
        boolean deathComment = false;
        Condition narratorUnconfirmed = new Condition(true);
        Condition noBonesAsk = new Condition(true);
        Condition noApology = new Condition(true);
        Condition noIfOnly = new Condition(true);
        Condition noWorldEndExplore = new Condition(true);
        Condition thoughtsHarsh = new Condition(false);
        Condition homeComment = new Condition(false);
        InverseCondition noHomeComment = new InverseCondition(homeComment);
        Condition possessionAsk = new Condition(false);
        InverseCondition noPossessionAsk = new InverseCondition(possessionAsk);

        OptionsMenu subMenu;
        boolean repeatSub;

        // 22 EXPLORE OPTIONS + 6 ACTION OPTIONS IN SOFT MENU. GOD DAMN.
        // THERE ARE 35 TOTAL OPTIONS IN THIS MENU. SEND HELP
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "confirmLoop", "(Explore) See, this is exactly what I was trying to tell you about in the woods. This already happened. We killed her.", this.sharedLoop, narratorUnconfirmed, isSoft));
        activeMenu.add(new Option(this.manager, "notDead", "(Explore) \"I killed you! What are you doing not being dead?\"", isSoft));
        activeMenu.add(new Option(this.manager, "body", "(Explore) \"Your body's right there, though. Your *dead* body.\"", activeMenu.get("notDead"), isSoft));
        activeMenu.add(new Option(this.manager, "whyBack", "(Explore) \"Do you know why you came back?\"", isSoft));
        activeMenu.add(new Option(this.manager, "supposed", "(Explore) \"And where are you supposed to be?\"", activeMenu.get("whyBack"), noHomeComment, isSoft));
        activeMenu.add(new Option(this.manager, "help", "(Explore) \"Is there any way I can help you get home? Do you need me to bury those bones?\"", homeComment, noPossessionAsk, noBonesAsk, isSoft));
        activeMenu.add(new Option(this.manager, "ifOnlyHarsh", "(Explore) \"If I knew I'd have to talk to you again, I wouldn't have slain you.\"", noIfOnly, isHostile));
        activeMenu.add(new Option(this.manager, "victim", "(Explore) \"Stop playing the victim. You threatened me last time.\""));
        activeMenu.add(new Option(this.manager, "sorryA", "(Explore) \"I'm sorry I killed you last time, I shouldn't have done that.\"", noApology));
        activeMenu.add(new Option(this.manager, "grovel", "(Explore) \"Do you want me to die? Do you want me to kill myself to satisfy some sort of sick revenge fantasy? Because I already did that and it wouldn't be hard to do it again.\""));
        activeMenu.add(new Option(this.manager, "sorryB", "(Explore) \"I'm sorry. Is there any way I can make it up to you?\"", noApology));
        activeMenu.add(new Option(this.manager, "trick", "(Explore) \"The people who wanted you dead tricked me, and the enemy of my enemy is my friend. Let's team up.\"", noHomeComment));
        activeMenu.add(new Option(this.manager, "wantA", "(Explore) \"What do you want from me?\"", noPossessionAsk, isSoft));
        activeMenu.add(new Option(this.manager, "ifOnlySoft", "(Explore) \"If I knew I'd wind up having to talk to you again, I wouldn't have slain you.\"", noIfOnly, isSoft));
        activeMenu.add(new Option(this.manager, "alsoDead", "(Explore) \"I died too and I'm not floating around like you are. What happened? Why am I different? Why are you different?\""));
        activeMenu.add(new Option(this.manager, "bonesAsk", "(Explore) \"What if I buried your bones outside?\"", thoughtsHarsh, noBonesAsk));
        activeMenu.add(new Option(this.manager, "worldEndHarsh", "(Explore) \"Of course I attacked you. You're supposed to end the world. That's why I killed you last time, too.\"", noWorldEndExplore, isHostile));
        activeMenu.add(new Option(this.manager, "howHurt", "(Explore) \"You're dead. Or at least mostly dead. What can you even do to hurt me?\""));
        activeMenu.add(new Option(this.manager, "teleport", "(Explore) \"After I killed you, this cabin... I want to say it teleported? It wasn't in the woods anymore, time stopped meaning anything, and I had to kill myself to escape.\"", isSoft));
        activeMenu.add(new Option(this.manager, "worldEndA", "(Explore) \"Before I agree to anything, we need to talk about what happens after you leave this place. I was told you'd end the world.\"", noWorldEndExplore, possessionAsk, isSoft));
        activeMenu.add(new Option(this.manager, "worldEndB", "(Explore) \"I guess I should tell you why I was sent to kill you. You were going to end the world.\"", noWorldEndExplore, isSoft));
        activeMenu.add(new Option(this.manager, "worldEndC", "(Explore) \"I was told you were going to end the world.\"", noWorldEndExplore, isSoft));
        activeMenu.add(new Option(this.manager, "wantB", "(Explore) \"Okay, clearly slaying you isn't going to work. What do you want?\"", noPossessionAsk, isSoft));
        activeMenu.add(new Option(this.manager, "wantHarsh", "(Explore) \"What do you want from me?\"", noPossessionAsk, isHostile));
        activeMenu.add(new Option(this.manager, "walls", "(Explore) \"If you can go through walls, can't you just leave on your own?\"", homeComment, isSoft));
        activeMenu.add(new Option(this.manager, "thoughts", "(Explore) Okay team, I'm out of ideas. Thoughts?"));
        activeMenu.add(new Option(this.manager, "possess", "\"Okay. I've given it enough thought. Let's get you out of here.\" [Let the Princess possess you.]", possessionAsk));
        activeMenu.add(new Option(this.manager, "refuse", manager.demoMode(), "\"Okay. I've given it enough thought. The answer is no. I can't let you out, and I won't let you possess me.\"", 0, possessionAsk));
        activeMenu.add(new Option(this.manager, "smashBones", manager.demoMode(), "\"[Smash her bones.]\"", 0, thoughtsHarsh));
        activeMenu.add(new Option(this.manager, "slayHarsh", manager.demoMode(), "\"[Slay the Princess, harder.]\"", 0, this.hasBlade, isHostile));
        activeMenu.add(new Option(this.manager, "grabHarsh", manager.demoMode(), "\"[Grab the Princess, but try harder.]\"", 0, !this.hasBlade, isHostile));
        activeMenu.add(new Option(this.manager, "leaveSoft", manager.demoMode(), "\"If you're dead, then there really isn't much for me to do, is there? I guess I'll get going.\" [Leave her in the basement.]", 0, isSoft));
        activeMenu.add(new Option(this.manager, "leaveHarsh", manager.demoMode(), "\"Fine. If I can't hurt you, then there really isn't anything for me to do here. I guess I'll get going.\" [Leave her in the basement.]", 0, isHostile));
        activeMenu.add(new Option(this.manager, "retrieve", manager.demoMode(), "\"Right. I don't think there's much more for us to talk about. I'm going to get my blade, and then the two of us can fight.\" [Retrieve the blade.]", 0, !this.hasBlade));
        activeMenu.add(new Option(this.manager, "slaySoft", "\"[Slay the Princess.]\"", this.hasBlade, isSoft));
        activeMenu.add(new Option(this.manager, "grabSoft", "\"[Grab the Princess.]\"", !this.hasBlade, isSoft));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);

            if (activeOutcome.equals("cSlayPrincess")) {
                if (!this.isHarsh) {
                    this.activeOutcome = "slaySoft";
                } else if (this.canJoint3) {
                    this.activeOutcome = "slayHarsh";
                }
            } else if (activeOutcome.equals("cSlayPrincessNoBladeFail")) {
                if (!this.isHarsh) {
                    this.activeOutcome = "grabSoft";
                } else if (this.canJoint3) {
                    // Could be interpreted as either smashBones or grabHarsh -- redirects to the most violent option available to you
                    if (thoughtsHarsh.check()) {
                        this.activeOutcome = "smashBones";
                    } else {
                        this.activeOutcome = "grabHarsh";
                    }
                }
            }

            switch (activeOutcome) {
                case "confirmLoop":
                    narratorUnconfirmed.set(true);
                    mainScript.runSection("confirmLoopMenu");

                    if (this.hasBlade) {
                        mainScript.runSection("confirmLoopBlade");
                    } else {
                        mainScript.runSection("confirmLoopNoBlade");
                    }

                    break;

                case "notDead":
                    deathComment = true;
                    mainScript.runSection("notDeadMenu");
                    if (this.mirrorComment || this.touchedMirror) mainScript.runSection("notDeadMirror");
                    mainScript.runSection("notDeadCont");
                    break;
                    
                case "sorryA":
                    noApology.set(false);
                case "body":
                case "whyBack":
                    mainScript.runSection(activeOutcome + "Menu");
                    break;
                    
                case "supposed":
                    homeComment.set(true);
                    mainScript.runSection("supposedMenu");
                    break;
                    
                case "help":
                    noBonesAsk.set(false);
                    possessionAsk.set(true);
                    mainScript.runSection("helpMenu");

                    switch (this.spectrePossessAsk(noWorldEndExplore, narratorUnconfirmed, shareDied, false)) {
                        case 1:
                            return this.spectrePossess();

                        case 2:
                            return this.spectreKill(false);
                    }

                    break;

                case "ifOnlySoft":
                case "ifOnlyHarsh":
                    noIfOnly.set(false);
                    mainScript.runSection("ifOnlyMenu");
                    break;
                    
                case "victim":
                    mainScript.runSection("victimMenu");

                    subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "defend", "(Explore) \"That knife could have been for anything!\""));
                    subMenu.add(new Option(this.manager, "return", "(Return) [Leave it at that.]"));

                    if (parser.promptOptionsMenu(subMenu).equals("defend")) {
                        mainScript.runSection("victimMenuPush");
                    }

                    break;
                    
                case "grovel":
                    shareDied = true;
                    mainScript.runSection("grovelMenu");

                    if (homeComment.check()) {
                        mainScript.runSection("grovelNoHomeComment");
                    } else {
                        homeComment.set(true);
                        mainScript.runSection("grovelHomeComment");
                    }
                    
                    break;
                    
                case "sorryB":
                    noApology.set(false);

                    if (possessionAsk.check()) {
                        mainScript.runSection("sorryBMenuAlreadyAsked");
                    } else {
                        mainScript.runSection("sorryBMenuPossessAsk");

                        switch (this.spectrePossessAsk(noWorldEndExplore, narratorUnconfirmed, shareDied, true)) {
                            case 1:
                                return this.spectrePossess();

                            case 2:
                                return this.spectreKill(false);
                        }
                    }

                    break;
                    
                case "trick":
                    homeComment.set(true);

                    if (!narratorUnconfirmed.check()) {
                        mainScript.runSection("trickMenuConfirmed");
                    } else if (this.sharedLoop) {
                        narratorUnconfirmed.set(false);
                        mainScript.runSection("trickMenuSharedLoop");
                    } else {
                        narratorUnconfirmed.set(false);
                        mainScript.runSection("trickMenuNoShare");

                        switch (this.spectrePossessAsk(noWorldEndExplore, narratorUnconfirmed, shareDied, true)) {
                            case 1:
                                return this.spectrePossess();

                            case 2:
                                return this.spectreKill(false);
                        }
                    }

                    break;
                    
                case "alsoDead":
                    shareDied = true;
                    mainScript.runSection("alsoDeadMenu");

                    if (possessionAsk.check()) {
                        mainScript.runSection("alsoDeadMenuAlreadyAsked");
                    } else if (!this.isHarsh) {
                        homeComment.set(true);
                        mainScript.runSection("alsoDeadMenuPossessAsk");
                    }

                    break;

                case "bonesAsk":
                    noBonesAsk.set(false);
                    mainScript.runSection("bonesAskMenu");
                    break;
                    
                case "howHurt":
                    if (deathComment) {
                        mainScript.runSection("howHurtMenuGhost");
                    } else {
                        mainScript.runSection("howHurtMenuDead");
                    }

                    break;
                    
                case "teleport":
                    shareDied = true;
                    mainScript.runSection("teleportMenu");
                    break;
                    
                case "walls":
                    mainScript.runSection("wallsMenu");

                    if (noPossessionAsk.check()) {
                        possessionAsk.set(true);

                        switch (this.spectrePossessAsk(noWorldEndExplore, narratorUnconfirmed, shareDied, true)) {
                            case 1:
                                return this.spectrePossess();

                            case 2:
                                return this.spectreKill(false);
                        }
                    }

                    break;
                    
                case "thoughts":
                    if (this.isHarsh) {
                        thoughtsHarsh.set(true);
                        mainScript.runSection("thoughtsMenuHarsh");
                    } else {
                        mainScript.runSection("thoughtsMenuSoft");

                        if (possessionAsk.check())
                        {
                            mainScript.runSection("thoughtsSoftAsked");
                        } else {
                            mainScript.runSection("thoughtsSoftNoAsk");
                        }
                    }

                    break;
                    
                case "worldEndA":
                case "worldEndB":
                case "worldEndC":
                case "worldEndHarsh":
                    noWorldEndExplore.set(false);
                    this.spectreShareTask(narratorUnconfirmed, shareDied);
                    break;
                    
                case "wantA":
                    possessionAsk.set(true);

                    if (homeComment.check()) {
                        mainScript.runSection("wantANoHomeComment");
                    } else {
                        homeComment.set(true);
                        mainScript.runSection("wantAHomeComment");
                    }

                    switch (this.spectrePossessAsk(noWorldEndExplore, narratorUnconfirmed, shareDied, true)) {
                        case 1:
                            return this.spectrePossess();

                        case 2:
                            return this.spectreKill(false);
                    }

                    break;
                    
                case "wantB":
                    homeComment.set(true);
                    possessionAsk.set(true);
                    mainScript.runSection("wantBMenu");

                    switch (this.spectrePossessAsk(noWorldEndExplore, narratorUnconfirmed, shareDied, true)) {
                        case 1:
                            return this.spectrePossess();

                        case 2:
                            return this.spectreKill(false);
                    }

                    break;

                case "wantHarsh":
                    possessionAsk.set(true);
                    mainScript.runSection("wantHarshMenu");

                    switch (this.spectrePossessAsk(noWorldEndExplore, narratorUnconfirmed, shareDied, true)) {
                        case 1:
                            return this.spectrePossess();

                        case 2:
                            return this.spectreKill(false);
                    }

                    break;
                    
                case "possess":
                    return this.spectrePossess();
                    
                case "refuse":
                    if (manager.hasVisited(Chapter.WRAITH)) {
                        this.spectreCantWraith();
                        parser.printDialogueLine(WORNPATH);
                        parser.printDialogueLine(WORNPATHHERO);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.WRAITH)) {
                        this.spectreCantWraith();
                        break;
                    }

                    if (this.isHarsh) {
                        mainScript.runSection("refuseHarsh");
                    } else {
                        mainScript.runSection("refuseSoft");
                    }

                    return this.spectreKill(false);

                case "smashBones":
                    if (manager.hasVisited(Chapter.WRAITH)) {
                        this.spectreCantWraith();
                        parser.printDialogueLine(WORNPATH);
                        parser.printDialogueLine(WORNPATHHERO);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.WRAITH)) {
                        this.spectreCantWraith();
                        break;
                    }

                    if (noBonesAsk.check()) {
                        mainScript.runSection("smashBonesNoAsk");
                    } else {
                        mainScript.runSection("smashBonesAsk");
                    }

                    return this.spectreKill(false);

                case "slayHarsh":
                    if (manager.hasVisited(Chapter.WRAITH)) {
                        this.spectreCantWraith();
                        parser.printDialogueLine(WORNPATH);
                        parser.printDialogueLine(WORNPATHHERO);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.WRAITH)) {
                        this.spectreCantWraith();
                        break;
                    }

                    mainScript.runSection("slayAgain");
                    return this.spectreKill(false);

                case "grabHarsh":
                    if (manager.hasVisited(Chapter.WRAITH)) {
                        this.spectreCantWraith();
                        parser.printDialogueLine(WORNPATH);
                        parser.printDialogueLine(WORNPATHHERO);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.WRAITH)) {
                        this.spectreCantWraith();
                        break;
                    }

                    mainScript.runSection("grabAgain");
                    return this.spectreKill(false);
                    
                case "cGoStairs":
                    if (!this.canJoint3) {
                        parser.printDialogueLine(DEMOBLOCK);
                        break;
                    }
                case "leaveSoft":
                case "leaveHarsh":
                    if (manager.hasVisited(Chapter.WRAITH)) {
                        this.spectreCantWraith();
                        parser.printDialogueLine(WORNPATH);
                        parser.printDialogueLine(WORNPATHHERO);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.WRAITH)) {
                        this.spectreCantWraith();
                        break;
                    }

                    mainScript.runSection("leaveAttempt");
                    return this.spectreKill(true);
                    
                case "retrieve":
                    if (manager.hasVisited(Chapter.WRAITH)) {
                        this.spectreCantWraith();
                        parser.printDialogueLine(WORNPATH);
                        parser.printDialogueLine(WORNPATHHERO);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.WRAITH)) {
                        this.spectreCantWraith();
                        break;
                    }

                    mainScript.runSection("retrieveAttempt");
                    return this.spectreKill(true);

                case "slaySoft":
                    this.isHarsh = true;
                    isHostile.set(true);
                    mainScript.runSection("softSlay");
                    break;

                case "grabSoft":
                    this.isHarsh = true;
                    isHostile.set(true);
                    mainScript.runSection("softGrab");
                    break;

                case "cSlayPrincessFail":
                case "cSlayPrincessNoBladeFail":
                    parser.printDialogueLine(DEMOBLOCK);
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        throw new RuntimeException("No ending reached");
    }

    /**
     * The player tells the Princess that she's allegedly going to end the world
     * @param narratorUnconfirmed whether the Narrator has finally agreed that you've been here before
     * @param shareDied whether the player has mentioned that they died
     */
    private void spectreShareTask(Condition narratorUnconfirmed, boolean shareDied) {
        OptionsMenu subMenu = new OptionsMenu(true);
        boolean repeatSub = true;

        if (this.isHarsh) {
            mainScript.runSection("worldEndHarshMenu");

            subMenu.add(new Option(this.manager, "end1", "(Explore) \"Well? Were you going to end the world? Would you end it, if you could?\""));
            subMenu.add(new Option(this.manager, "end2", "(Explore) \"Things end. Things have to end. This sentence just ended.\"", subMenu.get("end1")));
            subMenu.add(new Option(this.manager, "yesNo", "(Explore) \"It's a yes or no question. Do you want to end the world?\""));
            subMenu.add(new Option(this.manager, "return", "(Return) [Leave it at that.]"));

            while (repeatSub) {
                this.activeOutcome = parser.promptOptionsMenu(subMenu);
                switch (activeOutcome) {
                    case "end1":
                        mainScript.runSection("end1EndWorldHarsh");
                        if (shareDied) mainScript.runSection("end1HarshSharedDeath");
                        mainScript.runSection("end1HarshCont");
                        break;

                    case "end2":
                    case "yesNo":
                        mainScript.runSection(activeOutcome + "EndWorldHarsh");
                        break;

                    case "return":
                        repeatSub = false;
                        break;

                    default: super.giveDefaultFailResponse();
                }
            }
        } else {
            mainScript.runSection("worldEndSoftMenu");

            Condition firstOption = new Condition(true);
            subMenu.add(new Option(this.manager, "grovel2", "(Explore) Shit. Everyone sounds disappointed in me. I should grovel even more.", subMenu.get("grovel1")));
            subMenu.add(new Option(this.manager, "wrong", "(Explore) \"Obviously it was wrong of me to believe that. How could you have ended the world if all it took to kill you was a knife to the heart?\"", firstOption));
            subMenu.add(new Option(this.manager, "whatDo", "(Explore) \"What are you going to do if I help you get out of here?\""));
            subMenu.add(new Option(this.manager, "wereYou", "(Explore) \"Well, were you going to end the world?\""));
            subMenu.add(new Option(this.manager, "whatDo2", "(Explore) \"You didn't answer my question. Do you want to end the world?\""));
            subMenu.add(new Option(this.manager, "whatDo3", "(Explore) \"You still didn't answer my question. Even if you don't *want* to end it, does letting you out of here mean the world is going to end?\"", subMenu.get("whatDo2")));
            subMenu.add(new Option(this.manager, "grovel1", "(Explore) \"I'm not cold! I'm just... dumb! I'm just a big dumb stupid idiot! Stupid stupid stupid what was I thinking just believing what I was told?\"", firstOption));
            subMenu.add(new Option(this.manager, "return", "(Return) [Leave it at that.]"));

            while (repeatSub) {
                this.activeOutcome = parser.promptOptionsMenu(subMenu);
                switch (activeOutcome) {
                    case "grovel2":
                        if (this.hasBlade) {
                            mainScript.runSection("grovel2EndWorldSoftBlade");
                        } else {
                            mainScript.runSection("grovel2EndWorldSoftNoBlade");
                        }
                        
                        break;

                    case "wrong":
                        firstOption.set(false);

                        if (narratorUnconfirmed.check()) {
                            narratorUnconfirmed.set(false);
                            mainScript.runSection("wrongConfirmedEndWorldSoft");
                        }

                        mainScript.runSection("wrongEndWorldSoft");
                        break;

                    case "whatDo":
                    case "wereYou":
                    case "whatDo2":
                    case "grovel1":
                        firstOption.set(false);
                        break;

                    case "whatDo3":
                        mainScript.runSection("whatDo3WorldEndSoft");
                        if (shareDied) mainScript.runSection("whatDo3SharedDeath");
                        mainScript.runSection("whatDo3Cont");
                        break;
                    
                    case "return":
                        repeatSub = false;
                        break;

                    default: super.giveDefaultFailResponse();
                }
            }
        }
    }

    /**
     * The Spectre asks the player to let her possess them
     * @param noWorldEndExplore whether the player has already told the Spectre she's allegedly going to end the world
     * @param narratorUnconfirmed whether the Narrator has finally agreed that you've been here before
     * @param shareDied whether the player has mentioned that they died
     * @param lateJoin whether to skip the first bit of dialogue
     * @return 0 if the player decides to ask more questions; 1 if they agree to let the Spectre possess them; 2 otherwise
     */
    private int spectrePossessAsk(Condition noWorldEndExplore, Condition narratorUnconfirmed, boolean shareDied, boolean lateJoin) {
        if (this.isHarsh) {
            if (!lateJoin) mainScript.runSection("possessAskSoftEarlyJoin");
            mainScript.runSection("possessAskSoftJoin");
        } else {
            mainScript.runSection("possessAskHarshJoin");
        }

        String moodSuffix;
        String agreeDisplay;
        if (this.isHarsh) {
            moodSuffix = "Harsh";
            agreeDisplay = "\"No complaints here. Do it.\" [Let the Princess possess you.]";
        } else {
            moodSuffix = "Soft";
            agreeDisplay = "\"Sounds great. Do it.\" [Let the Princess possess you.]";
        }

        boolean trapSuggest = false;
        OptionsMenu possessMenu = new OptionsMenu(true);
        possessMenu.add(new Option(this.manager, "no", "(Explore) \"What if I say no?\""));
        possessMenu.add(new Option(this.manager, "wont", "(Explore) \"You *won't* hitch a ride if I say no, or you *can't* hitch a ride?\"", possessMenu.get("no"), !this.isHarsh));
        possessMenu.add(new Option(this.manager, "temp", "(Explore) \"This would just be temporary, right? You'll leave once we're out of the cabin?\""));
        possessMenu.add(new Option(this.manager, "control", "(Explore) \"If... if I let you in, do I still get to be in control?\""));
        possessMenu.add(new Option(this.manager, "worldEnd", "\"Before I agree to anything, we need to talk about what happens after you leave this place. I was told you'd end the world.\"", noWorldEndExplore));
        possessMenu.add(new Option(this.manager, "agree", agreeDisplay));
        possessMenu.add(new Option(this.manager, "refuse", manager.demoMode() || !this.canJoint3, "\"The answer's no.\""));
        possessMenu.add(new Option(this.manager, "return", "(Return) \"I need to think on this.\""));

        boolean repeatMenu = true;
        while (repeatMenu) {
            this.activeOutcome = parser.promptOptionsMenu(possessMenu);
            switch (activeOutcome) {
                case "no":
                    mainScript.runSection("noPossessAsk" + moodSuffix);

                    if (this.isHarsh) {
                        if (narratorUnconfirmed.check()) {
                            narratorUnconfirmed.set(false);
                            mainScript.runSection("noPossessAskNoConfirm");
                        } else {
                            mainScript.runSection("noPossessAskConfirmed");
                        }
                    }

                    break;

                case "wont":
                    mainScript.runSection("wontPossessAsk");
                    
                    if (!trapSuggest) {
                        trapSuggest = true;
                        mainScript.runSection("coldTrapSuggest");
                    }

                    break;

                case "temp":
                    mainScript.runSection("tempPossessAsk" + moodSuffix);

                    if (!this.isHarsh) {
                        if (trapSuggest) {
                            mainScript.runSection("tempNoSuggest");
                        } else {
                            trapSuggest = true;
                            mainScript.runSection("tempColdSuggest");
                        }
                    }

                    break;

                case "control":
                    mainScript.runSection("controlPossessAsk" + moodSuffix);

                    if (!this.isHarsh) {
                        if (trapSuggest) {
                            mainScript.runSection("controlNoSuggest");
                        } else {
                            trapSuggest = true;
                            mainScript.runSection("coldTrapSuggest");
                        }
                    }

                    break;

                case "worldEnd":
                    noWorldEndExplore.set(false);
                    this.spectreShareTask(narratorUnconfirmed, shareDied);
                    return 0;

                case "agree":
                    return 2;

                case "refuse":
                    if (manager.hasVisited(Chapter.WRAITH)) {
                        this.spectreCantWraith();
                        parser.printDialogueLine(WORNPATH);
                        parser.printDialogueLine(WORNPATHHERO);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.WRAITH)) {
                        this.spectreCantWraith();
                        break;
                    }

                    mainScript.runSection("askRefuse" + moodSuffix);
                    return 3;

                case "return":
                    mainScript.runSection("returnPossessAsk" + moodSuffix);
                    return 0;

                default: super.giveDefaultFailResponse();
            }
        }

        throw new RuntimeException("No conclusion reached");
    }

    /**
     * Force the player into the "Hitch a Ride" ending of The Spectre, either because the game is in demo mode or because they chose not to continue after seeing the Wraith's content warnings
     */
    private void spectreCantWraith() {
        this.canJoint3 = false;
        this.canSlayPrincess = false;
        activeMenu.setGreyedOut("refuse", true);
        activeMenu.setGreyedOut("smashBones", true);
        activeMenu.setGreyedOut("slayHarsh", true);
        activeMenu.setGreyedOut("grabHarsh", true);
        activeMenu.setGreyedOut("leaveSoft", true);
        activeMenu.setGreyedOut("leaveHarsh", true);
        activeMenu.setGreyedOut("retrieve", true);
    }

    /**
     * The player either offends the Spectre for a second time, refuses to let her possess them, or attempts to leave the basement; the Spectre kills them, leading to Chapter III: The Wraith
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding spectreKill(boolean leaveAttempt) {
        mainScript.runSection("killStart");

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "miss", "\"Did you miss?\""));
        activeMenu.add(new Option(this.manager, "afraid", "\"I'm not afraid of you.\""));
        activeMenu.add(new Option(this.manager, "silent", "[Stare at her in silence.]"));
        activeMenu.add(new Option(this.manager, "away", "[Step away.]"));

        this.activeOutcome = parser.promptOptionsMenu(activeMenu);
        switch (activeOutcome) {
            case "miss":
            case "afraid":
            case "away":
                mainScript.runSection(activeOutcome + "Kill");
                break;

            case "silent":
                // No extra dialogue
                break;
        }

        mainScript.runSection("killCont");

        // isHarsh is used here to indicate whether you tried to take the Princess down with you, which changes a couple dialogue lines in The Wraith
        if (this.hasBlade) {
            mainScript.runSection("killBladeStart");
            
            this.canSlayPrincess = true;
            this.activeMenu = new OptionsMenu();
            activeMenu.add(new Option(this.manager, "slay", "[Slay the Princess.]"));
            activeMenu.add(new Option(this.manager, "die", "[Die.]"));

            this.repeatActiveMenu = true;
            while (repeatActiveMenu) {
                switch (parser.promptOptionsMenu(activeMenu)) {
                    case "cSlayPrincess":
                    case "slay":
                        this.isHarsh = true;
                        mainScript.runSection("killBladeSlay");
                        break;

                    case "die":
                        this.isHarsh = false;
                        mainScript.runSection("killBladeDie");
                        break;
                }
            }
        } else {
            this.isHarsh = false;
            mainScript.runSection("killNoBlade");
        }

        if (leaveAttempt && !this.isHarsh) {
            return ChapterEnding.HEARTRIPPERLEAVE;
        } else {
            return ChapterEnding.HEARTRIPPER;
        }
    }

    /**
     * The player allows the Spectre to possess them
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding spectrePossess() {
        String moodSuffix = (this.isHarsh) ? "Harsh" : "Soft";

        mainScript.runSection("possessStart" + moodSuffix);
        mainScript.runSection("possessCont" + moodSuffix);
        mainScript.runSection("possessVoiceComment" + moodSuffix);
        mainScript.runSection("possessCont2" + moodSuffix);

        this.canSlayPrincess = true;
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "explore", "(Explore) I can't think straight... there's too much noise."));
        activeMenu.add(new Option(this.manager, "slay", manager.demoMode(), "[Slay the Princess.]", 0, this.hasBlade));
        activeMenu.add(new Option(this.manager, "leave", "[Leave the basement.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "explore":
                    mainScript.runSection("possessExplore" + moodSuffix);
                    break;

                case "cSlayPrincess":
                    if (manager.demoMode() || !this.canUnique3) {
                        parser.printDialogueLine(DEMOBLOCK);
                    }
                case "slay":
                    if (!manager.confirmContentWarnings(Chapter.DRAGON, "suicide")) {
                        this.canUnique3 = false;
                        activeMenu.setGreyedOut("slay", true);
                        break;
                    }

                    mainScript.runSection("exorcismStart");
                    mainScript.runSection("exorcismStart" + moodSuffix);
                    return ChapterEnding.EXORCIST;

                case "cGoStairs":
                case "leave":
                    this.repeatActiveMenu = false;
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        this.currentLocation = GameLocation.BASEMENT;
        if (!this.hasBlade) this.withBlade = true;
        boolean tookBladeStart = this.hasBlade;
        mainScript.runSection("possessUpstairs");
        mainScript.runSection("possessUpstairs" + moodSuffix);

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "take", "(Explore) [Take the blade.]", !this.hasBlade));
        activeMenu.add(new Option(this.manager, "slay", manager.demoMode() || !this.canUnique3, "[Slay the Princess.]", 0, this.hasBlade));
        activeMenu.add(new Option(this.manager, "cont", "[Trudge forward.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "cTake":
                case "take":
                    this.withBlade = false;
                    this.hasBlade = true;
                    activeMenu.setCondition("slay", true);
                    mainScript.runSection("possessTakeBlade");
                    break;

                case "cSlayPrincess":
                    if (manager.demoMode() || !this.canUnique3) {
                        parser.printDialogueLine(DEMOBLOCK);
                    }
                case "slay":
                    if (!manager.confirmContentWarnings(Chapter.DRAGON, "suicide")) {
                        this.canUnique3 = false;
                        activeMenu.setGreyedOut("slay", true);
                        break;
                    }

                    mainScript.runSection("exorcismStart");
                    mainScript.runSection("exorcismStart" + moodSuffix);
                    return ChapterEnding.EXORCIST;

                case "cGoHill":
                case "cont":
                    this.repeatActiveMenu = false;
                    break;

                case "cGoStairs":
                    mainScript.runSection("possessTurnAround" + moodSuffix);
                    break;

                case "cSlayPrincessNoBladeFail":
                    mainScript.runSection("possessSlaySuggest");
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        if (this.hasBlade && !tookBladeStart) {
            mainScript.runSection("possessForwardTookBlade");
        } else {
            mainScript.runSection("possessForwardOther");
        }

        mainScript.runSection("possessForward" + moodSuffix);

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "open", "[Open the door.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "cGoHill":
                case "open":
                    this.repeatActiveMenu = false;
                    break;

                default: super.giveDefaultFailResponse();
            }
        }

        mainScript.runSection();

        if (this.isFirstVessel) {
            mainScript.runSection("possessEndFirstVessel");
        } else {
            mainScript.runSection("possessEndNotFirstVessel");
        }

        return ChapterEnding.HITCHHIKER;
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
        activeMenu.add(new Option(this.manager, "q1", "(Explore) \"XXXXX\""));
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

        mainScript.runSection("encounterStart");

        OptionsMenu cantWildMenu = new OptionsMenu(true);
        cantWildMenu.add(new Option(this.manager, "dodge", "[Dodge.]", 0));

        int stallCount = 0;
        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "where", "(Explore) Where?"));
        activeMenu.add(new Option(this.manager, "chains", "(Explore) Don't you hear that clinking? She's in chains again. We're fine."));
        activeMenu.add(new Option(this.manager, "attack", "(Explore) \"You're about to attack me, aren't you? I can see right through you.\""));
        activeMenu.add(new Option(this.manager, "haveTo", "(Explore) \"We don't have to kill each other. You know that, right?\""));
        activeMenu.add(new Option(this.manager, "move", "[Move.]"));
        activeMenu.add(new Option(this.manager, "freeze", "[Stand still.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            if (stallCount >= 2) {
                if (!manager.hasVisited(Chapter.WILD)) {
                    this.repeatActiveMenu = false;
                    this.beastCantBeEaten(cantWildMenu);
                    break;
                }

                mainScript.runSection("eatenStartOther");
                return this.beastEaten(false, false);
            }

            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "where":
                case "chains":
                    stallCount += 1;
                    mainScript.runSection(activeOutcome);
                    break;

                case "attack":
                case "haveTo":
                    if (manager.hasVisited(Chapter.WILD)) return this.beastToDen(true, false);

                    mainScript.runSection("eatenStartOther");
                    return this.beastEaten(true, false);

                case "move":
                    this.repeatActiveMenu = false;
                    break;

                case "freeze":
                    if (!manager.hasVisited(Chapter.WILD)) {
                        this.repeatActiveMenu = false;
                        this.beastCantBeEaten(cantWildMenu);
                        break;
                    }

                    mainScript.runSection("eatenStartOther");
                    return this.beastEaten(false, false);

                case "cSlayPrincessNoBladeFail":
                case "cSlayPrincessFail":
                    mainScript.runSection("attackFail");
                    break;

                default: mainScript.runSection("genericFail");
            }
        }

        mainScript.runSection("dodge1");

        // Attack menu
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "explore", "(Explore) How exactly are we supposed to take back the momentum here?"));
        activeMenu.add(new Option(this.manager, "dodge", "[Survive.]", 0));
        activeMenu.add(new Option(this.manager, "flee", "[Run for the stairs.]", false));
        activeMenu.add(new Option(this.manager, "attack", manager.demoMode(), "[Wait for her to strike, and hit her back.]"));
        activeMenu.add(new Option(this.manager, "playDead", !this.canJoint3, "[Play dead.]"));
        activeMenu.add(new Option(this.manager, "freeze", !this.canJoint3, "[Stand still.]"));
        
        int stallLimit = 2;
        boolean stairsGuarded = true;
        boolean canTryFlee = true;
        ChapterEnding attackResult;
        OptionsMenu parentMenu = new OptionsMenu();
        parentMenu.add(new Option(this.manager, "tired", "(Explore) I don't think I can keep this up.", false));
        parentMenu.add(new Option(this.manager, "what", "(Explore) \"What do you want?\""));
        parentMenu.add(new Option(this.manager, "whyKill", "(Explore) \"But why? Why do you want to kill me?\"", parentMenu.get("what")));
        parentMenu.add(new Option(this.manager, "whyEat", "(Explore) \"Okay, fine. Why do you want to eat me?\"", parentMenu.get("whyKill")));
        parentMenu.add(new Option(this.manager, "help", "(Explore) \"We don't have to kill each other. What if I helped you? What if we left together? If you could get out of here on your own, wouldn't you have already left?\""));
        parentMenu.add(new Option(this.manager, "hide", "(Explore) \"Stop hiding and show yourself.\""));
        parentMenu.add(new Option(this.manager, "threat", "(Explore) \"I was sent to kill you because you're a threat to the world. I'm starting to believe that's true.\""));
        parentMenu.add(new Option(this.manager, "deflect", "(Explore) \"You're deflecting.\"", false));
        parentMenu.add(new Option(this.manager, "flee", "[Run for the stairs.]", 0));

        // Figure out how to put warnings for Den in here, considering the game forces you into it in several instances if you've already been to Wild...

        for (int phase = 2; phase < 5; phase++) {
            for (stallCount = 0; stallCount < stallLimit; stallCount++) {
                switch (parser.promptOptionsMenu(parentMenu)) {
                    case "hide":
                    case "threat":
                        parentMenu.setCondition("deflect", true);
                    case "tired":
                    case "whyKill":
                    case "whyEat":
                    case "help":
                    case "deflect":
                        mainScript.runSection(activeOutcome);
                        break;

                    case "what":
                        if (this.hasBlade) {
                            mainScript.runSection("whatBlade");
                        } else {
                            if (stairsGuarded) {
                                mainScript.runSection("whatGuarded");
                            } else {
                                mainScript.runSection("whatNoBlade");
                            }
                        }

                        break;

                    case "cGoStairs":
                        if (!canTryFlee) {
                            stallCount -= 1;
                            mainScript.runSection("cantFlee");
                            break;
                        }
                    case "flee":
                        if (stairsGuarded) {
                            stallCount -= 1;
                            canTryFlee = false;
                            parentMenu.setCondition("flee", false);
                            mainScript.runSection("tryFleeGuarded");
                        } else {
                            if (this.hasBlade) {
                                mainScript.runSection("tryFleeBlade");
                            } else {
                                mainScript.runSection("tryFleeNoBlade");
                            }

                            if (manager.hasVisited(Chapter.WILD)) {
                                mainScript.runSection("forceSkeptic");
                                return this.beastToDen(true, phase > 3);
                            } else {
                                mainScript.runSection("eatenStartRun");
                                return this.beastEaten(false, false);
                            }
                        }

                        break;

                    case "cSlayPrincessNoBladeFail":
                    case "cSlayPrincessFail":
                        stallCount -= 1;
                        mainScript.runSection("attackFail");
                        break;

                    default:
                        stallCount -= 1;
                        mainScript.runSection("genericFail");
                }
            }

            attackResult = this.beastAttack(phase);
            if (attackResult != null) return attackResult;

            this.canSlayPrincess = false;
            switch (phase) {
                case 2:
                    stairsGuarded = false;
                    canTryFlee = activeMenu.hasBeenPicked("flee");
                    activeMenu.setDisplay("dodge", "[Stay. Alive.]");
                    activeMenu.setCondition("explore", true);
                    activeMenu.setCondition("flee", true);
                    parentMenu.setCondition("tired", true);
                    parentMenu.setCondition("flee", canTryFlee);
                    break;

                case 3:
                    canTryFlee = true;
                    stairsGuarded = true;
                    activeMenu.setDisplay("dodge", "[Again...]");
                    activeMenu.setCondition("flee", false);
                    parentMenu.setCondition("flee", true);
                    stallLimit = 1;

                    if (manager.demoMode()) activeMenu.setGreyedOut("dodge", true);
                    break;
            }
        }

        return this.beastToDen(true, true);
    }

    /**
     * Runs one phase of the Beast's attack, allowing the player to choose a response
     * @param phase the current phase of the fight
     * @return the Chapter ending reached by the player if they trigger one, or null if the fight continues
     */
    private ChapterEnding beastAttack(int phase) {
        mainScript.runSection("attack" + phase);

        if (!manager.demoMode()) this.canSlayPrincess = true;
        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "explore":
                    if (this.hasBlade) {
                        mainScript.runSection("attackExploreBlade");
                    } else {
                        mainScript.runSection("attackExploreNoBlade");
                    }

                    break;

                case "dodge":
                    mainScript.runSection("dodge" + phase);
                    return null;

                case "cGoStairs":
                    if (phase != 3) {
                        mainScript.runSection("tryFleeGuarded");
                        break;
                    }
                case "flee":
                    if (this.hasBlade) {
                        mainScript.runSection("tryFleeBlade");
                    } else {
                        mainScript.runSection("tryFleeNoBlade");
                    }

                    if (manager.hasVisited(Chapter.WILD)) {
                        mainScript.runSection("forceSkeptic");
                        return this.beastToDen(true, phase > 3);
                    } else {
                        mainScript.runSection("eatenStartRun");
                        return this.beastEaten(false, false);
                    }

                case "cSlayPrincess":
                case "attack":
                    return this.beastToDen(false, phase > 3);

                case "playDead":
                    if (manager.hasVisited(Chapter.WILD)) {
                        this.canJoint3 = false;
                        activeMenu.setGreyedOut("freeze", true);
                        parser.printDialogueLine(WORNPATH);
                        parser.printDialogueLine(WORNPATHHERO);
                        break;
                    } else {
                        mainScript.runSection("eatenStartPlayDead");
                        return this.beastEaten(false, true);
                    }

                case "freeze":
                    if (manager.hasVisited(Chapter.WILD)) {
                        this.canJoint3 = false;
                        activeMenu.setGreyedOut("playDead", true);
                        parser.printDialogueLine(WORNPATH);
                        parser.printDialogueLine(WORNPATHHERO);
                        break;
                    } else {
                        mainScript.runSection("eatenStartOther");
                        return this.beastEaten(false, false);
                    }

                case "cSlayPrincessNoBladeFail":
                    mainScript.runSection("noWeapon");
                    break;

                case "cSlayPrincessFail":
                    parser.printDialogueLine(DEMOBLOCK);
                    break;

                default: mainScript.runSection("genericFail");
            }
        }

        throw new RuntimeException("No ending reached");
    }

    /**
     * Overrides the player's decision if they would be eaten by the Beast but have already encountered the Wild
     * @param cantWildMenu the options menu to show to the player
     */
    private void beastCantBeEaten(OptionsMenu cantWildMenu) {
        this.canJoint3 = false;
        parser.printDialogueLine(WORNPATH);
        parser.promptOptionsMenu(cantWildMenu, new DialogueLine("[You have no other option.]"));
        parser.printDialogueLine(WORNPATHHERO);
    }

    /**
     * The player manages to avoid being eaten by the Beast, leading to Chapter III: The Den
     * @param skepticPath whether the player continued dodging the Beast's attacks (granting them the Voice of the Skeptic) or fought back (granting them the Voice of the Stubborn)
     * @param wounded whether the player has already been wounded by the Beast
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding beastToDen(boolean skepticPath, boolean wounded) {
        if (skepticPath) {
            if (wounded) {
                mainScript.runSection("denSkepticWounded");
            } else {
                mainScript.runSection("denSkepticUnharmed");
            }

            return ChapterEnding.FLIGHT;
        } else {
            if (wounded && !manager.hasVisited(Chapter.WILD)) {
                mainScript.runSection("eatenStartFight");
                return this.beastEaten(false, false);
            } else {
                mainScript.runSection("denStubbornEnd");
                return ChapterEnding.FIGHT;
            }
        }
    }

    /**
     * The Beast pounces and swallows the player whole
     * @param talked whether the player talked in the initial encounter menu
     * @param playedDead whether the player played dead (guaranteeing them the Voice of the Contrarian in Chapter III: The Wild)
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding beastEaten(boolean talked, boolean playedDead) {
        if (talked) mainScript.runSection("eatenStartTalked");
        mainScript.runSection("eatenStartCont");

        if (this.hasBlade) {
            mainScript.runSection("eatenStartBlade");
        } else {
            mainScript.runSection("eatenStartNoBlade");
        }

        int beastHP = 4;
        boolean incrementFlag;
        this.canSlayPrincess = true;
        this.canSlaySelf = this.hasBlade && !manager.demoMode();
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "cabin", "(Explore) \"Can you talk to the cabin?\"", 0));
        activeMenu.add(new Option(this.manager, "asked", "(Explore) \"You could have asked me before swallowing me alive.\"", 0));
        activeMenu.add(new Option(this.manager, "threatExplore", "(Explore) \"Spit me out or I'll kill myself and nobody gets to leave.\"", 0, this.hasBlade));
        activeMenu.add(new Option(this.manager, "need", "(Explore) \"You need me to want to free you, don't you? You can't force me to let you out of here.\"", 0, false));
        activeMenu.add(new Option(this.manager, "freedom", "(Explore) \"Just because I want freedom, doesn't mean I want to give you yours.\"", false));
        activeMenu.add(new Option(this.manager, "threatA", "(Explore) \"I have my steel claw. I could use it to hurt you.\"", 0, this.hasBlade));
        activeMenu.add(new Option(this.manager, "threatB", "(Explore) \"I've found your heart. Spit me out, or I will end you.\"", 0, false));
        activeMenu.add(new Option(this.manager, "claw", "(Explore) [Claw and bite.]", 0, !this.hasBlade));
        activeMenu.add(new Option(this.manager, "dig", "(Explore) [Dig with the steel claw.]", 0, this.hasBlade));
        activeMenu.add(new Option(this.manager, "slay", manager.demoMode(), "[Slay the Princess.]", 0, false));
        activeMenu.add(new Option(this.manager, "surrenderA", "\"Fine! You can leave.\""));
        activeMenu.add(new Option(this.manager, "surrenderB", "\"You can leave! Just let me go.\""));
        activeMenu.add(new Option(this.manager, "suicide", manager.demoMode(), "\"Screw you!\" [Slay yourself.]", 0, this.hasBlade));
        activeMenu.add(new Option(this.manager, "wait", manager.demoMode(), "[Wait for death.]", 0));

        for (int turnCount = 0; turnCount < 3; turnCount++) {
            incrementFlag = true;

            this.activeOutcome = parser.promptOptionsMenu(activeMenu);

            if (activeOutcome.equals("cSlayPrincess")) {
                if (beastHP == 0) {
                    this.activeOutcome = "slay";
                } else {
                    this.activeOutcome = "dig";
                }
            }

            switch (activeOutcome) {
                case "cabin":
                case "asked":
                case "threatExplore":
                case "need":
                    if (turnCount == 2) {
                        if (!manager.confirmContentWarnings(Chapter.WILD)) {
                            incrementFlag = false;
                            this.beastForceDissolvedWill();
                            break;
                        }
                    }

                    activeMenu.setCondition(activeOutcome, false);
                    mainScript.runSection(activeOutcome + "Eaten");
                    break;
                    
                case "threatA":
                case "threatB":
                    if (turnCount == 2) {
                        if (!manager.confirmContentWarnings(Chapter.WILD)) {
                            incrementFlag = false;
                            this.beastForceDissolvedWill();
                            break;
                        }
                    }

                    activeMenu.setCondition("threatA", false);
                    activeMenu.setCondition("threatB", false);
                    mainScript.runSection("threatEaten");
                    break;
                    
                case "cSlayPrincessNoBladeFail":
                case "claw":
                    beastHP -= 1;
                    mainScript.runSection("eatenClaw" + (4 - beastHP));
                    break;
                    
                case "dig":
                    beastHP -= 2;

                    if (beastHP == 2) {
                        activeMenu.setCondition("threatA", false);
                        mainScript.runSection("eatenClaw1");
                    } else {
                        activeMenu.setCondition("dig", false);
                        activeMenu.setCondition("slay", true);
                        mainScript.runSection("eatenClaw2");
                    }
                    
                    break;
                    
                case "freedom":
                    mainScript.runSection("freedomEaten");
                    this.quietCreep();
                    mainScript.runSection();

                    if (this.isFirstVessel) {
                        mainScript.runSection("surrenderFirstVessel");
                    } else {
                        mainScript.runSection("surrenderNotFirstVessel");
                    }

                    return ChapterEnding.DISSOLVINGWILLACCIDENT;
                    
                case "surrenderA":
                case "surrenderB":
                    mainScript.runSection("eatenSurrender");
                    this.quietCreep();
                    mainScript.runSection();

                    if (this.isFirstVessel) {
                        mainScript.runSection("surrenderFirstVessel");
                    } else {
                        mainScript.runSection("surrenderNotFirstVessel");
                    }

                    return ChapterEnding.DISSOLVINGWILL;
                    
                case "slay":
                    if (!manager.confirmContentWarnings(Chapter.WILD)) {
                        incrementFlag = false;
                        this.beastForceDissolvedWill();
                        break;
                    }

                    mainScript.runSection("dissolvedSlay");
                    if (playedDead) {
                        return ChapterEnding.OPOSSUM;
                    } else {
                        return ChapterEnding.AHAB;
                    }
                    
                case "cSlaySelf":
                case "suicide":
                    if (!manager.confirmContentWarnings(Chapter.WILD)) {
                        incrementFlag = false;
                        this.beastForceDissolvedWill();
                        break;
                    }

                    mainScript.runSection("dissolvedSuicide");
                    if (playedDead) {
                        return ChapterEnding.OPOSSUM;
                    } else {
                        return ChapterEnding.SLAYYOURSELF;
                    }
                    
                case "wait":
                    if (!manager.confirmContentWarnings(Chapter.WILD)) {
                        incrementFlag = false;
                        this.beastForceDissolvedWill();
                        break;
                    }

                    mainScript.runSection("dissolvedWait");
                    if (playedDead) {
                        return ChapterEnding.OPOSSUM;
                    } else {
                        return ChapterEnding.DISSOLVED;
                    }
                    
                case "cSlayPrincessFail":
                case "cSlaySelfFail":
                    incrementFlag = false;
                    parser.printDialogueLine(DEMOBLOCK);
                    break;
                    
                case "cGoStairs":
                case "cGoFail":
                    incrementFlag = false;
                    mainScript.runSection("eatenGoFail");
                    break;
                    
                case "cSlaySelfNoBladeFail":
                    incrementFlag = false;
                    mainScript.runSection("eatenSuicideFail");
                    break;

                default:
                    incrementFlag = false;
                    mainScript.runSection("genericFail");
            }

            if (incrementFlag) {
                switch (turnCount) {
                    case 0:
                        activeMenu.setCondition("need", true);
                        activeMenu.setCondition("freedom", true);

                        mainScript.runSection("eatenTurn0");
                        if (beastHP == 4) mainScript.runSection("eatenTurn0NoAttack");
                        break;

                    case 1:
                        if (manager.demoMode()) this.beastForceDissolvedWill();

                        mainScript.runSection("eatenTurn1");
                        break;
                }
            } else {
                turnCount -= 1;
            }
        }

        // Ran out of time, dissolved (leads to Chapter III: The Wild with the Voice of the Broken or the Contrarian)
        mainScript.runSection("dissolvedOutOfTime");

        if (playedDead) {
            return ChapterEnding.OPOSSUM;
        } else {
            return ChapterEnding.DISSOLVED;
        }
    }

    /**
     * Force the player into the "Dissolved Will" ending of The Beast, either because the game is in demo mode or because they chose not to continue after seeing the Wild's content warnings
     */
    private void beastForceDissolvedWill() {
        this.canJoint3 = false;
        this.canSlayPrincess = false;
        this.canSlaySelf = false;
        activeMenu.setGreyedOut("cabin", true);
        activeMenu.setGreyedOut("asked", true);
        activeMenu.setGreyedOut("threatExplore", true);
        activeMenu.setGreyedOut("need", true);
        activeMenu.setGreyedOut("freedom", true);
        activeMenu.setGreyedOut("threatA", true);
        activeMenu.setGreyedOut("threatB", true);
        activeMenu.setGreyedOut("claw", true);
        activeMenu.setGreyedOut("dig", true);
        activeMenu.setGreyedOut("slay", true);
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

        boolean mutualDeath = false;
        switch (this.prevEnding) {
            case TOWITCHLOCKED:
                this.source = "locked";
                if (!this.chapter2Intro(false, false, false)) return ChapterEnding.ABORTED;
                break;

            case TOWITCHMUTUAL:
                mutualDeath = true;
                this.source = "mutual";
                if (!this.chapter2Intro(true, true, false)) return ChapterEnding.ABORTED;
                break;

            case TOWITCHBETRAYAL:
                mutualDeath = true;
                this.source = "betrayal";
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

        if (this.sharedLoopInsist) {
            mainScript.runSection("basementSharedLoopInsist");
        } else if (this.sharedLoop) {
            mainScript.runSection("basementSharedLoop");
        } else {
            mainScript.runSection("basementNoShare");
        }

        boolean talked = false;
        boolean heartComment = false;
        Condition witchFree = new Condition(false);
        InverseCondition witchChained = witchFree.getInverse();
        Condition apologized = new Condition(false);
        InverseCondition noApology = apologized.getInverse();
        Condition leaveMentioned = new Condition(false);
        InverseCondition leaveNotMentioned = leaveMentioned.getInverse();
        Condition noStall = new Condition(true);

        this.canSlayPrincess = true;
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "sorry", "(Explore) \"I'm sorry about last time.\"", noApology));
        activeMenu.add(new Option(this.manager, "mistake", "(Explore) \"Look, I made a mistake. We all make mistakes, right? I'm sure you've made mistakes.\"", noApology, leaveNotMentioned));
        activeMenu.add(new Option(this.manager, "getOutA", "(Explore) \"Don't worry, the blade isn't for you. Or, not for killing you. We've got to get you out somehow, right?\"", this.hasBlade, witchChained));
        activeMenu.add(new Option(this.manager, "notHappy", "(Explore) \"I get the sense that you're not happy with me.\""));
        activeMenu.add(new Option(this.manager, "scared", "(Explore) \"You scared me, okay? When you started gnawing your arm off, it scared me, so I stabbed you. Things got out of hand.\"", source.equals("betrayal"), noStall, noApology));
        activeMenu.add(new Option(this.manager, "goodWill", "(Explore) \"I'm unarmed. That's a gesture of good will! So why don't we talk it out?\"", !this.hasBlade, noStall, noApology));
        activeMenu.add(new Option(this.manager, "bygones", "(Explore) \"We both died last time. Can't bygones be bygones?\"", source.equals("betrayal"), noStall));
        activeMenu.add(new Option(this.manager, "locked", "(Explore) \"I didn't do shit to you last time. You're the one who locked me away until I died.\"", source.equals("locked"), noStall, leaveNotMentioned));
        activeMenu.add(new Option(this.manager, "trusted", "(Explore) \"I died last time. You didn't. If anyone here shouldn't be trusted, it's you!\"", !mutualDeath, noStall));
        activeMenu.add(new Option(this.manager, "messy", "(Explore) \"Look, I know, I know. Things got messy last time. But I think there's something bigger than both of us at work. We should team up.\"", leaveNotMentioned));
        activeMenu.add(new Option(this.manager, "impasse", "(Explore) \"So we're at an impasse. Neither of us are gonna get anywhere if we can't trust each other. Unless you want to fight. But I don't want to fight.\"", activeMenu.get("sorry"), leaveNotMentioned, apologized));
        activeMenu.add(new Option(this.manager, "cutA", "(Explore) \"I didn't bring my blade down, remember? How am I supposed to get you out of those chains?\"", !this.hasBlade, leaveMentioned, witchChained));
        activeMenu.add(new Option(this.manager, "cutB", "(Explore) \"I guess I'll cut you out of here.\"", this.hasBlade, leaveMentioned, witchChained));
        activeMenu.add(new Option(this.manager, "getOutB", "(Explore) \"Can't you get out of those on your own? Those chains didn't stop you last time.\"", !source.equals("betrayal"), witchChained));
        activeMenu.add(new Option(this.manager, "free", "(Explore) \"If you could have gotten out this whole time, what do you even need me for? The cabin isn't locked.\"", false));
        activeMenu.add(new Option(this.manager, "betray", "(Explore) \"Why do I have a nagging feeling you're going to stab me in the back if I help you out of here?\"", witchFree, leaveNotMentioned));
        activeMenu.add(new Option(this.manager, "stairs", "\"Okay. Let's leave.\" [Leave with the Princess.]", witchFree));
        activeMenu.add(new Option(this.manager, "offer", manager.demoMode(), "\"I don't want to hurt you, but clearly there's some broken trust. Take this as a gesture of my good will.\" [Give her the blade.]", this.hasBlade));
        activeMenu.add(new Option(this.manager, "leave", manager.demoMode(), "\"I don't trust you. Not enough to free you, and definitely not enough to get close to you. I'm leaving. Bye.\" [Leave her in the basement.]", 0, this.hasBlade));
        activeMenu.add(new Option(this.manager, "slayYap", manager.demoMode(), "\"I'd like to be straightforward with my intentions. I didn't care for how you treated me last time, and I think you might be a danger to the world. I'm going to attack you now.\" [Slay the Princess.]", 0, this.hasBlade));
        activeMenu.add(new Option(this.manager, "slaySilent", manager.demoMode(), "[Slay the Princess.]", 0, this.hasBlade));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "mistake":
                    leaveMentioned.set(true);
                case "sorry":
                    apologized.set(true);
                    mainScript.runSection(activeOutcome + "Menu");
                    break;
                    
                case "notHappy":
                case "betray":
                    mainScript.runSection(activeOutcome + "Menu");
                    break;
                    
                case "locked":
                    leaveMentioned.set(true);
                    activeMenu.setCondition("sorry", false);
                case "scared":
                case "goodWill":
                case "bygones":
                    noStall.set(false);
                    mainScript.runSection(activeOutcome + "Menu");
                    break;
                    
                case "trusted":
                    noStall.set(false);

                    if (this.rescuePath) {
                        mainScript.runSection("trustedRescueMenu");
                    } else {
                        mainScript.runSection("trustedMenu");
                    }
                    
                    break;
                    
                case "messy":
                case "impasse":
                case "free":
                    leaveMentioned.set(true);
                    mainScript.runSection(activeOutcome + "Menu");
                    break;
                    
                case "getOutB":
                    witchFree.set(true);
                    mainScript.runSection("getOutBMenu");

                    if (!heartComment) {
                        heartComment = true;
                        mainScript.runSection("chainsHeartComment");
                    }

                    mainScript.runSection("chainsFallCont");
                    break;

                case "getOutA":
                case "cutA":
                case "cutB":
                    witchFree.set(true);
                    mainScript.runSection("cutMenu");

                    if (!heartComment) {
                        heartComment = true;
                        mainScript.runSection("chainsHeartComment");
                    }

                    mainScript.runSection("chainsFallCont");
                    break;
                    
                case "stairs":
                    return this.witchStairs();
                    
                case "offer":
                    if (this.witchGiveBladeStart()) {
                        return this.witchGiveBlade(false, witchFree.check(), heartComment);
                    }

                    break;
                    
                case "cGoStairs":
                    if (manager.demoMode() || !this.canJoint3) {
                        parser.printDialogueLine(DEMOBLOCK);
                        break;
                    }
                case "leave":
                    if (manager.hasVisited(Chapter.WILD)) {
                        this.canJoint3 = false;
                        activeMenu.setGreyedOut("leave", true);
                        activeMenu.setGreyedOut("slayYap", true);
                        activeMenu.setGreyedOut("slaySilent", true);
                        parser.printDialogueLine(WORNPATH);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.WILD)) {
                        this.canJoint3 = false;
                        activeMenu.setGreyedOut("leave", true);
                        activeMenu.setGreyedOut("slayYap", true);
                        activeMenu.setGreyedOut("slaySilent", true);
                        parser.printDialogueLine(WORNPATH);
                        break;
                    }

                    return this.witchToWild(false, witchFree.check(), heartComment);
                    
                case "slayYap":
                    if (manager.hasVisited(Chapter.WILD)) {
                        this.canJoint3 = false;
                        activeMenu.setGreyedOut("leave", true);
                        activeMenu.setGreyedOut("slayYap", true);
                        activeMenu.setGreyedOut("slaySilent", true);
                        parser.printDialogueLine(WORNPATH);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.WILD)) {
                        this.canJoint3 = false;
                        activeMenu.setGreyedOut("leave", true);
                        activeMenu.setGreyedOut("slayYap", true);
                        activeMenu.setGreyedOut("slaySilent", true);
                        parser.printDialogueLine(WORNPATH);
                        break;
                    }

                    return this.witchToWild(true, witchFree.check(), heartComment);
                    
                case "cSlayPrincess":
                    if (manager.demoMode() || !this.canJoint3) {
                        parser.printDialogueLine(DEMOBLOCK);
                        break;
                    }
                case "slaySilent":
                    if (manager.hasVisited(Chapter.WILD)) {
                        this.canJoint3 = false;
                        activeMenu.setGreyedOut("leave", true);
                        activeMenu.setGreyedOut("slayYap", true);
                        activeMenu.setGreyedOut("slaySilent", true);
                        parser.printDialogueLine(WORNPATH);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.WILD)) {
                        this.canJoint3 = false;
                        activeMenu.setGreyedOut("leave", true);
                        activeMenu.setGreyedOut("slayYap", true);
                        activeMenu.setGreyedOut("slaySilent", true);
                        parser.printDialogueLine(WORNPATH);
                        break;
                    }

                    return this.witchToWild(true, witchFree.check(), heartComment);

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }
        
        throw new RuntimeException("No ending reached");
    }

    /**
     * The player offers to leave with the Witch
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding witchStairs() {
        mainScript.runSection("leaveStart");

        if (this.hasBlade) {
            mainScript.runSection("leaveStartBlade");
        } else {
            mainScript.runSection("leaveStartNoBlade");
        }

        Condition witchNotFirst = new Condition(true);
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "explore", "(Explore) \"You first.\"", witchNotFirst));
        activeMenu.add(new Option(this.manager, "offer", manager.demoMode(), "(Explore) \"Clearly, there's some broken trust here. What if I gave you this?\" [Give her the blade.]", this.hasBlade && this.canUnique3, witchNotFirst));
        activeMenu.add(new Option(this.manager, "implore", "(Explore) \"You're the one who said you can't leave here without me, which means I hold all the cards. Either you go first, or we stay here. Up to you!\"", activeMenu.get("explore"), witchNotFirst));
        activeMenu.add(new Option(this.manager, "silent", "\"[Step onto the stairs.]\""));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "explore":
                    mainScript.runSection("leaveStartExplore");
                    break;

                case "offer":
                    if (this.witchGiveBladeStart()) {
                        return this.witchGiveBlade(false, true, true);
                    }

                    break;

                case "implore":
                    witchNotFirst.set(false);
                    mainScript.runSection("leaveStartImplore");
                    break;
                
                case "cGoStairs":
                case "silent":
                    this.repeatActiveMenu = false;
                    break;
            }
        }

        if (witchNotFirst.check()) {
            if (this.hasBlade) {
                mainScript.runSection("followStartNoBlade");

                this.activeMenu = new OptionsMenu();
                activeMenu.add(new Option(this.manager, "refuse", "(Explore) I'm not stabbing her in the back."));
                activeMenu.add(new Option(this.manager, "refuse2", "I said I'm not stabbing her in the back. And I make the choices here."));
                activeMenu.add(new Option(this.manager, "praise", "Wow, that's an amazing idea that I totally never saw coming, thanks for looking out for us! [Stab her in the back.]"));
                activeMenu.add(new Option(this.manager, "noPraise", "I am not going to praise you, but I am going to [Stab her in the back.]"));
                activeMenu.add(new Option(this.manager, "ugh", "Ugh. Fine. [Stab her in the back.]"));

                this.repeatActiveMenu = true;
                while (repeatActiveMenu) {
                    this.activeOutcome = parser.promptOptionsMenu(activeMenu);
                    switch (activeOutcome) {
                        case "refuse":
                            mainScript.runSection("followRefuse");
                            break;

                        case "refuse2":
                            mainScript.runSection("followTopStartBlade");
                            this.witchLeaveBasement();
                            return ChapterEnding.FROGLOCKED;

                        case "praise":
                            mainScript.runSection("backStabPraise");
                            this.witchBetrayal(false);
                            return ChapterEnding.SCORPION;
                        
                        case "cSlayPrincess":
                        case "noPraise":
                        case "ugh":
                            mainScript.runSection("backStabUgh");
                            this.witchBetrayal(false);
                            return ChapterEnding.SCORPION;

                        default: this.giveDefaultFailResponse();
                    }
                }
            } else {
                mainScript.runSection("followStartNoBlade");
                this.witchLeaveBasement();
                return ChapterEnding.FROGLOCKED;
            }
        } else {
            mainScript.runSection("leadStart");

            if (this.hasBlade) {
                mainScript.runSection("leadStartBlade");
            } else {
                mainScript.runSection("leadStartNoBlade");
            }

            this.witchBetrayal(true);
            return ChapterEnding.FROG;
        }
        
        throw new RuntimeException("No ending reached");
    }

    /**
     * The player either chooses to stab the Witch in the back as they leave the basement together, leading to "The Scorpion" ending, or goes first up the stairs, leading to "The Frog" ending; either way, they claim the Witch
     */
    private void witchBetrayal(boolean wentFirst) {
        boolean brokenShare = false;
        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "broken", "\"I can't get up. You broke my back.\""));
        activeMenu.add(new Option(this.manager, "help", "\"What the hell was that for? I was trying to help you out of here!\"", wentFirst));
        activeMenu.add(new Option(this.manager, "damn", "\"Damn. I thought I had you there.\"", !wentFirst));
        activeMenu.add(new Option(this.manager, "never", "\"We were never going to get up those stairs, were we?\""));
        activeMenu.add(new Option(this.manager, "trust", "\"We could have gotten out of here if we'd just trusted each other.\""));
        activeMenu.add(new Option(this.manager, "silent", "[Say nothing.]"));

        switch (parser.promptOptionsMenu(activeMenu)) {
            case "broken":
                brokenShare = true;
            case "help":
            case "damn":
                mainScript.runSection(activeOutcome + "Betrayal");
                break;

            case "never":
                if (wentFirst) {
                    if (source.equals("locked")) {
                        mainScript.runSection("neverBetrayalLocked");
                    } else {
                        mainScript.runSection("neverBetrayalOther");
                    }
                } else {
                    mainScript.runSection("betrayalGenericLead");
                }
                
                break;

            case "trust":
                if (source.equals("locked")) {
                    mainScript.runSection("trustBetrayalLocked");
                } else {
                    mainScript.runSection("trustBetrayalOther");
                }

                if (wentFirst) {
                    if (source.equals("locked")) {
                        mainScript.runSection("betrayalJokeLocked");
                    } else {
                        mainScript.runSection("betrayalJokeOther");
                    }
                } else {
                    mainScript.runSection("betrayalGenericLead");
                }

                break;

            case "silent":
                if (wentFirst) {
                    mainScript.runSection("silentBetrayalFollow");

                    if (source.equals("locked")) {
                        mainScript.runSection("betrayalJokeLocked");
                    } else {
                        mainScript.runSection("betrayalJokeOther");
                    }
                } else {
                    mainScript.runSection("betrayalGenericLead");
                }

                break;
        }

        if (wentFirst) {
            if (this.hasBlade) {
                mainScript.runSection("betrayalContLeadBlade");
                mainScript.runSection("betrayalContLeadBlade2");
            } else {
                mainScript.runSection("betrayalContLeadNoBlade");
                mainScript.runSection("betrayalContLeadNoBlade2");
            }
        } else {
            mainScript.runSection("betrayalContFollow");
        }

        if (brokenShare) {
            mainScript.runSection("betrayalEndBrokenShare");
        } else {
            mainScript.runSection("betrayalEndNohare");
        }

        this.quietCreep();
        mainScript.runSection();

        if (this.isFirstVessel) {
            mainScript.runSection("witchEndFirstVessel");
        } else {
            mainScript.runSection("witchEndNotFirstVessel");
        }
    }

    /**
     * The player chooses to peacefully follow the Witch out of the basement, leading to "The Frog" ending and claiming the Witch
     */
    private void witchLeaveBasement() {
        if (source.equals("locked")) mainScript.runSection("lockedNotAgain");

        if (this.hasBlade) {
            mainScript.runSection("lockedBlade");
        } else {
            mainScript.runSection("lockedNoBlade");
        }

        if (source.equals("locked")) {
            mainScript.runSection("lockedReplyAgain");
        } else {
            mainScript.runSection("lockedReplyOther");
        }

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "try", "(Explore) [Try the door.]"));
        activeMenu.add(new Option(this.manager, "force", "(Explore) [Force the door.]"));
        activeMenu.add(new Option(this.manager, "funny", "(Explore) \"Okay. Fine. You got me. Very funny. Can you let me out now?\""));
        activeMenu.add(new Option(this.manager, "plead", "(Explore) \"Please just let me out, I promise I won't be mad.\""));
        activeMenu.add(new Option(this.manager, "need", "(Explore) \"I thought you needed me to get out.\""));
        activeMenu.add(new Option(this.manager, "stuck", "\"Okay. What happens now? I'm stuck here.\""));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "force":
                    activeMenu.setCondition("try", false);
                case "try":
                case "funny":
                case "plead":
                case "need":
                    mainScript.runSection(activeOutcome + "LockedMenu");
                    break;

                case "cGoStairs":
                    if (!activeMenu.hasBeenPicked("try")) {
                        activeMenu.setCondition("try", false);
                        mainScript.runSection("tryLockedMenu");
                    } else if (!activeMenu.hasBeenPicked("force")) {
                        activeMenu.setCondition("force", false);
                        mainScript.runSection("forceLockedMenu");
                    } else {
                        mainScript.runSection("tryAgainLockedMenu");
                    }

                case "stuck":
                    this.repeatActiveMenu = false;
                    break;

                default: this.giveDefaultFailResponse();
            }
        }

        this.quietCreep();
        mainScript.runSection("lockedEnd");

        if (this.isFirstVessel) {
            mainScript.runSection("witchEndFirstVessel");
        } else {
            mainScript.runSection("witchEndNotFirstVessel");
        }
    }

    /**
     * The player considers giving the Witch the blade as a gesture of good will
     * @return true if the player commits to giving the Witch the blade; false otherwise
     */
    private boolean witchGiveBladeStart() {
        OptionsMenu offerMenu = new OptionsMenu(true);
        offerMenu.add(new Option(this.manager, "commit", "This isn't a democracy. We're giving her the blade. [Give her the blade.]", 0));
        offerMenu.add(new Option(this.manager, "backOut", "Haha. Yeah, nevermind, that was such a silly idea. I'm not going to give her the blade. She clearly hates us. [Don't do it.]"));

        switch (parser.promptOptionsMenu(offerMenu)) {
            case "commit":
                if (!manager.confirmContentWarnings(Chapter.THORN)) {
                    offerMenu.setGreyedOut("commit", true);
                    break;
                }

                return true;

            case "nevermind":
                this.canUnique3 = false;
                mainScript.runSection("offerBackOut");
                return false;
        }

        throw new RuntimeException("No conclusion reached");
    }

    /**
     * The player gives the Witch the blade as a gesture of good will, leading to Chapter III: The Thorn
     * @param fromStairs whether the player offered to leave with the Witch and is at the stairs
     * @param witchFree whether the Witch has freed herself from her chains
     * @param heartComment whether the Voice of the Opportunist has made a comment about the Witch being "a woman after [his] own heart"
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding witchGiveBlade(boolean fromStairs, boolean witchFree, boolean heartComment) {
        if (fromStairs) {
            mainScript.runSection("offerCommitStairs");
        } else {
            mainScript.runSection("offerCommitBasement");

            if (!witchFree) {
                mainScript.runSection("offerCommitBasementNotFree");
                
                if (heartComment) {
                    mainScript.runSection("offerCommitBasementHeartComment");
                } else {
                    mainScript.runSection("offerCommitBasementHeartComment");
                }

                mainScript.runSection("offerCommitBasementCont");
            }
        }

        boolean smittenFlag = false;
        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "yourChoice", "\"That's up to you. It's why I gave you the blade. I chose last time, and I regret it. So now it's your time to choose.\""));
        activeMenu.add(new Option(this.manager, "scared", "\"We're both scared and we're both hurting. If one of us doesn't make a change, we'll probably kill each other forever. Do you want that? I don't. We can be better than this.\""));
        activeMenu.add(new Option(this.manager, "beautiful", "\"You're beautiful. I want to *actually* save you, and this feels like the only way to do it.\""));
        activeMenu.add(new Option(this.manager, "someone", "\"If you're like someone I know, you're probably going to kill me.\""));
        activeMenu.add(new Option(this.manager, "silent", "[Remain silent.]"));

        this.activeOutcome = parser.promptOptionsMenu(activeMenu);
        switch (activeOutcome) {
            case "scared":
                smittenFlag = true;
            case "yourChoice":
                mainScript.runSection("trickOffer");
                break;

            case "beautiful":
                smittenFlag = true;
            case "someone":
            case "silent":
                mainScript.runSection(activeOutcome + "Offer");
                break;
        }

        if (smittenFlag) {
            return ChapterEnding.PASTLIFEGAMBITSPECIAL;
        } else {
            return ChapterEnding.PASTLIFEGAMBIT;
        }
    }

    /**
     * The player chooses to either fight the Witch or leave the basement alone, leading to Chapter III: The Wild
     * @param fight whether the player chose to fight the Witch
     * @param witchFree whether the Witch has freed herself from her chains
     * @param heartComment whether the Voice of the Opportunist has made a comment about the Witch being "a woman after [his] own heart"
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding witchToWild(boolean fight, boolean witchFree, boolean heartComment) {
        String jumpSuffix = (fight) ? "Fight" : "Flee";

        boolean fightContinue = false;
        String sucksDisplay = "(Explore) \"Come on! They're pressing in on me. ";
        String sorryDisplay = "(Explore) \"I take it all back! I can help you get out of here! You and I can work together! We can be friends.";
        if (fight) {
            this.canSlayPrincess = true;
            sucksDisplay += "They're pressing in on you! This sucks!\"";
            sorryDisplay += " I'm sorry!\"";

            if (witchFree) {
                mainScript.runSection("fightStartFree");
            } else {
                mainScript.runSection("fightStartNotFree");

                if (!heartComment) {
                    mainScript.runSection("fightHeartComment");
                }

                mainScript.runSection("fightStartNotFreeCont");
            }
        } else {
            this.currentLocation = GameLocation.CABIN;
            this.withPrincess = false;
            this.canSlayPrincess = false;
            sucksDisplay += "They're probably pressing in on you too! I'm only assuming that because I can't see you but it sure sounds like they are!\"";
            sorryDisplay += "\"";
            mainScript.runSection("abandonStart");
        }
        
        Condition mutualNotFollowedUp = new Condition(true);
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "mutual", "(Explore) \"What about you? They'll crush you just as easily as they'll crush me.\""));
        activeMenu.add(new Option(this.manager, "stop", "(Explore) \"Make them stop! You can make them stop, right?\"", mutualNotFollowedUp));
        activeMenu.add(new Option(this.manager, "why", "(Explore) \"Why, though? Why are you doing this?\"", activeMenu.get("mutual"), mutualNotFollowedUp));
        activeMenu.add(new Option(this.manager, "sucks", sucksDisplay, false));
        activeMenu.add(new Option(this.manager, "animals", "(Explore) \"We're not animals! We're people. We can work this out. We can make things better.\""));
        activeMenu.add(new Option(this.manager, "sorry", sorryDisplay));
        activeMenu.add(new Option(this.manager, "wait", "[Give up, and await your death.]"));
        activeMenu.add(new Option(this.manager, "fight", "[Go out fighting.]", fight));

        for (int menuCount = 0; menuCount < 3; menuCount++) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "why":
                    mutualNotFollowedUp.set(false);
                case "mutual":
                case "sucks":
                case "animals":
                case "sorry":
                    mainScript.runSection(activeOutcome + "Wild" + jumpSuffix);
                    break;

                case "stop":
                    mutualNotFollowedUp.set(false);

                    if (activeMenu.hasBeenPicked("mutual")) {
                        mainScript.runSection("stopWild" + jumpSuffix);
                    } else {
                        mainScript.runSection("stopWildMutual");
                    }

                    break;

                case "wait":
                    menuCount = 2; // Fast-forward
                    if (fight) mainScript.runSection("waitWildFight");
                    break;

                case "cSlayPrincess":
                case "fight":
                    menuCount = 2; // Fast-forward
                    fightContinue = true;
                    mainScript.runSection("fightWild");
                    break;

                default:
                    menuCount -= 1;
                    this.giveDefaultFailResponse(activeOutcome);
            }

            switch (menuCount) {
                case 0:
                    mainScript.runSection("rootsComment1");
                    break;

                case 1:
                    mainScript.runSection("rootsComment2");
                    break;
            }
        }

        // Crushed to death
        mainScript.runSection("crushed" + jumpSuffix);

        if (!fight) {
            return ChapterEnding.PLAYINGITSAFE;
        } else if (fightContinue) {
            return ChapterEnding.KNIVESOUTMASKSOFF;
        } else {
            return ChapterEnding.KNIVESOUTMASKSOFFGIVEUP;
        }
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

        this.currentVoices.put(Voice.NARRATOR, false);
        this.quietCreep();
        mainScript.runSection("endingJoin");

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
        activeMenu.add(new Option(this.manager, "q1", "(Explore) \"XXXXX\""));
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


        mainScript.runSection("basementStart");

        if (this.sharedLoop) {
            mainScript.runSection("basementStartSharedLoop");
            
            if (this.sharedLoopInsist) {
                mainScript.runSection("basementStartSharedLoopInsist");
            } else {
                mainScript.runSection("basementStartNoInsist");
            }
        } else {
            mainScript.runSection("basementStartNoShare");
            mainScript.runSection("basementStartNoInsist");
        }

        boolean howFree = false;
        String endWorldResponse = "";

        OptionsMenu subMenu;
        this.canSlayPrincess = true;
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "killed", "(Explore) \"You killed me last time and it hurt a lot! Why did you do that?\""));
        activeMenu.add(new Option(this.manager, "howFreeNoBlade", "(Explore) \"I didn't bring a knife. Do I have to cut you out again?\"", !this.hasBlade));
        activeMenu.add(new Option(this.manager, "howFreeBlade", "(Explore) \"Do I have to cut you out again? I really didn't care for that last time.\"", this.hasBlade));
        activeMenu.add(new Option(this.manager, "after", "(Explore) \"What happened after I died?\""));
        activeMenu.add(new Option(this.manager, "endA", "(Explore) \"But before we started talking, did the world end? Did you end the world?\"", activeMenu.get("after")));
        activeMenu.add(new Option(this.manager, "endB", "(Explore) \"I have to ask... did you end the world after you killed me back there?\""));
        activeMenu.add(new Option(this.manager, "sorry", "(Explore) \"I'm sorry about what happened last time. The Narrator who sent me here to kill you took over my body. It was extremely unfair.\""));
        activeMenu.add(new Option(this.manager, "rescue", "[Rescue the Princess.]"));
        activeMenu.add(new Option(this.manager, "slay", manager.demoMode(), "[Slay the Princess.]", 0));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "killed":
                case "sorry":
                    mainScript.runSection(activeOutcome);
                    break;

                case "howFreeNoBlade":
                case "howFreeBlade":
                    howFree = true;
                    mainScript.runSection("howFree");
                    break;

                case "after":
                    activeMenu.setCondition("endB", false);
                    mainScript.runSection("after");
                    break;

                case "endA":
                    activeMenu.setCondition("endB", false);
                case "endB":
                    activeMenu.setCondition("endA", false);
                    mainScript.runSection("endWorldAsk");

                    subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "save", "\"No? I don't want the world to end.\""));
                    subMenu.add(new Option(this.manager, "neutral", "\"I have no feelings one way or another about the world ending.\""));
                    subMenu.add(new Option(this.manager, "burn", "\"Honestly, the world sucks. People are a plague and I hope you brought a slow and painful ruin to them all.\""));
                    subMenu.add(new Option(this.manager, "silent", "[Remain silent.]"));

                    endWorldResponse = parser.promptOptionsMenu(subMenu);
                    mainScript.runSection(endWorldResponse + "EndWorld");
                    break;

                case "rescue":
                    return this.damselRescue(howFree, endWorldResponse);

                case "cSlayPrincess":
                    if (manager.demoMode()) {
                        parser.printDialogueLine(DEMOBLOCK);
                        break;
                    }
                case "slay":
                    if (manager.hasVisited(Chapter.GREY)) {
                        parser.printDialogueLine(WORNPATH);
                        parser.printDialogueLine(WORNPATHHERO);

                        this.canSlayPrincess = false;
                        this.canJoint3 = false;
                        activeMenu.setGreyedOut("slay", true);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.GREY, ChapterEnding.LADYKILLER, "forced suicide")) {
                        this.canSlayPrincess = false;
                        this.canJoint3 = false;
                        activeMenu.setGreyedOut("slay", true);
                        break;
                    }

                    mainScript.runSection("conversationSlay");
                    this.damselSlay();
                    return ChapterEnding.LADYKILLER;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        throw new RuntimeException("No ending reached");
    }

    /**
     * The player decides to rescue the Damsel
     * @param howFree whether the player asked how they could free her
     * @param endWorldResponse how the player responds to the Princess asking if she was "supposed to end the world," if they triggered the question
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding damselRescue(boolean howFree, String endWorldResponse) {
        if (!howFree) {
            mainScript.runSection("rescueStartHowFree");
        }

        if (this.hasBlade) {
            mainScript.runSection("rescueStartBlade");
            mainScript.runSection("rescueContBlade");
        } else {
            mainScript.runSection("rescueStartNoBlade");
            mainScript.runSection("rescueContNoBlade");
        }

        this.canSlayPrincess = false;
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "want", "(Explore) \"What do you want to do?\"", 0));
        activeMenu.add(new Option(this.manager, "leave", "\"We leave. And then we have our whole lives to figure out what we want to do next.\""));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "want":
                    if (!manager.confirmContentWarnings("derealization")) break;

                    return this.damselDeconSequence(endWorldResponse);

                case "cGoStairs":
                case "leave":
                    mainScript.runSection("leaveStart");
                    return this.damselLeave(endWorldResponse);

                case "cSlayPrincessNoBladeFail":
                    mainScript.runSection("slayHeroFail");
                    break;

                case "cSlayPrincessFail":
                    mainScript.runSection("slaySmittenFail");
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }
        
        throw new RuntimeException("No ending reached");
    }

    /**
     * The player asks the Damsel what she wants, triggering the sequence that might lead to claiming the Deconstructed Damsel
     * @param endWorldResponse how the player responds to the Princess asking if she was "supposed to end the world," if they triggered the question
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding damselDeconSequence(String endWorldResponse) {
        mainScript.runSection("whatWant");

        if (this.whatWouldYouDo) {
            mainScript.runSection("whatWouldYouDo");
        }

        mainScript.runSection("whatWantCont");

        int depersonCount = 0;
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "stay", manager.demoMode(), "\"Okay. Clearly something is happening here, and I'm very scared. What if we just... don't do anything? What if we just stay here? Nobody gets hurt, and we just figure out a way to be happy, together.\"", false));
        activeMenu.add(new Option(this.manager, "parrot", "(Explore) \"You're just parroting my questions. What do you actually want?\"", false));
        activeMenu.add(new Option(this.manager, "wantA", "(Explore) \"I want you to tell me what you want.\""));
        activeMenu.add(new Option(this.manager, "wantB", "(Explore) \"There must be something you want!\""));
        activeMenu.add(new Option(this.manager, "makeYou", "(Explore) \"But what would make you happy?\"", false));
        activeMenu.add(new Option(this.manager, "more", "(Explore) \"You have to want something more than just making me happy.\"", false));
        activeMenu.add(new Option(this.manager, "ownThing", "(Explore) \"But you need your own thing. You just met me. You can't base your entire happiness around me.\"", false));
        activeMenu.add(new Option(this.manager, "unhappy", "(Explore) \"I want you to make me unhappy.\"", false));
        activeMenu.add(new Option(this.manager, "endWorld", "(Explore) \"Do you want to end the world?\""));
        activeMenu.add(new Option(this.manager, "leaveA", "\"I just want to leave. We can figure out the rest later.\""));
        activeMenu.add(new Option(this.manager, "leaveB", "\"If you want to leave, let's leave.\""));
        activeMenu.add(new Option(this.manager, "leaveC", "\"This isn't right. Let's just get out of here.\"", false));
        activeMenu.add(new Option(this.manager, "slay", manager.demoMode(), "\"Something isn't right here. I'm sorry.\" [Slay the Princess.]", 0, false));

        boolean skipDepersonComment = false;
        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (depersonCount) {
                case 1:
                    activeMenu.setCondition("makeYou", true);
                    activeMenu.setCondition("more", true);
                    activeMenu.setCondition("ownThing", true);
                    activeMenu.setCondition("unhappy", true);
                    activeMenu.setCondition("leaveC", true);
                    activeMenu.setCondition("slay", this.canJoint3 && this.hasBlade);
                    this.canSlayPrincess = this.canJoint3 && this.hasBlade;

                    if (!skipDepersonComment) mainScript.runSection("depersonComment1");
                    break;

                case 2:
                    if (!skipDepersonComment) mainScript.runSection("depersonComment2");
                    break;

                case 3:
                    activeMenu.setCondition("stay", true);

                    if (!skipDepersonComment) mainScript.runSection("depersonComment3");
                    break;

                case 4:
                    mainScript.runSection("deconEnding");

                    if (this.isFirstVessel) {
                        mainScript.runSection("deconEndingFirstVessel");
                    } else {
                        mainScript.runSection("deconEndingNotFirst");
                    }

                    return ChapterEnding.ANDTHEYLIVEDHAPPILY;
            }

            skipDepersonComment = false;
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "stay":
                    if (!manager.confirmContentWarnings(Chapter.HAPPY, "forced self-mutilation; forced suicide")) {
                        this.canUnique3 = false;
                        activeMenu.setGreyedOut("stay", true);
                        break;
                    }

                    mainScript.runSection("stayStartDecon");
                    return ChapterEnding.CONTENTSOFOURHEARTDECON;

                case "parrot":
                case "wantA":
                case "wantB":
                    depersonCount += 1;
                    this.damselDepersonTruthComment(depersonCount);
                    mainScript.runSection("wantDecon");
                    break;

                case "makeYou":
                case "more":
                case "ownThing":
                case "unhappy":
                    depersonCount += 1;
                    this.damselDepersonTruthComment(depersonCount);
                    mainScript.runSection(activeOutcome + "Decon");
                    break;

                case "endWorld":
                    mainScript.runSection("endWorldDecon");

                    if (endWorldResponse.isEmpty()) {
                        if (depersonCount == 0) {
                            mainScript.runSection("endWorldNoDeperson");
                        } else {
                            activeMenu.setCondition("parrot", true);
                            mainScript.runSection("endWorldDeperson");
                        }

                        OptionsMenu subMenu = new OptionsMenu(true);
                        subMenu.add(new Option(this.manager, "save", "\"No? I don't want the world to end.\""));
                        subMenu.add(new Option(this.manager, "neutral", "\"I have no feelings one way or another about the world ending.\""));
                        subMenu.add(new Option(this.manager, "burn", "\"Honestly, the world sucks. People are a plague and I hope you brought a slow and painful ruin to them all.\""));
                        subMenu.add(new Option(this.manager, "silent", "[Remain silent.]"));

                        endWorldResponse = parser.promptOptionsMenu(subMenu);
                        switch (endWorldResponse) {
                            case "silent":
                                mainScript.runSection("silentEndWorldDecon");
                                break;

                            case "save":
                                if (this.hasBlade) mainScript.runSection("saveEndWorldDeconBlade");

                                if (depersonCount == 0) {
                                    depersonCount += 1;
                                    this.damselDepersonTruthComment(depersonCount);
                                }

                                mainScript.runSection("saveEndWorldDecon");
                                break;

                            default:
                                mainScript.runSection(endWorldResponse + "EndWorldDecon");

                                if (depersonCount == 0) {
                                    depersonCount += 1;
                                    this.damselDepersonTruthComment(depersonCount);
                                }

                                mainScript.runSection();
                        }

                        break;
                    } else {
                        skipDepersonComment = true;
                        mainScript.runSection(endWorldResponse + "WorldDecon");
                    }

                    break;

                case "leaveA":
                case "leaveB":
                case "leaveC":
                    if (depersonCount > 1) {
                        mainScript.runSection("leaveStartDeconB");
                    } else {
                        mainScript.runSection("leaveStartDeconA");
                    }

                    return this.damselLeave(endWorldResponse);

                case "cSlayPrincess":
                    if (manager.demoMode()) {
                        parser.printDialogueLine(DEMOBLOCK);
                        break;
                    }
                case "slay":
                    if (manager.hasVisited(Chapter.GREY)) {
                        parser.printDialogueLine(WORNPATH);
                        parser.printDialogueLine(WORNPATHHERO);

                        this.canSlayPrincess = false;
                        this.canJoint3 = false;
                        activeMenu.setGreyedOut("slay", true);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.GREY, ChapterEnding.LADYKILLER, "forced suicide")) {
                        this.canSlayPrincess = false;
                        this.canJoint3 = false;
                        activeMenu.setGreyedOut("slay", true);
                        break;
                    }

                    mainScript.runSection("deconSlay");
                    this.damselSlay();
                    return ChapterEnding.LADYKILLER;

                default:
                    skipDepersonComment = true;
                    this.giveDefaultFailResponse(activeOutcome);
            }
        }

        throw new RuntimeException("No ending found");
    }

    /**
     * Describes the Princess deconstructing for a given depersonalization count
     * @param depersonCount the number of times the Princess has deconstructed already
     */
    private void damselDepersonTruthComment(int depersonCount) {

        mainScript.runSection("depersonTruth" + depersonCount);

        if (depersonCount == 1) {
            this.quietCreep();
        } else {
            mainScript.runSection("quietCont");
        }
    }

    /**
     * The player leaves the basement with the Damsel
     * @param endWorldResponse how the player responds to the Princess asking if she was "supposed to end the world," if they triggered the question
     * @return the Chapter ending reached by the player
     */
    private ChapterEnding damselLeave(String endWorldResponse) {
        boolean tookBlade = this.hasBlade;
        this.canSlayPrincess = false;

        if (this.hasBlade) {
            this.hasBlade = false;
            mainScript.runSection("leaveStartBlade");
        } else {
            mainScript.runSection("leaveStartNoBlade");
        }

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "explore", "(Explore) \"Do you think you can open it?\""));
        activeMenu.add(new Option(this.manager, "her", "\"Yeah. I think you've got this.\"", activeMenu.get("explore")));
        activeMenu.add(new Option(this.manager, "together", "\"I think we can open it if we try together.\""));
        activeMenu.add(new Option(this.manager, "alone", "\"I think I've got this.\" [Open the door by yourself.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "explore":
                    mainScript.runSection("openDoorExplore");
                    break;

                default:
                    this.repeatActiveMenu = false;
                    mainScript.runSection(activeOutcome + "OpenDoor");
            }
        }

        this.currentLocation = GameLocation.CABIN;

        if (tookBlade) {
            this.withBlade = this.canJoint3;
            mainScript.runSection("finalBladeChoice");

            this.activeMenu = new OptionsMenu();
            activeMenu.add(new Option(this.manager, "slay", manager.demoMode() || !this.canJoint3, "[Take the blade and slay the Princess.]", 0));
            activeMenu.add(new Option(this.manager, "nope", "[You're not doing that.]"));

            this.repeatActiveMenu = true;
            while (repeatActiveMenu) {
                this.activeOutcome = parser.promptOptionsMenu(activeMenu);
                switch (activeOutcome) {
                    case "cTake":
                    case "cSlayPrincessNoBladeFail":
                        if (!this.canJoint3) {
                            mainScript.runSection("slayAgainAttempt");
                            break;
                        } else if (manager.demoMode()) {
                            parser.printDialogueLine(DEMOBLOCK);
                            break;
                        }
                    case "slay":
                        if (manager.hasVisited(Chapter.GREY)) {
                            parser.printDialogueLine(WORNPATH);
                            parser.printDialogueLine(WORNPATHHERO);

                            this.withBlade = false;
                            this.canJoint3 = false;
                            activeMenu.setGreyedOut("slay", true);
                            break;
                        } else if (!manager.confirmContentWarnings(Chapter.GREY, ChapterEnding.LADYKILLER, "forced suicide")) {
                            this.withBlade = false;
                            this.canJoint3 = false;
                            activeMenu.setGreyedOut("slay", true);
                            break;
                        }

                        this.repeatActiveMenu = false;
                        mainScript.runSection("cabinSlay");
                        this.damselSlay();
                        return ChapterEnding.LADYKILLER;

                    case "nope":
                        this.repeatActiveMenu = false;
                        this.withBlade = false;
                        mainScript.runSection("preLeaveNoBlade");
                        break;

                    default: this.giveDefaultFailResponse(activeOutcome);
                }
            }
        } else {
            mainScript.runSection("preLeaveBlade");
        }

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "stay", manager.demoMode() || !this.canUnique3, "\"All we need to be happy is each other, what if we just stayed here and built a life together?\""));
        activeMenu.add(new Option(this.manager, "stop", "Stop it with these interruptions. I already made up my mind. We're leaving."));
        activeMenu.add(new Option(this.manager, "ignore", "[Just ignore them.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "stay":
                    if (!manager.confirmContentWarnings(Chapter.HAPPY, "forced self-mutilation; forced suicide")) {
                        activeMenu.setGreyedOut("stay", true);
                        break;
                    }

                    mainScript.runSection("staySuggest");

                    this.activeMenu = new OptionsMenu(true);
                    activeMenu.add(new Option(this.manager, "nonchalance", "\"Your nonchalance about the fate of the world has me a bit worried. That's why I want to stay here.\"", 0, !endWorldResponse.isEmpty()));
                    activeMenu.add(new Option(this.manager, "trust", "\"Trust me. It'll be better for both of us if we stay. We can be happy here. We just have to want it.\"", 0));
                    activeMenu.add(new Option(this.manager, "leave", "\"You're right. We're leaving.\"", 0));
                    activeMenu.add(new Option(this.manager, "silent", "[Remain silent.]", 0));

                    switch (parser.promptOptionsMenu(activeMenu)) {
                        case "leave":
                            this.repeatActiveMenu = false;
                            mainScript.runSection("stayReturn");
                            break;

                        case "nonchalance":
                            if (!manager.confirmContentWarnings(Chapter.HAPPY, "forced self-mutilation, forced suicide")) {
                                activeMenu.setGreyedOut("nonchalance", true);
                                activeMenu.setGreyedOut("trust", true);
                                activeMenu.setGreyedOut("silent", true);
                                break;
                            }

                            if (tookBlade) {
                                mainScript.runSection("stayNonchalanceBlade");
                            } else {
                                mainScript.runSection("stayNonchalanceNoBlade");
                            }

                            return ChapterEnding.CONTENTSOFOURHEARTUPSTAIRS;

                        case "trust":
                        case "silent":
                            if (!manager.confirmContentWarnings(Chapter.HAPPY, "forced self-mutilation, forced suicide")) {
                                activeMenu.setGreyedOut("nonchalance", true);
                                activeMenu.setGreyedOut("trust", true);
                                activeMenu.setGreyedOut("silent", true);
                                break;
                            }

                            mainScript.runSection("stayStartUpstairs");
                            return ChapterEnding.CONTENTSOFOURHEARTUPSTAIRS;
                    }

                    break;

                case "stop":
                    this.repeatActiveMenu = true;
                    break;

                case "cGoHill":
                case "ignore":
                    this.repeatActiveMenu = true;
                    mainScript.runSection("leaveEarlyJoin");
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        mainScript.runSection("leaveEnding");
        this.quietCreep();
        mainScript.runSection();
        mainScript.runSection("quietCont", true);
        mainScript.runSection();

        if (this.isFirstVessel) {
            mainScript.runSection("leaveEndFirstVessel");
        } else {
            mainScript.runSection("leaveEndNotFirstVessel");
        }

        return ChapterEnding.ROMANTICHAZE;
    }

    /**
     * The player chooses to slay the Damsel, leading to Chapter III: The Grey with the Voice of the Cold
     */
    private void damselSlay() {
        mainScript.runSection("slayMain");

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "refuse", "\"I'm the one who makes the decisions here, and I say *no!*\""));
        activeMenu.add(new Option(this.manager, "ok", "\"If that's what you want to do, let's see what happens.\""));
        activeMenu.add(new Option(this.manager, "silent", "[Remain silent.]"));

        mainScript.runSection(parser.promptOptionsMenu(activeMenu) + "Slay");
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
        activeMenu.add(new Option(this.manager, "q1", "(Explore) \"XXXXX\""));
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
        activeMenu.add(new Option(this.manager, "q1", "(Explore) \"XXXXX\""));
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
        activeMenu.add(new Option(this.manager, "q1", "(Explore) \"XXXXX\""));
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
