export interface Beoordeling {
    id: number;
    gebruikerId: number;
    // ik moet deze nog later toevoegen als er gebruikers zijn, zodat ik de naam van de gebruiker kan tonen bij de beoordeling
    // ik moet het ook aanpassen in de backend en in de database, zodat de dto ook de naam van de gebruiker bevat
    //gebruikerNaam?: string;
    boekId: number;
    waardering: number;
    inhoud: string;
}