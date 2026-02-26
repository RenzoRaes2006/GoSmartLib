import { Component } from '@angular/core';
import { RouterLink, RouterOutlet } from "@angular/router";

@Component({
  selector: 'app-hoofd-pagina',
  imports: [RouterLink],
  templateUrl: './hoofd-pagina.html',
  styleUrl: './hoofd-pagina.css',
})
export class HoofdPagina {
    bibliotheekTitel: string = "Go Smart Library";
}
