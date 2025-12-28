import java.util.ArrayList;

public class ScriptIssue extends ScriptNote {
    
    /* --- TYPE KEY ---
        - 0 = incomplete condition switch jump
            - 0 = switchjump (only one label given)
            - 1 = switchjump (more than 2 labels given)
            - 2 = numautojump (no labels exist for numbers up through 10)
        - 1 = argument given where none needed
            - 0 = quietcreep
            - 1 = claimfold
        - 2 = modifier(s) given for function that doesn't use them
            - 0 = label
            - 1 = other
        - 3 = duplicate in modifiers
            - 0 = duplicate modifier(s), check extraInfo
            - 1 = duplicate modifier argument(s), check extraInfo
        - 4 = redundant modifier(s) (with different values)
            - 0 = ifsource and ifsourcenot
            - 1 = ifnum and ifnumnot
            - 2 = ifstring and ifstringnot
        - 5 = concerningly high pause time (>5000ms)
    */

    // --- CONSTRUCTORS ---

    /**
     * Constructor
     * @param lineIndex the index of the line with the potential issue
     * @param type the type of this potential issue
     * @param subtype the subtype of this potential issue
     * @param extraInfo any extra information to print in the scan report
     */
    public ScriptIssue(int lineIndex, int type, int subtype, String[] extraInfo) {
        super(lineIndex, type, subtype, extraInfo);
    }

    /**
     * Constructor
     * @param lineIndex the index of the line with the potential issue
     * @param type the type of this potential issue
     * @param subtype the subtype of this potential issue
     * @param extraInfo any extra information to print in the scan report
     */
    public ScriptIssue(int lineIndex, int type, int subtype, ArrayList<String> extraInfo) {
        super(lineIndex, type, subtype, extraInfo.toArray(new String[0]));
    }

    /**
     * Constructor
     * @param lineIndex the index of the line with the potential issue
     * @param type the type of this potential issue
     * @param subtype the subtype of this potential issue
     * @param extraInfo any extra information to print in the scan report
     */
    public ScriptIssue(int lineIndex, int type, int subtype, String extraInfo) {
        super(lineIndex, type, subtype, extraInfo);
    }

    /**
     * Constructor
     * @param lineIndex the index of the line with the potential issue
     * @param type the type of this potential issue
     * @param subtype the subtype of this potential issue
     */
    public ScriptIssue(int lineIndex, int type, int subtype) {
        super(lineIndex, type, subtype);
    }

    /**
     * Constructor
     * @param lineIndex the index of the line with the potential issue
     * @param type the type of this potential issue
     * @param extraInfo any extra information to print in the scan report
     */
    public ScriptIssue(int lineIndex, int type, String[] extraInfo) {
        super(lineIndex, type, extraInfo);
    }

    /**
     * Constructor
     * @param lineIndex the index of the line with the potential issue
     * @param type the type of this potential issue
     * @param subtype the subtype of this potential issue
     * @param extraInfo any extra information to print in the scan report
     */
    public ScriptIssue(int lineIndex, int type, ArrayList<String> extraInfo) {
        super(lineIndex, type, extraInfo.toArray(new String[0]));
    }

    /**
     * Constructor
     * @param lineIndex the index of the line with the potential issue
     * @param type the type of this potential issue
     * @param extraInfo any extra information to print in the scan report
     */
    public ScriptIssue(int lineIndex, int type, String extraInfo) {
        super(lineIndex, type, extraInfo);
    }

    /**
     * Constructor
     * @param lineIndex the index of the line with the potential issue
     * @param type the type of this potential issue
     */
    public ScriptIssue(int lineIndex, int type) {
        super(lineIndex, type);
    }

    // --- MISC ---

    /**
     * Returns a String representation of this ScriptIssue
     */
    @Override
    public String toString() {
        String s = super.toString();

        switch (this.type) {
            case 0:
                s += "Incomplete ";
                switch (this.subtype) {
                    case 0:
                        s += "switchjump (only true label given)";
                        break;
                        
                    case 1:
                        s += "switchjump (more than 2 labels given)";
                        break;
                        
                    case 2:
                        s += "numautojump (no labels exist for numbers 0-9)";
                        break;
                }
                break;
            
            case 1:
                s += "Unnecessary argument given for ";
                switch (this.subtype) {
                    case 0:
                        s += "quietcreep";
                        break;
                        
                    case 1:
                        s += "claimfold";
                        break;
                }
                break;
            
            case 2:
                s += "Unnecessary modifier(s) given for ";
                switch (this.subtype) {
                    case 0:
                        s += "label";
                        break;
                        
                    case 1:
                        s += "skipped line";
                        break;
                }
                break;
            
            case 3:
                s += "Duplicate modifier";
                switch (this.subtype) {
                    case 0:
                        s += "(s) (" + this.extraList() + ")";
                        break;
                        
                    case 1:
                        s += " argument(s) (" + this.extraList() + ")";
                        break;
                }
                break;
            
            case 4:
                s += "Redundant modifiers (";
                switch (this.subtype) {
                    case 0:
                        s += "ifnum and ifnumnot with different values)";
                        break;
                        
                    case 1:
                        s += "ifstring and ifstringnot with different values)";
                        break;
                }
                break;

            case 5:
                s += "Excessive pause time (" + this.extraInfo[0] + "ms)";
                break;
        }

        return s;
    }
    
}
