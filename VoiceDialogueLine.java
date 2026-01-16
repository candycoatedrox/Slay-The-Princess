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
     * Slowly prints this line out
     * @param pauseAtPunctuation whether to pause for an extended time after punctuation or not
     * @param speedMultiplier the multiplier to apply to the standard speed of printing a line
     */
    @Override
    public void print(boolean pauseAtPunctuation, double speedMultiplier) {
        char[] chars = IOHandler.wordWrapIgnoreIndicator(this).toCharArray();
        int tagLength = speaker.getDialogueTag().length();

        int punctDelayLength = 0;
        boolean doubleTimeFlag = false;

        long activeWaitTime;
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
                doubleTimeFlag = false;
                activeWaitTime *= 2;
            }

            try {
                Thread.sleep(activeWaitTime);
            } catch (InterruptedException e) {
                throw new RuntimeException("Thread interrupted");
            }

            if (i == tagLength) {
                waitTime /= speedMultiplier;
                commaWaitTime /= speedMultiplier;
                punctWaitTime /= speedMultiplier;
                //IOHandler.wrapPrintln("[DEBUG: changing wait times; new times = " + waitTime + "/" + commaWaitTime + "/" + punctWaitTime + "]");
            }
        }
    }

    /**
     * Returns a String representation of this line
     * @return a String representation of this line
     */
    @Override
    public String toString() {
        if (speaker.isExternal()) {
            return speaker.getDialogueTag() + ": \"" + this.line + "\"";
        } else {
            return speaker.getDialogueTag() + ": " + this.line;
        }
    }

}
