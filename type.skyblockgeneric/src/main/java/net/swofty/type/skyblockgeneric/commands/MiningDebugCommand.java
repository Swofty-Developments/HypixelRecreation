package net.swofty.type.skyblockgeneric.commands;

import net.swofty.commons.text.Text;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.item.SkyBlockItemComponent;
import net.swofty.type.skyblockgeneric.item.components.AxeComponent;
import net.swofty.type.skyblockgeneric.item.components.DrillComponent;
import net.swofty.type.skyblockgeneric.item.components.HoeComponent;
import net.swofty.type.skyblockgeneric.item.components.PickaxeComponent;
import net.swofty.type.skyblockgeneric.region.mining.MineableBlock;
import net.swofty.type.skyblockgeneric.region.mining.handler.SkyBlockMiningHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CommandParameters(labels = "mininginfo",
        description = "Debug command to display mining handler relationships",
        usage = "/miningdebug",
        permission = Rank.STAFF,
        allowsConsole = false)
public class MiningDebugCommand extends HypixelCommand {

    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            sender.sendMessage("<6><l>=== Mining Handler Debug ===");
            sender.sendMessage("");

            // Group blocks by handler type
            Map<String, List<MineableBlock>> blocksByHandler = new HashMap<>();

            for (MineableBlock block : MineableBlock.values()) {
                SkyBlockMiningHandler handler = block.getMiningHandler();
                String handlerName = handler.getHandlerName();
                blocksByHandler.computeIfAbsent(handlerName, k -> new ArrayList<>()).add(block);
            }

            // Print blocks grouped by handler
            sender.sendMessage("<e><l>Blocks by Handler Type:");
            for (Map.Entry<String, List<MineableBlock>> entry : blocksByHandler.entrySet()) {
                sender.sendMessage("");
                sender.sendMessage(Text.of("<b>{} Handler <7>({} blocks):", entry.getKey(), entry.getValue().size()));
                for (MineableBlock block : entry.getValue()) {
                    SkyBlockMiningHandler handler = block.getMiningHandler();
                    Text strengthInfo = handler.breaksInstantly()
                            ? Text.of("<a>Instant")
                            : Text.of("<7>Strength: {}", handler.getStrength());
                    Text powerInfo = handler.getMiningPowerRequirement() > 0
                            ? Text.of(" <7>Power: {}", handler.getMiningPowerRequirement())
                            : Text.empty();
                    sender.sendMessage(Text.of("  <7>- <f>{} <7>({}{}<7>)", block.name(), strengthInfo, powerInfo));
                }
            }

            sender.sendMessage("");
            sender.sendMessage("<e><l>Tool Component Mappings:");

            // Print tool -> blocks relationships
            printToolBlocks(sender, "Pickaxe/Drill", PickaxeComponent.class, DrillComponent.class);
            printToolBlocks(sender, "Axe", AxeComponent.class);
            printToolBlocks(sender, "Hoe", HoeComponent.class);

            sender.sendMessage("");
            sender.sendMessage("<6><l>=========================");
        });
    }

    @SafeVarargs
    private void printToolBlocks(Object sender, String toolName, Class<? extends SkyBlockItemComponent>... componentClasses) {
        List<String> breakableBlocks = new ArrayList<>();

        for (MineableBlock block : MineableBlock.values()) {
            SkyBlockMiningHandler handler = block.getMiningHandler();
            List<Class<? extends SkyBlockItemComponent>> validComponents = handler.getValidToolComponents();

            for (Class<? extends SkyBlockItemComponent> componentClass : componentClasses) {
                if (validComponents.contains(componentClass)) {
                    breakableBlocks.add(block.name());
                    break;
                }
            }
        }

        net.minestom.server.command.CommandSender commandSender = (net.minestom.server.command.CommandSender) sender;
        if (!breakableBlocks.isEmpty()) {
            commandSender.sendMessage(Text.of("<b>{} <7>can break {} blocks:", toolName, breakableBlocks.size()));
            commandSender.sendMessage(Text.of("  <7>{}", String.join(", ", breakableBlocks)));
        } else {
            commandSender.sendMessage(Text.of("<b>{} <7>has no breakable blocks configured", toolName));
        }
    }
}
