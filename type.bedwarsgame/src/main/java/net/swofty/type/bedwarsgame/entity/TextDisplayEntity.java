package net.swofty.type.bedwarsgame.entity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.swofty.commons.text.Text;

public class TextDisplayEntity extends EntityCreature {

	public TextDisplayEntity(String markup, Object... arguments) {
		this(Text.of(markup, arguments));
	}

	public TextDisplayEntity(Text text) {
		super(EntityType.TEXT_DISPLAY);
		setNoGravity(true);
		setGlowing(true);
		editEntityMeta(TextDisplayMeta.class, (meta) -> {
			meta.setText(text.asComponent());
			meta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
			meta.setSeeThrough(true);
		});
	}

	public void setText(String markup, Object... arguments) {
		setText(Text.of(markup, arguments));
	}

	public void setText(Text text) {
		editEntityMeta(TextDisplayMeta.class, (meta) -> {
			meta.setText(text.asComponent());
		});
	}

	public void setTranslation(Pos translation) {
		editEntityMeta(TextDisplayMeta.class, (meta) -> {
			meta.setTranslation(translation);
		});
	}

	public void setConstraints(AbstractDisplayMeta.BillboardConstraints constraints) {
		editEntityMeta(TextDisplayMeta.class, (meta) -> {
			meta.setBillboardRenderConstraints(constraints);
		});
	}

}
