package ht.treechop.common.config;

public enum Rounder {
    DOWN {
        @Override
        public int round(double value) {
            return (int) Math.floor(value);
        }
    },
    NEAREST {
        @Override
        public int round(double value) {
            return (int) Math.round(value);
        }
    },
    UP {
        @Override
        public int round(double value) {
            return (int) Math.ceil(value);
        }
    };

    public abstract int round(double value);
}
