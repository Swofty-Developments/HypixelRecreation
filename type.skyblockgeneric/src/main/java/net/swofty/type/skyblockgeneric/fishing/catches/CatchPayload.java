package net.swofty.type.skyblockgeneric.fishing.catches;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointCollection;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointTrophyFish;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureSpawner;
import net.swofty.type.skyblockgeneric.fishing.FishingService;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

/**
 * Sealed view of what the rod can pull out of the water. Each variant
 * carries exactly its own fields — no bag of nullable @Nullable strings —
 * and owns its own {@link #apply(CatchAwardContext)} so the FishingService
 * dispatches by polymorphism rather than switch-on-kind.
 */
public sealed interface CatchPayload {

    /** Fishing skill XP to award; sea creatures intentionally return 0 (their kill awards XP separately). */
    double skillXp();

    /** Short human label for telemetry / chat — variants pick the format. */
    String summary();

    /** Run the catch's side-effects on the player. */
    void apply(CatchAwardContext ctx);

    /* ------------------------------------------------------------------ */
    /*  Variants                                                          */
    /* ------------------------------------------------------------------ */

    record Multi(List<CatchPayload> payloads) implements CatchPayload {
        @Override public double skillXp() { return payloads.stream().mapToDouble(CatchPayload::skillXp).sum(); }
        @Override public String summary() { return payloads.stream().map(CatchPayload::summary).toList().toString(); }
        @Override public void apply(CatchAwardContext ctx) { payloads.forEach(payload -> payload.apply(ctx)); }
    }

    record Item(String itemId, int amount, double skillXp, boolean fromTreasure) implements CatchPayload {
        @Override public String summary() { return (fromTreasure ? "treasure " : "") + amount + "x " + itemId; }

        @Override
        public void apply(CatchAwardContext ctx) {
            ItemType type = ItemType.valueOf(itemId);
            ctx.player().addAndUpdateItem(type, amount);
            ctx.player().getCollection().increase(type, amount);
            ctx.player().getSkyblockDataHandler()
                    .get(SkyBlockDataHandler.Data.COLLECTION, DatapointCollection.class)
                    .setValue(ctx.player().getCollection());

            Text prefix = fromTreasure ? Text.of("<b><l>TREASURE! ") : Text.of("<b><l>GOOD CATCH! ");
            ctx.player().sendMessage(prefix.append("<7>You found {}x <a>{}<7>!", amount, itemId));
        }
    }

    record SeaCreature(String seaCreatureId, double skillXp, int spawnCount) implements CatchPayload {
        public SeaCreature(String seaCreatureId, double skillXp) {
            this(seaCreatureId, skillXp, 1);
        }

        public SeaCreature withDoubleHook() {
            return new SeaCreature(seaCreatureId, skillXp, 2);
        }

        @Override public String summary() {
            return spawnCount > 1 ? "double-hook " + seaCreatureId : "sea creature " + seaCreatureId;
        }

        @Override
        public void apply(CatchAwardContext ctx) {
            if (spawnCount > 1) {
                ctx.player().sendMessage("<a><l>DOUBLE HOOK!");
            }
            for (int i = 0; i < spawnCount; i++) {
                SeaCreatureSpawner.spawn(ctx.player(), seaCreatureId, ctx.hookPosition());
            }
        }
    }

    record TrophyFish(String definitionId, TrophyTier tier, String itemId, double skillXp) implements CatchPayload {
        @Override public String summary() { return "trophy " + tier.name() + " " + definitionId; }

        @Override
        public void apply(CatchAwardContext ctx) {
            ItemType type = ItemType.valueOf(itemId);
            ctx.player().addAndUpdateItem(type, 1);

            DatapointTrophyFish.TrophyFishData data = ctx.player().getTrophyFishData();
            data.getProgress(definitionId).increment(tier.name());
            ctx.player().getSkyblockDataHandler()
                    .get(SkyBlockDataHandler.Data.TROPHY_FISH, DatapointTrophyFish.class)
                    .setValue(data);

            ctx.player().sendMessage("<b><l>TROPHY FISH! </l><7>You reeled in a <color:{0}>{1} <a>{2}<7>!",
                    tier.colour(), tier.displayName(), definitionId);
        }
    }

    record Quest(String itemId, int amount, double skillXp, String message) implements CatchPayload {
        @Override public String summary() { return "quest " + itemId; }

        @Override
        public void apply(CatchAwardContext ctx) {
            ctx.player().addAndUpdateItem(ItemType.valueOf(itemId), amount);
            ctx.player().sendMessage("<d><l>QUEST CATCH! </l><7>{}", message);
        }
    }

    record Special(String itemId, int amount, double skillXp, boolean corrupted) implements CatchPayload {
        @Override public String summary() { return "special " + itemId; }

        @Override
        public void apply(CatchAwardContext ctx) {
            ItemType type = ItemType.valueOf(itemId);
            ctx.player().addAndUpdateItem(type, amount);
            ctx.player().getCollection().increase(type, amount);
            Text prefix = corrupted ? Text.of("<5><l>CORRUPTED! ") : Text.of("<d><l>WONDROUS CATCH! ");
            ctx.player().sendMessage(prefix.append("<7>You found <a>{}<7>!", itemId));
        }
    }
}
