package id.kos.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

// Format angka, rupiah, dan tanggal, Pemisah ribuan dan desimal ditentukan di sini,
 
public class Format {
    private static final DateTimeFormatter FORMAT_TANGGAL = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private static DecimalFormat pola(String pola) {
        DecimalFormatSymbols simbol = new DecimalFormatSymbols(Locale.ROOT);
        simbol.setGroupingSeparator('.');
        simbol.setDecimalSeparator(',');
        DecimalFormat f = new DecimalFormat(pola, simbol);
        f.setRoundingMode(RoundingMode.HALF_UP);
        return f;
    }

    public static String rupiah(BigDecimal nilai) {
        if (nilai == null) return "Rp0";
        String angka = pola("#,##0").format(nilai.abs());
        return (nilai.signum() < 0 ? "-Rp" : "Rp") + angka;
    }

    public static String rupiah(double nilai) {
        return rupiah(BigDecimal.valueOf(nilai));
    }

    public static String angka(BigDecimal nilai) {
        return nilai == null ? "0" : pola("#,##0.##").format(nilai);
    }

    public static String angka(double nilai) {
        return pola("#,##0.##").format(nilai);
    }

    public static BigDecimal desimal(String teks, String namaKolom) {
        if (teks == null || teks.trim().isEmpty()) {
            throw new IllegalArgumentException(namaKolom + " wajib diisi.");
        }
        try {
            String bersih = teks.trim().replace(".", "").replace(",", ".");
            return new BigDecimal(bersih);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Format " + namaKolom + " harus berupa angka yang valid.");
        }
    }

    public static String tanggal(LocalDate tgl) {
        if (tgl == null) {
            return "-";
        }
        return tgl.format(FORMAT_TANGGAL);
    }

    public static LocalDate tanggal(String teks, String namaKolom) {
        if (teks == null || teks.trim().isEmpty()) {
            throw new IllegalArgumentException(namaKolom + " wajib diisi.");
        }
        try {
            return LocalDate.parse(teks.trim(), FORMAT_TANGGAL);
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(teks.trim());
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("Format " + namaKolom + " tidak valid (gunakan dd-MM-yyyy atau yyyy-MM-dd).");
            }
        }
    }
}