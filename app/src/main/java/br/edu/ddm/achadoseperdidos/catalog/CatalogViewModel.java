package br.edu.ddm.achadoseperdidos.catalog;

import androidx.lifecycle.*;
import br.edu.ddm.achadoseperdidos.data.UserProfile;
import java.util.*;

public final class CatalogViewModel extends ViewModel {
    public static final class State {
        public final List<CollectionPoint> points;
        public final List<FoundObject> objects;
        public final boolean loading, saving, saved;
        public final String error, saveError;
        State(List<CollectionPoint> points, List<FoundObject> objects, boolean loading, boolean saving, boolean saved, String error, String saveError) {
            this.points = Collections.unmodifiableList(new ArrayList<>(points)); this.objects = Collections.unmodifiableList(new ArrayList<>(objects));
            this.loading = loading; this.saving = saving; this.saved = saved; this.error = error; this.saveError = saveError;
        }
    }
    private final CatalogRepository repository;
    private final UserProfile profile;
    private final MutableLiveData<State> state = new MutableLiveData<>(new State(Collections.emptyList(), Collections.emptyList(), false, false, false, "", ""));
    private boolean started;
    private String pendingId;
    public String query = "", pointFilter = "", draftPoint = "";
    public Double latitude, longitude;
    public CatalogViewModel(CatalogRepository repository, UserProfile profile) { this.repository = repository; this.profile = profile; }
    public LiveData<State> state() { return state; }
    public boolean canCreate() { return "COMUM".equals(profile.role); }
    public void start() { if (!started) { started = true; refresh(); } }
    public void refresh() {
        State old = state.getValue();
        if (old.loading || old.saving) return;
        state.setValue(new State(old.points, old.objects, true, false, old.saved, "", old.saveError));
        repository.load(new CatalogRepository.Callback<CatalogRepository.Data>() {
            public void success(CatalogRepository.Data data) {
                State current = state.getValue();
                state.setValue(new State(data.points, data.objects, false, current.saving, current.saved, "", current.saveError));
            }
            public void error(String message) {
                State current = state.getValue();
                // Não apresentar dados antigos como uma lista vazia ou como consulta atualizada.
                state.setValue(new State(Collections.emptyList(), Collections.emptyList(), false, current.saving, current.saved, message, current.saveError));
            }
        });
    }
    public List<FoundObject> filteredObjects() {
        List<FoundObject> result = new ArrayList<>();
        String search = query.trim().toLowerCase(Locale.ROOT);
        for (FoundObject item : state.getValue().objects) {
            if (item.name.toLowerCase(Locale.ROOT).contains(search) && (pointFilter.isEmpty() || pointFilter.equals(item.pointId))) result.add(item);
        }
        return result;
    }
    public CollectionPoint point(String id) {
        for (CollectionPoint p : state.getValue().points) if (p.id.equals(id)) return p;
        return null;
    }
    public FoundObject object(String id) {
        for (FoundObject o : state.getValue().objects) if (o.id.equals(id)) return o;
        return null;
    }
    public void beginDraft() {
        if (state.getValue().saving) return;
        latitude = null; longitude = null; draftPoint = ""; pendingId = null;
        State old = state.getValue();
        state.setValue(new State(old.points, old.objects, old.loading, false, false, old.error, ""));
    }
    public void consumeSaved() {
        State old = state.getValue();
        state.setValue(new State(old.points, old.objects, old.loading, old.saving, false, old.error, old.saveError));
    }
    public void save(ObjectDraft draft) {
        State old = state.getValue();
        if (old.saving || old.saved) return;
        String error = !canCreate() ? "Somente usuários comuns podem cadastrar objetos." : draft.validate();
        if (error == null && (old.loading || !old.error.isEmpty() || point(draft.pointId) == null)) error = "Carregue e selecione um ponto de retirada existente.";
        if (error != null) { state.setValue(new State(old.points, old.objects, old.loading, false, false, old.error, error)); return; }
        if (pendingId == null) pendingId = repository.newObjectId();
        state.setValue(new State(old.points, old.objects, false, true, false, old.error, ""));
        repository.save(pendingId, draft, new CatalogRepository.Callback<String>() {
            public void success(String id) {
                State current = state.getValue();
                state.setValue(new State(current.points, current.objects, false, false, true, current.error, ""));
            }
            public void error(String message) {
                State current = state.getValue();
                state.setValue(new State(current.points, current.objects, false, false, false, current.error, message));
            }
        });
    }
}
