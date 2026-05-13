import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.time.LocalDate;

/**
 * Haalt de dringende leningen op: boeken die binnen 7 dagen ingeleverd moeten
 * worden, inclusief boeken die al te laat zijn.
 *
 * Sorteert oplopend op inleverdatum zodat de meest urgente leningen bovenaan staan.
 * Als startdatum wordt 365 dagen geleden gebruikt om ook al lang te late
 * boeken mee te nemen.
 *
 * @param page paginanummer (0-gebaseerd)
 * @param size aantal resultaten per pagina
 * @return een pagina met dringende leningen
 */
public Page<Uitlening> getDringendeLeningenSysteem(int page, int size) {
    LocalDate vandaag = LocalDate.now();
    LocalDate overEenWeek = vandaag.plusDays(7);

    Pageable pageable = PageRequest.of(page, size, Sort.by("inleverDatum").ascending());

    return uitleningRepository.findAllByIsIngeleverdFalseAndInleverDatumBetween(
        vandaag.minusDays(365),
        overEenWeek,
        pageable
    );
}
