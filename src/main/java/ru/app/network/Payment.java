package ru.app.network;

public class Payment {
    private long id;
    private String provider;
    private String number;
    private String text;
    private int sum;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getSum() {
        return sum;
    }

    public void setSum(int sum) {
        this.sum = sum;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", provider='" + provider + '\'' +
                ", number='" + number + '\'' +
                ", text='" + text + '\'' +
                ", sum=" + sum +
                '}';
    }
}
