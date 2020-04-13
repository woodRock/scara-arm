import ecs100.UI;
import ecs100.UIFileChooser;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

public class ToolPath {
    private final int HIGH = 1500;
    private final int LOW = 1100;
    private int n_steps;
    private ArrayList<Double> theta1Vector;
    private ArrayList<Double> theta2Vector;
    private ArrayList<Integer> penVector;
    private ArrayList<Integer> pwm1Vector;
    private ArrayList<Integer> pwm2Vector;
    private ArrayList<Integer> pwm3Vector;

    public ToolPath() {
        this.n_steps = 50;
        this.theta1Vector = new ArrayList();
        this.theta2Vector = new ArrayList();
        this.penVector = new ArrayList();
        this.pwm1Vector = new ArrayList();
        this.pwm2Vector = new ArrayList();
        this.pwm3Vector = new ArrayList();
    }

    public void saveAngles(Drawing drawing, Arm arm){
        String file = UIFileChooser.save();
        if (!isValidFile(file)) {
            return;
        }
        convertDrawingToAngles(drawing, arm);
        try {
            PrintStream out = new PrintStream(new File(file));
            for (int i = 1; i < this.theta1Vector.size(); i++) {
                out.println(this.theta1Vector.get(i));
                out.println(this.theta2Vector.get(i));
                out.println(this.penVector.get(i));
            }
            out.close();
        } catch (IOException e) {
            UI.println("Error saving angle file: " + e.getMessage());
        }
    }

    public void convertDrawingToAngles(Drawing drawing, Arm arm) {
        for (int i = 0; i < drawing.getDrawingSize() - 1; i++) {
            PenPosition p0 = drawing.getDrawingPoint(i);
            PenPosition p1 = drawing.getDrawingPoint(i + 1);
            this.n_steps = (int) distance(p0.getX(), p1.getX(), p0.getY(), p1.getY());
            for (int j = 0; j < this.n_steps; j++) {
                double x = p0.getX() + j * (p1.getX() - p0.getX()) / this.n_steps;
                double y = p0.getY() + j * (p1.getY() - p0.getY()) / this.n_steps;
                arm.inverseKinematic(x, y);
                this.theta1Vector.add(arm.getTheta1() * 180 / Math.PI);
                this.theta2Vector.add(arm.getTheta2() * 180 / Math.PI);
                this.penVector.add((p1.getPen()) ? 1 : 0);
            }
        }
    }

    public void convertAnglesToPwm(Arm arm){
        for (int i = 0; i < this.theta1Vector.size(); i++){
            arm.setAngles(this.theta1Vector.get(i),this.theta2Vector.get(i));
            this.pwm1Vector.add(arm.getPWM1());
            this.pwm2Vector.add(arm.getPWM2());
            this.pwm1Vector.add((this.penVector.get(i) == 1)? LOW : HIGH);
        }
    }

    public void savePWMFile(Drawing drawing, Arm arm){
        String file = UIFileChooser.save();
        if (!isValidFile(file)){
            return;
        }
        convertDrawingToAngles(drawing,arm);
        convertAnglesToPwm(arm);
        try {
            PrintStream out = new PrintStream(new File(file));
            for (int i = 0; i < this.pwm1Vector.size(); i++) {
                out.println(this.pwm1Vector.get(i) + "," + this.pwm2Vector.get(i) + "," + this.pwm3Vector.get(i));
            }
            out.println(HIGH+ "," + HIGH + "," + HIGH);
            out.close();
        } catch (IOException e) {
            UI.println("Error saving PWM File: " + e);
        }
    }

    private boolean isValidFile(String file){
        return file != null;
    }

    private double distance(double x1, double x2, double y1, double y2) {
        return Math.sqrt(Math.pow((x2 - x1),2) + Math.pow((y2 - y1),2));
    }
}
