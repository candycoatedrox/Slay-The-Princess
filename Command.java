import java.util.Arrays;

public enum Command {
    HELP("help", "Display all available commands or information on a given command.", "", "help", "show", "toggle", "go", "walk", "enter", "leave", "turn", "slay", "take", "drop", "throw"),
    SHOW("show", "Display content warnings (general, by chapter, or for the current chapter).", "general", "generic", "all", "full", "game", "full game", "full-game", "by chapter", "by-chapter", "chapter by chapter", "chapter-by-chapter", "chapters", "all chapters", "current", "active", "chapter", "current chapter", "active chapter", "route", "current route", "active route"),
    TOGGLE("toggle", "Toggle a given setting.", "warnings", "content warnings", "cws", "trigger warnings", "tws", "now playing", "nowplaying", "np", "music", "soundtrack"),
    GO("go", "Move in a given direction.", "forward", "forwards", "f", "back", "backward", "backwards", "b", "inside", "in", "i", "outside", "out", "o", "down", "d", "up", "u"),
    DIRECTGO("", "Move in a given direction.", "forward", "forwards", "f", "back", "backward", "backwards", "b", "inside", "in", "i", "outside", "out", "o", "down", "d", "up", "u"),
    WALK("walk", "Move in a given direction.", "forward", "forwards", "f", "back", "backward", "backwards", "b", "inside", "in", "i", "outside", "out", "o", "down", "d", "up", "u"),
    ENTER("enter", "Enter a given location or the nearest appropriate location.", "", "cabin", "basement"),
    LEAVE("leave", "Leave the current location.", "", "cabin", "basement", "woods", "path"),
    PROCEED("proceed", "Move forward.", ""),
    TURN("turn", "Turn back.", "", "around", "back"),
    APPROACH("approach", "Approach the mirror.", "the mirror", "mirror"),
    SLAY("slay", "Slay the Princess or yourself.", "the princess", "princess", "self", "yourself", "you", "myself", "me", "ourself", "ourselves", "us"),
    TAKE("take", "Take the blade.", "the blade", "blade", "pristine blade"),
    DROP("drop", "Drop the blade.", "the blade", "blade", "pristine blade"),
    THROW("throw", "Throw the blade out the window.", "the blade", "blade", "pristine blade");

    private final String prefix;
    private final String description;
    private final String[] validArguments;

    // --- CONSTRUCTOR ---

    /**
     * Constructor
     * @param prefix the first word the player enters to trigger this Command
     * @param description a brief description of this Command, shown when using the HELP command
     * @param validArguments all arguments the player can enter after the prefix that will result in a valid outcome
     */
    private Command(String prefix, String description, String... validArguments) {
        this.prefix = prefix;
        this.description = description;
        this.validArguments = validArguments;
    }

    // --- ACCESSORS & CHECKS ---

    /**
     * Accessor for prefix
     * @return the prefix of this Command
     */
    public String getPrefix() {
        return this.prefix;
    }

