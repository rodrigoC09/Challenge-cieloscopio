package com.cieloscopio.cieloscopio.principal;

import com.cieloscopio.cieloscopio.client.ApiClima.MeteorologiaService;
import com.cieloscopio.cieloscopio.convertidor.ConvierteDatos;
import com.cieloscopio.cieloscopio.exceptions.CiudadNoEncontradaException;
import com.cieloscopio.cieloscopio.model.DatosGeograficos;
import com.cieloscopio.cieloscopio.model.ModoReporte;
import com.cieloscopio.cieloscopio.model.RegistroConsulta;
import com.cieloscopio.cieloscopio.model.RespuestaForecast;
import com.cieloscopio.cieloscopio.service.HistorialConsultas;

import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class Principal {
    private final MeteorologiaService service = new MeteorologiaService();
    private final Scanner teclado = new Scanner(System.in);
    private HistorialConsultas historial = new HistorialConsultas();
    private ConvierteDatos conversor = new ConvierteDatos();

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
    public void ejecutarConsultaClimatica(String nombreCiudad, ModoReporte modo) {

        System.out.println("\n[i] Consultando: " + nombreCiudad);
        //ConvierteDatos conversor = new ConvierteDatos();

        service.buscarCoordenadas(nombreCiudad)
                .thenCompose(geoJson -> procesarCoordenadas(geoJson, conversor))
                .thenCompose(jsonOW -> {
                    RespuestaForecast datosOW = conversor.obtenerDatos(jsonOW, RespuestaForecast.class);

                    // Registro de auditoría con java.time
                    historial.agregarRegistro(new RegistroConsulta(datosOW.ciudad().nombre(), LocalDateTime.now(), modo));

                    // El switch decide si termina aquí o sigue hacia AccuWeather
                    switch (modo) {
                        case Pronostico_3Dias -> {
                            // Retornamos una NUEVA cadena asíncrona que se integra a la principal
                            return service.obtenerLocationKey(nombreCiudad)
                                    .thenCompose(jsonLoc -> {
                                        var locs = conversor.obtenerDatos(jsonLoc, RespuestaForecast.AccuLocation[].class);
                                        return (locs.length > 0) ? service.obtenerDetalleAccu(locs[0].llave()) : CompletableFuture.completedFuture(null);
                                    })
                                    .thenAccept(jsonAccu -> {
                                        RespuestaForecast.AccuForecast datosAccu = (jsonAccu != null)
                                                ? conversor.obtenerDatos(jsonAccu, RespuestaForecast.AccuForecast.class) : null;

                                        // Usamos la clase Visualizer
                                        VisualizarConsulta.mostrarReporteCompleto(datosOW, datosAccu);
                                    });
                        }
                        case Resumen_24H -> {
                            VisualizarConsulta.mostrarResumen24Horas(datosOW);
                            return CompletableFuture.completedFuture(null);
                        }
                        default -> {
                            return CompletableFuture.completedFuture(null);
                        }
                    }
                })
                .exceptionally(this::manejarErrores)
                .join();
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



}
