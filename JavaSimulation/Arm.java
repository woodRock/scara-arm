package ToWebSite;


/**
 * Class represents SCARA robotic arm.
 * 
 * @Arthur Roberts
 * @0.0
 */

import ecs100.UI;
import java.awt.Color;

public class Arm
{
    
    // fixed arm parameters
    private int xMotor1;  // coordinates of the motor(measured in pixels of the picture)
    private int yMotor1;
    private int xMotor2;
    private int yMotor2;
    private double r;  // length of the upper/fore arm
    // parameters of servo motors - linear function pwm(angle)
    // each of two motors has unique function which should be measured
    // linear function cam be described by two points
    // motor 1, point1 
    private double pwm1_val_1; 
    private double theta1_val_1;
    // motor 1, point 2
    private double pwm1_val_2; 
    private double theta1_val_2;
    
    // motor 2, point 1
    private double pwm2_val_1; 
    private double theta2_val_1;
    // motor 2, point 2
    private double pwm2_val_2; 
    private double theta2_val_2;
    
    
    // current state of the arm
    private double theta1; // angle of the upper arm
    private double theta2;
    
    private double xJoint1;     // positions of the joints
    private double yJoint1;
    private double xJoint2;
    private double yJoint2;
    private double xTool;     // position of the tool
    private double yTool;
    private boolean valid_state; // is state of the arm physically possible?
    
    /**
     * Constructor for objects of class Arm
     */
    public Arm()
    {
        xMotor1 = 287; // set motor coordinates
        yMotor1 = 374;
        xMotor2 = 377;
        yMotor2 = 374;
        r = 154.0;
        theta1 = -90.0*Math.PI/180.0; // initial angles of the upper arms
        theta2 = -90.0*Math.PI/180.0;
        valid_state = false;
    }
  
    // draws arm on the canvas
    public void draw()
    {
        // draw arm
        int height = UI.getCanvasHeight();
        int width = UI.getCanvasWidth();
        // calculate joint positions
        xJoint1 = xMotor1 + r*Math.cos(theta1);
        yJoint1 = yMotor1 + r*Math.sin(theta1);
        xJoint2 = xMotor2 + r*Math.cos(theta2);
        yJoint2 = yMotor2 + r*Math.sin(theta2);
        
        //draw motors and write angles
        int mr = 20;
        UI.setLineWidth(5);
        UI.setColor(Color.BLUE);
        UI.drawOval(xMotor1-mr/2, yMotor1 -mr/2,mr,mr);
        UI.drawOval(xMotor2 -mr/2, yMotor2 -mr/2,mr,mr);
        // write parameters of first motor
        String out_str=String.format("t1=%3.1f",theta1*180/Math.PI);
        UI.drawString(out_str, xMotor1-2*mr, yMotor1 -mr/2+2*mr);
        out_str=String.format("xMotor1=%d",xMotor1);
        UI.drawString(out_str, xMotor1-2*mr, yMotor1 -mr/2+3*mr);
        out_str=String.format("yMotor1=%d", yMotor1);
        UI.drawString(out_str, xMotor1-2*mr, yMotor1 -mr/2+4*mr);
        // ditto for second motor                
        out_str = String.format("t2=%3.1f",theta2*180/Math.PI);
        UI.drawString(out_str, xMotor2 +2*mr, yMotor2 -mr/2+2*mr);
        out_str=String.format("xMotor2=%d", xMotor2);
        UI.drawString(out_str, xMotor2 +2*mr, yMotor2 -mr/2+3*mr);
        out_str=String.format("yMotor2=%d", yMotor2);
        UI.drawString(out_str, xMotor2 +2*mr, yMotor2 -mr/2+4*mr);
        // draw Field Of View
        UI.setColor(Color.GRAY);
        UI.drawRect(0,0,640,480);
         
       // it can be uncommented later when
       // kinematic equations are derived
        if ( valid_state) {
          // draw upper arms
          UI.setColor(Color.GREEN);
          UI.drawLine(xMotor1, yMotor1, xJoint1, yJoint1);
          UI.drawLine(xMotor2, yMotor2, xJoint2, yJoint2);
          //draw forearms
          UI.drawLine(xJoint1, yJoint1, xTool, yTool);
          UI.drawLine(xJoint2, yJoint2, xTool, yTool);
          // draw tool
          double rt = 20;
          UI.drawOval(xTool -rt/2, yTool -rt/2,rt,rt);
        }
        
   }
    
