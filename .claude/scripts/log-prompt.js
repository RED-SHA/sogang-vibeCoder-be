#!/usr/bin/env node
const fs = require('fs');
const path = require('path');

let raw = '';
process.stdin.on('data', (chunk) => { raw += chunk; });
process.stdin.on('end', () => {
  try {
    const data = JSON.parse(raw || '{}');
    const prompt = (data.prompt || '').toString();
    if (!prompt.trim()) return;

    const cwd = data.cwd || process.cwd();
    const logPath = path.join(cwd, '.claude', 'conversation-log.md');
    fs.mkdirSync(path.dirname(logPath), { recursive: true });

    const d = new Date();
    const pad = (n) => String(n).padStart(2, '0');
    const ts = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
    const session = data.session_id ? ` (session: ${data.session_id.slice(0, 8)})` : '';
    const entry = `\n## ${ts}${session}\n\n${prompt}\n`;

    fs.appendFileSync(logPath, entry, 'utf8');
  } catch (e) {
    process.stderr.write(`[log-prompt] ${e.message}\n`);
  }
});
