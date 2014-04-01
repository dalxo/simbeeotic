package harvard.robobees.simbeeotic.model.protocol.IEEE802_15_4;

import harvard.robobees.simbeeotic.SimTime;
import harvard.robobees.simbeeotic.model.comms.DefaultRadioEvent;
import harvard.robobees.simbeeotic.model.comms.DefaultRadioState;
import harvard.robobees.simbeeotic.model.comms.HalfDuplexDefaultRadio;
import harvard.robobees.simbeeotic.model.comms.RadioEventListener;
import harvard.robobees.simbeeotic.model.protocol.AbstractPduWrap;
import harvard.robobees.simbeeotic.model.protocol.AbstractProtocol;
import harvard.robobees.simbeeotic.model.protocol.LayerType;
import harvard.robobees.simbeeotic.model.protocol.ProtocolEvent;
import harvard.robobees.simbeeotic.model.protocol.SapType;
import harvard.robobees.simbeeotic.model.protocol.IEEE802_15_4.MacDataConfirm802_15_4.Status;

import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;


/**
 * Model of the 802.15.4 MAC sublayer, currently only unslotted CSMA-CA implemented
 * with default PHY in ISM band and 250kbps rate.
 * @author Dalimir Orfanus
 *
 */
public class MAC802_15_4 extends AbstractProtocol implements RadioEventListener {
	private static Logger logger = Logger.getLogger(MAC802_15_4.class);

	public enum Ranging {
		NON_RANGING,
		ALL_RANGING,
		PHY_HEADER_ONLY
	};
	
	public enum UwbPRF {
		PRF_OFF,
		NOMINAL_4_M,
		NOMINAL_16_M,
		NOMINAL_64_M
	}
	
	final static int MAX_DATA_PAYLOAD = 127; 
		
	enum MacState {
		INIT,
		DELAY,
		TX_DATA,
		WAITING_FOR_ACK,
		WAITING_TO_ACK,
		TX_ACK,
	};
	
	MacState macState;
	
	/**
	 * Symbol rate in baud/s for current PHY. Default: 2.4-2.4835 GHz 62500baud/s, 16-ary orth., 250kbps
	 */
	int phySymbolRate = 62500;
	
	/**
	 * The duration of the synchronization header (SHR) in symbols for the current PHY.
	 */
	int phySHRDuration = 10; 
	
	/**
	 * The number of symbols per octet for the current PHY. For the UWB PHY this is defined in 14.2.3. 
	 * For the CSS PHY, 1.3 corresponds to 1 Mb/s while 5.3 corresponds to 250 kb/s. Possible
	 * values: 0.4, 1.3, 1.6, 2, 5.3, 8. 
	 */
	double phySymbolsPerOctet = 2d;
		
	/**
	 * Is MAC Full Functional Device (FFD)?
	 */
	boolean isFfd = true;
	
	/**
	 * Minimum Backoff Exponent, default 3, shall be within 0 - macMaxBE
	 */
	int macMinBE = 3; 
	
	/**
	 * Maximum Backoff Exponent, default 5, shall be within 3 - 8 
	 */
	int macMaxBE = 5;
	
	/**
	 * Maximum number of CSMA backoff, default 4, shall be within 0 - 5 
	 */
	int macMaxCSMABackoffs = 4;
	
	/**
	 * RX-to-TX or TX-to-RX turnaround time (in symbol periods), as defined in 8.2.1 and 8.2.2.
	 */
	final static int macTurnaroundTimeSymbols = 12;
	
	long macTurnaroundTimeNs = (long) (macUnitBackoffPeriod * 1d/(double)phySymbolRate * 1e9); 
	
	/**
	 * The number of symbols forming the basic time period used by the CSMACA algorithm.
	 */
	final static int macUnitBackoffPeriod = 20; 

	/**
	 * The number of symbols forming the basic time period used by the CSMACA algorithm.
	 */
	long macUnitBackoffPeriodNs = (long) (macUnitBackoffPeriod * 1d/(double)phySymbolRate * 1e9); 

	/**
	 * The maximum size of an MPDU, in octets, that can be followed by a SIFS period.
	 */
	final static int macMaxSIFSFrameSize = 18;
	
	/**
	 * The minimum number of octets added by the MAC sublayer to the PSDU.
	 */
	final static int macMinMPDUOverhead = 9;
	
