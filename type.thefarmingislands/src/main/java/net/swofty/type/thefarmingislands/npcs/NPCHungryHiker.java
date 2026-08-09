package net.swofty.type.thefarmingislands.npcs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.garden.progression.GardenProgressionReward;
import net.swofty.type.skyblockgeneric.garden.progression.GardenProgressionSupport;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCHungryHiker extends HypixelNPC {
    public NPCHungryHiker() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Hungry Hiker", "§e§lCLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "cIr8X0Mtz1Ok7wybAmWuIpersxILt4FbKNsIyDaQ3g90dd1Uq5qlvUT4CvRJigKBZZhMAeJOrRVWe+U+f4BqAXwo5V7lhLxhvBhVNYzE6+AsAh7V8kTJtMiymLtVVeRsuVWaeH2C5lMPQUHl9bzEgCPnMdLGEF4CRogVSXMoXcSj+ev6q7Vk7eNPEIPD2ws4p+N+MZJQ9lQHvh1GuCMqomW4TQ805fUJtY1d23xQ7fSVZr4M81kBEQSQWdVLoq38+LPYUr5RYxUfXYPyb10bET2mIS27blnwrEdpv+h5nFrpibCXHvxEQzfL7r8jf7jmn08pzdptbOebrnktzbsa+LQw5yHfkAgK0Z7NJMBADpeG3ek/mv5TGbIerNQo4KVx/OZNfR+jCr1gxnSKDKUB5ye59BuSqKVzi2XICvNynxnl6UfN3CQXK3iZPVXA7laFDmULftKXx5FFCQCFUzitxdOEcDO6GBhp0ZIC2FyIMGfA9attcmu6QDx0L0H6O54sDrOQRVBKfyBR5DW0isQQ6OpBVxsEQtL0ny+wZ8VAlQNuVaiYTQMp7O9J0sjJfjtgjyfGvRFHF0G+MFCH3NuY/ch8jt02CKEUDEaSSaCtbvPM/b9DtEPSUkEMrXWiVyvhLfiFuolQnfX8KXS6l6Dbq5wpTS/DQIPtUTLTDO8zLnI=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYxNzAzNjc3MTUyOSwKICAicHJvZmlsZUlkIiA6ICJiMGQ3MzJmZTAwZjc0MDdlOWU3Zjc0NjMwMWNkOThjYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJPUHBscyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9kNmY5YjliNjI0MDRjNWNmOTRjMzg1YzdmMWM4NmFkZGUwZDlhZWY5YTljZDI5ZTJhYjU4M2ZhMWRlNjNmNDI2IgogICAgfQogIH0KfQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(269, 48, -480, -130, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        if (e.player() instanceof SkyBlockPlayer player) {
            GardenProgressionSupport.apply(player, GardenProgressionReward.flag("SAVE_THIS_HIKER"));
            sendNPCMessage(player, "Thanks for stopping by. I feel a lot safer now.");
            return;
        }
        e.player().notImplemented();
    }
}
