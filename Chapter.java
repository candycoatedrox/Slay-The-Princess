import java.io.File;  // Import the File class
import java.util.Arrays;

public enum Chapter {
    // Chapter I
    CH1(1, "ch1", "The Hero and the Princess", "", "Start the game.", "Chapter 1/Chapter1Shared"),

    // Special
    SPACESBETWEEN("intermission", "The Spaces Between", "Reach the mirror.", "Intermission/IntermissionShared"),
    ENDOFEVERYTHING("finale", "The End of Everything", "Reach the end of everything.", "Finale/FinaleOpening"),

    // Chapter II
    ADVERSARY(2, "adversary", "The Adversary", "The song we write in our blood.", "Meet your equal in combat.", "Routes/Adversary/AdversaryShared"),
    TOWER(2, "tower", "The Tower", "Doubt forces the hand of fealty.", "Die, pathetically, to an abrasive prisoner.", "Routes/Tower/TowerShared"),
    SPECTRE(2, "spectre", "The Spectre", "The remains of violence free from hesitation.", "Slay her without a moment's hesitation.", "Routes/Spectre/SpectreShared"),
    NIGHTMARE(2, "nightmare", "The Nightmare", "Fear locked away in the basement of your mind.", "Lock your fears away.", "Routes/Nightmare/NightmareShared"),
    RAZOR(2, "razor", "The Razor", "To look too closely is to redraw the lines.", "She isn't armed... is she?", "Routes/Razor/Razor2"),
    BEAST(2, "beast", "The Beast", "Softness cornered turns to viciousness.", "Fall to a feral captive.", "Routes/Beast/BeastShared"),
    WITCH(2, "witch", "The Witch", "Offering one hand, while concealing the other.", "Betray her, before she can betray you.", "Routes/Witch/WitchShared"),
    STRANGER(2, "stranger", "The Stranger", "A peek behind the curtains, likely far too soon.", "You can't know someone you've never met.", "Routes/Stranger/StrangerShared"),
    PRISONER(2, "prisoner", "The Prisoner", "Doubt breaks one shackle while forcing another.", "Free an abrasive prisoner.", "Routes/Prisoner/PrisonerShared"),
    DAMSEL(2, "damsel", "The Damsel", "Unquestioning commitment to the other.", "Free a gentle captive.", "Routes/Damsel/DamselShared"),

    // Chapter III
    NEEDLE(3, "needle", "The Eye of the Needle", "For those who dwell in caves, meaning lies beyond the shadows dancing on the walls.", "You need more space. A narrow cave is no place to fight, and it's no place to stay forever.", "Routes/Adversary/NeedleShared"),
    FURY(3, "fury", "The Fury", "An angel felled is a demon scorned.", "Angels and Demons are cut from the same cloth, and it's best not to deny a proud being.", "Routes/JOINT/Fury/FuryShared"),
    APOTHEOSIS(3, "apotheosis", "The Apotheosis", "To struggle and fail against the divine is to welcome it into your heart.", "One might resist the divine, but it is very hard to kill a god. There is no shame in failing.", "Routes/Tower/ApotheosisShared"),
    DRAGON(3, "dragon", "The Princess and the Dragon", "To excise another is to excise one's heart.", "Sometimes, when you cut something out of you, a piece of you leaves with it.", true, "Routes/Spectre/DragonShared"),
    WRAITH(3, "wraith", "The Wraith", "A broken doll, a spirit slain.", "Kill your worst dreams, or be killed by the ghost of your past.", "Routes/JOINT/Wraith/WraithShared"),
    CLARITY(3, "clarity", "The Moment of Clarity", "Bear witness to one's darkest fears.", "Better to linger with your nightmares than to let them run wild.", "Routes/Nightmare/MomentOfClarity"),
    ARMSRACE(3, "armsrace", "The Arms Race", "Routes/Razor/Razor3Shared"),
    NOWAYOUT(3, "nowayout", "No Way Out", "Routes/Razor/Razor3Shared"),
    DEN(3, "den", "The Den", "A creature's lair.", "There are other ways to die to nature than to let it swallow you whole.", "Routes/Beast/DenShared"),
    WILD(3, "wild", "The Wild", "Bodies fused. Where does one thing begin and another end?", "Become one with the Princess, in a very literal sense. May involve, but does not necessitate, being eaten.", "Routes/JOINT/Wild/WildShared"),
    THORN(3, "thorn", "The Thorn", "Redemption in the thicket of distrust.", "Sometimes the only way to break a vicious cycle is to put your heart on the line.", "Routes/Witch/ThornShared"),
    CAGE(3, "cage", "The Cage", "A vicious cycle framed in chain.", "When you've seen the worst sights the prison of the world can offer, you can always choose to walk away empty-handed.", "Routes/Prisoner/PrisonerShared"),
    GREY(3, "grey", "The Grey", "Feelings buried like knives in hearts.", "There are many reasons spirits may linger. The flames of passion. The flood of words left unspoken.", "Routes/JOINT/Grey/GreyShared"),
    HAPPY(3, "happy", "Epilogue", "Happily Ever After", "Everything you didn't know you wanted.", "You don't need the world for your happy ending.", "Routes/Damsel/HappyShared"),
    
