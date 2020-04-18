package nz.ac.engr110.scara;

import ecs100.UI;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;

public class Drawing {
    private ArrayList<PenPosition> path;

    public Drawing() {
        this.path = new ArrayList<>();
    }

    public void addPointToPath(double x, double y, boolean pen) {
        PenPosition penPosition = new PenPosition(x, y, pen);
        this.path.add(penPosition);
    }

    public void printPath() {
        UI.printf("*************************\n");
        for(int i = 0; i < this.path.size(); ++i) {
            double x0 = this.path.get(i).getX();
            double y0 = this.path.get(i).getY();
            boolean p = this.path.get(i).getPen();
            UI.printf("i=%d x=%f y=%f pen=%b\n", i, x0, y0, p);
        }
        UI.printf("*************************\n");
    }

    public void draw() {
        for(int i = 1; i < this.path.size(); ++i) {
            PenPosition p0 = this.getDrawingPoint(i - 1);
            PenPosition p1 = this.getDrawingPoint(i);
            if(this.path.get(i).getPen()) {
                UI.setLineWidth(2);
                UI.setColor(Color.BLUE);
                UI.drawLine(p0.getX(), p0.getY(), p1.getX(), p1.getY());
            } else {
                UI.setColor(Color.LIGHT_GRAY);
            }
        }
    }

    public int getPathSize() {
        return this.path.size();
    }

    public PenPosition getPathLastPoint() {
        return this.path.get(this.path.size() - 1);
    }

    public void savePath(String fileName) {
        try (
                BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(fileName))));
                ){
            StringBuilder strOut;
            for (PenPosition penPosition : this.path) {
                strOut = new StringBuilder();
                strOut.append(penPosition.getX() + " " + penPosition.getY() + " ");
                strOut.append((penPosition.getPen()) ? "1" : "0");
                strOut.append("\n");
                w.write(strOut.toString());
            }
        } catch (Exception error) {
            UI.println("Problem writing to the file statsTest.txt:\n" + error.getMessage());
        }
    }

    public void loadPath(String fileName, Arm arm, double delay) {
        String inLine;
        try (
                BufferedReader e = new BufferedReader(new FileReader(new File(fileName)));
        ){
            this.path.clear();
            while((inLine = e.readLine()) != null) {
                UI.println(inLine);
                String[] tokens = inLine.split(" ");
                double x = Double.parseDouble(tokens[0]);
                double y = Double.parseDouble(tokens[1]);
                boolean pen = Integer.parseInt(tokens[2]) == 1;
                UI.clearGraphics();
                this.addPointToPath(x, y, pen);
                arm.inverseKinematic(x,y);
                arm.draw();
                this.draw();
                UI.sleep(delay);
            }
        } catch (Exception error) {
            UI.println("Invalid Points File:\n" + error.getMessage());
        }
    }

    public int getDrawingSize() {
        return this.path.size() - 1;
    }

    public PenPosition getDrawingPoint(int i) {
        return this.path.get(i);
    }
}
