public class DialogueLine {
    
    protected String line;
    protected boolean isInterrupted;
    
    private static final String PUNCTUATION = ".,?!:;-";
    private static final String DELAYCHARS = "?!:;*\"')";

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
        long activeWaitTime;
        int punctDelayLength = 0;
        boolean doubleTimeFlag = false;

        long waitTime = 30;
        long commaWaitTime = 150;
        long punctWaitTime = 200;

        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '`') {
                doubleTimeFlag = true;
                continue;
            }
            
            System.out.print(chars[i]);
            try {
                if (punctDelayLength != 0 && isDelayChar(chars[i])) {
                    if (i == chars.length - 1) {
                        if (punctDelayLength == 1) activeWaitTime = commaWaitTime;
                        else activeWaitTime = punctWaitTime;
                    } else if (!isDelayChar(chars[i+1])) {
                        if (punctDelayLength == 1) activeWaitTime = commaWaitTime;
                        else activeWaitTime = punctWaitTime;

                        punctDelayLength = 0;
                    } else {
                        activeWaitTime = waitTime;
                    }
                } else if (pauseAtPunctuation && isPunctuation(chars[i])) {
                    if (chars[i] == '-') {
                        if (chars[i-1] != '-') {
                            activeWaitTime = waitTime;
                        } else if (i == chars.length - 1) {
                            if (this.isInterrupted) activeWaitTime = commaWaitTime;
                            else activeWaitTime = punctWaitTime;
                        } else if (Character.isWhitespace(chars[i+1])) {
                            if (isDelayChar(chars[i+2])) {
                                punctDelayLength = 2;
                                activeWaitTime = waitTime;
                            } else {
                                activeWaitTime = punctDelayLength;
                            }
                        } else if (isDelayChar(chars[i+1])) {
                            if (isPunctuation(chars[i+1])) {
                                punctDelayLength = 2;
                            } else if (this.isInterrupted) {
                                punctDelayLength = 1;
                            } else {
                                punctDelayLength = 2;
                            }

                            activeWaitTime = waitTime;
                        } else {
                            activeWaitTime = waitTime;
                        }
                    } else if (chars[i] == ',') {
                        if (isDelayChar(chars[i+1])) {
                            punctDelayLength = 1;
                            activeWaitTime = waitTime;
                        } else {
                            activeWaitTime = commaWaitTime;
                        }
                    } else {
                        if (isDelayChar(chars[i+1])) {
                            punctDelayLength = 2;
                            activeWaitTime = waitTime;
                        } else {
                            activeWaitTime = punctWaitTime;
                        }
                    }
                } else {
                    activeWaitTime = waitTime;
                }
            } catch (IndexOutOfBoundsException e) {
                activeWaitTime = waitTime;
            }

            if (doubleTimeFlag) {
                activeWaitTime *= 2;
                doubleTimeFlag = false;
            }

            try {
                Thread.sleep(activeWaitTime);
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
        long activeWaitTime;
        int punctDelayLength = 0;
        boolean doubleTimeFlag = false;

        long waitTime = (long)(30 / speedMultiplier);
        long commaWaitTime = (long)(150 / speedMultiplier);
        long punctWaitTime = (long)(200 / speedMultiplier);

        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '`') {
                doubleTimeFlag = true;
                continue;
            }

            System.out.print(chars[i]);
            try {
                if (punctDelayLength != 0) {
                    if (i == chars.length - 1) {
                        if (punctDelayLength == 1) activeWaitTime = commaWaitTime;
                        else activeWaitTime = punctWaitTime;
                    } else if (!isDelayChar(chars[i+1])) {
                        if (punctDelayLength == 1) activeWaitTime = commaWaitTime;
                        else activeWaitTime = punctWaitTime;

                        punctDelayLength = 0;
                    } else {
                        activeWaitTime = waitTime;
                    }
                } else if (pauseAtPunctuation && isPunctuation(chars[i])) {
                    if (chars[i] == '-') {
                        if (chars[i-1] != '-') {
                            activeWaitTime = waitTime;
                        } else if (i == chars.length - 1) {
                            if (this.isInterrupted) activeWaitTime = commaWaitTime;
                            else activeWaitTime = punctWaitTime;
                        } else if (Character.isWhitespace(chars[i+1])) {
                            if (isDelayChar(chars[i+2])) {
                                punctDelayLength = 2;
                                activeWaitTime = waitTime;
                            } else {
                                activeWaitTime = punctDelayLength;
                            }
                        } else if (isDelayChar(chars[i+1])) {
                            if (isPunctuation(chars[i+1])) {
                                punctDelayLength = 2;
                            } else if (this.isInterrupted) {
                                punctDelayLength = 1;
                            } else {
                                punctDelayLength = 2;
                            }

                            activeWaitTime = waitTime;
                        } else {
                            activeWaitTime = punctWaitTime;
                        }
                    } else if (chars[i] == ',') {
                        if (i == chars.length - 1) {
                            activeWaitTime = commaWaitTime;
                        } else if (isDelayChar(chars[i+1])) {
                            punctDelayLength = 1;
                            activeWaitTime = waitTime;
                        } else {
                            activeWaitTime = commaWaitTime;
                        }
                    } else {
                        if (i == chars.length - 1) {
                            activeWaitTime = punctWaitTime;
                        } else if (isDelayChar(chars[i+1])) {
                            punctDelayLength = 2;
                            activeWaitTime = waitTime;
                        } else {
                            activeWaitTime = punctWaitTime;
                        }
                    }
                } else {
                    activeWaitTime = waitTime;
                }
            } catch (IndexOutOfBoundsException e) {
                activeWaitTime = waitTime;
            }

            if (doubleTimeFlag) {
                activeWaitTime *= 2;
                doubleTimeFlag = false;
            }

            try {
                Thread.sleep(activeWaitTime);
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
     * Checks if a given character is punctuation
     * @param c the character to check
     * @return true if c is punctuation; false otherwise
     */
    private static boolean isPunctuation(char c) {
        return PUNCTUATION.contains(Character.toString(c));
    }

    /**
     * Checks if a given character is a character that causes a delay 
     * @param c the character to check
     * @return true if c is a dash; false otherwise
     */
    private static boolean isDelayChar(char c) {
        return DELAYCHARS.contains(Character.toString(c));
    }

}
