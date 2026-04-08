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
        // Given - boek heeft alleen subgenre "Filosofie"
        Boek filosofieBoek = new Boek();
        filosofieBoek.setTitel("Uniek Filosofie Boek " + System.currentTimeMillis());
        filosofieBoek.setAuteur("Plato");
        filosofieBoek.setGenre(List.of("Filosofie"));
        boekRepository.save(filosofieBoek);

        // When - filter op ["Non-fictie algemeen", "Filosofie"] (hoofdgenre +
        // subgenres)
        List<String> genres = List.of("Non-fictie algemeen", "Filosofie", "Psychologie", "Geschiedenis");
        Page<Boek> resultaat = boekRepository.filterBoeken(null, genres, null, null, null,
                null, null, null, null, defaultPageable);

        // Then - het filosofieboek moet zichtbaar zijn
        List<String> titels = resultaat.getContent().stream().map(Boek::getTitel).toList();
        assertThat(titels).contains(filosofieBoek.getTitel());
    }

    @Test
    void givenBoekMetHoofdgenre_whenFilterOpAlleenHoofdgenre_thenBoekWordtGetoond() {
        // Given
        Boek hoofdgenreBoek = new Boek();
        hoofdgenreBoek.setTitel("Uniek Non-fictie Boek " + System.currentTimeMillis());
        hoofdgenreBoek.setAuteur("Auteur");
        hoofdgenreBoek.setGenre(List.of("Non-fictie algemeen"));
        boekRepository.save(hoofdgenreBoek);

        // When
        Page<Boek> resultaat = boekRepository.filterBoeken(null, List.of("Non-fictie algemeen"),
                null, null, null, null, null, null, null, defaultPageable);

        // Then
        List<String> titels = resultaat.getContent().stream().map(Boek::getTitel).toList();
        assertThat(titels).contains(hoofdgenreBoek.getTitel());
    }

    @Test
    void givenBoekMetVerkeerGenre_whenFilterOpAnderGenre_thenBoekWordtNietGetoond() {
        // Given
        Boek thrilllerBoek = new Boek();
        thrilllerBoek.setTitel("Uniek Thriller Boek " + System.currentTimeMillis());
        thrilllerBoek.setAuteur("Auteur");
        thrilllerBoek.setGenre(List.of("Thriller"));
        boekRepository.save(thrilllerBoek);

        // When - filter op Filosofie, niet op Thriller
        Page<Boek> resultaat = boekRepository.filterBoeken(null, List.of("Filosofie"),
                null, null, null, null, null, null, null, defaultPageable);

        // Then
        List<String> titels = resultaat.getContent().stream().map(Boek::getTitel).toList();
        assertThat(titels).doesNotContain(thrilllerBoek.getTitel());
    }

    @Test
    void givenMeerdereBoekenMetVerschillendeGenres_whenFilterOpMeerdereGenres_thenAlleMatchendeBoeken() {
        // Given
        Boek boek1 = new Boek();
        boek1.setTitel("Uniek Filosofie 2 " + System.currentTimeMillis());
        boek1.setGenre(List.of("Filosofie"));
        boekRepository.save(boek1);

        Boek boek2 = new Boek();
        boek2.setTitel("Uniek Psychologie " + System.currentTimeMillis());
        boek2.setGenre(List.of("Psychologie"));
        boekRepository.save(boek2);

        // When
        Page<Boek> resultaat = boekRepository.filterBoeken(null, List.of("Filosofie", "Psychologie"),
                null, null, null, null, null, null, null, defaultPageable);

        // Then - beide boeken worden getoond
        List<String> titels = resultaat.getContent().stream().map(Boek::getTitel).toList();
        assertThat(titels).contains(boek1.getTitel(), boek2.getTitel());
    }

    // --- LEESNIVEAU FILTER ---

    @Test
    void givenBoekMetLeesniveau_whenFilterOpLeesniveau_thenGeeftAlleenMatchendeBoeken() {
        // Given
        Boek eenvoudigBoek = new Boek();
        eenvoudigBoek.setTitel("Uniek Eenvoudig Boek " + System.currentTimeMillis());
        eenvoudigBoek.setLeesniveau("A (eenvoudig taalgebruik)");
        boekRepository.save(eenvoudigBoek);

        Boek moeilijkBoek = new Boek();
        moeilijkBoek.setTitel("Uniek Moeilijk Boek " + System.currentTimeMillis());
        moeilijkBoek.setLeesniveau("C (moeilijk taalgebruik)");
        boekRepository.save(moeilijkBoek);

        // When - filter op leesniveau "A (eenvoudig taalgebruik)"
        Page<Boek> resultaat = boekRepository.filterBoeken(null, null, null, null, null,
                null, null, null, "A (eenvoudig taalgebruik)", defaultPageable);

        // Then
        List<String> titels = resultaat.getContent().stream().map(Boek::getTitel).toList();
        assertThat(titels).contains(eenvoudigBoek.getTitel());
        assertThat(titels).doesNotContain(moeilijkBoek.getTitel());
    }

    // --- BESCHIKBAARHEID FILTER ---

    @Test
    void givenBoekBeschikbaar_whenFilterBeschikbaarTrue_thenGeeftAlleenBeschikbareBoeken() {
        // Given
        Boek beschikbaar = new Boek();
        beschikbaar.setTitel("Uniek Beschikbaar Boek " + System.currentTimeMillis());
        beschikbaar.setAantalExemplaren(3);
        boekRepository.save(beschikbaar);

        Boek uitgeleend = new Boek();
        uitgeleend.setTitel("Uniek Uitgeleend Boek " + System.currentTimeMillis());
        uitgeleend.setAantalExemplaren(0);
        boekRepository.save(uitgeleend);

        // When
        Page<Boek> resultaat = boekRepository.filterBoeken(null, null, null, null, null,
                true, null, null, null, defaultPageable);

        // Then
        List<String> titels = resultaat.getContent().stream().map(Boek::getTitel).toList();
        assertThat(titels).contains(beschikbaar.getTitel());
        assertThat(titels).doesNotContain(uitgeleend.getTitel());
    }

    // --- ZOEKTERM FILTER ---

    @Test
    void givenBoekMetTitel_whenFilterOpZoekterm_thenGeeftMatchendeBoeken() {
        // Given
        String uniekwoord = "ZoektermTestXYZ" + System.currentTimeMillis();
        Boek boek = new Boek();
        boek.setTitel(uniekwoord + " Boek");
        boek.setAuteur("Auteur");
        boekRepository.save(boek);

        // When
        Page<Boek> resultaat = boekRepository.filterBoeken(uniekwoord, null, null, null, null,
                null, null, null, null, defaultPageable);

        // Then
        assertThat(resultaat.getContent()).isNotEmpty();
        assertThat(resultaat.getContent().get(0).getTitel()).contains(uniekwoord);
    }

    // --- BOEKBEHEER ZOEKEN ---

    @Test
    void givenBoekMetAuteur_whenFilterOpZoektermAuteur_thenGeeftMatchendeBoeken() {
        // Given
        String uniekAuteur = "BeheerAuteurTest" + System.currentTimeMillis();
        Boek boek = new Boek();
        boek.setTitel("Willekeurige Titel");
        boek.setAuteur(uniekAuteur);
        boekRepository.save(boek);

        // When - zoek op auteur via zoekterm
        Page<Boek> resultaat = boekRepository.filterBoeken(uniekAuteur, null, null, null, null,
                null, null, null, null, defaultPageable);

        // Then
        assertThat(resultaat.getContent()).isNotEmpty();
        assertThat(resultaat.getContent().get(0).getAuteur()).contains(uniekAuteur);
    }

    @Test
    void givenBoekMetIsbn_whenFilterOpZoektermIsbn_thenGeeftMatchendeBoeken() {
        // Given
        String uniekIsbn = "978" + System.currentTimeMillis() % 10000000000L;
        // Zorg dat ISBN 13 cijfers is
        uniekIsbn = uniekIsbn.substring(0, 13);
        Boek boek = new Boek();
        boek.setTitel("ISBN Zoek Boek");
        boek.setAuteur("Auteur");
        boek.setIsbn(uniekIsbn);
        boekRepository.save(boek);

        // When - zoek op ISBN via zoekterm
        Page<Boek> resultaat = boekRepository.filterBoeken(uniekIsbn, null, null, null, null,
                null, null, null, null, defaultPageable);

        // Then
        assertThat(resultaat.getContent()).isNotEmpty();
        assertThat(resultaat.getContent().get(0).getIsbn()).isEqualTo(uniekIsbn);
    }

    @Test
    void givenZoektermDeelVanTitel_whenFilter_thenGeeftMatchOpGedeeltelijkeTitel() {
        // Given - zoekterm is slechts een deel van de titel
        String uniekwoord = "DeelZoek" + System.currentTimeMillis();
        Boek boek = new Boek();
        boek.setTitel("Het Grote " + uniekwoord + " Avontuur");
        boek.setAuteur("Auteur");
        boekRepository.save(boek);

        // When - zoek op een deel van de titel
        Page<Boek> resultaat = boekRepository.filterBoeken(uniekwoord, null, null, null, null,
                null, null, null, null, defaultPageable);

        // Then
        assertThat(resultaat.getContent()).isNotEmpty();
        assertThat(resultaat.getContent().get(0).getTitel()).contains(uniekwoord);
    }

    @Test
    void givenZoektermHoofdletterOnafhankelijk_whenFilter_thenGeeftMatchOngeachtCase() {
        // Given - case-insensitive zoeken
        String uniekwoord = "CaseTest" + System.currentTimeMillis();
        Boek boek = new Boek();
        boek.setTitel(uniekwoord + " Boek");
        boek.setAuteur("Auteur");
        boekRepository.save(boek);

        // When - zoek met lowercase
        Page<Boek> resultaat = boekRepository.filterBoeken(uniekwoord.toLowerCase(), null, null, null, null,
                null, null, null, null, defaultPageable);

        // Then
        assertThat(resultaat.getContent()).isNotEmpty();
        assertThat(resultaat.getContent().get(0).getTitel()).contains(uniekwoord);
    }

    @Test
    void givenGeenMatchOpZoekterm_whenFilter_thenGeeftLegeLijst() {
        // Given - zoekterm die nergens matcht
        String onbestaand = "XYZBESTAATNIETZZZZ" + System.currentTimeMillis();

        // When
        Page<Boek> resultaat = boekRepository.filterBoeken(onbestaand, null, null, null, null,
                null, null, null, null, defaultPageable);

        // Then
        assertThat(resultaat.getContent()).isEmpty();
    }

    // --- GECOMBINEERDE FILTERS ---

    @Test
    void givenBoekMetGenreEnZoekterm_whenFilterOpBeide_thenGeeftAlleenExacteMatch() {
        // Given
        String uniekwoord = "CombiTest" + System.currentTimeMillis();
        Boek match = new Boek();
        match.setTitel(uniekwoord + " Roman");
        match.setGenre(List.of("Roman"));
        boekRepository.save(match);

        Boek geenMatch = new Boek();
        geenMatch.setTitel(uniekwoord + " Thriller");
        geenMatch.setGenre(List.of("Thriller"));
        boekRepository.save(geenMatch);

        // When - filter op zoekterm + genre Roman
        Page<Boek> resultaat = boekRepository.filterBoeken(uniekwoord, List.of("Roman"),
                null, null, null, null, null, null, null, defaultPageable);

        // Then - alleen het Roman-boek
        List<String> titels = resultaat.getContent().stream().map(Boek::getTitel).toList();
        assertThat(titels).contains(match.getTitel());
        assertThat(titels).doesNotContain(geenMatch.getTitel());
    }
}
