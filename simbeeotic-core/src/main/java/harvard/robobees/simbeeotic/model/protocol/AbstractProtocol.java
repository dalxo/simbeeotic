package harvard.robobees.simbeeotic.model.protocol;

import harvard.robobees.simbeeotic.ClockControl;
import harvard.robobees.simbeeotic.SimEngine;
import harvard.robobees.simbeeotic.SimTime;
import harvard.robobees.simbeeotic.model.AbstractModel;

import java.util.HashSet;
import java.util.Set;

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
	private Set<AbstractProtocol> upperProtocols = new HashSet<AbstractProtocol>();

	private AbstractProtocol lowerProtocol;

	/**
	 * Layer where this protocol belongs to
	 */
	private LayerType layerType;	
	
	abstract public void initialize();
	
	abstract public void finish();
	
	abstract public void handleEvent(SimTime simTime, ProtocolEvent pe);
	
	/**
	 * Add upper protocol (message) listener
	 * @param protocol
	 */
	public final void addUpperProtocol(AbstractProtocol protocol) {
		upperProtocols.add(protocol);
	}
	
	/**
	 * Remove upper protocol listener for messages from this protocol
	 * @param protocol
	 */
	public final void removeUpperProtocols(AbstractProtocol protocol) {
		upperProtocols.remove(protocol);
	}
	
	/**
	 * Send message to upper protocols
	 * @param data
	 */
	protected final void notifyUpper(byte[] data, AbstractPduWrap pdu) {
		for(AbstractProtocol protocol : upperProtocols)
			protocol.lowerSAP(this, pdu);
	}

	/**
	 * Send message to lower protocol
	 * @param data
	 */
	protected final void notifyLower(byte[] data, AbstractPduWrap pdu) {
		lowerProtocol.upperSAP(this, pdu);
	}
	
	/**
	 * Receives messages from lower protocols 
	 */
	abstract public void lowerSAP(AbstractProtocol sender, AbstractPduWrap pdu);

	/**
	 * Receives messages from the upper protocols
	 */
	abstract public void upperSAP(AbstractProtocol sender, AbstractPduWrap pdu);
	
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

}
