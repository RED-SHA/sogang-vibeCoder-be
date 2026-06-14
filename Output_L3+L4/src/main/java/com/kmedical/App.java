package com.kmedical;

import com.kmedical.adapter.*;
import com.kmedical.control.*;
import com.kmedical.domain.enums.OAuthProviderType;
import com.kmedical.dto.passport.PassportInfoDTO;
import com.kmedical.http.*;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

/**
 * K-의료관광솔루션 서버 진입점.
 * JDK 내장 HttpServer(com.sun.net.httpserver)로 localhost:8080 서비스.
 */
public class App {

    private static final int PORT = 8080;

    public static void main(String[] args) throws Exception {

        // ── 1. 어댑터 스텁 (실제 배포 시 구현체로 교체) ──────────────────────
        OAuthAdapter oAuthAdapter = new OAuthAdapter() {
            public String verifyAndGetSubjectId(OAuthProviderType p, String code, String uri) {
                return "subj-" + code;
            }
            public String fetchEmail(OAuthProviderType p, String subjectId) {
                return subjectId + "@example.com";
            }
        };

        PushAdapter pushAdapter = new PushAdapter() {
            public boolean sendPush(String uid, String title, String body) {
                System.out.printf("[PUSH] → %s | %s%n", uid, title);
                return true;
            }
            public boolean sendBulkPush(List<String> uids, String title, String body) {
                System.out.printf("[PUSH-BULK] → %d명 | %s%n", uids.size(), title);
                return true;
            }
        };

        RealtimeSyncAdapter realtimeSyncAdapter = snapshot -> {
            System.out.printf("[REALTIME] scheduleItem=%s version=%s%n",
                    snapshot.getScheduleItemId(), snapshot.getVersion());
            return true;
        };

        MessengerAdapter messengerAdapter = new MessengerAdapter() {
            public boolean sendWhatsApp(String phone, String msg) {
                System.out.printf("[WHATSAPP] → %s%n", phone);
                return true;
            }
            public boolean sendEmail(String email, String subject, String body) {
                System.out.printf("[EMAIL] → %s | %s%n", email, subject);
                return true;
            }
        };

        TranslationAdapter translationAdapter = (text, src, tgt) -> "[번역됨] " + text;

        OCRAdapter ocrAdapter = url -> {
            PassportInfoDTO info = new PassportInfoDTO();
            info.setOcrFullNameEn("HONG GILDONG");
            info.setOcrPassportNumber("M12345678");
            info.setOcrNationality("KOR");
            info.setOcrExpiryDate(LocalDate.of(2030, 12, 31));
            return info;
        };

        // ── 2. 컨트롤러 조립 (생성자 기반 의존성 주입) ─────────────────────────
        // C01 Auth
        AuthController authController = new AuthController(oAuthAdapter);

        // C02 AccessLink
        AccessLinkController accessLinkController = new AccessLinkController();

        // C03 Passport
        PassportController passportController = new PassportController(ocrAdapter);

        // C04 Patient
        PatientController patientController = new PatientController();

        // C05 Agency
        AgencyController agencyController = new AgencyController();

        // C06 Quotation
        QuotationController quotationController = new QuotationController();

        // C07 Template
        TemplateController templateController = new TemplateController();

        // C08 Journey  (C01 alertController is used internally; alertController built first)
        AlertController alertController = new AlertController(pushAdapter, messengerAdapter);

        // C09 Staff
        StaffController staffController = new StaffController();

        // C10 StaffAssignment
        StaffAssignmentController staffAssignmentController = new StaffAssignmentController(pushAdapter);

        JourneyController journeyController = new JourneyController(
                alertController, staffAssignmentController, realtimeSyncAdapter, pushAdapter);

        // C11 Work
        WorkController workController = new WorkController(pushAdapter, alertController);

        // C12 Chat
        ChatController chatController = new ChatController(translationAdapter);

        // C13 Alert already built above

        // C14 SOS
        SOSController sosController = new SOSController(alertController);

        // C15 Invoice
        InvoiceController invoiceController = new InvoiceController(accessLinkController, workController, alertController);

        // C16 Guide
        GuideController guideController = new GuideController(alertController);

        // C17 Report
        ReportController reportController = new ReportController();

        // C18 RBAC
        RBACController rbacController = new RBACController();

        // C19 Dashboard
        DashboardController dashboardController = new DashboardController(
                journeyController, staffController, alertController, quotationController, sosController);

        // ── 3. HttpServer 구성 ──────────────────────────────────────────────────
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/api/health",       new HealthHandler());
        server.createContext("/api/auth",          new AuthHandler(authController));
        server.createContext("/api/access-links",  new AccessLinkHandler(accessLinkController));
        server.createContext("/api/passports",     new PassportHandler(passportController));
        server.createContext("/api/patients",      new PatientHandler(patientController));
        server.createContext("/api/agencies",      new AgencyHandler(agencyController));
        server.createContext("/api/quotations",    new QuotationHandler(quotationController));
        server.createContext("/api/templates",     new TemplateHandler(templateController));
        server.createContext("/api/journeys",      new JourneyHandler(journeyController));
        server.createContext("/api/staff",         new StaffHandler(staffController));
        server.createContext("/api/assignments",   new StaffAssignmentHandler(staffAssignmentController));
        server.createContext("/api/work",          new WorkHandler(workController));
        server.createContext("/api/chat",          new ChatHandler(chatController));
        server.createContext("/api/alerts",        new AlertHandler(alertController));
        server.createContext("/api/sos",           new SOSHandler(sosController));
        server.createContext("/api/invoices",      new InvoiceHandler(invoiceController));
        server.createContext("/api/guides",        new GuideHandler(guideController));
        server.createContext("/api/reports",       new ReportHandler(reportController));
        server.createContext("/api/rbac",          new RBACHandler(rbacController));
        server.createContext("/api/dashboard",     new DashboardHandler(dashboardController));

        server.setExecutor(Executors.newFixedThreadPool(8));

        // ── 4. 종료 훅 ──────────────────────────────────────────────────────────
        CountDownLatch latch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[서버] 종료 신호 수신 — 정리 중...");
            server.stop(1);
            latch.countDown();
            System.out.println("[서버] 종료 완료");
        }));

        // ── 5. 기동 ─────────────────────────────────────────────────────────────
        server.start();
        System.out.println("================================================");
        System.out.println("  K-의료관광솔루션 서버 기동");
        System.out.println("  http://localhost:" + PORT);
        System.out.println("================================================");
        System.out.println("  GET  /api/health");
        System.out.println("  POST /api/auth/login              로그인");
        System.out.println("  POST /api/auth/startup            시스템 가동");
        System.out.println("  POST /api/auth/closedown          시스템 종료");
        System.out.println("  POST /api/access-links            접속 링크 생성");
        System.out.println("  POST /api/passports/upload        여권 OCR");
        System.out.println("  GET  /api/patients/{id}           환자 조회");
        System.out.println("  POST /api/quotations/requests     견적 요청");
        System.out.println("  POST /api/quotations/issue        견적 발송");
        System.out.println("  POST /api/journeys                여정 생성");
        System.out.println("  POST /api/assignments             스태프 배정");
        System.out.println("  POST /api/work/status             업무 상태 변경");
        System.out.println("  POST /api/chat/conversations      채팅방 생성");
        System.out.println("  POST /api/sos                     긴급 호출");
        System.out.println("  POST /api/invoices                인보이스 생성");
        System.out.println("  POST /api/invoices/{id}/issue     인보이스 발행");
        System.out.println("  GET  /api/dashboard?agencyId={id} 대시보드");
        System.out.println("================================================");
        System.out.println("  종료: Ctrl+C");
        System.out.println("================================================");

        latch.await(); // Ctrl+C 전까지 대기
    }
}
