package nz.ac.engr110.scara;

import ecs100.UI;
import java.awt.Color;

public class Arm {
    private static final Color MOTOR_COLOR = Color.BLUE;
    private static final Color VALID_COLOR = Color.GREEN;
    private static final Color INVALID_COLOR = Color.RED;
    private static final Color TOOL_COLOR = Color.BLUE;
    private static final Color FIELD_COLOR = Color.GRAY;

    private final Point<Double> motor1;
    private final Point<Double> motor2;
    private static final double R = 154.0;

    private Point<Double> joint1;
    private Point<Double> joint2;
    private Point<Double> tool;

    private double theta1;
    private double theta2;

    private boolean validArmPosition = false;

    public Arm() {
        this.motor1 = new Point<>(287.0, 374.0);
        this.motor2 = new Point<>(377.0,374.0);
        this.theta1 = -90.0*Math.PI/180.0;
        this.theta2 = -90.0*Math.PI/180.0;
        this.tool = new Point<>(330.0,150.0);
        final double x1 = this.motor1.getX() + Arm.R * Math.cos(this.theta1);
        final double y1 = this.motor1.getY() + Arm.R * Math.sin(this.theta1);
        final double x2 = this.motor2.getX() + Arm.R * Math.cos(this.theta2);
        final double y2 = this.motor2.getY() + Arm.R * Math.sin(this.theta2);
        Point<Double> j1 = new Point<>(x1,y1);
        Point<Double> j2 = new Point<>(x2,y2);
        this.joint1 = j1;
        this.joint2 = j2;
    }

    public void draw() {
        Point<Double> p1 = new Point<>(this.motor1.getX() + R *Math.cos(this.theta1), this.motor1.getY() + R *Math.sin(this.theta1));
        Point<Double> p2 = new Point<>(this.motor2.getX() + R *Math.cos(this.theta2),this.motor2.getY() + R *Math.sin(this.theta2));
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
        UI.setColor(Arm.MOTOR_COLOR);
        drawMotorStatistics("₁", this.motor1, theta1);
        drawMotorStatistics("₂", this.motor2, theta2);
    }

    private void drawMotorStatistics(String motor, Point<Double> point, double theta) {
        final int MOTOR_RADIUS = 20;
        UI.drawOval(point.getX() - MOTOR_RADIUS/2.0, point.getY() - MOTOR_RADIUS/2.0, MOTOR_RADIUS, MOTOR_RADIUS);
        UI.drawString(String.format("Θ%s=%3.1f",motor, theta*180/Math.PI), point.getX() - 2 * MOTOR_RADIUS, point.getY() - MOTOR_RADIUS/2.0 + 2 * MOTOR_RADIUS);
        UI.drawString(String.format("Motor%s", motor), point.getX() - 2 * MOTOR_RADIUS, point.getY() - MOTOR_RADIUS/2.0 + 3 * MOTOR_RADIUS);
        UI.drawString(String.format("(%.0f,%.0f)", point.getX(), point.getY()), point.getX()-2 * MOTOR_RADIUS, point.getY() - MOTOR_RADIUS/2.0 + 4 * MOTOR_RADIUS);
    }

    private void drawUpperArms() {
        UI.setColor((this.validArmPosition)? Arm.VALID_COLOR : Arm.INVALID_COLOR);
        UI.drawLine(this.motor1.getX(), this.motor1.getY(), this.joint1.getX(), this.joint1.getY());
        UI.drawLine(this.motor2.getX(), this.motor2.getY(), this.joint2.getX(), this.joint2.getY());
    }

    private void drawForeArms() {
        UI.setColor((this.validArmPosition)? Arm.VALID_COLOR : Arm.INVALID_COLOR);
        UI.drawLine(this.joint1.getX(), this.joint1.getY(), this.tool.getX(), this.tool.getY());
        UI.drawLine(this.joint2.getX(), this.joint2.getY(), this.tool.getX(), this.tool.getY());
    }

    private void drawTool() {
        final int TOOL_RADIUS = 20;
        UI.setColor((this.validArmPosition)? Arm.TOOL_COLOR : Arm.INVALID_COLOR);
        UI.drawOval(this.tool.getX() - TOOL_RADIUS/2.0, this.tool.getY() - TOOL_RADIUS/2.0, TOOL_RADIUS, TOOL_RADIUS);
    }

