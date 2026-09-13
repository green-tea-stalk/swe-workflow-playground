import { test, describe } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync, existsSync } from 'node:fs';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const rootDir = resolve(__dirname, '..');
const catalogPath = resolve(rootDir, 'backend/gradle/libs.versions.toml');
const buildGradlePath = resolve(rootDir, 'backend/build.gradle.kts');

/**
 * Validates backend Gradle Version Catalog structure, dependency centralization, and build script migration.
 */
describe('Backend Gradle Version Catalog Governance', () => {
  /**
   * Verifies caller precondition that canonical libs.versions.toml exists under backend/gradle/.
   */
  test('libs.versions.toml exists under backend/gradle directory', () => {
    assert.ok(existsSync(catalogPath), 'backend/gradle/libs.versions.toml must exist');
  });

  /**
   * Verifies that the catalog defines versions conforming to SemVer schema and excludes platform runtimes.
   */
  test('libs.versions.toml contains required SemVer versions and excludes platform environment versions', () => {
    assert.ok(existsSync(catalogPath), 'libs.versions.toml must exist before reading');
    const content = readFileSync(catalogPath, 'utf8');

    assert.match(content, /\[versions\]/, 'Catalog must contain [versions] section');
    assert.match(content, /\[libraries\]/, 'Catalog must contain [libraries] section');
    assert.match(content, /\[plugins\]/, 'Catalog must contain [plugins] section');

    const semVerPattern = /^[0-9]+\.[0-9]+\.[0-9]+$/;
    const requiredVersions = ['micronaut', 'shadow', 'spotless', 'mockito', 'testcontainers'];
    for (const key of requiredVersions) {
      const versionMatch = content.match(new RegExp(`^${key}\\s*=\\s*"([^"]+)"`, 'm'));
      assert.ok(versionMatch, `Catalog must define version for ${key}`);
      assert.match(versionMatch[1], semVerPattern, `Version for ${key} must conform to SemVer pattern`);
    }

    assert.doesNotMatch(content, /java\s*=\s*"25"/i, 'Java 25 runtime must not be managed in version catalog');
    assert.doesNotMatch(content, /mysql\s*=\s*"8\.4"/i, 'MySQL container version must not be managed in version catalog');
    assert.doesNotMatch(content, /docker\s*=\s*/i, 'Docker runtime must not be managed in version catalog');
  });

  /**
   * Verifies that required backend plugins are cataloged with correct coordinates and version references.
   */
  test('libs.versions.toml configures required plugins with id and version references', () => {
    assert.ok(existsSync(catalogPath), 'libs.versions.toml must exist before reading');
    const content = readFileSync(catalogPath, 'utf8');

    const expectedPlugins = [
      { alias: 'micronaut-application', id: 'io.micronaut.application', versionRef: 'micronaut' },
      { alias: 'shadow', id: 'com.gradleup.shadow', versionRef: 'shadow' },
      { alias: 'spotless', id: 'com.diffplug.spotless', versionRef: 'spotless' },
      { alias: 'micronaut-aot', id: 'io.micronaut.aot', versionRef: 'micronaut' },
    ];

    for (const { alias, id, versionRef } of expectedPlugins) {
      const pluginRegex = new RegExp(
        `^${alias}\\s*=\\s*\\{\\s*id\\s*=\\s*"${id.replace(/\./g, '\\.')}"\\s*,\\s*version\\.ref\\s*=\\s*"${versionRef}"\\s*\\}`,
        'm',
      );
      assert.match(content, pluginRegex, `Plugin ${alias} must be defined with id "${id}" and version.ref "${versionRef}"`);
    }
  });

  /**
   * Verifies that build.gradle.kts references dependencies and plugins exclusively via libs accessors.
   */
  test('backend build.gradle.kts migrates plugins and dependencies to libs accessors without inline coordinates', () => {
    assert.ok(existsSync(buildGradlePath), 'backend/build.gradle.kts must exist');
    const content = readFileSync(buildGradlePath, 'utf8');

    const requiredPluginAccessors = [
      'libs.plugins.micronaut.application',
      'libs.plugins.shadow',
      'libs.plugins.spotless',
      'libs.plugins.micronaut.aot',
    ];
    for (const pluginAccessor of requiredPluginAccessors) {
      const accessorPattern = new RegExp(`alias\\(${pluginAccessor.replace(/\./g, '\\.')}\\)`);
      assert.match(content, accessorPattern, `build.gradle.kts must apply ${pluginAccessor} via alias()`);
    }

    assert.doesNotMatch(
      content,
      /(?:implementation|annotationProcessor|compileOnly|runtimeOnly|testImplementation|testRuntimeOnly)\s*\(\s*["']/,
      'Dependencies block must not contain inline string coordinates',
    );

    const requiredLibPrefixes = ['libs.micronaut.', 'libs.mockito.', 'libs.testcontainers.'];
    for (const prefix of requiredLibPrefixes) {
      assert.match(content, new RegExp(prefix.replace(/\./g, '\\.')), `build.gradle.kts must reference ${prefix} accessors`);
    }
  });

  /**
   * Verifies that Spotless configuration in build.gradle.kts configures Palantir Java Format and ktlint.
   */
  test('backend build.gradle.kts configures Spotless with Palantir Java Format and ktlint', () => {
    assert.ok(existsSync(buildGradlePath), 'backend/build.gradle.kts must exist');
    const content = readFileSync(buildGradlePath, 'utf8');

    assert.match(content, /spotless\s*\{/, 'build.gradle.kts must contain spotless block');
    assert.match(
      content,
      /java\s*\{[\s\S]*?target\("src\/\*\*\/\*\.java"\)[\s\S]*?palantirJavaFormat\(/,
      'Spotless java block must target "src/**/*.java" and configure palantirJavaFormat',
    );
    assert.match(
      content,
      /kotlinGradle\s*\{[\s\S]*?target\("\*\.gradle\.kts"\)[\s\S]*?ktlint\(/,
      'Spotless kotlinGradle block must target "*.gradle.kts" and configure ktlint',
    );
  });
});

