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

public class MeteorologiaService {
    private final String API_KEY;
    private final HttpClient httpClient;
    private final String API_KEY_ACCU;
    //temperatura actual

    //Url geolocalizacion(OpenWeather)
    private static final String GEO_URL="http://api.openweathermap.org/geo/1.0/direct";
    //Url pronostico de 5 dias
    private static final String FORECAST_URL = "https://api.openweathermap.org/data/2.5/forecast";

    //Url AccuWeather
    private static final String ACCU_LOCATION_URL = "https://dataservice.accuweather.com/locations/v1/cities/search";
    private static final String ACCU_FORECAST_URL = "https://dataservice.accuweather.com/forecasts/v1/daily/1day/";


    public MeteorologiaService() {
        this.API_KEY = System.getenv("API_KEY_weathermap");
        this.API_KEY_ACCU = System.getenv("API_KEY_accuweather");


        if (this.API_KEY == null || this.API_KEY_ACCU ==null) {
            throw new IllegalStateException("ERROR: La variable de entorno 'API_KEY_weathermap' no fue configurada o La variable de entorno 'API_KEY_AccuWeather' no fue configurada en IntelliJ.");
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

    //Metodos ACCUWEATHER

    public CompletableFuture<String> obtenerLocationKey(String ciudad) {
        String ciudadCodificada = URLEncoder.encode(ciudad, StandardCharsets.UTF_8);

        String url = String.format("%s?apikey=%s&q=%s&language=es-es",
                ACCU_LOCATION_URL, API_KEY_ACCU, ciudadCodificada);
        return ejecutarRequest(url);
    }

    public CompletableFuture<String> obtenerDetalleAccu(String locationKey) {
        String url = String.format("%s%s?apikey=%s&details=true&language=es-es",
                ACCU_FORECAST_URL, locationKey, API_KEY_ACCU);
        return ejecutarRequest(url);
    }




}
