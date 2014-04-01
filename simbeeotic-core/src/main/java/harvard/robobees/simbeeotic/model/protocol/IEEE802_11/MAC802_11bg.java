package harvard.robobees.simbeeotic.model.protocol.IEEE802_11;

import harvard.robobees.simbeeotic.SimTime;
import harvard.robobees.simbeeotic.model.comms.AbstractRadio;
import harvard.robobees.simbeeotic.model.comms.DefaultRadioEvent;
import harvard.robobees.simbeeotic.model.comms.DefaultRadioState;
import harvard.robobees.simbeeotic.model.comms.HalfDuplexDefaultRadio;
import harvard.robobees.simbeeotic.model.comms.RadioEventListener;
import harvard.robobees.simbeeotic.model.protocol.AbstractPduWrap;
import harvard.robobees.simbeeotic.model.protocol.AbstractProtocol;
import harvard.robobees.simbeeotic.model.protocol.EthernetMacAddress;
import harvard.robobees.simbeeotic.model.protocol.LayerType;
import harvard.robobees.simbeeotic.model.protocol.MacPduWrap;
import harvard.robobees.simbeeotic.model.protocol.ProtocolEvent;
import harvard.robobees.simbeeotic.model.protocol.SapType;

import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 * Model of the 802.11bg MAC layer
 * This is for experiments, not intended to be used regularly.
 * @author Dalimir Orfanus
 *
 */
public class MAC802_11bg extends AbstractProtocol implements RadioEventListener {
	private static Logger logger = Logger.getLogger(MAC802_11bg.class);
	
	enum MacState {
		INIT,		
		WAITING_DIFS,
		READY,
		TX_DATA,		
		WAITING_FOR_ACK,
		WAITING_TO_ACK,
		TX_ACK,
		TX_RTS,
		TX_CTS,
		WAITING_CTS,
		WAITING_TO_CTS,
		WAITING_TO_TX_DATA,
		WAITING_FOR_RX_DATA,
		BACKOFF,
		NAV
	};
	
	MacState macState;
	
	EthernetMacAddress macAddr;
	
	/**
	 * Maximum propagation delay, 1us ~ 300m
	 */
	final int maxPropagationDelayUs = 1;

	/**
	 * Short Inter Frame Space, is in 802.11g 10us, wherein 1us is propagation delay
	 * SIFS time with Propagation Delay included 
	 */
	final long sifsTimePdUs = 9 + maxPropagationDelayUs;
	
	/**
	 * Short Inter Frame Space with no propagation delay included
	 */
	final long sifsTimeNoPdUs = 9;
	
	/**
	 * Slot time
	 */
	final long slotTimeUs = 9;
	
	/**
	 * DCF Inter Frame Space, in microseconds. Already contains maximum propagation delay
	 * which is contained inside SIFS time.
	 */
	final long difsTimeUs = sifsTimePdUs + 2*slotTimeUs;
	
	
	/**
	 * Minimal contention window
	 */
	final int minCW = (1 << 4) & 0xFF; // 15 
		
	/**
	 * Maximum data payload
	 */
	final int MAX_PAYLOAD = 2312;
	
	final int MAX_RETRANSMIT = 5;
	/**
	 * Radio
	 */
	HalfDuplexDefaultRadio radio;
	
	/**
	 * Transmission queue, queuing of messages from upper layer
	 */
	LinkedList<MacPduWrap> txQueue;
	
	long eventIdDifs = 0;
	long eventIdSifs = 0;
	long eventIdAck = 0;	
	long eventIdToAck = 0;	
	long eventIdToCts = 0;
	long eventIdWaitCts = 0;
	long eventIdCts = 0;
	long eventIdToTxData = 0;
	long eventIdForRxData = 0;
	long eventIdBackoff = 0;
	long eventIdNav = 0;
	
	int backoff = 0;	// 0 means no backoff, 
	int currentCW = minCW;
	
	MacPduWrap currentTxFrame;
	int currentTxId = 0;
	int currentRetransmitAttempt = 0;

	final Random rnd = new Random();
	
	boolean rtsReceived = false;

	
	// ======================================================

