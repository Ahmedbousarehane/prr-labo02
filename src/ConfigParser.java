import java.util.regex.Pattern;

/**
 * Permet de parser la configuration depuis la ligne de commande En cas
 * d'erreur, on utilise les valeurs par defaut
 * 
 * @version 1.0
 * @author Laurent Constantin
 * @author Jonathan Gander
 */
public class ConfigParser implements Config {
	/**
	 * Parse une chaine et renvoie l'entier qu'elle contient.
	 * En cas d'erreur renvoie defaultInt
	 * @param args La chaine a parser
	 * @param defaultInt La valeur par default
	 * @return L'entier contenu dans la chaine
	 */
	private static int getIntFromString(String args, int defaultInt){
		if(Pattern.matches("[0-9]*", args)){
			return Integer.parseInt(args);
		}else{
			System.err.printf("Invalid number %s using %d \n",args,defaultInt );
		}	
		return defaultInt;
	}
	/**
	 * Renvoie l'entier contenu a la position "index" de la ligne de commande
	 * @param args Parametres de la ligne de commande
	 * @param index Index dans la ligne de commande
	 * @param def Valeur par defaut (en cas d'erreur)
	 * @return Entier contenu dans args[index] ou def.
	 */
	private static int getIntFromArgs(String args[], int index, int def){
		if(args.length > index){
			return getIntFromString(args[index], def);
		}
		return def;	
	}
	/**
	 * Renvoie le port client depuis la ligne de commande ou DEFAULT_PORT_CLIENTS
	 * @param args Ligne de commande
	 * @param index Index dans la ligne de commande
	 * @return Port client
	 */
	public static int getPortClientsFromArgs(String[] args,int index){
		return getIntFromArgs(args,index,DEFAULT_PORT_CLIENTS);
	}
	/**
	 * Renvoie le port serveur depuis la ligne de commande ou DEFAULT_PORT_SERVER
	 * @param args Ligne de commande
	 * @param index Index dans la ligne de commande
	 * @return Port serveur
	 */
	public static int getPortServerFromArgs(String[] args,int index){
		return getIntFromArgs(args,index,DEFAULT_PORT_SERVER);
	}
	/**
	 * Renvoie le nombre de clients depuis la ligne de commande ou DEFAULT_NBCLIENTS
	 * @param args Ligne de commande
	 * @param index Index dans la ligne de commande
	 * @return Nombre de clients
	 */
	public static int getNbClientsFromArgs(String[] args,int index){
		return getIntFromArgs(args,index,DEFAULT_NBCLIENTS);		
	}
	/**
	 * Renvoie l'hote multicast depuis la ligne de commande ou DEFAULT_MULTICAST_HOST
	 * @param args Ligne de commande
	 * @param index Index dans la ligne de commande
	 * @return Hote
	 */	
	public static String getMulticastHostFromArgs(String[] args,int index){
		if(args.length > index){
			return args[index];
		}
		return DEFAULT_MULTICAST_HOST;	
	}	
	/**
	 * Renvoie l'hote unicast depuis la ligne de commande ou DEFAULT_UNICAST_HOST
	 * @param args Ligne de commande
	 * @param index Index dans la ligne de commande
	 * @return Hote
	 */	
	public static String getUnicastHostFromArgs(String[] args,int index){
		if(args.length > index){
			return args[index];
		}
		return DEFAULT_UNICAST_HOST;	
	}
}