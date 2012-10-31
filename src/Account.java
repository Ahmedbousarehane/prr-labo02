/**
 * Classe permetant la gestion d'un numero de compte
 * 
 * @version 1.0
 * @author Laurent Constantin
 * @author Jonathan Gander*
 */
public class Account {

	/**
	 * @param bank Numero de la banque
	 * @return Lit un numero de compte
	 */
	public static int readAccount(int bank) {
		int a = Toolbox.readInt(0, (int)(Math.pow(2, 25) - 1));
		
		return serializeAccount(bank, a);
	}

	/**
	 * @param account An account number
	 * @return account id
	 */
	public static int getAccountId(int account){
		account =  account <<  8;
		return account >>> 8;
	}
	
	/**
	 * @param account An account number
	 * @return bankId
	 */
	public static int getBankId(int account){
		return account >> 24;
	}
	
	/**
	 * Retourne un numero de compte pour une banque donnee
	 * @param bank Numero de la banque
	 * @param account Numero du compte dans la banque
	 * @return Numero de compte pour la banque
	 */
	public static int serializeAccount(int bank, int account) {
		if (bank > (int)(Math.pow(2,9) - 1)) {
			throw new IllegalArgumentException("Le numero de banque est trop grand !");
		}
		if (account < 0 || account > (int)(Math.pow(2, 25) - 1)) {
			throw new IllegalArgumentException("Le numero de compte n'est pas valide !");
		}
		
		bank = bank << 24;
		return bank + account;
	}
}
