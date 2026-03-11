# RequiredMod (Forge 1.20.1)

Клиентский мод для Forge **1.20.1**, который проверяет обновления из GitHub и показывает окно установки на главном экране.

## Что сделано
- Проверка обновлений при старте клиента.
- Фильтрация версий по `minecraft=1.20.1` и `loader=forge`.
- Окно с кнопками:
  - `Скачать и установить`
  - `Открыть страницу релиза`
  - `Позже`
- Скачивание jar в папку `mods/`.
- Проверка SHA-256 (если хэш задан в manifest).

## Настройка под ваш репозиторий
Измените URL manifest в `UpdateService`:

```java
private static final String UPDATE_MANIFEST_URL = "https://raw.githubusercontent.com/your-org/RequiredMod/main/updates.json";
```

## Формат `updates.json`
Положите файл в репозиторий (например, в `main` branch):

```json
{
  "versions": [
    {
      "version": "0.1.1",
      "minecraft": "1.20.1",
      "loader": "forge",
      "download_url": "https://github.com/your-org/RequiredMod/releases/download/v0.1.1/requiredmod-0.1.1.jar",
      "sha256": "<sha256>",
      "changelog": "Fixes + performance improvements",
      "release_page": "https://github.com/your-org/RequiredMod/releases/tag/v0.1.1"
    }
  ]
}
```

## Запуск
```bash
./gradlew runClient
```

## Сборка
```bash
./gradlew build
```
