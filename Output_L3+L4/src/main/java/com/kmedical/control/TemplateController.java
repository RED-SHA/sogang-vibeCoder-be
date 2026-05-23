package com.kmedical.control;

import com.kmedical.domain.entity.ItineraryTemplate;
import com.kmedical.domain.entity.TemplateItem;
import com.kmedical.dto.template.ItineraryTemplateDTO;
import com.kmedical.dto.template.TemplateItemDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * SRV-C07 — TemplateController
 * 책임: 여정 템플릿·항목 CRUD.
 * UC: UC-A05
 */
public class TemplateController {

    private final Map<String, ItineraryTemplate> templateStore = new HashMap<>();
    private final Map<String, List<TemplateItem>> itemStore = new HashMap<>();

    private void guardNotClosedDown() {
        if (SystemStateRegistry.getInstance().isClosedDown()) {
            throw new IllegalStateException("System is closed down. Customer operations are not permitted.");
        }
    }

    /**
     * 여정 템플릿을 생성한다.
     * System Response: 입력 검증 → ItineraryTemplate 저장 → DTO 반환
     */
    public ItineraryTemplateDTO createTemplate(ItineraryTemplateDTO dto) {
        guardNotClosedDown();
        if (dto == null || dto.getAgencyId() == null) {
            throw new IllegalArgumentException("Template data is incomplete.");
        }

        ItineraryTemplate template = new ItineraryTemplate();
        template.setItineraryTemplateId(UUID.randomUUID().toString());
        template.setAgencyId(dto.getAgencyId());
        template.setProductType(dto.getProductType());
        template.setDurationDays(dto.getDurationDays());
        template.setCreatedBy(dto.getCreatedBy());
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());

        templateStore.put(template.getItineraryTemplateId(), template);
        itemStore.put(template.getItineraryTemplateId(), new ArrayList<>());
        return toTemplateDTO(template);
    }

    /**
     * 여정 템플릿을 수정한다.
     */
    public ItineraryTemplateDTO updateTemplate(ItineraryTemplateDTO dto) {
        guardNotClosedDown();
        if (dto == null || dto.getItineraryTemplateId() == null) {
            throw new IllegalArgumentException("Template update data is incomplete.");
        }

        ItineraryTemplate template = findTemplate(dto.getItineraryTemplateId());
        template.setProductType(dto.getProductType());
        template.setDurationDays(dto.getDurationDays());
        template.setUpdatedAt(LocalDateTime.now());
        return toTemplateDTO(template);
    }

    /**
     * 여정 템플릿을 조회한다.
     */
    public ItineraryTemplateDTO getTemplate(String templateId) {
        guardNotClosedDown();
        return toTemplateDTO(findTemplate(templateId));
    }

    /**
     * 에이전시의 여정 템플릿 목록을 조회한다.
     */
    public List<ItineraryTemplateDTO> getTemplatesByAgency(String agencyId) {
        guardNotClosedDown();
        List<ItineraryTemplateDTO> result = new ArrayList<>();
        for (ItineraryTemplate t : templateStore.values()) {
            if (agencyId.equals(t.getAgencyId())) result.add(toTemplateDTO(t));
        }
        return result;
    }

    /**
     * 템플릿 항목을 추가한다.
     * System Response: 템플릿 존재 확인 → TemplateItem 저장
     */
    public TemplateItemDTO addTemplateItem(TemplateItemDTO dto) {
        guardNotClosedDown();
        if (dto == null || dto.getItineraryTemplateId() == null) {
            throw new IllegalArgumentException("TemplateItem data is incomplete.");
        }

        findTemplate(dto.getItineraryTemplateId());

        TemplateItem item = new TemplateItem();
        item.setTemplateItemId(UUID.randomUUID().toString());
        item.setItineraryTemplateId(dto.getItineraryTemplateId());
        item.setItemType(dto.getItemType());
        item.setTitle(dto.getTitle());
        item.setDurationMinutes(dto.getDurationMinutes());
        item.setAssignedStaffRole(dto.getAssignedStaffRole());
        item.setSortOrder(dto.getSortOrder());

        itemStore.get(dto.getItineraryTemplateId()).add(item);
        return toItemDTO(item);
    }

    /**
     * 템플릿 항목 목록을 조회한다.
     */
    public List<TemplateItemDTO> getTemplateItems(String templateId) {
        guardNotClosedDown();
        findTemplate(templateId);
        List<TemplateItemDTO> result = new ArrayList<>();
        for (TemplateItem i : itemStore.getOrDefault(templateId, new ArrayList<>())) {
            result.add(toItemDTO(i));
        }
        return result;
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private ItineraryTemplate findTemplate(String id) {
        ItineraryTemplate t = templateStore.get(id);
        if (t == null) throw new IllegalArgumentException("ItineraryTemplate not found: " + id);
        return t;
    }

    private ItineraryTemplateDTO toTemplateDTO(ItineraryTemplate t) {
        ItineraryTemplateDTO dto = new ItineraryTemplateDTO();
        dto.setItineraryTemplateId(t.getItineraryTemplateId());
        dto.setAgencyId(t.getAgencyId());
        dto.setProductType(t.getProductType());
        dto.setDurationDays(t.getDurationDays());
        dto.setCreatedBy(t.getCreatedBy());
        dto.setCreatedAt(t.getCreatedAt());
        dto.setUpdatedAt(t.getUpdatedAt());
        return dto;
    }

    private TemplateItemDTO toItemDTO(TemplateItem i) {
        TemplateItemDTO dto = new TemplateItemDTO();
        dto.setTemplateItemId(i.getTemplateItemId());
        dto.setItineraryTemplateId(i.getItineraryTemplateId());
        dto.setItemType(i.getItemType());
        dto.setTitle(i.getTitle());
        dto.setDurationMinutes(i.getDurationMinutes());
        dto.setAssignedStaffRole(i.getAssignedStaffRole());
        dto.setSortOrder(i.getSortOrder());
        return dto;
    }
}
