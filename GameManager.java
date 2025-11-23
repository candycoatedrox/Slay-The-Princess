import java.util.ArrayList;
import java.util.HashMap;

public class GameManager {
    
    private final IOHandler parser;
    private Cycle currentCycle;

    private boolean autoContentWarnings = true;
    private boolean showNowPlaying = true;

    private String nowPlaying;

    private int vesselsAborted = 0;
    private Chapter firstPrincess;
    private ArrayList<Vessel> claimedVessels;
    private ArrayList<ChapterEnding> endingsFound;
    private HashMap<Chapter, Boolean> visitedChapters;
    private HashMap<Voice, Boolean> voicesMet;
    private ArrayList<String> playlist;

    private int mirrorCruelCount = 0;

    private final OptionsMenu warningsMenu;

    // --- CONSTRUCTOR ---

    public GameManager() {
        this.parser = new IOHandler(this);
        this.claimedVessels = new ArrayList<>();

        this.playlist = new ArrayList<>();
        this.playlist.add("The Princess");
        
        this.visitedChapters = new HashMap<>();
        for (Chapter c : Chapter.values()) {
            if (c != Chapter.CH1 && c != Chapter.SPACESBETWEEN && c != Chapter.ENDOFEVERYTHING) {
                this.visitedChapters.put(c, false);
            }
        }

        this.voicesMet = new HashMap<>();
        for (Voice v : Voice.values()) {
            if (v != Voice.NARRATOR && v != Voice.PRINCESS && v != Voice.HERO) {
                this.voicesMet.put(v, false);
            }
        }

        this.warningsMenu = this.createWarningsMenu();
    }

    private OptionsMenu createWarningsMenu() {
        OptionsMenu menu = new OptionsMenu(true);
        menu.add(new Option(this, "general", "[Show general content warnings.]", 0));
        menu.add(new Option(this, "by chapter", "[Show content warnings by chapter.]", 0));
        menu.add(new Option(this, "current", "[Show content warnings for the current chapter.]", 0));
        menu.add(new Option(this, "cancel", "[Return to game.]", 0));

        return menu;
    }

    // --- ACCESSORS & MANIPULATORS ---

    public Cycle getCurrentCycle() {
        return this.currentCycle;
    }

    public boolean autoContentWarnings() {
        return this.autoContentWarnings;
    }

    public boolean showNowPlaying() {
        return this.showNowPlaying;
    }

    public String getNowPlaying() {
        return this.nowPlaying;
    }

    public void setNowPlaying(String song) {
        this.nowPlaying = song;

        if (this.showNowPlaying) {
            parser.printDialogueLine("------- Now Playing: " + this.nowPlaying + " -------", true);
        }
    }

    public boolean hasVisited(Chapter c) {
        return this.visitedChapters.get(c);
    }

    public void updateVisitedChapters(ArrayList<Chapter> route) {
        for (int i = 1; i < route.size(); i++) {
            this.visitedChapters.put(route.get(i), true);
        }
    }

    public boolean hasMet(Voice v) {
        return this.voicesMet.get(v);
    }

    public void updateVoicesMet(ArrayList<Voice> voices) {
        for (Voice v : voices) {
            this.voicesMet.put(v, true);
        }
    }

    public void setFirstPrincess(Chapter c) {
        this.firstPrincess = c;
    }

    public Vessel getClaimedVessel(int n) {
        return this.claimedVessels.get(n);
    }

    public int nClaimedVessels() {
        return this.claimedVessels.size();
    }

    public int nVesselsAborted() {
        return this.vesselsAborted;
    }

    public void addToPlaylist(String song) {
        if (!this.playlist.contains(song)) {
            this.playlist.add(song);
        }
    }

    public void addCruelCount() {
        this.mirrorCruelCount += 1;
    }
    
    public OptionsMenu warningsMenu() {
        return this.warningsMenu;
    }

    // --- PLAYTHROUGH MANAGEMENT ---

    public void runGame() {
        ChapterEnding ending;

        this.intro();
        
        while (this.nClaimedVessels() < 5 && this.vesselsAborted < 6) {
            this.currentCycle = new Cycle(this, this.parser);
            ending = this.currentCycle.runCycle();

            if (ending == null) {
                this.abortVessel();
            } else {
                this.endingsFound.add(ending);
                this.claimedVessels.add(ending.getVessel());
            }
        }

        this.currentCycle = null;

        if (this.nClaimedVessels() == 5) {
            EndOfEverything end = new EndOfEverything(this, this.claimedVessels, this.parser);
            ending = end.runCycle();
        } else {
            ending = ChapterEnding.OBLIVION;
        }

        this.endGame(ending);
    }

