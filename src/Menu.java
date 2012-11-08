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
	/**
	 * Action utilisateur avec texte descriptif
	 * @param text La descritpion
	 */
	Menu(String text) {
		this.text = text;
	}

	/**
	 * Representation du menu
	 * @return la description
	 */
	public String toString() {
		return text;
	}
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
	public static Menu fromCode(byte code){
		return Menu.values()[(int)code];
	}
}
