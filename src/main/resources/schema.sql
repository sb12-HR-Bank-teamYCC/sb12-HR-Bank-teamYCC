DROP TABLE IF EXISTS files CASCADE;
DROP TABLE IF EXISTS departments CASCADE;
DROP TABLE IF EXISTS employees CASCADE;
DROP TABLE IF EXISTS data_backups CASCADE;
DROP TABLE IF EXISTS employee_change_logs CASCADE;
DROP TABLE IF EXISTS employee_change_log_diffs CASCADE;

-- files 테이블 생성
CREATE TABLE files (
    id UUID PRIMARY KEY,
    original_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL UNIQUE,
    content_type VARCHAR(100) NOT NULL,
    size BIGINT NOT NULL,
    file_type VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL
);



-- departments 테이블 생성
CREATE TABLE departments (
                             id UUID PRIMARY KEY,
                             name VARCHAR(50) NOT NULL UNIQUE,
                             description TEXT,
                             established_date DATE NOT NULL,
                             created_at TIMESTAMP NOT NULL,
                             updated_at TIMESTAMP NOT NULL
);

-- employees 테이블 생성
CREATE TABLE employees (
                           id UUID PRIMARY KEY,
                           name VARCHAR(50) NOT NULL,
                           email VARCHAR(255) NOT NULL UNIQUE,
                           employee_number VARCHAR(30) NOT NULL UNIQUE,
                           position VARCHAR(50) NOT NULL,
                           hire_date DATE NOT NULL,
                           status VARCHAR(20) NOT NULL,
                           created_at TIMESTAMP NOT NULL,
                           updated_at TIMESTAMP NOT NULL,
                           department_id UUID NOT NULL,
                           profile_image_id UUID UNIQUE,

                           CONSTRAINT fk_employees_department
                               FOREIGN KEY (department_id)
                                   REFERENCES departments(id),

                           CONSTRAINT fk_employees_profile_image
                               FOREIGN KEY (profile_image_id)
                                   REFERENCES files(id)
                                   ON DELETE SET NULL
);

-- data_backups 테이블 생성
CREATE TABLE data_backups (
                              id UUID PRIMARY KEY,
                              worker VARCHAR(45) NOT NULL,
                              started_at TIMESTAMP NOT NULL,
                              ended_at TIMESTAMP,
                              status VARCHAR(20) NOT NULL,
                              created_at TIMESTAMP NOT NULL,
                              updated_at TIMESTAMP NOT NULL,
                              file_id UUID UNIQUE,

                              CONSTRAINT fk_data_backups_file
                                  FOREIGN KEY (file_id)
                                      REFERENCES files(id)
                                      ON DELETE SET NULL
);

-- employees_change_logs 테이블 생성
CREATE TABLE employee_change_logs (
                                      id UUID PRIMARY KEY,
                                      type VARCHAR(20) NOT NULL,
                                      employee_id UUID NOT NULL,
                                      employee_number VARCHAR(30) NOT NULL,
                                      employee_name VARCHAR(50) NOT NULL,
                                      memo TEXT,
                                      ip_address VARCHAR(45) NOT NULL,
                                      at TIMESTAMP NOT NULL
);

-- employess_change_log_diffs 테이블 생성
CREATE TABLE employee_change_log_diffs (
                                           id UUID PRIMARY KEY,
                                           property_name VARCHAR(100) NOT NULL,
                                           before_value TEXT,
                                           after_value TEXT,
                                           log_id UUID NOT NULL,

                                           CONSTRAINT fk_employee_change_log_diffs_log
                                               FOREIGN KEY (log_id)
                                                   REFERENCES employee_change_logs(id)
                                                   ON DELETE CASCADE
);