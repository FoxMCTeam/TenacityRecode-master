package dev.tenacity.commands.impl;

import dev.tenacity.Client;
import dev.tenacity.commands.Command;
import dev.tenacity.module.Module;
import dev.tenacity.module.settings.Setting;
import dev.tenacity.module.settings.impl.*;

import java.awt.*;
import java.util.Arrays;

public class SettingCommand extends Command {

    public SettingCommand() {
        super("setting", "Changes settings of a module", ".setting module_name setting_name value");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            usage();
        } else {

            String moduleName = args[0].replace("_", " ");
            Module module = Client.INSTANCE.getModuleManager().getModuleByName(moduleName);
            if (module == null) {
                sendChatError("Module not found");
                usage();
                return;
            }

            String sendSuccess = args[args.length - 1];
            if (sendSuccess.equals("sendSuccess=false")) {
                Command.sendSuccess = false;
            }

            try {
                if (args.length < 2) {
                    sendChatError("No setting specified for the module " + module.getName());
                    usage();
                    return;
                }

                boolean found = false;
                for (Setting setting : module.getSettingsList()) {
                    String settingName = setting.name.replaceAll(" ", "_");
                    String settingInputName = args[1];
                    if (settingName.equalsIgnoreCase(settingInputName)) {
                        found = true;
                        boolean setValue = false;
                        String value = String.join(" ",
                                Arrays.copyOfRange(args, 2, Command.sendSuccess ? args.length : (args.length - 1)));

                        if (setting instanceof ModeSetting modeSetting) {
                            for (String mode : modeSetting.modes) {
                                if (mode.equalsIgnoreCase(value)) {
                                    modeSetting.setCurrentMode(mode);
                                    setValue = true;
                                    break;
                                }
                            }
                            if (!setValue) {
                                sendChatError("Invalid mode");
                            }
                        }

                        if (setting instanceof NumberSetting numberSetting) {
                            double valueDouble;
                            try {
                                valueDouble = Double.parseDouble(value);
                            } catch (NumberFormatException e) {
                                sendChatError("Invalid number");
                                return;
                            }

                            numberSetting.setValue(valueDouble);
                            sendChatWithInfo(numberSetting.getValue() + " value");

                            settingName = settingName.replaceAll("_", " ");
                            sendChatWithPrefix("Set " + module.getName() + " module's " + settingName + " to " + numberSetting.getValue());
                            break;
                        }

                        if (setting instanceof StringSetting stringSetting) {
                            stringSetting.setString(value);
                            setValue = true;
                        }

                        if (setting instanceof BooleanSetting booleanSetting) {
                            booleanSetting.setState(Boolean.parseBoolean(value));
                            setValue = true;
                        }

                        if (setting instanceof MultipleBoolSetting multipleBoolSetting) {
                            String[] inputBool = value.split(" ");
                            String boolName = inputBool[0].replace("_", " ");
                            BooleanSetting booleanSetting = multipleBoolSetting.getSetting(boolName);
                            if (booleanSetting == null) {
                                sendChatError("Invalid boolean setting name, " + boolName + ". (In multiple bool setting)");
                                return;
                            }

                            booleanSetting.setState(Boolean.parseBoolean(inputBool[1]));

                            settingName = settingName.replaceAll("_", " ");
                            sendChatWithPrefix("Set " + module.getName() + " module's " + settingName + "'s " + boolName + " to " + inputBool[1]);
                            break;
                        }

                        if (setting instanceof ColorSetting colorSetting) {
                            Color newColor;
                            try {
                                newColor = Color.decode("#" + value);
                            } catch (NumberFormatException e) {
                                sendChatError("Invalid hex");
                                return;
                            }

                            colorSetting.setColor(newColor);
                            setValue = true;
                        }


                        if (setValue) {
                            settingName = settingName.replaceAll("_", " ");
                            sendChatWithPrefix("Set " + module.getName() + " " + settingName + " to " + value);
                            break;
                        }
                    }
                }
                if (!found) {
                    sendChatError("Setting not found");
                }
            } catch (Exception e) {
                sendChatError("Error: " + e.getMessage());
                usage();
            }
            SettingCommand.sendSuccess = true;
        }
    }

}
