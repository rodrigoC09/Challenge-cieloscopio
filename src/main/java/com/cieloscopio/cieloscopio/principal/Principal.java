package com.cieloscopio.cieloscopio.principal;

import com.cieloscopio.cieloscopio.client.ApiClima.weathermapApi;
import com.cieloscopio.cieloscopio.convertidor.ConvierteDatos;
import com.cieloscopio.cieloscopio.exceptions.CiudadNoEncontradaException;
import com.cieloscopio.cieloscopio.exceptions.ErrorConsultaApiException;
import com.cieloscopio.cieloscopio.model.DatosGeograficos;
import com.cieloscopio.cieloscopio.model.RespuestaForecast;

import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class Principal {
    private final weathermapApi service = new weathermapApi();
    private final Scanner teclado = new Scanner(System.in);

    public void menuCieloscopio() {
        boolean salir = false;

        System.out.println("--- BIENVENIDO A CIELOSCOPIO ---");

        while (!salir){
            System.out.println("""
					Escoje una ciudad para obtener los datos meteorologicos 
					1. Ciudad de Mexico
					2. Buenos aires
					3. Bogota
					4. Lima
					5. Santiago
					6. Quiero consultar otra cuidad
					7. Salir
					""");
            String opcion =teclado.nextLine();

            switch (opcion) {
                case "1" -> consultarYMostrar("Ciudad de Mexico");
                case "2" -> consultarYMostrar("Buenos Aires");
                case "3" -> consultarYMostrar("Bogota");
                case "4" -> consultarYMostrar("Lima");
                case "5" -> consultarYMostrar("Santiago");
                case "6" ->{
                    System.out.print("Introduce el nombre de la ciudad: ");
                    String otraCiudad = teclado.nextLine();
                    if (!otraCiudad.isEmpty() || otraCiudad.matches(".*[a-zA-Z0-9].*")) {
                        consultarYMostrar(otraCiudad);
                    }else{
                        System.out.println("Nombre de ciudad no valido, intenta nuevamente");
                    }
                }
                case "7"-> {
                    System.out.println("Cerrando aplicacion");
                    salir = true;

                }
                default ->{
                    System.out.println("Opcion no valida, intenta con los numero que aparecen en pantalla");
                }
            }
        }
    }

    public void consultarYMostrar(String nombreCiudad) {
        ConvierteDatos conversor = new ConvierteDatos();

        service.buscarCoordenadas(nombreCiudad)
                .thenCompose(geoJson -> {
                    DatosGeograficos[] ciudades = conversor.obtenerDatos(geoJson, DatosGeograficos[].class);

                    if (ciudades.length == 0 || ciudades ==null) {

                        System.out.println("Verificar que la ciudad exista");

                        return CompletableFuture.failedFuture(new Exception("Ciudad no encontrada"));
                    }
                    double lat = ciudades[0].lat();
                    double lon = ciudades[0].lon();

                    //Pronóstico (forecast)
                    return service.obtenerPronostico(lat, lon);
                })
                .thenAccept(jsonForecast -> {

                    var datos = conversor.obtenerDatos(jsonForecast, RespuestaForecast.class);

                    // OpenWeather entrega bloques de 3 horas. 24 horas / 3 = 8 bloques.
                    var proximas24Horas = datos.listaClimas().stream()
                            .limit(8)
                            .toList();

                    //Temperatura actual
                    double tempActual = proximas24Horas.get(0).principales().temperatura();

                    //Temperatura mínima
                    double min24h = proximas24Horas.stream()
                            .mapToDouble(c -> c.principales().temperatura())
                            .min()
                            .orElse(tempActual);

                    //Temperatura máxima
                    double max24h = proximas24Horas.stream()
                            .mapToDouble(c -> c.principales().temperatura())
                            .max()
                            .orElse(tempActual);

                    //Condicion climatica
                    String condicion = "No disponible";
                    if (proximas24Horas.get(0).condiciones() !=null && !proximas24Horas.get(0).condiciones().isEmpty()) {
                        String descri = proximas24Horas.get(0).condiciones().get(0).descripcion();
                        if (descri != null) {
                            condicion = descri;
                        }
                    }

                    //Precipitación y volumen
                    double probabilidad =proximas24Horas.get(0).probabilidadPrecipitacion() *100;
                    //sin lluvia(null)
                    double volumenLluvia= 0.0;
                    var datosLluvia = proximas24Horas.get(0).lluvia();
                    if (datosLluvia != null && datosLluvia.volumen() != null) {
                        volumenLluvia = datosLluvia.volumen();
                    }

                    System.out.println("\n------------------------------------");
                    System.out.println("TEMPERATURA EN LAS PRÓXIMAS 24 HORAS: " + datos.ciudad().nombre().toUpperCase());
                    System.out.println("------------------------------------");
                    System.out.println(String.format("ESTADO:             %s", condicion.toUpperCase()));
                    System.out.println(String.format("TEMPERATURA ACTUAL: %.1f°C", tempActual));
                    System.out.println(String.format("MÍNIMA:     %.1f°C", min24h));
                    System.out.println(String.format("MÁXIMA:     %.1f°C", max24h));
                    System.out.println(String.format("PROBABILIDAD DE PRECIPITACIONES: %.0f%%", probabilidad));
                    System.out.println(String.format("VOLUMEN DE LLUVIA: %.1f mm", volumenLluvia));
                    System.out.println("------------------------------------");

                })
                .exceptionally(ex -> {
                    //usaremos CompletionException, para usar getCause()
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;

                    if (cause instanceof CiudadNoEncontradaException) {
                        System.err.println("\n[!] Error en la búsqueda: " + cause.getMessage());
                    } else if (cause instanceof ErrorConsultaApiException) {
                        System.out.println("\n[!] Error técnico: "+cause.getMessage());
                    }else{
                        System.out.println("\n[!] Error Inesperado: "+cause.getMessage());
                    }


                    return null;
                })
                .join();
    }



}
