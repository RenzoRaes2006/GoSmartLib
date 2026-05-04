import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BoekService } from '../services/boek.service';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import * as XLSX from 'xlsx';

@Component({
  selector: 'app-boek-import',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './boek-import.html',
  styleUrl: './boek-import.css',
})
export class BoekImport {
  boekService = inject(BoekService);

  selectedFile: File | null = null;
  isUploading = signal(false);
  successMessage = signal('');
  errorMessage = signal('');

  previewLijst = signal<any[]>([]);
  expandedRowIndex = signal<number | null>(null);

  itemsPerPage = 10;
  currentPage = signal<number>(1);

  // --- NIEUWE SELECTIE DATA ---
  genreHiërarchie: Record<string, string[]> = {
    Avontuur: [],
    'Coming-of-age': [],
    'Detective / misdaad': [],
    Dystopie: [],
    Fantasy: [],
    'Fictie algemeen': [],
    'Graphic novel / strip': [],
    'Historische roman': [],
    Horror: [],
    Humor: [],
    'Literaire roman': [],
    'Non-fictie algemeen': [
      'Biografie / autobiografie',
      'Filosofie',
      'Geschiedenis',
      'Kunst & cultuur',
      'Maatschappij & politiek',
      'Psychologie',
      'Wetenschap & technologie',
    ],
    'Oorlog & conflict': [],
    Poëzie: [],
    Romantiek: [],
    'Spanning / thriller': [],
  };
  hoofdGenres = Object.keys(this.genreHiërarchie);

  beschikbareTags: string[] = [
    'Armoede & ongelijkheid',
    'Avontuur & ontdekking',
    'Diversiteit & inclusie',
    'Familie',
    'Gender & seksualiteit',
    'Identiteit & zelfbeeld',
    'Liefde & relaties',
    'Macht & onrecht',
    'Mentale gezondheid',
    'Migratie & afkomst',
    'Overleven',
    'Rouw & verlies',
    'School & prestatiedruk',
    'Sociale media',
    'Toekomst & technologie',
    'Vriendschap',
  ];

  // --- TIJDELIJKE STAAT PER RIJ ---
  // We houden per geopende rij bij welke dropdown-waarden actief zijn, en of hij uploadt of url-invoer wil
  actiefHoofdGenre = signal('');
  actiefSubGenre = signal('');
  actieveTag = signal('');

  // Track image upload per boek index (sleutel = echte index in de volledige lijst)
  imageUploadMode = signal<Record<number, 'url' | 'bestand'>>({});
  imageUploadBezig = signal<Record<number, boolean>>({});

  gepagineerdePreviewLijst = computed(() => {
    const start = (this.currentPage() - 1) * this.itemsPerPage;
    return this.previewLijst().slice(start, start + this.itemsPerPage);
  });

  totaalPaginas = computed(() => Math.ceil(this.previewLijst().length / this.itemsPerPage) || 1);

  onFileSelected(event: any) {
    const file: File = event.target.files[0];
    if (file) {
      this.selectedFile = file;
      this.errorMessage.set('');
      this.successMessage.set('');
    }
  }

  laadPreview(fileInput: HTMLInputElement) {
    if (!this.selectedFile) return;

    this.isUploading.set(true);
    this.errorMessage.set('');
    this.successMessage.set('');

    this.boekService.uploadVoorPreview(this.selectedFile).subscribe({
      next: (boekenLijst) => {
        // Zorg dat genre en tag altíjd arrays zijn in de frontend,
        // zelfs als de backend per ongeluk null of string stuurde.
        const veiligeLijst = boekenLijst.map((b: any) => ({
          ...b,
          genre: Array.isArray(b.genre)
            ? b.genre
            : b.genre
              ? b.genre.split(',').map((x: string) => x.trim())
              : [],
          thematischeTag: Array.isArray(b.thematischeTag)
            ? b.thematischeTag
            : b.thematischeTag
              ? b.thematischeTag.split(',').map((x: string) => x.trim())
              : [],
        }));

        this.previewLijst.set(veiligeLijst);
        this.currentPage.set(1);
        this.expandedRowIndex.set(null);

        // Reset image modi: alles standaard op URL
        const initialModes: Record<number, 'url' | 'bestand'> = {};
        veiligeLijst.forEach((_: any, i: number) => (initialModes[i] = 'url'));
        this.imageUploadMode.set(initialModes);
        this.imageUploadBezig.set({});

        this.isUploading.set(false);
        this.successMessage.set(
          `Succes! ${boekenLijst.length} boeken uitgelezen. Controleer ze hieronder.`,
        );
      },
      error: (err) => {
        this.isUploading.set(false);
        this.errorMessage.set(
          err.error?.message || err.error || 'Fout bij het uitlezen van het bestand.',
        );
        fileInput.value = '';
        this.selectedFile = null;
      },
    });
  }

