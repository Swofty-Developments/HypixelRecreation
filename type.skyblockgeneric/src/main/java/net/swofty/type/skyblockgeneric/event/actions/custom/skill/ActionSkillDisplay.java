package net.swofty.type.skyblockgeneric.event.actions.custom.skill;

import net.swofty.commons.text.Text;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.event.custom.SkillUpdateEvent;
import net.swofty.type.skyblockgeneric.user.SkyBlockActionBar;

public class ActionSkillDisplay implements HypixelEventClass {


    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(SkillUpdateEvent event) {
        double oldValue = event.getOldValueRaw();
        double newValue = event.getNewValueRaw();
        double difference = newValue - oldValue;

        if (difference <= 0) return;

        int currentLevel = event.getPlayer().getSkills().getCurrentLevel(event.getSkillCategory());
        if (currentLevel >= event.getSkillCategory().asCategory().getHighestLevel()) return;
        int currentRequirement = event.getSkillCategory().asCategory().getReward(currentLevel + 1).requirement();

        SkyBlockActionBar.getFor(event.getPlayer()).addReplacement(
                SkyBlockActionBar.BarSection.DEFENSE,
                Text.of("<3>+{} {} ({}/{})", difference, event.getSkillCategory(),
                        event.getNewValueCumulative(), currentRequirement),
                20,
                2
        );
    }
}
