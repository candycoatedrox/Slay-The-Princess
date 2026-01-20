import java.util.ArrayList;

public class Finale extends Cycle {

    // Runs from final Narrator conversation to the end

    private final Vessel[] vessels;
    private final ChapterEnding[] endings;

    // Information about the Heart Princess
    private final boolean firstHarsh;
    private final boolean strangerHeart;
    private final Chapter firstPrincess2;
    private final String firstSource;

    private final boolean mirrorWasCruel;

    // Flags for the mirror conversation
    private final Condition mirrorNotSmashed = new Condition(true);
    private final GlobalInt mirrorAngryMeter = new GlobalInt();
    private final NumCondition mirrorIsAngry = new NumCondition(this.mirrorAngryMeter, 1, 2);
    private final Condition mirrorDeathReveal = new Condition();
    private final Condition mirrorConstructReveal = new Condition();
    private final Condition mirrorConstructExplained = new Condition();
    private final Condition mirrorWorseThanDeath = new Condition();

    // Flags for the Shifting Mound
    private boolean moundNameKnown = false;
    private boolean statedGoalSlay = false;
    private boolean mercyOffer = false;

    // Variables used during the debate
    private int violenceArguments = 0; // Number of times the player has selected arguments tied to the "Your New World" ending during the debate
    private int debateEffectiveArguments = 0;
    private int[] vesselOptionsResistance;
    private boolean[] vesselOptionsLeaveOffer;

    // --- CONSTRUCTOR ---

    /**
     * Constructor
     * @param manager the GameManager to link this instance of Finale to
     * @param vessels the list of Vessels that the player claimed
     * @param endings the list of ChapterEndings that the player reached
     * @param firstPrincess the first Chapter 2 encountered by the player (not counting aborted routes)
     * @param parser the IOHandler to link this instance of Finale to
     */
    public Finale(GameManager manager, IOHandler parser, ArrayList<Vessel> vessels, ArrayList<ChapterEnding> endings, boolean firstHarsh, Chapter firstPrincess2, String firstSource, boolean strangerTossedBlade) {
        super(manager, parser);
        this.clearVoices();

        if (vessels.size() != 5) {
            throw new IllegalArgumentException("Incorrect amount of vessels");
        }

        this.vessels = vessels.toArray(new Vessel[5]);
        this.endings = endings.toArray(new ChapterEnding[5]);

        this.firstHarsh = firstHarsh;
        this.firstPrincess2 = firstPrincess2;
        this.firstSource = firstSource;
        this.threwBlade = strangerTossedBlade;
        this.strangerHeart = firstPrincess2 == Chapter.STRANGER;

        this.mirrorWasCruel = manager.mirrorWasCruel();

        this.activeChapter = Chapter.ENDOFEVERYTHING;
        this.mainScript = new Script(this.manager, this.parser, activeChapter.getScriptFile());
    }

    // --- ACCESSORS & CHECKS ---

