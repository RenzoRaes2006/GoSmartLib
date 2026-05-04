import { Boek } from "./Boek";

export interface KlassLeeslijst {
  id: number;
  naam: string;
  boeken: Boek[]; 
}