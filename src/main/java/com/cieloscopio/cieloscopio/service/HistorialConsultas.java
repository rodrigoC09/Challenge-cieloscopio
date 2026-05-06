package com.cieloscopio.cieloscopio.service;

import com.cieloscopio.cieloscopio.model.RegistroConsulta;

import java.util.*;

public class HistorialConsultas {
    private final List<RegistroConsulta> registros = new ArrayList<>();
    private final int Limite_maximo= 15;

    public void agregarRegistro(RegistroConsulta registro){

        if (registros.size() > Limite_maximo) {
            registros.remove(0);
        }
        registros.add(registro);
    }
    public List<RegistroConsulta> obtenerRegistros() {

        List<RegistroConsulta> listaInvertida = new ArrayList<>(registros);
        Collections.reverse(listaInvertida);
        return listaInvertida;
    }
    public void limpiarHistorial(){
        registros.clear();
    }
}
