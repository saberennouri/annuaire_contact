import { NgTemplateOutlet } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { ApiService } from './api.service';
import { DELEG_AR, DICT, Dict, Lang } from './i18n';
import { Category, Counts, Establishment, Facets, Kind } from './models';

type TabKey = 'sec' | 'pri' | 'priv' | 'insp';

const TAB_CATEGORIES: Record<TabKey, Category[]> = {
  sec: ['SECONDARY'],
  pri: ['PRIMARY'],
  priv: ['PRIVATE_PRIMARY', 'PRESCHOOL'],
  insp: ['INSPECTION'],
};
const TAB_HASH: Record<TabKey, string> = { sec: 'secondaire', pri: 'primaire', priv: 'prive', insp: 'inspections' };
const PAGE_SIZE = 48;
const KINDS: Kind[] = ['COLLEGE', 'LYCEE', 'PRIVATE'];

function ls(key: string): string | null {
  try { return localStorage.getItem(key); } catch { return null; }
}
function lsSet(key: string, value: string): void {
  try { localStorage.setItem(key, value); } catch { /* storage unavailable */ }
}

@Component({
  selector: 'app-root',
  imports: [NgTemplateOutlet],
  templateUrl: './app.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class App {
  private readonly api = inject(ApiService);

  readonly tabKeys: TabKey[] = ['sec', 'pri', 'priv', 'insp'];
  readonly kinds = KINDS;
  readonly delegs = DELEG_AR;

  readonly lang = signal<Lang>((ls('sfx2-lang') as Lang) || (navigator.language.startsWith('ar') ? 'ar' : 'fr'));
  readonly theme = signal<'light' | 'dark' | null>((ls('sfx2-theme') as 'light' | 'dark' | null) ?? null);
  readonly tab = signal<TabKey>('sec');
  readonly q = signal('');
  readonly filter = signal<string>('all');
  readonly favOnly = signal(false);
  readonly favs = signal<number[]>(this.loadFavs());

  readonly items = signal<Establishment[]>([]);
  readonly total = signal(0);
  readonly hasNext = signal(false);
  readonly page = signal(0);
  readonly loading = signal(false);
  readonly failed = signal(false);
  readonly counts = signal<Counts | null>(null);
  readonly facets = signal<Facets>({ kinds: {}, delegations: {} });
  readonly toastMsg = signal('');

  readonly t = computed<Dict>(() => DICT[this.lang()]);
  readonly dir = computed(() => (this.lang() === 'ar' ? 'rtl' : 'ltr'));
  readonly darkNow = computed(() => {
    const th = this.theme();
    return th === 'dark' || (th === null && matchMedia('(prefers-color-scheme: dark)').matches);
  });
  readonly chipKind = computed(() => this.tab() === 'sec');
  readonly chipDeleg = computed(() => this.tab() === 'pri' || this.tab() === 'insp');
  readonly allCount = computed(() => {
    const c = this.counts();
    if (!c) return 0;
    return TAB_CATEGORIES[this.tab()].reduce((n, k) => n + (c[k] ?? 0), 0);
  });

  private reqId = 0;
  private qTimer: ReturnType<typeof setTimeout> | undefined;
  private toastTimer: ReturnType<typeof setTimeout> | undefined;

  constructor() {
    const fromHash = (Object.keys(TAB_HASH) as TabKey[]).find((k) => TAB_HASH[k] === location.hash.slice(1));
    if (fromHash) this.tab.set(fromHash);
    this.applyHtmlAttrs();
    this.reload();
    window.addEventListener('hashchange', () => {
      const k = (Object.keys(TAB_HASH) as TabKey[]).find((x) => TAB_HASH[x] === location.hash.slice(1));
      if (k && k !== this.tab()) { this.tab.set(k); this.filter.set('all'); this.reload(); }
    });
  }

  tabCount(k: TabKey): number {
    const c = this.counts();
    return c ? TAB_CATEGORIES[k].reduce((n, cat) => n + (c[cat] ?? 0), 0) : 0;
  }
  facetCount(kind: 'kinds' | 'delegations', key: string): number {
    return this.facets()[kind][key] ?? 0;
  }
  othersWithHits(): { key: TabKey; n: number }[] {
    return this.tabKeys.filter((k) => k !== this.tab()).map((key) => ({ key, n: this.tabCount(key) })).filter((x) => x.n > 0);
  }

  /* ---------- data loading ---------- */
  reload(): void {
    const id = ++this.reqId;
    this.loading.set(true);
    this.failed.set(false);
    this.page.set(0);
    this.api.search(this.params(0)).subscribe({
      next: (r) => {
        if (id !== this.reqId) return;
        this.items.set(r.content); this.total.set(r.totalElements); this.hasNext.set(r.hasNext); this.loading.set(false);
      },
      error: () => { if (id === this.reqId) { this.loading.set(false); this.failed.set(true); } },
    });
    this.api.counts(this.q()).subscribe({ next: (c) => { if (id === this.reqId) this.counts.set(c); }, error: () => undefined });
    this.api.facets(TAB_CATEGORIES[this.tab()], this.q()).subscribe({ next: (f) => { if (id === this.reqId) this.facets.set(f); }, error: () => undefined });
  }

  loadMore(): void {
    const next = this.page() + 1;
    const id = this.reqId;
    this.loading.set(true);
    this.api.search(this.params(next)).subscribe({
      next: (r) => {
        if (id !== this.reqId) return;
        this.items.update((cur) => [...cur, ...r.content]);
        this.page.set(next); this.hasNext.set(r.hasNext); this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  private params(page: number) {
    const f = this.filter();
    return {
      categories: TAB_CATEGORIES[this.tab()],
      q: this.q(),
      kind: this.chipKind() && f !== 'all' ? (f as Kind) : null,
      delegation: this.chipDeleg() && f !== 'all' ? f : null,
      ids: this.favOnly() ? this.favs() : null,
      page,
      size: PAGE_SIZE,
    };
  }

  /* ---------- UI actions ---------- */
  onSearch(value: string): void {
    clearTimeout(this.qTimer);
    this.qTimer = setTimeout(() => { this.q.set(value); this.filter.set('all'); this.reload(); }, 200);
  }
  clearSearch(input: HTMLInputElement): void {
    input.value = ''; this.q.set(''); this.filter.set('all'); this.reload(); input.focus();
  }
  setTab(k: TabKey): void {
    this.tab.set(k); this.filter.set('all');
    try { history.replaceState(null, '', '#' + TAB_HASH[k]); } catch { /* ignore */ }
    this.reload();
  }
  setFilter(v: string): void { this.filter.set(v); this.reload(); }
  toggleFav(): void { this.favOnly.update((v) => !v); this.reload(); }
  reset(input: HTMLInputElement): void {
    input.value = ''; this.q.set(''); this.filter.set('all'); this.favOnly.set(false); this.reload();
  }
  switchLang(): void {
    this.lang.update((l) => (l === 'fr' ? 'ar' : 'fr')); lsSet('sfx2-lang', this.lang()); this.applyHtmlAttrs();
  }
  switchTheme(): void {
    const next = this.darkNow() ? 'light' : 'dark';
    this.theme.set(next); lsSet('sfx2-theme', next); this.applyHtmlAttrs();
  }
  private applyHtmlAttrs(): void {
    const root = document.documentElement;
    root.lang = this.lang(); root.dir = this.dir();
    const th = this.theme();
    if (th) root.dataset['theme'] = th; else delete root.dataset['theme'];
    document.title = this.t().title;
  }

  /* ---------- favourites ---------- */
  private loadFavs(): number[] {
    try { const v = JSON.parse(ls('sfx2-favs-v2') || '[]'); return Array.isArray(v) ? v.filter((x) => typeof x === 'number') : []; } catch { return []; }
  }
  isFav(id: number): boolean { return this.favs().includes(id); }
  toggleStar(id: number): void {
    this.favs.update((f) => (f.includes(id) ? f.filter((x) => x !== id) : [...f, id]));
    lsSet('sfx2-favs-v2', JSON.stringify(this.favs()));
    if (this.favOnly()) this.reload();
  }

  /* ---------- helpers for the template ---------- */
  fmt(p: string): string { return /^\d{8}$/.test(p) ? p.replace(/(\d{2})(\d{3})(\d{3})/, '$1 $2 $3') : p; }
  ok(p: string): boolean { return /^\d{8}$/.test(p); }
  isLandline(p: string): boolean { return /^7\d{7}$/.test(p); }
  mobiles(e: Establishment): string[] { return e.category === 'SECONDARY' ? e.phones.filter((p) => !this.isLandline(p)) : []; }
  landlines(e: Establishment): string[] { return e.category === 'SECONDARY' ? e.phones.filter((p) => this.isLandline(p)) : []; }
  otherPhones(e: Establishment): string[] { return e.category === 'SECONDARY' ? [] : e.phones; }
  tagKind(e: Establishment): string | null { return e.kind ? this.t().kinds[e.kind] : null; }
  tagSub(e: Establishment): string | null { return this.t().sub2[e.category] ?? null; }
  delegLabel(e: Establishment): string {
    if (!e.delegation) return '';
    const fr = this.t().deleg[e.delegation];
    return (e.category === 'INSPECTION' ? this.t().circ + ' · ' : '') + (fr ? fr + ' · ' : '') + e.delegation;
  }
  delegFr(d: string): string { return this.t().deleg[d] ?? ''; }

  /* ---------- clipboard ---------- */
  copy(text: string): void {
    const done = () => this.toast(this.t().copied);
    const fallback = () => {
      const ta = document.createElement('textarea');
      ta.value = text; ta.setAttribute('readonly', ''); ta.style.cssText = 'position:fixed;opacity:0;top:0';
      document.body.appendChild(ta); ta.select();
      try { document.execCommand('copy'); done(); } catch { /* ignore */ }
      document.body.removeChild(ta);
    };
    if (navigator.clipboard?.writeText) navigator.clipboard.writeText(text).then(done, fallback); else fallback();
  }
  copyCard(e: Establishment): void {
    const t = this.t();
    const lines = [e.name];
    for (const c of e.contacts) {
      lines.push(`${t.roles[c.role]}: ${c.fullName ?? ''}${c.phones.length ? ' (' + c.phones.map((p) => this.fmt(p)).join(' / ') + ')' : ''}`.trim());
    }
    const m = this.mobiles(e), l = this.landlines(e), o = this.otherPhones(e);
    if (m.length) lines.push(`${t.mobile}: ${m.map((p) => this.fmt(p)).join(' / ')}`);
    if (l.length) lines.push(`${t.fixe}: ${l.map((p) => this.fmt(p)).join(' / ')}`);
    if (o.length) lines.push(`${t.tel}: ${o.map((p) => this.fmt(p)).join(' / ')}`);
    if (e.email) lines.push(`${t.mail}: ${e.email}`);
    if (e.address) lines.push(`${t.addr}: ${e.address}`);
    this.copy(lines.join('\n'));
  }
  private toast(msg: string): void {
    this.toastMsg.set(msg);
    clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => this.toastMsg.set(''), 1400);
  }
}
