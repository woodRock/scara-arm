package nz.ac.engr110.scara;

public class Point<T> {
    private final T x;
    private final T y;

    public Point(T x, T y){
        this.x = x;
        this.y = y;
    }

    public T getX(){
        return this.x;
    }

    public T getY() {
        return this.y;
    }
}
