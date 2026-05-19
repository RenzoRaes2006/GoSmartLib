import { Component, inject, OnInit } from '@angular/core';
import { Location } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { BoekService } from '../services/boek.service';
import { Boek } from '../models/Boek';
import { PageResponse } from '../models/PageResponse';
import { ReviewService } from '../services/review-service';
import { RapportageService } from '../services/rapportage-service';
import { Beoordeling } from '../models/review.model';
import { LeeslijstService } from '../services/leeslijstService';
import { LeenMandjeService } from '../services/leen-mandje.service';
import { TagsByTypePipe } from '../models/tags-by-type.pipe';

@Component({
  selector: 'app-detail',
  imports: [ReactiveFormsModule, RouterLink, FormsModule, TagsByTypePipe],
  templateUrl: './detail.html',
  styleUrl: './detail.css',
})
export class Detail implements OnInit {
  reviewService = inject(ReviewService);
  rapportageService = inject(RapportageService);
  boekService = inject(BoekService);
  fb = inject(FormBuilder);
  route = inject(ActivatedRoute);
  leeslijstService = inject(LeeslijstService);
  leenMandjeService = inject(LeenMandjeService);
  location = inject(Location);

  boek: Boek | null = null;
  gelijkaardigeBoeken: Boek[] = [];
  form!: FormGroup;
  submissionError: string = '';
  beoordelingen: Beoordeling[] = [];
  // COPILOT TOEVOEGING: Houdt bij welke spoiler-reviews de gebruiker bewust heeft opengeklikt.
  zichtbareSpoilers: Set<number> = new Set();

  isModalOpen: boolean = false;
  isFavoriet: boolean = false;
  succesMeldingMap: string = '';
  mappen: any[] = [];

  // Vriend's toevoegingen
  beschikbareEmojis = ['📚', '🎓', '🧠', '💡', '🎨', '🌍', '🔥', '❤️'];
  selectedEmoji = '📚';
  magLenen: boolean = false;
  leenReden: string = 'Status ophalen...';
  isLeenModalOpen: boolean = false;
  succesMeldingLenen: string = '';

  boekWaardering: number = 0;
  waarderingPrecentage: number = 0;

  rapportMelding: string = '';
  rapportMeldingType: 'success' | 'error' = 'success';
  rapporteerModalOpen: boolean = false;
  rapporteerReden: string = '';
  geselecteerdeReden: string = '';
  geselecteerdeBeoordelingId: number | null = null;
  gerapporteerdeBeoordelingen: number[] = [];


  boekId: number = 0;

  ngOnInit(): void {
    this.form = this.fb.group({
      waardering: ['', [Validators.required]],
      inhoud: ['', [Validators.required, Validators.maxLength(500)]],
      isAnoniem: [false],
      // COPILOT TOEVOEGING: Checkbox-waarde voor "bevat spoilers" bij het posten van een review.
      isSpoiler: [false],
    });

    this.route.paramMap.subscribe((params) => {
      const boekId = Number(params.get('id'));
      this.boekId = boekId;

      this.gelijkaardigeBoeken = [];

      this.boekService.getBoekOpId(boekId).subscribe({
        next: (boek) => {
          this.boek = boek;
          this.laadGelijkaardigeBoeken(boek);
          // Vriend's toevoeging: check leen status
          const gebruikerId = 1;
          this.boekService.checkLeenStatus(gebruikerId, boekId).subscribe({
            next: (status) => {
              this.magLenen = status.magLenen;
              this.leenReden = status.reden;
            },
            error: () => (this.leenReden = 'Kon uitleen status niet ophalen.'),
          });
        },
        error: (err) => console.log('Iets mis gegaan met het krijgen van de boek details. ', err), // Jouw error handling teruggezet
      });
      this.laadBeoordelingenVanBoek();
    });

    const gebruikerId = 1;
    this.leeslijstService.getMappenVanGebruiker(gebruikerId).subscribe({
      next: (data) => {
        // Vriend's toevoeging: filter Favorieten eruit
        this.mappen = data.filter((map) => map.naam.toLowerCase() !== 'favorieten');
      },
    });
  }

