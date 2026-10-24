package br.edu.ddm.achadoseperdidos.auth;
import br.edu.ddm.achadoseperdidos.data.UserProfile;
public final class AuthState {
    public enum Screen { LOGIN, REGISTER, HOME }
    public final Screen screen;
    public final boolean loading, completing;
    public final String message, email;
    public final UserProfile profile;
    public AuthState(Screen screen, boolean loading, boolean completing, String message, String email, UserProfile profile) {
        this.screen = screen; this.loading = loading; this.completing = completing;
        this.message = message; this.email = email; this.profile = profile;
    }
}
