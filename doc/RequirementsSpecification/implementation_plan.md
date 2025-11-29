# 実装計画書

## 1. 概要

SpringBootアプリケーションのクラス依存関係を解析するWebアプリケーションの実装計画。

### 1.1 実装範囲

- データベース設計とエンティティ実装
- 画面実装（解析入力・結果表示）
- 解析エンジン実装（JavaParserを使用）
- 10種類の依存関係解析機能
- 解析結果の永続化と表示

### 1.2 技術スタック

- **フレームワーク**: Spring Boot 3.5.8
- **Java**: 21
- **データベース**: H2（インメモリ）
- **ORM**: JPA（Hibernate）
- **テンプレートエンジン**: Thymeleaf
- **解析ライブラリ**: JavaParser
- **ビルドツール**: Maven

---

## 2. データベース設計

### 2.1 概念データモデルに基づくエンティティ設計

概念データモデル図から以下のエンティティを実装：

#### 2.1.1 基本エンティティ

1. **Project（プロジェクト）**
   - 解析対象プロジェクトの情報
   - フィールド: id, name, rootPath, createdAt

2. **Package（パッケージ）**
   - パッケージ情報（階層構造対応）
   - フィールド: id, projectId, name, parentPackageId, fullName

3. **Class（クラス）**
   - クラス情報
   - フィールド: id, packageId, name, fullQualifiedName, classTypeId

4. **ClassType（クラスタイプ）**
   - クラスの分類（Enum相当）
   - フィールド: id, code, name, description

5. **Member（メンバー）**
   - クラスのメンバー（フィールド、メソッド）
   - フィールド: id, classId, name, memberTypeId, signature

6. **MemberType（メンバータイプ）**
   - メンバーの分類（Enum相当）
   - フィールド: id, code, name, description

#### 2.1.2 依存関係エンティティ

7. **ClassDependency（依存関係）**
   - クラス間の依存関係（主要テーブル）
   - フィールド: id, sourceClassFqn, targetIdentifier, dependencyKindCode, detectedAt, analysisBatchId

8. **DependencyKind（依存タイプ）**
   - 依存関係の種類（Enum相当）
   - フィールド: id, code, name, description, category

#### 2.1.3 拡張エンティティ

9. **Endpoint（エンドポイント）**
   - HTTPエンドポイント情報
   - フィールド: id, memberId, path, httpMethodId

10. **HttpMethod（HTTPメソッド）**
    - HTTPメソッド（Enum相当）
    - フィールド: id, code, name

11. **Annotation（アノテーション）**
    - アノテーション情報
    - フィールド: id, memberId, name, fullQualifiedName

12. **AnnotationAttribute（アノテーション属性）**
    - アノテーションの属性
    - フィールド: id, annotationId, name, value

### 2.2 テーブル設計の優先順位

**Phase 1（最小実装）:**
- Project
- Package
- Class
- ClassType
- ClassDependency
- DependencyKind

**Phase 2（拡張実装）:**
- Member, MemberType
- Endpoint, HttpMethod
- Annotation, AnnotationAttribute

---

## 3. 実装フェーズ

### Phase 1: 基盤実装（必須）

#### 3.1 依存関係の追加
- [ ] `pom.xml`に必要な依存関係を追加
  - spring-boot-starter-web
  - spring-boot-starter-thymeleaf
  - spring-boot-starter-data-jpa
  - h2 (runtime)
  - com.github.javaparser:javaparser-core
  - com.github.javaparser:javaparser-symbol-solver-core

#### 3.2 データベース設定
- [ ] `application.properties`の設定
  - H2データベース設定
  - JPA設定（ddl-auto=create-drop）
  - H2コンソール設定
  - プロファイル設定（jpa）

#### 3.3 エンティティ実装（Phase 1）
- [ ] `model/entity/Project.java`
- [ ] `model/entity/Package.java`
- [ ] `model/entity/Class.java`
- [ ] `model/entity/ClassType.java`
- [ ] `model/entity/ClassDependency.java`
- [ ] `model/entity/DependencyKind.java`

#### 3.4 リポジトリ実装
- [ ] `repository/ProjectRepository.java`（インターフェース）
- [ ] `repository/ProjectRepositoryImpl.java`（実装）
- [ ] `repository/PackageRepository.java`（インターフェース）
- [ ] `repository/PackageRepositoryImpl.java`（実装）
- [ ] `repository/ClassRepository.java`（インターフェース）
- [ ] `repository/ClassRepositoryImpl.java`（実装）
- [ ] `repository/ClassDependencyRepository.java`（インターフェース）
- [ ] `repository/ClassDependencyRepositoryImpl.java`（実装）

