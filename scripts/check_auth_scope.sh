#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${1:-http://98.84.8.224}"
TMPDIR="$(mktemp -d)"
COOKIE_FILE="$TMPDIR/cookie.txt"

cleanup() {
  rm -rf "$TMPDIR"
}
trap cleanup EXIT

EMAIL="codex.auth.$(date +%s)@example.com"
PASS="Codex1234!"
PHONE="010-7777-$(date +%H%M)"
STORE_NAME="AuthScope$(date +%H%M%S)"

SIGNUP_PAYLOAD="$(cat <<JSON
{"account":{"email":"$EMAIL","password":"$PASS","userName":"AuthTester","userPhone":"$PHONE"},"store":{"storeName":"$STORE_NAME","storeTel":"02-1234-5678","storeAdd1":"Seoul Road 1","storeAdd2":"101"}}
JSON
)"

echo "[1] signup email=$EMAIL"
SIGNUP_RESP="$(curl -sS -H "Content-Type: application/json" -d "$SIGNUP_PAYLOAD" "$BASE_URL/onboarding/owner-signup")"
echo "$SIGNUP_RESP"

echo "[2] login"
LOGIN_STATUS="$(curl -sS -o "$TMPDIR/login_body.json" -w "%{http_code}" -c "$COOKIE_FILE" -H "Content-Type: application/json" -d "{\"email\":\"$EMAIL\",\"password\":\"$PASS\"}" "$BASE_URL/auth/local/login")"
cat "$TMPDIR/login_body.json"
echo
echo "login_status=$LOGIN_STATUS"
if [[ "$LOGIN_STATUS" != "200" ]]; then
  echo "login failed; cannot continue auth scope check" >&2
  exit 1
fi

echo "[3] session/me"
ME_STATUS="$(curl -sS -o "$TMPDIR/me_body.json" -w "%{http_code}" -b "$COOKIE_FILE" "$BASE_URL/auth/session/me")"
cat "$TMPDIR/me_body.json"
echo
echo "me_status=$ME_STATUS"
if [[ "$ME_STATUS" != "200" ]]; then
  echo "session/me failed; cannot continue auth scope check" >&2
  exit 1
fi

STORE_ID="$(grep -o '"storeId":[0-9]*' "$TMPDIR/me_body.json" | head -1 | cut -d: -f2)"
if [[ -z "$STORE_ID" ]]; then
  echo "failed to parse storeId from /auth/session/me" >&2
  exit 1
fi
OTHER_STORE_ID=$((STORE_ID + 1))

echo "[4] same store items"
SAME_ITEMS_STATUS="$(curl -sS -o "$TMPDIR/same_items.json" -w "%{http_code}" -b "$COOKIE_FILE" "$BASE_URL/stores/$STORE_ID/items")"
echo "same_items_status=$SAME_ITEMS_STATUS"
head -c 200 "$TMPDIR/same_items.json"; echo

echo "[5] other store items"
OTHER_ITEMS_STATUS="$(curl -sS -o "$TMPDIR/other_items.json" -w "%{http_code}" -b "$COOKIE_FILE" "$BASE_URL/stores/$OTHER_STORE_ID/items")"
echo "other_items_status=$OTHER_ITEMS_STATUS"
cat "$TMPDIR/other_items.json"; echo

echo "[6] same store requests"
SAME_REQ_STATUS="$(curl -sS -o "$TMPDIR/same_req.json" -w "%{http_code}" -b "$COOKIE_FILE" "$BASE_URL/stores/$STORE_ID/requests")"
echo "same_requests_status=$SAME_REQ_STATUS"
head -c 200 "$TMPDIR/same_req.json"; echo

echo "[7] other store requests"
OTHER_REQ_STATUS="$(curl -sS -o "$TMPDIR/other_req.json" -w "%{http_code}" -b "$COOKIE_FILE" "$BASE_URL/stores/$OTHER_STORE_ID/requests")"
echo "other_requests_status=$OTHER_REQ_STATUS"
cat "$TMPDIR/other_req.json"; echo

if [[ "$SAME_ITEMS_STATUS" != "200" || "$OTHER_ITEMS_STATUS" != "403" || "$SAME_REQ_STATUS" != "200" || "$OTHER_REQ_STATUS" != "403" ]]; then
  echo "auth scope regression detected" >&2
  exit 1
fi

echo "SUMMARY: PASS (storeId=$STORE_ID, email=$EMAIL)"
