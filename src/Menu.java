/**
 * Classe permettant la modelisation du menu client.
 * Attention, 127 elements au maximum
 * 
 * @version 1.0
 * @author Laurent Constantin
 * @author Jonathan Gander*
 */
public enum Menu {

	ADD_ACCOUNT("Creer un compte"), 
	DELETE_ACCOUNT("Supprimer un compte"), 
	ADD_MONEY("Ajouter un montant a un compte"), 
	TAKE_MONEY("Retirer un montant  a un compte"), 
	GET_BALANCE("Obtenir le solde d'un compte"),
	QUIT("Quitter");

	private final String text;

	Menu(String text) {
		this.text = text;
	}

	public String toString() {
		return text;
	}
	public byte getCode(){
		return (byte)this.ordinal();
	}
	
	public static Menu fromCode(byte code){
		try{
			return Menu.values()[(int)code];
		}catch(ArrayIndexOutOfBoundsException aioobe){
			aioobe.printStackTrace();
			return null;
		}
	}
}
