import java.util.ArrayList;
import java.util.HashMap;

public class Finale extends Cycle {

    // Runs from final Narrator conversation to the end

    private final Vessel[] vessels;
    private final Chapter firstPrincess;
    private final boolean strangerHeart;
    private final boolean mirrorWasCruel;

    private HashMap<String, Integer> debateProgress; // Checks how far the player has progressed in each line of argument during the debate

    // --- CONSTRUCTOR ---

    /**
     * Constructor
     * @param manager the GameManager to link this instance of Finale to
     * @param vessels the list of Vessels that the player claimed
     * @param parser the IOHandler to link this instance of Finale to
     */
    public Finale(GameManager manager, ArrayList<Vessel> vessels, Chapter firstPrincess, IOHandler parser) {
        super(manager, parser);
        this.clearVoices();

        if (vessels.size() != 5) {
            throw new IllegalArgumentException("Incorrect amount of vessels");
        }

        this.vessels = vessels.toArray(new Vessel[5]);
        this.firstPrincess = firstPrincess;
        this.strangerHeart = this.vessels[0] == Vessel.STRANGER;
        this.mirrorWasCruel = manager.mirrorWasCruel();
    }
    
    // --- COMMANDS ---

    /**
     * Lets the player choose between viewing general content warnings, content warnings by chapter, or content warnings for the current chapter
     */
    @Override
    protected void showWarningsMenu() {
        OptionsMenu warningsMenu = manager.warningsMenu();
        warningsMenu.setCondition("current", false);
        
        this.trueExclusiveMenu = true;
        switch (parser.promptOptionsMenu(warningsMenu)) {
            case "general":
                manager.showGeneralWarnings();
                break;
            case "by chapter":
                manager.showByChapterWarnings();
                break;
            case "cancel":
                break;
        }

        this.trueExclusiveMenu = false;
    }
    
    // --- COMMAND OVERRIDES ---

    /**
     * Attempts to move the player in a given direction
     * @param argument the direction to move the player in
     * @param secondPrompt whether the player has already been given a chance to re-enter a valid argument
     * @return "cFail" if argument is invalid; "cGo[Location]" if there is a valid location in the given direction; "cGoFail" otherwise
     */
    @Override
    protected String go(String argument, boolean secondPrompt) {
        switch (argument) {
            case "forward":
            case "forwards":
            case "f":
                switch (this.currentLocation.getForward()) {
                    case STAIRS: return "GoStairs";
                    case BASEMENT: return "GoBasement";
                    default: return "GoFail";
                }
            
            case "back":
            case "backward":
            case "backwards":
            case "b":
                switch (this.currentLocation.getBackward()) {
                    case HILL: return "GoHill";
                    case CABIN: return "GoCabin";
                    case STAIRS: return "GoStairs";
                    default: return "GoFail";
                }

            case "inside":
            case "in":
            case "i":
                return (this.currentLocation.canGoInside()) ? this.go("forward") : "GoFail";

            case "outside":
            case "out":
            case "o":
                return (this.currentLocation.canGoOutside()) ? this.go("back") : "GoFail";

            case "down":
            case "d":
                return (this.currentLocation.canGoDown()) ? this.go("forward") : "GoFail";

            case "up":
            case "u":
                return (this.currentLocation.canGoUp()) ? this.go("back") : "GoFail";

            case "":
                if (secondPrompt) {
                    manager.showCommandHelp("go");
                    return "Fail";
                } else {
                    parser.printDialogueLine("Where do you want to go?", true);
                    return this.go(parser.getInput(), true);
                }

            default:
                manager.showCommandHelp("go");
                return "Fail";
        }
    }

    /**
     * Attempts to let the player enter a given location or the nearest appropriate location
     * @param argument the location to enter (should be "cabin", "basement", or an empty String)
     * @return "cFail" if argument is invalid; "cGo[Location]" if there is a valid location the player can enter; "cEnterFail" otherwise
     */
    @Override
    public String enter(String argument) {
        switch (argument) {
            case "": return this.go("inside");

            case "cabin": return "EnterFail";

            case "basement":
                switch (this.currentLocation) {
                    case CABIN:
                    case STAIRS: return this.go("forward");
                    default: return "EnterFail";
                }

            default:
                manager.showCommandHelp("enter");
                return "Fail";
        }
    }

