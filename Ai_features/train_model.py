import json

import joblib
import pandas as pd
from sklearn.dummy import DummyClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import roc_auc_score
from sklearn.model_selection import train_test_split

data = pd.read_csv("training_data.csv")
y = data["reussite"]

FEATURES = {
    "success_model_1note.joblib": ["note1"],
    "success_model_2notes.joblib": ["note1", "note2"],
    "success_model.joblib": ["note1", "note2", "note3"],
}

report = {}
for filename, cols in FEATURES.items():
    X_train, X_test, y_train, y_test = train_test_split(
        data[cols], y, test_size=0.2, random_state=42, stratify=y
    )
    model = LogisticRegression().fit(X_train, y_train)
    baseline = DummyClassifier(strategy="most_frequent").fit(X_train, y_train)

    acc = model.score(X_test, y_test)
    base = baseline.score(X_test, y_test)
    auc = roc_auc_score(y_test, model.predict_proba(X_test)[:, 1])
    report[f"{len(cols)} note(s)"] = {
        "accuracy": round(acc, 3),
        "baseline_accuracy": round(base, 3),
        "roc_auc": round(auc, 3),
        "test_rows": len(y_test),
    }
    print(f"{len(cols)} note(s): accuracy={acc:.3f}  baseline={base:.3f}  AUC={auc:.3f}  (n_test={len(y_test)})")
    joblib.dump(model, filename)

with open("metrics.json", "w") as f:
    json.dump(report, f, indent=2)
print("saved models + metrics.json")
