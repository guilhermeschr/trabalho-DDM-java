package br.edu.ddm.achadoseperdidos.auth;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import br.edu.ddm.achadoseperdidos.data.UserProfile;
import static br.edu.ddm.achadoseperdidos.auth.AuthState.Screen.*;

public final class AuthViewModel extends ViewModel {
    private final AuthRepository repository;
    private final MutableLiveData<AuthState> state = new MutableLiveData<>(new AuthState(LOGIN, false, false, "", "", null));
    public Double latitude, longitude;
    public AuthViewModel(AuthRepository repository) { this.repository = repository; }
    public LiveData<AuthState> state() { return state; }
    private boolean started;
    public void start() { if (!started) { started = true; restore(); } }
    public void restore() { busy(); repository.restore(callback()); }
    public void showRegister() { if (!state.getValue().loading) state.setValue(new AuthState(REGISTER, false, false, "", "", null)); }
    public void showLogin() {
        if (state.getValue().loading) return;
        repository.logout(); latitude = null; longitude = null;
        state.setValue(new AuthState(LOGIN, false, false, "", "", null));
    }
    public void login(String email, String password) {
        if (state.getValue().loading) return;
        String error = AuthValidator.login(email, password);
        if (error != null) { error(error); return; }
        busy(); repository.login(email.trim(), password, callback());
    }
    public void register(Registration input) {
        AuthState current = state.getValue();
        if (current.loading) return;
        String error = AuthValidator.registration(input.name, input.email, input.password, input.confirm,
                input.role, input.address, input.latitude, input.longitude, current.completing);
        if (error != null) { error(error); return; }
        busy(); repository.register(input, current.completing, callback());
    }
    private void busy() {
        AuthState old = state.getValue();
        state.setValue(new AuthState(old.screen, true, old.completing, "", old.email, old.profile));
    }
    private void error(String message) {
        AuthState old = state.getValue();
        state.setValue(new AuthState(old.screen, false, old.completing, message, old.email, old.profile));
    }
    private AuthRepository.Callback callback() {
        return new AuthRepository.Callback() {
            public void success(UserProfile profile) { state.setValue(new AuthState(HOME, false, false, "", "", profile)); }
            public void incomplete(String email) {
                state.setValue(new AuthState(REGISTER, false, true,
                        "Sua conta foi criada, mas o perfil não foi salvo. Complete os dados para continuar.", email, null));
            }
            public void error(String message) { AuthViewModel.this.error(message); }
        };
    }
}
