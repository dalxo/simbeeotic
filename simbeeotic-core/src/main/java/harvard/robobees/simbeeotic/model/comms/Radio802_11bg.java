package harvard.robobees.simbeeotic.model.comms;


/**
 * Model of the 802.11bg radio
 * @author Dalimir Orfanus
 *
 */
public class Radio802_11bg extends HalfDuplexDefaultRadio {

	
    public void initialize() {
    	super.initialize();
    	
    	setBandwidth(54000);;
    }

}
