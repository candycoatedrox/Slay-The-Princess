import java.io.File;  // Import the File class
import java.util.Scanner; // Import the Scanner class to read text files
import java.io.FileWriter;
import java.io.BufferedWriter; // Import this class to write to a file
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.io.IOException;

public class AchievementTracker {
    
    private final GameManager manager;
    private final IOHandler parser;

    private final IndexedLinkedHashMap<String, Achievement> achievements;

    private static final File ACHIEVEMENTLIST = new File("Saves", "AchievementList.txt"); // Static file; contains a simple list of all valid achievements
    private static final File TRACKER = new File("Saves", "UnlockedAchievements.txt"); // Dynamic file; tracks the current state of achievements

    // --- CONSTRUCTOR ---

    /**
     * Constructor
     */
    public AchievementTracker(GameManager manager, IOHandler parser) {
        this.manager = manager;
        this.parser = parser;
        this.achievements = new IndexedLinkedHashMap<>();

        this.initialize();
        this.readTracker(true);
    }

    /**
     * Initializes the list of achievements based on AchievementList.txt
     */
    private void initialize() {
        String id = "";

        try (Scanner listReader = new Scanner(ACHIEVEMENTLIST);) {
            Chapter currentChapter = Chapter.CH1;
            String lineContent;
            String[] split;
            String content;

            while (listReader.hasNextLine()) {
                lineContent = listReader.nextLine();

                if (!lineContent.isEmpty() && !lineContent.startsWith("//")) {
                    if (lineContent.startsWith("--- ")) { /// indicates new Chapter
                        currentChapter = Chapter.getChapter(lineContent.substring(4));
                    } else {
                        split = lineContent.split(" // ");
                        content = split[0];
                        split = content.split(" / ");
                        id = split[0];

                        if (achievements.containsKey(id)) {
                            System.out.println("[DEBUG: Duplicate achievement " + id + "]");
                        } else {
                            achievements.put(id, new Achievement(id, currentChapter, split[1], split[2]));
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("No achievement list found");
        } catch (IndexOutOfBoundsException e) {
            throw new RuntimeException("Invalid achievement" + id + " (missing name and/or description)");
        }
    }

    // --- ACCESSORS & MANIPULATORS ---

    /**
     * Checks whether a given achievement has been unlocked
     * @param achievement the name of the achievement
     * @return whether the given achievement has been unlocked
     */
    public boolean check(String achievement) {
        if (achievements.containsKey(achievement)) {
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
        if (achievements.containsKey(id)) {
            Achievement achievement = achievements.get(id);
            if (!achievement.isUnlocked()) {
                achievement.unlock();

                System.out.println();
                parser.printDialogueLine("[ ACHIEVEMENT UNLOCKED: " + achievement.getName() + " ]");
                if (manager.globalSlowPrint()) Script.pause(1000);
                parser.printDialogueLine("[ " + achievement.getDescription() + " ]");
                System.out.println();
            }
        } else {
            throw new IllegalArgumentException("Achievement " + id + "does not exist");
        }
    }

    // --- TRACKER MANAGEMENT ---

    /**
     * Reset all achievements
     */
    public void reset() {
        TRACKER.delete();
        for (Achievement a : achievements.values()) {
            a.lock();
        }
    }

    /**
     * Unlock all achievements listed in UnlockedAchievements.txt
     * @param fromInitialize whether or not this method was called from the initialization of this tracker
     */
    private void readTracker(boolean fromInitialize) {
        try (Scanner tracker = new Scanner(TRACKER)) {
            String achievement;
            while (tracker.hasNextLine()) {
                achievement = tracker.nextLine();
                if (!achievement.isEmpty()) this.unlock(achievement);
            }
        } catch (FileNotFoundException e) {
            if (!fromInitialize) {
                for (Achievement a : achievements.values()) {
                    a.lock();
                }
            }
        }
    }

    /**
     * Updates UnlockedAchievements.txt
     */
    public void updateTracker() {
        try (BufferedWriter tracker = new BufferedWriter(new FileWriter(TRACKER));) {
            for (Achievement a : achievements.values()) {
                if (a.isUnlocked()) {
                    tracker.write(a.getName());
                    tracker.newLine();
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

}
