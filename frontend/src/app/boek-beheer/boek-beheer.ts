import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators,
  AbstractControl,
  ValidationErrors,
  FormsModule,
} from '@angular/forms';
import { RouterLink } from '@angular/router';
import { BoekService } from '../services/boek.service';
import { NieuwBoek } from '../models/NieuwBoek';
import { GoogleBookService } from '../services/googleBook.service';
import { Boek } from '../models/Boek';
import { FilterOptions } from '../models/FilterOptions';

@Component({
  selector: 'app-boek-beheer',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterLink, FormsModule],
  templateUrl: './boek-beheer.html',
  styleUrl: './boek-beheer.css',
})
export class BoekBeheer implements OnInit {
  // Mode management
  mode = signal<'toevoegen' | 'wijzigen'>('wijzigen');

  // Formulier en statusberichten
  boekForm;
  errorMessage = signal('');
  successMessage = signal('');

  // ISBN zoeken via Google Books
  toonIsbnZoek = signal(false);
  zoekTerm = signal('');
  zoekMessage = signal('');

  // Voor wijzig- en verwijdermodus
  boekGevonden = signal(false);
  naarTeWijzigenBoek = signal<Boek | null>(null);

  // Voor custom delete modal
  toonVerwijderModal = signal(false);

  // Boekenoverzicht en paginering
  boeknOverzicht = signal<Boek[]>([]);
  currentPage = signal(0);
  pageSize = 6;
  totaalPages = signal(0);
  isLoadingBoeken = signal(false);

  // Debounce timer voor live zoeken
  private zoekDebounceTimer: any = null;

  uploadModus = signal<'url' | 'bestand'>('url');
  uploadBezig = signal(false);

  // Genre dropdown
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
  selectedHoofdGenre = signal('');
  selectedSubGenre = signal('');
  geselecteerdeGenres = signal<string[]>([]);
  hoofdGenres = Object.keys(this.genreHiërarchie);

  // Constructor met dependency injection en formulieropbouw
  constructor(
    private fb: FormBuilder,
    private boekService: BoekService,
    private googleBookService: GoogleBookService,
  ) {
    this.boekForm = this.fb.group({
      titel: ['', [Validators.required, Validators.minLength(2)]],
      auteur: ['', [Validators.required, Validators.minLength(2)]],
      isbn: ['', [Validators.required, this.isbnValidator]],
      uitgever: [''],
      uitgaveJaar: [''],
      pagina: [''],
      genre: [''],
      beschrijving: ['', [Validators.required, Validators.minLength(10)]],
      aantalExemplaren: ['', [Validators.required, Validators.pattern(/^\d+$/)]],
      locatie: [''],
      afbeelding: [''],
      leesniveau: [''],
    });
  }

  // laad boekenoverzicht als we in wijzigmodus starten
  ngOnInit(): void {
    if (this.mode() === 'wijzigen') {
      this.laadBoekenPagina(0);
    }
  }

  // Wissel tussen toevoegen en wijzigen modus, reset het formulier en laad indien nodig het boekenoverzicht
  setMode(newMode: 'toevoegen' | 'wijzigen') {
    this.mode.set(newMode);
    this.resetForm();
    this.errorMessage.set('');
    this.successMessage.set('');
    this.boekGevonden.set(false);
    this.naarTeWijzigenBoek.set(null);
    this.toonIsbnZoek.set(false);
    this.boekForm.enable();

    // Laad het boekenoverzicht alleen in wijzigmodus
    if (newMode === 'wijzigen') {
      this.laadBoekenPagina(0);
    } else {
      this.boeknOverzicht.set([]);
    }
  }

  // Reset het formulier en statusberichten
  resetForm() {
    this.boekForm.reset();
    this.errorMessage.set('');
    this.successMessage.set('');
    this.boekGevonden.set(false);
    this.naarTeWijzigenBoek.set(null);
    this.geselecteerdeGenres.set([]);
    this.selectedHoofdGenre.set('');
    this.selectedSubGenre.set('');

    if (this.mode() === 'toevoegen') {
      this.toonIsbnZoek.set(false);
    }
  }

  // Poplueren van het formulier met de gegevens van een boek dat geselecteerd is voor wijziging
  private mapBoekNaarFormValue(boek: Boek) {
    return {
      titel: boek.titel,
      auteur: boek.auteur,
      isbn: boek.isbn,
      uitgever: boek.uitgever,
      uitgaveJaar: boek.uitgaveJaar,
      pagina: String(boek.pagina),
      genre: Array.isArray(boek.genre) ? boek.genre.join(', ') : '',
      beschrijving: boek.beschrijving,
      aantalExemplaren: String(boek.aantalExemplaren),
      locatie: boek.locatie,
      afbeelding: boek.afbeelding,
      leesniveau: boek.leesniveau || '',
    };
  }

