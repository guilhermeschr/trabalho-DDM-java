package br.edu.ddm.achadoseperdidos.ui;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.*;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import br.edu.ddm.achadoseperdidos.App;
import br.edu.ddm.achadoseperdidos.R;
import br.edu.ddm.achadoseperdidos.auth.AuthViewModel;
import br.edu.ddm.achadoseperdidos.catalog.CatalogViewModel;
import br.edu.ddm.achadoseperdidos.data.UserProfile;

public class HomeFragment extends Fragment {
    private CatalogViewModel model;
    private br.edu.ddm.achadoseperdidos.delivery.DeliveryViewModel delivery;
    private int tab = R.id.nav_objects;
    public HomeFragment() { super(R.layout.fragment_catalog_home); }
    @Override public void onCreate(Bundle saved) { super.onCreate(saved); if (saved != null) tab = saved.getInt("tab", R.id.nav_objects); }
    @Override public void onViewCreated(@NonNull View view, Bundle saved) {
        UserProfile profile = new ViewModelProvider(requireActivity()).get(AuthViewModel.class).state().getValue().profile;
        if (profile == null) return;
        model = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> type) {
                return type.cast(new CatalogViewModel(((App) requireActivity().getApplication()).catalog(), profile));
            }
        }).get(CatalogViewModel.class);
        BottomNavigationView navigation = view.findViewById(R.id.bottom_navigation);
        boolean pointProfile = "PONTO_COLETA".equals(profile.role);
        boolean resetPointTab = pointProfile && tab != R.id.nav_inventory && tab != R.id.nav_account;
        if (resetPointTab) tab = R.id.nav_inventory;
        if (pointProfile) {
            navigation.getMenu().findItem(R.id.nav_objects).setVisible(false);
            navigation.getMenu().findItem(R.id.nav_points).setVisible(false);
            delivery = new ViewModelProvider(this, new ViewModelProvider.Factory() {
                @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> type) {
                    return type.cast(new br.edu.ddm.achadoseperdidos.delivery.DeliveryViewModel(((App) requireActivity().getApplication()).deliveries(), profile));
                }
            }).get(br.edu.ddm.achadoseperdidos.delivery.DeliveryViewModel.class);
            delivery.state().observe(getViewLifecycleOwner(), state -> {
                for(int i=0;i<navigation.getMenu().size();i++) navigation.getMenu().getItem(i).setEnabled(!delivery.blocked() && !model.state().getValue().saving && !model.publicationPending());
            });
        } else navigation.getMenu().findItem(R.id.nav_inventory).setVisible(false);
        navigation.setSelectedItemId(tab);
        androidx.core.view.ViewCompat.requestApplyInsets(requireActivity().findViewById(R.id.container));
        navigation.setOnItemSelectedListener(item -> {
            if (!item.isVisible()) return false;
            if ((delivery != null && delivery.blocked()) || model.state().getValue().saving || model.publicationPending() || Boolean.TRUE.equals(model.preparingPhoto.getValue())) return false;
            tab = item.getItemId();
            getChildFragmentManager().popBackStackImmediate(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
            Fragment root = tab == R.id.nav_account ? new AccountFragment() : tab == R.id.nav_inventory ? new PointInventoryFragment() : CatalogListFragment.create(tab == R.id.nav_points);
            getChildFragmentManager().beginTransaction().replace(R.id.catalog_container, root).commit();
            if (tab != R.id.nav_account && !pointProfile) model.refresh();
            return true;
        });
        if (getChildFragmentManager().findFragmentById(R.id.catalog_container) == null || resetPointTab) {
            if (resetPointTab) getChildFragmentManager().popBackStackImmediate(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
            Fragment root = tab == R.id.nav_account ? new AccountFragment() : tab == R.id.nav_inventory ? new PointInventoryFragment() : CatalogListFragment.create(tab == R.id.nav_points);
            getChildFragmentManager().beginTransaction().replace(R.id.catalog_container, root).commit();
        }
        model.state().observe(getViewLifecycleOwner(), state -> {
            for (int i = 0; i < navigation.getMenu().size(); i++) navigation.getMenu().getItem(i).setEnabled(!state.saving && !model.publicationPending() && (delivery == null || !delivery.blocked()));
        });
        if (!pointProfile) model.start();
    }
    public br.edu.ddm.achadoseperdidos.delivery.DeliveryViewModel deliveryModel() { return delivery; }
    public CatalogViewModel catalogModel() { return model; }
    public boolean handleBack() {
        if (delivery != null && delivery.blocked()) return true;
        if (model != null && (model.state().getValue().saving || model.publicationPending() || Boolean.TRUE.equals(model.preparingPhoto.getValue()))) return true;
        if (getChildFragmentManager().getBackStackEntryCount() > 0) { getChildFragmentManager().popBackStack(); return true; }
        return false;
    }
    public void open(Fragment fragment) {
        getChildFragmentManager().beginTransaction().replace(R.id.catalog_container, fragment).addToBackStack(null).commit();
    }
    public void objectSaved() {
        model.consumeSaved();
        getChildFragmentManager().popBackStack();
        android.widget.Toast.makeText(requireContext(), R.string.object_saved, android.widget.Toast.LENGTH_SHORT).show();
        model.refresh();
    }
    @Override public void onSaveInstanceState(@NonNull Bundle out) { super.onSaveInstanceState(out); out.putInt("tab", tab); }
}
