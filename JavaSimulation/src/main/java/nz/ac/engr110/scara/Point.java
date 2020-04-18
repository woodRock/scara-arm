package nz.ac.engr110.scara;

public class Point<T> {
    private T x;
    private T y;

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

    public void setX(T x){
        this.x = x;
    }

    public void setY(T y){
        this.y = y;
    }
}
