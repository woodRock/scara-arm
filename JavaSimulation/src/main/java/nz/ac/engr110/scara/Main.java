package nz.ac.engr110.scara;

import ecs100.UI;
import ecs100.UIFileChooser;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    private static final int HEIGHT = 480;
    private static final int WIDTH = 640;
    private double delay = 20;
    private Arm arm;
    private Drawing drawing;
    private ToolPath toolPath;
    private STATES state = STATES.PEN_DOWN;

    private enum STATES {
        INITIAL,
        INVERSE_KINEMATICS,
        PEN_DOWN,
        PEN_UP,
        DIRECT_KINEMATICS,
        AUTOMATED,
        MANUAL
    }

    public Main() {
        this.initializeUI();
        this.defaults();
    }

    private void initializeUI() {
        UI.initialise();
        UI.addSlider("Delay",1,50, speed -> this.delay = speed );
        UI.addButton("Draw", () -> this.state = STATES.PEN_DOWN );
        UI.addButton("Clear", this::hardReset);
        UI.addButton("Draw Circle", this::drawCircle);
        UI.addButton("Draw Line", this::drawLine);
        UI.addButton("Draw Square", this::drawSquare);
        UI.addButton("Show Angle", () -> this.state = STATES.INVERSE_KINEMATICS);
        UI.addButton("Show Direct Kinematics", () -> this.state = STATES.DIRECT_KINEMATICS);
        UI.addButton("Save Path", this::savePath);
        UI.addButton("Load Path", this::loadPath);
        UI.addButton("Save Angles", this::saveAngle);
        UI.addButton("Load Angles", this::loadAngle);
        UI.addButton("Save Pulse", this::savePulse);
        UI.addButton("Export Pulse",this::sendPulse);
        UI.addButton("Load SVG", this::loadSVG);
        UI.addButton("Quit", this::quit);
        UI.setMouseMotionListener(this::doMouse);
        UI.setKeyListener(this::doKeys);
    }

    private void quit(){
        UI.quit();
    }

    private void defaults() {
        this.arm = new Arm();
        hardReset();
    }

    private void hardReset(){
        reset();
        doMouse("moved",Main.WIDTH/2.0,Main.HEIGHT/4.0);
    }

    private void reset() {
        this.drawing = new Drawing();
        this.toolPath = new ToolPath();
    }

    private void clearDrawing(){
        UI.clearGraphics();
        UI.clearText();
        defaults();
    }

    public void drawCircle() {
        clearDrawing();
        this.state = STATES.AUTOMATED;
        final double diameter = 67;
        final double xOffset = Main.WIDTH/2.0;
        final double yOffset = Main.HEIGHT/2.0 - diameter;
        double increment = 0.1;
        for (double i = 0; i - increment <= 2* Math.PI; i += increment){
            drawPoint(xOffset + (diameter/2) * Math.sin(i), yOffset + (diameter/2) * Math.cos(i), true);
        }
        drawPoint(xOffset + (diameter/2) * Math.sin(0), yOffset + (diameter/2) * Math.cos(0), true);
        this.state = STATES.MANUAL;
    }

    public void drawLine() {
        clearDrawing();
        this.state = STATES.AUTOMATED;
        final int length = 100;
        final double xOffset = Main.WIDTH/2.0 - length/2.0;
        final double yOffset = Main.HEIGHT/2.0 - length;
        for (int i = 0; i < length; i++){
            drawPoint(i + xOffset, yOffset, true);
        }
        this.state = STATES.MANUAL;
    }

    public void drawSquare() {
        clearDrawing();
        this.state = STATES.AUTOMATED;
        final int length = 54;
        final double xOffset = Main.WIDTH/2.0 - length/2.0;
        final double yOffset = Main.HEIGHT/2.0 - 2*length;
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
        this.state = STATES.MANUAL;
    }

    public void savePath() {
        this.state = STATES.INITIAL;
        this.drawing.savePath(UIFileChooser.save());
    }

    public void loadPath() {
        clearDrawing();
        this.state = STATES.AUTOMATED;
        this.drawing.loadPath(UIFileChooser.open(), this.arm, this.delay);
        this.state = STATES.MANUAL;
    }

    public void saveAngle() {
        this.state = STATES.INITIAL;
        this.toolPath.saveAngles(this.drawing, this.arm);
    }

    public void loadAngle() {
        clearDrawing();
        this.state = STATES.AUTOMATED;
        try (
                Scanner sc = new Scanner(new File(UIFileChooser.open()));
                ){
            while (sc.hasNext()) {
                double t1 = Double.parseDouble(sc.nextLine());
                double t2 = Double.parseDouble(sc.nextLine());
                this.arm.setAngles(t1* Math.PI/180,t2 * Math.PI/180);
                this.arm.directKinematic();
                boolean pen = Double.parseDouble(sc.nextLine()) == 1;
                drawPoint(this.arm.getXTool(), this.arm.getYTool(), pen);
            }
        } catch (IOException error) {
            UI.println("Invalid Angle File:\n" + error.getMessage());
        } finally {
            this.state = STATES.MANUAL;
        }
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

    public void loadSVG() {
        clearDrawing();
        this.state = STATES.AUTOMATED;
        StringBuilder out = new StringBuilder();
        String fileName = UIFileChooser.open();
        try (
                Scanner spacesScanner = new Scanner(new File(fileName))
        ) {
            while (spacesScanner.hasNext()) {
                out.append(" " + addSpaces(spacesScanner.next()));
            }
        } catch (IOException e){
            UI.println("File not found:" + e.getMessage());
        }
        try (
                Scanner sc = new Scanner(out.toString());
        ){
            while(sc.hasNext()) {
                String string = sc.next();
                double x = Main.WIDTH/2.0 - 100;
                double y = Main.HEIGHT/2.0 - 150;
                double scalar = 0.6;
                switch (string) {
                    case "L":
                        x += scalar * sc.nextDouble();
                        y += scalar * sc.nextDouble();
                        drawPoint(x,y,true);
                        break;
                    case "M":
                        x += scalar * sc.nextDouble();
                        y += scalar * sc.nextDouble();
                        drawPoint(x,y,false);
                        break;
                    default:
                        break;
                }
            }
        } finally {
            this.state = STATES.MANUAL;
        }
    }

    public String addSpaces(String token) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < token.length() - 1; i++){
            String character = token.substring(i, i+1);
            switch (character){
                case "L": case "M":
                    character = " " + character + " ";
                    break;
                case " ":
                    character = " ";
                    break;
                default:
                    break;
            }
            result.append(character);
        }
        return result.toString();
    }

    public void doKeys(String action) {
        switch (action){
            case "b":
                this.state = STATES.PEN_UP;
                break;
            case "q":
                UI.quit();
                break;
            case "c":
                reset();
                break;
            case "p":
                this.state = (this.state == STATES.AUTOMATED)? STATES.PEN_DOWN : STATES.AUTOMATED;
                break;
            default:
                break;
        }
    }

    public void doMouse(String action, double x, double y) {
        if (isOffScreen(x,y) || isAutomated())
            return;
        drawMouseToolTip(x,y);
        drawLineGuide(x,y,action);
        calculatePosition(x,y);
        drawPenDown(x,y,action);
        drawPenUp(x,y,action);
        directKinematic();
        showAngle();
        draw();
    }

    private boolean isOffScreen(double x, double y){
        return x >= Main.WIDTH || y >= Main.HEIGHT;
    }

    private boolean isAutomated(){
        return this.state == STATES.AUTOMATED;
    }

    private void drawMouseToolTip(double x, double y){
        final double offset = 20;
        UI.clearGraphics();
        String outStr = String.format("%3.1f %3.1f", x, y);
        UI.drawString(outStr, x + offset, y + offset);
    }

    private void drawLineGuide(double x, double y, String action) {
        if (!isDrawingALine(action))
            return;
        PenPosition lp = this.drawing.getPathLastPoint();
        UI.setColor(Color.GRAY);
        UI.drawLine(lp.getX(), lp.getY(), x, y);
    }

    private boolean isDrawingALine(String action){
        return (this.state == STATES.PEN_DOWN) && action.equals("moved") && this.drawing.getPathSize() > 0;
    }

    private void calculatePosition(double x, double y){
        this.arm.inverseKinematic(x,y);
    }

    private void drawPenDown(double x, double y, String action){
        if (!isPenDown(action))
            return;
        drawPoint(x, y, true);
        this.state = STATES.PEN_UP;
    }

    private boolean isPenDown(String action){
        return this.state == STATES.PEN_DOWN && action.equals("clicked");
    }

    private void drawPenUp(double x, double y, String action){
        if (!isPenUp(action))
            return;
        drawPoint(x, y, false);
        this.state = STATES.PEN_DOWN;
    }

    private boolean isPenUp(String action){
        return this.state == STATES.PEN_UP && action.equals("clicked");
    }

    private void directKinematic() {
        if (!isDirectKinematics())
            return;
        this.arm.directKinematic();
        UI.clearText();
        UI.println(String.format("Θ₁:%.1f, Θ₂:%.1f", this.arm.getTheta1(), this.arm.getTheta2()));

    }

    private boolean isDirectKinematics() {
        return this.state == STATES.DIRECT_KINEMATICS;
    }

    private void showAngle(){
        if (!isInverseKinematics())
            return;
        UI.clearText();
        UI.println(String.format("Θ₁:%.1f, Θ₂:%.1f", this.arm.getTheta1(), this.arm.getTheta2()));
    }

    private boolean isInverseKinematics() {
        return this.state == STATES.INVERSE_KINEMATICS;
    }

    private void draw(){
        this.arm.draw();
        this.drawing.draw();
    }

    private void drawPoint(double x, double y, boolean pen){
        UI.clearGraphics();
        this.arm.inverseKinematic(x,y);
        this.arm.draw();
        this.drawing.addPointToPath(x, y, pen);
        this.drawing.draw();
        sleep();
    }

    private void sleep(){
        try {
            UI.sleep(this.delay);
        }
        catch (Exception ex) {
            UI.println("Sleep error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        new Main();
    }
}
