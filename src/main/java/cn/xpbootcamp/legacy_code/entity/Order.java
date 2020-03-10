package cn.xpbootcamp.legacy_code.entity;

public class Order {
    public Order(String id, double amount) {
        this.id = id;
        this.amount = amount;
    }

    private String id;
    private double amount;

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
