package harvard.robobees.simbeeotic.model.protocol.IEEE802_15_4;

/**
 * Class which represents Frame Control Field of 802.15.4
 * 
 * @author Dalimir Orfanus
 *
 */
public class FrameControlField {
	
	enum FrameType {
		Beacon,
		Data,
		Ack,
		Command
	}
		
	enum FrameVersion {
		
		/**
		 * IEEE 802.15.4-2003 compatible mode
		 */
		Compatible,
		
		/**
		 * IEEE 802.15.4 frame
		 */
		Vanilla 
	}
	
	FrameType type = FrameType.Data;
	boolean securityEnabled = false;
	boolean framePending = false;
	boolean ackRequest = true;
	boolean panIdCompression = false;	
	FcfAddressingMode dstAddrMode = FcfAddressingMode.Short;
	FcfAddressingMode srcAddrMode = FcfAddressingMode.Short;
	FrameVersion frameVersion = FrameVersion.Vanilla;
	
	/**
	 * Creates Frame Control for Acknowledgment frame
	 * @return
	 */
	public static FrameControlField createAck() {
		FrameControlField fcf = new FrameControlField();
		fcf.setType(FrameType.Ack);		
		return fcf;
	}

	/**
	 * Creates Frame Control field 
	 * @param acked Is ACK requested?
	 * @param mode Addressing mode for SRC and DST address
	 * @return
	 */
	public static FrameControlField createData(boolean acked, FcfAddressingMode mode) {
		FrameControlField fcf = new FrameControlField();
		fcf.setType(FrameType.Data);
		fcf.setAckRequest(acked);
		fcf.setSrcAddrMode(mode);
		fcf.setDstAddrMode(mode);
		return fcf;
	}	

	

	public FrameType getType() {
		return type;
	}


	public void setType(FrameType type) {
		this.type = type;
	}


	public boolean isSecurityEnabled() {
		return securityEnabled;
	}


	public void setSecurityEnabled(boolean securityEnabled) {
		this.securityEnabled = securityEnabled;
	}


	public boolean isFramePending() {
		return framePending;
	}


	public void setFramePending(boolean framePending) {
		this.framePending = framePending;
	}


	public boolean isAckRequest() {
		return ackRequest;
	}


	public void setAckRequest(boolean ackRequest) {
		this.ackRequest = ackRequest;
	}


	public boolean isPanIdCompression() {
		return panIdCompression;
	}


	public void setPanIdCompression(boolean panIdCompression) {
		this.panIdCompression = panIdCompression;
	}


	public FcfAddressingMode getDstAddrMode() {
		return dstAddrMode;
	}


	public void setDstAddrMode(FcfAddressingMode dstAddrMode) {
		this.dstAddrMode = dstAddrMode;
	}


	public FcfAddressingMode getSrcAddrMode() {
		return srcAddrMode;
	}


	public void setSrcAddrMode(FcfAddressingMode srcAddrMode) {
		this.srcAddrMode = srcAddrMode;
	}


	public FrameVersion getFrameVersion() {
		return frameVersion;
	}


	public void setFrameVersion(FrameVersion frameVersion) {
		this.frameVersion = frameVersion;
	}

		
}
