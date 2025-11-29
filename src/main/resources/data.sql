-- 外部キー制約を一時無効化
SET REFERENTIAL_INTEGRITY FALSE;

-- 全テーブルのデータを削除（AUTO_INCREMENTもリセット）
-- H2では TRUNCATE TABLE IF EXISTS はサポートされていないため、
-- TRUNCATE TABLE のみを使用（ddl-auto=create-dropによりテーブルは常に存在する）
TRUNCATE TABLE class_dependencies;
TRUNCATE TABLE classes;
TRUNCATE TABLE packages;
TRUNCATE TABLE projects;
TRUNCATE TABLE class_types;
TRUNCATE TABLE dependency_kinds;

-- 外部キー制約を再有効化
SET REFERENTIAL_INTEGRITY TRUE;

-- クラスタイプの初期データ
INSERT INTO class_types (code, name, description) VALUES
('CLASS', 'クラス', '通常のクラス'),
('INTERFACE', 'インターフェース', 'インターフェース'),
('ENUM', '列挙型', '列挙型'),
('ANNOTATION', 'アノテーション', 'アノテーション型'),
('RECORD', 'レコード', 'レコード型（Java 14+）');

-- 依存種類の初期データ（基本型依存関係）
INSERT INTO dependency_kinds (code, name, description, category) VALUES
('001_001', '継承（extends）', 'クラスが別のクラスを継承して機能を拡張している状態', '基本型依存関係'),
('001_002', '実装（implements）', 'インタフェースを実装し、契約に沿って動作を保証する関係', '基本型依存関係'),
('001_003', 'フィールド依存', 'フィールドで他型を参照', '基本型依存関係'),
('001_004', 'メソッド引数依存', 'メソッド引数で他型を参照', '基本型依存関係'),
('001_005', 'メソッド戻り値依存', 'メソッド戻り値で他型を参照', '基本型依存関係'),
('001_006', 'ローカル変数依存', 'ローカル変数で他型を参照', '基本型依存関係'),
('001_007', '型パラメータ依存', '型パラメータで他型を参照', '基本型依存関係'),
('001_008', 'メソッド呼び出し依存', 'メソッド呼び出しで他型を参照', '基本型依存関係'),
('001_009', 'コンストラクタ呼び出し依存', 'コンストラクタ呼び出しで他型を参照', '基本型依存関係'),
('001_010', '配列要素型依存', '配列要素型で他型を参照', '基本型依存関係'),
('001_011', 'コレクション要素型依存', 'コレクション要素型で他型を参照', '基本型依存関係');

