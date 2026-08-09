import pandas as pd
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import train_test_split
import joblib

data = pd.read_csv("training_data.csv")

X = data[["note1", "note2", "note3"]]
y = data["reussite"]

X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

model = LogisticRegression()
model.fit(X_train, y_train)

accuracy = model.score(X_test, y_test)
print(f"accuracy: {accuracy}")

joblib.dump(model, "success_model.joblib")
print("saved success_model.joblib")
