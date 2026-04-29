// package ap.be.backend;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.*;
// import static org.mockito.Mockito.*;

// import java.util.Collections;
// import java.util.List;

// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageImpl;
// import org.springframework.data.domain.Sort;

// import ap.be.backend.entity.Boek;
// import ap.be.backend.exception.InvalidYearRangeException;
// import ap.be.backend.repository.BoekRepository;
// import ap.be.backend.service.BoekService;

// @ExtendWith(MockitoExtension.class)
// public class BoekFilterServiceTest {

//         @Mock
//         private BoekRepository boekRepository;

//         @InjectMocks
//         private BoekService boekService;

//         private final Sort defaultSort = Sort.by(Sort.Direction.ASC, "id");

//         // --- GENRE FILTERS ---

//         @Test
//         void givenGeenFilters_whenFilterBoeken_thenRoeptRepositoryAanMetNullGenres() {
//                 // Given
//                 Page<Boek> verwacht = new PageImpl<>(List.of(new Boek("Boek A", "Auteur A")));
//                 when(boekRepository.filterBoeken(eq(null), eq(null), eq(null), eq(null), eq(null),
//                                 eq(null), eq(null), eq(null), eq(null), eq(null), any()))
//                                 .thenReturn(verwacht);

//                 // When
//                 Page<Boek> resultaat = boekService.filterBoeken(null, null, null, null, null,
//                                 null, null, null, null, null, null, null, defaultSort);

//                 // Then
//                 assertEquals(verwacht.getContent(), resultaat.getContent());
//         }

//         @Test
//         void givenEnkelGenreInLijst_whenFilterBoeken_thenGeeftLijstAanRepository() {
//                 // Given
//                 List<String> genres = List.of("Filosofie");
//                 Page<Boek> verwacht = new PageImpl<>(List.of(new Boek("Socrates", "Plato")));
//                 when(boekRepository.filterBoeken(eq(null), eq(genres), eq(null), eq(null), eq(null),
//                                 eq(null), eq(null), eq(null), eq(null), eq(null), any()))
//                                 .thenReturn(verwacht);

//                 // When
//                 Page<Boek> resultaat = boekService.filterBoeken(null, genres, null, null, null,
//                                 null, null, null, null, null, null, null, defaultSort);

//                 // Then
//                 assertEquals(verwacht.getContent(), resultaat.getContent());
//         }

//         @Test
//         void givenHoofdgenrePlusSubgenres_whenFilterBoeken_thenGeeftVolleLijstAanRepository() {
//                 // Given - frontend stuurt hoofdgenre + alle subgenres als lijst
//                 List<String> genres = List.of("Non-fictie algemeen", "Filosofie", "Psychologie", "Geschiedenis");
//                 Page<Boek> verwacht = new PageImpl<>(List.of(
//                                 new Boek("Sapiens", "Harari"),
//                                 new Boek("Denken snel en langzaam", "Kahneman")));
//                 when(boekRepository.filterBoeken(eq(null), eq(genres), eq(null), eq(null), eq(null),
//                                 eq(null), eq(null), eq(null), eq(null), eq(null), any()))
//                                 .thenReturn(verwacht);

//                 // When
//                 Page<Boek> resultaat = boekService.filterBoeken(null, genres, null, null, null,
//                                 null, null, null, null, null, null, null, defaultSort);

//                 // Then
//                 assertEquals(2, resultaat.getContent().size());
//         }

//         @Test
//         void givenLegeGenresLijst_whenFilterBoeken_thenRoeptRepositoryAanMetNull() {
//                 // Given - lege lijst moet omgezet worden naar null (geen genre-filter)
//                 List<String> legeGenres = Collections.emptyList();
//                 Page<Boek> verwacht = new PageImpl<>(List.of(new Boek("Alle Boeken", "Iedereen")));
//                 when(boekRepository.filterBoeken(eq(null), eq(null), eq(null), eq(null), eq(null),
//                                 eq(null), eq(null), eq(null), eq(null), eq(null), any()))
//                                 .thenReturn(verwacht);

//                 // When
//                 Page<Boek> resultaat = boekService.filterBoeken(null, legeGenres, null, null, null,
//                                 null, null, null, null, null, null, null, defaultSort);

//                 // Then - lege lijst wordt als null doorgegeven
//                 assertEquals(verwacht.getContent(), resultaat.getContent());
//         }

//         // --- JAAR VALIDATIE ---

//         @Test
//         void givenJaarVanKleinerDan1900_whenFilterBoeken_thenGooitInvalidYearRangeException() {
//                 // When & Then
//                 assertThrows(InvalidYearRangeException.class, () -> boekService.filterBoeken(null, null, null, null,
//                                 null, null, null, 1899, null, null, null, null, defaultSort));
//                 verify(boekRepository, never()).filterBoeken(any(), any(), any(), any(), any(),
//                                 any(), any(), any(), any(), any(), any());
//         }

