/*
 * Copyright 2016, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.sync.jobs.common;

import java.util.NoSuchElementException;
import java.util.function.Function;

/**
 *
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public abstract class Either<L, R> {


    public static <L, R> Either<L, R> fromLeft(L left, Class<R> right) {
        return new Left<>(left);
    }

    public static <L, R> Either<L, R> fromRight(Class<L> left, R right) {
        return new Right<>(right);
    }

    public abstract L getLeft();

    public abstract R getRight();

    public abstract boolean hasLeft();

    public abstract boolean hasRight();

    public abstract <S> S map(final Function<? super L, S> left, final Function<? super R, ? extends S> right);

    private static class Left<L, R> extends Either<L, R> {
        private final L left;
        private Left(L left) {
            this.left = left;
        }

        @Override
        public L getLeft() {
            return left;
        }

        @Override
        public R getRight() {
            throw new NoSuchElementException("no right in Left");
        }

        @Override
        public boolean hasLeft() {
            return true;
        }

        @Override
        public boolean hasRight() {
            return false;
        }

        @Override
        public <S> S map(Function<? super L, S> leftFn,
                Function<? super R, ? extends S> rightFn) {
            return leftFn.apply(left);
        }
    }

    private static class Right<L, R> extends Either<L, R> {
        private final R right;
        private Right(R right) {
            this.right = right;
        }

        @Override
        public L getLeft() {
            throw new NoSuchElementException("no left in Right");
        }

        @Override
        public R getRight() {
            return right;
        }

        @Override
        public boolean hasLeft() {
            return false;
        }

        @Override
        public boolean hasRight() {
            return true;
        }

        @Override
        public <S> S map(Function<? super L, S> leftFn,
                Function<? super R, ? extends S> rightFn) {
            return rightFn.apply(right);
        }
    }

}
