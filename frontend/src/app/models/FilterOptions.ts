export interface FilterOptions {
  zoekterm?: string;
  tagIds?: number[];
  uitgever?: string;
  minPagina?: number;
  maxPagina?: number;
  sorteerOp?: string;
  sorteerRichting?: 'ASC' | 'DESC';
  beschikbaarheid?: string;
  uitgaveJaarVan?: number;
  uitgaveJaarTot?: number;
  leesniveau?: string;
  didactischMateriaal?: string;
  campusIds?: number[];
  minSterren?: number;
  page?: number;
  size?: number;
}
