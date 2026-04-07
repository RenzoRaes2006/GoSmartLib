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
    void givenBooksInDatabase_whenFindAllOrderByLocatie_thenReturnsSortedBooks() {
        // --- ARRANGE ---
        Boek boek1 = new Boek();
        boek1.setTitel("ZZZ Test Boek");
        boek1.setLocatie("MaGO! Antwerpen");

        Boek boek2 = new Boek();
        boek2.setTitel("AAA Test Boek");
        boek2.setLocatie("GO! Spectrumschool");

        boekRepository.save(boek1);
        boekRepository.save(boek2);

        // --- ACT ---
        List<Boek> resultaat = boekRepository.findAll();

        // --- ASSERT ---
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
    void givenBookWithZeroExemplaren_whenSaved_thenIsbnIsStoredCorrectly() {
        // --- ARRANGE (Given) ---
        Boek testBoek = new Boek();
        testBoek.setTitel("Test Boek");
        testBoek.setIsbn("1234567890123");
        testBoek.setAantalExemplaren(0);

        // --- ACT (When) ---
        Boek savedBoek = boekRepository.save(testBoek);

        // --- ASSERT (Then) ---
        assertThat(savedBoek.getId()).isNotNull();
        assertThat(savedBoek.getAantalExemplaren()).isEqualTo(0);
        assertThat(savedBoek.getIsbn()).isEqualTo("1234567890123");
    }

    @Test
    public void givenBoekenInDatabase_whenSearchWithExistingTitle_thenReturnsFilteredList() {
        // Given
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

        // When
        Page<Boek> result = boekRepository.filterBoeken("java", null, null, null, null, null, null, null, null,
                pageable);

        // Then
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().stream().map(Boek::getTitel).toList()).contains("Java for Beginners");
    }

    @Test
    public void givenBoekWithAuthor_whenSearchOnAuthorName_thenReturnsMatchingBoek() {
        // Given
        Boek b1 = new Boek();
        b1.setTitel("Effective Java");
        b1.setAuteur("Joshua Bloch");
        b1.setLocatie("C3");
        boekRepository.save(b1);
        Pageable pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.ASC, "locatie"));

        // When
        Page<Boek> result = boekRepository.filterBoeken("Bloch", null, null, null, null, null, null, null, null,
                pageable);

        // Then
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().get(0).getAuteur()).isEqualTo("Joshua Bloch");
    }

    // --- FIND BY ISBN ---

    @Test
    void givenBoekMetIsbn_whenFindByIsbn_thenGeeftBoekTerug() {
        // Given
        String uniekIsbn = "978" + (System.currentTimeMillis() % 10000000000L);
        uniekIsbn = uniekIsbn.substring(0, 13);
        Boek boek = new Boek();
        boek.setTitel("ISBN Test Boek");
        boek.setAuteur("Test Auteur");
        boek.setIsbn(uniekIsbn);
        boekRepository.save(boek);

        // When
        Optional<Boek> result = boekRepository.findByIsbn(uniekIsbn);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getIsbn()).isEqualTo(uniekIsbn);
        assertThat(result.get().getTitel()).isEqualTo("ISBN Test Boek");
    }

    @Test
    void givenGeenBoekMetIsbn_whenFindByIsbn_thenGeeftLegeOptional() {
        // When
        Optional<Boek> result = boekRepository.findByIsbn("0000000000000");

        // Then
        assertThat(result).isEmpty();
    }
}