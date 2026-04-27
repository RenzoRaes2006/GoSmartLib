import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.time.LocalDate;

// Voeg deze methode toe aan je Service
public Page<Uitlening> getDringendeLeningenSysteem(int page, int size) {
    LocalDate vandaag = LocalDate.now();
    LocalDate overEenWeek = vandaag.plusDays(7);
    
    // We sorteren oplopend, zodat de leningen die het eerst binnen moeten (of te laat zijn) bovenaan staan
    Pageable pageable = PageRequest.of(page, size, Sort.by("inleverDatum").ascending());
    
    // OPMERKING: We gebruiken minusDays(365) als startdatum om óók de boeken te vangen die al héél erg te laat zijn!
    return uitleningRepository.findAllByIsIngeleverdFalseAndInleverDatumBetween(
        vandaag.minusDays(365), 
        overEenWeek, 
        pageable
    );
}