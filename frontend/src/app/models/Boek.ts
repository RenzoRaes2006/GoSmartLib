import { Tag } from './Tag';

export interface Boek {
  id?: number;
  titel: string;
  auteur: string;
  uitgever: string;
  tags: Tag[];
  pagina: number;
  uitgaveJaar: string;
  beschrijving: string;
  afbeelding: string;
  isbn: string;
  aantalExemplaren: number;
  locatie: string;
  leesniveau?: string;
  didactischMateriaal?: boolean;
  campusId?: number | null;
  campusNaam?: string | null;
  heeftSchade?: boolean;
}
