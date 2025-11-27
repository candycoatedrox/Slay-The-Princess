import java.util.ArrayList;
import java.util.HashMap;

public class GameManager {
    
    private final IOHandler parser;
    private Cycle currentCycle;

    // Settings
    private boolean autoContentWarnings = true;
    private boolean showNowPlaying = true;

    // The song currently "playing"
    private String nowPlaying;

    // Global progress trackers
    private Chapter firstPrincess;
    private ArrayList<Vessel> claimedVessels;
    private ArrayList<ChapterEnding> endingsFound;
    private HashMap<Chapter, Boolean> visitedChapters;
    private HashMap<Voice, Boolean> voicesMet;
    private ArrayList<String> playlist;

    private int nVesselsAborted = 0;
    private boolean goodEndingAttempted = false;
    private int mirrorCruelCount = 0;

    private final OptionsMenu warningsMenu;

    // --- CONSTRUCTOR ---

    /**
     * Constructor
     */
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

    /**
     * 
     * @return
     */
    private OptionsMenu createWarningsMenu() {
        OptionsMenu menu = new OptionsMenu(true);
        menu.add(new Option(this, "general", "[Show general content warnings.]", 0));
        menu.add(new Option(this, "by chapter", "[Show content warnings by chapter.]", 0));
        menu.add(new Option(this, "current", "[Show content warnings for the current chapter.]", 0));
        menu.add(new Option(this, "cancel", "[Return to game.]", 0));

        return menu;
    }

    // --- ACCESSORS & MANIPULATORS ---

    /**
     * Accessor for currentCycle
     * @return the current active Cycle
     */
    public Cycle getCurrentCycle() {
        return this.currentCycle;
    }

    /**
     * Manipulator for nowPlaying; also prints the currently "playing" song if showNowPlaying is enabled
     * @param song the new song title to "play"
     * @param lineBreak whether or not to print a line break before the currently playing song
     */
    public void setNowPlaying(String song, boolean lineBreak) {
        if (this.showNowPlaying) {
            if (this.nowPlaying == null) {
                parser.printDialogueLine("------- Now Playing: " + song + " -------", true);
            } else if (!this.nowPlaying.equals(song)) {
                parser.printDialogueLine("------- Now Playing: " + song + " -------", true);
            }
        }

        this.nowPlaying = song;
    }

    /**
     * Manipulator for nowPlaying; also prints the currently "playing" song if showNowPlaying is enabled
     * @param song the new song title to "play"
     */
    public void setNowPlaying(String song) {
        this.setNowPlaying(song, false);
    }

    /**
     * Checks whether the player has visited a given Chapter
     * @param c the Chapter to check
     * @return true if the player has been to c; false otherwise
     */
    public boolean hasVisited(Chapter c) {
        return this.visitedChapters.get(c);
    }

    /**
     * Marks all Chapters in an ArrayList
     * @param route the list of Chapters visited by a player during a StandardCycle
     */
    public void updateVisitedChapters(ArrayList<Chapter> route) {
        for (int i = 1; i < route.size(); i++) {
            this.visitedChapters.put(route.get(i), true);
        }
    }

    /**
     * Checks if the player has encountered a given Voice in their playthrough
     * @param v the Voice to check
     * @return true if the player has encountered v; false otherwise
     */
    public boolean hasMet(Voice v) {
        return this.voicesMet.get(v);
    }

    /**
     * Marks all Voices in an ArrayList as met
     * @param voices the list of Voices encountered by the player during a StandardCycle
     */
    public void updateVoicesMet(ArrayList<Voice> voices) {
        for (Voice v : voices) {
            this.voicesMet.put(v, true);
        }
    }

    /**
     * Manipulator for firstPrincess
     * @param c the Chapter to set firstPrincess to
     */
    public void setFirstPrincess(Chapter c) {
        this.firstPrincess = c;
    }

    /**
     * Returns the nth Vessel claimed by the player
     * @param n the index of the Vessel being retrieved
     * @return the nth claimed Vessel
     */
    public Vessel getClaimedVessel(int n) {
        if (n > 4 || n < 0) {
            throw new IllegalArgumentException("Impossible claimed vessel index; player can only have up to 5 claimed vessels");
        } else if (n >= this.nClaimedVessels()) {
            return null; // No vessel with this index *yet*
        }

        return this.claimedVessels.get(n);
    }

