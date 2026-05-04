package ap.be.backend.dto;

public class LeerlingStatistiekDTO {
    private Integer gelezenBoeken;
    private Integer leesDoel;
    private Integer aantalActieveLeningen;
    private Integer aantalTeLaat;

    public LeerlingStatistiekDTO() {
    }

    public LeerlingStatistiekDTO(Integer gelezenBoeken, Integer leesDoel, Integer aantalActieveLeningen, Integer aantalTeLaat) {
        this.gelezenBoeken = gelezenBoeken;
        this.leesDoel = leesDoel;
        this.aantalActieveLeningen = aantalActieveLeningen;
        this.aantalTeLaat = aantalTeLaat;
    }

    public Integer getGelezenBoeken() { return gelezenBoeken; }
    public void setGelezenBoeken(Integer gelezenBoeken) { this.gelezenBoeken = gelezenBoeken; }

    public Integer getLeesDoel() { return leesDoel; }
    public void setLeesDoel(Integer leesDoel) { this.leesDoel = leesDoel; }

    public Integer getAantalActieveLeningen() { return aantalActieveLeningen; }
    public void setAantalActieveLeningen(Integer aantalActieveLeningen) { this.aantalActieveLeningen = aantalActieveLeningen; }

    public Integer getAantalTeLaat() { return aantalTeLaat; }
    public void setAantalTeLaat(Integer aantalTeLaat) { this.aantalTeLaat = aantalTeLaat; }
}
