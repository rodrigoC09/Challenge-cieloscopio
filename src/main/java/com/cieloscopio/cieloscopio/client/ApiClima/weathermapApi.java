package com.cieloscopio.cieloscopio.client.ApiClima;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class weathermapApi {
    private final String API_KEY;
    private final HttpClient httpClient;
    private static final String URL_TEMPLATE ="https://api.openweathermap.org/data/2.5/forecast";
    private static final String GEO_URL="http://api.openweathermap.org/geo/1.0/direct";


    public weathermapApi() {
        this.API_KEY = System.getenv("API_KEY_weathermap");
        if (this.API_KEY == null || this.API_KEY.isBlank()) {
            throw new IllegalStateException("ERROR: La variable de entorno 'API_KEY_weathermap' no fue configurada en IntelliJ.");
        }
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public CompletableFuture<String> buscarCoordenadas(String ciudad){
        String url = String.format("%s?q=%s&limit=1&appid=%s",GEO_URL, ciudad.replace(" ","%20"), API_KEY);

        HttpRequest request =HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response->{
                    if (response.statusCode() != 200) {
                        throw new RuntimeException("Error buscando ciudad: "+response.statusCode());

                    }
                    return response.body();
                });
    }

//    public CompletableFuture<String> obtenerUbicacion(double lat, double lon) {
//        String url = String.format("%s?lat=%f&lon=%f&units=metric&appid=%s&lang=es",
//                URL_TEMPLATE, lat, lon, API_KEY);
//
//        //HttpClient client = HttpClient.newHttpClient();
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create(url))
//                .GET()
//                .build();
//
//        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
//                        .thenApply(response->{
//                            if (response.statusCode() != 200) {
//                                throw new RuntimeException("Error API: "+response.statusCode());
//                            }
//                            return response.body();
//                        });
//
//
//
//    }
}
