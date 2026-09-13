import { test, describe } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync, existsSync } from 'node:fs';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';
import { parse as parseYaml } from 'yaml';

const __dirname = dirname(fileURLToPath(import.meta.url));
const rootDir = resolve(__dirname, '..');

const dependabotPath = resolve(rootDir, '.github/dependabot.yml');
const releasePleaseWorkflowPath = resolve(rootDir, '.github/workflows/release-please.yml');
const releasePleaseConfigPath = resolve(rootDir, '.github/release-please-config.json');
const releasePleaseManifestPath = resolve(rootDir, '.release-please-manifest.json');

describe('Multi-Ecosystem Dependabot Automation', () => {
  assert.ok(existsSync(dependabotPath), '.github/dependabot.yml must exist');
  const dependabotContent = readFileSync(dependabotPath, 'utf8');
  const dependabotConfig = parseYaml(dependabotContent);

  test('dependabot configuration file exists and conforms to v2 schema', () => {
    assert.equal(dependabotConfig.version, 2, 'Dependabot configuration version must be 2');
    assert.ok(Array.isArray(dependabotConfig.updates), 'updates must be an array');
    assert.equal(dependabotConfig.updates.length, 4, 'Must configure exactly 4 package ecosystems');
  });

  const expectedEcosystems = [
    { ecosystem: 'gradle', directory: '/backend' },
    { ecosystem: 'npm', directory: '/frontend' },
    { ecosystem: 'github-actions', directory: '/' },
    { ecosystem: 'docker', directory: '/' },
  ];

  for (const { ecosystem, directory } of expectedEcosystems) {
    test(`dependabot monitors ${ecosystem} ecosystem in ${directory} on weekly Monday schedule`, () => {
      const update = dependabotConfig.updates?.find(
        (u) => u['package-ecosystem'] === ecosystem && u.directory === directory
      );
      assert.ok(update, `Must contain update entry for ${ecosystem} in ${directory}`);
      assert.equal(update.schedule?.interval, 'weekly', `${ecosystem} schedule interval must be weekly`);
      assert.equal(update.schedule?.day, 'monday', `${ecosystem} schedule day must be monday`);
    });
  }
});

describe('Semantic Release Please Automation', () => {
  test('release-please workflow file exists with required permissions and trigger', () => {
    assert.ok(existsSync(releasePleaseWorkflowPath), '.github/workflows/release-please.yml must exist');
    const content = readFileSync(releasePleaseWorkflowPath, 'utf8');
    const workflow = parseYaml(content);

    assert.equal(workflow.name, 'release-please');
    assert.deepEqual(workflow.on?.push?.branches, ['main'], 'Workflow must trigger on push to main branch');
    assert.equal(workflow.permissions?.contents, 'write', 'Workflow must have contents: write permission');
    assert.equal(workflow.permissions?.['pull-requests'], 'write', 'Workflow must have pull-requests: write permission');

    const job = workflow.jobs?.['release-please'];
    assert.ok(job, 'Workflow must define release-please job');
    assert.equal(job['runs-on'], 'ubuntu-latest', 'Job must run on ubuntu-latest');

    const actionStep = job.steps?.find((s) => s.uses?.startsWith('googleapis/release-please-action'));
    assert.ok(actionStep, 'Job must include googleapis/release-please-action step');
    assert.equal(actionStep.uses, 'googleapis/release-please-action@v4', 'Workflow step must use exact release-please-action v4 reference');
    assert.equal(actionStep.with?.['config-file'], '.github/release-please-config.json', 'Step must configure config-file parameter');
    assert.equal(actionStep.with?.['manifest-file'], '.release-please-manifest.json', 'Step must configure manifest-file parameter');
  });

  test('release-please configuration defines simple release type for root package', () => {
    assert.ok(existsSync(releasePleaseConfigPath), '.github/release-please-config.json must exist');
    const content = readFileSync(releasePleaseConfigPath, 'utf8');
    const config = JSON.parse(content);

    assert.equal(config['release-type'], 'simple', 'Must use simple release type');
    assert.deepEqual(config.packages, { '.': {} }, 'packages mapping must configure root package "." with empty options object');
  });

  test('release-please manifest defines valid SemVer milestone for root package', () => {
    assert.ok(existsSync(releasePleaseManifestPath), '.release-please-manifest.json must exist');
    const content = readFileSync(releasePleaseManifestPath, 'utf8');
    const manifest = JSON.parse(content);

    const version = manifest['.'];
    assert.ok(version, 'root package version must exist in manifest');
    assert.match(version, /^[0-9]+\.[0-9]+\.[0-9]+$/, 'Manifest version must be a valid SemVer milestone');
    assert.equal(version, '0.1.0', 'Initial manifest version milestone must be 0.1.0');
  });
});
