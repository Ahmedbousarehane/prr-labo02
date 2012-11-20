/**
 * Modelise les erreurs entre le serveur et le client
 * Attention : 127 elements maximum
 * @author Laurent Constantin
 * @author Jonathan Gander
 */

public enum ErrorServerClient {
	OK,
	MONTANT_INCORRECT,
	SOLDE_INVALIDE,
	COMPTE_INEXISTANT,
	AUTRE;
	
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
	public static ErrorServerClient fromCode(byte code){
		return ErrorServerClient.values()[(int)code];
	}
}
