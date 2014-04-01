package harvard.robobees.simbeeotic.model.protocol.IEEE802_11;

import harvard.robobees.simbeeotic.model.protocol.AbstractPduWrap;
import harvard.robobees.simbeeotic.model.protocol.EthernetMacAddress;

public class DataPduWrap extends PhyPduWrap802_11 {

	FrameControlField frameControl;
	int duration;
	EthernetMacAddress addr1;
	EthernetMacAddress addr2;
	EthernetMacAddress addr3;
	EthernetMacAddress addr4;
	
	public DataPduWrap(EthernetMacAddress src, EthernetMacAddress dst, 
			FrameControlField frameControl, AbstractPduWrap upperPdu, int duration) {
		super(null, null, upperPdu);		
		this.addr1 = src;
		this.addr2 = dst;
		this.frameControl = frameControl;
		this.duration = duration;
	}
	
	@Override
	public int getDataSizeByte() {
		return LEN_FRAME_CONTROL + LEN_DURATION + 3*LEN_MAC_ADDR 
				+ LEN_SEQUENCE_CONTROL
				+ LEN_FCS
				+ upperPdu.getDataSizeByte();
	}

	public FrameControlField getFrameControl() {
		return frameControl;
	}

	public int getDuration() {
		return duration;
	}

	public EthernetMacAddress getAddr1() {
		return addr1;
	}

	public EthernetMacAddress getAddr2() {
		return addr2;
	}

	public EthernetMacAddress getAddr3() {
		return addr3;
	}

	public EthernetMacAddress getAddr4() {
		return addr4;
	}

}
