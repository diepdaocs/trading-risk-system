package me.diepdao.domain;

import java.time.Instant;
import java.util.UUID;

public class Trade {
    private final String tradeId;
    private final Instrument instrument;
    private final double price;
    private final int quantity;
    private final String buyerOrderId;
    private final String sellerOrderId;
    private final Instant timestamp;

    public Trade(Instrument instrument, double price, int quantity, String buyerOrderId, String sellerOrderId) {
        this.tradeId = UUID.randomUUID().toString();
        this.instrument = instrument;
        this.price = price;
        this.quantity = quantity;
        this.buyerOrderId = buyerOrderId;
        this.sellerOrderId = sellerOrderId;
        this.timestamp = Instant.now();
    }

    public String getTradeId() { return tradeId; }
    public Instrument getInstrument() { return instrument; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getBuyerOrderId() { return buyerOrderId; }
    public String getSellerOrderId() { return sellerOrderId; }
    public Instant getTimestamp() { return timestamp; }
}
