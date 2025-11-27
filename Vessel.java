public enum Vessel {
    // Chapter II
    ADVERSARY(Chapter.ADVERSARY),
    TOWER(Chapter.TOWER),
    SPECTRE(Chapter.SPECTRE),
    NIGHTMARE(Chapter.NIGHTMARE),
    BEAST(Chapter.BEAST),
    WITCH(Chapter.WITCH),
    STRANGER(Chapter.STRANGER),
    PRISONERHEAD(Chapter.PRISONER, "The Prisoner's Head"),
    PRISONER(Chapter.PRISONER),
    DAMSEL(Chapter.DAMSEL),
    DECONDAMSEL(Chapter.DAMSEL, "The Deconstructed Damsel"),

    // Chapter III
    NEEDLE(Chapter.NEEDLE),
    FURY(Chapter.FURY),
    REWOUNDFURY(Chapter.FURY, "The Rewound Fury"),
    APOTHEOSIS(Chapter.APOTHEOSIS),
    PATD(Chapter.DRAGON, "The Princess"),
    STENCILPATD(Chapter.DRAGON, "The Stenciled Princess"),
    WRAITH(Chapter.WRAITH),
    CLARITY(Chapter.CLARITY),
    RAZORFULL(Chapter.RAZOR, "The Razor (Full)"),
    RAZORHEART(Chapter.RAZOR, "The Razor's Heart"),
    DEN(Chapter.DEN),
    NETWORKWILD(Chapter.WILD, "The Networked Wild"),
    WOUNDEDWILD(Chapter.WILD, "The Wounded Wild"),
    THORN(Chapter.THORN),
    WATCHFULCAGE(Chapter.CAGE),
    OPENCAGE(Chapter.CAGE),
    BURNEDGREY(Chapter.GREY, "The Burned Grey"),
    DROWNEDGREY(Chapter.GREY, "The Drowned Grey"),
    HAPPY(Chapter.HAPPY),
    HAPPYDRY(Chapter.HAPPY);

    private Chapter fromChapter;
    private String name;

    // --- CONSTRUCTORS ---

    /**
     * Constructor
     * @param c the Chapter the Vessel comes from
     * @param name the name of the Vessel
     */
    private Vessel(Chapter c, String name) {
        this.fromChapter = c;
        this.name = name;
    }

    /**
     * Constructor for a Vessel who shares a name with their origin Chapter
     * @param c the Chapter the Vessel comes from
     */
    private Vessel(Chapter c) {
        this(c, c.getTitle());
    }

    // --- ACCESSORS ---
    
    /**
     * Accessor for fromChapter
     * @return the Chapter this Vessel comes from
     */
    public Chapter fromChapter() {
        return this.fromChapter;
    }

    /**
     * Returns a String representation of this Vessel
     * @return the name of this Vessel
     */
    @Override
    public String toString() {
        return this.name;
    }
}
