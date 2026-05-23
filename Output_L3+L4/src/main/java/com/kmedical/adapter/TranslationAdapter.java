package com.kmedical.adapter;

import com.kmedical.domain.enums.Language;

/**
 * INF-A04 — TranslationAdapter
 * E08 TranslationService 연동 인터페이스.
 * SRV-C12 ChatController가 호출한다.
 */
public interface TranslationAdapter {

    /**
     * 텍스트를 대상 언어로 번역한다.
     *
     * @param originalText 원문
     * @param sourceLang   원문 언어
     * @param targetLang   번역 대상 언어
     * @return 번역된 텍스트
     * @throws RuntimeException 번역 서비스 오류 시
     */
    String translate(String originalText, Language sourceLang, Language targetLang);
}
