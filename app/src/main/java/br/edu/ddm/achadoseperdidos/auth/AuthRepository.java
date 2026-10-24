package br.edu.ddm.achadoseperdidos.auth;
import br.edu.ddm.achadoseperdidos.data.UserProfile;
public interface AuthRepository {
    interface Callback {
        void success(UserProfile profile);
        void incomplete(String email);
        void error(String message);
    }
    void restore(Callback callback);
    void login(String email, String password, Callback callback);
    void register(Registration data, boolean completing, Callback callback);
    void logout();
}
