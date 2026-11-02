package br.edu.ddm.achadoseperdidos.catalog;

public final class FoundObject {
    public final String id, name, description, location, pointId;
    public final double latitude, longitude;
    public FoundObject(String id, String name, String description, String location,
                       String pointId, double latitude, double longitude) {
        this.id = id; this.name = name; this.description = description;
        this.location = location; this.pointId = pointId;
        this.latitude = latitude; this.longitude = longitude;
    }
}
