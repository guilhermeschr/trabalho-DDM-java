package br.edu.ddm.achadoseperdidos.catalog;
/** JPEG preparado e imutável; o Base64 não inclui quebras de linha. */
public final class ObjectPhoto {
    public static final int MAX_BYTES = 102400, MAX_COUNT = 5;
    public final String base64, thumbnail;
    public ObjectPhoto(String base64, String thumbnail) { this.base64 = base64; this.thumbnail = thumbnail; }
    public boolean valid() { return validBase64(base64, MAX_BYTES) && validBase64(thumbnail, 12288); }
    private static boolean validBase64(String text, int limit) {
        try { if (text == null || text.isEmpty() || text.length()%4 != 0 || text.length() > ((limit+2)/3)*4 || !text.matches("[A-Za-z0-9+/]*={0,2}")) return false;
            int padding = text.endsWith("==") ? 2 : text.endsWith("=") ? 1 : 0;
            return text.length()/4*3-padding <= limit; }
        catch (RuntimeException e) { return false; }
    }
}
