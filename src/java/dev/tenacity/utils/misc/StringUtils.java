package dev.tenacity.utils.misc;

import dev.tenacity.i18n.Localization;
import dev.tenacity.module.Module;

import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StringUtils {

    public static String findLongestModuleName(List<Module> modules) {
        return Collections.max(modules, Comparator.comparing(module -> (Localization.get(module.getName()) + (module.hasMode() ? " " + module.getSuffix() : "")).length())).getName();
    }

    public static String getLongestModeName(List<String> listOfWords) {
        String longestWord = null;
        for (String word : listOfWords) {
            if (longestWord == null || word.length() > longestWord.length()) {
                longestWord = word;
            }
        }
        return longestWord != null ? longestWord : "";
    }

    public static String b64(Object o) {
        return Base64.getEncoder().encodeToString(String.valueOf(o).getBytes());
    }

    public static String upperSnakeCaseToPascal(String s) {
        if (s == null) return null;
        if (s.length() == 1) return Character.toString(s.charAt(0));
        return s.charAt(0) + s.substring(1).toLowerCase();
    }

    public static String replaceUserSymbols(String str) {
        return str.replace('&', '\247').replace("<3", "\u2764");
    }

    public static String getTrimmedClipboardContents() {
        String data = ClipboardUtils.getClipboardContents();

        if (data != null) {
            data = data.trim();

            if (data.indexOf('\n') != -1)
                data = data.replace("\n", "");
        }

        return data;
    }


    public static String fromCharCodes(int[] codes) {
        StringBuilder builder = new StringBuilder();
        for (int cc : codes) {
            builder.append((char) cc);

        }
        return builder.toString();
    }

}
