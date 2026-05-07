import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BoekService } from '../services/boek.service';
import { Boek } from '../models/Boek';
import { FilterOptions } from '../models/FilterOptions';

// Nieuwe interface om het aantal per boek bij te houden
interface CartItem {
  boek: Boek;
  aantal: number;
}

@Component({
  selector: 'app-bulk-ontlenen',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './bulk-ontlenen-component.html',
  styleUrl: './bulk-ontlenen-component.css',
})
export class BulkOntlenenComponent implements OnInit {
  private boekService = inject(BoekService);

  zoekterm = signal<string>('');
  selectedGenre = signal<string>('');

  beschikbareGenres = [
    'Avontuur',
    'Fantasy',
    'Historische roman',
    'Spanning / thriller',
    'Non-fictie algemeen',
  ];

  zoekResultaten = signal<Boek[]>([]);
  isSearching = signal<boolean>(false);

  mandje = signal<CartItem[]>([]);

  mandjeIds = computed(() => this.mandje().map((item) => item.boek.id));
  totaalAantalBoeken = computed(() =>
    this.mandje().reduce((totaal, item) => totaal + item.aantal, 0),
  );

  successMessage = signal<string>('');
  errorMessage = signal<string>('');
  isSubmitting = signal<boolean>(false);

  ngOnInit(): void {
    this.zoekBoeken();
  }

  zoekBoeken() {
    this.isSearching.set(true);

    const filters: FilterOptions = {};
    if (this.zoekterm().trim()) filters.zoekterm = this.zoekterm();
    if (this.selectedGenre()) filters.genres = [this.selectedGenre()];
    filters.beschikbaarheid = 'true';

    this.boekService.getBoekenGefilterd(filters).subscribe({
      next: (response) => {
        this.zoekResultaten.set(response.content);
        this.isSearching.set(false);
      },
      error: (err) => {
        console.error('Fout bij zoeken:', err);
        this.isSearching.set(false);
      },
    });
  }

  onFilterChange() {
    this.zoekBoeken();
  }
  voegToeAanMandje(boek: Boek) {
    const huidigeItems = this.mandje();
    const bestaandItem = huidigeItems.find((item) => item.boek.id === boek.id);

    if (bestaandItem) {
      if (bestaandItem.aantal < boek.aantalExemplaren) {
        this.updateAantal(boek.id!, bestaandItem.aantal + 1);
      }
    } else {
      this.mandje.update((cart) => [...cart, { boek, aantal: 1 }]);
    }
  }

  updateAantal(boekId: number, nieuwAantal: number) {
    this.mandje.update((cart) =>
      cart.map((item) => {
        if (item.boek.id === boekId) {
          const geldigAantal = Math.max(1, Math.min(nieuwAantal, item.boek.aantalExemplaren));
          return { ...item, aantal: geldigAantal };
        }
        return item;
      }),
    );
  }

  verwijderUitMandje(boekId: number) {
    this.mandje.update((cart) => cart.filter((item) => item.boek.id !== boekId));
  }

  bevestigBulkOntlening() {
    if (this.mandje().length === 0) return;

    this.isSubmitting.set(true);
    this.errorMessage.set('');
    this.successMessage.set('');

    const boekIdsVoorBackend: number[] = [];
    this.mandje().forEach((item) => {
      for (let i = 0; i < item.aantal; i++) {
        boekIdsVoorBackend.push(item.boek.id!);
      }
    });

    const leraarId = 1; // Vervang dit door de daadwerkelijke ID van de aangelogde leerkracht

    this.boekService.leenBoekenBulk(leraarId, boekIdsVoorBackend).subscribe({
      next: (bericht) => {
        this.successMessage.set(bericht);
        this.mandje.set([]);
        this.isSubmitting.set(false);
        this.zoekBoeken();
      },
      error: (err) => {
        this.errorMessage.set('Er is een fout opgetreden bij het ontlenen.');
        this.isSubmitting.set(false);
      },
    });
  }
}
