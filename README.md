# Инструкция по запуску трансляции экрана Android на Django сайте

## 📋 Обзор

Этот проект состоит из:
1. **Django сервера** - принимает и транслирует видеопоток через WebSocket
2. **Веб-сайта** - отображает трансляцию в браузере
3. **Android приложения** - захватывает экран и отправляет на сервер

---

## 🚀 Быстрый старт (с готовыми приложениями)

### Вариант 1: Использование готового Android приложения (рекомендуется)

#### Шаг 1: Запуск Django сервера

```bash
cd /workspace
pip install django channels daphne pillow
python manage.py runserver 0.0.0.0:8000
```

Сервер запустится на порту 8000.

#### Шаг 2: Установка Android приложения

Установите одно из следующих приложений из Google Play:

**Рекомендуемые приложения:**

1. **Screen Stream Mirroring** (самое популярное)
   - Ссылка: https://play.google.com/store/apps/details?id=com.hypersonic.screenstreamer
   - Бесплатная версия с базовым функционалом

2. **Screen Stream over HTTP** 
   - Простое приложение для стриминга

3. **TeamViewer QuickSupport** (альтернатива)
   - Для удаленного доступа

#### Шаг 3: Настройка Android приложения

1. Откройте установленное приложение
2. В настройках укажите:
   - **URL сервера**: `ws://ВАШ_IP_СЕРВЕРА:8000`
   - **ID трансляции**: любое уникальное имя (например, `demo`)
   
3. Нажмите "Start" или "Начать трансляцию"
4. Разрешите доступ к захвату экрана

#### Шаг 4: Просмотр трансляции

1. Откройте браузер на любом устройстве
2. Перейдите по адресу: `http://ВАШ_IP_СЕРВЕРА:8000`
3. Введите тот же ID трансляции
4. Нажмите "Смотреть трансляцию"

---

### Вариант 2: Создание своего Android приложения

Если вы хотите создать собственное приложение, используйте код из папки `android_client/`.

#### Требования для сборки:

