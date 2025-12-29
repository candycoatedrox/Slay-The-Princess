import java.util.ArrayList;

public class ScriptError extends ScriptNote {
    
    /* --- TYPE KEY ---
        - 0 = invalid line
        - 1 = invalid linebreak / pause
            - 0 = one or more non-integer argument(s), check extraInfo
            - 1 = one or more negative argument(s), check extraInfo
            - 2 = no argument given for pause
            - 3 = 3+ arguments given for pause
        - 2 = invalid break
            - 0 = attempted with argument
            - 1 = no (valid) modifiers
        - 3 = invalid jumpto
            - 0 = line index too large
            - 1 = given label does not exist
        - 4 = invalid autoswitch
            - 0 = no prefix given
            - 1 = 2+ arguments given (firstswitch, moodswitch, sourceswitch)
            - 2 = 3+ arguments given (bladeswitch)
            - 3 = one or both labels do not exist, check extraInfo
            - 4 = sourceswitch (no suffix given)
            - 5 = sourceswitch (no labels with suffix exist)
        - 5 = invalid condition switch jump
            - 0 = switchjump (no labels given)
            - 1 = switchjump (one or both labels do not exist, check extra)
            - 2 = numswitchjump (no labels given)
            - 3 = numswitchjump (one or more labels do not exist, check extra)
            - 4 = stringswitchjump (no arguments given)
            - 5 = stringswitchjump (one argument given)
            - 6 = stringswitchjump (odd number of arguments)
            - 7 = stringswitchjump (one or more labels do not exist, check extra)
            - 8 = numautojump (no prefix given)
            - 9 = stringautojump (no suffix given)
        - 6 = invalid nowplaying (no argument given)
        - 7 = invalid modifier(s)
            - 0 = multiple modifier dividers (///)
            - 1 = modifier divider but no modifiers
            - 2 = completely invalid modifier
            - 3 = invalid syntax for modifier
            - 4 = checkvoice used without argument for truth / princess
            - 5 = one or more invalid arguments for checkvoice or checknovoice, check extraInfo
            - 6 = firstvessel, hasblade, threwblade, sharedloop, sharedinsist, mirrorask, mirrortouch, mirror2, harsh, knowledge, check, or negative counterparts attempted with argument, check extraInfo
            - 7 = more than 1 argument given for ifsource, ifnum, ifstring, or negative counterparts, check extraInfo
            - 8 = invalid argument for ifnum or ifnumnot (non-integer), check extraInfo
            - 9 = no argument given for ifsource, ifstring, or negative counterparts, check extraInfo
            - 10 = interrupt used for non-dialogue line
            - 11 = interrupt attempted with argument
        - 8 = conflicting modifiers
            - 0 = firstvessel, hasblade, threwblade, sharedloop, sharedinsist, mirrorask, mirrortouch, mirror2, harsh, knowledge, or check used with negative counterpart, check extraInfo
            - 1 = firstvessel, hasblade, ifsource, harsh, or negative counterparts used for corresponding autoswitch, check extraInfo
            - 2 = check, ifnum, ifstring, or negative counterparts used for corresponding switchjump, check extraInfo
            - 3 = ifsource & ifsourcenot with same value
            - 4 = ifnum & ifnumnot with same value
            - 5 = ifstring & ifstringnot with same value
            - 6 = multiple ifsource, ifnum, ifstring, or negative counterparts checks (different targets)
            - 7 = same argument used for both checkvoice and checknovoice, check extraInfo
            - 8 = checknovoice used with id for speaker
    */

    // --- CONSTRUCTORS ---

    /**
     * Constructor
     * @param lineIndex the index of the line with the error
     * @param type the type of this error
     * @param subtype the subtype of this error
     * @param extraInfo any extra information to print in the scan report
     */
    public ScriptError(int lineIndex, int type, int subtype, String[] extraInfo) {
        super(lineIndex, type, subtype, extraInfo);
    }

