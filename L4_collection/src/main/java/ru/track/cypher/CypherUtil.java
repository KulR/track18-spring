package ru.track.cypher;

import java.util.*;

import org.jetbrains.annotations.NotNull;

/**
 * Вспомогательные методы шифрования/дешифрования
 */
public class CypherUtil {

    public static final String SYMBOLS = "abcdefghijklmnopqrstuvwxyz";

    /**
     * Генерирует таблицу подстановки - то есть каждой буква алфавита ставится в соответствие другая буква
     * Не должно быть пересечений (a -> x, b -> x). Маппинг уникальный
     *
     * @return таблицу подстановки шифра
     */
    @NotNull
    public static Map<Character, Character> generateCypher() {
        Map<Character, Character> cypher = new TreeMap<Character, Character>();
        ArrayList<Character> from = new ArrayList<Character>(26);
        ArrayList<Character> to = new ArrayList<Character>(26);
        for (int i = 0; i < SYMBOLS.length(); i++) {
            from.add(SYMBOLS.charAt(i));
            to.add(SYMBOLS.charAt(i));
        }
        Collections.shuffle(to);
        for (int i = 0; i < from.size(); i++) {
            cypher.put(from.get(i), to.get(i));
        }
        return cypher;
    }
}
