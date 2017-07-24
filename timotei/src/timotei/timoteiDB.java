package timotei;

import java.io.IOException;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class timoteiDB {
    Connection connection;
    
    public timoteiDB() {
        connection = SqliteConnection.Connector();
        if (connection == null)
            System.exit(1);
    }
    public boolean isDbConnected() {
        try {
            return  !connection.isClosed();
        } catch(SQLException e) {
            return false;
        }
    }
    public void insertAutomaatit() {
        Connection connection = SqliteConnection.Connector();
        
        if (connection == null) {
            System.out.println("Tietokantaan yhdistäminen epäonnistui.");
            System.exit(1);
        }
        
        try {
            String query = "INSERT OR IGNORE INTO automaatti (nimi, osoite, toimipaikka, aukioloaika, postinumero, longitude, latitude) VALUES (?,?,?,?,?,?,?)";

            PreparedStatement psAutomaatti = connection.prepareStatement(query);
            
            try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            try {
                Document doc = builder.parse("http://iseteenindus.smartpost.ee/api/?request=destinations&country=FI&type=APT");
                NodeList nimiList = doc.getElementsByTagName("name");
                NodeList osoiteList = doc.getElementsByTagName("address");
                NodeList postinumeroList = doc.getElementsByTagName("postalcode");
                NodeList toimipisteList = doc.getElementsByTagName("city");
                NodeList aukioloajatList = doc.getElementsByTagName("availability");
                NodeList latitudeList = doc.getElementsByTagName("lat");
                NodeList longitudeList = doc.getElementsByTagName("lng");
                
                for (int i = 0; i<nimiList.getLength();i++) {
                    Node n = nimiList.item(i);
                    Node o = osoiteList.item(i);
                    Node pn = postinumeroList.item(i);
                    Node tp = toimipisteList.item(i);
                    Node ao = aukioloajatList.item(i);
                    Node lat = latitudeList.item(i);
                    Node lon = longitudeList.item(i);
                    
                    if (n.getNodeType()==Node.ELEMENT_NODE) {
                        Element nimi = (Element) n;
                        Element osoite = (Element) o;
                        Element toimipiste = (Element) tp;
                        Element aukioloajat = (Element) ao;
                        Element postinumero = (Element) pn;
                        Element longitude = (Element) lon;
                        Element latitude = (Element) lat;
                        
                        psAutomaatti.setString(1, nimi.getTextContent());
                        psAutomaatti.setString(2, osoite.getTextContent());
                        psAutomaatti.setString(3, toimipiste.getTextContent());
                        psAutomaatti.setString(4, aukioloajat.getTextContent());
                        psAutomaatti.setString(5, postinumero.getTextContent());
                        psAutomaatti.setString(6, longitude.getTextContent());
                        psAutomaatti.setString(7, latitude.getTextContent());
                        
                        psAutomaatti.executeUpdate();
                        }
                }
            } catch (SAXException | IOException ex) {
                Logger.getLogger(timoteiDB.class.getName()).log(Level.SEVERE, null, ex);
            }
            } catch (ParserConfigurationException ex) {
            Logger.getLogger(timoteiDB.class.getName()).log(Level.SEVERE, null, ex);
            }
            } catch (SQLException ex) {
            Logger.getLogger(LuoPakettiController.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
}