	/**
	 * Returns number of backoff intervals, randomly chosen from (0,currentCW] 
	 * @return
	 */
	int getNewBackoffInterval() {		
		return rnd.nextInt(currentCW) + 1;
	}
	
	/**
	 * Return new increased value of contention window (CW)
	 * @return
	 */
	int increaseCW() {
		return (currentCW << 1) > 0x80 ? 0x80 : (currentCW << 1);  
	}
	
	 /**
     * {@inheritDoc}
     */
	public void initialize() {
		setLayerType(LayerType.DLL);
		txQueue = new LinkedList<MacPduWrap>();
		macState = MacState.INIT;
	}
	
	 /**
     * {@inheritDoc}
     */
	public void finish() {
		// TODO Auto-generated method stub

	}

	@Override
	public void sapResponse(AbstractProtocol origin, SapType type, Object... params) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sapIndication(AbstractProtocol origin, SapType type, Object... params) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sapConfirm(AbstractProtocol origin, SapType type, Object... params) {
		// TODO Auto-generated method stub
		
	}

	 /**
     * {@inheritDoc}  
     * 
     * Method just enqueues messages for transmission 
     */
	@Override
	public void sapRequest(AbstractProtocol sender, SapType sapType, Object ... param) {
		
		if(sapType == SapType.DATA) {
			if(param.length == 0) {
				return;
			}
			
			AbstractPduWrap pdu = (AbstractPduWrap) param[0];
			if(pdu == null)
				return;
	
			if(!(pdu instanceof MacPduWrap))
				return;
			
			txQueue.add((MacPduWrap)pdu);
	 
			switch(macState) {
				case INIT:
					if(isRadioListening()) {
						macState = MacState.WAITING_DIFS;
						setTimerDifs();
					} 
					break;
				case READY:
			    	// if radio is in listening state, try immediately send (if there is something to be sent)
					if(isRadioListening())
						tryTransmission();				
					break;
					
				default:
					break;				
			}
		}
	}

