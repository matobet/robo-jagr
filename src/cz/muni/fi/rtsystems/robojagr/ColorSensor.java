
package cz.muni.fi.rtsystems.robojagr;

import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorMode;

/**
 * Color sensor manager.
 *
 */
class ColorSensor extends Thread {

    public static final int WHITE = -1;

    private EV3ColorSensor cs = new EV3ColorSensor(SensorPort.S1);
    private SensorMode sp = cs.getColorIDMode();
    private int color = 0;

    public void run() {
        while (true) {
            float[] sample = new float[sp.sampleSize()];
            sp.fetchSample(sample, 0);
            color = (int) sample[0];
        }
    }

    /**
     * Return color index, white = -1
     */
    public int getColor() {
        return color;
    }

    /**
     * Find out whether the detected color is white.
     * 
     * @return true if detected color is white
     */
    public boolean isWhite() {
        return color == WHITE;
    }

}
