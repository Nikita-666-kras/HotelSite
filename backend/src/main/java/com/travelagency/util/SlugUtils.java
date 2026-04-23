package com.travelagency.util;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class SlugUtils {

    private static final Map<Character, String> RU_TO_LAT = new HashMap<>();

    static {
        String ru = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя";
        String[] lat = {
            "a", "b", "v", "g", "d", "e", "yo", "zh", "z", "i", "y", "k", "l", "m", "n", "o", "p", "r", "s", "t", "u",
            "f", "h", "ts", "ch", "sh", "sch", "", "y", "", "e", "yu", "ya"
        };
        for (int i = 0; i < ru.length(); i++) {
            RU_TO_LAT.put(ru.charAt(i), lat[i]);
            RU_TO_LAT.put(Character.toUpperCase(ru.charAt(i)), lat[i]);
        }
    }

    private SlugUtils() {}

    public static String slugify(String title) {
        if (title == null || title.isBlank()) {
            return "tour";
        }
        StringBuilder sb = new StringBuilder();
        for (char c : title.trim().toCharArray()) {
            if (RU_TO_LAT.containsKey(c)) {
                sb.append(RU_TO_LAT.get(c));
            } else {
                sb.append(c);
            }
        }
        String n = Normalizer.normalize(sb.toString(), Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        n = n.toLowerCase(Locale.ROOT);
        n = n.replaceAll("[^a-z0-9]+", "-");
        n = n.replaceAll("^-+", "").replaceAll("-+$", "");
        n = n.replaceAll("-+", "-");
        if (n.isBlank()) {
            return "tour";
        }
        return n;
    }

    public static String uniqueSlug(String base, java.util.function.Predicate<String> exists) {
        String candidate = base;
        int i = 0;
        while (exists.test(candidate)) {
            candidate = base + "-" + (++i);
        }
        return candidate;
    }
}
