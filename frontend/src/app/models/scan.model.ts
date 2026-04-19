export type ScanModus = 'UITLENING' | 'RETOUR';

export interface MandjeItem {
  isbn: string;
  titel: string;
}

export interface ScanBatchRequest {
  gebruikerId: number;
  actie: ScanModus;
  isbns: string[];
}

export interface ScanBatchItemResult {
  isbn: string;
  titel: string | null;
  status: 'OK' | 'FOUT';
  foutReden: string | null;
}

export interface ScanBatchResponse {
  actie: ScanModus;
  resultaten: ScanBatchItemResult[];
}
