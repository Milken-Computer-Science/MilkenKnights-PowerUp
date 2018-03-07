package frc.team1836.robot.auto.trajectory;

import frc.team1836.robot.Constants;
import frc.team1836.robot.util.state.TrajectoryStatus;

public class PathFollower {

    protected TrajectoryFollower lFollower;
    protected TrajectoryFollower rFollower;
    protected boolean brakeMode;

    public PathFollower(Path mPath, double distTol, double angTol, boolean brakeMode) {
        lFollower = new TrajectoryFollower(mPath.getLeftWheelTrajectory());
        lFollower.configure(Constants.DRIVE.DRIVE_FOLLOWER_P,
                Constants.DRIVE.DRIVE_FOLLOWER_A, Constants.DRIVE.DRIVE_FOLLOWER_ANG, distTol, angTol);
        rFollower = new TrajectoryFollower(mPath.getRightWheelTrajectory());
        rFollower.configure(Constants.DRIVE.DRIVE_FOLLOWER_P,
                Constants.DRIVE.DRIVE_FOLLOWER_A, -Constants.DRIVE.DRIVE_FOLLOWER_ANG, distTol, angTol);
        this.brakeMode = brakeMode;
    }


    public TrajectoryStatus getLeftVelocity(double dist, double vel, double angle) {
        return lFollower.calculate(dist, vel, angle);
    }

    public TrajectoryStatus getRightVelocity(double dist, double vel, double angle) {
        return rFollower.calculate(dist, vel, angle);
    }

    public boolean getFinished() {
        return lFollower.isFinishedTrajectory() && rFollower.isFinishedTrajectory();
    }

    public boolean getBrakeMode() {
        return brakeMode;
    }
}
