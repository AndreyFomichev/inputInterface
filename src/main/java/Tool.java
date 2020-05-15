import java.util.ArrayList;
import java.util.Arrays;

// POJO класс для заполнения таблицы
public class Tool {
    private ArrayList<String> types  = new ArrayList<>();
    private ArrayList<String> values = new ArrayList<>();

    public Tool(String[] values, String[] types){
        this.values.addAll(Arrays.asList(values));
        this.types.addAll(Arrays.asList(types));
    }

    public ArrayList<String> getTypes(){
        return types;
    }
    public ArrayList<String> getValues(){
        return values;
    }

    public void setValue(int index, String value) { values.set(index, value);  }

}