    /**
     * Constructor
     * @param lineIndex the index of the line with the potential issue
     * @param type the type of this potential issue
     * @param subtype the subtype of this potential issue
     * @param extraInfo any extra information to print in the scan report
     */
    public ScriptError(int lineIndex, int type, int subtype, ArrayList<String> extraInfo) {
        super(lineIndex, type, subtype, extraInfo.toArray(new String[0]));
    }

    /**
     * Constructor
     * @param lineIndex the index of the line with the error
     * @param type the type of this error
     * @param subtype the subtype of this error
     * @param extraInfo any extra information to print in the scan report
     */
    public ScriptError(int lineIndex, int type, int subtype, String extraInfo) {
        super(lineIndex, type, subtype, extraInfo);
    }

    /**
     * Constructor
     * @param lineIndex the index of the line with the error
     * @param type the type of this error
     * @param subtype the subtype of this error
     */
    public ScriptError(int lineIndex, int type, int subtype) {
        super(lineIndex, type, subtype);
    }

    /**
     * Constructor
     * @param lineIndex the index of the line with the error
     * @param type the type of this error
     * @param extraInfo any extra information to print in the scan report
     */
    public ScriptError(int lineIndex, int type, String[] extraInfo) {
        super(lineIndex, type, extraInfo);
    }

    /**
     * Constructor
     * @param lineIndex the index of the line with the potential issue
     * @param type the type of this potential issue
     * @param extraInfo any extra information to print in the scan report
     */
    public ScriptError(int lineIndex, int type, ArrayList<String> extraInfo) {
        super(lineIndex, type, extraInfo.toArray(new String[0]));
    }

    /**
     * Constructor
     * @param lineIndex the index of the line with the error
     * @param type the type of this error
     * @param extraInfo any extra information to print in the scan report
     */
    public ScriptError(int lineIndex, int type, String extraInfo) {
        super(lineIndex, type, extraInfo);
    }

    /**
     * Constructor
     * @param lineIndex the index of the line with the error
     * @param type the type of this error
     */
    public ScriptError(int lineIndex, int type) {
        super(lineIndex, type);
    }

    // --- MISC ---

