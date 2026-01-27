package com.hosttale.htlevelsystem.db;

public final class DatabaseConfig {
    public enum Backend { SQLITE, POSTGRES, MYSQL }

    private Backend backend = Backend.SQLITE;
    private Sqlite sqlite = new Sqlite();
    private Postgres postgres = new Postgres();
    private Mysql mysql = new Mysql();

    public Backend getBackend() {
        return backend;
    }

    public void setBackend(Backend backend) {
        this.backend = backend;
    }

    public Sqlite getSqlite() {
        return sqlite;
    }

    public Postgres getPostgres() {
        return postgres;
    }

    public Mysql getMysql() {
        return mysql;
    }

    public static DatabaseConfig defaults() {
        return new DatabaseConfig();
    }

    public static final class Sqlite {
        private String file = "users.sql";

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }
    }

    public static final class Postgres {
        private String host = "localhost";
        private int port = 5432;
        private String database = "hytale";
        private String user = "hytale";
        private String password = "change_me";
        private boolean ssl = false;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getDatabase() {
            return database;
        }

        public void setDatabase(String database) {
            this.database = database;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public boolean isSsl() {
            return ssl;
        }

        public void setSsl(boolean ssl) {
            this.ssl = ssl;
        }
    }

    public static final class Mysql {
        private String host = "localhost";
        private int port = 3306;
        private String database = "hytale";
        private String user = "hytale";
        private String password = "change_me";
        private boolean useSsl = false;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getDatabase() {
            return database;
        }

        public void setDatabase(String database) {
            this.database = database;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public boolean isUseSsl() {
            return useSsl;
        }

        public void setUseSsl(boolean useSsl) {
            this.useSsl = useSsl;
        }
    }
}
