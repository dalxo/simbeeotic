package harvard.robobees.simbeeotic.model.comms;

import harvard.robobees.simbeeotic.model.Event;
import harvard.robobees.simbeeotic.model.protocol.AbstractPduWrap;

/**
 * Event indicating end of reception of currently receiving message.
 * This event is used to handle collisions of radion messages. 
 * @author Dalimir Orfanus
 *
 */
public final class EndOfReceptionEvent implements Event {

    private AbstractPduWrap pdu;
    private double rxPower;
    double frequency;
    
	public EndOfReceptionEvent(ReceptionEventPdu event) {
    	super();
    	this.pdu = event.getPdu();
    	this.rxPower = event.getRxPower();
    	this.frequency = event.getBand().getCenterFrequency();
    }
    
	public EndOfReceptionEvent(AbstractPduWrap pdu, double rxPower, double frequency) {
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
