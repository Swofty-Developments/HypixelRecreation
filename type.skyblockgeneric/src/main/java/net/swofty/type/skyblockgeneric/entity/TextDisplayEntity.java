package net.swofty.type.skyblockgeneric.entity;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.swofty.commons.text.Text;

import java.util.function.Consumer;

public class TextDisplayEntity extends LivingEntity {

	public TextDisplayEntity(Text text, Consumer<TextDisplayMeta> metaConsumer) {
		super(EntityType.TEXT_DISPLAY);

		editEntityMeta(TextDisplayMeta.class, meta -> {
			meta.setText(text.asComponent());
			meta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
			meta.setHasNoGravity(true);
			meta.setSeeThrough(true);
			metaConsumer.accept(meta);
		});
	}

	public TextDisplayEntity(String markup, Consumer<TextDisplayMeta> metaConsumer, Object... arguments) {
		this(Text.of(markup, arguments), metaConsumer);
	}

}
