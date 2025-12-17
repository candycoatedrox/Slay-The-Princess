import java.util.ArrayList;
import java.util.HashMap;

public class GameManager {
    
    private final IOHandler parser;
    private Cycle currentCycle;

    // Settings
    private final boolean demoMode = true;
    private final boolean trueDemoMode = false;
    private boolean dynamicWarnings = true;
    private boolean showNowPlaying = true;
    private boolean globalSlowPrint = true;
    private boolean autoAdvance = false;

    // The song currently "playing"
    private String nowPlaying;

    // Global progress trackers
    private Chapter firstPrincess; // need more nuance here for harsh/soft and different variations (ex. Razor-revival)
    private ArrayList<Vessel> claimedVessels;
    private ArrayList<ChapterEnding> endingsFound;
    private HashMap<Chapter, Boolean> visitedChapters;
    private HashMap<Voice, Boolean> voicesMet;
    private ArrayList<String> playlist;

    private int nVesselsAborted = 0;
    private int mirrorCruelCount = 0;
    private boolean goodEndingAttempted = false;

    // Variables used in the Spaces Between
    private boolean mirrorScaredFlag = false;
    private int moundFreedom = 0;
    private int moundSatisfaction = 0;
    private boolean directToMound = false;
    private boolean askedRiddleMound = false;
    private boolean threatenedMound = false;
    private boolean refuseExploreMound = false;

    // Global menus and options
    private boolean trueExclusiveMenu = false; // Only used during show() and settings() menus
    private final OptionsMenu settingsMenu;
    private final OptionsMenu warningsMenu;
    private final Option intermissionAttackMound;
    private final Option intermissionAttackSelf;
    private final Option intermissionWait;

    // --- CONSTRUCTOR ---

    /**
     * Constructor
     */
    public GameManager() {
        this.parser = new IOHandler(this);
        this.claimedVessels = new ArrayList<>();
        this.endingsFound = new ArrayList<>();

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
            switch (v) {
                case NARRATOR:
                case NARRATORPRINCESS:
                case PRINCESS:
                case HERO: break;

                default: this.voicesMet.put(v, false);
            }
        }

