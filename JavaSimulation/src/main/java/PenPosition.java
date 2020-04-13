public class PenPosition extends Point<Double> {
    private final boolean penDown;

    public PenPosition(double x, double y, boolean pen){
        super(x,y);
        this.penDown = pen;
    }

    public boolean getPen(){
        return this.penDown;
    }

}