	public void notifyRadioEvent(DefaultRadioEvent event, AbstractPduWrap pdu,
			double rxPower, double frequency) {

		logger.trace("Parent model: " + getContainer().getModelId() + ": current state: "
				+ macState + ", radio event: " + event);
		
		
		switch(macState) {
		case INIT:
			if(event == DefaultRadioEvent.LISTEN) {
				macState = MacState.WAITING_DIFS;
				setTimerDifs();
			} else if(event == DefaultRadioEvent.RX_RECEIVED) {
				if(pdu instanceof RtsPduWrap)
					parseRts((RtsPduWrap) pdu);
			}
			break;

		case READY:
			if(event == DefaultRadioEvent.START_RX || event == DefaultRadioEvent.RX_COLLISION) {
				if(eventIdBackoff != 0)
					getSimEngine().cancelEvent(eventIdBackoff);
				
			} else if(event == DefaultRadioEvent.LISTEN) {
				macState = MacState.WAITING_DIFS;
				setTimerDifs();				
			} else if(event == DefaultRadioEvent.RX_RECEIVED) {			
				if(pdu instanceof RtsPduWrap)
					parseRts((RtsPduWrap) pdu);
				else if(pdu instanceof DataPduWrap) {
					// somebody not using RTS/CTS, process it anyhow
					parseDataPdu((DataPduWrap) pdu);
				} else {
					// TODO other PDU, like CTS -to go offline
				}
				
				// if no change in state, frame not for us, go now to wait difs state
				if(macState == MacState.READY) {
					macState = MacState.WAITING_DIFS;
					setTimerDifs();				
				}
			}
			break;
				
		case TX_ACK:
			if(event == DefaultRadioEvent.LISTEN) {
				// ACK was sent
				macState = MacState.WAITING_DIFS;
				setTimerDifs();
			}
			break;
		
		case TX_DATA:
			if(event == DefaultRadioEvent.LISTEN) {
				// cancel backoff counting
				backoff = 0;
				if(currentTxFrame.getDestination().isBroadcast()) {
					// frame was sent, no wait for ACK, thus clear data as it was successful, set DIFS
					currentTxFrame = null;
					currentTxId = -1;
					currentRetransmitAttempt = 0;
					macState = MacState.WAITING_DIFS;
					setTimerDifs();
				} else {
					macState = MacState.WAITING_FOR_ACK;
					setTimerWaitForAck();
					break;
				}				
			}
			break;
		
		case TX_RTS:
			if(event == DefaultRadioEvent.LISTEN) {
				// RTS was successfully sent
				macState = MacState.WAITING_CTS;
				setTimerWaitForCts();
			} else if(event == DefaultRadioEvent.START_RX || event == DefaultRadioEvent.RX_COLLISION){
				// this should never happen
				logger.error("Parent model: " + getModelId() + ": mac state TX_RTS and got " + event + " radio event"); 
			}
			break;
		
		case WAITING_CTS:
			
			if(event == DefaultRadioEvent.START_RX) {
				// cancel timer
				if(eventIdWaitCts != 0)
					getSimEngine().cancelEvent(eventIdWaitCts);				

			} else if(event == DefaultRadioEvent.RX_RECEIVED) {								
				// parse pdu if CTS
				if(pdu instanceof CtsPduWrap) {
					CtsPduWrap ctsPdu = (CtsPduWrap) pdu;
					// if it is addressed for us
					if(ctsPdu.getRA().equals(macAddr)) {
						setTimerWaitToTxData();
						macState = MacState.WAITING_TO_TX_DATA;						
					} else {
						// TODO if not for us, then get the duration and go off
						if(backoff <= 0)
							backoff = getNewBackoffInterval();
					}
				} else {
					// TODO: what if it is not CTS?, which state?
					if(backoff <= 0)
						backoff = getNewBackoffInterval();
				}
			} else if(event == DefaultRadioEvent.LISTEN) {
				// after collision
				if(backoff <= 0)
					backoff = getNewBackoffInterval();

				macState = MacState.WAITING_DIFS;
				setTimerDifs();
			}
			
			break;
		
		case WAITING_DIFS:
			if(event == DefaultRadioEvent.START_RX) {
				// cancel wait for DIFS event, so we can receive the message
				if(eventIdDifs != 0)
					getSimEngine().cancelEvent(eventIdDifs);

				if(backoff > 0) {
					logger.debug("Parent model: " + getModelId() + ": stoping counting backoff");
					// stop counting backoff
					if(eventIdBackoff != 0)
						getSimEngine().cancelEvent(eventIdBackoff);
					
				} 
				
			} else if (event == DefaultRadioEvent.RX_RECEIVED) {
				// parse: RTS
				if(pdu instanceof RtsPduWrap)
					parseRts((RtsPduWrap) pdu);
				else if(pdu instanceof DataPduWrap) {
					parseDataPdu((DataPduWrap) pdu);
				} else {
					// ignore other packets
					setTimerDifs();
					logger.debug("Parent model: " + getModelId() + ": Ignoring pdu: " + pdu);
				}
			} else if(event == DefaultRadioEvent.LISTEN) {
					// end of collision
					setTimerDifs();
			}
			break;
		
		case WAITING_FOR_ACK:
			if(event == DefaultRadioEvent.START_RX) {
				if(eventIdAck != 0)
					getSimEngine().cancelEvent(eventIdAck);
			} else if(event == DefaultRadioEvent.RX_RECEIVED) {
				
				if(pdu instanceof AckPduWrap) {
					AckPduWrap ackPdu = (AckPduWrap) pdu;
					if(ackPdu.getRA().equals(macAddr)) {
						logger.debug("Parent model: " + getModelId() + ": Frame was ACKed");
						currentTxFrame = null;
						currentRetransmitAttempt = 0;
						currentTxId = 0;
					}
				} else {
					// drop the frame and set backoff
					if(backoff <= 0)
						backoff = getNewBackoffInterval();
				}
				
				macState = MacState.WAITING_DIFS;
				setTimerDifs();
				
			} else if(event == DefaultRadioEvent.LISTEN) {
				macState = MacState.WAITING_DIFS;
				setTimerDifs();
			}
			
			
			break;
		
		case WAITING_TO_ACK:
			if(event == DefaultRadioEvent.START_RX) {
				// cancel wait for ACK event
				if(eventIdToAck != 0)
					getSimEngine().cancelEvent(eventIdToAck);					
			} else if(event == DefaultRadioEvent.RX_RECEIVED
					|| event == DefaultRadioEvent.LISTEN) {
				// drop the frame and set DIFS timer
				macState = MacState.WAITING_DIFS;
				setTimerDifs();
			}

			break;
		
		case WAITING_TO_TX_DATA:			 
			if(event == DefaultRadioEvent.START_RX || event == DefaultRadioEvent.RX_COLLISION) {
				// this should not happen, somebody is interfering
				if(eventIdToTxData != 0)
					getSimEngine().cancelEvent(eventIdToTxData);
			} else if (event == DefaultRadioEvent.RX_RECEIVED 
					|| event == DefaultRadioEvent.LISTEN) {
				// drop the frame and go to backoff
				if(backoff <= 0)
					backoff = getNewBackoffInterval();
				
				macState = MacState.WAITING_DIFS;
				setTimerDifs();
			}			
			break;
			
		case TX_CTS:
			
			if(event == DefaultRadioEvent.LISTEN) {
				// CTS has been sent, let's wait for data, set timer
				logger.debug("Parent model: " + getModelId() + ": CTS sent, wait for data");
				macState = MacState.WAITING_FOR_RX_DATA;
				setTimerWaitForRxData();
			}
			
			break;
			
		case WAITING_TO_CTS:
			
			if(event == DefaultRadioEvent.START_RX || event == DefaultRadioEvent.RX_COLLISION) {
				// stop timer and wait for RX end
				if(eventIdToCts != 0)
					getSimEngine().cancelEvent(eventIdToCts);
			} else if(event == DefaultRadioEvent.RX_RECEIVED
					|| event == DefaultRadioEvent.LISTEN) {
				// drop the frame 
				macState = MacState.WAITING_DIFS;
				setTimerDifs();
			}

			break;
			
		case WAITING_FOR_RX_DATA:
			
			if(event == DefaultRadioEvent.START_RX || event == DefaultRadioEvent.RX_COLLISION) {
				// cancel timer
				getSimEngine().cancelEvent(eventIdForRxData);
			} else if (event == DefaultRadioEvent.RX_RECEIVED) {
				// data received
				if(pdu instanceof DataPduWrap)
					parseDataPdu((DataPduWrap) pdu); 
				else {
					// do nothing, nothing for us
				}
			} else if (event == DefaultRadioEvent.LISTEN) {
				// after collision is over
				macState = MacState.WAITING_DIFS;
				setTimerDifs();
			}
			
			break;
		
		case BACKOFF:
			if(event == DefaultRadioEvent.START_RX) {
				// stop interval decrementing
				logger.debug("Parent model: " + getModelId() + ": stoping counting backoff");
				if(eventIdBackoff != 0)
					getSimEngine().cancelEvent(eventIdBackoff);
				
			} else if (event == DefaultRadioEvent.LISTEN 
					|| event == DefaultRadioEvent.RX_RECEIVED) {
				// start interval counting
				macState = MacState.WAITING_DIFS;
				setTimerDifs();
			}
			
			break;
		
		case NAV:
			
			// what ever happens, do nothing, wait for NAV expiration
			
			break;
			
		default:
			break;
			
		
		}
		
	}