        this.settingsMenu = this.createSettingsMenu();
        this.warningsMenu = this.createWarningsMenu();
        this.intermissionAttackMound = new Option(this, "attackMound", "[Attack the entity.]");
        this.intermissionAttackSelf = new Option(this, "attackSelf", "[Destroy your body.]");
        this.intermissionWait = new Option(this, "wait", "\"I'm not going back.\" [Wait.]");
    }

    /**
     * Initializes the settings menu for this manager
     * @return the settings menu for this manager
     */
    private OptionsMenu createSettingsMenu() {
        OptionsMenu menu = new OptionsMenu(true);
        menu.add(new Option(this, "warnings", "[Turn dynamic content warnings OFF.]", 0));
        menu.add(new Option(this, "now playing", "[Turn soundtrack notifications OFF.]", 0));
        menu.add(new Option(this, "slow print", "[Set print speed to INSTANT.]", 0));
        menu.add(new Option(this, "auto advance", "[Turn auto-advancing dialogue ON.]", 0));
        menu.add(new Option(this, "cancel", "[Return to game.]", 0));

        return menu;
    }

    /**
     * Initializes the content warnings menu for this manager
     * @return the content warnings menu for this manager
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
     * Accessor for demoMode
     * @return whether demo mode is currently enabled
     */
    public boolean demoMode() {
        return this.demoMode;
    }

    /**
     * Accessor for trueDemoMode
     * @return whether "true" demo mode is currently enabled (i.e. whether to stop after meeting the Chapter 2 Princess)
     */
    public boolean trueDemoMode() {
        return this.trueDemoMode;
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
     * Accessor for globalSlowPrint
     * @return whether to slowly print dialogue lines or print them instantly
     */
    public boolean globalSlowPrint() {
        return this.globalSlowPrint;
    }

    /**
     * Accessor for autoAdvance
     * @return whether to automatically continue printing dialogue after each line or wait for player input
     */
    public boolean autoAdvance() {
        return this.autoAdvance;
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
     * Checks whether the player has visited all Chapters in a given list
     * @param chapters the Chapters to check
     * @return true if the player has been to every Chapter in chapters; false otherwise
     */
    public boolean hasVisitedAll(Chapter... chapters) {
        for (Chapter c : chapters) {
            if (!this.visitedChapters.get(c)) return false;
        }

        return true;
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
     * Checks if the player has claimed a given Vessel
     * @param v the Vessel to check for
     * @return true if the player has claimed v; false otherwise
     */
    public boolean hasClaimedVessel(Vessel v) {
        return this.claimedVessels.contains(v);
    }

    /**
     * Checks if the player has claimed any one of several Vessels
     * @param vessels the Vessels to check for
     * @return true if the player has claimed any Vessel in vessels; false otherwise
     */
    public boolean hasClaimedAnyVessel(Vessel... vessels) {
        for (Vessel v : vessels) {
            if (this.claimedVessels.contains(v)) return true;
        }
        
        return false;
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
        if (!song.isEmpty() && !this.playlist.contains(song)) {
            this.playlist.add(song);
        }
    }

    /**
     * Accessor for nVesselsAborted
     * @return the number of vessels the player has aborted this playthrough
     */
    public int nVesselsAborted() {
        return this.nVesselsAborted;
    }

    /**
     * Checks if the player was cruel at the mirror
     * @return true if the player was cruel to the Voices at the mirror 2+ times; false otherwise
     */
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

    /**
     * Accessor for mirrorScaredFlag
     * @return whether the player has reached the end of a cycle with the Voice of the Hero present
     */
    public boolean getMirrorScaredFlag() {
        return this.mirrorScaredFlag;
    }

    /**
     * Sets mirrorScaredFlag to true
     */
    public void setMirrorScaredFlag() {
        this.mirrorScaredFlag = true;
    }

    /**
     * Checks if moundFreedom is positive or equal to 0
     * @return whether the Shifting Mound's freedom value is positive, based on the endings the player has found
     */
    public boolean moundFreedom() {
        return this.moundFreedom >= 0;
    }

    /**
     * Checks if moundSatisfaction is positive or equal to 0
     * @return whether the Shifting Mound's satisfaction value is positive, based on the endings the player has found
     */
    public boolean moundSatisfaction() {
        return this.moundSatisfaction >= 0;
    }

    /**
     * Accessor for directToMound
     * @return whether the player claimed a vessel from the start or aborted at least one vessel before claiming one
     */
    public boolean getDirectToMound() {
        return this.directToMound;
    }

    /**
     * Accessor for askedRiddleMound
     * @return whether the player has asked the Shifting Mound to stop speaking in riddles in the Spaces Between
     */
    public boolean getAskedRiddleMound() {
        return this.askedRiddleMound;
    }

    /**
     * Sets askedRiddleMound to true
     */
    public void askRiddleMound() {
        this.askedRiddleMound = true;
    }

    /**
     * Accessor for threatenedMound
     * @return whether the player has threatened the Shifting Mound in the Spaces Between
     */
    public boolean getThreatenedMound() {
        return this.threatenedMound;
    }

    /**
     * Sets threatenedMound to true
     */
    public void threatenMound() {
        this.threatenedMound = true;
    }

    /**
     * Accessor for refuseExploreMound
     * @return whether the player has threatened the Shifting Mound in the Spaces Between
     */
    public boolean getRefuseExploreMound() {
        return this.refuseExploreMound;
    }

    /**
     * Sets refuseExploreMound to true
     */
    public void refuseExploreMound() {
        this.refuseExploreMound = true;
    }

    /**
     * Accessor for intermissionAttackMound
     * @return the Option to attack the Shifting Mound in the Spaces Between
     */
    public Option getIntermissionAttackMound() {
        return this.intermissionAttackMound;
    }

    /**
     * Accessor for intermissionAttackSelf
     * @return the Option for the player to attack themself in the Spaces Between
     */
    public Option getIntermissionAttackSelf() {
        return this.intermissionAttackSelf;
    }

    /**
     * Accessor for intermissionWait
     * @return the Option for the player to wait with the Shifting Mound in the Spaces Between
     */
    public Option getIntermissionWait() {
        return this.intermissionWait;
    }

    /**
     * Accessor for trueExclusiveMenu
     * @return whether the active menu is truly exclusive (i.e. not even meta commands are available until an option is selected)
     */
    public boolean trueExclusiveMenu() {
        return this.trueExclusiveMenu;
    }

    /**
     * Manipulator for trueExclusiveMenu
     * @param newValue whether the active menu is truly exclusive (i.e. not even meta commands are available until an option is selected)
     */
    public void setTrueExclusiveMenu(boolean newValue) {
        this.trueExclusiveMenu = newValue;
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
                ending = ChapterEnding.DEMOENDING;
                break;
            } else if (ending == ChapterEnding.ABORTED) {
                this.nVesselsAborted += 1;
            } else if (ending == ChapterEnding.GOODENDING) {
                break;
            } else {
                this.endingsFound.add(ending);
                this.claimedVessels.add(ending.getVessel());

                this.moundFreedom += ending.getFreedom();
                this.moundSatisfaction += ending.getSatisfaction();
                
                this.addToPlaylist(ending.getPlaylistSong());
                switch (this.nClaimedVessels()) {
                    case 1:
                        this.addToPlaylist("The Shifting Mound Movement I");
                        if (ending.getVessel() == Vessel.STRANGER) this.moundSatisfaction += 1;
                        if (this.nVesselsAborted == 0) this.directToMound = true;
                        break;
                    case 2:
                        this.addToPlaylist("The Shifting Mound Movement II");
                        break;
                    case 3:
                        this.addToPlaylist("The Shifting Mound Movement III");
                        break;
                    case 4:
                        this.addToPlaylist("The Shifting Mound Movement IV");
                        break;
                }

                if (this.demoMode) {
                    ending = ChapterEnding.DEMOENDING;
                    break;
                }
            }
        }

        if (this.nClaimedVessels() == 5) {
            this.currentCycle = new Finale(this, this.claimedVessels, this.endingsFound, this.firstPrincess, this.parser);
            ending = this.currentCycle.runCycle();

            switch (ending) {
                case NOENDINGS:
                case THROUGHCONFLICT:
                    this.addToPlaylist("The Long Quiet");
                    this.addToPlaylist("The Shifting Mound Movement V");
                    this.addToPlaylist("The Apotheosis");
                    break;
                case YOURNEWWORLD:
                    this.addToPlaylist("The Shifting Mound Movement V");
                    this.addToPlaylist("Transformation");
                    this.addToPlaylist("The Long Quiet");
                    break;
                case PATHINTHEWOODS:
                    this.addToPlaylist("The Long Quiet");
                    this.addToPlaylist("The Shifting Mound Movement V");
                    this.addToPlaylist("Transformation");
                    this.addToPlaylist("The End of Everything, The Beginning of Something New");
                    break;
                case NEWANDUNENDINGDAWN:
                case ANDEVERYONEHATESYOU:
                    this.addToPlaylist("The Shifting Mound Movement V");
                    this.addToPlaylist("Transformation");
                    this.addToPlaylist("The Long Quiet");
                    break;
                case WHATHAPPENSNEXT:
                case STRANGEBEGINNINGS:
                    this.addToPlaylist("The Long Quiet");
                    this.addToPlaylist("The Shifting Mound Movement V");
                    this.addToPlaylist("Transformation");
                    this.addToPlaylist("The Unknown Together");
                    break;
            }
        } else {
            this.currentCycle = null;

            if (ending == ChapterEnding.ABORTED) {
                ending = ChapterEnding.OBLIVION;
            }
        }

        this.endGame(ending);
    }

    /**
     * (DEBUG ONLY) Initiates and coordinates a full playthrough of the game, skipping the intro
     */
    private void debugRunGame() {
        ChapterEnding ending = null;
        
        while (this.nClaimedVessels() < 5 && this.nVesselsAborted < 6) {
            this.currentCycle = new StandardCycle(this, this.parser);
            ending = this.currentCycle.runCycle();

            if (ending == null) {
                ending = ChapterEnding.DEMOENDING;
                break;
            } else if (ending == ChapterEnding.ABORTED) {
                this.nVesselsAborted += 1;
            } else if (ending == ChapterEnding.GOODENDING) {
                break;
            } else {
                this.endingsFound.add(ending);
                this.claimedVessels.add(ending.getVessel());

                this.moundFreedom += ending.getFreedom();
                this.moundSatisfaction += ending.getSatisfaction();
                
                this.addToPlaylist(ending.getPlaylistSong());
                switch (this.nClaimedVessels()) {
                    case 1:
                        this.addToPlaylist("The Shifting Mound Movement I");
                        if (ending.getVessel() == Vessel.STRANGER) this.moundSatisfaction += 1;
                        if (this.nVesselsAborted == 0) this.directToMound = true;
                        break;
                    case 2:
                        this.addToPlaylist("The Shifting Mound Movement II");
                        break;
                    case 3:
                        this.addToPlaylist("The Shifting Mound Movement III");
                        break;
                    case 4:
                        this.addToPlaylist("The Shifting Mound Movement IV");
                        break;
                }

                if (this.demoMode) {
                    ending = ChapterEnding.DEMOENDING;
                    break;
                }
            }
        }

        if (this.nClaimedVessels() == 5) {
            this.currentCycle = new Finale(this, this.claimedVessels, this.endingsFound, this.firstPrincess, this.parser);
            ending = this.currentCycle.runCycle();

            switch (ending) {
                case NOENDINGS:
                case THROUGHCONFLICT:
                    this.addToPlaylist("The Long Quiet");
                    this.addToPlaylist("The Shifting Mound Movement V");
                    this.addToPlaylist("The Apotheosis");
                    break;
                case YOURNEWWORLD:
                    this.addToPlaylist("The Shifting Mound Movement V");
                    this.addToPlaylist("Transformation");
                    this.addToPlaylist("The Long Quiet");
                    break;
                case PATHINTHEWOODS:
                    this.addToPlaylist("The Long Quiet");
                    this.addToPlaylist("The Shifting Mound Movement V");
                    this.addToPlaylist("Transformation");
                    this.addToPlaylist("The End of Everything, The Beginning of Something New");
                    break;
                case NEWANDUNENDINGDAWN:
                case ANDEVERYONEHATESYOU:
                    this.addToPlaylist("The Shifting Mound Movement V");
                    this.addToPlaylist("Transformation");
                    this.addToPlaylist("The Long Quiet");
                    break;
                case WHATHAPPENSNEXT:
                case STRANGEBEGINNINGS:
                    this.addToPlaylist("The Long Quiet");
                    this.addToPlaylist("The Shifting Mound Movement V");
                    this.addToPlaylist("Transformation");
                    this.addToPlaylist("The Unknown Together");
                    break;
            }
        } else {
            this.currentCycle = null;

            if (ending == ChapterEnding.ABORTED) {
                ending = ChapterEnding.OBLIVION;
            }
        }

        this.endGame(ending);
    }

    
    /**
     * (DEBUG ONLY) Initiates and coordinates a full playthrough of the game, starting from a given ChapterEnding
     * @param startFromEnding the ChapterEnding to start from
     * @param harsh the value to set isHarsh to
     */
    private void debugRunGame(ChapterEnding startFromEnding, boolean harsh) {
        ChapterEnding ending = null;
        boolean firstCycle = true;
        
        while (this.nClaimedVessels() < 5 && this.nVesselsAborted < 6) {
            this.currentCycle = new StandardCycle(this, this.parser);
            if (firstCycle) {
                ending = this.currentCycle.debugRunCycle(startFromEnding, harsh);
                firstCycle = false;
            } else {
                ending = this.currentCycle.runCycle();
            }

            if (ending == null) {
                ending = ChapterEnding.DEMOENDING;
                break;
            } else if (ending == ChapterEnding.ABORTED) {
                this.nVesselsAborted += 1;
            } else if (ending == ChapterEnding.GOODENDING) {
                break;
            } else {
                this.endingsFound.add(ending);
                this.claimedVessels.add(ending.getVessel());

                this.moundFreedom += ending.getFreedom();
                this.moundSatisfaction += ending.getSatisfaction();
                
                this.addToPlaylist(ending.getPlaylistSong());
                switch (this.nClaimedVessels()) {
                    case 1:
                        this.addToPlaylist("The Shifting Mound Movement I");
                        if (ending.getVessel() == Vessel.STRANGER) this.moundSatisfaction += 1;
                        if (this.nVesselsAborted == 0) this.directToMound = true;
                        break;
                    case 2:
                        this.addToPlaylist("The Shifting Mound Movement II");
                        break;
                    case 3:
                        this.addToPlaylist("The Shifting Mound Movement III");
                        break;
                    case 4:
                        this.addToPlaylist("The Shifting Mound Movement IV");
                        break;
                }

                if (this.demoMode) {
                    ending = ChapterEnding.DEMOENDING;
                    break;
                }
            }
        }

        if (this.nClaimedVessels() == 5) {
            this.currentCycle = new Finale(this, this.claimedVessels, this.endingsFound, this.firstPrincess, this.parser);
            ending = this.currentCycle.runCycle();

            switch (ending) {
                case NOENDINGS:
                case THROUGHCONFLICT:
                    this.addToPlaylist("The Long Quiet");
                    this.addToPlaylist("The Shifting Mound Movement V");
                    this.addToPlaylist("The Apotheosis");
                    break;
                case YOURNEWWORLD:
                    this.addToPlaylist("The Shifting Mound Movement V");
                    this.addToPlaylist("Transformation");
                    this.addToPlaylist("The Long Quiet");
                    break;
                case PATHINTHEWOODS:
                    this.addToPlaylist("The Long Quiet");
                    this.addToPlaylist("The Shifting Mound Movement V");
                    this.addToPlaylist("Transformation");
                    this.addToPlaylist("The End of Everything, The Beginning of Something New");
                    break;
                case NEWANDUNENDINGDAWN:
                case ANDEVERYONEHATESYOU:
                    this.addToPlaylist("The Shifting Mound Movement V");
                    this.addToPlaylist("Transformation");
                    this.addToPlaylist("The Long Quiet");
                    break;
                case WHATHAPPENSNEXT:
                case STRANGEBEGINNINGS:
                    this.addToPlaylist("The Long Quiet");
                    this.addToPlaylist("The Shifting Mound Movement V");
                    this.addToPlaylist("Transformation");
                    this.addToPlaylist("The Unknown Together");
                    break;
            }
        } else {
            this.currentCycle = null;

            if (ending == ChapterEnding.ABORTED) {
                ending = ChapterEnding.OBLIVION;
            }
        }

        this.endGame(ending);
    }
    
    /**
     * (DEBUG ONLY) Initiates and coordinates a full playthrough of the game, starting from a given ChapterEnding
     * @param startFromEnding the ChapterEnding to start from
     */
    private void debugRunGame(ChapterEnding startFromEnding) {
        this.debugRunGame(startFromEnding, false);
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
        IOHandler.wrapPrintln("You can view and change these settings, as well as print speed and auto-advancing dialogue, at any time with > SETTINGS.");
        IOHandler.wrapPrintln("You can also view a list of available commands at any time with > HELP.");
        IOHandler.wrapPrintln("Press enter to advance dialogue.");
        IOHandler.wrapPrintln("(You cannot skip through dialogue that is currently printing with enter. This feature may be added in the future.)");
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

        if (ending == ChapterEnding.DEMOENDING) {
            parser.printDialogueLine("You have reached the end of the demo.");
            parser.printDialogueLine("Thank you for playing!");
        } else {
            this.showCredits();
            this.showPlaylist(ending);
        }

        this.parser.closeInput();
    }

    /**
     * Shows the credits of the game
     */
    private void showCredits() {
        System.out.println();
        parser.printDialogueLine("This game is based off of Slay the Princess, a game created by Tony Howard-Arias and Abby Howard, also known as Black Tabby Games.");
        parser.printDialogueLine("The original game was written and designed by Tony Howard-Arias, with art, editing, and additional writing by Abby Howard.");
        parser.printDialogueLine("The game is available on Steam, Nintendo Switch, PS4, PS5, and Xbox, and features everything found in this remake, plus gorgeous hand-penciled art and fantastic voice acting by Jonathan Sims (as the Voices in your Head) and Nicole Goodnight (as the Princess).");
    }

    /**
     * Shows the playlist generated from the current playthrough
     */
    private void showPlaylist(ChapterEnding ending) {
        System.out.println();
        parser.printDialogueLine("Thank you so much for playing. As an expression of our gratitude, here's the track order for a special playlist just for you.");
        parser.printDialogueLine("As a reminder, the soundtrack for the game can be found on Spotify at https://spotify.link/PdG0uXZecEb.");

        String playlistText;
        switch (ending) {
            case GOODENDING:
                playlistText = "----- YOUR SONG -----";
                playlistText += "\n  1.) The Princess";
                playlistText += "\n  2.) The World Ender";
                // This one isn't an actual song, it's literally a compilation of stock cheering and applause sound effects that plays on loop if you get the Good Ending. And yes, it is included on the playlist screen in the actual game
                playlistText += "\n  3.) Eternal Bliss (Yay, you did it!)";
                break;

            default:
                playlistText = "----- OUR SONG -----";
                for (int i = 0; i < this.playlist.size(); i++) {
                    playlistText += "\n  " + i + ".) " + this.playlist.get(i);
                }
        }

        System.out.println();
        IOHandler.wrapPrintln(playlistText);
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
        if (!this.dynamicWarnings) {
            return true;
        }

        if (guaranteed) {
            parser.printDialogueLine("[If you make this choice, you will encounter: " + warnings + ".]", true);
        } else {
            parser.printDialogueLine("[If you make this choice, you might encounter: " + warnings + ".]", true);
        }

        boolean confirm = this.parser.promptYesNo("[Are you sure you wish to proceed?]");
        parser.printDialogueLine("[You can turn dynamic content warnings off at any time with TOGGLE WARNINGS.]");
        System.out.println();
        return confirm;
    }

    /**
     * Warns the player of potential content warnings from committing to a choice that leads to a given Chapter, and allows them to change their mind
     * @param c the Chapter that this choice will lead the player to
     * @return true if dynamic content warnings are disabled or the player chooses to continue; false otherwise
     */
    public boolean confirmContentWarnings(Chapter c) {
        switch (c) {
            case RAZOR:
            case ARMSRACE:
            case NOWAYOUT:
            case MUTUALLYASSURED:
            case EMPTYCUP:
            case STRANGER:
            case CLARITY:
            case GREY: return this.confirmContentWarnings(c, true);
            default: return this.confirmContentWarnings(c, false);
        }
    }

    /**
     * Warns the player of potential content warnings from committing to a choice that leads to a given Chapter, and allows them to change their mind
     * @param c the Chapter that this choice will lead the player to (should be the Grey)
     * @param ending the chapter ending the player would achieve to reach c
     * @return true if dynamic content warnings are disabled or the player chooses to continue; false otherwise
     */
    public boolean confirmContentWarnings(Chapter c, ChapterEnding ending) {
        if (!this.dynamicWarnings) {
            return true;
        }

        parser.printDialogueLine("[If you make this choice, you will encounter: " + c.getContentWarnings(ending) + ".]", true);
        boolean confirm = this.parser.promptYesNo("[Are you sure you wish to proceed?]");
        parser.printDialogueLine("[You can turn dynamic content warnings off at any time with TOGGLE WARNINGS.]");
        System.out.println();
        return confirm;
    }

    /**
     * Warns the player of potential content warnings from committing to a choice that leads to a given Chapter, and allows them to change their mind
     * @param c the Chapter that this choice will lead the player to
     * @param guaranteed whether c's content warnings are guaranteed or not
     * @return true if dynamic content warnings are disabled or the player chooses to continue; false otherwise
     */
    public boolean confirmContentWarnings(Chapter c, boolean guaranteed) {
        if (!this.dynamicWarnings) {
            return true;
        }

        if (guaranteed) {
            parser.printDialogueLine("[If you make this choice, you will encounter: " + c.getContentWarnings() + ".]", true);
        } else {
            parser.printDialogueLine("[If you make this choice, you might encounter: " + c.getContentWarnings() + ".]", true);
        }

        boolean confirm = this.parser.promptYesNo("[Are you sure you wish to proceed?]");
        parser.printDialogueLine("[You can turn dynamic content warnings off at any time with TOGGLE WARNINGS.]");
        System.out.println();
        return confirm;
    }

    /**
     * Warns the player of potential content warnings from committing to a choice that leads to a given Chapter, and allows them to change their mind
     * @param c the Chapter that this choice will lead the player to
     * @param extraWarnings the extra content warnings that appear before the next Chapter begins
     * @return true if dynamic content warnings are disabled or the player chooses to continue; false otherwise
     */
    public boolean confirmContentWarnings(Chapter c, String extraWarnings) {
        if (!this.dynamicWarnings) {
            return true;
        }

        parser.printDialogueLine("[If you make this choice, you will encounter: " + extraWarnings + ".]", true);
        parser.printDialogueLine("[You might also encounter: " + c.getContentWarnings() + ".]", true);

        boolean confirm = this.parser.promptYesNo("[Are you sure you wish to proceed?]");
        parser.printDialogueLine("[You can turn dynamic content warnings off at any time with TOGGLE WARNINGS.]");
        System.out.println();
        return confirm;
    }

    /**
     * Warns the player of potential content warnings from committing to a choice that leads to a given Chapter, and allows them to change their mind
     * @param c the Chapter that this choice will lead the player to (should be the Grey)
     * @param ending the chapter ending the player would achieve to reach c
     * @param extraWarnings the extra content warnings that appear before the next Chapter begins
     * @return true if dynamic content warnings are disabled or the player chooses to continue; false otherwise
     */
    public boolean confirmContentWarnings(Chapter c, ChapterEnding ending, String extraWarnings) {
        if (!this.dynamicWarnings) {
            return true;
        }

        parser.printDialogueLine("[If you make this choice, you will encounter: " + extraWarnings + "; " + c.getContentWarnings(ending) + ".]", true);
        boolean confirm = this.parser.promptYesNo("[Are you sure you wish to proceed?]");
        parser.printDialogueLine("[You can turn dynamic content warnings off at any time with TOGGLE WARNINGS.]");
        System.out.println();
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
        if (!this.dynamicWarnings) {
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
            case "settings":
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
        System.out.println();
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
        IOHandler.wrapPrintln("General CWs: death; murder; suicide; verbal abuse; gaslighting; described gore; mutilation, disembowelment; loss of self; cosmic horror; existential horror; being eaten alive; suffocation; derealisation; forced suicide; loss of bodily autonomy; starvation; unreality; body horror; forced self-mutilation; self-degloving; flaying; self-immolation; drowning; burning to death; loss of control; dismemberment; self-decapitation; memory loss");
    }

    /**
     * Displays content warnings for each Chapter in the game
     */
    public void showByChapterWarnings() {
        boolean breakLoop = false;
        String s = "";
        
        for (Chapter c : Chapter.values()) {
            switch (c) {
                case CH1: continue;

                case MUTUALLYASSURED:
                    breakLoop = true;
                    break;

                case ADVERSARY:
                    s += "----- Possible content warnings for Chapter II -----";
                    s += "\n  - " + c.getTitle();
                    break;
                case NEEDLE:
                    s += "\n\n----- Possible content warnings for Chapter III -----";
                    s += "\n  - " + c.getTitle();
                    break;

                case ARMSRACE:
                    s += "\n  - " + c.getTitle() + " & Mutually Assured Destruction";
                    break;
                case NOWAYOUT:
                    s += "\n  - " + c.getTitle() + " & " + Chapter.EMPTYCUP.getFullTitle();
                    break;

                default: s += "\n  - " + c.getTitle();
            }

            if (breakLoop) break;

            if (c.hasContentWarnings()) {
                s += ": " + c.getContentWarnings();
            }
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
                case RAZOR:
                case ARMSRACE:
                case NOWAYOUT:
                case MUTUALLYASSURED:
                case EMPTYCUP:
                case STRANGER:
                case CLARITY:
                case GREY:
                    IOHandler.wrapPrintln(c.getFullTitle() + " contains: " + c.getContentWarnings(prevEnding));
                    break;
                default:
                    IOHandler.wrapPrintln(c.getFullTitle() + " may contain: " + c.getContentWarnings(prevEnding));
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
                case RAZOR:
                case ARMSRACE:
                case NOWAYOUT:
                case MUTUALLYASSURED:
                case EMPTYCUP:
                case STRANGER:
                case CLARITY:
                case GREY:
                    IOHandler.wrapPrintln(c.getFullTitle() + " contains: " + c.getContentWarnings());
                    break;
                default:
                    IOHandler.wrapPrintln(c.getFullTitle() + " may contain: " + c.getContentWarnings());
            }
        }
    }

    /**
     * Displays the current settings and allows the player to change them
     */
    public void settings() {
        // something is up with nested menus like this i think but idk what????
        boolean repeat = true;
        this.trueExclusiveMenu = true;
        
        while (repeat) {
            switch (parser.promptOptionsMenu(this.settingsMenu)) {
                case "warnings":
                    this.toggleAutoWarnings();
                    break;
                case "now playing":
                    this.toggleNowPlaying();
                    break;
                case "slow print":
                    this.toggleSlowPrint();
                    break;
                case "auto advance":
                    this.toggleAutoAdvance();
                    break;
                case "cancel":
                    repeat = false;
                    break;
                default: IOHandler.wrapPrintln("You have no other options.");
            }

            if (!repeat) break; // I know it *should* do this automatically, but it doesn't for some reason...?
        }

        this.trueExclusiveMenu = false;
    }

    /**
     * Toggles settings on or off
     * @param argument the setting to toggle on or off
     */
    public void toggle(String argument) {
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

            case "print speed":
            case "printing speed":
            case "dialogue speed":
            case "speed":
            case "slow":
            case "slow print":
            case "slow dialogue":
            case "instant print":
            case "instant dialogue":
                this.toggleSlowPrint();
                break;

            case "auto":
            case "auto advance":
            case "auto-advance":
            case "advance":
            case "auto dialogue":
                this.toggleAutoAdvance();
                break;

            case "":
                this.settings();
                break;

            default:
                this.showCommandHelp(Command.TOGGLE);
                break;
        }
    }

    /**
     * Toggles dynamic content warnings on or off
     */
    public void toggleAutoWarnings() {
        if (this.dynamicWarnings) {
            this.dynamicWarnings = false;
            this.settingsMenu.setDisplay("warnings", "[Turn dynamic content warnings ON.]");
            IOHandler.wrapPrintln("[Automatic content warnings have been disabled.]");
        } else {
            this.dynamicWarnings = true;
            this.settingsMenu.setDisplay("warnings", "[Turn dynamic content warnings OFF.]");
            IOHandler.wrapPrintln("[Automatic content warnings have been enabled.]");
        }
    }

    /**
     * Toggles soundtrack notifications on or off
     */
    public void toggleNowPlaying() {
        if (this.showNowPlaying) {
            this.showNowPlaying = false;
            this.settingsMenu.setDisplay("now playing", "[Turn soundtrack notifications ON.]");
            IOHandler.wrapPrintln("[Soundtrack notifications have been disabled.]");
        } else {
            this.showNowPlaying = true;
            this.settingsMenu.setDisplay("now playing", "[Turn soundtrack notifications OFF.]");
            IOHandler.wrapPrintln("[Soundtrack notifications have been enabled.]");
        }
    }

    /**
     * Toggles slow printing on or off
     */
    public void toggleSlowPrint() {
        if (this.globalSlowPrint) {
            this.globalSlowPrint = false;
            this.settingsMenu.setDisplay("slow print", "[Set print speed to SLOW.]");
            IOHandler.wrapPrintln("[Slow printing has been disabled.]");
        } else {
            this.globalSlowPrint = true;
            this.settingsMenu.setDisplay("slow print", "[Set print speed to INSTANT.]");
            IOHandler.wrapPrintln("[Slow printing has been enabled.]");
        }
    }

    /**
     * Toggles automatic dialogue advancement on or off
     */
    public void toggleAutoAdvance() {
        if (this.autoAdvance) {
            this.autoAdvance = false;
            this.settingsMenu.setDisplay("auto advance", "[Turn auto-advancing dialogue ON.]");
            IOHandler.wrapPrintln("[Dialogue will now automatically advance.]");
        } else {
            this.autoAdvance = true;
            this.settingsMenu.setDisplay("auto advance", "[Turn auto-advancing dialogue OFF.]");
            IOHandler.wrapPrintln("[Dialogue will no longer automatically advance.]");
            IOHandler.wrapPrintln("[Press enter to advance dialogue.]");
        }
    }

    public static void main(String[] args) {
        GameManager manager = new GameManager();
        manager.debugRunGame();
    }

}
