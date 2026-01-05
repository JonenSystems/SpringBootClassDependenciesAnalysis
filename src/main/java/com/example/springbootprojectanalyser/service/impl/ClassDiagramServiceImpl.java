package com.example.springbootprojectanalyser.service.impl;

import com.example.springbootprojectanalyser.model.dto.*;
import com.example.springbootprojectanalyser.model.entity.*;
import com.example.springbootprojectanalyser.repository.*;
import com.example.springbootprojectanalyser.service.ClassDiagramService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * クラス図生成サービス実装クラス
 */
@Service
public class ClassDiagramServiceImpl implements ClassDiagramService {

    private static final int MAX_DEPTH = 10;

    private final EndpointRepository endpointRepository;
    private final ClassDependencyRepository classDependencyRepository;
    private final MemberRepository memberRepository;

    public ClassDiagramServiceImpl(
            EndpointRepository endpointRepository,
            ClassDependencyRepository classDependencyRepository,
            MemberRepository memberRepository) {
        this.endpointRepository = endpointRepository;
        this.classDependencyRepository = classDependencyRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ClassDiagramDto generateClassDiagram(java.util.UUID selectedEndpointId, Long projectId) {
        // SPC-201.002-001: 起点クラスの選択
        Endpoint endpoint = endpointRepository.findById(selectedEndpointId.toString())
            .orElseThrow(() -> new IllegalArgumentException("エンドポイントが見つかりません: " + selectedEndpointId));
        
        ClassEntity startClass = endpoint.getClassEntity();
        
        // SPC-201.003-001: 対象クラスの抽出
        Set<ClassEntity> targetClasses = extractTargetClasses(startClass, projectId);
        List<ClassInfoDto> targetClassList = targetClasses.stream()
            .map(c -> new ClassInfoDto(c.getId(), c.getFullQualifiedName(), c.getSimpleName()))
            .collect(Collectors.toList());
        
        // 依存関係マップの作成
        Map<String, Map<String, List<String>>> dependencyMap = buildDependencyMap(targetClasses, projectId);
        
        // インターフェースクラスのセットを作成（実装関係「001_002」のターゲットになっているクラス）
        Set<String> interfaceClassFqns = identifyInterfaceClasses(targetClasses, projectId);
        
        // 起点クラスのFQNを保持（コントローラクラスを明示的に表現するため）
        String startClassFqn = startClass.getFullQualifiedName();
        
        // エンドポイント情報を取得（URIとHTTPメソッド）
        String endpointUri = endpoint.getUri();
        String httpMethod = endpoint.getHttpMethod() != null ? endpoint.getHttpMethod().getMethodName() : "";
        
        // SPC-201.004-001: クラスダイアログ記載事項の抽出
        // 注意: 現在の実装では、クラス依存関係解析時にメンバー情報は保存されていないため、
        // 依存関係から推測してメンバー情報を生成します
        Map<String, List<MemberInfoDto>> classMemberMap = extractClassMembers(targetClasses, projectId);
        
        // SPC-201.005-001: クラス図の書式生成
        String classDiagramText = generateMermaidClassDiagram(targetClassList, classMemberMap, dependencyMap, interfaceClassFqns, startClassFqn, endpointUri, httpMethod);
        
        return new ClassDiagramDto(classDiagramText, targetClassList, classMemberMap, dependencyMap);
    }

    /**
     * 対象クラスを抽出する（再帰的に依存関係を追跡）
     * SPC-201.003-001に準拠
     * インターフェースクラスも含める（逆方向の依存関係も追跡）
     */
    private Set<ClassEntity> extractTargetClasses(ClassEntity startClass, Long projectId) {
        // 訪問済みセットを初期化（対象クラスリストとして使用）
        Set<ClassEntity> visited = new HashSet<>();
        // 起点クラスをキューに追加
        Queue<ClassEntity> queue = new LinkedList<>();
        queue.offer(startClass);
        // 起点クラスを対象クラスリストに追加
        visited.add(startClass);
        
        int depth = 0;
        // キューが空になるまで、または最大深度に達するまで処理
        while (!queue.isEmpty() && depth < MAX_DEPTH) {
            int levelSize = queue.size();
            depth++;
            
            for (int i = 0; i < levelSize; i++) {
                // キューからクラスを取得
                ClassEntity current = queue.poll();
                if (current == null) {
                    continue;
                }
                
                // 順方向の依存関係を取得（class_dependenciesテーブルを参照）
                List<ClassDependency> outgoingDependencies = classDependencyRepository
                    .findBySourceClass_Id(current.getId());
                
                // 順方向の依存関係を処理
                if (outgoingDependencies != null && !outgoingDependencies.isEmpty()) {
                    for (ClassDependency dep : outgoingDependencies) {
                        // プロジェクト内のクラスで、targetClassが解決されているもののみ処理
                        if (dep.getTargetClass() != null) {
                            ClassEntity target = dep.getTargetClass();
                            Long targetProjectId = target.getProject() != null ? target.getProject().getId() : null;
                            
                            if (targetProjectId != null && targetProjectId.equals(projectId)) {
                                // テストクラスを除外（クラス名がTestで終わるもの）
                                if (isTestClass(target)) {
                                    continue;
                                }
                                
                                // 未訪問の依存先クラスをキューに追加
                                if (!visited.contains(target)) {
                                    visited.add(target);
                                    queue.offer(target);
                                }
                            }
                        }
                    }
                }
                
                // 逆方向の依存関係を取得（インターフェースクラスを含めるため）
                // 例：実装クラスがインターフェースを実装している場合、そのインターフェースも含める
                List<ClassDependency> incomingDependencies = classDependencyRepository
                    .findByTargetClass_Id(current.getId());
                
                // 逆方向の依存関係を処理（実装関係「001_002」のみを対象）
                if (incomingDependencies != null && !incomingDependencies.isEmpty()) {
                    for (ClassDependency dep : incomingDependencies) {
                        // 実装関係（001_002）のみを対象とする
                        if (dep.getDependencyKind() == null || 
                            !"001_002".equals(dep.getDependencyKind().getCode())) {
                            continue;
                        }
                        
                        // プロジェクト内のクラスで、sourceClassが解決されているもののみ処理
                        if (dep.getSourceClass() != null) {
                            ClassEntity source = dep.getSourceClass();
                            
                            // テストクラスを除外（クラス名がTestで終わるもの）
                            if (isTestClass(source)) {
                                continue;
                            }
                            
                            Long sourceProjectId = source.getProject() != null ? source.getProject().getId() : null;
                            
                            if (sourceProjectId != null && sourceProjectId.equals(projectId)) {
                                // 未訪問の依存元クラスをキューに追加
                                // 実装関係（001_002）の場合のみ、インターフェースと実装クラスの両方を含める
                                if (!visited.contains(source)) {
                                    visited.add(source);
                                    queue.offer(source);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return visited;
    }

    /**
     * テストクラスかどうかを判定する
     * クラス名がTestで終わるもの、またはパッケージ名にtestが含まれるものをテストクラスとみなす
     */
    private boolean isTestClass(ClassEntity classEntity) {
        if (classEntity == null) {
            return false;
        }
        
        String simpleName = classEntity.getSimpleName();
        if (simpleName != null && simpleName.endsWith("Test")) {
            return true;
        }
        
        String fullQualifiedName = classEntity.getFullQualifiedName();
        if (fullQualifiedName != null) {
            // パッケージ名にtestが含まれるかチェック
            String lowerFqn = fullQualifiedName.toLowerCase();
            if (lowerFqn.contains(".test.") || lowerFqn.startsWith("test.")) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * インターフェースクラスを識別する
     * 実装関係（依存種類コード「001_002」）のターゲットになっているクラスをインターフェースとみなす
     */
    private Set<String> identifyInterfaceClasses(Set<ClassEntity> classes, Long projectId) {
        Set<String> interfaceFqns = new HashSet<>();
        
        for (ClassEntity classEntity : classes) {
            // このクラスをターゲットとする実装関係（001_002）を検索
            List<ClassDependency> incomingDependencies = classDependencyRepository
                .findByTargetClass_Id(classEntity.getId());
            
            for (ClassDependency dep : incomingDependencies) {
                // 実装関係（001_002）のみを対象
                if (dep.getDependencyKind() != null && 
                    "001_002".equals(dep.getDependencyKind().getCode()) &&
                    dep.getSourceClass() != null &&
                    dep.getSourceClass().getProject() != null &&
                    dep.getSourceClass().getProject().getId().equals(projectId) &&
                    classes.contains(dep.getSourceClass())) {
                    // このクラスが実装関係のターゲットになっている場合、インターフェースとみなす
                    interfaceFqns.add(classEntity.getFullQualifiedName());
                    break; // 1つでも見つかればインターフェースと判定
                }
            }
        }
        
        return interfaceFqns;
    }

    /**
     * 依存関係マップを構築する
     * 同じソース→ターゲットの組み合わせをまとめて、依存の種類をリストとして保持
     * 戻り値: ソースFQN -> ターゲットFQN -> 依存の種類ラベルのリスト
     */
    private Map<String, Map<String, List<String>>> buildDependencyMap(Set<ClassEntity> classes, Long projectId) {
        // ソースFQN -> ターゲットFQN -> 依存の種類ラベルのリスト
        Map<String, Map<String, List<String>>> dependencyMap = new HashMap<>();
        
        for (ClassEntity sourceClass : classes) {
            List<ClassDependency> dependencies = classDependencyRepository
                .findBySourceClass_Id(sourceClass.getId());
            
            String sourceFqn = sourceClass.getFullQualifiedName();
            Map<String, List<String>> targetMap = dependencyMap.computeIfAbsent(sourceFqn, k -> new HashMap<>());
            
            for (ClassDependency dep : dependencies) {
                if (dep.getTargetClass() != null && 
                    dep.getTargetClass().getProject().getId().equals(projectId) &&
                    classes.contains(dep.getTargetClass())) {
                    
                    String targetFqn = dep.getTargetClass().getFullQualifiedName();
                    String dependencyKindCode = dep.getDependencyKind() != null ? 
                        dep.getDependencyKind().getCode() : "";
                    String dependencyKindDescription = dep.getDependencyKind() != null ? 
                        dep.getDependencyKind().getDescription() : "";
                    
                    // 依存の種類ラベルを生成
                    String dependencyLabel = formatDependencyLabel(dependencyKindCode, dependencyKindDescription);
                    
                    if (dependencyLabel != null && !dependencyLabel.isEmpty()) {
                        targetMap.computeIfAbsent(targetFqn, k -> new ArrayList<>()).add(dependencyLabel);
                    }
                }
            }
        }
        
        return dependencyMap;
    }

    /**
     * クラスメンバーを抽出する（参照を受けるメンバーのみ）
     * メンバー情報がデータベースに保存されていない場合は、依存関係から推測する
     */
    private Map<String, List<MemberInfoDto>> extractClassMembers(Set<ClassEntity> classes, Long projectId) {
        Map<String, List<MemberInfoDto>> classMemberMap = new HashMap<>();
        
        for (ClassEntity classEntity : classes) {
            // このクラスから他のクラスへの依存関係を取得（DIなどで使用されるメンバーを推測）
            List<ClassDependency> outgoingDeps = classDependencyRepository
                .findBySourceClass_Id(classEntity.getId());
            
            // メンバーを取得（データベースに保存されている場合）
            List<Member> members = memberRepository.findByClassEntity_Id(classEntity.getId());
            
            List<MemberInfoDto> referencedMembers = new ArrayList<>();
            
            if (!members.isEmpty()) {
                // データベースにメンバー情報がある場合
                referencedMembers = members.stream()
                    .map(m -> new MemberInfoDto(
                        m.getName(),
                        m.getReturnType(),
                        m.getVisibility(),
                        m.getMemberType().getCode()
                    ))
                    .distinct() // 重複を削除
                    .collect(Collectors.toList());
            } else {
                // メンバー情報がない場合、依存関係から推測
                // すべての依存関係から、このクラス内で使用されている型を抽出
                Set<String> processedTargets = new HashSet<>();
                for (ClassDependency dep : outgoingDeps) {
                    if (dep.getTargetClass() != null && 
                        dep.getTargetClass().getProject().getId().equals(projectId) &&
                        classes.contains(dep.getTargetClass())) {
                        
                        String targetFqn = dep.getTargetClass().getFullQualifiedName();
                        if (processedTargets.contains(targetFqn)) {
                            continue; // 重複を避ける
                        }
                        processedTargets.add(targetFqn);
                        
                        String targetName = dep.getTargetClass().getSimpleName();
                        // フィールド名を推測（クラス名の最初の文字を小文字に）
                        String memberName = Character.toLowerCase(targetName.charAt(0)) + targetName.substring(1);
                        
                        // 依存タイプからメンバータイプを推測
                        String memberType = "FIELD";
                        String depKind = dep.getDependencyKind().getCode();
                        if ("002_003".equals(depKind)) {
                            // コンストラクタDI
                            memberType = "CONSTRUCTOR";
                            memberName = targetName; // コンストラクタはクラス名と同じ
                        } else if ("002_001".equals(depKind)) {
                            // SetterDI
                            memberType = "METHOD";
                            memberName = "set" + targetName;
                        } else if ("002_004".equals(depKind)) {
                            // フィールドDI
                            memberType = "FIELD";
                        } else if ("001_009".equals(depKind) || "001_010".equals(depKind)) {
                            // コンポジション、集合保持
                            memberType = "FIELD";
                        } else {
                            // その他の依存関係もフィールドとして表示
                            memberType = "FIELD";
                        }
                        
                        referencedMembers.add(new MemberInfoDto(
                            memberName,
                            dep.getTargetClass().getFullQualifiedName(),
                            "PRIVATE",
                            memberType
                        ));
                    }
                }
                // 重複を削除
                referencedMembers = referencedMembers.stream()
                    .distinct()
                    .collect(Collectors.toList());
            }
            
            // すべてのクラスにメンバー情報を追加（空の場合は空のリスト）
            classMemberMap.put(classEntity.getFullQualifiedName(), referencedMembers);
        }
        
        return classMemberMap;
    }

    /**
     * Mermaid形式のクラス図テキストを生成する
     */
    private String generateMermaidClassDiagram(
            List<ClassInfoDto> targetClasses,
            Map<String, List<MemberInfoDto>> classMemberMap,
            Map<String, Map<String, List<String>>> dependencyMap,
            Set<String> interfaceClassFqns,
            String startClassFqn,
            String endpointUri,
            String httpMethod) {
        
        StringBuilder sb = new StringBuilder();
        sb.append("classDiagram\n");
        
        // クラス定義を追加
        for (ClassInfoDto classInfo : targetClasses) {
            String className = escapeMermaidName(classInfo.simpleName());
            sb.append("    class ").append(className).append(" {\n");
            
            // 起点クラス（コントローラクラス）の場合は<<URI(HTTPメソッド)>>を追加
            if (startClassFqn != null && startClassFqn.equals(classInfo.fullQualifiedName())) {
                String endpointLabel = formatEndpointLabel(endpointUri, httpMethod);
                if (endpointLabel != null && !endpointLabel.isEmpty()) {
                    sb.append("        <<").append(escapeMermaidLabel(endpointLabel)).append(">>\n");
                }
            }
            
            // インターフェースクラスの場合は<<interface>>を追加
            if (interfaceClassFqns.contains(classInfo.fullQualifiedName())) {
                sb.append("        <<interface>>\n");
            }
            
            // メンバーを追加（重複を削除）
            List<MemberInfoDto> members = classMemberMap.get(classInfo.fullQualifiedName());
            if (members != null && !members.isEmpty()) {
                // フォーマット後のメンバー行の重複を削除
                Set<String> addedMemberLines = new LinkedHashSet<>();
                for (MemberInfoDto member : members) {
                    String memberLine = formatMember(member);
                    if (memberLine != null && !memberLine.trim().isEmpty()) {
                        addedMemberLines.add(memberLine);
                    }
                }
                // 重複を削除したメンバー行を追加
                for (String memberLine : addedMemberLines) {
                    sb.append("        ").append(memberLine).append("\n");
                }
            }
            
            sb.append("    }\n");
        }
        
        // 依存関係の矢印を追加（同じターゲットへの依存の種類をまとめる）
        for (Map.Entry<String, Map<String, List<String>>> sourceEntry : dependencyMap.entrySet()) {
            String sourceFqn = sourceEntry.getKey();
            ClassInfoDto sourceClass = targetClasses.stream()
                .filter(c -> c.fullQualifiedName().equals(sourceFqn))
                .findFirst()
                .orElse(null);
            
            if (sourceClass == null) {
                continue;
            }
            
            String sourceName = escapeMermaidName(sourceClass.simpleName());
            
            // ターゲットごとに依存の種類をまとめる
            for (Map.Entry<String, List<String>> targetEntry : sourceEntry.getValue().entrySet()) {
                String targetFqn = targetEntry.getKey();
                List<String> dependencyLabels = targetEntry.getValue();
                
                ClassInfoDto targetClass = targetClasses.stream()
                    .filter(c -> c.fullQualifiedName().equals(targetFqn))
                    .findFirst()
                    .orElse(null);
                
                if (targetClass == null) {
                    continue;
                }
                
                String targetName = escapeMermaidName(targetClass.simpleName());
                
                // 依存の種類ラベルを結合（重複を削除し、<br>で区切る）
                if (dependencyLabels != null && !dependencyLabels.isEmpty()) {
                    // 重複を削除
                    List<String> uniqueLabels = dependencyLabels.stream()
                        .distinct()
                        .collect(Collectors.toList());
                    
                    // <br>で結合
                    String combinedLabel = String.join(",<br>", uniqueLabels);
                    
                    // Mermaid classDiagramの構文: A --> B : label
                    sb.append("    ").append(sourceName)
                      .append(" --> ").append(targetName)
                      .append(" : ").append(escapeMermaidLabel(combinedLabel)).append("\n");
                } else {
                    // ラベルがない場合は通常の矢印
                    sb.append("    ").append(sourceName).append(" --> ").append(targetName).append("\n");
                }
            }
        }
        
        return sb.toString();
    }
    
    /**
     * 依存の種類ラベルをフォーマットする
     * 形式: code_description（例：001_002_実装（implements））
     */
    private String formatDependencyLabel(String code, String description) {
        if (code == null || code.isEmpty()) {
            return description != null ? description : "";
        }
        if (description == null || description.isEmpty()) {
            return code;
        }
        return code + "_" + description;
    }

    /**
     * エンドポイントラベルをフォーマットする
     * 形式: URI(HTTPメソッド)（例：/products(GET)）
     */
    private String formatEndpointLabel(String uri, String httpMethod) {
        if (uri == null || uri.isEmpty()) {
            return httpMethod != null && !httpMethod.isEmpty() ? "(" + httpMethod + ")" : "";
        }
        if (httpMethod == null || httpMethod.isEmpty()) {
            return uri;
        }
        return uri + "(" + httpMethod + ")";
    }
    
    /**
     * Mermaidラベルで使用できない文字をエスケープする
     */
    private String escapeMermaidLabel(String label) {
        if (label == null || label.isEmpty()) {
            return "";
        }
        // Mermaidラベル内の特殊文字をエスケープ
        // パイプ(|)や改行などはエスケープが必要
        return label.replace("|", "\\|")
                   .replace("\n", " ")
                   .replace("\r", " ")
                   .trim();
    }

    /**
     * メンバーをフォーマットする
     */
    private String formatMember(MemberInfoDto member) {
        if (member == null || member.name() == null || member.name().isEmpty()) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();
        
        // 可視性記号
        String visibility = member.visibility();
        if (visibility != null) {
            switch (visibility.toUpperCase()) {
                case "PUBLIC":
                    sb.append("+");
                    break;
                case "PROTECTED":
                    sb.append("#");
                    break;
                case "PRIVATE":
                    sb.append("-");
                    break;
                default:
                    sb.append("~");
            }
        } else {
            sb.append("-");
        }
        
        // メンバー名
        String memberName = escapeMermaidName(member.name());
        sb.append(memberName);
        
        // 戻り値型または型情報
        if (member.returnType() != null && !member.returnType().isEmpty()) {
            String returnType = member.returnType();
            // 完全修飾名の場合は最後の部分だけを取得
            if (returnType.contains(".")) {
                returnType = returnType.substring(returnType.lastIndexOf(".") + 1);
            }
            returnType = escapeMermaidName(returnType);
            
            // メソッドかフィールドかを判定
            if ("METHOD".equals(member.memberTypeCode()) || 
                "CONSTRUCTOR".equals(member.memberTypeCode())) {
                sb.append("() : ").append(returnType);
            } else {
                sb.append(" : ").append(returnType);
            }
        }
        
        return sb.toString();
    }

    /**
     * Mermaid記法で使用できない文字をエスケープする
     */
    private String escapeMermaidName(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        // 特殊文字をエスケープ（Mermaidで使用できない文字を置換）
        // ただし、クラス名として使用する場合は、アルファベット、数字、アンダースコアのみ許可
        String escaped = name.replaceAll("[^a-zA-Z0-9_]", "_");
        // 連続するアンダースコアを1つに
        escaped = escaped.replaceAll("_{2,}", "_");
        // 先頭・末尾のアンダースコアを削除
        escaped = escaped.replaceAll("^_+|_+$", "");
        return escaped.isEmpty() ? "Unknown" : escaped;
    }
}

