package harvard.robobees.simbeeotic.model.comms;

import harvard.robobees.simbeeotic.model.Event;
import harvard.robobees.simbeeotic.model.protocol.AbstractPduWrap;

public final class EndOfTransmissionEvent implements Event {

	private AbstractPduWrap pdu;
    private double rxPower;
    private double frequency;

    public EndOfTransmissionEvent(AbstractPduWrap pdu, double rxPower, double frequency) {
		super();
		this.pdu = pdu;
		this.rxPower = rxPower;
		this.frequency = frequency;
	}

	public AbstractPduWrap getPdu() {
		return pdu;
	}

	public double getRxPower() {
		return rxPower;
	}

	public double getFrequency() {
		return frequency;
	}

    
	
}
