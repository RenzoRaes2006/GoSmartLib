export interface Gebruiker {
  // Nieuwe shape (backend Gebruiker → User migratie)
  id: number;
  smartschoolId: string;
  klas?: string;
  rol?: 'LEERLING' | 'LEERKRACHT' | 'BEHEERDER';

  // Verouderde velden — backend stuurt deze niet meer mee.
  // Optioneel gehouden zodat bestaande templates compileren tot ze gerefactord zijn.
  /** @deprecated gebruik `id` */
  gebruikerID?: number;
  /** @deprecated bestaat niet meer in het backend-DTO */
  schoolId?: number;
  /** @deprecated bestaat niet meer in het backend-DTO */
  voorNaam?: string;
  /** @deprecated bestaat niet meer in het backend-DTO */
  achterNaam?: string;
  /** @deprecated bestaat niet meer in het backend-DTO */
  achternaam?: string;
  /** @deprecated bestaat niet meer in het backend-DTO */
  email?: string;
}
