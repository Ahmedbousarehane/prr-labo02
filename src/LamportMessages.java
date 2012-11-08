
/**
 * @author Constantin Laurent
 * @author Jonathan Gander
 * @version 1.0
 */
public enum LamportMessages {
	LIBERATION,
	REQUETE,
	QUITANCE;
	
	public byte getCode(){
		return (byte)this.ordinal();
	}
	
	public static LamportMessages fromCode(byte code){
		try{
			return LamportMessages.values()[(int)code];
		}catch(ArrayIndexOutOfBoundsException aioobe){
			aioobe.printStackTrace();
			return null;
		}
	}
}
