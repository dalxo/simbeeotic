package harvard.robobees.simbeeotic.model.protocol.IEEE802_15_4;

/**
 * Class that represents MAC data confirmation primitive (MCPS-DATA.confirm).
 * @author Dalimir Orfanus
 *
 */
public class MacDataConfirm802_15_4 implements MacDataService802_15_4 {

	public enum Status {
		SUCCESS,
		TRANSACTION_OVERFLOW,
		TRANSACTION_EXPIRED,
		CHANNEL_ACCESS_FAILURE,
		INVALID_ADDRESS,
		INVALID_GTS, NO_ACK,
		COUNTER_ERROR,
		FRAME_TOO_LONG,
		UNAVAILABLE_KEY,
		UNSUPPORTED_SECURITY,
		INVALID_PARAMETER
	};
	
	/**
	 * The handle associated with the MSDU being confirmed.
	 */
	Byte msduHandle = 0x00;
	
	/**
	 * The status of the last MSDU transmission.
	 */
	Status status;

	public MacDataConfirm802_15_4(Byte msduHandle, Status status) {
		super();
		this.msduHandle = msduHandle;
		this.status = status;
	}

	public Byte getMsduHandle() {
		return msduHandle;
	}

	public Status getStatus() {
		return status;
	}
	
	
	
}
