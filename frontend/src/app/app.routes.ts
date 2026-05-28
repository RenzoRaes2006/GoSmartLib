import { Routes } from '@angular/router';
import { Detail } from './detail/detail';
import { Catalogus } from './catalogus/catalogus';
import { Leeslijst } from './leeslijst/leeslijst';
import { BoekBeheer } from './boek-beheer/boek-beheer';
import { HomePage } from './home-page/home-page';
import { ReserverenComponent } from './reserveren/reserveren';
import { PageNotFound } from './page-not-found/page-not-found';
import { Login } from './login/login';
import { AanvragenBeheer } from './aanvragen-beheer/aanvragen-beheer';
import { AdminDashboard } from './admin-dashboard/admin-dashboard';
import { BoekImport } from './boek-import/boek-import';
import { GerapporteerdeReviews } from './gerapporteerde-reviews/gerapporteerde-reviews';
import { Leengeschiedenis } from './leengeschiedenis/leengeschiedenis';
import { Leerkrachtdashboard } from './leerkrachtdashboard/leerkrachtdashboard';
import { ScanModus } from './scan-modus/scan-modus';
import { LoginSuccessComponent } from './login-success/login-success';
import { adminGuard } from './guards/admin-guard';
import { beheerderGuard } from './guards/beheerder-guard';
import { authGuard } from './guards/auth.guard';
import { teacherGuard } from './guards/teacher-guard';
import { LeningenBeheerder } from './leningen-beheerder/leningen-beheerder';
import { BeheerderDashboard } from './beheerder-dashboard/beheerder-dashboard';
import { KlassLeeslijstenOverzichtComponent } from './klass-leeslijsten-overzicht/klass-leeslijsten-overzicht';
import { KlassLeeslijstBeheerComponent } from './klass-leeslijst-beheer/klass-leeslijst-beheer';
import { KlassLeeslijstenArchiefComponent } from './klass-leeslijsten-archief/klass-leeslijsten-archief';
import { BeheerderArchiefComponent } from './beheerder-archief/beheerder-archief';
import { BulkOntlenenComponent } from './bulk-ontlenen-component/bulk-ontlenen-component';
import { KlassenleeslijstenLeerlingComponent } from './klassenleeslijsten-leerling/klassenleeslijsten-leerling';
import { KlassenleeslijstDetailLeerlingComponent } from './klassenleeslijst-detail-leerling/klassenleeslijst-detail-leerling';
import { AdminLogin } from './admin-login/admin-login';
import { AdminAuditLog } from './admin-audit-log/admin-audit-log';
import { BeschadigdeBoeken } from './beschadigde-boeken/beschadigde-boeken';
import { BeheerderRollenComponent } from './beheerder-rollen/beheerder-rollen';

export const routes: Routes = [
  // Geen guard
  { path: 'login', component: Login },
  { path: 'admin-login', component: AdminLogin },
  { path: 'login-success', component: LoginSuccessComponent },
  { path: 'not-found', component: PageNotFound },

  // authGuard — ingelogde gebruikers
  { path: '', component: HomePage, canActivate: [authGuard] },
  { path: 'catalogus', component: Catalogus, canActivate: [authGuard] },
  { path: 'details/:id', component: Detail, canActivate: [authGuard] },
  { path: 'leeslijst', component: Leeslijst, canActivate: [authGuard] },
  { path: 'leeslijst/:id', component: Leeslijst, canActivate: [authGuard] },
  { path: 'reserveren', component: ReserverenComponent, canActivate: [authGuard] },
  { path: 'leengeschiedenis', component: Leengeschiedenis, canActivate: [authGuard] },
  { path: 'klassenleeslijsten', component: KlassenleeslijstenLeerlingComponent, canActivate: [authGuard] },
  { path: 'klassenleeslijsten/:id', component: KlassenleeslijstDetailLeerlingComponent, canActivate: [authGuard] },

  // teacherGuard — enkel leerkrachten
  { path: 'leerkracht-dashboard', component: Leerkrachtdashboard, canActivate: [teacherGuard] },
  { path: 'leeslijsten', component: KlassLeeslijstenOverzichtComponent, canActivate: [teacherGuard] },
  { path: 'leeslijsten/archief', component: KlassLeeslijstenArchiefComponent, canActivate: [teacherGuard] },
  { path: 'leeslijsten/:id', component: KlassLeeslijstBeheerComponent, canActivate: [teacherGuard] },
  { path: 'bulk-ontlenen', component: BulkOntlenenComponent, canActivate: [teacherGuard] },

  // adminGuard — enkel de applicatiebeheerder ('admin')
  { path: 'admin', component: AdminDashboard, canActivate: [adminGuard] },
  { path: 'admin/audit-log', component: AdminAuditLog, canActivate: [adminGuard] },

  // beheerderGuard — enkel de bibliotheekbeheerder ('beheerder')
  { path: 'beheerder', component: BeheerderDashboard, canActivate: [beheerderGuard] },
  { path: 'beheerder/archief', component: BeheerderArchiefComponent, canActivate: [beheerderGuard] },
  { path: 'beheer/aanvragen', component: AanvragenBeheer, canActivate: [beheerderGuard] },
  { path: 'beheer/scan', component: ScanModus, canActivate: [beheerderGuard] },
  { path: 'beheer/actieve-leningen', component: LeningenBeheerder, canActivate: [beheerderGuard] },
  { path: 'boek-beheer', component: BoekBeheer, canActivate: [beheerderGuard] },
  { path: 'boek-import', component: BoekImport, canActivate: [beheerderGuard] },
  { path: 'gerapporteerde-beoordelingen', component: GerapporteerdeReviews, canActivate: [beheerderGuard] },
  { path: 'beheer/beschadigd', component: BeschadigdeBoeken, canActivate: [beheerderGuard] },
  { path: 'beheerder/rollen', component: BeheerderRollenComponent, canActivate: [beheerderGuard] },

  { path: '**', redirectTo: 'not-found' },
];
