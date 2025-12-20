public class Condition extends  AbstractCondition {
    
    private boolean value;
    
    // --- CONSTRUCTORS ---

    /**
     * Constructor
     * @param value the starting value of the condition
     */
    public Condition(boolean value) {
        this.value = value;
    }

    /**
     * Default constructor (false)
     */
    public Condition() {
        this(false);
    }

    // --- ACCESSORS & MANIPULATORS ---

    /**
     * Accessor for value
     * @return the boolean value of this condition
     */
    @Override
    public boolean check() {
        return this.value;
    }

    /**
     * Manipulator for value
     * @param newValue the new boolean value of this condition
     */
    public void set(boolean newValue) {
        this.value = newValue;
    }

    /**
     * Returns the inverse of this condition
     * @return the inverse of this condition
     */
    public InverseCondition getInverse() {
        return new InverseCondition(this);
    }

    /*
    public static void main(String[] args) {
        GameManager manager = new GameManager();
        IOHandler parser = new IOHandler(manager);
        OptionsMenu testMenu = new OptionsMenu();

        Condition testCon = new Condition(false);
        InverseCondition antiCon = new InverseCondition(testCon);

        testMenu.add(new Option(manager, "A", "The first option that triggers the condition", antiCon));
        testMenu.add(new Option(manager, "B", "Only available if you haven't chosen A or D, but doesn't update the condition", antiCon));
        testMenu.add(new Option(manager, "C", "Only available if you've chosen A or D", testCon));
        testMenu.add(new Option(manager, "D", "The second option that triggers the condition", antiCon));
        testMenu.add(new Option(manager, "E", "Normal option that's always here"));

        boolean repeat = true;
        String outcome;
        while (repeat) {
            outcome = parser.promptOptionsMenu(testMenu);
            switch (outcome) {
                case "A":
                case "D":
                    testCon.set(true);
            }

            System.out.println("You chose option " + outcome + "!");
            System.out.println("testCon = " + testCon.check() + "; antiCon = " + antiCon.check());
        }
    }
    */

}
