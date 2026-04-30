package ap.be.backend.dto;

public class AdminStatistiekDTO {
    private Double percentageUitgeleend;
    private Integer teLaatIngeleverd;
    private String populairsteGenre;
    private Integer uitleningenDezeMaand;

    public AdminStatistiekDTO() {
    }

    public AdminStatistiekDTO(Double percentageUitgeleend, Integer teLaatIngeleverd, String populairsteGenre, Integer uitleningenDezeMaand) {
        this.percentageUitgeleend = percentageUitgeleend;
        this.teLaatIngeleverd = teLaatIngeleverd;
        this.populairsteGenre = populairsteGenre;
        this.uitleningenDezeMaand = uitleningenDezeMaand;
    }

    public Double getPercentageUitgeleend() { return percentageUitgeleend; }
    public void setPercentageUitgeleend(Double percentageUitgeleend) { this.percentageUitgeleend = percentageUitgeleend; }

    public Integer getTeLaatIngeleverd() { return teLaatIngeleverd; }
    public void setTeLaatIngeleverd(Integer teLaatIngeleverd) { this.teLaatIngeleverd = teLaatIngeleverd; }

    public String getPopulairsteGenre() { return populairsteGenre; }
    public void setPopulairsteGenre(String populairsteGenre) { this.populairsteGenre = populairsteGenre; }

    public Integer getUitleningenDezeMaand() { return uitleningenDezeMaand; }
    public void setUitleningenDezeMaand(Integer uitleningenDezeMaand) { this.uitleningenDezeMaand = uitleningenDezeMaand; }
}
