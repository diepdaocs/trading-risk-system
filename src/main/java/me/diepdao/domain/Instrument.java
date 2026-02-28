package me.diepdao.domain;

import java.util.Objects;

public class Instrument {
    private final String symbol;
    private final AssetClass assetClass;

    public Instrument(String symbol, AssetClass assetClass) {
        this.symbol = symbol;
        this.assetClass = assetClass;
    }

    public String getSymbol() {
        return symbol;
    }

    public AssetClass getAssetClass() {
        return assetClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Instrument that = (Instrument) o;
        return Objects.equals(symbol, that.symbol) && assetClass == that.assetClass;
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, assetClass);
    }

    @Override
    public String toString() {
        return symbol + " (" + assetClass + ")";
    }
}
