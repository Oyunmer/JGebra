package net.orisestudios.com.func;

import java.util.Objects;

public final class TrigonometricFunction implements Function {
    public enum Type {
        SIN("sin"), 
        COS("cos"), 
        TAN("tan"), 
        COT("cot");

        private final String symbol;

        Type(String symbol) {
            this.symbol = Objects.requireNonNull(symbol);
        }

        public static Type fromString(String text) {
            Objects.requireNonNull(text, "Function type cannot be null");
            for (Type type : values()) {
                if (type.symbol.equalsIgnoreCase(text.trim())) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unsupported trigonometric function: " + text);
        }
    }

    private final Type type;          // sin, cos, tan, cot
    private final double amplitude;   // Genlik (örn: 2 in "2sin(x)")
    private final double frequency;   // Frekans (örn: 3 in "sin(3x)")
    private final double phaseShift;  // Faz kayması (örn: +1 in "sin(x+1)")
    private final double verticalShift; // Dikey kayma (örn: -5 in "sin(x)-5")

    // Constructor
    public TrigonometricFunction(Type type, 
                               double amplitude,
                               double frequency,
                               double phaseShift,
                               double verticalShift) {
        this.type = Objects.requireNonNull(type);
        this.amplitude = amplitude;
        this.frequency = frequency;
        this.phaseShift = phaseShift;
        this.verticalShift = verticalShift;
    }

    // Fonksiyon değerini hesapla
    @Override
    public double evaluate(double x) {
        final double argument = frequency * x + phaseShift;
        return switch (type) {
            case SIN -> amplitude * Math.sin(argument) + verticalShift;
            case COS -> amplitude * Math.cos(argument) + verticalShift;
            case TAN -> amplitude * Math.tan(argument) + verticalShift;
            case COT -> {
                double tanValue = Math.tan(argument);
                yield tanValue == 0 ? Double.NaN : amplitude * (1/tanValue) + verticalShift;
            }
        };
    }

    // String'e dönüştürme (konsolda okunabilir çıktı)
    @Override
    public String toString() {
        return String.format("y = %.2f %s(%.2fx %+.2f) %+.2f",
            amplitude,
            type.symbol,
            frequency,
            phaseShift,
            verticalShift);
    }

    // Getter metodları (isteğe bağlı)
    public Type getType() { return type; }
    public double getAmplitude() { return amplitude; }
    public double getFrequency() { return frequency; }
    public double getPhaseShift() { return phaseShift; }
    public double getVerticalShift() { return verticalShift; }
}