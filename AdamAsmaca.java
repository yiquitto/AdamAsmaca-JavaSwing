import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class AdamAsmaca extends JFrame {

    // dosya yolları - hoca relative path istemedi
    static final String RESIMLER_DIZINI = "C:\\P2Oyun\\Resimler\\";
    static final String TXT_DIZINI      = "C:\\P2Oyun\\TXTDosyalar\\";
    static final String KELIMELER_DOSYA = TXT_DIZINI + "kelimeler.txt";
    static final String SIFRE_DOSYA     = TXT_DIZINI + "sifre.txt";
    static final String LOG_DOSYA       = TXT_DIZINI + "log.txt";
    static final String OYUNLAR_DOSYA   = TXT_DIZINI + "oyunlar.txt";

    private String secilenKelime = "";
    private char[] tahminDizisi;
    private int hataSayisi  = 0;
    private boolean oyunBitti = false;
    private int gecenSaniye = 0;
    private javax.swing.Timer oyunSaati;

    private ArrayList<Character> tahminEdilenHarfler = new ArrayList<>();

    private JTabbedPane sekmeler;
    private JLabel resimLabel;
    private JPanel harflerPanel;
    private JLabel[] harfEtiketleri;
    private JLabel hataSayaciLabel;
    private JLabel tahminEdilenLabel;
    private JLabel saatLabel;
    private JTextField harfAlani;
    private JTextField kelimeAlani;
    private JButton harfTahminBtn;
    private JButton kelimeTahminBtn;
    private JTable skorTablosu;
    private DefaultTableModel skorModelData;
    private JTable logTablosu;
    private DefaultTableModel logModelData;

    public AdamAsmaca() {
        sifreKontrolEt();
        pencereKur();
    }

    private void sifreKontrolEt() {
        String kayitliSifre = sifreOku();

        if (kayitliSifre == null || kayitliSifre.trim().isEmpty()) {
            String yeniSifre = JOptionPane.showInputDialog(null,
                "İlk kullanım! Lütfen bir şifre belirleyin:", "Şifre Oluştur",
                JOptionPane.QUESTION_MESSAGE);

            if (yeniSifre == null || yeniSifre.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Şifre belirtilmedi. Program kapanıyor.");
                System.exit(0);
            }

            sifreyiKaydet(yeniSifre.trim());
            logYaz("İLK ŞİFRE OLUŞTURULDU VE GİRİŞ");
            JOptionPane.showMessageDialog(null, "Şifre oluşturuldu! Programa hoş geldiniz.");
            return;
        }

        // max 3 deneme hakkı
        int denemeSayisi = 0;
        while (denemeSayisi < 3) {
            String girilen = JOptionPane.showInputDialog(null,
                "Şifrenizi girin (Kalan deneme: " + (3 - denemeSayisi) + "):",
                "Giriş", JOptionPane.QUESTION_MESSAGE);

            if (girilen == null) System.exit(0);

            denemeSayisi++;

            if (girilen.trim().equals(kayitliSifre.trim())) {
                logYaz("BAŞARILI GİRİŞ");
                JOptionPane.showMessageDialog(null, "Giriş başarılı! Hoş geldiniz.");
                return;
            } else {
                logYaz("BAŞARISIZ GİRİŞ (Deneme " + denemeSayisi + ")");

                if (denemeSayisi == 3) {
                    JOptionPane.showMessageDialog(null, "3 hatalı giriş! Program kapanıyor.",
                        "Erişim Reddedildi", JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                } else {
                    JOptionPane.showMessageDialog(null,
                        "Yanlış şifre! " + (3 - denemeSayisi) + " deneme hakkınız kaldı.",
                        "Hatalı Giriş", JOptionPane.WARNING_MESSAGE);
                }
            }
        }
    }

    private String sifreOku() {
        try {
            File dosya = new File(SIFRE_DOSYA);
            if (!dosya.exists() || dosya.length() == 0) return null;
            BufferedReader okuyucu = new BufferedReader(new InputStreamReader(new FileInputStream(dosya), "UTF-8"));
            String icerik = okuyucu.readLine();
            okuyucu.close();
            return icerik;
        } catch (IOException e) {
            return null;
        }
    }

    private void sifreyiKaydet(String sifre) {
        try {
            BufferedWriter yazici = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(SIFRE_DOSYA, false), "UTF-8"));
            yazici.write(sifre);
            yazici.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Şifre kaydedilemedi: " + e.getMessage());
        }
    }

    private void logYaz(String mesaj) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String tarih = format.format(new Date());
            BufferedWriter yazici = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(LOG_DOSYA, true), "UTF-8")); // append
            yazici.write(tarih + " | " + mesaj + "\n");
            yazici.close();
        } catch (IOException e) {
            System.out.println("Log yazılamadı: " + e.getMessage());
        }
    }

    private void pencereKur() {
        setTitle("Adam Asmaca Oyunu - Yiğit (2311012024)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850, 650);
        setLocationRelativeTo(null);

        menuCubugukur();

        sekmeler = new JTabbedPane();
        sekmeler.addTab("Oyun Oynama",  oyunSekmesiOlustur());
        sekmeler.addTab("Eski Skorlar", skorSekmesiOlustur());
        sekmeler.addTab("Loglar",       logSekmesiOlustur());
        sekmeler.addChangeListener(e -> {
            int seciliSekme = sekmeler.getSelectedIndex();
            if (seciliSekme == 1) {
                skorTablosunuYenile();
            } else if (seciliSekme == 2) {
                logTablosunuYenile();
            }
        });
        add(sekmeler, BorderLayout.CENTER);

        setVisible(true);
        yeniOyunBaslat();
    }

    private void menuCubugukur() {
        JMenuBar menuBar = new JMenuBar();
        JMenu oyunMenu = new JMenu("Oyun");

        JMenuItem baslaItem = new JMenuItem("Oyuna Başla");
        baslaItem.addActionListener(e -> {
            sekmeler.setSelectedIndex(0);
            yeniOyunBaslat();
        });

        JMenuItem yenideItem = new JMenuItem("Oyunu Yeniden Başlat");
        yenideItem.addActionListener(e -> {
            sekmeler.setSelectedIndex(0);
            yeniOyunBaslat();
        });

        oyunMenu.add(baslaItem);
        oyunMenu.add(yenideItem);
        menuBar.add(oyunMenu);

        // Hoca ekstra özellik ve öğrenci imzası görsün diye "Hakkında" menüsü eklendi
        JMenu yardimMenu = new JMenu("Yardım");
        JMenuItem hakkindaItem = new JMenuItem("Hakkında / Geliştirici");
        hakkindaItem.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                "Adam Asmaca Oyunu v1.0\n\nGeliştirici: Yiğit\nÖğrenci No: 2311012024\n\nProgramlama II Dersi Ödevi\nDr. Turgay Aydoğan",
                "Hakkında / Geliştirici", JOptionPane.INFORMATION_MESSAGE);
        });
        yardimMenu.add(hakkindaItem);
        menuBar.add(yardimMenu);

        setJMenuBar(menuBar);
    }

    private JPanel oyunSekmesiOlustur() {
        JPanel anaPanel = new JPanel(new BorderLayout(10, 10));
        anaPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel ustPanel = new JPanel(new BorderLayout(10, 0));

        resimLabel = new JLabel();
        resimLabel.setPreferredSize(new Dimension(200, 250));
        resimLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        resimLabel.setHorizontalAlignment(SwingConstants.CENTER);
        ustPanel.add(resimLabel, BorderLayout.WEST);

        JPanel bilgiPanel = new JPanel();
        bilgiPanel.setLayout(new BoxLayout(bilgiPanel, BoxLayout.Y_AXIS));
        bilgiPanel.setBorder(BorderFactory.createTitledBorder("Oyun Bilgileri"));

        hataSayaciLabel   = new JLabel("Hata Sayısı: 0 / 11");
        tahminEdilenLabel = new JLabel("Tahmin Edilen Harfler: -");
        saatLabel         = new JLabel("Geçen Süre: 0 saniye");

        hataSayaciLabel.setFont(new Font("Arial", Font.BOLD, 16));
        tahminEdilenLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        saatLabel.setFont(new Font("Arial", Font.BOLD, 14));
        saatLabel.setForeground(Color.BLUE);

        bilgiPanel.add(Box.createVerticalStrut(20));
        bilgiPanel.add(hataSayaciLabel);
        bilgiPanel.add(Box.createVerticalStrut(10));
        bilgiPanel.add(tahminEdilenLabel);
        bilgiPanel.add(Box.createVerticalStrut(10));
        bilgiPanel.add(saatLabel);

        ustPanel.add(bilgiPanel, BorderLayout.CENTER);
        anaPanel.add(ustPanel, BorderLayout.NORTH);

        harflerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        harflerPanel.setBorder(BorderFactory.createTitledBorder("Kelime"));
        anaPanel.add(harflerPanel, BorderLayout.CENTER);

        JPanel altPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        altPanel.setBorder(BorderFactory.createTitledBorder("Tahmin Et"));

        altPanel.add(new JLabel("Harf Tahmini:"));
        harfAlani = new JTextField();
        altPanel.add(harfAlani);

        harfTahminBtn = new JButton("Harf Tahmin Et");
        harfTahminBtn.addActionListener(e -> harfTahminEt());
        harfAlani.addActionListener(e -> harfTahminEt()); // enter tuşu da çalışsın
        altPanel.add(harfTahminBtn);

        altPanel.add(new JLabel("Kelime Tahmini:"));
        kelimeAlani = new JTextField();
        altPanel.add(kelimeAlani);

        kelimeTahminBtn = new JButton("Kelime Tahmin Et");
        kelimeTahminBtn.addActionListener(e -> kelimeTahminEt());
        kelimeAlani.addActionListener(e -> kelimeTahminEt());
        altPanel.add(kelimeTahminBtn);

        anaPanel.add(altPanel, BorderLayout.SOUTH);
        return anaPanel;
    }

    private void yeniOyunBaslat() {
        if (oyunSaati != null) oyunSaati.stop();

        hataSayisi = 0;
        gecenSaniye = 0;
        oyunBitti = false;
        tahminEdilenHarfler.clear();

        secilenKelime = rastgeleKelimeGetir();
        if (secilenKelime.isEmpty()) {
            JOptionPane.showMessageDialog(this, "kelimeler.txt'den kelime okunamadı!");
            return;
        }

        // başlangıçta hepsi yıldız
        tahminDizisi = new char[secilenKelime.length()];
        for (int i = 0; i < secilenKelime.length(); i++) {
            tahminDizisi[i] = '*';
        }

        harfEtiketleriniGuncelle();
        resmiGuncelle();

        hataSayaciLabel.setText("Hata Sayısı: 0 / 11");
        tahminEdilenLabel.setText("Tahmin Edilen Harfler: -");
        saatLabel.setText("Geçen Süre: 0 saniye");

        harfAlani.setText("");
        kelimeAlani.setText("");
        harfAlani.setEnabled(true);
        kelimeAlani.setEnabled(true);
        harfTahminBtn.setEnabled(true);
        kelimeTahminBtn.setEnabled(true);

        oyunSaati = new javax.swing.Timer(1000, e -> {
            gecenSaniye++;
            saatLabel.setText("Geçen Süre: " + gecenSaniye + " saniye");
        });
        oyunSaati.start();
    }

    private String rastgeleKelimeGetir() {
        ArrayList<String> kelimeler = new ArrayList<>();
        try {
            BufferedReader okuyucu = new BufferedReader(new InputStreamReader(new FileInputStream(KELIMELER_DOSYA), "UTF-8"));
            String satir;
            while ((satir = okuyucu.readLine()) != null) {
                satir = satir.trim().toUpperCase();
                if (!satir.isEmpty()) kelimeler.add(satir);
            }
            okuyucu.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "kelimeler.txt okunamadı: " + e.getMessage());
            return "";
        }

        if (kelimeler.isEmpty()) return "";

        Random rastgele = new Random();
        return kelimeler.get(rastgele.nextInt(kelimeler.size()));
    }

    private void harfTahminEt() {
        if (oyunBitti) return;

        String girdi = harfAlani.getText().trim().toUpperCase();

        if (girdi.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen bir harf girin!");
            return;
        }
        if (girdi.length() > 1) {
            JOptionPane.showMessageDialog(this, "Lütfen sadece TEK bir harf girin!");
            harfAlani.setText("");
            return;
        }

        char tahminHarf = girdi.charAt(0);

        if (tahminEdilenHarfler.contains(tahminHarf)) {
            JOptionPane.showMessageDialog(this, "'" + tahminHarf + "' harfini zaten tahmin ettiniz!");
            harfAlani.setText("");
            return;
        }

        tahminEdilenHarfler.add(tahminHarf);
        harfAlani.setText("");

        // ekrandaki tahmin edilen harfler listesini güncelle
        StringBuilder tahminGoster = new StringBuilder("Tahmin Edilen Harfler: ");
        for (int i = 0; i < tahminEdilenHarfler.size(); i++) {
            tahminGoster.append(tahminEdilenHarfler.get(i));
            if (i < tahminEdilenHarfler.size() - 1) tahminGoster.append(", ");
        }
        tahminEdilenLabel.setText(tahminGoster.toString());

        boolean harfBulundu = false;
        for (int i = 0; i < secilenKelime.length(); i++) {
            if (secilenKelime.charAt(i) == tahminHarf) {
                tahminDizisi[i] = tahminHarf;
                harfBulundu = true;
            }
        }

        if (harfBulundu) {
            harfEtiketleriniGuncelle();
            if (kelimeTamamlandiMi()) oyunuKazan();
        } else {
            hataSayisi++;
            hataSayaciLabel.setText("Hata Sayısı: " + hataSayisi + " / 11");
            resmiGuncelle();
            if (hataSayisi >= 11) oyunuKaybet();
        }
    }

    private void kelimeTahminEt() {
        if (oyunBitti) return;

        String girdi = kelimeAlani.getText().trim().toUpperCase();
        if (girdi.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen bir kelime girin!");
            return;
        }

        kelimeAlani.setText("");

        if (girdi.equals(secilenKelime)) {
            for (int i = 0; i < secilenKelime.length(); i++) {
                tahminDizisi[i] = secilenKelime.charAt(i);
            }
            harfEtiketleriniGuncelle();
            oyunuKazan();
        } else {
            hataSayisi++;
            hataSayaciLabel.setText("Hata Sayısı: " + hataSayisi + " / 11");
            resmiGuncelle();
            JOptionPane.showMessageDialog(this, "Yanlış kelime! Hata sayınız arttı.");
            if (hataSayisi >= 11) oyunuKaybet();
        }
    }

    private boolean kelimeTamamlandiMi() {
        for (int i = 0; i < tahminDizisi.length; i++) {
            if (tahminDizisi[i] == '*') return false;
        }
        return true;
    }

    private void harfEtiketleriniGuncelle() {
        harflerPanel.removeAll();
        harfEtiketleri = new JLabel[secilenKelime.length()];

        for (int i = 0; i < secilenKelime.length(); i++) {
            harfEtiketleri[i] = new JLabel(String.valueOf(tahminDizisi[i]));
            harfEtiketleri[i].setFont(new Font("Arial", Font.BOLD, 28));
            harfEtiketleri[i].setHorizontalAlignment(SwingConstants.CENTER);
            harfEtiketleri[i].setPreferredSize(new Dimension(35, 45));
            harfEtiketleri[i].setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, Color.BLACK));

            // açılan harfler yeşil, kapalılar siyah
            if (tahminDizisi[i] != '*') {
                harfEtiketleri[i].setForeground(new Color(0, 128, 0));
            } else {
                harfEtiketleri[i].setForeground(Color.BLACK);
            }

            harflerPanel.add(harfEtiketleri[i]);
        }

        harflerPanel.revalidate();
        harflerPanel.repaint();
    }

    private void resmiGuncelle() {
        // hataSayisi 0 iken 1.jpg, 1 iken 2.jpg ... diye gider
        int resimNo = hataSayisi + 1;
        if (resimNo < 1) resimNo = 1;
        if (resimNo > 11) resimNo = 11;

        String resimYolu = RESIMLER_DIZINI + resimNo + ".jpg";
        ImageIcon ikon = new ImageIcon(resimYolu);
        Image olcekliResim = ikon.getImage().getScaledInstance(
            resimLabel.getPreferredSize().width,
            resimLabel.getPreferredSize().height,
            Image.SCALE_SMOOTH
        );
        resimLabel.setIcon(new ImageIcon(olcekliResim));
    }

    private void oyunuKazan() {
        oyunSaati.stop();
        oyunBitti = true;

        harfAlani.setEnabled(false);
        kelimeAlani.setEnabled(false);
        harfTahminBtn.setEnabled(false);
        kelimeTahminBtn.setEnabled(false);

        oyunKaydet("KAZANDI");

        int secim = JOptionPane.showConfirmDialog(this,
            "Tebrikler! Kelimeyi buldunuz: " + secilenKelime +
            "\nGeçen Süre: " + gecenSaniye + " saniye" +
            "\n\nYeni oyun oynamak ister misiniz?",
            "Oyun Bitti - Kazandınız!",
            JOptionPane.YES_NO_OPTION);

        if (secim == JOptionPane.YES_OPTION) yeniOyunBaslat();
    }

    private void oyunuKaybet() {
        oyunSaati.stop();
        oyunBitti = true;

        harfAlani.setEnabled(false);
        kelimeAlani.setEnabled(false);
        harfTahminBtn.setEnabled(false);
        kelimeTahminBtn.setEnabled(false);

        resmiGuncelle();
        oyunKaydet("KAYBETTİ");

        int secim = JOptionPane.showConfirmDialog(this,
            "Maalesef kaybettiniz! Kelime: " + secilenKelime +
            "\nGeçen Süre: " + gecenSaniye + " saniye" +
            "\n\nTekrar denemek ister misiniz?",
            "Oyun Bitti - Kaybettiniz!",
            JOptionPane.YES_NO_OPTION);

        if (secim == JOptionPane.YES_OPTION) yeniOyunBaslat();
    }

    private void oyunKaydet(String sonuc) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String tarih = format.format(new Date());

            // sütunları | ile ayırıyoruz, okurken split ile parse edeceğiz
            String satir = tarih + " | " + sonuc + " | " +
                           gecenSaniye + " saniye | Kelime: " + secilenKelime +
                           " | Hata: " + hataSayisi + "\n";

            BufferedWriter yazici = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(OYUNLAR_DOSYA, true), "UTF-8"));
            yazici.write(satir);
            yazici.close();

            skorTablosunuYenile();
        } catch (IOException e) {
            System.out.println("Oyun kaydedilemedi: " + e.getMessage());
        }
    }

    private JPanel skorSekmesiOlustur() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] sutunlar = {"Tarih ve Saat", "Sonuç", "Süre", "Kelime", "Hata Sayısı"};
        skorModelData = new DefaultTableModel(sutunlar, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        skorTablosu = new JTable(skorModelData);
        skorTablosu.setFillsViewportHeight(true);
        skorTablosu.getTableHeader().setReorderingAllowed(false);

        panel.add(new JScrollPane(skorTablosu), BorderLayout.CENTER);

        JButton temizleBtn = new JButton("Skorları Temizle");
        temizleBtn.setBackground(new Color(220, 50, 50));
        temizleBtn.setForeground(Color.WHITE);
        temizleBtn.addActionListener(e -> dosyayiTemizle(OYUNLAR_DOSYA, skorModelData, "Skorlar"));

        JPanel altPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        altPanel.add(temizleBtn);
        panel.add(altPanel, BorderLayout.SOUTH);

        skorTablosunuYenile();
        return panel;
    }

    private void skorTablosunuYenile() {
        skorModelData.setRowCount(0);
        try {
            File dosya = new File(OYUNLAR_DOSYA);
            if (!dosya.exists() || dosya.length() == 0) return;

            BufferedReader okuyucu = new BufferedReader(new InputStreamReader(new FileInputStream(dosya), "UTF-8"));
            String satir;
            while ((satir = okuyucu.readLine()) != null) {
                if (satir.trim().isEmpty()) continue;
                String[] parcalar = satir.split("\\|");
                if (parcalar.length >= 5) {
                    skorModelData.addRow(new Object[]{
                        parcalar[0].trim(), parcalar[1].trim(),
                        parcalar[2].trim(), parcalar[3].trim(), parcalar[4].trim()
                    });
                }
            }
            okuyucu.close();
        } catch (IOException e) {
            System.out.println("Skor dosyası okunamadı: " + e.getMessage());
        }
    }

    private JPanel logSekmesiOlustur() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] sutunlar = {"Tarih ve Saat", "İşlem"};
        logModelData = new DefaultTableModel(sutunlar, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        logTablosu = new JTable(logModelData);
        logTablosu.setFillsViewportHeight(true);
        logTablosu.getTableHeader().setReorderingAllowed(false);

        panel.add(new JScrollPane(logTablosu), BorderLayout.CENTER);

        JButton temizleBtn = new JButton("Logları Temizle");
        temizleBtn.setBackground(new Color(220, 50, 50));
        temizleBtn.setForeground(Color.WHITE);
        temizleBtn.addActionListener(e -> dosyayiTemizle(LOG_DOSYA, logModelData, "Loglar"));

        JPanel altPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        altPanel.add(temizleBtn);
        panel.add(altPanel, BorderLayout.SOUTH);

        logTablosunuYenile();
        return panel;
    }

    private void logTablosunuYenile() {
        logModelData.setRowCount(0);
        try {
            File dosya = new File(LOG_DOSYA);
            if (!dosya.exists() || dosya.length() == 0) return;

            BufferedReader okuyucu = new BufferedReader(new InputStreamReader(new FileInputStream(dosya), "UTF-8"));
            String satir;
            while ((satir = okuyucu.readLine()) != null) {
                if (satir.trim().isEmpty()) continue;
                String[] parcalar = satir.split("\\|");
                if (parcalar.length >= 2) {
                    logModelData.addRow(new Object[]{
                        parcalar[0].trim(), parcalar[1].trim()
                    });
                }
            }
            okuyucu.close();
        } catch (IOException e) {
            System.out.println("Log dosyası okunamadı: " + e.getMessage());
        }
    }

    // hem skor hem log sekmesi için kullanılan ortak temizleme metodu
    private void dosyayiTemizle(String dosyaYolu, DefaultTableModel tablo, String etiket) {
        String girilenSifre = JOptionPane.showInputDialog(this,
            etiket + " temizlemek için şifrenizi girin:",
            "Şifre Doğrulama", JOptionPane.QUESTION_MESSAGE);

        if (girilenSifre == null) return;

        String kayitliSifre = sifreOku();

        if (girilenSifre.trim().equals(kayitliSifre != null ? kayitliSifre.trim() : "")) {
            try {
                BufferedWriter yazici = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dosyaYolu, false), "UTF-8"));
                yazici.write("");
                yazici.close();
                tablo.setRowCount(0);
                JOptionPane.showMessageDialog(this, etiket + " başarıyla temizlendi!");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Dosya temizlenemedi: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Yanlış şifre! İşlem iptal edildi.",
                "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdamAsmaca());
    }
}
