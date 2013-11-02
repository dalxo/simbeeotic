package harvard.robobees.simbeeotic.model.protocol;

/**
 * Enumerator for communication layer types. Inspired, taken and rewritten from 
 * the ShoX WSN simulator http://shox.sourceforge.net. 
 * 
 * @author Dalimir Orfanus, ShoX developers
 */
public enum LayerType implements Comparable<LayerType> {
	
	/**
	 * Physical layer.
	 */
	PHYSICAL(1, "Physical", "phy"),
	
	/**
	 * Data Link layer
	 */
	DLL(2, "Data Link Layer", "dll"),
		
	/**
	 * Network layer.
	 */
	NETWORK(3, "Network", "net"),
	
	/**
	 * Transport layer.
	 */
	TRANSPORT(4, "Transport", "tra"),
	
	/**
	 * Session layer.
	 */
	SESSION(5, "Session", "sess"),

	/**
	 * Presentation layer.
	 */
	PRESENTATION(6, "Presenatation", "pre"),

	/**
	 * Application layer.
	 */
	APPLICATION(7, "Application", "app");
	
	/** 
	 * String describing a layer. 
	 */
	private final String description;
	
	/**
	 * Index of a layer type.
	 * Should not be used outside this class.
	 */
	private final int index;
	
	/**
	 * Short name of the layer type.
	 */
	private final String shortName;
	
	/**
	 * Returns the short name of the layer type.
	 * @return a short name
	 */
	public String getShortName() {
		return shortName;
	}
    
    /**
     * Private constructor of the class LayerType.
     * 
     * @param idx The index of the created layertype.
     * @param desc String description of the new layer type.
     * @param shortName a short name of the layer type
     */
    private LayerType(int idx, String desc, String shortName) {
    	this.index = idx;
    	this.description = desc;
    	this.shortName = shortName;
    }
    
    /**
     * Private method to get the layer by the index.
     * This method should be private to prevent other to rely on index
     * numbers
     * @param index an index between 0 and 6
     * @return a layer type or null
     */
    private LayerType getLayerByIndex(int index) {
    	switch(index) {
    	case 1: return PHYSICAL;
    	case 2: return DLL;
    	case 3: return NETWORK;
    	case 4: return TRANSPORT;
    	case 5: return SESSION;
    	case 6: return PRESENTATION;
    	case 7: return APPLICATION;
    	default:
    		assert false;
    	}
    	return null;
    }
	
    /**
     * Returns the next upper layer .
     * @return the next upper layer or null
     */
	public LayerType getUpperLayer() {
		if (this == APPLICATION) {
			return null;
		}
		return getLayerByIndex(index + 1);
	}

	/**
	 * Returns the next lower layer.
	 * @return the next lower layer or null
	 */
	public LayerType getLowerLayer() {
		if (this == PHYSICAL) {
			return null;
		}
		return getLayerByIndex(index - 1);
	}
	
	/**
	 * @see java.lang.Object#toString()
	 * @return Description of the layertype as String.
	 */
	@Override
	public String toString() {
		return description;
	}
}
