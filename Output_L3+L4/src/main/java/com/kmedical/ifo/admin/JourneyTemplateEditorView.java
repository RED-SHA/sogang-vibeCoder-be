package com.kmedical.ifo.admin;

import com.kmedical.control.TemplateController;
import com.kmedical.dto.template.ItineraryTemplateDTO;
import com.kmedical.dto.template.TemplateItemDTO;

import java.util.List;

/**
 * IFO-A06 — JourneyTemplateEditorView
 * UC: UC-A05 (여정 템플릿 관리)
 * 책임: 관리자가 일정 템플릿을 생성·수정·삭제하는 UI 진입점.
 */
public class JourneyTemplateEditorView {

    private final TemplateController templateController;

    public JourneyTemplateEditorView(TemplateController templateController) {
        this.templateController = templateController;
    }

    /**
     * 관리자가 템플릿 목록을 조회한다.
     * Actor Action: Admin views list of itinerary templates.
     */
    public List<ItineraryTemplateDTO> viewTemplates(String agencyId) {
        return templateController.getTemplatesByAgency(agencyId);
    }

    /**
     * 관리자가 새 템플릿을 생성한다.
     * Actor Action: Admin creates a new itinerary template.
     */
    public ItineraryTemplateDTO createTemplate(ItineraryTemplateDTO dto) {
        return templateController.createTemplate(dto);
    }

    /**
     * 관리자가 템플릿 항목을 추가한다.
     * Actor Action: Admin adds an item to a template. (templateId must be set in dto.itineraryTemplateId)
     */
    public TemplateItemDTO addTemplateItem(TemplateItemDTO itemDTO) {
        return templateController.addTemplateItem(itemDTO);
    }

    /**
     * 관리자가 특정 템플릿의 항목 목록을 조회한다.
     * Actor Action: Admin views items of a template.
     */
    public List<TemplateItemDTO> viewTemplateItems(String templateId) {
        return templateController.getTemplateItems(templateId);
    }

    /**
     * 관리자가 템플릿을 수정한다.
     * Actor Action: Admin updates an existing template.
     */
    public ItineraryTemplateDTO updateTemplate(ItineraryTemplateDTO dto) {
        return templateController.updateTemplate(dto);
    }
}