    /**
     * Returns the number of Vessels the player has claimed
     * @return the number of Vessels the player has claimed
     */
    public int nClaimedVessels() {
        return this.claimedVessels.size();
    }

    /**
     * Adds a song to the playthrough's playlist
     * @param song the song title to add to the playlist
     */
    public void addToPlaylist(String song) {
        if (!this.playlist.contains(song)) {
            this.playlist.add(song);
        }
    }

    /**
     * Increments nVesselsAborted
     */
    public void abortVessel() {
        this.nVesselsAborted += 1;
    }

    /**
     * Returns the number of Vessels/Chapters the player has aborted
     * @return the number of Vessels/Chapters the player has aborted
     */
    public int nVesselsAborted() {
        return this.nVesselsAborted;
    }

    /**
     * Accessor for goodEndingAttempted
     * @return the value of goodEndingAttempted
     */
    public boolean goodEndingAttempted() {
        return this.goodEndingAttempted;
    }

    /**
     * Sets goodEndingAttempted to true
     */
    public void attemptGoodEnding() {
        this.goodEndingAttempted = true;
    }

    public boolean mirrorWasCruel() {
        return this.mirrorCruelCount >= 2;
    }

    /**
     * Increments mirrorCruelCount
     */
    public void incrementCruelCount() {
        this.mirrorCruelCount += 1;
    }
    
    /**
     * Returns the content warnings menu (allowing the player to choose which set of content warnings they wish to view)
     * @return the content warnings menu
     */
    public OptionsMenu warningsMenu() {
        return this.warningsMenu;
    }

    // --- PLAYTHROUGH MANAGEMENT ---

    /**
     * Initiates and coordinates a full playthrough of the game
     */
    public void runGame() {
        ChapterEnding ending = null;

        this.intro();
        
        while (this.nClaimedVessels() < 5 && this.nVesselsAborted < 6) {
            this.currentCycle = new StandardCycle(this, this.parser);
            ending = this.currentCycle.runCycle();

            if (ending == null) {
                this.abortVessel();
            } else if (ending == ChapterEnding.GOODENDING) {
                break;
            } else {
                this.endingsFound.add(ending);
                this.claimedVessels.add(ending.getVessel());
            }
        }

        if (this.nClaimedVessels() == 5) {
            this.currentCycle = new Finale(this, this.claimedVessels, this.parser);
            ending = this.currentCycle.runCycle();
        } else {
            this.currentCycle = null;

            if (ending != ChapterEnding.GOODENDING) {
                ending = ChapterEnding.OBLIVION;
            }
        }

        this.endGame(ending);
    }

    /**
     * Initiates and coordinates a full playthrough of the game, skipping the intro (FOR DEBUG/PLAYTEST PURPOSES ONLY)
     * @param debug a generic parameter to distinguish it from the public, "final" version of runGame()
     */
    private void runGame(boolean debug) {
        ChapterEnding ending = null;
        
        while (this.nClaimedVessels() < 5 && this.nVesselsAborted < 6) {
            this.currentCycle = new StandardCycle(this, this.parser);
            ending = this.currentCycle.runCycle();

            if (ending == ChapterEnding.ABORTED) {
                this.nVesselsAborted += 1;
            } else if (ending == ChapterEnding.GOODENDING) {
                break;
            } else {
                this.endingsFound.add(ending);
                this.claimedVessels.add(ending.getVessel());
            }
        }

        if (this.nClaimedVessels() == 5) {
            this.currentCycle = new Finale(this, this.claimedVessels, this.parser);
            ending = this.currentCycle.runCycle();
        } else {
            this.currentCycle = null;
            if (ending != ChapterEnding.GOODENDING) ending = ChapterEnding.OBLIVION;
        }

        this.endGame(ending);
    }

    // --- SCENES ---

    /**
     * Runs the intro of the game, letting the player view content warnings and change settings
     */
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
        IOHandler.wrapPrintln("Would you like to turn dynamic content warnings off?");
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

    /**
     * Runs the ending sequence of the game, including showing credits and the playlist
     * @param ending the ending achieved by the player
     */
    private void endGame(ChapterEnding ending) {
        // credits, show playlist, etc
        this.showCredits();
        this.showPlaylist();

        this.parser.closeInput();
    }

    /**
     * Shows the credits of the game
     */
    private void showCredits() {

    }

    /**
     * Shows the playlist generated from the current playthrough
     */
    private void showPlaylist() {

    }

    // --- UTILITY ---

