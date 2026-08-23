package net.swofty.type.skywarslobby.gui;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.generic.data.datapoints.DatapointLong;
import net.swofty.type.generic.data.datapoints.DatapointSkywarsUnlocks;
import net.swofty.type.generic.data.handlers.SkywarsDataHandler;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.*;
import net.swofty.type.generic.gui.v2.context.ClickContext;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skywarslobby.kit.SkywarsKit;
import net.swofty.type.skywarslobby.kit.SkywarsKitRegistry;
import net.swofty.type.skywarslobby.perk.SkywarsPerk;
import net.swofty.type.skywarslobby.perk.SkywarsPerkRegistry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class GUISoulWellRolling implements StatefulView<GUISoulWellRolling.State> {
    private static final Random RANDOM = new Random();
    private static final Material[] GLASS_COLORS = {Material.PINK_STAINED_GLASS_PANE, Material.LIME_STAINED_GLASS_PANE, Material.LIGHT_BLUE_STAINED_GLASS_PANE, Material.ORANGE_STAINED_GLASS_PANE, Material.YELLOW_STAINED_GLASS_PANE, Material.MAGENTA_STAINED_GLASS_PANE};

    private static final int TOTAL_TICKS = 40;
    private static final int CLOSE_DELAY_MILLIS = 15000;

    private final int wheelCount;
    private final List<SoulWellReward> finalRewards;
    private final List<List<SoulWellReward>> wheelItems;
    private final int[][] wheelSlots;

    public GUISoulWellRolling(int wheelCount) {
        this.wheelCount = Math.min(5, Math.max(1, wheelCount));
        this.finalRewards = new ArrayList<>();
        this.wheelItems = new ArrayList<>();

        for (int i = 0; i < this.wheelCount; i++) {
            finalRewards.add(generateRandomReward());
            wheelItems.add(generateWheelItems());
        }

        this.wheelSlots = calculateWheelSlots(this.wheelCount);
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return new ViewConfiguration<>("Soul Well", InventoryType.CHEST_5_ROW);
    }

    @Override
    public void onOpen(State state, ViewContext ctx) {
        MinecraftServer.getSchedulerManager().scheduleNextTick(() -> startRolling(ctx));
    }

    private void startRolling(ViewContext ctx) {
        ViewSession<State> session = ctx.session(State.class);

        final Task[] task = new Task[1];
        task[0] = MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (session.isClosed()) {
                task[0].cancel();
                return;
            }

            State s = session.state();
            int nextTick = s.tick + 1;

            // Check which wheels should stop this tick and give rewards
            Set<Integer> newlyStopped = new HashSet<>(s.stoppedWheels);
            for (int wheel = 0; wheel < wheelCount; wheel++) {
                int stopTick = getStopTick(wheel);
                if (nextTick >= stopTick && !s.stoppedWheels.contains(wheel)) {
                    newlyStopped.add(wheel);
                    giveReward(ctx, finalRewards.get(wheel));
                }
            }

            session.update(_ -> new State(nextTick, newlyStopped));
            if (newlyStopped.size() >= wheelCount) {
                task[0].cancel();
                MinecraftServer.getSchedulerManager().buildTask(() -> {
                    if (!session.isClosed()) ctx.player().closeInventory();
                }).delay(TaskSchedule.millis(CLOSE_DELAY_MILLIS)).schedule();
            }
        }).delay(TaskSchedule.millis(50)).repeat(TaskSchedule.millis(100)).schedule();

        session.onClose(_ -> task[0].cancel());
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        layout.allowHotkey(false);
        for (int slot = 0; slot < 45; slot++) {
            int finalSlot = slot;
            layout.autoUpdating(finalSlot, (s, _) -> renderSlot(finalSlot, s), Duration.ofMillis(100));
        }
    }

    private ItemStack.Builder renderSlot(int slot, State state) {
        for (int wheel = 0; wheel < wheelSlots.length; wheel++) {
            int[] slots = wheelSlots[wheel];

            // Check adjacent columns for black glass
            int wheelCol = slots[0] % 9;
            int slotCol = slot % 9;
            int slotRow = slot / 9;

            // Left side of wheel column's middle position
            if (slotCol == wheelCol - 1 && slotRow == 2) {
                return ItemStacks.item(Material.BLACK_STAINED_GLASS_PANE, 1, " ");
            }
            // Right side of wheel column's middle position
            if (slotCol == wheelCol + 1 && slotRow == 2) {
                return ItemStacks.item(Material.BLACK_STAINED_GLASS_PANE, 1, " ");
            }

            for (int i = 0; i < slots.length; i++) {
                if (slots[i] != slot) continue;

                boolean stopped = state.stoppedWheels.contains(wheel);

                if (stopped) {
                    if (i == 2) {
                        return finalRewards.get(wheel).toItemStack();
                    }
                    return ItemStacks.item(Material.AIR, 1, " ");
                }

                List<SoulWellReward> items = wheelItems.get(wheel);
                int itemIndex = (state.tick + i) % items.size();
                return items.get(itemIndex).toItemStack();
            }
        }

        boolean allStopped = state.stoppedWheels.size() >= wheelCount;
        if (allStopped) {
            return ItemStacks.item(Material.AIR, 1, " ");
        }
        return ItemStacks.item(GLASS_COLORS[RANDOM.nextInt(GLASS_COLORS.length)], 1, " ");
    }

    private int getStopTick(int wheel) {
        return TOTAL_TICKS - (wheelCount - wheel - 1) * 5;
    }

    private static int[][] calculateWheelSlots(int count) {
        int[][] slots = new int[count][5];
        int[] columns = switch (count) {
            case 1 -> new int[]{4};
            case 2 -> new int[]{3, 5};
            case 3 -> new int[]{2, 4, 6};
            case 4 -> new int[]{1, 3, 5, 7};
            default -> new int[]{0, 2, 4, 6, 8};
        };

        for (int wheel = 0; wheel < count; wheel++) {
            int col = columns[wheel];
            for (int row = 0; row < 5; row++) {
                slots[wheel][row] = row * 9 + col;
            }
        }
        return slots;
    }

    private void giveReward(ViewContext ctx, SoulWellReward reward) {
        SkywarsDataHandler handler = SkywarsDataHandler.getUser(ctx.player());
        if (handler == null) return;

        switch (reward.type) {
            case COINS_SMALL -> {
                int amount = 100 + RANDOM.nextInt(401);
                long coins = handler.get(SkywarsDataHandler.Data.COINS, DatapointLong.class).getValue();
                handler.get(SkywarsDataHandler.Data.COINS, DatapointLong.class).setValue(coins + amount);
                ctx.player().sendMessage("<6><l>SOUL WELL! </l><7>You received <6>{} Coins<7>!", amount);
            }
            case COINS_MEDIUM -> {
                int amount = 300 + RANDOM.nextInt(701);
                long coins = handler.get(SkywarsDataHandler.Data.COINS, DatapointLong.class).getValue();
                handler.get(SkywarsDataHandler.Data.COINS, DatapointLong.class).setValue(coins + amount);
                ctx.player().sendMessage("<9><l>SOUL WELL! </l><7>You received <9>{} Coins<7>!", amount);
            }
            case COINS_LARGE -> {
                int amount = 500 + RANDOM.nextInt(1501);
                long coins = handler.get(SkywarsDataHandler.Data.COINS, DatapointLong.class).getValue();
                handler.get(SkywarsDataHandler.Data.COINS, DatapointLong.class).setValue(coins + amount);
                ctx.player().sendMessage("<6><l>SOUL WELL! </l><7>You received <6>{} Coins<7>!", amount);
            }
            case KIT -> {
                if (reward.kit != null) {
                    var unlocks = handler.get(SkywarsDataHandler.Data.UNLOCKS, DatapointSkywarsUnlocks.class).getValue();
                    unlocks.unlockKit(reward.kit.getId());
                    ctx.player().sendMessage("<a><l>SOUL WELL! </l><7>You unlocked the <a>{} Kit<7>!", reward.kit.getName());
                }
            }
            case PERK -> {
                if (reward.perk != null) {
                    var unlocks = handler.get(SkywarsDataHandler.Data.UNLOCKS, DatapointSkywarsUnlocks.class).getValue();
                    unlocks.unlockPerk(reward.perk.getId());
                    ctx.player().sendMessage("<a><l>SOUL WELL! </l><7>You unlocked the <a>{} Perk<7>!", reward.perk.getName());
                }
            }
        }
    }

    private static SoulWellReward generateRandomReward() {
        int roll = RANDOM.nextInt(100);
        if (roll < 30) return new SoulWellReward(RewardType.COINS_SMALL, null, null, "Small bag of coins");
        if (roll < 50) return new SoulWellReward(RewardType.COINS_MEDIUM, null, null, "Medium bag of coins");
        if (roll < 60) return new SoulWellReward(RewardType.COINS_LARGE, null, null, "Large bag of coins");
        if (roll < 80) {
            List<SkywarsKit> kits = SkywarsKitRegistry.getSoulWellKits();
            if (!kits.isEmpty()) {
                SkywarsKit kit = kits.get(RANDOM.nextInt(kits.size()));
                return new SoulWellReward(RewardType.KIT, kit, null, kit.getName() + " Kit");
            }
        } else {
            List<SkywarsPerk> perks = SkywarsPerkRegistry.getSoulWellPerks();
            if (!perks.isEmpty()) {
                SkywarsPerk perk = perks.get(RANDOM.nextInt(perks.size()));
                return new SoulWellReward(RewardType.PERK, null, perk, perk.getName() + " Perk");
            }
        }
        return new SoulWellReward(RewardType.COINS_SMALL, null, null, "Small bag of coins");
    }

    private static List<SoulWellReward> generateWheelItems() {
        List<SoulWellReward> items = new ArrayList<>();
        for (int i = 0; i < 15; i++) items.add(generateRandomReward());
        return items;
    }

    @Override
    public boolean onClick(ClickContext<State> click, ViewContext ctx) {
        return true;
    }

    @Override
    public State initialState() {
        return new State();
    }

    public record State(int tick, Set<Integer> stoppedWheels) {
        public State() {
            this(0, new HashSet<>());
        }
    }

    public enum RewardType {
        COINS_SMALL, COINS_MEDIUM, COINS_LARGE, KIT, PERK
    }

    public record SoulWellReward(RewardType type, SkywarsKit kit, SkywarsPerk perk, String displayName) {
        public ItemStack.Builder toItemStack() {
            return switch (type) {
                case COINS_SMALL -> ItemStacks.item(Material.SUNFLOWER, 1, "<a>Small bag of coins");
                case COINS_MEDIUM -> ItemStacks.item(Material.SUNFLOWER, 1, "<9>Medium bag of coins");
                case COINS_LARGE -> ItemStacks.item(Material.SUNFLOWER, 1, "<6>Large bag of coins");
                case KIT -> {
                    if (kit != null) {
                        if (kit.hasCustomTexture()) {
                            yield ItemStacks.head(kit.getIconTexture(), "<a>{}", displayName);
                        }
                        yield ItemStacks.item(kit.getIconMaterial(), 1, "<a>{}", displayName);
                    }
                    yield ItemStacks.item(Material.LEATHER_HELMET, 1, "<a>{}", displayName);
                }
                case PERK -> {
                    if (perk != null) {
                        yield ItemStacks.item(perk.getIconMaterial(), 1, "<a>{}", displayName);
                    }
                    yield ItemStacks.item(Material.OAK_PLANKS, 1, "<a>{}", displayName);
                }
            };
        }
    }
}
