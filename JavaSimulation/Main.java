//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

//package ToWebSite;

//import ToWebSite.Arm;
//import ToWebSite.Drawing;
//import ToWebSite.PointXY;
//import ToWebSite.ToolPath;
import ecs100.UI;
import ecs100.UIButtonListener;
import ecs100.UIFileChooser;
import ecs100.UIKeyListener;
import ecs100.UIMouseListener;
import java.awt.Color;

public class Main {
    private Arm arm;
    private Drawing drawing;
    private ToolPath tool_path;
    private int state;

    public Main() {
        UI.initialise();
        UI.addButton("xy to angles", this::inverse);
        UI.addButton("Enter path XY", this::enter_path_xy);
        UI.addButton("Save path XY", this::save_xy);
        UI.addButton("Load path XY", this::load_xy);
        UI.addButton("Save path Ang", this::save_ang);
        UI.addButton("Load path Ang:Play", this::load_ang);
        UI.setMouseMotionListener(this::doMouse);
        UI.setKeyListener(this::doKeys);
        this.arm = new Arm();
        this.drawing = new Drawing();
        this.run();
        this.arm.draw();
    }

    public void doKeys(String action) {
        UI.printf("Key :%s \n", new Object[]{action});
        if(action.equals("b")) {
            this.state = 3;
        }

    }

    public void doMouse(String action, double x, double y) {
        UI.clearGraphics();
        String out_str = String.format("%3.1f %3.1f", new Object[]{Double.valueOf(x), Double.valueOf(y)});
        UI.drawString(out_str, x + 10.0D, y + 10.0D);
        if(this.state == 1 && action.equals("clicked")) {
            this.arm.inverseKinematic(x, y);
            this.arm.draw();
        } else {
            if((this.state == 2 || this.state == 3) && action.equals("moved")) {
                this.arm.inverseKinematic(x, y);
                this.arm.draw();
                if(this.state == 2 && this.drawing.get_path_size() > 0) {
                    new PointXY();
                    PointXY lp = this.drawing.get_path_last_point();
                    UI.setColor(Color.GRAY);
                    UI.drawLine(lp.get_x(), lp.get_y(), x, y);
                }

                this.drawing.draw();
            }

            if(this.state == 2 && action.equals("clicked")) {
                UI.printf("Adding point x=%f y=%f\n", new Object[]{Double.valueOf(x), Double.valueOf(y)});
                this.drawing.add_point_to_path(x, y, true);
                this.arm.inverseKinematic(x, y);
                this.arm.draw();
                this.drawing.draw();
                this.drawing.print_path();
            }

            if(this.state == 3 && action.equals("clicked")) {
                this.drawing.add_point_to_path(x, y, false);
                this.arm.inverseKinematic(x, y);
                this.arm.draw();
                this.drawing.draw();
                this.drawing.print_path();
                this.state = 2;
            }

        }
    }

    public void save_xy() {
        this.state = 0;
        String fname = UIFileChooser.save();
        this.drawing.save_path(fname);
    }

    public void enter_path_xy() {
        this.state = 2;
    }

    public void inverse() {
        this.state = 1;
        this.arm.draw();
    }

    public void load_xy() {
        this.state = 0;
        String fname = UIFileChooser.open();
        this.drawing.load_path(fname);
        this.drawing.draw();
        this.arm.draw();
    }

    public void save_ang() {
        String fname = UIFileChooser.open();
        this.tool_path.convert_drawing_to_angles(this.drawing, this.arm, fname);
    }

    public void load_ang() {
    }

    public void run() {
        while(true) {
            this.arm.draw();
            UI.sleep(20.0D);
        }
    }

    public static void main(String[] args) {
        new Main();
    }
}
