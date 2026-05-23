import { CommonModule } from '@angular/common';
import { Component, ElementRef, OnInit, signal, viewChild, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
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
import { TagService } from '../services/tag.service';
import { CampusService, Campus } from '../services/campus.service';
import { NieuwBoek } from '../models/NieuwBoek';
import { GoogleBookService } from '../services/googleBook.service';
import { Boek } from '../models/Boek';
import { Tag, TagType } from '../models/Tag';
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
  toonModeSelector = signal(true);

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

  private zoekIsbnVeld = viewChild<ElementRef<HTMLInputElement>>('zoekIsbnVeld');

  uploadModus = signal<'url' | 'bestand'>('url');
  uploadBezig = signal(false);

  // Tag dropdowns — gevuld uit /api/tags door tagService.list()
  private tagService = inject(TagService);
  private campusService = inject(CampusService);
  beschikbareGenres = signal<Tag[]>([]);
  beschikbareThematischeTags = signal<Tag[]>([]);
  beschikbareDidactischeTags = signal<Tag[]>([]);
  beschikbareCampussen = signal<Campus[]>([]);
  campusFilterIds = signal<number[]>([]);

  selectedGenreId = signal<number | null>(null);
  selectedSubGenreId = signal<number | null>(null);
  selectedThematischeId = signal<number | null>(null);
  selectedDidactischeId = signal<number | null>(null);

  geselecteerdeTags = signal<Tag[]>([]);

  constructor(
    private fb: FormBuilder,
    private boekService: BoekService,
    private googleBookService: GoogleBookService,
    private route: ActivatedRoute,
  ) {
    this.boekForm = this.fb.group({
      titel: ['', [Validators.required, Validators.minLength(2)]],
      auteur: ['', [Validators.required, Validators.minLength(2)]],
      isbn: ['', [Validators.required, this.isbnValidator]],
      uitgever: [''],
      uitgaveJaar: [''],
      pagina: [''],
      beschrijving: ['', [Validators.required, Validators.minLength(10)]],
      aantalExemplaren: ['', [Validators.required, Validators.pattern(/^\d+$/)]],
      locatie: [''],
      afbeelding: [''],
      leesniveau: [''],
      didactischMateriaal: [false],
      campusId: [null as number | null],
    });
  }

  ngOnInit(): void {
    this.tagService.tree('GENRE').subscribe((t) => this.beschikbareGenres.set(t));
    this.tagService.list('THEMATISCH').subscribe((t) => this.beschikbareThematischeTags.set(t));
    this.tagService.list('DIDACTISCH').subscribe((t) => this.beschikbareDidactischeTags.set(t));
    this.campusService.getMijnCampussen().subscribe({
      next: (c) => this.beschikbareCampussen.set(c ?? []),
      error: () => this.beschikbareCampussen.set([]),
    });

    this.route.queryParams.subscribe((params) => {
      const urlMode = params['mode'];
      if (urlMode === 'toevoegen' || urlMode === 'wijzigen') {
        this.toonModeSelector.set(false);
        this.setMode(urlMode);
      } else {
        this.toonModeSelector.set(true);
        if (this.mode() === 'wijzigen') {
          this.laadBoekenPagina(0);
        }
      }
    });
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

  resetForm() {
    this.boekForm.reset();
    this.boekForm.patchValue({ didactischMateriaal: false });
    this.errorMessage.set('');
    this.successMessage.set('');
    this.boekGevonden.set(false);
    this.naarTeWijzigenBoek.set(null);

    this.resetDropdowns();

    if (this.mode() === 'toevoegen') {
      this.toonIsbnZoek.set(false);
    }
  }

  private resetDropdowns() {
    this.geselecteerdeTags.set([]);
    this.selectedGenreId.set(null);
    this.selectedSubGenreId.set(null);
    this.selectedThematischeId.set(null);
    this.selectedDidactischeId.set(null);
  }

  geselecteerdHoofdGenre(): Tag | null {
    const id = this.selectedGenreId();
    if (id == null) return null;
    return this.beschikbareGenres().find((g) => g.id === id) ?? null;
  }

  subGenresVanGeselecteerd(): Tag[] {
    return this.geselecteerdHoofdGenre()?.subTags ?? [];
  }

  onHoofdGenreChange(id: number | null): void {
    this.selectedGenreId.set(id);
    this.selectedSubGenreId.set(null);
  }

  voegGenreToe(): void {
    const subId = this.selectedSubGenreId();
    const hoofdId = this.selectedGenreId();
    const idOmToeTeVoegen = subId ?? hoofdId;
    if (idOmToeTeVoegen == null) return;
    const tag = this.vindGenreTag(idOmToeTeVoegen);
    if (!tag) return;
    if (this.geselecteerdeTags().some((t) => t.id === tag.id)) return;
    this.geselecteerdeTags.set([...this.geselecteerdeTags(), tag]);
    this.selectedGenreId.set(null);
    this.selectedSubGenreId.set(null);
  }

  private vindGenreTag(id: number): Tag | null {
    for (const hoofd of this.beschikbareGenres()) {
      if (hoofd.id === id) return hoofd;
      for (const sub of hoofd.subTags ?? []) {
        if (sub.id === id) return sub;
      }
    }
    return null;
  }

  private mapBoekNaarFormValue(boek: Boek) {
    return {
      titel: boek.titel,
      auteur: boek.auteur,
      isbn: boek.isbn,
      uitgever: boek.uitgever,
      uitgaveJaar: boek.uitgaveJaar,
      pagina: String(boek.pagina),
      beschrijving: boek.beschrijving,
      aantalExemplaren: String(boek.aantalExemplaren),
      locatie: boek.locatie,
      afbeelding: boek.afbeelding,
      leesniveau: boek.leesniveau || '',
      didactischMateriaal: !!boek.didactischMateriaal,
      campusId: boek.campusId ?? null,
    };
  }

  geselecteerdeTagsVanType(type: TagType): Tag[] {
    return this.geselecteerdeTags().filter((t) => t.type === type);
  }

  voegTagToe(type: TagType, idSignal: ReturnType<typeof signal<number | null>>): void {
    const id = idSignal();
    if (!id) return;
    const lijst =
      type === 'GENRE'
        ? this.beschikbareGenres()
        : type === 'THEMATISCH'
          ? this.beschikbareThematischeTags()
          : this.beschikbareDidactischeTags();
    const tag = lijst.find((t) => t.id === id);
    if (!tag) return;
    if (this.geselecteerdeTags().some((t) => t.id === tag.id)) return;
    this.geselecteerdeTags.set([...this.geselecteerdeTags(), tag]);
    idSignal.set(null);
  }

  verwijderTag(tag: Tag): void {
    this.geselecteerdeTags.set(this.geselecteerdeTags().filter((t) => t.id !== tag.id));
  }

  // Custom ISBN validator - accepteert ISBN-10 en ISBN-13 en "GO" nummers
  isbnValidator = (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    if (!value) return null;

    // Check of het 10 of 13 cijfers zijn
    if (/^\d{10}$/.test(value) || /^\d{13}$/.test(value) || /^GO\d{11}$/.test(value)) {
      return null;
    }
    return { isbn: 'ISBN moet 10 of 13 of GO cijfers zijn' };
  };

  // Vereenvoudigde functie om een "GO" nummer te genereren
  genereerIsbn(): void {
    const willekeurigeCijfers = this.randomCijfers(11);

    const isbn = `GO${willekeurigeCijfers}`;

    console.log('Gegenereerde ISBN:', isbn);

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

  onZoekIsbnInput(value: string) {
    this.zoekTerm.set(value.replace(/[^0-9]/g, '').slice(0, 13));
  }

  // Zet de ISBN zoekmodus aan/uit
  toggleIsbnZoek() {
    const opent = !this.toonIsbnZoek();
    this.toonIsbnZoek.set(opent);
    this.zoekTerm.set('');
    this.zoekMessage.set('');
    if (opent) {
      setTimeout(() => this.zoekIsbnVeld()?.nativeElement.focus(), 0);
    }
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

  // Verwerk het toevoegen van een nieuw boek
  private submitToevoegen() {
    if (this.boekForm.invalid) {
      this.boekForm.markAllAsTouched();
      return;
    } else {
      const nieuwBoek = { ...this.boekForm.value, tags: this.geselecteerdeTags() } as NieuwBoek;
      this.boekService.toevoegen(nieuwBoek).subscribe({
        next: () => {
          this.successMessage.set('Boek succesvol toegevoegd!');
          this.boekForm.reset();
          this.boekForm.patchValue({ didactischMateriaal: false });
          this.resetDropdowns();
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

    const bijgewerktBoek = { ...this.boekForm.value, tags: this.geselecteerdeTags() } as NieuwBoek;

    this.boekService.wijzigen(boek.id, bijgewerktBoek as unknown as Boek).subscribe({
      next: () => {
        this.successMessage.set('Boek succesvol bijgewerkt!');
        this.boekForm.reset();
        this.boekForm.patchValue({ didactischMateriaal: false });
        this.resetDropdowns();
        this.boekGevonden.set(false);
        this.naarTeWijzigenBoek.set(null);
        this.toonIsbnZoek.set(false);
        this.laadBoekenPagina(this.currentPage());
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
        this.boekForm.patchValue({ didactischMateriaal: false });
        this.resetDropdowns();
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
  laadBoekenPagina(page: number = 0) {
    this.isLoadingBoeken.set(true);
    const filters: FilterOptions = { page, size: this.pageSize };
    const ids = this.campusFilterIds();
    if (ids.length > 0) {
      filters.campusIds = ids;
    }
    this.boekService.getBoekenGefilterd(filters).subscribe({
      next: (response) => {
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

  // Toggle een campus in de filter en herlaad de lijst.
  toggleCampusFilter(campusId: number) {
    const huidige = this.campusFilterIds();
    if (huidige.includes(campusId)) {
      this.campusFilterIds.set(huidige.filter((id) => id !== campusId));
    } else {
      this.campusFilterIds.set([...huidige, campusId]);
    }
    this.laadBoekenPagina(0);
  }

  isCampusGefilterd(campusId: number): boolean {
    return this.campusFilterIds().includes(campusId);
  }

  wisCampusFilter() {
    this.campusFilterIds.set([]);
    this.laadBoekenPagina(0);
  }

  gaNaarVolgendePagina() {
    if (this.currentPage() + 1 < this.totaalPages()) {
      const nextPage = this.currentPage() + 1;
      if (this.zoekTerm().trim().length > 0) {
        this.laadZoekResultaten(nextPage);
      } else {
        this.laadBoekenPagina(nextPage);
      }
    }
  }

  gaNaarVorigePagina() {
    if (this.currentPage() > 0) {
      const prevPage = this.currentPage() - 1;
      if (this.zoekTerm().trim().length > 0) {
        this.laadZoekResultaten(prevPage);
      } else {
        this.laadBoekenPagina(prevPage);
      }
    }
  }

  private laadZoekResultaten(page: number) {
    this.isLoadingBoeken.set(true);
    const filters: FilterOptions = { zoekterm: this.zoekTerm() };
    const ids = this.campusFilterIds();
    if (ids.length > 0) {
      filters.campusIds = ids;
    }
    this.boekService.getBeheerBoekenGefilterd(filters, page, this.pageSize).subscribe({
      next: (response) => {
        this.boeknOverzicht.set(response.content);
        this.currentPage.set(response.number);
        this.totaalPages.set(response.totalPages);
        this.isLoadingBoeken.set(false);
      },
      error: () => {
        this.isLoadingBoeken.set(false);
      },
    });
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
          const ids = this.campusFilterIds();
          if (ids.length > 0) {
            filters.campusIds = ids;
          }
          this.boekService.getBeheerBoekenGefilterd(filters, 0, this.pageSize).subscribe({
            next: (response) => {
              this.boeknOverzicht.set(response.content);
              this.currentPage.set(response.number);
              this.totaalPages.set(response.totalPages);
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

  // Voor 'wijzig': toon het formulier. Voor 'verwijder': toon direct de modal.
  selecteerBoekUitLijst(boek: Boek, actie: 'wijzig' | 'verwijder') {
    if (actie === 'verwijder') {
      this.naarTeWijzigenBoek.set(boek);
      // Direct de modal tonen zonder formulier
      this.openVerwijderModal();
    } else {
      this.naarTeWijzigenBoek.set(boek);
      this.geselecteerdeTags.set([...(boek.tags ?? [])]);
      this.boekForm.patchValue(this.mapBoekNaarFormValue(boek));
      this.boekGevonden.set(true);
      this.boekForm.enable();
    }
  }

  naamFn = (t: Tag): string => t.naam;

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
