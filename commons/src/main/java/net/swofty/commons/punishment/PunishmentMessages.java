package net.swofty.commons.punishment;

import net.swofty.commons.text.Text;

public final class PunishmentMessages {
    private PunishmentMessages() {}

    public static Text banMessage(ActivePunishment punishment) {
        long expiresAt = punishment.expiresAt();
        PunishmentReason reason = punishment.reason();
        String banId = punishment.banId();

        Text header = expiresAt <= 0
                ? Text.of("<c>You are permanently banned from this server!")
                : Text.of("<c>You are temporarily banned for <f>{:time} <c>from this server!",
                        expiresAt - System.currentTimeMillis());

        Text findOutMore = reason.getBanType() != null && reason.getBanType().getUrl() != null
                ? Text.of("\n<7>Find out more: <b>{}", reason.getBanType().getUrl())
                : Text.empty();

        return Text.of("""
                {0}

                <7>Reason: <f>{1}{2}

                <7>Ban ID: <f>{3}
                <7>Sharing your Ban ID may affect the processing of your appeal!\
                """, header, reason.getReasonString(), findOutMore, banId);
    }

    public static Text muteMessage(ActivePunishment punishment) {
        long expiresAt = punishment.expiresAt();
        PunishmentReason reason = punishment.reason();
        String reasonString = reason.getReasonString();

        Text header;
        Text expiry;
        if (expiresAt <= 0) {
            header = Text.of("<c>You are permanently muted on this server!");
            expiry = Text.empty();
        } else {
            long timeLeft = expiresAt - System.currentTimeMillis();
            header = Text.of("<c>You are currently muted for {}", reasonString);
            expiry = Text.of("\n<7>Your mute will expire in <c>{:time}\n", timeLeft);
        }

        String rule = " ".repeat(53);

        return Text.of("""

                <c><m>{0}</m>
                {1}
                <7>Reason: <f>{2}{3}
                <7>Find out more here: <f>www.hypixel.net/mutes
                <7>Mute ID: <f>{4}
                <c><m>{0}</m>
                """, rule, header, reasonString, expiry, punishment.banId());
    }
}
