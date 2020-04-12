import ecs100.UI;
import ecs100.UIFileChooser;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    private final int HEIGHT = 480;
    private final int WIDTH = 640;
    private Arm arm;
    private Drawing drawing;
    private ToolPath toolPath;
    private STATES state = STATES.PEN_DOWN;
    private double delay = 20;

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
        UI.addButton("Load SVG", this::parseSVG);
        UI.addButton("Quit", () -> UI.quit());
        UI.setMouseMotionListener(this::doMouse);
        UI.setKeyListener(this::doKeys);
    }

    private void defaults() {
        this.arm = new Arm();
        hardReset();
    }

    private void reset() {
        this.drawing = new Drawing();
        this.toolPath = new ToolPath();
    }

    private void hardReset(){
        reset();
        doMouse("moved", this.WIDTH/2,this.HEIGHT/4);
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
        switch (action){
            case "b": {
                this.state = STATES.PEN_UP;
                break;
            }
            case "q": {
                UI.quit();
                break;
            }
            case "c": {
                reset();
                break;
            }
            case "p": {
                if (this.state != STATES.AUTOMATED){
                    this.state = STATES.AUTOMATED;
                } else {
                    this.state = STATES.PEN_DOWN;
                }
            }
        }
    }

    public void doMouse(String action, double x, double y) {
        if (isOffScreen(x,y) || this.state.equals(STATES.AUTOMATED)) {
            return;
        }
        drawMouseToolTip(x,y);
        drawLineGuide(x,y,action);
        calculatePosition(x,y);
        drawPenDown(x,y,action);
        drawPenUp(x,y,action);
        directKinematic(action);
        showAngle();
        draw();
    }

    private void showAngle(){
        if (this.state == STATES.INVERSE_KINEMATICS){
            UI.clearText();
            UI.println(String.format("Θ₁:%.1f, Θ₂:%.1f", this.arm.getTheta1(), this.arm.getTheta2()));
        }
    }

    private void calculatePosition(double x, double y){
        this.arm.inverseKinematic(x,y);
    }

    private void directKinematic(String action){
        if (this.state == STATES.DIRECT_KINEMATICS){
            this.arm.directKinematic();
            UI.clearText();
            UI.println(String.format("Θ₁:%.1f, Θ₂:%.1f", this.arm.getTheta1(), this.arm.getTheta2()));
        }
    }

    private void drawPenDown(double x, double y, String action){
        if (isPendDown(action)) {
            this.state = STATES.PEN_UP;
            this.drawing.addPointToPath(x, y, true);
        }
    }

    private boolean isPendDown(String action){
        return this.state == STATES.PEN_DOWN && action.equals("clicked");
    }

    private void drawPenUp(double x, double y, String action){
        if (isPenUp(action)) {
            this.state = STATES.PEN_DOWN;
            this.drawing.addPointToPath(x, y, false);
        }
    }

    private boolean isPenUp(String action){
        return this.state == STATES.PEN_UP && action.equals("clicked");
    }

    private void drawLineGuide(double x, double y, String action) {
        if (!isDrawingALine(action)){
            return;
        }
        PenPosition lp = this.drawing.getPathLastPoint();
        UI.setColor(Color.GRAY);
        UI.drawLine(lp.getX(), lp.getY(), x, y);
    }

    private boolean isDrawingALine(String action){
        return (this.state == STATES.PEN_DOWN) && action.equals("moved") && this.drawing.getPathSize() > 0;
    }

    private void drawMouseToolTip(double x, double y){
        final double offset = 20;
        UI.clearGraphics();
        String outStr = String.format("%3.1f %3.1f", x, y);
        UI.drawString(outStr, x + offset, y + offset);
    }

    private void draw(){
        this.arm.draw();
        this.drawing.draw();
    }

    private boolean isOffScreen(double x, double y){
        return x >= this.WIDTH || y >= this.HEIGHT;
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
        this.toolPath.convertDrawingToAngles(this.drawing, this.arm, UIFileChooser.save());
    }

    public void loadAngle() {
        clearDrawing();
        this.state = STATES.AUTOMATED;
        try {
            Scanner sc = new Scanner(new File(UIFileChooser.open()));
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

    public void drawCircle() {
        clearDrawing();
        this.state = STATES.AUTOMATED;
        final double diameter = 67;
        final double xOffset = this.WIDTH/2;
        final double yOffset = this.HEIGHT/2 - diameter;
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
        final int xOffset = this.WIDTH/2 - length/2;
        final int yOffset = this.HEIGHT/2 - length;
        for (int i = 0; i < length; i++){
            drawPoint(i + xOffset, yOffset, true);
        }
        this.state = STATES.MANUAL;
    }

    public void drawSquare() {
        clearDrawing();
        this.state = STATES.AUTOMATED;
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
        this.state = STATES.MANUAL;
    }

    public void parseSVG() {
        clearDrawing();
        this.state = STATES.AUTOMATED;
        String fileName = UIFileChooser.open();
        try {
            Scanner spacesScanner = new Scanner(new File(fileName));
            String out = "";
            while(spacesScanner.hasNext()){
                out += " ";
                out += addSpaces(spacesScanner.next());
            }
            Scanner sc = new Scanner(out);
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
        } finally {
            this.state = STATES.MANUAL;
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

    private void drawPoint(double x, double y, boolean pen){
        UI.clearGraphics();
        this.arm.inverseKinematic(x,y);
        this.arm.draw();
        this.drawing.addPointToPath(x, y, pen);
        this.drawing.draw();
        sleep();
    }

    private void sleep(){
        int delay = (int) this.delay;
        try {
            UI.sleep(delay);
        }
        catch (Exception ex) {
            UI.println("Sleep error: " + ex.getMessage());
        }
    }

    private void clearDrawing(){
        UI.clearGraphics();
        UI.clearText();
        defaults();
    }

    public static void main(String[] args) {
        new Main();
    }
}
