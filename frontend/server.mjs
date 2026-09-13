import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';
import { pipeline } from 'node:stream';
import { fileURLToPath } from 'node:url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const DIST_DIR = path.resolve(__dirname, 'dist/frontend/browser');
const BACKEND_HOST = process.env['BACKEND_HOST'] || '127.0.0.1';
const BACKEND_PORT = Number(process.env['BACKEND_PORT']) || 8080;
const PORT = Number(process.env['PORT']) || 4200;

const MIME_TYPES = {
  '.html': 'text/html; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8',
  '.mjs': 'application/javascript; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.gif': 'image/gif',
  '.svg': 'image/svg+xml',
  '.ico': 'image/x-icon',
  '.woff': 'font/woff',
  '.woff2': 'font/woff2',
  '.ttf': 'font/ttf',
  '.txt': 'text/plain; charset=utf-8',
};

const HOP_BY_HOP_HEADERS = new Set([
  'connection',
  'keep-alive',
  'proxy-authenticate',
  'proxy-authorization',
  'te',
  'trailer',
  'transfer-encoding',
  'upgrade',
]);

const SECURITY_HEADERS = {
  'X-Content-Type-Options': 'nosniff',
  'X-Frame-Options': 'SAMEORIGIN',
  'Referrer-Policy': 'strict-origin-when-cross-origin',
};

/**
 * Validates that a requested file path does not escape the designated base directory (prevents path traversal and null byte crashes).
 *
 * @param basePath designated safe root directory
 * @param targetPath resolved target file path
 * @returns true if targetPath is safely inside basePath without null bytes
 */
function isSafePath(basePath, targetPath) {
  if (!targetPath || targetPath.includes('\0')) {
    return false;
  }
  const rel = path.relative(basePath, targetPath);
  return !rel.startsWith('..') && !path.isAbsolute(rel);
}

/**
 * Canonicalizes real filesystem paths to ensure symlinks cannot escape the designated base directory.
 *
 * @param basePath designated safe root directory
 * @param targetPath resolved target file path
 * @returns true if the canonical filesystem realpath remains inside the canonical base directory
 */
function isSafeRealPath(basePath, targetPath) {
  if (!isSafePath(basePath, targetPath)) {
    return false;
  }
  try {
    const canonicalBase = fs.realpathSync(basePath);
    const canonicalTarget = fs.realpathSync(targetPath);
    const rel = path.relative(canonicalBase, canonicalTarget);
    return !rel.startsWith('..') && !path.isAbsolute(rel);
  } catch {
    return false;
  }
}

/**
 * Safely streams a file to the client using stream pipeline, capturing stream errors without crashing the process.
 *
 * @param filePath absolute path of file to serve
 * @param contentType HTTP content type header value
 * @param cacheControl HTTP cache control header value
 * @param res HTTP server response instance
 */
function serveStream(filePath, contentType, cacheControl, res) {
  res.writeHead(200, {
    ...SECURITY_HEADERS,
    'Content-Type': contentType,
    'Cache-Control': cacheControl,
  });

  pipeline(fs.createReadStream(filePath), res, (err) => {
    if (err) {
      console.error(`[i18n-server] Stream error serving "${filePath}":`, err.message);
      if (!res.headersSent) {
        res.writeHead(500, {
          ...SECURITY_HEADERS,
          'Content-Type': 'text/plain; charset=utf-8',
        });
        res.end('500 Internal Server Error');
      } else if (!res.writableEnded) {
        res.destroy();
      }
    }
  });
}

/**
 * Lightweight multi-locale static file server with Micronaut REST API reverse proxy.
 * Serves compiled Ahead-of-Time distributions for '/ja/' and '/en/' with SPA routing fallback.
 */
