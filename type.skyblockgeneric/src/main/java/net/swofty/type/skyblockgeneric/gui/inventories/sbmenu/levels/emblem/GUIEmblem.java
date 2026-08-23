package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.levels.emblem;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.*;
import net.swofty.type.generic.gui.v2.context.ClickContext;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.levels.SkyBlockEmblems;
import net.swofty.type.skyblockgeneric.levels.abstr.CauseEmblem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIEmblem extends PaginatedView<SkyBlockEmblems.SkyBlockEmblem, GUIEmblem.EmblemState> {
    private final SkyBlockEmblems emblemCategory;

    public GUIEmblem(SkyBlockEmblems emblemCategory) {
        this.emblemCategory = emblemCategory;
    }

    @Override
    public ViewConfiguration<EmblemState> configuration() {
        return ViewConfiguration.withText(
                (state, ctx) -> Text.of("Emblems - {} ({}/{})", emblemCategory.toString(), state.page() + 1,
                        Math.max(1, (int) Math.ceil((double) state.items().size() / getPaginatedSlots().length))),
                InventoryType.CHEST_5_ROW
        );
    }

    public static EmblemState createInitialState(SkyBlockEmblems emblemCategory) {
        return new EmblemState(new ArrayList<>(emblemCategory.getEmblems()), 0, "");
    }

    @Override
    protected int[] getPaginatedSlots() {
        return new int[]{
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
        };
    }

    @Override
    protected int getNextPageSlot() {
        return 44;
    }

    @Override
    protected int getPreviousPageSlot() {
        return 36;
    }

    @Override
    protected ItemStack.Builder renderItem(SkyBlockEmblems.SkyBlockEmblem item, int index, HypixelPlayer player) {
        SkyBlockPlayer sbPlayer = (SkyBlockPlayer) player;
        boolean unlocked = sbPlayer.hasUnlockedXPCause(item.cause());
        CauseEmblem causeEmblem = (CauseEmblem) item.cause();

        Text name = Text.of(unlocked ? "<a>{} " : "<c>{} ", item.displayName()).append(item.emblem());

        List<Text> lore;
        if (unlocked) {
            lore = new ArrayList<>(List.of(
                    Text.of("<8>{}", causeEmblem.emblemEisplayName()),
                    Text.literal(" "),
                    Text.of("<7>Preview: ").append(sbPlayer.getFullDisplayName(item)),
                    Text.literal(" "),
                    Text.of("<e>Click to select!")
            ));
        } else {
            lore = new ArrayList<>(List.of(
                    Text.of("<8>Locked"),
                    Text.literal(" "),
                    Text.of("<c>{}", causeEmblem.getEmblemRequiresMessage())
            ));
        }

        return ItemStacks.item(unlocked ? item.displayMaterial() : Material.GRAY_DYE, 1, name, lore);
    }

    @Override
    protected void onItemClick(ClickContext<EmblemState> click, ViewContext ctx, SkyBlockEmblems.SkyBlockEmblem item, int index) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        boolean unlocked = player.hasUnlockedXPCause(item.cause());

        if (!unlocked) {
            player.sendMessage("<c>You have not unlocked this emblem yet!");
            return;
        }

        player.getSkyBlockExperience().setEmblem(emblemCategory, item);
        player.sendMessage("<a>You have selected the {} emblem!", item.displayName());
    }

    @Override
    protected boolean shouldFilterFromSearch(EmblemState state, SkyBlockEmblems.SkyBlockEmblem item) {
        return !item.displayName().toLowerCase().contains(state.query.toLowerCase());
    }

    @Override
    protected void layoutCustom(ViewLayout<EmblemState> layout, EmblemState state, ViewContext ctx) {
        Components.close(layout, 40);
        Components.back(layout, 39, ctx);
    }

    public record EmblemState(List<SkyBlockEmblems.SkyBlockEmblem> items, int page, String query) implements PaginatedState<SkyBlockEmblems.SkyBlockEmblem> {
        @Override
        public PaginatedState<SkyBlockEmblems.SkyBlockEmblem> withPage(int page) {
            return new EmblemState(items, page, query);
        }

        @Override
        public PaginatedState<SkyBlockEmblems.SkyBlockEmblem> withItems(List<SkyBlockEmblems.SkyBlockEmblem> items) {
            return new EmblemState(items, page, query);
        }
    }
}
