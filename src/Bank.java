import java.io.*;
import java.net.*;

/**
 * Laboratoire No 2 PRR
 * Auteurs : L. Constantin & J.Gander
 * Date : octobre-novembre 2012
 * OS : Mac OS X 10.7 et 10.8
 * 
 * DESCRIPTION
 
 * 
 * ANALYSE
 
 * REMARQUES

 * TESTS

 * 
 * STRUCTURE DU PROGRAMME
 * Config.java : valeurs par defaut pour utiliser le programme
 * ConfigParser : parser la ligne de commande pour creer la config
 * Labo02 : lance des clients et un serveur dans des threads (tests)
 * ...
 * 
 * UTILISATION
 
 * @version 1.0
 * @author Laurent Constantin
 * @author Jonathan Gander
 */
public class Bank {
	public Bank(){}
	
	/**
	 * Permet d'instancier un serveur
	 * @param args MulticastHost, port serveur, port client, nombre de clients
	 */
	public static void main(String[] args) {
		// java -jar Bank.jar ...
		new Bank();
	}
}
