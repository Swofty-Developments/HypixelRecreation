package net.swofty.type.bedwarsgame.item.impl;

import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.CustomData;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class LuckyCombatEffects {
    private static final Map<UUID, Long> JUSTICE_HEAL_COOLDOWN = new HashMap<>();

    private LuckyCombatEffects() {
    }

    public static void handle(BedWarsPlayer attacker, Entity target) {
        ItemStack item = attacker.getItemInMainHand();
        String action = action(item);
        if (action == null) return;
        LivingEntity livingTarget = target instanceof LivingEntity living ? living : null;

        switch (action) {
            case "angel_sword" -> {
                if (livingTarget != null) {
                    livingTarget.damage(Damage.fromPlayer(attacker, 14f));
                }
                knockTarget(attacker, target, 14, 5);
                consumeMainHand(attacker, item);
                attacker.sendMessage("<c>Angel of Death's Sword shattered.");
            }
            case "knockback_slimeball" -> {
                knockTarget(attacker, target, 20, 6);
                consumeMainHand(attacker, item);
            }
            case "sword_justice" -> {
                long now = System.currentTimeMillis();
                long lastHeal = JUSTICE_HEAL_COOLDOWN.getOrDefault(attacker.getUuid(), 0L);
                if (now - lastHeal >= 2500L) {
                    attacker.setHealth((float) Math.min(20f, attacker.getHealth() + 2));
                    JUSTICE_HEAL_COOLDOWN.put(attacker.getUuid(), now);
                }
            }
            case "scythe" -> {
                if (livingTarget != null) livingTarget.damage(Damage.fromPlayer(attacker, 3f));
            }
            case "sharp_spoon" -> {
                if (livingTarget != null) livingTarget.damage(Damage.fromPlayer(attacker, 2f));
            }
            case "vampire_weapon" -> {
                if (livingTarget != null) livingTarget.damage(Damage.fromPlayer(attacker, 2f));
                attacker.addEffect(new Potion(PotionEffect.REGENERATION, (byte) 0, 40));
            }
            default -> {
            }
        }
    }

    public static ItemStack weapon(String name, Material material, String action, String... lore) {
        return new LuckySpecialItem().item(name, material, action, 0, lore);
    }

    public static String action(ItemStack item) {
        CustomData data = item.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        String itemId = data.getTag(net.minestom.server.tag.Tag.String("item"));
        if (!"lucky_special".equals(itemId)) return null;
        return data.getTag(LuckySpecialItem.ACTION_TAG);
    }

    private static void consumeMainHand(BedWarsPlayer attacker, ItemStack item) {
        attacker.setItemInHand(PlayerHand.MAIN, item.consume(1));
    }

    private static void knockTarget(BedWarsPlayer attacker, Entity target, double horizontal, double vertical) {
        Vec direction = target.getPosition().sub(attacker.getPosition()).asVec().normalize();
        target.setVelocity(target.getVelocity().add(direction.mul(horizontal)).add(0, vertical, 0));
    }
}
