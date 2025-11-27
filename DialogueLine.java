public class DialogueLine {
    
    protected String line;
    protected boolean isInterrupted;

    // --- CONSTRUCTORS ---

    public DialogueLine(String line, boolean isInterrupted) {
        this.line = line;
        this.isInterrupted = isInterrupted;
    }

    public DialogueLine(String line) {
        this(line, false);
    }

    public DialogueLine() {
        this("", false);
    }

    // --- ACCESSORS & CHECKS ---

    public boolean isInterrupted() {
        return this.isInterrupted;
    }

    public boolean isEmpty() {
        return this.line.equals("");
    }

    // --- UTILITY ---

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

    @Override
    public String toString() {
        return this.line;
    }

}
