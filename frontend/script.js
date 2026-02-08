// ------------------------------
// 🔹 Tab Switching Logic
// ------------------------------
const tabButtons = document.querySelectorAll(".tab-btn");
const tabContents = document.querySelectorAll(".tab-content");

tabButtons.forEach((btn) => {
  btn.addEventListener("click", () => {
    const tab = btn.getAttribute("data-tab");
    tabButtons.forEach((b) => b.classList.remove("active"));
    tabContents.forEach((c) => c.classList.remove("active"));
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
    const count = area.value.length;
    counter.textContent = `${count} characters`;
    
    // Visual feedback for minimum requirement (AI detection)
    if (area.id === "aiText") {
      if (count < 100 && count > 0) {
        counter.style.color = "#ef4444";
      } else {
        counter.style.color = "#64748b";
      }
    }
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
  setTimeout(() => (box.style.display = "none"), 5000);
}

// ------------------------------
// 🔹 Animate Metric Bar
// ------------------------------
function animateMetricBar(barId, valueId, value, suffix = "%") {
  const bar = document.getElementById(barId);
  const text = document.getElementById(valueId);
  if (!bar || !text) return;
  
  // Clamp value between 0-100 for bar width
  const barWidth = Math.max(0, Math.min(100, value));
  bar.style.width = `${barWidth}%`;
  text.textContent = `${value.toFixed(2)}${suffix}`;
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

    if (!res.ok) throw new Error("Network response was not ok");
    const data = await res.json();
    if (data.error) throw new Error(data.error);

    const score = data.similarity ?? 0;
    const jaccard = data.jaccard ?? 0;
    const cosine = data.cosine ?? 0;
    const lcs = data.lcs ?? 0;
    const ngram = data.ngram ?? 0;

    // Update Plagiarism Score
    document.getElementById("plag-score").textContent = `${score.toFixed(2)}%`;
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

    // Animate Metrics
    animateMetricBar("jaccard-bar", "jaccard-value", jaccard);
    animateMetricBar("cosine-bar", "cosine-value", cosine);
    animateMetricBar("lcs-bar", "lcs-value", lcs);
    animateMetricBar("ngram-bar", "ngram-value", ngram);

    document.getElementById("plagiarism-results").style.display = "block";
    
    // Scroll to results
    document.getElementById("plagiarism-results").scrollIntoView({ 
      behavior: "smooth", 
      block: "nearest" 
    });
  } catch (err) {
    console.error(err);
    showError("Failed to connect to backend. Please ensure the server is running.");
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

  if (text.length < 100) {
    showError("Please enter at least 100 characters for accurate AI detection.");
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

    if (!res.ok) throw new Error("Network response was not ok");
    const data = await res.json();
    if (data.error) throw new Error(data.error);

    // Extract metrics from backend
    const score = data.ai_percent ?? 0;
    const diversity = data.diversity ?? 0;
    const repetition = data.repetition ?? 0;
    const uniformity = data.uniformity ?? 0;
    const burstiness = data.burstiness ?? 0;
    const keywords = data.keywords ?? 0;
    const perplexity = data.perplexity ?? 0;
    const confidence = data.confidence ?? "Medium";

    console.log("AI Detection Results:", data);

    // Update AI Score Circle
    document.getElementById("ai-score").textContent = `${score.toFixed(2)}%`;
    const circle = document.getElementById("ai-score-circle");
    const resultText = document.getElementById("ai-classification");
    const confidenceBadge = document.getElementById("confidence-value");
    
    circle.classList.remove("score-low", "score-medium", "score-high");
    confidenceBadge.textContent = confidence;

    if (score < 30) {
      circle.classList.add("score-low");
      resultText.textContent = "Likely Human-written";
    } else if (score < 50) {
      circle.classList.add("score-medium");
      resultText.textContent = "Possibly Human with AI assistance";
    } else if (score < 75) {
      circle.classList.add("score-medium");
      resultText.textContent = "Possibly AI-generated or Mixed";
    } else if (score < 90) {
      circle.classList.add("score-high");
      resultText.textContent = "Likely AI-generated";
    } else {
      circle.classList.add("score-high");
      resultText.textContent = "Highly likely AI-generated (Claude-style)";
    }

    // Animate AI Metrics
    animateMetricBar("diversity-bar", "diversity-value", diversity);
    animateMetricBar("burstiness-bar", "burstiness-value", burstiness);
    animateMetricBar("uniformity-bar", "uniformity-value", uniformity);
    animateMetricBar("repetition-bar", "repetition-value", repetition);
    animateMetricBar("keywords-bar", "keywords-value", keywords);
    
    // Perplexity is not a percentage, display as raw value
    animateMetricBar("perplexity-bar", "perplexity-value", 
                     Math.min(100, perplexity), "");

    document.getElementById("ai-results").style.display = "block";
    
    // Scroll to results
    document.getElementById("ai-results").scrollIntoView({ 
      behavior: "smooth", 
      block: "nearest" 
    });
  } catch (err) {
    console.error(err);
    showError("Failed to connect to backend. Please ensure the server is running on port 8080.");
  } finally {
    toggleLoading("checkAI", false);
  }
});

// ------------------------------
// 🔹 Tooltip Enhancement
// ------------------------------
document.addEventListener("DOMContentLoaded", () => {
  const tooltips = document.querySelectorAll(".metric-tooltip");
  tooltips.forEach(tooltip => {
    tooltip.style.cursor = "help";
  });
});
