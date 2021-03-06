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
		} catch (UnknownHostException uhe) {
			System.err.println("Impossible de contacter une des banque.");
		} catch (SocketException se) {
			System.err.println("Impossible d'ouvrir la connexion.");
		}

		System.out.println("D�marrage du client ");

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
				if (accountNumber < 0) {
					System.out.println("Il n'y a plus de compte disponible !");
				} else {
					System.out.println("Compte cree : " + accountNumber);
				}
			}
				break;
			case DELETE_ACCOUNT: {
				System.out.print("Entrer le numero du compte a supprimer > ");
				int account = Toolbox.readInt(0, Account.getMaxAccount());

				ErrorServerClient ret = tellers[bankChoice]
						.deleteAccount(account);

				System.out.println(handleResponse(ret));
			}
				break;
			case ADD_MONEY: {
				System.out.print("Entrer le numero du compte a crediter > ");
				int account = Toolbox.readInt(0, Account.getMaxAccount());
				System.out.print("Entrer le montant a crediter > ");
				int money = Toolbox.readInt(1, Integer.MAX_VALUE);

				ErrorServerClient ret = tellers[bankChoice].addMoney(account,
						money);
				System.out.println(handleResponse(ret));

			}
				break;
			case TAKE_MONEY: {
				System.out.print("Entrer le numero du compte a debiter > ");
				int account = Toolbox.readInt(0, Account.getMaxAccount());
				System.out.print("Entrer le montant a debiter > ");
				int money = Toolbox.readInt(1, Integer.MAX_VALUE);

				ErrorServerClient ret = tellers[bankChoice].takeMoney(account,
						money);
				System.out.println(handleResponse(ret));

			}
				break;
			case GET_BALANCE: {
				System.out.print("Entrer le numero du compte > ");
				int account = Toolbox.readInt(0, Account.getMaxAccount());
				int balance = tellers[bankChoice].getBalance(account);
				if (balance < 0) {
					System.out.println("Le compte " + account
							+ " n'existe pas !");
				} else {
					System.out.println("Solde du compte " + account + " : "
							+ balance);
				}
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

	/**
	 * Permet d'afficher un message d'erreur correspondant a une reponse du
	 * serveur au client
	 * 
	 * @param response
	 *            La reponse recue du client
	 * @return Le message d'erreur
	 */
	public static String handleResponse(ErrorServerClient response) {
		switch (response) {
		case OK:
			return "Operation reussie !";
		case COMPTE_INEXISTANT:
			return "Le compte entre n'existe pas !";
		case SOLDE_INVALIDE:
			return "Le solde est invalide !";
		case MONTANT_INCORRECT:
			return "Le montant fourni est incorrect !";
		case AUTRE:
			return "Une erreur inconnue est survenue !";
		default:
			return "Une erreur non geree est survenue !";
		}
	}
}
