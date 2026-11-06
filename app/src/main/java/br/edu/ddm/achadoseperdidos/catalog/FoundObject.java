package br.edu.ddm.achadoseperdidos.catalog;

public final class FoundObject {
    public final String id, name, description, location, pointId;
    public final double latitude, longitude;
    public final java.util.List<String> photos;
    public final String thumbnail, status;
    public FoundObject(String id, String name, String description, String location,
                       String pointId, double latitude, double longitude) {
        this(id, name, description, location, pointId, latitude, longitude, java.util.Collections.emptyList(), "");
    }
    public FoundObject(String id, String name, String description, String location, String pointId, double latitude, double longitude, java.util.List<String> photos, String thumbnail) {
        this(id,name,description,location,pointId,latitude,longitude,photos,thumbnail,"DISPONIVEL");
    }
    public FoundObject(String id, String name, String description, String location, String pointId, double latitude, double longitude, java.util.List<String> photos, String thumbnail, String status) {
        this.status = status;
        this.photos = java.util.Collections.unmodifiableList(new java.util.ArrayList<>(photos)); this.thumbnail = thumbnail == null ? "" : thumbnail;
        this.id = id; this.name = name; this.description = description;
        this.location = location; this.pointId = pointId;
        this.latitude = latitude; this.longitude = longitude;
    }
}