1. **Android Studio** (скачать: https://developer.android.com/studio)
2. **JDK 11+**
3. **Android SDK API 21+**

#### Шаг 1: Создание проекта

1. Откройте Android Studio
2. Создайте новый проект: **Empty Activity**
3. Название: `ScreenStreamer`
4. Package name: `com.example.screenstreamer`
5. Minimum SDK: **API 21**
6. Language: **Kotlin**

#### Шаг 2: Добавление зависимостей

Откройте `build.gradle (Module: app)` и добавьте:

```gradle
dependencies {
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.9.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    
    // OkHttp для WebSocket
    implementation 'com.squareup.okhttp3:okhttp:4.11.0'
}
```

#### Шаг 3: Добавление разрешений

Откройте `AndroidManifest.xml` и добавьте перед `<application>`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
```

#### Шаг 4: Копирование кода

Скопируйте файлы из `/workspace/android_client/` в ваш проект:

- `MainActivity.kt` → `app/src/main/java/com/example/screenstreamer/`
- `ScreenStreamer.kt` → `app/src/main/java/com/example/screenstreamer/`
- `WebSocketClient.kt` → `app/src/main/java/com/example/screenstreamer/`
- `activity_main.xml` → `app/src/main/res/layout/`

#### Шаг 5: Сборка и установка

1. Подключите Android устройство по USB
2. Включите **Отладку по USB** в настройках разработчика
3. Нажмите **Run** (зеленый треугольник) в Android Studio
4. Или соберите APK: **Build → Build Bundle(s) / APK(s) → Build APK(s)**

---

## 🔧 Настройка сервера для доступа извне

### Для локальной сети (LAN):

1. Узнайте IP адрес вашего компьютера:
   - Windows: `ipconfig`
   - Linux/Mac: `ifconfig` или `ip addr`

2. Запустите сервер:
   ```bash
   python manage.py runserver 0.0.0.0:8000
   ```

3. На Android введите: `ws://ВАШ_IP:8000`

### Для доступа из интернета:

#### Вариант A: Ngrok (просто)

```bash
# Установите ngrok
# Запустите туннель
ngrok http 8000
```

Используйте URL от ngrok (например, `wss://xxxx.ngrok.io`)

#### Вариант B: Проброс портов на роутере

1. Зайдите в настройки роутера
2. Найдите **Port Forwarding**
3. Добавьте правило: порт 8000 → IP вашего компьютера
4. Используйте ваш внешний IP

---

## 📱 Как это работает

```
┌─────────────┐      WebSocket       ┌─────────────┐     HTTP/WebSocket    ┌──────────────┐
│   Android   │ ───────────────────► │   Django    │ ───────────────────► │   Браузер    │
│  Приложениe │   (JPEG кадры)       │   Сервер    │   (Трансляция)       │   Клиент     │
└─────────────┘                      └─────────────┘                      └──────────────┘
```

1. Android приложение захватывает экран (~15 FPS)
2. Каждый кадр сжимается в JPEG
3. Кадр отправляется через WebSocket на сервер
4. Сервер пересылает кадр всем подключенным зрителям
5. Браузер отображает кадры на canvas элементе

---

## ⚙️ Настройки производительности

В файле `ScreenStreamer.kt` можно изменить:

```kotlin
private val width = 1280      // Разрешение по ширине
private val height = 720      // Разрешение по высоте
private val dpi = 320         // Плотность пикселей

// Качество JPEG (0-100)
val jpegBytes = bitmapToJpeg(bitmap, 80) // 80% качество

// Частота кадров
Thread.sleep(67) // ~15 FPS (1000ms / 15 ≈ 67ms)
```

Для лучшего качества увеличьте разрешение и качество JPEG.
Для лучшей производительности уменьшите FPS.

---

## 🐛 Решение проблем

### "Не удается подключиться к серверу"
- Проверьте, что сервер запущен: `python manage.py runserver 0.0.0.0:8000`
- Убедитесь, что брандмауэр не блокирует порт 8000
- Проверьте, что Android и сервер в одной сети

### "Черный экран"
- Убедитесь, что разрешили захват экрана на Android
- Проверьте, что ID трансляции совпадает на клиенте и в браузере

### "Лагает видео"
- Уменьшите разрешение в `ScreenStreamer.kt`
- Уменьшите качество JPEG (до 60%)
- Уменьшите FPS (увеличьте delay)

### Ошибки WebSocket
- Проверьте протокол: `ws://` для HTTP, `wss://` для HTTPS
- Убедитесь, что ASGI настроен правильно

---

## 📄 Структура проекта

```
/workspace/
├── manage.py                 # Django entry point
├── screenstream/             # Django project
│   ├── settings.py           # Настройки (добавлены channels)
│   ├── urls.py               # URL маршруты
│   └── asgi.py               # ASGI конфигурация для WebSocket
├── stream/                   # Django app
│   ├── consumers.py          # WebSocket обработчик
│   ├── views.py              # View функции
│   ├── urls.py               # App URL маршруты
│   └── templates/stream/     # HTML шаблоны
│       ├── index.html        # Главная страница
│       └── stream.html       # Страница просмотра
└── android_client/           # Android приложение (код)
    ├── MainActivity.kt
    ├── ScreenStreamer.kt
    ├── WebSocketClient.kt
    └── activity_main.xml
```

---

## 🔐 Безопасность

Для продакшена рекомендуется:

1. Использовать HTTPS/WSS
2. Добавить аутентификацию
3. Ограничить количество подключений
4. Валидировать stream_id
5. Использовать Redis вместо InMemoryChannelLayer

---

## 📞 Контакты и поддержка

При возникновении проблем проверьте логи сервера:
```bash
python manage.py runserver --verbosity 2
```
