import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    // Externe API's (zoals Google Books) niet aanraken: een custom header
    // triggert een CORS preflight die de externe server afwijst.
    // Eigen backend op localhost (andere poort) telt NIET als extern.
    const isOwnBackend =
        req.url.startsWith('/') ||
        req.url.startsWith(window.location.origin) ||
        /^https?:\/\/localhost(:\d+)?\//i.test(req.url);
    if (!isOwnBackend) {
        return next(req);
    }

    // Als de aanroeper zelf al een Authorization-header heeft gezet
    // (bv. het tijdelijke reset-token bij wachtwoord-wijzigen),
    // niet overschrijven met het reguliere jwtToken uit localStorage.
    if (req.headers.has('Authorization')) {
        return next(req);
    }

    // Dev-modus: enkel op localhost. In de VM/productie zou een dev-build
    // anders X-Dev-Role meesturen i.p.v. het JWT-token, wat een OAuth-redirect
    // veroorzaakt omdat DevAuthFilter daar niet draait.
    const isLocalhost = window.location.hostname === 'localhost';
    if (isLocalhost) {
        const devRole = (localStorage.getItem('devRole') || 'BEHEERDER').toUpperCase();
        const devBeheerder = localStorage.getItem('devBeheerder') === 'true';
        const devReq = req.clone({
            setHeaders: {
                'X-Dev-Role': devRole,
                'X-Dev-Beheerder': String(devBeheerder),
            },
        });
        return next(devReq);
    }

    // Productie: standaard JWT-flow via localStorage.
    const token = localStorage.getItem('jwtToken');
    if (token) {
        const clonedRequest = req.clone({
            setHeaders: {
                Authorization: `Bearer ${token}`
            }
        });
        return next(clonedRequest);
    }

    return next(req);
};