#### 3.5 画面実装
- [ ] `controller/AnalysisController.java`
- [ ] `model/form/AnalysisForm.java`
- [ ] `model/dto/AnalysisResultDto.java`
- [ ] `templates/analysis/index.html`（入力画面）
- [ ] `templates/analysis/result.html`（結果表示画面）

#### 3.6 サービス層実装
- [ ] `service/AnalysisService.java`（インターフェース）
- [ ] `service/impl/AnalysisServiceImpl.java`（実装）
  - 解析実行のオーケストレーション
  - ファイルシステム走査
  - 解析結果の集約

#### 3.7 解析エンジン基盤
- [ ] `util/JavaSourceFileScanner.java`
  - ファイルシステムからJavaファイルを走査
  - パッケージパターンマッチング（Ant形式）
- [ ] `util/JavaParserUtil.java`
  - JavaParserの共通処理
  - AST生成のヘルパーメソッド

---

### Phase 2: 基本型依存関係の解析（SPC-001系）

#### 3.8 基本型依存関係解析サービス
- [ ] `service/analyzer/BasicTypeDependencyAnalyzer.java`（インターフェース）
- [ ] `service/analyzer/impl/BasicTypeDependencyAnalyzerImpl.java`（実装）

#### 3.9 個別解析実装
- [ ] SPC-001.001: 継承（extends）の解析
  - `service/analyzer/impl/ExtendsAnalyzer.java`
- [ ] SPC-001.002: 実装（implements）の解析
  - `service/analyzer/impl/ImplementsAnalyzer.java`
- [ ] SPC-001.003: フィールド依存の解析
  - `service/analyzer/impl/FieldDependencyAnalyzer.java`
- [ ] SPC-001.004: メソッド引数依存の解析
  - `service/analyzer/impl/MethodParameterDependencyAnalyzer.java`
- [ ] SPC-001.005: メソッド戻り値依存の解析
  - `service/analyzer/impl/MethodReturnTypeDependencyAnalyzer.java`
- [ ] SPC-001.006: ローカル変数依存の解析
  - `service/analyzer/impl/LocalVariableDependencyAnalyzer.java`
- [ ] SPC-001.007: 型パラメータ依存の解析
  - `service/analyzer/impl/TypeParameterDependencyAnalyzer.java`
- [ ] SPC-001.008: メソッド呼び出し依存の解析
  - `service/analyzer/impl/MethodInvocationDependencyAnalyzer.java`
- [ ] SPC-001.009: コンストラクタ呼び出し依存の解析
  - `service/analyzer/impl/ConstructorInvocationDependencyAnalyzer.java`
- [ ] SPC-001.010: 配列要素型依存の解析
  - `service/analyzer/impl/ArrayElementTypeDependencyAnalyzer.java`
- [ ] SPC-001.011: コレクション要素型依存の解析
  - `service/analyzer/impl/CollectionElementTypeDependencyAnalyzer.java`

---

### Phase 3: DI依存関係の解析（SPC-002系）

#### 3.10 DI依存関係解析サービス
- [ ] `service/analyzer/DiDependencyAnalyzer.java`（インターフェース）
- [ ] `service/analyzer/impl/DiDependencyAnalyzerImpl.java`（実装）

#### 3.11 個別解析実装
- [ ] SPC-002.001: SetterDIの解析
- [ ] SPC-002.002: フィールドDIの解析
- [ ] SPC-002.003: コンストラクタDIの解析
- [ ] SPC-002.004: @Bean提供の解析
- [ ] SPC-002.005: @Qualifier指定の解析
- [ ] SPC-002.006: @Primary指定の解析
- [ ] SPC-002.007: @ConditionalOnBeanの解析

---

### Phase 4: 永続化層依存関係の解析（SPC-003系）

#### 3.12 永続化層依存関係解析サービス
- [ ] `service/analyzer/PersistenceLayerDependencyAnalyzer.java`（インターフェース）
- [ ] `service/analyzer/impl/PersistenceLayerDependencyAnalyzerImpl.java`（実装）

#### 3.13 個別解析実装
- [ ] SPC-003.001: JPAリポジトリの解析
- [ ] SPC-003.002: JPAエンティティの解析
- [ ] SPC-003.003: クエリメソッドの解析
- [ ] SPC-003.004: @Entity関係の解析
- [ ] SPC-003.005: DTO/Entity変換の解析