    /**
     * Warns the player of potential content warnings from committing to a choice, and allows them to change their mind
     * @param warnings the content warnings that appear after this choice
     * @return true if dynamic content warnings are disabled or the player chooses to continue; false otherwise
     */
    public boolean confirmContentWarnings(String warnings) {
        return this.confirmContentWarnings(warnings, false);
    }

    /**
     * Warns the player of potential content warnings from committing to a choice, and allows them to change their mind
     * @param warnings the content warnings that appear after this choice
     * @param guaranteed whether the content warnings are guaranteed or not
     * @return true if dynamic content warnings are disabled or the player chooses to continue; false otherwise
     */
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
        parser.printDialogueLine("[You can turn dynamic content warnings off at any time with TOGGLE WARNINGS.]");
        return confirm;
    }

    /**
     * Warns the player of potential content warnings from committing to a choice that leads to a given Chapter, and allows them to change their mind
     * @param c the Chapter that this choice will lead the player to
     * @return true if dynamic content warnings are disabled or the player chooses to continue; false otherwise
     */
    public boolean confirmContentWarnings(Chapter c) {
        return this.confirmContentWarnings(c, false);
    }

    /**
     * Warns the player of potential content warnings from committing to a choice that leads to a given Chapter, and allows them to change their mind
     * @param c the Chapter that this choice will lead the player to
     * @param guaranteed whether c's content warnings are guaranteed or not
     * @return true if dynamic content warnings are disabled or the player chooses to continue; false otherwise
     */
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
        parser.printDialogueLine("[You can turn dynamic content warnings off at any time with TOGGLE WARNINGS.]");
        return confirm;
    }

    /**
     * Warns the player of potential content warnings from committing to a choice that leads to a given Chapter, and allows them to change their mind
     * @param c the Chapter that this choice will lead the player to
     * @param extraWarnings the extra content warnings that appear before the next Chapter begins
     * @return true if dynamic content warnings are disabled or the player chooses to continue; false otherwise
     */
    public boolean confirmContentWarnings(Chapter c, String extraWarnings) {
        if (!this.autoContentWarnings) {
            return true;
        }

        parser.printDialogueLine("[If you make this choice, you will encounter: " + extraWarnings + ".]", true);
        parser.printDialogueLine("[You might also encounter: " + c.getContentWarnings() + ".]", true);

        boolean confirm = this.parser.promptYesNo("[Are you sure you wish to proceed?]");
        parser.printDialogueLine("[You can turn dynamic content warnings off at any time with TOGGLE WARNINGS.]");
        return confirm;
    }

    /**
     * Warns the player of potential content warnings from committing to a choice that leads to a given Chapter, and allows them to change their mind
     * @param c the Chapter that this choice will lead the player to
     * @param extraWarnings the extra content warnings that appear before the next Chapter begins
     * @param guaranteed whether the extra content warnings are guaranteed or not
     * @return true if dynamic content warnings are disabled or the player chooses to continue; false otherwise
     */
    public boolean confirmContentWarnings(Chapter c, String extraWarnings, boolean guaranteed) {
        if (!this.autoContentWarnings) {
            return true;
        }

        if (guaranteed) {
            parser.printDialogueLine("[If you make this choice, you will encounter: " + extraWarnings + ".]", true);
            parser.printDialogueLine("[You might also encounter: " + c.getContentWarnings() + ".]", true);
        } else {
            parser.printDialogueLine("[If you make this choice, you might encounter: " + extraWarnings + "; " + c.getContentWarnings() + ".]", true);
        }

        boolean confirm = this.parser.promptYesNo("[Are you sure you wish to proceed?]");
        parser.printDialogueLine("[You can turn dynamic content warnings off at any time with TOGGLE WARNINGS.]");
        return confirm;
    }

    // --- COMMANDS ---

    /**
     * Displays all available commands or information on a given command
     * @param argument the command to show information on (or blank, to show all commands)
     */
    public void help(String argument) {
        String arg = (Command.GO.argumentIsValid(argument)) ? "go" : argument;
        switch (arg) {
            case "help":
            case "show":
            case "toggle":
            case "go":
            case "walk":
            case "enter":
            case "leave":
            case "proceed":
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
    }

    /**
     * Shows the name and a brief description of all available commands
     */
    public void showCommandList() {
        for (Command c : Command.values()) {
            switch (c) {
                case DIRECTGO:
                case WALK: break;

                default: IOHandler.wrapPrintln("  - " + c.getPrefix().toUpperCase() + ": " + c.getDescription());
            }
        }
    }

    /**
     * Shows a detailed description of a given command
     * @param command the command to show information on
     */
    public void showCommandHelp(String command) {
        Command c = Command.getCommand(command);
        if (c == null) throw new RuntimeException("Invalid command");

        this.showCommandHelp(c);
    }

    /**
     * Shows a detailed description of a given command
     * @param c the command to show information on
     */
    public void showCommandHelp(Command c) {
        IOHandler.wrapPrint(c.help());
    }

    /**
     * Displays general content warnings for the game
     */
    public void showGeneralWarnings() {
        IOHandler.wrapPrintln("You are guaranteed to encounter: death; murder; verbal abuse; gaslighting; described gore.");
        IOHandler.wrapPrintln("If suicide is a significantly triggering topic for you, we suggest you take care of yourself while playing the game, or for you to possibly avoid playing it.");
        System.out.println();
        IOHandler.wrapPrintln("General CWs: death; murder; suicide; verbal abuse; gaslighting; gore; mutilation, disembowelment; loss of self; cosmic horror; existential horror; being eaten alive; suffocation; derealisation; forced suicide; loss of bodily autonomy; starvation; unreality; body horror; forced self-mutilation; self-degloving; flaying; self-immolation; drowning; burning to death; loss of control; dismemberment; self-decapitation; memory loss");
    }

    /**
     * Displays content warnings for each Chapter in the game
     */
    public void showByChapterWarnings() {
        String s = "";
        
        for (Chapter c : Chapter.values()) {
            switch (c) {
                case CH1: continue;
                case MUTUALLYASSURED: break;

                case ADVERSARY:
                    s += "----- Possible content warnings for Chapter II -----";
                    s += "\n  - " + c.getTitle();
                    break;
                case NEEDLE:
                    s += "\n\n----- Possible content warnings for Chapter III -----";
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

            s += ": " + c.getContentWarnings();
        }

        IOHandler.wrapPrintln(s);
    }

    /**
     * Displays content warnings for a given variant of a Chapter, depending on the ending of the previous Chapter
     * @param c the Chapter to display content warnings for
     * @param prevEnding the ending of the previous Chapter
     */
    public void showChapterWarnings(Chapter c, ChapterEnding prevEnding) {
        if (!c.hasContentWarnings()) {
            parser.printDialogueLine("[The current chapter has no content warnings to display.]");
        } else {
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
    }

    /**
     * Displays content warnings for a given Chapter
     * @param c the Chapter to display content warnings for
     */
    public void showChapterWarnings(Chapter c) {
        if (!c.hasContentWarnings()) {
            parser.printDialogueLine("[The current chapter has no content warnings to display.]");
        } else {
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
    }

    /**
     * Toggles dynamic content warnings or soundtrack notifications on or off
     * @param argument the setting to toggle on or off
     */
    public void toggle(String argument) {
        toggle(argument, false);
    }

    /**
     * Toggles dynamic content warnings or soundtrack notifications on or off
     * @param argument the setting to toggle on or off
     * @param secondPrompt whether the player has already been given a chance to re-enter a valid argument
     */
    private void toggle(String argument, boolean secondPrompt) {
        switch (argument) {
            case "warnings":
            case "content warnings":
            case "cws":
            case "trigger warnings":
            case "tws":
                this.toggleAutoWarnings();
                break;

            case "now playing":
            case "nowplaying":
            case "np":
            case "music":
            case "soundtrack":
                this.toggleNowPlaying();
                break;

            case "":
                if (secondPrompt) {
                    this.showCommandHelp(Command.TOGGLE);
                    break;
                } else {
                    parser.printDialogueLine("Would you like to toggle dynamic content warnings (\"WARNINGS\") or soundtrack notifications (\"NOW PLAYING\")?", true);
                    this.toggle(parser.getInput(), true);
                    break;
                }

            default:
                this.showCommandHelp(Command.TOGGLE);
                break;
        }
    }

    /**
     * Toggles dynamic content warnings on or off
     */
    public void toggleAutoWarnings() {
        if (this.autoContentWarnings) {
            this.autoContentWarnings = false;
            parser.printDialogueLine("[Automatic content warnings have been disabled.]");
        } else {
            this.autoContentWarnings = true;
            parser.printDialogueLine("[Automatic content warnings have been enabled.]");
        }
    }

    /**
     * Toggles soundtrack notifications on or off
     */
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
