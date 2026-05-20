-- ============================================================
-- data.sql  ·  대용량 시드 데이터  (employees 1만 건 기준)
-- PostgreSQL generate_series + TEMP TABLE 전략
-- ============================================================
-- 생성 규모
--   departments            :    10건 (고정)
--   files (PROFILE_IMAGE)  : 10,000건
--   files (BACKUP_CSV/LOG) :   366건 내외
--   employees              : 10,000건
--   data_backups           :   366건 + IN_PROGRESS 1건
--   employee_change_logs   : 30,000건 (직원당 3건)
--   employee_change_log_diffs: 60,000건 (로그당 2건)
-- ============================================================

-- ────────────────────────────────────────────────────────────
-- 0. TRUNCATE
-- ────────────────────────────────────────────────────────────
TRUNCATE TABLE
    employee_change_log_diffs,
    employee_change_logs,
    data_backups,
    employees,
    departments,
    files
RESTART IDENTITY CASCADE;

-- ────────────────────────────────────────────────────────────
-- 1. departments (UUID 고정 — FK 참조용)
-- ────────────────────────────────────────────────────────────
INSERT INTO departments (id, name, description, established_date, created_at, updated_at) VALUES
    ('0196a7f0-0000-7000-8000-0000000003e8', '개발팀',    '백엔드와 프론트엔드 서비스 개발을 담당합니다.',     '2021-01-10', '2026-05-01 09:00:00', '2026-05-01 09:00:00'),
    ('0196a7f0-0000-7000-8000-0000000003e9', '인사팀',    '채용, 평가, 조직문화 운영을 담당합니다.',           '2020-03-15', '2026-05-01 09:00:00', '2026-05-01 09:00:00'),
    ('0196a7f0-0000-7000-8000-0000000003ea', '데이터팀',  '데이터 수집, 정제, 분석 파이프라인을 담당합니다.',  '2022-06-01', '2026-05-01 09:00:00', '2026-05-01 09:00:00'),
    ('0196a7f0-0000-7000-8000-0000000003eb', '품질관리팀','데이터 검수 및 품질 기준 관리를 담당합니다.',       '2022-09-20', '2026-05-01 09:00:00', '2026-05-01 09:00:00'),
    ('0196a7f0-0000-7000-8000-0000000003ec', '운영팀',    '서비스 운영과 고객 이슈 대응을 담당합니다.',        '2019-11-05', '2026-05-01 09:00:00', '2026-05-01 09:00:00'),
    ('0196a7f0-0000-7000-8000-0000000003ed', '기획팀',    '서비스 정책과 프로젝트 기획을 담당합니다.',         '2021-07-12', '2026-05-01 09:00:00', '2026-05-01 09:00:00'),
    ('0196a7f0-0000-7000-8000-0000000003ee', '디자인팀',  'UI/UX와 브랜드 디자인을 담당합니다.',              '2021-08-30', '2026-05-01 09:00:00', '2026-05-01 09:00:00'),
    ('0196a7f0-0000-7000-8000-0000000003ef', '보안팀',    '개인정보 보호와 시스템 보안을 담당합니다.',         '2023-02-01', '2026-05-01 09:00:00', '2026-05-01 09:00:00'),
    ('0196a7f0-0000-7000-8000-0000000003f0', '재무팀',    '회계, 정산, 비용 관리를 담당합니다.',              '2020-12-01', '2026-05-01 09:00:00', '2026-05-01 09:00:00'),
    ('0196a7f0-0000-7000-8000-0000000003f1', '마케팅팀',  '캠페인과 대외 커뮤니케이션을 담당합니다.',          '2022-04-18', '2026-05-01 09:00:00', '2026-05-01 09:00:00');

-- ────────────────────────────────────────────────────────────
-- 2. 프로필 이미지 파일 ID 사전 생성 (TEMP)
--    → employees.profile_image_id FK를 맞추기 위해 미리 UUID 확정
-- ────────────────────────────────────────────────────────────
CREATE TEMP TABLE _t_profile_files (
    rn      int  PRIMARY KEY,
    file_id uuid NOT NULL DEFAULT gen_random_uuid()
);

