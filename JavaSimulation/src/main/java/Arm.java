import ecs100.UI;
import java.awt.*;

public class Arm {
    private int xMotor1;
    private int yMotor1;
    private int xMotor2;
    private int yMotor2;
    private double r;

    private double theta1;
    private double theta2;
    
    private double xJoint1;
    private double yJoint1;
    private double xJoint2;
    private double yJoint2;

    private double xTool;
    private double yTool;

    private boolean validArmPosition;

    public Arm() {
        this.xMotor1 = 287;
        this.yMotor1 = 374;
        this.xMotor2 = 377;
        this.yMotor2 = 374;
        this.r = 154.0;
        this.theta1 = -90.0*Math.PI/180.0;
        this.theta2 = -90.0*Math.PI/180.0;
        this.validArmPosition = false;
    }

    public void draw() {
        this.xJoint1 = this.xMotor1 + r*Math.cos(this.theta1);
        this.yJoint1 = this.yMotor1 + r*Math.sin(this.theta1);
        this.xJoint2 = this.xMotor2 + r*Math.cos(this.theta2);
        this.yJoint2 = this.yMotor2 + r*Math.sin(this.theta2);

        int mr = 20;
        UI.setLineWidth(5);
        UI.setColor(Color.BLUE);
        UI.drawOval(this.xMotor1 - mr/2, this.yMotor1 - mr/2,mr,mr);
        UI.drawOval(this.xMotor2 - mr/2, this.yMotor2 - mr/2,mr,mr);

        String outStr=String.format("t1=%3.1f",this.theta1*180/Math.PI);
        UI.drawString(outStr, this.xMotor1 - 2 * mr, this.yMotor1 - mr/2 + 2 * mr);
        outStr=String.format("xMotor1=%d", this.xMotor1);
        UI.drawString(outStr, this.xMotor1 - 2 * mr, this.yMotor1 - mr/2 + 3 * mr);
        outStr=String.format("yMotor1=%d", this.yMotor1);
        UI.drawString(outStr, this.xMotor1-2 * mr, this.yMotor1 - mr/2 + 4 *mr);

        outStr = String.format("t2=%3.1f",this.theta2*180/Math.PI);
        UI.drawString(outStr, this.xMotor2 + 2 * mr, this.yMotor2 - mr/2 + 2*mr);
        outStr=String.format("xMotor2=%d", this.xMotor2);
        UI.drawString(outStr, this.xMotor2 + 2 * mr, this.yMotor2 - mr/2 + 3 * mr);
        outStr=String.format("yMotor2=%d", this.yMotor2);
        UI.drawString(outStr, this.xMotor2 + 2 * mr, this.yMotor2 -mr/2+ 4 * mr);

        drawField();

        UI.setColor((this.validArmPosition)? Color.GREEN : Color.RED);

        drawUpperArms();
        drawForeArms();
        drawTool();
   }

   private double distance(double x1, double x2, double y1, double y2) {
        return Math.sqrt(Math.pow((x2 - x1),2) + Math.pow((y2 - y1),2));
   }

   private void drawUpperArms(){
        UI.drawLine(this.xMotor1, this.yMotor1, this.xJoint1, this.yJoint1);
        UI.drawLine(this.xMotor2, this.yMotor2, this.xJoint2, this.yJoint2);
   }

   private void drawForeArms(){
       UI.drawLine(this.xJoint1, this.yJoint1, this.xTool, this.yTool);
       UI.drawLine(this.xJoint2, this.yJoint2, this.xTool, this.yTool);
   }

   private void drawTool(){
       double rt = 20;
       UI.drawOval(this.xTool -rt/2, this.yTool -rt/2,rt,rt);
   }

   public void drawField(){
       UI.setColor(Color.GRAY);
       UI.drawRect(0,0,640,480);
   }

