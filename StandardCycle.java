import java.util.ArrayList;
import java.util.HashMap;

public abstract class StandardCycle extends Cycle {

    // One CYCLE = from beginning of Ch1 to the end of Shifting Mound interlude

    protected final boolean isFirstVessel;
    protected final boolean canAbort;
    protected final ArrayList<Chapter> route;
    
    // Utility variables for checking command availability & default responses
    protected Condition cantTryAbort = new Condition();

    protected static final PrincessDialogueLine CANTSTRAY = new PrincessDialogueLine(true, "You have already committed to my completion. You cannot go further astray.");
    protected static final PrincessDialogueLine WORNPATH = new PrincessDialogueLine(true, "This path is already worn by travel and has been seen by one of my many eyes. You cannot walk it again. Change your course.");
    protected static final VoiceDialogueLine WORNPATHHERO = new VoiceDialogueLine(Voice.HERO, "Wait... what?!");
    protected static final PrincessDialogueLine DEMOBLOCK = new PrincessDialogueLine(true, "That path is not available to you.");

    // --- CONSTRUCTORS ---

    /**
     * Constructor for Chapter 1
     * @param manager the GameManager to link this StandardCycle to
     * @param parser the IOHandler to link this StandardCycle to
     */
    protected StandardCycle(GameManager manager, IOHandler parser) {
        super(manager, parser);

        this.isFirstVessel = manager.nClaimedVessels() == 0;
        this.canAbort = manager.nClaimedVessels() < 2;
        this.route = new ArrayList<>();

        this.currentVoices = new HashMap<>();
        for (Voice v : Voice.values()) {
            switch (v) {
                case NARRATOR:
                case HERO: this.currentVoices.put(v, true);

                default: this.currentVoices.put(v, false);
            }
        }
    }

    /**
     * Constructor for a Chapter 2 or 3
     * @param manager the GameManager to link this StandardCycle to
     * @param parser the IOHandler to link this StandardCycle to
     * @param hasTriedAbort whether the player has already tried (and failed) to abort this route
     * @param route the Chapters the player has visited so far during this route
     * @param prevEnding the ending of the previous chapter
     */
    protected StandardCycle(GameManager manager, IOHandler parser, ArrayList<Chapter> route, boolean hasTriedAbort, ChapterEnding prevEnding) {
        super(manager, parser);

        this.isFirstVessel = manager.nClaimedVessels() == 0;
        this.canAbort = manager.nClaimedVessels() < 2;

        this.cantTryAbort = new Condition(hasTriedAbort);

        this.activeChapter = prevEnding.getNextChapter();
        this.route = route;
        route.add(this.activeChapter);

        this.currentVoices = new HashMap<>();
        for (Voice v : Voice.values()) {
            switch (v) {
                case NARRATOR:
                case HERO: this.currentVoices.put(v, true);

                default: this.currentVoices.put(v, false);
            }
        }
    }

    // --- ACCESSORS & MANIPULATORS ---

    /**
     * Returns whether the player has not yet claimed their first vessel
     * @return whether the player has not yet claimed their first vessel
     */
    @Override
    public boolean isFirstVessel() {
        return this.isFirstVessel;
    }

    // --- COMMANDS ---

    /**
     * Attempts to let the player enter a given location or the nearest appropriate location
     * @param argument the location to enter (should be "cabin", "basement", or an empty String)
     * @return "cFail" if argument is invalid; "cGo[Location]" if there is a valid location the player can enter; "cEnterFail" otherwise
     */
    @Override
    public String enter(String argument) {
        String outcome = super.enter(argument);
        if (this.activeChapter == Chapter.SPACESBETWEEN && outcome.equals("GoCabin")) {
            return "EnterFail";
        } else {
            return outcome;
        }
    }

