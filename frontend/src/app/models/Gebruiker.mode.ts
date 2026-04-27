export interface Gebruiker {
  gebruikerID?: number;
  schoolId: number;
  voorNaam: string;
  achterNaam: string;
  email: string;
  rol: 'LEERLING' | 'LEERKRACHT' | 'BEHEERDER';
}
