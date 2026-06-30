package moonlit.dialogue;

import java.util.List;
import moonlit.dialogue.DialogueLine.Cue;
import moonlit.dialogue.DialogueLine.PortraitSide;

/**
 * Story script for the Starry Illusion three-boss route.
 */
public final class DialogueScripts {
    private DialogueScripts() {
    }

    public static DialogueScene miniBossOneIntro(Runnable onComplete) {
        return new DialogueScene("mini_boss_one_intro", List.of(
                system("A foxfire envoy slips out of the cloud road and blocks the shrine path."),
                bossOne("The star road ahead is sealed. Turn back while your charms still glow."),
                reimu("Nice warning, but I came here exactly because the sky is acting strange."),
                system("Small Boss 1 battle starts.", Cue.BOSS_THEME_SHAKE)), onComplete);
    }

    public static DialogueScene miniBossOneDefeated(Runnable onComplete) {
        return new DialogueScene("mini_boss_one_defeated", List.of(
                system("The foxfire envoy scatters into violet sparks.", Cue.STOP_MUSIC),
                bossOne("The lantern keeper is deeper in. She knows who opened the star gate."),
                reimu("Then I keep flying. Thanks for the shortcut.")), onComplete);
    }

    public static DialogueScene miniBossTwoIntro(Runnable onComplete) {
        return new DialogueScene("mini_boss_two_intro", List.of(
                system("Lantern light folds into butterfly wings above the moonlit road."),
                bossTwo("The second gate listens only to rhythm. Dodge beautifully, shrine maiden."),
                reimu("I prefer simple answers, but fine. I can be beautiful and impatient."),
                system("Small Boss 2 battle starts.", Cue.BOSS_THEME_SHAKE)), onComplete);
    }

    public static DialogueScene miniBossTwoDefeated(Runnable onComplete) {
        return new DialogueScene("mini_boss_two_defeated", List.of(
                system("The lantern butterfly dims and points toward the upper star field.", Cue.STOP_MUSIC),
                bossTwo("The Star Oracle waits beyond the cloud break. Do not mistake her light for mercy."),
                reimu("If she caused this incident, mercy can wait until after the danmaku.")), onComplete);
    }

    public static DialogueScene finalBossIntro(Runnable onComplete) {
        return new DialogueScene("final_boss_intro", List.of(
                system("The clouds split open. A star gate turns slowly above the road."),
                finalBoss("So the shrine maiden crossed both lantern seals. Impressive."),
                reimu("You are the one pouring strange light across the sky? Then this ends here."),
                finalBoss("The starlight only answers a wish. If you want the source, break my constellation first.",
                        Cue.BOSS_THEME_SHAKE),
                reimu("Good. I was already planning to break something."),
                system("Spell Card Attack! Final boss battle begins.")), onComplete);
    }

    public static DialogueScene finalBossDefeated(Runnable onComplete) {
        return new DialogueScene("final_boss_defeated", List.of(
                system("The Star Oracle falls, and hostile bullets dissolve into green light.", Cue.STOP_MUSIC),
                finalBoss("The gate is closing. Your path back to the shrine is clear."),
                reimu("Next time, try a normal invitation instead of rewriting the whole sky."),
                system("Stage Clear.")), onComplete);
    }

    public static DialogueScene midbossEncounter(Runnable onComplete) {
        return miniBossOneIntro(onComplete);
    }

    public static DialogueScene postBattle(Runnable onComplete) {
        return finalBossDefeated(onComplete);
    }

    private static DialogueLine reimu(String text) {
        return new DialogueLine("Reimu", text, PortraitSide.LEFT);
    }

    private static DialogueLine bossOne(String text) {
        return new DialogueLine("Kitsune Envoy", text, PortraitSide.RIGHT);
    }

    private static DialogueLine bossTwo(String text) {
        return new DialogueLine("Lantern Butterfly", text, PortraitSide.RIGHT);
    }

    private static DialogueLine finalBoss(String text) {
        return new DialogueLine("Star Oracle", text, PortraitSide.RIGHT);
    }

    private static DialogueLine finalBoss(String text, Cue cue) {
        return new DialogueLine("Star Oracle", text, PortraitSide.RIGHT, cue);
    }

    private static DialogueLine system(String text) {
        return new DialogueLine("System", text, PortraitSide.NONE);
    }

    private static DialogueLine system(String text, Cue cue) {
        return new DialogueLine("System", text, PortraitSide.NONE, cue);
    }
}
