public class Option {
    
    private GameManager manager;

    private String id;
    private String display;
    private final Integer maxTimesPicked; // if null, no max

    private Option prerequisiteOption;
    private Chapter leadsToChapter;

    private boolean conditionMet;
    private boolean greyedOut;
    private int timesPicked;

    // --- CONSTRUCTORS ---

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

    public Option(GameManager manager, String id, String display, int maxTimesPicked, Option prerequisiteOption, boolean conditionMet) {
        this(manager, id, display, maxTimesPicked, false, prerequisiteOption, null, conditionMet);
    }

    public Option(GameManager manager, String id, String display, int maxTimesPicked, Option prerequisiteOption) {
        this(manager, id, display, maxTimesPicked, false, prerequisiteOption, null, true);
    }

    public Option(GameManager manager, String id, String display, int maxTimesPicked) {
        this(manager, id, display, maxTimesPicked, false, null, null, true);
    }

    public Option(GameManager manager, String id, boolean greyedOut, String display, int maxTimesPicked) {
        this(manager, id, display, maxTimesPicked, greyedOut, null, null, true);
    }

    public Option(GameManager manager, String id, boolean greyedOut, String display, Option prerequisiteOption, boolean conditionMet) {
        this(manager, id, display, 1, greyedOut, prerequisiteOption, null, conditionMet);
    }

    public Option(GameManager manager, String id, boolean greyedOut, String display, Option prerequisiteOption) {
        this(manager, id, display, 1, greyedOut, prerequisiteOption, null, true);
    }

    public Option(GameManager manager, String id, boolean greyedOut, String display, Chapter leadsToChapter) {
        this(manager, id, display, 1, greyedOut, null, leadsToChapter, true);
    }

    public Option(GameManager manager, String id, boolean greyedOut, String display, boolean conditionMet) {
        this(manager, id, display, 1, greyedOut, null, null, conditionMet);
    }

    public Option(GameManager manager, String id, String display, Option prerequisiteOption, Chapter leadsToChapter, boolean conditionMet) {
        this(manager, id, display, 1, false, prerequisiteOption, leadsToChapter, conditionMet);
    }

    public Option(GameManager manager, String id, String display, int maxTimesPicked, Option prerequisiteOption, Chapter leadsToChapter) {
        this(manager, id, display, maxTimesPicked, false, prerequisiteOption, leadsToChapter, true);
    }

    public Option(GameManager manager, String id, String display, Option prerequisiteOption, Chapter leadsToChapter) {
        this(manager, id, display, 1, false, prerequisiteOption, leadsToChapter, true);
    }

    public Option(GameManager manager, String id, String display, Option prerequisiteOption, boolean conditionMet) {
        this(manager, id, display, 1, false, prerequisiteOption, null, conditionMet);
    }

    public Option(GameManager manager, String id, String display, Chapter leadsToChapter, boolean conditionMet) {
        this(manager, id, display, 1, false, null, leadsToChapter, conditionMet);
    }

    public Option(GameManager manager, String id, String display, Option prerequisiteOption) {
        this(manager, id, display, 1, false, prerequisiteOption, null, true);
    }

    public Option(GameManager manager, String id, String display, int maxTimesPicked, Chapter leadsToChapter) {
        this(manager, id, display, maxTimesPicked, false, null, leadsToChapter, true);
    }

    public Option(GameManager manager, String id, String display, int maxTimesPicked, boolean conditionMet) {
        this(manager, id, display, maxTimesPicked, false, null, null, conditionMet);
    }

    public Option(GameManager manager, String id, String display, Chapter leadsToChapter) {
        this(manager, id, display, 1, false, null, leadsToChapter, true);
    }

    public Option(GameManager manager, String id, String display, boolean conditionMet) {
        this(manager, id, display, 1, false, null, null, conditionMet);
    }

    public Option(GameManager manager, String id, boolean greyedOut, String display) {
        this(manager, id, display, 1, greyedOut, null, null, true);
    }

    public Option(GameManager manager, String id, String display) {
        this(manager, id, display, 1, false, null, null, true);
    }

    // --- ACCESSORS & MANIPULATORS ---

    public String getID() {
        return this.id;
    }

    public boolean hasBeenPicked() {
        return this.timesPicked > 0;
    }

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

    public boolean isAvailable() {
        return this.isShown() && !this.greyedOut();
    }

    public boolean greyedOut() {
        if (this.greyedOut) {
            return true;
        } else if (this.leadsToChapter != null) {
            return this.manager.hasVisited(this.leadsToChapter);
        }

        return false;
    }

    public void setPrerequisite(Option prerequisite) {
        this.prerequisiteOption = prerequisite;
    }

    public void setCondition(boolean isMet) {
        this.conditionMet = isMet;
    }

    public void setGreyedOut(boolean condition) {
        this.greyedOut = condition;
    }

    // --- MISC ---

    public void choose() {
        this.timesPicked += 1;
    }

    @Override
    public String toString() {
        return this.display;
    }

}
