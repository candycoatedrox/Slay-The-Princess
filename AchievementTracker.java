import java.io.BufferedWriter;  // Import the File class
import java.io.File; // Import the Scanner class to read text files
import java.io.FileNotFoundException;
import java.io.FileWriter; // Import this class to write to a file
import java.io.IOException;  // Import this class to handle errors
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class AchievementTracker {
    
    private final GameManager manager;
    private final IOHandler parser;

    private Condition galleryUnlocked;
    private HashMap<Chapter, Condition> unlockedChapters; // UNLOCKEDCHAPTERS SEEMS TO BE WORKING INCORRECTLY -- LOOK INTO THAT!!!
    private IndexedLinkedHashMap<String, Achievement> achievements;
    private final ArrayList<String> generalAchievements;
    private final HashMap<Chapter, ArrayList<String>> chapterAchievements;

    private final int nAchievements;
    private final int nGeneralAchievements;
    private int nUnlockedAchievements = 0;
    private final GlobalInt nLockedAchievements;
    private final InverseCondition lockedRemaining;
    private int nHiddenAchievements = 0;
    private int nLockedHiddenAchievements;

    private final OptionsMenu achievementsMenu;
    private final OptionsMenu returnMenu;

    public static final int PAGELENGTH = 15;
    private final GlobalInt currentPage;
    private final InverseCondition notFirstPage;
    private final OptionsMenu pageMenu;

    private static final File ACHIEVEMENTLIST = new File("Saves", "AchievementList.txt"); // Static file; contains a simple list of all valid achievements
    private static final File TRACKER = new File("Saves", "UnlockedAchievements.txt"); // Dynamic file; stores the current state of achievements

    // --- CONSTRUCTOR ---

    /**
     * Constructor
     * @param manager the GameManager to link the AchievementTracker to
     * @param parser the IOHandler to link the AchievementTracker to
     */
    public AchievementTracker(GameManager manager, IOHandler parser) {
        this.manager = manager;
        this.parser = parser;
        this.galleryUnlocked = new Condition();
        //System.out.println("Gallery unlocked: " + this.galleryUnlocked);
        this.achievements = new IndexedLinkedHashMap<>();
        this.generalAchievements = new ArrayList<>();
        this.chapterAchievements = new HashMap<>();
        this.nLockedAchievements = new GlobalInt();
        this.lockedRemaining = new InverseCondition(new NumCondition(this.nLockedAchievements, 0));

        this.unlockedChapters = new HashMap<>();
        this.achievementsMenu = new OptionsMenu(true);
        this.pageMenu = new OptionsMenu(true);
        this.returnMenu = new OptionsMenu(true);
        
        achievementsMenu.add(new Option(this.manager, "locked", "[View locked achievements.]", 0, this.lockedRemaining));
        achievementsMenu.add(new Option(this.manager, "general", "General", 0));
        for (Chapter c : Chapter.GALLERYCHAPTERS) {
            unlockedChapters.put(c, new Condition());
            //System.out.println(c + " unlocked: " + unlockedChapters.get(c) + "; shown in menu: " + new OrCondition(unlockedChapters.get(c), this.galleryUnlocked));
            chapterAchievements.put(c, new ArrayList<>());
            achievementsMenu.add(new Option(this.manager, c.getID(), unlockedChapters.get(c).getInverse(), c.galleryHintLocked(), 0, new OrCondition(unlockedChapters.get(c), this.galleryUnlocked)));
        }
        achievementsMenu.add(new Option(this.manager, "reset", "[Reset gallery.]", 0));

        this.currentPage = new GlobalInt();
        this.notFirstPage = new InverseCondition(new NumCondition(this.currentPage, 0));
        pageMenu.add(new Option(this.manager, "first", "[First page.]", 0, this.notFirstPage));
        pageMenu.add(new Option(this.manager, "prev", "[Previous page.]", 0, this.notFirstPage));
        pageMenu.add(new Option(this.manager, "next", "[Next page.]", 0));
        pageMenu.add(new Option(this.manager, "last", "[Last page.]", 0));

        Option returnOption = new Option(this.manager, "return", "[Return.]", 0);
        achievementsMenu.add(returnOption);
        pageMenu.add(returnOption);
        returnMenu.add(returnOption);

        this.initializeAchievements();
        this.readTracker(true);

        this.nAchievements = achievements.size();
        this.nGeneralAchievements = generalAchievements.size();
    }

    /**
     * Initializes the list of achievements based on AchievementList.txt
     */
    private void initializeAchievements() {
        Chapter tempChapter;
        Chapter currentChapter = null; // File MUST list general achievements before any chapter-specific achievements!!!
        String lineContent;
        String[] split;
        String content;
        String id = "";
        String[] hintSplit;
        String hint;
        boolean hidden;
        boolean message;

        try (Scanner listReader = new Scanner(ACHIEVEMENTLIST);) {
            while (listReader.hasNextLine()) {
                lineContent = listReader.nextLine();

                if (!lineContent.isEmpty() && !lineContent.startsWith("//")) {
                    if (lineContent.startsWith("--- ")) { /// Indicates new Chapter
                        tempChapter = Chapter.getChapterFromTitle(lineContent.substring(4));
                        if (tempChapter.hasGallery()) currentChapter = tempChapter;
                    } else {
                        split = lineContent.split(" // "); // Comments are ignored
                        content = split[0];
                        split = content.split(" / ");
                        id = split[0];

                        if (this.achievementExists(id)) {
                            System.out.println("[DEBUG: Duplicate achievement " + id + "]");
                        } else {
                            hint = "";
                            hidden = false;
                            message = true;
                            for (int i = 3; i < split.length; i++) {
                                if (split[i].startsWith("hint ")) {
                                    hintSplit = split[i].split(" ", 2);
                                    hint = hintSplit[1];
                                } else {
                                    switch (split[i]) {
                                        case "samehint":
                                            hint = split[2];
                                            break;

                                        case "hidden":
                                            this.nHiddenAchievements += 1;
                                            hidden = true;
                                            break;

                                        case "silent":
                                            message = false;
                                            break;
                                    }
                                }
                            }

                            achievements.put(id, new Achievement(id, currentChapter, hidden, message, split[1], split[2], hint));

                            if (currentChapter == null) {
                                generalAchievements.add(id);
                            } else {
                                chapterAchievements.get(currentChapter).add(id);
                            }
                        }
                    }
                }
            }

            this.nLockedAchievements.set(this.nAchievements);
            this.nLockedHiddenAchievements = this.nHiddenAchievements;
        } catch (FileNotFoundException e) {
            throw new RuntimeException("No achievement list found");
        } catch (IndexOutOfBoundsException e) {
            throw new RuntimeException("Invalid achievement" + id + " (missing name and/or description)");
        }
    }

    // --- ACCESSORS & MANIPULATORS ---

    /**
     * Accessor for galleryUnlocked
     * @return whether or not the Achievement Gallery is fully unlocked
     */
    public boolean galleryUnlocked() {
        return galleryUnlocked.check();
    }

    /**
     * Permanently unlocks the gallery
     */
    public void unlockGallery() {
        galleryUnlocked.set();
    }

    /**
     * Checks whether an achievement with the given ID exists
     * @param id the ID to check
     * @return true if an achievement with the given ID exists; false otherwise
     */
    public boolean achievementExists(String id) {
        return achievements.containsKey(id);
    }

    /**
     * Checks whether a given achievement has been unlocked
     * @param achievement the name of the achievement
     * @return whether the given achievement has been unlocked
     */
    public boolean check(String achievement) {
        if (this.achievementExists(achievement)) {
            return achievements.get(achievement).isUnlocked();
        } else {
            throw new IllegalArgumentException("Achievement " + achievement + "does not exist");
        }
    }

    /**
     * Unlock a given achievement
     * @param id the ID of the achievement to unlock
     */
    public void unlock(String id) {
        if (this.achievementExists(id)) {
            Achievement achievement = achievements.get(id);
            if (!achievement.isUnlocked()) {
                this.nUnlockedAchievements += 1;
                nLockedAchievements.decrement();
                if (achievement.isHidden()) this.nLockedHiddenAchievements -= 1;
                achievement.unlock();

                if (achievement.showsUnlockMessage()) {
                    System.out.println();
                    parser.printDialogueLine("[ ACHIEVEMENT UNLOCKED: " + achievement.getName() + " ]", true);
                    if (manager.globalSlowPrint() && !manager.autoAdvance()) GameManager.pause(1000);
                    parser.printDialogueLine("[ " + achievement.getDescription() + " ]", true);
                    if (manager.autoAdvance()) GameManager.pause(1000);
                    parser.waitForInput();
                    System.out.println();
                }

                if (this.nUnlockedAchievements == this.nAchievements - 1) this.unlock("galleryComplete");
            }
        } else {
            throw new IllegalArgumentException("Achievement \"" + id + "\" does not exist");
        }
    }

    /**
     * Checks whether a given Chapter's achievement list has been unlocked
     * @param c the Chapter to check
     * @return whether the given Chapter's achievement list has been unlocked
     */
    public boolean check(Chapter c) {
        c = getGalleryChapter(c);
        return unlockedChapters.get(c).check();
    }

    /**
     * Unlock the achievement list for a given Chapter when it is visited for the first time
     * @param c the Chapter to unlock
     */
    public void unlock(Chapter c) {
        c = getGalleryChapter(c);
        unlockedChapters.get(c).set();
        
        if (c.galleryHintUnlocked().isEmpty()) {
            achievementsMenu.setDisplay(c.getID(), c.toString());
        } else {
            achievementsMenu.setDisplay(c.getID(), c.toString() + " - " + c.galleryHintUnlocked());
        }
    }

    /**
     * Lock the achievement list for a given Chapter during a reset
     * @param c the Chapter to lock
     */
    public void lock(Chapter c) {
        c = getGalleryChapter(c);

        unlockedChapters.get(c).set(false);
        achievementsMenu.setDisplay(c.getID(), c.galleryHintLocked());
    }

    /**
     * Accessor for nAchievements
     * @return the number of total achievements in the game
     */
    public int nAchievements() {
        return this.nAchievements;
    }

    /**
     * Accessor for nGeneralAchievements
     * @return the total number of non-chapter-specific achievements
     */
    public int nGeneralAchievements() {
        return this.nGeneralAchievements;
    }

    /**
     * Returns the total number of achievements associated with a given Chapter
     * @param c the Chapter to check
     * @return the total number of achievements associated with the given Chapter
     */
    public int nChapterAchievements(Chapter c) {
        c = getGalleryChapter(c);
        return this.chapterAchievements.get(c).size();
    }

    /**
     * Accessor for nHiddenAchievements
     * @return the number of hidden achievements in the game
     */
    public int nHiddenAchievements() {
        return this.nHiddenAchievements;
    }

    /**
     * Accessor for nLockedAchievements
     * @return the GlobalInt representing the number of currently locked achievements
     */
    public GlobalInt nLockedAchievements() {
        return this.nLockedAchievements;
    }

    /**
     * Accessor for lockedRemaining
     * @return a condition tracking whether there are remaining locked achievements
     */
    public InverseCondition getLockedRemaining() {
        return this.lockedRemaining;
    }

    /**
     * Accessor for nLockedHiddenAchievements
     * @return the number of currently locked hidden achievements
     */
    public int nLockedHiddenAchievements() {
        return this.nLockedHiddenAchievements;
    }

    /**
     * Accessor for nUnlockedAchievements
     * @return the number of currently unlocked achievements
     */
    public int nUnlockedAchievements() {
        return this.nUnlockedAchievements;
    }

    /**
     * Accessor for achievementsMenu
     * @return the Achievement Gallery menu
     */
    public OptionsMenu achievementsMenu() {
        return this.achievementsMenu;
    }

    /**
     * Accessor for returnMenu
     * @return a menu with a single option to "return"
     */
    public OptionsMenu returnMenu() {
        return this.returnMenu;
    }

    /**
     * Accessor for currentPage
     * @return the GlobalInt representing the current page number
     */
    public GlobalInt getCurrentPage() {
        return this.currentPage;
    }

    /**
     * Accessor for pageMenu
     * @return a menu for navigating between pages of a list
     */
    public OptionsMenu pageMenu() {
        return this.pageMenu;
    }

    /**
     * Returns a list of all currently unlocked achievements in a given list of achievement IDs
     * @param ids the list of achievement IDs to check
     * @return all currently unlocked achievements referenced in ids
     */
    private ArrayList<Achievement> getUnlockedAchievements(ArrayList<String> ids) {
        ArrayList<Achievement> unlocked = new ArrayList<>();
        Achievement a;
        for (String id : ids) {
            if (this.achievementExists(id)) {
                a = achievements.get(id);
                if (a.isUnlocked()) unlocked.add(a);
            }
        }
        return unlocked;
    }

    /**
     * Returns an ArrayList of all unlocked non-chapter-specific achievements
     * @return all unlocked non-chapter-specific achievements
     */
    public ArrayList<Achievement> getUnlockedGeneralAchievements() {
        return this.getUnlockedAchievements(this.generalAchievements);
    }

    /**
     * Returns an ArrayList of all unlocked achievements from a given Chapter
     * @param c the Chapter to retrieve achievements from
     * @return all unlocked achievements from c
     */
    public ArrayList<Achievement> getUnlockedChapterAchievements(Chapter c) {
        c = getGalleryChapter(c);
        return this.getUnlockedAchievements(this.chapterAchievements.get(c));
    }

    /**
     * Returns a list of all currently unlocked achievements
     * @return all currently unlocked achievements
     */
    private ArrayList<Achievement> getUnlockedAchievements() {
        ArrayList<Achievement> unlocked = new ArrayList<>();
        for (Achievement a : achievements.valueList()) {
            if (a.isUnlocked()) unlocked.add(a);
        }
        return unlocked;
    }

    /**
     * Returns a list of all currently locked (non-hidden) achievements
     * @return all currently locked (non-hidden) achievements
     */
    public ArrayList<Achievement> getLockedAchievements() {
        ArrayList<Achievement> locked = new ArrayList<>();
        for (Achievement a : achievements.valueList()) {
            if (!a.isUnlocked() && !a.isHidden()) locked.add(a);
        }
        return locked;
    }

    // --- TRACKER MANAGEMENT ---

    /**
     * Reset all achievements and unlocked chapters
     */
    public void reset() {
        this.nUnlockedAchievements = 0;
        this.nLockedAchievements.set(this.nAchievements);
        this.nLockedHiddenAchievements = this.nHiddenAchievements;
        for (Chapter c : Chapter.values()) this.lock(c);
        for (Achievement a : achievements.values()) a.lock();
        TRACKER.delete();
    }

    /**
     * Unlock all Chapters and achievements listed in UnlockedAchievements.txt
     * @param fromInitialize whether or not this method was called from the initialization of this tracker
     */
    private void readTracker(boolean fromInitialize) {
        try (Scanner tracker = new Scanner(TRACKER)) {
            Chapter currentChapter;
            String lineContent;

            while (tracker.hasNextLine()) {
                lineContent = tracker.nextLine();
                if (!lineContent.isEmpty()) {
                    if (lineContent.equals("GALLERYUNLOCKED")) {
                        galleryUnlocked.set();
                    } else if (lineContent.startsWith("CHAPTER ")) {
                        currentChapter = Chapter.getChapter(lineContent.substring(8));
                        if (currentChapter == null) {
                            IOHandler.wrapPrintln("[DEBUG: Invalid chapter " + lineContent.substring(8) + " listed in UnlockAchievements.txt]");
                        } else {
                            this.unlock(currentChapter);
                        }
                    } else {
                        this.unlock(lineContent);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            if (!fromInitialize) {
                for (Chapter c : Chapter.values()) unlockedChapters.get(c).set(false);
                for (Achievement a : achievements.values()) a.lock();
            }
        }
    }

    /**
     * Updates UnlockedAchievements.txt
     */
    public void updateTracker() {
        try (BufferedWriter tracker = new BufferedWriter(new FileWriter(TRACKER));) {
            if (galleryUnlocked.check()) {
                tracker.write("GALLERYUNLOCKED");
                tracker.newLine();
            }

            for (Chapter c : Chapter.GALLERYCHAPTERS) {
                if (unlockedChapters.get(c).check()) {
                    tracker.write("CHAPTER " + c.getID());
                    tracker.newLine();
                }
            }

            for (Achievement a : achievements.valueList()) {
                if (a.isUnlocked()) {
                    tracker.write(a.getID());
                    tracker.newLine();
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    // --- GALLERY ---

    /**
     * Shows the player the Achievement Gallery
     */
    public void showGallery() {
        boolean repeat = true;
        String choice;

        // MENU IS BROKEN?
        System.out.println("[DEBUG: metaMenuActive is " + manager.metaMenuActive() + "]");

        while (repeat) {
            System.out.println();
            IOHandler.wrapPrintln("--- THE ACHIEVEMENT GALLERY ---");
            IOHandler.wrapPrintln("You have unlocked " + this.nUnlockedAchievements + "/" + this.nAchievements + " achievements.");

            choice = parser.promptOptionsMenu(achievementsMenu);
            switch (choice) {
                case "locked":
                    this.showLockedAchievements();
                    break;

                case "general":
                    this.printGeneralAchievementsList();
                    parser.promptOptionsMenu(returnMenu);
                    break;

                case "reset":
                    manager.resetAchievements();
                    break;

                case "return":
                    repeat = false;
                    break;

                default:
                    this.printChapterAchievementsList(Chapter.getChapter(choice));
                    parser.promptOptionsMenu(returnMenu);
            }
        }
    }

    /**
     * Shows a list of all locked (non-hidden) achievements, split into pages
     */
    private void showLockedAchievements() {
        ArrayList<Achievement> lockedAchievements = this.getLockedAchievements();
        int nVisibleAchievements = lockedAchievements.size();

        if (nVisibleAchievements == 0) {
            System.out.println();
            IOHandler.wrapPrintln("--- Locked Achievements ---");
            System.out.println();

            if (!lockedRemaining.check()) {
                IOHandler.wrapPrintln("No locked achievements to show!");
            } else {
                IOHandler.wrapPrintln("No locked achievements to show. " + this.nLockedHiddenAchievements + " hidden achievements not shown.");
            }

            parser.promptOptionsMenu(returnMenu);
        } else {
            int nPages = nVisibleAchievements / PAGELENGTH;

            if (nPages == 0) {
                System.out.println();
                IOHandler.wrapPrintln("--- Locked Achievements ---");
                System.out.println();

                for (int i = 0; i < nVisibleAchievements; i++) {
                    IOHandler.wrapPrintln("  (" + (i+1) + ".) " + lockedAchievements.get(i));
                }

                System.out.println();
                IOHandler.wrapPrintln("Showing achievements 1-" + nVisibleAchievements + " of " + nVisibleAchievements + ". " + this.nLockedHiddenAchievements + " hidden achievements not shown.");
            } else {
                if (nVisibleAchievements % PAGELENGTH != 0) nPages += 1;
                NumCondition notLastPage = new NumCondition(this.currentPage, -1, nPages - 1);

                int firstShown;
                int lastShown;
                currentPage.set(0);
                pageMenu.get("next").setConditions(notLastPage);
                pageMenu.get("last").setConditions(notLastPage);

                boolean repeat = true;
                String choice;
                while (repeat) {
                    firstShown = (currentPage.check() * PAGELENGTH) + 1;
                    lastShown = (notLastPage.check()) ? firstShown + PAGELENGTH - 1 : nVisibleAchievements - 1;

                    System.out.println();
                    IOHandler.wrapPrintln("--- Locked Achievements ---");
                    System.out.println();

                    for (int i = firstShown - 1; i < lastShown; i++) {
                        IOHandler.wrapPrintln("  (" + (i+1) + ".) " + lockedAchievements.get(i));
                    }

                    System.out.println();
                    if (this.nLockedHiddenAchievements == 0) {
                        IOHandler.wrapPrintln("Showing achievements " + firstShown + "-" + lastShown + " of " + nVisibleAchievements + ".");
                    } else {
                        IOHandler.wrapPrintln("Showing achievements " + firstShown + "-" + lastShown + " of " + nVisibleAchievements + ". " + this.nLockedHiddenAchievements + " hidden achievements not shown.");
                    }

                    choice = parser.promptOptionsMenu(pageMenu);
                    switch (choice) {
                        case "first":
                            currentPage.set(0);
                            break;

                        case "prev":
                            currentPage.decrement();
                            break;

                        case "next":
                            currentPage.increment();
                            break;

                        case "last":
                            currentPage.set(nPages - 1);
                            break;

                        case "return":
                            repeat = false;
                            break;
                    }
                }
            }
        }
    }

    /**
     * Prints all unlocked general achievements
     */
    public void printGeneralAchievementsList() {
        System.out.println();
        IOHandler.wrapPrintln("--- General ---");
        System.out.println();

        ArrayList<Achievement> unlockedAchievements = this.getUnlockedGeneralAchievements();
        if (unlockedAchievements.isEmpty()) {
            IOHandler.wrapPrintln("No unlocked achievements to show.");
        } else {
            for (int i = 0; i < unlockedAchievements.size(); i++) {
                IOHandler.wrapPrintln("  (" + (i+1) + ".) " + unlockedAchievements.get(i));
            }
        }

        System.out.println();
        IOHandler.wrapPrintln("You have unlocked " + unlockedAchievements.size() + "/" + this.nGeneralAchievements + " general achievements.");
    }

    /**
     * Prints all unlocked achievements from a given Chapter
     * @param c the Chapter to print achievements from
     */
    public void printChapterAchievementsList(Chapter c) {
        c = getGalleryChapter(c);

        System.out.println();
        IOHandler.wrapPrintln("--- " + c.toString() + " ---");
        IOHandler.wrapPrintln(c.galleryHintUnlocked());
        System.out.println();

        ArrayList<Achievement> unlockedAchievements = this.getUnlockedChapterAchievements(c);
        if (unlockedAchievements.isEmpty()) {
            IOHandler.wrapPrintln("No unlocked achievements to show.");
        } else {
            for (int i = 0; i < unlockedAchievements.size(); i++) {
                IOHandler.wrapPrintln("  (" + (i+1) + ".) " + unlockedAchievements.get(i));
            }
        }

        System.out.println();
        IOHandler.wrapPrintln("You have unlocked " + unlockedAchievements.size() + "/" + this.nChapterAchievements(c) + " achievements from this Chapter.");
    }

    // --- UTILITY ---

    /**
     * Checks if the given Chapter has its own gallery, and returns its corresponding Gallery chapter if not
     * @param c the Chapter to check
     * @return Chapter.RAZOR if the given Chapter is one of Razor's Chapter IIIs or IVs; c otherwise
     */
    public static Chapter getGalleryChapter(Chapter c) {
        switch (c) {
            case ARMSRACE:
            case NOWAYOUT:
            case MUTUALLYASSURED:
            case EMPTYCUP: return Chapter.RAZOR;
            default: return c;
        }
    }

}
