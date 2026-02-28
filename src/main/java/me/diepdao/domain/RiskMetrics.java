package me.diepdao.domain;

public class RiskMetrics {
    private final Instrument instrument;
    private int netPosition;
    private double realizedPnl;
    private double unrealizedPnl;
    private double currentPrice; // For MTM

    // Simple Greeks/Sensitivities
    private double delta;
    private double dv01;

    public RiskMetrics(Instrument instrument) {
        this.instrument = instrument;
        this.netPosition = 0;
        this.realizedPnl = 0.0;
        this.unrealizedPnl = 0.0;
        this.currentPrice = 0.0;
        this.delta = 0.0;
        this.dv01 = 0.0;
    }

    public Instrument getInstrument() { return instrument; }
    public int getNetPosition() { return netPosition; }
    public void setNetPosition(int netPosition) { this.netPosition = netPosition; }

    public double getRealizedPnl() { return realizedPnl; }
    public void setRealizedPnl(double realizedPnl) { this.realizedPnl = realizedPnl; }

    public double getUnrealizedPnl() { return unrealizedPnl; }
    public void setUnrealizedPnl(double unrealizedPnl) { this.unrealizedPnl = unrealizedPnl; }

    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }

    public double getDelta() { return delta; }
    public void setDelta(double delta) { this.delta = delta; }

    public double getDv01() { return dv01; }
    public void setDv01(double dv01) { this.dv01 = dv01; }

    public double getTotalPnl() { return realizedPnl + unrealizedPnl; }
}
