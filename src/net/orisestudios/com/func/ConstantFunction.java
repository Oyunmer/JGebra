package net.orisestudios.com.func;

public class ConstantFunction implements Function {
	
    private final double value;
    
    public ConstantFunction(double value) {this.value = value;}
    
    @Override
    public double evaluate(double x) {return value;}
    
    @Override
    public String toString() {return "y = " + value;}
    
}