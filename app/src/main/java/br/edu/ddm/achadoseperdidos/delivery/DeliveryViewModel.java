package br.edu.ddm.achadoseperdidos.delivery;
import androidx.lifecycle.*;
import java.util.*;
import br.edu.ddm.achadoseperdidos.catalog.*;
import br.edu.ddm.achadoseperdidos.data.UserProfile;
public final class DeliveryViewModel extends ViewModel {
    public static final class State {
        public final List<FoundObject> items;
        public final List<ObjectPhoto> photos;
        public final DeliveryRepository.Detail detail;
        public final boolean loading, saving, preparing, editing, pending;
        public final String error, notice, status;
        State(DeliveryViewModel m) {
            items=Collections.unmodifiableList(new ArrayList<>(m.items)); photos=Collections.unmodifiableList(new ArrayList<>(m.photos)); detail=m.detail;
            loading=m.loading;saving=m.saving;preparing=m.preparing;editing=m.editing;pending=m.snapshot!=null;error=m.error;notice=m.notice;status=m.status;
        }
        public boolean locked() { return saving || preparing || pending; }
    }
    private final DeliveryRepository repository;
    private final UserProfile profile;
    private final MutableLiveData<State> state=new MutableLiveData<>();
    private List<FoundObject> items=new ArrayList<>();
    private final List<ObjectPhoto> photos=new ArrayList<>();
    private DeliveryRepository.Detail detail;
    private String status="DISPONIVEL", selected="", description="", operation, error="", notice="";
    private boolean loading, saving, preparing, editing, alive=true;
    private int generation;
    private DeliveryDraft snapshot;
    public DeliveryViewModel(DeliveryRepository repository, UserProfile profile) {this.repository=repository;this.profile=profile;publish();}
    public LiveData<State> state() {return state;}
    private void publish() {if(alive) state.setValue(new State(this));}
    public boolean blocked() {return saving || preparing || snapshot!=null;}
    private boolean allowed() {return "PONTO_COLETA".equals(profile.role);}
    public boolean owns(String pointId) { return allowed() && profile.uid.equals(pointId); }
    public String description() {return description;}
    public void description(String value) {if(!blocked()) description=value;}
    public void inventory(String value) {
        if(blocked()) return;
        int request=++generation; selected=""; detail=null; editing=false;photos.clear();description="";
        status=value;loading=true;error="";notice="";items=new ArrayList<>();publish();
        if(!allowed()) {loading=false;error="Somente pontos de coleta podem consultar o inventário.";publish();return;}
        repository.inventory(status,new CatalogRepository.Callback<List<FoundObject>>() {
            public void success(List<FoundObject> result) {if(!alive || request!=generation)return;items=result;loading=false;publish();}
            public void error(String message) {if(!alive || request!=generation)return;error=message;loading=false;publish();}
        });
    }
    public void open(String id) {
        if(blocked()) return;
        int request=++generation;selected=id;detail=null;editing=false;photos.clear();description="";error="";notice="";loading=true;publish();
        if(!allowed()) {loading=false;error="Acesso exclusivo do ponto responsável.";publish();return;}
        repository.detail(id,new CatalogRepository.Callback<DeliveryRepository.Detail>() {
            public void success(DeliveryRepository.Detail value) {if(!alive || request!=generation)return;loading=false;
                if(!profile.uid.equals(value.object.pointId)) error="Objeto de outro ponto.";else detail=value;publish();}
            public void error(String message) {if(!alive || request!=generation)return;loading=false;error=message;publish();}
        });
    }
    public void begin() {if(blocked() || detail==null || detail.receipt!=null || !"DISPONIVEL".equals(detail.object.status))return;editing=true;operation=UUID.randomUUID().toString();error="";publish();}
    public boolean beginPhotos(int count) {
        if(blocked() || !editing)return false;
        if(photos.size()+count>5) {error="Selecione no máximo cinco fotos.";publish();return false;}
        preparing=true;error="";publish();return true;
    }
    public void finishPhotos(List<ObjectPhoto> result, String message) {
        if(!alive)return;
        preparing=false;if(message.isEmpty())photos.addAll(result);error=message;publish();
    }
    public void remove(ObjectPhoto photo) {if(!blocked()){photos.remove(photo);publish();}}
    public void abandon() {if(blocked())return;generation++;photos.clear();description="";snapshot=null;editing=false;detail=null;selected="";notice="";publish();}
    public void save() {
        if(saving || preparing || !editing || detail==null || !allowed())return;
        DeliveryDraft draft=snapshot==null ? new DeliveryDraft(description,photos) : snapshot;
        String invalid=draft.validate();if(invalid!=null){error=invalid;publish();return;}
        if(!profile.uid.equals(detail.object.pointId)){error="Objeto de outro ponto.";publish();return;}
        if(snapshot==null)snapshot=draft;
        saving=true;error="";publish();
        repository.confirm(selected,operation,snapshot,new DeliveryRepository.SaveCallback() {
            public void confirmed(DeliveryRepository.Outcome outcome) {
                if(!alive)return;saving=false;snapshot=null;photos.clear();description="";editing=false;
                String id=selected;open(id);notice=outcome==DeliveryRepository.Outcome.SAVED ? "Entrega registrada." : "Este objeto já foi entregue em outra sessão. Comprovante preservado.";publish();
            }
            public void failed(String message,boolean uncertain) {if(!alive)return;saving=false;if(!uncertain)snapshot=null;error=message;publish();}
        });
    }
    @Override protected void onCleared() {alive=false;generation++;photos.clear();snapshot=null;detail=null;}
}
