
package cz.muni.fi.rtsystems.robojagr;

import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorMode;

class ColorSensor extends Thread {

    public static int WHITE = -1;

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
     * zjisteni hodnoty barvy pod predkem vozitka, bila = -1
     */
    public int getColor() {
        return color;
    }

    public boolean isWhite() {
        return color == WHITE;
    }

}
