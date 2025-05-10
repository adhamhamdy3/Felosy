package felosy.assetmanagement;

public enum PropertyType {
    SINGLE_FAMILY_RESIDENTIAL,
    MULTI_FAMILY_RESIDENTIAL,
    OFFICE, 
    INDUSTRIAL, 
    RETAIL, 
    SELF_STORAGE, 
    LAND, 
    HOTELS_HOSPITALS, 
    MIXED_USE,
    OTHER;
    
    @Override
    public String toString() {
        return this.name();
    }
}
