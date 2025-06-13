package dev.tenacity.commands.impl;

import dev.tenacity.Client;
import dev.tenacity.commands.Command;
import dev.tenacity.i18n.Localization;
import dev.tenacity.module.Module;
import org.lwjglx.input.Keyboard;

public class HideCommand extends Command {
    public HideCommand() {
        super("hide", "Hide a module", ".hide [module]");
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 2) {
            usage();
        } else {
            String stringModule = args[0];

                Module module = Client.INSTANCE.getModuleManager().getModuleByName(stringModule);
                Client.INSTANCE.getModuleManager().commandHiddenModules.add(module);
                sendChatWithPrefix("Set hide for " + Localization.get(module.getName()));

        }
    }

}
