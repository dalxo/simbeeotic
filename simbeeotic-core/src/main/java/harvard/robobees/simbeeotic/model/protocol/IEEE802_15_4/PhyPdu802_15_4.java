package harvard.robobees.simbeeotic.model.protocol.IEEE802_15_4;

import harvard.robobees.simbeeotic.model.protocol.AbstractPduWrap;

public abstract class PhyPdu802_15_4 extends AbstractPduWrap {
	
	FrameControlField frameControlField;
	int sequenceNumber;
	
	final static int LEN_FRAME_CONTROL = 2;
	final static int LEN_SEQ_NUM = 1;
	final static int LEN_FCS = 2;
	final static int LEN_ADDR_SHORT = 2;
	final static int LEN_ADDR_EXT = 8;
	
	
	public PhyPdu802_15_4(MacAddress802_15_4 source, MacAddress802_15_4 destination, 
			AbstractPduWrap upperPdu, FrameControlField frameControlField, int sequenceNumber) {
		super(source, destination, upperPdu);
		
		this.frameControlField = frameControlField;
		this.sequenceNumber = sequenceNumber;
	}


	public FrameControlField getFrameControlField() {
		return frameControlField;
	}

	public int getSequenceNumber() {
		return sequenceNumber;
	}

	public MacAddress802_15_4 getSource() {
		return (MacAddress802_15_4) this.source;
	}
	
	public MacAddress802_15_4 getDestination() {
		return (MacAddress802_15_4) this.destination;
	}
}
