package utils;

import java.util.Scanner;

public class IntValidator {
	public static int validarEntero(Scanner scanner){
		while(true) {
			try {
				return Integer.parseInt(scanner.nextLine());
			} catch (NumberFormatException e) {
				System.out.println("Por favor introduce un número entero.");
			}
		}
	}
}
