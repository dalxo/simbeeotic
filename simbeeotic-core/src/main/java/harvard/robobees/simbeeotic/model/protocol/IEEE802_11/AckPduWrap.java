package harvard.robobees.simbeeotic.model.protocol.IEEE802_11;

import harvard.robobees.simbeeotic.model.protocol.EthernetMacAddress;

public class AckPduWrap extends PhyPduWrap802_11 {
	
	FrameControlField frameControl;
	int duration;
	EthernetMacAddress RA;
		
	
	public AckPduWrap(FrameControlField frameControl, int duration, EthernetMacAddress RA) {
		super(null, null, null);
		this.frameControl = frameControl;
		this.duration = duration;
		this.RA = RA;
	}
		
	@Override
	public int getDataSizeByte() {
		return LEN_FRAME_CONTROL + LEN_DURATION + LEN_MAC_ADDR + LEN_FCS; 
	}

	public EthernetMacAddress getRA() {
		return RA;
	}

	public int getDuration() {
		return duration;
	}
	
}
