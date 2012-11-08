
/**
 * @author Constantin Laurent
 * @author Jonathan Gander
 * @version 1.0
 * 
 * Messages utilises pour le transfert dans l'algorithme de Lamport
 * Attention, 127 elements au maximum

 */
public enum LamportMessages {
	RELEASE,
	REQUEST,
	RECEIPT;
	
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
	public static LamportMessages fromCode(byte code){
		return LamportMessages.values()[(int)code];
	}
}
