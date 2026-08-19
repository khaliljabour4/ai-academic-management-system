from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import requests
import os
import joblib
import pandas as pd

app = FastAPI()

OLLAMA_URL = "http://localhost:11434/api/generate"
MODEL_NAME = os.getenv("OLLAMA_MODEL", "llama3.2")

success_model_1note = joblib.load("success_model_1note.joblib")
success_model_2notes = joblib.load("success_model_2notes.joblib")
success_model = joblib.load("success_model.joblib")


class AskRequest(BaseModel):
    question: str
    history: list[str] = []


class PredictRequest(BaseModel):
    note1: float
    note2: float
    note3: float


class PredictProgressRequest(BaseModel):
    note1: float
    note2: float | None = None
    note3: float | None = None


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
    features = pd.DataFrame([[request.note1, request.note2, request.note3]],
                             columns=["note1", "note2", "note3"])
    probability = success_model.predict_proba(features)[0][1]
    return {"successRate": round(probability * 100, 1)}


@app.post("/ml/predict-progress")
def predict_progress(request: PredictProgressRequest):
    if request.note3 is not None:
        features = pd.DataFrame([[request.note1, request.note2, request.note3]],
                                 columns=["note1", "note2", "note3"])
        probability = success_model.predict_proba(features)[0][1]
        notesUsed = 3
    elif request.note2 is not None:
        features = pd.DataFrame([[request.note1, request.note2]],
                                 columns=["note1", "note2"])
        probability = success_model_2notes.predict_proba(features)[0][1]
        notesUsed = 2
    else:
        features = pd.DataFrame([[request.note1]], columns=["note1"])
        probability = success_model_1note.predict_proba(features)[0][1]
        notesUsed = 1

    confidence = "faible" if notesUsed == 1 else "moyenne" if notesUsed == 2 else "haute"

    return {
        "successRate": round(probability * 100, 1),
        "notesUsed": notesUsed,
        "confidence": confidence
    }
