package br.edu.ddm.achadoseperdidos.auth;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import com.google.firebase.firestore.WriteBatch;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import br.edu.ddm.achadoseperdidos.data.ProfileDao;
import br.edu.ddm.achadoseperdidos.data.UserProfile;

public final class FirebaseAuthRepository implements AuthRepository {
    private final FirebaseAuth auth;
    private final FirebaseFirestore store;
    private final ProfileDao cache;
    private final Executor disk;
    public FirebaseAuthRepository(FirebaseAuth auth, FirebaseFirestore store, ProfileDao cache, Executor disk) {
        this.auth = auth; this.store = store; this.cache = cache; this.disk = disk;
    }
    @Override public void restore(Callback callback) {
        if (auth.getCurrentUser() == null) callback.error("");
        else load(callback);
    }
    @Override public void login(String email, String password, Callback callback) {
        auth.signInWithEmailAndPassword(email.trim(), password).addOnSuccessListener(result -> load(callback))
                .addOnFailureListener(e -> callback.error(message(e)));
    }
    private void load(Callback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) { callback.error("Entre novamente para continuar."); return; }
        String uid = user.getUid();
        store.collection("usuarios").document(uid).get(Source.SERVER).addOnSuccessListener(doc -> {
            if (!doc.exists()) { callback.incomplete(user.getEmail()); return; }
            String name = doc.getString("nome"), role = doc.getString("perfil");
            if (name == null || name.trim().isEmpty() || !("COMUM".equals(role) || "PONTO_COLETA".equals(role))) {
                callback.error("Cadastro inválido. Entre em contato com o responsável pelo aplicativo."); return;
            }
            UserProfile profile = new UserProfile(uid, name, role);
            if ("PONTO_COLETA".equals(role)) {
                store.collection("pontosColeta").document(uid).get(Source.SERVER).addOnSuccessListener(point -> {
                    if (!point.exists()) callback.error("Cadastro do ponto inconsistente. Contate o responsável pelo aplicativo.");
                    else complete(profile, callback);
                }).addOnFailureListener(e -> callback.error(message(e)));
            } else complete(profile, callback);
        }).addOnFailureListener(e -> callback.error(message(e)));
    }
    private void complete(UserProfile profile, Callback callback) {
        // Cache não autentica. Sempre validar o perfil no servidor antes de liberar a sessão.
        disk.execute(() -> cache.save(profile));
        callback.success(profile);
    }
    @Override public void register(Registration data, boolean completing, Callback callback) {
        if (completing) {
            if (auth.getCurrentUser() == null) callback.error("Sua sessão expirou. Entre novamente.");
            else saveProfile(data, callback);
        } else {
            auth.createUserWithEmailAndPassword(data.email, data.password)
                    .addOnSuccessListener(result -> saveProfile(data, callback))
                    .addOnFailureListener(e -> callback.error(message(e)));
        }
    }
    private void saveProfile(Registration data, Callback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) { callback.error("Sua sessão expirou. Entre novamente."); return; }
        String uid = user.getUid();
        Map<String,Object> profile = new HashMap<>();
        profile.put("nome", data.name); profile.put("perfil", data.role);
        profile.put("criadoEm", FieldValue.serverTimestamp());
        WriteBatch batch = store.batch();
        batch.set(store.collection("usuarios").document(uid), profile);
        if ("PONTO_COLETA".equals(data.role)) {
            Map<String,Object> point = new HashMap<>();
            point.put("responsavelUid", uid); point.put("nome", data.name);
            point.put("endereco", data.address); point.put("latitude", data.latitude);
            point.put("longitude", data.longitude); point.put("criadoEm", FieldValue.serverTimestamp());
            batch.set(store.collection("pontosColeta").document(uid), point);
        }
        batch.commit().addOnSuccessListener(unused -> complete(new UserProfile(uid, data.name, data.role), callback))
                .addOnFailureListener(e -> {
                    // Auth e Firestore não compartilham transação: manter a sessão permite retomar o perfil.
                    store.collection("usuarios").document(uid).get(Source.SERVER)
                            .addOnSuccessListener(doc -> {
                                if (doc.exists()) load(callback);
                                else callback.incomplete(user.getEmail());
                            }).addOnFailureListener(readError -> callback.incomplete(user.getEmail()));
                });
    }
    @Override public void logout() { auth.signOut(); disk.execute(cache::clear); }
    private String message(Exception error) {
        if (error instanceof FirebaseNetworkException) return "Sem conexão. Verifique a internet e tente novamente.";
        if (error instanceof FirebaseAuthException) {
            String code = ((FirebaseAuthException) error).getErrorCode();
            switch (code) {
                case "ERROR_EMAIL_ALREADY_IN_USE": return "Este e-mail já está cadastrado. Entre com sua senha.";
                case "ERROR_WEAK_PASSWORD": return "A senha não atende à política de segurança do Firebase.";
                case "ERROR_INVALID_EMAIL": return "Informe um e-mail válido.";
                case "ERROR_USER_DISABLED": return "Esta conta está desativada.";
                case "ERROR_TOO_MANY_REQUESTS": return "Muitas tentativas. Aguarde antes de tentar novamente.";
                case "ERROR_WRONG_PASSWORD": case "ERROR_USER_NOT_FOUND": case "ERROR_INVALID_CREDENTIAL":
                    return "E-mail ou senha incorretos.";
                default: return "Não foi possível autenticar. Verifique a configuração e tente novamente.";
            }
        }
        return "Não foi possível carregar o cadastro. Verifique sua conexão e tente novamente.";
    }
}
