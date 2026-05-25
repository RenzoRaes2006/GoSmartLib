import { Boek } from "./Boek";

export interface KlassLeeslijst {
  id: number;
  naam: string;
  klas?: string | null;
  leerkrachtId?: number | null;
  leerkrachtNaam?: string | null;
  leerlingIds?: number[] | null;
  leerlingNamen?: string[] | null;
  beschrijving?: string | null;
  zichtbaar: boolean;
  gearchiveerd: boolean;
  boeken: Boek[];
}
