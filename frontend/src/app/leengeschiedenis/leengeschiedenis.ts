import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { BoekService } from '../services/boek.service';

@Component({
  selector: 'app-leengeschiedenis',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './leengeschiedenis.html',
  styleUrl: './leengeschiedenis.css'
})
export class Leengeschiedenis implements OnInit {
  private boekService = inject(BoekService);
  
  gebruikerId = 1; // Jouw ingelogde gebruiker
  alleLeningen = signal<any[]>([]);

  // Filters en dropdown data
  beschikbareSchooljaren = signal<string[]>([]);
  geselecteerdSchooljaar = signal<string>('');

  // De maanden van een schooljaar (Sept -> Aug)
  maandenNamen = ['September', 'Oktober', 'November', 'December', 'Januari', 'Februari', 'Maart', 'April', 'Mei', 'Juni', 'Juli', 'Augustus'];

  ngOnInit() {
    this.boekService.getLeengeschiedenis(this.gebruikerId).subscribe({
      next: (data) => {
        // Filter gewijgerde aanvragen eruit als je die niet in de geschiedenis wil zien
        const geldigeLeningen = data.filter(l => l.status !== 'GEWEIGERD');
        this.alleLeningen.set(geldigeLeningen);
        this.berekenSchooljaren(geldigeLeningen);
      },
      error: (err) => console.error('Fout bij ophalen geschiedenis', err)
    });
  }

  berekenSchooljaren(leningen: any[]) {
    const jaren = new Set<string>();
    
    // --- NIEUW: Voeg ALTIJD het huidige schooljaar toe op basis van de datum van vandaag ---
    const nu = new Date();
    const huidigeMaand = nu.getMonth(); // 0 = Jan, 8 = Sep
    const huidigJaar = nu.getFullYear();
    
    let huidigSchooljaar = '';
    if (huidigeMaand >= 8) {
      // September of later (bijv. nov 2026 wordt "2026-2027")
      huidigSchooljaar = `${huidigJaar}-${huidigJaar + 1}`;
    } else {
      // Voor september (bijv. april 2026 wordt "2025-2026")
      huidigSchooljaar = `${huidigJaar - 1}-${huidigJaar}`;
    }
    jaren.add(huidigSchooljaar);
    // ---------------------------------------------------------------------------------------

    // Loop daarna door alle leningen zoals we al deden
    leningen.forEach(l => {
      if (!l.uitleenDatum) return;
      
      const date = new Date(l.uitleenDatum);
      const month = date.getMonth(); 
      const year = date.getFullYear();

      let schooljaar = '';
      if (month >= 8) {
        schooljaar = `${year}-${year + 1}`;
        l.maandIndex = month - 8; // Sep = index 0
      } else {
        schooljaar = `${year - 1}-${year}`;
        l.maandIndex = month + 4; // Jan = index 4
      }
      
      jaren.add(schooljaar); // Set zorgt dat 'huidigSchooljaar' hier niet dubbel in komt
      l.schooljaar = schooljaar;
    });

    // Sorteer de jaren van nieuw naar oud (zodat het nieuwste jaar altijd bovenaan staat)
    const gesorteerdeJaren = Array.from(jaren).sort().reverse();
    this.beschikbareSchooljaren.set(gesorteerdeJaren);
    
    // Selecteer automatisch het nieuwste/huidige schooljaar bij het openen van de pagina
    if (gesorteerdeJaren.length > 0) {
      this.geselecteerdSchooljaar.set(gesorteerdeJaren[0]);
    }
  }

  // Haalt alleen leningen op van het gekozen jaar in de dropdown
  leningenHuidigSchooljaar = computed(() => {
    return this.alleLeningen().filter(l => l.schooljaar === this.geselecteerdSchooljaar());
  });

  // Berekent wat de hoogste staaf in de grafiek is voor een mooie schaalverdeling
  maxBoekenPerMaand = computed(() => {
     const counts = new Array(12).fill(0);
     this.leningenHuidigSchooljaar().forEach(l => counts[l.maandIndex]++);
     return Math.max(...counts, 5); // Zorgt dat de grafiek as minstens tot 5 loopt
  });

  // Bouwt de data op voor de grafiek én de lijsten eronder
  dataPerMaand = computed(() => {
    return this.maandenNamen.map((naam, index) => {
      const leningenInMaand = this.leningenHuidigSchooljaar().filter(l => l.maandIndex === index);
      return {
        naam,
        aantal: leningenInMaand.length,
        percentage: (leningenInMaand.length / this.maxBoekenPerMaand()) * 100,
        leningen: leningenInMaand
      };
    });
  });
}