INSERT INTO _t_profile_files (rn)
SELECT generate_series(1, 10000);

-- ────────────────────────────────────────────────────────────
-- 3. files — PROFILE_IMAGE 10,000건
-- ────────────────────────────────────────────────────────────
INSERT INTO files (id, original_name, stored_name, content_type, size, file_type, created_at)
SELECT
    f.file_id,
    'profile_' || lpad(f.rn::text, 5, '0') || '.png',
    '202601_profile_' || lpad(f.rn::text, 5, '0') || '_' || f.file_id::text || '.png',
    'image/png',
    (8000 + floor(random() * 92000))::bigint,
    'PROFILE_IMAGE',
    '2025-06-01'::timestamp
        + floor(random() * 365)::int * INTERVAL '1 day'
        + floor(random() * 86400)::int * INTERVAL '1 second'
FROM _t_profile_files f;

-- ────────────────────────────────────────────────────────────
-- 4. 직원 ID / 번호 / 이름 사전 생성 (TEMP)
-- ────────────────────────────────────────────────────────────
CREATE TEMP TABLE _t_employees (
    rn         int  PRIMARY KEY,
    emp_id     uuid NOT NULL DEFAULT gen_random_uuid(),
    emp_number text NOT NULL,
    emp_name   text NOT NULL
);

INSERT INTO _t_employees (rn, emp_number, emp_name)
SELECT
    s.i,
    'EMP-2026-' || lpad(s.i::text, 6, '0'),
    (ARRAY['김','이','박','최','정','강','조','윤','장','임',
           '한','오','서','신','권','황','안','송','류','전'])
        [floor(random() * 20)::int + 1]
    ||
    (ARRAY['민준','서연','지후','하은','도윤','지민','수아','현우','예진','시우',
           '유나','준호','다은','태민','소윤','지아','민서','하준','은우','채원'])
        [floor(random() * 20)::int + 1]
FROM generate_series(1, 10000) s(i);

-- ────────────────────────────────────────────────────────────
-- 5. department 순번 룩업 (TEMP)
-- ────────────────────────────────────────────────────────────
CREATE TEMP TABLE _t_departments (rn int, dept_id uuid);

INSERT INTO _t_departments
SELECT row_number() OVER (ORDER BY established_date), id
FROM departments;

-- ────────────────────────────────────────────────────────────
-- 6. employees 10,000건
--    status: ACTIVE / ON_LEAVE / RESIGNED 순환
-- ────────────────────────────────────────────────────────────
INSERT INTO employees
    (id, name, email, employee_number, position, hire_date,
     status, created_at, updated_at, department_id, profile_image_id)
SELECT
    te.emp_id,
    te.emp_name,
    'employee' || lpad(te.rn::text, 6, '0') || '@example.com',
    te.emp_number,
    (ARRAY['사원','주임','대리','과장','차장','팀장','선임 개발자','데이터 매니저'])
        [floor(random() * 8)::int + 1],
    '2019-01-01'::date + floor(random() * 2000)::int,
    (ARRAY['ACTIVE','ON_LEAVE','RESIGNED'])[ ((te.rn - 1) % 3) + 1 ],
    '2025-06-01'::timestamp + floor(random() * 365)::int * INTERVAL '1 day',
    '2025-06-01'::timestamp + floor(random() * 365)::int * INTERVAL '1 day' + INTERVAL '2 hours',
    (SELECT dept_id FROM _t_departments WHERE rn = ((te.rn - 1) % 10) + 1),
    tf.file_id
FROM _t_employees te
JOIN _t_profile_files tf ON tf.rn = te.rn;

