import { Boek } from "./Boek";

export interface KlassLeeslijst {
  id: number;
  naam: string;
  klas?: string | null;
  leerlingId?: number | null;
  leerlingNaam?: string | null;
  beschrijving?: string | null;
  zichtbaar: boolean;
  gearchiveerd: boolean;
  boeken: Boek[];
}
