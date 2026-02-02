#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROJECT_ROOT"

ENV_FILE=".env.production"

check_env() {
    if [ ! -f "$ENV_FILE" ]; then
        echo "ERROR: $ENV_FILE not found."
        echo "Run: cp .env.production.template .env.production"
        echo "Then fill in the real values."
        exit 1
    fi
}

build_frontend() {
    echo "=== Building frontend ==="
    cd "$PROJECT_ROOT/frontend"
    if [ "${1:-full}" = "full" ]; then
        echo "--- npm install ---"
        npm install
    fi
    echo "--- npm run build ---"
    npm run build
    cd "$PROJECT_ROOT"
    echo "=== Frontend build complete ==="
}

deploy() {
    check_env
    build_frontend "full"

    echo "=== Building and starting Docker services ==="
    docker compose --env-file "$ENV_FILE" build --no-cache backend
    docker compose --env-file "$ENV_FILE" up -d
    echo "=== Deployment complete ==="
    echo ""
    status
}

update() {
    check_env
    build_frontend "skip-install"

    echo "=== Rebuilding backend ==="
    docker compose --env-file "$ENV_FILE" build backend
    docker compose --env-file "$ENV_FILE" up -d
    echo "=== Update complete ==="
    echo ""
    status
}

restart() {
    check_env
    echo "=== Restarting services ==="
    docker compose --env-file "$ENV_FILE" restart
    echo "=== Restart complete ==="
}

logs() {
    check_env
    docker compose --env-file "$ENV_FILE" logs -f --tail=100 "${2:-}"
}

status() {
    check_env
    echo "=== Service Status ==="
    docker compose --env-file "$ENV_FILE" ps
}

stop() {
    check_env
    echo "=== Stopping services ==="
    docker compose --env-file "$ENV_FILE" down
    echo "=== Stopped ==="
}

usage() {
    echo "Usage: $0 {deploy|update|restart|logs|status|stop}"
    echo ""
    echo "  deploy   Full deployment: npm install + vite build + docker build + up"
    echo "  update   Quick update: skip npm install, rebuild backend + up"
    echo "  restart  Restart all containers"
    echo "  logs     Follow container logs (optionally: logs <service>)"
    echo "  status   Show container status"
    echo "  stop     Stop all containers"
}

case "${1:-}" in
    deploy)  deploy ;;
    update)  update ;;
    restart) restart ;;
    logs)    logs "$@" ;;
    status)  status ;;
    stop)    stop ;;
    *)       usage; exit 1 ;;
esac
