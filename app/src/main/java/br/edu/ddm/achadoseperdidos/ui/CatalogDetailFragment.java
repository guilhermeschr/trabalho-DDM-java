package br.edu.ddm.achadoseperdidos.ui;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import br.edu.ddm.achadoseperdidos.R;
import br.edu.ddm.achadoseperdidos.catalog.*;

public class CatalogDetailFragment extends Fragment {
    public CatalogDetailFragment() { super(R.layout.fragment_catalog_detail); }
    public static CatalogDetailFragment create(boolean point, String id) {
        CatalogDetailFragment fragment = new CatalogDetailFragment(); Bundle args = new Bundle(); args.putBoolean("point", point); args.putString("id", id); fragment.setArguments(args); return fragment;
    }
    private String loadedPhotos;
    private void showPhotos(View view, CatalogViewModel model, FoundObject object) {
        if (object.photos.isEmpty() || object.id.equals(loadedPhotos)) return;
        loadedPhotos = object.id;
        android.widget.LinearLayout area = new android.widget.LinearLayout(requireContext()); area.setOrientation(android.widget.LinearLayout.VERTICAL);
        ((android.view.ViewGroup)view.findViewById(R.id.detail_content)).addView(area, 0);
        Runnable[] load = new Runnable[1];
        load[0] = () -> {
            area.removeAllViews(); TextView status = new TextView(requireContext()); status.setText("Carregando fotos…"); area.addView(status);
            model.loadPhotos(object.id, new CatalogRepository.Callback<java.util.List<String>>() {
                public void success(java.util.List<String> photos) {
                    if (getView() != view) return; area.removeAllViews();
                    android.widget.HorizontalScrollView scroll = new android.widget.HorizontalScrollView(requireContext()); android.widget.LinearLayout row = new android.widget.LinearLayout(requireContext()); scroll.addView(row); area.addView(scroll);
                    for (String photo : photos) { android.widget.ImageView image = new android.widget.ImageView(requireContext()); image.setContentDescription("Foto do objeto. Toque para ampliar"); row.addView(image, new android.widget.LinearLayout.LayoutParams(280,280)); PhotoImages.bind(image, photo);
                        image.setOnClickListener(v -> { android.widget.ImageView full = new android.widget.ImageView(requireContext()); full.setAdjustViewBounds(true); PhotoImages.bind(full, photo); new androidx.appcompat.app.AlertDialog.Builder(requireContext()).setView(full).setPositiveButton("Fechar", null).show(); }); }
                }
                public void error(String message) { if (getView() != view) return; area.removeAllViews(); android.widget.Button retry = new android.widget.Button(requireContext()); retry.setText(message + " Tentar novamente"); retry.setOnClickListener(v -> load[0].run()); area.addView(retry); }
            });
        }; load[0].run();
    }
    @Override public void onDestroyView() { loadedPhotos = null; super.onDestroyView(); }
    @Override public void onViewCreated(@NonNull View view, Bundle saved) {
        HomeFragment home = (HomeFragment) requireParentFragment();
        CatalogViewModel model = home.catalogModel();
        if (model == null) return; // Sessão ainda sendo restaurada; a Activity exibirá a autenticação.
        view.findViewById(R.id.detail_back).setOnClickListener(v -> home.handleBack());
        view.findViewById(R.id.detail_retry).setOnClickListener(v -> model.refresh());
        model.state().observe(getViewLifecycleOwner(), state -> {
            boolean isPoint = requireArguments().getBoolean("point");
            String id = requireArguments().getString("id", "");
            CollectionPoint point = isPoint ? model.point(id) : null;
            FoundObject object = isPoint ? null : model.object(id);
            boolean exists = point != null || object != null;
            ((TextView)view.findViewById(R.id.detail_title)).setText(isPoint ? R.string.point_details : R.string.object_details);
            ((TextView)view.findViewById(R.id.detail_error)).setText(state.loading ? getString(R.string.loading_data)
                    : !state.error.isEmpty() ? state.error : !exists ? getString(R.string.item_unavailable) : "");
            view.findViewById(R.id.detail_content).setVisibility(exists && !state.loading && state.error.isEmpty() ? View.VISIBLE : View.GONE);
            view.findViewById(R.id.detail_retry).setVisibility(!state.error.isEmpty() || !exists ? View.VISIBLE : View.GONE);
            view.findViewById(R.id.detail_retry).setEnabled(!state.loading);
            if (!exists) return;
            ((TextView)view.findViewById(R.id.detail_name)).setText(isPoint ? point.name : object.name);
            ((TextView)view.findViewById(R.id.detail_description)).setText(isPoint ? point.address : object.description);
            double lat = isPoint ? point.latitude : object.latitude, lng = isPoint ? point.longitude : object.longitude;
            String location = isPoint ? getString(R.string.point_address) : getString(R.string.found_at, object.location);
            ((TextView)view.findViewById(R.id.detail_location)).setText(location + "\n" + getString(R.string.location_selected, lat, lng));
            view.findViewById(R.id.detail_map).setOnClickListener(v -> MapPickerDialog.create(lat, lng, true).show(getChildFragmentManager(), "map_detail"));
            view.findViewById(R.id.detail_point).setVisibility(isPoint ? View.GONE : View.VISIBLE);
            if (!isPoint) {
                showPhotos(view, model, object);
                CollectionPoint linked = model.point(object.pointId);
                ((TextView)view.findViewById(R.id.detail_point)).setText(linked == null ? getString(R.string.point_unavailable) : getString(R.string.pickup_point, linked.name));
                view.findViewById(R.id.detail_point).setEnabled(linked != null);
                view.findViewById(R.id.detail_point).setOnClickListener(v -> home.open(create(true, object.pointId)));
            }
        });
    }
}
