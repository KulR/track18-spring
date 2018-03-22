package ru.track.json;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * сериализатор в json
 */
public class JsonWriter {
    // В зависимости от типа объекта вызывает соответствующий способ сериализации
    public static String toJson(@Nullable Object object) {
        if (object == null) {
            return "null";
        }

        Class clazz = object.getClass();

        if (clazz.equals(String.class)
                || clazz.equals(Character.class)
                || clazz.isEnum()
                ) {
            return String.format("\"%s\"", object);
        }

        if (object instanceof Boolean || object instanceof Number) {
            return object.toString();
        }

        if (clazz.isArray()) {
            return toJsonArray(object);
        }

        if (object instanceof Collection) {
            return toJsonCollection(object);
        }

        if (object instanceof Map) {
            return toJsonMap(object);
        }

        return toJsonObject(object);
    }

    /**
     * Используется вспомогательный класс {@link Array}, чтобы работать с object instanceof Array
     * <p>
     * То есть чтобы получить i-й элемент массива, нужно вызвать {@link Array#get(Object, int)}, где i - это число от 0 до {@link Array#getLength(Object)}
     *
     * @param object - который Class.isArray()
     * @return строковое представление массива: [item1, item2, ...]
     */
    @NotNull
    private static String toJsonArray(@NotNull Object object) {
        int length = Array.getLength(object);
        StringBuilder result = new StringBuilder();
        result.append("[");
        for(int i = 0; i < length - 1; i++){
            result.append(JsonWriter.toJson(Array.get(object, i)));
            result.append(",");
        }
        result.append(JsonWriter.toJson(Array.get(object, length - 1)));
        result.append("]");

        // TODO: implement!

        return result.toString();
    }

    /**
     * В 1 шаг приводится к Collection
     */
    @NotNull
    private static String toJsonCollection(@NotNull Object object) {
        Collection collection = (Collection) object;
        return toJsonArray(collection.toArray());
    }

    /**
     * Сконвертить мап в json. Формат:
     * {key:value, key:value,..}
     * <p>
     * На входе мы проверили, что это Map, можно просто кастовать Map map = (Map) object;
     */
    @NotNull
    private static String toJsonMap(@NotNull Object object) {
        Map source = (Map) object;
        Map<String, String> map_result = new LinkedHashMap<>();
        for (Object o : source.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            Class clazz = entry.getKey().getClass();
            if(clazz.equals(String.class)){
                String temp = JsonWriter.toJson(entry.getKey());
                temp = temp.substring(1, temp.length() - 1);
                map_result.put(temp, JsonWriter.toJson(entry.getValue()));
            } else
            map_result.put(JsonWriter.toJson(entry.getKey()), JsonWriter.toJson(entry.getValue()));
        }
            return formatObject(map_result);
        // Можно воспользоваться этим методом, если сохранить все поля в новой мапе уже в строковом представлении
//        return formatObject(stringMap);
    }

    /**
     * 1) Чтобы распечатать объект, нужно знать его внутреннюю структуру, для этого нужно получить его Class-объект:
     * {@link Class} с помощью {@link Object#getClass()}
     * <p>
     * Получить поля класса можно с помощью {@link Class#getDeclaredFields()}
     * Приватные поля недоступны, нужно изменить в рантайм их accessibility: {@link Field#setAccessible(boolean)}
     * <p>
     * 2) Вторая часть задачи: {@link JsonNullable} и {@link SerializedTo}
     * Нужно проверить, что у класса/поля есть аннотация
     * <p>
     * {@link Class#getAnnotation(Class)} / {@link Field#getAnnotation(Class)}
     * и в зависимости от этого изменить поведение
     * <p>
     * NOTE: Удобно сложить все поля объекта в Map<String, String> то етсь {имя поля -> значение поля в json}
     * и воспользоваться методом {@link #formatObject(Map)}
     */
    @NotNull
    private static String toJsonObject(@NotNull Object object) {
        Class clazz = object.getClass();
        Map<String, String> map_result = new LinkedHashMap<>();
        Field[] fields = clazz.getDeclaredFields();
        Field.setAccessible(fields, true);
        for(Field field: fields) {
            try {
                if(field.getAnnotation(SerializedTo.class) != null){
                    map_result.put(field.getAnnotation(SerializedTo.class).value(), JsonWriter.toJson(field.get(object)));
                }
                else {
                    if ((field.get(object) != null) && field.getAnnotation(JsonNullable.class) != null) {
                        map_result.put(field.getName(), JsonWriter.toJson(field.get(object)));
                    } else
                        map_result.put(field.getName(), JsonWriter.toJson(field.get(object)));
                }
            } catch (IllegalAccessException ignored){}
        }
        // TODO: implement!

        return formatObject(map_result);
    }

    /**
     * Вспомогательный метод для форматирования содержимого Map<K, V>
     *
     * @param map
     * @return "{key:value, key:value,..}"
     */
    @NotNull
    private static String formatObject(@NotNull Map<String, String> map) {
        String r = String.join(",", map.entrySet().stream()
                .map(e -> String.format("\"%s\":%s", e.getKey(), e.getValue()))
                .collect(Collectors.toList())
        );

        return String.format("{%s}", r);
    }
}
