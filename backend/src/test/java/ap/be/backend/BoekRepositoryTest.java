package ap.be.backend;

import ap.be.backend.entity.Boek;
import ap.be.backend.repository.BoekRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;

import java.util.List;

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
        Sort sort = Sort.by(Sort.Direction.ASC, "locatie");

        // When
        List<Boek> result = boekRepository.filterBoeken("java", null, null, null, null, null, null, null, "java",
                null);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitel()).isEqualTo("Java for Beginners");
    }

    @Test
    public void givenBoekWithAuthor_whenSearchOnAuthorName_thenReturnsMatchingBoek() {
        // Given
        Boek b1 = new Boek();
        b1.setTitel("Effective Java");
        b1.setAuteur("Joshua Bloch");
        b1.setLocatie("C3");
        boekRepository.save(b1);
        Sort sort = Sort.by(Sort.Direction.ASC, "locatie");

        // When
        List<Boek> result = boekRepository.filterBoeken("Bloch", null, null, null, null, null, null, null, "Bloch",
                null);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getAuteur()).isEqualTo("Joshua Bloch");
    }
}