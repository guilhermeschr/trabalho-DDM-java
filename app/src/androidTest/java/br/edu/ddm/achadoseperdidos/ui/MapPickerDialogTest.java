package br.edu.ddm.achadoseperdidos.ui;

import android.os.SystemClock;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import org.junit.runner.RunWith;
import br.edu.ddm.achadoseperdidos.R;
import static org.junit.Assert.*;

/** Integração real com a Demo Key local: somente mapa, sem criar contas ou dados no Firebase. */
@RunWith(AndroidJUnit4.class)
public class MapPickerDialogTest {
    @Test public void selecionarLocalPreservaCoordenadasAoRecriarEDevolveResultado() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                MapPickerDialog.create(-27.2, -49.6, false).showNow(activity.getSupportFragmentManager(), "map_test");
                assertFalse(dialog(activity).getButton(AlertDialog.BUTTON_POSITIVE).isEnabled());
            });
            waitForMap(scenario, false);
            scenario.onActivity(activity -> tapMap(dialog(activity)));
            waitForSelection(scenario);
            AtomicReference<String> selected = new AtomicReference<>();
            scenario.onActivity(activity -> selected.set(status(activity)));
            scenario.recreate();
            waitForMap(scenario, false);
            AtomicReference<double[]> result = new AtomicReference<>();
            scenario.onActivity(activity -> {
                assertEquals(selected.get(), status(activity));
                activity.getSupportFragmentManager().setFragmentResultListener("location", activity,
                        (key, value) -> result.set(new double[]{value.getDouble("lat"), value.getDouble("lng")}));
                dialog(activity).getButton(AlertDialog.BUTTON_POSITIVE).performClick();
            });
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            assertNotNull("A confirmação deve devolver as coordenadas ao formulário", result.get());
            assertTrue(Math.abs(result.get()[0]) <= 90);
            assertTrue(Math.abs(result.get()[1]) <= 180);
        }
    }

    @Test public void consultarLocalMantemMarcadorEFechaSemAlterarCoordenadas() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            AtomicBoolean changed = new AtomicBoolean();
            scenario.onActivity(activity -> {
                activity.getSupportFragmentManager().setFragmentResultListener("location", activity,
                        (key, value) -> changed.set(true));
                MapPickerDialog.create(-27.2, -49.6, true).showNow(activity.getSupportFragmentManager(), "map_test");
            });
            waitForMap(scenario, true);
            scenario.onActivity(activity -> {
                AlertDialog dialog = dialog(activity);
                tapMap(dialog);
                assertEquals(activity.getString(R.string.map_read_only), status(activity));
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
            });
            assertFalse("Consultar um ponto não pode alterar sua localização", changed.get());
        }
    }

    private static AlertDialog dialog(MainActivity activity) {
        return (AlertDialog) ((MapPickerDialog) activity.getSupportFragmentManager()
                .findFragmentByTag("map_test")).requireDialog();
    }
    private static String status(MainActivity activity) {
        return ((TextView) dialog(activity).findViewById(R.id.map_status)).getText().toString();
    }
    private static void waitForMap(ActivityScenario<MainActivity> scenario, boolean readOnly) {
        long until = SystemClock.uptimeMillis() + 40000;
        AtomicBoolean ready = new AtomicBoolean();
        AtomicReference<String> lastStatus = new AtomicReference<>();
        while (!ready.get() && SystemClock.uptimeMillis() < until) {
            scenario.onActivity(activity -> {
                String message = status(activity);
                lastStatus.set(message);
                assertNotEquals("Falha no carregamento real do Google Maps", activity.getString(R.string.map_error), message);
                ready.set(readOnly ? message.equals(activity.getString(R.string.map_read_only))
                        : !message.equals(activity.getString(R.string.map_loading)));
            });
            if (!ready.get()) SystemClock.sleep(100);
        }
        assertTrue("O mapa não ficou pronto: " + lastStatus.get(), ready.get());
    }
    private static void waitForSelection(ActivityScenario<MainActivity> scenario) {
        long until = SystemClock.uptimeMillis() + 5000;
        AtomicBoolean selected = new AtomicBoolean();
        while (!selected.get() && SystemClock.uptimeMillis() < until) {
            scenario.onActivity(activity -> selected.set(dialog(activity).getButton(AlertDialog.BUTTON_POSITIVE).isEnabled()));
            if (!selected.get()) SystemClock.sleep(100);
        }
        assertTrue("O toque deve selecionar uma posição válida", selected.get());
    }
    private static void tapMap(AlertDialog dialog) {
        WebView web = dialog.findViewById(R.id.map_web);
        long now = SystemClock.uptimeMillis();
        MotionEvent down = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, web.getWidth() / 2f, web.getHeight() / 2f, 0);
        MotionEvent up = MotionEvent.obtain(now, now + 100, MotionEvent.ACTION_UP, web.getWidth() / 2f, web.getHeight() / 2f, 0);
        web.dispatchTouchEvent(down); web.dispatchTouchEvent(up);
        down.recycle(); up.recycle();
    }
}
