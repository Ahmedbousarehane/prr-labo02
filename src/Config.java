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
	public static final String banksAddresses[] = {"192.168.2.15","192.168.2.10"};
	
	/**
	 * Taille du buffer pour la recetion des donnees
	 */
	public static final int bufferSize = 256;
	/**
	 * Port utilise pour la communication entre banques (p.ex lamport)
	 */
	public static final int bank2bankLamportPort[] = {1517,1518};

}
