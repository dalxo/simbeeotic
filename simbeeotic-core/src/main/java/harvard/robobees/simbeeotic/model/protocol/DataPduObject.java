package harvard.robobees.simbeeotic.model.protocol;


/**
 * PDU which contains object as data. There are no upper PDUs
 * @author Dalimir Orfanus
 *
 */
public class DataPduObject extends AbstractPduWrap {

	Object data;
	int length;
	
	public DataPduObject(Object data, int length) {
		super(null, null, null);		
		this.data = data;
		this.length = length;
	}

	@Override
	public int getDataSizeByte() {		
		return length;
	}

	public Object getData() {
		return data;
	}

	
}
