package frc.team1836.robot.subsystems;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.team1836.robot.Constants;
import frc.team1836.robot.Constants.ARM;
import frc.team1836.robot.RobotState;
import frc.team1836.robot.RobotState.ArmControlState;
import frc.team1836.robot.RobotState.ArmState;
import frc.team1836.robot.util.drivers.MkTalon;
import frc.team1836.robot.util.drivers.MkTalon.TalonPosition;
import frc.team1836.robot.util.logging.ReflectingCSVWriter;
import frc.team1836.robot.util.loops.Loop;
import frc.team1836.robot.util.loops.Looper;
import frc.team1836.robot.util.math.MkMath;
import frc.team1836.robot.util.other.Subsystem;

public class Arm extends Subsystem {

    private final ReflectingCSVWriter<ArmDebugOutput> mCSVWriter;
    private final MkTalon armTalon;
    private final VictorSPX leftIntakeRollerTalon;
    private final VictorSPX rightIntakeRollerTalon;
    private ArmDebugOutput mDebug = new ArmDebugOutput();
    private boolean armSafety = true;
    private double armPosEnable = 0;

    private Arm() {
        mCSVWriter = new ReflectingCSVWriter<>(Constants.LOGGING.ARM_LOG_PATH,
                ArmDebugOutput.class);
        armTalon = new MkTalon(ARM.ARM_MASTER_TALON_ID, ARM.ARM_SLAVE_TALON_ID, TalonPosition.Arm);
        armTalon.setSensorPhase(ARM.ARM_SENSOR_PHASE);
        armTalon.configMotionMagic();
        armTalon.setSoftLimit(ARM.ARM_FORWARD_LIMIT, ARM.ARM_REVERSE_LIMIT);
        armTalon.setLimitEnabled(true);
        leftIntakeRollerTalon = new VictorSPX(Constants.ARM.LEFT_INTAKE_ROLLER_ID);
        rightIntakeRollerTalon = new VictorSPX(Constants.ARM.RIGHT_INTAKE_ROLLER_ID);
        rightIntakeRollerTalon.set(ControlMode.Follower, Constants.ARM.LEFT_INTAKE_ROLLER_ID);
        leftIntakeRollerTalon.setNeutralMode(NeutralMode.Brake);
        rightIntakeRollerTalon.setNeutralMode(NeutralMode.Brake);
        armTalon.invertMaster(ARM.ARM_MASTER_DIRECTION);
        armTalon.invertSlave(ARM.ARM_SLAVE_DIRECTION);
        leftIntakeRollerTalon.setInverted(ARM.LEFT_INTAKE_DIRECTION);
        rightIntakeRollerTalon.setInverted(ARM.RIGHT_INTAKE_DIRECTION);
    }

    public static Arm getInstance() {
        return InstanceHolder.mInstance;
    }

    @Override
    public void writeToLog() {
        mCSVWriter.write();
    }

    @Override
    public void outputToSmartDashboard() {
        armTalon.updateSmartDash();
        SmartDashboard.putNumber("Arm Current", armTalon.getCurrentOutput());
        SmartDashboard.putString("Arm Desired Position", RobotState.mArmState.toString());
        SmartDashboard.putString("Arm Control Mode", RobotState.mArmControlState.toString());
        SmartDashboard.putBoolean("Arm Status", armTalon.isEncoderConnected());
        SmartDashboard.putNumber("Roller Output", leftIntakeRollerTalon.getMotorOutputPercent());
    }

    @Override
    public void stop() {
        mCSVWriter.flush();
    }

    @Override
    public void zeroSensors() {
        armTalon.resetEncoder();
    }

    @Override
    public void checkSystem() {
        RobotState.mArmControlState = ArmControlState.ZEROING;
        Timer.delay(4.0);
        for (ArmState state : ArmState.values()) {
            RobotState.mArmState = state;
            setIntakeRollers(0.5);
            Timer.delay(2);
        }
    }

