import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;

/**
 * Represente un guichetier qui fait les requetes aux banques
 * 
 * @version 1.0
 * @author Laurent Constantin
 * @author Jonathan Gander
 */
public class Teller {
	private int bankId;
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

		this.bankId = bankId;

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
		DatagramPacket packet = new DatagramPacket(tampon, tampon.length, host,
				port);

		socket.send(packet);
	}

	private byte[] receivedPacket() throws IOException {
		// TODO vérifier taille
		byte[] tampon = new byte[256];

		DatagramPacket packet = new DatagramPacket(tampon, tampon.length);
		socket.receive(packet);

		return packet.getData();
	}

	/**
	 * Permet de construire un message avec la methode et les donnees a envoyer
	 * 
	 * @param code Code de la methode
	 * @param datas Data a envoyer
	 * @return Message a envoyer au serveur
	 */
	private byte[] buildMessage(byte code, int... datas) {
		byte[] message = new byte[1 + datas.length * 4];

		message[0] = code;

		int indice = 1;
		for (int i = 0; i < datas.length; i++) {
			byte[] data = Toolbox.int2Byte(datas[i]);

			for (int j = 0; j < data.length; j++) {
				message[indice++] = data[j];
			}
		}

		return message;
	}

	/**
	 * Permet de re-construire les donnees a partir du message recu
	 * Le code est ignore (message[0]);
	 * 
	 * @param message les donnes recu
	 * @return les donnees ou null
	 */
	private int[] buildData(byte[] message) {
		if (message.length <= 1)
			throw new IllegalArgumentException();

		// le code du message se trouve dans message[0];
		// Le reste des data sont des entiers..
		
		// We dont have data
		if ((message.length - 1) % 4 != 0 ) {
			return null;
		}
		// we have some data
		int nbInt = (message.length - 1) / 4;
		int data[] = new int[nbInt];

		if (message.length > 1) {
			for (int index = 0; index < nbInt; index++) {
				byte temp[] = new byte[4];
				temp[0] = message[1 + index*4];
				temp[1] = message[2 + index*4];
				temp[2] = message[3 + index*4];
				temp[3] = message[4 + index*4];
				data[index] = Toolbox.byte2int(temp);
			}
		}
		return data;
	}

	/**
	 * Permet d'ajouter un compte
	 * 
	 * @param money Montant initial
	 */
	public void addAccount(int money) {
		// TODO Contacter le serveur
		byte[] message = buildMessage((byte) 0, money);
		int[]data = buildData(message);
		// TODO : Remove debug
		System.out.println("Code:"+message[0]);
		System.out.println("Datas: ");
		for(int i = 0 ; data !=null && i < data.length;i++)
			System.out.println(data[i]);
	}

	/**
	 * Permet de supprimer un compte
	 * 
	 * @param account compte a supprimer
	 * @return true si suppression ok, false sinon
	 */
	public boolean deleteAccount(int account) {
		// TODO Auto-generated method stub
		byte[] message = buildMessage((byte) 1, account);
		System.out.println(message);
		return true;
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
