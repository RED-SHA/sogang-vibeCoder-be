# 컴파일 방법

## 환경 요구사항

- Java 11 이상 (`java`, `javac` 명령어 사용 가능해야 함)
- 별도 빌드 도구 불필요 (Pure Java SE, Spring/Maven/Gradle 없음)

## 컴파일

프로젝트 루트(`Output_L3+L4/`)에서 실행합니다.

```bash
mkdir -p out
find src -name "*.java" | xargs javac -d out -cp src/main/java
```

성공 시 아무 메시지 없이 종료됩니다 (exit code 0).

## 컴파일 결과 확인

```bash
find out -name "*.class" | wc -l
# 기대값: 162
```

## 정리

```bash
rm -rf out
```