    /**
     * Accessor for description
     * @return a brief description of this Command
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Returns a detailed description of this Command, including information on its argument(s) and different variations
     * @return a detailed description of this Command, including information on its argument(s) and different variations
     */
    public String help() {
        // Technically, I could just make this entire method one switch statement where each case returns the full help text in one line, but this is MUCH easier to read, understand, and edit.
        
        String displayPrefix;
        switch (this) {
            case GO:
            case DIRECTGO:
            case WALK:
                displayPrefix = "[GO / WALK]";
                break;

            default:
                displayPrefix = this.getPrefix().toUpperCase();
        }

        String s = displayPrefix + ": " + this.getDescription() + "\nSyntax: > ";
        switch (this) {
            case HELP:
                s += "HELP [command]\n\n";

                s += "- Arguments -\n";
                s += "  - [command]: Optional. Can be any valid command. See all valid commands with > HELP.\n\n";

                s += "- Variations -\n";
                s += "  - HELP: Displays a list of all available commands.\n";
                s += "  - HELP [command]: Displays information on a given command.\n";
                break;
                
            case SHOW:
                s += "SHOW [warnings] [set] [warnings]\n\n";

                s += "- Arguments -\n";
                s += "  - [warnings]: Both optional. The command functions the same, no matter if [warnings] is present or not. Can be any one of [WARNINGS / CONTENT WARNINGS / CWS / TRIGGER WARNINGS / TWS].\n";
                s += "  - [set]: Optional. The set of warnings you wish to view.\n\n";

                s += "- Variations -\n";
                s += "  - SHOW: Offers a choice between showing general content warnings, content warnings by chapter, or content warnings for the current chapter (if applicable).\n";
                s += "  - SHOW [warnings]: Same as > SHOW. Offers a choice between showing general content warnings, content warnings by chapter, or content warnings for the current chapter (if applicable).\n";
                s += "  - SHOW [GENERAL / GENERIC / ALL / FULL / GAME / FULL GAME / FULL-GAME]: Shows general content warnings.\n";
                s += "  - SHOW [BY CHAPTER / BY-CHAPTER / CHAPTER BY CHAPTER / CHAPTER-BY-CHAPTER / CHAPTERS / ALL CHAPTERS]: Shows content warnings by chapter.\n";
                s += "  - SHOW [CURRENT / ACTIVE / CHAPTER / CURRENT CHAPTER / ACTIVE CHAPTER / ROUTE / CURRENT ROUTE / ACTIVE ROUTE]: Shows content warnings for the current chapter, if applicable.\n";
                s += "  - SHOW [warnings] [set]: Same as > SHOW [set].\n";
                s += "  - SHOW [set] [warnings]: Same as > SHOW [set].\n";
                break;

            case TOGGLE:
                s += "TOGGLE [setting]\n\n";

                s += "- Arguments -\n";
                s += "  - [setting]: The setting you wish to toggle on or off.\n\n";

                s += "- Variations -\n";
                s += "  - TOGGLE [WARNINGS / CONTENT WARNINGS / CWS / TRIGGER WARNINGS / TWS]: Toggles dynamic content warnings on or off.\n";
                s += "  - TOGGLE [NOW PLAYING / NP / MUSIC / SOUNDTRACK]: Toggles soundtrack notifications on or off.\n";
                break;

            case GO:
            case DIRECTGO:
            case WALK:
                s += "[GO / WALK] [direction]\n";
                s += "\"[GO / WALK]\" is optional. > [direction] functions exactly the same.\n\n";

                s += "- Arguments -\n";
                s += "  - [direction]: The direction you wish to travel in.\n\n";

                s += "- Variations -\n";
                s += "  - [GO / WALK] [FORWARD / FORWARDS / F]: Press onwards.\n";
                s += "  - [GO / WALK] [BACK / BACKWARD / BACKWARDS / B]: Turn back.\n";
                s += "  - [GO / WALK] [INSIDE / IN / I]: Enter the nearest location, if possible.\n";
                s += "  - [GO / WALK] [OUTSIDE / OUT / O]: Leave your current location, if possible.\n";
                s += "  - [GO / WALK] [DOWN / D]: Descend.\n";
                s += "  - [GO / WALK] [UP / U]: Ascend.\n";
                break;

            case ENTER:
                s += "ENTER [location]\n\n";

                s += "- Arguments -\n";
                s += "  - [location]: Optional. The location you wish to enter.\n\n";

                s += "- Variations -\n";
                s += "  - ENTER: Enter the nearest appropriate location, if possible.\n";
                s += "  - ENTER CABIN: Enter the cabin, if possible. \n";
                s += "  - ENTER BASEMENT: Descend into the basement, if possible.\n";
                break;

            case LEAVE:
                s += "LEAVE [location]\n\n";

                s += "- Arguments -\n";
                s += "  - [location]: Optional. The location you wish to leave.\n\n";

                s += "- Variations -\n";
                s += "  - LEAVE: Leave the current location, if possible.\n";
                s += "  - LEAVE [WOODS / PATH]: Leave the woods, if possible.\n";
                s += "  - LEAVE CABIN: Leave the cabin, if possible. \n";
                s += "  - LEAVE BASEMENT: Ascend from the basement, if possible.\n";
                break;

            case PROCEED:
                s += "PROCEED\n\n";

                s += "- Arguments -\n";
                s += "None.\n";
                break;

            case TURN:
                s += "TURN [around]\n\n";

                s += "- Arguments -\n";
                s += "  - [around]: Optional. Does not affect the way the command functions.\n\n";

                s += "- Variations -\n";
                s += "  - TURN: Turn around and leave.\n";
                s += "  - TURN [AROUND / BACK]: Same as > TURN. Turn around and leave.\n";
                break;

            case APPROACH:
                s += "APPROACH [mirror]\n\n";

                s += "- Arguments -\n";
                s += "  - [mirror]: The mirror.\n\n";

                s += "- Variations -\n";
                s += "  - APPROACH [THE MIRROR / MIRROR]: Approach the mirror.\n";
                break;

            case SLAY:
                s += "SLAY [target]\n\n";

                s += "- Arguments -\n";
                s += "  - [target]: The person you wish to slay.\n\n";

                s += "- Variations -\n";
                s += "  - SLAY [THE PRINCESS / PRINCESS]: Slay the Princess. It's in the name.\n";
                s += "  - SLAY [SELF / YOURSELF / YOU / MYSELF / ME / OURSELF / OURSELVES / US]: Slay yourself.\n";
                break;

            case TAKE:
                s += "TAKE [blade]\n\n";

                s += "- Arguments -\n";
                s += "  - [blade]: The blade.\n\n";

                s += "- Variations -\n";
                s += "  - TAKE [PRISTINE BLADE / THE BLADE / BLADE]: Take the blade.\n";
                break;

            case DROP:
                s += "DROP [blade]\n\n";

                s += "- Arguments -\n";
                s += "  - [blade]: The blade.\n\n";

                s += "- Variations -\n";
                s += "  - DROP [PRISTINE BLADE / THE BLADE / BLADE]: Drop the blade,\n";
                break;

            case THROW:
                s += "THROW [blade]\n\n";

                s += "- Arguments -\n";
                s += "  - [blade]: The blade.\n\n";

                s += "- Variations -\n";
                s += "  - THROW [PRISTINE BLADE / THE BLADE / BLADE]: Throw the blade out the window.\n";
                break;

            default:
                throw new IllegalArgumentException("Invalid command");
        }

        return s;
    }