//         @Test
//         void givenJaarTotInDeToekomst_whenFilterBoeken_thenGooitInvalidYearRangeException() {
//                 // Given
//                 int toekomstjaar = java.time.Year.now().getValue() + 1;

//                 // When & Then
//                 assertThrows(InvalidYearRangeException.class, () -> boekService.filterBoeken(null, null, null, null,
//                                 null, null, null, null, toekomstjaar, null, null, null, defaultSort));
//                 verify(boekRepository, never()).filterBoeken(any(), any(), any(), any(), any(),
//                                 any(), any(), any(), any(), any(), any());
//         }

//         @Test
//         void givenJaarVanGroterDanJaarTot_whenFilterBoeken_thenGooitInvalidYearRangeException() {
//                 // When & Then
//                 assertThrows(InvalidYearRangeException.class, () -> boekService.filterBoeken(null, null, null, null,
//                                 null, null, null, 2020, 2010, null, null, null, defaultSort));
//                 verify(boekRepository, never()).filterBoeken(any(), any(), any(), any(), any(),
//                                 any(), any(), any(), any(), any(), any());
//         }

//         @Test
//         void givenGeldigJaarBereik_whenFilterBoeken_thenRoeptRepositoryAanMetJaren() {
//                 // Given
//                 Page<Boek> verwacht = new PageImpl<>(List.of(new Boek("Modern Boek", "Auteur")));
//                 when(boekRepository.filterBoeken(eq(null), eq(null), eq(null), eq(null), eq(null),
//                                 eq(null), eq(null), eq(2000), eq(2020), eq(null), any()))
//                                 .thenReturn(verwacht);

//                 // When
//                 Page<Boek> resultaat = boekService.filterBoeken(null, null, null, null, null,
//                                 null, null, 2000, 2020, null, null, null, defaultSort);

//                 // Then
//                 assertEquals(verwacht.getContent(), resultaat.getContent());
//         }

//         // --- OVERIGE FILTERS ---

//         @Test
//         void givenBeschikbaarFilter_whenFilterBoeken_thenGeeftBeschikbaarFlagAanRepository() {
//                 // Given
//                 Boek beschikbaarBoek = new Boek("Beschikbaar Boek", "Auteur");
//                 beschikbaarBoek.setAantalExemplaren(3);
//                 Page<Boek> verwacht = new PageImpl<>(List.of(beschikbaarBoek));
//                 when(boekRepository.filterBoeken(eq(null), eq(null), eq(null), eq(null), eq(null),
//                                 eq(null), eq(true), eq(null), eq(null), eq(null), any()))
//                                 .thenReturn(verwacht);

//                 // When
//                 Page<Boek> resultaat = boekService.filterBoeken(null, null, null, null, null,
//                                 null, true, null, null, null, null, null, defaultSort);

//                 // Then
//                 assertEquals(1, resultaat.getContent().size());
//         }

//         @Test
//         void givenLeesniveauFilter_whenFilterBoeken_thenGeeftLeesniveauAanRepository() {
//                 // Given
//                 Boek boek = new Boek("Eenvoudig Boek", "Auteur");
//                 boek.setLeesniveau("A (eenvoudig taalgebruik)");
//                 Page<Boek> verwacht = new PageImpl<>(List.of(boek));
//                 when(boekRepository.filterBoeken(eq(null), eq(null), eq(null), eq(null), eq(null),
//                                 eq(null), eq(null), eq(null), eq(null), eq("A (eenvoudig taalgebruik)"), any()))
//                                 .thenReturn(verwacht);

//                 // When
//                 Page<Boek> resultaat = boekService.filterBoeken(null, null, null, null, null,
//                                 null, null, null, null, "A (eenvoudig taalgebruik)", null, null, defaultSort);

//                 // Then
//                 assertEquals(1, resultaat.getContent().size());
//                 assertEquals("A (eenvoudig taalgebruik)", resultaat.getContent().get(0).getLeesniveau());
//         }

//         @Test
//         void givenZoektermFilter_whenFilterBoeken_thenGeeftZoektermAanRepository() {
//                 // Given
//                 Boek boek = new Boek("De Alchemist", "Paulo Coelho");
//                 Page<Boek> verwacht = new PageImpl<>(List.of(boek));
//                 when(boekRepository.filterBoeken(eq("alchemist"), eq(null), eq(null), eq(null), eq(null),
//                                 eq(null), eq(null), eq(null), eq(null), eq(null), any()))
//                                 .thenReturn(verwacht);

