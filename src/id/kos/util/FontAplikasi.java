package id.kos.util;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Menyiapkan font Manrope. Urutan: Manrope yang sudah terpasang di sistem, lalu berkas
 * Manrope-Regular.ttf dan Manrope-Bold.ttf di folder fonts/ (dicari dengan cara yang sama seperti folder images/).
 * Bila keduanya tidak ada, pemanggil memakai font cadangan.
 */
public final class FontAplikasi {
    public static final String NAMA = "Manrope";
    private static final String[] BERKAS = {"Manrope-Regular.ttf", "Manrope-Bold.ttf"};

    private FontAplikasi() { }

    /** Mengembalikan nama family Manrope bila tersedia, selain itu null. */
    public static String siapkan() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Set<String> terpasang = new HashSet<>(Arrays.asList(ge.getAvailableFontFamilyNames()));
        if (terpasang.contains(NAMA)) return NAMA;

        File folder = cariFolderFont();
        if (folder == null) return null;
        String family = null;
        for (String nama : BERKAS) {
            File f = new File(folder, nama);
            if (!f.isFile()) continue;
            try {
                Font font = Font.createFont(Font.TRUETYPE_FONT, f);
                ge.registerFont(font);
                if (family == null) family = font.getFamily();
            } catch (FontFormatException | IOException e) {
                System.err.println("Font tidak bisa dibaca: " + f + " (" + e.getMessage() + ")");
            }
        }
        return family;
    }

    private static File cariFolderFont() {
        List<File> awal = new ArrayList<>();
        awal.add(new File(System.getProperty("user.dir", ".")).getAbsoluteFile());
        try {
            File kode = new File(FontAplikasi.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            awal.add(kode.isFile() ? kode.getParentFile() : kode);
        } catch (URISyntaxException | RuntimeException ignored) { }
        for (File dasar : awal) {
            for (File f = dasar; f != null; f = f.getParentFile()) {
                File langsung = new File(f, "fonts");
                if (ada(langsung)) return langsung;
                File[] anak = f.listFiles(File::isDirectory);   // folder induk project yang dibuka di VS Code
                if (anak != null) {
                    for (File a : anak) {
                        File dalam = new File(a, "fonts");
                        if (ada(dalam)) return dalam;
                    }
                }
            }
        }
        return null;
    }

    private static boolean ada(File folder) {
        return new File(folder, BERKAS[0]).isFile() || new File(folder, BERKAS[1]).isFile();
    }
}
