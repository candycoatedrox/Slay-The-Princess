import java.util.ArrayList;

public class OptionsMenu {

    private IndexedLinkedHashMap<String, Option> options; // Map String ID --> Option
    private boolean isExclusive; // Can the player input commands during this options menu?
    private boolean isMeta;

    // --- CONSTRUCTORS ---

    /**
     * Private constructor
     * @param isExclusive whether the player can input commands during the menu
     * @param isMeta whether the menu is used for meta commands (e.g. the settings or content warnings menus)
     */
    public OptionsMenu(boolean isExclusive, boolean isMeta) {
        this.options = new IndexedLinkedHashMap<>();
        this.isExclusive = isExclusive;
        this.isMeta = isMeta;
    }

    /**
     * Constructor
     * @param isExclusive whether the player can input commands during the menu
     */
    public OptionsMenu(boolean isExclusive) {
        this(isExclusive, false);
    }

    /**
     * Constructor
     */
    public OptionsMenu() {
        this(false, false);
    }

    // --- ACCESSORS & MANIPULATORS ---

    /**
     * Accessor for isExclusive
     * @return whether the player can input commands during this menu
     */
    public boolean isExclusive() {
        return this.isExclusive;
    }

    /**
     * Accessor for isMeta
     * @return whether the menu is used for meta commands (e.g. the settings or content warnings menus)
     */
    public boolean isMeta() {
        return this.isMeta;
    }

    /**
     * Returns the number of Options in this menu
     * @return the number of Options in this menu
     */
    public int size() {
        return this.options.size();
    }

    /**
     * Returns the nth Option in this menu
     * @param n the index of the Option to retrieve
     * @return the nth Option in this menu
     */
    public Option get(int n) {
        return options.getValue(n);
    }

    /**
     * Returns the first Option with the given ID in this menu
     * @param id the ID of the Option to retrieve
     * @return the first Option with the given ID in this menu
     */
    public Option get(String id) {
        return options.get(id);
    }

    /**
     * Returns the display text of the nth Option in this menu
     * @param n the index of the Option to retrieve
     * @return the display text of the nth Option in this menu
     * @throws IllegalArgumentException if the given index is out of range
     */
    public String getDisplay(int n) {
        if (n < 0 || n >= this.size()) {
            throw new IllegalArgumentException("Option out of range");
        }
        
        return this.get(n).toString();
    }

    /**
     * Returns the display text of the first Option with the given ID in this menu
     * @param id the ID of the Option to retrieve
     * @return the display text of the first Option with the given ID in this menu
     */
    public String getDisplay(String id) {
        return this.get(id).toString();
    }

    /**
     * Sets the display text of the nth Option in this menu
     * @param n the index of the Option to retrieve
     * @param newDisplay the new text displayed to the player for the Option
     * @return the previous display text of the nth Option in this menu
     * @throws IllegalArgumentException if the given index is out of range
     */
    public String setDisplay(int n, String newDisplay) {
        if (n < 0 || n >= this.size()) {
            throw new IllegalArgumentException("Option out of range");
        }
        
        return this.get(n).setDisplay(newDisplay);
    }

    /**
     * Sets the display text of the first Option with the given ID in this menu
     * @param id the ID of the Option to retrieve
     * @param newDisplay the new text displayed to the player for the Option
     * @return the previous display text of the first Option with the given ID in this menu
     */
    public String setDisplay(String id, String newDisplay) {
        return this.get(id).setDisplay(newDisplay);
    }

    /**
     * Returns the nth visible Option in this menu
     * @param n the index of the visible Option to retrieve
     * @return the Option at the given index in this menu's list of visible Options
     * @throws IllegalArgumentException if the given index is out of range
     */
    public Option getShown(int n) {
        ArrayList<Option> shown = this.shownOptions();

        if (n < 0 || n >= shown.size()) {
            throw new IllegalArgumentException("Option out of range");
        }
        
        return shown.get(n);
    }

    /**
     * Returns the display text of the nth visible Option in this menu
     * @param n the index of the visible Option to retrieve
     * @return the display text of the Option at the given index in this menu's list of visible Options
     * @throws IllegalArgumentException if the given index is out of range
     */
    public String getShownDisplay(int n) {
        ArrayList<Option> shown = this.shownOptions();

        if (n < 0 || n >= shown.size()) {
            throw new IllegalArgumentException("Option out of range");
        }
        
        return shown.get(n).toString();
    }

    /**
     * Returns the nth available Option in this menu
     * @param n the index of the available Option to retrieve
     * @return the Option at the given index in this menu's list of available Options
     * @throws IllegalArgumentException if the given index is out of range
     */
    public Option getAvailable(int n) {
        ArrayList<Option> available = this.availableOptions();

        if (n < 0 || n >= available.size()) {
            throw new IllegalArgumentException("Option out of range");
        }
        
        return available.get(n);
    }

