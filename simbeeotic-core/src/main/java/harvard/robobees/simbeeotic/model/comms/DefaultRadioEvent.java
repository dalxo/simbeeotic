package harvard.robobees.simbeeotic.model.comms;

public enum DefaultRadioEvent {

	LISTEN,
	
	//END_OF_TX, we do not need this, we know when we request to transmist
	
	START_RX,
	
	RX_RECEIVED,
	
	RX_COLLISION,
	
	ERROR
	
}
