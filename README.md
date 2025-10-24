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




javac -cp .:json-20250517.jar *.java
java -cp .:json-20250517.jar MiniServer