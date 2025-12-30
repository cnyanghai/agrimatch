package com.agrimatch.requirement_template.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RequirementTemplateCreateRequest {
    @NotBlank
    @Size(max = 128)
    private String templateName;

    @NotBlank
    private String templateJson;

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateJson() {
        return templateJson;
    }

    public void setTemplateJson(String templateJson) {
        this.templateJson = templateJson;
    }
}