  private laadGelijkaardigeBoeken(boek: Boek) {
    const tagIds = (boek.tags ?? []).map((t) => t.id).filter((id) => id != null);
    if (tagIds.length === 0) {
      this.gelijkaardigeBoeken = [];
      return;
    }

    const empty: PageResponse<Boek> = {
      content: [],
    } as unknown as PageResponse<Boek>;

    this.boekService
      .getBoekenGefilterd({ tagIds, size: 30 })
      .pipe(catchError(() => of(empty)))
      .subscribe((page) => {
        const tagSet = new Set(tagIds);
        const score = (b: Boek) => (b.tags ?? []).filter((t) => tagSet.has(t.id)).length;

        this.gelijkaardigeBoeken = (page.content ?? [])
          .filter((b) => b.id != null && b.id !== boek.id)
          .map((b) => ({ b, s: score(b) }))
          .filter((x) => x.s > 0)
          .sort((a, b) => b.s - a.s)
          .slice(0, 6)
          .map((x) => x.b);
      });
  }

  laadBeoordelingenVanBoek() {
    this.reviewService.getBeoordelingenOpBoekId(this.boekId).subscribe({
      next: (beoordelingen) => {
        this.beoordelingen = beoordelingen;
        // COPILOT TOEVOEGING: Bij herladen resetten we opengeklikte spoilers,
        // zodat de gebruiker opnieuw bewust moet kiezen om spoilers te lezen.
        this.zichtbareSpoilers.clear();
        this.updateBoekWaardering();
      },
      // Jouw error handling hersteld!
      error: (err) => console.log('Iets mis gegaan met het krijgen van de beoordelingen. ', err),
    });
  }

  updateBoekWaardering() {
    if (this.beoordelingen.length > 0) {
      const totaleWaardering = this.beoordelingen.reduce(
        (sum, beoordeling) => sum + beoordeling.waardering,
        0,
      );
      // Vriend's kortere berekening
      this.boekWaardering = Number((totaleWaardering / this.beoordelingen.length).toFixed(1));
      this.waarderingPrecentage = (this.boekWaardering / 5) * 100;
    }
  }

  voegToeAanFavorieten() {
    if (!this.boek || this.isFavoriet) return;
    const gebruikerId = 1;
    // Vriend's toevoeging: emoji meegeven
    this.leeslijstService
      .voegBoekToeAanMap(gebruikerId, 'Favorieten', this.boek.id!, '⭐')
      .subscribe({
        next: () => {
          this.isFavoriet = true;
          // Jouw timeout teruggezet
          setTimeout(() => (this.isFavoriet = false), 3000);
        },
      });
  }

  voegToeAanLeeslijst() {
    if (!this.boek) return;
    this.isModalOpen = true;
    this.selectedEmoji = '📚';
  }

  sluitModal() {
    this.isModalOpen = false;
    this.succesMeldingMap = '';
  }

  bevestigToevoegenAanMap(bestaandeMap: string, nieuweMap: string) {
    const doelMap = nieuweMap.trim() !== '' ? nieuweMap.trim() : bestaandeMap;

    if (doelMap && doelMap !== '') {
      const gebruikerId = 1;
      this.leeslijstService
        .voegBoekToeAanMap(gebruikerId, doelMap, this.boek!.id!, this.selectedEmoji)
        .subscribe({
          next: () => {
            this.succesMeldingMap = `Boek toegevoegd aan '${doelMap}'!`;
            this.leeslijstService.getMappenVanGebruiker(gebruikerId).subscribe((data) => {
              this.mappen = data.filter((map) => map.naam.toLowerCase() !== 'favorieten');
            });
            setTimeout(() => this.sluitModal(), 1500);
          },
          error: () => (this.succesMeldingMap = 'Er ging iets mis...'),
        });
    }
  }

