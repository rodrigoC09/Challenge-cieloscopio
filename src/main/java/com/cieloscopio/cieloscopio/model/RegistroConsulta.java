package com.cieloscopio.cieloscopio.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record RegistroConsulta(String ciudad, LocalDateTime momento, ModoReporte modoReporte) {
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return String.format("[%s] Ciudad: %-15s | Modo: %s",
                momento.format(formatter), ciudad, modoReporte);
    }
}
