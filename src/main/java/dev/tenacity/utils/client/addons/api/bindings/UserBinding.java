package dev.tenacity.utils.client.addons.api.bindings;

import store.intent.intentguard.annotation.Exclude;
import store.intent.intentguard.annotation.Strategy;

@Exclude(Strategy.NAME_REMAPPING)
public class UserBinding {

    public String username() {
        return "USERNAME";
    }

}
