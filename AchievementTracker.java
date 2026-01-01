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

    private final Condition galleryUnlocked;
    private final HashMap<Chapter, Condition> unlockedChapters; // UNLOCKEDCHAPTERS SEEMS TO BE WORKING INCORRECTLY -- LOOK INTO THAT!!!
    private final IndexedLinkedHashMap<String, Achievement> achievements;
    private final HashMap<Chapter, ArrayList<String>> chapterAchievements;
    private final ArrayList<String> generalAchievements;

    private final int nAchievements;
    private int nUnlockedAchievements = 0;
    private final GlobalInt nLockedAchievements;
    private final InverseCondition lockedRemaining;
    private int nHiddenAchievements = 0;
    private int nLockedHiddenAchievements;

    private final OptionsMenu achievementsMenu;
    private final OptionsMenu returnMenu;

    private static final int PAGELENGTH = 10;
    private final GlobalInt currentPage;
    private final InverseCondition notFirstPage;
    private final OptionsMenu pageMenu;

    private static final File ACHIEVEMENTLIST = new File("Saves", "AchievementList.txt"); // Static file; contains a simple list of all valid achievements
    private static final File TRACKER = new File("Saves", "UnlockedAchievements.txt"); // Dynamic file; tracks the current state of achievements

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
        this.achievements = new IndexedLinkedHashMap<>();
        this.chapterAchievements = new HashMap<>();
        this.generalAchievements = new ArrayList<>();
        this.nLockedAchievements = new GlobalInt();
        this.lockedRemaining = new InverseCondition(new NumCondition(this.nLockedAchievements, 0));

        this.unlockedChapters = new HashMap<>();
        this.achievementsMenu = new OptionsMenu(true);
        this.pageMenu = new OptionsMenu(true);
        this.returnMenu = new OptionsMenu(true);
        
        achievementsMenu.add(new Option(this.manager, "locked", "[View locked achievements.]", 0, this.lockedRemaining));
        achievementsMenu.add(new Option(this.manager, "general", "General.", 0));
        for (Chapter c : Chapter.GALLERYCHAPTERS) {
            unlockedChapters.put(c, new Condition());
            chapterAchievements.put(c, new ArrayList<>());
            achievementsMenu.add(new Option(this.manager, c.getID(), unlockedChapters.get(c).getInverse(), c.galleryHintLocked(), 0, new OrCondition(unlockedChapters.get(c), this.galleryUnlocked)));
        }

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
                            hidden = false;
                            message = true;
                            for (int i = 3; i < split.length; i++) {
                                switch (split[i]) {
                                    case "hidden":
                                        this.nHiddenAchievements += 1;
                                        hidden = true;
                                        break;

                                    case "silent":
                                        message = false;
                                        break;
                                }
                            }

                            achievements.put(id, new Achievement(id, currentChapter, hidden, message, split[1], split[2]));

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
     * Checks whether a given Chapter's achievement list has been unlocked
     * @param c the Chapter to check
     * @return whether the given Chapter's achievement list has been unlocked
     */
    public boolean check(Chapter c) {
        switch (c) {
            case ARMSRACE:
            case NOWAYOUT:
            case MUTUALLYASSURED:
            case EMPTYCUP: return unlockedChapters.get(Chapter.RAZOR).check();
            default: return unlockedChapters.get(c).check();
        }
    }

    /**
     * Returns a list of all currently unlocked achievements
     * @return all currently unlocked achievements
     */
    private ArrayList<Achievement> unlockedAchievements() {
        ArrayList<Achievement> unlocked = new ArrayList<>();
        for (Achievement a : achievements.valueList()) {
            if (a.isUnlocked()) unlocked.add(a);
        }
        return unlocked;
    }

    /**
     * Returns a list of all currently unlocked achievements in a given list of achievement IDs
     * @param ids the list of achievement IDs to check
     * @return all currently unlocked achievements referenced in ids
     */
    private ArrayList<Achievement> unlockedAchievements(ArrayList<String> ids) {
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
     * Returns a list of all currently locked (non-hidden) achievements
     * @return all currently locked (non-hidden) achievements
     */
    private ArrayList<Achievement> lockedAchievements() {
        ArrayList<Achievement> locked = new ArrayList<>();
        for (Achievement a : achievements.valueList()) {
            if (!a.isUnlocked() && !a.isHidden()) locked.add(a);
        }
        return locked;
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
                    if (manager.globalSlowPrint()) Script.pause(1000);
                    parser.printDialogueLine("[ " + achievement.getDescription() + " ]");
                    System.out.println();
                    if (manager.globalSlowPrint()) Script.pause(1000);
                }

                if (this.nUnlockedAchievements == this.nAchievements - 1) this.unlock("galleryComplete");
            }
        } else {
            throw new IllegalArgumentException("Achievement " + id + "does not exist");
        }
    }

    /**
     * Unlock the achievement list for a given Chapter when it is visited for the first time
     * @param c the Chapter to unlock
     */
    public void unlock(Chapter c) {
        switch (c) {
            case ARMSRACE:
            case NOWAYOUT:
            case MUTUALLYASSURED:
            case EMPTYCUP: c = Chapter.RAZOR;
        }

        unlockedChapters.get(c).set();
        achievementsMenu.setDisplay(c.getID(), c.getTitle() + " - " + c.galleryHintUnlocked());
    }

    /**
     * Lock the achievement list for a given Chapter during a reset
     * @param c the Chapter to lock
     */
    public void lock(Chapter c) {
        switch (c) {
            case ARMSRACE:
            case NOWAYOUT:
            case MUTUALLYASSURED:
            case EMPTYCUP: c = Chapter.RAZOR;
        }

        unlockedChapters.get(c).set(false);
        achievementsMenu.setDisplay(c.getID(), c.galleryHintLocked());
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
                    tracker.write(a.getName());
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

        while (repeat) {
            IOHandler.wrapPrintln("You have unlocked " + this.nUnlockedAchievements + "/" + this.nAchievements + " achievements.");
            choice = parser.promptOptionsMenu(achievementsMenu);
            switch (choice) {
                case "locked":
                    this.showLockedAchievements();
                    break;

                case "general":
                    this.printGeneralAchievementsList();
                    break;

                case "return":
                    repeat = false;
                    break;

                default: this.printChapterAchievementsList(Chapter.getChapterFromTitle(choice));
            }

            parser.promptOptionsMenu(this.returnMenu);
        }
    }

    /**
     * Shows a list of all locked (non-hidden) achievements, split into pages
     */
    private void showLockedAchievements() {
        ArrayList<Achievement> lockedAchievements = this.lockedAchievements();
        int nVisibleAchievements = lockedAchievements.size();

        if (nVisibleAchievements == 0) {
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
                    lastShown = (notLastPage.check()) ? firstShown + PAGELENGTH - 1 : nVisibleAchievements;

                    IOHandler.wrapPrintln("--- Locked Achievements ---");
                    System.out.println();

                    for (int i = firstShown - 1; i < lastShown - 1; i++) {
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
    private void printGeneralAchievementsList() {
        IOHandler.wrapPrintln("--- General ---");
        System.out.println();

        ArrayList<Achievement> unlockedAchievements = this.unlockedAchievements(this.generalAchievements);
        if (unlockedAchievements.isEmpty()) {
            IOHandler.wrapPrintln("No unlocked achievements to show.");
        } else {
            for (int i = 0; i < unlockedAchievements.size(); i++) {
                IOHandler.wrapPrintln("  (" + (i+1) + ".) " + unlockedAchievements.get(i));
            }
        }

        parser.promptOptionsMenu(returnMenu);
    }

    /**
     * Prints all unlocked achievements from a given Chapter
     * @param c the Chapter to print achievements from
     */
    private void printChapterAchievementsList(Chapter c) {
        switch (c) {
            case ARMSRACE:
            case NOWAYOUT:
            case MUTUALLYASSURED:
            case EMPTYCUP: c = Chapter.RAZOR;
        }

        IOHandler.wrapPrintln("--- " + c.getTitle() + " ---");
        IOHandler.wrapPrintln(c.galleryHintUnlocked());
        System.out.println();

        ArrayList<Achievement> unlockedAchievements = this.unlockedAchievements(chapterAchievements.get(c));
        if (unlockedAchievements.isEmpty()) {
            IOHandler.wrapPrintln("No unlocked achievements to show.");
        } else {
            for (int i = 0; i < unlockedAchievements.size(); i++) {
                IOHandler.wrapPrintln("  (" + (i+1) + ".) " + unlockedAchievements.get(i));
            }
        }

        parser.promptOptionsMenu(returnMenu);
    }

}
