/*
 * Copyright (c) 2012, The President and Fellows of Harvard College.
 * All Rights Reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *  3. Neither the name of the University nor the names of its contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE UNIVERSITY AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE UNIVERSITY OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package harvard.robobees.simbeeotic.model.comms;


import harvard.robobees.simbeeotic.ClockControl;
import harvard.robobees.simbeeotic.SimTime;
import harvard.robobees.simbeeotic.configuration.ConfigurationAnnotations.GlobalScope;
import harvard.robobees.simbeeotic.model.EventHandler;
import harvard.robobees.simbeeotic.model.protocol.AbstractPduWrap;

import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;


/**
 * Extending original DefaultRadio to model most commonly used radio type:
 * half-duplex, sensing in one channel (band). 
 * 
 * A radio that transmits at maximum power and receives 100% of packets
 * with received power over a specific SNR threshold. 
 *
 * @author Dalimir Orfanus, bkate
 */
public class HalfDuplexDefaultRadio extends AbstractRadio {
	
    @Inject
    @GlobalScope
    private ClockControl clockControl;

	static final int BYTES_PER_KILOBIT = 125;

	DefaultRadioState radioState = DefaultRadioState.OFF;
	
    Band band = new Band(2442.5, 85);
    double snrMargin = 7;           // dBm
    protected double bandwidth = 31250;       // Bps
    double txEnergy = 12;           // mA
    double rxEnergy = 15;           // mA
    double idleEnergy = 0.5;        // mA
    double maxPower = 0;            // dBm
    // You don't snuggle with Max Power, you strap yourself in and feel the G's!

    private static Logger logger = Logger.getLogger(HalfDuplexDefaultRadio.class);

    long endOfReceptionEventId;
    SimTime endOfReception;
    
    HashSet<RadioEventListener> eventListener = new HashSet<RadioEventListener>();
    
    public void initialize() {
    	super.initialize();
    	
    	radioState = DefaultRadioState.LISTENING;
    }
    
    /**
     * {@inheritDoc}
     *
     * Transmits at the maximum power.
     */
    @Override
    public void transmit(AbstractPduWrap pdu) {

    	// if radio is in one of following states, ignore request and notify error
    	switch(radioState) {
	    	case OFF:
	    	case RX_BUSY:
	    	case RX_BUSY_COLLISION:
	    	case TX_BUSY:
	    		notifyRadioEvent(DefaultRadioEvent.ERROR, pdu, 0, 0);
	    		return;
	    		
			default:
				break;    	
    	}
    	
        super.transmit(pdu);
        radioState = DefaultRadioState.TX_BUSY;
        
        logger.trace("ID: " + getModelId() + ": Radio: start of transmission");
        getPropagationModel().transmit(this, pdu, maxPower, band);
        
        // set event for end of transmission
        
    	SimTime endOfTx = new SimTime(clockControl.getCurrentTime(), 
    			calculateAirTimeNs(pdu.getDataSizeByte()), 
				TimeUnit.NANOSECONDS);    			    	
		    	
		// schedule new end of transmission event
		getSimEngine().scheduleEvent(getModelId(), endOfTx, 
				new EndOfTransmissionEvent(pdu, maxPower, band.getCenterFrequency()));
        
		
    }

    
    @EventHandler
    public final void handleEndOfTransmissionEvent(SimTime time, EndOfTransmissionEvent event) {
        radioState = DefaultRadioState.LISTENING;
        
        logger.trace("ID: " + getModelId() + ": Radio: end of transmission event");
        
        // notify DLL that transmission is done
        //notifyRadioEvent(DefaultRadioEvent.END_OF_TX, null, 0, 0);
        
        // notify DLL that radio went back to LISTEN state
        notifyRadioEvent(DefaultRadioEvent.LISTEN, null, 0, 0);
    }


    /**
     * {@inheritDoc}
     *
     * This implementation performs an SNR thresholding and invokes all listeners
     * registered with this radio to receive notifications when a message is received.
     */
    @Override
    public void receive(SimTime time, AbstractPduWrap pdu, double rxPower, double frequency) {
    	logger.trace("ID: " + getModelId() + ": receivie call");
    	
        double snr = rxPower - getPropagationModel().getNoiseFloor(getOperatingBand());
    	
        // not enough power to capture signal?
        if (snr < snrMargin)
        	return;
    	
        switch(radioState) {
	        case LISTENING:
	    		logger.debug("ID: " + getModelId() + ": Radio: started to receive msg");
	    		
	    		radioState = DefaultRadioState.RX_BUSY;
	    		notifyRadioEvent(DefaultRadioEvent.START_RX, null, 0, 0);
	    		scheduleEndOfReception(pdu, rxPower, frequency);
	    		
	    		break;
	    	
	        case RX_BUSY:
	        case RX_BUSY_COLLISION:
	    		radioState = DefaultRadioState.RX_BUSY_COLLISION;
	    		
	    		logger.debug("ID: " + getModelId() + ": Radio: COLLISION detected");
	    		
	    		// calculate when current transmission will end
	    		long newEndRxTime = calculateAirTimeNs(pdu.getDataSizeByte()) + time.getTime();
	
	    		// compare intervals whether new transmission is longer then current one
	    		if(newEndRxTime > endOfReception.getTime()) {
	    			// cancel EndOfReception of current RX and schedule new EndOfReception
	    			
	    			getSimEngine().cancelEvent(endOfReceptionEventId);    			
	    			scheduleEndOfReception(pdu, rxPower, frequency);    			
	    		}
	
	    		notifyRadioEvent(DefaultRadioEvent.RX_COLLISION, null, 0, 0);
	    		getAggregator().addValue("collisions", "number", 1);
	    		break;
	    		
	    	default:
	    		break;
        }
        
    }

