package br.edu.ddm.achadoseperdidos.ui;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import br.edu.ddm.achadoseperdidos.R;
import br.edu.ddm.achadoseperdidos.catalog.FoundObject;
import br.edu.ddm.achadoseperdidos.delivery.DeliveryViewModel;
import java.util.*;
public class PointInventoryFragment extends Fragment {
    public PointInventoryFragment(){super(R.layout.fragment_point_inventory);}
    @Override public void onViewCreated(@NonNull View view,Bundle saved){
        HomeFragment home=(HomeFragment)requireParentFragment();DeliveryViewModel model=home.deliveryModel();if(model==null)return;
        RecyclerView list=view.findViewById(R.id.inventory_list);list.setLayoutManager(new LinearLayoutManager(requireContext()));
        CatalogAdapter adapter=new CatalogAdapter(id -> home.open(PointObjectFragment.create(id,false)));list.setAdapter(adapter);
        view.findViewById(R.id.inventory_available).setOnClickListener(v -> model.inventory("DISPONIVEL"));
        view.findViewById(R.id.inventory_delivered).setOnClickListener(v -> model.inventory("ENTREGUE"));
        view.findViewById(R.id.inventory_refresh).setOnClickListener(v -> model.inventory(model.state().getValue().status));
        model.state().observe(getViewLifecycleOwner(),s -> {
            view.findViewById(R.id.inventory_progress).setVisibility(s.loading?View.VISIBLE:View.GONE);
            ((TextView)view.findViewById(R.id.inventory_message)).setText(!s.error.isEmpty()?s.error:s.loading?"Carregando…":s.items.isEmpty()?"Nenhum objeto nesta situação.":"DISPONIVEL".equals(s.status)?"Disponíveis":"Entregues");
            List<CatalogAdapter.Row> rows=new ArrayList<>();if(!s.loading && s.error.isEmpty())for(FoundObject o:s.items)rows.add(new CatalogAdapter.Row(o.id,o.name,o.location,o.thumbnail));adapter.submit(rows);
            view.findViewById(R.id.inventory_available).setEnabled(!s.loading && !s.locked() && !"DISPONIVEL".equals(s.status));
            view.findViewById(R.id.inventory_delivered).setEnabled(!s.loading && !s.locked() && !"ENTREGUE".equals(s.status));
            view.findViewById(R.id.inventory_refresh).setEnabled(!s.loading && !s.locked());
        });
    }
    @Override public void onResume(){super.onResume();HomeFragment home=(HomeFragment)getParentFragment();if(home!=null && home.deliveryModel()!=null)home.deliveryModel().inventory(home.deliveryModel().state().getValue().status);}
}
