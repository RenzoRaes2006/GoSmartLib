import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    // 1. Haal de token uit de localStorage
    const token = localStorage.getItem('jwtToken');

    // 2. Als er een token is, kloon het verzoek en voeg de header toe
    if (token) {
        const clonedRequest = req.clone({
            setHeaders: {
                Authorization: `Bearer ${token}`
            }
        });
        // Stuur het aangepaste verzoek door naar de backend
        return next(clonedRequest);
    }

    // 3. Als er geen token is, stuur het originele verzoek gewoon door
    return next(req);
};