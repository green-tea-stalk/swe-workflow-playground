import { test, describe } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync, existsSync } from 'node:fs';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const rootDir = resolve(__dirname, '..');
const prettierrcPath = resolve(rootDir, 'frontend/.prettierrc');
const prettierignorePath = resolve(rootDir, 'frontend/.prettierignore');
const packageJsonPath = resolve(rootDir, 'frontend/package.json');

/**
 * Validates frontend Prettier formatting configuration, ignore rules, and npm scripts.
 */
describe('Frontend Prettier Configuration and Automation', () => {
  /**
   * Verifies that .prettierrc exists and strictly conforms to the PrettierConfigModel specification.
   */
  test('frontend .prettierrc exists and conforms to standard configuration schema', () => {
    assert.ok(existsSync(prettierrcPath), 'frontend/.prettierrc must exist');
    const raw = readFileSync(prettierrcPath, 'utf8');
    const config = JSON.parse(raw);

    const expectedConfig = {
      tabWidth: 2,
      useTabs: false,
      singleQuote: true,
      semi: true,
      trailingComma: 'all',
      printWidth: 100,
      bracketSpacing: true,
      arrowParens: 'always',
    };

    assert.deepStrictEqual(
      config,
      expectedConfig,
      'frontend/.prettierrc must strictly match the expected configuration schema with no unexpected or missing keys',
    );
  });

  /**
   * Verifies that .prettierignore exists and strictly defines the required artifact exclusions.
   */
  test('frontend .prettierignore exists and contains required ignore patterns', () => {
    assert.ok(existsSync(prettierignorePath), 'frontend/.prettierignore must exist');
    const content = readFileSync(prettierignorePath, 'utf8');

    const activeLines = content
      .split(/\r?\n/)
      .map((line) => line.trim())
      .filter((line) => line.length > 0 && !line.startsWith('#'));

    const expectedPatterns = ['dist/', '.angular/', 'node_modules/', 'coverage/'];
    assert.deepStrictEqual(
      activeLines,
      expectedPatterns,
      'frontend/.prettierignore must define exactly the expected active ignore patterns',
    );
  });

  /**
   * Verifies that frontend package.json exposes format:check and format scripts with correct CLI invocations.
   */
  test('frontend package.json defines format:check and format lifecycle scripts', () => {
    assert.ok(existsSync(packageJsonPath), 'frontend/package.json must exist');
    const raw = readFileSync(packageJsonPath, 'utf8');
    const pkg = JSON.parse(raw);

    assert.ok(pkg.devDependencies, 'package.json must contain devDependencies block');
    assert.ok(pkg.devDependencies.prettier, 'package.json must define prettier in devDependencies');
    assert.match(
      pkg.devDependencies.prettier,
      /^\^?\d+\.\d+\.\d+/,
      'prettier dependency must conform to SemVer pattern',
    );

    assert.ok(pkg.scripts, 'package.json must contain scripts block');
    assert.strictEqual(
      pkg.scripts['format:check'],
      'prettier --check .',
      'scripts["format:check"] must be "prettier --check ."',
    );
    assert.strictEqual(
      pkg.scripts.format,
      'prettier --write .',
      'scripts["format"] must be "prettier --write ."',
    );
  });
});
