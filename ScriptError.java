import java.util.ArrayList;

public class ScriptError extends ScriptNote {
    
    /* --- TYPE KEY ---
        - 0 = invalid line
        - 1 = invalid linebreak (non-int argument)
        - 2 = invalid jumpto
            - 0 = line index too large
            - 1 = given label does not exist
        - 3 = invalid firstswitch / bladeswitch / sourceswitch
            - 0 = firstswitch (no prefix given)
            - 1 = firstswitch (2+ arguments given)
            - 2 = firstswitch (one or both labels do not exist, check extra)
            - 3 = bladeswitch (no prefix given)
            - 4 = bladeswitch (3+ arguments given)
            - 5 = bladeswitch (one or both labels do not exist, check extra)
            - 6 = sourceswitch (no suffix given)
            - 7 = sourceswitch (2+ arguments given)
            - 8 = sourceswitch (no labels with suffix exist)
        - 4 = invalid condition switch jump
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
        - 5 = invalid nowplaying (no argument given)
        - 6 = invalid modifier(s)
            - 0 = multiple modifier dividers (///)
            - 1 = modifier divider but no modifiers
            - 2 = completely invalid modifier
            - 3 = invalid syntax for modifier
            - 4 = checkvoice used without argument for truth / princess
            - 5 = one or more invalid arguments for checkvoice or checknovoice, check extraInfo
            - 6 = firstvessel, hasblade, check, or negative counterparts attempted with argument, check extraInfo
            - 7 = more than 1 argument given for ifsource, ifnum, ifstring, or negative counterparts, check extraInfo
            - 8 = invalid argument for ifnum or ifnumnot (non-integer), check extraInfo
            - 9 = no argument given for ifsource, ifstring, or negative counterparts, check extraInfo
            - 10 = interrupt used for non-dialogue line
            - 11 = interrupt attempted with argument
        - 7 = conflicting modifiers
            - 0 = firstvessel & notfirstvessel
            - 1 = firstvessel or notfirstvessel used for firstswitch, check extraInfo
            - 2 = hasblade & noblade
            - 3 = hasblade or noblade used for bladeswitch, check extraInfo
            - 4 = ifsource & ifsourcenot with same value
            - 5 = ifsource or ifsourcenot used for sourceswitch, check extraInfo
            - 6 = check & checkfalse
            - 7 = check or checkfalse used for switchjump, check extraInfo
            - 8 = ifnum & ifnumnot with same value
            - 9 = ifnum or ifnumnot used for numswitchjump, check extraInfoextraInfo
            - 10 = ifstring & ifstringnot with same value
            - 11 = ifstring or ifstringnot used for stringswitchjump, check 
            - 12 = multiple ifsource, ifnum, ifstring, or negative counterparts checks (different targets)
            - 13 = same argument used for both checkvoice and checknovoice, check extraInfo
            - 14 = checknovoice used with id for speaker
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
                s += "Invalid linebreak (non-integer argument in place of number of lines)";
                break;
            
            case 2:
                s += "Invalid jumpto (";
                switch (this.subtype) {
                    case 0:
                        s += "given index is larger than script's size)";
                        break;
                        
                    case 1:
                        s += "given label " + this.extraInfo[0] + " does not exist in script)";
                        break;
                }
                break;
            
            case 3:
                s += "Invalid ";
                switch (this.subtype) {
                    case 0:
                        s += "firstswitch (no prefix given)";
                        break;

                    case 1:
                        s += "firstswitch (2+ arguments given)";
                        break;
                        
                    case 2:
                        s += "firstswitch (label(s) " + this.extraList() + " do not exist in script)";
                        break;
                        
                    case 3:
                        s += "bladeswitch (no prefix given)";
                        break;

                    case 4:
                        s += "bladeswitch (3+ arguments given)";
                        break;
                        
                    case 5:
                        s += "bladeswitch (label(s) " + this.extraList() + " do not exist in script)";
                        break;
                        
                    case 6:
                        s += "sourceswitch (no suffix given)";
                        break;

                    case 7:
                        s += "sourceswitch (2+ arguments given)";
                        break;
                        
                    case 8:
                        s += "sourceswitch (no labels ending in suffix " + this.extraInfo[0] + " exist in script)";
                        break;
                }
                break;
            
            case 4:
                s += "Invalid ";
                switch (this.subtype) {
                    case 0:
                        s += "switchjump (no labels given)";
                        break;
                        
                    case 1:
                        s += "switchjump (label(s) " + this.extraList() + " do not exist in script)";
                        break;
                        
                    case 2:
                        s += "numswitchjump (no labels given)";
                        break;
                        
                    case 3:
                        s += "numswitchjump (label(s) " + this.extraList() + " do not exist in script)";
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
                        s += "stringswitchjump (label(s) " + this.extraList() + " do not exist in script)";
                        break;

                    case 8:
                        s += "numautojump (no prefix given)";
                        break;

                    case 9:
                        s += "stringautojump (no suffix given)";
                        break;
                }
                break;
            
            case 5:
                s += "Invalid nowplaying (no argument given)";
                break;
            
            case 6:
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
                        s += this.extraInfo[0] + " with invalid syntax)";
                        break;
                        
                    case 4:
                        s += "checkvoice used without argument for non-Voice speaker)";
                        break;
                        
                    case 5:
                        s += "invalid argument(s) " + this.extraList() + " for checkvoice or checknovoice)";
                        break;
                        
                    case 6:
                        s += this.extraInfo[0] + " used with argument)";
                        break;
                        
                    case 7:
                        s += "more than 1 argument given for " + this.extraInfo[0] + ")";
                        break;
                        
                    case 8:
                        s += "non-integer argument for " + this.extraInfo[0] + ")";
                        break;
                        
                    case 9:
                        s += "no argument given for " + this.extraInfo[0] + ")";
                        break;
                        
                    case 10:
                        s += "interrupt used for non-dialogue line)";
                        break;
                        
                    case 11:
                        s += "interrupt used with argument)";
                        break;
                }
                break;
            
            case 7:
                s += "IMPOSSIBLE LINE -- Conflicting modifiers (";
                switch (this.subtype) {
                    case 0:
                        s += "firstvessel & notfirstvessel)";
                        break;

                    case 1:
                        s += this.extraInfo[0] + " used for firstswitch)";
                        break;

                    case 2:
                        s += "hasblade & noblade)";
                        break;

                    case 3:
                        s += this.extraInfo[0] + " used for bladeswitch)";
                        break;
                        
                    case 4:
                        s += "ifsource & ifsourcenot used with same value(s) " + this.extraList() + ")";
                        break;
                        
                    case 5:
                        s += this.extraInfo[0] + " used for sourceswitch)";
                        break;
                        
                    case 6:
                        s += "check & checkfalse)";
                        break;
                        
                    case 7:
                        s += this.extraInfo[0] + " used for switchjump)";
                        break;
                        
                    case 8:
                        s += "ifnum & ifnumnot used with same value(s) " + this.extraList() + ")";
                        break;

                    case 9:
                        s += this.extraInfo[0] + " used for numswitchjump)";
                        break;
                        
                    case 10:
                        s += "ifstring & ifstringnot used with same value(s) " + this.extraList() + ")";
                        break;
                        
                    case 11:
                        s += this.extraInfo[0] + " used for stringswitchjump)";
                        break;
                        
                    case 12:
                        s += "multiple " + this.extraInfo[0] + " checks for different values)";
                        break;
                        
                    case 13:
                        s += "same argument(s) " + this.extraList() + " used for both checkvoice and checknovoice)";
                        break;
                        
                    case 14:
                        s += "checknovoice used with same id as speaker " + this.extraInfo[0] + ")";
                        break;
                }
                break;

            default: s += "Invalid line (invalid command)";
        }
        
        return s;
    }

}
