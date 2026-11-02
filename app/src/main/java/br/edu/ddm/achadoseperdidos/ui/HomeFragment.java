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
        navigation.setSelectedItemId(tab);
        navigation.setOnItemSelectedListener(item -> {
            if (model.state().getValue().saving) return false;
            tab = item.getItemId();
            getChildFragmentManager().popBackStackImmediate(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
            Fragment root = tab == R.id.nav_account ? new AccountFragment() : CatalogListFragment.create(tab == R.id.nav_points);
            getChildFragmentManager().beginTransaction().replace(R.id.catalog_container, root).commit();
            if (tab != R.id.nav_account) model.refresh();
            return true;
        });
        if (getChildFragmentManager().findFragmentById(R.id.catalog_container) == null) {
            Fragment root = tab == R.id.nav_account ? new AccountFragment() : CatalogListFragment.create(tab == R.id.nav_points);
            getChildFragmentManager().beginTransaction().replace(R.id.catalog_container, root).commit();
        }
        model.state().observe(getViewLifecycleOwner(), state -> {
            for (int i = 0; i < navigation.getMenu().size(); i++) navigation.getMenu().getItem(i).setEnabled(!state.saving);
        });
        model.start();
    }
    public CatalogViewModel catalogModel() { return model; }
    public boolean handleBack() {
        if (model != null && model.state().getValue().saving) return true;
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
