public class VoiceDialogueLine extends DialogueLine {
    
    private Voice speaker;

    // --- CONSTRUCTORS ---

    /**
     * Constructor
     * @param speaker the Voice the line is spoken by
     * @param line the text of the dialogue line
     * @param isInterrupted whether to go straight into printing the next line or wait for player input after printing this line
     */
    public VoiceDialogueLine(Voice speaker, String line, boolean isInterrupted) {
        super(line, isInterrupted);
        this.speaker = speaker;
    }

    /**
     * Constructor
     * @param speaker the Voice the line is spoken by
     * @param line the text of the dialogue line
     */
    public VoiceDialogueLine(Voice speaker, String line) {
        this(speaker, line, false);
    }

    /**
     * Constructor
     * @param speaker the ID of the Voice the line is spoken by
     * @param line the text of the dialogue line
     * @param isInterrupted whether to go straight into printing the next line or wait for player input after printing this line
     */
    public VoiceDialogueLine(String speakerID, String line, boolean isInterrupted) {
        super(line, isInterrupted);
        this.speaker = Voice.getVoice(speakerID);
    }

    /**
     * Constructor
     * @param speakerID the ID of the Voice the line is spoken by
     * @param line the text of the dialogue line
     */
    public VoiceDialogueLine(String speakerID, String line) {
        this(speakerID, line, false);
    }

    /**
     * Constructor for a Narrator dialogue line
     * @param line the text of the dialogue line
     * @param isInterrupted whether to go straight into printing the next line or wait for player input after printing this line
     */
    public VoiceDialogueLine(String line, boolean isInterrupted) {
        this(Voice.NARRATOR, line, isInterrupted);
    }

    /**
     * Constructor for a Narrator dialogue line
     * @param line the text of the dialogue line
     */
    public VoiceDialogueLine(String line) {
        this(Voice.NARRATOR, line, false);
    }

    // --- UTILITY ---

    /**
     * Returns a String representation of this line
     * @return a String representation of this line
     */
    @Override
    public String toString() {
        return this.speaker.getDialogueTag() + ": " + this.line;
    }

}
