package ap.be.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import ap.be.backend.dto.AdminStatistiekDTO;
import ap.be.backend.dto.LeerkrachtStatistiekDTO;
import ap.be.backend.dto.LeerlingStatistiekDTO;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.repository.GebruikerRepository;
import ap.be.backend.repository.UitleningRepository;
import ap.be.backend.service.StatistiekService;

@ExtendWith(MockitoExtension.class)
class StatistiekServiceTest {

    @Mock
    private UitleningRepository uitleningRepository;

    @Mock
    private BoekRepository boekRepository;

    @Mock
    private GebruikerRepository gebruikerRepository;

    @InjectMocks
    private StatistiekService statistiekService;

    @Test
    void getAdminStatistieken_BerekentPercentageEnVeldenCorrect() {
        when(uitleningRepository.countByIsIngeleverdFalseAndStatusIn(anyList())).thenReturn(20L);
        when(boekRepository.sumTotaalAantalExemplaren()).thenReturn(80L);
        when(uitleningRepository.countByIsIngeleverdFalseAndStatusInAndInleverDatumBefore(anyList(), any(LocalDate.class)))
                .thenReturn(3L);
        when(uitleningRepository.findPopulairsteGenres(anyList(), any(Pageable.class)))
                .thenReturn(List.of("Fantasy"));
        when(uitleningRepository.countLeningenSinds(anyList(), any(LocalDate.class))).thenReturn(7L);

        AdminStatistiekDTO dto = statistiekService.getAdminStatistieken();

        // 20 actief / (80 + 20) = 20% → afgerond op 1 decimaal
        assertEquals(20.0, dto.getPercentageUitgeleend());
        assertEquals(3, dto.getTeLaatIngeleverd());
        assertEquals("Fantasy", dto.getPopulairsteGenre());
        assertEquals(7, dto.getUitleningenDezeMaand());
    }

    @Test
    void getAdminStatistieken_GeeftNullVoorGenreAlsLijstLeeg() {
        when(uitleningRepository.countByIsIngeleverdFalseAndStatusIn(anyList())).thenReturn(0L);
        when(boekRepository.sumTotaalAantalExemplaren()).thenReturn(50L);
        when(uitleningRepository.countByIsIngeleverdFalseAndStatusInAndInleverDatumBefore(anyList(), any(LocalDate.class)))
                .thenReturn(0L);
        when(uitleningRepository.findPopulairsteGenres(anyList(), any(Pageable.class)))
                .thenReturn(Collections.emptyList());
        when(uitleningRepository.countLeningenSinds(anyList(), any(LocalDate.class))).thenReturn(0L);

        AdminStatistiekDTO dto = statistiekService.getAdminStatistieken();

        assertNull(dto.getPopulairsteGenre());
        assertEquals(0.0, dto.getPercentageUitgeleend());
        assertEquals(0, dto.getTeLaatIngeleverd());
        assertEquals(0, dto.getUitleningenDezeMaand());
    }

    @Test
    void getAdminStatistieken_GeeftNulPercentageAlsCapaciteitNul() {
        when(uitleningRepository.countByIsIngeleverdFalseAndStatusIn(anyList())).thenReturn(0L);
        when(boekRepository.sumTotaalAantalExemplaren()).thenReturn(0L);
        when(uitleningRepository.countByIsIngeleverdFalseAndStatusInAndInleverDatumBefore(anyList(), any(LocalDate.class)))
                .thenReturn(0L);
        when(uitleningRepository.findPopulairsteGenres(anyList(), any(Pageable.class)))
                .thenReturn(Collections.emptyList());
        when(uitleningRepository.countLeningenSinds(anyList(), any(LocalDate.class))).thenReturn(0L);

        AdminStatistiekDTO dto = statistiekService.getAdminStatistieken();

        assertEquals(0.0, dto.getPercentageUitgeleend());
    }

