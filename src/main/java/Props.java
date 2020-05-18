import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import javafx.scene.control.TableView;

public class Props {
    final String WIDTH = "window.width";
    final String HEIGHT = "window.height";
    final String X = "window.x";
    final String Y = "window.y";
    final String COLUMN = "column";
    final String FILE_NAME = "config.properties";

    private static Properties properties = new Properties();

    public void load_props(Stage stage, TableView tbResult) {
        try (  FileInputStream     fis = new FileInputStream(FILE_NAME) ){
            properties.loadFromXML(fis);
        } catch (IOException e) {
            try (FileInputStream fis2 = new FileInputStream("resources/" + FILE_NAME);){
                properties.load(fis2);
            } catch (IOException e2) {
                System.out.println("Файл свойств отсутствует!");
            }
        }
        if (properties != null) {
            try {
                stage.setX(Double.parseDouble(properties.getProperty(X)));
                stage.setY(Double.parseDouble(properties.getProperty(Y)));

                stage.setWidth(Double.parseDouble(properties.getProperty(WIDTH)));
                stage.setHeight(Double.parseDouble(properties.getProperty(HEIGHT)));

                for (int i = 0; i < tbResult.getColumns().size(); i++) {
                    TableColumn col  = (TableColumn) tbResult.getColumns().get(i);
                    Double width = Double.parseDouble(properties.getProperty(COLUMN + String.valueOf(i)));
                    col.setPrefWidth(width);
                }

            } catch (Exception e) {
                System.out.println("Error parsing properties " + e.getMessage());

            }
        }

    }

    public void save_props(Stage stage, TableView tbResult) {
        FileOutputStream fis;
        TableColumn col;
        properties.setProperty(X, String.valueOf(stage.getX()));
        properties.setProperty(Y, String.valueOf(stage.getY()));
        properties.setProperty(HEIGHT, String.valueOf(stage.getHeight()));
        properties.setProperty(WIDTH, String.valueOf(stage.getWidth()));

        for (int i=0; i< tbResult.getColumns().size(); i++) {
            col = (TableColumn) tbResult.getColumns().get(i);
            properties.setProperty(COLUMN + String.valueOf(i),  Double.toString( col.getWidth()));
        }
        try {
            fis = new FileOutputStream("config.properties");
            properties.storeToXML(fis, null);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("properties are stored");
    }

}
