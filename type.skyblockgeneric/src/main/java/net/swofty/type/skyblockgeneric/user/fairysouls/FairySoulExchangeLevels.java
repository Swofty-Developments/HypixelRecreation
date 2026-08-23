package net.swofty.type.skyblockgeneric.user.fairysouls;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public enum FairySoulExchangeLevels {
    EXCHANGE_1(10, 1),
    EXCHANGE_2(10, 1),
    EXCHANGE_3(10, 0),
    EXCHANGE_4(10, 1),
    EXCHANGE_5(10, 0),
    EXCHANGE_6(10, 1),
    EXCHANGE_7(10, 0),
    EXCHANGE_8(10, 1),
    EXCHANGE_9(10, 0),
    EXCHANGE_10(10, 1),
    EXCHANGE_11(10, 0),
    EXCHANGE_12(10, 0),
    EXCHANGE_13(10, 1),
    EXCHANGE_14(10, 0),
    EXCHANGE_15(10, 0),
    EXCHANGE_16(10, 1),
    EXCHANGE_17(10, 0),
    EXCHANGE_18(10, 0),
    EXCHANGE_19(10, 1),
    EXCHANGE_20(10, 0),
    EXCHANGE_21(10, 0),
    EXCHANGE_22(10, 1),
    EXCHANGE_23(10, 0),
    EXCHANGE_24(10, 1),
    EXCHANGE_25(10, 0),
    EXCHANGE_26(10, 1),
    EXCHANGE_27(10, 0),
    EXCHANGE_28(10, 1),
    EXCHANGE_29(10, 0),
    EXCHANGE_30(10, 1),
    EXCHANGE_31(10, 0),
    EXCHANGE_32(10, 1),
    EXCHANGE_33(10, 0),
    EXCHANGE_34(10, 1),
    EXCHANGE_35(10, 0),
    EXCHANGE_36(10, 0),
    EXCHANGE_37(10, 1),
    EXCHANGE_38(10, 0),
    EXCHANGE_39(10, 0),
    EXCHANGE_40(10, 1),
    EXCHANGE_41(10, 0),
    EXCHANGE_42(10, 0),
    EXCHANGE_43(10, 0),
    EXCHANGE_44(10, 1),
    EXCHANGE_45(10, 0),
    EXCHANGE_46(10, 0),
    EXCHANGE_47(10, 0),
    EXCHANGE_48(10, 0),
    ;

    private final double skyBlockXP;
    private final int backpackSlots;

    FairySoulExchangeLevels(double skyBlockXP, int backpackSlots) {
        this.skyBlockXP = skyBlockXP;
        this.backpackSlots = backpackSlots;
    }

    public int previousAmountOfUnlockedSlots(int baseNumber) {
        int previous = 0;
        for (FairySoulExchangeLevels levels : values()) {
            if (levels.ordinal() < this.ordinal()) {
                previous += levels.getBackpackSlots();
            }
        }
        return baseNumber + previous;
    }

    public List<String> getDisplay() {
        List<String> toReturn = new ArrayList<>();

        if (skyBlockXP > 0) {
            toReturn.add("<8>+<b>" + skyBlockXP + " SkyBlock XP");
        }
        for (int i = 0; i < backpackSlots; i++) {
            toReturn.add("<6>Backpack Slot #" + (previousAmountOfUnlockedSlots(1) + i + 1));
        }

        return toReturn;
    }

    public static FairySoulExchangeLevels getLevel(int level) {
        for (FairySoulExchangeLevels levels : values()) {
            if ((levels.ordinal() + 1) == level) {
                return levels;
            }
        }
        return null;
    }
}