---

### Phase 5: 設定・構成依存関係の解析（SPC-004系）

#### 3.14 設定・構成依存関係解析サービス
- [ ] `service/analyzer/ConfigurationDependencyAnalyzer.java`（インターフェース）
- [ ] `service/analyzer/impl/ConfigurationDependencyAnalyzerImpl.java`（実装）

#### 3.15 個別解析実装
- [ ] SPC-004.001: @Value注入の解析
- [ ] SPC-004.002: @ConfigurationPropertiesの解析
- [ ] SPC-004.003: @Profile指定の解析
- [ ] SPC-004.004: @ConditionalOnPropertyの解析
- [ ] SPC-004.005: ビルド構成依存の解析

---

### Phase 6: 外部サービス・イベント依存関係の解析（SPC-005系）

#### 3.16 外部サービス・イベント依存関係解析サービス
- [ ] `service/analyzer/ExternalServiceDependencyAnalyzer.java`（インターフェース）
- [ ] `service/analyzer/impl/ExternalServiceDependencyAnalyzerImpl.java`（実装）

#### 3.17 個別解析実装
- [ ] SPC-005.001: RESTクライアント依存の解析
- [ ] SPC-005.002: イベントリスナー依存の解析
- [ ] SPC-005.003: メッセージキュー依存の解析

---

### Phase 7: 横断的関心事依存関係の解析（SPC-006系）

#### 3.18 横断的関心事依存関係解析サービス
- [ ] `service/analyzer/CrossCuttingDependencyAnalyzer.java`（インターフェース）
- [ ] `service/analyzer/impl/CrossCuttingDependencyAnalyzerImpl.java`（実装）

#### 3.19 個別解析実装
- [ ] SPC-006.001: AOP依存の解析
- [ ] SPC-006.002: 観測性依存の解析
- [ ] SPC-006.003: バリデーション依存の解析
- [ ] SPC-006.004: 例外処理依存の解析

---

### Phase 8: セキュリティ依存関係の解析（SPC-007系）

#### 3.20 セキュリティ依存関係解析サービス
- [ ] `service/analyzer/SecurityDependencyAnalyzer.java`（インターフェース）
- [ ] `service/analyzer/impl/SecurityDependencyAnalyzerImpl.java`（実装）

#### 3.21 個別解析実装
- [ ] SPC-007.001～SPC-007.017: 17種類のセキュリティ依存関係解析

---

### Phase 9: ライブラリ依存関係の解析（SPC-008系）

#### 3.22 ライブラリ依存関係解析サービス
- [ ] `service/analyzer/LibraryDependencyAnalyzer.java`（インターフェース）
- [ ] `service/analyzer/impl/LibraryDependencyAnalyzerImpl.java`（実装）

#### 3.23 個別解析実装
- [ ] SPC-008.001: Lombok依存の解析
- [ ] SPC-008.002: Jackson依存の解析

---

### Phase 10: アーキテクチャ依存関係の解析（SPC-009系）

#### 3.24 アーキテクチャ依存関係解析サービス
- [ ] `service/analyzer/ArchitectureDependencyAnalyzer.java`（インターフェース）
- [ ] `service/analyzer/impl/ArchitectureDependencyAnalyzerImpl.java`（実装）

#### 3.25 個別解析実装
- [ ] SPC-009.001: Controller→Serviceの解析
- [ ] SPC-009.002: Service→Repositoryの解析
- [ ] SPC-009.003: Repository→Entityの解析
- [ ] SPC-009.004: レイヤー違反検出（Controller→Repository）
- [ ] SPC-009.005: レイヤー違反検出（Service→Controller）

---

### Phase 11: 設計品質指標の解析（SPC-010系）

#### 3.26 設計品質指標解析サービス
- [ ] `service/analyzer/QualityMetricAnalyzer.java`（インターフェース）
- [ ] `service/analyzer/impl/QualityMetricAnalyzerImpl.java`（実装）

#### 3.27 個別解析実装
- [ ] SPC-010.001: 循環依存（クラス）の解析
  - グラフアルゴリズム実装（Tarjan/Kosaraju等）
- [ ] SPC-010.002: 循環依存（パッケージ）の解析
- [ ] SPC-010.003: レイヤー違反の総合検出

---

## 4. パッケージ構成

