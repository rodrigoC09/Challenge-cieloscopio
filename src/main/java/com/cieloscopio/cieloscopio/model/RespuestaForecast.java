package com.cieloscopio.cieloscopio.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public record RespuestaForecast(
        @SerializedName("list") java.util.List<DatosIteracion> listaClimas,
        @SerializedName("city") DatosCiudad ciudad
) {
    //campos de la API que deseamos mostrar
    public record DatosIteracion(
            @SerializedName("dt_txt") String fechaHora,
            @SerializedName("main") DatosPrincipales principales,
            @SerializedName("weather") List<DatosCondicion> condiciones,
            @SerializedName("pop") Double probabilidadPrecipitacion,
            @SerializedName("rain") DatosLluvia lluvia,
            @SerializedName("wind") DatosViento viento
            ) {
    }

    public record DatosViento(
            @SerializedName("speed") Double velocidad
    ){}

    public record DatosCondicion(
            @SerializedName("description") String descripcion
    ) { }
    public record DatosLluvia(
            @SerializedName("3h") Double volumen
    ){}

    public record DatosCiudad(
            @SerializedName("name") String nombre
    ) {
    }
    public record DatosPrincipales(
            @SerializedName("temp") Double temperatura,
            @SerializedName("temp_max") Double temperaturaMaxima,
            @SerializedName("temp_min") Double temperaturaMinima,
            @SerializedName("feels_like") Double sensacionTermica,
            @SerializedName("pressure") Integer presion,
            @SerializedName("humidity") Integer humedad
    ) {
    }
}
