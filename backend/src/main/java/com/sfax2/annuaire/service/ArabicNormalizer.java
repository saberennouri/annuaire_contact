package com.sfax2.annuaire.service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Normalise le texte arabe pour la recherche : ignore les diacritiques, unifie les variantes
 * de lettres (أ إ آ → ا, ة → ه, ى → ي…) et convertit les chiffres arabes-indiens.
 */
public final class ArabicNormalizer {

    private static final Pattern MARKS = Pattern.compile("[\\u064B-\\u065F\\u0670\\u0640]");
    private static final Pattern DIGITS_AND_SPACES = Pattern.compile("[0-9\\s]+");

    private ArabicNormalizer() {
    }

    public static String normalize(String input) {
        if (input == null) {
            return "";
        }
        String s = MARKS.matcher(input).replaceAll("");
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case 'أ', 'إ', 'آ', 'ٱ' -> sb.append('ا');
                case 'ى', 'ئ' -> sb.append('ي');
                case 'ة' -> sb.append('ه');
                case 'ؤ' -> sb.append('و');
                default -> {
                    if (c >= '٠' && c <= '٩') {
                        sb.append((char) ('0' + (c - '٠')));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString().toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
    }

    /**
     * Découpe une requête en mots-clés. Une requête composée uniquement de chiffres
     * (« 97 362 117 ») devient un seul mot-clé (« 97362117 »).
     */
    public static List<String> tokens(String query) {
        String n = normalize(query);
        if (n.isEmpty()) {
            return List.of();
        }
        if (DIGITS_AND_SPACES.matcher(n).matches()) {
            return List.of(n.replace(" ", ""));
        }
        return Arrays.asList(n.split(" "));
    }
}
