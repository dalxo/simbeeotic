package harvard.robobees.simbeeotic.model.protocol;

import java.util.Arrays;

public class EthernetMacAddress extends ProtocolAddress {

	byte[] addr;
	
	public EthernetMacAddress(byte[] addr) {
		this.addr = addr;
	}
	
	public EthernetMacAddress(int a, int b, int c, int d, int e, int f) {
		addr = new byte[] {(byte) a, (byte) b, (byte) c, (byte) d, (byte) e, (byte) f};
	}

	public EthernetMacAddress(byte a, byte b, byte c, byte d, byte e, byte f) {
		addr = new byte[] {a, b, c, d, e, f};
	}

	public static final byte[] broadcast = new byte[]{(byte) 0xFF,(byte) 0xFF,(byte) 0xFF,(byte) 0xFF,(byte) 0xFF,(byte) 0xFF};
	
	public boolean isBroadcast() {
		return EthernetMacAddress.isBroadcast(this.addr);
	}
	
	public byte[] getAddress() {
		return this.addr;
	}
	
	public static boolean isBroadcast(byte[] addr) {
		for(byte b : addr) {
			if(b != (byte)0xFF)
				return false;
		}
		
		return true;		
	}
	
	public static EthernetMacAddress getBroadcast() {
		return new EthernetMacAddress(broadcast);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null)
			return false;
		
		if(!(obj instanceof EthernetMacAddress))
			return false;
		
		return Arrays.equals(addr, ((EthernetMacAddress)obj).getAddress());
	}
	
	public int hashCode() {
		return Arrays.hashCode(addr);
	}
}
