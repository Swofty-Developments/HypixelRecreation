package net.swofty.type.skyblockgeneric.event.actions.item;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.item.ItemStack;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.entity.ExperienceBottleEntity;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.ExperienceBottleComponent;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public final class ActionThrowExperienceBottle implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerUseItemEvent event) {
        SkyBlockItem item = new SkyBlockItem(event.getItemStack());
        if (!item.hasComponent(ExperienceBottleComponent.class)) return;
        event.setCancelled(true);
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        int level = player.getSkills().getCurrentLevel(SkillCategories.ENCHANTING);
        long experience = Math.round(item.getComponent(ExperienceBottleComponent.class).getBaseExperience()
                * (1 + level * .05));
        ExperienceBottleEntity entity = new ExperienceBottleEntity(player, experience);
        Pos position = player.getPosition();
        entity.setInstance(player.getInstance(), position.add(0, 1.5, 0));
        ItemStack held = player.getItemInMainHand();
        player.setItemInMainHand(held.amount() > 1 ? held.withAmount(held.amount() - 1) : ItemStack.AIR);
    }
}
