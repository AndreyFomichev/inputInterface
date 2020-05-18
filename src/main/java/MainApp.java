import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.input.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import javax.swing.*;
import java.io.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;

public class MainApp extends Application {
    final int UNITS_LENGTH  = 1000;
    private BigDecimal[][] units = new BigDecimal[UNITS_LENGTH][UNITS_LENGTH];
    private int lastRowIndex = -1, lastColumnIndex = -1;
    private MouseEvent lastMouseEvent;
    private KeyEvent lastKeyEvent;
    private static ScrollEvent lastScrollEvent;
    Parent root;
    Scene mainScene;
    Stage dialog;
    Text dialogText;
    TextField dialogTextField;

    String[] header = new String[1000];

    FxController fxController;
    final String numbers = "0123456789";
    final String correctTypes ="shm%";
    private static ArrayList<Tool> tools = new ArrayList<>();
    private static String filePath;

    private static int maxColumnNum  = 0;
    public static ArrayList<Tool> getTools() {
        return tools;
    }

    public static void main(String[] args) {
        launch(args);
    }

    private String getType(String param) {
        if (param != null && param.endsWith("%") || param.endsWith("m")|| param.endsWith("h")|| param.endsWith("s"))
            return param.substring(param.length()-1);
        else return "";
    }

    private String getValue(String param) {
//        System.out.println("getvalue, param= [" + param + "]");
        if (param != null && param.endsWith("%") || param.endsWith("m")|| param.endsWith("h")|| param.endsWith("s"))
            return param.substring(0, param.length()-1);
        else return param;
    }

    private void loadFile(String fileName) throws IOException {
        FileReader input = null;
        String str;
        String[] x;
        String[] types = new String[100];
        String[] values = new String[100];
        boolean skip = true;
        filePath = "example.txt";
        try {
            input = new FileReader(filePath);
        } catch (IOException e) {
            try {
                filePath = "resources/example.txt";
                input = new FileReader(filePath);
            } catch (IOException e2) {
                String errMsg = "ОШИБКА: Файл example.txt отсутствует!";
                System.err.println(errMsg);
                JOptionPane.showMessageDialog(null, errMsg);
            }
        }
        BufferedReader bufRead = new BufferedReader(input);
        int headerIndex = 0;
        while ( (str = bufRead.readLine()) != null)
        {
            // очистим массивы после предыдущей загрузки
            for (int i = 0; i <= types.length - 1; i++) {
                types[i] = null;
                values[i] = null;
            }

            str = str.replace("\t"," ").trim();
            if (skip) {

                header[headerIndex++] = str==null ?" ":str; //сохраняем header для будущей записи в файл
                if (str.equals("Tools:")) {
                    skip = false;
                    continue;
                }
            } else if (!skip) {
                while(str.contains("  "))
                   str = str.replace("  ", " ");

                // System.out.println("str=" + str +", x.length="+ x.length);
                x = str.split(" ");
                maxColumnNum = x.length > maxColumnNum ? x.length : maxColumnNum;

                if (str.startsWith(">")) {
                    types[0] = x[0].substring(1); // это горячая клавиша
                    str = str.substring(str.indexOf(" ")+1); // удаляем ее из строки

                }
                x = str.split(" ");

                values[0] = x[0]; // а это имя инструмента

                for (int i = 1; i <= x.length-1; i++) {
             //       System.out.println("x.length=" + x.length + ", i=" + i);

                    if (x[i] != null) {
                        values[i] = getValue(x[i]);
                        types[i] = getType(x[i]);
                    }
                }

            tools.add( new Tool(values, types ) );

            }
        }

        System.out.println("Tools size = " + tools.size());
    }

    public BigDecimal getSelectedValue(int rowIndex, int colIndex) {
        String res = null;
        // Get the row index where your value is stored

        if (rowIndex >= 0) {
            Tool rowList =  fxController.getTbResult().getItems().get(rowIndex);
            res = rowList.getValues().get(colIndex);
        }
        return (res == null ? null : new BigDecimal(res));
    }

