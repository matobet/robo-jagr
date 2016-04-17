package cz.muni.fi.rtsystems.robojagr;

import java.util.Timer;
import java.util.TimerTask;

import cz.muni.fi.rtsystems.robojagr.enums.Color;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.GraphicsLCD;

class Robot {
    private static final int WALL_DISTANCE_INFINITY = 1000;
    private IRSensor irSensor;
    private ColorSensor colorSensor;
    private Motors motors = new Motors();
    private GraphicsLCD lcd = LocalEV3.get().getGraphicsLCD();
    private int orientation = 0; // smer natoceni ve stupnich, vychozi 0, pri
                                 // otoceni jinem nez o 360 potreba
                                 // aktualizovat

    /**
     * otoci se a zjisti pozici a vzdalenost k beaconu
     */
    public void findBeacon() {
        irSensor.setBeaconDetector();
        motors.turn();

        while (true) {
            if (irSensor.getValue1() > -5 && irSensor.getValue1() < 5 && irSensor.getValue1() != 0) {
                motors.stop();
                orientation = (orientation + motors.turnedDegree()) % 360;
                // TODO vlozeni smeru beaconu (priblizne orientation) a
                // vzdalenosti (aktualni irSensor.getValue2()) do nejake mapy
                // prostredi
                break;
            }
        }
    }

    /**
     * Turn 360� and find the walls of the arena.
     * 
     */
    public void mapWalls() {
        int wallDistances[] = {WALL_DISTANCE_INFINITY, WALL_DISTANCE_INFINITY, WALL_DISTANCE_INFINITY, WALL_DISTANCE_INFINITY};
        irSensor.setDistanceDetector();
        motors.turn(360);
        while (true) {
            int distance = irSensor.getValue1();
            int maxValueIndex = findMaxValueIndex(wallDistances);
            if (distance < wallDistances[maxValueIndex]) {
                wallDistances[maxValueIndex] = distance;
            }
            if (motors.isFinished()) {
                break;
            }
        }
        // TODO we now have 4 minimal distances after turnin 360�. This, in theory, should correspond to the distances of walls.
        // What should be done: Calculate the wall lengths/map from the distances.
    }
    
    private int findMaxValueIndex(int[] intArray) {
        int value = 0;
        int index = 0;
        for (int i = 0; i < intArray.length; i++) {
            if (intArray[i] > value) {
                value = intArray[i];
                index = i;
            }
        }
        return index;
    }

    public void findGoal() {
        // TO DO
        // bude nejakym zpusobem jezdit a hledat branku + aktualizovat mapu
        // prostredi, pokud mozno nenarazi do beaconu nebo do zdi
        // az najde branku dojede k beaconu a zkusi ho tam dotlacit

        // priklad jen jede rovne dokud se nepriblizi ke zdi nebo nenajde branku
        // (nema pod sebou bilou barvu]
        irSensor.setDistanceDetector();
        motors.forward(200);
        while (true) {
            if ((irSensor.getValue1() < 10 && irSensor.getValue1() > 0) || colorSensor.getColor() == Color.WHITE) {
                motors.stop();
                break;
            }
        }

    }

    /**
     * uvodni info na displeji
     */
    public void introMessage() {
        lcd.drawString("Hockey Car", 5, 0, 0);
        lcd.drawString("press any key to", 5, 20, 0);
        lcd.drawString("start, esc to exit", 5, 40, 0);
        Button.waitForAnyPress();
        if (Button.ESCAPE.isDown())
            System.exit(0);
        lcd.clear();
    }

    /**
     * Initialize and start sensors
     */
    public void startSensors() {
        lcd.drawString("Starting sensors", 5, 0, 0);
        irSensor = new IRSensor();
        irSensor.setDaemon(true);
        irSensor.start();
        colorSensor = new ColorSensor();
        colorSensor.setDaemon(true);
        colorSensor.start();
        lcd.clear();
    }

    /**
     * Display sensor status on display. Press ESC to shut down
     */
    public void startTimer() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                showSensorsMessage(irSensor.isInDistanceMode());
                if (Button.ESCAPE.isDown())
                    System.exit(0);
            }
        }, 400, 400);
    }

    private void showSensorsMessage(boolean isInDistanceMode) {
        lcd.clear();
        if (isInDistanceMode) {
            lcd.drawString("IR distance: " + irSensor.getValue1(), 5, 0, 0);
        } else {
            lcd.drawString("IR distance: " + irSensor.getValue2(), 5, 0, 0);
            lcd.drawString("Direction: " + irSensor.getValue1(), 5, 20, 0);
        }
        lcd.drawString("Color sensor: " + colorSensor.getColor(), 5, 40, 0);
        lcd.drawString("Orientation: " + orientation, 5, 80, 0);            
    }
}