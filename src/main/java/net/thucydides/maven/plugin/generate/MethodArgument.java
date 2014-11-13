package net.thucydides.maven.plugin.generate;

public class MethodArgument {

    private Class<?> argumentClass;
    private String argumentType;
    private String argumentName;
    private String argumentDefaultValue;
    private String argumentGenericType;

    public String getArgumentGenericType() {
        if(argumentGenericType != null){
            return "<" + argumentGenericType + ">";
        }
        return "";
    }

    public void setArgumentGenericType(String argumentGenericType) {
        this.argumentGenericType = argumentGenericType;
    }

    public Class<?> getArgumentClass() {
        return argumentClass;
    }

    public void setArgumentClass(Class<?> argumentClass) {
        this.argumentClass = argumentClass;
    }

    public String getArgumentType() {
        return argumentType;
    }

    public void setArgumentType(String argumentType) {
        this.argumentType = argumentType;
    }

    public String getArgumentName() {
        return argumentName;
    }

    public void setArgumentName(String argumentName) {
        this.argumentName = argumentName;
    }

    public String getArgumentDefaultValue() {
        return argumentDefaultValue;
    }

    public void setArgumentDefaultValue(String argumentDefaultValue) {
        this.argumentDefaultValue = argumentDefaultValue;
    }
}
