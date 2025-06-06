package dev.tenacity.utils.client.addons.api.bindings;


import dev.tenacity.Client;

public class UserBinding {

    public String username() {
        return Client.userName;
    }

}
