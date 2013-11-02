package harvard.robobees.simbeeotic.model.comms;

import harvard.robobees.simbeeotic.model.protocol.AbstractPduWrap;

public interface RadioEventListener {

	public void notifyRadioEvent(DefaultRadioEvent event, AbstractPduWrap pdu, double rxPower, double frequency);
}
