package harvard.robobees.simbeeotic.model.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Protocol data unit wrap
 * @author Dalimir Orfanus
 *
 */
public abstract class AbstractPduWrap {

	protected ProtocolAddress source, destination;	
	protected AbstractPduWrap upperPdu;
		
	public AbstractPduWrap(ProtocolAddress source, ProtocolAddress destination, AbstractPduWrap upperPdu) {
		this.source = source;
		this.destination = destination;
		this.upperPdu = upperPdu;
	}	

	public ProtocolAddress getSource() {
		return source;
	}

	public ProtocolAddress getDestination() {
		return destination;
	}

	
	public abstract int getDataSizeByte();
	
	public int getDataSizeBit() {
		return 8 * getDataSizeByte();
	};

	/**
	 * Returns size in bytes of serialized PduWrap object.
	 * This is the size of its payload and not size of serialized java object!
	 * @param data
	 * @return
	 */
	public static int getDataSizeByte(byte[] data) {
		if(data == null)
			return 0;

		AbstractPduWrap apw = deserialize(data);
		if(apw == null)
			return 0;
		
		return apw.getDataSizeByte();
	}
	
	public static int getDataSizeBit(byte[] data) {
		return getDataSizeByte(data) * 8;
	}
	
	public static AbstractPduWrap deserialize(byte[] data) {
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			ObjectInputStream ois = new ObjectInputStream(bais);
			Object obj = ois.readObject();
			
			if(obj instanceof AbstractPduWrap) {
				return (AbstractPduWrap)obj;
			}
			
		} catch (Exception e) {
			//e.printStackTrace();
			return null;
		}
		return null;
	}
	
	public static byte[] serialize(AbstractPduWrap apw) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream( baos );
			oos.writeObject(apw);
			oos.flush();
			oos.close();
			return baos.toByteArray();
		} catch (Exception e) {
			return null;
		}		
	}

	public AbstractPduWrap getUpperPdu() {
		return upperPdu;
	}
}
