import java.util.ArrayList;

public class OptionsMenu {

    // map String id --> Option
    private IndexedLinkedHashMap<String, Option> options;

    /*
     * Should be multiple ways of determining whether a given option is available, which can be combined:
     * - unavailable if has already been picked (on by default)
     * - unavailable if has already been picked (X) times
     * - only available if a given variable is equal to a given value (can be boolean or other)
     * - only available once another option in this menu has been picked
     */

    private boolean isExclusive; // can the player input commands during this options menu?

    // --- CONSTRUCTORS ---

    public OptionsMenu(boolean isExclusive) {
        this.options = new IndexedLinkedHashMap<>();
        this.isExclusive = isExclusive;
    }

    public OptionsMenu() {
        this(false);
    }

    // --- ACCESSORS & MANIPULATORS ---

    public int size() {
        return this.options.size();
    }

    public Option get(int n) {
        return options.getValue(n);
    }

    public Option get(String id) {
        return options.get(id);
    }

    public String getDisplay(int n) {
        return options.getValue(n).toString();
    }

    public String getDisplay(String id) {
        return options.get(id).toString();
    }

    public boolean isExclusive() {
        return this.isExclusive;
    }

    public Option getShown(int n) {
        return this.shownOptions().get(n);
    }

    public String getShownDisplay(int n) {
        return this.shownOptions().get(n).toString();
    }

    public Option getAvailable(int n) {
        return this.availableOptions().get(n);
    }

    public String getAvailableDisplay(int n) {
        return this.availableOptions().get(n).toString();
    }

    public boolean hasBeenPicked(int n) {
        return this.get(n).hasBeenPicked();
    }

    public boolean hasBeenPicked(String id) {
        return this.get(id).hasBeenPicked();
    }

    public boolean isShown(int n) {
        return this.get(n).isShown();
    }

    public boolean isShown(String id) {
        return this.get(id).isShown();
    }

    public boolean isAvailable(int n) {
        return this.get(n).isAvailable();
    }

    public boolean isAvailable(String id) {
        return this.get(id).isAvailable();
    }

    public boolean greyedOut(int n) {
        return this.get(n).greyedOut();
    }

    public boolean greyedOut(String id) {
        return this.get(id).greyedOut();
    }

    public int add(String id, Option o) {
        this.options.put(id, o);
        return this.options.getIndex(id);
    }

    public int add(Option o) {
        String id = o.getID();
        this.options.put(id, o);
        return this.options.getIndex(id);
    }

    public void setCondition(int n, boolean isMet) {
        this.get(n).setCondition(isMet);
    }

    public void setCondition(String id, boolean isMet) {
        this.get(id).setCondition(isMet);
    }

    public void setGreyedOut(int n, boolean condition) {
        this.get(n).setGreyedOut(condition);
    }

    public void setGreyedOut(String id, boolean condition) {
        this.get(id).setGreyedOut(condition);
    }

    // --- SHOWN / AVAILABLE OPTIONS ---

    public ArrayList<Option> shownOptions() {
        ArrayList<Option> shown = new ArrayList<>();

        for (int i = 0; i < this.options.size(); i++) {
            if (this.get(i).isShown()) {
                shown.add(this.get(i));
            }
        }

        return shown;
    }

    public ArrayList<Option> availableOptions() {
        ArrayList<Option> available = new ArrayList<>();

        for (int i = 0; i < this.options.size(); i++) {
            if (this.get(i).isAvailable()) {
                available.add(this.get(i));
            }
        }

        return available;
    }

    public int nAvailableOptions() {
        return this.availableOptions().size();
    }

    // --- MISC ---

    public String choose(int n) throws IllegalArgumentException {
        if (n < 0 || n >= this.size()) {
            throw new IllegalArgumentException("Option out of range");
        }

        this.get(n).choose();
        return this.get(n).getID();
    }

    public String playerChoose(int n) throws IllegalArgumentException {
        if (n <= 0 || n > this.nAvailableOptions()) {
            throw new IllegalArgumentException("Option out of available range");
        }

        return this.choose(this.trueIndex(n));
    }

    private int trueIndex(int playerIndex) {
        return this.options.indexOf(this.availableOptions().get(playerIndex - 1));
    }

    @Override
    public String toString() {
        ArrayList<Option> shown = this.shownOptions();
        ArrayList<Option> available = this.availableOptions();
        String s = "";
        int shownIndex = 0;

        for (int i = 0; i < shown.size(); i++) {
            shownIndex = available.indexOf(shown.get(i)) + 1;

            if (i > 0) {
                s += "\n";
            }

            if (shown.get(i).greyedOut()) {
                s += "  (--) " + shown.get(i);
            } else {
                s += "  (" + shownIndex + ".) " + shown.get(i);
            }
        }

        return s;
    }

}
