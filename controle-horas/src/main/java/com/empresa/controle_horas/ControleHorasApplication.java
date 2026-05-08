package com.empresa.controle_horas;

import com.empresa.controle_horas.ui.MainApp;
import javafx.application.Application;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ControleHorasApplication {

	public static void main(String[] args) {
		// Inicia o Spring Boot em thread separada
		new Thread(() ->
				SpringApplication.run(ControleHorasApplication.class, args)
		).start();

		// Aguarda o Spring subir antes de abrir a janela
		try { Thread.sleep(3000); } catch (InterruptedException ignored) {}

		// Abre a janela JavaFX
		Application.launch(MainApp.class, args);
	}
}