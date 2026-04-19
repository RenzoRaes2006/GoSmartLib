import { Injectable, signal } from "@angular/core";
import { jwtDecode } from "jwt-decode";

@Injectable({ providedIn: 'root' })
export class AuthService {
    // public currentUser = signal<{ id: string, name: string, role: string } | null>(null);

    // Dev mode, dummy gebruiker om te kunnen testen. Later de code in commentaar gebruiken om echte JWT tokens te decoden en de gebruiker in te stellen op basis van de token
    public currentUser = signal<{ id: string, name: string, role: string } | null>({
        id: '12345',
        name: 'Test User',
        role: 'leerkracht' 
    });

    constructor() {
        // const token = localStorage.getItem('jwtToken');
        // if (token) {
        //   const decoded: any = jwtDecode(token);
        //   this.currentUser.set({ id: decoded.sub, name: decoded.name, role: decoded.role });
        // }
    }
}