public enum Vessel {
    // Chapter II
    ADVERSARY(Chapter.ADVERSARY, "The Song We Write in Our Blood"),
    TOWER(Chapter.TOWER, "Supplication"),
    SPECTRE(Chapter.SPECTRE, "Hitching a Ride"),
    NIGHTMARE(Chapter.NIGHTMARE, "I Want to Watch It Happen"),
    BEAST(Chapter.BEAST, "I Am So Much More Than You"),
    WITCH(Chapter.WITCH, "It's in Our Nature"),
    STRANGER(Chapter.STRANGER, "To Be Everything"),
    PRISONERHEAD("The Prisoner's Head", Chapter.PRISONER, "Eyes On Me"),
    PRISONER(Chapter.PRISONER, "I Don't Like Small Talk"),
    DAMSEL(Chapter.DAMSEL, "It Was Always That Easy"),
    DECONDAMSEL("The Deconstructed Damsel", Chapter.DAMSEL, "I Just Want to Make You Happy"),

    // Chapter III+
    RAZORFULL("The Razor (Full)", Chapter.RAZOR, "Mutually Assured Destruction"),
    RAZORHEART("The Razor's Heart", Chapter.RAZOR);

    private String name;
    private Chapter fromChapter;
    private String playlistSong;

    // --- CONSTRUCTORS ---

    /**
     * Constructor
     * @param name the name of the Vessel
     * @param c the Chapter the Vessel comes from
     * @param playlistSong the song the Vessel adds to the current playlist by default
     */
    private Vessel(String name, Chapter c, String playlistSong) {
        this.fromChapter = c;
        this.name = name;
        this.playlistSong = playlistSong;
    }

    /**
     * Constructor for a Vessel whose playlist song is the title of their origin Chapter
     * @param name the name of the Vessel
     * @param c the Chapter the Vessel comes from
     */
    private Vessel(String name, Chapter c) {
        this(name, c, c.getTitle());
    }

    /**
     * Constructor for a Vessel who shares a name with their origin Chapter
     * @param c the Chapter the Vessel comes from
     * @param playlistSong the song the Vessel adds to the current playlist by default
     */
    private Vessel(Chapter c, String playlistSong) {
        this(c.getTitle(), c, playlistSong);
    }

    /**
     * Constructor for a Vessel whose name and playlist song are the title of their origin Chapter
     * @param c the Chapter the Vessel comes from
     */
    private Vessel(Chapter c) {
        this(c.getTitle(), c, c.getTitle());
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
}
