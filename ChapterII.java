import java.util.ArrayList;
import java.util.HashMap;

public class ChapterII extends StandardCycle {
    
    private final ChapterEnding prevEnding;
    
    // Variables that are used in all chapters
    private final Voice ch2Voice;
    private final Condition cantUnique3 = new Condition(manager.demoMode());
    private final Condition cantJoint3 = new Condition(manager.demoMode());
    private String source = "";
    private boolean sharedLoop = false;
    private boolean sharedLoopInsist = false;
    private boolean threwBlade = false;
    private boolean forestSpecial = false;
    private boolean skipHillDialogue = false;

    // Variables that persist from Chapter 1
    private final boolean droppedBlade1; // Used in Adversary
    private final boolean whatWouldYouDo; // Used in Damsel
    private final boolean rescuePath; // Used in Witch

    // Variables that persist in Chapter 3
    private boolean abandoned2 = false;
    private boolean spectrePossessAsk = false;
    private boolean spectreCantWontAsk = false;
    private boolean spectreEndSlayAttempt = false;
    private boolean prisonerHeartStopped = false;

    // --- CONSTRUCTOR ---

    /**
     * Constructor
     * @param prevEnding the ending of the previous chapter
     * @param manager the GameManager to link this chapter to
     * @param parser the IOHandler to link this chapter to
     * @param voicesMet the Voices the player has encountered so far during this cycle
     * @param cantTryAbort whether the player has already tried (and failed) to abort this route
     */
    public ChapterII(ChapterEnding prevEnding, GameManager manager, IOHandler parser, ArrayList<Voice> voicesMet, Condition cantTryAbort, boolean isHarsh, boolean knowsDestiny, boolean droppedBlade1, boolean whatWouldYouDo, boolean rescuePath) {
        super(manager, parser, voicesMet, cantTryAbort, prevEnding);

        this.isHarsh = isHarsh;
        this.knowsDestiny = knowsDestiny;

        this.droppedBlade1 = droppedBlade1;
        this.whatWouldYouDo = whatWouldYouDo;
        this.rescuePath = rescuePath;

        this.prevEnding = prevEnding;
        this.activeChapter = prevEnding.getNextChapter();
        this.route.add(this.activeChapter);

        this.ch2Voice = this.prevEnding.getNewVoice();
        this.addVoice(this.ch2Voice);
    }

    // --- ACCESSORS ---

    /**
     * Accessor for ch2Voice
     * @return the Voice the player gained at the start of Chapter II
     */
    public Voice ch2Voice() {
        return this.ch2Voice;
    }

    /**
     * Accessor for source
     * @return the current "source" of the active chapter
     */
    public String getSource() {
        return this.source;
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
     * Accessor for threwBlade
     * @return whether or not the player threw the blade out the window
     */
    public boolean threwBlade() {
        return this.threwBlade;
    }

    /**
     * Accessor for droppedBlade1
     * @return whether or not the player dropped the blade in Chapter I
     */
    public boolean droppedBlade1() {
        return this.droppedBlade1;
    }

    /**
     * Accessor for whatWouldYouDo
     * @return whether or not the player asked the Princess what she would do if she left the cabin in Chapter I
     */
    public boolean whatWouldYouDo() {
        return this.whatWouldYouDo;
    }

    /**
     * Accessor for rescuePath
     * @return whether or not the player started to free the Princess in Chapter I
     */
    public boolean rescuePath() {
        return this.rescuePath;
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

    // --- CHAPTER MANAGEMENT ---

    /**
     * Initiates the active Chapter 2
     * @return the ending reached by the player
     */
    @Override
    public ChapterEnding runChapter() {
        this.unlockChapter();
        this.mainScript = new Script(this.manager, this.parser, activeChapter.getScriptFile());
        
        this.displayTitleCard();
        
        ChapterEnding ending;
        switch (this.activeChapter) {
            case ADVERSARY:
                ending = this.adversary();
                break;
            case TOWER:
                ending = this.tower();
                break;
            case SPECTRE:
                ending = this.spectre();
                break;
            case NIGHTMARE:
                ending = this.nightmare();
                break;
            case RAZOR:
                ending = this.razor();
                break;
            case BEAST:
                ending = this.beast();
                break;
            case WITCH:
                ending = this.witch();
                break;
            case STRANGER:
                ending = this.stranger();
                break;
            case PRISONER:
                ending = this.prisoner();
                break;
            case DAMSEL:
                ending = this.damsel();
                break;
            default: throw new RuntimeException("Cannot run an invalid chapter");
        }

        if (!ending.getAchievementID().isEmpty()) {
            manager.unlock(ending.getAchievementID());
        }

        if (!ending.isFinal()) {
            ChapterIII chapter3 = new ChapterIII(ending, manager, parser, voicesMet, route, cantTryAbort, source, sharedLoop, sharedLoopInsist, mirrorComment, touchedMirror, isHarsh, knowsDestiny, ch2Voice, abandoned2, spectrePossessAsk, spectreCantWontAsk, spectreEndSlayAttempt, prisonerHeartStopped);
            ending = chapter3.runChapter();
        }

        return ending;
    }

    // --- CHAPTERS & SCENES ---

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

        Condition shared = new Condition();
        InverseCondition noShare = shared.getInverse();
        Condition canAssume = new Condition(true);
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "dejaVu", "(Explore) I'm getting a terrible sense of deja vu.", noShare));
        activeMenu.add(new Option(this.manager, "dejaVu2", "(Explore) This is more than just deja vu, though. I'm pretty sure this whole thing really just happened.", activeMenu.get("dejaVu")));
        activeMenu.add(new Option(this.manager, "happened", "(Explore) Wait... hasn't this already happened?", noShare));
        activeMenu.add(new Option(this.manager, "no", "(Explore) Okay, no.", noShare));
        activeMenu.add(new Option(this.manager, "died", "(Explore) But I died! What am I doing here?", this.activeChapter != Chapter.SPECTRE, noShare));
        activeMenu.add(new Option(this.manager, "killedSelf", "(Explore) But I killed myself! What am I doing here?", this.activeChapter == Chapter.SPECTRE, noShare));
        activeMenu.add(new Option(this.manager, "alreadyKilled", "(Explore) But I already killed the Princess.", this.activeChapter == Chapter.SPECTRE, noShare));
        activeMenu.add(new Option(this.manager, "trapped", "(Explore) You trapped me here after I slew her last time. I'm not going to play along this time.", this.activeChapter == Chapter.SPECTRE, noShare));
        activeMenu.add(new Option(this.manager, "killMe", "(Explore) She's going to kill me again!", youDied, noShare));
        activeMenu.add(new Option(this.manager, "slewHer", "(Explore) But I already slew the Princess. Sure, she *also* killed me, but I definitely got her. Why am I here again?", youDied && princessDied, noShare));
        activeMenu.add(new Option(this.manager, "wise", "(Explore) Oh, you bastard! You're in for it now. I'm wise to your tricks!", liedTo, noShare));
        activeMenu.add(new Option(this.manager, "assume", "(Explore)  Let's assume I'm telling the truth, and all of this really did already happen. Why should I listen to you? Why should I bother doing *anything?*", shared, canAssume));
        activeMenu.add(new Option(this.manager, "defy", "(Explore) I'm with them. I'm going to find a way to save her from that cabin.", activeMenu.get("assume"), this.activeChapter == Chapter.DAMSEL));
        activeMenu.add(new Option(this.manager, "princess", "(Explore) Let's talk about this Princess...", activeMenu.get("assume")));
        activeMenu.add(new Option(this.manager, "proceed", "[Proceed to the cabin.]"));
        activeMenu.add(new Option(this.manager, "abort", this.cantTryAbort, "[Turn around and leave.]", 0));

        Condition pessimismComment = new Condition();
        boolean shareDied = false;

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "dejaVu":
                    this.sharedLoop = true;
                    shared.set();
                    canAssume.set(false);
                    if (this.ch2Voice == Voice.BROKEN) shareDied = true;
                    secondaryScript.runSection("dejaVu");
                    mainScript.runSection();
                    break;

                case "dejaVu2":
                    canAssume.set();
                    secondaryScript.runSection("dejaVu2");
                    break;

                case "happened":
                    this.sharedLoop = true;
                    shared.set();
                    if (this.ch2Voice == Voice.BROKEN) shareDied = true;
                    secondaryScript.runSection("happened");
                    mainScript.runSection();
                    break;

                case "no":
                    this.sharedLoop = true;
                    shared.set();
                    if (this.ch2Voice == Voice.BROKEN) shareDied = true;
                    secondaryScript.runSection("no");
                    mainScript.runSection();
                    break;

                case "died":
                    this.sharedLoop = true;
                    shared.set();
                    shareDied = true;
                    secondaryScript.runSection("died");
                    mainScript.runSection();
                    if (this.activeChapter == Chapter.PRISONER) mainScript.runSection();
                    break;

                case "killedSelf":
                    this.sharedLoop = true;
                    shared.set();
                    shareDied = true;
                    secondaryScript.runSection("killedSelf");
                    mainScript.runSection();
                    break;

                case "alreadyKilled":
                    this.sharedLoop = true;
                    shared.set();
                    secondaryScript.runSection("alreadyKilled");
                    mainScript.runSection();
                    break;

                case "trapped":
                    this.sharedLoop = true;
                    shared.set();
                    secondaryScript.runSection("trapped");
                    mainScript.runSection();
                    break;

                case "killMe":
                    this.sharedLoop = true;
                    shared.set();
                    shareDied = true;
                    secondaryScript.runSection("killMe");
                    mainScript.runSection();
                    break;

                case "slewHer":
                    this.sharedLoop = true;
                    shared.set();
                    secondaryScript.runSection("slewHer");
                    mainScript.runSection();
                    break;

                case "wise":
                    this.sharedLoop = true;
                    shared.set();
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
                    switch (this.ch2IntroAskPrincess(pessimismComment)) {
                        case 1:
                            this.repeatActiveMenu = false;
                            break;

                        case 2:
                            switch (this.ch2AttemptAbortVessel()) {
                                case 0: return false;

                                case 1:
                                    cantTryAbort.set();
                                    activeMenu.setGreyedOut("abort", true);
                                    break;

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
                    if (this.cantTryAbort.check()) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                case "abort":
                    switch (this.ch2AttemptAbortVessel()) {
                        case 0: return false;

                        case 1:
                            cantTryAbort.set();
                            activeMenu.setGreyedOut("abort", true);
                            break;

                        case 2:
                            this.repeatActiveMenu = false;
                            break;
                    }

                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }
        
        this.currentLocation = GameLocation.HILL;
        if (!this.skipHillDialogue) {
            secondaryScript.runSection("hillDialogue");
            mainScript.runConditionalSection("hillDialogue", pessimismComment);
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
                    if (this.cantTryAbort.check()) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }

                    switch (this.ch2AttemptAbortVessel()) {
                        case 0: return false;

                        case 1:
                            cantTryAbort.set();
                            break;

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
        secondaryScript.runConditionalSection("assume", shareDied);

        switch (this.activeChapter) {
            case WITCH:
                mainScript.runConditionalSection("assume", princessDied);
                secondaryScript.runSection("assumePointB");
                break;

            case DAMSEL:
            case NIGHTMARE:
            case PRISONER:
                mainScript.runSection("assume");
                secondaryScript.runSection("assumePointA");

            default:
                mainScript.runSection("assume");
                secondaryScript.runSection("assumePointB");
        }
        
        if (this.activeChapter == Chapter.NIGHTMARE) {
            secondaryScript.runSection("consequenceFreeNightmare");
        } else if (youDied && princessDied) {
            secondaryScript.runConditionalSection("consequenceFreeMutual", this.activeChapter == Chapter.RAZOR);
        } else if (youDied) {
            secondaryScript.runSection("consequenceFreeDied");
        } else if (princessDied) { // Only possible in Spectre
            secondaryScript.runSection("consequenceFreeSlain");
        } else { // Only possible in Witch if you were locked in the basement
            secondaryScript.runSection("consequenceFreeLocked");
        }

        mainScript.runSection("assume2");
    }

    /**
     * The player asks the Narrator questions about the Princess
     * @param pessimismComment whether, in Chapter II: The Tower, the Voice of the Hero has already commented on the Voice of the Broken being a pessimist
     * @return 0 if the player returns to the dialogue menu normally; 1 if the player proceeds to the cabin via a command; 2 if the player attempts to leave via a command
     */
    private int ch2IntroAskPrincess(Condition pessimismComment) {
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
                    } else if (this.activeChapter == Chapter.TOWER && !pessimismComment.check()) {
                        pessimismComment.set();
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
                    } else if (this.activeChapter == Chapter.TOWER && !pessimismComment.check()) {
                        pessimismComment.set();
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
                            mainScript.runConditionalSection("princessCagey", pleadLeave);
                            pleadLeave = true;
                            break;

                        case RAZOR:
                        case SPECTRE:
                            mainScript.runSection("princessCagey");
                            break;

                        case TOWER:
                            if (pessimismComment.check()) {
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
                    return 0;

                case "cGoCabin":
                    return 1;

                case "cGoLeave":
                    if (this.cantTryAbort.check()) {
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }
                    
                    return 2;

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
     * @return 0 if the player commits to aborting the vessel; 1 if the player cannot attempt to abort the vessel; 2 if the player returns to the cabin
     */
    private int ch2AttemptAbortVessel() {
        cantTryAbort.set();
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
                    switch (this.activeChapter) {
                        case DAMSEL:
                            mainScript.runSection("abortReturnC");
                            break;

                        case NIGHTMARE:
                            mainScript.runSection("abortReturnB");
                            break;

                        default: mainScript.runSection("abortReturn");
                    }

                    this.skipHillDialogue = true;
                    return 2;

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
     * @param canAskMirror whether the player can ask about the mirror (should be true)
     * @param canApproach whether the player can approach the mirror (should be true)
     * 
     */
    private void ch2AskMirror(Condition canAskMirror, Condition canApproach) {
        canAskMirror.set(false);
        this.mirrorComment = true;
        
        mainScript.runSection("askMirror");
        secondaryScript.runSection("askMirrorStart");

        boolean defaultCareLie = false;
        boolean defaultWhyLie = false;
        boolean defaultNoMatter = true;
        boolean defaultHandsomeCare = false;
        switch (this.activeChapter) {
            case ADVERSARY:
            case RAZOR:
            case SPECTRE:
                defaultCareLie = true;
                defaultNoMatter = false;
                defaultHandsomeCare = true;
                break;

            case BEAST:
            case DAMSEL:
            case PRISONER:
            case STRANGER:
                defaultWhyLie = true;
                break;

            case TOWER:
                defaultNoMatter = false;
                break;
        }
        
        mainScript.runSection("askMirrorStart");

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
                    canApproach.set(false);

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
                    this.ch2ApproachMirror(canAskMirror, canApproach);
                    break;

                default:
                    super.giveDefaultFailResponse();
            }
        }
    }

    /**
     * The player approaches the mirror in the cabin during Chapter II
     * @param canAskMirror whether the player can ask about the mirror
     * @param canApproach whether the player can approach the mirror (should be true)
     */
    private void ch2ApproachMirror(Condition canAskMirror, Condition canApproach) {
        this.touchedMirror = true;
        this.mirrorPresent = false;
        canAskMirror.set(false);
        canApproach.set(false);

        if (this.activeChapter == Chapter.NIGHTMARE) {
            mainScript.runSection("approachMirror");
        } else {
            secondaryScript.runSection("approachMirror");
        }

        secondaryScript.runSection("approachComment");

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
            case NIGHTMARE:
            case TOWER:
            case WITCH:
                mainScript.runSection("mirrorGone");
                break;

            default: secondaryScript.runSection();
        }
    }


    // - Chapter II: The Adversary -

    /**
     * Runs Chapter II: The Adversary
     * @return the ending reached by the player
     */
    private ChapterEnding adversary() {
        // You gain the Voice of the Stubborn

        if (!this.chapter2Intro(true, true, false)) {
            return ChapterEnding.ABORTED;   
        }

        mainScript.runSection("cabinIntro");

        Condition canAskMirror = new Condition(true);
        Condition canApproach = new Condition(true);
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "mirror", "(Explore) You didn't say anything about the mirror on the wall.", canAskMirror));
        activeMenu.add(new Option(this.manager, "different", "(Explore) This whole cabin is different than last time.", this.sharedLoopInsist));
        activeMenu.add(new Option(this.manager, "approach", "(Explore) [Approach the mirror.]", canApproach));
        activeMenu.add(new Option(this.manager, "take", "(Explore) [Take the blade.]"));
        activeMenu.add(new Option(this.manager, "enter", "[Enter the basement.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "mirror":
                    this.ch2AskMirror(canAskMirror, canApproach);
                    break;

                case "different":
                    secondaryScript.runSection("cabinDifferent");
                    break;

                case "cApproachMirror":
                case "approach":
                    this.ch2ApproachMirror(canAskMirror, canApproach);
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

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Enter the basement
        this.currentLocation = GameLocation.BASEMENT;
        this.withPrincess = true;
        this.withBlade = false;
        this.mirrorPresent = false;
        mainScript.runSection("stairsStart");

        if (manager.trueDemoMode()) return ChapterEnding.DEMOENDING;

        boolean differentComment = false;
        Condition talked = new Condition();
        InverseCondition noTalk = talked.getInverse();
        Condition narratorProof = new Condition();
        InverseCondition noProof = narratorProof.getInverse();
        Condition scaredComment = new Condition();
        Condition closerComment = new Condition();
        InverseCondition noCloserComment = closerComment.getInverse();
        Condition freeOffer = new Condition();
        InverseCondition noFreeOffer = freeOffer.getInverse();
        Condition adversaryFree = new Condition();
        Condition adversaryNotFree = new Condition();

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
        activeMenu.add(new Option(this.manager, "unarmedAttackA", this.cantJoint3, "\"Fine. Let's do this.\" [Attack her unarmed.]", 0, activeMenu.get("banter"), !this.hasBlade));
        activeMenu.add(new Option(this.manager, "unarmedAttackB", this.cantJoint3, "[Attack her unarmed.]", 0, !this.hasBlade));
        activeMenu.add(new Option(this.manager, "retrieve", "\"The blade's upstairs. I'll be right back.\" [Go upstairs and retrieve the blade.]", !this.hasBlade));
        activeMenu.add(new Option(this.manager, "closer", "[Step closer.]", closerComment));
        activeMenu.add(new Option(this.manager, "leaveYap", this.cantJoint3, "\"I don't know what happened to you since the last time we met, but I am *not* fighting a giant demon-lady. Bye!\" [Turn around and leave.]", 0));
        activeMenu.add(new Option(this.manager, "free", this.cantUnique3, "[Attempt to free the Princess.]", this.hasBlade, freeOffer));
        activeMenu.add(new Option(this.manager, "leaveSilent", this.cantJoint3, "[Turn around and leave without saying anything.]", 0));

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
                    narratorProof.set();
                    talked.set();
                    mainScript.runSection("proofDistantMenu");
                    break;
                    
                case "different":
                    differentComment = true;
                    talked.set();
                    mainScript.runBladeSection("differentDistantMenu");
                    this.adversaryNarratorProof(narratorProof);
                    break;
                    
                case "memory":
                    talked.set();
                    mainScript.runBladeSection("memoryDistantMenu");
                    if (!this.hasBlade && narratorProof.check()) mainScript.runSection();
                    this.adversaryNarratorProof(narratorProof);
                    break;
                    
                case "chat":
                    talked.set();
                    scaredComment.set();
                    mainScript.runConditionalSection("chatDistantMenu", this.droppedBlade1);
                    this.adversaryNarratorProof(narratorProof);
                    break;
                    
                case "scared":
                    closerComment.set();
                    mainScript.runSection("scaredDistantMenu");
                    break;
                    
                case "freeOffer":
                    talked.set();
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
                    talked.set();
                    mainScript.runSection("unpackDistantMenu");
                    this.adversaryNarratorProof(narratorProof);
                    break;
                    
                case "undecided":
                    talked.set();
                    closerComment.set();
                    mainScript.runConditionalSection("undecidedDistantMenu", this.droppedBlade1);
                    break;
                    
                case "banter":
                    talked.set();
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
                    if (this.cantJoint3.check()) {
                        parser.printDialogueLine(DEMOBLOCK);
                        break;
                    }
                case "unarmedAttackA":
                case "unarmedAttackB":
                    if (manager.hasVisited(Chapter.FURY)) {
                        this.cantJoint3.set();
                        parser.printDialogueLine(WORNPATH);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.FURY)) {
                        this.cantJoint3.set();
                        break;
                    }
                    
                    return this.adversaryFightUnarmed(narratorProof);
                    
                case "retrieve":
                    mainScript.runSection("retrieveDistantMenu");
                    return this.adversaryRetrieveBlade(false, adversaryFree, narratorProof, noFreeOffer);
                    
                case "free":
                    return this.adversaryFree(adversaryFree, narratorProof);
                    
                case "cGoStairs":
                    if (this.cantJoint3.check()) {
                        parser.printDialogueLine(DEMOBLOCK);
                        break;
                    }
                case "leaveYap":
                case "leaveSilent":
                    if (manager.hasVisited(Chapter.FURY)) {
                        this.cantJoint3.set();
                        parser.printDialogueLine(WORNPATH);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.FURY)) {
                        this.cantJoint3.set();
                        break;
                    }
                    
