package harvard.robobees.simbeeotic.model.protocol.IEEE802_15_4;

import java.util.Arrays;

import harvard.robobees.simbeeotic.model.protocol.EthernetMacAddress;
import harvard.robobees.simbeeotic.model.protocol.ProtocolAddress;

/**
 * MAC address for 802.15.4
 * Address can be either short (2B) or extended (8B).
 * @author Dalimir Orfanus
 *
 */
public class MacAddress802_15_4 extends ProtocolAddress {

	static final int LEN_SHORT = 2;
	static final int LEN_EXT = 8;
	
	boolean isShort = false;
	boolean broadcast = false;
	
	
	byte[] addr = new byte[LEN_EXT];
	 
	
	public MacAddress802_15_4(byte[] addr, boolean isShort) {		
		this.addr = addr;
		broadcast = true;
		for(int i = 0; i < addr.length; i++) {
			if(this.addr[i] != (byte)0xFF) {
				broadcast = false;
				break;
			}
		}
		
		this.isShort = isShort;
	}
	
	public byte[] getAddr() {
		return addr;
	}
		
	
	public boolean isShort() {
		return isShort;
	}
		
	@Override
	public boolean isBroadcast() {			
		return broadcast;
	}

	
	public static boolean isBroadcast(byte[] addr) {
		for(byte b : addr) {
			if(b != (byte)0xFF)
				return false;
		}
		
		return true;		
	}
	
	/**
	 * Static method to create broadcast address object
	 * @param isShort
	 * @return Broadcast address for MAC 802.15.4 of given length
	 */
	public static MacAddress802_15_4 getBroadcastAddr(boolean isShort) {
		int len = isShort ? LEN_SHORT : LEN_EXT;
		byte[] addr = new byte[len];
		
		for(int i = 0; i < len; i++) {
			addr[i] = (byte) 0xFF;
		}
		
		return new MacAddress802_15_4(addr, isShort);
				 
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null)
			return false;
		
		if(!(obj instanceof EthernetMacAddress))
			return false;
		
		return Arrays.equals(addr, ((EthernetMacAddress)obj).getAddress());
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(addr);
	}
	
	@Override
	public String toString() {
		if(addr == null)
			return "::NULL MAC address::";
		
		if(!isShort && addr.length != LEN_EXT)
			return "::Wrong MAC address::";
		
		if(isShort)
			return addr[0] + ":" + addr[1];		
		else
			return addr[0] + ":" 
			+ addr[1] + ":" 
			+ addr[2] + ":" 
			+ addr[3] + ":" 
			+ addr[4] + ":" 
			+ addr[5] + ":"
			+ addr[6] + ":"
			+ addr[7];
		
	}

}
