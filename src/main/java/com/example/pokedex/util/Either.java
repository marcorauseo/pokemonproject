package com.example.pokedex.util;

import java.util.function.Function;

public class Either<L, R> {
    private final L left;
    private final R right;

    private Either(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public static <L,R> Either<L,R> left(L l) { return new Either<>(l, null); }
    public static <L,R> Either<L,R> right(R r) { return new Either<>(null, r); }

    public boolean isLeft() { return left != null; }
    public boolean isRight() { return right != null; }
    public L getLeft() { return left; }
    public R getRight() { return right; }

    public <L2> Either<L2,R> mapLeft(Function<L,L2> f) {
        return isLeft() ? Either.left(f.apply(left)) : Either.right(right);
        }
    public <R2> Either<L,R2> mapRight(Function<R,R2> f) {
        return isRight() ? Either.right(f.apply(right)) : Either.left(left);
    }

    public <T> T fold(Function<L,T> lf, Function<R,T> rf) {
        return isLeft() ? lf.apply(left) : rf.apply(right);
    }
}
