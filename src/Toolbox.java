import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Scanner;

/**
 * Classe utilitaire - Conversion en byte - Saisies utilisateur
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
						+ max + " > ";

				if (max == Integer.MAX_VALUE)
					msg = "Veuillez entrer une valeur superieur a " + min
							+ " > ";

				System.out.print(msg);
			} catch (Exception e) {
				System.out.println("Erreur de saisie");
			}
		} while (true);
	}

	/**
	 * Permet de construire un message avec la methode et les donnees a envoyer
	 * 
	 * @param code Code de la methode
	 * @param datas Data a envoyer
	 * @return Message a envoyer au serveur
	 */
	public static byte[] buildMessage(byte code, int... datas) {
		byte[] message = new byte[1 + datas.length * 4];

		message[0] = code;

		int indice = 1;
		for (int i = 0; i < datas.length; i++) {
			byte[] data = Toolbox.int2Byte(datas[i]);

			for (int j = 0; j < data.length; j++) {
				message[indice++] = data[j];
			}
		}

		return message;
	}

	/**
	 * Permet de re-construire les donnees a partir du message recu Les donnes
	 * sont obtenue a partir d'un DatagramPacket
	 * 
	 * @see buildData(byte[], int)
	 * 
	 * @param packet Un datagramPacket
	 * @return les donnees du message
	 */
	public static int[] buildData(DatagramPacket packet) {
		return buildData(packet.getData(), packet.getLength(),0);
	}
	/**
	 * @return Pause jusqu'a la pression de enter
	 */
	public static void pause() {
		System.out.println("Appuyer sur ENTER pour continuer !");
		try {
			Scanner in = new Scanner(System.in);
			in.nextLine();
		} catch (Exception e) {
			}
	}
	
	/**
	 * Permet de re-construire les donnees a partir du message recu Le code est
	 * ignore (message[0]);
	 * 
	 * @param message les donnes recu
	 * @param taille du message
	 * @return les donnees du message
	 */
	public static int[] buildData(byte[] message, int length,int offset) {
		// Attention: Ne pas utiliser message.length,
		// renvoie la taille du buffer et pas du contenu

		if (length <= 1)
			throw new IllegalArgumentException();

		// le code du message se trouve dans message[0];
		// Le reste des donnees sont des entiers..

		// Si pas de donnees, renvoie un tableau vide (eviter le null)
		if ((length - 1) % 4 != 0) {
			return new int[0];
		}
		// Si des donnees
		int nbInt = (length - 1) / 4;
		int data[] = new int[nbInt];

		if (message.length > 1) {
			for (int index = 0; index < nbInt; index++) {
				byte temp[] = new byte[4];
				temp[0] = message[offset + 1 + index * 4];
				temp[1] = message[offset + 2 + index * 4];
				temp[2] = message[offset + 3 + index * 4];
				temp[3] = message[offset + 4 + index * 4];
				data[index] = Toolbox.byte2int(temp);
			}
		}
		return data;
	}

	/**
	 * Renvoie le code d'un message
	 * 
	 * @param p Un datagramme
	 * @return Le code
	 */
	public static Byte getDataCode(DatagramPacket p) {
		if (p == null || p.getLength() == 0)
			return null;

		return p.getData()[0];
	}

	/**
	 * Concatener deux tableaux de byte
	 * 
	 * @param d1 Le premier tableau
	 * @param d2 Le deuxieme tableau
	 * @return la concatenation
	 */
	public static byte[] concat(byte[] d1, byte[] d2) {
		byte[] data = new byte[d1.length + d2.length];
		for (int i = 0; i < d1.length; i++) {
			data[i] = d1[i];
		}
		for (int i = 0; i < d2.length; i++) {
			data[i + d2.length] = d2[i];
		}
		return data;
	}
}
