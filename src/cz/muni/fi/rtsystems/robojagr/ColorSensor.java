
package cz.muni.fi.rtsystems.robojagr;

import cz.muni.fi.rtsystems.robojagr.enums.Color;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorMode;

class ColorSensor extends Thread {
    private static final int OFFSET = 0;
    private EV3ColorSensor cs = new EV3ColorSensor(SensorPort.S1);
    private SensorMode sp = cs.getColorIDMode();
    private volatile int colorNumber = 0;

    public void run() {
        while (true) {
            float[] sample = new float[sp.sampleSize()];
            sp.fetchSample(sample, OFFSET);
            colorNumber = (int) sample[0];
        }
    }

    /**
     * Returns the color seen by the color sensor
     * @return Color color of sensor
     */
    public Color getColor() {
        return Color.getColor(colorNumber);
    }

}