package net.orisestudios.com.func;

public class RationalFunction implements Function {
    private final double numerator;
    private final double denominator;

    public RationalFunction(double numerator, double denominator) {
        this.numerator = numerator;
        this.denominator = denominator;
    }

    @Override
    public double evaluate(double x) {
        if (denominator == 0 || x == 0) return Double.NaN;  // Tanımsız
        return numerator / (denominator * x);
    }

    @Override
    public String toString() {
        return String.format("y = %.2f / %.2fx", numerator, denominator);
    }
}