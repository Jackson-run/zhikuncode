# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-04-20

### Added
- Multi-model LLM support (通义千问, DeepSeek, Moonshot, OpenAI-compatible APIs, and more)
- Multi-agent collaboration with Team and Swarm modes
- 47 built-in tools (Bash, file editing, search, Git, etc.)
- MCP (Model Context Protocol) integration for extensible tool ecosystem
- 8-layer Bash security pipeline with permission controls
- WebSocket-based real-time communication
- React frontend with TailwindCSS
- Python analysis service with FastAPI
- Docker single-container deployment
- Comprehensive permission and path security system

### Security
- Path traversal protection
- Sensitive file access controls
- Command injection prevention
- Permission-based tool execution

[1.0.0]: https://github.com/zhikuncode/zhikuncode/releases/tag/v1.0.0
