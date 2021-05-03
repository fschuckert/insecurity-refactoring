/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base;

import java.util.Objects;

/**
 *
 * @author blubbomat
 */
public class DataType {

    private final String type;
    
    private DataType arraySubType;

    private DataType(String type) {
        this.type = type;
    }
    
    
    public static DataType String(){
        return new DataType("String");
    }
    
     public static DataType Integer(){
        return new DataType("Integer");
    }
     
    public static DataType Float(){
        return new DataType("Float");
    }
    
    public static DataType Boolean(){
        return new DataType("Boolean");
    }
    
    public static DataType Array(){
        return new DataType("Array");
    }
    
    public static DataType Any(){
        return new DataType("Any");
    }
    
    public static DataType Unknown(){
        return new DataType("Unknown");
    }
    
    public static DataType valueOf(String value){
        DataType instance = new DataType(value);
        return instance;        
    }
    
    

    public DataType getArraySubType() {
        return arraySubType;
    }

    public DataType setArraySubType(DataType arraySubType) {
        this.arraySubType = arraySubType;
        return this;
    }
    
    

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DataType other = (DataType) obj;
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        if (!Objects.equals(this.arraySubType, other.arraySubType)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return type + ( arraySubType != null ? "(" + arraySubType + ")" : "" );
    }

    public boolean isArray() {
        return "Array".equals(type);
    }

    /**
     * checks if this is equals to the other. If this is any it will always return true!
     * @param other the object to check against
     * @return 
     */
    public boolean equalsAny(DataType other) {        
        if(this.equals(Any())){
            return true;
        }
        
        return this.equals(other);
    }
    
    
    
}
