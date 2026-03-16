export interface FilterOptions {
  zoekterm?: string;
  genre?: string;
  uitgever?: string;
  minPagina?: number;
  maxPagina?: number;
  sorteerOp?: string;
  sorteerRichting?: 'ASC' | 'DESC';
}
