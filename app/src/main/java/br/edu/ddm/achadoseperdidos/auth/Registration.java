package br.edu.ddm.achadoseperdidos.auth;
public final class Registration {
    public final String name, email, password, confirm, role, address;
    public final Double latitude, longitude;
    public Registration(String name, String email, String password, String confirm, String role,
                        String address, Double latitude, Double longitude) {
        this.name = name.trim(); this.email = email.trim(); this.password = password;
        this.confirm = confirm; this.role = role; this.address = address.trim();
        this.latitude = latitude; this.longitude = longitude;
    }
}
