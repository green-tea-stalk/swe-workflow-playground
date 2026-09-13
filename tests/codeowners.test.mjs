import { test, describe } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync, existsSync } from 'node:fs';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const rootDir = resolve(__dirname, '..');
const codeownersPath = resolve(rootDir, '.github/CODEOWNERS');

/**
 * Validates repository code ownership governance and reviewer routing.
 */
describe('Repository Code Ownership Governance', () => {
  /**
   * Verifies caller precondition that canonical CODEOWNERS file exists at repository root.
   */
  test('canonical CODEOWNERS file exists under .github directory', () => {
    assert.ok(existsSync(codeownersPath), '.github/CODEOWNERS file must exist at repository root');
  });

  /**
   * Verifies postcondition that repository-wide wildcard rule strictly assigns review responsibility
   * to @green-tea-stalk with zero orphan paths and no extraneous active rules.
   */
  test('CODEOWNERS configures default review ownership for all repository files', () => {
    assert.ok(existsSync(codeownersPath), '.github/CODEOWNERS must exist before parsing');
    const content = readFileSync(codeownersPath, 'utf8');

    const rules = content
      .split('\n')
      .map((line) => line.trim())
      .filter((line) => line.length > 0 && !line.startsWith('#'));

    assert.equal(rules.length, 1, 'CODEOWNERS must contain exactly one active governance rule');

    const parts = rules[0].split(/\s+/);
    assert.deepEqual(
      parts,
      ['*', '@green-tea-stalk'],
      'Active rule must strictly map pattern "*" to "@green-tea-stalk"',
    );
  });
});