	final static int macMaxPHYPacketSize = 127;
	
	/**
	 * The maximum number of octets that can be transmitted in the MAC Payload field.
	 */
	final static int macMaxMACPayloadSize = macMaxPHYPacketSize - macMinMPDUOverhead;
	
	/**
	 * Configuration parameter: maximum communication radius in meters. 
	 */
	int radioRangeMeters = 300;
	
	/**
	 * How many nanoseconds needs light to travel one meter
	 */
	final static double lightNanosecondsPerMeter = 3.33564095;

	/**
	 * The maximum number of symbols to wait for an acknowledgment frame to arrive 
	 * following a transmitted data frame.
	 */
	int macAckWaitDurationSymbols = macUnitBackoffPeriod + macTurnaroundTimeSymbols + phySHRDuration + (int)Math.ceil(6 * phySymbolsPerOctet);

	/**
	 * The maximum number of nanoseconds to wait for an acknowledgment frame to arrive 
	 * following a transmitted data frame. It also counts radio range and propagation delay.
	 */	
	public long getMacAckWaitDurationNs() {
		return (long) (macAckWaitDurationSymbols * (1d/(double)phySymbolRate * 1e9)  
			+ getRadioRangeMeters() * lightNanosecondsPerMeter);
	}
	
	/**
	 * The minimum time (in symbols) forming a SIFS (Short Inter-Frame Space)
	 */
	int macSIFSPeriodSymbols = 40; 
	
	/**
	 * The minimum time (in nanoseconds) forming a SIFS (Short Inter-Frame Space)
	 */
	double macSIFSPeriodNs = macSIFSPeriodSymbols * 1d/(double)phySymbolRate * 1e9;
	
	/**
	 * The minimum time (in symbols) forming a LIFS (Long Inter-Frame Space) period
	 */
	int macLIFSPeriodSymbols = 12;
	
	/**
	 * The minimum time (in nanoseconds) forming a LIFS (Long Inter-Frame Space) period
	 */
	double macLIFSPeriodNs = macLIFSPeriodSymbols * 1d/(double)phySymbolRate * 1e9;

	/**
	 * The maximum number of retries allowed after a transmission failure.
	 */
	int macMaxFrameRetries = 3;
	
	
	/**
	 * Radio
	 */
	HalfDuplexDefaultRadio radio;
	
	MacAddress802_15_4 macAddress;
	
	/**
	 * List of all data requests to be sent
	 */
	LinkedList<MacDataRequest802_15_4> txQueue; 
	
	/**
	 * Currently being transmitted request
	 */
	MacDataRequest802_15_4 currentTxRequest;
	
	/**
	 * Most recently received PDU from radio
	 */
	PhyPdu802_15_4 currentRxPdu;

	int numberOfBackoffs = 0;
	int currentBackoffExponent = 0;
	int currentFrameRetries = 0;
	
	Random random = new Random();

	long eventIdDelay = 0;
	long eventIdAck = 0;
	long eventIdToAck = 0;

	int currentSequenceNumber = 0;
	
	 /**
     * {@inheritDoc}
     */
	@Override
	public void initialize() {
		setLayerType(LayerType.DLL);
		txQueue = new LinkedList<MacDataRequest802_15_4>();
		macState = MacState.INIT;
		currentSequenceNumber = 0;
	}

	 /**
     * {@inheritDoc}
     */
	@Override
	public void finish() {
		
	}

	 /**
     * {@inheritDoc}
     */
	@Override
	public void sapRequest(AbstractProtocol origin, SapType type, Object... params) {

		if(type == SapType.DATA) {
			if(params.length == 0) {
				return;
			}

			/**
			 * If MAC request is data request, queue it into tx list and try to transmit.
			 */
			if(params[0] instanceof MacDataRequest802_15_4) {
				txQueue.add((MacDataRequest802_15_4) params[0]);
				trySend();
			}
		}

	}

	 /**
     * {@inheritDoc}
     */
	@Override
	public void sapResponse(AbstractProtocol origin, SapType type,
			Object... params) {

		logger.warn("Parent model: " + getContainer().getModelId() + ": current state: "
				+ macState + ": UNIMPLEMENTED method called (sapResponse)");

	}

