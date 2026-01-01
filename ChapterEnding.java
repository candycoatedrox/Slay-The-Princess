public enum ChapterEnding {
    /*
    ChapterEnding names are pulled from, in order:
      1. achievement names;
      2. if there is no achievement for the ending, the name given by the unofficial wiki;
      3. if the wiki does not make a distinction between endings, whatever name I think is appropriate
    */

    // The Hero and the Princess
    TOADVERSARY(Chapter.ADVERSARY, Voice.STUBBORN),
    TOTOWER(Chapter.TOWER, Voice.BROKEN),
    TOTOWERUNHARMED(Chapter.TOWER, Voice.BROKEN),
    TOTOWERPATHETIC(Chapter.TOWER, Voice.BROKEN),
    GOODENDING("goodEnding"),
    TOSPECTRE(Chapter.SPECTRE, Voice.COLD),
    TONIGHTMARE(Chapter.NIGHTMARE, Voice.PARANOID),
    TONIGHTMAREFLED(Chapter.NIGHTMARE, Voice.PARANOID),
    TORAZOR(Chapter.RAZOR, Voice.CHEATED),
    TORAZORMUTUAL(Chapter.RAZOR, Voice.CHEATED),
    TORAZORREVIVAL(Chapter.RAZOR, Voice.CHEATED),
    TOBEAST(Chapter.BEAST, Voice.HUNTED),
    TOWITCH(Chapter.WITCH, Voice.OPPORTUNIST),
    TOWITCHMUTUAL(Chapter.WITCH, Voice.OPPORTUNIST),
    TOWITCHBETRAYAL(Chapter.WITCH, Voice.OPPORTUNIST),
    TOWITCHLOCKED(Chapter.WITCH, Voice.OPPORTUNIST),
    TOSTRANGER(Chapter.STRANGER, Voice.CONTRARIAN),
    TOPRISONER(Chapter.PRISONER, Voice.SKEPTIC),
    TODAMSEL(Chapter.DAMSEL, Voice.SMITTEN),
    
    // The Adversary
    THATWHICHCANNOTDIE("advyFight", Vessel.ADVERSARY, -1, 1, false),
    STRIKEMEDOWN("advyPacifism", Chapter.FURY, Voice.COLD),
    HEARNOBELL("advyUnarmed", Chapter.FURY, Voice.CONTRARIAN),
    DEADISDEAD(Chapter.FURY, Voice.BROKEN),
    THREADINGTHROUGH(Chapter.NEEDLE, Voice.HUNTED),
    FREEINGSOMEONE(Chapter.NEEDLE, Voice.SKEPTIC),

    // The Eye of the Needle
    FAILEDFIGHT(Vessel.NEEDLE, -1, 0, false),
    FAILEDFLEE(Vessel.NEEDLE, -1, -1, false),
    WIDEOPENFIELD("needleFight", Vessel.NEEDLE, 1, 1, true),
    BLINDLEADINGBLIND("needleFree", Vessel.NEEDLE, 1, -1, false),

    // The Fury
    QUANTUMBEAK("furyTower", Vessel.FURY, "An Empty Void that Dared to Dream it Was Alive", -1, 1, true),
    HINTOFFEELING("furySlay", Vessel.FURY, 0, -2, true),
    LEAVEHERBEHIND(Vessel.FURY, 0, -1, true),
    NEWLEAFWEATHEREDBOOK("furyTogether", Vessel.FURY, 1, 1, false),
    GOINGTHEDISTANCE("furyContra", Vessel.FURY, "The Bell", -1, 1, true),
    IFYOUCOULDUNDERSTAND(Vessel.REWOUNDFURY, 0, 1, false),

    // The Tower
    OBEDIENTSERVANT("towerPledge", Vessel.TOWER, 1, 1, false),
    GODKILLER(Chapter.FURY, Voice.STUBBORN),
    APOBLADE(Chapter.APOTHEOSIS, Voice.CONTRARIAN),
    APOUNARMED(Chapter.APOTHEOSIS, Voice.PARANOID),

    // The Apotheosis
    GRACE("apoAlone", Vessel.APOTHEOSIS, 1, 1, false),
    WINDOWTOUNKNOWN("apoTendrils", Vessel.APOTHEOSIS, 0, -1, true),
    SOMETHINGTOREMEMBER("apoFightContra", Vessel.APOTHEOSIS, 0, -1, true),
    GODDESSUNRAVELED("apoFightParaEnd", Vessel.APOTHEOSIS, "A Tapestry Undone", 0, -1, true),

    // The Spectre
    HITCHHIKER("spectreFree", Vessel.SPECTRE, 1, 1, false),
    HEARTRIPPERLEAVE(Chapter.WRAITH, Voice.PARANOID),
    EXORCIST("spectreSlay", Chapter.DRAGON, Voice.OPPORTUNIST),
    HEARTRIPPER(Chapter.WRAITH, Voice.CHEATED),

    // The Princess and the Dragon
    PRINCESSANDDRAGON("dragonFuse", Vessel.PATD, 0, 1, false),
    WHATONCEWASONE("dragonFree", Vessel.PATD, 1, 1, false),
    OPPORTUNISTATHEART("dragonLeave", Vessel.PATD, "The Life-Taker", 0, -1, true),
    
    // The Wraith
    EXORCISTIII(Vessel.WRAITH, -1, -2, true),
    PASSENGER(Vessel.WRAITH, 1, -1, false),
    
    // The Nightmare
    WORLDOFTERROR(Vessel.NIGHTMARE, 1, 1, false),
    HOUSEOFNOLEAVE(Chapter.WRAITH, Voice.COLD),
    TERMINALVELOCITY(Chapter.WRAITH, Voice.OPPORTUNIST),
    MONOLITHOFFEAR("nightStay", Chapter.CLARITY),

    // The Moment of Clarity
    MOMENTOFCLARITY(Vessel.CLARITY, 1, 0, false),

    // The Razor
    TOARMSRACEFIGHT(Chapter.ARMSRACE, Voice.STUBBORN),
    TOARMSRACEBORED(Chapter.ARMSRACE, Voice.BROKEN),
    TOARMSRACELEFT(Chapter.ARMSRACE, Voice.PARANOID),
    TONOWAYOUTBORED(Chapter.NOWAYOUT, Voice.BROKEN),
    TONOWAYOUTLEFT(Chapter.NOWAYOUT, Voice.PARANOID),

    // The Arms Race
    TOMUTUALLYASSURED(Chapter.MUTUALLYASSURED),

    // Mutually Assured Destruction
    WATERSTEEL(Vessel.RAZORFULL, -1, 1, true),

    // No Way Out
    TOEMPTYCUP(Chapter.EMPTYCUP),

    // The Empty Cup
    FORMLESS(Vessel.RAZORHEART, -1, 1, true),

    // The Beast
    DISSOLVINGWILL("beastFree", Vessel.BEAST, 1, 1, false),
    DISSOLVINGWILLACCIDENT(Vessel.BEAST, 1, 1, false),
    FIGHT(Chapter.DEN, Voice.STUBBORN),
    FLIGHT(Chapter.DEN, Voice.SKEPTIC),
    OPOSSUM(Chapter.WILD, Voice.CONTRARIAN),
    AHAB(Chapter.WILD, Voice.OPPORTUNIST),
    SLAYYOURSELF(Chapter.WILD, Voice.STUBBORN),
    DISSOLVED(Chapter.WILD, Voice.BROKEN),

    // The Den
    INSTINCT("denInstinct", Vessel.DEN, "The Rhythm of the Flesh", 0, -1, false),
    HEROICSTRIKE("denStrike", Vessel.DEN, "The Rhythm of the Flesh", 0, 0, true),
    COUPDEGRACE("denCoup", Vessel.DEN, "The Rhythm of the Flesh", 0, 1, true),
    LIONANDMOUSE("denFree", Vessel.DEN, "Hand-in-Claw", 1, 1, false),
    HUNGERPANGS("denStarve", Vessel.DEN, -1, -1, false),
    UNANSWEREDQUESTIONS(Vessel.DEN, -1, 1, false),

    // The Wild
    WOUNDSLAY(Vessel.WOUNDEDWILD, -1, -1, true),
    WOUNDSAVE(Vessel.WOUNDEDWILD, -1, 1, false),
    GLIMPSEOFSOMETHING(Vessel.NETWORKWILD, 2, 1, false),

    // The Witch
    SCORPION(Vessel.WITCH, -1, -1, false),
    FROG(Vessel.WITCH, -1, -1, false),
    FROGLOCKED(Vessel.WITCH, -1, -1, false),
    KNIVESOUTMASKSOFF(Chapter.WILD, Voice.STUBBORN),
    KNIVESOUTMASKSOFFGIVEUP(Chapter.WILD, Voice.CHEATED),
    PLAYINGITSAFE(Chapter.WILD, Voice.PARANOID),
    PASTLIFEGAMBITSPECIAL("witchGive", Chapter.THORN, Voice.SMITTEN),
    PASTLIFEGAMBIT("witchGive", Chapter.THORN, Voice.CHEATED),

    // The Thorn
    NEWLEAF(Vessel.THORN, "The Thorn", 1, 1, false),
    NEWLEAFKISS(Vessel.THORN, "A Kiss From a Thorn", 1, 2, false),
    ABANDONMENT(Vessel.THORN, -1, -1, false),
    TRUSTISSUES(Vessel.THORN, -1, -1, false),
    TRUSTISSUESSLAY(Vessel.THORN, -1, -1, false),
    
    // The Stranger
    ILLUSIONOFCHOICE("strangerEnd", Vessel.STRANGER, 0, 0, true),

    // The Prisoner
    TALKINGHEADS("prisonerHead", Vessel.PRISONERHEAD, 1, 1, false),
    PRISONEROFMIND("prisonerChain", Vessel.PRISONER, 1, 0, false),
    COLDLYRATIONAL(Chapter.GREY, Voice.COLD),
    RESTLESSFORCED(Chapter.CAGE, Voice.CHEATED),
    RESTLESSSELF(Chapter.CAGE, Voice.PARANOID),
    RESTLESSGIVEIN(Chapter.CAGE, Voice.BROKEN),

    // The Cage
    NOEXIT("cageCycle", Vessel.WATCHFULCAGE, -1, 1, false),
    RIDDLEOFSTEEL("cageSlay", Vessel.WATCHFULCAGE, -1, 1, true),
    ALLEGORYOFCAGE("cageFree", Vessel.OPENCAGE, 1, 1, false),
    FREEWILL("cageDrop", Vessel.WATCHFULCAGE, 0, -1, true),

    // The Grey
    BURNINGDOWNTHEHOUSE(Vessel.BURNEDGREY, -1, 1, true),
    ANDALLTHISLONGING(Vessel.DROWNEDGREY, -1, -1, true),

    // The Damsel
    ROMANTICHAZE("damselFree", Vessel.DAMSEL, 1, 1, false),
    ANDTHEYLIVEDHAPPILY("damselDecon", Vessel.DECONDAMSEL, -1, 2, true),
    LADYKILLER(Chapter.GREY, Voice.COLD),
    CONTENTSOFOURHEARTDECON(Chapter.HAPPY, Voice.SKEPTIC),
    CONTENTSOFOURHEARTUPSTAIRS(Chapter.HAPPY, Voice.OPPORTUNIST),

    // Happily Ever After
    IMEANTIT("happyDance", Vessel.HAPPY, "I Meant It", 1, 2, false),
    LEFTCABIN(Vessel.HAPPY, 1, 1, false),
    FINALLYOVER("happySlay", Vessel.HAPPYDRY, 0, 1, true),
    DONTLETITGOOUT("happyStay", Vessel.HAPPY, 0, -1, false),

    // Misc.
    NEWCYCLE(Chapter.CH1),
    ABORTED,
    DEMOENDING,

    // The End of Everything (full game endings)
    OBLIVION("oblivion"),
    NOENDINGS("ascendEarly"),
    THROUGHCONFLICT("ascendLate"),
    PATHINTHEWOODS("resetEnd"),
    NEWANDUNENDINGDAWN("slayEnd"),
    ANDEVERYONEHATESYOU("slayOopsEnd"),
    WHATHAPPENSNEXT("leaveEnd"),
    YOURNEWWORLD;

    private final boolean isFinal;
    private String achievementID;

    private final Chapter nextChapter;
    private Voice newVoice;

    private Vessel vessel;
    private String playlistSong = "";
    private int freedom = 0;
    private int satisfaction = 0;
    private boolean yourNewWorld;

    // --- CONSTRUCTORS ---

    /**
     * Standard constructor with an achievement
     * @param achievementID the ID of the achievement tied to the ending
     * @param nextChapter the Chapter this ending leads to
     * @param newVoice the Voice gained at the start of the next Chapter
     */
    private ChapterEnding(String achievementID, Chapter nextChapter, Voice newVoice) {
        this.isFinal = false;
        this.achievementID = achievementID;
        this.nextChapter = nextChapter;
        this.newVoice = newVoice;
    }

    /**
     * Standard constructor
     * @param nextChapter the Chapter this ending leads to
     * @param newVoice the Voice gained at the start of the next Chapter
     */
    private ChapterEnding(Chapter nextChapter, Voice newVoice) {
        this("", nextChapter, newVoice);
    }

    /**
     * Constructor for gaining all Voices at the start of the next Chapter or the beginning of a Cycle with an achievement
     * @param achievementID the ID of the achievement tied to the ending
     * @param nextChapter the Chapter this ending leads to
     */
    private ChapterEnding(String achievementID, Chapter nextChapter) {
        this(achievementID, nextChapter, (Voice)null);
    }

    /**
     * Constructor for gaining all Voices at the start of the next Chapter or the beginning of a Cycle
     * @param nextChapter the Chapter this ending leads to
     */
    private ChapterEnding(Chapter nextChapter) {
        this("", nextChapter, (Voice)null);
    }

    /**
     * Constructor for special Chapters (aborting a Chapter or game endings) with an achievement
     * @param achievementID the ID of the achievement tied to the ending
     */
    private ChapterEnding(String achievementID) {
        this.isFinal = true;
        this.achievementID = achievementID;
        this.nextChapter = Chapter.CH1;
    }

    /**
     * Constructor for special Chapters (aborting a Chapter or game endings)
     */
    private ChapterEnding() {
        this("");
    }

    /**
     * Constructor for endings where a Vessel has been claimed with an achievement
     * @param achievementID the ID of the achievement tied to the ending
     * @param v the Vessel claimed in the ending
     * @param playlistSong the song the ending adds to the current playlist
     * @param freedom the amount the ending alters the Shifting Mound's freedom value
     * @param satisfaction the amount the ending alters the Shifting Mound's satisfaction value
     * @param yourNewWorld whether the ending qualifies for the "Your New World" ending or not
     */
    private ChapterEnding(String achievementID, Vessel v, String playlistSong, int freedom, int satisfaction, boolean yourNewWorld) {
        this.isFinal = true;
        this.achievementID = achievementID;
        this.nextChapter = Chapter.SPACESBETWEEN;

        this.vessel = v;
        this.playlistSong = playlistSong;
        this.freedom = freedom;
        this.satisfaction = satisfaction;
        this.yourNewWorld = yourNewWorld;
    }

    /**
     * Constructor for endings where a Vessel has been claimed
     * @param v the Vessel claimed in the ending
     * @param playlistSong the song the ending adds to the current playlist
     * @param freedom the amount the ending alters the Shifting Mound's freedom value
     * @param satisfaction the amount the ending alters the Shifting Mound's satisfaction value
     * @param yourNewWorld whether the ending qualifies for the "Your New World" ending or not
     */
    private ChapterEnding(Vessel v, String playlistSong, int freedom, int satisfaction, boolean yourNewWorld) {
        this.isFinal = true;
        this.nextChapter = Chapter.SPACESBETWEEN;

        this.vessel = v;
        this.playlistSong = playlistSong;
        this.freedom = freedom;
        this.satisfaction = satisfaction;
        this.yourNewWorld = yourNewWorld;
    }

    /**
     * Constructor for endings where a Vessel has been claimed with an achievement
     * @param achievementID the ID of the achievement tied to the ending
     * @param v the Vessel claimed in the ending
     * @param freedom the amount the ending alters the Shifting Mound's freedom value
     * @param satisfaction the amount the ending alters the Shifting Mound's satisfaction value
     * @param yourNewWorld whether the ending qualifies for the "Your New World" ending or not
     */
    private ChapterEnding(String achievementID, Vessel v, int freedom, int satisfaction, boolean yourNewWorld) {
        this(achievementID, v, "", freedom, satisfaction, yourNewWorld);
    }

    /**
     * Constructor for endings where a Vessel has been claimed
     * @param v the Vessel claimed in the ending
     * @param freedom the amount the ending alters the Shifting Mound's freedom value
     * @param satisfaction the amount the ending alters the Shifting Mound's satisfaction value
     * @param yourNewWorld whether the ending qualifies for the "Your New World" ending or not
     */
    private ChapterEnding(Vessel v, int freedom, int satisfaction, boolean yourNewWorld) {
        this(v, "", freedom, satisfaction, yourNewWorld);
    }

    // --- ACCESSORS ---

    /**
     * Accessor for isFinal
     * @return whether this ending represents the end of a Cycle or leads to a new Chapter
     */
    public boolean isFinal() {
        return this.isFinal;
    }

    /**
     * Accessor for achievementID
     * @return the ID of the achievement tied to this ending
     */
    public String getAchievementID() {
        return this.achievementID;
    }

    /**
     * Accessor for nextChapter
     * @return the Chapter that this ending leads to
     */
    public Chapter getNextChapter() {
        return this.nextChapter;
    }

    /**
     * Accessor for newVoice
     * @return the Voice gained at the start of the next Chapter
     */
    public Voice getNewVoice() {
        return this.newVoice;
    }

    /**
     * Accessor for vessel
     * @return the Vessel claimed in the ending
     */
    public Vessel getVessel() {
        return this.vessel;
    }

    /**
     * Accessor for playlistSong
     * @return the song this ending adds to the current playlist
     */
    public String getPlaylistSong() {
        return this.playlistSong;
    }

    /**
     * Accessor for freedom
     * @return the amount of freedom this ending adds to the Shifting Mound
     */
    public int getFreedom() {
        return this.freedom;
    }

    /**
     * Accessor for satisfaction
     * @return the amount of satisfaction this ending adds to the Shifting Mound
     */
    public int getSatisfaction() {
        return this.satisfaction;
    }

    /**
     * Accessor for yourNewWorld
     * @param yourNewWorld whether this ending qualifies for the "Your New World" ending or not
     */
    public boolean qualifiesYNW() {
        return this.yourNewWorld;
    }
}
