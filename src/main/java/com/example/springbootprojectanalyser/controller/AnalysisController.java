package com.example.springbootprojectanalyser.controller;

import com.example.springbootprojectanalyser.model.dto.AnalysisResultDto;
import com.example.springbootprojectanalyser.model.form.AnalysisForm;
import com.example.springbootprojectanalyser.service.AnalysisService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 解析コントローラー
 */
@Controller
@RequestMapping("/analysis")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    /**
     * 解析入力画面を表示する
     *
     * @param model モデル
     * @return テンプレート名
     */
    @GetMapping
    public String index(Model model) {
        if (!model.containsAttribute("analysisForm")) {
            model.addAttribute("analysisForm", new AnalysisForm("", "**"));
        }
        return "analysis/index";
    }

    /**
     * 解析を実行する
     *
     * @param form 解析フォーム
     * @param bindingResult バリデーション結果
     * @param redirectAttributes リダイレクト属性
     * @return リダイレクト先
     */
    @PostMapping("/execute")
    public String execute(
        @Valid @ModelAttribute AnalysisForm form,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.analysisForm", bindingResult);
            redirectAttributes.addFlashAttribute("analysisForm", form);
            return "redirect:/analysis";
        }

        try {
            AnalysisResultDto result = analysisService.executeAnalysis(form.toDto());
            redirectAttributes.addFlashAttribute("result", result);
            return "redirect:/analysis/result";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "解析実行中にエラーが発生しました: " + e.getMessage());
            redirectAttributes.addFlashAttribute("analysisForm", form);
            return "redirect:/analysis";
        }
    }

    /**
     * 解析結果画面を表示する
     *
     * @param model モデル
     * @return テンプレート名
     */
    @GetMapping("/result")
    public String result(Model model) {
        if (!model.containsAttribute("result")) {
            return "redirect:/analysis";
        }
        return "analysis/result";
    }
}

