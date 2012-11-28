import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Implementation de l'algorithme de Lamport avec replication de donnees lors
 * d'une liberation
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

	private final boolean DEBUG = true;
	// Pour le mutex
	private int localTimestamp; // horloge logique
	private Boolean hasMutex = false;

	// Id de la banque courante
	final Bank bank;
	// Tableau des etats
	private LamportState[] state;

	/**
	 * Constructeur avec la banque
	 * 
	 * @param bank
	 *            Banque associee
	 * @throws SocketException
	 */
	public Lamport(Bank bank) throws SocketException {
		this.bank = bank;
		this.port = Config.bank2bankLamportPort[bank.getId()];
		System.out.println("La banque " + bank.getId() + " ecoute sur le port "
				+ port);
		socket = new DatagramSocket(port);

		// Initialise le tableau d'etat
		state = new LamportState[Config.banksAddresses.length];
		for (int i = 0; i < state.length; i++)
			state[i] = new LamportState();

		new Thread(this).start();

	}

	/**
	 * Indique si la banque courante peut entrer en section critique
	 * 
	 * @return True si la banque peut entrer en section critique
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
		sendToAllOthersBank(state[bank.getId()].toByte(this.bank.getId()));
		// Indique si l'on peut avoir le mutex

		// Si on a pas le mutex, on est en attente !
		synchronized (this) {
			hasMutex = localAccesGranted();

			while (!hasMutex) {
				try {
					if (DEBUG) {
						System.out.println("Wait() sur la banque "
								+ bank.getId());
					}
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				hasMutex = localAccesGranted();

			}
		}
	}

	/**
	 * Replication quand un compte est cree
	 * 
	 * @param account
	 *            Compte cree
	 * @param money
	 *            Argent verse initialement sur le compte
	 * @throws IOException
	 */
	public void accountCreated(int account, int money) throws IOException {

		// Infos de creation de compte
		byte[] temp = Toolbox.buildMessage(
				LamportMessages.NEW_ACCOUNT.getCode(), account, money);

		// Envoi
		sendToAllOthersBank(temp);
	}

	/**
	 * Libere le mutex et envoie des donnees dans le message de liberation
	 * 
	 * @param code
	 *            Le type de message.
	 * @param data
	 *            Les donnes.
	 * @throws IOException
	 */
	public void unlock(LamportUnlockMessage unlockType, int... data)
			throws IOException {
		System.out.println("Lamport.unlock()");

		// Construction du message a envoyer
		state[bank.getId()].set(LamportMessages.RELEASE, localTimestamp);
		byte[] messageData = state[bank.getId()].toByte(bank.getId());
		// Ajout des infos de liberation
		byte[] temp = Toolbox.buildMessage(unlockType.getCode(), data);
		// Envoi
		sendToAllOthersBank(Toolbox.concat(messageData, temp));
		synchronized (this) {
			hasMutex = false;
		}
	}

	/**
	 * Envoie un message a toutes les banques excepte soi-meme
	 * 
	 * @param data
	 *            Le message
	 * @throws IOException
	 *             En cas d'erreur
	 */
	public void sendToAllOthersBank(byte[] data) throws IOException {
		for (int i = 0; i < state.length; i++) {
			if (i != bank.getId()) {
				send(i, data);
			}
		}
	}

	/**
	 * Envoie un message a une banque
	 * 
	 * @param bankId
	 *            Id de la banque ou envoyer le message
	 * @param data
	 *            Le message
	 * @throws IOException
	 *             En cas d'erreur
	 */
	public void send(int bankId, byte[] data) throws IOException {
		// Construction de l'adresse et du datagramme
		int port = Config.bank2bankLamportPort[bankId];
		InetAddress host = InetAddress.getByName(Config.banksAddresses[bankId]);
		DatagramPacket packet = new DatagramPacket(data, data.length, host,
				port);
		// Envoi
		socket.send(packet);
	}

	/**
	 * Action du Lamport
	 */
	public void run() {
		byte[] buffer = new byte[Config.bufferSize];
		DatagramPacket data = new DatagramPacket(buffer, buffer.length);

		while (true) {
			try {
				// 1. Reception d'un message
				socket.receive(data);

				// 2. Regarde si une autre banque replique un nouveau compte
				LamportMessages type = LamportMessages
						.fromCode(data.getData()[0]);
				if (type == LamportMessages.NEW_ACCOUNT) {
					// 2b. Si oui, on l'ajout a la banque
					int[] newAccountData = Toolbox.buildData(data.getData(),
							data.getLength(), 0);
					bank.handleOnCreate(newAccountData[0], newAccountData[1]);

				} else {
					// 3. Sinon on a un message lamport normal
					LamportState state = LamportState.fromByte(data.getData(),
							data.getLength());
					acceptReceive(state, data);
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Implementation du Receive. Permet de gerer les messages de Lamport
	 * 
	 * @param remoteBankId
	 *            L'Id de la banque distante
	 * @param state
	 *            Etat de la requete
	 * @throws IOException
	 *             En cas d'erreur
	 */
	private void acceptReceive(LamportState state, DatagramPacket data)
			throws IOException {

		int remoteBankId = state.remoteBankId;

		// 1. Met a jour l'estampille
		localTimestamp = Math.max(localTimestamp, state.timestamp) + 1;

		// 2. Effectue les actions suivant le type de message recu
		switch (state.type) {
		case NEW_ACCOUNT:
			// Deja traite car pas un message lamport standard
			break;
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
			if (state.type == LamportMessages.RELEASE && data.getLength() >= 9){
				// Construction des donnees
				byte code = data.getData()[9]; // Code
				int[] releaseData = Toolbox.buildData(data.getData(),
						data.getLength() - 9, 9); // Donnee repliquee

				// Conversion en enum
				LamportUnlockMessage lum = LamportUnlockMessage.fromCode(code);

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

			}

			break;
		case RECEIPT:
			// Met a jour si on avait pas de requete dans le tableau
			if (this.state[remoteBankId].type != LamportMessages.REQUEST) {
				this.state[remoteBankId].set(LamportMessages.RECEIPT,
						state.timestamp);
			}
			break;

		}
		// Indique si l'on peut avoir le mutex
		synchronized (this) {
			hasMutex = (this.state[bank.getId()].type == LamportMessages.REQUEST)
					&& localAccesGranted();
			if (DEBUG) {
				System.out.println("Lamport.acceptReceive()");
			}
			if (hasMutex) {
				if (DEBUG)
					System.out
							.println("Notify() sur la banque " + bank.getId());
				notify();
			}
		}

	}
}
