package br.edu.ddm.achadoseperdidos.ui;

import android.annotation.SuppressLint;
import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewCompat;
import androidx.webkit.WebViewFeature;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.Collections;
import org.json.JSONException;
import org.json.JSONObject;
import br.edu.ddm.achadoseperdidos.R;

public class MapPickerDialog extends DialogFragment {
    private static final String ORIGIN = "https://appassets.androidplatform.net";
    private static final String PAGE = ORIGIN + "/assets/map.html";
    private static final String KEY_METADATA = "br.edu.ddm.achadoseperdidos.MAPS_API_KEY";
    private Double latitude, longitude;
    private boolean readOnly, ready;
    private WebView web;
    private TextView status;
    private View retry;
    private AlertDialog mapDialog;
    private int selectionVersion, permissionSelectionVersion;
    private final CurrentLocationSource locationSource = new CurrentLocationSource();
    private final ActivityResultLauncher<String[]> locationPermission = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                if (web == null || readOnly || permissionSelectionVersion != selectionVersion) return;
                if (CurrentLocationSource.hasPermission(requireContext())) locate();
                else showLocationMessage(R.string.map_location_denied);
            });
    private TextView locationStatus;
    private View currentLocation;
    private boolean autoLocationTried, locating;
    private Location pendingLocation;

    public static MapPickerDialog create(Double latitude, Double longitude, boolean readOnly) {
        MapPickerDialog dialog = new MapPickerDialog();
        Bundle args = new Bundle(); args.putBoolean("readOnly", readOnly);
        if (latitude != null && longitude != null) { args.putDouble("lat", latitude); args.putDouble("lng", longitude); }
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull @Override public Dialog onCreateDialog(Bundle saved) {
        readOnly = getArguments() != null && getArguments().getBoolean("readOnly");
        autoLocationTried = saved != null && saved.getBoolean("autoLocationTried");
        Bundle position = saved != null && saved.containsKey("lat") ? saved : getArguments();
        if (position != null && position.containsKey("lat") && position.containsKey("lng")) {
            double lat = position.getDouble("lat"), lng = position.getDouble("lng");
            if (validPosition(lat, lng)) { latitude = lat; longitude = lng; }
        }
        String key = apiKey();
        if (key.isEmpty()) return messageDialog(R.string.map_missing);
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
            return messageDialog(R.string.map_webview_unavailable);
        }
        View content = getLayoutInflater().inflate(R.layout.dialog_map, null);
        status = content.findViewById(R.id.map_status);
        retry = content.findViewById(R.id.map_retry);
        web = content.findViewById(R.id.map_web);
        locationStatus = content.findViewById(R.id.map_location_status);
        currentLocation = content.findViewById(R.id.map_current_location);
        currentLocation.setVisibility(readOnly ? View.GONE : View.VISIBLE);
        currentLocation.setOnClickListener(v -> requestLocation(false));
        android.util.DisplayMetrics metrics = getResources().getDisplayMetrics();
        web.getLayoutParams().height = (int) Math.min(360 * metrics.density, metrics.heightPixels * 0.38);
        mapDialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(readOnly ? R.string.view_map : R.string.map_choose_title).setView(content)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(readOnly ? android.R.string.ok : R.string.confirm_location, (d, which) -> {
                    if (!readOnly && ready && latitude != null) {
                        Bundle result = new Bundle(); result.putDouble("lat", latitude); result.putDouble("lng", longitude);
                        getParentFragmentManager().setFragmentResult("location", result);
                    }
                }).create();
        configureWebView(key);
        retry.setOnClickListener(v -> loadMap());
        return mapDialog;
    }

    @Override public void onStart() {
        super.onStart();
        if (web != null) loadMap();
        if (web != null && !readOnly && latitude == null && !autoLocationTried) {
            autoLocationTried = true;
            requestLocation(true);
        }
    }

    private void requestLocation(boolean automatic) {
        if (web == null || readOnly) return;
        if (CurrentLocationSource.hasPermission(requireContext())) { locate(); return; }
        android.content.SharedPreferences preferences = requireContext()
                .getSharedPreferences("location_permission", Context.MODE_PRIVATE);
        if (automatic && preferences.getBoolean("asked", false)) {
            showLocationMessage(R.string.map_location_denied); return;
        }
        preferences.edit().putBoolean("asked", true).apply();
        permissionSelectionVersion = selectionVersion;
        locationPermission.launch(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION});
    }

    private void locate() {
        if (web == null || readOnly) return;
        pendingLocation = null;
        locating = true;
        currentLocation.setEnabled(false);
        showLocationMessage(R.string.map_locating);
        web.evaluateJavascript("window.beginCurrentLocationRequest && window.beginCurrentLocationRequest();", null);
        locationSource.request(requireContext(), location -> {
            if (web == null || readOnly) return;
            locating = false;
            currentLocation.setEnabled(true);
            if (location == null || !validPosition(location.getLatitude(), location.getLongitude())) {
                showLocationMessage(R.string.map_location_unavailable); return;
            }
            pendingLocation = location;
            if (ready) applyCurrentLocation();
        });
    }

    private void applyCurrentLocation() {
        if (web == null || pendingLocation == null || readOnly) return;
        double lat = pendingLocation.getLatitude(), lng = pendingLocation.getLongitude();
        pendingLocation = null;
        web.evaluateJavascript("window.setCurrentLocation(" + lat + "," + lng + ");", null);
    }

    private void showLocationMessage(int message) {
        if (locationStatus == null) return;
        locationStatus.setText(message);
        locationStatus.setVisibility(View.VISIBLE);
    }

    private void cancelLocation() {
        locationSource.cancel();
        pendingLocation = null;
        locating = false;
        if (currentLocation != null) currentLocation.setEnabled(true);
    }

    private Dialog messageDialog(int message) {
        return new MaterialAlertDialogBuilder(requireContext()).setMessage(message)
                .setPositiveButton(android.R.string.ok, null).create();
    }

    private String apiKey() {
        try {
            Bundle meta = requireContext().getPackageManager()
                    .getApplicationInfo(requireContext().getPackageName(), PackageManager.GET_META_DATA).metaData;
            return meta == null ? "" : meta.getString(KEY_METADATA, "").trim();
        } catch (PackageManager.NameNotFoundException ignored) { return ""; }
    }

    @SuppressLint("SetJavaScriptEnabled") // Página local do mapa; mensagens limitadas à origem e ao frame principal.
    private void configureWebView(String key) {
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setGeolocationEnabled(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(requireContext())).build();
        WebViewCompat.addWebMessageListener(web, "MapHost", Collections.singleton(ORIGIN),
                (view, message, sourceOrigin, isMainFrame, replyProxy) -> {
                    if (web == null || !isMainFrame || !ORIGIN.equals(sourceOrigin.toString())) return;
                    receive(message.getData());
                });
        web.setWebViewClient(new WebViewClient() {
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return loader.shouldInterceptRequest(request.getUrl());
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                // O mapa não navega para páginas externas, inclusive links e iframes do provedor.
                return true;
            }
            @Override public void onPageFinished(WebView view, String url) {
                if (web != view || !PAGE.equals(url)) return;
                try {
                    JSONObject config = new JSONObject().put("key", key).put("readOnly", readOnly)
                            .put("lat", latitude == null ? JSONObject.NULL : latitude)
                            .put("lng", longitude == null ? JSONObject.NULL : longitude);
                    view.evaluateJavascript("window.initializeMap(" + config + ");", null);
                } catch (JSONException ignored) { showError(); }
            }
            @Override public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) showError();
            }
        });
    }

    private void loadMap() {
        ready = false;
        status.setText(R.string.map_loading);
        retry.setVisibility(View.GONE);
        updateConfirmation();
        web.loadUrl(PAGE);
    }

    private void receive(String message) {
        if (message == null || message.length() > 512) return;
        try {
            JSONObject data = new JSONObject(message);
            switch (data.optString("type")) {
                case "ready":
                    ready = true;
                    status.setText(readOnly ? getString(R.string.map_read_only) : latitude == null
                            ? getString(R.string.map_ready) : getString(R.string.location_selected, latitude, longitude));
                    if (locating || pendingLocation != null) {
                        applyCurrentLocation();
                    }
                    break;
                case "selected":
                    double lat = data.getDouble("lat"), lng = data.getDouble("lng");
                    if (readOnly || !ready || !validPosition(lat, lng)) return;
                    latitude = lat; longitude = lng;
                    selectionVersion++;
                    cancelLocation();
                    if ("current".equals(data.optString("source"))) showLocationMessage(R.string.map_location_suggested);
                    else locationStatus.setVisibility(View.GONE);
                    status.setText(getString(R.string.location_selected, latitude, longitude));
                    break;
                case "error": showError(); return;
                default: return;
            }
            updateConfirmation();
        } catch (JSONException ignored) { /* Não aceitar mensagens inválidas como localização. */ }
    }

    private static boolean validPosition(double lat, double lng) {
        return Double.isFinite(lat) && Double.isFinite(lng) && Math.abs(lat) <= 90 && Math.abs(lng) <= 180;
    }

    private void showError() {
        if (web == null) return;
        ready = false;
        status.setText(R.string.map_error);
        retry.setVisibility(View.VISIBLE);
        updateConfirmation();
    }

    private void updateConfirmation() {
        if (mapDialog != null && mapDialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
            mapDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(readOnly || (ready && latitude != null));
        }
    }

    @Override public void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        out.putBoolean("autoLocationTried", autoLocationTried && !locating && pendingLocation == null);
        if (latitude != null) { out.putDouble("lat", latitude); out.putDouble("lng", longitude); }
    }

    @Override public void onDestroyView() {
        cancelLocation();
        if (web != null) {
            WebViewCompat.removeWebMessageListener(web, "MapHost");
            web.stopLoading();
            if (web.getParent() instanceof ViewGroup) ((ViewGroup) web.getParent()).removeView(web);
            web.destroy(); web = null;
        }
        mapDialog = null;
        super.onDestroyView();
    }

    @Override public void onStop() {
        if ((locating || pendingLocation != null) && latitude == null) autoLocationTried = false;
        if (locating) showLocationMessage(R.string.map_location_unavailable);
        cancelLocation();
        super.onStop();
    }
}
