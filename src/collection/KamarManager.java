package collection;

import model.Kamar;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KamarManager {
    private final List<Kamar> daftarKamar = new ArrayList<>();
    private final Map<String, Kamar> indeksNomor = new HashMap<>();

    public void setData(List<Kamar> data) {
        daftarKamar.clear();
        indeksNomor.clear();
        for (Kamar kamar : data) {
            daftarKamar.add(kamar);
            indeksNomor.put(kamar.getNomorKamar(), kamar);
        }
    }

    public void tambah(Kamar kamar) {
        daftarKamar.add(kamar);
        indeksNomor.put(kamar.getNomorKamar(), kamar);
    }

    public Kamar cari(String nomor) {
        return indeksNomor.get(nomor);
    }

    public List<Kamar> getDaftarKamar() {
        return new ArrayList<>(daftarKamar);
    }
}
