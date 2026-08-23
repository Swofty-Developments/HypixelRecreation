package net.swofty.type.skyblockgeneric.commands;

import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.bank.PersonalBankService;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointBankData;
import net.swofty.type.skyblockgeneric.gui.inventories.banker.GUIBanker;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(description = "Open your Personal Bank", usage = "/bank", permission = Rank.DEFAULT,
    labels = "bank", allowsConsole = false)
public class BankCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.setDefaultExecutor((sender, _) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            if (!player.isBoosterCookieActive()) {
                player.sendMessage("<c>You need an active Booster Cookie to use /bank!");
                return;
            }
            if (!PersonalBankService.isUnlocked(player) || PersonalBankService.remaining(player) > 0) {
                player.sendMessage("<c>Your Personal Bank is locked or still on cooldown!");
                return;
            }
            new GUIBanker().open(player);
            DatapointBankData.BankData data = PersonalBankService.data(player);
            data.setLastRemoteBankUse(System.currentTimeMillis());
            player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.BANK_DATA, DatapointBankData.class).setValue(data);
        });
    }
}
