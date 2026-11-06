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
    private CatalogViewModel photoModel;
    private android.widget.LinearLayout photoPanel;
    private final androidx.activity.result.ActivityResultLauncher<android.net.Uri> camera = registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.TakePicture(), ok -> {
        if (photoModel == null || photoModel.cameraPath == null) return;
        java.io.File file = new java.io.File(photoModel.cameraPath); photoModel.cameraPath = null;
        if (ok) prepare(java.util.Collections.singletonList(android.net.Uri.fromFile(file)), file); else file.delete();
    });
    private final androidx.activity.result.ActivityResultLauncher<androidx.activity.result.PickVisualMediaRequest> gallery = registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia(5), uris -> prepare(uris, null));
    private void prepare(java.util.List<android.net.Uri> uris, java.io.File temporary) {
        if (uris.isEmpty()) return;
        CatalogViewModel model = photoModel;
        if (model.photos.size()+uris.size()>5) { model.photoError.setValue("Selecione no máximo cinco fotos."); return; }
        model.preparingPhoto.setValue(true);
        android.content.Context context = requireContext().getApplicationContext();
        PhotoImages.WORK.execute(() -> {
            java.util.List<ObjectPhoto> prepared = new java.util.ArrayList<>(); String error = "";
            try { for (android.net.Uri uri : uris) prepared.add(PhotoImages.prepare(context, uri)); }
            catch (Exception | OutOfMemoryError e) { error = "Não foi possível preparar a imagem. Escolha outra foto."; }
            finally { if (temporary != null) temporary.delete(); }
            String message = error;
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> { if (message.isEmpty()) model.photos.addAll(prepared); model.photoError.setValue(message); model.preparingPhoto.setValue(false); });
        });
    }
    private void renderPhotos() {
        if (photoPanel == null || getView() == null) return;
        CatalogViewModel model = photoModel; photoPanel.removeAllViews();
        TextView count = new TextView(requireContext()); count.setText("Fotos: " + model.photos.size() + "/5\n" + model.photoError.getValue()); photoPanel.addView(count);
        boolean busy = Boolean.TRUE.equals(model.preparingPhoto.getValue()) || model.state().getValue().saving || model.publicationPending();
        Button take = new Button(requireContext()); take.setText("Tirar foto"); photoPanel.addView(take);
        take.setEnabled(!busy && model.photos.size()<5);
        take.setOnClickListener(v -> { try { java.io.File dir = new java.io.File(requireContext().getCacheDir(), "object_photos"); dir.mkdirs(); java.io.File file = java.io.File.createTempFile("capture", ".jpg", dir); model.cameraPath = file.getAbsolutePath(); camera.launch(androidx.core.content.FileProvider.getUriForFile(requireContext(), requireContext().getPackageName()+".fileprovider", file)); } catch (Exception e) { model.photoError.setValue("Câmera indisponível."); } });
        Button pick = new Button(requireContext()); pick.setText("Escolher da galeria"); photoPanel.addView(pick); pick.setEnabled(!busy && model.photos.size()<5);
        pick.setOnClickListener(v -> gallery.launch(new androidx.activity.result.PickVisualMediaRequest.Builder().setMediaType(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build()));
        for (ObjectPhoto photo : new ArrayList<>(model.photos)) {
            ImageView image = new ImageView(requireContext()); image.setContentDescription("Prévia da foto"); photoPanel.addView(image, new LinearLayout.LayoutParams(-1, 200)); PhotoImages.bind(image, photo.thumbnail);
            Button remove = new Button(requireContext()); remove.setText("Remover foto"); remove.setEnabled(!busy); photoPanel.addView(remove); remove.setOnClickListener(v -> { model.photos.remove(photo); renderPhotos(); });
        }
        getView().findViewById(R.id.object_save).setEnabled(!Boolean.TRUE.equals(model.preparingPhoto.getValue()) && !model.state().getValue().saving && model.canCreate() && !model.state().getValue().loading && model.state().getValue().error.isEmpty() && !model.state().getValue().points.isEmpty());
    }
    @Override public void onDestroy() {
        if (isRemoving() && photoModel != null && !photoModel.publicationPending()) {
            photoModel.photos.clear();
            if (photoModel.cameraPath != null) { new java.io.File(photoModel.cameraPath).delete(); photoModel.cameraPath = null; }
        }
        super.onDestroy();
    }
    @Override public void onDestroyView() { photoPanel = null; super.onDestroyView(); }
    private String text(View view, int id) { return ((EditText)view.findViewById(id)).getText().toString(); }
    @Override public void onViewCreated(@NonNull View view, Bundle saved) {
        HomeFragment home = (HomeFragment) requireParentFragment();
        CatalogViewModel model = home.catalogModel();
        if (model == null) return; // Sessão ainda sendo restaurada; a Activity exibirá a autenticação.
        photoModel = model;
        android.view.ViewGroup parent = (android.view.ViewGroup) view.findViewById(R.id.object_save).getParent();
        photoPanel = new LinearLayout(requireContext()); photoPanel.setOrientation(LinearLayout.VERTICAL); parent.addView(photoPanel, parent.indexOfChild(view.findViewById(R.id.object_save)));
        model.preparingPhoto.observe(getViewLifecycleOwner(), value -> renderPhotos()); model.photoError.observe(getViewLifecycleOwner(), value -> renderPhotos());
        Spinner points = view.findViewById(R.id.object_point);
        view.findViewById(R.id.object_back).setOnClickListener(v -> home.handleBack());
        view.findViewById(R.id.object_reload).setOnClickListener(v -> model.refresh());
        view.findViewById(R.id.object_location).setOnClickListener(v -> MapPickerDialog.create(model.latitude, model.longitude, false).show(getChildFragmentManager(), "object_location"));
        getChildFragmentManager().setFragmentResultListener("location", getViewLifecycleOwner(), (key, result) -> {
            model.latitude = result.getDouble("lat"); model.longitude = result.getDouble("lng");
            ((TextView)view.findViewById(R.id.object_coordinates)).setText(getString(R.string.location_selected, model.latitude, model.longitude));
        });
        view.findViewById(R.id.object_save).setOnClickListener(v -> model.save(new ObjectDraft(text(view, R.id.object_name),
                text(view, R.id.object_description), text(view, R.id.object_place), model.draftPoint, model.latitude, model.longitude, model.photos)));
        model.state().observe(getViewLifecycleOwner(), state -> {
            if (state.saved) { home.objectSaved(); return; }
            view.findViewById(R.id.object_progress).setVisibility(state.saving || state.loading ? View.VISIBLE : View.GONE);
            String message = !state.saveError.isEmpty() ? state.saveError : !state.error.isEmpty() ? state.error : state.points.isEmpty() && !state.loading ? getString(R.string.no_points_for_object) : "";
            ((TextView)view.findViewById(R.id.object_error)).setText(message);
            ((TextView)view.findViewById(R.id.object_coordinates)).setText(model.latitude == null ? "" : getString(R.string.location_selected, model.latitude, model.longitude));
            for (int id : new int[]{R.id.object_name, R.id.object_description, R.id.object_place, R.id.object_location, R.id.object_back, R.id.object_point}) view.findViewById(id).setEnabled(!state.saving && !model.publicationPending());
            view.findViewById(R.id.object_save).setEnabled(model.canCreate() && !state.saving && !state.loading && state.error.isEmpty() && !state.points.isEmpty());
            view.findViewById(R.id.object_reload).setEnabled(!state.saving && !state.loading);
            renderPhotos();
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
