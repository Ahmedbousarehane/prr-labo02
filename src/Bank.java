import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Laboratoire No 2 PRR - modif OS : Mac OS X 10.7 et 10.8
 * 
 * @author L. Constantin & J.Gander Date :
 * @date octobre-novembre 2012
 * 
 *       DESCRIPTION
 * 
 * 
 *       ANALYSE
 * 
 *       REMARQUES
 * 
 *       TESTS
 * 
 * 
 *       STRUCTURE DU PROGRAMME
 *       <ul>
 *       <li>Config.java : valeurs par defaut pour utiliser le programme</li>
 *       <li>ConfigParser : parser la ligne de commande pour creer la config</li>
 *       <li>Labo02 : lance des clients et un serveur dans des threads (tests)..
 *       </li>
 *       </ul>
 *       UTILISATION
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
		accounts.remove(accounts);
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