    /**
     * Attempts to let the player wipe the mirror clean
     * @param argument the argument given by the player
     * @param secondPrompt whether the player has already been given a chance to re-enter a valid argument
     * @return "cFail" if argument is invalid; redirects to the APPROACH command if the player is not currently in front of the mirror; "cWipeFail" if the player cannot wipe the mirror clean right now; "cWipe" otherwise
     */
    @Override
    protected String wipe(String argument, boolean secondPrompt) {
        switch (argument) {
            case "the mirror":
            case "mirror":
                if (this.currentLocation == GameLocation.MIRROR) {
                    if (this.mirrorGazeFlag) {
                        return "WipeFail";
                    } else {
                        return "Wipe";
                    }
                } else {
                    return this.approach("mirror");
                }
            
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
     * @return "cFail" if argument is invalid; redirects to the APPROACH command if the player is not currently in front of the mirror; "cSmashNoStubbornFail" if the player does not currently have the Voice of the Stubborn; "cSmashFail" if the player cannot smash the mirror right now; "cSmash" otherwise
     */
    @Override
    protected String smash(String argument, boolean secondPrompt) {
        switch (argument) {
            case "the mirror":
            case "mirror":
                if (this.currentLocation == GameLocation.MIRROR) {
                    if (this.mirrorGazeFlag) {
                        return "SmashFail";
                    } else if (!this.hasVoice(Voice.STUBBORN)) {
                        return "SmashNoStubbornFail";
                    } else {
                        return "Smash";
                    }
                } else {
                    return this.approach("mirror");
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
     * @return "cFail" if argument is invalid; redirects to the APPROACH command if the player is standing before the mirror at the end of a cycle; redirects to the WIPE command if the player is at the mirror but cannot gaze into their reflection right now; "cGazeNoMirrorFail" if the mirror isn't present; "cGazeFail" if the player otherwise cannot gaze into their reflection right now; "cGaze" otherwise
     */
    @Override
    protected String gaze(String argument, boolean secondPrompt) {
        switch (argument) {
            case "the reflection":
            case "reflection":
            case "the mirror":
            case "mirror":
                if (this.currentLocation == GameLocation.MIRROR) {
                    if (this.mirrorGazeFlag) {
                        return "Gaze";
                    } else {
                        return this.wipe("mirror");
                    }
                } else if (this.mirrorPresent ||this.currentLocation == GameLocation.BEFOREMIRROR) {
                    return this.approach("mirror");
                } else {
                    return "GazeNoMirrorFail";
                }
            
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
     * @return "cFail" if argument is invalid; "cSlayNoPrincessFail" if attempting to slay the Princess when she is not present; "cSlayPrincessNoBladeFail" if attempting to slay the Princess without the blade; "cSlayPrincessFail" if the player cannot slay the Princess  right now; "cSlayPrincess" if otherwise attempting to slay the Princess; "cSlaySelfNoBladeFail" if attempting to slay themselves without the blade; "cSlaySelfFail" if the player cannot slay themselves right now; "cSlaySelf" if otherwise attempting to slay themselves
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
                } else if (!this.canSlaySelf) {
                    return "SlaySelfFail";
                } else {
                    return "SlaySelf";
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
        if (!this.hasVoice(Voice.NARRATOR)) {
            super.giveDefaultFailResponse(outcome);
            return;
        }

        switch (outcome) {
            case "cMeta": break;


            case "cGoFail":
            case "cGoLeave":
            case "cGoPath":
            case "cGoHill":
            case "cGoCabin":
            case "cGoStairs":
            case "cGoBasement":
            case "cGoLeft":
            case "cGoRight":
            case "cProceed":
                parser.printDialogueLine(new VoiceDialogueLine("There's nowhere to go that way."));
                break;

            case "cEnterFail":
                parser.printDialogueLine(new VoiceDialogueLine("You can't get there from where you are now. Just keep moving forward."));
                break;
                
            case "cLeaveFail":
                parser.printDialogueLine(new VoiceDialogueLine("You can't leave a place if you aren't there in the first place."));
                break;


            case "cApproachAtMirrorFail":
                parser.printDialogueLine(new VoiceDialogueLine("I don't know how many times I have to say this, but there is no mirror."));
                if (this.hasVoice(Voice.HERO)) {
                    if (this.mirrorPresent) {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "We're... already standing in front of the mirror."));
                    } else {
                        parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Well, *now* there isn't."));
                    }
                }

                break;
                
            case "cApproachMirrorFail":
            case "cApproachMirror":
            case "cGazeNoMirrorFail":
            case "cGazeFail":
            case "cGaze":
                parser.printDialogueLine(new VoiceDialogueLine("What are you talking about? There isn't a mirror."));
                if ((this.mirrorComment || this.touchedMirror) && this.hasVoice(Voice.HERO)) parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "He's... actually right this time. The mirror really isn't here."));
                break;

            case "cApproachHerFail":
            case "cApproachHer":
                parser.printDialogueLine(new VoiceDialogueLine("...What?"));
                break;

            case "cWipeFail":
            case "cWipe":
            case "cSmashFail":
            case "cSmash":
                // SHOULD BE INACCESSIBLE; cWipeFail and cSmashFail should only ever be returned without the Narrator there
                super.giveDefaultFailResponse(outcome);
                break;

            case "cSmashNoStubbornFail":
                parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "That seems a bit... aggressive. Let's just wipe it clean."));
                break;
                

            case "cSlayNoPrincessFail":
                parser.printDialogueLine(new VoiceDialogueLine("As much as I appreciate your enthusiasm, the Princess isn't here right now. Save it for when you reach the basement."));
                break;

            case "cSlayPrincessNoBladeFail":
                parser.printDialogueLine(new VoiceDialogueLine("*Sigh.* Unfortunately, you have no weapon with which to slay her. If only you had the blade, this would be so much easier."));
                break;

            case "cSlayPrincessFail":
            case "cSlayPrincess":
                // The Narrator doesn't have a line here because there is no universe in which He would ever say no to you trying to slay the Princess if you have the opportunity.
                // Unfortunately for Him, sometimes I can't let you slay her in the middle of certain menus. Too bad, Narrator.
                super.giveDefaultFailResponse(outcome);
                break;

            case "cSlaySelfNoBladeFail":
                parser.printDialogueLine(new VoiceDialogueLine("Why on earth would you even consider that?!"));

                if (this.knowsBlade) {
                    parser.printDialogueLine(new VoiceDialogueLine("*Sigh.* *Conveniently,* you don't have any sort of weapon with which to kill yourself, so you *can't.* Just get the idea out of your head, alright?"));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine("*Sigh.* *Conveniently,* you don't have the blade, so you *can't.* Just get the idea out of your head, alright?"));
                }

                break;

            case "cSlaySelfFail":
            case "cSlaySelf":
                parser.printDialogueLine(new VoiceDialogueLine("Are you insane?! Absolutely not."));
                if (this.hasVoice(Voice.HERO)) parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "He's right. We don't need to make such a... rash decision."));
                break;
                

            case "cTakeHasBladeFail":
                parser.printDialogueLine(new VoiceDialogueLine("You already have the blade, remember?"));
                break;
            
            case "cTakeBladeFail":
                if (this.knowsBlade) {
                    parser.printDialogueLine(new VoiceDialogueLine("As much as I appreciate your enthusiasm, the blade isn't here right now."));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine("As much as I appreciate your enthusiasm, there isn't a blade here."));
                }

                break;

            case "cTakeBlade":
                super.giveDefaultFailResponse(outcome);
                break;

            case "cDropNoBladeFail":
                if (this.knowsBlade) {
                    parser.printDialogueLine(new VoiceDialogueLine("I can't fathom why you would want to drop your only weapon, but you don't even have it right now."));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine("I can't fathom why you would want to drop your weapon, but you don't even have one right now."));
                }
                
                break;

            case "cDropFail":
            case "cDrop":
                parser.printDialogueLine(new VoiceDialogueLine("You can't drop the blade now. You're here for a reason. Finish the job."));
                break;

            case "cGiveNoBladeFail":
                if (this.knowsBlade) {
                    parser.printDialogueLine(new VoiceDialogueLine("I can't fathom why you would want to give your only weapon to someone who, in case you weren't paying attention, is an *existential threat to the entire world,* but you don't even have it right now."));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine("I can't fathom why you would want to give a weapon to someone who, in case you weren't paying attention, is an *existential threat to the entire world,* but you don't even have one right now."));
                }
                
                break;

            case "cGiveBladeFail":
            case "cGiveBlade":
                parser.printDialogueLine(new VoiceDialogueLine("What? Absolutely not. She's an existential threat to the entire world, you can't just *give her your weapon.*"));
                break;

            case "cThrowNoBladeFail":
                if (this.knowsBlade) {
                    parser.printDialogueLine(new VoiceDialogueLine("I can't fathom why you would want to throw your only weapon away, but conveniently, you don't even have it right now."));
                } else {
                    parser.printDialogueLine(new VoiceDialogueLine("I can't fathom why you would want to throw your weapon away, but conveniently, you don't even have one right now."));
                }
                
                break;
                
            case "cThrowFail":
            case "cThrow":
                parser.printDialogueLine(new VoiceDialogueLine("Are you insane?! Absolutely not."));
                if (this.hasVoice(Voice.HERO)) parser.printDialogueLine(new VoiceDialogueLine(Voice.HERO, "Why would we even do that? That seems... silly."));
                break;


            default: this.giveDefaultFailResponse();
        }
    }

    /**
     * Prints a generic response to a command failing or being unavailable
     */
    @Override
    protected void giveDefaultFailResponse() {
        if (!this.hasVoice(Voice.NARRATOR)) {
            super.giveDefaultFailResponse();
        } else {
            parser.printDialogueLine(new VoiceDialogueLine("You have no other choice."));
        }
    }

    /**
     * Prints a line about the Long Quiet beginning to creep closer, used in most endings right before a vessel is claimed
     */
    @Override
    public void quietCreep() {
        System.out.println();
        if (this.isFirstVessel && manager.nVesselsAborted() == 0) {
            parser.printDialogueLine("A textured nothingness begins to creep into the edges of your vision.");
        } else {
            parser.printDialogueLine("A textured nothingness begins to creep into the edges of your vision. Somehow, it feels familiar.");
        }
        System.out.println();
    }

    // --- CYCLE MANAGEMENT ---

    /**
     * Displays the title card of the active Chapter
     */
    protected void displayTitleCard() {
        System.out.println();
        System.out.println();
        System.out.println();

        if (this.activeChapter == Chapter.CLARITY) {
            try {
                parser.printDivider();
                parser.printDialogueLine("Chapter III", true);
                parser.printDivider();
                Thread.sleep(750);

                parser.printDialogueLine(new DialogueLine("   Chapter IV      Chapter V", true), 1.5);
                parser.printDivider();
                Thread.sleep(700);
                
                parser.printDialogueLine(new DialogueLine(" Chapter VII   Chapter VI          Chapter VIII", true), 2.5);
                parser.printDivider();
                Thread.sleep(700);
                
                IOHandler.wrapPrintln("Chapter XIIChapter IX  ChapterChXVIerX Chapter XVChapterXIV   ChapterhaXIer XIIIChapter XVII");
                parser.printDivider();
                Thread.sleep(550);
                
                System.out.println("-----------------------------------");
                System.out.println("CChXpICXaVIIaXtVapVerXhVIItXXIhapXrIVpChXXerV");
                System.out.println("-----------------------------------");
                System.out.println("-----------------------------------");
                Thread.sleep(400);

                
                System.out.println("-----------------------------------");
                System.out.println("-----------------------------------");
                System.out.println("-----------------------------------");
                Thread.sleep(350);

                
                System.out.println("-----------------------------------");
                System.out.println("-----------------------------------");
                System.out.println("-----------------------------------");
                System.out.println("-----------------------------------");
                Thread.sleep(200);

                
                System.out.println("-----------------------------------");
                System.out.println("-----------------------------------");
                System.out.println("-----------------------------------");
                System.out.println("-----------------------------------");
                System.out.println("-----------------------------------");
                System.out.println("-----------------------------------");
                Thread.sleep(2000);

                
                IOHandler.wrapPrintln("THE MOMENT OF CLARITY");
                System.out.print("-----------------------------------");
                parser.waitForInput();

                System.out.println();
            } catch (InterruptedException e) {
                throw new RuntimeException("Thread interrupted");
            }
        } else {
            parser.printDivider();
            parser.printDialogueLine(this.activeChapter.getPrefix(), true);
            parser.printDialogueLine(this.activeChapter.toString(), true);
            parser.printDivider(false);
            
            System.out.println();
        }
    }

    // --- CHAPTERS & SCENES ---

    // - The Mirror -

    /**
     * Runs the mirror sequence after claiming a vessel
     */
    protected void mirrorSequence(ChapterEnding prevEnding) {
        this.activeChapter = Chapter.SPACESBETWEEN;
        this.secondaryScript = manager.getMirrorScript();
        secondaryScript.updateReusedScriptFlags();

        this.hasBlade = false;

        this.repeatActiveMenu = false;
        this.reverseDirection = false;
        this.withBlade = false;
        this.canSlayPrincess = false;
        this.canSlaySelf = false;
        this.canDropBlade = false;

        this.currentLocation = GameLocation.BEFOREMIRROR;
        this.mirrorPresent = true;
        this.mirrorGazeFlag = true;
        this.removeVoice(Voice.NARRATOR);

        // Ensure all chapters from this route are unlocked
        for (Chapter c : this.route) {
            manager.unlock(c);
        }

        switch (prevEnding) {
            case HINTOFFEELING:
            case LEAVEHERBEHIND:
            case NEWLEAFWEATHEREDBOOK:
                mainScript.runSection("surviveMirror");

                this.activeMenu = new OptionsMenu();
                activeMenu.add(new Option(this.manager, "approach", "[Approach the mirror.]"));

                this.repeatActiveMenu = true;
                while (repeatActiveMenu) {
                    switch (parser.promptOptionsMenu(activeMenu)) {
                        case "cApproachMirror":
                        case "approach":
                            this.repeatActiveMenu = false;
                            break;

                        default: this.giveDefaultFailResponse();
                    }
                }

                mainScript.runSection();
                break;

            case SOMETHINGTOREMEMBER:
                this.activeMenu = new OptionsMenu();
                activeMenu.add(new Option(this.manager, "approach", "[Approach the mirror.]"));

                this.repeatActiveMenu = true;
                while (repeatActiveMenu) {
                    switch (parser.promptOptionsMenu(activeMenu)) {
                        case "cApproachMirror":
                        case "approach":
                            this.repeatActiveMenu = false;
                            break;

                        default: this.giveDefaultFailResponse();
                    }
                }

                mainScript.runSection("contraFightMirror");
                break;

            case GODDESSUNRAVELED:
                this.activeMenu = new OptionsMenu();
                activeMenu.add(new Option(this.manager, "approach", "[Approach the mirror.]"));

                this.repeatActiveMenu = true;
                while (repeatActiveMenu) {
                    switch (parser.promptOptionsMenu(activeMenu)) {
                        case "cApproachMirror":
                        case "approach":
                            this.repeatActiveMenu = false;
                            break;

                        default: this.giveDefaultFailResponse();
                    }
                }

                mainScript.runSection("unravelMirror");
                break;

            case MOMENTOFCLARITY:
                this.activeMenu = new OptionsMenu();
                activeMenu.add(new Option(this.manager, "approach", "[Approach the mirror.]"));

                this.repeatActiveMenu = true;
                while (repeatActiveMenu) {
                    switch (parser.promptOptionsMenu(activeMenu)) {
                        case "cApproachMirror":
                        case "approach":
                            this.repeatActiveMenu = false;
                            break;

                        default: super.giveDefaultFailResponse();
                    }
                }

                mainScript.runClaimSection("mirror");
                break;

            case IFYOUCOULDUNDERSTAND:
            case WATERSTEEL:
            case FORMLESS:
            case NOEXIT:
                this.activeMenu = new OptionsMenu();
                activeMenu.add(new Option(this.manager, "approach", "[Approach the mirror.]"));

                this.repeatActiveMenu = true;
                while (repeatActiveMenu) {
                    switch (parser.promptOptionsMenu(activeMenu)) {
                        case "cApproachMirror":
                        case "approach":
                            this.repeatActiveMenu = false;
                            break;

                        default: super.giveDefaultFailResponse();
                    }
                }

                mainScript.runSection("approachEnd");
                break;

            default:
                if (prevEnding != ChapterEnding.GRACE) secondaryScript.runSection();
                secondaryScript.runSection("mirrorComment");

                boolean explore = false;
                boolean silence = false;
                this.activeMenu = new OptionsMenu();
                if (this.isFirstVessel) {
                    activeMenu.add(new Option(this.manager, "where", "(Explore) I don't know where she went, and I don't know how we'd even go about looking for her."));
                    activeMenu.add(new Option(this.manager, "gone", "(Explore) The Narrator is gone..."));
                    activeMenu.add(new Option(this.manager, "suggest", "(Explore) I think I'm supposed to look at the mirror."));
                    activeMenu.add(new Option(this.manager, "approach", "[Approach the mirror.]"));

                    boolean contraAsk = false;
                    this.repeatActiveMenu = true;
                    while (repeatActiveMenu) {
                        this.activeOutcome = parser.promptOptionsMenu(activeMenu);
                        switch (activeOutcome) {
                            case "where":
                                secondaryScript.runConditionalSection("where", contraAsk);
                                contraAsk = true;
                                break;

                            case "gone":
                                secondaryScript.runConditionalSection("gone", contraAsk);
                                contraAsk = true;
                                break;

                            case "suggest":
                                explore = true;
                                secondaryScript.runConditionalSection("suggest", this.nVoices());
                                break;

                            case "cApproachMirror":
                            case "approach":
                                secondaryScript.runSection("approachFirstVessel");

                                this.activeMenu = new OptionsMenu();
                                activeMenu.add(new Option(this.manager, "explore", "(Explore) \"The mirror never scared you before.\"", this.mirrorComment || this.touchedMirror));
                                activeMenu.add(new Option(this.manager, "ignore", "[Ignore him.]"));
                                
                                while (repeatActiveMenu) {
                                    switch (parser.promptOptionsMenu(activeMenu)) {
                                        case "explore":
                                            secondaryScript.runSection();
                                            if (!explore) secondaryScript.runConditionalSection("exploreJoin", this.nVoices());
                                            explore = true;
                                            break;

                                        case "cApproachMirror":
                                        case "ignore":
                                            this.repeatActiveMenu = false;
                                            break;

                                        default: this.giveDefaultFailResponse(activeOutcome);
                                    }
                                }

                                break;

                            default: this.giveDefaultFailResponse(activeOutcome);
                        }
                    }
                } else {
                    Condition canExplore = new Condition(true);
                    activeMenu.add(new Option(this.manager, "cruel", "(Explore) Of course you're scared. This is the end, for you. But it's not the end for me.", canExplore));
                    activeMenu.add(new Option(this.manager, "comfortA", "(Explore) It's going to be okay. Just trust me.", !manager.getMirrorScaredFlag(), canExplore));
                    activeMenu.add(new Option(this.manager, "comfortB", "(Explore) It's going to be okay. Just trust me. We've been here before, and you always get scared.", manager.getMirrorScaredFlag(), canExplore));
                    activeMenu.add(new Option(this.manager, "approach", "[Approach the mirror.]"));

                    this.repeatActiveMenu = true;
                    while (repeatActiveMenu) {
                        this.activeOutcome = parser.promptOptionsMenu(activeMenu);
                        switch (activeOutcome) {
                            case "cruel":
                                canExplore.set(false);
                                explore = true;
                                manager.incrementCruelCount();
                                secondaryScript.runSection("cruel");
                                break;

                            case "comfortA":
                            case "comfortB":
                                canExplore.set(false);
                                explore = true;
                                secondaryScript.runConditionalSection("explore", this.nVoices());

                                boolean repeatSub = true;
                                this.subMenu = new OptionsMenu();
                                subMenu.add(new Option(this.manager, "comfortA", "(Explore) It's not the end. Whatever's on the other side is going to be nice."));
                                subMenu.add(new Option(this.manager, "cruel", "(Explore) It's the end for you, but not for me."));
                                subMenu.add(new Option(this.manager, "comfortB", "(Explore) I'll see you on the other side. It's going to be okay."));
                                subMenu.add(new Option(this.manager, "approach", "[Approach the mirror.]"));

                                while (repeatSub) {
                                    switch (parser.promptOptionsMenu(subMenu)) {
                                        case "comfortA":
                                        case "comfortB":
                                            repeatSub = false;
                                            secondaryScript.runSection("comfort");
                                            break;
                                        
                                        case "cruel":
                                            repeatSub = false;
                                            manager.incrementCruelCount();
                                            secondaryScript.runSection("cruel");
                                            break;

                                        case "cApproachMirror":
                                        case "approach":
                                            this.repeatActiveMenu = false;
                                            repeatSub = false;
                                            silence = true;
                                            break;
                                    }
                                }

                                break;

                            case "cApproachMirror":
                            case "approach":
                                this.repeatActiveMenu = false;
                                break;

                            default: this.giveDefaultFailResponse(activeOutcome);
                        }
                    }
                }

            // You've now been to the mirror with the Voice of the Hero at least once
            manager.setMirrorScaredFlag();
                
            // Approach the mirror
            secondaryScript.runConditionalSection("approach", silence);
            if (!explore) secondaryScript.runConditionalSection("approachExplore", this.nVoices());
        }

        // Gaze into your reflection
        this.currentLocation = GameLocation.MIRROR;
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "gaze", "[Gaze into your reflection.]"));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "cGaze":
                case "gaze":
                    this.repeatActiveMenu = false;
                    break;

                default: super.giveDefaultFailResponse();
            }
        }

        this.mirrorPresent = false;

        ArrayList<Voice> voicesMet = new ArrayList<>();
        for (Voice v : Voice.TRUEVOICES) {
            if (v != Voice.HERO && this.hasVoice(v)) voicesMet.add(v);
        }

        manager.updateVoicesMet(voicesMet);
        this.clearVoices();

        if (prevEnding == ChapterEnding.IFYOUCOULDUNDERSTAND) {
            // No dialogue
        } if (prevEnding == ChapterEnding.WATERSTEEL || prevEnding == ChapterEnding.FORMLESS) {
            mainScript.runSection("mirrorGaze");
        } else if (this.isFirstVessel || (manager.nClaimedVessels() == 1 && manager.hasClaimedAnyVessel(Vessel.RAZORFULL, Vessel.RAZORHEART))) {
            secondaryScript.runSection("gazeFirst");
        } else {
            secondaryScript.runSection("gazeAgain");
        }

        System.out.println();
        switch (manager.nClaimedVessels()) {
            case 0:
                manager.unlock(Chapter.SPACESBETWEEN);
            case 1:
            case 2:
            case 3:
                manager.unlock("mirror" + manager.nClaimedVessels());
                secondaryScript.runSection("gaze" + manager.nClaimedVessels());
                this.theSpacesBetween(prevEnding);
                break;

            case 4: // Leads into Finale.finalMirror()
                secondaryScript.runSection("gazeFinal");

                this.activeMenu = new OptionsMenu(true);
                activeMenu.add(new Option(this.manager, "ask", "\"Are you me?\""));
                parser.promptOptionsMenu(activeMenu);
                
                secondaryScript.runSection();
                break;
        }
    }

    /**
     * Runs the encounter with the Shifting Mound after claiming each vessel, excluding the fifth and final vessel
     */
    protected void theSpacesBetween(ChapterEnding prevEnding) {
        this.activeChapter = Chapter.SPACESBETWEEN;
        this.mainScript = manager.getIntermissionScript();
        mainScript.updateReusedScriptFlags();

        this.currentLocation = GameLocation.PATH;
        mainScript.runConditionalSection(manager.nClaimedVessels());

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
                    mainScript.runSection("nowhereToGo");
                    break;
                    
                default: this.giveDefaultFailResponse(this.activeOutcome);
            }
        }

        this.currentLocation = GameLocation.HILL;
        mainScript.runSection("cabin");

        this.canApproachHer = true;
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "approach", "[Approach her.]"));

        this.repeatActiveMenu = true;
        while (this.repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);

            switch (this.activeOutcome) {
                case "cGoCabin":
                case "cApproachHer":
                case "approach":
                    this.repeatActiveMenu = false;
                    break;

                case "cGoPath":
                case "cGoFail":
                case "cEnterFail":
                case "cLeaveFail":
                    mainScript.runSection("nowhereToGo");
                    break;
                    
                default: this.giveDefaultFailResponse(this.activeOutcome);
            }
        }

        this.canApproachHer = false;
        this.withPrincess = true;

        switch (manager.nClaimedVessels()) { // Conversation
            case 0:
                this.canApproachHer = false;
                this.shiftingMoundTalk1(prevEnding);
                break;

            case 1:
                this.canApproachHer = false;
                mainScript.runSection("talkStart");
                this.shiftingMoundTalk2(prevEnding);
                break;

            case 2:
                this.canApproachHer = false;
                mainScript.runSection("talkStart");
                this.shiftingMoundTalk3(prevEnding);
                break;

            case 3:
                this.canApproachHer = false;
                mainScript.runSection("talkStart");
                this.shiftingMoundTalk4(prevEnding);
                break;
        }
    }

    /**
     * Runs the conversation with the Shifting Mound after claiming the first vessel
     */
    protected void shiftingMoundTalk1(ChapterEnding prevEnding) {
        this.secondaryScript = new Script(this.manager, this.parser, "Intermission/IntermissionTalk1");

        secondaryScript.runConditionalSection(manager.nVesselsAborted());

        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "dream", "(Explore) \"You're that thing I met in the space outside of the woods, aren't you? I thought that was a dream.\"", manager.nVesselsAborted() != 0));
        activeMenu.add(new Option(this.manager, "what", "(Explore) \"What are you?\""));
        activeMenu.add(new Option(this.manager, "fragile", "(Explore) \"The gift of a fragile vessel?\""));
        activeMenu.add(new Option(this.manager, "end", "(Explore) \"Is this the end of the world?\""));
        activeMenu.add(new Option(this.manager, "letOut", "(Explore) \"Let her out of there!\""));
        activeMenu.add(new Option(this.manager, "narrator", "(Explore) \"Do you know the Narrator?\""));
        activeMenu.add(new Option(this.manager, "trapped", "(Explore) \"Are you what sent me to slay the Princess? Are you what trapped me here?\""));
        activeMenu.add(new Option(this.manager, "worlds", "(Explore) \"Do you know about the worlds beyond this place?\""));
        activeMenu.add(new Option(this.manager, "princess", "(Explore) \"Are you the Princess?\""));
        activeMenu.add(new Option(this.manager, "familiar", "(Explore) \"Do we know each other?\"", manager.nVesselsAborted() == 0));
        activeMenu.add(new Option(this.manager, "whatNow", "\"What happens now?\""));
        activeMenu.add(manager.getIntermissionAttackMound());
        activeMenu.add(manager.getIntermissionAttackSelf());

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "dream":
                case "end":
                case "letOut":
                case "trapped":
                case "worlds":
                case "familiar":
                    secondaryScript.runSection(activeOutcome);
                    break;

                case "what":
                    secondaryScript.runSection("what");

                    this.subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "explore", "(Explore) \"Solitary lights? What do you mean?\""));
                    subMenu.add(new Option(this.manager, "youThink", "\"What do you think I am?\""));
                    subMenu.add(new Option(this.manager, "dunno", "\"I don't know what I am.\""));
                    subMenu.add(new Option(this.manager, "person", "\"I'm a person.\""));

                    switch (parser.promptOptionsMenu(subMenu)) {
                        case "explore":
                        case "person":
                            secondaryScript.runSection("what" + activeOutcome);
                            break;

                        case "youThink":
                        case "dunno":
                            secondaryScript.runSection("dunnoWhat");
                            break;
                    }

                    break;

                case "fragile":
                    secondaryScript.runSection("fragile");
                    this.giveVesselThoughts(prevEnding.getVessel());
                    break;

                case "narrator":
                    secondaryScript.runConditionalSection("narrator", manager.hasClaimedAnyVessel(Vessel.WOUNDEDWILD, Vessel.NETWORKWILD, Vessel.SPECTRE, Vessel.WRAITH, Vessel.TOWER, Vessel.APOTHEOSIS));
                    break;

                case "princess":
                    secondaryScript.runSection("princess");

                    this.subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "press", "\"But were you always the Princess, or are you just making her a part of yourself?\""));
                    subMenu.add(new Option(this.manager, "silent", "[Say nothing.]"));

                    if (parser.promptOptionsMenu(subMenu).equals("press")) secondaryScript.runSection();
                    break;

                case "whatNow":
                    this.repeatActiveMenu = false;
                    break;

                case "cSlayPrincessNoBladeFail": // Override: you don't need the blade
                    if (manager.getIntermissionAttackMound().hasBeenPicked()) {
                        mainScript.runSection("alreadyTried");
                        break;
                    }
                case "attackMound":
                    mainScript.runSection("attackMound");
                    break;

                case "cSlaySelfNoBladeFail": // Override: you don't need the blade
                    if (manager.getIntermissionAttackSelf().hasBeenPicked()) {
                        mainScript.runSection("alreadyTried");
                        break;
                    }
                case "attackSelf":
                    mainScript.runSection("attackSelf");
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // "What happens now?" continues here
        secondaryScript.runSection("whatNow");

        this.activeMenu = new OptionsMenu(true);
        activeMenu.add(new Option(this.manager, "kill", "(Explore) \"Aren't you scared that I'll find a way to kill you?\""));
        activeMenu.add(new Option(this.manager, "howMuch", "(Explore) \"How much will I forget?\""));
        activeMenu.add(new Option(this.manager, "pieces", "(Explore) \"How many more pieces of you do I have to find?\""));
        activeMenu.add(new Option(this.manager, "refuse", "(Explore) \"And what if I don't let you do this to me?\""));
        activeMenu.add(new Option(this.manager, "destroy", "(Explore) \"I was sent to slay the Princess to stop her from destroying the world. If I help you, is that what you're going to do?\""));
        activeMenu.add(new Option(this.manager, "wait", "\"I'm not going back.\" [Wait.]", activeMenu.get("refuse")));
        activeMenu.add(new Option(this.manager, "forget", "\"Okay. Make me forget.\""));

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            switch (parser.promptOptionsMenu(activeMenu)) {
                case "refuse":
                    manager.noRefuseExploreMound().set(false);
                case "kill":
                case "howMuch":
                case "pieces":
                    secondaryScript.runSection(activeOutcome);
                    break;

                case "destroy":
                    secondaryScript.runSection("destroy");

                    this.subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "press", "\"You're being semantic. What are you going to do if I help you?\""));
                    subMenu.add(new Option(this.manager, "silent", "[Let it be.]"));

                    if (parser.promptOptionsMenu(subMenu).equals("press")) secondaryScript.runSection();
                    break;

                case "wait":
                    secondaryScript.runSection("wait");
                    
                    // The original game has a creative gimmick here where, if you choose to wait forever, the game will quit out. When you reopen it, instead of starting on the main menu, the game will start right where you left off, and the Shifting Mound will comment on how long the game was closed for.
                    // Unfortunately, I have no idea how to replicate any of that here, and it would be insanely complicated to figure out (I would need to implement an entire save-and-load system...), so this option is just stuck being kind of weird and lame.

                    this.subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "wait", true, "[Wait forever.]"));
                    subMenu.add(new Option(this.manager, "forget", "\"Okay. I'm ready. Make me forget.\""));
                    parser.promptOptionsMenu(subMenu);

                    this.repeatActiveMenu = false;
                    break;

                case "forget":
                    this.repeatActiveMenu = false;
                    break;
            }
        }

        // Forget continues here
        secondaryScript.runSection("forget");
        mainScript.runSection("forget");
    }

    /**
     * Runs the conversation with the Shifting Mound after claiming the second vessel
     */
    protected void shiftingMoundTalk2(ChapterEnding prevEnding) {
        this.secondaryScript = new Script(this.manager, this.parser, "Intermission/IntermissionTalk2");
        boolean freed = manager.moundFreedom();
        boolean satisfied = manager.moundSatisfaction();

        if (manager.getDirectToMound() && manager.nVesselsAborted() != 0) secondaryScript.runConditionalSection("aborted", manager.nVesselsAborted());
        secondaryScript.runSection("start");
        
        Condition talked = new Condition();
        Condition canKind = new Condition(true);
        Condition canFeelings = new Condition(true);
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "riddle", "(Explore) \"Everything you say feels like a riddle. Can you give me a single straight answer?\"", talked, manager.canAskRiddleMound()));
        activeMenu.add(new Option(this.manager, "same", "(Explore) \"Are you the same being as you were before? How much have you changed?\""));
        activeMenu.add(new Option(this.manager, "change", "(Explore) \"What does it feel like to change like this?\""));
        activeMenu.add(new Option(this.manager, "want", "(Explore) \"When this is all done, do you know what you want to do?\""));
        activeMenu.add(new Option(this.manager, "threat", "(Explore) \"You know that at the end of this -- once you're finished -- I'm going to kill you, right?\""));
        activeMenu.add(new Option(this.manager, "wall", "(Explore) \"When I go back, it's as if an invisible wall closes around me. Why can I not do the same things I've done before?\""));
        activeMenu.add(new Option(this.manager, "kindA", "(Explore) \"You have been kinder to me than anyone else I've met. Thank you.\"", canKind));
        activeMenu.add(new Option(this.manager, "kindB", "(Explore) \"You have been kinder to me than anyone else I've met. Why?\"", canKind));
        activeMenu.add(new Option(this.manager, "requests", "(Explore) \"What do you want me to bring you next time?\""));
        activeMenu.add(new Option(this.manager, "thoughts", "(Explore) \"Do you have any thoughts on this vessel?\""));
        activeMenu.add(new Option(this.manager, "preferences", "(Explore) \"So you don't have any preferences on how you'd like to change or grow?\"", activeMenu.get("requests")));
        activeMenu.add(new Option(this.manager, "feelingsA", "(Explore) \"I don't want to hurt you, but the more times I go back, the worse I fear things will be.\"", canFeelings));
        activeMenu.add(new Option(this.manager, "feelingsB", "(Explore) \"What do you feel about me? These vessels I've been bringing you, I've hurt them.\"", canFeelings));
        activeMenu.add(new Option(this.manager, "howMany", "(Explore) \"How many more vessels do I need to bring you?\""));
        activeMenu.add(new Option(this.manager, "refuse", "(Explore) \"And what if I don't want to bring you any more vessels? What if I just wait here forever?\"", manager.noRefuseExploreMound()));
        activeMenu.add(new Option(this.manager, "pretension", "(Explore) \"Enough with all of this pretension. You're not actually saying anything.\"", talked, manager.canAskRiddleMound()));
        activeMenu.add(new Option(this.manager, "return", "\"I'm ready to go back.\""));
        activeMenu.add(manager.getIntermissionAttackMound());
        activeMenu.add(manager.getIntermissionAttackSelf());

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "threat":
                    manager.threatenedMound().set();
                case "same":
                case "change":
                case "wall":
                case "preferences":
                    talked.set();
                    secondaryScript.runSection(activeOutcome);
                    break;

                case "want":
                    talked.set();
                    secondaryScript.runConditionalSection("want", satisfied);
                    break;

                case "requests":
                    talked.set();
                    mainScript.runConditionalSection("requests", satisfied);
                    break;

                case "kindA":
                case "kindB":
                    talked.set();
                    canKind.set(false);
                    secondaryScript.runSection("kind");
                    break;

                case "thoughts":
                    talked.set();
                    this.giveVesselThoughts(prevEnding.getVessel());
                    break;

                case "feelingsA":
                case "feelingsB":
                    talked.set();
                    canFeelings.set(false);
                    
                    if (satisfied) {
                        secondaryScript.runConditionalSection("feelingsSatisfy", freed);
                    } else {
                        secondaryScript.runSection("feelingsDeny");
                    }

                    break;

                case "howMany":
                    talked.set();
                    
                    if (satisfied) {
                        secondaryScript.runSection("howManySatisfy");
                    } else {
                        secondaryScript.runConditionalSection("howManyDeny", freed);
                    }

                    break;

                case "refuse":
                    talked.set();
                    manager.noRefuseExploreMound().set(false);

                    secondaryScript.runSection("refuse");

                    this.subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "wait", true, "[Wait.]"));
                    subMenu.add(new Option(this.manager, "no", "[You have no need to wait.]"));
                    parser.promptOptionsMenu(subMenu);
                    break;

                case "riddle":
                case "pretension":
                    manager.canAskRiddleMound().set(false);
                    mainScript.runSection("riddle");
                    break;

                case "return":
                    this.repeatActiveMenu = false;
                    break;

                case "cSlayPrincessNoBladeFail": // Override: you don't need the blade
                    if (manager.getIntermissionAttackMound().hasBeenPicked()) {
                        mainScript.runSection("alreadyTried");
                        break;
                    }
                case "attackMound":
                    mainScript.runSection("attackMound");
                    break;

                case "cSlaySelfNoBladeFail": // Override: you don't need the blade
                    if (manager.getIntermissionAttackSelf().hasBeenPicked()) {
                        mainScript.runSection("alreadyTried");
                        break;
                    }
                case "attackSelf":
                    mainScript.runSection("attackSelf");
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Ending
        secondaryScript.runConditionalSection("end", satisfied);
        mainScript.runSection("forget");
    }

    /**
     * Runs the conversation with the Shifting Mound after claiming the third vessel
     */
    protected void shiftingMoundTalk3(ChapterEnding prevEnding) {
        this.secondaryScript = new Script(this.manager, this.parser, "Intermission/IntermissionTalk3");
        boolean satisfied = manager.moundSatisfaction();

        secondaryScript.runSection();
        
        Condition talked = new Condition();
        InverseCondition noThreat = manager.threatenedMound().getInverse();
        Condition localCanThreat = new Condition(true);
        InverseCondition noRequests = manager.askedRequestsMound().getInverse();
        Condition localCanRequests = new Condition(true);
        InverseCondition localAskedRequests = localCanRequests.getInverse();
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "riddle", "(Explore) \"Everything you say feels like a riddle. Can you give me a single straight answer?\"", talked, manager.canAskRiddleMound()));
        activeMenu.add(new Option(this.manager, "noExist", "(Explore) \"You can't be a contradiction. Contradictions don't exist.\""));
        activeMenu.add(new Option(this.manager, "how", "(Explore) \"How can you stand to be a contradiction?\""));
        activeMenu.add(new Option(this.manager, "worse", "(Explore) \"It doesn't matter how many times I go back. At least one of us always hurts the other. Doesn't that change you? Doesn't that make you worse?\""));
        activeMenu.add(new Option(this.manager, "vessel", "(Explore) \"What do you think of this vessel?\""));
        activeMenu.add(new Option(this.manager, "pretension", "(Explore) \"Enough with all of this pretension. You're not actually saying anything.\"", talked, manager.canAskRiddleMound()));
        activeMenu.add(new Option(this.manager, "threatA", "(Explore) \"I'm still planning to kill you once we're done with this.\"", manager.threatenedMound(), localCanThreat));
        activeMenu.add(new Option(this.manager, "threatB", "(Explore) \"You know that at the end of this -- once you're finished -- I'm going to kill you, right?\"", noThreat, localCanThreat));
        activeMenu.add(new Option(this.manager, "worlds", "(Explore) \"Do you know what happens to the worlds we leave behind?\""));
        activeMenu.add(new Option(this.manager, "want", "(Explore) \"Have you figured out what you'll want when we're finished?\""));
        activeMenu.add(new Option(this.manager, "requestsA", "(Explore) \"Do you still not care what I bring you next?\"", manager.askedRequestsMound(), localCanRequests));
        activeMenu.add(new Option(this.manager, "requestsB", "(Explore) \"What do you want me to bring you next time?\"", noRequests, localCanRequests));
        activeMenu.add(new Option(this.manager, "preferences", "(Explore) \"So you don't have any preferences on how you'd like to change or grow?\"", localAskedRequests));
        activeMenu.add(new Option(this.manager, "howMany", "(Explore) \"How many more vessels do I need to bring you?\""));
        activeMenu.add(new Option(this.manager, "refuse", "(Explore) \"I don't want to go back anymore. I just want to stay here. Forever if I have to.\"", manager.noRefuseExploreMound()));
        activeMenu.add(new Option(this.manager, "return", "\"I'm ready to go back.\""));
        activeMenu.add(manager.getIntermissionAttackMound());
        activeMenu.add(manager.getIntermissionAttackSelf());

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "noExist":
                case "how":
                case "howMany":
                case "worlds":
                    talked.set();
                    secondaryScript.runSection(activeOutcome);
                    break;

                case "worse":
                    talked.set();
                    secondaryScript.runSection("worse");

                    this.subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "more", "\"If anything, it makes me like you more. I don't know what that says about me.\""));
                    subMenu.add(new Option(this.manager, "distant", "\"No, not really. It all seems so distant as soon as I'm near you.\""));
                    subMenu.add(new Option(this.manager, "meh", "\"I have no opinion one way or another on the matter.\""));
                    subMenu.add(new Option(this.manager, "stop", "\"I just want it all to stop.\""));
                    subMenu.add(new Option(this.manager, "torture", "\"Yes. You're torturing me, and I hate it. I think I hate you.\""));
                    subMenu.add(new Option(this.manager, "silent", "[Remain silent.]"));

                    this.activeOutcome = parser.promptOptionsMenu(subMenu);
                    switch (activeOutcome) {
                        case "torture":
                        case "silent":
                            secondaryScript.runConditionalSection("worseConditional", satisfied);
                            break;

                        default: secondaryScript.runSection(activeOutcome + "Worse");
                    }

                    break;

                case "vessel":
                    talked.set();
                    this.giveVesselThoughts(prevEnding.getVessel());
                    break;

                case "threatB":
                    manager.threatenedMound().set();
                case "threatA":
                    talked.set();
                    localCanThreat.set(false);
                    secondaryScript.runConditionalSection("threat", satisfied);
                    break;

                case "want":
                    talked.set();
                    secondaryScript.runConditionalSection("want", satisfied);
                    break;

                case "requestsA":
                    talked.set();
                    localCanRequests.set(false);
                    manager.askedRequestsMound().set();
                    mainScript.runConditionalSection("requests", satisfied);
                    break;

                case "requestsB":
                    talked.set();
                    localCanRequests.set(false);
                    manager.askedRequestsMound().set();
                    secondaryScript.runConditionalSection("requests", satisfied);
                    break;

                case "preferences":
                    secondaryScript.runConditionalSection("preferences", satisfied);
                    break;

                case "refuse":
                    talked.set();
                    manager.noRefuseExploreMound().set(false);

                    secondaryScript.runSection("refuse");

                    this.subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "wait", true, "[Continue to wait. Forever.]"));
                    subMenu.add(new Option(this.manager, "no", "[There is no waiting forever.]"));
                    parser.promptOptionsMenu(subMenu);

                    break;

                case "riddle":
                case "pretension":
                    manager.canAskRiddleMound().set(false);
                    mainScript.runSection("riddle");
                    break;

                case "return":
                    this.repeatActiveMenu = false;
                    break;

                case "cSlayPrincessNoBladeFail": // Override: you don't need the blade
                    if (manager.getIntermissionAttackMound().hasBeenPicked()) {
                        mainScript.runSection("alreadyTried");
                        break;
                    }
                case "attackMound":
                    mainScript.runSection("attackMound");
                    break;

                case "cSlaySelfNoBladeFail": // Override: you don't need the blade
                    if (manager.getIntermissionAttackSelf().hasBeenPicked()) {
                        mainScript.runSection("alreadyTried");
                        break;
                    }
                case "attackSelf":
                    mainScript.runSection("attackSelf");
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Ending
        secondaryScript.runSection("end");
        mainScript.runSection("forget");
    }

    /**
     * Runs the conversation with the Shifting Mound after claiming the fourth vessel
     */
    protected void shiftingMoundTalk4(ChapterEnding prevEnding) {
        this.secondaryScript = new Script(this.manager, this.parser, "Intermission/IntermissionTalk4");
        Condition freed = new Condition(manager.moundFreedom());
        InverseCondition kept = freed.getInverse();
        boolean satisfied = manager.moundSatisfaction();

        secondaryScript.runConditionalSection(freed);
        
        Condition talked = new Condition();
        InverseCondition noTalk = talked.getInverse();
        InverseCondition refuseExplored = manager.noRefuseExploreMound().getInverse();
        Condition noLocalRefuse = new Condition(true);
        this.activeMenu = new OptionsMenu();
        activeMenu.add(new Option(this.manager, "riddle", "(Explore) \"Everything you say feels like a riddle. Can you give me a single straight answer?\"", talked, manager.canAskRiddleMound()));
        activeMenu.add(new Option(this.manager, "people", "(Explore) \"Do you think there are people out there?\"", kept));
        activeMenu.add(new Option(this.manager, "rhetorical", "(Explore) \"Is that a rhetorical question? Do you know? Do you want to tell me?\"", freed, noTalk));
        activeMenu.add(new Option(this.manager, "trees", "(Explore) \"There's trees, and stars. And there are people, I think. At least there are supposed to be people.\"", freed));
        activeMenu.add(new Option(this.manager, "real", "(Explore) \"Do you think that anything is real out there? Do you think that we're real?\""));
        activeMenu.add(new Option(this.manager, "vessel", "(Explore) \"Do you have thoughts on this vessel?\""));
        activeMenu.add(new Option(this.manager, "awaken", "(Explore) \"Do you know what's going to happen when you awaken?\""));
        activeMenu.add(new Option(this.manager, "narrator", "(Explore) \"When you send me back, I'm not alone. There are voices that speak to me. Some of them are me, but one of them is something else. I call him The Narrator, and he wants me to kill you. Do you have a Narrator? Have the vessels had one?\""));
        activeMenu.add(new Option(this.manager, "howMany", "(Explore) \"How many more vessels do I need to bring you?\""));
        activeMenu.add(new Option(this.manager, "requests", "(Explore) \"If this is the last time, is there anything you would like me to bring you?\"", activeMenu.get("howMany")));
        activeMenu.add(new Option(this.manager, "threat", "(Explore) \"You know we're going to fight when this is over. Do you really want me to bring you the last vessel?\"", activeMenu.get("howMany"), manager.threatenedMound()));
        activeMenu.add(new Option(this.manager, "refuseA", "(Explore) \"If this is the last stage before your completion, then I'm not going back. I'm just going to stay here.\"", activeMenu.get("howMany"), refuseExplored, noLocalRefuse));
        activeMenu.add(new Option(this.manager, "refuseB", "(Explore) \"If this is the last stage before your completion, then I'm not going back. I'm just going to stay here. Forever if I have to.\"", activeMenu.get("howMany"), manager.noRefuseExploreMound(), noLocalRefuse));
        activeMenu.add(new Option(this.manager, "return", "\"I'm ready to go back.\""));
        activeMenu.add(manager.getIntermissionAttackMound());
        activeMenu.add(manager.getIntermissionAttackSelf());

        this.repeatActiveMenu = true;
        while (repeatActiveMenu) {
            this.activeOutcome = parser.promptOptionsMenu(activeMenu);
            switch (activeOutcome) {
                case "rhetorical":
                case "trees":
                case "real":
                case "howMany":
                case "requests":
                case "people":
                case "awaken":
                case "threat":
                    talked.set();
                    secondaryScript.runConditionalSection(activeOutcome, satisfied);
                    break;

                case "vessel":
                    talked.set();
                    this.giveVesselThoughts(prevEnding.getVessel());
                    break;

                case "narrator":
                    talked.set();

                    if (manager.hasClaimedAnyVessel(Vessel.WOUNDEDWILD, Vessel.NETWORKWILD, Vessel.SPECTRE, Vessel.WRAITH, Vessel.TOWER, Vessel.APOTHEOSIS)) {
                        secondaryScript.runSection("narratorMet");
                    } else {
                        secondaryScript.runConditionalSection("narratorNotMet", freed);
                    }

                    this.subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "fact", "(Explore) \"He does. I don't know why, but I know this for a fact.\""));
                    subMenu.add(new Option(this.manager, "dunno", "(Explore) \"He does. I don't know what I'm going to do when I find him.\""));
                    subMenu.add(new Option(this.manager, "answers", "(Explore) \"He does. And when I find him, you and I are finally going to have answers.\""));
                    subMenu.add(new Option(this.manager, "kill", "(Explore) \"He does. And when I find him, I'm going to kill him.\""));
                    subMenu.add(new Option(this.manager, "silent", "(Explore) He does. But you're going to keep that to yourself."));

                    secondaryScript.runConditionalSection(parser.promptOptionsMenu(subMenu) + "Narrator", freed);
                    break;

                case "refuseA":
                case "refuseB":
                    talked.set();
                    secondaryScript.runSection(activeOutcome);

                    this.subMenu = new OptionsMenu(true);
                    subMenu.add(new Option(this.manager, "wait", true, "[Wait forever.]"));
                    subMenu.add(new Option(this.manager, "no", "[There is no waiting forever.]"));
                    parser.promptOptionsMenu(subMenu);
                    break;

                case "riddle":
                    manager.canAskRiddleMound().set(false);
                    mainScript.runSection("riddle");
                    break;

                case "return":
                    this.repeatActiveMenu = false;
                    break;

                case "cSlayPrincessNoBladeFail": // Override: you don't need the blade
                    if (manager.getIntermissionAttackMound().hasBeenPicked()) {
                        mainScript.runSection("alreadyTried");
                        break;
                    }
                case "attackMound":
                    mainScript.runSection("attackMound");
                    break;

                case "cSlaySelfNoBladeFail": // Override: you don't need the blade
                    if (manager.getIntermissionAttackSelf().hasBeenPicked()) {
                        mainScript.runSection("alreadyTried");
                        break;
                    }
                case "attackSelf":
                    mainScript.runSection("attackSelf");
                    break;

                default: this.giveDefaultFailResponse(activeOutcome);
            }
        }

        // Ending
        secondaryScript.runSection("end");
        mainScript.runSection("forget");
    }

    /**
     * The Shifting Mound gives her thoughts on a given Vessel
     * @param v the Vessel to comment on
     */
    protected void giveVesselThoughts(Vessel v) {
        switch (v) {
            // Chapter II
            case ADVERSARY:
                mainScript.runSection("adversary");
                break;
            case TOWER:
                mainScript.runSection("tower");
                break;
            case SPECTRE:
                mainScript.runSection("spectre");
                break;
            case NIGHTMARE:
                mainScript.runSection("nightmare");
                break;
            case BEAST:
                mainScript.runSection("beast");
                break;
            case WITCH:
                mainScript.runSection("witch");
                break;
            case STRANGER:
                if (this.isFirstVessel) {
                    mainScript.runSection("stranger");
                } else {
                    mainScript.runSection("strangerFirst");
                }
                break;
            case PRISONERHEAD:
                mainScript.runSection("prisonerHead");
                break;
            case PRISONER:
                mainScript.runSection("prisoner");
                break;
            case DAMSEL:
                mainScript.runSection("damsel");
                break;
            case DECONDAMSEL:
                mainScript.runSection("deconDamsel");
                break;

            // Chapter III or IV
            case NEEDLE:
                mainScript.runSection("needle");
                break;
            case FURY:
            case REWOUNDFURY:
                mainScript.runSection("fury");
                break;
            case APOTHEOSIS:
                mainScript.runSection("apotheosis");
                break;
            case PATD:
            case STENCILPATD:
                mainScript.runSection("dragon");
                break;
            case WRAITH:
                mainScript.runSection("wraith");
                break;
            case CLARITY:
                mainScript.runSection("clarity");
                break;
            case RAZORFULL:
            case RAZORHEART:
                mainScript.runSection("razor");
                break;
            case DEN:
                mainScript.runSection("den");
                // might need another Vessel for den for an extra line??
                break;
            case NETWORKWILD:
                mainScript.runSection("nWild");
                break;
            case WOUNDEDWILD:
                mainScript.runSection("wWild");
                break;
            case THORN:
                mainScript.runSection("thorn");
                break;
            case WATCHFULCAGE:
                mainScript.runSection("cageWatchful");
                break;
            case OPENCAGE:
                mainScript.runSection("cageOpen");
                break;
            case DROWNEDGREY:
                mainScript.runSection("dGrey");
                break;
            case BURNEDGREY:
                mainScript.runSection("bGrey");
                break;
            case HAPPY:
            case HAPPYDRY:
                mainScript.runSection("happy");
                break;
        }
    }


    
    /**
     * The player aborts the current Chapter (and therefore the current Cycle as well), contributing to the Oblivion ending
     */
    protected void abortVessel(boolean lateJoin) {
        this.secondaryScript = new Script(this.manager, this.parser, "Intermission/AbortVessel");

        if (!lateJoin) {
            secondaryScript.runConditionalSection(activeChapter.getNumber(), activeChapter.getID());
            secondaryScript.runSection("voices");
        }

        this.clearVoices();
        secondaryScript.runSection("unwound");
        
        switch (manager.nVesselsAborted()) {
            case 0:
                if (this.isFirstVessel) {
                    secondaryScript.runSection("abort0");

                    this.activeMenu = new OptionsMenu(true);
                    activeMenu.add(new Option(this.manager, "wake", "This is a nightmare. Wake up."));
                    activeMenu.add(new Option(this.manager, "embrace", "Embrace the thoughts constricting you."));

                    switch (parser.promptOptionsMenu(activeMenu)) {
                        case "wake":
                            secondaryScript.runSection("firstAbortDream");
                            break;

                        case "embrace":
                            secondaryScript.runSection();
                            break;
                    }
                } else {
                    secondaryScript.runSection("abort0VesselClaimed");
                }

                break;


            case 1:
            case 2:
            case 3:
            case 4:
                secondaryScript.runSection("abort" + manager.nClaimedVessels());
                break;


            case 5: // Oblivion ending
                manager.addToPlaylist("Oblivion");
                secondaryScript.runSection("oblivion");

                this.activeMenu = new OptionsMenu(true);
                activeMenu.add(new Option(this.manager, "exist", "[Exist.]", 0));
                activeMenu.add(new Option(this.manager, "fade", "[Consciousness fades away.]"));

                for (int i = 0; i < 4; i++) {
                    secondaryScript.runSection("oblivionLoop");

                    if (i == 3) {
                        switch (parser.promptOptionsMenu(activeMenu)) {
                            case "exist":
                                // You can keep doing this forever, if you want
                                i -= 1;
                                break;

                            case "fade":
                                // End the game
                                break;
                        }
                    }
                }

                break;
        }

        if (manager.nVesselsAborted() != 5) {
            secondaryScript.runSection("endLoop");
            manager.unlock("abort" + manager.nVesselsAborted());
        }
    }

}
