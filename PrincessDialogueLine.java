public class PrincessDialogueLine extends DialogueLine {

    private boolean isMoundEcho;

    // --- CONSTRUCTORS ---

    /**
     * Constructor
     * @param isMoundEcho whether the line is an "echo" of the Shifting Mound or a normal line from the Princess
     * @param line the text of the dialogue line
     * @param isInterrupted whether to go straight into printing the next line or wait for player input after printing this line
     */
    public PrincessDialogueLine(boolean isMoundEcho, String line, boolean isInterrupted) {
        super(line, isInterrupted);
        this.isMoundEcho = isMoundEcho;
    }

    /**
     * Constructor
     * @param isMoundEcho whether the line is an "echo" of the Shifting Mound or a normal line from the Princess
     * @param line the text of the dialogue line
     */
    public PrincessDialogueLine(boolean isMoundEcho, String line) {
        this(isMoundEcho, line, false);
    }

    /**
     * Constructor
     * @param line the text of the dialogue line
     * @param isInterrupted whether to go straight into printing the next line or wait for player input after printing this line
     */
    public PrincessDialogueLine(String line, boolean isInterrupted) {
        this(false, line, isInterrupted);
    }

    /**
     * Constructor
     * @param line the text of the dialogue line
     */
    public PrincessDialogueLine(String line) {
        this(false, line, false);
    }

    // --- UTILITY ---

    /**
     * Returns a String representation of this line
     * @return a String representation of this line
     */
    @Override
    public String toString() {
        String s = "";

        if (this.isMoundEcho) s += "(";
        s += '"' + this.line + '"';
        if (this.isMoundEcho) s += ")";

        return s;
    }
    
}
