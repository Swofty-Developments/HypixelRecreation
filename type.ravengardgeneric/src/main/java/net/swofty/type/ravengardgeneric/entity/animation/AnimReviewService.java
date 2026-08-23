package net.swofty.type.ravengardgeneric.entity.animation;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.color.Color;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.ravengardgeneric.user.RavengardPlayer;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AnimReviewService {
    public static final Tag<String> CONTROL = Tag.String("review_control");
    private static final File MARKS_DIR = new File("./configuration/ravengard/anim_marks");
    private static final Map<UUID, Session> SESSIONS = new ConcurrentHashMap<>();
    private static boolean ticking;

    private AnimReviewService() {
    }

    public static Session session(RavengardPlayer player) {
        return SESSIONS.get(player.getUuid());
    }

    public static void start(RavengardPlayer player, String clipName) {
        RavengardReviewClip clip = RavengardReviewClip.load(clipName);
        Session existing = SESSIONS.get(player.getUuid());
        if (existing != null) {
            existing.swap(clip);
        } else {
            Session session = new Session(player, clip);
            SESSIONS.put(player.getUuid(), session);
            session.spawn();
            ensureTicking();
        }
        player.sendMessage("<a>Reviewing <f>{}<a> ({} ticks). Swing or use the hotbar controls; "
                        + "<f>/animreview mark \\<name><a> saves a range.",
                clipName, clip.frames().size());
    }

    public static void stop(RavengardPlayer player) {
        Session session = SESSIONS.remove(player.getUuid());
        if (session != null) {
            session.remove();
        }
    }

    private static synchronized void ensureTicking() {
        if (ticking) return;
        ticking = true;
        MinecraftServer.getSchedulerManager().buildTask(() -> SESSIONS.values().forEach(session -> {
            try {
                session.tick();
            } catch (Exception ignored) {
            }
        })).repeat(net.minestom.server.timer.TaskSchedule.tick(1)).schedule();
    }

    public static final class Session {
        private final RavengardPlayer player;
        private RavengardReviewClip clip;
        private final List<Entity> parts = new ArrayList<>();
        private float[][] lastSent;
        private final Map<String, int[]> marks = new LinkedHashMap<>();
        private Pos anchor;
        private double[] rootBase;
        private ItemStack[] savedInventory;
        private Pos returnPosition;
        private net.minestom.server.entity.GameMode returnGameMode;
        private final List<Pos> platform = new ArrayList<>();
        private int tick;
        private double accumulator;
        private double speed = 1.0;
        private boolean paused = true;
        private int markStart = -1;
        private float reviewYaw;

        Session(RavengardPlayer player, RavengardReviewClip clip) {
            this.player = player;
            this.clip = clip;
            this.lastSent = new float[clip.parts().size()][];
        }

        void spawn() {
            returnPosition = player.getPosition();
            returnGameMode = player.getGameMode();

            // a floating barrier stage high above the map: the player stands on it in
            // creative with the rig a few blocks ahead, free to fly around it
            Pos stage = new Pos(Math.floor(returnPosition.x()) + 0.5, 300,
                    Math.floor(returnPosition.z()) + 0.5, 0, 0);
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -1; dz <= 6; dz++) {
                    Pos block = stage.add(dx, -1, dz);
                    player.getInstance().setBlock(block, net.minestom.server.instance.block.Block.BARRIER);
                    platform.add(block);
                }
            }
            player.setGameMode(net.minestom.server.entity.GameMode.CREATIVE);
            player.teleport(stage);
            anchor = stage.add(0, 0, 4);
            giveControls();
            spawnParts();
        }

        void swap(RavengardReviewClip next) {
            parts.forEach(Entity::remove);
            parts.clear();
            clip = next;
            tick = 0;
            accumulator = 0;
            paused = true;
            markStart = -1;
            reviewYaw = 0;
            spawnParts();
        }

        private void spawnParts() {
            lastSent = new float[clip.parts().size()][];
            rootBase = null;
            marks.clear();
            loadMarks();
            for (double[] root : clip.root()) {
                if (root != null) {
                    rootBase = root;
                    break;
                }
            }
            for (RavengardMobClip.Part part : clip.parts()) {
                Entity display = new Entity(EntityType.ITEM_DISPLAY);
                display.setNoGravity(true);
                display.editEntityMeta(ItemDisplayMeta.class, meta -> {
                    meta.setItemStack(ItemStack.builder(Material.LEATHER_BOOTS)
                            .set(DataComponents.ITEM_MODEL, part.model())
                            .set(DataComponents.DYED_COLOR, new Color(0xFFFFFF))
                            .build());
                    ItemDisplayMeta.DisplayContext[] contexts = ItemDisplayMeta.DisplayContext.values();
                    int context = part.context();
                    meta.setDisplayContext(contexts[context >= 0 && context < contexts.length ? context : 0]);
                    if (part.translation() != null) meta.setTranslation(vec(part.translation()));
                    meta.setScale(vec(part.scale()));
                    if (part.leftRotation() != null) meta.setLeftRotation(part.leftRotation());
                    meta.setRightRotation(part.rightRotation());
                    meta.setTransformationInterpolationDuration(1);
                    meta.setPosRotInterpolationDuration(2);
                    meta.setViewRange(part.viewRange());
                    // the stage floats in unlit void chunks, so light the rig explicitly
                    meta.setBrightness(15, 15);
                });
                double[] off = part.offset();
                display.setInstance(player.getInstance(), anchor.add(off[0], off[1], off[2]));
                parts.add(display);
            }
            resync(0);
        }

        private void loadMarks() {
            File file = new File(MARKS_DIR, clip.name() + ".json");
            if (!file.exists()) return;
            try (java.io.FileReader reader = new java.io.FileReader(file)) {
                var doc = new com.google.gson.Gson().fromJson(reader, com.google.gson.JsonObject.class);
                var stored = doc.getAsJsonObject("marks");
                if (stored == null) return;
                for (String key : stored.keySet()) {
                    var range = stored.getAsJsonArray(key);
                    marks.put(key, new int[]{range.get(0).getAsInt(), range.get(1).getAsInt()});
                }
            } catch (Exception ignored) {
            }
        }

        void remove() {
            parts.forEach(Entity::remove);
            for (Pos block : platform) {
                player.getInstance().setBlock(block, net.minestom.server.instance.block.Block.AIR);
            }
            if (savedInventory != null) {
                for (int i = 0; i < savedInventory.length; i++) {
                    player.getInventory().setItemStack(i, savedInventory[i]);
                }
            }
            if (returnGameMode != null) player.setGameMode(returnGameMode);
            if (returnPosition != null) player.teleport(returnPosition);
            player.sendActionBar(Component.empty());
        }

        private void giveControls() {
            var inventory = player.getInventory();
            savedInventory = new ItemStack[inventory.getSize()];
            for (int i = 0; i < inventory.getSize(); i++) {
                savedInventory[i] = inventory.getItemStack(i);
                inventory.setItemStack(i, ItemStack.AIR);
            }
            inventory.setItemStack(0, control(Material.CLOCK, "pause", "<e>Pause / Play"));
            inventory.setItemStack(1, control(Material.ARROW, "back1", "<f>◀ 1 tick"));
            inventory.setItemStack(2, control(Material.ARROW, "fwd1", "<f>1 tick ▶"));
            inventory.setItemStack(3, control(Material.SPECTRAL_ARROW, "back10", "<f>◀◀ 10 ticks"));
            inventory.setItemStack(4, control(Material.SPECTRAL_ARROW, "fwd10", "<f>10 ticks ▶▶"));
            inventory.setItemStack(5, control(Material.SLIME_BALL, "speed", "<b>Cycle speed"));
            inventory.setItemStack(6, control(Material.COMPASS, "rotate", "<d>Rotate rig 45°"));
            inventory.setItemStack(7, control(Material.EMERALD, "markstart", "<a>Set mark start here"));
            inventory.setItemStack(8, control(Material.NETHER_STAR, "picker", "<d>Pick mob / capture"));
        }

        private static ItemStack control(Material material, String action, String markup) {
            ItemStack.Builder builder = ItemStacks.named(material, markup);
            builder.setTag(CONTROL, action);
            return builder.build();
        }

        public void control(String action) {
            switch (action) {
                case "pause" -> paused = !paused;
                case "back1" -> resync(tick - 1);
                case "fwd1" -> applyStep(1);
                case "back10" -> resync(tick - 10);
                case "fwd10" -> applyStep(10);
                case "speed" -> speed = speed == 1.0 ? 0.5 : speed == 0.5 ? 0.25 : speed == 0.25 ? 2.0 : 1.0;
                case "rotate" -> {
                    reviewYaw = (reviewYaw + 45f) % 360f;
                    resync(tick);
                }
                case "markstart" -> {
                    markStart = tick;
                    player.sendMessage("<a>Mark start set at tick <f>{}<a>. Scrub to the end and run "
                                    + "<f>/animreview mark \\<name><a>.",
                            tick);
                }
                case "marks" -> listMarks();
                case "picker" -> net.swofty.type.generic.gui.v2.ViewNavigator.get(player)
                        .push(new net.swofty.type.ravengardgeneric.gui.GUIAnimReview(clip.mob()));
            }
        }

        public void listMarks() {
            if (marks.isEmpty()) {
                player.sendMessage("<7>No marks yet on {}.", clip.name());
                return;
            }
            marks.forEach((name, range) -> player.sendMessage(
                    "<f>{}<7>: {} - {}", name, range[0], range[1]));
        }

        public void mark(String name) {
            if (markStart < 0) {
                player.sendMessage("<c>Set a mark start first (emerald or /animreview markstart).");
                return;
            }
            int from = Math.min(markStart, tick), to = Math.max(markStart, tick);
            marks.put(name, new int[]{from, to});
            markStart = -1;
            save();
            player.sendMessage("<a>Marked <f>{}<a> as {} - {} and saved.", name, from, to);
        }

        public void unmark(String name) {
            if (marks.remove(name) != null) {
                save();
                player.sendMessage("<a>Removed mark <f>{}<a>.", name);
            }
        }

        public void gotoTick(int target) {
            resync(target);
        }

        private void save() {
            MARKS_DIR.mkdirs();
            StringBuilder json = new StringBuilder();
            json.append("{\"clip\":\"").append(clip.name())
                    .append("\",\"mob\":\"").append(clip.mob())
                    .append("\",\"source\":\"").append(clip.source())
                    .append("\",\"marks\":{");
            boolean first = true;
            for (var entry : marks.entrySet()) {
                if (!first) json.append(',');
                first = false;
                json.append('"').append(entry.getKey()).append("\":[")
                        .append(entry.getValue()[0]).append(',').append(entry.getValue()[1]).append(']');
            }
            json.append("}}");
            try (FileWriter writer = new FileWriter(new File(MARKS_DIR, clip.name() + ".json"))) {
                writer.write(json.toString());
            } catch (Exception exception) {
                player.sendMessage("<c>Failed to save marks: {}", exception.getMessage());
            }
        }

        void tick() {
            if (player.isRemoved() || !player.isOnline()) {
                SESSIONS.remove(player.getUuid());
                remove();
                return;
            }
            if (!paused) {
                accumulator += speed;
                while (accumulator >= 1.0) {
                    accumulator -= 1.0;
                    if (tick >= clip.frames().size() - 1) {
                        paused = true;
                        break;
                    }
                    applyFrame(tick + 1);
                }
            }
            actionBar();
        }

        private void applyStep(int by) {
            for (int i = 0; i < by && tick < clip.frames().size() - 1; i++) {
                applyFrame(tick + 1);
            }
        }

        private void resync(int target) {
            target = Math.clamp(target, 0, Math.max(0, clip.frames().size() - 1));
            Map<Integer, RavengardMobClip.Keyframe> pose = new LinkedHashMap<>();
            Map<Integer, float[]> translations = new LinkedHashMap<>();
            Map<Integer, float[]> rotations = new LinkedHashMap<>();
            Map<Integer, float[]> scales = new LinkedHashMap<>();
            for (int i = 0; i < clip.parts().size(); i++) {
                RavengardMobClip.Part part = clip.parts().get(i);
                if (part.translation() != null) translations.put(i, part.translation());
                if (part.leftRotation() != null) rotations.put(i, part.leftRotation());
                scales.put(i, part.scale());
            }
            for (int t = 0; t <= target; t++) {
                for (var entry : clip.frames().get(t).entrySet()) {
                    int index = Integer.parseInt(entry.getKey());
                    RavengardMobClip.Keyframe keyframe = entry.getValue();
                    if (keyframe.translation() != null) translations.put(index, keyframe.translation());
                    if (keyframe.leftRotation() != null) rotations.put(index, keyframe.leftRotation());
                    if (keyframe.scale() != null) scales.put(index, keyframe.scale());
                }
            }
            tick = target;
            for (int i = 0; i < parts.size(); i++) {
                final float[] translation = translations.get(i);
                final float[] rotation = rotations.get(i);
                final float[] scale = scales.get(i);
                final int index = i;
                parts.get(i).editEntityMeta(ItemDisplayMeta.class, meta -> {
                    meta.setTransformationInterpolationStartDelta(0);
                    if (translation != null) meta.setTranslation(rotateVec(translation));
                    if (rotation != null) meta.setLeftRotation(guard(index, rotateQuat(rotation)));
                    if (scale != null) meta.setScale(vec(scale));
                });
            }
            movePosition();
        }

        private void applyFrame(int target) {
            tick = target;
            Map<String, RavengardMobClip.Keyframe> frame = clip.frames().get(tick);
            for (var entry : frame.entrySet()) {
                int index = Integer.parseInt(entry.getKey());
                if (index >= parts.size()) continue;
                RavengardMobClip.Keyframe keyframe = entry.getValue();
                parts.get(index).editEntityMeta(ItemDisplayMeta.class, meta -> {
                    if (keyframe.duration() != null) {
                        meta.setTransformationInterpolationDuration(keyframe.duration());
                    }
                    meta.setTransformationInterpolationStartDelta(0);
                    if (keyframe.translation() != null) {
                        meta.setTranslation(rotateVec(keyframe.translation()));
                    }
                    if (keyframe.leftRotation() != null) {
                        meta.setLeftRotation(guard(index, rotateQuat(keyframe.leftRotation())));
                    }
                    if (keyframe.scale() != null) meta.setScale(vec(keyframe.scale()));
                });
            }
            movePosition();
        }

        private void movePosition() {
            if (rootBase == null || clip.root().isEmpty()) return;
            double[] root = clip.root().get(Math.min(tick, clip.root().size() - 1));
            if (root == null) return;
            double theta = Math.toRadians(reviewYaw);
            double rawX = root[0] - rootBase[0], rawZ = root[1] - rootBase[1];
            double dx = rawX * Math.cos(theta) + rawZ * Math.sin(theta);
            double dz = -rawX * Math.sin(theta) + rawZ * Math.cos(theta);
            for (int i = 0; i < parts.size(); i++) {
                double[] off = clip.parts().get(i).offset();
                parts.get(i).teleport(anchor.add(off[0] + dx, off[1], off[2] + dz));
            }
        }

        private Vec rotateVec(float[] value) {
            double theta = Math.toRadians(reviewYaw);
            double sin = Math.sin(theta), cos = Math.cos(theta);
            return new Vec(value[0] * cos + value[2] * sin, value[1],
                    -value[0] * sin + value[2] * cos);
        }

        private float[] rotateQuat(float[] rotation) {
            double theta = Math.toRadians(reviewYaw) / 2.0;
            float sin = (float) Math.sin(theta), cos = (float) Math.cos(theta);
            float rx = rotation[0], ry = rotation[1], rz = rotation[2], rw = rotation[3];
            return new float[]{cos * rx + sin * rz, cos * ry + sin * rw,
                    cos * rz - sin * rx, cos * rw - sin * ry};
        }

        private float[] guard(int index, float[] rotation) {
            float[] result = rotation.clone();
            float[] last = lastSent[index];
            if (last != null) {
                float dot = last[0] * result[0] + last[1] * result[1]
                        + last[2] * result[2] + last[3] * result[3];
                if (dot < 0) {
                    for (int i = 0; i < 4; i++) result[i] = -result[i];
                }
            }
            lastSent[index] = result;
            return result;
        }

        private void actionBar() {
            Text bar = Text.of("<e>{} <f>{}<7>/{}", clip.name(), tick, clip.frames().size() - 1)
                    .append(paused ? " <c>⏸" : " <a>▶")
                    .append(" <b>x{}", speed);
            if (markStart >= 0) {
                bar = bar.append(" <a>start={}", markStart);
            }
            List<Integer> flashes = clip.flashes();
            for (int flash : flashes) {
                if (Math.abs(flash - tick) <= 2) {
                    bar = bar.append(" <c>[hit]");
                    break;
                }
            }
            player.sendActionBar(bar);
        }

        private static Vec vec(float[] values) {
            return new Vec(values[0], values[1], values[2]);
        }
    }
}
