// package ap.be.backend;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertFalse;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
// import static org.junit.jupiter.api.Assertions.assertNull;
// import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.junit.jupiter.api.Assertions.assertTrue;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.never;
// import static org.mockito.Mockito.times;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// import java.util.List;
// import java.util.Optional;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.ArgumentCaptor;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

// import ap.be.backend.dto.ScanBatchItemResultDTO;
// import ap.be.backend.dto.ScanBatchRequestDTO;
// import ap.be.backend.dto.ScanBatchResponseDTO;
// import ap.be.backend.entity.Boek;
// import ap.be.backend.entity.Gebruiker;
// import ap.be.backend.entity.Uitlening;
// import ap.be.backend.repository.BoekRepository;
// import ap.be.backend.repository.UitleningRepository;
// import ap.be.backend.service.BoekService;
// import ap.be.backend.service.GebruikerService;
// import ap.be.backend.service.ScanService;

// @ExtendWith(MockitoExtension.class)
// public class ScanServiceTest {

//     @Mock
//     private GebruikerService gebruikerService;

//     @Mock
//     private BoekService boekService;

//     @Mock
//     private BoekRepository boekRepository;

//     @Mock
//     private UitleningRepository uitleningRepository;

//     @InjectMocks
//     private ScanService scanService;

//     private Gebruiker gebruiker;
//     private Boek boek;

//     @BeforeEach
//     void setUp() {
//         gebruiker = new Gebruiker();
//         gebruiker.setGebruikerID(1L);
//         gebruiker.setSchoolId(12345L);
//         gebruiker.setVoorNaam("Test");
//         gebruiker.setAchternaam("Leerling");

//         boek = new Boek();
//         boek.setId(10L);
//         boek.setIsbn("9780132350884");
//         boek.setTitel("Clean Code");
//         boek.setAantalExemplaren(3);
//     }

//     private ScanBatchRequestDTO buildRequest(String actie, String... isbns) {
//         ScanBatchRequestDTO req = new ScanBatchRequestDTO();
//         req.setGebruikerId(1L);
//         req.setActie(actie);
//         req.setIsbns(List.of(isbns));
//         return req;
//     }

//     // --- INVALID ACTIE ---

//     @Test
//     void givenOnbekendeActie_whenVerwerkBatch_thenThrowIllegalArgumentException() {
//         ScanBatchRequestDTO request = buildRequest("VERNIETIGEN", "9780132350884");
//         when(gebruikerService.getGebruikerById(1L)).thenReturn(gebruiker);

//         IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
//                 () -> scanService.verwerkBatch(request));

//         assertEquals("Onbekende actie: VERNIETIGEN", ex.getMessage());
//         verify(boekRepository, never()).save(any());
//         verify(uitleningRepository, never()).save(any());
//     }

//     // --- UITLENING HAPPY PATH ---

//     @Test
//     void givenBeschikbaarBoek_whenUitlening_thenCreateUitleningAndDecreaseVoorraad() {
//         ScanBatchRequestDTO request = buildRequest("UITLENING", "9780132350884");
//         when(gebruikerService.getGebruikerById(1L)).thenReturn(gebruiker);
//         when(boekService.getBoekByIsbn("9780132350884")).thenReturn(boek);
//         when(uitleningRepository.findFirstByGebruikerAndBoekAndIsIngeleverdFalseOrderByUitleenDatumAsc(gebruiker, boek))
//                 .thenReturn(Optional.empty());

//         ScanBatchResponseDTO response = scanService.verwerkBatch(request);

//         assertEquals("UITLENING", response.getActie());
//         assertEquals(1, response.getResultaten().size());
//         ScanBatchItemResultDTO item = response.getResultaten().get(0);
//         assertEquals("OK", item.getStatus());
//         assertEquals("Clean Code", item.getTitel());
//         assertNull(item.getFoutReden());

//         assertEquals(2, boek.getAantalExemplaren());
//         verify(boekRepository, times(1)).save(boek);

//         ArgumentCaptor<Uitlening> uitleningCaptor = ArgumentCaptor.forClass(Uitlening.class);
//         verify(uitleningRepository, times(1)).save(uitleningCaptor.capture());
//         Uitlening saved = uitleningCaptor.getValue();
//         assertEquals(gebruiker, saved.getGebruiker());
//         assertEquals(boek, saved.getBoek());
//         assertEquals("ACTIEF", saved.getStatus());
//         assertFalse(saved.isIngeleverd());
//         assertNotNull(saved.getUitleenDatum());
//         assertNotNull(saved.getInleverDatum());
//         assertTrue(saved.getInleverDatum().isAfter(saved.getUitleenDatum()));
//     }

//     // --- UITLENING: BOEK NIET GEVONDEN ---

//     @Test
//     void givenIsbnZonderBoek_whenUitlening_thenFoutResult() {
//         ScanBatchRequestDTO request = buildRequest("UITLENING", "0000000000000");
//         when(gebruikerService.getGebruikerById(1L)).thenReturn(gebruiker);
//         when(boekService.getBoekByIsbn("0000000000000")).thenReturn(null);

