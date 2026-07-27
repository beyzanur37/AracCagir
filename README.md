#  Araç Çağır (Ride Hailing App)

**Araç Çağır**, kullanıcıların hızlı ve güvenli bir şekilde araç bulmasını sağlayan veya sürücülerin yolcu bularak kazanç elde etmesine olanak tanıyan, Android platformu için geliştirilmiş kapsamlı bir **Araç Çağırma (Ride-Hailing)** uygulamasıdır.

Yolcu ve Sürücü olmak üzere iki farklı kullanıcı tipi barındırır. Firebase altyapısı ile gerçek zamanlı veri akışı sağlar ve Google Haritalar entegrasyonu ile anlık konum takibi sunar.

---

##  Öne Çıkan Özellikler

###  Kullanıcı ve Rol Yönetimi
* **Sürücü veya Yolcu Olma Seçeneği:** Kayıt esnasında rol seçimi (RoleSelectionActivity).
* **Profil Yönetimi:** Kişisel bilgiler, iletişim bilgileri ve sürücüler için araç bilgilerini (plaka, model, renk vb.) düzenleme.
* **Güvenli Kimlik Doğrulama:** Firebase Authentication ile güvenli giriş/çıkış ve kayıt işlemleri.

### Harita ve Konum (Map & Location)
* **Canlı Harita (Google Maps API):** Yolcular için araç çağırma ve sürücülerin anlık konumunu görme (PassengerMapFragment).
* **Sürücü Haritası:** Sürücülerin gelen çağrıları görmesi ve yolcu konumuna gitmesi (DriverMapFragment).

###  Cüzdan ve Ödeme (Wallet & Payment)
* **Uygulama İçi Cüzdan:** Kullanıcıların bakiyelerini görüntüleyebildiği cüzdan ekranı (WalletFragment).
* **Kart Ekleme:** Ödeme işlemleri için kredi kartı/banka kartı ekleme diyaloğu (PaymentCardDialog).

###  Geçmiş ve Değerlendirme (History & Reviews)
* **Yolculuk Geçmişi:** Kullanıcıların geçmişte yaptığı yolculukları listeleme (HistoryFragment & RideHistory).
* **Puanlama ve Yorum Sistemi:** Yolculuk sonrası sürücüyü/yolcuyu değerlendirme ve yorum bırakabilme (ReviewsFragment & ReviewAdapter).

---

##  Kullanılan Teknolojiler ve Mimariler

* **Programlama Dili:** Java
* **UI/UX:** XML, Material Design Components
* **Veritabanı:** Firebase Realtime Database
* **Kimlik Doğrulama:** Firebase Authentication
* **Harita & Konum Servisleri:** Google Maps SDK for Android, Fused Location Provider
* **Asenkron İşlemler:** Firebase Tasks & Listeners
* **Mimari Yapı:** Activity & Fragment tabanlı modüler UI (Bottom Navigation)

## UYGULAMA EKRAN GÖRÜNTÜLERİ
## YOLCU PANELİ
### GİRİŞ EKRANI
<img width="435" height="843" alt="Ekran görüntüsü 2026-07-27 110549" src="https://github.com/user-attachments/assets/6a64489d-ea2a-4047-aa83-e85fc13a89c4" />

### İKİ AYRI KAYIT EKRANI 
* YOLCU VE SÜRÜCÜ İÇİN İKİ AYRI ARAYÜZ OLDUĞU İÇİN
<img width="445" height="850" alt="Ekran görüntüsü 2026-07-27 110555" src="https://github.com/user-attachments/assets/ae037d14-c32d-4748-add0-b6f50ef9af85" />
<img width="439" height="849" alt="Ekran görüntüsü 2026-07-27 110649" src="https://github.com/user-attachments/assets/ee0c69cb-10f9-4838-bb7b-c84109f998f5" />

### YOLCU HARİTADAN KONUM SEÇME
<img width="429" height="847" alt="Ekran görüntüsü 2026-07-27 135746" src="https://github.com/user-attachments/assets/44b5320e-db86-4f55-b46e-ea22504d04d1" />

### ARAÇ TİPİNİ SEÇME ALANI VE ARAÇ ÇAĞIRMA ALANI
<img width="434" height="827" alt="Ekran görüntüsü 2026-07-27 135801" src="https://github.com/user-attachments/assets/390f7e9c-facf-440e-8981-39de708d3893" />

### GEÇMİŞ SÜRÜŞLERİ GÖREBİLME
<img width="463" height="827" alt="Ekran görüntüsü 2026-07-27 135819" src="https://github.com/user-attachments/assets/7cfda54a-5d9e-4b03-b638-4f2ffbb0e4ec" />

### UYGULAMADAKİ BAKİYE VE BAKİYE YÜKLEME ALANI
<img width="467" height="850" alt="Ekran görüntüsü 2026-07-27 135827" src="https://github.com/user-attachments/assets/613178d3-6a6e-4a39-be5c-692fb69fabc2" />
<img width="499" height="838" alt="Ekran görüntüsü 2026-07-27 135851" src="https://github.com/user-attachments/assets/ff11a720-1e52-4499-9fec-14015fde35f6" />

### PROFİL
<img width="507" height="850" alt="Ekran görüntüsü 2026-07-27 135834" src="https://github.com/user-attachments/assets/65e18928-32fd-40fb-b20a-a961378fab8a" />

## SÜRÜCÜ PANELİ
*Giriş arayüzü, kayıt olma arayüzü, sürüş geçmişi ve profil yolcu paneli ile aynı sadece kayıt kısmında fazladan araç bilgileri alınıyor.

### YOLCU BİLDİRİM EKRANI
<img width="508" height="838" alt="Ekran görüntüsü 2026-07-27 140024" src="https://github.com/user-attachments/assets/dde65ec8-8967-45bc-be0f-5a5c57c9685b" />

### SÜRÜŞ BİTTİ ÖDEME AL
<img width="500" height="845" alt="Ekran görüntüsü 2026-07-27 140038" src="https://github.com/user-attachments/assets/22322700-6ce6-4f35-be97-8be0dd100ccb" />

### SÜRÜCÜ DEĞERLENDİRMELERİ
<img width="516" height="841" alt="Ekran görüntüsü 2026-07-27 140050" src="https://github.com/user-attachments/assets/b1e924a6-bdba-4534-b6ce-63ca8dc3b5e6" />







## 🚀 Kurulum (Local'de Çalıştırma)

Bu projeyi kendi bilgisayarınızda çalıştırmak için aşağıdaki adımları izleyin:

1. Bu depoyu klonlayın:
   ```bash
   git clone https://github.com/KULLANICI_ADIN/AracCagir.git
