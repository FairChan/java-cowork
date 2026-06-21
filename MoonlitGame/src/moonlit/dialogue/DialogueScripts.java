package moonlit.dialogue;

import java.util.List;
import moonlit.dialogue.DialogueLine.Cue;
import moonlit.dialogue.DialogueLine.PortraitSide;

/**
 * Story script for Starry Illusion encounter scenes.
 */
public final class DialogueScripts {
    private DialogueScripts() {
    }

    public static DialogueScene midbossEncounter(Runnable onComplete) {
        return new DialogueScene("midboss_encounter", List.of(
                system("魔理沙从屏幕下方冲出，悬停在急速倒退的云层中央。"),
                reimu("我就知道，只要天上掉下什么稀奇古怪的东西，肯定能在这附近逮到你。"),
                marisa("哟！这不是灵梦嘛。大半夜的出来兜风？不过这里的“流星”已经被我包场了，想要的话自己去更高的地方捡吧 DA ZE！"),
                reimu("谁要捡那些危险的石头啊！快给我停下，你这家伙身上的魔力都要满得溢出来了！"),
                marisa("嘿嘿，既然被你发现了，那就让你见识一下刚收集到的“星之魔力”吧！只要接下我这招，我就放你过去！", Cue.SHAKE),
                system("弹幕战开始：星符「Meteoric Shower」。")), onComplete);
    }

    public static DialogueScene finalBossIntro(Runnable onComplete) {
        return new DialogueScene("final_boss_intro", List.of(
                system("云层被突破，背景变成璀璨的宇宙星海。魔理沙在屏幕上方等待，灵梦飞入。"),
                reimu("呼……总算追上了。飞得这么高，连空气都变稀薄了。"),
                reimu("好了，魔理沙，异变调查时间到了。老实交代，这满天乱掉的星星，是不是你用八卦炉搞出来的恶作剧？"),
                marisa("哈？你太高估我了吧。虽然我也很想把整个星空据为己有，但这异变可不是我干的。"),
                marisa("我只是个路过的、勤奋的“星星清道夫”而已。"),
                reimu("不管是不是你，你现在吸收了这么多异常的魔力，已经是个巨大的危险源了。我要在这里把你没收的星星全部砸碎。"),
                marisa("哦？口气不小嘛。这可是我辛辛苦苦收集来的宝贝，凭什么让你砸碎？"),
                marisa("再说了，你难道没感觉到吗？在这个高度，连重力都束缚不了我们！"),
                marisa("就用你来测试一下吧！由纯粹的星光压缩而成的、最棒的火力！", Cue.BOSS_THEME_SHAKE),
                reimu("大言不惭。别以为捡了点星星就能赢过博丽的巫女。待会儿被揍哭了，可别怪我没提醒你！"),
                marisa("弹幕是力量（Power），也是华丽的星辰（Star）！接招吧，灵梦！"),
                system("Spell Card Attack! 关底 Boss 战正式开始。")), onComplete);
    }

    public static DialogueScene postBattle(Runnable onComplete) {
        return new DialogueScene("post_battle", List.of(
                system("魔理沙被击败，冒着黑烟转圈掉落一小段距离后稳住。", Cue.STOP_MUSIC),
                marisa("痛痛痛……你下手也太狠了吧。好不容易收集的星之结晶，全在这场战斗里烧光了。"),
                reimu("这叫防患于未然。如果你带着那种状态回神社，肯定会把我的塞钱箱炸飞的。"),
                reimu("既然不是你干的，那也就是说，真正的犯人还在更高的地方咯？"),
                marisa("啊，没错。刚才在收集星星的时候，我看到光芒都是从“那个方向”倾泻下来的。感觉有某种不得了的家伙在上面呢。"),
                marisa("要不是你拦着我，我早就冲上去把源头也给搬空了。"),
                reimu("行了行了，你赶紧回地面去吧。顺便帮我看着点神社。"),
                reimu("真是的，今晚看来是彻底别想睡觉了。"),
                marisa("一路顺风啊！要是遇到什么好东西，记得给我留一份——！"),
                system("灵梦向屏幕上方加速飞去，第四面结束，切入第五面。")), onComplete);
    }

    private static DialogueLine reimu(String text) {
        return new DialogueLine("灵梦", text, PortraitSide.LEFT);
    }

    private static DialogueLine marisa(String text) {
        return new DialogueLine("魔理沙", text, PortraitSide.RIGHT);
    }

    private static DialogueLine marisa(String text, Cue cue) {
        return new DialogueLine("魔理沙", text, PortraitSide.RIGHT, cue);
    }

    private static DialogueLine system(String text) {
        return new DialogueLine("系统", text, PortraitSide.NONE);
    }

    private static DialogueLine system(String text, Cue cue) {
        return new DialogueLine("系统", text, PortraitSide.NONE, cue);
    }
}