package br.edu.ddm.achadoseperdidos.catalog;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.*;

public final class FirebaseCatalogRepository implements CatalogRepository {
    private final FirebaseFirestore store;
    private final FirebaseAuth auth;
    public FirebaseCatalogRepository(FirebaseFirestore store, FirebaseAuth auth) { this.store = store; this.auth = auth; }
    @Override public void load(Callback<Data> callback) {
        Tasks.whenAllSuccess(store.collection("pontosColeta").get(Source.SERVER),
                store.collection("objetos").whereEqualTo("situacao", "DISPONIVEL").get(Source.SERVER))
                .addOnSuccessListener(results -> {
                    List<CollectionPoint> points = new ArrayList<>();
                    List<FoundObject> objects = new ArrayList<>();
                    try {
                        for (DocumentSnapshot doc : (QuerySnapshot) results.get(0)) {
                            points.add(new CollectionPoint(doc.getId(), text(doc, "nome"), text(doc, "endereco"), number(doc, "latitude"), number(doc, "longitude")));
                        }
                        for (DocumentSnapshot doc : (QuerySnapshot) results.get(1)) {
                            objects.add(new FoundObject(doc.getId(), text(doc, "nome"), text(doc, "descricao"), text(doc, "localAproximado"),
                                    text(doc, "pontoId"), number(doc, "latitude"), number(doc, "longitude"), photoIds(doc), doc.getString("miniaturaBase64")));
                        }
                        points.sort(Comparator.comparing(p -> p.name.toLowerCase(Locale.ROOT)));
                        objects.sort(Comparator.comparing(o -> o.name.toLowerCase(Locale.ROOT)));
                        callback.success(new Data(points, objects));
                    } catch (RuntimeException e) { callback.error("Há dados incompletos no cadastro. Contate o responsável pelo aplicativo."); }
                }).addOnFailureListener(e -> callback.error("Não foi possível carregar os dados. Verifique a conexão e tente novamente."));
    }
    @SuppressWarnings("unchecked") private List<String> photoIds(DocumentSnapshot doc) {
        Object ids = doc.get("fotos"); return ids instanceof List ? (List<String>) ids : Collections.emptyList();
    }
    @Override public void loadPhotos(String id, Callback<List<String>> callback) {
        store.collection("objetos").document(id).collection("fotos").orderBy(FieldPath.documentId()).get(Source.SERVER)
            .addOnSuccessListener(result -> { List<String> photos = new ArrayList<>();
                for (DocumentSnapshot doc : result) photos.add(doc.getString("base64")); callback.success(photos);
            }).addOnFailureListener(e -> callback.error("Não foi possível carregar as fotos. Tente novamente."));
    }
    private String text(DocumentSnapshot doc, String key) {
        String value = doc.getString(key);
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(key);
        return value;
    }
    private double number(DocumentSnapshot doc, String key) {
        Double value = doc.getDouble(key);
        if (value == null || !Double.isFinite(value) || Math.abs(value) > ("latitude".equals(key) ? 90 : 180)) throw new IllegalArgumentException(key);
        return value;
    }
    @Override public String newObjectId() { return store.collection("objetos").document().getId(); }
    @Override public void save(String id, ObjectDraft draft, Callback<String> callback) {
        if (auth.getCurrentUser() == null) { callback.error("Entre novamente para continuar."); return; }
        String uid = auth.getCurrentUser().getUid();
        DocumentReference reference = store.collection("objetos").document(id);
        Map<String,Object> data = new HashMap<>();
        data.put("nome", draft.name); data.put("descricao", draft.description); data.put("localAproximado", draft.location);
        data.put("latitude", draft.latitude); data.put("longitude", draft.longitude); data.put("pontoId", draft.pointId);
        data.put("autorUid", uid); data.put("situacao", "DISPONIVEL"); List<String> slots = new ArrayList<>(); for (int i=0; i<draft.photos.size(); i++) slots.add(String.valueOf(i)); data.put("fotos", slots);
        data.put("miniaturaBase64", draft.photos.isEmpty() ? "" : draft.photos.get(0).thumbnail);
        data.put("criadoEm", FieldValue.serverTimestamp());
        // Sem fila offline: a transação exige confirmação do servidor. O documento existente é uma repetição já concluída.
        store.runTransaction(transaction -> {
            DocumentSnapshot existing = transaction.get(reference);
            if (existing.exists()) {
                if (!uid.equals(existing.getString("autorUid"))) throw new IllegalStateException("Autor inválido");
            } else {
                transaction.set(reference, data);
                for (int i=0; i<draft.photos.size(); i++) {
                    Map<String,Object> photo = new HashMap<>(); photo.put("base64", draft.photos.get(i).base64); photo.put("tipo", "image/jpeg");
                    transaction.set(reference.collection("fotos").document(String.valueOf(i)), photo);
                }
            }
            return id;
        }).addOnSuccessListener(callback::success)
          .addOnFailureListener(e -> reference.get(Source.SERVER).addOnSuccessListener(existing -> {
              if (existing.exists() && uid.equals(existing.getString("autorUid"))) callback.success(id);
              else callback.error("Não foi possível salvar. Verifique sua conexão e tente novamente.");
          }).addOnFailureListener(readError -> callback.error("Confirmação pendente. Tente salvar novamente com os mesmos dados.")));
    }
}