-- ────────────────────────────────────────────────────────────
-- 7. 백업 계획 수립 (TEMP)
--    365일 × 1건, rn % 30 = FAILED, rn % 20 = SKIPPED
-- ────────────────────────────────────────────────────────────
CREATE TEMP TABLE _t_backup_plan (
    rn      int  PRIMARY KEY,
    bdate   date NOT NULL,
    status  text NOT NULL,
    file_id uuid NOT NULL DEFAULT gen_random_uuid()
);

INSERT INTO _t_backup_plan (rn, bdate, status)
SELECT
    row_number() OVER (ORDER BY d.bdate),
    d.bdate,
    CASE
        WHEN extract(day FROM d.bdate)::int % 30 = 0 THEN 'FAILED'
        WHEN extract(day FROM d.bdate)::int % 20 = 0 THEN 'SKIPPED'
        ELSE 'COMPLETED'
    END
FROM (
    SELECT generate_series(
        '2025-05-20'::date,
        '2026-05-19'::date,
        '1 day'::interval
    )::date AS bdate
) d;

-- ────────────────────────────────────────────────────────────
-- 8. files — BACKUP_CSV / ERROR_LOG
-- ────────────────────────────────────────────────────────────
INSERT INTO files (id, original_name, stored_name, content_type, size, file_type, created_at)
SELECT
    bp.file_id,
    CASE bp.status
        WHEN 'COMPLETED' THEN 'employees_backup_' || to_char(bp.bdate, 'YYYY-MM-DD') || '.csv'
        ELSE                  'backup_error_'     || to_char(bp.bdate, 'YYYY-MM-DD') || '.log'
    END,
    CASE bp.status
        WHEN 'COMPLETED' THEN 'backup_'       || to_char(bp.bdate, 'YYYYMMDD') || '_' || bp.file_id::text || '.csv'
        ELSE                  'backup_error_' || to_char(bp.bdate, 'YYYYMMDD') || '_' || bp.file_id::text || '.log'
    END,
    CASE bp.status WHEN 'COMPLETED' THEN 'text/csv' ELSE 'text/plain' END,
    CASE bp.status
        WHEN 'COMPLETED' THEN (900000 + bp.rn * 2048)::bigint
        ELSE                  (1024   + bp.rn * 128)::bigint
    END,
    CASE bp.status WHEN 'COMPLETED' THEN 'BACKUP_CSV' ELSE 'ERROR_LOG' END,
    bp.bdate::timestamp + INTERVAL '1 hour 5 minutes'
FROM _t_backup_plan bp
WHERE bp.status <> 'SKIPPED';

-- ────────────────────────────────────────────────────────────
-- 9. data_backups (366건 + IN_PROGRESS 1건)
-- ────────────────────────────────────────────────────────────
INSERT INTO data_backups (id, worker, started_at, ended_at, status, created_at, updated_at, file_id)
SELECT
    gen_random_uuid(),
    CASE WHEN bp.rn % 10 = 0
         THEN '192.168.0.' || ((bp.rn % 40) + 1)
         ELSE 'system'
    END,
    bp.bdate::timestamp + INTERVAL '1 hour',
    CASE bp.status
        WHEN 'COMPLETED' THEN bp.bdate::timestamp + INTERVAL '1 hour 5 minutes'
        WHEN 'FAILED'    THEN bp.bdate::timestamp + INTERVAL '1 hour 3 minutes'
        WHEN 'SKIPPED'   THEN bp.bdate::timestamp + INTERVAL '1 hour 1 minute'
    END,
    bp.status,
    bp.bdate::timestamp - INTERVAL '30 seconds',
    bp.bdate::timestamp + INTERVAL '1 hour 5 minutes 30 seconds',
    CASE bp.status WHEN 'SKIPPED' THEN NULL ELSE bp.file_id END
FROM _t_backup_plan bp;

-- 오늘 날짜 IN_PROGRESS 1건 추가
INSERT INTO data_backups (id, worker, started_at, ended_at, status, created_at, updated_at, file_id)
VALUES (gen_random_uuid(), 'system', '2026-05-20 01:00:00', NULL, 'IN_PROGRESS',
        '2026-05-20 00:59:30', '2026-05-20 01:00:30', NULL);

