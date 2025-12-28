public abstract class ScriptNote {
    
    protected final int lineIndex;
    protected final int type;
    protected final int subtype;
    protected final String[] extraInfo;

    // --- CONSTRUCTORS ---

    /**
     * Constructor
     * @param lineIndex the index of the line with the note
     * @param type the type of this note (dependent on whether it is an error or potential issue)
     * @param subtype the subtype of this note (dependent on whether it is an error or potential issue)
     * @param extraInfo any extra information to print in the scan report
     */
    public ScriptNote(int lineIndex, int type, int subtype, String[] extraInfo) {
        this.lineIndex = lineIndex;
        this.type = type;
        this.subtype = subtype;
        this.extraInfo = extraInfo;
    }

    /**
     * Constructor
     * @param lineIndex the index of the line with the note
     * @param type the type of this note (dependent on whether it is an error or potential issue)
     * @param subtype the subtype of this note (dependent on whether it is an error or potential issue)
     * @param extraInfo any extra information to print in the scan report
     */
    public ScriptNote(int lineIndex, int type, int subtype, String extraInfo) {
        this.lineIndex = lineIndex;
        this.type = type;
        this.subtype = subtype;

        this.extraInfo = new String[1];
        this.extraInfo[0] = extraInfo;
    }

    /**
     * Constructor
     * @param lineIndex the index of the line with the note
     * @param type the type of this note (dependent on whether it is an error or potential issue)
     * @param subtype the subtype of this note (dependent on whether it is an error or potential issue)
     */
    public ScriptNote(int lineIndex, int type, int subtype) {
        this.lineIndex = lineIndex;
        this.type = type;
        this.subtype = subtype;
        this.extraInfo = new String[0];
    }

    /**
     * Constructor without subtype
     * @param lineIndex the index of the line with the note
     * @param type the type of this note (dependent on whether it is an error or potential issue)
     * @param extraInfo any extra information to print in the scan report
     */
    public ScriptNote(int lineIndex, int type, String[] extraInfo) {
        this(lineIndex, type, 0, extraInfo);
    }

    /**
     * Constructor without subtype
     * @param lineIndex the index of the line with the note
     * @param type the type of this note (dependent on whether it is an error or potential issue)
     * @param extraInfo any extra information to print in the scan report
     */
    public ScriptNote(int lineIndex, int type, String extraInfo) {
        this(lineIndex, type, 0, extraInfo);
    }

    /**
     * Constructor without subtype
     * @param lineIndex the index of the line with the note
     * @param type the type of this note (dependent on whether it is an error or potential issue)
     */
    public ScriptNote(int lineIndex, int type) {
        this(lineIndex, type, 0);
    }

    // --- MISC ---

    /**
     * Returns all included extra information formatted as a list
     * @return all included extra information, formatted as a list
     */
    protected String extraList(boolean skipFirst) {
        int startIndex = (skipFirst) ? 1 : 0;
        if (extraInfo.length <= startIndex) return "";

        String s = this.extraInfo[startIndex];
        for (int i = startIndex; i < extraInfo.length; i++) {
            s += ", ";
            s += this.extraInfo[i];
        }
        return s;
    }

    /**
     * Returns all included extra information formatted as a list
     * @return all included extra information, formatted as a list
     */
    protected String extraList() {
        return this.extraList(false);
    }

    /**
     * Returns a String representation of this ScriptNote
     */
    @Override
    public String toString() {
        return "  - LINE " + (lineIndex + 1) + ": ";
    }

}
