package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCAmelia extends HypixelNPC {

    public NPCAmelia() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Amelia", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "QMZr7iKG5E74dUHwnnPYC36dM5pbiOx2rpjfbH3nzA+3yTntqWY873eHU/JHFXFfYwSKOufprcLqH2KOGjAfAhn7ez24bq4KKRfDWH1HJ0KYmo07gAjgZrYKTUrFf4dEN3mPwL8A2w8QeHLu/l95gEWx/eLYS8al+vhiomFyXJjmSjKLzrrIiQVrMa2Kaf9Ev9x/0UWWmNji2Jvhaf0NPRUXshWrLONXGUcgTZv3B8T1IPRGDPdw04L1kSuL92N5SZa2l216bJRzl4op2PQmSsGch/UP5g6jzMWx/GensNuw8X0ZmiFCi/UAvV6Ry3hH9Ce0S0FU6FMjbfeuq/r+6zp0UC1br2Nf9inLDUt0Fc4CHzLjD3Q5EkC/crj4EbaPvBMQ+AaN6n80LdlbxRESc/XlQ2U+anYSB9PPGn3cqOPOlLxkhWVLQ6IRxCehR73LQdoblIaKRc/VfWU7xjxhybSsO3muLcl2U8AAbRsqtuFlf3s6764lLdHluDZl992nWZYJMBysddpU9tVs/PGFscdJw1KC69Q3/UmNZO2HtW7B13sJw8JIFPvk2mqgCQvMfnkFJ/lHWfgITphpk6mWqe1IokA5ZCnqhmkWwUAxQaP9q7at2LbPO1gb5R/5XKhmakQZ7lShw8/blENqRe6qYpm3qWgBEtjdfQnZd9ZCo3k=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYwMTU1MTcxMzA0NSwKICAicHJvZmlsZUlkIiA6ICJkZTE0MGFmM2NmMjM0ZmM0OTJiZTE3M2Y2NjA3MzViYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJTUlRlYW0iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmQ1OTU1Y2M1YWY2ZmEzMzkwZGYzNmM5YTIzYTMyNjRjODQyNjk5MWU3NTMzYWQyZDA4MWY0MWVhMTMwYzYzYiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-15.5, 74, -68.5, -90, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        e.player().notImplemented();
    }

}
