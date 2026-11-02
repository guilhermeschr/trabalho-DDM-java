package br.edu.ddm.achadoseperdidos.catalog;
public final class UnconfiguredCatalogRepository implements CatalogRepository {
    public void load(Callback<Data> callback) { callback.error("Firebase não configurado. Consulte o README."); }
    public String newObjectId() { return ""; }
    public void save(String id, ObjectDraft draft, Callback<String> callback) { callback.error("Firebase não configurado. Consulte o README."); }
}
