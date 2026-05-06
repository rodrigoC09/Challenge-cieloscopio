package com.cieloscopio.cieloscopio.principal;

import com.cieloscopio.cieloscopio.client.ApiClima.weathermapApi;
import com.cieloscopio.cieloscopio.convertidor.ConvierteDatos;
import com.cieloscopio.cieloscopio.exceptions.CiudadNoEncontradaException;
import com.cieloscopio.cieloscopio.exceptions.ErrorConsultaApiException;
import com.cieloscopio.cieloscopio.model.DatosGeograficos;
import com.cieloscopio.cieloscopio.model.ModoReporte;
import com.cieloscopio.cieloscopio.model.RegistroConsulta;
import com.cieloscopio.cieloscopio.model.RespuestaForecast;
import com.cieloscopio.cieloscopio.service.HistorialConsultas;

import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class Principal {
    private final weathermapApi service = new weathermapApi();
    private final Scanner teclado = new Scanner(System.in);
    private HistorialConsultas historial = new HistorialConsultas();

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
					7. Pronostico para los 3 dias siguientes
					8. Historial de búsqueda
					9. Salir
					""");
            String opcion =teclado.nextLine();

            switch (opcion) {
                case "1" -> ejecutarConsultaClimatica("Ciudad de Mexico", ModoReporte.Resumen_24H);
                case "2" -> ejecutarConsultaClimatica("Buenos Aires", ModoReporte.Resumen_24H);
                case "3" -> ejecutarConsultaClimatica("Bogota", ModoReporte.Resumen_24H);
                case "4" -> ejecutarConsultaClimatica("Lima", ModoReporte.Resumen_24H);
                case "5" -> ejecutarConsultaClimatica("Santiago", ModoReporte.Resumen_24H);
                case "6" ->{
                    System.out.print("Introduce el nombre de la ciudad: ");
                    String otraCiudad = teclado.nextLine();
                    if (!otraCiudad.isEmpty() || otraCiudad.matches(".*[a-zA-Z0-9].*")) {
                        ejecutarConsultaClimatica(otraCiudad, ModoReporte.Resumen_24H);
                    }else{
                        System.out.println("Nombre de ciudad no valido, intenta nuevamente");
                    }
                }
                case "7" ->{
                    System.out.println("""
					Escoje una ciudad para obtener los datos meteorologicos para los proximos 3 dias 
					1. Ciudad de Mexico
					2. Buenos aires
					3. Bogota
					4. Lima
					5. Santiago
					6. Quiero consultar otra cuidad
					7. Salir
					""");
                    String opcion2 =teclado.nextLine();

                    switch (opcion2) {
                        case "1" -> ejecutarConsultaClimatica("Ciudad de Mexico", ModoReporte.Pronostico_3Dias);
                        case "2" -> ejecutarConsultaClimatica("Buenos Aires", ModoReporte.Pronostico_3Dias);
                        case "3" -> ejecutarConsultaClimatica("Bogota", ModoReporte.Pronostico_3Dias);
                        case "4" -> ejecutarConsultaClimatica("Lima", ModoReporte.Pronostico_3Dias);
                        case "5" -> ejecutarConsultaClimatica("Santiago", ModoReporte.Pronostico_3Dias);
                        case "6" ->{
                            System.out.print("Introduce el nombre de la ciudad: ");
                            String otraCiudad = teclado.nextLine();
                            if (!otraCiudad.isEmpty() || otraCiudad.matches(".*[a-zA-Z0-9].*")) {
                                ejecutarConsultaClimatica(otraCiudad, ModoReporte.Pronostico_3Dias);
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
                case "8"->{
                    var lista = historial.obtenerRegistros();
                    if (lista.isEmpty()) {
                        System.out.println("\n[!] El historial esta vacío");
                    }else{
                        System.out.println("\n/// CIUDADES CONSULTADAS RECIENTEMENTE ///");
//                        for (int i =0; i< lista.size(); i++){
//                            System.out.println((i+1)+". "+lista.get(i));
//                        }
                        lista.forEach(System.out::println);
                        System.out.println("/////////////////////////////");
                    }
                }
                case "9"-> {
                    System.out.println("Cerrando aplicacion");
                    salir = true;

                }
                default ->{
                    System.out.println("Opcion no valida, intenta con los numero que aparecen en pantalla");
                }
            }
        }
    }

    public void consultarYMostrar3(String nombreCiudad) {
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
                    System.out.println("PRONÓSTICO PRÓXIMOS 3 DÍAS : " + datos.ciudad().nombre().toUpperCase());
                    System.out.println("------------------------------------");
                    //Agruparemos los datos por fecha utilizando lamdas para recorrerlos
                    datos.listaClimas().stream()
                                    .filter(i-> i.fechaHora().contains("12:00:00"))
                                            .limit(3)//proximos 3 dias
                                                    .forEach(dia->{
                                                        String fecha = dia.fechaHora().split(" ")[0];
                                                        String estado = dia.condiciones().get(0).descripcion().toUpperCase();
                                                        double temp = dia.principales().temperatura();
                                                        double sensacionn = dia.principales().sensacionTermica();
                                                        double vientoo = dia.viento().velocidad() * 3.6;
                                                        int humedadd = dia.principales().humedad();
                                                        int presionn = dia.principales().presion();

                                                        System.out.println("FECHA: "+fecha);
                                                        System.out.println(String.format("ESTADO:             %s", estado));
                                                        System.out.println(String.format("TEMPERATURA: %.1f°C", temp));



                                                        System.out.println(String.format("PORCENTAJE DE HUMEDAD: %d%%", humedadd));
                                                        System.out.println(String.format("SENSACION TERMICA: %.1fC", sensacionn));
                                                        System.out.println(String.format("VIENTO: %.1f km/h", vientoo));
                                                        System.out.println(String.format("PRESIÓN ATMOSFERICA: %d hPa", presionn));
                                                        //Pendiente
                                                        System.out.println(String.format("INDICE UV: "));
                                                        System.out.println(String.format("FASE LUNAR: "));
                                                        System.out.println("------------------------------------");

                                                    });



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

    public void consultarYMostrar(String nombreCiudad){
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
                    //dfgdfg
                    var actual = datos.listaClimas().get(0);
                    double sensacion = actual.principales().sensacionTermica();
                    int humedad = actual.principales().humedad();
                    int presion = actual.principales().presion();
                    double viento = actual.viento().velocidad() * 3.6;

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
                    System.out.println("PRONÓSTICO PRÓXIMAS 24 HORAS : " + datos.ciudad().nombre().toUpperCase());
                    System.out.println("------------------------------------");

                    System.out.println(String.format("ESTADO:             %s", condicion.toUpperCase()));
                    System.out.println(String.format("TEMPERATURA ACTUAL: %.1f°C", tempActual));
                    System.out.println(String.format("MÍNIMA:     %.1f°C", min24h));
                    System.out.println(String.format("MÁXIMA:     %.1f°C", max24h));
                    System.out.println(String.format("PROBABILIDAD DE PRECIPITACIONES: %.0f%%", probabilidad));
                    System.out.println(String.format("VOLUMEN DE LLUVIA: %.1f mm", volumenLluvia));
                    System.out.println(String.format("PORCENTAJE DE HUMEDAD: %d%%", humedad));
                    System.out.println(String.format("SENSACION TERMICA: %.1fC", sensacion));
                    System.out.println(String.format("VIENTO: %.1f km/h", viento));
                    System.out.println(String.format("PRESIÓN ATMOSFERICA: %d hPa", presion));
                    //Pendiente
                    System.out.println(String.format("INDICE UV: "));
                    System.out.println(String.format("FASE LUNAR: "));
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

    public void ejecutarConsultaClimatica(String nombreCiudad, ModoReporte modo) {
        ConvierteDatos conversor = new ConvierteDatos();

        service.buscarCoordenadas(nombreCiudad)
                .thenCompose(geoJson -> procesarCoordenadas(geoJson, conversor))
                .thenAccept(jsonForecast -> {
                    RespuestaForecast datos = conversor.obtenerDatos(jsonForecast, RespuestaForecast.class);

                    //historial.agregarCiudad(datos.ciudad().nombre());

                    RegistroConsulta nuevoRegistro = new RegistroConsulta(
                        datos.ciudad().nombre(),
                                LocalDateTime.now(),
                                modo
                    );
                    historial.agregarRegistro(nuevoRegistro);

                    switch (modo) {
                        case Resumen_24H -> mostrarResumen24Horas(datos);
                        case Pronostico_3Dias -> mostrarPronostico3Dias(datos);

                    }

                })
                .exceptionally(this::manejarErrores)
                .join();
    }

    private void mostrarPronostico3Dias(RespuestaForecast datos) {
        System.out.println("\n------------------------------------");
        System.out.println("PRONÓSTICO PRÓXIMOS 3 DÍAS (Mediodía)");
        System.out.println("------------------------------------");

        datos.listaClimas().stream()
                .filter(i -> i.fechaHora().contains("12:00:00"))
                .limit(3)
                .forEach(dia -> {
                    String condicion = extraerCondicion(dia);
                    imprimirMenu(dia, condicion, null, null, null);
                });
    }

    private void mostrarResumen24Horas(RespuestaForecast datos) {
        var bloques24h = datos.listaClimas().stream().limit(8).toList();
        var actual = bloques24h.get(0);

        // Cálculos rápidos
        double tempActual = actual.principales().temperatura();
        double min = bloques24h.stream().mapToDouble(c -> c.principales().temperatura()).min().orElse(tempActual);
        double max = bloques24h.stream().mapToDouble(c -> c.principales().temperatura()).max().orElse(tempActual);
        String condicion = extraerCondicion(actual);
        double volumenLluvia = actual.lluvia() != null ? actual.lluvia().volumen() : 0.0;

        System.out.println("\n------------------------------------");
        System.out.println("PRONÓSTICO PRÓXIMAS 24 HORAS: " + datos.ciudad().nombre().toUpperCase());
        System.out.println("------------------------------------");
        imprimirMenu(actual, condicion, min, max, volumenLluvia);
    }

    public CompletableFuture<String> procesarCoordenadas(String geoJson, ConvierteDatos conversor) {
        DatosGeograficos[] ciudades = conversor.obtenerDatos(geoJson, DatosGeograficos[].class);

        if (ciudades == null || ciudades.length == 0) {
            return CompletableFuture.failedFuture(new CiudadNoEncontradaException("La ciudad no existe en los registros"));

        }
        return service.obtenerPronostico(ciudades[0].lat(), ciudades[0].lon());
    }

    private Void manejarErrores(Throwable ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        String prefijo = (cause instanceof CiudadNoEncontradaException) ? "[!] Búsqueda" : "[!] Técnico";
        System.err.println("\n" + prefijo + ": " + cause.getMessage());
        return null;
    }
    private void imprimirMenu(RespuestaForecast.DatosIteracion dia, String cond, Double min, Double max, Double lluvia) {
        System.out.println("FECHA: " + dia.fechaHora());
        System.out.println(String.format("ESTADO:             %s", cond.toUpperCase()));
        System.out.println(String.format("TEMPERATURA:        %.1f°C", dia.principales().temperatura()));
        if (min != null) System.out.println(String.format("MÍN/MÁX 24H:        %.1f°C / %.1f°C", min, max));
        System.out.println(String.format("SENSACIÓN TÉRMICA:  %.1f°C", dia.principales().sensacionTermica()));
        System.out.println(String.format("HUMEDAD:            %d%%", dia.principales().humedad()));
        System.out.println(String.format("VIENTO:             %.1f km/h", dia.viento().velocidad() * 3.6));
        System.out.println("------------------------------------");
    }
    private String extraerCondicion(RespuestaForecast.DatosIteracion item) {
        if (item.condiciones() != null && !item.condiciones().isEmpty()) {
            return item.condiciones().get(0).descripcion();
        }
        return "No disponible";
    }

}