	void parseRts(RtsPduWrap rts) {
		// check if it for us
		if(rts.getRA().equals(macAddr)) {
			logger.debug("Parent model: " + getModelId() + ": RTS received");
			setTimerWaitToCts(rts);
			macState = MacState.WAITING_TO_CTS;
			rtsReceived = true;
		} else {
			// not for us, but useful to know how long to go offline
			macState = MacState.NAV;
			setTimerNav(rts.getDuration());
		}
	}
	
	void parseDataPdu(DataPduWrap dataPdu) {
		logger.debug("Parent model: " + getModelId() + ": data frame received");
		
		if(dataPdu.addr1.isBroadcast()) {
			indicateData(dataPdu.getUpperPdu());
		} else {
			
			if(dataPdu.getAddr1().equals(macAddr)) {
				// if received data and no broadcast, then send ACK
				macState = MacState.WAITING_TO_ACK;
				// sent timer to send ack
				setTimerWaitToAck(dataPdu.getAddr2());
				// notify listeners
				indicateData(dataPdu.getUpperPdu());					
			} else {
				// not for us, drop the frame
				macState = MacState.NAV;
				setTimerNav(dataPdu.getDuration());				
			}			
		}
	}
	
	/**
	 * Set timer for DIFS
	 */
	void setTimerDifs() {
		SimTime disfTimeEnd = new SimTime(getCurrTime(), difsTimeUs, TimeUnit.MICROSECONDS);
		eventIdDifs = getSimEngine().scheduleEvent(getModelId(), disfTimeEnd, new EventDifs(this));		
	}

