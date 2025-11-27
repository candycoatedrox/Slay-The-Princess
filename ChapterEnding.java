public enum ChapterEnding {
    // The Hero and the Princess
    TOADVERSARY(Chapter.ADVERSARY, Voice.STUBBORN),
    TOTOWER(Chapter.TOWER, Voice.BROKEN),
    TOTOWERUNHARMED(Chapter.TOWER, Voice.BROKEN),
    GOODENDING,
    TOSPECTRE(Chapter.SPECTRE, Voice.COLD),
    TONIGHTMARE(Chapter.NIGHTMARE, Voice.PARANOID),
    TONIGHTMAREFLED(Chapter.NIGHTMARE, Voice.PARANOID),
    TORAZOR(Chapter.RAZOR, Voice.CHEATED),
    TORAZORMUTUAL(Chapter.RAZOR, Voice.CHEATED),
    TORAZORREVIVAL(Chapter.RAZOR, Voice.CHEATED),
    TOBEAST(Chapter.BEAST, Voice.HUNTED),
    TOWITCH(Chapter.WITCH, Voice.OPPORTUNIST),
    TOWITCHLOCKED(Chapter.WITCH, Voice.OPPORTUNIST),
    TOSTRANGER(Chapter.STRANGER, Voice.CONTRARIAN),
    TOPRISONER(Chapter.PRISONER, Voice.SKEPTIC),
    TODAMSEL(Chapter.DAMSEL, Voice.SMITTEN),
    
    // The Adversary
    STRIKEMEDOWN(Chapter.FURY, Voice.COLD),
    HEARNOBELL(Chapter.FURY, Voice.CONTRARIAN),
    THATWHICHCANNOTDIE(Vessel.ADVERSARY, false),
    DEADISDEAD(Chapter.FURY, Voice.BROKEN),
    THREADINGTHROUGH(Chapter.NEEDLE, Voice.HUNTED),
    FREEINGSOMEONE(Chapter.NEEDLE, Voice.SKEPTIC),

    // The Eye of the Needle
    FAILEDFIGHT(Vessel.NEEDLE, false),
    FAILEDFLEE(Vessel.NEEDLE, false),
    WIDEOPENFIELD(Vessel.NEEDLE, true),
    BLINDLEADINGBLIND(Vessel.NEEDLE, false),

    // The Fury
    QUANTUMBEAK(Vessel.FURY, true),
    HINTOFFEELING(Vessel.FURY, true),
    LEAVEHERBEHIND(Vessel.FURY, true),
    NEWLEAFWEATHEREDBOOK(Vessel.FURY, false),
    GOINGTHEDISTANCE(Vessel.FURY, true),
    IFYOUCOULDUNDERSTAND(Vessel.REWOUNDFURY, false),

    // The Tower
    OBEDIENTSERVANT(Vessel.TOWER, false),
    GODKILLER(Chapter.FURY, Voice.STUBBORN),
    APOBLADE(Chapter.APOTHEOSIS, Voice.CONTRARIAN),
    APOUNARMED(Chapter.APOTHEOSIS, Voice.PARANOID),

    // The Apotheosis
    SOMETHINGTOREMEMBER(Vessel.APOTHEOSIS, true),
    WINDOWTOUNKNOWN(Vessel.APOTHEOSIS, true),
    GODDESSUNRAVELED(Vessel.APOTHEOSIS, true),
    GRACE(Vessel.APOTHEOSIS, false),

    // The Spectre
    HITCHHIKER(Vessel.SPECTRE, false),
    HEARTRIPPERLEAVE(Chapter.WRAITH, Voice.PARANOID),
    HEARTRIPPER(Chapter.WRAITH, Voice.CHEATED),
    EXORCIST(Chapter.DRAGON, Voice.OPPORTUNIST),

    // The Princess and the Dragon
    WHATONCEWASONE(Vessel.PATD, false),
    PRINCESSANDDRAGON(Vessel.PATD, false),
    OPPORTUNISTATHEART(Vessel.PATD, true),
    
    // The Wraith
    PASSENGER(Vessel.WRAITH, false),
    EXORCISTIII(Vessel.WRAITH, true),
    
    // The Nightmare
    WORLDOFTERROR(Vessel.NIGHTMARE, false),
    HOUSEOFNOLEAVE(Chapter.WRAITH, Voice.COLD),
    TERMINALVELOCITY(Chapter.WRAITH, Voice.OPPORTUNIST),
    MONOLITHOFFEAR(Chapter.CLARITY),

    // The Moment of Clarity
    MOMENTOFCLARITY(Vessel.CLARITY, false),

    // The Razor
    TOARMSRACEFIGHT(Chapter.ARMSRACE, Voice.HUNTED, Voice.STUBBORN),
    TOARMSRACEBORED(Chapter.ARMSRACE, Voice.HUNTED, Voice.BROKEN),
    TOARMSRACELEFT(Chapter.ARMSRACE, Voice.HUNTED, Voice.PARANOID),
    TONOWAYOUTBORED(Chapter.ARMSRACE, Voice.CONTRARIAN, Voice.BROKEN),
    TONOWAYOUTLEFT(Chapter.ARMSRACE, Voice.CONTRARIAN, Voice.PARANOID),

    // The Arms Race
    TOMUTUALLYASSURED(Chapter.MUTUALLYASSURED),

    // Mutually Assured Destruction
    MUTUALLYASSURED(Vessel.RAZORFULL, true),

    // No Way Out
    TOEMPTYCUP(Chapter.EMPTYCUP),

    // The Empty Cup
    EMPTYCUP(Vessel.RAZORHEART, true),

    // The Beast
    DISSOLVINGWILL(Vessel.BEAST, false),
    FIGHT(Chapter.DEN, Voice.STUBBORN),
    FLIGHT(Chapter.DEN, Voice.SKEPTIC),
    OPOSSUM(Chapter.WILD, Voice.CONTRARIAN),
    AHAB(Chapter.WILD, Voice.OPPORTUNIST),
    SLAYYOURSELF(Chapter.WILD, Voice.STUBBORN),
    DISSOLVED(Chapter.WILD, Voice.BROKEN),

    // The Den
    HEROICSTRIKE(Vessel.DEN, true),
    COUPDEGRACE(Vessel.DEN, true),
    INSTINCT(Vessel.DEN, false),
    HUNGERPANGS(Vessel.DEN, false),
    LIONANDMOUSE(Vessel.DEN, false),
    UNANSWEREDQUESTIONS(Vessel.DEN, false),

    // The Wild
    WOUNDSLAY(Vessel.WOUNDEDWILD, true),
    WOUNDSAVE(Vessel.WOUNDEDWILD, false),
    GLIMPSEOFSOMETHING(Vessel.NETWORKWILD, false),

    // The Witch
    SCORPION(Vessel.WITCH, false),
    FROG(Vessel.WITCH, false),
    FROGLOCKED(Vessel.WITCH, false),
    KNIVESOUTMASKSOFF(Chapter.WILD, Voice.STUBBORN),
    KNIVESOUTMASKSOFFESCAPE(Chapter.WILD, Voice.CHEATED),
    PLAYINGITSAFE(Chapter.WILD, Voice.PARANOID),
    PASTLIFEGAMBITSPECIAL(Chapter.THORN, Voice.SMITTEN),
    PASTLIFEGAMBIT(Chapter.THORN, Voice.CHEATED),

    // The Thorn
    TRUSTISSUES(Vessel.THORN, false),
    ABANDONMENT(Vessel.THORN, false),
    NEWLEAFSMITTEN(Vessel.THORN, false),
    NEWLEAFCHEATED(Vessel.THORN, false),
    
    // The Stranger
    ILLUSIONOFCHOICE(Vessel.STRANGER, true),

    // The Prisoner
    TALKINGHEADS(Vessel.PRISONERHEAD, false),
    PRISONEROFMIND(Vessel.PRISONER, false),
    COLDLYRATIONAL(Chapter.GREY, Voice.COLD),
    RESTLESSFORCED(Chapter.CAGE, Voice.CHEATED),
    RESTLESSSELF(Chapter.CAGE, Voice.PARANOID),
    RESTLESSGIVEIN(Chapter.CAGE, Voice.BROKEN),

    // The Cage
    NOEXIT(Vessel.WATCHFULCAGE, false),
    RIDDLEOFSTEEL(Vessel.WATCHFULCAGE, true),
    ALLEGORYOFCAGE(Vessel.OPENCAGE, false),
    FREEWILL(Vessel.WATCHFULCAGE, true),

    // The Grey
    BURNINGDOWNTHEHOUSE(Vessel.BURNEDGREY, true),
    ANDALLTHISLONGING(Vessel.DROWNEDGREY, true),

    // The Damsel
    ROMANTICHAZE(Vessel.DAMSEL, false),
    ANDTHEYLIVEDHAPPILY(Vessel.DECONDAMSEL, true),
    LADYKILLER(Chapter.GREY, Voice.COLD),
    CONTENTSOFOURHEARTDECON(Chapter.HAPPY, Voice.SKEPTIC),
    CONTENTSOFOURHEARTUPSTAIRS(Chapter.HAPPY, Voice.OPPORTUNIST),

    // Happily Ever After
    IMEANTIT(Vessel.HAPPY, false),
    LEFTCABIN(Vessel.HAPPY, false),
    FINALLYOVER(Vessel.HAPPYDRY, true),
    DONTLETITGOOUT(Vessel.HAPPY, false),

    // Misc.
    NEWCYCLE(Chapter.CH1),
    ABORTED,

    // The End of Everything (full game endings)
    OBLIVION,
    NOENDINGS,
    PATHINTHEWOODS,
    THROUGHCONFLICT,
    NEWANDUNENDINGDAWN,
    ANDEVERYONEHATESYOU,
    WHATHAPPENSNEXT,
    STRANGEBEGINNINGS,
    YOURNEWWORLD;

    private boolean isFinal;

    private Chapter nextChapter;
    private Voice newVoice;
    private Voice newVoice2;

    private Vessel vessel;
    private boolean yourNewWorld;

    // --- CONSTRUCTORS ---

    private ChapterEnding(Chapter nextChapter, Voice newVoice) {
        this.isFinal = false;
        this.nextChapter = nextChapter;
        this.newVoice = newVoice;
    }

    private ChapterEnding(Chapter nextChapter, Voice newVoiceA, Voice newVoiceB) {
        // oh Razor my special little snowflake
        this(nextChapter, newVoiceA);
        this.newVoice2 = newVoiceB;
    }

    private ChapterEnding(Chapter nextChapter) {
        // indicates that we should just add all voices
        // or that this is a new cycle
        this(nextChapter, null);
    }

    private ChapterEnding() {
        // aborted chapter or game ending
        this.isFinal = true;
        this.nextChapter = Chapter.CH1;
    }

    private ChapterEnding(Vessel v, boolean yourNewWorld) {
        this.isFinal = true;
        this.nextChapter = Chapter.SPACESBETWEEN;
        this.vessel = v;
        this.yourNewWorld = yourNewWorld;
    }

    // --- ACCESSORS ---

    public boolean isFinal() {
        return this.isFinal;
    }

    public Chapter getNextChapter() {
        return this.nextChapter;
    }

    public Voice getNewVoice() {
        return this.newVoice;
    }

    public Voice getNewVoice2() {
        return this.newVoice2;
    }

    public Vessel getVessel() {
        return this.vessel;
    }

    public boolean qualifiesYNW() {
        return this.yourNewWorld;
    }
}
