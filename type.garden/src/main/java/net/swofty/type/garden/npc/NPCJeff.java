package net.swofty.type.garden.npc;

import net.swofty.type.garden.gui.GUIManageChips;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCJeff extends AbstractGardenNpc {
    private static final String TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTc1NjgyNTA3NDg4OSwKICAicHJvZmlsZUlkIiA6ICI0OWIzODUyNDdhMWY0NTM3YjBmN2MwZTFmMTVjMTc2NCIsCiAgInByb2ZpbGVOYW1lIiA6ICJiY2QyMDMzYzYzZWM0YmY4IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzJhNGE3Nzg0MDQ0OTQzN2QyMWY5ZDVmMDQ3ZjUxODQxM2NiMmY2OWUzZWNiZmI5OTM4NjY0OWM5OTdjYTFlOTEiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==";
    private static final String SIGNATURE = "Iw9yC7X3a2dPeEYsgmxARXA7iNQI6txFpVmQx5GBRF/SfVcFWDUbNLw3X8GzZp1hneHucwrTcgz1CLpdoJlawt0ZveveEMxJqrYPhvdGfJp1Pp1ap/Q70M19Ht24wxfG6eED8+SYsghk3mBrLs6pEQ63sg0SbZN6BRW50G6L2PGrgujBibdlj4m49CWE0gv3tqofWUH4yT/KXHN11bJ2LVURYMzSxl0EG6jrFKNkuufP3JJrK9oDaSt1msgxjqhK1O/cxv73nW2+na4ZKe9FDpGM7KF9fC4HirgeWu7UxOobNstHXcpfNZBPOLIry42nM2apEuH6jPm04tajLZJvYZqYyCS2LWc7uaIjhOrMm0nqZ7OKWj6E6m9szPPEeVq1MWT0nsp+pRqxBfqQld9EoN1sLKgzRGQ++qW+4bP7ceH6A1MoisJQ7ch8qF95F+FWDjjVcvV42iFB5ftXtMKTai8Vjw5ZcxfANk0Od+Nmxic4eC4QKm/tupvjnKS8rSPOZishmYh3B+aA/JOuzhqp844Sc4apPFBcLXCEK0+9oKHkVDkE3owHXHh3ktTs7naxgq92t3IVlxUtKGR6aVOSSEfp1FINXfdRBYuECpM4X/3H7zyE58UF0YmIKfYYh8jq/r2GTufXELY6kcc4Xy5XT8ZM8GQC1pJ/ih69hOOJKAQ=";

    public NPCJeff() {
        super("jeff", "§bJeff", TEXTURE, SIGNATURE);
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
        player.openView(new GUIManageChips());
    }

    @Override
    protected DialogueSet[] dialogues(net.swofty.type.generic.user.HypixelPlayer player) {
        return configuredDialogues();
    }
}
