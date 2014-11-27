package net.thucydides.maven.plugin.utils;

public final class NameUtils {
    private NameUtils() {
    }

    public static String getMethodNameFrom(String text) {
        String[] words = text.split(" ");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i == 0) {
                builder.append(Character.toLowerCase(words[i].charAt(0)));
            } else if (words[i].length() == 0) {
                continue;
            } else {
                builder.append(Character.toTitleCase(words[i].charAt(0)));
            }
            builder.append(words[i].substring(1));
        }
        return builder.toString().replaceAll("\\W", "");
    }

    public static String replaceFirstCharacterToLowerCase(String in) {
        StringBuilder builder = new StringBuilder();
        builder.append(Character.toLowerCase(in.charAt(0)));
        builder.append(in.substring(1));
        return builder.toString();
    }
}
