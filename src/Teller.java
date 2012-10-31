import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
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

	/**
	 * Constructeur
	 * 
	 * @param bankId Id de la banque a creer
	 * @throws UnknownHostException Si la banque ne peut pas etre trouvee sur le
	 *             reseau
	 */
	public Teller(int bankId) throws UnknownHostException {
		if (bankId < 0 || bankId > Config.banksAddresses.length - 1)
			throw new IllegalArgumentException(
					"No de banque invalide pour le guichetier !");

		this.bankId = bankId;

		port = Config.banksPorts[bankId];
		host = InetAddress.getByName(Config.banksAddresses[bankId]);
	}

	/**
	 * Permet d'ajouter un compte
	 * 
	 * @param money Montant initial
	 */
	public void addAccount(int money) {
		// TODO Contacter le serveur
	}

	/**
	 * Permet de supprimer un compte
	 * 
	 * @param account compte a supprimer
	 * @return true si suppression ok, false sinon
	 */
	public boolean deleteAccount(int account) {
		// TODO Auto-generated method stub
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
