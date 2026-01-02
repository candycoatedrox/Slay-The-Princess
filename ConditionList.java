public class ConditionList extends AbstractCondition {

    private boolean bool;
    private final AbstractCondition[] conditions;
        
    // --- CONSTRUCTORS ---

    /**
     * Constructor
     * @param bool a simple boolean condition that is part of the and statement
     * @param conditions the conditions that make up the and statement
     */
    public ConditionList(boolean bool, AbstractCondition... conditions) {
        this.bool = bool;
        this.conditions = conditions;
    }

    /**
     * Constructor
     * @param conditions the conditions that make up the and statement
     */
    public ConditionList(AbstractCondition... conditions) {
        this(true, conditions);
    }

    // --- CHECKS ---

    /**
     * Checks whether this condition is met
     * @return the boolean value of this condition
     */
    @Override
    public boolean check() {
        if (!this.bool) return false;
        return AbstractCondition.check(this.conditions);
    }

}