                    return this.adversaryFlee(false, adversaryFree, narratorProof, noFreeOffer);
            }
        }

        // Step closer
        mainScript.runBladeSection("closeStart");

        Condition noEndWorldAsk = new Condition(true);
        Condition noAskFree = new Condition(true);
        Condition purposeAsk = new Condition();
        InverseCondition noPurposeAsk = purposeAsk.getInverse();
        Condition whyOrPurpose = new Condition();
        InverseCondition noWhyOrPurpose = whyOrPurpose.getInverse();

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
        activeMenu.add(new Option(this.manager, "refuse", this.cantJoint3, "\"I'm not going to fight you.\""));
        activeMenu.add(new Option(this.manager, "free", this.cantUnique3, "[Attempt to free the Princess.]", this.hasBlade, adversaryNotFree, new OrCondition(freeOffer, whyOrPurpose)));
        activeMenu.add(new Option(this.manager, "leave", this.cantJoint3, "[Turn around and leave.]"));
        activeMenu.add(new Option(this.manager, "silent", this.cantJoint3, "[Remain silent.]"));

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
                    mainScript.runSection("noEndCloseMenu");
                    break;
                    
                case "freeAskA":
                case "freeAskB":
                    adversaryFree.set();
                    freeOffer.set();
                    noAskFree.set(false);
                    mainScript.runSection(activeOutcome + "CloseMenu");
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
                            case "you":
                                mainScript.runConditionalSection(activeOutcome + "AfterDied", differentComment);
                                break;
                            
                            case "return":
                                repeatSub = false;
                                this.adversaryNarratorProof(narratorProof);
                                break;
                        }
                    }

                    break;

                case "why":
                    whyOrPurpose.set();
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
                            purposeAsk.set();
                            mainScript.runSection(activeOutcome + "WhyHere");
                            break;
                    }

                    break;
                    
                case "reason":
                case "cared":
                    purposeAsk.set();
                    whyOrPurpose.set();
                    mainScript.runSection(activeOutcome + "CloseMenu");
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
                        this.cantJoint3.set();
                        parser.printDialogueLine(WORNPATH);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.FURY)) {
                        this.cantJoint3.set();
                        break;
                    }
                    
                    return this.adversaryPacifism(true, false, adversaryFree, narratorProof, noFreeOffer);
                    
                case "free":
                    return this.adversaryFree(adversaryFree, narratorProof);
                    
                case "cGoStairs":
                    if (this.cantJoint3.check()) {
                        parser.printDialogueLine(DEMOBLOCK);
                        break;
                    }
                case "leave":
                    if (manager.hasVisited(Chapter.FURY)) {
                        this.cantJoint3.set();
                        parser.printDialogueLine(WORNPATH);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.FURY)) {
                        this.cantJoint3.set();
                        break;
                    }

                    return this.adversaryFlee(false, adversaryFree, narratorProof, noFreeOffer);
                    
                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }
        
        throw new RuntimeException("No ending reached");
    }

    /**
     * The Narrator finally accepts that the player and the Adversary have met before (if He hasn't already)
     * @param narratorProof whether the Narrator has accepted that you've been here before
     */
    private void adversaryNarratorProof(Condition narratorProof) {
        if (narratorProof.check()) return;

        narratorProof.set();
        mainScript.runSection("narratorProof");

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
     * @return the ending reached by the player
     */
    private ChapterEnding adversaryFightDirect(boolean immediate, Condition adversaryFree, Condition narratorProof, AbstractCondition noFreeOffer) {
        mainScript.runSection("directStart");

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "push", "[Keep pushing.]"));
        activeMenu.add(new Option(this.manager, "unlodge", this.cantUnique3, "[Unlodge the blade and attack her from a different angle.]", 0));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "cSlayPrincess":
                case "push":
                    this.repeatActiveMenu = false;
                    break;

                case "cTakeHasBladeFail":
                    if (this.cantUnique3.check()) {
                        parser.printDialogueLine(DEMOBLOCK);
                        break;
                    }
                case "unlodge":
                    if (!manager.confirmContentWarnings(Chapter.NEEDLE)) {
                        this.cantUnique3.set();
                        break;
                    }

                    mainScript.runConditionalSection("unlodgeDirect", true);
                    return ChapterEnding.THREADINGTHROUGH;

                default: this.giveDefaultFailResponse();
            }
        }

        mainScript.runConditionalSection("pushDirect", immediate);

        InverseCondition noProof = narratorProof.getInverse();
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "agree", "\"I do.\""));
        activeMenu.add(new Option(this.manager, "lie", "(Lie) \"I do.\""));
        activeMenu.add(new Option(this.manager, "question", "\"What are you talking about?\""));
        activeMenu.add(new Option(this.manager, "silent", "[Silently continue pushing.]"));
        activeMenu.add(new Option(this.manager, "unlodge", this.cantUnique3, "[Unlodge the blade.]", 0));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "agree":
                    this.repeatActiveMenu = false;
                    mainScript.runConditionalSection("understandingKnowledge", this.knowsDestiny);
                    mainScript.runSection("understandingAgree");
                    break;

                case "question":
                    mainScript.runSection("understandingQuestion");
                case "lie":
                    this.repeatActiveMenu = false;
                    mainScript.runConditionalSection("understandingKnowledge", this.knowsDestiny);
                    mainScript.runSection("understandingOther");
                    break;

                case "cSlayPrincess":
                case "silent":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("understandingSilent");
                    break;

                case "cTakeHasBladeFail":
                    if (this.cantUnique3.check()) {
                        parser.printDialogueLine(DEMOBLOCK);
                        break;
                    }
                case "unlodge":
                    if (!manager.confirmContentWarnings(Chapter.NEEDLE)) {
                        this.cantUnique3.set();
                        break;
                    }

                    mainScript.runConditionalSection("unlodgeDirect", false);
                    return ChapterEnding.THREADINGTHROUGH;
            }
        }

        mainScript.runConditionalSection("pushCont", adversaryFree.check());

        Condition canUnderstanding = new Condition(true);
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "proof", "(Explore) I hope you heard all of that, Mr. Narrator. This is a lot different than last time, but last time definitely *happened.*", this.sharedLoop, noProof));
        activeMenu.add(new Option(this.manager, "jumpGun", "(Explore) \"Aren't you jumping the gun a little here? We each died *once.* That doesn't mean we're *immortal.*\""));
        activeMenu.add(new Option(this.manager, "metaphor", "(Explore) \"Were you being metaphorical when you said that nothing exists outside of us? There's more to the world than just this cabin. I saw trees and everything on my way here.\""));
        activeMenu.add(new Option(this.manager, "freeOffer", "(Explore) \"Don't you want to be free, though? Isn't there more to life than waking up chained in a basement and fighting to the death in an endless loop?\"", noFreeOffer));
        activeMenu.add(new Option(this.manager, "dontGet", "(Explore) \"Is that what you meant when you said I 'understood?' Because if that's the case then I absolutely don't get it. I'm not even sure there's an 'it' to get!\"", activeMenu.get("metaphor"), canUnderstanding));
        activeMenu.add(new Option(this.manager, "song", "(Explore) \"Yes! The two of us are a chorus of notes building on top of each other forever. The song we write in our blood will be the most beautiful music ever written!\"", activeMenu.get("metaphor"), canUnderstanding));
        activeMenu.add(new Option(this.manager, "refuse", this.cantJoint3, "\"Actually, I think I'm done fighting you. I don't think this is healthy for either of us.\""));
        activeMenu.add(new Option(this.manager, "attack", "[Pick up the blade and attack her again.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "proof":
                    narratorProof.set();
                    mainScript.runSection("proofDistantMenu");
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
                        this.cantJoint3.set();
                        parser.printDialogueLine(WORNPATH);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.FURY)) {
                        this.cantJoint3.set();
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
                    mainScript.runConditionalSection(activeOutcome + "Opening", activeOutcome);
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
            mainScript.runConditionalSection("directEndStartFromOpening", cantDieThought);

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

        if (this.isFirstVessel) {
            this.activeMenu = new OptionsMenu(true);
            activeMenu.add(new Option(this.manager, "chin", "\"Chin up! Isn't this what we wanted? Just you and me forever?\""));
            activeMenu.add(new Option(this.manager, "ok", "\"Are you okay?\""));
            activeMenu.add(new Option(this.manager, "end", "\"We ended the world, didn't we? Everything is gone.\""));
            activeMenu.add(new Option(this.manager, "rest", "\"We've been fighting for a long time. You should rest.\""));
            activeMenu.add(new Option(this.manager, "silent", "[Remain silent.]"));

            switch (parser.promptOptionsMenu(activeMenu)) {
                case "silent":
                    mainScript.claimFoldLine();
                    mainScript.runSection("directFinalEndSilent");
                    break;

                default:
                    mainScript.claimFoldLine();
                    mainScript.runSection("directFinalEndReply");
                    break;
            }
        } else {
            mainScript.claimFoldLine();
            mainScript.runSection("directFinalEndNotFirstVessel");
        }
    }

    /**
     * After not initially taking the blade, the player chooses to retrieve it from upstairs
     * @param wounded whether the player is already wounded
     * @param adversaryFree whether the Princess has already broken out of her chains
     * @param narratorProof whether the Narrator has accepted that you've been here before
     * @param noFreeOffer whether the player has already offered to free the Princess
     * @return the ending reached by the player
     */
    private ChapterEnding adversaryRetrieveBlade(boolean wounded, Condition adversaryFree, Condition narratorProof, AbstractCondition noFreeOffer) {
        this.currentLocation = GameLocation.CABIN;
        this.withPrincess = false;
        this.withBlade = true;

        mainScript.runSection("retrieveStart");

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "explore", "(Explore) You know, we could just stay here. Who says we have to fight to the death?"));
        activeMenu.add(new Option(this.manager, "take", "[Take the blade from the altar.]"));
        activeMenu.add(new Option(this.manager, "stay", this.cantJoint3, "We're doing it. We're staying up here.", 0, activeMenu.get("explore")));
        activeMenu.add(new Option(this.manager, "leave", this.cantJoint3, "We're leaving.", 0, activeMenu.get("explore")));
        activeMenu.add(new Option(this.manager, "return", "[Return to the basement.]", false));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "explore":
                    mainScript.runConditionalSection("exploreRetrieve", adversaryFree);
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
                        this.cantJoint3.set();
                        parser.printDialogueLine(WORNPATH);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.FURY)) {
                        this.cantJoint3.set();
                        break;
                    }
                    
                    if (this.hasBlade) mainScript.runSection("fleeRetrieveTookBlade");
                    mainScript.runConditionalSection("stayRetrieve", adversaryFree);
                    mainScript.runSection("stayRetrieveCont");
                    return this.adversaryFleeUpstairs(wounded, true);

                case "cGoHill":
                    if (this.cantJoint3.check() || !activeMenu.hasBeenPicked("explore")) {
                        parser.printDialogueLine(DEMOBLOCK);
                        break;
                    }
                case "leave":
                    if (manager.hasVisited(Chapter.FURY)) {
                        this.cantJoint3.set();
                        parser.printDialogueLine(WORNPATH);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.FURY)) {
                        this.cantJoint3.set();
                        break;
                    }
                    
                    if (this.hasBlade) mainScript.runSection("fleeRetrieveTookBlade");
                    mainScript.runConditionalSection("leaveRetrieve", adversaryFree);
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
     * @return the ending reached by the player
     */
    private ChapterEnding adversaryFight(boolean wounded, Condition adversaryFree, Condition narratorProof, AbstractCondition noFreeOffer) {
        if (wounded) {
            mainScript.runSection("fightWounded");
            return ChapterEnding.DEADISDEAD;
        }

        mainScript.runSection("fightStart");

        if (!adversaryFree.check()) {
            adversaryFree.set();
            mainScript.runSection("fightBreakChains");
        }

        mainScript.runConditionalSection("fightCont", narratorProof);

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "bait", manager.demoMode(), "[Bait an opening and outmaneuver her.]", 0));
        activeMenu.add(new Option(this.manager, "strike", "[Strike at her heart head-on.]"));
        activeMenu.add(new Option(this.manager, "run", this.cantJoint3, "[Run.]", 0));

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
                    if (this.cantJoint3.check()) {
                        parser.printDialogueLine(DEMOBLOCK);
                        break;
                    }
                case "run":
                    if (manager.hasVisited(Chapter.FURY)) {
                        this.cantJoint3.set();
                        parser.printDialogueLine(WORNPATH);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.FURY)) {
                        this.cantJoint3.set();
                        break;
                    }
                    
                    mainScript.runSection("fleeRunFail");
                    mainScript.runConditionalSection("fleeEnd", narratorProof);
                    return ChapterEnding.DEADISDEAD;
            }
        }

        throw new RuntimeException("No ending reached");
    }

    /**
     * The player attempts to fight the Adversary unarmed, leading to Chapter III: The Fury
     * @param narratorProof whether the Narrator has accepted that you've been here before
     * @return the ending reached by the player
     */
    private ChapterEnding adversaryFightUnarmed(Condition narratorProof) {
        mainScript.runSection("unarmedStart");

        OptionsMenu deathMenu = new OptionsMenu(true);
        deathMenu.add(new Option(this.manager, "getUp", "[Get up.]", 0));
        deathMenu.add(new Option(this.manager, "die", "[Die.]"));

        if (parser.promptOptionsMenu(deathMenu).equals("die")) {
            mainScript.runConditionalSection("unarmedDie", narratorProof);
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
                    mainScript.runConditionalSection(activeOutcome + "UnarmedPhase1", this.forestSpecial);
                    break;

                case "better":
                case "easy":
                    mainScript.runSection("easyUnarmedPhase1");
                case "attack":
                    this.repeatActiveMenu = false;
                    mainScript.runConditionalSection("attackUnarmedPhase1", this.forestSpecial);
                    break;
            }
        }

        if (parser.promptOptionsMenu(deathMenu).equals("die")) {
            mainScript.runConditionalSection("unarmedDiePhase1", narratorProof);
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
     * @return the ending reached by the player
     */
    private ChapterEnding adversaryPacifism(boolean tookBladeStart, boolean fromFight, Condition adversaryFree, Condition narratorProof, AbstractCondition noFreeOffer) {
        if (!adversaryFree.check()) {
            adversaryFree.set();
            mainScript.runSection("refuseBreakChains");

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

        mainScript.runConditionalSection("refuseCommit", fromFight);

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
        mainScript.runConditionalSection("refuseEndStart", isStanding);

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "noMatter", "\"Because death doesn't matter anymore, does it? Fighting, not fighting -- what does any of it matter if it all ends the same way?\""));
        activeMenu.add(new Option(this.manager, "more", "\"Because there's more to this than just fighting each other. If letting you kill me is how I can show you that, then it's worth it.\""));
        activeMenu.add(new Option(this.manager, "care", "\"I care about you, and I don't want to hurt you anymore.\""));
        activeMenu.add(new Option(this.manager, "funny", "\"I just think it's kind of funny...\""));
        activeMenu.add(new Option(this.manager, "silent", "[Remain silent.]"));

        mainScript.runConditionalSection(parser.promptOptionsMenu(activeMenu) + "RefuseEnd", narratorProof);
        return ChapterEnding.STRIKEMEDOWN;
    }

    /**
     * The player attempts to free the Adversary, leading to Chapter III: The Eye of the Needle
     * @param wounded whether the player is already wounded
     * @param adversaryFree whether the Princess has already broken out of her chains
     * @param narratorProof whether the Narrator has accepted that you've been here before
     * @return the ending reached by the player
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
                    return this.adversaryFight(false, adversaryFree, narratorProof, new Condition());

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
                    return this.adversaryFight(false, adversaryFree, narratorProof, new Condition());

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
     * @return the ending reached by the player
     */
    private ChapterEnding adversaryFlee(boolean wounded, Condition adversaryFree, Condition narratorProof, AbstractCondition noFreeOffer) {
        boolean brokeFree = !adversaryFree.check();
        adversaryFree.set();
        this.activeMenu = new OptionsMenu();

        if (brokeFree) {
            mainScript.runSection("fleeStartNotFree");
            
            activeMenu.add(new Option(this.manager, "explore", "(Explore) Okay, team. What are we thinking?"));
            activeMenu.add(new Option(this.manager, "turn", "[Turn and fight her head-on.]", this.hasBlade));
            activeMenu.add(new Option(this.manager, "dodge", this.cantUnique3, "[Dodge to the side and counter-attack.]", 0, this.hasBlade));
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
                            this.cantUnique3.set();
                            break;
                        }

                        mainScript.runSection("dodgeFirstAttack");
                        return ChapterEnding.THREADINGTHROUGH;

                    case "cGoStairs":
                    case "run":
                        mainScript.runSection("fleeRunSuccess");
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
            mainScript.runSection("fleeStartFree");
            
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
        mainScript.runConditionalSection("fleeEnd", narratorProof);
        return ChapterEnding.DEADISDEAD;
    }

    /**
     * The player manages to flee from the Adversary and make it upstairs
     * @param wounded whether the player is already wounded
     * @param retrieve whether the player originally went upstairs to retrieve the blade
     * @param lateJoin whether to skip the first portion of dialogue
     * @return the ending reached by the player
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
                            mainScript.runSection("upstairsLeaveBlade");
                            break;

                        default: this.giveDefaultFailResponse();
                    }
                }

                this.withBlade = false;
            }

            mainScript.runSection("upstairsFleeStart");
        }

        if (this.hasBlade) {
            if (tookBladeStart) {
                mainScript.runSection("upstairsCommentBlade");
            } else {
                mainScript.runSection("upstairsCommentTookBlade");
            }

            if (wounded) {
                mainScript.runSection("upstairsDieWounded");
            } else {
                mainScript.runSection("upstairsFightEnd");
                return ChapterEnding.THREADINGTHROUGH;
            }
        } else {
            mainScript.runSection("upstairsCommentNoBlade");
            mainScript.runSection("upstairsDieEnd");
        }

        mainScript.runSection("upstairsDieCont");
        return ChapterEnding.DEADISDEAD;
    }


    // - Chapter II: The Tower -

    /**
     * Runs Chapter II: The Tower
     * @return the ending reached by the player
     */
    private ChapterEnding tower() {
        // You gain the Voice of the Broken

        GlobalInt resistCount = new GlobalInt();
        GlobalInt submitCount = new GlobalInt();
        boolean tookBlade = false;

        switch (this.prevEnding) {
            case TOTOWERUNHARMED:
                this.source = "unharmed";
                break;
            
            case TOTOWERPATHETIC:
                this.source = "pathetic";
                break;

            default: this.source = "normal";
        }

        if (!this.chapter2Intro(true, false, false)) return ChapterEnding.ABORTED;

        mainScript.runSection("cabinIntro");

        Condition canAskMirror = new Condition(true);
        Condition canApproach = new Condition(true);
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "mirror", "(Explore) You didn't say anything about the mirror on the wall.", canAskMirror));
        activeMenu.add(new Option(this.manager, "different", "(Explore) This whole cabin is different than last time.", this.sharedLoopInsist));
        activeMenu.add(new Option(this.manager, "approach", "(Explore) [Approach the mirror.]", canApproach));
        activeMenu.add(new Option(this.manager, "take", "(Explore) [Take the blade.]"));
        activeMenu.add(new Option(this.manager, "enter", "[Enter the basement.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "mirror":
                    this.ch2AskMirror(canAskMirror, canApproach);
                    break;

                case "different":
                    mainScript.runSection("cabinDifferent");
                    break;

                case "cApproachMirror":
                case "approach":
                    this.ch2ApproachMirror(canAskMirror, canApproach);
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

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Enter the basement
        this.currentLocation = GameLocation.STAIRS;
        this.withBlade = false;
        this.mirrorPresent = false;
        if (!this.hasBlade) submitCount.increment();
        mainScript.runSection("stairsStart");

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
                        submitCount.increment();
                        mainScript.runSection("dropWilling");
                        break;

                    case "cSlayPrincessFail":
                    case "tighten":
                        this.repeatActiveMenu = false;
                        resistCount.increment();
                        mainScript.runSection("dropForced");
                        break;

                    default: this.giveDefaultFailResponse();
                }
            }

            this.hasBlade = false;
            this.canDropBlade = false;
            this.withBlade = true;
        }

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "no", "\"No.\""));
        activeMenu.add(new Option(this.manager, "kneel", "[Kneel.]"));

        switch (parser.promptOptionsMenu(activeMenu)) {
            case "no":
                if (resistCount.equals(2)) {
                    resistCount.increment();
                    mainScript.runSection("kneelResistAgain");
                } else {
                    mainScript.runSection("kneelResistFirst");
                }

                break;

            case "kneel":
                submitCount.increment();
                mainScript.runSection("kneelWilling");
                break;
        }

        if (manager.trueDemoMode()) return ChapterEnding.DEMOENDING;

        if (this.knowsDestiny) {
            mainScript.runSection("startKnowsDestiny");
        } else {
            mainScript.runBladeSection("start");

            this.activeMenu = new OptionsMenu(true);
            activeMenu.add(new Option(this.manager, "cantRefuse", "(Explore) I don't think I can refuse her. Sorry.", new NumCondition(resistCount, 0)));
            activeMenu.add(new Option(this.manager, "noStutter", "(Explore) \"N-no. I w-won't t-tell you.\"", new OrCondition(new NumCondition(resistCount, 1, 0), new NumCondition(submitCount, 1, 0))));
            activeMenu.add(new Option(this.manager, "shareMotive", "\"You're supposed to end the world.\""));
            activeMenu.add(new Option(this.manager, "noForce", this.cantUnique3, "\"I said NO!\"", 0, activeMenu.get("shareMotive"), tookBlade, new NumCondition(resistCount, 1, 2)));
            activeMenu.add(new Option(this.manager, "no", this.cantUnique3, "\"No.\"", 0, new NumCondition(submitCount, 0)));
            activeMenu.add(new Option(this.manager, "silent", new ConditionList(manager.demoMode(), new NumCondition(submitCount, 0)), "[Remain silent.]", 0));

            this.repeatActiveMenu = true;
            while (repeatActiveMenu) {
                switch (parser.promptOptionsMenu(activeMenu)) {
                    case "cantRefuse":
                        mainScript.runSection("motiveCantRefuse");
                        break;
                    
                    case "noStutter":
                        resistCount.increment();
                        mainScript.runSection("motiveNoStutter");
                        break;
                    
                    case "shareMotive":
                        this.repeatActiveMenu = false;
                        this.knowsDestiny = true;
                        mainScript.runSection("motiveShare");
                        break;
                    
                    case "noForce":
                        if (!manager.confirmContentWarnings("forced self-mutilation, forced suicide", true)) {
                            this.cantUnique3.set();
                            break;
                        }

                        this.repeatActiveMenu = false;
                        mainScript.runSection("motiveNoForce");
                        return this.towerResistBlade(resistCount, submitCount, false);
                    
                    case "no":
                        if (!manager.confirmContentWarnings("forced self-mutilation, forced suicide", true)) {
                            this.cantUnique3.set();
                            break;
                        }

                        this.repeatActiveMenu = false;
                        return this.towerResistBlade(resistCount, submitCount, false);
                    
                    case "silent":
                        if (submitCount.equals(0)) {
                            if (!manager.confirmContentWarnings("forced self-mutilation, forced suicide", true)) {
                                this.cantUnique3.set();
                                break;
                            }

                            mainScript.runSection("motiveSilentNoSubmit");
                            return this.towerResistBlade(resistCount, submitCount, false);
                        }

                        this.repeatActiveMenu = false;
                        this.knowsDestiny = true;
                        mainScript.runSection("motiveSilent");
                        break;
                }
            }
        }

        Condition noPriestOffer = new Condition(true);
        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "role", "(Explore) \"What would you have me do? What do you have planned?\""));
        activeMenu.add(new Option(this.manager, "powerful", "(Explore) \"If you're so powerful, can't you just break the chains yourself?\"", activeMenu.get("role")));
        activeMenu.add(new Option(this.manager, "selfDetermination", "(Explore) \"Just because you're supposed to end the world doesn't mean you actually have to do it. You can be whatever you want to be.\""));
        activeMenu.add(new Option(this.manager, "questions", "(Explore) \"I have questions for you before I decide to do anything.\"", noPriestOffer));
        activeMenu.add(new Option(this.manager, "happened", "(Explore) \"What happened to you after I died?\"", noPriestOffer));
        activeMenu.add(new Option(this.manager, "refuseNoBladeA", this.cantUnique3, "\"I'm not going to help you end the world. I don't care if something new comes after. I just can't let you do that.\"", 0, !tookBlade, new NumCondition(resistCount, 1, 1)));
        activeMenu.add(new Option(this.manager, "refuseNoBladeB", this.cantUnique3, "\"No. I won't take part in this.\" [Refuse her.]", 0, !tookBlade, new NumCondition(submitCount, 1, 1)));
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
                case "happened":
                    priestOffer = true;
                    noPriestOffer.set(false);
                    mainScript.runSection("questions");
                    break;

                case "refuseNoBladeA":
                case "refuseNoBladeB":
                    if (!manager.confirmContentWarnings(Chapter.APOTHEOSIS, "forced suicide")) {
                        this.cantUnique3.set();
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
                    this.towerPledge();
                    return ChapterEnding.OBEDIENTSERVANT;
            }
        }

        throw new RuntimeException("No ending reached");
    }

    /**
     * The player pledges themself to the Tower, leading to the "Your Obedient Servant" ending and claiming the Tower as a vessel
     */
    private void towerPledge() {
        mainScript.runSection("pledge");
        mainScript.runSection("pledgeCont");

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

        mainScript.runClaimSection("pledgeEnd", true);
    }

    /**
     * The player attempts to resist the Tower after bringing down the blade, leading to Chapter III: The Apotheosis / The Fury
     * @return the ending reached by the player
     */
    private ChapterEnding towerResistBlade(GlobalInt resistCount, GlobalInt submitCount, boolean priestOffer) {
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

        NumCondition resisted = new NumCondition(resistCount, 1, 2);
        NumCondition submitted = new NumCondition(submitCount, 1, 0);
        NumCondition noSubmit = new NumCondition(submitCount, 0);
        
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "self", "[Slay yourself.]", 0));
        activeMenu.add(new Option(this.manager, "resist", "[Resist.]", resisted, submitted));
        activeMenu.add(new Option(this.manager, "slay", "[Slay the Princess.]", noSubmit));

        this.canSlayPrincess = noSubmit.check();
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
        activeMenu.add(new Option(this.manager, "push", "[Push forward.]", resisted, submitted));
        activeMenu.add(new Option(this.manager, "slay", "[Slay the Princess.]", noSubmit));

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
        mainScript.runConditionalSection("advance2", priestOffer);
        
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "pledge", "\"I'm yours.\" [Pledge yourself to her.]"));
        activeMenu.add(new Option(this.manager, "resist", "\"I. Said. NO!\" [Resist.]", 0, submitted));
        activeMenu.add(new Option(this.manager, "slay", this.cantJoint3, "[Slay the Princess.]", noSubmit));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "pledge":
                    mainScript.runConditionalSection("advancePledge", this.knowsDestiny);
                    this.towerPledge();
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
                        this.cantJoint3.set();
                        activeMenu.setCondition("resist", true);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.FURY)) {
                        this.canSlayPrincess = false;
                        this.cantJoint3.set();
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
     * @return the ending reached by the player (indicating which Voice to gain in The Apotheosis)
     */
    private ChapterEnding towerSlaySelf(boolean tookBlade, boolean lateJoin) {

        if (!lateJoin) {
            mainScript.runSection("slaySelf");
            mainScript.runSection("slaySelfCont");
        }

        mainScript.runSection("slaySelfJoin");

        if (tookBlade || this.knowsDestiny) {
            return ChapterEnding.APOBLADE; // Voice of the Contrarian
        } else {
            return ChapterEnding.APOUNARMED; // Voice of the Paranoid
        }
    }


    // - Chapter II: The Spectre -

    /**
     * Runs Chapter II: The Spectre
     * @return the ending reached by the player
     */
    private ChapterEnding spectre() {
        // You gain the Voice of the Cold

        this.isHarsh = false;

        if (!this.chapter2Intro(false, true, true)) return ChapterEnding.ABORTED;

        mainScript.runSection("cabinIntro");

        Condition canAskMirror = new Condition(true);
        Condition canApproach = new Condition(true);
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "mirror", "(Explore) You didn't say anything about the mirror on the wall.", canAskMirror));
        activeMenu.add(new Option(this.manager, "different", "(Explore) This whole cabin is different than last time.", this.sharedLoopInsist));
        activeMenu.add(new Option(this.manager, "approach", "(Explore) [Approach the mirror.]", canApproach));
        activeMenu.add(new Option(this.manager, "take", "(Explore) [Take the blade.]"));
        activeMenu.add(new Option(this.manager, "enter", "[Enter the basement.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "mirror":
                    this.ch2AskMirror(canAskMirror, canApproach);
                    break;

                case "different":
                    mainScript.runSection("cabinDifferent");
                    break;

                case "cApproachMirror":
                case "approach":
                    this.ch2ApproachMirror(canAskMirror, canApproach);
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

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        this.currentLocation = GameLocation.BASEMENT;
        this.withPrincess = true;
        this.withBlade = false;
        this.mirrorPresent = false;
        mainScript.runSection("stairsStart");

        if (manager.trueDemoMode()) return ChapterEnding.DEMOENDING;

        mainScript.runSection();

        Condition isHostile = new Condition();
        InverseCondition isSoft = isHostile.getInverse();

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
                    isHostile.set();
                    mainScript.runSection("softSlay");
                    break;

                case "cSlayPrincessNoBladeFail":
                case "grab":
                    this.repeatActiveMenu = false;
                    this.isHarsh = true;
                    isHostile.set();
                    mainScript.runSection("softGrab");
                    break;

                case "wait":
                    this.repeatActiveMenu = false;
                    mainScript.runBladeSection("basementStartWait");
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
        Condition thoughtsHarsh = new Condition();
        Condition homeComment = new Condition();
        InverseCondition noHomeComment = homeComment.getInverse();
        Condition possessionAsk = new Condition();
        InverseCondition noPossessionAsk = possessionAsk.getInverse();
        OptionsMenu subMenu;

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
        activeMenu.add(new Option(this.manager, "refuse", this.cantJoint3, "\"Okay. I've given it enough thought. The answer is no. I can't let you out, and I won't let you possess me.\"", 0, possessionAsk));
        activeMenu.add(new Option(this.manager, "smashBones", this.cantJoint3, "\"[Smash her bones.]\"", 0, thoughtsHarsh));
        activeMenu.add(new Option(this.manager, "slayHarsh", this.cantJoint3, "\"[Slay the Princess, harder.]\"", 0, this.hasBlade, isHostile));
        activeMenu.add(new Option(this.manager, "grabHarsh", this.cantJoint3, "\"[Grab the Princess, but try harder.]\"", 0, !this.hasBlade, isHostile));
        activeMenu.add(new Option(this.manager, "leaveSoft", this.cantJoint3, "\"If you're dead, then there really isn't much for me to do, is there? I guess I'll get going.\" [Leave her in the basement.]", 0, isSoft));
        activeMenu.add(new Option(this.manager, "leaveHarsh", this.cantJoint3, "\"Fine. If I can't hurt you, then there really isn't anything for me to do here. I guess I'll get going.\" [Leave her in the basement.]", 0, isHostile));
        activeMenu.add(new Option(this.manager, "retrieve", this.cantJoint3, "\"Right. I don't think there's much more for us to talk about. I'm going to get my blade, and then the two of us can fight.\" [Retrieve the blade.]", 0, !this.hasBlade));
        activeMenu.add(new Option(this.manager, "slaySoft", "\"[Slay the Princess.]\"", this.hasBlade, isSoft));
        activeMenu.add(new Option(this.manager, "grabSoft", "\"[Grab the Princess.]\"", !this.hasBlade, isSoft));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);

            if (activeOutcome.equals("cSlayPrincess")) {
                if (!this.isHarsh) {
                    this.activeOutcome = "slaySoft";
                } else if (!this.cantJoint3.check()) {
                    this.activeOutcome = "slayHarsh";
                }
            } else if (activeOutcome.equals("cSlayPrincessNoBladeFail")) {
                if (!this.isHarsh) {
                    this.activeOutcome = "grabSoft";
                } else if (!this.cantJoint3.check()) {
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
                    narratorUnconfirmed.set(false);
                    mainScript.runSection("confirmLoopMenu");
                    break;

                case "notDead":
                    deathComment = true;
                    mainScript.runSection("notDeadMenu");
                    break;
                    
                case "sorryA":
                    noApology.set(false);
                case "body":
                case "whyBack":
                    mainScript.runSection(activeOutcome + "Menu");
                    break;
                    
                case "supposed":
                    homeComment.set();
                    mainScript.runSection("supposedMenu");
                    break;
                    
                case "help":
                    noBonesAsk.set(false);
                    possessionAsk.set();
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

                    if (parser.promptOptionsMenu(subMenu).equals("defend")) mainScript.runSection("victimMenuPush");
                    break;
                    
                case "grovel":
                    shareDied = true;
                    mainScript.runConditionalSection("grovelMenu", homeComment);
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
                    homeComment.set();
                    mainScript.runConditionalSection("trickMenu", narratorUnconfirmed);
                    narratorUnconfirmed.set(false);

                    switch (this.spectrePossessAsk(noWorldEndExplore, narratorUnconfirmed, shareDied, true)) {
                        case 1:
                            return this.spectrePossess();

                        case 2:
                            return this.spectreKill(false);
                    }

                    break;
                    
                case "alsoDead":
                    shareDied = true;
                    mainScript.runConditionalSection("alsoDeadMenu", possessionAsk);
                    if (!possessionAsk.check() && !this.isHarsh) homeComment.set();
                    break;

                case "bonesAsk":
                    noBonesAsk.set(false);
                    mainScript.runSection("bonesAskMenu");
                    break;
                    
                case "howHurt":
                    mainScript.runConditionalSection("howHurtMenu", deathComment);
                    break;
                    
                case "teleport":
                    shareDied = true;
                    mainScript.runSection("teleportMenu");
                    break;
                    
                case "walls":
                    mainScript.runSection("wallsMenu");

                    if (noPossessionAsk.check()) {
                        possessionAsk.set();

                        switch (this.spectrePossessAsk(noWorldEndExplore, narratorUnconfirmed, shareDied, true)) {
                            case 1:
                                return this.spectrePossess();

                            case 2:
                                return this.spectreKill(false);
                        }
                    }

                    break;
                    
                case "thoughts":
                    if (this.isHarsh) thoughtsHarsh.set();
                    mainScript.runConditionalSection("thoughtsMenu", possessionAsk);
                    break;
                    
                case "worldEndA":
                case "worldEndB":
                case "worldEndC":
                case "worldEndHarsh":
                    noWorldEndExplore.set(false);
                    this.spectreShareTask(narratorUnconfirmed, shareDied);
                    break;
                    
                case "wantA":
                case "wantB":
                    possessionAsk.set();
                    mainScript.runConditionalSection(activeOutcome + "Menu", homeComment);
                    homeComment.set();

                    switch (this.spectrePossessAsk(noWorldEndExplore, narratorUnconfirmed, shareDied, true)) {
                        case 1:
                            return this.spectrePossess();

                        case 2:
                            return this.spectreKill(false);
                    }

                    break;

                case "wantHarsh":
                    possessionAsk.set();
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
                        this.cantJoint3.set();
                        parser.printDialogueLine(WORNPATH);
                        parser.printDialogueLine(WORNPATHHERO);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.WRAITH)) {
                        this.cantJoint3.set();
                        break;
                    }

                    mainScript.runSection("refuseSwitch");
                    return this.spectreKill(false);

                case "smashBones":
                    if (manager.hasVisited(Chapter.WRAITH)) {
                        this.cantJoint3.set();
                        parser.printDialogueLine(WORNPATH);
                        parser.printDialogueLine(WORNPATHHERO);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.WRAITH)) {
                        this.cantJoint3.set();
                        break;
                    }

                    mainScript.runConditionalSection("smashBones", noBonesAsk);
                    return this.spectreKill(false);

                case "slayHarsh":
                    if (manager.hasVisited(Chapter.WRAITH)) {
                        this.cantJoint3.set();
                        parser.printDialogueLine(WORNPATH);
                        parser.printDialogueLine(WORNPATHHERO);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.WRAITH)) {
                        this.cantJoint3.set();
                        break;
                    }

                    mainScript.runSection("slayAgain");
                    return this.spectreKill(false);

                case "grabHarsh":
                    if (manager.hasVisited(Chapter.WRAITH)) {
                        this.cantJoint3.set();
                        parser.printDialogueLine(WORNPATH);
                        parser.printDialogueLine(WORNPATHHERO);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.WRAITH)) {
                        this.cantJoint3.set();
                        break;
                    }

                    mainScript.runSection("grabAgain");
                    return this.spectreKill(false);
                    
                case "cGoStairs":
                    if (this.cantJoint3.check()) {
                        parser.printDialogueLine(DEMOBLOCK);
                        break;
                    }
                case "leaveSoft":
                case "leaveHarsh":
                    if (manager.hasVisited(Chapter.WRAITH)) {
                        this.cantJoint3.set();
                        parser.printDialogueLine(WORNPATH);
                        parser.printDialogueLine(WORNPATHHERO);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.WRAITH)) {
                        this.cantJoint3.set();
                        break;
                    }

                    this.abandoned2 = true;
                    mainScript.runSection("leaveAttempt");
                    return this.spectreKill(true);
                    
                case "retrieve":
                    if (manager.hasVisited(Chapter.WRAITH)) {
                        this.cantJoint3.set();
                        parser.printDialogueLine(WORNPATH);
                        parser.printDialogueLine(WORNPATHHERO);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.WRAITH)) {
                        this.cantJoint3.set();
                        break;
                    }

                    this.abandoned2 = true;
                    mainScript.runSection("retrieveAttempt");
                    return this.spectreKill(true);

                case "slaySoft":
                    this.isHarsh = true;
                    isHostile.set();
                    mainScript.runSection("softSlay");
                    break;

                case "grabSoft":
                    this.isHarsh = true;
                    isHostile.set();
                    mainScript.runSection("softGrab");
                    break;

                case "cSlayPrincessFail":
                case "cSlayPrincessNoBladeFail":
                    // Only possible if the Princess is hostile and you can't go to Wraith
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
                    case "end2":
                    case "yesNo":
                        mainScript.runConditionalSection(activeOutcome + "EndWorldHarsh", shareDied);
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
                    case "grovel1":
                        mainScript.runSection("grovel1EndWorldSoft");
                        break;

                    case "wrong":
                        firstOption.set(false);
                        mainScript.runConditionalSection("wrongEndWorldSoft", narratorUnconfirmed);
                        narratorUnconfirmed.set(false);
                        break;

                    case "whatDo":
                    case "wereYou":
                    case "whatDo2":
                    case "grovel2":
                        firstOption.set(false);
                        mainScript.runSection(activeOutcome + "EndWorldSoft");
                        break;

                    case "whatDo3":
                        mainScript.runConditionalSection("whatDo3EndWorldSoft", shareDied);
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
        spectrePossessAsk = true;
        mainScript.runConditionalSection("possessAskSwitch", lateJoin);

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
        possessMenu.add(new Option(this.manager, "refuse", this.cantJoint3, "\"The answer's no.\""));
        possessMenu.add(new Option(this.manager, "return", "(Return) \"I need to think on this.\""));

        boolean repeatMenu = true;
        while (repeatMenu) {
            this.activeOutcome = parser.promptOptionsMenu(possessMenu);
            switch (activeOutcome) {
                case "no":
                    mainScript.runConditionalSection("noPossessAsk" + moodSuffix, narratorUnconfirmed);
                    if (this.isHarsh) narratorUnconfirmed.set(false);
                    break;

                case "wont":
                    this.spectreCantWontAsk = true;
                    mainScript.runConditionalSection("wontPossessAsk", trapSuggest);
                    trapSuggest = true;
                    break;

                case "temp":
                    mainScript.runConditionalSection("tempPossessAsk" + moodSuffix, trapSuggest);
                    if (!this.isHarsh) trapSuggest = true;
                    break;

                case "control":
                    mainScript.runConditionalSection("controlPossessAsk" + moodSuffix, trapSuggest);
                    if (!this.isHarsh) trapSuggest = true;
                    break;

                case "worldEnd":
                    noWorldEndExplore.set(false);
                    this.spectreShareTask(narratorUnconfirmed, shareDied);
                    return 0;

                case "agree":
                    return 2;

                case "refuse":
                    if (manager.hasVisited(Chapter.WRAITH)) {
                        this.cantJoint3.set();
                        parser.printDialogueLine(WORNPATH);
                        parser.printDialogueLine(WORNPATHHERO);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.WRAITH)) {
                        this.cantJoint3.set();
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
     * The player either offends the Spectre for a second time, refuses to let her possess them, or attempts to leave the basement; the Spectre kills them, leading to Chapter III: The Wraith
     * @return the ending reached by the player
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
                        this.spectreEndSlayAttempt = true;
                        mainScript.runSection("killBladeSlay");
                        break;

                    case "die":
                        mainScript.runSection("killBladeDie");
                        break;
                }
            }
        } else {
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
     * @return the ending reached by the player
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
        activeMenu.add(new Option(this.manager, "slay", this.cantUnique3, "[Slay the Princess.]", 0, this.hasBlade));
        activeMenu.add(new Option(this.manager, "leave", "[Leave the basement.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "explore":
                    mainScript.runSection("possessExplore" + moodSuffix);
                    break;

                case "cSlayPrincess":
                    if (this.cantUnique3.check()) {
                        parser.printDialogueLine(DEMOBLOCK);
                    }
                case "slay":
                    if (!manager.confirmContentWarnings(Chapter.DRAGON, "suicide")) {
                        this.cantUnique3.set();
                        break;
                    }

                    mainScript.runSection("exorcismStart");
                    return ChapterEnding.EXORCIST;

                case "cGoStairs":
                case "leave":
                    this.repeatActiveMenu = false;
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        this.currentLocation = GameLocation.CABIN;
        if (!this.hasBlade) this.withBlade = true;
        boolean tookBladeStart = this.hasBlade;
        mainScript.runSection("possessUpstairs");
        mainScript.runSection("possessUpstairs" + moodSuffix);

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "take", "(Explore) [Take the blade.]", !this.hasBlade));
        activeMenu.add(new Option(this.manager, "slay", this.cantUnique3, "[Slay the Princess.]", 0, this.hasBlade));
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
                    if (this.cantUnique3.check()) {
                        parser.printDialogueLine(DEMOBLOCK);
                    }
                case "slay":
                    if (!manager.confirmContentWarnings(Chapter.DRAGON, "suicide")) {
                        this.cantUnique3.set();
                        break;
                    }

                    mainScript.runSection("exorcismStart");
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
        return ChapterEnding.HITCHHIKER;
    }


    // - Chapter II: The Nightmare -

    /**
     * Runs Chapter II: The Nightmare
     * @return the ending reached by the player
     */
    private ChapterEnding nightmare() {
        // You gain the Voice of the Paranoid

        this.source = (this.prevEnding == ChapterEnding.TONIGHTMAREFLED) ? "fled" : "normal";
        if (!this.chapter2Intro(true, false, false)) return ChapterEnding.ABORTED;

        mainScript.runSection("cabinIntro");

        Condition canAskMirror = new Condition(true);
        Condition canApproach = new Condition(true);
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "mirror", "(Explore) You didn't say anything about the mirror on the wall.", canAskMirror));
        activeMenu.add(new Option(this.manager, "different", "(Explore) This whole cabin is different than last time.", this.sharedLoopInsist));
        activeMenu.add(new Option(this.manager, "approach", "(Explore) [Approach the mirror.]", canApproach));
        activeMenu.add(new Option(this.manager, "take", "(Explore) [Take the blade.]"));
        activeMenu.add(new Option(this.manager, "enter", "[Enter the basement.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "mirror":
                    this.ch2AskMirror(canAskMirror, canApproach);
                    break;

                case "different":
                    mainScript.runSection("cabinDifferent");
                    break;

                case "cApproachMirror":
                case "approach":
                    this.ch2ApproachMirror(canAskMirror, canApproach);
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

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }
        
        // Enter the basement
        this.currentLocation = GameLocation.STAIRS;
        this.withBlade = false;
        this.mirrorPresent = false;
        mainScript.runSection("stairsStart");

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
                    mainScript.runBladeSection("stairsThrow");

                    if (!this.hasBlade && !lookedBack) {
                        lookedBack = true;
                        mainScript.runSection("stairsLookBack");
                    }

                    break;

                case "talk":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("stairsTalk");
                    break;

                case "noPlan":
                    this.repeatActiveMenu = false;
                    voiceOfReasonComment = true;

                    mainScript.runBladeSection("stairsNoPlan");
                    
                    if (!this.hasBlade && !lookedBack) {
                        lookedBack = true;
                        mainScript.runSection("stairsLookBack");
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
        mainScript.runConditionalSection("basementStart", voiceOfReasonComment);

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

        if (manager.trueDemoMode()) return ChapterEnding.DEMOENDING;
        
        this.withPrincess = true;
        mainScript.runSection("encounterStart");

        boolean turnOffComment = false;
        Condition noWhyNoKill = new Condition(true);
        Condition whyNeed = new Condition();
        InverseCondition noWhyNeed = whyNeed.getInverse();
        Condition sharedTask = new Condition();
        OrCondition canSeparateWays = new OrCondition(whyNeed, sharedTask);
        Condition threatened = new Condition();

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "whyNoKill", "(Explore) \"Why won't you finish me off?\"", noWhyNoKill));
        activeMenu.add(new Option(this.manager, "want", "(Explore) \"What good am I to you alive? What do you want from me?\"", noWhyNoKill, noWhyNeed));
        activeMenu.add(new Option(this.manager, "afterDied", "(Explore) \"What happened after you killed me last time?\"", noWhyNeed));
        activeMenu.add(new Option(this.manager, "whyThreat", "(Explore) \"If you need me alive, then why did you threaten me on the stairs? Why didn't you try being nice to me?\"", whyNeed));
        activeMenu.add(new Option(this.manager, "shareTask", "(Explore) \"I was sent here to stop you from destroying the world. I can't just let you leave.\""));
        activeMenu.add(new Option(this.manager, "people", "(Explore) \"People will die if you do to them what you've done to me.\"", activeMenu.get("shareTask")));
        activeMenu.add(new Option(this.manager, "separate", "(Explore) \"And if I let you out? What then? Do we go our separate ways?\"", activeMenu.get("people"), canSeparateWays));
        activeMenu.add(new Option(this.manager, "agony", "(Explore) \"Being around you is agony. I'm not going to stick around.\"", activeMenu.get("separate")));
        activeMenu.add(new Option(this.manager, "dieThen", "(Explore) \"I'll just die then.\"", activeMenu.get("agony")));
        activeMenu.add(new Option(this.manager, "infinite", "(Explore) \"Are you sure about that? Give it enough tries and I'm bound to win eventually. And maybe you don't get to come back like I do.\"", activeMenu.get("dieThen")));
        activeMenu.add(new Option(this.manager, "lunatic", "(Explore) \"You're a lunatic. You know that, right?\""));
        activeMenu.add(new Option(this.manager, "turnOff", "(Explore) \"If you want to work together, can you at least turn off this whole organs-shutting-down situation?\""));
        activeMenu.add(new Option(this.manager, "threat", "(Explore) \"How about I just kill you instead?\""));
        activeMenu.add(new Option(this.manager, "canDie", "(Explore) \"Does that mean you can die?\"", !this.hasBlade, threatened));
        activeMenu.add(new Option(this.manager, "ifRefuse", "(Explore) \"And what if I refuse to let you out? What happens then?\"", canSeparateWays));
        activeMenu.add(new Option(this.manager, "friends", "(Explore) \"We don't have to be enemies. We can work together. We can be friends, even.\""));
        activeMenu.add(new Option(this.manager, "remainBlade", this.cantUnique3, "\"I'm not doing any of this. I'm not helping you leave, and I'm not going to try and kill you, so do your worst.\" [Toss the blade and remain with your Nightmare.]", 0, this.hasBlade));
        activeMenu.add(new Option(this.manager, "remainNoBlade", this.cantUnique3, "\"I'm not doing any of this. I'm not helping you leave, and I'm not going to try and kill you, so do your worst.\" [Remain with your Nightmare.]", 0, !this.hasBlade));
        activeMenu.add(new Option(this.manager, "leaveA", "\"Okay, let's get out of here.\" [Leave together.]"));
        activeMenu.add(new Option(this.manager, "leaveB", "\"Fine, you win. I'll let you leave.\" [Leave together.]"));
        activeMenu.add(new Option(this.manager, "run", this.cantUnique3, "[Run.]", 0));
        activeMenu.add(new Option(this.manager, "slay", this.cantJoint3, "[Slay the Princess.]", 0));

        this.canSlayPrincess = this.hasBlade;
        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "whyThreat":
                case "people":
                case "separate":
                case "agony":
                case "dieThen":
                case "infinite":
                case "canDie":
                case "ifRefuse":
                    mainScript.runSection(activeOutcome + "Menu");
                    break;

                case "whyNoKill":
                    noWhyNoKill.set(false);
                    mainScript.runSection("whyNoKillMenu");
                    break;

                case "want":
                case "afterDied":
                    whyNeed.set();
                    mainScript.runSection("whyNeedMenu");
                    break;

                case "shareTask":
                    sharedTask.set();
                    if (this.knowsDestiny) mainScript.runSection("shareTaskAlready");
                    mainScript.runSection("shareTaskMenu");
                    break;

                case "lunatic":
                case "turnOff":
                    mainScript.runConditionalSection(activeOutcome + "Menu", turnOffComment);
                    turnOffComment = true;
                    break;

                case "threat":
                    threatened.set();
                    mainScript.runBladeSection("threatMenu");
                    break;

                case "friends":
                    mainScript.runConditionalSection("friendsMenu", whyNeed);
                    whyNeed.set();
                    break;

                case "remainBlade":
                    if (!manager.confirmContentWarnings(Chapter.CLARITY)) {
                        this.cantUnique3.set(false);
                        break;
                    }

                    mainScript.runSection("remainBlade");
                    return ChapterEnding.MONOLITHOFFEAR;

                case "remainNoBlade":
                    if (!manager.confirmContentWarnings(Chapter.CLARITY)) {
                        this.cantUnique3.set(false);
                        break;
                    }

                    mainScript.runSection("remainJoin");
                    return ChapterEnding.MONOLITHOFFEAR;

                case "leaveA":
                case "leaveB":
                    return this.nightmareLeave();

                case "cGoStairs":
                    if (this.cantUnique3.check()) {
                        parser.printDialogueLine(DEMOBLOCK);
                        break;
                    }
                case "run":
                    if (!manager.confirmContentWarnings(Chapter.CLARITY)) {
                        this.cantUnique3.set(false);
                        break;
                    }

                    this.abandoned2 = true;
                    mainScript.runSection("runAttempt");
                    return ChapterEnding.MONOLITHOFFEAR;

                case "cSlayPrincess":
                    if (this.cantJoint3.check()) {
                        parser.printDialogueLine(DEMOBLOCK);
                        break;
                    }
                case "slay":
                    if (manager.hasVisited(Chapter.WRAITH)) {
                        this.cantJoint3.set();
                        parser.printDialogueLine(WORNPATH);
                        parser.printDialogueLine(WORNPATHHERO);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.WRAITH, "suicide; starvation", false)) {
                        this.cantJoint3.set();
                        break;
                    }

                    return this.nightmareSlay(false);

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }
        
        throw new RuntimeException("No ending reached");
    }

    /**
     * The player decides to leave the basement with the Nightmare
     * @return the ending reached by the player
     */
    private ChapterEnding nightmareLeave() {
        mainScript.runSection("leaveStart");

        if (this.hasBlade) {
            this.activeMenu = new OptionsMenu();
            activeMenu.add(new Option(this.manager, "suggest", "(Explore) \"How about you go first?\""));
            activeMenu.add(new Option(this.manager, "stairs", "[Step onto the stairs.]"));

            this.repeatActiveMenu = true;
            while (repeatActiveMenu) {
                switch (parser.promptOptionsMenu(activeMenu)) {
                    case "suggest":
                        activeMenu.setDisplay("stairs", "\"Okay. Fine.\" [Step onto the stairs.]");
                        mainScript.runSection("leaveStairsSuggest");

                    case "cGoStairs":
                    case "stairs":
                        this.repeatActiveMenu = false;
                        break;

                    default: this.giveDefaultFailResponse();
                }
            }

            this.currentLocation = GameLocation.STAIRS;
            mainScript.runSection("leaveBladeCont");

            boolean triedDoor = false;
            boolean canOpenDoor = false;
            Condition canTryDoor = new Condition();
            this.activeMenu = new OptionsMenu();
            activeMenu.add(new Option(this.manager, "locked", "(Explore) \"The door's locked.\""));
            activeMenu.add(new Option(this.manager, "try", "(Explore) [Try the door.]", 2, canTryDoor));
            activeMenu.add(new Option(this.manager, "slay", this.cantJoint3, "[Slay the Princess.]"));

            this.repeatActiveMenu = true;
            while (repeatActiveMenu) {
                this.activeOutcome = parser.promptOptionsMenu(activeMenu);
                switch (activeOutcome) {
                    case "locked":
                        canOpenDoor = true;
                        canTryDoor.set();
                        if (triedDoor) {
                            mainScript.runSection("lockedTried");
                        } else {
                            mainScript.runSection("lockedNotTried");
                        }

                        break;

                    case "try":
                        if (canOpenDoor) {
                            this.repeatActiveMenu = false;
                        } else {
                            triedDoor = true;
                            canTryDoor.set(false);
                            mainScript.runSection("tryDoorA");
                        }

                        break;

                    case "cSlayPrincess":
                        if (this.cantJoint3.check()) {
                            parser.printDialogueLine(DEMOBLOCK);
                            break;
                        }
                    case "slay":
                        if (manager.hasVisited(Chapter.WRAITH)) {
                            this.cantJoint3.set();
                            parser.printDialogueLine(WORNPATH);
                            break;
                        } else if (!manager.confirmContentWarnings(Chapter.WRAITH, "suicide; starvation", false)) {
                            this.cantJoint3.set();
                            break;
                        }

                        return this.nightmareSlay(true);

                    case "cGoCabin":
                        mainScript.runSection("stairsTryLeave");
                        break;

                    default: this.giveDefaultFailResponse();
                }
            }

            this.currentLocation = GameLocation.CABIN;
            mainScript.runSection("leaveBladeCabin");

            this.activeMenu = new OptionsMenu();
            activeMenu.add(new Option(this.manager, "slay", this.cantJoint3, "[Slay the Princess.]"));
            activeMenu.add(new Option(this.manager, "leave", "[Leave the Cabin.]"));

            this.repeatActiveMenu = true;
            while (repeatActiveMenu) {
                switch (activeOutcome) {
                    case "cSlayPrincess":
                        if (this.cantJoint3.check()) {
                            parser.printDialogueLine(DEMOBLOCK);
                            break;
                        }
                    case "slay":
                        if (manager.hasVisited(Chapter.WRAITH)) {
                            this.cantJoint3.set();
                            parser.printDialogueLine(WORNPATH);
                            break;
                        } else if (!manager.confirmContentWarnings(Chapter.WRAITH, "suicide; starvation", false)) {
                            this.cantJoint3.set();
                            break;
                        }

                        return this.nightmareSlay(true);

                    case "cGoHill":
                    case "leave":
                        this.repeatActiveMenu = false;
                        break;

                    case "cGoStairs":
                        mainScript.runSection("leaveCabinStairsAttempt");
                        break;

                    default: this.giveDefaultFailResponse();
                }
            }
        } else {
            this.activeMenu = new OptionsMenu();
            activeMenu.add(new Option(this.manager, "leave", "[Step onto the stairs and follow the Princess.]"));

            this.repeatActiveMenu = true;
            while (repeatActiveMenu) {
                switch (activeOutcome) {
                    case "cGoStairs":
                    case "leave":
                        this.repeatActiveMenu = false;
                        break;

                    default: this.giveDefaultFailResponse();
                }
            }

            this.currentLocation = GameLocation.CABIN;
            mainScript.runSection();

            this.activeMenu = new OptionsMenu();
            activeMenu.add(new Option(this.manager, "leave", "[Step into the world.]"));

            this.repeatActiveMenu = true;
            while (repeatActiveMenu) {
                switch (activeOutcome) {
                    case "cGoHill":
                    case "leave":
                        this.repeatActiveMenu = false;
                        break;

                    case "cGoStairs":
                        mainScript.runSection("leaveCabinStairsAttempt");
                        break;

                    default: this.giveDefaultFailResponse();
                }
            }
        }
        
        // Step outside
        mainScript.runSection("leaveCabinEnd");
        return ChapterEnding.WORLDOFTERROR;
    }

    /**
     * The player slays the Nightmare, leading to Chapter III: The Wraith
     * @param falling whether the player slew the Princess on the stairs and ended up falling forever
     * @return the ending reached by the player
     */
    private ChapterEnding nightmareSlay(boolean falling) {
        this.source = (falling) ? "stairs" : "basement";
        mainScript.runSection("slayStart");

        if (!falling) {
            this.hasBlade = false;
            this.withBlade = true;
        }
        this.withPrincess = false;
        this.canSlayPrincess = false;
        this.canSlaySelf = true;
        this.canDropBlade = true;
        this.canThrowBlade = true;
        boolean comeBackComment = false;
        boolean bladeGone = false;
        Condition biologyComment = new Condition();
        InverseCondition noBiologyComment = biologyComment.getInverse();

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "comeBack", "(Explore) It's not like it matters if I die. I'll just come back again."));
        activeMenu.add(new Option(this.manager, "secret1", "(Explore) Can you tell me your secrets now?", activeMenu.get("noMatter")));
        activeMenu.add(new Option(this.manager, "secret2", "(Explore) How about now? Is it secret time?", activeMenu.get("secret1")));
        activeMenu.add(new Option(this.manager, "secret3", "(Explore) I'm going to wear you down eventually. You might as well spill those sweet-sweet secrets now. Then we'll have something else to talk about!", activeMenu.get("secret2")));
        activeMenu.add(new Option(this.manager, "secret4", "(Explore) Secret?", activeMenu.get("secret3")));
        activeMenu.add(new Option(this.manager, "secret5", "(Explore) Secret?", 0, activeMenu.get("secret4")));
        activeMenu.add(new Option(this.manager, "company", "(Explore) But I've got the best company I could ask for! You guys! What more do I need?"));
        activeMenu.add(new Option(this.manager, "fine", "(Explore) I don't know... falling forever doesn't seem too bad to me.", falling, noBiologyComment));
        activeMenu.add(new Option(this.manager, "biology", "(Explore) Am I not a creature of biology? Won't I starve or die of dehydration before forever happens?"));
        activeMenu.add(new Option(this.manager, "stuck", "(Explore) Are you stuck here with us or are you capable of going... other places?"));
        activeMenu.add(new Option(this.manager, "lonely", "(Explore) I get it. You don't want us to die because you'd be lonely! How sweet.", activeMenu.get("stuck")));
        activeMenu.add(new Option(this.manager, "drop", "(Explore) [Drop the blade.]", falling));
        activeMenu.add(new Option(this.manager, "take", "(Explore) [Take the blade from her body.]", !falling));
        activeMenu.add(new Option(this.manager, "throw", "(Explore) [Throw the blade into the void.]", false));
        activeMenu.add(new Option(this.manager, "suicide", "[Slay yourself.]", falling));
        activeMenu.add(new Option(this.manager, "wait", "[Wait.]", 0));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);

            if (activeOutcome.equals("cDrop") || activeOutcome.equals("cThrow")) {
                if (falling) {
                    this.activeOutcome = "drop";
                } else {
                    this.activeOutcome = "throw";
                }
            }

            switch (activeOutcome) {
                case "comeBack":
                    comeBackComment = true;
                case "secret1":
                case "secret2":
                case "secret3":
                case "secret4":
                case "secret5":
                case "company":
                case "fine":
                case "stuck":
                case "lonely":
                    mainScript.runSection(activeOutcome + "Slain");
                    break;

                case "biology":
                    biologyComment.set();
                    mainScript.runConditionalSection("biologySlain", bladeGone);
                    break;

                case "cTake":
                case "take":
                    this.hasBlade = true;
                    this.withBlade = false;
                    mainScript.runSection("takeSlain");
                    break;

                case "drop":
                case "throw":
                    this.hasBlade = false;
                    bladeGone = true;
                    mainScript.runSection(activeOutcome + "Slain");
                    if (biologyComment.check()) mainScript.runSection("bladeGoneBiology");
                    break;

                case "cSlaySelf":
                case "suicide":
                    this.repeatActiveMenu = false;
                    mainScript.runConditionalSection("suicideEndSlain", comeBackComment);
                    break;

                case "wait":
                    if (biologyComment.check() && bladeGone) {
                        this.repeatActiveMenu = false;
                        mainScript.runConditionalSection("waitEndSlain", comeBackComment);
                    } else {
                        mainScript.runSourceSection("WaitNoBiology");
                    }

                    break;

                case "cGoHill":
                case "cGoCabin":
                case "cGoStairs":
                case "cGoBasement":
                case "cGoFail":
                    mainScript.runSection("nowhereToGo");
                    break;

                case "cSlayNoPrincessFail":
                    mainScript.runSection("alreadySlain");
                    break;

                case "cEnterFail":
                case "cLeaveFail":
                    mainScript.runSection("noLeave");
                    break;

                case "cSlaySelfNoBladeFail":
                    if (bladeGone) {
                        mainScript.runSection("cantSuicide");
                        break;
                    }
                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }
        
        if (falling) {
            return ChapterEnding.TERMINALVELOCITY;
        } else {
            return ChapterEnding.HOUSEOFNOLEAVE;
        }
    }


    // - Chapter II: The Razor -

    /**
     * Runs Chapter II: The Razor
     * @return the ending reached by the player
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

        Condition canAskMirror = new Condition(true);
        Condition canApproach = new Condition(true);
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "mirror", "(Explore) You didn't say anything about the mirror on the wall.", canAskMirror));
        activeMenu.add(new Option(this.manager, "different", "(Explore) This whole cabin is different than last time.", this.sharedLoopInsist));
        activeMenu.add(new Option(this.manager, "approach", "(Explore) [Approach the mirror.]", canApproach));
        activeMenu.add(new Option(this.manager, "take", "(Explore) [Take the blade.]"));
        activeMenu.add(new Option(this.manager, "enter", "[Enter the basement.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "mirror":
                    this.ch2AskMirror(canAskMirror, canApproach);
                    break;

                case "different":
                    secondaryScript.runSection("cabinDifferent");
                    break;

                case "cApproachMirror":
                case "approach":
                    this.ch2ApproachMirror(canAskMirror, canApproach);
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

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Enter the basement
        this.currentLocation = GameLocation.BASEMENT;
        this.withPrincess = true;
        this.withBlade = false;
        this.mirrorPresent = false;
        mainScript.runSection("stairsStart");

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
                case "suspicious":
                case "talk":
                case "key":
                case "mad":
                case "nobody":
                    followUpFlag = true;
                    mainScript.runSection(activeOutcome);
                    break;

                case "lastTime1":
                    lastTimeFlag = true;
                    mainScript.runSourceSection("LastTime1");
                    break;

                case "lastKill":
                    lastTimeFlag = true;
                case "lastTime2":
                    mainScript.runConditionalSection(activeOutcome, loopComment);
                    break;

                case "honest":
                    followUpFlag = true;
                case "happened":
                    lastTimeFlag = true;
                    mainScript.runSection(activeOutcome);
                    break;

                case "approach":
                    mainScript.runSection("approachEnd");

                    if (this.hasBlade) {
                        return ChapterEnding.TOARMSRACEBORED;
                    } else {
                        return ChapterEnding.TONOWAYOUTBORED;
                    }

                case "cGoStairs":
                    mainScript.runSection("leaveAttemptSilent");
                case "bye":
                case "rightBack":
                    mainScript.runConditionalSection("leaveAttempt", source.equals("revival"));

                    if (this.hasBlade) {
                        return ChapterEnding.TOARMSRACELEFT;
                    } else {
                        return ChapterEnding.TONOWAYOUTLEFT;
                    }

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
                followUpFlag = false;
                mainScript.runSection("loopComment");
            } else if (followUpFlag) {
                mainScript.runConditionalSection("followUp" + followUpCount, source.equals("revival"));

                if (followUpCount == 4) {
                    if (this.hasBlade) {
                        return ChapterEnding.TOARMSRACEBORED;
                    } else {
                        return ChapterEnding.TONOWAYOUTBORED;
                    }
                }
                
                followUpFlag = false;
                followUpCount += 1;
            }
        }

        throw new RuntimeException("No ending found");
    }


    // - Chapter II: The Beast -

    /**
     * Runs Chapter II: The Beast
     * @return the ending reached by the player
     */
    private ChapterEnding beast() {
        // You gain the Voice of the Hunted

        if (!this.chapter2Intro(true, false, false)) {
            return ChapterEnding.ABORTED;
        }

        mainScript.runSection("cabinIntro");

        Condition canAskMirror = new Condition(true);
        Condition canApproach = new Condition(true);
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "mirror", "(Explore) You didn't say anything about the mirror on the wall.", canAskMirror));
        activeMenu.add(new Option(this.manager, "different", "(Explore) This whole cabin is different than last time.", this.sharedLoopInsist));
        activeMenu.add(new Option(this.manager, "approach", "(Explore) [Approach the mirror.]", canApproach));
        activeMenu.add(new Option(this.manager, "take", "(Explore) [Take the blade.]"));
        activeMenu.add(new Option(this.manager, "enter", "[Enter the basement.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "mirror":
                    this.ch2AskMirror(canAskMirror, canApproach);
                    break;

                case "different":
                    mainScript.runSection("cabinDifferent");
                    break;

                case "cApproachMirror":
                case "approach":
                    this.ch2ApproachMirror(canAskMirror, canApproach);
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

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Enter the basement
        this.currentLocation = GameLocation.BASEMENT;
        this.withBlade = false;
        this.mirrorPresent = false;
        mainScript.runSection("stairsStart");

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

                    manager.unlock("beastFreeze");
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
        Condition canTryFlee = new Condition(false);
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "explore", "(Explore) How exactly are we supposed to take back the momentum here?"));
        activeMenu.add(new Option(this.manager, "dodge", "[Survive.]", 0));
        activeMenu.add(new Option(this.manager, "flee", "[Run for the stairs.]", canTryFlee));
        activeMenu.add(new Option(this.manager, "attack", manager.demoMode(), "[Wait for her to strike, and hit her back.]"));
        activeMenu.add(new Option(this.manager, "playDead", this.cantJoint3, "[Play dead.]"));
        activeMenu.add(new Option(this.manager, "freeze", this.cantJoint3, "[Stand still.]"));
        
        int stallLimit = 2;
        Condition stairsGuarded = new Condition(true);
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
        parentMenu.add(new Option(this.manager, "flee", "[Run for the stairs.]", 0, canTryFlee));

        // Figure out how to put warnings for Den in here, considering the game forces you into it in several instances if you've already been to Wild...

        for (int phase = 2; phase < 5; phase++) {
            this.canSlayPrincess = false;
            canTryFlee.set();

            switch (phase) {
                case 2:
                    stairsGuarded.set();
                    activeMenu.setDisplay("dodge", "[Stay. Alive.]");
                    activeMenu.setCondition("explore", true);
                    parentMenu.setCondition("tired", true);
                    break;

                case 3:
                    canTryFlee.set();
                    stairsGuarded.set();
                    activeMenu.setDisplay("dodge", "[Again...]");
                    activeMenu.setCondition("flee", false);
                    stallLimit = 1;

                    if (manager.demoMode()) activeMenu.setGreyedOut("dodge", true);
                    break;
            }

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
                            if (stairsGuarded.check()) {
                                mainScript.runSection("whatGuarded");
                            } else {
                                mainScript.runSection("whatNoBlade");
                            }
                        }

                        break;

                    case "cGoStairs":
                        if (!canTryFlee.check()) {
                            stallCount -= 1;
                            mainScript.runSection("cantFlee");
                            break;
                        }
                    case "flee":
                        if (stairsGuarded.check()) {
                            stallCount -= 1;
                            canTryFlee.set(false);
                            parentMenu.setCondition("flee", false);
                            mainScript.runSection("tryFleeGuarded");
                        } else {
                            mainScript.runSection("tryFlee");
                            
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
        }

        return this.beastToDen(true, true);
    }

    /**
     * Runs one phase of the Beast's attack, allowing the player to choose a response
     * @param phase the current phase of the fight
     * @return the ending reached by the player if they trigger one, or null if the fight continues
     */
    private ChapterEnding beastAttack(int phase) {
        mainScript.runSection("attack" + phase);

        if (!manager.demoMode()) this.canSlayPrincess = true;
        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "explore":
                    mainScript.runBladeSection("attackExplore");
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
                    mainScript.runSection("tryFlee");

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
                        this.cantJoint3.set();
                        parser.printDialogueLine(WORNPATH);
                        parser.printDialogueLine(WORNPATHHERO);
                        break;
                    } else {
                        mainScript.runSection("eatenStartPlayDead");
                        return this.beastEaten(false, true);
                    }

                case "freeze":
                    if (manager.hasVisited(Chapter.WILD)) {
                        this.cantJoint3.set();
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
        this.cantJoint3.set();
        parser.printDialogueLine(WORNPATH);
        parser.promptOptionsMenu(cantWildMenu, new DialogueLine("[You have no other option.]"));
        parser.printDialogueLine(WORNPATHHERO);
    }

    /**
     * The player manages to avoid being eaten by the Beast, leading to Chapter III: The Den
     * @param skepticPath whether the player continued dodging the Beast's attacks (granting them the Voice of the Skeptic) or fought back (granting them the Voice of the Stubborn)
     * @param wounded whether the player has already been wounded by the Beast
     * @return the ending reached by the player
     */
    private ChapterEnding beastToDen(boolean skepticPath, boolean wounded) {
        if (skepticPath) {
            mainScript.runConditionalSection("denSkeptic", wounded);
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
     * @return the ending reached by the player
     */
    private ChapterEnding beastEaten(boolean talked, boolean playedDead) {
        mainScript.runConditionalSection("eatenStartCont", talked);

        boolean incrementFlag;
        GlobalInt beastHP = new GlobalInt(4);
        NumCondition maxHP = new NumCondition(beastHP, 4);
        NumCondition canSlay = new NumCondition(beastHP, 0);
        InverseCondition cantSlay = canSlay.getInverse();
        Condition noThreat = new Condition(true);
        Condition notFirstTurn = new Condition();
        Condition forceDissolved = new Condition();
        
        this.canSlayPrincess = true;
        this.canSlaySelf = this.hasBlade && !manager.demoMode();
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "cabin", forceDissolved, "(Explore) \"Can you talk to the cabin?\"", 0));
        activeMenu.add(new Option(this.manager, "asked", forceDissolved, "(Explore) \"You could have asked me before swallowing me alive.\"", 0));
        activeMenu.add(new Option(this.manager, "threatExplore", forceDissolved, "(Explore) \"Spit me out or I'll kill myself and nobody gets to leave.\"", 0, this.hasBlade));
        activeMenu.add(new Option(this.manager, "need", forceDissolved, "(Explore) \"You need me to want to free you, don't you? You can't force me to let you out of here.\"", 0, notFirstTurn));
        activeMenu.add(new Option(this.manager, "freedom", forceDissolved, "(Explore) \"Just because I want freedom, doesn't mean I want to give you yours.\"", notFirstTurn));
        activeMenu.add(new Option(this.manager, "threatA", forceDissolved, "(Explore) \"I have my steel claw. I could use it to hurt you.\"", 0, this.hasBlade, maxHP, noThreat));
        activeMenu.add(new Option(this.manager, "threatB", forceDissolved, "(Explore) \"I've found your heart. Spit me out, or I will end you.\"", 0, canSlay, noThreat));
        activeMenu.add(new Option(this.manager, "claw", forceDissolved, "(Explore) [Claw and bite.]", 0, !this.hasBlade));
        activeMenu.add(new Option(this.manager, "dig", forceDissolved, "(Explore) [Dig with the steel claw.]", 0, this.hasBlade, cantSlay));
        activeMenu.add(new Option(this.manager, "slay", this.cantJoint3, "[Slay the Princess.]", 0, canSlay));
        activeMenu.add(new Option(this.manager, "surrenderA", "\"Fine! You can leave.\""));
        activeMenu.add(new Option(this.manager, "surrenderB", "\"You can leave! Just let me go.\""));
        activeMenu.add(new Option(this.manager, "suicide", this.cantJoint3, "\"Screw you!\" [Slay yourself.]", 0, this.hasBlade));
        activeMenu.add(new Option(this.manager, "wait", this.cantJoint3, "[Wait for death.]", 0));

        for (int turnCount = 0; turnCount < 3; turnCount++) {
            incrementFlag = true;

            this.activeOutcome = parser.promptOptionsMenu(activeMenu);

            if (activeOutcome.equals("cSlayPrincess")) {
                if (canSlay.check()) {
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
                            this.cantJoint3.set();
                            forceDissolved.set();
                            this.canSlayPrincess = false;
                            this.canSlaySelf = false;
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
                            this.cantJoint3.set();
                            forceDissolved.set();
                            this.canSlayPrincess = false;
                            this.canSlaySelf = false;
                            break;
                        }
                    }

                    noThreat.set(false);
                    mainScript.runSection("threatEaten");
                    break;
                    
                case "cSlayPrincessNoBladeFail":
                case "claw":
                    beastHP.decrement();;
                    mainScript.runSection("eatenClaw" + (4 - beastHP.check()));
                    break;
                    
                case "dig":
                    beastHP.subtract(2);

                    if (beastHP.equals(2)) {
                        mainScript.runSection("eatenClaw1");
                    } else {
                        mainScript.runSection("eatenClaw2");
                    }
                    
                    break;
                    
                case "freedom":
                    mainScript.runSection("freedomEaten");
                    return ChapterEnding.DISSOLVINGWILLACCIDENT;
                    
                case "surrenderA":
                case "surrenderB":
                    mainScript.runSection("eatenSurrender");
                    return ChapterEnding.DISSOLVINGWILL;
                    
                case "slay":
                    if (!manager.confirmContentWarnings(Chapter.WILD)) {
                        incrementFlag = false;
                        this.cantJoint3.set();
                        forceDissolved.set();
                        this.canSlayPrincess = false;
                        this.canSlaySelf = false;
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
                        this.cantJoint3.set();
                        forceDissolved.set();
                        this.canSlayPrincess = false;
                        this.canSlaySelf = false;
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
                        this.cantJoint3.set();
                        forceDissolved.set();
                        this.canSlayPrincess = false;
                        this.canSlaySelf = false;
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
                        notFirstTurn.set();
                        mainScript.runSection("eatenTurn0");
                        if (maxHP.check()) mainScript.runSection("eatenTurn0NoAttack");
                        break;

                    case 1:
                        if (manager.demoMode()) forceDissolved.set();
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


    // - Chapter II: The Witch -

    /**
     * Runs Chapter II: The Witch
     * @return the ending reached by the player
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

        Condition canAskMirror = new Condition(true);
        Condition canApproach = new Condition(true);
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "mirror", "(Explore) You didn't say anything about the mirror on the wall.", canAskMirror));
        activeMenu.add(new Option(this.manager, "different", "(Explore) This whole cabin is different than last time.", this.sharedLoopInsist));
        activeMenu.add(new Option(this.manager, "approach", "(Explore) [Approach the mirror.]", canApproach));
        activeMenu.add(new Option(this.manager, "take", "(Explore) [Take the blade.]"));
        activeMenu.add(new Option(this.manager, "enter", "[Enter the basement.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "mirror":
                    this.ch2AskMirror(canAskMirror, canApproach);
                    break;

                case "different":
                    secondaryScript.runSection("cabinDifferent");
                    break;

                case "cApproachMirror":
                case "approach":
                    this.ch2ApproachMirror(canAskMirror, canApproach);
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

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Enter the basement
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
        mainScript.runSection("stairsEnd");

        if (manager.trueDemoMode()) return ChapterEnding.DEMOENDING;
        
        mainScript.runSection("basementStartShareSwitch");

        boolean heartComment = false;
        Condition witchFree = new Condition();
        InverseCondition witchChained = witchFree.getInverse();
        Condition apologized = new Condition();
        InverseCondition noApology = apologized.getInverse();
        Condition leaveMentioned = new Condition();
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
        activeMenu.add(new Option(this.manager, "offer", this.cantUnique3, "\"I don't want to hurt you, but clearly there's some broken trust. Take this as a gesture of my good will.\" [Give her the blade.]", this.hasBlade));
        activeMenu.add(new Option(this.manager, "leave", this.cantJoint3, "\"I don't trust you. Not enough to free you, and definitely not enough to get close to you. I'm leaving. Bye.\" [Leave her in the basement.]", 0));
        activeMenu.add(new Option(this.manager, "slayYap", this.cantJoint3, "\"I'd like to be straightforward with my intentions. I didn't care for how you treated me last time, and I think you might be a danger to the world. I'm going to attack you now.\" [Slay the Princess.]", 0, this.hasBlade));
        activeMenu.add(new Option(this.manager, "slaySilent", this.cantJoint3, "[Slay the Princess.]", 0, this.hasBlade));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "mistake":
                    leaveMentioned.set();
                case "sorry":
                    apologized.set();
                case "notHappy":
                case "betray":
                    mainScript.runSection(activeOutcome + "Menu");
                    break;
                    
                case "locked":
                    leaveMentioned.set();
                    activeMenu.setCondition("sorry", false);
                case "scared":
                case "goodWill":
                case "bygones":
                case "trusted":
                    noStall.set(false);
                    mainScript.runConditionalSection(activeOutcome + "Menu", this.rescuePath);
                    break;
                    
                case "messy":
                case "impasse":
                case "free":
                    leaveMentioned.set();
                    mainScript.runSection(activeOutcome + "Menu");
                    break;
                    
                case "getOutB":
                    witchFree.set();
                    mainScript.runConditionalSection("getOutBMenu", heartComment);
                    break;

                case "getOutA":
                case "cutA":
                case "cutB":
                    witchFree.set();
                    mainScript.runConditionalSection("cutMenu", heartComment);
                    break;
                    
                case "stairs":
                    return this.witchStairs();
                    
                case "offer":
                    if (this.witchGiveBladeStart()) {
                        return this.witchGiveBlade(false, witchFree.check(), heartComment);
                    }

                    break;
                    
                case "cGoStairs":
                    if (this.cantJoint3.check()) {
                        parser.printDialogueLine(DEMOBLOCK);
                        break;
                    }
                case "leave":
                    if (manager.hasVisited(Chapter.WILD)) {
                        this.cantJoint3.set();
                        parser.printDialogueLine(WORNPATH);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.WILD)) {
                        this.cantJoint3.set();
                        parser.printDialogueLine(WORNPATH);
                        break;
                    }

                    return this.witchToWild(false, witchFree.check(), heartComment);
                    
                case "slayYap":
                    if (manager.hasVisited(Chapter.WILD)) {
                        this.cantJoint3.set();
                        parser.printDialogueLine(WORNPATH);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.WILD)) {
                        this.cantJoint3.set();
                        parser.printDialogueLine(WORNPATH);
                        break;
                    }

                    return this.witchToWild(true, witchFree.check(), heartComment);
                    
                case "cSlayPrincess":
                    if (this.cantJoint3.check()) {
                        parser.printDialogueLine(DEMOBLOCK);
                        break;
                    }
                case "slaySilent":
                    if (manager.hasVisited(Chapter.WILD)) {
                        this.cantJoint3.set();
                        parser.printDialogueLine(WORNPATH);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.WILD)) {
                        this.cantJoint3.set();
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
     * @return the ending reached by the player
     */
    private ChapterEnding witchStairs() {
        mainScript.runSection("leaveStart");

        Condition witchNotFirst = new Condition(true);
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "explore", "(Explore) \"You first.\"", witchNotFirst));
        activeMenu.add(new Option(this.manager, "offer", manager.demoMode(), "(Explore) \"Clearly, there's some broken trust here. What if I gave you this?\" [Give her the blade.]", this.hasBlade && !this.cantUnique3.check(), witchNotFirst));
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
            mainScript.runSection("followStart");

            if (this.hasBlade) {
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
                this.witchLeaveBasement();
                return ChapterEnding.FROGLOCKED;
            }
        } else {
            mainScript.runSection("leadStart");
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
                    mainScript.runSection("neverBetrayal");
                } else {
                    mainScript.runSection("betrayalGenericLead");
                }
                
                break;

            case "trust":
                mainScript.runSection("trustBetrayal");

                if (wentFirst) {
                    mainScript.runSection("betrayalJoke");
                } else {
                    mainScript.runSection("betrayalGenericLead");
                }

                break;

            case "silent":
                if (wentFirst) {
                    mainScript.runSection("silentBetrayalFollow");
                } else {
                    mainScript.runSection("betrayalGenericLead");
                }

                break;
        }        

        mainScript.runConditionalSection("betrayalCont", wentFirst);

        if (brokenShare) {
            mainScript.runSection("betrayalEndBrokenShare");
        } else {
            mainScript.runSection("betrayalEndNoShare");
        }
    }

    /**
     * The player chooses to peacefully follow the Witch out of the basement, leading to "The Frog" ending and claiming the Witch
     */
    private void witchLeaveBasement() {
        mainScript.runSection("lockedStart");

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

        mainScript.runSection("lockedEnd");
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
                this.cantUnique3.set();
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
     * @return the ending reached by the player
     */
    private ChapterEnding witchGiveBlade(boolean fromStairs, boolean witchFree, boolean heartComment) {
        if (fromStairs) {
            mainScript.runSection("offerCommitStairs");
        } else {
            mainScript.runSection("offerCommitBasement");
            if (!witchFree) mainScript.runConditionalSection("offerCommitBasementNotFree", heartComment);
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
     * @return the ending reached by the player
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
                mainScript.runConditionalSection("fightStartNotFree", heartComment);
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
                case 1:
                    mainScript.runSection("rootsComment" + (menuCount + 1));
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


    // - Chapter II: The Stranger -

    /**
     * Runs Chapter II: The Stranger
     * @return the ending reached by the player
     */
    private ChapterEnding stranger() {
        // You gain the Voice of the Contrarian

        this.secondaryScript = new Script(this.manager, this.parser, "Chapter2Shared");
        if (this.isFirstVessel) manager.setFirstPrincess(Chapter.STRANGER);

        secondaryScript.runSection();
        mainScript.runSection();

        Condition shared = new Condition();
        InverseCondition noShare = shared.getInverse();
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "dejaVu", "(Explore) I'm getting a terrible sense of deja vu.", noShare));
        activeMenu.add(new Option(this.manager, "happened", "(Explore) Wait... hasn't this already happened?", noShare));
        activeMenu.add(new Option(this.manager, "no", "(Explore) Okay, no.", noShare));
        activeMenu.add(new Option(this.manager, "notKidding", "(Explore) You aren't kidding. She actually ended the world last time, didn't she? What the hell is she?.", noShare));
        activeMenu.add(new Option(this.manager, "wise", "(Explore) Oh, you bastard! You're in for it now. I'm wise to your tricks!", noShare));
        activeMenu.add(new Option(this.manager, "died", "(Explore) But I died! The whole world ended! What am I doing here?", noShare));
        activeMenu.add(new Option(this.manager, "walls", "(Explore) Those walls weren't here last time! You can't just force me to go to the cabin."));
        activeMenu.add(new Option(this.manager, "assume", "(Explore)  Let's assume I'm telling the truth, and all of this really did already happen. Why should I listen to you? Why should I bother doing *anything?*", shared));
        activeMenu.add(new Option(this.manager, "lie", "(Lie) Yep. Okay. Heading to the cabin now where I'm definitely going to slay that Princess."));
        activeMenu.add(new Option(this.manager, "cabin", "Yeah, yeah. I get it. I'm going to the cabin."));
        activeMenu.add(new Option(this.manager, "proceed", "[Silently proceed to the cabin.]", noShare));
        activeMenu.add(new Option(this.manager, "abort", this.cantTryAbort, "\"If I can't run away from the cabin, then I'm just staying here in the woods. Forever.\" [Stay in the woods. Forever.]", 0, !this.isFirstVessel));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "dejaVu":
                case "happened":
                case "no":
                case "wise":
                case "died":
                    this.sharedLoop = true;
                    shared.set();
                    
                    secondaryScript.runSection(activeOutcome);
                    mainScript.runSection("shareLoop");
                    break;

                case "notKidding":
                    this.sharedLoop = true;
                    shared.set();

                    mainScript.runSection("notKidding");
                    break;

                case "walls":
                    mainScript.runConditionalSection("walls", true);
                    
                    if (!this.sharedLoop) {
                        this.sharedLoop = true;
                        shared.set();
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
                        cantTryAbort.set();
                        activeMenu.setGreyedOut("abort", true);
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }

                    mainScript.runSection("abort");

                    this.abortVessel(true);
                    return ChapterEnding.ABORTED;

                default: this.giveDefaultFailResponse(activeOutcome);
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

        String endChoiceText = "\"I'm getting you out of here.\" [Try and free her.]\n  (NUM) \"I don't know what you are, but I can't trust you. I can't trust anyone here.\" [Leave her in the basement.]\n  (NUM) [Retrieve the blade.]";

        mainScript.runSection("cabinIntro");

        Condition canAskMirror = new Condition(true);
        Condition canApproach = new Condition(true);
        Condition canThrow = new Condition();
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "mirror", "(Explore) You didn't say anything about the mirror on the wall.", canAskMirror));
        activeMenu.add(new Option(this.manager, "approach", "(Explore) [Approach the mirror.]", canApproach));
        activeMenu.add(new Option(this.manager, "take", "(Explore) [Take the blade.]"));
        activeMenu.add(new Option(this.manager, "throw", "(Explore) [Throw the blade out the window.]", canThrow));
        activeMenu.add(new Option(this.manager, "enter", "[Enter the basement.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "mirror":
                    this.ch2AskMirror(canAskMirror, canApproach);
                    break;

                case "cApproachMirror":
                case "approach":
                    this.ch2ApproachMirror(canAskMirror, canApproach);
                    break;

                case "cTake":
                    activeMenu.setCondition("take", false);
                case "take":
                    this.hasBlade = true;
                    this.withBlade = false;
                    this.canThrowBlade = true;
                    canThrow.set();
                    endChoiceText = "I'm getting you out of here.\" [Try and free her.]\n  (NUM) \"I don't know what you are, but I can't trust you. I can't trust anyone here.\" [Leave her in the basement.]\n  (NUM) [Slay the Princess.]";

                    mainScript.runSection("takeBlade");

                    OptionsMenu subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "keep", "[Keep your grip as it is.]"));
                    subMenu.add(new Option(this.manager, "reverse", "[Hold the blade the other way.]"));

                    mainScript.runSection(parser.promptOptionsMenu(subMenu) + "Grip");
                    break;

                case "cThrow":
                    activeMenu.setCondition("throw", false);
                case "throw":
                    this.hasBlade = false;
                    this.canThrowBlade = false;
                    this.threwBlade = true;
                    canThrow.set(false);
                    endChoiceText = "I'm getting you out of here.\" [Try and free her.]\n  (NUM) \"I don't know what you are, but I can't trust you. I can't trust anyone here.\" [Leave her in the basement.]\n  (NUM) [Regretfully think about that time you threw the blade out the window.]";

                    mainScript.runSection("throwBlade");
                    break;

                case "cGoStairs":
                case "enter":
                    this.repeatActiveMenu = false;
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        this.currentLocation = GameLocation.STAIRS;
        this.withBlade = false;
        this.canThrowBlade = false;
        this.mirrorPresent = false;
        mainScript.runSection("stairsStart");

        if (manager.trueDemoMode()) return ChapterEnding.DEMOENDING;

        GlobalInt schismCount = new GlobalInt(1);
        NumCondition singleSchism = new NumCondition(schismCount, 1);
        NumCondition multiSchism = new NumCondition(schismCount, 1, 1);
        NumCondition notMaxSchisms = new NumCondition(schismCount, 5);
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

        this.currentLocation = GameLocation.BASEMENT;
        this.withPrincess = true;
        mainScript.runConditionalSection(firstSchism + "Stairs", firstSchism);

        String setNewSchism = "";
        boolean newSchismComment = false;
        boolean schismThisOption;
        Condition sharedTask = new Condition();
        Condition canWhatDo = new Condition();

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "sorry", "(Explore) \"I'm sorry... I didn't realize I was here.\"", singleSchism));
        activeMenu.add(new Option(this.manager, "more", "(Explore) \"There's more of you now...\"", multiSchism, notMaxSchisms));
        activeMenu.add(new Option(this.manager, "name", "(Explore) \"What's your name?\"", notMaxSchisms));
        activeMenu.add(new Option(this.manager, "weird", "(Explore) \"Getting down here was... weird. Like I was pulled apart and put back together again. Do you know what happened to me?\"", notMaxSchisms));
        activeMenu.add(new Option(this.manager, "reason", "(Explore) \"For all I know, you're locked up down here for a reason. Do you know why you're down here?\"", notMaxSchisms));
        activeMenu.add(new Option(this.manager, "threatShare", "(Explore) \"You're apparently a threat to the world. I was sent here to slay you.\"", notMaxSchisms));
        activeMenu.add(new Option(this.manager, "whatDo", "(Explore) \"If I let you out of here, what are you going to do?\"", false, canWhatDo, notMaxSchisms));
        activeMenu.add(new Option(true, this.manager, "ending", endChoiceText, multiSchism));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            schismThisOption = false;

            if (!setNewSchism.equals("")) {
                schismsPresent.put(setNewSchism, true);
                setNewSchism = "";
            }

            if (!newSchismComment && multiSchism.check()) {
                newSchismComment = true;
                mainScript.runSection("schism" + schismCount + "Comment");
            }

            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "sorry":
                    mainScript.runSection(firstSchism + "SorryOpening");

                    // New schism; harsh if you have the blade, gentle if not, neutral if you already have that princess
                    newSchismComment = false;
                    schismCount.increment();
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
                        schismCount.increment();

                        this.strangerRunSchismSection("More", "neutral");
                    } else if (!schismsPresent.get("harsh") && !firstSchism.equals("harsh")) {
                        newSchismComment = false;
                        schismThisOption = true;
                        schismsPresent.put("harsh", true);
                        schismCount.increment();
                        
                        this.strangerRunSchismSection("More", "harsh");
                    } else if (!schismsPresent.get("gentle") && !firstSchism.equals("gentle")) {
                        newSchismComment = false;
                        schismThisOption = true;
                        schismsPresent.put("gentle", true);
                        schismCount.increment();

                        this.strangerRunSchismSection("More", "gentle");
                    }

                    if (schismThisOption) {
                        mainScript.runSection("moreSchism");
                    } else {
                        this.strangerRunSchismSection("More", "neutral", "harsh", "gentle");
                        mainScript.runSection("moreNoSchism");
                    }

                    break;

                case "name":
                    mainScript.runSection(firstSchism + "Name");

                    // Attempt new schism: harsh if you have the blade, gentle if not, neutral if you already have that princess; fails if you have all 3 already
                    if (this.hasBlade && !firstSchism.equals("harsh")) {
                        newSchismComment = false;
                        schismCount.increment();
                        schismThisOption = true;
                        schismsPresent.put("harsh", true);
                    } else if (!this.hasBlade && !firstSchism.equals("gentle")) {
                        newSchismComment = false;
                        schismCount.increment();
                        schismThisOption = true;
                        schismsPresent.put("gentle", true);
                    } else if (!schismsPresent.get("neutral") && !firstSchism.equals("neutral")) {
                        newSchismComment = false;
                        schismCount.increment();
                        schismThisOption = true;
                        schismsPresent.put("neutral", true);
                    }

                    if (schismThisOption) mainScript.runSection("genericFracture");
                    this.strangerRunSchismSection(schismsPresent, "Name", "neutral", "harsh", "gentle", "emo", "monster");
                    if (schismCount.check() != 2) mainScript.runSection("nameFollowUp");
                    break;

                case "weird":
                    mainScript.runSection(firstSchism + "FirstWeird");
                    mainScript.runSection("genericFracture");

                    // New schism: attempt emo, then attempt monster, then attempt gentle, then attempt harsh, then neutral
                    newSchismComment = false;
                    schismCount.increment();
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
                    
                    canWhatDo.set(activeMenu.hasBeenPicked("reason") && schismsPresent.get("monster"));
                    break;

                case "reason":
                    activeMenu.setCondition("threatShare", false);
                    mainScript.runSection(firstSchism + "Reason");

                    // New schism: attempt emo, then attempt monster
                    if (!schismsPresent.get("emo")) {
                        newSchismComment = false;
                        schismCount.increment();
                        schismThisOption = true;
                        schismsPresent.put("emo", true);
                    } else if (!schismsPresent.get("monster")) {
                        newSchismComment = false;
                        schismCount.increment();
                        schismThisOption = true;
                        schismsPresent.put("monster", true);
                    }

                    if (schismThisOption) mainScript.runSection("genericFracture");
                    this.strangerRunSchismSection(schismsPresent, "Reason", "emo", "monster");
                    
                    canWhatDo.set(schismsPresent.get("monster"));
                    break;

                case "threatShare":
                    sharedTask.set();
                    mainScript.runSection(firstSchism + "ThreatShare");

                    // New schism: attempt monster, then attempt emo
                    if (!schismsPresent.get("monster")) {
                        newSchismComment = false;
                        schismCount.increment();
                        schismThisOption = true;
                        schismsPresent.put("monster", true);
                    } else if (!schismsPresent.get("emo")) {
                        newSchismComment = false;
                        schismCount.increment();
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
                        schismCount.increment();
                        schismThisOption = true;
                        setNewSchism = "harsh";
                    } else if (!schismsPresent.get("neutral") && !firstSchism.equals("neutral")) {
                        newSchismComment = false;
                        schismCount.increment();
                        schismThisOption = true;
                        setNewSchism = "neutral";
                    } else if (!schismsPresent.get("gentle") && !firstSchism.equals("gentle")) {
                        newSchismComment = false;
                        schismCount.increment();
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
                    mainScript.runConditionalSection("whatDoFollowUp", schismsPresent.get("monster"));
                    break;

                case "cGoStairs":
                case "cSlayPrincess":
                    if (schismCount.equals(1)) {
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
        this.currentVoices.put(Voice.NARRATOR, false);
        mainScript.runSection("endingStart");

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "okay", "\"It's going to be okay...\""));
        activeMenu.add(new Option(this.manager, "best", "\"I'll do my best.\""));
        activeMenu.add(new Option(this.manager, "supposed", "\"I don't think you're supposed to be fixed.\""));
        activeMenu.add(new Option(this.manager, "no", "\"No.\""));
        activeMenu.add(new Option(this.manager, "destroyed", "\"You just destroyed everything. I'm not going to fix you.\""));
        activeMenu.add(new Option(this.manager, "silent", "[Say nothing.]"));

        if (parser.promptOptionsMenu(activeMenu).equals("silent")) {
            mainScript.runClaimSection("endSilent", true);
        } else {
            mainScript.runClaimSection("endReply", true);
        }
        
        if (this.isFirstVessel) manager.updateMoundValues(0, 1);
        return ChapterEnding.ILLUSIONOFCHOICE;
    }

    /**
     * Used during Chapter II: The Stranger; for a given ordering of schisms, checks whether a given schism of the Princess is present, then runs the corresponding section in the script
     * @param schismsPresent Which versions of the Princess are currently present
     * @param sectionID The last part of each label in the script which corresponds to this response
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

            if (schismsPresent.get(schism)) mainScript.runSection(schism + sectionID);
        }
    }

    /**
     * Used during Chapter II: The Stranger; for a given ordering of schisms of the Princess, runs the corresponding section in the script
     * @param sectionID The last part of each label in the script which corresponds to this response
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
     * @return the ending reached by the player
     */
    private ChapterEnding prisoner() {
        // You gain the Voice of the Skeptic

        if (!this.chapter2Intro(true, false, true)) return ChapterEnding.ABORTED;

        mainScript.runSection("cabinIntro");

        Condition canAskMirror = new Condition(true);
        Condition canApproach = new Condition(true);
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "mirror", "(Explore) You didn't say anything about the mirror on the wall.", canAskMirror));
        activeMenu.add(new Option(this.manager, "different", "(Explore) This whole cabin is different than last time.", this.sharedLoopInsist));
        activeMenu.add(new Option(this.manager, "approach", "(Explore) [Approach the mirror.]", canApproach));
        activeMenu.add(new Option(this.manager, "take", "(Explore) [Take the blade.]"));
        activeMenu.add(new Option(this.manager, "enter", "[Enter the basement.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "mirror":
                    this.ch2AskMirror(canAskMirror, canApproach);
                    break;

                case "different":
                    mainScript.runSection("cabinDifferent");
                    break;

                case "cApproachMirror":
                case "approach":
                    this.ch2ApproachMirror(canAskMirror, canApproach);
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

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Enter the basement
        if (!this.hasBlade) {
            mainScript.runSection("stairsNoBlade");

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

        if (manager.trueDemoMode()) return ChapterEnding.DEMOENDING;

        Condition narratorProof = new Condition();
        InverseCondition narratorNoProof = narratorProof.getInverse();
        Condition locked = new Condition();
        InverseCondition notLocked = locked.getInverse();

        this.canSlayPrincess = true;
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "leave", "I'm not sure I need to do much of anything. Do you see how locked up she is? I'm just going to leave her here. [Leave the Princess in the basement.]"));
        activeMenu.add(new Option(this.manager, "sit", "[Sit with her.]"));
        activeMenu.add(new Option(this.manager, "slay", manager.demoMode(), "[Slay the Princess.]", 0));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "cGoStairs":
                    if (locked.check()) {
                        mainScript.runSection("leaveAttemptLocked");
                        break;
                    }
                case "leave":
                    locked.set();
                    mainScript.runConditionalSection("doorLock", this.sharedLoopInsist);
                    mainScript.runSection("doorLockCommentStart");
                    break;

                case "sit":
                    this.repeatActiveMenu = false;
                    break;

                case "cSlayPrincess":
                    if (this.cantJoint3.check()) {
                        parser.printDialogueLine(DEMOBLOCK);
                        break;
                    }
                case "slay":
                    if (manager.hasVisited(Chapter.GREY)) {
                        parser.printDialogueLine(WORNPATH);
                        this.cantJoint3.set();
                        activeMenu.setGreyedOut("slay", true);
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.GREY, ChapterEnding.COLDLYRATIONAL)) {
                        this.cantJoint3.set();
                        activeMenu.setGreyedOut("slay", true);
                        break;
                    }

                    mainScript.runSection("attackStart");
                    return this.prisonerStrangled(true);

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Sit with her
        mainScript.runSection("sitStart");

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "right", "[Sit where you were told to sit.]"));
        activeMenu.add(new Option(this.manager, "closer", "[Sit closer.]"));

        switch (parser.promptOptionsMenu(activeMenu)) {
            case "right":
                this.repeatActiveMenu = false;
                break;

            case "closer":
                mainScript.runConditionalSection("sitTooClose", narratorProof);
                return this.prisonerStrangled(false);
        }

        mainScript.runSection();

        Condition talked = new Condition();
        InverseCondition noTalk = talked.getInverse();
        Condition askedIntentions = new Condition();
        InverseCondition noIntentions = askedIntentions.getInverse();
        Condition whatDo = new Condition();
        InverseCondition noWhatDo = whatDo.getInverse();
        Condition noGiveExplore = new Condition();
        InverseCondition noGiveNotExplored = noGiveExplore.getInverse();
        Condition noPatientComment = new Condition(true);
        Condition immovable = new Condition();

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "inspect", "(Explore) [Inspect the shackle.]"));
        activeMenu.add(new Option(this.manager, "lockedBeg", "(Explore) Hey! Let me out of here! Open the door!", locked));
        activeMenu.add(new Option(this.manager, "intentionsA", "(Explore) \"You were asking about my intentions earlier. What did you mean by that?\"", talked, noIntentions));
        activeMenu.add(new Option(this.manager, "intentionsB", "(Explore) \"My intentions? What do you mean?\"", noTalk, noIntentions));
        activeMenu.add(new Option(this.manager, "memory", "(Explore) \"I've been here before. Am I the only one who remembers that?\"", narratorNoProof));
        activeMenu.add(new Option(this.manager, "afterDied", "(Explore) \"What happened after I died last time?\""));
        activeMenu.add(new Option(this.manager, "head", "(Explore) \"How am I supposed to cut you out? If you didn't notice, your head is in a shackle, too.\"", askedIntentions, noWhatDo));
        activeMenu.add(new Option(this.manager, "otherChain", "(Explore) \"That other chain on the wall... who is it for?\""));
        activeMenu.add(new Option(this.manager, "whatDo", "(Explore) \"What would you have me do?\"", noWhatDo));
        activeMenu.add(new Option(this.manager, "noGiveA", "(Explore) \"I'm not giving you a weapon.\"", whatDo, noGiveNotExplored));
        activeMenu.add(new Option(this.manager, "distrust", "(Explore) \"You realize that I have as much reason to distrust you as you have to distrust me, right?\"", whatDo, noPatientComment));
        activeMenu.add(new Option(this.manager, "noGiveB", "(Explore) \"I'm not giving you a weapon. In case you've forgotten, you killed me with it last time.\"", whatDo, noGiveNotExplored));
        activeMenu.add(new Option(this.manager, "noGiveC", "(Explore) \"*I* can cut you out of here. I'm not giving you a weapon.\"", whatDo, noGiveNotExplored));
        activeMenu.add(new Option(this.manager, "threaten", "(Explore) \"If you don't give up now, then I have no choice but to slay you.\"", noGiveExplore));
        activeMenu.add(new Option(this.manager, "negotiate", "(Explore) \"You're the one in chains. If anyone should be to negotiating here, it's you, not me.\"", noGiveExplore));
        activeMenu.add(new Option(this.manager, "giveBlade", "\"Okay. I'm trusting you.\" [Give her the blade.]", whatDo));
        activeMenu.add(new Option(this.manager, "cutAttempt", "\"If you want to leave, I'm going to be the one with the weapon. Deal with it.\" [Cut her out on your own.]", whatDo));
        activeMenu.add(new Option(this.manager, "slayA", this.cantJoint3, "\"If that's how you're going to be, then I guess I have to do this.\" [Slay the Princess.]", noGiveExplore));
        activeMenu.add(new Option(this.manager, "slayB", this.cantJoint3, "\"It seems I don't have much of a choice. I'm sorry.\" [Slay the Princess.]", locked));
        activeMenu.add(new Option(this.manager, "leaveA", "\"If you're so immovable, then I'm just going to leave you here. Bye!\" [Leave the Princess in the basement.]", notLocked, immovable));
        activeMenu.add(new Option(this.manager, "leaveB", "\"I think I'm just going to leave you here, actually. You're not much of a threat to anyone locked up like that. Bye!\" [Leave the Princess in the basement.]", notLocked));
        activeMenu.add(new Option(this.manager, "slayC", this.cantJoint3, "[Slay the Princess.]"));

        activeMenu.get("inspect").setPrerequisite(activeMenu.get("otherChain"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "inspect":
                    mainScript.runSection("inspectMenu");

                    OptionsMenu inspectMenu = new OptionsMenu(true);
                    inspectMenu.add(new Option(this.manager, "inspect", "[Inspect the shackle.]"));
                    inspectMenu.add(new Option(this.manager, "return", "[Let it go.]"));

                    switch (parser.promptOptionsMenu(inspectMenu)) {
                        case "inspect":
                            this.prisonerChained();
                            return ChapterEnding.PRISONEROFMIND;
                        
                        case "return":
                            mainScript.runSection();
                            break;
                    }

                    break;

                case "lockedBeg":
                case "otherChain":
                    talked.set();
                    mainScript.runSection(activeOutcome + "Menu");
                    break;

                case "intentionsA":
                case "intentionsB":
                    talked.set();
                    askedIntentions.set();
                    mainScript.runConditionalSection("intentionsMenu", narratorProof);
                    break;

                case "memory":
                    talked.set();
                    mainScript.runSection("memoryMenu");
                    if (narratorNoProof.check()) mainScript.runSection("narratorProof");
                    narratorProof.set();
                    break;

                case "afterDied":
                    talked.set();
                    mainScript.runConditionalSection("afterDiedMenu", narratorProof);
                    if (narratorNoProof.check()) mainScript.runSection("narratorProof");
                    narratorProof.set();
                    break;

                case "head":
                case "whatDo":
                    talked.set();
                    whatDo.set();
                    mainScript.runSection(activeOutcome + "Menu");
                    break;

                case "noGiveA":
                case "noGiveC":
                    noGiveExplore.set();
                    mainScript.runConditionalSection("noGiveJoin", narratorProof);
                    if (narratorNoProof.check()) mainScript.runSection("narratorProof");
                    narratorProof.set();
                    break;

                case "noGiveB":
                    noGiveExplore.set();
                    mainScript.runConditionalSection("noGiveBMenu", narratorProof);
                    break;

                case "distrust":
                    noPatientComment.set(false);
                    mainScript.runSection("distrustMenu");
                    break;

                case "threaten":
                    immovable.set();
                    mainScript.runSection("threatenMenu");
                    break;

                case "negotiate":
                    immovable.set();
                    mainScript.runConditionalSection("negotiateMenu", locked);
                    mainScript.runConditionalSection("negotiateMenuCont", noPatientComment);
                    break;

                case "giveBlade":
                    mainScript.runConditionalSection("giveBladeStart", narratorProof);
                    return this.prisonerDecapitate();

                case "cutAttempt":
                    mainScript.runConditionalSection("attemptCut", narratorProof);
                    return this.prisonerStrangled(false);

                case "slayA":
                case "slayB":
                    if (manager.hasVisited(Chapter.GREY)) {
                        parser.printDialogueLine(WORNPATH);
                        this.cantJoint3.set();
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.GREY, ChapterEnding.COLDLYRATIONAL)) {
                        this.cantJoint3.set();
                        break;
                    }

                    mainScript.runConditionalSection(activeOutcome + "Start", narratorProof);
                    return this.prisonerStrangled(true);

                case "cSlayPrincess":
                    if (this.cantJoint3.check()) {
                        parser.printDialogueLine(DEMOBLOCK);
                        break;
                    }
                case "slayC":
                    if (manager.hasVisited(Chapter.GREY)) {
                        parser.printDialogueLine(WORNPATH);
                        this.cantJoint3.set();
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.GREY, ChapterEnding.COLDLYRATIONAL)) {
                        this.cantJoint3.set();
                        break;
                    }

                    mainScript.runConditionalSection("attackJoin", narratorProof);
                    return this.prisonerStrangled(true);

                case "leaveA":
                    locked.set();
                    mainScript.runSection("leaveAttemptA");
                    mainScript.runConditionalSection("doorLock", this.sharedLoopInsist || narratorProof.check());
                    mainScript.runSection("doorLockCommentAbandon");
                    break;

                case "cGoStairs":
                    if (locked.check()) {
                        mainScript.runSection("leaveAttemptLocked");
                        break;
                    }
                case "leaveB":
                    locked.set();
                    mainScript.runSection("leaveAttemptB");
                    mainScript.runConditionalSection("doorLock", this.sharedLoopInsist || narratorProof.check());
                    mainScript.runSection("doorLockCommentAbandon");
                    break;
            }
        }

        throw new RuntimeException("No ending reached");
    }

    /**
     * The player continues to investigate the empty chain, leading to the "Prisoner of the Mind" ending and claiming the Prisoner
     */
    private void prisonerChained() {
        this.hasBlade = false;
        mainScript.runSection("chainedStart");

        int talkCount = 0;
        boolean smallTalk = false;
        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "stuck", "(Explore) \"So we're both stuck here.\""));
        activeMenu.add(new Option(this.manager, "whatDo", "(Explore) \"What should we do?\""));
        activeMenu.add(new Option(this.manager, "know", "(Explore) \"Did you know that this was going to happen to me?\""));
        activeMenu.add(new Option(this.manager, "smallTalk", "(Explore) \"Not one for small talk, are you?\"", false));
        activeMenu.add(new Option(this.manager, "wait", "[Wait in silence.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            if (talkCount >= 2) activeMenu.setCondition("smallTalk", true);

            switch (parser.promptOptionsMenu(activeMenu)) {
                case "stuck":
                case "whatDo":
                case "know":
                    if (smallTalk) {
                        mainScript.runSection("chainedNoSmallTalk");
                    } else {
                        mainScript.runSection(activeOutcome + "Chained");
                    }
                    
                    break;

                case "smallTalk":
                    smallTalk = true;
                    mainScript.runSection("smallTalkChained");
                    break;

                case "wait":
                    this.repeatActiveMenu = false;
                    break;
            }
        }

        mainScript.runSection("chainedCont");

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "explore", "(Explore) \"Hey, have you noticed the basement changing?\""));
        activeMenu.add(new Option(this.manager, "wait", "[Continue to wait in silence.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "explore":
                    if (smallTalk) {
                        mainScript.runSection("basementNoSmallTalk");
                    } else {
                        mainScript.runSection("basementSmallTalk");
                    }

                    break;

                case "wait":
                    this.repeatActiveMenu = false;
                    break;
            }
        }

        mainScript.runSection("chainedEnd");
    }

    /**
     * The player violates the Prisoner's agency and she attempts to strangle them
     * @param attack whether the player attempted to slay the Princess
     * @return the ending reached by the player
     */
    private ChapterEnding prisonerStrangled(boolean attack) {
        this.canSlayPrincess = true;
        this.canDropBlade = true;
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "slay", this.cantJoint3, "[Slay the Princess.]", 0));
        activeMenu.add(new Option(this.manager, "drop", "[Drop it.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "cSlayPrincess":
                    if (this.cantJoint3.check()) {
                        parser.printDialogueLine(DEMOBLOCK);
                        break;
                    }
                case "slay":
                    if (manager.hasVisited(Chapter.GREY)) {
                        parser.printDialogueLine(WORNPATH);
                        parser.printDialogueLine(WORNPATHHERO);
                        this.cantJoint3.set();
                        break;
                    } else if (!attack) {
                        if (!manager.confirmContentWarnings(Chapter.GREY, ChapterEnding.COLDLYRATIONAL)) {
                            this.cantJoint3.set();
                            break;
                        }
                    }

                    mainScript.runSection("slayStart");
                    return this.prisonerSlain(false);

                case "cDrop":
                case "drop":
                    this.repeatActiveMenu = false;
                    break;

                default: this.giveDefaultFailResponse();
            }
        }

        // Drop the blade
        this.hasBlade = false;
        mainScript.runConditionalSection("strangleDropStart", attack);
        return this.prisonerDecapitate();
    }

    /**
     * The Prisoner cuts off her own head after the player gives her the knife (willingly or not)
     * @return the ending reached by the player
     */
    private ChapterEnding prisonerDecapitate() {
        mainScript.runSection("headStart");

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "away", "[Look away.]"));
        activeMenu.add(new Option(this.manager, "watch", "[Watch in uneasy silence.]"));

        mainScript.runSection(parser.promptOptionsMenu(activeMenu) + "Head");

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "leave", this.cantUnique3, "[Leave the basement to claim your reward.]", 0));
        activeMenu.add(new Option(this.manager, "take", "[Take the Princess with you.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "cGoStairs":
                    if (this.cantUnique3.check()) {
                        parser.printDialogueLine(DEMOBLOCK);
                        break;
                    }
                case "leave":
                    if (!manager.confirmContentWarnings(Chapter.CAGE, "suicide")) {
                        this.cantUnique3.set();
                        break;
                    }

                    return this.prisonerSlain(true);

                case "take":
                    this.repeatActiveMenu = false;
                    break;

                default: this.giveDefaultFailResponse();
            }
        }

        this.currentLocation = GameLocation.CABIN;
        mainScript.runSection();

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "leave", "[Leave the cabin and claim your reward.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "cGoHill":
                case "leave":
                    this.repeatActiveMenu = false;
                    break;

                default: this.giveDefaultFailResponse();
            }
        }

        mainScript.runSection();
        return ChapterEnding.TALKINGHEADS;
    }

    /**
     * With the Prisoner dead, the player leaves the basement
     * @param selfSlain whether the Princess decapitated herself (indicating which Chapter this leads to)
     * @return the ending reached by the player
     */
    private ChapterEnding prisonerSlain(boolean selfSlain) {
        this.currentLocation = GameLocation.CABIN;
        this.hasBlade = true;
        mainScript.runSection("slainStart");

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "leave", "[Open the door and accept your reward.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "cGoHill":
                case "leave":
                    this.repeatActiveMenu = false;
                    break;

                default: this.giveDefaultFailResponse();
            }
        }

        mainScript.runSection();

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "bullshit", "That's bullshit! Let us out of here!"));
        activeMenu.add(new Option(this.manager, "ok", "I don't know. I'm pretty okay with this."));
        activeMenu.add(new Option(this.manager, "better", "I was kind of hoping we'd get a better ending for saving the world."));

        mainScript.runSection(parser.promptOptionsMenu(activeMenu) + "DoorMenu");

        boolean suggestion = false;
        Condition happyExplored = new Condition();
        InverseCondition noHappyExplore = happyExplored.getInverse();

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "happy", "(Explore) Didn't you hear The Narrator? I'm happy. We're happy."));
        activeMenu.add(new Option(this.manager, "suggest", "(Explore) What do you suggest, then?"));
        activeMenu.add(new Option(this.manager, "acceptA", "I don't want to die again. I didn't like dying last time. I'm going to accept my reward now.", activeMenu.get("suggest")));
        activeMenu.add(new Option(this.manager, "acceptB", "I dunno, I'm pretty happy. I'm going to accept my reward now.", noHappyExplore));
        activeMenu.add(new Option(this.manager, "acceptC", "Well, you might not be happy, but I am. I'm going to accept my reward now.", happyExplored));
        activeMenu.add(new Option(this.manager, "paranoid", "[Slay yourself.]", activeMenu.get("suggest")));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "happy":
                    happyExplored.set();
                    mainScript.runSection("happySlain");
                    break;

                case "suggest":
                    this.canSlaySelf = true;
                    suggestion = true;
                    mainScript.runSection("suggestSlain");
                    break;

                case "acceptA":
                case "acceptB":
                case "acceptC":
                    mainScript.runConditionalSection("slainAccept", suggestion);

                    // Option names here correspond to which Voice you gain in The Cage
                    this.canSlaySelf = true;
                    this.activeMenu = new OptionsMenu();
                    activeMenu.add(new Option(this.manager, "noWay", "(Explore) Is there really no other way? Because I don't want to use the blade on myself."));
                    activeMenu.add(new Option(this.manager, "happy2", "(Explore) But I liked being happy! Are you really going to take it away from me?"));
                    activeMenu.add(new Option(this.manager, "broken", "[Give the blade to the Voice of the Skeptic.]", activeMenu.get("noWay")));
                    activeMenu.add(new Option(this.manager, "paranoid", "[Slay yourself.]"));
                    activeMenu.add(new Option(this.manager, "cheated", "Sorry, but we're not doing that."));

                    while (repeatActiveMenu) {
                        this.activeOutcome = parser.promptOptionsMenu(activeMenu);
                        switch (activeOutcome) {
                            case "noWay":
                            case "happy2":
                                mainScript.runSection(activeOutcome + "Slain");
                                break;

                            case "broken":
                            case "paranoid":
                            case "cheated":
                                this.repeatActiveMenu = false;
                                mainScript.runSection(activeOutcome + "Suicide");
                                mainScript.runSection(activeOutcome + "SuicideCont");
                                break;

                            default: super.giveDefaultFailResponse();
                        }
                    }
                    break;
                    
                case "cSlaySelf":
                case "paranoid":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("suicideStart");
                    mainScript.runSection("paranoidSuicideCont");
                    break;

                default: super.giveDefaultFailResponse();
            }
        }
        
        if (!selfSlain) {
            if (activeOutcome.equals("cheated")) this.prisonerHeartStopped = true;
            return ChapterEnding.COLDLYRATIONAL;
        } else {
            switch (activeOutcome) {
                case "broken": return ChapterEnding.RESTLESSGIVEIN;
                case "cheated": return ChapterEnding.RESTLESSFORCED;
                default: return ChapterEnding.RESTLESSSELF;
            }
        }
    }


    // - Chapter II: The Damsel -

    /**
     * Runs Chapter II: The Damsel
     * @return the ending reached by the player
     */
    private ChapterEnding damsel() {
        // You gain the Voice of the Smitten

        if (!this.chapter2Intro(true, false, true)) return ChapterEnding.ABORTED;

        mainScript.runSection("cabinIntro");

        Condition canAskMirror = new Condition(true);
        Condition canApproach = new Condition(true);
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "mirror", "(Explore) You didn't say anything about the mirror on the wall.", canAskMirror));
        activeMenu.add(new Option(this.manager, "different", "(Explore) This whole cabin is different than last time.", this.sharedLoopInsist));
        activeMenu.add(new Option(this.manager, "approach", "(Explore) [Approach the mirror.]", canApproach));
        activeMenu.add(new Option(this.manager, "take", "(Explore) [Take the blade.]"));
        activeMenu.add(new Option(this.manager, "enter", "[Enter the basement.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "mirror":
                    this.ch2AskMirror(canAskMirror, canApproach);
                    break;

                case "different":
                    mainScript.runSection("cabinDifferent");
                    break;

                case "cApproachMirror":
                case "approach":
                    this.ch2ApproachMirror(canAskMirror, canApproach);
                    break;

                case "cTake":
                    activeMenu.setCondition("take", false);
                case "take":
                    this.hasBlade = true;
                    this.withBlade = false;
                    mainScript.runConditionalSection("takeBlade", this.sharedLoop);
                    break;

                case "cGoStairs":
                case "enter":
                    this.repeatActiveMenu = false;
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Enter the basement
        this.currentLocation = GameLocation.BASEMENT;
        this.withPrincess = true;
        this.withBlade = false;
        this.mirrorPresent = false;
        mainScript.runConditionalSection("stairsStart");

        if (manager.trueDemoMode()) return ChapterEnding.DEMOENDING;

        mainScript.runSection("basementStart");

        boolean howFree = false;
        String endWorldResponse = "";

        OptionsMenu subMenu;
        this.canSlayPrincess = true;
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "killed", "(Explore) \"You killed me last time and it hurt a lot! Why did you do that?\""));
        activeMenu.add(new Option(this.manager, "howFreeNoBlade", "(Explore) \"I didn't bring a knife. Do I have to cut you out again?\"", !this.hasBlade));
        activeMenu.add(new Option(this.manager, "howFreeBlade", "(Explore) \"Do I have to cut you out again? I really didn't care for that last time.\"", this.hasBlade));
        activeMenu.add(new Option(this.manager, "after", "(Explore) \"What happened after I died?\""));
        activeMenu.add(new Option(this.manager, "end", "(Explore) \"I have to ask... did you end the world after you killed me back there?\""));
        activeMenu.add(new Option(this.manager, "sorry", "(Explore) \"I'm sorry about what happened last time. The Narrator who sent me here to kill you took over my body. It was extremely unfair.\""));
        activeMenu.add(new Option(this.manager, "rescue", "[Rescue the Princess.]"));
        activeMenu.add(new Option(this.manager, "slay", this.cantJoint3, "[Slay the Princess.]", 0));

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
                    activeMenu.setDisplay("end", "(Explore) \"But before we started talking, did the world end? Did you end the world?\"");
                    mainScript.runSection("after");
                    break;

                case "end":
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
                        this.cantJoint3.set();
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.GREY, ChapterEnding.LADYKILLER, "forced suicide")) {
                        this.cantJoint3.set();
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
     * @return the ending reached by the player
     */
    private ChapterEnding damselRescue(boolean howFree, String endWorldResponse) {
        mainScript.runConditionalSection("rescueStart", howFree);

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
     * @return the ending reached by the player
     */
    private ChapterEnding damselDeconSequence(String endWorldResponse) {
        mainScript.runConditionalSection("whatWant", this.whatWouldYouDo);

        GlobalInt depersonCount = new GlobalInt();
        NumCondition depersoned = new NumCondition(depersonCount, 1, 0);
        Condition parrotComment = new Condition();
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "stay", this.cantUnique3, "\"Okay. Clearly something is happening here, and I'm very scared. What if we just... don't do anything? What if we just stay here? Nobody gets hurt, and we just figure out a way to be happy, together.\"", new NumCondition(depersonCount, 1, 2)));
        activeMenu.add(new Option(this.manager, "parrot", "(Explore) \"You're just parroting my questions. What do you actually want?\"", parrotComment));
        activeMenu.add(new Option(this.manager, "wantA", "(Explore) \"I want you to tell me what you want.\""));
        activeMenu.add(new Option(this.manager, "wantB", "(Explore) \"There must be something you want!\""));
        activeMenu.add(new Option(this.manager, "makeYou", "(Explore) \"But what would make you happy?\"", depersoned));
        activeMenu.add(new Option(this.manager, "more", "(Explore) \"You have to want something more than just making me happy.\"", depersoned));
        activeMenu.add(new Option(this.manager, "ownThing", "(Explore) \"But you need your own thing. You just met me. You can't base your entire happiness around me.\"", depersoned));
        activeMenu.add(new Option(this.manager, "unhappy", "(Explore) \"I want you to make me unhappy.\"", depersoned));
        activeMenu.add(new Option(this.manager, "endWorld", "(Explore) \"Do you want to end the world?\""));
        activeMenu.add(new Option(this.manager, "leaveA", "\"I just want to leave. We can figure out the rest later.\""));
        activeMenu.add(new Option(this.manager, "leaveB", "\"If you want to leave, let's leave.\""));
        activeMenu.add(new Option(this.manager, "leaveC", "\"This isn't right. Let's just get out of here.\"", depersoned));
        activeMenu.add(new Option(this.manager, "slay", this.cantJoint3, "\"Something isn't right here. I'm sorry.\" [Slay the Princess.]", 0, depersoned));

        boolean skipDepersonComment = false;
        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (depersonCount.check()) {
                case 1:
                    this.canSlayPrincess = this.hasBlade;
                case 2:
                case 3:
                    if (!skipDepersonComment) mainScript.runSection("depersonComment" + depersonCount.check());
                    break;

                case 4:
                    mainScript.runSection("deconEnding");
                    return ChapterEnding.ANDTHEYLIVEDHAPPILY;
            }

            skipDepersonComment = false;
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "stay":
                    if (!manager.confirmContentWarnings(Chapter.HAPPY, "forced self-mutilation; forced suicide")) {
                        this.cantUnique3.set();
                        break;
                    }

                    mainScript.runSection("stayStartDecon");
                    return ChapterEnding.CONTENTSOFOURHEARTDECON;

                case "parrot":
                case "wantA":
                case "wantB":
                    depersonCount.increment();
                    mainScript.runConditionalSection("depersonTruth" + depersonCount, depersonCount);
                    mainScript.runSection("wantDecon");
                    break;

                case "makeYou":
                case "more":
                case "ownThing":
                case "unhappy":
                    depersonCount.increment();
                    mainScript.runConditionalSection("depersonTruth" + depersonCount, depersonCount);
                    mainScript.runSection(activeOutcome + "Decon");
                    break;

                case "endWorld":
                    mainScript.runSection("endWorldDecon");

                    if (endWorldResponse.isEmpty()) {
                        mainScript.runConditionalSection("endWorldDeconFirst", depersonCount);
                        if (!depersonCount.equals(0)) parrotComment.set();

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

                                if (depersonCount.equals(0)) {
                                    depersonCount.increment();
                                    mainScript.runConditionalSection("depersonTruth" + depersonCount, depersonCount);
                                }

                                mainScript.runSection("saveEndWorldDecon");
                                break;

                            default:
                                mainScript.runSection(endWorldResponse + "EndWorldDecon");

                                if (depersonCount.equals(0)) {
                                    depersonCount.increment();
                                    mainScript.runConditionalSection("depersonTruth" + depersonCount, depersonCount);
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
                    if (depersonCount.greaterThan(1)) {
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
                        this.cantJoint3.set();
                        break;
                    } else if (!manager.confirmContentWarnings(Chapter.GREY, ChapterEnding.LADYKILLER, "forced suicide")) {
                        this.cantJoint3.set();
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
     * The player leaves the basement with the Damsel
     * @param endWorldResponse how the player responds to the Princess asking if she was "supposed to end the world," if they triggered the question
     * @return the ending reached by the player
     */
    private ChapterEnding damselLeave(String endWorldResponse) {
        boolean tookBlade = this.hasBlade;
        this.hasBlade = false;
        this.canSlayPrincess = false;
        mainScript.runBladeSection("leaveStart");

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
            this.withBlade = !this.cantJoint3.check();
            mainScript.runSection("finalBladeChoice");

            this.activeMenu = new OptionsMenu();
            activeMenu.add(new Option(this.manager, "slay", this.cantJoint3, "[Take the blade and slay the Princess.]", 0));
            activeMenu.add(new Option(this.manager, "nope", "[You're not doing that.]"));

            this.repeatActiveMenu = true;
            while (repeatActiveMenu) {
                this.activeOutcome = parser.promptOptionsMenu(activeMenu);
                switch (activeOutcome) {
                    case "cTake":
                    case "cSlayPrincessNoBladeFail":
                        if (this.cantJoint3.check()) {
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
                            this.cantJoint3.set();
                            break;
                        } else if (!manager.confirmContentWarnings(Chapter.GREY, ChapterEnding.LADYKILLER, "forced suicide")) {
                            this.withBlade = false;
                            this.cantJoint3.set();
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
        activeMenu.add(new Option(this.manager, "stay", this.cantUnique3, "\"All we need to be happy is each other, what if we just stayed here and built a life together?\""));
        activeMenu.add(new Option(this.manager, "stop", "Stop it with these interruptions. I already made up my mind. We're leaving."));
        activeMenu.add(new Option(this.manager, "ignore", "[Just ignore them.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "stay":
                    mainScript.runSection("staySuggest");

                    this.activeMenu = new OptionsMenu(true);
                    activeMenu.add(new Option(this.manager, "nonchalance", this.cantUnique3, "\"Your nonchalance about the fate of the world has me a bit worried. That's why I want to stay here.\"", 0, !endWorldResponse.isEmpty()));
                    activeMenu.add(new Option(this.manager, "trust", this.cantUnique3, "\"Trust me. It'll be better for both of us if we stay. We can be happy here. We just have to want it.\"", 0));
                    activeMenu.add(new Option(this.manager, "leave", "\"You're right. We're leaving.\""));
                    activeMenu.add(new Option(this.manager, "silent", this.cantUnique3, "[Remain silent.]", 0));

                    switch (parser.promptOptionsMenu(activeMenu)) {
                        case "leave":
                            this.repeatActiveMenu = false;
                            mainScript.runSection("stayReturn");
                            break;

                        case "nonchalance":
                            if (!manager.confirmContentWarnings(Chapter.HAPPY, "forced self-mutilation, forced suicide")) {
                                this.cantUnique3.set();
                                break;
                            }

                            mainScript.runSection("stayNonchalance");
                            return ChapterEnding.CONTENTSOFOURHEARTUPSTAIRS;

                        case "trust":
                        case "silent":
                            if (!manager.confirmContentWarnings(Chapter.HAPPY, "forced self-mutilation, forced suicide")) {
                                this.cantUnique3.set();
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

        mainScript.runConditionalSection("leaveEnding", true);
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
}
