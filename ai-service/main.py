import asyncio
import hashlib
from pathlib import Path

import edge_tts
from deep_translator import GoogleTranslator
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field


app = FastAPI(title="LinguaFlow AI Service")

BACKEND_AUDIO_DIR = (
    Path(__file__).resolve().parent.parent
    / "backend"
    / "src"
    / "main"
    / "resources"
    / "static"
    / "audio"
)

VOICE_BY_LANGUAGE = {
    "en": "en-US-AriaNeural",
    "ja": "ja-JP-NanamiNeural",
    "ko": "ko-KR-SunHiNeural",
}


class TranslationRequest(BaseModel):
    text: str = Field(min_length=1)
    sourceLanguageCode: str = Field(min_length=2, max_length=10)
    targetLanguageCode: str = Field(min_length=2, max_length=10)


class TranslationResponse(BaseModel):
    translatedText: str


class TextToSpeechRequest(BaseModel):
    text: str = Field(min_length=1)
    languageCode: str = Field(min_length=2, max_length=10)


class TextToSpeechResponse(BaseModel):
    audioPath: str


@app.post("/api/translate", response_model=TranslationResponse)
def translate(request: TranslationRequest) -> TranslationResponse:
    try:
        translated_text = GoogleTranslator(
            source=request.sourceLanguageCode,
            target=request.targetLanguageCode,
        ).translate(request.text)
        return TranslationResponse(translatedText=translated_text)
    except Exception as exception:
        raise HTTPException(status_code=502, detail="Translation provider failed") from exception


@app.post("/api/tts", response_model=TextToSpeechResponse)
async def text_to_speech(request: TextToSpeechRequest) -> TextToSpeechResponse:
    language_code = request.languageCode.lower()
    voice = VOICE_BY_LANGUAGE.get(language_code)
    if voice is None:
        raise HTTPException(status_code=400, detail="Unsupported language code")

    file_hash = hashlib.sha256(
        f"{language_code}:{request.text}".encode("utf-8")
    ).hexdigest()[:24]
    file_name = f"{file_hash}-{language_code}.mp3"
    output_path = BACKEND_AUDIO_DIR / file_name
    BACKEND_AUDIO_DIR.mkdir(parents=True, exist_ok=True)

    try:
        if not output_path.exists():
            communicator = edge_tts.Communicate(request.text, voice)
            await communicator.save(str(output_path))
        return TextToSpeechResponse(audioPath=f"/static/audio/{file_name}")
    except Exception as exception:
        raise HTTPException(status_code=502, detail="Text-to-speech provider failed") from exception


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}