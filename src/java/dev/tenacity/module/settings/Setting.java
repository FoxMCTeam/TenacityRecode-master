package dev.tenacity.module.settings;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import org.openjdk.nashorn.api.scripting.JSObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

@Getter
public abstract class Setting {

    @Expose
    @SerializedName("name")
    public String name;
    @Setter
    private List<ParentAttribute<? extends Setting>> parents = new ArrayList<>();

    public static <T extends Setting> void addParent(T parent, Predicate<T> condition, Setting... settings) {
        Arrays.asList(settings).forEach(s -> s.addParent(new ParentAttribute<>(parent, condition)));
    }

    public boolean hasParent() {
        return !parents.isEmpty();
    }

    public void addParent(ParentAttribute<? extends Setting> parent) {
        parents.add(parent);
    }

    public <T extends Setting> void addParent(T parent, Predicate<T> condition) {
        addParent(new ParentAttribute<>(parent, condition));
    }

    public boolean cannotBeShown() {
        if (!hasParent()) return false;
        return getParents().stream().noneMatch(ParentAttribute::isValid);
    }


    public <T extends Setting> void addJSParent(T parent, JSObject scriptFunction) {
        Predicate<T> predicate;
        try {
            predicate = object -> (boolean) scriptFunction.call(null, object);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create predicate for parent");
        }

        addParent(new ParentAttribute<>(parent, predicate));
    }


    public abstract <T> T getConfigValue();


}