  // Berekent de 'echte' index in previewLijst() op basis van paginering en de relatieve $index
  private getEchteIndex(relatieveIndex: number): number {
    return (this.currentPage() - 1) * this.itemsPerPage + relatieveIndex;
  }

  toggleRow(relatieveIndex: number) {
    if (this.expandedRowIndex() === relatieveIndex) {
      this.expandedRowIndex.set(null);
    } else {
      this.expandedRowIndex.set(relatieveIndex);
      // Reset dropdown selecties wanneer een nieuwe rij opent
      this.actiefHoofdGenre.set('');
      this.actiefSubGenre.set('');
      this.actieveTag.set('');
    }
  }

  // --- GENRE LOGICA (AANGEPAST VOOR ARRAYS IN EEN LIJST) ---
  getSubGenres(hoofdGenre: string): string[] {
    return this.genreHiërarchie[hoofdGenre] || [];
  }

  voegGenreToe(boek: any) {
    const nieuwGenre = this.actiefSubGenre() || this.actiefHoofdGenre();
    if (nieuwGenre && !boek.genre.includes(nieuwGenre)) {
      boek.genre.push(nieuwGenre);
    }
    this.actiefHoofdGenre.set('');
    this.actiefSubGenre.set('');
  }

  verwijderGenre(boek: any, genreOmTeVerwijderen: string) {
    boek.genre = boek.genre.filter((g: string) => g !== genreOmTeVerwijderen);
  }

  // --- TAG LOGICA ---
  voegTagToe(boek: any) {
    const nieuweTag = this.actieveTag();
    if (nieuweTag && !boek.thematischeTag.includes(nieuweTag)) {
      boek.thematischeTag.push(nieuweTag);
    }
    this.actieveTag.set('');
  }

  verwijderTag(boek: any, tagOmTeVerwijderen: string) {
    boek.thematischeTag = boek.thematischeTag.filter((t: string) => t !== tagOmTeVerwijderen);
  }

  // --- AFBEELDING UPLOAD LOGICA ---
  getUploadMode(relatieveIndex: number): 'url' | 'bestand' {
    return this.imageUploadMode()[this.getEchteIndex(relatieveIndex)] || 'url';
  }

  setUploadMode(relatieveIndex: number, mode: 'url' | 'bestand') {
    const current = { ...this.imageUploadMode() };
    current[this.getEchteIndex(relatieveIndex)] = mode;
    this.imageUploadMode.set(current);
  }

  getUploadBezig(relatieveIndex: number): boolean {
    return this.imageUploadBezig()[this.getEchteIndex(relatieveIndex)] || false;
  }

  opBestandGeselecteerd(event: Event, boek: any, relatieveIndex: number): void {
    const input = event.target as HTMLInputElement;
    const bestand = input.files?.[0];
    if (!bestand) return;

    const echteIndex = this.getEchteIndex(relatieveIndex);

    // Zet bezigs-indicator aan voor specifiek dit boek
    this.imageUploadBezig.update((m) => ({ ...m, [echteIndex]: true }));

    this.boekService.uploadAfbeelding(bestand).subscribe({
      next: (url) => {
        boek.afbeelding = url; // Update de afbeelding URL in het boek object
        this.imageUploadBezig.update((m) => ({ ...m, [echteIndex]: false }));
        input.value = '';
      },
      error: (err) => {
        alert('Afbeelding uploaden mislukt: ' + err.error);
        this.imageUploadBezig.update((m) => ({ ...m, [echteIndex]: false }));
        input.value = '';
      },
    });
  }

  verwijderAfbeelding(boek: any): void {
    boek.afbeelding = '';
  }

  verwijderUitPreview(relatieveIndex: number, titel: string) {
    const echteIndex = this.getEchteIndex(relatieveIndex);
    const huidigeLijst = this.previewLijst();
    huidigeLijst.splice(echteIndex, 1);
    this.previewLijst.set([...huidigeLijst]);

    this.expandedRowIndex.set(null);
    this.successMessage.set(`"${titel}" is uit de importlijst verwijderd.`);

    if (this.currentPage() > this.totaalPaginas()) {
      this.currentPage.set(Math.max(1, this.totaalPaginas()));
    }

    setTimeout(() => this.successMessage.set(''), 4000);
  }

  vorigePagina() {
    if (this.currentPage() > 1) {
      this.currentPage.update((p) => p - 1);
      this.expandedRowIndex.set(null);
    }
  }
  volgendePagina() {
    if (this.currentPage() < this.totaalPaginas()) {
      this.currentPage.update((p) => p + 1);
      this.expandedRowIndex.set(null);
    }
  }

  annuleer() {
    this.previewLijst.set([]);
    this.selectedFile = null;
    this.successMessage.set('');
    this.errorMessage.set('');
    this.expandedRowIndex.set(null);
  }

