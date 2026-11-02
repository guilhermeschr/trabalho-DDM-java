package br.edu.ddm.achadoseperdidos.catalog;

public final class ObjectDraft {
    public final String name, description, location, pointId;
    public final Double latitude, longitude;
    public ObjectDraft(String name, String description, String location, String pointId, Double latitude, Double longitude) {
        this.name = name.trim(); this.description = description.trim(); this.location = location.trim();
        this.pointId = pointId; this.latitude = latitude; this.longitude = longitude;
    }
    public String validate() {
        if (name.isEmpty()) return "Informe o nome do objeto.";
        if (description.isEmpty()) return "Informe a descrição do objeto.";
        if (location.isEmpty()) return "Informe onde o objeto foi encontrado.";
        if (latitude == null || longitude == null || !Double.isFinite(latitude) || !Double.isFinite(longitude)
                || Math.abs(latitude) > 90 || Math.abs(longitude) > 180) return "Selecione o local encontrado no mapa.";
        if (pointId == null || pointId.isEmpty()) return "Selecione um ponto de retirada.";
        return null;
    }
}