    /**
     * Attempts to let the player leave the current location
     * @param argument the location to leave (should be "woods", "path", "cabin", "basement", or an empty String)
     * @return "cFail" if argument is invalid; "cGo[Location]" if there is a valid location the player can leave; "cLeaveFail" otherwise
     */
    @Override
    public String leave(String argument) {
        switch (argument) {
            case "": this.go("back");

            case "woods":
            case "path": return "LeaveFail";

            case "cabin":
                return (this.currentLocation == GameLocation.CABIN) ? this.go("back") : "LeaveFail";

            case "basement":
                switch (this.currentLocation) {
                    case STAIRS:
                    case BASEMENT: return this.go("back");
                    default: return "LeaveFail";
                }

            default:
                manager.showCommandHelp("leave");
                return "Fail";
        }
    }

    /**
     * Attempts to let the player approach the mirror
     * @param argument the argument given by the player (should be "the mirror" or "mirror")
     * @return "cFail" if argument is invalid; "cApproachFail" if the mirror is not present; "cApproach" otherwise
     */
    @Override
    public String approach(String argument) {
        switch (argument) {
            case "the mirror":
            case "mirror":
                return (this.currentLocation == GameLocation.MIRROR) ? "ApproachAtMirrorFail" : "ApproachFail";
            
            default: return "Fail";
        }
    }

    /**
     * Attempts to let the player slay either the Princess or themselves
     * @param argument the target to slay
     * @param secondPrompt whether the player has already been given a chance to re-enter a valid argument
     * @return "cFail" if argument is invalid; "cSlayNoPrincessFail" if attempting to slay the Princess when she is not present; "cSlayPrincessNoBladeFail" if attempting to slay the Princess without the blade; "cSlayPrincessFail" if the player cannot slay the Princess  right now; "cSlayPrincess" if otherwise attempting to slay the Princess; "cSlaySelfNoBladeFail" if attempting to slay themselves without the blade; "cSlaySelfFail" if otherwise attempting to slay themselves
     */
    @Override
    protected String slay(String argument, boolean secondPrompt) {
        switch (argument) {
            case "the princess":
            case "princess":
                if (!this.withPrincess) {
                    return "SlayNoPrincessFail";
                } else if (!this.hasBlade) {
                    return "SlayPrincessNoBladeFail";
                } else if (!this.canSlayPrincess) {
                    return "SlayPrincessFail";
                } else {
                    return "SlayPrincess";
                }

            case "yourself":
            case "self":
            case "you":
            case "myself":
            case "me":
            case "ourself":
            case "ourselves":
            case "us":
                if (!this.hasBlade) {
                    return "SlaySelfNoBladeFail";
                } else {
                    return "SlaySelfFail";
                }
            
            case "":
                if (secondPrompt) {
                    manager.showCommandHelp("slay");
                    return "Fail";
                } else {
                    parser.printDialogueLine("Who do you want to slay?", true);
                    return this.slay(parser.getInput(), true);
                }

            default:
                manager.showCommandHelp("slay");
                return "Fail";
        }
    }

    /**
     * Prints a generic response to a command failing or being unavailable
     * @param outcome the String representation of the outcome of the attempted command
     */
    @Override
    protected void giveDefaultFailResponse(String outcome) {
        switch (this.currentLocation) {
            case MIRROR:
            case HILL:
                this.giveDefaultFailResponseMound(outcome);
                break;

            case CABIN:
            case STAIRS:
            case BASEMENT:
                this.giveDefaultFailResponseCabin(outcome);
                break;
            
            default: super.giveDefaultFailResponse(outcome);
        }
    }

    /**
     * Prints a generic response to a command failing or being unavailable during the mirror scene or the encounter with the Shifting Mound
     * @param outcome the String representation of the outcome of the attempted command
     */
    private void giveDefaultFailResponseMound(String outcome) {
        switch (outcome) {
            case "cGoCabin":
            case "cGoFail":
            case "cEnterFail":
            case "cLeaveFail":
                parser.printDialogueLine(new DialogueLine("There is nowhere for you to go."));                
                break;

            case "cApproachAtMirrorFail":
                parser.printDialogueLine(new DialogueLine("You watched the mirror shatter into pieces."));
                break;

            case "cApproachFail":
                parser.printDialogueLine(new DialogueLine("You watched the mirror shatter for good."));
                break;

            case "cSlayNoPrincessFail":
            case "cSlayPrincessNoBladeFail":
            case "cSlayPrincessFail":
                parser.printDialogueLine(new DialogueLine("You cannot attempt to slay her now."));
                break;

            case "cSlaySelfNoBladeFail":
            case "cSlaySelfFail":
                parser.printDialogueLine(new DialogueLine("You cannot slay yourself now."));
                break;
            
            case "cTakeFail":
                parser.printDialogueLine(new DialogueLine("The pristine blade is not here."));
                break;

            case "cDropNoBladeFail":
                parser.printDialogueLine(new DialogueLine("You do not have the blade."));
                break;

            case "cThrowNoBladeFail":
                parser.printDialogueLine(new DialogueLine("You do not have the blade."));
                break;

            default:
                parser.printDialogueLine("You have no other options.");
        }
    }

