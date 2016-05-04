package cz.muni.fi.rtsystems.robojagr;

public class JagrApplication {

    public static void main(String[] args) {
        Robot robot = new Robot();
        robot.introMessage();
        robot.startSensors();
        // robot.testForCalibrateDistanceRatio();
        robot.startTimer();
        robot.findBeacon();
        // robot.measureDistance();
        robot.turnAwayFromBeacon();
        robot.findGoal();
        robot.goToBeacon();
        robot.goRoundBeacon();
        robot.goToGoal();
        // atd.
    }
}