//         ScanBatchResponseDTO response = scanService.verwerkBatch(request);

//         ScanBatchItemResultDTO item = response.getResultaten().get(0);
//         assertEquals("FOUT", item.getStatus());
//         assertEquals("Boek niet gevonden.", item.getFoutReden());
//         assertNull(item.getTitel());
//         verify(boekRepository, never()).save(any());
//         verify(uitleningRepository, never()).save(any());
//     }

//     // --- UITLENING: AL GELEEND ---

//     @Test
//     void givenBoekAlGeleend_whenUitlening_thenFoutResult() {
//         ScanBatchRequestDTO request = buildRequest("UITLENING", "9780132350884");
//         Uitlening bestaande = new Uitlening();
//         bestaande.setGebruiker(gebruiker);
//         bestaande.setBoek(boek);
//         bestaande.setIngeleverd(false);

//         when(gebruikerService.getGebruikerById(1L)).thenReturn(gebruiker);
//         when(boekService.getBoekByIsbn("9780132350884")).thenReturn(boek);
//         when(uitleningRepository.findFirstByGebruikerAndBoekAndIsIngeleverdFalseOrderByUitleenDatumAsc(gebruiker, boek))
//                 .thenReturn(Optional.of(bestaande));

//         ScanBatchResponseDTO response = scanService.verwerkBatch(request);

//         ScanBatchItemResultDTO item = response.getResultaten().get(0);
//         assertEquals("FOUT", item.getStatus());
//         assertEquals("Je hebt dit boek al geleend.", item.getFoutReden());
//         assertEquals("Clean Code", item.getTitel());
//         assertEquals(3, boek.getAantalExemplaren());
//         verify(boekRepository, never()).save(any());
//         verify(uitleningRepository, never()).save(any());
//     }

//     // --- UITLENING: GEEN EXEMPLAREN ---

//     @Test
//     void givenGeenExemplaren_whenUitlening_thenFoutResult() {
//         boek.setAantalExemplaren(0);
//         ScanBatchRequestDTO request = buildRequest("UITLENING", "9780132350884");

//         when(gebruikerService.getGebruikerById(1L)).thenReturn(gebruiker);
//         when(boekService.getBoekByIsbn("9780132350884")).thenReturn(boek);
//         when(uitleningRepository.findFirstByGebruikerAndBoekAndIsIngeleverdFalseOrderByUitleenDatumAsc(gebruiker, boek))
//                 .thenReturn(Optional.empty());

//         ScanBatchResponseDTO response = scanService.verwerkBatch(request);

//         ScanBatchItemResultDTO item = response.getResultaten().get(0);
//         assertEquals("FOUT", item.getStatus());
//         assertEquals("Geen exemplaren beschikbaar.", item.getFoutReden());
//         assertEquals(0, boek.getAantalExemplaren());
//         verify(boekRepository, never()).save(any());
//         verify(uitleningRepository, never()).save(any());
//     }

//     // --- RETOUR HAPPY PATH ---

//     @Test
//     void givenActieveUitlening_whenRetour_thenMarkIngeleverdAndIncreaseVoorraad() {
//         ScanBatchRequestDTO request = buildRequest("RETOUR", "9780132350884");
//         Uitlening bestaande = new Uitlening();
//         bestaande.setGebruiker(gebruiker);
//         bestaande.setBoek(boek);
//         bestaande.setIngeleverd(false);
//         bestaande.setStatus("ACTIEF");

//         when(gebruikerService.getGebruikerById(1L)).thenReturn(gebruiker);
//         when(boekService.getBoekByIsbn("9780132350884")).thenReturn(boek);
//         when(uitleningRepository.findFirstByGebruikerAndBoekAndIsIngeleverdFalseOrderByUitleenDatumAsc(gebruiker, boek))
//                 .thenReturn(Optional.of(bestaande));

//         ScanBatchResponseDTO response = scanService.verwerkBatch(request);

//         assertEquals("RETOUR", response.getActie());
//         ScanBatchItemResultDTO item = response.getResultaten().get(0);
//         assertEquals("OK", item.getStatus());
//         assertEquals("Clean Code", item.getTitel());
//         assertNull(item.getFoutReden());

//         assertTrue(bestaande.isIngeleverd());
//         assertNotNull(bestaande.getInleverDatum());
//         assertEquals(4, boek.getAantalExemplaren());
//         verify(uitleningRepository, times(1)).save(bestaande);
//         verify(boekRepository, times(1)).save(boek);
//     }

//     // --- RETOUR: GEEN ACTIEVE UITLENING ---

//     @Test
//     void givenGeenActieveUitlening_whenRetour_thenFoutResult() {
//         ScanBatchRequestDTO request = buildRequest("RETOUR", "9780132350884");
//         when(gebruikerService.getGebruikerById(1L)).thenReturn(gebruiker);
//         when(boekService.getBoekByIsbn("9780132350884")).thenReturn(boek);
//         when(uitleningRepository.findFirstByGebruikerAndBoekAndIsIngeleverdFalseOrderByUitleenDatumAsc(gebruiker, boek))
//                 .thenReturn(Optional.empty());

