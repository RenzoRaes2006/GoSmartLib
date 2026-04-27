package ap.be.backend;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import ap.be.backend.entity.Boek;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.service.BoekImportService;
import ap.be.backend.exception.DuplicateBoekException;
import ap.be.backend.exception.InvalidImportDataException;

@ExtendWith(MockitoExtension.class)
public class BoekImportServiceTest {

    @Mock
    private BoekRepository boekRepository;

    @InjectMocks
    private BoekImportService boekImportService;

    // Helper-methode om razendsnel een nep-Excel bestand in het geheugen te maken
    private MockMultipartFile createMockExcelFile(List<String[]> rows) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Boeken");
            for (int i = 0; i < rows.size(); i++) {
                Row row = sheet.createRow(i);
                String[] rowData = rows.get(i);
                for (int j = 0; j < rowData.length; j++) {
                    row.createCell(j).setCellValue(rowData[j]);
                }
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return new MockMultipartFile("file", "test.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", bos.toByteArray());
        }
    }

    @Test
    void givenGeldigBestandZonderIsbn_whenImporteerBestand_thenReturnsAantalGeimporteerdEnSlaatOp() throws Exception {
        // Given
        List<String[]> excelData = Arrays.asList(
                new String[] { "Titel", "Auteur", "ISBN", "Aantal exemplaren" },
                new String[] { "Zelfgemaakt Boek", "De Leerkracht", "", "2" });
        MockMultipartFile file = createMockExcelFile(excelData);

        when(boekRepository.findByTitelAndAuteur("Zelfgemaakt Boek", "De Leerkracht")).thenReturn(Optional.empty());

        // When
        int result = boekImportService.importeerBestand(file);

        // Then
        assertEquals(1, result);
        verify(boekRepository, times(1)).saveAll(anyList());
    }

    @Test
    void givenBestandZonderTitel_whenImporteerBestand_thenThrowsInvalidImportDataException() throws Exception {
        // Given
        List<String[]> excelData = Arrays.asList(
                new String[] { "Titel", "Auteur", "ISBN" },
                new String[] { "", "J.K. Rowling", "1234567890" });
        MockMultipartFile file = createMockExcelFile(excelData);

        // When & Then
        InvalidImportDataException ex = assertThrows(InvalidImportDataException.class, () -> {
            boekImportService.importeerBestand(file);
        });

        String errorMessage = ex.getMessage().toLowerCase();
        assertTrue(errorMessage.contains("titel") || errorMessage.contains("verplicht"),
                "Foutmelding was: " + ex.getMessage());
    }

    @Test
    void givenBestandZonderAuteur_whenImporteerBestand_thenThrowsInvalidImportDataException() throws Exception {
        // Given
        List<String[]> excelData = Arrays.asList(
                new String[] { "Titel", "Auteur", "ISBN" },
                new String[] { "Harry Potter", "", "1234567890" });
        MockMultipartFile file = createMockExcelFile(excelData);

        // When & Then
        InvalidImportDataException ex = assertThrows(InvalidImportDataException.class, () -> {
            boekImportService.importeerBestand(file);
        });

        String errorMessage = ex.getMessage().toLowerCase();
        assertTrue(errorMessage.contains("auteur") || errorMessage.contains("verplicht"),
                "Foutmelding was: " + ex.getMessage());
    }

    @Test
    void givenBestandMetBestaandeTitelEnAuteur_whenImporteerBestand_thenThrowsDuplicateBoekException()
            throws Exception {
        // Given
        List<String[]> excelData = Arrays.asList(
                new String[] { "Titel", "Auteur", "ISBN" },
                new String[] { "De Hobbit", "Tolkien", "" });
        MockMultipartFile file = createMockExcelFile(excelData);

        when(boekRepository.findByTitelAndAuteur("De Hobbit", "Tolkien")).thenReturn(Optional.of(new Boek()));

        // When & Then
        DuplicateBoekException ex = assertThrows(DuplicateBoekException.class, () -> {
            boekImportService.importeerBestand(file);
        });

        assertTrue(ex.getMessage().toLowerCase().contains("bestaat al"));
    }
}