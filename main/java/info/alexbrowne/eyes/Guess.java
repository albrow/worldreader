package info.alexbrowne.eyes;

/**
 * Created by alex on 11/15/14.
 */
public class Guess {
    private double confidence;
    private String value;

    public Guess(String value, String confidence) {
        this.value = value;
        this.confidence = Double.parseDouble(confidence);
    }

    public String getValue() {
        return this.value;
    }

    public double getConfidence() {
        return this.confidence;
    }

    public String toString() {
        return this.value;
    }
}
