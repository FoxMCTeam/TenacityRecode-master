package dev.tenacity.i18n;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;

/**
 * @author Hazsi
 * @since 10/31/2022
 */
@Getter
@RequiredArgsConstructor
public enum Locale {
    ZH_HK("zh_HK"),
    ZH_CN("zh_CN"),
    EN_US("en_US"),
    DE_DE("de_DE"),
    FR_FR("fr_FR"),
    RU_RU("ru_RU");

    private final String file;
    private final HashMap<String, String> strings = new HashMap<>();
}