   // calculate tool position from motor angles 
   // updates variable in the class
   public void directKinematic(){
       
       // midpoint between joints
       double  xa = xJoint1 + 0.5 * (xJoint2 - xJoint1);
       double  ya = yJoint1 + 0.5 * (yJoint2 - yJoint1);
       // distance between joints
       double d = Math.sqrt(Math.pow((xJoint2 - xJoint1),2) + Math.pow((yJoint2 - yJoint1),2));
       if (d<2*r){
            valid_state = true;
            // half distance between tool positions
            double  h = Math.pow(r,2) + Math.sqrt(Math.pow(xa - xJoint1,2) + Math.pow(ya - yJoint1,2));
            double alpha= Math.atan((yJoint1 - yJoint2)/(xJoint2 - xJoint1));
            // tool position
            double xt = xa + h * Math.cos(Math.PI/2 - alpha);
            double yt = ya + h * Math.sin(Math.PI/2 - alpha);
            double xt2 = xa - h*Math.cos(alpha-Math.PI/2);
            double yt2 = ya - h*Math.sin(alpha-Math.PI/2);
       } else {
           valid_state = false;
        }
       
    }
    
    // motor angles from tool position
    // updates variables of the class
    public void inverseKinematic(double xt_new,double yt_new){
         
        valid_state = true;
        xTool = xt_new;
        yTool = yt_new;
        valid_state = true;
        double dx1 = xTool - xMotor1;
        double dy1 = yTool - yMotor1;
        // distance between pen and motor
        double d1 =  Math.sqrt(Math.pow((dx1),2) + Math.pow((dy1),2));
        if (d1>2*r){
            UI.println("Arm 1 - can not reach");
            valid_state = false;
            return;
        }
        double h1 = Math.sqrt(r*r - d1*d1/4);

        double alpha = Math.PI/2 - (Math.PI - Math.atan2(yTool - yMotor1, xTool - xMotor2));
        double xA = xTool + (xMotor1 - xTool)/2;
        double yA = yTool + (yMotor1 - yTool)/2;
        // elbows positions
        xJoint1 = xA - h1 * Math.sin(alpha);;
        yJoint1 = yA - h1 * Math.sin(alpha);;

        theta1 = Math.atan2(yJoint1 - yMotor1,xJoint1-xMotor1);
        if ((theta1>0)||(theta1<-Math.PI)){
            valid_state = false;
            UI.println("Angle 1 -invalid");
            return;
        }
        double dx2 = xTool - xMotor2;
        double dy2 = yTool - yMotor2;
        double d2 = Math.sqrt(Math.pow((dx2),2) + Math.pow((dy2),2));
        if (d2>2*r){
            UI.println("Arm 2 - can not reach");
            valid_state = false;
            return;
        }

        double h2 = Math.sqrt(r*r - d2*d2/4);
        // elbows positions
        xA = xTool + (xMotor2 - xTool)/2;
        yA = yTool + (yMotor2 - yTool)/2;
        xJoint2 = xA - h2 * Math.sin(alpha);
        yJoint2 = yA - h2 * Math.sin(alpha);
        // motor angles for both 1st elbow positions
        theta2 = Math.atan2(yJoint2 - yMotor2, xJoint2-xMotor1);
        if ((theta2>0)||(theta2<-Math.PI)){
            valid_state = false;
            UI.println("Angle 2 -invalid");
            return;
        }
        
        //UI.printf("xTool:%3.1f, yTool:%3.1f\n",xTool,yTool);
        //UI.printf("theta1:%3.1f, theta2:%3.1f\n",theta1*180/Math.PI,theta2*180/Math.PI);
        return;
    }
    
    // returns angle of motor 1
    public double get_theta1(){
        return theta1;
    }
    // returns angle of motor 2
    public double get_theta2(){
        return theta2;
    }
    // sets angle of the motors
    public void set_angles(double t1, double t2){
        theta1 = t1;
        theta2 = t2;
    }
    
    // returns motor control signal
    // for motor to be in position(angle) theta1
    // linear intepolation
    public int get_pwm1(){
        int pwm = 0;
        return pwm;
    }
    // ditto for motor 2
    public int get_pwm2(){
        int pwm =0;
        //pwm = (int)(pwm2_90 + (theta2 - 90)*pwm2_slope);
        return pwm;
    }
    
 }
