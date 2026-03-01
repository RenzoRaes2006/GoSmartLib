import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators,
  AbstractControl,
  ValidationErrors,
} from '@angular/forms';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-boek-toevoegen',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterLink],
  templateUrl: './boek-toevoegen.html',
  styleUrl: './boek-toevoegen.css',
})
export class BoekToevoegen {
  boekForm;
  errorMessage: string = '';
  successMessage: string = '';

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
  ) {
    this.boekForm = this.fb.group({
      titel: ['', [Validators.required, Validators.minLength(2)]],
      auteur: ['', [Validators.required, Validators.minLength(2)]],
      isbn: ['', [Validators.required, this.isbnValidator]],
      uitgever: ['', [Validators.required, Validators.minLength(2)]],
      publicatieJaar: ['', [Validators.required, Validators.pattern(/^\d{4}$/)]],
      aantalPaginas: ['', [Validators.required, Validators.pattern(/^\d+$/)]],
      genre: ['', [Validators.required, Validators.minLength(2)]],
      beschrijving: ['', [Validators.required, Validators.minLength(10)]],
      aantalExemplaren: ['', [Validators.required, Validators.pattern(/^\d+$/)]],
      locatie: ['', [Validators.required, Validators.minLength(1)]],
      coverUrl: [
        '',
        [Validators.required, Validators.pattern(/^(https?:\/\/.*\.(?:png|jpg|jpeg|gif|svg))$/i)],
      ],
    });
  }

  // Custom ISBN validator
  isbnValidator(control: AbstractControl): ValidationErrors | null {
    const value = control.value;
    if (!value) return null;

    // Check of het 13 cijfers zijn
    if (!/^\d{13}$/.test(value)) {
      return { isbn: 'ISBN moet 13 cijfers bevatten' };
    }

    return null;
  }

  // Genereer ISBN-13
  genereerIsbn(): void {
    const prefix = '978';
    const landcode = '90';
    const basis12 = `${prefix}${landcode}${this.randomCijfers(7)}`;
    const isbn = `${basis12}${BoekToevoegen.berekenIsbn13CheckDigit(basis12)}`;

    this.boekForm.patchValue({ isbn });
    this.boekForm.get('isbn')?.markAsDirty();
    this.boekForm.get('isbn')?.markAsTouched();
    this.boekForm.get('isbn')?.updateValueAndValidity();
  }

  // Genereer X random cijfers
  private randomCijfers(lengte: number): string {
    return Array.from({ length: lengte }, () => Math.floor(Math.random() * 10)).join('');
  }

  // Bereken ISBN-13 checkdigit
  private static berekenIsbn13CheckDigit(basis12: string): string {
    // Gewogen som: 1,3,1,3,1,3...
    const som = basis12
      .split('')
      .map(Number)
      .reduce((acc, cijfer, i) => acc + cijfer * (i % 2 === 0 ? 1 : 3), 0);

    // Checkdigit zodat totaal deelbaar door 10 is
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

  onSubmit() {
    this.errorMessage = '';
    this.successMessage = '';

    if (this.boekForm.invalid) {
      this.boekForm.markAllAsTouched();
      return;
    } else {
      const nieuwBoek = this.boekForm.value;
      this.http.post('/api/boeken', nieuwBoek).subscribe({
        next: () => {
          console.log('Boek succesvol toegevoegd');
          this.successMessage = 'Boek succesvol toegevoegd!';
          this.boekForm.reset();
          setTimeout(() => (this.successMessage = ''), 5000);
        },
        error: (err) => {
          console.error('Fout bij het toevoegen van het boek:', err);

          if (err.status === 409) {
            this.errorMessage = 'Dit ISBN-nummer bestaat al in de database!';
          } else if (err.status === 400) {
            this.errorMessage = 'Ongeldige invoer. Controleer alle velden.';
          } else {
            this.errorMessage = 'Er is een fout opgetreden: ' + err.message;
          }
        },
      });
    }
  }
}
