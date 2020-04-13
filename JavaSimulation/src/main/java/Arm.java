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
        Point<Double> joint1 = new Point<>(this.motor1.getX() + this.r * Math.cos(this.theta1), this.motor1.getY() + this.r * Math.sin(this.theta1));
        Point<Double> joint2 = new Point<>(this.motor2.getX() + this.r * Math.cos(this.theta2), this.motor2.getY() + this.r * Math.sin(this.theta2));
        double d = distance(joint1, joint2);
        if (!isDistanceValid(d))
            return;
        Point<Double> midpoint = midpoint(joint1, joint2);
        double h = Math.sqrt(Math.abs(Math.pow(r, 2) - Math.pow(d / 2, 2)));
        double alpha = Math.atan((joint1.getY() - joint2.getY()) / (joint2.getX() - joint1.getX()));
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
        double h1 = Math.sqrt(r*r - d1*d1/4);
        double alpha = Math.PI/2 - (Math.PI - Math.atan2(yTool - this.motor1.getY(), xTool - this.motor1.getX()));
        Point<Double> midpoint = midpoint(this.tool, this.motor1);
        Point<Double> joint1 = new Point<>(midpoint.getX() + h1 * Math.cos(alpha), midpoint.getY() + h1 * Math.sin(alpha));
        double theta1 = Math.atan2(joint1.getY() - this.motor1.getY(), joint1.getX() - this.motor1.getX());
        if (!isAngleValid(theta1))
            return;
        double h2 = Math.sqrt(r*r - d2*d2/4);
        alpha = Math.PI/2 - (Math.PI - Math.atan2(yTool - this.motor2.getY(), xTool - this.motor2.getX()));
        midpoint = midpoint(this.tool, this.motor2);
        Point<Double> joint2 = new Point<>(midpoint.getX() - h2 * Math.cos(alpha), midpoint.getY() - h2 * Math.sin(alpha));
        double theta2 = Math.atan2(joint2.getY() - this.motor2.getY(), joint2.getX() - this.motor2.getX());
        if (!isAngleValid(theta2))
            return;
        setJointXY(joint1, joint2);
        setAngles(theta1, theta2);
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
        this.validArmPosition = d < 2 * this.r;
        return this.validArmPosition;
    }

    private boolean isAngleValid(double theta){
        this.validArmPosition =  theta <= 0 || theta >= Math.PI;
        return this.validArmPosition;
    }

    private boolean isSingularity(double d1, double d2){
        this.validArmPosition = d1+d2 > 2*r;
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
