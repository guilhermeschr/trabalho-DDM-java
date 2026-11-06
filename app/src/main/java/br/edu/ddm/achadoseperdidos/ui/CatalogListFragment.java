package br.edu.ddm.achadoseperdidos.ui;

import android.os.Bundle;
import android.text.*;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import br.edu.ddm.achadoseperdidos.R;
import br.edu.ddm.achadoseperdidos.catalog.*;
import java.util.*;

public class CatalogListFragment extends Fragment {
    public CatalogListFragment() { super(R.layout.fragment_catalog_list); }
    public static CatalogListFragment create(boolean points) {
        CatalogListFragment fragment = new CatalogListFragment(); Bundle args = new Bundle(); args.putBoolean("points", points); fragment.setArguments(args); return fragment;
    }
    @Override public void onViewCreated(@NonNull View view, Bundle saved) {
        boolean points = requireArguments().getBoolean("points");
        HomeFragment home = (HomeFragment) requireParentFragment();
        CatalogViewModel model = home.catalogModel();
        if (model == null) return; // Sessão ainda sendo restaurada; a Activity exibirá a autenticação.
        ((TextView)view.findViewById(R.id.list_title)).setText(points ? R.string.points_title : R.string.objects_title);
        RecyclerView list = view.findViewById(R.id.catalog_list);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        CatalogAdapter adapter = new CatalogAdapter(id -> home.open(CatalogDetailFragment.create(points, id)));
        list.setAdapter(adapter);
        EditText search = view.findViewById(R.id.search_objects);
        Spinner filter = view.findViewById(R.id.point_filter);
        ((View) search.getParent().getParent()).setVisibility(points ? View.GONE : View.VISIBLE);
        view.findViewById(R.id.filter_label).setVisibility(points ? View.GONE : View.VISIBLE);
        filter.setVisibility(points ? View.GONE : View.VISIBLE);
        search.setText(model.query);
        Runnable renderRows = () -> {
            List<CatalogAdapter.Row> rows = new ArrayList<>();
            CatalogViewModel.State state = model.state().getValue();
            if (!state.loading && state.error.isEmpty()) {
                if (points) for (CollectionPoint p : state.points) rows.add(new CatalogAdapter.Row(p.id, p.name, p.address));
                else for (FoundObject o : model.filteredObjects()) {
                    CollectionPoint p = model.point(o.pointId);
                    rows.add(new CatalogAdapter.Row(o.id, o.name, o.location + "\n" + (p == null ? getString(R.string.point_unavailable) : p.name), o.thumbnail));
                }
            }
            adapter.submit(rows);
            TextView empty = view.findViewById(R.id.list_empty);
            empty.setText(points ? R.string.no_points : state.points.isEmpty() && model.canCreate() ? R.string.no_points_for_object : R.string.no_objects);
            empty.setVisibility(!state.loading && state.error.isEmpty() && rows.isEmpty() ? View.VISIBLE : View.GONE);
        };
        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) { model.query = s.toString(); renderRows.run(); }
            public void afterTextChanged(Editable value) {}
        });
        view.findViewById(R.id.refresh_catalog).setOnClickListener(v -> model.refresh());
        view.findViewById(R.id.create_object).setVisibility(!points && model.canCreate() ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.create_object).setOnClickListener(v -> { model.beginDraft(); home.open(new ObjectFormFragment()); });
        model.state().observe(getViewLifecycleOwner(), state -> {
            view.findViewById(R.id.list_progress).setVisibility(state.loading ? View.VISIBLE : View.GONE);
            ((TextView)view.findViewById(R.id.list_error)).setText(state.error);
            view.findViewById(R.id.refresh_catalog).setEnabled(!state.loading);
            view.findViewById(R.id.create_object).setEnabled(!state.loading && state.error.isEmpty() && !state.points.isEmpty());
            List<String> labels = new ArrayList<>(), ids = new ArrayList<>();
            labels.add(getString(R.string.all_points)); ids.add("");
            for (CollectionPoint p : state.points) { labels.add(p.name); ids.add(p.id); }
            filter.setOnItemSelectedListener(null);
            filter.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, labels));
            int selection = ids.indexOf(model.pointFilter);
            // Preservar o filtro enquanto carrega; remover apenas se o ponto realmente deixou de existir.
            if (!state.loading && state.error.isEmpty() && selection < 0) model.pointFilter = "";
            filter.setSelection(Math.max(0, selection));
            filter.setEnabled(!state.loading && state.error.isEmpty());
            filter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View selected, int position, long id) {
                    if (!model.state().getValue().loading && model.state().getValue().error.isEmpty()) { model.pointFilter = ids.get(position); renderRows.run(); }
                }
                public void onNothingSelected(AdapterView<?> parent) {}
            });
            renderRows.run();
        });
    }
}
