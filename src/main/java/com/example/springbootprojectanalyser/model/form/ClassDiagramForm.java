package com.example.springbootprojectanalyser.model.form;

import jakarta.validation.constraints.NotBlank;

/**
 * クラス図作成フォーム
 */
public record ClassDiagramForm(
    @NotBlank(message = "対象プロジェクトパスは必須です")
    String targetProjectPath,
    
    String selectedEndpointId
) {
    public ClassDiagramForm {
        if (targetProjectPath != null) {
            targetProjectPath = targetProjectPath.trim();
        }
    }
    
    public static ClassDiagramForm empty() {
        return new ClassDiagramForm("", "");
    }
}

