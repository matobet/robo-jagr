package cz.muni.fi.rtsystems.robojagr;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;

class Motors {

    public static final double TURN_RATIO = 3.65;
    public static final double DISTANCE_RATIO = 73; // v jednotkach priblizne
    public static final int DEFAULT_SPEED = 200;

    // odpovidajici ir detectoru
    private EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.C);
    private EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.B);

    /**
     * jede dopredu dokud nezavolano stop()
     *
     * @param speed
     *            rychlost
     */
    public void forward(int speed) {
        rightMotor.resetTachoCount();
        rightMotor.setSpeed(speed);
        leftMotor.setSpeed(speed);
        rightMotor.forward();
        leftMotor.forward();
    }

    public void forward() {
        forward(DEFAULT_SPEED);
    }

    public int drivenDistance() {
        return (int) Math.round(rightMotor.getTachoCount() / DISTANCE_RATIO);
    }

    /**
     * zastavi
     */
    public void stop() {
        rightMotor.stop(true);
        leftMotor.stop(true);
    }

    public void forward(int speed, int distance) {
        rightMotor.setSpeed(speed);
        leftMotor.setSpeed(speed);
        rightMotor.rotate((int) Math.round(DISTANCE_RATIO * distance), true);
        leftMotor.rotate((int) Math.round(DISTANCE_RATIO * distance), true);
    }

    /**
     * zatoci na miste o dany pocet stupnu kladne toci proti smeru hodin
     *
     * @param degree
     *            uhel
     */
    public void turn(int degree) {
        rightMotor.setSpeed(200);
        leftMotor.setSpeed(200);
        rightMotor.rotate((int) Math.round(TURN_RATIO * degree), true);
        leftMotor.rotate((int) Math.round(-TURN_RATIO * degree), true);
    }

    /**
     * zataci proti smeru hodin, dokud nezavolano stop()
     */
    public void turn() {
        rightMotor.resetTachoCount();
        rightMotor.setSpeed(200);
        leftMotor.setSpeed(200);
        rightMotor.forward();
        leftMotor.backward();

    }

    public int turnedDegree() {
        return (int) Math.round(rightMotor.getTachoCount() / TURN_RATIO);
    }

    public boolean isFinished() {
        return !rightMotor.isMoving() && !leftMotor.isMoving();
    }
}
