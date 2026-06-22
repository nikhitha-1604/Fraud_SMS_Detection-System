# Fraud SMS Detection Backend (Flask + TF-IDF + ML)

This backend trains a model using your dataset (`label,text`) and exposes a REST API for Android (via Retrofit).

## 1) Create virtual environment

From this folder:

```bash
cd fraud_sms_detection\backend
python -m venv venv
venv\Scripts\activate
```

## 2) Install dependencies

```bash
pip install -r requirements.txt
python -c "import nltk; nltk.download('stopwords')"
```

## 3) Train the model

```bash
python train_model.py --dataset "C:\Users\LASYA SRI\Desktop\osp\final_combined_dataset .csv" --out_dir "models"
```

This will generate:

- `models/sms_fraud_pipeline.joblib`

## 4) Run the API

```bash
python app.py --model_path "models/sms_fraud_pipeline.joblib"
```

API:

- `POST /predict` with JSON: `{"text":"...sms..."}`.
- Response: `{"label":"spam|genuine","spam_probability":0.0}`

Health check:

- `GET /health`

