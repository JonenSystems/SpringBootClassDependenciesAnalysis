package com.example.springbootprojectanalyser.controller;

import com.example.springbootprojectanalyser.model.dto.AnalysisExecutionDto;
import com.example.springbootprojectanalyser.model.dto.AnalysisResultDto;
import com.example.springbootprojectanalyser.model.form.AnalysisForm;
import com.example.springbootprojectanalyser.repository.DependencyKindRepository;
import com.example.springbootprojectanalyser.service.ClassDependencyAnalysisService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

/**
 * 解析コントローラー
 */
@Controller
public class AnalysisController {

    private final ClassDependencyAnalysisService analysisService;
    private final DependencyKindRepository dependencyKindRepository;

    public AnalysisController(ClassDependencyAnalysisService analysisService, DependencyKindRepository dependencyKindRepository) {
        this.analysisService = analysisService;
        this.dependencyKindRepository = dependencyKindRepository;
    }

    @GetMapping("/")
    public String index(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new AnalysisForm(""));
        }
        // 依存タイプのマップを作成（コード -> 説明）
        Map<String, String> dependencyKindMap = dependencyKindRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(
                        com.example.springbootprojectanalyser.model.entity.DependencyKindEntity::getCode,
                        com.example.springbootprojectanalyser.model.entity.DependencyKindEntity::getDescription));
        model.addAttribute("dependencyKindMap", dependencyKindMap);
        return "analysis/index";
    }

    @PostMapping("/analyze")
    public String analyze(
            @Valid @ModelAttribute("form") AnalysisForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.form", bindingResult);
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/";
        }

        try {
            // デフォルトパッケージパターン（全パッケージ）
            String targetPackagePattern = "**";
            AnalysisExecutionDto executionDto = new AnalysisExecutionDto(
                    form.getTargetProjectPath(),
                    targetPackagePattern
            );
            
            AnalysisResultDto result = analysisService.executeAnalysis(executionDto);
            
            // 結果が空の場合のチェック
            if (result == null || result.packageSummaries() == null || result.packageSummaries().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "解析が完了しましたが、結果が見つかりませんでした。プロジェクトパスを確認してください。");
            } else {
                redirectAttributes.addFlashAttribute("result", result);
            }
            redirectAttributes.addFlashAttribute("form", form);
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "エラー: " + e.getMessage());
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/";
        } catch (Exception e) {
            e.printStackTrace(); // デバッグ用
            redirectAttributes.addFlashAttribute("error", "解析エラー: " + e.getMessage() + " (詳細: " + e.getClass().getSimpleName() + ")");
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/";
        }

        return "redirect:/";
    }
}

