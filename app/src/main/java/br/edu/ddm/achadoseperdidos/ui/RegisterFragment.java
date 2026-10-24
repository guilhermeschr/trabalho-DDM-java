package br.edu.ddm.achadoseperdidos.ui;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import br.edu.ddm.achadoseperdidos.R;
import br.edu.ddm.achadoseperdidos.auth.*;
public class RegisterFragment extends Fragment {
    public RegisterFragment() { super(R.layout.fragment_register); }
    private String text(View view, int id) { return ((EditText)view.findViewById(id)).getText().toString(); }
    @Override public void onViewCreated(@NonNull View view, Bundle saved) {
        AuthViewModel model = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        RadioGroup role = view.findViewById(R.id.role);
        role.setOnCheckedChangeListener((group, id) -> view.findViewById(R.id.point_fields).setVisibility(id == R.id.collection ? View.VISIBLE : View.GONE));
        view.findViewById(R.id.location).setOnClickListener(v -> new MapPickerDialog().show(getChildFragmentManager(), "location"));
        getChildFragmentManager().setFragmentResultListener("location", getViewLifecycleOwner(), (key,result) -> {
            model.latitude = result.getDouble("lat"); model.longitude = result.getDouble("lng");
            coordinates(view, model);
        });
        view.findViewById(R.id.back).setOnClickListener(v -> model.showLogin());
        view.findViewById(R.id.save).setOnClickListener(v -> model.register(new Registration(text(view,R.id.name), text(view,R.id.email),
                text(view,R.id.password), text(view,R.id.confirm), role.getCheckedRadioButtonId() == R.id.collection ? "PONTO_COLETA" : "COMUM",
                text(view,R.id.address), model.latitude, model.longitude)));
        model.state().observe(getViewLifecycleOwner(), state -> {
            view.findViewById(R.id.progress).setVisibility(state.loading ? View.VISIBLE : View.GONE);
            ((TextView)view.findViewById(R.id.error)).setText(state.message);
            for (int id : new int[]{R.id.save,R.id.back,R.id.name,R.id.password,R.id.confirm,R.id.address,R.id.location,R.id.common,R.id.collection}) view.findViewById(id).setEnabled(!state.loading);
            view.findViewById(R.id.email).setEnabled(!state.loading && !state.completing);
            ((TextView)view.findViewById(R.id.title)).setText(state.completing ? R.string.complete_profile : R.string.register_title);
            if (state.completing) ((EditText)view.findViewById(R.id.email)).setText(state.email);
            for (int id : new int[]{R.id.password,R.id.confirm}) {
                ((View)view.findViewById(id).getParent().getParent()).setVisibility(state.completing ? View.GONE : View.VISIBLE);
                if (state.completing || state.screen == AuthState.Screen.HOME) ((EditText)view.findViewById(id)).setText("");
            }
            coordinates(view, model);
        });
    }
    @Override public void onViewStateRestored(Bundle saved) {
        super.onViewStateRestored(saved);
        View view = requireView();
        view.findViewById(R.id.point_fields).setVisibility(((RadioGroup)view.findViewById(R.id.role)).getCheckedRadioButtonId() == R.id.collection ? View.VISIBLE : View.GONE);
    }
    private void coordinates(View view, AuthViewModel model) {
        ((TextView)view.findViewById(R.id.coordinates)).setText(model.latitude == null ? "" : getString(R.string.location_selected, model.latitude, model.longitude));
    }
}
