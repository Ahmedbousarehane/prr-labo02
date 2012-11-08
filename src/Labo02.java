
/**
 * Labo02 - Permet d'instancier 2 banques et 1 client.
 * Pour lancer le serveur et les clients dans des processus differents, utiliser
 * les fichiers jars. Cette classe sert uniquement de test et n'est pas a
 * utilisee pour le labo final. Pour un bon fonctionnement, il faut lancer les
 * fichiers .jar separement.
 * 
 * @version 1.0
 * @author Laurent Constantin
 * @author Jonathan Gander
 */
public class Labo02 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Test du labo\nCreation des banques");
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					new Bank(0);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}).start();
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					new Bank(1);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}).start();
		System.out.println("Creation du client");

		Thread t =new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					String args[] = {};
					Client.main(args);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		});
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
