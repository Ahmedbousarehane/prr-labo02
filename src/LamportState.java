/**
 * Modelise les message de fonctionnement pour Lamport Le type du message et
 * l'estampille sont utilises ainsi que la conversion avec des octets.
 * 
 * @author Laurent Constantin
 * @author Jonathan Gander 
 */
public class LamportState {

	public LamportMessages type;
	public int timestamp;
	// Utilise pour l'envoi et la reception uniquement
	public int remoteBankId;

	/**
	 * Construit un message de type RELEASE et d'estampille 0.
	 * 
	 * @param type Le type du message
	 * @param timestamp Le timestamp du message
	 */
	public LamportState() {
		this(LamportMessages.RELEASE, 0);
	}

	/**
	 * Construit un message
	 * 
	 * @param type Le type du message
	 * @param timestamp L'estampille du message
	 */
	public LamportState(LamportMessages type, int timestamp) {
		this.type = type;
		this.timestamp = timestamp;
	}

	/**
	 * Definit un message
	 * 
	 * @param type Le type du message
	 * @param timestamp L'estampille du message
	 */
	public void set(LamportMessages type, int timestamp) {
		this.type = type;
		this.timestamp = timestamp;
	}

	/**
	 * Convertit le message en un tableau de byte.
	 * 
	 * @return Le tableau de byte
	 */
	public byte[] toByte(int bankId) {
		byte[] data = new byte[1 + 4 + 4];
		// Type [1]
		data[0] = type.getCode();
		// Timestamp [4]
		byte[] temp = Toolbox.int2Byte(timestamp);
		for (int i = 0; i < temp.length; i++) {
			data[1 + i] = temp[i];
		}
		// Bank id [4]
		temp = Toolbox.int2Byte(bankId);
		for (int i = 0; i < temp.length; i++) {
			data[5 + i] = temp[i];
		}		
		
		return data;
	}

	/**
	 * Lit un message LAMPORT et renvoie ses infos.
	 * 
	 * @param data Donnees
	 * @param length Longueur des donnees
	 * @return type et entropie du message
	 */
	public static LamportState fromByte(byte[] data,int length) {
		assert (length >= 9);
		// type [1]
		LamportState state = new LamportState();
		state.type = LamportMessages.fromCode(data[0]);
		// timestamp[4]
		byte[] tempInt = new byte[4];
		for (int i = 0; i < tempInt.length; i++) {
			tempInt[i] = data[i + 1];
		}
		state.timestamp = Toolbox.byte2int(tempInt);
		// Bank ID[4]
		tempInt = new byte[4];
		for (int i = 0; i < tempInt.length; i++) {
			tempInt[i] = data[i + 5];
		}	
		state.remoteBankId = Toolbox.byte2int(tempInt);

		
		return state;
	}

}