    /**
     * Accessor for isHarsh
     * @return whether the Princess is currently hostile in Chapters where it varies
     */
    @Override
    public boolean isHarsh() {
        if (this.currentLocation == GameLocation.MIRROR) {
            return mirrorIsAngry.check();
        } else {
            return this.isHarsh;
        }
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
     * Attempts to let the player wipe the mirror clean
     * @param argument the argument given by the player
     * @param secondPrompt whether the player has already been given a chance to re-enter a valid argument
     * @return "cFail" if argument is invalid; redirects to the APPROACH command otherwise
     */
    @Override
    protected String wipe(String argument, boolean secondPrompt) {
        switch (argument) {
            case "the mirror":
            case "mirror":
                return this.approach(argument);
            
            case "":
                if (secondPrompt) {
                    manager.showCommandHelp(Command.WIPE);
                    return "Fail";
                } else {
                    parser.printDialogueLine("What do you want to wipe clean?", true);
                    return this.approach(parser.getInput(), true);
                }

            default:
                manager.showCommandHelp(Command.WIPE);
                return "Fail";
        }
    }

    /**
     * Attempts to let the player smash the mirror
     * @param argument the argument given by the player
     * @param secondPrompt whether the player has already been given a chance to re-enter a valid argument
     * @return "cFail" if argument is invalid; "cSmashFail" if the player is in the heart cabin; redirects to the APPROACH command otherwise
     */
    @Override
    protected String smash(String argument, boolean secondPrompt) {
        switch (argument) {
            case "the mirror":
            case "mirror":
                switch (this.currentLocation) {
                    case MIRROR: return "Smash";
 
                    case CABIN:
                    case STAIRS:
                    case BASEMENT:
                        return "SmashFail";
                    
                    default: return this.approach("mirror");
                }
            
            case "":
                if (secondPrompt) {
                    manager.showCommandHelp(Command.SMASH);
                    return "Fail";
                } else {
                    parser.printDialogueLine("What do you want to smash?", true);
                    return this.approach(parser.getInput(), true);
                }

            default:
                manager.showCommandHelp(Command.SMASH);
                return "Fail";
        }
    }

    /**
     * Attempts to let the player gaze into their reflection
     * @param argument the argument given by the player
     * @param secondPrompt whether the player has already been given a chance to re-enter a valid argument
     * @return "cFail" if argument is invalid; redirects to the APPROACH command otherwise
     */
    @Override
    protected String gaze(String argument, boolean secondPrompt) {
        switch (argument) {
            case "the reflection":
            case "reflection":
            case "the mirror":
            case "mirror":
                return this.approach(argument);
            
            case "":
                if (secondPrompt) {
                    manager.showCommandHelp(Command.GAZE);
                    return "Fail";
                } else {
                    parser.printDialogueLine("What do you want to gaze into?", true);
                    return this.approach(parser.getInput(), true);
                }

            default:
                manager.showCommandHelp(Command.GAZE);
                return "Fail";
        }
    }

    /**
     * Attempts to let the player slay either the Princess or themselves
     * @param argument the target to slay
     * @param secondPrompt whether the player has already been given a chance to re-enter a valid argument
     * @return "cFail" if argument is invalid; "cSlayPrincessDeadFail" if attempting to slay the Princess when she is already dead; "cSlayNoPrincessFail" if attempting to slay the Princess when she is not present; "cSlayPrincessNoBladeFail" if attempting to slay the Princess without the blade; "cSlayPrincessFail" if the player cannot slay the Princess  right now; "cSlayPrincess" if otherwise attempting to slay the Princess; "cSlaySelfNoBladeFail" if attempting to slay themselves without the blade; "cSlaySelfFail" if otherwise attempting to slay themselves
     */
    @Override
    protected String slay(String argument, boolean secondPrompt) {
        switch (argument) {
            case "the princess":
            case "princess":
                if (this.princessDead) {
                    return "SlayPrincessDeadFail";
                } else if (!this.withPrincess) {
                    return "SlayNoPrincessFail";
                } else if (this.currentLocation != GameLocation.HILL && !this.hasBlade) {
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
            case "cMeta": break;


            case "cGoFail":
            case "cEnterFail":
            case "cLeaveFail":
            case "cGoLeave":
            case "cGoPath":
            case "cGoHill":
            case "cGoCabin":
            case "cGoStairs":
            case "cGoBasement":
            case "cGoLeft":
            case "cGoRight":
            case "cProceed":
                parser.printDialogueLine("There is nowhere for you to go.");                
                break;


            case "cApproachAtMirrorFail":
                parser.printDialogueLine("You watched the mirror shatter into pieces.");
                break;

            case "cApproachMirrorFail":
            case "cApproachMirror":
                parser.printDialogueLine("You watched the mirror shatter for good.");
                break;

            case "cApproachHerFail":
            case "cApproachHer":
                if (this.withPrincess) {
                    parser.printDialogueLine("You are already with her.");
                } else {
                    parser.printDialogueLine("She is not here.");
                }

                break;

            case "cWipeFail":
            case "cWipe":
                break;

            case "cSmashNoStubbornFail":
                break;

            case "cSmashFail":
            case "cSmash":
                break;

            case "cGazeNoMirrorFail":
                break;

            case "cGazeFail":
            case "cGaze":
                break;


            case "cSlayPrincessDeadFail":
            case "cTakeHandDeadFail":
            case "cGiveHandDeadFail":
                parser.printDialogueLine("She is already dead.");
                break;

            case "cSlayNoPrincessFail":
            case "cSlayPrincessNoBladeFail":
            case "cSlayPrincessFail":
            case "cSlayPrincess":
                parser.printDialogueLine("You cannot attempt to slay her now.");
                break;

            case "cSlaySelfNoBladeFail":
            case "cSlaySelfFail":
            case "cSlaySelf":
                parser.printDialogueLine("You cannot slay yourself now.");
                break;

            
            case "cTakeBladeFail":
            case "cTakeBlade":
                parser.printDialogueLine("The pristine blade is not here.");
                break;

            case "cDropNoBladeFail":
            case "cDropFail":
            case "cDrop":
            case "cGiveNoBladeFail":
            case "cGiveBladeFail":
            case "cGiveBlade":
            case "cThrowNoBladeFail":
            case "cThrowFail":
            case "cThrow":
                parser.printDialogueLine("You do not have the blade.");
                break;


            case "cTakeHandNoPrincessFail":
                parser.printDialogueLine("There is no one here.");
                break;

            case "cTakeHandFail":
            case "cTakeHand":
                parser.printDialogueLine("You cannot take her hand now.");
                break;

            case "cGiveHandNoPrincessFail":
                parser.printDialogueLine("There is no one here to offer your hand to.");
                break;

            case "cGiveHandFail":
            case "cGiveHand":
                parser.printDialogueLine("You cannot offer her your hand now.");
                break;


            default: parser.printDialogueLine("You have no other options.");
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
            case "cGoLeave":
            case "cGoPath":
            case "cGoCabin":
            case "cGoStairs":
            case "cGoBasement":
            case "cGoLeft":
            case "cGoRight":
            case "cProceed":
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


            case "cApproachAtMirrorFail":
            case "cApproachMirrorFail":
            case "cApproachMirror":
            case "cWipeFail":
            case "cWipe":
            case "cGazeNoMirrorFail":
            case "cGazeFail":
            case "cGaze":
                if (this.strangerHeart) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "XXXXXXXX"));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                }
                
                break;

            case "cApproachHerFail":
            case "cApproachHer":
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

            case "cSmashNoStubbornFail":
            case "cSmashFail":
            case "cSmash": // same Hero line as approach/wipe/gaze but change Contra line
                if (this.strangerHeart) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "XXXXXXXX"));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                }
                
                break;
                

            case "cSlayPrincessDeadFail":
            case "cTakeHandDeadFail":
            case "cGiveHandDeadFail":
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
            case "cSlayPrincess":
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
            case "cSlaySelf":
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
            
            case "cTakeBladeFail":
            case "cTakeBlade":
                if (this.strangerHeart) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "XXXXXXXX"));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                }

                break;

            case "cDropNoBladeFail":
            case "cGiveNoBladeFail":
                if (this.strangerHeart) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "XXXXXXXX"));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                }
                
                break;

            case "cDropFail":
            case "cDrop":
                if (this.strangerHeart) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "XXXXXXXX"));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                }
                
                break;

            case "cGiveBladeFail":
            case "cGiveBlade":
                if (this.strangerHeart) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "XXXXXXXX"));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                }
                
                break;

            case "cThrowNoBladeFail": // same Hero line as drop/give but change Contra line
                if (this.strangerHeart) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "XXXXXXXX"));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                }
                
                break;
                
            case "cThrowFail":
            case "cThrow":
                if (this.strangerHeart) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "XXXXXXXX"));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                }

                break;


            case "cTakeHandNoPrincessFail":
                if (this.strangerHeart) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "XXXXXXXX"));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                }

                break;

            case "cTakeHandFail":
            case "cTakeHand":
                if (this.strangerHeart) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "XXXXXXXX"));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                }

                break;

            case "cGiveHandNoPrincessFail":
                if (this.strangerHeart) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "XXXXXXXX"));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                }

                break;

            case "cGiveHandFail":
            case "cGiveHand":
                if (this.strangerHeart) {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.CONTRARIAN, "XXXXXXXX"));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
                }

                break;


            default: parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "XXXXXXXX"));
        }
    }

    // --- CYCLE MANAGEMENT ---

    /**
     * Initiates and runs the finale of the game, from the conversation with the Narrator until the end
     */
    @Override
    public ChapterEnding runChapter() {
        this.finalMirror();
        return this.moundStart();
    }

    // --- SCENES ---

    /**
     * Runs the conversation with the Narrator in the mirror
     */
    private void finalMirror() {
        this.mainScript = new Script(this.manager, this.parser, "Mirror/FinalMirror");
        mainScript.runSection();
        
        GlobalInt mirrorShards = new GlobalInt(11);
        NumCondition shardsBroken = new NumCondition(mirrorShards, -1, 11);
        GlobalInt revealCount = new GlobalInt();
        NumCondition hasRevealed = new NumCondition(revealCount, 1, 1);

        Condition questGiven = new Condition();
        InverseCondition noQuest = questGiven.getInverse();
        InverseCondition noDeathReveal = mirrorDeathReveal.getInverse();
        InverseCondition noConstructReveal = mirrorConstructReveal.getInverse();
        InverseCondition noConstructExplain = mirrorConstructExplained.getInverse();
        Condition versionsComment = new Condition();
        Condition creationReveal = new Condition();
        Condition moundReveal = new Condition();
        InverseCondition noMoundReveal = moundReveal.getInverse();
        Condition longQuietReveal = new Condition();
        InverseCondition noLongQuietReveal = longQuietReveal.getInverse();
        OrCondition canDeathComment = new OrCondition(this.mirrorDeathReveal, longQuietReveal);

        Condition narratorReveal = new Condition();
        InverseCondition noNarratorReveal = narratorReveal.getInverse();
        Condition echoReveal = new Condition();
        InverseCondition noEchoReveal = echoReveal.getInverse();
        Condition noMultiNarratorReveal = new Condition(true);
        Condition intrusiveAsk = new Condition();
        InverseCondition noIntrusiveAsk = intrusiveAsk.getInverse();
        boolean seeThisAsk = false;
        Condition noWhyPrincessReveal = new Condition(true);
        Condition noSlayWorseComment = new Condition(true);
        Condition noPeopleAsk = new Condition(true);
        Condition noGodComment = new Condition(true);
        InverseCondition noWorseThanDeath = mirrorWorseThanDeath.getInverse();
        Condition whyKillAsk = new Condition();
        InverseCondition noWhyKillAsk = whyKillAsk.getInverse();
        Condition noWhyNoDeathAsk = new Condition(true);
        Condition diedLotsComment = new Condition();
        InverseCondition noDiedLotsComment = diedLotsComment.getInverse();
        Condition noShapedReveal = new Condition(true);
        Condition tortureComment = new Condition();
        Condition deludedComment = new Condition();
        Condition narratorGodAsk = new Condition();
        InverseCondition noNarratorGodAsk = narratorGodAsk.getInverse();
        OrCondition canHubrisComment = new OrCondition(this.mirrorConstructExplained, moundReveal, deludedComment);

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "happy", "(Explore) \"In one of my lives, you doubted yourself. You thought that all of this was wrong.\"", manager.hasClaimedVessel(Vessel.HAPPY), new OrCondition(longQuietReveal, questGiven, creationReveal)));
        activeMenu.add(new Option(this.manager, "torture", "(Explore) \"If you made us, then I want you to know that this has been torture.\"", moundReveal, longQuietReveal));
        activeMenu.add(new Option(this.manager, "alone", "(Explore) \"If I destroy Her, won't I be alone?\"", moundReveal, questGiven));
        activeMenu.add(new Option(this.manager, "whyKill", "(Explore) \"Why would you want me to destroy the concept of transformation?\"", moundReveal, noLongQuietReveal, noDeathReveal));
        activeMenu.add(new Option(this.manager, "worseDeathA", "(Explore) \"If I destroy Her, how is that existence any better than death? Or even different from death at all? Honestly, it feels worse.\"", this.mirrorDeathReveal, moundReveal, noWorseThanDeath, whyKillAsk));
        activeMenu.add(new Option(this.manager, "worseDeathB", "(Explore) \"If you want me to destroy the concept of transformation, how is that existence any better than death? Or even different from death at all? Honestly, it feels worse.\"", this.mirrorDeathReveal, moundReveal, noWorseThanDeath, noWhyKillAsk));
        activeMenu.add(new Option(this.manager, "deluded", "(Explore) \"You're delusional.\"", this.mirrorWorseThanDeath));
        activeMenu.add(new Option(this.manager, "hubris", "(Explore) \"Do you have anything to say for yourself? For all this hubris?\"", canHubrisComment));
        activeMenu.add(new Option(this.manager, "deserve", "(Explore) \"After everything you've done to us, do you think anyone deserves to live?\"", canHubrisComment));
        activeMenu.add(new Option(this.manager, "slayWorse", "(Explore) \"Do you know that things won't just be worse if I destroy Her?\"", moundReveal, noSlayWorseComment));
        activeMenu.add(new Option(this.manager, "postPrincess", "(Explore) \"What would it be like to live in a world without Her?\"", moundReveal));
        activeMenu.add(new Option(this.manager, "anyoneKnow", "(Explore) \"Does anyone else know about this? Does anyone else know about *us?*\"", moundReveal, longQuietReveal, noPeopleAsk));
        activeMenu.add(new Option(this.manager, "godReject", "(Explore) \"I don't want to be a god. I want to be me.\"", longQuietReveal, noGodComment));
        activeMenu.add(new Option(this.manager, "godAccept", "(Explore) \"A god. I always knew I was special.\"", longQuietReveal, noGodComment));
        activeMenu.add(new Option(this.manager, "task", "(Explore) \"I was made to do this single task? Who made me? What am I?\"", noLongQuietReveal, noEchoReveal, questGiven));
        activeMenu.add(new Option(this.manager, "narrator", "(Explore) \"So you're the Narrator. I was wondering if I'd ever get to see you.\"", noNarratorReveal));
        activeMenu.add(new Option(this.manager, "echoA", "(Explore) \"What are you? Are you something like me?\"", noEchoReveal, noNarratorGodAsk));
        activeMenu.add(new Option(this.manager, "echoB", "(Explore) \"If you're not me, then what are you?\"", noEchoReveal, noNarratorGodAsk));
        activeMenu.add(new Option(this.manager, "others", "(Explore) \"'Others like you.' You've said something like that before. Has every Narrator really been different?\"", noMultiNarratorReveal, echoReveal));
        activeMenu.add(new Option(this.manager, "questions", "(Explore) \"I have so many questions for you!\""));
        activeMenu.add(new Option(this.manager, "hurt", "(Explore) \"Does it hurt when pieces of you break off like that?\"", shardsBroken));
        activeMenu.add(new Option(this.manager, "sorry", "(Explore) \"I'm sorry. I don't want to destroy you. Will it help if I look away or stop asking questions?\"", shardsBroken));
        activeMenu.add(new Option(this.manager, "break", "(Explore) \"Every time I ask you something, it's like a piece of you breaks.\"", shardsBroken));
        activeMenu.add(new Option(this.manager, "answers", "(Explore) \"Whenever I've tried getting answers out of you before, you've been absolutely impenetrable. Why are you suddenly being so open?\"", hasRevealed, narratorReveal));
        activeMenu.add(new Option(this.manager, "part", "(Explore) \"Are you a part of me? Or are you something else?\""));
        activeMenu.add(new Option(this.manager, "versions", "(Explore) \"'Versions of you.' You've said that before. So I really was meeting different you's.\"", noMultiNarratorReveal, versionsComment));
        activeMenu.add(new Option(this.manager, "wantSlay", "(Explore) \"You're the one who wanted me to slay the Princess. Why?\"", noNarratorReveal, noDeathReveal));
        activeMenu.add(new Option(this.manager, "whatIsShe", "(Explore) \"You said She contains death. What is She?\"", this.mirrorDeathReveal, noMoundReveal));
        activeMenu.add(new Option(this.manager, "whyHide", "(Explore) \"Why couldn't you have told me all of this from the start? I would have helped you destroy Her.\"", moundReveal));
        activeMenu.add(new Option(this.manager, "living", "(Explore) \"'I don't work the way a living being does? Not anymore?!' Am I not a living being?\"", activeMenu.get("whyHide"), intrusiveAsk));
        activeMenu.add(new Option(this.manager, "intrusiveA", "(Explore) \"What do you mean a single intrusive thought could have instantly ended the world?\"", activeMenu.get("whyHide"), noIntrusiveAsk));
        activeMenu.add(new Option(this.manager, "intrusiveB", "(Explore) \"Doesn't telling me this now mean that an intrusive thought could still end the world?\"", activeMenu.get("whyHide"), noIntrusiveAsk));
        activeMenu.add(new Option(this.manager, "will", "(Explore) \"If She's capable of becoming whatever people believe Her to be, can't I just... will Her into something small?\"", moundReveal, noIntrusiveAsk));
        activeMenu.add(new Option(this.manager, "same", "(Explore) \"I've met you many times. Have you been the same you all along?\"", noMultiNarratorReveal));
        activeMenu.add(new Option(this.manager, "gaslight", "(Explore) \"So you do know about the looping. So many of the times I met you, you denied it as even being a possibility. Why did you lie to me?\"", this.mirrorConstructExplained));
        activeMenu.add(new Option(this.manager, "whatAmI", "(Explore) \"If you made me, what am I?\"", noLongQuietReveal, echoReveal));
        activeMenu.add(new Option(this.manager, "godAsk", "(Explore) \"Are you a god? Or... were you a god?\"", longQuietReveal, noNarratorGodAsk));
        activeMenu.add(new Option(this.manager, "seeThis", "(Explore) \"I wasn't supposed to see all this, was I?\""));
        activeMenu.add(new Option(this.manager, "needToKnow", "(Explore) \"If you want me to slay Her, I need to know what She actually is.\"", seeThisAsk, noMoundReveal));
        activeMenu.add(new Option(this.manager, "howDie", "(Explore) \"How did you die?\"", moundReveal));
        activeMenu.add(new Option(this.manager, "whatPrincess", "(Explore) \"What is the Princess? Did you make Her too?\"", creationReveal, noMoundReveal));
        activeMenu.add(new Option(this.manager, "whyPrincessA", "(Explore) \"Why did you make Her a Princess?\"", moundReveal, noWhyPrincessReveal));
        activeMenu.add(new Option(this.manager, "whyPrincess2", "(Explore) \"I chose to make Her a princess? Why couldn't I have made things easier on myself and picked something small or weak like an ant or a slice of bread?\"", activeMenu.get("whyPrincessA")));
        activeMenu.add(new Option(this.manager, "whyPrincessB", "(Explore) \"Of all things, why is She a Princess? Why couldn't She be an ant or a slice of soggy bread?\"", moundReveal, noWhyPrincessReveal));
        activeMenu.add(new Option(this.manager, "abstract", "(Explore) \"How am I supposed to destroy an abstract concept?\"", moundReveal));
        activeMenu.add(new Option(this.manager, "stall", "(Explore) \"What if neither of us leave this place? Does that work? Can we just stay here together and leave the people out there alone?\"", moundReveal));
        activeMenu.add(new Option(this.manager, "stall2", "(Explore) \"Is there a difference between leaving this place and staying here?\"", activeMenu.get("stall")));
        activeMenu.add(new Option(this.manager, "peopleKnow", "(Explore) \"The people out there beyond the walls of the construct... Do *they* know about this? Do they know what you want me to do to them?\"", this.mirrorConstructReveal, noPeopleAsk));
        activeMenu.add(new Option(this.manager, "where", "(Explore) \"What is this place? Where are we?\"", noConstructReveal, noConstructExplain));
        activeMenu.add(new Option(this.manager, "identity", "(Explore) \"And what is my 'true identity?'\"", this.mirrorConstructExplained, noLongQuietReveal));
        activeMenu.add(new Option(this.manager, "construct", "(Explore) \"You've called this place a construct. What is it supposed to do?\"", this.mirrorConstructReveal, noConstructExplain));
        activeMenu.add(new Option(this.manager, "whyDeath", "(Explore) \"Why would you want to rid the world of death?\"", canDeathComment, noWhyNoDeathAsk));
        activeMenu.add(new Option(this.manager, "deathGood", "(Explore) \"I'm pretty sure death is good, actually. Important, even.\"", canDeathComment, noWhyNoDeathAsk));
        activeMenu.add(new Option(this.manager, "plentyA", "(Explore) \"But I've died plenty of times.\"", activeMenu.get("deathGood"), noDiedLotsComment));
        activeMenu.add(new Option(this.manager, "plentyB", "(Explore) \"Who cares about dying? I've died plenty of times.\"", canDeathComment, noDiedLotsComment));
        activeMenu.add(new Option(this.manager, "experience", "(Explore) \"And how do you know everybody else doesn't also experience death the way I do?\"", diedLotsComment));
        activeMenu.add(new Option(this.manager, "makingUp", "(Explore) \"I think you're wrong. I don't think dying is bad at all, and you're just making all this up as you go.\"", activeMenu.get("experience")));
        activeMenu.add(new Option(this.manager, "ridDeath", "(Explore) \"How am I supposed to rid the world of death?\"", longQuietReveal, noQuest));
        activeMenu.add(new Option(this.manager, "shapedA", "(Explore) \"You made us? Out of what?\"", narratorGodAsk, noShapedReveal));
        activeMenu.add(new Option(this.manager, "shapedB", "(Explore) \"What were we shaped from?\"", activeMenu.get("abstract"), noShapedReveal));
        activeMenu.add(new Option(this.manager, "smash", "(Explore) [Destroy the mirror.]"));

        boolean shardBreakFlag;
        while (mirrorShards.greaterThan(1)) {
            shardBreakFlag = true;

            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "happy":
                    mirrorAngryMeter.add(2);
                    mainScript.runSection(activeOutcome);
                    break;

                case "torture":
                    tortureComment.set();
                    mirrorAngryMeter.increment();
                    mainScript.runSection(activeOutcome);
                    break;

                case "alone":
                case "hurt":
                case "sorry":
                case "break":
                case "howDie":
                case "stall2":
                case "experience":
                    mainScript.runSection(activeOutcome);
                    break;

                case "whyKill":
                    whyKillAsk.set();
                case "wantSlay":
                    mirrorDeathReveal.set();
                    mainScript.runConditionalSection("containsDeath", questGiven);
                    questGiven.set();
                    break;

                case "worseDeathA":
                case "worseDeathB":
                    mirrorWorseThanDeath.set();
                    mainScript.runConditionalSection("worseThanDeath", noSlayWorseComment);
                    noSlayWorseComment.set(false);
                    break;

                case "deluded":
                case "hubris":
                case "deserve":
                case "questions":
                case "stall":
                case "makingUp":
                    mirrorAngryMeter.increment();
                    mainScript.runSection(activeOutcome);
                    break;

                case "slayWorse":
                    noSlayWorseComment.set(false);
                    mainScript.runSection(activeOutcome);
                    break;

                case "postPrincess":
                    mainScript.runSection(activeOutcome);
                    break;

                case "anyoneKnow":
                    mirrorConstructReveal.set();
                    noPeopleAsk.set(false);
                    mainScript.runSection(activeOutcome);
                    break;

                case "godReject":
                case "godAccept":
                    noGodComment.set(false);
                    mainScript.runSection(activeOutcome);
                    break;

                case "task":
                case "whatAmI":
                case "identity":
                    longQuietReveal.set();
                    creationReveal.set();
                    mainScript.runSection("quietReveal");
                    break;

                case "narrator":
                    narratorReveal.set();
                    mainScript.runSection(activeOutcome);
                    break;

                case "echoA":
                case "echoB":
                    echoReveal.set();
                    creationReveal.set();
                    mainScript.runSection(activeOutcome);
                    break;

                case "others":
                case "versions":
                case "same":
                    revealCount.increment();
                    mirrorConstructReveal.set();
                    mirrorConstructExplained.set();
                    noMultiNarratorReveal.set(false);
                    break;

                case "answers":
                    intrusiveAsk.set();
                    mainScript.runSection(activeOutcome);
                    break;

                case "part":
                    versionsComment.set();
                    mainScript.runSection(activeOutcome);
                    break;

                case "whatIsShe":
                    revealCount.increment();
                    moundReveal.set();
                    mainScript.runConditionalSection("whatIsShe", questGiven);
                    break;

                case "whyHide":
                    revealCount.increment();
                    mainScript.runSection(activeOutcome);
                    break;

                case "living":
                    revealCount.increment();
                    mirrorConstructReveal.set();
                    creationReveal.set();
                    longQuietReveal.set();
                    mainScript.runSection(activeOutcome);
                    break;

                case "intrusiveA":
                    revealCount.increment();
                case "intrusiveB":
                    intrusiveAsk.set();
                    mainScript.runSection(activeOutcome);
                    break;

                case "will":
                    revealCount.increment();
                    intrusiveAsk.set();
                    break;

                case "gaslight":
                    revealCount.increment();
                    mainScript.runConditionalSection("gaslight", versionsComment);
                    versionsComment.set();
                    break;

                case "godAsk":
                    revealCount.increment();
                    narratorGodAsk.set();
                    mainScript.runSection(activeOutcome);
                    break;

                case "seeThis":
                    seeThisAsk = true;
                    mainScript.runSection(activeOutcome);
                    break;

                case "needToKnow":
                case "whatPrincess":
                    revealCount.increment();
                    mirrorConstructReveal.set();
                    moundReveal.set();
                    mainScript.runSection("moundReveal");

                    if (questGiven.check()) {
                        mainScript.runSection("moundRevealQuest");
                    } else {
                        questGiven.set();
                        mainScript.runConditionalSection("moundRevealNoQuest", seeThisAsk);
                    }

                    break;

                case "whyPrincessA":
                case "whyPrincessB":
                    noWhyPrincessReveal.set(false);
                case "whyPrincess2":
                    mainScript.runSection(activeOutcome);
                    break;

                case "abstract":
                    mirrorConstructReveal.set();
                    mainScript.runSection(activeOutcome);
                    break;

                case "peopleKnow":
                    revealCount.increment();
                    noPeopleAsk.set(false);
                    mainScript.runSection(activeOutcome);
                    break;

                case "where":
                case "construct":
                    revealCount.increment();
                    mirrorConstructReveal.set();
                    mirrorConstructExplained.set();
                    mainScript.runSection(activeOutcome);
                    break;

                case "deathGood":
                    mirrorAngryMeter.increment();
                case "whyDeath":
                    noWhyNoDeathAsk.set(false);
                    mainScript.runSection(activeOutcome);
                    break;

                case "plentyA":
                case "plentyB":
                    mirrorAngryMeter.increment();
                    diedLotsComment.set();
                    mainScript.runSection("plenty");
                    break;

                case "ridDeath":
                    questGiven.set();
                    mainScript.runSection(activeOutcome);
                    break;

                case "shapedA":
                case "shapedB":
                    noShapedReveal.set(false);
                    mainScript.runSection("shaped");
                    break;

                case "cSmash":
                case "smash":
                    mirrorNotSmashed.set(false);
                    mainScript.runConditionalSection("destroyMirror", longQuietReveal, mirrorShards);
                    mirrorShards.set(0); // Skip the rest of the menu
                    break;

                default:
                    shardBreakFlag = false;
                    this.giveDefaultFailResponse(activeOutcome);
            }

            if (shardBreakFlag) {
                mirrorShards.decrement();
                mainScript.runConditionalSection("shardBreak", mirrorShards);

                if (mirrorShards.equals(3) && noQuest.check()) {
                    mirrorShards.decrement();
                    questGiven.set();
                    creationReveal.set();
                    mainScript.runSection("shards3Quest");
                }
            }
        }

        if (mirrorNotSmashed.check()) {
            this.activeMenu = new OptionsMenu(true);
            activeMenu.add(new Option(this.manager, "time", "\"I think you're out of time.\""));
            activeMenu.add(new Option(this.manager, "not", "\"I'm not going to slay Her, and I want you to know that before you die for good.\""));
            activeMenu.add(new Option(this.manager, "destroy", "\"Rest easy. I'm going to destroy Her.\""));
            activeMenu.add(new Option(this.manager, "lie", "(Lie) \"Rest easy. I'm going to destroy Her.\""));
            activeMenu.add(new Option(this.manager, "undecided", "\"I haven't decided what I'm going to do yet. I still have to see what She thinks about all of this.\""));
            activeMenu.add(new Option(this.manager, "silent", "[Say nothing, and watch him end.]"));

            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "destroy":
                case "lie":
                    mainScript.runConditionalSection("destroyFinal", echoReveal);
                    break;

                case "undecided":
                case "silent":
                    mainScript.runConditionalSection("defaultFinal", echoReveal);
                    break;

                default: mainScript.runConditionalSection(activeOutcome + "Final", echoReveal);
            }

            mainScript.runConditionalSection("finalBreak", longQuietReveal);
        }

        this.moundNameKnown = moundReveal.check();

        this.currentLocation = GameLocation.PATH;
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "proceed", "[Proceed to the cabin, one last time.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "cGoHill":
                case "proceed":
                    this.repeatActiveMenu = false;
                    break;

                default: this.giveDefaultFailResponse();
            }
        }

        manager.unlock(this.vessels[4].getAchievementID());
    }

    /**
     * Runs the intiial conversation with the Shifting Mound
     * @return the ending reached by the player
     */
    private ChapterEnding moundStart() {
        this.mainScript = new Script(this.manager, this.parser, "Finale/FinaleMound");

        this.currentLocation = GameLocation.HILL;
        this.withPrincess = true;
        mainScript.runSection();
        manager.unlock(Chapter.ENDOFEVERYTHING);

        Condition firstChoice = new Condition();
        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "missed", "(Explore) \"I've missed you too.\"", firstChoice));
        activeMenu.add(new Option(this.manager, "echo", "(Explore) \"Do you know about the Echo? Did you hear our conversation?\"", this.mirrorNotSmashed));
        activeMenu.add(new Option(this.manager, "name", "(Explore) \"I'm the Long Quiet. But I don't really know what that means.\""));
        activeMenu.add(new Option(this.manager, "now", "\"What happens now?\""));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "echo":
                    if (mirrorConstructExplained.check()) {
                        mainScript.runSection("echoIntroConstruct");
                    } else {
                        mainScript.runConditionalSection("echoIntroNoConstruct", this.mirrorDeathReveal);
                    }

                    break;

                case "now":
                    this.repeatActiveMenu = false;
                    break;

                case "name": this.moundNameKnown = true;
                default: mainScript.runSection(activeOutcome + "Intro");
            }
        }

        // "What happens now?"
        mainScript.runSection("preDebate");

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "stories", "(Explore) \"There's so many stories we've left unfinished. Can we really just leave?\""));
        activeMenu.add(new Option(this.manager, "say", "(Explore) \"Don't you have a say in all of this? Why is this all falling on me?\""));
        activeMenu.add(new Option(this.manager, "talk", "(Explore) \"Let's talk this through. I still have so many questions and I need answers before I can make a choice.\""));
        activeMenu.add(new Option(this.manager, "world", "(Explore) \"If I let you out, an entire world ends for good. I can't do that.\""));
        activeMenu.add(new Option(this.manager, "point", "(Explore) \"If you were always going to become this, then what was the point of me doing anything? Did it even matter what roads I walked if all of them would have led to this moment?\""));
        activeMenu.add(new Option(this.manager, "another", "(Explore) \"There has to be another way. This can't just come down to me either destroying you or letting you out. I won't do it.\""));
        activeMenu.add(new Option(this.manager, "threatened", "\"I told you what was going to happen when we reached this point.\" [Slay the Princess.]", manager.threatenedMound()));
        activeMenu.add(new Option(this.manager, "slay", "[Slay the Princess.]"));
        activeMenu.add(new Option(this.manager, "ascend", "\"I think it's time for us to leave this place, but I don't know how to leave or where to go.\""));

        this.canSlayPrincess = true;
        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "stories":
                case "say":
                    mainScript.runSection(activeOutcome + "PreDebate");
                    break;

                case "talk":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("talkPreDebate");
                    break;

                case "world":
                case "point":
                case "another":
                    this.repeatActiveMenu = false;
                    mainScript.runSection("understandPreDebate");
                    break;

                case "threatened":
                    this.repeatActiveMenu = false;
                    this.statedGoalSlay = true;
                    mainScript.runSection("threatenedPreDebate");
                    break;

                case "cSlayPrincess":
                case "slay":
                    this.repeatActiveMenu = false;
                    this.statedGoalSlay = true;
                    mainScript.runSection("slayPreDebate");
                    break;

                case "ascend":
                    return this.moundAscend(false);

                default: this.giveDefaultFailResponse();
            }
        }

        // Trigger the debate
        return this.moundDebate();
    }

    /**
     * The player chooses to ascend with the Shifting Mound
     * @param fromDebate whether the player initiated the debate with the Shifting Mound
     * @return the ending reached by the player
     */
    private ChapterEnding moundAscend(boolean fromDebate) {
        if (fromDebate) manager.setNowPlaying("The Shifting Mound Movement V");
        mainScript.runSection("ascendStart");

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "free", "[Free yourself.]"));
        parser.promptOptionsMenu(activeMenu);
        mainScript.runConditionalSection(this.mirrorConstructReveal);

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "hand", "[Take her hand.]"));

        this.canTakeHand = true;
        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "cTakeHand":
                case "hand":
                    this.repeatActiveMenu = false;
                    break;

                default: this.giveDefaultFailResponse();
            }
        }

        // Take her hand
        mainScript.runSection();

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "explore", "(Explore) \"What happens now?\""));
        activeMenu.add(new Option(this.manager, "step", "[Step into the Infinite.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "explore":
                    mainScript.runSection("exploreAscend");
                    break;
                
                case "step":
                    this.repeatActiveMenu = false;
                    break;
            }
        }

        // Step into the Infinite
        mainScript.runSection("ascendEnd");

        if (fromDebate) {
            return ChapterEnding.THROUGHCONFLICT;
        } else {
            return ChapterEnding.NOENDINGS;
        }
    }

    /**
     * Initializes the OptionsMenu used during the debate
     * @return the OptionsMenu used during the debate
     */
    private OptionsMenu createDebateMenu() {
        OptionsMenu menu = new OptionsMenu(true);

        // Up to 3 vessel-specific Your New World options
        activeMenu.add(new Option(this.manager, "violence1", "XXXXX", 0));
        activeMenu.add(new Option(this.manager, "violence2", "XXXXX", 0));
        activeMenu.add(new Option(this.manager, "violence3", "XXXXX", 0));
        
        // Up to 10 vessel-specific options
        activeMenu.add(new Option(this.manager, "vessel1", "XXXXX", 0));
        activeMenu.add(new Option(this.manager, "vessel2", "XXXXX", 0));
        activeMenu.add(new Option(this.manager, "vessel3", "XXXXX", 0));
        activeMenu.add(new Option(this.manager, "vessel4", "XXXXX", 0));
        activeMenu.add(new Option(this.manager, "vessel5", "XXXXX", 0));
        activeMenu.add(new Option(this.manager, "vessel6", "XXXXX", 0));
        activeMenu.add(new Option(this.manager, "vessel7", "XXXXX", 0));
        activeMenu.add(new Option(this.manager, "vessel8", "XXXXX", 0));
        activeMenu.add(new Option(this.manager, "vessel9", "XXXXX", 0));
        activeMenu.add(new Option(this.manager, "vessel10", "XXXXX", 0));
        
        activeMenu.add(new Option(this.manager, "appeal1", "[Appeal to your shared humanity.] \"You speak about life and death and change and stagnation, but that isn't what any of this has been about.\""));
        activeMenu.add(new Option(this.manager, "appeal2", "[Continue to appeal to your shared humanity.] \"This has always just been about us. Two people forced to hurt each other again and again and again. But we don't have to hurt each other anymore.\"", activeMenu.get("appeal1")));
        activeMenu.add(new Option(this.manager, "appeal3", "[Continue to appeal to your shared humanity.] \"There can be love and conflict and beauty and ugliness between us without bringing the whole of reality into the picture.\"", activeMenu.get("appeal2")));
        activeMenu.add(new Option(this.manager, "appeal4", "[Continue to appeal to your shared humanity.] \"We're not the whole of reality. Why won't you see me the way I still see you? Why won't you see me for what I am?\"", activeMenu.get("appeal3")));
        activeMenu.add(new Option(this.manager, "appeal5", "[Continue to appeal to your shared humanity.] \"This doesn't have to be so big. You can come back down to my level. You can come back to me.\"", activeMenu.get("appeal5")));
        
        activeMenu.add(new Option(this.manager, "lecture1", "[Reject her authority.] \"You've done nothing but lecture me since the minute I got here.\""));
        activeMenu.add(new Option(this.manager, "lecture2", "[Continue to reject her authority.] \"You use all these pretentious metaphors and pretend you're making grand proclamations about who I am and who you are. But really you aren't saying much of anything.\"", activeMenu.get("lecture1")));
        activeMenu.add(new Option(this.manager, "lecture3", "[Continue to reject her authority.] \"It doesn't even matter what I say to you, because you're just going to keep telling me your perspective like it's some universal truth.\"", activeMenu.get("lecture2")));
        activeMenu.add(new Option(this.manager, "lecture4", "[Continue to reject her authority.] \"It was so much better when I was with your vessels. Even at their worst, they all still heard me.\"", activeMenu.get("lecture3")));
        activeMenu.add(new Option(this.manager, "lecture5", "[Continue to reject her authority.] \"I don't know what you are, but you aren't any of them. You're just something wearing their skin.\"", activeMenu.get("lecture4")));
        
        activeMenu.add(new Option(this.manager, "assert1", "[Argue your indepedence.] \"You act as though the world can't exist without you. But I've existed without you.\""));
        activeMenu.add(new Option(this.manager, "assert2", "[Continue to argue your indepedence.] \"What are the woods, then? What is the cabin? What is the time we've spent apart if not me existing as myself?\"", activeMenu.get("assert1")));
        activeMenu.add(new Option(this.manager, "assert3", "[Continue to argue your indepedence.] \"I wouldn't be here if destroying you would leave all of reality a colorless blur.\"", activeMenu.get("assert2")));
        activeMenu.add(new Option(this.manager, "assert4", "[Continue to argue your indepedence.] \"I'd rather trust an ignorant soul who died trying to make things better than a god who'd let the wheel of suffering turn forever.\"", activeMenu.get("assert3")));
        activeMenu.add(new Option(this.manager, "assert5A", "[Continue to argue your indepedence.] \"If I need to destroy you to build a better world, then I will.\"", activeMenu.get("assert4")));
        activeMenu.add(new Option(this.manager, "assert5B", "[Continue to argue your indepedence.] \"Who said anything about destroying you? I just need to make you stop.\"", activeMenu.get("assert4")));
        
        activeMenu.add(new Option(this.manager, "reject1", "[Reject her perspective.] \"I won't engage with violence.\""));
        activeMenu.add(new Option(this.manager, "reject2", "[Continue to reject her perspective.] \"It doesn't matter how I feel. Death, suffering, and oblivion shouldn't fall on others. If we are able to transcend death, then we are responsible for those it holds captive.\"", activeMenu.get("reject1")));
        activeMenu.add(new Option(this.manager, "reject3", "[Continue to reject her perspective.] \"Suffering born in delusion is still suffering. It doesn't matter what we are now. We hurt each other, and we shouldn't have done that. We cannot let a world be spun out of that pain.\"", activeMenu.get("reject2")));
        activeMenu.add(new Option(this.manager, "reject4", "[Continue to reject her perspective.] \"You reject the suffering of material reality, and yet you cling to its framework for meaning. We can be better than this.\"", activeMenu.get("reject3")));
        activeMenu.add(new Option(this.manager, "reject5", "[Continue to reject her perspective.] \"You claim your destruction would steal meaning from existence, but if my actions can make existence worse, then there must be actions that make it better. Perfection implies finality, and nothing is final.\"", activeMenu.get("reject4")));
        
        activeMenu.add(new Option(this.manager, "surrender", "\"I get it. There's no need for us to keep fighting. I'll leave with you. I just don't know how.\" [Stop the fight early and surrender.]", false));
        activeMenu.add(new Option(this.manager, "silent", "[Remain silent.]", 0));

        // lines of argument: appeal, lecture, assert, reject
        // each line has 5 stages
        // options always available: surrender (not available first Vessel), silent

        return menu;
    }

    /**
     * Initializes the OptionsMenu used when the Shifting Mound encourages you to surrender during the debate
     * @return the OptionsMenu used when the Shifting Mound encourages you to surrender during the debate
     */
    private OptionsMenu createSurrenderMenu() {
        OptionsMenu menu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "ascend", "\"I'm ready. I want to leave with you.\" [Stop the fight early and surrender.]"));
        activeMenu.add(new Option(this.manager, "refuse", "\"I won't leave with you. Not until you see things from my perspective.\"", 0));
        return menu;
    }

    /**
     * Runs the debate with the Shifting Mound
     * @return the ending reached by the player
     */
    private ChapterEnding moundDebate() {
        this.secondaryScript = new Script(this.manager, this.parser, "Finale/FinaleDebate");

        this.activeMenu = this.createDebateMenu();
        this.subMenu = this.createSurrenderMenu();
        Vessel currentVessel;
        ChapterEnding currentEnding;
        String vesselOption;

        boolean prevViolenceFlag = false;
        boolean violenceBrokenComment = false;

        int silenceCount = 0;

        // Counts down, starting from *last* Vessel claimed
        for (int i = 4; i >= 0; i--) {
            if (i == 0 && this.strangerHeart) break;
            if (i == 3) activeMenu.setCondition("surrender", true);

            if (this.violenceArguments > 0 && prevViolenceFlag && !violenceBrokenComment) {
                violenceBrokenComment = true;
                secondaryScript.runSection("violenceBroken");
            }

            prevViolenceFlag = false;
            currentVessel = this.vessels[i];
            currentEnding = this.endings[i];

            this.moundDebateRunArgument(currentVessel, currentEnding);

            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "violence1":
                case "violence2":
                case "violence3":
                    this.violenceArguments += 1;
                    this.debateEffectiveArguments += 1;
                    prevViolenceFlag = true;

                    if (this.violenceArguments == 5 || (this.strangerHeart && this.violenceArguments == 4)) {
                        if (this.moundViolenceEnd()) {
                            return ChapterEnding.YOURNEWWORLD;
                        }
                    } else {
                        secondaryScript.runSection("violence" + this.violenceArguments);
                    }
                    
                    break;

                case "vessel1":
                case "vessel2":
                case "vessel3":
                case "vessel4":
                case "vessel5":
                case "vessel6":
                case "vessel7":
                case "vessel8":
                case "vessel9":
                case "vessel10":
                    vesselOption = activeOutcome.substring(6);

                    if (!this.moundVesselArgumentResponse(i, currentVessel, vesselOption)) {
                        manager.unlock("ascendDebate");
                        return this.moundAscend(true);
                    }

                    break;

                case "appeal1":
                case "appeal2":
                case "appeal3":
                case "appeal4":
                case "appeal5":
                case "reject1":
                case "reject2":
                case "reject3":
                case "reject4":
                case "reject5":
                    this.debateEffectiveArguments += 1;
                case "lecture1":
                case "lecture2":
                case "lecture3":
                case "lecture4":
                case "lecture5":
                case "assert1":
                case "assert2":
                case "assert3":
                case "assert4":
                case "assert5A":
                case "assert5B":
                    secondaryScript.runSection(activeOutcome);
                    break;

                case "surrender":
                    manager.unlock("ascendDebate");
                    return this.moundAscend(true);

                case "silent":
                    silenceCount += 1;
                    secondaryScript.runSection("silent" + silenceCount);
                    break;

                // lines of argument: violence, appeal, lecture, assert, reject
            }

            if (i == 4) activeMenu.setCondition("surrender", true); 
        }

        // post debate -- to heart cabin
        // "intermission" -- in felina_fight_0_staging.py








        // temporary templates for copy-and-pasting
        /*
        parser.printDialogueLine("XXXXX");
        parser.printDialogueLine(new PrincessDialogueLine("XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "(Explore) XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "(Explore) \"XXXXX\""));
        activeMenu.add(new Option(this.manager, "q1", "XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "\"XXXXX\""));
        */

        // PLACEHOLDER
        if (this.strangerHeart) {
            return this.heartCabinStranger();
        } else {
            return this.heartCabin();
        }
    }

    /**
     * Runs the Shifting Mound's argument for a given Vessel and ChapterEnding and configures the OptionsMenu accordingly to prepare for the player's response
     * @param vessel the current Vessel presenting the argument
     * @param ending the relevant Chapter ending reached by the player
     */
    private void moundDebateRunArgument(Vessel vessel, ChapterEnding ending) {
        secondaryScript.runConditionalSection(ending.moundApproachType() + "Approach", vessel.isHeldByMound(), vessel.getNameInDialogue());

        this.vesselOptionsResistance = new int[10];
        this.vesselOptionsLeaveOffer = new boolean[10];

        switch (vessel) {
            // 1) Run Shifting Mound argument
            // 2) Rename Your New World + vessel-specific arguments
            // 3) Set vessel-specific Options true/false
            // 4) Set this.vesselOptionsResistance and this.vesselOptionsLeaveOffer

            // NOTE: Tower/Apotheosis share a lot of code in the original for some reason? Don't do that here

            // ...

            // Chapter II Vessels
            case ADVERSARY:
                break;

            case TOWER:
                break;

            case SPECTRE:
                break;

            case NIGHTMARE:
                break;

            case BEAST:
                break;

            case WITCH:
                break;

            case STRANGER:
                break;

            case PRISONERHEAD:
            case PRISONER:
                break;

            case DAMSEL:
            case DECONDAMSEL:
                break;

            // Chapter III vessels
            case NEEDLE:
                break;

            case FURY:
            case REWOUNDFURY:
                break;

            case APOTHEOSIS:
                break;

            case PATD:
            case STENCILPATD:
                break;

            case WRAITH:
                break;

            case RAZORFULL:
            case RAZORHEART:
                break;

            case DEN:
                break;

            case NETWORKWILD:
            case WOUNDEDWILD:
                break;

            case THORN:
                break;

            case WATCHFULCAGE:
            case OPENCAGE:
                break;

            case DROWNEDGREY:
            case BURNEDGREY:
                break;

            case HAPPY:
                break;
        }






        // temporary templates for copy-and-pasting
        /*
        parser.printDialogueLine("XXXXX");
        parser.printDialogueLine(new PrincessDialogueLine("XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "(Explore) XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "(Explore) \"XXXXX\""));
        activeMenu.add(new Option(this.manager, "q1", "XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "\"XXXXX\""));
        */
    }

    /**
     * Runs the Shifting Mound's response to a vessel-specific response, in some cases giving the player a chance to end the debate early and ascend with her
     * @param vessel the current Vessel presenting the argument
     * @param nArgument the number of the argument specific to this Vessel
     * @return true if the player continues the fight; false if they choose to ascend with the Shifting Mound
     */
    private boolean moundVesselArgumentResponse(int vesselNum, Vessel vessel, String nArgument) {
        // redirect to script label [vessel][nArgument]
        String vesselID;
        switch (vessel) {
            case PRISONERHEAD:
                vesselID = "prisoner";
                break;

            case DECONDAMSEL:
                vesselID = "damsel";
                break;

            case REWOUNDFURY:
                vesselID = "fury";
                break;

            case STENCILPATD:
                vesselID = "dragon";
                break;

            case RAZORFULL:
            case RAZORHEART:
                vesselID = "razor";
                break;

            case NETWORKWILD:
            case WOUNDEDWILD:
                vesselID = "wild";
                break;

            case DROWNEDGREY:
            case BURNEDGREY:
                vesselID = "grey";
                break;

            default: vesselID = vessel.getID();
        }

        int intArgument = Integer.parseInt(nArgument);
        this.debateEffectiveArguments += this.vesselOptionsResistance[intArgument - 1];
        secondaryScript.runSection(vesselID + nArgument);

        if (vesselNum < 4 && this.vesselOptionsLeaveOffer[intArgument - 1]) {
            if (parser.promptOptionsMenu(subMenu).equals("ascend")) {
                return false;
            } else {
                secondaryScript.runSection("refuseSurrender");
                return true;
            }
        } else {
            return true;
        }
    }

    /**
     * The 
     * @return true if the player commits to slaying the Princess; false if the player offers her mercy
     */
    private boolean moundViolenceEnd() {
        mainScript.runSection("violenceStart");
        




        // DON'T FORGET TO SET mercyOffer IF THE PLAYER OFFERS HER MERCY


        // temporary templates for copy-and-pasting
        /*
        parser.printDialogueLine("XXXXX");
        parser.printDialogueLine(new PrincessDialogueLine("XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "(Explore) XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "(Explore) \"XXXXX\""));
        activeMenu.add(new Option(this.manager, "q1", "XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "\"XXXXX\""));
        */

        // PLACEHOLDER
        return false;
    }

    /**
     * Runs the cabin sequence at the heart of the Shifting Mound (standard version)
     * @return the ending reached by the player
     */
    private ChapterEnding heartCabin() {






        // temporary templates for copy-and-pasting
        /*
        parser.printDialogueLine("XXXXX");
        parser.printDialogueLine(new PrincessDialogueLine("XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "(Explore) XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "(Explore) \"XXXXX\""));
        activeMenu.add(new Option(this.manager, "q1", "XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "\"XXXXX\""));
        */

        // PLACEHOLDER
        return null;
    }

    /**
     * Runs the cabin sequence at the heart of the Shifting Mound (Stranger version)
     * @return the ending reached by the player
     */
    private ChapterEnding heartCabinStranger() {
        manager.unlock("strangerHeart");








        // temporary templates for copy-and-pasting
        /*
        parser.printDialogueLine("XXXXX");
        parser.printDialogueLine(new PrincessDialogueLine("XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "(Explore) XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "(Explore) \"XXXXX\""));
        activeMenu.add(new Option(this.manager, "q1", "XXXXX"));
        activeMenu.add(new Option(this.manager, "q1", "\"XXXXX\""));
        */

        // PLACEHOLDER
        return null;
    }

}
