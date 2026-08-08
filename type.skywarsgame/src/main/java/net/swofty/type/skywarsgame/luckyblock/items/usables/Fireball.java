package net.swofty.type.skywarsgame.luckyblock.items.usables;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockConsumable;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

import java.util.List;

public class Fireball implements LuckyBlockConsumable {

    @Override
    public String getId() {
        return "fireball";
    }

    @Override
    public String getDisplayName() {
        return "Fireball";
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStack.builder(Material.FIRE_CHARGE)
                .amount(8)
                .customName(Component.text(getDisplayName(), NamedTextColor.RED)
                        .decoration(TextDecoration.ITALIC, false)
                        .decoration(TextDecoration.BOLD, true))
                .lore(List.of(
                        Component.text("Launch an explosive fireball!", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.empty(),
                        Component.text("Right-click to use!", NamedTextColor.YELLOW)
                                .decoration(TextDecoration.ITALIC, false)
                ))
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, getId())
                .build();
    }

    @Override
    public void onConsume(SkywarsPlayer player) {
        Polyp.getInstance().services().projectiles().launch(ProjectileSnapshot.of(player,
                io.github.term4.polyp.mechanics.projectile.types.Fireball.INSTANCE));
        player.playSound(Sound.sound(Key.key("minecraft:entity.ghast.shoot"), Sound.Source.PLAYER, 1f, 1f), Sound.Emitter.self());
    }
}
