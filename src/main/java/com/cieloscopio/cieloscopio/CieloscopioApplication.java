package com.cieloscopio.cieloscopio;

import com.cieloscopio.cieloscopio.client.ApiClima.weathermapApi;
import com.cieloscopio.cieloscopio.principal.Principal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Scanner;

@SpringBootApplication
public class CieloscopioApplication implements CommandLineRunner{

	public static void main(String[] args) {
		SpringApplication.run(CieloscopioApplication.class, args);
	}

	@Override
	public void run(String... args)throws Exception {
		Principal principal = new Principal();
		principal.menuCieloscopio();
	}

}