//                 // When
//                 Page<Boek> resultaat = boekService.filterBoeken("alchemist", null, null, null, null,
//                                 null, null, null, null, null, null, null, defaultSort);

//                 // Then
//                 assertEquals(1, resultaat.getContent().size());
//                 assertEquals("De Alchemist", resultaat.getContent().get(0).getTitel());
//         }

//         // --- BOEKBEHEER ZOEKEN ---

//         @Test
//         void givenZoektermOpAuteur_whenFilterBoeken_thenGeeftZoektermAanRepository() {
//                 // Given - boekbeheer zoekt op auteur
//                 Boek boek = new Boek("De Alchemist", "Paulo Coelho");
//                 when(boekRepository.filterBoeken(eq("coelho"), eq(null), eq(null), eq(null), eq(null),
//                                 eq(null), eq(null), eq(null), eq(null), eq(null), any()))
//                                 .thenReturn(new PageImpl<>(List.of(boek)));

//                 // When
//                 Page<Boek> resultaat = boekService.filterBoeken("coelho", null, null, null, null,
//                                 null, null, null, null, null, null, null, defaultSort);

//                 // Then
//                 assertEquals(1, resultaat.getContent().size());
//                 assertEquals("Paulo Coelho", resultaat.getContent().get(0).getAuteur());
//         }

//         @Test
//         void givenZoektermOpIsbn_whenFilterBoeken_thenGeeftZoektermAanRepository() {
//                 // Given - boekbeheer zoekt op ISBN
//                 Boek boek = new Boek("Clean Code", "Robert Martin");
//                 boek.setIsbn("9780132350884");
//                 when(boekRepository.filterBoeken(eq("9780132350884"), eq(null), eq(null), eq(null), eq(null),
//                                 eq(null), eq(null), eq(null), eq(null), eq(null), any()))
//                                 .thenReturn(new PageImpl<>(List.of(boek)));

//                 // When
//                 Page<Boek> resultaat = boekService.filterBoeken("9780132350884", null, null, null, null,
//                                 null, null, null, null, null, null, null, defaultSort);

//                 // Then
//                 assertEquals(1, resultaat.getContent().size());
//                 assertEquals("9780132350884", resultaat.getContent().get(0).getIsbn());
//         }

//         @Test
//         void givenZoektermZonderMatch_whenFilterBoeken_thenGeeftLegeLijst() {
//                 // Given
//                 when(boekRepository.filterBoeken(eq("xyzbestaatnietzzzz"), eq(null), eq(null), eq(null), eq(null),
//                                 eq(null), eq(null), eq(null), eq(null), eq(null), any()))
//                                 .thenReturn(new PageImpl<>(List.of()));

//                 // When
//                 Page<Boek> resultaat = boekService.filterBoeken("xyzbestaatnietzzzz", null, null, null, null,
//                                 null, null, null, null, null, null, null, defaultSort);

//                 // Then
//                 assertEquals(0, resultaat.getContent().size());
//         }

//         @Test
//         void givenZoektermMetMeerdereMatches_whenFilterBoeken_thenGeeftAlleBoekenTerug() {
//                 // Given - zoekterm "java" matcht meerdere boeken
//                 Page<Boek> verwacht = new PageImpl<>(List.of(
//                                 new Boek("Effective Java", "Joshua Bloch"),
//                                 new Boek("Java Concurrency", "Brian Goetz")));
//                 when(boekRepository.filterBoeken(eq("java"), eq(null), eq(null), eq(null), eq(null),
//                                 eq(null), eq(null), eq(null), eq(null), eq(null), any()))
//                                 .thenReturn(verwacht);

//                 // When
//                 Page<Boek> resultaat = boekService.filterBoeken("java", null, null, null, null,
//                                 null, null, null, null, null, null, null, defaultSort);

//                 // Then
//                 assertEquals(2, resultaat.getContent().size());
//         }

//         // --- PAGINABEREIK ---

//         @Test
//         void givenPaginaBereik_whenFilterBoeken_thenGeeftMinEnMaxPaginaAanRepository() {
//                 // Given
//                 Page<Boek> verwacht = new PageImpl<>(List.of(new Boek("Kort Boek", "Auteur")));
//                 when(boekRepository.filterBoeken(eq(null), eq(null), eq(null), eq(null), eq(100), eq(300),
//                                 eq(null), eq(null), eq(null), eq(null), any()))
//                                 .thenReturn(verwacht);

//                 // When
//                 Page<Boek> resultaat = boekService.filterBoeken(null, null, null, null, 100, 300,
//                                 null, null, null, null, null, null, defaultSort);

//                 // Then
//                 assertEquals(1, resultaat.getContent().size());
//         }
// }
