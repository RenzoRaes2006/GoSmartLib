package ap.be.backend;

import ap.be.backend.entity.Boek;
import ap.be.backend.entity.LeeslijstMap;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.repository.LeeslijstMapRepository;
import ap.be.backend.service.GebruikerService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GebruikerServiceTest {

    @Mock
    private LeeslijstMapRepository leeslijstMapRepository;

    @Mock
    private BoekRepository boekRepository;

    @InjectMocks
    private GebruikerService gebruikerService;

    private Boek testBoek;

    @BeforeEach
    void setUp() {
        testBoek = new Boek();
        testBoek.setId(1L);
        testBoek.setTitel("Test Boek");
    }

    @Test
    void testVerwijderBoekUitMap_VerwijdertMapAlsLeeg_TenzijFavorieten() {
        // Arrange: Maak een normale map "School" met 1 boek
        LeeslijstMap schoolMap = new LeeslijstMap();
        schoolMap.setNaam("School");
        schoolMap.getBoeken().add(testBoek);

        when(leeslijstMapRepository.findByGebruiker_GebruikerIDAndNaam(1L, "School"))
                .thenReturn(Optional.of(schoolMap));
        when(boekRepository.findById(1L)).thenReturn(Optional.of(testBoek));

        // Act: Verwijder het boek
        gebruikerService.verwijderBoekUitMap(1L, "School", 1L);

        // Assert: Map is nu leeg, dus delete() moet zijn aangeroepen!
        verify(leeslijstMapRepository, times(1)).delete(schoolMap);
        verify(leeslijstMapRepository, never()).save(schoolMap);
    }

    @Test
    void testVerwijderBoekUitMap_BehoudtMapAlsLeeg_OmdatHetFavorietenIs() {
        // Arrange: Maak de "Favorieten" map met 1 boek
        LeeslijstMap favoMap = new LeeslijstMap();
        favoMap.setNaam("Favorieten");
        favoMap.getBoeken().add(testBoek);

        when(leeslijstMapRepository.findByGebruiker_GebruikerIDAndNaam(1L, "Favorieten"))
                .thenReturn(Optional.of(favoMap));
        when(boekRepository.findById(1L)).thenReturn(Optional.of(testBoek));

        // Act: Verwijder het boek
        gebruikerService.verwijderBoekUitMap(1L, "Favorieten", 1L);

        // Assert: Map is nu leeg, maar mag NIET gedelete worden! save() moet zijn
        // aangeroepen.
        verify(leeslijstMapRepository, never()).delete(favoMap);
        verify(leeslijstMapRepository, times(1)).save(favoMap);
    }
}
