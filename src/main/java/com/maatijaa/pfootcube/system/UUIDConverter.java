
package com.maatijaa.pfootcube.system;

import java.io.ObjectInputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.util.HashMap;

public class UUIDConverter
{
    private HashMap<String, String> values;
    private String path;
    private File file;
    
    public UUIDConverter() {
        this.values = new HashMap<String, String>();
    }
    
    public void setup(final String s) {
        this.path = s;
        this.file = new File(this.path);
        if (!this.file.getAbsoluteFile().exists()) {
            try {
                this.file.createNewFile();
                this.save();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public String get(final String key) {
        return this.values.get(key);
    }
    
    public String getKey(final String value) {
        for (final String s : this.values.keySet()) {
            if (this.values.get(s).equals(value)) {
                return s;
            }
        }
        return null;
    }
    
    public boolean has(final String key) {
        return this.values.containsKey(key);
    }
    
    public boolean hasValue(final String value) {
        return this.values.containsValue(value);
    }
    
    public void put(final String key, final String value) {
        this.values.put(key, value);
        this.save();
    }
    
    public void save() {
        try {
            final ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(this.path)));
            oos.writeObject(this.values);
            oos.flush();
            oos.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void load() {
        try {
            final ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(this.path)));
            this.values.clear();
            final HashMap<String, String> scoreMap = (HashMap<String, String>)ois.readObject();
            this.values = scoreMap;
            ois.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
