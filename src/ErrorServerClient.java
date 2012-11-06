/**
 * Modelise les erreur entre le serveur et le client
 * @author Laurent Constantin
 * @author Jonathan Gander
 */

public enum ErrorServerClient {
	OK,
	MONTANT_INCORRECT,
	SOLDE_INVALIDE,
	COMPTE_INEXISTANT,
	AUTRE;
	
	public byte getCode(){
		return (byte)this.ordinal();
	}
	
	public static ErrorServerClient fromCode(byte code){
		try{
			return ErrorServerClient.values()[(int)code];
		}catch(ArrayIndexOutOfBoundsException aioobe){
			aioobe.printStackTrace();
			return null;
		}
	}
}
