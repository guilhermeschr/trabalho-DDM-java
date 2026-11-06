package br.edu.ddm.achadoseperdidos.catalog;
import java.util.List;

public interface CatalogRepository {
    interface Callback<T> { void success(T value); void error(String message); }
    final class Data {
        public final List<CollectionPoint> points;
        public final List<FoundObject> objects;
        public Data(List<CollectionPoint> points, List<FoundObject> objects) { this.points = points; this.objects = objects; }
    }
    void load(Callback<Data> callback);
    default void loadPhotos(String id, Callback<List<String>> callback) { callback.error("Fotos indisponíveis."); }
    // O ID é reutilizado após falha de rede para não criar dois anúncios na repetição.
    String newObjectId();
    void save(String id, ObjectDraft draft, Callback<String> callback);
}
