import { Boek } from "./Boek";

export interface KlassLeeslijst {
  id: number;
  naam: string;
  klas?: string | null;
  leerlingIds?: number[] | null;
  leerlingNamen?: string[] | null;
  beschrijving?: string | null;
  zichtbaar: boolean;
  gearchiveerd: boolean;
  boeken: Boek[];
}
