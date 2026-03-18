package ap.be.backend.dto;

public class LeenStatusResponse {
    private boolean magLenen;
    private String reden;

    public LeenStatusResponse(boolean magLenen, String reden) {
        this.magLenen = magLenen;
        this.reden = reden;
    }

    public boolean isMagLenen() {
        return magLenen;
    }

    public String getReden() {
        return reden;
    }
}