  // Custom ISBN validator - accepteert ISBN-10 en ISBN-13 en "GO" nummers
  isbnValidator = (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    if (!value) return null;

    // Check of het 10 of 13 cijfers zijn
    if (/^\d{10}$/.test(value) || /^\d{13}$/.test(value) || /^GO\d{11}$/.test(value)) {
      return null;
    }
    return { isbn: 'ISBN moet 10 of 13 of GOcijfers zijn' };
  };

  // Nieuwe, vereenvoudigde functie om een "GO" nummer te genereren
  genereerIsbn(): void {
    const willekeurigeCijfers = this.randomCijfers(11);

    const isbn = `GO${willekeurigeCijfers}`;

    console.log('Gegenereerde ISBN:', isbn);

    this.boekForm.patchValue({ isbn });

    // Dit zorgt ervoor dat Angular de input herkent als ingevuld en de nieuwe isbnValidator aanroept
    this.boekForm.get('isbn')?.markAsDirty();
    this.boekForm.get('isbn')?.markAsTouched();
    this.boekForm.get('isbn')?.updateValueAndValidity();
  }

  // Genereer X random cijfers
  private randomCijfers(lengte: number): string {
    return Array.from({ length: lengte }, () => Math.floor(Math.random() * 10)).join('');
  }

  // Genereer ISBN-13 checkdigit
  private static berekenIsbn13CheckDigit(basis12: string): string {
    const som = basis12
      .split('')
      .map(Number)
      .reduce((acc, cijfer, i) => acc + cijfer * (i % 2 === 0 ? 1 : 3), 0);
    return String((10 - (som % 10)) % 10);
  }

  // Filter: alleen cijfers toestaan
  onIsbnInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    let value = input.value.replace(/\D/g, '');

    if (value.length > 13) {
      value = value.substring(0, 13);
    }