	/**
	 * Set timer for SIFS
	 */
	void setTimerSifs(long value) {
		SimTime sisfTimeEnd = new SimTime(getCurrTime(), value, TimeUnit.MICROSECONDS);
		eventIdSifs = getSimEngine().scheduleEvent(getModelId(), sisfTimeEnd, new EventSifs(this));
	}
	
	void setTimerBackoffInterval() {
		SimTime beckoffTimeEnd = new SimTime(getCurrTime(), slotTimeUs, TimeUnit.MICROSECONDS);
		eventIdBackoff = getSimEngine().scheduleEvent(getModelId(), beckoffTimeEnd, new EventBackoffInterval(this));
	}

	void setTimerWaitForRxData() {
		SimTime waitForRxData = new SimTime(getCurrTime(), sifsTimePdUs, TimeUnit.MICROSECONDS);
		eventIdForRxData = getSimEngine().scheduleEvent(getModelId(), waitForRxData, new EventWaitForRxData(this));
	}

	void setTimerNav(long delayUs) {
		SimTime navTimeEnd = new SimTime(getCurrTime(), delayUs, TimeUnit.MICROSECONDS);
		eventIdNav = getSimEngine().scheduleEvent(getModelId(), navTimeEnd, new EventNavExpired(this));		
	}
	
	/**
	 * Set timer for waiting of ACK frame after transmission
	 */
	void setTimerWaitForAck() {
		SimTime ackTimeEnd = new SimTime(getCurrTime(), sifsTimePdUs, TimeUnit.MICROSECONDS);
		eventIdAck = getSimEngine().scheduleEvent(getModelId(), ackTimeEnd, new EventAck(this));				
	}

	void setTimerWaitToAck(EthernetMacAddress source) {
		SimTime ackTimeEnd = new SimTime(getCurrTime(), sifsTimeNoPdUs, TimeUnit.MICROSECONDS);
		eventIdToAck = getSimEngine().scheduleEvent(getModelId(), ackTimeEnd, new EventToAck(this, source));				
	}

	void setTimerWaitForCts() {
		SimTime ctsTime = new SimTime(getCurrTime(), sifsTimePdUs, TimeUnit.MICROSECONDS);
		eventIdCts = getSimEngine().scheduleEvent(getModelId(), ctsTime, new EventWaitCts(this));				
	}

	void setTimerWaitToCts(RtsPduWrap rts) {
		SimTime ctsTime = new SimTime(getCurrTime(), sifsTimeNoPdUs, TimeUnit.MICROSECONDS);
		eventIdToCts = getSimEngine().scheduleEvent(getModelId(), ctsTime, new EventWaitToCts(this, rts));				
	}
	
	void setTimerWaitToTxData() {
		SimTime txDataTime = new SimTime(getCurrTime(), sifsTimeNoPdUs, TimeUnit.MICROSECONDS);
		eventIdToTxData = getSimEngine().scheduleEvent(getModelId(), txDataTime, new EventWaitToTxData(this));		
	}
	
    /**
     * {@inheritDoc}
     * Handles expiraion of DIFS event
     */
    public final void handleDifsEvent(SimTime time, EventDifs eventDifs) {
    	logger.debug("Parent model: " + getModelId() + ": DIFS expired");
    	eventIdDifs = 0;
    	
    	if(isRadioListening()) {
    		if(backoff > 0) {
    			logger.debug("Parent model: " + getModelId() + ": resuming backoff");
    			macState = MacState.BACKOFF;
    			setTimerBackoffInterval();
    		} else {    		
	    		macState = MacState.READY;
	    		tryTransmission();
    		}
    	}    	
    }
    
