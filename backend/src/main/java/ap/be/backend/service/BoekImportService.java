package ap.be.backend.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import ap.be.backend.entity.Boek;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.exception.IsbnAlreadyExistsException;

@Service
public class BoekImportService {

    @Autowired
    private BoekRepository boekRepository;

    public int importeerBestand(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename != null && (filename.endsWith(".xls") || filename.endsWith(".xlsx"))) {
            return importeerExcel(file);
        } else {
            throw new IllegalArgumentException(
                    "Ongeldig bestandstype. Alleen Excel (.xlsx of .xls) wordt ondersteund.");
        }
    }

    // EXCEL PARSEN
    private int importeerExcel(MultipartFile file) {
        List<Boek> boekenOmOpTeSlaan = new ArrayList<>();
        DataFormatter dataFormatter = new DataFormatter();

        try (InputStream is = file.getInputStream();
                Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            int rijNummer = 1;
            boolean isHeader = true;
            Map<String, Integer> headerMap = new HashMap<>();

            while (rows.hasNext()) {
                Row currentRow = rows.next();

                if (isHeader) {
                    for (Cell cell : currentRow) {
                        headerMap.put(dataFormatter.formatCellValue(cell).trim(), cell.getColumnIndex());
                    }
                    isHeader = false;
                    continue;
                }

                rijNummer++;

                boolean isEmptyRow = true;
                for (int c = currentRow.getFirstCellNum(); c < currentRow.getLastCellNum(); c++) {
                    Cell cell = currentRow.getCell(c);
                    if (cell != null && cell.getCellType() != CellType.BLANK) {
                        isEmptyRow = false;
                        break;
                    }
                }
                if (isEmptyRow)
                    continue;

                try {
                    Boek boek = new Boek();

                    boek.setTitel(getWaarde(currentRow, headerMap.get("Titel"), dataFormatter));
                    boek.setAuteur(getWaarde(currentRow, headerMap.get("Auteur"), dataFormatter));
                    String isbn = getWaarde(currentRow, headerMap.get("ISBN"), dataFormatter);

                    if (isbn == null || isbn.isEmpty()) {
                        throw new IllegalArgumentException("ISBN ontbreekt.");
                    }
                    if (boekRepository.findByIsbn(isbn).isPresent()) {
                        throw new IsbnAlreadyExistsException("ISBN " + isbn + " bestaat al in de database.");
                    }
                    boek.setIsbn(isbn);

                    boek.setUitgever(getWaarde(currentRow, headerMap.get("Uitgever"), dataFormatter));
                    boek.setUitgaveJaar(getWaarde(currentRow, headerMap.get("Uitgavejaar"), dataFormatter));

                    String paginaStr = getWaarde(currentRow, headerMap.get("Pagina"), dataFormatter);
                    boek.setPagina((paginaStr != null && !paginaStr.isEmpty()) ? Integer.parseInt(paginaStr) : 0);

                    String exemplarenStr = getWaarde(currentRow, headerMap.get("Aantal exemplaren"), dataFormatter);
                    boek.setAantalExemplaren(
                            (exemplarenStr != null && !exemplarenStr.isEmpty()) ? Integer.parseInt(exemplarenStr) : 1);

                    String genreString = getWaarde(currentRow, headerMap.get("Genre"), dataFormatter);
                    if (genreString != null && !genreString.isEmpty()) {
                        boek.setGenre(new ArrayList<>(List.of(genreString.split("\\s*,\\s*"))));
                    } else {
                        boek.setGenre(new ArrayList<>());
                    }

                    boek.setBeschrijving(getWaarde(currentRow, headerMap.get("Beschrijving"), dataFormatter));
                    boek.setLocatie(getWaarde(currentRow, headerMap.get("Locatie"), dataFormatter));
                    boek.setAfbeelding(getWaarde(currentRow, headerMap.get("Afbeelding URL"), dataFormatter));
                    boek.setLeesniveau(getWaarde(currentRow, headerMap.get("Leesniveau"), dataFormatter));

                    boekenOmOpTeSlaan.add(boek);

                } catch (IsbnAlreadyExistsException e) {
                    throw e;
                } catch (Exception e) {
                    throw new IllegalArgumentException("Fout in rij " + rijNummer + ": " + e.getMessage());
                }
            }

            boekRepository.saveAll(boekenOmOpTeSlaan);
            return boekenOmOpTeSlaan.size();

        } catch (Exception e) {
            if (e instanceof IllegalArgumentException || e instanceof IsbnAlreadyExistsException) {
                throw (RuntimeException) e;
            }
            throw new IllegalArgumentException(
                    "Kan bestand niet inlezen. Controleer of het een geldig Excel-bestand is.");
        }
    }

    private String getWaarde(Row row, Integer colIndex, DataFormatter formatter) {
        if (colIndex == null)
            return null;
        Cell cell = row.getCell(colIndex);
        if (cell == null)
            return null;
        return formatter.formatCellValue(cell).trim();
    }
}