package harvard.robobees.simbeeotic.model.protocol.IEEE802_11;

import harvard.robobees.simbeeotic.model.protocol.AbstractPduWrap;
import harvard.robobees.simbeeotic.model.protocol.ProtocolAddress;

public abstract class PhyPduWrap802_11 extends AbstractPduWrap {

	final static int LEN_FRAME_CONTROL = 2;
	final static int LEN_DURATION = 2;
	final static int LEN_MAC_ADDR = 6;
	final static int LEN_FCS = 4;
	final static int LEN_SEQUENCE_CONTROL = 2;
	
	public PhyPduWrap802_11(ProtocolAddress source, ProtocolAddress destination, AbstractPduWrap upperPdu) {
		super(source, destination, upperPdu);
	}	

	

}
