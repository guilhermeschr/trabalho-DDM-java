package br.edu.ddm.achadoseperdidos.catalog;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import br.edu.ddm.achadoseperdidos.data.UserProfile;
import org.junit.*;
import java.util.*;
import static org.junit.Assert.*;

public class CatalogViewModelTest {
    @Rule public InstantTaskExecutorRule executor = new InstantTaskExecutorRule();
    private final FakeRepository repository = new FakeRepository();
    private final CatalogViewModel model = new CatalogViewModel(repository, new UserProfile("u", "Ana", "COMUM"));
    private void load() {
        model.start();
        repository.read.success(new CatalogRepository.Data(Arrays.asList(new CollectionPoint("p1", "Ponto A", "Rua A", 0, 0), new CollectionPoint("p2", "Ponto B", "Rua B", 1, 1)),
                Arrays.asList(new FoundObject("1", "Mochila Azul", "Descrição", "Praça", "p1", 0, 0), new FoundObject("2", "Mochila Verde", "Descrição", "Rua", "p2", 1, 1), new FoundObject("3", "Chave", "Descrição", "Rua", "p2", 0, 0))));
    }
    private ObjectDraft valid() { return new ObjectDraft("Mochila", "Azul", "Praça", "p1", 0.0, 0.0); }
    @Test public void searchIgnoresCaseAndCombinesPointFilter() {
        load(); model.query = " MOCHILA "; model.pointFilter = "p2";
        assertEquals(1, model.filteredObjects().size()); assertEquals("2", model.filteredObjects().get(0).id);
        model.pointFilter = ""; assertEquals(2, model.filteredObjects().size());
        model.query = ""; assertEquals(3, model.filteredObjects().size());
    }
    @Test public void emptyIntersectionHasNoResults() { load(); model.query = "chave"; model.pointFilter = "p1"; assertTrue(model.filteredObjects().isEmpty()); }
    @Test public void errorsAreDistinctFromEmptySuccess() {
        model.start(); repository.read.error("Sem rede"); assertEquals("Sem rede", model.state().getValue().error); assertFalse(model.state().getValue().loading);
        model.refresh(); repository.read.success(new CatalogRepository.Data(Collections.emptyList(), Collections.emptyList())); assertEquals("", model.state().getValue().error);
    }
    @Test public void rotationDoesNotRepeatInitialLoad() { model.start(); model.start(); assertEquals(1, repository.loads); }
    @Test public void nonexistentPointCannotBeSaved() { load(); model.save(new ObjectDraft("Chave", "Azul", "Rua", "inexistente", 0.0, 0.0)); assertEquals(0, repository.saves); assertFalse(model.state().getValue().saveError.isEmpty()); }
    @Test public void failedReloadCannotUseOldPointToPublish() { load(); model.refresh(); repository.read.error("Sem rede"); model.save(valid()); assertEquals(0, repository.saves); }
    @Test public void collectionAccountCannotPublish() {
        CatalogViewModel point = new CatalogViewModel(repository, new UserProfile("p1", "Ponto", "PONTO_COLETA"));
        assertFalse(point.canCreate()); point.save(valid()); assertEquals(0, repository.saves);
    }
    @Test public void savingBlocksDuplicateSubmitAndRefresh() {
        load(); model.save(valid()); model.save(valid()); model.refresh(); assertEquals(1, repository.saves); assertEquals(1, repository.loads); assertTrue(model.state().getValue().saving);
    }
    @Test public void networkRetryReusesDocumentId() {
        load(); model.save(valid()); String id = repository.savedId; repository.write.error("Sem rede");
        model.save(valid()); assertEquals(id, repository.savedId); assertEquals(1, repository.generated);
    }
    @Test public void successCanBeConsumedBeforeRefreshWithoutRepeatingNavigation() {
        load(); model.save(valid()); repository.write.success("1"); assertTrue(model.state().getValue().saved);
        model.consumeSaved(); model.refresh(); assertFalse(model.state().getValue().saved); assertTrue(model.state().getValue().loading);
    }
    @Test public void newDraftClearsPreviousLocationAndOperation() {
        load(); model.latitude = 5.0; model.longitude = 8.0; model.draftPoint = "p1";
        model.beginDraft(); assertNull(model.latitude); assertNull(model.longitude); assertEquals("", model.draftPoint);
    }
    @Test public void whitespaceAndMissingCoordinatesAreRejected() {
        assertNotNull(new ObjectDraft(" \t", "Desc", "Rua", "p1", 0.0, 0.0).validate());
        assertNotNull(new ObjectDraft("Chave", "\n", "Rua", "p1", 0.0, 0.0).validate());
        assertNotNull(new ObjectDraft("Chave", "Desc", "Rua", "p1", null, null).validate());
        assertNotNull(new ObjectDraft("Chave", "Desc", "Rua", "p1", Double.NaN, 0.0).validate());
        assertNotNull(new ObjectDraft("Chave", "Desc", "Rua", "p1", 91.0, 0.0).validate());
        assertNull(valid().validate());
    }
    private static final class FakeRepository implements CatalogRepository {
        int loads, saves, generated; String savedId;
        Callback<Data> read; Callback<String> write;
        public void load(Callback<Data> callback) { loads++; read = callback; }
        public String newObjectId() { return "draft-" + (++generated); }
        public void save(String id, ObjectDraft draft, Callback<String> callback) { saves++; savedId = id; write = callback; }
    }
}
