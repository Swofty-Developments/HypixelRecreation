package net.swofty.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentGroup;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.utils.location.RelativeVec;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.item.components.RegionSelectorComponent;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "regions",
    description = "Handles regions across the server",
        usage = "/regions <text>",
    permission = Rank.STAFF,
    allowsConsole = false)
public class RegionCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentGroup removeRegion = ArgumentType.Group("remove",
            ArgumentType.Literal("remove"),
            ArgumentType.String("region_id"));
        ArgumentGroup addRegion = ArgumentType.Group("add",
            ArgumentType.Literal("add"),
            ArgumentType.String("region_id"),
            ArgumentType.Enum("region_type", RegionType.class),
            ArgumentType.RelativeBlockPosition("pos1"),
            ArgumentType.RelativeBlockPosition("pos2"));
        ArgumentGroup wandRegion = ArgumentType.Group("wand",
            ArgumentType.Literal("wand"),
            ArgumentType.String("region_id"),
            ArgumentType.Enum("region_type", RegionType.class));

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            String regionId = context.get(removeRegion).get("region_id");
            SkyBlockRegion region = SkyBlockRegion.getFromID(regionId);

            if (region == null) {
                sender.sendMessage(Text.of("<c>Unable to find a region by the ID <e>{}<c>.", regionId));
                return;
            }

            region.delete();
            sender.sendMessage(Text.of("<a>Successfully deleted region <e>{}<a>.", regionId));
        }, removeRegion);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            String regionId = context.get(addRegion).get("region_id");
            RelativeVec position1 = context.get(addRegion).get("pos1");
            RelativeVec position2 = context.get(addRegion).get("pos2");
            Vec vectorPosition1 = position1.from((SkyBlockPlayer) sender);
            Vec vectorPosition2 = position2.from((SkyBlockPlayer) sender);
            RegionType regionType = context.get(addRegion).get("region_type");

            sender.sendMessage(Text.of("<a>Successfully created region <e>{}<a> with type <e>{}<a>.", regionId, regionType.name()));
            sender.sendMessage(Text.of("<a>Position 1: <e>{}, {}, {}", vectorPosition1.x(), vectorPosition1.y(), vectorPosition1.z()));
            sender.sendMessage(Text.of("<a>Position 2: <e>{}, {}, {}", vectorPosition2.x(), vectorPosition2.y(), vectorPosition2.z()));

            new SkyBlockRegion(regionId,
                new Pos(vectorPosition1.x(), vectorPosition1.y(), vectorPosition1.z()),
                new Pos(vectorPosition2.x(), vectorPosition2.y(), vectorPosition2.z()),
                regionType,
                HypixelConst.getTypeLoader().getType()).save();
        }, addRegion);

        command.addSyntax((sender, context) -> {
            RegionSelectorComponent.SelectedRegion region = RegionSelectorComponent.getPlayerRegionSelection().get((SkyBlockPlayer) sender);
            if (region == null || region.getPos1() == null || region.getPos2() == null) {
                sender.sendMessage("<c>You must select a region first using the region selector item.");
                return;
            }

            String regionId = context.get(wandRegion).get("region_id");
            RegionType regionType = context.get(wandRegion).get("region_type");

            sender.sendMessage(Text.of("<a>Successfully created region <e>{}<a> with type <e>{}<a>.", regionId, regionType.name()));
            sender.sendMessage(Text.of("<a>Position 1: <e>{}, {}, {}", region.getPos1().x(), region.getPos1().y(), region.getPos1().z()));
            sender.sendMessage(Text.of("<a>Position 2: <e>{}, {}, {}", region.getPos2().x(), region.getPos2().y(), region.getPos2().z()));

            new SkyBlockRegion(regionId,
                new Pos(region.getPos1().x(), region.getPos1().y(), region.getPos1().z()),
                new Pos(region.getPos2().x(), region.getPos2().y(), region.getPos2().z()),
                regionType,
                HypixelConst.getTypeLoader().getType()).save();
        }, wandRegion);
    }
}
