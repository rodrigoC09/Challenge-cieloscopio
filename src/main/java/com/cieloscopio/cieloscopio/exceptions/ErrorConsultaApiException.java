package com.cieloscopio.cieloscopio.exceptions;

public class ErrorConsultaApiException extends RuntimeException{
    public ErrorConsultaApiException(String mensaje) {
        super(mensaje);
    }
}
