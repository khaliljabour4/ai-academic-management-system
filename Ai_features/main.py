from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import requests
import os
import joblib

app = FastAPI()

OLLAMA_URL = "http://localhost:11434/api/generate"
MODEL_NAME = os.getenv("OLLAMA_MODEL", "llama3.2")
success_model = joblib.load("success_model.joblib")


class AskRequest(BaseModel):
    question: str
    history: list[str] = []


class PredictRequest(BaseModel):
    note1: float
    note2: float
    note3: float


def build_prompt(question, history):
    if not history:
        return question
    parts = []
    for i in range(0, len(history) - 1, 2):
        parts.append(f"Q: {history[i]}\nA: {history[i+1]}")
    parts.append(f"Q: {question}")
    return "\n\n".join(parts)


@app.post("/ai/ask")
def ask(request: AskRequest):
    prompt = build_prompt(request.question, request.history)

    try:
        response = requests.post(
            OLLAMA_URL,
            json={
                "model": MODEL_NAME,
                "prompt": prompt,
                "stream": False
            },
            timeout=60
        )
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail="Ollama not reachable")
    except requests.exceptions.Timeout:
        raise HTTPException(status_code=504, detail="Ollama took too long to respond")

    if response.status_code != 200:
        raise HTTPException(status_code=502, detail="Ollama returned an error")

    data = response.json()
    return {"answer": data["response"]}


@app.get("/ai/health")
def health():
    try:
        requests.get("http://localhost:11434", timeout=3)
        return {"ollama": "up"}
    except requests.exceptions.RequestException:
        return {"ollama": "down"}


@app.post("/ml/predict-success")
def predict_success(request: PredictRequest):
    features = [[request.note1, request.note2, request.note3]]
    probability = success_model.predict_proba(features)[0][1]
    return {"successRate": round(probability * 100, 1)}
