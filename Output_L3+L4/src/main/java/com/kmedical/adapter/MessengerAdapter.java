package com.kmedical.adapter;

/**
 * INF-A03 — MessengerAdapter
 * E07 MessengerService(WhatsApp/Email) 연동 인터페이스.
 * SRV-C13 AlertController가 호출한다.
 */
public interface MessengerAdapter {

    /**
     * WhatsApp 메시지를 발송한다.
     *
     * @param phoneE164 E.164 형식 국제 전화번호
     * @param message   발송 메시지
     * @return 발송 성공 여부
     */
    boolean sendWhatsApp(String phoneE164, String message);

    /**
     * 이메일을 발송한다.
     *
     * @param toEmail 수신 이메일 주소
     * @param subject 제목
     * @param body    본문
     * @return 발송 성공 여부
     */
    boolean sendEmail(String toEmail, String subject, String body);
}
