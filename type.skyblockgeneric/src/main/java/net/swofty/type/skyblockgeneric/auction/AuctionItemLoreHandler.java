package net.swofty.type.skyblockgeneric.auction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import net.swofty.commons.skyblock.auctions.AuctionItem;
import net.swofty.commons.text.Text;
import net.swofty.commons.text.TextBody;
import net.swofty.type.skyblockgeneric.data.monogdb.CoopDatabase;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.text.LoreText;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public record AuctionItemLoreHandler(AuctionItem auctionItem) {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private static final String ITEM = "auction_item";
    private static final String DETAILS = "auction_details";
    private static final String TOP_BID = "auction_top_bid";
    private static final String VIEWER = "auction_viewer";
    private static final String STATUS = "auction_status";
    private static final String ACTION = "auction_action";

    private static final int ITEM_ORDER = 1000;
    private static final int DETAILS_ORDER = 1010;
    private static final int TOP_BID_ORDER = 1020;
    private static final int VIEWER_ORDER = 1030;
    private static final int STATUS_ORDER = 1040;
    private static final int ACTION_ORDER = 1050;

    @JsonIgnore
    @Transient
    public List<String> getLore() {
        return getLore(null);
    }

    @JsonIgnore
    @Transient
    public List<String> getLore(SkyBlockPlayer player) {
        Locale locale = player != null ? player.getLocale() : Locale.US;
        List<Text> lore = getLoreTexts(player);
        List<String> toReturn = new ArrayList<>(lore.size());
        lore.forEach(line -> toReturn.add(LEGACY.serialize(GlobalTranslator.render(line.asComponent(), locale))));
        return toReturn;
    }

    @JsonIgnore
    @Transient
    public List<Text> getLoreTexts() {
        return getLoreTexts(null);
    }

    @JsonIgnore
    @Transient
    public List<Text> getLoreTexts(SkyBlockPlayer player) {
        SkyBlockItem skyBlockItem = new SkyBlockItem(auctionItem.getItem());
        LoreText lore = new LoreText(skyBlockItem, player);

        List<Text> itemLore = player == null
                ? skyBlockItem.getLoreText()
                : skyBlockItem.getLoreText(player);
        lore.section(ITEM, ITEM_ORDER).lines(itemLore);

        TextBody.Section details = lore.section(DETAILS, DETAILS_ORDER);
        details.line("<8><m>----------------------");
        details.line("<7>Seller: {}", SkyBlockPlayer.getDisplayName(auctionItem.getOriginator()));

        if (auctionItem.isBin()) {
            details.line("<7>Buy it now: <6>{} coins", auctionItem.getStartingPrice());
        } else if (auctionItem.getBids().isEmpty()) {
            details.line("<7>Starting bid: <6>{} coins", auctionItem.getStartingPrice());
        } else {
            details.line("<7>Bids: <a>{} bid{}", auctionItem.getBids().size(),
                    auctionItem.getBids().size() == 1 ? "" : "s");

            AuctionItem.Bid topBid = auctionItem.getBids().stream().max(Comparator.comparing(AuctionItem.Bid::value)).orElse(null);
            lore.section(TOP_BID, TOP_BID_ORDER).separated()
                    .line("<7>Top bid: <6>{} coins", topBid.value())
                    .line("<7>Bidder: {}", SkyBlockPlayer.getDisplayName(topBid.uuid()));
        }

        if (player != null) {
            TextBody.Section viewer = lore.section(VIEWER, VIEWER_ORDER).separated();
            if (auctionItem.getOriginator().equals(player.getUuid())) {
                viewer.line("<a>This is your own auction!");
            } else {
                CoopDatabase.Coop viewerCoop = CoopDatabase.getFromMember(player.getUuid());
                if (viewerCoop != null && viewerCoop.members().contains(auctionItem.getOriginator())) {
                    viewer.line("<a>This is a coop member's auction!");
                }
            }
        }

        TextBody.Section status = lore.section(STATUS, STATUS_ORDER).separated();
        if (auctionItem.isBin() && !auctionItem.getBids().isEmpty()) {
            status.line("<7>Status: <a>Purchased");
        } else if (auctionItem.getEndTime() > System.currentTimeMillis()) {
            status.line("<7>Ends in: <e>{:time}", auctionItem.getEndTime() - System.currentTimeMillis());
        } else {
            status.line("<7>Status: <a>Ended!");
        }

        lore.section(ACTION, ACTION_ORDER).separated().line("<e>Click to inspect!");
        return lore.render();
    }
}
