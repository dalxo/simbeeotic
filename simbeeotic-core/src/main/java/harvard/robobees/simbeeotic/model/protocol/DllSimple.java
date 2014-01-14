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
	public int getMaxDataPayload() {
		// TODO Auto-generated method stub
		return 0;
	}

	 /**
     * {@inheritDoc}
     */
	@Override
	public void handleEvent(SimTime simTime, ProtocolEvent pe) {
		// TODO Auto-generated method stub
		
	}

	 /**
     * {@inheritDoc}
     */
	@Override
	public void sapRequest(AbstractProtocol origin, SapType type, Object... params) {
		// TODO Auto-generated method stub
		
	}

	 /**
     * {@inheritDoc}
     */
	@Override
	public void sapResponse(AbstractProtocol origin, SapType type, Object... params) {
		// TODO Auto-generated method stub
		
	}

	 /**
     * {@inheritDoc}
     */
	@Override
	public void sapIndication(AbstractProtocol origin, SapType type, Object... params) {
		// TODO Auto-generated method stub
		
	}

	 /**
     * {@inheritDoc}
     */
	@Override
	public void sapConfirm(AbstractProtocol origin, SapType type, Object... params) {
		// TODO Auto-generated method stub
		
	}

}
