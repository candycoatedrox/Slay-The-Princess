public enum Vessel {
    // Chapter II
    ADVERSARY(Chapter.ADVERSARY, "The Song We Write in Our Blood", "advyClaim"),
    TOWER(Chapter.TOWER, "Supplication", "towerClaim"),
    SPECTRE(Chapter.SPECTRE, "Hitching a Ride", "spectreClaim"),
    NIGHTMARE(Chapter.NIGHTMARE, "I Want to Watch It Happen", "nightClaim"),
    BEAST(Chapter.BEAST, "I Am So Much More Than You", "beastClaim"),
    WITCH(Chapter.WITCH, "It's in Our Nature", "witchClaim"),
    STRANGER(Chapter.STRANGER, "To Be Everything", "strangerClaim"),
    PRISONERHEAD("The Prisoner's Head", Chapter.PRISONER, "Eyes On Me", "headClaim"),
    PRISONER(Chapter.PRISONER, "I Don't Like Small Talk", "prisonerClaim"),
    DAMSEL(Chapter.DAMSEL, "It Was Always That Easy", "damselClaim"),
    DECONDAMSEL("The Deconstructed Damsel", Chapter.DAMSEL, "I Just Want to Make You Happy", "deconClaim"),

    // Chapter III
    NEEDLE(Chapter.NEEDLE, "needleClaim"),
    FURY(Chapter.FURY, "There's Nothing I Can Do To Bring You Back", "furyClaim"),
    REWOUNDFURY("The Rewound Fury", Chapter.FURY, "Thirty-Trillion Cells", "rewoundClaim"),
    APOTHEOSIS(Chapter.APOTHEOSIS, "The Apotheosis", "apoClaim"),
    PATD("The Princess", Chapter.DRAGON, "What Once Was One", "dragonClaim"),
    STENCILPATD("The Stenciled Princess", Chapter.DRAGON, "stencilClaim"),
    WRAITH(Chapter.WRAITH, "I'm Taking What I'm Owed", "wraithClaim"),
    CLARITY(Chapter.CLARITY, "clarityClaim"),
    RAZORFULL("The Razor (Full)", Chapter.RAZOR, "Mutually Assured Destruction", "razorClaim"),
    RAZORHEART("The Razor's Heart", Chapter.RAZOR, "heartClaim"),
    DEN(Chapter.DEN, "denClaim"),
    NETWORKWILD("The Networked Wild", Chapter.WILD, "nWildClaim"),
    WOUNDEDWILD("The Wounded Wild", Chapter.WILD, "wWildClaim"),
    THORN(Chapter.THORN, "A Moment Trapped for All Time", "thornClaim"),
    WATCHFULCAGE(Chapter.CAGE, "A Prison of Flesh", "cageClaim"),
    OPENCAGE(Chapter.CAGE, "An Open Door", "cageClaim"),
    DROWNEDGREY("The Drowned Grey", Chapter.GREY, "The Grey (Water)", "dGreyClaim"),
    BURNEDGREY("The Burned Grey", Chapter.GREY, "The Grey (Fire)", "bGreyClaim"),
    HAPPY(Chapter.HAPPY, "What Remains After the Fire", "happyClaim"),
    HAPPYDRY(Chapter.HAPPY, "What Remains After the Fire", "happyClaim");

    private final String name;
    private final Chapter fromChapter;
    private final String playlistSong;
    private final String achievementID;

    // --- CONSTRUCTORS ---

    /**
     * Constructor
     * @param name the name of the Vessel
     * @param c the Chapter the Vessel comes from
     * @param playlistSong the song the Vessel adds to the current playlist by default
     * @param achievementID the ID of the achievement tied to the Vessel
     */
    private Vessel(String name, Chapter c, String playlistSong, String achievementID) {
        this.fromChapter = c;
        this.name = name;
        this.playlistSong = playlistSong;
        this.achievementID = achievementID;
    }

    /**
     * Constructor for a Vessel whose playlist song is the title of their origin Chapter
     * @param name the name of the Vessel
     * @param c the Chapter the Vessel comes from
     * @param achievementID the ID of the achievement tied to the Vessel
     */
    private Vessel(String name, Chapter c, String achievementID) {
        this(name, c, c.toString(), achievementID);
    }

    /**
     * Constructor for a Vessel who shares a name with their origin Chapter
     * @param c the Chapter the Vessel comes from
     * @param playlistSong the song the Vessel adds to the current playlist by default
     * @param achievementID the ID of the achievement tied to the Vessel
     */
    private Vessel(Chapter c, String playlistSong, String achievementID) {
        this(c.toString(), c, playlistSong, achievementID);
    }

    /**
     * Constructor for a Vessel whose name and playlist song are the title of their origin Chapter
     * @param c the Chapter the Vessel comes from
     * @param achievementID the ID of the achievement tied to the Vessel
     */
    private Vessel(Chapter c, String achievementID) {
        this(c.toString(), c, c.toString(), achievementID);
    }

    // --- ACCESSORS ---

    /**
     * Returns a String representation of this Vessel
     * @return the name of this Vessel
     */
    @Override
    public String toString() {
        return this.name;
    }
    
    /**
     * Accessor for fromChapter
     * @return the Chapter this Vessel comes from
     */
    public Chapter fromChapter() {
        return this.fromChapter;
    }

    /**
     * Accessor for playlistSong
     * @return the song this Vessel adds to the current playlist by default
     */
    public String getPlaylistSong() {
        return this.playlistSong;
    }

    /**
     * Accessor for achievementID
     * @return the ID of the achievement tied to this Vessel
     */
    public String getAchievementID() {
        return this.achievementID;
    }
}
