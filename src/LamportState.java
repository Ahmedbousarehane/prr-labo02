

/**
 * @author Laurent Constantin
 * @author Jonathan Gander
 * 
 */
public class LamportState {

	public LamportMessages type = LamportMessages.LIBERATION;
	public int timestamp = 0;
	
	/**
	 * Convertit le message en un tableau de byte.
	 * @return Le tableau de byte
	 */
	public byte[] toByte() {
		byte[] data = new byte[1 + 4];
		data[0] = type.getCode();
		byte[] temp = Toolbox.int2Byte(timestamp);
		for (int i = 0; i < temp.length; i++) {
			data[1 + i] = temp[i];
		}
		return data;
	}
	
	/**
	 * Lit un message LAMPORT et renvoie ses infos.
	 * @param data Donnees
	 * @param length Longueur des donnees
	 * @return type et entropie du message
	 */
	public static LamportState fromByte(byte[] data) {
		assert (data.length >= 5);

		LamportState state = new LamportState();
		state.type = LamportMessages.fromCode(data[0]);
		byte[] tempInt = new byte[4];
		for (int i = 0; i < tempInt.length; i++) {
			tempInt[i] = data[i + 1];
		}
		state.timestamp = Toolbox.byte2int(tempInt);
		return state;
	}

}
