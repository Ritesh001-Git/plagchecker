# 🔧 Troubleshooting Guide

## Common Issues and Solutions

### ❌ Issue 1: "404 Not Found" when accessing http://localhost:8080

**Cause:** Server can't find the frontend files.

**Solution:**

**Option A - Run from project root (RECOMMENDED):**
```bash
# Make sure you're in the ai-content-detector directory
cd ai-content-detector

# Then run
./run.sh
# or on Windows
run.bat
```

**Option B - Manual fix if still having issues:**

1. Check your directory structure:
```
ai-content-detector/
├── frontend/
│   ├── index.html
│   ├── style.css
│   └── script.js
└── src/
    └── main/
        └── java/
            └── com/
                └── detector/
```

2. Make sure you run Maven from the **ai-content-detector** directory (where pom.xml is located):
```bash
pwd  # Should show: .../ai-content-detector
ls   # Should show: pom.xml, frontend/, src/, etc.
```

3. If frontend folder is missing, extract the ZIP file again completely.

**Option C - Alternative: Use absolute path:**

Edit `src/main/java/com/detector/server/MiniServer.java`:
```java
private static final String FRONTEND_DIR = "/full/path/to/ai-content-detector/frontend";
```

Replace with your actual full path, then rebuild:
```bash
mvn clean package
```

---

### ❌ Issue 2: Maven build fails

**Error:** `mvn: command not found`

**Solution:**
```bash
# Install Maven
# On Mac:
brew install maven

# On Ubuntu/Debian:
sudo apt-get install maven

# On Windows:
# Download from https://maven.apache.org/download.cgi
# Add to PATH
```

**Error:** `JAVA_HOME not set`

**Solution:**
```bash
# Find Java location
which java

# Set JAVA_HOME (add to ~/.bashrc or ~/.zshrc)
export JAVA_HOME=/path/to/java
export PATH=$JAVA_HOME/bin:$PATH

# On Windows, set environment variable in System Properties
```

---

### ❌ Issue 3: Port 8080 already in use

**Error:** `Address already in use`

**Solution A - Kill the process:**
```bash
# On Unix/Mac:
lsof -ti:8080 | xargs kill -9

# On Windows:
netstat -ano | findstr :8080
taskkill /PID <PID_NUMBER> /F
```

**Solution B - Change the port:**

Edit `src/main/java/com/detector/server/MiniServer.java`:
```java
private static final int PORT = 9090;  // Change to any available port
```

Then rebuild and access at `http://localhost:9090`

---

### ❌ Issue 4: Java version error

**Error:** `class file has wrong version` or `UnsupportedClassVersionError`

**Solution:**
```bash
# Check Java version
java -version

# Need Java 17 or higher
# Download from: https://adoptium.net/

# On Mac:
brew install openjdk@17

# Verify installation
java -version  # Should show 17.x.x or higher
```

---

### ❌ Issue 5: Dependencies download fails

**Error:** `Could not resolve dependencies` or `Connection timed out`

**Solution:**

1. **Check internet connection**

2. **Clear Maven cache:**
```bash
rm -rf ~/.m2/repository
mvn clean install
```

3. **Try with proxy (if behind corporate firewall):**
Edit `~/.m2/settings.xml`:
```xml
<proxies>
  <proxy>
    <host>your.proxy.host</host>
    <port>8080</port>
  </proxy>
</proxies>
```

---

### ❌ Issue 6: Frontend can't connect to backend

**Error in browser console:** `Failed to fetch` or `CORS error`

**Solution:**

1. **Verify server is running:**
   - Look for "Server running on http://localhost:8080" in terminal
   - Try accessing http://localhost:8080/health

2. **Clear browser cache:**
   - Press Ctrl+Shift+Delete
   - Clear cached images and files

3. **Try incognito/private window**

4. **Check firewall:**
   - Temporarily disable firewall to test
   - Add exception for Java/port 8080

---

### ❌ Issue 7: "Text too short" error

**Error:** `Text too short. Minimum 100 characters required`

**Solution:**
- AI detection requires at least 100 characters for accuracy
- Add more text or use a longer sample
- For testing, use the examples in QUICK_START.md

---

### ❌ Issue 8: Slow performance

**Issue:** Detection takes > 5 seconds

**Solution:**

1. **Check text length:**
   - Very long texts (>10,000 words) take longer
   - Consider breaking into smaller chunks

2. **Check system resources:**
```bash
# Check memory
free -h  # On Unix
# Task Manager on Windows

# Check CPU
top  # On Unix
```

3. **Increase heap size:**
```bash
export MAVEN_OPTS="-Xmx2g"
mvn exec:java -Dexec.mainClass="com.detector.server.MiniServer"
```

---

### ❌ Issue 9: Browser doesn't open automatically

**Solution:**
- Manually open http://localhost:8080 in your browser
- Check if Desktop.isDesktopSupported() is true for your system
- This is normal on headless servers

---

### ❌ Issue 10: File permissions error (Unix/Mac)

**Error:** `Permission denied` when running `./run.sh`

**Solution:**
```bash
chmod +x run.sh
./run.sh
```

---

## 🔍 Debug Mode

Enable detailed logging:

1. Edit `src/main/resources/logback.xml`:
```xml
<logger name="com.detector" level="DEBUG"/>
```

2. Rebuild:
```bash
mvn clean package
```

3. Check logs in `logs/ai-detector.log`

---

## 🧪 Test Installation

Run this to verify everything works:

```bash
# 1. Build
mvn clean package

# 2. Run tests
mvn test

# 3. Check server starts
mvn exec:java -Dexec.mainClass="com.detector.server.MiniServer"

# 4. In another terminal, test API
curl http://localhost:8080/health
```

Expected response:
```json
{"status":"healthy","service":"AI Content Detector","version":"1.0.0"}
```

---

## 📞 Still Having Issues?

### Checklist:
- [ ] Java 17+ installed and in PATH
- [ ] Maven 3.6+ installed and in PATH
- [ ] In correct directory (ai-content-detector/)
- [ ] Port 8080 is available
- [ ] Internet connection available (for Maven dependencies)
- [ ] frontend/ folder exists in project root
- [ ] No antivirus blocking Java

### Get Help:

1. **Check logs:**
   - Terminal output
   - `logs/ai-detector.log`

2. **Verify file structure:**
```bash
tree -L 3  # Or use 'find .' to see all files
```

3. **Clean rebuild:**
```bash
mvn clean
rm -rf target/
mvn install
```

4. **Test with minimal example:**
```bash
# Just try to compile
mvn compile

# Then run main class directly
mvn exec:java -Dexec.mainClass="com.detector.server.MiniServer"
```

---

## 💡 Quick Fixes Summary

| Problem | Quick Fix |
|---------|-----------|
| 404 Error | Run from project root where pom.xml is |
| Port in use | Change PORT in MiniServer.java |
| Maven not found | Install Maven, set PATH |
| Java version | Install Java 17+, set JAVA_HOME |
| Build fails | `mvn clean install` |
| Frontend issues | Check frontend/ folder exists |
| Slow performance | Reduce text length, increase heap |

---

**Most Common Solution:** Run from the correct directory!

```bash
cd ai-content-detector  # The folder with pom.xml
./run.sh               # Should work now!
```
