# 🏦 HR Bank

> **Batch로 데이터를 관리하는 Open EMS**
> 기업의 인적 자원 데이터를 안전하게 저장하고, 대량의 데이터를 주기적으로 백업 및 관리하는 HR 관리 시스템

**프로젝트 기간:** 2026.05.14 ~ 2026.05.26

<br>

---

## 팀원 소개 — Young Creator Crew (YCC)

| 이름 | 역할 |
|------|------|
| [장현우](https://github.com/gusdn6763) | 프로젝트 총괄·기술 리드, 공통 모듈 개발, 데이터 백업·스케줄링, 페이지네이션, PR 리뷰 및 병합, 배포 |
| [김수아](https://github.com/suaripa) | 포트폴리오 제작, 발표 자료 관리, 팀 일정·커뮤니케이션 관리 |
| [석지예](https://github.com/zziyo8) | 기능 명세서 및 API 명세서, 부서 Entity·DTO·Mapper·Repository·Service·Controller |
| [송유정](https://github.com/Yoojungee) | 파일 Entity·Repository·Service·Controller, 파일 업로드·다운로드, FileStorage·FileConfig |
| [오소현](https://github.com/Oh-Sohyeon) | ERD 작성 및 DB 모델링, 직원·수정 이력 Entity·DTO·Mapper·Service·Controller |

<br>

---

## 기술 스택

### Backend

![Java](https://img.shields.io/badge/Java-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=flat-square&logo=spring&logoColor=white)
![QueryDSL](https://img.shields.io/badge/QueryDSL-0769AD?style=flat-square)
![MapStruct](https://img.shields.io/badge/MapStruct-red?style=flat-square)

### Database

![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=flat-square&logo=postgresql&logoColor=white)

### DevOps & Communication

![Git](https://img.shields.io/badge/Git-F05032?style=flat-square&logo=git&logoColor=white)
![GitHub](https://img.shields.io/badge/GitHub-181717?style=flat-square&logo=github&logoColor=white)
![Railway](https://img.shields.io/badge/Railway-0B0D0E?style=flat-square&logo=railway&logoColor=white)
![Notion](https://img.shields.io/badge/Notion-000000?style=flat-square&logo=notion&logoColor=white)
![Discord](https://img.shields.io/badge/Discord-5865F2?style=flat-square&logo=discord&logoColor=white)

<br>

---

## 주요 기능

### 🏢 부서 관리

- 부서 등록·수정·삭제·목록 조회
- 이름/설명 부분 일치 검색
- 이름·설립일 기준 정렬 및 커서 기반 페이지네이션

### 👤 직원 정보 관리

- 직원 등록·수정·삭제·상세 조회·목록 조회
- 사원 번호 자동 부여, 상태 관리 (`재직중` / `휴직중` / `퇴사`)
- 이름·이메일·부서·직함·사원번호·입사일·상태 기준 복합 검색
- 이름·입사일·사원번호 기준 정렬 및 커서 기반 페이지네이션

### 📁 파일 관리

- 메타 정보(DB)와 실제 파일(로컬 디스크) 분리 저장
- 파일 ID 기반 다운로드

### 📋 직원 수정 이력 관리

- 직원 추가·수정·삭제 시 자동 이력 기록
- 변경 전/후 값, IP 주소, 메모, 유형 저장
- 이력 목록 조회(커서 기반 페이지네이션) 및 상세 변경 내용 조회

### 💾 데이터 백업 관리

- Spring Scheduler 기반 1시간 주기 자동 백업
- 마지막 완료 백업 이후 변경 데이터가 있는 경우에만 백업 수행
- 전체 직원 데이터를 CSV로 저장 (OOM 방지 처리 포함)
- 백업 성공/실패/건너뜀 이력 관리, 실패 시 에러 로그 파일 저장
- 백업 이력 목록 조회 (작업자·시작 시간·상태 기준 필터링)

### 📊 대시보드

- 총 직원 수 / 최근 일주일 수정 이력 건수 / 이번달 입사자 수 / 마지막 백업 시간
- 최근 1년 월별 직원수 변동 추이
- 부서별·직무별 직원 분포

<br>

---

## 파일 구조

```
src
 ┣ main
 ┃ ┣ java
 ┃ ┃ ┗ com
 ┃ ┃   ┗ hrbank
 ┃ ┃     ┣ common
 ┃ ┃     ┃ ┣ config
 ┃ ┃     ┃ ┃ ┣ FileConfig.java
 ┃ ┃     ┃ ┃ ┣ OpenApiConfig.java
 ┃ ┃     ┃ ┃ ┣ QuerydslConfig.java
 ┃ ┃     ┃ ┃ ┗ WebConfig.java
 ┃ ┃     ┃ ┣ exception
 ┃ ┃     ┃ ┃ ┣ ErrorCode.java
 ┃ ┃     ┃ ┃ ┗ GlobalExceptionHandler.java
 ┃ ┃     ┃ ┣ file
 ┃ ┃     ┃ ┃ ┗ FileStorage.java
 ┃ ┃     ┃ ┗ scheduler
 ┃ ┃     ┃   ┗ BackupScheduler.java
 ┃ ┃     ┣ controller
 ┃ ┃     ┃ ┣ ChangeLogController.java
 ┃ ┃     ┃ ┣ DataBackupController.java
 ┃ ┃     ┃ ┣ DepartmentController.java
 ┃ ┃     ┃ ┣ EmployeeController.java
 ┃ ┃     ┃ ┗ FileMetadataController.java
 ┃ ┃     ┣ dto
 ┃ ┃     ┃ ┣ changeLog
 ┃ ┃     ┃ ┃ ┣ ChangeLogDetailDto.java
 ┃ ┃     ┃ ┃ ┣ ChangeLogDto.java
 ┃ ┃     ┃ ┃ ┗ DiffDto.java
 ┃ ┃     ┃ ┣ dataBackup
 ┃ ┃     ┃ ┃ ┗ DataBackupDto.java
 ┃ ┃     ┃ ┣ department
 ┃ ┃     ┃ ┃ ┣ DepartmentCreateRequest.java
 ┃ ┃     ┃ ┃ ┣ DepartmentDto.java
 ┃ ┃     ┃ ┃ ┣ DepartmentResponse.java
 ┃ ┃     ┃ ┃ ┣ DepartmentSearchRequest.java
 ┃ ┃     ┃ ┃ ┣ DepartmentSliceResponse.java
 ┃ ┃     ┃ ┃ ┣ DepartmentUpdateRequest.java
 ┃ ┃     ┃ ┃ ┣ SortDirection.java
 ┃ ┃     ┃ ┃ ┗ SortField.java
 ┃ ┃     ┃ ┣ employee
 ┃ ┃     ┃ ┃ ┣ EmployeeCreateRequest.java
 ┃ ┃     ┃ ┃ ┣ EmployeeDistributionDto.java
 ┃ ┃     ┃ ┃ ┣ EmployeeDto.java
 ┃ ┃     ┃ ┃ ┣ EmployeeTrendDto.java
 ┃ ┃     ┃ ┃ ┗ EmployeeUpdateRequest.java
 ┃ ┃     ┃ ┣ error
 ┃ ┃     ┃ ┃ ┣ ErrorResponse.java
 ┃ ┃     ┃ ┃ ┗ HrBankException.java
 ┃ ┃     ┃ ┗ page
 ┃ ┃     ┃   ┣ CursorPageResponse.java
 ┃ ┃     ┃   ┗ CursorPageResponseDepartmentDto.java
 ┃ ┃     ┣ entity
 ┃ ┃     ┃ ┣ backupStatus
 ┃ ┃     ┃ ┃ ┣ BackupStatus.java
 ┃ ┃     ┃ ┃ ┗ DataBackup.java
 ┃ ┃     ┃ ┣ base
 ┃ ┃     ┃ ┃ ┣ BaseEntity.java
 ┃ ┃     ┃ ┃ ┗ BaseUpdatableEntity.java
 ┃ ┃     ┃ ┣ changeLog
 ┃ ┃     ┃ ┃ ┣ ChangeLog.java
 ┃ ┃     ┃ ┃ ┣ ChangeType.java
 ┃ ┃     ┃ ┃ ┗ DetailChangeLog.java
 ┃ ┃     ┃ ┣ employee
 ┃ ┃     ┃ ┃ ┣ Employee.java
 ┃ ┃     ┃ ┃ ┗ EmployeeStatus.java
 ┃ ┃     ┃ ┣ file
 ┃ ┃     ┃ ┃ ┣ FileMetadata.java
 ┃ ┃     ┃ ┃ ┗ FileTypeConst.java
 ┃ ┃     ┃ ┗ Department.java
 ┃ ┃     ┣ mapper
 ┃ ┃     ┃ ┣ ChangeLogMapper.java
 ┃ ┃     ┃ ┣ DataBackupMapper.java
 ┃ ┃     ┃ ┣ DepartmentMapper.java
 ┃ ┃     ┃ ┗ EmployeeMapper.java
 ┃ ┃     ┣ repository
 ┃ ┃     ┃ ┣ querydsl
 ┃ ┃     ┃ ┃ ┣ ChangeLogQueryRepository.java
 ┃ ┃     ┃ ┃ ┣ ChangeLogQueryRepositoryImpl.java
 ┃ ┃     ┃ ┃ ┣ DataBackupQueryRepository.java
 ┃ ┃     ┃ ┃ ┣ DataBackupQueryRepositoryImpl.java
 ┃ ┃     ┃ ┃ ┣ EmployeeRepositoryCustom.java
 ┃ ┃     ┃ ┃ ┗ EmployeeRepositoryCustomImpl.java
 ┃ ┃     ┃ ┣ ChangeLogRepository.java
 ┃ ┃     ┃ ┣ DataBackupRepository.java
 ┃ ┃     ┃ ┣ DepartmentRepository.java
 ┃ ┃     ┃ ┣ EmployeeRepository.java
 ┃ ┃     ┃ ┗ FileMetadataRepository.java
 ┃ ┃     ┣ service
 ┃ ┃     ┃ ┣ dataBackup
 ┃ ┃     ┃ ┃ ┣ DataBackupService.java
 ┃ ┃     ┃ ┃ ┗ DataBackupTxService.java
 ┃ ┃     ┃ ┣ ChangeLogService.java
 ┃ ┃     ┃ ┣ DepartmentService.java
 ┃ ┃     ┃ ┣ EmployeeService.java
 ┃ ┃     ┃ ┣ EmployeeSnapshot.java
 ┃ ┃     ┃ ┗ FileMetadataService.java
 ┃ ┃     ┗ HrBankApplication.java
 ┃ ┗ resources
 ┃   ┣ application.yml
 ┃   ┣ application-dev.yml
 ┃   ┣ application-prod.yml
 ┃   ┣ bigData.sql
 ┃   ┣ data.sql
 ┃   ┗ schema.sql
 ┗ test
   ┗ java
     ┗ com
       ┗ hrbank
         ┗ HrBankApplicationTests.java
 ┣ build.gradle
 ┣ settings.gradle
 ┣ .gitignore
 ┗ README.md
```

<br>

---

## 배포

[🔗 서비스 바로가기](https://sb12-hr-bank-teamycc-production.up.railway.app/#/histories)
