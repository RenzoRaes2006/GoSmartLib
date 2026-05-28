import { Component, inject, Input, OnChanges, signal, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LesmateriaalService } from '../services/lesmateriaal.service';
import { Lesmateriaal } from '../models/Lesmateriaal';

@Component({
  selector: 'app-lesmateriaal-sectie',
  imports: [CommonModule, FormsModule],
  templateUrl: './lesmateriaal-sectie.html',
  styleUrl: './lesmateriaal-sectie.css',
})
export class LesmateriaalSectie implements OnChanges {
  private lesmateriaalService = inject(LesmateriaalService);

  @Input({ required: true }) boekId!: number;

  bestandLijst = signal<Lesmateriaal[]>([]);
  laden = signal<boolean>(false);
  bezig = signal<boolean>(false);
  fout = signal<string>('');
  succes = signal<string>('');
  dragOver = signal<boolean>(false);

  beschrijvingNieuw = '';
  bewerkenId: number | null = null;
  bewerkenBeschrijving = '';
  teVerwijderen = signal<Lesmateriaal | null>(null);

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['boekId'] && this.boekId) {
      this.laad();
    }
  }

  private laad() {
    this.laden.set(true);
    this.lesmateriaalService.list(this.boekId).subscribe({
      next: (lijst) => {
        this.bestandLijst.set(lijst);
        this.laden.set(false);
      },
      error: () => {
        this.fout.set('Kon lesmateriaal niet ophalen.');
        this.laden.set(false);
      },
    });
  }

  get eigenMateriaal(): Lesmateriaal[] {
    return this.bestandLijst().filter((l) => l.eigenaar);
  }

  get materiaalVanCollegas(): Lesmateriaal[] {
    return this.bestandLijst().filter((l) => !l.eigenaar);
  }

  onDragOver(event: DragEvent) {
    event.preventDefault();
    this.dragOver.set(true);
  }

  onDragLeave(event: DragEvent) {
    event.preventDefault();
    this.dragOver.set(false);
  }

  onDrop(event: DragEvent) {
    event.preventDefault();
    this.dragOver.set(false);
    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      this.uploadBestanden(Array.from(files));
    }
  }

  onFileSelect(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.uploadBestanden(Array.from(input.files));
      input.value = '';
    }
  }

  private uploadBestanden(files: File[]) {
    this.fout.set('');
    this.succes.set('');
    const beschrijving = this.beschrijvingNieuw.trim();
    this.bezig.set(true);

    let resterend = files.length;
    let mislukt = 0;
    for (const file of files) {
      this.lesmateriaalService.upload(this.boekId, file, beschrijving || undefined).subscribe({
        next: () => {
          resterend--;
          if (resterend === 0) this.afrondenUpload(files.length, mislukt);
        },
        error: () => {
          mislukt++;
          resterend--;
          if (resterend === 0) this.afrondenUpload(files.length, mislukt);
        },
      });
    }
  }

  private afrondenUpload(totaal: number, mislukt: number) {
    this.bezig.set(false);
    this.beschrijvingNieuw = '';
    if (mislukt === 0) {
      this.succes.set(`${totaal} bestand(en) geüpload.`);
      setTimeout(() => this.succes.set(''), 3000);
    } else if (mislukt === totaal) {
      this.fout.set('Upload mislukt. Controleer bestandsgrootte (max 50MB).');
    } else {
      this.fout.set(`${mislukt} van ${totaal} bestanden mislukt.`);
    }
    this.laad();
  }

  downloaden(l: Lesmateriaal) {
    this.lesmateriaalService.download(l.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = l.origineleBestandsnaam;
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => this.fout.set('Download mislukt.'),
    });
  }

  startBewerken(l: Lesmateriaal) {
    this.bewerkenId = l.id;
    this.bewerkenBeschrijving = l.beschrijving ?? '';
  }

  annuleerBewerken() {
    this.bewerkenId = null;
    this.bewerkenBeschrijving = '';
  }

  opslaanBewerken(l: Lesmateriaal) {
    this.lesmateriaalService.updateBeschrijving(l.id, this.bewerkenBeschrijving).subscribe({
      next: (updated) => {
        this.bestandLijst.update((lijst) => lijst.map((x) => (x.id === updated.id ? updated : x)));
        this.annuleerBewerken();
      },
      error: () => this.fout.set('Wijzigen mislukt.'),
    });
  }

  verwijderen(l: Lesmateriaal) {
    this.teVerwijderen.set(l);
  }

  annuleerVerwijderen() {
    this.teVerwijderen.set(null);
  }

  bevestigVerwijderen() {
    const l = this.teVerwijderen();
    if (!l) return;
    this.lesmateriaalService.delete(l.id).subscribe({
      next: () => {
        this.bestandLijst.update((lijst) => lijst.filter((x) => x.id !== l.id));
        this.teVerwijderen.set(null);
      },
      error: () => {
        this.fout.set('Verwijderen mislukt.');
        this.teVerwijderen.set(null);
      },
    });
  }

  bestandsicoon(contentType: string): string {
    if (contentType.startsWith('image/')) return '🖼️';
    if (contentType === 'application/pdf') return '📕';
    if (contentType.includes('word')) return '📝';
    if (contentType.includes('presentation') || contentType.includes('powerpoint')) return '📊';
    if (contentType.includes('sheet') || contentType.includes('excel')) return '📈';
    if (contentType.startsWith('video/')) return '🎬';
    if (contentType.startsWith('audio/')) return '🎵';
    return '📎';
  }

  formatGrootte(bytes: number): string {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  }

  formatDatum(iso: string): string {
    const d = new Date(iso);
    return d.toLocaleDateString('nl-BE', { day: '2-digit', month: '2-digit', year: 'numeric' });
  }
}
