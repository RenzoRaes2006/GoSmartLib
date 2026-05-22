import { CommonModule } from '@angular/common';
import { Component, computed, OnInit, signal, inject } from '@angular/core';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators, FormArray } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { SchoolService } from '../services/School.Service';
import { AdminTags } from '../admin-tags/admin-tags';
import { AdminSchooljaar } from '../admin-schooljaar/admin-schooljaar';
import { AdminGebruiker, AdminGebruikerService, GebruikerCreatePayload } from '../services/AdminGebruikerService';

// static list of all official secondary schools under go! scholengroep antwerpen
const GO_ANTWERPEN_SCHOLEN = [
  { naam: 'AP Hogeschool Antwerpen', adres: 'Noorderplaats 2, 2000 Antwerpen' },
  { naam: 'GO! Atheneum Antwerpen', adres: 'Franklin Rooseveltplaats 11, 2060 Antwerpen' },
  { naam: 'GO! Koninklijk Lyceum Antwerpen', adres: 'Kasteelpleinstraat 31, 2000 Antwerpen' },
  { naam: 'GO! Kunsthumaniora Antwerpen', adres: 'Karel Oomsstraat 24, 2018 Antwerpen' },
  { naam: 'GO! Topsportschool Antwerpen', adres: 'Edegemsesteenweg 100, 2610 Wilrijk' },
  { naam: 'GO! Spectrumschool (Campus Ruggeveld)', adres: 'Ruggeveldlaan 496, 2100 Deurne' },
  { naam: 'GO! Spectrumschool (Campus Plantin)', adres: 'Plantin en Moretuslei 165, 2140 Borgerhout' },
  { naam: 'GO! Atheneum Deurne', adres: 'Hooftvunderlei 160, 2100 Deurne' },
  { naam: 'GO! Atheneum Hoboken', adres: 'Lage Weg 100, 2660 Hoboken' },
  { naam: 'GO! De Scheepvaartschool - Cenflumarin', adres: 'Gloriantlaan 75, 2050 Antwerpen' },
  { naam: 'GO! Atheneum Schoten', adres: 'Villerslei 124, 2900 Schoten' },
  { naam: 'GO! Atheneum Brasschaat', adres: 'Augustijnslei 46, 2930 Brasschaat' },
  { naam: 'GO! KTA Kapellen', adres: 'Pastoor Vandenhoudtstraat 8, 2950 Kapellen' },
  { naam: 'GO! Middenschool Centrum Antwerpen', adres: 'Lange Nieuwstraat 105, 2000 Antwerpen' },
  { naam: 'GO! KMS (Koninklijke Middenschool)', adres: 'Kasteelpleinstraat 31, 2000 Antwerpen' },
  { naam: 'GO! De Es (Buitengewoon secundair)', adres: 'Karel Oomsstraat 24, 2018 Antwerpen' },
  { naam: 'GO! SBSO Zonnebos (Campus Brasschaat)', adres: 'Sint-Antoniuslei 95, 2930 Brasschaat' },
  { naam: 'GO! SBSO Zonnebos (Campus Antwerpen)', adres: 'Karel Oomsstraat 24, 2018 Antwerpen' }
];

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, AdminTags, AdminSchooljaar],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css'
})
export class AdminDashboard implements OnInit {

