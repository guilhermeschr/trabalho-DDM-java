package br.edu.ddm.achadoseperdidos.catalog;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
public class ObjectPhotoTest {
    private String image(int bytes) { return Base64.getEncoder().encodeToString(new byte[bytes]); }
    @Test public void limitesExatos() {
        assertTrue(new ObjectPhoto(image(102400), image(12288)).valid());
        assertFalse(new ObjectPhoto(image(102401), image(100)).valid());
        assertFalse(new ObjectPhoto(image(100), image(12289)).valid());
        assertFalse(new ObjectPhoto("!", image(100)).valid());
    }
    @Test public void cincoFotosESnapshotImutavel() {
        List<ObjectPhoto> photos = new ArrayList<>(Collections.nCopies(5, new ObjectPhoto(image(100),image(100))));
        ObjectDraft draft = new ObjectDraft("Objeto", "Descrição", "Praça", "ponto", 0d, 0d, photos);
        assertNull(draft.validate()); photos.add(photos.get(0)); assertEquals(5,draft.photos.size());
        assertNotNull(new ObjectDraft("Objeto", "Descrição", "Praça", "ponto",0d,0d,photos).validate());
    }
}