    // Chapter IV
    MUTUALLYASSURED(4, "mutuallyassured", "Mutually Assured Destruction", "Routes/Razor/Razor4"),
    EMPTYCUP(4, "emptycup", "The Empty Cup", "Routes/Razor/Razor4");

    private final String id;
    private final int number;
    private String prefix;
    private final String title;
    private final boolean specialTitle;

    private final String galleryHintUnlocked;
    private final String galleryHintLocked;

    private final File scriptFile;

    public static final Chapter[] GALLERYCHAPTERS = {CH1, SPACESBETWEEN, ENDOFEVERYTHING, ADVERSARY, TOWER, SPECTRE, NIGHTMARE, RAZOR, BEAST, WITCH, STRANGER, PRISONER, DAMSEL, NEEDLE, FURY, APOTHEOSIS, DRAGON, WRAITH, CLARITY, DEN, WILD, THORN, CAGE, GREY, HAPPY};

    // --- CONSTRUCTORS ---

    /**
     * Constructor
     * @param number the Chapter number
     * @param id the internal ID for this Chapter
     * @param title the title of the Chapter
     * @param galleryHintUnlocked the description displayed in the gallery when this Chapter is unlocked
     * @param galleryHintLocked the hint displayed in the gallery when this Chapter is locked
     * @param specialTitle whether this Chapter has a special title (and thus should not display a title card at the start of the Chapter)
     * @param scriptDirectory the directory of the primary script for this Chapter
     */
    private Chapter(int number, String id, String title, String galleryHintUnlocked, String galleryHintLocked, boolean specialTitle, String scriptDirectory) {
        this.specialTitle = specialTitle;
        this.number = number;
        this.id = id;
        this.title = title;
        this.galleryHintUnlocked = title + galleryHintUnlocked;
        this.galleryHintLocked = galleryHintLocked;
        this.scriptFile = Script.getScriptFromDirectory(scriptDirectory);

        this.prefix = "Chapter ";
        switch(number) {
            case 1:
                this.prefix += "I";
                break;
            case 2:
                this.prefix += "II";
                break;
            case 3:
                this.prefix += "III";
                break;
            case 4:
                this.prefix += "IV";
        }
    }

    /**
     * Constructor for a standard Chapter
     * @param number the Chapter number
     * @param id the internal ID for this Chapter
     * @param title the title of the Chapter
     * @param galleryHintUnlocked the description displayed in the gallery when this Chapter is unlocked
     * @param galleryHintLocked the hint displayed in the gallery when this Chapter is locked
     * @param scriptDirectory the directory of the primary script for this Chapter
     */
    private Chapter(int number, String id, String title, String galleryHintUnlocked, String galleryHintLocked, String scriptDirectory) {
        this.specialTitle = false;
        this.number = number;
        this.id = id;
        this.title = title;
        this.galleryHintUnlocked = galleryHintUnlocked;
        this.galleryHintLocked = galleryHintLocked;
        this.scriptFile = Script.getScriptFromDirectory(scriptDirectory);

        this.prefix = "Chapter ";
        switch(number) {
            case 1:
                this.prefix += "I";
                break;
            case 2:
                this.prefix += "II";
                break;
            case 3:
                this.prefix += "III";
                break;
            case 4:
                this.prefix += "IV";
        }
    }

