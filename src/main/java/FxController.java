import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class FxController{

    private Stage stage;
    @FXML
    public GridPane mainPane;

    public TableView<Tool> getTbResult() { return tbResult; }

    @FXML
    private TableView<Tool> tbResult;

    private ObservableList<Tool> tools = FXCollections.observableArrayList();

    public FxController(Stage stage) {
        this.stage = stage;
    }

    private String formatParam(int i, String v, String t) {
        String res = (v == null ? "" : v);
        if (t != null && !t.equals(""))
            res = res + (i > 0 ? t: " (" + t + ")");
        return res; // (v == null ? "" : ColumnInfo.normalizeNumber(v.toString(),'.',' ')) ;
    }

    @FXML
    public void initialize() throws Exception {

        tools.addAll(MainApp.getTools());
        //Fill the table

        for(int i = 0; i < MainApp.getMaxColumnNum() - 1; i++){
            final int indexColumn = i;
            TableColumn<Tool, String> tableColumn = new TableColumn<>( (i==0 ? "Parameter" : "" + i));
            tableColumn.setCellValueFactory(
                param -> new SimpleStringProperty(
                                 formatParam(indexColumn,
                                     param.getValue().getValues().get(indexColumn),
                                     param.getValue().getTypes().get(indexColumn)
                                 )
                         )
            );

            tableColumn.setCellFactory(TextFieldTableCell.<Tool> forTableColumn());

            tbResult.getColumns().add(tableColumn);
        }
        tbResult.setItems(tools);
        tbResult.getSelectionModel().setCellSelectionEnabled(true);
     //   tbResult.getSelectionModel().getSelectedCells().addListener(this::selectCells);
        tbResult.prefHeightProperty().bind(stage.heightProperty());
        tbResult.prefWidthProperty().bind(stage.widthProperty());
   }

 //   private void selectCells(ListChangeListener.Change<? extends TablePosition> c) {
   //     c.getList().forEach(System.out::println);
   // }
}




