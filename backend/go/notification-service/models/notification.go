package models

import (
	"context"
	"encoding/json"
	"log"
	"time"

	"github.com/google/uuid"
	"nyihtuun.com/bentosystem/repository"
)

type Notification struct {
	ID        int64     `json:"id"`
	UserID    uuid.UUID `json:"userId"`
	Type      string    `json:"type"`
	Payload   json.RawMessage
	Read      bool      `json:"read"`
	CreatedAt time.Time `json:"createdAt"`
	ReadAt    time.Time `json:"readAt"`
}

func GetNotificationsByUserId(userId uuid.UUID) ([]Notification, error) {
	var notifications []Notification
	query := `SELECT * FROM "notification".notification WHERE user_id = $1 AND read = false`
	rows, err := repository.DBpool.Query(context.Background(), query, userId)
	if err != nil {
		log.Fatalf("unable to query rows: %v\n", err)
		return nil, err
	}

	defer rows.Close()

	for rows.Next() {
		var notification Notification
		err := rows.Scan(&notification.ID, &notification.UserID, &notification.Type, &notification.Payload, &notification.Read, &notification.CreatedAt, &notification.ReadAt)
		if err != nil {
			log.Fatalf("unable to scan row: %v\n", err)
			return nil, err
		}

		notifications = append(notifications, notification)
	}
	log.Fatalf("rows: %v\n", notifications)
	return notifications, nil
}

func GetNotificationById(id int64) (Notification, error) {
	var notification Notification
	query := `SELECT * FROM "notification".notification WHERE id = $1`
	row := repository.DBpool.QueryRow(context.Background(), query, id)
	err := row.Scan(&notification.ID, &notification.UserID, &notification.Type, &notification.Payload, &notification.Read, &notification.CreatedAt, &notification.ReadAt)
	if err != nil {
		log.Fatalf("unable to scan row: %v\n", err)
		return notification, err
	}
	return notification, nil
}

func (notification *Notification) Save() error {
	query := `
		INSERT INTO "notification".notification (
		 user_id, type, payload, created_at
		) VALUES ($1, $2, $3, $4)
	`

	_, err := repository.DBpool.Exec(context.Background(), query, notification.UserID, notification.Type, notification.Payload, notification.CreatedAt)
	if err != nil {
		log.Fatalf("unable to insert into notifications table: %v\n", err)
		return err
	}

	return nil
}

func (notification *Notification) MarkAsRead() error {
	query := `UPDATE "notification".notification SET read = true, read_at = CURRENT_TIMESTAMP WHERE id = $1`
	_, err := repository.DBpool.Exec(context.Background(), query, notification.ID)
	if err != nil {
		log.Fatalf("unable to update notification: %v\n", err)
		return err
	}
	return nil
}

func DeleteNotificationsOneWeekBeforeByUserId(userId uuid.UUID) error {
	query := `DELETE FROM "notification".notification WHERE user_id = $1 AND created_at < CURRENT_TIMESTAMP - INTERVAL '1 week'`
	results, err := repository.DBpool.Exec(context.Background(), query, userId)
	if err != nil {
		log.Fatalf("unable to delete notifications: %v\n", err)
		return err
	}
	log.Printf("deleted %d notifications\n", results.RowsAffected())
	return nil
}
