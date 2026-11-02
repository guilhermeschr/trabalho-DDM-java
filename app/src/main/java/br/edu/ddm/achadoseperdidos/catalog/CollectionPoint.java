package br.edu.ddm.achadoseperdidos.catalog;

public final class CollectionPoint {
    public final String id, name, address;
    public final double latitude, longitude;
    public CollectionPoint(String id, String name, String address, double latitude, double longitude) {
        this.id = id; this.name = name; this.address = address;
        this.latitude = latitude; this.longitude = longitude;
    }
    @Override public String toString() { return name; }
}
