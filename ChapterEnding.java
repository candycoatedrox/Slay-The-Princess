public enum ChapterEnding {
    /*
    ChapterEnding names are pulled from, in order:
      1. achievement names;
      2. if there is no achievement for the ending, the name given by the unofficial wiki;
      3. if the wiki does not make a distinction between endings, whatever name I think is appropriate
    */

    // The Hero and the Princess
    TOADVERSARY("toAdversary", Chapter.ADVERSARY, Voice.STUBBORN),
    TOTOWER("toTower", Chapter.TOWER, Voice.BROKEN),
    TOTOWERUNHARMED("toTowerUnharmed", Chapter.TOWER, Voice.BROKEN),
    TOTOWERPATHETIC("toTowerPathetic", Chapter.TOWER, Voice.BROKEN),
    GOODENDING("goodEnding", true),
    TOSPECTRE("toSpectre", Chapter.SPECTRE, Voice.COLD),
    TONIGHTMARE("toNightmare", Chapter.NIGHTMARE, Voice.PARANOID),
    TONIGHTMAREFLED("toNightmareFled", Chapter.NIGHTMARE, Voice.PARANOID),
    TORAZOR("toRazor", Chapter.RAZOR, Voice.CHEATED),
    TORAZORMUTUAL("toRazorMutual", Chapter.RAZOR, Voice.CHEATED),
    TORAZORREVIVAL("toRazorRevival", Chapter.RAZOR, Voice.CHEATED),
    TOBEAST("toBeast", Chapter.BEAST, Voice.HUNTED),
    TOWITCH("toWitch", Chapter.WITCH, Voice.OPPORTUNIST),
    TOWITCHMUTUAL("toWitchMutual", Chapter.WITCH, Voice.OPPORTUNIST),
    TOWITCHBETRAYAL("toWitchBetrayal", Chapter.WITCH, Voice.OPPORTUNIST),
    TOWITCHLOCKED("toWitchLocked", Chapter.WITCH, Voice.OPPORTUNIST),
    TOSTRANGER("toStranger", Chapter.STRANGER, Voice.CONTRARIAN),
    TOPRISONER("toPrisoner", Chapter.PRISONER, Voice.SKEPTIC),
    TODAMSEL("toDamsel", Chapter.DAMSEL, Voice.SMITTEN),
    
    // The Adversary
    THATWHICHCANNOTDIE("advyFight", true, Vessel.ADVERSARY, -1, 1, false),
    STRIKEMEDOWN("advyPacifism", true, Chapter.FURY, Voice.COLD),
    HEARNOBELL("advyUnarmed", true, Chapter.FURY, Voice.CONTRARIAN),
    DEADISDEAD("advyDead", Chapter.FURY, Voice.BROKEN),
    DEADISDEADUPSTAIRS("advyUpstairs", Chapter.FURY, Voice.BROKEN),
    THREADINGTHROUGH("advyDodge", Chapter.NEEDLE, Voice.HUNTED),
    FREEINGSOMEONE("advyFree", Chapter.NEEDLE, Voice.SKEPTIC),

    // The Eye of the Needle
    FAILEDFIGHT("needleFightFail", Vessel.NEEDLE, -1, 0, false),
    FAILEDFLEE("needleFreeFail", Vessel.NEEDLE, -1, -1, false),
    WIDEOPENFIELD("needleFight", true, Vessel.NEEDLE, 1, 1, true),
    BLINDLEADINGBLIND("needleFree", true, Vessel.NEEDLE, 1, -1, false),

    // The Fury
    QUANTUMBEAK("furyTower", true, Vessel.FURY, "An Empty Void that Dared to Dream it Was Alive", -1, 1, true),
    HINTOFFEELING("furySlay", true, Vessel.FURY, 0, -2, true),
    LEAVEHERBEHIND("furyLeave", Vessel.FURY, 0, -1, true),
    NEWLEAFWEATHEREDBOOK("furyTogether", true, Vessel.FURY, 1, 1, false),
    GOINGTHEDISTANCE("furyContra", true, Vessel.FURY, "The Bell", -1, 1, true),
    IFYOUCOULDUNDERSTAND("furyRewound", Vessel.REWOUNDFURY, 0, 1, false),

    // The Tower
    OBEDIENTSERVANT("towerPledge", true, Vessel.TOWER, 1, 1, false),
    GODKILLER("towerSlay", Chapter.FURY, Voice.STUBBORN),
    APOBLADE("towerApoContra", Chapter.APOTHEOSIS, Voice.CONTRARIAN),
    APOUNARMED("towerApoPara", Chapter.APOTHEOSIS, Voice.PARANOID),

    // The Apotheosis
    GRACE("apoAlone", true, Vessel.APOTHEOSIS, 1, 1, false),
    WINDOWTOUNKNOWN("apoTendrils", true, Vessel.APOTHEOSIS, 0, -1, true),
    SOMETHINGTOREMEMBER("apoFightContra", true, Vessel.APOTHEOSIS, 0, -1, true),
    GODDESSUNRAVELED("apoFightParaEnd", true, Vessel.APOTHEOSIS, "A Tapestry Undone", 0, -1, true),

    // The Spectre
    HITCHHIKER("spectreFree", true, Vessel.SPECTRE, 1, 1, false),
    HEARTRIPPERLEAVE("spectreLeave", Chapter.WRAITH, Voice.PARANOID),
    EXORCIST("spectreSlay", true, Chapter.DRAGON, Voice.OPPORTUNIST),
    HEARTRIPPER("spectreOffend", Chapter.WRAITH, Voice.CHEATED),

    // The Princess and the Dragon
    PRINCESSANDDRAGON("dragonFuse", true, Vessel.PATD, 0, 1, false),
    WHATONCEWASONE("dragonFree", true, Vessel.PATD, 1, 1, false),
    OPPORTUNISTATHEART("dragonLeave", true, Vessel.PATD, "The Life-Taker", 0, -1, true),
    
    // The Wraith
    EXORCISTIII("wraithAbyss", Vessel.WRAITH, -1, -2, true),
    PASSENGER("wraithFree", Vessel.WRAITH, 1, -1, false),
    
    // The Nightmare
    WORLDOFTERROR("nightFree", Vessel.NIGHTMARE, 1, 1, false),
    HOUSEOFNOLEAVE("nightStuck", Chapter.WRAITH, Voice.COLD),
    TERMINALVELOCITY("nightFall", Chapter.WRAITH, Voice.OPPORTUNIST),
    MONOLITHOFFEAR("nightStay", true, Chapter.CLARITY),

    // The Moment of Clarity
    MOMENTOFCLARITY("clarityEnd", Vessel.CLARITY, 1, 0, false),

    // The Razor
    TOARMSRACEFIGHT("razorFight", Chapter.ARMSRACE, Voice.STUBBORN),
    TOARMSRACEBORED("razorBoredHunted", Chapter.ARMSRACE, Voice.BROKEN),
    TOARMSRACELEFT("razorLeaveHunted", Chapter.ARMSRACE, Voice.PARANOID),
    TONOWAYOUTBORED("razorBoredContra", Chapter.NOWAYOUT, Voice.BROKEN),
    TONOWAYOUTLEFT("razorLeaveContra", Chapter.NOWAYOUT, Voice.PARANOID),

    // The Arms Race
    TOMUTUALLYASSURED("razor3Blade", Chapter.MUTUALLYASSURED),

    // Mutually Assured Destruction
    WATERSTEEL("razor4Blade", Vessel.RAZORFULL, -1, 1, true),

    // No Way Out
    TOEMPTYCUP("razor3Empty", Chapter.EMPTYCUP),

    // The Empty Cup
    FORMLESS("razor4Empty", Vessel.RAZORHEART, -1, 1, true),

    // The Beast
    DISSOLVINGWILL("beastFree", true, Vessel.BEAST, 1, 1, false),
    DISSOLVINGWILLACCIDENT("beastFree", "beastFreeAccidental", Vessel.BEAST, 1, 1, false),
    FIGHT("beastFight", Chapter.DEN, Voice.STUBBORN),
    FLIGHT("beastFlight", Chapter.DEN, Voice.SKEPTIC),
    OPOSSUM("beastPlayDead", Chapter.WILD, Voice.CONTRARIAN),
    AHAB("beastSlay", Chapter.WILD, Voice.OPPORTUNIST),
    SLAYYOURSELF("beastSuicide", Chapter.WILD, Voice.STUBBORN),
    DISSOLVED("beastDissolve", Chapter.WILD, Voice.BROKEN),

    // The Den
    INSTINCT("denInstinct", true, Vessel.DEN, "The Rhythm of the Flesh", 0, -1, false),
    HEROICSTRIKE("denStrike", true, Vessel.DEN, "The Rhythm of the Flesh", 0, 0, true),
    COUPDEGRACE("denCoup", true, Vessel.DEN, "The Rhythm of the Flesh", 0, 1, true),
    LIONANDMOUSE("denFree", true, Vessel.DEN, "Hand-in-Claw", 1, 1, false),
    HUNGERPANGS("denStarve", true, Vessel.DEN, -1, -1, false),
    UNANSWEREDQUESTIONS("denMisc", Vessel.DEN, -1, 1, false),

    // The Wild
    WOUNDSLAY("wildSlay", Vessel.WOUNDEDWILD, -1, -1, true),
    WOUNDSAVE("wildFree", Vessel.WOUNDEDWILD, -1, 1, false),
    GLIMPSEOFSOMETHING("wildNetwork", Vessel.NETWORKWILD, 2, 1, false),

    // The Witch
    SCORPION("witchBetray", Vessel.WITCH, -1, -1, false),
    FROG("witchBetrayed", Vessel.WITCH, -1, -1, false),
    FROGLOCKED("witchLocked", Vessel.WITCH, -1, -1, false),
    KNIVESOUTMASKSOFF("witchFight", Chapter.WILD, Voice.STUBBORN),
    KNIVESOUTMASKSOFFGIVEUP("witchFightEscape", Chapter.WILD, Voice.CHEATED),
    PLAYINGITSAFE("witchLeave", Chapter.WILD, Voice.PARANOID),
    PASTLIFEGAMBITSPECIAL("witchGive", true, Chapter.THORN, Voice.SMITTEN),
    PASTLIFEGAMBIT("witchGive", true, Chapter.THORN, Voice.CHEATED),

    // The Thorn
    NEWLEAF("thornFree", Vessel.THORN, "The Thorn", 1, 1, false),
    NEWLEAFKISS("thornFreeKiss", Vessel.THORN, "A Kiss From a Thorn", 1, 2, false),
    ABANDONMENT("thornLeaveBlade", Vessel.THORN, -1, -1, false),
    TRUSTISSUES("thornLeave", Vessel.THORN, -1, -1, false),
    TRUSTISSUESSLAY("thornSlay", Vessel.THORN, -1, -1, false),
    
    // The Stranger
    ILLUSIONOFCHOICE("strangerEnd", true, Vessel.STRANGER, 0, 0, true),

    // The Prisoner
    TALKINGHEADS("prisonerHead", true, Vessel.PRISONERHEAD, 1, 1, false),
    PRISONEROFMIND("prisonerChain", true, Vessel.PRISONER, 1, 0, false),
    COLDLYRATIONAL("prisonerSlay", Chapter.GREY, Voice.COLD),
    RESTLESSFORCED("prisonerHeart", Chapter.CAGE, Voice.CHEATED),
    RESTLESSSELF("prisonerSuicide", Chapter.CAGE, Voice.PARANOID),
    RESTLESSGIVEIN("prisonerAccept", Chapter.CAGE, Voice.BROKEN),

    // The Cage
    NOEXIT("cageCycle", true, Vessel.WATCHFULCAGE, -1, 1, false),
    RIDDLEOFSTEEL("cageSlay", true, Vessel.WATCHFULCAGE, -1, 1, true),
    ALLEGORYOFCAGE("cageFree", true, Vessel.OPENCAGE, 1, 1, false),
    FREEWILL("cageDrop", true, Vessel.WATCHFULCAGE, 0, -1, true),

    // The Grey
    BURNINGDOWNTHEHOUSE("greyBurned", Vessel.BURNEDGREY, -1, 1, true),
    ANDALLTHISLONGING("greyDrowned", Vessel.DROWNEDGREY, -1, -1, true),

    // The Damsel
    ROMANTICHAZE("damselFree", true, Vessel.DAMSEL, 1, 1, false),
    ANDTHEYLIVEDHAPPILY("damselDecon", true, Vessel.DECONDAMSEL, -1, 2, true),
    LADYKILLER("damselSlay", Chapter.GREY, Voice.COLD),
    CONTENTSOFOURHEARTDECON("damselStayDecon", Chapter.HAPPY, Voice.SKEPTIC),
    CONTENTSOFOURHEARTUPSTAIRS("damselStayUpstairs", Chapter.HAPPY, Voice.OPPORTUNIST),

    // Happily Ever After
    IMEANTIT("happyDance", true, Vessel.HAPPY, "I Meant It", 1, 2, false),
    LEFTCABIN("happyFree", Vessel.HAPPY, 1, 1, false),
    FINALLYOVER("happySlay", true, Vessel.HAPPYDRY, 0, 1, true),
    DONTLETITGOOUT("happyStay", true, Vessel.HAPPY, 0, -1, false),

    // Misc.
    NEWCYCLE("start", Chapter.CH1),
    ABORTED("abort"),
    DEMOENDING("demo"),

    // The End of Everything (full game endings)
    OBLIVION("oblivion", true),
    NOENDINGS("ascendEarly", true),
    THROUGHCONFLICT("ascendLate", true),
    PATHINTHEWOODS("resetEnd", true),
    NEWANDUNENDINGDAWN("slayEnd", true),
    ANDEVERYONEHATESYOU("slayOopsEnd", true),
    WHATHAPPENSNEXT("leaveEnd", true),
    YOURNEWWORLD("yourNewWorld");

    private final String id;
    private final String achievementID;
    private final boolean isFinal;

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
     * @param id the internal ID for the ending
     * @param achievementID the ID of the Achievement gained from the ending at the end of the chapter
     * @param nextChapter the Chapter this ending leads to
     * @param newVoice the Voice gained at the start of the next Chapter
     */
    private ChapterEnding(String id, String achievementID, Chapter nextChapter, Voice newVoice) {
        this.id = id;
        this.achievementID = achievementID;
        this.isFinal = false;
        this.nextChapter = nextChapter;
        this.newVoice = newVoice;
    }

    /**
     * Standard constructor with an achievement
     * @param id the internal ID for the ending
     * @param hasAchievement whether or not the player earns an Achievement from the ending at the end of the chapter
     * @param nextChapter the Chapter this ending leads to
     * @param newVoice the Voice gained at the start of the next Chapter
     */
    private ChapterEnding(String id, boolean hasAchievement, Chapter nextChapter, Voice newVoice) {
        this.id = id;
        this.achievementID = (hasAchievement) ? id : "";
        this.isFinal = false;
        this.nextChapter = nextChapter;
        this.newVoice = newVoice;
    }

    /**
     * Standard constructor
     * @param nextChapter the Chapter this ending leads to
     * @param newVoice the Voice gained at the start of the next Chapter
     */
    private ChapterEnding(String id, Chapter nextChapter, Voice newVoice) {
        this(id, "", nextChapter, newVoice);
    }

    /**
     * Constructor for gaining all Voices at the start of the next Chapter or the beginning of a Cycle with an achievement
     * @param id the internal ID for the ending
     * @param achievementID the ID of the Achievement gained from the ending at the end of the chapter
     * @param nextChapter the Chapter this ending leads to
     */
    private ChapterEnding(String id, String achievementID, Chapter nextChapter) {
        this(id, achievementID, nextChapter, (Voice)null);
    }

    /**
     * Constructor for gaining all Voices at the start of the next Chapter or the beginning of a Cycle with an achievement
     * @param id the internal ID for the ending
     * @param hasAchievement whether or not the player earns an Achievement from the ending at the end of the chapter
     * @param nextChapter the Chapter this ending leads to
     */
    private ChapterEnding(String id, boolean hasAchievement, Chapter nextChapter) {
        this(id, hasAchievement, nextChapter, (Voice)null);
    }

    /**
     * Constructor for gaining all Voices at the start of the next Chapter or the beginning of a Cycle
     * @param id the internal ID for the ending
     * @param nextChapter the Chapter this ending leads to
     */
    private ChapterEnding(String id, Chapter nextChapter) {
        this(id, "", nextChapter, (Voice)null);
    }

    /**
     * Constructor for special Chapters (aborting a Chapter or game endings) with an achievement
     * @param id the internal ID for the ending
     * @param achievementID the ID of the Achievement gained from the ending at the end of the chapter
     */
    private ChapterEnding(String id, String achievementID) {
        this.id = id;
        this.isFinal = true;
        this.achievementID = achievementID;
        this.nextChapter = Chapter.CH1;
    }

    /**
     * Constructor for special Chapters (aborting a Chapter or game endings) with an achievement
     * @param id the internal ID for the ending
     * @param hasAchievement whether or not the player earns an Achievement from the ending at the end of the chapter
     */
    private ChapterEnding(String id, boolean hasAchievement) {
        this.id = id;
        this.isFinal = true;
        this.achievementID = (hasAchievement) ? id : "";
        this.nextChapter = Chapter.CH1;
    }

    /**
     * Constructor for special Chapters (aborting a Chapter or game endings)
     * @param id the internal ID for the ending
     */
    private ChapterEnding(String id) {
        this(id, "");
    }

    /**
     * Constructor for endings where a Vessel has been claimed with an achievement
     * @param id the internal ID for the ending
     * @param achievementID the ID of the Achievement gained from the ending at the end of the chapter
     * @param v the Vessel claimed in the ending
     * @param playlistSong the song the ending adds to the current playlist
     * @param freedom the amount the ending alters the Shifting Mound's freedom value
     * @param satisfaction the amount the ending alters the Shifting Mound's satisfaction value
     * @param yourNewWorld whether the ending qualifies for the "Your New World" ending or not
     */
    private ChapterEnding(String id, String achievementID, Vessel v, String playlistSong, int freedom, int satisfaction, boolean yourNewWorld) {
        this.id = id;
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
     * Constructor for endings where a Vessel has been claimed with an achievement
     * @param id the internal ID for the ending
     * @param hasAchievement whether or not the player earns an Achievement from the ending at the end of the chapter
     * @param v the Vessel claimed in the ending
     * @param playlistSong the song the ending adds to the current playlist
     * @param freedom the amount the ending alters the Shifting Mound's freedom value
     * @param satisfaction the amount the ending alters the Shifting Mound's satisfaction value
     * @param yourNewWorld whether the ending qualifies for the "Your New World" ending or not
     */
    private ChapterEnding(String id, boolean hasAchievement, Vessel v, String playlistSong, int freedom, int satisfaction, boolean yourNewWorld) {
        this.id = id;
        this.isFinal = true;
        this.achievementID = (hasAchievement) ? id : "";
        this.nextChapter = Chapter.SPACESBETWEEN;

        this.vessel = v;
        this.playlistSong = playlistSong;
        this.freedom = freedom;
        this.satisfaction = satisfaction;
        this.yourNewWorld = yourNewWorld;
    }

    /**
     * Constructor for endings where a Vessel has been claimed
     * @param id the internal ID for the ending
     * @param v the Vessel claimed in the ending
     * @param playlistSong the song the ending adds to the current playlist
     * @param freedom the amount the ending alters the Shifting Mound's freedom value
     * @param satisfaction the amount the ending alters the Shifting Mound's satisfaction value
     * @param yourNewWorld whether the ending qualifies for the "Your New World" ending or not
     */
    private ChapterEnding(String id, Vessel v, String playlistSong, int freedom, int satisfaction, boolean yourNewWorld) {
        this(id, "", v, playlistSong, freedom, satisfaction, yourNewWorld);
    }

    /**
     * Constructor for endings where a Vessel has been claimed with an achievement
     * @param id the internal ID for the ending
     * @param achievementID the ID of the Achievement gained from the ending at the end of the chapter
     * @param v the Vessel claimed in the ending
     * @param freedom the amount the ending alters the Shifting Mound's freedom value
     * @param satisfaction the amount the ending alters the Shifting Mound's satisfaction value
     * @param yourNewWorld whether the ending qualifies for the "Your New World" ending or not
     */
    private ChapterEnding(String id, String achievementID, Vessel v, int freedom, int satisfaction, boolean yourNewWorld) {
        this(id, achievementID, v, "", freedom, satisfaction, yourNewWorld);
    }

    /**
     * Constructor for endings where a Vessel has been claimed with an achievement
     * @param id the internal ID for the ending
     * @param hasAchievement whether or not the player earns an Achievement from the ending at the end of the chapter
     * @param v the Vessel claimed in the ending
     * @param freedom the amount the ending alters the Shifting Mound's freedom value
     * @param satisfaction the amount the ending alters the Shifting Mound's satisfaction value
     * @param yourNewWorld whether the ending qualifies for the "Your New World" ending or not
     */
    private ChapterEnding(String id, boolean hasAchievement, Vessel v, int freedom, int satisfaction, boolean yourNewWorld) {
        this(id, hasAchievement, v, "", freedom, satisfaction, yourNewWorld);
    }

    /**
     * Constructor for endings where a Vessel has been claimed
     * @param id the internal ID for the ending
     * @param v the Vessel claimed in the ending
     * @param freedom the amount the ending alters the Shifting Mound's freedom value
     * @param satisfaction the amount the ending alters the Shifting Mound's satisfaction value
     * @param yourNewWorld whether the ending qualifies for the "Your New World" ending or not
     */
    private ChapterEnding(String id, Vessel v, int freedom, int satisfaction, boolean yourNewWorld) {
        this(id, "", v, freedom, satisfaction, yourNewWorld);
    }

    // --- ACCESSORS ---

    /**
     * Accessor for id
     * @return the internal ID for this ending
     */
    @Override
    public String toString() {
        return this.id;
    }

    /**
     * Accessor for isFinal
     * @return whether this ending represents the end of a Cycle or leads to a new Chapter
     */
    public boolean isFinal() {
        return this.isFinal;
    }

    /**
     * Accessor for achievementID
     * @return the ID of the Achievement gained from the ending at the end of the chapter
     */
    public String getAchievementID() {
        return this.achievementID;
    }

    /**
     * Checks whether this ending has an associated Achievement
     * @return whether or not the player earns an Achievement from the ending at the end of the chapter
     */
    public boolean hasAchievement() {
        return !achievementID.isEmpty();
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
