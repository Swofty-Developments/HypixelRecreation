package net.swofty.type.skyblockgeneric.tabwidgets;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Per-player tab widget configuration. Unknown JSON fields are deliberately tolerated for forward compatibility.
 */
public final class TablistWidgetSettings {
    public static final Set<String> DEFAULT_STATS = Set.of("speed", "strength", "crit_chance", "crit_damage", "attack_speed");

    public record Options(boolean spacing, boolean wrapping, boolean showPetItem, boolean showPetXp,
                          boolean showPetOverflowXp, boolean showPetTrainingSlots,
                          boolean hideEmptyTrainingSlots, boolean showPetSitter,
                          boolean showFairySouls, boolean showSkyBlockLevel, boolean showBankBalance,
                          boolean showNextInterest, boolean showSoulflow, int mayorAmount,
                          boolean showMayorPerks, boolean showVotesBar, Set<String> shownEvents,
                          Set<String> shownStats) {
        static Options defaults() {
            return new Options(true, false, false, true, false, false, false, false,
                    true, true, true, true, false, 3, false, true, Set.of(), DEFAULT_STATS);
        }

        Options toggle(String key) {
            return new Options(
                    key.equals("spacing") ? !spacing : spacing, key.equals("wrapping") ? !wrapping : wrapping,
                    key.equals("pet_item") ? !showPetItem : showPetItem, key.equals("pet_xp") ? !showPetXp : showPetXp,
                    key.equals("pet_overflow") ? !showPetOverflowXp : showPetOverflowXp,
                    key.equals("training") ? !showPetTrainingSlots : showPetTrainingSlots,
                    key.equals("hide_training") ? !hideEmptyTrainingSlots : hideEmptyTrainingSlots,
                    key.equals("pet_sitter") ? !showPetSitter : showPetSitter,
                    key.equals("fairy_souls") ? !showFairySouls : showFairySouls,
                    key.equals("skyblock_level") ? !showSkyBlockLevel : showSkyBlockLevel,
                    key.equals("bank_balance") ? !showBankBalance : showBankBalance,
                    key.equals("next_interest") ? !showNextInterest : showNextInterest,
                    key.equals("soulflow") ? !showSoulflow : showSoulflow, mayorAmount,
                    key.equals("mayor_perks") ? !showMayorPerks : showMayorPerks,
                    key.equals("votes_bar") ? !showVotesBar : showVotesBar, shownEvents, shownStats);
        }

        Options mayorAmount(int value) {
            return copy(Math.max(1, Math.min(5, value)), shownEvents, shownStats);
        }

        Options shownEvents(Set<String> value) {
            return copy(mayorAmount, Set.copyOf(value), shownStats);
        }

        Options shownStats(Set<String> value) {
            return copy(mayorAmount, shownEvents, Set.copyOf(value));
        }

        private Options copy(int mayors, Set<String> events, Set<String> stats) {
            return new Options(spacing, wrapping, showPetItem, showPetXp, showPetOverflowXp,
                    showPetTrainingSlots, hideEmptyTrainingSlots, showPetSitter, showFairySouls,
                    showSkyBlockLevel, showBankBalance, showNextInterest, showSoulflow, mayors,
                    showMayorPerks, showVotesBar, events, stats);
        }
    }

    private final EnumMap<TablistLocation, List<TablistWidget>> order = new EnumMap<>(TablistLocation.class);
    private final EnumMap<TablistLocation, EnumSet<TablistWidget>> disabled = new EnumMap<>(TablistLocation.class);
    private final EnumMap<TablistLocation, Boolean> thirdColumn = new EnumMap<>(TablistLocation.class);
    private final Map<String, Options> options = new HashMap<>();

    public TablistWidgetSettings() {
        resetAll();
    }

    public List<TablistWidget> order(TablistLocation location) {
        return order.get(location);
    }

    public boolean enabled(TablistLocation location, TablistWidget widget) {
        return widget == TablistWidget.GENERAL_INFO || !disabled.get(location).contains(widget);
    }

    public void toggle(TablistLocation location, TablistWidget widget) {
        if (widget != TablistWidget.GENERAL_INFO && !disabled.get(location).remove(widget))
            disabled.get(location).add(widget);
    }

