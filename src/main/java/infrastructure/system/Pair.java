package infrastructure.system;

public class Pair<T,Z> {

    private T first;
    private Z second;

    public Pair (T first, Z second){
        this.first = first;
        this.second = second;
    }

    public T getFirst() {
        return first;
    }

    public void setFirst(T first) {
        this.first = first;
    }

    public Z getSecond() {
        return second;
    }

    public void setSecond(Z second) {
        this.second = second;
    }
}
