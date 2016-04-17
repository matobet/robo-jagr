package cz.muni.fi.rtsystems.robojagr;

import cz.muni.fi.rtsystems.robojagr.enums.FieldMapEnum;

public class FieldMap {
    private FieldMapEnum[][] map;

    /**
     * Get the map
     * @return the map
     */
    public FieldMapEnum[][] getMap() {
        return map;
    }

    /**
     * Set the map
     * @param map the map to set
     */
    public void setMap(FieldMapEnum[][] map) {
        this.map = map;
    }
    
    
}
