package frc.team1836.robot.auto.modes;

import frc.team1836.robot.AutoChooser;
import frc.team1836.robot.Constants;
import frc.team1836.robot.auto.actions.DelayAction;
import frc.team1836.robot.auto.actions.DrivePathAction;
import frc.team1836.robot.auto.actions.RollerAction;
import frc.team1836.robot.util.auto.AutoModeBase;
import frc.team1836.robot.util.auto.AutoModeEndedException;
import frc.team1836.robot.util.auto.ParallelAction;

import java.util.Arrays;

public class SingleSideSwitchMode extends AutoModeBase {
    private AutoChooser.GameObjectPosition position;

    public SingleSideSwitchMode(AutoChooser.GameObjectPosition position) {
        this.position = position;
    }

    @Override
    protected void routine() throws AutoModeEndedException {
        switch (position) {
            case LEFT:
                leftRoutine();
                break;
            case RIGHT:
                rightRoutine();
                break;
        }

    }

    private void leftRoutine() throws AutoModeEndedException {
        runAction(new ParallelAction(Arrays
                .asList(
                        new DrivePathAction(AutoChooser.autoPaths.get("CBS-1L"), false, false, false),
                        new DelayAction(AutoChooser.autoPaths.get("CBS-1L").getTime() - 0.25, new RollerAction(0.35, Constants.ARM.INTAKE_OUT_ROLLER_SPEED))
                )));
    }

    private void rightRoutine() throws AutoModeEndedException {
        runAction(new ParallelAction(Arrays
                .asList(
                        new DrivePathAction(AutoChooser.autoPaths.get("CBS-1R"), false, false, false),
                        new DelayAction(AutoChooser.autoPaths.get("CBS-1R").getTime() - 0.25, new RollerAction(0.35, Constants.ARM.INTAKE_OUT_ROLLER_SPEED))
                )));
    }
}