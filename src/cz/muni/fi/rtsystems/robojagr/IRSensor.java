package cz.muni.fi.rtsystems.robojagr;

import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.hardware.sensor.SensorMode;

class IRSensor extends Thread {
    private static final int OFFSET = 0;
    private EV3IRSensor ir = new EV3IRSensor(SensorPort.S4);
    private SensorMode sp = ir.getSeekMode();
    private boolean isInDistanceMode = false;
    private int value1;
    private int value2;

    public void run() {
        while (true) {
            float[] sample = new float[sp.sampleSize()];
            sp.fetchSample(sample, OFFSET);
            value1 = (int) sample[0];
            if (!isInDistanceMode()) {
                value2 = (int) sample[1];
            }
        }
    }

    /**
     * Returns the first value from the IR sensor:
     * 
     * In DistanceMode, the value represents distance
     * In BeaconMode, the value represents direction
     * 
     * @return the first value from the sensor
     */
    public int getValue1() {
        return value1;
    }

    /**
     * Returns the second value from the IR sensor:
     * 
     * In BeaconMode, the value represents direction
     * 
     * @return the second value from the sensor
     */
    public int getValue2() {
        return value2;
    }

    /**
     * Set IRSensor to beacon mode.
     */
    public void setBeaconDetector() {
        sp = ir.getSeekMode();
        isInDistanceMode = false;
    }

    /**
     * Set IRSensor to distance mode.
     */
    public void setDistanceDetector() {
        sp = ir.getDistanceMode();
        isInDistanceMode = true;
    }

    /**
     * Finds out whether the IR sensor is in distance mode.
     * 
     * @return true if IRSensor is in distance mode, false otherwise.
     */
    public boolean isInDistanceMode() {
        return isInDistanceMode;
    }

}
