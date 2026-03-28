#!/bin/bash
# PostToolUse hook: Write matcher
# Checks if a Controller file was written and reminds to add Swagger annotations

fp=$(jq -r '.tool_input.file_path // ""')

if echo "$fp" | grep -qE '/controller/[A-Z].*Controller\.java$'; then
  cat <<'HOOKJSON'
{"hookSpecificOutput":{"hookEventName":"PostToolUse","additionalContext":"[Swagger Hook] Controller file detected. Add SpringDoc annotations:\n- Class: @Tag(name=\"domain\", description=\"desc\")\n- Methods: @Operation(summary=\"\", description=\"\")\n- Responses: @ApiResponses({@ApiResponse(responseCode=\"200\", description=\"OK\")})\n- Import: io.swagger.v3.oas.annotations.*"}}
HOOKJSON
fi
