package ap.be.backend;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import ap.be.backend.entity.Boek;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.service.BoekImportService;
import ap.be.backend.exception.IsbnAlreadyExistsException;

@ExtendWith(MockitoExtension.class)
public class BoekImportServiceTest {

    @Mock
    private BoekRepository boekRepository;

    @InjectMocks
    private BoekImportService boekImportService;

    private MockMultipartFile geldigExcelBestand;

    @BeforeEach
    void setUp() throws Exception {
        // Maak een in-memory Excel bestand aan voor de tests
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Boeken");

        // Header rij
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Titel");
        header.createCell(1).setCellValue("Auteur");
        header.createCell(2).setCellValue("ISBN");
        header.createCell(3).setCellValue("Aantal exemplaren");

        // Data rij
        Row data = sheet.createRow(1);
        data.createCell(0).setCellValue("Test Boek");
        data.createCell(1).setCellValue("Test Auteur");
        data.createCell(2).setCellValue("9781234567890");
        data.createCell(3).setCellValue(5);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();

        geldigExcelBestand = new MockMultipartFile(
                "file",
                "test-boeken.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                bos.toByteArray());
    }

    @Test
    void importeerBestand_VerkeerdBestandstype_GooitIllegalArgumentException() {
        MockMultipartFile foutBestand = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Dit is geen excel".getBytes());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            boekImportService.importeerBestand(foutBestand);
        });

        assertTrue(exception.getMessage().contains("Ongeldig bestandstype"));
    }

    @Test
    void importeerBestand_GeldigBestand_SlaatBoekenOp() {
        when(boekRepository.findByIsbn(anyString())).thenReturn(Optional.empty());

        int resultaat = boekImportService.importeerBestand(geldigExcelBestand);

        assertEquals(1, resultaat);
        verify(boekRepository, times(1)).saveAll(any());
    }

    @Test
    void importeerBestand_IsbnBestaatAl_GooitIsbnAlreadyExistsException() {
        // Simuleer dat het boek al in de database zit
        when(boekRepository.findByIsbn("9781234567890")).thenReturn(Optional.of(new Boek()));

        IsbnAlreadyExistsException exception = assertThrows(IsbnAlreadyExistsException.class, () -> {
            boekImportService.importeerBestand(geldigExcelBestand);
        });

        assertTrue(exception.getMessage().contains("bestaat al in de database"));
        verify(boekRepository, never()).saveAll(any());
    }
}