package dev.tenacity.ui.altmanager.helpers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import dev.tenacity.Client;
import dev.tenacity.ui.notifications.NotificationManager;
import dev.tenacity.ui.notifications.NotificationType;
import dev.tenacity.utils.Utils;
import dev.tenacity.utils.client.addons.microsoft.MicrosoftLogin;
import dev.tenacity.utils.misc.Multithreading;
import dev.tenacity.utils.objects.FileUtils;
import dev.tenacity.utils.objects.TextField;
import dev.tenacity.utils.time.TimerUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.Session;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;


public class AltManagerUtils implements Utils {

    public static File altsFile = new File(Client.DIRECTORY, "Alts.json");
    @Getter
    private static ConfigAlt configAlt;
    @Getter
    private static List<Alt> alts = new ArrayList<>();
    private final TimerUtil timerUtil = new TimerUtil();

    public AltManagerUtils() {
        if (!altsFile.exists()) {
            FileUtils.createFile(altsFile, false);
            configAlt = new ConfigAlt(alts);
        }
        if (!FileUtils.getFileContent(altsFile).isEmpty()) {
            Gson gson = new Gson();
            String contents = FileUtils.readFile(altsFile);
            ConfigAlt configAlt1 = gson.fromJson(contents, ConfigAlt.class);
            configAlt = configAlt1;
            alts = configAlt1.alts;
            if (configAlt1.alts != null) {
                configAlt1.alts.forEach(this::getHead);
            } else {
                configAlt1.alts = new ArrayList<>();
            }
        }
    }

    public static void removeAlt(Alt alt) {
        if (alt != null) {
            configAlt.alts.remove(alt);
        }
    }

    public static void writeAlts() {
        Multithreading.runAsync(() -> {
            val gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
            try {
                Files.write(
                        altsFile.toPath(),gson.toJson(AltManagerUtils.getConfigAlt()).getBytes(StandardCharsets.UTF_8)
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void writeAltsToFile() {
        if (timerUtil.hasTimeElapsed(15000, true)) {
            new Thread(() -> {
                try {
                    if (!altsFile.exists()) {
                        if (altsFile.getParentFile().mkdirs()) {
                            altsFile.createNewFile();
                        }
                    }
                    val gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
                    FileUtils.writeFile(altsFile, gson.toJson(configAlt));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    public void login(TextField username, TextField password) {
        String usernameS;
        String passwordS;
        if (username.getText().contains(":")) {
            String[] combo = username.getText().split(":");
            usernameS = combo[0];
            passwordS = combo[1];
        } else {
            usernameS = username.getText();
            passwordS = password.getText();
        }

        if (usernameS.isEmpty() && passwordS.isEmpty()) return;

        loginWithString(usernameS, passwordS, false);
    }


    public void microsoftLoginAsync(String email, String password) {
        microsoftLoginAsync(null, email, password);
    }


    public void microsoftLoginAsync(Alt alt, String email, String password) {
        if (alt == null) {
            alt = new Alt(email, password);
        }
        AtomicReference<String> refreshTokenSTR = new AtomicReference<>("");
        Alt finalAlt = alt;
        Multithreading.runAsync(() -> {
            CompletableFuture<Session> future = new CompletableFuture<>();
            MicrosoftLogin.getRefreshToken(refreshToken -> {
                if (refreshToken != null) {
                    CompletableFuture<Session> login = MicrosoftLogin.login(refreshToken.mcToken);
                    try {
                        future.complete(login.get());
                        refreshTokenSTR.set(refreshToken.mcToken);
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            Session auth = future.join();
            if (auth != null) {
                mc.session = auth;
                finalAlt.uuid = auth.getPlayerID();
                finalAlt.altType = Alt.AltType.MICROSOFT;
                finalAlt.username = auth.getUsername();
                finalAlt.token = refreshTokenSTR.get();
                if (auth.getUsername() == null) {
                    NotificationManager.post(NotificationType.WARNING, "Alt Manager", "Please set an username on your Minecraft account!", 12);
                }
                Alt.stage = 2;
                finalAlt.altState = Alt.AltState.LOGIN_SUCCESS;
                for (Alt alt1 : configAlt.getAlts()) {
                    if (!(Objects.equals(alt1.username, finalAlt.username))) {
                        continue;
                    }
                    configAlt.alts.remove(alt1);
                }
                AltManagerUtils.getConfigAlt().getAlts().add(finalAlt);
                AltManagerUtils.writeAlts();
                Client.INSTANCE.getAltManager().currentSessionAlt = finalAlt;
                Client.INSTANCE.getAltManager().getAltPanel().refreshAlts();
            } else {
                Alt.stage = 1;
                finalAlt.altState = Alt.AltState.LOGIN_FAIL;
            }
        });

    }

    public void loginWithString(String username, String password, boolean microsoft) {
        for (Alt alt : configAlt.getAlts()) {
            if (alt.email.equals(username) && alt.password.equals(password)) {
                Alt.stage = 0;
                alt.loginAsync(microsoft);
                return;
            }
        }
        Alt alt = new Alt(username, password);
        configAlt.getAlts().add(alt);
        Alt.stage = 0;
        alt.loginAsync(microsoft);
    }

    public void getHead(Alt alt) {
        if (alt.uuid == null || alt.head != null || alt.headTexture || alt.headTries > 5) return;
        Multithreading.runAsync(() -> {
            alt.headTries++;
            try {
                BufferedImage image = ImageIO.read(new URL("https://visage.surgeplay.com/bust/160/" + alt.uuid));
                alt.headTexture = true;
                // run on main thread for OpenGL context
                mc.addScheduledTask(() -> {
                    DynamicTexture texture = new DynamicTexture(image);
                    alt.head = mc.getTextureManager().getDynamicTextureLocation("HEAD-" + alt.uuid, texture);
                });
            } catch (IOException e) {
                alt.headTexture = false;
            }
        });
    }

    @Getter
    @Setter
    public static class ConfigAlt {
        @Expose
        @SerializedName("alts")
        List<Alt> alts;

        public void setLatestAlt(Alt latestAlt) {
            this.latestAlt = latestAlt;
            AltManagerUtils.writeAlts();
        }

        @Expose
        @SerializedName("latestAlt")
        Alt latestAlt;

        ConfigAlt(List<Alt> alts) {
            this.alts = alts;
        }
    }
}
