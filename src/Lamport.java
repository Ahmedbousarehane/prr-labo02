
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Implementation de l'algorithme de Lamport
 * Avec envoi de donnees lors d'une liberation
 * 
 * @author Constantin Laurent
 * @author Gander Jonathan
 * @version 1.0
 */
public class Lamport{
	// Pour la communication
	private DatagramSocket socket;
	private final int port;
	
	// Pour le mutex
	private int localTimestamp; // horloge logique
	private boolean hasMutex = false;
		
	int bankId;
	private LamportState[] state = new LamportState[Config.banksAddresses.length];
	
	public Lamport(int bankId) throws SocketException{
		this.bankId = bankId;
		this.port = Config.interBankPort;
		socket = new DatagramSocket(port);
		
		for(int i = 0 ; i < state.length;i++)
			state[i] = new LamportState();
	}
	
	/**
	 * Indique si le site peut entrer en section critique
	 * @return Si le site peut entrer en section critique
	 */
	private boolean accesGranted(){
		// Il peut si etat[bankid]=requete
		// et que son estampille est la plus ancienne !
		if(state[bankId].type != LamportMessages.REQUETE)
			return false;
		
		int myTimeStamp = state[bankId].timestamp;
		for(int i = 0 ; i < state.length;i++){
			if(myTimeStamp > state[i].timestamp)
				return false;
			else if(myTimeStamp == state[i].timestamp && i!=bankId){
				// TODO 
			}
		}
		return true;	
	}
	/**
	 * Pour obtenir le mutex
	 * TODO implementer
	 */
	public void lock(){
		
	}
	


	
	
	/**
	 * Libere le mutex et envoie des donnees dans le message de liberation
	 * @param code Le type de message.
	 * @param data Les donnes.
	 * TODO : Implementer
	 */
	public void unlock(LamportUnlockMessage type, int ...data){
		
	}
	/**
	 * Envoie un message a toutes les banques excepte soi-meme
	 * @param data Le message
	 * @throws IOException En cas d'erreur
	 */
	public void sendToAll(byte[] data) throws IOException{
		for(int i = 0 ; i < state.length;i++){
			if(i==bankId) continue;
			
			InetAddress	host = InetAddress.getByName(Config.banksAddresses[i]);
			DatagramPacket packet = new DatagramPacket(data,data.length,host,port);
			
			socket.send(packet);
		}	
	}

}
