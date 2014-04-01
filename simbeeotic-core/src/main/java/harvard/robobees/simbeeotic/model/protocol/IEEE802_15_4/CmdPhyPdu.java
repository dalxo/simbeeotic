package harvard.robobees.simbeeotic.model.protocol.IEEE802_15_4;

import harvard.robobees.simbeeotic.model.protocol.AbstractPduWrap;

/**
 * Wrap for MAC command frames at physical layer for 802.15.4. It has the same
 * header as data frame, only difference is extra byte in the payload. Thus, this
 * class is derived from the {@link DataPhyPdu}
 * @author Dalimir Orfanus
 *
 */
public class CmdPhyPdu extends DataPhyPdu {

	enum CommandIdentifier {
		AssociationRequest,
		AssociationResponse,
		DisassociationNotification,
		DataRequest,
		PanIdConflictNotification,
		OrphanNotification,
		BeaconRequest,
		CoordinatorRealignment,
		GtsRequest,		
		
	};
	
	CommandIdentifier commandFrameIdentifier;
	
	public CmdPhyPdu(MacAddress802_15_4 source, MacAddress802_15_4 destination,
			FrameControlField frameControlField, int sequenceNumber,
			AbstractPduWrap upperPdu, CommandIdentifier commandFrameIdentifier) {
		super(source, destination, frameControlField, sequenceNumber, upperPdu);

		this.commandFrameIdentifier = commandFrameIdentifier;
	}

	public CommandIdentifier getCommandFrameIdentifier() {
		return commandFrameIdentifier;
	}

}
