package com.k.medtour.infra.translation;

import java.util.Map;

/**
 * 번역 서비스 인터페이스.
 * MVP 이후 Google Translate, DeepL 등 외부 API 연동 구현체로 교체 예정.
 */
public interface TranslationService {

    /**
     * 텍스트를 원본 언어에서 대상 언어로 번역한다.
     *
     * @param text     원본 텍스트
     * @param fromLang 원본 언어 코드 (e.g., "en", "ko")
     * @param toLang   대상 언어 코드
     * @return 언어 코드 -> 번역 결과 Map
     */
    Map<String, String> translate(String text, String fromLang, String toLang);
}
