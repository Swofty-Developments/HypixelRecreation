package net.swofty.type.skyblockgeneric.utility;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.entity.hologram.HologramEntity;

public class DamageIndicator {
    private static final char[] DEFAULT_COLOUR_PALLET = {'F', 'F', 'E', '6', 'C', 'C'};

    private int damage;
    private Pos pos;
    private boolean crit;
    private final int randomnessMultiplier;

    public DamageIndicator() {
        randomnessMultiplier = 1;
        crit = false;
    }

    public void display(Instance instance) {
        if (this.pos == null) return;

        Text name = crit ? Text.of(getRainbowEffect("✧" + damage + "✧"))
                : Text.of("<7>{}", damage);

        Entity entity = new HologramEntity(name);
        entity.setInstance(instance, utilRandomizeLoc(pos));
        entity.setAutoViewable(true);
        MinecraftServer.getSchedulerManager().scheduleTask(entity::remove, TaskSchedule.seconds(1), TaskSchedule.stop());
    }

    public DamageIndicator damage(float d) {
        this.damage = Math.round(d);
        return this;
    }

    public DamageIndicator pos(Pos pos) {
        this.pos = pos;
        return this;
    }

    public DamageIndicator critical(boolean c) {
        this.crit = c;
        return this;
    }

    private String getRainbowEffect(String string) {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (String c : string.split("")) {
            if (i > DEFAULT_COLOUR_PALLET.length - 1) i = 0;
            builder.append("<").append(Character.toLowerCase(DEFAULT_COLOUR_PALLET[i])).append(">").append(c);
            i++;
        }
        return builder.toString();
    }

    private Pos utilRandomizeLoc(Pos pos) {
        return pos.add(StringUtility.random(-1.5, 1.5) + randomnessMultiplier, 1, StringUtility.random(-1.5, 1.5) * randomnessMultiplier);
    }
}
