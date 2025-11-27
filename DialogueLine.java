public class DialogueLine {
    
    protected String line;
    protected boolean isInterrupted;

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
     */
    public void print() {
        char[] chars = IOHandler.wordWrap(this).toCharArray();

        for (int i = 0; i < chars.length; i++) {
            System.out.print(chars[i]);
            try {
                Thread.sleep(30);
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
        char[] chars = IOHandler.wordWrap(this).toCharArray();
        double waitTime = 30 / speedMultiplier;

        for (int i = 0; i < chars.length; i++) {
            System.out.print(chars[i]);
            try {
                Thread.sleep((long)waitTime);
            } catch (InterruptedException e) {
                throw new RuntimeException("Thread interrupted");
            }
        }
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

}
