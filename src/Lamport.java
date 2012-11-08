import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Implementation de l'algorithme de Lamport avec envoi de donnees lors d'une
 * liberation
 * 
 * @author Constantin Laurent
 * @author Gander Jonathan
 * @version 1.0
 * 
 * TODO : Gestion du reseau, mutex sur donnees locale.
 * File d'attentes FIFO.
 * 
 */
public class Lamport {
	// Pour la communication
	private DatagramSocket socket;
	private final int port;

	// Pour le mutex
	private int localTimestamp; // horloge logique
	private boolean hasMutex = false;

	// Id de la banque courante
	int bankId;
	// Tableau des etats
	private LamportState[] state;

	public Lamport(int bankId) throws SocketException {
		this.bankId = bankId;
		this.port = Config.interBankPort;
		socket = new DatagramSocket(port);

		// Initialise le tableau d'etat
		state = new LamportState[Config.banksAddresses.length];
		for (int i = 0; i < state.length; i++)
			state[i] = new LamportState();
	}

	/**
	 * Indique si la banque courante peut entrer en section critique
	 * 
	 * @return Si la banque peut entrer en section critique
	 */
	private boolean localAccesGranted() {
		// Il peut si etat[bankid]=requete
		// et que son estampille est la plus ancienne !
		if (state[bankId].type != LamportMessages.REQUEST)
			return false;

		int myTimeStamp = state[bankId].timestamp;
		for (int i = 0; i < state.length; i++) {
			if (myTimeStamp > state[i].timestamp)
				return false;
			else if (myTimeStamp == state[i].timestamp && i != bankId) {
				if (bankId > i)
					return false;
			}
		}
		return true;
	}

	/**
	 * Pour obtenir le mutex 
	 * TODO implementer
	 */
	public void lock() {

	}

	/**
	 * Libere le mutex et envoie des donnees dans le message de liberation
	 * 
	 * @param code Le type de message.
	 * @param data Les donnes. 
	 * TODO : Implementer
	 * @throws IOException
	 */
	public void unlock(LamportUnlockMessage type, int... data)
			throws IOException {
		// Construction du message a envoyer
		state[bankId].set(LamportMessages.RELEASE, localTimestamp);
		byte[] messageData = state[bankId].toByte();
		// Ajout des infos de liberation
		byte[] temp = Toolbox.buildMessage((byte) type.ordinal(), data);
		// Envoi
		sendToAll(Toolbox.concat(messageData, temp));
		hasMutex = false;
	}

	/**
	 * Envoie un message a toutes les banques excepte soi-meme
	 * 
	 * @param data Le message
	 * @throws IOException En cas d'erreur
	 */
	public void sendToAll(byte[] data) throws IOException {
		for (int i = 0; i < state.length; i++) {
			if (i == bankId)
				continue;
			send(i, data);
		}
	}

	/**
	 * Envoie un message a une banque
	 * 
	 * @param bankId Id de la banque ou envoyer le message
	 * @param data Le message
	 * @throws IOException En cas d'erreur
	 */
	public void send(int bankId, byte[] data) throws IOException {
		// Construction de l'adresse et du datagramme
		InetAddress host = InetAddress.getByName(Config.banksAddresses[bankId]);
		DatagramPacket packet = new DatagramPacket(data, data.length, host,
				port);
		// Envoi
		socket.send(packet);
	}

	/**
	 * Implementation du Receive (code accept receive d'ada)
	 * 
	 * @param remoteBankId L'Id de la banque distante
	 * @param state Etat de la requete
	 * @throws IOException En cas d'erreur
	 * TODO : Utiliser cette fonction
	 */
	private void acceptReceive(int remoteBankId, LamportState state)
			throws IOException {
		
		
		// Met a jour l'estampille
		localTimestamp = Math.max(localTimestamp, state.timestamp) + 1;
		// Effectue les actions suivant le type de message recu
		switch (state.type) {
		case REQUEST:
			// Mise a jour de la table locale
			this.state[remoteBankId].set(LamportMessages.REQUEST,
					state.timestamp);

			// Envoi du recu
			LamportState data = new LamportState(LamportMessages.RECEIPT,
					localTimestamp);

			send(remoteBankId, data.toByte());
			break;
		case RELEASE:
			// Mise a jour de la table locale
			this.state[remoteBankId].set(LamportMessages.RELEASE,
					state.timestamp);
			break;
		case RECEIPT:
			// Ignore le recu si on avait pas de demande
			if (this.state[remoteBankId].type != LamportMessages.REQUEST) {
				this.state[remoteBankId].set(LamportMessages.RECEIPT,
						state.timestamp);
			}
			break;

		}
		// Indique si l'on peut avoir le mutex
		hasMutex = (this.state[bankId].type == LamportMessages.REQUEST)
				&& localAccesGranted();
	}

	/**
	 * Envoi d'une demande d'obtention de mutex aux autres sites. Implementation
	 * du code ada "accept demande"
	 * 
	 * @throws IOException En cas d'erreur
	 * TODO : Utiliser cette fonction
	 */
	private void acceptRequest() throws IOException {
		// Mise a jour de l'estampille
		localTimestamp++;
		// Envoi d'une requete
		state[bankId].set(LamportMessages.REQUEST, localTimestamp);
		sendToAll(state[bankId].toByte());
		// Indique si l'on peut avoir le mutex
		hasMutex = localAccesGranted();
	}

	/**
	 * Renvoi le numero de la banque a partir d'un datagramme
	 * 
	 * @param p Le datagramme
	 * @return L'ID de la banque ou -1
	 * TODO A tester ! Utiliser si on code pas l'id dans LamportMessage
	 */
	public static int getBankIdFromDatagram(DatagramPacket p) {
		String address = p.getAddress().getHostAddress();
		for (int i = 0; i < Config.banksAddresses.length; i++) {
			if (Config.banksAddresses[i].compareToIgnoreCase(address) == 0)
				return i;
		}
		return -1;
	}
	
}
