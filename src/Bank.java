import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Laboratoire No 2 PRR
 * Auteurs : L. Constantin & J.Gander
 * Date : octobre-novembre 2012
 * OS : Mac OS X 10.7 et 10.8
 * 
 * DESCRIPTION
 * Dans ce laboratoire, il a ete demande de simuler une banque, representee par
 * deux succursales. Ces deux succursales stockent des comptes (numŽro, solde)
 * et doivent les partager entre elles.
 * De plus, une application client permet de faire des requetes sur la banque
 * independemment de la succursale choisie.
 * La banque doit pouvoir offrir au client les fonctionnalites suivantes :
 * - Creer un compte
 * - Supprimer un compte
 * - Deposer un montant
 * - Retirer un montant
 * - Obtenir le solde d'un compte
 * Afin de s'assurer de la coherence des donnees, les deux succursales utilisent
 * l'algorithme de Lamport etudie en classe. Une petite modification de
 * l'algorithme a ete faite : lors du message de liberation, une succursale
 * communique a l'autre les changements effectues.
 * Les methodes "creer un compte" et "obtenir le solde d'un compte" ne neces-
 * sitent pas d'exclusion mutuelle entre les succursales contrairement aux
 * trois autres.
 * Les numeros de comptes sont representes par des entiers sur 32 bits avec 8
 * bits pour indiquer l'id de la succursale et 24 bits pour un id du compte au
 * sein de la succursale. Ceci permet d'eviter les communications inutiles lors
 * de la creation de compte.
 * Suppositions : le reseau et les succursales sont fiables. Les banques sont
 * lancees avant les clients.  
 * 
 * ANALYSE
 * Dans notre logique, la classe Client.java represente uniquement un terminal
 * permettant au client de faire des requetes. Ces dernieres sont gerees par un
 * guichetier (Teller.java). Celui-ci va gerer les communications avec la 
 * succursale desiree.
 * La succursale traite les demandes clients une par une. Chaque succursale a
 * un thread Lamport permettant l'exclusion mutuelle si besoin avec l'autre 
 * succursale.
 * 
 * Lors de notre analyse, nous avons tout d'abord voulu mettre Lamport hors
 * d'un thread. Cependant, l'ecoute sur le socket doit se faire en tout temps 
 * afin de gerer les replications. Si aucune demande client n'est faite, 
 * l'ecoute ne serait pas effectuee et la replication ne fonctionnerait 
 * donc pas. 
 * Nous avons donc decide de separer la succursale (Bank.java) et le thread 
 * Lamport (Lamport.java) afin de mieux correspondre avec l'algorithme Ada
 * etudie en cours.
 * 
 * 
 * COMMUNICATIONS
 * Afin de bien structurer les communications, nous avons utilise des types
 * enumeres pour representer les types de messages transitant sur le reseau.
 * 
 * Les communications entre client et banque (Teller et Bank) ont la forme 
 * suivante : (Type_Message [, Numero_Compte] [, Montant])
 * 
 * Les communications Lamport en cas de Requete ou Quittance ont la forme 
 * suivante : (Type_Message, Estampille, Id_Banque_Emettrice)
 * 
 * Les communications Lamport en cas de Liberation ont la forme suivante afin 
 * de repliquer les donnees a l'autre succursale :
 * (Type_Message, Estampille, Id_Banque_Emettrice, Type_Message 
 * [, Numero_Compte] [, Montant])
 * 
 * Lors de la creation de compte, la replication doit etre effectuee sans
 * utiliser Lamport. Le message de replication est tout de meme envoye en
 * utilisant cette classe afin de ne pas creer un thread supplementaire dans
 * les succursales. Ce message a la forme suivante :
 * (Type_Message, Numero_Compte, Montant)
 *
 * La creation des messages a ete faite par la classe Toolbox.
 *
 * REMARQUES
 * Le fait d'avoir utilise un thread pour Lamport nous a permis de bloquer le
 * traitement de la demande client a l'aide d'un wait() lorsque la succursale
 * n'a pas l'exclusion mutuelle. Et de la debloquer a l'aide d'un notify lorsque
 * l'acces est autorise. Il ne peut donc y avoir qu'une seule demande client 
 * traitee a la fois.
 * 
 * A comparer avec le laboratoire no 1, nous avons remarque qu'il nous a ete
 * plus difficile d'implementer l'architecture du probleme. 
 * 
 * TESTS
 * 
 * 
 * STRUCTURE DU PROGRAMME
 * Config.java : valeurs par defaut pour utiliser le programme
 * ConfigParser : parser la ligne de commande pour creer la config
 * Labo02 : lance des clients et un serveur dans des threads (tests)..
 * 
 * 
 * UTILISATION
 * Il faut tout d'abord modifier le fichier Config.java avec les bonnes IPs.
 * 
 * Le laboratoire se decompose en deux executables JAR pour le client et les 
 * succursale. Les jars peuvent etre crees a l'aide de l'utilitaire ANT.
 * 
 * Les fichiers se lancent avec la commande java -jar NomJar Parametres
 * Pour le client :
 * java -jar Client.jar
 * Pour la succursale :
 * java -jar Bank.jar id_de_la_succursale (0 ou 1)
 * 
 * @version 1.0
 * @author Laurent Constantin
 * @author Jonathan Gander
 */