    public final void handleWaitToCtsEvent(SimTime time, EventWaitToCts eventWaitToCts) {
    	logger.trace("Parent model: " + getModelId() + ": wait to send CTS expired");    	
    	eventIdToCts = 0;    	
    	if(isRadioListening()) {
    		macState = MacState.TX_CTS;
    		/**
    		 * For all CTS frames transmitted by a non-QoS STA in response to RTS frames, the duration 
    		 * value is the value obtained from the Duration field of the immediately previous RTS frame, 
    		 * minus the time, in microseconds, required to transmit the CTS frame and its SIFS interval.
    		 * If the calculated duration includes a fractional microsecond, that value is rounded up to
    		 * the next higher integer.
    		 */
    		long nano = getRadio().calculateAirTimeNs(14);
    		int duration  = eventWaitToCts.getRts().getDuration() - (int)sifsTimePdUs - (int)Math.ceil(((double)nano)/1000d); 
    		CtsPduWrap cts = new CtsPduWrap(FrameControlField.createCts(), duration, eventWaitToCts.getRts().getTA());
    		radio.transmit(cts);
    	} 
    }
    
    /**
     * {@inheritDoc}
     * Handles expiration of waiting for ACK. Event expires only when no ACK has been 
     * received (!!!)
     */
    public final void handleAckEvent(SimTime time, EventAck eventAck) {
    	logger.trace("Parent model: " + getContainer().getModelId() + ": ACK expired");
    	
    	eventIdAck = 0;
    	
		if(backoff <= 0)
			backoff = getNewBackoffInterval();

//    	if(macState == MacState.READY)
//    		tryTransmission();
//    	else 
    	if(isRadioListening()) {
    		macState = MacState.WAITING_DIFS;
    		setTimerDifs();
    	}
    }
    

    /**
     * {@inheritDoc}
     * Handles expiration of waiting to send confirming ACK. 
     */
    public final void handleAckToEvent(SimTime time, EventToAck eventToAck) {
    	logger.debug("Parent model: " + getModelId() + ": Wait to ACK expired");
    	eventIdToAck = 0;
    	AckPduWrap ackPdu = new AckPduWrap(FrameControlField.createAck(), 0, eventToAck.getSource());    	
    	macState = MacState.TX_ACK;
    	radio.transmit(ackPdu);    	
    }
    
    /**
     * Handles expiration of timer waiting for CTS message
     * @param time
     * @param eventCts
     */
    public void handleWaitCtsEvent(SimTime time, EventWaitCts eventCts) {
    	logger.debug("Parent model: " + getModelId() + ": Wait to CTS expired, radio in " + getRadioState());
    	
    	eventIdWaitCts = 0;
    	
		if(backoff <= 0)
			backoff = getNewBackoffInterval();

    	if(isRadioListening()) {
    		macState = MacState.WAITING_DIFS;
    		setTimerDifs();
    	} else if (isRadioRxBusy()) {
        	// if radio busy, then just wait    		
    	}
    }
    
    /**
     * Handles expiration of timer to send data. After this expiration, we should send data
     * @param time
     * @param eventTx
     */
    public void handleWaitToTxDataEvent(SimTime time, EventWaitToTxData eventTx) {    	
		logger.trace("Parent model: " + getModelId() + ": WAITING_TO_TX_DATA expired");
		if(isRadioListening()) {    			
			//send directly data
	    	macState = MacState.TX_DATA;
	    	int duration = 0;
	    	if(!currentTxFrame.getDestination().isBroadcast()) {
	    		long nano = getRadio().calculateAirTimeNs(14);
	    		duration = (int)Math.ceil( (double)nano/1000d ) + (int)sifsTimePdUs;
	    	}
	    	DataPduWrap dataPdu = new DataPduWrap(currentTxFrame.getDestination(), macAddr, FrameControlField.createDataNoFragment(), currentTxFrame, duration);    	
	    	radio.transmit(dataPdu);    	    	
			
		} 
//		else if(isRadioRxBusy()) {
//			macState = MacState.RX_BUSY;
//		}
    	
    }
    
