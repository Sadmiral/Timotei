package timotei;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Sadmiral
 */
public class MuokkaaPakettiaController implements Initializable {
    
    @FXML
    private ComboBox<String> lähtöComboBox;
    @FXML
    private ComboBox<String> lähtöautomaattiComboBox;
    @FXML
    private ComboBox<String> kohdeComboBox;
    @FXML
    private ComboBox<String> kohdeautomaattiComboBox;
    @FXML
    private ComboBox<String> valitseluokkaComboBox;
    @FXML
    private Button peruutaButton;
    @FXML
    private Button luopakettiButton;
    @FXML
    private TextField nimiField;
    @FXML
    private TextField kokoField;
    @FXML
    private TextField massaField;
    @FXML
    private CheckBox särkyvääCheckBox;
    @FXML
    private Label infotextArea;
    @FXML
    private Label pktidLabel;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Connection connection = SqliteConnection.Connector();
        String prev = null;
        
        if (connection == null) {
            System.out.println("Tietokantaan yhdistäminen epäonnistui.");
            System.exit(1);
        }
        
        //Haetaan comboboxeihin tarvittavat tiedot
        try {
            PreparedStatement psLuokka = connection.prepareStatement("SELECT ID FROM luokka");
            PreparedStatement psToimipiste = connection.prepareStatement("SELECT toimipaikka FROM automaatti");
             
            ResultSet rsL = psLuokka.executeQuery();
            ResultSet rsT = psToimipiste.executeQuery();           
            
            while (rsL.next()) {
                valitseluokkaComboBox.getItems().add(rsL.getString(1) + ". Luokka");
            }
            
            while (rsT.next()) {
                if (!rsT.getString(1).equals(prev)) {
                    lähtöComboBox.getItems().add(rsT.getString(1));
                    kohdeComboBox.getItems().add(rsT.getString(1));
                }
                prev = rsT.getString(1);
            }
            psLuokka.close();
            psToimipiste.close();
            rsT.close();
            rsL.close();
            
            } catch (SQLException ex) {
            Logger.getLogger(LuoPakettiController.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
    //Alustetaan aluksi paketin alkuperäiset arvot kenttiin
    public void setValues(Integer pID) {
        Connection connection;
        connection = SqliteConnection.Connector();
        pktidLabel.setText(String.valueOf(pID));
        
        if (connection == null) {
            System.out.println("Tietokantaan yhdistäminen epäonnistui.");
            System.exit(1);
        }
        try {
            PreparedStatement psPaketti = connection.prepareStatement("SELECT * FROM paketti JOIN esine ON esineID = ? "
                    + "JOIN asiakas ON paketti.asiakasID = asiakas.asiakasID WHERE esineID = pakettiID AND paketti.asiakasID = asiakas.asiakasID");
            psPaketti.setInt(1,pID);
            ResultSet rsP = psPaketti.executeQuery();

            Integer ID = Integer.valueOf(rsP.getString(2));
            String nimi = rsP.getString(5);
            Double koko = Double.valueOf(rsP.getString(6));
            Boolean brk = Boolean.valueOf(rsP.getString(7));
            Float massa = Float.valueOf(rsP.getString(8));
            String aNimi = rsP.getString(10);
            
            if (brk == true)
                särkyvääCheckBox.setSelected(true);
            
            nimiField.setText(nimi);
            kokoField.setText(String.valueOf(koko));
            massaField.setText(String.valueOf(massa));
            
            psPaketti.close();
            rsP.close();
        } catch (SQLException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @FXML
    private void lähtöSelect(ActionEvent event) {
        Connection connection = SqliteConnection.Connector();
        
        if (connection == null) {
            System.out.println("Tietokantaan yhdistäminen epäonnistui.");
            System.exit(1);
        }
        
        try {
            PreparedStatement psLähtö = connection.prepareStatement("SELECT nimi FROM automaatti WHERE toimipaikka = '" + lähtöComboBox.getValue() + "'");
            ResultSet rsL = psLähtö.executeQuery();
            
            while (lähtöautomaattiComboBox.getItems().size() > 0)
                lähtöautomaattiComboBox.getItems().remove(0);
            while (rsL.next()) {
                lähtöautomaattiComboBox.getItems().add(rsL.getString(1));
            }
            rsL.close();
            psLähtö.close();
            } catch (SQLException ex) {
            Logger.getLogger(LuoPakettiController.class.getName()).log(Level.SEVERE, null, ex);
            }
    }

    @FXML
    private void kohdeSelect(ActionEvent event) {
        Connection connection = SqliteConnection.Connector();
        
        if (connection == null) {
            System.out.println("Tietokantaan yhdistäminen epäonnistui.");
            System.exit(1);
        }
        try {
            PreparedStatement psKohde = connection.prepareStatement("SELECT nimi FROM automaatti WHERE toimipaikka = '" + kohdeComboBox.getValue() + "'");
            
            ResultSet rsK = psKohde.executeQuery();
            
            while (kohdeautomaattiComboBox.getItems().size() > 0)
                kohdeautomaattiComboBox.getItems().remove(0);
            while (rsK.next()) {
                kohdeautomaattiComboBox.getItems().add(rsK.getString(1));
            }
            psKohde.close();
            rsK.close();
            } catch (SQLException ex) {
            Logger.getLogger(LuoPakettiController.class.getName()).log(Level.SEVERE, null, ex);
            }
    }

    @FXML
    private void luokanInfo(ActionEvent event) {
        if (valitseluokkaComboBox.getValue().equals("1. Luokka"))
            infotextArea.setText("Ensimmäisen luokan toimitukset ovat nopeimpia, mutta toimituksien maksimi etäisyys on 150km. Myöskin kaikki särkyvät esineet tulevat menemään rikki ensimmäisen luokan lähetyksessä.");
        
        else if (valitseluokkaComboBox.getValue().equals("2. Luokka"))
            infotextArea.setText("Toisen luokan toimitukset ovat turvallisimpia, ja luokka sallii särkyvien esineiden lähettämisen kunhan esineet eivät ole liian isoja (<12000cm^3).");
        
        else if (valitseluokkaComboBox.getValue().equals("3. Luokka"))
            infotextArea.setText("Kolmannen luokan toimitukset ovat kaikkein riskialttiimpia särkymiselle, ellei esine ole suuri kokoinen (>12000cm^3) ja painava (>12kg). Tämä on myös kaikkein hitain toimitusluokka.");
        else
            infotextArea.setText("");
    }

    @FXML
    private void Peruuta(ActionEvent event) throws SQLException {
        Stage stage = (Stage) peruutaButton.getScene().getWindow();
        stage.close();
    }

    //Muutetaan pakettia tietokannassa käyttäjän syötteiden mukaan
    @FXML
    private void Luo(ActionEvent event) {
        Connection connection = SqliteConnection.Connector();
        Character c = valitseluokkaComboBox.getValue().charAt(0);
        Integer lk = Character.getNumericValue(c);
        
        if (connection == null) {
            System.out.println("Tietokantaan yhdistäminen epäonnistui.");
            System.exit(1);
        }
        
        if (!nimiField.getText().isEmpty() & !kokoField.getText().isEmpty() & !massaField.getText().isEmpty() & valitseluokkaComboBox.getValue() != null & lähtöautomaattiComboBox.getValue() != null & kohdeautomaattiComboBox.getValue() != null) {
            String eNimi = nimiField.getText();
            Double eKoko = Double.valueOf(kokoField.getText());
            Float eMassa = Float.valueOf(massaField.getText());
            Boolean eBrk = särkyvääCheckBox.isSelected();

            try {
            Integer pktID = Integer.valueOf(pktidLabel.getText());

            PreparedStatement psEsine = connection.prepareStatement("UPDATE esine SET (nimi, koko, breakable, massa) = (?,?,?,?) WHERE esineID = ?");
            PreparedStatement psOsoite1 = connection.prepareStatement("SELECT osoite FROM automaatti WHERE nimi = ?");
            PreparedStatement psOsoite2 = connection.prepareStatement("SELECT osoite FROM automaatti WHERE nimi = ?");
            PreparedStatement psPaketti = connection.prepareStatement("UPDATE paketti SET (ID) = (?)");
            PreparedStatement psReitti = connection.prepareStatement("UPDATE reitti SET (osoite1, osoite2) = (?,?) WHERE reittiID = ?");

            psEsine.setString(1, eNimi);
            psEsine.setDouble(2, eKoko);
            psEsine.setBoolean(3, eBrk);
            psEsine.setFloat(4, eMassa);
            psEsine.setInt(5, pktID);
            psEsine.executeUpdate();
            
            psOsoite1.setString(1, lähtöautomaattiComboBox.getValue());
            ResultSet rsO1 = psOsoite1.executeQuery();
            psOsoite2.setString(1, kohdeautomaattiComboBox.getValue());
            ResultSet rsO2 = psOsoite2.executeQuery();
            
            psReitti.setString(1, rsO1.getString(1));
            psReitti.setString(2, rsO2.getString(1));
            psReitti.setInt(3, pktID);
            
            psReitti.executeUpdate();

            psPaketti.setInt(1, lk);
            psPaketti.executeUpdate();
            
            Stage stage = (Stage) luopakettiButton.getScene().getWindow();
            stage.close();
            
            rsO1.close();
            rsO2.close();           
            psPaketti.close();
            psEsine.close();
            psOsoite1.close();
            psOsoite2.close();
            psReitti.close();
            } catch (SQLException ex) {
                Logger.getLogger(LuoPakettiController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else {
            infotextArea.setText("Syötä tarvittavat tiedot ennen paketin luontia!");
        }
    }
}