    /**
     * Returns the display text of the nth available Option in this menu
     * @param n the index of the available Option to retrieve
     * @return the display text of the Option at the given index in this menu's list of available Options
     * @throws IllegalArgumentException if the given index is out of range
     */
    public String getAvailableDisplay(int n) {
        ArrayList<Option> available = this.availableOptions();

        if (n < 0 || n >= available.size()) {
            throw new IllegalArgumentException("Option out of range");
        }
        
        return available.get(n).toString();
    }

    /**
     * Checks if the nth Option in this menu has been picked at least once
     * @param n the index of the Option to check
     * @return false if the nth Option in this menu has never been picked; true otherwise
     * @throws IllegalArgumentException if the given index is out of range
     */
    public boolean hasBeenPicked(int n) {
        if (n < 0 || n >= this.size()) {
            throw new IllegalArgumentException("Option out of range");
        }
        
        return this.get(n).hasBeenPicked();
    }

    /**
     * Checks if the first Option with the given ID in this menu has been picked at least once
     * @param id the ID of the Option to check
     * @return false if the first Option with the given ID in this menu has never been picked; true otherwise
     */
    public boolean hasBeenPicked(String id) {
        return this.get(id).hasBeenPicked();
    }

    
    /**
     * Checks if the nth Option in this menu is visible
     * @param n the index of the Option to check
     * @return true if the nth Option in this menu is visible; false otherwise
     * @throws IllegalArgumentException if the given index is out of range
     */
    public boolean isShown(int n) {
        if (n < 0 || n >= this.size()) {
            throw new IllegalArgumentException("Option out of range");
        }
        
        return this.get(n).isShown();
    }

    /**
     * Checks if the first Option with the given ID in this menu is visible
     * @param id the ID of the Option to check
     * @return true if the first Option with the given ID in this menu is visible; false otherwise
     */
    public boolean isShown(String id) {
        return this.get(id).isShown();
    }

    /**
     * Checks if the nth Option in this menu is available to be chosen by the player
     * @param n the index of the Option to check
     * @return false if the first Option with the given ID in this menu is available to be chosen by the player; false otherwise
     * @throws IllegalArgumentException if the given index is out of range
     */
    public boolean isAvailable(int n) {
        if (n < 0 || n >= this.size()) {
            throw new IllegalArgumentException("Option out of range");
        }
        
        return this.get(n).isAvailable();
    }

    /**
     * Checks if the first Option with the given ID in this menu is available to be chosen by the player
     * @param id the ID of the Option to check
     * @return false if the first Option with the given ID in this menu is available to be chosen by the player; false otherwise
     */
    public boolean isAvailable(String id) {
        return this.get(id).isAvailable();
    }

    /**
     * Checks if the nth Option in this menu is greyed out
     * @param n the index of the Option to check
     * @return false if the nth Option in this menu is greyed out; false otherwise
     * @throws IllegalArgumentException if the given index is out of range
     */
    public boolean greyedOut(int n) {
        if (n < 0 || n >= this.size()) {
            throw new IllegalArgumentException("Option out of range");
        }
        
        return this.get(n).greyedOut();
    }

    /**
     * Checks if the first Option with the given ID in this menu is greyed out
     * @param id the ID of the Option to check
     * @return false if the first Option with the given ID in this menu is greyed out; false otherwise
     */
    public boolean greyedOut(String id) {
        return this.get(id).greyedOut();
    }

    /**
     * Adds an Option to this menu
     * @param o the Option to add to this menu
     * @return the index of the Option in this menu
     */
    public int add(Option o) {
        String id = o.getID();
        this.options.put(id, o);
        return this.options.getIndex(id);
    }

    /**
     * Adds a copy of a given Option with the given ID to this menu
     * @param o the Option to add to this menu
     * @param id the new ID of the Option
     * @return the index of the Option in this menu
     */
    public int add(Option o, String id) {
        Option oCopy = o.clone();
        oCopy.setID(id);

        this.options.put(id, oCopy);
        return this.options.getIndex(id);
    }

    /**
     * Adds all Options in another OptionsMenu to the end of this menu
     * @param menu the OptionsMenu to append to the end of this menu
     */
    public void append(OptionsMenu menu) {
        for (int i = 0; i < menu.size(); i++) {
            this.add(menu.get(i));
        }
    }

    /**
     * Sets conditionMet for the nth Option in this menu to the specified value
     * @param n the index of the Option to modify
     * @param isMet whether to set the Option to visible or not
     * @throws IllegalArgumentException if the given index is out of range
     */
    public void setCondition(int n, boolean isMet) {
        if (n < 0 || n >= this.size()) {
            throw new IllegalArgumentException("Option out of range");
        }

        this.get(n).setCondition(isMet);
    }

    /**
     * Sets conditionMet for the first Option with the given ID in this menu to the specified value
     * @param id the ID of the Option to modify
     * @param isMet whether to set the Option to visible or not
     */
    public void setCondition(String id, boolean isMet) {
        this.get(id).setCondition(isMet);
    }

    /**
     * Sets greyedOut for the nth Option in this menu to the specified value
     * @param n the index of the Option to modify
     * @param condition the value to set the Option's greyedOut to
     * @throws IllegalArgumentException if the given index is out of range
     */
    public void setGreyedOut(int n, boolean condition) {
        if (n < 0 || n >= this.size()) {
            throw new IllegalArgumentException("Option out of range");
        }
        
        this.get(n).setGreyedOut(condition);
    }