    @Test
    void getLeerkrachtStatistieken_MetKlasGebruiktKlasFilter() {
        String klas = "5A";

        when(gebruikerRepository.countByKlasIgnoreCase(klas)).thenReturn(22L);
        when(uitleningRepository.countActieveLeningenVoorKlas(eq(klas), anyList())).thenReturn(8L);
        when(uitleningRepository.countTeLaatVoorKlas(eq(klas), anyList(), any(LocalDate.class))).thenReturn(2L);
        when(uitleningRepository.countLeningenSindsVoorKlas(eq(klas), anyList(), any(LocalDate.class))).thenReturn(5L);
        when(uitleningRepository.findTopBoekTitelsVoorKlas(eq(klas), anyList(), any(Pageable.class)))
                .thenReturn(List.of("Boek A", "Boek B", "Boek C"));

        LeerkrachtStatistiekDTO dto = statistiekService.getLeerkrachtStatistieken(klas);

        assertEquals(22, dto.getAantalLeerlingen());
        assertEquals(8, dto.getAantalActieveLeningen());
        assertEquals(2, dto.getAantalTeLaat());
        assertEquals(5, dto.getBoekenAfgelopenWeek());
        assertEquals(List.of("Boek A", "Boek B", "Boek C"), dto.getTopBoeken());
    }

    @Test
    void getLeerkrachtStatistieken_ZonderKlasGebruiktTotaalAantalLeerlingen() {
        when(gebruikerRepository.count()).thenReturn(150L);
        when(uitleningRepository.countActieveLeningenVoorKlas(eq(null), anyList())).thenReturn(40L);
        when(uitleningRepository.countTeLaatVoorKlas(eq(null), anyList(), any(LocalDate.class))).thenReturn(6L);
        when(uitleningRepository.countLeningenSindsVoorKlas(eq(null), anyList(), any(LocalDate.class))).thenReturn(20L);
        when(uitleningRepository.findTopBoekTitelsVoorKlas(eq(null), anyList(), any(Pageable.class)))
                .thenReturn(List.of("Top1"));

        LeerkrachtStatistiekDTO dto = statistiekService.getLeerkrachtStatistieken(null);

        assertEquals(150, dto.getAantalLeerlingen());
        assertEquals(40, dto.getAantalActieveLeningen());
        assertEquals(List.of("Top1"), dto.getTopBoeken());
    }

    @Test
    void getLeerkrachtStatistieken_LegeStringTeltAlsGeenFilter() {
        when(gebruikerRepository.count()).thenReturn(99L);
        when(uitleningRepository.countActieveLeningenVoorKlas(eq(null), anyList())).thenReturn(0L);
        when(uitleningRepository.countTeLaatVoorKlas(eq(null), anyList(), any(LocalDate.class))).thenReturn(0L);
        when(uitleningRepository.countLeningenSindsVoorKlas(eq(null), anyList(), any(LocalDate.class))).thenReturn(0L);
        when(uitleningRepository.findTopBoekTitelsVoorKlas(eq(null), anyList(), any(Pageable.class)))
                .thenReturn(Collections.emptyList());

        LeerkrachtStatistiekDTO dto = statistiekService.getLeerkrachtStatistieken("   ");

        assertEquals(99, dto.getAantalLeerlingen());
    }

    @Test
    void getLeerlingStatistieken_GeldigSchoolIdRetourneertCounts() {
        long sid = 12345L;
        when(uitleningRepository.countLeengeschiedenisVoorSchoolId(sid, "GEWEIGERD")).thenReturn(11L);
        when(uitleningRepository.countActieveLeningenVoorSchoolId(eq(sid), anyList())).thenReturn(2L);
        when(uitleningRepository.countTeLaatVoorSchoolId(eq(sid), anyList(), any(LocalDate.class))).thenReturn(1L);

        LeerlingStatistiekDTO dto = statistiekService.getLeerlingStatistieken("12345");

        assertEquals(11, dto.getGelezenBoeken());
        assertEquals(10, dto.getLeesDoel());
        assertEquals(2, dto.getAantalActieveLeningen());
        assertEquals(1, dto.getAantalTeLaat());
    }

    @Test
    void getLeerlingStatistieken_OngeldigSchoolIdRetourneertDefaults() {
        LeerlingStatistiekDTO dto = statistiekService.getLeerlingStatistieken("niet-een-getal");

        assertEquals(0, dto.getGelezenBoeken());
        assertEquals(10, dto.getLeesDoel());
        assertEquals(0, dto.getAantalActieveLeningen());
        assertEquals(0, dto.getAantalTeLaat());
    }
}
