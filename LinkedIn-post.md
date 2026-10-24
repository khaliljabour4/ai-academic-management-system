# LinkedIn post: AI Academic Management System

## Title (headline line)
I built a school platform that predicts at-risk students after the first grade

## Description (post body, copy-paste)
Most school systems tell you a student is failing when it's already too late.

So I built one that doesn't wait. My AI Academic Management System estimates a student's chance of passing as soon as the first grade is entered, and sharpens the estimate as grades 2 and 3 arrive.

What's inside:
• Spring Boot REST API with JWT auth and 4 roles (Admin, Director, Professor, Student)
• Python FastAPI service running 3 scikit-learn models, one for each number of grades available
• A local LLM study assistant (Llama 3.2 via Ollama) with one conversation per module
• Director dashboard: at-risk students and early predictions per module

What I learned the hard way:
🔐 A security review of my own code found that anyone could register an admin account. Fixed with role-based authorization, default-deny routes and tests for every role.
📊 My first model scored 58%, worse than always guessing "pass". Honest evaluation against a baseline showed my training data was weak. I rebuilt the pipeline with a documented synthetic dataset: 90.5% accuracy vs a 67% baseline, 0.955 AUC. The data is synthetic, and I say so in the README.

Next step: validating the model on real historical grades.

Code and architecture in the comments 👇

## Hashtags
#SpringBoot #Java #Python #FastAPI #MachineLearning #scikitlearn #LLM #Ollama #FullStack #JWT #SoftwareEngineering #EdTech #StudentProject #Morocco

## Suggested image order (carousel or first 4 images)
1. docs/screenshots/04-director-predictions.png  (best hook: shows the ML output)
2. docs/screenshots/05-student-ai-assistant.png
3. docs/screenshots/03-director-ml-risk.png
4. docs/screenshots/02-director-overview.png
(extras: 06 schedule, 07 grades entry, 08 admin, 01 login)

## Before you post
- Add the GitHub link in the first comment, not in the post body (LinkedIn reaches fewer people when the post has a link).
- Push the updated code and README first, so the link works when people click it.
- Screenshots use sample data; the README says so.
