# 🧠 Plagiarism & AI Content Detector

A lightweight full-stack tool that checks **text similarity (plagiarism)** and **AI-generated likelihood** using natural language metrics.  
Built with **Java (HTTP Server + NLP logic)** and a **JavaScript frontend** for real-time results.

---

## 🚀 Features

- 🔍 **Plagiarism Detection**
  - Uses multiple metrics (Cosine Similarity, Jaccard Index, LCS, N-gram).
  - Gives detailed percentage match and qualitative interpretation (Low / Medium / High).

- 🤖 **AI Content Detection**
  - Detects likely AI-generated writing using lexical and structural analysis.
  - Metrics include **diversity**, **repetition**, **uniformity**, and **keyword patterns**.
  - Displays AI-likelihood percentage and explanation.

- 💻 **Frontend**
  - Responsive UI with animated metric bars and live scores.
  - Two tabs: “Plagiarism Check” and “AI Detection”.
  - Visual feedback (low / medium / high) using color-coded result circles.

- ⚙️ **Backend**
  - Lightweight Java HTTP server (`com.sun.net.httpserver.HttpServer`).
  - JSON-based request/response via POST endpoint `/check`.
  - No external dependencies except `json-20250517.jar`.

---

## 🧩 Folder Structure

```
project/
│
├── backend/
│   ├── MiniServer.java
│   ├── PlagiarismChecker.java
│   ├── PlagiarismCheckerAi.java
│   ├── json-20250517.jar
│
├── frontend/
│   ├── index.html
│   ├── style.css
│   └── script.js
│
└── README.md
```

---

## ⚡ Installation & Setup

### 1️⃣ Prerequisites
- Java 17 or higher  
- A modern browser (Chrome, Edge, Firefox, etc.)

### 2️⃣ Compile Backend
```bash
cd backend
javac -cp .:json-20250517.jar *.java
```

### 3️⃣ Run Server
```bash
java -cp .:json-20250517.jar MiniServer
```
If successful, you should see:
```
🚀 Server running on http://localhost:8080
```
---

## 📡 API Endpoint

**POST `/check`**

| Parameter | Type | Description |
|------------|------|-------------|
| `mode` | string | `"plagiarism"` or `"ai"` |
| `text1` | string | First text input (or the only text for AI check) |
| `text2` | string | Second text (used only for plagiarism) |