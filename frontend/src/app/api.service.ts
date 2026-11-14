import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Category, Counts, Establishment, Facets, PageResponse, SearchParams } from './models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/establishments';

  search(p: SearchParams): Observable<PageResponse<Establishment>> {
    let params = new HttpParams().set('category', p.categories.join(',')).set('page', p.page).set('size', p.size);
    if (p.q.trim()) params = params.set('q', p.q.trim());
    if (p.kind) params = params.set('kind', p.kind);
    if (p.delegation) params = params.set('delegation', p.delegation);
    if (p.ids) params = params.set('ids', p.ids.length ? p.ids.join(',') : '0');
    return this.http.get<PageResponse<Establishment>>(this.base, { params });
  }

  counts(q: string): Observable<Counts> {
    let params = new HttpParams();
    if (q.trim()) params = params.set('q', q.trim());
    return this.http.get<Counts>(`${this.base}/counts`, { params });
  }

  facets(categories: Category[], q: string): Observable<Facets> {
    let params = new HttpParams().set('category', categories.join(','));
    if (q.trim()) params = params.set('q', q.trim());
    return this.http.get<Facets>(`${this.base}/facets`, { params });
  }
}