    /**
     * Sets greyedOut for the first Option with the given ID in this menu to the specified value
     * @param id the ID of the Option to modify
     * @param condition the value to set the Option's greyedOut to
     */
    public void setGreyedOut(String id, boolean condition) {
        this.get(id).setGreyedOut(condition);
    }

    /**
     * Sets greyCondition for the nth Option in this menu to the specified value
     * @param n the index of the Option to modify
     * @param condition the condition to set the Option's greyCondition to
     * @throws IllegalArgumentException if the given index is out of range
     */
    public void setGreyCondition(int n, AbstractCondition condition) {
        if (n < 0 || n >= this.size()) {
            throw new IllegalArgumentException("Option out of range");
        }
        
        this.get(n).setGreyCondition(condition);
    }

    /**
     * Sets greyedOut for the first Option with the given ID in this menu to the specified value
     * @param id the ID of the Option to modify
     * @param condition the condition to set the Option's greyCondition to
     */
    public void setGreyCondition(String id, AbstractCondition condition) {
        this.get(id).setGreyCondition(condition);
    }

    // --- SHOWN / AVAILABLE OPTIONS ---

    /**
     * Returns a list of all currently visible Options in this menu
     * @return a list of all currently visible Options in this menu
     */
    public ArrayList<Option> shownOptions() {
        ArrayList<Option> shown = new ArrayList<>();
        for (int i = 0; i < this.options.size(); i++) {
            if (this.get(i).isShown()) shown.add(this.get(i));
        }
        return shown;
    }

    /**
     * Returns a list of all currently available Options in this menu
     * @return a list of all currently available Options in this menu
     */
    public ArrayList<Option> availableOptions() {
        ArrayList<Option> available = new ArrayList<>();
        for (int i = 0; i < this.options.size(); i++) {
            if (this.get(i).isAvailable()) available.add(this.get(i));
        }
        return available;
    }

    /**
     * Returns the number of currently available Options in this menu
     * @return the number of currently available Options in this menu
     */
    public int nAvailableOptions() {
        return this.availableOptions().size();
    }

    // --- MISC ---

    /**
     * Increments the number of times the nth Option in this menu has been picked and returns its ID
     * @param n the index of the Option being picked
     * @return the ID of the chosen Option
     * @throws IllegalArgumentException if the given index is out of range
     */
    public String choose(int n) throws IllegalArgumentException {
        if (n < 0 || n >= this.size()) {
            throw new IllegalArgumentException("Option out of range");
        }

        return this.get(n).choose();
    }

    /**
     * Increments the number of times the nth available Option in this menu has been picked and returns its ID
     * @param playerIndex the index given by the player, indicating their choice out of the currently available Options
     * @return the ID of the chosen Option
     * @throws IllegalArgumentException if the given index is out of range
     */
    public String playerChoose(int n) throws IllegalArgumentException {
        if (n <= 0 || n > this.nAvailableOptions()) {
            throw new IllegalArgumentException("Option out of available range");
        }

        return this.choose(this.trueIndex(n));
    }

    /**
     * Converts the index of an Option out of the currently available Options to its true index out of all Options in this menu
     * @param playerIndex the index given by the player, indicating their choice out of the currently available Options
     * @return the true index of the chosen Option, out of all Options in the menu
     */
    private int trueIndex(int playerIndex) {
        return this.options.indexOf(this.availableOptions().get(playerIndex - 1));
    }

    /**
     * Returns a formatted list of all currently visible Options in this menu
     * @return a formatted list of all currently visible Options in this menu
     */
    @Override
    public String toString() {
        ArrayList<Option> shown = this.shownOptions();
        ArrayList<Option> available = this.availableOptions();
        String s = "";
        int shownIndex;

        for (int i = 0; i < shown.size(); i++) {
            shownIndex = available.indexOf(shown.get(i)) + 1;

            if (i > 0) {
                s += "\n";
            }

            if (shown.get(i).isStrangerEnding()) {
                s += "  (" + shownIndex + ".) " + shown.get(i).toString().replaceAll("NUM", shownIndex + ".");
            } else {
                if (shown.get(i).greyedOut()) {
                    s += "  (--) " + shown.get(i);
                } else {
                    s += "  (" + shownIndex + ".) " + shown.get(i);
                }
            }
        }

        return s;
    }

    /* public static void main(String[] args) {
        GameManager manager = new GameManager();
        IOHandler parser = new IOHandler(manager);
        OptionsMenu testMenu = new OptionsMenu();
        testMenu.add(new Option(manager, "testID", "This is a test option."));
        testMenu.add(new Option(manager, "t2", "This is also a test option."));
        testMenu.add(new Option(manager, "someoneElse", "This, too, is a test option, but it depends.", testMenu.get("testID")));

        parser.promptOptionsMenu(testMenu);
        parser.promptOptionsMenu(testMenu);
    } */

}
