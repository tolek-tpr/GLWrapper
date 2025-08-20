package pl.epsi.glWrapper.utils;

import java.io.InputStream;
import java.util.Objects;

public class Identifier {

    private final String namespace, path;

    public Identifier(String path) {
        this.namespace = "glw";
        this.path = path;
    }

    public Identifier(String namespace, String path) {
        this.namespace = namespace;
        this.path = path;
    }

    public InputStream asInputStream() {
        return Identifier.class.getClassLoader().getResourceAsStream(namespace + "/assets/" + path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.namespace, this.path);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;            // same reference
        if (o == null || getClass() != o.getClass()) return false; // different class
        Identifier myKey = (Identifier) o;
        // compare the two strings
        return Objects.equals(namespace, myKey.namespace) &&
                Objects.equals(path, myKey.path);
    }

}
