export interface Gebruiker {
  gebruikerID?: number;
  schoolId: number;
  voorNaam: string;
  achterNaam: string;
  email: string;
  klas?: string;
  rol: 'LEERLING' | 'LEERKRACHT' | 'BEHEERDER';
}
