package net.swofty.type.ravengardgeneric.entity.animation;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.ravengardgeneric.entity.RavengardNPC;
import net.swofty.type.ravengardgeneric.user.RavengardPlayer;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AnimatedRavengardNPC extends RavengardNPC {
    private static final Random TALK_RANDOM = new Random();

    private final RavengardAnimationClip clip;
    private final List<Entity> animatedParts = new ArrayList<>();
    private final Map<Integer, Pos> homePositions = new HashMap<>();
    private final Map<UUID, DialogueSession> dialogueSessions = new ConcurrentHashMap<>();

    private RavengardRoaming roaming;
    private float baseYaw;
    private Vec currentOffset = Vec.ZERO;
    private float lastYaw = Float.NaN;
    private final Map<Integer, float[]> lastSentRotation = new HashMap<>();
    private RavengardAnimationPhase phase = RavengardAnimationPhase.IDLE;
    private int frame;
    private int moveFrame;

    public AnimatedRavengardNPC(RavengardAnimationClip clip, Pos position, String name, String bottom) {
        super(buildConfiguration(clip, position, name, bottom));
        this.clip = clip;
    }

    private static Configuration buildConfiguration(RavengardAnimationClip clip, Pos position,
                                                    String name, String bottom) {
        Configuration.Builder builder = Configuration.builder()
                .position(position)
                .name(name)
                .bottom(bottom);

        for (RavengardAnimationClip.Part part : clip.parts()) {
            RavengardAnimationClip.Base base = part.base();
            double[] offset = base.offset();
            builder.itemDisplay(new ItemDisplayData(
                    new Vec(offset[0], offset[1], offset[2]),
                    Material.LEATHER_BOOTS,
                    base.model(),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    0xFFFFFF,
                    null,
                    displayContextName(base.itemDisplayContext()),
                    new DisplayData(
                            vec(base.translation()),
                            vec(base.scale()),
                            base.leftRotation(),
                            base.rightRotation(),
                            0,
                            clip.interpolationDuration(),
                            0,
                            "FIXED",
                            -1,
                            0f,
                            0f,
                            base.glowColor()
                    )
            ));
        }

        return builder.build();
    }

    @Override
    public void spawn(Instance instance) {
        super.spawn(instance);

        animatedParts.clear();
        List<Entity> spawned = getEntities();
        int partCount = clip.parts().size();
        for (int i = spawned.size() - partCount; i < spawned.size(); i++) {
            animatedParts.add(spawned.get(i));
        }

        homePositions.clear();
        for (Entity entity : spawned) {
            homePositions.put(entity.getEntityId(), entity.getPosition());
        }

        baseYaw = getConfiguration().getPosition().yaw();
        if (clip.walkSpeed() > 0) {
            roaming = new RavengardRoaming(
                    getConfiguration().getPosition(),
                    clip.roamRadius(),
                    clip.walkSpeed(),
                    RavengardRoaming.MIN_PAUSE_TICKS,
                    RavengardRoaming.MAX_PAUSE_TICKS);
            roaming.setInstance(instance);
        }
    }

    @Override
    public void onClick(RavengardPlayer player) {
        // once the introduction has been heard, shopkeepers go straight to their shop
        if (clip.shop() != null && dialogueSessions.get(player.getUuid()) == null
                && net.swofty.type.ravengardgeneric.profile.RavengardProfiles.hasIntro(player, clip.name())) {
            openShop(player);
            return;
        }
        // a click mid-speech advances the dialogue; the talk cycle keeps playing from where it is
        if (phase != RavengardAnimationPhase.TALK) {
            play(RavengardAnimationPhase.TALK);
        }
        advanceDialogue(player);
    }

    private void openShop(RavengardPlayer player) {
        net.swofty.type.ravengardgeneric.shop.RavengardShop shop =
                net.swofty.type.ravengardgeneric.shop.RavengardShopRegistry.get(clip.shop());
        if (shop == null) {
            return;
        }
        if (clip.shopLine() != null && clip.dialogue() != null) {
            player.sendMessage(speakerLine(clip.dialogue().speaker(), clip.shopLine()));
        }
        talkSound(player);
        net.swofty.type.generic.gui.v2.ViewNavigator.get(player)
                .push(new net.swofty.type.ravengardgeneric.gui.GUIShop(shop));
    }

    /**
     * One click starts the dialogue; each further click during it skips ahead, sending the next
     * line immediately instead of waiting out its delay.
     */
    private void advanceDialogue(RavengardPlayer player) {
        RavengardAnimationClip.Dialogue dialogue = clip.dialogue();
        if (dialogue == null || dialogue.lines().isEmpty()) {
            return;
        }

        DialogueSession session = dialogueSessions.get(player.getUuid());
        if (session == null) {
            sendLine(player, dialogue, 0);
            return;
        }
        if (session.pending != null) {
            session.pending.cancel();
        }
        sendLine(player, dialogue, session.nextLine);
    }

    private void sendLine(RavengardPlayer player, RavengardAnimationClip.Dialogue dialogue, int index) {
        List<RavengardAnimationClip.Line> lines = dialogue.lines();
        if (!player.isOnline() || index >= lines.size()) {
            dialogueSessions.remove(player.getUuid());
            return;
        }

        player.sendMessage(format(dialogue.speaker(), lines.get(index)));
        talkSound(player);

        if (index == lines.size() - 1) {
            dialogueSessions.remove(player.getUuid());
            onDialogueComplete(player);
            return;
        }

        DialogueSession session = new DialogueSession();
        session.nextLine = index + 1;
        int delayTicks = Math.max(1, lines.get(index + 1).delay() * 20);
        session.pending = MinecraftServer.getSchedulerManager()
                .buildTask(() -> sendLine(player, dialogue, session.nextLine))
                .delay(TaskSchedule.tick(delayTicks))
                .schedule();
        dialogueSessions.put(player.getUuid(), session);
    }

    private static final class DialogueSession {
        private int nextLine;
        private net.minestom.server.timer.Task pending;
    }

    private void onDialogueComplete(RavengardPlayer player) {
        if (!player.isOnline()) {
            return;
        }
        if (clip.shop() != null) {
            net.swofty.type.ravengardgeneric.profile.RavengardProfiles.markIntro(player, clip.name());
            openShop(player);
            return;
        }
        if ("select_class".equals(clip.onComplete())) {
            net.swofty.type.generic.gui.v2.ViewNavigator.get(player)
                    .push(new net.swofty.type.ravengardgeneric.gui.GUISelectClass());
        }
    }

    private static Text format(String speaker, RavengardAnimationClip.Line line) {
        Text prefix = Text.empty();
        if (line.index() != null && line.total() != null) {
            String indexTag = line.index().equals(line.total()) ? "<a>" : "<7>";
            prefix = Text.of("<7>[" + indexTag + "{}<7>/<a>{}<7>] ", line.index(), line.total());
        }
        return prefix.append(speakerLine(speaker, line.text()));
    }

    private static Text speakerLine(String speaker, String text) {
        return Text.of("<d>{}<f>: ", speaker).append(Text.legacy(text));
    }

    private static void talkSound(RavengardPlayer player) {
        player.playSound(net.kyori.adventure.sound.Sound.sound()
                .type(net.kyori.adventure.key.Key.key("entity.villager.celebrate"))
                .volume(1.0f)
                .pitch(0.8f + TALK_RANDOM.nextFloat() * 0.4f)
                .build());
    }

    public void play(RavengardAnimationPhase target) {
        phase = target;
        frame = 0;
    }

    public void tick() {
        if (animatedParts.isEmpty()) {
            return;
        }

        boolean exhausted = true;
        for (int i = 0; i < animatedParts.size() && i < clip.parts().size(); i++) {
            List<RavengardAnimationClip.Frame> frames = clip.parts().get(i).phase(phase);
            if (frames == null || frames.isEmpty()) {
                continue;
            }

            int index = phase.looping() ? frame % frames.size() : Math.min(frame, frames.size() - 1);
            if (frame < frames.size() - 1) {
                exhausted = false;
            }

            RavengardAnimationClip.Frame current = frames.get(index);
            Entity entity = animatedParts.get(i);
            final int partIndex = i;
            entity.editEntityMeta(ItemDisplayMeta.class, meta -> {
                meta.setTransformationInterpolationDuration(clip.interpolationDuration());
                meta.setTransformationInterpolationStartDelta(0);
                if (current.translation() != null) {
                    meta.setTranslation(rotateOffset(vec(current.translation())));
                }
                if (current.leftRotation() != null) {
                    meta.setLeftRotation(continuous(partIndex, rotateRig(current.leftRotation())));
                }
            });
        }

        applyMovement();

        if (phase != RavengardAnimationPhase.TALK) {
            RavengardAnimationPhase desired = isWalking()
                    ? RavengardAnimationPhase.WALK
                    : RavengardAnimationPhase.IDLE;
            if (desired != phase) {
                play(desired);
            }
        }

        frame++;
        if (!phase.looping() && exhausted) {
            play(RavengardAnimationPhase.IDLE);
        }
    }

    private void applyMovement() {
        if (roaming == null) {
            return;
        }

        Vec next = roaming.advance(currentOffset);
        boolean moved = !next.equals(currentOffset);
        currentOffset = next;

        if (!moved && lastYaw == roaming.yaw()) {
            return;
        }
        lastYaw = roaming.yaw();

        for (Entity entity : getEntities()) {
            Pos home = homePositions.get(entity.getEntityId());
            if (home == null) {
                continue;
            }
            entity.teleport(new Pos(
                    home.x() + currentOffset.x(),
                    home.y() + currentOffset.y(),
                    home.z() + currentOffset.z(),
                    roaming.yaw(),
                    home.pitch()));
        }
    }

    public boolean isWalking() {
        return roaming != null && roaming.isMoving();
    }

    private double facingRadians() {
        if (roaming == null) {
            return 0.0;
        }
        return Math.toRadians(normalise(roaming.yaw() - baseYaw));
    }

    private float[] continuous(int part, float[] rotation) {
        float[] last = lastSentRotation.get(part);
        if (last != null) {
            float dot = last[0] * rotation[0] + last[1] * rotation[1]
                    + last[2] * rotation[2] + last[3] * rotation[3];
            if (dot < 0) {
                for (int i = 0; i < 4; i++) rotation[i] = -rotation[i];
            }
        }
        lastSentRotation.put(part, rotation);
        return rotation;
    }

    private float[] rotateRig(float[] rotation) {
        double theta = facingRadians();
        if (theta == 0.0) {
            return rotation;
        }

        float sin = (float) Math.sin(theta / 2.0);
        float cos = (float) Math.cos(theta / 2.0);

        float rx = rotation[0], ry = rotation[1], rz = rotation[2], rw = rotation[3];
        return new float[]{
                cos * rx + sin * rz,
                cos * ry + sin * rw,
                cos * rz - sin * rx,
                cos * rw - sin * ry
        };
    }

    private Vec rotateOffset(Vec translation) {
        double theta = facingRadians();
        if (theta == 0.0) {
            return translation;
        }

        double sin = Math.sin(theta);
        double cos = Math.cos(theta);
        return new Vec(
                translation.x() * cos + translation.z() * sin,
                translation.y(),
                -translation.x() * sin + translation.z() * cos);
    }

    private static float normalise(float degrees) {
        return ((degrees + 540f) % 360f) - 180f;
    }

    private static Vec vec(float[] values) {
        if (values == null || values.length < 3) {
            return Vec.ZERO;
        }
        return new Vec(values[0], values[1], values[2]);
    }

    private static String displayContextName(int ordinal) {
        ItemDisplayMeta.DisplayContext[] values = ItemDisplayMeta.DisplayContext.values();
        if (ordinal < 0 || ordinal >= values.length) {
            return "HEAD";
        }
        return values[ordinal].name();
    }
}