//         ScanBatchResponseDTO response = scanService.verwerkBatch(request);

//         ScanBatchItemResultDTO item = response.getResultaten().get(0);
//         assertEquals("FOUT", item.getStatus());
//         assertEquals("Geen actieve uitlening gevonden om terug te brengen.", item.getFoutReden());
//         assertEquals("Clean Code", item.getTitel());
//         assertEquals(3, boek.getAantalExemplaren());
//         verify(boekRepository, never()).save(any());
//         verify(uitleningRepository, never()).save(any());
//     }

//     // --- RETOUR: BOEK NIET GEVONDEN ---

//     @Test
//     void givenIsbnZonderBoek_whenRetour_thenFoutResult() {
//         ScanBatchRequestDTO request = buildRequest("RETOUR", "0000000000000");
//         when(gebruikerService.getGebruikerById(1L)).thenReturn(gebruiker);
//         when(boekService.getBoekByIsbn("0000000000000")).thenReturn(null);

//         ScanBatchResponseDTO response = scanService.verwerkBatch(request);

//         ScanBatchItemResultDTO item = response.getResultaten().get(0);
//         assertEquals("FOUT", item.getStatus());
//         assertEquals("Boek niet gevonden.", item.getFoutReden());
//         verify(uitleningRepository, never()).save(any());
//     }

//     // --- BATCH: MEERDERE ISBNS MET GEMENGDE UITKOMSTEN ---

//     @Test
//     void givenBatchMetGemengdeIsbns_whenUitlening_thenPerItemResultaat() {
//         Boek boek2 = new Boek();
//         boek2.setId(11L);
//         boek2.setIsbn("9780321125217");
//         boek2.setTitel("Domain-Driven Design");
//         boek2.setAantalExemplaren(0);

//         ScanBatchRequestDTO request = buildRequest("UITLENING",
//                 "9780132350884", "9780321125217", "0000000000000");

//         when(gebruikerService.getGebruikerById(1L)).thenReturn(gebruiker);
//         when(boekService.getBoekByIsbn("9780132350884")).thenReturn(boek);
//         when(boekService.getBoekByIsbn("9780321125217")).thenReturn(boek2);
//         when(boekService.getBoekByIsbn("0000000000000")).thenReturn(null);
//         when(uitleningRepository.findFirstByGebruikerAndBoekAndIsIngeleverdFalseOrderByUitleenDatumAsc(
//                 eq(gebruiker), any(Boek.class))).thenReturn(Optional.empty());

//         ScanBatchResponseDTO response = scanService.verwerkBatch(request);

//         assertEquals(3, response.getResultaten().size());
//         assertEquals("OK", response.getResultaten().get(0).getStatus());
//         assertEquals("FOUT", response.getResultaten().get(1).getStatus());
//         assertEquals("Geen exemplaren beschikbaar.", response.getResultaten().get(1).getFoutReden());
//         assertEquals("FOUT", response.getResultaten().get(2).getStatus());
//         assertEquals("Boek niet gevonden.", response.getResultaten().get(2).getFoutReden());

//         verify(uitleningRepository, times(1)).save(any(Uitlening.class));
//     }

//     // --- ONVERWACHTE EXCEPTIE WORDT AFGEVANGEN PER ITEM ---

//     @Test
//     void givenRepositoryThrows_whenVerwerkBatch_thenFoutResultMetOnverwachteFout() {
//         ScanBatchRequestDTO request = buildRequest("UITLENING", "9780132350884");
//         when(gebruikerService.getGebruikerById(1L)).thenReturn(gebruiker);
//         when(boekService.getBoekByIsbn("9780132350884")).thenReturn(boek);
//         when(uitleningRepository.findFirstByGebruikerAndBoekAndIsIngeleverdFalseOrderByUitleenDatumAsc(gebruiker, boek))
//                 .thenThrow(new RuntimeException("DB down"));

//         ScanBatchResponseDTO response = scanService.verwerkBatch(request);

//         ScanBatchItemResultDTO item = response.getResultaten().get(0);
//         assertEquals("FOUT", item.getStatus());
//         assertTrue(item.getFoutReden().startsWith("Onverwachte fout:"));
//         assertTrue(item.getFoutReden().contains("DB down"));
//     }

//     // --- LEGE ISBN-LIJST LEVERT LEGE RESULTATENLIJST OP ---

//     @Test
//     void givenLegeIsbnLijst_whenVerwerkBatch_thenLegeResultaten() {
//         ScanBatchRequestDTO request = new ScanBatchRequestDTO();
//         request.setGebruikerId(1L);
//         request.setActie("UITLENING");
//         request.setIsbns(List.of());
//         when(gebruikerService.getGebruikerById(1L)).thenReturn(gebruiker);

//         ScanBatchResponseDTO response = scanService.verwerkBatch(request);

//         assertEquals("UITLENING", response.getActie());
//         assertTrue(response.getResultaten().isEmpty());
//         verify(boekRepository, never()).save(any());
//         verify(uitleningRepository, never()).save(any());
//     }
// }
