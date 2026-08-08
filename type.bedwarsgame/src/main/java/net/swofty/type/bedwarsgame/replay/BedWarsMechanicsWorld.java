package net.swofty.type.bedwarsgame.replay;

import io.github.term4.polyp.world.InstanceWorld;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.swofty.type.bedwarsgame.game.v2.BedWarsGame;
import org.jetbrains.annotations.NotNull;

public final class BedWarsMechanicsWorld extends InstanceWorld {
    private final BedWarsGame game;

    public BedWarsMechanicsWorld(Instance instance, BedWarsGame game) {
        super(instance);
        this.game = game;
    }

    @Override
    public void broadcast(@NotNull ServerPacket packet) {
        super.broadcast(packet);
        if (packet instanceof ParticlePacket particle && game.getReplayManager().isRecording()) {
            game.getReplayManager().recordParticle(particle);
        }
    }

    @Override
    public void playSound(@NotNull Sound sound, @NotNull Point pos) {
        super.playSound(sound, pos);
        if (game.getReplayManager().isRecording()) {
            game.getReplayManager().recordSound(sound, pos.x(), pos.y(), pos.z());
        }
    }
}
