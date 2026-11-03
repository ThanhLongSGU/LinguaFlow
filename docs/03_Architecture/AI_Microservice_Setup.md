# LinguaFlow AI Microservice Setup

## Components

- `ai-service/main.py`: FastAPI service on `http://localhost:8000`.
- `POST /api/translate`: Google Translate adapter through `deep-translator`.
- `POST /api/tts`: Microsoft Neural Voice adapter through `edge-tts`.
- Generated MP3 files are stored in `backend/src/main/resources/static/audio/`.
- Spring Boot calls both endpoints through `RestClient` and retries transient failures with Resilience4j.

## Run Locally

From the repository root:

```text
python -m venv ai-service/.venv
ai-service/.venv/Scripts/activate
pip install -r ai-service/requirements.txt
uvicorn main:app --app-dir ai-service --host 0.0.0.0 --port 8000
```

The Spring Boot application must run alongside the Python service. The mock AI providers were replaced by the Python providers.

## API Contracts

Translation request:

```json
{
  "text": "Xin chao",
  "sourceLanguageCode": "vi",
  "targetLanguageCode": "en"
}
```

Translation response:

```json
{
  "translatedText": "Hello"
}
```

Text-to-speech request:

```json
{
  "text": "Hello",
  "languageCode": "en"
}
```

Text-to-speech response:

```json
{
  "audioPath": "/static/audio/<hash>-en.mp3"
}
```
