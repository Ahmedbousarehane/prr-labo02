import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

import javax.tools.Tool;

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
public class Bank implements LamportOnUnlock {

	/**
	 * Thread pour syncroniser la creation d'un compte distant entre 2 banques
	 */
	class HandleBankRequest implements Runnable {
		final Bank bank;
		final DatagramSocket socket;

		public HandleBankRequest(Bank bank) throws SocketException {
			this.bank = bank;
			this.socket = new DatagramSocket(Config.bank2bank[bank.getId()]);
		}

		public void run() {
			while (true) {
				byte[] buffer = new byte[Config.bufferSize];
				DatagramPacket packet = new DatagramPacket(buffer,
						buffer.length);
				try {
					socket.receive(packet);
					// Account
					int account = Toolbox.byte2int(buffer);
					// Amount
					byte[] b = new byte[4];
					for (int i = 0; i < b.length; i++)
						b[i] = buffer[i + 4];
					int money = Toolbox.byte2int(b);

					bank.handleOnCreate(account, money);
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}

	}

	/**
	 * Classe interne pour gerer une requete client TODO : Nettoyer c'est plus
	 * un thread
	 */
	class HandleClientRequest {
		private final Bank bank;
		private final DatagramPacket packet;

		public HandleClientRequest(Bank bank, DatagramPacket packet) {
			this.bank = bank;
			this.packet = packet;
		}

		/**
		 * Envoie des donnees au client
		 * 
		 * @param code Le code d'erreur
		 * @param data[] Les donnees
		 * @throws IOException Si erreur
		 */
		public void sendData(ErrorServerClient code, int... data)
				throws IOException {

			byte[] message = Toolbox.buildMessage(code.getCode(), data);

			// Cree les donnees
			DatagramPacket packet = new DatagramPacket(message, message.length,
					this.packet.getAddress(), this.packet.getPort());
			// Cree le socket et envoi
			DatagramSocket sendToClientSocket = new DatagramSocket();
			sendToClientSocket.send(packet);
		}

		public void run() {
			// Lit les donnees
			Menu action = Menu.fromCode(Toolbox.getDataCode(this.packet));
			if (action == null) {
				System.err.println("Message invalide");
				return;
			}

			System.out.println("La banque "+bankId+" recoit:\n > action : " + action);
			int val[] = Toolbox.buildData(this.packet);
			for (int i = 0; i < val.length; i++)
				System.out.println(" > " + val[i]);
			// Lance une action suivant le message recu
			// TODO Envoi de la reponse au client
			try {

				switch (action) {
				case ADD_ACCOUNT:
					if (val[0] < 0) {
						this.sendData(ErrorServerClient.MONTANT_INCORRECT);
						return;
					}

					int accountNumber = bank.addAccount(val[0]);
					if (accountNumber == -1) {
						// Renvoi erreur 4 (autre)
						this.sendData(ErrorServerClient.AUTRE);
						return;
					}

					// Renvoie le numero de compte au client
					this.sendData(ErrorServerClient.OK, accountNumber);

					// TODO Repliquer a l'autre banque
					// Cree les donnees
					byte[] dataAccount = Toolbox.int2Byte(accountNumber);
					byte[] dataMoney = Toolbox.int2Byte(val[0]);
					byte[] data = Toolbox.concat(dataAccount, dataMoney);
					sendToOtherBank(data);
					
					break;
				case DELETE_ACCOUNT: {

					ErrorServerClient ret = bank.deleteAccount(val[0]);
					if (ret != ErrorServerClient.OK) {
						// Erreur au client
						this.sendData(ret);
						return;
					}

					// Reponse au client
					this.sendData(ErrorServerClient.OK);

				}
					break;
				case ADD_MONEY: {

					ErrorServerClient ret = bank.addMoney(val[0], val[1]);

					if (ret != ErrorServerClient.OK) {
						// Erreur au client
						this.sendData(ret);
						return;
					}

					// Reponse au client
					this.sendData(ErrorServerClient.OK);

				}
					break;
				case TAKE_MONEY: {

					ErrorServerClient ret = bank.takeMoney(val[0], val[1]);
					if (ret != ErrorServerClient.OK) {
						// Erreur au client
						this.sendData(ret);
						return;
					}

					// Reponse au client
					this.sendData(ErrorServerClient.OK);

				}
					break;
				case GET_BALANCE:
					int money = bank.getBalance(val[0]);

					if (money < 0) {
						// Erreur au client
						this.sendData(ErrorServerClient.COMPTE_INEXISTANT);
						return;
					}

					// Reponse au client
					// TODO Remove debug
					System.out.println("GET_BALANCE : Solde du compte "+val[0]+" => "+money);
					this.sendData(ErrorServerClient.OK, money);
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
	 * @param id Id de la banque
	 * @throws SocketException
	 */
	public Bank(int id) throws SocketException {
		this.bankId = id;
		System.out.println("Bank " + id + " : initialisation sur le port :"
				+ Config.banks2ClientPorts[id]);
		listenFromClientSocket = new DatagramSocket(
				Config.banks2ClientPorts[id]);

		this.lamport = new Lamport(this);
		new Thread(lamport).start();
		
		new Thread(new HandleBankRequest(this)).start();

		// 0. Receptionne une commande client

		while (true) {
			try {
				// 1. Cree un thread pour gerer une requete client
				byte[] buffer = new byte[Config.bufferSize];
				DatagramPacket data = new DatagramPacket(buffer, buffer.length);
				listenFromClientSocket.receive(data);
				new HandleClientRequest(this, data).run();

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
	 * Envoi a l'autre banque
	 * @throws IOException 
	 * @throws SocketException 
	 */
	public void sendToOtherBank(byte[] message) throws SocketException, IOException{
		int remoteId = (this.bankId == 0?1:0);
		// Construction de l'adresse et du datagramme
		InetAddress host = InetAddress.getByName(Config.banksAddresses[remoteId]);
		DatagramPacket packet = new DatagramPacket(message, message.length, host,
				Config.bank2bank[remoteId]);
		// Envoi
		new DatagramSocket().send(packet);		
	}
	/**
	 * Cree un nouveau compte
	 * 
	 * @param montant initial
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
	 * @param account Compte a supprimer
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
	 * @param account Compte a crediter
	 * @param money Montant a ajouter
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
	 * @param account Compte a crediter
	 * @param montant a supprimer
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
	 * @param account compte a qui obtenir le solde
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
	 * @param account Le compte
	 * @param money le nouveau montant
	 */
	@Override
	public void handleOnUpdate(int account, int money) {
		System.out.println("Mutex distant lache: la banque maj le compte "
				+ account + " avec le montant :" + money);

		accounts.put(account, money);

	}

	/**
	 * Suppression d'un element lorsqu'une autre banque libere le mutex
	 * 
	 * @param account Le compte a supprimer
	 */
	@Override
	public void handleOnDelete(int account) {
		System.out.println("Mutex distant lache: la banque supprime le compte "
				+ account);
		accounts.remove(accounts);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see LamportOnUnlock#handleOnCreate(int, int)
	 */
	@Override
	public void handleOnCreate(int account, int money) {
		System.out.println("La banque "+ bankId+" ajoute un compte distant " + account
				+ " (" + money + ")"+ "appartenant a la banque :"+Account.getBankId(account));
		accounts.put(account, money);
	}


	/**
	 * Permet d'instancier un serveur
	 * 
	 * @param args MulticastHost, port serveur, port client, nombre de clients
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
