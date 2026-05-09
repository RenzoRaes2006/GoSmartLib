// package ap.be.backend;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.*;

// import java.util.Optional;
// import java.util.HashSet;
// import java.util.List;
// import java.util.Collections;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

// import ap.be.backend.dto.KlassLeeslijstResponseDTO;
// import ap.be.backend.entity.Boek;
// import ap.be.backend.entity.KlassLeeslijst;
// import ap.be.backend.entity.Gebruiker;
// import ap.be.backend.exception.GebruikerNotFoundException;
// import ap.be.backend.exception.KlassLeeslijstNotFoundException;
// import ap.be.backend.repository.BoekRepository;
// import ap.be.backend.repository.KlassLeeslijstRepository;
// import ap.be.backend.repository.GebruikerRepository;
// import ap.be.backend.service.KlassLeeslijstService;

// @ExtendWith(MockitoExtension.class)
// public class KlassLeeslijstServiceTest {

//     @Mock
//     private KlassLeeslijstRepository leeslijstRepository;
//     @Mock
//     private GebruikerRepository gebruikerRepository;
//     @Mock
//     private BoekRepository boekRepository;

//     @InjectMocks
//     private KlassLeeslijstService leeslijstService;

//     private Gebruiker testLeerkracht;
//     private KlassLeeslijst testLijst;
//     private Boek testBoek;

//     @BeforeEach
//     void setUp() {
//         testLeerkracht = new Gebruiker();
//         testLeerkracht.setGebruikerID(1L);

//         testLijst = new KlassLeeslijst();
//         testLijst.setId(10L);
//         testLijst.setNaam("Testlijst");
//         testLijst.setLeerkracht(testLeerkracht);
//         testLijst.setBoeken(new HashSet<>());

//         testBoek = new Boek();
//         testBoek.setId(5L);
//         testBoek.setTitel("Test Boek");
//     }

//     /**
//      * Test of een nieuwe leeslijst correct wordt aangemaakt en opgeslagen
//      * wanneer een bestaande leerkracht en geldige naam worden meegegeven.
//      */
//     @Test
//     void givenGeldigeData_whenCreateLeeslijst_thenSavesAndReturnsDTO() {
//         when(gebruikerRepository.findById(1L)).thenReturn(Optional.of(testLeerkracht));
//         when(leeslijstRepository.save(any(KlassLeeslijst.class))).thenReturn(testLijst);

//         KlassLeeslijstResponseDTO result = leeslijstService.createLeeslijst("Testlijst", 1L);

//         assertNotNull(result);
//         assertEquals("Testlijst", result.naam());
//         verify(leeslijstRepository, times(1)).save(any(KlassLeeslijst.class));
//     }

//     /**
//      * Test of er een exceptie wordt gegooid wanneer er geprobeerd wordt
//      * een leeslijst aan te maken voor een niet-bestaande leerkracht.
//      */
//     @Test
//     void givenLeerkrachtNietGevonden_whenCreateLeeslijst_thenThrowsGebruikerNotFoundException() {
//         when(gebruikerRepository.findById(1L)).thenReturn(Optional.empty());

//         assertThrows(GebruikerNotFoundException.class, () -> {
//             leeslijstService.createLeeslijst("Naam", 1L);
//         });
//     }

//     /**
//      * Test of een bestaand boek succesvol aan een bestaande leeslijst
//      * wordt toegevoegd en de lijst wordt geüpdatet.
//      */
//     @Test
//     void givenGeldigeIds_whenAddBoekToLeeslijst_thenUpdatesLijst() {
//         when(leeslijstRepository.findById(10L)).thenReturn(Optional.of(testLijst));
//         when(boekRepository.findById(5L)).thenReturn(Optional.of(testBoek));
//         when(leeslijstRepository.save(any(KlassLeeslijst.class))).thenReturn(testLijst);

//         KlassLeeslijstResponseDTO result = leeslijstService.addBoekToLeeslijst(10L, 5L);

//         verify(leeslijstRepository, times(1)).save(testLijst);
//     }

//     /**
//      * Test of er een exceptie wordt gegooid wanneer een boek wordt
//      * toegevoegd aan een leeslijst die niet bestaat in de database.
//      */
//     @Test
//     void givenLijstNietGevonden_whenAddBoekToLeeslijst_thenThrowsKlassLeeslijstNotFoundException() {
//         when(leeslijstRepository.findById(10L)).thenReturn(Optional.empty());

//         assertThrows(KlassLeeslijstNotFoundException.class, () -> {
//             leeslijstService.addBoekToLeeslijst(10L, 5L);
//         });
//     }

//     /**
//      * Test of een boek succesvol wordt verwijderd uit een bestaande
//      * leeslijst en de lijst correct wordt opgeslagen in de database.
//      */
//     @Test
//     void givenGeldigeIds_whenRemoveBoekFromLeeslijst_thenRemovesBoek() {
//         testLijst.getBoeken().add(testBoek);
//         when(leeslijstRepository.findById(10L)).thenReturn(Optional.of(testLijst));
//         when(boekRepository.findById(5L)).thenReturn(Optional.of(testBoek));
//         when(leeslijstRepository.save(any(KlassLeeslijst.class))).thenReturn(testLijst);

//         KlassLeeslijstResponseDTO result = leeslijstService.removeBoekFromLeeslijst(10L, 5L);

//         verify(leeslijstRepository, times(1)).save(testLijst);
//         assertTrue(testLijst.getBoeken().isEmpty()); // Verwijdert de koppeling tussen de leeslijst en het boek[cite:
//                                                      // 10]
//     }

//     /**
//      * Test of de naam van een bestaande leeslijst correct kan
//      * worden gewijzigd en de wijziging wordt opgeslagen.
//      */
//     @Test
//     void givenGeldigId_whenUpdateLeeslijstNaam_thenUpdatesNaam() {
//         when(leeslijstRepository.findById(10L)).thenReturn(Optional.of(testLijst));
//         when(leeslijstRepository.save(any(KlassLeeslijst.class))).thenReturn(testLijst);

//         KlassLeeslijstResponseDTO result = leeslijstService.updateLeeslijstNaam(10L, "Nieuwe Naam");

//         verify(leeslijstRepository, times(1)).save(testLijst);
//         assertEquals("Nieuwe Naam", testLijst.getNaam());
//     }

//     /**
//      * Test of een bestaande leeslijst correct wordt verwijderd
//      * uit de database.
//      */
//     @Test
//     void givenGeldigId_whenDeleteLeeslijst_thenDeletesLijst() {
//         when(leeslijstRepository.findById(10L)).thenReturn(Optional.of(testLijst));

//         leeslijstService.deleteLeeslijst(10L);

//         verify(leeslijstRepository, times(1)).delete(testLijst); // Controleert of leeslijstRepository.delete wordt
//                                                                  // aangeroepen[cite: 10]
//     }

//     /**
//      * Test of alle beschikbare klassenleeslijsten worden opgehaald
//      * en succesvol worden omgezet naar een lijst van DTO's.
//      */
//     @Test
//     void givenLeeslijsten_whenGetAllKlassenLeeslijsten_thenReturnsList() {
//         when(leeslijstRepository.findAll()).thenReturn(Collections.singletonList(testLijst));

//         List<KlassLeeslijstResponseDTO> result = leeslijstService.getAllKlassenLeeslijsten();

//         assertNotNull(result);
//         assertEquals(1, result.size());
//         assertEquals("Testlijst", result.get(0).naam());
//         verify(leeslijstRepository, times(1)).findAll();
//     }
// }