    public void drawField() {
        UI.setColor(Arm.FIELD_COLOR);
        UI.drawRect(0,0,640,480);
    }

    public void directKinematic() {
        Point<Double> j1 = new Point<>(this.motor1.getX() + Arm.R * Math.cos(this.theta1), this.motor1.getY() + Arm.R * Math.sin(this.theta1));
        Point<Double> j2 = new Point<>(this.motor2.getX() + Arm.R * Math.cos(this.theta2), this.motor2.getY() + Arm.R * Math.sin(this.theta2));
        double d = distance(j1, j2);
        if (!isDistanceValid(d))
            return;
        Point<Double> midpoint = midpoint(j1, j2);
        double h = Math.sqrt(Math.abs(Math.pow(R, 2) - Math.pow(d / 2, 2)));
        double alpha = Math.atan((j1.getY() - j2.getY()) / (j2.getX() - j1.getX()));
        this.tool = new Point<>(midpoint.getX() - h * Math.cos(Math.PI / 2 - alpha), midpoint.getY() - h * Math.sin(Math.PI / 2 - alpha));
    }

    public void inverseKinematic(double xTool,double yTool) {
        this.validArmPosition = true;
        this.tool = new Point<>(xTool, yTool);
        double d1 = distance(this.tool, this.motor1);
        double d2 = distance(this.tool, this.motor2);
        if (!isDistanceValid(d1))
            return;
        if (!isDistanceValid(d2))
            return;
        if(!isSingularity(d1,d2))
            return;
        double h1 = Math.sqrt(R * R - d1*d1/4);
        double alpha = Math.PI/2 - (Math.PI - Math.atan2(yTool - this.motor1.getY(), xTool - this.motor1.getX()));
        Point<Double> midpoint = midpoint(this.tool, this.motor1);
        Point<Double> j1 = new Point<>(midpoint.getX() + h1 * Math.cos(alpha), midpoint.getY() + h1 * Math.sin(alpha));
        double t1 = Math.atan2(j1.getY() - this.motor1.getY(), j1.getX() - this.motor1.getX());
        if (!isAngleValid(t1))
            return;
        double h2 = Math.sqrt(R * R - d2*d2/4);
        alpha = Math.PI/2 - (Math.PI - Math.atan2(yTool - this.motor2.getY(), xTool - this.motor2.getX()));
        midpoint = midpoint(this.tool, this.motor2);
        Point<Double> j2 = new Point<>(midpoint.getX() - h2 * Math.cos(alpha), midpoint.getY() - h2 * Math.sin(alpha));
        double t2 = Math.atan2(j2.getY() - this.motor2.getY(), j2.getX() - this.motor2.getX());
        if (!isAngleValid(t2))
            return;
        setJointXY(j1, j2);
        setAngles(t1, t2);
    }

    private Point<Double> midpoint(Point<Double> p1, Point<Double> p2){
        double x = p1.getX() + (p2.getX() - p1.getX())/2;
        double y = p1.getY() + (p2.getY() - p1.getY())/2;
        return new Point<>(x,y);
    }

    private double distance(Point<Double> p1, Point<Double> p2) {
        return distance(p1.getX(),p2.getX(),p1.getY(),p2.getY());
    }

    private double distance(double x1, double x2, double y1, double y2) {
        return Math.sqrt(Math.pow((x2 - x1),2) + Math.pow((y2 - y1),2));
    }

    private boolean isDistanceValid(double d) {
        this.validArmPosition = d < 2 * Arm.R;
        return this.validArmPosition;
    }

    private boolean isAngleValid(double theta){
        this.validArmPosition =  theta <= 0 || theta >= Math.PI;
        return this.validArmPosition;
    }

    private boolean isSingularity(double d1, double d2){
        this.validArmPosition = d1+d2 > 2* R;
        return this.validArmPosition;
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

    public void setAngles(double t1, double t2){
        this.theta1 = t1;
        this.theta2 = t2;
    }
    private void setJointXY(Point<Double> p1, Point<Double> p2){
        this.joint1 = p1;
        this.joint2 = p2;
    }
}
