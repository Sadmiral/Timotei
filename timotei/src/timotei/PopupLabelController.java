package timotei;


import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class PopupLabelController implements Initializable {

    @FXML
    private Button okButton;
    @FXML
    private Label teksti;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }    
    
    public void setText(String teksti) {
        this.teksti.setText(teksti);
    }
    @FXML
    private void handleOK(ActionEvent event) {
        Stage stage = (Stage) okButton.getScene().getWindow();
        stage.close();
    }
    
}
