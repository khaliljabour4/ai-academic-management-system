"""Generate a synthetic but plausible training set.

Rule (documented, so the model's job is well-defined):
  - each student has a latent "ability" (normal, centered around 11.5/20)
  - each exam grade = ability + exam noise, clipped to [0, 20]
  - a student passes the module if their latent ability + small noise is >= 10

This is SYNTHETIC data for demonstrating the pipeline. Replace training_data.csv
with real historical grades to get a real predictor.
"""
import numpy as np
import pandas as pd

rng = np.random.default_rng(42)
N = 1000

ability = rng.normal(11.5, 3.0, N)
notes = np.clip(ability[:, None] + rng.normal(0, 2.0, (N, 3)), 0, 20).round(1)
reussite = ((ability + rng.normal(0, 1.0, N)) >= 10).astype(int)

df = pd.DataFrame(notes, columns=["note1", "note2", "note3"])
df["reussite"] = reussite
df.to_csv("training_data.csv", index=False)
print(f"wrote training_data.csv: {N} rows, pass rate {reussite.mean():.0%}")