  private schoolService = inject(SchoolService);
  private adminGebruikerService = inject(AdminGebruikerService);
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);

  // dezelfde URL-resolutie als SchoolService gebruikt
  private apiBase = window.location.hostname === 'localhost'
    ? 'http://localhost:8081/api/scholen'
    : 'https://api-sp09.cloud-ap.be/api/scholen';

  // tab navigation state
  activeTab = signal<'scholen' | 'wizard' | 'tags' | 'rollen' | 'schooljaar' | 'gebruikers'>('scholen');

  // rollen-tab state (geen paging — toon volledige leerkrachten-lijst)
  leerkrachten = signal<AdminGebruiker[]>([]);
  leerkrachtenLaden = signal<boolean>(false);
  promotieBezig = signal<number | null>(null);
  geselecteerdeRollenSchoolId = signal<number | null>(null);

  // gebruikers-tab state (lifecycle: nieuwe leerling/leerkracht + markeer vertrokken)
  gebruikersSchoolId = signal<number | null>(null);
  lifecycleLeerlingen = signal<AdminGebruiker[]>([]);
  lifecycleLeerkrachten = signal<AdminGebruiker[]>([]);
  lifecycleLaden = signal<boolean>(false);
  lifecycleFout = signal<string>('');
  vertrekBezig = signal<number | null>(null);
  nieuweLeerlingFormOpen = signal<boolean>(false);
  nieuweLeerkrachtFormOpen = signal<boolean>(false);
  nieuweGebruikerSmartschoolId = signal<string>('');
  nieuweGebruikerKlas = signal<string>('');
  nieuweGebruikerBezig = signal<boolean>(false);
  beschikbareKlassenVoorSchool = signal<string[]>([]);

  // confirm-modal voor vertrek (vervangt browser confirm())
  vertrekModalDoel = signal<{ gebruiker: AdminGebruiker; rol: 'LEERLING' | 'LEERKRACHT' } | null>(null);

  // confirm-modal voor reactiveer (vertrokken → actief)
  reactiveerModalDoel = signal<{ gebruiker: AdminGebruiker; rol: 'LEERLING' | 'LEERKRACHT' } | null>(null);
  reactiveerBezig = signal<number | null>(null);

  // Auto-dismiss timer voor lifecycle-foutmelding (5s na laatste set met inhoud).
  private lifecycleFoutTimer: any = null;
  toonLifecycleFout(msg: string) {
    if (this.lifecycleFoutTimer) {
      clearTimeout(this.lifecycleFoutTimer);
      this.lifecycleFoutTimer = null;
    }
    this.lifecycleFout.set(msg);
    if (msg) {
      this.lifecycleFoutTimer = setTimeout(() => {
        this.lifecycleFout.set('');
        this.lifecycleFoutTimer = null;
      }, 5000);
    }
  }

  // server-side paginering voor de twee lifecycle-tabellen
  readonly gebruikersItemsPerPagina = 5;
  leerlingenPage = signal<number>(0);
  leerlingenTotaalPaginas = signal<number>(1);
  leerkrachtenPage = signal<number>(0);
  leerkrachtenTotaalPaginas = signal<number>(1);

  // state for the list of saved schools
  geregistreerdeScholen = signal<any[]>([]);

  // wizard state
  huidigeStapWizard = signal<number>(1);
  totaleStappen = 5;
  bezig = signal<boolean>(false);
  foutmelding = signal<string>('');

  verbindingStatus = signal<'idle' | 'ok' | 'fout' | 'bezig'>('idle');
  verbindingDetail = signal<string>('');
  previewData = signal<{
    aantalKlassen: number;
    aantalGebruikers: number;
    aantalLeerlingen: number;
    aantalLeerkrachten: number;
  } | null>(null);
  onboardSucces = signal<boolean>(false);

  // edit state voor campussen per school
  bewerkSchoolId = signal<number | null>(null);
  bewerkCampussen = signal<{ id: number; naam: string }[]>([]);
  nieuweCampusNaam = signal<string>('');
  bewerkFout = signal<string>('');
  syncBezig = signal<boolean>(false);
  syncResultaat = signal<string>('');

  wizardForm: FormGroup = this.fb.group({
    stap1: this.fb.group({
      schoolNaam: ['', Validators.required],
      adres: ['', Validators.required],
      smartschoolDomein: ['', Validators.required],
      oauthClientId: ['', Validators.required],
      oauthClientSecret: ['', Validators.required]
    }),
    stap3: this.fb.group({
      campussen: this.fb.array([this.maakCampusVeld()])
    })
  });

  beschikbareScholen = computed(() => {
    const scholen = this.geregistreerdeScholen() || [];
    const geregistreerdeNamen = scholen.map(s => s?.naam).filter(Boolean);
    return GO_ANTWERPEN_SCHOLEN.filter(school => !geregistreerdeNamen.includes(school.naam));
  });

  ngOnInit() {
    this.laadScholenUitDatabase();
  }

  // --- campus form array helpers ---
  get campussen() {
    return this.wizardForm.get('stap3.campussen') as FormArray;
  }

  maakCampusVeld(): FormGroup {
    return this.fb.group({ naam: ['', Validators.required] });
  }

  voegCampusToe() {
    this.campussen.push(this.maakCampusVeld());
  }

  verwijderCampus(index: number) {
    if (this.campussen.length > 1) {
      this.campussen.removeAt(index);
    }
  }

  onSchoolGeselecteerd(event: Event) {
    const select = event.target as HTMLSelectElement;
    const school = this.beschikbareScholen().find(s => s.naam === select.value);
    if (school) {
      this.wizardForm.get('stap1')?.patchValue({
        schoolNaam: school.naam,
        adres: school.adres
      });
    }
  }

  // --- wizard navigation ---
  volgendeStapWizard() {
    this.foutmelding.set('');
    if (this.huidigeStapWizard() < this.totaleStappen) {
      this.huidigeStapWizard.set(this.huidigeStapWizard() + 1);
    }
  }

  vorigeStapWizard() {
    this.foutmelding.set('');
    if (this.huidigeStapWizard() > 1) {
      this.huidigeStapWizard.set(this.huidigeStapWizard() - 1);
    }
  }

  // --- step 2: test verbinding ---
  testVerbinding() {
    this.foutmelding.set('');
    this.verbindingStatus.set('bezig');
    const v = this.wizardForm.value.stap1;
    this.http.post<any>(`${this.apiBase}/test-verbinding`, {
      smartschoolDomein: v.smartschoolDomein,
      oauthClientId: v.oauthClientId,
      oauthClientSecret: v.oauthClientSecret
    }).subscribe({
      next: (res) => {
        this.verbindingStatus.set(res?.success ? 'ok' : 'fout');
        this.verbindingDetail.set(res?.detail ?? '');
      },
      error: (err) => {
        this.verbindingStatus.set('fout');
        this.verbindingDetail.set(err?.error?.detail ?? err?.message ?? '');
      }
    });
  }

  // --- step 4: import preview ---
  haalPreviewOp() {
    this.foutmelding.set('');
    this.bezig.set(true);
    const v = this.wizardForm.value.stap1;
    this.http.post<any>(`${this.apiBase}/preview`, {
      smartschoolDomein: v.smartschoolDomein,
      oauthClientId: v.oauthClientId,
      oauthClientSecret: v.oauthClientSecret
    }).subscribe({
      next: (res) => { this.previewData.set(res); this.bezig.set(false); },
      error: () => { this.foutmelding.set('Kon preview niet ophalen.'); this.bezig.set(false); }
    });
  }

  // --- step 5: bevestigen → transactionele import ---
  bevestigEnImporteer() {
    this.bezig.set(true);
    this.foutmelding.set('');
    const v = this.wizardForm.value;
    const payload = {
      schoolNaam: v.stap1.schoolNaam,
      adres: v.stap1.adres,
      smartschoolDomein: v.stap1.smartschoolDomein,
      oauthClientId: v.stap1.oauthClientId,
      oauthClientSecret: v.stap1.oauthClientSecret,
      campussen: v.stap3.campussen
    };
    this.http.post<any>(`${this.apiBase}/onboard`, payload).subscribe({
      next: () => {
        this.bezig.set(false);
        this.onboardSucces.set(true);
        setTimeout(() => this.resetWizard(), 1500);
      },
      error: (err) => {
        this.foutmelding.set(err?.error?.error ?? 'Er is een fout opgetreden bij het opslaan.');
        this.bezig.set(false);
      }
    });
  }

  private resetWizard() {
    this.huidigeStapWizard.set(1);
    this.verbindingStatus.set('idle');
    this.verbindingDetail.set('');
    this.previewData.set(null);
    this.onboardSucces.set(false);
    this.wizardForm.reset();
    this.campussen.clear();
    this.campussen.push(this.maakCampusVeld());
    this.laadScholenUitDatabase();
    this.wisselTab('scholen');
  }

  // --- general logic ---
  laadScholenUitDatabase() {
    this.schoolService.getAlleScholen().subscribe({
      next: (scholen) => this.geregistreerdeScholen.set(Array.isArray(scholen) ? scholen : []),
      error: () => this.geregistreerdeScholen.set([])
    });
  }

  wisselTab(tab: 'scholen' | 'wizard' | 'tags' | 'rollen' | 'schooljaar' | 'gebruikers') {
    this.activeTab.set(tab);
    this.sluitBewerken(); // Sluit campussen editor indien open
    if (tab === 'rollen') {
      // Zorg dat scholenlijst klaar is voor de dropdown
      if (this.geregistreerdeScholen().length === 0) {
        this.laadScholenUitDatabase();
      }
      this.leerkrachten.set([]);
    }
    if (tab === 'gebruikers') {
      if (this.geregistreerdeScholen().length === 0) {
        this.laadScholenUitDatabase();
      }
      this.lifecycleLeerlingen.set([]);
      this.lifecycleLeerkrachten.set([]);
      this.gebruikersSchoolId.set(null);
      this.leerlingenPage.set(0);
      this.leerkrachtenPage.set(0);
      this.sluitNieuweGebruikerForms();
    }
  }

  // --- gebruikers (lifecycle) tab ---
  onGebruikersSchoolGeselecteerd(event: Event) {
    const value = (event.target as HTMLSelectElement).value;
    const schoolId = value ? Number(value) : null;
    this.gebruikersSchoolId.set(schoolId);
    this.sluitNieuweGebruikerForms();
    if (schoolId != null) {
      this.laadLifecycleGebruikers(schoolId, true);
      this.laadKlassenVoorSchool(schoolId);
    } else {
      this.lifecycleLeerlingen.set([]);
      this.lifecycleLeerkrachten.set([]);
      this.leerlingenTotaalPaginas.set(1);
      this.leerkrachtenTotaalPaginas.set(1);
      this.beschikbareKlassenVoorSchool.set([]);
    }
  }

  private laadKlassenVoorSchool(schoolId: number) {
    this.adminGebruikerService.getKlassenVoorSchool(schoolId).subscribe({
      next: (klassen) => this.beschikbareKlassenVoorSchool.set(klassen ?? []),
      error: () => this.beschikbareKlassenVoorSchool.set([]),
    });
  }

  private laadLifecycleGebruikers(schoolId: number, resetPaging = false) {
    if (resetPaging) {
      this.leerlingenPage.set(0);
      this.leerkrachtenPage.set(0);
    }
    this.lifecycleLaden.set(true);
    this.toonLifecycleFout('');
    this.laadLifecycleLeerlingenPagina(schoolId);
    this.laadLifecycleLeerkrachtenPagina(schoolId);
  }

  private laadLifecycleLeerlingenPagina(schoolId: number) {
    this.adminGebruikerService
      .getLeerlingenInclVertrokken(schoolId, this.leerlingenPage(), this.gebruikersItemsPerPagina)
      .subscribe({
        next: (page) => {
          this.lifecycleLeerlingen.set(page.content ?? []);
          this.leerlingenTotaalPaginas.set(Math.max(1, page.totalPages ?? 1));
          this.lifecycleLaden.set(false);
        },
        error: (err) => {
          this.toonLifecycleFout(err?.error ?? 'Kon leerlingen niet ophalen.');
          this.lifecycleLaden.set(false);
        },
      });
  }

  private laadLifecycleLeerkrachtenPagina(schoolId: number) {
    this.adminGebruikerService
      .getAlleLeerkrachten(schoolId, true, this.leerkrachtenPage(), this.gebruikersItemsPerPagina)
      .subscribe({
        next: (page) => {
          this.lifecycleLeerkrachten.set(page.content ?? []);
          this.leerkrachtenTotaalPaginas.set(Math.max(1, page.totalPages ?? 1));
          this.lifecycleLaden.set(false);
        },
        error: (err) => {
          this.toonLifecycleFout(err?.error ?? 'Kon leerkrachten niet ophalen.');
          this.lifecycleLaden.set(false);
        },
      });
  }

  vorigeLeerlingenPagina() {
    const schoolId = this.gebruikersSchoolId();
    if (schoolId == null || this.leerlingenPage() === 0) return;
    this.leerlingenPage.update((p) => p - 1);
    this.laadLifecycleLeerlingenPagina(schoolId);
  }
  volgendeLeerlingenPagina() {
    const schoolId = this.gebruikersSchoolId();
    if (schoolId == null || this.leerlingenPage() >= this.leerlingenTotaalPaginas() - 1) return;
    this.leerlingenPage.update((p) => p + 1);
    this.laadLifecycleLeerlingenPagina(schoolId);
  }
  vorigeLeerkrachtenPagina() {
    const schoolId = this.gebruikersSchoolId();
    if (schoolId == null || this.leerkrachtenPage() === 0) return;
    this.leerkrachtenPage.update((p) => p - 1);
    this.laadLifecycleLeerkrachtenPagina(schoolId);
  }
  volgendeLeerkrachtenPagina() {
    const schoolId = this.gebruikersSchoolId();
    if (schoolId == null || this.leerkrachtenPage() >= this.leerkrachtenTotaalPaginas() - 1) return;
    this.leerkrachtenPage.update((p) => p + 1);
    this.laadLifecycleLeerkrachtenPagina(schoolId);
  }

  openNieuweLeerlingForm() {
    this.nieuweLeerlingFormOpen.set(true);
    this.nieuweLeerkrachtFormOpen.set(false);
    this.nieuweGebruikerSmartschoolId.set('');
    this.nieuweGebruikerKlas.set('');
  }

  openNieuweLeerkrachtForm() {
    this.nieuweLeerkrachtFormOpen.set(true);
    this.nieuweLeerlingFormOpen.set(false);
    this.nieuweGebruikerSmartschoolId.set('');
    this.nieuweGebruikerKlas.set('');
  }

  sluitNieuweGebruikerForms() {
    this.nieuweLeerlingFormOpen.set(false);
    this.nieuweLeerkrachtFormOpen.set(false);
    this.nieuweGebruikerSmartschoolId.set('');
    this.nieuweGebruikerKlas.set('');
    this.toonLifecycleFout('');
  }

  bevestigNieuweGebruiker(rol: 'LEERLING' | 'LEERKRACHT') {
    const schoolId = this.gebruikersSchoolId();
    const smartschoolId = this.nieuweGebruikerSmartschoolId().trim();
    if (schoolId == null) return;
    if (!smartschoolId) {
      this.toonLifecycleFout('SmartschoolId is verplicht.');
      return;
    }
    // Leerkrachten hebben in OneRoster geen klas; alleen leerling-form vraagt 'm.
    const klas = rol === 'LEERLING' ? (this.nieuweGebruikerKlas().trim() || null) : null;
    const payload: GebruikerCreatePayload = {
      smartschoolId,
      schoolId,
      klas
    };
    this.nieuweGebruikerBezig.set(true);
    this.toonLifecycleFout('');
    const obs = rol === 'LEERLING'
      ? this.adminGebruikerService.voegLeerlingToe(payload)
      : this.adminGebruikerService.voegLeerkrachtToe(payload);
    obs.subscribe({
      next: () => {
        this.nieuweGebruikerBezig.set(false);
        this.sluitNieuweGebruikerForms();
        // Spring naar pagina 0 zodat de nieuwe rij bovenaan zichtbaar is
        // (sortering: actief eerst, daarna alfabetisch).
        if (rol === 'LEERLING') {
          this.leerlingenPage.set(0);
          this.laadLifecycleLeerlingenPagina(schoolId);
        } else {
          this.leerkrachtenPage.set(0);
          this.laadLifecycleLeerkrachtenPagina(schoolId);
        }
      },
      error: (err) => {
        this.nieuweGebruikerBezig.set(false);
        this.toonLifecycleFout(err?.error ?? 'Toevoegen mislukt.');
      }
    });
  }

  openVertrekModal(gebruiker: AdminGebruiker, rol: 'LEERLING' | 'LEERKRACHT') {
    this.toonLifecycleFout('');
    this.vertrekModalDoel.set({ gebruiker, rol });
  }

  annuleerVertrekModal() {
    if (this.vertrekBezig() !== null) return;
    this.vertrekModalDoel.set(null);
  }

  openReactiveerModal(gebruiker: AdminGebruiker, rol: 'LEERLING' | 'LEERKRACHT') {
    this.toonLifecycleFout('');
    this.reactiveerModalDoel.set({ gebruiker, rol });
  }

  annuleerReactiveerModal() {
    if (this.reactiveerBezig() !== null) return;
    this.reactiveerModalDoel.set(null);
  }

  bevestigReactiveer() {
    const doel = this.reactiveerModalDoel();
    if (!doel) return;
    const { gebruiker, rol } = doel;

    this.reactiveerBezig.set(gebruiker.id);
    this.toonLifecycleFout('');
    const obs = rol === 'LEERLING'
      ? this.adminGebruikerService.reactiveerLeerling(gebruiker.id)
      : this.adminGebruikerService.reactiveerLeerkracht(gebruiker.id);
    obs.subscribe({
      next: () => {
        this.reactiveerBezig.set(null);
        this.reactiveerModalDoel.set(null);
        const schoolId = this.gebruikersSchoolId();
        if (schoolId != null) {
          if (rol === 'LEERLING') this.laadLifecycleLeerlingenPagina(schoolId);
          else this.laadLifecycleLeerkrachtenPagina(schoolId);
        }
      },
      error: (err) => {
        this.reactiveerBezig.set(null);
        this.reactiveerModalDoel.set(null);
        this.toonLifecycleFout(err?.error ?? 'Reactiveren mislukt.');
      },
    });
  }

  bevestigVertrek() {
    const doel = this.vertrekModalDoel();
    if (!doel) return;
    const { gebruiker, rol } = doel;

    this.vertrekBezig.set(gebruiker.id);
    this.toonLifecycleFout('');
    const obs = rol === 'LEERLING'
      ? this.adminGebruikerService.markeerLeerlingVertrokken(gebruiker.id)
      : this.adminGebruikerService.markeerLeerkrachtVertrokken(gebruiker.id);
    obs.subscribe({
      next: () => {
        this.vertrekBezig.set(null);
        this.vertrekModalDoel.set(null);
        const schoolId = this.gebruikersSchoolId();
        if (schoolId != null) {
          // Bewust niet resetten — we blijven op dezelfde pagina.
          if (rol === 'LEERLING') this.laadLifecycleLeerlingenPagina(schoolId);
          else this.laadLifecycleLeerkrachtenPagina(schoolId);
        }
      },
      error: (err) => {
        this.vertrekBezig.set(null);
        this.vertrekModalDoel.set(null);
        this.toonLifecycleFout(err?.error ?? 'Markeren mislukt.');
      }
    });
  }

  onRollenSchoolGeselecteerd(event: Event) {
    const value = (event.target as HTMLSelectElement).value;
    const schoolId = value ? Number(value) : null;
    this.geselecteerdeRollenSchoolId.set(schoolId);
    if (schoolId != null) {
      this.laadLeerkrachten(schoolId);
    } else {
      this.leerkrachten.set([]);
    }
  }

  laadLeerkrachten(schoolId: number) {
    this.leerkrachtenLaden.set(true);
    // Geen paginering in deze tab: vraag een ruime size zodat alle leerkrachten van de
    // school in één call terugkomen. 10 000 is ruim boven elk realistisch aantal.
    this.adminGebruikerService.getAlleLeerkrachten(schoolId, false, 0, 10000).subscribe({
      next: (page) => {
        this.leerkrachten.set(page.content ?? []);
        this.leerkrachtenLaden.set(false);
      },
      error: (err) => {
        console.error('error fetching teachers:', err);
        this.leerkrachten.set([]);
        this.leerkrachtenLaden.set(false);
      },
    });
  }

  toggleBeheerder(leerkracht: AdminGebruiker) {
    const nieuweWaarde = !leerkracht.isBeheerder;
    const actie = nieuweWaarde ? 'beheerder maken' : 'de beheerder-rol intrekken bij';
    const wie = leerkracht.naam || leerkracht.smartschoolId;
    const bevestigd = confirm(`Weet je zeker dat je ${wie} wilt ${actie}?`);
    if (!bevestigd) return;

    this.promotieBezig.set(leerkracht.id);
    this.adminGebruikerService.setBeheerder(leerkracht.id, nieuweWaarde).subscribe({
      next: (geupdate) => {
        this.leerkrachten.update(lijst =>
          lijst.map(l => (l.id === geupdate.id ? geupdate : l))
        );
        this.promotieBezig.set(null);
      },
      error: (err) => {
        console.error('error toggling beheerder:', err);
        alert('Kan de status niet wijzigen. Probeer opnieuw.');
        this.promotieBezig.set(null);
      }
    });
  }

  // --- bewerk campussen van een geregistreerde school ---
  bewerkCampussenVoor(school: any) {
    if (this.bewerkSchoolId() === school.id) {
      this.sluitBewerken();
      return;
    }
    this.bewerkSchoolId.set(school.id);
    this.bewerkFout.set('');
    this.nieuweCampusNaam.set('');
    this.laadCampussen(school.id);
  }

  sluitBewerken() {
    this.bewerkSchoolId.set(null);
    this.bewerkCampussen.set([]);
    this.nieuweCampusNaam.set('');
    this.bewerkFout.set('');
    this.syncResultaat.set('');
  }

  private laadCampussen(schoolId: number) {
    this.http.get<{ id: number; naam: string }[]>(`${this.apiBase}/${schoolId}/campussen`).subscribe({
      next: (lijst) => this.bewerkCampussen.set(lijst ?? []),
      error: () => this.bewerkFout.set('Kon campussen niet ophalen.')
    });
  }

  voegCampusToeAanBestaande() {
    const schoolId = this.bewerkSchoolId();
    const naam = this.nieuweCampusNaam().trim();
    if (schoolId == null || !naam) return;
    this.bewerkFout.set('');
    this.http.post<{ id: number; naam: string }>(
      `${this.apiBase}/${schoolId}/campussen`, { naam }).subscribe({
      next: (campus) => {
        this.bewerkCampussen.update(lijst => [...lijst, campus]);
        this.nieuweCampusNaam.set('');
      },
      error: (err) => this.bewerkFout.set(err?.error?.error ?? 'Toevoegen mislukt.')
    });
  }

  syncGebruikersVanSchool() {
    const schoolId = this.bewerkSchoolId();
    if (schoolId == null) return;
    this.syncBezig.set(true);
    this.syncResultaat.set('');
    this.bewerkFout.set('');
    this.http.post<any>(`${this.apiBase}/${schoolId}/sync-gebruikers`, {}).subscribe({
      next: (res) => {
        const nieuw = (res.leerlingenNieuw ?? 0) + (res.leerkrachtenNieuw ?? 0);
        const geupdatet = (res.leerlingenGeupdatet ?? 0) + (res.leerkrachtenGeupdatet ?? 0);
        const ongewijzigd = (res.leerlingenOngewijzigd ?? 0) + (res.leerkrachtenOngewijzigd ?? 0);
        this.syncResultaat.set(`✓ ${nieuw} nieuw, ${geupdatet} bijgewerkt, ${ongewijzigd} ongewijzigd.`);
        this.syncBezig.set(false);
      },
      error: (err) => {
        this.bewerkFout.set(err?.error?.error ?? 'Sync mislukt.');
        this.syncBezig.set(false);
      }
    });
  }

  verwijderBestaandeCampus(campus: { id: number; naam: string }) {
    const schoolId = this.bewerkSchoolId();
    if (schoolId == null) return;
    if (!confirm(`Campus ${campus.naam} verwijderen?`)) return;
    this.bewerkFout.set('');
    this.http.delete(`${this.apiBase}/${schoolId}/campussen/${campus.id}`).subscribe({
      next: () => this.bewerkCampussen.update(lijst => lijst.filter(c => c.id !== campus.id)),
      error: (err) => this.bewerkFout.set(err?.error?.error ?? 'Verwijderen mislukt.')
    });
  }

  verwijderSchool(school: any) {
    if (confirm(`Weet je zeker dat je ${school.naam} wilt verwijderen?`)) {
      this.schoolService.verwijderSchool(school.id).subscribe({
        next: () => {
          this.geregistreerdeScholen.update(scholen => scholen.filter(s => s.id !== school.id));
        },
        error: () => alert('Fout bij het verwijderen van de school.')
      });
    }
  }
}