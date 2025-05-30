package felosy.assetmanagement;

public enum TickerType {
    AAPL,
    ABBV,
    ABT,
    ACN,
    ADBE,
    AIG,
    AMD,
    AMGN,
    AMT,
    AMZN,
    AVGO,
    AXP,
    BA,
    BAC,
    BK,
    BKNG,
    BLK,
    BMY,
    BRK_B,
    C,
    CAT,
    CHTR,
    CL,
    CMCSA,
    COF,
    COP,
    COST,
    CRM,
    CSCO,
    CVS,
    CVX,
    DE,
    DHR,
    DIS,
    DUK,
    EMR,
    FDX,
    GD,
    GE,
    GILD,
    GM,
    GOOG,
    GOOGL,
    GS,
    HD,
    HON,
    IBM,
    INTC,
    INTU,
    ISRG,
    MSFT;
    
    @Override
    public String toString() {
        return this.name().replace("_", ".");
    }
}