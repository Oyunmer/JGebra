package net.orisestudios.com.func;

public class LinearFunction implements Function {
    private double a; // katsayı
    private double b; // sabit terim
    
    public LinearFunction(double a, double b) {
        this.a = a;
        this.b = b;
    }
    
    @Override
    public double evaluate(double x) {
        return a * x + b;
    }
    
    @Override
    public String toString() {
        return String.format("y = %.2fx + %.2f", a, b);
    }
}