package cz.muni.fi.rtsystems.robojagr;

import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.hardware.sensor.SensorMode;

class IRSensor extends Thread {
    private static final int OFFSET = 0;
    private EV3IRSensor ir = new EV3IRSensor(SensorPort.S4);
    private SensorMode sp = ir.getSeekMode();
    private boolean inDistanceMode = false;
    private int value1;
    private int value2;

    public void run() {
        while (true) {
            float[] sample = new float[sp.sampleSize()];
            sp.fetchSample(sample, OFFSET);
            value1 = (int) sample[0];
            if (!inDistanceMode) {
                value2 = (int) sample[1];
            }

        }
    }

    /**
     * hodnota ze senzoru - u distacemodu vzdalenost, u beaconmodu smer
     * 
     * @return
     */
    public int getValue1() {
        return value1;
    }

    /**
     * dalsi hodnota ze senzoru, u beaconmodu vzdalenost
     * 
     * @return
     */
    public int getValue2() {
        return value2;
    }

    /**
     * prepnuti do modu beacondetector
     */
    public void switchBeaconDetector() {
        sp = ir.getSeekMode();
        inDistanceMode = false;
    }

    /**
     * prepnuti do modu distancedetector
     */
    public void switchDistanceDetector() {
        sp = ir.getDistanceMode();
        inDistanceMode = true;
    }

    public boolean isInDistanceMode() {
        return inDistanceMode;
    }

}
