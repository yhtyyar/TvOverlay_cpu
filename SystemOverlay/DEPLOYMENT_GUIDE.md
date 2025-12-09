# 🚀 Руководство по развертыванию System Overlay

## Быстрый старт для Android TV

### 1. Сборка приложения

```bash
# Убедитесь, что у вас установлен Android SDK и настроены переменные окружения
export ANDROID_HOME=/path/to/android-sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools

# Клонируйте и перейдите в проект
git clone <repository-url>
cd SystemOverlay

# Создайте релизную сборку
./gradlew clean assembleRelease

# APK файл будет создан в: app/build/outputs/apk/release/app-release.apk
```

### 2. Установка на Android TV

```bash
# Подключитесь к TV устройству
adb connect <TV_IP_ADDRESS>:5555

# Или через USB
adb devices

# Установите приложение
adb install app/build/outputs/apk/release/app-release.apk

# Предоставьте необходимые разрешения
adb shell appops set com.systemoverlay.app SYSTEM_ALERT_WINDOW allow
adb shell dumpsys deviceidle whitelist +com.systemoverlay.app

# Опционально: отключите battery optimization (рекомендуется)
adb shell dumpsys deviceidle whitelist +com.systemoverlay.app
```

### 3. Проверка установки

```bash
# Проверьте, что приложение установлено
adb shell pm list packages | grep systemoverlay

# Запустите приложение
adb shell am start -n com.systemoverlay.app/.presentation.ui.TvActivity

# Проверьте логи для диагностики
adb logcat -s SystemOverlay
```

## 🔧 Настройка для оптимальной производительности

### Рекомендуемые настройки для Android TV:

1. **Интервал обновления**: 3-5 секунд (автоматически настраивается)
2. **Позиция**: Top Right (безопасная зона для TV)
3. **Прозрачность**: 85% (оптимальная видимость)
4. **Автозапуск**: Включить для мониторинга 24/7

### ADB команды для настройки:

```bash
# Включить режим разработчика (если нужно)
adb shell settings put global adb_enabled 1

# Отключить анимации для лучшей производительности
adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0

# Настроить system overlay permissions
adb shell appops set com.systemoverlay.app SYSTEM_ALERT_WINDOW allow
adb shell appops get com.systemoverlay.app SYSTEM_ALERT_WINDOW
```

## 📊 Мониторинг производительности

### Команды для проверки эффективности:

```bash
# Проверка использования CPU приложением
adb shell top -p $(adb shell pidof com.systemoverlay.app)

# Мониторинг памяти
adb shell dumpsys meminfo com.systemoverlay.app

# Проверка energy usage
adb shell dumpsys batterystats --pkg=com.systemoverlay.app

# Логи производительности
adb logcat -s "SystemOverlay" -s "CpuDataSource" -s "GpuDataSource" -s "RamDataSource"
```

### Ожидаемые показатели после оптимизации:

| Метрика | Значение | Комментарий |
|---------|----------|-------------|
| **CPU Usage** | 0.5-2% | В зависимости от активности системы |
| **RAM Usage** | 15-25 MB | Включая UI и кеши |
| **Update Frequency** | 3-5 сек | Адаптивно изменяется |
| **Battery Impact** | Минимальный | < 1% в час на TV устройствах |

## 🐛 Диагностика проблем

### Частые проблемы и решения:

#### 1. Оверлей не отображается
```bash
# Проверьте разрешения
adb shell appops get com.systemoverlay.app SYSTEM_ALERT_WINDOW

# Должно быть: allow
# Если нет, выполните:
adb shell appops set com.systemoverlay.app SYSTEM_ALERT_WINDOW allow
```

#### 2. Высокое потребление CPU
```bash
# Проверьте адаптивный режим
adb logcat -s "OverlayService" | grep -i "adaptive"

# Принудительно активируйте power saving режим
adb shell settings put global low_power 1
```

#### 3. Сервис останавливается
```bash
# Проверьте whitelist для background activity
adb shell dumpsys deviceidle whitelist +com.systemoverlay.app

# Отключите battery optimization
adb shell settings put global app_standby_enabled 0
```

#### 4. GPU метрики недоступны
```bash
# Это нормально для некоторых TV устройств
# Приложение автоматически скрывает GPU виджет
adb logcat -s "GpuDataSource"
```

### Логи для отладки:

```bash
# Полные логи приложения
adb logcat -s "SystemOverlayApp" -s "OverlayService" -s "DeviceUtils"

# Логи источников данных
adb logcat -s "CpuDataSource" -s "GpuDataSource" -s "RamDataSource"

# Производительность и адаптация
adb logcat | grep -E "(SystemOverlay|adaptive|throttl|cache)"
```

## 🏗️ Кастомизация для конкретных устройств

### Настройка констант для специфичных TV:

Отредактируйте `Constants.kt`:

```kotlin
// Для мощных Android TV (NVIDIA Shield, etc.)
const val TV_FAST_UPDATE_INTERVAL_MS = 1500L

// Для бюджетных TV устройств
const val TV_SLOW_UPDATE_INTERVAL_MS = 7000L

// Для устройств с ограниченной памятью
const val MEMORY_PRESSURE_THRESHOLD_MB = 64
```

### Создание flavor'ов для разных устройств:

```kotlin
// В build.gradle.kts
android {
    flavorDimensions += "device"
    
    productFlavors {
        create("shield") {
            dimension = "device"
            buildConfigField("long", "DEFAULT_INTERVAL", "1500L")
        }
        
        create("budget") {
            dimension = "device"  
            buildConfigField("long", "DEFAULT_INTERVAL", "7000L")
        }
    }
}
```

## 📈 Рекомендации по эксплуатации

### Для продакшена:
1. **Мониторинг**: Настройте crash reporting (Firebase Crashlytics)
2. **Обновления**: Используйте автообновления через Play Store
3. **Аналитика**: Добавьте метрики использования
4. **Тестирование**: Проверьте на разных моделях Android TV

### Для разработки:
1. **Профилирование**: Используйте Android Studio Profiler
2. **Тестирование**: Unit и integration тесты для DataSource'ов
3. **CI/CD**: Автоматическая сборка и тестирование
4. **Код-ревью**: Проверка performance-критичных изменений

## 🔒 Безопасность

### Важные соображения:
- ✅ Приложение не собирает персональные данные
- ✅ Все системные данные читаются локально
- ✅ Нет сетевых запросов (кроме обновлений)
- ✅ Минимальные разрешения (только overlay)
- ✅ Отдельный процесс для изоляции

### Рекомендации для enterprise:
```bash
# Отключить дебаг логи в продакшене
adb shell setprop log.tag.SystemOverlay ERROR

# Проверить отсутствие sensitive data в логах
adb logcat -s "SystemOverlay" | grep -i "password\|token\|key"
```

Приложение готово к развертыванию на Android TV устройствах с максимальной производительностью и минимальной нагрузкой на систему! 🚀
