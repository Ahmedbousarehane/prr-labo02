import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Scanner;

/**
 * Classe utilitaire
 * - Conversion en byte
 * - Saisies utilisateur
 * 
 * @version 1.0
 * @author Laurent Constantin
 * @author Jonathan Gander
 */
public class Toolbox {
	/**
	 * Permet de convertir un long en un tableau de byte
	 * 
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
	 * 
	 * @param b le tableau de byte
	 * @return Le long qu'il contient
	 */
	public static long byte2Long(byte[] b) {
		ByteBuffer bb = ByteBuffer.wrap(b);
		return bb.getLong();
	}
	
	/**
	 * Permet de convertir un int en un tableau de byte
	 * 
	 * @param i int
	 * @return Tableau de byte.
	 */
	public static byte[] int2Byte(int i) {
		ByteBuffer boeuf = ByteBuffer.allocate(4);
		boeuf.order(ByteOrder.BIG_ENDIAN);
		boeuf.putInt(i);
		return boeuf.array();
	}

	/**
	 * Permet de convertir un tableau de byte en int
	 * 
	 * @param b le tableau de byte
	 * @return Le int qu'il contient
	 */
	public static int byte2int(byte[] b) {
		ByteBuffer bb = ByteBuffer.wrap(b);
		return bb.getInt();
	}

	/**
	 * @return Saisie d'une banque
	 */
	public static int readBank() {
		int n;
		try {
			Scanner in = new Scanner(System.in);
			n = in.nextInt();
		} catch (Exception e) {
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
		} catch (Exception e) {
			return null;
		}

		if (n < 0 || n > Menu.values().length)
			return null;
		else
			return Menu.values()[n];
	}

	/**
	 * Lit la saisie d'un int compris entre min et max
	 * 
	 * @param min valeur min
	 * @param max valeur max
	 * @return int compris entre min et max (inclus)
	 */
	public static int readInt(int min, int max) {
		if (min > max) {
			int tmp = max;
			max = min;
			min = tmp;
		}

		int n;

		do {
			try {
				Scanner in = new Scanner(System.in);
				n = in.nextInt();

				if (n >= min && n <= max) {
					return n;
				}
				String msg = "Veuillez entrer une valeur entre " + min + " et "
						+ max + "> ";

				if (max == Integer.MAX_VALUE)
					msg = "Veuillez entrer une valeur supperieur a " + min
							+ "> ";

				System.out.print(msg);
			} catch (Exception e) {
				System.out.println("Erreur de saisie");
			}
		} while (true);
	}
}
