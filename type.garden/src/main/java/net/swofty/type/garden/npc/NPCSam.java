package net.swofty.type.garden.npc;

import net.swofty.type.garden.gui.GUIDesk;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCSam extends AbstractGardenNpc {
    private static final String BUILDER_TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTY2MTI0NzcyMjk1MiwKICAicHJvZmlsZUlkIiA6ICJjYmFkZmRmNTRkZTM0N2UwODQ3MjUyMDIyYTFkNGRkZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJvRml3aSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS85NWRjZmM4YjMyMDAzMjZiOGY2MWQ4MDYxNWM1YjUzNjk5N2ExMTg1NzBhNDdmMWQzNTMwNjMyOTY5MjFmZTEzIgogICAgfQogIH0KfQ==";
    private static final String BUILDER_SIGNATURE = "BgdB1A6pFJnqpQ5hCaEhFcEt4kZgvELFPg5Sx1bCXsUM956OmkAOJ3vqsyTAVcfhV/US7sKxDqwSh8JIV0tTV8fsC28cjur2f83cTw6ehlBKoERMRzwMV+ppLs6YjWIncTbst0D5CZqLpDs4ZhuR6u5uaDtbC716FJWFng+4YYn4XAapwiOqufUtmTdNjBuN1gmAJXtMhdcG5pNrm3SYO6KqVzo1a5eHcdXIkGp0mHFulAYZNy9S9RHuaEX1YNWWjyCTI+KQyv/mNfV/OfJtLH5e/BwPvL+Dcp7f+4wPeu3U2exiGpXPe2VOFRqFnaGP2URagVMj5BjVeuuiaY519YEGbYbR0SyG6HPSXVOM8A5NeVqGJ6FCcCg1ejfWNpIDS9mchIxgbufIzFOK1rD1E/JX051/IRAUrxW9hx8hne8X3/gOy84D6wS0j/KDjR7X7U1gZyAsTb1HqYdcNard1PNSLsdathh71APASTH0EZVm2JCh8exx7tNCKSW4jB9uvw0FugAdDUBKjWs1qO2Zrd7/IBePfW1uvoEU7TUnXRMKu0OB8yug7Y7CCE/jM1zMDnWRhn1QuF94+se6R2swmDJM8NA/exsDmzFMXi+ndw+bOfJYdyzM+s3H6AgvOnmOtgrZBIkLYSMlvSMigxl2Vd/9rRdVn4wFyv4/Dd1aPho=";

    public NPCSam() {
        super("sam", "§bSam", BUILDER_TEXTURE, BUILDER_SIGNATURE);
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player)) {
            return;
        }
        if (!hasSpoken(player)) {
            setDialogue(player, "hello").thenRun(() -> markSpoken(player));
            return;
        }
        player.openView(new GUIDesk());
    }

    @Override
    protected DialogueSet[] dialogues(net.swofty.type.generic.user.HypixelPlayer player) {
        return configuredDialogues();
    }
}
