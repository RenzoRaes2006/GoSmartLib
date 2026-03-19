import { Injectable, Inject } from '@angular/core';
import { DOCUMENT } from '@angular/common';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ToegankelijkheidService {
  private isDyslexieActief = false;
  public dyslexieModus$ = new BehaviorSubject<boolean>(this.isDyslexieActief);

  constructor(@Inject(DOCUMENT) private document: Document) {}

  toggleDyslexieFont() {
    this.isDyslexieActief = !this.isDyslexieActief;

    if (this.isDyslexieActief) {
      this.document.body.classList.add('dyslexia-mode');
    } else {
      this.document.body.classList.remove('dyslexia-mode');
    }

    this.dyslexieModus$.next(this.isDyslexieActief);
  }

  get isActief(): boolean {
    return this.isDyslexieActief;
  }
}
