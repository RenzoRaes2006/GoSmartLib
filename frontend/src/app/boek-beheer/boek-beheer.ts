import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
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
  // Formulier en status boek toevoegen
  boekForm;
  errorMessage: string = '';
  successMessage: string = '';
  toonIsbnZoek: boolean = false;
  zoekTerm: string = '';
  zoekMessage: string = '';
  // Mode management
  mode: 'toevoegen' | 'wijzigen' = 'wijzigen';
  // Voor wijzig en verwijdermodus
  boekIdnaarWijzigen: String = '';
  boekGevonden: boolean = false;
  naarTeWijzigenBoek: any = null;
  // Voor custom delete modal
  toonVerwijderModal: boolean = false;
  // Voor boekenoverzicht en paginering
  boeknOverzicht: Boek[] = [];
  currentPage: number = 0;
  pageSize: number = 6;
  totaalPages: number = 0;
  isLoadingBoeken: boolean = false;
  // Debounce timer voor live zoeken
  private zoekDebounceTimer: any = null;

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
      uitgever: ['', [Validators.required, Validators.minLength(2)]],
      uitgaveJaar: ['', [Validators.required, Validators.pattern(/^\d{4}$/)]],
      pagina: ['', [Validators.required, Validators.pattern(/^\d+$/)]],
      genre: ['', [Validators.required, Validators.minLength(2)]],
      beschrijving: ['', [Validators.required, Validators.minLength(10)]],
      aantalExemplaren: ['', [Validators.required, Validators.pattern(/^\d+$/)]],
      locatie: ['', [Validators.required, Validators.minLength(1)]],
      afbeelding: [''],
      leesniveau: [''],
    });
  }

  // Initialisatie: laad boekenoverzicht als we in wijzigmodus starten
  ngOnInit(): void {
    if (this.mode === 'wijzigen') {
      this.laadBoekenPagina(0);
    }
  }

  // Custom ISBN validator - accepteert ISBN-10 en ISBN-13
  isbnValidator = (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    if (!value) return null;

    // Check of het 10 of 13 cijfers zijn
    if (/^\d{10}$/.test(value) || /^\d{13}$/.test(value)) {
      return null;
    }
    return { isbn: 'ISBN moet 10 of 13 cijfers zijn' };
  };

  // Genereer ISBN-13
  genereerIsbn(): void {
    const prefix = '978';
    const landcode = '90';
    const basis12 = `${prefix}${landcode}${this.randomCijfers(7)}`;
    const isbn = `${basis12}${BoekBeheer.berekenIsbn13CheckDigit(basis12)}`;

    this.boekForm.patchValue({ isbn });
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

  // Filter: alleen cijfers toestaan (max 13)
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
    this.toonIsbnZoek = !this.toonIsbnZoek;
    this.zoekTerm = '';
    this.zoekMessage = '';
  }

  // Deze functie wordt gebruikt bij het laden van een bestaand boek in wijzig/verwijdermodus, zodat de form correct wordt gepopuleerd.
  // Map een Boek-object naar de form-structuur, vooral voor genre-array naar string
  // Dit is nodig omdat het formulier een kommagescheiden string verwacht, terwijl het Boek-model een array van genres heeft.
  // We zorgen er ook voor dat pagina en aantalExemplaren als strings worden weergegeven in het formulier, omdat het formulier tekstinvoer gebruikt.
  // We moeten ook rekening houden met het feit dat sommige velden mogelijk null of undefined kunnen zijn, dus we gebruiken optionele chaining en standaardwaarden.
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

  // Zoek een boek op ISBN via Google Books API
  zoekBoekOp() {
    if (!this.zoekTerm || (this.zoekTerm.length !== 10 && this.zoekTerm.length !== 13)) {
      this.zoekMessage = 'Voer een geldig ISBN in (10 of 13 cijfers).';
      return;
    }

    this.zoekMessage = 'Boek zoeken...';

    this.googleBookService.zoekOpIsbn(this.zoekTerm).subscribe({
      next: (boekData) => {
        if (boekData) {
          this.boekForm.patchValue(boekData);
          this.boekForm.updateValueAndValidity();
          this.zoekMessage = 'Boek gevonden! vul de overige velden aan.';
          setTimeout(() => (this.toonIsbnZoek = false), 3000);
        } else {
          this.zoekMessage = 'Geen boek gevonden met dit ISBN.';
        }
      },
      error: () => {
        this.zoekMessage = 'Fout bij het zoeken naar het boek.';
      },
    });
  }

  // Reset het formulier en statusberichten
  resetForm() {
    this.boekForm.reset();
    this.errorMessage = '';
    this.successMessage = '';
    this.boekGevonden = false;
    this.naarTeWijzigenBoek = null;

    if (this.mode === 'toevoegen') {
      this.toonIsbnZoek = false;
    }
  }

  setMode(newMode: 'toevoegen' | 'wijzigen') {
    this.mode = newMode;
    this.resetForm();
    this.errorMessage = '';
    this.successMessage = '';
    this.boekGevonden = false;
    this.naarTeWijzigenBoek = null;
    this.toonIsbnZoek = false;
    this.boekForm.enable();

    // Laad het boekenoverzicht alleen in wijzigmodus
    if (newMode === 'wijzigen') {
      this.laadBoekenPagina(0);
    } else {
      this.boeknOverzicht = [];
    }
  }

  // Verwerk het toevoegen of wijzigen van een boek
  onSubmit() {
    this.errorMessage = '';
    this.successMessage = '';

    if (this.mode === 'toevoegen') {
      this.submitToevoegen();
    } else if (this.mode === 'wijzigen') {
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
          this.successMessage = 'Boek succesvol toegevoegd!';
          this.boekForm.reset();
          setTimeout(() => (this.successMessage = ''), 5000);
        },
        error: (err) => {
          console.error('Fout bij het toevoegen van het boek:', err);

          if (err.status === 409) {
            this.errorMessage = 'Dit ISBN-nummer bestaat al!';
          } else if (err.status === 400) {
            this.errorMessage = 'Ongeldige invoer. Controleer alle velden.';
          } else {
            this.errorMessage = 'Er is een fout opgetreden: ' + err.message;
          }

          setTimeout(() => (this.errorMessage = ''), 5000);
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

    if (!this.naarTeWijzigenBoek || !this.naarTeWijzigenBoek.id) {
      this.errorMessage = 'geen boek geselecteerd';
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
    this.boekService
      .wijzigen(this.naarTeWijzigenBoek.id, bijgewerktBoek as unknown as Boek)
      .subscribe({
        next: () => {
          this.successMessage = 'Boek succesvol bijgewerkt!';
          this.boekForm.reset();
          this.boekGevonden = false;
          this.naarTeWijzigenBoek = null;
          this.toonIsbnZoek = false;
          this.laadBoekenPagina(this.currentPage); // Herlaad de huidige pagina van het boekenoverzicht om de wijzigingen te tonen
          setTimeout(() => (this.successMessage = ''), 5000);
        },
        error: (err) => {
          console.error('Fout bij het bijwerken van het boek:', err);

          if (err.status === 409) {
            this.errorMessage = 'Dit ISBN-nummer bestaat al!';
          } else if (err.status === 400) {
            this.errorMessage = 'Ongeldige invoer. Controleer alle velden.';
          } else {
            this.errorMessage = 'Er is een fout opgetreden: ' + err.message;
          }

          setTimeout(() => (this.errorMessage = ''), 5000);
        },
      });
  }

  // Open de bevestigingsmodal als er een boek geselecteerd is.
  openVerwijderModal() {
    if (!this.naarTeWijzigenBoek || !this.naarTeWijzigenBoek.id) {
      this.errorMessage = 'geen boek geselecteerd';
      return;
    }
    this.toonVerwijderModal = true;
  }

  // Sluit de bevestigingsmodal zonder te verwijderen.
  sluitVerwijderModal() {
    this.toonVerwijderModal = false;
  }

  // Verwijder het geselecteerde boek na bevestiging in de modal.
  bevestigVerwijderen() {
    this.toonVerwijderModal = false;
    this.boekService.verwijderen(this.naarTeWijzigenBoek.id).subscribe({
      next: () => {
        this.successMessage = 'Boek succesvol verwijderd!';
        this.boekForm.reset();
        this.boekGevonden = false;
        this.naarTeWijzigenBoek = null;
        this.toonIsbnZoek = false;
        this.laadBoekenPagina(this.currentPage); // Toon opnieuw de lijst na succesvol verwijderen
        setTimeout(() => (this.successMessage = ''), 5000);
      },
      error: (err) => {
        console.error('Fout bij het verwijderen van het boek:', err);
        this.errorMessage = 'Fout bij het verwijderen van het boek.';
        setTimeout(() => (this.errorMessage = ''), 5000);
      },
    });
  }

  // Laad een pagina met boeken voor het overzicht, inclusief paginering.
  // Deze functie wordt aangeroepen bij het initialiseren van het component en bij het navigeren tussen pagina's.
  laadBoekenPagina(page: number = 0) {
    this.isLoadingBoeken = true;
    this.boekService.getBoekenPaged(page, this.pageSize).subscribe({
      next: (response) => {
        // We krijgen een PageResponse terug van de API, die de boeken bevat in response.content en ook informatie over het totale aantal pagina's en elementen.
        this.boeknOverzicht = response.content;
        this.currentPage = response.number;
        this.totaalPages = response.totalPages;
        this.isLoadingBoeken = false;
      },
      error: (err) => {
        console.error('Fout bij het laden van boeken:', err);
        this.isLoadingBoeken = false;
      },
    });
  }

  // Live zoeken: filter de boekenlijst terwijl de gebruiker typt
  onZoekTermInput(value: string) {
    this.zoekTerm = value;

    if (this.zoekDebounceTimer) {
      clearTimeout(this.zoekDebounceTimer);
    }

    this.zoekDebounceTimer = setTimeout(() => {
      if (this.mode === 'wijzigen') {
        if (this.zoekTerm.trim().length === 0) {
          // Geen zoekterm: toon de normale gepagineerde lijst
          this.laadBoekenPagina(0);
        } else {
          // Zoek en toon resultaten in de lijst
          this.isLoadingBoeken = true;
          const filters: FilterOptions = { zoekterm: this.zoekTerm };
          this.boekService.getBeheerBoekenGefilterd(filters).subscribe({
            next: (data) => {
              this.boeknOverzicht = data;
              this.currentPage = 0;
              this.totaalPages = 1;
              this.isLoadingBoeken = false;
            },
            error: () => {
              this.isLoadingBoeken = false;
            },
          });
        }
      }
    }, 300);
  }

  gaNaarVolgendePagina() {
    if (this.currentPage + 1 < this.totaalPages) {
      this.laadBoekenPagina(this.currentPage + 1);
    }
  }

  gaNaarVorigePagina() {
    if (this.currentPage > 0) {
      this.laadBoekenPagina(this.currentPage - 1);
    }
  }

  // Deze functie wordt aangeroepen wanneer een gebruiker een boek selecteert uit de lijst.
  // Voor 'wijzig': toon het formulier. Voor 'verwijder': toon direct de modal.
  selecteerBoekUitLijst(boek: Boek, actie: 'wijzig' | 'verwijder') {
    if (actie === 'verwijder') {
      this.naarTeWijzigenBoek = boek;
      // Direct de modal tonen zonder formulier
      this.openVerwijderModal();
    } else {
      this.naarTeWijzigenBoek = boek;
      // Formulier tonen voor wijzigen
      this.boekForm.patchValue(this.mapBoekNaarFormValue(boek));
      this.boekGevonden = true;
      this.boekForm.enable();
    }
  }
}
