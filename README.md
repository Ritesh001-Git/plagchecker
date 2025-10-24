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
#### For Max/Linux
```bash
cd backend
javac -cp .:json-20250517.jar *.java
```
#### For Windows
```bash
cd backend
javac -cp .;json-20250517.jar *.java
```

### 3️⃣ Run Server
#### For Mac/Linux
```bash
java -cp .:json-20250517.jar MiniServer
```
#### For Windows
```bash
java -cp .;json-20250517.jar MiniServer
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

### 🔹 Example Request (Plagiarism)
```json
{
  "mode": "plagiarism",
  "text1": "Sample text A",
  "text2": "Sample text B"
}
```

### 🔹 Example Request (AI Detection)
```json
{
  "mode": "ai",
  "text1": "This is a generated paragraph..."
}
```

### 🔹 Example Response
```json
{
  "ai_percent": 72.5,
  "diversity": 63.0,
  "repetition": 12.0,
  "uniformity": 48.0,
  "keywords": 18.0
}
```
---

## 🧪 Testing

Use these quick test cases:

| Type | Input | Expected |
|------|--------|----------|
| Plagiarism | Two identical paragraphs | 100% similarity |
| Plagiarism | Two totally different texts | 0–20% similarity |
| AI Detection | ChatGPT-generated text | >70% AI likelihood |
| AI Detection | Human-written article | <30% AI likelihood |

---

## 🛠 Troubleshooting

| Issue | Solution |
|--------|-----------|
| ❌ *“Failed to connect to backend”* | Ensure `MiniServer` is running at `http://localhost:8080`. |
| ❌ *CORS or network error* | Use the project locally (both frontend and backend on the same machine). |
| ⚠️ *AI percent always low* | Fine-tune `PlagiarismCheckerAi` thresholds or pattern weights. |

---

## 📜 License
[MIT LICENSE](LICENCE) — Free for personal and educational use.

---

## 🙏 Thank You
Thank you for checking out this project!
If you find it useful, consider ⭐ starring the repository or sharing it with others who might benefit from it.
Your feedback and support mean a lot — they help make tools like this better every day.

Happy Coding! 🚀

---

## 👨‍💻 Author
**Ritesh Kumar Swain**  
*Built with 💻, logic, and a lot of debugging.*