-- ────────────────────────────────────────────────────────────
-- 10. 변경 로그 ID 사전 생성 (TEMP)
-- ────────────────────────────────────────────────────────────
CREATE TEMP TABLE _t_logs (
    rn     int  PRIMARY KEY,
    log_id uuid NOT NULL DEFAULT gen_random_uuid()
);

INSERT INTO _t_logs (rn)
SELECT generate_series(1, 30000);

-- ────────────────────────────────────────────────────────────
-- 11. employee_change_logs 30,000건 (직원당 3건, 순환)
-- ────────────────────────────────────────────────────────────
INSERT INTO employee_change_logs
    (id, type, employee_id, employee_number, employee_name, memo, ip_address, at)
SELECT
    tl.log_id,
    (ARRAY['CREATED','UPDATED','DELETED'])[ ((tl.rn - 1) % 3) + 1 ],
    te.emp_id,
    te.emp_number,
    te.emp_name,
    CASE (tl.rn % 4)
        WHEN 0 THEN NULL
        WHEN 1 THEN '직함 변경에 따른 수정'
        WHEN 2 THEN '부서 이동 반영'
        ELSE        '신규 직원 등록'
    END,
    '192.168.0.' || ((tl.rn % 40) + 1),
    '2025-01-01'::timestamp
        + floor(tl.rn::numeric / 30000 * 120)::int * INTERVAL '1 day'
        + (tl.rn % 86400) * INTERVAL '1 second'
FROM _t_logs tl
JOIN _t_employees te ON te.rn = ((tl.rn - 1) % 10000) + 1;

-- ────────────────────────────────────────────────────────────
-- 12. employee_change_log_diffs 60,000건 (로그당 2건, 순환)
-- ────────────────────────────────────────────────────────────
INSERT INTO employee_change_log_diffs (id, property_name, before_value, after_value, log_id)
SELECT
    gen_random_uuid(),
    (ARRAY['직함','부서','상태','이메일','이름'])[ ((s.i - 1) % 5) + 1 ],
    CASE ((s.i - 1) % 5)
        WHEN 0 THEN CASE WHEN s.i % 3 = 0 THEN NULL ELSE '사원'            END
        WHEN 1 THEN CASE WHEN s.i % 3 = 0 THEN NULL ELSE '운영팀'          END
        WHEN 2 THEN CASE WHEN s.i % 3 = 0 THEN NULL ELSE 'ACTIVE'          END
        WHEN 3 THEN CASE WHEN s.i % 3 = 0 THEN NULL ELSE 'old@example.com' END
        ELSE        CASE WHEN s.i % 3 = 0 THEN NULL ELSE '홍길동'          END
    END,
    CASE ((s.i - 1) % 5)
        WHEN 0 THEN CASE WHEN s.i % 3 = 1 THEN NULL ELSE '대리'            END
        WHEN 1 THEN CASE WHEN s.i % 3 = 1 THEN NULL ELSE '개발팀'          END
        WHEN 2 THEN CASE WHEN s.i % 3 = 1 THEN NULL ELSE 'ON_LEAVE'        END
        WHEN 3 THEN CASE WHEN s.i % 3 = 1 THEN NULL ELSE 'new@example.com' END
        ELSE        CASE WHEN s.i % 3 = 1 THEN NULL ELSE '홍길순'          END
    END,
    tl.log_id
FROM generate_series(1, 60000) s(i)
JOIN _t_logs tl ON tl.rn = ((s.i - 1) % 30000) + 1;

-- ────────────────────────────────────────────────────────────
-- 13. 임시 테이블 정리
-- ────────────────────────────────────────────────────────────
DROP TABLE IF EXISTS _t_profile_files, _t_employees, _t_departments, _t_backup_plan, _t_logs;
