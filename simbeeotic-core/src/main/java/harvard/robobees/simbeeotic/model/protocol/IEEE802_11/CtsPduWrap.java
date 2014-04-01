package harvard.robobees.simbeeotic.model.protocol.IEEE802_11;

import harvard.robobees.simbeeotic.model.protocol.EthernetMacAddress;

public class CtsPduWrap extends PhyPduWrap802_11 {

	FrameControlField frameControl;
	int duration;
	EthernetMacAddress RA;

	
	public CtsPduWrap(FrameControlField frameControl, int duration, EthernetMacAddress rA) {		
		super(null, null, null);
		this.frameControl = frameControl;
		this.duration = duration;
		RA = rA;
	}


	@Override
	public int getDataSizeByte() {
		return LEN_FRAME_CONTROL + LEN_DURATION + LEN_MAC_ADDR + LEN_FCS;
	}


	public FrameControlField getFrameControl() {
		return frameControl;
	}


	public int getDuration() {
		return duration;
	}


	public EthernetMacAddress getRA() {
		return RA;
	}

}
