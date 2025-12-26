import java.io.File;  // Import the File class

public enum Chapter {
    // Chapter I
    CH1(1, "The Hero and the Princess", "Chapter 1/Chapter1Shared"),

    // Chapter II
    ADVERSARY(2, "The Adversary", "Routes/Adversary/AdversaryShared"),
    TOWER(2, "The Tower", "Routes/Tower/TowerShared"),
    SPECTRE(2, "The Spectre", "Routes/Spectre/SpectreShared"),
    NIGHTMARE(2, "The Nightmare", "Routes/Nightmare/NightmareShared"),
    RAZOR(2, "The Razor", "Routes/Razor/Razor2"),
    BEAST(2, "The Beast", "Routes/Beast/BeastShared"),
    WITCH(2, "The Witch", "Routes/Witch/WitchShared"),
    STRANGER(2, "The Stranger", "Routes/Stranger/StrangerIntro"),
    PRISONER(2, "The Prisoner", "Routes/Prisoner/PrisonerShared"),
    DAMSEL(2, "The Damsel", "Routes/Damsel/DamselShared"),

    // Chapter III
    NEEDLE(3, "The Eye of the Needle", "Routes/Adversary/NeedleShared"),
    FURY(3, "The Fury", "Routes/JOINT/Fury/FuryShared"),
    APOTHEOSIS(3, "The Apotheosis", "Routes/Tower/ApotheosisShared"),
    DRAGON(3, "The Princess and the Dragon", true, "Routes/Spectre/DragonShared"),
    WRAITH(3, "The Wraith", "Routes/JOINT/Wraith/WraithShared"),
    CLARITY(3, "The Moment of Clarity", "Routes/Nightmare/MomentOfClarity"),
    ARMSRACE(3, "The Arms Race", "Routes/Razor/Razor3Shared"),
    NOWAYOUT(3, "No Way Out", "Routes/Razor/Razor3Shared"),
    DEN(3, "The Den", "Routes/Beast/DenShared"),
    WILD(3, "The Wild", "Routes/JOINT/Wild/WildShared"),
    THORN(3, "The Thorn", "Routes/Witch/ThornShared"),
    CAGE(3, "The Cage", "Routes/Prisoner/PrisonerShared"),
    GREY(3, "The Grey", "Routes/JOINT/Grey/GreyShared"),
    HAPPY(3, "Epilogue", "Happily Ever After", "Routes/Damsel/HappyShared"),
    
    // Chapter IV
    MUTUALLYASSURED(4, "Mutually Assured Destruction", "Routes/Razor/Razor4"),
    EMPTYCUP(4, "The Empty Cup", "Routes/Razor/Razor4"),

    // Special
    SPACESBETWEEN("The Spaces Between", "Intermission/IntermissionShared"),
    ENDOFEVERYTHING("The End of Everything", "Finale/FinaleOpening");

    private boolean specialTitle;
    private int number;
    private String prefix;
    private String title;

    private File scriptFile;

    // --- CONSTRUCTORS ---

    /**
     * Constructor
     * @param number the Chapter number
     * @param title the title of the Chapter
     * @param specialTitle whether this Chapter has a special title (and thus should not display a title card at the start of the Chapter)
     * @param scriptDirectory the directory of the primary script for this Chapter
     */
    private Chapter(int number, String title, boolean specialTitle, String scriptDirectory) {
        this.specialTitle = specialTitle;
        this.number = number;
        this.title = title;
        this.scriptFile = Script.getFromDirectory(scriptDirectory);

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
     * @param title the title of the Chapter
     * @param scriptDirectory the directory of the primary script for this Chapter
     */
    private Chapter(int number, String title, String scriptDirectory) {
        this.specialTitle = false;
        this.number = number;
        this.title = title;
        this.scriptFile = Script.getFromDirectory(scriptDirectory);

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
     * @param specialPrefix the displayed prefix of the Chapter, in place of "Chapter [number]"
     * @param title the title of the Chapter
     * @param scriptDirectory the directory of the primary script for this Chapter
     */
    private Chapter(int realNumber, String specialPrefix, String title, String scriptDirectory) {
        this.number = realNumber;
        this.prefix = specialPrefix;
        this.title = title;
        this.specialTitle = false;
        this.scriptFile = Script.getFromDirectory(scriptDirectory);
    }

    /**
     * Constructor for a special Chapter
     * @param title the title of the Chapter
     * @param scriptDirectory the directory of the primary script for this Chapter
     */
    private Chapter(String title, String scriptDirectory) {
        this.specialTitle = true;
        this.number = 0;
        this.prefix = "";
        this.title = title;
        this.scriptFile = Script.getFromDirectory(scriptDirectory);
    }

    // --- ACCESSORS & CHECKS ---

    /**
     * Accessor for specialTitle
     * @return whether this Chapter has a special title (and thus should not display a title card at the start of the Chapter)
     */
    public boolean hasSpecialTitle() {
        return this.specialTitle;
    }

    /**
     * Accessor for number
     * @return the "true" number of this Chapter, even if it is usually displayed without a chapter number
     */
    public int getNumber() {
        return this.number;
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
}
