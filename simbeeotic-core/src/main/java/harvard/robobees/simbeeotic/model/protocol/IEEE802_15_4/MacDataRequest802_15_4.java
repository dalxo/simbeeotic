package harvard.robobees.simbeeotic.model.protocol.IEEE802_15_4;

import harvard.robobees.simbeeotic.model.protocol.AbstractPduWrap;
import harvard.robobees.simbeeotic.model.protocol.IEEE802_15_4.MAC802_15_4.Ranging;
import harvard.robobees.simbeeotic.model.protocol.IEEE802_15_4.MAC802_15_4.UwbPRF;

/**
 * Class that represents request for data service (send MSDU). 
 * @author Dalimir Orfanus
 *
 */
public class MacDataRequest802_15_4 implements MacDataService802_15_4 {

	MacAddress802_15_4 srcAddr;
	MacAddress802_15_4 dstAddr;

	Byte msduHandle = 0x00;
	
	int msduLenght = 0;
		
	/**
	 * MAC service datagram unit
	 */
	AbstractPduWrap msdu;
	
	boolean ackTx = true;
	boolean gtsTx = true;
	boolean indirectTx = false;
	
	UwbPRF uwbPRF = UwbPRF.PRF_OFF;
	
	Ranging ranging = Ranging.NON_RANGING;
	
	/**
	 * The preamble symbol repetitions of the UWB PHY frame. A zero value is used 
	 * for non-UWB PHYs. Valid range 0, 16, 64, 1024, 4096.
	 */
	int uwbPreambleSymRep = 0;
	
	/**
	 * Indicates the data rate. For CSS PHYs, a value of one indicates 250 kb/s while 
	 * a value of two indicates 1 Mb/s. For UWB PHYs, values 1–4 are valid and are defined 
	 * in 14.2.6.1. For all other PHYs, the parameter is set to zero.
	 */
	int dataRate = 1;

	public MacDataRequest802_15_4(MacAddress802_15_4 srcAddr,
			MacAddress802_15_4 dstAddr, byte msduHandle, int msduLenght,
			AbstractPduWrap msdu) {
		super();
		this.srcAddr = srcAddr;
		this.dstAddr = dstAddr;
		this.msduHandle = msduHandle;
		this.msduLenght = msduLenght;
		this.msdu = msdu;
	}

	public MacDataRequest802_15_4(MacAddress802_15_4 srcAddr,
			MacAddress802_15_4 dstAddr, byte msduHandle, int msduLenght,
			AbstractPduWrap msdu, boolean ackTx) {
		super();
		this.srcAddr = srcAddr;
		this.dstAddr = dstAddr;
		this.msduHandle = msduHandle;
		this.msduLenght = msduLenght;
		this.msdu = msdu;
		this.ackTx = ackTx;
	}

	public MacAddress802_15_4 getSrcAddr() {
		return srcAddr;
	}

	public void setSrcAddr(MacAddress802_15_4 srcAddr) {
		this.srcAddr = srcAddr;
	}

	public MacAddress802_15_4 getDstAddr() {
		return dstAddr;
	}

	public void setDstAddr(MacAddress802_15_4 dstAddr) {
		this.dstAddr = dstAddr;
	}

	public byte getMsduHandle() {
		return msduHandle;
	}

	public void setMsduHandle(byte msduHandle) {
		this.msduHandle = msduHandle;
	}

	public int getMsduLenght() {
		return msduLenght;
	}

	public void setMsduLenght(int msduLenght) {
		this.msduLenght = msduLenght;
	}

	public AbstractPduWrap getMsdu() {
		return msdu;
	}

	public void setMsdu(AbstractPduWrap msdu) {
		this.msdu = msdu;
	}

	public boolean isAckTx() {
		return ackTx;
	}

	public void setAckTx(boolean ackTx) {
		this.ackTx = ackTx;
	}

	public boolean isGtsTx() {
		return gtsTx;
	}

	public void setGtsTx(boolean gtsTx) {
		this.gtsTx = gtsTx;
	}

	public boolean isIndirectTx() {
		return indirectTx;
	}

	public void setIndirectTx(boolean indirectTx) {
		this.indirectTx = indirectTx;
	}

	public UwbPRF getUwbPRF() {
		return uwbPRF;
	}

	public void setUwbPRF(UwbPRF uwbPRF) {
		this.uwbPRF = uwbPRF;
	}

	public Ranging getRanging() {
		return ranging;
	}

	public void setRanging(Ranging ranging) {
		this.ranging = ranging;
	}

	public int getUwbPreambleSymRep() {
		return uwbPreambleSymRep;
	}

	public void setUwbPreambleSymRep(int uwbPreambleSymRep) {
		this.uwbPreambleSymRep = uwbPreambleSymRep;
	}

	public int getDataRate() {
		return dataRate;
	}

	public void setDataRate(int dataRate) {
		this.dataRate = dataRate;
	}
	

}
