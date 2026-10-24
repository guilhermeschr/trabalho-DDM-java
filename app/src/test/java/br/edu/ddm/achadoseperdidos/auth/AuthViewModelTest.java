package br.edu.ddm.achadoseperdidos.auth;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import org.junit.Rule;
import org.junit.Test;
import br.edu.ddm.achadoseperdidos.data.UserProfile;
import static org.junit.Assert.*;
public class AuthViewModelTest {
    @Rule public InstantTaskExecutorRule executor = new InstantTaskExecutorRule();
    private final FakeRepository repository = new FakeRepository();
    private final AuthViewModel model = new AuthViewModel(repository);
    @Test public void invalidLoginDoesNotCallFirebase() { model.login("invalid", "password"); assertEquals(0, repository.calls); assertFalse(model.state().getValue().loading); }
    @Test public void disablesDuplicateSubmissions() { model.login("a@b.com", "123456"); model.login("a@b.com", "123456"); assertEquals(1, repository.calls); assertTrue(model.state().getValue().loading); }
    @Test public void successfulLoginReleasesHome() { model.login("a@b.com", "123456"); repository.pending.success(new UserProfile("1", "Ana", "COMUM")); assertEquals(AuthState.Screen.HOME, model.state().getValue().screen); assertEquals("Ana",model.state().getValue().profile.name); }
    @Test public void failedLoginKeepsUserOutsideHome() { model.login("a@b.com", "123456"); repository.pending.error("Falha de rede"); assertEquals(AuthState.Screen.LOGIN,model.state().getValue().screen); assertFalse(model.state().getValue().loading); assertEquals("Falha de rede",model.state().getValue().message); }
    @Test public void incompleteAccountMustFinishProfile() { model.login("a@b.com", "123456"); repository.pending.incomplete("a@b.com"); assertEquals(AuthState.Screen.REGISTER, model.state().getValue().screen); assertTrue(model.state().getValue().completing); assertNull(model.state().getValue().profile); }
    @Test public void logoutClearsSessionAndMapSelection() { model.latitude=1.0; model.longitude=2.0; model.showLogin(); assertTrue(repository.loggedOut); assertEquals(AuthState.Screen.LOGIN,model.state().getValue().screen); assertNull(model.latitude); assertNull(model.state().getValue().profile); }
    @Test public void rotationDoesNotRepeatRestore() { model.start(); model.start(); assertEquals(1,repository.calls); }
    @Test public void incompleteProfileCanRetryWithoutCreatingNewAccount() { model.login("a@b.com","123456"); repository.pending.incomplete("a@b.com"); model.register(new Registration("Ana","a@b.com","","","COMUM","",null,null)); assertTrue(repository.completing); assertTrue(model.state().getValue().loading); }
    @Test public void pointCannotRegisterWithoutPosition() { model.showRegister(); model.register(new Registration("Loja","a@b.com","123456","123456","PONTO_COLETA","Rua A",null,null)); assertEquals(0,repository.calls); assertNotNull(model.state().getValue().message); }
    private static class FakeRepository implements AuthRepository {
        int calls; boolean completing, loggedOut; Callback pending;
        public void restore(Callback callback) { calls++; pending=callback; }
        public void login(String email,String password,Callback callback) { calls++; pending=callback; }
        public void register(Registration data,boolean completing,Callback callback) { calls++; this.completing=completing; pending=callback; }
        public void logout() { loggedOut=true; }
    }
}