   public void directKinematic(){
       double xJoint1 = this.xMotor1 + r * Math.cos(this.theta1);
       double yJoint1 = this.yMotor1 + r * Math.sin(this.theta1);
       double xJoint2 = this.xMotor2 + r * Math.cos(this.theta2);
       double yJoint2 = this.yMotor2 + r * Math.sin(this.theta2);
       double  xa = xJoint1 + (xJoint2 - xJoint1) / 2;
       double  ya = yJoint1 + (yJoint2 - yJoint1) / 2;
       double d = distance(xJoint1, xJoint2, yJoint1, yJoint2);

       this.validArmPosition = isDistanceValid(d);

       if (this.validArmPosition) {
           double h = Math.sqrt(Math.pow(r,2) - Math.pow(d/2,2));
           double alpha = Math.atan((yJoint1 - yJoint2) / (xJoint2 - xJoint1));

           double xt2 = xa - h * Math.cos(Math.PI / 2 - alpha);
           double yt2 = ya - h * Math.sin(Math.PI / 2 - alpha);
           this.xTool = xt2;
           this.yTool = yt2;
       }
    }

    private boolean isDistanceValid(double d) {
        return d < 2 * r;
    }

    private boolean isAngleValid(double theta){
        return theta <= 0 || theta >= Math.PI;
    }

    private boolean isSingularity(double d1, double d2){
        return d1+d2 > 2*r;
    }

    public void inverseKinematic(double xt_new,double yt_new) throws Exception {
        this.xTool = xt_new;
        this.yTool = yt_new;
        double d1 =  distance(this.xTool - this.xMotor1,0,this.yTool - this.yMotor1,0);
        this.validArmPosition = isDistanceValid(d1);

        if (!this.validArmPosition){
            throw new Exception("Arm 1 - can not reach");
        }

        double d2 = distance(this.yTool - this.yMotor2,0,this.xTool - this.xMotor2,0);
        this.validArmPosition = isDistanceValid(d2);

        if (!this.validArmPosition){
            throw new Exception("Arm 2 - can not reach");
        }

        double h1 = distance(r,0,d1/2,0);
        double alpha = Math.PI/2 - (Math.PI - Math.atan2(this.yTool - this.yMotor1, this.xTool - this.xMotor1));
        double xA = this.xTool + (this.xMotor1 - this.xTool)/2;
        double yA = yTool + (this.yMotor1 - this.yTool)/2;
        double xJoint1 = xA + h1 * Math.cos(alpha);
        double yJoint1 = yA + h1 * Math.sin(alpha);
        double theta1 = Math.atan2(yJoint1 - this.yMotor1, xJoint1 - this.xMotor1);

        this.validArmPosition = isAngleValid(theta1);
        if (!this.validArmPosition){
            throw new Exception("Angle 1 -invalid");
        }

        double h2 = distance(r,0,d2/2,0);

        alpha = Math.PI/2 - (Math.PI - Math.atan2(this.yTool - this.yMotor2, this.xTool - this.xMotor2));
        xA = this.xTool + (this.xMotor2 - this.xTool)/2;
        yA = this.yTool + (this.yMotor2 - this.yTool)/2;

        double xJoint2 = xA - h2 * Math.cos(alpha);
        double yJoint2 = yA - h2 * Math.sin(alpha);
        double theta2 = Math.atan2(yJoint2 - this.yMotor2, xJoint2 - this.xMotor2);

        this.validArmPosition = isAngleValid(theta2);

        if (!this.validArmPosition) {
            throw new Exception("Angle 2 -invalid");
        }

        this.validArmPosition = isSingularity(d1,d2);
        if (!this.validArmPosition) {
            throw new Exception("Singularity");
        }

        this.xJoint1 = xJoint1;
        this.xJoint2 = xJoint2;
        this.yJoint1 = yJoint1;
        this.yJoint2 = yJoint2;
        this.theta1 = theta1;
        this.theta2 = theta2;
    }
    
    public double getTheta1(){
        return this.theta1;
    }

    public double getTheta2(){
        return this.theta2;
    }

    public void setAngles(double t1, double t2){
        this.theta1 = t1;
        this.theta2 = t2;
    }

    public int getPWM1(){
        return (int)((this.theta1 - 5.1686)/-0.0943);
    }

    public int getPWM2(){
        return (int) ((this.theta2 - 93.925)/-0.1041);
    }

    public double getYTool() {
        return this.yTool;
    }

    public double getXTool() {
        return this.xTool;
    }
 }
