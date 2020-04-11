import ecs100.UI;
import ecs100.UIFileChooser;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    private final int HEIGHT = 480;
    private final int WIDTH = 640;
    private Arm arm;
    private Drawing drawing;
    private ToolPath toolPath;
    private int state;

    private enum STATES {
        INITIAL,
        INVERSED,
        STARTED,
        FINISH
    }

    public Main() {
        this.initializeUI();
        this.defaults();
    }

    private void initializeUI() {
        UI.initialise();
        UI.addButton("Clear Drawing", this::defaults);
        UI.addButton("xy to angles", this::inverse);
        UI.addButton("Enter path XY", this::enterPathXY);
        UI.addButton("Save path XY", this::saveXY);
        UI.addButton("Load path XY", this::loadXY);
        UI.addButton("Save path Ang", this::saveAngle);
        UI.addButton("Load path Ang:Play", this::loadAngle);
        UI.addButton("Check Direct Kinematics", this::checkDirect);
        UI.addButton("Save Pulse", this::savePulse);
        UI.addButton("Send pulses to RPi",this::sendPulse);
        UI.addButton("Load SVG", this::drawSVG);
        UI.addButton("Circle", this::drawCircle);
        UI.addButton("Line", this::drawLine);
        UI.addButton("Square", this::drawSquare);
        UI.setMouseMotionListener(this::doMouse);
        UI.setKeyListener(this::doKeys);
    }

    private void defaults() {
        this.arm = new Arm();
        this.drawing = new Drawing();
        this.toolPath = new ToolPath();
        this.arm.draw();
        this.drawing.draw();
    }

    public void checkDirect(){
        this.state = 4;
    }

    public void savePulse(){
        this.toolPath.savePWMFile(this.drawing, this.arm);
    }

    public void sendPulse() {
        try {
            Runtime.getRuntime().exec("expect scp.exp");
        } catch (IOException error) {
            UI.println("Couldn't send pulse:\n" + error.getMessage());
        }
    }

    public void doKeys(String action) {
        UI.printf("Key :%s \n", action);
        if(action.equals("b")) {
            this.state = 3;
        }
    }

    public void doMouse(String action, double x, double y) {
        if (isOffScreen(x,y)) {
            return;
        }
        UI.clearGraphics();
        String outStr = String.format("%3.1f %3.1f", x, y);
        UI.drawString(outStr, x + 10.0D, y + 10.0D);
        this.arm.drawField();
        this.drawing.draw();
        if (this.state == 1 && action.equals("clicked")) {
            moveArm(x,y);
            this.arm.draw();
        } else {
            if ((this.state == 2 || this.state == 3) && action.equals("moved")) {
                moveArm(x,y);
                this.arm.draw();
                if(this.state == 2 && this.drawing.getPathSize() > 0) {
                    new PointXY();
                    PointXY lp = this.drawing.getPathLastPoint();
                    UI.setColor(Color.GRAY);
                    UI.drawLine(lp.getX(), lp.getY(), x, y);
                }
                this.drawing.draw();
            }
            if (this.state == 2 && action.equals("clicked")) {
                this.drawing.addPointToPath(x, y, true);
                moveArm(x,y);
                this.arm.draw();
                this.drawing.draw();
                this.drawing.printPath();
            }
            if (this.state == 3 && action.equals("clicked")) {
                this.drawing.addPointToPath(x, y, false);
                moveArm(x,y);
                this.arm.draw();
                this.drawing.draw();
                this.drawing.printPath();
                this.state = 2;
            }
            if (this.state == 4 && action.equals("clicked")){
                moveArm(x,y);
                this.arm.draw();
                this.arm.directKinematic();
            }
        }
    }

    private void moveArm(double x, double y){
        try {
            this.arm.inverseKinematic(x,y);
        } catch (Exception error){
            // UI.println("Invalid Position:" + error.getMessage());
        }
    }

    private boolean isOffScreen(double x, double y){
        return x >= this.WIDTH || y >= this.HEIGHT;
    }

    public void saveXY() {
        this.state = 0;
        this.drawing.savePath(UIFileChooser.save());
    }

    public void loadXY() {
        this.state = 0;
        this.drawing.loadPath(UIFileChooser.open());
        this.drawing.draw();
    }

    public void inverse() {
        this.state = 1;
        this.arm.draw();
    }

    public void enterPathXY() {
        this.state = 2;
    }

    public void saveAngle() {
        this.toolPath.convertDrawingToAngles(this.drawing, this.arm, UIFileChooser.save());
    }

    public void loadAngle() {
        try {
            Scanner sc = new Scanner(new File(UIFileChooser.open()));
            while (sc.hasNext()) {
                double x = Double.parseDouble(sc.nextLine());
                double y = Double.parseDouble(sc.nextLine());
                this.arm.setAngles(x * Math.PI/180,y* Math.PI/180);
                this.arm.directKinematic();
                if (Double.parseDouble(sc.nextLine()) == 1) {
                    this.drawing.addPointToPath(arm.getXTool(), arm.getYTool(), true);
                } else this.drawing.addPointToPath(arm.getXTool(), arm.getYTool(), false);
                this.drawing.draw();
            }
        } catch (IOException error) {
            UI.println("Invalid Angle File:\n" + error.getMessage());
        }
    }

    public void drawCircle() {
        clearDrawing();
        final double diameter = 67;
        final double xOffset = this.WIDTH/2;
        final double yOffset = this.HEIGHT/2 - diameter;
        double increment = 0.01;
        for  (double i = 0; i - increment <= 2* Math.PI; i += increment){
            drawPoint(xOffset + (diameter/2) * Math.sin(i), yOffset + (diameter/2) * Math.cos(i), true);
        }
    }

    public void drawLine() {
        clearDrawing();
        int length = 100;
        int xOffset = this.WIDTH/2 - length/2;
        int yOffset = this.HEIGHT/2 - length;
        int increments = 1;
        for (int i = 0; i < length; i+= increments){
            drawPoint(i + xOffset, yOffset, true);
        }
    }

    public void drawSquare() {
        clearDrawing();
        final int length = 54;
        final int xOffset = this.WIDTH/2 - length/2;
        final int yOffset = this.HEIGHT/2 - 2*length;
        for (int i = 0; i < length; i++){
            drawPoint(i + xOffset, yOffset, true);
        }
        for (int i = 0; i < length; i++){
            drawPoint(length + xOffset, yOffset + i, true);
        }
        for (int i = 0; i < length; i++){
            drawPoint(xOffset + length - i,length + yOffset, true);
        }
        for (int i = 0; i <= length; i++){
            drawPoint(xOffset,yOffset + length - i, true);
        }
        this.drawing.addPointToPath(length + xOffset,yOffset,true);
    }

    private void drawPoint(double x, double y, boolean pen){
        UI.clearGraphics();
        this.arm.drawField();
        moveArm(x,y);
        this.arm.draw();
        this.drawing.addPointToPath(x, y, pen );
        this.drawing.draw();
        sleep();
    }

    private void sleep(){
        final int delay = 5;
        try {
            Thread.sleep(delay);
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
    }

    public void drawSVG() {
        String fileName = UIFileChooser.open();
        clearDrawing();
        try {
            Scanner spacesScanner = new Scanner(new File(fileName + ".txt"));
            PrintStream out = new PrintStream(new File(fileName + "2.txt"));
            while(spacesScanner.hasNext()){
                out.print(" ");
                out.print(addSpaces(spacesScanner.next()));
            }
            Scanner sc = new Scanner(new File(fileName + "2.txt"));
            while(sc.hasNext()) {
                String string = sc.next();
                double x = this.WIDTH/2 - 100;
                double y = this.HEIGHT/2 - 150;
                double scalar = 0.6;
                if (string.equals("L")) {
                    x += scalar * sc.nextDouble();
                    y += scalar * sc.nextDouble();
                    drawPoint(x,y,true);
                }
                if (string.equals("M")) {
                    x += scalar * sc.nextDouble();
                    y += scalar * sc.nextDouble();
                    drawPoint(x,y,false);
                }
            }
        }
        catch (Exception error) {
            UI.println("SVG File Error:\n " + error.getMessage());
        }
    }

    public String addSpaces(String token) {
        ArrayList<String> temp = new ArrayList();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < token.length() - 1; i++){
            String character = token.substring(i, i+1);
            if (character.equals("L") || character.equals("M")){
                character = " " + character + " ";
            }
            if (character.equals(" ")) {
                character = " ";
            }
            temp.add(character);
        }
        for (String s: temp){
            result.append(s);
        }
        return result.toString();
    }

    public static void main(String[] args) {
        new Main();
    }

    private void clearDrawing(){
        UI.clearGraphics();
        this.drawing = new Drawing();
    }
}
