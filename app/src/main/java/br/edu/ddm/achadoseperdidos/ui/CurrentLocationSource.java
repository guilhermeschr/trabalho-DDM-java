package br.edu.ddm.achadoseperdidos.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import androidx.core.content.ContextCompat;
import androidx.core.location.LocationManagerCompat;
import androidx.core.util.Consumer;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

/** Uma consulta em primeiro plano; sem acompanhamento contínuo ou persistência de localização. */
final class CurrentLocationSource {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private CancellationTokenSource token;
    private Runnable timeout;
    private int generation;

    static boolean hasPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission") // Permissão verificada antes de cada consulta e falha tratada se for revogada.
    void request(Context context, Consumer<Location> callback) {
        cancel();
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (!hasPermission(context) || manager == null || !LocationManagerCompat.isLocationEnabled(manager)) {
            callback.accept(null); return;
        }
        int request = generation;
        token = new CancellationTokenSource();
        timeout = () -> finish(request, null, callback);
        handler.postDelayed(timeout, 12000);
        boolean precise = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        CurrentLocationRequest options = new CurrentLocationRequest.Builder()
                .setPriority(precise ? Priority.PRIORITY_HIGH_ACCURACY : Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                .setMaxUpdateAgeMillis(10000).setDurationMillis(10000).build();
        try {
            LocationServices.getFusedLocationProviderClient(context).getCurrentLocation(options, token.getToken())
                    .addOnSuccessListener(location -> finish(request, location, callback))
                    .addOnFailureListener(error -> finish(request, null, callback));
        } catch (SecurityException | IllegalStateException error) { finish(request, null, callback); }
    }

    private void finish(int request, Location location, Consumer<Location> callback) {
        if (request != generation) return;
        cancel();
        callback.accept(location);
    }

    void cancel() {
        generation++;
        if (timeout != null) { handler.removeCallbacks(timeout); timeout = null; }
        if (token != null) { token.cancel(); token = null; }
    }
}
