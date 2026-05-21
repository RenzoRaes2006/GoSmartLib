export interface LeeslijstRequest {
  naam: string;
  klas?: string | null;
  leerlingIds?: number[] | null;
  beschrijving?: string | null;
  zichtbaar?: boolean;
}
