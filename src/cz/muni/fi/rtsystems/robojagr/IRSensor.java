package cz.muni.fi.rtsystems.robojagr;

import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.hardware.sensor.SensorMode;

class IRSensor extends Thread {
    private EV3IRSensor ir = new EV3IRSensor(SensorPort.S4);
    private SensorMode sp = ir.getSeekMode();
    private boolean inDistanceMode = false;
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
     * hodnota ze senzoru - vzdalenost u distance /beacon modu
     *
     * @return
     */
    public int getDistance() {
        if (inDistanceMode) {
            return value1;
        } else {
            return value2;
        }
    }

    /**
     * dalsi hodnota ze senzoru, u beaconmodu vzdalenost
     *
     * @return
     */
    public int getDirection() {
        if (!inDistanceMode) {
            return value1;
        } else {
            return 0;
        }
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
