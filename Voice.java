import java.util.Arrays;

public enum Voice {
    NARRATOR("The Narrator", "narrator"),
    NARRATORPRINCESS("The Narrator", "narrprincess", true), // Used when the Tower possesses the Narrator
    PRINCESS("The Princess", "princess"), // Used in Wild and Princess and the Dragon

    HERO("Voice of the Hero", "hero"),
    BROKEN("Voice of the Broken", "broken"),
    CHEATED("Voice of the Cheated", "cheated"),
    COLD("Voice of the Cold", "cold"),
    CONTRARIAN("Voice of the Contrarian", "contrarian"),
    HUNTED("Voice of the Hunted", "hunted"),
    OPPORTUNIST("Voice of the Opportunist", "opportunist"),
    PARANOID("Voice of the Paranoid", "paranoid"),
    SKEPTIC("Voice of the Skeptic", "skeptic"),
    SMITTEN("Voice of the Smitten", "smitten"),
    STUBBORN("Voice of the Stubborn", "stubborn"),

    // Princess and the Dragon Voices
    UNKNOWNEXT("???", "dragon", true),
    HEROEXT("Hero", "heroext", true),
    COLDEXT("Cold", "coldext", true),
    OPPOEXT("Opportunist", "oppoext", true),

    // Two voices speaking at once
    NARRSTUB("The Narrator and the Voice of the Stubborn", "narrstub"),
    STUBCONT("Voices of the Stubborn and Contrarian", "stubcont"),
    PARASKEP("Voices of the Paranoid and Skeptic", "paraskep");

    private final String dialogueTag;
    private final String shorthand;
    private final boolean external; // Used in Tower, Princess and the Dragon

    public static final Voice[] TRUEVOICES = {HERO, BROKEN, CHEATED, COLD, CONTRARIAN, HUNTED, OPPORTUNIST, PARANOID, SKEPTIC, SMITTEN, STUBBORN};

    // --- CONSTRUCTOR ---

    /**
     * Constructor
     * @param tag the "dialogue tag" that appears at the beginning of the Voice's dialogue lines
     * @param shorthand the shorthand name for this Voice
     * @param external whether the Voice is external to the player's head
     */
    private Voice(String tag, String shorthand, boolean external) {
        this.dialogueTag = tag;
        this.shorthand = shorthand;
        this.external = external;
    }

    /**
     * Constructor
     * @param tag the "dialogue tag" that appears at the beginning of the Voice's dialogue lines
     * @param shorthand the shorthand name for this Voice
     */
    private Voice(String tag, String shorthand) {
        this(tag, shorthand, false);
    }

    // --- ACCESSORS & CHECKS ---

    /**
     * Accessor for dialogueTag
     * @return the dialogue tag for this Voice
     */
    public String getDialogueTag() {
        return this.dialogueTag;
    }

    /**
     * Accessor for shorthand
     * @return the shorthand name for this Voice
     */
    @Override
    public String toString() {
        return this.shorthand;
    }
    
    /**
     * Accessor for external
     * @return whether this Voice is external
     */
    public boolean isExternal() {
        return this.external;
    }

    /**
     * Checks if this Voice is a "true" Voice (not the Narrator or the Princess)
     * @return false if this Voice is the Narrator, the Princess, or a combination of multiple Voices; true otherwise
     */
    public boolean isTrueVoice() {
        return Arrays.asList(TRUEVOICES).contains(this);
    }

    /**
     * Returns the Voice that a Script should check for if performing a default Voice check for a line from this Voice
     * @return the Voice that a Script should check for if performing a default Voice check for a line from this Voice
     */
    public Voice checkVoice() {
        switch (this) {
            case UNKNOWNEXT:
            case HEROEXT: return HERO;
            case COLDEXT: return COLD;
            case OPPOEXT: return OPPORTUNIST;

            case NARRSTUB: return STUBBORN;
            case STUBCONT: return CONTRARIAN;
            case PARASKEP: return PARANOID;

            default: return this;
        }
    }

    /**
     * Returns the Voice corresponding to the given ID-String
     * @param characterID the ID-String of the Voice
     * @return the Voice corresponding to the given ID-String
     */
    public static Voice getVoice(String characterID) {
        switch (characterID) {
            case "n":
            case "narrator": return NARRATOR;

            case "np":
            case "narratorprincess": return NARRATORPRINCESS;

            case "pint":
            case "princessint": return PRINCESS;

            // Voice of the Hero gets the special privilege of getting a single-letter shortcut even though there's another voice that starts with the same letter, since he's more or less the secondary protagonist of the entire game
            case "h":
            case "hero": return HERO;

            case "b":
            case "broken": return BROKEN;

            case "ch":
            case "cheated": return CHEATED;

            case "cl":
            case "cold": return COLD;

            case "cn":
            case "contra":
            case "contrarian": return CONTRARIAN;

            case "ht":
            case "hunted": return HUNTED;

            case "o":
            case "oppo":
            case "opportunist": return OPPORTUNIST;

            case "pr":
            case "para":
            case "paranoid": return PARANOID;

            case "sk":
            case "skeptic": return SKEPTIC;

            case "sm":
            case "smitten": return SMITTEN;

            case "st":
            case "stubborn": return STUBBORN;

            case "dragon":
            case "uext":
            case "unknownext": return UNKNOWNEXT;
            case "hext":
            case "heroext": return HEROEXT;
            case "cext":
            case "coldext": return COLDEXT;
            case "oext":
            case "oppoext":
            case "opportunistext": return OPPOEXT;

            case "nstub": return NARRSTUB;
            case "stubcont": return STUBCONT;
            case "paraskep": return PARASKEP;

            default: return null;
        }
    }
}
