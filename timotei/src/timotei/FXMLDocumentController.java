/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package timotei;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 *
 * @author Sadmiral
 */
public class FXMLDocumentController implements Initializable {
    
    @FXML
    private ComboBox<String> kaupunkiComboBox;
    @FXML
    private Button lisääkartalleButton;
    @FXML
    private Button päivitäpakettiButton;
    @FXML
    private Button luopakettiButton;
    @FXML
    private Button lähetäpakettiButton;
    @FXML
    private Button poistareititButton;
    @FXML
    private ComboBox<String> postiComboBox;
    @FXML
    private javafx.scene.web.WebView web;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        timoteiDB tDB = new timoteiDB();
        String prev = null;
        
        Connection connection;
        connection = SqliteConnection.Connector();
        
        if (connection != null) {
            tDB.insertAutomaatit();
        }
        else if (connection == null) {
            System.out.println("Tietokantaan yhdistäminen epäonnistui.");
            System.exit(1);
        }
        
        //luetaan tietokantaan tallennettujen automaattien toimipisteet, ja paketit
        web.getEngine().load(getClass().getResource("index.html").toExternalForm());
        try {
            PreparedStatement psToimipiste = connection.prepareStatement("SELECT toimipaikka FROM automaatti");
            PreparedStatement psPaketti = connection.prepareStatement("SELECT * FROM paketti JOIN esine ON esineID = pakettiID JOIN luokka ON paketti.ID = luokka.ID");
            
            ResultSet rsP = psPaketti.executeQuery();
            ResultSet rsT = psToimipiste.executeQuery();

            while (rsT.next()) {
                if (!rsT.getString(1).equals(prev)) {
                    kaupunkiComboBox.getItems().add(rsT.getString(1));
                }
                prev = rsT.getString(1);
            }

            while (rsP.next()) {
                postiComboBox.getItems().add(rsP.getString(1) + "   " + rsP.getString(5) + " " + rsP.getString(2) + ". Luokka");
            }
            psPaketti.close();
            psToimipiste.close();
            rsT.close();
            rsP.close();
        } catch (SQLException ex) {
        Logger.getLogger(LuoPakettiController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @FXML
    private void drawPin(ActionEvent event) {
        Connection connection = SqliteConnection.Connector();
        
        if (connection == null) {
            System.out.println("Tietokantaan yhdistäminen epäonnistui.");
            System.exit(1);
        }
        
        //piirretään kaikki automaatit käyttäjän antaman toimipisteen mukaan
        try {
            PreparedStatement psAutomaatit = connection.prepareStatement("SELECT * FROM automaatti WHERE toimipaikka = '" + kaupunkiComboBox.getValue() + "'");
            ResultSet rsA = psAutomaatit.executeQuery();
            
            while(rsA.next()) {
                web.getEngine().executeScript("document.goToLocation('" + rsA.getString(2) + ", " + rsA.getString(5) + " " + rsA.getString(3) + "', 'Auki: " + rsA.getString(4) + "', 'red')");
            }
            psAutomaatit.close();
            rsA.close();
        } catch (SQLException ex) {
        Logger.getLogger(LuoPakettiController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    @FXML
    private void drawRoad(ActionEvent event) {
        ArrayList<Float> al = new ArrayList<>();
        Connection connection = SqliteConnection.Connector();
        
        if (connection == null) {
            System.out.println("Tietokantaan yhdistäminen epäonnistui.");
            System.exit(1);
        }
        
        try {
            if (postiComboBox.getValue() != null) {
                Character c = postiComboBox.getValue().charAt(0);
                Integer pktID = Character.getNumericValue(c);
                
                //Haetaan tietokannasta kaikki tarvittavat tiedot teiden, ja automaattien piirtämiseen kartalle
                PreparedStatement psLähtö = connection.prepareStatement("SELECT osoite1, latitude, longitude FROM reitti"
                        + " JOIN paketti ON " + pktID + " = reittiID JOIN automaatti ON osoite1 = osoite");
                PreparedStatement psKohde = connection.prepareStatement("SELECT osoite2, latitude, longitude FROM reitti"
                        + " JOIN paketti ON " + pktID + " = reittiID JOIN automaatti ON osoite2 = osoite");
                PreparedStatement psBreak = connection.prepareStatement("SELECT * FROM esine JOIN paketti ON " + pktID + "= esineID");
                PreparedStatement psLkID = connection.prepareStatement("SELECT ID FROM paketti WHERE pakettiID = ?");
                PreparedStatement psLuokka = connection.prepareStatement("SELECT * FROM luokka WHERE ID = ?");
                PreparedStatement psKuljetus = connection.prepareStatement("INSERT INTO kuljetus (kuljetusID) VALUES (?)");
                PreparedStatement psAutomaatti1 = connection.prepareStatement("SELECT * FROM automaatti JOIN reitti ON osoite1 = osoite JOIN paketti ON " + pktID + " = reittiID WHERE osoite1 = osoite");
                PreparedStatement psAutomaatti2 = connection.prepareStatement("SELECT * FROM automaatti JOIN reitti ON osoite2 = osoite JOIN paketti ON " + pktID + " = reittiID WHERE osoite2 = osoite");
    
                psLkID.setInt(1, pktID);
                ResultSet rsLid = psLkID.executeQuery();
                psLuokka.setInt(1, Integer.valueOf(rsLid.getString(1)));
                
                ResultSet rsL = psLähtö.executeQuery();
                ResultSet rsK = psKohde.executeQuery();
                ResultSet rsB = psBreak.executeQuery();
                ResultSet rsLk = psLuokka.executeQuery();
                ResultSet rsA1 = psAutomaatti1.executeQuery();
                ResultSet rsA2 = psAutomaatti2.executeQuery();
                
                String katuosoite1 = rsA1.getString(2);
                String postinumero1 = rsA1.getString(5);
                String toimipaikka1 = rsA1.getString(3);
                String nimi1 = rsA1.getString(1);
                String aukioloajat1 = rsA2.getString(4);
                String katuosoite2 = rsA2.getString(2);
                String postinumero2 = rsA2.getString(5);
                String toimipaikka2 = rsA2.getString(3);
                String nimi2 = rsA2.getString(1);
                String aukioloajat2 = rsA2.getString(4);
                
                psKuljetus.setInt(1, pktID);
                
                String eNimi = rsB.getString(2);
                Double eKoko = Double.valueOf(rsB.getString(3));
                Boolean eBrk = Boolean.valueOf(rsB.getString(4));
                Float eMassa = Float.valueOf(rsB.getString(5));
                Double maxDist = Double.valueOf(rsLk.getString(2));
                Float maxKG = Float.valueOf(rsLk.getString(3));
                Double maxKoko = Double.valueOf(rsLk.getString(4));
                
                al.add(Float.valueOf(rsL.getString(2)));
                al.add(Float.valueOf(rsL.getString(3)));
                al.add(Float.valueOf(rsK.getString(2)));
                al.add(Float.valueOf(rsK.getString(3)));
                
                Integer luokkaID = Integer.valueOf(rsLk.getString(1));
                Double distance = Distance(Float.parseFloat(rsL.getString(2)), Float.parseFloat(rsL.getString(3)), Float.parseFloat(rsK.getString(2)), Float.parseFloat(rsK.getString(3)));
                
                //Tarkastetaan meneekö esine rikki/voiko pakettia lähettää
                if (luokkaID == 1) {
                    if (distance > maxDist) {
                        try {
                            FXMLLoader Loader = new FXMLLoader();
                            Loader.setLocation(getClass().getResource("popupBrk.fxml"));
                            Loader.load();

                            PopupLabelController display = Loader.getController();
                            display.setText("Ensimmäisen luokan pakettia ei voi lähettää yli 150km päähän!");
                            Parent p = Loader.getRoot();
                            Stage stage = new Stage();
                            stage.setScene(new Scene(p));
                            stage.show();
                        } catch (IOException ex) {
                            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    else if (eBrk == true) {
                        web.getEngine().executeScript("document.goToLocation('" + katuosoite1 + ", " + postinumero1 + " " + toimipaikka1 + "','" + nimi1 + " Aukioloajat: " + aukioloajat1 + "', 'blue')");
                        web.getEngine().executeScript("document.goToLocation('" + katuosoite2 + ", " + postinumero2 + " " + toimipaikka2 + "','" + nimi2 + " Aukioloajat: " + aukioloajat2 + "', 'yellow')");
                        web.getEngine().executeScript("document.createPath(" + al + ", 'red', 2)");
                        try {
                            FXMLLoader Loader = new FXMLLoader();
                            Loader.setLocation(getClass().getResource("popupBrk.fxml"));
                            Loader.load();

                            PopupLabelController display = Loader.getController();
                            display.setText("Paketin sisältämä esine meni rikki!");
                            Parent p = Loader.getRoot();
                            Stage stage = new Stage();
                            stage.setScene(new Scene(p));
                            stage.show();
                        } catch (IOException ex) {
                            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    else {
                        web.getEngine().executeScript("document.goToLocation('" + katuosoite1 + ", " + postinumero1 + " " + toimipaikka1 + "','" + nimi1 + " Aukioloajat: " + aukioloajat1 + "', 'blue')");
                        web.getEngine().executeScript("document.goToLocation('" + katuosoite2 + ", " + postinumero2 + " " + toimipaikka2 + "','" + nimi2 + " Aukioloajat: " + aukioloajat2 + "', 'yellow')");
                        web.getEngine().executeScript("document.createPath(" + al + ", 'red', 2)");
                    }
                }
                else if (luokkaID == 2) {
                    if (eKoko > maxKoko & eBrk == true) {
                        web.getEngine().executeScript("document.goToLocation('" + katuosoite1 + ", " + postinumero1 + " " + toimipaikka1 + "','" + nimi1 + " Aukioloajat: " + aukioloajat1 + "', 'blue')");
                        web.getEngine().executeScript("document.goToLocation('" + katuosoite2 + ", " + postinumero2 + " " + toimipaikka2 + "','" + nimi2 + " Aukioloajat: " + aukioloajat2 + "', 'yellow')");
                        web.getEngine().executeScript("document.createPath(" + al + ", 'red', 2)");
                        try {
                            FXMLLoader Loader = new FXMLLoader();
                            Loader.setLocation(getClass().getResource("popupBrk.fxml"));
                            Loader.load();

                            PopupLabelController display = Loader.getController();
                            display.setText("Paketin sisältämä esine meni rikki!");
                            Parent p = Loader.getRoot();
                            Stage stage = new Stage();
                            stage.setScene(new Scene(p));
                            stage.show();
                        } catch (IOException ex) {
                            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    else {
                        web.getEngine().executeScript("document.goToLocation('" + katuosoite1 + ", " + postinumero1 + " " + toimipaikka1 + "','" + nimi1 + " Aukioloajat: " + aukioloajat1 + "', 'blue')");
                        web.getEngine().executeScript("document.goToLocation('" + katuosoite2 + ", " + postinumero2 + " " + toimipaikka2 + "','" + nimi2 + " Aukioloajat: " + aukioloajat2 + "', 'yellow')");
                        web.getEngine().executeScript("document.createPath(" + al + ", 'red', 2)");
                    }
                }
                else if (luokkaID == 3) {
                    if (eBrk == true & eKoko < 12000 | eMassa < 12) {
                        web.getEngine().executeScript("document.goToLocation('" + katuosoite1 + ", " + postinumero1 + " " + toimipaikka1 + "','" + nimi1 + " Aukioloajat: " + aukioloajat1 + "', 'blue')");
                        web.getEngine().executeScript("document.goToLocation('" + katuosoite2 + ", " + postinumero2 + " " + toimipaikka2 + "','" + nimi2 + " Aukioloajat: " + aukioloajat2 + "', 'yellow')");
                        web.getEngine().executeScript("document.createPath(" + al + ", 'red', 2)");
                        try {
                            FXMLLoader Loader = new FXMLLoader();
                            Loader.setLocation(getClass().getResource("popupBrk.fxml"));
                            Loader.load();

                            PopupLabelController display = Loader.getController();
                            display.setText("Paketin sisältämä esine meni rikki!");
                            Parent p = Loader.getRoot();
                            Stage stage = new Stage();
                            stage.setScene(new Scene(p));
                            stage.show();
                        } catch (IOException ex) {
                            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    else {
                        web.getEngine().executeScript("document.goToLocation('" + katuosoite1 + ", " + postinumero1 + " " + toimipaikka1 + "','" + nimi1 + " Aukioloajat: " + aukioloajat1 + "', 'blue')");
                        web.getEngine().executeScript("document.goToLocation('" + katuosoite2 + ", " + postinumero2 + " " + toimipaikka2 + "','" + nimi2 + " Aukioloajat: " + aukioloajat2 + "', 'yellow')");
                        web.getEngine().executeScript("document.createPath(" + al + ", 'red', 2)");
                    }
                }
                psKuljetus.close();
                psBreak.close();
                psLähtö.close();
                psKohde.close();
                psLuokka.close();
                psLkID.close();
                psAutomaatti1.close();
                psAutomaatti2.close();
                rsA1.close();
                rsA2.close();
                rsL.close();
                rsK.close();
                rsB.close();
                rsLk.close();
                rsLid.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(LuoPakettiController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @FXML
    private void deleteRoads(ActionEvent event) {
        web.getEngine().executeScript("document.deletePaths()");
    }
    @FXML
    private void luoPaketti(ActionEvent event) {
        try {
            Stage paketinluonti = new Stage();
            Parent page = FXMLLoader.load(getClass().getResource("luoPaketti.fxml"));
            
            Scene scene = new Scene(page);
            
            paketinluonti.setScene(scene);
            paketinluonti.show();
            
        } catch (IOException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    @FXML
    private void muokkaaPakettia(ActionEvent event) {
        
        if (postiComboBox.getValue() != null) {
                Character c = postiComboBox.getValue().charAt(0);
                Integer pktID = Character.getNumericValue(c);

                try {
                    FXMLLoader Loader = new FXMLLoader();
                    Loader.setLocation(getClass().getResource("muokkaaPakettia.fxml"));
                    Loader.load();
                    MuokkaaPakettiaController display = Loader.getController();
                    display.setValues(pktID);
                    Parent p = Loader.getRoot();
                    Stage stage = new Stage();
                    stage.setScene(new Scene(p));
                    stage.show();
                } catch (IOException ex) {
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
    }
    //Päivitetään paketit comboboxiin aina kun comboboxi valitaan
    @FXML
    private void updatePaketit(MouseEvent event) {
        String prev = null;
        
        Connection connection;
        connection = SqliteConnection.Connector();
        
        if (connection == null) {
            System.out.println("Tietokantaan yhdistäminen epäonnistui.");
            System.exit(1);
        }
        
        try {
            PreparedStatement psPaketti = connection.prepareStatement("SELECT * FROM paketti JOIN esine ON esineID = pakettiID JOIN luokka ON paketti.ID = luokka.ID");
            
            ResultSet rsP = psPaketti.executeQuery();

            while (postiComboBox.getItems().size() > 0)
                postiComboBox.getItems().remove(0);
            while (rsP.next()) {
                postiComboBox.getItems().add(rsP.getString(1) + "   " + rsP.getString(5) + " " + rsP.getString(2) + ". Luokka");
            }
            psPaketti.close();
            rsP.close();
            
            } catch (SQLException ex) {
            Logger.getLogger(LuoPakettiController.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
    //Lasketaan automaattien välinen etäisyys longituden ja latituden avulla
    public static double Distance(float lng1, float lat1, float lng2, float lat2) {
        double earthRadius = 6371000; 
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        float dist = (float) (earthRadius * c)/1000;

        return dist;
    }
}