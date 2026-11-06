package br.edu.ddm.achadoseperdidos.ui;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.webkit.WebView;
import androidx.appcompat.app.AlertDialog;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Tasks;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import br.edu.ddm.achadoseperdidos.R;
import static org.junit.Assert.*;

/** Coordenadas fictícias pelo provedor de testes; não usa a localização real do usuário. */
@RunWith(AndroidJUnit4.class)
public class CurrentLocationMapTest {
    @Rule public GrantPermissionRule permission = GrantPermissionRule.grant(
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
    private FusedLocationProviderClient client;
    private String previousLocationEnabled;

    @Before public void prepareLocation() throws Exception {
        previousLocationEnabled = shell("cmd location is-location-enabled").trim();
        shell("cmd location set-location-enabled true");
        shell("appops set trabalho.DDM.java android:mock_location allow");
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        client = LocationServices.getFusedLocationProviderClient(context);
        Tasks.await(client.setMockMode(true), 10, TimeUnit.SECONDS);
        pushMockLocation();
    }

    private void pushMockLocation() throws Exception {
        Location location = new Location("fused");
        location.setLatitude(-27.2); location.setLongitude(-49.6);
        location.setAccuracy(5); location.setTime(System.currentTimeMillis());
        location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        Tasks.await(client.setMockLocation(location), 10, TimeUnit.SECONDS);
    }

    @After public void restoreLocation() throws Exception {
        try { if (client != null) Tasks.await(client.setMockMode(false), 10, TimeUnit.SECONDS); }
        finally {
            shell("appops set trabalho.DDM.java android:mock_location default");
            if ("false".equals(previousLocationEnabled)) shell("cmd location set-location-enabled false");
            else shell("cmd location set-location-enabled true");
        }
    }

    @Test public void localAtualFicaPreSelecionadoSemToqueEPrecisaDeConfirmacao() throws Exception {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            AtomicReference<double[]> result = new AtomicReference<>();
            scenario.onActivity(activity -> {
                listen(activity, result);
                MapPickerDialog.create(null, null, false).showNow(activity.getSupportFragmentManager(), "location_test");
            });
            // Publicar uma posição nova depois de iniciar a consulta, sem depender da velocidade de abertura da Activity.
            pushMockLocation();
            waitFor(scenario, R.string.map_location_suggested, true);
            assertNull("Pré-seleção não deve salvar o formulário automaticamente", result.get());
            scenario.onActivity(activity -> dialog(activity).getButton(AlertDialog.BUTTON_POSITIVE).performClick());
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            assertArrayEquals(new double[]{-27.2, -49.6}, result.get(), 0.00001);
        }
    }

