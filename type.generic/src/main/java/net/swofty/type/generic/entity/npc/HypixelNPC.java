package net.swofty.type.generic.entity.npc;

import lombok.Builder;
import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.swofty.type.generic.entity.npc.configuration.AnimalConfiguration;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.entity.npc.configuration.NPCConfiguration;
import net.swofty.type.generic.entity.npc.configuration.VillagerConfiguration;
import net.swofty.type.generic.entity.npc.impl.NPCAnimalEntityImpl;
import net.swofty.type.generic.entity.npc.impl.NPCEntityImpl;
import net.swofty.type.generic.entity.npc.impl.NPCViewable;
import net.swofty.type.generic.entity.npc.impl.NPCVillagerEntityImpl;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.i18n.I18n;
import net.swofty.type.generic.user.HypixelPlayer;
import org.tinylog.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public abstract class HypixelNPC {
    private static final int SPAWN_DISTANCE = 48;
    private static final int LOOK_DISTANCE = 5;

    @Getter
    private static final List<HypixelNPC> registeredNPCs = new ArrayList<>();
    @Getter
    private static final Map<UUID, HypixelNPC.PlayerNPCCache> perPlayerNPCs = new ConcurrentHashMap<>();

    @Getter
    private final NPCConfiguration parameters;
    private final DialogueController dialogueController;

    public HypixelNPC(NPCConfiguration configuration) {
        this.parameters = configuration;
        this.dialogueController = new DialogueController(this);
    }

    public static HypixelNPC getFromImpl(HypixelPlayer player, Entity impl) {
        PlayerNPCCache cache = perPlayerNPCs.get(player.getUuid());
        if (cache == null) return null;

        Map<HypixelNPC, Entity> npcs = cache.getEntityImpls();
        if (npcs == null) return null;

        for (Map.Entry<HypixelNPC, Entity> entry : npcs.entrySet()) {
            if (entry.getValue().equals(impl)) {
                return entry.getKey();
            }
        }

        return null;
    }

    /**
     * Removes all dialogue states and NPC cache for a player.
     * Call this when the player disconnects.
     *
     * @param player The player to clean up.
     */
    public static void removeDialogueCache(HypixelPlayer player) {
        DialogueController.removeAllDialogues(player);
        PlayerNPCCache cache = perPlayerNPCs.remove(player.getUuid());
        if (cache != null) {
            cache.getEntityImpls().forEach((npc, entity) -> {
                entity.remove();
            });
        }
    }

    public static void updateForPlayer(HypixelPlayer player) {
        PlayerNPCCache cache = perPlayerNPCs.computeIfAbsent(
            player.getUuid(),
            k -> new PlayerNPCCache()
        );

        synchronized (cache) {
            HypixelNPC.getRegisteredNPCs().forEach((npc) -> {
                // If the main username can't be used (over 16 chars), use a blank space instead and use holograms for all lines
                boolean playerHasNPC = cache.getEntityImpls().containsKey(npc);

                if (!playerHasNPC) {
                    NPCConfiguration config = npc.getParameters();

                    if (!config.visible(player)) return;

                    String[] holograms = Arrays.stream(config.hologramComponents(player))
                            .map(component -> LegacyComponentSerializer.legacySection().serialize(component))
                            .toArray(String[]::new);
                    Pos position = config.position(player);

                    String username = holograms[holograms.length - 1];
                    Entity entity;

                    switch (config) {
                        case HumanConfiguration humanConfig -> entity = new NPCEntityImpl(
                            player,
                            position,
                            username,
                            humanConfig.texture(player),
                            humanConfig.signature(player),
                            holograms,
                            humanConfig
                        );
                        case VillagerConfiguration villagerConfig -> entity = new NPCVillagerEntityImpl(player,
                            position, username, villagerConfig.profession(), villagerConfig, holograms);
                        case AnimalConfiguration animalConfig -> entity = new NPCAnimalEntityImpl(player,
                            position,
                            username,
                            animalConfig.entityType(), animalConfig, holograms);
                        default ->
                            throw new IllegalStateException("Unknown NPCConfiguration type: " + config.getClass().getName());
                    }

                    cache.add(npc, entity);
                    perPlayerNPCs.put(player.getUuid(), cache);
                    return;
                }

                Entity entity = cache.get(npc);
                NPCConfiguration config = npc.getParameters();

                if (!config.visible(player)) {
                    entity.remove();
                    cache.remove(npc);
                    return;
                }

                if (entity.getInstance() != config.instance(player)) {
                    entity.remove();
                    cache.remove(npc);
                    return;
                }

                Pos npcPosition = config.position(player);
                if (!(entity instanceof NPCViewable npcViewable)) {
                    Logger.error("Entity for NPC {} does not implement NPCViewable, skipping update", npc.getName(player));
                    return;
                }
                npcViewable.updateNPC();

                Pos playerPosition = player.getPosition();
                double entityDistance = playerPosition.distance(npcPosition);
                boolean isLookingNPC = config.looking(player)
                        && !npcViewable.getMovementController().isMoving()
                        && player.getGameMode() != GameMode.SPECTATOR;

                // Get inRangeOf list based on entity type
                List<HypixelPlayer> inRange = npcViewable.getInRangeOf();
                if (entityDistance <= SPAWN_DISTANCE) {
                    if (!inRange.contains(player)) {
                        inRange.add(player);
                        entity.updateNewViewer(player);
                    }

                    if (isLookingNPC) {
                        if (entityDistance <= LOOK_DISTANCE) {
                            cache.setLookingAtPlayer(npc);
                            entity.lookAt(player);
                        } else if (cache.stopLookingAtPlayer(npc)) {
                            // over the distance, reset back to default rotation
                            Pos defaultPosition = config.position(player);
                            entity.setView(defaultPosition.yaw(), defaultPosition.pitch());
                        }
                    }
                } else {
                    if (inRange.contains(player)) {
                        inRange.remove(player);
                        entity.updateOldViewer(player);
                        if (cache.stopLookingAtPlayer(npc)) {
                            Pos defaultPosition = config.position(player);
                            entity.setView(defaultPosition.yaw(), defaultPosition.pitch());
                        }
                    }
                }
            });
        }
    }

    public abstract void onClick(NPCInteractEvent event);

    public void register() {
        registeredNPCs.add(this);
    }

    public void unregister() {
        registeredNPCs.remove(this);
    }

    /**
     * Walks this player's copy of the NPC through the supplied points in order.
     * Calling this again replaces the NPC's current route.
     */
    public CompletableFuture<Void> walkPath(HypixelPlayer player, List<Pos> points) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(points, "points");

        PlayerNPCCache cache = perPlayerNPCs.get(player.getUuid());
        Entity entity = cache == null ? null : cache.get(this);
        if (!(entity instanceof NPCViewable viewable)) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException("NPC " + getName(player) + " is not spawned for " + player.getUsername())
            );
        }
        return viewable.getMovementController().walkPath(points);
    }

    public CompletableFuture<Void> walkPath(HypixelPlayer player, Pos... points) {
        return walkPath(player, List.of(points));
    }

    /**
     * Walks every currently spawned per-player copy of this NPC.
     */
    public CompletableFuture<Void> walkPath(List<Pos> points) {
        Objects.requireNonNull(points, "points");
        CompletableFuture<?>[] movements = perPlayerNPCs.values().stream()
                .map(cache -> cache.get(this))
                .filter(Objects::nonNull)
                .map(entity -> ((NPCViewable) entity).getMovementController().walkPath(points))
                .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(movements);
    }

    public CompletableFuture<Void> walkPath(Pos... points) {
        return walkPath(List.of(points));
    }

    /**
     * Stops this player's copy of the NPC at its current position.
     */
    public void stopWalking(HypixelPlayer player) {
        PlayerNPCCache cache = perPlayerNPCs.get(player.getUuid());
        Entity entity = cache == null ? null : cache.get(this);
        if (entity instanceof NPCViewable viewable) {
            viewable.getMovementController().stop();
        }
    }

    /**
     * Stops every currently spawned per-player copy of this NPC.
     */
    public void stopWalking() {
        perPlayerNPCs.values().stream()
                .map(cache -> cache.get(this))
                .filter(Objects::nonNull)
                .map(entity -> (NPCViewable) entity)
                .forEach(viewable -> viewable.getMovementController().stop());
    }

    public void sendNPCMessage(HypixelPlayer player, String message) {
        sendNPCMessage(player, LegacyComponentSerializer.legacySection().deserialize(message));
    }

    public void sendNPCMessage(HypixelPlayer player, Component message) {
        sendNPCMessage(player, message, Sound.sound().type(Key.key("entity.villager.celebrate")).volume(1.0f).pitch(0.8f + new Random().nextFloat() * 0.4f).build());
    }

    public String getName(HypixelPlayer player) {
        return LegacyComponentSerializer.legacySection().serialize(getNameComponent(player));
    }

    /**
     * Compatibility name used by administrative commands.
     */
    public String getName() {
        String className = getClass().getSimpleName().replace("NPC", "").replace("Villager", "");
        return className.replaceAll("(?<=.)(?=\\p{Lu})", " ");
    }

    public Component getNameComponent(HypixelPlayer player) {
        Component name = parameters.chatNameComponent(player);
        return name != null ? name : Component.text(getName());
    }

    public void sendNPCMessage(HypixelPlayer player, String message, Sound sound) {
        sendNPCMessage(player, Component.text(message), sound);
    }

    public void sendNPCMessage(HypixelPlayer player, Component message, Sound sound) {
        player.sendMessage(Component.text()
            .append(Component.text("[NPC] ", NamedTextColor.YELLOW))
                .append(getNameComponent(player))
            .append(Component.text(": ", NamedTextColor.WHITE))
            .append(message)
            .build());
        player.playSound(sound);
    }

    protected DialogueController dialogue() {
        return dialogueController;
    }

    /**
     * Checks if the player is currently in dialogue with this NPC.
     *
     * @param player The player to check.
     * @return True if in dialogue, false otherwise.
     */
    public boolean isInDialogue(HypixelPlayer player) {
        return dialogueController.isInDialogue(player);
    }

    /**
     * Starts a dialogue with the player using the specified dialogue key.
     * Override {@link #dialogues(HypixelPlayer)} to define dialogue sets.
     *
     * @param player The player to start dialogue with.
     * @param key    The dialogue set key to play.
     * @return CompletableFuture that completes when dialogue finishes.
     */
    public CompletableFuture<String> setDialogue(HypixelPlayer player, String key) {
        return dialogueController.setDialogue(player, key);
    }

    /**
     * Override this method to define dialogue sets for this NPC.
     * Return an array of DialogueSet objects keyed by unique identifiers.
     *
     * @param player The player to get dialogues for (they can be different per-player).
     *               May be null when called from {@link #hasDialogue()} — implementations
     *               must handle null gracefully (e.g. return a default set).
     * @return Array of DialogueSet definitions.
     */
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return DialogueSet.EMPTY;
    }

    protected boolean hasDialogue() {
        return dialogues(null).length > 0;
    }

    public static class PlayerNPCCache {
        private final Map<HypixelNPC, Entity> npcs = new ConcurrentHashMap<>();
        private final Set<HypixelNPC> lookingAtPlayer = ConcurrentHashMap.newKeySet();

        public void add(HypixelNPC npc, Entity entity) {
            npcs.put(npc, entity);
        }

        public void remove(HypixelNPC npc) {
            npcs.remove(npc);
            lookingAtPlayer.remove(npc);
        }

        public void setLookingAtPlayer(HypixelNPC npc) {
            lookingAtPlayer.add(npc);
        }

        public boolean stopLookingAtPlayer(HypixelNPC npc) {
            return lookingAtPlayer.remove(npc);
        }

        public Map<HypixelNPC, Entity> getEntityImpls() {
            return npcs;
        }

        public Entity get(HypixelNPC npc) {
            return npcs.get(npc);
        }
    }

    @Builder
    public record DialogueSet(String key, Component[] lines, Sound sound) {
        public static final DialogueSet[] EMPTY = new DialogueSet[0];

        public static class DialogueSetBuilder {
            public DialogueSetBuilder lines(Component[] lines) {
                this.lines = lines;
                return this;
            }

            public DialogueSetBuilder lines(String[] lines) {
                if (lines == null) {
                    this.lines = null;
                    return this;
                }

                Component[] components = new Component[lines.length];
                for (int i = 0; i < lines.length; i++) {
                    components[i] = LegacyComponentSerializer.legacySection().deserialize(lines[i]);
                }
                this.lines = components;
                return this;
            }
        }

        public static DialogueSet ofTranslation(String key, String translationKey) {
            return new DialogueSet(key, I18n.iterable(translationKey), null);
        }

        public static DialogueSet ofTranslation(String key, String translationKey, Component... args) {
            return new DialogueSet(key, I18n.iterable(translationKey, args), null);
        }

        public static DialogueSet ofTranslation(String key, String translationKey, Sound sound) {
            return new DialogueSet(key, I18n.iterable(translationKey), sound);
        }

        public static DialogueSet ofTranslation(String key, String translationKey, Sound sound, Component... args) {
            return new DialogueSet(key, I18n.iterable(translationKey, args), sound);
        }

    }
}
