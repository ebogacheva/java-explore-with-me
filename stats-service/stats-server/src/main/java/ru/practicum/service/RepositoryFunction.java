package ru.practicum.service;

@FunctionalInterface
public interface RepositoryFunction<T, U, V, W, R> {
    R apply(T t, U u, V v, W w, R r);
}
