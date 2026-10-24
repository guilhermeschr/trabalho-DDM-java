package br.edu.ddm.achadoseperdidos.ui;
import android.app.Dialog;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import br.edu.ddm.achadoseperdidos.R;
public class MapPickerDialog extends DialogFragment {
    private static final int MAP_ID = 0x123456;
    private LatLng selected;
    @NonNull @Override public Dialog onCreateDialog(Bundle saved) {
        if (saved != null && saved.containsKey("lat")) selected = new LatLng(saved.getDouble("lat"),saved.getDouble("lng"));
        boolean configured = false;
        try {
            android.os.Bundle meta = requireContext().getPackageManager().getApplicationInfo(requireContext().getPackageName(), PackageManager.GET_META_DATA).metaData;
            configured = meta != null && !meta.getString("com.google.android.geo.API_KEY", "").isEmpty();
        } catch (PackageManager.NameNotFoundException ignored) { }
        if (!configured) return new MaterialAlertDialogBuilder(requireContext()).setMessage(R.string.map_missing).setPositiveButton(android.R.string.ok,null).create();
        FrameLayout frame = new FrameLayout(requireContext()); frame.setId(MAP_ID);
        frame.setMinimumHeight((int)(360 * getResources().getDisplayMetrics().density));
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.map_title).setView(frame)
            .setNegativeButton(android.R.string.cancel,null).setPositiveButton(R.string.confirm_location, (d,w) -> {
                if (selected == null) return;
                Bundle result = new Bundle(); result.putDouble("lat", selected.latitude); result.putDouble("lng", selected.longitude);
                getParentFragmentManager().setFragmentResult("location", result);
            }).create();
        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(selected != null);
            SupportMapFragment map = (SupportMapFragment)getChildFragmentManager().findFragmentByTag("map");
            if (map == null) { map = SupportMapFragment.newInstance(); getChildFragmentManager().beginTransaction().replace(MAP_ID,map,"map").commitNow(); }
            map.getMapAsync(google -> {
                google.moveCamera(CameraUpdateFactory.newLatLngZoom(selected == null ? new LatLng(-15.8,-47.9) : selected, selected == null ? 4 : 15));
                if (selected != null) google.addMarker(new MarkerOptions().position(selected));
                google.setOnMapClickListener(position -> {
                    selected = position; google.clear(); google.addMarker(new MarkerOptions().position(position));
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                });
            });
        });
        return dialog;
    }
    @Override public void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        if (selected != null) { out.putDouble("lat",selected.latitude); out.putDouble("lng",selected.longitude); }
    }
}
