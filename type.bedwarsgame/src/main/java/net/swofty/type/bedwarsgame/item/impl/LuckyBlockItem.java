package net.swofty.type.bedwarsgame.item.impl;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.component.CustomData;
import net.minestom.server.tag.Tag;
import net.swofty.commons.text.Text;
import net.swofty.type.bedwarsgame.TypeBedWarsGameLoader;
import net.swofty.type.bedwarsgame.item.SimpleInteractableItem;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.generic.gui.inventory.ItemStacks;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LuckyBlockItem extends SimpleInteractableItem {
    private static final Tag<String> TIER_TAG = Tag.String("lucky_tier");
    private static final Tag<Boolean> PLACED_LUCKY_BLOCK_TAG = Tag.Boolean("placed_lucky_block");
    private static final Map<Instance, Map<BlockVec, PlacedLuckyBlock>> PLACED_BLOCKS = new ConcurrentHashMap<>();

    public LuckyBlockItem() {
        super("lucky_block");
    }

    @Override
    public ItemStack getBlandItem() {
        return stackForTier(LuckyBlockTier.NORMAL, Text.of("<7>Place, then break to open."));
    }

    public ItemStack getItemStack(LuckyBlockTier tier) {
        return stackForTier(tier, Text.of("<7>Place, then break to open."))
            .with(DataComponents.CUSTOM_DATA, new CustomData(CompoundBinaryTag.builder()
                .putString("item", getId())
                .putString("lucky_tier", tier.name())
                .build()));
    }

    @Override
    public void onBlockPlace(PlayerBlockPlaceEvent event) {
        event.setCancelled(true);
        BedWarsPlayer player = (BedWarsPlayer) event.getPlayer();
        ItemStack stack = event.getPlayer().getItemInMainHand();
        LuckyBlockTier tier = readTier(stack);
        if (!canUse(player)) {
            return;
        }

        Point position = event.getBlockPosition();
        player.getInstance().setBlock(position, tier.placeBlock()
            .withTag(TypeBedWarsGameLoader.PLAYER_PLACED_TAG, true)
            .withTag(PLACED_LUCKY_BLOCK_TAG, true)
            .withTag(TIER_TAG, tier.name()));

        Entity display = spawnHeadDisplay(player.getInstance(), position, tier);
        PLACED_BLOCKS.computeIfAbsent(player.getInstance(), _ -> new ConcurrentHashMap<>())
            .put(position.asBlockVec(), new PlacedLuckyBlock(tier, display));

        player.setItemInHand(PlayerHand.MAIN, stack.consume(1));
    }

    public static boolean handleBreak(BedWarsPlayer player, Point point, Block block) {
        if (!Boolean.TRUE.equals(block.getTag(PLACED_LUCKY_BLOCK_TAG))) {
            return false;
        }
        BlockVec key = point.asBlockVec();
        Map<BlockVec, PlacedLuckyBlock> placedBlocks = PLACED_BLOCKS.get(player.getInstance());
        PlacedLuckyBlock placed = placedBlocks == null ? null : placedBlocks.remove(key);
        LuckyBlockTier tier = placed != null ? placed.tier() : readTier(block);
        if (placed != null && placed.display() != null) {
            placed.display().remove();
        }

        player.getInstance().setBlock(point, Block.AIR);
        LuckyBlockRewards.apply(player, new Pos(point.blockX(), point.blockY(), point.blockZ()), tier);
        return true;
    }

    private boolean canUse(BedWarsPlayer player) {
        return player.getGame() != null && player.getGame().getGameType().isLuckyBlock();
    }

    private static LuckyBlockTier readTier(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        LuckyBlockTier tier = LuckyBlockTier.NORMAL;
        if (data != null) {
            String tierName = data.getTag(TIER_TAG);
            if (tierName != null) {
                tier = LuckyBlockTier.valueOf(tierName);
            }
        }
        return tier;
    }

    private static LuckyBlockTier readTier(Block block) {
        String tierName = block.getTag(TIER_TAG);
        if (tierName == null) {
            return LuckyBlockTier.NORMAL;
        }
        return LuckyBlockTier.valueOf(tierName);
    }

    private static ItemStack stackForTier(LuckyBlockTier tier, Text... lore) {
        return ItemStacks.head(tier.headTexture(), tier.displayName(), List.of(lore)).build();
    }

    private static Entity spawnHeadDisplay(Instance instance, Point point, LuckyBlockTier tier) {
        ItemStack headItem = stackForTier(tier);
        LivingEntity displayEntity = new LivingEntity(EntityType.ITEM_DISPLAY);
        displayEntity.editEntityMeta(ItemDisplayMeta.class, meta -> {
            meta.setItemStack(headItem);
            meta.setScale(new Vec(1.15, 1.15, 1.15));
            meta.setHasNoGravity(true);
        });
        displayEntity.setInstance(instance, new Pos(point.blockX() + 0.5, point.blockY() + 0.78, point.blockZ() + 0.5));
        return displayEntity;
    }

    private record PlacedLuckyBlock(LuckyBlockTier tier, Entity display) {
    }
}
