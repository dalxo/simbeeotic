package harvard.robobees.simbeeotic.model.protocol;

import harvard.robobees.simbeeotic.ClockControl;
import harvard.robobees.simbeeotic.SimEngine;
import harvard.robobees.simbeeotic.SimTime;
import harvard.robobees.simbeeotic.model.AbstractModel;

/**
 * Abstract model of a communication protocol. Protocol has one Service Access Point (SAP)
 * for lower protocol and can send data to several upper protocols via their lower SAP.
 * @author Dalimir Orfanus
 *
 */
public abstract class AbstractProtocol {

	AbstractModel container;
	SimEngine simEngine;
	ClockControl clockControl;
	
	
	/**
	 * Upper protocol listeners
	 */
	private AbstractProtocol upperProtocol;

	private AbstractProtocol lowerProtocol;

	/**
	 * Layer where this protocol belongs to
	 */
	private LayerType layerType;	
	
	abstract public void initialize();
	
	abstract public void finish();
	
	abstract public void handleEvent(SimTime simTime, ProtocolEvent pe);
	
	
	protected final void indicateData(AbstractPduWrap pdu) {
		this.upperProtocol.sapIndication(this, SapType.DATA, pdu);
	}
		
	
	protected final void requestData(AbstractPduWrap pdu) {
		this.lowerProtocol.sapRequest(this, SapType.DATA, pdu);
	}

	/**
	 * Service Access Point (SAP) primitive: request. The request primitive is 
	 * passed from the N-user to the N-layer (or sublayer) to request that a service be initiated.
	 */
	abstract public void sapRequest(AbstractProtocol origin, SapType type, Object ... params);

	/**
	 * Service Access Point (SAP) primitive: response. The response primitive is passed from the
	 * N-user to the N-layer (or sublayer) to complete a procedure previously invoked by an 
	 * indication primitive.
	 */
	abstract public void sapResponse(AbstractProtocol origin, SapType type, Object ... params);

	/**
	 * Service Access Point (SAP) primitive: indication. The indication primitive is passed from the 
	 * N-layer (or sublayer) to the N-user to indicate an internal N-layer (or sublayer) event that 
	 * is significant to the N-user. This event may be logically related to a remote service request,
	 * or may be caused by an event internal to the N-layer (or sublayer).
	 */
	abstract public void sapIndication(AbstractProtocol origin, SapType type, Object ... params);

	/**
	 * Service Access Point (SAP) primitive: confirm. The confirm primitive is passed from the 
	 * N-layer (or sublayer) to the N-user to convey the results of one or more associated previous
	 * service request(s). 
	 */
	abstract public void sapConfirm(AbstractProtocol origin, SapType type, Object ... params);
	
	
	abstract public int getMaxDataPayload();
	
	protected int getModelId() {
		return container.getModelId();
	}
	
	// ----- GETTERS / SETTERS -----
	
	public LayerType getLayerType() {
		return layerType;
	}

	public void setLayerType(LayerType layerType) {
		this.layerType = layerType;
	}

	public AbstractProtocol getLowerProtocol() {
		return lowerProtocol;
	}

	public void setLowerProtocol(AbstractProtocol lowerProtocol) {
		this.lowerProtocol = lowerProtocol;
	}

	public AbstractModel getContainer() {
		return container;
	}

	public void setContainer(AbstractModel container) {
		this.container = container;
	}

	public SimEngine getSimEngine() {
		return simEngine;
	}

	public void setSimEngine(SimEngine simEngine) {
		this.simEngine = simEngine;
	}

	public SimTime getCurrTime() {
		return clockControl.getCurrentTime();
	}

	public ClockControl getClockControl() {
		return clockControl;
	}

	public void setClockControl(ClockControl clockControl) {
		this.clockControl = clockControl;
	}

	public AbstractProtocol getUpperProtocol() {
		return upperProtocol;
	}

	public void setUpperProtocol(AbstractProtocol upperProtocol) {
		this.upperProtocol = upperProtocol;
	}

}
