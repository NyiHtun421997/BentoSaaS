package handlers

import (
	"errors"
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"nyihtuun.com/bentosystem/models"
)

func fetchUserId(context *gin.Context) (uuid.UUID, error) {
	userId, exists := context.Get("userId")
	if !exists {
		return uuid.Nil, errors.New("user ID not found in context")
	}

	userIdStr, ok := userId.(string)
	if !ok {
		return uuid.Nil, errors.New("user ID type mismatch in context")
	}

	uid, err := uuid.Parse(userIdStr)
	if err != nil {
		return uuid.Nil, err
	}

	return uid, nil
}

func GetNotificationsByUserId(context *gin.Context) {
	uid, err := fetchUserId(context)
	if err != nil {
		context.JSON(http.StatusBadRequest, gin.H{"error": "Failed to retrieve user ID"})
		return
	}

	notifications, err := models.GetNotificationsByUserId(uid)
	if err != nil {
		context.JSON(http.StatusBadRequest, gin.H{"error": "Failed to retrieve notifications"})
		return
	}
	context.JSON(http.StatusOK, notifications)
}

func MarkNotificationsAsRead(context *gin.Context) {
	uid, err := fetchUserId(context)
	if err != nil {
		context.JSON(http.StatusBadRequest, gin.H{"error": "Failed to retrieve user ID"})
		return
	}

	id, err := strconv.ParseInt(context.Param("id"), 10, 64)
	if err != nil {
		context.JSON(http.StatusBadRequest, gin.H{"error": "Invalid notification ID"})
		return
	}

	notificationById, err := models.GetNotificationById(id)
	if err != nil {
		context.JSON(http.StatusBadRequest, gin.H{"error": "Notification not found"})
		return
	}

	if notificationById.UserID != uid {
		context.JSON(http.StatusForbidden, gin.H{"error": "Forbidden"})
		return
	}

	err = notificationById.MarkAsRead()
	if err != nil {
		context.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to mark notification as read"})
		return
	}
	context.JSON(http.StatusOK, gin.H{"message": "Notification marked as read"})
}
