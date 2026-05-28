export interface Lesmateriaal {
  id: number;
  boekId: number;
  uploaderId: number;
  uploaderNaam: string;
  origineleBestandsnaam: string;
  contentType: string;
  grootte: number;
  beschrijving: string | null;
  uploadedAt: string;
  eigenaar: boolean;
}
