package com.cieloscopio.cieloscopio.principal;

import com.cieloscopio.cieloscopio.model.RespuestaForecast;

public class VisualizarConsulta {
    public static void mostrarResumen24Horas(RespuestaForecast datos) {
        var bloques24h = datos.listaClimas().stream().limit(8).toList();
        var actual = bloques24h.get(0);

        double tempActual = actual.principales().temperatura();
        double min = bloques24h.stream().mapToDouble(c -> c.principales().temperatura()).min().orElse(tempActual);
        double max = bloques24h.stream().mapToDouble(c -> c.principales().temperatura()).max().orElse(tempActual);
        String condicion = extraerCondicion(actual);
        double lluvia = actual.lluvia() != null ? actual.lluvia().volumen() : 0.0;

        System.out.println("\n------------------------------------");
        System.out.println("PRONÓSTICO PRÓXIMAS 24 HORAS: " + datos.ciudad().nombre().toUpperCase());
        System.out.println("------------------------------------");
        imprimirBloque(actual, condicion, min, max, lluvia);
    }

    public static void mostrarPronostico3Dias(RespuestaForecast datos) {
        System.out.println("\n------------------------------------");
        System.out.println("PRONÓSTICO PRÓXIMOS 3 DÍAS (Mediodía)");
        System.out.println("------------------------------------");

        datos.listaClimas().stream()
                .filter(i -> i.fechaHora().contains("12:00:00"))
                .limit(3)
                .forEach(dia -> imprimirBloque(dia, extraerCondicion(dia), null, null, null));
    }

    public static void mostrarExtrasAccu(RespuestaForecast.AccuForecast datosAccu) {
        if (datosAccu == null || datosAccu.pronosticos() == null || datosAccu.pronosticos().isEmpty()) return;

        var hoy = datosAccu.pronosticos().get(0);
        System.out.println("\n========= DETALLES ADICIONALES (AccuWeather) =========");

        hoy.aire().stream()
                .filter(a -> a.nombre().equalsIgnoreCase("UVIndex"))
                .findFirst()
                .ifPresent(uv -> System.out.println(String.format("ÍNDICE UV:   %d (%s)", uv.valor(), uv.categoria())));

        System.out.println(String.format("FASE LUNAR:  %s", hoy.luna().nombreFase()));
        System.out.println(String.format("ILUMINACIÓN: %d%%", hoy.luna().edad()));
        System.out.println("=====================================================\n");
    }

    public static void mostrarReporteCompleto(RespuestaForecast datosOW, RespuestaForecast.AccuForecast datosAccu) {
        mostrarPronostico3Dias(datosOW);
        mostrarExtrasAccu(datosAccu);
    }

    private static void imprimirBloque(RespuestaForecast.DatosIteracion dia, String cond, Double min, Double max, Double lluvia) {
        System.out.println("FECHA: " + dia.fechaHora());
        System.out.println(String.format("ESTADO:             %s", cond.toUpperCase()));
        System.out.println(String.format("TEMPERATURA:        %.1f°C", dia.principales().temperatura()));
        if (min != null) System.out.println(String.format("MÍN/MÁX 24H:        %.1f°C / %.1f°C", min, max));
        System.out.println(String.format("HUMEDAD:            %d%%", dia.principales().humedad()));
        System.out.println(String.format("VIENTO:             %.1f km/h", dia.viento().velocidad() * 3.6));
        System.out.println("------------------------------------");
    }

    private static String extraerCondicion(RespuestaForecast.DatosIteracion item) {
        return (item.condiciones() != null && !item.condiciones().isEmpty())
                ? item.condiciones().get(0).descripcion() : "No disponible";
    }
}
