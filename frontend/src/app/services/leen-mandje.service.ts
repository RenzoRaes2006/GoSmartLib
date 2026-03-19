import { Injectable } from '@angular/core';
import { Boek } from '../models/Boek';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class LeenMandjeService {
  private mandje: Boek[] = [];
  public mandje$ = new BehaviorSubject<Boek[]>(this.mandje);

  constructor() {}

  voegBoekToe(boek: Boek): string {
    // Max 3 boeken
    if (this.mandje.length >= 3) {
      return 'Je mag maximaal 3 boeken tegelijk reserveren.';
    }

    // Dubbel boek
    const zitErAlIn = this.mandje.find((b) => b.id === boek.id);
    if (zitErAlIn) {
      return 'Dit boek zit al in je reserveringen.';
    }

    // boek beschikbaar
    if (boek.aantalExemplaren <= 0) {
      return 'Dit boek is momenteel niet beschikbaar.';
    }

    this.mandje.push(boek);
    this.mandje$.next(this.mandje);
    return 'Succes';
  }

  verwijderBoek(boekId: number) {
    this.mandje = this.mandje.filter((b) => b.id !== boekId);
    this.mandje$.next(this.mandje);
  }

  getMandje() {
    return this.mandje;
  }

  maakMandjeLeeg() {
    this.mandje = [];
    this.mandje$.next(this.mandje);
  }
}
