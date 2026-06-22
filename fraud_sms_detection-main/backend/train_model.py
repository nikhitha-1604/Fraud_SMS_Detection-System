import argparse
import html
import os
import re

import joblib
import nltk
import pandas as pd
from nltk.corpus import stopwords
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics import accuracy_score, classification_report
from sklearn.model_selection import train_test_split
from sklearn.linear_model import LogisticRegression
from sklearn.pipeline import Pipeline


def clean_text(s: str) -> str:
    s = "" if s is None else str(s)
    # Decode common HTML entities present in SMS datasets
    s = html.unescape(s)
    # Normalize URLs and numbers to reduce sparsity
    s = re.sub(r"http\\S+|www\\.\\S+", " URL ", s, flags=re.IGNORECASE)
    s = re.sub(r"\\b\\d+\\b", " NUM ", s)
    # Remove leftover tags if any
    s = re.sub(r"<[^>]+>", " ", s)
    s = s.replace("\\n", " ").replace("\\r", " ")
    s = re.sub(r"\\s+", " ", s).strip()
    return s


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--dataset",
        required=True,
        help="Path to CSV dataset with columns: label,text",
    )
    parser.add_argument(
        "--out_dir",
        default="models",
        help="Folder to store trained model artifacts",
    )
    parser.add_argument(
        "--model_name",
        default="sms_fraud_pipeline.joblib",
        help="Model filename",
    )
    args = parser.parse_args()

    df = pd.read_csv(args.dataset)
    if "label" not in df.columns or "text" not in df.columns:
        raise ValueError("Dataset must contain columns: 'label' and 'text'")

    df = df.dropna(subset=["label", "text"]).copy()
    df["label"] = df["label"].astype(str).str.lower().str.strip()
    df["text"] = df["text"].astype(str).map(clean_text)

    # This project is binary classification: `spam` vs `genuine`.
    # If your dataset contains extra labels, drop them so LogisticRegression
    # (with the binary-friendly `liblinear` solver) can train properly.
    allowed_labels = {"spam", "genuine"}
    df = df[df["label"].isin(allowed_labels)].copy()
    if df.empty:
        raise ValueError("After filtering, dataset is empty. Check label values in the CSV.")
    if df["label"].nunique() < 2:
        raise ValueError(f"Need at least 2 labels for training. Found: {df['label'].unique()}")

    # Download stopwords if missing (nltk sometimes throws LookupError)
    try:
        stop_words = stopwords.words("english")
    except LookupError:
        nltk.download("stopwords")
        stop_words = stopwords.words("english")

    X = df["text"]
    y = df["label"]

    X_train, X_test, y_train, y_test = train_test_split(
        X,
        y,
        test_size=0.2,
        random_state=42,
        stratify=y,
    )

    vectorizer = TfidfVectorizer(
        lowercase=True,
        stop_words=stop_words,
        max_df=0.95,
        min_df=2,
        # Longer n-grams help catch urgent/payment patterns in SMS text.
        ngram_range=(1, 3),
        sublinear_tf=True,
    )

    # Logistic Regression tends to perform better than Naive Bayes for TF-IDF text.
    clf = LogisticRegression(
        max_iter=2000,
        solver="liblinear",
        class_weight="balanced",
    )

    pipeline = Pipeline(
        [
            ("tfidf", vectorizer),
            ("clf", clf),
        ]
    )

    pipeline.fit(X_train, y_train)

    y_pred = pipeline.predict(X_test)
    print("Accuracy:", accuracy_score(y_test, y_pred))
    print(classification_report(y_test, y_pred, zero_division=0))

    os.makedirs(args.out_dir, exist_ok=True)
    out_path = os.path.join(args.out_dir, args.model_name)
    joblib.dump(pipeline, out_path)
    print(f"Saved model to: {out_path}")


if __name__ == "__main__":
    main()

