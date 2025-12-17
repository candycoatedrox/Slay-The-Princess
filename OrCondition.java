public class OrCondition extends AbstractCondition {

    private AbstractCondition[] conditions;
        
    // --- CONSTRUCTOR ---

    /**
     * Constructor
     * @param conditions the conditions that make up this or statement
     */
    public OrCondition(AbstractCondition... conditions) {
        this.conditions = conditions;
    }

    // --- CHECKS ---

    /**
     * Checks whether this condition is met
     * @return the boolean value of this condition
     */
    public boolean check() {
        for (AbstractCondition c : conditions) {
            if (c.check()) return true;
        }

        return false;
    }

}