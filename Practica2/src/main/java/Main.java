import java.util.Scanner;

import services.DatabaseService;
import services.CharacterService;
import utils.IntValidator;

/**
 * @autor Olexandr Galaktionov Tsisar
 */
public class Main { 
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        int opcion, eleccion;

        do {
        	menuPrincipal();
            opcion = IntValidator.validarEntero(scanner);
            switch (opcion) {
            	case 1:
            		do {
	            		menuGestion("personaje");
	            		eleccion = IntValidator.validarEntero(scanner);
	            		switch (eleccion) {
	            			case 1:
	            				menuConsultasPNJ(scanner);
	            				break;
	            			case 2:
	            				CharacterService.insertarPersonajeHQL();
	            				break;
	            			case 3:
	            				CharacterService.modificarPersonajeHQL();
	            				break;
	            			case 4:
	            				CharacterService.borrarPersonajeHQL();
	            				break;
	            			case 5:
	            				System.out.println("Volviendo al menú principal...");
	                			break;
                			default: 
                                System.out.println("Opción inválida. Inténtalo de nuevo.");
	            		}
            		} while (eleccion != 5);
            		break;
            	case 2:
            		menuGestion("locations");
            		eleccion = IntValidator.validarEntero(scanner);
            		switch (eleccion) {
            			case 1:
            				break;
            			case 2:
            				break;
            			case 3:
            				break;
            			case 4:
            				break;
            			case 5:
        					return;
            		}
            		break;
            	case 3:
            		menuGestion("episodios");
            		eleccion = IntValidator.validarEntero(scanner);
            		switch (eleccion) {
            			case 1: 
            				break;
            			case 2:
            				break;
            			case 3:
            				break;
            			case 4:
            				break;
            			case 5:
        					return;
            		}
            		break;
            	case 4:
            		DatabaseService.fillDatabase();
            		break;
                case 0:
                    System.out.println("Saliendo del programa...");
                    break;  
                default:
                    System.out.println("Opción inválida. Inténtalo de nuevo.");
            }
        } while (opcion != 0);

        scanner.close();
    }
    
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

	
	public static void menuConsultasLocations() {
		System.out.println("\nMenú Consultas de Locations:");
		System.out.println("1. Buscar por texto");
		System.out.println("2. Buscar locations sin personajes");
	}
	
	public static void menuConsultasEpisodios() {
		System.out.println("\nMenú Consultas de Episodios:");
		System.out.println("1. Buscar por texto");
		System.out.println("2. Busca episodio en el que aparecen más personajes");
	}
	
	
}
