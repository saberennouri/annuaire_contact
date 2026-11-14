export type Category = 'SECONDARY' | 'PRIMARY' | 'PRIVATE_PRIMARY' | 'PRESCHOOL' | 'INSPECTION';
export type Kind = 'COLLEGE' | 'LYCEE' | 'PRIVATE';
export type Role = 'DIRECTOR' | 'CENSEUR' | 'SURVEILLANT_1' | 'SURVEILLANT_2' | 'ASSISTANT' | 'CLERK';

export interface Contact {
  role: Role;
  fullName: string | null;
  phones: string[];
}

export interface Establishment {
  id: number;
  category: Category;
  kind: Kind | null;
  delegation: string | null;
  number: number | null;
  name: string;
  email: string | null;
  address: string | null;
  note: string | null;
  phones: string[];
  contacts: Contact[];
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
}

export interface Facets {
  kinds: Record<string, number>;
  delegations: Record<string, number>;
}

export type Counts = Record<Category, number>;

export interface SearchParams {
  categories: Category[];
  q: string;
  kind?: Kind | null;
  delegation?: string | null;
  ids?: number[] | null;
  page: number;
  size: number;
}
