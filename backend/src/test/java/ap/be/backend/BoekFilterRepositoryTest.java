package ap.be.backend;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import ap.be.backend.entity.Boek;
import ap.be.backend.entity.DidactischeSubcategorie;
import ap.be.backend.repository.BoekRepository;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class BoekFilterRepositoryTest {

    @Autowired
    private BoekRepository boekRepository;

    private final Pageable defaultPageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.ASC, "id"));

    // --- GENRE FILTERS ---

    @Test
    void givenBoekMetEnkelSubgenre_whenFilterOpHoofdgenrePlusSubgenres_thenBoekWordtGetoond() {
        Boek filosofieBoek = new Boek();
        filosofieBoek.setTitel("Uniek Filosofie Boek " + System.currentTimeMillis());
        filosofieBoek.setAuteur("Plato");
        filosofieBoek.setGenre(List.of("Filosofie"));
        boekRepository.save(filosofieBoek);

        List<String> genres = List.of("Non-fictie algemeen", "Filosofie", "Psychologie", "Geschiedenis");
        // 13 params: zoekterm, genres, tags, uitg, minP, maxP, besch, jaarV, jaarT, leesniv, didactMat, didactSub, pageable
        Page<Boek> resultaat = boekRepository.filterBoeken(null, genres, null, null, null,
                null, null, null, null, null, null, null, defaultPageable);

        List<String> titels = resultaat.getContent().stream().map(Boek::getTitel).toList();
        assertThat(titels).contains(filosofieBoek.getTitel());
    }

    @Test
    void givenBoekMetHoofdgenre_whenFilterOpAlleenHoofdgenre_thenBoekWordtGetoond() {
        Boek hoofdgenreBoek = new Boek();
        hoofdgenreBoek.setTitel("Uniek Non-fictie Boek " + System.currentTimeMillis());
        hoofdgenreBoek.setGenre(List.of("Non-fictie algemeen"));
        boekRepository.save(hoofdgenreBoek);

        Page<Boek> resultaat = boekRepository.filterBoeken(null, List.of("Non-fictie algemeen"),
                null, null, null, null, null, null, null, null, null, null, defaultPageable);

        assertThat(resultaat.getContent().stream().map(Boek::getTitel)).contains(hoofdgenreBoek.getTitel());
    }

    // --- DIDACTISCH MATERIAAL FILTERS (NIEUW) ---

    @Test
    void givenDidactischBoek_whenFilterDidactischTrue_thenBoekWordtGetoond() {
        // Given
        Boek didactisch = new Boek();
        didactisch.setTitel("Didactisch Boek " + System.currentTimeMillis());
        didactisch.setDidactischMateriaal(true);
        boekRepository.save(didactisch);

        Boek normaal = new Boek();
        normaal.setTitel("Normaal Boek " + System.currentTimeMillis());
        normaal.setDidactischMateriaal(false);
        boekRepository.save(normaal);

        // When - filter op didactischMateriaal = true
        Page<Boek> resultaat = boekRepository.filterBoeken(null, null, null, null, null,
                null, null, null, null, null, true, null, defaultPageable);

        // Then
        List<String> titels = resultaat.getContent().stream().map(Boek::getTitel).toList();
        assertThat(titels).contains(didactisch.getTitel());
        assertThat(titels).doesNotContain(normaal.getTitel());
    }

    @Test
    void givenBoekMetDidactischeSubcategorie_whenFilterOpSubcategorie_thenAlleenDatBoekGetoond() {
        // Given
        Boek lesmethode = new Boek();
        lesmethode.setTitel("Wiskunde Methode " + System.currentTimeMillis());
        lesmethode.setDidactischMateriaal(true);
        lesmethode.setDidactischeSubcategorie(DidactischeSubcategorie.LESMETHODES);
        boekRepository.save(lesmethode);

        Boek naslagwerk = new Boek();
        naslagwerk.setTitel("Woordenboek " + System.currentTimeMillis());
        naslagwerk.setDidactischMateriaal(true);
        naslagwerk.setDidactischeSubcategorie(DidactischeSubcategorie.NASLAGWERKEN);
        boekRepository.save(naslagwerk);

        // When - filter op LESMETHODES
        Page<Boek> resultaat = boekRepository.filterBoeken(null, null, null, null, null,
                null, null, null, null, null, null, DidactischeSubcategorie.LESMETHODES, defaultPageable);

        // Then
        List<String> titels = resultaat.getContent().stream().map(Boek::getTitel).toList();
        assertThat(titels).contains(lesmethode.getTitel());
        assertThat(titels).doesNotContain(naslagwerk.getTitel());
    }

    // --- LEESNIVEAU FILTER ---

    @Test
    void givenBoekMetLeesniveau_whenFilterOpLeesniveau_thenGeeftAlleenMatchendeBoeken() {
        Boek eenvoudigBoek = new Boek();
        eenvoudigBoek.setTitel("Uniek Eenvoudig Boek " + System.currentTimeMillis());
        eenvoudigBoek.setLeesniveau("A (eenvoudig taalgebruik)");
        boekRepository.save(eenvoudigBoek);

        Page<Boek> resultaat = boekRepository.filterBoeken(null, null, null, null, null,
                null, null, null, null, "A (eenvoudig taalgebruik)", null, null, defaultPageable);

        assertThat(resultaat.getContent().stream().map(Boek::getTitel)).contains(eenvoudigBoek.getTitel());
    }

    // --- BESCHIKBAARHEID FILTER ---

    @Test
    void givenBoekBeschikbaar_whenFilterBeschikbaarTrue_thenGeeftAlleenBeschikbareBoeken() {
        Boek beschikbaar = new Boek();
        beschikbaar.setTitel("Uniek Beschikbaar Boek " + System.currentTimeMillis());
        beschikbaar.setAantalExemplaren(3);
        boekRepository.save(beschikbaar);

        Boek uitgeleend = new Boek();
        uitgeleend.setTitel("Uniek Uitgeleend Boek " + System.currentTimeMillis());
        uitgeleend.setAantalExemplaren(0);
        boekRepository.save(uitgeleend);

        Page<Boek> resultaat = boekRepository.filterBoeken(null, null, null, null, null,
                null, true, null, null, null, null, null, defaultPageable);

        List<String> titels = resultaat.getContent().stream().map(Boek::getTitel).toList();
        assertThat(titels).contains(beschikbaar.getTitel());
        assertThat(titels).doesNotContain(uitgeleend.getTitel());
    }

    // --- ZOEKTERM FILTER ---

    @Test
    void givenBoekMetTitel_whenFilterOpZoekterm_thenGeeftMatchendeBoeken() {
        String uniekwoord = "ZoektermTestXYZ" + System.currentTimeMillis();
        Boek boek = new Boek();
        boek.setTitel(uniekwoord + " Boek");
        boekRepository.save(boek);

        Page<Boek> resultaat = boekRepository.filterBoeken(uniekwoord, null, null, null, null,
                null, null, null, null, null, null, null, defaultPageable);

        assertThat(resultaat.getContent()).isNotEmpty();
        assertThat(resultaat.getContent().get(0).getTitel()).contains(uniekwoord);
    }

    @Test
    void givenBoekMetIsbn_whenFilterOpZoektermIsbn_thenGeeftMatchendeBoeken() {
        String uniekIsbn = "978" + System.currentTimeMillis() % 10000000000L;
        uniekIsbn = uniekIsbn.substring(0, 13);
        Boek boek = new Boek();
        boek.setTitel("ISBN Zoek Boek");
        boek.setIsbn(uniekIsbn);
        boekRepository.save(boek);

        Page<Boek> resultaat = boekRepository.filterBoeken(uniekIsbn, null, null, null, null,
                null, null, null, null, null, null, null, defaultPageable);

        assertThat(resultaat.getContent()).isNotEmpty();
        assertThat(resultaat.getContent().get(0).getIsbn()).isEqualTo(uniekIsbn);
    }

    @Test
    void givenZoektermHoofdletterOnafhankelijk_whenFilter_thenGeeftMatchOngeachtCase() {
        String uniekwoord = "CaseTest" + System.currentTimeMillis();
        Boek boek = new Boek();
        boek.setTitel(uniekwoord + " Boek");
        boekRepository.save(boek);

        Page<Boek> resultaat = boekRepository.filterBoeken(uniekwoord.toLowerCase(), null, null, null, null,
                null, null, null, null, null, null, null, defaultPageable);

        assertThat(resultaat.getContent()).isNotEmpty();
        assertThat(resultaat.getContent().get(0).getTitel()).contains(uniekwoord);
    }

    // --- GECOMBINEERDE FILTERS ---

    @Test
    void givenDidactischBoekMetZoekterm_whenFilterOpBeide_thenAlleenExacteMatch() {
        // Given
        String term = "Wiskunde";
        Boek match = new Boek();
        match.setTitel("Wiskunde voor gevorderden");
        match.setDidactischMateriaal(true);
        boekRepository.save(match);

        Boek geenMatch = new Boek();
        geenMatch.setTitel("Wiskunde Roman");
        geenMatch.setDidactischMateriaal(false);
        boekRepository.save(geenMatch);

        // When - filter op zoekterm "Wiskunde" EN didactischMateriaal = true
        Page<Boek> resultaat = boekRepository.filterBoeken(term, null, null, null, null,
                null, null, null, null, null, true, null, defaultPageable);

        // Then
        List<String> titels = resultaat.getContent().stream().map(Boek::getTitel).toList();
        assertThat(titels).contains(match.getTitel());
        assertThat(titels).doesNotContain(geenMatch.getTitel());
    }
}