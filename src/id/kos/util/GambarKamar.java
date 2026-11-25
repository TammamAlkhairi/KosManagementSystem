package id.kos.util;

import id.kos.model.Kamar;
import java.awt.RenderingHints;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
 
public final class GambarKamar {
    private static final String FOLDER = "images";
    private static final Map<String, BufferedImage> ASLI = new HashMap<>();
    private static final Map<String, BufferedImage> KECIL = new HashMap<>();
    private static final Map<String, Long> GAGAL = new HashMap<>();
    private static final long JEDA_CARI_ULANG_MS = 3000;

    private GambarKamar() { }

    public static BufferedImage untuk(Kamar kamar) {
        return kamar == null ? null : muat(kamar.getNamaGambar());
    }

    // Foto yang sudah diperkecil disimpan di cache.
    public static BufferedImage untuk(Kamar kamar, int lebar, int tinggi) {
        BufferedImage asli = untuk(kamar);
        if (asli == null || lebar <= 0 || tinggi <= 0) return null;
        double skala = Math.max((double) lebar / asli.getWidth(), (double) tinggi / asli.getHeight());
        if (skala >= 1) return asli;
        int w = Math.max(1, (int) Math.ceil(asli.getWidth() * skala));
        int h = Math.max(1, (int) Math.ceil(asli.getHeight() * skala));
        String kunci = kamar.getNamaGambar() + "@" + w + "x" + h;
        synchronized (KECIL) {
            BufferedImage hasil = KECIL.get(kunci);
            if (hasil == null) {
                hasil = perkecil(asli, w, h);
                KECIL.put(kunci, hasil);
            }
            return hasil;
        }
    }

    // Memperkecil foto bertahap
    private static BufferedImage perkecil(BufferedImage src, int w, int h) {
        BufferedImage cur = src;
        int cw = src.getWidth(), ch = src.getHeight();
        while (cw > w || ch > h) {
            cw = Math.max(w, cw / 2);
            ch = Math.max(h, ch / 2);
            BufferedImage next = new BufferedImage(cw, ch, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = next.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(cur, 0, 0, cw, ch, null);
            g.dispose();
            cur = next;
        }
        return cur;
    }

    public static synchronized BufferedImage muat(String namaBerkas) {
        if (namaBerkas == null) return null;
        BufferedImage ada = ASLI.get(namaBerkas);
        if (ada != null) return ada;
        Long gagalPada = GAGAL.get(namaBerkas);
        if (gagalPada != null && System.currentTimeMillis() - gagalPada < JEDA_CARI_ULANG_MS) return null;
        BufferedImage img = dariClasspath(namaBerkas);
        if (img == null) img = dariFile(new File(FOLDER, namaBerkas));
        if (img == null) {
            for (File dasar : lokasiKode()) {
                for (File f = dasar; f != null; f = f.getParentFile()) {
                    img = dariFile(new File(new File(f, FOLDER), namaBerkas));
                    
                    File[] anak = img == null ? f.listFiles(File::isDirectory) : null;
                    if (anak != null) {
                        for (File a : anak) {
                            img = dariFile(new File(new File(a, FOLDER), namaBerkas));
                            if (img != null) break;
                        }
                    }
                    if (img != null) break;
                }
                if (img != null) break;
            }
        }
        if (img != null) { ASLI.put(namaBerkas, img); GAGAL.remove(namaBerkas); }
        else GAGAL.put(namaBerkas, System.currentTimeMillis());
        return img;
    }

    private static BufferedImage dariClasspath(String nama) {
        try (InputStream in = GambarKamar.class.getResourceAsStream("/" + FOLDER + "/" + nama)) {
            return in == null ? null : ImageIO.read(in);
        } catch (IOException e) {
            return null;
        }
    }

    private static BufferedImage dariFile(File f) {
        try {
            return f.isFile() ? ImageIO.read(f) : null;
        } catch (IOException e) {
            return null;
        }
    }

    private static File[] lokasiKode() {
        try {
            File f = new File(GambarKamar.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            File dir = f.isFile() ? f.getParentFile() : f;
            return new File[]{dir, new File(System.getProperty("user.dir", "."))};
        } catch (URISyntaxException | RuntimeException e) {
            return new File[]{new File(System.getProperty("user.dir", "."))};
        }
    }
}
