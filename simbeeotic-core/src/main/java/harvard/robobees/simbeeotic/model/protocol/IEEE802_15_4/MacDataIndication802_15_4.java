package harvard.robobees.simbeeotic.model.protocol.IEEE802_15_4;

import harvard.robobees.simbeeotic.model.protocol.AbstractPduWrap;
import harvard.robobees.simbeeotic.model.protocol.IEEE802_15_4.MAC802_15_4.Ranging;
import harvard.robobees.simbeeotic.model.protocol.IEEE802_15_4.MAC802_15_4.UwbPRF;

/**
 * Class that represents MAC DATA indication of data service in 802.15.4
 * @author Dalimir Orfanus
 *
 */
public class MacDataIndication802_15_4 implements MacDataService802_15_4 {

	MacAddress802_15_4 src;
	MacAddress802_15_4 dst;
	
	int msduLenght = 0;
	
	/**
	 * RSSI
	 */
	double rxPower;
	
	/**
	 * MAC service datagram unit
	 */
	AbstractPduWrap msdu;

	UwbPRF uwbPRF = UwbPRF.PRF_OFF;
	
	Ranging ranging = Ranging.NON_RANGING;

	/**
	 * Indicates the data rate. For CSS PHYs, a value of one indicates 250 kb/s while 
	 * a value of two indicates 1 Mb/s. For UWB PHYs, values 1–4 are valid and are defined 
	 * in 14.2.6.1. For all other PHYs, the parameter is set to zero.
	 */
	int dataRate = 1;

	/**
	 * Data sequence number of received frame.
	 */
	int DSN;
	
	public MacDataIndication802_15_4(
			MacAddress802_15_4 src, MacAddress802_15_4 dst, AbstractPduWrap msdu, 
			int msduLength, int sequenceNumber, double rxPower) {
		this.src = src;
		this.dst = dst;
		this.msdu = msdu;
		this.msduLenght = msduLength;
		this.DSN = sequenceNumber;
		this.rxPower = rxPower;
	}

	public MacDataIndication802_15_4(MacAddress802_15_4 src,
			MacAddress802_15_4 dst, int msduLenght, AbstractPduWrap msdu,
			UwbPRF uwbPRF, Ranging ranging, int dataRate, int dSN, double rxPower) {		
		this.src = src;
		this.dst = dst;
		this.msduLenght = msduLenght;
		this.msdu = msdu;
		this.uwbPRF = uwbPRF;
		this.ranging = ranging;
		this.dataRate = dataRate;
		this.DSN = dSN;
		this.rxPower = rxPower;
	}

	public MacAddress802_15_4 getSrc() {
		return src;
	}

	public MacAddress802_15_4 getDst() {
		return dst;
	}

	public int getMsduLenght() {
		return msduLenght;
	}

	public AbstractPduWrap getMsdu() {
		return msdu;
	}

	public UwbPRF getUwbPRF() {
		return uwbPRF;
	}

	public Ranging getRanging() {
		return ranging;
	}

	public int getDataRate() {
		return dataRate;
	}

	public int getDSN() {
		return DSN;
	}

	public double getRxPower() {
		return rxPower;
	}
	
	
	
	
}
