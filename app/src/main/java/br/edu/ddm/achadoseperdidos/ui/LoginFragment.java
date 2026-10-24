package br.edu.ddm.achadoseperdidos.ui;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import br.edu.ddm.achadoseperdidos.R;
import br.edu.ddm.achadoseperdidos.auth.AuthViewModel;
public class LoginFragment extends Fragment {
    public LoginFragment() { super(R.layout.fragment_login); }
    @Override public void onViewCreated(@NonNull View view, Bundle saved) {
        AuthViewModel model = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        EditText email = view.findViewById(R.id.email), password = view.findViewById(R.id.password);
        view.findViewById(R.id.login).setOnClickListener(v -> model.login(email.getText().toString(), password.getText().toString()));
        view.findViewById(R.id.register).setOnClickListener(v -> model.showRegister());
        model.state().observe(getViewLifecycleOwner(), state -> {
            view.findViewById(R.id.progress).setVisibility(state.loading ? View.VISIBLE : View.GONE);
            view.findViewById(R.id.login).setEnabled(!state.loading);
            view.findViewById(R.id.register).setEnabled(!state.loading);
            email.setEnabled(!state.loading); password.setEnabled(!state.loading);
            ((TextView)view.findViewById(R.id.error)).setText(state.message);
            if (state.screen != br.edu.ddm.achadoseperdidos.auth.AuthState.Screen.LOGIN) password.setText("");
        });
    }
}