	 /**
     * {@inheritDoc}
     */
	@Override
	public void sapIndication(AbstractProtocol origin, SapType type,
			Object... params) {

		logger.warn("Parent model: " + getContainer().getModelId() + ": current state: "
				+ macState + ": UNIMPLEMENTED method called (sapIndication)");
	}
	
	 /**
     * {@inheritDoc}
     */
	@Override
	public void sapConfirm(AbstractProtocol origin, SapType type,
			Object... params) {

		logger.warn("Parent model: " + getContainer().getModelId() + ": current state: "
				+ macState + ": UNIMPLEMENTED method called (sapConfirm)");
	}

	 /**
     * {@inheritDoc}
     */
	@Override
	public int getMaxDataPayload() {
		return MAX_DATA_PAYLOAD;
	}

	 /**
     * {@inheritDoc}
     */
	public void notifyRadioEvent(DefaultRadioEvent event, AbstractPduWrap pdu,
			double rxPower, double frequency) {

		logger.trace("Parent model: " + getContainer().getModelId() + ": current state: "
				+ macState + ", radio event: " + event + ", pdu: " + pdu);

		
		if(pdu != null && !(pdu instanceof PhyPdu802_15_4))
			return;
		
		switch(macState) {		
		case INIT:			
			if(event == DefaultRadioEvent.RX_RECEIVED) {
				processRadioRxEvent(pdu, rxPower);
				
			} else if(event == DefaultRadioEvent.LISTEN) {
				// try to send data
				logger.trace("Parent model: " + getContainer().getModelId() + ": " + macState + ": trying to send frame");
				trySend();
			}
			break;			
			
		case DELAY:
			// Initially: ignore any radio event here, including RX.
			// Currently: process RX receivied radio event
			if(event == DefaultRadioEvent.RX_RECEIVED) {
				processRadioRxEvent(pdu, rxPower);
			}
			
			break;
			
		case TX_DATA:
			if(event == DefaultRadioEvent.LISTEN) {
				// frame has been sent
				
				// Should wait for ACK?
				if(currentTxRequest.isAckTx() && !currentTxRequest.getDstAddr().isBroadcast()) {
					logger.trace("Parent model: " + getContainer().getModelId() + ": seting timer to receive ACK");
					
					setTimerWaitForAck();
					
					// set waiting for ACK
					macState = MacState.WAITING_FOR_ACK;					
					
				} else {
					// if no ACK needed, then: go to INIT state and try to send another frame 
					macState = MacState.INIT;
					
					// Confirm to upper layer frame has been sent
					getUpperProtocol().sapConfirm(this, SapType.DATA, new MacDataConfirm802_15_4(currentTxRequest.getMsduHandle(), Status.SUCCESS));
					
					// probe new frame (if any)
					trySend(); 
				}
			} else {
				logger.error("Parent model: " + getContainer().getModelId() + ": " + macState + ": got unexpected radio event (" + event + ")");
			}
			break;
			
			
		case WAITING_FOR_ACK:
			if(event == DefaultRadioEvent.RX_RECEIVED) {
				logger.trace("Parent model: " + getContainer().getModelId() + ": " + macState + ": received: " + pdu);
				if(pdu instanceof AckPhyPdu) {
					AckPhyPdu ack = (AckPhyPdu) pdu;
					// cancel timer
					getSimEngine().cancelEvent(eventIdAck);
					if(ack.getSequenceNumber() == currentSequenceNumber) {
						// frame was acked						
						
						macState = MacState.INIT;
						// Confirm it to upper layer
						getUpperProtocol().sapConfirm(this, SapType.DATA, new MacDataConfirm802_15_4(currentTxRequest.getMsduHandle(), Status.SUCCESS));
						
						trySend();
					} else {
						logger.debug("Parent model: " + getContainer().getModelId() + ": " + macState + ": wrong sequence number, ignoring");
						// frame was not acked
						// wait for next frame or timer expiration
					}
										
				} else {
					logger.debug("Parent model: " + getContainer().getModelId() + ": " + macState + ": expecting ACK frame instead of " + pdu);
				}
			}			
			break;
			
		case TX_ACK:
			
			if(event == DefaultRadioEvent.LISTEN) {
				// ACK frame has been sent, go to the init state								
			} else {
				// ooops, there should not be any other radio event
				logger.error("Parent model: " + getContainer().getModelId() + ": " + macState + ": Expecting LISTEN radio event instead of: " + event);				
			}
			
			// go to init state in any case
			macState = MacState.INIT;
			trySend();
			
			break;
			
			
		case WAITING_TO_ACK:
			// ignore all radio events here, we wait until event expires so we can sent ACK
			break;
			
			
		default:
			logger.error("Parent model: " + getContainer().getModelId() + "Unknow or unhandled mac state: " + macState);
			break;
		
		}
		
	}