public class Bank {
	/**
	 * Classe interne pour gerer une requete client
	 */
	class Bank2ClientTeller {
		private final Bank bank;
		private final DatagramPacket packet;

		/**
		 * Constructeur
		 * 
		 * @param bank
		 *            La banque qu'il doit gerer
		 * @param packet
		 *            Paquet envoye par le client
		 */
		public Bank2ClientTeller(Bank bank, DatagramPacket packet) {
			this.bank = bank;
			this.packet = packet;

			handleClientRequests();
		}

		/**
		 * Envoie des donnees au client
		 * 
		 * @param code
		 *            Le code d'erreur
		 * @param data
		 *            [] Les donnees
		 * @throws IOException
		 *             Si erreur
		 */
		public void sendDataToClient(ErrorServerClient code, int... data)
				throws IOException {

			byte[] message = Toolbox.buildMessage(code.getCode(), data);

			// Cree les donnees
			DatagramPacket packet = new DatagramPacket(message, message.length,
					this.packet.getAddress(), this.packet.getPort());
			// Cree le socket et envoi
			DatagramSocket sendToClientSocket = new DatagramSocket();
			sendToClientSocket.send(packet);
		}

		/**
		 * Gere une requete client (appel des bonnes methodes)
		 */
		private void handleClientRequests() {
			// Lit les donnees
			Menu action = Menu.fromCode(Toolbox.getDataCode(this.packet));
			int val[] = Toolbox.buildData(this.packet);

			// Debug
			System.out.print("La banque " + bankId + " recoit: > " + action);
			System.out.print("(");
			for (int i = 0; i < val.length; i++)
				System.out.print(val[i] + (i < val.length - 1 ? " " : ""));
			System.out.println(")");

			// Lance une action suivant le message recu
			try {

				switch (action) {
				case ADD_ACCOUNT:
					if (val[0] < 1) {
						this.sendDataToClient(ErrorServerClient.MONTANT_INCORRECT);
						return;
					}

					int accountNumber = bank.addAccount(val[0]);
					if (accountNumber == -1) {
						// Renvoi erreur 4 (autre)
						this.sendDataToClient(ErrorServerClient.AUTRE);
						return;
					}

					// Renvoie le numero de compte au client
					this.sendDataToClient(ErrorServerClient.OK, accountNumber);

					// Prevenir les autres banques
					lamport.accountCreated(accountNumber, val[0]);

					break;
				case DELETE_ACCOUNT: {

					ErrorServerClient ret = bank.deleteAccount(val[0]);
					if (ret != ErrorServerClient.OK) {
						// Erreur au client
						this.sendDataToClient(ret);
						return;
					}

					// Reponse au client
					this.sendDataToClient(ErrorServerClient.OK);

				}
					break;
				case ADD_MONEY: {

					ErrorServerClient ret = bank.addMoney(val[0], val[1]);

					if (ret != ErrorServerClient.OK) {
						// Erreur au client
						this.sendDataToClient(ret);
						return;
					}

					// Reponse au client
					this.sendDataToClient(ErrorServerClient.OK);

				}
					break;
				case TAKE_MONEY: {

					ErrorServerClient ret = bank.takeMoney(val[0], val[1]);
					if (ret != ErrorServerClient.OK) {
						// Erreur au client
						this.sendDataToClient(ret);
						return;
					}

					// Reponse au client
					this.sendDataToClient(ErrorServerClient.OK);

				}
					break;
				case GET_BALANCE:
					int money = bank.getBalance(val[0]);

					if (money < 0) {
						// Erreur au client
						this.sendDataToClient(ErrorServerClient.COMPTE_INEXISTANT);
						return;
					}

					// Reponse au client
					this.sendDataToClient(ErrorServerClient.OK, money);
					break;
				default:
					throw new IllegalStateException("Unimplemented action");
				}
			} catch (IOException e) {
				// Erreur d'envoi au client
				e.printStackTrace();
			}
		}
	}

