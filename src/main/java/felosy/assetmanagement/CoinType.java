package felosy.assetmanagement;

public enum CoinType {
    BTC,
    ETH,
    XRP,
    LTC,
    ADA,
    DOT,
    DOGE,
    OTHER;
    
    @Override
    public String toString() {
        return this.name();
    }
}