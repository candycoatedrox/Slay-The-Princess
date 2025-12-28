public class Achievement {
    
    private final String id;
    private final Chapter origin;
    private boolean isUnlocked = false;

    private final String name;
    private final String description;


    // --- CONSTRUCTOR ---

    /**
     * Constructor
     * @param id the internal ID of this achievement
     * @param origin the chapter this achievement is unlocked in
     * @param name the name of this achievement
     * @param description the description of this achievement
     */
    public Achievement(String id, Chapter origin, String name, String description) {
        this.id = id;
        this.origin = origin;
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
        return this.getName() + ": " + this.getDescription();
    }

}
