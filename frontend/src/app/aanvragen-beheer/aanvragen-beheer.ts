import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BoekService } from '../services/boek.service';

@Component({
  selector: 'app-aanvragen-beheer',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './aanvragen-beheer.html',
  styleUrl: './aanvragen-beheer.css'
})
export class AanvragenBeheer implements OnInit {
  private boekService = inject(BoekService);

  // signal to hold the pending requests
  aanvragen = signal<any[]>([]);

  ngOnInit(): void {
    this.haalAanvragenOp();
  }

  haalAanvragenOp() {
    this.boekService.getAlleAanvragen().subscribe({
      next: (data) => {
        this.aanvragen.set(data);

        // fetch active loan count for each user in the background
        data.forEach((aanvraag: any) => {
          const userId = aanvraag.gebruiker.gebruikerID || aanvraag.gebruiker.id; 
          this.boekService.getActieveLeningen(userId).subscribe({
            next: (leningen) => {
              aanvraag.totaalLeningen = leningen.length;
            }
          });
        });
      },
      error: (err) => console.error('Error fetching requests:', err)
    });
  }

  keurGoed(id: number) {
    this.boekService.keurAanvraagGoed(id).subscribe({
      next: () => {
        this.haalAanvragenOp(); // refresh the list
      },
      error: (err) => alert('Fout bij goedkeuren: ' + err.message)
    });
  }

  wijsAf(id: number) {
    this.boekService.weigerAanvraag(id).subscribe({
      next: () => {
        this.haalAanvragenOp(); // refresh the list
      },
      error: (err) => alert('Fout bij weigeren: ' + err.message)
    });
  }
}