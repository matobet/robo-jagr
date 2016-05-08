package cz.muni.fi.rtsystems.robojagr;

import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.hardware.sensor.SensorMode;

class IRSensor extends Thread {
    public static final int STOP_WALL_THRESHOLD = 10;
    public static final int STOP_BEACON_THRESHOLD = 11;
    public static final int STOP_CLOSE_WALL_THRESHOLD = 1;

    private EV3IRSensor ir = new EV3IRSensor(SensorPort.S4);
    private SensorMode sp = ir.getDistanceMode();
    private boolean inDistanceMode = true;
    private int value1;
    private int value2;

    public void run() {
        while (true) {
            int size = sp.sampleSize();
            float[] sample = new float[size];
            sp.fetchSample(sample, 0);
            value1 = (int) sample[0];
            if (!inDistanceMode && size > 1) {
                value2 = (int) sample[1];
            }

        }
    }

    /**
     * distance value from sensor - for both distance and beacon mode
     *
     * @return distance in sensor units
     */
    public int getDistance() {
        if (inDistanceMode) {
            return value1;
        } else {
            return value2;
        }
    }

    /**
     * if there is wall or beacon (in beacon mode) closer than threshold
     * @return true if object is close
     */
    public boolean isObjectAhead() {
        int threshold;
    	if (inDistanceMode) {
        	threshold = STOP_WALL_THRESHOLD;
        } else {
        	threshold = STOP_BEACON_THRESHOLD;
        }
    	return getDistance() < threshold && getDistance() != 0;
    }
    
    public boolean isCloseToWall() {
    	return getDistance() < STOP_CLOSE_WALL_THRESHOLD;
    }
    
    /**
     * direction value from beacon mode
     *
     * @return direction value
     */
    public int getDirection() {
        if (!inDistanceMode) {
            return value1;
        } else {
            return 0;
        }
    }

    /**
     * switch to beacon mode
     */
    public void switchBeaconDetector() {
        if (inDistanceMode) {
        	sp = ir.getSeekMode();
            inDistanceMode = false;
        }	
    }

    /**
     * switch detector to distance mode
     */
    public void switchDistanceDetector() {
    	if (!inDistanceMode) {
    		sp = ir.getDistanceMode();
    		inDistanceMode = true;
    	}
    }

    public boolean isInDistanceMode() {
        return inDistanceMode;
    }

}
