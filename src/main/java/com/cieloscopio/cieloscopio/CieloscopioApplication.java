package com.cieloscopio.cieloscopio;

import com.cieloscopio.cieloscopio.principal.Principal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
