import { test, describe } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync, existsSync } from 'node:fs';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';
import { spawn } from 'node:child_process';

const __dirname = dirname(fileURLToPath(import.meta.url));
const rootDir = resolve(__dirname, '..');
const packageJsonPath = resolve(rootDir, 'package.json');

assert.ok(existsSync(packageJsonPath), 'root package.json must exist');
const pkg = JSON.parse(readFileSync(packageJsonPath, 'utf8'));

describe('Root Development Orchestrator', () => {
  test('package.json conforms to configuration schema and script contracts', () => {
    assert.equal(pkg.name, 'swe-workflow-playground');
    assert.equal(pkg.private, true);

    assert.ok(pkg.scripts, 'scripts object must exist');
    assert.equal(pkg.scripts['db:up'], 'docker compose up -d');
    assert.equal(pkg.scripts['db:down'], 'docker compose down');
    assert.equal(pkg.scripts['test:backend'], 'cd backend && ./gradlew test');
    assert.equal(pkg.scripts['test:frontend'], 'cd frontend && npm test -- --watch=false');
    assert.equal(pkg.scripts['test:e2e'], 'cd frontend && npm run e2e');
    assert.equal(pkg.scripts.test, 'node --test tests/dev-orchestrator.test.mjs && npm run test:backend && npm run test:frontend');

    assert.ok(pkg.devDependencies, 'devDependencies must exist');
    assert.match(pkg.devDependencies.concurrently, /^\^?\d+\.\d+\.\d+/, 'concurrently must be a valid SemVer');
    assert.match(pkg.devDependencies['wait-on'], /^\^?\d+\.\d+\.\d+/, 'wait-on must be a valid SemVer');
  });

  const devTargets = [
    { target: 'dev', expectedLocaleCommand: 'npm run start:ja' },
    { target: 'dev:ja', expectedLocaleCommand: 'npm run start:ja' },
    { target: 'dev:en', expectedLocaleCommand: 'npm run start:en' },
  ];

  for (const { target, expectedLocaleCommand } of devTargets) {
    test(`scripts.${target} orchestrates persistence, backend, and frontend (${expectedLocaleCommand})`, () => {
      const script = pkg.scripts[target];
      assert.ok(script, `scripts.${target} must be defined`);

      assert.match(script, /(npm run db:up|docker compose up -d)/, 'must start database persistence');
      assert.match(script, /concurrently/, 'must invoke concurrently');
      assert.match(script, /(-k|--kill-others)/, 'must configure kill-others flag');
      assert.match(script, /--kill-timeout\s+5000/, 'must configure 5-second kill timeout');
      assert.match(script, /(-p|--prefix)\s+["'][^"']+["']/, 'must configure output prefix template');
      assert.match(script, /(-n|--names)\s+["']backend,frontend["']/, 'must name child processes backend and frontend');
      assert.match(script, /(-c|--prefix-colors)\s+["']blue,green["']/, 'must configure distinct prefix colors');
      assert.match(script, /cd backend && \.\/gradlew run/, 'must execute backend server');
      assert.match(script, new RegExp(`cd frontend && ${expectedLocaleCommand}`), `must target ${expectedLocaleCommand}`);
    });
  }

  test('scripts.start boots database and multi-locale production distribution server', () => {
    const startScript = pkg.scripts.start;
    assert.ok(startScript, 'scripts.start must be defined');
    assert.match(startScript, /(npm run db:up|docker compose up -d)/);
    assert.match(startScript, /cd frontend && npm start/);
  });

  test('process lifecycle terminates cleanly on SIGINT signal and prevents orphaned child processes', async () => {
    const concurrentlyBin = resolve(rootDir, 'node_modules/.bin/concurrently');
    assert.ok(existsSync(concurrentlyBin), 'concurrently binary must be installed');

    const childPids = [];
    const child = spawn(
      concurrentlyBin,
      [
        '-k',
        '--kill-timeout',
        '5000',
        'node -e "console.log(\'PID:\' + process.pid); setInterval(() => {}, 1000)"',
        'node -e "console.log(\'PID:\' + process.pid); setInterval(() => {}, 1000)"',
      ],
      {
        cwd: rootDir,
        stdio: ['pipe', 'pipe', 'pipe'],
      }
    );

    try {
      child.stdout.on('data', (data) => {
        const matches = data.toString().matchAll(/PID:(\d+)/g);
        for (const match of matches) {
          childPids.push(Number(match[1]));
        }
      });

      // Allow spawned child processes to initialize and emit their PIDs
      await new Promise((resolveTimeout) => setTimeout(resolveTimeout, 1000));
      assert.equal(childPids.length, 2, 'Must have captured PIDs for both spawned child processes');

      const exitPromise = new Promise((resolveExit) => {
        child.on('exit', (code, signal) => resolveExit({ code, signal }));
      });

      child.kill('SIGINT');

      const result = await Promise.race([
        exitPromise,
        new Promise((_, reject) => setTimeout(() => reject(new Error('Process failed to terminate within 5s')), 5000)),
      ]);

      assert.ok(
        result.code === 0 || result.signal === 'SIGINT',
        `Expected exit code 0 or SIGINT, received code=${result.code}, signal=${result.signal}`
      );

      // Probe child PIDs via process.kill(pid, 0) to guarantee zero orphaned background processes
      for (const pid of childPids) {
        assert.throws(
          () => process.kill(pid, 0),
          { code: 'ESRCH' },
          `Child process PID ${pid} must be terminated and not left running as an orphan`
        );
      }
    } finally {
      if (!child.killed) {
        child.kill('SIGKILL');
      }
    }
  });
});
