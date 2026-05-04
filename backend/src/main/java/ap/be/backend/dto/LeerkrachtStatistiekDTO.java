package ap.be.backend.dto;

import java.util.ArrayList;
import java.util.List;

public class LeerkrachtStatistiekDTO {
    private Integer aantalLeerlingen;
    private Integer aantalActieveLeningen;
    private Integer aantalTeLaat;
    private Integer boekenAfgelopenWeek;
    private List<String> topBoeken;

    public LeerkrachtStatistiekDTO() {
        this.topBoeken = new ArrayList<>();
    }

    public LeerkrachtStatistiekDTO(Integer aantalLeerlingen, Integer aantalActieveLeningen, Integer aantalTeLaat,
                                   Integer boekenAfgelopenWeek, List<String> topBoeken) {
        this.aantalLeerlingen = aantalLeerlingen;
        this.aantalActieveLeningen = aantalActieveLeningen;
        this.aantalTeLaat = aantalTeLaat;
        this.boekenAfgelopenWeek = boekenAfgelopenWeek;
        this.topBoeken = topBoeken == null ? new ArrayList<>() : topBoeken;
    }

    public Integer getAantalLeerlingen() { return aantalLeerlingen; }
    public void setAantalLeerlingen(Integer aantalLeerlingen) { this.aantalLeerlingen = aantalLeerlingen; }

    public Integer getAantalActieveLeningen() { return aantalActieveLeningen; }
    public void setAantalActieveLeningen(Integer aantalActieveLeningen) { this.aantalActieveLeningen = aantalActieveLeningen; }

    public Integer getAantalTeLaat() { return aantalTeLaat; }
    public void setAantalTeLaat(Integer aantalTeLaat) { this.aantalTeLaat = aantalTeLaat; }

    public Integer getBoekenAfgelopenWeek() { return boekenAfgelopenWeek; }
    public void setBoekenAfgelopenWeek(Integer boekenAfgelopenWeek) { this.boekenAfgelopenWeek = boekenAfgelopenWeek; }

    public List<String> getTopBoeken() { return topBoeken; }
    public void setTopBoeken(List<String> topBoeken) { this.topBoeken = topBoeken; }
}