	/**
	 * Process radio event when received frame.
	 * @param pdu 
	 */
	private void processRadioRxEvent(AbstractPduWrap pdu, double rxPower) {
		if(!(pdu instanceof PhyPdu802_15_4)) {
			logger.warn("Parent model: " + getContainer().getModelId() + ": " + macState + ": received PDU is not 802.15.4, ignoring: " + pdu);
			return;
		}
		
		currentRxPdu = (PhyPdu802_15_4) pdu;

		// treat ACK
		if(currentRxPdu.getFrameControlField().isAckRequest() && !currentRxPdu.getDestination().isBroadcast()) {
			
			logger.trace("Parent model: " + getContainer().getModelId() + ": " + macState + ": RX frame, require ACK");
			
			// Wait to send ack
			macState = MacState.WAITING_TO_ACK;
			
			// set timer for ACK
			setTimerWaitToAck(currentRxPdu.getSequenceNumber());				
			
		} else {
			logger.trace("Parent model: " + getContainer().getModelId() + ": " + macState + ": RX frame, no ACK required");					
		}
																
		processReceivedFrame(rxPower);
	}
	
	
	/**
	 * Processes and parse currently received frame
	 */
	private void processReceivedFrame(double rxPower) {

		if(currentRxPdu instanceof DataPhyPdu) {
			DataPhyPdu dataFrame = (DataPhyPdu) currentRxPdu;

			// Indicate to the upper layer reception of data
			getUpperProtocol().sapIndication(this, SapType.DATA, 
					new MacDataIndication802_15_4(
							dataFrame.getSource(), 
							dataFrame.getDestination(), 
							dataFrame.getUpperPdu(), 
							dataFrame.getDataSizeByte(), 
							dataFrame.getSequenceNumber(),
							rxPower));
								
			
		} else if(currentRxPdu instanceof CmdPhyPdu) {
			CmdPhyPdu cmdFrame = (CmdPhyPdu) currentRxPdu;
			logger.warn("Parent model: " + getContainer().getModelId() + ": Received unsupported frame type: " + cmdFrame);
			// TODO: wtf?
		}

	}
	
	/**
	 * Method performs initialization to send a frame (if queue is not empty). If there are no frames to be sent
	 * or MAC is busy with reception/transmission of another frame, method will return. 
	 * Otherwise method will dequeue next frame and goes to delay state
	 */
	private void trySend() {
		
		/**
		 * If MAC is not in the INIT state, then skip. Means it is busy with send/reception
		 * of another frame
		 */
		if(macState != MacState.INIT)
			return;
		
		/**
		 * Is there something to transmit?
		 */
		if(txQueue.size() == 0)
			return;
		
		// 1. Dequeue frame to be sent
		currentTxRequest = txQueue.poll();

		// if data too long
		if(currentTxRequest.getMsduLenght() > macMaxMACPayloadSize) {
			getUpperProtocol().sapConfirm(this, SapType.DATA, new MacDataConfirm802_15_4(currentTxRequest.getMsduHandle(), Status.FRAME_TOO_LONG));
			return;
		}
		currentFrameRetries = 0;

		initDelay();
	}	

	/**
	 * Initializes DELAY state, i.e. reset backoff timers and counters, sets new timer for delay and 
	 * change current state to DELAY
	 */
	private void initDelay() {
		// 1. reset backoff counters and timers
		numberOfBackoffs = 0;
		currentBackoffExponent = macMaxBE;
				
		// 2. Calculate delay and set timer
		int delayUnits = random.nextInt( (1 << currentBackoffExponent) );
		setTimerDelay(delayUnits);
		
		// 3. Set new state
		macState = MacState.DELAY;
	}
	
