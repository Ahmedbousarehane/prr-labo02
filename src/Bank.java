import java.io.*;
import java.net.*;

/**
 * Laboratoire No 2 
 * PRR - modif 
 * OS : Mac OS X 10.7 et 10.8
 * @author L. Constantin & J.Gander Date :
 * @date octobre-novembre 2012 
 * 
 * DESCRIPTION
 * 
 * 
 * ANALYSE
 * 
 * REMARQUES
 * 
 * TESTS
 * 
 * 
 * STRUCTURE DU PROGRAMME 
 * <ul>
 * <li>Config.java : valeurs par defaut pour utiliser le
 * programme</li>
 * <li>ConfigParser : parser la ligne de commande pour creer la config</li>
 * <li>Labo02 : lance des clients et un serveur dans des threads (tests)..</li>
 * </ul>
 * UTILISATION
 * 
 * @version 1.0
 * @author Laurent Constantin
 * @author Jonathan Gander
 */
public class Bank implements TellerInterface{

	/**
	 * Classe interne pour gerer une requete client
	 *
	 */
	class HandleClientRequest implements Runnable {
		private final Bank bank;
		private final DatagramPacket packet;
		public HandleClientRequest(Bank bank ,DatagramPacket packet) {
			this.bank = bank;
			this.packet = packet;
		}

		public void run() {
			// Lit les donnees
			Menu action = Menu.fromCode(Toolbox.getDataCode(this.packet));
			if(action == null){
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
			switch(action){
			case ADD_ACCOUNT:
				bank.addAccount(val[0]);
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
				bank.takeMoney(val[0],val[1]);
				break;
			default:
				throw new IllegalStateException("Unimplemented action");
			}	
		}
	}

	// Comptes (n,montant)
	// private Map<Integer,Integer> accounts = new HashMap<Integer,Integer>();
	//private int bankId;

	private DatagramSocket clientSocket;

	public Bank(int id) throws SocketException {
		System.out.println("Bank " + id + " : initialisation sur le port :"
				+ Config.banksPorts[id]);
		clientSocket = new DatagramSocket(Config.banksPorts[id]);
		// 0. Receptionne une commande client

		while (true) {
			try {
				// 1. Cree un thread pour gerer une requete client
				byte[] buffer = new byte[Config.bufferSize];
				DatagramPacket data = new DatagramPacket(buffer, buffer.length);
				clientSocket.receive(data);
				new Thread(new HandleClientRequest(this,data)).start();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/* (non-Javadoc)
	 * @see TellerInterface#addAccount(int)
	 */
	@Override
	public void addAccount(int money) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see TellerInterface#deleteAccount(int)
	 */
	@Override
	public void deleteAccount(int account) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see TellerInterface#addMoney(int, int)
	 */
	@Override
	public void addMoney(int account, int money) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see TellerInterface#takeMoney(int, int)
	 */
	@Override
	public void takeMoney(int account, int money) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
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
