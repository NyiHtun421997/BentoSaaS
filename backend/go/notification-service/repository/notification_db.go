package repository

import (
	"context"
	"log"
	"time"

	pgxuuid "github.com/jackc/pgx-gofrs-uuid"
	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"
	"nyihtuun.com/bentosystem/config"
)

var DBpool *pgxpool.Pool

func InitNotificationDB() {
	var err error

	dbConfig, err := pgxpool.ParseConfig(config.Cfg.DBParams.Url)
	if err != nil {
		log.Fatalf("unable to parse database url: %v\n", err)
		return
	}
	dbConfig.MaxConns = 10
	dbConfig.MaxConnIdleTime = time.Minute * 3

	dbConfig.AfterConnect = func(ctx context.Context, conn *pgx.Conn) error {
		pgxuuid.Register(conn.TypeMap())
		return nil
	}

	DBpool, err = pgxpool.NewWithConfig(context.Background(), dbConfig)
	if err != nil {
		log.Fatalf("unable to create connection pool: %v\n", err)
	}

	createTable()
}

func createTable() {
	createSchema := `CREATE SCHEMA IF NOT EXISTS notification`
	_, err := DBpool.Exec(context.Background(), createSchema)
	if err != nil {
		log.Fatalf("unable to create schema: %v\n", err)
		return
	}

	createUUIDExtension := `CREATE EXTENSION IF NOT EXISTS "uuid-ossp"`
	_, err = DBpool.Exec(context.Background(), createUUIDExtension)
	if err != nil {
		log.Fatalf("unable to create uuid-ossp extension: %v\n", err)
		return
	}

	createNotificationTable := `
	CREATE TABLE IF NOT EXISTS "notification".notification (
	    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	    user_id UUID NOT NULL,
	    type VARCHAR(255) NOT NULL,
	    payload JSONB,
	    read BOOLEAN DEFAULT FALSE,
	    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
	    read_at TIMESTAMPTZ,
	    CONSTRAINT type_allowed CHECK (type IN ('PLAN_UPDATED_EVENT', 'PLAN_DELETED_EVENT', 'INVOICE_ISSUED_EVENT'))
	)
	`
	_, err = DBpool.Exec(context.Background(), createNotificationTable)
	if err != nil {
		log.Fatalf("unable to create notifications table: %v\n", err)
	}
}

func CloseDB() {
	DBpool.Close()
}
