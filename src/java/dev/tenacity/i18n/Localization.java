package dev.tenacity.i18n;

import dev.tenacity.Client;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 本地化工具类，用于根据语言加载字符串资源
 */
public class Localization {

    // 是否已经加载资源
    private static boolean populated = false;

    static {
        // 类加载时尝试初始化语言资源
        populate();
    }

    /**
     * 获取当前语言下某个 key 的翻译文本
     */
    public static String get(String key) {
        return get(key, Client.INSTANCE.getLocale());
    }

    /**
     * 获取指定语言下某个 key 的翻译文本，提供英文和 key 本身作为后备
     */
    public static String get(String key, Locale locale) {
        if (!populated) populate();
        String translated = locale.getStrings().get(key);
        if (translated == null) translated = Locale.EN_US.getStrings().get(key);
        return translated == null ? key : translated;
    }

    /**
     * Gets a localized string using lowercase key matching (useful for module names)
     * @param key The lowercase key to look up
     * @param locale The locale to use for translation
     * @return The translated string or the original key if not found
     */
    public static String getLopper(String key, Locale locale) {
        if (!populated) populate();

        // Convert the key to lowercase for matching
        String lowerKey = key.toLowerCase();

        // First try the requested locale with lowercase matching
        String translated = locale.getStrings().entrySet().stream()
                .filter(entry -> entry.getKey().toLowerCase().equals(lowerKey))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);

        // If not found, try English locale with lowercase matching
        if (translated == null) {
            translated = Locale.EN_US.getStrings().entrySet().stream()
                    .filter(entry -> entry.getKey().toLowerCase().equals(lowerKey))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);
        }

        // If still not found, return the original key
        return translated == null ? key : translated;
    }
    /**
     * 读取所有语言文件并解析 key-value
     */
    @SneakyThrows
    public static void populate() {
        for (Locale locale : Locale.values()) {
            ResourceLocation resourceLocation = new ResourceLocation("Tenacity/i18n/" + locale.getFile() + ".properties");

            try (InputStream stream = Minecraft.getMinecraft().getResourceManager().getResource(resourceLocation).getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    // 忽略空行和注释
                    if (line.isEmpty() || line.startsWith("#")) continue;

                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        locale.getStrings().put(parts[0].trim(), parts[1].trim());
                    }
                }
            }
        }
        populated = true;
    }
}
