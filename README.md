# 🏋️ LiftLog

Mobilna aplikacja na system Android do śledzenia postępów siłowych na siłowni. Umożliwia tworzenie planów treningowych, rejestrowanie serii i ciężarów oraz wizualizację postępu w czasie.

---

## 👥 Zespół

| Członek | Obszar |
|--------|--------|
| lubkaf | Backend / Baza danych (Room, logika sesji) |
| jana | Frontend / UI (ekrany, nawigacja, wykresy) |
| bogdans | Integracja API, biblioteka ćwiczeń, dokumentacja |

---

## 📱 Funkcjonalności

- Tworzenie i zarządzanie planami treningowymi
- Biblioteka ćwiczeń z możliwością dodawania własnych
- Rejestrowanie serii, powtórzeń i ciężarów podczas treningu
- Historia treningów z wykresami postępu
- Kalkulator One Rep Max (1RM) z zewnętrznym API
- Timer odpoczynku między seriami
- Statystyki: wolumen, rekordy osobiste

---

## 🛠️ Stos technologiczny

- **Język:** Java
- **Platforma:** Android (min. SDK 26 / Android 8.0)
- **Baza danych:** SQLite via Room
- **HTTP:** Retrofit 2
- **Wykresy:** MPAndroidChart
- **API:** (na razie brak)

---

## 🗂️ Struktura ekranów

```
Dashboard
  ├── Plany treningowe
  │     ├── Edycja planu ──► Biblioteka ćwiczeń
  │     └── Aktywny trening ──► Podsumowanie treningu
  ├── Historia treningów
  ├── Biblioteka ćwiczeń
  └── Kalkulator 1RM
```

---

## 🚀 Uruchomienie projektu

### Wymagania

- Android Studio Narwhal 3 (2025.1.3)
- JDK 17+
- Android SDK 26+

### Kroki

1. Sklonuj repozytorium:
   ```bash
   git clone https://github.com/lubkaf/LiftLog.git
   cd liftlog
   ```

2. Otwórz projekt w Android Studio.

3. Dodaj klucze API — utwórz plik `local.properties` w katalogu głównym projektu i dodaj:
   ```
   RAPIDAPI_KEY=...
   API_NINJAS_KEY=...
   ```

4. Zbuduj i uruchom projekt na emulatorze lub urządzeniu fizycznym (Android 8.0+).

---

## 🗄️ Schemat bazy danych

```
exercises          training_plans
    |                    |
    └── plan_exercises ──┘

exercises
    |
    └── session_sets ── workout_sessions
```

**Tabele:** `exercises`, `training_plans`, `plan_exercises`, `workout_sessions`, `session_sets`

Szczegółowy opis w [dokumentacji projektu](./LiftLog_Dokumentacja.docx).

---

## 🤝 Konwencje Git

- Gałęzie: `feature/<nazwa>`, `fix/<nazwa>`
- Commity po polsku lub angielsku
- Pull requesty przed mergem do `main`
- Każdy członek zespołu commituje

---

## 📄 Licencja

Projekt zaliczeniowy — Programowanie Aplikacji Mobilnych.
