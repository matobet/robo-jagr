package cz.muni.fi.rtsystems.robojagr;

public class JagrApplication {

    public static void main(String[] args) {
        Robot robot = new Robot();
        robot.introMessage();
        robot.startSensors();
        robot.startTimer();
        robot.findBeacon();
        robot.mapWalls();
        robot.findGoal();
    }
}