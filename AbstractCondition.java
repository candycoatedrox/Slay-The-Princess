public abstract class AbstractCondition {
    
    public abstract boolean check();

    /**
     * Returns a String representation of this AbstractCondition
     */
    @Override
    public String toString() {
        return Boolean.toString(this.check());
    }

}
