-- 依存タイプテーブル
CREATE TABLE IF NOT EXISTS dependency_kinds (
    code VARCHAR(50) NOT NULL PRIMARY KEY,
    description VARCHAR(200) NOT NULL
);

-- プロジェクトテーブル
CREATE TABLE IF NOT EXISTS projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    root_path VARCHAR(1000) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL
);

-- パッケージテーブル
CREATE TABLE IF NOT EXISTS packages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    parent_package_id BIGINT,
    full_name VARCHAR(1000) NOT NULL,
    simple_name VARCHAR(500) NOT NULL,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_package_id) REFERENCES packages(id) ON DELETE CASCADE
);

-- クラステーブル
CREATE TABLE IF NOT EXISTS classes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    package_id BIGINT,
    full_qualified_name VARCHAR(1000) NOT NULL,
    simple_name VARCHAR(500) NOT NULL,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (package_id) REFERENCES packages(id) ON DELETE CASCADE
);

-- クラス依存関係テーブル
CREATE TABLE IF NOT EXISTS class_dependencies (
    dependency_record_id VARCHAR(36) NOT NULL PRIMARY KEY,
    source_class_id BIGINT NOT NULL,
    target_class_id BIGINT,
    source_class_fqn VARCHAR(1000) NOT NULL,
    target_identifier VARCHAR(1000) NOT NULL,
    dependency_kind_code VARCHAR(50) NOT NULL,
    detected_at TIMESTAMP NOT NULL,
    FOREIGN KEY (source_class_id) REFERENCES classes(id) ON DELETE CASCADE,
    FOREIGN KEY (target_class_id) REFERENCES classes(id) ON DELETE CASCADE,
    FOREIGN KEY (dependency_kind_code) REFERENCES dependency_kinds(code) ON DELETE RESTRICT
);

-- HTTPメソッドテーブル
CREATE TABLE IF NOT EXISTS http_methods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    method_name VARCHAR(10) NOT NULL UNIQUE
);

-- クラスタイプテーブル
CREATE TABLE IF NOT EXISTS class_types (
    code VARCHAR(50) NOT NULL PRIMARY KEY,
    description VARCHAR(200) NOT NULL
);

-- メンバータイプテーブル
CREATE TABLE IF NOT EXISTS member_types (
    code VARCHAR(50) NOT NULL PRIMARY KEY,
    description VARCHAR(200) NOT NULL
);

-- メンバーテーブル
CREATE TABLE IF NOT EXISTS members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_id BIGINT NOT NULL,
    member_type_code VARCHAR(50) NOT NULL,
    name VARCHAR(500) NOT NULL,
    return_type VARCHAR(1000),
    visibility VARCHAR(20),
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    FOREIGN KEY (member_type_code) REFERENCES member_types(code) ON DELETE RESTRICT
);

-- エンドポイントテーブル
CREATE TABLE IF NOT EXISTS endpoints (
    endpoint_id VARCHAR(36) NOT NULL PRIMARY KEY,
    class_id BIGINT NOT NULL,
    uri VARCHAR(1000) NOT NULL,
    http_method_id BIGINT NOT NULL,
    detected_at TIMESTAMP NOT NULL,
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    FOREIGN KEY (http_method_id) REFERENCES http_methods(id) ON DELETE RESTRICT
);

-- アノテーションテーブル
CREATE TABLE IF NOT EXISTS annotations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT,
    class_id BIGINT,
    annotation_name VARCHAR(500) NOT NULL,
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE
);

-- アノテーション属性テーブル
CREATE TABLE IF NOT EXISTS annotation_attributes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    annotation_id BIGINT NOT NULL,
    attribute_name VARCHAR(500),
    attribute_value VARCHAR(2000),
    FOREIGN KEY (annotation_id) REFERENCES annotations(id) ON DELETE CASCADE
);

-- インデックス作成
CREATE INDEX IF NOT EXISTS idx_packages_project_id ON packages(project_id);
CREATE INDEX IF NOT EXISTS idx_packages_parent_package_id ON packages(parent_package_id);
CREATE INDEX IF NOT EXISTS idx_classes_project_id ON classes(project_id);
CREATE INDEX IF NOT EXISTS idx_classes_package_id ON classes(package_id);
CREATE INDEX IF NOT EXISTS idx_classes_full_qualified_name ON classes(full_qualified_name);
CREATE INDEX IF NOT EXISTS idx_class_dependencies_source_class_id ON class_dependencies(source_class_id);
CREATE INDEX IF NOT EXISTS idx_class_dependencies_target_class_id ON class_dependencies(target_class_id);
CREATE INDEX IF NOT EXISTS idx_class_dependencies_dependency_kind_code ON class_dependencies(dependency_kind_code);
CREATE INDEX IF NOT EXISTS idx_members_class_id ON members(class_id);
CREATE INDEX IF NOT EXISTS idx_endpoints_class_id ON endpoints(class_id);
CREATE INDEX IF NOT EXISTS idx_endpoints_http_method_id ON endpoints(http_method_id);
CREATE INDEX IF NOT EXISTS idx_annotations_member_id ON annotations(member_id);
CREATE INDEX IF NOT EXISTS idx_annotations_class_id ON annotations(class_id);
CREATE INDEX IF NOT EXISTS idx_annotation_attributes_annotation_id ON annotation_attributes(annotation_id);

