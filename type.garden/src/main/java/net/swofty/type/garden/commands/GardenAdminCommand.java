package net.swofty.type.garden.commands;

import net.minestom.server.command.builder.arguments.ArgumentGroup;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentDouble;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.command.builder.arguments.number.ArgumentLong;
import net.swofty.type.garden.GardenServices;
import net.swofty.type.garden.config.GardenConfigRegistry;
import net.swofty.type.garden.gui.GardenGuiSupport;
import net.swofty.type.garden.visitor.GardenBarnRuntime;
import net.swofty.type.garden.visitor.GardenVisitorRuntime;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.garden.GardenData;
import net.swofty.type.skyblockgeneric.garden.progression.GardenProgressionSupport;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Locale;

@CommandParameters(
    labels = "gardenadmin",
    description = "Mutates Garden data for testing",
    usage = "/gardenadmin <section> ...",
    permission = Rank.STAFF,
    allowsConsole = false
)
public class GardenAdminCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentInteger intAmount = ArgumentType.Integer("amount");
        ArgumentLong longAmount = ArgumentType.Long("amount");
        ArgumentDouble doubleAmount = ArgumentType.Double("amount");

        ArgumentGroup coreLevel = ArgumentType.Group("core_level", ArgumentType.Literal("core"), ArgumentType.Literal("level"), intAmount);
        command.addSyntax((sender, context) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            GardenGuiSupport.core(player).setLevel(Math.max(1, context.get(coreLevel).get("amount")));
            sender.sendMessage("§aGarden level set.");
        }, coreLevel);

        ArgumentGroup coreXp = ArgumentType.Group("core_xp", ArgumentType.Literal("core"), ArgumentType.Literal("xp"), longAmount);
        command.addSyntax((sender, context) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            GardenGuiSupport.core(player).setExperience(Math.max(0L, context.get(coreXp).get("amount")));
            sender.sendMessage("§aGarden XP set.");
        }, coreXp);

        ArgumentGroup coreCopper = ArgumentType.Group("core_copper", ArgumentType.Literal("core"), ArgumentType.Literal("copper"), longAmount);
        command.addSyntax((sender, context) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            GardenGuiSupport.core(player).setCopper(Math.max(0L, context.get(coreCopper).get("amount")));
            sender.sendMessage("§aCopper set.");
        }, coreCopper);

        ArgumentGroup coreBarnSkinUnlock = ArgumentType.Group("core_barnskin_unlock",
            ArgumentType.Literal("core"), ArgumentType.Literal("barnskin"), ArgumentType.Literal("unlock"), ArgumentType.String("skin_id"));
        command.addSyntax((sender, context) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            String skinId = normalize(context.get(coreBarnSkinUnlock).get("skin_id"));
            GardenGuiSupport.core(player).getOwnedBarnSkins().add(skinId);
            GardenBarnRuntime.requestImmediateSync(player);
            sender.sendMessage("§aUnlocked barn skin §e" + skinId + "§a.");
        }, coreBarnSkinUnlock);

        ArgumentGroup coreBarnSkinSelect = ArgumentType.Group("core_barnskin_select",
            ArgumentType.Literal("core"), ArgumentType.Literal("barnskin"), ArgumentType.Literal("select"), ArgumentType.String("skin_id"));
        command.addSyntax((sender, context) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            String skinId = normalize(context.get(coreBarnSkinSelect).get("skin_id"));
            GardenGuiSupport.core(player).setSelectedBarnSkin(skinId);
            GardenBarnRuntime.requestImmediateSync(player);
            sender.sendMessage("§aSelected barn skin §e" + skinId + "§a.");
        }, coreBarnSkinSelect);

        ArgumentGroup coreTime = ArgumentType.Group("core_time",
            ArgumentType.Literal("core"), ArgumentType.Literal("time"), ArgumentType.Word("mode"));
        command.addSyntax((sender, context) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            String mode = normalize(context.get(coreTime).get("mode"));
            if (!mode.equals("DYNAMIC") && !mode.equals("DAY") && !mode.equals("NIGHT")) {
                sender.sendMessage("§cTime mode must be DYNAMIC, DAY, or NIGHT.");
                return;
            }
            GardenGuiSupport.core(player).setSelectedTimeMode(mode);
            sender.sendMessage("§aGarden time mode set to §e" + mode + "§a.");
        }, coreTime);

        ArgumentGroup visitorsSpawn = ArgumentType.Group("visitors_spawn",
            ArgumentType.Literal("visitors"), ArgumentType.Literal("spawn"), ArgumentType.String("visitor_id"));
        command.addSyntax((sender, context) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            String visitorId = normalize(context.get(visitorsSpawn).get("visitor_id"));
            GardenData.GardenVisitorState state = GardenVisitorRuntime.createVisitorState(player, visitorId);
            if (state == null) {
                sender.sendMessage("§cUnknown visitor §e" + visitorId + "§c.");
                return;
            }
            GardenGuiSupport.visitors(player).getActiveVisitors().add(state);
            GardenBarnRuntime.requestImmediateSync(player);
            sender.sendMessage("§aSpawned visitor §e" + visitorId + "§a.");
        }, visitorsSpawn);

        ArgumentGroup visitorsQueue = ArgumentType.Group("visitors_queue",
            ArgumentType.Literal("visitors"), ArgumentType.Literal("queue"), ArgumentType.String("visitor_id"));
        command.addSyntax((sender, context) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            String visitorId = normalize(context.get(visitorsQueue).get("visitor_id"));
            GardenData.GardenVisitorState state = GardenVisitorRuntime.createVisitorState(player, visitorId);
            if (state == null) {
                sender.sendMessage("§cUnknown visitor §e" + visitorId + "§c.");
                return;
            }
            state.setQueued(true);
            GardenGuiSupport.visitors(player).getQueuedVisitors().add(state);
            GardenBarnRuntime.requestImmediateSync(player);
            sender.sendMessage("§aQueued visitor §e" + visitorId + "§a.");
        }, visitorsQueue);

        ArgumentGroup visitorsClear = ArgumentType.Group("visitors_clear", ArgumentType.Literal("visitors"), ArgumentType.Literal("clear"));
        command.addSyntax((sender, context) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            GardenGuiSupport.visitors(player).getActiveVisitors().clear();
            GardenGuiSupport.visitors(player).getQueuedVisitors().clear();
            GardenBarnRuntime.requestImmediateSync(player);
            sender.sendMessage("§aCleared active and queued visitors.");
        }, visitorsClear);

        ArgumentGroup visitorsAcceptCount = ArgumentType.Group("visitors_acceptcount",
            ArgumentType.Literal("visitors"), ArgumentType.Literal("acceptcount"), ArgumentType.String("visitor_id"), intAmount);
        command.addSyntax((sender, context) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            String visitorId = normalize(context.get(visitorsAcceptCount).get("visitor_id"));
            GardenGuiSupport.visitors(player).getServedCounts().put(visitorId, Math.max(0, context.get(visitorsAcceptCount).get("amount")));
            sender.sendMessage("§aUpdated accepted offer count for §e" + visitorId + "§a.");
        }, visitorsAcceptCount);

        ArgumentGroup visitorsVisitCount = ArgumentType.Group("visitors_visitcount",
            ArgumentType.Literal("visitors"), ArgumentType.Literal("visitcount"), ArgumentType.String("visitor_id"), intAmount);
        command.addSyntax((sender, context) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            String visitorId = normalize(context.get(visitorsVisitCount).get("visitor_id"));
            GardenGuiSupport.visitors(player).getVisitCounts().put(visitorId, Math.max(0, context.get(visitorsVisitCount).get("amount")));
            sender.sendMessage("§aUpdated visit count for §e" + visitorId + "§a.");
        }, visitorsVisitCount);

        ArgumentGroup visitorsServedUnique = ArgumentType.Group("visitors_servedunique",
            ArgumentType.Literal("visitors"), ArgumentType.Literal("servedunique"), ArgumentType.Word("mode"), ArgumentType.String("visitor_id"));
        command.addSyntax((sender, context) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            String mode = normalize(context.get(visitorsServedUnique).get("mode"));
            String visitorId = normalize(context.get(visitorsServedUnique).get("visitor_id"));
            if (mode.equals("ADD")) {
                GardenGuiSupport.core(player).getServedUniqueVisitors().add(visitorId);
            } else if (mode.equals("REMOVE")) {
                GardenGuiSupport.core(player).getServedUniqueVisitors().remove(visitorId);
            } else {
                sender.sendMessage("§cMode must be add or remove.");
                return;
            }
            sender.sendMessage("§aUpdated served-unique set for §e" + visitorId + "§a.");
        }, visitorsServedUnique);

        ArgumentGroup visitorsNextArrival = ArgumentType.Group("visitors_nextarrival",
            ArgumentType.Literal("visitors"), ArgumentType.Literal("nextarrival"), longAmount);
        command.addSyntax((sender, context) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            long seconds = Math.max(0L, context.get(visitorsNextArrival).get("amount"));
            GardenGuiSupport.visitors(player).setNextArrivalAt(System.currentTimeMillis() + (seconds * 1000L));
            sender.sendMessage("§aNext arrival timer updated.");
        }, visitorsNextArrival);

        ArgumentGroup visitorsFlag = ArgumentType.Group("visitors_flag",
            ArgumentType.Literal("visitors"), ArgumentType.Literal("flag"), ArgumentType.String("key"), ArgumentType.Boolean("enabled"));
        command.addSyntax((sender, context) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            String key = normalize(context.get(visitorsFlag).get("key"));
            boolean enabled = context.get(visitorsFlag).get("enabled");
            if (enabled) {
                GardenGuiSupport.personal(player).getVisitorRequirementFlags().add(key);
            } else {
                GardenGuiSupport.personal(player).getVisitorRequirementFlags().remove(key);
            }
            sender.sendMessage("§aVisitor flag §e" + key + " §aupdated.");
        }, visitorsFlag);

        ArgumentGroup visitorsCounter = ArgumentType.Group("visitors_counter",
            ArgumentType.Literal("visitors"), ArgumentType.Literal("counter"), ArgumentType.String("key"), longAmount);
        command.addSyntax((sender, context) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            String key = normalize(context.get(visitorsCounter).get("key"));
            long value = Math.max(0L, context.get(visitorsCounter).get("amount"));
            GardenGuiSupport.personal(player).getVisitorRequirementCounters().put(key, value);
            sender.sendMessage("§aVisitor counter §e" + key + " §aset to §e" + value + "§a.");
        }, visitorsCounter);

        ArgumentGroup personalTickets = ArgumentType.Group("personal_tickets",
            ArgumentType.Literal("personal"), ArgumentType.Literal("jacobstickets"), intAmount);
        command.addSyntax((sender, context) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            GardenGuiSupport.personal(player).setJacobsTickets(Math.max(0, context.get(personalTickets).get("amount")));
            sender.sendMessage("§aJacob's Tickets updated.");
        }, personalTickets);

        ArgumentGroup personalMedal = ArgumentType.Group("personal_medal",
            ArgumentType.Literal("personal"), ArgumentType.Literal("medal"), ArgumentType.Word("medal_type"), intAmount);
        command.addSyntax((sender, context) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            String medalType = normalize(context.get(personalMedal).get("medal_type"));
            GardenGuiSupport.personal(player).getMedals().put(medalType, Math.max(0, context.get(personalMedal).get("amount")));
            sender.sendMessage("§aUpdated medal count for §e" + medalType + "§a.");
        }, personalMedal);

        ArgumentGroup personalSpoken = ArgumentType.Group("personal_spoken",
            ArgumentType.Literal("personal"), ArgumentType.Literal("spoken"), ArgumentType.Word("mode"), ArgumentType.String("npc_id"));
        command.addSyntax((sender, context) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            String mode = normalize(context.get(personalSpoken).get("mode"));
            String npcId = GardenProgressionSupport.normalizeSpokenKey(context.get(personalSpoken).get("npc_id"));
            if (mode.equals("ADD")) {
                GardenGuiSupport.personal(player).getSpokenNpcFlags().add(npcId);
            } else if (mode.equals("REMOVE")) {
                GardenGuiSupport.personal(player).getSpokenNpcFlags().remove(npcId);
            } else {
                sender.sendMessage("§cMode must be add or remove.");
                return;
            }
            sender.sendMessage("§aUpdated spoken NPC flag for §e" + npcId + "§a.");
        }, personalSpoken);

        ArgumentGroup personalDonate = ArgumentType.Group("personal_donate",
            ArgumentType.Literal("personal"), ArgumentType.Literal("donated"), ArgumentType.Word("mode"), ArgumentType.String("item_id"));
        command.addSyntax((sender, context) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            String mode = normalize(context.get(personalDonate).get("mode"));
            String itemId = GardenProgressionSupport.normalizeKey(context.get(personalDonate).get("item_id"));
            if (mode.equals("ADD")) {
                GardenGuiSupport.personal(player).getDonatedItems().add(itemId);
            } else if (mode.equals("REMOVE")) {
                GardenGuiSupport.personal(player).getDonatedItems().remove(itemId);
            } else {
                sender.sendMessage("§cMode must be add or remove.");
                return;
            }
            sender.sendMessage("§aUpdated donated item flag for §e" + itemId + "§a.");
        }, personalDonate);

        ArgumentGroup personalExport = ArgumentType.Group("personal_export",
            ArgumentType.Literal("personal"), ArgumentType.Literal("exported"), ArgumentType.String("item_id"), longAmount);
        command.addSyntax((sender, context) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            String itemId = GardenProgressionSupport.normalizeKey(context.get(personalExport).get("item_id"));
            long amount = Math.max(0L, context.get(personalExport).get("amount"));
            GardenGuiSupport.personal(player).getExportedItems().put(itemId, amount);
            sender.sendMessage("§aUpdated exported amount for §e" + itemId + " §ato §e" + amount + "§a.");
        }, personalExport);

        ArgumentGroup personalAnitaUnlock = ArgumentType.Group("personal_anitaunlock",
            ArgumentType.Literal("personal"), ArgumentType.Literal("anitaunlock"), ArgumentType.Word("mode"), ArgumentType.String("unlock_id"));
        command.addSyntax((sender, context) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            String mode = normalize(context.get(personalAnitaUnlock).get("mode"));
            String unlockId = normalize(context.get(personalAnitaUnlock).get("unlock_id"));
            if (mode.equals("ADD")) {
                GardenGuiSupport.personal(player).getAnitaPurchases().add(unlockId);
            } else if (mode.equals("REMOVE")) {
                GardenGuiSupport.personal(player).getAnitaPurchases().remove(unlockId);
            } else {
                sender.sendMessage("§cMode must be add or remove.");
                return;
            }
            sender.sendMessage("§aUpdated Anita unlock §e" + unlockId + "§a.");
        }, personalAnitaUnlock);

        ArgumentGroup greenhouseUnlock = ArgumentType.Group("greenhouse_unlock",
            ArgumentType.Literal("greenhouse"), ArgumentType.Literal("unlock"), ArgumentType.Boolean("enabled"));
        command.addSyntax((sender, context) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            GardenGuiSupport.greenhouse(player).setBlueprintUnlocked(context.get(greenhouseUnlock).get("enabled"));
            sender.sendMessage("§aGreenhouse unlock updated.");
        }, greenhouseUnlock);

        ArgumentGroup greenhouseDna = ArgumentType.Group("greenhouse_dna",
            ArgumentType.Literal("greenhouse"), ArgumentType.Literal("dna"), intAmount);
        command.addSyntax((sender, context) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            GardenGuiSupport.greenhouse(player).setDnaMilestone(Math.max(0, context.get(greenhouseDna).get("amount")));
            sender.sendMessage("§aGreenhouse DNA milestone updated.");
        }, greenhouseDna);

        ArgumentGroup greenhouseMutationHarvest = ArgumentType.Group("greenhouse_mutationharvest",
            ArgumentType.Literal("greenhouse"), ArgumentType.Literal("mutationharvest"), ArgumentType.String("mutation_id"), intAmount);
        command.addSyntax((sender, context) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            String mutationId = normalize(context.get(greenhouseMutationHarvest).get("mutation_id"));
            GardenGuiSupport.greenhouse(player).getMutationHarvests().put(mutationId, Math.max(0, context.get(greenhouseMutationHarvest).get("amount")));
            sender.sendMessage("§aUpdated greenhouse mutation harvest for §e" + mutationId + "§a.");
        }, greenhouseMutationHarvest);

        ArgumentGroup pestsSpawn = ArgumentType.Group("pests_spawn",
            ArgumentType.Literal("pests"), ArgumentType.Literal("spawn"), ArgumentType.String("pest_id"));
        command.addSyntax((sender, context) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            String pestId = normalize(context.get(pestsSpawn).get("pest_id"));
            boolean knownPest = GardenServices.pests().getPests().stream()
                .anyMatch(entry -> normalize(GardenConfigRegistry.getString(entry, "id", "")).equals(pestId));
            if (!knownPest) {
                sender.sendMessage("§cUnknown pest §e" + pestId + "§c.");
                return;
            }
            GardenData.GardenPestState pest = new GardenData.GardenPestState();
            pest.setPestId(pestId);
            pest.setPlotId("central");
            pest.setSpawnedAt(System.currentTimeMillis());
            GardenGuiSupport.pests(player).getActivePests().add(pest);
            sender.sendMessage("§aSpawned pest §e" + pestId + "§a.");
        }, pestsSpawn);

        ArgumentGroup pestsStored = ArgumentType.Group("pests_stored",
            ArgumentType.Literal("pests"), ArgumentType.Literal("stored"), intAmount);
        command.addSyntax((sender, context) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            GardenGuiSupport.pests(player).setStoredPests(Math.max(0, context.get(pestsStored).get("amount")));
            sender.sendMessage("§aStored pests count updated.");
        }, pestsStored);

        ArgumentGroup pestsRepellent = ArgumentType.Group("pests_repellent",
            ArgumentType.Literal("pests"), ArgumentType.Literal("repellent"), longAmount);
        command.addSyntax((sender, context) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            long seconds = Math.max(0L, context.get(pestsRepellent).get("amount"));
            GardenGuiSupport.pests(player).setRepellentEndsAt(System.currentTimeMillis() + (seconds * 1000L));
            sender.sendMessage("§aRepellent timer updated.");
        }, pestsRepellent);

        ArgumentGroup composterMatter = ArgumentType.Group("composter_matter",
            ArgumentType.Literal("composter"), ArgumentType.Literal("matter"), doubleAmount);
        command.addSyntax((sender, context) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            GardenGuiSupport.composter(player).setOrganicMatter(Math.max(0D, context.get(composterMatter).get("amount")));
            sender.sendMessage("§aOrganic matter updated.");
        }, composterMatter);

        ArgumentGroup composterFuel = ArgumentType.Group("composter_fuel",
            ArgumentType.Literal("composter"), ArgumentType.Literal("fuel"), doubleAmount);
        command.addSyntax((sender, context) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            GardenGuiSupport.composter(player).setFuel(Math.max(0D, context.get(composterFuel).get("amount")));
            sender.sendMessage("§aFuel updated.");
        }, composterFuel);

        ArgumentGroup composterCompost = ArgumentType.Group("composter_compost",
            ArgumentType.Literal("composter"), ArgumentType.Literal("compost"), intAmount);
        command.addSyntax((sender, context) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            GardenGuiSupport.composter(player).setCompostAvailable(Math.max(0, context.get(composterCompost).get("amount")));
            sender.sendMessage("§aCompost available updated.");
        }, composterCompost);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().replace(' ', '_').toUpperCase(Locale.ROOT);
    }
}
