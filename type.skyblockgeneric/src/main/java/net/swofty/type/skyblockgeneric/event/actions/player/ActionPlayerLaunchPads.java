package net.swofty.type.skyblockgeneric.event.actions.player;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.swofty.commons.ServerType;
import net.swofty.commons.StringUtility;
import net.swofty.commons.UnderstandableProxyServer;
import net.swofty.commons.text.Text;
import net.swofty.proxyapi.ProxyInformation;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.generic.utility.MathUtility;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.LaunchPads;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ActionPlayerLaunchPads implements HypixelEventClass {
    private static final int SEGMENTS = 30;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final Set<UUID> notifiedPlayers = ConcurrentHashMap.newKeySet();

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, isAsync = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerMoveEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();

        LaunchPads pad;
        try {
            pad = LaunchPads.getLaunchPadInRange(player.getPosition(), 2);
        } catch (ExceptionInInitializerError err) {
            return;
        }

        if (pad == null) {
            notifiedPlayers.remove(player.getUuid());
            return;
        }

        if (player.isInLaunchpad()) return;
        player.setInLaunchpad(true);

        boolean accepted = pad.getShouldAllow().apply(player);
        Text rejectionMessage = Text.parse(pad.getRejectionMessage());
        if (!accepted && player.getRank().isStaff()) {
            accepted = true;
            player.getLogHandler().debug("As a staff member, you have bypassed the launchpad requirement.");
        }

        player.sendPacket(new ParticlePacket(
                Particle.EXPLOSION,
                false,
                false,
                (float) player.getPosition().x(),
                (float) player.getPosition().y(),
                (float) player.getPosition().z(),
                0,
                0,
                0,
                0,
                1
        ));
        player.playSound(Sound.sound(Key.key("entity.generic.explode"), Sound.Source.PLAYER, 1, 1));

        ServerType targetServerType = pad.getTargetServerType();
        ProxyInformation proxyInfo = new ProxyInformation();

        List<UnderstandableProxyServer> informations =
                proxyInfo.getServerInformation(targetServerType).join();
        if (informations.isEmpty()) {
            accepted = false;
            rejectionMessage = Text.of("<c>There are no {} servers available at the moment. Please try again later.",
                    StringUtility.toNormalCase(targetServerType.name()));
        }

        if (!accepted) {
            player.setVelocity(player.getPosition().direction().mul(-30).withY(5));
            player.sendMessage(rejectionMessage);
            player.setInLaunchpad(false);
            return;
        }

        Pos originalPosition = player.getPosition();
        player.playSound(Sound.sound(Key.key("entity.firework_rocket.launch"), Sound.Source.PLAYER, 1, 1));

        LivingEntity armorStand = new LivingEntity(EntityType.ARMOR_STAND);
        armorStand.getEntityMeta().setInvisible(true);
        armorStand.getEntityMeta().setHasNoGravity(true);
        armorStand.setInstance(player.getInstance(), player.getPosition());
        armorStand.addPassenger(player);

        List<Pos> curve = MathUtility.bezierCurve(player.getPosition(), pad.getDestination(), SEGMENTS);
        long timeToSleep = 3000 / SEGMENTS;

        // Use ScheduledExecutorService to run the launch trajectory with delays
        AtomicInteger index = new AtomicInteger(0);
        ScheduledFuture<?>[] taskHolder = new ScheduledFuture<?>[1];
        taskHolder[0] = scheduler.scheduleAtFixedRate(() -> {
            int currentIndex = index.getAndIncrement();
            if (currentIndex >= curve.size()) {
                // Cancel the task
                if (taskHolder[0] != null) {
                    taskHolder[0].cancel(false);
                }

                player.setInLaunchpad(false);
                notifiedPlayers.remove(player.getUuid());

                // Execute the after finished callback
                pad.getAfterFinished().accept(player);

                // Check after a delay if player is still on this server (transfer failed)
                if (pad.getServerType() != pad.getTargetServerType()) {
                    scheduler.schedule(() -> {
                        // If player is still on the same server and instance, teleport them back
                        if (player.getInstance() != null && player.getInstance().equals(armorStand.getInstance())) {
                            player.teleport(originalPosition);
                            player.sendMessage("<c>Failed to connect to the server. You have been teleported back.");
                        }
                        try {
                            armorStand.remove();
                        } catch (Exception _) {
                        }
                    }, 2, TimeUnit.SECONDS);
                } else {
                    armorStand.remove();
                }

                return;
            }

            Pos pos = curve.get(currentIndex);
            Vec toGoTo = pos.asVec();
            Vec direction = toGoTo.sub(player.getPosition().asVec()).normalize();
            armorStand.setVelocity(direction.mul(50, 5, 50));
        }, 0, timeToSleep, TimeUnit.MILLISECONDS);
    }
}
