export interface Boek {
  id?: number;
  titel: string;
  auteur: string;
  uitgever: string;
  genre: string[];
  thematischeTag: string[];
  pagina: number;
  uitgaveJaar: string;
  beschrijving: string;
  afbeelding: string;
  isbn: string;
  aantalExemplaren: number;
  locatie: string;
  leesniveau?: string;
  didactischMateriaal?: boolean;
  didactischeSubcategorie?: string;
}
