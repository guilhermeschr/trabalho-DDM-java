package br.edu.ddm.achadoseperdidos.ui;
import android.content.Context;
import android.graphics.*;
import android.net.Uri;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.exifinterface.media.ExifInterface;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import br.edu.ddm.achadoseperdidos.catalog.ObjectPhoto;
public class PhotoImagesTest {
    @Test public void compressaoOrientacaoERemocaoDeMetadados() throws Exception {
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        File file=File.createTempFile("photo-test", ".jpg",context.getCacheDir());
        try {
            Bitmap bitmap=Bitmap.createBitmap(2400,1200,Bitmap.Config.ARGB_8888); bitmap.eraseColor(Color.BLUE);
            try(FileOutputStream out=new FileOutputStream(file)) { bitmap.compress(Bitmap.CompressFormat.JPEG,95,out); } bitmap.recycle();
            ExifInterface exif=new ExifInterface(file); exif.setAttribute(ExifInterface.TAG_ORIENTATION,"6"); exif.setLatLong(-23,-46); exif.saveAttributes();
            ObjectPhoto photo=PhotoImages.prepare(context,Uri.fromFile(file)); assertTrue(photo.valid());
            byte[] bytes=android.util.Base64.decode(photo.base64,0); Bitmap prepared=BitmapFactory.decodeByteArray(bytes,0,bytes.length);
            assertTrue(prepared.getHeight()>prepared.getWidth()); assertTrue(prepared.getHeight()<=1280); prepared.recycle();
            ExifInterface result=new ExifInterface(new ByteArrayInputStream(bytes)); assertNull(result.getLatLong());
        } finally { file.delete(); }
    }
    @Test public void cameraRecebeUriPrivadaECancelamentoNaoConfirma() throws Exception {
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext(); File dir=new File(context.getCacheDir(),"object_photos"); dir.mkdirs(); File file=File.createTempFile("capture-test", ".jpg",dir);
        try {
            Uri uri=androidx.core.content.FileProvider.getUriForFile(context,context.getPackageName()+".fileprovider",file);
            assertEquals("content",uri.getScheme());
            androidx.activity.result.contract.ActivityResultContracts.TakePicture contract=new androidx.activity.result.contract.ActivityResultContracts.TakePicture();
            assertFalse(contract.parseResult(android.app.Activity.RESULT_CANCELED,null));
            assertEquals(uri,contract.createIntent(context,uri).getParcelableExtra(android.provider.MediaStore.EXTRA_OUTPUT));
        } finally {file.delete();}
    }
    @Test public void arquivoInvalidoNaoViraAnexo() throws Exception {
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext(); File file=File.createTempFile("invalid", ".jpg",context.getCacheDir());
        try { PhotoImages.prepare(context,Uri.fromFile(file)); fail("Deveria rejeitar imagem vazia"); } catch(IOException expected) {} finally {file.delete();}
    }
}
