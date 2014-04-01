package harvard.robobees.simbeeotic.model.protocol.IEEE802_11;

/**
 * Class which represents Frame Control Field of 802.11
 * 
 * @author Dalimir Orfanus
 *
 */
public class FrameControlField {
	
	enum FrameType {
		Management,
		Control,
		Data,
		Reserved
	}
	
	enum FrameSubType {
		Ack,
		Rts,
		Cts,
		Data,
		Beacon,
		Reserved
	}
	
	int protocolVersion = 0;
	FrameType type = FrameType.Reserved;
	FrameSubType subType = FrameSubType.Reserved;
	
	boolean toDS = false;
	boolean fromDS = false;
	boolean moreFragments = false;
	boolean retry = false;
	boolean powerManagement = false;
	boolean moreData = false;
	boolean protectedFrame = false;
	boolean order = false;
	
	public static FrameControlField createAck() {
		FrameControlField fcf = new FrameControlField();
		fcf.setType(FrameType.Control);
		fcf.setSubType(FrameSubType.Ack);
		return fcf;
	}
	
	
	public static FrameControlField createRts() {
		FrameControlField fcf = new FrameControlField();
		fcf.setType(FrameType.Control);
		fcf.setSubType(FrameSubType.Ack);
		return fcf;		
	}

	public static FrameControlField createCts() {
		FrameControlField fcf = new FrameControlField();
		fcf.setType(FrameType.Control);
		fcf.setSubType(FrameSubType.Cts);
		return fcf;		
	}

	/**
	 * Creates Frame Control Field for regular non-fragmented data
	 * @return
	 */
	public static FrameControlField createDataNoFragment() {
		FrameControlField fcf = new FrameControlField();
		fcf.setType(FrameType.Data);
		fcf.setSubType(FrameSubType.Data);
		return fcf;		
	}

	
	public int getProtocolVersion() {
		return protocolVersion;
	}
	public void setProtocolVersion(int protocolVersion) {
		this.protocolVersion = protocolVersion;
	}
	public FrameType getType() {
		return type;
	}
	public void setType(FrameType type) {
		this.type = type;
	}
	public FrameSubType getSubType() {
		return subType;
	}
	public void setSubType(FrameSubType subType) {
		this.subType = subType;
	}
	public boolean isToDS() {
		return toDS;
	}
	public void setToDS(boolean toDS) {
		this.toDS = toDS;
	}
	public boolean isFromDS() {
		return fromDS;
	}
	public void setFromDS(boolean fromDS) {
		this.fromDS = fromDS;
	}
	public boolean isMoreFragments() {
		return moreFragments;
	}
	public void setMoreFragments(boolean moreFragments) {
		this.moreFragments = moreFragments;
	}
	public boolean isRetry() {
		return retry;
	}
	public void setRetry(boolean retry) {
		this.retry = retry;
	}
	public boolean isPowerManagement() {
		return powerManagement;
	}
	public void setPowerManagement(boolean powerManagement) {
		this.powerManagement = powerManagement;
	}
	public boolean isMoreData() {
		return moreData;
	}
	public void setMoreData(boolean moreData) {
		this.moreData = moreData;
	}
	public boolean isProtectedFrame() {
		return protectedFrame;
	}
	public void setProtectedFrame(boolean protectedFrame) {
		this.protectedFrame = protectedFrame;
	}
	public boolean isOrder() {
		return order;
	}
	public void setOrder(boolean order) {
		this.order = order;
	}

	
	
}
