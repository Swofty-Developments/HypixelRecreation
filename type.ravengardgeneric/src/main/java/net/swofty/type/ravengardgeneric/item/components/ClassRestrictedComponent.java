package net.swofty.type.ravengardgeneric.item.components;

import lombok.Getter;
import net.swofty.type.ravengardgeneric.classes.RavengardClass;
import net.swofty.type.ravengardgeneric.item.RavengardItemComponent;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Restricts an item to one or more classes, so kits and drops can be filtered per class. */
@Getter
public class ClassRestrictedComponent implements RavengardItemComponent {
    private final Set<RavengardClass> restrictedTo = new LinkedHashSet<>();

    @Override
    public String id() {
        return "CLASS_RESTRICTED";
    }

    @Override
    public void configure(Map<String, Object> config) {
        if (config.get("classes") instanceof List<?> values) {
            for (Object value : values) {
                RavengardClass parsed = RavengardClass.fromKey(String.valueOf(value));
                if (parsed != null) {
                    restrictedTo.add(parsed);
                }
            }
        }
        Object single = config.get("class");
        if (single != null) {
            RavengardClass parsed = RavengardClass.fromKey(single.toString());
            if (parsed != null) {
                restrictedTo.add(parsed);
            }
        }
    }
}
