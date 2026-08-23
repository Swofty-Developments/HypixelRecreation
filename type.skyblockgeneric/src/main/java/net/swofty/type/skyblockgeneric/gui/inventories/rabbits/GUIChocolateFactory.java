package net.swofty.type.skyblockgeneric.gui.inventories.rabbits;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.StatefulView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory;
import net.swofty.type.skyblockgeneric.rabbits.ChocolateFactoryHelper;
import net.swofty.type.skyblockgeneric.rabbits.ChocolateRabbit;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIChocolateFactory implements StatefulView<GUIChocolateFactory.State> {
    private static final int RABBIT_BARN_MAX_LEVEL = 247;
    private static final int RABBIT_BARN_EXTRA_CAPACITY = 2;
    private static final int HAND_BAKED_MAX_LEVEL = 10;
    private static final int TIME_TOWER_MAX_LEVEL = 15;
    private static final int RABBIT_SHRINE_MAX_LEVEL = 20;
    private static final int COACH_JACKRABBIT_MAX_LEVEL = 20;
    private static final int EMPLOYEE_MAX_LEVEL = 220;
    private static final int TOTAL_RABBITS = ChocolateRabbit.values().length;
    private static final long MILLIS_PER_MINUTE = 60_000L;
    private static final long MILLIS_PER_SECOND = 1_000L;

    private static final Text NOT_ENOUGH_CHOCOLATE_MESSAGE = Text.of("<c>You don't have enough Chocolate!");
    private static final Text UPGRADE_DIVIDER = Text.of("<8><m>-----------------");
    private static final Text NOT_ENOUGH_CHOCOLATE_SHORT = Text.of("<c>Not enough chocolate!");
    private static final Text CLICK_TO_UPGRADE = Text.of("<e>Click to upgrade!");
    private static final Text COST_LABEL = Text.of("<7>Cost");

    // Texture IDs
    private static final String CHOCOLATE_TEXTURE = "9a815398e7da89b1bc08f646cafc8e7b813da0be0eec0cce6d3eff5207801026";
    private static final String HOPPITY_TEXTURE = "b79e7f3341b672d9de6564cbaca052a6a723ea466a2e66af35ba1ba855f0d692";
    private static final String COACH_JACKRABBIT_TEXTURE = "bc0cc67e79c228e541e68aeb1d81ed7af51166622ad4db9417d7a29d1b89af95";

    private static final Sound CLICK_SOUND = Sound.sound(Key.key("block.note_block.bit"), Sound.Source.PLAYER, 1.0f, 1.21f);
    private static final Sound NOT_ENOUGH_CHOCOLATE_SOUND = Sound.sound(Key.key("entity.enderman.teleport"), Sound.Source.PLAYER, 8.0f, 0.0f);
    private static final Sound UPGRADE_SOUND = Sound.sound(Key.key("block.note_block.pling"), Sound.Source.PLAYER, 8.0f, 4.05f);

    // Employee slots (slots 28-34)
    private static final int[] EMPLOYEE_SLOTS = {28, 29, 30, 31, 32, 33, 34};
    private static final String[] EMPLOYEE_NAMES = {
            "Rabbit Bro", "Rabbit Cousin", "Rabbit Sis", "Rabbit Daddy",
            "Rabbit Granny", "Rabbit Uncle", "Rabbit Dog"
    };
    private static final String[] EMPLOYEE_TEXTURES = {
            "287934bdd9df2705b251bb997e029b18c1e94df12992b8107e74497b205ca7e8",
            "a982825c01b658f348a099b4579029a180d2e415183951b2e6e5e27257df4254",
            "fd076e0e3d4072d0fffee0a87a5d726fc34b2bcec38c264fb9b67871a8ead633",
            "57cab0c34d7ddcf72db56ff36f2883f554cff76eb5d3b3e0562338036c976043",
            "d6eb2d85ee8e3af1c2ec934beb70a39c5e766b23bdab63210bd2aacd73cbbfc8",
            "a865176723a0b9ee2916180a55a04cccb7704ad1f31fdf3e9d89c798f6802e6b",
            "35ca98bede3865dd1205e4d091036cd9dc36791b83ea4e0ff4a99ad61b71e898"
    };
    private static final String[] EMPLOYEE_SUBTITLES = {
            "Ambition on two feet!", "Laid-back legend!", "Rebel with a cause!",
            "CFO with nerves of steel!", "Storyteller supreme!",
            "Stuck in a highlight reel!", "Making chocolate, not eating it!"
    };

    public record State() {
    }

    @Override
    public State initialState() {
        return new State();
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withString((state, ctx) -> "Chocolate Factory", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void onOpen(State state, ViewContext ctx) {
        // Update production based on time elapsed (handles offline production)
        ChocolateFactoryHelper.updateProduction((SkyBlockPlayer) ctx.player());
    }

    @Override
    public void onRefresh(State state, ViewContext ctx) {
        ChocolateFactoryHelper.updateProduction((SkyBlockPlayer) ctx.player());
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.fill(layout);

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        ChocolateFactoryHelper.updateProduction(player);
        DatapointChocolateFactory.ChocolateFactoryData data = ChocolateFactoryHelper.getData(player);

        // Slot 13: Chocolate cookie (clickable)
        layout.slot(13, (s, c) -> {
            SkyBlockPlayer p = (SkyBlockPlayer) c.player();
            DatapointChocolateFactory.ChocolateFactoryData d = ChocolateFactoryHelper.getData(p);

            return ItemStacks.head(CHOCOLATE_TEXTURE, """
                    <e>{} <6>Chocolate
                    <7><6>Chocolate<7>, of course, is not a valid
                    <7>source of <a>nutrition<7>. This, however,
                    <7>does not stop it from being <d>awesome<7>.

                    <7>Chocolate Production
                    <6>{} <8>per second

                    <7>All-time Chocolate: <6>{}

                    <e>Click to uncover the meaning of life!""",
                    ChocolateFactoryHelper.formatChocolate(d.getChocolate()),
                    String.format("%.2f", d.getChocolatePerSecond()),
                    ChocolateFactoryHelper.formatChocolate(d.getChocolateAllTime()));
        }, (click, c) -> {
            SkyBlockPlayer p = (SkyBlockPlayer) c.player();
            ChocolateFactoryHelper.handleClick(p);
            p.playSound(CLICK_SOUND);
            c.session(State.class).refresh();
        });

        // Slot 27: Prestige/Chocolate Factory level
        layout.slot(27, (s, c) -> {
            SkyBlockPlayer p = (SkyBlockPlayer) c.player();
            DatapointChocolateFactory.ChocolateFactoryData d = ChocolateFactoryHelper.getData(p);
            int prestigeLevel = d.getPrestigeLevel();
            String prestigeColor = ChocolateFactoryHelper.getPrestigeRankColor(prestigeLevel);

            return ItemStacks.item(Material.DROPPER, """
                    {}
                    <7>Chocolate Production Multiplier: <6>{}
                    <7>Max Rabbit Rarity: <b><l>DIVINE</l>
                    <7>Max Chocolate: <6>60B
                    <7>Max Employee: [220] <b>Board Member
                    <7>Max <c>Rabbit Hitman <7>Slots: <6>28

                    <7>Chocolate this Prestige: <6>{}

                    {}""",
                    Text.of(prestigeColor + "Chocolate Factory {:roman}", prestigeLevel + 1),
                    String.format("%.1fx", d.getShrineMultiplier() * d.getCoachMultiplier()),
                    ChocolateFactoryHelper.formatChocolate(d.getChocolateAllTime()),
                    prestigeLevel >= 6 ? Text.of("<a>You have reached max prestige!") : Text.of("<e>Click to prestige!"));
        });

        // Employee slots (28-34)
        setupEmployeeSlots(layout);

        // Slot 35: Rabbit Barn
        layout.slot(35, (s, c) -> createRabbitBarnItem((SkyBlockPlayer) c.player()), (click, c) -> {
            SkyBlockPlayer p = (SkyBlockPlayer) c.player();
            DatapointChocolateFactory.ChocolateFactoryData d = ChocolateFactoryHelper.getData(p);

            if (d.getRabbitBarnLevel() >= RABBIT_BARN_MAX_LEVEL) {
                p.sendMessage("<c>Your Rabbit Barn is already at maximum capacity!");
                p.playSound(NOT_ENOUGH_CHOCOLATE_SOUND);
                return;
            }

            if (ChocolateFactoryHelper.purchaseUpgrade(p, ChocolateFactoryHelper.UpgradeType.RABBIT_BARN)) {
                d = ChocolateFactoryHelper.getData(p);
                p.sendMessage("<7>Your <a>Rabbit Barn <7>capacity has been increased to <a>{} Rabbits<7>!",
                        d.getMaxRabbitSlots() + RABBIT_BARN_EXTRA_CAPACITY);
                p.playSound(UPGRADE_SOUND);
            } else {
                sendNotEnoughChocolateFeedback(p);
            }
            c.session(State.class).refresh();
        });

        // Slot 38: Hand-Baked Chocolate
        layout.slot(38, (s, c) -> createHandBakedChocolateItem((SkyBlockPlayer) c.player()), (click, c) -> {
            SkyBlockPlayer p = (SkyBlockPlayer) c.player();
            DatapointChocolateFactory.ChocolateFactoryData d = ChocolateFactoryHelper.getData(p);

            if (d.getHandBakedChocolateLevel() >= HAND_BAKED_MAX_LEVEL) {
                p.sendMessage("<c>You only have so many fingers! You can't click any faster!");
                p.playSound(NOT_ENOUGH_CHOCOLATE_SOUND);
                return;
            }

            if (ChocolateFactoryHelper.purchaseUpgrade(p, ChocolateFactoryHelper.UpgradeType.HAND_BAKED_CHOCOLATE)) {
                d = ChocolateFactoryHelper.getData(p);
                p.sendMessage("<7>You will now produce <6>+{} Chocolate <7>per click!", d.getClickPower());
                p.playSound(UPGRADE_SOUND);
            } else {
                sendNotEnoughChocolateFeedback(p);
            }
            c.session(State.class).refresh();
        });

        // Slot 39: Time Tower
        layout.slot(39, (s, c) -> createTimeTowerItem((SkyBlockPlayer) c.player()), (click, c) -> {
            SkyBlockPlayer p = (SkyBlockPlayer) c.player();
            DatapointChocolateFactory.ChocolateFactoryData d = ChocolateFactoryHelper.getData(p);

            if (d.getPrestigeLevel() < 1) {
                p.sendMessage("<c>This requires Chocolate Factory II!");
                p.playSound(NOT_ENOUGH_CHOCOLATE_SOUND);
                return;
            }

            // Right-click to activate
            if (click.click() instanceof Click.Right && d.getTimeTowerLevel() > 0 && d.getTimeTowerCharges() > 0 && !d.isTimeTowerActive()) {
                if (ChocolateFactoryHelper.activateTimeTower(p)) {
                    p.sendMessage("<a>Time Tower activated for 1 hour!");
                    p.playSound(Sound.sound(Key.key("block.beacon.activate"), Sound.Source.PLAYER, 1.0f, 1.0f));
                    c.session(State.class).refresh();
                    return;
                }
            }

            // Left-click to upgrade
            if (d.getTimeTowerLevel() >= TIME_TOWER_MAX_LEVEL) {
                p.sendMessage("<c>The Time Tower is already at its maximum level!");
                p.playSound(NOT_ENOUGH_CHOCOLATE_SOUND);
            } else if (ChocolateFactoryHelper.purchaseUpgrade(p, ChocolateFactoryHelper.UpgradeType.TIME_TOWER)) {
                p.sendMessage("<7>You upgraded to <d>Time Tower {:roman}<7>!", d.getTimeTowerLevel() + 1);
                p.playSound(UPGRADE_SOUND);
            } else {
                sendNotEnoughChocolateFeedback(p);
            }
            c.session(State.class).refresh();
        });

        // Slot 41: Rabbit Shrine
        layout.slot(41, (s, c) -> createRabbitShrineItem((SkyBlockPlayer) c.player()), (click, c) -> {
            SkyBlockPlayer p = (SkyBlockPlayer) c.player();
            DatapointChocolateFactory.ChocolateFactoryData d = ChocolateFactoryHelper.getData(p);

            if (d.getPrestigeLevel() < 2) {
                p.sendMessage("<c>This requires Chocolate Factory III!");
                p.playSound(NOT_ENOUGH_CHOCOLATE_SOUND);
                return;
            }

            if (d.getRabbitShrineLevel() >= RABBIT_SHRINE_MAX_LEVEL) {
                p.sendMessage("<c>Your Rabbit Shrine is already at its maximum level!");
                p.playSound(NOT_ENOUGH_CHOCOLATE_SOUND);
                return;
            }

            if (ChocolateFactoryHelper.purchaseUpgrade(p, ChocolateFactoryHelper.UpgradeType.RABBIT_SHRINE)) {
                p.sendMessage("<a>Upgraded Rabbit Shrine!");
                p.playSound(UPGRADE_SOUND);
            } else {
                sendNotEnoughChocolateFeedback(p);
            }
            c.session(State.class).refresh();
        });

        // Slot 42: Coach Jackrabbit
        layout.slot(42, (s, c) -> createCoachJackrabbitItem((SkyBlockPlayer) c.player()), (click, c) -> {
            SkyBlockPlayer p = (SkyBlockPlayer) c.player();
            DatapointChocolateFactory.ChocolateFactoryData d = ChocolateFactoryHelper.getData(p);

            if (d.getPrestigeLevel() < 3) {
                p.sendMessage("<c>This requires Chocolate Factory IV!");
                p.playSound(NOT_ENOUGH_CHOCOLATE_SOUND);
                return;
            }

            if (d.getCoachJackrabbitLevel() >= COACH_JACKRABBIT_MAX_LEVEL) {
                p.sendMessage("<c>Coach Jackrabbit has already taught you all that he can teach!");
                p.playSound(NOT_ENOUGH_CHOCOLATE_SOUND);
                return;
            }

            if (ChocolateFactoryHelper.purchaseUpgrade(p, ChocolateFactoryHelper.UpgradeType.COACH_JACKRABBIT)) {
                p.sendMessage("<a>Upgraded Coach Jackrabbit!");
                p.playSound(UPGRADE_SOUND);
            } else {
                sendNotEnoughChocolateFeedback(p);
            }
            c.session(State.class).refresh();
        });

        // Slot 45: Chocolate Production info
        layout.slot(45, (s, c) -> createProductionInfoItem((SkyBlockPlayer) c.player()));

        // Slot 49: Close
        Components.close(layout, 49);

        // Slot 50: Hoppity's Collection
        layout.slot(50, (s, c) -> {
            SkyBlockPlayer p = (SkyBlockPlayer) c.player();
            DatapointChocolateFactory.ChocolateFactoryData d = ChocolateFactoryHelper.getData(p);

            int rabbitsFound = d.getFoundRabbitCount();
            double percentage = (rabbitsFound / (double) TOTAL_RABBITS) * 100;

            return ItemStacks.head(HOPPITY_TEXTURE, """
                    <a>Hoppity's Collection
                    <7>Help <a>Hoppity <7>find all of his <a>Chocolate
                    <a>Rabbits <7>during the <d>Hoppity's Hunt
                    <7>event!

                    <7>The more unique <a>Chocolate Rabbits
                    <7>that you find, the more your
                    <6>Chocolate Factory <7>will produce!

                    <7>Rabbits Found: <e>{}<6>%
                    <2><l><m>    <f>                     <r> <e>{}<6>/<e>{}
</m>
                    <e>Click to view!""",
                    String.format("%.1f", percentage), rabbitsFound, TOTAL_RABBITS);
        }, (click, c) -> ((SkyBlockPlayer) c.player()).openView(new GUIHoppityCollection()));

        // Slot 51: Rabbit Hitman
        layout.slot(51, (s, c) -> ItemStacks.item(Material.BOW, """
                <c>Rabbit Hitman
                <7>Hire this private rabbit to hunt eggs
                <7>for you, they will collect eggs you
                <7>missed!

                <7>Available eggs: <a>0
                <7>Purchased slots: <e>0<7>/<a>28

                <e>Click to view!"""),
                (click, c) -> c.player().sendMessage("<7>Opening Rabbit Hitman... (Coming Soon)"));

        // Slot 52: Chocolate Factory Ranking
        layout.slot(52, (s, c) -> ItemStacks.item(Material.MILK_BUCKET, """
                <d>Chocolate Factory Ranking
                <7>You are <8>#<b>??? <7>in all-time
                <7>Chocolate.
                <8>You are in the top <e>??%<8> of players!"""));

        // Slot 53: Chocolate Factory Milestones
        layout.slot(53, (s, c) -> ItemStacks.item(Material.LADDER, """
                <6>Chocolate Factory Milestones
                <7>Unlock special <a>Chocolate Rabbits <7>by
                <7>reaching all-time <6>Chocolate
                <7>milestones!

                <e>Click to view!"""),
                (click, c) -> ((SkyBlockPlayer) c.player()).openView(new GUIChocolateFactoryMilestones()));
    }

    private void setupEmployeeSlots(ViewLayout<State> layout) {
        for (int i = 0; i < EMPLOYEE_SLOTS.length; i++) {
            int slot = EMPLOYEE_SLOTS[i];
            String employeeName = EMPLOYEE_NAMES[i];
            String employeeTexture = EMPLOYEE_TEXTURES[i];
            String employeeSubtitle = EMPLOYEE_SUBTITLES[i];

            layout.slot(slot, (s, c) -> createEmployeeItem((SkyBlockPlayer) c.player(), employeeName, employeeTexture, employeeSubtitle),
                    (click, c) -> {
                        handleEmployeeClick((SkyBlockPlayer) c.player(), employeeName);
                        c.session(State.class).refresh();
                    });
        }
    }

    private void handleEmployeeClick(SkyBlockPlayer player, String employeeName) {
        DatapointChocolateFactory.ChocolateFactoryData data = ChocolateFactoryHelper.getData(player);
        DatapointChocolateFactory.EmployeeType employeeType = DatapointChocolateFactory.EmployeeType.fromName(employeeName)
                .orElseThrow(() -> new IllegalArgumentException("Unknown chocolate factory employee: " + employeeName));

        if (!ChocolateFactoryHelper.isEmployeeUnlocked(player, employeeName)) {
            String prereq = ChocolateFactoryHelper.getEmployeePrerequisite(employeeName);
            player.sendMessage("<c>Promote <f>{} <c>to <7>[20] <f>Employee <c>to unlock <f>{}<c>!", prereq, employeeName);
            player.playSound(NOT_ENOUGH_CHOCOLATE_SOUND);
            return;
        }

        DatapointChocolateFactory.EmployeeData existingEmployee = data.getEmployees().get(employeeType);

        if (existingEmployee != null && existingEmployee.getLevel() >= EMPLOYEE_MAX_LEVEL) {
            player.sendMessage("<b>{} <c>cannot ascend the corporate ladder any further!", employeeName);
            player.playSound(NOT_ENOUGH_CHOCOLATE_SOUND);
            return;
        }

        if (existingEmployee == null) {
            long cost = ChocolateFactoryHelper.getEmployeeCost(employeeName, 1);
            if (data.getChocolate() < cost) {
                player.sendMessage("<c>{} does not work at your <6>Chocolate Factory <c>yet!", employeeName);
                player.playSound(NOT_ENOUGH_CHOCOLATE_SOUND);
                return;
            }
        }

        if (ChocolateFactoryHelper.hireOrUpgradeEmployee(player, employeeName)) {
            DatapointChocolateFactory.ChocolateFactoryData newData = ChocolateFactoryHelper.getData(player);
            DatapointChocolateFactory.EmployeeData emp = newData.getEmployees().get(employeeType);
            int level = emp != null ? emp.getLevel() : 1;
            String rank = getEmployeeRank(level);
            String rankColor = getEmployeeRankColor(level);

            player.sendMessage("<color:{0}>{1} <7>has been promoted to <7>[{2}] <color:{0}>{3}<7>!",
                    rankColor, employeeName, level, rank);
            player.playSound(UPGRADE_SOUND);
        } else {
            sendNotEnoughChocolateFeedback(player);
        }
    }

    private ItemStack.Builder createEmployeeItem(SkyBlockPlayer player, String employeeName, String texture, String subtitle) {
        DatapointChocolateFactory.ChocolateFactoryData data = ChocolateFactoryHelper.getData(player);
        DatapointChocolateFactory.EmployeeType employeeType = DatapointChocolateFactory.EmployeeType.fromName(employeeName)
                .orElseThrow(() -> new IllegalArgumentException("Unknown chocolate factory employee: " + employeeName));
        boolean isUnlocked = ChocolateFactoryHelper.isEmployeeUnlocked(player, employeeName);
        String prerequisite = ChocolateFactoryHelper.getEmployeePrerequisite(employeeName);

        if (!isUnlocked) {
            return ItemStacks.item(Material.GRAY_DYE, 1, Text.of("<c>{}", employeeName), List.of(
                    Text.of("<8><o>{}", subtitle),
                    Text.empty(),
                    Text.of("<7>Promote <f>{} <7>to <7>[20]", prerequisite),
                    Text.of("<f>Employee <7>to unlock!")));
        }

        DatapointChocolateFactory.EmployeeData employee = data.getEmployees().get(employeeType);
        double baseProduction = ChocolateFactoryHelper.getEmployeeBaseProduction(employeeName);

        if (employee == null) {
            long cost = ChocolateFactoryHelper.getEmployeeCost(player, employeeName, 1);
            boolean canAfford = data.getChocolate() >= cost;

            return ItemStacks.head(texture, 1, Text.of("<c>{}", employeeName), List.of(
                    Text.of("<8><o>{}", subtitle),
                    Text.empty(),
                    Text.of("<c>{} <7>does not work at", employeeName),
                    Text.of("<7>your <6>Chocolate Factory <7>yet!"),
                    Text.empty(),
                    Text.of("<7>Hire them and they will produce"),
                    Text.of("<6>+{} Chocolate <7>per second!", String.format("%.0f", baseProduction)),
                    Text.empty(),
                    Text.of(canAfford ? "<7>Cost: <6>{} Chocolate" : "<7>Cost: <c>{} Chocolate",
                            ChocolateFactoryHelper.formatChocolate(cost)),
                    Text.empty(),
                    canAfford ? Text.of("<e>Click to hire!") : NOT_ENOUGH_CHOCOLATE_MESSAGE));
        }

        int level = employee.getLevel();
        long cost = ChocolateFactoryHelper.getEmployeeCost(player, employeeName, level + 1);
        boolean canAfford = data.getChocolate() >= cost;
        double currentProduction = baseProduction * level;
        String rank = getEmployeeRank(level);
        String rankColor = getEmployeeRankColor(level);
        boolean isUnemployed = level < 1;

        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<8><o>{}", subtitle));
        lore.add(Text.empty());
        lore.add(Text.of("<color:{0}>{1} <7>is {2}<color:{3}>{4}<7>. They",
                rankColor, employeeName, isUnemployed ? "" : "a ", rankColor, rank));
        lore.add(Text.of(rank.equals("Board Member")
                ? "<7>are on the Board of Rabbits and" : "<7>are working hard and"));
        lore.add(Text.of("<7>produce <6>+{} Chocolate <7>per second!", String.format("%.0f", currentProduction)));
        lore.add(Text.empty());

        if (level >= EMPLOYEE_MAX_LEVEL) {
            lore.add(Text.of("<color:{}>{} <a>has climbed as far as the", rankColor, employeeName));
            lore.add(Text.of("<a>corporate ladder will allow!"));
        } else {
            lore.add(UPGRADE_DIVIDER);
            lore.add(Text.of("<a><l>{}</l> <8>➜ <7>[{}] <color:{}>{}",
                    isUnemployed ? "HIRE" : "PROMOTE", level + 1,
                    getEmployeeRankColor(level + 1), getEmployeeRank(level + 1)));
            lore.add(Text.of("  <6>+{} Chocolate per second", String.format("%.0f", baseProduction)));
            lore.add(Text.empty());
            lore.add(COST_LABEL);
            lore.add(Text.of("<6>{} Chocolate", ChocolateFactoryHelper.formatChocolate(cost)));
            lore.add(Text.empty());
            lore.add(canAfford
                    ? Text.of(isUnemployed ? "<e>Click to hire!" : "<e>Click to promote!")
                    : NOT_ENOUGH_CHOCOLATE_MESSAGE);
        }

        Text title = isUnemployed
                ? Text.of("<color:{0}>{1}<8> - <color:{0}>{2}", rankColor, employeeName, rank)
                : Text.of("<color:{0}>{1}<8> - <7>[{2}] <color:{0}>{3}", rankColor, employeeName, level, rank);
        return ItemStacks.head(texture, 1, title, lore);
    }

    private ItemStack.Builder createRabbitBarnItem(SkyBlockPlayer player) {
        DatapointChocolateFactory.ChocolateFactoryData data = ChocolateFactoryHelper.getData(player);
        int level = data.getRabbitBarnLevel();
        long cost = ChocolateFactoryHelper.getRabbitBarnCost(level);
        boolean canAfford = data.getChocolate() >= cost;

        List<Text> lore = new ArrayList<>(List.of(
                Text.of("<7>Your <a>Rabbit Barn <7>can only hold so"),
                Text.of("<7>many <a>Chocolate Rabbits<7>."),
                Text.empty(),
                Text.of("<7>If you try collecting more unique"),
                Text.of("<7>rabbits than your barn can hold,"),
                Text.of("<7>they will be <c>crushed<7>."),
                Text.empty(),
                Text.of("<7>Your Barn: <a>{}<7>/<a>{} Rabbits", data.getEmployeeCount(),
                        data.getMaxRabbitSlots() + RABBIT_BARN_EXTRA_CAPACITY)));
        if (level >= RABBIT_BARN_MAX_LEVEL) {
            lore.add(Text.empty());
            lore.add(Text.of("<a>Your Rabbit Barn is at maximum capacity!"));
        } else {
            lore.add(UPGRADE_DIVIDER);
            lore.add(Text.of("<a><l>UPGRADE</l> <8>➜ <a>Rabbit Barn {:roman}", level + 2));
            lore.add(Text.of("  <a>+2 Capacity"));
            lore.add(Text.empty());
            lore.add(COST_LABEL);
            lore.add(Text.of("<6>{} Chocolate", ChocolateFactoryHelper.formatChocolate(cost)));
            lore.add(Text.empty());
            lore.add(canAfford ? CLICK_TO_UPGRADE : NOT_ENOUGH_CHOCOLATE_SHORT);
        }

        return ItemStacks.item(Material.OAK_FENCE, 1, Text.of("<a>Rabbit Barn {:roman}", level + 1), lore);
    }

    private ItemStack.Builder createHandBakedChocolateItem(SkyBlockPlayer player) {
        DatapointChocolateFactory.ChocolateFactoryData data = ChocolateFactoryHelper.getData(player);
        int level = data.getHandBakedChocolateLevel();
        long cost = ChocolateFactoryHelper.getHandBakedChocolateCost(level);
        boolean canAfford = data.getChocolate() >= cost;

        List<Text> lore = new ArrayList<>(List.of(
                Text.of("<7>A good boss can get down in the"),
                Text.of("<7>trenches and help out their"),
                Text.of("<7>workforce. In exchange for some"),
                Text.of("<6>Chocolate<7>, you can increase the"),
                Text.of("<7>amount of <6>Chocolate <7>that you"),
                Text.of("<7>produce each time you click!"),
                Text.empty(),
                Text.of("<7>Chocolate Per Click: <6>+{} Chocolate", data.getClickPower()),
                Text.empty()));
        if (level >= HAND_BAKED_MAX_LEVEL) {
            lore.add(Text.of("<a>You have reached the maximum"));
            lore.add(Text.of("<a>amount of upgrades!"));
        } else {
            lore.add(UPGRADE_DIVIDER);
            lore.add(Text.of("<a><l>UPGRADE</l> <8>➜ <d>Hand-Baked Chocolate {:roman}", level + 2));
            lore.add(Text.of("  <6>+1 Chocolate Per Click"));
            lore.add(Text.empty());
            lore.add(COST_LABEL);
            lore.add(Text.of("<6>{} Chocolate", ChocolateFactoryHelper.formatChocolate(cost)));
            lore.add(Text.empty());
            lore.add(canAfford ? CLICK_TO_UPGRADE : NOT_ENOUGH_CHOCOLATE_SHORT);
        }

        return ItemStacks.item(Material.COOKIE, 1,
                Text.of("<d>Hand-Baked Chocolate {:roman}", level + 1), lore);
    }

    private ItemStack.Builder createTimeTowerItem(SkyBlockPlayer player) {
        DatapointChocolateFactory.ChocolateFactoryData data = ChocolateFactoryHelper.getData(player);

        if (data.getPrestigeLevel() < 1) {
            return lockedUpgradeItem("<c>Chocolate Factory II");
        }

        int level = data.getTimeTowerLevel();
        long cost = ChocolateFactoryHelper.getTimeTowerCost(level, data.getPrestigeLevel());
        boolean canAfford = data.getChocolate() >= cost;
        boolean isActive = data.isTimeTowerActive();

        List<Text> lore = new ArrayList<>(List.of(
                Text.of("<7>When active, this ancient building"),
                Text.of("<7>increases the production of your"),
                Text.of("<6>Chocolate Factory <7>by <6>+{} <7>for <a>1h<7>.",
                        String.format("%.1fx", (level > 0 ? 0.1 * level : 0.1))),
                Text.empty()));
        if (isActive) {
            long remaining = data.getTimeTowerActiveUntil() - System.currentTimeMillis();
            long minutes = remaining / MILLIS_PER_MINUTE;
            long seconds = (remaining % MILLIS_PER_MINUTE) / MILLIS_PER_SECOND;
            lore.add(Text.of("<7>Status: <a><l>ACTIVE"));
            lore.add(Text.of("<7>Time Remaining: <a>{}m {}s", minutes, seconds));
        } else {
            lore.add(Text.of("<7>Status: <c><l>INACTIVE"));
        }
        lore.add(Text.empty());
        lore.add(Text.of("<7>Charges: <a>{}<7>/<a>3", data.getTimeTowerCharges()));
        lore.add(Text.empty());
        if (level >= TIME_TOWER_MAX_LEVEL) {
            lore.add(Text.of("<a>The Time Tower is maxed out!"));
        } else {
            lore.add(UPGRADE_DIVIDER);
            lore.add(Text.of("<a><l>UPGRADE</l> <8>➜ <d>Time Tower {:roman}", level + 2));
            lore.add(Text.of("  <6>+0.1x Production Multiplier"));
            lore.add(Text.empty());
            lore.add(COST_LABEL);
            lore.add(Text.of("<6>{} Chocolate", ChocolateFactoryHelper.formatChocolate(cost)));
            lore.add(Text.empty());
            lore.add(canAfford ? CLICK_TO_UPGRADE : NOT_ENOUGH_CHOCOLATE_SHORT);
        }
        if (data.getTimeTowerCharges() > 0 && !isActive) {
            lore.add(Text.of("<d>Right-click to activate!"));
        }

        return ItemStacks.item(Material.CLOCK, 1, Text.of("<d>Time Tower {:roman}", level + 1), lore);
    }

    private ItemStack.Builder createRabbitShrineItem(SkyBlockPlayer player) {
        DatapointChocolateFactory.ChocolateFactoryData data = ChocolateFactoryHelper.getData(player);

        if (data.getPrestigeLevel() < 2) {
            return lockedUpgradeItem("<c>Chocolate Factory III");
        }

        int level = data.getRabbitShrineLevel();
        long cost = ChocolateFactoryHelper.getRabbitShrineCost(level);
        boolean canAfford = data.getChocolate() >= cost;
        int oddsBonus = level * 2;

        List<Text> lore = new ArrayList<>(List.of(
                Text.of("<7>The <d>Rabbit Shrine <7>increases the"),
                Text.of("<d>odds <7>of finding <a>Chocolate Rabbits <7>of"),
                Text.of("<7>higher rarity during <d>Hoppity's Hunt"),
                Text.of("<7>by <a>{}%<7>.", oddsBonus),
                Text.empty()));
        if (level >= RABBIT_SHRINE_MAX_LEVEL) {
            lore.add(Text.of("<a>Your Rabbit Shrine is at its maximum"));
            lore.add(Text.of("<a>level!"));
        } else {
            lore.add(UPGRADE_DIVIDER);
            lore.add(Text.of("<a><l>UPGRADE</l> <8>➜ <d>Rabbit Shrine {:roman}", level + 2));
            lore.add(Text.of("  <a>+2% Rare Rabbit Odds"));
            lore.add(Text.empty());
            lore.add(COST_LABEL);
            lore.add(Text.of("<6>{} Chocolate", ChocolateFactoryHelper.formatChocolate(cost)));
            lore.add(Text.empty());
            lore.add(canAfford ? CLICK_TO_UPGRADE : NOT_ENOUGH_CHOCOLATE_SHORT);
        }

        return ItemStacks.item(Material.RABBIT_FOOT, 1, Text.of("<d>Rabbit Shrine {:roman}", level + 1), lore);
    }

    private ItemStack.Builder createCoachJackrabbitItem(SkyBlockPlayer player) {
        DatapointChocolateFactory.ChocolateFactoryData data = ChocolateFactoryHelper.getData(player);

        if (data.getPrestigeLevel() < 3) {
            return lockedUpgradeItem("<c>Chocolate Factory IV");
        }

        int level = data.getCoachJackrabbitLevel();
        long cost = ChocolateFactoryHelper.getCoachJackrabbitCost(level);
        boolean canAfford = data.getChocolate() >= cost;
        double multiplierBonus = level * 0.01;

        List<Text> lore = new ArrayList<>(List.of(
                Text.of("<8><o>Pep talk pro!"),
                Text.empty(),
                Text.of("<d>Coach Jackrabbit <7>is a motivational"),
                Text.of("<7>speaker that is helping you reach"),
                Text.of("<7>your full potential by granting <6>+{}",
                        String.format("%.2fx", multiplierBonus)),
                Text.of("<6>Chocolate <7>per second!"),
                Text.empty()));
        if (level >= COACH_JACKRABBIT_MAX_LEVEL) {
            lore.add(Text.of("<a>Coach Jackrabbit has already taught"));
            lore.add(Text.of("<a>you all that he can teach!"));
        } else {
            lore.add(UPGRADE_DIVIDER);
            lore.add(Text.of("<a><l>UPGRADE</l> <8>➜ <d>Coach Jackrabbit {:roman}", level + 2));
            lore.add(Text.of("  <6>+0.01x Production Multiplier"));
            lore.add(Text.empty());
            lore.add(COST_LABEL);
            lore.add(Text.of("<6>{} Chocolate", ChocolateFactoryHelper.formatChocolate(cost)));
            lore.add(Text.empty());
            lore.add(canAfford ? CLICK_TO_UPGRADE : NOT_ENOUGH_CHOCOLATE_SHORT);
        }

        return ItemStacks.head(COACH_JACKRABBIT_TEXTURE, 1,
                Text.of("<d>Coach Jackrabbit {:roman}", level + 1), lore);
    }

    private ItemStack.Builder lockedUpgradeItem(String requirement) {
        return ItemStacks.item(Material.GRAY_DYE, 1, Text.of("<c>???"), List.of(
                Text.of("<7>What does it do? Nobody knows..."),
                Text.empty(),
                Text.of(requirement)));
    }

    private ItemStack.Builder createProductionInfoItem(SkyBlockPlayer player) {
        DatapointChocolateFactory.ChocolateFactoryData data = ChocolateFactoryHelper.getData(player);

        double employeeProduction = 0;
        for (DatapointChocolateFactory.EmployeeData employee : data.getEmployees().values()) {
            employeeProduction += employee.getProductionPerSecond();
        }

        // Calculate bonuses from Hoppity's Collection
        int rabbitChocolateBonus = 0;
        double rabbitMultiplierBonus = 0;
        for (ChocolateRabbit rabbit : data.getFoundRabbits()) {
            rabbitChocolateBonus += rabbit.getChocolateBonus();
            rabbitMultiplierBonus += rabbit.getMultiplierBonus();
        }

        double baseProduction = employeeProduction + rabbitChocolateBonus;
        double totalMultiplier = data.getShrineMultiplier() * data.getTimeTowerMultiplier() * data.getCoachMultiplier() + rabbitMultiplierBonus;

        List<Text> lore = new ArrayList<>(List.of(
                Text.of("<6>{} Chocolate <8>per second", String.format("%.2f", data.getChocolatePerSecond())),
                Text.empty(),
                Text.of("<7>Base Chocolate: <6>{} <8>per second", String.format("%.0f", baseProduction)),
                Text.of("  <6>+{} <8>(Rabbit Employees<8>)", String.format("%.0f", employeeProduction))));
        if (rabbitChocolateBonus > 0) {
            lore.add(Text.of("  <6>+{} <8>(Hoppity's Collection<8>)", rabbitChocolateBonus));
        }
        lore.add(Text.empty());
        lore.add(Text.of("<7>Total Multiplier: <6>{}", String.format("%.3fx", totalMultiplier)));
        lore.add(Text.of("  <6>+1x <8>(Base Multiplier)"));
        if (rabbitMultiplierBonus > 0) {
            lore.add(Text.of("  <6>+{} <8>(Hoppity's Collection<8>)", String.format("%.3fx", rabbitMultiplierBonus)));
        }
        if (data.getRabbitShrineLevel() > 0) {
            lore.add(Text.of("  <6>+{} <8>(Rabbit Shrine)", String.format("%.1fx", data.getShrineMultiplier() - 1.0)));
        }
        if (data.getCoachJackrabbitLevel() > 0) {
            lore.add(Text.of("  <6>+{} <8>(Coach Jackrabbit)", String.format("%.2fx", data.getCoachMultiplier() - 1.0)));
        }
        if (data.isTimeTowerActive()) {
            lore.add(Text.of("  <6>+{} <8>(Time Tower)", String.format("%.1fx", data.getTimeTowerMultiplier() - 1.0)));
        }

        return ItemStacks.item(Material.COCOA_BEANS, 1, Text.of("<6>Chocolate Production"), lore);
    }

    private String getEmployeeRank(int level) {
        if (level >= EMPLOYEE_MAX_LEVEL) return "Board Member";
        if (level >= 200) return "Executive";
        if (level >= 180) return "Director";
        if (level >= 140) return "Manager";
        if (level >= 120) return "Assistant";
        if (level >= 20) return "Employee";
        if (level >= 1) return "Intern";
        return "Unemployed";
    }

    private String getEmployeeRankColor(int level) {
        if (level >= EMPLOYEE_MAX_LEVEL) return "b";
        if (level >= 200) return "d";
        if (level >= 180) return "6";
        if (level >= 140) return "5";
        if (level >= 120) return "9";
        if (level >= 20) return "a";
        if (level >= 1) return "f";
        return "c";
    }

    private void sendNotEnoughChocolateFeedback(SkyBlockPlayer player) {
        player.sendMessage(NOT_ENOUGH_CHOCOLATE_MESSAGE);
        player.playSound(NOT_ENOUGH_CHOCOLATE_SOUND);
    }

}
