export interface OpenLeningPreview {
  uitleningId: number;
  leerlingSmartschoolId: string;
  boekTitel: string;
  boekIsbn: string;
}

export interface RolloverPreview {
  aantalVertrokkenLeerlingen: number;
  openLeningenVertrokkenen: OpenLeningPreview[];
  aantalTeArchiverenKlasLeeslijsten: number;
  kanDoorgaan: boolean;
}

export interface RolloverResultaat {
  nieuwSchooljaar: string;
  leerlingenGedeactiveerd: number;
  beoordelingenGeanonimiseerd: number;
  leeslijstenGearchiveerd: number;
  leningenAlsVerlorenGemarkeerd: number;
}

export interface RolloverParams {
  forceerVerloren: boolean;
  nieuwNaam: string;
  nieuwStartDatum: string;
  nieuwEindDatum: string;
}