    @Test public void localExistentePermaneceAteSolicitarExplicitamentePosicaoAtual() throws Exception {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> MapPickerDialog.create(-26.1, -48.2, false)
                    .showNow(activity.getSupportFragmentManager(), "location_test"));
            waitFor(scenario, R.string.location_selected, false);
            pushMockLocation();
            scenario.onActivity(activity -> {
                assertEquals(activity.getString(R.string.location_selected, -26.1, -48.2), text(activity, R.id.map_status));
                assertEquals(View.GONE, dialog(activity).findViewById(R.id.map_location_status).getVisibility());
                dialog(activity).findViewById(R.id.map_current_location).performClick();
            });
            waitFor(scenario, R.string.map_location_suggested, true);
            scenario.onActivity(activity -> assertEquals(activity.getString(R.string.location_selected, -27.2, -49.6), text(activity, R.id.map_status)));
        }
    }

    @Test public void gpsDesligadoPermiteSelecionarEConfirmarManualmente() throws Exception {
        shell("cmd location set-location-enabled false");
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            AtomicReference<double[]> result = new AtomicReference<>();
            scenario.onActivity(activity -> {
                listen(activity, result);
                MapPickerDialog.create(null, null, false).showNow(activity.getSupportFragmentManager(), "location_test");
            });
            waitFor(scenario, R.string.map_location_unavailable, true);
            waitFor(scenario, R.string.map_ready, false);
            scenario.onActivity(activity -> {
                assertFalse(dialog(activity).getButton(AlertDialog.BUTTON_POSITIVE).isEnabled());
                WebView web = dialog(activity).findViewById(R.id.map_web);
                long now = SystemClock.uptimeMillis();
                MotionEvent down = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, web.getWidth() / 2f, web.getHeight() / 2f, 0);
                MotionEvent up = MotionEvent.obtain(now, now + 100, MotionEvent.ACTION_UP, web.getWidth() / 2f, web.getHeight() / 2f, 0);
                web.dispatchTouchEvent(down); web.dispatchTouchEvent(up);
                down.recycle(); up.recycle();
            });
            long until = SystemClock.uptimeMillis() + 5000;
            AtomicBoolean enabled = new AtomicBoolean();
            while (!enabled.get() && SystemClock.uptimeMillis() < until) {
                scenario.onActivity(activity -> enabled.set(dialog(activity).getButton(AlertDialog.BUTTON_POSITIVE).isEnabled()));
                if (!enabled.get()) SystemClock.sleep(100);
            }
            assertTrue(enabled.get());
            scenario.onActivity(activity -> dialog(activity).getButton(AlertDialog.BUTTON_POSITIVE).performClick());
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            assertNotNull(result.get());
        }
    }

    @Test public void semPermissaoConsultaDeLocalizacaoRetornaSemAcessarProvedor() {
        Context context = new ContextWrapper(InstrumentationRegistry.getInstrumentation().getTargetContext()) {
            @Override public int checkPermission(String permission, int pid, int uid) { return PackageManager.PERMISSION_DENIED; }
        };
        AtomicBoolean callback = new AtomicBoolean();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> new CurrentLocationSource().request(context, location -> {
            assertNull(location); callback.set(true);
        }));
        assertTrue(callback.get());
    }

    private static void listen(MainActivity activity, AtomicReference<double[]> result) {
        activity.getSupportFragmentManager().setFragmentResultListener("location", activity,
                (key, value) -> result.set(new double[]{value.getDouble("lat"), value.getDouble("lng")}));
    }
    private static AlertDialog dialog(MainActivity activity) {
        return (AlertDialog) ((MapPickerDialog) activity.getSupportFragmentManager()
                .findFragmentByTag("location_test")).requireDialog();
    }
    private static String text(MainActivity activity, int id) {
        return ((TextView) dialog(activity).findViewById(id)).getText().toString();
    }
    private static void waitFor(ActivityScenario<MainActivity> scenario, int message, boolean locationStatus) {
        long until = SystemClock.uptimeMillis() + 40000;
        AtomicBoolean ready = new AtomicBoolean();
        AtomicReference<String> last = new AtomicReference<>();
        while (!ready.get() && SystemClock.uptimeMillis() < until) {
            scenario.onActivity(activity -> {
                String value = text(activity, locationStatus ? R.id.map_location_status : R.id.map_status);
                last.set(value);
                assertNotEquals(activity.getString(R.string.map_error), text(activity, R.id.map_status));
                ready.set(message == R.string.location_selected ? value.startsWith("Local selecionado:")
                        : value.equals(activity.getString(message)));
            });
            if (!ready.get()) SystemClock.sleep(100);
        }
        assertTrue("Mensagem não exibida: " + last.get(), ready.get());
    }
    private static String shell(String command) throws Exception {
        ParcelFileDescriptor descriptor = InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(command);
        try (FileInputStream input = new ParcelFileDescriptor.AutoCloseInputStream(descriptor);
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024]; int read;
            while ((read = input.read(buffer)) != -1) output.write(buffer, 0, read);
            return output.toString("UTF-8");
        }
    }
}
