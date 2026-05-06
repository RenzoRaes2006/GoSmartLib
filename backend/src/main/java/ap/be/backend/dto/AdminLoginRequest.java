package ap.be.backend.dto;

public class AdminLoginRequest {
    private String gebruikersnaam;
    private String wachtwoord;

    public String getGebruikersnaam() { return gebruikersnaam; }
    public void setGebruikersnaam(String gebruikersnaam) { this.gebruikersnaam = gebruikersnaam; }

    public String getWachtwoord() { return wachtwoord; }
    public void setWachtwoord(String wachtwoord) { this.wachtwoord = wachtwoord; }
}