  isTitelOngeldig(boek: any): boolean {
    return !boek.titel || boek.titel.trim().length < 2;
  }

  isAuteurOngeldig(boek: any): boolean {
    return !boek.auteur || boek.auteur.trim().length < 2;
  }

  isLijstGeldig(): boolean {
    const lijst = this.previewLijst();
    if (lijst.length === 0) return false;

    return lijst.every(
      (boek) =>
        boek.titel &&
        boek.titel.trim().length >= 2 &&
        boek.auteur &&
        boek.auteur.trim().length >= 2 &&
        boek.isbn &&
        boek.isbn.trim().length > 0,
    );
  }

  genereerIsbn(boek: any): void {
    const willekeurigeCijfers = Array.from({ length: 11 }, () =>
      Math.floor(Math.random() * 10),
    ).join('');
    boek.isbn = `GO${willekeurigeCijfers}`;
  }
  bevestigImport() {
    this.isUploading.set(true);

    this.boekService.bevestigImport(this.previewLijst()).subscribe({
      next: (bericht) => {
        this.successMessage.set(bericht);
        this.previewLijst.set([]);
        this.selectedFile = null;
        this.isUploading.set(false);
      },
      error: (err) => {
        this.isUploading.set(false);
        this.errorMessage.set('Er is een fout opgetreden tijdens het definitief opslaan.');
      },
    });
  }
  downloadExcelVoorbeeld() {
    // Het is cruciaal dat élk object exact dezelfde keys heeft (ook al zijn ze leeg).
    // Zo worden de Excel-kolommen altijd in de juiste volgorde en correct gegenereerd.
    const data = [
      {
        Titel: 'De Hobbit',
        Auteur: 'J.R.R. Tolkien',
        ISBN: '9789022573523',
        Uitgever: 'Boekerij',
        Uitgavejaar: '2015',
        Pagina: 352,
        'Aantal exemplaren': 3,
        Genre: 'Fantasy, Avontuur',
        'Thematische tags': 'Klassieker, Magie, Reis',
        Populariteit: 4.5,
        Beschrijving: 'Een episch avontuur over Bilbo Balings.',
        Locatie: 'Kast A1',
        'Afbeelding URL': 'https://example.com/hobbit.jpg',
        Leesniveau: 'B (gemiddeld taalgebruik)',
      },
      {
        Titel: 'Oorlogswinter',
        Auteur: 'Jan Terlouw',
        ISBN: '9789047700252',
        Uitgever: 'Lemniscaat',
        Uitgavejaar: '1972',
        Pagina: 168,
        'Aantal exemplaren': 5,
        Genre: 'Historisch, Spanning',
        'Thematische tags': 'Tweede Wereldoorlog, Verzet, Jeugd',
        Populariteit: 4.8,
        Beschrijving: 'Michiel raakt in de hongerwinter betrokken bij het verzet.',
        Locatie: 'Kast B3',
        'Afbeelding URL': '',
        Leesniveau: 'B (gemiddeld taalgebruik)',
      },
      {
        Titel: 'Alleen Titel en Auteur zijn verplicht',
        Auteur: 'Test Auteur',
        ISBN: '',
        Uitgever: '',
        Uitgavejaar: '',
        Pagina: '',
        'Aantal exemplaren': 1,
        Genre: '',
        'Thematische tags': '',
        Populariteit: '',
        Beschrijving: 'De rest van de velden mag leeg zijn, maar VERWIJDER DE KOLOMMEN NIET!',
        Locatie: '',
        'Afbeelding URL': '',
        Leesniveau: '',
      },
    ];

    // Maak een werkblad en een werkmap aan
    const worksheet: XLSX.WorkSheet = XLSX.utils.json_to_sheet(data);
    const workbook: XLSX.WorkBook = XLSX.utils.book_new();

    // UX-Upgrade: Maak de kolommen automatisch wat breder zodat de beheerder direct kan lezen wat er staat
    worksheet['!cols'] = [
      { wch: 35 }, // Titel
      { wch: 20 }, // Auteur
      { wch: 15 }, // ISBN
      { wch: 15 }, // Uitgever
      { wch: 12 }, // Uitgavejaar
      { wch: 8 }, // Pagina
      { wch: 18 }, // Aantal exemplaren
      { wch: 20 }, // Genre
      { wch: 25 }, // Thematische tags
      { wch: 12 }, // Populariteit
      { wch: 50 }, // Beschrijving
      { wch: 12 }, // Locatie
      { wch: 25 }, // Afbeelding URL
      { wch: 20 }, // Leesniveau
    ];

    // Voeg het werkblad toe aan de werkmap met de naam "Import Sjabloon"
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Import Sjabloon');

    // Sla het bestand op en trigger de download met een professionele naam
    XLSX.writeFile(workbook, 'SmartLib_Boeken_Import_Sjabloon.xlsx');
  }
}
