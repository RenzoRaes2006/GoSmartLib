import { CommonModule } from '@angular/common';
import { Component, computed, OnInit, signal, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SchoolService } from '../services/School.Service';
import { AdminTags } from '../admin-tags/admin-tags';
import { AdminGebruiker, AdminGebruikerService } from '../services/AdminGebruikerService';

// static list of all official secondary schools under go! scholengroep antwerpen
const GO_ANTWERPEN_SCHOLEN = [
  { naam: 'GO! Atheneum Antwerpen', adres: 'Franklin Rooseveltplaats 11, 2060 Antwerpen', apiCode: 'ss-ant-001' },
  { naam: 'GO! Koninklijk Lyceum Antwerpen', adres: 'Kasteelpleinstraat 31, 2000 Antwerpen', apiCode: 'ss-lyc-002' },
  { naam: 'GO! Kunsthumaniora Antwerpen', adres: 'Karel Oomsstraat 24, 2018 Antwerpen', apiCode: 'ss-kun-003' },
  { naam: 'GO! Topsportschool Antwerpen', adres: 'Edegemsesteenweg 100, 2610 Wilrijk', apiCode: 'ss-top-004' },
  { naam: 'GO! Spectrumschool (Campus Ruggeveld)', adres: 'Ruggeveldlaan 496, 2100 Deurne', apiCode: 'ss-spe-005' },
  { naam: 'GO! Spectrumschool (Campus Plantin)', adres: 'Plantin en Moretuslei 165, 2140 Borgerhout', apiCode: 'ss-spp-006' },
  { naam: 'GO! Atheneum Deurne', adres: 'Hooftvunderlei 160, 2100 Deurne', apiCode: 'ss-deu-007' },
  { naam: 'GO! Atheneum Hoboken', adres: 'Lage Weg 100, 2660 Hoboken', apiCode: 'ss-hob-008' },
  { naam: 'GO! De Scheepvaartschool - Cenflumarin', adres: 'Gloriantlaan 75, 2050 Antwerpen', apiCode: 'ss-sch-009' },
  { naam: 'GO! Atheneum Schoten', adres: 'Villerslei 124, 2900 Schoten', apiCode: 'ss-sct-010' },
  { naam: 'GO! Atheneum Brasschaat', adres: 'Augustijnslei 46, 2930 Brasschaat', apiCode: 'ss-bra-011' },
  { naam: 'GO! KTA Kapellen', adres: 'Pastoor Vandenhoudtstraat 8, 2950 Kapellen', apiCode: 'ss-kap-012' },
  { naam: 'GO! Middenschool Centrum Antwerpen', adres: 'Lange Nieuwstraat 105, 2000 Antwerpen', apiCode: 'ss-mid-013' },
  { naam: 'GO! KMS (Koninklijke Middenschool)', adres: 'Kasteelpleinstraat 31, 2000 Antwerpen', apiCode: 'ss-kms-014' },
  { naam: 'GO! De Es (Buitengewoon secundair)', adres: 'Karel Oomsstraat 24, 2018 Antwerpen', apiCode: 'ss-es-015' },
  { naam: 'GO! SBSO Zonnebos (Campus Brasschaat)', adres: 'Sint-Antoniuslei 95, 2930 Brasschaat', apiCode: 'ss-zob-016' },
  { naam: 'GO! SBSO Zonnebos (Campus Antwerpen)', adres: 'Karel Oomsstraat 24, 2018 Antwerpen', apiCode: 'ss-zoa-017' }
];

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, AdminTags],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css'
})
export class AdminDashboard implements OnInit {

  // inject the service to communicate with spring boot
  private schoolService = inject(SchoolService);
  private adminGebruikerService = inject(AdminGebruikerService);

  // tab navigation state
  activeTab = signal<'scholen' | 'wizard' | 'tags' | 'rollen'>('scholen');

  // rollen-tab state
  leerkrachten = signal<AdminGebruiker[]>([]);
  leerkrachtenLaden = signal<boolean>(false);
  promotieBezig = signal<number | null>(null);

  // state for the selected school detail view
  geselecteerdeSchool = signal<any>(null);

  // temporary memory for live smartschool data
  liveKlassen = signal<any[]>([]);
  liveLeerlingen = signal<any[]>([]);
  isDataAanHetLaden = signal<boolean>(false);

  // wizard state
  huidigeStap = signal<number>(1);
  nieuweSchool = signal({ naam: '', adres: '', apiCode: '' });

  // actual database records from spring boot
  geregistreerdeScholen = signal<any[]>([]);

  // computed signal: returns only schools that are not yet registered
  beschikbareScholen = computed(() => {
    const scholen = this.geregistreerdeScholen() || [];
    const geregistreerdeNamen = scholen.map(s => s?.naam).filter(Boolean);
    return GO_ANTWERPEN_SCHOLEN.filter(school => !geregistreerdeNamen.includes(school.naam));
  });

