package br.edu.ddm.achadoseperdidos.ui;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import br.edu.ddm.achadoseperdidos.R;
import br.edu.ddm.achadoseperdidos.auth.AuthViewModel;
public class HomeFragment extends Fragment {
    public HomeFragment() { super(R.layout.fragment_home); }
    @Override public void onViewCreated(@NonNull View view, Bundle saved) {
        AuthViewModel model = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        model.state().observe(getViewLifecycleOwner(), state -> {
            if (state.profile == null) return;
            ((TextView)view.findViewById(R.id.greeting)).setText(getString(R.string.welcome, state.profile.name));
            ((TextView)view.findViewById(R.id.profile_role)).setText("PONTO_COLETA".equals(state.profile.role) ? R.string.collection : R.string.common);
        });
        view.findViewById(R.id.logout).setOnClickListener(v -> model.showLogin());
    }
}
