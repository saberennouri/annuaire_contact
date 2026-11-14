// Mock du back-end pour développer ou tester le front seul : node tools/mock-api.mjs [dossier-statique]
// Même contrat que l'API Spring Boot (lecture seule), alimenté par la graine JSON.
import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const here = path.dirname(fileURLToPath(import.meta.url));
const seed = JSON.parse(fs.readFileSync(path.join(here, '../backend/src/main/resources/seed/establishments.json'), 'utf8'));
const rows = seed.map((e, i) => ({ id: i + 1, ...e }));
const staticDir = process.argv[2] ? path.resolve(process.argv[2]) : null;

const norm = (s) => String(s ?? '').toLowerCase()
  .replace(/[ً-ٰٟـ]/g, '')
  .replace(/[أإآٱ]/g, 'ا').replace(/ى/g, 'ي').replace(/ة/g, 'ه').replace(/ؤ/g, 'و').replace(/ئ/g, 'ي')
  .replace(/\s+/g, ' ').trim();
rows.forEach((r) => {
  r.search = norm([r.name, r.delegation, r.email, r.address, r.note, ...r.phones,
    ...r.contacts.flatMap((c) => [c.fullName, ...c.phones])].filter(Boolean).join(' '));
});
const tokens = (q) => {
  const n = norm(q); if (!n) return [];
  return /^[\d\s]+$/.test(n) ? [n.replace(/\s/g, '')] : n.split(' ');
};
const strip = ({ search, ...e }) => e;
const match = (r, p, skip = {}) =>
  (!p.cats.length || p.cats.includes(r.category)) &&
  (skip.kind || !p.kind || r.kind === p.kind) &&
  (skip.deleg || !p.delegation || r.delegation === p.delegation) &&
  (!p.ids || p.ids.includes(r.id)) &&
  tokens(p.q).every((t) => r.search.includes(t));

const server = http.createServer((req, res) => {
  const u = new URL(req.url, 'http://x');
  const g = (k) => u.searchParams.get(k);
  const p = { cats: (g('category') || '').split(',').filter(Boolean), q: g('q') || '', kind: g('kind'), delegation: g('delegation'),
    ids: g('ids') ? g('ids').split(',').map(Number) : null };
  const json = (o, code = 200) => { res.writeHead(code, { 'content-type': 'application/json; charset=utf-8' }); res.end(JSON.stringify(o)); };
  if (u.pathname === '/api/establishments') {
    const page = +(g('page') || 0), size = Math.min(+(g('size') || 50), 200);
    const all = rows.filter((r) => match(r, p));
    const content = all.slice(page * size, page * size + size).map(strip);
    return json({ content, page, size, totalElements: all.length, totalPages: Math.ceil(all.length / size), hasNext: (page + 1) * size < all.length });
  }
  if (u.pathname === '/api/establishments/counts') {
    const out = { SECONDARY: 0, PRIMARY: 0, PRIVATE_PRIMARY: 0, PRESCHOOL: 0, INSPECTION: 0 };
    rows.filter((r) => match(r, { ...p, cats: [] })).forEach((r) => out[r.category]++);
    return json(out);
  }
  if (u.pathname === '/api/establishments/facets') {
    const kinds = {}, delegations = {};
    rows.filter((r) => match(r, p, { kind: true, deleg: true })).forEach((r) => {
      if (r.kind) kinds[r.kind] = (kinds[r.kind] || 0) + 1;
      if (r.delegation) delegations[r.delegation] = (delegations[r.delegation] || 0) + 1;
    });
    return json({ kinds, delegations });
  }
  if (staticDir) {
    let f = path.join(staticDir, u.pathname === '/' ? 'index.html' : u.pathname);
    if (!fs.existsSync(f) || fs.statSync(f).isDirectory()) f = path.join(staticDir, 'index.html');
    const types = { '.js': 'text/javascript', '.css': 'text/css', '.html': 'text/html', '.ico': 'image/x-icon' };
    res.writeHead(200, { 'content-type': types[path.extname(f)] || 'application/octet-stream' });
    return fs.createReadStream(f).pipe(res);
  }
  json({ error: 'not found' }, 404);
});
server.listen(+(process.env.PORT || 8080), () => console.log('mock API on :' + (process.env.PORT || 8080)));
