import { test, describe } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync, existsSync } from 'node:fs';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';
import { parse as parseYaml } from 'yaml';

const __dirname = dirname(fileURLToPath(import.meta.url));
const rootDir = resolve(__dirname, '..');
const ciWorkflowPath = resolve(rootDir, '.github/workflows/ci.yml');

describe('Continuous Integration Workflow Engine', () => {
  assert.ok(existsSync(ciWorkflowPath), '.github/workflows/ci.yml must exist');
  const content = readFileSync(ciWorkflowPath, 'utf8');
  const workflow = parseYaml(content);

  test('ci workflow file exists and defines canonical trigger, permissions, concurrency, and job topology', () => {
    assert.equal(workflow.name, 'CI', 'Workflow name must be CI');
    assert.equal(workflow.on?.pull_request?.branches, undefined, 'Must trigger on pull_request to any target branch');
    assert.deepEqual(
      workflow.on?.pull_request?.types,
      ['opened', 'synchronize'],
      'Must trigger strictly on opened and synchronize events'
    );
    assert.equal(workflow.permissions?.contents, 'read', 'Workflow must enforce read-only contents permission');

    assert.equal(workflow.concurrency?.['cancel-in-progress'], true, 'Must cancel redundant in-progress runs');
    assert.equal(
      workflow.concurrency?.group,
      '${{ github.workflow }}-${{ github.ref }}',
      'Concurrency group must strictly partition by workflow and ref'
    );

    assert.deepEqual(
      Object.keys(workflow.jobs ?? {}).sort(),
      ['backend', 'e2e', 'frontend'],
      'Workflow must define exactly backend, frontend, and e2e jobs'
    );
  });

  const parallelJobs = [
    {
      name: 'backend',
      runner: 'ubuntu-latest',
      setupAction: 'actions/setup-java@v6',
      setupWith: { distribution: 'corretto', 'java-version': '25', cache: 'gradle' },
      expectedCommands: ['docker compose up -d --wait', './gradlew test'],
    },
    {
      name: 'frontend',
      runner: 'ubuntu-latest',
      setupAction: 'actions/setup-node@v7',
      setupWith: {
        'node-version': 22,
        cache: 'npm',
        'cache-dependency-path': 'frontend/package-lock.json',
      },
      expectedCommands: ['npm ci', 'npm test -- --watch=false', 'npm run build'],
    },
  ];

  for (const jobConfig of parallelJobs) {
    test(`${jobConfig.name} verification job configures runtime and executes required steps concurrently`, () => {
      const job = workflow.jobs?.[jobConfig.name];
      assert.ok(job, `Workflow must define ${jobConfig.name} job`);
      assert.equal(job['runs-on'], jobConfig.runner, `${jobConfig.name} job must run on ${jobConfig.runner}`);
      assert.equal(job.needs, undefined, `${jobConfig.name} job must run concurrently without upstream dependencies`);

      const steps = job.steps ?? [];
      const checkoutStep = steps.find((s) => s.uses === 'actions/checkout@v7');
      assert.ok(checkoutStep, `${jobConfig.name} job must check out repository via actions/checkout@v7`);

      const runtimeStep = steps.find((s) => s.uses === jobConfig.setupAction);
      assert.ok(runtimeStep, `${jobConfig.name} job must setup runtime via ${jobConfig.setupAction}`);
      for (const [key, expectedValue] of Object.entries(jobConfig.setupWith)) {
        assert.equal(
          String(runtimeStep.with?.[key]),
          String(expectedValue),
          `${jobConfig.name} setup step must configure with.${key} = ${expectedValue}`
        );
      }

      let lastIndex = -1;
      for (const cmd of jobConfig.expectedCommands) {
        const stepIndex = steps.findIndex((s) => s.run?.includes(cmd));
        assert.ok(stepIndex !== -1, `${jobConfig.name} job must execute command matching "${cmd}"`);
        assert.ok(
          stepIndex > lastIndex,
          `${jobConfig.name} step executing "${cmd}" must execute after preceding step`
        );
        lastIndex = stepIndex;
      }
    });
  }

  test('e2e browser job strictly gates downstream execution on backend and frontend completion', () => {
    const e2eJob = workflow.jobs?.e2e;
    assert.ok(e2eJob, 'Workflow must define e2e job');
    assert.equal(e2eJob['runs-on'], 'ubuntu-latest', 'E2E job must run on ubuntu-latest');
    assert.deepEqual(e2eJob.needs, ['backend', 'frontend'], 'E2E job must strictly depend on backend and frontend jobs');

    const steps = e2eJob.steps ?? [];

    const checkoutStep = steps.find((s) => s.uses === 'actions/checkout@v7');
    assert.ok(checkoutStep, 'E2E job must check out repository via actions/checkout@v7');

    const javaStep = steps.find((s) => s.uses === 'actions/setup-java@v6');
    assert.ok(javaStep, 'E2E job must configure Java runtime via actions/setup-java@v6');
    assert.equal(javaStep.with?.distribution, 'corretto');
    assert.equal(String(javaStep.with?.['java-version']), '25');
    assert.equal(javaStep.with?.cache, 'gradle');

    const nodeStep = steps.find((s) => s.uses === 'actions/setup-node@v7');
    assert.ok(nodeStep, 'E2E job must configure Node runtime via actions/setup-node@v7');
    assert.equal(String(nodeStep.with?.['node-version']), '22');
    assert.equal(nodeStep.with?.cache, 'npm');
    assert.equal(nodeStep.with?.['cache-dependency-path'], 'frontend/package-lock.json');

    const mysqlStep = steps.find((s) => s.run?.includes('docker compose up -d --wait'));
    assert.ok(mysqlStep, 'E2E job must start MySQL persistence via docker compose up -d --wait');

    const frontendInstallStep = steps.find((s) => s.run?.includes('npm ci'));
    assert.ok(frontendInstallStep, 'E2E job must install frontend dependencies');

    const backendBgStep = steps.find(
      (s) => s.run?.includes('./gradlew run > backend.log') && s.run?.includes('backend.pid')
    );
    assert.ok(backendBgStep, 'E2E job must launch backend service in background');

    const frontendBgStep = steps.find(
      (s) => s.run?.includes('npm start > frontend.log') && s.run?.includes('frontend.pid')
    );
    assert.ok(frontendBgStep, 'E2E job must launch frontend service in background');

    const readinessStep = steps.find(
      (s) =>
        s.run?.includes('http://localhost:8080/api/posts') &&
        s.run?.includes('http://localhost:4200/') &&
        s.run?.includes('--retry 30') &&
        s.run?.includes('--retry-delay 2')
    );
    assert.ok(readinessStep, 'E2E job must poll backend and frontend readiness endpoints with 60s retry loop');

    const playwrightInstallStep = steps.find((s) => s.run?.includes('playwright install --with-deps chromium'));
    assert.ok(playwrightInstallStep, 'E2E job must install Playwright Chromium dependencies');

    const playwrightRunStep = steps.find((s) => s.run?.includes('playwright test'));
    assert.ok(playwrightRunStep, 'E2E job must execute Playwright test suite');

    const artifactStep = steps.find((s) => s.uses === 'actions/upload-artifact@v7');
    assert.ok(artifactStep, 'E2E job must include artifact upload step using actions/upload-artifact@v7');
    assert.equal(artifactStep.if, 'failure()', 'Artifact upload must execute only on failure');
    assert.equal(artifactStep.with?.['retention-days'], 14, 'Artifact retention must be set to 14 days');
    assert.equal(artifactStep.with?.name, 'playwright-failure-diagnostics', 'Artifact name must match diagnostics');

    const artifactPaths = artifactStep.with?.path ?? '';
    assert.ok(artifactPaths.includes('frontend/test-results/'), 'Must upload Playwright test results');
    assert.ok(artifactPaths.includes('frontend/playwright-report/'), 'Must upload Playwright report');
    assert.ok(artifactPaths.includes('backend/backend.log'), 'Must upload backend service log');
    assert.ok(artifactPaths.includes('frontend/frontend.log'), 'Must upload frontend service log');
  });
});
