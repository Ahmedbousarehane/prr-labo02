
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Client.
 * 
 * @version 1.0
 * @author Laurent Constantin
 * @author Jonathan Gander
 */
public class Client {

	public static void main(String[] args) {

		Menu choice;
		
		// Initialisation des guichetiers
		Teller tellers[] = new Teller[Config.banksAddresses.length];
		try {
			for (int i = 0; i < tellers.length; i++) {
				tellers[i] = new Teller(i);
			}
		}
		catch (UnknownHostException uhe) {
			System.err.println("Impossible de contacter une des banque.");
		}
		catch (SocketException se) {
			System.err.println("Impossible d'ouvrir la connexion.");
		}
		

		System.out.println("DŽmarrage du client ");

		do {

			// Choisir une banque
			int bankChoice;
			do {
				System.out.print("Veuillez entrer le numero de la banque (0.."
						+ (Config.banksAddresses.length - 1) + ") > ");
				bankChoice = Toolbox.readBank();

			} while (bankChoice < 0
					|| bankChoice > Config.banksAddresses.length - 1);

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
			case ADD_ACCOUNT: {
				System.out.print("Entrer le montant initial > ");
				int money = Toolbox.readInt(1, Integer.MAX_VALUE);
				int accountNumber = tellers[bankChoice].addAccount(money);
				System.out.println("Compte cree : "+accountNumber);
			}
				break;
			case DELETE_ACCOUNT: {
				System.out.print("Entrer le numero du compte a supprimer > ");
				int account = Account.readAccount(bankChoice);	
			
				tellers[bankChoice].deleteAccount(account);
			}
				break;
			case ADD_MONEY: {
				System.out.print("Entrer le numero du compte a crediter > ");
				int account = Account.readAccount(bankChoice);
				System.out.print("Entrer le moutant a crediter > ");
				int money = Toolbox.readInt(1, Integer.MAX_VALUE);
				
				tellers[bankChoice].addMoney(account,money);
			}
				break;
			case TAKE_MONEY: {
				System.out.print("Entrer le numero du compte a debiter > ");
				int account = Account.readAccount(bankChoice);
				System.out.print("Entrer le moutant a debiter > ");
				int money = Toolbox.readInt(1, Integer.MAX_VALUE);
				
				tellers[bankChoice].takeMoney(account, money);
			}
				break;
			case GET_BALANCE: {
				System.out.print("Entrer le numero du compte > ");
				int account = Account.readAccount(bankChoice);
				
				tellers[bankChoice].getBalance(account);
			}
				break;
			case QUIT:
				break;
			default:
				throw new UnsupportedOperationException("Menu inconnu");
			}

		} while (choice != Menu.QUIT);

		System.out.println("Fin du client");
	}
}
