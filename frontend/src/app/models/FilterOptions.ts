export interface FilterOptions {
  zoekterm?: string;
  genre?: string;
  genres?: string[];
  uitgever?: string;
  minPagina?: number;
  maxPagina?: number;
  sorteerOp?: string;
  sorteerRichting?: 'ASC' | 'DESC';
  beschikbaarheid?: string;
  uitgaveJaarVan?: number;
  uitgaveJaarTot?: number;
  leesniveau?: string;
}
