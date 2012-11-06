import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Represente un guichetier qui fait les requetes aux banques
 * 
 * @version 1.0
 * @author Laurent Constantin
 * @author Jonathan Gander
 */
public class Teller implements TellerInterface {
	private int port;
	private InetAddress host;

	private DatagramSocket socket;

	/**
	 * Constructeur
	 * 
	 * @param bankId Id de la banque a creer
	 * @throws UnknownHostException Si la banque ne peut pas etre trouvee sur le
	 *             reseau
	 * @throws SocketException Si la socket ne peut etre ouverte
	 */
	public Teller(int bankId) throws UnknownHostException, SocketException {
		if (bankId < 0 || bankId > Config.banksAddresses.length - 1)
			throw new IllegalArgumentException(
					"No de banque invalide pour le guichetier !");

		port = Config.banksPorts[bankId];
		host = InetAddress.getByName(Config.banksAddresses[bankId]);

		socket = new DatagramSocket();

	}

	/**
	 * Permet d'envoyer un tampon de donnees au serveur
	 * 
	 * @param tampon Tampon a envoyer
	 * @throws IOException Erreur lors de l'envoi
	 */
	private void sendPacket(byte[] tampon) throws IOException {
		System.out.println("Envoi de "+tampon.length+ "byte(s) a la banque");
		DatagramPacket packet = new DatagramPacket(tampon, tampon.length, host,
				port);

		socket.send(packet);
	}
	/**
	 * Attend la reception d'un paquet (bloquant)
	 * @return Le paquet recu
	 * @throws IOException En cas d'erreur
	 */
	private DatagramPacket receivePacket() throws IOException {
		byte[] tampon = new byte[Config.bufferSize];

		DatagramPacket packet = new DatagramPacket(tampon, tampon.length);
		System.out.println("Attente de la reponse de la banque");
		socket.receive(packet);
		
		return packet;
	}

	


	/**
	 * Permet d'ajouter un compte
	 * 
	 * @param money Montant initial
	 */
	public int addAccount(int money) {
		// TODO Reponse du serveur
		try {
			sendPacket(Toolbox.buildMessage(Menu.ADD_ACCOUNT.getCode(), money));
			
			
			DatagramPacket p = receivePacket();
			ErrorServerClient code = ErrorServerClient.fromCode(p.getData()[0]);
			// Si aucune erreur
			if(code == ErrorServerClient.OK){
				// renvoie le numero de compte
				int[] data = Toolbox.buildData(p);
				return data[0];
			}else{
				System.err.println("Plus de compte disponible ou montant incorrect");
				return -1;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;

	}

	/**
	 * Permet de supprimer un compte
	 * 
	 * @param account compte a supprimer
	 */
	public void deleteAccount(int account) {
		// TODO Auto-generated method stub
	}

	/**
	 * Ajout de l'argent a un compte
	 * 
	 * @param account Compte a crediter
	 * @param money Montant a ajouter
	 */
	public void addMoney(int account, int money) {
		// TODO Auto-generated method stub

	}

	/**
	 * Debite de l'argent a un compte
	 * 
	 * @param account Compte a debiter
	 * @param money Montant a retirer
	 */
	public void takeMoney(int account, int money) {
		// TODO Auto-generated method stub

	}

	/**
	 * Obtenir le solde du compte
	 * 
	 * @param account Compte
	 * @return Solde du compte
	 */
	public int getBalance(int account) {
		// TODO Auto-generated method stub

		return 0;
	}

}
