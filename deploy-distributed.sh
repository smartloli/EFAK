#!/usr/bin/env bash
################################################################################
# EFAK-AI distributed one-click deploy
#
# Starts MySQL + Redis + Nginx + Web (UI) + Worker (collector).
# Web replicas share Redis sessions. Workers join the consistent-hash ring
# using unique container IPs (do not set EFAK_NODE_ID when scaling).
#
# Usage:
#   ./deploy-distributed.sh                 # 1 web + 2 workers
#   ./deploy-distributed.sh --web 2 --workers 3
#   ./deploy-distributed.sh status|logs|down|restart
################################################################################

set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

VERSION="5.0.0"
PROJECT_NAME="efak-dist"
COMPOSE_FILE="docker-compose.distributed.yml"
WEB_REPLICAS=1
WORKER_REPLICAS=2
WEB_PORT="${WEB_PORT:-8080}"
BUILD_FLAG="--build"
ACTION="up"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${SCRIPT_DIR}"

usage() {
    cat <<EOF
EFAK-AI distributed deploy (${VERSION})

Usage:
  $0 [command] [options]

Commands:
  up         Build (optional) and start the stack (default)
  down       Stop and remove containers (keeps MySQL/Redis volumes)
  restart    Restart the stack
  status     Show container and health status
  logs       Follow logs (all services)
  scale      Only re-apply replica counts

Options:
  --web N        Web replica count (default: ${WEB_REPLICAS})
  --workers N    Worker replica count (default: ${WORKER_REPLICAS})
  --port N       Host port for the UI (default: ${WEB_PORT})
  --no-build     Skip image rebuild on up
  -h, --help     Show this help

Examples:
  $0
  $0 --web 2 --workers 3
  $0 logs
  $0 down
EOF
}

compose() {
    docker compose -p "${PROJECT_NAME}" -f "${COMPOSE_FILE}" "$@"
}

require_docker() {
    if ! command -v docker >/dev/null 2>&1; then
        echo -e "${RED}Docker is not installed.${NC}"
        exit 1
    fi
    if ! docker compose version >/dev/null 2>&1; then
        echo -e "${RED}Docker Compose v2 is required (docker compose).${NC}"
        exit 1
    fi
}

wait_healthy() {
    local url="http://127.0.0.1:${WEB_PORT}/health/check"
    echo -e "${YELLOW}Waiting for UI health check on ${url} ...${NC}"
    local i
    for i in $(seq 1 60); do
        if curl -fsS "${url}" >/dev/null 2>&1; then
            echo -e "${GREEN}Stack is healthy.${NC}"
            return 0
        fi
        sleep 3
    done
    echo -e "${YELLOW}Timed out waiting for health check. Check logs with: $0 logs${NC}"
    return 1
}

print_banner() {
    echo -e "${BLUE}"
    cat << 'LOGO'
 _____ _____ _    _  __       _    ___
| ____|  ___/ \  | |/ /      / \  |_ _|
|  _| | |_ / _ \ | ' /_____ / _ \  | |
| |___|  _/ ___ \| . \_____/ ___ \ | |
|_____|_|/_/   \_\_|\_\   /_/   \_\___|
LOGO
    echo -e "${NC}"
    echo -e "${GREEN}EFAK-AI distributed deploy${NC}"
    echo ""
}

print_success() {
    echo ""
    echo -e "${GREEN}================================${NC}"
    echo -e "${GREEN}Distributed stack is up${NC}"
    echo -e "${GREEN}================================${NC}"
    echo ""
    echo "  UI:            http://127.0.0.1:${WEB_PORT}"
    echo "  Health:        http://127.0.0.1:${WEB_PORT}/health/check"
    echo "  Login:         admin / admin123"
    echo "  Web replicas:  ${WEB_REPLICAS}   (role=web, Redis session)"
    echo "  Workers:       ${WORKER_REPLICAS}   (role=worker, consistent-hash shards)"
    echo ""
    echo "Commands:"
    echo "  $0 status"
    echo "  $0 logs"
    echo "  $0 --web 2 --workers 3 scale"
    echo "  $0 down"
    echo ""
}

while [[ $# -gt 0 ]]; do
    case "$1" in
        up|down|restart|status|logs|scale)
            ACTION="$1"
            shift
            ;;
        --web)
            WEB_REPLICAS="$2"
            shift 2
            ;;
        --workers)
            WORKER_REPLICAS="$2"
            shift 2
            ;;
        --port)
            WEB_PORT="$2"
            shift 2
            ;;
        --no-build)
            BUILD_FLAG=""
            shift
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            echo -e "${RED}Unknown argument: $1${NC}"
            usage
            exit 1
            ;;
    esac
done

if ! [[ "${WEB_REPLICAS}" =~ ^[1-9][0-9]*$ ]]; then
    echo -e "${RED}--web must be a positive integer${NC}"
    exit 1
fi
if ! [[ "${WORKER_REPLICAS}" =~ ^[1-9][0-9]*$ ]]; then
    echo -e "${RED}--workers must be a positive integer${NC}"
    exit 1
fi

if [[ ! -f "${COMPOSE_FILE}" ]]; then
    echo -e "${RED}Missing ${COMPOSE_FILE}${NC}"
    exit 1
fi

export WEB_PORT
mkdir -p logs/web logs/worker

require_docker
print_banner

case "${ACTION}" in
    up)
        echo -e "${YELLOW}Starting ${WEB_REPLICAS} web + ${WORKER_REPLICAS} worker ...${NC}"
        compose up -d ${BUILD_FLAG} --scale "efak-web=${WEB_REPLICAS}" --scale "efak-worker=${WORKER_REPLICAS}"
        wait_healthy || true
        print_success
        compose ps
        ;;
    scale)
        echo -e "${YELLOW}Scaling to ${WEB_REPLICAS} web + ${WORKER_REPLICAS} worker ...${NC}"
        compose up -d --no-recreate --scale "efak-web=${WEB_REPLICAS}" --scale "efak-worker=${WORKER_REPLICAS}"
        compose ps
        ;;
    restart)
        compose restart
        compose ps
        ;;
    down)
        compose down
        echo -e "${GREEN}Stopped. MySQL/Redis volumes were kept.${NC}"
        echo "Remove data with: docker compose -p ${PROJECT_NAME} -f ${COMPOSE_FILE} down -v"
        ;;
    status)
        compose ps
        echo ""
        echo "Health:"
        curl -fsS "http://127.0.0.1:${WEB_PORT}/health/check" || echo "(UI not reachable)"
        echo ""
        ;;
    logs)
        compose logs -f --tail=200
        ;;
esac
