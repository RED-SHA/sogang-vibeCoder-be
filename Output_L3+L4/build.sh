#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
OUT="$ROOT/out"
JAR="$ROOT/kmedical.jar"

# 컴파일 완료 후 out/ 정리 (JAR은 유지)
cleanup() {
  rm -rf "$OUT"
}
trap cleanup EXIT

echo "=== [1/3] 컴파일 ==="
mkdir -p "$OUT"
find "$ROOT/src" -name "*.java" | xargs javac -d "$OUT" -cp "$ROOT/src/main/java"
echo "컴파일 완료 ($(find "$OUT" -name "*.class" | wc -l | tr -d ' ')개 클래스)"

echo ""
echo "=== [2/3] JAR 빌드 ==="
jar cfe "$JAR" com.kmedical.App -C "$OUT" .
echo "빌드 완료 → kmedical.jar ($(du -h "$JAR" | cut -f1))"

echo ""
echo "=== [3/3] 실행 ==="
java -jar "$JAR"
