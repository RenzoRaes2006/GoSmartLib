export interface LeeslijstRequest {
  naam: string;
  klas?: string | null;
  leerlingId?: number | null;
  beschrijving?: string | null;
  zichtbaar?: boolean;
}
