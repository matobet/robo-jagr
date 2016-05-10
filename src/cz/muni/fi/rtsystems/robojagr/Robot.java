package cz.muni.fi.rtsystems.robojagr;

import java.util.Timer;
import java.util.TimerTask;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.GraphicsLCD;

class Robot {
	
	public static final int DISPLAY_REFRESH_TIME = 400;
	public static final int HALF_GOAL_DISTANCE = 8;
	public static final int[] GO_ROUND_BEACON_PARAMS = {7,22,7};

    private IRSensor irSensor;
    private ColorSensor colorSensor;
    private Motors motors = new Motors();
    private GraphicsLCD lcd = LocalEV3.get().getGraphicsLCD();
    // direction of the robot, 0 default; not really used in this version
    private int orientation = 0;

    
    /**
     * Display an intro message on the LCD
     * 
     */
    public void introMessage() {
        lcd.drawString("ROBO-JAGR", 5, 0, 0);
        lcd.drawString("press any key to", 5, 20, 0);
        lcd.drawString("start, esc to exit", 5, 40, 0);
        waitForButton();
        lcd.clear();
    }

    /**
     * Initialize sensors
     * 
     */
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
     * Refresh info on LCD
     * 
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
        }, DISPLAY_REFRESH_TIME, DISPLAY_REFRESH_TIME);
    }

    /**
     * Move along the edge of the arena until the goal is found.
     * 
     */
    public void findGoal() {
    	irSensor.switchDistanceDetector();
    	motors.forward();
        boolean success;
        while (true) {
            if (irSensor.isObjectAhead()) {
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
        	closerCarefully();
            turnAndOrientation(90);          
            findGoal();
        }
        irSensor.switchBeaconDetector();
    }

    private void closerCarefully() {
    	motors.forward(motors.DEFAULT_SPEED/2);
    	while (true) {
            if (irSensor.isCloseToWall()) {
            	motors.stop();
                break;
            }
    	}
    }
    
    /**
     * Move robot to the center of the goal.
     */
    public void goToMiddleOfGoal() {
        forwardAndUpdate(Motors.DEFAULT_SPEED, HALF_GOAL_DISTANCE);
    }

    /**
     * Find IR beacon and move to it.
     */
    public void goToBeacon() {
    	irSensor.switchBeaconDetector();
        findBeacon();
        motors.forward();
        while (true) {
            if (irSensor.isObjectAhead()) {
                motors.stop();
                break;
            }
        }
    }
    
    /**
     * Turn the robot towards the IR beacon.
     * 
     */
    private void findBeacon() {
        motors.turn();
        stopTowardsBeacon(-15,0);

        motors.turnSlower();
        stopTowardsBeacon(-2,1);
    }
    
    private void stopTowardsBeacon(int lowerThreshold, int upperThreshold) {
    	while (true) {
            if (irSensor.getDirection() > lowerThreshold && irSensor.getDirection() < upperThreshold) {
            	motors.stop();
            	orientation = (orientation + motors.turnedDegree()) % 360;
                break;
            }
        }
    }
    
    /**
     * Move behind the beacon.
     * 
     */
    public void goRoundBeacon() {
    	irSensor.switchDistanceDetector();
        turnAndOrientation(-90);
        forwardAndUpdate(Motors.DEFAULT_SPEED, GO_ROUND_BEACON_PARAMS[0]);
        turnAndOrientation(90);
        forwardAndUpdate(Motors.DEFAULT_SPEED, GO_ROUND_BEACON_PARAMS[1]);
        turnAndOrientation(90);
        forwardAndUpdate(Motors.DEFAULT_SPEED, GO_ROUND_BEACON_PARAMS[2]);
        turnAndOrientation(90);
    }

    /**
     * Push the beacon towards the goal.
     */
    public void goToGoal() {
    	irSensor.switchDistanceDetector();
    	motors.forward();
        while (true) {
            if (irSensor.isObjectAhead() || colorSensor.isWhite()) {
                motors.stop();
                break;
            }
        }
        System.exit(0);
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


    private void waitForButton() {
        Button.waitForAnyPress();
        if (Button.ESCAPE.isDown())
            System.exit(0);
    }

}
