//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

//package ToWebSite;

//import ToWebSite.PointXY;

import ecs100.UI;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;

public class Drawing {
    private ArrayList<PointXY> path = new ArrayList();

    public Drawing() {
    }

    public void add_point_to_path(double x, double y, boolean pen) {
        PointXY new_point = new PointXY(x, y, pen);
        this.path.add(new_point);
        UI.printf("Point added.x=%f y=%f pen=%b New path size - %d\n", Double.valueOf(x), Double.valueOf(y), Boolean.valueOf(pen), Integer.valueOf(this.path.size()));
    }

    public void print_path() {
        UI.printf("*************************\n");

        for(int i = 0; i < this.path.size(); ++i) {
            double x0 = this.path.get(i).get_x();
            double y0 = this.path.get(i).get_y();
            boolean p = this.path.get(i).get_pen();
            UI.printf("i=%d x=%f y=%f pen=%b\n", Integer.valueOf(i), Double.valueOf(x0), Double.valueOf(y0), Boolean.valueOf(p));
        }

        UI.printf("*************************\n");
    }

    public void draw() {
        for(int i = 1; i < this.path.size(); ++i) {
            PointXY p0 = this.get_drawing_point(i - 1);
            PointXY p1 = this.get_drawing_point(i);
            if(this.path.get(i).get_pen()) {
                UI.setColor(Color.BLUE);
            } else {
                UI.setColor(Color.LIGHT_GRAY);
            }

            UI.drawLine(p0.get_x(), p0.get_y(), p1.get_x(), p1.get_y());
        }

    }

    public int get_path_size() {
        return this.path.size();
    }

    public void path_raise_pen() {
        this.path.get(this.path.size() - 1).set_pen(false);
    }

    public PointXY get_path_last_point() {
        PointXY lp = this.path.get(this.path.size() - 1);
        return lp;
    }

    public void save_path(String fname) {
        try {
            File e = new File(fname);
            FileOutputStream is = new FileOutputStream(e);
            OutputStreamWriter osw = new OutputStreamWriter(is);
            BufferedWriter w = new BufferedWriter(osw);

            for(int i = 0; i < this.path.size(); i++) {
                String str_out;
                if(this.path.get(i).get_pen()) {
                    str_out = this.path.get(i).get_x() + " " + this.path.get(i).get_y() + " 1\n";
                } else {
                    str_out = this.path.get(i).get_x() + " " + this.path.get(i).get_y() + " 0\n";
                }

                w.write(str_out);
            }

            w.close();
        } catch (IOException var8) {
            UI.println("Problem writing to the file statsTest.txt");
        }

    }

    public void load_path(String fname) {
        String in_line = null;

        try {
            BufferedReader e = new BufferedReader(new FileReader(new File(fname)));
            this.path.clear();

            while((in_line = e.readLine()) != null) {
                UI.println(in_line);
                String[] tokens = in_line.split(" ");
                UI.println("Number of tokens in line " + in_line + ": " + tokens.length);
                UI.println("The tokens are:");
                UI.printf("%s %s %s\n", tokens[0], tokens[1], tokens[2]);
                double x = Double.parseDouble(tokens[0]);
                double y = Double.parseDouble(tokens[1]);
                boolean pen = Integer.parseInt(tokens[2]) == 1;
                this.add_point_to_path(x, y, pen);
            }
        } catch (Exception var10) {
            var10.printStackTrace();
        }

    }

    public void loadPathFromSvg(String fName) {
    }

    public int get_drawing_size() {
        return this.path.size();
    }

    public PointXY get_drawing_point(int i) {
        PointXY p = this.path.get(i);
        return p;
    }
}
