package frc.team1836.robot.auto.modes;

import edu.wpi.first.wpilibj.DriverStation.Alliance;
import frc.team1836.robot.AutoChooser;
import frc.team1836.robot.AutoChooser.GameObjectPosition;
import frc.team1836.robot.Constants;
import frc.team1836.robot.Constants.ELEVATOR;
import frc.team1836.robot.RobotState;
import frc.team1836.robot.RobotState.ElevatorState;
import frc.team1836.robot.auto.actions.DelayAction;
import frc.team1836.robot.auto.actions.DrivePathAction;
import frc.team1836.robot.auto.actions.MoveArmAction;
import frc.team1836.robot.auto.actions.RollerAction;
import frc.team1836.robot.util.auto.AutoModeBase;
import frc.team1836.robot.util.auto.AutoModeEndedException;
import frc.team1836.robot.util.auto.ParallelAction;
import frc.team1836.robot.util.logging.Log;

import java.util.Arrays;

public class CenterSwitchMode extends AutoModeBase {

    @Override
    protected void routine() throws AutoModeEndedException {
        Log.verbose("Starting Center Switch Mode");
        runAction(new ParallelAction(Arrays.asList(
                new DrivePathAction(1, false, false),
                new MoveArmAction(ElevatorState.SWITCH_PLACE),
                new DelayAction(getTime(1) - 0.5, new RollerAction(0.45, Constants.ELEVATOR.INTAKE_OUT_ROLLER_SPEED)))));

        runAction(new ParallelAction(Arrays.asList(
                new DrivePathAction(2, true, true),
                new DelayAction(0.3, new MoveArmAction(ElevatorState.INTAKE))
        )));

        runAction(new ParallelAction(Arrays.asList(
                new DrivePathAction(3, false, true),
                new RollerAction(getTime(3), Constants.ELEVATOR.INTAKE_IN_ROLLER_SPEED, true)
        )));

        runAction(new DrivePathAction(4, true, false));

        runAction(new ParallelAction(Arrays.asList(
                new DrivePathAction(5, false, false),
                new DelayAction(0.5, new MoveArmAction(ElevatorState.SWITCH_PLACE)),
                new DelayAction(getTime(5) - 0.475, new RollerAction(0.5, ELEVATOR.INTAKE_OUT_ROLLER_SPEED / 1.5))
        )));

        runAction(new ParallelAction(Arrays.asList(
                new DrivePathAction(6, true, true),
                new DelayAction(0.5, new MoveArmAction(ElevatorState.SECOND_SWITCH_PLACE))
        )));

        runAction(new ParallelAction(Arrays.asList(
                new DrivePathAction(7, false, true),
                new RollerAction(getTime(7), Constants.ELEVATOR.INTAKE_IN_ROLLER_SPEED, true)
        )));

        runAction(new DrivePathAction(8, true, false));

        runAction(new ParallelAction(Arrays.asList(
                new DelayAction(0.5, new MoveArmAction(ElevatorState.SWITCH_PLACE)),
                new DrivePathAction(9, false, false),
                new DelayAction(
                        getTime(9) - 0.65, new RollerAction(0.75, Constants.ELEVATOR.INTAKE_OUT_ROLLER_SPEED / 1.5)))));
    }

    private double getTime(int pathNum) {
        return AutoChooser.autoPaths.get(
                "CS-" + Integer.toString(pathNum) + ((RobotState.matchData.switchPosition
                        == GameObjectPosition.LEFT) ? "L" : "R") + ((RobotState.matchData.alliance
                        == Alliance.Blue) ? "B" : "R")).getTime();
    }

}