    public void setSelectedValue(int rowIndex, int colIndex,  BigDecimal value) {
        if (rowIndex >= 0 && colIndex > 0) {
            Tool rowList =  fxController.getTbResult().getItems().get(rowIndex);
            rowList. getValues().set(colIndex, ColumnInfo.normalizeNumber(String.valueOf(value), '.', ' '));
        }
    }

    public void setSelectedType(int rowIndex, int colIndex, String type) {
        if (rowIndex >= 0 && colIndex > 0) {
            Tool rowList =  fxController.getTbResult().getItems().get(rowIndex);
            rowList. getTypes().set(colIndex, type);
        }
    }
    public BigDecimal getUnit(BigDecimal x, Integer row, Integer column) {
        BigDecimal z = null;
        if (row >= 0 && row < UNITS_LENGTH && column >= 0 && column < UNITS_LENGTH)
          z = units[row][column];
        if (z==null && x != null) {
            MathContext mc = new MathContext(20, RoundingMode.DOWN);
            z = BigDecimal.TEN.pow(-x.scale(), mc);
            units[row][column] = z;
        }
        if (lastMouseEvent != null && z != null) {
            if (lastMouseEvent.isControlDown() && !lastMouseEvent.isShiftDown())
                z = z.multiply(BigDecimal.valueOf(10));
            else if (!lastMouseEvent.isControlDown() && lastMouseEvent.isShiftDown())
                z = z.multiply(BigDecimal.valueOf(100));
            else if (lastMouseEvent.isControlDown() && lastMouseEvent.isShiftDown())
                z = z.multiply(BigDecimal.valueOf(1000));

        } else if (lastKeyEvent != null && z != null) {
            if  (lastKeyEvent.isControlDown() && !lastKeyEvent.isShiftDown())
                z = z.multiply(BigDecimal.valueOf(10));
            else if (!lastKeyEvent.isControlDown() && lastKeyEvent.isShiftDown())
                z = z.multiply(BigDecimal.valueOf(100));
            else if (lastKeyEvent.isControlDown() && lastKeyEvent.isShiftDown())
                z = z.multiply(BigDecimal.valueOf(1000));

        } else if (lastScrollEvent != null && z != null) {
            if  (lastScrollEvent.isControlDown() && !lastScrollEvent.isShiftDown())
                z = z.multiply(BigDecimal.valueOf(10));
            else if (!lastScrollEvent.isControlDown() && lastScrollEvent.isShiftDown())
                z = z.multiply(BigDecimal.valueOf(100));
            else if (lastScrollEvent.isControlDown() && lastScrollEvent.isShiftDown())
                z = z.multiply(BigDecimal.valueOf(1000));
        }

        return z;
    }

    public void increase(int rowIndex, int colIndex )  {
        if (colIndex <= 0) return; // первую колонку с названиями инструментов не изменяем

        BigDecimal d = getSelectedValue(rowIndex, colIndex);
        BigDecimal u = getUnit(d, rowIndex, colIndex);
        if (d != null && u != null) {
            d = d.add(u);
            setSelectedValue(rowIndex, colIndex, d);
            System.out.println("increase " + d + " by  " + u + ", scale=" + d.scale());
            fxController.getTbResult().refresh();
            saveFile();
        } else
            System.out.println("d=" + d + ", u=" + u);
    }

    public void decrease(int rowIndex, int colIndex) {
        if (colIndex <= 0) return; // первую колонку с названиями инструментов не изменяем

        BigDecimal d = getSelectedValue(rowIndex, colIndex);
        BigDecimal u = getUnit(d, rowIndex, colIndex);
        if (d != null && u != null) {
            d = d.subtract( u);
            setSelectedValue(rowIndex, colIndex, d );
            System.out.println("decrease " + d + " by  " + u + ", scale=" + d.scale());
            fxController.getTbResult().refresh();
            saveFile();
        } else
            System.out.println("d=" + d + ", u=" + u);
    }

