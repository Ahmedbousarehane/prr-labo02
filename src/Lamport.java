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
 */
public class Lamport implements Runnable {
	// Pour la communication
	private DatagramSocket socket;
	private final int port;

	// Pour le mutex
	private int localTimestamp; // horloge logique
	private Boolean hasMutex = false;

	// Id de la banque courante
	final Bank bank;
	// Tableau des etats
	private LamportState[] state;

	public Lamport(Bank bank) throws SocketException {
		this.bank = bank;
		this.port = Config.bank2bankLamportPort[bank.getId()];
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
		if (state[bank.getId()].type != LamportMessages.REQUEST)
			return false;

		int myTimeStamp = state[bank.getId()].timestamp;
		for (int i = 0; i < state.length; i++) {
			if (myTimeStamp > state[i].timestamp) {
				return false;
			} else if (myTimeStamp == state[i].timestamp && i != bank.getId()) {
				if (bank.getId() > i)
					return false;
			}
		}
		System.out.println("Lamport.localAccesGranted() : Pour la banque "+bank.getId());
		return true;
	}

	/**
	 * Pour obtenir le mutex
	 * 
	 * @throws IOException
	 */
	public void lock() throws IOException {
		System.out.println("Lamport.lock()");
		// Mise a jour de l'estampille
		localTimestamp++;
		// Envoi d'une requete
		state[bank.getId()].set(LamportMessages.REQUEST, localTimestamp);
		sendToAll(state[bank.getId()].toByte(this.bank.getId()));
		// Indique si l'on peut avoir le mutex
		hasMutex = localAccesGranted();

		// Si on a pas le mutex, on est en attente !
		if (!hasMutex) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Libere le mutex et envoie des donnees dans le message de liberation
	 * 
	 * @param code Le type de message.
	 * @param data Les donnes.
	 * @throws IOException
	 */
	public void unlock(LamportUnlockMessage type, int... data)
			throws IOException {
		System.out.println("Lamport.unlock()");
		// Construction du message a envoyer
		state[bank.getId()].set(LamportMessages.RELEASE, localTimestamp);
		byte[] messageData = state[bank.getId()].toByte(bank.getId());
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
			if (i == bank.getId())
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

	public void run() {
		byte[] buffer = new byte[Config.bufferSize];
		DatagramPacket data = new DatagramPacket(buffer, buffer.length);

		while (true) {
			try {
				System.out.println("Lamport.run() : Attente");
				socket.receive(data);

				LamportState state = LamportState.fromByte(data.getData(),
						data.getLength());
				System.out.println("Lamport.run() : Recu action "+state.type);

				acceptReceive(state, data);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Implementation du Receive (code accept receive d'ada)
	 * 
	 * @param remoteBankId L'Id de la banque distante
	 * @param state Etat de la requete
	 * @throws IOException En cas d'erreur
	 */
	private void acceptReceive(LamportState state, DatagramPacket data)
			throws IOException {

		int remoteBankId = state.remoteBankId;

		// Met a jour l'estampille
		localTimestamp = Math.max(localTimestamp, state.timestamp) + 1;
		// Effectue les actions suivant le type de message recu
		switch (state.type) {
		case REQUEST:
			// Mise a jour de la table locale
			this.state[remoteBankId].set(LamportMessages.REQUEST,
					state.timestamp);

			// Envoi du recu
			LamportState data2send = new LamportState(LamportMessages.RECEIPT,
					localTimestamp);

			send(remoteBankId, data2send.toByte(bank.getId()));
			break;
		case RELEASE:
			// Mise a jour de la table locale
			this.state[remoteBankId].set(LamportMessages.RELEASE,
					state.timestamp);
			// Recupe des donnees en plus dans le message de release
			if (state.type == LamportMessages.RELEASE && data.getLength() >= 9) {
				// Construction des donnees
				byte code = data.getData()[8];
				int[] releaseData = Toolbox.buildData(data.getData(),
						data.getLength() - 9, 9);

				// Conversion en enum
				try {
					LamportUnlockMessage lum = LamportUnlockMessage
							.fromCode(code);
					// Mise a jour de la banque suivant les donnees recues

					switch (lum) {
					case DELETE_ACCOUNT:
						bank.handleOnDelete(releaseData[0]);
						break;
					case UPDATE_MONEY:
						bank.handleOnUpdate(releaseData[0], releaseData[1]);
						break;
					default:
						System.err
								.println("LamportRelease: action non implementee");
						break;
					}
				} catch (ArrayIndexOutOfBoundsException aioobe) {
					System.err.println("LamportRelease: Erreur de conversion");
					aioobe.printStackTrace();
				}

			}

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
		hasMutex = (this.state[bank.getId()].type == LamportMessages.REQUEST)
				&& localAccesGranted();

	}
}
