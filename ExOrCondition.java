public class ExOrCondition extends AbstractCondition {

    private boolean bool;
    private final AbstractCondition[] conditions;
        
    // --- CONSTRUCTORS ---

    /**
     * Constructor
     * @param bool a simple boolean condition that is part of the exclusive-or statement
     * @param conditions the conditions that make up the exclusive-or statement
     */
    public ExOrCondition(boolean bool, AbstractCondition... conditions) {
        this.bool = bool;
        this.conditions = conditions;
    }

    /**
     * Constructor
     * @param conditions the conditions that make up the exclusive-or statement
     */
    public ExOrCondition(AbstractCondition... conditions) {
        this(false, conditions);
    }

    // --- CHECKS ---

    /**
     * Checks whether this condition is met
     * @return the boolean value of this condition
     */
    @Override
    public boolean check() {
        int conditionsMet = 0;
        if (this.bool) conditionsMet += 1;
        if (conditions.length != 0) {
            for (AbstractCondition c : conditions) {
                if (c.check()) conditionsMet += 1;
            }
        }
        return conditionsMet == 1;
    }

}