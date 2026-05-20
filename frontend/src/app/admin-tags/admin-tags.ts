import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { TagService } from '../services/tag.service';
import { Tag, TagType } from '../models/Tag';

const TAB_LABELS: Record<TagType, string> = {
  GENRE: 'Genres',
  THEMATISCH: 'Thematische tags',
  DIDACTISCH: 'Didactische tags',
};

@Component({
  selector: 'app-admin-tags',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-tags.html',
  styleUrl: './admin-tags.css',
})
export class AdminTags implements OnInit {
  private tagService = inject(TagService);

  tabs: TagType[] = ['GENRE', 'THEMATISCH', 'DIDACTISCH'];
  activeTab = signal<TagType>('GENRE');
  tags = signal<Tag[]>([]);
  laden = signal(false);
  fout = signal<string | null>(null);

  nieuweNaam = '';
  bewerktId: number | null = null;
  bewerktNaam = '';

  pageSize = signal(3);
  huidigePagina = signal(1);

  teVerwijderenTag = signal<Tag | null>(null);
  private foutTimer: ReturnType<typeof setTimeout> | null = null;

  totaalPaginas = computed(() => {
    return Math.max(1, Math.ceil(this.tags().length / this.pageSize()));
  });

  zichtbareTags = computed(() => {
    const start = (this.huidigePagina() - 1) * this.pageSize();
    return this.tags().slice(start, start + this.pageSize());
  });

  ngOnInit(): void {
    this.laadTags();
  }

  private toonFout(boodschap: string): void {
    this.fout.set(boodschap);
    if (this.foutTimer) clearTimeout(this.foutTimer);
    this.foutTimer = setTimeout(() => {
      this.fout.set(null);
      this.foutTimer = null;
    }, 5000);
  }

  tabLabel(type: TagType): string {
    return TAB_LABELS[type];
  }

  wisselTab(type: TagType): void {
    if (this.activeTab() === type) return;
    this.activeTab.set(type);
    this.bewerktId = null;
    this.nieuweNaam = '';
    this.fout.set(null);
    this.huidigePagina.set(1);
    this.laadTags();
  }

  vorigePagina(): void {
    if (this.huidigePagina() > 1) this.huidigePagina.update((p) => p - 1);
  }

  volgendePagina(): void {
    if (this.huidigePagina() < this.totaalPaginas()) this.huidigePagina.update((p) => p + 1);
  }

  laadTags(): void {
    this.laden.set(true);
    this.fout.set(null);
    this.tagService.list(this.activeTab()).subscribe({
      next: (data) => {
        this.tags.set(data);
        const max = Math.max(1, Math.ceil(data.length / this.pageSize()));
        if (this.huidigePagina() > max) this.huidigePagina.set(max);
        this.laden.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.toonFout(err.error ?? 'Kon tags niet laden.');
        this.laden.set(false);
      },
    });
  }

  toevoegen(): void {
    const naam = this.nieuweNaam.trim();
    if (!naam) return;
    this.fout.set(null);
    this.tagService.create(naam, this.activeTab()).subscribe({
      next: () => {
        this.nieuweNaam = '';
        this.laadTags();
      },
      error: (err: HttpErrorResponse) => this.toonFout(err.error ?? 'Aanmaken mislukt.'),
    });
  }

  startBewerken(tag: Tag): void {
    this.bewerktId = tag.id;
    this.bewerktNaam = tag.naam;
    this.fout.set(null);
  }

  annuleerBewerken(): void {
    this.bewerktId = null;
    this.bewerktNaam = '';
  }

  bewaarBewerken(tag: Tag): void {
    const nieuw = this.bewerktNaam.trim();
    if (!nieuw || nieuw === tag.naam) {
      this.annuleerBewerken();
      return;
    }
    this.tagService.rename(tag.id, nieuw).subscribe({
      next: () => {
        this.annuleerBewerken();
        this.laadTags();
      },
      error: (err: HttpErrorResponse) => this.toonFout(err.error ?? 'Hernoemen mislukt.'),
    });
  }

  verwijderen(tag: Tag): void {
    this.teVerwijderenTag.set(tag);
  }

  sluitVerwijderModal(): void {
    this.teVerwijderenTag.set(null);
  }

  bevestigVerwijderen(): void {
    const tag = this.teVerwijderenTag();
    if (!tag) return;
    this.tagService.delete(tag.id).subscribe({
      next: () => {
        this.teVerwijderenTag.set(null);
        this.laadTags();
      },
      error: (err: HttpErrorResponse) => {
        this.teVerwijderenTag.set(null);
        this.toonFout(err.error ?? 'Verwijderen mislukt.');
      },
    });
  }
}
