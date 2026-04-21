#!/bin/sh
# =============================================================================
# ZhikunCode — Container Entrypoint
# Starts Java backend (which internally manages the Python subprocess)
# =============================================================================
set -e

# ---- Resolve Python virtual environment ----
export PATH="/app/python-service/.venv/bin:${PATH}"
export PYTHONPATH="/app/python-service/src"

# ---- Ensure data directories exist ----
mkdir -p /app/data /app/workspace

# ---- Startup banner ----
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  ZhikunCode — Starting..."
echo "  Java:   $(java --version 2>&1 | head -1)"
echo "  Python: $(python --version 2>&1)"
echo "  Profile: ${SPRING_PROFILES_ACTIVE:-default}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# ---- Launch Java application ----
# PythonProcessManager inside Spring Boot will start the Python subprocess automatically
exec java \
    ${JAVA_OPTS} \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.profiles.active="${SPRING_PROFILES_ACTIVE:-production}" \
    -jar /app/app.jar \
    "$@"
