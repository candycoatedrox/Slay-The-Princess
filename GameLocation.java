public enum GameLocation {
    PATH,
    HILL,
    CABIN,
    CABINMIRROR,
    STAIRS,
    BASEMENT,

    BEFOREMIRROR,
    MIRROR,

    LEAVING;

    // --- ACCESSORS & CHECKS ---

    /**
     * Returns the location that is "forward" from this location
     * @param reverse whether forward and backward are currently "reversed" (for example, if the player is leaving the basement, using "forward" on the stairs should take them to the cabin and not the basement)
     * @return the location that is "forward" from this location
     */
    public GameLocation getForward(boolean reverse) {
        return (reverse) ? this.getBackward() : this.getForward();
    }

    /**
     * Returns the location that is "forward" from this location
     * @return the location that is "forward" from this location
     */
    public GameLocation getForward() {
        switch (this) {
            case LEAVING: return LEAVING;
            case PATH: return HILL;
            case HILL: return CABIN;
            case CABIN: return STAIRS;
            case STAIRS: return BASEMENT;
            case BEFOREMIRROR: return MIRROR;
            default: return null;
        }
    }

    /**
     * Returns the location that is "backward" from this location
     * @param reverse whether forward and backward are currently "reversed" (for example, if the player is leaving the basement, using "backward" on the stairs should take them to the basement and not the cabin)
     * @return the location that is "backward" from this location
     */
    public GameLocation getBackward(boolean reverse) {
        return (reverse) ? this.getForward() : this.getBackward();
    }

    /**
     * Returns the location that is "backward" from this location
     * @return the location that is "backward" from this location
     */
    public GameLocation getBackward() {
        switch (this) {
            case PATH: return LEAVING;
            case HILL: return LEAVING;
            case CABIN: return HILL;
            case CABINMIRROR: return CABIN;
            case STAIRS: return CABIN;
            case BASEMENT: return STAIRS;
            case LEAVING: return HILL;
            default: return null;
        }
    }

    /**
     * Checks whether the player can go "inside" from this location
     * @return true if the player can go "inside" from this location; false otherwise
     */
    public boolean canGoInside() {
        switch (this) {
            case HILL: return true;
            case CABIN: return true;
            case STAIRS: return true;
            default: return false;
        }
    }

    /**
     * Checks whether the player can go "outside" from this location
     * @return true if the player can go "outside" from this location; false otherwise
     */
    public boolean canGoOutside() {
        switch (this) {
            case CABIN: return true;
            case STAIRS: return true;
            case BASEMENT: return true;
            default: return false;
        }
    }

    /**
     * Checks whether the player can go "down" from this location
     * @return true if the player can go "down" from this location; false otherwise
     */
    public boolean canGoDown() {
        switch (this) {
            case CABIN: return true;
            case STAIRS: return true;
            default: return false;
        }
    }

    /**
     * Checks whether the player can go "up" from this location
     * @return true if the player can go "up" from this location; false otherwise
     */
    public boolean canGoUp() {
        switch (this) {
            case STAIRS: return true;
            case BASEMENT: return true;
            default: return false;
        }
    }

    /**
     * Returns a String representation of this location
     * @return a String representation of this location
     */
    @Override
    public String toString() {
        switch (this) {
            case PATH: return "path";
            case HILL: return "hill";
            case CABIN: return "cabin";
            case CABINMIRROR: return "mirror (cabin)";
            case STAIRS: return "stairs";
            case BASEMENT: return "basement";

            case BEFOREMIRROR: return "before mirror";
            case MIRROR: return "mirror";

            case LEAVING: return "leaving";
            
            default: return "n/a";
        }
    }
}
