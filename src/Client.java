import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

/**
 * Client.
 *  
 * @version 1.0
 * @author Laurent Constantin
 * @author Jonathan Gander
 */
public class Client {
	
	public Client() {

		Menu choice;
		
		System.out.println("DŽmarrage du client ");

		do {
			
			// Choisir une banque
			int bankChoice;
			do {
				System.out.print("Veuillez entrer le numero de la banque (0.."+ (Config.banksAddresses.length - 1) +") > ");
				bankChoice = Toolbox.readBank();
				
			} while (bankChoice < 0 || bankChoice > Config.banksAddresses.length - 1);
			
			
			// Affichage du menu
			for (Menu m : Menu.values()) {
				System.out.println(m.ordinal() + ": " + m);
			}
			
			// Lecture du choix
			System.out.println("Votre choix > ");
			choice = Toolbox.readMenu();
			
			if (choice == null) {
				System.out.println("Erreur de saisie, veuillez recommencer.");
				continue;
			}
						
			// Lance la bonne operation
			switch (choice) {
				case ADD_ACCOUNT : {
					
				}
				break;
				case DELETE_ACCOUNT : {
					
				}
				break;
				case ADD_MONEY : {
					
				}
				break;
				case TAKE_MONEY : {
					
				}
				break;
				case GET_BALANCE : {
					
				}
				break;
				case QUIT : break;
				default :
					throw new UnsupportedOperationException("Menu inconnu");
			}
			
		} while (choice != Menu.QUIT);
			
		System.out.println("Fin du client");
	}

	public static void main(String[] args) {
		// java -jar Client.jar host portClient unicastHost portServer
		new Client();
	}
}
