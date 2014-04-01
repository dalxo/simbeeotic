package harvard.robobees.simbeeotic.model.protocol.IEEE802_15_4;

/**
 * Wrap for ACK frames at physical layer for 802.15.4
 * @author Dalimir Orfanus
 *
 */
public class AckPhyPdu extends PhyPdu802_15_4 {
	
	public AckPhyPdu(MacAddress802_15_4 source, MacAddress802_15_4 destination, 
			FrameControlField frameControlField, int sequenceNumber) {
		super(source, destination, null, frameControlField, sequenceNumber);

	}

	@Override
	public int getDataSizeByte() {
		
		return LEN_FRAME_CONTROL + LEN_SEQ_NUM + LEN_FCS;
	}

}
