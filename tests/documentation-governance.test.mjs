import { test, describe } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync, existsSync } from 'node:fs';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const rootDir = resolve(__dirname, '..');
const licensePath = resolve(rootDir, 'LICENSE');
const readmePath = resolve(rootDir, 'README.md');
const readmeJaPath = resolve(rootDir, 'README.ja.md');
const agentsPath = resolve(rootDir, 'AGENTS.md');

describe('Documentation Governance & Badges Presentation', () => {
  test('canonical MIT LICENSE file exists at repository root', () => {
    assert.ok(existsSync(licensePath), 'LICENSE file must exist at repository root');
    const content = readFileSync(licensePath, 'utf8');
    assert.match(content, /MIT License/, 'LICENSE must declare MIT License');
    assert.match(content, /2026/, 'LICENSE must specify year 2026');
    assert.match(content, /green-tea-stalk/, 'LICENSE must specify copyright holder');
  });

  const badgeOrder = [
    {
      type: 'release',
      pattern: /\[!\[GitHub Release\]\(https:\/\/img\.shields\.io\/github\/v\/release\/green-tea-stalk\/swe-workflow-playground\)\]\(https:\/\/github\.com\/green-tea-stalk\/swe-workflow-playground\/releases\)/,
    },
    {
      type: 'ci',
      pattern: /\[!\[CI\]\(https:\/\/github\.com\/green-tea-stalk\/swe-workflow-playground\/actions\/workflows\/ci\.yml\/badge\.svg\)\]\(https:\/\/github\.com\/green-tea-stalk\/swe-workflow-playground\/actions\/workflows\/ci\.yml\)/,
    },
    {
      type: 'release-please',
      pattern: /\[!\[release-please\]\(https:\/\/github\.com\/green-tea-stalk\/swe-workflow-playground\/actions\/workflows\/release-please\.yml\/badge\.svg\)\]\(https:\/\/github\.com\/green-tea-stalk\/swe-workflow-playground\/actions\/workflows\/release-please\.yml\)/,
    },
    {
      type: 'dependabot',
      pattern: /\[!\[Dependabot\]\(https:\/\/img\.shields\.io\/badge\/dependabot-enabled-blue\.svg\?logo=dependabot\)\]\(https:\/\/github\.com\/green-tea-stalk\/swe-workflow-playground\/network\/updates\)/,
    },
    {
      type: 'license',
      pattern: /\[!\[License: MIT\]\(https:\/\/img\.shields\.io\/badge\/License-MIT-yellow\.svg\)\]\(LICENSE\)/,
    },
  ];

  const docFiles = [
    { name: 'README.md', path: readmePath },
    { name: 'README.ja.md', path: readmeJaPath },
  ];

  for (const { name, path } of docFiles) {
    test(`${name} contains all 5 status badges with valid links in strict sequential order`, () => {
      assert.ok(existsSync(path), `${name} must exist`);
      const content = readFileSync(path, 'utf8');

      let lastIndex = -1;
      for (const badge of badgeOrder) {
        const match = content.match(badge.pattern);
        assert.ok(match, `${name} must contain full markdown syntax for ${badge.type} badge with valid target link`);
        assert.ok(
          match.index > lastIndex,
          `${name}: ${badge.type} badge at index ${match.index} must appear after preceding badge at index ${lastIndex}`
        );
        lastIndex = match.index;
      }
    });

    test(`${name} contains zero broken relative links or anchor references`, () => {
      assert.ok(existsSync(path), `${name} must exist`);
      const content = readFileSync(path, 'utf8');

      const linkRegex = /\[([^\]]+)\]\(([^)]+)\)/g;
      let match;
      const links = [];
      while ((match = linkRegex.exec(content)) !== null) {
        links.push({ text: match[1], target: match[2] });
      }

      const headingToSlug = (heading) =>
        heading
          .toLowerCase()
          .replace(/[^\p{L}\p{N}\s-]/gu, '')
          .trim()
          .replace(/ /g, '-');

      for (const { target } of links) {
        if (target.startsWith('http://') || target.startsWith('https://')) {
          continue;
        }

        const [filePath, anchor] = target.split('#');
        const resolvedPath = filePath ? resolve(rootDir, filePath) : path;
        assert.ok(existsSync(resolvedPath), `${name} links to non-existent file: ${filePath}`);

        if (anchor) {
          const targetContent = readFileSync(resolvedPath, 'utf8');
          const headings = [...targetContent.matchAll(/^#{1,6}\s+(.+)$/gm)].map((m) => headingToSlug(m[1]));
          assert.ok(
            headings.includes(anchor),
            `${name} references non-existent anchor #${anchor} in ${filePath || name}`
          );
        }
      }
    });
  }

  test('AGENTS.md documents consolidated npm run dev in Quick Start guide', () => {
    assert.ok(existsSync(agentsPath), 'AGENTS.md must exist');
    const content = readFileSync(agentsPath, 'utf8');
    assert.match(content, /npm run dev/, 'AGENTS.md must document npm run dev consolidated command');
  });

  test('AGENTS.md strictly preserves Unidirectional Reference Rule (never references README)', () => {
    assert.ok(existsSync(agentsPath), 'AGENTS.md must exist');
    const content = readFileSync(agentsPath, 'utf8');
    assert.doesNotMatch(content, /README\.md/i, 'AGENTS.md must not reference README.md');
    assert.doesNotMatch(content, /README\.ja\.md/i, 'AGENTS.md must not reference README.ja.md');
  });
});
