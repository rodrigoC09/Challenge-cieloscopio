package com.cieloscopio.cieloscopio;

import com.cieloscopio.cieloscopio.client.ApiClima.weathermapApi;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Scanner;

@SpringBootApplication
public class CieloscopioApplication {

	public static void main(String[] args) {
		SpringApplication.run(CieloscopioApplication.class, args);
	}

	@Bean
	public CommandLineRunner run() {
		return args -> {
			weathermapApi service = new weathermapApi();
			Scanner teclado = new Scanner(System.in);

			System.out.println("--- BIENVENIDO A CIELOSCOPIO ---");
			System.out.print("Introduce el nombre de la ciudad: ");
			String ciudad = teclado.nextLine();

			service.buscarCoordenadas(ciudad)
					.thenAccept(json -> {
						System.out.println("Datos recibidos exitosamente:");
						System.out.println(json);

						// Si el JSON es [] es que la ciudad no existe
						if (json.equals("[]")) {
							System.out.println("No se encontró la ciudad: " + ciudad);
						}
					})
					.exceptionally(ex -> {
						System.out.println("Fallo en la aplicación: " + ex.getMessage());
						return null;
					})
					.join();
		};
	}
}
