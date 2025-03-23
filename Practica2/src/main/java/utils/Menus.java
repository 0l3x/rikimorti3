package utils;

import java.util.Scanner;

import services.CharacterService;
import services.EpisodeService;
import services.LocationService;

public class Menus {
	public static void menuPrincipal() {
		System.out.println("\nMenú Principal:");
        System.out.println("1. Gestión de personajes");
        System.out.println("2. Gestión de locations");
        System.out.println("3. Gestión de episodios");
        System.out.println("4. Llenar la BBDD desde la API");
        System.out.println("0. Salir");
        System.out.print("Selecciona una opción: ");
    }
	
	public static void menuGestion(String nomTabla) {
		System.out.println("\nMenú "+ nomTabla +":");
        System.out.println("1. Consultas");
        System.out.println("2. Insertar " + nomTabla);
        System.out.println("3. Modificar " + nomTabla);
        System.out.println("4. Borrar " + nomTabla);
        System.out.println("5. Salir");
        System.out.print("Elige opción: ");
    }
	
	public static void menuConsultasPNJ(Scanner scanner) {
	    int opcion;
	    do {
	        System.out.println("\nMenú Consultas de Personajes:");
	        System.out.println("1. Buscar por texto");
	        System.out.println("2. Buscar personajes sin episodio");
	        System.out.println("0. Volver al menú anterior");
	        System.out.print("Elija opción: ");
	        
	        opcion = IntValidator.validarEntero(scanner);

	        switch (opcion) {
	            case 1:
	                CharacterService.buscarPersonajesPorTexto();
	                break;
	            case 2:
	                CharacterService.buscarPersonajesSinEpisodiosHQL();
	                break;
	            case 0:
	                System.out.println("Volviendo al menú anterior...");
	                break;
	            default:
	                System.out.println("Opción inválida. Inténtalo de nuevo.");
	                break;
	        }
	    } while (opcion != 0);
	}
	
	public static void menuConsultasLocations(Scanner scanner) {
		int opcion;
	    do {
	    	System.out.println("\nMenú Consultas de Locations:");
			System.out.println("1. Buscar por texto");
			System.out.println("2. Buscar locations sin personajes");
	        System.out.println("0. Volver al menú anterior");
	        System.out.print("Elija opción: ");
	        
	        opcion = IntValidator.validarEntero(scanner);

	        switch (opcion) {
	            case 1:
	            	LocationService.buscarLocalizacionesPorTexto();
	                break;
	            case 2:
	            	LocationService.buscarLocationsSinPersonajes();
	                break;
	            case 0:
	                System.out.println("Volviendo al menú anterior...");
	                break;
	            default:
	                System.out.println("Opción inválida. Inténtalo de nuevo.");
	                break;
	        }
	    } while (opcion != 0);
	    
	}
	
	public static void menuConsultasEpisodios(Scanner scanner) {
		int opcion;
	    do {
	    	System.out.println("\nMenú Consultas de Episodios:");
			System.out.println("1. Buscar por texto");
			System.out.println("2. Busca episodio en el que aparecen más personajes");
	        System.out.println("0. Volver al menú anterior");
	        System.out.print("Elija opción: ");
	        
	        opcion = IntValidator.validarEntero(scanner);

	        switch (opcion) {
	            case 1:
	                EpisodeService.buscarEpisodiosPorTexto();
	                break;
	            case 2:
	                EpisodeService.buscarEpisodioConMasPersonajes();
	                break;
	            case 0:
	                System.out.println("Volviendo al menú anterior...");
	                break;
	            default:
	                System.out.println("Opción inválida. Inténtalo de nuevo.");
	                break;
	        }
	    } while (opcion != 0);
	}	
	
	public static void menuGestionEpisodio(String nomTabla) {
		System.out.println("\nMenú "+ nomTabla +":");
        System.out.println("1. Consultas");
        System.out.println("2. Insertar " + nomTabla);
        System.out.println("3. Modificar " + nomTabla);
        System.out.println("4. Borrar " + nomTabla);
        System.out.println("5. Asignar personajes a episodio"); // opcion extra de Episodio, haciendo necesario duplicar menuGestion
        System.out.println("6. Salir");
        System.out.print("Elige opción: ");
    }
}
