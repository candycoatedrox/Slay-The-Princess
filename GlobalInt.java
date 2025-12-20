public class GlobalInt {
    
    private int value;

    // --- CONSTRUCTORS ---

    /**
     * Constructor
     * @param value the starting value of the GlobalInt
     */
    public GlobalInt(int value) {
        this.value = value;
    }

    /**
     * Empty constructor (0)
     */
    public GlobalInt() {
        this(0);
    }

    // --- ACCESSORS & MANIPULATORS

    /**
     * Accessor for value
     * @return the integer value of this GlobalInt
     */
    public int check() {
        return this.value;
    }

    /**
     * Manipulator for value
     * @param newValue the new integer value of this GlobalInt
     */
    public void set(int newValue) {
        this.value = newValue;
    }

    /**
     * Add an int to this GlobalInt
     * @param n the integer to add to this GlobalInt
     */
    public void add(int n) {
        this.value += n;
    }

    /**
     * Increment this GlobalInt by 1
     */
    public void increment() {
        this.value += 1;
    }

    /**
     * Subtract an int from this GlobalInt
     * @param n the integer to subtract from this GlobalInt
     */
    public void subtract(int n) {
        this.value -= n;
    }

    /**
     * Decrement this GlobalInt by 1
     */
    public void decrement() {
        this.value -= 1;
    }

    /**
     * Returns a String representation of this AbstractCondition
     */
    @Override
    public String toString() {
        return Integer.toString(this.check());
    }

}