    /**
     * Returns a String representation of this ScriptError
     */
    @Override
    public String toString() {
        String s = super.toString();

        switch (this.type) {
            case 1:
                s += "Invalid ";
                switch (this.subtype) {
                    case 0:
                        s += this.extraInfo[0];
                        s += " (non-integer argument(s))";
                        break;

                    case 1:
                        s += this.extraInfo[0];
                        s += " (negative argument(s))";
                        break;
                        
                    case 2:
                        s += "pause (no arguments given)";
                        break;
                        
                    case 3:
                        s += "pause (3 or more arguments given)";
                        break;
                }
                break;

            case 2:
                s += "Invalid break (";
                switch (this.subtype) {
                    case 0:
                        s += "attempted with argument)";
                        break;
                        
                    case 1:
                        s += "used without valid modifiers)";
                        break;
                }
                break;
            
            case 3:
                s += "Invalid jumpto (";
                switch (this.subtype) {
                    case 0:
                        s += "given index is larger than script's size)";
                        break;
                        
                    case 1:
                        s += "given label ";
                        s += this.extraInfo[0];
                        s += " does not exist in script)";
                        break;
                }
                break;
            
            case 4:
                s += "Invalid ";
                switch (this.subtype) {
                    case 0:
                        s += this.extraInfo[0];
                        s += " (no prefix given)";
                        break;

                    case 1:
                        s += this.extraInfo[0];
                        s += " (2+ arguments given)";
                        break;

                    case 2:
                        s += "bladeswitch (3+ arguments given)";
                        break;
                        
                    case 3:
                        s += this.extraInfo[0];
                        s += " (label(s) ";
                        s += this.extraList(true);
                        s += " do not exist in script)";
                        break;
                        
                    case 4:
                        s += "sourceswitch (no suffix given)";
                        break;

                    case 5:
                        s += "sourceswitch (no labels ending in suffix ";
                        s += this.extraInfo[0];
                        s += " exist in script)";
                        break;
                }
                break;
            
            case 5:
                s += "Invalid ";
                switch (this.subtype) {
                    case 0:
                        s += "switchjump (no labels given)";
                        break;
                        
                    case 1:
                        s += "switchjump (label(s) ";
                        s += this.extraList();
                        s += " do not exist in script)";
                        break;
                        
                    case 2:
                        s += "numswitchjump (no labels given)";
                        break;
                        
                    case 3:
                        s += "numswitchjump (label(s) ";
                        s += this.extraList();
                        s += " do not exist in script)";
                        break;
                        
                    case 4:
                        s += "stringswitchjump (no arguments given)";
                        break;
                        
                    case 5:
                        s += "stringswitchjump (only one argument given)";
                        break;
                        
                    case 6:
                        s += "stringswitchjump (odd number of arguments)";
                        break;
                        
                    case 7:
                        s += "stringswitchjump (label(s) ";
                        s += this.extraList();
                        s += " do not exist in script)";
                        break;

                    case 8:
                        s += "numautojump (no prefix given)";
                        break;

                    case 9:
                        s += "stringautojump (no suffix given)";
                        break;
                }
                break;
            
            case 6:
                s += "Invalid nowplaying (no argument given)";
                break;
            
            case 7:
                s += "Invalid modifier (";
                switch (this.subtype) {
                    case 0:
                        s += "multiple modifier dividers)";
                        break;

                    case 1:
                        s += "modifier divider present without modifiers)";
                        break;

                    case 2:
                        s += "completely invalid modifier)";
                        break;

                    case 3:
                        s += this.extraInfo[0];
                        s += " with invalid syntax)";
                        break;
                        
                    case 4:
                        s += "checkvoice used without argument for non-Voice speaker)";
                        break;
                        
                    case 5:
                        s += "invalid argument(s) ";
                        s += this.extraList();
                        s += " for checkvoice or checknovoice)";
                        break;
                        
                    case 6:
                        s += this.extraInfo[0];
                        s += " used with argument)";
                        break;
                        
                    case 7:
                        s += "more than 1 argument given for ";
                        s += this.extraInfo[0];
                        s += ")";
                        break;
                        
                    case 8:
                        s += "non-integer argument for ";
                        s += this.extraInfo[0];
                        s += ")";
                        break;
                        
                    case 9:
                        s += "no argument given for ";
                        s += this.extraInfo[0];
                        s += ")";
                        break;
                        
                    case 10:
                        s += "interrupt used for non-dialogue line)";
                        break;
                        
                    case 11:
                        s += "interrupt used with argument)";
                        break;
                }
                break;
            
            case 8:
                s += "IMPOSSIBLE LINE -- Conflicting modifiers (";
                switch (this.subtype) {
                    case 0: // used with negative counterpart
                        s += this.extraInfo[0];
                        s += ")";
                        break;

                    case 1: // attribute switches
                        s += this.extraInfo[0];
                        s += " used for ";
                        s += this.extraInfo[1];
                        s += ")";
                        break;

                    case 2: // condition switchjumps
                        s += this.extraInfo[0];
                        s += " used for ";
                        s += this.extraInfo[1];
                        s += ")";
                        break;
                        
                    case 3:
                        s += "ifsource & ifsourcenot used with same value(s) ";
                        s += this.extraList();
                        s += ")";
                        break;
                        
                    case 4:
                        s += "ifnum & ifnumnot used with same value(s) ";
                        s += this.extraList();
                        s += ")";
                        break;
                        
                    case 5:
                        s += "ifstring & ifstringnot used with same value(s) ";
                        s += this.extraList();
                        s += ")";
                        break;
                        
                    case 6:
                        s += "multiple ";
                        s += this.extraInfo[0];
                        s += " checks for different values)";
                        break;
                        
                    case 7:
                        s += "same argument(s) ";
                        s += this.extraList();
                        s += " used for both checkvoice and checknovoice)";
                        break;
                        
                    case 8:
                        s += "checknovoice used with same id as speaker ";
                        s += this.extraInfo[0];
                        s += ")";
                        break;
                }
                break;

            default: s += "Invalid line (invalid command)";
        }
        
        return s;
    }

}
