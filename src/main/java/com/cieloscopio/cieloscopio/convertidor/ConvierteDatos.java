package com.cieloscopio.cieloscopio.convertidor;

import com.google.gson.Gson;

public class ConvierteDatos {
    private final Gson gson = new Gson();

    public <T> T obtenerDatos(String json, Class<T> clase){
        try{
            return gson.fromJson(json, clase);
        }catch (Exception e){
            throw new RuntimeException("Error al convertir Json con Gnson"+e.getMessage());
        }

    }
}