    /**
     * Handles expiration of event "waiting for rx data"
     * @param time
     * @param event
     */
    public void handleWaitForRxDataEvent(SimTime time, EventWaitForRxData event) {
    	
    	// if radio is still idle, then trigger waiting DIFS state
    	if(isRadioListening()) {
    		macState = MacState.WAITING_DIFS;
    		setTimerDifs();
    	} 
    }
    
    public void handleBackoffIntervalEvent(SimTime time, EventBackoffInterval event) {
    	logger.trace("Parent model: " + getModelId() + ": one backoff interval expired, remaining: " + (backoff -1));
    	
    	// one interval less
    	--backoff;

		if(isRadioListening()) {
			if(backoff > 0) {
		    	// if radio is idle, then set new interval event
				setTimerBackoffInterval();
			}
			else {
				// backoff expired, go for transmission
				tryTransmission();
			}
		}

    }
    
    /**
     * Called when NAV expires, state should change to waiting DIFFS
     * @param time
     * @param event
     */
    public void handleNavExpiredEvent(SimTime time, EventNavExpired event) {
    	logger.trace("Parent model: " + getModelId() + ": NAV expired");
    	eventIdNav = 0;
    	
    	macState = MacState.WAITING_DIFS;
    	if(isRadioListening()) {
    		setTimerDifs();
    	}
    }
    
    /**
     * 
     * @param dataLen Length of following data frame in bytes
     */
    void sendRts(int dataLen) {
    	logger.debug("Parent model: " + getModelId() + ": RTS sent");
    	/*
    	 * For all RTS frames sent by non-QoS STAs, the duration value is the time, in microseconds, required to 
    	 * transmit the pending data or management frame, plus one CTS frame, plus one ACK frame, plus three SIFS 
    	 * intervals. If the calculated duration includes a fractional microsecond, that value is rounded up to
    	 * the next higher integer.
    	 */
    	// 14B for CTS, 14B for ACK,  DATA = 28B + dataSize
    	long nano = getRadio().calculateAirTimeNs(14 + 14 + 28 + dataLen);
    	int duration =  (int)(3L*sifsTimePdUs) +  (int)Math.ceil((double)nano/1000L);    	
    	PhyPduWrap802_11 rtsPdu = new RtsPduWrap(FrameControlField.createRts(), duration, 
    			currentTxFrame.getDestination(), macAddr);    	
    	macState = MacState.TX_RTS;
    	radio.transmit(rtsPdu);    	    	
    }
    
	/**
	* Method tries transmission. Should be called after DIFS expires and carrier is still idle
	*/
    void tryTransmission() {
    	MacPduWrap pdu; 
    	FrameControlField frameControl;
    	
    	// retransmit?
    	if(currentTxFrame != null) {
        	if(currentRetransmitAttempt >= MAX_RETRANSMIT) {
        		logger.debug("Parent model: " + getModelId() + ": max retrnamission achieved, dropping the frame");
        		return;
        	}
        	    		
    		++currentRetransmitAttempt;
    		logger.debug("Parent model: " + getModelId() + ": retransmitting");
    		frameControl = FrameControlField.createDataNoFragment();
    		frameControl.setRetry(true);
    	} else if (txQueue.size() > 0) {
    		pdu = txQueue.poll();
    		currentTxFrame = pdu;
    		currentRetransmitAttempt = 0;
    		frameControl = FrameControlField.createDataNoFragment();
    		// currentTxId = some ID
    		
        	// set the message ID        	
    		logger.debug("Parent model: " + getModelId() + ": transmitting new frame");
    	} else {
    		//oooops, this should never happen
    		return;
    	}
      	
    	if(currentTxFrame != null) {
    		if(currentTxFrame.getDestination().isBroadcast()) {
    	    	macState = MacState.TX_DATA;
    	    	// If broadcast, duration is set to 0
    	    	DataPduWrap dataPdu = new DataPduWrap(currentTxFrame.getDestination(), macAddr, frameControl, currentTxFrame, 0);    	
    	    	radio.transmit(dataPdu); 
    		} else {
    			sendRts(currentTxFrame.getDataSizeByte());
    		}
    	}
    }
    

