package cz.muni.fi.ia158.robot;

import java.util.Timer;
import java.util.TimerTask;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.hardware.sensor.SensorMode;

/**
 * 
 * dalsi jednoducha verze, robot jede kolem mantinelu (musi byt postaveno
 * rovnobezne), hleda branku, az ji najde dojede k beaconu, objede ho a zatlaci
 * do branky... zatim nespolehlive, puvodne jsem chtel vyuzivat smery a
 * vzdalenosti, ale je to nespolehlive a asi i chybne implementovana
 *
 */
public class RobotClasses {

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

class Robot {
	private IRSensor irSensor;
	private ColorSensor colorSensor;
	private Motors motors = new Motors();
	private GraphicsLCD lcd = LocalEV3.get().getGraphicsLCD();
	private int orientation = 0; // smer natoceni ve stupnich, vychozi 0, pri
									// otoceni jinem nez o 360 potreba
									// aktualizovat
	private int beaconDirection;
	private int beaconDistance;
	private int goalDirection;
	private int goalDistance;

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
				beaconDirection = orientation;
				beaconDistance = irSensor.getDistance();
				// TO DO vlozeni smeru beaconu (priblizne orientation) a
				// vzdalenosti (aktualni irSensor.getDistance()) do nejake mapy
				// prostredi
				break;
			}
		}
	}

	/**
	 * otoci se a zjisti vzdalenosti ke kraji
	 */
	public void measureDistance() {

		irSensor.switchDistanceDetector();
		motors.turn(360);
		waitTillFinish();
		// TO DO vlozeni hodnot do nejake mapy prostredi
		// asi zjistovat prubezne orientaci a ukladat vzdalenosti ke kraji
		// (irSensor.getDistance())
	}

	/**
	 * otoci se o 90-180st. od beaconu, ke kolmici
	 */
	public void turnAwayFromBeacon() {
		int turnAngle = 180 - (beaconDirection % 90);
		turnAndOrientation(turnAngle);
	}

	public void findGoal() {
		// TO DO
		// bude nejakym zpusobem jezdit a hledat branku + aktualizovat mapu
		// prostredi, pokud mozno nenarazi do beaconu nebo do zdi
		// az najde branku dojede k beaconu a zkusi ho tam dotlacit

		// priklad jen jede rovne dokud se nepriblizi ke zdi nebo nenajde branku
		// (nema pod sebou bilou barvu]
		irSensor.switchDistanceDetector();
		goRoundCorners();

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
		motors.forward(200);
		while (true) {
			if (irSensor.getDistance() <= 10 && irSensor.getDistance() != 0) {
				motors.stop();
				updateBeaconLocation(motors.drivenDistance());
				updateGoalLocation(motors.drivenDistance());
				break;
			}
		}
	}

	public void goRoundBeacon() {
		turnAndOrientation(-90);
		forwardAndUpdate(200, 8);
		turnAndOrientation(90);
		forwardAndUpdate(200, 22);
		turnAndOrientation(90);
		forwardAndUpdate(200, 8);
		turnAndOrientation(90);

	}

	public void goToGoal() {
		motors.forward(200);
		irSensor.switchDistanceDetector();
		while (true) {
			if ((irSensor.getDistance() < 10 && irSensor.getDistance() != 0)) {
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
		updateBeaconLocation(distance);
		updateGoalLocation(distance);
	}

	private void sensorsMessageBeacon() {
		lcd.clear();
		lcd.drawString("IR distance: " + irSensor.getDistance(), 5, 0, 0);
		lcd.drawString("Direction: " + irSensor.getDirection(), 5, 20, 0);
		lcd.drawString("Color sensor: " + colorSensor.getColor(), 5, 40, 0);
		lcd.drawString("Orientation: " + orientation, 5, 80, 0);
		lcd.drawString("B-o/d: " + beaconDirection + "/" + beaconDistance, 5, 100, 0);
	}

	private void sensorsMessageDistance() {
		lcd.clear();
		lcd.drawString("IR distance: " + irSensor.getDistance(), 5, 0, 0);
		lcd.drawString("Color sensor: " + colorSensor.getColor(), 5, 40, 0);
		lcd.drawString("Orientation: " + orientation, 5, 80, 0);
		lcd.drawString("B-o/d: " + beaconDirection + "/" + beaconDistance, 5, 100, 0);
	}

	private void waitTillFinish() {
		while (true) {
			if (motors.isFinished()) {
				break;
			}
		}
	}

	private void updateBeaconLocation(int distance) {
		int gama = Math.abs(orientation - beaconDirection);
		int a = beaconDistance;
		int b = distance;
		int[] dirDis = remapCoordinates(gama, a, b);
		beaconDirection = dirDis[0];
		beaconDistance = dirDis[1];
	}

	private void updateGoalLocation(int distance) {
		int gama = Math.abs(orientation - goalDirection);
		int a = goalDistance;
		int b = distance;
		int[] dirDis = remapCoordinates(gama, a, b);
		goalDirection = dirDis[0];
		goalDistance = dirDis[1];
	}

	private int[] remapCoordinates(int gama, int a, int b) {
		// kozinova veta :-)
		double c = Math.sqrt(a * a + b * b + 2 * a * b * Math.cos(gama * Math.PI / 180));
		// synova veta :-)
		double alfa = Math.asin(a / c * Math.sin(gama * Math.PI / 180));
		int[] dirDis = new int[2];
		dirDis[0] = ((int) Math.round(alfa * 180 / Math.PI) + orientation + 180) % 360;
		dirDis[1] = (int) Math.round(c);
		return dirDis;
	}

	private void goRoundCorners() {
		motors.forward(200);
		boolean success;
		while (true) {
			if ((irSensor.getDistance() < 10 && irSensor.getDistance() > 0)) {
				motors.stop();
				success = false;
				updateBeaconLocation(motors.drivenDistance());
				break;
			}
			if (colorSensor.getColor() == -1) {
				motors.stop();
				success = true;
				updateBeaconLocation(motors.drivenDistance());
				break;
			}
		}
		if (!success) {
			turnAndOrientation(90);
			waitForButton();
			goRoundCorners();
		}
		goalDistance = 0;
		goalDirection = orientation;
		irSensor.switchBeaconDetector(); // chvili trva prepnuti
		waitForButton();
	}

	private void waitForButton() {
		Button.waitForAnyPress();
		if (Button.ESCAPE.isDown())
			System.exit(0);
	}

}

class Motors {
	public static final double TURN_RATIO = 3.65;
	public static final double DISTANCE_RATIO = 73; // v jednotkach priblizne
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

class IRSensor extends Thread {
	private EV3IRSensor ir = new EV3IRSensor(SensorPort.S4);
	private SensorMode sp = ir.getSeekMode();
	private boolean inDistanceMode = false;
	private int value1;
	private int value2;

	public void run() {
		while (true) {
			int size = sp.sampleSize();
			float[] sample = new float[size];
			sp.fetchSample(sample, 0);
			value1 = (int) sample[0];
			if (!inDistanceMode && size > 1) {
				value2 = (int) sample[1];
			}

		}
	}

	/**
	 * hodnota ze senzoru - vzdalenost u distance /beacon modu
	 * 
	 * @return
	 */
	public int getDistance() {
		if (inDistanceMode) {
			return value1;
		} else {
			return value2;
		}
	}

	/**
	 * dalsi hodnota ze senzoru, u beaconmodu vzdalenost
	 * 
	 * @return
	 */
	public int getDirection() {
		if (!inDistanceMode) {
			return value1;
		} else {
			return 0;
		}
	}

	/**
	 * prepnuti do modu beacondetector
	 */
	public void switchBeaconDetector() {
		sp = ir.getSeekMode();
		inDistanceMode = false;
	}

	/**
	 * prepnuti do modu distancedetector
	 */
	public void switchDistanceDetector() {
		sp = ir.getDistanceMode();
		inDistanceMode = true;
	}

	public boolean isInDistanceMode() {
		return inDistanceMode;
	}

}

class ColorSensor extends Thread {
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

}