    input.value = value;
    this.boekForm.patchValue({ isbn: value }, { emitEvent: false });
  }

  // Zet de ISBN zoekmodus aan/uit
  toggleIsbnZoek() {
    this.toonIsbnZoek.set(!this.toonIsbnZoek());
    this.zoekTerm.set('');
    this.zoekMessage.set('');
  }

  // Zoek een boek op ISBN via Google Books API
  zoekBoekOp() {
    const term = this.zoekTerm();
    if (!term || (term.length !== 10 && term.length !== 13)) {
      this.zoekMessage.set('Voer een geldig ISBN in (10 of 13 cijfers).');
      return;
    }

    this.zoekMessage.set('Boek zoeken...');

    this.googleBookService.zoekOpIsbn(term).subscribe({
      next: (boekData) => {
        if (boekData) {
          this.boekForm.patchValue(boekData);
          // Populeer genre tags vanuit de Google Books data
          if (boekData.genre && typeof boekData.genre === 'string') {
            this.geselecteerdeGenres.set(
              boekData.genre
                .split(',')
                .map((g: string) => g.trim())
                .filter((g: string) => g.length > 0),
            );
            this.updateGenreFormControl();
          }
          this.boekForm.updateValueAndValidity();
          this.zoekMessage.set('Boek gevonden! vul de overige velden aan.');
          setTimeout(() => this.toonIsbnZoek.set(false), 3000);
        } else {
          this.zoekMessage.set('Geen boek gevonden met dit ISBN.');
        }
      },
      error: () => {
        this.zoekMessage.set('Fout bij het zoeken naar het boek.');
      },
    });
  }

  // Verwerk het toevoegen of wijzigen van een boek
  onSubmit() {
    this.errorMessage.set('');
    this.successMessage.set('');

    if (this.mode() === 'toevoegen') {
      this.submitToevoegen();
    } else if (this.mode() === 'wijzigen') {
      this.submitWijzigen();
    }
  }

  private submitToevoegen() {
    if (this.boekForm.invalid) {
      this.boekForm.markAllAsTouched();
      return;
    } else {
      const nieuwBoek = this.boekForm.value as NieuwBoek;
      // Converteer kommagescheiden genre-string naar array
      if (typeof nieuwBoek.genre === 'string') {
        nieuwBoek.genre = nieuwBoek.genre
          .split(',')
          .map((g) => g.trim())
          .filter((g) => g.length > 0);
      }
      this.boekService.toevoegen(nieuwBoek).subscribe({
        next: () => {
          console.log('Boek succesvol toegevoegd');
          this.successMessage.set('Boek succesvol toegevoegd!');
          this.boekForm.reset();
          setTimeout(() => this.successMessage.set(''), 5000);
        },
        error: (err) => {
          console.error('Fout bij het toevoegen van het boek:', err);

          if (err.status === 409) {
            this.errorMessage.set('Dit ISBN-nummer bestaat al!');
          } else if (err.status === 400) {
            this.errorMessage.set('Ongeldige invoer. Controleer alle velden.');
          } else {
            this.errorMessage.set('Er is een fout opgetreden: ' + err.message);
          }

          setTimeout(() => this.errorMessage.set(''), 5000);
        },
      });
    }
  }

  // Verwerk het wijzigen van een bestaand boek
  private submitWijzigen() {
    if (this.boekForm.invalid) {
      this.boekForm.markAllAsTouched();
      return;
    }

    const boek = this.naarTeWijzigenBoek();
    if (!boek || !boek.id) {
      this.errorMessage.set('geen boek geselecteerd');
      return;
    }

    const bijgewerktBoek = this.boekForm.value as NieuwBoek;
    // Converteer kommagescheiden genre-string naar array
    if (typeof bijgewerktBoek.genre === 'string') {
      bijgewerktBoek.genre = bijgewerktBoek.genre
        .split(',')
        .map((g) => g.trim())
        .filter((g) => g.length > 0);
    }
    // We moeten het bijgewerkte boek casten naar het volledige Boek-model, inclusief het ID
    // We gebruiken 'as unknown as Boek' omdat we zeker weten dat het voldoet aan de structuur van Boek, maar TypeScript dit niet automatisch kan afleiden vanwege het ontbreken van het ID-veld in NieuwBoek.
    this.boekService.wijzigen(boek.id, bijgewerktBoek as unknown as Boek).subscribe({
      next: () => {
        this.successMessage.set('Boek succesvol bijgewerkt!');
        this.boekForm.reset();
        this.boekGevonden.set(false);
        this.naarTeWijzigenBoek.set(null);
        this.toonIsbnZoek.set(false);
        this.laadBoekenPagina(this.currentPage()); // Herlaad de huidige pagina van het boekenoverzicht om de wijzigingen te tonen
        setTimeout(() => this.successMessage.set(''), 5000);
      },
      error: (err) => {
        console.error('Fout bij het bijwerken van het boek:', err);

        if (err.status === 409) {
          this.errorMessage.set('Dit ISBN-nummer bestaat al!');
        } else if (err.status === 400) {
          this.errorMessage.set('Ongeldige invoer. Controleer alle velden.');
        } else {
          this.errorMessage.set('Er is een fout opgetreden: ' + err.message);
        }

        setTimeout(() => this.errorMessage.set(''), 5000);
      },
    });
  }

  // Open de bevestigingsmodal als er een boek geselecteerd is.
  openVerwijderModal() {
    const boek = this.naarTeWijzigenBoek();
    if (!boek || !boek.id) {
      this.errorMessage.set('geen boek geselecteerd');
      return;
    }
    this.toonVerwijderModal.set(true);
  }

  // Sluit de bevestigingsmodal zonder te verwijderen.
  sluitVerwijderModal() {
    this.toonVerwijderModal.set(false);
  }

  // Verwijder het geselecteerde boek na bevestiging in de modal.
  bevestigVerwijderen() {
    this.toonVerwijderModal.set(false);
    const boek = this.naarTeWijzigenBoek();
    this.boekService.verwijderen(boek!.id!).subscribe({
      next: () => {
        this.successMessage.set('Boek succesvol verwijderd!');
        this.boekForm.reset();
        this.boekGevonden.set(false);
        this.naarTeWijzigenBoek.set(null);
        this.toonIsbnZoek.set(false);
        this.laadBoekenPagina(this.currentPage()); // Toon opnieuw de lijst na succesvol verwijderen
        setTimeout(() => this.successMessage.set(''), 5000);
      },
      error: (err) => {
        console.error('Fout bij het verwijderen van het boek:', err);
        this.errorMessage.set('Fout bij het verwijderen van het boek.');
        setTimeout(() => this.errorMessage.set(''), 5000);
      },
    });
  }

  // Laad een pagina met boeken voor het overzicht, inclusief paginering.
  // Deze functie wordt aangeroepen bij het initialiseren van het component en bij het navigeren tussen pagina's.
  laadBoekenPagina(page: number = 0) {
    this.isLoadingBoeken.set(true);
    this.boekService.getBoekenPaged(page, this.pageSize).subscribe({
      next: (response) => {
        // We krijgen een PageResponse terug van de API, die de boeken bevat in response.content en ook informatie over het totale aantal pagina's en elementen.
        this.boeknOverzicht.set(response.content);
        this.currentPage.set(response.number);
        this.totaalPages.set(response.totalPages);
        this.isLoadingBoeken.set(false);
      },
      error: (err) => {
        console.error('Fout bij het laden van boeken:', err);
        this.isLoadingBoeken.set(false);
      },
    });
  }

  gaNaarVolgendePagina() {
    if (this.currentPage() + 1 < this.totaalPages()) {
      this.laadBoekenPagina(this.currentPage() + 1);
    }
  }

  gaNaarVorigePagina() {
    if (this.currentPage() > 0) {
      this.laadBoekenPagina(this.currentPage() - 1);
    }
  }

  // Live zoeken: filter de boekenlijst terwijl de gebruiker typt
  onZoekTermInput(value: string) {
    this.zoekTerm.set(value);

    if (this.zoekDebounceTimer) {
      clearTimeout(this.zoekDebounceTimer);
    }

    this.zoekDebounceTimer = setTimeout(() => {
      if (this.mode() === 'wijzigen') {
        if (this.zoekTerm().trim().length === 0) {
          // Geen zoekterm: toon de normale gepagineerde lijst
          this.laadBoekenPagina(0);
        } else {
          // Zoek en toon resultaten in de lijst
          this.isLoadingBoeken.set(true);
          const filters: FilterOptions = { zoekterm: this.zoekTerm() };
          this.boekService.getBeheerBoekenGefilterd(filters).subscribe({
            next: (data) => {
              this.boeknOverzicht.set(data);
              this.currentPage.set(0);
              this.totaalPages.set(1);
              this.isLoadingBoeken.set(false);
            },
            error: () => {
              this.isLoadingBoeken.set(false);
            },
          });
        }
      }
    }, 300);
  }

  // Deze functie wordt aangeroepen wanneer een gebruiker een boek selecteert uit de lijst.
  // Voor 'wijzig': toon het formulier. Voor 'verwijder': toon direct de modal.
  selecteerBoekUitLijst(boek: Boek, actie: 'wijzig' | 'verwijder') {
    if (actie === 'verwijder') {
      this.naarTeWijzigenBoek.set(boek);
      // Direct de modal tonen zonder formulier
      this.openVerwijderModal();
    } else {
      this.naarTeWijzigenBoek.set(boek);
      // Populeer geselecteerde genres vanuit het boek
      this.geselecteerdeGenres.set(Array.isArray(boek.genre) ? [...boek.genre] : []);
      this.updateGenreFormControl();
      // Formulier tonen voor wijzigen
      this.boekForm.patchValue(this.mapBoekNaarFormValue(boek));
      this.boekGevonden.set(true);
      this.boekForm.enable();
    }
  }

  // Haal subgenres op basis van het geselecteerde hoofdgenre
  getSubGenres(hoofdGenre: string): string[] {
    return this.genreHiërarchie[hoofdGenre] || [];
  }

  // Voeg een genre toe aan de lijst van geselecteerde genres, gebaseerd op de dropdownselecties
  voegGenreToe() {
    const genre = this.selectedSubGenre() || this.selectedHoofdGenre();
    if (genre && !this.geselecteerdeGenres().includes(genre)) {
      this.geselecteerdeGenres.set([...this.geselecteerdeGenres(), genre]);
      this.updateGenreFormControl();
    }
    this.selectedHoofdGenre.set('');
    this.selectedSubGenre.set('');
  }

  // Verwijder een genre uit de lijst van geselecteerde genres
  verwijderGenre(genre: string) {
    this.geselecteerdeGenres.set(this.geselecteerdeGenres().filter((g) => g !== genre));
    this.updateGenreFormControl();
  }

  // Update de genre form control met de huidige lijst van geselecteerde genres
  private updateGenreFormControl() {
    const value = this.geselecteerdeGenres().join(', ');
    this.boekForm.patchValue({ genre: value });
    this.boekForm.get('genre')?.markAsDirty();
    this.boekForm.get('genre')?.updateValueAndValidity();
  }

  verwijderAfbeelding(): void {
    this.boekForm.patchValue({ afbeelding: '' });
  }

  opBestandGeselecteerd(event: Event): void {
    const input = event.target as HTMLInputElement;
    const bestand = input.files?.[0];

    if (!bestand) return;

    this.uploadBezig.set(true);
    this.boekService.uploadAfbeelding(bestand).subscribe({
      next: (url) => {
        this.boekForm.patchValue({ afbeelding: url });
        this.uploadBezig.set(false);
        input.value = '';
      },
      error: (err) => {
        this.errorMessage.set('Afbeelding uploaden mislukt: ' + err.error);
        this.uploadBezig.set(false);
        input.value = '';
      },
    });
  }
}
