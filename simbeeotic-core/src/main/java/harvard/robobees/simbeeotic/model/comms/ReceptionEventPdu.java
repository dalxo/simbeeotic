package harvard.robobees.simbeeotic.model.comms;

import harvard.robobees.simbeeotic.model.Event;
import harvard.robobees.simbeeotic.model.protocol.AbstractPduWrap;

public class ReceptionEventPdu implements Event {
	
	AbstractPduWrap pdu;
	double rxPower;
	Band band;

	public ReceptionEventPdu(AbstractPduWrap pdu, double rxPower, Band band) {
		super();
		this.pdu = pdu;
		this.rxPower = rxPower;
		this.band = band;
	}

	public AbstractPduWrap getPdu() {
		return pdu;
	}
	public double getRxPower() {
		return rxPower;
	}
	public Band getBand() {
		return band;
	}
	
}
