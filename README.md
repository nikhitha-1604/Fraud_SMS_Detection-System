# Fraud SMS Detection System

## Overview

The Fraud SMS Detection System is a machine learning-based application that detects fraudulent and spam SMS messages in real time. The system consists of an Android application and a Flask backend API. Incoming SMS messages are analyzed using a trained machine learning model and classified as either **Spam** or **Safe (Ham)**.



## Features

- Real-time SMS monitoring on Android devices
- Automatic spam/fraud detection
- Machine Learning-based classification
- TF-IDF feature extraction
- Multinomial Naive Bayes prediction model
- REST API communication using Flask
- Spam probability score generation
- Android notifications for suspicious messages
- SMS history tracking
- Health check endpoint for backend monitoring


## Technology Stack

### Frontend
- Kotlin
- Android Studio
- Android SDK
- AppCompat Libraries

### Backend
- Python
- Flask
- scikit-learn
- NLTK
- Joblib

### Machine Learning
- TF-IDF Vectorizer
- Multinomial Naive Bayes

### Storage
- File-based model storage using `.joblib`



## Project Architecture

```text
Incoming SMS
      │
      ▼
Android Application
      │
 REST API Request
      ▼
Flask Backend
      │
TF-IDF Vectorization
      │
Multinomial Naive Bayes Model
      ▼
Prediction Result
      │
      ▼
Android Notification


## Project Structure

```text
FraudSMSDetection/
│
├── AndroidApp/
│   ├── Activities
│   ├── BroadcastReceiver
│   ├── Notification Module
│   └── API Integration
│
├── Backend/
│   ├── app.py
│   ├── train_model.py
│   ├── sms_fraud_pipeline.joblib
│   └── requirements.txt
│
├── Dataset/
│   └── spam.csv
│
└── README.md



**### Install Dependencies**


```bash
pip install -r requirements.txt
```

### Train Model

```bash
python train_model.py
```

### Run Flask Server

```bash
python app.py
```

Server starts at:

```text
http://127.0.0.1:5000
```

---

## API Endpoints

### Health Check

```http
GET /health
```

Response:

```json
{
  "status": "running"
}
```

### SMS Prediction

```http
POST /predict
```

Request:

```json
{
  "message": "Congratulations! You have won a free prize."
}
```

Response:

```json
{
  "prediction": "Spam",
  "probability": 0.98
}


## Dataset

The project uses the SMS Spam Collection Dataset containing labeled SMS messages categorized as:

- Spam
- Ham (Safe)



## Results

- High classification accuracy using TF-IDF and Naive Bayes
- Real-time SMS fraud detection
- Lightweight and efficient architecture
- User-friendly Android interface


## Future Enhancements

- Support for iOS and macOS platforms
- Deep Learning-based classification models
- Larger training datasets
- Enhanced security mechanisms
- Advanced analytics dashboard



