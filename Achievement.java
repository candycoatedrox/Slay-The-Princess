public class Achievement {
    
    // Internal information
    private final String id;
    private final Chapter origin;
    private final boolean isHidden;
    private final boolean showsUnlockMessage;
    private boolean isUnlocked = false;

    // Display information
    private final String name;
    private final String description;


    // --- CONSTRUCTOR ---

    /**
     * Constructor
     * @param id the internal ID of this achievement
     * @param origin the chapter this achievement is unlocked in
     * @param isHidden whether or not this achievement's name is hidden in the gallery before it is unlocked
     * @param showsUnlockMessage whether or not this achievement prints a message when unlocked
     * @param name the name of this achievement
     * @param description the description of this achievement
     */
    public Achievement(String id, Chapter origin, boolean isHidden, boolean showsUnlockMessage, String name, String description) {
        this.id = id;
        this.origin = origin;
        this.isHidden = isHidden;
        this.showsUnlockMessage = showsUnlockMessage;
        this.name = name;
        this.description = description;
    }

    // --- ACCESSORS & MANIPULATORS ---

    /**
     * Accessor for id
     * @return the internal ID of this achievement
     */
    public String getID() {
        return this.id;
    }

    /**
     * Accessor for origin
     * @return the chapter this achievement is unlocked in
     */
    public Chapter getOrigin() {
        return this.origin;
    }

    /**
     * Accessor for isHidden
     * @return whether or not this achievement's name is hidden in the gallery before it is unlocked
     */
    public boolean isHidden() {
        return this.isHidden;
    }

    /**
     * Accessor for showsUnlockMessage
     * @return whether or not this achievement prints a message when unlocked
     */
    public boolean showsUnlockMessage() {
        return this.showsUnlockMessage;
    }

    /**
     * Accessor for isUnlocked
     * @return whether this achievement has been unlocked
     */
    public boolean isUnlocked() {
        return this.isUnlocked;
    }

    /**
     * Accessor for name
     * @return the name of this achievement
     */
    public String getName() {
        return this.name;
    }

    /**
     * Accessor for description
     * @return the description of this achievement
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Unlocks this achievement
     */
    public void unlock() {
        this.isUnlocked = true;
    }

    /**
     * Locks this achievement
     */
    public void lock() {
        this.isUnlocked = false;
    }

    // --- MISC ---

    /**
     * Returns a String representation of this achievement
     */
    @Override
    public String toString() {
        return this.getName() + " - " + this.getDescription();
    }

}
