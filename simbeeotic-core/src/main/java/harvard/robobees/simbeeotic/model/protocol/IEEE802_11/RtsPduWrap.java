package harvard.robobees.simbeeotic.model.protocol.IEEE802_11;

import harvard.robobees.simbeeotic.model.protocol.EthernetMacAddress;

public class RtsPduWrap extends PhyPduWrap802_11 {

	FrameControlField frameControl;
	int duration;
	EthernetMacAddress RA;
	EthernetMacAddress TA;

	
	public RtsPduWrap(FrameControlField frameControl, int duration, EthernetMacAddress RA, EthernetMacAddress TA) {
		super(null, null, null);

		this.frameControl = frameControl;
		this.duration = duration;
		this.RA = RA;
		this.TA = TA;
	}

	@Override
	public int getDataSizeByte() {
		return LEN_FRAME_CONTROL + LEN_DURATION + 2*LEN_MAC_ADDR + LEN_FCS;
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

	public EthernetMacAddress getTA() {
		return TA;
	}

}
