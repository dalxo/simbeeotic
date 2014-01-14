package harvard.robobees.simbeeotic.model.comms;


/**
 * Model of the 802.15.4 radio
 * @author Dalimir Orfanus
 *
 */
public class Radio802_15_4 extends HalfDuplexDefaultRadio {

	
    public void initialize() {
    	super.initialize();
    	
    	setBandwidth(250);;
    }

}
