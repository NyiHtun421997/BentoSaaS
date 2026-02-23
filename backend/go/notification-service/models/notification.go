package models

import (
	"context"
	"database/sql"
	"encoding/json"
	"log"
	"time"

	"github.com/google/uuid"
	"nyihtuun.com/bentosystem/repository"
)

type Notification struct {
	ID               int64           `json:"id"`
	UserID           uuid.UUID       `json:"userId"`
	PlanID           uuid.UUID       `json:"planId"`
	NotificationType string          `json:"notificationType"`
	Payload          json.RawMessage `json:"payload"`
	Read             bool            `json:"read"`
	CreatedAt        time.Time       `json:"createdAt"`
	ReadAt           sql.NullTime    `json:"readAt"`
}

func New(userId uuid.UUID, planId uuid.UUID, notificationType string, payload json.RawMessage) *Notification {
	return &Notification{
		UserID:           userId,
		PlanID:           planId,
		NotificationType: notificationType,
		Payload:          payload,
	}
}

func GetNotificationsByUserId(userId uuid.UUID) ([]Notification, error) {
	var notifications []Notification
	query := `SELECT id, user_id, plan_id, type, payload, read, created_at, read_at FROM "notification".notification WHERE user_id = $1`
	rows, err := repository.DBpool.Query(context.Background(), query, userId)
	if err != nil {
		log.Printf("unable to query rows: %v\n", err)
		return nil, err
	}

	defer rows.Close()

	for rows.Next() {
		var notification Notification
		err := rows.Scan(&notification.ID, &notification.UserID, &notification.PlanID, &notification.NotificationType, &notification.Payload, &notification.Read, &notification.CreatedAt, &notification.ReadAt)
		if err != nil {
			log.Printf("unable to scan row: %v\n", err)
			return nil, err
		}

		notifications = append(notifications, notification)
	}
	return notifications, nil
}

func GetNotificationById(id int64) (Notification, error) {
	var notification Notification
	query := `SELECT id, user_id, plan_id, type, payload, read, created_at, read_at FROM "notification".notification WHERE id = $1`
	row := repository.DBpool.QueryRow(context.Background(), query, id)
	err := row.Scan(&notification.ID, &notification.UserID, &notification.PlanID, &notification.NotificationType, &notification.Payload, &notification.Read, &notification.CreatedAt, &notification.ReadAt)
	if err != nil {
		log.Printf("unable to scan row: %v\n", err)
		return notification, err
	}
	return notification, nil
}

func (notification *Notification) Save() error {
	query := `
		INSERT INTO "notification".notification (
		 user_id, plan_id, type, payload
		) VALUES ($1, $2, $3, $4)
	`

	_, err := repository.DBpool.Exec(context.Background(), query, notification.UserID, notification.PlanID, notification.NotificationType, notification.Payload)
	if err != nil {
		log.Printf("unable to insert into notifications table: %v\n", err)
		return err
	}

	return nil
}

func (notification *Notification) MarkAsRead() error {
	query := `UPDATE "notification".notification SET read = true, read_at = CURRENT_TIMESTAMP WHERE id = $1`
	_, err := repository.DBpool.Exec(context.Background(), query, notification.ID)
	if err != nil {
		log.Printf("unable to update notification: %v\n", err)
		return err
	}
	return nil
}

func DeleteNotificationsOneWeekBefore() error {
	query := `DELETE FROM "notification".notification WHERE created_at < CURRENT_TIMESTAMP - INTERVAL '1 week'`
	results, err := repository.DBpool.Exec(context.Background(), query)
	if err != nil {
		log.Printf("unable to delete notifications: %v\n", err)
		return err
	}
	log.Printf("deleted %d notifications\n", results.RowsAffected())
	return nil
}
