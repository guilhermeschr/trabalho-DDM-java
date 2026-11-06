package br.edu.ddm.achadoseperdidos.delivery;
import java.util.*;
import br.edu.ddm.achadoseperdidos.catalog.ObjectPhoto;
public final class DeliveryDraft {
    public final String description;
    public final List<ObjectPhoto> photos;
    public DeliveryDraft(String description, List<ObjectPhoto> photos) {
        this.description = description.trim(); this.photos = Collections.unmodifiableList(new ArrayList<>(photos));
    }
    public String validate() {
        if (description.isEmpty()) return "Descreva a retirada do objeto.";
        if (photos.isEmpty() || photos.size()>5) return "Selecione de uma a cinco fotos do comprovante.";
        for (ObjectPhoto photo : photos) if (photo == null || !photo.valid()) return "Comprovante inválido ou acima do limite.";
        return null;
    }
}
