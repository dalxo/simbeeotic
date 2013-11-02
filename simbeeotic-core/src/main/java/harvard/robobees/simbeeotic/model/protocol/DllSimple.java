package harvard.robobees.simbeeotic.model.protocol;

import harvard.robobees.simbeeotic.SimTime;
import harvard.robobees.simbeeotic.model.comms.AbstractRadio;

public class DllSimple extends AbstractProtocol {

	AbstractRadio radio;

	 /**
     * {@inheritDoc}
     */
	public void initialize() {
		
		setLayerType(LayerType.DLL);
	}

	 /**
     * {@inheritDoc}
     */
	public void finish() {
		// TODO Auto-generated method stub

	}

	@Override
	public void lowerSAP(AbstractProtocol sender, AbstractPduWrap pdu) {
		// Ignore, we do not have lowerSAP, just radio
	}

	public AbstractRadio getRadio() {
		return radio;
	}

	public void setRadio(AbstractRadio radio) {
		this.radio = radio;
	}

	 /**
     * {@inheritDoc}
     */
	@Override
	public void upperSAP(AbstractProtocol sender, AbstractPduWrap pdu) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getMaxDataPayload() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void handleEvent(SimTime simTime, ProtocolEvent pe) {
		// TODO Auto-generated method stub
		
	}

}