    public Options options(TablistLocation location, TablistWidget widget) {
        return options.getOrDefault(key(location, widget), Options.defaults());
    }

    public boolean thirdColumn(TablistLocation location) {
        return thirdColumn.getOrDefault(location, false);
    }

    public void toggleThirdColumn(TablistLocation location) {
        thirdColumn.put(location, !thirdColumn(location));
    }

    public void toggleOption(TablistLocation location, TablistWidget widget, String key) {
        options.put(key(location, widget), options(location, widget).toggle(key));
    }

    public void changeMayorAmount(TablistLocation location, int delta) {
        Options o = options(location, TablistWidget.ELECTION);
        options.put(key(location, TablistWidget.ELECTION), o.mayorAmount(o.mayorAmount() + delta));
    }

    public void toggleEvent(TablistLocation location, String id, Collection<String> defaults) {
        Options o = options(location, TablistWidget.EVENTS);
        Set<String> set = new HashSet<>(o.shownEvents().isEmpty() ? defaults : o.shownEvents());
        if (!set.remove(id)) set.add(id);
        options.put(key(location, TablistWidget.EVENTS), o.shownEvents(set));
    }

    public void clearEvents(TablistLocation location) {
        Options o = options(location, TablistWidget.EVENTS);
        options.put(key(location, TablistWidget.EVENTS), o.shownEvents(Set.of("__none__")));
    }

    public boolean eventShown(TablistLocation location, String id, Collection<String> defaults) {
        Options o = options(location, TablistWidget.EVENTS);
        return (o.shownEvents().isEmpty() ? defaults : o.shownEvents()).contains(id);
    }

    public void toggleStat(TablistLocation location, String id) {
        Options o = options(location, TablistWidget.STATS);
        Set<String> set = new HashSet<>(o.shownStats());
        if (!set.remove(id)) set.add(id);
        options.put(key(location, TablistWidget.STATS), o.shownStats(set));
    }

    public void clearStats(TablistLocation location) {
        Options o = options(location, TablistWidget.STATS);
        options.put(key(location, TablistWidget.STATS), o.shownStats(Set.of()));
    }

    public void move(TablistLocation location, TablistWidget widget, int delta) {
        List<TablistWidget> list = order.get(location);
        int i = list.indexOf(widget), n = Math.max(1, Math.min(list.size() - 1, i + delta));
        if (i > 0 && i != n) {
            list.remove(i);
            list.add(n, widget);
        }
    }

    public void reset(TablistLocation location) {
        Set<TablistWidget> available = TablistWidget.available(location);
        order.put(location, new ArrayList<>(available));
        EnumSet<TablistWidget> d = EnumSet.copyOf(available);
        d.removeAll(EnumSet.of(TablistWidget.GENERAL_INFO, TablistWidget.PROFILE, TablistWidget.PET, TablistWidget.FIRE_SALES, TablistWidget.ELECTION, TablistWidget.EVENTS, TablistWidget.SKILLS, TablistWidget.STATS));
        disabled.put(location, d);
        thirdColumn.put(location, false);
        options.keySet().removeIf(k -> k.startsWith(location + ":"));
    }

    public void resetAll() {
        options.clear();
        for (TablistLocation l : TablistLocation.values()) reset(l);
    }

    public void apply(TablistLocation from, TablistWidget widget) {
        Options o = options(from, widget);
        for (TablistLocation l : TablistLocation.values())
            if (l != TablistLocation.THE_RIFT && TablistWidget.available(l).contains(widget))
                options.put(key(l, widget), o);
    }

    public String serialize() {
        JSONObject root = new JSONObject();
        for (TablistLocation l : TablistLocation.values()) {
            JSONObject x = new JSONObject();
            x.put("order", new JSONArray(order(l).stream().map(Enum::name).toList()));
            x.put("disabled", new JSONArray(disabled.get(l).stream().map(Enum::name).toList()));
            x.put("thirdColumn", thirdColumn(l));
            root.put(l.name(), x);
        }
        JSONObject os = new JSONObject();
        options.forEach((k, v) -> os.put(k, toJson(v)));
        root.put("options", os);
        return root.toString();
    }

