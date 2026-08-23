package net.swofty.type.skyblockgeneric.entity;

import net.swofty.commons.text.Text;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.entity.metadata.item.ItemEntityMeta;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.swofty.type.generic.entity.InteractionEntity;
import net.swofty.type.generic.entity.hologram.HologramEntity;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;

import java.util.function.BiConsumer;

public class GlassDisplay extends LivingEntity {

	private static final float offset = 1f / 5f;
	private static final float scale = 0.6f;

	private final BiConsumer<HypixelPlayer, PlayerEntityInteractEvent> onClick;
	private final SkyBlockItem item;

	// Child entities that need to be removed when this display is removed
	private LivingEntity itemEntity;
	private HologramEntity nameDisplay;
	private InteractionEntity interactionEntity;

	public GlassDisplay(SkyBlockItem item, BiConsumer<HypixelPlayer, PlayerEntityInteractEvent> onClick) {
		super(EntityType.BLOCK_DISPLAY);
		this.item = item;
		this.onClick = onClick;

		setNoGravity(true);

		BlockDisplayMeta meta = (BlockDisplayMeta) super.entityMeta;
		meta.setBlockState(Block.GLASS);
		meta.setTranslation(new Vec(offset, offset)); // Vec3 0.2, 0, 0.2
		meta.setScale(new Vec(scale, scale, scale));
	}

	public static GlassDisplay create(SkyBlockItem item, Instance instance, Point position, BiConsumer<HypixelPlayer, PlayerEntityInteractEvent> onClick) {
		GlassDisplay display = new GlassDisplay(item, onClick);
		display.setInstance(instance, position);
		return display;
	}

	@Override
	public void spawn() {
		super.spawn();
		itemEntity = new LivingEntity(EntityType.ITEM);
		itemEntity.editEntityMeta(ItemEntityMeta.class, meta -> {
			meta.setItem(new NonPlayerItemUpdater(item.getItemStack()).getUpdatedItem().build());
		});
		itemEntity.setCanPickupItem(false);
		itemEntity.setNoGravity(true);
		itemEntity.setInstance(getInstance(), getPosition().add(0.5, -0.1, 0.5));

		nameDisplay = new HologramEntity(Text.literal(item.getDisplayName()));
		nameDisplay.setInstance(getInstance(), getPosition().add(0.5, 0.5, 0.5));

		interactionEntity = new InteractionEntity(scale, scale, onClick);
		interactionEntity.setInstance(getInstance(), getPosition().add(offset + (scale / 2), 0, offset + (scale / 2)));
	}

	@Override
	public void remove() {
		super.remove();
		if (itemEntity != null) {
			itemEntity.remove();
		}
		if (nameDisplay != null) {
			nameDisplay.remove();
		}
		if (interactionEntity != null) {
			interactionEntity.remove();
		}
	}

}
