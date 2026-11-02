package br.edu.ddm.achadoseperdidos.ui;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import br.edu.ddm.achadoseperdidos.R;
import br.edu.ddm.achadoseperdidos.catalog.*;
import java.util.*;

public class ObjectFormFragment extends Fragment {
    public ObjectFormFragment() { super(R.layout.fragment_object_form); }
    private String text(View view, int id) { return ((EditText)view.findViewById(id)).getText().toString(); }
    @Override public void onViewCreated(@NonNull View view, Bundle saved) {
        HomeFragment home = (HomeFragment) requireParentFragment();
        CatalogViewModel model = home.catalogModel();
        if (model == null) return; // Sessão ainda sendo restaurada; a Activity exibirá a autenticação.
        Spinner points = view.findViewById(R.id.object_point);
        view.findViewById(R.id.object_back).setOnClickListener(v -> home.handleBack());
        view.findViewById(R.id.object_reload).setOnClickListener(v -> model.refresh());
        view.findViewById(R.id.object_location).setOnClickListener(v -> MapPickerDialog.create(model.latitude, model.longitude, false).show(getChildFragmentManager(), "object_location"));
        getChildFragmentManager().setFragmentResultListener("location", getViewLifecycleOwner(), (key, result) -> {
            model.latitude = result.getDouble("lat"); model.longitude = result.getDouble("lng");
            ((TextView)view.findViewById(R.id.object_coordinates)).setText(getString(R.string.location_selected, model.latitude, model.longitude));
        });
        view.findViewById(R.id.object_save).setOnClickListener(v -> model.save(new ObjectDraft(text(view, R.id.object_name),
                text(view, R.id.object_description), text(view, R.id.object_place), model.draftPoint, model.latitude, model.longitude)));
        model.state().observe(getViewLifecycleOwner(), state -> {
            if (state.saved) { home.objectSaved(); return; }
            view.findViewById(R.id.object_progress).setVisibility(state.saving || state.loading ? View.VISIBLE : View.GONE);
            String message = !state.saveError.isEmpty() ? state.saveError : !state.error.isEmpty() ? state.error : state.points.isEmpty() && !state.loading ? getString(R.string.no_points_for_object) : "";
            ((TextView)view.findViewById(R.id.object_error)).setText(message);
            ((TextView)view.findViewById(R.id.object_coordinates)).setText(model.latitude == null ? "" : getString(R.string.location_selected, model.latitude, model.longitude));
            for (int id : new int[]{R.id.object_name, R.id.object_description, R.id.object_place, R.id.object_location, R.id.object_back, R.id.object_point}) view.findViewById(id).setEnabled(!state.saving);
            view.findViewById(R.id.object_save).setEnabled(model.canCreate() && !state.saving && !state.loading && state.error.isEmpty() && !state.points.isEmpty());
            view.findViewById(R.id.object_reload).setEnabled(!state.saving && !state.loading);
            List<String> labels = new ArrayList<>(), ids = new ArrayList<>(); labels.add(getString(R.string.select_point)); ids.add("");
            for (CollectionPoint p : state.points) { labels.add(p.name); ids.add(p.id); }
            points.setOnItemSelectedListener(null);
            points.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, labels));
            points.setSelection(Math.max(0, ids.indexOf(model.draftPoint)));
            points.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View selected, int position, long id) { model.draftPoint = ids.get(position); }
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        });
    }
}
