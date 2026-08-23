package net.swofty.type.skyblockgeneric.event.actions.custom.collection;

import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.generic.event.HypixelEventHandler;
import net.swofty.type.generic.utility.ScheduleUtility;
import net.swofty.type.skyblockgeneric.collection.CollectionCategories;
import net.swofty.type.skyblockgeneric.collection.CollectionCategory;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointCollection;
import net.swofty.type.skyblockgeneric.event.custom.CollectionUpdateEvent;
import net.swofty.type.skyblockgeneric.event.custom.CustomBlockBreakEvent;
import net.swofty.type.skyblockgeneric.user.SkyBlockActionBar;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionCollectionAdd implements HypixelEventClass {


    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(CustomBlockBreakEvent event) {
        if (event.getPlayerPlaced()) return;

        SkyBlockPlayer player = event.getPlayer();

        // Process each dropped item for collection
        for (net.swofty.type.skyblockgeneric.item.SkyBlockItem drop : event.getDrops()) {
            ItemType type = drop.getAttributeHandler().getPotentialType();
            if (type == null) continue;

            int oldAmount = player.getCollection().get(type);
            int dropAmount = drop.getAmount();
            player.getCollection().increase(type, dropAmount);

            player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.COLLECTION, DatapointCollection.class).setValue(
                    player.getCollection()
            );

            HypixelEventHandler.callCustomEvent(new CollectionUpdateEvent(player, type, oldAmount));

            CollectionCategory category = CollectionCategories.getCategory(type);
            if (category == null) continue;
            CollectionCategory.ItemCollection collection = category.getCollection(type);

            final int finalDropAmount = dropAmount;
            final ItemType finalType = type;
            ScheduleUtility.delay(() -> {
                SkyBlockActionBar bar = SkyBlockActionBar.getFor(player);
                int startingPriority = 5;
                int addedAmount = finalDropAmount;

                SkyBlockActionBar.DisplayReplacement existingReplacement = bar.getReplacement(SkyBlockActionBar.BarSection.DEFENSE);
                if (existingReplacement != null) {
                    startingPriority = existingReplacement.priority() + 1;
                    String plain = existingReplacement.display().plain();
                    int separator = plain.indexOf(' ');
                    if (separator > 0) {
                        try {
                            addedAmount = Integer.parseInt(plain.substring(0, separator)) + finalDropAmount;
                        } catch (NumberFormatException ignored) {}
                    }
                }
                if (player.getCollection().getReward(collection) != null) {
                    bar.addReplacement(
                            SkyBlockActionBar.BarSection.DEFENSE,
                            Text.of("<2>+{} {} <7>({}/{})",
                                    addedAmount,
                                    finalType.getDisplayName(),
                                    StringUtility.commaify(player.getCollection().get(finalType)),
                                    StringUtility.shortenNumber(player.getCollection().getReward(collection).requirement())),
                            20,
                            startingPriority
                    );
                } else { //if Collection is maxed
                    bar.addReplacement(
                            SkyBlockActionBar.BarSection.DEFENSE,
                            Text.of("<2>+{} {} <7>({})",
                                    addedAmount,
                                    finalType.getDisplayName(),
                                    StringUtility.commaify(player.getCollection().get(finalType))),
                            20,
                            startingPriority
                    );
                }
            }, 5);
        }
    }
}
