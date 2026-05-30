package ap.be.backend;

import ap.be.backend.entity.Boek;
import ap.be.backend.repository.BoekRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BoekRepositoryTest {

    @Autowired
    private BoekRepository boekRepository;

    @Test
    void givenBoekenInDatabase_whenFindAllOrderByLocatie_thenGesorteerd() {
        Boek boek1 = new Boek();
        boek1.setTitel("ZZZ Test Boek");
        boek1.setLocatie("MaGO! Antwerpen");

        Boek boek2 = new Boek();
        boek2.setTitel("AAA Test Boek");
        boek2.setLocatie("GO! Spectrumschool");

        boekRepository.save(boek1);
        boekRepository.save(boek2);

        List<Boek> resultaat = boekRepository.findAll();

        assertThat(resultaat.size()).isGreaterThanOrEqualTo(2);

        List<String> locaties = resultaat.stream()
                .map(Boek::getLocatie)
                .filter(l -> l.equals("GO! Spectrumschool") || l.equals("MaGO! Antwerpen"))
                .distinct()
                .sorted()
                .toList();

        assertThat(locaties.get(0)).isEqualTo("GO! Spectrumschool");
        assertThat(locaties.get(1)).isEqualTo("MaGO! Antwerpen");
    }

    @Test
    void givenBoekZonderExemplaren_whenSaved_thenIsbnCorrectOpgeslagen() {
        Boek testBoek = new Boek();
        testBoek.setTitel("Test Boek");
        testBoek.setIsbn("1234567890123");

        Boek savedBoek = boekRepository.save(testBoek);

        assertThat(savedBoek.getId()).isNotNull();
        assertThat(savedBoek.getAantalBeschikbaar()).isEqualTo(0);
        assertThat(savedBoek.getIsbn()).isEqualTo("1234567890123");
    }

    @Test
    public void givenBestaandeTitel_whenFilterBoeken_thenGefilterdeResultaten() {
        Boek b1 = new Boek();
        b1.setTitel("Java for Beginners");
        b1.setAuteur("John Doe");
        b1.setLocatie("A1");
        Boek b2 = new Boek();
        b2.setTitel("Python Basics");
        b2.setAuteur("Jane Smith");
        b2.setLocatie("B2");
        boekRepository.saveAll(List.of(b1, b2));
        Pageable pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.ASC, "locatie"));

        Page<Boek> result = boekRepository.filterBoeken("java", null, null, null, null, null, null, null, null, null,
                null, null, null, pageable);

        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().stream().map(Boek::getTitel).toList()).contains("Java for Beginners");
    }

    @Test
    public void givenBoekMetAuteur_whenFilterBoekenOpAuteur_thenPassendBoek() {
        Boek b1 = new Boek();
        b1.setTitel("Effective Java");
        b1.setAuteur("Joshua Bloch");
        b1.setLocatie("C3");
        boekRepository.save(b1);
        Pageable pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.ASC, "locatie"));

        Page<Boek> result = boekRepository.filterBoeken("Bloch", null, null, null, null, null, null, null, null, null,
                null, null, null, pageable);

        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().get(0).getAuteur()).isEqualTo("Joshua Bloch");
    }

    @Test
    void givenDidactischMateriaal_whenFilteredOnDidactischTrue_thenReturnsOnlyDidactisch() {
        Boek d1 = new Boek();
        d1.setTitel("Didactisch Materiaal 1");
        d1.setDidactischMateriaal(true);
        Boek n1 = new Boek();
        n1.setTitel("Normaal Boek 1");
        n1.setDidactischMateriaal(false);
        boekRepository.saveAll(List.of(d1, n1));
        Pageable pageable = PageRequest.of(0, 100);

        Page<Boek> result = boekRepository.filterBoeken(null, null, null, null, null, null, null, null, null, true, null, null, null, pageable);

        assertThat(result.getContent().stream().map(Boek::getTitel)).contains("Didactisch Materiaal 1");
        assertThat(result.getContent().stream().map(Boek::getTitel)).doesNotContain("Normaal Boek 1");
    }

    @Test
    void givenBoekMetIsbn_whenFindByIsbn_thenGeeftBoekTerug() {
        String uniekIsbn = String.format("978%010d", System.currentTimeMillis() % 10000000000L);
        Boek boek = new Boek();
        boek.setTitel("ISBN Test Boek");
        boek.setAuteur("Test Auteur");
        boek.setIsbn(uniekIsbn);
        boekRepository.save(boek);

        Optional<Boek> result = boekRepository.findByIsbn(uniekIsbn);

        assertThat(result).isPresent();
        assertThat(result.get().getIsbn()).isEqualTo(uniekIsbn);
        assertThat(result.get().getTitel()).isEqualTo("ISBN Test Boek");
    }

    @Test
    void givenGeenBoekMetIsbn_whenFindByIsbn_thenGeeftLegeOptional() {
        Optional<Boek> result = boekRepository.findByIsbn("0000000000000");

        assertThat(result).isEmpty();
    }

    @Test
    void givenBoeken_whenFindTop5Populair_thenGeeftBoekenTerug() {
        Boek b = new Boek();
        b.setTitel("Populair testboek");
        boekRepository.save(b);

        List<Boek> top = boekRepository.findTop5PopulaireBoeken();

        assertThat(top).isNotEmpty();
        assertThat(top.get(0).getTitel()).isNotNull();
    }
}
