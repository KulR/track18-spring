package ru.track;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;


/**
 * Задание 1: Реализовать два метода
 *
 * Формат файла: текстовый, на каждой его строке есть (или/или)
 * - целое число (int)
 * - текстовая строка
 * - пустая строка (пробелы)
 *
 * Числа складываем, строки соединяем через пробел, пустые строки пропускаем
 *
 *
 * Пример файла - words.txt в корне проекта
 *
 * ******************************************************************************************
 *  Пожалуйста, не меняйте сигнатуры методов! (название, аргументы, возвращаемое значение)
 *
 *  Можно дописывать новый код - вспомогательные методы, конструкторы, поля
 *
 * ******************************************************************************************
 *
 */
public class CountWords {

    String skipWord;

    public CountWords(String skipWord) {
        this.skipWord = skipWord;
    }

    /**
     * Метод на вход принимает объект File, изначально сумма = 0
     * Нужно пройти по всем строкам файла, и если в строке стоит целое число,
     * то надо добавить это число к сумме
     * @param file - файл с данными
     * @return - целое число - сумма всех чисел из файла
     */
    public long countNumbers(File file) throws Exception {
        long sum = 0;
        if(file.exists() && file.isFile()) {
            BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line = bf.readLine();
            while (line != null) {
                if(Isword(line)){
                    Long number = Long.parseLong(line);
                    sum += number;
                }
                line = bf.readLine();
            }
        }
        return sum;
    }


    /**
     * Метод на вход принимает объект File, изначально результат= ""
     * Нужно пройти по всем строкам файла, и если в строка не пустая и не число
     * то надо присоединить ее к результату через пробел
     * @param file - файл с данными
     * @return - результирующая строка
     */
    public String concatWords(File file) throws Exception {
        String result = "";
        StringBuilder builder = new StringBuilder();
        if(file.exists() && file.isFile()) {
            BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line = bf.readLine();
            while (line != null) {
                if (!Isword(line) && !line.equals(skipWord)) {
                    builder.append(line);
                    builder.append(" ");
                }
                line = bf.readLine();
            }
        }
        result = builder.toString();
        return result;
    }

    public static boolean Isword(String line){
        for (int i = 0; i < line.length(); i++){
            if(line.charAt(i) > '9' || line.charAt(i) < '0'){
                return false;
            }
        }
        return true;
    }
}