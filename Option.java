public class Option {
    
    /*
     * There are multiple ways of determining whether a given option is available, which can be combined:
     * - unavailable if has already been picked (on by default)
     * - unavailable if has already been picked (X) times
     * - only available if a given variable is equal to a given value (can be boolean or other)
     * - only available once another option in this menu has been picked
     */
    
    private GameManager manager;

    private String id;
    private String display;
    private final int maxTimesPicked; // If 0, there is no max

    private Option prerequisiteOption;
    private Chapter leadsToChapter;

    private boolean conditionMet;
    private boolean greyedOut;
    private int timesPicked;

    // --- CONSTRUCTORS ---

    /**
     * Constructor including all attributes; should only ever be called by other constructors
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param display the text displayed to the player for the Option
     * @param maxTimesPicked the maximum number of times the Option can be picked; a value of 0 means there is no maximum
     * @param greyedOut whether the Option is initially greyed out or not
     * @param prerequisiteOption another Option that must be chosen before this Option appears
     * @param leadsToChapter the Chapter that the Option leads to
     * @param conditionMet whether other conditions necessary for the Option to be available are met
     */
    private Option(GameManager manager, String id, String display, int maxTimesPicked, boolean greyedOut, Option prerequisiteOption, Chapter leadsToChapter, boolean conditionMet) {
        this.manager = manager;
        
        this.id = id;
        this.display = display;
        this.maxTimesPicked = maxTimesPicked;
        this.greyedOut = greyedOut;

        this.prerequisiteOption = prerequisiteOption;
        this.leadsToChapter = leadsToChapter;

        this.conditionMet = conditionMet;
        this.timesPicked = 0;
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param display the text displayed to the player for the Option
     * @param maxTimesPicked the maximum number of times the Option can be picked; a value of 0 means there is no maximum
     * @param prerequisiteOption another Option that must be chosen before this Option appears
     * @param conditionMet whether other conditions necessary for the Option to be available are met
     */
    public Option(GameManager manager, String id, String display, int maxTimesPicked, Option prerequisiteOption, boolean conditionMet) {
        this(manager, id, display, maxTimesPicked, false, prerequisiteOption, null, conditionMet);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param display the text displayed to the player for the Option
     * @param maxTimesPicked the maximum number of times the Option can be picked; a value of 0 means there is no maximum
     * @param prerequisiteOption another Option that must be chosen before this Option appears
     */
    public Option(GameManager manager, String id, String display, int maxTimesPicked, Option prerequisiteOption) {
        this(manager, id, display, maxTimesPicked, false, prerequisiteOption, null, true);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param display the text displayed to the player for the Option
     * @param maxTimesPicked the maximum number of times the Option can be picked; a value of 0 means there is no maximum
     */
    public Option(GameManager manager, String id, String display, int maxTimesPicked) {
        this(manager, id, display, maxTimesPicked, false, null, null, true);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param greyedOut whether the Option is initially greyed out or not
     * @param display the text displayed to the player for the Option
     * @param maxTimesPicked the maximum number of times the Option can be picked; a value of 0 means there is no maximum
     */
    public Option(GameManager manager, String id, boolean greyedOut, String display, int maxTimesPicked) {
        this(manager, id, display, maxTimesPicked, greyedOut, null, null, true);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param greyedOut whether the Option is initially greyed out or not
     * @param display the text displayed to the player for the Option
     * @param prerequisiteOption another Option that must be chosen before this Option appears
     * @param conditionMet whether other conditions necessary for the Option to be available are met
     */
    public Option(GameManager manager, String id, boolean greyedOut, String display, Option prerequisiteOption, boolean conditionMet) {
        this(manager, id, display, 1, greyedOut, prerequisiteOption, null, conditionMet);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param greyedOut whether the Option is initially greyed out or not
     * @param display the text displayed to the player for the Option
     * @param prerequisiteOption another Option that must be chosen before this Option appears
     */
    public Option(GameManager manager, String id, boolean greyedOut, String display, Option prerequisiteOption) {
        this(manager, id, display, 1, greyedOut, prerequisiteOption, null, true);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param greyedOut whether the Option is initially greyed out or not
     * @param display the text displayed to the player for the Option
     * @param leadsToChapter the Chapter that the Option leads to
     */
    public Option(GameManager manager, String id, boolean greyedOut, String display, Chapter leadsToChapter) {
        this(manager, id, display, 1, greyedOut, null, leadsToChapter, true);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param greyedOut whether the Option is initially greyed out or not
     * @param display the text displayed to the player for the Option
     * @param conditionMet whether other conditions necessary for the Option to be available are met
     */
    public Option(GameManager manager, String id, boolean greyedOut, String display, boolean conditionMet) {
        this(manager, id, display, 1, greyedOut, null, null, conditionMet);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param display the text displayed to the player for the Option
     * @param prerequisiteOption another Option that must be chosen before this Option appears
     * @param leadsToChapter the Chapter that the Option leads to
     * @param conditionMet whether other conditions necessary for the Option to be available are met
     */
    public Option(GameManager manager, String id, String display, Option prerequisiteOption, Chapter leadsToChapter, boolean conditionMet) {
        this(manager, id, display, 1, false, prerequisiteOption, leadsToChapter, conditionMet);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param greyedOut whether the Option is initially greyed out or not
     * @param display the text displayed to the player for the Option
     * @param maxTimesPicked the maximum number of times the Option can be picked; a value of 0 means there is no maximum
     * @param prerequisiteOption another Option that must be chosen before this Option appears
     * @param leadsToChapter the Chapter that the Option leads to
     */
    public Option(GameManager manager, String id, String display, int maxTimesPicked, Option prerequisiteOption, Chapter leadsToChapter) {
        this(manager, id, display, maxTimesPicked, false, prerequisiteOption, leadsToChapter, true);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param display the text displayed to the player for the Option
     * @param prerequisiteOption another Option that must be chosen before this Option appears
     * @param leadsToChapter the Chapter that the Option leads to
     */
    public Option(GameManager manager, String id, String display, Option prerequisiteOption, Chapter leadsToChapter) {
        this(manager, id, display, 1, false, prerequisiteOption, leadsToChapter, true);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param display the text displayed to the player for the Option
     * @param prerequisiteOption another Option that must be chosen before this Option appears
     * @param conditionMet whether other conditions necessary for the Option to be available are met
     */
    public Option(GameManager manager, String id, String display, Option prerequisiteOption, boolean conditionMet) {
        this(manager, id, display, 1, false, prerequisiteOption, null, conditionMet);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param display the text displayed to the player for the Option
     * @param leadsToChapter the Chapter that the Option leads to
     * @param conditionMet whether other conditions necessary for the Option to be available are met
     */
    public Option(GameManager manager, String id, String display, Chapter leadsToChapter, boolean conditionMet) {
        this(manager, id, display, 1, false, null, leadsToChapter, conditionMet);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param display the text displayed to the player for the Option
     * @param prerequisiteOption another Option that must be chosen before this Option appears
     */
    public Option(GameManager manager, String id, String display, Option prerequisiteOption) {
        this(manager, id, display, 1, false, prerequisiteOption, null, true);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param display the text displayed to the player for the Option
     * @param maxTimesPicked the maximum number of times the Option can be picked; a value of 0 means there is no maximum
     * @param leadsToChapter the Chapter that the Option leads to
     */
    public Option(GameManager manager, String id, String display, int maxTimesPicked, Chapter leadsToChapter) {
        this(manager, id, display, maxTimesPicked, false, null, leadsToChapter, true);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param display the text displayed to the player for the Option
     * @param maxTimesPicked the maximum number of times the Option can be picked; a value of 0 means there is no maximum
     * @param conditionMet whether other conditions necessary for the Option to be available are met
     */
    public Option(GameManager manager, String id, String display, int maxTimesPicked, boolean conditionMet) {
        this(manager, id, display, maxTimesPicked, false, null, null, conditionMet);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param display the text displayed to the player for the Option
     * @param leadsToChapter the Chapter that the Option leads to
     */
    public Option(GameManager manager, String id, String display, Chapter leadsToChapter) {
        this(manager, id, display, 1, false, null, leadsToChapter, true);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param display the text displayed to the player for the Option
     * @param conditionMet whether other conditions necessary for the Option to be available are met
     */
    public Option(GameManager manager, String id, String display, boolean conditionMet) {
        this(manager, id, display, 1, false, null, null, conditionMet);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param greyedOut whether the Option is initially greyed out or not
     * @param display the text displayed to the player for the Option
     */
    public Option(GameManager manager, String id, boolean greyedOut, String display) {
        this(manager, id, display, 1, greyedOut, null, null, true);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param display the text displayed to the player for the Option
     */
    public Option(GameManager manager, String id, String display) {
        this(manager, id, display, 1, false, null, null, true);
    }

    // --- ACCESSORS & MANIPULATORS ---

    /**
     * Accessor for id
     * @return the shorthand ID of this Option
     */
    public String getID() {
        return this.id;
    }

    /**
     * Checks if this Option has been picked at least once
     * @return false if this Option has never been picked; true otherwise
     */
    public boolean hasBeenPicked() {
        return this.timesPicked > 0;
    }

    /**
     * Checks if this Option is visible
     * @return true if this Option is visible
     */
    public boolean isShown() {
        if (this.timesPicked >= this.maxTimesPicked && this.maxTimesPicked != 0) {
            return false;
        } else if (!this.conditionMet) {
            return false;
        } else if (this.prerequisiteOption != null) {
            if (!this.prerequisiteOption.hasBeenPicked()) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Checks if this Option is available to be chosen by the player
     * @return true if this Option is both visible and not greyed out; false otherwise
     */
    public boolean isAvailable() {
        return this.isShown() && !this.greyedOut();
    }

    /**
     * Checks if this Option is greyed out (i.e. if it is unvavailable, even if visible)
     * @return true if this option is greyed out; false otherwise
     */
    public boolean greyedOut() {
        if (this.greyedOut) {
            return true;
        } else if (this.leadsToChapter != null) {
            return this.manager.hasVisited(this.leadsToChapter);
        }

        return false;
    }

    /**
     * Manipulator for prerequisiteOption
     * @param prerequisite the new prerequisite Option of this Option
     */
    public void setPrerequisite(Option prerequisite) {
        this.prerequisiteOption = prerequisite;
    }

    /**
     * Manipulator for conditionMet
     * @param isMet whether miscellaneous conditions necessary for this Option to be available are met
     */
    public void setCondition(boolean isMet) {
        this.conditionMet = isMet;
    }

    /**
     * Manipulator for greyedOut
     * @param condition whether miscellaneous conditions necessary for this Option to become greyed out are met
     */
    public void setGreyedOut(boolean condition) {
        this.greyedOut = condition;
    }

    // --- MISC ---

    /**
     * Increments the number of times this Option has been picked
     */
    public void choose() {
        this.timesPicked += 1;
    }

    /**
     * Returns the display text of this Option
     * @return the display text of this Option
     */
    @Override
    public String toString() {
        return this.display;
    }

}
