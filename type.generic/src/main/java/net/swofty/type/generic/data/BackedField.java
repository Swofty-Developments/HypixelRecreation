package net.swofty.type.generic.data;

public interface BackedField {
    String readData(DataHandler handler);

    void writeData(DataHandler handler, String serialized);
}
