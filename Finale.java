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
        manager.warningsMenu().setCondition("current", false);
        
        this.trueExclusiveMenu = true;
        switch (parser.promptOptionsMenu(manager.warningsMenu())) {
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
    public String go(String argument) {
        String outcome = super.go(argument, false);
        
        switch (this.currentLocation) {
            case HILL:
                if (outcome.equals("GoCabin")) break;
            case MIRROR:
                return "GoFail";

            case CABIN:
            case STAIRS:
            case BASEMENT:
                if (outcome.equals("GoHill")) return "GoFail";
                break;
        }

        return outcome;
    }

    /**
     * Attempts to let the player leave the current location
     * @param argument the location to leave (should be "woods", "path", "cabin", "basement", or an empty String)
     * @return "cFail" if argument is invalid; "cGo[Location]" if there is a valid location the player can leave; "cLeaveFail" otherwise
     */
    @Override
    public String leave(String argument) {
        String outcome = super.leave(argument);
        if (outcome.equals("GoLeave")) return "GoFail";
        return outcome;
    }

    /**
     * Attempts to let the player approach the mirror or her
     * @param argument the argument given by the player -- the target to approach
     * @param secondPrompt whether the player has already been given a chance to re-enter a valid argument
     * @return "cFail" if argument is invalid; "cApproachAtMirrorFail" if attempting to approach the mirror when the player is already at the mirror; "cApproachMirrorFail" if attempting to approach the mirror when it is not present; "cApproachMirror" if otherwise attempting to approach the mirror; "cApproachHerFail" if attempting to approach her when not in the Spaces Between; "cApproachHer" if otherwise attempting to approach her
     */
    @Override
    protected String approach(String argument, boolean secondPrompt) {
        switch (argument) {
            case "the mirror":
            case "mirror":
                if (this.currentLocation == GameLocation.MIRROR) {
                    return "ApproachAtMirrorFail";
                } else {
                    return "ApproachMirrorFail";
                }
            
            case "":
                if (secondPrompt) {
                    manager.showCommandHelp("approach");
                    return "Fail";
                } else {
                    parser.printDialogueLine("What do you want to approach?", true);
                    return this.approach(parser.getInput(), true);
                }

            default:
                return super.approach(argument, secondPrompt);
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

            case "cApproachMirrorFail":
                parser.printDialogueLine(new DialogueLine("You watched the mirror shatter for good."));
                break;

            case "cApproachHerFail":
                if (this.withPrincess) {
                    parser.printDialogueLine(new DialogueLine("You are already with her."));
                } else {
                    parser.printDialogueLine(new DialogueLine("She is not here."));
                }

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
            case "cGoHill":
                if (this.strangerHeart) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "XXXXXXXX"));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "We can't just... leave. We have to see this through."));
                }
                
                break;

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

            case "cApproachMirrorFail":
                if (this.strangerHeart) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "XXXXXXXX"));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                }
                
                break;

            case "cApproachHerFail":
                if (this.withPrincess) {
                    if (this.strangerHeart) {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "XXXXXXXX"));
                    } else {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                    }
                } else {
                    if (this.strangerHeart) {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "XXXXXXXX"));
                    } else {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                    }
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
        System.out.println();
    }

    /**
     * Runs the intiial conversation with the Shifting Mound
     * @return the ending the player reaches
     */
    private ChapterEnding openingConversation() {
        System.out.println();
        System.out.println();
        System.out.println();
        parser.printDialogueLine("You find yourself in The Long Quiet once again.");

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "proceed", "[Proceed to the cabin.]"));

        this.repeatActiveMenu = true;
        while (this.repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);

            switch (this.activeOutcome) {
                case "cGoHill":
                case "proceed":
                    this.repeatActiveMenu = false;
                    break;

                case "cGoLeave":
                case "cGoFail":
                case "cEnterFail":
                case "cLeaveFail":
                    parser.printDialogueLine("There is nowhere else for you to go.");
                    break;
                    
                default:
                    this.giveDefaultFailResponse(this.activeOutcome);
            }
        }



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
