public class BTree<K extends Comparable<K>, V> {
    private static final int MAX_KEYS_COUNT = 3;
    private Node root;
    private int height;

    private static final class Node {
        private int numberOfKeys;
        private Entry[] keyList = new Entry[MAX_KEYS_COUNT];
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
        newRootNode.keyList[0] = root.keyList[MAX_KEYS_COUNT/2];
        root.numberOfKeys = MAX_KEYS_COUNT/2;
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
                t = new Entry(childNode.keyList[MAX_KEYS_COUNT/2].key, childNode.keyList[MAX_KEYS_COUNT/2].val);
                t.next = newNode;
                t.prev = childNode;
            }
        }
        for (int i = h.numberOfKeys; i > j; i--) {
            h.keyList[i] = h.keyList[i-1];
        }
        h.keyList[j] = t;
        h.numberOfKeys++;
        if (h.numberOfKeys < MAX_KEYS_COUNT) return null;
        else return split(h);
    }

    private Node split(Node h) {
        Node rightNode = new Node(MAX_KEYS_COUNT/2);
        int i = 0;
        int j = MAX_KEYS_COUNT/2 + 1;
        while (j < MAX_KEYS_COUNT) {
            rightNode.keyList[i] = h.keyList[j];
            i++;
            j++;
        }
        h.numberOfKeys = MAX_KEYS_COUNT/2;
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

        BTree<Integer, Integer> st = new BTree<Integer, Integer>();

        st.put(2,2);
        st.put(3,3);
        st.put(1,1);
        st.put(4,4);
        st.put(5,5);
        st.put(6,6);
        st.put(7,7);
        System.out.println("height:  " + st.height());
        System.out.println(st);
        System.out.println(st.get(5));
    }
}
