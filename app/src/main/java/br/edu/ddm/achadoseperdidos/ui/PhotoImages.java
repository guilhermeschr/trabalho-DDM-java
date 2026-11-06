package br.edu.ddm.achadoseperdidos.ui;
import android.graphics.*;
import android.net.Uri;
import android.content.Context;
import android.widget.ImageView;
import android.view.View;
import androidx.exifinterface.media.ExifInterface;
import br.edu.ddm.achadoseperdidos.catalog.ObjectPhoto;
import java.io.*;
import java.util.concurrent.*;

public final class PhotoImages {
    static final ExecutorService WORK = Executors.newSingleThreadExecutor();
    private PhotoImages() {}
    static String encode(Bitmap source, int limit) throws IOException {
        Bitmap bitmap = source;
        try {
            for (;;) {
                for (int quality=85; quality>=45; quality-=10) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
                    if (out.size() <= limit) return android.util.Base64.encodeToString(out.toByteArray(), android.util.Base64.NO_WRAP);
                }
                if (bitmap.getWidth() <= 32 || bitmap.getHeight() <= 32) throw new IOException("Imagem muito complexa");
                Bitmap next = Bitmap.createScaledBitmap(bitmap, Math.max(1, bitmap.getWidth()*3/4), Math.max(1, bitmap.getHeight()*3/4), true);
                if (bitmap != source) bitmap.recycle(); bitmap = next;
            }
        } finally { if (bitmap != source) bitmap.recycle(); }
    }
    static ObjectPhoto prepare(Context context, Uri uri) throws IOException {
        BitmapFactory.Options opts = new BitmapFactory.Options(); opts.inJustDecodeBounds = true;
        try (InputStream in = context.getContentResolver().openInputStream(uri)) { BitmapFactory.decodeStream(in, null, opts); }
        if (opts.outWidth <= 0 || opts.outHeight <= 0) throw new IOException("Imagem inválida");
        opts.inJustDecodeBounds = false; opts.inSampleSize = 1;
        while (Math.max(opts.outWidth, opts.outHeight)/opts.inSampleSize > 2560) opts.inSampleSize *= 2;
        Bitmap bitmap;
        try (InputStream in = context.getContentResolver().openInputStream(uri)) { bitmap = BitmapFactory.decodeStream(in, null, opts); }
        if (bitmap == null) throw new IOException("Imagem inválida");
        try {
            try (InputStream in = context.getContentResolver().openInputStream(uri)) {
                ExifInterface exif = new ExifInterface(in); Matrix matrix = new Matrix();
                if (exif.isFlipped()) matrix.postScale(-1, 1); matrix.postRotate(exif.getRotationDegrees());
                Bitmap oriented = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                if (oriented != bitmap) { bitmap.recycle(); bitmap = oriented; }
            }
            float scale = Math.min(1f, 1280f/Math.max(bitmap.getWidth(), bitmap.getHeight()));
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, Math.max(1, Math.round(bitmap.getWidth()*scale)), Math.max(1, Math.round(bitmap.getHeight()*scale)), true);
            if (scaled != bitmap) { bitmap.recycle(); bitmap = scaled; }
            String full = encode(bitmap, ObjectPhoto.MAX_BYTES);
            float small = Math.min(1f, 240f/Math.max(bitmap.getWidth(), bitmap.getHeight()));
            Bitmap thumb = Bitmap.createScaledBitmap(bitmap, Math.max(1, Math.round(bitmap.getWidth()*small)), Math.max(1, Math.round(bitmap.getHeight()*small)), true);
            try { return new ObjectPhoto(full, encode(thumb, 12288)); } finally { if (thumb != bitmap) thumb.recycle(); }
        } finally { bitmap.recycle(); }
    }
    static void bind(ImageView view, String data) {
        Object token = new Object(); view.setTag(token); view.setImageDrawable(null);
        view.setVisibility(data == null || data.isEmpty() ? View.GONE : View.VISIBLE);
        if (data == null || data.isEmpty()) return;
        WORK.execute(() -> {
            Bitmap bitmap = null;
            try { byte[] bytes = android.util.Base64.decode(data, android.util.Base64.DEFAULT); if (bytes.length <= 102400) {
                BitmapFactory.Options bounds = new BitmapFactory.Options(); bounds.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(bytes,0,bytes.length,bounds); bounds.inJustDecodeBounds = false; bounds.inSampleSize = 1;
                while (Math.max(bounds.outWidth,bounds.outHeight)/bounds.inSampleSize>1280) bounds.inSampleSize *= 2;
                bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length,bounds);
            } } catch (RuntimeException | OutOfMemoryError ignored) {}
            Bitmap result = bitmap;
            view.post(() -> { if (view.getTag() == token) { view.setImageBitmap(result); if (result == null) { view.setContentDescription("Foto indisponível"); view.setImageResource(android.R.drawable.ic_menu_report_image); } } });
        });
    }
}
