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
public class Bank implements TellerInterface {

	/**
	 * Classe interne pour gerer une requete client
	 * 
	 */
	class HandleClientRequest implements Runnable {
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
				// TODO : Envoi d'une erreur
				System.err.println("Message invalide");
				return;
			}

			System.out.println("La banque recoit:\n > action : " + action);
			int val[] = Toolbox.buildData(this.packet);
			for (int i = 0; i < val.length; i++)
				System.out.println(" > " + val[i]);
			// Lance une action suivant le message recu
			// TODO : Envoi de la reponse
			try {

				switch (action) {
				case ADD_ACCOUNT:
					if (val[0] < 0) {
						this.sendData(ErrorServerClient.MONTANT_INCORRECT);
					}

					int accountNumber = bank.addAccount(val[0]);
					if (accountNumber == -1) {
						// Renvoi erreur 4 (autre)
						this.sendData(ErrorServerClient.AUTRE);

					}

					// renvoie num compte a l autre banque et au client
					this.sendData(ErrorServerClient.OK, accountNumber);
					// TODO: Renvoyer au serveur

					break;
				case DELETE_ACCOUNT:
					bank.deleteAccount(val[0]);
					break;
				case ADD_MONEY:
					bank.addAccount(val[0]);
					break;
				case GET_BALANCE:
					bank.getBalance(val[0]);
					break;
				case TAKE_MONEY:
					bank.takeMoney(val[0], val[1]);
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
				+ Config.banksPorts[id]);
		listenFromClientSocket = new DatagramSocket(Config.banksPorts[id]);
		// 0. Receptionne une commande client

		while (true) {
			try {
				// 1. Cree un thread pour gerer une requete client
				byte[] buffer = new byte[Config.bufferSize];
				DatagramPacket data = new DatagramPacket(buffer, buffer.length);
				listenFromClientSocket.receive(data);
				new Thread(new HandleClientRequest(this, data)).start();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see TellerInterface#addAccount(int)
	 */
	@Override
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see TellerInterface#deleteAccount(int)
	 */
	@Override
	public void deleteAccount(int account) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see TellerInterface#addMoney(int, int)
	 */
	@Override
	public void addMoney(int account, int money) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see TellerInterface#takeMoney(int, int)
	 */
	@Override
	public void takeMoney(int account, int money) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see TellerInterface#getBalance(int)
	 */
	@Override
	public int getBalance(int account) {
		// TODO Auto-generated method stub
		return 0;
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
