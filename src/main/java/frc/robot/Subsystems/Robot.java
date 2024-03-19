package frc.robot.Subsystems;

import org.littletonrobotics.junction.LoggedRobot;

import com.ctre.phoenix6.StatusCode;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.Follower;
import com.ctre.phoenix6.controls.NeutralOut;
import com.ctre.phoenix6.controls.VelocityTorqueCurrentFOC;
import com.ctre.phoenix6.controls.VelocityVoltage;
import com.ctre.phoenix6.hardware.TalonFX;

import edu.wpi.first.wpilibj.XboxController;

public class Robot extends LoggedRobot {
  private static final String canBusName = "canivore";
  private final TalonFX m_fx   = new TalonFX(0, canBusName);
  private final TalonFX m_fllr = new TalonFX(1, canBusName);
  
  /* Be able to switch which control request to use based on a button press */
  /* Start at velocity 0, enable FOC, no feed forward, use slot 0 */
  private final VelocityVoltage m_voltageVelocity = new VelocityVoltage( 0, 0, true, 0, 0, false, false, false );
  /* Start at velocity 0, no feed forward, use slot 1 */
  private final VelocityTorqueCurrentFOC m_torqueVelocity = new VelocityTorqueCurrentFOC(0, 0, 0, 1, false, false, false);
  /* Keep a neutral out so we can disable the motor */

  private final NeutralOut     m_brake    = new NeutralOut();
  private final XboxController m_joystick = new XboxController(0);

  @Override public void robotInit() {
    TalonFXConfiguration configs = new TalonFXConfiguration();

    /* Voltage-based velocity requires a feed forward to account for the back-emf of the motor */
    configs.Slot0.kP = 0.11;   // An error of 1 rotation per second results in 2V output
    configs.Slot0.kI = 0.50;   // An error of 1 rotation per second increases output by 0.5V every second
    configs.Slot0.kD = 0.0001; // A change of 1 rotation per second squared results in 0.01 volts output
    configs.Slot0.kV = 0.12;   // Falcon 500 is a 500kV motor, 500rpm per V = 8.333 rps per V, 1/8.33 = 0.12 volts / Rotation per second

    // Peak output of 8 volts
    configs.Voltage.PeakForwardVoltage =  8;
    configs.Voltage.PeakReverseVoltage = -8;
    
    /* Torque-based velocity does not require a feed forward, as torque will accelerate the rotor up to the desired velocity by itself */
    configs.Slot1.kP = 5;     // An error of 1 rotation per second results in 5 amps output
    configs.Slot1.kI = 0.1;   // An error of 1 rotatio--n per second increases output by 0.1 amps every second
    configs.Slot1.kD = 0.001; // A change of 1000 rotation per second squared results in 1 amp output

    // Peak output of 40 amps
    configs.TorqueCurrent.PeakForwardTorqueCurrent =  40;
    configs.TorqueCurrent.PeakReverseTorqueCurrent = -40;

    /* Retry config apply up to 5 times, report if failure */
    StatusCode status = StatusCode.StatusCodeNotInitialized;
    for ( int i = 0; i < 5; ++i ) {
      status = m_fx.getConfigurator().apply( configs );
      if ( status.isOK() ) break;
    }
 
    if( !status.isOK() ) {
      System.out.println( "Could not apply configs, error code: " + status.toString() );
    }

    m_fllr.setControl( new Follower( m_fx.getDeviceID(), false ) );
  }

  @Override public void teleopPeriodic() {
    double joyValue = m_joystick.getLeftY();
    if ( joyValue > -0.1 && joyValue < 0.1 ) joyValue = 0; // Dead zone

    double desiredRotationsPerSecond = joyValue * 50;      // Go for plus/minus 10 rotations per second
    if ( Math.abs( desiredRotationsPerSecond ) <= 1 ) {    // Joystick deadzone
      desiredRotationsPerSecond = 0;
    }
    if ( m_joystick.getLeftBumper() ) {
      m_fx.setControl(m_voltageVelocity.withVelocity(desiredRotationsPerSecond)); // Use voltage velocity
    }
    else if ( m_joystick.getRightBumper() ) {
      double friction_torque = ( joyValue > 0 ) ? 1 : -1; // To account for friction, we add this to the arbitrary feed forward
      /* Use torque velocity */
      m_fx.setControl( m_torqueVelocity.withVelocity( desiredRotationsPerSecond ).withFeedForward( friction_torque ) );
    }
    else {
      /* Disable the motor instead */
      m_fx.setControl(m_brake);
    }
  }

}