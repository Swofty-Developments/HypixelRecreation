package net.swofty.type.skyblockgeneric.event.actions.custom;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.title.Title;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.data.datapoints.DatapointStringList;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.event.custom.PlayerRegionChangeEvent;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.Duration;
import java.util.List;

public class ActionNewZoneDisplay implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerRegionChangeEvent event) {
        SkyBlockPlayer player = event.getPlayer();

        if (event.getTo() == null || event.getTo().equals(event.getFrom())) {
            return;
        }

        DatapointStringList discoveredZones = player.getSkyblockDataHandler().get(
            SkyBlockDataHandler.Data.VISITED_REGIONS,
            DatapointStringList.class
        );
        List<String> discoveredZonesList = discoveredZones.getValue();

        if (discoveredZonesList.contains(event.getTo().getName())) {
            return;
        }

        discoveredZonesList.add(event.getTo().getName());
        discoveredZones.setValue(discoveredZonesList);

        switch (event.getTo()) {
            case VILLAGE -> onNewZone(player, RegionType.VILLAGE,
                Text.of("Purchase items at the Market."),
                Text.of("Visit the Auction House."),
                Text.of("Manage your Coins in the Bank."),
                Text.of("Enchant items at the Library."));

            case AUCTION_HOUSE -> onNewZone(player, RegionType.AUCTION_HOUSE,
                Text.of("Auction off your special items."),
                Text.of("Bid on other player's items."));

            case CRYPTS ->
                onNewZone(player, RegionType.CRYPTS, Text.of("Explore the Crypts."), Text.of("Watch out for the Zombies that lurk here!"));

            case FORAGING_CAMP -> onNewZone(player, RegionType.FORAGING_CAMP,
                Text.of("Unlock the <2>Foraging Skill <f>at <2>Lumber Jark<f>."),
                Text.of("Purchase Foraging Tools from the <2>Lumber Merchant<f>."),
                Text.of("Travel to the <a>Birch Park<f>."));

            case MINING_DISTRICT -> onNewZone(player, RegionType.MINING_DISTRICT,
                Text.of("Purchase Mining Tools from the <6>Mining Merchant<f>."),
                Text.of("Learn about <3>Reforges <f>at the <6>Blacksmith<f>."),
                Text.of("Travel to the <8>Coal Mine<f>."));

            case SHENS_AUCTION -> onNewZone(player, RegionType.SHENS_AUCTION,
                Text.of("Talk to Damia."),
                Text.of("Bid on high-end items."));

            case BANK -> onNewZone(player, RegionType.BANK,
                Text.of("Talk to the Banker."),
                Text.of("Store your coins to keep them safe."),
                Text.of("Earn interest on your coins."));

            case DEEP_CAVERNS -> onNewZone(player, RegionType.DEEP_CAVERNS,
                Text.of("Talk to the Lift Operator"),
                Text.of("Mine valuable ores."),
                Text.of("Watch out for mobs!"));

            case MOUNTAIN -> onNewZone(player, RegionType.MOUNTAIN,
                Text.of("Climb to the top!"));

            case DWARVEN_MINES -> onNewZone(player, RegionType.DWARVEN_MINES,
                Text.of("Mine rare ores."));

            case GUNPOWDER_MINES -> onNewZone(player, RegionType.GUNPOWDER_MINES,
                Text.of("Talk to the Lift Operator."),
                Text.of("Explore the caverns."),
                Text.of("Mine Coal, Iron ore, and Gold ore."));

            case LAPIS_QUARRY -> onNewZone(player, RegionType.LAPIS_QUARRY,
                Text.of("The Lift Operator will now let you travel to the <b>Lapis Quarry."),
                Text.of("Access to Lapis Lazuli ore."),
                Text.of("Talk to the Lapis Miner."),
                Text.of("Watch out for the zombies!"));

            // TODO: Replace placeholder text with correct Pigmen's Den zone features
            case PIGMENS_DEN -> onNewZone(player, RegionType.PIGMENS_DEN,
                Text.of("The Lift Operator will now let you travel to the <b>Lapis Quarry."),
                Text.of("Access to Lapis Lazuli ore."),
                Text.of("Talk to the Lapis Miner."),
                Text.of("Watch out for the zombies!"));

            case SLIMEHILL -> onNewZone(player, RegionType.SLIMEHILL,
                Text.of("The Lift Operator will now let you travel to the <b>Slimehill."),
                Text.of("Mine Emerald ore."),
                Text.of("This area is covered with slimes!"));

            case DIAMOND_RESERVE -> onNewZone(player, RegionType.DIAMOND_RESERVE,
                Text.of("The Lift Operator will now let you travel to the <b>Diamond Reserve."),
                Text.of("Mine Diamond ore."),
                Text.of("Beware of deadly monsters!"));

            case OBSIDIAN_SANCTUARY -> onNewZone(player, RegionType.OBSIDIAN_SANCTUARY,
                Text.of("The Lift Operator will now let you travel to the <b>Obsidian Sanctuary."),
                Text.of("Mine Obsidian and Diamond."),
                Text.of("Beware of deadly monsters!"),
                Text.of("Talk to <d>Rhys."));

            case GOLD_MINE -> onNewZone(player, RegionType.GOLD_MINE,
                Text.of("Talk to the Lazy Miner."),
                Text.of("Mine for gold, iron, and coal."),
                Text.of("Visit the Iron and Gold Forgers."),
                Text.of("Visit the Blacksmith."),
                Text.of("Talk to Rusty."));

            case COAL_MINE -> onNewZone(player, RegionType.COAL_MINE,
                Text.of("Mine <8>Coal<f>."),
                Text.of("Travel to the Gold Mine."));

            case FARM -> onNewZone(player, RegionType.FARM,
                Text.of("Learn about the Farming Skill at <e>Farmer Rigby<f>."),
                Text.of("Gather <e>Wheat<f>."),
                Text.of("Learn about Minion Upgrades at <e>Arthur<f>."),
                Text.of("Travel to <e>The Barn<f>."));

            case BIRCH_PARK -> onNewZone(player, RegionType.BIRCH_PARK,
                Text.of("Talk to Charlie."),
                Text.of("Chop down Birch logs."));

            case FOREST -> onNewZone(player, RegionType.FOREST,
                Text.of("Visit the <a>Lumber Jack."),
                Text.of("Chop down trees."),
                Text.of("Travel to the <a>Birch Park<f>."));

            case SPRUCE_WOODS -> onNewZone(player, RegionType.SPRUCE_WOODS,
                Text.of("Chop down Spruce logs."));

            case DARK_THICKET -> onNewZone(player, RegionType.DARK_THICKET,
                Text.of("Chop down Dark Oak Logs."),
                Text.of("Talk to <c>Ryan <f>about the <6>Trial of Fire<f>."));

            case TRIALS_OF_FIRE -> onNewZone(player, RegionType.TRIALS_OF_FIRE,
                Text.of("Compete in a <6>Trial of Fire<f>."));

            case SAVANNA_WOODLAND -> onNewZone(player, RegionType.SAVANNA_WOODLAND,
                Text.of("Chop down Acacia logs."));

            case GRAVEYARD -> onNewZone(player, RegionType.GRAVEYARD,
                Text.of("Fight Zombies."),
                Text.of("Travel to the Spider's Den."),
                Text.of("Talk to <7>Pat<f>."),
                Text.of("Investigate the Catacombs."));

            case COMBAT_SETTLEMENT -> onNewZone(player, RegionType.COMBAT_SETTLEMENT,
                Text.of("Buy Combat Tools from the <c>Weaponsmith <f>and <c>Rosetta<f>."),
                Text.of("Talk to <c>Talk <f>at the <2>Archery Range <f>to learn about bows and arrows."),
                Text.of("Learn about <2>Enchanting <f>at the <2>Library<f>."),
                Text.of("Talk to <c>Maxwell <f>at the <6>Thaumaturgist <f>to learn about <6>Magical Power<f>."));

            case FISHING_OUTPOST -> onNewZone(player, RegionType.FISHING_OUTPOST,
                Text.of("Buy fishing essentials from the <b>Fishing Merchant<f>."),
                Text.of("Talk to <9>Fisherman Gerald <f>and <6>Captain Baha <f>about your <6>Ship<f>."),
                Text.of("Learn about <a>Fishing <f>stats from <b>Gwynnie<f>."));

            case FLOWER_HOUSE -> onNewZone(player, RegionType.FLOWER_HOUSE,
                Text.of("Talk to Marco about <a>Spray Cans<f>."),
                Text.of("Gather flowers."));

            case BAZAAR_ALLEY -> onNewZone(player, RegionType.BAZAAR_ALLEY,
                Text.of("Buy and sell materials in bulk in the Bazaar."));

            case WILDERNESS -> onNewZone(player, RegionType.WILDERNESS,
                Text.of("Fish in the pond."),
                Text.of("Visit <d>Tia the Fairy <f>at the <d>Fairy Pond<f>."),
                Text.of("Discover hidden secrets."));

            case RUINS -> onNewZone(player, RegionType.RUINS,
                Text.of("Explore the ancient ruins."),
                Text.of("Watch out for the guard dogs!"));

            case THE_END -> onNewZone(player, RegionType.THE_END,
                Text.of("Talk to the Pearl Dealer."),
                Text.of("Explore the End Shop."),
                Text.of("Kill Endermen."),
                Text.of("Fight Dragons!"));

            case ARCHERY_RANGE -> onNewZone(player, RegionType.ARCHERY_RANGE,
                Text.of("Talk to Jax to forge special arrows!"));
        }
    }

    public void onNewZone(SkyBlockPlayer player, RegionType zone, Text... features) {
        player.sendMessage("");
        player.sendMessage("<6><l> NEW AREA DISCOVERED!");
        player.sendMessage(Text.of("<7>  ⏣ ").append("<color:{}>{}", zone.getColor(), zone.getName()));
        player.sendMessage("");
        if (features.length > 0) {
            for (Text feature : features) {
                player.sendMessage(Text.of("<7>   ⬛ <f>").append(feature));
            }
        } else {
            player.sendMessage("<7>   ⬛ <c>Not much yet!");
        }
        player.sendMessage("");

        player.playSound(Sound.sound()
            .type(Key.key("random.levelup"))
            .volume(1f)
            .build());

        player.showTitle(
            Text.of("<color:{}>{}", zone.getColor(), zone.getName()),
            Text.of("<6><l>NEW AREA DISCOVERED!"),
            Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(3), Duration.ofSeconds(1))
        );
    }
}