    private void saveFile() {
        StringBuilder str = new StringBuilder();

        try(FileOutputStream l = new FileOutputStream(filePath);
            BufferedWriter r = new BufferedWriter(new OutputStreamWriter(l));
           ){
            for (String s : header) {
                if (s != null) {
                    r.write(s);
                    r.newLine();
                }
            }

            for (Tool t : fxController.getTbResult().getItems()) {
                str.setLength(0);
                if(t.getTypes().get(0) != null)
                    str.append(">").append(t.getTypes().get(0) );

                str.append(" ").append(t.getValues().get(0) ).append(" ");

                for (int i = 1; i < maxColumnNum; i++) {
                    if (t.getValues().get(i) != null)
                        str.append(t.getValues().get(i));
                    if (t.getTypes().get(i) != null)
                        str.append(t.getTypes().get(i));
                    str.append(" ");
                }
                r.write(str.toString());
                r.newLine();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void start(Stage stage) throws IOException {

        loadFile("/example.txt");
        Props props = new Props();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/mainForm.fxml"));
        fxController = new FxController( stage );
        fxmlLoader.setController(fxController);
        root = fxmlLoader.load();
        stage.setTitle("Cписок средств и их параметров");
        mainScene = new Scene(root, 800, 100, null);

        mainScene.addEventFilter(MouseEvent.MOUSE_PRESSED, this::mousePressHandle);
        mainScene.addEventFilter(MouseEvent.MOUSE_RELEASED, this::mouseReleaseHandle);
        mainScene.addEventFilter(KeyEvent.KEY_PRESSED, this::keyPressHandle);
        fxController.getTbResult().addEventFilter(ScrollEvent.SCROLL, this::onScrollHandle);

        stage.setScene(mainScene);
        props.load_props(stage, fxController.getTbResult());

        stage.setOnHiding(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent event) {
                props.save_props(stage, fxController.getTbResult());

            }
        });
        stage.show();

    }

    public void onScrollHandle (ScrollEvent event) {
        System.out.println("Mouse scroll detected: deltax=" + event.getDeltaX() + ", deltaY=" + event.getDeltaY()+ ", isShiftDown=" + event.isShiftDown());
        TablePosition tp = (TablePosition) fxController.getTbResult().getFocusModel().getFocusedCell();
        int colIndex = tp.getColumn();
        if (colIndex <= 0) return; // первую колонку с названиями инструментов не изменяем
        int rowIndex = tp.getRow();
        if (!event.isAltDown()) {
            if (event.getDeltaY() > 0 || (event.isShiftDown() && event.getDeltaX() > 0)) {
                lastScrollEvent = event;
                increase(rowIndex, colIndex);
                event.consume();
            } else if (event.getDeltaY() < 0 || (event.isShiftDown() && event.getDeltaX() < 0)) {
                lastScrollEvent = event;
                decrease(rowIndex, colIndex);
                event.consume();
            }
        } else if (event.getDeltaY() < 0) {
            fxController.getTbResult().getSelectionModel().selectBelowCell();
            fxController.getTbResult().scrollTo(fxController.getTbResult().getFocusModel().getFocusedCell().getRow()-1);
            System.out.println("select below cell");

            event.consume();
        } else if (event.getDeltaY() > 0) {
            fxController.getTbResult().getSelectionModel().selectAboveCell();
            fxController.getTbResult().scrollTo(fxController.getTbResult().getFocusModel().getFocusedCell().getRow()-1);
            System.out.println("select above cell");
            event.consume();
        }

        lastScrollEvent = null;
        fxController.getTbResult().refresh();
    }

    public void dialogKeyPressHandle(KeyEvent ke) {
        KeyCode code = ke.getCode();
        BigDecimal bd;
        if  (code == KeyCode.ESCAPE) {
            System.out.println("ESCAPE pressed in dialog, cancel editing");
            dialog.close();
            fxController.getTbResult().refresh();
            ke.consume();
        } else if  (code == KeyCode.ENTER) {
            System.out.println("ENTER pressed in dialog");
            String newValue = dialogTextField.getCharacters().toString();
            String newType = dialogText.getText();
            if (newType.length() == 0 && !numbers.contains(newValue.substring(newValue.length()-1))) {
                newType = newValue.substring(newValue.length()-1);
                newValue = newValue.substring(0, newValue.length()-1);
            }
            if (!correctTypes.contains(newType)) {
                String errMsg = "ОШИБКА: некорректный тип " + newType;
                System.err.println(errMsg);
                JOptionPane.showMessageDialog(null, errMsg);
                ke.consume();
                return;
            }
            try {
                bd = new BigDecimal(newValue);
                setSelectedType(lastRowIndex, lastColumnIndex, newType);
                setSelectedValue(lastRowIndex, lastColumnIndex, bd);
                dialog.close();
                fxController.getTbResult().refresh();
                saveFile();
                System.out.println("new value saved: " + newValue + newType);
                ke.consume();
            } catch( Exception e) {
                String errMsg = "ОШИБКА: неверное число " + newValue + " [" + e.getMessage() + "]";
                System.err.println(errMsg);
                JOptionPane.showMessageDialog(null, errMsg);
                ke.consume();
                return;
            }
        }
    }

    public void keyPressHandle(KeyEvent ke) {
        KeyCode code = ke.getCode();
        String name = code.getName();
        TablePosition tp = (TablePosition) fxController.getTbResult().getFocusModel().getFocusedCell();
        int colIndex = tp.getColumn();
        if (colIndex <= 0) return; // первую колонку с названиями инструментов не изменяем
        int rowIndex = tp.getRow();

    //    System.out.println(name + " key press processing");
        if (code == KeyCode.ENTER) {
            lastColumnIndex = colIndex;
            lastRowIndex = rowIndex;
            System.out.println("ENTER key pressed, colIndex=" + colIndex + ", rowIndex=" + rowIndex);
            dialog = new Stage();
            dialog.initStyle(StageStyle.UTILITY);
            String oldValue = fxController.getTbResult().getItems().get(rowIndex).getValues().get(colIndex);
            String oldType = fxController.getTbResult().getItems().get(rowIndex).getTypes().get(colIndex);
            String textStr = "";
            if (oldType == null) oldType = "";
            if (oldType.equals("%"))
                textStr = "%";
             else
                oldValue = oldValue + oldType;

            dialogText = new Text(160, 15, textStr);
            dialogTextField =  new TextField(oldValue );

            Group gr = new Group( dialogTextField, dialogText);

            Scene scene = new Scene(gr);
            dialog.setScene(scene);
            dialog.setTitle("Введите значение");
            scene.addEventFilter(KeyEvent.KEY_PRESSED, this::dialogKeyPressHandle);

            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.showAndWait();
            ke.consume();

        } else  if (!ke.isAltDown() && code == KeyCode.UP) {
            System.out.println("UP key pressed");
            lastKeyEvent = ke;
            increase(rowIndex,  colIndex);
            ke.consume();
            lastKeyEvent = null;
        } else if (!ke.isAltDown() && ke.getCode() == KeyCode.DOWN) {
            System.out.println("DOWN key pressed");
            lastKeyEvent = ke;
            decrease(rowIndex, colIndex);
            ke.consume();
            lastKeyEvent = null;
        } else if (numbers.indexOf(name) >- 1) {
            // number key pressed
            System.out.println(name+ " number key pressed.");
            if (name.equals("0")) name = "10";
            TableColumn<Tool, String> c =
                (TableColumn<Tool, String>) fxController.getTbResult().getColumns().get(Integer.valueOf(name));

            fxController.getTbResult().getSelectionModel().select( tp.getRow(), c);
            ke.consume();
            lastKeyEvent = null;
        } else if (code != KeyCode.LEFT && code != KeyCode.UP && code != KeyCode.RIGHT && code != KeyCode.DOWN ) {
            System.out.println(name + " key pressed.");
            int i = 0;
            for (Tool t : fxController.getTbResult().getItems()) {
                if (t != null) {
                    for (String s : t.getTypes())
                        if (s != null)
                            if (name.toLowerCase().equals(s.toLowerCase())) {
                                fxController.getTbResult().getSelectionModel().select(i, tp.getTableColumn());
                                fxController.getTbResult().scrollTo(i);
                            }
                }
                i++;
            }
            ke.consume();

        }

        if (!ke.isConsumed() && ke.isAltDown()) {
            if (code == KeyCode.UP) {
                fxController.getTbResult().getSelectionModel().selectAboveCell();
                fxController.getTbResult().scrollTo(fxController.getTbResult().getFocusModel().getFocusedCell().getRow()-1);
                ke.consume();

            } else if (code == KeyCode.DOWN) {
                fxController.getTbResult().getSelectionModel().selectBelowCell();
                fxController.getTbResult().scrollTo(fxController.getTbResult().getFocusModel().getFocusedCell().getRow()-1);
                ke.consume();

            } else if (code == KeyCode.LEFT) {
                fxController.getTbResult().getSelectionModel().selectLeftCell();
                fxController.getTbResult().scrollTo(fxController.getTbResult().getFocusModel().getFocusedCell().getRow()-1);
                ke.consume();

            } else if (code == KeyCode.RIGHT) {
                fxController.getTbResult().getSelectionModel().selectRightCell();
                fxController.getTbResult().scrollTo(fxController.getTbResult().getFocusModel().getFocusedCell().getRow()-1);
                ke.consume();
            }
        }
        lastKeyEvent = null;
        fxController.getTbResult().refresh();
    }

    public void mousePressHandle(MouseEvent e) {
        if (e.isPrimaryButtonDown()) {
            System.out.println("left mouse press detected, x="+ e.getX() + ", y=" + e.getY());
        }

        lastMouseEvent = e;
    }

    public void mouseReleaseHandle(MouseEvent e) {
        if (lastMouseEvent == null) return;
        TablePosition tp = (TablePosition) fxController.getTbResult().getFocusModel().getFocusedCell();
        int colIndex = tp.getColumn();
        if (colIndex <= 0) return; // первую колонку с названиями инструментов не изменяем
        int rowIndex = tp.getRow();

        if (rowIndex != lastRowIndex || colIndex != lastColumnIndex) {
            //при первом нажатии на неактивную ячейку мышкой, чтобы значение не менялось.
            // Смысл в том, чтобы именно на активной ячейке менялись значения а не на только что выбранной,
            // чтобы случайным нажатием кнопки мыши не менять цифру.
            System.out.println("Первый выбор активной ячейки - значение не изменяем");

            lastRowIndex = rowIndex;
            lastColumnIndex = colIndex;
            lastMouseEvent = null;
            fxController.getTbResult().refresh();
            e.consume();
            return;
        }
        lastRowIndex = rowIndex;
        lastColumnIndex = colIndex;

        TableHeaderRow tbHeader = (TableHeaderRow) fxController.getTbResult().lookup("TableHeaderRow");

        if (e.getY() <= tbHeader.getHeight()) {
            System.out.println("return because e.getY=" + e.getY() +" <= tbHeader.Height=" + tbHeader.getHeight());
            fxController.getTbResult().refresh();
            return;
        }

        if (lastMouseEvent.isPrimaryButtonDown()) {
            System.out.println("left mouse Release detected! ");
            increase(rowIndex, colIndex);
            e.consume();
        } else if  (lastMouseEvent.isSecondaryButtonDown() || lastMouseEvent.isMiddleButtonDown()) {
            System.out.println("middle or right mouse Release detected! " + e.getSource());
            decrease(rowIndex, colIndex);
            e.consume();
        }
        lastMouseEvent = null;
        fxController.getTbResult().refresh();
    }

    public static int getMaxColumnNum() {
        return maxColumnNum;
    }
}
