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

public class RobotClasses {

	public static void main(String[] args) {
		Robot robot = new Robot();
		robot.introMessage();
		robot.startSensors();
		robot.startTimer();
		robot.findBeacon();
		robot.measureDistance();
		robot.findGoal();
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

	/**
	 * otoci se a zjisti pozici a vzdalenost k beaconu
	 */
	public void findBeacon() {
		irSensor.switchBeaconDetector();
		motors.turn();

		while (true) {
			if (irSensor.getValue1() > -5 && irSensor.getValue1() < 5 && irSensor.getValue1() != 0) {
				motors.stop();
				orientation = (orientation + motors.turnedDegree()) % 360;
				// TO DO vlozeni smeru beaconu (priblizne orientation) a
				// vzdalenosti (aktualni irSensor.getValue2()) do nejake mapy
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
		while (true)
		{
			if (motors.isFinished()) {
				break;
			}
		}
		// TO DO vlozeni hodnot do nejake mapy prostredi
    // asi zjistovat prubezne orientaci a ukladat vzdalenosti ke kraji (irSensor.getValue1())
	}

	public void findGoal() {
		// TO DO
		// bude nejakym zpusobem jezdit a hledat branku + aktualizovat mapu
		// prostredi, pokud mozno nenarazi do beaconu nebo do zdi
		// az najde branku dojede k beaconu a zkusi ho tam dotlacit

		// priklad jen jede rovne dokud se nepriblizi ke zdi nebo nenajde branku (nema pod sebou bilou barvu]
		irSensor.switchDistanceDetector();
		motors.forward(200);
		while (true) {
			if ((irSensor.getValue1() < 10 && irSensor.getValue1() > 0) || colorSensor.getColor() == -1) {
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

	private void sensorsMessageBeacon() {
		lcd.clear();
		lcd.drawString("IR distance: " + irSensor.getValue2(), 5, 0, 0);
		lcd.drawString("Direction: " + irSensor.getValue1(), 5, 20, 0);
		lcd.drawString("Color sensor: " + colorSensor.getColor(), 5, 40, 0);
		lcd.drawString("Orientation: " + orientation, 5, 80, 0);
	}

	private void sensorsMessageDistance() {
		lcd.clear();
		lcd.drawString("IR distance: " + irSensor.getValue1(), 5, 0, 0);
		lcd.drawString("Color sensor: " + colorSensor.getColor(), 5, 40, 0);
		lcd.drawString("Orientation: " + orientation, 5, 80, 0);
	}

}

class Motors {
	public static final double TURN_RATIO = 3.65;
	private EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.C);
	private EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.B);

	/**
	 * jede dopredu dokud nezavolano stop()
	 * 
	 * @param speed
	 *            rychlost
	 */
	public void forward(int speed) {
		rightMotor.setSpeed(speed);
		leftMotor.setSpeed(speed);
		rightMotor.forward();
		leftMotor.forward();
	}

	/**
	 * zastavi
	 */
	public void stop() {
		rightMotor.stop(true);
		leftMotor.stop(true);
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
			float[] sample = new float[sp.sampleSize()];
			sp.fetchSample(sample, 0);
			value1 = (int) sample[0];
			if (!inDistanceMode) {
				value2 = (int) sample[1];
			}

		}
	}

	/**
	 * hodnota ze senzoru - u distacemodu vzdalenost, u beaconmodu smer
	 * 
	 * @return
	 */
	public int getValue1() {
		return value1;
	}

	/**
	 * dalsi hodnota ze senzoru, u beaconmodu vzdalenost
	 * 
	 * @return
	 */
	public int getValue2() {
		return value2;
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
