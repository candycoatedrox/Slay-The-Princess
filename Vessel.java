public enum Vessel {
    // Chapter II
    ADVERSARY(Chapter.ADVERSARY, "The Song We Write in Our Blood", "advyClaim"),
    TOWER(Chapter.TOWER, "Supplication", "towerClaim"),
    SPECTRE(Chapter.SPECTRE, "Hitching a Ride", "spectreClaim"),
    NIGHTMARE(Chapter.NIGHTMARE, "I Want to Watch It Happen", "nightClaim"),
    BEAST(Chapter.BEAST, "I Am So Much More Than You", "beastClaim"),
    WITCH(Chapter.WITCH, "It's in Our Nature", "witchClaim"),
    STRANGER(Chapter.STRANGER, "To Be Everything", "strangerClaim"),
    PRISONERHEAD("prisonerHead", "The Prisoner's Head", Chapter.PRISONER, "Eyes On Me", "headClaim"),
    PRISONER(Chapter.PRISONER, "I Don't Like Small Talk", "prisonerClaim"),
    DAMSEL(Chapter.DAMSEL, "It Was Always That Easy", "damselClaim"),
    DECONDAMSEL("deconDamsel", "The Deconstructed Damsel", Chapter.DAMSEL, "I Just Want to Make You Happy", "deconClaim"),

    // Chapter III
    NEEDLE(Chapter.NEEDLE, "needleClaim"),
    FURY(Chapter.FURY, "There's Nothing I Can Do To Bring You Back", "furyClaim"),
    REWOUNDFURY("furyRewound", "The Rewound Fury", Chapter.FURY, "Thirty-Trillion Cells", "rewoundClaim"),
    APOTHEOSIS(Chapter.APOTHEOSIS, "The Apotheosis", "apoClaim"),
    PATD("dragon", "The Princess", Chapter.DRAGON, "What Once Was One", "dragonClaim"),
    STENCILPATD("dragonStencil", "The Stenciled Princess", Chapter.DRAGON, "stencilClaim"),
    WRAITH(Chapter.WRAITH, "I'm Taking What I'm Owed", "wraithClaim"),
    CLARITY(Chapter.CLARITY, "clarityClaim"),
    RAZORFULL("razorFull", "The Razor (Full)", Chapter.RAZOR, "Mutually Assured Destruction", "razorClaim"),
    RAZORHEART("razorHeart", "The Razor's Heart", Chapter.RAZOR, "heartClaim"),
    DEN(Chapter.DEN, "denClaim"),
    NETWORKWILD("wildNetwork", "The Networked Wild", Chapter.WILD, "nWildClaim"),
    WOUNDEDWILD("wildWound", "The Wounded Wild", Chapter.WILD, "wWildClaim"),
    THORN(Chapter.THORN, "A Moment Trapped for All Time", "thornClaim"),
    WATCHFULCAGE(Chapter.CAGE, "A Prison of Flesh", "cageClaim"),
    OPENCAGE(Chapter.CAGE, "An Open Door", "cageClaim"),
    DROWNEDGREY("greyDrowned", "The Drowned Grey", Chapter.GREY, "The Grey (Water)", "dGreyClaim"),
    BURNEDGREY("greyBurned", "The Burned Grey", Chapter.GREY, "The Grey (Fire)", "bGreyClaim"),
    HAPPY(Chapter.HAPPY, "What Remains After the Fire", "happyClaim");

    private final String id;
    private final String name;
    private final Chapter fromChapter;
    private final String playlistSong;
    private final String achievementID;

    // --- CONSTRUCTORS ---

    /**
     * Constructor
     * @param id the internal ID of the Vessel
     * @param name the name of the Vessel
     * @param c the Chapter the Vessel comes from
     * @param playlistSong the song the Vessel adds to the current playlist by default
     * @param achievementID the ID of the achievement tied to the Vessel
     */
    private Vessel(String id, String name, Chapter c, String playlistSong, String achievementID) {
        this.id = id;
        this.name = name;
        this.fromChapter = c;
        this.playlistSong = playlistSong;
        this.achievementID = achievementID;
    }

    /**
     * Constructor for a Vessel whose playlist song is the title of their origin Chapter
     * @param id the internal ID of the Vessel
     * @param name the name of the Vessel
     * @param c the Chapter the Vessel comes from
     * @param achievementID the ID of the achievement tied to the Vessel
     */
    private Vessel(String id, String name, Chapter c, String achievementID) {
        this(id, name, c, c.toString(), achievementID);
    }

    /**
     * Constructor for a Vessel who shares a name with their origin Chapter
     * @param c the Chapter the Vessel comes from
     * @param playlistSong the song the Vessel adds to the current playlist by default
     * @param achievementID the ID of the achievement tied to the Vessel
     */
    private Vessel(Chapter c, String playlistSong, String achievementID) {
        this(c.toString(), c.toString(), c, playlistSong, achievementID);
    }

    /**
     * Constructor for a Vessel whose name and playlist song are the title of their origin Chapter
     * @param c the Chapter the Vessel comes from
     * @param achievementID the ID of the achievement tied to the Vessel
     */
    private Vessel(Chapter c, String achievementID) {
        this(c.toString(), c.toString(), c, c.toString(), achievementID);
    }

    // --- ACCESSORS ---

    /**
     * Accessor for id
     * @return the internal ID for this Voice
     */
    public String getID() {
        return this.id;
    }

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

    /**
     * Returns this Vessel's name as it appears in dialogue
     * @return this Vessel's name as it appears in dialogue
     */
    public String getNameInDialogue() {
        switch (this) {
            case ADVERSARY: return "the Adversary";
            case TOWER: return "the Tower";
            case SPECTRE: return "the Spectre";
            case NIGHTMARE: return "the Nightmare";
            case BEAST: return "the Beast";
            case WITCH: return "the Witch";
            case STRANGER: return "the Stranger";
            case PRISONERHEAD: return "the Prisoner's head";
            case PRISONER: return "the Prisoner";
            case DAMSEL: return "the Damsel";
            case DECONDAMSEL: return "the Damsel, as flat and two-dimensional as before,";

            case NEEDLE: return "the Eye of the Needle";
            case FURY: return "the Fury";
            case REWOUNDFURY: return "the Fury's heart";
            case APOTHEOSIS: return "the Apotheosis";
            case PATD:;
            case STENCILPATD: return "the Princess whose mind you know as well as your own";
            case WRAITH: return "the Wraith";
            case RAZORFULL: return "the Razor";
            case RAZORHEART: return "the Razor's Heart";
            case DEN: return "the Den";
            case NETWORKWILD: return "a network of nerves that you recognize as the Wild";
            case WOUNDEDWILD: return "the Wild";
            case THORN: return "the Thorn";
            case WATCHFULCAGE:
            case OPENCAGE: return "the Cage";
            case DROWNEDGREY:
            case BURNEDGREY: return "the Grey";
            default: return "your Happily Ever After";
        }
    }

    /**
     * Returns whether this Vessel needs to be held by the Shifting Mound (e.g. the Razor's heart) or can stand on its own
     * @return whether this Vessel needs to be held by the Shifting Mound (e.g. the Razor's heart) or can stand on its own
     */
    public boolean isHeldByMound() {
        switch (this) {
            case PRISONERHEAD:
            case REWOUNDFURY:
            case RAZORHEART:
            case WATCHFULCAGE:
            case OPENCAGE: return true;

            default: return false;
        }
    }

    // Vessel case templates for copy-pasting
    /*
        // Chapter II Vessels
        case ADVERSARY:
            break;

        case TOWER:
            break;

        case SPECTRE:
            break;

        case NIGHTMARE:
            break;

        case BEAST:
            break;

        case WITCH:
            break;

        case STRANGER:
            break;

        case PRISONERHEAD:
        case PRISONER:
            break;

        case DAMSEL:
        case DECONDAMSEL:
            break;

        // Chapter III vessels
        case NEEDLE:
            break;

        case FURY:
        case REWOUNDFURY:
            break;

        case APOTHEOSIS:
            break;

        case PATD:
        case STENCILPATD:
            break;

        case WRAITH:
            break;

        case RAZORFULL:
        case RAZORHEART:
            break;

        case DEN:
            break;

        case NETWORKWILD:
        case WOUNDEDWILD:
            break;

        case THORN:
            break;

        case WATCHFULCAGE:
        case OPENCAGE:
            break;

        case DROWNEDGREY:
        case BURNEDGREY:
            break;

        case HAPPY:
            break;
    */
}
