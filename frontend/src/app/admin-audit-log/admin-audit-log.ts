import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import {
  AuditActie,
  AuditLogEntry,
  AuditLogService,
} from '../services/audit-log.service';

const ACTIE_LABEL: Record<AuditActie, string> = {
  GEBRUIKER_TOEGEVOEGD: 'Gebruiker toegevoegd',
  GEBRUIKER_TOEGEVOEGD_SYNC: 'Gebruiker toegevoegd (sync)',
  GEBRUIKER_VERTROKKEN: 'Gebruiker vertrokken',
  GEBRUIKER_VERTROKKEN_SYNC: 'Gebruiker vertrokken (sync)',
  GEBRUIKER_GEREACTIVEERD: 'Gebruiker gereactiveerd',
  KLAS_GEWIJZIGD: 'Klas gewijzigd',
  KLAS_GEWIJZIGD_SYNC: 'Klas gewijzigd (sync)',
  ROLLOVER_UITGEVOERD: 'Rollover uitgevoerd',
  SCHOOL_ONBOARDED: 'School onboarded',
};

@Component({
  selector: 'app-admin-audit-log',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-audit-log.html',
  styleUrl: './admin-audit-log.css',
})
export class AdminAuditLog implements OnInit {
  private service = inject(AuditLogService);

  rijen = signal<AuditLogEntry[]>([]);
  laden = signal(false);
  fout = signal<string | null>(null);

  // Filters
  actie: AuditActie | '' = '';
  entiteitType = '';
  vanaf = '';
  tot = '';

  pagina = signal(0);
  pageSize = 25;
  totalPages = signal(1);
  totalElements = signal(0);

  alleActies = Object.keys(ACTIE_LABEL) as AuditActie[];

  ngOnInit(): void {
    this.laad();
  }

  actieLabel(actie: AuditActie): string {
    return ACTIE_LABEL[actie] ?? actie;
  }

  // JSON details proberen pretty-printen; valt terug op rauwe string als parsing faalt.
  parseDetails(json: string | null): Array<{ k: string; v: string }> {
    if (!json) return [];
    try {
      const obj = JSON.parse(json);
      return Object.entries(obj).map(([k, v]) => ({
        k,
        v: v == null ? '—' : String(v),
      }));
    } catch {
      return [{ k: 'raw', v: json }];
    }
  }

  laad(): void {
    this.laden.set(true);
    this.fout.set(null);
    this.service
      .zoek({
        actie: this.actie || null,
        entiteitType: this.entiteitType.trim() || null,
        vanaf: this.vanaf ? `${this.vanaf}T00:00:00` : null,
        tot: this.tot ? `${this.tot}T23:59:59` : null,
        page: this.pagina(),
        size: this.pageSize,
      })
      .subscribe({
        next: (res) => {
          this.rijen.set(res.content);
          this.totalPages.set(Math.max(1, res.totalPages));
          this.totalElements.set(res.totalElements);
          this.laden.set(false);
        },
        error: (err: HttpErrorResponse) => {
          this.fout.set(err.error?.message ?? 'Audit-log laden mislukt.');
          this.laden.set(false);
        },
      });
  }

  toepassen(): void {
    this.pagina.set(0);
    this.laad();
  }

  wis(): void {
    this.actie = '';
    this.entiteitType = '';
    this.vanaf = '';
    this.tot = '';
    this.toepassen();
  }

  vorige(): void {
    if (this.pagina() > 0) {
      this.pagina.update((p) => p - 1);
      this.laad();
    }
  }

  volgende(): void {
    if (this.pagina() < this.totalPages() - 1) {
      this.pagina.update((p) => p + 1);
      this.laad();
    }
  }
}