    // DEBUG / PLAYTEST ONLY -- SKIP INTRO
    private void runGame(boolean debug) {
        ChapterEnding ending;
        
        while (this.nClaimedVessels() < 5 && this.vesselsAborted < 6) {
            this.currentCycle = new Cycle(this, this.parser);
            ending = this.currentCycle.runCycle();

            if (ending == null) {
                this.abortVessel();
            } else {
                this.endingsFound.add(ending);
                this.claimedVessels.add(ending.getVessel());
            }
        }

        this.currentCycle = null;

        if (this.nClaimedVessels() == 5) {
            EndOfEverything end = new EndOfEverything(this, this.claimedVessels, this.parser);
            ending = end.runCycle();
        } else {
            ending = ChapterEnding.OBLIVION;
        }

        this.endGame(ending);
    }

    // --- SCENES ---

    private void intro() {
        System.out.println("-----------------------------------");
        System.out.println("         SLAY THE PRINCESS");
        System.out.println("-----------------------------------");

        System.out.println();
        System.out.println("CONTENT WARNING:");
        IOHandler.wrapPrintln("This is a horror game, and it is not intended for all audiences.");
        System.out.println();
        if (this.parser.promptYesNo("Would you like to view the list of content warnings now?", false)) {
            this.showGeneralWarnings();
            System.out.print("\n");
        }

        System.out.println();
        IOHandler.wrapPrintln("You can view content warnings at any time with > SHOW WARNINGS.");

        System.out.println();
        IOHandler.wrapPrintln("By default, some choices will ask you to confirm whether you are all right with potential content warnings beyond that point.");
        IOHandler.wrapPrintln("Would you like to turn choice confirmations off?");
        if (this.parser.promptYesNo("You can change this at any time with > TOGGLE WARNINGS.", false)) {
            this.toggleAutoWarnings();
        }

        System.out.println();
        IOHandler.wrapPrintln("By default, the game will display the song currently playing from the official Slay the Princess soundtrack whenever it changes.");
        IOHandler.wrapPrintln("The soundtrack can be found on Spotify at https://spotify.link/PdG0uXZecEb.");
        IOHandler.wrapPrintln("Would you like to turn soundtrack notifications off?");
        if (this.parser.promptYesNo("You can change this at any time with > TOGGLE NOW PLAYING.", false)) {
            this.toggleNowPlaying();
        }
        
        System.out.println();
        IOHandler.wrapPrint("You can view a list of available commands at any times with > HELP.");
        IOHandler.wrapPrint("Press enter to progress dialogue.");
        this.parser.waitForInput();

        System.out.println();
        System.out.println();
        System.out.println();
        parser.printDivider();
        try {
            parser.printDialogueLine("Whatever horrors you may find in these dark places, have heart and see them through.", true);
            Thread.sleep(1000);
            parser.printDialogueLine("There are no premature endings. There are no wrong decisions.", true);
            Thread.sleep(1000);
            parser.printDialogueLine("There are only fresh perspectives and new beginnings.", true);
            Thread.sleep(1000);
            parser.printDialogueLine("This is a love story.");

            System.out.println();
            System.out.println();
            System.out.println();
        } catch (InterruptedException e) {
            throw new RuntimeException("Thread interrupted");
        }
    }

    public boolean abortVessel() {
        this.vesselsAborted += 1;
        // PLACEHOLDER
        return true;
    }

    private void endGame(ChapterEnding ending) {
        // credits, show playlist, etc
        this.showCredits();
        this.showPlaylist();

        this.parser.closeInput();
    }

    private void showCredits() {

    }

    private void showPlaylist() {

    }

    // --- UTILITY ---

    public boolean confirmContentWarnings(Chapter c, String extraWarnings) {
        if (!this.autoContentWarnings) {
            return true;
        }

        parser.printDialogueLine("[If you make this choice, you will encounter: " + extraWarnings + ".]", true);
        parser.printDialogueLine("[You might also encounter: " + c.getContentWarnings() + ".]", true);

        boolean confirm = this.parser.promptYesNo("[Are you sure you wish to proceed?]");
        parser.printDialogueLine("[You can turn choice confirmations off at any time with TOGGLE WARNINGS.]");
        return confirm;
    }

    public boolean confirmContentWarnings(String warnings, boolean guaranteed) {
        if (!this.autoContentWarnings) {
            return true;
        }

        if (guaranteed) {
            parser.printDialogueLine("[If you make this choice, you will encounter: " + warnings + ".]", true);
        } else {
            parser.printDialogueLine("[If you make this choice, you might encounter: " + warnings + ".]", true);
        }

        boolean confirm = this.parser.promptYesNo("[Are you sure you wish to proceed?]");
        parser.printDialogueLine("[You can turn choice confirmations off at any time with TOGGLE WARNINGS.]");
        return confirm;
    }