	// Comptes (n,montant)
	private Map<Integer, Integer> accounts = new HashMap<Integer, Integer>();
	private int bankId;
	private final Lamport lamport;
	private DatagramSocket listenFromClientSocket;

	/**
	 * Constructeur d'une banque
	 * 
	 * @param id
	 *            Id de la banque
	 * @throws SocketException
	 */
	public Bank(int id) throws SocketException {
		this.bankId = id;
		listenFromClientSocket = new DatagramSocket(
				Config.banks2ClientPorts[id]);

		this.lamport = new Lamport(this);

		// 0. Receptionne une commande client
		while (true) {
			try {
				// 1. Ecoute et gere la demande d'un client
				byte[] buffer = new byte[Config.bufferSize];
				DatagramPacket data = new DatagramPacket(buffer, buffer.length);
				listenFromClientSocket.receive(data);
				new Bank2ClientTeller(this, data);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Renvoie l'identifiant de la banque
	 * 
	 * @return L'identifiant de la banque
	 */
	public int getId() {
		return bankId;
	}

	/**
	 * Cree un nouveau compte
	 * 
	 * @param montant
	 *            initial
	 * @return numero du compte
	 */
	public int addAccount(int money) {
		// Boucle sur tous les comptes possibles
		for (int i = 0; i < Account.getMaxAccount(); i++) {
			// Recupere le numero de compte de la banque
			int accountNumber = Account.serializeAccount(this.bankId, i);

			// Si le compte n'existe pas on le cree avec le montant initial
			if (!accounts.containsKey(accountNumber)) {
				accounts.put(accountNumber, money);
				return accountNumber;
			}
		}
		// Tous les comptes sont pris
		return -1;
	}

	/**
	 * Supprime un compte (en section critique)
	 * 
	 * @param account
	 *            Compte a supprimer
	 * @return Code d'erreur
	 * @throws IOException
	 */
	public ErrorServerClient deleteAccount(int account) throws IOException {
		if (!accounts.containsKey(account)) {
			return ErrorServerClient.COMPTE_INEXISTANT;
		}

		if (accounts.get(account) != 0) {
			return ErrorServerClient.SOLDE_INVALIDE;
		}

		lamport.lock();

		if (!accounts.containsKey(account)) {
			return ErrorServerClient.COMPTE_INEXISTANT;
		}

		if (accounts.get(account) != 0) {
			return ErrorServerClient.SOLDE_INVALIDE;
		}

		accounts.remove(account);

		lamport.unlock(LamportUnlockMessage.DELETE_ACCOUNT, account);

		return ErrorServerClient.OK;
	}

	/**
	 * Ajoute un montant a un compte (en section critique)
	 * 
	 * @param account
	 *            Compte a crediter
	 * @param money
	 *            Montant a ajouter
	 * @return Code d'erreur
	 * @throws IOException
	 */
	public ErrorServerClient addMoney(int account, int money)
			throws IOException {
		if (money < 0) {
			return ErrorServerClient.MONTANT_INCORRECT;
		}

		if (!accounts.containsKey(account)) {
			return ErrorServerClient.COMPTE_INEXISTANT;
		}

		lamport.lock();

		if (!accounts.containsKey(account)) {
			return ErrorServerClient.COMPTE_INEXISTANT;
		}

		accounts.put(account, accounts.get(account) + money);

		lamport.unlock(LamportUnlockMessage.UPDATE_MONEY, account,
				accounts.get(account));

		return ErrorServerClient.OK;

	}

	/**
	 * Debite un montant a un compte (en section critique)
	 * 
	 * @param account
	 *            Compte a crediter
	 * @param montant
	 *            a supprimer
	 * @return Code d'erreur
	 * @throws IOException
	 */
	public ErrorServerClient takeMoney(int account, int money)
			throws IOException {
		if (money < 0) {
			return ErrorServerClient.MONTANT_INCORRECT;
		}

		if (!accounts.containsKey(account)) {
			return ErrorServerClient.COMPTE_INEXISTANT;
		}

		if (accounts.get(account) - money < 0) {
			return ErrorServerClient.SOLDE_INVALIDE;
		}

		lamport.lock();

		if (!accounts.containsKey(account)) {
			return ErrorServerClient.COMPTE_INEXISTANT;
		}

		if (accounts.get(account) - money < 0) {
			return ErrorServerClient.SOLDE_INVALIDE;
		}

		accounts.put(account, accounts.get(account) - money);

		lamport.unlock(LamportUnlockMessage.UPDATE_MONEY, account,
				accounts.get(account));

		return ErrorServerClient.OK;

	}

	/**
	 * Obtient le solde d'un compte
	 * 
	 * @param account
	 *            compte a qui obtenir le solde
	 * @return Solde du compte
	 */
	public int getBalance(int account) {
		if (accounts.containsKey(account))
			return accounts.get(account);

		return -1;
	}

	/**
	 * Suppression d'un element lorsqu'une autre banque libere le mutex
	 * 
	 * @param account
	 *            Le compte
	 * @param money
	 *            le nouveau montant
	 */
	public void handleOnUpdate(int account, int money) {
		System.out.println("Mutex distant lache: la banque maj le compte "
				+ account + " avec le montant :" + money);
		if (!accounts.containsKey(account)) {
			System.out.println("handleOnUpdate : le compte n'existe plus");
			return;
		}
		accounts.put(account, money);

	}

	/**
	 * Suppression d'un element lorsqu'une autre banque libere le mutex
	 * 
	 * @param account
	 *            Le compte a supprimer
	 */
	public void handleOnDelete(int account) {
		System.out.println("Mutex distant lache: la banque supprime le compte "
				+ account);

		if (!accounts.containsKey(account)) {
			return;
		}

		if (accounts.get(account) != 0) {
			return;
		}
		accounts.remove(account);
	}

	/**
	 * Replication d'un compte quand une autre banque en cree un.
	 * 
	 * @param account
	 *            Le compte a creer
	 * @param money
	 *            L'argent
	 */
	public void handleOnCreate(int account, int money) {
		System.out.println("Banque " + bankId + " : "
				+ LamportMessages.NEW_ACCOUNT + " n: " + account + ", " + money
				+ "CHF");

		if (money < 1)
			return;

		accounts.put(account, money);
	}

	/**
	 * Permet d'instancier une banque
	 * 
	 * @param args
	 *            MulticastHost, port serveur, port client, nombre de clients
	 */
	public static void main(String[] args) {
		// java -jar Bank.jar BankId
		try {
			new Bank(ConfigParser.getBankIdFromArg(args, 0));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
