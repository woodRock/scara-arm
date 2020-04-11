import ecs100.UI;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

public class ToolPath {
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

    public void convertDrawingToAngles(Drawing drawing, Arm arm, String fileName) {
        for (int i = 0; i < drawing.getDrawingSize() - 1; i++){
            PointXY p0 = drawing.getDrawingPoint(i);
            PointXY p1 = drawing.getDrawingPoint(i+1);
            this.n_steps = (int)(Math.sqrt(Math.pow(p0.getX() - p1.getX(),2) + Math.pow(p0.getY() - p1.getY(),2)));
            for ( int j = 0 ; j< this.n_steps;j++) {
                double x = p0.getX() + j * (p1.getX() - p0.getX()) / this.n_steps;
                double y = p0.getY() + j * (p1.getY()-p0.getY()) / this.n_steps;
                try {
                    arm.inverseKinematic(x, y);
                } catch (Exception error){
                    UI.println("Invalid Position:\n" + error.getMessage());
                }
                this.theta1Vector.add(arm.getTheta1()*180/Math.PI);
                this.theta2Vector.add(arm.getTheta2()*180/Math.PI);
                if (p0.getPen()){
                    this.penVector.add(1);
                } else {
                    this.penVector.add(0);
                }
            }
        }
        if(fileName != null) {
            saveAngles(fileName);
        }
    }
    
    public void saveAngles(String fileName){
        for (int i = 0; i < this.theta1Vector.size(); i++) {
            UI.printf(" t1=%3.1f t2=%3.1f pen=%d\n", this.theta1Vector.get(i), this.theta2Vector.get(i), this.penVector.get(i));
        }
        try {
            File statText = new File(fileName);
            PrintStream out = new PrintStream(statText);
        for (int i = 1; i < this.theta1Vector.size(); i++) {
            out.println(this.theta1Vector.get(i));
            out.println(this.theta2Vector.get(i));
            out.println(this.penVector.get(i));
        }
        out.close();
        } catch (IOException e) {
            UI.println("Problem writing to the file statsTest.txt");
        }
    }

    public void convertAnglesToPwm(Arm arm){
        for (int i = 0; i < this.theta1Vector.size(); i++){
            arm.setAngles(this.theta1Vector.get(i),this.theta2Vector.get(i));
            this.pwm1Vector.add(arm.getPWM1());
            this.pwm2Vector.add(arm.getPWM2());
            if(this.penVector.get(i) ==1){
                this.pwm3Vector.add(1100);
            } else {
                this.pwm3Vector.add(1500);
            }
        }
    }

    public void savePWMFile(Drawing drawing, Arm arm){
        convertDrawingToAngles(drawing,arm,null);
        convertAnglesToPwm(arm);
        try {
            PrintStream out = new PrintStream(new File("pwm.txt"));

            for (int i = 0; i < this.pwm1Vector.size(); i++) {
                out.println(this.pwm1Vector.get(i) + "," + this.pwm2Vector.get(i) + "," + this.pwm3Vector.get(i));
            }
            out.println(1500+ "," + 1500 + "," + 1500);
            out.close();

        } catch (IOException e) {
            UI.println("Error: " + e);
        }
    }
}
