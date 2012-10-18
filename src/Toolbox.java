import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Scanner;

/**
 * Classe utilitaire
 * 
 * @version 1.0
 * @author Laurent Constantin
 * @author Jonathan Gander
 */
public class Toolbox {
	/**
	 * Permet de convertir un long en un tableau de byte
	 * @param l long
	 * @return Tableau de byte.
	 */
	public static byte[] long2Byte(long l) {
		ByteBuffer boeuf = ByteBuffer.allocate(8);
		boeuf.order(ByteOrder.BIG_ENDIAN);
		boeuf.putLong(l);
		return boeuf.array();
	}

	/**
	 * Permet de convertir un tableau de byte en long
	 * @param b le tableau de byte
	 * @return Le long qu'il contient
	 */
	public static long byte2Long(byte[] b) {
		ByteBuffer bb = ByteBuffer.wrap(b);
		return bb.getLong();
	}
	
	
	/**
	 * @return Saisie d'une banque
	 */
	public static int readBank() {
		int n;
		try {
			Scanner in = new Scanner(System.in);
			n = in.nextInt();
		}
		catch (Exception e) {
			return -1;
		} 
		
		if (n < 0 || n > Config.banksAddresses.length - 1) 
			return -1;
		else 
			return n;
	}
	
	/**
	 * @return Menu en fonction d'un entier
	 */
	public static Menu readMenu() {
		int n;
		try {
			Scanner in = new Scanner(System.in);
			n = in.nextInt();
		}
		catch (Exception e) {
			return null;
		} 
		
		if (n < 0 || n > Menu.values().length) 
			return null;
		else 
			return Menu.values()[n];
	}
}
