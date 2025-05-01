package net.orisestudios.com.func;

public class QuadraticFunction implements Function {
    private double a, b, c;
    
    public QuadraticFunction(double a, double b, double c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }
    
    @Override
    public double evaluate(double x) {
        return a * x * x + b * x + c;
    }
    
    @Override
    public String toString() {
        return String.format("y = %.2fx² + %.2fx + %.2f", a, b, c);
    }
}