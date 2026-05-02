#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${1:-https://tripwawe.ru}"
LOCAL_FRONT="${2:-http://127.0.0.1:8888}"

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

pass() { echo -e "${GREEN}OK${NC}  $1"; }
warn() { echo -e "${YELLOW}WARN${NC} $1"; }
fail() { echo -e "${RED}FAIL${NC} $1"; exit 1; }

check_status() {
  local name="$1"
  local url="$2"
  local expected="${3:-200}"
  local status
  status="$(curl -s -o /dev/null -w "%{http_code}" "$url" || true)"
  if [[ "$status" == "$expected" ]]; then
    pass "$name -> $status"
  else
    fail "$name -> got $status, expected $expected ($url)"
  fi
}

check_post_json() {
  local name="$1"
  local url="$2"
  local json="$3"
  local expected="$4"
  local status
  status="$(curl -s -o /tmp/smoke-body.json -w "%{http_code}" \
    -X POST "$url" \
    -H "Content-Type: application/json" \
    -d "$json" || true)"
  if [[ "$status" == "$expected" ]]; then
    pass "$name -> $status"
  else
    echo "Response body:"
    cat /tmp/smoke-body.json || true
    fail "$name -> got $status, expected $expected ($url)"
  fi
}

echo "== TripWave prod smoke check =="
echo "BASE_URL=$BASE_URL"
echo "LOCAL_FRONT=$LOCAL_FRONT"

check_status "Public site" "$BASE_URL/" 200
check_status "Public tours search" "$BASE_URL/api/tours/search" 200
check_post_json \
  "Public OTP request (login)" \
  "$BASE_URL/api/auth/login/request" \
  '{"email":"manager@agency.local","password":"Manager123!Secure"}' \
  200

check_status "Local frontend proxy tours search" "$LOCAL_FRONT/api/tours/search" 200
check_post_json \
  "Local frontend proxy OTP request" \
  "$LOCAL_FRONT/api/auth/login/request" \
  '{"email":"manager@agency.local","password":"Manager123!Secure"}' \
  200

if command -v docker >/dev/null 2>&1; then
  if docker compose ps backend >/dev/null 2>&1; then
    health="$(docker compose ps backend --format json | jq -r '.[0].Health // "unknown"' 2>/dev/null || true)"
    if [[ -n "$health" ]]; then
      warn "docker backend health: $health"
    fi
  fi
fi

pass "All checks completed"