  submitBeoordeling() {
    if (this.form.invalid) {
      this.submissionError = 'Vul alle verplichte velden in (max 500 karakters).';
      return;
    }
    const raw = this.form.value;
    const beoordeling: Omit<Beoordeling, 'id'> = {
      gebruikerNaam: '',
      boekId: this.boek?.id ?? 0,
      waardering: Number(raw.waardering),
      inhoud: raw.inhoud,
      isAnoniem: raw.isAnoniem,
      gerapporteerd: false,
      // COPILOT TOEVOEGING: Stuurt spoiler-flag mee naar de backend bij create.
      isSpoiler: raw.isSpoiler,
    };

    this.reviewService.voegBeoordelingToe(beoordeling).subscribe({
      next: () => {
        this.form.reset({
          waardering: '',
          inhoud: '',
          isAnoniem: false,
          // COPILOT TOEVOEGING: Na submit default terug op false voor voorspelbaar gedrag.
          isSpoiler: false,
        });
        this.submissionError = '';
        this.laadBeoordelingenVanBoek();
      },
      // Jouw error handling hersteld!
      error: (error) => {
        if (error.status === 400) {
          this.submissionError = 'Je beoordeling bevat ongepaste taal. Pas deze aan en probeer opnieuw.';
        } else {
          this.submissionError = 'Er ging iets mis bij het toevoegen. Probeer opnieuw.';
        }
      },
    });
  }

  // COPILOT TOEVOEGING: True betekent "spoiler en nog niet vrijgegeven" -> inhoud verbergen.
  isSpoilerVerborgen(beoordeling: Beoordeling): boolean {
    return beoordeling.isSpoiler && !this.zichtbareSpoilers.has(beoordeling.id);
  }

  // COPILOT TOEVOEGING: Gebruiker kiest expliciet om 1 specifieke spoiler-review te tonen.
  toonSpoiler(beoordelingId: number) {
    this.zichtbareSpoilers.add(beoordelingId);
  }

  // Jouw ongewijzigde functie hersteld!
  bevestigRapport() {
    const customReden = this.rapporteerReden.trim();
    const reden: string = this.geselecteerdeReden === 'anders' ? customReden : this.geselecteerdeReden;

    if (!reden) {
      this.rapportMeldingType = 'error';
      this.rapportMelding = 'Geef een geldige reden op.';
      setTimeout(() => (this.rapportMelding = ''), 3000);
      return;
    }

    this.rapportageService
      .rapporteerBeoordeling(this.geselecteerdeBeoordelingId!, reden)
      .subscribe({
        next: () => {
          this.gerapporteerdeBeoordelingen.push(this.geselecteerdeBeoordelingId!);

          // Update de lokale lijst zodat de UI direct weerspiegelt dat deze gerapporteerd is
          const beoordeling = this.beoordelingen.find(
            (b) => b.id === this.geselecteerdeBeoordelingId,
          );

          if (beoordeling) {
            beoordeling.gerapporteerd = true;
          }

          this.rapportMeldingType = 'success';
          this.rapportMelding = 'Beoordeling succesvol gerapporteerd. Bedankt voor je feedback!';
          this.rapporteerModalOpen = false;
          this.geselecteerdeBeoordelingId = null;
          this.rapporteerReden = '';

          setTimeout(() => (this.rapportMelding = ''), 4000);
        },
        error: (err) => {
          this.rapportMeldingType = 'error';
          this.rapportMelding = 'Er ging iets mis bij het rapporteren. Probeer opnieuw.';
          setTimeout(() => (this.rapportMelding = ''), 4000);
        },
      });
  }

  openRapporteerModal(beoordelingId: number) {
    this.rapporteerModalOpen = true;
    this.geselecteerdeBeoordelingId = beoordelingId;
    this.geselecteerdeReden = '';
    this.rapporteerReden = '';
  }

  sluitRapporteerModal() {
    this.rapporteerModalOpen = false;
    this.geselecteerdeReden = '';
    this.rapporteerReden = '';
  }

  // --- Vriend's functies hieronder ---
  openLeenModal() {
    if (this.magLenen) this.isLeenModalOpen = true;
  }

  sluitLeenModal() {
    this.isLeenModalOpen = false;
    this.succesMeldingLenen = '';
  }

  voegToeAanReservering() {
    if (!this.boek) return;
    const resultaat = this.leenMandjeService.voegBoekToe(this.boek);
    this.succesMeldingLenen =
      resultaat === 'Succes'
        ? `'${this.boek.titel}' is toegevoegd aan je lenings aanvragen!`
        : resultaat;
    this.isLeenModalOpen = true;
    setTimeout(() => this.sluitLeenModal(), 2500);
  }

  goBack(): void {
    this.location.back();
  }
}
