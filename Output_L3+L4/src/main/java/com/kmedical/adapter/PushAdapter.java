package com.kmedical.adapter;

/**
 * INF-A02 — PushAdapter
 * E06 NotificationService(FCM 등) 연동 인터페이스.
 * SRV-C13 AlertController, SRV-C10 StaffAssignmentController,
 * SRV-C11 WorkController, SRV-C14 SOSController가 호출한다.
 */
public interface PushAdapter {

    /**
     * 지정 사용자에게 Push 알림을 발송한다.
     *
     * @param recipientUserId 수신 사용자 ID
     * @param title           알림 제목
     * @param body            알림 본문
     * @return 발송 성공 여부
     */
    boolean sendPush(String recipientUserId, String title, String body);

    /**
     * 여러 사용자에게 일괄 Push 알림을 발송한다.
     *
     * @param recipientUserIds 수신 사용자 ID 목록
     * @param title            알림 제목
     * @param body             알림 본문
     */
    boolean sendBulkPush(java.util.List<String> recipientUserIds, String title, String body);
}
