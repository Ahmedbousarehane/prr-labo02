
/**
 * Donnees aditionnelles envoyees lorsque on relache un mutex Lamport.
 * Attention, 127 elements au maximum
 * @author Laurent Constantin
 * @author Jonathan Gander
 * @version 1.0
 */
public enum LamportUnlockMessage {
	DELETE_ACCOUNT,
	UPDATE_MONEY;
	
	/**
	 * Permet d'obtenir le code de l'element pour un transfert reseau
	 * @return Le code en byte
	 */
	public byte getCode(){
		return (byte)this.ordinal();
	}
	/**
	 * Reconsruit l'element depuis le code (sans verifications)
	 * @param code Le code (ordinal)
	 * @return L'element de l'enum
	 */
	public static LamportUnlockMessage fromCode(byte code){
		return LamportUnlockMessage.values()[(int)code];
	}
}
