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
}