/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyf.proyectofinal.util;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author ludwi
 */

public class GsonUtil {

    private static final DateTimeFormatter FMT_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter FMT_DT   = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .serializeNulls()
            .create();

    public static Gson getGson()                           { return gson; }
    public static String toJson(Object obj)                { return gson.toJson(obj); }
    public static <T> T fromJson(String j, Class<T> c)    { return gson.fromJson(j, c); }

    private static class LocalDateAdapter
            implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
        public JsonElement serialize(LocalDate src, Type t, JsonSerializationContext c) {
            return new JsonPrimitive(src.format(FMT_DATE));
        }
        public LocalDate deserialize(JsonElement j, Type t, JsonDeserializationContext c) {
            return LocalDate.parse(j.getAsString(), FMT_DATE);
        }
    }

    private static class LocalDateTimeAdapter
            implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        public JsonElement serialize(LocalDateTime src, Type t, JsonSerializationContext c) {
            return new JsonPrimitive(src.format(FMT_DT));
        }
        public LocalDateTime deserialize(JsonElement j, Type t, JsonDeserializationContext c) {
            return LocalDateTime.parse(j.getAsString(), FMT_DT);
        }
    }
}
