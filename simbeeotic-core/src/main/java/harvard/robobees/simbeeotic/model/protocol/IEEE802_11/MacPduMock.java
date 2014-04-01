package harvard.robobees.simbeeotic.model.protocol.IEEE802_11;

import harvard.robobees.simbeeotic.model.protocol.EthernetMacAddress;
import harvard.robobees.simbeeotic.model.protocol.MacPduWrap;

public class MacPduMock extends MacPduWrap {

	byte[] data; 
	
	public MacPduMock(EthernetMacAddress source,
			EthernetMacAddress destination, byte[] data) {
		super(source, destination, null);
		
		this.data = data;
	}

	@Override
	public int getDataSizeByte() {
		return data.length;
	}


	public byte[] getData() {
		return data;
	}
}
