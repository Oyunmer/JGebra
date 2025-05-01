package net.orisestudios.com.func;

public class CubicFunction implements Function {
    private double a, b, c, d;
    
    public CubicFunction(double a, double b, double c, double d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }
    
    @Override
    public double evaluate(double x) {
        return a * x * x * x + b * x * x + c * x + d;
    }
    
    @Override
    public String toString() {
        return String.format("y = %.2fx³ + %.2fx² + %.2fx + %.2f", a, b, c, d);
    }
}