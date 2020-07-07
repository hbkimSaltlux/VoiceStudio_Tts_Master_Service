package com.saltlux.tts.util;

import java.io.Serializable;

public class SimpleEntry<K extends Object, V extends Object>
    implements Comparable<SimpleEntry<K, V>>, Serializable {

  private static final long serialVersionUID = 3808707498312840665L;
  
  private K key;
  private V value;

  public SimpleEntry() {
  }

  public SimpleEntry(K key, V value) {
    this.key = key;
    this.value = value;
  }

  public K getKey() {
    return key;
  }

  public V getValue() {
    return value;
  }

  public V setValue(V value) {
    V oldValue = this.value;
    this.value = value;
    return oldValue;
  }

  public int hashCode() {
    return ((key == null) ? 0 : key.hashCode());
  }

  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (obj == null || obj.getClass() != this.getClass()) {
      return false;
    } else {
      SimpleEntry<?, ?> entry = (SimpleEntry<?, ?>) obj;
      return eq(key, entry.getKey());
    }
  }

  private static boolean eq(Object o1, Object o2) {
    return (o1 == null) ? (o2 == null) : o1.equals(o2);
  }

  public String toString() {
    return key.toString() + "=" + value.toString();
  }

  @Override
  public int compareTo(SimpleEntry<K, V> obj) {
    if (obj.value instanceof Number) {
      Number oo = (Number) obj.value;
      Number tt = (Number) this.value;
      double diff = oo.doubleValue() - tt.doubleValue();
      return checkDiff(diff);
    } else {
      return 0;
    }
  }
  
  private static int checkDiff(double diff) {
    if (diff > 0) {
      return -1;
    } else if (diff < 0) {
      return 1;
    } else {
      return 0;
    }
  }
}
