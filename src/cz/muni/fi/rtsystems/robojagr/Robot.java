package cz.muni.fi.rtsystems.robojagr;

import java.util.Timer;
import java.util.TimerTask;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.GraphicsLCD;

class Robot {

    public static final int SPEED = 200;

    private IRSensor irSensor;
    private ColorSensor colorSensor;
    private Motors motors = new Motors();
    private GraphicsLCD lcd = LocalEV3.get().getGraphicsLCD();
    private int orientation = 0; // smer natoceni ve stupnich, vychozi 0, pri

    public void testForCalibrateDistanceRatio() {
        irSensor.switchBeaconDetector();
        int bd = irSensor.getDistance();
        motors.forward(100);
        while (true) {
            if (irSensor.getDistance() <= bd - 20) {
                motors.stop();
                break;
            }
        }
        lcd.clear();
        lcd.drawString("bd-20/d: " + (bd - 20) + "/" + motors.drivenDistance(), 5, 0, 0);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (Button.ESCAPE.isDown())
                    System.exit(0);
            }
        }, 400, 400);
    }

    /**
     * otoci se a zjisti pozici a vzdalenost k beaconu
     */
    public void findBeacon() {
        irSensor.switchBeaconDetector();
        motors.turn();

        while (true) {
            if (irSensor.getDirection() > -5 && irSensor.getDirection() < 5 && irSensor.getDirection() != 0) {
                motors.stop();
                orientation = (orientation + motors.turnedDegree()) % 360;
                break;
            }
        }
    }

    public void findGoal() {
        irSensor.switchDistanceDetector();

        goRoundCorners();
    }

    /**
     * posun sa do stredu branky aby sme mohli odtial vyrazit k beaconu
     */
    public void goToMiddleOfGoal() {
        // TODO: najst spravnu magic constantu pre `distance` od kraju branku do stredu
        forwardAndUpdate(SPEED, 8);
    }

    /**
     * uvodni info na displeji
     */
    public void introMessage() {
        lcd.drawString("Hockey Car", 5, 0, 0);
        lcd.drawString("press any key to", 5, 20, 0);
        lcd.drawString("start, esc to exit", 5, 40, 0);
        waitForButton();
        lcd.clear();
    }

    public void startSensors() {
        lcd.drawString("...starting sensors", 5, 0, 0);
        irSensor = new IRSensor();
        irSensor.setDaemon(true);
        irSensor.start();
        colorSensor = new ColorSensor();
        colorSensor.setDaemon(true);
        colorSensor.start();
        lcd.clear();
    }

    /**
     * prubezne zobrazeni udaju ze senzoru, vypnuti pri esc
     */
    public void startTimer() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (irSensor.isInDistanceMode()) {
                    sensorsMessageDistance();
                } else {
                    sensorsMessageBeacon();
                }
                if (Button.ESCAPE.isDown())
                    System.exit(0);
            }
        }, 400, 400);
    }

    public void goToBeacon() {
        findBeacon();
        motors.forward(SPEED);
        while (true) {
            if (irSensor.isWallAhead()) {
                motors.stop();
                break;
            }
        }
    }

    public void goRoundBeacon() {
        turnAndOrientation(-90);
        forwardAndUpdate(SPEED, 8);
        turnAndOrientation(90);
        forwardAndUpdate(SPEED, 22);
        turnAndOrientation(90);
        forwardAndUpdate(SPEED, 8);
        turnAndOrientation(90);

    }

    public void goToGoal() {
        motors.forward(SPEED);
        irSensor.switchDistanceDetector();
        while (true) {
            if (irSensor.isWallAhead()) {
                motors.stop();
                break;
            }
        }
    }

    private void turnAndOrientation(int degree) {
        motors.turn(degree);
        waitTillFinish();
        orientation = (orientation + degree) % 360;
    }

    private void forwardAndUpdate(int speed, int distance) {
        motors.forward(speed, distance);
        waitTillFinish();
    }

    private void sensorsMessageBeacon() {
        lcd.clear();
        lcd.drawString("IR distance: " + irSensor.getDistance(), 5, 0, 0);
        lcd.drawString("Direction: " + irSensor.getDirection(), 5, 20, 0);
        lcd.drawString("Color sensor: " + colorSensor.getColor(), 5, 40, 0);
        lcd.drawString("Orientation: " + orientation, 5, 80, 0);
    }

    private void sensorsMessageDistance() {
        lcd.clear();
        lcd.drawString("IR distance: " + irSensor.getDistance(), 5, 0, 0);
        lcd.drawString("Color sensor: " + colorSensor.getColor(), 5, 40, 0);
        lcd.drawString("Orientation: " + orientation, 5, 80, 0);
    }

    private void waitTillFinish() {
        while (true) {
            if (motors.isFinished()) {
                break;
            }
        }
    }

    private void goRoundCorners() {
        motors.forward(SPEED);
        boolean success;
        while (true) {
            if (irSensor.isWallAhead()) {
                motors.stop();
                success = false;
                break;
            }
            if (colorSensor.isWhite()) {
                motors.stop();
                success = true;
                break;
            }
        }
        if (!success) {
            turnAndOrientation(90);
            waitForButton();
            goRoundCorners();
        }
        irSensor.switchBeaconDetector(); // chvili trva prepnuti
        waitForButton();
    }

    private void waitForButton() {
        Button.waitForAnyPress();
        if (Button.ESCAPE.isDown())
            System.exit(0);
    }

}
