import pandas as pd
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import train_test_split
import joblib

data = pd.read_csv("training_data.csv")
y = data["reussite"]

X1 = data[["note1"]]
X2 = data[["note1", "note2"]]
X3 = data[["note1", "note2", "note3"]]

X1_train, X1_test, y_train, y_test = train_test_split(X1, y, test_size=0.2, random_state=42)
model1 = LogisticRegression()
model1.fit(X1_train, y_train)
print(f"accuracy (note1 only): {model1.score(X1_test, y_test)}")
joblib.dump(model1, "success_model_1note.joblib")

X2_train, X2_test, _, _ = train_test_split(X2, y, test_size=0.2, random_state=42)
model2 = LogisticRegression()
model2.fit(X2_train, y_train)
print(f"accuracy (note1+note2): {model2.score(X2_test, y_test)}")
joblib.dump(model2, "success_model_2notes.joblib")

X3_train, X3_test, _, _ = train_test_split(X3, y, test_size=0.2, random_state=42)
model3 = LogisticRegression()
model3.fit(X3_train, y_train)
print(f"accuracy (note1+note2+note3): {model3.score(X3_test, y_test)}")
joblib.dump(model3, "success_model.joblib")

print("saved success_model_1note.joblib, success_model_2notes.joblib, success_model.joblib")
