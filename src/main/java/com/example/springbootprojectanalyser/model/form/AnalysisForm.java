package com.example.springbootprojectanalyser.model.form;

import com.example.springbootprojectanalyser.model.dto.AnalysisRequestDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 解析入力フォーム
 */
public record AnalysisForm(
    @NotBlank(message = "解析対象プロジェクトのパスを入力してください")
    @Pattern(regexp = "^[A-Za-z]:\\\\.*|/.*", message = "有効なパスを入力してください")
    String targetProjectPath,

    String targetPackagePattern
) {
    public AnalysisForm {
        // デフォルト値の設定
        if (targetPackagePattern == null || targetPackagePattern.isBlank()) {
            targetPackagePattern = "**";
        }
    }

    /**
     * DTOへの変換メソッド
     *
     * @return 解析リクエストDTO
     */
    public AnalysisRequestDto toDto() {
        return new AnalysisRequestDto(targetProjectPath, targetPackagePattern);
    }
}

