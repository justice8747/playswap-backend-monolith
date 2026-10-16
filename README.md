# PlaySwap Backend — Modular Monolith

PlaySwap is a music playlist transfer platform connecting Spotify and YouTube Music.

This repository contains the **consolidated, resource-efficient Spring Boot modular monolith** refactored from a distributed microservices architecture into a single, high-performance, deployable application.

---

## Key Advantages

- **Resource Efficient:** Runs on **~350-500 MB RAM** instead of ~2 GB, easily deployable on a 1 GB or 2 GB VM.
- **Fast & Direct:** Replaced internal network HTTP calls between services with in-process Java method calls.
- **Unified Architecture:** Non-blocking Spring WebFlux on Netty, handling hundreds of concurrent external API calls with minimal thread overhead.
- **Zero Frontend Breaking Changes:** Exposes port 9000 with 100% backward-compatible API contracts.

---

## Opening and Running in IntelliJ IDEA

### 1. Open the Project
1. Launch **IntelliJ IDEA**.
2. Click **File -> Open...** (or **Open** from the welcome screen).
3. Select the folder `playswap-backend-monolith` (or select `pom.xml` directly) and click **OK**.
4. When prompted, select **Open as Project** and **Trust Project**.
5. IntelliJ will automatically detect Maven, download dependencies, and index the project.

### 2. Configure Java 21 SDK
1. Go to **File -> Project Structure... -> Project**.
2. Ensure **SDK** is set to **Java 21** (or download Temurin / Corretto 21 via IntelliJ).
3. Set **Language Level** to **21**.

### 3. Setup Environment Variables in IntelliJ
1. In the project tree, navigate to:  
   `src/main/java/com/justice/playswap/PlaySwapApplication.java`
2. Right-click `PlaySwapApplication.java` and click **Modify Run Configuration...**.
3. Under **Environment variables**, click the browse icon and paste your variables (or use the `.env` IntelliJ plugin):
   ```
   DB_AUTH_USERNAME=postgres;DB_AUTH_PASSWORD=postgres;SPOTIFY_CLIENT_ID=your_spotify_id;SPOTIFY_CLIENT_SECRET=your_spotify_secret;GOOGLE_CLIENT_ID=your_google_id;GOOGLE_CLIENT_SECRET=your_google_secret
   ```
4. Click **Apply** and **Run** (or `Shift + F10`).

---

## Running via Command Line (Maven)

### Build and Run Tests
```powershell
.\mvnw.cmd clean test
```

### Run Locally
```powershell
.\mvnw.cmd spring-boot:run
```

### Package Production JAR
```powershell
.\mvnw.cmd clean package
java -jar target/playswap-backend-1.0.0.jar
```

---

## Docker & Docker Compose

### 1. Configure Secrets
```bash
cp .env.example .env
# Edit .env with your Spotify, Google, and Database credentials
```

### 2. Run with Docker Compose
```bash
docker compose up -d --build
```
This starts:
- `playswap-db` (PostgreSQL 16 on internal network)
- `playswap-backend` (Spring Boot Modular Monolith exposed on port 9000)

### 3. Check Health
```bash
curl http://localhost:9000/actuator/health
```

---

## Package Architecture

```
src/main/java/com/justice/playswap/
├── PlaySwapApplication.java         # Main Spring Boot Entry Point
├── auth/                            # OAuth2, JWT Security, User & Token Persistence
├── spotify/                         # Spotify API Client, Models, and Operations
├── ytmusic/                         # YouTube Music Client, 4-Tier Track Matching
├── transfer/                        # Playlist Transfer Orchestration & History
├── common/                          # In-Process Interfaces (TokenProvider, SpotifyOperations, YtmusicOperations)
└── config/                          # Security WebFilterChain, CORS, WebClient Config
```