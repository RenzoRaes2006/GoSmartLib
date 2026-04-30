import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BoekService } from '../services/boek.service';

@Component({
  selector: 'app-aanvragen-beheer',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './aanvragen-beheer.html',
  styleUrl: './aanvragen-beheer.css',
})
export class AanvragenBeheer implements OnInit {
  private boekService = inject(BoekService);

  aanvragen = signal<any[]>([]);
  verlengAanvragen = signal<any[]>([]);

  ngOnInit(): void {
    this.haalDataOp();
  }

  haalDataOp() {
    this.boekService.getAlleAanvragen().subscribe({
      next: (data) => {
        this.aanvragen.set(data);

        data.forEach((aanvraag: any) => {
          const userId = aanvraag.gebruiker.gebruikerID || aanvraag.gebruiker.id;
          this.boekService.getActieveLeningen(userId).subscribe({
            next: (leningen) => {
              aanvraag.totaalLeningen = leningen.length;
            },
          });
        });
      },
      error: (err) => console.error('Error fetching regular requests:', err),
    });

    this.boekService.getAlleVerlengingAanvragen().subscribe({
      next: (data) => this.verlengAanvragen.set(data),
      error: (err) => console.error('Error fetching extension requests:', err),
    });
  }

  keurGoed(id: number) {
    this.boekService.keurAanvraagGoed(id).subscribe({
      next: () => this.haalDataOp(),
      error: (err) => alert('Fout bij goedkeuren: ' + err.message),
    });
  }

  wijsAf(id: number) {
    this.boekService.weigerAanvraag(id).subscribe({
      next: () => this.haalDataOp(), // refresh the list
      error: (err) => alert('Fout bij weigeren: ' + err.message),
    });
  }

  keurVerlengingGoed(id: number) {
    this.boekService.keurVerlengingGoed(id).subscribe({
      next: () => this.haalDataOp(),
      error: (err) => alert('Fout bij goedkeuren verlenging: ' + err.message),
    });
  }

  wijsVerlengingAf(id: number) {
    this.boekService.weigerVerlenging(id).subscribe({
      next: () => this.haalDataOp(),
      error: (err) => alert('Fout bij weigeren verlenging: ' + err.message),
    });
  }
}
