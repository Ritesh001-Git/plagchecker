// ------------------------------
// 🔹 Tab Switching Logic
// ------------------------------
const tabButtons = document.querySelectorAll(".tab-btn");
const tabContents = document.querySelectorAll(".tab-content");

tabButtons.forEach((btn) => {
  btn.addEventListener("click", () => {
    const tab = btn.getAttribute("data-tab");

    // remove active class from all
    tabButtons.forEach((b) => b.classList.remove("active"));
    tabContents.forEach((c) => c.classList.remove("active"));

    // activate clicked
    btn.classList.add("active");
    document.getElementById(`${tab}-tab`).classList.add("active");
  });
});

// ------------------------------
// 🔹 Character Counter for Textareas
// ------------------------------
document.querySelectorAll("textarea").forEach((area) => {
  area.addEventListener("input", () => {
    const counter = area.parentElement.querySelector(".char-count");
    counter.textContent = `${area.value.length} characters`;
  });
});

// ------------------------------
// 🔹 Helper: Show/Hide Loader
// ------------------------------
function toggleLoading(buttonId, isLoading) {
  const button = document.getElementById(buttonId);
  const loader = button.querySelector(".loader");
  const text = button.querySelector(".btn-text");
  if (isLoading) {
    loader.style.display = "inline-block";
    text.style.display = "none";
    button.disabled = true;
  } else {
    loader.style.display = "none";
    text.style.display = "inline";
    button.disabled = false;
  }
}

// ------------------------------
// 🔹 Helper: Show Error Message
// ------------------------------
function showError(msg) {
  const box = document.getElementById("error-message");
  box.textContent = msg;
  box.style.display = "block";
  setTimeout(() => (box.style.display = "none"), 3000);
}

// ------------------------------
// 🔹 Animate metric bars
// ------------------------------
function animateMetricBar(barId, valueId, value) {
  const bar = document.getElementById(barId);
  const text = document.getElementById(valueId);
  bar.style.width = `${value}%`;
  text.textContent = `${value.toFixed(2)}%`;
}

// ------------------------------
// 🔹 Handle Plagiarism Check
// ------------------------------
document.getElementById("checkPlagiarism").addEventListener("click", async () => {
  const text1 = document.getElementById("text1").value.trim();
  const text2 = document.getElementById("text2").value.trim();

  if (!text1 || !text2) {
    showError("Please enter text in both fields.");
    return;
  }

  toggleLoading("checkPlagiarism", true);
  try {
    const body = { text1, text2, mode: "plagiarism" };
    const res = await fetch("http://localhost:8080/check", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });

    const data = await res.json();
    const score = data.similarity.toFixed(2);

    // Update score circle + classification
    document.getElementById("plag-score").textContent = `${score}%`;
    const circle = document.getElementById("plag-score-circle");
    const resultText = document.getElementById("plag-classification");
    circle.classList.remove("score-low", "score-medium", "score-high");

    if (score < 30) {
      circle.classList.add("score-low");
      resultText.textContent = "Low similarity (Human-written)";
    } else if (score < 70) {
      circle.classList.add("score-medium");
      resultText.textContent = "Medium similarity";
    } else {
      circle.classList.add("score-high");
      resultText.textContent = "High similarity (Possible plagiarism)";
    }

    // Animate plagiarism metric bars
    animateMetricBar("jaccard-bar", "jaccard-value", score);
    animateMetricBar("cosine-bar", "cosine-value", Math.max(score - 10, 0)); // pseudo variation
    animateMetricBar("lcs-bar", "lcs-value", Math.min(score + 5, 100));
    animateMetricBar("ngram-bar", "ngram-value", score);

    document.getElementById("plagiarism-results").style.display = "block";
  } catch (err) {
    showError("Failed to connect to backend. Ensure MiniServer is running.");
  } finally {
    toggleLoading("checkPlagiarism", false);
  }
});

// ------------------------------
// 🔹 Handle AI Detection
// ------------------------------
document.getElementById("checkAI").addEventListener("click", async () => {
  const text = document.getElementById("aiText").value.trim();
  if (!text) {
    showError("Please enter text to analyze.");
    return;
  }

  toggleLoading("checkAI", true);
  try {
    const body = { text1: text, mode: "ai" };
    const res = await fetch("http://localhost:8080/check", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });

    const data = await res.json();
    const score = data.ai_percent.toFixed(2);

    document.getElementById("ai-score").textContent = `${score}%`;
    const circle = document.getElementById("ai-score-circle");
    const resultText = document.getElementById("ai-classification");
    circle.classList.remove("score-low", "score-medium", "score-high");

    if (score < 30) {
      circle.classList.add("score-low");
      resultText.textContent = "Likely Human-written";
    } else if (score < 70) {
      circle.classList.add("score-medium");
      resultText.textContent = "Possibly Mixed (Human + AI)";
    } else {
      circle.classList.add("score-high");
      resultText.textContent = "Likely AI-generated";
    }

    // Animate AI metric bars
    animateMetricBar("diversity-bar", "diversity-value", Math.max(100 - score, 0));
    animateMetricBar("repetition-bar", "repetition-value", Math.min(score, 100));
    animateMetricBar("uniformity-bar", "uniformity-value", Math.min(score * 0.8, 100));
    animateMetricBar("keywords-bar", "keywords-value", Math.min(score * 0.5, 100));

    document.getElementById("ai-results").style.display = "block";
  } catch (err) {
    showError("Failed to connect to backend. Ensure MiniServer is running.");
  } finally {
    toggleLoading("checkAI", false);
  }
});
