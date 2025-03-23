import java.util.Scanner;

import services.DatabaseService;
import services.EpisodeService;
import services.LocationService;
import services.CharacterService;
import utils.HibernateUtils;
import utils.IntValidator;
import utils.Menus;

/**
 * @autor Olexandr Galaktionov Tsisar
 */
public class Main { 
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        int opcion, eleccion;

        do {
        	Menus.menuPrincipal();
            opcion = IntValidator.validarEntero(scanner);
            switch (opcion) {
            	case 1:
            		do {
	            		Menus.menuGestion("personaje");
	            		eleccion = IntValidator.validarEntero(scanner);
	            		switch (eleccion) {
	            			case 1:
	            				Menus.menuConsultasPNJ(scanner);
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
            		do {
            		Menus.menuGestion("locations");
            		eleccion = IntValidator.validarEntero(scanner);
	            		switch (eleccion) {
	            			case 1:
	            				Menus.menuConsultasLocations(scanner);
	            				break;
	            			case 2:
	            				LocationService.insertarLocation();
	            				break;
	            			case 3:
	            				LocationService.modificarLocation();
	            				break;
	            			case 4:
	            				LocationService.borrarLocation();
	            				break;
	            			case 5:
	            				System.out.println("Volviendo al menú principal...");
	        					break;
	            			default: 
	                            System.out.println("Opción inválida. Inténtalo de nuevo.");
	            		}
            		} while (eleccion != 5);
            		break;
            	case 3:
            		do {
            		Menus.menuGestionEpisodio("episodios");
            		eleccion = IntValidator.validarEntero(scanner);
	            		switch (eleccion) {
	            			case 1: 
	            				Menus.menuConsultasEpisodios(scanner);
	            				break;
	            			case 2:
	            				EpisodeService.insertarEpisodio();
	            				break;
	            			case 3:
	            				EpisodeService.modificarEpisodio();
	            				break;
	            			case 4:
	            				EpisodeService.borrarEpisodio();
	            				break;
	            			case 5:
	            				EpisodeService.asignarPersonajeAEpisodio();
	        					break;
	            			case 6:
	            				System.out.println("Volviendo al menú principal...");
	            				break;
	            			default: 
                                System.out.println("Opción inválida. Inténtalo de nuevo.");
	            		}
            		} while (eleccion != 6);
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
        HibernateUtils.shutdown(); // libera recursos y cierra Hibernate al salir
    }
}
