package handlers

import (
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"nyihtuun.com/bentosystem/models"
)

func GetNotificationsByUserId(context *gin.Context) {
	userId, exists := context.Get("userId")
	if !exists {
		context.JSON(http.StatusUnauthorized, gin.H{"error": "User ID not found in context"})
		return
	}

	userIdStr, ok := userId.(string)
	if !ok {
		context.JSON(http.StatusInternalServerError, gin.H{"error": "User ID type mismatch in context"})
		return
	}

	uid, err := uuid.Parse(userIdStr)
	if err != nil {
		context.JSON(http.StatusInternalServerError, gin.H{"error": "Invalid user ID format in context"})
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

	err = notificationById.MarkAsRead()
	if err != nil {
		context.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to mark notification as read"})
		return
	}
	context.JSON(http.StatusOK, gin.H{"message": "Notification marked as read"})
}
