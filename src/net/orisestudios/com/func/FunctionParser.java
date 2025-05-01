package net.orisestudios.com.func;

import java.util.*;
import java.util.regex.*;

public class FunctionParser {
    public static Function parse(String input) throws IllegalArgumentException {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Fonksiyon boş olamaz!");
        }

        input = input.replaceAll("\\s+", "").toLowerCase();

        Matcher trigMatcher = Pattern.compile(
            "y=([+-]?\\d*\\.?\\d*)?(sin|cos|tan|cot)\\(([+-]?\\d*\\.?\\d*)x([+-]\\d*\\.?\\d*)?\\)([+-]\\d*\\.?\\d*)?"
        ).matcher(input);
        
        if (trigMatcher.matches()) {
            try {
                String ampGroup = trigMatcher.group(1);
                double amplitude = 1.0;
                if (ampGroup != null) {
                    ampGroup = ampGroup.replace("+", "").replace("*", "").trim();
                    amplitude = ampGroup.isEmpty() ? 1.0 : Double.parseDouble(ampGroup);
                }
                
                TrigonometricFunction.Type type = TrigonometricFunction.Type.fromString(trigMatcher.group(2));
                double frequency = trigMatcher.group(3) == null ? 1.0 : 
                                 Double.parseDouble(trigMatcher.group(3).replace("*", ""));
                double phaseShift = trigMatcher.group(4) == null ? 0.0 : 
                                  Double.parseDouble(trigMatcher.group(4).replace("*", ""));
                double verticalShift = trigMatcher.group(5) == null ? 0.0 : 
                                     Double.parseDouble(trigMatcher.group(5).replace("*", ""));
                
                return new TrigonometricFunction(type, amplitude, frequency, phaseShift, verticalShift);
            } catch (Exception e) {
                throw new IllegalArgumentException("Geçersiz trigonometrik fonksiyon: " + input + 
                       "\nÖrnek: y=2sin(3x+1)-5");
            }
        }

        Matcher divisionMatcher = Pattern.compile(
            "y=([+-]?\\d*\\.?\\d*)x/([+-]?\\d+\\.?\\d*)|" +  // y=x/2, y=3x/5
            "y=([+-]?\\d+\\.?\\d*)/x"                          // y=2/x
        ).matcher(input);

        if (divisionMatcher.matches()) {
            try {
                if (divisionMatcher.group(1) != null) { // y=kx/d
                    double k = divisionMatcher.group(1).isEmpty() ? 1.0 : Double.parseDouble(divisionMatcher.group(1));
                    double d = Double.parseDouble(divisionMatcher.group(2));
                    return new PolynomialFunction(List.of(new Term(k/d, 1)));
                } 
                else { // y=k/x
                    double k = Double.parseDouble(divisionMatcher.group(3));
                    return new RationalFunction(k, 1.0);
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Geçersiz bölme: " + input + "\nÖrnek: y=x/2 veya y=3/x");
            }
        }

        if (input.matches("y=[+-]?\\d*\\.?\\d+")) return new ConstantFunction(Double.parseDouble(input.substring(2)));
        if (input.equals("y=x")) return new PolynomialFunction(List.of(new Term(1.0, 1)));
        if (input.equals("y=-x")) return new PolynomialFunction(List.of(new Term(-1.0, 1)));

        if (input.startsWith("y=")) {
            List<Term> terms = new ArrayList<>();
            Matcher termMatcher = Pattern.compile(
                "([+-]?\\d*\\.?\\d*)x\\^(\\d+)|" +  // x^3, -2x^2
                "([+-]?\\d*\\.?\\d*)x|" +          // -x, 3x
                "([+-]\\d+\\.?\\d*)"               // -2, +5
            ).matcher(input.substring(2));

            while (termMatcher.find()) {
                if (termMatcher.group(1) != null) { // x^3 gibi
                    terms.add(new Term(parseCoeff(termMatcher.group(1)), Integer.parseInt(termMatcher.group(2))));
                } 
                else if (termMatcher.group(3) != null) { // -x gibi
                    terms.add(new Term(parseCoeff(termMatcher.group(3)), 1));
                }
                else if (termMatcher.group(4) != null) { // -2 gibi
                    terms.add(new Term(Double.parseDouble(termMatcher.group(4)), 0));
                }
            }
            if (!terms.isEmpty()) return new PolynomialFunction(terms);
        }

        throw new IllegalArgumentException("Geçersiz fonksiyon formatı: " + input);
    }

    private static double parseCoeff(String coeffStr) {
        if (coeffStr == null || coeffStr.isEmpty() || coeffStr.equals("+")) return 1.0;
        if (coeffStr.equals("-")) return -1.0;
        return Double.parseDouble(coeffStr);
    }

    public static class Term {
        public double coefficient;
        public int power;
        public Term(double coefficient, int power) {
            this.coefficient = coefficient;
            this.power = power;
        }
    }
}