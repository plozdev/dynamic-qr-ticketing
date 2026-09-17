import urllib.request
import urllib.error
import json
import time
import sys
import uuid
from datetime import datetime, timezone, timedelta

sys.stdout.reconfigure(encoding='utf-8')

BASE_URL = "http://localhost:8080"

class TestRunner:
    def __init__(self):
        self.passed = 0
        self.failed = 0
        self.results = []

    def log_result(self, tc_id, name, success, details=""):
        status = "PASSED" if success else "FAILED"
        if success:
            self.passed += 1
            icon = "✅"
        else:
            self.failed += 1
            icon = "❌"
        self.results.append((tc_id, name, status, details))
        print(f"{icon} [{tc_id}] {name}: {status} {details}")

    def request(self, method, path, data=None, headers=None):
        url = f"{BASE_URL}{path}"
        req_headers = {"Accept": "application/json"}
        if headers:
            req_headers.update(headers)
        
        body = None
        if data is not None:
            req_headers["Content-Type"] = "application/json; charset=utf-8"
            body = json.dumps(data).encode("utf-8")

        req = urllib.request.Request(url, data=body, headers=req_headers, method=method)
        start_time = time.time()
        try:
            with urllib.request.urlopen(req) as resp:
                elapsed_ms = int((time.time() - start_time) * 1000)
                res_body = resp.read().decode("utf-8")
                json_data = json.loads(res_body) if res_body else None
                return resp.getcode(), json_data, elapsed_ms
        except urllib.error.HTTPError as e:
            elapsed_ms = int((time.time() - start_time) * 1000)
            err_body = e.read().decode("utf-8")
            try:
                json_data = json.loads(err_body)
            except Exception:
                json_data = err_body
            return e.code, json_data, elapsed_ms

