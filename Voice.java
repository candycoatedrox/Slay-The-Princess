public enum Voice {
    NARRATOR("The Narrator"),
    NARRATORPRINCESS("The Narrator"), // Used when the Tower possesses the Narrator
    PRINCESS("The Princess"), // Used in Wild and Princess and the Dragon

    HERO("Voice of the Hero"),
    BROKEN("Voice of the Broken"),
    CHEATED("Voice of the Cheated"),
    COLD("Voice of the Cold"),
    CONTRARIAN("Voice of the Contrarian"),
    HUNTED("Voice of the Hunted"),
    OPPORTUNIST("Voice of the Opportunist"),
    PARANOID("Voice of the Paranoid"),
    SKEPTIC("Voice of the Skeptic"),
    SMITTEN("Voice of the Smitten"),
    STUBBORN("Voice of the Stubborn"),

    // Two voices speaking at once
    NARRATORSTUBBORN("The Narrator and the Voice of the Stubborn");

    private String dialogueTag;

    // --- CONSTRUCTOR ---

    /**
     * Constructor
     * @param tag the "dialogue tag" that appears at the beginning of the Voice's dialogue lines
     */
    private Voice(String tag) {
        this.dialogueTag = tag;
    }

    // --- ACCESSORS ---
    
    /**
     * Accessor for dialogueTag
     * @return the dialogue tag for this Voice
     */
    public String getDialogueTag() {
        return this.dialogueTag;
    }

    public static Voice getVoice(String characterID) {
        switch (characterID) {
            case "n":
            case "narrator": return NARRATOR;

            case "np":
            case "narratorprincess": return NARRATORPRINCESS;

            case "pint":
            case "princessint": return PRINCESS;

            // Voice of the Hero gets the special privelege of getting a single-letter shortcut even though there's another voice that starts with the same letter, since he's more or less the secondary protagonist of the entire game
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

            case "nstub": return NARRATORSTUBBORN;

            default: return null;
        }
    }
}