    /**
     * Checks if a given argument is a valid argument of this Command
     * @param argument the argument to check
     * @return true if argument is a valid argument of this Command; false otherwise
     */
    public boolean argumentIsValid(String argument) {
        if (argument == null) {
            return this.argumentIsValid();
        } else {
            return Arrays.asList(this.validArguments).contains(argument.toLowerCase());
        }
    }

    /**
     * Checks if an empty argument is a valid argument of this Command
     * @return true if an empty argument is a valid argument of this Command; false otherwise
     */
    public boolean argumentIsValid() {
        return this.validArguments.length == 0 || Arrays.asList(this.validArguments).contains("");
    }

    /**
     * Returns the Command triggered by a given prefix
     * @param prefix the prefix to check against each Command
     * @return the Command that matches the given prefix
     */
    public static Command getCommand(String prefix) {
        String command = prefix.toLowerCase();

        if (GO.argumentIsValid(command)) return DIRECTGO;
        switch (command) {
            case "help": return HELP;
            case "show": return SHOW;
            case "toggle": return TOGGLE;
            case "go": return GO;
            case "walk": return WALK;
            case "proceed": return PROCEED;
            case "enter": return ENTER;
            case "leave": return LEAVE;
            case "turn": return TURN;
            case "approach": return APPROACH;
            case "slay": return SLAY;
            case "take": return TAKE;
            case "drop": return DROP;
            case "throw": return THROW;

            default: return null;
        }
    }
}
