export interface GerapporteerdeBeoordelingAdmin {
    id: number;
    reviewId: number;
    boekTitel: string;
    reviewInhoud: string;
    schrijverId: number;
    melderId: number;
    reden: string;
    datumGemeld: string; // ISO-8601 string van LocalDateTime
}