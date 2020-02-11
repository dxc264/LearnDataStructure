public class BTree<K extends Comparable<K>, V> {
    private static final int BRANCHING_FACTOR = 3;
    private static final int MAX_KEYS_COUNT = 2 * BRANCHING_FACTOR - 1;
    private static final int MIN_KEYS_COUNT = BRANCHING_FACTOR - 1;
    private static final int START_SPLIT_COUNT = MAX_KEYS_COUNT + 1;
    private Node root;
    private int height;

    private static final class Node {
        private int numberOfKeys;
        private Entry[] keyList = new Entry[START_SPLIT_COUNT];
        private Node(int numberOfKeys) {
            this.numberOfKeys = numberOfKeys;
        }
    }

    private static final class Entry<V> {
        private Comparable key;
        private V val;
        private Node next;
        private Node prev;
        private Entry(Comparable key, V val) {
            this.key = key;
            this.val = val;
        }
    }

    public BTree() {
        root = new Node(0);
        height = 1;
    }

    public void put(K key, V val) {
        if (key == null) throw new IllegalArgumentException("key is null");
        Node newNode = insert(root, key, val, height);
        if (newNode == null) return;
        Node newRootNode = new Node(1);
        newRootNode.keyList[0] = root.keyList[START_SPLIT_COUNT/2];
        root.numberOfKeys = START_SPLIT_COUNT/2;
        newRootNode.keyList[0].prev = root;
        newRootNode.keyList[0].next = newNode;
        root = newRootNode;
        height++;
    }

    public int height() {
        return height;
    }

    public V get(K key) {
        if (key == null) throw new IllegalArgumentException("key is null");
        return search(root, key, height);
    }

    private V search(Node x, K key, int height) {
        if (height == 1) {
            for (int j = 0; j < x.numberOfKeys; j++) {
                if (eq(key, x.keyList[j].key)) {
                    return (V)x.keyList[j].val;
                }
            }
        } else {
            Node childNode = null;
            for (int j = 0; j < x.numberOfKeys;j++) {
                if (less(key, x.keyList[j].key)) {
                    childNode = x.keyList[j].prev;
                    break;
                }
                if ((j + 1 == x.numberOfKeys) && gt(key, x.keyList[j].key)) {
                    childNode = x.keyList[j].next;
                }
            }
            if (childNode != null) {
                return search(childNode, key, height - 1);
            }
        }
        return null;
    }

    private Node insert(Node h, K key, V value, int height) {
        Entry t = new Entry(key, value);
        int j;
        if (height == 1) {
            for (j = 0; j < h.numberOfKeys; j++) {
                if (less(key, h.keyList[j].key)) {
                    break;
                }
            }
        } else {
            Node childNode = null;
            for (j = 0; j < h.numberOfKeys; j++) {
                if (less(key, h.keyList[j].key)) {
                    childNode = h.keyList[j].prev;
                    break;
                }
                if (j + 1 == h.numberOfKeys) {
                    childNode = h.keyList[j].next;
                }
            }
            if (childNode != null) {
                Node newNode = insert(childNode, key, value, height - 1);
                if (newNode == null) return null;
                t = new Entry(childNode.keyList[START_SPLIT_COUNT/2].key, childNode.keyList[START_SPLIT_COUNT/2].val);
                t.next = newNode;
                t.prev = childNode;
            }
        }
        for (int i = h.numberOfKeys; i > j; i--) {
            h.keyList[i] = h.keyList[i-1];
        }
        h.keyList[j] = t;
        h.numberOfKeys++;
        if (h.numberOfKeys <= MAX_KEYS_COUNT) return null;
        else return split(h);
    }

    private Node split(Node h) {
        Node rightNode = new Node(START_SPLIT_COUNT/2 - 1);
        int i = 0;
        int j = START_SPLIT_COUNT/2 + 1;
        while (j < START_SPLIT_COUNT) {
            rightNode.keyList[i] = h.keyList[j];
            i++;
            j++;
        }
        h.numberOfKeys = START_SPLIT_COUNT/2;
        return rightNode;
    }

    public String toString() {
        return toString(root, height, "") + "\n";
    }

    private String toString(Node h, int height, String indent) {
        StringBuilder s = new StringBuilder();
        if (height == 1) {
            for (int j = 0; j < h.numberOfKeys; j++) {
                s.append(indent + h.keyList[j].key + "\n");
            }
        } else {
            for (int j = 0; j < h.numberOfKeys; j++) {
                if (h.keyList[j].prev != null) {
                    s.append(toString(h.keyList[j].prev, height - 1, indent));
                }
                s.append(indent + h.keyList[j].key + "\n");
                if ((j + 1 == h.numberOfKeys) && (h.keyList[j].next != null)) {
                    s.append(toString(h.keyList[j].next, height - 1, indent));
                }
            }
        }
        return s.toString();
    }

    public String printTree() {
        return printTree(root, height, " ");
    }

    private String printTree(Node h, int height, String indent) {
        StringBuilder s = new StringBuilder();
        for (int j = 0; j < h.numberOfKeys; j++) {
            s.append(indent + h.keyList[j].key + ",");
        }
        if (height == 1) return s.toString();
        for (int j = 0; j < h.numberOfKeys; j++) {
            s.append("Left");
            if (h.keyList[j].prev != null) {
                s.append(printTree(h.keyList[j].prev, height - 1, indent));
                s.append("\n");
            }
            if ((j + 1 == h.numberOfKeys) && (h.keyList[j].next != null)) {
                s.append("Right");
                s.append(printTree(h.keyList[j].next, height - 1, indent));
                s.append("\n");
            }
        }
        return s.toString();
    }

    public void delete(K key) {
        if (key == null) throw new IllegalArgumentException("key is null");
        delete(root, key, height, null, 0);
    }

    private Node delete(Node h, K key, int height, Node parentNode,  int separatorIndex) {
        int j;
        Node newDeficientNode = null;
        if (height == 1) {
            for (j = 0; j < h.numberOfKeys; j++) {
                if (eq(key, h.keyList[j].key)) {
                    h.numberOfKeys--;
                    for (int i = j; i < h.numberOfKeys; i++) {
                        h.keyList[i] = h.keyList[i+1];
                    }
                    if (h.numberOfKeys < MIN_KEYS_COUNT) {
                        return rebalanceAfterDelete(parentNode, separatorIndex, height);
                    }
                }
            }
        } else {
            for (j = 0; j < h.numberOfKeys; j++) {
                if (eq(key, h.keyList[j].key)) {
                    deleteFromInternalNode(h, j, height);
                }
                if (less(key, h.keyList[j].key) && h.keyList[j].prev != null) {
                   newDeficientNode = delete(h.keyList[j].prev, key, height - 1, h, j);
                }
                if ((j + 1 == h.numberOfKeys) && h.keyList[j].next != null) {
                    newDeficientNode = delete(h.keyList[j].next, key, height - 1, h, j);
                }
            }
        }
        if (newDeficientNode != null) {
            return rebalanceAfterDelete(parentNode, separatorIndex, height);
        }
        return null;
    }

    private Node deleteFromInternalNode(Node parentNode, int deleteIndex, int height) {
        Entry oldSeparator = parentNode.keyList[deleteIndex];
        Entry newSeparator = null;
        if (oldSeparator.prev.numberOfKeys > oldSeparator.next.numberOfKeys) {
            newSeparator = new Entry(oldSeparator.prev.keyList[oldSeparator.prev.numberOfKeys-1].key, oldSeparator.prev.keyList[oldSeparator.prev.numberOfKeys-1].val);
            oldSeparator.prev.numberOfKeys--;
        }
        if (oldSeparator.prev.numberOfKeys <= oldSeparator.next.numberOfKeys) {
            newSeparator = new Entry(oldSeparator.next.keyList[0].key, oldSeparator.next.keyList[0].val);
            shiftToLeft(oldSeparator.next, 0);
        }
        if (newSeparator != null) {
            newSeparator.prev = oldSeparator.prev;
            newSeparator.next = oldSeparator.next;
        }
        parentNode.keyList[deleteIndex] = newSeparator;//delete oldSeparator
        if ((oldSeparator.prev.numberOfKeys < MIN_KEYS_COUNT) || (oldSeparator.next.numberOfKeys < MIN_KEYS_COUNT)) {
            return rebalanceAfterDelete(parentNode, deleteIndex, height);
        }
        return null;
    }

    private void rotateRight(Node parentNode, int separatorIndex, Node deficientNode, Node siblingNode) {
        Entry separatorEntry = parentNode.keyList[separatorIndex];
        for (int i = deficientNode.numberOfKeys; i > 0; i--) {
            deficientNode.keyList[i] = deficientNode.keyList[i-1];
        }
        deficientNode.keyList[0] = new Entry(separatorEntry.key, separatorEntry.val);
        deficientNode.keyList[0].next = deficientNode.keyList[1].prev;
        deficientNode.keyList[0].prev = siblingNode.keyList[siblingNode.numberOfKeys-1].next;
        deficientNode.numberOfKeys++;
        parentNode.keyList[separatorIndex] = new Entry(siblingNode.keyList[siblingNode.numberOfKeys-1].key, siblingNode.keyList[siblingNode.numberOfKeys-1].val);
        siblingNode.numberOfKeys--;
        parentNode.keyList[separatorIndex].prev = siblingNode;
        parentNode.keyList[separatorIndex].next = deficientNode;
    }

    private void rotateLeft(Node parentNode, int separatorIndex, Node deficientNode, Node siblingNode) {
        Entry separatorEntry = parentNode.keyList[separatorIndex];
        deficientNode.keyList[deficientNode.numberOfKeys] = new Entry(separatorEntry.key, separatorEntry.val);
        deficientNode.keyList[deficientNode.numberOfKeys].prev = siblingNode.keyList[siblingNode.numberOfKeys-1].next;
        deficientNode.numberOfKeys++;
        parentNode.keyList[separatorIndex] = new Entry(siblingNode.keyList[0].key, siblingNode.keyList[0].val);
        deficientNode.keyList[deficientNode.numberOfKeys].next = siblingNode.keyList[0].prev;
        for (int i = 0; i < siblingNode.numberOfKeys; i++) {
            siblingNode.keyList[i] = siblingNode.keyList[i + 1];
        }
        siblingNode.numberOfKeys--;
        parentNode.keyList[separatorIndex].prev = siblingNode;
        parentNode.keyList[separatorIndex].next = deficientNode;
    }

    private Node mergeAllToLeft(Node parentNode, int separatorIndex, Node deficientNode, Node siblingNode) {
        deficientNode.keyList[deficientNode.numberOfKeys] = new Entry(parentNode.keyList[separatorIndex].key, parentNode.keyList[separatorIndex].val);
        deficientNode.numberOfKeys++;
        for (int i = 0; i < siblingNode.numberOfKeys; i++) {
            deficientNode.keyList[deficientNode.numberOfKeys] = new Entry(siblingNode.keyList[i].key, siblingNode.keyList[i].val);
            deficientNode.numberOfKeys++;// deficientNode is full
        }
        return packAfterMergeChilAndDeleteParent(parentNode, separatorIndex, deficientNode);
    }

    private Node mergeAllToRight(Node parentNode, int separatorIndex, Node deficientNode, Node siblingNode) {
        Node newNode = new Node(siblingNode.numberOfKeys + 1 + deficientNode.numberOfKeys);
        int i = 0;
        for (i = 0; i < siblingNode.numberOfKeys; i++) {
            newNode.keyList[i] = siblingNode.keyList[i];
        }
        newNode.keyList[i] = new Entry(parentNode.keyList[separatorIndex].key, parentNode.keyList[separatorIndex].val);
        for (int j = 0; j < deficientNode.numberOfKeys; j++) {
            newNode.keyList[++i] = deficientNode.keyList[j];
        }
        return packAfterMergeChilAndDeleteParent(parentNode, separatorIndex, newNode);
    }

    private Node  packAfterMergeChilAndDeleteParent(Node parentNode, int separatorIndex, Node newNode) {
        parentNode.keyList[separatorIndex - 1].next = newNode;
        if (parentNode.keyList[separatorIndex + 1] != null) {
            parentNode.keyList[separatorIndex + 1].prev = newNode;
        }
        return shiftToLeft(parentNode, separatorIndex);
    }
    private Node shiftToLeft(Node node, int shiftIndex) {
        node.numberOfKeys--;
        for (int i = shiftIndex; i < node.numberOfKeys; i++) {
            node.keyList[i] = node.keyList[i+1];
        }
        return node;
    }
    private Node rebalanceAfterDelete(Node parentNode, int separatorIndex, int height) {
        Node newNode = null;
        if (parentNode.keyList[separatorIndex].prev.numberOfKeys < parentNode.keyList[separatorIndex].next.numberOfKeys) {
            if (parentNode.keyList[separatorIndex].next.numberOfKeys > MIN_KEYS_COUNT) {
                rotateLeft(parentNode, separatorIndex, parentNode.keyList[separatorIndex].prev, parentNode.keyList[separatorIndex].next);
                return null;
            } else {
                newNode = mergeAllToLeft(parentNode, separatorIndex, parentNode.keyList[separatorIndex].prev, parentNode.keyList[separatorIndex].next);
            }
        } else {
            if (parentNode.keyList[separatorIndex].prev.numberOfKeys > MIN_KEYS_COUNT) {
                rotateRight(parentNode, separatorIndex, parentNode.keyList[separatorIndex].next, parentNode.keyList[separatorIndex].prev);
                return null;
            } else {
                newNode = mergeAllToRight(parentNode, separatorIndex, parentNode.keyList[separatorIndex].prev, parentNode.keyList[separatorIndex].next);
            }
        }
        if (parentNode.numberOfKeys == 0 && height == 0) {
            root = newNode;
            return null;
        }
        if (parentNode.numberOfKeys < MIN_KEYS_COUNT) {
            return parentNode;
        }
        return null;
    }

    private boolean less(Comparable k1, Comparable k2) {
        return k1.compareTo(k2) < 0;
    }
    private boolean eq(Comparable k1, Comparable k2) {
        return k1.compareTo(k2) == 0;
    }
    private boolean gt(Comparable k1, Comparable k2) {
        return k1.compareTo(k2) > 0;
    }

    public static void main(String[] args) {

        BTree<String, String> st = new BTree<String, String>();

        st.put("A","A");
        st.put("B","B");
        st.put("C","C");
        st.put("D","D");
        st.put("E","E");
        st.put("F","F");
        st.put("G","G");
        st.put("H","H");
        st.put("I","I");
        st.put("J","J");
        st.put("K","K");
        st.put("L","L");
        st.put("M","M");
        st.put("N","N");
        st.put("O","O");
        st.put("P","P");
        st.put("Q","Q");
        st.put("R","R");
        st.put("S","S");
        st.put("T","T");
        st.put("U","U");
        st.put("V","V");
        st.put("W","W");
        st.put("X","X");
        st.put("Y","Y");
        st.put("Z","Z");
        System.out.println("height:  " + st.height());
       // System.out.println(st);
      //  System.out.println(st.printTree());
       st.delete("Z");
      //  System.out.println(st.printTree());
        st.delete("Y");
        System.out.println(st.printTree());
    }
}