    /**
     * Constructor for a standard Chapter without a gallery
     * @param number the Chapter number
     * @param id the internal ID for this Chapter
     * @param title the title of the Chapter
     * @param scriptDirectory the directory of the primary script for this Chapter
     */
    private Chapter(int number, String id, String title, String scriptDirectory) {
        this.specialTitle = false;
        this.number = number;
        this.id = id;
        this.title = title;
        this.galleryHintUnlocked = "";
        this.galleryHintLocked = "";
        this.scriptFile = Script.getScriptFromDirectory(scriptDirectory);

        this.prefix = "Chapter ";
        switch(number) {
            case 1:
                this.prefix += "I";
                break;
            case 2:
                this.prefix += "II";
                break;
            case 3:
                this.prefix += "III";
                break;
            case 4:
                this.prefix += "IV";
        }
    }

    /**
     * Constructor for a Chapter with a special prefix
     * @param realNumber the true internal Chapter number
     * @param id the internal ID for this Chapter
     * @param specialPrefix the displayed prefix of the Chapter, in place of "Chapter [number]"
     * @param title the title of the Chapter
     * @param galleryHintUnlocked the description displayed in the gallery when this Chapter is unlocked
     * @param galleryHintLocked the hint displayed in the gallery when this Chapter is locked
     * @param scriptDirectory the directory of the primary script for this Chapter
     */
    private Chapter(int realNumber, String id, String specialPrefix, String title, String galleryHintUnlocked, String galleryHintLocked, String scriptDirectory) {
        this.number = realNumber;
        this.id = id;
        this.prefix = specialPrefix;
        this.title = title;
        this.galleryHintUnlocked = galleryHintUnlocked;
        this.galleryHintLocked = galleryHintLocked;
        this.specialTitle = false;
        this.scriptFile = Script.getScriptFromDirectory(scriptDirectory);
    }

    /**
     * Constructor for a special Chapter
     * @param id the internal ID for this Chapter
     * @param title the title of the Chapter
     * @param galleryHintLocked the hint displayed in the gallery when this Chapter is locked
     * @param scriptDirectory the directory of the primary script for this Chapter
     */
    private Chapter(String id, String title, String galleryHintLocked, String scriptDirectory) {
        this.id = id;
        this.number = 0;
        this.prefix = "";
        this.title = title;
        this.galleryHintUnlocked = title;
        this.galleryHintLocked = galleryHintLocked;
        this.specialTitle = true;
        this.scriptFile = Script.getScriptFromDirectory(scriptDirectory);
    }

    // --- ACCESSORS & CHECKS ---

    /**
     * Accessor for number
     * @return the "true" number of this Chapter, even if it is usually displayed without a chapter number
     */
    public int getNumber() {
        return this.number;
    }

    /**
     * Accessor for id
     * @return the internal ID for this Chapter
     */
    public String getID() {
        return this.id;
    }

    /**
     * Accessor for prefix
     * @return the prefix of this Chapter's title (most commonly "Chapter [n]")
     */
    public String getPrefix() {
        return this.prefix;
    }

    /**
     * Accessor for title
     * @return the title of this Chapter
     */
    public String getTitle() {
        /* if (this != CLARITY) {
            return this.prefix + ": " + this.title;
        } else {
            return this.title;
        } */

        return this.title;
    }

    /**
     * Returns the full title of this Chapter, including both prefix and title
     * @return the full title of this Chapter, including both prefix and title
     */
    public String getFullTitle() {
        switch (this) {
            case CLARITY: return "Chapter ???: " + this.title;
            case SPACESBETWEEN: return "The Spaces Between";
            case ENDOFEVERYTHING: return "The End of Everything";
            default: return this.prefix + ": " + this.title;
        }
    }

    /**
     * Accessor for specialTitle
     * @return whether this Chapter has a special title (and thus should not display a title card at the start of the Chapter)
     */
    public boolean hasSpecialTitle() {
        return this.specialTitle;
    }

    /**
     * Checks whether or not this Chapter has a dedicated section in the Achievements Gallery
     * @return whether or not this Chapter has a dedicated section in the Achievements Gallery
     */
    public boolean hasGallery() {
        return Arrays.asList(GALLERYCHAPTERS).contains(this);
    }

    /**
     * Accessor for galleryHintUnlocked
     * @return the description displayed in the gallery when this Chapter is unlocked
     */
    public String galleryHintUnlocked() {
        return this.galleryHintUnlocked;
    }

