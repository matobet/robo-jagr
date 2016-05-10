package cz.muni.fi.rtsystems.robojagr;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;

class Motors {

    public static final double TURN_RATIO = 3.65;
    // to have distance aprox. in sensor distance units
    public static final double DISTANCE_RATIO = 73;
    public static final int DEFAULT_SPEED = 200;
    public static final int DEFAULT_TURN_SPEED = 150;

    private EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.C);
    private EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.B);

    /**
     * Drives forward until stop() is called
     *
     * @param speed speed
     */
    public void forward(int speed) {
        rightMotor.resetTachoCount();
        rightMotor.setSpeed(speed);
        leftMotor.setSpeed(speed);
        rightMotor.forward();
        leftMotor.forward();
    }

    /**
     * Drives forward until stop() is called
     */
    public void forward() {
        forward(DEFAULT_SPEED);
    }

    /**
     * Measure the driven distance.
     * 
     * @return distance driven
     */
    public int drivenDistance() {
        return (int) Math.round(rightMotor.getTachoCount() / DISTANCE_RATIO);
    }

    /**
     * Stop motors.
     */
    public void stop() {
        rightMotor.stop(true);
        leftMotor.stop(true);
    }

    /**
     * Drives forward a specified distance.
     * 
     * @param speed speed
     * @param distance distance
     */
    public void forward(int speed, int distance) {
        rightMotor.setSpeed(speed);
        leftMotor.setSpeed(speed);
        rightMotor.rotate((int) Math.round(DISTANCE_RATIO * distance), true);
        leftMotor.rotate((int) Math.round(DISTANCE_RATIO * distance), true);
    }

    /**
     * Turns by specified degree (positive = anti-clockwise).
     *
     * @param degree angle to turn by
     */
    public void turn(int degree) {
        rightMotor.setSpeed(DEFAULT_TURN_SPEED);
        leftMotor.setSpeed(DEFAULT_TURN_SPEED);
        rightMotor.rotate((int) Math.round(TURN_RATIO * degree), true);
        leftMotor.rotate((int) Math.round(-TURN_RATIO * degree), true);
    }

    /**
     * Turn anti-clockwise until stop() is called.
     * 
     */
    public void turn() {
        rightMotor.resetTachoCount();
        rightMotor.setSpeed(DEFAULT_TURN_SPEED);
        leftMotor.setSpeed(DEFAULT_TURN_SPEED);
        rightMotor.forward();
        leftMotor.backward();

    }

    /**
     * Turn anti-clockwise slowly until stop() is called.
     * 
     */
    public void turnSlower() {
        rightMotor.resetTachoCount();
        rightMotor.setSpeed(DEFAULT_TURN_SPEED/3);
        leftMotor.setSpeed(DEFAULT_TURN_SPEED/3);
        rightMotor.forward();
        leftMotor.backward();
    }

    /**
     * Gets the angle turned by the robot.
     * 
     * @return angle turned
     */
    public int turnedDegree() {
        return (int) Math.round(rightMotor.getTachoCount() / TURN_RATIO);
    }

    /**
     * Finds out whether the motors have finished rotating.
     * 
     * @return true if they finished rotating
     */
    public boolean isFinished() {
        return !rightMotor.isMoving() && !leftMotor.isMoving();
    }
}
