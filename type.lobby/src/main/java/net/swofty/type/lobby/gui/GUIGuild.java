package net.swofty.type.lobby.gui;

import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.guild.GuildData;
import net.swofty.commons.guild.GuildMember;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.HypixelGenericLoader;
import net.swofty.type.generic.data.HypixelDataHandler;
import net.swofty.type.generic.data.datapoints.DatapointString;
import net.swofty.type.generic.gui.HypixelSignGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.View;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.guild.GuildManager;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class GUIGuild implements View<GUIGuild.GuildState> {

    @Override
    public ViewConfiguration<GuildState> configuration() {
        return new ViewConfiguration<>("Guild", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<GuildState> layout, GuildState state, ViewContext ctx) {
        if (state.guild() == null) {
            layoutNoGuild(layout, ctx);
        } else {
            layoutWithGuild(layout, state.guild(), ctx);
        }
    }

    private void layoutNoGuild(ViewLayout<GuildState> layout, ViewContext ctx) {
        HypixelPlayer player = ctx.player();
        int level = player.getExperienceHandler().getLevel();
        int achievementPoints = player.getAchievementHandler().getTotalPoints();

        layout.slot(2, ItemStacks.head(
            player.getSkin(),
            player.getFullDisplayName(),
            Text.of("""
                    <7>Hypixel Level: <6>{}
                    <7>Achievement Points: <e>{:,}
                    <7>Guild: <b>None""", level, achievementPoints).lines()
        ));
        layout.slot(3, ItemStacks.head("e063eedb2184354bd43a19deffba51b53dd6b7222f8388caa239cabcdce84", """
                <a>Friends
                <7>View your Hypixel friends' profiles,
                <7>and interact with your online friends!"""));
        layout.slot(4, ItemStacks.head("667963ca1ffdc24a10b397ff8161d0da82d6a3f4788d5f67f1a9f9bfbc1eb1", """
                <a>Party
                <7>Create a party and join up with
                <7>other players to play games
                <7>together!"""));
        layout.slot(5, ItemStacks.head("fe8b59f8cce510809427c3843cf575fae8fe6a8b7d1560dd46958d148563815", """
                <a>Guild
                <7>Form a guild with other Hypixel
                <7>players to conquer game modes and
                <7>work towards common Hypixel
                <7>rewards."""));
        layout.slot(6, ItemStacks.head("9993a356809532d696841a37a0549b81b159b79a7b2919cff4e5abdfea83d66", """
                <a>Recent Players
                <7>View players you have played recent
                <7>games with."""));

        if (canCreateGuild(player)) {
            layout.slot(29, ItemStacks.item(Material.OAK_SIGN, """
                    <a>Create Guild
                    <7>Create a guild with your own tag,
                    <7>settings and progression.

                    <e>Click to create!"""), (click, viewCtx) -> new HypixelSignGUI(viewCtx.player())
                .open(new String[]{"Guild Name", "Enter guild name"})
                .thenAccept(name -> {
                    if (name == null || name.isBlank()) {
                        return;
                    }
                    GuildManager.createGuild(viewCtx.player(), name.trim());
                }));
        } else {
            layout.slot(29, ItemStacks.item(Material.OAK_SIGN, """
                    <c>Create Guild
                    <7>Only players with <a>VIP<6>+<7> or higher can
                    <7>create guilds, but anybody can join
                    <7>them."""));
        }

        layout.slot(31, ItemStacks.item(Material.PAPER, """
                <a>Guild Finder
                <7>Find a Guild you can join based on
                <7>your favorite games.

                <e>Click to browse!"""));
        layout.slot(33, ItemStacks.item(Material.BOOK, """
                <a>Search Guilds
                <7>Click here to search guilds you can
                <7>join on the Hypixel Network website!"""),
            (_, context) -> context.player().sendMessage("<click:url:'https://github.com/Swofty-Developments/HypixelRecreation'><c>This Feature is not there yet. <a>Open a Pull request HERE to get it added quickly!"));
    }

    private void layoutWithGuild(ViewLayout<GuildState> layout, GuildData guild, ViewContext ctx) {
        HypixelPlayer player = ctx.player();
        int level = player.getExperienceHandler().getLevel();
        int achievementPoints = player.getAchievementHandler().getTotalPoints();

        layout.slot(2, ItemStacks.head(
            player.getSkin(),
            player.getFullDisplayName(),
            Text.of("""
                    <7>Hypixel Level: <6>{}
                    <7>Achievement Points: <e>{:,}
                    <7>Guild: <b>{}""", level, achievementPoints, guild.getName()).lines()
        ));
        layout.slot(3, ItemStacks.head("e063eedb2184354bd43a19deffba51b53dd6b7222f8388caa239cabcdce84", """
                <a>Friends
                <7>View your Hypixel friends' profiles,
                <7>and interact with your online friends!"""));
        layout.slot(4, ItemStacks.head("667963ca1ffdc24a10b397ff8161d0da82d6a3f4788d5f67f1a9f9bfbc1eb1", """
                <a>Party
                <7>Create a party and join up with
                <7>other players to play games
                <7>together!"""));
        layout.slot(5, ItemStacks.head("fe8b59f8cce510809427c3843cf575fae8fe6a8b7d1560dd46958d148563815", """
                <a>Guild
                <7>Form a guild with other Hypixel
                <7>players to conquer game modes and
                <7>work towards common Hypixel
                <7>rewards."""));
        layout.slot(6, ItemStacks.head("9993a356809532d696841a37a0549b81b159b79a7b2919cff4e5abdfea83d66", """
                <a>Recent Players
                <7>View players you have played recent
                <7>games with."""));

        GuildMember self = guild.getMember(player.getUuid());
        String rankName = self != null ? self.getRankName() : "Unknown";

        layout.slot(18, ItemStacks.item(Material.WRITABLE_BOOK, """
                <a>Invite Player
                <7>Click here to invite a player to your
                <7>Guild."""), (click, viewCtx) -> new HypixelSignGUI(viewCtx.player())
            .open(new String[]{"Invite Player", "Enter username"})
            .thenAccept(name -> {
                if (name == null || name.isBlank()) {
                    return;
                }
                GuildManager.invitePlayer(viewCtx.player(), name.trim());
            }));

        layout.slot(19, ItemStacks.item(Material.PAINTING, """
                <a>Guild Information
                <7>Name: <6>{}
                <7>Rank: <6>{}
                <7>Daily Exp: <6>{:,}
                <7>Members: <6>{}<b>/<6>{}""",
            guild.getName(), rankName, 0, guild.getMembers().size(), GuildData.MAX_MEMBERS));

        layout.slot(20, ItemStacks.item(Material.COMPARATOR, """
                <a>Guild Settings
                <7>Edit settings such as your tag,
                <7>permissions and guild finder options

                <e>Click to configure!"""),
            (click, viewCtx) -> viewCtx.push(new GUIGuildSettings(), new GUIGuildSettings.GuildSettingsState(guild)));

        layout.slot(21, ItemStacks.item(Material.ENCHANTED_BOOK, """
                <a>Weekly Guild Quest
                <e>To complete the quest, Guild Members
                <e>need to complete Challenges in any
                <e>game.
                <7>Tier 1: <6>0<7>/25
                <7>Tier 2: <6>0<7>/100
                <7>Tier 3: <6>0<7>/500
                <7>Tier 4: <6>0<7>/1500

                <7>Reward: <2>50,000 Guild Experience
                <e>Resets in: 0 hours, 0 minutes"""));

        long expIntoCurrentLevel = getExpIntoCurrentLevel(guild);
        long expNeededForNext = guild.getGexpForLevel(guild.getLevel() + 1);
        long expRemaining = Math.max(0, expNeededForNext - expIntoCurrentLevel);
        double levelProgress = expNeededForNext <= 0 ? 1.0 : Math.min(1.0, (double) expIntoCurrentLevel / (double) expNeededForNext);
        int progressPercent = (int) Math.round(levelProgress * 100.0);

        layout.slot(22, ItemStacks.item(Material.BREWING_STAND, """
                <a>Guild Leveling
                <7>Guild Level: <6>{0}
                <6>{0} {1} <6>{2}
                <7>Exp until next level: <6>{3:,} <7>(<6>{4}%<7>)

                <7>Today's exp: <6>{5:,}
                <7>The guild is earning exp at <6>100%<7> rate!

                <6>Today's exp \\< 200,000 → 100%
                <7>Today's exp >= 200,000 → 10%
                <7>Today's exp >= 250,000 → 3%

                <e>Click to view leveling rewards!""",
            guild.getLevel(), createProgressBar(levelProgress, 40), guild.getLevel() + 1,
            expRemaining, progressPercent, 0),
            (click, viewCtx) -> viewCtx.push(new GUIGuildLevelingRewards(), GUIGuildLevelingRewards.createState(guild)));

        layout.slot(23, ItemStacks.item(Material.DIAMOND, """
                <a>Guild Achievements
                <7>Achievements completed: <e>{}<7>/26

                <e>Click to view!""", countCompletedAchievements(guild)),
            (click, viewCtx) -> viewCtx.push(new GUIGuildAchievements(), new GUIGuildAchievements.GuildAchievementsState(guild)));

        layout.slot(24, ItemStacks.head("7873c12bffb5251a0b88d5ae75c7247cb39a75ff1a81cbe4c8a39b311ddeda", """
                <a>Guild Discord
                <7>Your Guild has a Discord
                <7>server that Guild Members can
                <7>join.

                <e>Click to view Invite Link
                <e>Right-click to modify"""), (click, viewCtx) -> {
            if (click.click() instanceof Click.Right) {
                new HypixelSignGUI(viewCtx.player())
                    .open(new String[]{"Discord Link", "Paste invite URL"})
                    .thenAccept(link -> {
                        if (link == null || link.isBlank()) {
                            return;
                        }
                        GuildManager.changeSetting(viewCtx.player(), "discord", link.trim());
                    });
                return;
            }

            String discordLink = guild.getDiscordLink();
            if (discordLink == null || discordLink.isBlank()) {
                viewCtx.player().sendMessage("<c>Your guild does not have a Discord link set.");
                return;
            }

            viewCtx.player().sendMessage("<click:url:'{}'><e>Click here to open your guild Discord invite", discordLink);
        });

        layout.slot(25, ItemStacks.item(Material.PAPER, """
                <a>Guild Finder
                <7>Find a Guild you can join based on
                <7>your favorite games.

                <e>Click to browse!"""));

        layout.slot(33, ItemStacks.item(Material.HOPPER, """
                <a>Change sort
                <7>Current sort: <b>Last Online
                <7>Sorting order: <b>Normal

                <b>Last Online<7>: Sorts by who was
                <7>most recently online
                <b>Guild Rank<7>: Shows highest Guild
                <7>Rank first
                <b>Veterancy<7>: How long they've
                <7>been in the guild
                <b>Alphabetical<7>: Show everyone
                <7>listed from A-Z
                <b>AP<7>: Sort by Achievement Points
                <b>Level<7>: Sort by Hypixel Level

                <e>LEFT CLICK<7> to change between
                <7>all the available sorting options.

                <e>RIGHT CLICK<7> to reverse the
                <7>current order!"""));

        layout.slot(34, ItemStacks.item(Material.OAK_SIGN, "<a>Search Players"),
            (click, viewCtx) -> viewCtx.push(new GUIGuildMembers(), GUIGuildMembers.createState(guild)));

        layout.slot(35, ItemStacks.item(Material.ARROW, """
                <a>Next Page
                <e>LEFT CLICK<7> to go to the next
                <7>page
                <e>RIGHT CLICK<7> to go to the last
                <7>page
                <7>Page 1/{}""", Math.max(1, (int) Math.ceil(guild.getMembers().size() / 18.0))),
            (click, viewCtx) -> viewCtx.push(new GUIGuildMembers(), GUIGuildMembers.createState(guild)));

        int[] previewSlots = {36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53};
        List<GuildMember> members = guild.getMembers();
        for (int i = 0; i < previewSlots.length && i < members.size(); i++) {
            layout.slot(previewSlots[i], buildMemberPreview(members.get(i)));
        }
    }

    private boolean canCreateGuild(HypixelPlayer player) {
        return player.getRank().isEqualOrHigherThan(Rank.VIP_PLUS);
    }

    private ItemStack.Builder buildMemberPreview(GuildMember member) {
        UUID uuid = member.getUuid();
        Text displayName = HypixelPlayer.getDisplayName(uuid);
        String memberSince = formatDuration(System.currentTimeMillis() - member.getJoinedAt());

        HypixelPlayer loadedPlayer = HypixelGenericLoader.getLoadedPlayers().stream()
            .filter(p -> p.getUuid().equals(uuid))
            .findFirst()
            .orElse(null);

        List<Text> lore;
        PlayerSkin skin;

        if (loadedPlayer != null) {
            lore = Text.of("""
                    <7>Hypixel Level: <6>{}
                    <7>Achievement Points: <e>{:,}
                    <7>Guild Rank: <b>{}
                    <7>Member since: <b>{}

                    <7>Online Status: <b>Online""",
                loadedPlayer.getExperienceHandler().getLevel(),
                loadedPlayer.getAchievementHandler().getTotalPoints(),
                member.getRankName(), memberSince).lines();
            skin = loadedPlayer.getSkin();
        } else {
            lore = Text.of("""
                    <7>Hypixel Level: <6>?
                    <7>Achievement Points: <e>?
                    <7>Guild Rank: <b>{}
                    <7>Member since: <b>{}

                    <7>Last Online: <b>Unknown""", member.getRankName(), memberSince).lines();
            skin = resolveOfflineSkin(uuid);
        }

        if (skin != null) {
            return ItemStacks.head(skin, displayName, lore);
        }
        return ItemStacks.item(Material.PLAYER_HEAD, 1, displayName, lore);
    }

    private PlayerSkin resolveOfflineSkin(UUID uuid) {
        try {
            HypixelDataHandler dataHandler = HypixelDataHandler.getOfOfflinePlayer(uuid);
            String texture = dataHandler.get(HypixelDataHandler.Data.SKIN_TEXTURE, DatapointString.class).getValue();
            String signature = dataHandler.get(HypixelDataHandler.Data.SKIN_SIGNATURE, DatapointString.class).getValue();
            if (texture == null || signature == null || texture.equals("null") || signature.equals("null")) {
                return null;
            }
            return new PlayerSkin(texture, signature);
        } catch (Exception ignored) {
            return null;
        }
    }

    private long getExpIntoCurrentLevel(GuildData guild) {
        long accumulated = 0;
        for (int level = 1; level <= guild.getLevel(); level++) {
            accumulated += guild.getGexpForLevel(level);
        }
        return Math.max(0, guild.getTotalGexp() - accumulated);
    }

    private int countCompletedAchievements(GuildData guild) {
        int completed = 0;
        int level = guild.getLevel();
        long gexp = guild.getTotalGexp();
        int memberCount = guild.getMembers().size();

        int[] prestigeTiers = {20, 40, 60, 80, 100};
        int[] expKingTiers = {50000, 100000, 150000, 200000, 250000, 275000, 300000};
        int[] familyTiers = {5, 15, 30, 40, 50, 60, 70};

        for (int tier : prestigeTiers) {
            if (level >= tier) completed++;
        }
        for (int tier : expKingTiers) {
            if (gexp >= tier) completed++;
        }
        for (int tier : familyTiers) {
            if (memberCount >= tier) completed++;
        }

        return completed;
    }

    private Text createProgressBar(double progress, int length) {
        int filled = (int) Math.round(progress * length);
        return Text.of("<6>{}<7>{}", "|".repeat(filled), "|".repeat(length - filled));
    }

    private String formatDuration(long millis) {
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        long hours = TimeUnit.MILLISECONDS.toHours(millis) % 24;
        if (days > 0) return days + " days, " + hours + " hours ago";
        if (hours > 0) return hours + " hours ago";
        return "Just now";
    }

    public record GuildState(@Nullable GuildData guild) {
    }
}
