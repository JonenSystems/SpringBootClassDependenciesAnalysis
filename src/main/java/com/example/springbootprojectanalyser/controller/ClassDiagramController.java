package com.example.springbootprojectanalyser.controller;

import com.example.springbootprojectanalyser.model.dto.ClassDiagramDto;
import com.example.springbootprojectanalyser.model.dto.EndpointDto;
import com.example.springbootprojectanalyser.model.form.ClassDiagramForm;
import com.example.springbootprojectanalyser.repository.EndpointRepository;
import com.example.springbootprojectanalyser.repository.ProjectRepository;
import com.example.springbootprojectanalyser.service.ClassDiagramService;
import com.example.springbootprojectanalyser.service.EndpointExtractionService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

/**
 * クラス図作成コントローラー
 */
@Controller
public class ClassDiagramController {

    private final EndpointExtractionService endpointExtractionService;
    private final ClassDiagramService classDiagramService;
    private final ProjectRepository projectRepository;
    private final EndpointRepository endpointRepository;

    public ClassDiagramController(
            EndpointExtractionService endpointExtractionService,
            ClassDiagramService classDiagramService,
            ProjectRepository projectRepository,
            EndpointRepository endpointRepository) {
        this.endpointExtractionService = endpointExtractionService;
        this.classDiagramService = classDiagramService;
        this.projectRepository = projectRepository;
        this.endpointRepository = endpointRepository;
    }

    @GetMapping({"/classdiagram", "/classdiagram/"})
    public String index(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", ClassDiagramForm.empty());
        }
        // プロジェクト一覧を取得してモデルに追加
        model.addAttribute("projects", projectRepository.findAll());
        // フォームから選択済みエンドポイントIDを取得してモデルに追加（エンドポイント選択の保持のため）
        // リダイレクト属性のselectedEndpointIdが優先されるが、フォームにも設定する
        if (model.containsAttribute("form")) {
            ClassDiagramForm form = (ClassDiagramForm) model.getAttribute("form");
            if (form != null && form.selectedEndpointId() != null && !form.selectedEndpointId().isEmpty()) {
                // リダイレクト属性にselectedEndpointIdがない場合のみ、フォームから設定
                if (!model.containsAttribute("selectedEndpointId")) {
                    model.addAttribute("selectedEndpointId", form.selectedEndpointId());
                }
            }
        }
        return "classdiagram/index";
    }

    @PostMapping("/classdiagram/extract")
    public String extractEndpoints(
            @Valid @ModelAttribute("form") ClassDiagramForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.form", bindingResult);
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/classdiagram/";
        }

        try {
            // エンドポイント情報を抽出
            String targetPackagePattern = "**"; // 全パッケージを対象
            List<EndpointDto> endpoints = endpointExtractionService.extractEndpoints(
                form.targetProjectPath(),
                targetPackagePattern
            );
            
            // プロジェクトIDを取得
            Long projectId = projectRepository.findByRootPath(form.targetProjectPath())
                .map(p -> p.getId())
                .orElseThrow(() -> new IllegalArgumentException("プロジェクトが見つかりません"));
            
            redirectAttributes.addFlashAttribute("endpoints", endpoints);
            redirectAttributes.addFlashAttribute("projectId", projectId);
            redirectAttributes.addFlashAttribute("form", form);
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "エラー: " + e.getMessage());
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/classdiagram/";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "解析エラー: " + e.getMessage());
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/classdiagram/";
        }

        return "redirect:/classdiagram/";
    }

    @PostMapping("/classdiagram/generate")
    public String generateClassDiagram(
            @RequestParam("selectedEndpointId") String selectedEndpointId,
            @RequestParam("projectId") Long projectId,
            RedirectAttributes redirectAttributes) {
        
        try {
            UUID endpointId = UUID.fromString(selectedEndpointId);
            
            // 選択されたエンドポイントの情報を取得（URI、HTTPメソッド、クラス名を保存するため）
            com.example.springbootprojectanalyser.model.entity.Endpoint selectedEndpoint = 
                endpointRepository.findById(selectedEndpointId)
                    .orElseThrow(() -> new IllegalArgumentException("エンドポイントが見つかりません: " + selectedEndpointId));
            
            String selectedUri = selectedEndpoint.getUri();
            String selectedHttpMethod = selectedEndpoint.getHttpMethod().getMethodName();
            String selectedClassName = selectedEndpoint.getClassEntity().getSimpleName();
            
            ClassDiagramDto classDiagram = classDiagramService.generateClassDiagram(endpointId, projectId);
            
            // プロジェクト情報を取得
            com.example.springbootprojectanalyser.model.entity.Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("プロジェクトが見つかりません"));
            
            // エンドポイント情報を再取得（クラス図表示後もエンドポイント選択を可能にするため）
            String targetPackagePattern = "**"; // 全パッケージを対象
            List<EndpointDto> endpoints = endpointExtractionService.extractEndpoints(
                project.getRootPath(),
                targetPackagePattern
            );
            
            // 再取得後のエンドポイント一覧から、URI、HTTPメソッド、クラス名で一致するエンドポイントを探す
            String newSelectedEndpointId = endpoints.stream()
                .filter(e -> e.uri().equals(selectedUri) && 
                            e.httpMethodName().equals(selectedHttpMethod) && 
                            e.className().equals(selectedClassName))
                .map(EndpointDto::endpointId)
                .findFirst()
                .orElse(null);
            
            // フォーム情報を作成（プロジェクトパスを保持）
            ClassDiagramForm form = new ClassDiagramForm(project.getRootPath(), 
                newSelectedEndpointId != null ? newSelectedEndpointId : selectedEndpointId);
            
            redirectAttributes.addFlashAttribute("classDiagram", classDiagram);
            redirectAttributes.addFlashAttribute("selectedEndpointId", 
                newSelectedEndpointId != null ? newSelectedEndpointId : selectedEndpointId);
            redirectAttributes.addFlashAttribute("selectedEndpointUri", selectedUri);
            redirectAttributes.addFlashAttribute("selectedEndpointHttpMethod", selectedHttpMethod);
            redirectAttributes.addFlashAttribute("selectedEndpointClassName", selectedClassName);
            redirectAttributes.addFlashAttribute("projectId", projectId);
            redirectAttributes.addFlashAttribute("endpoints", endpoints);
            redirectAttributes.addFlashAttribute("form", form);
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "エラー: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "クラス図生成エラー: " + e.getMessage());
        }

        return "redirect:/classdiagram/";
    }
}

