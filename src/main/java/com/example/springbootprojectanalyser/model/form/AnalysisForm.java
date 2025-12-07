package com.example.springbootprojectanalyser.model.form;

import jakarta.validation.constraints.NotBlank;

/**
 * 解析実行フォーム
 */
public class AnalysisForm {
    
    @NotBlank(message = "解析対象プロジェクトのパスを入力してください")
    private String targetProjectPath;

    public AnalysisForm() {
    }

    public AnalysisForm(String targetProjectPath) {
        this.targetProjectPath = targetProjectPath;
    }

    public String getTargetProjectPath() {
        return targetProjectPath;
    }

    public void setTargetProjectPath(String targetProjectPath) {
        this.targetProjectPath = targetProjectPath;
    }
}