    /**
     * Prints a generic response to a command failing or being unavailable while in the heart of the Shifting Mound
     * @param outcome the String representation of the outcome of the attempted command
     */
    private void giveDefaultFailResponseCabin(String outcome) {
        // Responses here depend on whether you have normal heart (Hero) or Stranger heart (Hero + Contrarian)

        switch (outcome) {
            case "cGoFail":
                if (this.strangerHeart) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "XXXXXXXX"));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                }
                
                break;

            case "cEnterFail":
                if (this.strangerHeart) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "XXXXXXXX"));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                }
                
                break;
                
            case "cLeaveFail":
                if (this.strangerHeart) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "XXXXXXXX"));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                }
                
                break;

            case "cApproachFail":
                if (this.strangerHeart) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "XXXXXXXX"));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                }
                
                break;
            
            case "cApproachInvalidFail":
                if (this.strangerHeart) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "XXXXXXXX"));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                }
                
                break;
                

            case "cSlayNoPrincessFail":
                if (this.strangerHeart) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "XXXXXXXX"));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                }
                
                break;

            case "cSlayPrincessNoBladeFail":
                if (this.strangerHeart) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "XXXXXXXX"));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                }
                
                break;

            case "cSlayPrincessFail":
                if (this.strangerHeart) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "XXXXXXXX"));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                }
                
                break;

            case "cSlaySelfNoBladeFail":
                if (this.strangerHeart) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "XXXXXXXX"));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                }
                
                break;

            case "cSlaySelfFail":
                if (this.strangerHeart) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "XXXXXXXX"));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "We can't just end it here. We have to see this through."));
                }
                
                break;
                

            case "cTakeHasBladeFail":
                if (this.strangerHeart) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "XXXXXXXX"));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                }

                break;
            
            case "cTakeFail":
                if (this.strangerHeart) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "XXXXXXXX"));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                }
                
                break;

            case "cDropNoBladeFail":
                if (this.strangerHeart) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "XXXXXXXX"));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                }
                
                break;

            case "cDropFail":
                if (this.strangerHeart) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "XXXXXXXX"));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                }
                
                break;

            case "cThrowNoBladeFail":
                if (this.strangerHeart) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "XXXXXXXX"));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                }
                
                break;
                
            case "cThrowFail":
                if (this.strangerHeart) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "XXXXXXXX"));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                }

                break;

            default:
                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
        }
    }

    // --- CYCLE MANAGEMENT ---

    /**
     * Initiates and runs the finale of the game, from the conversation with the Narrator until the end
     */
    @Override
    public ChapterEnding runCycle() {
        this.finalMirror();
        
        // PLACEHOLDER
        return this.openingConversation();
    }

    // --- SCENES ---

    /**
     * Runs the conversation with the Narrator in the mirror
     */
    private void finalMirror() {
        // starts right after mirror shatters
    }

    /**
     * Runs the intiial conversation with the Shifting Mound
     * @return the ending the player reaches
     */
    private ChapterEnding openingConversation() {
        // PLACEHOLDER
        return this.debate();
    }

    /**
     * Runs the debate with the Shifting Mound
     * @return the ending the player reaches
     */
    private ChapterEnding debate() {
        // PLACEHOLDER
        return this.heartCabin();
    }

    /**
     * Runs the cabin sequence at the heart of the Shifting Mound (standard version)
     * @return the ending the player reaches
     */
    private ChapterEnding heartCabin() {
        // PLACEHOLDER
        return ChapterEnding.PATHINTHEWOODS;
    }

    /**
     * Runs the cabin sequence at the heart of the Shifting Mound (Stranger version)
     * @return the ending the player reaches
     */
    private ChapterEnding heartCabinStranger() {
        // PLACEHOLDER
        return ChapterEnding.PATHINTHEWOODS;
    }

}