```
com.example.springbootprojectanalyser
├── controller/
│   └── AnalysisController.java
├── service/
│   ├── AnalysisService.java
│   ├── impl/
│   │   └── AnalysisServiceImpl.java
│   └── analyzer/
│       ├── BasicTypeDependencyAnalyzer.java
│       ├── DiDependencyAnalyzer.java
│       ├── PersistenceLayerDependencyAnalyzer.java
│       ├── ConfigurationDependencyAnalyzer.java
│       ├── ExternalServiceDependencyAnalyzer.java
│       ├── CrossCuttingDependencyAnalyzer.java
│       ├── SecurityDependencyAnalyzer.java
│       ├── LibraryDependencyAnalyzer.java
│       ├── ArchitectureDependencyAnalyzer.java
│       ├── QualityMetricAnalyzer.java
│       └── impl/
│           └── [各実装クラス]
├── repository/
│   ├── ProjectRepository.java
│   ├── PackageRepository.java
│   ├── ClassRepository.java
│   ├── ClassDependencyRepository.java
│   └── impl/
│       └── [各実装クラス]
├── model/
│   ├── entity/
│   │   ├── Project.java
│   │   ├── Package.java
│   │   ├── Class.java
│   │   ├── ClassType.java
│   │   ├── ClassDependency.java
│   │   └── DependencyKind.java
│   ├── form/
│   │   └── AnalysisForm.java
│   └── dto/
│       ├── AnalysisResultDto.java
│       └── PackageSummaryDto.java
├── util/
│   ├── JavaSourceFileScanner.java
│   └── JavaParserUtil.java
└── config/
    └── JpaConfig.java（必要に応じて）
```

---

## 5. 実装の優先順位とマイルストーン

### マイルストーン1: 基盤完成（Phase 1）
**目標**: 解析実行の基本フローが動作する
- データベース設定完了
- 基本エンティティ実装完了
- 画面実装完了（入力・結果表示）
- ファイル走査機能実装完了

### マイルストーン2: 基本解析機能（Phase 2）
**目標**: 基本型依存関係の解析が動作する
- 継承・実装関係の解析
- フィールド・メソッド依存の解析
- 解析結果の表示

### マイルストーン3: DI解析機能（Phase 3）
**目標**: DI依存関係の解析が動作する
- 各種DIパターンの検出
- 解析結果の永続化

### マイルストーン4: アーキテクチャ解析（Phase 9, 10）
**目標**: アーキテクチャ層の依存関係とレイヤー違反を検出
- Controller→Service→Repositoryの依存関係検出
- レイヤー違反の検出

### マイルストーン5: 品質指標解析（Phase 11）
**目標**: 循環依存の検出が動作する
- クラス間循環依存の検出
- パッケージ間循環依存の検出

---

## 6. 技術的な考慮事項

### 6.1 JavaParserの使用方法
- AST（抽象構文木）を生成してソースコードを解析
- `ClassOrInterfaceDeclaration`からクラス情報を抽出
- `MethodDeclaration`からメソッド情報を抽出
- `FieldDeclaration`からフィールド情報を抽出
- アノテーション情報の抽出

### 6.2 ファイルシステム走査
- `java.nio.file.Files.walk()`を使用
- Ant形式のパッケージパターンマッチング
- `.java`ファイルのみを対象

### 6.3 パフォーマンス考慮
- 大量のソースファイルを解析する場合のメモリ使用量
- 解析結果のバッチ保存
- 非同期処理の検討（将来拡張）

### 6.4 エラーハンドリング
- パースエラーの処理（不正なJavaファイル）
- ファイルアクセスエラーの処理
- データベースエラーの処理

---

## 7. テスト計画

### 7.1 単体テスト
- 各解析サービスのテスト
- リポジトリのテスト
- ユーティリティクラスのテスト

### 7.2 統合テスト
- 解析実行フローのテスト
- データベース保存のテスト
- 画面遷移のテスト

### 7.3 テストデータ
- サンプルSpringBootプロジェクトの準備
- 各種依存関係パターンを含むテストコード

---

## 8. 今後の拡張検討事項

- Phase 2以降のエンティティ実装（Member, Endpoint, Annotation等）
- 解析結果の可視化（グラフ表示）
- 解析履歴の管理
- 複数プロジェクトの比較機能
- 解析結果のエクスポート機能（CSV, JSON等）

---

## 9. 実装開始時のチェックリスト

- [ ] `pom.xml`の依存関係確認
- [ ] `application.properties`の設定確認
- [ ] パッケージ構成の作成
- [ ] エンティティクラスの実装開始
- [ ] リポジトリインターフェースの定義
- [ ] サービスインターフェースの定義
- [ ] コントローラーの基本実装
- [ ] 画面テンプレートの作成

