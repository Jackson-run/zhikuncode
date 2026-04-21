# ZhikunCode

**An AI-powered intelligent coding assistant** with multi-model support, multi-agent collaboration, and IDE-level tooling — built for developers who want AI to truly understand their codebase.

## ✨ Features

- 🤖 **Multi-Model Support** — Qwen, DeepSeek, OpenAI, Anthropic Claude, and any OpenAI-compatible API
- 🧠 **Multi-Agent Collaboration** — Team and Swarm modes for complex task orchestration
- 🛡️ **Secure Bash Execution** — 8-layer safety checks with sandboxed command execution
- 🔌 **MCP Integration** — Model Context Protocol for extensible tool capabilities
- 🛠️ **IDE-Level Tooling** — File editing, code search, symbol analysis, and project navigation
- 🔄 **Real-Time Communication** — WebSocket-based streaming for instant feedback
- 🔐 **Permission Control** — Fine-grained security sandbox with access policies
- 📝 **Persistent Memory** — Cross-session context retention for smarter assistance

## 🏗️ Tech Stack

| Layer | Technologies |
|-------|-------------|
| **Backend** | Java 21, Spring Boot 3.x, WebSocket, SQLite |
| **Frontend** | React 18, TypeScript, Vite, TailwindCSS |
| **Python Service** | FastAPI, Uvicorn, Code Analysis Tools |
| **Build Tools** | Maven, npm, pip |
| **Deployment** | Docker, Docker Compose |

## 🚀 Quick Start

### Option 1: Docker (Recommended)

```bash
git clone https://github.com/zhikuncode/zhikuncode.git
cd zhikuncode

# Configure your API key
cp .env .env.local
# Edit .env.local and set LLM_API_KEY=your-api-key

# Start
docker compose up -d

# Open http://localhost:8080
```

### Option 2: Local Development

**Prerequisites:** JDK 21, Node.js 20+, Python 3.11+

```bash
git clone https://github.com/zhikuncode/zhikuncode.git
cd zhikuncode
```

**1. Configure environment**

```bash
cp .env .env.local
# Edit .env.local and set LLM_API_KEY=your-api-key
```

**2. Start all services**

```bash
./start.sh
```

This launches all three services:
- **Backend** → `http://localhost:8080`
- **Python Service** → `http://localhost:8000`
- **Frontend** → `http://localhost:5173`

Or start each service manually:

```bash
# Backend
cd backend && ./mvnw spring-boot:run -DskipTests

# Python Service
cd python-service
python -m venv venv && source venv/bin/activate
pip install -r requirements.txt
uvicorn src.main:app --host 0.0.0.0 --port 8000

# Frontend
cd frontend && npm install && npm run dev
```

## 🏛️ Architecture

```
┌─────────────┐     WebSocket/HTTP     ┌──────────────────┐
│   Frontend   │ ◄──────────────────► │     Backend      │
│  React/Vite  │                       │  Spring Boot 3   │
│  :5173       │                       │  :8080           │
└─────────────┘                       └────────┬─────────┘
                                               │ HTTP
                                               ▼
                                      ┌──────────────────┐
                                      │  Python Service   │
                                      │  FastAPI/Uvicorn  │
                                      │  :8000            │
                                      └──────────────────┘
```

- **Backend (Java)** — Core orchestration, LLM API routing, agent management, tool execution, session persistence, and security enforcement.
- **Frontend (React)** — Interactive chat UI, file explorer, settings panel, and real-time streaming display.
- **Python Service** — Code analysis, AST parsing, and specialized processing tasks.

## ⚙️ Configuration

Environment variables are managed via `.env` file. Key settings:

| Variable | Required | Description |
|----------|----------|-------------|
| `LLM_API_KEY` | ✅ | Your LLM provider API key |
| `ZHIKUN_PORT` | — | Host port mapping (default: `8080`) |
| `SPRING_PROFILES_ACTIVE` | — | Spring profile (default: `production`) |
| `JAVA_OPTS` | — | JVM options |
| `WORKSPACE_PATH` | — | Project workspace to mount |

See [`.env`](.env) for the full configuration template.

## 🤝 Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on how to get started.

## 📄 License

This project is licensed under the [MIT License](LICENSE).
