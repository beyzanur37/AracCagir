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

##

## 🚀 Kurulum (Local'de Çalıştırma)

Bu projeyi kendi bilgisayarınızda çalıştırmak için aşağıdaki adımları izleyin:

1. Bu depoyu klonlayın:
   ```bash
   git clone https://github.com/KULLANICI_ADIN/AracCagir.git
