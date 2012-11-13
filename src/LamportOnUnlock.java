/**
 * 
 */

/**
 * @author laurent
 *
 */
public interface LamportOnUnlock {
	public void handleOnUpdate(int account, int money);
	public void handleOnDelete(int account);
	public void handleOnCreate(int account,int money);
}	
