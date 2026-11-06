package br.edu.ddm.achadoseperdidos.delivery;
import java.util.*;
import br.edu.ddm.achadoseperdidos.catalog.*;
public interface DeliveryRepository {
    enum Outcome { SAVED, ALREADY_DELIVERED }
    interface SaveCallback { void confirmed(Outcome outcome); void failed(String message, boolean uncertain); }
    final class Detail {
        public final FoundObject object;
        public final Delivery receipt;
        public final List<String> objectPhotos, receiptPhotos;
        public Detail(FoundObject object, Delivery receipt, List<String> objectPhotos, List<String> receiptPhotos) {
            this.object=object; this.receipt=receipt;
            this.objectPhotos=Collections.unmodifiableList(new ArrayList<>(objectPhotos));
            this.receiptPhotos=Collections.unmodifiableList(new ArrayList<>(receiptPhotos));
        }
    }
    void inventory(String status, CatalogRepository.Callback<List<FoundObject>> callback);
    void detail(String objectId, CatalogRepository.Callback<Detail> callback);
    void confirm(String objectId, String operationId, DeliveryDraft draft, SaveCallback callback);
}
