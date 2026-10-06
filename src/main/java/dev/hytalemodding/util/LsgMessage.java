package dev.hytalemodding.util;

import com.hypixel.hytale.server.core.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LsgMessage {

    private static final Pattern COLOR_PATTERN = Pattern.compile("[§&]([0-9a-fk-orA-FK-OR])");

    private LsgMessage() {}

    /**
     * Elimina códigos de color (§a, &a, etc.) para que las etiquetas de interfaz (UI)
     * nunca muestren símbolos o códigos literales como §a o $a.
     */
    public static String stripColor(String input) {
        if (input == null) return "";
        return COLOR_PATTERN.matcher(input).replaceAll("");
    }

    /**
     * Transforma texto con códigos de formato de color (§ o &) en un Message nativo de Hytale
     * con los colores y estilos aplicados correctamente sin mostrar caracteres extraños.
     */
    public static Message format(String input) {
        if (input == null || input.isEmpty()) {
            return Message.empty();
        }

        if (!input.contains("§") && !input.contains("&")) {
            return Message.raw(input);
        }

        List<Message> parts = new ArrayList<>();
        String currentColor = null;
        boolean bold = false;
        boolean italic = false;
        boolean monospace = false;
        boolean strikethrough = false;

        int lastIdx = 0;
        Matcher matcher = COLOR_PATTERN.matcher(input);

        while (matcher.find()) {
            int matchStart = matcher.start();
            if (matchStart > lastIdx) {
                String text = input.substring(lastIdx, matchStart);
                Message part = Message.raw(text);
                if (currentColor != null) part.color(currentColor);
                if (bold) part.bold(true);
                if (italic) part.italic(true);
                if (monospace) part.monospace(true);
                if (strikethrough) part.strikethrough(true);
                parts.add(part);
            }

            char code = Character.toLowerCase(matcher.group(1).charAt(0));
            switch (code) {
                case '0' -> currentColor = "#000000";
                case '1' -> currentColor = "#0000aa";
                case '2' -> currentColor = "#00aa00";
                case '3' -> currentColor = "#00aaaa";
                case '4' -> currentColor = "#aa0000";
                case '5' -> currentColor = "#aa00aa";
                case '6' -> currentColor = "#ffaa00";
                case '7' -> currentColor = "#aaaaaa";
                case '8' -> currentColor = "#555555";
                case '9' -> currentColor = "#5555ff";
                case 'a' -> currentColor = "#55ff55";
                case 'b' -> currentColor = "#55ffff";
                case 'c' -> currentColor = "#ff5555";
                case 'd' -> currentColor = "#ff55ff";
                case 'e' -> currentColor = "#ffff55";
                case 'f' -> currentColor = "#ffffff";
                case 'l' -> bold = true;
                case 'o' -> italic = true;
                case 'm' -> strikethrough = true;
                case 'r' -> {
                    currentColor = null;
                    bold = false;
                    italic = false;
                    monospace = false;
                    strikethrough = false;
                }
            }

            lastIdx = matcher.end();
        }

        if (lastIdx < input.length()) {
            String text = input.substring(lastIdx);
            Message part = Message.raw(text);
            if (currentColor != null) part.color(currentColor);
            if (bold) part.bold(true);
            if (italic) part.italic(true);
            if (monospace) part.monospace(true);
            if (strikethrough) part.strikethrough(true);
            parts.add(part);
        }

        if (parts.isEmpty()) {
            return Message.empty();
        }
        if (parts.size() == 1) {
            return parts.get(0);
        }
        return Message.join(parts.toArray(new Message[0]));
    }
}
