public enum Voice {
    NARRATOR("The Narrator"),
    PRINCESS("The Princess"), // Exclusively used in Wild and Princess and the Dragon

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
    STUBBORN("Voice of the Stubborn");

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
            case "n": return NARRATOR;
            case "pint": return PRINCESS;

            case "hero": return HERO;
            case "broken": return BROKEN;
            case "cheated": return CHEATED;
            case "cold": return COLD;
            case "contra": return CONTRARIAN;
            case "hunted": return HUNTED;
            case "oppo": return OPPORTUNIST;
            case "para": return PARANOID;
            case "skeptic": return SKEPTIC;
            case "smitten": return SMITTEN;
            case "stubborn": return STUBBORN;

            default: return null;
        }
    }
}