	/**
	 * Method will transmit a frame. This shall be called after we passed DELAY state.
	 * The frame is created from MacData request found in {@link currentTxRequest} field.
	 */
	private void transmitFrame() {
		if(macState != MacState.DELAY) {
			logger.error("transmitFrame: not in DELAY mac state but " + macState);
			return;
		}
		
		if(currentTxRequest == null) {
			macState = MacState.INIT;
			logger.error("transmitFrame: currentTxRequest is empty");
			trySend();
			return;
		}
		
		// 1. create a data frame
		MacAddress802_15_4 srcAddr = getMacAddress() == null ? currentTxRequest.getSrcAddr() : getMacAddress();
		DataPhyPdu dataFrame = new DataPhyPdu(srcAddr, currentTxRequest.getDstAddr(), 
				FrameControlField.createData(true, FcfAddressingMode.Short), 
				++currentSequenceNumber , currentTxRequest.getMsdu());

		// 2. set state to DATA_TX
		macState = MacState.TX_DATA;

		// 3. submit to radio
		radio.transmit(dataFrame);
	}
	
	/**
	 * Set timer for DIFS
	 */
	void setTimerDelay(int delayUnits) {
		SimTime delayTimeEnd = new SimTime(getCurrTime(), delayUnits * macUnitBackoffPeriodNs, TimeUnit.NANOSECONDS);
		eventIdDelay = getSimEngine().scheduleEvent(getModelId(), delayTimeEnd, new EventDelay(this));		
	}
	
	/**
	 * Set timer which waits for ACK.
	 * See 6.4.3 of standard, dont forget to count distance and maximum range.
	 */
	void setTimerWaitForAck() { 
		SimTime waitForAckTimeEnd = new SimTime(getCurrTime(), getMacAckWaitDurationNs(), TimeUnit.NANOSECONDS);
		eventIdAck = getSimEngine().scheduleEvent(getModelId(), waitForAckTimeEnd, new EventAck(this));		
	}
	
	/**
	 * Sets timer event to indicate when to send ACK frame
	 * @param sequenceNumber
	 */
	void setTimerWaitToAck(int sequenceNumber) {
		SimTime waitToAckTimeEnd = new SimTime(getCurrTime(), macTurnaroundTimeNs, TimeUnit.NANOSECONDS);
		eventIdToAck = getSimEngine().scheduleEvent(getModelId(), waitToAckTimeEnd, new EventToAck(this, sequenceNumber));				
	}

	
    /**
     * {@inheritDoc}
     * Handles expiration of ACK event. If maximum number of retransmission reached, reports NO_ACK.
     * Otherwise it goes back to DELAY state with new reseted backoff timers and counters. 
     */
    public final void handleAckExpiredEvent(SimTime time, EventAck eventAck) {    	
    	logger.info("Parent model: " + getModelId() + ": Waiting for ACK expired, restransmitting");    	
    	    	
    	++currentFrameRetries;
    	
    	// Have we reached max number of retransmission?
    	if(currentFrameRetries > macMaxFrameRetries) {    		    		
    		macState = MacState.INIT;    		
			getUpperProtocol().sapConfirm(this, SapType.DATA, new MacDataConfirm802_15_4(currentTxRequest.getMsduHandle(), Status.NO_ACK));
    	} else {    		
    		initDelay();
    	}
    }

    
    /**
     * {@inheritDoc}
     * Handles expiration of event to wait to send ACK
     */
    public final void handleWaitToAckEvent(SimTime time, EventToAck eventToAck) {
    	// Wait to send ack expired, send it now...
    	
    	// what if radio is not in LISTEN state
    	if(!isRadioListening()) {
    		// dont send and go to init state
    		macState = MacState.INIT;
    		logger.info("Parent model: " + getModelId() + ": Wait to Ack expired, radio is busy, skipping TX_ACK");
    		return;
    	}
    	
		// 1. create a data frame
		MacAddress802_15_4 srcAddr = getMacAddress() == null ? currentTxRequest.getSrcAddr() : getMacAddress();
		
		AckPhyPdu ackFrame = new AckPhyPdu(srcAddr, currentRxPdu.getDestination(), FrameControlField.createAck(), eventToAck.sequence);
		
		// 2. set state to DATA_TX
		macState = MacState.TX_ACK;

		// 3. submit to radio
		radio.transmit(ackFrame);
    }

    
    /**
     * {@inheritDoc}
     * Handles expiration of DELAY event
     */
    public final void handleDelayEvent(SimTime time, EventDelay eventDelay) {
    	logger.debug("Parent model: " + getModelId() + ": DELAY expired");
    	eventIdDelay = 0;
    	
    	// check radio, if idle, then go to transmit
    	if(isRadioListening()) {
    		transmitFrame();    		
    	} else {
    		
    		logger.info("Parent model: " + getModelId() + ": DELAY expired, radio is busy, backing off");
    		
    		++numberOfBackoffs;
    		
    		if(numberOfBackoffs > macMaxCSMABackoffs) {
        		logger.info("Parent model: " + getModelId() + ": DELAY expired, max num of attempts for backoff reached");
    			
    			// failure, number of attempts to retransmit reached max. 
    			macState = MacState.INIT;
    			
    			// Report to upper layer channel access failure
    			getUpperProtocol().sapConfirm(this, SapType.DATA, new MacDataConfirm802_15_4(currentTxRequest.getMsduHandle(), Status.CHANNEL_ACCESS_FAILURE));
    			    			
    			
    		} else {
    			// try again
    			currentBackoffExponent = Math.min(currentBackoffExponent + 1, macMinBE);
    		
    			// 2. Calculate delay and set timer
    			int delayUnits = random.nextInt( (1 << currentBackoffExponent) );
    			setTimerDelay(delayUnits);
    			
    			// 3. Set new state
    			macState = MacState.DELAY;
    		}
    	}
    	
    }

