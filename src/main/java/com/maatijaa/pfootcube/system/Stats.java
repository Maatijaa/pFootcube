
package com.maatijaa.pfootcube.system;

import java.io.ObjectInputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.Set;
import java.io.IOException;
import java.io.File;
import java.util.HashMap;

public class Stats
{
    private HashMap<String, Integer> values;
    private String path;
    private File file;
    
    public Stats() {
        this.values = new HashMap<String, Integer>();
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
    
    public int get(final String key) {
        return this.values.get(key);
    }
    
    public boolean has(final String key) {
        return this.values.containsKey(key);
    }
    
    public void put(final String key, final int value) {
        this.values.put(key, value);
        this.save();
    }
    
    public void rise(final String key) {
        this.values.put(key, this.values.get(key) + 1);
        this.save();
    }
    
    public Set<String> keySet() {
        return this.values.keySet();
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
            final HashMap<String, Integer> scoreMap = (HashMap<String, Integer>)ois.readObject();
            this.values = scoreMap;
            ois.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
