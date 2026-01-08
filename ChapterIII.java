import java.util.ArrayList;

public class ChapterIII extends StandardCycle {
    
    private ChapterEnding prevEnding;
    
    // Variables that are used in all chapters
    private Voice ch3Voice;
    private String source = "";
    private boolean threwBlade;

    // Flags that persist from Chapter 2
    private Voice ch2Voice;
    private final boolean sharedLoop;
    private final boolean sharedLoopInsist;
    private final boolean freeFromChains2;
    private final boolean abandoned2;
    private final boolean adversaryTookBlade;
    private final boolean adversaryFaceExplore;
    private final boolean spectrePossessAsk;
    private final boolean spectreCantWontAsk;
    private final boolean spectreEndSlay;
    private final boolean prisonerForcedBlade;
    private final boolean prisonerWatchedHead;
    private final boolean prisonerGoodEndingSeen;
    private final boolean prisonerHeartStopped;

    // Flags used in The Princess and the Dragon
    private boolean dragonInPrincess = false;
    private boolean dragonBodyDownstairs = false;

    // Flags used in The Cage
    private boolean cageCutRoute = false;
    private boolean cageNoBladeMentioned = false;

    /**
     * Constructor
     * @param prevEnding the ending of the previous chapter
     * @param manager the GameManager to link this chapter to
     * @param parser the IOHandler to link this chapter to
     * @param route the Chapters the player has visited so far during this route
     * @param hasTriedAbort whether the player has already tried (and failed) to abort this route
     * @param source2 the "source" of Chapter II
     * @param sharedLoop whether the Narrator knows that the player has been here before
     * @param sharedLoopInsist whether the player insisted that they've been here before in the woods
     * @param mirrorComment whether the player asked about the mirror in Chapter II
     * @param touchedMirror whether the player approached the mirror in Chapter II
     * @param isHarsh whether the Princess is currently hostile in Chapters where it varies
     * @param knowsDestiny whether the Princess knows she's (allegedly) going to end the world
     * @param ch2Voice the Voice the player gained at the start of Chapter II
     * @param freeFromChains2 whether the Princess freed herself from her chains in Chapter II
     * @param abandoned2 whether 
     * @param adversaryTookBlade whether or not the player took the blade before entering the basement in Chapter II: The Adversary
     * @param adversaryFaceExplore whether the player asked about their missing face while fighting the Adversary unarmed in Chapter II
     * @param spectrePossessAsk whether the Spectre asked to possess the player in Chapter II
     * @param spectreCantWontAsk whether the player asked the Spectre whether she "couldn't" or "wouldn't" possess them if they refused in Chapter II
     * @param spectreEndSlay whether the player tried to take the Spectre down as she killed them in Chapter II
     * @param prisonerForcedBlade whether the Voice of the Skeptic forced the player to take the blade in Chapter II
     * @param prisonerWatchedHead whether the player chose to watch the Prisoner decapitate herself in Chapter II
     * @param prisonerGoodEndingSeen whether the player saw the Good Ending in Chapter II
     * @param prisonerHeartStopped whether the Voice of the Skeptic stopped the player's heart in Chapter II
     */
    public ChapterIII(ChapterEnding prevEnding, GameManager manager, IOHandler parser, ArrayList<Chapter> route, Condition hasTriedAbort, String source2, boolean sharedLoop, boolean sharedLoopInsist, boolean mirrorComment, boolean touchedMirror, boolean isHarsh, boolean knowsDestiny, Voice ch2Voice, boolean freeFromChains2, boolean abandoned2, boolean adversaryTookBlade, boolean adversaryFaceExplore, boolean spectrePossessAsk, boolean spectreCantWontAsk, boolean spectreEndSlay, boolean prisonerForcedBlade, boolean prisonerWatchedHead, boolean prisonerGoodEndingSeen, boolean prisonerHeartStopped) {
        super(manager, parser, route, hasTriedAbort.check(), prevEnding);

        this.source = source2;
        this.sharedLoop = sharedLoop;
        this.sharedLoopInsist = sharedLoopInsist;
        this.mirrorComment = mirrorComment;
        this.touchedMirror = touchedMirror;
        this.isHarsh = isHarsh;
        this.knowsDestiny = knowsDestiny;
        
        this.prevEnding = prevEnding;
        this.ch2Voice = ch2Voice;
        
        this.abandoned2 = abandoned2;
        this.adversaryTookBlade = adversaryTookBlade;
        this.freeFromChains2 = freeFromChains2;
        this.adversaryFaceExplore = adversaryFaceExplore;
        this.spectrePossessAsk = spectrePossessAsk;
        this.spectreCantWontAsk = spectreCantWontAsk;
        this.spectreEndSlay = spectreEndSlay;
        this.prisonerForcedBlade = prisonerForcedBlade;
        this.prisonerWatchedHead = prisonerWatchedHead;
        this.prisonerGoodEndingSeen = prisonerGoodEndingSeen;
        this.prisonerHeartStopped = prisonerHeartStopped;

        Voice newVoice = prevEnding.getNewVoice();
        this.ch3Voice = newVoice;

        if (this.activeChapter == Chapter.CLARITY) {
            for (Voice v : Voice.TRUEVOICES) {
                this.addVoice(v);
            }
        } else if (newVoice != null) {
            this.addVoice(newVoice);
        }
        
        switch (this.activeChapter) {
            case CAGE:
                this.hasBlade = true;
                this.knowsBlade = true;
                break;

            case ARMSRACE:
                this.knowsBlade = true;
                this.currentLocation = GameLocation.CABIN;
                this.mirrorPresent = true;
                this.addVoice(Voice.HUNTED);
                break;
                
            case NOWAYOUT:
                this.knowsBlade = true;
                this.threwBlade = true;
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
                this.withPrincess = true;
                this.knowsBlade = true;
                this.threwBlade = true;
                this.currentLocation = GameLocation.BASEMENT;
                break;

            case HAPPY:
                this.currentLocation = GameLocation.CABIN;
                this.removeVoice(Voice.SMITTEN);
                break;

            case DRAGON:
                this.withPrincess = true;
                this.currentLocation = GameLocation.BASEMENT;
                this.dragonInPrincess = true;
                this.removeVoice(Voice.NARRATOR);
                this.addVoice(Voice.PRINCESS);
        }
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
     * Accessor for ch3Voice
     * @return the Voice the player gained at the start of Chapter III
     */
    public Voice ch3Voice() {
        return this.ch3Voice;
    }

    /**
     * Accessor for source
     * @return the "source" of the active Chapter
     */
    public String getSource() {
        return this.source;
    }

    /**
     * Accessor for sharedLoop
     * @return whether or not the Narrator knew that the player has been here before in Chapter II
     */
    public boolean sharedLoop() {
        return this.sharedLoop;
    }

    /**
     * Accessor for sharedLoopInsist
     * @return whether or not the player insisted that they've been here before in the woods in Chapter II
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
     * Accessor for abandoned2
     * @return whether or not the player tried to abandon the Spectre or the Nightmare in Chapter II
     */
    public boolean abandoned2() {
        return this.abandoned2;
    }

    /**
     * Accessor for adversaryTookBlade
     * @return whether or not the player took the blade before entering the basement in Chapter II: The Adversary
     */
    public boolean adversaryTookBlade() {
        return this.adversaryTookBlade;
    }

    /**
     * Accessor for freeFromChains2
     * @return whether or not the Adversary broke her chains in Chapter II
     */
    public boolean freeFromChains2() {
        return this.freeFromChains2;
    }

    /**
     * Accessor for adversaryFaceExplore
     * @return whether or not the player asked about their missing face while fighting the Adversary unarmed in Chapter II
     */
    public boolean adversaryFaceExplore() {
        return this.adversaryFaceExplore;
    }

    /**
     * Accessor for spectrePossessAsk
     * @return whether or not the Spectre asked to possess the player in Chapter II
     */
    public boolean spectrePossessAsk() {
        return this.spectrePossessAsk;
    }

    /**
     * Accessor for spectreCantWontAsk
     * @return whether or not the player asked the Spectre whether she "couldn't" or "wouldn't" possess them if they refused in Chapter II
     */
    public boolean spectreCantWontAsk() {
        return this.spectreCantWontAsk;
    }

    /**
     * Accessor for spectreEndSlay
     * @return whether or not the player tried to take the Spectre down as she killed them in Chapter II
     */
    public boolean spectreEndSlay() {
        return this.spectreEndSlay;
    }

    /**
     * Accessor for prisonerForcedBlade
     * @return whether or not the Voice of the Skeptic forced the player to take the blade in Chapter II
     */
    public boolean prisonerForcedBlade() {
        return this.prisonerForcedBlade;
    }

    /**
     * Accessor for prisonerWatchedHead
     * @return whether or not the player chose to watch the Prisoner decapitate herself in Chapter II
     */
    public boolean prisonerWatchedHead() {
        return this.prisonerWatchedHead;
    }

    /**
     * Accessor for prisonerGoodEndingSeen
     * @return whether or not the player saw the Good Ending in Chapter II
     */
    public boolean prisonerGoodEndingSeen() {
        return this.prisonerGoodEndingSeen;
    }

    /**
     * Accessor for prisonerHeartStopped
     * @return whether or not the Voice of the Skeptic stopped the player's heart in Chapter II
     */
    public boolean prisonerHeartStopped() {
        return this.prisonerHeartStopped;
    }

    /**
     * Accessor for cageCutRoute
     * @return whether the player is attempting to cut themselves out of their chains in Chapter III: The Cage
     */
    public boolean cageCutRoute() {
        return this.cageCutRoute;
    }

    // --- COMMANDS ---

    /**
     * Lets the player choose between viewing general content warnings, content warnings by chapter, or content warnings for the current chapter
     */
    @Override
    protected void showWarningsMenu() {
        OptionsMenu warningsMenu = manager.warningsMenu();
        warningsMenu.setCondition("current", this.activeChapter.hasContentWarnings());

        manager.setMetaMenuActive(true);
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

        manager.setMetaMenuActive(false);
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
     * Prints a generic response to a command failing or being unavailable
     * @param outcome the String representation of the outcome of the attempted command
     */
    @Override
    protected void giveDefaultFailResponse(String outcome) {
        if (this.dragonInPrincess && mainScript.getFileName().equals("DragonShared.txt")) { // Double check the file with the responses is loaded
            this.giveDefaultFailResponsePrincess(outcome);
        } else {
            super.giveDefaultFailResponse(outcome);
        }
    }

    // --- CHAPTER MANAGEMENT ---

    /**
     * Initiates the active Chapter 3
     * @return the ending reached by the player
     */
    @Override
    public ChapterEnding runChapter() {
        //System.out.println("Running runChapter with prevEnding " + this.prevEnding);
        //System.out.println("Active chapter is " + this.activeChapter);

        this.unlockChapter();
        manager.updateTracker();
        this.mainScript = new Script(this.manager, this.parser, activeChapter.getScriptFile());
        
        if (!activeChapter.hasSpecialTitle()) this.displayTitleCard();
        
        if (manager.demoMode() && this.activeChapter != Chapter.SPACESBETWEEN) return ChapterEnding.DEMOENDING;

        ChapterEnding ending;
        switch (this.activeChapter) {
            case NEEDLE:
                ending = this.eyeOfNeedle();
                break;
            case FURY:
                ending = this.fury();
                break;
            case APOTHEOSIS:
                ending = this.apotheosis();
                break;
            case DRAGON:
                ending = this.princessAndDragon();
                break;
            case WRAITH:
                ending = this.wraith();
                break;
            case CLARITY:
                ending = this.momentOfClarity();
                break;
            case ARMSRACE:
            case NOWAYOUT:
                ending = this.razor3Intro();
                break;
            case DEN:
                ending = this.den();
                break;
            case WILD:
                ending = this.wild();
                break;
            case THORN:
                ending = this.thorn();
                break;
            case CAGE:
                ending = this.cage();
                break;
            case GREY:
                ending = this.grey();
                break;
            case HAPPY:
                ending = this.happilyEverAfter();
                break;

            // Should only be used in case of debugRunChapter()

            case MUTUALLYASSURED:
            case EMPTYCUP:
                ending = this.razor4();
                break;

            case SPACESBETWEEN:
                //System.out.println("Running Spaces Between");
                ending = this.prevEnding;
                break;

            default: throw new RuntimeException("Cannot run an invalid chapter");
        }

        //System.out.println("Ending: " + ending);
        if (ending == null) return ChapterEnding.DEMOENDING;

        if (ending.hasAchievement()) {
            manager.unlock(ending.getAchievementID());
        }

        manager.updateTracker();
        return ending;
    }

    /**
     * (DEBUG ONLY) Initiates and coordinates a partial cycle, starting from a given Chapter II or Chapter III ending through the player's conversation with the Shifting Mound
     * @return the ending reached by the player
     */
    @Override
    public ChapterEnding debugRunChapter() {
        //System.out.println("Running debugRunChapter");

        // Add the appropriate Chapter 2/3 voice(s)
        switch (this.prevEnding) {

            // Chapter II endings

            case THATWHICHCANNOTDIE:
            case STRIKEMEDOWN:
            case HEARNOBELL:
            case DEADISDEAD:
            case DEADISDEADUPSTAIRS:
            case THREADINGTHROUGH:
            case FREEINGSOMEONE:
                route.add(Chapter.ADVERSARY);
                this.addVoice(Voice.STUBBORN);
                this.ch2Voice = Voice.STUBBORN;
                break;

            case OBEDIENTSERVANT:
            case GODKILLER:
            case APOBLADE:
            case APOUNARMED:
                route.add(Chapter.TOWER);
                this.addVoice(Voice.BROKEN);
                this.ch2Voice = Voice.BROKEN;
                break;

            case HITCHHIKER:
            case HEARTRIPPERLEAVE:
            case HEARTRIPPER:
            case EXORCIST:
                route.add(Chapter.SPECTRE);
                this.addVoice(Voice.COLD);
                this.ch2Voice = Voice.COLD;
                break;

            case WORLDOFTERROR:
            case HOUSEOFNOLEAVE:
            case TERMINALVELOCITY:
            case MONOLITHOFFEAR:
                route.add(Chapter.NIGHTMARE);
                this.addVoice(Voice.PARANOID);
                this.ch2Voice = Voice.PARANOID;
                break;

            case TOARMSRACEFIGHT:
            case TOARMSRACEBORED:
            case TOARMSRACELEFT:
            case TONOWAYOUTBORED:
            case TONOWAYOUTLEFT:
                route.add(Chapter.RAZOR);
                this.addVoice(Voice.CHEATED);
                this.ch2Voice = Voice.CHEATED;
                break;

            case TOMUTUALLYASSURED:
                route.add(Chapter.RAZOR);
                route.add(Chapter.ARMSRACE);
                this.ch2Voice = Voice.CHEATED;
                break;

            case TOEMPTYCUP:
                route.add(Chapter.RAZOR);
                route.add(Chapter.NOWAYOUT);
                this.ch2Voice = Voice.CHEATED;
                break;

            case DISSOLVINGWILL:
            case FIGHT:
            case FLIGHT:
            case OPOSSUM:
            case AHAB:
            case SLAYYOURSELF:
            case DISSOLVED:
                route.add(Chapter.BEAST);
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
                route.add(Chapter.WITCH);
                this.addVoice(Voice.OPPORTUNIST);
                this.ch2Voice = Voice.OPPORTUNIST;
                break;

            case ILLUSIONOFCHOICE:
                route.add(Chapter.STRANGER);
                this.addVoice(Voice.CONTRARIAN);
                this.ch2Voice = Voice.CONTRARIAN;
                break;

            case TALKINGHEADS:
            case PRISONEROFMIND:
            case COLDLYRATIONAL:
            case RESTLESSFORCED:
            case RESTLESSSELF:
            case RESTLESSGIVEIN:
                route.add(Chapter.PRISONER);
                this.addVoice(Voice.SKEPTIC);
                this.ch2Voice = Voice.SKEPTIC;
                break;

            case ROMANTICHAZE:
            case ANDTHEYLIVEDHAPPILY:
            case LADYKILLER:
                this.addVoice(Voice.SMITTEN);
            case CONTENTSOFOURHEARTDECON:
            case CONTENTSOFOURHEARTUPSTAIRS:
                route.add(Chapter.DAMSEL);
                this.ch2Voice = Voice.SMITTEN;
                break;

            // Chapter III endings

            // The Eye of the Needle
            case FAILEDFIGHT: // assume Skeptic
            case BLINDLEADINGBLIND: // assume Skeptic
                route.add(Chapter.ADVERSARY);
                route.add(Chapter.NEEDLE);
                this.addVoice(Voice.STUBBORN);
                this.addVoice(Voice.SKEPTIC);
                break;
            case FAILEDFLEE: // assume Hunted
            case WIDEOPENFIELD:
                route.add(Chapter.ADVERSARY);
                route.add(Chapter.NEEDLE);
                this.addVoice(Voice.STUBBORN);
                this.addVoice(Voice.HUNTED);
                break;

            // The Fury
            case QUANTUMBEAK: // Broken has been replaced with Cold by now
                route.add(Chapter.TOWER);
                route.add(Chapter.FURY);
                this.addVoice(Voice.COLD);
                this.addVoice(Voice.STUBBORN);
                break;
            case HINTOFFEELING:
            case LEAVEHERBEHIND: // assume Cold
                route.add(Chapter.ADVERSARY);
                route.add(Chapter.FURY);
                this.addVoice(Voice.STUBBORN);
                this.addVoice(Voice.COLD);
                this.removeVoice(Voice.HERO);
                break;
            case NEWLEAFWEATHEREDBOOK: // assume Broken
                route.add(Chapter.ADVERSARY);
                route.add(Chapter.FURY);
                this.addVoice(Voice.STUBBORN);
                this.addVoice(Voice.BROKEN);
                this.removeVoice(Voice.HERO);
                break;
            case GOINGTHEDISTANCE:
                route.add(Chapter.ADVERSARY);
                route.add(Chapter.FURY);
                this.addVoice(Voice.STUBBORN);
                this.addVoice(Voice.CONTRARIAN);
                break;
            case IFYOUCOULDUNDERSTAND: // assume from Tower; all Voices gone by now
                route.add(Chapter.TOWER);
                route.add(Chapter.FURY);
                this.clearVoices();

            // The Apotheosis
            case WINDOWTOUNKNOWN:
            case GRACE: // assume Contrarian
            case SOMETHINGTOREMEMBER:
                route.add(Chapter.TOWER);
                route.add(Chapter.APOTHEOSIS);
                this.addVoice(Voice.BROKEN);
                this.addVoice(Voice.CONTRARIAN);
                break;
            case GODDESSUNRAVELED:
                route.add(Chapter.TOWER);
                route.add(Chapter.APOTHEOSIS);
                this.addVoice(Voice.BROKEN);
                this.addVoice(Voice.PARANOID);
                break;

            // The Princess and the Dragon
            case WHATONCEWASONE:
            case PRINCESSANDDRAGON:
            case OPPORTUNISTATHEART:
                route.add(Chapter.SPECTRE);
                route.add(Chapter.DRAGON);
                this.addVoice(Voice.COLD);
                this.addVoice(Voice.OPPORTUNIST);
                break;

            // The Wraith
            case PASSENGER: // assume Opportunist
                route.add(Chapter.NIGHTMARE);
                route.add(Chapter.WRAITH);
                this.addVoice(Voice.PARANOID);
                this.addVoice(Voice.OPPORTUNIST);
                break;
            case EXORCISTIII: // assume Spectre-Paranoid
                route.add(Chapter.SPECTRE);
                route.add(Chapter.WRAITH);
                this.addVoice(Voice.COLD);
                this.addVoice(Voice.PARANOID);
                break;

            // The Moment of Clarity
            case MOMENTOFCLARITY:
                route.add(Chapter.NIGHTMARE);
                route.add(Chapter.CLARITY);
                for (Voice v : Voice.TRUEVOICES) {
                    this.addVoice(v);
                }
                break;

            // Mutually Assured Destruction / The Empty Cup
            case WATERSTEEL:
                route.add(Chapter.RAZOR);
                route.add(Chapter.ARMSRACE);
                route.add(Chapter.MUTUALLYASSURED);
                for (Voice v : Voice.TRUEVOICES) {
                    this.addVoice(v);
                }
                break;
            case FORMLESS:
                route.add(Chapter.RAZOR);
                route.add(Chapter.NOWAYOUT);
                route.add(Chapter.EMPTYCUP);
                for (Voice v : Voice.TRUEVOICES) {
                    this.addVoice(v);
                }
                break;

            // The Den
            case UNANSWEREDQUESTIONS: // assume Stubborn
            case HEROICSTRIKE:
            case INSTINCT:
                route.add(Chapter.BEAST);
                route.add(Chapter.DEN);
                this.addVoice(Voice.HUNTED);
                this.addVoice(Voice.STUBBORN);
                break;
            case HUNGERPANGS: // assume Skeptic
            case COUPDEGRACE:
            case LIONANDMOUSE:
                route.add(Chapter.BEAST);
                route.add(Chapter.DEN);
                this.addVoice(Voice.HUNTED);
                this.addVoice(Voice.SKEPTIC);
                break;

            // The Wild
            case GLIMPSEOFSOMETHING: // assume Beast-Broken
                route.add(Chapter.BEAST);
                route.add(Chapter.WILD);
                this.addVoice(Voice.HUNTED);
                this.addVoice(Voice.BROKEN);
                break;
            case WOUNDSAVE:
            case WOUNDSLAY: // assume Witch-Cheated
                route.add(Chapter.WITCH);
                route.add(Chapter.WILD);
                this.addVoice(Voice.OPPORTUNIST);
                this.addVoice(Voice.CHEATED);
                break;

            // The Thorn
            case TRUSTISSUES:
            case TRUSTISSUESSLAY:
            case ABANDONMENT:
            case NEWLEAF: // assume Cheated
                route.add(Chapter.WITCH);
                route.add(Chapter.THORN);
                this.addVoice(Voice.OPPORTUNIST);
                this.addVoice(Voice.CHEATED);
                break;
            case NEWLEAFKISS:
                route.add(Chapter.WITCH);
                route.add(Chapter.THORN);
                this.addVoice(Voice.OPPORTUNIST);
                this.addVoice(Voice.SMITTEN);
                break;

            // The Cage
            case NOEXIT:
            case FREEWILL: // assume Cheated
                route.add(Chapter.PRISONER);
                route.add(Chapter.CAGE);
                this.addVoice(Voice.SKEPTIC);
                this.addVoice(Voice.CHEATED);
                break;
            case RIDDLEOFSTEEL:
                route.add(Chapter.PRISONER);
                route.add(Chapter.CAGE);
                this.addVoice(Voice.SKEPTIC);
                this.addVoice(Voice.PARANOID);
                break;
            case ALLEGORYOFCAGE: // assume Broken
                route.add(Chapter.PRISONER);
                route.add(Chapter.CAGE);
                this.addVoice(Voice.SKEPTIC);
                this.addVoice(Voice.BROKEN);
                break;

            // The Grey
            case BURNINGDOWNTHEHOUSE:
                route.add(Chapter.DAMSEL);
                route.add(Chapter.GREY);
                this.addVoice(Voice.SMITTEN);
                this.addVoice(Voice.COLD);
                break;
            case ANDALLTHISLONGING:
                route.add(Chapter.PRISONER);
                route.add(Chapter.GREY);
                this.addVoice(Voice.SKEPTIC);
                this.addVoice(Voice.COLD);
                break;

            // Happily Ever After
            case IMEANTIT:
            case LEFTCABIN: // assume Skeptic
                route.add(Chapter.DAMSEL);
                route.add(Chapter.HAPPY);
                this.addVoice(Voice.SKEPTIC);
                break;
            case FINALLYOVER:
            case DONTLETITGOOUT: // assume Opportunist
                route.add(Chapter.DAMSEL);
                route.add(Chapter.HAPPY);
                this.addVoice(Voice.OPPORTUNIST);
                break;
        }

        ChapterEnding ending = this.runChapter();
        //System.out.println("Debug ending: " + ending);

        switch (ending) {
            case ABORTED:
            case GOODENDING:
            case DEMOENDING: break;

            default:
                manager.updateMoundValues(ending.getFreedom(), ending.getSatisfaction());
                this.mirrorSequence(ending);
        }

        return ending;
    }

    // --- CHAPTERS & SCENES ---

    // - Chapter III: The Eye of the Needle -

    /**
     * Runs Chapter III: The Eye of the Needle
     * @return the ending reached by the player
     */
    private ChapterEnding eyeOfNeedle() {
        /*
          Possible combinations:
            - Stubborn + Hunted
            - Stubborn + Skeptic
         */

        mainScript.runSection();

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "different", "(Explore) We haven't talked enough about how different this place is. I wouldn't even call these woods. It's like her influence has poured out into the world."));
        activeMenu.add(new Option(this.manager, "plan", "(Explore) What if I don't agree with the plan? What if I'd rather do something else?"));
        activeMenu.add(new Option(this.manager, "proceedA", "No matter what happens next, it seems like all our answers are in the cabin. Let's see this through. [Proceed to the cabin.]"));
        activeMenu.add(new Option(this.manager, "proceedB", "[Silently proceed to the cabin.]"));
        activeMenu.add(new Option(this.manager, "abort", this.cantTryAbort, "I'm done with this. Bye! [Turn around and leave.]", 0));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "different":
                case "plan":
                    mainScript.runSection(activeOutcome + "Woods");
                    break;

                case "cGoHill":
                case "proceedA":
                case "proceedB":
                    this.repeatActiveMenu = false;
                    break;

                case "cGoLeave":
                    if (cantTryAbort.check()) {
                        parser.printDialogueLine("You have already tried that.");
                        break;
                    }
                case "abort":
                    if (!this.canAbort) {
                        cantTryAbort.set();
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }

                    mainScript.runSection("abort");
                    this.abortVessel(true);
                    return ChapterEnding.ABORTED;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Proceed to the cabin
        this.currentLocation = GameLocation.HILL;
        mainScript.runSection("hillDialogue");

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "proceed", "[Proceed into the cabin.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "cGoCabin":
                case "proceed":
                    this.repeatActiveMenu = false;
                    break;
                
                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Enter the cabin
        this.currentLocation = GameLocation.CABIN;
        this.knowsBlade = true;
        this.withBlade = true;
        this.mirrorPresent = true;
        mainScript.runSection("cabinIntro");

        Condition currentMirrorComment = new Condition(this.hasVoice(Voice.SKEPTIC) && this.touchedMirror);
        InverseCondition noCurrentMirrorComment = currentMirrorComment.getInverse();
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "mirrorA", "(Explore) You never mention the mirror.", this.mirrorComment, noCurrentMirrorComment));
        activeMenu.add(new Option(this.manager, "mirrorB", "(Explore) That damn mirror's back.", this.mirrorComment, noCurrentMirrorComment));
        activeMenu.add(new Option(this.manager, "mirrorC", "(Explore) But there's no way forward. There's a mirror at the end of the room and that's it.", !this.mirrorComment, noCurrentMirrorComment));
        activeMenu.add(new Option(this.manager, "take", "(Explore) [Take the blade.]"));
        activeMenu.add(new Option(this.manager, "approach", "[Approach the mirror.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "mirrorA":
                case "mirrorB":
                case "mirrorC":
                    currentMirrorComment.set();
                    mainScript.runSection("mirrorAsk");
                    break;

                case "cTakeBlade":
                    activeMenu.setCondition("take", false);
                case "take":
                    this.hasBlade = true;
                    this.withBlade = false;
                    mainScript.runSection("takeBlade");
                    break;

                case "cGoStairs":
                case "cApproachMirror":
                case "approach":
                    this.repeatActiveMenu = false;
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Approach the mirror
        this.currentLocation = GameLocation.MIRROR;
        mainScript.runConditionalSection("approachMirror", currentMirrorComment);

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "wipe", "[Wipe the mirror clean.]"));
        activeMenu.add(new Option(this.manager, "smash", "[Smash it.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "cWipe":
                case "wipe":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("wipeMirror");
                    break;

                case "cSmash":
                case "smash":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("smashMirror");
                    break;

                case "cApproachAtMirrorFail":
                    this.giveDefaultFailResponse("cApproachAtMirrorFail");
                    break;

                default: super.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Enter the basement
        this.currentLocation = GameLocation.STAIRS;
        this.withBlade = false;
        this.mirrorPresent = false;
        this.touchedMirror = true;

        Condition noStairsExplore = new Condition(true);
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "taunt", "(Explore) \"If you want a fight then how about you come and get one?\"", noStairsExplore));
        activeMenu.add(new Option(this.manager, "free", "(Explore) \"I know you can get out of there on your own.\"", this.freeFromChains2, noStairsExplore));
        activeMenu.add(new Option(this.manager, "sure", "(Explore) \"Are you sure you still want to fight?\"", noStairsExplore));
        activeMenu.add(new Option(this.manager, "cont", "[Continue to the basement landing.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "taunt":
                case "free":
                case "sure":
                    noStairsExplore.set(false);
                    mainScript.runSection(activeOutcome + "Stairs");
                    break;

                case "cGoBasement":
                case "cont":
                    this.repeatActiveMenu = false;
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Continue to the basement landing
        this.currentLocation = GameLocation.BASEMENT;
        this.withPrincess = true;
        mainScript.runSection("basementStart");

        if (this.hasBlade) {
            this.canSlayPrincess = true;
            this.activeMenu = new OptionsMenu();
            activeMenu.add(new Option(this.manager, "talk", "(Explore) \"Let's talk a bit first. We can always fight when we're done, but I have questions. I want to know what happened after you killed me.\""));
            activeMenu.add(new Option(this.manager, "taunt", "(Explore) \"Not down here. If you want me, you'll have to come and get me.\""));
            activeMenu.add(new Option(this.manager, "space", "(Explore) \"If you want a good fight, if you *really* want to see me at my best, we need somewhere with more space.\""));
            activeMenu.add(new Option(this.manager, "free", "(Explore) \"Why don't you want to be free? Why do you insist on fighting me to the death down here in the dark?\""));
            activeMenu.add(new Option(this.manager, "pacifism", "(Explore) \"I'm not fighting you.\""));
            activeMenu.add(new Option(this.manager, "closer", "[Step closer, stopping just outside her chains.]"));
            activeMenu.add(new Option(this.manager, "slay", "[Slay the Princess.]"));

            this.repeatActiveMenu = true;
            while (repeatActiveMenu) {
                this.activeOutcome = parser.promptOptionsMenu(activeMenu);
                switch (activeOutcome) {
                    case "talk":
                    case "free":
                    case "closer":
                        this.repeatActiveMenu = false;
                        mainScript.runSection(activeOutcome + "Basement");
                        break;

                    case "taunt":
                    case "space":
                    case "pacifism":
                        this.repeatActiveMenu = false;
                        mainScript.runSection("commonBasement");
                        break;

                    case "cSlayPrincess":
                    case "slay":
                        mainScript.runSection("basementSlay");
                        return ChapterEnding.FAILEDFIGHT;

                    case "cGoStairs":
                        mainScript.runSection("basementLeaveAttempt");
                        break;

                    default: this.giveDefaultFailResponse(activeOutcome);
                }
            }
        }

        // Get her to chase you out of the basement
        this.currentLocation = GameLocation.CABIN;
        mainScript.runSection("basementLoose");

        if (!this.hasBlade) {
            this.withBlade = true;
            this.activeMenu = new OptionsMenu();
            activeMenu.add(new Option(this.manager, "take", "[Take the blade.]"));
            activeMenu.add(new Option(this.manager, "leave", "[It's too late. Run for the door.]"));

            this.repeatActiveMenu = true;
            while (repeatActiveMenu) {
                this.activeOutcome = parser.promptOptionsMenu(activeMenu);
                switch (activeOutcome) {
                    case "cTakeBlade":
                    case "take":
                        this.hasBlade = true;
                        mainScript.runSection(this.ch3Voice + "ChaseTake");

                        switch (ch3Voice) {
                            case HUNTED: return this.eyeOfNeedleCombat();
                            default: return ChapterEnding.FAILEDFLEE;
                        }

                    case "cGoHill":
                    case "leave":
                        if (this.ch3Voice == Voice.HUNTED) {
                            mainScript.runSection("huntedChaseLeave");
                            return ChapterEnding.FAILEDFLEE;
                        }

                        this.repeatActiveMenu = false;
                        break;

                    case "cGoStairs":
                        mainScript.runSection("chaseTurnAround");
                        break;

                    default: this.giveDefaultFailResponse(activeOutcome);
                }
            }

            this.withBlade = false;
        }

        if (this.ch3Voice == Voice.SKEPTIC) {
            mainScript.runSection("skepticChaseLeave");

            this.activeMenu = new OptionsMenu();
            activeMenu.add(new Option(this.manager, "free", "[Free her.]"));
            activeMenu.add(new Option(this.manager, "die", "[Die.]"));

            this.repeatActiveMenu = true;
            while (repeatActiveMenu) {
                switch (parser.promptOptionsMenu(activeMenu)) {
                    case "cGoHill":
                    case "free":
                        this.eyeOfNeedleFree();
                        return ChapterEnding.BLINDLEADINGBLIND;

                    case "die":
                        mainScript.runSection("choiceDie");
                        return ChapterEnding.FAILEDFLEE;

                    case "cGoStairs":
                        mainScript.runSection("chaseTurnAround");
                        break;

                    default: this.giveDefaultFailResponse();
                }
            }

            throw new RuntimeException("No ending reached");
        } else {
            return this.eyeOfNeedleCombat();
        }
    }

    /**
     * The player successfully leads the Eye of the Needle out of the cabin with the Voice of the Hunted
     * @return the ending reached by the player
     */
    private ChapterEnding eyeOfNeedleCombat() {
        this.currentLocation = GameLocation.HILL;
        mainScript.runSection("combatStart");

        this.canSlayPrincess = true;
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "trap", "(Explore) \"You've fallen right into my trap.\""));
        activeMenu.add(new Option(this.manager, "free", "\"Are you sure you want to do this? You're free now, and out here I have the upper hand. But we don't have to fight.\""));
        activeMenu.add(new Option(this.manager, "slay", "[Slay the Princess.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            if (activeOutcome.equals("cSlayPrincess")) this.activeOutcome = "slay";

            switch (activeOutcome) {
                case "free":
                    mainScript.runSection("freeCombat");
                    return ChapterEnding.BLINDLEADINGBLIND;

                case "trap":
                case "slay":
                    mainScript.runSection(activeOutcome + "Combat");
                    return ChapterEnding.WIDEOPENFIELD;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        throw new RuntimeException("No ending reached");
    }

    /**
     * The player successfully leads the Eye of the Needle out of the cabin with the Voice of the Skeptic
     */
    private void eyeOfNeedleFree() {
        mainScript.runSection("freeStart");

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "freeA", "\"It's freedom.\""));
        activeMenu.add(new Option(this.manager, "freeB", "\"Don't you get it? Everything you've known down there has been a lie. Whatever we find out here is the truth. It's the only way for us to be free.\""));
        activeMenu.add(new Option(this.manager, "otherA", "\"I don't know. But I was hoping we could find out together.\""));
        activeMenu.add(new Option(this.manager, "otherB", "\"What do you mean, 'this doesn't feel right?'\""));
        activeMenu.add(new Option(this.manager, "otherC", "[Say nothing.]"));

        switch (parser.promptOptionsMenu(activeMenu)) {
            case "freeA":
            case "freeB":
                mainScript.runSection("freeFreeComment");
                break;

            default: mainScript.runSection("freeOther");
        }
    }


    // - Chapter III: The Fury -

    /**
     * Runs Chapter III: The Fury
     * @return the ending reached by the player
     */
    private ChapterEnding fury() {
        /*
          Possible combinations:
            - Broken + Stubborn (either Chapter)
            - Stubborn + Cold (Adversary)
            - Stubborn + Contrarian (Adversary)
         */

        boolean foughtUnarmed = false;
        switch (this.prevEnding) {
            case STRIKEMEDOWN:
                this.secondaryScript = new Script(this.manager, this.parser, "Routes/JOINT/Fury/FuryAdversary");
                this.source = "pacifism";
                break;

            case HEARNOBELL:
                this.secondaryScript = new Script(this.manager, this.parser, "Routes/JOINT/Fury/FuryAdversary");
                this.source = "unarmed";
                foughtUnarmed = true;
                break;

            case DEADISDEADUNARMED:
                foughtUnarmed = true;
            case DEADISDEAD:
                this.secondaryScript = new Script(this.manager, this.parser, "Routes/JOINT/Fury/FuryAdversary");
                this.source = "pathetic";
                break;

            case DEADISDEADUPSTAIRS:
                this.secondaryScript = new Script(this.manager, this.parser, "Routes/JOINT/Fury/FuryAdversary");
                this.source = "upstairs";
                break;

            default:
                this.secondaryScript = new Script(this.manager, this.parser, "Routes/JOINT/Fury/FuryTower");
                this.source = "tower";
        }

        String voiceCombo;
        if (this.prevEnding == ChapterEnding.GODKILLER) {
            voiceCombo = "tower";
            mainScript.runSection("towerStart");
        } else {
            voiceCombo = ch3Voice.toString();
            mainScript.runSection("advyStart");
        }

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "pulled", "(Explore) It feels like I'm being pulled in a hundred different directions. You'd better all listen to me when the time comes to make a choice."));
        activeMenu.add(new Option(this.manager, "noReturn", "(Explore) If we hit this \"point of no return\" you mentioned... then what happens?"));
        activeMenu.add(new Option(this.manager, "proceedA", "No matter what happens next, it seems like all our answers are in the cabin. Let's see this through. [Proceed to the cabin.]"));
        activeMenu.add(new Option(this.manager, "proceedB", "[Silently proceed to the cabin.]"));
        activeMenu.add(new Option(this.manager, "abort", this.cantTryAbort, "I'm done with this. Bye! [Turn around and leave.]", 0));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "pulled":
                    mainScript.runSection(voiceCombo + "PulledWoods");
                    break;

                case "noReturn":
                    mainScript.runSection("noReturnWoods");
                    break;

                case "cGoHill":
                case "proceedA":
                case "proceedB":
                    this.repeatActiveMenu = false;
                    break;

                case "cGoLeave":
                    if (cantTryAbort.check()) {
                        parser.printDialogueLine("You have already tried that.");
                        break;
                    }
                case "abort":
                    if (!this.canAbort) {
                        cantTryAbort.set();
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }

                    mainScript.runSection("abort");
                    this.abortVessel(true);
                    return ChapterEnding.ABORTED;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Proceed to the cabin
        this.currentLocation = GameLocation.HILL;
        mainScript.runSection("hillDialogue");

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "proceed", "[Proceed into the cabin.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "cGoCabin":
                case "proceed":
                    this.repeatActiveMenu = false;
                    break;
                
                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Enter the cabin
        this.currentLocation = GameLocation.CABIN;
        this.knowsBlade = true;
        this.withBlade = true;
        this.mirrorPresent = true;
        mainScript.runSection("cabinIntro");

        Condition currentMirrorComment = new Condition();
        InverseCondition noCurrentMirrorComment = currentMirrorComment.getInverse();
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "mirrorA", "(Explore) It's that mirror again. And this time it's blocking the door.", this.mirrorComment, noCurrentMirrorComment));
        activeMenu.add(new Option(this.manager, "mirrorB", "(Explore) You didn't mention the mirror last time either. Why?", this.mirrorComment, noCurrentMirrorComment));
        activeMenu.add(new Option(this.manager, "mirrorC", "(Explore) How are we supposed to get to the basement? There's no door. There's just a mirror where the door used to be.", !this.mirrorComment, noCurrentMirrorComment));
        activeMenu.add(new Option(this.manager, "take", "(Explore) [Take the blade.]"));
        activeMenu.add(new Option(this.manager, "approach", "[Approach the mirror.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "mirrorA":
                case "mirrorB":
                case "mirrorC":
                    currentMirrorComment.set();
                    mainScript.runSection("mirrorAsk");
                    break;

                case "cTakeBlade":
                    activeMenu.setCondition("take", false);
                case "take":
                    this.withBlade = false;
                    mainScript.runSection("takeBlade");

                    if (this.hasVoice(Voice.CONTRARIAN)) {
                        this.threwBlade = true;
                        this.subMenu = new OptionsMenu(true);
                        subMenu.add(new Option(this.manager, "no", "No. We absolutely did not throw the blade out the window."));
                        subMenu.add(new Option(this.manager, "decisions", "Hey, I thought I was the one who made the decisions around here."));
                        subMenu.add(new Option(this.manager, "override", "So, what, you can all just override me now?"));
                        subMenu.add(new Option(this.manager, "hellYeah", "Hell yeah, that kicked ass. Love when my tenants throw things out of windows when I'm not paying attention."));
                        subMenu.add(new Option(this.manager, "silent", "[Let it go.]"));

                        this.activeOutcome = parser.promptOptionsMenu(subMenu);
                        switch (activeOutcome) {
                            case "no":
                            case "decisions":
                            case "override":
                                mainScript.runSection(activeOutcome + "Throw");
                                break;

                            default: mainScript.runSection("throwJoin");
                        }
                    } else {
                        this.hasBlade = true;
                    }

                    break;

                case "cGoStairs":
                case "cApproachMirror":
                case "approach":
                    this.repeatActiveMenu = false;
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Approach the mirror
        this.currentLocation = GameLocation.MIRROR;
        mainScript.runConditionalSection("approachMirror", currentMirrorComment);

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "wipe", "[Wipe the mirror clean.]"));
        activeMenu.add(new Option(this.manager, "smash", "[Smash it.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "cWipe":
                case "wipe":
                    this.repeatActiveMenu = false;
                    mainScript.runConditionalSection("wipeMirror", foughtUnarmed, voiceCombo);
                    break;

                case "cSmash":
                case "smash":
                    this.repeatActiveMenu = false;
                    mainScript.runConditionalSection("smashMirror", foughtUnarmed, voiceCombo);
                    break;

                case "cApproachAtMirrorFail":
                    this.giveDefaultFailResponse("cApproachAtMirrorFail");
                    break;

                default: super.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Enter the basement
        this.currentLocation = GameLocation.STAIRS;
        this.withBlade = false;
        this.mirrorPresent = false;
        this.touchedMirror = true;

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "cont", "[Continue down the stairs.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "cGoBasement":
                case "cont":
                    this.repeatActiveMenu = false;
                    break;

                default: this.giveDefaultFailResponse();
            }
        }

        // Continue down the stairs
        this.currentLocation = GameLocation.BASEMENT;
        this.withPrincess = true;
        this.canSlayPrincess = true;
        mainScript.runConditionalSection("basementStart", voiceCombo);

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "taunt", "\"Given up on destroying the world, have we?\"", source.equals("tower") && this.knowsDestiny));
        activeMenu.add(new Option(this.manager, "happened", "\"What happened to you?\""));
        activeMenu.add(new Option(this.manager, "wait", "\"Wait! We don't have to do this!\""));
        activeMenu.add(new Option(this.manager, "sorry", "\"I'm sorry, okay? I'm sorry for what I did to you!\""));
        activeMenu.add(new Option(this.manager, "listen", "\"Please just listen to me!\""));
        activeMenu.add(new Option(this.manager, "round3A", "\"This isn't over until I say it's over. I'm taking you out.\" [Round three.]", source.equals("unarmed")));
        activeMenu.add(new Option(this.manager, "slayA", "\"I'm going to end you.\" [Slay the Princess.]", this.hasBlade && !source.equals("pacifism")));
        activeMenu.add(new Option(this.manager, "slayB", "[Slay the Princess.]", this.hasBlade && !source.equals("pacifism")));
        activeMenu.add(new Option(this.manager, "round3B", "[Round three.]", source.equals("unarmed")));
        activeMenu.add(new Option(this.manager, "back", "[Fight back.]", (!this.hasBlade && !source.equals("unarmed") || (this.hasBlade && source.equals("pacifism")))));
        activeMenu.add(new Option(this.manager, "let", "[Let her end you.]"));

        boolean earlyApology = false;
        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);

            // Redirect slay command to the appropriate options
            if (activeOutcome.equals("cSlayPrincess")) {
                if (source.equals("pacifism")) {
                    this.activeOutcome = "back";
                } else {
                    this.activeOutcome = "slayB";
                }
            } else if (activeOutcome.equals("cSlayPrincessNoBladeFail")) {
                if (source.equals("unarmed")) {
                    this.activeOutcome = "round3B";
                } else {
                    this.activeOutcome = "back";
                }
            }

            switch (activeOutcome) {
                case "sorry":
                    earlyApology = true;
                case "taunt":
                case "happened":
                case "wait":
                case "listen":
                    this.repeatActiveMenu = false;
                    mainScript.runSection(activeOutcome + "Basement");
                    break;

                case "round3A":
                case "slayA":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("chargeTalkBasement");
                    break;

                case "round3B":
                case "slayB":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("chargeBasement");
                    break;

                case "back":
                    this.repeatActiveMenu = false;

                    if (this.hasBlade) {
                        mainScript.runSection("noFightBasement");
                    } else {
                        mainScript.runSection("fightBackBasement");
                    }
                    
                    break;

                case "let":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("letEndBasement");
                    this.hasBlade = false;
                    break;

                default: this.giveDefaultFailResponse();
            }
        }

        // You are unwound
        if (!source.equals("tower")) this.hasBlade = false;
        mainScript.runConditionalSection("unwoundStart", voiceCombo);

        switch (voiceCombo) {
            case "tower":
                if (this.hasBlade) {
                    return this.furyTower(earlyApology);
                } else {
                    return this.furyUnwinding();
                }

            case "contrarian": return this.furyContra();
        }
        
        // With the Voice of the Cold or Broken (from the Adversary)
        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "talkA", "\"And what am I supposed to see?\""));
        activeMenu.add(new Option(this.manager, "talkB", "\"You're a monster.\""));
        activeMenu.add(new Option(this.manager, "talkC", "\"Please, just talk to me! Can we not use words to settle our differences? We're both here for a reason. It's more important for us to figure that out than to fight.\""));
        activeMenu.add(new Option(this.manager, "talkD", "\"You won't win.\""));
        activeMenu.add(new Option(this.manager, "die", "[Attempt to die.]"));
        activeMenu.add(new Option(this.manager, "silent", "[Say nothing.]"));

        this.activeOutcome = parser.promptOptionsMenu(activeMenu);
        switch (activeOutcome) {
            case "die":
            case "silent":
                mainScript.runSection(activeOutcome + "ColdBroken");
                break;

            default: mainScript.runSection("talkAttemptGeneric");
        }

        return this.furyUnwinding();
    }

    /**
     * The player is unwound with the Voice of the Contrarian, pushing onward to attack the Fury unarmed
     * @return the ending reached by the player
     */
    private ChapterEnding furyContra() {
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "talkA", "(Explore) \"Numb?! You ripped your own skin off!\""));
        activeMenu.add(new Option(this.manager, "talkB", "(Explore) \"If you want a challenge, how about you play fair?\""));
        activeMenu.add(new Option(this.manager, "talkC", "(Explore) \"Do you think this is enough to stop me?\""));
        activeMenu.add(new Option(this.manager, "talkD", "(Explore) \"What happened to you? You're so much colder than you were before.\""));
        activeMenu.add(new Option(this.manager, "giveUp", "[Give up.]"));
        activeMenu.add(new Option(this.manager, "push", "[Ignore her and push forward.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu, true)) {
                case "cSlayPrincessNoBladeFail":
                case "cProceed":
                case "push":
                    this.repeatActiveMenu = false;
                    break;

                case "talkA":
                case "talkB":
                case "talkC":
                case "talkD":
                    mainScript.runSection("talkAttemptGeneric");
                    return this.furyUnwinding();

                case "giveUp":
                    mainScript.runSection("contraGiveUp1");
                    return this.furyUnwinding();

                default: this.giveDefaultFailResponse();
            }
        }

        // Ignore her and push forward
        mainScript.runSection("contraPush1");

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "explore", "(Explore) Guys, I'm not sure we've got this one."));
        activeMenu.add(new Option(this.manager, "giveUp", "[Give up.]"));
        activeMenu.add(new Option(this.manager, "push", "[Take another step.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu, true)) {
                case "cSlayPrincessNoBladeFail":
                case "cProceed":
                case "push":
                    this.repeatActiveMenu = false;
                    break;

                case "explore":
                case "giveUp":
                    mainScript.runSection("contraGiveUp2");
                    return this.furyUnwinding();

                default: this.giveDefaultFailResponse();
            }
        }

        // Take another step
        mainScript.runSection("contraPush2");

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "explore", "(Explore) \"Mr. Anatomy\" has a point. How *can* we move without any muscle?"));
        activeMenu.add(new Option(this.manager, "giveUp", "[You don't have any muscle. Give up.]"));
        activeMenu.add(new Option(this.manager, "push", "[Take another step.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu, true)) {
                case "cSlayPrincessNoBladeFail":
                case "cProceed":
                case "push":
                    this.repeatActiveMenu = false;
                    break;

                case "explore":
                case "giveUp":
                    mainScript.runSection("contraGiveUp3");
                    return this.furyUnwinding();

                default: this.giveDefaultFailResponse();
            }
        }

        // Take another step
        mainScript.runSection("contraPush3");

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "explore", "(Explore) This is too much. It isn't going to work. Wouldn't it be better to just start over and try again?"));
        activeMenu.add(new Option(this.manager, "giveUp", "[You've done enough. It's time to die.]"));
        activeMenu.add(new Option(this.manager, "heart", "[Reach for her heart.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu, true)) {
                case "cSlayPrincessNoBladeFail":
                case "cProceed":
                case "heart":
                    this.repeatActiveMenu = false;
                    break;

                case "explore":
                    mainScript.runSection("contraGiveUp3");
                    return this.furyUnwinding();

                case "giveUp":
                    mainScript.runSection("contraGiveUp4");
                    return this.furyUnwinding();

                default: this.giveDefaultFailResponse();
            }
        }

        // Reach for her heart
        mainScript.runSection("contraHeart");
        this.removeVoice(Voice.NARRATOR);

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "slay", "[Slay the Princess.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "cSlayPrincessNoBladeFail":
                case "slay":
                    this.repeatActiveMenu = false;
                    break;

                default: super.giveDefaultFailResponse();
            }
        }

        // Slay the Princess
        mainScript.runSection();
        return ChapterEnding.GOINGTHEDISTANCE;
    }

    /**
     * The player is unwound with the Voice of the Broken (from the Tower)
     * @param earlyApology whether the player apologized to the Fury at the bottom of the stairs
     * @return the ending reached by the player
     */
    private ChapterEnding furyTower(boolean earlyApology) {
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "insides", "(Explore) \"You damned cheater! Give me my insides back!\""));
        activeMenu.add(new Option(this.manager, "threat", "(Explore) \"I killed you last time, I swear on everything that I'm going to find a way to kill you again!\""));
        activeMenu.add(new Option(this.manager, "talkA", "(Explore) \"I only killed you last time because you were going to end the world! We can talk ourselves back from this ledge.\""));
        activeMenu.add(new Option(this.manager, "talkB", "(Explore) \"I'm sorry for what I did to you, but you did horrible things to me, too.\""));
        activeMenu.add(new Option(this.manager, "talkC", "(Explore) \"Where is all of this supposed to end? We can't just keep killing each other forever.\""));
        activeMenu.add(new Option(this.manager, "engage", "[This is no time for words. Step forward and engage your enemy.]"));

        String firstTalkAttempt = "";
        int submitCount = 0;
        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "insides":
                case "threat":
                    this.repeatActiveMenu = false;
                    firstTalkAttempt = this.activeOutcome;
                    mainScript.runSection("towerTalkFail");
                    break;

                case "talkA":
                case "talkB":
                    submitCount += 1;
                case "talkC":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("towerTalkFail");
                    break;

                case "cSlayPrincess":
                case "engage":
                    return this.furyQuantum(false);

                default: this.giveDefaultFailResponse();
            }
        }

        // Attempt to talk
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "sorry", "(Explore) \"I'm sorry, okay? I'm sorry for what I did!\""));
        activeMenu.add(new Option(this.manager, "insides", "(Explore) \"I said, give me my insides back! ... Please?\"", firstTalkAttempt.equals("insides")));
        activeMenu.add(new Option(this.manager, "threat", "(Explore) \"I said, I'm going to kill you!\"", firstTalkAttempt.equals("threat")));
        activeMenu.add(new Option(this.manager, "stop", "(Explore) \"We have to stop hurting each other!\""));
        activeMenu.add(new Option(this.manager, "endGame", "(Explore) \"What's your end game here? Are we supposed to keep torturing each other forever?\""));
        activeMenu.add(new Option(this.manager, "engage", "[This is no time for words. Step forward and engage your enemy.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "sorry":
                case "stop":
                    this.repeatActiveMenu = false;
                    submitCount += 2;
                    mainScript.runConditionalSection(activeOutcome + "Tower", earlyApology);
                    break;

                case "insides":
                    this.repeatActiveMenu = false;
                    submitCount += 1;
                    mainScript.runSection("insidesTower");
                    break;

                case "endGame":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("endGameTower");
                    break;

                case "threat":
                    mainScript.runSection("threatTower");
                    return this.furyQuantum(true);

                case "cSlayPrincess":
                case "engage":
                    return this.furyQuantum(false);

                default: this.giveDefaultFailResponse();
            }
        }

        // Final choice
        this.canSlayPrincess = submitCount < 2;
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "submit", "[It's hopeless. Submit.]"));
        activeMenu.add(new Option(this.manager, "fight", submitCount >= 2, "[Seize control of your broken body and lay waste to your foe.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "submit":
                    mainScript.runSection("submitTower");
                    return this.furyUnwinding();

                case "cSlayPrincess":
                case "fight":
                    return this.furyQuantum(false);

                default: this.giveDefaultFailResponse();
            }
        }

        throw new RuntimeException("No ending reached");
    }

    /**
     * The player attempts to fight the Fury head-on (only accessible from the Tower)
     * @param lateJoin whether to skip the first section of dialogue
     * @return the ending reached by the player
     */
    private ChapterEnding furyQuantum(boolean lateJoin) {
        this.canSlayPrincess = false;
        if (!lateJoin) mainScript.runSection("quantumStart");
        mainScript.runSection("quantumStartJoin");

        Condition noEncouragement = new Condition(true);
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "lose", "(Explore) How are we supposed to lose this one?", noEncouragement));
        activeMenu.add(new Option(this.manager, "cant", "(Explore) I can't do anything right now. She's not letting me.", noEncouragement));
        activeMenu.add(new Option(this.manager, "worse", "(Explore) She's going to get worse. She gets worse every time we get to a new cabin."));
        activeMenu.add(new Option(this.manager, "die", "[Shed this vessel for the next.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "lose":
                case "cant":
                    noEncouragement.set(false);
                case "worse":
                    mainScript.runSection(activeOutcome + "QuantumStart");
                    break;

                case "die":
                    this.repeatActiveMenu = false;
                    break;

                default: mainScript.runSection("quantumFail");
            }
        }

        // Shed this vessel for the next
        this.removeVoice(Voice.BROKEN);
        mainScript.runSection("quantumSequence1");
        
        int deathNum = 1;
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "die2", "[Die again.]"));
        activeMenu.add(new Option(this.manager, "die3", "[And again.]", activeMenu.get("die2")));
        activeMenu.add(new Option(this.manager, "die4", "[AGAIN!]", activeMenu.get("die3")));
        activeMenu.add(new Option(this.manager, "end", "[End this.]", activeMenu.get("die4")));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "die4":
                    this.canSlayPrincess = true;
                    this.addVoice(Voice.COLD);
                case "die2":
                case "die3":
                    deathNum += 1;
                    mainScript.runSection("quantumSequence" + deathNum);
                    break;
                
                case "cSlayPrincess":
                case "end":
                    this.repeatActiveMenu = false;
                    break;

                default: mainScript.runConditionalSection("quantumFail", deathNum > 1);
            }
        }

        // End her
        mainScript.runSection("quantumEnd");
        return ChapterEnding.QUANTUMBEAK;
    }

    /**
     * The player is unwound by the Fury
     * @return the ending reached by the player
     */
    private ChapterEnding furyUnwinding() {
        mainScript.runSection("cellsStart");
        // Voice of the Contrarian is gone at this point; Voice of the Broken gives up if he's from the Tower
        this.removeVoice(Voice.CONTRARIAN);
        if (source.equals("tower")) this.removeVoice(Voice.BROKEN);

        mainScript.runSection();
        // Voice of the Hero is gone
        this.removeVoice(Voice.HERO);

        if (!this.hasAnyVoice(Voice.COLD, Voice.BROKEN)) {
            // Fail to survive the unwinding
            this.activeMenu = new OptionsMenu(true);
            activeMenu.add(new Option(this.manager, "fakeA", true, "\"Why did you keep me alive?\""));
            activeMenu.add(new Option(this.manager, "fakeB", true, "\"I've been here so long.\""));
            activeMenu.add(new Option(this.manager, "fakeC", true, "\"Everyone is gone.\""));
            activeMenu.add(new Option(this.manager, "fakeD", true, "\"There's no one else here.\""));
            activeMenu.add(new Option(this.manager, "fakeE", true, "\"I can't feel anything anymore.\""));
            activeMenu.add(new Option(this.manager, "fakeF", true, "\"But still, I'm here. Watching.\""));
            activeMenu.add(new Option(this.manager, "silent", "[There is nothing for you to say.]"));
            parser.promptOptionsMenu(activeMenu, "There is nothing for you to say.");

            mainScript.runSection();
            return ChapterEnding.IFYOUCOULDUNDERSTAND;
        }

        // Survive the unwinding
        // Remaining Voice: Cold or Broken (from the Adversary)
        Condition noExplore = new Condition(true);
        Condition niceMenuNotSeen = new Condition(true);
        
        this.canSlayPrincess = true;
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "here", "(Explore) \"I'm not gone. I'm right here.\"", noExplore));
        activeMenu.add(new Option(this.manager, "care", "(Explore) \"I never stopped caring about you. But you have to let go of all of that pain.\"", noExplore, niceMenuNotSeen));
        activeMenu.add(new Option(this.manager, "more", "(Explore) \"There's more to life than violence and pain. We can be more than that, if you want.\"", niceMenuNotSeen));
        activeMenu.add(new Option(this.manager, "noFight", "(Explore) \"I didn't like fighting you when we first met. I still don't want to do that.\"", niceMenuNotSeen));
        activeMenu.add(new Option(this.manager, "goal", "(Explore) \"What were you hoping to accomplish with all of that?\""));
        activeMenu.add(new Option(this.manager, "off", "(Explore) \"You seem so... off. It's like you're a completely different person than you were when you started pulling me apart.\""));
        activeMenu.add(new Option(this.manager, "awful", "(Explore) \"That was awful. I hated every second of it.\"", noExplore));
        activeMenu.add(new Option(this.manager, "all", "(Explore) \"So that's all you've got, huh?\"", noExplore));
        activeMenu.add(new Option(this.manager, "offer", "\"I'm going to leave now. I was hoping you'd come with me.\""));
        activeMenu.add(new Option(this.manager, "leaveTell", "\"I'm going to leave now.\""));
        activeMenu.add(new Option(this.manager, "leaveSilent", "[Turn and leave.]"));
        activeMenu.add(new Option(this.manager, "slay", "[Slay the Princess.]", this.hasBlade));

        OptionsMenu offerMenu = new OptionsMenu(true);
        offerMenu.add(new Option(this.manager, "hand", "[Give her your hand.]"));
        offerMenu.add(new Option(this.manager, "no", "\"No. I just wanted to hurt you back.\""));
        offerMenu.add(new Option(this.manager, "dunno", "\"No. And I don't know why I said that.\""));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "here":
                case "awful":
                case "all":
                    noExplore.set(false);
                    mainScript.runSection(activeOutcome + "Survive");

                    this.subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "offer", "\"All I did was stop the violence. You can stop it, too. Just leave with me.\""));
                    subMenu.add(new Option(this.manager, "open", "\"At some point you have to open your eyes and see the rest of the world. You have to see the world outside of us.\""));
                    subMenu.add(new Option(this.manager, "silent", "[Say nothing.]"));
                    
                    switch (parser.promptOptionsMenu(subMenu)) {
                        case "offer":
                            mainScript.runSection("offerASurvive");

                            this.activeOutcome = parser.promptOptionsMenu(offerMenu);
                            switch (activeOutcome) {
                                case "hand":
                                    mainScript.runSection("togetherEnd");
                                    return ChapterEnding.NEWLEAFWEATHEREDBOOK;
                                
                                default:
                                    mainScript.runSection(activeOutcome + "Offer");
                                    return ChapterEnding.LEAVEHERBEHIND;
                            }

                        case "open":
                            mainScript.runSection("openSurvive");
                            break;
                    }

                    break;

                case "care":
                    noExplore.set(false);
                case "more":
                case "nofight":
                    niceMenuNotSeen.set(false);
                    mainScript.runSection(activeOutcome + "Survive");

                    this.subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "offer", "\"You could always leave with me.\""));
                    subMenu.add(new Option(this.manager, "you", "\"That's up to you.\""));
                    subMenu.add(new Option(this.manager, "silent", "[Say nothing.]"));
                    
                    switch (parser.promptOptionsMenu(subMenu)) {
                        case "offer":
                            mainScript.runSection("offerASurvive");

                            this.activeOutcome = parser.promptOptionsMenu(offerMenu);
                            switch (activeOutcome) {
                                case "hand":
                                    mainScript.runSection("togetherEnd");
                                    return ChapterEnding.NEWLEAFWEATHEREDBOOK;
                                
                                default:
                                    mainScript.runSection(activeOutcome + "Offer");
                                    return ChapterEnding.LEAVEHERBEHIND;
                            }

                        case "you":
                            mainScript.runSection("upToYouSurvive");
                            break;
                    }

                    break;

                case "goal":
                case "off":
                    mainScript.runSection(activeOutcome + "Survive");
                    break;

                case "offer":
                    mainScript.runSection("offerBSurvive");

                    this.activeOutcome = parser.promptOptionsMenu(offerMenu);
                    switch (activeOutcome) {
                        case "hand":
                            mainScript.runSection("togetherEnd");
                            return ChapterEnding.NEWLEAFWEATHEREDBOOK;
                        
                        default:
                            mainScript.runSection(activeOutcome + "Offer");
                            return ChapterEnding.LEAVEHERBEHIND;
                    }

                case "leaveTell":
                    mainScript.runSection("leaveTellSurvive");

                    this.subMenu = new OptionsMenu();
                    subMenu.add(new Option(this.manager, "aloneA", "\"Yes.\""));
                    subMenu.add(new Option(this.manager, "aloneB", "\"If you'll let me.\""));
                    subMenu.add(new Option(this.manager, "offerB", "\"I was hoping you'd come with me.\""));
                    subMenu.add(new Option(this.manager, "offerC", "\"That's up to you.\""));
                    subMenu.add(new Option(this.manager, "silent", "[Turn and leave.]"));

                    while (repeatActiveMenu) {
                        this.activeOutcome = parser.promptOptionsMenu(subMenu);
                        switch (activeOutcome) {
                            case "aloneA":
                            case "aloneB":
                                mainScript.runSection("aloneTellSurvive");
                            case "cGoStairs":
                            case "silent":
                                mainScript.runSection("abandonEnd");
                                return ChapterEnding.LEAVEHERBEHIND;

                            case "offerB":
                            case "offerC":
                                mainScript.runSection(activeOutcome + "Survive");

                                this.activeOutcome = parser.promptOptionsMenu(offerMenu);
                                switch (activeOutcome) {
                                    case "hand":
                                        mainScript.runSection("togetherEnd");
                                        return ChapterEnding.NEWLEAFWEATHEREDBOOK;
                                    
                                    default:
                                        mainScript.runSection(activeOutcome + "Offer");
                                        return ChapterEnding.LEAVEHERBEHIND;
                                }
                            
                            default: super.giveDefaultFailResponse();
                        }
                    }

                    break;

                case "cGoStairs":
                case "leaveSilent":
                    mainScript.runSection("abandonEnd");
                    return ChapterEnding.LEAVEHERBEHIND;

                case "cTakeHasBladeFail":
                case "cSlayPrincess":
                case "slay":
                    mainScript.runSection("slayEnd");
                    return ChapterEnding.HINTOFFEELING;
                
                default: super.giveDefaultFailResponse();
            }
        }

        throw new RuntimeException("No ending reached");
    }


    // - Chapter III: The Apotheosis -

    /**
     * Runs Chapter III: The Apotheosis
     * @return the ending reached by the player
     */
    private ChapterEnding apotheosis() {
        /*
          Possible combinations:
            - Broken + Paranoid
            - Broken + Contrarian
         */

        mainScript.runSection();

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "explore", "(Explore) Okay, now hold on. I have SO many questions."));
        activeMenu.add(new Option(this.manager, "answers", "(Explore) I'm not going to the cabin until I have answers! What am I?"));
        activeMenu.add(new Option(this.manager, "cabin", "[Head to the cabin.]"));
        activeMenu.add(new Option(this.manager, "run", "[Run away.]"));
        activeMenu.add(new Option(this.manager, "stay", "[Stay where you are.]"));

        boolean runAttempt = false;
        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "explore":
                    mainScript.runSection("exploreWoods");
                    break;

                case "answers":
                case "stay":
                    this.repeatActiveMenu = false;
                    mainScript.runSection(activeOutcome + "Woods");
                    break;

                case "cGoLeave":
                case "run":
                    runAttempt = true;
                case "cGoHill":
                case "cabin":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("genericWoods");
                    break;

                default: super.giveDefaultFailResponse();
            }
        }

        this.currentLocation = GameLocation.HILL;
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "cabin", "[Proceed into the cabin.]"));
        
        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "cGoCabin":
                case "cabin":
                    this.repeatActiveMenu = false;
                    break;

                default: this.giveDefaultFailResponse();
            }
        }

        // Start to proceed into the cabin
        this.withBlade = true;
        mainScript.runSection();

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "explore", "(Explore) Hey, funny one, didn't you say something about a third option earlier? One that would make everyone unhappy?", this.hasVoice(Voice.CONTRARIAN)));
        activeMenu.add(new Option(this.manager, "fight", "[Take the blade.]"));
        activeMenu.add(new Option(this.manager, "submit", "[Embrace your new goddess.]"));
        activeMenu.add(new Option(this.manager, "flee", "[Flee.]", activeMenu.get("explore")));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "explore":
                    mainScript.runConditionalSection("contraExplore", runAttempt);
                    break;

                case "cTakeBlade":
                case "cSlayPrincessNoBladeFail":
                case "fight":
                    this.hasBlade = true;
                    this.withBlade = false;
                    switch (this.ch3Voice) {
                        case CONTRARIAN: return this.apotheosisFightContrarian();
                        default: return this.apotheosisFightParanoid();
                    }
                
                case "submit":
                    return this.apotheosisSubmit(false);

                case "flee":
                    mainScript.runSection("contraFlee");

                    this.activeMenu = new OptionsMenu();
                    activeMenu.add(new Option(this.manager, "submit", "[Embrace your new goddess.]"));

                    this.repeatActiveMenu = true;
                    while (repeatActiveMenu) {
                        switch (parser.promptOptionsMenu(activeMenu)) {
                            case "submit":
                                this.repeatActiveMenu = false;
                                break;

                            default: super.giveDefaultFailResponse();
                        }
                    }

                    mainScript.runSection("contraLateSubmit");
                    return this.apotheosisSubmit(true);

                default: this.giveDefaultFailResponse();
            }
        }

        throw new RuntimeException("No ending reached");
    }

    /**
     * The player attempts to fight the Apotheosis with the Voice of the Contrarian
     * @return the ending reached by the player
     */
    private ChapterEnding apotheosisFightContrarian() {
        mainScript.runSection("contraFightStart");

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "slay", "[Charge into oblivion.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "cSlayPrincess":
                case "slay":
                    this.repeatActiveMenu = false;
                    break;

                default: super.giveDefaultFailResponse();
            }
        }

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "slay", "[Carve into her divine heart.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "cSlayPrincess":
                case "slay":
                    this.repeatActiveMenu = false;
                    break;

                default: super.giveDefaultFailResponse();
            }
        }

        this.canThrowBlade = true;
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "throw", "[Hurl the blade at her eye.]"));
        activeMenu.add(new Option(this.manager, "submit", "[Embrace your new goddess.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "cSlayPrincess":
                case "cThrowBlade":
                case "throw":
                    this.repeatActiveMenu = false;
                    break;

                case "submit":
                    mainScript.runSection("contraLateSubmit");
                    return this.apotheosisSubmit(true);

                default: super.giveDefaultFailResponse();
            }
        }

        mainScript.runSection();
        this.clearVoices();
        return ChapterEnding.SOMETHINGTOREMEMBER;
    }

    /**
     * The player attempts to fight the Apotheosis with the Voice of the Paranoid
     * @return the ending reached by the player
     */
    private ChapterEnding apotheosisFightParanoid() {
        mainScript.runSection("paraFightStart");

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "slay", "[Embrace your destiny.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "cSlayPrincess":
                case "slay":
                    this.repeatActiveMenu = false;
                    break;

                default: super.giveDefaultFailResponse();
            }
        }

        mainScript.runSection();

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "slay", "[Assail the unassailable.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "cSlayPrincess":
                case "slay":
                    this.repeatActiveMenu = false;
                    break;

                default: super.giveDefaultFailResponse();
            }
        }

        mainScript.runSection();

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "still", "[Still your doubts.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "cSlayPrincess":
                case "still":
                    this.repeatActiveMenu = false;
                    break;

                default: super.giveDefaultFailResponse();
            }
        }
        
        mainScript.runSection();

        boolean push = false;
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "dream", "(Explore) Then what do we do? I thought you said that all of this was like a dream. That we could choose how it goes. So why can't we choose to win?"));
        activeMenu.add(new Option(this.manager, "push", "No! We just have to push harder! We wouldn't be here if there wasn't a way for us to win."));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "dream":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("paraFightDream");
                    break;
                
                case "cSlayPrincess":
                case "push":
                    this.repeatActiveMenu = false;
                    push = true;
                    mainScript.runSection("paraFightPush");
                    break;

                default: super.giveDefaultFailResponse();
            }
        }

        this.removeVoice(Voice.NARRATOR);
        this.hasBlade = false;
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "fight", "[Fight back.]"));
        activeMenu.add(new Option(this.manager, "submit", "[Submit.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "cSlayPrincess":
                case "fight":
                    this.repeatActiveMenu = false;
                    break;
                
                case "submit":
                    mainScript.runSection("paraLateSubmit");
                    return ChapterEnding.GRACE;

                default: super.giveDefaultFailResponse();
            }
        }
        
        mainScript.runConditionalSection(push);

        if (push) {
            this.clearVoices();
            return ChapterEnding.GODDESSUNRAVELED;
        } else {
            return ChapterEnding.WINDOWTOUNKNOWN;
        }
    }

    /**
     * The player submits to the Apotheosis
     * @param lateJoin whether the player first attempted to fight the Princess
     * @return the ending reached by the player
     */
    private ChapterEnding apotheosisSubmit(boolean lateJoin) {
        if (!lateJoin) mainScript.runSection("submitStart");

        this.removeVoice(Voice.NARRATOR);
        activeMenu.add(new Option(this.manager, "scream", "[Scream in unintelligible agony.]"));
        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "stop", "\"YOU HAVE TO STOP THIS, I CAN'T TAKE IT!\""));
        activeMenu.add(new Option(this.manager, "hurting", "\"YOU'RE HURTING ME!\""));
        activeMenu.add(new Option(this.manager, "silent", "[Suffer in silence.]"));
        parser.promptOptionsMenu(activeMenu);

        mainScript.runSection();

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "tendrils", "[Make her suffer with you.]"));
        activeMenu.add(new Option(this.manager, "alone", "[Suffer in the darkness alone.]"));

        switch (parser.promptOptionsMenu(activeMenu)) {
            case "tendrils":
                mainScript.runSection();
                return ChapterEnding.WINDOWTOUNKNOWN;

            case "alone":
                mainScript.runSection("aloneEnd");
                return ChapterEnding.GRACE;
        }

        throw new RuntimeException("No ending reached");
    }


    // - Chapter III: The Princess and the Dragon -

    /**
     * Prints a generic response to a command failing or being unavailable from the Princess during the Princess and the Dragon
     * @param outcome the String representation of the outcome of the attempted command
     */
    protected void giveDefaultFailResponsePrincess(String outcome) {
        switch (outcome) {
            case "cMeta":
                break;


            case "cGoFail":
            case "cEnterFail":
            case "cGoLeave":
            case "cGoPath":
            case "cGoHill":
            case "cGoCabin":
            case "cGoStairs":
            case "cGoBasement":
            case "cGoLeft":
            case "cGoRight":
            case "cProceed":
                mainScript.runMoodSection("defaultResponseGo");
                break;
                
            case "cLeaveFail":
                mainScript.runMoodSection("defaultResponseLeave");
                break;


            case "cApproachAtMirrorFail":
            case "cApproachMirrorFail":
            case "cApproachMirror":
            case "cWipeFail":
            case "cWipe":
            case "cSmashNoStubbornFail":
            case "cSmashFail":
            case "cSmash":
            case "cGazeNoMirrorFail":
            case "cGazeFail":
            case "cGaze":
                mainScript.runMoodSection("defaultResponseMirror");
                break;

            case "cApproachHerFail":
            case "cApproachHer":
                mainScript.runMoodSection("defaultResponseApproachHer");
                break;


            case "cSlayNoPrincessFail":
            case "cSlayPrincessNoBladeFail":
            case "cSlayPrincessFail":
            case "cSlayPrincess":
                mainScript.runMoodSection("defaultResponseSlayPrincess");
                break;

            case "cSlaySelfNoBladeFail":
            case "cSlaySelfFail":
            case "cSlaySelf":
                if (this.dragonBodyDownstairs) {
                    mainScript.runMoodSection("defaultResponseSlaySelfWithDragon");
                } else {
                    mainScript.runMoodSection("defaultResponseSlaySelf");
                }

                break;


            case "cTakeHasBladeFail":
            case "cTakeBladeFail":
            case "cTakeBlade":
                if (this.dragonBodyDownstairs) {
                    mainScript.runMoodSection("defaultResponseTakeWithDragon");
                } else {
                    mainScript.runMoodSection("defaultResponseTake");
                }

                break;

            case "cDropNoBladeFail":
            case "cDropFail":
            case "cDrop":
                mainScript.runMoodSection("defaultResponseDrop");
                break;

            case "cGiveNoBladeFail":
            case "cGiveBladeFail":
            case "cGiveBlade":
                if (this.dragonBodyDownstairs) {
                    mainScript.runMoodSection("defaultResponseGiveWithDragon");
                } else {
                    mainScript.runMoodSection("defaultResponseGive");
                }

                break;

            case "cThrowNoBladeFail":
            case "cThrowFail":
            case "cThrow":
                mainScript.runMoodSection("defaultResponseThrow");
                break;


            default: this.giveDefaultFailResponse();
        }
    }

    /**
     * Runs Chapter III: The Princess and the Dragon
     * @return the ending reached by the player
     */
    private ChapterEnding princessAndDragon() {
        // "You" have Cold + Opportunist, but you do not have any of the voices at the start of the Chapter






        // temporary templates for copy-and-pasting
        /*
        parser.printDialogueLine("XXXXX");
        parser.printDialogueLine(new PrincessDialogueLine("XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "(Explore) XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "(Explore) \"XXXXX\""));
        activeMenu.add(new Option(this.manager, "q1", "XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "\"XXXXX\""));
        */

        // PLACEHOLDER
        return null;
    }


    // - Chapter III: The Wraith -

    /**
     * Runs Chapter III: The Wraith
     * @return the ending reached by the player
     */
    private ChapterEnding wraith() {
        /*
          Possible combinations:
            - Cold + Paranoid (either Chapter)
            - Cold + Cheated (Spectre)
            - Paranoid + Opportunist (Nightmare)
         */

        switch (this.prevEnding) {
            case HEARTRIPPER:
            case HEARTRIPPERLEAVE:
                this.source = "spectre";
                break;
            default: this.source = "nightmare";
        }

        String voiceCombo;
        if (this.ch3Voice == Voice.CHEATED) {
            voiceCombo = "cheated";
        } else if (ch3Voice == Voice.OPPORTUNIST) {
            voiceCombo = "oppo";
        } else {
            voiceCombo = "paracold";
        }

        mainScript.runConditionalSection(voiceCombo);

        boolean loopExplore = false;
        boolean letOutExplore = false;
        Condition dontGoExplored = new Condition();
        InverseCondition noDontGoExplore = dontGoExplored.getInverse();
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "help", "(Explore) And why should we help you? All you're going to do is lock us away forever again.", ch3Voice == Voice.CHEATED));
        activeMenu.add(new Option(this.manager, "loop", "(Explore) Are you the same Narrator we met on the other loops? You were quick to accept that we've been here before."));
        activeMenu.add(new Option(this.manager, "letOut", "(Explore) We've killed her and been killed by her, and neither of those things have gone well for us. If we're going to fall through this loop forever, eventually we're going to let her out. We might as well do it now."));
        activeMenu.add(new Option(this.manager, "dontGo", "(Explore) What happens if we don't go to the cabin? That's another option."));
        activeMenu.add(new Option(this.manager, "proceed", "[Proceed to the cabin.]"));
        activeMenu.add(new Option(this.manager, "abortA", this.cantTryAbort, "[Turn around and leave.]", dontGoExplored));
        activeMenu.add(new Option(this.manager, "abortB", this.cantTryAbort, "There's something else we haven't tried... [Turn around and leave.]", noDontGoExplore));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "loop":
                    loopExplore = true;
                case "help":
                    mainScript.runSection(activeOutcome + "Start");
                    break;

                case "letOut":
                    letOutExplore = true;
                    mainScript.runConditionalSection("letOutStart", loopExplore);
                    break;

                case "dontGo":
                    dontGoExplored.set(true);
                    mainScript.runConditionalSection("dontGoStart", letOutExplore);
                    break;

                case "cGoHill":
                case "proceed":
                    this.repeatActiveMenu = false;
                    break;

                case "cGoLeave":
                    if (cantTryAbort.check()) {
                        parser.printDialogueLine("You have already tried that.");
                        break;
                    }
                case "abortA":
                case "abortB":
                    if (!this.canAbort) {
                        cantTryAbort.set();
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }

                    mainScript.runSection("abort");
                    this.abortVessel(true);
                    return ChapterEnding.ABORTED;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Proceed to the cabin
        this.currentLocation = GameLocation.HILL;
        mainScript.runSection("hillDialogue");

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "explore", "(Explore) Are you trying to use reverse-psychology on me or have you just given up?"));
        activeMenu.add(new Option(this.manager, "cabin", "[Enter the cabin.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "explore":
                    break;

                case "cGoHill":
                case "cabin":
                    this.repeatActiveMenu = false;
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Enter the cabin
        this.currentLocation = GameLocation.CABIN;
        this.knowsBlade = true;
        this.mirrorPresent = true;
        mainScript.runSection("cabinIntro");

        boolean surpriseComment = false;
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "explore", "(Explore) Let's pretend there is a mirror at the end of this hallway, and that right now, we can't see behind it. What's there? What's behind it?"));
        activeMenu.add(new Option(this.manager, "approach", "[Approach the mirror.]"));

        this.repeatActiveMenu = false;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "explore":
                    surpriseComment = this.ch3Voice != Voice.OPPORTUNIST;
                    mainScript.runSection("mirrorExplore");
                    break;

                case "cApproachMirror":
                case "cGoStairs":
                case "approach":
                    this.repeatActiveMenu = false;
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }
        
        // Approach the mirror
        this.currentLocation = GameLocation.MIRROR;
        mainScript.runSection("mirrorApproach");

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "wipe", "[Wipe the mirror clean.]"));
        
        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "cWipe":
                case "wipe":
                    this.repeatActiveMenu = false;
                    break;

                case "cApproachAtMirrorFail":
                    this.giveDefaultFailResponse("cApproachAtMirrorFail");
                    break;

                default: this.giveDefaultFailResponse();
            }
        }

        // Wipe the mirror clean
        this.currentLocation = GameLocation.CABIN;
        this.withPrincess = true;
        this.mirrorPresent = false;
        this.touchedMirror = true;
        mainScript.runConditionalSection("mirrorWipe", surpriseComment, voiceCombo);

        String evenOrder = (source.equals("spectre")) ? "I killed you, and then you killed me." : "You killed me, and then I killed you.";
        GlobalInt stallCount = new GlobalInt();
        NumCondition noStall = new NumCondition(stallCount, 0);
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "agree", "(Explore) \"I thought you couldn't possess me on your own. I thought I needed to agree to it.\"", this.spectreCantWontAsk));
        activeMenu.add(new Option(this.manager, "even", "(Explore) \"Look, we're even now. " + evenOrder + " Water under the bridge, right?\""));
        activeMenu.add(new Option(this.manager, "want", "(Explore) \"I never wanted to hurt you. I don't even know how I got here!\""));
        activeMenu.add(new Option(this.manager, "victim", "(Explore) \"I'm a victim in all of this too, you know!\""));
        activeMenu.add(new Option(this.manager, "better", "(Explore) \"Wouldn't possessing me against my will make you no better than me? You don't have to be evil. You don't have to do this.\""));
        activeMenu.add(new Option(this.manager, "barter", "(Explore) \"Do you *need* to take my body? Can't I just... open the door for you?\""));
        activeMenu.add(new Option(this.manager, "consentA", "\"That's fine. I actually came here to free you.\"", noStall));
        activeMenu.add(new Option(this.manager, "consentB", "\"Okay. Fine. Just do it.\"", noStall.getInverse()));
        activeMenu.add(new Option(this.manager, "struggle", "[Struggle.]"));

        boolean consent = false;
        boolean incrementFlag;
        while (stallCount.lessThan(2)) {
            incrementFlag = true;

            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "agree":
                case "even":
                case "want":
                case "victim":
                case "better":
                case "barter":
                    mainScript.runSection(activeOutcome + "Menu");
                    break;

                case "consentA":
                case "consentB":
                    stallCount.set(3); // Fast-forward
                    incrementFlag = false;
                    consent = true;
                    mainScript.runSection("consentMenu");
                    break;

                case "struggle":
                    stallCount.set(3); // Fast-forward
                    incrementFlag = false;
                    mainScript.runSection("struggleMenu");
                    break;

                default:
                    incrementFlag = false;
                    this.giveDefaultFailResponse(activeOutcome);
            }

            if (incrementFlag) {
                stallCount.increment();
                if (stallCount.equals(2)) mainScript.runSection("stallMenu");
            }
        }

        if (!consent) {
            mainScript.runSection(voiceCombo + "NoConsent");

            this.reverseDirection = true;
            this.activeMenu = new OptionsMenu();
            activeMenu.add(new Option(this.manager, "leave", "[Leave the cabin.]"));
            activeMenu.add(new Option(this.manager, "abyss", "[Throw your body into the abyss.]"));

            this.repeatActiveMenu = true;
            while (repeatActiveMenu) {
                switch (parser.promptOptionsMenu(activeMenu)) {
                    case "cGoHill":
                    case "leave":
                        this.repeatActiveMenu = false;
                        break;

                    case "cGoStairs":
                    case "abyss":
                        mainScript.runSection("abyssStart");
                        return ChapterEnding.EXORCISTIII;

                    default: super.giveDefaultFailResponse();
                }
            }
        }

        mainScript.runConditionalSection("freeStart", consent);
        return ChapterEnding.PASSENGER;
    }


    // - Chapter ???: The Moment of Clarity -

    /**
     * Runs Chapter ???: The Moment of Clarity
     * @return the ending reached by the player
     */
    private ChapterEnding momentOfClarity() {
        // You have all voices

        mainScript.runSection();

        boolean nothingAttempt = false;
        Condition talked = new Condition();
        InverseCondition noTalk = talked.getInverse();
        Condition askedHowMany = new Condition();
        InverseCondition noHowMany = askedHowMany.getInverse();
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "wrong", "(Explore) I think they're all... wrong."));
        activeMenu.add(new Option(this.manager, "howManyA", "(Explore) That's a good question. How many times have you all been here?", noHowMany, noTalk));
        activeMenu.add(new Option(this.manager, "howManyB", "(Explore) Getting back to His earlier question, how many times have you all been here?", noHowMany, talked));
        activeMenu.add(new Option(this.manager, "decider", "(Explore) But that doesn't make sense. I only remember being here twice before this, and some of you don't seem to remember being here at all. Was I here those other times? Did someone else make the decisions?", askedHowMany));
        activeMenu.add(new Option(this.manager, "notMe", "(Explore) If I don't remember what I did, then it couldn't have been me that did it.", activeMenu.get("decider")));
        activeMenu.add(new Option(this.manager, "dontGo", "(Explore) What if we don't go to the cabin?"));
        activeMenu.add(new Option(this.manager, "sense", "(Explore) Can you make sense of them?"));
        activeMenu.add(new Option(this.manager, "disjointed", "(Explore) I feel so disjointed. I don't know if I can pull this off. I don't know if I can slay her."));
        activeMenu.add(new Option(this.manager, "proceed", "[Proceed to the cabin.]"));
        activeMenu.add(new Option(this.manager, "nothing", this.cantTryAbort, "The only way out is to do nothing. So nothing I will do. [Stay where you are.]", 0));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "wrong":
                case "dontGo":
                case "sense":
                case "disjointed":
                    talked.set();
                case "decider":
                case "notMe":
                    mainScript.runSection(activeOutcome);
                    break;

                case "howManyA":
                case "howManyB":
                    talked.set();
                    askedHowMany.set();
                    mainScript.runSection("howMany");
                    break;

                case "cGoHill":
                case "proceed":
                    this.repeatActiveMenu = false;
                    break;

                case "nothing":
                    nothingAttempt = true;
                    mainScript.runSection("nothing");

                    this.activeMenu = new OptionsMenu();
                    activeMenu.add(new Option(this.manager, "proceed", "[Proceed to the cabin.]"));
                    activeMenu.add(new Option(this.manager, "nothing", this.cantTryAbort, "[Continue to do nothing.]", 0));

                    while (repeatActiveMenu) {
                        this.activeOutcome = parser.promptOptionsMenu(activeMenu);
                        switch (activeOutcome) {
                            case "cGoCabin":
                            case "proceed":
                                this.repeatActiveMenu = false;
                                break;

                            case "nothing":
                                if (!this.canAbort) {
                                    cantTryAbort.set();
                                    parser.printDialogueLine(CANTSTRAY);
                                    break;
                                }

                                mainScript.runSection();
                                this.abortVessel(true);
                                return ChapterEnding.ABORTED;

                            default: this.giveDefaultFailResponse(activeOutcome);
                        }
                    }

                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Proceed to the cabin
        mainScript.runSection("hillApproach");

        this.currentLocation = GameLocation.HILL;
        this.mirrorPresent = true;
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "explore", "(Explore) And what's wrong with giving them space? What if it helps them? What if they need to be heard?"));
        activeMenu.add(new Option(this.manager, "approach", "[Approach the mirror.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "explore":
                    mainScript.runSection("hillExplore");
                    break;

                case "cGoCabin":
                case "cApproachMirror":
                case "approach":
                    this.repeatActiveMenu = false;
                    break;

                default: super.giveDefaultFailResponse();
            }
        }

        // Approach the mirror
        this.currentLocation = GameLocation.MIRROR;
        mainScript.runSection("cabinApproach");

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "proceed", "[Proceed.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu, true)) {
                case "cProceed":
                case "proceed":
                    this.repeatActiveMenu = false;
                    break;

                default: super.giveDefaultFailResponse();
            }
        }

        // Enter "the cabin"
        this.currentLocation = GameLocation.CABIN;
        this.knowsBlade = true;
        this.withBlade = true;
        this.touchedMirror = true;
        this.mirrorPresent = false;
        mainScript.runConditionalSection("proceed", nothingAttempt);

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "takeA", "[Take the blade.]"));
        activeMenu.add(new Option(this.manager, "falseA", true, "[It's the only way forward.]"));
        activeMenu.add(new Option(this.manager, "falseB", true, "[You've already tried everything else.]"));
        activeMenu.add(new Option(this.manager, "falseC", true, "[Don't you remember?]"));
        activeMenu.add(new Option(this.manager, "takeB", "[You have to take the blade.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu, "You've already tried everything else.")) {
                case "cTakeBlade":
                case "takeA":
                case "takeB":
                    this.repeatActiveMenu = false;
            }
        }

        // Take the blade
        this.withBlade = false;
        this.withPrincess = true;
        mainScript.runSection();

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "falseA", true, "[You're just an object.]"));
        activeMenu.add(new Option(this.manager, "falseB", true, "[A tool.]"));
        activeMenu.add(new Option(this.manager, "falseC", true, "[You once were something else, a long time ago.]"));
        activeMenu.add(new Option(this.manager, "falseD", true, "[But was that something you, or is it just a dull and jaded memory?]"));
        activeMenu.add(new Option(this.manager, "falseE", true, "[There is no other ending here.]"));
        activeMenu.add(new Option(this.manager, "take", "[Just take her hand, and set her free.]"));

        parser.promptOptionsMenu(activeMenu, "There is no other ending here.");
        mainScript.runSection();
        return ChapterEnding.MOMENTOFCLARITY;
    }


    // - Chapter III: The Arms Race / No Way Out -

    /**
     * Runs the opening sequence of Chapter III: The Arms Race / No Way Out
     * @return the ending reached by the player
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

        if (!this.hasBlade) this.threwBlade = true;
        mainScript.runSection();

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
        
        this.currentLocation = GameLocation.MIRROR;
        mainScript.runSection("approachMirror");

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "wipe", "[Wipe the mirror clean.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "cWipe":
                case "wipe":
                    this.repeatActiveMenu = false;
                    break;

                case "cApproachAtMirrorFail":
                    this.giveDefaultFailResponse("cApproachAtMirrorFail");
                    break;

                default: super.giveDefaultFailResponse();
            }
        }

        this.currentLocation = GameLocation.BASEMENT;
        this.withPrincess = true;
        this.canSlayPrincess = true;
        this.touchedMirror = true;
        this.mirrorPresent = false;
        mainScript.runSection("wipeMirror");

        // Create the options menu used in both chapters here, then pass it into the basement methods; the menu is almost identical in both chapters anyway
        // Your choice in this menu determines which voice you get after dying
        Condition noCold = new Condition(true);
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "coldA", "We're going to fight her again, and we're going to have a stiff upper lip about it. She can't hurt us if we don't let ourselves feel it.", this.hasVoice(Voice.STUBBORN), noCold));
        activeMenu.add(new Option(this.manager, "stubborn", "We're fighting her, obviously.", !this.hasVoice(Voice.STUBBORN)));
        activeMenu.add(new Option(this.manager, "oppo", "We're going to appeal to her authority. Puff her up a bit. There's no reason we can't talk this out."));
        activeMenu.add(new Option(this.manager, "broken", "We're going to unconditionally surrender.", !this.hasVoice(Voice.BROKEN)));
        activeMenu.add(new Option(this.manager, "hunted", "I'm going to go with not letting her stab us. We can dodge, right?", !this.hasVoice(Voice.HUNTED)));
        activeMenu.add(new Option(this.manager, "smitten", "Oh, that's easy. I'm going to try flirting with her."));
        activeMenu.add(new Option(this.manager, "para", "She has swords for arms and we don't. We're panicking!", !this.hasVoice(Voice.PARANOID)));
        activeMenu.add(new Option(this.manager, "coldB", "We're going to fight her, and we're going to have a stiff upper lip about it. She can't hurt us if we don't let ourselves feel it.", this.hasBlade && !this.hasVoice(Voice.STUBBORN), noCold));
        activeMenu.add(new Option(this.manager, "coldNWO", "We're going to let her stab us, and we're going to have a stiff upper lip about it. She can't hurt us if we don't let ourselves feel it.", !this.hasBlade, noCold));
        activeMenu.add(new Option(this.manager, "contra", "She wins by killing us, right? So let's beat her to it!", !this.hasVoice(Voice.CONTRARIAN)));
        activeMenu.add(new Option(this.manager, "skeptic", "[All of these ideas suck. Think up something better.]"));

        if (this.hasBlade) {
            this.armsRaceBasement(noCold);
        } else {
            this.noWayOutBasement(noCold);
        }

        return this.razor4();
    }

    /**
     * Runs the basement section of Chapter III: The Arms Race
     * @param noCold a condition keeping track of whether the player has the Voice of the Cold
     */
    private void armsRaceBasement(Condition noCold) {
        this.secondaryScript = new Script(this.manager, this.parser, "Routes/Razor/BasementArmsRace");

        secondaryScript.runSection();

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
                    secondaryScript.runSection("coldMenuStubborn");
                case "coldB":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("coldMenu");

                    this.subMenu = new OptionsMenu(true);
                    activeMenu.add(new Option(this.manager, "taunt", "\"Do your worst! I bet you can't even hurt me.\""));
                    activeMenu.add(new Option(this.manager, "wait", "[Wait for her to come to you.]"));

                    noCold.set(false);
                    mainScript.runSection(parser.promptOptionsMenu(subMenu) + "Cold");
                    secondaryScript.runSection("coldMenu");
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
                    this.subMenu = new OptionsMenu(true);
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

                    this.subMenu = new OptionsMenu(true);
                    activeMenu.add(new Option(this.manager, "winner", "\"You know, I'm a big fan of winners, and you've got 'winner' written all over you. How about we stop fighting and team up? I'll even let you be in charge!\""));
                    activeMenu.add(new Option(this.manager, "join", "\"Look, both of us are stuck here against our will. What if we joined forces?\""));
                    activeMenu.add(new Option(this.manager, "stabbing", "\"Has anyone ever told you how good you are at stabbing things?\""));

                    switch (parser.promptOptionsMenu(subMenu)) {
                        case "winner":
                        case "join":
                            mainScript.runSection("oppoWinner");
                            break;

                        case "stabbing":
                            secondaryScript.runSection("oppoStabbing");

                            this.subMenu = new OptionsMenu(true);
                            activeMenu.add(new Option(this.manager, "goodSide", "\"Yes! Yes, I am trying to get on your good side. Did it work?\""));
                            activeMenu.add(new Option(this.manager, "bored", "\"Yes! Yes, I am bored of you stabbing me. Can you stop stabbing me now?\""));
                            activeMenu.add(new Option(this.manager, "facts", "\"Psht. What? Me? Fluffing you up? I'm just stating facts.\""));
                            activeMenu.add(new Option(this.manager, "silent", "[Say nothing.]"));

                            mainScript.runSection(parser.promptOptionsMenu(subMenu) + "Oppo");
                            break;
                    }
                    
                    secondaryScript.runSection("oppoJoin");
                    mainScript.runSection("oppoEnd");
                    this.addVoice(Voice.OPPORTUNIST);
                    break;
                    
                case "broken":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("brokenMenu");

                    // Your choice here doesn't actually matter
                    this.subMenu = new OptionsMenu(true);
                    activeMenu.add(new Option(this.manager, "giveUp", "\"I give up. I'll do anything, just please don't stab me!\""));
                    activeMenu.add(new Option(this.manager, "silent", "[Silently throw your hands in the air.]"));
                    parser.promptOptionsMenu(subMenu);

                    secondaryScript.runSection("brokenMenu");
                    this.addVoice(Voice.BROKEN);
                    break;
                    
                case "smitten":
                    this.repeatActiveMenu = false;
                    secondaryScript.runSection("smittenMenu");

                    this.subMenu = new OptionsMenu(true);
                    activeMenu.add(new Option(this.manager, "gorgeous", "\"I know you want to kill me, but has anyone ever told you how gorgeous you are?\""));
                    activeMenu.add(new Option(this.manager, "getYou", "\"I just feel like I really get you. I like you. Romantically, even. Maybe we can hash this out over a date.\""));
                    activeMenu.add(new Option(this.manager, "dinner", "\"How about you buy me dinner before impaling me to death?\""));
                    activeMenu.add(new Option(this.manager, "TheLook", "[Give her *The Look.*]"));

                    this.activeOutcome = parser.promptOptionsMenu(subMenu);
                    switch (activeOutcome) {
                        case "TheLook":
                            secondaryScript.runSection("TheLookSmitten");
                        case "gorgeous":
                        case "getYou":
                        case "dinner":
                            mainScript.runSection(activeOutcome + "Smitten");
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
     * @param noCold a condition keeping track of whether the player has the Voice of the Cold
     */
    private void noWayOutBasement(Condition noCold) {
        this.secondaryScript = new Script(this.manager, this.parser, "Routes/Razor/BasementNoWayOut");

        secondaryScript.runSection();

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
                    this.subMenu = new OptionsMenu(true);
                    activeMenu.add(new Option(this.manager, "maybe", "Maybe we'll win!"));
                    activeMenu.add(new Option(this.manager, "see", "See, but that's the brilliance of it all. She won't see it coming."));
                    activeMenu.add(new Option(this.manager, "done", "I'm done explaining myself. I'm going to punch her now."));

                    if (parser.promptOptionsMenu(subMenu).equals("maybe")) secondaryScript.runSection("stubbornMaybe");
                    secondaryScript.runSection("stubbornCont");
                    this.addVoice(Voice.STUBBORN);
                    break;
                    
                case "oppo":
                    this.repeatActiveMenu = false;
                    secondaryScript.runSection("oppoMenu");
                    mainScript.runSection("oppoMenu");

                    this.subMenu = new OptionsMenu(true);
                    activeMenu.add(new Option(this.manager, "winner", "\"You know, I'm a big fan of winners, and you've got 'winner' written all over you. How about we stop fighting and team up? I'll even let you be in charge!\""));
                    activeMenu.add(new Option(this.manager, "join", "\"Look, both of us are stuck here against our will. What if we joined forces?\""));
                    activeMenu.add(new Option(this.manager, "stabbing", "\"Has anyone ever told you how good you are at stabbing things?\""));

                    switch (parser.promptOptionsMenu(subMenu)) {
                        case "winner":
                        case "join":
                            mainScript.runSection("oppoWinner");
                            break;

                        case "stabbing":
                            secondaryScript.runSection("oppoStabbing");

                            this.subMenu = new OptionsMenu(true);
                            activeMenu.add(new Option(this.manager, "goodSide", "\"Yes! Yes, I am trying to get on your good side. Did it work?\""));
                            activeMenu.add(new Option(this.manager, "facts", "\"Psht. What? Me? Fluffing you up? I'm just stating facts.\""));
                            activeMenu.add(new Option(this.manager, "silent", "[Say nothing.]"));

                            mainScript.runSection(parser.promptOptionsMenu(subMenu) + "Oppo");
                            break;
                    }

                    secondaryScript.runSection("oppoJoin");
                    mainScript.runSection("oppoEnd");
                    this.addVoice(Voice.OPPORTUNIST);
                    break;
                    
                case "broken":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("brokenMenu");

                    this.subMenu = new OptionsMenu(true);
                    activeMenu.add(new Option(this.manager, "giveUp", "\"I give up. I'll do anything, just please don't stab me!\""));
                    activeMenu.add(new Option(this.manager, "silent", "[Silently throw your hands in the air.]"));

                    if (parser.promptOptionsMenu(subMenu).equals("silent")) secondaryScript.runSection("brokenSilent");
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

                    this.subMenu = new OptionsMenu(true);
                    activeMenu.add(new Option(this.manager, "gorgeous", "\"I know you want to kill me, but has anyone ever told you how gorgeous you are?\""));
                    activeMenu.add(new Option(this.manager, "getYou", "\"I just feel like I really get you. I like you. Romantically, even. Maybe we can hash this out over a date.\""));
                    activeMenu.add(new Option(this.manager, "dinner", "\"How about you buy me dinner before impaling me to death?\""));
                    activeMenu.add(new Option(this.manager, "TheLook", "[Give her *The Look.*]"));

                    this.activeOutcome = parser.promptOptionsMenu(subMenu);
                    switch (activeOutcome) {
                        case "TheLook":
                            secondaryScript.runSection("TheLookSmitten");
                        case "gorgeous":
                        case "getYou":
                        case "dinner":
                            mainScript.runSection(activeOutcome + "Smitten");
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

                case "coldNWO":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("coldMenu");

                    this.subMenu = new OptionsMenu(true);
                    activeMenu.add(new Option(this.manager, "taunt", "\"Do your worst! I bet you can't even hurt me.\""));
                    activeMenu.add(new Option(this.manager, "wait", "[Wait for her to come to you.]"));
                    
                    noCold.set(false);
                    mainScript.runSection(parser.promptOptionsMenu(subMenu) + "Cold");
                    secondaryScript.runSection("coldMenu");
                    mainScript.runSection("coldJoin");
                    this.addVoice(Voice.COLD);
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

        Voice menuVoice = Voice.HERO;
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
                    menuVoice = Voice.COLD;
                    break;
                    
                case "stubborn":
                    this.repeatActiveMenu = false;
                    menuVoice = Voice.STUBBORN;
                    break;
                    
                case "oppo":
                    this.repeatActiveMenu = false;
                    menuVoice = Voice.OPPORTUNIST;
                    break;
                    
                case "broken":
                    this.repeatActiveMenu = false;
                    menuVoice = Voice.BROKEN;
                    break;
                    
                case "hunted":
                    this.repeatActiveMenu = false;
                    menuVoice = Voice.HUNTED;
                    break;
                    
                case "smitten":
                    this.repeatActiveMenu = false;
                    menuVoice = Voice.SMITTEN;
                    break;
                    
                case "para":
                    this.repeatActiveMenu = false;
                    menuVoice = Voice.PARANOID;
                    break;
                    
                case "cSlaySelf":
                case "contra":
                    this.repeatActiveMenu = false;
                    menuVoice = Voice.CONTRARIAN;
                    break;
                    
                case "skeptic":
                    this.repeatActiveMenu = false;
                    menuVoice = Voice.SKEPTIC;
                    break;

                case "cGoStairs":
                    mainScript.runSection("leaveAttempt");
                    break;

                case "cSlaySelfFail":
                case "cSlayPrincessFail":
                case "noFightOptions":
                    mainScript.runSection("failedSlayAttempt");
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }
        
        this.addVoice(menuVoice);
        mainScript.runConditionalSection("endMenu", menuVoice.toString());

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
                case 5:
                    mainScript.runSection("montage" + segmentNum);
                    break;

                case 2:
                    if (!this.hasBlade || contraLast) {
                        mainScript.runSection("montage2NoBlade");
                    } else {
                        mainScript.runSection("montage2Blade");
                    }

                    break;

                case 3:
                case 4:
                    mainScript.runBladeSection("montage" + segmentNum);
                    break;
            }

            this.addVoice(v);
            mainScript.runSection(v + "Montage");
            contraLast = v == Voice.CONTRARIAN;
        }

        mainScript.runSection("montageEnd");
    }


    // - Chapter IV: Mutually Assured Destruction / The Empty Cup -

    /**
     * Runs Chapter IV: Mutually Assured Destruction / The Empty Cup
     * @return the ending reached by the player
     */
    private ChapterEnding razor4() {
        // You have all Voices
        
        mainScript.runSection();

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

        mainScript.runSection("empty2");

        if (this.hasBlade) {
            return ChapterEnding.WATERSTEEL;
        } else {
            return ChapterEnding.FORMLESS;
        }
    }


    // - Chapter III: The Den -

    /**
     * Runs Chapter III: The Den
     * @return the ending reached by the player
     */
    private ChapterEnding den() {
        /*
          Possible combinations:
            - Hunted + Stubborn
            - Hunted + Skeptic
         */

        mainScript.runSection();

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "Plan", "(Explore) Okay. What's the plan?"));
        activeMenu.add(new Option(this.manager, "different", "(Explore) The path is different than it was before."));
        activeMenu.add(new Option(this.manager, "answers", "(Explore) I want answers. What is going on here? What do you know that you're not telling me?"));
        activeMenu.add(new Option(this.manager, "leave", "(Explore) What if we just leave? What happens then?"));
        activeMenu.add(new Option(this.manager, "proceedA", "No matter what happens next, it seems like all our answers are in the cabin. Let's see this through. [Proceed to the cabin.]"));
        activeMenu.add(new Option(this.manager, "proceedB", "[Silently proceed to the cabin.]"));
        activeMenu.add(new Option(this.manager, "abort", this.cantTryAbort, "I'm done with this. Bye! [Turn around and leave.]", 0));

        boolean plan = false;
        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "Plan":
                    plan = true;
                    mainScript.runSection(this.ch3Voice + activeOutcome + "Woods");
                    break;

                case "different":
                case "answers":
                case "leave":
                    mainScript.runSection(activeOutcome + "Woods");
                    break;

                case "cGoHill":
                case "proceedA":
                case "proceedB":
                    this.repeatActiveMenu = false;
                    break;

                case "cGoLeave":
                    if (cantTryAbort.check()) {
                        parser.printDialogueLine("You have already tried that.");
                        break;
                    }
                case "abort":
                    if (!this.canAbort) {
                        cantTryAbort.set();
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }

                    mainScript.runSection("abort");
                    this.abortVessel(true);
                    return ChapterEnding.ABORTED;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Proceed to the cabin
        this.currentLocation = GameLocation.HILL;
        mainScript.runConditionalSection("hillDialogue", plan);

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "proceed", "[Proceed into the cabin.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "cGoCabin":
                case "proceed":
                    this.repeatActiveMenu = false;
                    break;
                
                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Enter the cabin
        this.currentLocation = GameLocation.CABIN;
        this.knowsBlade = true;
        this.withBlade = true;
        this.mirrorPresent = true;
        mainScript.runSection("cabinIntro");

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "take", "(Explore) [Take the blade.]"));
        activeMenu.add(new Option(this.manager, "approach", "[Approach the mirror.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "cTakeBlade":
                    activeMenu.setCondition("take", false);
                case "take":
                    this.hasBlade = true;
                    this.withBlade = false;
                    mainScript.runSection("takeBlade");
                    break;

                case "cGoStairs":
                case "cApproachMirror":
                case "approach":
                    this.repeatActiveMenu = false;
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Approach the mirror
        this.currentLocation = GameLocation.MIRROR;
        mainScript.runSection("approachMirror");

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "wipe", "[Wipe the mirror clean.]"));
        activeMenu.add(new Option(this.manager, "smash", "[Smash it.]", this.hasVoice(Voice.STUBBORN)));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "cWipe":
                case "wipe":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("wipeMirror");
                    break;

                case "cSmash":
                case "smash":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("smashMirror");
                    break;

                case "cApproachAtMirrorFail":
                    this.giveDefaultFailResponse("cApproachAtMirrorFail");
                    break;

                default: super.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Start down the "stairs"
        this.currentLocation = GameLocation.STAIRS;
        this.withBlade = false;
        this.mirrorPresent = false;
        this.touchedMirror = true;

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "unfinished", "\"We have unfinished business.\""));
        activeMenu.add(new Option(this.manager, "talk", "\"We should talk.\""));
        activeMenu.add(new Option(this.manager, "free", "\"We don't have to fight. I'm ready to let you out of here.\""));
        activeMenu.add(new Option(this.manager, "lie", "(Lie) \"We don't have to fight. I'm ready to let you out of here.\""));
        activeMenu.add(new Option(this.manager, "silent", "[Say nothing, and silently proceed down the \"stairs.\"]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "unfinished":
                case "talk":
                case "free":
                case "lie":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("stairsComment");
                    break;

                case "cGoBasement":
                case "silent":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("stairsSilent");
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Continue down the stairs
        this.currentLocation = GameLocation.BASEMENT;
        this.withPrincess = true;
        this.canSlayPrincess = true;

        Condition noChat = new Condition(true);
        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "quiet", "(Explore) \"Staying quiet, are we?\""));
        activeMenu.add(new Option(this.manager, "dontHave", "(Explore) \"We don't have to do this.\""));
        activeMenu.add(new Option(this.manager, "fight", "[Step into the shadows, ready to fight.]"));
        activeMenu.add(new Option(this.manager, "lure", "[Step into the shadows and try to lure her out.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "quiet":
                case "dontHave":
                    noChat.set(false);
                    mainScript.runSection(activeOutcome + "BasementStart");
                    break;

                case "cSlayPrincess":
                case "fight":
                    return this.denFight();

                case "lure":
                    return this.denLure();

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        throw new RuntimeException("No ending reached");
    }

    /**
     * The player attempts to fight the Den
     * @return the ending reached by the player
     */
    private ChapterEnding denFight() {
        mainScript.runSection("fightStart");

        if (this.hasVoice(Voice.SKEPTIC) || !this.hasBlade) return ChapterEnding.UNANSWEREDQUESTIONS;

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "strike", "[Take the opening and strike at her heart.]"));
        activeMenu.add(new Option(this.manager, "instinct", "[Embrace instinct.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "cSlayPrincess":
                case "strike":
                    mainScript.runSection("strikeEnd");
                    return ChapterEnding.HEROICSTRIKE;

                case "instinct":
                    mainScript.runSection("instinctEnd");
                    return ChapterEnding.INSTINCT;

                default: this.giveDefaultFailResponse();
            }
        }

        throw new RuntimeException("No ending reached");
    }

    /**
     * The player attempts to lure the Den out of the basement
     * @return the ending reached by the player
     */
    private ChapterEnding denLure() {
        mainScript.runSection("lureStart");

        if (this.hasVoice(Voice.STUBBORN)) return ChapterEnding.UNANSWEREDQUESTIONS;
        
        this.currentLocation = GameLocation.STAIRS;
        this.canDropBlade = true;
        this.canApproachHer = true;
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "approachDrop", "[Drop the blade and approach her.]", this.hasBlade));
        activeMenu.add(new Option(this.manager, "approachBlade", "[Approach her, blade held behind your back.]", this.hasBlade));
        activeMenu.add(new Option(this.manager, "approachNoBlade", "[Approach her.]", !this.hasBlade));
        activeMenu.add(new Option(this.manager, "retrieve", "[Turn back for the blade.]", !this.hasBlade));
        activeMenu.add(new Option(this.manager, "leave", "[Turn around and leave. You're done here.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "cDrop":
                case "approachDrop":
                    this.hasBlade = false;
                    mainScript.runSection("dropBlade");
                case "cApproachHer":
                case "cSlayPrincess":
                case "cGoBasement":
                case "approachBlade":
                case "approachNoBlade":
                    this.repeatActiveMenu = false;
                    break;
                    
                case "cTakeBladeFail":
                case "cGoCabin":
                case "retrieve":
                case "leave":
                    return this.denCollapse();

                case "cSlayPrincessNoBladeFail":
                    mainScript.runSection("slayAttemptNoBlade");
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Approach her
        this.canDropBlade = false;
        this.canApproachHer = false;
        mainScript.runSection("approachStart");

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "strike", "[Strike at her heart.]", this.hasBlade));
        activeMenu.add(new Option(this.manager, "offer", "[Offer her your hand.]"));
        activeMenu.add(new Option(this.manager, "flinch", "[Flinch.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "cSlayPrincess":
                case "strike":
                    mainScript.runSection("coupEnd");
                    return ChapterEnding.COUPDEGRACE;

                case "offer":
                    if (this.hasBlade) {
                        mainScript.runSection("offerBlade");
                        return ChapterEnding.UNANSWEREDQUESTIONS;
                    }

                    this.repeatActiveMenu = false;
                    break;

                case "flinch":
                    mainScript.runSection("flinchStart");
                    return ChapterEnding.UNANSWEREDQUESTIONS;

                case "cSlayPrincessNoBladeFail":
                    mainScript.runSection("slayAttemptApproach");
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Offer her your hand
        mainScript.runSection("lionStart");

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "pull", "[Pull her free.]"));
        parser.promptOptionsMenu(activeMenu);
        mainScript.runSection();

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "dig", "[Dig through the earth together.]"));
        parser.promptOptionsMenu(activeMenu);
        mainScript.runSection();

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "claw", "[Claw your way to freedom.]"));
        parser.promptOptionsMenu(activeMenu);

        this.currentLocation = GameLocation.CABIN;
        mainScript.runSection();

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "open", "[Open the door and step into the wilds.]"));

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
        return ChapterEnding.LIONANDMOUSE;
    }

    /**
     * The player attempts to leave after trapping the Princess in the tunnel, leading to the "Unanswered Questions" ending and claiming the Den
     * @return the ending reached by the player
     */
    private ChapterEnding denCollapse() {
        this.canDropBlade = false;
        this.canApproachHer = false;
        mainScript.runSection("collapseStart");

        Condition stuckExplore = new Condition();
        InverseCondition noStuckExplore = stuckExplore.getInverse();
        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "blade", "(Explore) I still have the blade. Maybe I can still end this.", this.hasBlade, noStuckExplore));
        activeMenu.add(new Option(this.manager, "wiggle", "(Explore) Come on, you have to give us something! Just a little wiggle room.", noStuckExplore));
        activeMenu.add(new Option(this.manager, "struggle", "(Explore) [Struggle to free yourself.]"));
        activeMenu.add(new Option(this.manager, "die", "(Explore) So we're dying down here. How long is that going to take?", activeMenu.get("struggle"), stuckExplore));
        activeMenu.add(new Option(this.manager, "beg", "\"We can't both be stuck here! This can't be how it ends. You must be able to free us.\""));
        activeMenu.add(new Option(this.manager, "both", "\"This is both of our faults. I'm sorry.\""));
        activeMenu.add(new Option(this.manager, "mine", "\"This is my fault. I should have done things differently. I'm sorry.\""));
        activeMenu.add(new Option(this.manager, "yours", "\"Yes. This is your fault.\""));
        activeMenu.add(new Option(this.manager, "nobody", "\"This isn't anybody's fault.\""));
        activeMenu.add(new Option(this.manager, "narrator", "\"I know whose fault this is, and it isn't ours.\""));
        activeMenu.add(new Option(this.manager, "wait", "[Wait in silence for your end.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "blade":
                case "wiggle":
                    stuckExplore.set();
                case "struggle":
                case "die":
                    mainScript.runSection(activeOutcome + "Stuck");
                    break;

                case "beg":
                case "yours":
                case "narrator":
                case "wait":
                    this.repeatActiveMenu = false;
                    mainScript.runSection(activeOutcome + "Stuck");
                    break;

                case "both":
                case "mine":
                case "nobody":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("okayStuck");
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // You die
        mainScript.runSection("starveEnd");
        return ChapterEnding.HUNGERPANGS;
    }


    // - Chapter III: The Wild -

    /**
     * Runs Chapter III: The Wild
     * @return the ending reached by the player
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

        if (this.hasVoice(Voice.HUNTED)) {
            this.source = "beast";
        } else {
            this.source = "witch";
        }

        mainScript.runSection();

        boolean pushEarlyJoin = true;
        Condition askedNarrator = new Condition();
        InverseCondition noAskNarrator = askedNarrator.getInverse();
        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "askNarrator", "(Explore) This... thing watching us. What is He?"));
        activeMenu.add(new Option(this.manager, "princessAskA", "I've had enough of this guy. How do we stop him?", askedNarrator));
        activeMenu.add(new Option(this.manager, "pushA", "Okay. Let's say I want to stop her. What do I do? I feel like I can't do much of anything right now.", askedNarrator));
        activeMenu.add(new Option(this.manager, "passiveA", "[Passively exist.]", this.hasVoice(Voice.CONTRARIAN)));
        activeMenu.add(new Option(this.manager, "passiveB", "Why should anyone do anything right now? This is fine! I like being this."));
        activeMenu.add(new Option(this.manager, "passiveWitch", "Why are you being nice to me? Don't you hate me? Don't we sort of hate each other?", source.equals("witch")));
        activeMenu.add(new Option(this.manager, "passiveBeast", "Why are you being nice to me? Aren't you a monster? Didn't you eat me?", source.equals("beast")));
        activeMenu.add(new Option(this.manager, "pushB", "He's right! I don't want a passive existence. I want things to do. So someone give me some options!", this.hasVoice(Voice.CONTRARIAN), noAskNarrator));
        activeMenu.add(new Option(this.manager, "princessAskB", "I can feel the pressure of the outside pushing in on us. What are we supposed to do about it?"));
        activeMenu.add(new Option(this.manager, "princessAskC", "This is how we're supposed to be. But what do we do now?", noAskNarrator));
        activeMenu.add(new Option(this.manager, "abomination", "Whatever we are right now is an abomination, and I want out!"));
        activeMenu.add(new Option(this.manager, "notMe", "I don't like this. I'm supposed to be me, and you're supposed to be something that isn't me."));
        activeMenu.add(new Option(this.manager, "pushC", "Okay, you, Narrator. How do I stop her?"));
        activeMenu.add(new Option(this.manager, "passiveC", "[Do nothing.]", !this.hasVoice(Voice.CONTRARIAN)));

        VoiceDialogueLine pExclusiveOverride = new VoiceDialogueLine(Voice.PRINCESS, "This is what we are. There is no other path.");
        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu, pExclusiveOverride);
            switch (activeOutcome) {
                case "askNarrator":
                    askedNarrator.set();
                    mainScript.runSection("askNarrator");
                    break;

                case "passiveA":
                case "passiveB":
                case "passiveC":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("passive");
                    break;

                case "abomination":
                case "notMe":
                    this.repeatActiveMenu = false;
                    mainScript.runSection(activeOutcome);
                    break;

                case "pushA":
                case "pushB":
                case "pushC":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("pushMenu");
                    break;

                case "princessAskA":
                case "princessAskB":
                case "princessAskC":
                    mainScript.runSection("princessAsk");

                    this.activeMenu = new OptionsMenu(true);
                    activeMenu.add(new Option(this.manager, "remember", "[Remember how it felt.]"));
                    activeMenu.add(new Option(this.manager, "freedom", "[Turn inwards and find your freedom.]"));

                    switch (parser.promptOptionsMenu(activeMenu, pExclusiveOverride)) {
                        case "freedom":
                            this.wildNetworked(pExclusiveOverride);
                            return ChapterEnding.GLIMPSEOFSOMETHING;

                        default:
                            this.repeatActiveMenu = false;
                            pushEarlyJoin = false;
                            break;
                    }

                    break;

                case "passiveWitch":
                case "passiveBeast":
                    this.repeatActiveMenu = false;
                    pushEarlyJoin = false;
                    break;
            }
        }

        // The Narrator pushes the player to separate from the Princess
        mainScript.runConditionalSection(this.source + "Push", pushEarlyJoin);

        String gazeDisplayChange = (source.equals("witch")) ? "hatred" : "terror";
        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "narrator", "[Gaze at the " + gazeDisplayChange + " in your heart.]"));
        activeMenu.add(new Option(this.manager, "princess", "[Bury it. Now. Before it's too late.]"));

        if (parser.promptOptionsMenu(activeMenu, pExclusiveOverride).equals("narrator")) {
            mainScript.runSourceSection("Gaze");
            return this.wildWounded();
        }

        // Bury the feeling
        mainScript.runSection("pushBury");
        
        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "narrator", "But the past does exist. I remember it."));
        activeMenu.add(new Option(this.manager, "princess", "[Turn inwards and find your freedom.]"));

        if (parser.promptOptionsMenu(activeMenu, pExclusiveOverride).equals("narrator")) {
            return this.wildWounded();
        } else {
            this.wildNetworked(pExclusiveOverride);
            return ChapterEnding.GLIMPSEOFSOMETHING;
        }
    }

    /**
     * The player chooses to remain merged with the Princess, leading to the "A Glimpse of Something Bigger" ending and claiming the Networked Wild
     * @param pExclusiveOverride The line used during an exclusive options menu during this Chapter
     */
    private void wildNetworked(VoiceDialogueLine pExclusiveOverride) {
        mainScript.runSection("networkStart");

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "cont", "[There is a place you need to be. You just need to find it.]"));
        parser.promptOptionsMenu(activeMenu, pExclusiveOverride);
        mainScript.runSection();

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "cont", "I trust you. [Find the way out.]"));
        parser.promptOptionsMenu(activeMenu, pExclusiveOverride);
        mainScript.runSection();

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "cont", "[Shatter the cage.]"));
        parser.promptOptionsMenu(activeMenu, pExclusiveOverride);
        mainScript.runSection();
    }

    /**
     * The player separates from the Princess
     * @return the ending reached by the player
     */
    private ChapterEnding wildWounded() {
        this.currentLocation = GameLocation.CABIN;
        this.knowsBlade = true;
        this.hasBlade = true;
        this.withPrincess = true;
        this.canSlayPrincess = true;

        mainScript.runSection(this.source + "FallApart");
        mainScript.runSection(this.source + "FallApartCont");
        mainScript.runSection(this.source + "CabinStart");

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "free", "\"I never wanted to kill you. Not really. But we can't be the same thing as each other. I had to put an end to whatever happened to us.\" [Cut her free.]"));
        activeMenu.add(new Option(this.manager, "slay", "[Slay the Princess.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "free":
                    mainScript.runSection("woundedFree");
                    return ChapterEnding.WOUNDSAVE;

                case "cSlayPrincess":
                case "slay":
                    mainScript.runSection("woundedSlay");
                    return ChapterEnding.WOUNDSLAY;

                default: this.giveDefaultFailResponse();
            }
        }
        
        throw new RuntimeException("No ending reached");
    }


    // - Chapter III: The Thorn -

    /**
     * Runs Chapter III: The Thorn
     * @return the ending reached by the player
     */
    private ChapterEnding thorn() {
        /*
          Possible combinations:
            - Opportunist + Smitten
            - Opportunist + Cheated
         */

        mainScript.runSection();

        boolean askedLooping = false;
        Condition noIntentions = new Condition(true);
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "different", "(Explore) This place is different. It keeps changing."));
        activeMenu.add(new Option(this.manager, "intentFree", "(Explore) We're going to free her.", noIntentions));
        activeMenu.add(new Option(this.manager, "intentNeutral", "(Explore) We're even now. I'm sure she understands that. But we can see what she has to say for herself when we get to the cabin.", noIntentions));
        activeMenu.add(new Option(this.manager, "intentHarsh", "(Explore) I hope you don't think I'm planning on freeing her after she stabbed me in the heart.", noIntentions));
        activeMenu.add(new Option(this.manager, "leave", "(Explore) Screw the cabin, what happens if we just leave?"));
        activeMenu.add(new Option(this.manager, "looping", "(Explore) You sure seem to be taking the whole \"looping\" thing in stride."));
        activeMenu.add(new Option(this.manager, "proceedA", "No matter what happens next, it seems like all our answers are in the cabin. Let's see this through. [Proceed to the cabin.]"));
        activeMenu.add(new Option(this.manager, "proceedB", "[Silently proceed to the cabin.]"));
        activeMenu.add(new Option(this.manager, "abort", this.cantTryAbort, "I'm done with this. Bye! [Turn around and leave.]", 0));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "intentFree":
                case "intentNeutral":
                case "intentHarsh":
                    noIntentions.set(false);
                case "different":
                case "leave":
                    mainScript.runSection(activeOutcome + "Woods");
                    break;

                case "looping":
                    askedLooping = true;
                    mainScript.runSection("loopingWoods");
                    break;

                case "cGoHill":
                case "proceedA":
                case "proceedB":
                    this.repeatActiveMenu = false;
                    break;

                case "cGoLeave":
                    if (cantTryAbort.check()) {
                        parser.printDialogueLine("You have already tried that.");
                        break;
                    }
                case "abort":
                    if (!this.canAbort) {
                        cantTryAbort.set();
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }

                    mainScript.runSection("abort");
                    this.abortVessel(true);
                    return ChapterEnding.ABORTED;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Proceed to the cabin
        this.currentLocation = GameLocation.HILL;
        mainScript.runConditionalSection("hillDialogue", askedLooping);

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "proceed", "[Proceed into the cabin.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "cGoCabin":
                case "proceed":
                    this.repeatActiveMenu = false;
                    break;
                
                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Enter the cabin
        this.currentLocation = GameLocation.CABIN;
        this.knowsBlade = true;
        this.mirrorPresent = true;
        mainScript.runSection("cabinIntro");

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "explore", "(Explore) How do we even get down there? The only thing I see is that mirror."));
        activeMenu.add(new Option(this.manager, "approach", "[Approach the mirror.]"));

        boolean mentionedMirror = false;
        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "explore":
                    mentionedMirror = true;
                    mainScript.runSection("mirrorExplore");
                    break;

                case "cGoStairs":
                case "cApproachMirror":
                case "approach":
                    this.repeatActiveMenu = false;
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Approach the mirror
        this.currentLocation = GameLocation.MIRROR;
        mainScript.runSection("approachMirror");

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "wipe", "[Wipe the mirror clean.]"));
        
        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "cWipe":
                case "wipe":
                    this.repeatActiveMenu = false;
                    break;

                case "cApproachAtMirrorFail":
                    this.giveDefaultFailResponse("cApproachAtMirrorFail");
                    break;

                default: super.giveDefaultFailResponse();
            }
        }

        // Wipe the mirror clean
        this.currentLocation = GameLocation.CABIN;
        this.mirrorPresent = false;
        this.touchedMirror = true;
        mainScript.runConditionalSection("wipeMirror", mentionedMirror);

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "enter", "[Enter the basement.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "cGoStairs":
                case "enter":
                    this.repeatActiveMenu = false;
                    break;

                default: this.giveDefaultFailResponse();
            }
        }

        // Enter the basement
        this.currentLocation = GameLocation.STAIRS;
        mainScript.runSection("stairsStart");

        Condition noStairsExplore = new Condition(true);
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "wayOut", "(Explore) \"I want to figure out a way out of here. For good.\"", noStairsExplore));
        activeMenu.add(new Option(this.manager, "dunno", "(Explore) \"I don't know what I want. I never really chose to come here.\"", noStairsExplore));
        activeMenu.add(new Option(this.manager, "free", "(Explore) \"I want to free you. I mean it.\"", noStairsExplore));
        activeMenu.add(new Option(this.manager, "talk", "(Explore) \"I just want to talk. Really talk.\"", noStairsExplore));
        activeMenu.add(new Option(this.manager, "proceed", "[Proceed down the stairs.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "wayOut":
                case "dunno":
                case "free":
                case "talk":
                    noStairsExplore.set(false);
                    mainScript.runSection("stairsExplore");

                case "cGoBasement":
                case "proceed":
                    this.repeatActiveMenu = false;
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Proceed down the stairs
        this.currentLocation = GameLocation.BASEMENT;
        this.withBlade = true;
        this.withPrincess = true;
        mainScript.runSection("basementStart");

        boolean bladeAttempt = false;
        boolean canTakeBlade = false;
        Condition notHostile = new Condition(true);
        Condition canBladeAttempt = new Condition();
        Condition tookBlade = new Condition();
        InverseCondition noBlade = tookBlade.getInverse();
        Condition notFirstChoice = new Condition();
        InverseCondition firstChoice = notFirstChoice.getInverse();
        Condition introGiven = new Condition();
        InverseCondition noIntroGiven = introGiven.getInverse();
        Condition noBladeAsk = new Condition(true);

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "laugh", "(Explore) \"Yeah. I'm here to laugh. What did you think would happen after you killed me? Did you think I wouldn't hold it against you?\"", firstChoice));
        activeMenu.add(new Option(this.manager, "intro", "(Explore) \"I'm not here to laugh. I'm here to free you. If you'll let me.\"", noIntroGiven, notHostile, noBlade));
        activeMenu.add(new Option(this.manager, "follow", "(Explore) \"You're not the only one who yearns for freedom. I'm as trapped as you are. I think we need to leave together.\"", introGiven, notHostile, noBlade));
        activeMenu.add(new Option(this.manager, "offer", "(Explore) \"I can cut you free. But you'll have to give me the blade.\"", noBlade, noIntroGiven));
        activeMenu.add(new Option(this.manager, "change", "(Explore) \"Is there nothing I can say to change your mind?\"", notFirstChoice, noBladeAsk, noBlade));
        activeMenu.add(new Option(this.manager, "bladeAsk", "(Explore) \"Can I take the blade now?\"", activeMenu.get("follow"), notHostile, noBlade));
        activeMenu.add(new Option(this.manager, "bladeA", "(Explore) \"Then maybe it's past time for either of us to say anything. All that counts is action.\" [Reach for the blade.]", activeMenu.get("change"), notHostile, noBladeAsk, noBlade));
        activeMenu.add(new Option(this.manager, "bladeB", "(Explore) [Reach for the blade.]", 0, canBladeAttempt, noBlade));
        activeMenu.add(new Option(this.manager, "leaveA", "\"You're in a prison of your own making. I broke our cycle of violence. If you still want to wallow in it, be my guest.\" [Turn and leave.]", noBlade));
        activeMenu.add(new Option(this.manager, "leaveB", "\"I guess I don't have anything left to say to you.\" [Turn and leave.]", noBlade));
        activeMenu.add(new Option(this.manager, "leaveC", "\"Fine. If you're going to be like that, I'm going to leave. Have a nice life.\" [Turn and leave.]", noBlade));
        activeMenu.add(new Option(this.manager, "free", "[Cut her free.]", tookBlade));
        activeMenu.add(new Option(this.manager, "slay", "[Slay the Princess.]", tookBlade));
        activeMenu.add(new Option(this.manager, "leaveBlade", "\"I just wanted my blade back. You're on your own.\" [Turn and leave.]", tookBlade));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "laugh":
                    notHostile.set(false);
                case "intro":
                case "offer":
                    notFirstChoice.set();
                    introGiven.set();
                    mainScript.runConditionalSection(activeOutcome, notHostile);
                    break;

                case "follow":
                    canTakeBlade = true;
                    mainScript.runSection("follow");
                    break;

                case "change":
                    mainScript.runConditionalSection("change", notHostile);
                    break;

                case "bladeAsk":
                    noBladeAsk.set(false);
                    mainScript.runSection("bladeAsk");
                    break;

                case "bladeA":
                    this.thornBladeAttempt(bladeAttempt, true, notHostile, canBladeAttempt, tookBlade);
                    break;

                case "cTakeBlade":
                    if (!canBladeAttempt.check()) {
                        mainScript.runSection();
                    }
                case "bladeB":
                    this.thornBladeAttempt(bladeAttempt, canTakeBlade, notHostile, canBladeAttempt, tookBlade);
                    bladeAttempt = true;
                    break;

                case "leaveA":
                case "leaveB":
                case "leaveC":
                case "leaveBlade":
                    return this.thornLeave();

                case "free":
                    return this.thornFree();

                case "cSlayPrincess":
                case "slay":
                    mainScript.runSection("slayStart");
                    return ChapterEnding.TRUSTISSUESSLAY;
                
                case "cSlayPrincessNoBladeFail":
                    mainScript.runSection("noBladeSlay");
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        throw new RuntimeException("No ending reached");
    }

    /**
     * The player attempts to take the blade from the Thorn
     * @param bladeAttempt whether or not the player has already attempted to take the blade
     * @param canTakeBlade whether or not the player can successfully take the blade
     * @param notHostile whether or not the Princess is already hostile towards the player
     * @param canBladeAttempt whether or not the player can attempt to take the blade
     * @param tookBlade whether or not the player has taken the blade from the Princess
     */
    private void thornBladeAttempt(boolean bladeAttempt, boolean canTakeBlade, Condition notHostile, Condition canBladeAttempt, Condition tookBlade) {
        if (canTakeBlade) {
            tookBlade.set();
            this.hasBlade = true;
            this.canSlayPrincess = true;

            mainScript.runConditionalSection("takeBlade", bladeAttempt);
        } else {
            if (bladeAttempt) {
                notHostile.set(false);
                canBladeAttempt.set(false);
                mainScript.runSection("bladeFail2");
            } else {
                mainScript.runSection("bladeFail1");
            }
        }
    }

    /**
     * The player cuts the Thorn free
     * @return the ending reached by the player
     */
    private ChapterEnding thornFree() {
        this.canSlayPrincess = false;
        mainScript.runSection("freeStart");

        boolean thornKiss = false;
        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "ofCourse", "\"Of course I did.\""));
        activeMenu.add(new Option(this.manager, "hate", "\"I just really hate the people who put us here.\""));
        activeMenu.add(new Option(this.manager, "kiss", "[Kiss her.]", this.hasVoice(Voice.SMITTEN)));
        activeMenu.add(new Option(this.manager, "silent", "[Remain silent.]"));

        switch (parser.promptOptionsMenu(activeMenu)) {
            case "hate":
                mainScript.runSection("freeHate");
            case "ofCourse":
                mainScript.runSection("freeSmileLeave");
                break;

            case "kiss":
                thornKiss = true;
                
            case "silent":
                mainScript.runSection("freeLeave");
                break;
        }

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "cut", "[Cut into the thorns.]"));
        activeMenu.add(new Option(this.manager, "step", "[Step into the thorns.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "cut":
                    this.repeatActiveMenu = false;
                    mainScript.runConditionalSection("cutFree", thornKiss);
                    break;

                case "cGoStairs":
                case "step":
                    this.repeatActiveMenu = false;
                    mainScript.runConditionalSection("stepFree", thornKiss);

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Leave the basement
        this.currentLocation = GameLocation.CABIN;
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "step", "[Step into your freedom.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (activeOutcome) {
                case "cGoHill":
                case "step":
                    this.repeatActiveMenu = false;
                    break;
            }
        }

        // Leave the cabin
        mainScript.runSection();

        if (thornKiss) {
            return ChapterEnding.NEWLEAFKISS;
        } else {
            return ChapterEnding.NEWLEAF;
        }
    }

    /**
     * The player attempts to leave the Thorn in the basement
     * @return the ending reached by the player
     */
    private ChapterEnding thornLeave() {
        mainScript.runSection("leaveStart");

        if (this.hasBlade) return ChapterEnding.ABANDONMENT;
        
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "rush", "[Rush for the blade.]"));
        activeMenu.add(new Option(this.manager, "nothing", "[Do nothing.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "cTakeBlade":
                case "rush":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("leaveRush");
                    break;

                case "nothing":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("leaveNothing");
                    break;

                default: super.giveDefaultFailResponse();
            }
        }

        return ChapterEnding.TRUSTISSUES;
    }


    // - Chapter III: The Cage -

    /**
     * Runs Chapter III: The Cage
     * @return the ending reached by the player
     */
    private ChapterEnding cage() {
        /*
          Possible combinations:
            - Skeptic + Broken
            - Skeptic + Paranoid
            - Skeptic + Cheated
         */

        mainScript.runSection();

        Condition noLastTimeExplore = new Condition(true);
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "blade", "(Explore) Should we talk about how we still have the blade? We've never started with the blade before."));
        activeMenu.add(new Option(this.manager, "void", "(Explore) She died last time, and the way you decided to thank us was locking us away in a happiness void forever.", this.prisonerGoodEndingSeen, noLastTimeExplore));
        activeMenu.add(new Option(this.manager, "sure", "(Explore) Are you sure we even need to slay her? She cut her own head off last time.", noLastTimeExplore));
        activeMenu.add(new Option(this.manager, "happy", "(Explore) I was happy with eternal bliss last time.", this.prisonerGoodEndingSeen, noLastTimeExplore));
        activeMenu.add(new Option(this.manager, "toss", "(Explore) I'm not taking the blade again. [Toss your weapon into the woods.]", activeMenu.get("blade")));
        activeMenu.add(new Option(this.manager, "proceedA", "No way out but through. Let's go see her. [Proceed to the cabin.]"));
        activeMenu.add(new Option(this.manager, "proceedB", "[Silently proceed to the cabin.]"));
        activeMenu.add(new Option(this.manager, "abort", this.cantTryAbort, "I'm done with this. Bye! [Turn around and leave.]", 0));

        boolean tossBladeAttempt = false;
        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "blade":
                    this.canDropBlade = true;
                    this.canThrowBlade = true;
                    mainScript.runSection("bladeWoods");
                    break;

                case "void":
                case "sure":
                case "happy":
                    noLastTimeExplore.set(false);
                    mainScript.runSection(activeOutcome + "Woods");
                    break;

                case "cDrop":
                case "cThrow":
                case "toss":
                    tossBladeAttempt = this.cageTossBladeAttempt();
                    this.canDropBlade = false;
                    this.canThrowBlade = false;
                    break;

                case "cGoHill":
                case "proceedA":
                case "proceedB":
                    this.repeatActiveMenu = false;
                    break;

                case "cGoLeave":
                    if (cantTryAbort.check()) {
                        parser.printDialogueLine("You have already tried that.");
                        break;
                    }
                case "abort":
                    if (!this.canAbort) {
                        cantTryAbort.set();
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }

                    mainScript.runSection("abort");
                    this.abortVessel(true);
                    return ChapterEnding.ABORTED;

                case "cDropNoBladeFail":
                case "cThrowNoBladeFail":
                    mainScript.runSection("alreadyTossed");
                    break;

                case "cDropFail":
                case "cThrowFail":
                    mainScript.runSection("tossSecondAttempt");
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Proceed to the cabin
        this.currentLocation = GameLocation.HILL;
        mainScript.runSection("hillDialogue");

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "proceed", "[Proceed into the cabin.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "cGoCabin":
                case "proceed":
                    this.repeatActiveMenu = false;
                    break;
                
                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Enter the cabin
        this.currentLocation = GameLocation.CABIN;
        this.mirrorPresent = true;
        mainScript.runSection("cabinIntro");

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

        // Approach the mirror, falling into the basement
        this.currentLocation = GameLocation.BASEMENT;
        this.mirrorPresent = false;
        this.touchedMirror = true;
        mainScript.runSection("approachMirror");

        boolean moveAttempt = false;
        this.canSlayPrincess = true;
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "talkA", "\"Hello?\""));
        activeMenu.add(new Option(this.manager, "talkB", "\"Are you mad at me?\""));
        activeMenu.add(new Option(this.manager, "talkC", "\"So you can come back too.\""));
        activeMenu.add(new Option(this.manager, "talkD", "\"Look, I'm sorry about what happened last time. I'm... supposed to be sorry, right?\""));
        activeMenu.add(new Option(this.manager, "talkE", "\"Why the hell did you cut your head off?\""));
        activeMenu.add(new Option(this.manager, "attack", "[Attack her.]", this.hasBlade));
        activeMenu.add(new Option(this.manager, "wait", "[Wait for her to make the first move.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "talkA":
                case "talkB":
                case "talkC":
                case "talkD":
                case "talkE":
                    mainScript.runSection("");

                    this.activeMenu = new OptionsMenu();
                    activeMenu.add(new Option(this.manager, "talkA", "(Explore) \"Hello?\""));
                    activeMenu.add(new Option(this.manager, "talkB", "(Explore) \"I'm talking to you.\""));
                    activeMenu.add(new Option(this.manager, "talkC", "(Explore) \"I'm sorry, okay? I'm sorry!\""));
                    activeMenu.add(new Option(this.manager, "talkD", "(Explore) \"Look, I'm sorry about what happened last time. I'm... supposed to be sorry, right?\""));
                    activeMenu.add(new Option(this.manager, "attack", "(Explore) \"Screw it.\" [Attack her.]", this.hasBlade));
                    activeMenu.add(new Option(this.manager, "free", "(Explore) [Try to slip free.]", !this.hasVoice(Voice.PARANOID)));
                    activeMenu.add(new Option(this.manager, "cut", "(Explore) [Try to cut yourself free.]", this.hasVoice(Voice.PARANOID)));
                    activeMenu.add(new Option(this.manager, "silent", "(Explore) [Remain silent.]"));

                    while (repeatActiveMenu) {
                        switch (parser.promptOptionsMenu(activeMenu)) {
                            case "talkA":
                            case "talkB":
                            case "talkC":
                            case "talkD":
                                this.repeatActiveMenu = false;
                                break;

                            case "cSlayPrincess":
                            case "attack":
                                mainScript.runSection("attackAttempt");
                            case "free":
                                this.repeatActiveMenu = false;
                                moveAttempt = true;
                                break;

                            case "cut":
                                this.repeatActiveMenu = false;
                                this.cageCutRoute = true;
                                mainScript.runSection("cut1");
                                break;

                            default: this.giveDefaultFailResponse();
                        }
                    }

                    break;

                case "cSlayPrincess":
                case "attack":
                    this.repeatActiveMenu = false;
                    moveAttempt = true;
                    mainScript.runSection("attackAttempt");
                    break;

                case "wait":
                    this.repeatActiveMenu = false;
                    break;

                default: this.giveDefaultFailResponse();
            }
        }

        mainScript.runConditionalSection("encounterStart", moveAttempt);

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "help", "(Explore) \"What is your problem with me? I've only ever tried to help you.\""));
        activeMenu.add(new Option(this.manager, "fix", "(Explore) \"Let me down! Killing me isn't going to fix anything.\""));
        activeMenu.add(new Option(this.manager, "noBlade", "(Explore) \"Look, I didn't even bring a knife. I just wanted to talk to you.\"", !this.hasBlade));
        activeMenu.add(new Option(this.manager, "happened", "(Explore) \"What... happened to you?\""));
        activeMenu.add(new Option(this.manager, "head", "(Explore) \"You cut your own head off last time. You can't be mad at me about that!\""));
        activeMenu.add(new Option(this.manager, "cutFail", "(Explore) [Attempt to cut yourself free.]", this.hasBlade && !this.cageCutRoute));
        activeMenu.add(new Option(this.manager, "swing", "(Explore) [Swing your blade.]"));
        activeMenu.add(new Option(this.manager, "cutCont", "(Explore) [Keep cutting.]", this.cageCutRoute));
        activeMenu.add(new Option(this.manager, "silent", "(Explore) [Remain silent.]"));

        boolean helpMentioned = false;
        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "help":
                    helpMentioned = true;
                case "fix":
                case "happened":
                case "head":
                    this.repeatActiveMenu = false;
                    this.cageCutRoute = false;
                    mainScript.runSection(activeOutcome + "Menu1");
                    break;

                case "noBlade":
                    this.repeatActiveMenu = false;
                    this.cageNoBladeMentioned = true;
                    mainScript.runSection("noBladeMenu1");
                    break;

                case "cutFail":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("cutFailMenu1");
                    break;

                case "cSlayPrincess":
                case "swing":
                    this.repeatActiveMenu = false;
                    this.cageCutRoute = false;
                    mainScript.runSection("swingMenu1");
                    break;

                case "cutCont":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("cut2");
                    break;

                case "silent":
                    this.repeatActiveMenu = false;
                    this.cageCutRoute = false;
                    mainScript.runSection("silentMenu1");
                    break;

                default: this.giveDefaultFailResponse();
            }
        }

        this.canDropBlade = true;
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "cut", "[Cut yourself free.]", this.cageCutRoute));
        activeMenu.add(new Option(this.manager, "none", "(Explore) \"None of this is my fault. I haven't done anything to you except give you my weapon, twice.\""));
        activeMenu.add(new Option(this.manager, "body", "(Explore) \"'She?' Are you talking about your body as if it's a separate person? It's you.\""));
        activeMenu.add(new Option(this.manager, "help", "(Explore) \"Nothing that's happened to you is my fault. Again, I keep trying to help you.\"", helpMentioned));
        activeMenu.add(new Option(this.manager, "watcher", "(Explore) \"What do you mean, you're just a watcher? You cut your own head off last time. You made that decision.\""));
        activeMenu.add(new Option(this.manager, "choices", "(Explore) \"You've made choices. And you've never stopped. You're choosing violence right now.\""));
        activeMenu.add(new Option(this.manager, "drop", "(Explore) [Drop the blade.]", this.hasBlade));
        activeMenu.add(new Option(this.manager, "silent", "(Explore) [Say nothing.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "cut":
                    mainScript.runSection("cut3");
                    this.cageSlay();
                    return ChapterEnding.RIDDLEOFSTEEL;

                case "none":
                case "body":
                case "help":
                case "watcher":
                case "choices":
                case "silent":
                    this.repeatActiveMenu = false;
                    this.cageCutRoute = false;
                    mainScript.runSection(activeOutcome + "Menu2");
                    break;

                case "cDrop":
                case "drop":
                    this.repeatActiveMenu = false;
                    this.hasBlade = false;
                    this.cageCutRoute = false;
                    mainScript.runSection("dropMenu2");
                    break;

                default: this.giveDefaultFailResponse();
            }
        }

        if (this.threwBlade) return this.cageEmpty();

        this.hasBlade = false;
        this.canDropBlade = false;
        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "forced", "(Explore) \"I didn't want to take the knife with me. I was *forced* to.\"", tossBladeAttempt && this.hasBlade));
        activeMenu.add(new Option(this.manager, "setup", "(Explore) \"You're setting me up to fail. I can't make real choices strung up like this, and you're refusing to make any choices at all.\""));
        activeMenu.add(new Option(this.manager, "will", "(Explore) \"Us choosing the same moves every time we play isn't an indictment of anyone's free will. It's just how things happened to play out. I could have thrown away that knife in the woods if I wanted to. But I chose to come here with it.\"", !tossBladeAttempt));
        activeMenu.add(new Option(this.manager, "supposed", "(Explore) \"What was I supposed to have done? Did you want me to carry your severed head out of the cabin with me? I thought you were *dead.*\""));
        activeMenu.add(new Option(this.manager, "beg", "(Explore) \"Please. Just stop walking forward. I'm going to die if you don't stop.\""));
        activeMenu.add(new Option(this.manager, "silent", "(Explore) [Say nothing.]"));

        // You are decapitated
        mainScript.runSection(parser.promptOptionsMenu(activeMenu) + "Menu3");
        this.removeVoice(Voice.NARRATOR);

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "ripped", "\"I didn't let go. You ripped my head off.\""));
        activeMenu.add(new Option(this.manager, "do", "\"What are we supposed to do now?\""));
        activeMenu.add(new Option(this.manager, "pattern", "\"You didn't cut yourself free. My body is still the one with the knife. Doesn't that mean this is different? Doesn't that mean I broke your pattern?\""));
        activeMenu.add(new Option(this.manager, "head", "\"This isn't nicer! I'm a severed head.\""));
        activeMenu.add(new Option(this.manager, "guess", "\"I thought that would be the end of me. I guess this isn't so bad.\""));
        activeMenu.add(new Option(this.manager, "silent", "[Say nothing.]"));

        mainScript.runSection(parser.promptOptionsMenu(activeMenu) + "Decapitate");
        return ChapterEnding.NOEXIT;
    }

    /**
     * The player attempts to toss the blade into the woods in The Cage
     * @return true if the player was forced to keep the blade by the Voices of the Paranoid and Skeptic; false if they chose to either toss or keep the blade
     */
    private boolean cageTossBladeAttempt() {
        mainScript.runSection("tossStart");

        this.subMenu = new OptionsMenu();
        subMenu.add(new Option(this.manager, "toss1", "[Do it. Toss the blade.]"));
        subMenu.add(new Option(this.manager, "toss2", "I'm in charge, and I say we're tossing the blade. You had your chance last time, and we all saw how that worked out for us.", subMenu.get("toss1")));
        subMenu.add(new Option(this.manager, "keep", "Fine. Have it your way. I'll keep it."));

        boolean secondChoice = false;
        boolean repeatMenu = true;
        while (repeatMenu) {
            switch (parser.promptOptionsMenu(subMenu)) {
                case "cDrop":
                case "cThrow":
                case "toss1":
                case "toss2":
                    if (secondChoice) {
                        repeatMenu = false;
                    } else {
                        secondChoice = true;
                        mainScript.runSection();
                    }

                    break;

                case "keep":
                    mainScript.runSection("tossKeepBlade");
                    return false;

                default: this.giveDefaultFailResponse();
            }
        }

        // Choose to toss the blade, again, twice
        if (this.hasVoice(Voice.PARANOID)) {
            mainScript.runSection("paraSkepMutiny");

            this.subMenu = new OptionsMenu(true);
            subMenu.add(new Option(this.manager, "fakeA", true, "Are you seriously not letting me toss it?"));
            subMenu.add(new Option(this.manager, "fakeB", true, "Real mature."));
            subMenu.add(new Option(this.manager, "fakeC", true, "When this goes sideways, it's on the two of you."));
            subMenu.add(new Option(this.manager, "keep", "Fine. Then I guess we're keeping it."));

            parser.promptOptionsMenu(subMenu);
            mainScript.runSection();
            return true;
        } else {
            this.hasBlade = false;
            this.threwBlade = true;
            mainScript.runSection("tossBlade");
            return false;
        }
    }

    /**
     * The player is given the chance to free the Cage after tossing the blade in the woods
     * @return the ending reached by the player
     */
    private ChapterEnding cageEmpty() {
        mainScript.runConditionalSection("emptyStart", this.cageNoBladeMentioned);

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "where", "\"Yeah, is that right? I bring you the knife? Where do you think it is?\""));
        activeMenu.add(new Option(this.manager, "already", "\"You clearly weren't listening to me earlier, because I already told you I threw away that knife. I ditched it before I even got to the cabin.\"", this.cageNoBladeMentioned));
        activeMenu.add(new Option(this.manager, "chose", "\"There's no grand scheme guiding us along a predetermined path. We're both agents of chaos who make our own choices. And this time, I chose to talk to you. Unarmed.\""));
        activeMenu.add(new Option(this.manager, "pattern", "\"Your pattern's broken. I'm unarmed.\""));
        activeMenu.add(new Option(this.manager, "silent", "[Silently wave your empty hands.]"));

        mainScript.runSection(parser.promptOptionsMenu(activeMenu) + "EmptyStart");

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "you", "\"You've been the one listening to me. You made yourself stop. And now you can decide to finally let me down.\""));
        activeMenu.add(new Option(this.manager, "guard", "\"What do you actually want? The only way we can get out of here is if you drop your guard. Just like I did.\""));
        activeMenu.add(new Option(this.manager, "know", "\"I think you know what it's supposed to mean.\""));
        activeMenu.add(new Option(this.manager, "talk", "\"Well? Are you ready to talk?\""));
        activeMenu.add(new Option(this.manager, "silent", "[Say nothing.]"));

        mainScript.runSection(parser.promptOptionsMenu(activeMenu) + "EmptyCont");

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "take", "[Take your Princess from her cage.]"));
        parser.promptOptionsMenu(activeMenu);

        mainScript.runSection();

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "take", "[Take hold of the chain.]"));
        parser.promptOptionsMenu(activeMenu);
        
        mainScript.runSection();

        this.currentLocation = GameLocation.CABIN;
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "will", "(Explore) \"You know, it's funny. All of this talk about free will, and I feel like I'm locked into this outcome.\""));
        activeMenu.add(new Option(this.manager, "leave", "[Step into your freedom.]"));
        activeMenu.add(new Option(this.manager, "drop", "[Drop her.]", activeMenu.get("will")));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "will":
                    mainScript.runSection("willExploreEmpty");
                    break;

                case "cGoHill":
                case "leave":
                    mainScript.runSection("freeEnd");
                    return ChapterEnding.ALLEGORYOFCAGE;

                case "drop":
                    mainScript.runSection("willEnd");
                    return ChapterEnding.FREEWILL;

                default: mainScript.runSection("emptyEndDefault");
            }
        }

        throw new RuntimeException("No ending reached");
    }

    /**
     * The player cuts themselves free from the chains and slays the Cage
     */
    private void cageSlay() {
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "fakeA", true, "The pieces fall into place."));
        activeMenu.add(new Option(this.manager, "fakeB", true, "For every permutation, there is a pattern."));
        activeMenu.add(new Option(this.manager, "fakeC", true, "And when the pattern is learned, the illusion dissolves."));
        activeMenu.add(new Option(this.manager, "fakeD", true, "But was the pattern always there, or were you the ones who wrote it in the sand?"));
        activeMenu.add(new Option(this.manager, "fakeE", true, "The writers must follow their own script."));
        activeMenu.add(new Option(this.manager, "give", "[Give her your implement.]"));

        this.canGiveBlade = true;
        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "cGiveBlade":
                case "give":
                    this.repeatActiveMenu = false;
                    break;

                default: this.giveDefaultFailResponse();
            }
        }

        // Give her your implement
        this.canGiveBlade = false;
        mainScript.runSection();
    }


    // - Chapter III: The Grey -

    /**
     * Runs Chapter III: The Grey (coming from the Prisoner)
     * @return the ending reached by the player
     */
    private ChapterEnding grey() {
        /*
          You gain the Voice of the Cold
          Possible combinations:
            - Skeptic + Cold (from Prisoner)
            - Smitten + Cold (from Damsel)
         */

        if (this.hasVoice(Voice.SMITTEN)) {
            this.source = "burned";
        } else {
            this.source = "drowned";
        }

        mainScript.runConditionalSection(this.source + "Start", this.prisonerHeartStopped);

        Condition noDifferentAsk = new Condition(true);
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "rain", "(Explore) It's raining. It wasn't raining last time. Or the time before that. The whole path is different.", source.equals("drowned"), noDifferentAsk));
        activeMenu.add(new Option(this.manager, "different", "(Explore) We haven't talked enough about how different this place is. It wasn't different last time.", noDifferentAsk));
        activeMenu.add(new Option(this.manager, "noCabin", "(Explore) What happens if we don't go to the cabin?"));
        activeMenu.add(new Option(this.manager, "charge", "(Explore) I'm the one in charge here, and if we slay her again, you are not going to make us kill ourself. Is that clear?", source.equals("burned")));
        activeMenu.add(new Option(this.manager, "noWant", "(Explore) I'll have you know that I didn't want to kill myself last time.", this.prisonerHeartStopped));
        activeMenu.add(new Option(this.manager, "proceed", "Whatever happens next, it seems like all our answers are in the cabin. We might as well see this through. [Proceed to the cabin.]"));
        activeMenu.add(new Option(this.manager, "abort", this.cantTryAbort, "I'm done with this. Bye! [Turn around and leave.]", 0));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "rain":
                    mainScript.runSection("rainPath");
                case "different":
                    noDifferentAsk.set(false);
                    mainScript.runSourceSection("DifferentPath");
                    break;

                case "noCabin":
                    mainScript.runSection("noCabinPath");
                    break;
                    
                case "charge":
                case "noWant":
                    mainScript.runSection(activeOutcome + "Path");

                case "cGoHill":
                case "proceed":
                    this.repeatActiveMenu = false;
                    break;

                case "cGoLeave":
                    if (this.cantTryAbort.check()) {
                        parser.printDialogueLine("You have already tried that.");
                    }
                case "abort":
                    if (manager.nClaimedVessels() >= 2) {
                        cantTryAbort.set();
                        parser.printDialogueLine(CANTSTRAY);
                        break;
                    }

                    mainScript.runSection("abort");
                    this.abortVessel(true);
                    return ChapterEnding.ABORTED;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Continue to the cabin
        this.currentLocation = GameLocation.HILL;
        mainScript.runSourceSection("Hill");

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "proceed", "[Proceed into the cabin.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "cGoCabin":
                case "proceed":
                    this.repeatActiveMenu = false;
                    break;

                default: this.giveDefaultFailResponse();
            }
        }

        // Enter the cabin
        this.currentLocation = GameLocation.CABIN;
        this.knowsBlade = true;
        this.mirrorPresent = true;
        mainScript.runSourceSection("Cabin");

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "explore", "(Explore) But there is no door."));
        activeMenu.add(new Option(this.manager, "approach", "[Approach the mirror.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "explore":
                    mainScript.runSourceSection("AskMirror");
                    break;

                case "cApproachMirror":
                case "cGoStairs":
                case "approach":
                    this.repeatActiveMenu = false;
                    break;

                default: super.giveDefaultFailResponse();
            }
        }

        // Approach the mirror
        mainScript.runSection("approachMirror");

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "wipe", "[Wipe the mirror clean.]"));
        
        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "cWipe":
                case "wipe":
                    this.repeatActiveMenu = false;
                    break;

                case "cApproachAtMirrorFail":
                    this.giveDefaultFailResponse("cApproachAtMirrorFail");
                    break;

                default: super.giveDefaultFailResponse();
            }
        }
        
        // Wipe the mirror clean
        this.touchedMirror = true;
        this.mirrorPresent = false;
        mainScript.runSection("wipeMirror");
        
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "enter", "[Enter the basement.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "cGoStairs":
                case "enter":
                    this.repeatActiveMenu = false;
                    break;

                default: this.giveDefaultFailResponse();
            }
        }

        // Enter the basement
        this.currentLocation = GameLocation.STAIRS;
        mainScript.runSourceSection("StairsStart");

        Condition noStairsExplore = new Condition(true);
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "sorry", "(Explore) \"I'm sorry about last time! Are we good?\"", noStairsExplore));
        activeMenu.add(new Option(this.manager, "anyone", "(Explore) \"Is anyone there?\"", noStairsExplore));
        activeMenu.add(new Option(this.manager, "talk", "(Explore) \"I think we have a lot to talk about.\"", noStairsExplore));
        activeMenu.add(new Option(this.manager, "weapon", "(Explore) \"I don't have a weapon. There wasn't anything upstairs for me when I got here.\"", noStairsExplore));
        activeMenu.add(new Option(this.manager, "proceed", "[Proceed down the stairs.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "sorry":
                case "anyone":
                case "talk":
                    noStairsExplore.set(false);
                    mainScript.runSection("stairsExploreA");
                    break;

                case "weapon":
                    noStairsExplore.set(false);
                    mainScript.runSection("stairsExploreB");
                    break;

                case "cGoBasement":
                case "proceed":
                    this.repeatActiveMenu = false;
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Continue down the stairs
        this.currentLocation = GameLocation.BASEMENT;
        this.withBlade = true;
        this.withPrincess = true;
        mainScript.runSourceSection("BasementStart");

        GlobalInt deathTimer = new GlobalInt();
        boolean incrementFlag;
        NumCondition timer0 = new NumCondition(deathTimer, 0);
        InverseCondition timerIncremented = timer0.getInverse();
        Condition burnedTogetherComment = new Condition();
        boolean burnedGrudgeComment;
        this.activeMenu = new OptionsMenu();

        if (source.equals("drowned")) {
            burnedGrudgeComment = true;
            activeMenu.add(new Option(this.manager, "Why", "(Explore) \"Why did you close the door?\"", timer0));
            activeMenu.add(new Option(this.manager, "Kill", "(Explore) \"Let me out! Are you trying to kill me?\""));
            activeMenu.add(new Option(this.manager, "Die", "(Explore) \"I'm going to drown!\"", timerIncremented));
            activeMenu.add(new Option(this.manager, "Wrong", "(Explore) \"What's wrong with you? I don't want this!\""));
            activeMenu.add(new Option(this.manager, "Even", "(Explore) \"I only killed you after you killed me first! We're even now! We don't need to do this again.\""));
            activeMenu.add(new Option(this.manager, "Beg", "(Explore) \"Please! I'm begging you! I'll do anything, just don't let me drown!\""));
            activeMenu.add(new Option(this.manager, "Sorry", "(Explore) \"Is this about last time? I'm sorry! Now can you let me out?\""));
        } else {
            burnedGrudgeComment = false;
            activeMenu.add(new Option(this.manager, "Why", "(Explore) \"Why did you close the door?\""));
            activeMenu.add(new Option(this.manager, "Kill", "(Explore) \"Let me out! Are you trying to kill me?\""));
            activeMenu.add(new Option(this.manager, "Die", "(Explore) \"I'm going to burn!\""));
            activeMenu.add(new Option(this.manager, "Wrong", "(Explore) \"What's wrong with you? I don't want this!\"", burnedTogetherComment));
            activeMenu.add(new Option(this.manager, "Beg", "(Explore) \"Please! I'm begging you! I'll do anything, just don't let me burn!\""));
            activeMenu.add(new Option(this.manager, "Sorry", "(Explore) \"Are you mad at me for killing you? I'm sorry!\""));
        }
        activeMenu.add(new Option(this.manager, "Blade", "[Rush for the blade.]"));
        activeMenu.add(new Option(this.manager, "Door", "[Rush to the door.]"));

        while (deathTimer.lessThan(3)) {
            incrementFlag = true;

            this.activeOutcome = parser.promptOptionsMenu(activeMenu);

            // Redirect to rush options
            if (activeOutcome.equals("cTakeBlade")) {
                this.activeOutcome = "Blade";
            } else if (activeOutcome.equals("cGoStairs")) {
                this.activeOutcome = "Door";
            }

            switch (activeOutcome) {
                case "Why":
                    if (source.equals("burned")) {
                        burnedTogetherComment.set();
                        mainScript.runSection("burnedWhy");
                    }

                    break;

                case "Kill":
                    mainScript.runConditionalSection(this.source + "Kill", burnedTogetherComment);
                    burnedTogetherComment.set();
                    break;

                case "Die":
                    if (source.equals("burned") || deathTimer.equals(1)) {
                        mainScript.runSection(source + "Die");
                    }

                    break;

                case "Wrong":
                case "Even":
                case "Beg":
                case "Sorry":
                    if (source.equals("burned") || deathTimer.equals(1)) {
                        mainScript.runConditionalSection(this.source + activeOutcome, burnedGrudgeComment);
                        burnedGrudgeComment = true;
                    }

                    break;

                case "Blade":
                case "Door":
                    incrementFlag = false;
                    deathTimer.set(3); // Fast-forward
                    mainScript.runSourceSection(activeOutcome);
                    break;

                default:
                    incrementFlag = false;
                    this.giveDefaultFailResponse(activeOutcome);
            }

            if (incrementFlag) {
                deathTimer.increment();
                mainScript.runSourceSection("Timer" + deathTimer);
            }
        }

        // Run out of time
        mainScript.runSourceSection("End");
        switch (this.source) {
            case "drowned": return ChapterEnding.ANDALLTHISLONGING;
            default: return ChapterEnding.BURNINGDOWNTHEHOUSE;
        }
    }


    // - Epilogue: Happily Ever After -

    /**
     * Runs Chapter III: Happily Ever After
     * @return the ending reached by the player
     */
    private ChapterEnding happilyEverAfter() {
        /*
          You lose the Voice of the Smitten
          Possible combinations:
            - Skeptic
            - Opportunist
         */






        // temporary templates for copy-and-pasting
        /*
        parser.printDialogueLine("XXXXX");
        parser.printDialogueLine(new PrincessDialogueLine("XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "(Explore) XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "(Explore) \"XXXXX\""));
        activeMenu.add(new Option(this.manager, "q1", "XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "\"XXXXX\""));
        */

        // PLACEHOLDER
        return null;
    }
}
