package net.swofty.type.skywarsgame.luckyblock.oprule;

import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.swofty.commons.text.Text;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.swofty.type.skywarsgame.game.SkywarsGame;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

import java.util.Random;
import java.util.function.BiConsumer;

/**
 * OP Rules are game-wide modifiers activated by breaking an OP Rule Lucky Block.
 * Only one OP Rule can be active per game.
 * Based on Hypixel SkyWars Lucky Block mode.
 */
@Getter
public enum OPRule {
    DOUBLE_JUMPS(
            "Double Jumps",
            "All players can double jump!",
            NamedTextColor.GREEN,
            (game, activator) -> {
                // Double jump is handled by the OPRuleManager checking if this rule is active
                game.broadcastMessage(Text.of("<a>Double Jump enabled! Press space twice to fly!"));
            }
    ),

    TRIPLE_HP(
            "Triple HP",
            "All players have triple health!",
            NamedTextColor.RED,
            (game, activator) -> {
                for (SkywarsPlayer player : game.getAlivePlayers()) {
                    // Grant absorption hearts instead of modifying max health
                    // which provides extra hearts on top of current health
                    player.addEffect(new Potion(PotionEffect.ABSORPTION, (byte) 9, Integer.MAX_VALUE));
                    player.addEffect(new Potion(PotionEffect.REGENERATION, (byte) 1, 600));
                }
            }
    ),

    SPEED_IV(
            "Speed IV",
            "All players have Speed IV!",
            NamedTextColor.AQUA,
            (game, activator) -> {
                for (SkywarsPlayer player : game.getAlivePlayers()) {
                    player.addEffect(new Potion(PotionEffect.SPEED, (byte) 3, Integer.MAX_VALUE));
                }
            }
    ),

    TNT_RAIN(
            "TNT Rain",
            "TNT is raining from the sky!",
            NamedTextColor.RED,
            (game, activator) -> {
                // TNT rain is handled by the OPRuleManager with a scheduled task
                game.broadcastMessage(Text.of("<c>Watch out! TNT is falling from the sky!"));
            }
    ),

    CREEPER_INFESTATION(
            "Creeper Infestation",
            "Creepers are spawning everywhere!",
            NamedTextColor.GREEN,
            (game, activator) -> {
                // Creeper spawning is handled by the OPRuleManager with a scheduled task
                game.broadcastMessage(Text.of("<a>Creepers are invading the map!"));
            }
    ),

    THROW_FURTHER(
            "Throw Projectiles Further",
            "All projectiles travel twice as far!",
            NamedTextColor.YELLOW,
            (game, activator) -> {
                // Projectile velocity modification is handled in projectile launch event
                game.broadcastMessage(Text.of("<e>Projectiles now travel twice as far!"));
            }
    );

    private static final Random RANDOM = new Random();

    private final String displayName;
    private final String description;
    private final NamedTextColor color;
    private final BiConsumer<SkywarsGame, SkywarsPlayer> activationAction;

    OPRule(String displayName, String description, NamedTextColor color,
           BiConsumer<SkywarsGame, SkywarsPlayer> activationAction) {
        this.displayName = displayName;
        this.description = description;
        this.color = color;
        this.activationAction = activationAction;
    }

    /**
     * Activate this OP Rule for a game.
     */
    public void activate(SkywarsGame game, SkywarsPlayer activator) {
        activationAction.accept(game, activator);
    }

    /**
     * Get the announcement text for this rule.
     */
    public Text getAnnouncement() {
        return Text.of("""

                <color:{0}><l>★ OP RULE ACTIVATED ★</l></color>
                <color:{0}>{1}</color>
                <7>{2}</7>
                """, color, displayName, description);
    }

    /**
     * Get a random OP Rule.
     */
    public static OPRule random() {
        OPRule[] rules = values();
        return rules[RANDOM.nextInt(rules.length)];
    }
}
