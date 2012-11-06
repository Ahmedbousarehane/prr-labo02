/**
 * 
 */

/**
 * @author laurent
 * 
 */
public interface TellerInterface {
	/**
	 * Permet d'ajouter un compte
	 * 
	 * @param money Montant initial
	 */
	public int addAccount(int money);

	/**
	 * Permet de supprimer un compte
	 * 
	 * @param account compte a supprimer
	 */
	public void deleteAccount(int account);

	/**
	 * Ajout de l'argent a un compte
	 * 
	 * @param account Compte a crediter
	 * @param money Montant a ajouter
	 */
	public void addMoney(int account, int money);

	/**
	 * Debite de l'argent a un compte
	 * 
	 * @param account Compte a debiter
	 * @param money Montant a retirer
	 */
	public void takeMoney(int account, int money);

	/**
	 * Obtenir le solde du compte
	 * 
	 * @param account Compte
	 * @return Solde du compte
	 */
	public int getBalance(int account);

}
