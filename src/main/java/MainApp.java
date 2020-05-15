import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.input.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.swing.*;
import java.io.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Properties;

public class MainApp extends Application {
    final int UNITS_LENGTH  = 1000;
    private BigDecimal[][] units = new BigDecimal[UNITS_LENGTH][UNITS_LENGTH];

    private MouseEvent lastMouseEvent;
    private KeyEvent lastKeyEvent;
    private static ScrollEvent lastScrollEvent;

    String[] header = new String[1000];

    FxController fxController;
    final String numbers = "0123456789";
    private static  ArrayList<Tool> tools = new ArrayList<>();
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
            for (int i = 0; i<=types.length-1; i++) {
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

    public void setSelectedValue(int rowIndex, int colIndex, BigDecimal value) {
        if (rowIndex >= 0 && colIndex >= 0) {
            Tool rowList =  fxController.getTbResult().getItems().get(rowIndex);
            rowList. getValues().set(colIndex, ColumnInfo.normalizeNumber(String.valueOf(value), '.', ' '));
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

    public void increase()  {
        TablePosition tp = (TablePosition) fxController.getTbResult().getFocusModel().getFocusedCell();
        int rowIndex = tp.getRow();
        int colIndex = tp.getColumn();

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

    public void decrease() {
        TablePosition tp = fxController.getTbResult().getFocusModel().getFocusedCell();
        int rowIndex = tp.getRow();
        int colIndex = tp.getColumn();

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

        try{
            FileOutputStream l = new FileOutputStream(filePath);

            BufferedWriter r = new BufferedWriter(new OutputStreamWriter(l));
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
            r.close();
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
        Parent root = fxmlLoader.load();
        stage.setTitle("Cписок средств и их параметров");
        Scene scene = new Scene(root, 800, 100, null);

        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, this::MousePressHandle);
        scene.addEventFilter(MouseEvent.MOUSE_RELEASED, this::MouseReleaseHandle);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::KeyPressHandle);
        stage.setScene(scene);
        props.load_props(stage, fxController.getTbResult());

        stage.setOnHiding(new EventHandler<WindowEvent>() {

            public void handle(WindowEvent event) {
                props.save_props(stage, fxController.getTbResult());

            }
        });

        stage.show();
        //Creating the mouse event handler

        EventHandler<ScrollEvent> eventHandler = new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent e) { onScrollHandle(e); }
        };
        fxController.getTbResult().addEventFilter(ScrollEvent.ANY, eventHandler);
    }

    public void onScrollHandle (ScrollEvent event) {
        System.out.println("Mouse scroll detected: deltax=" + event.getDeltaX() + ", deltaY=" + event.getDeltaY()+ ", isShiftDown=" + event.isShiftDown());

        if (!event.isAltDown()) {
            if (event.getDeltaY() > 0 || (event.isShiftDown() && event.getDeltaX() > 0)) {
                lastScrollEvent = event;
                increase();
                event.consume();
            } else if (event.getDeltaY() < 0 || (event.isShiftDown() && event.getDeltaX() < 0)) {
                lastScrollEvent = event;
                decrease();
                event.consume();
            }
        } else if (event.getDeltaY() < 0) {
            fxController.getTbResult().getSelectionModel().selectBelowCell();
            fxController.getTbResult().scrollTo(fxController.getTbResult().getFocusModel().getFocusedCell().getRow());
            System.out.println("select below cell");

            event.consume();
        } else if (event.getDeltaY() > 0) {
            fxController.getTbResult().getSelectionModel().selectAboveCell();
            fxController.getTbResult().scrollTo(fxController.getTbResult().getFocusModel().getFocusedCell().getRow());
            System.out.println("select above cell");
            event.consume();
        }

        lastScrollEvent = null;
        fxController.getTbResult().refresh();
    }

    public void KeyPressHandle(KeyEvent ke) {
        KeyCode code = ke.getCode();
        String name = code.getName();
    //    System.out.println(name + " key press processing");

        if (!ke.isAltDown() && code == KeyCode.UP) {
            System.out.println("UP key pressed");
            lastKeyEvent = ke;
            increase();
            ke.consume();
            lastKeyEvent = null;
        } else if (!ke.isAltDown() && ke.getCode() == KeyCode.DOWN) {
            System.out.println("DOWN key pressed");
            lastKeyEvent = ke;
            decrease();
            ke.consume();
            lastKeyEvent = null;
        } else if (numbers.indexOf(name) >- 1) {
            // number key pressed
            System.out.println(name+ " number key pressed.");
            TablePosition tp = fxController.getTbResult().getFocusModel().getFocusedCell();
            if (name.equals("0")) name = "10";
            TableColumn<Tool, String> c =
                (TableColumn<Tool, String>) fxController.getTbResult().getColumns().get(Integer.valueOf(name));

            fxController.getTbResult().getSelectionModel().select( tp.getRow(), c);
            ke.consume();
            lastKeyEvent = null;
        } else if (code != KeyCode.LEFT && code != KeyCode.UP && code != KeyCode.RIGHT && code != KeyCode.DOWN ) {
            System.out.println(name + " key pressed.");
            TablePosition tp = fxController.getTbResult().getFocusModel().getFocusedCell();
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

        if (!ke.isConsumed()&& ke.isAltDown()) {
            if (code == KeyCode.UP) {
                fxController.getTbResult().getSelectionModel().selectAboveCell();
                ke.consume();
            } else if (code == KeyCode.DOWN) {
                fxController.getTbResult().getSelectionModel().selectBelowCell();
                ke.consume();
            } else if (code == KeyCode.LEFT) {
                fxController.getTbResult().getSelectionModel().selectLeftCell();
                ke.consume();
            } else if (code == KeyCode.RIGHT) {
                fxController.getTbResult().getSelectionModel().selectRightCell();
                ke.consume();
            }
        }
        lastKeyEvent = null;
        fxController.getTbResult().refresh();
    }

    public void MousePressHandle(MouseEvent e) {
        lastMouseEvent = e;
    }

    public void MouseReleaseHandle(MouseEvent e) {
        if (lastMouseEvent == null) return;

        if (lastMouseEvent.isPrimaryButtonDown()) {
            System.out.println("left mouse click detected! ");
            increase();
            e.consume();
        } else if  (lastMouseEvent.isSecondaryButtonDown() || lastMouseEvent.isMiddleButtonDown()) {
            System.out.println("middle or right mouse click detected! " + e.getSource());
            decrease();
            e.consume();
        }
        lastMouseEvent = null;
        fxController.getTbResult().refresh();
    }


    public static int getMaxColumnNum() {
        return maxColumnNum;
    }
}
