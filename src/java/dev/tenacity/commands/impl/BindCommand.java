package dev.tenacity.commands.impl;

import dev.tenacity.Client;
import dev.tenacity.commands.Command;
import dev.tenacity.i18n.Localization;
import dev.tenacity.module.Module;
import org.lwjglx.input.Keyboard;

public class BindCommand extends Command {

    public BindCommand() {
        super("bind", "Binds a module to a certain key", ".bind [module] [key]");
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 2) {
            usage();
        } else {
            String stringModule = args[0];
            try {
                Module module = Client.INSTANCE.getModuleManager().getModuleByName(stringModule);
                module.getKeybind().setCode(Keyboard.getKeyIndex(args[1].toUpperCase()));
                sendChatWithPrefix("Set keybind for " + Localization.get(module.getName()) + " to " + args[1].toUpperCase());
            } catch (Exception e) {
                usage();
            }
        }
    }

}
