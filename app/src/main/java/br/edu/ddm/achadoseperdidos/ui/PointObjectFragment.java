package br.edu.ddm.achadoseperdidos.ui;
import android.os.*;
import android.net.Uri;
import android.view.View;
import android.widget.*;
import android.text.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.activity.result.*;
import androidx.activity.result.contract.ActivityResultContracts;
import br.edu.ddm.achadoseperdidos.R;
import br.edu.ddm.achadoseperdidos.catalog.*;
import br.edu.ddm.achadoseperdidos.delivery.*;
import java.util.*;
public class PointObjectFragment extends Fragment {
    private DeliveryViewModel model;
    private Object rendered;
    private boolean autoBegin;
    public PointObjectFragment(){super(R.layout.fragment_point_object);}
    public static PointObjectFragment create(String id,boolean edit){PointObjectFragment f=new PointObjectFragment();Bundle b=new Bundle();b.putString("id",id);b.putBoolean("edit",edit);f.setArguments(b);return f;}
    private final ActivityResultLauncher<PickVisualMediaRequest> gallery=registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(5),uris -> {
        if(model==null || uris.isEmpty() || !model.beginPhotos(uris.size()))return;
        DeliveryViewModel target=model;android.content.Context context=requireContext().getApplicationContext();
        PhotoImages.WORK.execute(() -> {
            List<ObjectPhoto> photos=new ArrayList<>();String error="";
            try{for(Uri uri:uris)photos.add(PhotoImages.prepare(context,uri));}catch(Exception | OutOfMemoryError e){error="Não foi possível preparar a imagem. Escolha outra foto.";}
            String message=error;new Handler(Looper.getMainLooper()).post(() -> target.finishPhotos(photos,message));
        });
    });
    private void images(LinearLayout area,List<String> photos){
        area.removeAllViews();if(photos.isEmpty())return;
        HorizontalScrollView scroll=new HorizontalScrollView(requireContext());LinearLayout row=new LinearLayout(requireContext());scroll.addView(row);area.addView(scroll);
        int size=Math.round(160*getResources().getDisplayMetrics().density);
        for(String photo:photos){ImageView image=new ImageView(requireContext());image.setContentDescription("Foto. Toque para ampliar");row.addView(image,new LinearLayout.LayoutParams(size,size));PhotoImages.bind(image,photo);
            image.setOnClickListener(v -> {ImageView full=new ImageView(requireContext());full.setAdjustViewBounds(true);PhotoImages.bind(full,photo);new androidx.appcompat.app.AlertDialog.Builder(requireContext()).setView(full).setPositiveButton("Fechar",null).show();});}
    }
    @Override public void onViewCreated(@NonNull View view,Bundle saved){
        HomeFragment home=(HomeFragment)requireParentFragment();model=home.deliveryModel();if(model==null)return;
        String id=requireArguments().getString("id");autoBegin=saved==null && requireArguments().getBoolean("edit");
        EditText description=view.findViewById(R.id.delivery_description);description.setText(model.description());
        description.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int start,int count,int after){}public void onTextChanged(CharSequence s,int start,int before,int count){model.description(s.toString());}public void afterTextChanged(Editable s){}});
        view.findViewById(R.id.delivery_pick).setOnClickListener(v -> gallery.launch(new PickVisualMediaRequest.Builder().setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build()));
        view.findViewById(R.id.delivery_begin).setOnClickListener(v -> model.begin());
        view.findViewById(R.id.delivery_confirm).setOnClickListener(v -> model.save());
        view.findViewById(R.id.delivery_retry).setOnClickListener(v -> model.open(id));
        view.findViewById(R.id.delivery_back).setOnClickListener(v -> {if(!model.blocked()){model.abandon();home.handleBack();}});
        model.state().observe(getViewLifecycleOwner(),s -> {
            view.findViewById(R.id.delivery_progress).setVisibility(s.loading||s.saving||s.preparing?View.VISIBLE:View.GONE);
            ((TextView)view.findViewById(R.id.delivery_message)).setText(!s.error.isEmpty()?s.error:s.notice);
            view.findViewById(R.id.delivery_retry).setVisibility(!s.error.isEmpty() && s.detail==null?View.VISIBLE:View.GONE);
            view.findViewById(R.id.delivery_retry).setEnabled(!s.loading && !s.locked());
            view.findViewById(R.id.delivery_content).setVisibility(s.detail==null||s.loading?View.GONE:View.VISIBLE);
            view.findViewById(R.id.delivery_back).setEnabled(!s.locked());
            if(s.detail==null)return;
            FoundObject o=s.detail.object;
            ((TextView)view.findViewById(R.id.delivery_object)).setText(o.name+"\n"+o.description+"\nEncontrado em: "+o.location+"\nSituação: "+o.status);
            view.findViewById(R.id.delivery_map).setOnClickListener(v -> MapPickerDialog.create(o.latitude,o.longitude,true).show(getChildFragmentManager(),"point_object_map"));
            if(rendered!=s.detail){rendered=s.detail;images(view.findViewById(R.id.delivery_original_photos),s.detail.objectPhotos);images(view.findViewById(R.id.delivery_receipt_photos),s.detail.receiptPhotos);}
            Delivery receipt=s.detail.receipt;
            ((TextView)view.findViewById(R.id.delivery_receipt)).setText(receipt==null?"":"Comprovante privado\n"+receipt.description+"\n"+java.text.DateFormat.getDateTimeInstance().format(receipt.createdAt));
            view.findViewById(R.id.delivery_begin).setVisibility(receipt==null && !s.editing?View.VISIBLE:View.GONE);
            view.findViewById(R.id.delivery_begin).setEnabled("DISPONIVEL".equals(o.status));
            view.findViewById(R.id.delivery_form).setVisibility(s.editing?View.VISIBLE:View.GONE);description.setEnabled(!s.locked());
            view.findViewById(R.id.delivery_pick).setEnabled(!s.locked() && s.photos.size()<5);
            view.findViewById(R.id.delivery_confirm).setEnabled(!s.saving && !s.preparing);
            ((TextView)view.findViewById(R.id.delivery_count)).setText("Comprovantes: "+s.photos.size()+"/5");
            LinearLayout previews=view.findViewById(R.id.delivery_previews);previews.removeAllViews();
            for(ObjectPhoto photo:s.photos){ImageView image=new ImageView(requireContext());image.setContentDescription("Prévia do comprovante");previews.addView(image,new LinearLayout.LayoutParams(-1,240));PhotoImages.bind(image,photo.thumbnail);
                Button remove=new Button(requireContext());remove.setText("Remover foto");remove.setEnabled(!s.locked());previews.addView(remove);remove.setOnClickListener(v -> model.remove(photo));}
            if(autoBegin && receipt==null && id.equals(o.id)){autoBegin=false;model.begin();}
        });
        DeliveryViewModel.State current=model.state().getValue();
        if(saved==null || current.detail==null || !id.equals(current.detail.object.id))model.open(id);
    }
    @Override public void onDestroyView(){rendered=null;super.onDestroyView();}
}
