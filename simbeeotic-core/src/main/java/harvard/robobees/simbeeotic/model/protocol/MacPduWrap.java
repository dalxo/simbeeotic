package harvard.robobees.simbeeotic.model.protocol;

/**
 * {@inheritDoc}
 * 
 * Wrap of PDU for MAC. It basically overrides generic addresses to MAC address.
 * 
 * @author Dalimir Orfanus
 * 
 */
public class MacPduWrap extends AbstractPduWrap {

	/**
	 * {@inheritDoc}
	 * @param source If not null, overrides address of the MAC.
	 * @param destination
	 * @param data
	 */
	public MacPduWrap(EthernetMacAddress source, EthernetMacAddress destination, AbstractPduWrap upperPdu) {
		super(source, destination, upperPdu);
	}

	/**
	 * {@inheritDoc}
	 */
	public EthernetMacAddress getSource() {
		return (EthernetMacAddress)source;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public EthernetMacAddress getDestination() {
		return (EthernetMacAddress)destination;
	}

	@Override
	public int getDataSizeByte() {
		return upperPdu.getDataSizeByte();
	}
	
	
}
