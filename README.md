# PlaySwap Backend — Modular Monolith

PlaySwap is a music playlist transfer platform connecting Spotify and YouTube Music.

This repository contains the **consolidated, resource-efficient Spring Boot modular monolith** refactored from a distributed microservices architecture into a single, high-performance, deployable application.

---

## Key Advantages

- **Resource Efficient:** Runs on **~350-500 MB RAM** instead of ~2 GB, easily deployable on a 1 GB or 2 GB VM.
- **Fast & Direct:** Replaced internal network HTTP calls between services with in-process Java method calls.
- **Unified Architecture:** Non-blocking Spring WebFlux on Netty, handling hundreds of concurrent external API calls with minimal thread overhead.
- **Zero Frontend Breaking Changes:** Exposes port 9000 with 100% backward-compatible API contracts.

