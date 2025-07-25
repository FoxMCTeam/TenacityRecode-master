package dev.tenacity.commands.impl;

import dev.tenacity.Client;
import dev.tenacity.commands.Command;
import dev.tenacity.i18n.Localization;
import dev.tenacity.module.Module;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ToggleCommand extends Command {

    public ToggleCommand() {
        super("toggle", "Toggles a module", ".t [module]", "t");
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            usage();
        } else {
            String moduleName = Arrays.stream(args).skip(0).collect(Collectors.joining(" "));
            Module module = Client.INSTANCE.getModuleManager().getModuleByName(moduleName);
            if (module != null) {
                module.toggle();
                sendChatWithPrefix("Toggled " + Localization.get(module.getName()) + "!");
            } else {
                sendChatWithPrefix("The module \"" + moduleName + "\" does not exist!");
            }
        }
    }

}
