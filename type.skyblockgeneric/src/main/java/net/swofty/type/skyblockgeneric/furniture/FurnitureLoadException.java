package net.swofty.type.skyblockgeneric.furniture;

public class FurnitureLoadException extends RuntimeException {
    public FurnitureLoadException(String message) {
        super(message);
    }

    public FurnitureLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
