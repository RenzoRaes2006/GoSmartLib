import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BoekService } from '../services/boek.service';
import { RouterLink } from '@angular/router';
import * as XLSX from 'xlsx';

@Component({
  selector: 'app-boek-import',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './boek-import.html',
  styleUrl: './boek-import.css',
})
export class BoekImport {
  boekService = inject(BoekService);

  selectedFile: File | null = null;
  isUploading = signal(false);
  successMessage = signal('');
  errorMessage = signal('');

  onFileSelected(event: any) {
    const file: File = event.target.files[0];
    if (file) {
      this.selectedFile = file;
      this.errorMessage.set('');
      this.successMessage.set('');
    }
  }

  uploadBestand(fileInput: HTMLInputElement) {
    if (!this.selectedFile) return;

    this.isUploading.set(true);
    this.errorMessage.set('');
    this.successMessage.set('');

    this.boekService.importeerBoeken(this.selectedFile).subscribe({
      next: (response) => {
        this.successMessage.set(response);
        this.isUploading.set(false);
        this.selectedFile = null;
        fileInput.value = '';
      },
      error: (err) => {
        console.error('Upload fout:', err);

        if (err.error instanceof ProgressEvent) {
          this.errorMessage.set(
            'Kan geen verbinding maken met de server. Controleer of de backend draait.',
          );
        } else {
          this.errorMessage.set(
            err.error || 'Er is een fout opgetreden bij het uploaden van het bestand.',
          );
        }

        this.isUploading.set(false);
        fileInput.value = '';
      },
    });
  }

  downloadExcelVoorbeeld() {
    const data = [
      {
        Titel: 'De Hobbit',
        Auteur: 'J.R.R. Tolkien',
        ISBN: '9789022573523',
        Uitgever: 'Boekerij',
        Uitgavejaar: '2015',
        Pagina: 352,
        Genre: 'Fantasy, Avontuur',
        'Thematische tags': 'Klassieker, Magie, Reis',
        Beschrijving: 'Een episch avontuur over Bilbo Balings.',
        'Aantal exemplaren': 3,
        Locatie: 'Kast A1',
        'Afbeelding URL': 'https://example.com/hobbit.jpg',
        Leesniveau: 'B (gemiddeld taalgebruik)',
        Populariteit: 4.5,
      },
      {
        Titel: 'Harry Potter en de Steen der Wijzen',
        Auteur: 'J.K. Rowling',
        ISBN: '9789061695977',
        Uitgever: 'De Harmonie',
        Uitgavejaar: '1998',
        Pagina: 256,
        Genre: 'Fantasy, Jeugd',
        Beschrijving: 'Een jongen ontdekt dat hij een tovenaar is.',
        'Aantal exemplaren': 5,
        Locatie: 'Kast A2',
        'Afbeelding URL': '',
        Leesniveau: 'A (eenvoudig taalgebruik)',
      },
    ];

    // Maak een werkblad en een werkmap aan
    const worksheet: XLSX.WorkSheet = XLSX.utils.json_to_sheet(data);
    const workbook: XLSX.WorkBook = XLSX.utils.book_new();

    // Voeg het werkblad toe aan de werkmap met de naam "Boeken"
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Boeken');

    // Sla het bestand op en trigger de download
    XLSX.writeFile(workbook, 'voorbeeld_boeken_import.xlsx');
  }
}
