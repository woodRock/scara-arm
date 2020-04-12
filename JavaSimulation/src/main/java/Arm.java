import ecs100.UI;

import java.awt.*;

public class Arm {
    private final Color MOTOR_COLOR = Color.BLUE;
    private final Color VALID_COLOR = Color.GREEN;
    private final Color INVALID_COLOR = Color.RED;
    private final Color TOOL_COLOR = Color.BLUE;
    private final Color FIELD_COLOR = Color.GRAY;

    private Point<Double> motor1;
    private Point<Double> motor2;
    private Point<Double> joint1;
    private Point<Double> joint2;
    private Point<Double> tool;

    private double r = 154.0;
    private double theta1;
    private double theta2;

    private boolean validArmPosition = false;

    public Arm() {
        this.motor1 = new Point(287.0, 374.0);
        this.motor2 = new Point(377.0,374.0);
        this.theta1 = -90.0*Math.PI/180.0;
        this.theta2 = -90.0*Math.PI/180.0;
        this.tool = new Point(330.0,150.0);
        final double x1 = this.motor1.getX() + this.r * Math.cos(this.theta1);
        final double y1 = this.motor1.getY() + this.r * Math.sin(this.theta1);
        final double x2 = this.motor2.getX() + this.r * Math.cos(this.theta2);
        final double y2 = this.motor2.getY() + this.r * Math.sin(this.theta2);
        Point<Double> j1 = new Point<>(x1,y1);
        Point<Double> j2 = new Point<>(x2,y2);
        this.joint1 = j1;
        this.joint2 = j2;
    }

    public void draw() {
        Point<Double> p1 = new Point(this.motor1.getX() + r*Math.cos(this.theta1), this.motor1.getY() + r*Math.sin(this.theta1));
        Point<Double> p2 = new Point(this.motor2.getX() + r*Math.cos(this.theta2),this.motor2.getY() + r*Math.sin(this.theta2));
        setJointXY(p1,p2);
        drawMotors();
        drawField();
        drawUpperArms();
        drawForeArms();
        drawTool();
    }

    private void drawMotors(){
        final int LINE_WIDTH = 5;
        UI.setLineWidth(LINE_WIDTH);
        UI.setColor(this.MOTOR_COLOR);
        drawMotorStatistics("₁", this.motor1, theta1);
        drawMotorStatistics("₂", this.motor2, theta2);
    }

    private void drawMotorStatistics(String motor, Point<Double> point, double theta) {
        final int MOTOR_RADIUS = 20;
        UI.drawOval(point.getX() - MOTOR_RADIUS /2, point.getY() - MOTOR_RADIUS /2, MOTOR_RADIUS, MOTOR_RADIUS);
        UI.drawString(String.format("Θ%s=%3.1f",motor, theta*180/Math.PI), point.getX() - 2 * MOTOR_RADIUS, point.getY() - MOTOR_RADIUS /2 + 2 * MOTOR_RADIUS);
        UI.drawString(String.format("Motor%s", motor), point.getX() - 2 * MOTOR_RADIUS, point.getY() - MOTOR_RADIUS /2 + 3 * MOTOR_RADIUS);
        UI.drawString(String.format("(%.0f,%.0f)", point.getX(), point.getY()), point.getX()-2 * MOTOR_RADIUS, point.getY() - MOTOR_RADIUS /2 + 4 * MOTOR_RADIUS);
    }

    private double distance(double x1, double x2, double y1, double y2) {
        return Math.sqrt(Math.pow((x2 - x1),2) + Math.pow((y2 - y1),2));
    }

    private void drawUpperArms() {
        UI.setColor((this.validArmPosition)? this.VALID_COLOR : this.INVALID_COLOR);
        UI.drawLine(this.motor1.getX(), this.motor1.getY(), this.joint1.getX(), this.joint1.getY());
        UI.drawLine(this.motor2.getX(), this.motor2.getY(), this.joint2.getX(), this.joint2.getY());
    }

    private void drawForeArms() {
        UI.setColor((this.validArmPosition)? this.VALID_COLOR : this.INVALID_COLOR);
        UI.drawLine(this.joint1.getX(), this.joint1.getY(), this.tool.getX(), this.tool.getY());
        UI.drawLine(this.joint2.getX(), this.joint2.getY(), this.tool.getX(), this.tool.getY());
    }

    private void drawTool() {
        final int TOOL_RADIUS = 20;
        UI.setColor((this.validArmPosition)? this.TOOL_COLOR : this.INVALID_COLOR);
        UI.drawOval(this.tool.getX() - TOOL_RADIUS /2, this.tool.getY() - TOOL_RADIUS /2, TOOL_RADIUS, TOOL_RADIUS);
    }

    public void drawField() {
        UI.setColor(this.FIELD_COLOR);
        UI.drawRect(0,0,640,480);
    }

