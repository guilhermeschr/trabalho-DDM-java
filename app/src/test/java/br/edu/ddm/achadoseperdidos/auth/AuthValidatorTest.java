package br.edu.ddm.achadoseperdidos.auth;
import org.junit.Test;
import static org.junit.Assert.*;
public class AuthValidatorTest {
    @Test public void acceptsValidCommonUser() { assertNull(AuthValidator.registration("Maria", "maria@example.com", "123456", "123456", "COMUM", "", null, null, false)); }
    @Test public void rejectsBlankName() { assertNotNull(AuthValidator.registration("  ", "a@b.com", "123456", "123456", "COMUM", "", null, null, false)); }
    @Test public void rejectsInvalidEmail() { assertNotNull(AuthValidator.login("invalido", "123456")); }
    @Test public void preservesPasswordSpaces() { assertNull(AuthValidator.login(" a@b.com ", "      ")); }
    @Test public void rejectsShortPassword() { assertNotNull(AuthValidator.registration("Ana", "a@b.com", "123", "123", "COMUM", "", null, null, false)); }
    @Test public void rejectsMismatch() { assertNotNull(AuthValidator.registration("Ana", "a@b.com", "123456", "abcdef", "COMUM", "", null, null, false)); }
    @Test public void pointRequiresLocation() { assertNotNull(AuthValidator.registration("Loja", "a@b.com", "123456", "123456", "PONTO_COLETA", "Rua A", null, null, false)); }
    @Test public void rejectsOutOfRangeLocation() { assertNotNull(AuthValidator.registration("Loja", "a@b.com", "123456", "123456", "PONTO_COLETA", "Rua A", 91.0, 10.0, false)); }
    @Test public void acceptsZeroCoordinates() { assertNull(AuthValidator.registration("Loja", "a@b.com", "123456", "123456", "PONTO_COLETA", "Rua A", 0.0, 0.0, false)); }
    @Test public void completionDoesNotRequirePassword() { assertNull(AuthValidator.registration("Ana", "a@b.com", "", "", "COMUM", "", null, null, true)); }
    @Test public void rejectsUnknownRole() { assertNotNull(AuthValidator.registration("Ana", "a@b.com", "123456", "123456", "ADMIN", "", null, null, false)); }
}
