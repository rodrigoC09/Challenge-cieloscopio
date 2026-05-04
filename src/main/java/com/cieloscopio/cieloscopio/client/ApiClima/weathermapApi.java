package com.cieloscopio.cieloscopio.client.ApiClima;

import com.cieloscopio.cieloscopio.exceptions.ErrorConsultaApiException;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class weathermapApi {
    private final String API_KEY;
    private final HttpClient httpClient;
    //temperatura actual

    //Url geolocalizacion
    private static final String GEO_URL="http://api.openweathermap.org/geo/1.0/direct";
    //Url pronostico de 5 dias
    private static final String FORECAST_URL = "https://api.openweathermap.org/data/2.5/forecast";



    public weathermapApi() {
        this.API_KEY = System.getenv("API_KEY_weathermap");
        if (this.API_KEY == null || this.API_KEY.isBlank()) {
            throw new IllegalStateException("ERROR: La variable de entorno 'API_KEY_weathermap' no fue configurada en IntelliJ.");
        }
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public CompletableFuture<String> buscarCoordenadas(String ciudad) {
        try {
            String ciudadCodificada = URLEncoder.encode(ciudad, StandardCharsets.UTF_8);
            String url = String.format("%s?q=%s&limit=1&appid=%s", GEO_URL, ciudad.replace(" ", "%20"), API_KEY);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() != 200) {
                            throw new ErrorConsultaApiException("Error buscando ciudad: " + response.statusCode());

                        }
                        return response.body();
                    });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(new ErrorConsultaApiException("URL con error o se precentan caracteres especiales"));
        }
    }


    public CompletableFuture<String> obtenerPronostico(double lat, double lon) {
        String url = String.format("%s?lat=%f&lon=%f&units=metric&appid=%s&lang=es",
                FORECAST_URL, lat, lon, API_KEY);
        return ejecutarRequest(url);
    }
    private CompletableFuture<String> ejecutarRequest(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        throw new RuntimeException("Error en la API: " + response.statusCode());
                    }
                    return response.body();
                });
    }

}
