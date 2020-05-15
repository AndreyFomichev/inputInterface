import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class ColumnInfo {
    public enum VALUE_TYPE {
        NUMBER, DATE, DATE_TIME, STRING, RICH_TEXT
    }

    private String name;
    private VALUE_TYPE valueType;
    private int width;
    private Boolean wrapText;
    private boolean locked;
    private short align;
    private short valign;
    private String dataFormat;

    /**
     * Constructor ColumnInfo
     *
     * @param name
     * @param valueType
     * @param width
     * @param wrapText
     */
    public ColumnInfo(String name, VALUE_TYPE valueType, int width,
                      Boolean wrapText) {
        this.name = name;
        this.valueType = valueType;
        this.width = width;
        this.wrapText = wrapText;
    }

    /**
     * Constructor ColumnInfo
     *
     * @param name
     * @param valueType
     * @param width
     * @param wrapText
     * @param locked - блокировка для редактирвоания колонки
     */
    public ColumnInfo(String name, VALUE_TYPE valueType, int width, Boolean wrapText, Boolean locked) {
        this.name = name;
        this.valueType = valueType;
        this.width = width;
        this.wrapText = wrapText;
        this.locked = locked;
    }

    /**
     * Constructor ColumnInfo
     *
     * @param name
     * @param valueType
     * @param width
     * @param wrapText
     * @param horizontalAlign
     * @param verticalAlign
     */
    public ColumnInfo(String name, VALUE_TYPE valueType, int width,
                      Boolean wrapText, short horizontalAlign, short verticalAlign) {
        this.name = name;
        this.valueType = valueType;
        this.width = width;
        this.wrapText = wrapText;
        this.align = horizontalAlign;
        this.valign = verticalAlign;

    }

    public ColumnInfo(String name, VALUE_TYPE valueType, int width,
                      Boolean wrapText, short align, short valign, String dataFormat) {
        super();
        this.name = name;
        this.valueType = valueType;
        this.width = width;
        this.wrapText = wrapText;
        this.align = align;
        this.valign = valign;
        this.dataFormat = dataFormat;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public VALUE_TYPE getValueType() {
        return valueType;
    }

    public void setValueType(VALUE_TYPE valueType) {
        this.valueType = valueType;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public Boolean getWrapText() {
        return wrapText;
    }

    public void setWrapText(Boolean wrapText) {
        this.wrapText = wrapText;
    }

    public boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public short getAlign() {
        return align;
    }

    public void setAlign(short align) {
        this.align = align;
    }

    public short getValign() {
        return valign;
    }

    public void setValign(short valign) {
        this.valign = valign;
    }

    public String getDefaultDataFormat() {
        String result = "";
        switch (valueType) {
            case NUMBER:
                result = "@";
                break;
            case DATE:
                result = "dd.mm.yyyy";
                break;
            case DATE_TIME:
                result = "dd.m.yyyy h:mm";
                break;
            case STRING:
                result = "@";
                break;
            case RICH_TEXT:
                result = "General";
                break;
            default:
                result = "General";// valueType.toString();
                break;
        }
        return result;
    }

    public String formatValue(String value) {
        if (value != null) {
            switch (valueType) {
                case NUMBER:
                    return normalizeNumber(value, ',', ' ');
                case RICH_TEXT:
                    return value.replace("\r\n", "\n");
                default:
                    return value;
            }
        }
        return value;
    }

    public void setDataFormat(String dataFormat) {
        this.dataFormat = dataFormat;
    }

    public String getDataFormat() {
        if (dataFormat == null) {
            return getDefaultDataFormat();
        }
        return dataFormat;
    }

    public static String normalizeNumber(String value, char decimalSeparator,
                                         char groupSeparator) {
        value = value.trim();
        if (value == "")
            return value;
        try {
            double result = tryParseDouble(value, decimalSeparator,
                    groupSeparator);
            DecimalFormat df = new DecimalFormat(
                    "###0.################################");
            DecimalFormatSymbols newSymbols = DecimalFormatSymbols
                    .getInstance();
            newSymbols.setDecimalSeparator(decimalSeparator);
            newSymbols.setGroupingSeparator(groupSeparator);
            df.setDecimalFormatSymbols(newSymbols);
            return df.format(result);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // / <summary>
    // / Пытается привести текст в double, предварительно
    // / заменив точку и запятую на стандартный разделитель целой и дробной
    // частей,
    // / а также, удаляем пробелы.
    // / </summary>
    // / <param name="value">Значение, которое нужно перевести в double</param>
    // / <param name="result">Возвращаемое значение. При успехе
    // / равно результирующему значению</param>
    // / <returns>Флаг успеха операции: true - операция выполнена,
    // / false - в противном случае</returns>
    public static Double tryParseDouble(String value, char demicalSeparator,
                                        char groupSeparator) {
        // Приводим к стандартному виду
        value = value.replace(demicalSeparator, '.').replace(
                new String(new char[] { groupSeparator }), "");
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }


}
