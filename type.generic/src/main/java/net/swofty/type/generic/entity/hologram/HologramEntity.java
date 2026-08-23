package net.swofty.type.generic.entity.hologram;

import lombok.Getter;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.swofty.commons.text.Text;

@Getter
public class HologramEntity extends Entity {
    private Text text;

    public HologramEntity(Text text) {
        super(EntityType.ARMOR_STAND);
        this.text = text;

        setInvisible(true);
        ArmorStandMeta meta = (ArmorStandMeta) this.getEntityMeta();
        meta.setNotifyAboutChanges(false);
        set(DataComponents.CUSTOM_NAME, text.asComponent());
        meta.setNotifyAboutChanges(true);

        editEntityMeta(ArmorStandMeta.class, m -> {
            m.setCustomNameVisible(true);
            m.setSmall(true);
            m.setHasNoGravity(true);
            m.setMarker(true);
        });
    }

    public HologramEntity(String markup) {
        this(markup.indexOf('§') >= 0 ? Text.legacy(markup) : Text.parseLenient(markup));
    }

    public HologramEntity(String markup, Object... arguments) {
        this(Text.of(markup, arguments));
    }

    public void setText(Text text) {
        this.text = text;

        ArmorStandMeta meta = (ArmorStandMeta) this.getEntityMeta();

        meta.setNotifyAboutChanges(false);
        set(DataComponents.CUSTOM_NAME, text.asComponent());
        meta.setNotifyAboutChanges(true);
    }

    public void setText(String markup) {
        setText(Text.read(markup));
    }

    public void setText(String markup, Object... arguments) {
        setText(Text.of(markup, arguments));
    }
}
