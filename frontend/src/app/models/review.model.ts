export interface Beoordeling {
    id: number;
    boekId: number;
    gebruikerNaam?: string; 
    waardering: number;
    inhoud: string;
    isAnoniem: boolean;
    gerapporteerd: boolean;
}