    public boolean confirmContentWarnings(String warnings) {
        return this.confirmContentWarnings(warnings, false);
    }

    public boolean confirmContentWarnings(Chapter c, String extraWarnings, boolean guaranteed) {
        if (!this.autoContentWarnings) {
            return true;
        }

        // figure out what i was trying to do here and fix it

        if (guaranteed) {
            parser.printDialogueLine("[If you make this choice, you will encounter: " + "; " + c.getContentWarnings() + ".]", true);
        } else {
            parser.printDialogueLine("[If you make this choice, you might encounter: " + "; " + c.getContentWarnings() + ".]", true);
        }

        boolean confirm = this.parser.promptYesNo("[Are you sure you wish to proceed?]");
        parser.printDialogueLine("[You can turn choice confirmations off at any time with TOGGLE WARNINGS.]");
        return confirm;
    }

    public boolean confirmContentWarnings(Chapter c, boolean guaranteed) {
        if (!this.autoContentWarnings) {
            return true;
        }

        if (guaranteed) {
            parser.printDialogueLine("[If you make this choice, you will encounter: " + c.getContentWarnings() + ".]", true);
        } else {
            parser.printDialogueLine("[If you make this choice, you might encounter: " + c.getContentWarnings() + ".]", true);
        }

        boolean confirm = this.parser.promptYesNo("[Are you sure you wish to proceed?]");
        parser.printDialogueLine("[You can turn choice confirmations off at any time with TOGGLE WARNINGS.]");
        return confirm;
    }

    public boolean confirmContentWarnings(Chapter c) {
        return this.confirmContentWarnings(c, false);
    }

    // --- COMMANDS ---

    public String help(String argument) {
        String arg = (Command.GO.argumentIsValid(argument)) ? "go" : argument;
        switch (arg) {
            case "help":
            case "show":
            case "toggle":
            case "go":
            case "walk":
            case "enter":
            case "leave":
            case "turn":
            case "approach":
            case "slay":
            case "take":
            case "drop":
            case "throw":
                try {
                    this.showCommandHelp(arg);
                } catch (RuntimeException e) {
                    this.showCommandList();
                }

                break;
            default:
                this.showCommandList();
        }

        return "Meta";
    }

    public void showCommandList() {
        for (Command c : Command.values()) {
            if (c != Command.DIRECTGO) {
                IOHandler.wrapPrintln("  - " + c.getPrefix().toUpperCase() + ": " + c.getDescription());
            }
        }
    }

    public void showCommandHelp(String command) {
        String prefix;
        Command c = Command.getCommand(command);

        if (c == null) {
            throw new RuntimeException("Invalid command");
        } else if (c == Command.DIRECTGO) {
            c = Command.GO;
            prefix = "GO";
        } else if (c == Command.WALK) {
            c = Command.GO;
            prefix = "WALK";
        } else {
            prefix = c.getPrefix().toUpperCase();
        }

        String s = prefix + ": " + c.getDescription() + "\nSyntax: ";
        switch (c) {
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
                s += "  - TOGGLE [WARNINGS / CONTENT WARNINGS / CWS / TRIGGER WARNINGS / TWS]: Toggles automatic content warnings on or off.\n";
                s += "  - TOGGLE [NOW PLAYING / NP / MUSIC / SOUNDTRACK]: Toggles soundtrack notifications on or off.\n";
                break;
            case GO:
                s += prefix + " [direction]\n";
                s += prefix + " is optional. > [direction] functions exactly the same.\n\n";

                s += "- Arguments -\n";
                s += "  - [direction]: The direction you wish to travel in.\n\n";

                s += "- Variations -\n";
                s += "  - " + prefix + " [FORWARD / FORWARDS / F]: Press onwards.\n";
                s += "  - " + prefix + " [BACK / BACKWARD / BACKWARDS / B]: Turn back.\n";
                s += "  - " + prefix + " [INSIDE / IN / I]: Enter the nearest location, if possible.\n";
                s += "  - " + prefix + " [OUTSIDE / OUT / O]: Leave your current location, if possible.\n";
                s += "  - " + prefix + " [DOWN / D]: Descend.\n";
                s += "  - " + prefix + " [UP / U]: Ascend.\n";
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
        }

        IOHandler.wrapPrint(s);
    }

