package br.edu.ddm.achadoseperdidos.data;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
@Dao public interface ProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) void save(UserProfile profile);
    @Query("DELETE FROM profiles") void clear();
    @Query("SELECT * FROM profiles WHERE uid = :uid LIMIT 1") UserProfile find(String uid);
}
