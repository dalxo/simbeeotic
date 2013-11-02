package harvard.robobees.simbeeotic.model.protocol;

import harvard.robobees.simbeeotic.model.Event;

public abstract class ProtocolEvent implements Event {
	
	AbstractProtocol protocol;
	
	public ProtocolEvent(AbstractProtocol protocol) {
		this.protocol = protocol;
	}

	public AbstractProtocol getProtocol() {
		return protocol;
	}
		
}
