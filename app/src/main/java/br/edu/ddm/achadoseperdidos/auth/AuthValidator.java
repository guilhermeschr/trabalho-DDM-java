package br.edu.ddm.achadoseperdidos.auth;

public final class AuthValidator {
    private AuthValidator() {}
    public static String login(String email, String password) {
        if (email == null || !email.trim().matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) return "Informe um e-mail válido.";
        if (password == null || password.isEmpty()) return "Informe sua senha.";
        return null;
    }
    public static String registration(String name, String email, String password, String confirm,
            String role, String address, Double lat, Double lng, boolean completing) {
        if (name == null || name.trim().isEmpty()) return "Informe o nome.";
        if (!"COMUM".equals(role) && !"PONTO_COLETA".equals(role)) return "Selecione um perfil válido.";
        String error = login(email, completing ? "unused" : password);
        if (error != null) return error;
        if (!completing && password.length() < 6) return "Use uma senha com pelo menos 6 caracteres.";
        if (!completing && !password.equals(confirm)) return "As senhas não coincidem.";
        if ("PONTO_COLETA".equals(role)) {
            if (address == null || address.trim().isEmpty()) return "Informe o endereço do ponto.";
            if (lat == null || lng == null || !Double.isFinite(lat) || !Double.isFinite(lng)
                    || Math.abs(lat) > 90 || Math.abs(lng) > 180) return "Selecione a localização no mapa.";
        }
        return null;
    }
}