    /**
     * Schedules end of reception event for given incoming message
     * @param data
     * @param rxPower
     * @param frequency
     */
    private void scheduleEndOfReception(AbstractPduWrap pdu, double rxPower, double frequency) {
    	endOfReception = new SimTime(clockControl.getCurrentTime(), 
    			calculateAirTimeNs(pdu.getDataSizeByte()), 
				TimeUnit.NANOSECONDS);
		
		// schedule new end of reception
		endOfReceptionEventId = getSimEngine().scheduleEvent(getModelId(), 
				endOfReception, new EndOfReceptionEvent(pdu, rxPower, frequency));    	
    }
    
    /**
     * Handles end of reception event. If there was collision during reception,
     * returns back to LISTEN state. If no collision, DLL is notified.
     * @param time
     * @param event
     */
    @EventHandler
    public final void handleEndOfReceptionEvent(SimTime time, EndOfReceptionEvent event) {        
    	super.receive(time, event.getPdu(), event.getRxPower(), event.getFrequency());
    	logger.trace("ID: " + getModelId() + ": End of reception event");
    	
        // if there was a collision, do nothing and go back to LISTEN state
        if(radioState == DefaultRadioState.RX_BUSY_COLLISION) {
        	radioState = DefaultRadioState.LISTENING;
        	notifyRadioEvent(DefaultRadioEvent.LISTEN, null, 0, 0);        	
        } else {                
        	// go back to the LISTEN state
        	radioState = DefaultRadioState.LISTENING;        
        	// notify DLL for received data
        	notifyRadioEvent(DefaultRadioEvent.RX_RECEIVED, event.getPdu(), event.getRxPower(), event.getFrequency());        
        }
    }
    
    /**
     * {@inheritDoc}
     * @param size
     * @return
     */
    @Override
    public long calculateAirTimeNs(long sizeBytes) {    	
    	//double airTime = (sizeBytes/(double) BYTES_PER_KILOBIT) / getBandwidth();
    	double airTime = sizeBytes/getBandwidth();
    	return (long) (airTime * TimeUnit.SECONDS.toNanos(1));
    }
    
    
    public void addRadioEventListener(RadioEventListener listener) {
    	eventListener.add(listener);
    }
    
    public void removeRadioEventListener(RadioEventListener listener) {
    	eventListener.remove(listener);
    }
    
    protected void notifyRadioEvent(DefaultRadioEvent event, AbstractPduWrap pdu, double rxPower, double frequency) {
    	for(RadioEventListener listener : eventListener) {
    		listener.notifyRadioEvent(event, pdu, rxPower, frequency);
    	}
    }
    
    public void turnOnRadio() {
    	setRadioState(DefaultRadioState.LISTENING);
    	//notifyRadioEvent(DefaultRadioEvent.LISTEN, null, 0, 0);
    }
    
    public void sleepRadion() {
    	
    }
    
    public void turnOffRadio() {
    	
    }

    /** {@inheritDoc} */
    @Override
    protected double getIdleEnergy() {
        return idleEnergy;
    }


    /** {@inheritDoc} */
    @Override
    protected double getRxEnergy() {
        return rxEnergy;
    }


    /** {@inheritDoc} */
    @Override
    protected double getTxEnergy() {
        return txEnergy;
    }


    /** {@inheritDoc} */
    @Override
    protected double getBandwidth() {
        return bandwidth;
    }


    /** {@inheritDoc} */
    @Override
    public Band getOperatingBand() {
        return band;
    }
    
	public DefaultRadioState getRadioState() {
		return radioState;
	}

	protected void setRadioState(DefaultRadioState radioState) {
		this.radioState = radioState;
	}
    

    @Inject(optional = true)
    public final void setCenterFrequency(@Named("center-frequency") final double freq) {
        this.band = new Band(freq, this.band.getBandwidth());
    }


    @Inject(optional = true)
    public final void setChanelBandwidth(@Named("channel-bandwidth") final double bandwidth) {
        this.band = new Band(this.band.getCenterFrequency(), bandwidth);
    }


    @Inject(optional = true)
    public final void setMaxPower(@Named("max-power") final double power) {
        this.maxPower = power;
    }


    @Inject(optional = true)
    public final void setSnrMargin(@Named("snr-margin") final double margin) {
        this.snrMargin = margin;
    }


    @Inject(optional = true)
    public final void setBandwidth(@Named("bandwidth") final double bandwidth) {
        this.bandwidth = bandwidth * BYTES_PER_KILOBIT;  // convert kbps to Bps
    }


    @Inject(optional = true)
    public final void setTxEnergy(@Named("tx-energy") final double energy) {
        this.txEnergy = energy;
    }


    @Inject(optional = true)
    public final void setRxEnergy(@Named("rx-energy") final double energy) {
        this.rxEnergy = energy;
    }


    @Inject(optional = true)
    public final void setIdleEnergy(@Named("idle-energy") final double energy) {
        this.idleEnergy = energy;
    }
}
