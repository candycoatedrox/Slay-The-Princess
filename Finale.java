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

    private int ynwArguments = 0; // Number of times the player has selected arguments tied to the "Your New World" ending during the debate
    private int silentCount = 0; // Number of the times the player has remained silent during the debate

    private boolean abortedYNW = false;
    private boolean statedGoalSlay = false;

    // --- CONSTRUCTOR ---

    /**
     * Constructor
     * @param manager the GameManager to link this instance of Finale to
     * @param vessels the list of Vessels that the player claimed
     * @param endings the list of ChapterEndings that the player reached
     * @param firstPrincess the first Chapter 2 encountered by the player (not counting aborted routes)
     * @param parser the IOHandler to link this instance of Finale to
     */
    public Finale(GameManager manager, IOHandler parser, ArrayList<Vessel> vessels, ArrayList<ChapterEnding> endings, boolean firstHarsh, Chapter firstPrincess2, String firstSource) {
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
        this.strangerHeart = firstPrincess2 == Chapter.STRANGER;

        this.mirrorWasCruel = manager.mirrorWasCruel();

        this.activeChapter = Chapter.ENDOFEVERYTHING;
        this.mainScript = new Script(this.manager, this.parser, activeChapter.getScriptFile());
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
            case "cProceed":
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
    public ChapterEnding runChapter() {
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
        // use isHarsh to indicate angry here







        manager.unlock(this.vessels[4].getAchievementID());
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


        manager.unlock(Chapter.ENDOFEVERYTHING);




        // PLACEHOLDER
        return this.debate();
    }

    /**
     * Initializes the OptionsMenu used during the debate
     * @return the OptionsMenu used during the debate
     */
    private OptionsMenu createDebateMenu() {
        OptionsMenu menu = new OptionsMenu();

        // Up to 3 vessel-specific Your New World options
        activeMenu.add(new Option(this.manager, "yourNewWorld1", "XXXXX", 0));
        activeMenu.add(new Option(this.manager, "yourNewWorld2", "XXXXX", 0));
        activeMenu.add(new Option(this.manager, "yourNewWorld3", "XXXXX", 0));
        
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
     * Runs the debate with the Shifting Mound
     * @return the ending the player reaches
     */
    private ChapterEnding debate() {
        this.secondaryScript = new Script(this.manager, this.parser, "Finale/FinaleDebate");
        this.activeMenu = this.createDebateMenu();
        Vessel currentVessel;
        ChapterEnding currentEnding;
        String vesselOption;

        // Counts down, starting from *last* Vessel claimed
        for (int i = 4; i >= 0; i--) {
            if (i == 0 && this.strangerHeart) break;

            currentVessel = this.vessels[i];
            currentEnding = this.endings[i];

            this.debateShiftingMoundArgument(currentVessel, currentEnding);

            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "yourNewWorld1":
                case "yourNewWorld2":
                case "yourNewWorld3":
                    this.ynwArguments += 1;
                    secondaryScript.runSection("ynwDebate" + ynwArguments);
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
                    this.vesselArgumentResponse(currentVessel, vesselOption);
                    break;

                case "appeal1":
                case "appeal2":
                case "appeal3":
                case "appeal4":
                case "appeal5":
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
                case "reject1":
                case "reject2":
                case "reject3":
                case "reject4":
                case "reject5":
                    secondaryScript.runSection(activeOutcome);
                    break;

                case "surrender":
                    // FILL IN
                    break;

                case "silent":
                    this.silentCount += 1;
                    secondaryScript.runSection("silent" + silentCount);
                    break;

                // lines of argument: yourNewWorld, appeal, lecture, assert, reject
            }

            if (i == 4) activeMenu.setCondition("surrender", true); 
        }

        // post debate -- to heart cabin


        // PLACEHOLDER
        if (this.strangerHeart) {
            return this.heartCabinStranger();
        } else {
            return this.heartCabin();
        }
    }

    /**
     * Runs the Shifting Mound's argument for a given Vessel and ChapterEnding and configures the OptionsMenu accordingly to prepare for the player's response
     * @param ending the relevant Chapter ending reached by the player
     */
    private void debateShiftingMoundArgument(Vessel vessel, ChapterEnding ending) {
        switch (vessel) {
            // 1) Run Shifting Mound argument
            // 2) Rename Your New World + vessel-specific arguments
            // 3) Set vessel-specific Options true/false

            // NOTE: Tower/Apotheosis share a lot of code in the original for some reason? Don't do that here

            // ...
        }
    }

    private void vesselArgumentResponse(Vessel vessel, String nArgument) {
        // redirect to script label [vessel][nArgument]

        String vesselID = "";
        switch (vessel) {
            // set vesselID
            // ...
        }

        secondaryScript.runSection(vesselID + nArgument);
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
        manager.unlock("strangerHeart");


        // PLACEHOLDER
        return ChapterEnding.PATHINTHEWOODS;
    }

}
