package br.edu.ddm.achadoseperdidos.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "profiles")
public class UserProfile {
    @PrimaryKey @NonNull public String uid = "";
    @NonNull public String name = "";
    @NonNull public String role = "";
    public UserProfile() {}
    @androidx.room.Ignore
    public UserProfile(@NonNull String uid, @NonNull String name, @NonNull String role) {
        this.uid = uid; this.name = name; this.role = role;
    }
}