    private static JSONObject toJson(Options v) {
        return new JSONObject().put("spacing", v.spacing()).put("wrapping", v.wrapping()).put("showPetItem", v.showPetItem()).put("showPetXp", v.showPetXp()).put("showPetOverflowXp", v.showPetOverflowXp()).put("showPetTrainingSlots", v.showPetTrainingSlots()).put("hideEmptyTrainingSlots", v.hideEmptyTrainingSlots()).put("showPetSitter", v.showPetSitter()).put("showFairySouls", v.showFairySouls()).put("showSkyBlockLevel", v.showSkyBlockLevel()).put("showBankBalance", v.showBankBalance()).put("showNextInterest", v.showNextInterest()).put("showSoulflow", v.showSoulflow()).put("mayorAmount", v.mayorAmount()).put("showMayorPerks", v.showMayorPerks()).put("showVotesBar", v.showVotesBar()).put("shownEvents", new JSONArray(v.shownEvents())).put("shownStats", new JSONArray(v.shownStats()));
    }

    public static TablistWidgetSettings deserialize(String json) {
        TablistWidgetSettings s = new TablistWidgetSettings();
        if (json == null || json.isBlank() || json.equals("{}")) return s;
        try {
            JSONObject root = new JSONObject(json);
            for (TablistLocation l : TablistLocation.values())
                if (root.has(l.name())) {
                    JSONObject x = root.getJSONObject(l.name());
                    List<TablistWidget> list = new ArrayList<>();
                    x.optJSONArray("order", new JSONArray()).forEach(v -> {
                        try {
                            TablistWidget w = TablistWidget.valueOf(v.toString());
                            if (TablistWidget.available(l).contains(w) && !list.contains(w)) list.add(w);
                        } catch (Exception ignored) {
                        }
                    });
                    TablistWidget.available(l).forEach(w -> {
                        if (!list.contains(w)) list.add(w);
                    });
                    s.order.put(l, list);
                    EnumSet<TablistWidget> d = EnumSet.noneOf(TablistWidget.class);
                    x.optJSONArray("disabled", new JSONArray()).forEach(v -> {
                        try {
                            d.add(TablistWidget.valueOf(v.toString()));
                        } catch (Exception ignored) {
                        }
                    });
                    s.disabled.put(l, d);
                    s.thirdColumn.put(l, x.optBoolean("thirdColumn", false));
                }
            JSONObject os = root.optJSONObject("options");
            if (os != null) for (String k : os.keySet()) s.options.put(k, fromJson(os.getJSONObject(k)));
            return s;
        } catch (Exception ignored) {
            return s;
        }
    }

    private static Options fromJson(JSONObject x) {
        Options d = Options.defaults();
        return new Options(x.optBoolean("spacing", d.spacing()), x.optBoolean("wrapping", d.wrapping()), x.optBoolean("showPetItem", d.showPetItem()), x.optBoolean("showPetXp", d.showPetXp()), x.optBoolean("showPetOverflowXp", d.showPetOverflowXp()), x.optBoolean("showPetTrainingSlots", d.showPetTrainingSlots()), x.optBoolean("hideEmptyTrainingSlots", d.hideEmptyTrainingSlots()), x.optBoolean("showPetSitter", d.showPetSitter()), x.optBoolean("showFairySouls", d.showFairySouls()), x.optBoolean("showSkyBlockLevel", d.showSkyBlockLevel()), x.optBoolean("showBankBalance", d.showBankBalance()), x.optBoolean("showNextInterest", d.showNextInterest()), x.optBoolean("showSoulflow", d.showSoulflow()), x.optInt("mayorAmount", d.mayorAmount()), x.optBoolean("showMayorPerks", d.showMayorPerks()), x.optBoolean("showVotesBar", d.showVotesBar()), jsonSet(x.optJSONArray("shownEvents")), x.has("shownStats") ? jsonSet(x.optJSONArray("shownStats")) : DEFAULT_STATS);
    }

    private static Set<String> jsonSet(JSONArray values) {
        if (values == null) return Set.of();
        Set<String> s = new HashSet<>();
        values.forEach(v -> s.add(v.toString()));
        return Set.copyOf(s);
    }

    private static String key(TablistLocation l, TablistWidget w) {
        return l + ":" + w;
    }
}
