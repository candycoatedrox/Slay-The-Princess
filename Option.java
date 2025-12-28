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
    private AbstractCondition[] conditions;
    private boolean greyedOut;
    private AbstractCondition greyCondition;
    private boolean strangerEnding = false;

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
     * @param conditions an array of Conditions that must be met for the Option to be available
     */
    private Option(GameManager manager, String id, String display, int maxTimesPicked, boolean greyedOut, Option prerequisiteOption, Chapter leadsToChapter, boolean conditionMet, AbstractCondition... conditions) {
        this.manager = manager;
        
        this.id = id;
        this.display = display;
        this.maxTimesPicked = maxTimesPicked;
        this.greyedOut = greyedOut;
        this.greyCondition = new Condition(false);

        this.prerequisiteOption = prerequisiteOption;
        this.leadsToChapter = leadsToChapter;

        this.conditionMet = conditionMet;
        this.conditions = conditions;
        this.timesPicked = 0;
    }

    /**
     * Constructor including all attributes; should only ever be called by other constructors
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param display the text displayed to the player for the Option
     * @param maxTimesPicked the maximum number of times the Option can be picked; a value of 0 means there is no maximum
     * @param greyCondition a Condition that must be met for the Option to be greyed out
     * @param prerequisiteOption another Option that must be chosen before this Option appears
     * @param leadsToChapter the Chapter that the Option leads to
     * @param conditionMet whether other conditions necessary for the Option to be available are met
     * @param conditions an array of Conditions that must be met for the Option to be available
     */
    private Option(GameManager manager, String id, String display, int maxTimesPicked, AbstractCondition greyCondition, Option prerequisiteOption, Chapter leadsToChapter, boolean conditionMet, AbstractCondition... conditions) {
        this.manager = manager;
        
        this.id = id;
        this.display = display;
        this.maxTimesPicked = maxTimesPicked;
        this.greyedOut = false;
        this.greyCondition = greyCondition;

        this.prerequisiteOption = prerequisiteOption;
        this.leadsToChapter = leadsToChapter;

        this.conditionMet = conditionMet;
        this.conditions = conditions;
        this.timesPicked = 0;
    }

    /**
     * Constructor including all attributes except conditions; should only ever be called by other constructors
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param display the text displayed to the player for the Option
     * @param maxTimesPicked the maximum number of times the Option can be picked; a value of 0 means there is no maximum
     * @param greyCondition a Condition that must be met for the Option to be greyed out
     * @param prerequisiteOption another Option that must be chosen before this Option appears
     * @param leadsToChapter the Chapter that the Option leads to
     * @param conditionMet whether other conditions necessary for the Option to be available are met
     */
    private Option(GameManager manager, String id, String display, int maxTimesPicked, AbstractCondition greyCondition, Option prerequisiteOption, Chapter leadsToChapter, boolean conditionMet) {
        this(manager, id, display, maxTimesPicked, greyCondition, prerequisiteOption, leadsToChapter, conditionMet, new AbstractCondition[0]);
    }

    /**
     * Constructor including all attributes except conditions; should only ever be called by other constructors
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
        this(manager, id, display, maxTimesPicked, greyedOut, prerequisiteOption, leadsToChapter, conditionMet, new AbstractCondition[0]);
    }

    /**
     * Constructor including all attributes except conditionMet; should only ever be called by other constructors
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param display the text displayed to the player for the Option
     * @param maxTimesPicked the maximum number of times the Option can be picked; a value of 0 means there is no maximum
     * @param greyCondition a Condition that must be met for the Option to be greyed out
     * @param prerequisiteOption another Option that must be chosen before this Option appears
     * @param leadsToChapter the Chapter that the Option leads to
     * @param conditions an array of Conditions that must be met for the Option to be available
     */
    private Option(GameManager manager, String id, String display, int maxTimesPicked, Condition greyCondition, Option prerequisiteOption, Chapter leadsToChapter, AbstractCondition[] conditions) {
        this(manager, id, display, maxTimesPicked, greyCondition, prerequisiteOption, leadsToChapter, true, conditions);
    }

    /**
     * Constructor including all attributes except conditionMet; should only ever be called by other constructors
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param display the text displayed to the player for the Option
     * @param maxTimesPicked the maximum number of times the Option can be picked; a value of 0 means there is no maximum
     * @param greyedOut whether the Option is initially greyed out or not
     * @param prerequisiteOption another Option that must be chosen before this Option appears
     * @param leadsToChapter the Chapter that the Option leads to
     * @param conditions an array of Conditions that must be met for the Option to be available
     */
    private Option(GameManager manager, String id, String display, int maxTimesPicked, boolean greyedOut, Option prerequisiteOption, Chapter leadsToChapter, AbstractCondition[] conditions) {
        this(manager, id, display, maxTimesPicked, greyedOut, prerequisiteOption, leadsToChapter, true, conditions);
    }

    /**
     * Constructor including all attributes except conditionMet; should only ever be called by other constructors
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param display the text displayed to the player for the Option
     * @param maxTimesPicked the maximum number of times the Option can be picked; a value of 0 means there is no maximum
     * @param greyCondition a Condition that must be met for the Option to be greyed out
     * @param prerequisiteOption another Option that must be chosen before this Option appears
     * @param leadsToChapter the Chapter that the Option leads to
     * @param conditions an array of Conditions that must be met for the Option to be available
     */
    private Option(GameManager manager, String id, String display, int maxTimesPicked, AbstractCondition greyCondition, Option prerequisiteOption, Chapter leadsToChapter, AbstractCondition[] conditions) {
        this(manager, id, display, maxTimesPicked, greyCondition, prerequisiteOption, leadsToChapter, true, conditions);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param display the text displayed to the player for the Option
     * @param maxTimesPicked the maximum number of times the Option can be picked; a value of 0 means there is no maximum
     * @param prerequisiteOption another Option that must be chosen before this Option appears
     * @param conditionMet whether other conditions necessary for the Option to be available are met
     * @param conditions an array of Conditions that must be met for the Option to be available
     */
    public Option(GameManager manager, String id, String display, int maxTimesPicked, Option prerequisiteOption, boolean conditionMet, AbstractCondition... conditions) {
        this(manager, id, display, maxTimesPicked, false, prerequisiteOption, null, conditionMet, conditions);
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
     * @param conditions an array of Conditions that must be met for the Option to be available
     */
    public Option(GameManager manager, String id, String display, int maxTimesPicked, Option prerequisiteOption, AbstractCondition... conditions) {
        this(manager, id, display, maxTimesPicked, false, prerequisiteOption, null, conditions);
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
     * @param greyCondition a Condition that must be met for the Option to be greyed out
     * @param display the text displayed to the player for the Option
     * @param maxTimesPicked the maximum number of times the Option can be picked; a value of 0 means there is no maximum
     */
    public Option(GameManager manager, String id, AbstractCondition greyCondition, String display, int maxTimesPicked) {
        this(manager, id, display, maxTimesPicked, greyCondition, null, null, true);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param greyedOut whether the Option is initially greyed out or not
     * @param display the text displayed to the player for the Option
     * @param prerequisiteOption another Option that must be chosen before this Option appears
     * @param conditionMet whether other conditions necessary for the Option to be available are met
     * @param conditions an array of Conditions that must be met for the Option to be available
     */
    public Option(GameManager manager, String id, boolean greyedOut, String display, Option prerequisiteOption, boolean conditionMet, AbstractCondition... conditions) {
        this(manager, id, display, 1, greyedOut, prerequisiteOption, null, conditionMet, conditions);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param greyCondition a Condition that must be met for the Option to be greyed out
     * @param display the text displayed to the player for the Option
     * @param prerequisiteOption another Option that must be chosen before this Option appears
     * @param conditionMet whether other conditions necessary for the Option to be available are met
     * @param conditions an array of Conditions that must be met for the Option to be available
     */
    public Option(GameManager manager, String id, AbstractCondition greyCondition, String display, Option prerequisiteOption, boolean conditionMet, AbstractCondition... conditions) {
        this(manager, id, display, 1, greyCondition, prerequisiteOption, null, conditionMet, conditions);
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
     * @param greyCondition a Condition that must be met for the Option to be greyed out
     * @param display the text displayed to the player for the Option
     * @param prerequisiteOption another Option that must be chosen before this Option appears
     * @param conditionMet whether other conditions necessary for the Option to be available are met
     */
    public Option(GameManager manager, String id, AbstractCondition greyCondition, String display, Option prerequisiteOption, boolean conditionMet) {
        this(manager, id, display, 1, greyCondition, prerequisiteOption, null, conditionMet);
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
     * @param greyCondition a Condition that must be met for the Option to be greyed out
     * @param display the text displayed to the player for the Option
     * @param prerequisiteOption another Option that must be chosen before this Option appears
     */
    public Option(GameManager manager, String id, AbstractCondition greyCondition, String display, Option prerequisiteOption) {
        this(manager, id, display, 1, greyCondition, prerequisiteOption, null, true);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param greyCondition a Condition that must be met for the Option to be greyed out
     * @param display the text displayed to the player for the Option
     * @param leadsToChapter the Chapter that the Option leads to
     * @param conditionMet whether other conditions necessary for the Option to be available are met
     * @param conditions an array of Conditions that must be met for the Option to be available
     */
    public Option(GameManager manager, String id, AbstractCondition greyCondition, String display, Chapter leadsToChapter, boolean conditionMet, AbstractCondition... conditions) {
        this(manager, id, display, 1, greyCondition, null, leadsToChapter, conditionMet, conditions);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param greyedOut whether the Option is initially greyed out or not
     * @param display the text displayed to the player for the Option
     * @param leadsToChapter the Chapter that the Option leads to
     * @param conditionMet whether other conditions necessary for the Option to be available are met
     * @param conditions an array of Conditions that must be met for the Option to be available
     */
    public Option(GameManager manager, String id, boolean greyedOut, String display, Chapter leadsToChapter, boolean conditionMet, AbstractCondition... conditions) {
        this(manager, id, display, 1, greyedOut, null, leadsToChapter, conditionMet, conditions);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param greyCondition a Condition that must be met for the Option to be greyed out
     * @param display the text displayed to the player for the Option
     * @param leadsToChapter the Chapter that the Option leads to
     * @param conditionMet whether other conditions necessary for the Option to be available are met
     */
    public Option(GameManager manager, String id, AbstractCondition greyCondition, String display, Chapter leadsToChapter, boolean conditionMet) {
        this(manager, id, display, 1, greyCondition, null, leadsToChapter, conditionMet);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param greyedOut whether the Option is initially greyed out or not
     * @param display the text displayed to the player for the Option
     * @param leadsToChapter the Chapter that the Option leads to
     * @param conditionMet whether other conditions necessary for the Option to be available are met
     */
    public Option(GameManager manager, String id, boolean greyedOut, String display, Chapter leadsToChapter, boolean conditionMet) {
        this(manager, id, display, 1, greyedOut, null, leadsToChapter, conditionMet);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param greyCondition a Condition that must be met for the Option to be greyed out
     * @param display the text displayed to the player for the Option
     * @param leadsToChapter the Chapter that the Option leads to
     * @param conditions an array of Conditions that must be met for the Option to be available
     */
    public Option(GameManager manager, String id, AbstractCondition greyCondition, String display, Chapter leadsToChapter, AbstractCondition... conditions) {
        this(manager, id, display, 1, greyCondition, null, leadsToChapter, conditions);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param greyedOut whether the Option is initially greyed out or not
     * @param display the text displayed to the player for the Option
     * @param leadsToChapter the Chapter that the Option leads to
     * @param conditions an array of Conditions that must be met for the Option to be available
     */
    public Option(GameManager manager, String id, boolean greyedOut, String display, Chapter leadsToChapter,AbstractCondition... conditions) {
        this(manager, id, display, 1, greyedOut, null, leadsToChapter, conditions);
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
     * @param greyCondition a Condition that must be met for the Option to be greyed out
     * @param display the text displayed to the player for the Option
     * @param leadsToChapter the Chapter that the Option leads to
     */
    public Option(GameManager manager, String id, AbstractCondition greyCondition, String display, Chapter leadsToChapter) {
        this(manager, id, display, 1, greyCondition, null, leadsToChapter, true);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param greyedOut whether the Option is initially greyed out or not
     * @param display the text displayed to the player for the Option
     * @param conditionMet whether other conditions necessary for the Option to be available are met
     * @param conditions an array of Conditions that must be met for the Option to be available
     */
    public Option(GameManager manager, String id, boolean greyedOut, String display, boolean conditionMet, AbstractCondition... conditions) {
        this(manager, id, display, 1, greyedOut, null, null, conditionMet, conditions);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param greyCondition a Condition that must be met for the Option to be greyed out
     * @param display the text displayed to the player for the Option
     * @param conditionMet whether other conditions necessary for the Option to be available are met
     * @param conditions an array of Conditions that must be met for the Option to be available
     */
    public Option(GameManager manager, String id, AbstractCondition greyCondition, String display, boolean conditionMet, AbstractCondition... conditions) {
        this(manager, id, display, 1, greyCondition, null, null, conditionMet, conditions);
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
     * @param greyCondition a Condition that must be met for the Option to be greyed out
     * @param display the text displayed to the player for the Option
     * @param conditionMet whether other conditions necessary for the Option to be available are met
     */
    public Option(GameManager manager, String id, AbstractCondition greyCondition, String display, boolean conditionMet) {
        this(manager, id, display, 1, greyCondition, null, null, conditionMet);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param greyedOut whether the Option is initially greyed out or not
     * @param display the text displayed to the player for the Option
     * @param conditions an array of Conditions that must be met for the Option to be available
     */
    public Option(GameManager manager, String id, boolean greyedOut, String display, AbstractCondition... conditions) {
        this(manager, id, display, 1, greyedOut, null, null, conditions);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param greyCondition a Condition that must be met for the Option to be greyed out
     * @param display the text displayed to the player for the Option
     * @param conditions an array of Conditions that must be met for the Option to be available
     */
    public Option(GameManager manager, String id, AbstractCondition greyCondition, String display, AbstractCondition... conditions) {
        this(manager, id, display, 1, greyCondition, null, null, conditions);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param display the text displayed to the player for the Option
     * @param prerequisiteOption another Option that must be chosen before this Option appears
     * @param leadsToChapter the Chapter that the Option leads to
     * @param conditionMet whether other conditions necessary for the Option to be available are met
     * @param conditions an array of Conditions that must be met for the Option to be available
     */
    public Option(GameManager manager, String id, String display, Option prerequisiteOption, Chapter leadsToChapter, boolean conditionMet, AbstractCondition... conditions) {
        this(manager, id, display, 1, false, prerequisiteOption, leadsToChapter, conditionMet, conditions);
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
     * @param display the text displayed to the player for the Option
     * @param prerequisiteOption another Option that must be chosen before this Option appears
     * @param leadsToChapter the Chapter that the Option leads to
     * @param conditions an array of Conditions that must be met for the Option to be available
     */
    public Option(GameManager manager, String id, String display, Option prerequisiteOption, Chapter leadsToChapter, AbstractCondition... conditions) {
        this(manager, id, display, 1, false, prerequisiteOption, leadsToChapter, conditions);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param greyedOut whether the Option is initially greyed out or not
     * @param display the text displayed to the player for the Option
     * @param maxTimesPicked the maximum number of times the Option can be picked; a value of 0 means there is no maximum
     * @param prerequisiteOption another Option that must be chosen before this Option appears
     * @param conditionMet whether other conditions necessary for the Option to be available are met
     * @param conditions an array of Conditions that must be met for the Option to be available
     */
    public Option(GameManager manager, String id, boolean greyedOut, String display, int maxTimesPicked, Option prerequisiteOption, boolean conditionMet, AbstractCondition... conditions) {
        this(manager, id, display, maxTimesPicked, greyedOut, prerequisiteOption, null, conditionMet, conditions);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param greyCondition a Condition that must be met for the Option to be greyed out
     * @param display the text displayed to the player for the Option
     * @param maxTimesPicked the maximum number of times the Option can be picked; a value of 0 means there is no maximum
     * @param prerequisiteOption another Option that must be chosen before this Option appears
     * @param conditionMet whether other conditions necessary for the Option to be available are met
     * @param conditions an array of Conditions that must be met for the Option to be available
     */
    public Option(GameManager manager, String id, AbstractCondition greyCondition, String display, int maxTimesPicked, Option prerequisiteOption, boolean conditionMet, AbstractCondition... conditions) {
        this(manager, id, display, maxTimesPicked, greyCondition, prerequisiteOption, null, conditionMet, conditions);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param greyedOut whether the Option is initially greyed out or not
     * @param display the text displayed to the player for the Option
     * @param maxTimesPicked the maximum number of times the Option can be picked; a value of 0 means there is no maximum
     * @param prerequisiteOption another Option that must be chosen before this Option appears
     * @param conditionMet whether other conditions necessary for the Option to be available are met
     */
    public Option(GameManager manager, String id, boolean greyedOut, String display, int maxTimesPicked, Option prerequisiteOption, boolean conditionMet) {
        this(manager, id, display, maxTimesPicked, greyedOut, prerequisiteOption, null, conditionMet);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param greyCondition a Condition that must be met for the Option to be greyed out
     * @param display the text displayed to the player for the Option
     * @param maxTimesPicked the maximum number of times the Option can be picked; a value of 0 means there is no maximum
     * @param prerequisiteOption another Option that must be chosen before this Option appears
     * @param conditionMet whether other conditions necessary for the Option to be available are met
     */
    public Option(GameManager manager, String id, AbstractCondition greyCondition, String display, int maxTimesPicked, Option prerequisiteOption, boolean conditionMet) {
        this(manager, id, display, maxTimesPicked, greyCondition, prerequisiteOption, null, conditionMet);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param greyedOut whether the Option is initially greyed out or not
     * @param display the text displayed to the player for the Option
     * @param maxTimesPicked the maximum number of times the Option can be picked; a value of 0 means there is no maximum
     * @param prerequisiteOption another Option that must be chosen before this Option appears
     * @param conditions an array of Conditions that must be met for the Option to be available
     */
    public Option(GameManager manager, String id, boolean greyedOut, String display, int maxTimesPicked, Option prerequisiteOption, AbstractCondition... conditions) {
        this(manager, id, display, maxTimesPicked, greyedOut, prerequisiteOption, null, conditions);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param greyCondition a Condition that must be met for the Option to be greyed out
     * @param display the text displayed to the player for the Option
     * @param maxTimesPicked the maximum number of times the Option can be picked; a value of 0 means there is no maximum
     * @param prerequisiteOption another Option that must be chosen before this Option appears
     * @param conditions an array of Conditions that must be met for the Option to be available
     */
    public Option(GameManager manager, String id, AbstractCondition greyCondition, String display, int maxTimesPicked, Option prerequisiteOption, AbstractCondition... conditions) {
        this(manager, id, display, maxTimesPicked, greyCondition, prerequisiteOption, null, conditions);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
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
     * @param greyedOut whether the Option is initially greyed out or not
     * @param display the text displayed to the player for the Option
     * @param maxTimesPicked the maximum number of times the Option can be picked; a value of 0 means there is no maximum
     * @param conditionMet whether other conditions necessary for the Option to be available are met
     * @param conditions an array of Conditions that must be met for the Option to be available
     */
    public Option(GameManager manager, String id, boolean greyedOut, String display, int maxTimesPicked, boolean conditionMet, AbstractCondition... conditions) {
        this(manager, id, display, maxTimesPicked, greyedOut, null, null, conditionMet, conditions);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param greyCondition a Condition that must be met for the Option to be greyed out
     * @param display the text displayed to the player for the Option
     * @param maxTimesPicked the maximum number of times the Option can be picked; a value of 0 means there is no maximum
     * @param conditionMet whether other conditions necessary for the Option to be available are met
     * @param conditions an array of Conditions that must be met for the Option to be available
     */
    public Option(GameManager manager, String id, AbstractCondition greyCondition, String display, int maxTimesPicked, boolean conditionMet, AbstractCondition... conditions) {
        this(manager, id, display, maxTimesPicked, greyCondition, null, null, conditionMet, conditions);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param greyedOut whether the Option is initially greyed out or not
     * @param display the text displayed to the player for the Option
     * @param maxTimesPicked the maximum number of times the Option can be picked; a value of 0 means there is no maximum
     * @param conditionMet whether other conditions necessary for the Option to be available are met
     */
    public Option(GameManager manager, String id, boolean greyedOut, String display, int maxTimesPicked, boolean conditionMet) {
        this(manager, id, display, maxTimesPicked, greyedOut, null, null, conditionMet);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param greyCondition a Condition that must be met for the Option to be greyed out
     * @param display the text displayed to the player for the Option
     * @param maxTimesPicked the maximum number of times the Option can be picked; a value of 0 means there is no maximum
     * @param conditionMet whether other conditions necessary for the Option to be available are met
     */
    public Option(GameManager manager, String id, AbstractCondition greyCondition, String display, int maxTimesPicked, boolean conditionMet) {
        this(manager, id, display, maxTimesPicked, greyCondition, null, null, conditionMet);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param greyedOut whether the Option is initially greyed out or not
     * @param display the text displayed to the player for the Option
     * @param maxTimesPicked the maximum number of times the Option can be picked; a value of 0 means there is no maximum
     * @param conditions an array of Conditions that must be met for the Option to be available
     */
    public Option(GameManager manager, String id, boolean greyedOut, String display, int maxTimesPicked, AbstractCondition... conditions) {
        this(manager, id, display, maxTimesPicked, greyedOut, null, null, conditions);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param greyCondition a Condition that must be met for the Option to be greyed out
     * @param display the text displayed to the player for the Option
     * @param maxTimesPicked the maximum number of times the Option can be picked; a value of 0 means there is no maximum
     * @param conditions an array of Conditions that must be met for the Option to be available
     */
    public Option(GameManager manager, String id, AbstractCondition greyCondition, String display, int maxTimesPicked, AbstractCondition... conditions) {
        this(manager, id, display, maxTimesPicked, greyCondition, null, null, conditions);
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
     * @param conditions an array of Conditions that must be met for the Option to be available
     */
    public Option(GameManager manager, String id, String display, Option prerequisiteOption, boolean conditionMet, AbstractCondition... conditions) {
        this(manager, id, display, 1, false, prerequisiteOption, null, conditionMet, conditions);
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
     * @param prerequisiteOption another Option that must be chosen before this Option appears
     * @param conditions an array of Conditions that must be met for the Option to be available
     */
    public Option(GameManager manager, String id, String display, Option prerequisiteOption, AbstractCondition... conditions) {
        this(manager, id, display, 1, false, prerequisiteOption, null, conditions);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param display the text displayed to the player for the Option
     * @param leadsToChapter the Chapter that the Option leads to
     * @param conditionMet whether other conditions necessary for the Option to be available are met
     * @param conditions an array of Conditions that must be met for the Option to be available
     */
    public Option(GameManager manager, String id, String display, Chapter leadsToChapter, boolean conditionMet, AbstractCondition... conditions) {
        this(manager, id, display, 1, false, null, leadsToChapter, conditionMet, conditions);
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
     * @param leadsToChapter the Chapter that the Option leads to
     * @param conditions an array of Conditions that must be met for the Option to be available
     */
    public Option(GameManager manager, String id, String display, Chapter leadsToChapter, AbstractCondition... conditions) {
        this(manager, id, display, 1, false, null, leadsToChapter, conditions);
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
     * @param greyedOut whether the Option is initially greyed out or not
     * @param display the text displayed to the player for the Option
     * @param maxTimesPicked the maximum number of times the Option can be picked; a value of 0 means there is no maximum
     * @param leadsToChapter the Chapter that the Option leads to
     */
    public Option(GameManager manager, String id, boolean greyedOut, String display, int maxTimesPicked, Chapter leadsToChapter) {
        this(manager, id, display, maxTimesPicked, greyedOut, null, leadsToChapter, true);
    }

    /**
     * Constructor
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param greyCondition a Condition that must be met for the Option to be greyed out
     * @param display the text displayed to the player for the Option
     * @param maxTimesPicked the maximum number of times the Option can be picked; a value of 0 means there is no maximum
     * @param leadsToChapter the Chapter that the Option leads to
     */
    public Option(GameManager manager, String id, AbstractCondition greyCondition, String display, int maxTimesPicked, Chapter leadsToChapter) {
        this(manager, id, display, maxTimesPicked, greyCondition, null, leadsToChapter, true);
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
     * @param conditions an array of Conditions that must be met for the Option to be available
     */
    public Option(GameManager manager, String id, String display, int maxTimesPicked, boolean conditionMet, AbstractCondition... conditions) {
        this(manager, id, display, maxTimesPicked, false, null, null, conditionMet, conditions);
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
     * @param maxTimesPicked the maximum number of times the Option can be picked; a value of 0 means there is no maximum
     * @param conditions an array of Conditions that must be met for the Option to be available
     */
    public Option(GameManager manager, String id, String display, int maxTimesPicked, AbstractCondition... conditions) {
        this(manager, id, display, maxTimesPicked, false, null, null, conditions);
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
     * @param conditions an array of Conditions that must be met for the Option to be available
     */
    public Option(GameManager manager, String id, String display, boolean conditionMet, AbstractCondition... conditions) {
        this(manager, id, display, 1, false, null, null, conditionMet, conditions);
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
     * @param display the text displayed to the player for the Option
     * @param conditions an array of Conditions that must be met for the Option to be available
     */
    public Option(GameManager manager, String id, String display, AbstractCondition... conditions) {
        this(manager, id, display, 1, false, null, null, conditions);
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
     * @param greyCondition a Condition that must be met for the Option to be greyed out
     * @param display the text displayed to the player for the Option
     */
    public Option(GameManager manager, String id, AbstractCondition greyCondition, String display) {
        this(manager, id, display, 1, greyCondition, null, null, true);
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

    /**
     * Constructor used exclusively for the special Option at the end of Chapter II: The Stranger
     * @param strangerEnding used to indicate this this Option is special
     * @param manager the GameManager to link this Option to
     * @param id the shorthand ID of the Option
     * @param display the text displayed to the player for the Option
     * @param condition a condition that must be met for the Option to be available
     */
    public Option(boolean strangerEnding, GameManager manager, String id, String display, AbstractCondition condition) {
        this(manager, id, display, 1, false, null, null, condition);
        this.strangerEnding = true;
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
     * Manipulator for id
     * @param newID the new shorthand ID of this Option
     */
    public void setID(String newID) {
        this.id = newID;
    }

    /**
     * Manipulator for display
     * @param newDisplay the new text displayed to the player for this Option
     * @return the previous display text of this Option
     */
    public String setDisplay(String newDisplay) {
        String prevDisplay = this.display;
        this.display = newDisplay;
        return prevDisplay;
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
        if ((this.maxTimesPicked != 0 && this.timesPicked >= this.maxTimesPicked) || !check(this.conditions) || !this.conditionMet) {
            return false;
        } else if (this.prerequisiteOption != null) {
            if (!this.prerequisiteOption.hasBeenPicked()) return false;
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
        if (this.greyedOut || this.greyCondition.check()) {
            return true;
        } else if (this.leadsToChapter != null) {
            return this.manager.hasVisited(this.leadsToChapter);
        }

        return false;
    }

    /**
     * Accessor for strangerEnding
     * @return whether this Option is the special Option at the end of Chapter II: The Stranger or not
     */
    public boolean isStrangerEnding() {
        return this.strangerEnding;
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
        this.greyCondition = new Condition();
    }

    /**
     * Manipulator for greyCondition
     * @param condition the AbstractCondition necessary for this Option to become greyed out are met
     */
    public void setGreyCondition(AbstractCondition condition) {
        this.greyedOut = false;
        this.greyCondition = condition;
    }

    // --- MISC ---

    /**
     * Increments the number of times this Option has been picked
     * @return this Option's ID
     */
    public String choose() {
        this.timesPicked += 1;
        return this.id;
    }

    /**
     * Returns the display text of this Option
     * @return the display text of this Option
     */
    @Override
    public String toString() {
        return this.display;
    }

    /**
     * Returns a copy of this Option
     * @return a copy of this Option
     */
    @Override
    public Option clone() {
        return new Option(this.manager, this.id, this.display, this.maxTimesPicked, this.greyedOut, this.prerequisiteOption, this.leadsToChapter, this.conditionMet);
    }

    /**
     * Checks if all conditions in a given list are met
     * @param conditions the list of conditions to check
     * @return true if all conditions are met; false otherwise
     */
    private static boolean check(AbstractCondition[] conditions) {
        for (AbstractCondition c : conditions) {
            if (!c.check()) return false;
        }

        return true;
    }

}
