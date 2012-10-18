import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

/**
 * Client.
 *  
 * @version 1.0
 * @author Laurent Constantin
 * @author Jonathan Gander
 */
public class Client {
	
	public Client(){
	
	}

	public static void main(String[] args) {
		// java -jar Client.jar host portClient unicastHost portServer
		new Client();
	}
}
