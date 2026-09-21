#!/bin/bash
echo "Script started"

# =========================
# CONFIGURATION
# =========================

SLOT_ID=8

URL="http://localhost:8080/api/appointments"

TOKEN_A="eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjaGFybGllMTIzQGdtYWlsLmNvbSIsInJvbGUiOiJQQVRJRU5UIiwiaWF0IjoxNzg5OTY2ODY0LCJleHAiOjE3OTAwNTMyNjR9.oXEEg_2e0c21xG1BHs7Tz3mXMoU3lNf9NcIcf1Vvt1M"

TOKEN_B="eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJib2IxMjM0QGdtYWlsLmNvbSIsInJvbGUiOiJQQVRJRU5UIiwiaWF0IjoxNzg5OTY2OTA5LCJleHAiOjE3OTAwNTMzMDl9.AxjCLDkM2rF1AnpcBvkpw13BymXgLMQ34wIPIAWJaMw"

# =========================
# TEST EXECUTION
# =========================

echo "Starting concurrency test..."
echo "Slot ID: $SLOT_ID"
echo ""

curl -s -o result_a.json -w "Patient A -> HTTP %{http_code}\n" \
  -X POST "$URL" \
  -H "Authorization: Bearer $TOKEN_A" \
  -H "Content-Type: application/json" \
  -d "{\"slotId\": $SLOT_ID, \"reasonForVisit\": \"Patient A visit\"}" &

curl -s -o result_b.json -w "Patient B -> HTTP %{http_code}\n" \
  -X POST "$URL" \
  -H "Authorization: Bearer $TOKEN_B" \
  -H "Content-Type: application/json" \
  -d "{\"slotId\": $SLOT_ID, \"reasonForVisit\": \"Patient B visit\"}" &

wait

echo ""
echo "========== RESULTS =========="

echo "--- Result A ---"
cat result_a.json
echo ""

echo "--- Result B ---"
cat result_b.json
echo ""

echo "============================="