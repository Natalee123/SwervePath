package frc.robot.Subsystems;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkLowLevel.MotorType;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Config.Ports.pIntake;

public class SubIntake extends SubsystemBase {

  public static CANSparkMax
    Lft = new CANSparkMax( pIntake.CAN_Lft, MotorType.kBrushless ),
    Rgt = new CANSparkMax( pIntake.CAN_Rgt, MotorType.kBrushless );

  public static double
    Power = 0;

  public SubIntake() {
    Lft.restoreFactoryDefaults();
    Rgt.restoreFactoryDefaults();

    Lft.setInverted( false );
    Rgt.setInverted( false );

    Lft.setSmartCurrentLimit( 25, 10 );
    Rgt.setSmartCurrentLimit( 25, 10 );
  }

  @Override public void periodic() {
    Lft.set( Power );
    Rgt.set( Power );
  }

  public static void Display() {
    SmartDashboard.putNumber( "Intake Power", Power );
  }

// ================ COMMANDS ====================

  public static void Spit() { Power = -1.00; }
  public Command cSpit() { return this.runOnce( () -> Spit() ); }

  public static void Stop() { Power =  0.00; }
  public Command cStop() { return this.runOnce( () -> Stop() ); }

  public static void Suck() { Power =  1.00; }
  public Command cSuck() { return this.runOnce( () -> Suck() ); }

}