  ngOnInit() {
    // fetch saved schools from the spring boot backend on startup
    this.laadScholenUitDatabase();
  }

  laadScholenUitDatabase() {
    this.schoolService.getAlleScholen().subscribe({
      next: (scholen) => {
        if (Array.isArray(scholen)) {
          this.geregistreerdeScholen.set(scholen);
        } else {
          this.geregistreerdeScholen.set([]);
        }
      },
      error: (err) => {
        console.error('error fetching schools from backend:', err);
        this.geregistreerdeScholen.set([]);
      }
    });
  }

  wisselTab(tab: 'scholen' | 'wizard' | 'tags' | 'rollen') {
    this.activeTab.set(tab);
    this.sluitSchoolDetails();
    if (tab === 'rollen') {
      this.laadLeerkrachten();
    }
  }

  laadLeerkrachten() {
    this.leerkrachtenLaden.set(true);
    this.adminGebruikerService.getAlleLeerkrachten().subscribe({
      next: (lijst) => {
        this.leerkrachten.set(lijst ?? []);
        this.leerkrachtenLaden.set(false);
      },
      error: (err) => {
        console.error('error fetching teachers:', err);
        this.leerkrachten.set([]);
        this.leerkrachtenLaden.set(false);
      }
    });
  }

  toggleBeheerder(leerkracht: AdminGebruiker) {
    const nieuweWaarde = !leerkracht.isBeheerder;
    const actie = nieuweWaarde ? 'beheerder maken' : 'de beheerder-rol intrekken bij';
    const bevestigd = confirm(`Weet je zeker dat je ${leerkracht.smartschoolId} wilt ${actie}?`);
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

  // fetch live data from smartschool when editing a school
  bekijkSchoolDetails(school: any) {
    this.geselecteerdeSchool.set(school);
    this.isDataAanHetLaden.set(true);

    this.schoolService.getLiveSmartschoolData(school.id).subscribe({
      next: (response) => {
        this.liveKlassen.set(response.liveSmartschoolData.klassen);
        this.liveLeerlingen.set(response.liveSmartschoolData.leerlingen);
        this.isDataAanHetLaden.set(false);
      },
      error: (err) => {
        console.error('error fetching live smartschool data:', err);
        this.isDataAanHetLaden.set(false);
      }
    });
  }

  sluitSchoolDetails() {
    this.geselecteerdeSchool.set(null);
    // clear memory immediately
    this.liveKlassen.set([]);
    this.liveLeerlingen.set([]);
  }

  volgendeStap() { this.huidigeStap.update(s => s + 1); }
  vorigeStap() { this.huidigeStap.update(s => s - 1); }

  startSchoolOp() {
    const schoolData = this.nieuweSchool();

    // only save the basic info
    const nieuweSchoolRecord = {
      naam: schoolData.naam,
      adres: schoolData.adres,
      apiCode: schoolData.apiCode
    };

    console.log('sending school to spring boot...', nieuweSchoolRecord);

    // use the service to talk to spring boot
    this.schoolService.maakSchoolAan(nieuweSchoolRecord).subscribe({
      next: (opgeslagenSchool) => {
        console.log('successfully saved in db:', opgeslagenSchool);

        // update the ui
        this.geregistreerdeScholen.update(scholen => [...scholen, opgeslagenSchool]);

        // reset wizard and return to overview
        this.huidigeStap.set(1);
        this.nieuweSchool.set({ naam: '', adres: '', apiCode: '' });
        this.wisselTab('scholen');
      },
      error: (err) => {
        console.error('error saving school to backend:', err);
        alert('Kan de school niet opslaan. Draait je backend wel?');
      }
    });
  }

  // handle the select change safely via ngmodel
  onSchoolGeselecteerd(geselecteerdeNaam: string) {
    const school = this.beschikbareScholen().find(s => s.naam === geselecteerdeNaam);

    if (school) {
      this.nieuweSchool.set({
        naam: school.naam,
        adres: school.adres,
        apiCode: school.apiCode
      });
    } else {
      this.nieuweSchool.set({ naam: '', adres: '', apiCode: '' });
    }
  }

  // delete a school after user confirmation
  verwijderSchool(school: any) {
    const isConfirmed = confirm(`Weet je zeker dat je ${school.naam} wilt verwijderen?`);

    if (isConfirmed) {
      this.schoolService.verwijderSchool(school.id).subscribe({
        next: () => {
          console.log(`successfully deleted ${school.naam}`);
          // remove the deleted school from the local ui list
          this.geregistreerdeScholen.update(scholen => scholen.filter(s => s.id !== school.id));
        },
        error: (err) => {
          console.error('error deleting school:', err);
          alert('Kan de school niet verwijderen. Draait je backend?');
        }
      });
    }
  }
}
