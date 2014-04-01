package harvard.robobees.simbeeotic.model.protocol;


/**
 * PDU which contains only data as byte array. There are no upper PDUs
 * @author Dalimir Orfanus
 *
 */
public class DataPduByte extends AbstractPduWrap {

	byte[] data;
	
	public DataPduByte(byte[] data) {
		super(null, null, null);		
		this.data = data;
	}

	@Override
	public int getDataSizeByte() {		
		return data != null ? data.length : 0;
	}

	
}
