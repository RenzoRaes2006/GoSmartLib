export interface Boek {
  id?: number;
  titel: string;
  auteur: string;
  isbn: string;
  uitgever: string;
  publicatieJaar: number;
  aantalPaginas: number;
  genre: string;
  beschrijving: string;
  aantalExemplaren: number;
  locatie: string;
  coverUrl: string;
}
