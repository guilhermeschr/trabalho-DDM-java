package br.edu.ddm.achadoseperdidos.delivery;
import java.util.*;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import br.edu.ddm.achadoseperdidos.catalog.*;
public final class FirebaseDeliveryRepository implements DeliveryRepository {
    private final FirebaseFirestore store;
    private final FirebaseAuth auth;
    public FirebaseDeliveryRepository(FirebaseFirestore store, FirebaseAuth auth) { this.store=store; this.auth=auth; }
    private String uid() { return auth.getCurrentUser()==null ? null : auth.getCurrentUser().getUid(); }
    @SuppressWarnings("unchecked") private FoundObject object(DocumentSnapshot d) {
        return new FoundObject(d.getId(), d.getString("nome"), d.getString("descricao"), d.getString("localAproximado"), d.getString("pontoId"), d.getDouble("latitude"), d.getDouble("longitude"),
            d.get("fotos") instanceof List ? (List<String>)d.get("fotos") : Collections.emptyList(), d.getString("miniaturaBase64"), d.getString("situacao"));
    }
    private List<String> photos(QuerySnapshot docs) {
        List<String> result=new ArrayList<>(); for(DocumentSnapshot d:docs) result.add(d.getString("base64")); return result;
    }
    @Override public void inventory(String status, CatalogRepository.Callback<List<FoundObject>> callback) {
        String user=uid(); if(user==null) { callback.error("Entre novamente para continuar."); return; }
        store.collection("objetos").whereEqualTo("pontoId",user).whereEqualTo("situacao",status).get(Source.SERVER)
            .addOnSuccessListener(docs -> { if(!user.equals(uid())) return;
                try { List<FoundObject> result=new ArrayList<>(); for(DocumentSnapshot d:docs) result.add(object(d)); result.sort(Comparator.comparing(o -> o.name.toLowerCase(Locale.ROOT))); callback.success(result); }
                catch(RuntimeException e) {callback.error("Inventário com dados incompletos.");}
            }).addOnFailureListener(e -> callback.error("Não foi possível carregar o inventário. Tente novamente."));
    }
    @Override public void detail(String id, CatalogRepository.Callback<Detail> callback) {
        String user=uid(); if(user==null) {callback.error("Entre novamente para continuar.");return;}
        DocumentReference ref=store.collection("objetos").document(id);
        ref.get(Source.SERVER).addOnSuccessListener(doc -> {
            if(!user.equals(uid())) return;
            if(!doc.exists() || !user.equals(doc.getString("pontoId"))) {callback.error("Objeto indisponível para este ponto.");return;}
            FoundObject item;
            try {item=object(doc);} catch(RuntimeException e) {callback.error("Objeto com dados incompletos.");return;}
            ref.collection("fotos").orderBy(FieldPath.documentId()).get(Source.SERVER).addOnSuccessListener(originals -> {
                if(!user.equals(uid())) return;
                if(!"ENTREGUE".equals(item.status)) {callback.success(new Detail(item,null,photos(originals),Collections.emptyList()));return;}
                DocumentReference receipt=store.collection("entregas").document(id);
                Tasks.whenAllSuccess(receipt.get(Source.SERVER),receipt.collection("fotos").orderBy(FieldPath.documentId()).get(Source.SERVER))
                    .addOnSuccessListener(results -> {
                        if(!user.equals(uid())) return;
                        try {DocumentSnapshot d=(DocumentSnapshot)results.get(0);
                            Delivery delivery=new Delivery(id,d.getString("pontoId"),d.getString("responsavelUid"),d.getString("descricao"),d.getString("operacaoId"),d.getDate("criadoEm"));
                            callback.success(new Detail(item,delivery,photos(originals),photos((QuerySnapshot)results.get(1))));
                        } catch(RuntimeException e) {callback.error("Comprovante com dados incompletos.");}
                    }).addOnFailureListener(e -> callback.error("Não foi possível carregar os comprovantes. Tente novamente."));
            }).addOnFailureListener(e -> callback.error("Não foi possível carregar as fotos. Tente novamente."));
        }).addOnFailureListener(e -> callback.error("Não foi possível consultar o objeto. Verifique sua conexão."));
    }
    @Override public void confirm(String id, String operation, DeliveryDraft draft, SaveCallback callback) {
        String user=uid(); if(user==null) {callback.failed("Entre novamente para continuar.",false);return;}
        String validation=draft.validate(); if(validation!=null) {callback.failed(validation,false);return;}
        DocumentReference item=store.collection("objetos").document(id), receipt=store.collection("entregas").document(id);
        store.runTransaction(tx -> {
            DocumentSnapshot obj=tx.get(item), existing=tx.get(receipt);
            if(!obj.exists() || !user.equals(obj.getString("pontoId"))) throw new IllegalStateException("Ponto inválido");
            if(existing.exists()) {
                if(!"ENTREGUE".equals(obj.getString("situacao"))) throw new IllegalStateException("Entrega inconsistente");
                return operation.equals(existing.getString("operacaoId")) ? Outcome.SAVED : Outcome.ALREADY_DELIVERED;
            }
            if(!"DISPONIVEL".equals(obj.getString("situacao"))) throw new IllegalStateException("Objeto indisponível");
            Map<String,Object> data=new HashMap<>(); List<String> slots=new ArrayList<>();
            for(int i=0;i<draft.photos.size();i++) slots.add(String.valueOf(i));
            data.put("objetoId",id);data.put("pontoId",user);data.put("responsavelUid",user);data.put("descricao",draft.description);
            data.put("operacaoId",operation);data.put("criadoEm",FieldValue.serverTimestamp());data.put("fotos",slots);
            tx.set(receipt,data);
            for(int i=0;i<draft.photos.size();i++) {Map<String,Object> photo=new HashMap<>();photo.put("tipo","image/jpeg");photo.put("base64",draft.photos.get(i).base64);tx.set(receipt.collection("fotos").document(String.valueOf(i)),photo);}
            tx.update(item,"situacao","ENTREGUE"); return Outcome.SAVED;
        }).addOnSuccessListener(outcome -> {if(user.equals(uid())) callback.confirmed(outcome);})
            .addOnFailureListener(error -> {
                if(error instanceof FirebaseFirestoreException && ((FirebaseFirestoreException)error).getCode()==FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                    if(user.equals(uid())) callback.failed("Este ponto não tem permissão para registrar a entrega.",false); return;
                }
                // Só resolver a ausência depois de terminar a transação; sem fila offline.
                receipt.get(Source.SERVER).addOnSuccessListener(d -> {
                    if(!user.equals(uid())) return;
                    if(d.exists()) callback.confirmed(operation.equals(d.getString("operacaoId")) ? Outcome.SAVED : Outcome.ALREADY_DELIVERED);
                    else callback.failed("Entrega não salva. Verifique a conexão ou a permissão e tente novamente.",false);
                }).addOnFailureListener(e -> {if(user.equals(uid())) callback.failed("Confirmação pendente. Tente novamente com os mesmos dados.",true);});
            });
    }
}
