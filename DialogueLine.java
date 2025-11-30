public class DialogueLine {
    
    protected String line;
    protected boolean isInterrupted;
    
    private static final String COMMA = ",";
    private static final String PUNCTUATION = ".,?!:;";

    // --- CONSTRUCTORS ---

    /**
     * Constructor
     * @param line the text of the dialogue line
     * @param isInterrupted whether to go straight into printing the next line or wait for player input after printing this line
     */
    public DialogueLine(String line, boolean isInterrupted) {
        this.line = line;
        this.isInterrupted = isInterrupted;
    }

    /**
     * Constructor
     * @param line the text of the dialogue line
     */
    public DialogueLine(String line) {
        this(line, false);
    }

    /**
     * Constructor for an empty dialogue line
     */
    public DialogueLine() {
        this("", false);
    }

    // --- ACCESSORS & CHECKS ---

    /**
     * Accessor for isInterrupted
     * @return whether to go straight into printing the next line or wait for player input after printing this line
     */
    public boolean isInterrupted() {
        return this.isInterrupted;
    }

    /**
     * Checks if this line consists of an empty String
     * @return true if line is an empty String; false otherwise
     */
    public boolean isEmpty() {
        return this.line.equals("");
    }

    // --- UTILITY ---

    /**
     * Slowly prints this line out
     * @param pauseAtPunctuation whether to pause for an extended time after punctuation or not
     */
    public void print(boolean pauseAtPunctuation) {
        char[] chars = IOHandler.wordWrap(this).toCharArray();

        for (int i = 0; i < chars.length; i++) {
            System.out.print(chars[i]);
            try {
                if (pauseAtPunctuation && isPunctuation(chars[i])) {
                    if (isComma(chars[i])) Thread.sleep(150);
                    else Thread.sleep(200);
                }
                else {
                    Thread.sleep(30);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException("Thread interrupted");
            }
        }
    }

    /**
     * Slowly prints this line out
     */
    public void print() {
        this.print(true);
    }

    /**
     * Slowly prints this line out
     * @param pauseAtPunctuation whether to pause for an extended time after punctuation or not
     * @param speedMultiplier the multiplier to apply to the standard speed of printing a line
     */
    public void print(boolean pauseAtPunctuation, double speedMultiplier) {
        char[] chars = IOHandler.wordWrap(this).toCharArray();
        double waitTime = 30 / speedMultiplier;
        double commaWaitTime = 150 / speedMultiplier;
        double punctWaitTime = 200 / speedMultiplier;

        for (int i = 0; i < chars.length; i++) {
            System.out.print(chars[i]);
            try {
                if (pauseAtPunctuation && isPunctuation(chars[i])) {
                    if (isComma(chars[i])) Thread.sleep((long)commaWaitTime);
                    else Thread.sleep((long)punctWaitTime);
                }
                else {
                    Thread.sleep((long)waitTime);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException("Thread interrupted");
            }
        }
    }

    /**
     * Slowly prints this line out
     * @param speedMultiplier the multiplier to apply to the standard speed of printing a line
     */
    public void print(double speedMultiplier) {
        this.print(false, speedMultiplier);
    }

    /**
     * Slowly prints this line out, then terminates the line
     */
    public void println() {
        char[] chars = IOHandler.wordWrap(this).toCharArray();

        for (int i = 0; i < chars.length; i++) {
            System.out.print(chars[i]);
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                throw new RuntimeException("Thread interrupted");
            }
        }

        System.out.print("\n");
    }

    /**
     * Returns a String representation of this line
     * @return a String representation of this line
     */
    @Override
    public String toString() {
        return this.line;
    }

    /**
     * Checks if a given character is a comma
     * @param c the character to check
     * @return true if c is a comma; false otherwise
     */
    private static boolean isComma(char c) {
        return COMMA.contains(Character.toString(c));
    }

    /**
     * Checks if a given character is punctuation
     * @param c the character to check
     * @return true if c is punctuation; false otherwise
     */
    private static boolean isPunctuation(char c) {
        return PUNCTUATION.contains(Character.toString(c));
    }

}