    public void showGeneralWarnings(boolean slowPrint) {
        if (slowPrint) {
            parser.printDialogueLine("General CWs: death; murder; suicide; verbal abuse; gaslighting; gore; mutilation, disembowelment; loss of self; cosmic horror; existential horror; being eaten alive; suffocation; derealisation; forced suicide; loss of bodily autonomy; starvation; unreality; body horror; forced self-mutilation; self-degloving; flaying; self-immolation; drowning; burning to death; loss of control; dismemberment; self-decapitation; memory loss", true);
        } else {
            IOHandler.wrapPrintln("General CWs: death; murder; suicide; verbal abuse; gaslighting; gore; mutilation, disembowelment; loss of self; cosmic horror; existential horror; being eaten alive; suffocation; derealisation; forced suicide; loss of bodily autonomy; starvation; unreality; body horror; forced self-mutilation; self-degloving; flaying; self-immolation; drowning; burning to death; loss of control; dismemberment; self-decapitation; memory loss");
        }
    }

    public void showGeneralWarnings() {
        this.showGeneralWarnings(false);
    }

    public void showByChapterWarnings() {
        String s = "";
        
        for (Chapter c : Chapter.values()) {
            switch (c) {
                case CH1: continue;
                case MUTUALLYASSURED: break;

                case ADVERSARY:
                    s += "------- Possible content warnings for Chapter II -------";
                    s += "\n  - " + c.getTitle();
                    break;
                case NEEDLE:
                    s += "\n\n------- Possible content warnings for Chapter III -------";
                    s += "\n  - " + c.getTitle();
                    break;

                case ARMSRACE:
                    s += "\n  - " + c.getTitle() + " & " + Chapter.MUTUALLYASSURED.getFullTitle();
                    break;
                case NOWAYOUT:
                    s += "\n  - " + c.getTitle() + " & " + Chapter.EMPTYCUP.getFullTitle();
                    break;

                default: s += "\n  - " + c.getTitle();
            }

            switch (c) {
                default: s += "  - " + c.getTitle();
            }

            s += ": " + c.getContentWarnings();
        }

        IOHandler.wrapPrintln(s);
    }

    public void showChapterWarnings(Chapter c, ChapterEnding prevEnding) {
        switch (c) {
            case STRANGER:
            case CLARITY:
            case GREY:
                parser.printDialogueLine(c.getFullTitle() + " contains: " + c.getContentWarnings(prevEnding));
                break;
            default:
                parser.printDialogueLine(c.getFullTitle() + " may contain: " + c.getContentWarnings(prevEnding));
        }
    }

    public void showChapterWarnings(Chapter c) {
        switch (c) {
            case STRANGER:
            case CLARITY:
            case GREY:
                parser.printDialogueLine(c.getFullTitle() + " contains: " + c.getContentWarnings());
                break;
            default:
                parser.printDialogueLine(c.getFullTitle() + " may contain: " + c.getContentWarnings());
        }
    }

    private String toggle(String argument, boolean secondPrompt) {
        switch (argument) {
            case "warnings":
            case "content warnings":
            case "cws":
            case "trigger warnings":
            case "tws":
                this.toggleAutoWarnings();
                return "Meta";

            case "now playing":
            case "nowplaying":
            case "np":
            case "music":
            case "soundtrack":
                this.toggleNowPlaying();
                return "Meta";

            case "":
                if (secondPrompt) {
                    this.showCommandHelp("toggle");
                    return "Meta";
                } else {
                    parser.printDialogueLine("Would you like to toggle automatic content warnings (\"WARNINGS\") or soundtrack notifications (\"NOW PLAYING\")?", true);
                    return this.toggle(parser.getInput(), true);
                }

            default:
                this.showCommandHelp("toggle");
                return "Meta";
        }
    }

    public String toggle(String argument) {
        return toggle(argument, false);
    }

    public void toggleAutoWarnings() {
        if (this.autoContentWarnings) {
            this.autoContentWarnings = false;
            parser.printDialogueLine("[Automatic content warnings have been disabled.]");
        } else {
            this.autoContentWarnings = true;
            parser.printDialogueLine("[Automatic content warnings have been enabled.]");
        }
    }

    public void toggleNowPlaying() {
        if (this.showNowPlaying) {
            this.showNowPlaying = false;
            parser.printDialogueLine("[Soundtrack notifications have been disabled.]");
        } else {
            this.showNowPlaying = true;
            parser.printDialogueLine("[Soundtrack notifications have been enabled.]");
        }
    }

    public static void main(String[] args) {
        GameManager manager = new GameManager();
        manager.runGame(true);
    }

}
