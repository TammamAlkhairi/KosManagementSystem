package id.kos.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class Format {
    private static final Locale LOKAL_ID = Locale.of("id", "ID");
    private static final NumberFormat FORMAT_RUPIAH = NumberFormat.getCurrencyInstance(LOKAL_ID);
    private static final NumberFormat FORMAT_ANGKA = NumberFormat.getNumberInstance(LOKAL_ID);
    private static final DateTimeFormatter FORMAT_TANGGAL = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public static String rupiah(BigDecimal nilai) {
        if (nilai == null) {
            return "Rp0";
        }
        return FORMAT_RUPIAH.format(nilai);
    }

    public static String rupiah(double nilai) {
        return FORMAT_RUPIAH.format(nilai);
    }

    public static String angka(BigDecimal nilai) {
        if (nilai == null) {
            return "0";
        }
        return FORMAT_ANGKA.format(nilai);
    }

    public static String angka(double nilai) {
        return FORMAT_ANGKA.format(nilai);
    }public static BigDecimal desimal(String teks, String namaKolom) {
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