package net.sourceforge.jaad.mp4.boxes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.jaad.mp4.MP4Input;


public class BoxImpl implements Box {

    private final String name;
    protected long size, type, offset;
    protected Box parent;
    protected final List<Box> children;

    public BoxImpl(String name) {
        this.name = name;

        children = new ArrayList<>(4);
    }

    public void setParams(Box parent, long size, long type, long offset) {
        this.size = size;
        this.type = type;
        this.parent = parent;
        this.offset = offset;
    }

    protected long getLeft(MP4Input in) throws IOException {
        return (offset + size) - in.getOffset();
    }

    /**
     * Decodes the given input stream by reading this box and all of its
     * children (if any).
     *
     * @param in an input stream
     * @throws IOException if an error occurs while reading
     */
    public void decode(MP4Input in) throws IOException {
    }

    @Override
    public long getType() {
        return type;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public Box getParent() {
        return parent;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + " [" + BoxFactory.typeToString(type) + "]";
    }

    // container methods
    @Override
    public boolean hasChildren() {
        return !children.isEmpty();
    }

    @Override
    public boolean hasChild(long type) {
        boolean b = false;
        for (Box box : children) {
            if (box.getType() == type) {
                b = true;
                break;
            }
        }
        return b;
    }

    @Override
    public Box getChild(long type) {
        Box box = null, b = null;
        int i = 0;
        while (box == null && i < children.size()) {
            b = children.get(i);
            if (b.getType() == type) box = b;
            i++;
        }
        return box;
    }

    @Override
    public List<Box> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public List<Box> getChildren(long type) {
        List<Box> l = new ArrayList<>();
        for (Box box : children) {
            if (box.getType() == type)
                l.add(box);
        }
        return l;
    }

    protected Box parseBox(MP4Input in) throws IOException {
        return BoxFactory.parseBox(this, in);
    }

    protected void readChildren(MP4Input in) throws IOException {
        Box box;
        while (in.getOffset() < (offset + size)) {
            box = BoxFactory.parseBox(this, in);
            children.add(box);
        }
    }

    protected void readChildren(MP4Input in, int len) throws IOException {
        Box box;
        for (int i = 0; i < len; i++) {
            box = BoxFactory.parseBox(this, in);
            children.add(box);
        }
    }
}
