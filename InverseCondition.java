public class InverseCondition extends AbstractCondition {

    private final AbstractCondition inverse;
        
    // --- CONSTRUCTOR ---

    /**
     * Constructor
     * @param inverse the Condition to define the inverse of
     */
    public InverseCondition(AbstractCondition inverse) {
        this.inverse = inverse;
    }

    // --- CHECKS ---

    /**
     * Checks whether this condition is met
     * @return the boolean value of this condition
     */
    @Override
    public boolean check() {
        return !this.inverse.check();
    }
    
}
