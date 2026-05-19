import { Tag } from './Tag';

export interface NieuwBoek {
  titel: string;
  auteur: string;
  isbn: string;
  uitgever: string;
  uitgaveJaar: string;
  pagina: string;
  tags: Tag[];
  beschrijving: string;
  aantalExemplaren: string;
  locatie: string;
  afbeelding: string;
  leesniveau?: string;
  didactischMateriaal?: boolean;
}
