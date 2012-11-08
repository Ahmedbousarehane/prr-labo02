
/**
 * @author Laurent Constantin
 * @author Jonathan Gander
 * @version 1.0
 *
 * Donnees aditionnelles envoyee lorsque on relache un mutex lamport.
 */
public enum LamportUnlockMessage {
	DELETE_ACCOUNT,
	UPDATE_MONEY
	// CREATE_ACCOUNT n'est pas fait avec un mutex..
}
