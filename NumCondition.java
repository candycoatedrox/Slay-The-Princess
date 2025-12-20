public class NumCondition extends AbstractCondition {
    
    private GlobalInt dynamicValue;
    private final int condition;
    private final int targetValue;

    // --- CONSTRUCTORS ---

    /**
     * Constructor
     * @param dynamicValue the dynamic value for the condition to check
     * @param condition the sign of the relationship between the dynamic value and the target value (n == 0 - d == t; n > 0 - d > t; n < 0 - d < t)
     * @param targetValue the target value for the condition to check against
     */
    public NumCondition(GlobalInt dynamicValue, int condition, int targetValue) {
        this.dynamicValue = dynamicValue;
        this.condition = Integer.signum(condition);
        this.targetValue = targetValue;
    }

    /**
     * Constructor
     * @param initialValue the initial value for the condition's value to start off at
     * @param condition the sign of the relationship between the dynamic value and the target value (n == 0 - d == t; n > 0 - d > t; n < 0 - d < t)
     * @param targetValue the target value for the condition to check against
     */
    public NumCondition(int initialValue, int condition, int targetValue) {
        this(new GlobalInt(initialValue), condition, targetValue);
    }

    /**
     * Constructor
     * @param dynamicValue the dynamic value for the condition to check
     * @param targetValue the target value for the condition to check against
     */
    public NumCondition(GlobalInt dynamicValue, int targetValue) {
        this(dynamicValue, 0, targetValue);
    }

    /**
     * Constructor
     * @param initialValue the initial value for the condition's value to start off at
     * @param targetValue the target value for the condition to check against
     */
    public NumCondition(int initialValue, int targetValue) {
        this(new GlobalInt(initialValue), 0, targetValue);
    }

    // --- ACCESSORS & MANIPULATORS

    /**
     * Checks whether this condition is met
     * @return the boolean dynamicValue of this condition
     */
    @Override
    public boolean check() {
        switch (this.condition) {
            case -1: return this.dynamicValue.check() < this.targetValue;
            case 1: return this.dynamicValue.check() > this.targetValue;
            default: return this.dynamicValue.check() == this.targetValue;
        }
    }

    /**
     * Manipulator for the integer dynamicValue of this condition
     * @param newValue the new integer dynamicValue of this condition
     */
    public void set(int newValue) {
        this.dynamicValue.set(newValue);
    }

    /**
     * Add an int to the dynamicValue of this condition
     * @param n the integer to add to the dynamicValue of this condition
     */
    public void add(int n) {
        this.dynamicValue.add(n);
    }

    /**
     * Increment the dynamicValue of this condition by 1
     */
    public void increment() {
        this.dynamicValue.increment();
    }

    /**
     * Subtract an int from the dynamicValue of this condition
     * @param n the integer to subtract from the dynamicValue of this condition
     */
    public void subtract(int n) {
        this.dynamicValue.subtract(n);
    }

    /**
     * Decrement the dynamicValue of this condition by 1
     */
    public void decrement() {
        this.dynamicValue.decrement();
    }

}
