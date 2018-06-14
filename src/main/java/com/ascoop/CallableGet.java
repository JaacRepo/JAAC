package com.ascoop;

@FunctionalInterface
public interface CallableGet<T, V> {
	Future<T> run(V futValue);
}