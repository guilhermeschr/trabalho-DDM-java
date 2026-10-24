package br.edu.ddm.achadoseperdidos.auth;
public final class UnconfiguredAuthRepository implements AuthRepository {
    private void fail(Callback c) { c.error("Firebase não configurado. Adicione app/google-services.json e habilite Authentication e Firestore conforme o README."); }
    @Override public void restore(Callback c) { fail(c); }
    @Override public void login(String email, String password, Callback c) { fail(c); }
    @Override public void register(Registration data, boolean completing, Callback c) { fail(c); }
    @Override public void logout() {}
}
