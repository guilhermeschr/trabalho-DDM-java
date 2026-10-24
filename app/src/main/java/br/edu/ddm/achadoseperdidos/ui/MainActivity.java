package br.edu.ddm.achadoseperdidos.ui;
import android.os.Bundle;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import br.edu.ddm.achadoseperdidos.App;
import br.edu.ddm.achadoseperdidos.R;
import br.edu.ddm.achadoseperdidos.auth.*;

public class MainActivity extends AppCompatActivity {
    private AuthViewModel model;
    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved); setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.container), (view, insets) -> {
            androidx.core.graphics.Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom); return insets;
        });
        model = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> type) {
                return type.cast(new AuthViewModel(((App) getApplication()).auth()));
            }
        }).get(AuthViewModel.class);
        model.state().observe(this, state -> {
            String tag = state.screen.name();
            if (getSupportFragmentManager().findFragmentByTag(tag) != null) return;
            androidx.fragment.app.Fragment screen;
            switch (state.screen) {
                case REGISTER: screen = new RegisterFragment(); break;
                case HOME: screen = new HomeFragment(); break;
                default: screen = new LoginFragment();
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.container, screen, tag).commit();
        });
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                AuthState state = model.state().getValue();
                if (state.loading) return;
                if (state.screen == AuthState.Screen.REGISTER) model.showLogin();
                else finish();
            }
        });
        model.start();
    }
}
