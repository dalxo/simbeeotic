package harvard.robobees.simbeeotic.model.protocol.IEEE802_15_4;

import harvard.robobees.simbeeotic.model.protocol.AbstractPduWrap;

/**
 * Wrap for DATA frames at physical layer for 802.15.4
 * @author Dalimir Orfanus
 *
 */
public class DataPhyPdu extends PhyPdu802_15_4 {
			
	public DataPhyPdu(MacAddress802_15_4 source, MacAddress802_15_4 destination,
			FrameControlField frameControlField, int sequenceNumber, AbstractPduWrap upperPdu) {
		super(source, destination, upperPdu, frameControlField, sequenceNumber);
	}

	@Override
	public int getDataSizeByte() {
		int tmp = LEN_FRAME_CONTROL + LEN_SEQ_NUM + LEN_FCS + upperPdu.getDataSizeByte();
	
		tmp += frameControlField.getDstAddrMode().len();
		tmp += frameControlField.getSrcAddrMode().len();
		
		return tmp;
	}

}