	 /**
     * {@inheritDoc}
     */
	@Override
	public void handleEvent(SimTime simTime, ProtocolEvent pe) {
		if(pe instanceof EventDelay) {
			handleDelayEvent(simTime, (EventDelay) pe);
		} else if(pe instanceof EventAck) {
			handleAckExpiredEvent(simTime, (EventAck) pe);
		} else if(pe instanceof EventToAck) {
			handleWaitToAckEvent(simTime, (EventToAck) pe);
		} else {
			logger.error("Unknow type of event: " + pe);
		}

	}
	
	/**
	 * How many TX requests are queued in the MAC layer
	 * @return Length of the TX queue
	 */
	public int getTxQueueLen() {
		return txQueue.size();
	}

	/**
	 * What is the maximum payload size for data transmissions
	 * @return Size in octects
	 */
	public int getMaxPayload() {
		return macMaxMACPayloadSize;
	}
	
	DefaultRadioState getRadioState() {
		return radio.getRadioState();
	}
	
	boolean isRadioRxBusy() {
		return getRadioState() == DefaultRadioState.RX_BUSY || 
				getRadioState() == DefaultRadioState.RX_BUSY_COLLISION;
	}
	
	boolean isRadioListening() {
		return getRadioState() == DefaultRadioState.LISTENING;
	}



	// -------------------------------------------------------------
	
	public HalfDuplexDefaultRadio getRadio() {
		return radio;
	}

	public void setRadio(HalfDuplexDefaultRadio radio) {
		this.radio = radio;
	}

	public boolean isFfd() {
		return isFfd;
	}

	public void setFfd(boolean isFfd) {
		this.isFfd = isFfd;
	}
	
	public MacAddress802_15_4 getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(MacAddress802_15_4 macAddress) {
		this.macAddress = macAddress;
	}	

	public int getRadioRangeMeters() {
		return radioRangeMeters;
	}

	public void setRadioRangeMeters(int radioRangeMeters) {
		this.radioRangeMeters = radioRangeMeters;
	}

	// ===================================================================
	
	/**
	 * Event to signal end of delay period before transmission
	 * @author Dalimir Orfanus
	 *
	 */
	public class EventDelay extends ProtocolEvent {
		public EventDelay(AbstractProtocol protocol) {
			super(protocol);
		}
	}

	/**
	 * Event to signal end of waiting for for ACK frame
	 * @author Dalimir Orfanus
	 *
	 */
	public class EventAck extends ProtocolEvent {
		public EventAck(AbstractProtocol protocol) {
			super(protocol);
		}
	}

	/**
	 * Event to signal end of waiting to send ACK frame
	 * @author Dalimir Orfanus
	 *
	 */
	public class EventToAck extends ProtocolEvent {
		int sequence;
		public EventToAck(AbstractProtocol protocol, int sequence) {
			super(protocol);
			this.sequence = sequence;
		}
		public int getSequence() {
			return sequence;
		}
	}

}