    public void directKinematic() {
        double xJoint1 = this.motor1.getX() + this.r * Math.cos(this.theta1);
        double yJoint1 = this.motor1.getY() + this.r * Math.sin(this.theta1);

        double xJoint2 = this.motor2.getX() + this.r * Math.cos(this.theta2);
        double yJoint2 = this.motor2.getY() + this.r * Math.sin(this.theta2);

        double xa = xJoint1 + (xJoint2 - xJoint1) / 2;
        double ya = yJoint1 + (yJoint2 - yJoint1) / 2;

        double d = distance(xJoint1, xJoint2, yJoint1, yJoint2);
        if (d < 2 * this.r ) {
            double h = Math.sqrt(Math.abs(Math.pow(r, 2) - Math.pow(d / 2, 2)));
            double alpha = Math.atan((yJoint1 - yJoint2) / (xJoint2 - xJoint1));

            double xt2 = xa - h * Math.cos(Math.PI / 2 - alpha);
            double yt2 = ya - h * Math.sin(Math.PI / 2 - alpha);
            setToolXY(xt2, yt2);
        } else {
            UI.println("Invalid");
        }
    }

    public void inverseKinematic(double xTool,double yTool) {
        validArmPosition = true;

        //the position you want the tool to move to
        setToolXY(xTool, yTool);

        //distance from tool to motor one in both vectors
        double dx1 = xTool - motor1.getX();
        double dy1 = yTool - motor1.getY();
        // distance between tool and motor
        double d1 =  Math.sqrt(Math.pow((dx1),2) + Math.pow((dy1),2));

        //Check distance is not greater than length of arm 1
        if (d1>2*r){
            validArmPosition = false;
            return;
        }
        //distance from tool to motor two in both vectors
        double dx2 = xTool - motor2.getX();
        double dy2 = yTool - motor2.getY();
        // distance between tool and motor
        double d2 = Math.sqrt(Math.pow((dx2),2) + Math.pow((dy2),2));
        //Check distance is not greater than length of arm 2
        if (d2>2*r){
            validArmPosition = false;
            return;
        }
        //Distance from joint to path from motor to tool
        double h1 = Math.sqrt(r*r - d1*d1/4);

        double alpha = Math.PI/2 - (Math.PI - Math.atan2(yTool - motor1.getY(), xTool - motor1.getX()));

        double xA = xTool + (motor1.getX() - xTool)/2;
        double yA = yTool + (motor1.getY() - yTool)/2;
        //Joint positions for joint 1
        double xJoint1 = xA + h1 * Math.cos(alpha);
        double yJoint1 = yA + h1 * Math.sin(alpha);

        double theta1 = Math.atan2(yJoint1 - motor1.getY(),xJoint1-motor1.getX());
        if ((theta1>0)||(theta1<-Math.PI)){
            validArmPosition = false;
            return;
        }

        //Distance from joint to path from motor to tool
        double h2 = Math.sqrt(r*r - d2*d2/4);

        alpha = Math.PI/2 - (Math.PI - Math.atan2(yTool - motor2.getY(), xTool - motor2.getX()));

        xA = xTool + (motor2.getX() - xTool)/2;
        yA = yTool + (motor2.getY() - yTool)/2;

        //Joint positions for joint 1
        double xJoint2 = xA - h2 * Math.cos(alpha);
        double yJoint2 = yA - h2 * Math.sin(alpha);

        // motor angles for both 1st elbow positions
        double theta2 = Math.atan2(yJoint2 - motor2.getY(), xJoint2-motor2.getX());
        if ((theta2>0)||(theta2<-Math.PI)){
            validArmPosition = false;
            return;
        }

        if(d1+d2 <= 2*r){
            validArmPosition = false;
            return;
        }
        //Assign calculated values to be the new positions of actual parts
        setJointXY(new Point<>(xJoint1,yJoint1), new Point<>(xJoint2, yJoint2));
        setAngles(theta1, theta2);
    }

    private boolean isValidArmPosition(double d1, double d2, double theta1, double theta2){
        this.validArmPosition = isDistanceValid(d1) && isDistanceValid(d2) && isAngleValid(theta1) && isAngleValid(theta2) && isSingularity(d1,d2);
        return this.validArmPosition;
    }

    private boolean isDistanceValid(double d) {
        return d < 2 * this.r;
    }

    private boolean isAngleValid(double theta){
        return theta <= 0 || theta >= Math.PI;
    }

    private boolean isSingularity(double d1, double d2){
        return d1+d2 > 2*r;
    }

    public double getTheta1(){
        return this.theta1;
    }

    public double getTheta2(){
        return this.theta2;
    }

    public int getPWM1(){
        return (int)((this.theta1 - 5.1686)/-0.0943);
    }

    public int getPWM2(){
        return (int) ((this.theta2 - 93.925)/-0.1041);
    }

    public double getYTool() {
        return this.tool.getY();
    }

    public double getXTool() {
        return this.tool.getX();
    }

    private void setToolXY(double x, double y) {
        this.tool = new Point(x,y);
    }

    public void setAngles(double t1, double t2){
        this.theta1 = t1;
        this.theta2 = t2;
    }
    private void setJointXY(Point<Double> p1, Point<Double> p2){
        this.joint1 = p1;
        this.joint2 = p2;
    }
}
