package ru.track.cypher;

import java.util.*;

import org.jetbrains.annotations.NotNull;


public class Decoder {

    // Расстояние между A-Z -> a-z
    public static final int SYMBOL_DIST = 32;

    private Map<Character, Character> cypher;

    /**
     * Конструктор строит гистограммы открытого домена и зашифрованного домена
     * Сортирует буквы в соответствие с их частотой и создает обратный шифр Map<Character, Character>
     *
     * @param domain - текст по кторому строим гистограмму языка
     */
    public Decoder(@NotNull String domain, @NotNull String encryptedDomain) {
        Map<Character, Integer> domainHist = createHist(domain);
        Map<Character, Integer> encryptedDomainHist = createHist(encryptedDomain);

        cypher = new LinkedHashMap<>();
        Iterator<Map.Entry<Character, Integer>> entries_dom = domainHist.entrySet().iterator();
        Iterator<Map.Entry<Character, Integer>> entries_enc = encryptedDomainHist.entrySet().iterator();
        while (entries_dom.hasNext()){
            Map.Entry<Character,Integer> entry_dom = entries_dom.next();
            Map.Entry<Character,Integer> entry_enc = entries_enc.next();
            cypher.put(entry_enc.getKey(), entry_dom.getKey());
        }
    }

    public Map<Character, Character> getCypher() {
        return cypher;
    }

    /**
     * Применяет построенный шифр для расшифровки текста
     *
     * @param encoded зашифрованный текст
     * @return расшифровка
     */
    @NotNull
    public String decode(@NotNull String encoded) {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < encoded.length(); i++){
            if(cypher.get(encoded.charAt(i)) != null){
                builder.append(cypher.get(encoded.charAt(i)));
            }
            else
                builder.append(encoded.charAt(i));
        }
        return builder.toString();
    }

    /**
     * Считывает входной текст посимвольно, буквы сохраняет в мапу.
     * Большие буквы приводит к маленьким
     *
     *
     * @param text - входной текст
     * @return - мапа с частотой вхождения каждой буквы (Ключ - буква в нижнем регистре)
     * Мапа отсортирована по частоте. При итерировании на первой позиции наиболее частая буква
     */
    @NotNull
    Map<Character, Integer> createHist(@NotNull String text) {
        Map<Character, Integer> result_temp = new HashMap<Character, Integer>();
        String temp = text.toLowerCase();
        for(int i = 0; i < text.length(); i++){
            if(temp.charAt(i) < 'a' || temp.charAt(i) > 'z')
                continue;
            int char_amount = result_temp.get(temp.charAt(i)) == null ? 1 : (result_temp.get(temp.charAt(i)) + 1);
            result_temp.put(temp.charAt(i), char_amount);
        }
        //sort map
        List<Map.Entry<Character, Integer>> entries = new ArrayList<>(result_temp.entrySet());
        entries.sort(new Comparator<Map.Entry<Character, Integer>>() {
            @Override
            public int compare(Map.Entry<Character, Integer> o1, Map.Entry<Character, Integer> o2) {
                return o2.getValue() - o1.getValue();
            }
        });
        Map<Character, Integer> result = new LinkedHashMap<>();
        for(Map.Entry<Character, Integer> entry: entries){
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
