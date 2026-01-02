public abstract class AbstractCondition {
    
    public abstract boolean check();

    /**
     * Returns the inverse of this AbstractCondition
     * @return the inverse of this AbstractCondition
     */
    public InverseCondition getInverse() {
        return new InverseCondition(this);
    }

    /**
     * Returns a String representation of this AbstractCondition
     */
    @Override
    public String toString() {
        return Boolean.toString(this.check());
    }

    /**
     * Checks if all conditions in a given list are met
     * @param conditions the list of conditions to check
     * @return true if all conditions are met; false otherwise
     */
    public static boolean check(AbstractCondition[] conditions) {
        for (AbstractCondition c : conditions) {
            if (!c.check()) return false;
        }

        return true;
    }

    /**
     * Checks if at least one condition in a given list are met
     * @param conditions the list of conditions to check
     * @return true if at least one of the conditions is met; false otherwise
     */
    public static boolean checkAny(AbstractCondition[] conditions) {
        for (AbstractCondition c : conditions) {
            if (c.check()) return true;
        }

        return false;
    }

}
