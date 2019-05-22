package ru.app.protocol.payout;

public enum StreamType {
    OUTPUT,
    OUTPUT_ENCRYPT,
    INPUT,
    INPUT_DECRYPT;

    public String toString() {
        return this.name().contains("INPUT") ? this.name() + " << " : this.name() + " >> ";
    }
}