    /**
     * Accessor for galleryHintLocked
     * @return the hint displayed in the gallery when this Chapter is locked
     */
    public String galleryHintLocked() {
        return this.galleryHintLocked;
    }
    
    /**
     * Accessor for scriptFile
     * @return the file containing the initial script of this Chapter
     */
    public File getScriptFile() {
        return this.scriptFile;
    }

    /**
     * Checks if this Chapter has a list of content warnings
     * @return true if this Chapter has a list of content warnings; false otherwise
     */
    public boolean hasContentWarnings() {
        return this.number != 0 && this.number != 1;
    }

    /**
     * Returns the list of possible content warnings in this Chapter, given the previous Chapter's ending
     * @param prevEnding the ending achieved in the previous Chapter
     * @return the list of possible content warnings in this Chapter
     */
    public String getContentWarnings(ChapterEnding prevEnding) {
        if (this == GREY && prevEnding == ChapterEnding.LADYKILLER) {
            return "self-immolation; burning to death; body horror"; // Burned Grey warnings
        } else if (this == GREY && prevEnding == ChapterEnding.COLDLYRATIONAL) {
            return "description of a bloated corpse; drowning; body horror"; // Drowned Grey warnings
        } else {
            return this.getContentWarnings();
        }
    }

    /**
     * Returns the list of possible content warnings in this Chapter
     * @return the list of possible content warnings in this Chapter
     */
    public String getContentWarnings() {
        switch (this) {
            // Chapter II
            case ADVERSARY: return "mutilation; disembowelment";
            case TOWER: return "loss of bodily autonomy; forced self-mutilation; forced suicide; gore";
            case SPECTRE: return "exposed organs";
            case NIGHTMARE: return "loss of bodily autonomy; starvation";
            case RAZOR: return "dismemberment; self-dismemberment; unreality; suicide; excessive gore";
            case BEAST: return "being eaten alive; disembowelment; suffocation; gore";
            case WITCH: return "gore; strangulation; being crushed to death";
            case STRANGER: return "derealization; unreality; Cronenberg-esque body horror";
            case PRISONER: return "self-decapitation; gore";
            case DAMSEL: return "derealization; forced self-mutilation; forced suicide; gore";

            // Chapter III
            case NEEDLE: return "gore";
            case FURY: return "self-degloving; flaying; disembowelment; gore; extreme body-horror; existential horror; physical and psychological torture";
            case APOTHEOSIS: return "psychological torture; eye-puncturing";
            case DRAGON: return "gore; dismemberment";
            case WRAITH: return "body horror; loss of bodily autonomy; jumpscare";
            case CLARITY: return "derealization; unreality; loss of control; memory loss";
            case ARMSRACE: return "dismemberment; self-dismemberment; unreality; suicide; excessive gore";
            case NOWAYOUT: return "dismemberment; self-dismemberment; unreality; suicide; excessive gore";
            case DEN: return "cannibalism; extreme gore; extreme violence; starvation";
            case WILD: return "gore; body horror";
            case THORN: return "self-mutilation";
            case CAGE: return "existentialism; decapitation; gore";
            case GREY: return "self-immolation; description of a bloated corpse; drowning; burning to death; body horror"; // combined warnings
            case HAPPY: return "derealization; existentialism; end-of-relationship; some people have experienced triggers related to emotional domestic abuse while playing this chapter";

            // Chapter IV
            case MUTUALLYASSURED: return "dismemberment; self-dismemberment; unreality; suicide; excessive gore";
            case EMPTYCUP: return "dismemberment; self-dismemberment; unreality; suicide; excessive gore";

            default: return "";
        }
    }

    /**
     * Returns the Chapter with the given title
     * @param id the ID of the Chapter
     * @return the Chapter with the given title
     */
    public static Chapter getChapter(String id) {
        for (Chapter c : values()) {
            if (c.getID().equals(id)) return c;
        }

        return null;
    }

    /**
     * Returns the Chapter with the given title
     * @param title the title of the Chapter
     * @return the Chapter with the given title
     */
    public static Chapter getChapterFromTitle(String title) {
        for (Chapter c : values()) {
            if (c.title.equals(title)) return c;
        }

        return null;
    }
}