    @Override
    public void registerEnabledLoops(Looper enabledLooper) {
        Loop mLoop = new Loop() {

            @Override
            public void onStart(double timestamp) {
                synchronized (Arm.this) {
                    armPosEnable = armTalon.getPosition();
                }
            }

            /**
             * Updated from mEnabledLoop in Robot.java
             * @param timestamp Time in seconds since code start
             */
            @Override
            public void onLoop(double timestamp) {
                synchronized (Arm.this) {
                    armSafetyCheck();
                    updateFeedForward();
                    updateDebugOutput(timestamp);
                    mCSVWriter.add(mDebug);
                    switch (RobotState.mArmControlState) {
                        case MOTION_MAGIC:
                            updateArmSetpoint();
                            return;
                        case ZEROING:
                            zeroArm();
                            return;
                        case OPEN_LOOP:
                            return;
                        default:
                            System.out
                                    .println("Unexpected arm control state: " + RobotState.mArmControlState);
                            break;
                    }
                }
            }

            @Override
            public void onStop(double timestamp) {
                stop();
            }
        };
        enabledLooper.register(mLoop);
    }

    public void changeSafety() {
        armSafety = !armSafety;
        armTalon.setLimitEnabled(armSafety);
    }

    private void updateFeedForward() {
        double realArmPos = armTalon.getPosition() + ARM.ANGLE_OFFSET;
        armTalon.setF(1023.0 / (ARM.MAX_REG + (Math.sin(Math.toRadians(realArmPos))) * ARM.STEADY_PERCENT_V_BUS));
    }

    private void updateDebugOutput(double timestamp) {
        mDebug.controlMode = RobotState.mArmControlState.toString();
        mDebug.output = armTalon.getPercentOutput();
        mDebug.position = armTalon.getPosition();
        mDebug.velocity = armTalon.getSpeed();
        mDebug.setpoint = RobotState.mArmState.state;
        mDebug.timestamp = timestamp;
    }

    private void updateArmSetpoint() {
        if (RobotState.mArmState.equals(ArmState.ENABLE)) {
            armTalon.set(ControlMode.MotionMagic, MkMath.angleToNativeUnits(armPosEnable));
        } else {
            armTalon.set(ControlMode.MotionMagic, MkMath.angleToNativeUnits(RobotState.mArmState.state));
        }
    }

    private void zeroArm() {
        if (armTalon.getCurrentOutput() > ARM.CURRENT_HARDSTOP_LIMIT) {
            RobotState.mArmControlState = ArmControlState.OPEN_LOOP;
            setOpenLoop(0);
            edu.wpi.first.wpilibj.Timer.delay(0.25);
            armTalon.resetEncoder();
            armTalon.setLimitEnabled(true);
            armSafety = true;
            System.out.println("Zeroed");
            RobotState.mArmState = ArmState.ZEROED;
            RobotState.mArmControlState = ArmControlState.MOTION_MAGIC;
        } else {
            armTalon.setLimitEnabled(false);
            setOpenLoop(ARM.ZEROING_POWER);
        }
    }

    private void armSafetyCheck() {
        if (armTalon.getCurrentOutput() > ARM.SAFE_CURRENT_OUTPUT && armSafety) {
            System.out.println("Unsafe Output");
            RobotState.mArmControlState = ArmControlState.OPEN_LOOP;
            setOpenLoop(0);
        }
        if (!armTalon.isEncoderConnected()) {
            System.out.println("Encoder Not Connected");
            RobotState.mArmControlState = ArmControlState.OPEN_LOOP;
        }
    }

    public void setOpenLoop(double output) {
        armTalon.set(ControlMode.PercentOutput, output);
    }

    public void setIntakeRollers(double output) {
        leftIntakeRollerTalon.set(ControlMode.PercentOutput, output);
    }

    public void invertRightRoller(boolean dir) {
        rightIntakeRollerTalon
                .setInverted(dir ? !ARM.RIGHT_INTAKE_DIRECTION : ARM.RIGHT_INTAKE_DIRECTION);
    }

    public static class ArmDebugOutput {

        double timestamp;
        String controlMode;
        double output;
        double position;
        double velocity;
        double setpoint;
    }

    private static class InstanceHolder {

        private static final Arm mInstance = new Arm();
    }
}
