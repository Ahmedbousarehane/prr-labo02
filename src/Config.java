/**
 * Configuration par defaut des adresses et ports pour les 
 * communications
 * 
 * @version 1.0
 * @author Laurent Constantin
 * @author Jonathan Gander
 */
public interface Config {
	/**
	 * Ports utilises pour les communications
	 */
	public static final int banks2ClientPorts[] = {1515,1516};
	/**
	 * IP utilises pour les communications
	 */
	public static final String banksAddresses[] = {"127.0.0.1","127.0.0.1"};
	
	/**
	 * Taille du buffer pour la recetion des donnees
	 */
	public static final int bufferSize = 256;
	/**
	 * Port utilise pour la communication entre banques (lamport)
	 */
	public static final int bank2bankLamportPort[] = {1517,1518};
	/**
	 * Utilise pour la communication entre banques sans Lamport
	 */
	public static final int bank2bank[] = {1519,1520};

}
