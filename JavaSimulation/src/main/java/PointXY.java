public class PointXY {
    private double x;
    private double y;
    private boolean penDown;

    public PointXY(){ }

    public PointXY(double x,double y,boolean pen){
        this.penDown = pen;
        this.x = x;
        this.y = y;
    }

    public double getX(){
        return this.x;
    }

    public double getY(){
        return this.y;
    }

    public boolean getPen(){
        return this.penDown;
    }

}
