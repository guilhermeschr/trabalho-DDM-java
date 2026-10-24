package br.edu.ddm.achadoseperdidos.data;
import androidx.room.Database;
import androidx.room.RoomDatabase;
@Database(entities = {UserProfile.class}, version = 1, exportSchema = true)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ProfileDao profiles();
}
