import java.util.Arrays;

public enum Command {
    HELP("help", "Display all available commands or information on a given command.", "", "help", "show", "toggle", "go", "walk", "enter", "leave", "turn", "slay", "take", "drop", "throw"),
    SHOW("show", "Display content warnings.", "general", "generic", "all", "full", "game", "full game", "full-game", "by chapter", "by-chapter", "chapter by chapter", "chapter-by-chapter", "chapters", "all chapters", "current", "active", "chapter", "current chapter", "active chapter", "route", "current route", "active route"),
    TOGGLE("toggle", "Toggle a given setting.", "warnings", "content warnings", "cws", "trigger warnings", "tws", "now playing", "nowplaying", "np", "music", "soundtrack"),
    GO("go", "Move in a given direction.", "forward", "forwards", "f", "back", "backward", "backwards", "b", "inside", "in", "i", "outside", "out", "o", "down", "d", "up", "u"),
    DIRECTGO("", "Move in a given direction.", "forward", "forwards", "f", "back", "backward", "backwards", "b", "inside", "in", "i", "outside", "out", "o", "down", "d", "up", "u"),
    WALK("walk", "Move in a given direction.", "forward", "forwards", "f", "back", "backward", "backwards", "b", "inside", "in", "i", "outside", "out", "o", "down", "d", "up", "u"),
    ENTER("enter", "Enter a given location or the nearest appropriate location.", "", "cabin", "basement"),
    LEAVE("leave", "Leave a given location or the current location.", "", "cabin", "basement", "woods", "path"),
    TURN("turn", "Leave the current location.", "", "around", "back"),
    APPROACH("approach", "Approach the mirror.", "the mirror", "mirror"),
    SLAY("slay", "Slay the Princess or yourself.", "the princess", "princess", "self", "yourself", "you", "myself", "me", "ourself", "ourselves", "us"),
    TAKE("take", "Take the blade.", "the blade", "blade", "pristine blade"),
    DROP("drop", "Drop the blade.", "the blade", "blade", "pristine blade"),
    THROW("throw", "Throw the blade out the window.", "the blade", "blade", "pristine blade");

    private final String prefix;
    private final String description;
    private final String[] validArguments;

    private static final String[] DIRECTIONARGS = GO.getValidArguments();

    // --- CONSTRUCTOR ---

    private Command(String prefix, String description, String... validArguments) {
        this.prefix = prefix;
        this.description = description;
        this.validArguments = validArguments;
    }

    // --- ACCESSORS & CHECKS ---

    public String getPrefix() {
        return this.prefix;
    }

    public String getDescription() {
        return this.description;
    }

    public String[] getValidArguments() {
        return this.validArguments;
    }

    public int nValidArguments() {
        return this.validArguments.length;
    }

    public boolean argumentIsValid(String argument) {
        if (argument == null) {
            return this.argumentIsValid();
        } else {
            return Arrays.asList(this.validArguments).contains(argument.toLowerCase());
        }
    }

    public boolean argumentIsValid() {
        return this.validArguments.length == 0 || Arrays.asList(this.validArguments).contains("");
    }

    public static Command getCommand(String prefix) {
        String command = prefix.toLowerCase();

        if (GO.argumentIsValid(command)) {
            return DIRECTGO;
        }

        switch (command) {
            case "help": return HELP;
            case "show": return SHOW;
            case "toggle": return TOGGLE;
            case "go": return GO;
            case "walk": return WALK;
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