const server = http.createServer((req, res) => {
  let parsedUrl;
  try {
    parsedUrl = new URL(req.url || '/', 'http://127.0.0.1');
  } catch {
    res.writeHead(400, {
      ...SECURITY_HEADERS,
      'Content-Type': 'text/plain; charset=utf-8',
    });
    res.end('400 Bad Request: Malformed URI');
    return;
  }

  const pathname = parsedUrl.pathname;

  // 1. Reverse proxy for backend REST API requests
  if (pathname.startsWith('/api/')) {
    const sanitizedHeaders = {};
    for (const [headerKey, headerVal] of Object.entries(req.headers)) {
      if (!HOP_BY_HOP_HEADERS.has(headerKey.toLowerCase())) {
        sanitizedHeaders[headerKey] = headerVal;
      }
    }
    sanitizedHeaders['host'] = `${BACKEND_HOST}:${BACKEND_PORT}`;

    const proxyReq = http.request(
      {
        host: BACKEND_HOST,
        port: BACKEND_PORT,
        path: parsedUrl.pathname + parsedUrl.search,
        method: req.method,
        headers: sanitizedHeaders,
      },
      (proxyRes) => {
        res.writeHead(proxyRes.statusCode || 500, proxyRes.headers);
        proxyRes.pipe(res, { end: true });
      }
    );

    proxyReq.on('error', (err) => {
      console.error(`[i18n-server] Proxy connection error (${BACKEND_HOST}:${BACKEND_PORT}):`, err.message);
      if (!res.headersSent) {
        res.writeHead(502, {
          ...SECURITY_HEADERS,
          'Content-Type': 'text/plain; charset=utf-8',
        });
        res.end('502 Bad Gateway: Backend service unavailable');
      } else if (!res.writableEnded) {
        res.destroy();
      }
    });

    req.pipe(proxyReq, { end: true });
    return;
  }

  // 2. Canonical redirect for bare locale paths without trailing slashes
  if (pathname === '/ja' || pathname === '/en') {
    res.writeHead(301, {
      ...SECURITY_HEADERS,
      Location: `${pathname}/`,
    });
    res.end();
    return;
  }

  // 3. Initial root access: lightweight bootstrap router redirecting based on stored locale preference or browser language
  if (pathname === '/' || pathname === '' || pathname === '/index.html') {
    res.writeHead(200, {
      ...SECURITY_HEADERS,
      'Content-Type': 'text/html; charset=utf-8',
      'Cache-Control': 'no-cache, no-store, must-revalidate',
    });
    res.end(`<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <title>Bulletin Board</title>
  <script>
    (function() {
      try {
        var stored = window.localStorage.getItem('bb_locale');
        if (stored === 'ja' || stored === 'en') {
          window.location.replace('/' + stored + '/');
          return;
        }
      } catch (e) {}
      var lang = (navigator.language || (navigator.languages && navigator.languages[0]) || '');
      lang = lang.toLowerCase();
      if (lang.startsWith('ja')) {
        window.location.replace('/ja/');
      } else {
        window.location.replace('/en/');
      }
    })();
  </script>
  <noscript>
    <meta http-equiv="refresh" content="0; url=/en/">
  </noscript>
</head>
<body></body>
</html>`);
    return;
  }

  // 4. Resolve target locale distribution directory
  let locale = 'en';
  let subPath = pathname;
  if (pathname.startsWith('/ja/')) {
    locale = 'ja';
    subPath = pathname.slice(3) || '/';
  } else if (pathname.startsWith('/en/')) {
    locale = 'en';
    subPath = pathname.slice(3) || '/';
  }

  const localeDir = path.resolve(DIST_DIR, locale);
  let resolvedFilePath;
  try {
    resolvedFilePath = path.resolve(localeDir, '.' + subPath);
  } catch {
    res.writeHead(400, {
      ...SECURITY_HEADERS,
      'Content-Type': 'text/plain; charset=utf-8',
    });
    res.end('400 Bad Request');
    return;
  }

  // Path traversal defense
  if (!isSafePath(localeDir, resolvedFilePath)) {
    res.writeHead(403, {
      ...SECURITY_HEADERS,
      'Content-Type': 'text/plain; charset=utf-8',
    });
    res.end('403 Forbidden');
    return;
  }

  // If path points to directory, look for index.html
  try {
    if (fs.existsSync(resolvedFilePath) && fs.statSync(resolvedFilePath).isDirectory()) {
      resolvedFilePath = path.join(resolvedFilePath, 'index.html');
    }
  } catch {
    // Stat error ignored; fall through to 404
  }

  // Serve static asset if file exists and remains within web root via realpath
  try {
    if (fs.existsSync(resolvedFilePath) && fs.statSync(resolvedFilePath).isFile()) {
      if (!isSafeRealPath(localeDir, resolvedFilePath)) {
        res.writeHead(403, {
          ...SECURITY_HEADERS,
          'Content-Type': 'text/plain; charset=utf-8',
        });
        res.end('403 Forbidden');
        return;
      }
      const ext = path.extname(resolvedFilePath).toLowerCase();
      const contentType = MIME_TYPES[ext] || 'application/octet-stream';
      const cacheControl = ext === '.html' ? 'no-cache' : 'public, max-age=31536000, immutable';
      serveStream(resolvedFilePath, contentType, cacheControl, res);
      return;
    }
  } catch (err) {
    console.error(`[i18n-server] File stat error for "${resolvedFilePath}":`, err.message);
  }

  // Check if requested target is a static file asset rather than an SPA route
  const ext = path.extname(resolvedFilePath).toLowerCase();
  if (ext && ext !== '.html') {
    res.writeHead(404, {
      ...SECURITY_HEADERS,
      'Content-Type': 'text/plain; charset=utf-8',
    });
    res.end(`404 Not Found: Asset "${pathname}" does not exist.`);
    return;
  }

  // Single Page Application (SPA) client-side routing fallback
  const fallbackIndex = path.join(localeDir, 'index.html');
  if (fs.existsSync(fallbackIndex)) {
    serveStream(fallbackIndex, 'text/html; charset=utf-8', 'no-cache', res);
    return;
  }

  res.writeHead(404, {
    ...SECURITY_HEADERS,
    'Content-Type': 'text/plain; charset=utf-8',
  });
  res.end('404 Not Found: Distribution files not built. Please run "npm run build" first.');
});

server.listen(PORT, () => {
  console.log(`[i18n-server] Serving multi-locale distribution at http://localhost:${PORT}/ (ja & en)`);
});
