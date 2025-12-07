-- 外部キー制約を一時無効化
SET REFERENTIAL_INTEGRITY FALSE;

-- 全テーブルのデータを削除（AUTO_INCREMENTもリセット）
TRUNCATE TABLE class_dependencies;
TRUNCATE TABLE classes;
TRUNCATE TABLE packages;
TRUNCATE TABLE projects;
TRUNCATE TABLE dependency_kinds;

-- 外部キー制約を再有効化
SET REFERENTIAL_INTEGRITY TRUE;

-- 依存タイプの初期データ投入
INSERT INTO dependency_kinds (code, description) VALUES ('001_001', '継承（extends）');
INSERT INTO dependency_kinds (code, description) VALUES ('001_002', '実装（implements）');
INSERT INTO dependency_kinds (code, description) VALUES ('001_003', 'ジェネリクス型参照');
INSERT INTO dependency_kinds (code, description) VALUES ('001_004', '例外型依存');
INSERT INTO dependency_kinds (code, description) VALUES ('001_005', 'メソッド呼び出し');
INSERT INTO dependency_kinds (code, description) VALUES ('001_006', '戻り値型依存');
INSERT INTO dependency_kinds (code, description) VALUES ('001_007', '引数型依存');
INSERT INTO dependency_kinds (code, description) VALUES ('001_008', '静的メソッド依存');
INSERT INTO dependency_kinds (code, description) VALUES ('001_009', 'コンポジション（保持）');
INSERT INTO dependency_kinds (code, description) VALUES ('001_010', '集合保持');
INSERT INTO dependency_kinds (code, description) VALUES ('001_011', '定数参照');

