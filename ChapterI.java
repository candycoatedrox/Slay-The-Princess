public class ChapterI extends StandardCycle {
    
    private boolean skipHillDialogue = false;

    // Flags used only in this Chapter
    private final boolean canNightmare;
    private final boolean canRazor;
    private final boolean canTower;
    private final boolean canSteelSlay;
    private final boolean canHesitateSlay;
    private final boolean mustSpectre;
    private Condition mustStranger;
    private boolean canFree = true;
    private boolean canNotFreeSoft = true;
    private final Condition canSlay = new Condition();
    private final InverseCondition cantSlay = canSlay.getInverse();
    private final Condition sharedTask = new Condition();
    private final InverseCondition noShareTask = sharedTask.getInverse();
    private boolean askedPrize = false;
    private boolean harshHesitated = false;
    private boolean harshIsArmed = false;
    private boolean harshAfraid = false;

    // Flags that persist in Chapter 2
    private boolean droppedBlade1 = false; // Used in Adversary
    private boolean whatWouldYouDo = false; // Used in Damsel
    private boolean rescuePath = false; // Used in Witch

    // --- CONSTRUCTOR ---

    /**
     * Constructor
     * @param manager the GameManager to link this StandardCycle to
     * @param parser the IOHandler to link this StandardCycle to
     */
    public ChapterI(GameManager manager, IOHandler parser) {
        super(manager, parser);
        this.activeChapter = Chapter.CH1;

        this.canNightmare = !manager.hasVisited(Chapter.NIGHTMARE);
        this.canRazor = !manager.hasVisited(Chapter.RAZOR);
        this.canTower = !manager.hasVisited(Chapter.TOWER);

        this.canSteelSlay = !manager.hasVisited(Chapter.SPECTRE) || this.canRazor;
        this.canHesitateSlay = !manager.hasVisited(Chapter.ADVERSARY) || this.canTower || this.canNightmare;
        this.mustSpectre = !this.canHesitateSlay && manager.hasVisited(Chapter.PRISONER);
    }

    // --- CHAPTER MANAGEMENT ---

    /**
     * Initiates Chapter 1
     * @return the ending reached by the player
     */
    @Override
    public ChapterEnding runChapter() {
        this.unlockChapter();
        manager.updateTracker();
        this.mainScript = new Script(this.manager, this.parser, activeChapter.getScriptFile());
        
        this.displayTitleCard();

        ChapterEnding ending = this.heroAndPrincess();
        if (ending == null) return ChapterEnding.DEMOENDING;
        switch (ending) {
            case ABORTED:
            case GOODENDING:
            case DEMOENDING: break;

            default:
                ChapterII chapter2 = new ChapterII(ending, manager, parser, route, isHarsh, knowsDestiny, droppedBlade1, whatWouldYouDo, rescuePath);
                ending = chapter2.runChapter();
                
                if (ending == null) return ChapterEnding.DEMOENDING;
                switch (ending) {
                    case ABORTED:
                    case GOODENDING:
                    case DEMOENDING: break;

                    default:
                        manager.updateMoundValues(ending.getFreedom(), ending.getSatisfaction());
                        this.mirrorSequence(ending);
                }
        }

        return ending;
    }

    // --- CHAPTERS & SCENES ---

    // - Chapter I: The Hero and the Princess -

    /**
     * Runs the opening sequence of Chapter I, from the opening conversation to entering the basement
     * @return the ending reached by the player
     */
    private ChapterEnding heroAndPrincess() {
        // You always start with the Voice of the Hero
        
        boolean canSoft = !manager.hasVisitedAll(Chapter.BEAST, Chapter.WITCH, Chapter.DAMSEL, Chapter.NIGHTMARE);
        boolean canHarsh = !manager.hasVisitedAll(Chapter.ADVERSARY, Chapter.TOWER, Chapter.SPECTRE, Chapter.NIGHTMARE, Chapter.RAZOR, Chapter.PRISONER);
        this.mustStranger = new Condition(!canSoft && !canHarsh);

        boolean canStranger = !manager.hasVisited(Chapter.STRANGER);

        if (!this.isFirstVessel || manager.nVesselsAborted() > 0) manager.unlock("firstRoute");
        
        mainScript.runSection();

        Condition canReluctant = new Condition();
        Condition canQuestionFollowUp = new Condition(true);

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "question1", "(Explore) The end of the world? What are you talking about?"));
        activeMenu.add(new Option(this.manager, "question2", "(Explore) But how can a princess locked away in a basement end the world?", activeMenu.get("question1"), canQuestionFollowUp));
        activeMenu.add(new Option(this.manager, "whyDanger", "(Explore) If you don't tell me why she's dangerous, I'm not going to kill her.", activeMenu.get("question1"), canQuestionFollowUp));
        activeMenu.add(new Option(this.manager, "whatHappens", "(Explore) Okay. What happens if she gets out then? I want specifics.", activeMenu.get("whyDanger")));
        activeMenu.add(new Option(this.manager, "evidence", "(Explore) Do you have any evidence to back this up?", activeMenu.get("question1")));
        activeMenu.add(new Option(this.manager, "chickenEgg", "(Explore) Have you considered that maybe the only reason she's going to end the world is *because* she's locked up?"));
        activeMenu.add(new Option(this.manager, "conscience", "(Explore) Killing a princess seems kind of bad, though, doesn't it?"));
        activeMenu.add(new Option(this.manager, "someoneElse", "(Explore) Can't someone else do this?"));
        activeMenu.add(new Option(this.manager, "refuse", "(Explore) Forget it. I'm not doing this."));
        activeMenu.add(new Option(this.manager, "letItBurn", "(Explore) Have you considered that maybe I'm okay with the world ending?"));
        activeMenu.add(new Option(this.manager, "prize", "(Explore) Do I get some kind of reward for doing this?"));
        activeMenu.add(new Option(this.manager, "prize2", "(Explore) Can you tell me what my prize is going to be for doing a good job?", activeMenu.get("prize")));
        activeMenu.add(new Option(this.manager, "reluctant", this.mustStranger, "Look, I'll go to the cabin and I'll talk to her, and if she's as bad as you say she is then *maybe* I'll slay her. But I'm not committing to anything until I've had the chance to meet her face to face.", canReluctant));
        activeMenu.add(new Option(this.manager, "okFine", this.mustStranger, "Okay. Fine. I'll go to the cabin.", activeMenu.get("refuse")));
        activeMenu.add(new Option(this.manager, "sold", this.mustStranger, "Okay, I'm sold. Let's get this over with.", activeMenu.get("question1")));
        activeMenu.add(new Option(this.manager, "thanks", this.mustStranger, "Oh, okay. Thanks for telling me what to do."));
        activeMenu.add(new Option(this.manager, "sweet", this.mustStranger, "Sweet! I've always wanted to off a monarch. Viva la revolución!"));
        activeMenu.add(new Option(this.manager, "silent", this.mustStranger, "[Silently continue to the cabin.]"));
        activeMenu.add(new Option(this.manager, "leave", this.cantTryAbort, "[Turn around and leave.]", 0, Chapter.STRANGER));

        this.repeatActiveMenu = true;
        while (this.repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);

            switch (this.activeOutcome) {
                case "question1":
                case "refuse":
                    canReluctant.set();
                    mainScript.runSection(activeOutcome);
                    break;

                case "question2":
                case "whyDanger":
                    canQuestionFollowUp.set(false);
                    mainScript.runSection(activeOutcome);
                    break;

                case "someoneElse":
                    if (activeMenu.hasBeenPicked("question2") || activeMenu.hasBeenPicked("whyDanger")) {
                        mainScript.runSection("someoneElseA");
                    } else {
                        mainScript.runSection("someoneElseB");
                    }

                    break;

                case "prize":
                    this.askedPrize = true;
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
                    this.repeatActiveMenu = false;
                    mainScript.runSection(activeOutcome);
                    break;

                case "cGoHill":
                    if (mustStranger.check()) {
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
                    if (this.ch1AttemptStranger()) {
                        return ChapterEnding.TOSTRANGER;
                    } else {
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
        if (!this.skipHillDialogue) mainScript.runSection("hillDialogue");

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

                    if (this.ch1AttemptStranger()) {
                        return ChapterEnding.TOSTRANGER;
                    } else {
                        this.currentLocation = GameLocation.HILL;
                        this.repeatActiveMenu = false;
                        canStranger = false;
                        break;
                    }
                    
                default:
                    this.giveDefaultFailResponse(activeOutcome);
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
                case "cTakeBlade":
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
                    cantTryAbort.set(false);
                    this.withBlade = false;
                    manager.addToPlaylist("The World Ender");
                    return (this.isHarsh) ? this.ch1BasementHarsh() : this.ch1BasementSoft();

                case "cGoHill":
                    if (manager.hasVisited(Chapter.STRANGER)) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    } else if (!canStranger) {
                        mainScript.runSection("alreadyTried");
                        break;
                    }

                    if (this.ch1AttemptStranger()) {
                        return ChapterEnding.TOSTRANGER;
                    } else {
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
     * @return true if the player commits to the Stranger; false if the player returns to the cabin
     */
    private boolean ch1AttemptStranger() {
        this.secondaryScript = new Script(this.manager, this.parser, "Chapter 1/StrangerAttempt");
        cantTryAbort.set();
        secondaryScript.runSection();

        OptionsMenu leaveMenu = new OptionsMenu();
        leaveMenu.add(new Option(this.manager, "ugh", this.mustStranger, "Okay, fine. You're persistent. I'll go to the cabin and I'll slay the Princess. Ugh!"));
        leaveMenu.add(new Option(this.manager, "maybe", this.mustStranger, "Okay, fine. I'll go to the cabin and I'll talk to the Princess. Maybe I'll slay her. Maybe I won't. I guess we'll see."));
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
                    return false;

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
        leaveMenu.add(new Option(this.manager, "cabin", this.mustStranger, "Okay, okay! I'm going into the cabin. Sheesh."));
        leaveMenu.add(new Option(this.manager, "commit", "[Turn around (again) and leave (again).]"));

        repeatMenu = true;
        while (repeatMenu) {
            outcome = parser.promptOptionsMenu(leaveMenu);
            switch (outcome) {
                case "cGoCabin":
                    this.skipHillDialogue = true;
                    secondaryScript.runSection("cabinSilent");
                    return false;

                case "cabin":
                    this.skipHillDialogue = true;
                    secondaryScript.runSection("cabin");
                    return false;

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
                    this.skipHillDialogue = true;
                    secondaryScript.runSection("cabin2");
                    return false;

                default:
                    this.giveDefaultFailResponse(outcome);
            }
        }

        this.currentLocation = GameLocation.LEAVING;

        leaveMenu = new OptionsMenu();
        leaveMenu.add(new Option(this.manager, "cabin", this.mustStranger, "There's no fighting this, is there? I have to go into the cabin, don't I? Fine."));
        leaveMenu.add(new Option(this.manager, "commit", "Oh, yeah? Well I guess I start walking in a different direction. Again. In fact, I'm going to just keep trekking through the wilderness until I find a way out of this place.", 0));

        repeatMenu = true;
        while (repeatMenu) {
            outcome = parser.promptOptionsMenu(leaveMenu);
            switch (outcome) {
                case "cGoHill":
                case "cabin":
                    this.skipHillDialogue = true;
                    secondaryScript.runSection("cabin3");
                    return false;

                case "cGoLeave":
                case "commit":
                    if (manager.confirmContentWarnings(Chapter.STRANGER)) repeatMenu = false;
                    break;

                default:
                    this.giveDefaultFailResponse(outcome);
            }
        }

        secondaryScript.runSection("strangerCommit");
        return true;
    }

    /**
     * Runs the beginning of the basement sequence with the soft princess (did not take the blade)
     * @return the ending reached by the player
     */
    private ChapterEnding ch1BasementSoft() {
        this.secondaryScript = new Script(this.manager, this.parser, "Chapter 1/Basement1Soft");

        boolean canDamsel = !manager.hasVisited(Chapter.DAMSEL);
        boolean canBeast = !manager.hasVisited(Chapter.BEAST);
        boolean canWitch = !manager.hasVisited(Chapter.WITCH);

        this.canFree = canDamsel || canWitch;
        this.canNotFreeSoft = canBeast || canWitch || this.canNightmare;
        canSlay.set(canBeast || canWitch);

        this.currentLocation = GameLocation.STAIRS;
        this.withPrincess = true;
        mainScript.runSection("stairs");
        secondaryScript.runSection();

        Condition jokeKill = new Condition();
        InverseCondition noJokeKill = jokeKill.getInverse();
        boolean hereToSave = false;
        boolean lieSave = false;

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "hi", "\"Hi!\""));
        activeMenu.add(new Option(this.manager, "checkIn", "\"Just checking in on you.\""));
        activeMenu.add(new Option(this.manager, "hereToSave", "\"I'm here to save you!\""));
        activeMenu.add(new Option(this.manager, "lieSave", "(Lie) \"I'm here to save you!\"", this.canFree));
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
                    jokeKill.set();
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

        if (jokeKill.check()) {
            secondaryScript.runSection("jokeKillBasement");
        } else if (hereToSave) {
            secondaryScript.runConditionalSection("hereToSaveBasement", lieSave);
        } else {
            secondaryScript.runSection("genericBasement");
        }

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "talk", "\"Hold on. Let's talk a bit first...\""));
        activeMenu.add(new Option(this.manager, "free", !this.canFree, "\"I'll see what I can do.\" [Examine the chains.]", 0));

        this.repeatActiveMenu = true;
        while (this.repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (this.activeOutcome) {
                case "talk":
                    this.repeatActiveMenu = false;
                    break;

                case "free":
                    if (!manager.confirmContentWarnings("self-mutilation", true)) break;

                    return this.ch1RescueSoft(false, hereToSave && !lieSave, false);
                
                case "cGoStairs":
                    mainScript.runSection("basementLeaveFail");
                    break;

                default:
                    this.giveDefaultFailResponse(this.activeOutcome);
            }
        }

        mainScript.runSection("princessTalk");

        String outcome = "";
        boolean repeatSub;

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "name", "(Explore) \"What's your name?\""));
        activeMenu.add(new Option(this.manager, "name2", "(Explore) \"So is Princess your name?\"", activeMenu.get("name")));
        activeMenu.add(new Option(this.manager, "whyImprisoned", "(Explore) \"I don't know anything about you. For all I know you're locked up down here for a reason.\""));
        activeMenu.add(new Option(this.manager, "notKidding", "(Explore) \"I wasn't kidding when I said I was sent here to kill you. You're apparently going to end the world.\"", jokeKill, this.noShareTask));
        activeMenu.add(new Option(this.manager, "eat", "(Explore) \"If I'm the first person you've seen in a while, what have you been eating? Or drinking?\""));
        activeMenu.add(new Option(this.manager, "shareTask", "(Explore) \"I was sent here to slay you. You're apparently supposed to end the world...\"", noJokeKill, this.noShareTask));
        activeMenu.add(new Option(this.manager, "whatWouldYouDo", "(Explore) \"What are you going to do if I let you out of here?\"", this.noShareTask));
        activeMenu.add(new Option(this.manager, "compromiseA", !this.canNightmare, "\"I won't kill you, but I can't just set you free. It's too risky. What if I stayed for a while and just kept you company? Maybe then everyone could be happy.\"", new OrCondition(jokeKill, this.sharedTask)));
        activeMenu.add(new Option(this.manager, "compromiseB", !this.canNightmare, "\"I'm going to keep you locked away down here. At least for a little bit. We can get to know each other better while I decide what to do.\" [Keep her locked away.]"));
        activeMenu.add(new Option(this.manager, "getBladeSorry", this.cantSlay, "\"I'm sorry, but I just can't trust you. This doesn't add up, and it isn't worth the risk to take your word over the potential fate of the world.\" [Retrieve the blade.]", 0));
        activeMenu.add(new Option(this.manager, "getBladeSilent", this.cantSlay, "[Go back upstairs to retrieve the blade without saying another word.]", 0));
        activeMenu.add(new Option(this.manager, "free", !this.canFree, "\"I can't believe they've been keeping you down here like this! I'm getting you out of here.\" [Examine the chains.]", 0));
        activeMenu.add(new Option(this.manager, "freeDontRegret", !this.canFree, "\"Okay, I'm going to get you out of here. Don't make me regret this.\" [Examine the chains.]", 0));

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
                    if (this.ch1ShareTaskSoft(false)) {
                        return this.ch1RescueSoft(true, false, false);
                    } else {
                        if (this.whatWouldYouDo) activeMenu.setCondition("whatWouldYouDo", false);
                    }

                case "shareTask":
                    this.subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "notDanger", "\"But I don't think you're actually dangerous.\""));
                    subMenu.add(new Option(this.manager, "notSure", "\"But I wanted to see you for myself. I'm still not sure what to believe.\""));
                    subMenu.add(new Option(this.manager, "notRight", "\"I'm starting to think it's true. There's something about you that doesn't feel right.\""));

                    switch (parser.promptOptionsMenu(subMenu)) {
                        case "notDanger":
                        case "notSure":
                            if (this.ch1ShareTaskSoft(false)) {
                                return this.ch1RescueSoft(true, hereToSave && !lieSave, false);
                            } else {
                                if (this.whatWouldYouDo) activeMenu.setCondition("whatWouldYouDo", false);
                            }
                            
                            break;
                            
                        case "notRight":
                            secondaryScript.runSection("shareNotRight");

                            if (this.ch1ShareTaskSoft(true)) {
                                return this.ch1RescueSoft(true, hereToSave && !lieSave, false);
                            } else {
                                if (this.whatWouldYouDo) activeMenu.setCondition("whatWouldYouDo", false);
                            }
                    }

                    break;

                case "whatWouldYouDo":
                    this.whatWouldYouDo = true;
                    secondaryScript.runSection("whatWouldYouDo");
                    secondaryScript.runSection("whatDoA");
                    break;

                case "compromiseA":
                case "compromiseB":
                    this.repeatActiveMenu = false;
                    if (jokeKill.check() && !this.knowsDestiny) {
                        secondaryScript.runSection("compromiseA");
                    } else {
                        secondaryScript.runSection("compromiseB");
                    }

                    this.subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "getBlade", this.cantSlay, "[Retrieve the blade.]", 0));
                    subMenu.add(new Option(this.manager, "free", !this.canFree, "\"Okay. Let's get you out of here.\" [Examine the chains.]", 0));
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

                                return this.ch1RescueSoft(true, hereToSave && !lieSave, false);

                            case "lock":
                                if (manager.confirmContentWarnings(Chapter.NIGHTMARE)) repeatSub = false;
                                break;
                        }
                    }

                    // Lock continues here
                    secondaryScript.runSection();

                    this.subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "lock", "No, we're sticking to the plan and locking her away."));
                    subMenu.add(new Option(this.manager, "slay", this.cantSlay, "Oh that's a relief! I was afraid I'd already committed to not slaying her.", 0));

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

                    if (!this.knowsDestiny) {
                        if (jokeKill.check()) secondaryScript.runSection("retrieveA");
                        else secondaryScript.runSection("retrieveB");
                    }

                    return this.ch1RetrieveBlade(true);
                
                case "cGoStairs":
                    secondaryScript.runSection("attemptLeave");

                    this.subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "slay", this.cantSlay, "Yes. Something here just doesn't add up, and it isn't worth the risk to take her word over the potential fate of the world. [Retrieve the blade.]"));
                    subMenu.add(new Option(this.manager, "lock", !this.canNightmare, "No, but I can't just set her free. I don't have enough information to make a decision yet. I'm going to keep her locked away down here, at least for a little bit. We can get to know each other better while I decide what to do. [Keep her locked away.]", 0));
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

                                this.subMenu = new OptionsMenu(true);
                                subMenu.add(new Option(this.manager, "lock", "No, we're sticking to the plan and locking her away."));
                                subMenu.add(new Option(this.manager, "slay", this.cantSlay, "Oh that's a relief! I was afraid I'd already committed to not slaying her.", 0));

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

                    return this.ch1RescueSoft(false, hereToSave && !lieSave, true);

                case "free":
                    if (!manager.confirmContentWarnings("self-mutilation", true)) break;

                    return this.ch1RescueSoft(false, hereToSave && !lieSave, false);

                default:
                    this.giveDefaultFailResponse(activeOutcome);
            }
        }

        throw new RuntimeException("No ending reached");
    }

    /**
     * The player tells the soft Princess that she's allegedly going to end the world
     * @param lateJoin whether or not to skip the first section of dialogue
     * @return true if the player chooses to free the Princess; false otherwise
     */
    private boolean ch1ShareTaskSoft(boolean lateJoin) {
        this.knowsDestiny = true;
        sharedTask.set();

        if (!lateJoin) secondaryScript.runSection("shareTask");

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
                    secondaryScript.runSection("shareNoDanger");

                    OptionsMenu finalShareMenu = new OptionsMenu(true);
                    finalShareMenu.add(new Option(this.manager, "talk", "\"I still have a few more questions before we leave.\""));
                    finalShareMenu.add(new Option(this.manager, "free", !this.canFree, "\"I'll see what I can do.\" [Examine the chains.]", 0));

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
     * @return the ending reached by the player
     */
    private ChapterEnding ch1RescueSoft(boolean lateJoin, boolean hereToSaveTruth, boolean dontRegret) {
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
                        triedDoor = true;
                        secondaryScript.runSection("rescueStairsTryFirst");
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
        activeMenu.add(new Option(this.manager, "save", !this.canFree, "[Save the Princess.]", 0));
        activeMenu.add(new Option(this.manager, "slay", !this.canNotFreeSoft, "[Slay the Princess.]", !hereToSaveTruth));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "save":
                    if (!manager.confirmContentWarnings("mutilation; loss of bodily autonomy", true)) break;

                    this.repeatActiveMenu = false;
                    break;

                case "cSlayPrincess":
                    if (!this.canNotFreeSoft || hereToSaveTruth) {
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
                    if (!manager.confirmContentWarnings(Chapter.WITCH)) break;
                    
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
                    if (!manager.confirmContentWarnings(Chapter.WITCH)) break;

                    secondaryScript.runSection("rescueControlledSlay2");
                    return ChapterEnding.TOWITCH;

                case "resist":
                    if (!manager.confirmContentWarnings(Chapter.DAMSEL)) break;

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
     * @return the ending reached by the player
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
                    if (!manager.confirmContentWarnings(Chapter.BEAST)) break;
                    
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
                    if (!manager.confirmContentWarnings(Chapter.WITCH)) break;
                    
                    this.repeatActiveMenu = false;
                    secondaryScript.runSection("rescueSlayFinish");

                    return ChapterEnding.TOWITCHBETRAYAL;

                case "cGoStairs":
                    if (manager.hasVisited(Chapter.NIGHTMARE)) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                case "run":
                    if (!manager.confirmContentWarnings(Chapter.NIGHTMARE)) break;
                    
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
     * @return the ending reached by the player
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

        String outcome;
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

                    this.subMenu = new OptionsMenu();
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

                    this.subMenu = new OptionsMenu();
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

                    secondaryScript.runConditionalSection("locked", worthRisk);
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

                    this.subMenu = new OptionsMenu(true);
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
     * @return the ending reached by the player
     */
    private ChapterEnding ch1BasementHarsh() {
        this.secondaryScript = new Script(this.manager, this.parser, "Chapter 1/Basement1Harsh");
        this.canFree = !manager.hasVisitedAll(Chapter.PRISONER, Chapter.TOWER, Chapter.ADVERSARY);

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

        if (jokeKill) {
            secondaryScript.runSection("jokeKillBasement");
        } else {
            secondaryScript.runSection("genericBasement");
        }

        boolean undecided = false;
        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "noWay", this.mustSpectre, "\"What? No way. Why would you even think that?\""));
        activeMenu.add(new Option(this.manager, "noJoke", "\"Yeah, it wasn't a joke.\"", jokeKill));
        activeMenu.add(new Option(this.manager, "caughtMe", this.mustSpectre, "\"Okay, yeah, you caught me. I'm here to slay you.\"", !jokeKill));
        activeMenu.add(new Option(this.manager, "nuhUh", this.mustSpectre, "\"Nuh... nuh uh!\"")); // yes this is a real line from the original game
        activeMenu.add(new Option(this.manager, "undecided", this.mustSpectre, "\"I haven't decided yet.\""));
        activeMenu.add(new Option(this.manager, "talk", this.mustSpectre, "\"I'm just here to talk.\""));
        activeMenu.add(new Option(this.manager, "steel", "[Steel your nerves and step forward.]"));

        switch (parser.promptOptionsMenu(activeMenu)) {
            case "undecided":
                undecided = true;
            case "noWay":
            case "caughtMe":
            case "nuhUh":
            case "talk":
                this.harshHesitated = true;
            case "noJoke":
                this.repeatActiveMenu = false;
                secondaryScript.runSection(activeOutcome);
                break;

            case "steel":
                this.canDropBlade = true;
                this.canSlayPrincess = true;
                
                return this.ch1SteelNervesHarsh();
        }

        this.canDropBlade = true;
        this.canSlayPrincess = true;
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "drop", this.mustSpectre, "[Drop it.]"));
        activeMenu.add(new Option(this.manager, "tighten", "[Tighten your grip.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (this.activeOutcome) {
                case "cDrop":
                    if (this.mustSpectre) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                case "drop":
                    if (undecided) {
                        secondaryScript.runSection("dropA");
                    } else {
                        secondaryScript.runSection("dropB");
                    }

                    return this.ch1DropBladeHarsh(false);

                case "cSlayPrincess":
                case "tighten":
                    secondaryScript.runSection("tighten");
                    
                    return this.ch1SteelNervesHarsh();

                default:
                    this.giveDefaultFailResponse(this.activeOutcome);
            }
        }
        
        throw new RuntimeException("No ending reached");
    }

    /**
     * The player steels their nerves and steps forward
     * @return the ending reached by the player
     */
    private ChapterEnding ch1SteelNervesHarsh() {
        boolean canSpectre = !manager.hasVisited(Chapter.SPECTRE);
        canSlay.set((this.harshHesitated) ? this.canHesitateSlay : this.canSteelSlay);

        Condition noExplore = new Condition(true);
        
        secondaryScript.runConditionalSection("steelJoin", this.harshHesitated);

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "bluff", this.mustSpectre, "(Explore) What if she isn't bluffing? What if she kills us?", Chapter.TOWER, noExplore));
        activeMenu.add(new Option(this.manager, "isArmed", this.mustSpectre, "(Explore) Are you *sure* she's not armed?", Chapter.RAZOR, noExplore));
        activeMenu.add(new Option(this.manager, "sorry", this.mustSpectre, "(Explore) \"I'm sorry. Can we just talk?\"", noExplore));
        activeMenu.add(new Option(this.manager, "noDrop", this.mustSpectre, "\"I'm not dropping the blade.\""));
        activeMenu.add(new Option(this.manager, "drop", this.mustSpectre, "[Drop the blade.]"));
        activeMenu.add(new Option(this.manager, "slay", this.cantSlay, "[Slay the Princess.]", 0));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (this.activeOutcome) {
                case "bluff":
                    noExplore.set(false);
                    this.harshHesitated = true;
                    this.harshAfraid = true;
                    canSlay.set(this.canTower);
                    
                    secondaryScript.runSection("steelBluff");
                    break;

                case "isArmed":
                    noExplore.set(false);
                    this.harshHesitated = true;
                    this.harshIsArmed = true;
                    canSlay.set(this.canRazor);
                    
                    secondaryScript.runSection("steelIsArmed");
                    break;

                case "sorry":
                    noExplore.set(false);
                    canSlay.set(this.canHesitateSlay);
                    
                    secondaryScript.runSection("steelSorry");
                    break;

                case "noDrop":
                    this.repeatActiveMenu = false;
                    canSlay.set(this.canHesitateSlay);
                    break;

                case "cDrop":
                    if (this.mustSpectre) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                case "drop":
                    secondaryScript.runSection("dropA");

                    return this.ch1DropBladeHarsh(true);

                case "cSlayPrincess":
                    if (cantSlay.check()) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                case "slay":
                    if (!this.harshHesitated && !this.canRazor) {
                        if (!manager.confirmContentWarnings(Chapter.SPECTRE)) break;
                    } else if (this.harshIsArmed || (!this.harshHesitated && !canSpectre)) {
                        if (!manager.confirmContentWarnings(Chapter.RAZOR)) break;
                    } else if (this.harshAfraid) {
                        if (!manager.confirmContentWarnings(Chapter.TOWER)) break;
                    }

                    return this.ch1SlayHarsh();

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
        activeMenu.add(new Option(this.manager, "slay", this.cantSlay, "[Slay the Princess.]", 0));

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

                    return this.ch1DropBladeHarsh(true);

                case "cSlayPrincess":
                    if (cantSlay.check()) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                case "slay":
                    if (this.harshIsArmed) {
                        if (!manager.confirmContentWarnings(Chapter.RAZOR)) break;
                    } else if (this.harshAfraid) {
                        if (!manager.confirmContentWarnings(Chapter.TOWER)) break;
                    }

                    return this.ch1SlayHarsh();

                default:
                    this.giveDefaultFailResponse(this.activeOutcome);
            }
        }

        throw new RuntimeException("No ending reached");
    }

    /**
     * The player drops the blade to talk to the harsh Princess
     * @param steeled whether the player previously steeled their nerves or dropped the blade immediately
     * @return the ending reached by the player
     */
    private ChapterEnding ch1DropBladeHarsh(boolean steeled) {
        this.canFree = !manager.hasVisitedAll(Chapter.PRISONER, Chapter.TOWER, Chapter.ADVERSARY);

        this.droppedBlade1 = true;
        this.canDropBlade = false;
        this.hasBlade = false;

        int vagueCount = 0;
        boolean howFree = false;
        Condition noSmallTalk = new Condition(true);

        String outcome;
        boolean repeatSub;
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "awkward", "(Explore) \"Yeah, it's uh... pretty awkward.\"", noSmallTalk));
        activeMenu.add(new Option(this.manager, "relationship", "(Explore) \"A 'relationship?' Are you coming on to me?\"", noSmallTalk));
        activeMenu.add(new Option(this.manager, "howFree", "(Explore) \"How would I get you out of here?\""));
        activeMenu.add(new Option(this.manager, "shareTaskA", !this.canTower, "(Explore) \"I'm here because you're supposed to end the world.\"", this.noShareTask));
        activeMenu.add(new Option(this.manager, "shareTaskB", !this.canTower, "(Explore) \"There's people out there who think you're going to end the world. What do you have to say about that?\"", this.noShareTask));
        activeMenu.add(new Option(this.manager, "name", "(Explore) \"What's your name?\""));
        activeMenu.add(new Option(this.manager, "howLong", "(Explore) \"How long have you been down here?\""));
        activeMenu.add(new Option(this.manager, "whyHere", "(Explore) \"Do you know *why* I'm here to kill you?\"", this.noShareTask));
        activeMenu.add(new Option(this.manager, "enough", "\"Okay, we've talked enough...\""));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "awkward":
                    noSmallTalk.set(false);
                    secondaryScript.runSection("awkward");

                    this.subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "reasons", "\"I have my reasons. Do you think I'd just come here to kill someone without even knowing why? That'd be ridiculous!\""));
                    subMenu.add(new Option(this.manager, "deflect", "\"Do you know why I'm here to kill you?\""));
                    subMenu.add(new Option(this.manager, "shareTask", !this.canTower, "\"You're supposed to end the world.\"", this.noShareTask));
                    subMenu.add(new Option(this.manager, "notSure", "\"I've been told things, but I'm not sure what to believe.\""));

                    switch (parser.promptOptionsMenu(subMenu)) {
                        case "reasons":
                            secondaryScript.runConditionalSection("awkwardReasons", steeled);
                            break;

                        case "deflect":
                            secondaryScript.runConditionalSection("awkwardDeflect", steeled);
                            break;

                        case "shareTask":
                            switch (this.ch1ShareTaskHarsh(steeled)) {
                                case 1: return this.ch1SlayHarsh();
                                case 2: return this.ch1RescueHarsh(howFree);
                            }

                            vagueCount += 1;
                            break;

                        case "notSure":
                            secondaryScript.runSection("awkwardNotSure");
                            break;
                    }

                case "relationship":
                    noSmallTalk.set(false);
                    secondaryScript.runSection("relationship");
                    break;

                case "howFree":
                    howFree = true;
                    secondaryScript.runSection("howFree");
                    break;

                case "shareTaskA":
                    switch (this.ch1ShareTaskHarsh(steeled)) {
                        case 1: return this.ch1SlayHarsh();
                        case 2: return this.ch1RescueHarsh(howFree);
                    }

                    vagueCount += 1;
                    break;

                case "shareTaskB":
                    switch (this.ch1ShareTaskHarsh(steeled)) {
                        case 1: return this.ch1SlayHarsh();
                        case 2: return this.ch1RescueHarsh(howFree);
                    }

                    vagueCount += 1;
                    break;

                case "name":
                case "howLong":
                    vagueCount += 1;
                    secondaryScript.runConditionalSection(activeOutcome, vagueCount);
                    break;

                case "whyHere":
                    secondaryScript.runSection("whyHere");

                    this.subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "shareTask", !this.canTower, "\"You're apparently going to end the world.\""));
                    subMenu.add(new Option(this.manager, "told", "\"I know what I've been told. Whether or not I believe it is an entirely different matter.\""));
                    subMenu.add(new Option(this.manager, "lie", "(Lie) \"No.\""));
                    subMenu.add(new Option(this.manager, "silent", "[Remain silent.]"));

                    switch (parser.promptOptionsMenu(subMenu)) {
                        case "shareTask":
                            switch (this.ch1ShareTaskHarsh(steeled)) {
                                case 1: return this.ch1SlayHarsh();
                                case 2: return this.ch1RescueHarsh(howFree);
                            }

                            vagueCount += 1;
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
                    if (cantSlay.check()) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    } else if (this.harshIsArmed && !manager.confirmContentWarnings(Chapter.RAZOR)) {
                        break;
                    } else if (this.harshAfraid && !manager.confirmContentWarnings(Chapter.TOWER)) {
                        break;
                    }

                    return this.ch1SlayHarsh();

                case "cGoStairs":
                    secondaryScript.runSection("attemptLeave");

                    this.subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "lock", !this.canNightmare, "I don't have enough information to make a decision yet. I'm going to keep her locked away down here, at least for a little bit. We can get to know each other better while I decide what to do. [Keep her locked away.]", 0));
                    subMenu.add(new Option(this.manager, "nevermind", "You're right. I still have a few more questions for her before I make a decision. [Turn back.]"));

                    repeatSub = true;
                    while (repeatSub) {
                        outcome = parser.promptOptionsMenu(subMenu);
                        switch (outcome) {
                            case "lock":
                                if (!manager.confirmContentWarnings(Chapter.NIGHTMARE)) break;

                                secondaryScript.runSection();

                                this.subMenu = new OptionsMenu(true);
                                subMenu.add(new Option(this.manager, "lock", "No, we're sticking to the plan and locking her down here."));
                                subMenu.add(new Option(this.manager, "slay", !this.canTower, "Oh that's a relief! I was afraid I'd already committed to not slaying her.", 0));

                                while (repeatSub) {
                                    switch (parser.promptOptionsMenu(subMenu)) {
                                        case "lock":
                                            secondaryScript.runSection();
                                            this.ch1ToNightmare(false, false);
                                            return ChapterEnding.TONIGHTMARE;

                                        case "slay":
                                            if (!manager.confirmContentWarnings(Chapter.TOWER)) break;

                                            this.harshAfraid = true;
                                            secondaryScript.runSection("slayFromLock");
                                            return this.ch1SlayHarsh();
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
        activeMenu.add(new Option(this.manager, "slay", this.cantSlay, "[Slay the Princess.]", 0));
        activeMenu.add(new Option(this.manager, "free", !this.canFree, "\"I'm getting you out of here.\" [Examine the chains.]", 0));
        activeMenu.add(new Option(this.manager, "lock", !this.canNightmare, "\"I'm going to keep you locked away down here. At least for a bit. We can get to know each other better while I decide what to do.\" [Keep her locked away.]", 0));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "cSlayPrincess":
                    if (cantSlay.check()) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                case "slay":
                    if (this.harshIsArmed && !manager.confirmContentWarnings(Chapter.RAZOR)) {
                        break;
                    } else if (this.harshAfraid && !manager.confirmContentWarnings(Chapter.TOWER)) {
                        break;
                    }

                    return this.ch1SlayHarsh();

                case "free":
                    secondaryScript.runSection("enoughRescue");
                    return this.ch1RescueHarsh(howFree);

                case "cGoStairs":
                    if (!this.canNightmare) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                case "lock":
                    if (manager.confirmContentWarnings(Chapter.NIGHTMARE)) this.repeatActiveMenu = false;
                    break;

                default:
                    this.giveDefaultFailResponse();
            }
        }

        // Lock continues here
        secondaryScript.runSection("lock");

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "slay", this.cantSlay.check() && !this.canTower, "[Slay the Princess.]"));
        activeMenu.add(new Option(this.manager, "free", !this.canFree, "\"Okay. Let's get you out of here.\" [Examine the chains.]"));
        activeMenu.add(new Option(this.manager, "lock", "Uh, I *made* my choice. I'm locking her in the basement."));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "cSlayPrincess":
                    if (cantSlay.check() && !this.canTower) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                case "slay":
                    if (this.harshIsArmed && !manager.confirmContentWarnings(Chapter.RAZOR)) {
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.TOWER)) {
                       break; 
                    }

                    this.harshAfraid = true;
                    return this.ch1SlayHarsh();

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
        activeMenu.add(new Option(this.manager, "slay", !this.canTower, "Oh that's a relief! I was afraid I'd already committed to not slaying her.", 0));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "lock":
                    secondaryScript.runSection();
                    this.ch1ToNightmare(false, false);
                    return ChapterEnding.TONIGHTMARE;

                case "slay":
                    if (!manager.confirmContentWarnings(Chapter.TOWER)) break;
                    
                    this.harshAfraid = true;
                    secondaryScript.runSection("slayFromLock");
                    return this.ch1SlayHarsh();
            }
        }

        throw new RuntimeException("No ending reached");
    }

    /**
     * The player tells the harsh Princess that she's allegedly going to end the world
     * @param steeled whether the player previously steeled their nerves or dropped the blade immediately
     * @return 1 if the player decides to slay the Princess; 2 if the player decides to free the Princess; 0 otherwise
     */
    private int ch1ShareTaskHarsh(boolean steeled) {
        this.knowsDestiny = true;
        sharedTask.set();
        this.harshAfraid = true;
        canSlay.set(this.canTower);
        secondaryScript.runConditionalSection("shareTask", steeled);

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
                this.harshAfraid = false;
                canSlay.set((this.harshIsArmed) ? this.canRazor : this.canHesitateSlay);
                secondaryScript.runSection("shareYouTell");
                break;

            case "reasons":
                secondaryScript.runSection("shareReasons");
                break;
            
            case "trustYou":
                this.harshAfraid = false;
                canSlay.set((this.harshIsArmed) ? this.canRazor : this.canHesitateSlay);
                secondaryScript.runSection("shareTrustYou");

                OptionsMenu finalShareMenu = new OptionsMenu();
                finalShareMenu.add(new Option(this.manager, "talk", "\"I still have a few more questions before I decide what to do.\""));
                finalShareMenu.add(new Option(this.manager, "slay", this.cantSlay, "\"Actually, I've changed my mind. I don't trust you.\" [Slay the Princess.]", 0));
                finalShareMenu.add(new Option(this.manager, "free", !this.canFree, "\"I'll see what I can do.\" [Examine the chains.]", 0));

                boolean repeatMenu = true;
                String outcome;
                while (repeatMenu) {
                    outcome = parser.promptOptionsMenu(finalShareMenu);
                    switch (outcome) {
                        case "talk":
                            secondaryScript.runSection();
                            return 0;

                        case "cSlayPrincess":
                            if (cantSlay.check()) {
                                parser.printDialogueLine(CANTSTRAY);
                                break;
                            }
                        case "slay":
                            if (this.harshIsArmed && !manager.confirmContentWarnings(Chapter.RAZOR)) break;

                            return 1;

                        case "free":
                            if (!manager.confirmContentWarnings("self-mutilation", true)) break;

                            return 2;

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
     * @return the ending reached by the player
     */
    private ChapterEnding ch1SlayHarsh() {
        // !harshHesitated = Spectre / Razor
        // harshIsArmed = forced Razor
        // harshAfraid = forced Tower
        // otherwise = Adversary / Tower / Nightmare

        this.hasBlade = true;
        this.canDropBlade = false;
        if (this.harshIsArmed) {
            return this.ch1SlayHarshForceRazor();
        } else if (this.harshAfraid) {
            secondaryScript.runSection("towerCommitUnharmed");
            return ChapterEnding.TOTOWERUNHARMED;
        } else if (!this.harshHesitated) {
            return this.ch1SlayHarshSteeled();
        } else {
            return this.ch1SlayHarshHesitated();
        }
    }

    /**
     * The player slays the harsh Princess without hesitation (leads to Chapter II: The Spectre / The Razor)
     * @return the ending reached by the player
     */
    private ChapterEnding ch1SlayHarshSteeled() {
        secondaryScript.runSection("steelSlay");

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "yes", "Of course it is. She's dead.", 0, Chapter.SPECTRE));
        activeMenu.add(new Option(this.manager, "maybe", "I'm not sure. I feel like she has to have some kind of trick up her sleeve.", Chapter.RAZOR));
        activeMenu.add(new Option(this.manager, "no", "Of course not. That was too easy.", Chapter.RAZOR));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "yes":
                    if (!manager.confirmContentWarnings(Chapter.SPECTRE, "suicide")) break;

                    secondaryScript.runSection("steelSlayYes");
                    return this.ch1SlaySuccess();
                
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
        activeMenu.add(new Option(this.manager, "blade", "[Remove the blade.]", 0));
        activeMenu.add(new Option(this.manager, "pulse", "[Check for a pulse.]", 0));
        activeMenu.add(new Option(this.manager, "leave", "You're right. She's dead. Let's just get out of here.", 0, Chapter.SPECTRE));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "cTakeBlade":
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
                    return this.ch1SlaySuccess();

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
     * @return the ending reached by the player
     */
    private ChapterEnding ch1SlaySuccess() {
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
        activeMenu.add(new Option(this.manager, "prizeYay", "Wait, is this my prize? This is great! Thank you so much.", this.askedPrize));
        activeMenu.add(new Option(this.manager, "prizeBoo", "Wait, is this my prize? This sucks!", this.askedPrize));
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
                if (this.askedPrize) secondaryScript.runSection("successPrizeBoo");
                else secondaryScript.runSection("successBullshit");
                break;

            case "ok":
                secondaryScript.runSection("successOk");
                break;

            case "better":
                secondaryScript.runSection("successBetter");
                break;
        }

        Condition noExplore = new Condition(true);
        Condition localGoodEndingAttempt = new Condition();
        InverseCondition noLocalAttempt = localGoodEndingAttempt.getInverse();

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "explore", "(Explore) Didn't you hear the Narrator? I'm happy. We're happy."));
        activeMenu.add(new Option(this.manager, "notHappyA", "Hmm, okay, maybe I'm not happy. And I'm not just saying that because you're the last person I talked to.", activeMenu.get("explore"), noLocalAttempt));
        activeMenu.add(new Option(this.manager, "notHappyB", "Hmm, okay, maybe I'm not happy. And I'm not just saying that because you're the last person I talked to.", manager.goodEndingAttempted(), new OrCondition(noExplore, localGoodEndingAttempt)));
        activeMenu.add(new Option(this.manager, "sure", manager.goodEndingAttempted(), "No, we're happy. I'm sure of it.", activeMenu.get("explore")));
        activeMenu.add(new Option(this.manager, "hellNo", "Hell no, do you have any idea how to get us the heck out of here?", manager.goodEndingAttempted()));
        activeMenu.add(new Option(this.manager, "ofCourse", manager.goodEndingAttempted(), "Of course we are. I like it here."));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "explore":
                    noExplore.set(false);
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
                        manager.goodEndingAttempted().set();
                        localGoodEndingAttempt.set();

                        System.out.println();
                        parser.printDialogueLine(CANTSTRAY);
                        manager.unlock("goodEndingFail");
                        break;
                    }
                
                case "hellNo":
                    secondaryScript.runSection("successHellNo");
                    
                    if (this.ch1HeroSuggestSpectre()) {
                        return ChapterEnding.TOSPECTRE;
                    } else if (this.isFirstVessel) {
                        return ChapterEnding.GOODENDING;
                    } else {
                        manager.goodEndingAttempted().set();
                        localGoodEndingAttempt.set();

                        System.out.println();
                        parser.printDialogueLine(CANTSTRAY);
                        manager.unlock("goodEndingFail");
                        break;
                    }

                case "sure":
                case "ofCourse":
                    secondaryScript.runSection("attemptGoodEnding");

                    if (this.isFirstVessel) {
                        return ChapterEnding.GOODENDING;
                    } else {
                        manager.goodEndingAttempted().set();
                        localGoodEndingAttempt.set();

                        System.out.println();
                        parser.printDialogueLine(CANTSTRAY);
                        manager.unlock("goodEndingFail");
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
        this.subMenu = new OptionsMenu();
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
                case "cTakeBladeFail":
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
     * @return the ending reached by the player
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
     * @return the ending reached by the player
     */
    private ChapterEnding ch1RescueHarsh(boolean howFree) {
        boolean canPrisoner = !manager.hasVisited(Chapter.PRISONER);
        canSlay.set(this.canHesitateSlay);

        secondaryScript.runConditionalSection("rescue", howFree);

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
        activeMenu.add(new Option(this.manager, "save", !this.canFree, "[Save the Princess.]", 0));
        activeMenu.add(new Option(this.manager, "slay", this.cantSlay, "[Slay the Princess.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "save":
                    if (!manager.confirmContentWarnings("mutilation; loss of bodily autonomy", true)) break;

                    this.repeatActiveMenu = false;
                    break;

                case "cSlayPrincess":
                    if (cantSlay.check()) {
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
        Option slay = new Option(this.manager, "slay", !this.canTower, "[Slay the Princess.]", 0);

        this.activeMenu = new OptionsMenu();
        for (int i = 0; i < 13; i++) activeMenu.add(slay, "slay" + i);
        activeMenu.add(new Option(this.manager, "warn", !canPrisoner, "[Warn her.]"));
        for (int i = 0; i < 4; i++) activeMenu.add(slay, "slay" + (12 + i));
        
        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "cSlayPrincess":
                    if (!this.canTower) {
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
                    if (!this.canTower) {
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
     * @return the ending reached by the player
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
                    if (!manager.confirmContentWarnings(Chapter.NIGHTMARE)) break;
                    
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
        mainScript.runConditionalSection("nightmareStart", wounded);

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "threat", "\"Threaten me all you want! All it does is ease my guilty conscience.\""));
        activeMenu.add(new Option(this.manager, "notPrincess", "\"Whatever you are, you're not a Princess. Go ahead and waste your energy. I'll be waiting for you.\"", this.isHarsh));
        activeMenu.add(new Option(this.manager, "act", "\"So all of that was just an act, wasn't it? You're not really innocent or harmless. You're not even a princess. You're a *monster.*\"", !this.isHarsh));
        activeMenu.add(new Option(this.manager, "bleedOut", "\"Bang on the door all you want. It'll only make you bleed out faster.\"", wounded && !this.isHarsh));
        activeMenu.add(new Option(this.manager, "ignore", "[Ignore her and go to sleep.]"));

        mainScript.runConditionalSection(parser.promptOptionsMenu(activeMenu) + "Nightmare", lostArm);
    }
}