    /**
     * {@inheritDoc}
     * Dispatches event to the proper handler
     */
	@Override
	public void handleEvent(SimTime simTime, ProtocolEvent pe) {
		if(pe instanceof EventDifs) {
			handleDifsEvent(simTime, (EventDifs)pe);
		} else if (pe instanceof EventAck) {
			handleAckEvent(simTime, (EventAck)pe);
		} else if (pe instanceof EventToAck) {
			handleAckToEvent(simTime, (EventToAck) pe);
		} else if (pe instanceof EventWaitToCts) {
			handleWaitToCtsEvent(simTime, (EventWaitToCts) pe);
		} else if(pe instanceof EventWaitCts) {
			handleWaitCtsEvent(simTime, (EventWaitCts) pe);
		} else if (pe instanceof EventWaitToTxData) {
			handleWaitToTxDataEvent(simTime, (EventWaitToTxData) pe);
		} else if(pe instanceof EventWaitForRxData) {
			handleWaitForRxDataEvent(simTime, (EventWaitForRxData) pe);
		} else if(pe instanceof EventBackoffInterval) {
			handleBackoffIntervalEvent(simTime, (EventBackoffInterval) pe);
		} else if(pe instanceof EventNavExpired) {
			handleNavExpiredEvent(simTime, (EventNavExpired) pe);
		} else {
			logger.error("Unknow type of event: " + pe);
		}
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
	// ---------------------------------------------------------

	@Override
	public int getMaxDataPayload() {
		return MAX_PAYLOAD;
	}

	public AbstractRadio getRadio() {
		return radio;
	}

	public void setRadio(AbstractRadio radio) {
		if(radio instanceof HalfDuplexDefaultRadio)
			this.radio = (HalfDuplexDefaultRadio)radio;
	}
	
	public EthernetMacAddress getMacAddr() {
		return macAddr;
	}

	public void setMacAddr(EthernetMacAddress macAddr) {
		this.macAddr = macAddr;
	}

	// =========================================================
	
	public class EventDifs extends ProtocolEvent {
		public EventDifs(AbstractProtocol protocol) {
			super(protocol);
		}
	}

	public class EventSifs extends ProtocolEvent  {

		public EventSifs(AbstractProtocol protocol) {
			super(protocol);
		}		
	}

	public class EventAck extends ProtocolEvent  {

		public EventAck(AbstractProtocol protocol) {
			super(protocol);
		}		
	}

	public class EventToAck extends ProtocolEvent  {
		EthernetMacAddress source;
		
		public EventToAck(AbstractProtocol protocol, EthernetMacAddress source) {
			super(protocol);
			this.source = source;
		}

		public EthernetMacAddress getSource() {
			return source;
		}		
	}

	public class EventWaitToCts extends ProtocolEvent {
		RtsPduWrap rts;
		public EventWaitToCts(AbstractProtocol protocol, RtsPduWrap rts) {
			super(protocol);
			this.rts = rts;
		}
		public RtsPduWrap getRts() {
			return rts;
		}		
		
	}

	public class EventWaitCts extends ProtocolEvent {
		public EventWaitCts(AbstractProtocol protocol) {
			super(protocol);			
		}		
	}
	
	public class EventWaitToTxData extends ProtocolEvent {
		public EventWaitToTxData(AbstractProtocol protocol) {
			super(protocol);
		}		
	}
	
	public class EventWaitForRxData extends ProtocolEvent {
		public EventWaitForRxData(AbstractProtocol protocol) {
			super(protocol);
		}	
	}
	
	public class EventBackoffInterval extends ProtocolEvent {
		public EventBackoffInterval(AbstractProtocol protocol) {
			super(protocol);
		}
		
	}

	public class EventNavExpired extends ProtocolEvent {
		public EventNavExpired(AbstractProtocol protocol) {
			super(protocol);
		}		
	}

}
