package net.orisestudios.com.func;

import java.util.List;

import net.orisestudios.com.func.FunctionParser.Term;

public class PolynomialFunction implements Function {
    private final List<FunctionParser.Term> terms;
    
    public PolynomialFunction(List<FunctionParser.Term> terms) {
        this.terms = terms;
    }
    
    @Override
    public double evaluate(double x) {
        double result = 0;
        for (FunctionParser.Term term : terms) {
            result += term.coefficient * Math.pow(x, term.power);
        }
        return result;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("y = ");
        boolean firstTerm = true;
        
        for (FunctionParser.Term term : terms) {
            if (!firstTerm && term.coefficient > 0) {
                sb.append(" + ");
            } else if (term.coefficient < 0) {
                sb.append(" - ");
            }
            
            double absCoeff = Math.abs(term.coefficient);
            if (term.power == 0) {
                sb.append(absCoeff);
            } else {
                if (absCoeff != 1) {
                    sb.append(absCoeff);
                }
                sb.append("x");
                if (term.power != 1) {
                    sb.append("^").append(term.power);
                }
            }
            
            firstTerm = false;
        }
        
        return sb.toString();
    }

	public List<Term> getTerms() {
		return terms;
	}
}