def run_all_tests():
    print("=" * 80)
    print("🚀 BẮT ĐẦU CHẠY BỘ TEST TỰ ĐỘNG TOÀN DIỆN (18/18 TEST CASES)")
    print(f"Target Server: {BASE_URL}")
    print("=" * 80)

    runner = TestRunner()

    shared_state = {}

    # -------------------------------------------------------------
    # NHÓM 1: HAPPY PATH FLOW
    # -------------------------------------------------------------
    print("\n--- NHÓM 1: HAPPY PATH FLOW ---")

    # TC01: Create Event
    now = datetime.now(timezone.utc)
    start_dt = (now + timedelta(days=30)).strftime("%Y-%m-%dT%H:%M:%SZ")
    end_dt = (now + timedelta(days=30, hours=4)).strftime("%Y-%m-%dT%H:%M:%SZ")
    
    event_payload = {
        "name": "Đại nhạc hội Symphony of Stars 2026",
        "description": "Đại nhạc hội âm nhạc quốc tế quy mô 40.000 khán giả",
        "venueName": "Sân vận động Quốc gia Mỹ Đình",
        "venueAddress": "Số 1 Lê Đức Thọ, Nam Từ Liêm, Hà Nội",
        "venueGates": ["GATE_A_VIP", "GATE_B_STANDARD"],
        "startDateTime": start_dt,
        "endDateTime": end_dt
    }
    code, res, ms = runner.request("POST", "/api/v1/events", event_payload)
    if code == 201 and res and res.get("status") == "PUBLISHED":
        shared_state["eventId"] = res["id"]
        runner.log_result("TC01", "Create Event (POST /api/v1/events)", True, f"({ms}ms, eventId={res['id'][:8]}...)")
    else:
        runner.log_result("TC01", "Create Event (POST /api/v1/events)", False, f"Code {code}: {res}")

    # TC02: Get Event By Id
    event_id = shared_state.get("eventId")
    if event_id:
        code, res, ms = runner.request("GET", f"/api/v1/events/{event_id}")
        if code == 200 and res and res.get("id") == event_id:
            runner.log_result("TC02", "Get Event By ID (GET /api/v1/events/{id})", True, f"({ms}ms)")
        else:
            runner.log_result("TC02", "Get Event By ID (GET /api/v1/events/{id})", False, f"Code {code}")
    else:
        runner.log_result("TC02", "Get Event By ID", False, "Skipped: eventId not available")

    # TC03: Issue Ticket
    user_id = str(uuid.uuid4())
    shared_state["userId"] = user_id
    ticket_payload = {
        "eventId": event_id,
        "userId": user_id,
        "categoryName": "VIP_ZONE_A",
        "attendeeEmail": "khanhgia_vip@gmail.com"
    }
    code, res, ms = runner.request("POST", "/api/v1/tickets/issue", ticket_payload)
    if code == 201 and res and "ticketId" in res:
        ticket_id = res["ticketId"]
        shared_state["ticketId"] = ticket_id
        runner.log_result("TC03", "Issue Ticket (POST /api/v1/tickets/issue)", True, f"({ms}ms, ticketId={ticket_id[:8]}...)")
    else:
        runner.log_result("TC03", "Issue Ticket", False, f"Code {code}: {res}")

    # TC04: Mobile Key Provisioning Sync
    ticket_id = shared_state.get("ticketId")
    if ticket_id:
        code, res, ms = runner.request("GET", f"/api/v1/tickets/{ticket_id}/sync", headers={"X-User-Id": user_id})
        if code == 200 and res and "secretKeyBase64" in res and "serverTimeEpochSeconds" in res:
            shared_state["secretKey"] = res["secretKeyBase64"]
            runner.log_result("TC04", "Mobile Key Sync (GET /api/v1/tickets/{id}/sync)", True, f"({ms}ms)")
        else:
            runner.log_result("TC04", "Mobile Key Sync", False, f"Code {code}: {res}")
    else:
        runner.log_result("TC04", "Mobile Key Sync", False, "Skipped: ticketId not available")

    # TC05: Web Dynamic QR
    if ticket_id:
        code, res, ms = runner.request("GET", f"/api/v1/tickets/{ticket_id}/dynamic-qr", headers={"X-User-Id": user_id})
        if code == 200 and res and "dynamicPayload" in res and res["dynamicPayload"].startswith("TICKETING:"):
            shared_state["dynamicPayload"] = res["dynamicPayload"]
            runner.log_result("TC05", "Web Dynamic QR (GET /api/v1/tickets/{id}/dynamic-qr)", True, f"({ms}ms, payload={res['dynamicPayload'][:30]}...)")
        else:
            runner.log_result("TC05", "Web Dynamic QR", False, f"Code {code}: {res}")
    else:
        runner.log_result("TC05", "Web Dynamic QR", False, "Skipped: ticketId not available")

    # TC06: Gate Validation (First check-in)
    dynamic_payload = shared_state.get("dynamicPayload")
    if dynamic_payload:
        scan_payload = {"rawQrPayload": dynamic_payload}
        code, res, ms = runner.request("POST", "/api/v1/gates/GATE_A_VIP/validate", scan_payload)
        if code == 200 and res and res.get("status") == "GRANTED" and res.get("success") is True:
            runner.log_result("TC06", "Gate Check-in Valid (POST /api/v1/gates/{gateId}/validate)", True, f"({ms}ms, GRANTED)")
        else:
            runner.log_result("TC06", "Gate Check-in Valid", False, f"Code {code}: {res}")
    else:
        runner.log_result("TC06", "Gate Check-in Valid", False, "Skipped: dynamicPayload not available")

    # TC07: Offline Gate Roster Sync
    code, res, ms = runner.request("GET", "/api/v1/gates/GATE_A_VIP/sync-roster")
    if code == 200 and res and "gateId" in res and "roster" in res:
        runner.log_result("TC07", "Offline Gate Roster Sync (GET /api/v1/gates/{gateId}/sync-roster)", True, f"({ms}ms)")
    else:
        runner.log_result("TC07", "Offline Gate Roster Sync", False, f"Code {code}: {res}")

    # TC08: Audit Log Verification
    time.sleep(0.5)
    code, res, ms = runner.request("GET", "/api/v1/audit-logs")
    if code == 200 and isinstance(res, list) and len(res) >= 3:
        event_types = [item["eventType"] for item in res]
        has_published = "EVENT_PUBLISHED" in event_types
        has_issued = "TICKET_ISSUED" in event_types
        has_validated = "TICKET_VALIDATED" in event_types
        if has_published and has_issued and has_validated:
            runner.log_result("TC08", "Audit Log Record Verification (GET /api/v1/audit-logs)", True, f"({ms}ms, found {len(res)} logs)")
        else:
            runner.log_result("TC08", "Audit Log Record Verification", False, f"Missing event types: {event_types}")
    else:
        runner.log_result("TC08", "Audit Log Record Verification", False, f"Code {code}: {res}")

    # -------------------------------------------------------------
    # NHÓM 2: NEGATIVE & VALIDATION CASES
    # -------------------------------------------------------------
    print("\n--- NHÓM 2: NEGATIVE & VALIDATION CASES ---")

    # TC09: Create Event Missing Venue
    invalid_event = {
        "name": "Sự kiện thiếu địa điểm",
        "startDateTime": start_dt,
        "endDateTime": end_dt
    }
    code, res, ms = runner.request("POST", "/api/v1/events", invalid_event)
    if code == 400:
        runner.log_result("TC09", "Create Event Missing Venue (Expect HTTP 400)", True, f"({ms}ms, Bad Request)")
    else:
        runner.log_result("TC09", "Create Event Missing Venue", False, f"Expected 400, got {code}")

    # TC10: Create Event Invalid Date Range (start > end)
    invalid_dates_event = {
        "name": "Sự kiện ngày kết thúc trước ngày bắt đầu",
        "venueName": "Nhà hát lớn",
        "startDateTime": end_dt,
        "endDateTime": start_dt
    }
    code, res, ms = runner.request("POST", "/api/v1/events", invalid_dates_event)
    if code == 400:
        runner.log_result("TC10", "Create Event Start > End Date (Expect HTTP 400)", True, f"({ms}ms, Bad Request)")
    else:
        runner.log_result("TC10", "Create Event Start > End Date", False, f"Expected 400, got {code}")

    # TC11: Get Event Not Found
    random_event_id = str(uuid.uuid4())
    code, res, ms = runner.request("GET", f"/api/v1/events/{random_event_id}")
    if code == 404:
        runner.log_result("TC11", "Get Non-existent Event (Expect HTTP 404)", True, f"({ms}ms, Not Found)")
    else:
        runner.log_result("TC11", "Get Non-existent Event", False, f"Expected 404, got {code}")

    # TC12: Issue Ticket with Non-existent Event
    invalid_ticket_req = {
        "eventId": random_event_id,
        "userId": user_id,
        "categoryName": "VIP",
        "attendeeEmail": "test@test.com"
    }
    code, res, ms = runner.request("POST", "/api/v1/tickets/issue", invalid_ticket_req)
    if code in [400, 404]:
        runner.log_result("TC12", "Issue Ticket for Invalid Event (Expect HTTP 400/404)", True, f"({ms}ms, Code {code})")
    else:
        runner.log_result("TC12", "Issue Ticket for Invalid Event", False, f"Expected 400/404, got {code}")

    # TC13: Sync Ticket with Unauthorized User
    another_user_id = str(uuid.uuid4())
    code, res, ms = runner.request("GET", f"/api/v1/tickets/{ticket_id}/sync", headers={"X-User-Id": another_user_id})
    if code in [400, 403]:
        runner.log_result("TC13", "Sync Ticket Unauthorized User (Expect HTTP 400/403)", True, f"({ms}ms, Forbidden)")
    else:
        runner.log_result("TC13", "Sync Ticket Unauthorized User", False, f"Expected 400/403, got {code}")

    # -------------------------------------------------------------
    # NHÓM 3: SECURITY & FRAUD PREVENTION CASES
    # -------------------------------------------------------------
    print("\n--- NHÓM 3: SECURITY & FRAUD PREVENTION CASES ---")

    # TC14: Anti-Replay Attack (Quét lại mã QR vừa check-in ở TC06)
    code, res, ms = runner.request("POST", "/api/v1/gates/GATE_A_VIP/validate", {"rawQrPayload": dynamic_payload})
    if code == 403 and res and res.get("status") == "DENIED_REPLAY_ATTACK":
        runner.log_result("TC14", "Anti-Replay Attack Prevention (Expect HTTP 403 DENIED_REPLAY_ATTACK)", True, f"({ms}ms, Blocked)")
    else:
        runner.log_result("TC14", "Anti-Replay Attack Prevention", False, f"Expected 403 DENIED_REPLAY_ATTACK, got {code}: {res}")

    # TC15: Already Used Ticket Prevention (Vé đã dùng quét bằng token mới)
    future_time_30s = int(time.time()) + 25
    fresh_token_for_used_ticket = f"TICKETING:{ticket_id}:{future_time_30s}:freshTokenForUsedTicket_{uuid.uuid4().hex[:8]}"
    code2, res2, ms2 = runner.request("POST", "/api/v1/gates/GATE_A_VIP/validate", {"rawQrPayload": fresh_token_for_used_ticket})
    if code2 == 403 and res2 and res2.get("status") == "DENIED_ALREADY_USED":
        runner.log_result("TC15", "Already Used Ticket Prevention (Expect HTTP 403 DENIED_ALREADY_USED)", True, f"({ms2}ms, Blocked)")
    else:
        runner.log_result("TC15", "Already Used Ticket Prevention", False, f"Expected 403 DENIED_ALREADY_USED, got {code2}: {res2}")

    # TC16: Expired QR Code
    expired_time = int(time.time()) - 120
    expired_payload = f"TICKETING:{ticket_id}:{expired_time}:fakeToken12345"
    code, res, ms = runner.request("POST", "/api/v1/gates/GATE_A_VIP/validate", {"rawQrPayload": expired_payload})
    if code == 403 and res and res.get("status") == "DENIED_EXPIRED_QR":
        runner.log_result("TC16", "Expired QR Code Rejection (Expect HTTP 403 DENIED_EXPIRED_QR)", True, f"({ms}ms, Expired)")
    else:
        runner.log_result("TC16", "Expired QR Code Rejection", False, f"Expected 403 DENIED_EXPIRED_QR, got {code}: {res}")

    # TC17: Forged / Tampered HMAC Signature
    future_time = int(time.time()) + 25
    fake_token = "FAKED_UNAUTHENTICATED_HMAC_SIGNATURE_HERE_XYZ"
    code_t2, res_t2, _ = runner.request("POST", "/api/v1/tickets/issue", {
        "eventId": event_id, "userId": user_id, "categoryName": "VIP", "attendeeEmail": "active_ticket@gmail.com"
    })
    ticket2_id = res_t2["ticketId"] if code_t2 == 201 else str(uuid.uuid4())
    tampered_payload = f"TICKETING:{ticket2_id}:{future_time}:{fake_token}"
    code, res, ms = runner.request("POST", "/api/v1/gates/GATE_A_VIP/validate", {"rawQrPayload": tampered_payload})
    if code == 403 and res and res.get("status") == "DENIED_INVALID_SIGNATURE":
        runner.log_result("TC17", "Forged Signature Rejection (Expect HTTP 403 DENIED_INVALID_SIGNATURE)", True, f"({ms}ms, Invalid Signature)")
    else:
        runner.log_result("TC17", "Forged Signature Rejection", False, f"Expected 403 DENIED_INVALID_SIGNATURE, got {code}: {res}")

    # TC18: Audit Log Security Alert Verification
    time.sleep(0.5)
    code, res, ms = runner.request("GET", "/api/v1/audit-logs")
    if code == 200 and isinstance(res, list):
        security_alerts = [item for item in res if item["eventType"] == "SECURITY_ALERT"]
        if len(security_alerts) > 0:
            runner.log_result("TC18", "Security Alert in Audit Log (GET /api/v1/audit-logs)", True, f"({ms}ms, found {len(security_alerts)} security alerts)")
        else:
            runner.log_result("TC18", "Security Alert in Audit Log", False, "No SECURITY_ALERT found in audit logs")
    else:
        runner.log_result("TC18", "Security Alert in Audit Log", False, f"Code {code}: {res}")

    # -------------------------------------------------------------
    # TỔNG KẾT BÁO CÁO
    # -------------------------------------------------------------
    print("\n" + "=" * 80)
    print("📊 BẢNG TỔNG HỢP KẾT QUẢ KIỂM THỬ TOÀN DIỆN (SUMMARY REPORT)")
    print("=" * 80)
    print(f"{'TC ID':<8} | {'Tên Ca Kiểm Thử':<45} | {'Kết Quả':<10} | {'Ghi Chú'}")
    print("-" * 80)
    for tc_id, name, status, details in runner.results:
        color_icon = "PASS" if status == "PASSED" else "FAIL"
        print(f"{tc_id:<8} | {name:<45} | {color_icon:<10} | {details}")
    print("-" * 80)
    print(f"Tổng số ca kiểm thử: {len(runner.results)}")
    print(f"Thành công (Passed): {runner.passed}/{len(runner.results)} ({runner.passed/len(runner.results)*100:.1f}%)")
    print(f"Thất bại  (Failed): {runner.failed}/{len(runner.results)}")
    print("=" * 80)

if __name__ == "__main__":
    run_all_tests()
