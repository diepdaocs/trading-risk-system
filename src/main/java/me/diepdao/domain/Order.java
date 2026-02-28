package me.diepdao.domain;

import java.util.UUID;

public class Order {
    private final String id;
    private final Instrument instrument;
    private final Side side;
    private final double price; // 0 for market order
    private final int totalQuantity;
    private int filledQuantity;

    public Order(Instrument instrument, Side side, double price, int totalQuantity) {
        this.id = UUID.randomUUID().toString();
        this.instrument = instrument;
        this.side = side;
        this.price = price;
        this.totalQuantity = totalQuantity;
        this.filledQuantity = 0;
    }

    public String getId() { return id; }
    public Instrument getInstrument() { return instrument; }
    public Side getSide() { return side; }
    public double getPrice() { return price; }
    public int getTotalQuantity() { return totalQuantity; }
    public int getFilledQuantity() { return filledQuantity; }
    public int getRemainingQuantity() { return totalQuantity - filledQuantity; }
    public boolean isFilled() { return filledQuantity >= totalQuantity; }

    public void fill(int quantity) {
        this.filledQuantity += quantity;
    }
}
