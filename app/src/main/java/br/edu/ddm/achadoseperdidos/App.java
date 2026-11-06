package br.edu.ddm.achadoseperdidos;
import android.app.Application;
import androidx.room.Room;
import br.edu.ddm.achadoseperdidos.catalog.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import java.util.concurrent.Executors;
import br.edu.ddm.achadoseperdidos.auth.*;
import br.edu.ddm.achadoseperdidos.data.AppDatabase;
public class App extends Application {
    private AuthRepository auth;
    private CatalogRepository catalog;
    @Override public void onCreate() {
        super.onCreate();
        java.io.File[] abandoned = new java.io.File(getCacheDir(), "object_photos").listFiles();
        if (abandoned != null) for (java.io.File file : abandoned) file.delete();
        AppDatabase db = Room.databaseBuilder(this, AppDatabase.class, "achados-cache.db").build();
        if (FirebaseApp.initializeApp(this) == null) {
            auth = new UnconfiguredAuthRepository(); catalog = new UnconfiguredCatalogRepository();
        }
        else {
            FirebaseFirestore store = FirebaseFirestore.getInstance();
            // Room é o cache explícito; perfis autorizadores não dependem do cache automático do Firestore.
            store.setFirestoreSettings(new FirebaseFirestoreSettings.Builder().setPersistenceEnabled(false).build());
            catalog = new FirebaseCatalogRepository(store, FirebaseAuth.getInstance());
            auth = new FirebaseAuthRepository(FirebaseAuth.getInstance(), store, db.profiles(), Executors.newSingleThreadExecutor());
        }
    }
    public CatalogRepository catalog() { return catalog; }
    public AuthRepository auth() { return auth; }
}
