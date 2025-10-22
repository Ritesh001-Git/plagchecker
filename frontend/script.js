async function checkPlagiarism(mode) {
    const text1 = document.getElementById("input1").value;
    const text2 = document.getElementById("input2")?.value || "";
  
    const body = { text1, text2, mode };
    const res = await fetch("http://localhost:8080/check", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });
  
    const data = await res.json();
    if (mode === "ai") {
      alert(`AI-generated content: ${data.ai_percent.toFixed(2)}%`);
    } else {
      alert(`Similarity: ${data.similarity.toFixed(2)}%`);
    }
  }
  