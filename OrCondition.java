public class OrCondition extends AbstractCondition {

    private boolean bool;
    private final AbstractCondition[] conditions;
        
    // --- CONSTRUCTORS ---

    /**
     * Constructor
     * @param bool a simple boolean condition that is part of the or statement
     * @param conditions the conditions that make up the or statement
     */
    public OrCondition(boolean bool, AbstractCondition... conditions) {
        this.bool = bool;
        this.conditions = conditions;
    }

    /**
     * Constructor
     * @param conditions the conditions that make up the or statement
     */
    public OrCondition(AbstractCondition... conditions) {
        this(true, conditions);
    }

    // --- CHECKS ---

    /**
     * Checks whether this condition is met
     * @return the boolean value of this condition
     */
    @Override
    public boolean check() {
        if (this.bool || conditions.length == 0) return true;
        for (AbstractCondition c : conditions) {
            if (c.check()) return true;
        }
        return false;
    }

}