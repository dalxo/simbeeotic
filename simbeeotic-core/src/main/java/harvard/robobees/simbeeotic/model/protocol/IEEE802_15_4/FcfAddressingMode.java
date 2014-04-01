package harvard.robobees.simbeeotic.model.protocol.IEEE802_15_4;

/**
 * Enum class for bit field Addressing Mode in Frame Control Field of the 802.15.4 frame.
 * @author Dalimir Orfanus
 *
 */
public enum FcfAddressingMode {

	NotPresent	(0),
	Short		(2),
	Extended	(8);

	private final int len;
	
	FcfAddressingMode(int len) {
		this.len = len;
	}
	
	/**
	 * 
	 * @return length in bytes
	 */
	public int len() { return this.len; }
	
	
}
